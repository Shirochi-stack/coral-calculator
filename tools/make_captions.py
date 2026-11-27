"""
What is written under Plus run's films: the table tools/make_run_films.py turns into the
files the game reads.

    python tools/make_captions.py

Writes tools/cutscenes/captions.json: for each set of words, its lines in every language
there is; and for each sound, which set of words it is and when each line of it begins and
ends in that sound. Where the times come from:

  Halgakos on the user's words (prologue_h, idol_h, and _ja, _ko, _zh): the line files
      (prologue-mc.json, idol-mc.json) and the times tools/make_mc.py wrote beside each track
      (OUT.times.json). A line of the lyrics may be several lines of the line file (her call
      and the crowd's answer): the line file's "captions" says which; the crowd's part is
      shown in brackets, as in the lyrics.
  Halgakos as she first did it: the prologue's song (dist/cutscenes/opening/song.json: each
      line's second and length as it was laid); the idol song's lines in tools/make_idol_mc.py.
  The sound the picture's model made (prologue_o, idol_tall_m, idol_wide_m): the first
      lyrics, each line at the second a transcriber heard it begin (the readings kept in
      dist/cutscenes: *.heard.json); a line it did not hear is put where it was planned,
      between the lines that were heard.

The first lyrics in other languages are in tools/cutscenes/first-lyrics.json, line for line.
A language that is not there for a set of words is simply not written for it.
"""
import importlib.util
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
BOARDS = ROOT / "tools" / "cutscenes"
SHOTS = ROOT / "dist" / "cutscenes"
TONGUES = ("ja", "ko", "zh")


def module(name):
    spec = importlib.util.spec_from_file_location(name, str(ROOT / "tools" / f"{name}.py"))
    made = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(made)
    return made


def read(path):
    return json.loads(path.read_text(encoding="utf-8"))


def grouped(spec):
    """Which lines of a line file make each line of the lyrics: as the file says, or one for one."""
    return spec.get("captions") or [[index] for index in range(len(spec["lines"]))]


def shown(spec, words, group):
    """A line of the lyrics from the lines of a line file: what she says as it is, what the crowd answers in brackets."""
    return " ".join(f"({words[index]})" if spec["lines"][index][3] == "crowd" else words[index] for index in group)


def spans(times, groups):
    return [[times[group[0]][0], times[group[-1]][1]] for group in groups]


def heard_times(lines, planned, heard_file, total):
    """When each line of some lyrics begins in a sound, by what a transcriber heard; and where it ends: when the next begins, or a few seconds on."""
    fit = module("fit_song")
    answer = read(heard_file)
    heard = [(chunk["timestamp"][0], chunk["text"].strip()) for chunk in answer.get("chunks", []) if chunk.get("timestamp") and chunk["timestamp"][0] is not None]
    placed = [({"from": begins, "to": ends}, [line]) for line, (begins, ends) in zip(lines, planned)]
    found = fit.lines_found(placed, heard, 1.0)
    starts, last = [], 0.0
    for (begins, _), (_, _, when, exact, _) in zip(planned, found):
        # (A line found by words further into it began a little before that; one not heard is where it was planned, never before the line above.)
        start = begins if when is None else when if exact else max(begins - .5, when - .8)
        start = max(start, last + .3)
        starts.append(round(start, 2))
        last = start
    heard_count = sum(1 for _, _, when, _, _ in found if when is not None)
    times = [[start, round(min(starts[index + 1] if index + 1 < len(starts) else total, start + 6.0), 2)] for index, start in enumerate(starts)]
    return times, heard_count


def main():
    words, sounds, notes = {}, {}, []
    # Halgakos on the user's words.
    for film, name, track in (("prologue", "prologue-mc", SHOTS / "opening" / "prologue-halgakos"), ("idol", "idol-mc", SHOTS / "idol" / "songs" / "idol-halgakos")):
        spec = read(BOARDS / f"{name}.json")
        groups = grouped(spec)
        key = f"{film}_new"
        words[key] = {"en": [shown(spec, [line[2] for line in spec["lines"]], group) for group in groups]}
        for tongue in TONGUES:
            other = spec.get("tongues", {}).get(tongue)
            if other and len(other["lines"]) == len(spec["lines"]):
                words[key][tongue] = [shown(spec, other["lines"], group) for group in groups]
        for tongue in ("",) + TONGUES:
            times = pathlib.Path(str(track) + (f"-{tongue}" if tongue else "") + ".times.json")
            if times.exists():
                sounds[f"{film}_h" + (f"_{tongue}" if tongue else "")] = {"words": key, "times": spans(read(times), groups)}
    more = read(BOARDS / "first-lyrics.json") if (BOARDS / "first-lyrics.json").exists() else {}
    # The prologue's first lyrics: Halgakos's own first song of them, and the sound Seedance made.
    old = read(SHOTS / "opening" / "song.json")
    lines = [line["shown"] for line in old["lines"]]
    words["prologue_first"] = dict({"en": lines}, **{tongue: said for tongue, said in more.get("prologue_first", {}).items() if len(said) == len(lines)})
    laid = [[line["at"], round(line["at"] + line["lasts"], 2)] for line in old["lines"]]
    sounds["prologue_l"] = {"words": "prologue_first", "times": laid}
    times, count = heard_times(lines, laid, SHOTS / "opening" / "prologue_o.heard.json", old["seconds"])
    sounds["prologue_o"] = {"words": "prologue_first", "times": times}
    notes.append(f"prologue_o: {count} of {len(lines)} lines heard")
    # The idol song's first lyrics, as MAGI-2 sang them with each picture.
    song = read(BOARDS / "idol-song.json")
    lines, planned = [], []
    for part in song["sections"]:
        each = (part["to"] - part["from"]) / len(part["lines"])
        for index, line in enumerate(part["lines"]):
            lines.append(line)
            planned.append([round(part["from"] + each * index, 2), round(part["from"] + each * (index + 1), 2)])
    words["idol_first"] = dict({"en": lines}, **{tongue: said for tongue, said in more.get("idol_first", {}).items() if len(said) == len(lines)})
    for shape in ("tall", "wide"):
        times, count = heard_times(lines, planned, SHOTS / "idol" / f"idol_{shape}_m.heard.json", song["seconds"])
        sounds[f"idol_{shape}_m"] = {"words": "idol_first", "times": times}
        notes.append(f"idol_{shape}_m: {count} of {len(lines)} lines heard")
    # Halgakos as the idols' MC on the first words: her lines, with the crowd's answers after them in brackets.
    first = module("make_idol_mc")
    groups = []
    for index, (_, _, _, how) in enumerate(first.LINES):
        if how == "crowd" and groups and any(first.LINES[other][3] != "crowd" for other in groups[-1]):
            groups[-1].append(index)
        elif how == "crowd" and groups and len(groups[-1]) == 1:
            groups[-1].append(index)
        else:
            groups.append([index])
    lines = [" ".join(f"({first.LINES[index][2]})" if first.LINES[index][3] == "crowd" else first.LINES[index][2] for index in group) for group in groups]
    # (The crowd alone, chanting two words: one pair of brackets round both.)
    lines = [re.sub(r"\) \(", " ", line) if line.startswith("(") else line for line in lines]
    words["idol_legacy"] = dict({"en": lines}, **{tongue: said for tongue, said in more.get("idol_legacy", {}).items() if len(said) == len(lines)})
    sounds["idol_l"] = {"words": "idol_legacy", "times": [[first.LINES[group[0]][0], round(first.LINES[group[-1]][0] + first.LINES[group[-1]][1], 2)] for group in groups]}
    table = {"about": "What is written under Plus run's films: made by tools/make_captions.py (see there for where each time comes from); read by tools/make_run_films.py captions. "
                      "words: each set of words, line by line, in each language there is. sounds: for each sound, its set of words and when each line begins and ends in it, in seconds.",
             "words": words, "sounds": sounds}
    (BOARDS / "captions.json").write_text(json.dumps(table, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    for name, entry in sounds.items():
        print(f"{name}: {len(entry['times'])} lines of {entry['words']} in {', '.join(words[entry['words']])}")
    for note in notes:
        print(note)


if __name__ == "__main__":
    main()
