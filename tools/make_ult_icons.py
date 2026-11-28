"""
Draws the six ultimates' pictures for her skill tree, and sets them in the sheet of the notables' pictures, in a row of their own at its foot
(the notables are 96 pixels square, eight to a row, in the order of RunSkills.NOTABLES: the ultimates are the last six).

    python tools/make_ult_icons.py

Drawn in the style of the rest of the sheet: a dark night disk in a ring (pink for an active, with the four gold studs of the biggest
notables), a glow of the colour of each, and one bold thing in the middle: a red-flamed greatsword (berserk), a clock stopped on a cracked
glass (the stilled hour), a curling wave of ink (the tide), a golden hammer coming down on a cracked floor (the giant), three figures one
behind another (the legion), and a rose (the bloom). Drawn four times as large and brought down, so that the edges are smooth. Needs Pillow.
"""

import math
import pathlib

from PIL import Image, ImageChops, ImageDraw, ImageFilter

ROOT = pathlib.Path(__file__).resolve().parent.parent
SHEET = ROOT / "runart" / "src" / "main" / "assets" / "runart" / "tree_notables.webp"
CELL = 96
SCALE = 4
BIG = CELL * SCALE
COLUMNS = 8
FIRST = 176  # the index of the first ultimate's picture in the sheet


def layer():
    return Image.new("RGBA", (BIG, BIG), (0, 0, 0, 0))


def glow(image, colour, radius, strength=1.0):
    """The shapes of an image, blurred and tinted: a light round them."""
    alpha = image.getchannel("A").filter(ImageFilter.GaussianBlur(radius))
    alpha = alpha.point(lambda value: min(255, int(value * strength)))
    lit = Image.new("RGBA", image.size, colour + (0,))
    lit.putalpha(alpha)
    return lit


def disk(inner, outer):
    """The night disk: lighter toward the middle."""
    image = layer()
    draw = ImageDraw.Draw(image)
    centre = BIG / 2
    for step in range(60, 0, -1):
        t = step / 60
        colour = tuple(int(outer[i] + (inner[i] - outer[i]) * (1 - t)) for i in range(3)) + (255,)
        radius = BIG * .47 * t
        draw.ellipse((centre - radius, centre - radius, centre + radius, centre + radius), fill=colour)
    return image


def ring(colour, studs=True):
    """The ring: dark outside, the colour, a light edge, gold within; and gold studs at four places."""
    image = layer()
    draw = ImageDraw.Draw(image)
    c = BIG / 2
    for radius, width, paint in ((BIG * .495, BIG * .035, (30, 16, 40, 255)), (BIG * .47, BIG * .06, colour + (255,)), (BIG * .445, BIG * .018, (255, 214, 120, 255)),
                                 (BIG * .405, BIG * .014, (20, 12, 36, 255))):
        draw.ellipse((c - radius, c - radius, c + radius, c + radius), outline=paint, width=int(width))
    draw.arc((c - BIG * .47, c - BIG * .47, c + BIG * .47, c + BIG * .47), 200, 290, fill=(255, 255, 255, 120), width=int(BIG * .012))
    if studs:
        for turn in range(4):
            angle = turn * math.pi / 2 + math.pi / 4
            x, y = c + math.cos(angle) * BIG * .47, c + math.sin(angle) * BIG * .47
            r = BIG * .042
            draw.polygon([(x, y - r), (x + r, y), (x, y + r), (x - r, y)], fill=(255, 214, 90, 255), outline=(120, 60, 20, 255))
    return image


def polygon(image, points, fill, outline=None, width=0):
    ImageDraw.Draw(image).polygon([(x * SCALE, y * SCALE) for x, y in points], fill=fill, outline=outline, width=width * SCALE)


def circle(image, x, y, r, fill, outline=None, width=0):
    ImageDraw.Draw(image).ellipse(((x - r) * SCALE, (y - r) * SCALE, (x + r) * SCALE, (y + r) * SCALE), fill=fill, outline=outline, width=width * SCALE)


def line(image, a, b, fill, width):
    ImageDraw.Draw(image).line([(a[0] * SCALE, a[1] * SCALE), (b[0] * SCALE, b[1] * SCALE)], fill=fill, width=int(width * SCALE))


def rotated(points, about, degrees):
    c, s = math.cos(math.radians(degrees)), math.sin(math.radians(degrees))
    return [(about[0] + (x - about[0]) * c - (y - about[1]) * s, about[1] + (x - about[0]) * s + (y - about[1]) * c) for x, y in points]


def berserk():
    art = layer()
    # Red flames rising behind her sword: three tongues, the middle the tallest.
    for tip, base, hue in (((48, 8), 22, (255, 60, 40)), ((26, 22), 14, (230, 40, 40)), ((70, 22), 14, (230, 40, 40)), ((48, 22), 17, (255, 150, 50))):
        polygon(art, [(tip[0] - base, 82), (tip[0] - base * .5, 50), (tip[0] - 3, 34), (tip[0], tip[1]), (tip[0] + 3, 34), (tip[0] + base * .5, 50), (tip[0] + base, 82)], hue + (235,))
    # The greatsword, point up, the blade pale steel with a bright edge, the guard and grip gold and red.
    polygon(art, [(48, 6), (55, 18), (55, 62), (41, 62), (41, 18)], (214, 224, 238, 255), (60, 70, 90, 255), 1)
    polygon(art, [(48, 6), (48, 62), (41, 62), (41, 18)], (160, 176, 200, 255))
    line(art, (48, 12), (48, 60), (255, 255, 255, 255), 1.2)
    polygon(art, [(30, 62), (66, 62), (62, 69), (34, 69)], (255, 200, 70, 255), (110, 60, 10, 255), 1)
    polygon(art, [(45, 69), (51, 69), (51, 82), (45, 82)], (150, 40, 40, 255), (60, 10, 10, 255), 1)
    circle(art, 48, 86, 4, (255, 200, 70, 255), (110, 60, 10, 255), 1)
    out = glow(art, (255, 40, 30), 14, 1.6)
    out.alpha_composite(art)
    return out


def stilled():
    art = layer()
    circle(art, 48, 48, 30, (226, 236, 246, 255), (90, 110, 140, 255), 3)
    circle(art, 48, 48, 25, (190, 206, 226, 255))
    for tick in range(12):
        a = math.radians(tick * 30 - 90)
        inner = 21 if tick % 3 == 0 else 23
        line(art, (48 + math.cos(a) * inner, 48 + math.sin(a) * inner), (48 + math.cos(a) * 25, 48 + math.sin(a) * 25), (40, 52, 80, 255), 2 if tick % 3 == 0 else 1)
    # Hands stopped at a quarter past, and a crack of white across the glass.
    line(art, (48, 48), (48, 29), (30, 40, 70, 255), 3)
    line(art, (48, 48), (62, 52), (30, 40, 70, 255), 3)
    circle(art, 48, 48, 3.5, (255, 200, 70, 255), (110, 60, 10, 255), 1)
    for a, b in (((70, 24), (56, 40)), ((56, 40), (62, 46)), ((62, 46), (50, 62)), ((50, 62), (54, 76))):
        line(art, a, b, (255, 255, 255, 230), 1.6)
    # Shards of time hanging round it.
    for centre, size in (((18, 28), 6), ((78, 66), 7), ((22, 70), 5), ((76, 24), 5)):
        polygon(art, [(centre[0], centre[1] - size), (centre[0] + size * .6, centre[1]), (centre[0], centre[1] + size * 1.3), (centre[0] - size * .6, centre[1])], (170, 220, 255, 235), (240, 250, 255, 255), 1)
    out = glow(art, (150, 200, 255), 10, 1.4)
    out.alpha_composite(art)
    return out


def arc(image, x, y, r, start, end, fill, width):
    ImageDraw.Draw(image).arc(((x - r) * SCALE, (y - r) * SCALE, (x + r) * SCALE, (y + r) * SCALE), start, end, fill=fill, width=int(width * SCALE))


def tide():
    art = layer()
    # A wave: the swell of ink along the foot, and over it a crest curling in a ring, foam on the lip, drops flung off it.
    swell = [(10, 86)]
    for step in range(0, 25):
        t = step / 24
        swell.append((10 + 76 * t, 64 - 10 * math.sin(t * math.pi * 1.5 + .6)))
    swell.append((86, 86))
    polygon(art, swell, (30, 60, 170, 255), (10, 20, 80, 255), 1)
    arc(art, 54, 44, 22, 170, 400, (20, 44, 150, 255), 11)
    arc(art, 54, 44, 22, 170, 400, (90, 140, 255, 255), 3)
    arc(art, 54, 44, 27, 250, 400, (236, 244, 255, 255), 3)
    for x, y, r in ((74, 24, 4), (80, 34, 3), (64, 20, 3.5), (46, 22, 3)):
        circle(art, x, y, r, (236, 244, 255, 255))
    for x, y, r in ((20, 44, 3), (28, 34, 2.2), (14, 54, 2)):
        circle(art, x, y, r, (20, 24, 90, 255), (150, 170, 255, 255), 1)
    out = glow(art, (70, 110, 255), 10, 1.3)
    out.alpha_composite(art)
    return out

def titan():
    art = layer()
    # Cracks in the floor, spreading from where it lands.
    for end in ((14, 80), (26, 88), (70, 88), (82, 80), (48, 90), (10, 70), (86, 70)):
        line(art, (48, 76), end, (60, 36, 20, 255), 2.4)
        line(art, (48, 76), end, (255, 200, 90, 255), .9)
    # A war hammer, huge, held high: head a block of gold with a darker face, a long haft.
    polygon(art, [(44, 32), (52, 32), (53, 78), (43, 78)], (130, 78, 40, 255), (50, 26, 10, 255), 1)
    polygon(art, [(22, 12), (74, 12), (78, 18), (78, 32), (74, 36), (22, 36), (18, 32), (18, 18)], (255, 200, 70, 255), (110, 60, 10, 255), 2)
    polygon(art, [(22, 12), (74, 12), (78, 18), (18, 18)], (255, 238, 150, 255))
    polygon(art, [(18, 28), (78, 28), (78, 32), (74, 36), (22, 36), (18, 32)], (200, 140, 30, 255))
    circle(art, 48, 24, 5, (255, 120, 50, 255), (110, 40, 10, 255), 1)
    for x in (24, 34, 62, 72):
        line(art, (x, 42), (x, 52), (255, 230, 150, 160), 1.6)
    out = glow(art, (255, 190, 60), 10, 1.3)
    out.alpha_composite(art)
    return out


def figure(art, x, y, size, body, edge, bright=0):
    circle(art, x, y - size * .9, size * .34, body, edge, 1)
    polygon(art, [(x - size * .5, y + size * .8), (x - size * .32, y - size * .45), (x + size * .32, y - size * .45), (x + size * .5, y + size * .8)], body, edge, 1)
    if bright:
        circle(art, x - size * .12, y - size * .98, size * .08, (255, 255, 255, 255))
        circle(art, x + size * .12, y - size * .98, size * .08, (255, 255, 255, 255))


def legion():
    art = layer()
    # Three echoes behind her, each fainter, each a step further to the side; her in front, bright.
    figure(art, 20, 48, 20, (110, 80, 200, 150), (200, 170, 255, 170))
    figure(art, 76, 48, 20, (110, 80, 200, 150), (200, 170, 255, 170))
    figure(art, 34, 52, 24, (150, 110, 235, 210), (220, 200, 255, 220))
    figure(art, 62, 52, 24, (150, 110, 235, 210), (220, 200, 255, 220))
    figure(art, 48, 56, 30, (255, 255, 255, 255), (150, 110, 235, 255), 1)
    polygon(art, [(36, 74), (60, 74), (58, 78), (38, 78)], (255, 200, 70, 255), (110, 60, 10, 255), 1)
    out = glow(art, (170, 120, 255), 10, 1.4)
    out.alpha_composite(art)
    return out


def bloom():
    art = layer()
    # Leaves, then a rose: rings of petals, the outer pink and the inner deeper, with a coil of the bud in the middle.
    for turn in (-1, 1):
        polygon(art, [(48, 74), (48 + turn * 6, 62), (48 + turn * 28, 62), (48 + turn * 34, 74), (48 + turn * 20, 82)], (60, 170, 90, 255), (20, 90, 50, 255), 1)
    for count, radius, colour, edge, offset in ((7, 26, (255, 126, 170, 255), (140, 30, 80, 255), 0), (6, 19, (240, 80, 140, 255), (130, 20, 70, 255), 20), (5, 12, (205, 50, 110, 255), (110, 10, 60, 255), 36)):
        for petal in range(count):
            a = math.radians(petal * 360 / count + offset - 90)
            circle(art, 48 + math.cos(a) * radius * .55, 44 + math.sin(a) * radius * .55, radius * .62, colour, edge, 1)
    circle(art, 48, 44, 7, (170, 30, 90, 255), (100, 10, 50, 255), 1)
    line(art, (44, 44), (52, 44), (255, 170, 200, 255), 1.4)
    # Sparkles of light round it.
    for x, y, r in ((14, 20, 4), (82, 24, 5), (80, 74, 3), (14, 72, 3.4)):
        polygon(art, [(x, y - r * 1.6), (x + r * .4, y - r * .4), (x + r * 1.6, y), (x + r * .4, y + r * .4), (x, y + r * 1.6), (x - r * .4, y + r * .4), (x - r * 1.6, y), (x - r * .4, y - r * .4)], (255, 250, 220, 255))
    out = glow(art, (255, 110, 170), 11, 1.4)
    out.alpha_composite(art)
    return out


# (The glyph, the disk's light and dark, the ring's colour.)
ICONS = [
    (berserk, (70, 14, 24), (22, 6, 16), (232, 60, 90)),
    (stilled, (30, 44, 80), (10, 14, 34), (200, 90, 170)),
    (tide, (14, 24, 70), (6, 8, 26), (200, 90, 170)),
    (titan, (60, 40, 22), (22, 14, 12), (232, 70, 110)),
    (legion, (48, 30, 90), (14, 10, 36), (200, 90, 200)),
    (bloom, (74, 24, 56), (24, 8, 28), (240, 100, 160)),
]


def make(glyph, inner, outer, ring_colour):
    base = disk(inner, outer)
    # The glyph is kept inside the disk.
    art = glyph()
    mask = Image.new("L", (BIG, BIG), 0)
    ImageDraw.Draw(mask).ellipse((BIG * .06, BIG * .06, BIG * .94, BIG * .94), fill=255)
    art.putalpha(ImageChops.multiply(art.getchannel("A"), mask))
    base.alpha_composite(art)
    base.alpha_composite(ring(ring_colour))
    return base.resize((CELL, CELL), Image.LANCZOS)


def main():
    sheet = Image.open(SHEET).convert("RGBA")
    rows = (FIRST + len(ICONS) + COLUMNS - 1) // COLUMNS
    height = rows * CELL
    if sheet.height < height:
        grown = Image.new("RGBA", (sheet.width, height), (0, 0, 0, 0))
        grown.paste(sheet, (0, 0))
        sheet = grown
    for index, (glyph, inner, outer, ring_colour) in enumerate(ICONS):
        at = FIRST + index
        x, y = at % COLUMNS * CELL, at // COLUMNS * CELL
        sheet.paste((0, 0, 0, 0), (x, y, x + CELL, y + CELL))
        sheet.alpha_composite(make(glyph, inner, outer, ring_colour), (x, y))
    sheet.save(SHEET, "WEBP", quality=94, method=6)
    print("saved", SHEET, sheet.size, SHEET.stat().st_size, "bytes")


if __name__ == "__main__":
    main()
