"""
The Japanese prologue with its picture slowed between 0:05 and 0:30, so that Halgakos's lines there are said at natural speed.

    python tools/prologue_ja_slow.py spec F        writes tools/cutscenes/prologue-slow-mc.json for a slowing of F (1.25: a quarter slower), and the stretched band
    python tools/prologue_ja_slow.py picture F     the picture: 0:05-0:30 of the shipped picture slowed by F with motion-compensated in-between frames

The map of time: before 5 s nothing changes; from 5 s to 30 s the film is F times as long; after 30 s everything is later by D = 25 (F - 1) seconds.
Every line of the spoken-lyrics spec has its start and its room moved by that map; the band (the instrumental her track is made over) is stretched
over 5 to 30 s with its pitch kept, and the rest of it moved later; make_mc.py then makes the track from the new spec (--tongue ja, as the
other languages are made). The picture is made from the picture the app ships (res/raw/run_film_prologue.mp4), no frame redrawn: the in-between
frames are the two frames either side of them, moved along their motion (OpenCV DIS optical flow) and blended.
"""
import json
import math
import pathlib
import sys

import numpy
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes")
RATE = 48000
A, B = 5.0, 23.0
ADD = 8.0  # seconds added: four bars of the music at 120 to the minute, so that the music can be extended by repeating them without changing its tempo


def mapped(t, f):
    return t if t <= A else A + (t - A) * f if t <= B else t + (B - A) * (f - 1)


def factor():
    """The slowing of the picture between 5 and 23 s: the seconds added, over the seconds there are."""
    return (B - A + ADD) / (B - A)


def beat_phase(band):
    """Where, in the first half second, the beats of the music fall (seconds), from the steps of its energy."""
    import scipy.signal

    x = numpy.abs(band[: RATE * 30])
    hop = RATE // 200
    env = numpy.sqrt((x[: len(x) // hop * hop].reshape(-1, hop) ** 2).mean(axis=1))
    env = numpy.maximum(0, numpy.diff(env))
    score = [env[k::100].sum() for k in range(100)]
    return int(numpy.argmax(score)) / 200


def loop_join(band):
    """
    Where to insert four bars (8 s) of the music that came before, inside 5 to 23 s: at a beat, and where the music just before the join is most
    like the music just before the same bars one time earlier, so that the repeat is a musical loop. Gives the second at which the copy begins.
    """
    import scipy.signal

    phase = beat_phase(band)

    def spectrum(a, b):
        seg = band[int(a * RATE):int(b * RATE)]
        f, _, z = scipy.signal.stft(seg, RATE, nperseg=4096)
        s = numpy.log(numpy.abs(z).mean(axis=1) + 1e-6)[(f > 60) & (f < 5000)]
        return (s - s.mean()) / (s.std() + 1e-9)

    best, at = -9, None
    k = 0
    while True:
        t = phase + k * .5
        k += 1
        if t < A + ADD:
            continue
        if t > B:
            break
        score = float((spectrum(t - 1, t) * spectrum(t - 1 - ADD, t - ADD)).mean())
        # (a bar line is better than a beat: the score is raised a little where the second is a whole bar from the music's start)
        if abs(((t - phase) / 2) - round((t - phase) / 2)) < 1e-6:
            score += .05
        if score > best:
            best, at = score, t
    return at, best, phase


def spec(f):
    import clips_fit as cf
    import soundfile

    base = json.loads((ROOT / "tools" / "cutscenes" / "prologue-mc.json").read_text(encoding="utf-8"))
    extra = (B - A) * (f - 1)
    seconds = int(math.ceil(base["seconds"] + extra))
    lines = []
    for line in base["lines"]:
        at, room = line[0], line[1]
        new_at = mapped(at, f)
        new_room = mapped(at + room, f) - new_at
        lines.append([round(new_at, 2), round(new_room, 2)] + line[2:])
    base["lines"] = lines
    base["seconds"] = seconds
    base["out"] = "opening/prologue-halgakos-slow"
    # the band: at its own speed and pitch, with four bars repeated inside 5 to 23 s (crossfaded 40 ms on the beat)
    band = cf.read_audio(D / base["band"])
    at, score, phase = loop_join(band)
    fade = int(.04 * RATE)
    first, copy, rest = band[: int(at * RATE)], band[int((at - ADD) * RATE):int(at * RATE)], band[int(at * RATE):]
    out = first
    for part in (copy, rest):
        ramp = numpy.linspace(0, 1, fade)
        out = numpy.concatenate([out[:-fade], out[-fade:] * (1 - ramp) + part[:fade] * ramp, part[fade:]])
    soundfile.write(str(D / "opening" / "band-slow.wav"), out[: seconds * RATE], RATE)
    print(f"music: the {ADD:g} s from {at - ADD:.2f} to {at:.2f} s are said again after {at:.2f} s (beats fall at {phase:.2f} s + n x 0.5; the likeness of the two joins {score:.2f})")
    (D / "opening" / "band-slow.txt").write_text(f"repeated: {at - ADD:.2f}-{at:.2f} s, inserted at {at:.2f} s, length {ADD} s\n", encoding="utf-8")
    base["band"] = "opening/band-slow.wav"
    (ROOT / "tools" / "cutscenes" / "prologue-slow-mc.json").write_text(json.dumps(base, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    print("seconds", seconds, "added", round(extra, 2), "s; band", round(len(out) / RATE, 2), "s")


def picture(f):
    import av
    import cv2
    from PIL import Image

    src = ROOT / "app" / "src" / "main" / "res" / "raw" / "run_film_prologue.mp4"
    box = av.open(str(src))
    video = box.streams.video[0]
    fps = float(video.average_rate)
    frames = [fr.to_ndarray(format="rgb24") for fr in box.decode(video)]
    box.close()
    n = len(frames)
    # How different each frame is from the one before it: a big jump is a cut of the film (never interpolated across), a middling one is fast motion
    # (blended, not warped), a small one is calm (warped along its motion).
    small = [cv2.resize(cv2.cvtColor(fr, cv2.COLOR_RGB2GRAY), (160, 90)).astype("float32") for fr in frames]
    jump = [0.0] + [float(numpy.abs(small[k] - small[k - 1]).mean()) for k in range(1, n)]
    cuts = [k for k in range(1, n) if jump[k] > 35]
    print("cuts of the film (seconds):", [round(k / fps, 2) for k in cuts if k / fps < 40], flush=True)
    extra = (B - A) * (f - 1)
    total = int(round((n / fps + extra) * fps))
    dis = cv2.DISOpticalFlow_create(cv2.DISOPTICAL_FLOW_PRESET_MEDIUM)
    out = av.open(str(D / "opening" / "prologue-ja-slow.mp4"), "w", options={"movflags": "+faststart"})
    stream = out.add_stream("libx264", rate=round(fps))
    stream.width, stream.height = frames[0].shape[1], frames[0].shape[0]
    stream.pix_fmt = "yuv420p"
    stream.bit_rate = 1_150_000
    stream.options = {"profile": "main", "preset": "slow", "g": str(int(fps * 2)), "keyint_min": str(int(fps)), "maxrate": str(int(1_150_000 * 1.6)), "bufsize": str(2_300_000)}
    flows = {}

    def warp(i, j, u):
        """The frame u of the way from frame i to frame j, by their motion."""
        a, b = frames[i], frames[j]
        ga, gb = cv2.cvtColor(a, cv2.COLOR_RGB2GRAY), cv2.cvtColor(b, cv2.COLOR_RGB2GRAY)
        if (i, j) not in flows:
            flows[(i, j)] = (dis.calc(ga, gb, None), dis.calc(gb, ga, None))
        fwd, back = flows[(i, j)]
        h, w = ga.shape
        gx, gy = numpy.meshgrid(numpy.arange(w), numpy.arange(h))
        wa = cv2.remap(a, (gx - fwd[..., 0] * u).astype("float32"), (gy - fwd[..., 1] * u).astype("float32"), cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)
        wb = cv2.remap(b, (gx - back[..., 0] * (1 - u)).astype("float32"), (gy - back[..., 1] * (1 - u)).astype("float32"), cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)
        return cv2.addWeighted(wa, 1 - u, wb, u, 0)

    kept = None
    for k in range(total):
        t = k / fps
        # which time of the shipped film is shown at this time of the new one
        if t <= A:
            s = t
        elif t <= B + extra:
            s = A + (t - A) / f
        else:
            s = t - extra
        s = min(s * fps, n - 1.001)
        i = int(s)
        u = s - i
        if t > A and t <= B + extra and u > .02:
            step = jump[i + 1]
            if step > 35:
                frame = frames[i]                      # a cut: the shot before it goes on until the cut, which is one frame, clean
            elif step > 12:
                frame = cv2.addWeighted(frames[i], 1 - u, frames[i + 1], u, 0)   # fast motion: a plain blend of two frames of the same shot
            else:
                frame = warp(i, i + 1, u)
        else:
            frame = frames[i]
        for packet in stream.encode(av.VideoFrame.from_ndarray(frame, format="rgb24")):
            out.mux(packet)
    for packet in stream.encode():
        out.mux(packet)
    out.close()
    print("picture", total, "frames,", round(total / fps, 2), "s")


def picture_master(f):
    """
    The slowed picture made from the untouched 1080p master (dist/cutscenes/opening/opening-seedance.mp4), in one pass, with two outputs from the same frames:
    the picture for the app, 1280 x 720 at 1.3 Mb/s (a little better than the 1.16 Mb/s picture the app ships), and a 1080p review copy with the Japanese
    captions and the track, at about 2.0 Mb/s. Nothing is encoded twice. Cuts are found first (a jump of over 35 on a small copy of the frame), and
    never interpolated across: the shot before a cut is held until it, fast motion is blended within its shot, calm motion is warped along its flow
    (computed at half size, applied at full size).
    """
    import av
    import cv2
    import soundfile
    import clips_fit as cf

    master = D / "opening" / "opening-seedance.mp4"
    box = av.open(str(master))
    video = box.streams.video[0]
    fps = float(video.average_rate)
    small = []
    for fr in box.decode(video):
        small.append(cv2.resize(cv2.cvtColor(fr.to_ndarray(format="rgb24"), cv2.COLOR_RGB2GRAY), (160, 90)).astype("float32"))
    box.close()
    n = len(small)
    jump = [0.0] + [float(numpy.abs(small[k] - small[k - 1]).mean()) for k in range(1, n)]
    print("cuts (s):", [round(k / fps, 2) for k in range(1, n) if jump[k] > 35 and k / fps < 40], flush=True)
    extra = (B - A) * (f - 1)
    total = int(round((n / fps + extra) * fps))

    # the track and the captions of the review copy
    spec = json.loads((ROOT / "tools" / "cutscenes" / "prologue-mc.json").read_text(encoding="utf-8"))
    times = json.loads((D / "opening" / "prologue-halgakos-slow-ja.times.json").read_text(encoding="utf-8"))
    rows = [(a, b, w.replace("\u2661", "")) for (a, b), w in zip(times, spec["tongues"]["ja"]["lines"])]
    audio = cf.read_audio(D / "opening" / "fx" / "run_film_prologue_h_ja_slow.m4a")[: int(total / fps * RATE)]

    app = av.open(str(D / "opening" / "prologue-ja-slow-app.mp4"), "w", options={"movflags": "+faststart"})
    sa = app.add_stream("libx264", rate=round(fps))
    sa.width, sa.height, sa.pix_fmt, sa.bit_rate = 1280, 720, "yuv420p", 1_300_000
    sa.options = {"profile": "main", "preset": "slower", "g": str(int(fps * 2)), "keyint_min": str(int(fps)), "maxrate": str(int(1_300_000 * 1.6)), "bufsize": "2600000"}
    rev = av.open(str(D / "films-review" / "prologue-ja-effects.mp4"), "w", options={"movflags": "+faststart"})
    sr = rev.add_stream("libx264", rate=round(fps))
    sr.width, sr.height, sr.pix_fmt = 1920, 1080, "yuv420p"
    sr.options = {"preset": "slower", "crf": "21", "maxrate": "2000000", "bufsize": "4000000"}
    ar = rev.add_stream("aac", rate=RATE, layout="mono")
    ar.bit_rate = 160000

    dis = cv2.DISOpticalFlow_create(cv2.DISOPTICAL_FLOW_PRESET_MEDIUM)
    decoder = av.open(str(master))
    gen = (fr.to_ndarray(format="rgb24") for fr in decoder.decode(decoder.streams.video[0]))
    held, last = {}, -1

    def get(i):
        nonlocal last
        while last < i:
            last += 1
            held[last] = next(gen)
            for old in [k for k in held if k < last - 2]:
                del held[old]
        return held[i]

    gx, gy = numpy.meshgrid(numpy.arange(1920, dtype="float32"), numpy.arange(1080, dtype="float32"))

    def warp(i, j, u):
        a, b = get(i), get(j)
        ga = cv2.cvtColor(cv2.resize(a, (960, 540), interpolation=cv2.INTER_AREA), cv2.COLOR_RGB2GRAY)
        gb = cv2.cvtColor(cv2.resize(b, (960, 540), interpolation=cv2.INTER_AREA), cv2.COLOR_RGB2GRAY)
        fwd = cv2.resize(dis.calc(ga, gb, None), (1920, 1080)) * 2
        back = cv2.resize(dis.calc(gb, ga, None), (1920, 1080)) * 2
        wa = cv2.remap(a, gx - fwd[..., 0] * u, gy - fwd[..., 1] * u, cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)
        wb = cv2.remap(b, gx - back[..., 0] * (1 - u), gy - back[..., 1] * (1 - u), cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)
        return cv2.addWeighted(wa, 1 - u, wb, u, 0)

    for k in range(total):
        t = k / fps
        s = t if t <= A else A + (t - A) / f if t <= B + extra else t - extra
        s = min(s * fps, n - 1.001)
        i = int(s)
        u = s - i
        if t > A and t <= B + extra and u > .02:
            step = jump[i + 1]
            if step > 35:
                frame = get(i)
            elif step > 12:
                frame = cv2.addWeighted(get(i), 1 - u, get(i + 1), u, 0)
            else:
                frame = warp(i, i + 1, u)
        else:
            frame = get(i)
        for packet in sa.encode(av.VideoFrame.from_ndarray(cv2.resize(frame, (1280, 720), interpolation=cv2.INTER_AREA), format="rgb24")):
            app.mux(packet)
        shown = Image.fromarray(frame)
        for a0, b0, words in rows:
            if a0 <= t <= b0 + .3:
                shown = cf.captioned(shown, words, None, None, "ja")
                break
        for packet in sr.encode(av.VideoFrame.from_image(shown)):
            rev.mux(packet)
    for packet in sa.encode():
        app.mux(packet)
    for packet in sr.encode():
        rev.mux(packet)
    app.close()
    pcm = numpy.clip(audio, -1, 1).astype("float32")
    for i in range(0, len(pcm), 1024):
        fr = av.AudioFrame.from_ndarray(pcm[i:i + 1024].reshape(1, -1), format="flt", layout="mono")
        fr.sample_rate = RATE
        fr.pts = i
        for packet in ar.encode(fr):
            rev.mux(packet)
    for packet in ar.encode():
        rev.mux(packet)
    rev.close()
    print("picture for the app, review copy:", total, "frames,", round(total / fps, 2), "s", flush=True)


if __name__ == "__main__":
    f = float(sys.argv[2])
    {"spec": spec, "picture": picture, "master": picture_master}[sys.argv[1]](f)
