"""Derive the launcher icon, the companion sprite and the store icon from the Halgakos art.

Usage: python tools/make_art.py [--preview DIR] [--pet SHEET]

Reads assets/HalgakosWhite.png and rewrites the generated images under app/src/main/res
and store/. With --preview it also writes the icon under the masks launchers apply. With
--pet it also rebuilds the animated companion's frames from the Codex pet atlas, which is
kept in the Halgakos-Codex-Pet repository rather than here.
"""
import argparse
import pathlib

from PIL import Image, ImageChops, ImageDraw, ImageFont

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
# they take in its own atlas: idle, running right, running left, waving, jumping, failed, working.
PET_CELL = (192, 208)
PET_COLUMNS = 8
PET_ROWS = (0, 1, 2, 3, 4, 5, 7)


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
