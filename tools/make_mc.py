"""
A film's track in Halgakos's own voice and no other: she does not sing, she talks and calls on
the beat over a band; a crowd that answers her is her own shout, many times over.

    python tools/make_mc.py SPEC [--again]

SPEC is a file in tools/cutscenes (idol-mc, prologue-mc): where the track goes, the band under
her, how she is asked to speak, and her lines, each with the second it begins on, the seconds it
has, its words exactly as written, and how it is said: "solo" (talked), "call" (one shouted
word or two), or "crowd" (her shout a dozen times over). (make_idol_mc.py is the first of these,
kept as it made the track of the first idol words.)

Each line is recorded with the call the game's lines are recorded with (her voice, asked how to
say it), kept under a name made from what was sent in the spec's "kept" folder, and recorded
again only if that changes or --again is given. What is sent is the line without its heart, and
with the spec's "says" in place of words the voice would misread from their spelling (the words
shown stay as written); a line may also carry, as a fifth thing, what is sent in its place when
only its marks differ (a "~" that would draw a word out where there is no room for it). A long
dash in a line is a pause: the line is recorded in the pieces the dash divides it into, and they
are laid a beat apart where the line's seconds allow, closer where they do not.

A line longer than its seconds is, in this order: said again by the voice itself at a brisker
pace (asked to be brisk, and at a higher speaking rate: the slowest of a few that fits is the
one used); its pauses shortened (to a seventh of a second); and only then hurried afterwards
with its pitch kept, by no more than a tenth. If it is still too long the next line waits for
it, and that is said. The band is held down a little while she is heard.
Writes OUT.wav and OUT-voice.wav (her voice alone). Needs numpy, soundfile, av, requests; the
voice's key, by name.
"""
import hashlib
import importlib.util
import json
import pathlib
import sys

import numpy
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
SHOTS = ROOT / "dist" / "cutscenes"
RATE = 48000
MOST_FASTER = 1.1
# The speaking rates the voice is asked for, slowest first, when a line does not fit as it is first said.
RATES = (1.12, 1.25, 1.4)


def module(name):
    spec = importlib.util.spec_from_file_location(name, str(ROOT / "tools" / f"{name}.py"))
    made = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(made)
    return made


def trimmed(wave):
    """Without the quiet before and after it."""
    loud = numpy.where(numpy.abs(wave) > .02 * numpy.abs(wave).max())[0]
    return wave[max(0, loud[0] - 480):loud[-1] + 2400] if len(loud) else wave


def tightened(wave, most=.14):
    """With no pause inside it longer than so many seconds: the middle of a longer one is taken out."""
    size = RATE // 100
    level = numpy.sqrt(numpy.convolve(wave ** 2, numpy.ones(size) / size, mode="same"))
    quiet = level < .03 * level.max()
    out, at, index = [], 0, 0
    while index < len(wave):
        if not quiet[index]:
            index += 1
            continue
        ends = index
        while ends < len(wave) and quiet[ends]:
            ends += 1
        if (ends - index) / RATE > most and index > 0 and ends < len(wave):
            keep = int(most * RATE / 2)
            out.append(wave[at:index + keep])
            at = ends - keep
        index = ends
    out.append(wave[at:])
    return numpy.concatenate(out)


def main():
    name = next((word for word in sys.argv[1:] if not word.startswith("--")), None)
    if not name:
        raise SystemExit(__doc__)
    spec = json.loads((ROOT / "tools" / "cutscenes" / f"{name}.json").read_text(encoding="utf-8"))
    voice, first = module("make_run_voice"), module("make_idol_mc")
    key = voice.secret("INWORLD_API_KEY")
    kept = SHOTS / spec["kept"]
    kept.mkdir(parents=True, exist_ok=True)
    out = SHOTS / spec["out"]
    out.parent.mkdir(parents=True, exist_ok=True)
    seconds = spec["seconds"]

    def said(words, scene, take=0, pace=1.0):
        """Her voice saying some words, at this file's rate: from what is kept, or recorded now. Pace: the speaking rate the voice is asked for, against her own."""
        sent = words
        for shown, read in spec.get("says", {}).items():
            sent = sent.replace(shown, read)
        path = kept / (hashlib.sha1((f"{sent}|{scene}|{take}" + (f"|{pace}" if pace != 1.0 else "")).encode("utf-8")).hexdigest()[:10] + ".wav")
        if not path.exists() or "--again" in sys.argv:
            who, hers = voice.VOICES["H"][0], voice.VOICES["H"]
            voice.VOICES["H"] = (who, hers[1] * pace, hers[2])
            try:
                sound, _ = voice.speak(key, sent, "H", scene)
            finally:
                voice.VOICES["H"] = hers
            soundfile.write(path, voice.finished(sound, hers[2]), voice.RATE_OUT)
        wave, rate = soundfile.read(path)
        wave = wave.mean(axis=1) if wave.ndim > 1 else wave
        return numpy.interp(numpy.arange(int(len(wave) * RATE / rate)) * rate / RATE, numpy.arange(len(wave)), wave)

    def crowd(word):
        """A hall shouting a word: her own shout, four takes of it, a dozen times over, each a little off in time and in pitch, spread from left to right."""
        random = numpy.random.default_rng(len(word) * 7)
        takes = [trimmed(said(word, spec["shout"], take)) for take in range(4)]
        both = numpy.zeros((int(1.4 * RATE), 2))
        for copy in range(12):
            take = takes[copy % 4]
            higher = random.uniform(.93, 1.08)
            bent = numpy.interp(numpy.arange(int(len(take) / higher)) * higher, numpy.arange(len(take)), take)
            at = int(random.uniform(0, .07) * RATE)
            pan = random.uniform(.15, .85)
            room = min(len(bent), len(both) - at)
            both[at:at + room, 0] += bent[:room] * (1 - pan) * random.uniform(.5, 1)
            both[at:at + room, 1] += bent[:room] * pan * random.uniform(.5, 1)
        return both / (numpy.abs(both).max() + 1e-9)

    def fitted(piece, how, room):
        """A piece said so as to fit its seconds if the voice can: (its sound, what was done to it)."""
        ways = [(spec["shout"] if how == "call" else spec["scene"], 1.0)] + [(spec["shout"] if how == "call" else spec["brisk"], pace) for pace in RATES]
        best = None
        for scene, pace in ways:
            wave = trimmed(said(piece, scene, 0, pace))
            note = "" if pace == 1.0 else f"said at {pace}"
            if len(wave) / RATE <= room + .08:
                return wave, note
            tight = tightened(wave)
            if len(tight) / RATE <= room + .08:
                return tight, (note + ", " if note else "") + "pauses shortened"
            if best is None or len(tight) < len(best[0]):
                best = (tight, (note + ", " if note else "") + "pauses shortened")
        return best

    track = numpy.zeros((seconds * RATE, 2))
    shouts, free, sped, late, paced = {}, 0.0, [], [], []
    for line in spec["lines"]:
        at, room, words, how = line[:4]
        sent = line[4] if len(line) > 4 else words
        if how == "crowd":
            if words not in shouts:
                shouts[words] = crowd(sent)
            piece = shouts[words] * .55
            begins = int(at * RATE)
            track[begins:begins + len(piece)] += piece[: len(track) - begins]
            continue
        pieces = [piece.strip() for piece in sent.split("\u2014") if piece.strip()]
        # How long each piece is when she says it her own way; its share of the line's seconds goes by that.
        own = [len(trimmed(said(piece, spec["shout"] if how == "call" else spec["scene"]))) / RATE for piece in pieces]
        came = sum(own)
        share = [(room - .08 * (len(pieces) - 1)) * length / came for length in own]
        made = [fitted(piece, how, share[number]) for number, piece in enumerate(pieces)]
        waves, steps = [wave for wave, _ in made], [note for _, note in made if note]
        if any("said at" in note for note in steps):
            paced.append((at, words, max(float(note.split("said at ")[1].split(",")[0]) for note in steps if "said at" in note)))
        length = sum(len(wave) for wave in waves) / RATE
        faster = min(MOST_FASTER, max(1.0, length / (room - .08 * (len(pieces) - 1) + .08)))
        if faster > 1.005:
            waves = [first.hurried(wave, RATE, faster) for wave in waves]
            steps.append(f"hurried x{faster:.2f}")
            sped.append((at, words, faster))
        length = sum(len(wave) for wave in waves) / RATE
        # Between the pieces a dash divides a line into: a beat, or what the line's seconds leave, never less than a twelfth of a second.
        gap = min(spec.get("beat", .5), max(.08, (room - length) / max(1, len(waves) - 1))) if len(waves) > 1 else 0
        begins = max(at, free)
        if begins > at + .05:
            late.append((at, words, begins - at))
        now = begins
        for wave in waves:
            wave = wave.copy()
            wave[-960:] *= numpy.linspace(1, 0, 960)
            wave = wave / (numpy.abs(wave).max() + 1e-9) * .85
            start = int(now * RATE)
            track[start:start + len(wave)] += numpy.stack([wave, wave], axis=1)[: len(track) - start]
            now += len(wave) / RATE + gap
        ends = now - gap
        # (A recording keeps a twentieth of a second after its last sound: the next line may begin on that.)
        free = ends - .05
        over = ends - (at + room)
        print((f"{at:5.1f} s  place {room:3.1f}  her own way {came:4.1f} -> {ends - begins:4.1f} s" + (f" [{'; '.join(steps)}]" if steps else "") + (f" (a pause of {gap:.2f} s at its dash)" if len(waves) > 1 else "")
               + (f" BEGINS {begins - at:.2f} s LATE" if begins > at + .05 else "") + (f" runs {over:.2f} s past its place" if over > .1 else "") + f"  {words}").encode("ascii", "replace").decode(), flush=True)
    soundfile.write(str(out) + "-voice.wav", numpy.clip(track, -1, 1), RATE)
    print("\nlines the voice said at a brisker pace:", "none" if not paced else "; ".join(f"{at:.0f} s at {pace}" for at, _, pace in paced))
    print("lines hurried afterwards:", "none" if not sped else "; ".join(f"{at:.0f} s x{faster:.2f}" for at, _, faster in sped))
    print("lines that begin late:", "none" if not late else "; ".join(f"{at:.0f} s by {by:.2f} s" for at, _, by in late))
    band = SHOTS / spec["band"] if spec.get("band") else None
    if band is None or not band.exists():
        print("no band at", band, ": her voice alone was written")
        return
    import av

    box = av.open(str(band))
    resampler = av.AudioResampler(format="flt", layout="stereo", rate=RATE)
    sound = numpy.concatenate([made.to_ndarray().reshape(-1, 2) for frame in box.decode(audio=0) for made in resampler.resample(frame)])[: len(track)]
    under = numpy.zeros_like(track)
    under[: len(sound)] = sound
    under *= .5 / (numpy.sqrt((under ** 2).mean()) * 4 + 1e-9) if numpy.sqrt((under ** 2).mean()) > .125 else 1
    # The band is held down a little wherever she or the crowd is heard.
    heard = numpy.convolve((numpy.abs(track).max(axis=1) > .03).astype(float), numpy.ones(RATE // 5) / (RATE // 5), mode="same")
    mix = track * .9 + under * (1 - .35 * numpy.clip(heard * 1.5, 0, 1))[:, None]
    mix[-RATE // 2:] *= numpy.linspace(1, 0, RATE // 2)[:, None]
    mix = mix / max(1, numpy.abs(mix).max() / .97)
    soundfile.write(str(out) + ".wav", mix, RATE)
    print("saved", str(out) + ".wav", "over", band.name)


if __name__ == "__main__":
    main()
