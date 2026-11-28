"""
Clips v4: the voice lines written again from the transcript of what the generated mouths say (dist/cutscenes/clips/transcripts), one line per phrase of
the original, in every language, recorded fresh in the speaker's own voice, laid on the real onsets of the original voice.

    python tools/clips_v4.py record            records what is missing: dist/cutscenes/clips/<clip>/voice/v4-<lang>-<k>.wav
    python tools/clips_v4.py                   (used by clips_v3.build for the clips in PHRASES)

A phrase: who speaks, the span of the original voice (a, b: the Demucs vocals stem's own onset and end), and the line in four languages. Status says what was done to the line
that was there: KEPT (the original's words, cleaned: the garbles mended), REWRITTEN (old text -> new, and why), REMOVED (listed in REMOVED with the reason).
English is laid word by word (our words by their share of the phrase's syllables onto the original's words, within x0.8 to x1.25); the other languages phrase by phrase:
the first sound on the original's onset, the phrase stretched into its span by no more than x1.25.
"""
import pathlib
import re
import sys
import time

sys.path.insert(0, str(pathlib.Path(__file__).parent))

ROOT = pathlib.Path(__file__).resolve().parent.parent
CLIPS = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\clips")

S = "Sloppy~\u2661"


def P(who, a, b, en, ja, ko, zh, status, why=""):
    return dict(who=who, a=a, b=b, text=dict(en=en, ja=ja, ko=ko, zh=zh), status=status, why=why)


PHRASES = {
    "03": [
        P("H", 1.31, 2.49, "Here we go.", "いくよ。", "간다.", "来了。", "REWRITTEN", "was 'A heavy book.': it only named what the picture shows; her mouth moves 1.3-2.5 s, so it says what she would say"),
        P("H", 5.0, 6.60, "Knowledge hurts, " + S, "知識は痛いわ、スロッピー\u2661", "지식은 아파, 슬로피\u2661", "知识会痛，Sloppy\u2661", "KEPT", "the original's own words and mouth (4.64-6.6 s); she is turned away until 5.0 s, so it begins where her face is back"),
    ],
    "04": [
        P("R", 1.0, 2.17, "My ink hurts!", "インクが痛い！", "내 잉크 아파!", "墨水好痛！", "REWRITTEN", "the original's 'My ink gets hurt!' one syllable shorter so that it fits the 1.2 s her mouth is open; Rune is crying"),
        P("R", 2.49, 4.11, "Not you, Halgakos!", "ハルガコスさまじゃない！", "할가코스 님은 아니야!", "不是你，哈尔加科斯！", "KEPT", "the original's 'Not you, Helgocco's name!' with the name mended; 2.5-4.1 s"),
    ],
    "05": [
        P("H", 4.80, 5.45, "Hey!", "ちょっと！", "야!", "喂！", "REWRITTEN", "was 'Careful!': 0.5 s of mouth cannot hold it; wide-eyed alarm"),
        P("H", 5.56, 7.15, "Her hearts turned my arrow around!", "ハートが矢を曲げたの！", "하트가 화살을 돌렸어!", "爱心把我的箭掉头了！", "REWRITTEN", "the original's 'Her heart's turned my arrow's round' mended into a sentence; her face goes from alarm to clenched teeth"),
        P("H", 7.30, 7.95, "Sloppy!", "スロッピー！", "슬로피!", "Sloppy！", "KEPT", "outraged, teeth bared: the catchphrase said angrily, as the picture has her"),
    ],
    "07": [
        P("T", 0.24, 1.55, "Found you. Normally.", "見つけた。普通に。", "찾았다. 평범하게.", "找到你了。很正常。", "KEPT", "the original's words, 0.24-1.55 s"),
        P("T", 1.84, 3.10, "I will bat you across.", "打って向こうへ飛ばします。", "쳐서 넘겨 드릴게요.", "我把你打过去。", "KEPT", "the original's 'in a cross' mended to 'across'"),
        P("H", 5.48, 7.72, "Nice swing. You missed me, " + S, "いい振りね。外れたわよ、スロッピー\u2661", "좋은 스윙이야. 빗나갔네, 슬로피\u2661", "挥得不错。可惜没打中，Sloppy\u2661", "REWRITTEN", "was 'Jump on her after she swings Sloppy': it narrated her own action (a prompt leak); she is smirking and pointing at Tsubakieh"),
    ],
    "09": [
        P("H", 0.67, 1.66, "Watch this!", "見てなさい！", "잘 봐!", "看好了！", "REWRITTEN", "was 'An ink gun.': it named what we can see; she is grinning with the gun raised"),
        P("H", 1.91, 4.33, "Oh, this is going to get messy, " + S, "あら、これは散らかるわよ、スロッピー\u2661", "어머, 이거 엉망 되겠는데, 슬로피\u2661", "哎呀，这下要乱套了，Sloppy\u2661", "KEPT", "the original's words, 1.9-4.3 s, delighted"),
    ],
    "10": [
        P("H", 3.55, 6.53, "Now it's a fair fight, " + S, "さあ、対等な勝負よ、スロッピー\u2661", "자, 공평한 승부야, 슬로피\u2661", "好，这才公平，Sloppy\u2661", "KEPT", "the original's words as one line, 3.55-6.5 s (its first word 'Now' is only 0.5 s of mouth); the smirk opens into a grin"),
    ],
    "11": [
        P("R", 1.20, 2.47, "I'll draw the lines!", "私が線を引く！", "내가 선을 그을게!", "我来画线！", "KEPT", "the original's words (the 'I'll to' garble mended); Rune weeping, mouth open"),
        P("C", 3.85, 5.18, "I'll bake him flat!", "ぺちゃんこに焼いてやる！", "납작하게 구워 줄게!", "我来把它烤扁！", "KEPT", "Chikuwa's, from the original 3.99-5.18 s"),
    ],
    "12": [
        P("H", 0.38, 3.07, "The pen is mightier than the sword, " + S, "ペンは剣より強いのよ、スロッピー\u2661", "펜은 칼보다 강해, 슬로피\u2661", "笔比剑更强，Sloppy\u2661", "KEPT", "the original's words (0.4-3.1 s) as one sentence"),
    ],
    "13": [
        P("M", 0.63, 2.18, "Friends are a burden.", "友達は重荷。", "친구는 짐.", "朋友是负担。", "KEPT", "the Mawang's, original 0.6-2.2 s, her mouth open"),
        P("H", 4.37, 7.05, "A burden shared weighs half, " + S, "分け合えば半分の重さよ、スロッピー\u2661", "나누면 절반만 무거워, 슬로피\u2661", "分担就只有一半重，Sloppy\u2661", "KEPT", "Halgakos's answer, original 4.4-7.1 s"),
    ],
    "14": [
        P("F", 2.73, 4.93, "Every tongue will sound like mine.", "すべての言葉は私の声になる。", "모든 말은 내 목소리가 된다.", "所有语言都会像我。", "KEPT", "the dragon's, original 2.7-4.9 s, her mouth open at 4.2 s"),
        P("H", 5.16, 7.34, "Just a mother who won't let go, " + S, "手を離せない母よ、スロッピー\u2661", "못 놓는 엄마야, 슬로피\u2661", "放不开手的妈妈，Sloppy\u2661", "KEPT", "Halgakos's, original 5.2-7.3 s"),
    ],
    "15": [
        P("T", 1.44, 3.62, "I will hit it for you. Normally.", "私が打ちます。普通に。", "제가 쳐 드릴게요. 평범하게.", "我来帮你打。很正常。", "KEPT", "Tsubakieh's, original 1.4-3.6 s, smiling, mouth open"),
        P("H", 5.60, 6.23, "Whoa!", "うわっ！", "으악!", "哇！", "REWRITTEN", "was 'You hit it INTO me Sloppy': her face is behind the book until 5.5 s, so no words sit there; the one shocked cry sits on her opened mouth"),
    ],
    "16": [
        P("M", 1.56, 3.41, "This time I walk with you.", "今度は、私も一緒に行く。", "이번엔 나도 같이 가.", "这次我和你一起走。", "KEPT", "the Mawang's, original 1.6-3.4 s"),
        P("H", 6.47, 7.99, "Don't slow me down, " + S, "遅れないでよ、スロッピー\u2661", "느려지지 마, 슬로피\u2661", "别拖我后腿，Sloppy\u2661", "KEPT", "Halgakos's, original 6.5-8.0 s, her mouth open at 6.8 s"),
    ],
    "17": [
        P("I", 0.67, 2.16, "Heroine: Illya.", "ヒロインは、イリヤ。", "주인공은 일리야.", "女主角：伊莉雅。", "KEPT", "the original's words ('HeroBind' mended), mouth open 0.6-2.2 s"),
        P("I", 2.71, 4.55, "Supporting cast: none.", "脇役は、いない。", "조연은 없어.", "配角：没有。", "KEPT", "the original's words, 2.7-4.6 s"),
    ],
    "18": [
        P("H", 1.46, 3.45, "Nothing thrown gets through it,", "投げたものは何も通らない、", "던진 건 못 뚫어,", "扔过去的东西穿不透，", "KEPT", "the original's words, 1.5-3.5 s"),
        P("H", 4.70, 6.88, "so I'll read it to death,", "だから読み尽くしてやる、", "그러니 읽어서 끝내 줄게,", "那我就把它读死，", "KEPT", "the original's words, 4.7-6.9 s"),
        P("H", 7.42, 8.57, "Read on, " + S, "読んで、スロッピー\u2661", "읽어, 슬로피\u2661", "读吧，Sloppy\u2661", "REWRITTEN", "the original's lone 'sloppy!' held for 1.2 s is made a short call that fills it"),
    ],
    "21": [
        P("H", 2.98, 4.20, "You're not alone.", "ひとりじゃない。", "혼자가 아니야.", "你不是一个人。", "REWRITTEN", "was 'That's her line': it made no sense to the player; Halgakos, grinning, is saying it to the Mawang"),
        P("H", 4.53, 6.19, "Not alone, not alone.", "ひとりじゃない、ひとりじゃない。", "혼자가 아니야, 아니야.", "不是一个人，不是。", "REWRITTEN", "was 'Not yours, not yours.': the same beat said as the answer to the Mawang"),
        P("H", 6.83, 8.93, "I'm bringing you home, " + S, "連れて帰るわよ、スロッピー\u2661", "데려갈 거야, 슬로피\u2661", "我带你回家，Sloppy\u2661", "KEPT", "the original's words, 6.8-8.9 s"),
    ],
    "25": [
        P("H", 1.09, 3.47, "Everyone, together, " + S, "みんなで、せーの、スロッピー\u2661", "다 같이, 슬로피\u2661", "大家一起，Sloppy\u2661", "KEPT", "the original's words, 1.1-3.5 s"),
    ],
    "31": [
        P("H", 5.03, 6.71, "That was my mint ice cream.", "それは私のミントアイスよ。", "그건 내 민트 아이스크림이야.", "那是我的薄荷冰淇淋。", "KEPT", "the retake's own words, 5.0-6.7 s, shouting with fists clenched (the cone lies splatted on the ground, hands empty)"),
        P("H", 7.16, 8.01, "You'll pay!", "覚悟しなさい！", "각오해!", "你要付出代价！", "KEPT", "the retake's own words, 7.2-8.0 s, teeth bared, just before the beam"),
    ],
    "33": [
        P("T", 2.18, 3.36, "Normal number three!", "ノーマル三号！", "노멀 3호!", "三号普通棒！", "KEPT", "her mouth opens in a shout at 2.4 s (checked in the frames), original 2.2-3.4 s"),
    ],
}

REMOVED = {
    "15": [("You hit it INTO me (4.27-5.45 s)", "Halgakos's face is behind the book: no mouth to sit on")],
    "16": [("Back to back (5.13-5.93 s)", "narrates the shot and no mouth is speaking it")],
    "33": [("Swinging! (3.66-4.14 s)", "Tsubakieh's face is covered by the purple magic then: no mouth, and it narrates her swing")],
    "07": [("Jump on her after she swings (5.0-7.4 s original text)", "narrated her own action; replaced, see above")],
}


def record():
    import soundfile
    import clips_data as data
    import make_run_voice as voice
    key = voice.secret("INWORLD_API_KEY")
    for clip in data.CLIPS:
        n = clip["id"][:2]
        for k, ph in enumerate(PHRASES.get(n, [])):
            who = ph["who"]
            vk = "f" if who == "F" else who
            for lang, text in ph["text"].items():
                target = CLIPS / clip["id"] / "voice" / f"v4-{lang}-{k}.wav"
                if target.exists():
                    continue
                scene = voice.MANNER.get(vk, "") + "She is in the middle of a fast fight in a platformer; " + data.SHEETS.get(who, ("", who))[1].split(" (")[0] + " says this line, in character, with effort and attitude. "
                if who == "H":
                    scene = "She is talking to the player who controls her in a platform game, whom she calls Sloppy. She is in an action scene, smug and teasing, a little breathless. "
                for attempt in range(4):
                    try:
                        sound, _ = voice.speak(key, text, vk if vk in voice.VOICES else "H", scene, None if lang == "en" else lang)
                        sound = voice.finished(sound, voice.VOICES[vk][2])
                        soundfile.write(str(target), sound, voice.RATE_OUT)
                        print("recorded", clip["id"], lang, k, round(len(sound) / voice.RATE_OUT, 2), flush=True)
                        break
                    except Exception as error:  # noqa: BLE001
                        print("retry", n, lang, k, str(error)[:80])
                        time.sleep(3 + attempt * 4)


def refit(rounds=4):
    """Each recording is said again at the pace its mouth has: the speaking rate scaled by (its length / the mouth's) each time, the take nearest the mouth's length kept."""
    import math
    import soundfile
    import clips_data as data
    import make_run_voice as voice
    key = voice.secret("INWORLD_API_KEY")
    for clip in data.CLIPS:
        n = clip["id"][:2]
        for k, ph in enumerate(PHRASES.get(n, [])):
            who = ph["who"]
            vk = "f" if who == "F" else who
            vk = vk if vk in voice.VOICES else "H"
            base = voice.VOICES[vk]
            span = ph["b"] - ph["a"]
            for lang, text in ph["text"].items():
                target = CLIPS / clip["id"] / "voice" / f"v4-{lang}-{k}.wav"
                x, r = soundfile.read(str(target))
                best, best_q, rate = x, len(x) / r / span, base[1]
                q = best_q
                for rd in range(rounds):
                    if .9 <= best_q <= 1.1:
                        break
                    rate = min(1.5, max(.55, rate * min(1.5, max(.65, q))))
                    voice.VOICES[vk] = (base[0], rate, base[2])
                    scene = voice.MANNER.get(vk, "") + "She is in the middle of a fast fight in a platformer; says this line in character, with effort and attitude, " + ("quickly and crisply. " if q > 1 else "unhurried, drawing it out. ")
                    if who == "H":
                        scene = "She is talking to the player who controls her in a platform game, whom she calls Sloppy. She is in an action scene, smug and teasing, a little breathless; " + ("quick and crisp. " if q > 1 else "unhurried. ")
                    try:
                        sound, _ = voice.speak(key, text, vk, scene, None if lang == "en" else lang)
                        sound = voice.finished(sound, base[2])
                        q = len(sound) / voice.RATE_OUT / span
                        if abs(math.log(q)) < abs(math.log(best_q)):
                            best, best_q = sound, q
                        print("refit", clip["id"], lang, k, f"take {rd}: x{q:.2f} of the mouth (rate {rate:.2f}), best x{best_q:.2f}", flush=True)
                    except Exception as error:  # noqa: BLE001
                        print("retry", n, lang, k, str(error)[:80])
                    voice.VOICES[vk] = base
                if best is not x:
                    soundfile.write(str(target), best, voice.RATE_OUT)


# ---------------------------------------------------------------------------------------------- placing

def syl(text):
    text = re.sub(r"[~\u2661\u2014]", " ", text)
    total = 0
    for w in re.findall(r"[A-Za-z']+", text):
        w = w.lower()
        k = len(re.findall(r"[aeiouy]+", w))
        if w.endswith("e") and not w.endswith(("le", "ee")) and k > 1:
            k -= 1
        total += max(1, k)
    return total


def trim(x, rate):
    import numpy
    hop = rate // 200
    env = numpy.sqrt((x[: len(x) // hop * hop].reshape(-1, hop) ** 2).mean(axis=1))
    loud = numpy.where(env > .06 * env.max())[0]
    if not len(loud):
        return x
    return x[max(0, (loud[0] - 1) * hop):(loud[-1] + 2) * hop]


_model = None


def words_of(sound, rate):
    """Our own recording's words and their times (faster-whisper), English only."""
    global _model
    import tempfile
    import soundfile
    from faster_whisper import WhisperModel
    if _model is None:
        _model = WhisperModel("small", device="cpu", compute_type="int8")
    tmp = pathlib.Path(tempfile.gettempdir()) / f"v4_{abs(hash(sound.tobytes())) % 10**8}.wav"
    soundfile.write(str(tmp), sound, rate)
    segs, _ = _model.transcribe(str(tmp), word_timestamps=True, language="en", vad_filter=False, beam_size=3, condition_on_previous_text=False)
    out = [(w.word.strip(), w.start, w.end) for s in segs for w in (s.words or [])]
    tmp.unlink(missing_ok=True)
    return out


def place(clip, lang, voice, rate, cf, orig_words):
    """Lays every phrase of the clip in `voice`; returns [(start, end, k, how)]."""
    import numpy
    import soundfile
    import scipy.signal
    n = clip["id"][:2]
    laid = []
    for k, ph in enumerate(PHRASES[n]):
        x, r = soundfile.read(str(CLIPS / clip["id"] / "voice" / f"v4-{lang}-{k}.wav"))
        x = scipy.signal.resample_poly(x.mean(axis=1) if x.ndim > 1 else x, rate, r).astype("float32")
        x = trim(x, rate)
        a, b = ph["a"], ph["b"]
        span = b - a
        nat = len(x) / rate
        piece, how = None, ""
        if lang == "en":
            ow = [w for w in orig_words if w["a"] < b - .05 and w["b"] > a + .05]
            if ow:
                ow[0] = dict(ow[0], a=max(a, ow[0]["a"]))
                ow = [dict(w, a=max(a, min(w["a"], b - .05)), b=min(b, w["b"])) for w in ow]
            ours = words_of(x, rate)
            if ow and ours:
                knots = knots_by_syllables(ours, ow, a, b, nat)
                segs, ok = [], True
                for (s0, t0), (s1, t1) in zip(knots[:-1], knots[1:]):
                    q = (s1 - s0) / max(.04, t1 - t0)
                    if not .8 <= q <= 1.25:
                        ok = False
                        break
                if ok:
                    for (s0, t0), (s1, t1) in zip(knots[:-1], knots[1:]):
                        seg = x[int(s0 * rate):int(s1 * rate)]
                        q = (s1 - s0) / max(.04, t1 - t0)
                        segs.append(cf.stretch(seg, q, rate) if abs(q - 1) > .03 else seg)
                    piece = numpy.concatenate(segs)
                    how = "word by word on the original's words: " + ", ".join(f"{s:.2f}->{t:.2f}" for s, t in knots[:5])
        if piece is None:
            final = min(max(span, nat / 1.35), nat / .8)
            q = nat / final
            piece = cf.stretch(x, q, rate) if abs(q - 1) > .02 else x
            how = f"phrase on the original's onset {a:.2f}, x{q:.2f} into {span:.2f} s"
        piece = cf.edged(piece)
        start = a
        s = int(start * rate)
        voice[s:s + len(piece)] += piece[: len(voice) - s]
        laid.append((start, start + len(piece) / rate, k, how))
    return laid


def knots_by_syllables(ours, orig, a, b, nat):
    """[(our time, target time)]: each of our words, at its share of our syllables, goes where the original has that share of its syllables."""
    import numpy
    o_syl = [max(1, syl(w["w"])) for w in orig]
    cum_o = numpy.concatenate([[0], numpy.cumsum(o_syl)])
    pts_t, pts_f = [], []
    for j, w in enumerate(orig):
        pts_t += [w["a"], w["b"]]
        pts_f += [cum_o[j] / cum_o[-1], cum_o[j + 1] / cum_o[-1]]
    pts_t[0] = a
    pts_t[-1] = b
    pts_f_arr = numpy.array(pts_f)
    for i in range(1, len(pts_f_arr)):
        pts_f_arr[i] = max(pts_f_arr[i], pts_f_arr[i - 1] + 1e-6)
    pts_t = numpy.maximum.accumulate(numpy.array(pts_t))
    s_syl = [max(1, syl(w[0])) for w in ours]
    cum_s = numpy.concatenate([[0], numpy.cumsum(s_syl)])
    knots = [(0.0, a)]
    first = ours[0][1]
    for i, (_, s0, s1) in enumerate(ours):
        if i == 0:
            continue
        f = cum_s[i] / cum_s[-1]
        t = float(numpy.interp(f, pts_f_arr, pts_t))
        knots.append((float(s0), t))
    knots.append((nat, b))
    # strictly increasing
    out = [knots[0]]
    for s0, t0 in knots[1:]:
        if s0 > out[-1][0] + .03 and t0 > out[-1][1] + .03:
            out.append((s0, t0))
    return out


if __name__ == "__main__":
    if sys.argv[1:] == ["record"]:
        record()
    elif sys.argv[1:] == ["refit"]:
        refit()
