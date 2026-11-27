"""
The voices for the shots where all five sing together, and for the answers of the crowd, of the idol song.

    python tools/idol_group.py            records what is missing (kept under dist/cutscenes/idol/group, never recorded twice)

  * For each line that is sung by the five (every form of it, in four languages): Chikuwa, Rune, Illya and Tsubakieh each say it in their own
    Inworld voice (the voices of tools/make_run_voice.py), to be laid together with Halgakos's own, so that it is the five of them in unison.
  * For each answer of the crowd (Sloppy~, and the names): Halgakos's own voice saying it once as a shout, and once whispered (her voice, asked to
    whisper), to be laid together as a deliberate double: the whisper softly under the shout, a little after it.
"""
import concurrent.futures
import hashlib
import importlib.util
import json
import pathlib
import sys
import time

import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import idol_variants as iv  # noqa: E402

GROUP = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\idol\group")
LANGS = ("en", "ja", "ko", "zh")
SUNG_BY_FIVE = (4, 5, 6, 7, 9, 15, 16, 17, 18, 34, 35, 36, 38, 39, 40, 41)
CROWD = (11, 13, 21, 24, 27, 30, 33)
OTHERS = ("C", "R", "I", "T")


def module():
    spec = importlib.util.spec_from_file_location("voice", str(ROOT / "tools" / "make_run_voice.py"))
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def key_of(text):
    return hashlib.sha1(text.encode("utf-8")).hexdigest()[:10]


def path_of(who, lang, text, kind=""):
    return GROUP / f"{who}-{lang}-{key_of(text)}{kind}.wav"


def specs():
    spec = json.loads((ROOT / "tools" / "cutscenes" / "idol-variants-mc.json").read_text(encoding="utf-8"))
    return spec


def texts(spec, lang, i):
    """Every form of line i in a language: the line, its short form, its single word."""
    own = spec["lines"][i][2] if lang == "en" else spec["tongues"][lang]["lines"][i]
    forms = [own]
    if i in iv.V:
        forms += list(iv.V[i][lang])
    return forms


def says(spec, lang, text):
    table = spec.get("says", {}) if lang == "en" else spec["tongues"][lang].get("says", {})
    for shown, read in table.items():
        text = text.replace(shown, read)
    return text


def main():
    GROUP.mkdir(parents=True, exist_ok=True)
    voice = module()
    key = voice.secret("INWORLD_API_KEY")
    spec = specs()
    jobs = []
    for lang in LANGS:
        for i in SUNG_BY_FIVE:
            for text in texts(spec, lang, i):
                for who in OTHERS:
                    jobs.append((who, lang, text, ""))
        for i in CROWD:
            text = texts(spec, lang, i)[0]
            jobs += [("H", lang, text, "-n"), ("H", lang, text, "-w")]
    todo = [j for j in dict.fromkeys(jobs) if not path_of(*j[:3], j[3]).exists()]
    print(len(jobs), "voices,", len(todo), "to record", flush=True)

    def record(job):
        who, lang, text, kind = job
        sent = says(spec, lang, text)
        if kind == "-w":
            scene = "Halgakos whispers this one word very softly, breathy and hushed, like a secret said close to the microphone, a clear whisper with no voice in it. "
        elif kind == "-n":
            scene = voice.MANNER.get("H", "") + "She shouts this one word, bright and loud and short, as the answer of a crowd at her own concert. "
        else:
            scene = voice.MANNER.get(who, "") + "She is singing along, cheerful and playful, with four other girls at a concert, saying the line together with them in unison, in time. "
        for attempt in range(4):
            try:
                sound, _ = voice.speak(key, sent, who, scene, None if lang == "en" else lang)
                soundfile.write(str(path_of(who, lang, text, kind)), voice.finished(sound, voice.VOICES[who][2]), voice.RATE_OUT)
                return True
            except Exception as error:  # noqa: BLE001
                time.sleep(3 + attempt * 4)
                last = error
        print("failed", who, lang, text, last, flush=True)
        return False

    done = 0
    with concurrent.futures.ThreadPoolExecutor(4) as pool:
        for ok in pool.map(record, todo):
            done += 1
            if done % 50 == 0:
                print(done, "of", len(todo), flush=True)
    print("recorded", done, flush=True)


if __name__ == "__main__":
    main()
