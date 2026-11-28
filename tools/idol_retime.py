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
# The shots of the storyboard (tools/cutscenes/idol.json: A1, A7, B3, C3, C7) where Halgakos alone is shown, her mouth moving: she is heard wherever it moves, said a little long or squeezed to x1.3 if need be.
H_SHOTS = [(0.0, 4.0), (16.0, 20.0), (24.0, 30.3), (38.0, 42.0), (68.0, 72.0), (84.0, 90.0)]
# The shots where all five sing together (A3-A4, A6, B1-B2, C1-C2, C4-C6 of the storyboard): her voice is the five in unison.
G_SHOTS = [(8.0, 16.0), (20.0, 24.0), (30.0, 38.0), (60.0, 68.0), (72.0, 84.0)]
G_FILL = [((8.0, 16.0), 6), ((20.0, 24.0), 9), ((30.0, 38.0), 16), ((60.0, 68.0), 35), ((72.0, 84.0), 39)]  # the line said again in a mouth of a group shot that has none
UNISON = (("C", .5, .012), ("R", .4, .02), ("I", .5, .006), ("T", .45, .016))
H_CAP = 1.3
DROPPED = {1, 3, 11, 13, 21, 44}  # a lone "Sloppy~" (the old second halves of intro, call, member and ending): folded into the line before it in the spec
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


def shortest_fit(group_lengths, window, cap=1.15):
    """Which form of each line of a group, so that the group fits a mouth at a natural pace: (levels, total seconds, whether it fits). group_lengths: for each line, its lengths from the longest form to the shortest."""
    level = [0] * len(group_lengths)
    total = lambda: sum(g[l] for g, l in zip(group_lengths, level)) + .08 * (len(level) - 1)
    while total() > cap * window:
        options = [i for i, g in enumerate(group_lengths) if level[i] + 1 < len(g)]
        if not options:
            break
        level[max(options, key=lambda i: group_lengths[i][level[i]])] += 1
    return level, total(), total() <= cap * window


def in_g(t):
    return any(a <= t < b for a, b in G_SHOTS)


def in_h(t):
    return any(a <= t < b for a, b in H_SHOTS)


def assign_forms(lengths, phrases, starts, drop=7.0, hph=None, hunit=None):
    """The same as assign, with the forms each line can be said in: a group of lines that cannot be made to fit a mouth costs a great deal, a line left out much less."""
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
                cost = here + .9 * (phrases[j][1] - phrases[j][0])
                if cost < dp[i][j + 1]:
                    dp[i][j + 1], back[(i, j + 1)] = cost, (i, j, 0)
                for k in range(1, 4):
                    if i + k > n or abs(phrases[j][0] - starts[i + k - 1]) > 4.0:
                        break
                    window = phrases[j][1] - phrases[j][0]
                    hot = bool(hph and hph[j])
                    levels, total, fits = shortest_fit(lengths[i:i + k], window, H_CAP if hot else 1.15)
                    if not fits and hot:
                        cost = here + 12 + 6 * (total - H_CAP * window)
                    elif not fits and (k > 1 or window >= .78):
                        cost = here + 40 + 20 * (total - 1.15 * window)
                    elif not fits:
                        cost = here + 40
                    else:
                        cost = here + .5 * sum(levels) + 1.5 * abs(phrases[j][0] - starts[i]) + 2 * abs(numpy.log(max(.3, total / window)))
                    if cost < dp[i + k][j + 1]:
                        dp[i + k][j + 1], back[(i + k, j + 1)] = cost, (i, j, k)
            if i < n:
                cost = here + (60.0 if hunit and hunit[i] else drop)
                if cost < dp[i + 1][j]:
                    dp[i + 1][j], back[(i + 1, j)] = cost, (i, j, -1)
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
    spec = json.loads((ROOT / "tools" / "cutscenes" / ("idol-variants-mc.json" if film == "idol" else f"{film}-mc.json")).read_text(encoding="utf-8"))
    songs = D / "idol" / "songs" if film == "idol" else D / "opening"
    out_dir = D / ("idol" if film == "idol" else "opening") / "retimed"
    out_dir.mkdir(exist_ok=True)
    stem = "idol-variants" if film == "idol" else "prologue-halgakos"
    vocals_path = D / "idol" / f"idol-{shape}-vocals.wav" if film == "idol" else D / "opening" / "opening-seedance-vocals.wav"
    bed_path = D / "idol" / f"idol-{shape}-bed.wav" if film == "idol" else D / "opening" / "opening-seedance-bed.wav"
    if film == "prologue":
        INTRO = 4.6
    base = songs / f"{stem}{LANG_SUFFIX[lang]}"
    voice, rate = soundfile.read(str(base) + "-voice.wav")
    voice = voice.mean(axis=1) if voice.ndim > 1 else voice
    voice = scipy.signal.resample_poly(voice, RATE, rate).astype("float32")
    times = json.loads(pathlib.Path(str(base) + ".times.json").read_text(encoding="utf-8"))
    SONG = 45  # the lines of the song itself; the rest of the spec are its shorter forms
    units = units_of({"lines": spec["lines"][:SONG]}) if film == "idol" else units_of(spec)
    import idol_variants as iv
    order = sorted(iv.V)
    forms_of = {i: [45 + 2 * order.index(i), 45 + 2 * order.index(i) + 1] for i in order} if film == "idol" else {}
    import idol_group as ig
    W = {"en": [l[2] for l in spec["lines"]], **{k: spec["tongues"][k]["lines"] for k in ("ja", "ko", "zh")}}
    starts_all = sorted(x[0] for x in times)

    def load(path):
        """A recorded voice at the film's rate, with the quiet at both ends taken off (a word is not given a long tail of breath)."""
        x, r = soundfile.read(str(path))
        x = x.mean(axis=1) if x.ndim > 1 else x
        x = scipy.signal.resample_poly(x, RATE, r).astype("float32")
        hop = RATE // 100
        env = numpy.sqrt((x[: len(x) // hop * hop].reshape(-1, hop) ** 2).mean(axis=1))
        loud = numpy.where(env > .07 * env.max())[0]
        if len(loud):
            x = x[max(0, (loud[0] - 1) * hop):(loud[-1] + 3) * hop]
        return x

    def edged(x, ms=6):
        x = x.copy()
        n = min(len(x) // 2, int(ms * RATE / 1000))
        if n:
            x[:n] *= numpy.linspace(0, 1, n)
            x[-n:] *= numpy.linspace(1, 0, n)
        return x

    def crowd_piece(i):
        """The crowd's answer: her own shout once, and her whisper of it softly under it, a little after: a deliberate double, not a voice laid over itself."""
        text = W[lang][i]
        n_path, w_path = ig.path_of("H", lang, text, "-n"), ig.path_of("H", lang, text, "-w")
        if not (n_path.exists() and w_path.exists()):
            return None
        n, w = load(n_path), load(w_path)
        n = n / (numpy.abs(n).max() + 1e-9) * .85
        w = w / (numpy.abs(w).max() + 1e-9) * .85
        off = int(.035 * RATE)
        out = numpy.zeros(max(len(n), len(w) + off), dtype="float32")
        out[:len(n)] += n
        out[off:off + len(w)] += .5 * w
        return edged(out)

    def piece_of(i):
        """A line of the recording, cut at its own ends: not into the next line's first sound, and faded at both ends so that nothing clicks."""
        if film == "idol" and spec["lines"][i][3] == "crowd":
            c = crowd_piece(i)
            if c is not None:
                return c
        a0 = times[i][0] - .008
        later = [s for s in starts_all if s > times[i][0] + .01]
        room = (min(later) - times[i][1] - .02) if later else .3
        tail = max(.02, min(.12 if spec["lines"][i][3] != "crowd" else .3, room))
        return edged(voice[max(0, int(a0 * RATE)):int((times[i][1] + tail) * RATE)])

    cut = []
    for unit in units:
        a = min(times[i][0] for i in unit)
        cut.append((a, a, piece_of(unit[0])))
    phrases = phrases_of(vocals_path)
    lengths = [len(c[2]) / RATE for c in cut]
    if lang != "en":
        eng = json.loads((songs / f"{stem}.times.json").read_text(encoding="utf-8")) if (songs / f"{stem}.times.json").exists() else times
        lengths_en = [max(eng[i][1] for i in u) - min(eng[i][0] for i in u) + .12 for u in units]
    else:
        lengths_en = lengths
    movable = [k for k, u in enumerate(units) if times[u[0]][0] >= INTRO - .3 and (film != 'idol' or u[0] not in DROPPED)]
    starts_of = {k: times[units[k][0]][0] for k in movable}
    # Her call section (A7, 24-30 s: Squish! Sloppy~ Squish! Sloppy~ Louder, zaako!) is put on the mouths of that shot one to one, in order, before anything else.
    call_units, call_spans = [], []
    merged = []
    if film == "idol":
        call_units = [k for k in movable if 23.5 <= starts_of[k] < 28.9]
        movable = [k for k in movable if k not in call_units]
        raw = [list(s) for s in sy.spans(sy.on_mask(sy.envelope(str(vocals_path))))]
        for a, b in raw:
            if merged and a - merged[-1][1] < .18:
                merged[-1][1] = b
            else:
                merged.append([a, b])
        call_spans = [(a, b) for a, b in merged if 23.9 <= a < 30.3 and b - a >= .3]
        phrases = [ph for ph in phrases if not (23.9 <= ph[0] < 30.3)]

    def lengths_of(k):
        i = units[k][0]
        base_len = len(cut[k][2]) / RATE
        return [base_len] + [times[j][1] - times[j][0] + .12 for j in forms_of.get(i, [])]

    hph = [in_h((a + b) / 2) for a, b in phrases] if film == "idol" else None
    hunit = [in_h(starts_of[k]) for k in movable] if film == "idol" else None
    got = assign_forms([lengths_of(k) for k in movable], phrases, [starts_of[k] for k in movable], hph=hph, hunit=hunit)
    pick_of = {k: g for k, g in zip(movable, got)}
    pick = [pick_of[k] for k in movable]
    track = numpy.zeros(90 * RATE, dtype=numpy.float32)
    rows, notes = [], []  # rows: (begins, ends, number of the line)
    lane, shifted = [], []

    def words(language, u):
        return W[language][u] if film == "idol" and u >= 0 else " ".join(W[language][i] for i in units[u])

    def unison(piece, i2, at):
        """In a shot where the five sing together, the line is the five of them: her own voice with Chikuwa, Rune, Illya and Tsubakieh saying the same line, each in her own voice, to the same length."""
        if film != "idol" or not in_g(at):
            return piece
        mixed, used = piece.astype("float32").copy(), 0
        for letter, gain, off in UNISON:
            path = ig.path_of(letter, lang, W[lang][i2])
            if not path.exists():
                continue
            g = load(path)
            g = cf.stretch(g, min(1.9, max(.6, len(g) / len(piece))), RATE)
            g = edged(g[: len(piece)])
            o = int(off * RATE)
            g = g[: max(0, len(mixed) - o)]
            mixed[o:o + len(g)] += gain * g
            used += 1
        if used:
            peak = numpy.abs(mixed).max()
            mixed *= max(numpy.abs(piece).max(), .3) / peak if peak > 0 else 1
        return mixed

    def put(piece, at, i2=None, wait=True, maxf=1.5, pin=False):
        """
        Lays a line in the track where it begins, never over another line of hers and never cutting one off. If another is still being said where it
        begins, it waits for it (by up to 0.6 s); if the next begins before it ends, it is said faster to fit before it (by up to x1.5); if neither will do, it
        is not laid (None). It is never cut off at the end of the film.
        """
        n = len(piece) / RATE
        start = at
        if pin:
            # Her own mouth comes first: the line is laid on it, at its onset, never delayed and never left out.
            if start + n > 89.97:
                start = max(0.0, 89.97 - n)
            s = int(start * RATE)
            track[s:s + len(piece)] += piece[: len(track) - s]
            lane.append((start, start + n))
            if i2 is not None:
                rows.append((start, start + n, i2))
            return start
        running = [e0 for s0, e0 in lane if s0 <= at + .02 < e0 - .02]
        if running:
            if not wait:
                return None
            start = max(running) + .03
            if start - at > .6:
                return None
        later = [s0 for s0, e0 in lane if s0 > start + .02]
        nxt = min(later) if later else 89.97
        if start + n > nxt - .03:
            avail = nxt - .03 - start
            if avail <= .2 or n / avail > maxf or not wait:
                return None
            piece = cf.stretch(piece, n / avail, RATE)
            n = len(piece) / RATE
        if start + n > 89.97:
            start = max(0.0, 89.97 - n)
        if abs(start - at) > .005:
            shifted.append((at, start))
        s = int(start * RATE)
        track[s:s + len(piece)] += piece[: len(track) - s]
        lane.append((start, start + n))
        if i2 is not None:
            rows.append((start, start + n, i2))
        return start

    # Her call section (A7): every mouth of hers in the shot has a call on it, laid first and pinned: "Squish!" on each, and "Louder, zaako." on the last. The crowd's answers
    # (her shout with her whisper) are not laid in this shot: the mouths run on without a gap for them.
    if film == "idol" and call_units:
        calls = [k for k in call_units if units[k][0] in (10, 12)]
        louder = next(k for k in call_units if spec["lines"][units[k][0]][2].startswith("Louder"))
        answer_text, answer = "", None  # no lone answers any more: every call is a whole line
        for n, (a, b) in enumerate(call_spans):
            last = n == len(call_spans) - 1
            k = louder if last else calls[n % 2]
            i2 = units[k][0]
            piece0 = cut[k][2]
            if False:
                # a mouth long enough for the call and its answer has both, in her own voice: "Squish!" and "Sloppy~!", one after the other, to fill it
                total = (len(piece0) + len(answer)) / RATE + .03
                factor = max(total / max(b - a, total / 2.0), .8)
                first, factor = stretch_to(piece0, len(piece0) / RATE / factor)
                second = cf.stretch(answer, factor, RATE)
                start = put(first, a, i2, pin=True)
                put(second, start + len(first) / RATE + .03, units[call_units[1]][0], pin=True)
                notes.append(f"{start:5.1f}-{start + total / factor:5.1f} s her call and its answer, pinned on her mouth {a:.1f}-{b:.1f}: x{factor:.2f}  {W[lang][i2]} {answer_text}")
                continue
            word = W[lang][i2]
            if False:
                # a short mouth gets the word with the most voice in it (Sloppy~), which is heard where "Squish!" is mostly breath
                piece0, i2, word = answer, units[call_units[1]][0], answer_text
            share = min(max(b - a, len(piece0) / RATE / 2.0), len(piece0) / RATE / .6)
            piece, factor = stretch_to(piece0, share)
            start = put(piece, a, i2, pin=True)
            notes.append(f"{start:5.1f}-{start + share:5.1f} s her call, pinned on her mouth {a:.1f}-{b:.1f}: x{factor:.2f}  {word}")
        dropped = [W[lang][units[k][0]] for k in call_units if spec["lines"][units[k][0]][3] == "crowd"]
        notes.append(f"the crowd's whispered double of the answers ({', '.join(dropped)}) is left out of this shot: her mouths leave no gap for it")
    # the intro stays where it was said
    for k, (a, b, piece) in enumerate(cut):
        if k not in movable and k not in call_units and units[k][0] not in DROPPED:
            put(piece, a, k)
            notes.append(f"{a:5.1f} s intro, left where it was said: nobody's mouth moves ({words(lang, k)})")
    forms = {k: [(units[k][0], cut[k][2])] + [(i2, piece_of(i2)) for i2 in forms_of.get(units[k][0], [])] for k in range(len(units))}
    cap = 1.15  # the most a line may be stretched: it is said at a natural pace
    byphrase = {}
    for u, ph in zip(movable, pick):
        if ph is None:
            a0 = cut[u][0]
            if film == "idol":
                notes.append(f"{a0:5.1f} s no mouth window for it within 4 s: LEFT OUT ({W[lang][units[u][0]]})")
                continue
            put(cut[u][2], a0, units[u][0])
            notes.append(f"{a0:5.1f} s no mouth window within 4 s: left where it was said ({W[lang][units[u][0]]})")
        else:
            byphrase.setdefault(ph, []).append(u)
    free = 0.0
    over_count, shortened = 0, []
    for ph, us in sorted(byphrase.items()):
        a, b = phrases[ph]
        level = {u: 0 for u in us}
        size = lambda u: len(forms[u][level[u]][1]) / RATE
        total = lambda: sum(size(u) for u in us) + .08 * (len(us) - 1)
        hot = film == "idol" and in_h((a + b) / 2)
        if hot:
            for u in [u for u in us if spec["lines"][units[u][0]][3] == "crowd"]:
                notes.append(f"{a:5.1f}-{b:5.1f} s the crowd's answer ({W[lang][units[u][0]]}) is left out of her close-up: her mouth leaves no gap for it")
                us.remove(u)
            if not us:
                continue
        cap = H_CAP if hot else 1.15
        while total() > cap * (b - a):
            candidates = [u for u in us if level[u] + 1 < len(forms[u])]
            if not candidates:
                break
            level[max(candidates, key=size)] += 1
        for u in list(us):
            if not hot and total() > cap * (b - a) and size(u) > b - a and len(us) == 1 and b - a < .78:
                notes.append(f"{a:5.1f}-{b:5.1f} s mouth too short for a word ({W[lang][units[u][0]]}): left silent")
                us.remove(u)
        if not us:
            continue
        stretch_all = max(.85, min(cap, total() / (b - a)))
        at = a
        for u in us:
            i2, piece0 = forms[u][level[u]]
            share = len(piece0) / RATE / stretch_all
            piece, factor = stretch_to(piece0, share)
            at = max(at, free)
            piece = unison(piece, i2, at)
            start = put(piece, at, i2, pin=hot)
            if start is None:
                notes.append(f"{at:5.1f} s no room for it between the lines either side: LEFT OUT ({W[lang][i2]})")
                continue
            over = start + share - b
            bad = over > .12
            over_count += 1 if bad and not hot else 0
            if level[u]:
                shortened.append(f"{W['en'][units[u][0]]} -> {W[lang][i2]}")
            flag = (f" <-- (her close-up: said {over:.1f} s long, so that she is heard)" if hot else f" <-- RUNS {over:.1f} s PAST ITS MOUTH even in its shortest form") if bad else ""
            tag = " [the five in unison]" if film == "idol" and in_g(at) and any(ig.path_of(l, lang, W[lang][i2]).exists() for l, _, _ in UNISON) else ""
            notes.append(f"{start:5.1f}-{start + share:5.1f} s phrase {a:.1f}-{b:.1f}: x{factor:.2f}  form {level[u]}  {W[lang][i2]}{tag}{flag}")
            free = start + share + .08
            at = start + share + .08
    # A mouth that is left with no line is given a line she already has, said again: in a shot where the five sing, the line of that shot, in unison; in her close-up,
    # a call of hers; elsewhere "Squish!" for a short one and "Louder" for a longer.
    short_unit = next(k for k, u in enumerate(units) if u[0] == 10)
    long_unit = next(k for k, u in enumerate(units) if spec["lines"][u[0]][2].startswith("Louder"))
    filled = []

    def fill(a, b, why):
        if film == "idol" and in_g((a + b) / 2):
            u = next(line for (x, y), line in G_FILL if x <= a < y)
        else:
            u = long_unit if (b - a >= (1.2 if film == "idol" and in_h((a + b) / 2) else 1.5)) else short_unit
        # the longest form of the line that fits the mouth, or the shortest
        fits = [f for f in forms[u] if len(f[1]) / RATE <= H_CAP * (b - a)]
        i2, piece0 = fits[0] if fits else forms[u][-1]
        piece, factor = stretch_to(piece0, min(max(b - a, len(piece0) / RATE / H_CAP), len(piece0) / RATE / .8))
        piece = unison(piece, i2, a)
        start = put(piece, a, i2, wait=False, maxf=2.0, pin=(film == "idol" and in_h((a + b) / 2)))
        if start is None:
            notes.append(f"{a:5.1f}-{b:5.1f} s {why}: not filled, another line of hers is being said there")
            return
        filled.append(f"{a:.1f}-{b:.1f}")
        notes.append(f"{start:5.1f}-{b:5.1f} s {why}: filled with a line again, x{factor:.2f}  {W[lang][i2]}" + (" [the five in unison]" if film == "idol" and in_g(a) else ""))

    for j, (a, b) in enumerate(phrases):
        if j in byphrase or b - a < (.3 if film == "idol" and (in_h((a + b) / 2) or in_g((a + b) / 2)) else .78):
            continue
        fill(a, b, "mouth with no line")
    if film == "idol":
        for a, b in merged:
            if b - a < .3 or not (in_h((a + b) / 2) or in_g((a + b) / 2)):
                continue
            covered = sum(max(0, min(b, r1) - max(a, r0)) for r0, r1, _ in rows) / (b - a)
            if covered >= .35:
                continue
            fill(a, b, "mouth with nothing on it")
    bare = [f"{a:.1f}-{b:.1f}" for j, (a, b) in enumerate(phrases) if j not in byphrase and f"{a:.1f}-{b:.1f}" not in filled]
    notes.append("mouths still with no voice (shorter than any word she has): " + (", ".join(bare) if bare else "none"))
    notes.append(f"clips that had to wait for another so as not to cut it: {len(shifted)}" + ("".join(f" ({x:.2f} -> {y:.2f})" for x, y in shifted[:12])))
    notes.append(f"RESULT {shape} {lang}: lines over their mouth: {over_count}")
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



