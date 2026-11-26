"""
Builds the art of Plus run from four generated pictures.

    python tools/make_run_art.py <folder with sky.png, ground.png, minus.png, items.png> [--preview out.png]

The pictures were made with an image model through Glossarion, from prompts that ask for a
background, a ground texture, and two sheets of sprites on flat magenta. This cuts them up:

  * sky.png becomes res/drawable-nodpi/plus_run_sky.webp, the background, scaled down.
  * ground.png gives two square tiles: the top of the ground, with its turf, and the soil.
  * minus.png gives the minus walking on one foot, on the other, and flattened.
  * items.png gives the plus, a ledge, the flag and the gate.

The nine go side by side into res/drawable-nodpi/plus_run_art.webp, each in a square cell,
standing on the bottom of it and centred. How much of its cell each one fills is printed, to
be copied into PlayStage.java as RUN_ART_WIDE and RUN_ART_TALL. Needs Pillow.
"""

import argparse
import pathlib

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
CELL = 128
SKY_WIDTH = 1024
# How far from the sprite sheets' background a colour has to be before it is kept whole, and
# how near before it is dropped: between the two an edge fades out.
KEEP, DROP = 40, 150


def keyed(image):
    """A sprite sheet with its magenta background made clear, and the magenta taken out of the edges."""
    image = image.convert("RGBA")
    pixels = image.load()
    for y in range(image.size[1]):
        for x in range(image.size[0]):
            red, green, blue, _ = pixels[x, y]
            # Magenta is much red and much blue with little green.
            magenta = min(red, blue) - green
            if magenta >= DROP:
                pixels[x, y] = (0, 0, 0, 0)
            elif magenta > KEEP:
                solid = 1 - (magenta - KEEP) / (DROP - KEEP)
                pixels[x, y] = (round(green + (red - green) * solid), green, round(green + (blue - green) * solid), round(255 * solid))
    return image


def cleared(image):
    """A sprite sheet with nothing behind its sprites, whether it came with a clear background or a magenta one."""
    if image.mode == "RGBA" and image.getchannel("A").getextrema()[0] == 0:
        # Already clear. What is all but solid is made solid, so no sprite is faintly see-through.
        image = image.copy()
        image.putalpha(image.getchannel("A").point(lambda value: 255 if value >= 235 else value))
        return image
    return keyed(image)


def sprites(sheet, expected):
    """The separate sprites of a keyed sheet, left to right, each trimmed to what is drawn."""
    alpha = sheet.getchannel("A").point(lambda value: 255 if value > 60 else 0)
    columns = [alpha.crop((x, 0, x + 1, sheet.size[1])).getbbox() is not None for x in range(sheet.size[0])]
    runs, start, gap = [], None, 0
    for x, filled in enumerate(columns + [False] * 14):
        if filled:
            if start is None:
                start = x
            gap = 0
        elif start is not None:
            gap += 1
            # A gap this wide ends a sprite; a narrower one is a hole in it.
            if gap >= 14:
                runs.append((start, x - gap + 1))
                start = None
    runs = [run for run in runs if run[1] - run[0] >= 30]
    if len(runs) != expected:
        raise SystemExit(f"expected {expected} sprites, found {len(runs)}: {runs}")
    found = []
    for left, right in runs:
        piece = sheet.crop((left, 0, right, sheet.size[1]))
        found.append(piece.crop(piece.getchannel("A").point(lambda value: 255 if value > 60 else 0).getbbox()))
    return found


def cell(sprite):
    """A sprite scaled to fit a cell, standing on the bottom of it and centred; and how much of the cell it fills."""
    scale = CELL / max(sprite.size)
    size = (max(1, round(sprite.size[0] * scale)), max(1, round(sprite.size[1] * scale)))
    small = sprite.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
    square = Image.new("RGBA", (CELL, CELL), (0, 0, 0, 0))
    square.alpha_composite(small, ((CELL - size[0]) // 2, CELL - size[1]))
    return square, size[0] / CELL, size[1] / CELL


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("folder", type=pathlib.Path)
    parser.add_argument("--preview", type=pathlib.Path)
    args = parser.parse_args()

    sky = Image.open(args.folder / "sky.png").convert("RGB")
    sky = sky.resize((SKY_WIDTH, round(sky.size[1] * SKY_WIDTH / sky.size[0])), Image.LANCZOS)
    sky.save(RES / "plus_run_sky.webp", quality=82, method=6)

    ground = Image.open(args.folder / "ground.png").convert("RGBA")
    side = ground.size[0]
    # The turf lies along the top fifth: a tile three quarters of the picture high has it as its top quarter.
    turf = ground.crop((side // 8, 0, side // 8 + side * 3 // 4, side * 3 // 4)).resize((CELL, CELL), Image.LANCZOS)
    soil = ground.crop((side // 8, side * 3 // 8, side // 8 + side // 2, side * 7 // 8)).resize((CELL, CELL), Image.LANCZOS)

    minus = sprites(cleared(Image.open(args.folder / "minus.png")), 3)
    items = sprites(cleared(Image.open(args.folder / "items.png")), 4)

    cells, wide, tall = [turf, soil], [1.0, 1.0], [1.0, 1.0]
    for sprite in minus + items:
        square, across, up = cell(sprite)
        cells.append(square)
        wide.append(across)
        tall.append(up)
    atlas = Image.new("RGBA", (CELL * len(cells), CELL), (0, 0, 0, 0))
    for index, square in enumerate(cells):
        atlas.alpha_composite(square.convert("RGBA"), (index * CELL, 0))
    atlas.save(RES / "plus_run_art.webp", lossless=True, quality=100, method=6)
    print("sky", sky.size, "atlas", atlas.size)
    print("RUN_ART_WIDE = {" + ", ".join(f"{value:.3f}f" for value in wide) + "};")
    print("RUN_ART_TALL = {" + ", ".join(f"{value:.3f}f" for value in tall) + "};")
    if args.preview:
        board = Image.new("RGBA", (atlas.size[0] * 2, atlas.size[1] * 2 + sky.size[1] // 2), (120, 120, 130, 255))
        board.alpha_composite(atlas.resize((atlas.size[0] * 2, atlas.size[1] * 2), Image.LANCZOS), (0, 0))
        board.paste(sky.resize((sky.size[0] // 2, sky.size[1] // 2), Image.LANCZOS), (0, atlas.size[1] * 2))
        board.convert("RGB").save(args.preview)


if __name__ == "__main__":
    main()
