"""
Frames of the longest caption of each review copy of the idol film (each shape, each language), side by side, to see that none is cut off.
    python tools/captions_check.py
Writes dist/cutscenes/films-review/longest-captions-upright.jpg and -wide.jpg.
"""
import pathlib

import av
from PIL import Image

D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes")
R = D / "films-review"


def frame_at(path, t):
    box = av.open(str(path))
    video = box.streams.video[0]
    box.seek(int(t * 1_000_000))
    for f in box.decode(video):
        if float(f.pts * f.time_base) >= t - .05:
            image = f.to_image()
            box.close()
            return image
    box.close()


def main():
    for shape, name, size in (("9x16", "upright", (360, 640)), ("16x9", "wide", (640, 360))):
        tiles = []
        for lang in ("en", "ja", "ko", "zh"):
            rows = [r.split("\t", 2) for r in (D / "idol" / "retimed" / f"idol-{shape}-{lang}-captions.txt").read_text(encoding="utf-8").strip().split("\n")]
            a, b, text = max(rows, key=lambda r: len(r[2]))
            t = (float(a) + float(b)) / 2
            image = frame_at(R / f"idol-{name}-{lang}-retimed.mp4", t)
            tiles.append(image.resize(size))
            print(shape, lang, f"{t:.1f} s", len(text), "letters:", text)
        sheet = Image.new("RGB", (size[0] * 4, size[1]))
        for i, im in enumerate(tiles):
            sheet.paste(im, (i * size[0], 0))
        sheet.save(R / f"longest-captions-{name}.jpg", quality=88)


if __name__ == "__main__":
    main()
