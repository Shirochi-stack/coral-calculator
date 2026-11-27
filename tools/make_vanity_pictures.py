"""
The vanity pictures: Halgakos drawn full-size, as an adult woman (her full-size proportions, not the chibi of the game's frames), in
each vanity outfit and with each head part, in the way Teen-rated gacha splash art draws them.

    python tools/make_vanity_pictures.py jobs          writes the jobs for gen.py (three shards)
    python tools/make_vanity_pictures.py pack          writes vanity/src/main/assets/vanity/pictures/ID.webp and a contact sheet

Every picture is one outfit or part; an outfit may have variants (a second picture in the manner of another cut). A picture the
image tool refuses is left out and listed; no prompt is reworded to get past a refusal.
"""
import argparse, json, pathlib
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "vanity/src/main/assets/vanity/pictures"
WORK = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\vanity\pictures")
FULL = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\vanity\art\refs\idol_full.png")
FACE = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\assets\fight\face_halgakos.png")

WHO = ("Halgakos, an adult woman (a young woman in her twenties, with an adult figure, adult proportions and an adult face, tall and graceful, never childlike), "
       "with very long pink hair, red eyes, two dark curled horns, pink cat ears, a white feathered angel wing on one side and a dark bat wing on the other, and a long pink cat tail, "
       "exactly as drawn in reference image 1 (the full-size drawing of her) and reference image 2 (her face), not as a chibi. ")
STYLE = (" Drawn as polished anime gacha-game splash art: crisp clean lineart, rich cel shading, soft highlights, vivid colour, a friendly confident expression. "
         "Full body from the head to the shoes, standing, centred, on a soft pastel background with a few sparkles. One character only, no text, no logo, no watermark.")

# id: (name, what she wears and does)
PICTURES = {
    "bunny": ("Bunny Girl", "the classic bunny girl costume: a glossy black satin bunny leotard with a high-cut leg line and a sweetheart neckline, sheer black tights, a white detached collar with a red bow tie, white cuffs with cufflinks and black high heels; one hand on her hip and the other raised in a playful wave, winking"),
    "bunny2": ("Bunny Girl, white", "a white and pink variation of the bunny girl costume: a glossy white satin bunny leotard with pink trim, a pink bow tie, a white detached collar, white cuffs, sheer white tights and pink high heels; carrying a small silver tray, smiling playfully"),
    "maid": ("Maid Dress", "a short frilly maid dress: a black dress with puffed sleeves and a short layered skirt, a white frilled apron with a big bow at the back, a white lace headband between her cat ears, white thigh-high stockings with black ribbon garters and black strap shoes; holding a tray with a cup and curtsying, cheerful"),
    "idol": ("Sloppy\u2606Stage", "her idol stage outfit exactly as in reference image 1: a black off-shoulder frilly stage dress with detached puffed sleeves, a pink ribbon corset lacing and a white bow at the waist, a layered black and pink ruffled skirt, black tights and black shoes with white bows; singing into a microphone in a bright stage pose"),
    "idol2": ("Sloppy\u2606Stage, crop", "a cropped-top version of her idol stage outfit: a short black and pink frilled crop top with off-shoulder detached sleeves and a pink ribbon, a layered frilly mini skirt in black and pink with a white bow, thigh-high black socks with pink frills and black platform shoes; singing into a microphone and winking in a confident idol pose"),
    "swim1": ("Seaside Swimsuit", "a cute frilly one-piece swimsuit with a white sailor-style collar, aqua and white stripes, a small frilled skirt and a red ribbon at the chest, white sandals and a straw sun hat held in one hand; standing on a sunny beach with a blue sea behind her, smiling"),
    "swim2": ("Seaside Swimsuit, bikini", "a pretty frilled bikini in aqua and white with a bandeau top and a small bow, a thin sheer-free aqua wrap skirt tied at the hip, white sandals; standing on a sunny beach with a blue sea behind her, holding a beach ball, smiling cheerfully"),
    "pajama": ("Pajama Set", "a cute pajama set: a loose soft purple button-up pajama top with white stars and short pajama shorts, fluffy slippers, a nightcap; hugging a big pillow and yawning sweetly, in a cosy bedroom with a night sky"),
    "gothic": ("Gothic Dress", "a gothic lolita dress: black and violet lace with a corset bodice, a layered ruffled skirt, a red gem at the throat, black stockings and platform boots; holding a lace parasol with an elegant dramatic pose"),
    "tabard": ("Knight's Tabard", "a knight's outfit: a royal blue tabard with a gold edge and a white crest over a light steel breastplate with one pauldron, a short skirt, armoured boots; holding a longsword upright in a heroic pose"),
    "maid2": ("Maid Dress, pink", "a pink and white French maid mini dress: a short pink dress with a heart-shaped neckline, puffed off-shoulder sleeves, a short frilled white apron and a layered petticoat skirt, a white lace headband between her cat ears, white thigh-high stockings with pink bows, and pink strap shoes; blowing a kiss with one hand and holding a feather duster in the other"),
    "pajama2": ("Pajama Set, camisole", "a cute pajama set: a soft lavender satin camisole top with a lace trim and matching short pajama shorts with white stars, fluffy slippers, her long hair loose; sitting in a pose of stretching sleepily on a bed edge, a teddy bear beside her, in a cosy bedroom with a night sky"),
    "gothic2": ("Gothic Dress, mini", "a gothic lolita mini dress: a black corset mini dress with violet lace trim and off-shoulder lace sleeves, a layered ruffled mini skirt, a red ribbon choker, black thigh-high stockings with lace tops and platform boots; holding a small black cat plush, in a confident playful pose"),
    "tabard2": ("Knight's Tabard, sky", "a fantasy valkyrie-style knight outfit: a short royal blue tabard dress with a gold trim, a white crest, light silver pauldrons and bracers, a bare midriff under a short breastplate, armoured thigh boots; holding a spear and a round shield, in a heroic confident pose"),
    "swim3": ("Seaside Swimsuit, sporty", "a sporty swimsuit set: a fitted aqua and white racing-style one-piece swimsuit with a zip at the front, a white open short-sleeved beach jacket tied at the waist, sandals and a swim ring over one arm; standing on a sunny beach with a blue sea behind her, giving a thumbs-up and a bright smile"),
    "bunnyg": ("Bunny Girl (Gacha)", "the gacha-game bunny girl costume: a glossy red and black satin bunny leotard with a sweetheart neckline and gold buckle details, fishnet tights, a white detached collar with a black bow tie, white wrist cuffs, a pair of black bunny ears on a headband worn between her cat ears, a fluffy white pom-pom tail at the back and red high heels; leaning forward with one finger raised to her lips and winking, in a playful splash-art pose"),
}


def make_jobs():
    WORK.mkdir(parents=True, exist_ok=True)
    jobs = []
    for pid, (name, what) in PICTURES.items():
        prompt = ("Create a full-size picture of " + WHO + "She is wearing " + what + "." + STYLE)
        jobs.append({"name": "pic_" + pid, "background": "auto", "size": "1024x1536", "prompt": prompt, "refs": [str(FULL), str(FACE)]})
    for s in range(3):
        (WORK / ("jobs_%d.json" % s)).write_text(json.dumps(jobs[s::3]))
    print("pictures", len(jobs))


def pack():
    OUT.mkdir(parents=True, exist_ok=True)
    tiles, missing = [], []
    for pid in PICTURES:
        src = WORK / "raw" / ("pic_%s.png" % pid)
        if not src.exists():
            missing.append(pid)
            continue
        im = Image.open(src).convert("RGB")
        im.save(OUT / (pid + ".webp"), quality=86, method=6)
        tiles.append(im.resize((340, 510)))
    if tiles:
        cols = 5
        sheet = Image.new("RGB", (cols * 340, ((len(tiles) + cols - 1) // cols) * 510), (255, 255, 255))
        for i, t in enumerate(tiles):
            sheet.paste(t, ((i % cols) * 340, (i // cols) * 510))
        sheet.save(WORK / "contact.jpg", quality=85)
    size = sum(p.stat().st_size for p in OUT.glob("*.webp"))
    print("packed", len(tiles), "missing", missing, size, "bytes")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("what", choices=["jobs", "pack"])
    a = ap.parse_args()
    {"jobs": make_jobs, "pack": pack}[a.what]()
