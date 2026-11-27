"""
The sound effects of the original prologue (Seedance's own sound), taken out of it and laid under Halgakos's tracks, which stay as they are.

    python tools/prologue_fx.py effects              dist/cutscenes/opening/prologue-fx.wav: the effects alone, 90 s
    python tools/prologue_fx.py tracks               the four tracks with the effects added: dist/cutscenes/opening/fx/prologue_h[_xx].wav, and the shipped m4a
    python tools/prologue_fx.py slow                 the same for the slowed Japanese prologue (tools/prologue_ja_slow.py)

What the original sound is made of (Demucs htdemucs, four stems, tools/clips_bed.py with STEMS=1): the singing is by far the loudest
(-25 dB), the model's own music is thin (bass, other about -40 dB), and the effects are in drums and other, as hits. So that the music is
not doubled, only the hits are kept: the percussive part of drums + other (a median-filter HPSS), opened only where it jumps up from the
level around it. The old singing is the vocals stem, which is not used at all.
"""
import json
import pathlib
import sys

import numpy
import scipy.ndimage
import scipy.signal
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import clips_fit as cf  # noqa: E402

D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\opening")
RATE = 48000
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
LANGS = ("en", "ja", "ko", "zh")
GAIN_DB = 17.0  # the hits are about -40 dB in the stem; this brings their loudest to about -22 dB under the mix


def effects():
    x = cf.read_audio(D / "opening-seedance-drums.wav") + cf.read_audio(D / "opening-seedance-other.wav")
    f, t, z = scipy.signal.stft(x, RATE, nperseg=2048, noverlap=1536)
    mag = numpy.abs(z)
    harm = scipy.ndimage.median_filter(mag, size=(1, 17))
    perc = scipy.ndimage.median_filter(mag, size=(17, 1))
    mask = perc ** 2 / (harm ** 2 + perc ** 2 + 1e-12)
    _, y = scipy.signal.istft(z * mask, RATE, nperseg=2048, noverlap=1536)
    y = y[: len(x)].astype("float32")
    y = scipy.signal.sosfilt(scipy.signal.butter(2, 150, "highpass", fs=RATE, output="sos"), y)
    # a gate that opens on a jump of the level over the level around it, and stays open a third of a second
    hop = RATE // 100
    n = len(y) // hop
    env = numpy.sqrt((y[: n * hop].reshape(n, hop) ** 2).mean(axis=1)) + 1e-9
    around = scipy.ndimage.median_filter(env, size=151)
    opens = env > around * 2.2
    # The model's music has a beat of its own, 120 to the minute: its hits are not effects. The gate is shut on the beat unless something much louder than the beats comes on it.
    frames = numpy.arange(n)
    score = [env[(frames % 50) == ph].sum() for ph in range(50)]
    phase = int(numpy.argmax(score))
    near = numpy.zeros(n, bool)
    for k in range(-1, n // 50 + 2):
        c = phase + 50 * k
        near[max(0, c - 8):max(0, min(n, c + 9))] = True
    beat_level = numpy.median(env[(frames % 50) == phase])
    opens = opens & (~near | (env > beat_level * 4))
    held = scipy.ndimage.maximum_filter1d(opens.astype(float), size=35)
    held = scipy.ndimage.uniform_filter1d(held, size=9)
    gate = numpy.interp(numpy.arange(len(y)), numpy.arange(n) * hop + hop / 2, held)
    y = y * gate
    # the hits are brought up until their loudest are at about -13 dBFS (a quarter of full scale), where a hit is heard through the music
    # (brought up until the effects, over the whole film, are at -28 dBFS RMS, about 11 dB under the track, and no hit is above -4 dBFS)
    y *= 10 ** (-28 / 20) / float(numpy.sqrt((y ** 2).mean()) + 1e-9)
    peak = numpy.abs(y).max()
    if peak > .6:
        y *= .6 / peak
    return y.astype("float32")


def levels(name, mix, fx, voice=None):
    db = lambda a: 20 * numpy.log10(float(numpy.sqrt((a ** 2).mean())) + 1e-9)
    rows = [f"{name}: level of each 10 s (dBFS RMS): whole track / the effects added"]
    total = len(mix) // RATE
    for a in range(0, total, 10):
        sl = slice(a * RATE, min(len(mix), (a + 10) * RATE))
        rows.append(f"{a:3d}-{a + 10:<3d} s  track {db(mix[sl]):6.1f}   effects {db(fx[sl]):6.1f}")
    return rows


def encode(stereo, target, bitrate=128000):
    import av

    out = av.open(str(target), "w")
    stream = out.add_stream("aac", rate=RATE, layout="stereo")
    stream.bit_rate = bitrate
    pcm = numpy.clip(stereo, -1, 1).astype("float32")
    for i in range(0, len(pcm), 1024):
        frame = av.AudioFrame.from_ndarray(numpy.ascontiguousarray(pcm[i:i + 1024].T), format="fltp", layout="stereo")
        frame.sample_rate = RATE
        frame.pts = i
        for packet in stream.encode(frame):
            out.mux(packet)
    for packet in stream.encode():
        out.mux(packet)
    out.close()


def stereo_of(path):
    import av

    box = av.open(str(path))
    resampler = av.AudioResampler(format="flt", layout="stereo", rate=RATE)
    parts = []
    for frame in box.decode(audio=0):
        for out in resampler.resample(frame):
            parts.append(out.to_ndarray().reshape(-1, 2))
    box.close()
    return numpy.concatenate(parts)


def tracks():
    fx_path = D / "prologue-fx.wav"
    if not fx_path.exists():
        soundfile.write(str(fx_path), effects(), RATE)
    fx = cf.read_audio(fx_path)
    (D / "fx").mkdir(exist_ok=True)
    notes = []
    for lang in LANGS:
        name = "prologue_h" + ("" if lang == "en" else f"_{lang}")
        base = stereo_of(RAW / f"run_film_{name}.m4a")
        e = numpy.pad(fx, (0, max(0, len(base) - len(fx))))[: len(base)].copy()
        if lang == "en":
            # the English track has the page's flutter at the joins already: the effects keep clear of it
            for j in (30, 60):
                a, b = int((j - 1.2) * RATE), int((j + 1.4) * RATE)
                ramp = numpy.ones(len(e)); ramp[a:b] = 0
                e = e * numpy.convolve(ramp, numpy.ones(2000) / 2000, mode="same")
        mix = base + e[:, None]
        peak = numpy.abs(mix).max()
        mix *= .97 / peak if peak > .97 else 1
        soundfile.write(str(D / "fx" / f"{name}.wav"), mix, RATE)
        encode(mix, D / "fx" / f"run_film_{name}.m4a")
        notes += levels(name, mix.mean(axis=1), e) + [""]
    (D / "fx" / "levels.txt").write_text("\n".join(notes) + "\n", encoding="utf-8")
    print("\n".join(notes))


F = 1.45
A, B = 5.0, 30.0


def slow_track():
    """The slowed Japanese prologue's track: its voice and band (tools/prologue_ja_slow.py, make_mc.py) with the effects moved the same way."""
    fx = cf.read_audio(D / "prologue-fx.wav")
    cut = lambda a, b: fx[int(a * RATE):int(b * RATE)]
    fade = int(.04 * RATE)
    out = cut(0, A)
    for part in (cf.stretch(cut(A, B), 1 / F, RATE), cut(B, 90)):
        ramp = numpy.linspace(0, 1, fade)
        out = numpy.concatenate([out[:-fade], out[-fade:] * (1 - ramp) + part[:fade] * ramp, part[fade:]])
    base = stereo_of(D / "prologue-halgakos-slow-ja.wav") if False else None
    mc, rate = soundfile.read(str(D / "prologue-halgakos-slow-ja.wav"))
    mc = mc if mc.ndim > 1 else numpy.stack([mc, mc], axis=1)
    e = numpy.pad(out, (0, max(0, len(mc) - len(out))))[: len(mc)]
    mix = mc + e[:, None]
    peak = numpy.abs(mix).max()
    mix *= .97 / peak if peak > .97 else 1
    (D / "fx").mkdir(exist_ok=True)
    soundfile.write(str(D / "fx" / "prologue_h_ja_slow.wav"), mix, RATE)
    encode(mix, D / "fx" / "run_film_prologue_h_ja_slow.m4a")
    notes = levels("prologue_h_ja (slowed picture)", mix.mean(axis=1), e)
    (D / "fx" / "levels-slow.txt").write_text("\n".join(notes) + "\n", encoding="utf-8")
    print("\n".join(notes), "\nlength", round(len(mix) / RATE, 2), "s")


def review(lang):
    """The review copy: the picture the app ships (the slowed one for the Japanese), the track with the effects, and its captions burned in."""
    name = "prologue_h" + ("" if lang == "en" else f"_{lang}")
    slow = lang == "ja"
    audio = cf.read_audio(D / "fx" / ("run_film_prologue_h_ja_slow.m4a" if slow else f"run_film_{name}.m4a"))
    if slow:
        spec = json.loads((ROOT / "tools" / "cutscenes" / "prologue-mc.json").read_text(encoding="utf-8"))
        times = json.loads((D / "prologue-halgakos-slow-ja.times.json").read_text(encoding="utf-8"))
        rows = [(a, b, w.replace("\u2661", "")) for (a, b), w in zip(times, spec["tongues"]["ja"]["lines"])]
        picture = D / "prologue-ja-slow.mp4"
    else:
        rows = []
        for r in (ROOT / "app" / "src" / "main" / "assets" / "films" / f"{name}.{lang}.txt").read_text(encoding="utf-8").strip().split("\n"):
            a, b, w = r.split("\t", 2)
            rows.append((float(a), float(b), w.replace("\u2661", "")))
        picture = RAW / "run_film_prologue.mp4"
    import av
    box = av.open(str(picture))
    length = float(box.duration) / 1e6
    box.close()
    audio = audio[: int(length * RATE)]
    target = D.parent / "films-review" / f"prologue-{lang}-effects.mp4"
    print("glyphs missing:", cf.missing_glyphs("".join(r[2] for r in rows), lang) or "none")
    cf.write_video(picture, target, audio, captions=rows, bitrate=1_500_000, lang=lang)
    print("review copy", target, f"{target.stat().st_size / 1e6:.1f} MB")


if __name__ == "__main__":
    {"effects": lambda: soundfile.write(str(D / "prologue-fx.wav"), effects(), RATE), "tracks": tracks, "slow": slow_track,
     "review": lambda: [review(l) for l in sys.argv[2:]]}[sys.argv[1]]()
