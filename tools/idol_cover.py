"""For every mouth of hers (0.3 s or more) in 23-30 s, and in her other close-ups, the share of it covered by her voice. python tools/idol_cover.py [DIR]"""
import pathlib, sys
sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import idol_sync as sy
import idol_retime as rt

D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\idol")
folder = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else D / "retimed"
for shape in ("9x16", "16x9"):
    mouth = sy.on_mask(sy.envelope(str(D / f"idol-{shape}-vocals.wav")))
    for lang in ("en", "ja", "ko", "zh"):
        hers = sy.on_mask(sy.envelope(str(folder / f"idol-{shape}-{lang}-voice.wav")), floor=.08)
        n = min(len(mouth), len(hers))
        call, other = [], []
        for a, b in sy.spans(mouth[:n]):
            if b - a < .3:
                continue
            share = hers[int(a * 100):int(b * 100)].mean() * 100
            if 23 <= a < 30.3:
                call.append((a, b, share))
            elif rt.in_h(a):
                other.append((a, b, share))
        low = [f"{a:.1f}-{b:.1f} {s:.0f}%" for a, b, s in call + other if s < 80]
        print(f"{shape} {lang}: 23-30 s: " + "  ".join(f"{a:.1f}:{s:.0f}%" for a, b, s in call) + f" | other close-ups: {len(other)} mouths, under 80%: " + (", ".join(f"{a:.1f}-{b:.1f} {s:.0f}%" for a, b, s in other if s < 80) or "none"))
