"""
Makes candidates of a song with models that sing, on fal, from words and a style of its own.

    python tools/make_song.py SONG                       what each model would be asked and what it would cost; sends nothing
    python tools/make_song.py SONG --go minimax3 3       makes three songs with that model
    python tools/make_song.py SONG --go minimax26 3 --shade cute    the same in one of the song's other styles ("shades"); the songs are named for it
    python tools/make_song.py SONG --hear                asks fal's transcriber when each word of every song made is sung, and says where the parts fall

SONG is the name of a file in tools/cutscenes (opening-song, for opening-song.json): its
style in words, its length, and its parts in order, each with its lines and where on the
picture it should fall. The songs are saved in dist/cutscenes/BOARD/songs (BOARD is the
name before "-song"), with the words as a text file beside them. Every song made is
entered in the same book of spending as the takes (takes/spending.json), under fal, and
none is made that would pass fal's ceiling.

The models, as fal's pages describe them (read 2026-11-27):
  minimax3   minimax/music-3              a style and the words with [parts] on lines of their own; a length it may stop short of; $0.002 a second
  acestep    fal-ai/ace-step              tags and the words with [parts]; an exact length; $0.0002 a second
  minimax26  fal-ai/minimax-music/v2.6    a style and the words; its own length; $0.15 a song
  eleven     fal-ai/elevenlabs/music      a plan: what the song is and is not to be, and its parts each with its own seconds and lines; $0.60 a minute, rounded up
  mureka     mureka/v9.5/generate/song    the words, a style of at most 1,024 letters, a woman's or a man's voice; two minutes at the least when a length is given; $0.15 a song
  lyria      fal-ai/lyria3/pro            one description, with the words inside it; its own length; $0.08 a song
Only the last takes times (a part's seconds, held to); of the others, where a part falls is what the model makes of it,
and --hear measures it afterwards (fal-ai/whisper, word by word; a sung word is not always
heard right, so a name is looked for by how it begins).
"""
import argparse
import importlib.util
import json
import pathlib
import sys
import time

import requests

HERE = pathlib.Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("make_cutscene", str(HERE / "make_cutscene.py"))
tool = importlib.util.module_from_spec(spec)
spec.loader.exec_module(tool)

def planned(song, shade, short):
    """The song as a plan of parts with their seconds: the roll call as one part for each friend. Short: the first chorus alone, after a few seconds of intro."""
    plan = song["plans"][shade or "cute"]
    local = song["plans"]["local"]
    parts = []
    for part in song["sections"]:
        lines = [line.replace("~\u2661", "").replace("-nim", "nim").replace("-ssi", "sshi").strip() for line in part["lines"]]
        lasts = part["to"] - part["from"]
        if part["tag"] == "bridge":
            each = lasts // (len(lines) // 2)
            for index in range(0, len(lines), 2):
                parts.append({"section_name": f"Call-out {index // 2 + 1}", "positive_local_styles": local.get("bridge", []), "negative_local_styles": [], "duration_ms": each * 1000, "lines": lines[index:index + 2]})
        else:
            parts.append({"section_name": part["tag"].capitalize(), "positive_local_styles": local.get(part["tag"], []), "negative_local_styles": [], "duration_ms": max(3, lasts) * 1000, "lines": lines})
    if short:
        parts = [dict(parts[0], duration_ms=3000), next(part for part in parts if part["section_name"] == "Chorus")]
    return {"composition_plan": {"positive_global_styles": plan["positive"], "negative_global_styles": plan["negative"], "sections": parts}, "respect_sections_durations": True}


MODELS = {
    "mureka": {"id": "mureka/v9.5/generate/song", "costs": lambda seconds: .15,
               "asks": lambda song, words, seed: {"lyrics": words, "prompt": song["style"][:1024], "gender": "female", **({"duration": 120} if song["seconds"] >= 60 else {})}},
    "lyria": {"id": "fal-ai/lyria3/pro", "costs": lambda seconds: .08,
              "asks": lambda song, words, seed: {"prompt": song["style"] + (f" About {song['seconds']} seconds long." if song["seconds"] < 60 else "") + "\n\nLyrics:\n" + words}},
    "eleven": {"id": "fal-ai/elevenlabs/music", "costs": lambda seconds: .6 * -(-seconds // 60), "asks": None},
    "minimax3": {"id": "minimax/music-3", "costs": lambda seconds: .002 * seconds,
                 "asks": lambda song, words, seed: {"prompt": song["style"], "lyrics": words, "duration": song["seconds"], "seed": seed}},
    "acestep": {"id": "fal-ai/ace-step", "costs": lambda seconds: .0002 * seconds,
                "asks": lambda song, words, seed: {"tags": song["tags"], "lyrics": words, "duration": song["seconds"], "seed": seed}},
    "minimax26": {"id": "fal-ai/minimax-music/v2.6", "costs": lambda seconds: .15,
                  "asks": lambda song, words, seed: {"prompt": song["style"], "lyrics": words}},
}
# A name sung is looked for by how it begins, as a transcriber may spell it.
NAMES = {"Chikuwa": ("chik", "chick", "tiku", "chiku", "cheek"), "Rune": ("rune", "roon", "run!", "ruin"), "Illya": ("illya", "ilya", "elia", "ilia", "ellia", "illia", "ella"),
         "Tsubakieh": ("tsub", "subak", "zubak", "tsuba", "suba")}


def sung(song):
    """The words as a model is given them: each part's name on a line of its own, and nothing in a line that is not sung as it is written."""
    lines = []
    for part in song["sections"]:
        lines.append(f"[{part['tag']}]")
        lines += [line.replace("~♡", "").replace("-nim", "nim").replace("-ssi", "sshi").replace("  ", " ").strip() for line in part["lines"]]
        lines.append("")
    return "\n".join(lines).strip()


def shown(song):
    """The words as they are read, with where each part should fall."""
    lines = [song["title"], ""]
    for part in song["sections"]:
        lines.append(f"[{part['tag']}  {part['from']}-{part['to']} s: {part['picture']}]")
        lines += part["lines"] + [""]
    return "\n".join(lines)


def fal(headers, model, asked, note=None):
    """
    One request to fal's queue, waited for; what it answered. With a note to keep (a file), what was sent is written there as soon as
    it is sent, and a request found there from before is asked after and not sent again.
    """
    if note is not None and note.exists():
        sent = json.loads(note.read_text(encoding="utf-8"))
    else:
        reply = requests.post(f"{tool.FAL}/{model}", headers=headers, json=asked, timeout=300)
        if reply.status_code not in (200, 201, 202):
            raise SystemExit(f"The service refused the request: HTTP {reply.status_code} {reply.text[:800]}")
        sent = reply.json()
        if note is not None:
            note.write_text(json.dumps(sent, indent=1), encoding="utf-8")
    print("request", sent["request_id"], flush=True)
    began = time.time()
    while time.time() - began < 3600:
        time.sleep(6)
        try:
            status = requests.get(sent["status_url"], headers=headers, timeout=60).json()
        except (requests.RequestException, ValueError):
            continue
        if status.get("status") not in ("IN_QUEUE", "IN_PROGRESS"):
            break
    result = requests.get(sent["response_url"], headers=headers, timeout=120)
    if result.status_code != 200:
        raise SystemExit(f"Nothing made: HTTP {result.status_code} {result.text[:800]}")
    return sent["request_id"], result.json()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("song")
    parser.add_argument("--go", nargs=2, metavar=("MODEL", "HOW_MANY"), help="make that many songs with that model: this costs money")
    parser.add_argument("--hear", action="store_true", help="measure where the parts of the songs made fall")
    parser.add_argument("--shade", help="one of the song's other styles, by name")
    parser.add_argument("--only", help="with --hear: only the songs whose names begin so")
    parser.add_argument("--short", action="store_true", help="a test of the voice: the first chorus alone (named ...-test-N)")
    args = parser.parse_args()
    song = json.loads((tool.BOARDS / f"{args.song}.json").read_text(encoding="utf-8"))
    if args.shade:
        song["style"] = song["shades"][args.shade]
    folder = tool.SHOTS / args.song.rsplit("-song", 1)[0]
    out = folder / "songs"
    out.mkdir(parents=True, exist_ok=True)
    words = sung(song)
    if args.short:
        chorus = next(part for part in song["sections"] if part["tag"] == "chorus")
        words = sung({"sections": [chorus]})
        song["seconds"] = 21
    (out / "lyrics.txt").write_text(shown(song) + "\n", encoding="utf-8")
    (out / "lyrics-as-sent.txt").write_text(words + "\n", encoding="utf-8")
    book, spent = tool.spending(folder)
    if not args.go and not args.hear:
        print(shown(song))
        print("\nstyle:", song["style"])
        for name, model in MODELS.items():
            print(f"{name}: {model['id']}, about ${model['costs'](song['seconds']):.3f} a song")
        print(f"left to spend with fal: ${tool.left_of(spent, 'fal'):.2f}. Nothing sent.")
        return
    key = tool.secret(tool.FAL_KEY_NAMES)
    if not key:
        raise SystemExit("No key for fal.")
    headers = {"Authorization": f"Key {key}", "Content-Type": "application/json"}
    if args.go:
        many = int(args.go[1])
        model = MODELS[args.go[0]]
        name = args.go[0] + (f"-{args.shade}" if args.shade else "") + ("-test" if args.short else "")
        price = model["costs"](song["seconds"])
        for _ in range(many):
            _, spent = tool.spending(folder)
            left = tool.left_of(spent, "fal")
            print(f"{name}: about ${price:.3f}; left with fal ${left:.2f}", flush=True)
            if price > left:
                raise SystemExit("Not sent: fal's ceiling would be passed.")
            waiting = sorted(out.glob(f"{name}-*.sent"))
            number = int(waiting[0].stem.split("-")[-1]) if waiting else max([int(made.stem.split("-")[-1]) for made in out.glob(f"{name}-*.json") if made.stem.split("-")[-1].isdigit()], default=0) + 1
            seed = 1000 * number + 7
            note = out / f"{name}-{number}.sent"
            asked = planned(song, args.shade, args.short) if model["asks"] is None else model["asks"](song, words, seed)
            request, answer = fal(headers, model["id"], asked, note)
            note.unlink()
            audio = answer.get("audio") or {}
            address = audio.get("url") if isinstance(audio, dict) else audio
            if not address:
                raise SystemExit(f"No song in the answer: {json.dumps(answer)[:500]}")
            sound = requests.get(address, timeout=600)
            sound.raise_for_status()
            kind = pathlib.Path(address.split("?")[0]).suffix or ".mp3"
            target = out / f"{name}-{number}{kind}"
            target.write_bytes(sound.content)
            (out / f"{name}-{number}.json").write_text(json.dumps({"model": model["id"], "request": request, "seed": seed, "address": address, "answer": answer}, indent=1), encoding="utf-8")
            _, spent = tool.spending(folder)
            spent["fal"].setdefault("calls", []).append({"when": time.strftime("%Y-%m-%d %H:%M:%S"), "what": f"song {target.name} with {model['id']}", "seconds": song["seconds"], "price": round(price, 3), "state": "made", "clip": target.name})
            book.write_text(json.dumps(spent, indent=1), encoding="utf-8")
            print("saved", target, f"{len(sound.content):,} bytes", flush=True)
    if args.hear:
        import av

        for note in sorted(out.glob("*-*.json")):
            if note.name.endswith(".heard.json") or note.name.startswith("lyrics") or (args.only and not note.name.startswith(args.only)):
                continue
            made = json.loads(note.read_text(encoding="utf-8"))
            heard = note.with_suffix(".heard.json")
            if not heard.exists():
                _, answer = fal(headers, "fal-ai/whisper", {"audio_url": made["address"], "task": "transcribe", "language": "en", "chunk_level": "word"})
                heard.write_text(json.dumps(answer, indent=1), encoding="utf-8")
            answer = json.loads(heard.read_text(encoding="utf-8"))
            chunks = [(chunk["timestamp"][0], chunk["text"].strip()) for chunk in answer.get("chunks", []) if chunk.get("timestamp") and chunk["timestamp"][0] is not None]
            clip = next(out.glob(note.stem + ".*[!n]"), None)
            lasts = None
            for file in out.glob(note.stem + ".*"):
                if file.suffix in (".wav", ".mp3", ".flac", ".m4a"):
                    lasts = float(av.open(str(file)).duration) / 1e6
            found = {}
            for who, begins in NAMES.items():
                found[who] = next((when for when, word in chunks if word.lower().strip("!,.?\"'").startswith(begins) or any(word.lower().startswith(b) for b in begins)), None)
            pitch = None
            try:
                import numpy

                file = next(found_ for found_ in out.glob(note.stem + ".*") if found_.suffix in (".wav", ".mp3", ".flac", ".m4a"))
                box = av.open(str(file))
                resampler = av.AudioResampler(format="flt", layout="mono", rate=16000)
                wave = numpy.concatenate([made_.to_ndarray().ravel() for frame_ in box.decode(audio=0) for made_ in resampler.resample(frame_)])
                found_pitches = []
                for when, _ in chunks:
                    piece = wave[int(when * 16000):int(when * 16000) + 4096]
                    if len(piece) < 4096:
                        continue
                    spectrum = numpy.abs(numpy.fft.rfft(piece * numpy.hanning(4096)))
                    hertz = numpy.fft.rfftfreq(4096, 1 / 16000)
                    band = (hertz >= 180) & (hertz <= 1100)
                    # The strongest pitch of a voice's range whose octave and twelfth above it sound too (a sung note, not a drum).
                    score = spectrum[band].copy()
                    for k, f in enumerate(hertz[band]):
                        score[k] *= 1 + spectrum[min(len(spectrum) - 1, int(round(2 * f / hertz[1])))] / (spectrum[band].max() + 1e-9)
                    found_pitches.append(float(hertz[band][int(numpy.argmax(score))]))
                pitch = float(numpy.median(found_pitches)) if found_pitches else None
            except Exception as error:
                pitch = None
            squish = [when for when, word in chunks if word.lower().startswith("squish")]
            sloppy = [when for when, word in chunks if "slop" in word.lower()]
            first = chunks[0][0] if chunks else None
            print(f"{note.stem}: {lasts:.1f} s; voice's middle pitch about {pitch and round(pitch)} Hz; first word at {first}; roll call: " + ", ".join(f"{who} {('%.1f' % when) if when is not None else 'not heard'}" for who, when in found.items())
                  + f"; 'squish' at {[round(t, 1) for t in squish]}; last 'Sloppy' at {round(sloppy[-1], 1) if sloppy else None}; last word at {chunks[-1][0] if chunks else None}")
            print("   heard:", " ".join(word for _, word in chunks)[:700])


if __name__ == "__main__":
    sys.exit(main())
