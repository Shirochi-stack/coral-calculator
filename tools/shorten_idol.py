"""
Shortens the idol song's lines that ran past the mouths of the singers (see tools/idol_retime.py), by cutting words and rewriting none:
the spoken-lyrics spec tools/cutscenes/idol-mc.json is changed, in English and in its three tongues, and the list of what changed is written
to tools/cutscenes/idol-shortened.txt (and beside the user's original lyrics, which are never touched, in dist/cutscenes/lyrics-shortened.txt).
Run once: python tools/shorten_idol.py
"""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
SPEC = ROOT / "tools" / "cutscenes" / "idol-mc.json"
NOTE = ROOT / "tools" / "cutscenes" / "idol-shortened.txt"
BESIDE = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\lyrics-shortened.txt")

# line index: (english, ja, ko, zh)
SHORT = {
    8: ("Hands up. Higher.", "手を上げて。もっと高く。", "손 들어. 더 높이.", "举起手。再高点。"),
    15: ("Squish, Sloppy~♡ — eyes up!", "スクイッシュ、Sloppy~♡ ——顔を上げて！", "스퀴시, Sloppy~♡ ——눈 들어!", "啪叽，Sloppy~♡ ——抬起头！"),
    16: ("Squish, Sloppy~♡ — five colors!", "スクイッシュ、Sloppy~♡ ——五色よ！", "스퀴시, Sloppy~♡ ——다섯 가지 색!", "啪叽，Sloppy~♡ ——五种颜色！"),
    28: ("Gold. Lead.", "金。リード。", "금색. 리드.", "金色。领唱。"),
    31: ("Purple.", "紫。", "보라.", "紫色。"),
    38: ("Squish, Sloppy~♡ — encore!", "スクイッシュ、Sloppy~♡ ——アンコール！", "스퀴시, Sloppy~♡ ——앙코르!", "啪叽，Sloppy~♡ ——安可！"),
    39: ("Squish, Sloppy~♡ — say it!", "スクイッシュ、Sloppy~♡ ——言って！", "스퀴시, Sloppy~♡ ——말해 봐!", "啪叽，Sloppy~♡ ——说出来！"),
    40: ("Jump, heart, bow — again.", "ジャンプ、ハート、お辞儀——もう一回。", "점프, 하트, 인사——다시.", "跳、爱心、鞠躬——再来。"),
    41: ("Squish, Sloppy~♡ — don't stop!", "スクイッシュ、Sloppy~♡ ——止めないで！", "스퀴시, Sloppy~♡ ——멈추지 마!", "啪叽，Sloppy~♡ ——别停！"),
    43: ("My mint ice cream.", "私のミントアイス。", "내 민트 아이스크림.", "我的薄荷冰淇淋。"),
}


# The seconds each shortened line is said in (what the mouths give it): the voice is asked for a brisker pace to fit, not stretched afterwards.
ROOMS = {8: 1.7, 15: 1.25, 16: 1.3, 28: 1.3, 31: 0.9, 38: 1.5, 39: 1.5, 40: 1.7, 41: 1.4, 43: 1.2, 44: 0.8}


def original():
    """The spec as it was before anything was shortened: the commit that added the tongues."""
    import subprocess

    text = subprocess.run(["git", "show", "a9cebbf:tools/cutscenes/idol-mc.json"], cwd=ROOT, capture_output=True, check=True).stdout.decode("utf-8")
    return json.loads(text)


def main():
    spec = json.loads(SPEC.read_text(encoding="utf-8"))
    first = original()
    rows = ["The idol song's lines that were shortened, because they ran past the mouths of the singers in the film (before -> after).",
            "The user's own lyrics, dist/cutscenes/lyrics-from-user.txt, are untouched. Only words were cut; none was rewritten (but the last, the \"Now.\" of the ice-cream line).", ""]
    for i, (en, ja, ko, zh) in SHORT.items():
        line = spec["lines"][i]
        rows.append(f"EN  {first['lines'][i][2]}  ->  {en}")
        for lang, text in zip(("ja", "ko", "zh"), (ja, ko, zh)):
            rows.append(f"{lang.upper()}  {first['tongues'][lang]['lines'][i]}  ->  {text}")
            spec["tongues"][lang]["lines"][i] = text
        line[2] = en
        if i in ROOMS:
            line[1] = ROOMS[i]
        if len(line) > 4:
            line[4] = en.replace("~\u2661", "!")
        rows.append("")
    SPEC.write_text(json.dumps(spec, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    NOTE.write_text("\n".join(rows), encoding="utf-8")
    BESIDE.write_text("\n".join(rows), encoding="utf-8")
    print(len(SHORT), "lines shortened")


if __name__ == "__main__":
    main()
