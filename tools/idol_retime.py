"""
Re-times Halgakos's lines of the idol song into the windows where the mouths of the film move, without touching a frame.

    python tools/idol_retime.py SHAPE LANG [--review]      SHAPE 9x16 or 16x9; LANG en, ja, ko or zh

The mouths: the film's own sound (MAGI-2's) was separated with Demucs (tools/clips_bed.py); its vocals stem is where the singers' mouths move.
Its spans, closed up across breaths, are the phrases. Halgakos's lines are taken from her voice-only track (tools/make_mc.py's
OUT-voice.wav, cut by its OUT.times.json), a call and the crowd's answer to it being one unit, and are assigned in order to the phrases:
one unit or several to a phrase, each stretched or squeezed (pitch kept) to its share of it, by no more than LIMITS; a phrase may be left
without a line, and a line without a phrase is left where it was said (the intro, before anyone sings). What is not the voice of the film
is its Demucs bed (the music and effects with the singing taken out), with a little of it held down under her.
Writes dist/cutscenes/idol/retimed/idol-SHAPE-LANG-voice.wav, -mix.wav, -captions.txt, and with --review the film with captions at 720p
under dist/cutscenes/films-review/. Prints what could not be made to fit.
"""
import json
import pathlib
import sys

import numpy
import scipy.signal
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import clips_fit as cf  # noqa: E402
import idol_sync as sy  # noqa: E402

D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes")
RATE = 48000
LIMITS = (.8, 1.4)  # how much a line may be slowed and sped
INTRO = 7.9  # before this second nobody sings: what is said there stays where it was said
LANG_SUFFIX = {"en": "", "ja": "-ja", "ko": "-ko", "zh": "-zh"}


def units_of(spec):
    """Lines grouped: a crowd's answer belongs to the call before it."""
    units = []
    for i, line in enumerate(spec["lines"]):
        units.append([i])
    return units


def phrases_of(vocals_path):
    mask = sy.on_mask(sy.envelope(str(vocals_path)))
    spans = [list(s) for s in sy.spans(mask)]
    out = []
    for a, b in spans:
        if out and a - out[-1][1] < .18:
            out[-1][1] = b
        else:
            out.append([a, b])
    return [(a, b) for a, b in out if b - a >= .5 and a >= INTRO - .3]


def assign(lengths, phrases, starts):
    """Which units go to which phrase, in order, by dynamic programming: cost is how far the squeeze is from none, a phrase left bare costs by its length, a unit dropped a great deal."""
    n, m = len(lengths), len(phrases)
    inf = 1e9
    dp = numpy.full((n + 1, m + 1), inf)
    back = {}
    dp[0][0] = 0
    for i in range(n + 1):
        for j in range(m + 1):
            here = dp[i][j]
            if here >= inf:
                continue
            if j < m:
                # leave the phrase bare
                cost = here + .9 * (phrases[j][1] - phrases[j][0])
                if cost < dp[i][j + 1]:
                    dp[i][j + 1], back[(i, j + 1)] = cost, (i, j, 0)
                # give it units i .. i+k-1
                total = 0.0
                for k in range(1, 5):
                    if i + k > n:
                        break
                    total += lengths[i + k - 1] + (.1 if k > 1 else 0)
                    if abs(phrases[j][0] - starts[i + k - 1]) > 4.0:
                        continue
                    factor = total / (phrases[j][1] - phrases[j][0])
                    lo, hi = LIMITS
                    off = numpy.log(factor / min(hi, max(lo, factor))) if factor else 0
                    cost = here + 3 * numpy.log(factor) ** 2 + 12 * off * off + 1.5 * abs(phrases[j][0] - starts[i])
                    if cost < dp[i + k][j + 1]:
                        dp[i + k][j + 1], back[(i + k, j + 1)] = cost, (i, j, k)
            if i < n:
                cost = here + 12
                if cost < dp[i + 1][j]:
                    dp[i + 1][j], back[(i + 1, j)] = cost, (i, j, -1)
    # trace back
    placed, i, j = [None] * n, n, m
    while (i, j) != (0, 0):
        pi, pj, k = back[(i, j)]
        if k > 0:
            for u in range(pi, i):
                placed[u] = pj
        i, j = pi, pj
    return placed


def stretch_to(sound, seconds):
    factor = (len(sound) / RATE) / max(.2, seconds)
    return cf.stretch(sound, factor, RATE), factor


def main():
    global INTRO
    shape, lang = sys.argv[1], sys.argv[2]
    film = "prologue" if shape == "prologue" else "idol"
    spec = json.loads((ROOT / "tools" / "cutscenes" / f"{film}-mc.json").read_text(encoding="utf-8"))
    songs = D / "idol" / "songs" if film == "idol" else D / "opening"
    out_dir = D / ("idol" if film == "idol" else "opening") / "retimed"
    out_dir.mkdir(exist_ok=True)
    stem = "idol-halgakos" if film == "idol" else "prologue-halgakos"
    vocals_path = D / "idol" / f"idol-{shape}-vocals.wav" if film == "idol" else D / "opening" / "opening-seedance-vocals.wav"
    bed_path = D / "idol" / f"idol-{shape}-bed.wav" if film == "idol" else D / "opening" / "opening-seedance-bed.wav"
    if film == "prologue":
        INTRO = 4.6
    base = songs / f"{stem}{LANG_SUFFIX[lang]}"
    voice, rate = soundfile.read(str(base) + "-voice.wav")
    voice = voice.mean(axis=1) if voice.ndim > 1 else voice
    voice = scipy.signal.resample_poly(voice, RATE, rate).astype("float32")
    times = json.loads(pathlib.Path(str(base) + ".times.json").read_text(encoding="utf-8"))
    units = units_of(spec)
    cut = []
    for unit in units:
        a = min(times[i][0] for i in unit)
        b = max(times[i][1] for i in unit) + (.12 if spec["lines"][unit[0]][3] != "crowd" else .35)
        cut.append((a, b, voice[int(a * RATE):int(b * RATE)]))
    phrases = phrases_of(vocals_path)
    lengths = [len(c[2]) / RATE for c in cut]
    # English lengths decide which unit goes where, so that every language has the same places; only the squeeze differs.
    if lang != "en":
        eng = json.loads((songs / f"{stem}.times.json").read_text(encoding="utf-8")) if (songs / f"{stem}.times.json").exists() else times
        lengths_en = [max(eng[i][1] for i in u) - min(eng[i][0] for i in u) + .12 for u in units]
    else:
        lengths_en = lengths
    movable = [k for k, u in enumerate(units) if times[u[0]][0] >= INTRO - .3]
    # The short calls (Squish! Sloppy~ Louder) are put first, each on the nearest mouth that begins within 1.8 s of where it was said: a call and its answer go one to a mouth, so that no mouth is left silent between them.
    starts_of = {k: times[units[k][0]][0] for k in movable}
    taken, pinned = set(), {}
    for k in movable:
        if lengths_en[k] <= 1.7:
            best = None
            for j, (a, b) in enumerate(phrases):
                if j in taken or b - a < .45 or abs(a - starts_of[k]) > 1.8:
                    continue
                if best is None or abs(a - starts_of[k]) < abs(phrases[best][0] - starts_of[k]):
                    best = j
            if best is not None:
                taken.add(best)
                pinned[k] = best
    rest = [k for k in movable if k not in pinned]
    free = [j for j in range(len(phrases)) if j not in taken]
    got = assign([lengths_en[k] for k in rest], [phrases[j] for j in free], [starts_of[k] for k in rest])
    pick_of = dict(pinned)
    for k, g in zip(rest, got):
        pick_of[k] = free[g] if g is not None else None
    pick = [pick_of[k] for k in movable]
    track = numpy.zeros(90 * RATE, dtype=numpy.float32)
    rows, notes = [], []  # rows: (begins, ends, number of the unit)
    W = {"en": [l[2] for l in spec["lines"]], **{k: spec["tongues"][k]["lines"] for k in ("ja", "ko", "zh")}}

    def words(language, u):
        return " ".join(W[language][i] for i in units[u])

    # the intro stays where it was said
    for k, (a, b, piece) in enumerate(cut):
        if k not in movable:
            s = int(a * RATE)
            track[s:s + len(piece)] += piece[: len(track) - s]
            rows.append((a, b, k))
            notes.append(f"{a:5.1f} s intro, left where it was said: nobody's mouth moves ({words(lang, k)})")
    byphrase = {}
    for u, ph in zip(movable, pick):
        if ph is None:
            a0 = cut[u][0]
            s0 = int(a0 * RATE)
            track[s0:s0 + len(cut[u][2])] += cut[u][2][: len(track) - s0]
            rows.append((a0, a0 + len(cut[u][2]) / RATE, u))
            notes.append(f"{a0:5.1f} s no mouth window within 3 s: left where it was said ({words('en', u)})")
        else:
            byphrase.setdefault(ph, []).append(u)
    free = 0.0
    for ph, us in sorted(byphrase.items()):
        a, b = phrases[ph]
        total = sum(lengths[u] for u in us) + .1 * (len(us) - 1)
        scale = (b - a) / total
        at = a
        for u in us:
            # (Squeezed or slowed by LIMITS at most: a line that will not fit runs past its window and is said so, never crammed.)
            share = min(max(lengths[u] * scale, lengths[u] / LIMITS[1]), lengths[u] / LIMITS[0])
            piece, factor = stretch_to(cut[u][2], share)
            over = at + share - b
            at = max(at, free)
            s = int(at * RATE)
            track[s:s + len(piece)] += piece[: len(track) - s]
            rows.append((at, at + len(piece) / RATE, u))
            flag = f" <-- does not fit: runs {over:.1f} s past its window, needs trimming" if over > .25 else ""
            if at + share > 90:
                flag += " <-- runs past the end of the film"
            notes.append(f"{at:5.1f}-{at + share:5.1f} s phrase {a:.1f}-{b:.1f}: x{factor:.2f}  {words(lang, u)}{flag}")
            free = at + share + .08
            at += share + .08
    # A mouth that is left with no line (0.6 s or more) is given a call she already has: "Squish!" for a short one, "Louder, zaako." for a longer, said again, not a new lyric.
    short_unit = next(k for k, u in enumerate(units) if spec["lines"][u[0]][2].startswith("Squish!") and spec["lines"][u[0]][3] == "call" and times[u[0]][0] >= INTRO)
    long_unit = next(k for k, u in enumerate(units) if spec["lines"][u[0]][2].startswith("Louder"))
    filled = []
    for j, (a, b) in enumerate(phrases):
        if j in byphrase or b - a < .6:
            continue
        k = long_unit if b - a >= 1.2 else short_unit
        piece, factor = stretch_to(cut[k][2], min(max(b - a, lengths[k] / LIMITS[1]), lengths[k] / LIMITS[0]))
        s = int(a * RATE)
        track[s:s + len(piece)] += piece[: len(track) - s]
        rows.append((a, a + len(piece) / RATE, k))
        filled.append(f"{a:.1f}-{b:.1f}")
        notes.append(f"{a:5.1f}-{b:5.1f} s mouth with no line: filled with her call again, x{factor:.2f}  {words(lang, k)}")
    bare = [f"{a:.1f}-{b:.1f}" for j, (a, b) in enumerate(phrases) if j not in byphrase and f"{a:.1f}-{b:.1f}" not in filled]
    notes.append("mouths still with no voice (shorter than 0.6 s): " + (", ".join(bare) if bare else "none"))
    peak = numpy.abs(track).max()
    track *= .9 / peak if peak > .9 else 1
    soundfile.write(out_dir / f"{film}-{shape}-{lang}-voice.wav", track, RATE)
    rows.sort()
    (out_dir / f"{film}-{shape}-{lang}-captions.txt").write_text("\n".join(f"{a:.2f}\t{b:.2f}\t{words(lang, u)}" for a, b, u in rows) + "\n", encoding="utf-8")

    # The mix. The music is the instrumental her track was made over (spec["band"]), at its own timing, which was fitted to the 90 seconds
    # of the film: only her voice moved. Her voice is 11.6 dB above it where she speaks, as in the mix that was accepted; the music is
    # held down 3.3 dB under her and is at about -23 dB where she is not. The film's own bed (Demucs, no singing) is laid under both, quietly.
    def fit90(x):
        return numpy.pad(x, (0, max(0, 90 * RATE - len(x))))[: 90 * RATE].astype("float32")

    def level(x, mask=None):
        x = x if mask is None else x[mask]
        return float(numpy.sqrt((x ** 2).mean()) + 1e-9)

    db = lambda r: 20 * numpy.log10(r + 1e-9)
    band = fit90(cf.read_audio(D / spec["band"]))
    bed = fit90(cf.read_audio(bed_path))
    act = numpy.abs(track) > .02
    env = numpy.convolve(act.astype(float), numpy.ones(RATE // 6) / (RATE // 6), mode="same")
    env = numpy.clip(numpy.convolve(numpy.clip(env * 1.5, 0, 1), numpy.ones(RATE // 8) / (RATE // 8), mode="same"), 0, 1)
    voice_part = track * (10 ** (-14.9 / 20) / level(track, act))
    music = band * (10 ** (-23.2 / 20) / level(band)) * (1 - .31 * env)
    effects = bed * (level(band) * (10 ** (-23.2 / 20) / level(band)) * .35 / level(bed)) * (1 - .31 * env)
    mix = voice_part + music + effects
    peak = numpy.abs(mix).max()
    mix *= .93 / peak if peak > .93 else 1
    soundfile.write(out_dir / f"{film}-{shape}-{lang}-mix.wav", mix, RATE)
    lines_db = ["", "level of each 10 s of the mix (dB full scale, RMS): voice where she speaks / music / the film's own bed / music under the voice"]
    for a in range(0, 90, 10):
        sl = slice(a * RATE, (a + 10) * RATE)
        on = act[sl]
        v = db(level(voice_part[sl], on)) if on.any() else float("nan")
        mu = db(level(music[sl]))
        mu_under = db(level(music[sl], on)) if on.any() else float("nan")
        lines_db.append(f"{a:3d}-{a + 10:<3d} s  voice {v:6.1f}  music {mu:6.1f}  bed {db(level(effects[sl])):6.1f}  voice over music while she speaks {v - mu_under:5.1f} dB")
    notes += lines_db
    (out_dir / f"{film}-{shape}-{lang}-notes.txt").write_text("\n".join(notes) + "\n", encoding="utf-8")
    print("\n".join(notes))
    if "--review" in sys.argv:
        target = D / "films-review" / (f"prologue-{lang}-retimed.mp4" if film == "prologue" else f"idol-{'upright' if shape == '9x16' else 'wide'}-{lang}-retimed.mp4")
        target.parent.mkdir(exist_ok=True)
        size = (720, 1280) if shape == "9x16" else (1280, 720)
        capt = [(a, b, words(lang, u).replace("\u2661", "")) for a, b, u in rows]
        print(f"glyphs the {lang} caption font cannot draw:", cf.missing_glyphs("".join(c[2] for c in capt), lang) or "none")
        cf.write_video(D / ("idol" if film == "idol" else "opening") / (f"idol-{shape}.mp4" if film == "idol" else "opening-seedance.mp4"), target, mix, captions=capt, bitrate=1_500_000, size=size, foot=.80 if shape == "9x16" else None, lang=lang)
        print("review copy", target, f"{target.stat().st_size / 1e6:.1f} MB")
    if "--ship" in sys.argv and film == "idol":
        ship(shape, lang, mix, rows, words, spec)


def ship(shape, lang, mix, rows, words, spec):
    """What goes into the app: her track for this shape and language (AAC mono 96 kb/s), and what is written under it in each language."""
    import av

    app = ROOT / "app" / "src" / "main"
    name = f"idol_{'tall' if shape == '9x16' else 'wide'}_h" + ("" if lang == "en" else f"_{lang}")
    out = av.open(str(app / "res" / "raw" / f"run_film_{name}.m4a"), "w")
    stream = out.add_stream("aac", rate=RATE, layout="mono")
    stream.bit_rate = 96000
    pcm = numpy.clip(mix, -1, 1).astype("float32")
    for i in range(0, len(pcm), 1024):
        frame = av.AudioFrame.from_ndarray(pcm[i:i + 1024].reshape(1, -1), format="flt", layout="mono")
        frame.sample_rate = RATE
        frame.pts = i
        for packet in stream.encode(frame):
            out.mux(packet)
    for packet in stream.encode():
        out.mux(packet)
    out.close()
    for cap in ("en", "ja", "ko", "zh"):
        (app / "assets" / "films" / f"{name}.{cap}.txt").write_text("\n".join(f"{a:.2f}\t{b:.2f}\t{words(cap, u)}" for a, b, u in rows) + "\n", encoding="utf-8")
    print("shipped", name)


if __name__ == "__main__":
    main()



