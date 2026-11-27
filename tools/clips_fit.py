"""
Fits the recorded voices of a clip to its untouched picture, and makes the review copy. Used by tools/make_clips.py (fit, cut, review).

The mouth timing comes from the picture's own sound: the model that made the clip made the speech with the mouth, so where its sound is
speech is where the mouth moves. Those spans are found (speech-band energy), the model's sound is muted inside them (what is left is the
sound effects), and each recorded line is shifted to the start of its span and stretched or squeezed to its length (pitch kept, by
ffmpeg's atempo through PyAV). Nothing in the picture is touched. A clip made with the recording as its reference (Seedance) has its
spans where the recording was laid.
"""
import fractions
import pathlib

import av
import numpy
import soundfile
from PIL import Image, ImageDraw, ImageFont

RATE = 48000
LIMITS = (0.8, 1.5)  # how far a line may be slowed and sped


def read_audio(path, rate=RATE):
    """The sound of a file as mono floats at a rate; empty if it has none."""
    box = av.open(str(path))
    if not box.streams.audio:
        return numpy.zeros(0, dtype=numpy.float32)
    resampler = av.AudioResampler(format="flt", layout="mono", rate=rate)
    pieces = []
    for frame in box.decode(audio=0):
        for out in resampler.resample(frame):
            pieces.append(out.to_ndarray().reshape(-1))
    for out in resampler.resample(None):
        pieces.append(out.to_ndarray().reshape(-1))
    box.close()
    return numpy.concatenate(pieces) if pieces else numpy.zeros(0, dtype=numpy.float32)


def speech_spans(sound, rate=RATE, floor=0.28):
    """Where the speech is in a sound, as (begin, end) seconds: energy between 300 and 3400 Hz over time, against its own loudest part."""
    import scipy.signal

    if len(sound) < rate // 2:
        return []
    band = scipy.signal.sosfilt(scipy.signal.butter(4, [300, 3400], "bandpass", fs=rate, output="sos"), sound)
    hop = rate // 50
    count = len(band) // hop
    energy = numpy.sqrt((band[: count * hop].reshape(count, hop) ** 2).mean(axis=1))
    if energy.max() < 1e-4:
        return []
    level = numpy.percentile(energy, 95)
    on = energy > level * floor
    # Small gaps (a breath between words) are closed; blips are dropped.
    spans, start = [], None
    for i, flag in enumerate(on):
        if flag and start is None:
            start = i
        if not flag and start is not None:
            spans.append([start, i])
            start = None
    if start is not None:
        spans.append([start, count])
    merged = []
    for a, b in spans:
        if merged and a - merged[-1][1] < 14:
            merged[-1][1] = b
        else:
            merged.append([a, b])
    return [(a * hop / rate, b * hop / rate) for a, b in merged if b - a >= 12]


def match_spans(spans, lengths, fallback):
    """One window for each line: the spans closed up or split until there are as many as lines; or the fallback windows if there are none."""
    want = len(lengths)
    spans = [list(s) for s in spans]
    if not spans:
        return fallback
    # Gaps of a breath are closed; if there are still too many, the longest spans are the speech and the rest are effects.
    i = 0
    while i < len(spans) - 1:
        if spans[i + 1][0] - spans[i][1] < .45:
            spans[i][1] = spans[i + 1][1]
            del spans[i + 1]
        else:
            i += 1
    if len(spans) > want:
        keep = sorted(sorted(spans, key=lambda s: s[0] - s[1])[:want])
        spans = [list(s) for s in keep]
    while len(spans) < want:
        # Split the longest span in proportion to the lines that must share it, or fall back.
        i = int(numpy.argmax([b - a for a, b in spans]))
        a, b = spans[i]
        if b - a < 1.2:
            return fallback
        half = (a + b) / 2
        spans[i:i + 1] = [[a, half - .05], [half + .05, b]]
    return [tuple(s) for s in spans]


def stretch(sound, factor, rate=RATE):
    """A sound played `factor` times as fast with its pitch kept (ffmpeg's atempo, in steps if need be)."""
    if abs(factor - 1) < .02:
        return sound
    graph = av.filter.Graph()
    source = graph.add_abuffer(format="flt", sample_rate=rate, layout="mono", time_base=fractions.Fraction(1, rate))
    last = source
    tempo = []
    f = factor
    while f > 2.0:
        tempo.append(2.0)
        f /= 2.0
    while f < .5:
        tempo.append(.5)
        f /= .5
    tempo.append(f)
    for t in tempo:
        node = graph.add("atempo", f"{t:.5f}")
        last.link_to(node)
        last = node
    sink = graph.add("abuffersink")
    last.link_to(sink)
    graph.configure()
    pieces = []
    data = sound.astype(numpy.float32)
    step = 1024
    for i in range(0, len(data), step):
        chunk = data[i:i + step]
        frame = av.AudioFrame.from_ndarray(chunk.reshape(1, -1), format="flt", layout="mono")
        frame.sample_rate = rate
        frame.pts = i
        frame.time_base = fractions.Fraction(1, rate)
        source.push(frame) if hasattr(source, "push") else graph.push(frame)
        while True:
            try:
                out = sink.pull() if hasattr(sink, "pull") else graph.pull()
            except (av.error.BlockingIOError, av.error.EOFError):
                break
            pieces.append(out.to_ndarray().reshape(-1))
    try:
        (source.push(None) if hasattr(source, "push") else graph.push(None))
        while True:
            out = sink.pull() if hasattr(sink, "pull") else graph.pull()
            pieces.append(out.to_ndarray().reshape(-1))
    except (av.error.BlockingIOError, av.error.EOFError):
        pass
    return numpy.concatenate(pieces) if pieces else data


def fit_lines(voices, windows, total, bed, rate=RATE, mute=True):
    """
    The track of a clip in one language: the model's sound (the effects) with its speech spans muted, and each voice laid in its window.
    voices: recorded lines (arrays at `rate`); windows: (begin, end) seconds for each. Gives the track and the (begin, end) each line really has.
    """
    track = numpy.zeros(int(total * rate), dtype=numpy.float32)
    if len(bed):
        track[: min(len(track), len(bed))] = bed[: len(track)]
    placed = []
    for sound, (a, b) in zip(voices, windows):
        # Mute the bed inside the window, with short fades.
        i, j = int(a * rate), min(len(track), int(b * rate))
        fade = int(.04 * rate)
        gain = numpy.ones(len(track), dtype=numpy.float32)
        lo, hi = max(0, i - fade), min(len(track), j + fade)
        floor = .06 if mute else .4
        gain[lo:hi] = floor
        ramp = numpy.linspace(1, floor, i - lo) if i > lo else numpy.zeros(0)
        gain[lo:i] = ramp
        gain[j:hi] = numpy.linspace(floor, 1, hi - j) if hi > j else gain[j:hi]
        track *= gain
    for sound, (a, b) in zip(voices, windows):
        length = len(sound) / rate
        factor = min(LIMITS[1], max(LIMITS[0], length / max(.3, b - a)))
        fitted = stretch(sound, factor, rate)
        start = int(a * rate)
        end = min(len(track), start + len(fitted))
        track[start:end] += fitted[: end - start]
        placed.append((start / rate, end / rate))
    peak = numpy.abs(track).max()
    if peak > .95:
        track *= .95 / peak
    return track, placed


def font(size, wide=False):
    for name in (["malgun.ttf", "msyh.ttc", "YuGothM.ttc", "segoeui.ttf"] if wide else ["segoeuib.ttf", "arialbd.ttf"]):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def captioned(frame_image, text, size=44):
    """A frame with its caption at the bottom: white with a dark outline."""
    image = frame_image.copy()
    draw = ImageDraw.Draw(image)
    f = font(size, wide=any(ord(c) > 0x2e80 for c in text))
    words, lines, line = text.split(" "), [], ""
    for w in words:
        trial = (line + " " + w).strip()
        if draw.textlength(trial, font=f) > image.width * .86 and line:
            lines.append(line)
            line = w
        else:
            line = trial
    lines.append(line)
    y = image.height - 70 - len(lines) * (size + 8)
    for l in lines:
        x = (image.width - draw.textlength(l, font=f)) / 2
        draw.text((x, y), l, font=f, fill="white", stroke_width=4, stroke_fill="black")
        y += size + 8
    return image


def write_video(source, target, audio, captions=None, bitrate=None, size=None, fps=None, holds=()):
    """
    The source picture, with an audio track laid under it (mono floats at RATE), and captions [(begin, end, words)] drawn on if given.
    The picture is re-encoded (H.264) at `bitrate`; with no captions and no size it is the same size as the source.
    """
    box = av.open(str(source))
    video = box.streams.video[0]
    rate = video.average_rate
    out = av.open(str(target), "w", options={"movflags": "+faststart"})
    vs = out.add_stream("libx264", rate=rate)
    vs.width, vs.height = size or (video.width, video.height)
    vs.pix_fmt = "yuv420p"
    if bitrate:
        vs.bit_rate = bitrate
        vs.options = {"preset": "slow", "maxrate": str(int(bitrate * 1.5)), "bufsize": str(bitrate * 2)}
    else:
        vs.options = {"crf": "16", "preset": "slow"}
    a = out.add_stream("aac", rate=RATE, layout="mono")
    a.bit_rate = 160000
    kept = None
    for frame in box.decode(video):
        t = float(frame.pts * frame.time_base)
        image = frame.to_image()
        if size:
            image = image.resize(size, Image.LANCZOS)
        # A span named in holds shows the frame before it instead: nothing is drawn over a frame.
        if any(a <= t <= b for a, b in holds) and kept is not None:
            image = kept
        else:
            kept = image
        if captions:
            for begin, end, words in captions:
                if begin <= t <= end:
                    image = captioned(image, words, 46 if image.height >= 1000 else 30)
                    break
        for packet in vs.encode(av.VideoFrame.from_image(image)):
            out.mux(packet)
    for packet in vs.encode():
        out.mux(packet)
    box.close()
    pcm = numpy.clip(audio, -1, 1).astype(numpy.float32)
    step = 1024
    for i in range(0, len(pcm), step):
        chunk = pcm[i:i + step]
        frame = av.AudioFrame.from_ndarray(chunk.reshape(1, -1), format="flt", layout="mono")
        frame.sample_rate = RATE
        frame.pts = i
        for packet in a.encode(frame):
            out.mux(packet)
    for packet in a.encode():
        out.mux(packet)
    out.close()



def nearest_spans(spans, nominal):
    """
    For each line, the speech span that begins nearest where the clip was asked for the line (within three seconds), each span used once and in order;
    a line with none near is left at its asked place. A span shorter than the line needs is grown, forwards, to a fifth under the line's length.
    """
    out, used = [], set()
    for a0, b0 in nominal:
        need = (b0 - a0) / 1.3
        best, dist = None, 6.0
        for k, (a, b) in enumerate(spans):
            if k in used:
                continue
            d = abs(a - a0)
            if d < dist:
                best, dist = k, d
        if best is None:
            out.append((a0, b0))
            continue
        used.add(best)
        a, b = spans[best]
        out.append((a, max(b, a + need)))
    return out





