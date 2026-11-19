"""Derive the launcher icon, the companion sprite and the store icon from the Halgakos art.

Usage: python tools/make_art.py [--preview DIR]

Reads assets/HalgakosWhite.png and rewrites the generated images under app/src/main/res
and store/. With --preview it also writes the icon under the masks launchers apply.
"""
import argparse
import pathlib

from PIL import Image, ImageChops, ImageDraw

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


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview")
    arguments = parser.parse_args()
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
