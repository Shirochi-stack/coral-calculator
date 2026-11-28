"""
Makes the sounds of the six ultimates in her skill tree, from nothing but arithmetic, the way make_run_sounds.py makes the rest.

    python tools/make_ult_sounds.py

Each ultimate has a sound for its beginning and a sound for its end, saved as res/raw/run_s_ult_NAME.ogg and run_s_ult_NAME_end.ogg:
berserk (a growl that climbs, and a sag), the stilled hour (a clock winding down and then a rewind and a crack), the tide of ink (a wave
of noise over a swell, and a splash), the giant (a boom under a horn, and a landing), the legion (three bells, each after the other, and
the same going away), and the rose bloom (a rising run of bright notes, and its petals falling). Nothing is recorded or taken from anywhere.
"""

import numpy
import soundfile

from make_run_sounds import RAW, RATE, after, clock, fade, mix, noise, note, soft, tone


def swell(sound, peak=.5):
    """A sound that grows to its peak, a part of the way through, and fades after it."""
    n = len(sound)
    up = int(n * peak)
    shape = numpy.concatenate([numpy.linspace(0, 1, up), numpy.linspace(1, 0, n - up) ** 1.4])
    return sound * shape


def echoes(pitch, gaps, seconds=.5, shape="triangle"):
    """The same bell struck again and again, each a little softer and further off."""
    parts = []
    for index, gap in enumerate(gaps):
        parts.append(numpy.concatenate([numpy.zeros(int(RATE * gap)), fade(mix(tone(seconds, pitch, shape=shape), .3 * tone(seconds, pitch * 2.01)), .004, seconds) * (.9 ** index)]))
    return mix(*parts)


SOUNDS = {
    # Berserk begins: a low growl that climbs, a roar of noise, and a hard bright sweep over it.
    "ult_berserk": mix(
        swell(tone(1.0, 70, 190, "square", .06, 38), .7) * .8,
        swell(soft(noise(1.0), .1), .35) * .6,
        after(numpy.zeros(int(RATE * .18)), fade(tone(.5, 380, 1100, "triangle"), .01)) * .5),
    # Berserk ends: she sags, the growl dying down into breath.
    "ult_berserk_end": mix(fade(tone(.9, 320, 70, "triangle"), .01), .5 * fade(soft(noise(.9), .08) * numpy.linspace(1, 0, int(RATE * .9)), .02)),
    # The stilled hour begins: the world winding down, then two ticks of a clock in the quiet.
    "ult_time": after(
        mix(fade(tone(.7, 900, 45, "sine", .03, 9), .01), .5 * fade(soft(noise(.7), .05) * numpy.linspace(1, 0, int(RATE * .7)), .01)),
        numpy.zeros(int(RATE * .08)),
        fade(tone(.05, 1400, 1300, "square"), .001), numpy.zeros(int(RATE * .22)), fade(tone(.05, 1000, 900, "square"), .001)),
    # The hour ends: a rewind, rising, and then everything held lands at once with a crack.
    "ult_time_end": after(
        fade(tone(.5, 70, 1100, "triangle", .04, 14), .01),
        mix(fade(tone(.4, 120, 40), .002), fade(soft(noise(.4), .5) * numpy.linspace(1, 0, int(RATE * .4)), .001), .5 * fade(tone(.3, 2200, 1500), .001))),
    # The tide of ink: a wave of noise rising over a swelling hum.
    "ult_tide": mix(
        swell(soft(noise(1.1), .05), .6) * .9,
        swell(tone(1.1, 110, 260, "sine", .08, 5), .7) * .7,
        after(numpy.zeros(int(RATE * .3)), fade(tone(.6, 500, 160), .02)) * .4),
    # She forms again: a splash and a round drop of a note.
    "ult_tide_end": mix(fade(soft(noise(.4), .12) * numpy.linspace(1, .1, int(RATE * .4)), .004), .6 * fade(tone(.4, 700, 180), .004)),
    # The giant: a boom, with a horn climbing under it.
    "ult_titan": mix(
        fade(tone(.9, 62, 28), .002),
        .8 * fade(soft(noise(.5), .03), .002),
        swell(tone(1.2, 110, 220, "square", .02, 6), .6) * .45),
    # The giant shrinks: a landing, and what falls after.
    "ult_titan_end": mix(fade(tone(.6, 95, 36), .002), .6 * fade(soft(noise(.5), .06) * numpy.linspace(1, 0, int(RATE * .5)), .002)),
    # The legion: three bells, one after another, from far to near.
    "ult_legion": mix(echoes(659, [0, .16, .32], .55), .5 * echoes(988, [.05, .21, .37], .45), .4 * swell(soft(noise(.9), .1), .3)),
    # The legion goes: the same bells, going away, lower.
    "ult_legion_end": echoes(494, [0, .14, .28], .5),
    # The rose blooms: a rising run of bright notes, and a shimmer over them.
    "ult_bloom": mix(
        after(note(784, .1), note(988, .1), note(1175, .1), note(1568, .1), note(1976, .5)),
        .35 * swell(tone(1.0, 3000, 3300, "sine", .02, 14), .5)),
    # The bloom ends: petals falling, notes coming down one by one.
    "ult_bloom_end": after(note(1568, .09), note(1175, .09), note(988, .09), note(784, .4)),
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
