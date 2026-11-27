"""Clipped edges in a voice-only track: a stretch of voice that begins or ends within a few milliseconds at a loud level (a clip cut into a word, or laid over another).
python tools/idol_edges.py [DIR]"""
import pathlib, sys
import numpy, soundfile

D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\idol")
folder = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else D / "retimed"


def scan(path):
    x, r = soundfile.read(str(path))
    x = x.mean(axis=1) if x.ndim > 1 else x
    hop = r // 1000  # 1 ms
    n = len(x) // hop
    env = numpy.sqrt((x[: n * hop].reshape(n, hop) ** 2).mean(axis=1))
    level = numpy.percentile(env, 95)
    on = numpy.convolve((env > level * .1).astype(float), numpy.ones(60) / 60, mode="same") > 0
    # spans of voice, gaps under 60 ms closed
    spans, start = [], None
    for i, v in enumerate(list(on) + [False]):
        if v and start is None:
            start = i
        if not v and start is not None:
            spans.append((start, i)); start = None
    bad = []
    for a, b in spans:
        if b - a < 120:
            continue
        mid = numpy.median(env[a:b]) + 1e-9
        end = env[max(a, b - 12):b - 2].mean() / mid
        begin = env[a + 2:a + 12].mean() / mid
        if end > .55:
            bad.append(f"ends hard at {b / 1000:.2f}")
        if begin > .8:
            bad.append(f"begins hard at {a / 1000:.2f}")
    return len(spans), bad


for shape in ("9x16", "16x9"):
    for lang in ("en", "ja", "ko", "zh"):
        f = folder / f"idol-{shape}-{lang}-voice.wav"
        if f.exists():
            n, bad = scan(f)
            print(f"{shape} {lang}: {n} stretches of voice, {len(bad)} hard edges" + (": " + ", ".join(bad[:14]) if bad else ""))
