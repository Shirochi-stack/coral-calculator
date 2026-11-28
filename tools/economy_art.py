"""Builds the economy's icon sheets for the runart asset pack from the uncompressed originals in assets/economy/ (made with gpt-6-astra).

usage: python tools/economy_art.py
curio_icons.webp: 48 curios, eight 128 px squares to a row (curios_0..2.png are 4x4 grids of 16 each, in order).
weapon_icons.webp: the named weapons, sixteen to a row; and a table of each icon's handle and tip (written to weapon_axes.txt for RunWeapons).
"""
import os, sys, glob
from PIL import Image
import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "assets", "economy")
OUT = os.path.join(ROOT, "runart", "src", "main", "assets", "runart")
os.makedirs(OUT, exist_ok=True)
SQ = 128


def cells(img, cols, rows):
    w, h = img.width // cols, img.height // rows
    return [img.crop((c * w, r * h, (c + 1) * w, (r + 1) * h)) for r in range(rows) for c in range(cols)]


def fit(cell, fill=.9):
    """Trim to the picture and scale it to fill a square of SQ, centred."""
    alpha = np.asarray(cell.getchannel("A"))
    ys, xs = np.where(alpha > 24)
    if len(xs) == 0:
        return Image.new("RGBA", (SQ, SQ), (0, 0, 0, 0))
    sprite = cell.crop((xs.min(), ys.min(), xs.max() + 1, ys.max() + 1))
    scale = SQ * fill / max(sprite.width, sprite.height)
    sprite = sprite.resize((max(1, round(sprite.width * scale)), max(1, round(sprite.height * scale))), Image.LANCZOS)
    out = Image.new("RGBA", (SQ, SQ), (0, 0, 0, 0))
    out.alpha_composite(sprite, ((SQ - sprite.width) // 2, (SQ - sprite.height) // 2))
    return out


def sheet(icons, columns):
    rows = (len(icons) + columns - 1) // columns
    out = Image.new("RGBA", (columns * SQ, rows * SQ), (0, 0, 0, 0))
    for i, icon in enumerate(icons):
        out.alpha_composite(icon, ((i % columns) * SQ, (i // columns) * SQ))
    return out


def axis(icon):
    """A weapon icon drawn along the diagonal, handle at the lower left and tip at the upper right: the two ends, as the points of the
    picture furthest along that line (x - y smallest and largest)."""
    alpha = np.asarray(icon.getchannel("A"))
    ys, xs = np.where(alpha > 40)
    d = xs - ys
    lo, hi = d.argmin(), d.argmax()
    return int(xs[lo]), int(ys[lo]), int(xs[hi]), int(ys[hi])


def curios():
    icons = []
    for n in range(3):
        path = os.path.join(SRC, "curios_%d.png" % n)
        icons += [fit(c) for c in cells(Image.open(path).convert("RGBA"), 4, 4)]
    out = sheet(icons, 8)
    out.save(os.path.join(OUT, "curio_icons.webp"), "WEBP", quality=90, method=6)
    print("curio_icons", out.size, os.path.getsize(os.path.join(OUT, "curio_icons.webp")) // 1024, "KB")


def trim(img):
    alpha = np.asarray(img.getchannel("A"))
    ys, xs = np.where(alpha > 24)
    return img.crop((xs.min(), ys.min(), xs.max() + 1, ys.max() + 1))


def elf():
    """Aelwyn's eight pictures in a row (cells of 256 by 320, feet on one line, one scale for all), and her stall."""
    sheet_ = Image.open(os.path.join(SRC, "elf_idle.png")).convert("RGBA")
    parts = cells(sheet_, 4, 2)
    boxes = []
    for p in parts:
        a = np.asarray(p.getchannel("A"))
        ys, xs = np.where(a > 24)
        boxes.append((xs.min(), ys.min(), xs.max() + 1, ys.max() + 1))
    scale = 300.0 / float(np.median([b[3] - b[1] for b in boxes]))
    cw, ch, foot = 256, 320, 312
    out = Image.new("RGBA", (cw * 8, ch), (0, 0, 0, 0))
    for i, (p, b) in enumerate(zip(parts, boxes)):
        sprite = p.crop(b)
        sprite = sprite.resize((max(1, round(sprite.width * scale)), max(1, round(sprite.height * scale))), Image.LANCZOS)
        # (Centred on the cell's own middle, so that a raised arm does not shift her.)
        x = round(cw / 2 - (p.width / 2 - b[0]) * scale)
        out.alpha_composite(sprite, (i * cw + x, foot - sprite.height))
    out.save(os.path.join(OUT, "elf_idle.webp"), "WEBP", quality=90, method=6)
    stall = trim(Image.open(os.path.join(SRC, "elf_stall.png")).convert("RGBA"))
    stall = stall.resize((512, round(512 * stall.height / stall.width)), Image.LANCZOS)
    stall.save(os.path.join(OUT, "elf_stall.webp"), "WEBP", quality=90, method=6)
    print("elf", out.size, stall.size)


def weapons():
    """128 weapon icons in sixteen to a row, in the order of weapons_table.ROWS, and each one handle and tip."""
    import json
    icons = []
    for n in range(8):
        path = os.path.join(SRC, "weapons_%d.png" % n)
        icons += [fit(c, .94) for c in cells(Image.open(path).convert("RGBA"), 4, 4)]
    icons = icons[:128]
    out = sheet(icons, 16)
    out.save(os.path.join(OUT, "weapon_icons.webp"), "WEBP", quality=90, method=6)
    axes = [list(axis(i)) for i in icons]
    json.dump(axes, open(os.path.join(SRC, "weapon_axes.json"), "w"))
    print("weapon_icons", out.size, os.path.getsize(os.path.join(OUT, "weapon_icons.webp")) // 1024, "KB")


if __name__ == "__main__":
    curios()
    if os.path.exists(os.path.join(SRC, "weapons_0.png")):
        weapons()
    if os.path.exists(os.path.join(SRC, "elf_idle.png")):
        elf()
