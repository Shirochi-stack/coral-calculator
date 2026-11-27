"""The mouths that move in her close-ups with no voice (0.3 s or more): python tools/idol_silence.py [DIR]  (DIR: where the voice-only wavs are; default dist/cutscenes/idol/retimed)"""
import pathlib, sys
import numpy
sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import idol_sync as sy
import idol_retime as rt

D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\idol")
folder = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else D / "retimed"
for shape in ("9x16", "16x9"):
    mouth = sy.on_mask(sy.envelope(str(D / f"idol-{shape}-vocals.wav")))
    for lang in ("en", "ja", "ko", "zh"):
        f = folder / f"idol-{shape}-{lang}-voice.wav"
        if not f.exists():
            continue
        hers = sy.on_mask(sy.envelope(str(f)), floor=.12)
        n = min(len(mouth), len(hers))
        out = []
        for a, b in sy.spans(mouth[:n]):
            if b - a < .3 or not rt.in_h(a):
                continue
            covered = hers[int(a * 100):int(b * 100)].mean()
            if covered < .35:
                out.append(f"{a:.1f}-{b:.1f}")
        print(f"{shape} {lang}: silent mouths in her close-ups: {', '.join(out) if out else 'none'}")
