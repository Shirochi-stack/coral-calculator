"""
New animation sheets for Halgakos, made from her own frames the way the vanity outfits are: a grid of the frames to be changed goes to the
image model with a picture of the pose to give them, and what comes back is fitted to the frame it redraws (so nothing moves or changes size).

    python tools/make_move_anims.py jobs [NAME ...] [--takes N]    the grids and the jobs for gen.py (three shards, N draws of each grid)
    python tools/make_move_anims.py pack [NAME ...] [--pick FILE]  the best draw of every frame (or the draws named in a JSON file, {"gun_walk": {"0": 2, ...}})
                                                                   cut, fitted, and written as runart/src/main/assets/runart/halgakos_NAME.webp,
                                                                   with a contact sheet of every draw to choose by

  gun_walk      6 frames of 304x248: her ink gun fired on the move: a four-step run with the gun aimed out in front (muzzle splash in two), and the
                same aim going up and coming down.
  great_charge  10 frames of 500x248: the greatsword's charge held (two, a pulse), walked with (four), and let go as a spinning slam (four).
"""
import argparse, json, pathlib, sys
import numpy as np
from PIL import Image
import cv2

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res/drawable-nodpi"
OUT = ROOT / "runart/src/main/assets/runart"
SCRATCH = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\anims")
CANVAS = (1536, 1024)
LAYOUT = {"g3x2": (3, 2), "g2x3": (2, 3), "g4x2": (4, 2)}

spec_ = __import__("importlib.util").util.spec_from_file_location("outfit", ROOT / "tools/make_vanity_outfit.py")
outfit = __import__("importlib.util").util.module_from_spec(spec_)
spec_.loader.exec_module(outfit)

HER = ("Halgakos, a chibi catgirl with pink hair, dark curled horns, pink cat ears, one white feathered angel wing, one dark bat wing and a pink tail, wearing a white school blouse, "
       "red ribbon, navy pleated skirt and white socks")
STYLE = (" Keep the same cute chibi anime video game art, clean dark outlines, soft cel shading and line weight as the reference, the same size, the same position inside each cell "
         "and the same facing direction. Keep the same grid layout, one figure per cell, each well inside its own cell, no cell added, removed or left out." + outfit.CLEAR)

# name: sheet geometry (frames, cell width, cell height); the grids (kind, [(source sheet, source frame, what this frame is)]) in the order of the frames they make;
# one picture of the pose to give them; and the words.
SPECS = {
    "gun_walk": {
        "cells": (6, 304, 248),
        "grids": [("g3x2", [("arm5", 1), ("arm5", 2), ("arm5", 3), ("arm5", 4), ("arm5", 5), ("arm5", 6)])],
        "pose": ("arm5", 7),
        "prompt": ("Reference image 1 is a sprite sheet of 6 animation frames of " + HER + ", carrying a toy ink gun, in 3 columns and 2 rows (left to right, top to bottom): "
                   "the first four are her running to the right (four steps of one run cycle), the fifth is her rising in a jump and the sixth falling. Reference image 2 shows "
                   "her aiming and firing the same ink gun: both arms stretched out straight in front of her to the right at shoulder height, the gun held level, pointing right. "
                   "Redraw the WHOLE sprite sheet. In every cell keep EXACTLY the same legs, stride, body lean, head, hair, wings, tail and place as in image 1, and change only her "
                   "arms and the gun: she aims the ink gun straight out in front of her at shoulder height as in image 2, as if she is firing while she runs and while she jumps. "
                   "In cells 1 and 3 add a small burst of pink and blue ink splashing at the very tip of the gun, as it fires; cells 2, 4, 5 and 6 have no splash. Her legs keep "
                   "running (cells 1 to 4) and tucked up or hanging as in image 1 (cells 5 and 6)." + STYLE)},
    "great_charge": {
        "cells": (10, 500, 248),
        "grids": [("g2x3", [("arm12", 9), ("arm12", 9), ("arm12", 1), ("arm12", 2), ("arm12", 3), ("arm12", 4)]),
                  ("g2x3", [("arm12", 7), ("arm12", 8), ("arm12", 10), ("arm12", 11)])],
        "pose": ("arm12", 9),
        "prompts": [
            ("Reference image 1 is a sprite sheet of 6 animation frames of " + HER + ", with a huge greatsword: the first two are her standing and gathering a charge with the sword "
             "drawn back, the next four are her running to the right (four steps of one run cycle). Reference image 2 shows her charging pose: braced low, the huge greatsword "
             "held back behind her in both hands. Redraw the WHOLE sprite sheet. Cells 1 and 2: the charge held, as in image 2 (braced knees, the sword drawn back behind her), the "
             "blade glowing with a pale blue-white charge light, brighter and with a few sparks in cell 2 (a pulse). Cells 3 to 6: a slow heavy walk while charging to the right: "
             "short, heavy steps (the legs alternating over four different frames of one walking cycle, body leaning forward under the weight), she keeps the greatsword drawn back "
             "behind her in both hands with its tip low near the ground, the blade glowing the same pale blue-white charge light. Same size and place as image 1." + STYLE),
            ("Reference image 1 is a sprite sheet of 4 animation frames of " + HER + ", with a huge greatsword, in 2 columns (left to right, top to bottom). Redraw the WHOLE sheet as "
             "a charged spinning slam, a stronger attack than her normal swing: cell 1: she crouches and winds the greatsword far back and around behind her, body twisted, the blade "
             "beginning to glow pale blue-white; cell 2: in the middle of a spin, the greatsword swung out wide to the right at shoulder height with a broad white-blue crescent arc of "
             "motion trailing round her, her hair and tail flying; cell 3: the greatsword slammed down onto the ground in front of her with a burst of white-blue impact light, "
             "cracks and flying rubble; cell 4: she straightens up, the sword resting point-down on the ground, a little dust and fading sparks. Same size and place as image 1." + STYLE)],
    },
}


def frame_of(sheet, index):
    cols, rows, cw, ch = outfit.SHEETS[sheet]
    im = outfit.sheet_image(sheet)
    return im.crop(((index % cols) * cw, (index // cols) * ch, (index % cols) * cw + cw, (index // cols) * ch + ch))


def grid_of(name, gi):
    kind, frames = SPECS[name]["grids"][gi]
    cols, rows = LAYOUT[kind]
    cw, ch = CANVAS[0] // cols, CANVAS[1] // rows
    canvas = Image.new("RGBA", CANVAS, (0, 0, 0, 0))
    place = []
    for k, (sheet, idx) in enumerate(frames):
        c = frame_of(sheet, idx)
        s = min(cw * 0.92 / c.size[0], ch * 0.92 / c.size[1])
        big = c.convert("RGBa").resize((round(c.size[0] * s), round(c.size[1] * s)), Image.LANCZOS).convert("RGBA")
        x, y = (k % cols) * cw + (cw - big.size[0]) // 2, (k // cols) * ch + (ch - big.size[1]) // 2
        canvas.alpha_composite(big, (x, y))
        place.append({"sheet": sheet, "index": idx, "cell": [(k % cols) * cw, (k // cols) * ch, cw, ch], "at": [x, y], "scale": s})
    return canvas, place


def tidy(frame):
    """What the margin of a fitted cell took in of its neighbours (a foot or a hat at the very edge) is taken off: small pieces that are not by the figure and lie at the top or bottom."""
    arr = np.array(frame)
    mask = (arr[..., 3] > 30).astype(np.uint8)
    n, lab, st, cen = cv2.connectedComponentsWithStats(mask, 8)
    if n < 3:
        return frame
    biggest = st[1:, 4].max()
    h = arr.shape[0]
    for i in range(1, n):
        area, top, tall = st[i, 4], st[i, 1], st[i, 3]
        near_edge = top < h * .06 or top + tall > h * .94
        if area < biggest * .06 and near_edge:
            arr[lab == i, 3] = 0
    return Image.fromarray(arr)


def make_jobs(names, takes):
    jobs = []
    for name in names:
        work = SCRATCH / name
        (work / "grids").mkdir(parents=True, exist_ok=True)
        (work / "raw").mkdir(exist_ok=True)
        sheet, idx = SPECS[name]["pose"]
        pose = frame_of(sheet, idx)
        pose = pose.convert("RGBa").resize((pose.size[0] * 2, pose.size[1] * 2), Image.LANCZOS).convert("RGBA")
        pose.save(work / "pose.png")
        manifest = {}
        for gi in range(len(SPECS[name]["grids"])):
            canvas, place = grid_of(name, gi)
            gname = "%s_g%d" % (name, gi)
            canvas.save(work / "grids" / (gname + ".png"))
            manifest[gname] = place
            prompt = SPECS[name]["prompts"][gi] if "prompts" in SPECS[name] else SPECS[name]["prompt"]
            for take in range(takes):
                tname = "%s_t%d" % (gname, take)
                if (work / "raw" / (tname + ".png")).exists():
                    continue
                jobs.append({"name": tname, "background": "transparent", "size": "%dx%d" % CANVAS, "prompt": prompt,
                             "refs": [str(work / "grids" / (gname + ".png")), str(work / "pose.png")]})
        (work / "manifest.json").write_text(json.dumps(manifest))
    for s in range(3):
        (SCRATCH / ("jobs_%d.json" % s)).write_text(json.dumps(jobs[s::3]))
    print("jobs", len(jobs))


def pack(names, pick):
    OUT.mkdir(parents=True, exist_ok=True)
    for name in names:
        work = SCRATCH / name
        manifest = json.loads((work / "manifest.json").read_text())
        frames_n, cw, ch = SPECS[name]["cells"]
        sheet = Image.new("RGBA", (frames_n * cw, ch), (0, 0, 0, 0))
        k = 0
        board = []
        choice = (pick or {}).get(name, {})
        for gi in range(len(SPECS[name]["grids"])):
            gname = "%s_g%d" % (name, gi)
            draws = sorted((work / "raw").glob(gname + "_t*.png"))
            opened = [outfit.cleaned(Image.open(d)) for d in draws]
            for fr in manifest[gname]:
                src, idx = fr["sheet"], fr["index"]
                orig = frame_of(src, idx)
                x, y, w, h = fr["cell"]
                s = fr["scale"]
                ref_cell = Image.new("RGBA", (w, h), (0, 0, 0, 0))
                big = orig.convert("RGBa").resize((round(orig.size[0] * s), round(orig.size[1] * s)), Image.LANCZOS).convert("RGBA")
                ref_cell.alpha_composite(big, (fr["at"][0] - x, fr["at"][1] - y))
                ref_mask = outfit.solid(ref_cell)
                options = []
                for t, im in enumerate(opened):
                    if im.size != CANVAS:
                        im = im.resize(CANVAS, Image.LANCZOS)
                    pad = 40
                    out_cell = im.crop((x - pad, y - pad, x + w + pad, y + h + pad))
                    big_ref = Image.new("RGBA", out_cell.size, (0, 0, 0, 0))
                    big_ref.alpha_composite(ref_cell, (pad, pad))
                    fitted, v = outfit.register(out_cell, outfit.solid(big_ref))
                    if fitted is None:
                        options.append((0, None))
                        continue
                    piece = fitted.crop((pad, pad, pad + w, pad + h))
                    ax, ay = fr["at"][0] - x, fr["at"][1] - y
                    frame = piece.crop((ax, ay, ax + big.size[0], ay + big.size[1])).convert("RGBa").resize((cw, ch), Image.LANCZOS).convert("RGBA")
                    options.append((v, tidy(frame)))
                if not options:
                    print("no draws for", gname)
                    k += 1
                    continue
                use = int(choice.get(str(k), max(range(len(options)), key=lambda i: options[i][0])))
                v, frame = options[use]
                if frame is not None:
                    sheet.alpha_composite(frame, (k * cw, 0))
                print("%s frame %d: take %d (overlap %.2f of %s)" % (name, k, use, v, ", ".join("%.2f" % o[0] for o in options)))
                board.append([o[1] for o in options])
                k += 1
        sheet.save(OUT / ("halgakos_%s.webp" % name), quality=90, alpha_quality=95, method=6)
        # a board: every draw of every frame, in rows by take, over a grey
        rows = max(len(b) for b in board) if board else 0
        contact = Image.new("RGBA", (len(board) * cw, rows * ch), (150, 150, 165, 255))
        for col, options in enumerate(board):
            for row, frame in enumerate(options):
                if frame is not None:
                    contact.alpha_composite(frame, (col * cw, row * ch))
        contact.convert("RGB").save(work / "contact.jpg", quality=86)
        print("written", OUT / ("halgakos_%s.webp" % name))


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("what", choices=["jobs", "pack"])
    ap.add_argument("names", nargs="*")
    ap.add_argument("--takes", type=int, default=3)
    ap.add_argument("--pick")
    a = ap.parse_args()
    names = a.names or list(SPECS)
    pick = json.loads(pathlib.Path(a.pick).read_text()) if a.pick else None
    {"jobs": lambda: make_jobs(names, a.takes), "pack": lambda: pack(names, pick)}[a.what]()
