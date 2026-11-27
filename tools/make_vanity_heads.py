"""
Head parts for Halgakos: her horns and her ears, taken off or replaced, on every frame of every sheet.

    python tools/make_vanity_heads.py jobs VARIANT      writes the grids (her heads, enlarged, twelve to a picture) and the jobs
    python tools/make_vanity_heads.py pack VARIANT      fits the redrawn heads back to their frames, finds what each part changed
                                                        and writes, per sheet, a patch (the new pixels) and a mask (where they go)
                                                        into vanity/src/main/assets/vanity/heads/VARIANT/

The variants: nohorns, elf (pointed elf ears instead of the horns), noears, fox, bunny, apron (a frilled headband instead of
the cat ears). Each is drawn from HER OWN frames (the uniform), a head at a time with room round it, and it is only ever a patch:
where the part was. The game puts the patch over any frame she is drawn in (her own or an outfit's), so a part and an outfit
combine freely and nothing has to be drawn per outfit.
"""
import argparse, json, pathlib, sys
import numpy as np
from PIL import Image
import cv2

ROOT = pathlib.Path(__file__).resolve().parent.parent
spec = __import__("importlib.util").util.spec_from_file_location("outfit", ROOT / "tools/make_vanity_outfit.py")
outfit = __import__("importlib.util").util.module_from_spec(spec)
spec.loader.exec_module(outfit)
SHEETS, cells, solid, cleaned, register, WORK = outfit.SHEETS, outfit.cells, outfit.solid, outfit.cleaned, outfit.register, outfit.WORK
HEADS = ROOT / "vanity/src/main/assets/vanity/heads"
CARDS = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\vanity\art\cards")
CANVAS = (1536, 1024)
COLS, ROWS = 4, 3

WHAT = {
    "nohorns": "Remove both dark curled horns completely, so that her pink hair is smooth where they were; keep the pink cat ears, the hair, the face and everything else exactly as it is.",
    "elf": "Replace the two dark curled horns with two long pointed elf ears that stick out sideways from the sides of her head, horizontally, in her skin colour with a pale pink inside; no horns at all any more; keep the pink cat ears, the hair, the face and everything else exactly as it is.",
    "noears": "Remove both pink cat ears completely, so that her pink hair is smooth and rounded on top of her head where they were; keep the dark curled horns, the hair, the face and everything else exactly as it is.",
    "fox": "Replace the two pink cat ears with two pointed fox ears of the same size and in the same place, orange with cream-white fur inside and dark brown tips; no cat ears any more; keep the dark curled horns, the hair, the face and everything else exactly as it is.",
    "bunny": "Replace the two pink cat ears with two soft bunny ears of about the same height as the cat ears, white with a pink inside and the tips bent a little forward, in the same place; no cat ears any more; keep the dark curled horns, the hair, the face and everything else exactly as it is.",
    "apron": "Replace the two pink cat ears with a white frilled lace maid headband (a ruffled headpiece) worn across the top of her hair; no cat ears any more; keep the dark curled horns, the hair, the face and everything else exactly as it is.",
}
CLEAR = (" The background must be fully transparent (a PNG with an alpha channel): no backdrop colour, no checkerboard, no ground, no shadows, no text, no borders, "
         "and no glow, halo or mist around anything.")


def head_crop(img):
    """The box of a frame's head, with room above and at the sides: left, top, width, height, in the frame's own pixels (top may be negative)."""
    a = np.array(img.getchannel("A")) > 128
    ys, xs = np.nonzero(a)
    x0, x1, y0, y1 = xs.min(), xs.max(), ys.min(), ys.max()
    h = y1 - y0
    # The head is the top half; a weapon held out sideways is not part of it, so the width is that of the top third plus a margin.
    top3 = a[y0:y0 + max(4, h // 3)]
    tx = np.nonzero(top3.any(axis=0))[0]
    left, right = int(tx.min()), int(tx.max())
    pad = 14
    crop_h = int(h * 0.50) + 8
    return int(left - pad), int(y0 - int(crop_h * 0.35)), int((right - left) + pad * 2), int(crop_h + int(crop_h * 0.35))


LATER = ["arm%d" % i for i in range(12, 18)]


def frames_list():
    """Every frame: those of the sheets she had first, then those of the sheets that came later (as grids of their own, so that the grids drawn before stay the same)."""
    out, later = [], []
    for name in SHEETS:
        for idx, c in cells(name):
            (later if name in LATER else out).append((name, idx, c))
    return out, later


def make_jobs(variant):
    work = WORK.parent / "heads" / variant
    (work / "grids").mkdir(parents=True, exist_ok=True)
    (work / "raw").mkdir(exist_ok=True)
    manifest = {}
    jobs = []
    first, later = frames_list()
    allf = first + later
    groups = [first[i:i + COLS * ROWS] for i in range(0, len(first), COLS * ROWS)] + [later[i:i + COLS * ROWS] for i in range(0, len(later), COLS * ROWS)]
    cw, ch = CANVAS[0] // COLS, CANVAS[1] // ROWS
    for gnum, group in enumerate(groups):
        name = "%s_%02d" % (variant, gnum)
        canvas = Image.new("RGBA", CANVAS, (0, 0, 0, 0))
        place = []
        for k, (sheet, idx, c) in enumerate(group):
            bx, by, bw, bh = head_crop(c)
            crop = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
            crop.alpha_composite(c, (-bx, -by))
            s = min(cw * 0.92 / bw, ch * 0.92 / bh)
            big = crop.convert("RGBa").resize((round(bw * s), round(bh * s)), Image.LANCZOS).convert("RGBA")
            x = (k % COLS) * cw + (cw - big.size[0]) // 2
            y = (k // COLS) * ch + (ch - big.size[1]) // 2
            canvas.alpha_composite(big, (x, y))
            place.append({"sheet": sheet, "index": idx, "crop": [bx, by, bw, bh], "cell": [(k % COLS) * cw, (k // COLS) * ch, cw, ch], "at": [x, y], "scale": s})
        canvas.save(work / "grids" / (name + ".png"))
        manifest[name] = place
        prompt = (
            f"Reference image 1 is a sheet of {len(group)} heads of Halgakos, a chibi catgirl with pink hair, dark curled horns, pink cat ears and a white feathered wing, cut from different animation frames, in {COLS} columns and {ROWS} rows (left to right, top to bottom), "
            "one head in each cell, each in a different pose or angle, with empty space round each. "
            "Redraw the WHOLE sheet. In every cell keep EXACTLY the same head, the same size, the same position inside its cell, the same angle, hair, face, expression, eyes, wing and clothing, "
            "and change only this: " + WHAT[variant] + " The change must look the same in every cell, drawn to fit that head's angle. "
            "Keep the same grid layout, one head per cell, none added, removed or left out, nothing crossing into another cell. "
            "Same cute chibi anime video game art, clean dark outlines, soft cel shading and line weight as the reference." + CLEAR)
        jobs.append({"name": name, "background": "transparent", "size": "%dx%d" % CANVAS, "prompt": prompt, "refs": [str(work / "grids" / (name + ".png"))]})
    (work / "manifest.json").write_text(json.dumps(manifest))
    for s in range(3):
        (work / ("jobs_%d.json" % s)).write_text(json.dumps(jobs[s::3]))
    print(variant, "grids", len(manifest), "frames", len(allf))


def components(mask, least):
    """Keep only the parts of a mask that are at least so many pixels (the specks of drift are not a part)."""
    n, lab, st, _ = cv2.connectedComponentsWithStats(mask.astype(np.uint8), 8)
    out = np.zeros_like(mask)
    for i in range(1, n):
        if st[i, 4] >= least:
            out |= lab == i
    return out


def pack(variant):
    work = WORK.parent / "heads" / variant
    manifest = json.loads((work / "manifest.json").read_text())
    out = HEADS / variant
    out.mkdir(parents=True, exist_ok=True)
    patch, mask = {}, {}
    seen, failed = 0, []
    for name, group in manifest.items():
        draws = [d for d in sorted((work / "raw").glob(name + "*.png")) if d.stem == name or d.stem.startswith(name + "_r")]
        if not draws:
            failed += [(name, "no draw")]
            continue
        opened = [cleaned(Image.open(d)) for d in draws]
        for fr in group:
            sheet, idx = fr["sheet"], fr["index"]
            cols, rows, cw, ch = SHEETS[sheet]
            orig = [c for i, c in cells(sheet) if i == idx][0]
            bx, by, bw, bh = fr["crop"]
            ref = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
            ref.alpha_composite(orig, (-bx, -by))
            x, y, w, h = fr["cell"]
            s = fr["scale"]
            big = ref.convert("RGBa").resize((round(bw * s), round(bh * s)), Image.LANCZOS).convert("RGBA")
            ref_cell = Image.new("RGBA", (w, h), (0, 0, 0, 0))
            ref_cell.alpha_composite(big, (fr["at"][0] - x, fr["at"][1] - y))
            best = (-1, None)
            for im in opened:
                pad = 24
                cell = im.crop((x - pad, y - pad, x + w + pad, y + h + pad))
                big_ref = Image.new("RGBA", cell.size, (0, 0, 0, 0))
                big_ref.alpha_composite(ref_cell, (pad, pad))
                # The silhouettes differ by the part, so the fit is judged on the part of the head that does not change.
                fitted, v = register_part(cell, solid(big_ref))
                if fitted is not None and v > best[0]:
                    best = (v, fitted.crop((pad, pad, pad + w, pad + h)))
            if best[1] is None:
                failed.append((name, sheet, idx))
                continue
            seen += 1
            # Back to the frame's own pixels, in the crop box: the cell is cut down to the enlarged crop, then to the crop's size.
            region = best[1].crop((fr["at"][0] - x, fr["at"][1] - y, fr["at"][0] - x + big.size[0], fr["at"][1] - y + big.size[1]))
            var = region.convert("RGBa").resize((bw, bh), Image.LANCZOS).convert("RGBA")
            # (the faint haze at the edges of a redraw is not part of anything: only what is plainly drawn is a patch)
            va = np.array(var)
            va[..., 3] = np.where(va[..., 3] < 120, 0, va[..., 3])
            # (and the model leaves ghosts, thin broken lines where a removed part was: pieces smaller than a part are dropped)
            # (her school collar is navy and white: a part is never that, and an outfit has a collar of its own)
            navy = (va[..., 2].astype(int) > va[..., 0].astype(int) + 12) & (va[..., 2] > 70)
            va[..., 3] = np.where(navy, 0, va[..., 3])
            solid_va = va[..., 3] > 0
            va[..., 3] = np.where(components(solid_va, 24), va[..., 3], 0)
            var = Image.fromarray(va)
            o = np.array(ref).astype(np.int32)
            v = np.array(var).astype(np.int32)
            alpha_diff = np.abs(o[..., 3] - v[..., 3]) > 90
            both = (o[..., 3] > 128) & (v[..., 3] > 128)
            colour_diff = both & (np.abs(o[..., :3] - v[..., :3]).sum(axis=2) > 110)
            ys_ = np.nonzero((o[..., 3] > 128).any(axis=1))[0]
            if variant in ("nohorns", "elf"):
                # Only the horns' own place: every pixel that is a horn in her frame, and (for elf ears) what the redraw adds there. The rest of the head
                # is drawn again by the model a little differently, and that drift is not a change.
                r_, g_, b_ = o[..., 0], o[..., 1], o[..., 2]
                horn = (o[..., 3] > 100) & (r_ < 140) & (g_ < 100) & (b_ < 150) & (b_ + 6 >= g_) & (r_ >= g_ + 8)
                near = cv2.dilate(horn.astype(np.uint8), np.ones((15, 15), np.uint8)) > 0
                added = components(alpha_diff & near, 8)
                diff = horn | added
            else:
                # Ears: the top of the head; what the part changed in colour or in outline, from the strongest differences only.
                diff = alpha_diff | (both & (np.abs(o[..., :3] - v[..., :3]).sum(axis=2) > 90))
                diff = cv2.morphologyEx(diff.astype(np.uint8), cv2.MORPH_OPEN, np.ones((3, 3), np.uint8)) > 0
                diff = components(diff, 20)
                # Horns are never touched by an ear: where a horn is (in her frame or in the redraw), nothing is a patch.
                def hornish(a_):
                    return (a_[..., 3] > 100) & (a_[..., 0] < 140) & (a_[..., 1] < 100) & (a_[..., 2] < 150) & (a_[..., 2] + 6 >= a_[..., 1]) & (a_[..., 0] >= a_[..., 1] + 8)
                keep_off = cv2.dilate((hornish(o) | hornish(v)).astype(np.uint8), np.ones((5, 5), np.uint8)) > 0
                diff = diff & ~keep_off
                if len(ys_):
                    diff[ys_[0] + int((ys_[-1] - ys_[0]) * 0.34):] = False
            # Two regions: the tight one, where the part's own pixels go (a little wider than the part), and, for horns, a loose one round it,
            # where only the last dark pixels of the old horns are taken off (an outfit's frame has them a pixel or two off).
            tight = cv2.dilate(diff.astype(np.uint8), np.ones((9, 9), np.uint8)) > 0
            loose = (cv2.dilate(diff.astype(np.uint8), np.ones((31, 31), np.uint8)) > 0) & ~tight if variant in ("nohorns", "elf") else np.zeros_like(tight)
            diff = tight | loose
            code = np.where(tight, 255, np.where(loose, 90, 0)).astype(np.uint8)
            if not diff.any():
                continue
            if sheet not in patch:
                patch[sheet] = np.zeros((rows * ch, cols * cw, 4), np.uint8)
                mask[sheet] = np.zeros((rows * ch, cols * cw), np.uint8)
            px, py = (idx % cols) * cw, (idx // cols) * ch
            # the box may reach outside the frame (room above the head): only what lies inside the frame can be shown
            for yy in range(bh):
                fy = by + yy
                if fy < 0 or fy >= ch:
                    continue
                for_x0, for_x1 = max(0, bx), min(cw, bx + bw)
                if for_x0 >= for_x1:
                    continue
                row = tight[yy, for_x0 - bx:for_x1 - bx]
                patch[sheet][py + fy, px + for_x0:px + for_x1][row] = np.array(var)[yy, for_x0 - bx:for_x1 - bx][row].astype(np.uint8)
                mask[sheet][py + fy, px + for_x0:px + for_x1] = np.maximum(mask[sheet][py + fy, px + for_x0:px + for_x1], code[yy, for_x0 - bx:for_x1 - bx])
    for sheet in patch:
        m = mask[sheet]
        pr = patch[sheet].copy()
        Image.fromarray(pr, "RGBA").save(out / (sheet + ".webp"), quality=88, alpha_quality=95, method=6)
        mk = np.zeros(m.shape + (4,), np.uint8)
        mk[..., :3] = 255
        mk[..., 3] = m
        Image.fromarray(mk, "RGBA").save(out / (sheet + "_m.webp"), lossless=True, quality=100, method=6, exact=True)
    size = sum(p.stat().st_size for p in out.glob("*.webp"))
    report = {"variant": variant, "frames": seen, "failed": failed, "bytes": size}
    (work / "report.json").write_text(json.dumps(report))
    print(json.dumps(report))


def register_part(cell, ref_mask):
    """
    The scale and shift that put a redrawn head cell back on the head it redraws, judged on the lower part of the head, which a part
    (ears, horns) does not change: the bottoms are lined up and so are the middles of the lowest third; then a small search.
    """
    h, w = ref_mask.shape
    om = solid(cell)
    if om.sum() < 200 or ref_mask.sum() < 200:
        return None, 0.0
    def low(mask):
        ys = np.nonzero(mask.any(axis=1))[0]
        bottom = ys[-1]
        top3 = max(ys[0], bottom - (bottom - ys[0]) // 3)
        xs = np.nonzero(mask[top3:bottom + 1].any(axis=0))[0]
        return bottom, (xs.min() + xs.max()) / 2.0
    rb, rx = low(ref_mask)
    ob, ox = low(om)
    ys = np.nonzero(ref_mask.any(axis=1))[0]
    first = ys[0] + int((rb - ys[0]) * 0.45)
    best = (-1, 1.0, 0, 0)
    for s in np.arange(0.94, 1.07, 0.01):
        base_dx, base_dy = rx - ox * s, rb - ob * s
        M = np.array([[s, 0, 0], [0, s, 0]], np.float32)
        for dy in range(-8, 9, 2):
            for dx in range(-8, 9, 2):
                M[0, 2], M[1, 2] = base_dx + dx, base_dy + dy
                warped = cv2.warpAffine(om.astype(np.uint8), M, (w, h), flags=cv2.INTER_NEAREST) > 0
                v = outfit.iou(warped[first:], ref_mask[first:])
                if v > best[0]:
                    best = (v, s, base_dx + dx, base_dy + dy)
    v, s, tx, ty = best
    M = np.array([[s, 0, tx], [0, s, ty]], np.float32)
    rgba = np.array(cell.convert("RGBa"))
    fitted = cv2.warpAffine(rgba, M, (w, h), flags=cv2.INTER_CUBIC, borderValue=(0, 0, 0, 0))
    return Image.fromarray(fitted, "RGBa").convert("RGBA"), v


def offsets(outfit_name):
    """
    Where an outfit's head sits against her own: for every frame, the shift (dx, dy, up to 4 pixels) that makes its face line up with the
    face of the frame it redraws, so that a part drawn on her own frame can be laid on the outfit's. One table per outfit:
    vanity/src/main/assets/vanity/heads/offsets_OUTFIT.txt, a line "sheet frame dx dy" for each frame.
    """
    lines = []
    for name, (cols, rows, cw, ch) in SHEETS.items():
        base = outfit.sheet_image(name)
        new = Image.open(ROOT / "vanity/src/main/assets/vanity" / outfit_name / (name + ".webp")).convert("RGBA")
        for idx in range(cols * rows):
            box = ((idx % cols) * cw, (idx // cols) * ch, (idx % cols) * cw + cw, (idx // cols) * ch + ch)
            a = np.array(base.crop(box)).astype(np.int32)
            b = np.array(new.crop(box)).astype(np.int32)
            al = a[..., 3] > 128
            if al.sum() < 400:
                continue
            ys = np.nonzero(al.any(axis=1))[0]
            top, bottom = ys[0], ys[0] + int((ys[-1] - ys[0]) * 0.42)
            # the face and hair, below the ears and horns, above the chin
            y0 = top + int((bottom - top) * 0.45)
            best = (1e18, 0, 0)
            for dy in range(-4, 5):
                for dx in range(-4, 5):
                    shifted = np.roll(np.roll(b, dy, axis=0), dx, axis=1)
                    region = (slice(y0, bottom), slice(None))
                    ra, rb = a[region], shifted[region]
                    both = (ra[..., 3] > 128) & (rb[..., 3] > 128)
                    if both.sum() < 50:
                        continue
                    # (the cost: the difference where both are drawn, and a heavy cost for where only one is)
                    diff = np.abs(ra[..., :3] - rb[..., :3]).sum(axis=2)[both].mean() + 60.0 * ((ra[..., 3] > 128) ^ (rb[..., 3] > 128)).mean() * 255
                    if diff < best[0]:
                        best = (diff, dx, dy)
            # the shift to put the uniform's patch on the outfit: the other way
            lines.append("%s %d %d %d" % (name, idx, -best[1], -best[2]))
    HEADS.mkdir(parents=True, exist_ok=True)
    (HEADS.parent / "heads" / ("offsets_%s.txt" % outfit_name)).write_text("\n".join(lines) + "\n")
    print(outfit_name, len(lines), "frames; shifts:", sorted({tuple(l.split()[2:]) for l in lines})[:12])


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("what", choices=["jobs", "pack", "offsets"])
    ap.add_argument("variant")
    a = ap.parse_args()
    {"jobs": lambda: make_jobs(a.variant), "pack": lambda: pack(a.variant), "offsets": lambda: offsets(a.variant)}[a.what]()
