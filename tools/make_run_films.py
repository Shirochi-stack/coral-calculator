"""
The films Plus run shows, made ready for a phone from the films the cutscene tool edits.

    python tools/make_run_films.py [pictures] [sounds] [captions] [stills]      (nothing named: all of them)

A film is shipped as one picture without sound and several sounds, each a file of its own,
which the game starts together (RunVideo): so the picture is not shipped once for every sound.

pictures  res/raw/run_film_<name>.mp4: H.264 (main), 1280 x 720 or 720 x 1280, a key frame at
          least every two seconds so that it begins at once, no sound. The bit rate is PICTURE's.
sounds    res/raw/run_film_<name>.m4a: AAC, stereo, 128 kb/s. Each is the sound of a film as
          the edit made it, so the page's flutter (or the sting) at the joins is in it.
captions  assets/films/<sound>.<language>.txt: what is heard, line by line, as
          "begins<TAB>ends<TAB>words"; from tools/cutscenes/captions.json (see there).
stills    assets/gallery/*.webp: a frame of each film for its tile, the storyboards and the
          cast sheets, at a size that reads on a phone.

What is made from what is the tables below. Needs av, numpy, Pillow.
"""
import fractions
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SHOTS = ROOT / "dist" / "cutscenes"
RAW = ROOT / "films" / "src" / "main" / "assets" / "films"
ASSETS = ROOT / "app" / "src" / "main" / "assets"
# A picture: its name in the game, the film it is the picture of, its size, and its bits a second.
PICTURES = [
    ("prologue", "opening/opening-seedance-new-words.mp4", (1280, 720), 1_150_000),
    ("idol_tall", "idol/idol-9x16-new-words.mp4", (720, 1280), 1_250_000),
    ("idol_wide", "idol/idol-16x9-new-words.mp4", (1280, 720), 1_250_000),
]
# A sound: its name in the game, and the film whose sound it is.
SOUNDS = [
    ("prologue_h", "opening/opening-seedance-new-words.mp4"),
    ("prologue_h_ja", "opening/opening-seedance-new-words-ja.mp4"),
    ("prologue_h_ko", "opening/opening-seedance-new-words-ko.mp4"),
    ("prologue_h_zh", "opening/opening-seedance-new-words-zh.mp4"),
    ("prologue_o", "opening/opening-seedance.mp4"),
    ("prologue_l", "opening/opening-seedance-legacy.mp4"),
    ("idol_h", "idol/idol-9x16-new-words.mp4"),
    ("idol_h_ja", "idol/idol-9x16-new-words-ja.mp4"),
    ("idol_h_ko", "idol/idol-9x16-new-words-ko.mp4"),
    ("idol_h_zh", "idol/idol-9x16-new-words-zh.mp4"),
    ("idol_tall_m", "idol/idol-9x16.mp4"),
    ("idol_wide_m", "idol/idol-16x9.mp4"),
    ("idol_l", "idol/idol-9x16-halgakos.mp4"),
]
# A still for the gallery: its name there, what it is made from (a picture, or a film and a second of it), and how many dots its longer side has.
STILLS = [
    ("video_prologue", ("opening/opening-seedance.mp4", 31.5), 640),
    ("video_idol", ("idol/idol-16x9.mp4", 61.5), 640),
    ("video_clip", (str(RAW / "run_cut_mother.mp4"), 2.0), 640),
    ("board_prologue", "opening/storyboard.jpg", 1800),
    ("board_idol_tall", "idol/storyboard-9x16.jpg", 1800),
    ("board_idol_wide", "idol/storyboard-16x9.jpg", 1800),
    ("cast_idol", "idol/cast.png", 1600),
    ("cast_prologue", "opening/sheets/cast.jpg", 1600),
]


def picture(name, film, size, rate):
    """A film's picture alone, at a phone's size."""
    import av

    source = av.open(str(SHOTS / film))
    seen = source.streams.video[0]
    fps = seen.average_rate
    target = RAW / f"run_film_{name}.mp4"
    out = av.open(str(target), "w", options={"movflags": "+faststart"})
    made = out.add_stream("libx264", rate=fps)
    made.width, made.height = size
    made.pix_fmt = "yuv420p"
    made.bit_rate = rate
    made.options = {"profile": "main", "preset": "slow", "g": str(int(round(float(fps) * 2))), "keyint_min": str(int(round(float(fps)))), "maxrate": str(int(rate * 1.6)), "bufsize": str(rate * 3),
                    "sc_threshold": "40"}
    count = 0
    for frame in source.decode(seen):
        small = frame.reformat(width=size[0], height=size[1], format="yuv420p", interpolation="LANCZOS")
        small.pts = count
        small.time_base = fractions.Fraction(fps.denominator, fps.numerator)
        for packet in made.encode(small):
            out.mux(packet)
        count += 1
    for packet in made.encode():
        out.mux(packet)
    out.close()
    source.close()
    print(f"picture {target.name}: {size[0]} x {size[1]}, {count} frames at {float(fps):.0f} a second, {target.stat().st_size:,} bytes ({target.stat().st_size * 8 / (count / float(fps)) / 1e6:.2f} Mb/s)", flush=True)


def sound(name, film):
    """A film's sound alone."""
    import av

    if not (SHOTS / film).exists():
        print(f"sound {name}: no {film} yet, left out", flush=True)
        return
    source = av.open(str(SHOTS / film))
    target = RAW / f"run_film_{name}.m4a"
    out = av.open(str(target), "w", format="ipod", options={"movflags": "+faststart"})
    made = out.add_stream("aac", rate=48000)
    made.bit_rate = 128_000
    made.layout = "stereo"
    resampler = av.AudioResampler(format=made.format, layout="stereo", rate=48000, frame_size=1024)
    at = 0
    for frame in source.decode(audio=0):
        for piece in resampler.resample(frame):
            piece.pts = at
            at += piece.samples
            for packet in made.encode(piece):
                out.mux(packet)
    for piece in resampler.resample(None):
        piece.pts = at
        at += piece.samples
        for packet in made.encode(piece):
            out.mux(packet)
    for packet in made.encode():
        out.mux(packet)
    out.close()
    source.close()
    print(f"sound {target.name}: {at / 48000:.1f} s, {target.stat().st_size:,} bytes", flush=True)


def still(name, source, longest):
    """A picture for the gallery, no larger than it need be."""
    from PIL import Image

    if isinstance(source, tuple):
        import av

        film, second = source
        path = pathlib.Path(film) if pathlib.Path(film).is_absolute() else SHOTS / film
        box = av.open(str(path))
        image = None
        for frame in box.decode(video=0):
            if frame.time >= second:
                image = frame.to_image()
                break
        box.close()
    else:
        image = Image.open(SHOTS / source).convert("RGB")
    scale = min(1.0, longest / max(image.size))
    if scale < 1:
        image = image.resize((round(image.size[0] * scale), round(image.size[1] * scale)), Image.LANCZOS)
    target = ASSETS / "gallery" / f"{name}.webp"
    target.parent.mkdir(parents=True, exist_ok=True)
    image.save(target, "WEBP", quality=82, method=6)
    print(f"still {target.name}: {image.size[0]} x {image.size[1]}, {target.stat().st_size:,} bytes", flush=True)


def captions():
    """What is heard in each sound, line by line, in each language there is."""
    table = json.loads((ROOT / "tools" / "cutscenes" / "captions.json").read_text(encoding="utf-8"))
    folder = ASSETS / "films"
    folder.mkdir(parents=True, exist_ok=True)
    for old in folder.glob("*.txt"):
        old.unlink()
    for name, entry in table["sounds"].items():
        times = entry["times"]
        for language, words in table["words"][entry["words"]].items():
            if len(words) != len(times):
                print(f"captions {name}.{language}: {len(words)} lines of words for {len(times)} times, left out", flush=True)
                continue
            lines = []
            for index, ((begins, ends), said) in enumerate(zip(times, words)):
                # (A line stays up until the next begins, if that is soon; never past it.)
                later = times[index + 1][0] if index + 1 < len(times) else ends + 1
                lines.append(f"{begins:.2f}\t{min(max(ends, begins + .8), later):.2f}\t{said}")
            (folder / f"{name}.{language}.txt").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
            print(f"captions {name}.{language}: {len(lines)} lines", flush=True)


def main():
    asked = [word for word in sys.argv[1:] if not word.startswith("--")] or ["pictures", "sounds", "captions", "stills"]
    only = next((word[7:] for word in sys.argv[1:] if word.startswith("--only=")), None)
    RAW.mkdir(parents=True, exist_ok=True)
    if "pictures" in asked:
        for name, film, size, rate in PICTURES:
            if only is None or only == name:
                picture(name, film, size, rate)
    if "sounds" in asked:
        for name, film in SOUNDS:
            if only is None or only == name:
                sound(name, film)
    if "captions" in asked:
        captions()
    if "stills" in asked:
        for name, source, longest in STILLS:
            if only is None or only == name:
                still(name, source, longest)


if __name__ == "__main__":
    sys.exit(main())
