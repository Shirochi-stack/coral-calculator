"""
Builds the art of Page brawl from the pictures in assets/fight.

    python tools/make_fight_art.py [--preview out.png]

The pictures were made with an image model (gpt-6-astra, through Glossarion) on transparent
backgrounds, each fighter drawn from her own non-chibi design sheet of the webtoon:

  * NAME_move.webp and NAME_hits.webp are two sheets of six frames, three across and two
    down, of each of the five fighters. The first has her standing, walking, jumping,
    guarding, hit and down; the second her quick move, her heavy one, her special, her
    ultimate, her win, and one more of her special.
  * fx.webp is ten things that fly and flash, five across and two down.
  * stage1.webp and stage2.webp are the two stages.
  * face_NAME.png is each one's design sheet, made small, from which her portrait is cut.

Each fighter's twelve frames go into res/drawable-nodpi/fight_NAME.webp, six across and two
down, every frame in a cell of the same size with her feet at the same place in it and all
twelve at one scale: her two sheets are brought to the same height standing, and so are the
five fighters. The model does not keep to an exact grid, so a sheet is taken apart by what
touches what: its largest pieces are the figures, and every smaller piece, a thrown roll or
a drop of ink, goes with the figure it is nearest. The things that fly go into fight_fx.webp, the portraits into fight_faces.webp,
and the stages into fight_stage1.webp and fight_stage2.webp. What FightGame.java needs to
know about them is printed. Needs Pillow, numpy and scipy.
"""

import argparse
import pathlib

import numpy
import scipy.ndimage
from PIL import Image, ImageDraw, ImageFilter

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "assets" / "fight"
RES = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
FIGHTERS = ("halgakos", "chikuwa", "tsubakieh", "rune", "illya")
# A fighter's cell, where her feet stand in it, and how tall she is standing.
CELL = (480, 400)
FEET = (200, 392)
STAND = 262
# The frames of each sheet in which she is upright, from which her height is taken.
UPRIGHT = {"move": (0, 1, 3, 4), "hits": (0, 1, 2, 4, 5)}
FX_CELL = 160
FACE = 256
STAGE_WIDTH = 1280


def clean(image):
    """A sheet with the faint haze round its figures taken off, and what is all but solid made solid."""
    image = image.convert("RGBA")
    image.putalpha(image.getchannel("A").point(lambda value: 0 if value < 48 else 255 if value >= 235 else value))
    return image


def solid(image):
    return image.getchannel("A").point(lambda value: 255 if value > 60 else 0)


def cells(sheet, across, down):
    """
    The pictures of a sheet, so many across and down, in reading order, each trimmed to what
    is drawn. The largest pieces of the sheet are the pictures; a smaller piece belongs to
    the picture it is nearest.
    """
    mask = numpy.array(solid(sheet)) > 0
    # Pieces a few pixels apart are one piece: a sparkle beside a ball of light, a tear beside a cheek.
    labels, count = scipy.ndimage.label(scipy.ndimage.binary_dilation(mask, iterations=3))
    labels = labels * mask
    boxes = scipy.ndimage.find_objects(labels)
    sizes = scipy.ndimage.sum(mask, labels, range(1, count + 1))
    wanted = across * down
    order = sorted(range(count), key=lambda index: -sizes[index])
    if count < wanted:
        raise SystemExit(f"expected {wanted} pictures, found {count} pieces")
    main = order[:wanted]

    def away(one, other):
        """How far apart two boxes are."""
        (y0, x0), (y1, x1) = one, other
        across_gap = max(0, x0.start - x1.stop, x1.start - x0.stop)
        down_gap = max(0, y0.start - y1.stop, y1.start - y0.stop)
        return (across_gap ** 2 + down_gap ** 2) ** .5

    owner = {index: index for index in main}
    for index in order[wanted:]:
        if sizes[index] < 6:
            continue
        owner[index] = min(main, key=lambda body: away(boxes[index], boxes[body]))
    # Reading order: rows by how far down their middles are, then left to right.
    def middle(index):
        rows, columns = boxes[index]
        return (rows.start + rows.stop) / 2, (columns.start + columns.stop) / 2

    by_row = sorted(main, key=lambda index: middle(index)[0])
    ordered = []
    for row in range(down):
        ordered += sorted(by_row[row * across:(row + 1) * across], key=lambda index: middle(index)[1])
    pixels = numpy.array(sheet)
    found = []
    for body in ordered:
        mine = numpy.isin(labels, [index + 1 for index, to in owner.items() if to == body])
        piece = pixels.copy()
        piece[~mine] = 0
        image = Image.fromarray(piece, "RGBA")
        found.append(image.crop(solid(image).getbbox()))
    return found


def feet_middle(frame):
    """Where her feet are across a frame: the middle of what is drawn in its lowest tenth."""
    mask = solid(frame)
    tall = frame.size[1]
    box = mask.crop((0, tall - max(4, tall // 10), frame.size[0], tall)).getbbox()
    return (box[0] + box[2]) / 2


def hair_colour(frame):
    """The mean colour of the upper third of a frame's solid pixels: mostly her hair."""
    pixels = frame.load()
    total, count = [0, 0, 0], 0
    for y in range(0, frame.size[1] // 3, 2):
        for x in range(0, frame.size[0], 2):
            pixel = pixels[x, y]
            if pixel[3] >= 235:
                total = [total[i] + pixel[i] for i in range(3)]
                count += 1
    return [round(channel / max(1, count)) for channel in total]


def fighter(name, report):
    """A fighter's twelve frames, six across and two down, at one scale with her feet in one place."""
    atlas = Image.new("RGBA", (CELL[0] * 6, CELL[1] * 2), (0, 0, 0, 0))
    for row, sheet_name in enumerate(("move", "hits")):
        frames = cells(clean(Image.open(SOURCE / f"{name}_{sheet_name}.webp")), 3, 2)
        scale = STAND / max(frames[index].size[1] for index in UPRIGHT[sheet_name])
        report[f"{name}_{sheet_name}"] = {"scale": round(scale, 3), "hair": hair_colour(frames[0])}
        for column, frame in enumerate(frames):
            size = (max(1, round(frame.size[0] * scale)), max(1, round(frame.size[1] * scale)))
            small = frame.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
            left = round(FEET[0] - feet_middle(small))
            top = FEET[1] - size[1]
            # Kept inside its cell, if it would stick out of it.
            left = max(0, min(CELL[0] - size[0], left))
            if size[0] > CELL[0] or top < 0:
                raise SystemExit(f"{name} {sheet_name} frame {column} is {size}, too large for its cell {CELL}")
            atlas.alpha_composite(small, (column * CELL[0] + left, row * CELL[1] + top))
    atlas.save(RES / f"fight_{name}.webp", quality=88, alpha_quality=92, method=6)
    return atlas


def without_white(image):
    """A design sheet drawn on white, with the white round the figure made clear."""
    image = image.convert("RGBA")
    if image.getchannel("A").getextrema()[0] == 0:
        return image
    mask = Image.new("L", image.size, 0)
    grey = image.convert("L").point(lambda value: 255 if value >= 244 else 0)
    # The white that can be reached from the corners is the background; white inside her outline is not.
    for corner in ((0, 0), (image.size[0] - 1, 0), (0, image.size[1] - 1), (image.size[0] - 1, image.size[1] - 1)):
        if grey.getpixel(corner) == 255:
            ImageDraw.floodfill(grey, corner, 128)
    mask = grey.point(lambda value: 0 if value == 128 else 255).filter(ImageFilter.MinFilter(3)).filter(ImageFilter.GaussianBlur(.8))
    image.putalpha(mask)
    return image


def face(name):
    """A fighter's portrait: her head and shoulders, cut from her design sheet."""
    sheet = without_white(Image.open(SOURCE / f"face_{name}.png"))
    box = solid(sheet).getbbox()
    tall = box[3] - box[1]
    # Her head is under the middle of the very top of her: her ears, her horns, her hat.
    head = solid(sheet).crop((box[0], box[1], box[2], box[1] + tall // 16)).getbbox()
    middle = box[0] + (head[0] + head[2]) / 2
    side = tall * .37
    crop = sheet.crop((round(middle - side / 2), round(box[1] - side * .04), round(middle + side / 2), round(box[1] + side * .96)))
    return crop.convert("RGBa").resize((FACE, FACE), Image.LANCZOS).convert("RGBA")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", type=pathlib.Path)
    args = parser.parse_args()
    report = {}
    atlases = [fighter(name, report) for name in FIGHTERS]

    faces = Image.new("RGBA", (FACE * len(FIGHTERS), FACE), (0, 0, 0, 0))
    for index, name in enumerate(FIGHTERS):
        faces.alpha_composite(face(name), (index * FACE, 0))
    faces.save(RES / "fight_faces.webp", quality=90, alpha_quality=92, method=6)

    things = cells(clean(Image.open(SOURCE / "fx.webp")), 5, 2)
    fx = Image.new("RGBA", (FX_CELL * len(things), FX_CELL), (0, 0, 0, 0))
    wide, tall = [], []
    for index, thing in enumerate(things):
        scale = FX_CELL / max(thing.size)
        size = (max(1, round(thing.size[0] * scale)), max(1, round(thing.size[1] * scale)))
        small = thing.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
        fx.alpha_composite(small, (index * FX_CELL + (FX_CELL - size[0]) // 2, (FX_CELL - size[1]) // 2))
        wide.append(size[0] / FX_CELL)
        tall.append(size[1] / FX_CELL)
    fx.save(RES / "fight_fx.webp", quality=90, alpha_quality=92, method=6)

    for number in (1, 2):
        stage = Image.open(SOURCE / f"stage{number}.webp").convert("RGB")
        stage = stage.resize((STAGE_WIDTH, round(stage.size[1] * STAGE_WIDTH / stage.size[0])), Image.LANCZOS)
        stage.save(RES / f"fight_stage{number}.webp", quality=80, method=6)

    print("cell", CELL, "feet", FEET, "stand", STAND, "faces", faces.size, "fx", fx.size)
    print("FX_WIDE = {" + ", ".join(f"{value:.3f}f" for value in wide) + "};")
    print("FX_TALL = {" + ", ".join(f"{value:.3f}f" for value in tall) + "};")
    for key, value in report.items():
        print(key, value)
    for name in list(FIGHTERS) + ["faces", "fx", "stage1", "stage2"]:
        print(f"fight_{name}.webp", (RES / f"fight_{name}.webp").stat().st_size)
    if args.preview:
        grey = (110, 120, 140, 255)
        half = [atlas.resize((atlas.size[0] // 2, atlas.size[1] // 2), Image.LANCZOS) for atlas in atlases]
        board = Image.new("RGBA", (half[0].size[0], sum(one.size[1] for one in half) + FACE + FX_CELL), grey)
        y = 0
        for one in half:
            board.alpha_composite(one, (0, y))
            # A line where their feet stand, to see that they stand on it.
            for row in range(2):
                ImageDraw.Draw(board).line((0, y + (row * CELL[1] + FEET[1]) // 2, board.size[0], y + (row * CELL[1] + FEET[1]) // 2), fill=(255, 255, 255, 90))
            y += one.size[1]
        board.alpha_composite(faces, (0, y))
        board.alpha_composite(fx, (0, y + FACE))
        board.convert("RGB").save(args.preview)


if __name__ == "__main__":
    main()
