"""Cuts the outfit layer of Halgakos once, for every frame of every sheet she is drawn on in Plus run.

For each sheet it writes
  app/src/main/res/drawable-nodpi/vanity_mask_<sheet>.webp   a mask atlas (the sheet's size; the mask is the alpha channel)
and, for all of them,
  app/src/main/assets/vanity_anchors.txt   one line per frame: sheet frame then points (x y) for
        top shoulder chest waist hem hand, in the frame's own pixels; 'none' where the frame has no outfit.

The mask is what the first pass could tell by colour (navy skirt, the near-white blouse and socks next to it, the red
ribbon) with the two fixes the prototype asked for: the white angel wing's root is cut off the blouse by taking only
what lies inside the torso column of the skirt, and the far sock (white, in shadow) is let in by a lower brightness
floor in the leg zone. Everything else (skin, hair, ears, horns, wings, shoes, what she holds) is left alone, so any
cosmetic is a draw over the same pixels and never moves a silhouette.

  python tools/make_vanity_layers.py [--preview DIR]
"""
import argparse, pathlib, sys
import numpy as np
from PIL import Image
import cv2

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res/drawable-nodpi"
ASSETS = ROOT / "app/src/main/assets"

# name: (columns, rows, cell width, cell height) ; single-row sheets are cell width by height
SHEETS = {
    "pet": (8, 9, 173, 202),
    "jump": (6, 1, 256, 208), "lose": (6, 1, 288, 224), "party": (6, 1, 173, 202),
    "white": (12, 1, 240, 212), "back": (6, 1, 204, 236),
    "arm0": (8, 1, 304, 248), "arm1": (8, 1, 304, 248), "arm2": (8, 1, 304, 248), "arm3": (8, 1, 304, 248),
    "arm4": (8, 1, 304, 248), "arm5": (8, 1, 304, 248), "arm6": (12, 1, 500, 248), "arm7": (12, 1, 336, 248),
    "arm8": (14, 1, 400, 248), "arm9": (12, 1, 380, 248), "arm10": (12, 1, 340, 248), "arm11": (12, 1, 500, 248), "arm12": (12, 1, 500, 248), "arm13": (12, 1, 500, 248), "arm14": (12, 1, 500, 248), "arm15": (12, 1, 500, 248), "arm16": (12, 1, 500, 248), "arm17": (12, 1, 500, 248),
}


def cut(cell):
    """Outfit mask (0..1, same size as the cell) and the anchor points, or (None, None) for an empty cell or one with no skirt."""
    a = np.array(cell).astype(np.float32)
    rgb = a[..., :3]
    al = a[..., 3] > 128
    if al.sum() < 400:
        return None, None
    hsv = cv2.cvtColor(rgb.astype(np.uint8), cv2.COLOR_RGB2HSV).astype(np.float32)
    H, S, V = hsv[..., 0] * 2, hsv[..., 1] / 255, hsv[..., 2] / 255
    r, g, b = rgb[..., 0], rgb[..., 1], rgb[..., 2]
    # The skirt is navy; in a frame where it is in shadow it is a greyer blue, so a second, looser try is made before giving up.
    for gap, sat, bright in ((15, 0.28, 0.75), (8, 0.15, 0.62), (4, 0.10, 0.55)):
        navy = al & (b > r + gap) & (V < bright) & (H > 190) & (H < 262) & (S > sat)
        n, lab, st, _ = cv2.connectedComponentsWithStats(navy.astype(np.uint8), 8)
        if n < 2:
            continue
        big = 1 + int(np.argmax(st[1:, 4]))
        if st[big, 4] >= 150:
            navy = lab == big
            break
    else:
        return None, None
    ys, xs = np.nonzero(navy)
    x0, x1, y0, y1 = xs.min(), xs.max(), ys.min(), ys.max()
    sw, sh = max(1, x1 - x0), max(1, y1 - y0)
    white = al & (S < 0.28) & (V > 0.6) & (b >= r - 10) & (b >= g - 6)
    shadow = al & (S < 0.30) & (V > 0.42) & (b >= r - 12) & (b >= g - 8)
    red = al & (r > 150) & (g < 90) & (b < 110) & (r - b > 80)
    hh, ww = al.shape
    yy, xx = np.mgrid[0:hh, 0:ww]
    # The blouse: above the skirt, in the torso column only (the wing's root lies outside it).
    blouse = white & (yy < y0 + 2) & (yy > y0 - sh * 1.35) & (xx > x0 - sw * 0.05) & (xx < x1 + sw * 0.22)
    # The socks: below the skirt's middle; the far one is in shadow, so the brightness floor is lower there.
    legs = (white | shadow) & (yy > y0 + sh * 0.55) & (xx > x0 - sw * 0.9) & (xx < x1 + sw * 0.9) & (yy < y1 + sh * 2.2)
    cloth = navy | blouse | legs | (red & (yy > y0 - sh * 1.5) & (yy < y0 + sh) & (xx > x0 - sw * 0.1))
    m = cv2.morphologyEx(cloth.astype(np.uint8), cv2.MORPH_OPEN, np.ones((2, 2), np.uint8))
    near = cv2.dilate(navy.astype(np.uint8), np.ones((25, 25), np.uint8))
    n, lab, st, _ = cv2.connectedComponentsWithStats(m, 8)
    out = np.zeros_like(m)
    for i in set(np.unique(lab[near > 0])) - {0}:
        if st[i, 4] > 20:
            out |= (lab == i).astype(np.uint8)
    out = cv2.morphologyEx(out, cv2.MORPH_CLOSE, np.ones((3, 3), np.uint8))
    out = (out > 0) & al
    soft = cv2.GaussianBlur(out.astype(np.float32), (0, 0), 0.6) * al
    oy, ox = np.nonzero(out)
    top = oy.min()
    sx = float(np.median(ox[oy < top + 6]))
    ay, ax = np.nonzero(al)
    ty = ay.min()
    tx = float(np.median(ax[ay < ty + 5]))
    cx = (x0 + x1) / 2
    anchors = {"top": (tx, float(ty)), "shoulder": (sx, float(top + 3)), "chest": (sx, float(top + 10)),
               "waist": (cx, float(y0)), "hem": (cx, float(y1)), "hand": (float(x1 + sw * 0.35), float(y0 - sh * 0.4))}
    return soft, anchors


def frames(name, im):
    cols, rows, cw, ch = SHEETS[name]
    if im.size != (cols * cw, rows * ch):
        sys.exit("%s is %s, expected %s" % (name, im.size, (cols * cw, rows * ch)))
    for row in range(rows):
        for col in range(cols):
            yield row * cols + col, col, row, im.crop((col * cw, row * ch, col * cw + cw, row * ch + ch))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--preview", default=None)
    args = ap.parse_args()
    lines, total, hit = [], 0, 0
    for name, (cols, rows, cw, ch) in SHEETS.items():
        im = Image.open(RES / ("halgakos_%s.webp" % name)).convert("RGBA")
        atlas = np.zeros((rows * ch, cols * cw), np.float32)
        tiles = []
        for idx, col, row, cell in frames(name, im):
            m, anc = cut(cell)
            total += 1
            if m is None:
                lines.append("%s %d none" % (name, idx))
                continue
            hit += 1
            atlas[row * ch:(row + 1) * ch, col * cw:(col + 1) * cw] = m
            lines.append("%s %d %s" % (name, idx, " ".join("%.1f %.1f" % anc[k] for k in ("top", "shoulder", "chest", "waist", "hem", "hand"))))
            if args.preview:
                base = np.array(cell).astype(np.float32)
                al = base[..., 3:4] / 255
                comp = base[..., :3] * al + np.array([120, 120, 140]) * (1 - al)
                mm = m * 0.7
                comp[..., 0] = comp[..., 0] * (1 - mm) + 255 * mm
                comp[..., 1] *= (1 - mm)
                comp[..., 2] *= (1 - mm)
                tiles.append(Image.fromarray(comp.astype(np.uint8)))
        mask = np.zeros((rows * ch, cols * cw, 4), np.uint8)
        mask[..., :3] = 255
        mask[..., 3] = np.clip(atlas * 255 + 0.5, 0, 255).astype(np.uint8)
        Image.fromarray(mask, "RGBA").save(RES / ("vanity_mask_%s.webp" % name), lossless=True, quality=100, method=6, exact=True)
        if args.preview and tiles:
            out = pathlib.Path(args.preview)
            out.mkdir(parents=True, exist_ok=True)
            per = 8
            for part in range(0, len(tiles), per * 4):
                chunk = tiles[part:part + per * 4]
                sheet = Image.new("RGB", (per * cw, ((len(chunk) + per - 1) // per) * ch), (120, 120, 140))
                for i, t in enumerate(chunk):
                    sheet.paste(t, ((i % per) * cw, (i // per) * ch))
                sheet.save(out / ("%s_%d.jpg" % (name, part // (per * 4))), quality=82)
        print(name, "frames with an outfit", sum(1 for l in lines if l.startswith(name + " ") and not l.endswith("none")))
    ASSETS.mkdir(parents=True, exist_ok=True)
    (ASSETS / "vanity_anchors.txt").write_text("\n".join(lines) + "\n")
    print("frames", total, "with an outfit", hit)


if __name__ == "__main__":
    main()
