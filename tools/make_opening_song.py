"""
Makes the opening's song: "Calculation, Translation, and Sloppy", ninety seconds exactly.

    python tools/make_opening_song.py [--again]

It is forty-five bars of four at a hundred and twenty beats a minute, so a bar is two
seconds: shots of four seconds and of six can be cut on the bars. (At a hundred and sixty,
which was tried first, a line of hers did not fit the two bars it had: she sings slowly.) The backing is made here
from nothing, as the game's sounds are: drums, a bass, chords on the off beats and an
arpeggio, over the same four chords throughout. Halgakos sings it, a line at a time, in
her own voice through the call the game's lines are recorded with, asked to sing; what
comes back is laid on the bar its line begins on, and hurried a little if it is longer
than its bars; each is kept under a name made from its words, and sung again only if they
change.

What this makes is a stand-in, not the opening's song: a speaking voice asked to sing tries
too hard and sounds worse than it should, and the song is to be made again with a model
that sings. What stays is here: the words (LINES), and the bars they lie on, which the
storyboard is cut to (tools/cutscenes/opening.json) and which song.json carries to the
film's edit.

Writes, in dist/cutscenes/opening: opening-song.wav, the two it is mixed from
(opening-backing.wav, opening-vocal.wav), every line as it came back (vocal/), and
song.json: the bars, the sections, and where each line lies. Lines already there are kept
unless --again is given. Needs numpy, soundfile, requests; the voice's key, by name.
"""
import hashlib
import importlib.util
import json
import pathlib
import sys

import numpy
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "dist" / "cutscenes" / "opening"
RATE, BPM, BARS = 48000, 120, 45
BEAT = 60 / BPM
BAR = BEAT * 4
SECTIONS = [("Intro", 0, 2), ("Verse", 2, 8), ("Lift", 10, 4), ("Chorus", 14, 8), ("Roll call", 22, 8), ("Last chorus", 30, 12), ("Ending", 42, 3)]
# Each line: the bar it begins on, how many bars it has, what is shown, and what is sent to be sung (notes about it, vowels drawn out).
LINES = [
    (1, 1, "Sloppy~\u2661", "\u266a Slooppyyy~\u2661 \u266a"),
    (2, 2, "My one day off, one ice cream cone,", "\u266a My ooone day off, one iiice cream cooone~ \u266a"),
    (4, 2, "a book fell down and now I'm thrown!", "\u266a a boook fell dooown and nooow I'm throoown~! \u266a"),
    (6, 2, "The sums grew legs, the words ran wild,", "\u266a The suuums grew legs, the wooords ran wiiild~ \u266a"),
    (8, 2, "who cleans it up? Me, Sloppy~\u2661", "\u266a who cleeeans it up? Meee, Slooppyyy~\u2661 \u266a"),
    (10, 2, "Carry the one, mind the footnote,", "\u266a Caaarry the ooone, miiind the foootnote~ \u266a"),
    (12, 2, "stretch your thumbs, here we go Sloppy~\u2661", "\u266a streeetch your thumbs, heeere we gooo, Slooppyyy~\u2661 \u266a"),
    (14, 2, "Calculation! Translation! And Sloppy~\u2661", "\u266a Calculaaation! Translaaation! And Slooppyyy~\u2661 \u266a"),
    (16, 2, "Two plus two is four. You're still Sloppy~\u2661", "\u266a Twooo plus two is fooour. You're stiiill Slooppyyy~\u2661 \u266a"),
    (18, 2, "Every word as written, every sum just right,", "\u266a Eeevery word as wriiitten, eeevery sum just riiight~ \u266a"),
    (20, 2, "Squish Sloppy~\u2661 Flat as a page tonight!", "\u266a Squiiish, Slooppyyy~\u2661 Flaaat as a page toniiight~! \u266a"),
    (22, 2, "Chikuwa! The answer is not always bread!", "\u266a Chikuwaaa! The aaanswer is not aaalways breeead~! \u266a"),
    (24, 2, "Rune! Stop crying in the ink!", "\u266a Ruuune! Stooop crying in the iiink~! \u266a"),
    (26, 2, "Illya! One heroine is plenty!", "\u266a Illyaaa! Ooone heroine is pleeenty~! \u266a"),
    (28, 2, "Tsubakieh! Put the bat down. Normally!", "\u266a Tsubakieeeh! Puuut the bat dooown. Nooormally~! \u266a"),
    (30, 2, "Calculation! Translation! And Sloppy~\u2661", "\u266a Calculaaation! Translaaation! And Slooppyyy~\u2661 \u266a"),
    (32, 2, "-nim and -ssi, I keep them all,", "\u266a Niiim and ssiii, I keeep them aaall~ \u266a"),
    (34, 2, "drop one honorific and you're Sloppy~\u2661", "\u266a drooop one honoriiific and yooou're Slooppyyy~\u2661 \u266a"),
    (36, 2, "not bad. For you. Sloppy~\u2661", "\u266a nooot baaad. For yooou. Slooppyyy~\u2661 \u266a"),
    (38, 2, "Calculation! Translation! And Sloppy~\u2661", "\u266a Calculaaation! Translaaation! And Slooppyyy~\u2661 \u266a"),
    (40, 2, "Sloppy, sloppy, sloppy, Sloppy~\u2661", "\u266a Slooppy, slooppy, slooppy, Slooppyyy~\u2661 \u266a"),
    (42, 2, "Now where is my ice cream Sloppy~\u2661", "\u266a Nooow wheeere is my iiice creeeam, Slooppyyy~\u2661 \u266a"),
    (44, 1, "Sloppy~\u2661", "Sloppy~\u2661"),
]
SCENE = ("She is singing the opening theme song of her own anime, a silly comedy song: a bright, bouncy, upbeat J-pop melody at a brisk tempo. She really sings it, "
         "on pitch, like an idol singing a theme song, a little off-key and very pleased with herself; she does not speak it. Cheerful, energetic, smug, quick.")
TAG = "[singing a bright, upbeat J-pop melody, briskly] "
# The four chords, a bar each, round and round: F, G, E minor, A minor; and C to end on. As notes above the C below middle C, in semitones.
CHORDS = [(5, 9, 12), (7, 11, 14), (4, 7, 11), (9, 12, 16)]
HOME = (0, 4, 7)


def pitch(semitones, octave=0):
    return 130.81 * 2 ** (semitones / 12 + octave)


def tone(seconds, hertz, shape="square", decay=6.0, attack=.004):
    count = int(seconds * RATE)
    time = numpy.arange(count) / RATE
    phase = 2 * numpy.pi * hertz * time
    wave = numpy.sign(numpy.sin(phase)) * .55 if shape == "square" else 2 * numpy.abs(2 * (time * hertz % 1) - 1) - 1 if shape == "triangle" \
        else 2 * (time * hertz % 1) - 1 if shape == "saw" else numpy.sin(phase)
    return wave * numpy.exp(-time * decay) * numpy.minimum(1, time / attack)


def put(track, wave, at, loud=1.0):
    start = int(at * RATE)
    if start >= len(track):
        return
    wave = wave[: len(track) - start]
    track[start:start + len(wave)] += wave * loud


def backing():
    track = numpy.zeros(int(BARS * BAR * RATE) + RATE)
    random = numpy.random.default_rng(7)
    hiss = random.standard_normal(RATE)
    kick = numpy.sin(2 * numpy.pi * numpy.cumsum(40 + 110 * numpy.exp(-numpy.arange(int(.18 * RATE)) / RATE * 28)) / RATE) * numpy.exp(-numpy.arange(int(.18 * RATE)) / RATE * 16)
    snare = (hiss[: int(.16 * RATE)] * .7 + numpy.sin(2 * numpy.pi * 190 * numpy.arange(int(.16 * RATE)) / RATE) * .4) * numpy.exp(-numpy.arange(int(.16 * RATE)) / RATE * 22)
    hat = numpy.diff(hiss[: int(.05 * RATE) + 1]) * numpy.exp(-numpy.arange(int(.05 * RATE)) / RATE * 90)
    crash = numpy.diff(hiss[: int(.9 * RATE) + 1]) * numpy.exp(-numpy.arange(int(.9 * RATE)) / RATE * 4.5)
    section = {bar: name for name, first, many in SECTIONS for bar in range(first, first + many)}
    ends = {first + many - 1 for _, first, many in SECTIONS}
    for bar in range(BARS):
        at, name = bar * BAR, section[bar]
        last = bar == BARS - 1
        chord = HOME if last else CHORDS[bar % 4]
        quiet = name == "Intro" and bar < 1
        halved = name == "Roll call"
        loud = 1.15 if "horus" in name else 1.0
        if bar in {first for _, first, _ in SECTIONS} and bar > 0:
            put(track, crash, at, .22)
        if last:
            # The last hit: everything at once, and then nothing.
            if last:
                put(track, kick, at, .9)
                put(track, crash, at, .4)
                for degree in HOME:
                    put(track, tone(1.4, pitch(degree, 1), "saw", 3), at, .12)
                    put(track, tone(1.4, pitch(degree, 2), "square", 3), at, .06)
                put(track, tone(1.4, pitch(0, -1), "triangle", 2.5), at, .5)
            continue
        for beat in range(4):
            when = at + beat * BEAT
            if not quiet:
                # Drums: the kick on every beat, the snare on two and four; in the roll call, half as often.
                if not halved or beat == 0:
                    put(track, kick, when, .8)
                if (beat in (1, 3) and not halved) or (halved and beat == 2):
                    put(track, snare, when, .5)
                # The bass: the chord's root, twice a beat.
                for half in range(2):
                    put(track, tone(BEAT / 2 * .9, pitch(chord[0], -1), "triangle", 5), when + half * BEAT / 2, .42 * loud)
                # The chords, short, on the off beats.
                for degree in chord:
                    put(track, tone(BEAT * .3, pitch(degree, 1), "saw", 14), when + BEAT / 2, .075 * loud)
            for half in range(2):
                put(track, hat, when + half * BEAT / 2, .16 if half else .24)
            # The arpeggio: the chord's notes going up, four to a beat; out in front where no one sings.
            for step in range(4):
                degree = chord[(beat * 4 + step) % 3] + (12 if (beat * 4 + step) % 6 >= 3 else 0)
                put(track, tone(BEAT / 4 * .9, pitch(degree, 2), "square", 18), when + step * BEAT / 4, (.11 if name in ("Intro", "Ending") else .045) * loud)
        if bar in ends and not quiet:
            # A fill at the end of a part: the snare, four to a beat, through the last two beats.
            for step in range(8):
                put(track, snare, at + 2 * BEAT + step * BEAT / 4, .25 + .04 * step)
    track = track[: int(BARS * BAR * RATE)]
    return track / numpy.abs(track).max() * .85


def vocal(again):
    spec = importlib.util.spec_from_file_location("voice", str(ROOT / "tools" / "make_run_voice.py"))
    voice = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(voice)
    key = voice.secret("INWORLD_API_KEY")
    (OUT / "vocal").mkdir(parents=True, exist_ok=True)
    track = numpy.zeros(int(BARS * BAR * RATE))
    placed = []
    for index, (bar, bars, shown, sung) in enumerate(LINES):
        path = OUT / "vocal" / (hashlib.sha1(sung.encode("utf-8")).hexdigest()[:10] + ".wav")
        if again or not path.exists():
            for attempt in range(5):
                try:
                    sound, way = voice.speak(key, (TAG if "♪" in sung else "") + sung, "H", SCENE)
                    break
                except Exception as error:  # noqa: BLE001
                    sound = None
                    print(path.name, "try", attempt + 1, "FAILED", type(error).__name__, flush=True)
            if sound is None:
                raise SystemExit(f"no voice for line {index + 1}")
            soundfile.write(path, voice.finished(sound, voice.VOICES["H"][2]), voice.RATE_OUT)
        wave, rate = soundfile.read(path)
        wave = numpy.interp(numpy.arange(int(len(wave) * RATE / rate)) * rate / RATE, numpy.arange(len(wave)), wave)
        came, room = len(wave) / RATE, bars * BAR + .2
        # Longer than its bars: it is hurried, by no more than a quarter (which also raises it a little); and what is still over is faded away.
        faster = min(1.25, max(1.0, came / room))
        if faster > 1:
            wave = numpy.interp(numpy.arange(int(len(wave) / faster)) * faster, numpy.arange(len(wave)), wave)
        if len(wave) / RATE > room + .25:
            wave = wave[: int((room + .25) * RATE)]
            wave[-RATE // 5:] *= numpy.linspace(1, 0, RATE // 5)
        put(track, wave, bar * BAR, .95)
        placed.append({"line": index + 1, "bar": bar, "bars": bars, "at": round(bar * BAR, 2), "shown": shown, "came back": round(came, 2), "hurried by": round(faster, 2),
                       "lasts": round(len(wave) / RATE, 2), "file": path.name, "cut short": bool(came / faster > room + .25)})
        print(f"{index + 1:02d} bar {bar:02d}  {came:4.1f} s -> {len(wave) / RATE:4.1f} s  x{faster:.2f}  {shown}".encode("ascii", "replace").decode(), flush=True)
    return track, placed


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    band = backing()
    sung, placed = vocal("--again" in sys.argv)
    # Under her the band stands back a little.
    level = numpy.convolve((numpy.abs(sung) > .02).astype(float), numpy.ones(RATE // 5) / (RATE // 5), "same")
    mix = sung + band * (1 - .2 * numpy.clip(level * 2, 0, 1))
    mix = mix / max(1, numpy.abs(mix).max() / .97)
    soundfile.write(OUT / "opening-backing.wav", band, RATE)
    soundfile.write(OUT / "opening-vocal.wav", numpy.clip(sung, -1, 1), RATE)
    soundfile.write(OUT / "opening-song.wav", mix, RATE)
    (OUT / "song.json").write_text(json.dumps({"seconds": BARS * BAR, "beats a minute": BPM, "bar": BAR, "bars": BARS,
                                               "sections": [{"name": name, "bar": first, "bars": many, "at": first * BAR, "lasts": many * BAR} for name, first, many in SECTIONS],
                                               "lines": placed}, indent=1, ensure_ascii=False), encoding="utf-8")
    print("song", len(mix) / RATE, "s", OUT / "opening-song.wav")


if __name__ == "__main__":
    main()
