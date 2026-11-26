"""
Builds the art of Page brawl from the pictures in assets/fight.

    python tools/make_fight_art.py [--preview FOLDER] [--only NAME ...]

The pictures were made with an image model (gpt-6-astra, through Glossarion) on transparent
backgrounds, each fighter drawn from her own non-chibi design sheet of the webtoon:

  * NAME/SHEET.webp are a fighter's frames: twenty-eight sheets of six, three across and two down.
    Eight are of how she moves (idle and guard, walking, walking back, dashing, jumping, being
    hit, going down and getting up, winning and losing) and eight of how she fights (the five
    blows of her quick chain, of her heavy chain, in the air, the smash, her specials, her
    other blows, her ultimate, and her dodge and the greater ultimate of a clash).
  * NAME_move.webp and NAME_hits.webp are the two sheets of six she had at first. A fighter
    who does not yet have all her new sheets is built from these, the same frame standing in
    for many.
  * fx.webp is ten things that fly and flash, five across and two down.
  * stage1.webp to stage3.webp are the stages.
  * face_NAME.png is each one's design sheet, made small, from which her portrait is cut.

The model does not keep to an exact grid, so a sheet is taken apart by what touches what:
its six largest pieces are the figures, and every smaller piece, a thrown roll or a drop of
ink, goes with the figure it is nearest. Every sheet has a frame in which she stands upright,
by which the sheet is brought to her height; her hair is brought to one colour across all her
sheets; each frame is trimmed to what is drawn and the frames are packed close together into
res/drawable-nodpi/fight_NAME_0.webp, _1 and so on. Where each frame is, and where her feet
are in it, is written to FightFrames.java, with a name for every frame. The things that fly go
into fight_fx.webp, the portraits into fight_faces.webp, and the stages into fight_stage1.webp
and so on. Needs Pillow, numpy and scipy.
"""

import argparse
import pathlib

import numpy
import scipy.ndimage
from PIL import Image, ImageDraw, ImageFilter

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "assets" / "fight"
RES = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
JAVA = ROOT / "app" / "src" / "main" / "java" / "com" / "coral" / "calculator" / "FightFrames.java"
FIGHTERS = ("halgakos", "chikuwa", "tsubakieh", "rune", "illya")
# How tall she is standing, in the pixels her frames are kept in: large enough that she is not stretched to more
# than about one and a half times her size on a big screen.
STAND = 393
ATLAS = 2048
FX_CELL = 240
FACE = 256
STAGE_WIDTH = 1920
STAGES = 3

# The sheets, the names of the six frames of each, and which of them show her upright at her full height.
SHEETS = [
    ("idle", "IDLE1 IDLE2 IDLE3 IDLE4 GUARD GUARD_HIT", (0, 1, 2, 3)),
    ("walk", "WALK1 WALK2 WALK3 WALK4 WALK5 WALK6", (0, 1, 2, 3, 4, 5)),
    ("back", "BACK1 BACK2 BACK3 BACK4 HOP1 HOP2", (0, 1, 2, 3)),
    ("dash", "TURN DASH1 DASH2 DASH3 SKID CROUCH", (0,)),
    ("jump", "READY PREJUMP RISE TOP FALL LAND", (0,)),
    ("hits", "HIT1 HIT2 BLOWN STAGGER AIR_HIT AIR_GUARD", (0,)),
    ("down", "FALLING LYING SIT_UP KNEEL_UP DUST KO", (4,)),
    ("win", "WIN1 WIN2 WIN3 LOSE1 LOSE2 TAUNT", (0,)),
    ("quick", "QS Q1 Q2 Q3 Q4 Q5", (0,)),
    ("heavy", "HS H1 H2 H3 H4 H5", (0,)),
    ("air", "AS A1 A2 A3 AX1 AX2", (0,)),
    ("smash", "SM_UP SM_FALL SM_HIT SM_REC SM_RISE SM_MISS", (4,)),
    ("special", "SS S1 S2 S3 S4 S5", (0,)),
    ("moves", "MS DASH_Q DASH_H BREAK LAUNCH SHOVE", (0,)),
    ("ult", "U1 U2 U3 U4 CHARGE TAUNT2", (0,)),
    ("extra", "XS DODGE1 DODGE2 C1 C2 C3", (0,)),
    # Her dodges that go somewhere: back away from the other (its start, its furthest, its end), forward past her, and in the air.
    ("slip", "SLIP_S BDODGE1 BDODGE2 BDODGE3 FDODGE ADODGE", (0,)),
    # The frames that go between two different poses of something that happens once: the guard coming up and a turn half
    # made; the jump's in-betweens; coming round from a blow and getting up; then for every blow the wind-up before it
    # and the follow-through after it. There are none for what loops or is held: several drawings of one pose never
    # agree in their small parts, and swapped quickly they flicker. Standing is the four frames of her stance that were
    # drawn together, a walk its own six.
    ("back2", "BB1 BB2 BB3 BB4 GUARD_UP TURN2", (0, 1, 2, 3)),
    ("jump2", "J_S PUSH RISE2 TOP2 FALL2 LAND2", (0,)),
    ("hits2", "H_S HIT1R HIT2R STAGGER2 PROP HALF_UP", (0,)),
    ("quick2", "QW_S QW1 QW2 QW3 QW4 QW5", (0,)),
    ("quick3", "QF_S QF1 QF2 QF3 QF4 QF5", (0,)),
    ("heavy2", "HW_S HW1 HW2 HW3 HW4 HW5", (0,)),
    ("heavy3", "HF_S HF1 HF2 HF3 HF4 HF5", (0,)),
    ("air2", "A_S AF1 AF2 AF3 AW SM_TIP", (0,)),
    ("moves2", "MW_S DASH_QW DASH_HW BREAK_W LAUNCH_W SHOVE_W", (0,)),
    ("special2", "SW_S SW1 SW2 SW3 SW4 SW5", (0,)),
    ("ult2", "UW_S U12 U23 U34 U4R SF", (0,)),
]
# The sheets of in-betweens, which a fighter may be without: what she had before stands in for them.
MORE = [sheet for sheet, _, _ in SHEETS[17:]]
# A frame that is only there to give its sheet her height is not kept; nor is one that is no longer shown: the
# halves of the walk backward, and the second stagger.
DROP = "SLIP_S J_S H_S QW_S QF_S HW_S HF_S A_S MW_S SW_S UW_S BB1 BB2 BB3 BB4 STAGGER2".split()
# An in-between is as tall as the two frames it goes between are, taken together: these are the two, for each frame of a sheet.
BETWEENS = {
    "back2": [(f"BACK{n}", f"BACK{n % 4 + 1}") for n in range(1, 5)],
}
NAMES = [name for _, names, _ in SHEETS for name in names.split()]
# Frames that are placed by the middle of what is drawn, not by where her feet are: in the air, lying down, and
# leaning far out of a dodge.
CENTRED = set("HOP2 RISE TOP FALL BLOWN AIR_HIT AIR_GUARD FALLING LYING KO A1 A2 A3 AX1 AX2 SM_UP SM_FALL DODGE2 ADODGE BDODGE2 FDODGE "
              "RISE2 TOP2 AF1 AF2 AF3 AW SM_TIP PROP".split())
# What stands in for the dodge and the clash while a fighter has no sheet of them yet.
SPARE = {"XS": "IDLE1", "DODGE1": "HOP1", "DODGE2": "HOP2", "C1": "U1", "C2": "U3", "C3": "U4",
         "SLIP_S": "XS", "BDODGE1": "HOP1", "BDODGE2": "HOP2", "BDODGE3": "HOP1", "FDODGE": "DODGE1", "ADODGE": "DODGE2",
         "BR1": "IDLE4", "BR2": "IDLE4", "BR3": "IDLE1", "BR4": "IDLE1", "BR5": "IDLE2", "BR6": "IDLE2",
         "WB1": "WALK1", "WB2": "WALK2", "WB3": "WALK3", "WB4": "WALK4", "WB5": "WALK5", "WB6": "WALK6",
         "BB1": "BACK1", "BB2": "BACK2", "BB3": "BACK3", "BB4": "BACK4", "GUARD_UP": "GUARD", "TURN2": "TURN",
         "PUSH": "PREJUMP", "RISE2": "RISE", "TOP2": "TOP", "FALL2": "FALL", "LAND2": "LAND",
         "HIT1R": "HIT1", "HIT2R": "HIT2", "STAGGER2": "STAGGER", "PROP": "SIT_UP", "HALF_UP": "KNEEL_UP",
         "QW1": "QS", "QW2": "Q1", "QW3": "Q2", "QW4": "Q3", "QW5": "Q4", "QF1": "Q1", "QF2": "Q2", "QF3": "Q3", "QF4": "Q4", "QF5": "Q5",
         "HW1": "HS", "HW2": "H1", "HW3": "H2", "HW4": "H3", "HW5": "H4", "HF1": "H1", "HF2": "H2", "HF3": "H3", "HF4": "H4", "HF5": "H5",
         "AF1": "A1", "AF2": "A2", "AF3": "A3", "AW": "A1", "SM_TIP": "SM_UP",
         "DASH_QW": "DASH_Q", "DASH_HW": "DASH_H", "BREAK_W": "MS", "LAUNCH_W": "LAUNCH", "SHOVE_W": "SHOVE",
         "SW1": "SS", "SW2": "SS", "SW3": "SS", "SW4": "SS", "SW5": "SS", "U12": "U1", "U23": "U2", "U34": "U3", "U4R": "U4", "SF": "SS"}
# For a fighter who has only her first twelve frames (standing, walking, jumping, guarding, hit, down, quick, heavy,
# special, ultimate, win, and one more), which of them stands in for each frame.
OLD = dict(zip(NAMES, [
    0, 0, 0, 0, 3, 3, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 2, 0, 1, 1, 1, 0, 0, 0, 0, 2, 2, 2, 0, 4, 4, 4, 4, 4, 3, 4, 5, 5, 5, 0, 5,
    10, 10, 10, 5, 5, 10, 0, 6, 6, 6, 6, 6, 0, 7, 7, 7, 7, 7, 0, 6, 6, 6, 8, 8, 2, 7, 7, 0, 0, 0, 8, 8, 11, 8, 11, 8, 0, 6, 7, 7, 7, 6,
    9, 9, 11, 9, 10, 10, 0, 2, 2, 9, 11, 9, 0, 2, 2, 2, 2, 2]))
for _name in NAMES:
    OLD.setdefault(_name, 0)
# Roughly the colour of each one's hair, to find it by; what it is exactly is measured from her first sheet.
HAIR = {"halgakos": (238, 160, 180), "chikuwa": (200, 150, 110), "tsubakieh": (150, 122, 150), "rune": (150, 200, 240), "illya": (245, 190, 150)}
# A sheet whose upright frame came out a different size from the rest of it is put right by hand here: (fighter, sheet) to a factor.
RESIZE = {}


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
    the picture it is nearest. With each comes where her body is in it, apart from what
    flies about her: its left, top, right and bottom, and the middle of her feet.
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
        image = Image.fromarray(piece)
        box = solid(image).getbbox()
        rows, columns = boxes[body]
        # Her feet: the middle of what there is of her body in its lowest tenth.
        tall = rows.stop - rows.start
        low = labels[rows.stop - max(4, tall // 10):rows.stop, columns] == body + 1
        across_low = numpy.where(low.any(axis=0))[0]
        feet = columns.start + (across_low[0] + across_low[-1] + 1) / 2
        found.append((image.crop(box), (columns.start - box[0], rows.start - box[1], columns.stop - box[0], rows.stop - box[1]), feet - box[0]))
    return found


def chroma(pixels):
    total = pixels[..., :3].astype(numpy.float32).sum(axis=-1) + 1e-6
    return pixels[..., 0] / total, pixels[..., 1] / total, total / 3


def hairness(pixels, red, green, radius):
    """How much each pixel of a picture is of the colour of her hair, from 0 to 1."""
    r, g, bright = chroma(pixels)
    weight = numpy.clip(1 - numpy.hypot(r - red, g - green) / radius, 0, 1)
    return weight * (pixels[..., 3] > 200) * (bright > 55) * (bright < 248)


def hair_of(name, frames):
    """The colour of a fighter's hair as her first sheet has it: where it lies among colours, and its mean."""
    hint = numpy.array(HAIR[name], dtype=numpy.float32)
    red, green = hint[0] / hint.sum(), hint[1] / hint.sum()
    for radius in (.07, .05, .04):
        r_sum = g_sum = weight_sum = 0.0
        colour = numpy.zeros(3)
        for image in frames:
            pixels = numpy.array(image).astype(numpy.float32)
            top = pixels[: pixels.shape[0] * 45 // 100]
            weight = hairness(top, red, green, radius)
            r, g, _ = chroma(top)
            r_sum += (r * weight).sum()
            g_sum += (g * weight).sum()
            weight_sum += weight.sum()
            colour += (top[..., :3] * weight[..., None]).sum(axis=(0, 1))
        red, green = r_sum / weight_sum, g_sum / weight_sum
    return red, green, colour / weight_sum


def matched(frames, hair):
    """Some frames with her hair brought to the colour it has in her first sheet. Returns them and how far off they were."""
    red, green, wanted = hair
    colour, weight_sum = numpy.zeros(3), 0.0
    for image in frames:
        pixels = numpy.array(image).astype(numpy.float32)
        weight = hairness(pixels, red, green, .05)
        colour += (pixels[..., :3] * weight[..., None]).sum(axis=(0, 1))
        weight_sum += weight.sum()
    if weight_sum < 500:
        return frames, [1, 1, 1]
    gains = numpy.clip(wanted / (colour / weight_sum), .85, 1.15)
    if numpy.abs(gains - 1).max() < .02:
        return frames, [round(float(gain), 3) for gain in gains]
    out = []
    for image in frames:
        pixels = numpy.array(image).astype(numpy.float32)
        weight = hairness(pixels, red, green, .06)[..., None]
        pixels[..., :3] = numpy.clip(pixels[..., :3] * (1 + (gains - 1) * weight), 0, 255)
        out.append(Image.fromarray(pixels.astype(numpy.uint8)))
    return out, [round(float(gain), 3) for gain in gains]


def sized(parts, upright, factor=1.0):
    """The frames of a sheet brought to her height, by the frames of it in which she stands upright."""
    scale = STAND / max(parts[index][1][3] - parts[index][1][1] for index in upright) * factor
    out = []
    for image, body, feet in parts:
        size = (max(1, round(image.size[0] * scale)), max(1, round(image.size[1] * scale)))
        out.append((image.convert("RGBa").resize(size, Image.LANCZOS).convert("RGBA"), [value * scale for value in body], feet * scale))
    return out, scale


def fighter(name, report):
    """A fighter's frames by name: each a picture, and where in it she stands (across to her feet, down to the ground)."""
    old = []
    for sheet_name, upright in (("move", (0, 1, 3, 4)), ("hits", (0, 1, 2, 4, 5))):
        parts, _ = sized(cells(clean(Image.open(SOURCE / f"{name}_{sheet_name}.webp")), 3, 2), upright)
        old += parts
    hair = hair_of(name, [old[0][0], old[1][0]])
    report[f"{name} hair"] = [round(float(value)) for value in hair[2]]
    folder = SOURCE / name
    full = all((folder / f"{sheet}.webp").exists() for sheet, _, _ in SHEETS[:15])
    frames = {}
    if not full:
        for slot, index in OLD.items():
            image, body, feet = old[index]
            frames[slot] = (image, feet, body[3], index)
        return frames, False, False
    missing = []
    for sheet, names, upright in SHEETS:
        path = folder / f"{sheet}.webp"
        if not path.exists():
            continue
        try:
            cut, factor = cells(clean(Image.open(path)), 3, 2), RESIZE.get((name, sheet), 1.0)
        except SystemExit as problem:
            # A sheet of in-betweens that cannot be cut into its six is left out, and what she had stands in for it.
            if sheet not in MORE:
                raise
            report[f"{name} {sheet}"] = f"LEFT OUT: {problem}"
            missing.append(sheet)
            continue
        parts, scale = sized(cut, upright, factor)
        if sheet in BETWEENS:
            ratios = sorted((part[1][3] - part[1][1]) / ((frames[one][0].size[1] + frames[other][0].size[1]) / 2)
                            for part, (one, other) in zip(parts, BETWEENS[sheet]))
            parts, scale = sized(cut, upright, factor / ratios[len(ratios) // 2])
        pictures, gains = matched([part[0] for part in parts], hair)
        report[f"{name} {sheet}"] = {"scale": round(scale, 3), "hair": gains,
                                    "tall": [round(part[1][3] - part[1][1]) for part in parts]}
        for slot, picture, (_, body, feet) in zip(names.split(), pictures, parts):
            across = (body[0] + body[2]) / 2 if slot in CENTRED else feet
            frames[slot] = (picture, across, body[3], None)
    for slot, spare in SPARE.items():
        frames.setdefault(slot, frames[spare])
    for slot in DROP:
        frames[slot] = frames["IDLE1"]
    return frames, True, not missing and all((folder / f"{sheet}.webp").exists() for sheet in MORE)


def packed(name, frames):
    """Packs a fighter's frames close together into as few pictures as hold them. Returns those, and where each frame went."""
    # A frame that stands in for several is kept once.
    unique = {}
    for slot in NAMES:
        unique.setdefault(id(frames[slot][0]), frames[slot][0])
    order = sorted(unique, key=lambda key: -unique[key].size[1])
    atlases, where, x, y, row = [], {}, 0, 0, 0
    sheet = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 0))
    for key in order:
        image = unique[key]
        if image.size[0] + 2 > ATLAS:
            raise SystemExit(f"{name}: a frame is {image.size}, too wide")
        if x + image.size[0] + 2 > ATLAS:
            x, y, row = 0, y + row + 2, 0
        if y + image.size[1] + 2 > ATLAS:
            atlases.append(sheet.crop((0, 0, ATLAS, y)))
            sheet, x, y, row = Image.new("RGBA", (ATLAS, ATLAS), (0, 0, 0, 0)), 0, 0, 0
        sheet.alpha_composite(image, (x + 1, y + 1))
        where[key] = (len(atlases), x + 1, y + 1)
        x += image.size[0] + 2
        row = max(row, image.size[1])
    atlases.append(sheet.crop((0, 0, ATLAS, y + row + 2)))
    table = []
    for slot in NAMES:
        image, across, down, _ = frames[slot]
        atlas, left, top = where[id(image)]
        table.append((atlas, left, top, image.size[0], image.size[1], round(across), round(down)))
    return atlases, table


def without_white(image):
    """A design sheet drawn on white, with the white round the figure made clear."""
    image = image.convert("RGBA")
    if image.getchannel("A").getextrema()[0] == 0:
        return image
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


def preview(name, frames, folder):
    """Every frame of a fighter standing on a line, a sheet to a row and a half, small: to see that they are one girl at one size."""
    box, per_row = (300, 250), 12
    rows = (len(NAMES) + per_row - 1) // per_row
    board = Image.new("RGBA", (box[0] * per_row, box[1] * rows), (110, 120, 140, 255))
    draw = ImageDraw.Draw(board)
    for index, slot in enumerate(NAMES):
        image, across, down, _ = frames[slot]
        small = image.resize((max(1, image.size[0] // 2), max(1, image.size[1] // 2)), Image.LANCZOS)
        left, ground = index % per_row * box[0], index // per_row * box[1] + box[1] - 14
        draw.line((left, ground, left + box[0], ground), fill=(255, 255, 255, 70))
        board.alpha_composite(small, (max(left - 40, min(left + box[0] - small.size[0] + 40, round(left + box[0] * .42 - across / 2))),
                                      round(ground - down / 2)), )
        draw.text((left + 4, index // per_row * box[1] + 2), slot, fill=(255, 255, 255, 255))
    board.convert("RGB").save(folder / f"{name}.jpg", quality=80)


def java(tables, sizes, full, smooth):
    lines = ["package com.coral.calculator;", "",
             "/**",
             " * Where every frame of every fighter of Page brawl is, in the pictures they are packed into. Written by",
             " * tools/make_fight_art.py: not to be changed by hand. A frame is a picture's number, its place and size in that",
             " * picture, and where in the frame she stands: across to her feet, down to the ground. They are kept so many",
             " * pixels to her standing height.",
             " */",
             "final class FightFrames {",
             f"    static final int STAND = {STAND}, COUNT = {len(NAMES)};"]
    for start in range(0, len(NAMES), 12):
        lines.append("    static final int " + ", ".join(f"{slot} = {start + index}" for index, slot in enumerate(NAMES[start:start + 12])) + ";")
    lines.append("    /** The pictures each fighter's frames are in, and how wide each was made. */")
    lines.append("    static final int[][] ATLASES = {")
    for name, size in zip(FIGHTERS, sizes):
        lines.append("            {" + ", ".join(f"R.drawable.fight_{name}_{index}" for index in range(len(size))) + "},")
    lines.append("    };")
    lines.append("    static final int[][] HIGH = {" + ", ".join("{" + ", ".join(str(one[1]) for one in size) + "}" for size in sizes) + "};")
    lines.append(f"    static final int WIDE = {ATLAS};")
    lines.append("    /** Whether a fighter has all her frames, or only her first twelve standing in for the rest. */")
    lines.append("    static final boolean[] FULL = {" + ", ".join("true" if one else "false" for one in full) + "};")
    lines.append("    /** Whether she has the frames that go between the others as well, or the others standing in for them. */")
    lines.append("    static final boolean[] SMOOTH = {" + ", ".join("true" if one else "false" for one in smooth) + "};")
    lines.append("    private static final String[] DATA = {")
    for table in tables:
        text = " ".join(" ".join(str(value) for value in row) for row in table)
        chunks = [text[start:start + 110] for start in range(0, len(text), 110)]
        lines.append("            " + "\n                    + ".join(f'"{chunk}"' for chunk in chunks) + ",")
    lines.append("    };")
    lines.append("    /** Every fighter's frames: the picture, left, top, width, height, across to her feet and down to the ground of each. */")
    lines.append("    static final short[][][] FRAMES = new short[DATA.length][COUNT][7];")
    lines.append("")
    lines.append("    static {")
    lines.append("        for (int fighter = 0; fighter < DATA.length; fighter++) {")
    lines.append('            String[] numbers = DATA[fighter].split(" ");')
    lines.append("            for (int index = 0; index < numbers.length; index++) FRAMES[fighter][index / 7][index % 7] = Short.parseShort(numbers[index]);")
    lines.append("        }")
    lines.append("    }")
    lines.append("")
    lines.append("    private FightFrames() {}")
    lines.append("}")
    JAVA.write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", type=pathlib.Path)
    args = parser.parse_args()
    report, tables, sizes, full, smooth = {}, [], [], [], []
    for old in RES.glob("fight_*.webp"):
        if old.stem.split("_")[1] in FIGHTERS:
            old.unlink()
    for name in FIGHTERS:
        frames, whole, between = fighter(name, report)
        smooth.append(between)
        atlases, table = packed(name, frames)
        for index, atlas in enumerate(atlases):
            atlas.save(RES / f"fight_{name}_{index}.webp", quality=80, alpha_quality=90, method=6)
        tables.append(table)
        sizes.append([atlas.size for atlas in atlases])
        full.append(whole)
        pixels = sum(atlas.size[0] * atlas.size[1] for atlas in atlases)
        report[f"{name} pictures"] = f"{len(atlases)} {'full' if whole else 'first twelve only'}, {pixels * 4 / 1e6:.0f} MB in memory at full size"
        if args.preview:
            args.preview.mkdir(parents=True, exist_ok=True)
            preview(name, frames, args.preview)
    java(tables, sizes, full, smooth)

    faces = Image.new("RGBA", (FACE * len(FIGHTERS), FACE), (0, 0, 0, 0))
    for index, name in enumerate(FIGHTERS):
        faces.alpha_composite(face(name), (index * FACE, 0))
    faces.save(RES / "fight_faces.webp", quality=90, alpha_quality=92, method=6)

    things = [part[0] for part in cells(clean(Image.open(SOURCE / "fx.webp")), 5, 2)]
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

    for number in range(1, STAGES + 1):
        stage = Image.open(SOURCE / f"stage{number}.webp").convert("RGB")
        stage = stage.resize((STAGE_WIDTH, round(stage.size[1] * STAGE_WIDTH / stage.size[0])), Image.LANCZOS)
        stage.save(RES / f"fight_stage{number}.webp", quality=88, method=6)

    print("FX_WIDE = {" + ", ".join(f"{value:.3f}f" for value in wide) + "};")
    print("FX_TALL = {" + ", ".join(f"{value:.3f}f" for value in tall) + "};")
    for key, value in report.items():
        print(key, value)
    print("res", sum(path.stat().st_size for path in RES.glob("fight_*.webp")), "bytes in", len(list(RES.glob("fight_*.webp"))), "files")


if __name__ == "__main__":
    main()
