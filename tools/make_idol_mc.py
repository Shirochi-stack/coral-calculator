"""
The idol opening's song with Halgakos's own voice and no other: she does not sing it, she is
the unit's MC. Every line is hers, talked and called on the beat; the crowd that answers
her is her own voice too, many times over. Under her is the band alone.

    python tools/make_idol_mc.py [--again]

Each line is recorded with the call the game's lines are recorded with (her voice, asked
how to say it), kept under a name made from its words in dist/cutscenes/idol/mc, and
recorded again only if the words change or --again is given. A line longer than its place
is hurried a little with its pitch kept. The crowd's shouts are a dozen copies of her own
shout, each a little off in time and pitch. The band is the newest band-only song in
dist/cutscenes/idol/songs (made by make_song.py with --bare: the same parts and seconds as
the sung version), held down a little while she speaks. Writes idol/songs/halgakos-mc.wav
(and halgakos-mc-voice.wav, her voice alone). Needs numpy, soundfile, av, requests; the
voice's key, by name.
"""
import hashlib
import importlib.util
import pathlib
import sys

import numpy
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "dist" / "cutscenes" / "idol"
RATE, SECONDS = 48000, 90
SCENE = ("She is the centre of an idol group on stage in front of a cheering hall, hyping the crowd as the group's MC over fast, bouncy idol pop at 120 beats a minute. "
         "She does not sing: she talks and calls in rhythm, right on the beat, punchy and bright, smug and teasing, loud enough for a hall. Quick and rapid-fire, with no pauses between phrases.")
SHOUT = "She is on stage and shouts one word to a cheering hall, as loud and bright and short as she can, like a concert call."
# When (seconds), how long its place is, what she says, and how: hers alone ("solo"), or the crowd's (her own shout, many times over).
LINES = [
    (.5, .9, "Squish!", "crowd"), (1.5, .9, "Sloppy!", "crowd"), (2.5, .9, "Squish!", "crowd"), (3.5, .9, "Sloppy!", "crowd"),
    (4.5, .9, "Squish!", "crowd"), (5.5, .9, "Sloppy!", "crowd"), (6.5, .9, "Squish!", "crowd"), (7.5, .9, "Sloppy!", "crowd"),
    (8, 3.9, "Lights go up, the pages fly, five of us beneath the sky!", "solo"), (12, 3.9, "Open the book, the story's ours: sing it loud and light the stars!", "solo"),
    (16, 2, "Hands up high!", "solo"), (18, 2, "Penlights bright!", "solo"), (20, 2, "Call my name!", "solo"), (22, 2, "And call it right!", "solo"),
    (24.5, .9, "Squish!", "call"), (25.5, .9, "Sloppy!", "crowd"), (26.5, .9, "Squish!", "call"), (27.5, .9, "Sloppy!", "crowd"), (28.5, 1.4, "Louder!", "call"),
    (30, 3.2, "Squish, Sloppy! We're on tonight!", "solo"), (33.3, .7, "Sloppy!", "crowd"), (34, 3.2, "Five colours of light! Get up on your feet!", "solo"), (37.3, .7, "Sloppy!", "crowd"),
    (38, 3.5, "Pink in the centre, smug as can be: me!", "solo"), (41.5, .5, "Sloppy!", "crowd"),
    (42, 3.5, "Red as a ribbon, warm as bread: Chikuwa!", "solo"), (45.5, .5, "Sloppy!", "crowd"),
    (46, 3.5, "Blue as the ink and the tears she's shed: Rune!", "solo"), (49.5, .5, "Sloppy!", "crowd"),
    (50, 3.5, "Gold in the spotlight, born to lead: Illya!", "solo"), (53.5, .5, "Sloppy!", "crowd"),
    (54, 4.3, "Purple and calm, she swings, she speeds: Tsubakieh!", "solo"), (58.5, .9, "Sloppy!", "crowd"),
    (60, 3, "Down the runway, hand in hand!", "solo"), (63, 3, "Every voice in storybook land!", "solo"), (66, 3, "Not bad. For you. You kept the beat.", "solo"), (69, 3, "One more time! Up on your feet!", "solo"),
    (72, 3, "Squish, Sloppy! We're on tonight!", "solo"), (75, 3, "Squish, Sloppy! Five colours of light!", "solo"), (78, 3, "Every heart, every page, every beat!", "solo"), (81, 3, "Squish, Sloppy! Encore! Repeat!", "solo"),
    (84.5, 1.5, "Thank you!", "solo"), (86.3, 3.5, "Now where is my mint ice cream Sloppy~♡", "solo"),
]


def hurried(wave, rate, faster):
    """A sound made shorter with its pitch kept: pieces of it laid over one another closer together, each where it best goes on from the last."""
    if faster <= 1.005:
        return wave
    size, hop, reach = int(rate * .04), int(rate * .01), int(rate * .008)
    window = numpy.hanning(size)
    out = numpy.zeros(int(len(wave) / faster) + size * 2)
    weight = numpy.zeros_like(out)
    at, put, last = 0, 0, None
    while at + size + reach < len(wave) and put + size < len(out):
        best = at
        if last is not None and last + hop + size <= len(wave):
            want = wave[last + hop:last + hop + size]
            best = max(range(max(0, at - reach), min(len(wave) - size, at + reach)), key=lambda start: float(numpy.dot(wave[start:start + size], want)))
        out[put:put + size] += wave[best:best + size] * window
        weight[put:put + size] += window
        last = best
        put += hop
        at = int(round(put * faster))
    return (out[:put + size - hop] / numpy.maximum(weight[:put + size - hop], 1e-3))[: int(len(wave) / faster)]


def main():
    spec = importlib.util.spec_from_file_location("voice", str(ROOT / "tools" / "make_run_voice.py"))
    voice = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(voice)
    key = voice.secret("INWORLD_API_KEY")
    kept = OUT / "mc"
    kept.mkdir(parents=True, exist_ok=True)

    def said(words, scene, take=0):
        """Her voice saying some words, at this file's rate: from what is kept, or recorded now."""
        path = kept / (hashlib.sha1(f"{words}|{scene}|{take}".encode("utf-8")).hexdigest()[:10] + ".wav")
        if not path.exists() or "--again" in sys.argv:
            sound, _ = voice.speak(key, words, "H", scene)
            soundfile.write(path, voice.finished(sound, voice.VOICES["H"][2]), voice.RATE_OUT)
        wave, rate = soundfile.read(path)
        wave = wave.mean(axis=1) if wave.ndim > 1 else wave
        return numpy.interp(numpy.arange(int(len(wave) * RATE / rate)) * rate / RATE, numpy.arange(len(wave)), wave)

    def trimmed(wave):
        """Without the quiet before and after it."""
        loud = numpy.where(numpy.abs(wave) > .02 * numpy.abs(wave).max())[0]
        return wave[max(0, loud[0] - 480):loud[-1] + 2400] if len(loud) else wave

    def crowd(word):
        """A hall shouting a word: her own shout, four takes of it, a dozen times over, each a little off in time and in pitch, spread from left to right."""
        random = numpy.random.default_rng(len(word) * 7)
        takes = [trimmed(said(word, SHOUT, take)) for take in range(4)]
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

    track = numpy.zeros((SECONDS * RATE, 2))
    shouts = {}
    for at, room, words, how in LINES:
        begins = int(at * RATE)
        if how == "crowd":
            if words not in shouts:
                shouts[words] = crowd(words)
            piece = shouts[words] * .55
            track[begins:begins + len(piece)] += piece[: len(track) - begins]
            continue
        wave = trimmed(said(words, SHOUT if how == "call" else SCENE))
        came = len(wave) / RATE
        faster = min(1.3, max(1.0, came / room))
        wave = hurried(wave, RATE, faster)
        if len(wave) > int((room + .3) * RATE):
            print("        (still too long for its place: its end is faded away)")
        wave = wave[: int((room + .3) * RATE)]
        wave[-960:] *= numpy.linspace(1, 0, 960)
        wave = wave / (numpy.abs(wave).max() + 1e-9) * .85
        track[begins:begins + len(wave)] += numpy.stack([wave, wave], axis=1)[: len(track) - begins]
        print(f"{at:5.1f} s  {came:4.1f} s -> {len(wave) / RATE:4.1f} s  x{faster:.2f}  {words}".encode("ascii", "replace").decode(), flush=True)
    soundfile.write(OUT / "songs" / "halgakos-mc-voice.wav", numpy.clip(track, -1, 1), RATE)
    bands = sorted((OUT / "songs").glob("*-band-*.mp3"), key=lambda path: path.stat().st_mtime)
    if not bands:
        print("no band-only song in", OUT / "songs", ": her voice alone was written")
        return
    import av

    box = av.open(str(bands[-1]))
    resampler = av.AudioResampler(format="flt", layout="stereo", rate=RATE)
    band = numpy.concatenate([made.to_ndarray().reshape(-1, 2) for frame in box.decode(audio=0) for made in resampler.resample(frame)])[: len(track)]
    under = numpy.zeros_like(track)
    under[: len(band)] = band
    under *= .5 / (numpy.sqrt((under ** 2).mean()) * 4 + 1e-9) if numpy.sqrt((under ** 2).mean()) > .125 else 1
    # The band is held down a little wherever she or the crowd is heard.
    heard = numpy.convolve((numpy.abs(track).max(axis=1) > .03).astype(float), numpy.ones(RATE // 5) / (RATE // 5), mode="same")
    mix = track * .9 + under * (1 - .35 * numpy.clip(heard * 1.5, 0, 1))[:, None]
    mix[-RATE // 2:] *= numpy.linspace(1, 0, RATE // 2)[:, None]
    mix = mix / max(1, numpy.abs(mix).max() / .97)
    soundfile.write(OUT / "songs" / "halgakos-mc.wav", mix, RATE)
    print("saved", OUT / "songs" / "halgakos-mc.wav", "over", bands[-1].name)


if __name__ == "__main__":
    main()
