"""
The game's visual effects, as animated sprite sheets.

    python tools/make_vfx.py jobs [NAME ...] [--redo]   writes the jobs for gen.py (three shards), one grid of twelve frames of an
                                                        effect on a black background each (what has been drawn is not drawn again)
    python tools/make_vfx.py pack [NAME ...]            keys the black out of every drawn grid, trims the frames to the one box they
                                                        all fit in (centred, so what grows grows from its middle), and writes the
                                                        single-row sheets (lossy WebP) into runart/src/main/assets/runart/vfx/, the
                                                        originals (the drawn grids, as they came) into assets/vfx/, and a preview
    python tools/make_vfx.py preview                    a contact sheet, and a GIF of each, on a dark and on a light ground

The black becomes alpha by brightness (a glow has no edge of its own, and on black what is bright is what is there); the colour is kept as
it was over the black, so an effect drawn with the ADD blend looks as it was drawn, and one drawn the normal way has no dark fringe.
The catalogue below is the one in Vfx.java: keep them in step (name, frames, fps, loop, blend).
"""
import argparse, json, pathlib, sys
import numpy as np
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "runart/src/main/assets/runart/vfx"
ORIG = ROOT / "assets/vfx"
# The plus coins (and their pop) are Plus run's own art: a folder of their own in the pack, and in the originals.
OUT_COINS = ROOT / "runart/src/main/assets/runart/coins"
ORIG_COINS = ROOT / "assets/plus-run/coins"
SOLID = {"plus_coin", "plus_silver", "plus_gold", "plus_prism", "gem_glossary"}
COINS = {"plus_coin", "plus_silver", "plus_gold", "plus_prism", "gem_glossary", "coin_pop"}
SCRATCH = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\vfx")
RAW = SCRATCH / "raw"
CANVAS = (1536, 1024)
COLS, ROWS = 4, 3
FRAMES = COLS * ROWS
CENTRED = {"flames", "poison", "stun", "poof", "dust", "plus_coin", "plus_gold", "plus_silver", "plus_prism", "gem_glossary", "clock_shards", "time_sand", "legion_wisps", "petal_storm"}

# name: (what it is, fps, loops, additive, px (the longest side of a frame))
EFFECTS = {
    "plus_coin": ("a glossy collectible coin shaped like a thick plus sign (a cross with equal arms and rounded ends): a deep red ruby-gem plus with bright glossy highlights and "
                  "facets, set in a bevelled gold rim, with a crisp clean edge and no glow, halo, cloud or mist round it at all. It turns slowly and steadily around its vertical axis through one full turn, so that over the twelve "
                  "frames it is seen from the front, then narrowing as it turns edge-on, then from the back, then edge-on, then front again, with a white light glint sweeping across "
                  "its face as it turns; it stays recognisably a plus at every angle except edge-on, and the last frame leads smoothly into the first", 14, True, False, 128),
    "plus_gold": ("a glossy collectible coin shaped like a thick plus sign (a cross with equal arms and rounded ends): a bright polished gold plus with rose-gold facets and white "
                  "highlights, set in a thin warm-orange rim, with a soft golden glow and two tiny sparkles. It turns slowly and steadily around its vertical axis through one full "
                  "turn, so that over the twelve frames it is seen from the front, then narrowing as it turns edge-on, then from the back, then edge-on, then front again, with a white "
                  "light glint sweeping across its face as it turns; the last frame leads smoothly into the first", 14, True, False, 128),
    "plus_silver": ("a glossy collectible coin shaped like a thick plus sign (a cross with equal arms and rounded ends): a polished silver-white plus with pale-blue steel facets and "
                    "bright white highlights, set in a bevelled silver rim with small studs, with a crisp clean edge and no glow, halo, cloud or mist round it at all. It turns slowly and steadily around its vertical axis through one full turn, so that over the twelve frames it is seen from the front, then narrowing as it turns edge-on, then from the back, then edge-on, then front again, with a white light glint sweeping across its face as it turns; the last frame leads smoothly into the first", 14, True, False, 128),
    "plus_prism": ("a rare glossy collectible coin shaped like a thick plus sign (a cross with equal arms and rounded ends): a prismatic crystal plus whose facets shine in rainbow "
                   "colours (pink, gold, green, cyan, violet), set in a bevelled white-gold rim with tiny gems, with a crisp clean edge and no halo, cloud or mist round it, and just a few tiny sparkles. It turns slowly and steadily around its vertical axis through one full turn, so that over the twelve frames it is seen from the front, then narrowing as it turns edge-on, then from the back, then edge-on, then front again, with a white light glint sweeping across its face as it turns; the last frame leads smoothly into the first", 14, True, False, 144),
    "gem_glossary": ("a rare collectible multiplier gem: a large faceted diamond-cut gemstone in glowing aqua and violet with a bright white core, a small glowing multiplication "
                     "cross mark (like an x) shining inside it, and a gold collar and tiny floating sparkles. It turns slowly and steadily around its vertical axis through one full "
                     "turn over the twelve frames, with a white light glint sweeping across its facets as it turns and a gentle pulse of glow; the last frame leads smoothly into the first", 14, True, True, 144),
    "coin_pop": ("a small pop when a coin is picked up: a bright gold and white flash from a point, a thin ring of light expanding, five little gold plus signs and round sparkles "
                 "flying outward and fading, with a tiny soft glow", 26, False, True, 160),
    "slash_light": ("a quick sword slash: a thin white-cyan crescent arc drawn left to right across the cell like a smile (curving up at both ends); it begins as a hair-thin bright line, "
                    "widens into a glowing crescent with a hot white edge and a few sparks, then thins out and fades", 30, False, True, 192),
    "slash_heavy": ("a heavy sword slash: a thick orange and white crescent arc drawn left to right across the cell like a smile, with a hot white core, a red-orange edge, smear lines and a "
                    "burst of sparks at its lowest point; it swings in, flares, then breaks into embers", 26, False, True, 224),
    "slash_crescent": ("a sweeping crescent wave: a large pink and white moon-shaped energy blade, curved like a smile, with feathered glowing edges and drifting petals of light, "
                       "that grows from a thin line to a wide crescent and fades into sparkles", 26, False, True, 224),
    "slash_cross": ("two slashes crossing to make an X: a white-gold diagonal slash from upper left to lower right, then a second from upper right to lower left, each a thin bright "
                    "streak with a glowing core; the X flashes at its middle and fades with sparks", 28, False, True, 224),
    "thrust": ("a thrust: a bright narrow spear-shaped streak shooting to the right across the cell, with a sharp white star flash at its tip, a few thin speed lines behind it and "
               "small sparks; it stretches out, flares at the tip, and fades", 30, False, True, 224),
    "impact_star": ("a blunt impact: a comic-book star burst with eight sharp points, a white-yellow core and an orange edge, expanding from nothing, with short debris sparks, "
                    "then fading and breaking into small stars", 28, False, True, 192),
    "shockwave": ("a shockwave on the ground: a flat horizontal elliptical ring of pale gold light expanding outward from the middle of the cell, seen from slightly above, with a "
                  "thin bright rim, a few lines of light along the ground and puffs of dust at the edge; it fades as it widens", 24, False, True, 256),
    "ink_splash": ("glossy deep blue-violet ink splashing outward from a point in a crown shape, with round droplets flying off and bright cyan highlights on the rims of the ink; "
                   "it rises, spreads, then falls apart into droplets", 24, False, False, 192),
    "ink_explosion": ("an explosion of ink: a radial burst of glossy blue-violet ink with long splashing tendrils and many round droplets, bright cyan highlights on the rims, "
                      "a pale white-blue flash at the very start", 24, False, True, 224),
    "ink_splat": ("an ink splat landing on a wall: glossy blue-violet ink hitting and spreading into an irregular blot with spiky edges, small satellite droplets and two or "
                  "three drips running down; it spreads quickly in the first frames, then holds nearly still, and in the last four frames the same blot just becomes fainter and fainter until it is gone (its own colour only, with no grey or white fog, haze or box around it)", 22, False, False, 192),
    "fire_burst": ("a burst of fire: a round fireball that blooms outward from a white-yellow core into orange and red flames with licking tongues and flying embers; it flares, "
                   "then breaks into rising flames and sparks", 26, False, True, 224),
    "flames": ("burning flames that loop: a small bright campfire-like column of orange and yellow flames with a white-hot base, tongues flickering and rising and a few embers, "
               "drawn so that the last frame leads smoothly into the first", 20, True, True, 192),
    "ice_burst": ("a burst of ice: sharp pale-blue crystal shards bursting outward from a bright white-cyan flash, with frost sparkles and snow; the shards fly out, "
                  "shine, and fade", 26, False, True, 224),
    "freeze": ("a freeze: a glassy pale-blue block of ice forms around the middle of the cell (frost spreading, then a solid shining crystal block), then cracks and shatters into "
               "flying shards in the last four frames", 22, False, True, 224),
    "poison": ("a poison cloud that loops: a swirling cloud of purple-green toxic gas with glossy bubbles rising out of it and popping, a sickly green glow inside; drawn so that "
               "the last frame leads smoothly into the first", 14, True, False, 192),
    "stun": ("stun stars that loop: three yellow five-pointed stars with white cores circling around a point in the middle of the cell in an ellipse (as over a dizzy head), "
             "with tiny sparkles; drawn so that the last frame leads smoothly into the first", 14, True, True, 192),
    "lightning": ("a lightning strike: a jagged white-blue bolt striking down from the top of the cell to the middle, branching, with a bright flash at the ground, electric arcs "
                  "and sparks; it flashes, forks, and fades", 28, False, True, 256),
    "sparkle": ("a magic sparkle burst: pink, gold and white four-pointed twinkling stars and round glints flying outward from a bright point, popping and fading at different "
                "times, with a soft glow", 22, False, True, 192),
    "heal": ("a healing pillar: a column of soft green and white light rising from the bottom of the cell, with expanding rings, rising glowing plus signs and sparkles; it "
             "grows, shines, and lifts away", 20, False, True, 224),
    "crit": ("a critical hit flash: a huge white-yellow starburst with long radial rays and a sharp bright core, the rays snapping out, a ring of light popping, then a fast "
             "fade with sparks", 30, False, True, 256),
    "shield_break": ("a shield breaking: a glowing blue crystalline round shield with a bright rim cracks across its face and shatters into flying hexagonal shards with a flash "
                     "of white light", 24, False, True, 224),
    "poof": ("a puff of smoke: a round white-grey smoke cloud bursting out from the middle with a few small stars and a faint pale wisp rising from it, then thinning and "
             "drifting apart", 20, False, False, 192),
    "dust": ("a dust cloud: a low brown-grey puff of dust kicked up along the ground, rolling outward and rising, with small flecks, thinning at the end", 18, False, False, 192),
    "feathers": ("white feathers on a gust of wind: a trail of soft white feathers swirling to the right in a loose spiral with pale blue streaks of wind, tumbling and fading", 20,
                 False, True, 224),
    "beam_impact": ("a beam hitting a point: a cyan and white burst with a bright core, two rings of light expanding, and sparks flying out in a fan to the left; it flares, then "
                    "fades", 26, False, True, 224),
    "beam_start": ("a beam starting: a charging orb of cyan and white light that gathers sparks into it, swells, and flashes out into a short bright cone of light pointing right", 26,
                   False, True, 224),
    "explosion": ("a big explosion: a white-yellow flash, an orange fireball swelling with a dark red edge and black-grey smoke, a ring shockwave and flying debris sparks; "
                  "it swells, peaks, and rolls into smoke", 22, False, True, 256),

    # The ultimates' (see RunUltimateFx): drawn as real fire, ink, sand and petals, not as shapes. The auras loop; the others play once.
    "rampage_body": ("realistic roiling flames that loop, wrapping a hollow column the height of a standing person (narrow and tall): thick tongues of orange and yellow flame with white-hot "
                     "cores and deep red edges lick up both sides and curl over the top, the middle of the column left empty and dark so a figure can stand in it, with sparks and glowing "
                     "embers rising off the flames; drawn so that the last frame leads smoothly into the first", 20, True, True, 320),
    "rampage_crown": ("a crown of dark flames that loops, over the top of a head: a ring of tall wavering tongues of black-red fire with glowing orange edges and a few rising embers, thick "
                      "smoky black at their tips, seen from the front and low (a flame diadem); drawn so that the last frame leads smoothly into the first", 20, True, False, 192),
    "rampage_burst": ("an eruption of fire: a ring of realistic flame bursting outward from the middle in one great circle, a white-yellow flash at the start, towering tongues of orange and red flame "
                      "shooting up and out, a shockwave ring, thick embers and sparks flying in all directions; it erupts, roars, and rolls apart into embers", 24, False, True, 320),
    "clock_shards": ("a clock shattered in mid-air that loops: a round cracked glass clock face broken into curved, sharp shards of pale blue-white glass that float slowly apart and turn round "
                     "a hollow middle, with fine cracks of light between them, a few thin gold clock hands and tiny cyan glints; drawn so that the last frame leads smoothly into the first", 14, True, True, 256),
    "time_sand": ("a spiral of sand that loops: fine golden-white grains of sand and tiny glowing time particles drifting in a slow rising spiral round an empty middle, with a few "
                  "soft streaks of pale gold light; drawn so that the last frame leads smoothly into the first", 14, True, True, 256),
    "ink_wave": ("a wave of ink that loops, rushing to the right: a glossy liquid wave of deep blue-black ink with a curling crest, bright cyan and white highlights along its wet rim, thin "
                 "tendrils of ink peeling off the crest and many round droplets and splashes flying ahead of it; drawn so that the last frame leads smoothly into the first", 22, True, False, 320),
    "titan_stride": ("a heavy footstep landing: two flat expanding rings of brown dust bursting out along the ground to both sides, a cloud of grey-brown dust rolling up, cracks spreading "
                     "in the ground and small stones flying; it slams down, spreads, and thins out. Only the dust and stones themselves are drawn: no grey haze, no fog, no veil, no vignette, no "
                     "dark rectangle round the cloud, and the empty space round it fully transparent in every frame", 20, False, False, 320),
    "legion_wisps": ("ghostly afterimages that loop: translucent violet and white flowing ribbons of spirit light in the shape of trailing, blurred human silhouettes, drifting up and to the "
                     "left like smoke, with soft glowing edges and tiny sparkles; drawn so that the last frame leads smoothly into the first", 16, True, True, 256),
    "petal_storm": ("a storm of rose petals that loops: dozens of pink, red and white rose petals swirling in a tall loose spiral round an empty middle, each petal soft and "
                    "velvety, a few golden sparkles among them; drawn so that the last frame leads smoothly into the first", 18, True, False, 256),
    "rose_burst": ("a rose blooming: a closed rose bud in the middle opens at once into a full pink and red rose, soft petals bursting outward in a ring with sparkles and a rising "
                   "pale pink glow; it opens, flares, and the petals drift away", 22, False, False, 256),
}

# (The ultimates' are drawn as real fire, ink and petals, not as anime shapes.)
COMMON_REAL = (" Fully transparent background (a PNG with an alpha channel) in every cell: no backdrop colour, no black or grey box, no ground, no border lines between the cells, no text, no labels, "
               "no numbers, no characters, no people. Realistic, high-end particle visual effects as in a modern game engine: volumetric, soft, detailed and physically believable, with natural "
               "colour gradients, motion blur and fine particles, not a cartoon and not a flat vector shape. Each frame is centred in its own cell and well inside it, never touching the "
               "cell's edge or crossing into another cell.")
# (Drawn with a flat grey veil over the cell that the model could not leave out: cleaned as a glow is, though it is drawn the normal way.)
VEILED = {"titan_stride", "petal_storm", "rose_burst", "legion_wisps"}
REAL = {"rampage_body", "rampage_crown", "rampage_burst", "clock_shards", "time_sand", "ink_wave", "titan_stride", "legion_wisps", "petal_storm", "rose_burst"}

COMMON = (" Fully transparent background (a PNG with an alpha channel) in every cell: no backdrop colour, no black or grey box, no ground, no border lines between the cells, no text, no labels, no numbers, no characters, no "
          "people. Polished anime action-game visual effects as in a high-end mobile gacha game: a hot white core, saturated colour at the edges, sharp clean shapes, glow, "
          "sparks and smear lines. Each frame is centred in its own cell and well inside it, never touching the cell's edge or crossing into another cell.")


# The ultimates' effects as they are used now: paler, as the anime's own spell effects (cel-shaded, soft gradients, pastel), to sit with the game's painterly pastel art.
# (The realistic ones above, COMMON_REAL and REAL, are kept in the tool and not used: put a name in REAL_STYLE to draw it that way again.)
REAL_STYLE = set()
COMMON_PALE = (" Fully transparent background (a PNG with an alpha channel) in every cell: no backdrop colour, no black or grey box, no ground, no haze, no fog, no border lines between the cells, no text, "
               "no labels, no numbers, no characters, no people. Pale, pastel anime spell effects as in a TV anime: cel-shaded with soft gradients, clean flowing shapes, thin light outlines, "
               "gentle glow, low saturation and low contrast, light airy colours (never dark, never heavy, never photographic or photoreal), still dynamic and spectacular. Each frame is "
               "centred in its own cell and well inside it, never touching the cell's edge or crossing into another cell.")
PALE_WHAT = {
    "rampage_body": "pale anime flames that loop, wrapping a hollow column the height of a standing person (narrow and tall): cel-shaded flame tongues in pale rose-pink, cream and white-gold with "
                    "thin light outlines and soft glows, curling up both sides and over the top, the middle of the column left empty and clear so a figure can stand in it, a few small light "
                    "sparks rising; drawn so that the last frame leads smoothly into the first",
    "rampage_crown": "a crown of pale anime flames that loops, in a flat ring over the top of a head: a ring of tall wavering cel-shaded tongues in pale rose, pink and white-gold with soft glows "
                     "and a few small sparks, light and airy, no black and no dark smoke; drawn so that the last frame leads smoothly into the first",
    "rampage_burst": "an eruption of pale anime fire: a ring of cel-shaded flame bursting outward from the middle in one great circle, a white-gold flash at the start, graceful pale rose, pink and "
                     "cream flame tongues shooting up and out, a thin shockwave ring and light sparks; it erupts, flares, and fades into floating sparks",
    "clock_shards": "a clock shattered in mid-air that loops: a round pale clock face broken into curved, sharp shards of soft lavender and white glass that float slowly apart and turn round a "
                    "hollow middle, with thin light cracks between them, thin pale-gold clock hands and tiny glints, cel-shaded with soft gradients; drawn so that the last frame leads smoothly into the first",
    "time_sand": "a spiral of pale sand that loops: fine cream and pale-gold grains and tiny glowing sparkles drifting in a slow rising spiral round an empty middle, with a few soft light "
                 "streaks, anime style; drawn so that the last frame leads smoothly into the first",
    "ink_wave": "a wave of translucent pale-blue ink that loops, rushing to the right: a cel-shaded anime wave in light sky blue and pale periwinkle with a curling crest, white highlights and "
                "foam-like swirls along its rim, thin pale tendrils and round droplets flying ahead of it; light and see-through, not dark; drawn so that the last frame leads smoothly into the first",
    "titan_stride": "a heavy footstep landing, in anime style: two flat expanding rings of cream and pale-beige dust bursting out along the ground to both sides, a soft cel-shaded puff of cream "
                    "dust rolling up, thin pale cracks in the ground and a few small light stones flying; it slams down, spreads, and thins out. Only the dust and stones are drawn: no grey haze, no veil",
    "legion_wisps": "ghostly afterimages that loop, in anime style: translucent pale lilac and white flowing ribbons of spirit light in the shape of trailing, blurred human silhouettes, drifting up and "
                    "to the left like soft smoke, cel-shaded with gentle glowing edges and tiny sparkles; drawn so that the last frame leads smoothly into the first",
    "petal_storm": "a storm of pastel petals that loops: dozens of pale pink, cream and white rose petals swirling in a tall loose spiral round an empty middle, cel-shaded with thin light outlines, "
                   "and a few small golden sparkles; drawn so that the last frame leads smoothly into the first",
    "rose_burst": "a pastel rose blooming, in anime style: a closed pale pink rose bud in the middle opens at once into a full soft pink and cream rose with thin light outlines, petals bursting "
                  "outward in a ring with sparkles and a rising pale glow; it opens, flares, and the petals drift away",
}


def prompt(name):
    what = PALE_WHAT[name] if name in PALE_WHAT and name not in REAL_STYLE else EFFECTS[name][0]
    common = COMMON_PALE if name in PALE_WHAT and name not in REAL_STYLE else COMMON_REAL if name in REAL else COMMON
    return (f"A sprite sheet of a game visual effect: {FRAMES} animation frames of {what}. The frames are laid out in a grid of {COLS} columns and {ROWS} rows (left to right, "
            f"top to bottom), one frame in each cell, in order from the first instant to the last, so that played quickly they make one smooth animation, each frame a little "
            f"further along than the one before. All {FRAMES} cells are filled.{common}")


def make_jobs(names, redo):
    SCRATCH.mkdir(parents=True, exist_ok=True)
    RAW.mkdir(exist_ok=True)
    jobs = []
    for name in names:
        suffix = ""
        if redo:
            n = 1
            while (RAW / ("%s_r%d.png" % (name, n))).exists():
                n += 1
            suffix = "_r%d" % n
        elif (RAW / (name + ".png")).exists():
            continue
        jobs.append({"name": name + suffix, "background": "transparent", "size": "%dx%d" % CANVAS, "prompt": prompt(name), "refs": []})
    for s in range(3):
        (SCRATCH / ("jobs_%d.json" % s)).write_text(json.dumps(jobs[s::3]))
    print("jobs", len(jobs))


def key(rgb):
    """Straight colour and alpha from a picture drawn over black: alpha by brightness, colour as it was over the black."""
    a = rgb.astype(np.float32)
    lum = a.max(axis=2)
    alpha = np.clip((lum - 14) / 70.0, 0, 1)
    alpha = np.where(alpha > 0, alpha ** 0.85, 0)
    out = np.zeros(a.shape[:2] + (4,), np.uint8)
    safe = np.maximum(alpha, 1 / 255.0)[..., None]
    out[..., :3] = np.clip(a / safe, 0, 255).astype(np.uint8)
    out[..., 3] = (alpha * 255 + .5).astype(np.uint8)
    return out


def clean(rgba, glow=False, solid=False):
    """A frame that came with its own alpha: faint haze under a cut is dropped, and the colour at the edges is the colour of the effect (not of what was behind it)."""
    import cv2
    a = rgba[..., 3].astype(np.float32) / 255
    a = np.where(a < 0.05, 0, a)
    if glow:
        # (and a flat veil, the same faint alpha over a whole patch of the cell: a tint of the backdrop the model could not leave out. It is the commonest value
        # of a faint alpha, and a lot of the cell.)
        hist = np.bincount((a * 255).astype(int).ravel(), minlength=256)
        peak = 8 + int(hist[8:128].argmax())
        if hist[max(0, peak - 2):peak + 3].sum() > 0.06 * a.size:
            base = (peak + 4) / 255.0
            a = np.clip((a - base) / (1 - base), 0, 1)
        # (the model puts a soft grey haze round a glow: translucent and dark, so on a light ground it is a dirty edge. What is not plainly there and not bright goes.)
        bright = rgba[..., :3].max(axis=2).astype(np.float32)
        keep = np.clip((bright - 95) / 90.0, 0, 1)
        a = np.where(a >= 0.8, a, a * keep)
        a = np.where(a < 0.04, 0, a)
    if solid:
        # (a coin is a thing with an edge: the model's pale cloud round it is not part of it. Whatever is not plainly there and is not beside what is, goes.)
        import cv2
        body = (a >= 0.7).astype(np.uint8)
        near = cv2.dilate(body, np.ones((5, 5), np.uint8)) > 0
        a = np.where(near | (a >= 0.7), a, 0)
        a = np.where(a < 0.12, 0, a)
    out = rgba.copy()
    out[..., 3] = (a * 255 + .5).astype(np.uint8)
    return out


def shrink(rgba, size):
    """A picture made smaller with its colour kept where it is faint: in premultiplied float, not in the eight bits that turn a pale edge dark."""
    import cv2
    a = rgba.astype(np.float32) / 255
    a[..., :3] *= a[..., 3:4]
    small = cv2.resize(a, size, interpolation=cv2.INTER_AREA if size[0] < a.shape[1] else cv2.INTER_CUBIC)
    alpha = small[..., 3:4]
    rgb = np.where(alpha > 1e-4, small[..., :3] / np.maximum(alpha, 1e-4), 0)
    out = np.concatenate([np.clip(rgb, 0, 1), np.clip(alpha, 0, 1)], axis=2)
    return (out * 255 + .5).astype(np.uint8)


def best_draw(name):
    draws = sorted(RAW.glob(name + "*.png"))
    draws = [d for d in draws if d.stem == name or d.stem.startswith(name + "_r")]
    return draws[-1] if draws else None


def pack(names):
    OUT.mkdir(parents=True, exist_ok=True)
    ORIG.mkdir(parents=True, exist_ok=True)
    OUT_COINS.mkdir(parents=True, exist_ok=True)
    ORIG_COINS.mkdir(parents=True, exist_ok=True)
    total = 0
    for name in names:
        draw = best_draw(name)
        if draw is None:
            print("missing", name)
            continue
        full = Image.open(draw).convert("RGBA")
        opaque = np.array(full.getchannel("A")).min() == 255
        im = full.convert("RGB") if opaque else full
        if im.size != CANVAS:
            im = im.resize(CANVAS, Image.LANCZOS)
        cw, ch = CANVAS[0] // COLS, CANVAS[1] // ROWS
        frames = []
        for k in range(FRAMES):
            cell = im.crop(((k % COLS) * cw, (k // COLS) * ch, (k % COLS) * cw + cw, (k // COLS) * ch + ch))
            frames.append(key(np.array(cell)) if opaque else clean(np.array(cell), EFFECTS[name][3] or name in VEILED, name in SOLID))
        # (a cell's own edge is black, and any line the model drew along it is not part of the effect)
        for f in frames:
            f[:3, :, 3] = 0
            f[-3:, :, 3] = 0
            f[:, :3, 3] = 0
            f[:, -3:, 3] = 0
        # (a loop that stays where it is is lined up by the middle of what is drawn, so that it does not wobble)
        if name in CENTRED:
            for k, f in enumerate(frames):
                w = f[..., 3].astype(np.float32)
                if w.sum() < 2000:
                    continue
                cx = (np.arange(f.shape[1]) * w.sum(axis=0)).sum() / w.sum()
                cy = (np.arange(f.shape[0]) * w.sum(axis=1)).sum() / w.sum()
                frames[k] = np.roll(np.roll(f, int(round(ch / 2 - cy)), axis=0), int(round(cw / 2 - cx)), axis=1)
        # the one box they all fit in, centred on the middle of the cell
        reach_x = reach_y = 8
        for f in frames:
            ys, xs = np.nonzero(f[..., 3] > 24)
            if len(xs):
                reach_x = max(reach_x, cw // 2 - xs.min(), xs.max() + 1 - cw // 2)
                reach_y = max(reach_y, ch // 2 - ys.min(), ys.max() + 1 - ch // 2)
        reach_x, reach_y = min(cw // 2, reach_x + 6), min(ch // 2, reach_y + 6)
        box = (cw // 2 - reach_x, ch // 2 - reach_y, cw // 2 + reach_x, ch // 2 + reach_y)
        px = EFFECTS[name][4]
        w, h = box[2] - box[0], box[3] - box[1]
        scale = px / max(w, h)
        fw, fh = max(1, round(w * scale)), max(1, round(h * scale))
        sheet = Image.new("RGBA", (fw * FRAMES, fh), (0, 0, 0, 0))
        for k, f in enumerate(frames):
            piece = Image.fromarray(shrink(f[box[1]:box[3], box[0]:box[2]], (fw, fh)), "RGBA")
            sheet.paste(piece, (k * fw, 0))
        out_dir, orig_dir = (OUT_COINS, ORIG_COINS) if name in COINS else (OUT, ORIG)
        sheet.save(out_dir / (name + ".webp"), quality=84, alpha_quality=90, method=6)
        full.save(orig_dir / (name + ".png"), optimize=True)
        size = (out_dir / (name + ".webp")).stat().st_size
        total += size
        print("%-15s frame %dx%d  %7d bytes" % (name, fw, fh, size))
    print("total", total)


def composite(name, ground, additive):
    """Frames of a packed sheet over a ground, as they would be drawn."""
    sheet = Image.open(OUT / (name + ".webp")).convert("RGBA")
    fh = sheet.size[1]
    fw = sheet.size[0] // FRAMES
    out = []
    for k in range(FRAMES):
        f = np.array(sheet.crop((k * fw, 0, k * fw + fw, fh))).astype(np.float32)
        a = f[..., 3:4] / 255
        base = np.zeros((fh, fw, 3), np.float32) + np.array(ground, np.float32)
        rgb = np.clip(base + f[..., :3] * a, 0, 255) if additive else base * (1 - a) + f[..., :3] * a
        out.append(Image.fromarray(rgb.astype(np.uint8)))
    return out


def preview(names):
    SCRATCH.mkdir(parents=True, exist_ok=True)
    report = SCRATCH / "report"
    report.mkdir(exist_ok=True)
    rows = []
    for name in names:
        if not (OUT / (name + ".webp")).exists():
            continue
        dark = composite(name, (28, 30, 40), EFFECTS[name][3])
        light = composite(name, (236, 238, 242), False) if not EFFECTS[name][3] else composite(name, (120, 124, 140), True)
        gif = dark + light
        gif[0].save(report / (name + ".gif"), save_all=True, append_images=gif[1:], duration=int(1000 / EFFECTS[name][1]) * 2, loop=0)
        rows.append((name, dark))
    if not rows:
        return
    tile = 140
    cols = 6
    sheet_rows = []
    for name, dark in rows:
        strip = Image.new("RGB", (tile * FRAMES, tile), (28, 30, 40))
        for k, f in enumerate(dark):
            f2 = f.copy()
            f2.thumbnail((tile, tile))
            strip.paste(f2, (k * tile + (tile - f2.size[0]) // 2, (tile - f2.size[1]) // 2))
        sheet_rows.append(strip)
    # six effects to a picture
    for part in range(0, len(sheet_rows), 6):
        chunk = sheet_rows[part:part + 6]
        board = Image.new("RGB", (tile * FRAMES, tile * len(chunk)), (28, 30, 40))
        for i, strip in enumerate(chunk):
            board.paste(strip, (0, i * tile))
        board.save(report / ("strip_%d.jpg" % (part // 6)), quality=84)
    print("preview", len(rows))


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("what", choices=["jobs", "pack", "preview"])
    ap.add_argument("names", nargs="*")
    ap.add_argument("--redo", action="store_true")
    a = ap.parse_args()
    names = a.names or [n for n in EFFECTS if n not in COINS]
    for n in names:
        if n not in EFFECTS:
            sys.exit("no such effect: " + n)
    {"jobs": lambda: make_jobs(names, a.redo), "pack": lambda: pack(names), "preview": lambda: preview(names)}[a.what]()
