"""Derive the launcher icon, the companion sprite and the store icon from the Halgakos art.

Usage: python tools/make_art.py [--preview DIR] [--pet SHEET]

Reads assets/HalgakosWhite.png and rewrites the generated images under app/src/main/res
and store/. With --preview it also writes the icon under the masks launchers apply. With
--pet it also rebuilds the animated companion's frames from the Codex pet atlas, which is
kept in the Halgakos-Codex-Pet repository rather than here.
"""
import argparse
import pathlib

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "assets" / "HalgakosWhite.png"
RES = ROOT / "app" / "src" / "main" / "res"
STORE = ROOT / "store"

# The bust shown in the icon: centre of the crop in the source art, and the source width
# that spans the 72dp a launcher mask leaves visible out of the 108dp layer.
BUST_CENTRE = (512, 500)
BUST_VISIBLE = 1010
# Top and bottom of the icon background. Keep in step with res/drawable/ic_launcher_background.xml.
BACKGROUND_TOP = (255, 122, 116)
BACKGROUND_BOTTOM = (240, 46, 66)
SPRITE_HEIGHT = 512
# The Codex pet atlas: cell size, cells per row, and the rows the app plays, in the order
# they take in its own atlas: idle, running right, running left, jumping, failed, working.
# Row 3, waving, is left out: it is drawn in a different style from the rest.
PET_CELL = (192, 208)
PET_COLUMNS = 8
PET_ROWS = (0, 1, 2, 4, 5, 7)
# The idle row breathes by narrowing her whole body below the collar, which reads as her
# shrinking. So her body holds still: every idle frame takes it from the row's first frame.
# Two parts move. Her head, with its blink and bob, is each frame's own down to
# PET_IDLE_SEAM, a row of the cell across her collar, crossfaded over the feather.
#
# Her tail swings. In the row as drawn it hardly changes place from frame to frame while its
# colour does, from dusky to bright and back, which reads as a light playing on it and not
# as movement. So the tail too is the first frame's, one drawing in one colour: the curl of
# it inside PET_IDLE_TAIL, the box of the cell from below the hem of her skirt to the tip,
# is lifted out and turned about PET_IDLE_TAIL_ROOT, where it comes out from behind her
# skirt, by PET_IDLE_TAIL_SWING degrees in each frame in turn: a flick to her left and back,
# a little past where it hangs, and still for the frame she rests on. What is lifted begins
# PET_IDLE_TAIL_LAP pixels above where it is cut away from her, so the two overlap there
# and no gap opens at the join as it turns.
PET_IDLE = 0
PET_IDLE_SEAM = 115
PET_IDLE_FEATHER = 8
PET_IDLE_TAIL = (117, 158, 162, 200)
PET_IDLE_TAIL_LAP = 4
PET_IDLE_TAIL_ROOT = (121, 153)
PET_IDLE_TAIL_SWING = (0, 6, 12, 9, 3, -4, -1)
# The failed row, her apology, draws her larger than the other rows do: her head is up to a
# third wider at the bottom of her bow, where the frame is filled to its edges. Each of its
# frames is made smaller about her feet, by how much wider her horns stand apart than in the
# idle, raised to PET_SORRY_EASE. At 1 her head would be the idle's size exactly and the rest
# of her, which the row draws no larger, would be left looking small; at this her head and
# her body both come out close to their size elsewhere.
PET_SORRY = 4
PET_SORRY_EASE = 0.8
PET_HORN_ROWS = 95


def clean(image):
    """Drops the faint haze around the figure so trimming and shadows follow the outline."""
    alpha = image.getchannel("A").point(lambda value: 0 if value < 12 else value)
    image.putalpha(alpha)
    return image


def trimmed(image):
    return image.crop(image.getchannel("A").getbbox())


def foreground(source, size):
    """The 108dp adaptive-icon layer: the bust centred, with the outer 18dp free to be masked."""
    span = BUST_VISIBLE * 108 / 72
    left = BUST_CENTRE[0] - span / 2
    top = BUST_CENTRE[1] - span / 2
    canvas = Image.new("RGBA", (round(span), round(span)), (0, 0, 0, 0))
    canvas.alpha_composite(source, (round(-left), round(-top)))
    return canvas.resize((size, size), Image.LANCZOS)


def gradient(size):
    image = Image.new("RGBA", (size, size))
    draw = ImageDraw.Draw(image)
    for y in range(size):
        t = y / (size - 1)
        draw.line([(0, y), (size, y)], fill=tuple(
            round(a + (b - a) * t) for a, b in zip(BACKGROUND_TOP, BACKGROUND_BOTTOM)) + (255,))
    return image


def masked(layers, shape):
    """What a launcher shows: the middle 72dp of the layers under its mask."""
    size = layers.size[0]
    inset = size // 6
    visible = layers.crop((inset, inset, size - inset, size - inset))
    side = visible.size[0]
    mask = Image.new("L", (side * 4, side * 4), 0)
    draw = ImageDraw.Draw(mask)
    if shape == "circle":
        draw.ellipse((0, 0, side * 4, side * 4), fill=255)
    elif shape == "squircle":
        draw.rounded_rectangle((0, 0, side * 4, side * 4), radius=side * 4 * .28, fill=255)
    else:
        draw.rectangle((0, 0, side * 4, side * 4), fill=255)
    visible.putalpha(ImageChops.multiply(visible.getchannel("A"), mask.resize((side, side), Image.LANCZOS)))
    return visible


def is_horn(pixel):
    """Whether a pixel of the pet atlas is the dark purple of her horns."""
    red, green, blue, alpha = pixel
    return alpha > 200 and red < 125 and green < 95 and 55 < blue < 135 and blue >= green + 12 and red >= green


def pet_atlas(sheet_path):
    """
    The animated companion's frames, cut from the Halgakos Codex pet atlas (8 by 11 cells of
    192 by 208). Only the rows the app plays are kept, each cell trimmed to the box that
    holds the figure in every frame, so the frames stay registered with one another.
    """
    sheet = Image.open(sheet_path).convert("RGBA")
    if sheet.size != (PET_CELL[0] * PET_COLUMNS, PET_CELL[1] * 11):
        raise SystemExit(f"{sheet_path}: not a Codex v2 pet atlas, size {sheet.size}")
    cells = {}
    box = None
    for row, source_row in enumerate(PET_ROWS):
        for column in range(PET_COLUMNS):
            left, top = column * PET_CELL[0], source_row * PET_CELL[1]
            cell = sheet.crop((left, top, left + PET_CELL[0], top + PET_CELL[1]))
            bounds = cell.getchannel("A").getbbox()
            if bounds is None:
                continue
            cells[(row, column)] = cell
            box = bounds if box is None else (min(box[0], bounds[0]), min(box[1], bounds[1]),
                                              max(box[2], bounds[2]), max(box[3], bounds[3]))
    # Her idle frames share one still body, under each frame's own head, with her tail swung: see PET_IDLE_SEAM.
    below = Image.new("L", (1, PET_CELL[1]))
    below.putdata([round(255 * max(0, min(1, (y - PET_IDLE_SEAM + PET_IDLE_FEATHER / 2) / PET_IDLE_FEATHER)))
                   for y in range(PET_CELL[1])])
    below = below.resize(PET_CELL)

    def area(from_row, feather):
        """The tail's box from a row of the cell down, as a mask with a softened edge."""
        mask = Image.new("L", PET_CELL, 0)
        left, _, right, bottom = PET_IDLE_TAIL
        ImageDraw.Draw(mask).rounded_rectangle((left, from_row, right, bottom), radius=6, fill=255)
        return mask.filter(ImageFilter.GaussianBlur(feather))

    # Blended with the colours weighted by how solid they are, so no edge picks up a fringe.
    first = cells[(PET_IDLE, 0)]
    still = first.convert("RGBa")
    clear = Image.new("RGBa", PET_CELL, (0, 0, 0, 0))
    curl = Image.composite(still, clear, area(PET_IDLE_TAIL[1], 1.5))
    rest = Image.composite(clear, still, area(PET_IDLE_TAIL[1] + PET_IDLE_TAIL_LAP, 1))
    for (row, column), cell in list(cells.items()):
        if row != PET_IDLE:
            continue
        frame = Image.composite(rest, cell.convert("RGBa"), below).convert("RGBA")
        swung = curl.rotate(PET_IDLE_TAIL_SWING[column % len(PET_IDLE_TAIL_SWING)], resample=Image.BICUBIC,
                            center=PET_IDLE_TAIL_ROOT).convert("RGBA")
        cells[(row, column)] = Image.alpha_composite(frame, swung)

    def solid(cell):
        return cell.getchannel("A").point(lambda value: 255 if value > 40 else 0)

    def horns(cell):
        """How far apart the outer edges of her horns are: a measure of how large she is drawn."""
        top = solid(cell).getbbox()[1]
        pixels = cell.load()
        across = [x for y in range(top, min(PET_CELL[1], top + PET_HORN_ROWS)) for x in range(PET_CELL[0])
                  if is_horn(pixels[x, y])]
        return max(across) - min(across)

    # Her apology, brought down to the size she is in the other rows: see PET_SORRY.
    usual = horns(first)
    for (row, column), cell in list(cells.items()):
        if row != PET_SORRY:
            continue
        factor = min(1.0, (usual / horns(cell)) ** PET_SORRY_EASE)
        bounds = solid(cell).getbbox()
        feet = solid(cell).crop((0, bounds[3] - 10, PET_CELL[0], bounds[3])).getbbox()
        middle, ground = (feet[0] + feet[2]) / 2, bounds[3]
        size = (max(1, round(PET_CELL[0] * factor)), max(1, round(PET_CELL[1] * factor)))
        smaller = cell.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
        frame = Image.new("RGBA", PET_CELL, (0, 0, 0, 0))
        # Made smaller about the middle of her feet, which stay where they stood.
        frame.alpha_composite(smaller, (max(0, round(middle - middle * factor)), max(0, round(ground - ground * factor))))
        cells[(row, column)] = frame
    # A clear margin round every frame keeps a scaled frame from picking up its neighbour.
    box = (max(0, box[0] - 2), max(0, box[1] - 2), min(PET_CELL[0], box[2] + 2), min(PET_CELL[1], box[3] + 2))
    width, height = box[2] - box[0], box[3] - box[1]
    atlas = Image.new("RGBA", (width * PET_COLUMNS, height * len(PET_ROWS)), (0, 0, 0, 0))
    for (row, column), cell in cells.items():
        atlas.paste(cell.crop(box), (column * width, row * height))
    counts = [sum(1 for (row, _) in cells if row == index) for index in range(len(PET_ROWS))]
    return atlas, (width, height), counts


def feature_graphic(source):
    """Google Play's 1024 by 500 banner: the name beside the figure, on the icon's colours."""
    width, height = 1024, 500
    banner = Image.new("RGBA", (width, height))
    draw = ImageDraw.Draw(banner)
    for x in range(width):
        t = x / (width - 1)
        draw.line([(x, 0), (x, height)], fill=tuple(
            round(a + (b - a) * t) for a, b in zip(BACKGROUND_BOTTOM, BACKGROUND_TOP)) + (255,))
    figure = trimmed(source)
    tall = 452
    figure = figure.resize((round(figure.size[0] * tall / figure.size[1]), tall), Image.LANCZOS)
    banner.alpha_composite(figure, (width - figure.size[0] - 58, (height - tall) // 2 + 6))

    def font(names, size):
        for name in names:
            try:
                return ImageFont.truetype(name, size)
            except OSError:
                continue
        return ImageFont.load_default()

    bold = ("segoeuib.ttf", "DejaVuSans-Bold.ttf", "Arial Bold.ttf")
    regular = ("segoeui.ttf", "DejaVuSans.ttf", "Arial.ttf")
    draw.text((64, 150), "Halgakos", font=font(bold, 92), fill=(255, 255, 255, 255))
    draw.text((66, 250), "Calculator", font=font(regular, 76), fill=(255, 255, 255, 255))
    draw.text((70, 362), "A calculator with a companion", font=font(regular, 30), fill=(255, 240, 238, 255))
    return banner.convert("RGB")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview")
    parser.add_argument("--pet", help="spritesheet-v2-smooth.webp from the Halgakos Codex pet repository")
    arguments = parser.parse_args()
    if arguments.pet:
        atlas, frame, counts = pet_atlas(arguments.pet)
        (RES / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
        atlas.save(RES / "drawable-nodpi" / "halgakos_pet.webp", lossless=True, quality=100, method=6)
        # Keep Companion.java's PET_FRAMES in step with these.
        print("pet atlas", atlas.size, "frame", frame, "frames per row", counts)
    source = clean(Image.open(SOURCE).convert("RGBA"))

    icon = foreground(source, 432)
    (RES / "mipmap-xxxhdpi").mkdir(parents=True, exist_ok=True)
    icon.save(RES / "mipmap-xxxhdpi" / "ic_launcher_foreground.png", optimize=True)

    figure = trimmed(source)
    width = round(figure.size[0] * SPRITE_HEIGHT / figure.size[1])
    sprite = figure.resize((width, SPRITE_HEIGHT), Image.LANCZOS)
    (RES / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
    sprite.save(RES / "drawable-nodpi" / "halgakos.png", optimize=True)

    # Play asks for a 512px icon with no transparency; it applies its own rounded mask.
    large = Image.alpha_composite(gradient(768), foreground(source, 768))
    STORE.mkdir(exist_ok=True)
    large.crop((128, 128, 640, 640)).convert("RGB").save(STORE / "icon-512.png", optimize=True)
    feature_graphic(source).save(STORE / "feature-graphic.png", optimize=True)

    if arguments.preview:
        out = pathlib.Path(arguments.preview)
        out.mkdir(parents=True, exist_ok=True)
        layers = Image.alpha_composite(gradient(432), icon)
        sheet = Image.new("RGBA", (288 * 3 + 80, 288 + 40), (236, 236, 236, 255))
        for index, shape in enumerate(("circle", "squircle", "square")):
            sheet.alpha_composite(masked(layers.copy(), shape), (20 + index * 308, 20))
        sheet.save(out / "icon-masks.png")
        for name, backdrop in (("light", (250, 250, 250)), ("dark", (23, 25, 27))):
            board = Image.new("RGBA", (sprite.size[0] + 80, SPRITE_HEIGHT + 80), backdrop + (255,))
            board.alpha_composite(sprite, (40, 40))
            board.save(out / f"sprite-{name}.png")
    print("icon", icon.size, "sprite", sprite.size)


if __name__ == "__main__":
    main()
