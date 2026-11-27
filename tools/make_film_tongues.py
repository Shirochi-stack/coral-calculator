"""
Makes the Japanese, Korean and Chinese tracks of the prologue and the idol song, and what is written under each in every language.

    python tools/make_film_tongues.py

From the spoken-lyrics specs (tools/cutscenes/prologue-mc.json, idol-mc.json), whose "tongues" hold the translated lines, and the tracks
tools/make_mc.py made (dist/cutscenes/.../OUT-ja.wav and so on):
  * res/raw/run_film_{prologue,idol}_h_{ja,ko,zh}.m4a: the track, AAC stereo 128 kb/s, as the English ones are;
  * assets/films/{prologue,idol}_h[_{ja,ko,zh}].{en,ja,ko,zh}.txt: each line's begin, end and words, with tabs between, in the language
    named after the dot. Where the track is Halgakos's English one, the lines are placed as the spec places them.
Needs av, numpy, soundfile.
"""
import json
import pathlib

import av
import numpy
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
DIST = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes")
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
FILMS = ROOT / "app" / "src" / "main" / "assets" / "films"
SPECS = {"prologue": ("prologue-mc", "opening/prologue-halgakos"), "idol": ("idol-mc", "idol/songs/idol-halgakos")}
LANGS = ("en", "ja", "ko", "zh")


def encode(wav, target):
    sound, rate = soundfile.read(str(wav))
    if sound.ndim == 1:
        sound = numpy.stack([sound, sound], axis=1)
    sound = sound.astype("float32")
    out = av.open(str(target), "w")
    stream = out.add_stream("aac", rate=rate, layout="stereo")
    stream.bit_rate = 128000
    for i in range(0, len(sound), 1024):
        chunk = numpy.ascontiguousarray(sound[i:i + 1024].T)
        frame = av.AudioFrame.from_ndarray(chunk, format="fltp", layout="stereo")
        frame.sample_rate = rate
        frame.pts = i
        for packet in stream.encode(frame):
            out.mux(packet)
    for packet in stream.encode():
        out.mux(packet)
    out.close()


def rows(times, words):
    """Lines in order of when they begin, each kept until the next one begins if they would overlap."""
    items = sorted(zip(times, words), key=lambda x: x[0][0])
    out = []
    for i, ((a, b), text) in enumerate(items):
        if i + 1 < len(items) and items[i + 1][0][0] < b:
            b = max(a + .4, items[i + 1][0][0])
        out.append(f"{a:.2f}\t{b:.2f}\t{text}")
    return out


def main():
    FILMS.mkdir(parents=True, exist_ok=True)
    for film, (spec_name, out) in SPECS.items():
        spec = json.loads((ROOT / "tools" / "cutscenes" / f"{spec_name}.json").read_text(encoding="utf-8"))
        words = {"en": [line[2] for line in spec["lines"]]}
        for lang in LANGS[1:]:
            words[lang] = spec["tongues"][lang]["lines"]
        nominal = [(line[0], line[0] + line[1]) for line in spec["lines"]]
        for voice in LANGS[1:]:
            wav = DIST / f"{out}-{voice}.wav"
            if not wav.exists():
                print("missing", wav)
                continue
            encode(wav, RAW / f"run_film_{film}_h_{voice}.m4a")
            times = [tuple(t) for t in json.loads((DIST / f"{out}-{voice}.times.json").read_text(encoding="utf-8"))]
            for cap in LANGS:
                (FILMS / f"{film}_h_{voice}.{cap}.txt").write_text("\n".join(rows(times, words[cap])) + "\n", encoding="utf-8")
        for cap in LANGS[1:]:
            (FILMS / f"{film}_h.{cap}.txt").write_text("\n".join(rows(nominal, words[cap])) + "\n", encoding="utf-8")
        print(film, "done")


if __name__ == "__main__":
    main()
