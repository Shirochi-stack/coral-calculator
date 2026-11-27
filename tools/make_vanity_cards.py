"""
Draws the chibi design cards of the outfits in vanity_designs.py: one figure of her in the outfit, from her own idle frame, which is the
reference that every grid of that outfit is then drawn against.

    python tools/make_vanity_cards.py ID [ID ...]         writes the jobs (scratchpad) for gen.py; run gen.py on them
"""
import json, pathlib, sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from vanity_designs import DESIGNS

V = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\vanity\art")
CLEAR = " The background must be fully transparent (a PNG with an alpha channel): no backdrop colour, no ground, no shadows, no text, no glow or halo."
ART = " Cute chibi anime video game art, clean dark outlines, soft cel shading, same line weight and style as the reference."
BASE = ("Reference image 1 is a chibi game sprite of Halgakos: a pink-haired catgirl with two dark curled horns, pink cat ears, one white feathered angel wing, one dark bat wing and a pink tail, wearing a white school blouse, red ribbon and navy pleated skirt. "
        "Draw Halgakos again in exactly the same pose, size, face, hair, horns, ears, both wings and tail, but wearing a different outfit in place of her school uniform: ")


def main():
    ids = sys.argv[1:] or list(DESIGNS)
    jobs = [{"name": i, "background": "transparent", "size": "1024x1024", "refs": [str(V / "refs" / "idle.png")],
             "prompt": BASE + DESIGNS[i][1] + ". One figure only, whole body, centred, large, not touching the edges." + ART + CLEAR} for i in ids]
    for s in range(3):
        (V / ("jobs_cards3_%d.json" % s)).write_text(json.dumps(jobs[s::3]))
    print(len(jobs), "cards")


if __name__ == "__main__":
    main()
