"""
Shorter ways to say each line of the idol song, for the mouths that are too short for the line. Each line (by its number in tools/cutscenes/idol-mc.json)
has two shorter forms in each language: a short one, and a single word or call. The words are only cut from the line, and the catchphrase stays
where there is room for it; they are not word-for-word translations in Japanese and Korean, but the shortest natural way to say the same.

    python tools/idol_variants.py spec        writes tools/cutscenes/idol-variants-mc.json: the spec with every shorter form added as a line of its own,
                                              at its own place far from the song, so that make_mc.py records all of them in one go
    python tools/idol_variants.py note        writes tools/cutscenes/idol-shortened.txt and dist/cutscenes/lyrics-shortened.txt: every form, in four languages

idol_retime.py then chooses for each mouth the longest form that fits it at a natural pace (stretched by no more than 1.15).
"""
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
BASE = ROOT / "tools" / "cutscenes" / "idol-mc.json"
OUT = ROOT / "tools" / "cutscenes" / "idol-variants-mc.json"
NOTE = ROOT / "tools" / "cutscenes" / "idol-shortened.txt"
BESIDE = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\lyrics-shortened.txt")
ORIGINAL_COMMIT = "a9cebbf"

# number of the line: {language: (short, single word or call)}
V = {
    4: {"en": ("Lights up. How cute.", "How cute."), "ja": ("ライトアップ。かわいい。", "かわいい。"), "ko": ("조명 켜졌다. 귀여워.", "귀여워."), "zh": ("灯亮了。真可爱。", "可爱。")},
    5: {"en": ("Five of us.", "Five."), "ja": ("私たち五人。", "五人。"), "ko": ("우리 다섯.", "다섯."), "zh": ("我们五个。", "五个。")},
    6: {"en": ("Book's open. Sing.", "Sing."), "ja": ("本が開いた。歌って。", "歌って。"), "ko": ("책이 열렸어. 노래해.", "노래해."), "zh": ("书开了。唱吧。", "唱！")},
    7: {"en": ("Loud.", "Louder!"), "ja": ("大きく。", "もっと！"), "ko": ("크게.", "더!"), "zh": ("大声点。", "大声！")},
    8: {"en": ("Hands up.", "Up!"), "ja": ("手を上げて。", "上げて。"), "ko": ("손 들어.", "위로."), "zh": ("举起手。", "举手。")},
    9: {"en": ("Call my name.", "My name."), "ja": ("名前を呼んで。", "呼んで。"), "ko": ("내 이름 불러.", "불러."), "zh": ("叫我名字。", "叫！")},
    15: {"en": ("Sloppy~♡ — eyes up!", "Eyes up!"), "ja": ("Sloppy~♡ ——顔上げて！", "顔上げて！"), "ko": ("Sloppy~♡ ——눈 들어!", "눈 들어!"), "zh": ("Sloppy~♡ ——抬头！", "抬头！")},
    16: {"en": ("Sloppy~♡ — five colors!", "Five colors!"), "ja": ("Sloppy~♡ ——五色！", "五色！"), "ko": ("Sloppy~♡ ——다섯 색!", "다섯 색!"), "zh": ("Sloppy~♡ ——五色！", "五色！")},
    17: {"en": ("Miss the beat —", "Beat!"), "ja": ("リズム外したら——", "リズム！"), "ko": ("박자 놓치면——", "박자!"), "zh": ("漏拍就重来——", "节拍！")},
    18: {"en": ("Sloppy~♡ — up!", "Up!"), "ja": ("Sloppy~♡ ——立って！", "立って！"), "ko": ("Sloppy~♡ ——일어서!", "일어서!"), "zh": ("Sloppy~♡ ——站起来！", "站起来！")},
    19: {"en": ("Pink. Center.", "Pink."), "ja": ("ピンク。センター。", "ピンク。"), "ko": ("핑크. 센터.", "핑크."), "zh": ("粉色。中心。", "粉色。")},
    22: {"en": ("Red. Warm.", "Red."), "ja": ("赤。あったかい。", "赤。"), "ko": ("빨강. 따뜻해.", "빨강."), "zh": ("红色。温暖。", "红色。")},
    25: {"en": ("Blue. Crying.", "Blue."), "ja": ("青。泣き虫。", "青。"), "ko": ("파랑. 울보.", "파랑."), "zh": ("蓝色。爱哭。", "蓝色。")},
    28: {"en": ("Gold. Lead.", "Gold."), "ja": ("金。リード。", "金。"), "ko": ("금색. 리드.", "금색."), "zh": ("金色。领唱。", "金色。")},
    34: {"en": ("Down the runway.", "Runway."), "ja": ("ランウェイ。", "進んで。"), "ko": ("런웨이.", "걸어."), "zh": ("走T台。", "走！")},
    35: {"en": ("Hold on.", "Hold."), "ja": ("しがみついてる。", "必死。"), "ko": ("꽉 잡아.", "잡아."), "zh": ("抓紧了。", "抓紧。")},
    36: {"en": ("Not bad. For you.", "Not bad."), "ja": ("悪くない。", "いいね。"), "ko": ("나쁘지 않아.", "좋아."), "zh": ("还不错。", "不错。")},
    37: {"en": ("Once more. Up.", "Again."), "ja": ("もう一回。", "もう。"), "ko": ("한 번 더.", "다시."), "zh": ("再来。", "再来！")},
    38: {"en": ("Sloppy~♡ — encore!", "Encore!"), "ja": ("Sloppy~♡ ——アンコール！", "アンコール！"), "ko": ("Sloppy~♡ ——앙코르!", "앙코르!"), "zh": ("Sloppy~♡ ——安可！", "安可！")},
    39: {"en": ("Sloppy~♡ — say it!", "Say it!"), "ja": ("Sloppy~♡ ——言って！", "言って！"), "ko": ("Sloppy~♡ ——말해 봐!", "말해!"), "zh": ("Sloppy~♡ ——说出来！", "说！")},
    40: {"en": ("Jump, heart, bow.", "Jump!"), "ja": ("ジャンプ、ハート、お辞儀。", "ジャンプ！"), "ko": ("점프, 하트, 인사.", "점프!"), "zh": ("跳、爱心、鞠躬。", "跳！")},
    41: {"en": ("Sloppy~♡ — don't stop!", "Don't stop!"), "ja": ("Sloppy~♡ ——止めないで！", "止めないで！"), "ko": ("Sloppy~♡ ——멈추지 마!", "멈추지 마!"), "zh": ("Sloppy~♡ ——别停！", "别停！")},
    42: {"en": ("You're welcome.", "Welcome."), "ja": ("どういたしまして。", "どうぞ。"), "ko": ("천만에.", "별말씀."), "zh": ("不客气。", "不客气。")},
    43: {"en": ("Mint ice cream, Sloppy~♡", "Ice cream, Sloppy~♡"), "ja": ("ミントアイス、Sloppy~♡", "アイス、Sloppy~♡"), "ko": ("민트 아이스크림, Sloppy~♡", "아이스, Sloppy~♡"), "zh": ("薄荷冰淇淋，Sloppy~♡", "冰淇淋，Sloppy~♡")},
}
FIRST = 100.0   # where the first of the forms is said in the spec, and how far apart they are
EVERY = 5.0


def forms():
    """(line number, level 1 or 2) in order."""
    return [(i, level) for i in sorted(V) for level in (1, 2)]


def spec():
    base = json.loads(BASE.read_text(encoding="utf-8"))
    for n, (i, level) in enumerate(forms()):
        at = FIRST + n * EVERY
        how = "call" if level == 2 else "solo"
        base["lines"].append([at, 4.5, V[i]["en"][level - 1], how])
        for lang in ("ja", "ko", "zh"):
            base["tongues"][lang]["lines"].append(V[i][lang][level - 1])
    base["seconds"] = int(FIRST + len(forms()) * EVERY + 10)
    base["out"] = "idol/songs/idol-variants"
    OUT.write_text(json.dumps(base, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    print(len(forms()), "forms;", base["seconds"], "s")


def note():
    import subprocess

    first = json.loads(subprocess.run(["git", "show", f"{ORIGINAL_COMMIT}:tools/cutscenes/idol-mc.json"], cwd=ROOT, capture_output=True, check=True).stdout.decode("utf-8"))
    rows = ["The idol song's lines, and the shorter forms each can be said in, for the mouths of the film that are too short for the whole line.",
            "The film picks, for each mouth, the longest form that fits it at a natural pace (stretched by no more than 1.15). Only words are cut. The user's own lyrics,",
            "dist/cutscenes/lyrics-from-user.txt, are untouched. Each block: the line as it was (English, then Japanese, Korean, Chinese), then its short form and its single word or call.", ""]
    for i in sorted(V):
        for lang in ("en", "ja", "ko", "zh"):
            was = first["lines"][i][2] if lang == "en" else first["tongues"][lang]["lines"][i]
            rows.append(f"{lang.upper()}  {was}  ->  {V[i][lang][0]}  ->  {V[i][lang][1]}")
        rows.append("")
    NOTE.write_text("\n".join(rows), encoding="utf-8")
    BESIDE.write_text("\n".join(rows), encoding="utf-8")
    print("written")


if __name__ == "__main__":
    {"spec": spec, "note": note}[sys.argv[1]]()
