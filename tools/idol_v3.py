"""
The idol song, v3: strictly the original's own voice. The film's singing (MAGI-2's, separated with Demucs, transcribed with Whisper: dist/cutscenes/idol/transcripts/idol-SHAPE.json)
is our lyrics sung; her lines are THAT transcription, cleaned against the lyrics it was given from (dist/cutscenes/idol/songs/lyrics.txt) and adapted into English,
Japanese, Korean and Chinese, phrase by phrase, to the phrase's syllable count. Voice only where the original has voice: every line begins on the onset of an original phrase and ends with
it; no filler, no extra calls, no whisper double. Where the five sing together (the film's group shots) the line is the five in unison. The instrumental is the original's own
(Demucs no-vocals stem), under her at the balance the original has between its voice and its music.

    python tools/idol_v3.py record SHAPE      records what is missing (dist/cutscenes/idol/v3), at each mouth's pace
    python tools/idol_v3.py build SHAPE LANG  writes dist/cutscenes/idol/retimed-v3/idol-SHAPE-LANG-{voice,mix}.wav and -captions.txt
    python tools/idol_v3.py lyrics            writes dist/cutscenes/films-review/idol-v3-lyrics.txt
"""
import concurrent.futures
import hashlib
import json
import math
import pathlib
import sys
import time

import numpy
import scipy.signal
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import clips_fit as cf  # noqa: E402
import idol_retime as ir  # noqa: E402
import idol_v4 as iv  # noqa: E402

D = ir.D
I = D / "idol"
V3 = I / "v3"
V3.mkdir(exist_ok=True)
OUTD = I / "retimed-v3"
OUTD.mkdir(exist_ok=True)
RATE = 48000
LANGS = ("en", "ja", "ko", "zh")
S = {"en": "Sloppy", "ja": "スロッピー", "ko": "슬로피", "zh": "Sloppy"}
Q = {"en": "Squish", "ja": "スクイッシュ", "ko": "스퀴시", "zh": "啪叽"}
N = {"H": {"en": "Halgakos", "ja": "ハルガコス", "ko": "할가코스", "zh": "哈尔加科斯"}, "C": {"en": "Chikuwa", "ja": "ちくわ", "ko": "치쿠와", "zh": "竹轮"},
     "R": {"en": "Rune", "ja": "ルーン", "ko": "루네", "zh": "露恩"}, "I": {"en": "Illya", "ja": "イリヤ", "ko": "일리야", "zh": "伊莉雅"}, "T": {"en": "Tsubakieh", "ja": "ツバキエ", "ko": "츠바키에", "zh": "椿"}}


def L(en, ja, ko, zh):
    out = {"en": en, "ja": ja, "ko": ko, "zh": zh}
    return {lg: t.format(S=S[lg], Q=Q[lg], **{k: v[lg] for k, v in N.items()}) for lg, t in out.items()}


# The lyrics' lines as the original sings them, in four languages (the original is English; the other three adapt it, to the syllables of the phrase).
T = {
    "lights": L("Lights go up, the pages fly, five of us beneath the sky", "ライトが上がり、ページが舞う、空の下に私たち五人", "조명이 켜지고 페이지가 날아, 하늘 아래 우리 다섯", "灯光亮起，书页飞舞，天空之下我们五人"),
    "open": L("Open the book, the story's ours, sing it loud and light the stars", "本を開こう、物語は私たちのもの、大きく歌って星を灯そう", "책을 펼쳐, 이야기는 우리 거야, 크게 노래해 별을 밝혀", "翻开书，故事属于我们，放声歌唱点亮星星"),
    "open1": L("Open the book,", "本を開こう、", "책을 펼쳐,", "翻开书，"),
    "open2": L("the story's ours, sing it loud and light the stars", "物語は私たちのもの、大きく歌って星を灯そう", "이야기는 우리 거야, 크게 노래해 별을 밝혀", "故事属于我们，放声歌唱点亮星星"),
    "hands": L("Hands up high, penlights bright", "手を高く、ペンライト輝いて", "손을 높이, 펜라이트 반짝여", "高举双手，荧光棒闪耀"),
    "call": L("Call my name and call it right", "私の名前を呼んで、ちゃんと呼んで", "내 이름을 불러, 제대로 불러", "叫我的名字，要叫对"),
    "calls": L("Call my name and call it right, right!", "私の名前を呼んで、ちゃんと呼んで、ちゃんと！", "내 이름을 불러, 제대로 불러, 제대로!", "叫我的名字，要叫对，叫对！"),
    "sloppy": L("{S}", "{S}", "{S}", "{S}"),
    "squish": L("{Q}!", "{Q}！", "{Q}!", "{Q}！"),
    "louder": L("Louder!", "もっと大きく！", "더 크게!", "再大声点！"),
    "tonight": L("{S}, we're on tonight", "{S}、今夜は私たちの番", "{S}, 오늘 밤은 우리 거야", "{S}，今夜属于我们"),
    "colors": L("{Q}, {S}, five colors of light", "{Q}、{S}、五色の光", "{Q}, {S}, 다섯 빛깔", "{Q}，{S}，五色之光"),
    "every": L("Every heart, every page, every beat", "すべての心、すべてのページ、すべての拍", "모든 마음, 모든 페이지, 모든 박자", "每颗心，每一页，每个节拍"),
    "feet": L("{S}, get up on your feet", "{S}、立ち上がって", "{S}, 일어서", "{S}，站起来"),
    "pink": L("Pink in the centre, smug as can be. {H}!", "センターはピンク、得意げに。{H}！", "센터는 핑크, 의기양양하게. {H}!", "粉色在中央，得意洋洋。{H}！"),
    "red": L("{S}! Red as a ribbon, warm as bread. {C}! {C}!", "{S}！リボンみたいに赤く、パンみたいに温かい。{C}！{C}！", "{S}! 리본처럼 빨갛고 빵처럼 따뜻해. {C}! {C}!", "{S}！红如丝带，暖如面包。{C}！{C}！"),
    "blue": L("Blue as the ink and the tears I've shed", "インクのように青く、流した涙のように", "잉크처럼 파랗고, 흘린 눈물처럼", "蓝如墨水，蓝如我流过的泪"),
    "rune": L("{R}! {R}!", "{R}！{R}！", "{R}! {R}!", "{R}！{R}！"),
    "gold": L("Gold in the spotlight, born to lead, {I}!", "スポットライトの金、リードするために生まれた、{I}！", "스포트라이트의 금빛, 이끌기 위해 태어났어, {I}!", "聚光灯下的金色，天生的领唱，{I}！"),
    "gold1": L("Gold in the spotlight,", "スポットライトの金、", "스포트라이트의 금빛,", "聚光灯下的金色，"),
    "gold2": L("born to lead, {I}!", "リードするために生まれた、{I}！", "이끌기 위해 태어났어, {I}!", "天生的领唱，{I}！"),
    "purple": L("Purple and calm, I swing, I speed", "紫は穏やか、振って、駆ける", "보라는 차분하게, 휘둘러 달려", "紫色沉静，我挥棒，我疾驰"),
    "tsub": L("{T}!", "{T}！", "{T}!", "{T}！"),
    "tsubrun": L("{T}! {T}! Down the runway, hand in hand", "{T}！{T}！ランウェイを、手をつないで", "{T}! {T}! 런웨이를 손잡고", "{T}！{T}！沿着T台，手拉手"),
    "runway": L("Down the runway, hand in hand", "ランウェイを、手をつないで", "런웨이를 손잡고", "沿着T台，手拉手"),
    "voice": L("Every voice in storybook land", "物語の国のすべての声", "이야기 나라의 모든 목소리", "故事国度里的每个声音"),
    "bad": L("Not bad for you, you kept the beat, one more time, up on your feet!", "あなたにしては悪くない、リズムも守れた、もう一回、立ち上がって！", "너치곤 나쁘지 않아, 박자도 맞췄어, 한 번 더, 일어서!", "对你来说不错，跟上了节拍，再来一次，站起来！"),
    "bad1": L("Not bad for you, you kept the beat", "あなたにしては悪くない、リズムも守れた", "너치곤 나쁘지 않아, 박자도 맞췄어", "对你来说不错，跟上了节拍"),
    "bad2": L("One more time, up on your feet!", "もう一回、立ち上がって！", "한 번 더, 일어서!", "再来一次，站起来！"),
    "encore": L("{S}, encore, repeat!", "{S}、アンコール、もう一度！", "{S}, 앙코르, 다시!", "{S}，安可，再来！"),
    "encore1": L("{S},", "{S}、", "{S},", "{S}，"),
    "encore2": L("encore, repeat!", "アンコール、もう一度！", "앙코르, 다시!", "安可，再来！"),
    "yeah": L("Yeah!", "イェイ！", "예!", "耶！"),
    "thank": L("Thank you!", "ありがとう！", "고마워!", "谢谢！"),
    "mint": L("Now where is my mint ice cream?", "私のミントアイスはどこ？", "내 민트 아이스크림은 어디 있지?", "我的薄荷冰淇淋在哪？"),
}
# 9x16: the start of each original mouth phrase (the transcript's own) -> which line it sings
TEXT9 = {8.18: "lights", 12.22: "open", 17.39: "hands", 19.40: "call", 21.89: "call", 24.94: "sloppy", 25.83: "sloppy", 26.93: "squish", 27.87: "sloppy", 28.93: "louder",
         30.60: "tonight", 32.09: "colors", 34.08: "every", 36.10: "feet", 38.10: "pink", 41.60: "red", 46.18: "blue", 48.92: "rune", 50.14: "gold", 54.27: "purple",
         58.00: "tsubrun", 64.12: "voice", 68.09: "bad", 72.72: "tonight", 74.28: "colors", 76.22: "every", 80.69: "encore", 83.14: "yeah", 84.35: "thank", 87.15: "mint"}
T["red16"] = {lg: T["red"][lg] + " " + T["blue"][lg] + " " + T["rune"][lg] for lg in LANGS}
T["tc"] = {lg: T["tonight"][lg] + " " + T["colors"][lg] for lg in LANGS}
TEXT16 = {7.99: "lights", 13.32: "open2", 17.21: "hands", 20.22: "calls", 25.24: "squish", 28.02: "louder", 28.84: "squish", 30.68: "tonight", 32.56: "colors", 34.15: "every", 36.13: "feet",
          38.08: "pink", 41.51: "red16", 50.05: "gold1", 51.81: "gold2", 54.04: "purple", 57.05: "tsub", 58.07: "tsub", 59.15: "runway", 64.07: "voice", 68.10: "bad1", 70.15: "bad2",
          72.55: "tc", 76.06: "every", 80.50: "encore1", 81.79: "encore2", 84.37: "thank", 87.34: "mint"}
TEXTS = {"9x16": TEXT9, "16x9": TEXT16}
MEMBERS = ("C", "R", "I", "T")
SCENE_H = ("She is the centre of an idol group on stage, singing this lyric of the idol song cheerfully and clearly, bright, smug and playful, in time with the band, ")
SCENE_G = "She is singing along, cheerful and playful, with four other girls at a concert, singing the line together with them in unison, in time. "


def phrases_for(shape):
    tr = json.loads((I / "transcripts" / f"idol-{shape}.json").read_text(encoding="utf-8"))["phrases"]
    out = []
    for p in tr:
        key = TEXTS[shape].get(round(p["a"], 2))
        if key is None:
            continue
        out.append(dict(a=p["a"], b=p["b"], key=key, syl=p["syllables"], orig=p["words"], group=ir.in_g((p["a"] + p["b"]) / 2) and not (24 <= p["a"] < 30.3)))
    return out


def tag_of(who, text, span):
    return hashlib.sha1(f"{who}|{text}|{round(span, 1)}".encode("utf-8")).hexdigest()[:12]


def make_one(key, ph, lang):
    import make_run_voice as voice
    text = T[ph["key"]][lang]
    span = ph["b"] - ph["a"]
    tag = tag_of("H", text, span)
    path, meta = V3 / f"H-{tag}-{lang}.wav", V3 / f"H-{tag}-{lang}.json"
    if not path.exists():
        mult, best, best_q, best_rate = 1.0, None, None, 1.0
        for rd in range(5):
            sound, rate = iv.take(key, "H", text, lang, SCENE_H + ("quickly." if mult > 1 else "unhurried."), mult)
            q = len(sound) / voice.RATE_OUT / span
            if best is None or abs(math.log(q)) < abs(math.log(best_q)):
                best, best_q, best_rate = sound, q, rate
            if .93 <= q <= 1.07:
                break
            mult = min(1.5, max(.65, (rate / voice.VOICES["H"][1]) * q))
        soundfile.write(str(path), best, voice.RATE_OUT)
        meta.write_text(json.dumps({"q": best_q, "rate": best_rate}), encoding="utf-8")
    rate = json.loads(meta.read_text(encoding="utf-8"))["rate"]
    if ph["group"]:
        for who in MEMBERS:
            p2 = V3 / f"{who}-{tag}-{lang}.wav"
            if not p2.exists():
                sound, _ = iv.take(key, who, text, lang, voice.MANNER.get(who, "") + SCENE_G, 1.0, base_rate_override=min(1.5, max(.55, voice.VOICES[who][1] * rate / voice.VOICES["H"][1])))
                soundfile.write(str(p2), sound, voice.RATE_OUT)


def record(shape):
    import make_run_voice as voice
    key = voice.secret("INWORLD_API_KEY")
    jobs = [(ph, lang) for ph in phrases_for(shape) for lang in LANGS]
    with concurrent.futures.ThreadPoolExecutor(6) as pool:
        for f in [pool.submit(make_one, key, ph, lang) for ph, lang in jobs]:
            f.result()
    print("recorded", shape, len(jobs), flush=True)


def load(path):
    x, r = soundfile.read(str(path))
    x = x.mean(axis=1) if x.ndim > 1 else x
    x = scipy.signal.resample_poly(x, RATE, r).astype("float32")
    hop = RATE // 100
    env = numpy.sqrt((x[: len(x) // hop * hop].reshape(-1, hop) ** 2).mean(axis=1))
    loud = numpy.where(env > .07 * env.max())[0]
    if len(loud):
        x = x[max(0, (loud[0] - 1) * hop):(loud[-1] + 3) * hop]
    return x


def build(shape, lang):
    import idol_sync as sy
    track = numpy.zeros(90 * RATE, dtype=numpy.float32)
    rows, notes = [], []
    for ph in phrases_for(shape):
        text = T[ph["key"]][lang]
        span = ph["b"] - ph["a"]
        tag = tag_of("H", text, span)
        x = load(V3 / f"H-{tag}-{lang}.wav")
        nat = len(x) / RATE
        final = min(max(span, nat / 1.2), nat / .85)
        q = nat / final
        piece = cf.edged(cf.stretch(x, q, RATE) if abs(q - 1) > .02 else x)
        if ph["group"]:
            mixed = piece.copy()
            for who, (gain, off) in zip(MEMBERS, ((.5, .012), (.4, .02), (.5, .006), (.45, .016))):
                g = load(V3 / f"{who}-{tag}-{lang}.wav")
                g = cf.stretch(g, min(1.9, max(.6, len(g) / len(piece))), RATE)
                g = cf.edged(g[: len(piece)])
                o = int(off * RATE)
                mixed[o:o + len(g)] += gain * g[: max(0, len(mixed) - o)]
            peak = numpy.abs(mixed).max()
            mixed *= max(numpy.abs(piece).max(), .3) / peak if peak > 0 else 1
            piece = mixed
        s = int(ph["a"] * RATE)
        track[s:s + len(piece)] += piece[: len(track) - s]
        rows.append((ph["a"], ph["a"] + len(piece) / RATE, text))
        notes.append(f"{ph['a']:6.2f}-{ph['b']:6.2f} original: {ph['orig']!r} ({ph['syl']} syl)  ->  ours ends {ph['a'] + len(piece) / RATE:6.2f}  x{q:.2f}  {'[the five in unison] ' if ph['group'] else ''}{text}")
    peak = numpy.abs(track).max()
    track *= .9 / peak if peak > .9 else 1
    soundfile.write(OUTD / f"idol-{shape}-{lang}-voice.wav", track, RATE)
    rows.sort()
    (OUTD / f"idol-{shape}-{lang}-captions.txt").write_text("\n".join(f"{a:.2f}\t{b:.2f}\t{t}" for a, b, t in rows) + "\n", encoding="utf-8")
    # The mix: the original's own instrumental (Demucs no-vocals stem), her voice at the balance the original has between its voice and its music.
    bed = cf.read_audio(I / f"idol-{shape}-bed.wav")
    orig_v = cf.read_audio(I / f"idol-{shape}-vocals.wav")
    n = 90 * RATE
    bed = numpy.pad(bed, (0, max(0, n - len(bed))))[:n]
    orig_v = numpy.pad(orig_v, (0, max(0, n - len(orig_v))))[:n]
    act_o = numpy.abs(orig_v) > .02 * numpy.abs(orig_v).max()
    ratio_orig = float(numpy.sqrt((orig_v[act_o] ** 2).mean()) / (numpy.sqrt((bed[act_o] ** 2).mean()) + 1e-9))
    act = numpy.abs(track) > .02
    ours = track * (float(numpy.sqrt((bed[act] ** 2).mean())) * ratio_orig / float(numpy.sqrt((track[act] ** 2).mean())))
    mix = bed + ours
    import pyloudnorm
    gain = 10 ** ((-16.0 - pyloudnorm.Meter(RATE).integrated_loudness(mix.astype("float64"))) / 20)
    mix = (mix * gain).astype("float32")
    for _ in range(4):
        up = scipy.signal.resample_poly(mix, 4, 1)
        if 20 * numpy.log10(numpy.abs(up).max() + 1e-9) <= -1.0:
            break
        mix *= .93 / max(numpy.abs(mix).max(), 1e-9)
    soundfile.write(OUTD / f"idol-{shape}-{lang}-mix.wav", mix, RATE)
    (OUTD / f"idol-{shape}-{lang}-notes.txt").write_text("\n".join(notes) + f"\n\nvoice/music balance of the original: {20 * numpy.log10(ratio_orig):.1f} dB (vocals stem over Demucs bed, where it sings); ours laid at the same\n", encoding="utf-8")
    covered = sum(b - a for a, b, _ in rows)
    mouth = sy.on_mask(sy.envelope(str(I / f"idol-{shape}-vocals.wav")))
    hers = sy.on_mask(sy.envelope(str(OUTD / f"idol-{shape}-{lang}-voice.wav")), floor=.12)
    m = min(len(mouth), len(hers))
    both = (mouth[:m] & hers[:m]).sum() / sy.RATE
    print(f"{shape} {lang}: {len(rows)} lines, mouths {mouth[:m].sum() / sy.RATE:.1f} s, hit {both:.1f} s ({100 * both / (mouth[:m].sum() / sy.RATE):.0f}%), voice with no mouth {(hers[:m] & ~mouth[:m]).sum() / sy.RATE:.1f} s, balance {20 * numpy.log10(ratio_orig):.1f} dB")


def lyrics():
    rows = ["# Idol song v3: the original's own phrases, adapted", "",
            "For each original mouth phrase (9x16 timing): what the original sings (Whisper on the Demucs vocals stem, left as it heard it), then her line in each language.", ""]
    for shape in ("9x16", "16x9"):
        rows += [f"## {shape}", ""]
        for ph in phrases_for(shape):
            rows.append(f"{ph['a']:6.2f}-{ph['b']:6.2f} s  ({ph['syl']} syllables){'  [the five in unison]' if ph['group'] else ''}")
            rows.append(f"  ORIGINAL (transcribed): {ph['orig']}")
            for lg in LANGS:
                rows.append(f"  {lg.upper()}: {T[ph['key']][lg]}")
            rows.append("")
    rows.append("Original phrases the original sings with no usable words (breaths, a 1-syllable sound) get no line; the intro (before 8 s) has no singing, so no voice.")
    (D / "films-review" / "idol-v3-lyrics.txt").write_text("\n".join(rows), encoding="utf-8")
    print("lyrics written")


if __name__ == "__main__":
    cmd = sys.argv[1]
    {"record": lambda: record(sys.argv[2]), "build": lambda: build(sys.argv[2], sys.argv[3]), "lyrics": lyrics}[cmd]()
