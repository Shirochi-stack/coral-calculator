"""
Draws one vanity outfit over every frame of every sheet Halgakos is drawn on in Plus run, by whole grids at a time.

    python tools/make_vanity_outfit.py jobs  OUTFIT [--redo]   writes the grids (her own frames, enlarged, six or eight to a
                                                               sheet) and the jobs for gen.py, in three shards
    python tools/make_vanity_outfit.py pack  OUTFIT            cuts the redrawn grids, lines every frame up with the frame it
                                                               redraws, writes the sheets (lossy WebP) into
                                                               vanity/src/main/assets/vanity/OUTFIT/ (the asset pack), a report and contact sheets

A grid is one picture of several of her frames, drawn at once from one prompt and one design card, so the outfit is the same
in all of them. Every redrawn cell is then fitted to the frame it came from (scale and shift picked for the best overlap of
silhouettes), so no frame moves or changes size. A frame that overlaps badly is listed; `jobs --redo` draws the grids that hold
them again under another name, and `pack` takes the best of every draw of a frame.
"""
import argparse, json, pathlib, sys
import numpy as np
from PIL import Image
import cv2

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res/drawable-nodpi"
ASSETS = ROOT / "vanity/src/main/assets/vanity"
WORK = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\vanity\outfits")
CARDS = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\vanity\art\cards")

spec = __import__("importlib.util").util.spec_from_file_location("layers", ROOT / "tools/make_vanity_layers.py")
layers = __import__("importlib.util").util.module_from_spec(spec)
spec.loader.exec_module(layers)
SHEETS = layers.SHEETS
SMALL = ["jump", "lose", "party", "white", "back"]
ARMS = ["arm%d" % i for i in range(12)]

DESIGNS = {
    "bunny": ("the bunny girl suit", "a strapless glossy black satin bodysuit shaped like a ballet leotard, sheer black tights, a white detached collar with a red bow tie, white wrist cuffs and black high heels"),
    "maid": ("the maid dress", "a short black dress with puffed short sleeves, a white frilled apron with a bow, a white ruffled collar with a small pink ribbon, white frilled thigh-high stockings with black ribbon garters and black strap shoes"),
    "idol": ("the Sloppy Stage idol outfit", "a black off-shoulder frilly stage dress with detached puffed sleeves, a pink ribbon corset lacing and a white bow at the waist, a layered black and pink ruffled skirt, black tights and black shoes with white bows"),
    "swim1": ("the seaside swimsuit", "a sporty one-piece swimsuit with a white sailor-style collar, aqua and white horizontal stripes, a small frilled skirt hem, short sleeves, a tiny red ribbon at the chest and white strap sandals"),
    "bunnyg": ("the gacha bunny girl suit", "a glossy red satin bodysuit with gold buckle details, a short black cropped tailcoat jacket with red lining and gold buttons worn over it, opaque black tights, a white detached collar with a black bow tie, white wrist cuffs, red high heels and a small fluffy white pom-pom tail at the back of her waist"),
}
CANVAS = (1536, 1024)
CLEAR = (" The background must be fully transparent (a PNG with an alpha channel): no backdrop colour, no checkerboard, no ground, no shadows, no text, no borders, "
         "and no glow, halo or mist around anything.")


def sheet_image(name):
    return Image.open(RES / ("halgakos_%s.webp" % name)).convert("RGBA")


def cells(name):
    """Every non-empty frame of a sheet: (index, image)."""
    cols, rows, cw, ch = SHEETS[name]
    im = sheet_image(name)
    found = []
    for idx in range(cols * rows):
        c = im.crop(((idx % cols) * cw, (idx // cols) * ch, (idx % cols) * cw + cw, (idx // cols) * ch + ch))
        if np.array(c.getchannel("A")).max() > 128 and (np.array(c.getchannel("A")) > 128).sum() > 400:
            found.append((idx, c))
    return found


def groups():
    """The jobs' contents: lists of (sheet, index) in jobs of eight (the pet atlas) or six."""
    out = []
    pet = [("pet", i) for i, _ in cells("pet")]
    out += [("g4x2", pet[i:i + 8]) for i in range(0, len(pet), 8)]
    small = [(s, i) for s in SMALL for i, _ in cells(s)]
    out += [("g3x2", small[i:i + 6]) for i in range(0, len(small), 6)]
    arms = [(s, i) for s in ARMS for i, _ in cells(s)]
    out += [("g2x3", arms[i:i + 6]) for i in range(0, len(arms), 6)]
    return out


LAYOUT = {"g4x2": (4, 2), "g3x2": (3, 2), "g2x3": (2, 3)}


def make_jobs(outfit, redo):
    title, text = DESIGNS[outfit]
    work = WORK / outfit
    (work / "grids").mkdir(parents=True, exist_ok=True)
    (work / "raw").mkdir(exist_ok=True)
    manifest = {}
    jobs = []
    suffix = ""
    bad = set()
    if redo:
        report = json.loads((work / "report.json").read_text())
        bad = {f["job"] for f in report["bad"]}
        n = 1
        while any((work / "raw" / ("%s_r%d.png" % (j, n))).exists() for j in bad):
            n += 1
        suffix = "_r%d" % n
    for gi, (kind, frames) in enumerate(groups()):
        name = "%s_%02d" % (outfit, gi)
        cols, rows = LAYOUT[kind]
        cw, ch = CANVAS[0] // cols, CANVAS[1] // rows
        canvas = Image.new("RGBA", CANVAS, (0, 0, 0, 0))
        place = []
        for k, (sheet, idx) in enumerate(frames):
            c = dict(cells(sheet))[idx] if False else None
        cache = {}
        for k, (sheet, idx) in enumerate(frames):
            if sheet not in cache:
                cache[sheet] = dict(cells(sheet))
            c = cache[sheet][idx]
            s = min(cw * 0.92 / c.size[0], ch * 0.92 / c.size[1])
            big = c.convert("RGBa").resize((round(c.size[0] * s), round(c.size[1] * s)), Image.LANCZOS).convert("RGBA")
            x = (k % cols) * cw + (cw - big.size[0]) // 2
            y = (k // cols) * ch + (ch - big.size[1]) // 2
            canvas.alpha_composite(big, (x, y))
            place.append({"sheet": sheet, "index": idx, "cell": [(k % cols) * cw, (k // cols) * ch, cw, ch], "at": [x, y], "scale": s})
        canvas.save(work / "grids" / (name + ".png"))
        manifest[name] = {"kind": kind, "frames": place}
        if redo and name not in bad:
            continue
        prompt = (
            f"Reference image 1 is a sprite sheet of {len(frames)} animation frames of Halgakos, a chibi catgirl with pink hair, dark curled horns, pink cat ears, one white feathered angel wing, one dark bat wing and a pink tail, "
            f"wearing a white school blouse, red ribbon, navy pleated skirt and white socks, laid out in {cols} columns and {rows} rows (left to right, top to bottom), one frame in each cell, each in a different pose (some hold a weapon or a tool). "
            f"Reference image 2 shows her wearing {title}: {text}. "
            "Redraw the WHOLE sprite sheet. In every cell keep EXACTLY the same pose, the same size, the same position inside its cell, the same facing direction, the same hair, face, expression, horns, cat ears, wings and tail, "
            "and anything she holds (a weapon, a pen, a book, keys) exactly as in image 1, and replace only her school uniform with the outfit from image 2, identical in design, colours and proportions in every cell. "
            "Keep the same grid layout, one figure per cell, each well inside its own cell and never crossing into another, no cell added, removed or left out. "
            "Same cute chibi anime video game art, clean dark outlines, soft cel shading and line weight as the reference." + CLEAR)
        jobs.append({"name": name + suffix, "background": "transparent", "size": "%dx%d" % CANVAS, "prompt": prompt,
                     "refs": [str(work / "grids" / (name + ".png")), str(CARDS / (outfit + ".png"))]})
    (work / "manifest.json").write_text(json.dumps(manifest))
    for s in range(3):
        (work / ("jobs%s_%d.json" % (suffix, s))).write_text(json.dumps(jobs[s::3]))
    print(outfit, "grids", len(manifest), "jobs", len(jobs), suffix)


def solid(im):
    a = np.array(im.getchannel("A")).astype(np.int32)
    return a > 90


def cleaned(im):
    a = np.array(im.convert("RGBA"))
    al = a[..., 3].astype(np.int32)
    a[..., 3] = np.where(al < 60, 0, np.clip((al - 60) * 255 // 150, 0, 255)).astype(np.uint8)
    return Image.fromarray(a)


def iou(a, b):
    inter = np.logical_and(a, b).sum()
    union = np.logical_or(a, b).sum()
    return inter / union if union else 0.0


def register(out_cell, ref_mask):
    """Scale and shift of a redrawn cell (an RGBA image the size of the reference cell) that best overlaps the silhouette it redraws. Returns the fitted RGBA and the overlap."""
    h, w = ref_mask.shape
    om = solid(out_cell)
    if om.sum() < 200:
        return None, 0.0
    ys, xs = np.nonzero(om)
    ry, rx = np.nonzero(ref_mask)
    s0 = np.sqrt(ref_mask.sum() / om.sum())
    best = (-1, 1, 0, 0)
    down = 4
    refd = cv2.resize(ref_mask.astype(np.uint8), (w // down, h // down), interpolation=cv2.INTER_AREA) > 0
    for s in np.arange(s0 * 0.94, s0 * 1.07, 0.015):
        M = np.array([[s, 0, 0], [0, s, 0]], np.float32)
        # put the centroid of what was drawn on the centroid of what it redraws, then try shifts around that
        cx, cy = xs.mean() * s, ys.mean() * s
        base_dx, base_dy = rx.mean() - cx, ry.mean() - cy
        for dy in range(-12, 13, 3):
            for dx in range(-12, 13, 3):
                M[0, 2], M[1, 2] = base_dx + dx, base_dy + dy
                warped = cv2.warpAffine(om.astype(np.uint8), M, (w, h), flags=cv2.INTER_NEAREST) > 0
                wd = cv2.resize(warped.astype(np.uint8), (w // down, h // down), interpolation=cv2.INTER_AREA) > 0
                v = iou(wd, refd)
                if v > best[0]:
                    best = (v, s, base_dx + dx, base_dy + dy)
    _, s, tx, ty = best
    # refine at full size around the best
    best2 = (-1, s, tx, ty)
    for ds in (-.008, 0, .008):
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                M = np.array([[s + ds, 0, tx + dx], [0, s + ds, ty + dy]], np.float32)
                warped = cv2.warpAffine(om.astype(np.uint8), M, (w, h), flags=cv2.INTER_NEAREST) > 0
                v = iou(warped, ref_mask)
                if v > best2[0]:
                    best2 = (v, s + ds, tx + dx, ty + dy)
    v, s, tx, ty = best2
    M = np.array([[s, 0, tx], [0, s, ty]], np.float32)
    rgba = np.array(out_cell.convert("RGBa"))
    fitted = cv2.warpAffine(rgba, M, (w, h), flags=cv2.INTER_CUBIC, borderValue=(0, 0, 0, 0))
    return Image.fromarray(fitted, "RGBa").convert("RGBA"), v


def pack(outfit, threshold=0.80):
    work = WORK / outfit
    manifest = json.loads((work / "manifest.json").read_text())
    out_dir = ASSETS / outfit
    out_dir.mkdir(parents=True, exist_ok=True)
    sheets = {}
    metrics = {}
    bad, missing = [], []
    for name, info in manifest.items():
        draws = sorted((work / "raw").glob(name + "*.png"))
        draws = [d for d in draws if d.stem == name or d.stem.startswith(name + "_r")]
        if not draws:
            missing += [name]
            continue
        opened = [cleaned(Image.open(d)) for d in draws]
        for fr in info["frames"]:
            sheet, idx = fr["sheet"], fr["index"]
            cols, rows, cw, ch = SHEETS[sheet]
            orig = [c for i, c in cells(sheet) if i == idx][0]
            x, y, w, h = fr["cell"]
            s = fr["scale"]
            ref_cell = Image.new("RGBA", (w, h), (0, 0, 0, 0))
            big = orig.convert("RGBa").resize((round(cw * s), round(ch * s)), Image.LANCZOS).convert("RGBA")
            ref_cell.alpha_composite(big, (fr["at"][0] - x, fr["at"][1] - y))
            ref_mask = solid(ref_cell)
            best = (-1, None)
            for im in opened:
                if im.size != CANVAS:
                    im = im.resize(CANVAS, Image.LANCZOS)
                # a little margin round the cell, as a wing may cross its edge
                pad = 24
                out_cell = im.crop((x - pad, y - pad, x + w + pad, y + h + pad))
                big_ref = Image.new("RGBA", out_cell.size, (0, 0, 0, 0))
                big_ref.alpha_composite(ref_cell, (pad, pad))
                fitted, v = register(out_cell, solid(big_ref))
                if fitted is not None and v > best[0]:
                    best = (v, fitted.crop((pad, pad, pad + w, pad + h)))
            if best[1] is None:
                bad.append({"job": name, "sheet": sheet, "index": idx, "iou": 0})
                continue
            frame = best[1].convert("RGBa").resize((cw, ch), Image.LANCZOS).convert("RGBA")
            # her own pixels' shape is the shape: anything the fit left outside her silhouette's neighbourhood is cut
            keep = cv2.dilate((np.array(orig.getchannel("A")) > 20).astype(np.uint8), np.ones((9, 9), np.uint8)) > 0
            a = np.array(frame)
            a[..., 3] = np.where(keep, a[..., 3], 0)
            frame = Image.fromarray(a)
            if sheet not in sheets:
                sheets[sheet] = Image.new("RGBA", (cols * cw, rows * ch), (0, 0, 0, 0))
            sheets[sheet].alpha_composite(frame, ((idx % cols) * cw, (idx // cols) * ch))
            metrics[f"{sheet}:{idx}"] = round(best[0], 3)
            if best[0] < threshold:
                bad.append({"job": name, "sheet": sheet, "index": idx, "iou": round(best[0], 3)})
    for sheet, im in sheets.items():
        # (the original sheet fills what was not redrawn: an empty cell stays empty)
        im.save(out_dir / (sheet + ".webp"), quality=88, alpha_quality=95, method=6)
    size = sum((out_dir / (s + ".webp")).stat().st_size for s in sheets)
    report = {"outfit": outfit, "missing_jobs": missing, "frames": len(metrics), "bad": bad, "mean_iou": round(float(np.mean(list(metrics.values()))), 3) if metrics else 0, "bytes": size}
    (work / "report.json").write_text(json.dumps(report, indent=1))
    print(json.dumps({k: v for k, v in report.items() if k != "bad"}), "bad", len(bad))
    contact(outfit)


def contact(outfit):
    out_dir = ASSETS / outfit
    work = WORK / outfit
    (work / "contact").mkdir(exist_ok=True)
    for sheet, (cols, rows, cw, ch) in SHEETS.items():
        p = out_dir / (sheet + ".webp")
        if not p.exists():
            continue
        new = Image.open(p).convert("RGBA")
        old = sheet_image(sheet)
        per = min(cols, 8)
        frames = [i for i in range(cols * rows) if np.array(old.crop(((i % cols) * cw, (i // cols) * ch, (i % cols) * cw + cw, (i // cols) * ch + ch)).getchannel("A")).max() > 128]
        for part in range(0, len(frames), per * 3):
            chunk = frames[part:part + per * 3]
            lines = (len(chunk) + per - 1) // per
            board = Image.new("RGBA", (per * cw, lines * ch * 2), (150, 150, 165, 255))
            for k, i in enumerate(chunk):
                box = ((i % cols) * cw, (i // cols) * ch, (i % cols) * cw + cw, (i // cols) * ch + ch)
                board.alpha_composite(old.crop(box), ((k % per) * cw, (k // per) * ch * 2))
                board.alpha_composite(new.crop(box), ((k % per) * cw, (k // per) * ch * 2 + ch))
            board.convert("RGB").save(work / "contact" / ("%s_%d.jpg" % (sheet, part // (per * 3))), quality=84)


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("what", choices=["jobs", "pack", "contact"])
    ap.add_argument("outfit")
    ap.add_argument("--redo", action="store_true")
    a = ap.parse_args()
    {"jobs": lambda: make_jobs(a.outfit, a.redo), "pack": lambda: pack(a.outfit), "contact": lambda: contact(a.outfit)}[a.what]()
