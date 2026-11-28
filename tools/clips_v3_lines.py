"""New, shorter lines for the v3 clips (JA/KO/ZH where the v2 line did not fit the mouth). Recorded in the same voices into <clip>/voice/v3-<lang>-<i>.wav.
    python tools/clips_v3_lines.py        records the ones missing
"""
import pathlib
import sys
import time

sys.path.insert(0, str(pathlib.Path(__file__).parent))

# (clip number, line index) -> {lang: text}
NEW = {
    ("02", 0): {"ko": "모두 빵 받아라! 팍팍!", "zh": "人人有面包！来啦！"},
    ("04", 0): {"ja": "インクが傷つく！ハルガコスさまじゃない！", "ko": "내 잉크가 다쳐! 할가코스 님은 아니야!", "zh": "墨水会受伤！不是你，哈尔加科斯！"},
    ("05", 0): {"ja": "ハートで矢が曲がるわ、スロッピー♡", "ko": "하트가 화살을 돌려, 슬로피♡", "zh": "爱心让箭拐弯，Sloppy♡"},
    ("07", 0): {"ko": "찾았다. 평범하게. 칠게요."},
    ("07", 1): {"ja": "振ったら飛び乗って、スロッピー♡", "ko": "휘두르면 뛰어올라, 슬로피♡", "zh": "她一挥棒就跳，Sloppy♡"},
    ("09", 0): {"zh": "墨水枪。哎呀，要乱套了，Sloppy♡"},
    ("11", 0): {"ja": "私が線を引くよ……うわぁん……", "ko": "내가 선 그을게… 으앙…", "zh": "我来画线……呜哇……"},
    ("13", 0): {"ko": "친구는 짐."},
    ("14", 0): {"ja": "すべての言葉は私の声になる。", "ko": "모든 말은 내 목소리가 된다.", "zh": "所有语言都会像我。"},
    ("14", 1): {"ja": "手を離せない母よ、スロッピー♡", "ko": "못 놓는 엄마야, 슬로피♡", "zh": "放不开手的妈妈，Sloppy♡"},
    ("15", 0): {"ja": "打ちます。普通に。"},
    ("15", 1): {"ja": "私に打ったの、スロッピー♡", "zh": "打到我了，Sloppy♡"},
    ("16", 0): {"ja": "今度は一緒に行く。", "ko": "이번엔 같이 가.", "zh": "这次一起走。"},
    ("26", 0): {"en": "Eek!", "ja": "ひっ！", "ko": "헉!", "zh": "呀！"},
    ("33", 0): {"ko": "노멀 3호. 휘둘러요."},
}


def text_of(clip, lang, i):
    from clips_data import LANGS as _l  # noqa: F401
    new = NEW.get((clip["id"][:2], i), {}).get(lang)
    return new


def main():
    import soundfile
    import clips_data as data
    import make_run_voice as voice
    key = voice.secret("INWORLD_API_KEY")
    root = pathlib.Path("dist/cutscenes/clips")
    for clip in data.CLIPS:
        n = clip["id"][:2]
        for (m, i), langs in NEW.items():
            if m != n:
                continue
            who = clip["lines"][i][0]
            vk = "f" if who == "F" else who
            for lang, text in langs.items():
                target = root / clip["id"] / "voice" / f"v3-{lang}-{i}.wav"
                if target.exists():
                    continue
                scene = voice.MANNER.get(vk, "") + "She is in the middle of a fast fight in a platformer; " + data.SHEETS.get(who, ("", who))[1].split(" (")[0] + " says this line, in character, with effort and attitude. "
                if who == "H":
                    scene = "She is talking to the player who controls her in a platform game, whom she calls Sloppy. She is in an action scene, smug and teasing, a little breathless. "
                for attempt in range(4):
                    try:
                        sound, _ = voice.speak(key, text, vk if vk in voice.VOICES else "H", scene, lang)
                        sound = voice.finished(sound, voice.VOICES[vk][2])
                        soundfile.write(str(target), sound, voice.RATE_OUT)
                        print("recorded", clip["id"], lang, i, len(sound) / voice.RATE_OUT, flush=True)
                        break
                    except Exception as error:  # noqa: BLE001
                        print("retry", n, lang, i, str(error)[:80])
                        time.sleep(3 + attempt * 4)


if __name__ == "__main__":
    main()
