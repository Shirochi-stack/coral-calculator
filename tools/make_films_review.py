"""
Review copies of the films as they ship: each picture (res/raw), with its voice track in a language (run_film_*_h[_xx].m4a) and the captions of
assets/films in the same language burned in, 720p, under 30 MB. Written to dist/cutscenes/films-review/.
    python tools/make_films_review.py [MAIN]      MAIN is the app's src/main (default: this checkout's)
"""
import pathlib
import sys

import av
import numpy

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import clips_fit as cf  # noqa: E402

MAIN = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "app" / "src" / "main"
OUT = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\films-review")
LANGS = ("en", "ja", "ko", "zh")
CLIP_SAYS = {"en": "So you're the great evil. The Mother Tongue Sloppy~", "ja": "あんたが大いなる災いってわけ。マザー・タング Sloppy~", "ko": "네가 그 큰 재앙이구나. 마더 텅 Sloppy~", "zh": "原来你就是那个大灾祸。母语 Sloppy~"}


def captions(path):
    rows = []
    for row in path.read_text(encoding="utf-8").strip().split("\n"):
        a, b, text = row.split("\t", 2)
        rows.append((float(a), float(b), text.replace("\u2661", "").strip()))
    return rows


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    raw, films = MAIN.parent.parent.parent / "films" / "src" / "main" / "assets" / "films", MAIN / "assets" / "films"
    for name, picture, base, foot in (("prologue", "run_film_prologue.mp4", "prologue_h", None), ("idol-upright", "run_film_idol_tall.mp4", "idol_h", .78),
                                      ("idol-wide", "run_film_idol_wide.mp4", "idol_h", None)):
        if "--no-idol" in sys.argv and name != "prologue":
            continue
        for lang in LANGS:
            sound = base if lang == "en" else f"{base}_{lang}"
            audio = cf.read_audio(raw / f"run_film_{sound}.m4a")
            target = OUT / f"{name}-{lang}.mp4"
            cf.write_video(raw / picture, target, audio, captions=captions(films / f"{sound}.{lang}.txt"), bitrate=1_500_000, foot=foot, lang=lang)
            print(target.name, f"{target.stat().st_size / 1e6:.1f} MB", flush=True)
    # The clip of the Mother Tongue: its sound is in its file, in English; what is written under it is whole while it plays.
    clip = raw / "run_cut_mother.mp4"
    audio = cf.read_audio(clip)
    for lang in LANGS:
        target = OUT / f"mother-tongue-{lang}.mp4"
        cf.write_video(clip, target, audio, captions=[(0.0, 99.0, CLIP_SAYS[lang])], bitrate=1_500_000, foot=.85, lang=lang)
        print(target.name, f"{target.stat().st_size / 1e6:.1f} MB", flush=True)


if __name__ == "__main__":
    main()
