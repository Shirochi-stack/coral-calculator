"""
The idol song, v4: Halgakos's lines written again against the phrases the film's own singers sing (their Demucs vocals stem, transcribed:
dist/cutscenes/idol/transcripts/idol-SHAPE.json), one line per mouth phrase with the syllables the phrase has, every "Sloppy~" inside a whole line,
recorded fresh at the pace of its mouth, and laid on the phrase's real onset. Where the five sing together (G_SHOTS of idol_retime.py) the line is the five
in unison and Halgakos's whispered double lies softly under it; her calls (24-30 s) are on her own mouths. The music is the instrumental, as it was.

    python tools/idol_v4.py record SHAPE      records what is missing (dist/cutscenes/idol/v4), at the mouth's rate
    python tools/idol_v4.py build SHAPE LANG  writes dist/cutscenes/idol/retimed/idol-SHAPE-LANG-{voice,mix}.wav, -captions.txt, -v4-notes.txt
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

D = ir.D
I = D / "idol"
V4 = I / "v4"
V4.mkdir(exist_ok=True)
RATE = 48000
LANGS = ("en", "ja", "ko", "zh")
SL = {"en": "Sloppy~♡", "ja": "スロッピー♡", "ko": "슬로피♡", "zh": "Sloppy♡"}
SQ = {"en": "Squish", "ja": "スクイッシュ", "ko": "스퀴시", "zh": "啪叽"}
NAME = {
    "H": {"en": "Halgakos", "ja": "ハルガコス", "ko": "할가코스", "zh": "哈尔加科斯"},
    "C": {"en": "Chikuwa", "ja": "ちくわ", "ko": "치쿠와", "zh": "竹轮"},
    "R": {"en": "Rune", "ja": "ルーン", "ko": "루네", "zh": "露恩"},
    "I": {"en": "Illya", "ja": "イリヤ", "ko": "일리야", "zh": "伊莉雅"},
    "T": {"en": "Tsubakieh", "ja": "ツバキエ", "ko": "츠바키에", "zh": "椿"},
}


def L(en, ja, ko, zh):
    return {"en": en, "ja": ja, "ko": ko, "zh": zh}


def sl(fmt):
    """A line with Sloppy and Squish and names filled in per language."""
    return {lg: fmt[lg].format(SL=SL[lg], SQ=SQ[lg], **{k: v[lg] for k, v in NAME.items()}) for lg in LANGS}


# The slots: where in the song each line is (the 9x16 transcript's mouth, seconds), and what it says. A shape's phrase takes the slots that start in it.
SLOTS = [
    (8.18, sl(L("Lights up. You're staring. How cute. Five of us. Try to keep count.", "ライトアップ。見とれてるの？ かわいい。私たち五人。数え間違えないでね。", "조명 켜졌다. 넋 놓고 보네? 귀여워. 우리는 다섯. 세는 거 놓치지 마.", "灯亮了。看呆了？真可爱。我们五个人。别数错了。"))),
    (12.22, sl(L("Book's open. Sing it loud. I'll tell you if it's right.", "本が開いた。大きな声で歌って。合ってるか教えてあげる。", "책이 열렸어. 크게 노래해. 맞는지 내가 말해 줄게.", "书打开了。大声唱。对不对我会告诉你。"))),
    (17.39, sl(L("Hands up. Higher. Penlights up.", "手を上げて。もっと高く。ペンライトも。", "손 들어. 더 높이. 펜라이트도.", "举起手。再高点。荧光棒也举起来。"))),
    (19.40, sl(L("Call my name. Slowly. Call it right.", "私の名前を呼んで。ゆっくり。ちゃんと呼んで。", "내 이름을 불러 봐. 천천히. 제대로.", "叫我的名字。慢一点。要叫对。"))),
    (21.89, sl(L("Call my name. Call it right.", "私の名前を呼んで。ちゃんと呼んで。", "내 이름을 불러. 제대로 불러.", "叫我的名字。要叫对。"))),
    (24.94, sl(L("Hey, {SL}", "ねえ、{SL}", "야, {SL}", "喂，{SL}"))),
    (25.83, sl(L("Yes, {SL}", "そう、{SL}", "그래, {SL}", "对，{SL}"))),
    (26.93, sl(L("Up, {SL}", "もっと、{SL}", "위로, {SL}", "再来，{SL}"))),
    (27.87, sl(L("More, {SL}", "まだよ、{SL}", "더, {SL}", "还要，{SL}"))),
    (28.93, sl(L("Louder, zaako.", "もっと大きく、ざぁこ。", "더 크게, 허접.", "再大声点，杂鱼。"))),
    (30.60, sl(L("{SQ}, {SL} eyes up!", "{SQ}、{SL} 顔を上げて！", "{SQ}, {SL} 눈 들어!", "{SQ}，{SL} 抬起头！"))),
    (32.09, sl(L("{SQ}, {SL} five colors of light!", "{SQ}、{SL} 五色の光！", "{SQ}, {SL} 다섯 빛깔!", "{SQ}，{SL} 五色之光！"))),
    (34.08, sl(L("Miss one beat and I restart you.", "リズムを外したら最初からやり直し。", "박자 놓치면 처음부터 다시.", "跟丢一拍就重来。"))),
    (36.10, sl(L("{SQ}, {SL} up on your feet!", "{SQ}、{SL} 立ち上がって！", "{SQ}, {SL} 일어서!", "{SQ}，{SL} 站起来！"))),
    (38.10, sl(L("Pink. Center. Obviously. {H}, {SL}", "ピンク。センター。当然でしょ。{H}、{SL}", "핑크. 센터. 당연하지. {H}, {SL}", "粉色。中心。理所当然。{H}，{SL}"))),
    (41.60, sl(L("Red as a ribbon. Warm. Still not bread. {C}! {C}!", "赤いリボン。あったかい。まだパンじゃない。{C}！{C}！", "빨간 리본. 따뜻해. 그래도 빵은 아니야. {C}! {C}!", "红丝带。温暖。还是不是面包。{C}！{C}！"))),
    (46.18, sl(L("Blue. Crying. We can all see. {R}!", "青。泣き虫。みんな見てるよ。{R}！", "파랑. 울보. 다 보이거든. {R}!", "蓝色。爱哭。大家都看见了。{R}！"))),
    (48.92, sl(L("{R}!", "{R}！", "{R}!", "{R}！"))),
    (50.14, sl(L("Gold. In the spotlight. Lead. {I}! {I}!", "金。スポットライト。リード。{I}！{I}！", "금색. 스포트라이트. 리드. {I}! {I}!", "金色。聚光灯。领唱。{I}！{I}！"))),
    (54.27, sl(L("Purple. Calm. Swinging. {T}!", "紫。穏やか。振るよ。{T}！", "보라. 차분해. 휘둘러. {T}!", "紫色。冷静。挥棒。{T}！"))),
    (58.00, sl(L("{T}! Down the runway. Don't let go. Hand in hand.", "{T}！ランウェイを進んで。手を離さないで。手をつないで。", "{T}! 런웨이를 따라가. 놓지 마. 손을 잡고.", "{T}！沿着 T 台走。别松手。手拉手。"))),
    (64.12, sl(L("You're holding on like it's a favor. Not bad.", "お願いされたみたいにしがみついてる。悪くない。", "무슨 부탁이라도 받은 것처럼 매달리네. 나쁘지 않아.", "你抓得那么紧，像在帮我的忙。还不错。"))),
    (68.09, sl(L("Once more. Up. That was easy. {SQ}, {SL} encore!", "もう一回。上へ。簡単だったでしょ。{SQ}、{SL} アンコール！", "한 번 더. 위로. 쉬웠지. {SQ}, {SL} 앙코르!", "再来一次。起来。很简单吧。{SQ}，{SL} 安可！"))),
    (72.72, sl(L("Say it, {SL}", "言って、{SL}", "말해 봐, {SL}", "说吧，{SL}"))),
    (74.28, sl(L("{SQ}, {SL} say it!", "{SQ}、{SL} 言って！", "{SQ}, {SL} 말해 봐!", "{SQ}，{SL} 说出来！"))),
    (76.22, sl(L("{SQ}, {SL} say it! Jump, heart, bow.", "{SQ}、{SL} 言って！ジャンプ、ハート、お辞儀。", "{SQ}, {SL} 말해 봐! 점프, 하트, 인사.", "{SQ}，{SL} 说出来！跳、爱心、鞠躬。"))),
    (80.69, sl(L("{SQ}, {SL} don't stop!", "{SQ}、{SL} 止めないで！", "{SQ}, {SL} 멈추지 마!", "{SQ}，{SL} 别停！"))),
    (83.14, sl(L("Again!", "もう一回！", "다시!", "再来！"))),
    (84.35, sl(L("You're welcome.", "どういたしまして。", "천만에.", "不客气。"))),
    (86.01, sl(L("{SQ}!", "{SQ}！", "{SQ}!", "{SQ}！"))),
    (87.15, sl(L("My mint ice cream, {SL}", "私のミントアイス、{SL}", "내 민트 아이스크림, {SL}", "我的薄荷冰淇淋，{SL}"))),
]
INTRO_LINES = [(1.0, 2.9), (5.0, 6.9)]
INTRO_TEXT = sl(L("{SQ}, {SL}", "{SQ}、{SL}", "{SQ}, {SL}", "{SQ}，{SL}"))
MEMBERS = ("C", "R", "I", "T")


def phrases_for(shape):
    """The shape's mouth phrases, each with the slots that start in it (a slot is used once) and whether the five sing it."""
    tr = json.loads((I / "transcripts" / f"idol-{shape}.json").read_text(encoding="utf-8"))["phrases"]
    out, used = [], set()
    for p in tr:
        a, b = p["a"], p["b"]
        if b - a < .5 and not (24 <= a < 30.3):
            continue
        idx = [i for i, (t, _) in enumerate(SLOTS) if i not in used and a - .75 <= t <= b - .15]
        if not idx:
            # a mouth the earlier phrase left no slot for takes that phrase's last slot, if it had two or more
            if out and len(out[-1]["slots"]) >= 2 and a - SLOTS[out[-1]["slots"][-1]][0] < 2.0:
                idx = [out[-1]["slots"].pop()]
            else:
                continue
        used.update(idx)
        out.append(dict(a=a, b=b, slots=idx, syl=p["syllables"], orig=p["words"], group=ir.in_g((a + b) / 2) and not (24 <= a < 30.3)))
    return out


def text_of(ph, lang):
    t = " ".join(SLOTS[i][1][lang] for i in ph["slots"])
    return t.replace("  ", " ")


def key_of(*parts):
    return hashlib.sha1("|".join(str(x) for x in parts).encode("utf-8")).hexdigest()[:12]


def take(key, who, text, lang, scene, rate_mult, base_rate_override=None):
    import make_run_voice as voice
    vk = who
    base = voice.VOICES[vk]
    rate = base_rate_override if base_rate_override else min(1.5, max(.55, base[1] * rate_mult))
    name = f"{vk}@{rate:.3f}"
    voice.VOICES[name] = (base[0], rate, base[2])
    for attempt in range(4):
        try:
            sound, _ = voice.speak(key, text, name, scene, None if lang == "en" else lang)
            return voice.finished(sound, base[2]), rate
        except Exception as error:  # noqa: BLE001
            print("retry", str(error)[:70], flush=True)
            time.sleep(3 + attempt * 4)
    raise RuntimeError("no take")


SCENE_H = "She is the centre of an idol group on stage before a cheering hall, the group's MC over bouncy idol pop at 120 beats a minute. She talks on the beat in short dry phrases, smug, bratty and teasing, loud enough for a hall, "
SCENE_G = "She is singing along, cheerful and playful, with four other girls at a concert, saying the line together with them in unison, in time. "
SCENE_W = "Halgakos whispers this line very softly, breathy and hushed, like a secret said close to the microphone, a clear whisper with no voice in it. "


def make_one(key, shape, ph, lang, text):
    """Records, at the mouth's pace, the line of one phrase: Halgakos (the best of up to four takes), and for a group phrase the four others and her whisper at her rate."""
    import make_run_voice as voice
    span = ph["b"] - ph["a"]
    tag = key_of("H", lang, text, round(span, 1))
    path = V4 / f"H-{tag}.wav"
    meta = V4 / f"H-{tag}.json"
    if not path.exists():
        mult, best, best_q, best_rate = 1.0, None, None, 1.0
        for rd in range(4):
            scene = SCENE_H + ("quickly and crisply." if mult > 1 else "unhurried.")
            sound, rate = take(key, "H", text, lang, scene, mult)
            q = len(sound) / voice.RATE_OUT / span
            if best is None or abs(math.log(q)) < abs(math.log(best_q)):
                best, best_q, best_rate = sound, q, rate
            if .9 <= q <= 1.1:
                break
            mult = min(1.5, max(.65, (rate / voice.VOICES["H"][1]) * q))
        soundfile.write(str(path), best, voice.RATE_OUT)
        meta.write_text(json.dumps({"q": best_q, "rate": best_rate}), encoding="utf-8")
    rate = json.loads(meta.read_text(encoding="utf-8"))["rate"]
    if ph["group"]:
        for who in MEMBERS:
            p2 = V4 / f"{who}-{tag}.wav"
            if not p2.exists():
                sound, _ = take(key, who, text, lang, voice.MANNER.get(who, "") + SCENE_G, 1.0, base_rate_override=min(1.5, max(.55, voice.VOICES[who][1] * rate / voice.VOICES["H"][1])))
                soundfile.write(str(p2), sound, voice.RATE_OUT)
        pw = V4 / f"W-{tag}.wav"
        if not pw.exists():
            sound, _ = take(key, "H", text, lang, SCENE_W, 1.0, base_rate_override=rate)
            soundfile.write(str(pw), sound, voice.RATE_OUT)
    return tag


def record(shape):
    import make_run_voice as voice
    key = voice.secret("INWORLD_API_KEY")
    jobs = []
    for ph in phrases_for(shape):
        for lang in LANGS:
            jobs.append((ph, lang, text_of(ph, lang)))
    for t0, t1 in INTRO_LINES:
        for lang in LANGS:
            jobs.append((dict(a=t0, b=t1, slots=[], group=False), lang, INTRO_TEXT[lang]))
    with concurrent.futures.ThreadPoolExecutor(6) as pool:
        futs = [pool.submit(make_one, key, shape, ph, lang, text) for ph, lang, text in jobs]
        for f in futs:
            f.result()
    print("recorded", shape, len(jobs), flush=True)


# --------------------------------------------------------------------------------------------- build

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


def edged(x, ms=6):
    return cf.edged(x, ms) if hasattr(cf, "edged") else x


def build(shape, lang):
    notes, rows = [], []
    track = numpy.zeros(90 * RATE, dtype=numpy.float32)
    lane = []

    def put(piece, at, text, tag):
        s = int(at * RATE)
        track[s:s + len(piece)] += piece[: len(track) - s]
        rows.append((at, at + len(piece) / RATE, text))
        lane.append((at, at + len(piece) / RATE))
        return at + len(piece) / RATE

    jobs = [(p, text_of(p, lang)) for p in phrases_for(shape)] + [(dict(a=t0, b=t1, slots=[], group=False, intro=True), INTRO_TEXT[lang]) for t0, t1 in INTRO_LINES]
    tot = []
    for ph, text in jobs:
        span = ph["b"] - ph["a"]
        tag = key_of("H", lang, text, round(span, 1))
        x = load(V4 / f"H-{tag}.wav")
        nat = len(x) / RATE
        final = min(max(span, nat / 1.3), nat / .8)
        q = nat / final
        piece = cf.stretch(x, q, RATE) if abs(q - 1) > .02 else x
        piece = edged(piece)
        if ph["group"]:
            mixed = piece.copy()
            for who, (gain, off) in zip(MEMBERS, ((.5, .012), (.4, .02), (.5, .006), (.45, .016))):
                g = load(V4 / f"{who}-{tag}.wav")
                g = cf.stretch(g, min(1.9, max(.6, len(g) / len(piece))), RATE)
                g = edged(g[: len(piece)])
                o = int(off * RATE)
                mixed[o:o + len(g)] += gain * g[: max(0, len(mixed) - o)]
            peak = numpy.abs(mixed).max()
            mixed *= max(numpy.abs(piece).max(), .3) / peak if peak > 0 else 1
            w = load(V4 / f"W-{tag}.wav")
            w = cf.stretch(w, min(1.9, max(.6, len(w) / len(piece))), RATE)
            w = edged(w[: len(piece)])
            o = int(.035 * RATE)
            mixed[o:o + len(w)] += .5 * w[: max(0, len(mixed) - o)]
            piece = mixed
        end = put(piece, ph["a"], text, tag)
        tot.append((ph["a"], ph["b"], end, q))
        notes.append(f"{ph['a']:6.2f}-{ph['b']:6.2f} phrase ({ph.get('syl', '-')} syl: {ph.get('orig', 'intro: nobody sings')})  ours x{q:.2f} ends {end:.2f} {'[the five in unison + her whispered double]' if ph['group'] else ''}  {text}")
    # a mouth of the film that no line covers is given a call of hers ("Hey, Sloppy~") on it, as the earlier tracks did, so that no mouth moves in silence
    import idol_sync as sy
    mouth = sy.on_mask(sy.envelope(str(I / f"idol-{shape}-vocals.wav")))
    ftext = SLOTS[5][1][lang]
    fpath = V4 / f"H-{key_of('H', lang, ftext, round(.63, 1))}.wav"
    filled = []
    if fpath.exists():
        gaps = []
        for ma, mb in sy.spans(mouth):
            if mb - ma < .3 or ma < 7.5 or not (ir.in_h((ma + mb) / 2) or ir.in_g((ma + mb) / 2)):
                continue
            cur = ma
            for r0, r1 in sorted((r0, r1) for r0, r1, _ in rows if r1 > ma and r0 < mb):
                if r0 - cur >= .45:
                    gaps.append((cur, r0))
                cur = max(cur, r1 + .05)
            if mb - cur >= .45:
                gaps.append((cur, mb))
        for ga, gb in gaps:
            x = load(fpath)
            nat = len(x) / RATE
            q = min(1.3, max(.8, nat / (gb - ga)))
            piece = edged(cf.stretch(x, q, RATE) if abs(q - 1) > .02 else x)
            if len(piece) / RATE > (gb - ga) + .25:
                continue
            put(piece, ga, ftext, "fill")
            filled.append(f"{ga:.2f}-{gb:.2f}")
            notes.append(f"{ga:6.2f}-{gb:6.2f} mouth left uncovered: filled with her call  x{q:.2f}  {ftext}")
    peak = numpy.abs(track).max()
    track *= .9 / peak if peak > .9 else 1
    out_dir = I / "retimed"
    out_dir.mkdir(exist_ok=True)
    soundfile.write(out_dir / f"idol-{shape}-{lang}-voice.wav", track, RATE)
    rows.sort()
    (out_dir / f"idol-{shape}-{lang}-captions.txt").write_text("\n".join(f"{a:.2f}\t{b:.2f}\t{t}" for a, b, t in rows) + "\n", encoding="utf-8")
    # the mix, as idol_retime made it
    spec = json.loads((ROOT / "tools" / "cutscenes" / "idol-variants-mc.json").read_text(encoding="utf-8"))

    def fit90(x):
        return numpy.pad(x, (0, max(0, 90 * RATE - len(x))))[: 90 * RATE].astype("float32")

    def level(x, mask=None):
        x = x if mask is None else x[mask]
        return float(numpy.sqrt((x ** 2).mean()) + 1e-9)

    band = fit90(cf.read_audio(D / spec["band"]))
    bed = fit90(cf.read_audio(I / f"idol-{shape}-bed.wav"))
    act = numpy.abs(track) > .02
    env = numpy.convolve(act.astype(float), numpy.ones(RATE // 6) / (RATE // 6), mode="same")
    env = numpy.clip(numpy.convolve(numpy.clip(env * 1.5, 0, 1), numpy.ones(RATE // 8) / (RATE // 8), mode="same"), 0, 1)
    voice_part = track * (10 ** (-14.9 / 20) / level(track, act))
    music = band * (10 ** (-23.2 / 20) / level(band)) * (1 - .31 * env)
    effects = bed * (level(band) * (10 ** (-23.2 / 20) / level(band)) * .35 / level(bed)) * (1 - .31 * env)
    mix = voice_part + music + effects
    peak = numpy.abs(mix).max()
    mix *= .93 / peak if peak > .93 else 1
    soundfile.write(out_dir / f"idol-{shape}-{lang}-mix.wav", mix, RATE)
    (out_dir / f"idol-{shape}-{lang}-v4-notes.txt").write_text("\n".join(notes) + "\n", encoding="utf-8")
    over = [t for t in tot if t[2] - t[1] > .15]
    print(f"{shape} {lang}: {len(jobs)} lines, {len(over)} end more than 0.15 s past their mouth, stretch range x{min(t[3] for t in tot):.2f}-x{max(t[3] for t in tot):.2f}")


if __name__ == "__main__":
    cmd = sys.argv[1]
    if cmd == "record":
        record(sys.argv[2])
    elif cmd == "build":
        build(sys.argv[2], sys.argv[3])
