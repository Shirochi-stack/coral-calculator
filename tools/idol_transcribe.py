"""Transcribes the original singing of the idol films (MAGI-2's own vocals stems, Demucs) with word timestamps, in 12 s windows (so that nothing loops),
and counts syllable nuclei per mouth phrase from the stem's own envelope (a language-free count that every new line is written against).

    python tools/idol_transcribe.py       writes dist/cutscenes/idol/transcripts/idol-SHAPE.json and .txt
"""
import json, pathlib, sys
import numpy, scipy.signal, soundfile
from faster_whisper import WhisperModel
ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import idol_sync as sy  # noqa: E402
import idol_retime as ir  # noqa: E402
I = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\idol")
out = I / "transcripts"; out.mkdir(exist_ok=True)
model = WhisperModel("small", device="cpu", compute_type="int8")


def nuclei(path, a, b):
    """Syllable nuclei in [a, b]: peaks of the 300-3000 Hz envelope smoothed to about 8 Hz."""
    x, r = soundfile.read(str(path))
    x = x.mean(axis=1) if x.ndim > 1 else x
    sos = scipy.signal.butter(4, [300, 3000], "bandpass", fs=r, output="sos")
    y = numpy.abs(scipy.signal.sosfilt(sos, x))
    hop = r // 100
    env = y[: len(y) // hop * hop].reshape(-1, hop).mean(axis=1)
    env = scipy.signal.sosfiltfilt(scipy.signal.butter(2, 8, "low", fs=100, output="sos"), env)
    seg = env[int(a * 100):int(b * 100)]
    if len(seg) < 5:
        return 0
    pk, _ = scipy.signal.find_peaks(seg, prominence=.12 * numpy.percentile(env, 95), distance=8)
    return len(pk)


for shape in ("9x16", "16x9"):
    v = I / f"idol-{shape}-vocals.wav"
    x, r = soundfile.read(str(v)); x = x.mean(axis=1) if x.ndim > 1 else x
    words = []
    for t0 in range(0, int(len(x) / r), 12):
        seg = x[int(max(0, t0 - .5) * r):int((t0 + 12.5) * r)].astype("float32")
        if len(seg) < r:
            continue
        import tempfile
        tmp = pathlib.Path(tempfile.gettempdir()) / "idol_win.wav"; soundfile.write(str(tmp), seg, r)
        segs, info = model.transcribe(str(tmp), word_timestamps=True, vad_filter=False, beam_size=3, condition_on_previous_text=False, language="en")
        for s in segs:
            for w in (s.words or []):
                a = round(w.start + max(0, t0 - .5), 2)
                if t0 <= a < t0 + 12:
                    words.append({"w": w.word.strip(), "a": a, "b": round(w.end + max(0, t0 - .5), 2)})
    phrases = []
    for a, b in ir.phrases_of(v):
        ws = [w for w in words if a - .2 <= w["a"] <= b]
        phrases.append({"a": round(a, 2), "b": round(b, 2), "syllables": nuclei(v, a, b), "words": " ".join(w["w"] for w in ws)})
    json.dump({"shape": shape, "words": words, "phrases": phrases}, open(out / f"idol-{shape}.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    open(out / f"idol-{shape}.txt", "w", encoding="utf-8").write("\n".join(f"{p['a']:6.2f}-{p['b']:6.2f}  {p['syllables']:2d} syl  {p['words']}" for p in phrases))
    print(shape, len(words), "words,", len(phrases), "phrases", flush=True)
