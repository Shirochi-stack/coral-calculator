"""
Makes the sound effects of Page brawl, from nothing but arithmetic.

    python tools/make_fight_sounds.py

Each sound is a few tones and a little noise, shaped and added up, and saved as
res/raw/fight_s_NAME.ogg. Nothing is recorded or taken from anywhere. Needs numpy and soundfile.
"""

import pathlib

import numpy
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
RATE = 22050
random = numpy.random.default_rng(11)


def clock(seconds):
    return numpy.arange(int(RATE * seconds)) / RATE


def tone(seconds, start, end=None, shape="sine", wobble=0.0, wobble_rate=0.0):
    """A tone that slides from one pitch to another, with a wobble to it if asked."""
    t = clock(seconds)
    pitch = numpy.linspace(start, end if end is not None else start, len(t))
    if wobble:
        pitch = pitch * (1 + wobble * numpy.sin(2 * numpy.pi * wobble_rate * t))
    phase = 2 * numpy.pi * numpy.cumsum(pitch) / RATE
    if shape == "square":
        return numpy.sign(numpy.sin(phase)) * .6
    if shape == "triangle":
        return 2 / numpy.pi * numpy.arcsin(numpy.sin(phase))
    return numpy.sin(phase)


def noise(seconds):
    return random.uniform(-1, 1, int(RATE * seconds))


def fade(sound, attack=.004, release=None):
    """A sound that comes in quickly and dies away."""
    n = len(sound)
    release = release if release is not None else n / RATE - attack
    shape = numpy.ones(n)
    a, r = max(1, int(RATE * attack)), max(1, int(RATE * release))
    shape[:a] = numpy.linspace(0, 1, a)
    shape[n - r:] = numpy.linspace(1, 0, r) ** 1.6
    return sound * shape


def after(*parts):
    return numpy.concatenate(parts)


def mix(*parts):
    longest = max(len(part) for part in parts)
    total = numpy.zeros(longest)
    for part in parts:
        total[:len(part)] += part
    return total


def soft(sound, cutoff=.25):
    """A sound with its sharpest edges taken off."""
    kernel = int(max(1, 1 / cutoff))
    return numpy.convolve(sound, numpy.ones(kernel) / kernel, mode="same")


def note(pitch, seconds, shape="triangle"):
    return fade(mix(tone(seconds, pitch, shape=shape), .35 * tone(seconds, pitch * 2)), .004)


def rise(sound, start=.2):
    """A sound that grows as it goes."""
    return sound * numpy.linspace(start, 1, len(sound))


SOUNDS = {
    # A quick blow landing: a short thud with a snap on it.
    "hit": mix(fade(tone(.11, 260, 90), .002), .5 * fade(soft(noise(.06), .5), .001)),
    # A heavy one: lower, longer, with a crack.
    "heavy": mix(fade(tone(.26, 170, 42), .002), .7 * fade(soft(noise(.13), .3), .001), .3 * fade(tone(.08, 1200, 500, "square"), .001)),
    # A blow taken on the guard: a hard knock.
    "block": mix(fade(tone(.07, 900, 620, "triangle"), .001), .5 * fade(soft(noise(.05), .6), .001)),
    # A swing through the air.
    "swing": fade(rise(soft(noise(.15), .28)) + .2 * tone(.15, 420, 1100), .02, .06),
    # Something thrown.
    "throw": fade(soft(noise(.13), .12) * numpy.linspace(1, .25, int(RATE * .13)) + .3 * tone(.13, 700, 380), .01),
    # Glossarion's scan: a bright sweep upward and a blip.
    "scan": after(fade(mix(tone(.16, 900, 2600), .4 * tone(.16, 1800, 5200)), .005, .05), note(2093, .07)),
    # A chunk cut: a quick sharp slice.
    "slice": fade(mix(tone(.12, 3200, 900), .5 * soft(noise(.12), .7)), .002),
    # The bat meeting something: a crack of wood.
    "bat": mix(fade(tone(.09, 1500, 700, "triangle"), .001), fade(tone(.2, 240, 110), .002), .5 * fade(soft(noise(.05), .8), .001)),
    # Bread: a soft thump.
    "bread": fade(mix(tone(.16, 190, 85), .3 * soft(noise(.1), .1)), .004),
    # An ink tear: a drop falling into water.
    "ink": fade(tone(.17, 520, 1500, "sine", .05, 30), .003),
    # A heart: two small bright notes.
    "heart": after(note(1568, .05), note(2349, .12)),
    # Pink magic: a shimmer.
    "magic": fade(mix(tone(.34, 1175, 1760, "sine", .03, 18), .5 * tone(.34, 2349, 3520, "sine", .03, 23)), .01),
    # A jump.
    "jump": fade(tone(.13, 320, 640, "triangle"), .004),
    # A knock-down: a body meeting the floor.
    "fall": mix(fade(tone(.22, 130, 48), .002), .5 * fade(soft(noise(.14), .12), .002)),
    # The last blow of a round: a boom that rings.
    "ko": mix(fade(tone(.9, 110, 34), .002), .6 * fade(soft(noise(.4), .1), .002), .25 * fade(tone(.9, 880, 220), .01)),
    # The bell that starts a round.
    "bell": fade(mix(tone(.7, 1318), .6 * tone(.7, 1976), .35 * tone(.7, 2637), .2 * tone(.7, 3951)), .002),
    # A meter filling up: three notes rising.
    "meter": after(note(784, .06), note(1175, .06), note(1568, .2)),
    # An ultimate announced: a rush upward into a chord.
    "ultimate": after(fade(rise(mix(tone(.42, 180, 1400, "triangle"), .6 * soft(noise(.42), .2))), .01, .04),
                      fade(mix(tone(.55, 523), tone(.55, 784), tone(.55, 1047), .5 * tone(.55, 2093)), .004)),
    # A button, and a fighter picked.
    "tap": fade(tone(.05, 900, 700, "triangle"), .002),
    "pick": after(note(659, .06), note(988, .14)),
    # A fight won, and a fight lost.
    "win": after(note(523, .1), note(659, .1), note(784, .1), note(1047, .14), note(784, .08), note(1047, .4)),
    "lose": after(note(440, .14, "square"), note(370, .14, "square"), note(311, .14, "square"), note(233, .44, "square")),
    # A harder swing: the last blow of a chain.
    "whoosh": fade(rise(soft(noise(.24), .16)) + .25 * tone(.24, 260, 900), .03, .09),
    # A dodge: a quick breath of air going by, upward.
    "dodge": fade(soft(noise(.14), .4) * numpy.linspace(.3, 1, int(RATE * .14)) + .3 * tone(.14, 900, 2200), .01, .07),
    # A smash coming down: a boom with rubble after it.
    "smash": mix(fade(tone(.5, 150, 30), .002), .8 * fade(soft(noise(.3), .12), .002), .3 * fade(tone(.12, 900, 300, "square"), .001),
                 .3 * after(numpy.zeros(int(RATE * .12)), fade(soft(noise(.3), .5), .01))),
    # Two ultimates meeting: a clang, and a held, rising hum under it.
    "clash": mix(fade(mix(tone(.5, 1244, shape="triangle"), tone(.5, 1661, shape="triangle"), .5 * tone(.5, 2489)), .001),
                 .6 * fade(soft(noise(.1), .7), .001), .5 * fade(rise(tone(.9, 110, 440, "triangle")), .01, .1)),
    # A home run: the crack of the bat, the whistle of what was hit going up and away, and a crowd's far cheer.
    "homer": mix(fade(mix(tone(.07, 2100, 900, "triangle"), soft(noise(.05), .9)), .001),
                 .5 * fade(tone(.25, 300, 120), .002),
                 .35 * after(numpy.zeros(int(RATE * .08)), fade(tone(1.1, 1500, 3200, "sine", .01, 9), .05, .5)),
                 .3 * after(numpy.zeros(int(RATE * .25)), fade(rise(soft(noise(1.3), .09)), .3, .6))),
    # The brick bread drop: something heavy whistling down, a deep thud, crumbs pattering, and a happy note.
    "drop": mix(.4 * fade(tone(.45, 2400, 500, "sine", .01, 7), .05, .1),
                after(numpy.zeros(int(RATE * .42)), mix(fade(tone(.6, 120, 28), .002), .8 * fade(soft(noise(.35), .1), .002))),
                .3 * after(numpy.zeros(int(RATE * .6)), fade(soft(noise(.7), .7) * (random.uniform(0, 1, int(RATE * .7)) > .93), .01)),
                .5 * after(numpy.zeros(int(RATE * 1.0)), after(note(1047, .08), note(1568, .25)))),
    # One tap of a clash: a small bright tick.
    "tick": fade(tone(.035, 1500, 1900, "triangle"), .001),
}


def main():
    RAW.mkdir(parents=True, exist_ok=True)
    for name, sound in SOUNDS.items():
        sound = sound / max(1e-6, numpy.max(numpy.abs(sound))) * .8
        target = RAW / f"fight_s_{name}.ogg"
        soundfile.write(target, sound.astype(numpy.float32), RATE, format="OGG", subtype="VORBIS")
        print(name, f"{len(sound) / RATE:.2f}s", target.stat().st_size, "bytes")


if __name__ == "__main__":
    main()
