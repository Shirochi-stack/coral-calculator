"""Verifies the v4 clips against the original voice and writes dist/cutscenes/clips/review/v4-table.md. python tools/clips_v4_report.py"""
import json, sys, pathlib
sys.path.insert(0, "tools")
import numpy, soundfile
import idol_sync as sy
import clips_data as D, clips_v4 as V
C = pathlib.Path("dist/cutscenes/clips"); R = C / "review"
EXPR = {  # what her face does, from the frames at the phrase times
 ("03",0):"serious then a grin, mouth opening", ("03",1):"turned away, then grinning, swinging the book up",
 ("04",0):"crying, eyes shut, mouth open", ("04",1):"crying, teeth clenched, pushing the ink",
 ("05",0):"wide-eyed alarm, mouth open", ("05",1):"alarm turning to anger", ("05",2):"outrage, teeth bared, fists up",
 ("07",0):"smiling, calm", ("07",1):"smiling, swinging", ("07",2):"smug smirk, pointing at Tsubakieh",
 ("09",0):"grinning, gun raised", ("09",1):"delighted, ink spraying",
 ("10",0):"smirk, then a grin, katana forward",
 ("11",0):"weeping, hands up, then clenched teeth pushing ink", ("11",1):"smug smile, rolling pin raised",
 ("12",0):"small in a wide shot, smiling, pen planted",
 ("13",0):"the Mawang diving, mouth open, grave", ("13",1):"Halgakos blocking, mouth open, firm",
 ("14",0):"the dragon, mouth open, imperious", ("14",1):"Halgakos holding her book, steady",
 ("15",0):"smiling, mid-swing", ("15",1):"shocked, mouth an 'o'",
 ("16",0):"flying, calm", ("16",1):"back to back, mouth open, brisk",
 ("17",0):"smiling, writing", ("17",1):"smug, writing",
 ("18",0):"grinning, book open", ("18",1):"grinning, beam firing", ("18",2):"grinning, eyes closed, beam firing",
 ("21",0):"grinning, wing clash", ("21",1):"grinning, pushing the book", ("21",2):"grinning, gust of pages",
 ("25",0):"the five posed, Halgakos centre, confident",
 ("31",0):"furious, fists clenched, shouting at the splatted cone", ("31",1):"teeth bared, hand rising with the beam",
 ("33",0):"mouth open in a shout, bat raised",
}
rows = ["# Clips v4: what was kept, rewritten or removed, and how well each line sits on its mouth", "",
        "Original = what the generated mouth says (Whisper on its Demucs vocals stem). Onset/offset error = our voice layer's own first/last sound against the original phrase's (vocals stem) onset/end, in ms (+ = ours later).",
        "Voice with no mouth = seconds of our voice outside every mouth span of the original; mouth with no voice = seconds of the original's mouths that we leave silent.", ""]
summary = []
for c in D.CLIPS:
    n = c["id"][:2]
    if n not in V.PHRASES: continue
    orig = C / "transcripts" / f"{c['id']}.txt"
    otext = orig.read_text(encoding="utf-8").split("\n")[0]
    vp = f"dist/cutscenes/clips/{c['id']}/{c['id']}-vocals.wav"
    mouth = sy.on_mask(sy.envelope(vp, lo=150, hi=5000), floor=.12, gap=.12, least=.08)
    rows += [f"## {c['id']}", "", f"Original: {otext}", ""]
    for lang in ("en", "ja", "ko", "zh"):
        vf = R / "v4-voice" / f"{c['id']}-{lang}.wav"
        if not vf.exists(): continue
        x, r = soundfile.read(str(vf)); env = sy.envelope(str(vf), lo=150, hi=5000); hers = sy.on_mask(env, floor=.12, gap=.08, least=.05)
        n_ = min(len(mouth), len(hers)); m, h = mouth[:n_], hers[:n_]
        vo = (h & ~m).sum() / sy.RATE; mo = (m & ~h).sum() / sy.RATE
        rows += [f"**{lang.upper()}**: voice with no mouth {vo:.2f} s; mouth with no voice {mo:.2f} s", "", "| # | speaker | status | face | original | our line | placed | onset err | offset err | mode |", "|---|---|---|---|---|---|---|---|---|---|"]
        errs = []
        for k, ph in enumerate(V.PHRASES[n]):
            phs = V.PHRASES[n]
            prev_b = phs[k - 1]["b"] + .05 if k else 0.0
            next_a = phs[k + 1]["a"] - .05 if k + 1 < len(phs) else 99.0
            lo, hi = int(max(ph["a"] - .3, prev_b) * sy.RATE), int(min(ph["b"] + .6, next_a) * sy.RATE)
            seg = hers[lo:hi]; idx = numpy.where(seg)[0]
            if len(idx):
                on = (lo + idx[0]) / sy.RATE; off = (lo + idx[-1]) / sy.RATE
            else: on = off = float("nan")
            oe, fe = (on - ph["a"]) * 1000, (off - ph["b"]) * 1000
            errs.append((oe, fe))
            ow = " ".join(w["w"] for w in json.load(open(C / "transcripts" / f"{c['id']}.json", encoding="utf-8"))["words"] if ph["a"] - .3 <= w["a"] <= ph["b"])
            rows.append(f"| {k} | {ph['who']} | {ph['status']} | {EXPR.get((n, k), '')} | {ow} ({ph['a']:.2f}-{ph['b']:.2f}) | {ph['text'][lang]} | {on:.2f}-{off:.2f} | {oe:+.0f} ms | {fe:+.0f} ms | |")
        rows.append("")
        summary.append((c["id"], lang, vo, mo, max(abs(e[0]) for e in errs if e[0]==e[0]), max(abs(e[1]) for e in errs if e[1]==e[1])))
    for k, ph in enumerate(V.PHRASES[n]):
        rows.append(f"- line {k}: {ph['status']}: {ph['why']}")
    for what, why in V.REMOVED.get(n, []):
        rows.append(f"- REMOVED: {what}: {why}")
    rows.append("")
rows += ["## Summary (worst phrase per clip and language)", "", "| clip | lang | voice no mouth s | mouth no voice s | max onset err ms | max offset err ms |", "|---|---|---|---|---|---|"]
rows += [f"| {a} | {b} | {c:.2f} | {d:.2f} | {e:.0f} | {f:.0f} |" for a, b, c, d, e, f in summary]
(R / "v4-table.md").write_text("\n".join(rows), encoding="utf-8")
print(len(summary), "rows;", "worst onset", max(s[4] for s in summary), "ms; worst offset", max(s[5] for s in summary), "ms")
