"""
Makes the sound effects of Plus run, from nothing but arithmetic.

    python tools/make_run_sounds.py

Each sound is a few tones and a little noise, shaped and added up, and saved as
res/raw/run_s_NAME.ogg. Nothing is recorded or taken from anywhere. Needs numpy and soundfile.
"""

import pathlib

import numpy
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
RATE = 22050
random = numpy.random.default_rng(7)


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


SOUNDS = {
    # Her jump: a quick rise.
    "jump": fade(tone(.16, 330, 720, "triangle"), .004),
    # A plus: two bright notes.
    "plus": after(note(1318, .05), note(1760, .12)),
    # Landing on a sign: a thump and a squeak.
    "squish": mix(fade(tone(.14, 220, 70), .002), .5 * fade(tone(.1, 900, 400, "square"), .002)),
    # A hurt: a buzz that falls.
    "hurt": fade(mix(tone(.32, 380, 110, "square"), .4 * soft(noise(.32))), .004),
    # A spring: a boing.
    "spring": fade(tone(.4, 200, 880, "sine", .08, 22), .004),
    # A flag: three notes going up.
    "flag": after(note(660, .08), note(880, .08), note(1320, .2)),
    # The gate: a little fanfare.
    "win": after(note(523, .1), note(659, .1), note(784, .1), note(1047, .12), note(784, .08), note(1047, .34)),
    # Out of hearts: four notes going down.
    "lose": after(note(440, .14, "square"), note(370, .14, "square"), note(311, .14, "square"), note(233, .4, "square")),
    # A star on the card at the end.
    "star": mix(note(1568, .3), .5 * note(2093, .3)),
    # A button.
    "tap": fade(tone(.05, 900, 700, "triangle"), .002),
    # Zero taking a landing: something heavy.
    "blow": mix(fade(tone(.34, 140, 45), .002), .6 * fade(soft(noise(.2), .08), .002)),
    # Zero going down for good.
    "fall": after(fade(tone(.5, 300, 50, "square"), .004), fade(soft(noise(.3), .06), .002)),
    # Something thrown: a short rush of air.
    "throw": fade(soft(noise(.13), .1) * numpy.linspace(1, .25, int(RATE * .13)) + .3 * tone(.13, 700, 380), .01),
    # A swing of the brush or the sword: a longer, sharper one.
    "slash": fade(soft(noise(.17), .3) * numpy.linspace(.3, 1, int(RATE * .17)) + .25 * tone(.17, 500, 1500), .02, .06),
    # The ink gun: a quick falling squirt.
    "shot": fade(mix(tone(.09, 1500, 420, "square"), .5 * soft(noise(.09), .2)), .002),
    # Picking something up: three quick notes.
    "pick": after(note(784, .05), note(988, .05), note(1319, .14)),
    # Something of theirs sent back or wiped away: a ring of metal.
    "clang": fade(mix(tone(.26, 2100), .6 * tone(.26, 3150), .4 * tone(.26, 1270, shape="triangle")), .002),
    # A page of the story turning.
    "page": fade(soft(noise(.12), .12) * numpy.linspace(.2, 1, int(RATE * .12)), .01),
}


def main():
    RAW.mkdir(parents=True, exist_ok=True)
    for name, sound in SOUNDS.items():
        sound = sound / max(1e-6, numpy.max(numpy.abs(sound))) * .8
        target = RAW / f"run_s_{name}.ogg"
        soundfile.write(target, sound.astype(numpy.float32), RATE, format="OGG", subtype="VORBIS")
        print(name, f"{len(sound) / RATE:.2f}s", target.stat().st_size, "bytes")


if __name__ == "__main__":
    main()
