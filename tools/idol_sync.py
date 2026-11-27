"""
Measures how Halgakos's voice track lies against the mouths of a film made by a video model whose own singing has been separated (Demucs).

    python tools/idol_sync.py FILM VOICE.wav VOCALS.wav [--bucket 5]

The model's own singing and calling (VOCALS.wav, the vocals stem of the film's original sound) is where the mouths move; VOICE.wav is the
voice track alone. Prints, for each bucket of seconds, how much of the time the mouths move and how much Halgakos's voice is there, and
what falls on each side: mouths with no voice (a mouth moving in silence) and voice with no mouth (a voice over a closed mouth).
"""
import sys

import numpy
import scipy.signal
import soundfile

RATE = 100  # envelope samples a second


def envelope(path, lo=200, hi=4000):
    x, rate = soundfile.read(path)
    if x.ndim > 1:
        x = x.mean(axis=1)
    sos = scipy.signal.butter(4, [lo, hi], "bandpass", fs=rate, output="sos")
    y = scipy.signal.sosfilt(sos, x)
    hop = rate // RATE
    n = len(y) // hop
    return numpy.sqrt((y[: n * hop].reshape(n, hop) ** 2).mean(axis=1))


def on_mask(env, floor=.2, gap=.2, least=.12):
    level = numpy.percentile(env, 95)
    on = env > level * floor
    # close small gaps, drop blips
    out = on.copy()
    run = 0
    for i, v in enumerate(on):
        if v:
            if 0 < run <= gap * RATE and i - run > 0:
                out[i - run:i] = True
            run = 0
        else:
            run += 1
    start = None
    for i, v in enumerate(list(out) + [False]):
        if v and start is None:
            start = i
        if not v and start is not None:
            if i - start < least * RATE:
                out[start:i] = False
            start = None
    return out


def spans(mask):
    out, start = [], None
    for i, v in enumerate(list(mask) + [False]):
        if v and start is None:
            start = i
        if not v and start is not None:
            out.append((start / RATE, i / RATE))
            start = None
    return out


def main():
    voice, vocals = sys.argv[2], sys.argv[3]
    bucket = int(sys.argv[sys.argv.index("--bucket") + 1]) if "--bucket" in sys.argv else 5
    mouth = on_mask(envelope(vocals))
    hers = on_mask(envelope(voice), floor=.12)
    n = min(len(mouth), len(hers))
    mouth, hers = mouth[:n], hers[:n]
    print(f"{'seconds':>9} {'mouths':>7} {'voice':>6} {'both':>6} {'mouths, no voice':>17} {'voice, no mouth':>16}")
    tot = [0, 0, 0, 0, 0]
    for a in range(0, n, bucket * RATE):
        m, h = mouth[a:a + bucket * RATE], hers[a:a + bucket * RATE]
        both, mo, vo = (m & h).sum() / RATE, (m & ~h).sum() / RATE, (h & ~m).sum() / RATE
        print(f"{a / RATE:4.0f}-{a / RATE + bucket:<4.0f} {m.sum() / RATE:7.1f} {h.sum() / RATE:6.1f} {both:6.1f} {mo:17.1f} {vo:16.1f}")
        for k, v in enumerate((m.sum() / RATE, h.sum() / RATE, both, mo, vo)):
            tot[k] += v
    print(f"{'total':>9} {tot[0]:7.1f} {tot[1]:6.1f} {tot[2]:6.1f} {tot[3]:17.1f} {tot[4]:16.1f}")
    print("mouth spans :", ", ".join(f"{a:.1f}-{b:.1f}" for a, b in spans(mouth)))
    print("voice spans :", ", ".join(f"{a:.1f}-{b:.1f}" for a, b in spans(hers)))


if __name__ == "__main__":
    main()
