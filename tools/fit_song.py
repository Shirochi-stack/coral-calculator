"""
A song made somewhere else, measured against a film whose seconds are already fixed by its picture.

    python tools/fit_song.py SONG TRACK              how long the track is, its beat, where it changes, against SONG's parts
    python tools/fit_song.py SONG TRACK --words      the same, and where each line of the words is sung (asks a transcriber on fal: no charge when last looked, said before it is asked)
    python tools/fit_song.py SONG --lyrics FILE      no track yet: where each line of new words would go on the parts, and how fast it would have to be sung
    python tools/fit_song.py SONG TRACK --lyrics FILE --words     a track with words of its own: the lines looked for are FILE's

SONG is a file in tools/cutscenes (idol-song: its "sections" are the parts, each with the seconds it has on the picture). Nothing
is changed and nothing is bought. What it says at the end is what the edit would be asked (make_cutscene.py --edit --song TRACK
--song-from S --song-speed X) to lay the track on the film as it is, and how far off each part would still be: if that is more
than a beat or two, the track does not fit by sliding and stretching alone, and shots would have to be cut to it.

How: the length and the first sound are read from the samples. The beat is the lag at which the track's onsets (rises in loudness,
band by band) best repeat; the picture's cuts are on its own beat, so the first fit is by the beat alone: the speed that makes the
two beats one, and the slide that puts the track's beats on the picture's. A change is a moment where the four seconds after sound
least like the four seconds before: these are listed to be looked at, and not fitted by, since a part does not always begin with a
change of sound. With --words the second fit is the straight line through (the second a part begins on the picture, the second its
first line is sung): its slope is the speed, its start the second of the track the film begins on. A line is looked for near where
it would be if the track were the picture's length (within ten seconds), never before the line above it, as the run of heard words
most of which are the line's; a transcriber misses and mishears sung words, so a line not heard may still be sung.
A lyrics file is plain text: one line a line; a line in [square brackets] names the part the lines under it belong to (matched to
SONG's parts: [Verse] is the next verse, [Chorus] the next chorus, [Rune] the member whose picture is Rune's); blank lines are
skipped. Without such names, stanzas (lines between blank lines) are given to the parts in order. What the picture has and the
words have not, and the other way round, is said.
Needs numpy, av; with --words also requests, soundfile, and the key of fal, by name.
"""
import argparse
import importlib.util
import json
import pathlib
import re
import sys

import numpy

ROOT = pathlib.Path(__file__).resolve().parent.parent
RATE = 22050


def heard_of(track):
    """A track's samples, one channel, at this file's rate."""
    import av

    box = av.open(str(track))
    resampler = av.AudioResampler(format="flt", layout="mono", rate=RATE)
    return numpy.concatenate([made.to_ndarray().ravel() for frame in box.decode(audio=0) for made in resampler.resample(frame)])


def bands_of(wave, hop=512, size=1024, count=24):
    """How loud the track is in each of some bands of pitch, moment by moment (in decibels, as an ear would have it)."""
    frames = 1 + (len(wave) - size) // hop
    window = numpy.hanning(size)
    edges = numpy.geomspace(60, RATE / 2, count + 1) * size / RATE
    out = numpy.zeros((frames, count), numpy.float32)
    for start in range(0, frames, 2000):
        index = numpy.arange(start, min(frames, start + 2000))[:, None] * hop + numpy.arange(size)[None, :]
        power = numpy.abs(numpy.fft.rfft(wave[index] * window, axis=1)) ** 2
        for band in range(count):
            low, high = int(edges[band]), max(int(edges[band]) + 1, int(edges[band + 1]))
            out[start:start + len(index), band] = power[:, low:high].sum(axis=1)
    return 10 * numpy.log10(out + 1e-9), hop / RATE


def beat_of(bands, step):
    """The track's beat: how many to the minute, how sure, and when the first falls."""
    rises = numpy.maximum(0, numpy.diff(bands, axis=0)).sum(axis=1)
    rises = rises - numpy.convolve(rises, numpy.ones(43) / 43, mode="same")
    rises = numpy.maximum(0, rises)
    again = numpy.correlate(rises, rises, "full")[len(rises) - 1:]
    lags = numpy.arange(int(60 / 200 / step), int(60 / 60 / step))
    # (Of a beat and its half or double, the one nearer 120 to the minute is taken: that is how it would be counted.)
    weight = numpy.exp(-.5 * (numpy.log2(60 / (lags * step) / 120) / .7) ** 2)
    lag = int(lags[int(numpy.argmax(again[lags] * weight))])
    # (Between two lags the true one lies a little to one side: where a curve through the three would peak.)
    a, b, c = again[lag - 1], again[lag], again[lag + 1]
    exact = lag + (.5 * (a - c) / (a - 2 * b + c) if a - 2 * b + c else 0)
    # A longer look for the same beat: the lag near a whole number of beats, sixteen beats on, says the beat sixteen times as finely.
    far = int(round(exact * 16))
    if far + 3 < len(again):
        near = numpy.arange(far - 3, far + 4)
        best = int(near[int(numpy.argmax(again[near]))])
        a, b, c = again[best - 1], again[best], again[best + 1]
        exact = (best + (.5 * (a - c) / (a - 2 * b + c) if a - 2 * b + c else 0)) / 16
    period = exact * step
    phases = numpy.arange(0, period, step)
    marks = [float(numpy.interp(numpy.arange(phase, len(rises) * step, period), numpy.arange(len(rises)) * step, rises).mean()) for phase in phases]
    return 60 / period, float(again[lag] / (again[0] + 1e-9)), float(phases[int(numpy.argmax(marks))])


def changes_of(bands, step, span=4.0, apart=3.0):
    """Moments where the track becomes something else: (second, how strongly), strongest first."""
    every = max(1, int(.25 / step))
    width = int(span / step)
    total = numpy.concatenate([numpy.zeros((1, bands.shape[1])), numpy.cumsum(bands, axis=0)])
    moments, scores = [], []
    for at in range(width, len(bands) - width, every):
        before = (total[at] - total[at - width]) / width
        after = (total[at + width] - total[at]) / width
        moments.append(at * step)
        scores.append(float(numpy.abs(after - before).mean()))
    moments, scores = numpy.array(moments), numpy.array(scores)
    found = []
    for index in numpy.argsort(-scores):
        if scores[index] < numpy.median(scores) * 1.25:
            break
        if all(abs(moments[index] - when) >= apart for when, _ in found):
            found.append((float(moments[index]), float(scores[index] / (numpy.median(scores) + 1e-9))))
    return found


def line_fit(pairs):
    """The straight line through (second on the picture, second in the track): speed, start, and what is left of each pair."""
    if len(pairs) < 2:
        return 1.0, (pairs[0][1] - pairs[0][0] if pairs else 0.0), [0.0] * len(pairs)
    asked, came = numpy.array([pair[0] for pair in pairs]), numpy.array([pair[1] for pair in pairs])
    slope, start = numpy.polyfit(asked, came, 1)
    return float(slope), float(start), [float(value) for value in came - (slope * asked + start)]


def beats_in(words):
    """Roughly how many syllables some words have: their groups of vowels."""
    total = 0
    for word in re.findall(r"[A-Za-z']+", words):
        count = len(re.findall(r"[aeiouy]+", word.lower()))
        if word.lower().endswith("e") and not word.lower().endswith(("le", "ee")) and count > 1:
            count -= 1
        total += max(1, count)
    return total


def lyrics_of(path, parts):
    """A lyrics file as the parts' lines: [(part, [lines])], and what could not be placed."""
    named, stanzas, stanza, label = [], [], [], None
    for raw in pathlib.Path(path).read_text(encoding="utf-8").splitlines() + [""]:
        line = raw.strip()
        if line.startswith("[") and line.endswith("]"):
            if stanza:
                stanzas.append((label, stanza))
            stanza, label = [], line[1:-1].strip().lower()
            named.append(label)
        elif not line:
            if stanza:
                stanzas.append((label, stanza))
            stanza = []
            label = label if named else None
        else:
            stanza.append(line)
    notes, given, used = [], {}, -1
    for label, lines in stanzas:
        target = None
        if label:
            # A named stanza goes to the next part of that name ([Verse 2] -> the next verse; [Rune] -> the member whose picture is Rune's).
            key = label.split(" ")[0]
            target = next((index for index in range(used + 1, len(parts))
                           if parts[index]["tag"] in (label, key) or (parts[index]["tag"] == "member" and key in parts[index].get("picture", "").lower())), None)
            if target is None and used + 1 < len(parts):
                notes.append(f"the picture has no part named '[{label}]' after {parts[used]['from'] if used >= 0 else 0} s: its lines are given to the next part, {parts[used + 1]['tag']} at {parts[used + 1]['from']} s")
        if target is None:
            target = used + 1
        if target >= len(parts):
            notes.append(f"no part of the picture is left for: {' / '.join(lines)[:90]}")
            continue
        for skipped in range(used + 1, target):
            notes.append(f"the words have nothing for the picture's {parts[skipped]['tag']} at {parts[skipped]['from']}-{parts[skipped]['to']} s ({parts[skipped].get('picture', '')})")
        given[target], used = lines, target
    for skipped in range(used + 1, len(parts)):
        notes.append(f"the words have nothing for the picture's {parts[skipped]['tag']} at {parts[skipped]['from']}-{parts[skipped]['to']} s ({parts[skipped].get('picture', '')})")
    return [(part, given.get(index, [])) for index, part in enumerate(parts)], notes


def words_heard(track):
    """What a transcriber hears in the track: [(second, word)]. Kept beside the track, so it is asked once."""
    kept = pathlib.Path(track).with_suffix(".heard.json")
    if not kept.exists():
        import requests
        import soundfile

        def module(name):
            spec = importlib.util.spec_from_file_location(name, str(ROOT / "tools" / f"{name}.py"))
            made = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(made)
            return made

        cut, song = module("make_cutscene"), module("make_song")
        key = cut.secret(("FAL_KEY",))
        if not key:
            raise SystemExit("--words asks a transcriber on fal: its key is not in the environment or the project's .env.")
        headers = {"Authorization": f"Key {key}", "Content-Type": "application/json"}
        price = requests.get("https://api.fal.ai/v1/models/pricing", params={"endpoint_id": "fal-ai/whisper"}, headers=headers, timeout=60)
        print("the transcriber's price, as fal gives it:", price.text[:200] if price.status_code == 200 else f"not given (HTTP {price.status_code})", flush=True)
        wave = heard_of(track)
        small = numpy.interp(numpy.arange(int(len(wave) * 16000 / RATE)) * RATE / 16000, numpy.arange(len(wave)), wave)
        local = kept.with_suffix(".16k.wav")
        soundfile.write(local, small, 16000, subtype="PCM_16")
        place = requests.post(cut.FAL_STORE, headers=headers, json={"file_name": local.name, "content_type": "audio/wav"}, timeout=60).json()
        put = requests.put(place["upload_url"], data=local.read_bytes(), headers={"Content-Type": "audio/wav"}, timeout=(30, 300))
        if put.status_code not in (200, 201, 204):
            raise SystemExit(f"the track could not be put in fal's file store: HTTP {put.status_code}")
        _, answer = song.fal(headers, "fal-ai/whisper", {"audio_url": place["file_url"], "task": "transcribe", "language": "en", "chunk_level": "word"})
        kept.write_text(json.dumps(answer, indent=1), encoding="utf-8")
        local.unlink()
    answer = json.loads(kept.read_text(encoding="utf-8"))
    return [(chunk["timestamp"][0], chunk["text"].strip()) for chunk in answer.get("chunks", []) if chunk.get("timestamp") and chunk["timestamp"][0] is not None]


def plain(word):
    return re.sub(r"[^a-z]", "", word.lower())


def lines_found(placed, heard, scale):
    """Each line's second in the track, and whether that is its first word's: the run of heard words most like the line, near where the line would be, never before the line above it."""
    spoken = [(when, plain(word)) for when, word in heard if plain(word)]
    found, after = [], -1.0
    for part, lines in placed:
        for number, line in enumerate(lines):
            wanted = [plain(word) for word in re.findall(r"[A-Za-z']+", re.sub(r"\([^)]*\)", "", line)) if plain(word)]
            due = (part["from"] + (part["to"] - part["from"]) * number / max(1, len(lines))) * scale
            best = None
            for index, (when, word) in enumerate(spoken):
                if when <= after or abs(when - due) > 10 or not any(word[:3] == want[:3] for want in wanted):
                    continue
                left, got = list(wanted), 0
                for _, other in spoken[index:index + len(wanted) + 2]:
                    same = next((want for want in left if want[:3] == other[:3]), None)
                    if same is not None:
                        left.remove(same)
                        got += 1
                enough = got == len(wanted) if len(wanted) <= 2 else got >= 2 and got / len(wanted) >= .5
                if enough and (best is None or (got, -abs(when - due)) > (best[0], -abs(best[1] - due))):
                    best = (got, when, word[:3] == wanted[0][:3])
            if best:
                after = best[1]
            found.append((part, line, best[1] if best else None, bool(best and best[2]), number == 0))
    return found


def shown(text):
    return text.encode("ascii", "replace").decode()


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("song", help="the film's song file in tools/cutscenes, without .json (its parts and their seconds)")
    parser.add_argument("track", nargs="?", help="a sound file")
    parser.add_argument("--lyrics", help="a text file of words: the lines to place, in place of the song file's own")
    parser.add_argument("--words", action="store_true", help="ask a transcriber where the lines are sung")
    args = parser.parse_args()
    song = json.loads((ROOT / "tools" / "cutscenes" / f"{args.song}.json").read_text(encoding="utf-8"))
    parts, total = song["sections"], song["seconds"]
    board_file = ROOT / "tools" / "cutscenes" / f"{args.song.removesuffix('-song')}.json"
    shots = []
    if board_file.exists():
        board, at = json.loads(board_file.read_text(encoding="utf-8")), 0.0
        for shot in board["shots"]:
            shots.append((shot["name"], at, at + shot["bars"] * board["bar"]))
            at += shot["bars"] * board["bar"]
    if args.lyrics:
        placed, notes = lyrics_of(args.lyrics, parts)
    else:
        placed, notes = [(part, part["lines"]) for part in parts], []

    print(f"The picture: {total} s, {len(parts)} parts" + (f", {len(shots)} shots" if shots else ""))
    print("\nWHERE THE WORDS GO ON THE PICTURE" + (" (the song file's own words)" if not args.lyrics else f" ({pathlib.Path(args.lyrics).name})"))
    for part, lines in placed:
        lasts = part["to"] - part["from"]
        on = ", ".join(name for name, begins, ends in shots if begins < part["to"] and ends > part["from"])
        count = sum(beats_in(re.sub(r"\([^)]*\)", "", line)) for line in lines)
        pace = count / lasts
        say = "nothing to sing" if not lines else "too many words to sing clearly" if pace > 5.5 else "quick" if pace > 4.2 else "a few words, room to breathe" if pace < 1.5 else "comfortable"
        print(f"  {part['from']:4.0f}-{part['to']:<3.0f} s {part['tag']:<11} shots {on:<12} {count:3d} syllables in {lasts:.0f} s = {pace:.1f} a second: {say}")
        for line in lines:
            print("        " + shown(line))
    for note in notes:
        print("  NOTE:", shown(note))
    if not args.track:
        return 0

    wave = heard_of(args.track)
    length = len(wave) / RATE
    loud = numpy.abs(wave)
    sounding = numpy.where(loud > .02 * loud.max())[0]
    first, last = sounding[0] / RATE, sounding[-1] / RATE
    print(f"\nTHE TRACK: {pathlib.Path(args.track).name}")
    print(f"  length {length:.2f} s against the picture's {total} s: {length - total:+.2f} s; first sound at {first:.2f} s, last at {last:.2f} s")
    print(f"  played {length / total:.3f} times as fast it would be exactly {total} s" + ("" if abs(length / total - 1) < .06 else ": that is more than a listener would let pass (about 6%)"))
    bands, step = bands_of(wave)
    tempo, sure, phase = beat_of(bands, step)
    bar = song.get("bar_seconds", 2.0)
    wanted = 240 / bar
    print(f"  beat: {tempo:.1f} to the minute (how sure: {sure:.2f} of 1), the first at {phase:.2f} s; the picture is cut at {wanted:.0f} to the minute (a bar is {bar:.0f} s)")
    ratio = min((tempo * factor / wanted for factor in (.5, 1, 2)), key=lambda value: abs(numpy.log(value)))
    print(f"  to bring its beat onto the picture's cuts it would be played {1 / ratio:.3f} times as fast, and would then last {length * ratio:.1f} s")

    # The beat alone: the speed that makes the track's beat the picture's, and how far its beats then lie from the picture's.
    period = 60 / wanted
    off = ((phase * ratio + period / 2) % period) - period / 2
    print(f"\nTHE FIT BY THE BEAT")
    print(f"  at that speed, laid from the film's first frame, its beats fall {abs(off):.2f} s {'after' if off > 0 else 'before'} the picture's (a beat is {period:.2f} s)")
    if abs(1 / ratio - 1) > .06:
        print(f"  the speed needed ({1 / ratio:.3f}) is more than can be done without its being heard: the picture would have to be cut to the track's beat (its bar is {240 / tempo:.2f} s, the picture's {bar:.2f} s)")
    else:
        print(f"  the edit: python tools/make_cutscene.py {args.song.removesuffix('-song')} --edit --with NAME --song \"{args.track}\" --named NAME2 --song-from {off / ratio:.2f} --song-speed {1 / ratio:.3f}")
        print("  (this puts beats on cuts; whether the parts fall on their shots is what the words and the changes below say)")

    changes = changes_of(bands, step)
    print("\nWHERE THE TRACK CHANGES (second: how strongly, 1 = no more than usual); to be looked at against where the picture's parts begin")
    print("  the track:   " + ", ".join(f"{when:.1f} ({much:.1f})" for when, much in sorted(changes)))
    print("  the picture: " + ", ".join(f"{part['from']:.0f} {part['tag']}" for part in parts))
    if not args.words:
        return 0
    heard = words_heard(args.track)
    found = lines_found(placed, heard, length / total)
    print(f"\nWHERE THE LINES ARE SUNG ({len(heard)} words heard; a line not heard may still be sung)")
    pairs = []
    for part, line, when, exact, opens in found:
        mark = ""
        if when is not None and opens and exact:
            pairs.append((part["from"], when))
            mark = f"   <- its part begins at {part['from']} s on the picture: {when - part['from']:+.1f} s"
        elif when is not None and not part["from"] - .5 <= when <= part["to"]:
            mark = f"   (outside its part's {part['from']}-{part['to']} s)"
        print(f"  {('%6.1f s' % when) if when is not None else ' not heard'}{' ' if exact or when is None else '~'} {shown(line)[:70]}{mark}")
    print("  (~ : found by words further into the line, so the line begins a little before that)")
    print(f"\nTHE FIT BY THE WORDS (the first line of {len(pairs)} of the picture's {len(parts)} parts found)")
    if len(pairs) < 3:
        print("  too few to fit by.")
        return 0
    print("  as it is, laid from the film's first frame: " + ", ".join(f"{asked:.0f} s -> {came - asked:+.1f}" for asked, came in pairs))
    if sure >= .2 and abs(1 / ratio - 1) <= .06:
        # The beat says the speed far more finely than a few lines can: the speed is the beat's, and only the start is fitted (the middle of what the parts ask).
        slope = 1 / ratio
        start = float(numpy.median([came - slope * asked for asked, came in pairs]))
        left = [came - (slope * asked + start) for asked, came in pairs]
        print(f"  (the speed is taken from the beat, {slope:.3f}; only the start is fitted)")
    else:
        slope, start, left = line_fit(pairs)
        # (One place far from the line the rest make is a wrong match, not a part of another length: it is set aside, once.)
        far = max(range(len(left)), key=lambda index: abs(left[index]))
        if len(pairs) > 4 and abs(left[far]) > 1.5 and abs(left[far]) > 3 * numpy.median(numpy.abs(left)):
            print(f"  (set aside as a wrong match: {pairs[far][0]:.0f} s, found at {pairs[far][1]:.1f})")
            pairs = pairs[:far] + pairs[far + 1:]
            slope, start, left = line_fit(pairs)
    print(f"  best: begin the film on second {start:.2f} of the track and play it {slope:.3f} times as fast (a sung line begins a little after its part does: a few tenths late is as it should be)")
    print("  what would still be off then: " + ", ".join(f"{asked:.0f} s -> {rest:+.1f}" for (asked, _), rest in zip(pairs, left)))
    worst = max(abs(rest) for rest in left)
    as_is = max(abs(came - asked) for asked, came in pairs)
    if as_is <= 1.0:
        verdict = f"it fits as it is, laid from the film's first frame: no part's first line is more than {as_is:.1f} s from where its part begins"
    elif abs(slope - 1) > .06:
        verdict = f"the speed needed ({slope:.3f}) is more than can be done without its being heard: the track is built to other lengths than the picture, and shots would have to be cut to it"
    elif worst > 1.0:
        late = ", ".join(f"the part at {asked:.0f} s by {rest:+.1f}" for (asked, _), rest in zip(pairs, left) if abs(rest) > 1.0)
        verdict = f"sliding and stretching still leave {late}: those parts of the track are longer or shorter than the picture's, and their shots would have to be cut to it (or the lateness lived with)"
    else:
        verdict = "it fits with the start and speed above"
    print("  " + verdict)
    if as_is > 1.0:
        print(f"  the edit: python tools/make_cutscene.py {args.song.removesuffix('-song')} --edit --with NAME --song \"{args.track}\" --named NAME2 --song-from {start:.2f} --song-speed {slope:.3f}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
