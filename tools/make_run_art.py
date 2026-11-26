"""
Builds the art of Plus run from the pictures in assets/plus-run.

    python tools/make_run_art.py [--preview out.png]

The pictures were made with an image model (gpt-6-astra, through Glossarion), from prompts
that ask for backgrounds, ground textures, sheets of sprites on a transparent background, and
six frames of Halgakos jumping drawn from her own running frames. This cuts them up:

  * sky1.png to sky5.png become res/drawable-nodpi/plus_run_sky1.webp to 5, one a chapter.
  * ground1.png to ground5.png each give two square tiles: the top of the ground, with its
    turf, and what lies under it.
  * minus.png, items.png, things.png, foes.png, zero.png and weapons.png give the sprites.
  * jump.png gives her six jump frames, brought to the pink and the size of her other frames.
  * buffs.png has the things that help her, and chikuwa.png,
    rune.png, illya.png and tsubakieh.png are her friends and the two she has to get past,
    drawn by the model from their own chibi pictures in the style of her game frames. The
    pictures in faces/ are those chibi pictures themselves, made small for the story cards.
  * arm_pen.png and the five like it each give eight frames of her carrying one of the things
    she fights with: standing, four of running, rising, falling, and using it.
  * party.png gives six of her celebrating a birthday, for the companion: res/drawable-nodpi/halgakos_party.webp.
  * lose.png gives six more of her, for when she is hurt and when she has lost: hit, knocked
    into the air, falling, sitting dazed, sulking and crying. They are treated the same way.

Tiles and sprites go side by side into res/drawable-nodpi/plus_run_art.webp, each in a square
cell, standing on the bottom of it and centred; how much of its cell each one fills is
printed, to be copied into RunGame.java. Her jump frames go into halgakos_jump.webp
and her losing frames into halgakos_lose.webp. Needs Pillow.
"""

import argparse
import colorsys
import pathlib

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "assets" / "plus-run"
RES = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
CELL = 128
SKY_WIDTH = 1024
CHAPTERS = 5
# How far from a magenta background a colour has to be before it is kept whole, and how near
# before it is dropped: between the two an edge fades out. Only the first sheet, minus.png,
# came on magenta; the rest came with nothing behind them.
KEEP, DROP = 40, 150
# Her jump frames: the cell each goes in, and how many frames her atlas has across and down,
# from which the size of her other frames is taken.
JUMP_CELL = (256, 208)
LOSE_CELL = (288, 224)
ARM_CELL = (304, 248)
# Her birthday frames, for the companion: each the size of one of her own frames, so she is drawn the same size.
PARTY_CELL = (173, 202)
ARMS = ("pen", "knife", "book", "brush", "sword", "gun")
# The brush stands over her head and is taken for part of her horns, which would make her too small: on that sheet
# she is given the size she has on the sheets drawn to the same layout.
ARM_SCALE = {"arm3": .505}
# Her friends and foes from the webtoon: how many rows and frames across each sheet has, and the cell each frame goes in.
CAST = {"chikuwa": (1, 3), "rune": (1, 3), "illya": (1, 4), "tsubakieh": (2, 3)}
CAST_CELL = (288, 248)
# How tall the first frame of each is made, in the pixels her own frames are measured in: she stands 190 tall.
CAST_TALL = {"chikuwa": 198, "rune": 190, "illya": 188, "tsubakieh": 190}
FACES = ("chikuwa", "rune", "illya", "tsubakieh", "mawang")
FACE_TALL = 520
PET_GRID = (8, 9)


def keyed(image):
    """A sprite sheet with its magenta background made clear, and the magenta taken out of the edges."""
    image = image.convert("RGBA")
    pixels = image.load()
    for y in range(image.size[1]):
        for x in range(image.size[0]):
            red, green, blue, _ = pixels[x, y]
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


def solid(image):
    return image.getchannel("A").point(lambda value: 255 if value > 60 else 0)


def runs(filled, gap, least):
    """The stretches of a row of yes and no that are yes, a gap of some length ending each."""
    found, start, empty = [], None, 0
    for place, here in enumerate(list(filled) + [False] * gap):
        if here:
            if start is None:
                start = place
            empty = 0
        elif start is not None:
            empty += 1
            if empty >= gap:
                found.append((start, place - empty + 1))
                start = None
    return [run for run in found if run[1] - run[0] >= least]


def sprites(sheet, expected):
    """The separate sprites of a sheet laid out in one row, left to right, each trimmed to what is drawn."""
    mask = solid(sheet)
    columns = [mask.crop((x, 0, x + 1, sheet.size[1])).getbbox() is not None for x in range(sheet.size[0])]
    found = runs(columns, 14, 30)
    if len(found) > expected:
        # Something small drawn apart from a sprite, a knife just thrown, say, is left out: the widest are the sprites.
        widest = sorted(found, key=lambda run: run[0] - run[1])[:expected]
        found = sorted(widest)
    if len(found) != expected:
        raise SystemExit(f"expected {expected} sprites, found {len(found)}: {found}")
    pieces = []
    for left, right in found:
        piece = sheet.crop((left, 0, right, sheet.size[1]))
        pieces.append(piece.crop(solid(piece).getbbox()))
    return pieces


def cell(sprite):
    """A sprite scaled to fit a cell, standing on the bottom of it and centred; and how much of the cell it fills."""
    scale = CELL / max(sprite.size)
    size = (max(1, round(sprite.size[0] * scale)), max(1, round(sprite.size[1] * scale)))
    small = sprite.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
    square = Image.new("RGBA", (CELL, CELL), (0, 0, 0, 0))
    square.alpha_composite(small, ((CELL - size[0]) // 2, CELL - size[1]))
    return square, size[0] / CELL, size[1] / CELL


# Her jump frames

def pinkness(pixel):
    """How much a pixel belongs to the pinks of her hair and tail, from 0 to 1: as in tools/make_art.py."""
    red, green, blue, alpha = pixel
    if alpha == 0:
        return 0.0
    hue, saturation, value = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
    if hue >= .5:
        by_hue = min(1.0, max(0.0, (hue - .86) / .04))
    else:
        by_hue = min(1.0, max(0.0, (.055 - hue) / .02))
    return by_hue * min(1.0, max(0.0, (saturation - .12) / .1)) * min(1.0, max(0.0, (value - .35) / .15))


def hair_colour(frames):
    """The mean colour of her hair over some frames: the pinks of the upper part of each."""
    total, count = [0, 0, 0], 0
    for frame in frames:
        box = solid(frame).getbbox()
        pixels = frame.load()
        for y in range(box[1], box[1] + (box[3] - box[1]) * 55 // 100):
            for x in range(box[0], box[2]):
                if pixels[x, y][3] >= 220 and pinkness(pixels[x, y]) >= 1:
                    total = [total[i] + pixels[x, y][i] for i in range(3)]
                    count += 1
    return [channel / count for channel in total]


def recoloured(frame, gains):
    result = frame.copy()
    pixels = result.load()
    for y in range(frame.size[1]):
        for x in range(frame.size[0]):
            pixel = pixels[x, y]
            weight = pinkness(pixel)
            if weight > 0:
                pixels[x, y] = tuple(min(255, round(pixel[i] * (1 + (gains[i] - 1) * weight))) for i in range(3)) + (pixel[3],)
    return result


def is_horn(pixel):
    red, green, blue, alpha = pixel
    return alpha > 200 and red < 125 and green < 95 and 55 < blue < 135 and blue >= green + 12 and red >= green


def horn_span(frame):
    """How far apart the outer edges of her horns are: a measure of how large she is drawn."""
    pixels = frame.load()
    box = solid(frame).getbbox()
    across = [x for y in range(box[1], box[1] + (box[3] - box[1]) // 2) for x in range(box[0], box[2]) if is_horn(pixels[x, y])]
    return max(across) - min(across) if across else 0


def skirt_middle(frame):
    """Where her skirt is across the frame: the middle of her body, whatever her hair is doing."""
    pixels = frame.load()
    box = solid(frame).getbbox()
    across = [x for y in range(box[1], box[3]) for x in range(box[0], box[2])
              if pixels[x, y][3] > 200 and pixels[x, y][2] > pixels[x, y][0] + 22 and pixels[x, y][2] > pixels[x, y][1] + 8
              and pixels[x, y][2] < 175 and pixels[x, y][0] < 110]
    return sum(across) / len(across) if across else (box[0] + box[2]) / 2


def rows_of(sheet, gap):
    """The rows of sprites of a sheet, from the top: where each begins and ends."""
    mask = solid(sheet)
    return runs([mask.crop((0, y, sheet.size[0], y + 1)).getbbox() is not None for y in range(sheet.size[1])], gap, 60)


def cast_frames(name):
    """The frames of one of the others, in a row of cells, standing on the bottom of each, at her size."""
    down, across = CAST[name]
    sheet = cleared(Image.open(SOURCE / f"{name}.png"))
    sheet.putalpha(sheet.getchannel("A").point(lambda value: 0 if value < 48 else value))
    rows = rows_of(sheet, 30)
    if len(rows) != down:
        raise SystemExit(f"expected {down} rows of {name}, found {len(rows)}")
    frames = []
    for top, bottom in rows:
        frames += sprites(sheet.crop((0, top, sheet.size[0], bottom)), across)
    scale = CAST_TALL[name] / frames[0].size[1]
    strip = Image.new("RGBA", (CAST_CELL[0] * len(frames), CAST_CELL[1]), (0, 0, 0, 0))
    for index, frame in enumerate(frames):
        size = (max(1, round(frame.size[0] * scale)), max(1, round(frame.size[1] * scale)))
        if size[0] > CAST_CELL[0] or size[1] > CAST_CELL[1]:
            raise SystemExit(f"{name} frame {index} is {size}, larger than its cell {CAST_CELL}")
        small = frame.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
        # Her feet in the middle of the cell: the middle of what is drawn in the lowest fifth of her.
        feet = solid(small).crop((0, size[1] * 4 // 5, size[0], size[1])).getbbox()
        middle = (feet[0] + feet[2]) / 2 if feet else size[0] / 2
        left = max(0, min(CAST_CELL[0] - size[0], round(CAST_CELL[0] / 2 - middle)))
        strip.alpha_composite(small, (index * CAST_CELL[0] + left, CAST_CELL[1] - size[1]))
    clear = Image.new("RGBA", strip.size, (0, 0, 0, 0))
    strip = Image.composite(strip, clear, strip.getchannel("A").point(lambda value: 255 if value else 0))
    strip.save(RES / f"run_{name}.webp", lossless=True, quality=100, method=6, exact=True)
    return strip


def face(name):
    """One of the chibi pictures the webtoon's cast was drawn from, small, for the cards the story is told on."""
    from PIL import ImageDraw

    image = Image.open(SOURCE / "faces" / f"{name}.png").convert("RGBA")
    if image.getchannel("A").getextrema()[0] == 255:
        # On white: the white round her is made clear, from the corners in.
        for corner in ((0, 0), (image.size[0] - 1, 0), (0, image.size[1] - 1), (image.size[0] - 1, image.size[1] - 1)):
            ImageDraw.floodfill(image, corner, (255, 255, 255, 0), thresh=40)
    image = image.crop(solid(image).getbbox())
    image = image.convert("RGBa").resize((round(image.size[0] * FACE_TALL / image.size[1]), FACE_TALL), Image.LANCZOS).convert("RGBA")
    image.save(RES / f"run_face_{name}.webp", quality=88, method=6)
    return image


def her_frames(name, source, box, gap, across, report):
    """Frames of her from a sheet of two rows, in one row, in the pink and at the size of her running frames."""
    sheet = cleared(Image.open(SOURCE / f"{source}.png"))
    # The faint haze the model leaves round her is taken off, so she has no glow about her in the game.
    sheet.putalpha(sheet.getchannel("A").point(lambda value: 0 if value < 48 else value))
    mask = solid(sheet)
    # For her losing frames the gap is wide enough that the stars over her head stay with her.
    rows = runs([mask.crop((0, y, sheet.size[0], y + 1)).getbbox() is not None for y in range(sheet.size[1])], gap, 60)
    if len(rows) != 2:
        raise SystemExit(f"expected 2 rows of {name} frames, found {len(rows)}")
    frames = []
    for top, bottom in rows:
        strip = sheet.crop((0, top, sheet.size[0], bottom))
        frames += sprites(strip, across)
    pet = Image.open(RES / "halgakos_pet.webp").convert("RGBA")
    wide, tall = pet.size[0] // PET_GRID[0], pet.size[1] // PET_GRID[1]
    idle = [pet.crop((0, 0, wide, tall))]
    running = [pet.crop((column * wide, tall, column * wide + wide, tall * 2)) for column in range(8)]
    # Her pinks, brought to the idle's as every other row of hers is, three times over.
    wanted = hair_colour(idle)
    report[name + "_hair_wanted"] = [round(value, 1) for value in wanted]
    report[name + "_hair_before"] = [round(value, 1) for value in hair_colour(frames)]
    for _ in range(3):
        has = hair_colour(frames)
        frames = [recoloured(frame, [wanted[i] / has[i] for i in range(3)]) for frame in frames]
    report[name + "_hair_after"] = [round(value, 1) for value in hair_colour(frames)]
    report[name + "_hair_running"] = [round(value, 1) for value in hair_colour(running)]
    # Her size, by how far apart her horns stand, against her running frames.
    # Measured again once they are small, since a horn's edge blurs as it shrinks, and the size set right by that.
    def middle(values):
        ordered = sorted(values)
        return (ordered[(len(ordered) - 1) // 2] + ordered[len(ordered) // 2]) / 2

    usual = middle(horn_span(frame) for frame in running)
    scale = usual / middle(horn_span(frame) for frame in frames)
    for _ in range(3):
        small = [frame.convert("RGBa").resize((max(1, round(frame.size[0] * scale)), max(1, round(frame.size[1] * scale))),
                                              Image.LANCZOS).convert("RGBA") for frame in frames]
        scale *= usual / middle(horn_span(frame) for frame in small)
    scale = ARM_SCALE.get(name, scale)
    report[name + "_horn_span_running"] = usual
    report[name + "_scale"] = round(scale, 4)
    strip = Image.new("RGBA", (box[0] * len(frames), box[1]), (0, 0, 0, 0))
    spans = []
    for index, frame in enumerate(frames):
        size = (max(1, round(frame.size[0] * scale)), max(1, round(frame.size[1] * scale)))
        small = frame.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA")
        spans.append(horn_span(small))
        # Her skirt in the middle of the cell, her lowest point on the bottom of it.
        left = round(box[0] / 2 - skirt_middle(small))
        left = max(0, min(box[0] - size[0], left))
        if size[0] > box[0] or size[1] > box[1]:
            raise SystemExit(f"{name} frame {index} is {size}, larger than its cell {box}")
        strip.alpha_composite(small, (index * box[0] + left, box[1] - size[1]))
    report[name + "_horn_spans"] = spans
    report[name + "_sizes"] = [(round(frame.size[0] * scale), round(frame.size[1] * scale)) for frame in frames]
    clear = Image.new("RGBA", strip.size, (0, 0, 0, 0))
    strip = Image.composite(strip, clear, strip.getchannel("A").point(lambda value: 255 if value else 0))
    strip.save(RES / f"halgakos_{name}.webp", lossless=True, quality=100, method=6, exact=True)
    return strip, running


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", type=pathlib.Path)
    args = parser.parse_args()

    cells, wide, tall = [], [], []
    skies = []
    for chapter in range(1, CHAPTERS + 1):
        sky = Image.open(SOURCE / f"sky{chapter}.png").convert("RGB")
        sky = sky.resize((SKY_WIDTH, round(sky.size[1] * SKY_WIDTH / sky.size[0])), Image.LANCZOS)
        sky.save(RES / f"plus_run_sky{chapter}.webp", quality=82, method=6)
        skies.append(sky)
        ground = Image.open(SOURCE / f"ground{chapter}.png").convert("RGBA")
        side = ground.size[0]
        # The turf lies along the top fifth: a tile three quarters of the picture high has it as its top quarter.
        cells.append(ground.crop((side // 8, 0, side // 8 + side * 3 // 4, side * 3 // 4)).resize((CELL, CELL), Image.LANCZOS))
        cells.append(ground.crop((side // 8, side * 3 // 8, side // 8 + side // 2, side * 7 // 8)).resize((CELL, CELL), Image.LANCZOS))
        wide += [1.0, 1.0]
        tall += [1.0, 1.0]
    for name, count in (("minus", 3), ("items", 4), ("things", 4), ("foes", 4), ("zero", 3), ("weapons", 6), ("buffs", 6)):
        for sprite in sprites(cleared(Image.open(SOURCE / f"{name}.png")), count):
            square, across, up = cell(sprite)
            cells.append(square)
            wide.append(across)
            tall.append(up)
    atlas = Image.new("RGBA", (CELL * len(cells), CELL), (0, 0, 0, 0))
    for index, square in enumerate(cells):
        atlas.alpha_composite(square.convert("RGBA"), (index * CELL, 0))
    atlas.save(RES / "plus_run_art.webp", lossless=True, quality=100, method=6)

    report = {}
    jump, running = her_frames("jump", "jump", JUMP_CELL, 14, 3, report)
    lose, _ = her_frames("lose", "lose", LOSE_CELL, 40, 3, report)
    armed = [her_frames(f"arm{index}", f"arm_{name}", ARM_CELL, 14, 4, report)[0] for index, name in enumerate(ARMS)]
    her_frames("party", "party", PARTY_CELL, 14, 3, report)
    others = [cast_frames(name) for name in CAST]
    faces = [face(name) for name in FACES]
    print("atlas", atlas.size, "cells", len(cells), "jump", jump.size, "lose", lose.size, "armed", armed[0].size,
          "cast", [strip.size for strip in others], "faces", [image.size for image in faces])
    print("ART_WIDE = {" + ", ".join(f"{value:.3f}f" for value in wide) + "};")
    print("ART_TALL = {" + ", ".join(f"{value:.3f}f" for value in tall) + "};")
    for key, value in report.items():
        print(key, value)
    if args.preview:
        grey = (120, 120, 130, 255)
        half = atlas.resize((atlas.size[0] // 2, atlas.size[1] // 2), Image.LANCZOS)
        board = Image.new("RGBA", (max(half.size[0], lose.size[0], armed[0].size[0]),
                                   half.size[1] + JUMP_CELL[1] * 2 + LOSE_CELL[1] + 30 + (ARM_CELL[1] + 4) * len(armed) + (CAST_CELL[1] + 4) * 2), grey)
        board.alpha_composite(half, (0, 0))
        board.alpha_composite(jump, (0, half.size[1] + 10))
        # Her running frames under her jump frames, at the same size, to see that they are one girl.
        for index, frame in enumerate(running[:6]):
            board.alpha_composite(frame, (index * JUMP_CELL[0] + (JUMP_CELL[0] - frame.size[0]) // 2, half.size[1] + 10 + JUMP_CELL[1] + (JUMP_CELL[1] - frame.size[1])))
        board.alpha_composite(lose, (0, half.size[1] + 20 + JUMP_CELL[1] * 2))
        down = half.size[1] + 30 + JUMP_CELL[1] * 2 + LOSE_CELL[1]
        for strip in armed:
            board.alpha_composite(strip, (0, down))
            down += ARM_CELL[1] + 4
        across = 0
        for index, strip in enumerate(others):
            if index == 2:
                across, down = 0, down + CAST_CELL[1] + 4
            board.alpha_composite(strip, (across, down))
            across += strip.size[0] + 8
        board.convert("RGB").save(args.preview)


if __name__ == "__main__":
    main()
