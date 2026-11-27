"""Packs Tulle, the vanity shop's keeper, from the model's sheet of six poses (3 columns by 2 rows) into six frames of
224 by 288 in a row: res/drawable-nodpi/run_vanity_npc.webp. Every frame is cut at one scale (so she never changes size
between frames) and stands with her feet on the same line and on the same column; the faint haze the model leaves about
a figure is taken off, as the game's other art is.

    python tools/make_vanity_npc.py RAW_SHEET.png [PREVIEW.png]
"""
import pathlib, sys
import numpy as np
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "app/src/main/res/drawable-nodpi/run_vanity_npc.webp"
CELL = (224, 288)
COLS, ROWS = 3, 2


def cleaned(im):
    a = np.array(im.convert("RGBA"))
    al = a[..., 3].astype(np.int32)
    a[..., 3] = np.where(al < 60, 0, np.clip((al - 60) * 255 // 150, 0, 255)).astype(np.uint8)
    return Image.fromarray(a, "RGBA")


def main():
    raw = cleaned(Image.open(sys.argv[1]))
    cw, ch = raw.size[0] // COLS, raw.size[1] // ROWS
    cells = []
    for i in range(COLS * ROWS):
        c = raw.crop(((i % COLS) * cw, (i // COLS) * ch, (i % COLS) * cw + cw, (i // COLS) * ch + ch))
        box = c.getchannel("A").point(lambda v: 255 if v > 60 else 0).getbbox()
        cells.append((c, box))
    heights = sorted(b[3] - b[1] for _, b in cells)
    scale = (CELL[1] - 8) / heights[len(heights) // 2] * 0.97
    frames = []
    for c, box in cells:
        # Her feet: the bottom tenth of her, and the middle of them, is where she stands.
        feet = c.crop((box[0], box[3] - (box[3] - box[1]) // 9, box[2], box[3]))
        fb = feet.getchannel("A").point(lambda v: 255 if v > 60 else 0).getbbox()
        fx = box[0] + (fb[0] + fb[2]) / 2
        sprite = c.convert("RGBa").resize((round(c.size[0] * scale), round(c.size[1] * scale)), Image.LANCZOS).convert("RGBA")
        frame = Image.new("RGBA", CELL, (0, 0, 0, 0))
        frame.alpha_composite(sprite, (round(CELL[0] / 2 - fx * scale), round(CELL[1] - 4 - box[3] * scale)))
        frames.append(frame)
    sheet = Image.new("RGBA", (CELL[0] * len(frames), CELL[1]), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        sheet.alpha_composite(f, (i * CELL[0], 0))
    sheet.save(OUT, lossless=True, quality=100, method=6)
    print("frames", len(frames), "scale", round(scale, 3), OUT.stat().st_size, "bytes")
    if len(sys.argv) > 2:
        bg = Image.new("RGBA", sheet.size, (205, 205, 215, 255))
        bg.alpha_composite(sheet)
        bg.convert("RGB").save(sys.argv[2])


if __name__ == "__main__":
    main()
