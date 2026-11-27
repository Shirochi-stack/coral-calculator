"""A short run-cycle GIF of an outfit beside her own look: the eight frames of the run to the right (row 1 of the pet atlas).

    python tools/make_vanity_gif.py OUTFIT OUT.gif
"""
import pathlib, sys
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res/drawable-nodpi"
ASSETS = ROOT / "vanity/src/main/assets/vanity"
CW, CH, COLS = 173, 202, 8


def main():
    outfit, out = sys.argv[1], sys.argv[2]
    old = Image.open(RES / "halgakos_pet.webp").convert("RGBA")
    new = Image.open(ASSETS / outfit / "pet.webp").convert("RGBA")
    frames = []
    for i in range(8, 16):
        board = Image.new("RGBA", (CW * 2, CH), (232, 236, 246, 255))
        for k, sheet in enumerate((old, new)):
            board.alpha_composite(sheet.crop(((i % COLS) * CW, (i // COLS) * CH, (i % COLS) * CW + CW, (i // COLS) * CH + CH)), (k * CW, 0))
        frames.append(board.convert("RGB").resize((CW * 4, CH * 2), Image.LANCZOS))
    frames[0].save(out, save_all=True, append_images=frames[1:], duration=85, loop=0, optimize=True)
    print(out, pathlib.Path(out).stat().st_size, "bytes")


if __name__ == "__main__":
    main()
