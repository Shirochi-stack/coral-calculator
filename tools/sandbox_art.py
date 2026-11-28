"""Builds the sandbox asset pack's art from the uncompressed originals in assets/sandbox/ (generated with gpt-6-astra).

usage: python tools/sandbox_art.py
Seamless textures: each original is made to tile (offset blend), shrunk to 256 px. Sprite sheets: cut by grid, trimmed, scaled and
anchored at the feet so that every cell has its feet on the same line, written as a single row or small grid.
"""
import os
from PIL import Image
import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "assets", "sandbox")
OUT = os.path.join(ROOT, "sandbox", "src", "main", "assets", "sandbox")
os.makedirs(OUT, exist_ok=True)


def seamless(im, size=256):
    im = im.convert("RGB").resize((512, 512), Image.LANCZOS)
    a = np.asarray(im).astype(np.float32)
    shifted = np.roll(np.roll(a, 256, 0), 256, 1)
    ramp = np.abs(np.linspace(-1, 1, 512))
    w = np.maximum(ramp[None, :], ramp[:, None])  # 0 in the middle, 1 at the edges
    w = np.clip((w - .25) / .6, 0, 1)
    w = (w * w * (3 - 2 * w))[..., None]
    out = shifted * w + a * (1 - w)
    return Image.fromarray(out.astype(np.uint8)).resize((size, size), Image.LANCZOS)


def bbox(cell):
    alpha = np.asarray(cell.getchannel("A"))
    ys, xs = np.where(alpha > 24)
    if len(xs) == 0:
        return None
    return xs.min(), ys.min(), xs.max() + 1, ys.max() + 1


def cells(im, cols, rows):
    w, h = im.width // cols, im.height // rows
    return [im.crop((c * w, r * h, (c + 1) * w, (r + 1) * h)) for r in range(rows) for c in range(cols)]


def place(cell, scale, out_w, out_h, base, centre_on_cell=False):
    box = bbox(cell)
    out = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    if box is None:
        return out
    sprite = cell.crop(box)
    sprite = sprite.resize((max(1, round(sprite.width * scale)), max(1, round(sprite.height * scale))), Image.LANCZOS)
    if centre_on_cell:
        cx = (box[0] + box[2]) / 2 if False else cell.width / 2
        x = round(out_w / 2 - (cell.width / 2 - box[0]) * scale)
    else:
        x = (out_w - sprite.width) // 2
    out.alpha_composite(sprite, (x, base - sprite.height))
    return out


def sheet(parts, cols, out_w, out_h):
    rows = (len(parts) + cols - 1) // cols
    out = Image.new("RGBA", (cols * out_w, rows * out_h), (0, 0, 0, 0))
    for i, p in enumerate(parts):
        out.alpha_composite(p, ((i % cols) * out_w, (i // cols) * out_h))
    return out


def save(im, name, quality=88):
    path = os.path.join(OUT, name + ".webp")
    im.save(path, "WEBP", quality=quality, method=6)
    print(name, os.path.getsize(path) // 1024, "KB", im.size)


def main():
    for name in ["grass", "path", "stone", "water", "floor", "wall2", "roof"]:
        save(seamless(Image.open(os.path.join(SRC, "tex_" + name + ".png"))), "tex_" + name.replace("2", ""), 85)
    # Halgakos: one scale for all eighteen frames, so that she is the same size from every side.
    her = {}
    heights = []
    for name, file in (("down", "her_down"), ("up", "her_up"), ("side", "her_side2")):
        her[name] = cells(Image.open(os.path.join(SRC, file + ".png")).convert("RGBA"), 3, 2)
        heights += [bbox(c)[3] - bbox(c)[1] for c in her[name][:5]]
    scale = 190 / float(np.median(heights))
    for name in her:
        save(sheet([place(c, scale, 256, 240, 232, True) for c in her[name]], 6, 256, 240), "her_" + name)
    trees = cells(Image.open(os.path.join(SRC, "trees.png")).convert("RGBA"), 3, 1)
    save(sheet([place(c, min(1.0, 372 / (bbox(c)[3] - bbox(c)[1]), 312 / (bbox(c)[2] - bbox(c)[0])), 320, 384, 380) for c in trees], 3, 320, 384), "trees")
    for name, file, cols in (("rocks", "rocks", 2), ("furniture", "furniture", 2), ("creatures", "creatures", 2)):
        parts = []
        for c in cells(Image.open(os.path.join(SRC, file + ".png")).convert("RGBA"), cols, 2):
            b = bbox(c)
            parts.append(place(c, min(1.0, 232 / (b[3] - b[1]), 232 / (b[2] - b[0])), 256, 256, 250))
        save(sheet(parts, 2, 256, 256), name)


main()
