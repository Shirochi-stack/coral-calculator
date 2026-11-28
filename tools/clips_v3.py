"""
The clips, edited properly (v3): voices timed to the lip movements, the generated sound effects kept, effects laid on the action, a bed under it all, one loudness.

    python tools/clips_v3.py ID [ID ...] [--langs en,ja,ko,zh]       builds dist/cutscenes/clips/review/<id>-<model>-v3.mp4 (English) and -v3-ja/-ko/-zh
    python tools/clips_v3.py table                                    writes dist/cutscenes/clips/review/v3-table.md

The order is fixed, so that nothing of ours is ever put through a voice-removal step: the generated video's own sound is separated with Demucs first (tools/clips_bed.py:
its effects bed, and the voice stem, which is where the mouths of the picture move); our lines (Inworld, from voice/) are cut and fitted afterwards and laid on top of the bed
untouched. Each line begins on the first mouth the generated speech has near where it was asked for it (Seedance, which was given our recording as the lip reference, keeps
the recording's own places), is stretched by at most 1.35 (or squeezed to 0.85), and never lies over another.
Effects: the action is found in the picture (motion and flashes between frames, cuts), and the game's own effects (res/raw/fight_s_*.ogg) are laid on its peaks, the loudest
peaks with a heavy one and the softest with a whoosh, by the palette written for each clip below; any 1.5 s of action without one is given a whoosh. Under it all: the generated
effects (the bed), a quiet room tone, and the game's own instrumental very low, ducked under the voice. Loudness: -18 LUFS integrated (the level of clips 1 and 34, which
were approved), true peak under -1 dB. The picture is encoded once from the generated master (CRF 20, H.264 high, 1080p), with the captions of the language burned in.
"""
import json
import pathlib
import sys, os

import av
import PIL.Image
import numpy
import pyloudnorm
import scipy.signal
import soundfile
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import clips_data as data  # noqa: E402
import clips_fit as cf  # noqa: E402
import clips_v3_lines as nl  # noqa: E402
import make_clips as mk  # noqa: E402

RATE = 48000
CLIPS = mk.CLIPS
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
BAND = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\prologue\songs\eleven-band-1.mp3")
LANGS = ("en", "ja", "ko", "zh")
TARGET_LUFS = -18.0
# per clip, the game's effects for the heaviest, the middle and the lightest peaks of the action
PALETTE = {
    "01": ("smash", "jump", "whoosh"), "02": ("bread", "throw", "whoosh"), "03": ("heavy", "swing", "whoosh"), "04": ("ink", "magic", "whoosh"),
    "05": ("heart", "magic", "whoosh"), "06": ("smash", "slice", "whoosh"), "07": ("bat", "swing", "whoosh"), "08": ("clash", "bat", "whoosh"),
    "09": ("ink", "throw", "drop"), "10": ("slice", "clash", "whoosh"), "11": ("smash", "ink", "bread"), "12": ("heavy", "jump", "whoosh"),
    "13": ("clash", "block", "whoosh"), "14": ("ultimate", "magic", "whoosh"), "15": ("homer", "bat", "whoosh"), "16": ("slice", "whoosh", "magic"),
    "17": ("ultimate", "magic", "scan"), "18": ("ultimate", "magic", "whoosh"), "19": ("smash", "ko", "magic"), "20": ("heavy", "whoosh", "scan"),
    "21": ("clash", "block", "whoosh"), "22": ("ultimate", "magic", "heart"), "23": ("ultimate", "whoosh", "magic"), "24": ("ink", "ultimate", "whoosh"),
    "25": ("ultimate", "smash", "magic"), "26": ("drop", "smash", "whoosh"), "27": ("slice", "slice", "whoosh"), "28": ("bread", "throw", "heart"),
    "29": ("ultimate", "magic", "whoosh"), "30": ("ultimate", "slice", "magic"), "31": ("ink", "ultimate", "drop"), "32": ("magic", "tick", "pick"),
    "33": ("ultimate", "heart", "bread"), "34": ("win", "magic", "bell"),
}
GAIN = {"smash": -5, "heavy": -5, "ultimate": -6, "clash": -6, "homer": -5, "ko": -6, "bat": -6, "slice": -7, "ink": -7, "magic": -9, "heart": -9, "bread": -8, "throw": -9,
        "swing": -9, "block": -8, "jump": -10, "drop": -8, "whoosh": -11, "scan": -12, "tick": -12, "pick": -12, "win": -8, "bell": -10}


def library():
    out = {}
    for path in RAW.glob("fight_s_*.ogg"):
        x, r = soundfile.read(str(path))
        x = x if x.ndim == 1 else x.mean(axis=1)
        x = scipy.signal.resample_poly(x, RATE, r).astype("float32")
        loud = numpy.where(numpy.abs(x) > .02 * numpy.abs(x).max())[0]
        x = x[max(0, loud[0] - 200):loud[-1] + 2400] if len(loud) else x
        out[path.stem[len("fight_s_"):]] = x / (numpy.abs(x).max() + 1e-9)
    return out


def master_of(clip):
    """The generated picture the clip is made from, and the seconds of it that are the clip."""
    if clip.get("gen"):
        return CLIPS / "gens" / f"{clip['gen']}.mp4", clip["cut"][0], clip["cut"][1]
    return mk.source_of(clip), 0.0, float(clip["secs"])


def analyse(master, a, b):
    """Frame times, and how much happens at each: motion between frames, flashes of light, and which frames are cuts."""
    import cv2

    box = av.open(str(master))
    video = box.streams.video[0]
    fps = float(video.average_rate)
    previous, motion, light, times = None, [], [], []
    for frame in box.decode(video):
        t = float(frame.pts * frame.time_base)
        if t < a - 1e-6:
            continue
        if t >= b - 1e-6:
            break
        g = cv2.resize(cv2.cvtColor(frame.to_ndarray(format="rgb24"), cv2.COLOR_RGB2GRAY), (160, 90)).astype("float32")
        motion.append(0.0 if previous is None else float(numpy.abs(g - previous).mean()))
        light.append(float(g.mean()))
        times.append(t - a)
        previous = g
    box.close()
    motion, light = numpy.array(motion), numpy.array(light)
    flash = numpy.abs(numpy.diff(light, prepend=light[0]))
    return fps, numpy.array(times), motion, flash


def effects_of(number, secs, fps, times, motion, flash):
    """(second, effect, dB) for the peaks of the action, and a whoosh for any 1.5 s of action that has none."""
    cuts = motion > 35
    score = numpy.where(cuts, 0, motion) + 2 * flash
    score = numpy.convolve(score, numpy.ones(3) / 3, mode="same")
    top = numpy.percentile(score, 98) + 1e-6
    peaks, props = scipy.signal.find_peaks(score, prominence=.25 * top, distance=max(1, int(.5 * fps)))
    heavy, middle, light = PALETTE[number]
    found = []
    for p, pr in zip(peaks, props["prominences"]):
        strength = min(1.0, pr / top)
        name = heavy if strength > .66 else middle if strength > .33 else light
        found.append((float(times[p]), name, GAIN[name] + (0 if strength > .5 else -2)))
    for k in numpy.where(cuts)[0]:
        found.append((float(times[k]), "whoosh", GAIN["whoosh"]))
    found.sort()
    # a stretch of action of 1.5 s or more with no effect gets a whoosh where the action rises most
    busy = numpy.convolve(motion, numpy.ones(int(fps)) / fps, mode="same") > numpy.median(motion) * 1.0
    step = int(.75 * fps)
    for i in range(0, len(times) - int(1.5 * fps), step):
        lo, hi = times[i], times[min(len(times) - 1, i + int(1.5 * fps))]
        if busy[i:i + int(1.5 * fps)].mean() > .6 and not any(lo - .1 <= t <= hi for t, _, _ in found):
            j = i + int(numpy.argmax(numpy.diff(motion[i:i + int(1.5 * fps)], prepend=0)))
            found.append((float(times[j]), "whoosh", GAIN["whoosh"] - 1))
    found.sort()
    out = []
    for t, name, g in found:
        if not out or t - out[-1][0] > .25:
            out.append((t, name, g))
    return out


def lines_of(clip, lang):
    folder = CLIPS / clip["id"] / "voice"
    out = []
    for i, line in enumerate(clip["lines"]):
        f = folder / f"v3-{lang}-{i}.wav"
        x, r = soundfile.read(str(f if f.exists() else folder / f"{lang}-{i}.wav"))
        out.append(scipy.signal.resample_poly(x.mean(axis=1) if x.ndim > 1 else x, RATE, r).astype("float32"))
    return out


def windows(clip, vocals, en_lines):
    """Where each line goes: the first mouth of the generated speech near where it was asked for it; Seedance's recording places for its own."""
    nominal = [(l[1], l[1] + len(en_lines[i]) / RATE) for i, l in enumerate(clip["lines"])]
    if clip["model"] == "seedance":
        return nominal, "the recording's own places (the lips followed our recording)"
    spans = cf.speech_spans(vocals, floor=.22)
    spans = [(a, b) for a, b in spans if b - a >= .25]
    if not spans:
        return nominal, "no speech found in the generated sound: lines left at the asked places"
    return cf.nearest_spans(spans, nominal), f"{len(spans)} span(s) of the generated speech: " + ", ".join(f"{a:.1f}-{b:.1f}" for a, b in spans)


_whisper = None


def our_words(path):
    """The words of one of our recordings with their times (cached next to it)."""
    cache = pathlib.Path(str(path).replace(".wav", ".words.json"))
    if cache.exists():
        return json.loads(cache.read_text(encoding="utf-8"))
    global _whisper
    if _whisper is None:
        from faster_whisper import WhisperModel
        _whisper = WhisperModel("small", device="cpu", compute_type="int8")
    segs, _ = _whisper.transcribe(str(path), word_timestamps=True, language="en", vad_filter=False, beam_size=3, condition_on_previous_text=False)
    words = [{"w": w.word.strip(), "a": round(w.start, 3), "b": round(w.end, 3)} for s in segs for w in (s.words or [])]
    cache.write_text(json.dumps(words), encoding="utf-8")
    return words


def norm(word):
    return "".join(c for c in word.lower() if c.isalnum())


def similar(a, b):
    import difflib
    a, b = norm(a), norm(b)
    if not a or not b:
        return 0.0
    return 1.0 if a == b else difflib.SequenceMatcher(None, a, b).ratio()


def align_words(ours, theirs):
    """Our words (a list of (line, word, begin, end)) to the generated speech's words, in order, as a global alignment: pairs of (our index, their index)."""
    n, m = len(ours), len(theirs)
    score = numpy.zeros((n + 1, m + 1))
    gap = -.35
    for i in range(1, n + 1):
        score[i][0] = i * gap
    for j in range(1, m + 1):
        score[0][j] = j * gap
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            s = similar(ours[i - 1][1], theirs[j - 1]["w"])
            match = score[i - 1][j - 1] + (2 * s - .9 if s >= .6 else -.8)
            score[i][j] = max(match, score[i - 1][j] + gap, score[i][j - 1] + gap)
    pairs, i, j = [], n, m
    while i > 0 and j > 0:
        s = similar(ours[i - 1][1], theirs[j - 1]["w"])
        if abs(score[i][j] - (score[i - 1][j - 1] + (2 * s - .9 if s >= .6 else -.8))) < 1e-9 and s >= .6:
            pairs.append((i - 1, j - 1))
            i, j = i - 1, j - 1
        elif abs(score[i][j] - (score[i - 1][j] + gap)) < 1e-9:
            i -= 1
        else:
            j -= 1
    return pairs[::-1]


def lay_english(clip, secs):
    """
    Each English line, word by word on the mouth: our recording's words are matched to the words the generated speech says (its Demucs voice stem, transcribed with times), and
    the line is stretched between matched words so that each begins and ends where the generated mouth's word does. Gives, for each line, (audio, where it begins, the anchors).
    """
    folder = CLIPS / clip["id"]
    speech = json.loads((folder / f"{clip['id']}-speech.json").read_text(encoding="utf-8"))["words"]
    pieces, ours = [], []
    for i in range(len(clip["lines"])):
        path = folder / "voice" / f"en-{i}.wav"
        x, r = soundfile.read(str(path))
        x = scipy.signal.resample_poly(x.mean(axis=1) if x.ndim > 1 else x, RATE, r).astype("float32")
        pieces.append(x)
        for w in our_words(path):
            ours.append((i, w["w"], w["a"], w["b"]))
    pairs = align_words(ours, speech)
    by_line = {}
    for oi, mi in pairs:
        line, _, a, b = ours[oi]
        by_line.setdefault(line, []).append((a, b, speech[mi]["a"], speech[mi]["b"]))
    out = {}
    for i, x in enumerate(pieces):
        anchors = by_line.get(i)
        if not anchors:
            out[i] = None
            continue
        # a piece from segment to segment, each stretched to the generated word's own length
        cuts = [(0.0, None)]
        parts, at = [], None
        first_a, first_ma = anchors[0][0], anchors[0][2]
        lead = x[: int(max(0, first_a - .03) * RATE)]
        built = []
        times = []  # (our time, clip time) knots
        for a, b, ma, mb in anchors:
            times.append((a, ma))
            times.append((b, mb))
        # make the knots increasing in both
        knots = [times[0]]
        for k in times[1:]:
            if k[0] > knots[-1][0] + .01 and k[1] > knots[-1][1] + .01:
                knots.append(k)
        segs = []
        ratios = [((o1 - o0) / (m1 - m0)) for (o0, m0), (o1, m1) in zip(knots[:-1], knots[1:])]
        if all(.75 <= q <= 1.35 for q in ratios):
            # each matched word keeps the generated mouth's own length
            for (o0, m0), (o1, m1), q in zip(knots[:-1], knots[1:], ratios):
                seg = x[int(o0 * RATE):int(o1 * RATE)]
                segs.append(cf.stretch(seg, q, RATE) if abs(q - 1) > .03 else seg)
        else:
            # the generated words are drawn out or hurried: the whole span of the line, from its first matched word to its last, is stretched at once, once
            o0, m0, o1, m1 = knots[0][0], knots[0][1], knots[-1][0], knots[-1][1]
            q = min(1.6, max(.65, (o1 - o0) / (m1 - m0)))
            seg = x[int(o0 * RATE):int(o1 * RATE)]
            segs.append(cf.stretch(seg, q, RATE) if abs(q - 1) > .03 else seg)
            knots = [knots[0], knots[-1]]
        body = numpy.concatenate(segs) if segs else x
        tail = x[int(knots[-1][0] * RATE):]
        lead_len = len(lead) / RATE
        audio = numpy.concatenate([lead, body, tail])
        begins = knots[0][1] - lead_len
        out[i] = (audio, max(0.0, begins), [(round(k[0], 2), round(k[1], 2)) for k in knots])
    return out, speech


def build(clip, lang, table=None):
    """A first pass finds the lines that still cannot fit the mouth (x1.3 or more); the picture there is slowed (see slow_segments) and the build is done again."""
    need = _build(clip, lang, None, [], False)
    segs = slow_segments(clip, need) if need else []
    _build(clip, lang, table, segs, True)


def slow_segments(clip, need):
    """(first frame, last frame, extra frames): the mouth span of each line that is too long, plus a little either side, cut-free, slowed by at most x1.5."""
    master, a, b = master_of(clip)
    fps, times, motion, flash = analyse(master, a, b)
    cuts = [k for k in range(1, len(motion)) if motion[k] > 35]
    segs = []
    for wa, wb, delta in need:
        ka, kb = int(max(0, wa - .2) * fps), min(len(motion) - 1, int((wb + .2) * fps) + 1)
        edges = [ka] + [k for k in cuts if ka < k < kb] + [kb]
        k0, k1 = max(zip(edges[:-1], edges[1:]), key=lambda r: r[1] - r[0])
        if k1 - k0 < 6:
            continue
        if segs and k0 <= segs[-1][1]:
            k0 = segs[-1][1] + 1
        extra = min(int(round(delta * fps)), (k1 - k0) // 2)
        if extra > 0 and k1 - k0 >= 6:
            segs.append((k0, k1, extra))
    return segs


def _build(clip, lang, table, segs, encode_it):
    number = clip["id"][:2]
    secs = float(clip["secs"])
    folder = CLIPS / clip["id"]
    master, a, b = master_of(clip)
    fps, times, motion, flash = analyse(master, a, b)
    extra_s = sum(x for *_, x in segs) / fps
    def mp(t):
        return t + sum(x / fps * min(1.0, max(0.0, (t * fps - k0) / (k1 - k0))) for k0, k1, x in segs)
    need = []
    bed = cf.read_audio(folder / f"{clip['id']}-bed.wav")
    if segs:
        out, pos = [], 0
        fade = int(.012 * RATE)
        for k0, k1, x in segs:
            e = int(k1 / fps * RATE)
            d = int(x / fps * RATE)
            out.append(bed[pos:e])
            piece = bed[max(0, e - d):e].copy()
            if len(piece) > 2 * fade:
                piece[:fade] *= numpy.linspace(0, 1, fade)
                piece[-fade:] *= numpy.linspace(1, 0, fade)
            out.append(piece)
            pos = e
        out.append(bed[pos:])
        bed = numpy.concatenate(out)
    vocals_path = folder / f"{clip['id']}-vocals.wav"
    vocals = cf.read_audio(vocals_path) if vocals_path.exists() else numpy.zeros(0, dtype="float32")
    if len(vocals) and clip.get("gen"):
        pass
    en_lines = lines_of(clip, "en")
    folder = CLIPS / clip["id"]
    total = int((secs + extra_s) * RATE)
    secs_new = secs + extra_s
    voice = numpy.zeros(total, dtype="float32")
    laid, lane, notes_how = [], [], []
    spoken = (folder / f"{clip['id']}-speech.json").exists()
    aligned, speech = (lay_english(clip, secs) if spoken else ({}, []))
    if clip["model"] == "seedance" and not spoken:
        wins, how = windows(clip, vocals, en_lines)
    else:
        wins, how = windows(clip, vocals, en_lines)
    mine = lines_of(clip, lang)
    for i, piece in enumerate(mine):
        got = None if (number == '13' and i == 1) or number in ('26', '33') else aligned.get(i)
        if got is not None and lang == "en":
            shown, start, knots = got
            mode = "word by word on the generated speech: " + ", ".join(f"{o:.2f}->{m:.2f}" for o, m in knots[:4]) + ("..." if len(knots) > 4 else "")
        else:
            wa, wb = wins[i]
            if number == '26':
                # only Rune's shocked gasp, at the strongest motion of the reveal
                calm = numpy.where((motion < 35) & (times > 3) & (times < secs - 1.2), motion, 0)
                wa = float(times[int(calm.argmax())]) - .1
                wb = wa + 1.0
            if got is not None:
                # the other languages: from the first to the last word the generated mouth says for this line
                anchors = got[2]
                wa, wb = anchors[0][1] - (0.0), anchors[-1][1]
                wa = max(0.0, got[1] if lang != "en" else wa)
                ends = [m for _, m in anchors]
                wb = max(ends) + .2
            natural = len(piece) / RATE
            if not segs and lang != "en" and got is not None and natural / max(wb - wa, .3) > 1.3:
                need.append((wa, wb, natural / 1.15 - (wb - wa)))
            if segs:
                wa, wb = mp(wa), mp(wb)
            share = min(max(wb - wa, natural / 1.5), natural / .8)
            shown = cf.stretch(piece, natural / share, RATE) if abs(natural / share - 1) > .02 else piece
            start = wa
            mode = f"fitted to the mouth {wa:.2f}-{wb:.2f} (x{natural / share:.2f})" if got is not None else f"fitted to speech/asked window {wa:.2f}-{wb:.2f} (x{natural / share:.2f})"
        for s0, e0 in lane:
            if start < e0 + .06:
                start = e0 + .06
        n = len(shown) / RATE
        if start + n > secs_new - .05:
            start = max(0.0, secs_new - .05 - n)
        shown = cf.edged(shown)
        s = int(start * RATE)
        voice[s:s + len(shown)] += shown[: total - s]
        lane.append((start, start + n))
        laid.append((start, start + n, i, mode))
    # the voice at one level: -16 dBFS RMS where it speaks, as in the clips that were approved
    on = numpy.abs(voice) > .01
    if on.any():
        voice *= 10 ** (-16 / 20) / float(numpy.sqrt((voice[on] ** 2).mean()))
    env = numpy.convolve(on.astype(float), numpy.ones(RATE // 6) / (RATE // 6), mode="same")
    env = numpy.clip(numpy.convolve(numpy.clip(env * 1.5, 0, 1), numpy.ones(RATE // 8) / (RATE // 8), mode="same"), 0, 1)
    duck = 1 - .45 * env
    bed = numpy.pad(bed, (0, max(0, total - len(bed))))[:total] * duck
    # room tone and the instrumental, very low, so that the clip never opens or runs on in silence
    rng = numpy.random.default_rng(int(number))
    tone = scipy.signal.lfilter(*scipy.signal.butter(2, 900, "low", fs=RATE), rng.standard_normal(total)).astype("float32")
    tone *= 10 ** (-48 / 20) / float(numpy.sqrt((tone ** 2).mean()))
    band = cf.read_audio(BAND)
    off = int(((int(number) * 7) % 40) * 2.0 * RATE)
    music = numpy.resize(band[off:], total).astype("float32")
    music *= 10 ** (-33 / 20) / float(numpy.sqrt((music ** 2).mean()))
    music *= numpy.minimum(1, numpy.arange(total) / (.4 * RATE)) * duck
    # effects on the action
    lib = library()
    plan = effects_of(number, secs, fps, times, motion, flash)
    fx = numpy.zeros(total, dtype="float32")
    for t, name, gdb in plan:
        s = int(mp(t) * RATE)
        x = lib[name] * 10 ** (gdb / 20)
        fx[s:s + len(x)] += x[: max(0, total - s)]
    mix = voice + bed + tone + music + fx
    # a clip must open with sound: a quiet first half second (more than 12 dB under the whole) gets a whoosh at once
    if numpy.sqrt((mix[: RATE // 2] ** 2).mean()) < 10 ** (-12 / 20) * numpy.sqrt((mix ** 2).mean()):
        x = lib["whoosh"] * 10 ** (-3 / 20)
        mix[int(.05 * RATE):int(.05 * RATE) + len(x)] += x[: len(mix) - int(.05 * RATE)]
        plan = [(.05, "whoosh", -3)] + list(plan)
    # in and out: short fades; loudness: -18 LUFS; true peak under -1 dB
    f = int(.03 * RATE)
    mix[:f] *= numpy.linspace(0, 1, f)
    mix[-f:] *= numpy.linspace(1, 0, f)
    meter = pyloudnorm.Meter(RATE)
    for _ in range(4):
        loud = meter.integrated_loudness(mix.astype("float64"))
        mix = (mix * 10 ** ((TARGET_LUFS - loud) / 20)).astype("float32")
        up = scipy.signal.resample_poly(mix, 4, 1)
        peak = 20 * numpy.log10(numpy.abs(up).max() + 1e-9)
        if peak <= -1.3:
            break
        # a limiter: the peaks above -1.3 dB are taken down, smoothly
        ceiling = 10 ** (-1.7 / 20)
        hold = scipy.ndimage_filter = None
        import scipy.ndimage as nd
        level = nd.maximum_filter1d(numpy.abs(mix), size=int(.006 * RATE))
        gain = numpy.minimum(1.0, ceiling / (level + 1e-9))
        gain = nd.uniform_filter1d(gain, size=int(.004 * RATE))
        mix = (mix * gain).astype("float32")
    loud = meter.integrated_loudness(mix.astype("float64"))
    up = scipy.signal.resample_poly(mix, 4, 1)
    peak = 20 * numpy.log10(numpy.abs(up).max() + 1e-9)
    # captions in the language, then the picture, once
    rows = [(s, e, (nl.NEW.get((number, i), {}).get(lang) or clip["lines"][i][2 + LANGS.index(lang)]).replace("\u2661", "")) for s, e, i, _ in laid]
    target = CLIPS / "review" / f"{clip['id']}-{lang}-v3.mp4"
    if encode_it and not os.environ.get("DRY"):
        encode(master, a, b, mix, rows, lang, target, mk.HOLDS.get(number, ()), segs)
    if need or not encode_it:
        return need
    if table is not None:
        table[number + "-" + lang] = dict(id=clip["id"], model=clip["model"], how=how, laid=[(s, e, i, m) for s, e, i, m in laid], plan=plan, lufs=loud, peak=peak,
                             opening=float(20 * numpy.log10(numpy.sqrt((mix[:RATE] ** 2).mean()) + 1e-9)), size=(target.stat().st_size if target.exists() else 0), master=master, span=(a, b), lengthened=[(round(k0 / fps, 2), round(k1 / fps, 2), round(x / fps, 2)) for k0, k1, x in segs])
    print(f"{clip['id']} {lang} (+{extra_s:.2f} s): {(target.stat().st_size if target.exists() else 0) / 1e6:.1f} MB, {loud:.1f} LUFS, true peak {peak:.1f} dB, {len(plan)} effects", flush=True)


def between(fa, fb, u):
    """The picture a fraction u of the way from frame fa to frame fb (PIL images): both moved along the motion between them (OpenCV DIS flow) and blended."""
    import cv2
    A, B = numpy.asarray(fa), numpy.asarray(fb)
    ga = cv2.cvtColor(cv2.resize(A, (960, 540)), cv2.COLOR_RGB2GRAY)
    gb = cv2.cvtColor(cv2.resize(B, (960, 540)), cv2.COLOR_RGB2GRAY)
    flow = cv2.DISOpticalFlow_create(cv2.DISOPTICAL_FLOW_PRESET_MEDIUM).calc(ga, gb, None)
    flow = cv2.resize(flow, (A.shape[1], A.shape[0])) * 2.0
    gx, gy = numpy.meshgrid(numpy.arange(A.shape[1], dtype="float32"), numpy.arange(A.shape[0], dtype="float32"))
    wa = cv2.remap(A, gx - u * flow[..., 0], gy - u * flow[..., 1], cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)
    wb = cv2.remap(B, gx + (1 - u) * flow[..., 0], gy + (1 - u) * flow[..., 1], cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)
    return PIL.Image.fromarray(((1 - u) * wa.astype("float32") + u * wb.astype("float32")).round().astype("uint8"))


def encode(master, a, b, track, rows, lang, target, holds, segs=()):
    box = av.open(str(master))
    video = box.streams.video[0]
    fps = float(video.average_rate)
    out = av.open(str(target), "w", options={"movflags": "+faststart"})
    sv = out.add_stream("libx264", rate=video.average_rate)
    sv.width, sv.height, sv.pix_fmt = video.width, video.height, "yuv420p"
    sv.options = {"preset": "medium", "crf": "20", "profile": "high", "tune": "animation"}
    sa = out.add_stream("aac", rate=RATE, layout="stereo")
    sa.bit_rate = 192000
    kept = None
    count = [0]

    def emit(image, t):
        nonlocal kept
        if any(x <= t <= y for x, y in holds) and kept is not None:
            image = kept
        else:
            kept = image
        for s0, e0, words in rows:
            if s0 <= t <= e0 + .3:
                image = cf.captioned(image, words, None, None, lang)
                break
        for packet in sv.encode(av.VideoFrame.from_image(image)):
            out.mux(packet)
        count[0] += 1

    segs = list(segs)
    buffer = []
    k = -1
    for frame in box.decode(video):
        t = float(frame.pts * frame.time_base)
        if t < a - 1e-6:
            continue
        if t >= b - 1e-6:
            break
        k += 1
        image = frame.to_image()
        if segs and segs[0][0] <= k <= segs[0][1]:
            buffer.append(image)
            if k < segs[0][1]:
                continue
            k0, k1, extra = segs.pop(0)
            L = k1 - k0
            for j in range(L + extra):
                p = j * L / (L + extra)
                i = int(p)
                emit(buffer[i] if p - i < 1e-6 else between(buffer[i], buffer[i + 1], p - i), count[0] / fps)
            emit(image, count[0] / fps)
            buffer = []
            continue
        emit(image, count[0] / fps)
    for packet in sv.encode():
        out.mux(packet)
    box.close()
    pcm = numpy.stack([track, track], axis=1).astype("float32")
    for i in range(0, len(pcm), 1024):
        fr = av.AudioFrame.from_ndarray(numpy.ascontiguousarray(pcm[i:i + 1024].T), format="fltp", layout="stereo")
        fr.sample_rate = RATE
        fr.pts = i
        for packet in sa.encode(fr):
            out.mux(packet)
    for packet in sa.encode():
        out.mux(packet)
    out.close()


def main():
    args = [x for x in sys.argv[1:] if not x.startswith("--")]
    langs = LANGS
    if "--langs" in sys.argv:
        langs = sys.argv[sys.argv.index("--langs") + 1].split(",")
        args = [x for x in args if x != sys.argv[sys.argv.index("--langs") + 1]]
    table = {}
    for cid in args:
        clip = mk.find(cid)
        for lang in langs:
            build(clip, lang, table)
    if table:
        (CLIPS / "review" / f"v3-table-{'-'.join(args[:1])}.json").write_text(json.dumps({k: {**v, "master": str(v["master"])} for k, v in table.items()}, indent=1, default=float), encoding="utf-8")


if __name__ == "__main__":
    main()


