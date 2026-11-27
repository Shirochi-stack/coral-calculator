"""
Makes the story's clips (tools/clips_data.py): the picture of each from a video model, its voices from Inworld in four languages,
the voices fitted to the untouched picture, and a review copy with English captions.

    python tools/make_clips.py plan                  writes dist/cutscenes/clips/plan.md
    python tools/make_clips.py gen ID [--go]         says what would be sent and costs; with --go sends it (kling and seedance)
    python tools/make_clips.py tts ID                records its lines in en, ja, ko, zh
    python tools/make_clips.py fit ID [--source F]   fits the voices to the picture, writes ID/ID-<lang>.m4a and the review copy
    python tools/make_clips.py show                  the spending log

Keys, by name, from the project's .env: FAL_KEY, BYTEPLUS_API_Key, INWORLD_API_KEY. Never printed. Needs numpy, soundfile, av, requests, Pillow.
Everything is written under CLIPS (dist/cutscenes/clips in the main checkout).
"""
import argparse
import importlib.util
import json
import pathlib
import sys
import time

ROOT = pathlib.Path(__file__).resolve().parent.parent
CLIPS = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\clips")
DIST = CLIPS.parent
sys.path.insert(0, str(ROOT / "tools"))
import clips_data as data  # noqa: E402

LANGS = ("en", "ja", "ko", "zh")
BUDGET = {"fal": 25.96, "byteplus": 20.60, "higgsfield_credits": 610}
# Spans of a clip (its own seconds) shown as the frame before them: a framing that is not for Everyone is held past, never patched.
HOLDS = {"06": ((3.25, 3.88),)}
KLING_RATE = .235
SEED_RATE = 11.7 * 1920 * 1080 * 24 / 1024 / 1e6  # dollars a second at 1080p


def load(module, name):
    spec = importlib.util.spec_from_file_location(name, str(ROOT / "tools" / module))
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def find(cid):
    for clip in data.CLIPS:
        if clip["id"] == cid or clip["id"].startswith(cid + "-"):
            return clip
    raise SystemExit("no such clip " + cid)


def cost(clip):
    if clip["model"] == "kling":
        return clip["secs"] * KLING_RATE
    if clip["model"] == "seedance":
        return clip["secs"] * SEED_RATE
    return 0.0


def spending():
    path = CLIPS / "spending.json"
    if path.exists():
        return json.loads(path.read_text(encoding="utf-8"))
    return {"budget": BUDGET, "calls": []}


def spend(entry):
    log = spending()
    log["calls"].append(entry)
    CLIPS.mkdir(parents=True, exist_ok=True)
    (CLIPS / "spending.json").write_text(json.dumps(log, indent=1, ensure_ascii=False), encoding="utf-8")


def left(who):
    log = spending()
    used = sum(c.get("price", 0) for c in log["calls"] if c.get("service") == who and c.get("state") == "made")
    return log["budget"][who if who != "higgsfield" else "higgsfield_credits"] - used


def plan():
    out = ["# Clips plan", "", "Every clip: 16:9, 1920x1080, a different scene, mostly combat. Voices (Inworld inworld-tts-2, reasoning on) in English, Japanese, Korean, Chinese; "
           "captions in the same four. Lip sync: the voices are fitted to the picture's own mouth movements by shifting and stretching the audio; no frame is touched.", "",
           "Budget: fal $25.96 (Kling 4.0, $0.235/s at 1080p), BytePlus $20.60 (Seedance 2.5, about $0.57/s at 1080p), Higgsfield 610 credits (MAGI-2, about 2.5 credits/s).", ""]
    totals = {}
    for clip in data.CLIPS:
        totals.setdefault(clip["model"], [0, 0.0])
        totals[clip["model"]][0] += clip["secs"]
        totals[clip["model"]][1] += cost(clip)
    gens = {c["gen"] for c in data.CLIPS if c.get("gen")}
    out.append("| model | clips | seconds | estimated cost |")
    out.append("|---|---|---|---|")
    for model, (secs, price) in totals.items():
        n = sum(1 for c in data.CLIPS if c["model"] == model)
        extra = f" ({len(gens)} generations of 30 s = {len(gens) * 75} credits)" if model == "magi" else f"${price:.2f}"
        out.append(f"| {model} | {n} | {secs} | {extra} |")
    out.append("")
    out.append(f"Total {len(data.CLIPS)} clips, {sum(c['secs'] for c in data.CLIPS)} s. Held back for remakes and extra clips: about $3.5 of fal, $2.4 of BytePlus, 160 credits of Higgsfield.")
    out.append("")
    for clip in data.CLIPS:
        how = f"{clip['model']}, {clip['secs']} s" + (f", cut {clip['cut'][0]}-{clip['cut'][1]} s of generation {clip['gen']}" if clip.get("gen") else "")
        out.append(f"## {clip['id']}  ({how}; about ${cost(clip):.2f})" if clip["model"] != "magi" else f"## {clip['id']}  ({how})")
        out.append(f"- Plays: {clip['place']}")
        out.append(f"- Cast: " + ", ".join(data.SHEETS[c][1] for c in clip["cast"] if c in data.SHEETS) + "  (voices: " + ", ".join(sorted({l[0] for l in clip["lines"]})) + ")")
        for i, shot in enumerate(clip["shots"], 1):
            out.append(f"- Shot {i}: {shot}")
        for who, at, en, ja, ko, zh in clip["lines"]:
            out.append(f"- {who} at {at:g} s: EN \"{en}\" / JA \"{ja}\" / KO \"{ko}\" / ZH \"{zh}\"")
        out.append("")
    out.append("## Generations on MAGI-2 (each is 30 s, cut into three clips)")
    for g, text in data.GENS.items():
        out.append(f"- {g}: {text}")
    CLIPS.mkdir(parents=True, exist_ok=True)
    (CLIPS / "plan.md").write_text("\n".join(out) + "\n", encoding="utf-8")
    print("wrote", CLIPS / "plan.md")


def refs(clip):
    return [DIST / data.SHEETS[c][0] for c in clip["cast"] if c in data.SHEETS]


def prompt_for(clip, kling):
    names = []
    for i, c in enumerate(clip["cast"] if kling is not None else clip["cast"]):
        if c in data.SHEETS:
            names.append(f"{'@Image' if True else ''}{i + 1} is the reference sheet of {data.SHEETS[c][1]}: she looks exactly as on it from every side.")
    said = []
    for who, at, en, *_ in clip["lines"]:
        said.append(f'At about {at:g} s {data.SHEETS.get(who, ("", "she"))[1].split(" (")[0]} says in English: "{en.replace(chr(0x2661), "").strip()}", her mouth moving with the words.')
    return " ".join(names) + " " + clip["scene"] + " " + " ".join(said) + f" Sound effects: {clip['sfx']}. " + data.STYLE


def gen(cid, go):
    clip = find(cid)
    if clip["model"] not in ("kling", "seedance"):
        raise SystemExit("MAGI-2 clips are made in the Higgsfield page, not here.")
    mc = load("make_cutscene.py", "mc")
    folder = CLIPS / clip["id"]
    folder.mkdir(parents=True, exist_ok=True)
    text = prompt_for(clip, clip["model"] == "kling")
    (folder / "prompt.txt").write_text(text, encoding="utf-8")
    price = cost(clip)
    who = "fal" if clip["model"] == "kling" else "byteplus"
    print(f"{clip['id']}: {clip['model']} {clip['secs']} s, estimated ${price:.2f}; left of {who}: ${left(who):.2f}; prompt {len(text)} letters")
    if not go:
        print(text)
        return
    if price > left(who) + 1e-6:
        raise SystemExit("not enough budget left")
    if clip["model"] == "kling":
        asked = {"prompt": text, "image_urls": ["file:" + str(p) for p in refs(clip)], "duration": clip["secs"], "resolution": "1080p", "aspect_ratio": "16:9"}
        body = {"model": "kling/v4/reference-to-video", "asked": asked, "resolution": "1080p", "duration": clip["secs"]}
        # Kling's first words: the references go in as pictures; @Image1 etc. name them.
        target, _ = mc.make(body, folder, clip["id"] + "-kling", None)
        spend({"when": time.strftime("%Y-%m-%d %H:%M:%S"), "service": "fal", "what": clip["id"], "seconds": clip["secs"], "price": round(price, 2), "state": "made", "clip": target.name})
    else:
        # Seedance takes the English recording as a reference for the lips: record it first.
        wav = folder / "voice" / "en.wav"
        if not wav.exists():
            tts(cid)
        shot = {"model": "dreamina-seedance-2-5-260628", "prompt": "prompt.txt", "images": [{"file": str(p), "role": "reference_image"} for p in refs(clip)],
                "audio": [{"file": "voice/en.wav"}], "resolution": "1080p", "ratio": "16:9", "duration": clip["secs"], "generate_audio": True}
        text += " The voices are the recording @Audio 1: use its voice and timing as they are and move the lips in precise sync with it; no other voices."
        (folder / "prompt.txt").write_text(text, encoding="utf-8")
        body, _ = mc.request_of(folder, {**shot, "images": [{"file": str(p), "role": "reference_image"} for p in refs(clip)]})
        target, used = mc.make(body, folder, clip["id"] + "-seedance", None)
        spend({"when": time.strftime("%Y-%m-%d %H:%M:%S"), "service": "byteplus", "what": clip["id"], "seconds": clip["secs"], "price": round(used or price, 2), "state": "made", "clip": target.name})


def tts(cid):
    clip = find(cid)
    voice = load("make_run_voice.py", "voice")
    key = voice.secret("INWORLD_API_KEY")
    if not key:
        raise SystemExit("no voice key")
    import numpy
    import soundfile

    folder = CLIPS / clip["id"] / "voice"
    folder.mkdir(parents=True, exist_ok=True)
    for i, (who, at, *texts) in enumerate(clip["lines"]):
        vk = "f" if who == "F" else who
        for lang, text in zip(LANGS, texts):
            target = folder / f"{lang}-{i}.wav"
            if target.exists():
                continue
            scene = voice.MANNER.get(vk, "") + "She is in the middle of a fast fight in a platformer; " + data.SHEETS.get(who, ("", who))[1].split(" (")[0] + " says this line, in character, with effort and attitude. "
            if who == "H":
                scene = "She is talking to the player who controls her in a platform game, whom she calls Sloppy. She is in an action scene, smug and teasing, a little breathless. "
            for attempt in range(4):
                try:
                    sound, _ = voice.speak(key, text, vk if vk in voice.VOICES else "H", scene, None if lang == "en" else lang)
                    sound = voice.finished(sound, voice.VOICES[vk][2])
                    soundfile.write(str(target), sound, voice.RATE_OUT)
                    break
                except Exception as error:  # noqa: BLE001
                    print("retry", cid, lang, i, error)
                    time.sleep(3 + attempt * 4)
    # The English track in one piece, lines at their seconds, for Seedance's reference.
    rate = voice.RATE_OUT
    track = numpy.zeros(int(clip["secs"] * rate) + rate)
    for i, (who, at, *_t) in enumerate(clip["lines"]):
        f = folder / f"en-{i}.wav"
        if f.exists():
            s, _ = soundfile.read(str(f))
            a = int(at * rate)
            track[a:a + len(s)] += s[: len(track) - a]
    soundfile.write(str(folder / "en.wav"), track[: int(clip["secs"] * rate)], rate)
    print("recorded", clip["id"])


def source_of(clip):
    folder = CLIPS / clip["id"]
    found = sorted([p for p in folder.glob("*.mp4") if not p.stem.endswith("-silent")], key=lambda p: p.stat().st_mtime)
    if not found:
        raise SystemExit("no picture yet for " + clip["id"])
    return found[-1]


def fit(cid, review=True):
    import numpy
    import soundfile
    import clips_fit as cf

    clip = find(cid)
    folder = CLIPS / clip["id"]
    source = source_of(clip)
    orig = cf.read_audio(source)
    bedfile = folder / f"{clip['id']}-bed.wav"
    stripped = bedfile.exists()
    bed = cf.read_audio(bedfile) if stripped else orig
    total = clip["secs"]
    voices = {lang: [] for lang in LANGS}
    for i, line in enumerate(clip["lines"]):
        for lang in LANGS:
            sound, rate = soundfile.read(str(folder / "voice" / f"{lang}-{i}.wav"))
            import scipy.signal
            voices[lang].append(scipy.signal.resample_poly(sound, cf.RATE, rate).astype("float32"))
    nominal = [(line[1], line[1] + len(voices["en"][i]) / cf.RATE) for i, line in enumerate(clip["lines"])]
    if clip["model"] == "seedance":
        windows = nominal
        how = "the recording's own places (the lips follow the recording)"
    else:
        spans = cf.speech_spans(orig)
        windows = cf.nearest_spans(spans, nominal)
        how = f"{len(spans)} speech span(s) found in the picture's own sound: " + ", ".join(f"{a:.2f}-{b:.2f}" for a, b in spans)
    # Windows must not run past the clip.
    windows = [(a, min(b, total - .05)) for a, b in windows]
    print(clip["id"], how, "-> windows", [(round(a, 2), round(b, 2)) for a, b in windows])
    if stripped:
        import scipy.signal as sg
        sos = sg.butter(4, [300, 3400], "bandpass", fs=cf.RATE, output="sos")
        def band(x, a, b):
            seg = sg.sosfilt(sos, x[int(a * cf.RATE):int(b * cf.RATE)])
            return 10 * numpy.log10(float((seg ** 2).mean()) + 1e-12) if len(seg) else -120.0
        rows = [(band(orig, a, b), band(bed, a, b)) for a, b in windows]
        print("   speech-band energy in the line windows, original -> bed:", ", ".join(f"{x:.0f} -> {y:.0f} dB" for x, y in rows))
    tracks = {}
    for lang in LANGS:
        track, placed = cf.fit_lines(voices[lang], windows, total, bed, mute=not stripped)
        tracks[lang] = track
        soundfile.write(str(folder / f"{clip['id']}-{lang}.wav"), track, cf.RATE)
        rows = [f"{a:.2f}\t{b:.2f}\t{line[2 + LANGS.index(lang)]}" for (a, b), line in zip(placed, clip["lines"])]
        (folder / f"{clip['id']}.{lang}.txt").write_text("\n".join(rows) + "\n", encoding="utf-8")
    if review:
        out = CLIPS / "review"
        out.mkdir(parents=True, exist_ok=True)
        rows = [(float(r.split("\t")[0]), float(r.split("\t")[1]) + .3, r.split("\t")[2].replace("\u2661", "")) for r in (folder / f"{clip['id']}.en.txt").read_text(encoding="utf-8").strip().split("\n")]
        target = out / f"{clip['id']}-{clip['model']}{'-v2' if stripped else ''}.mp4"
        size = source.stat().st_size
        cf.write_video(source, target, tracks["en"], captions=rows, holds=HOLDS.get(clip["id"][:2], ()), bitrate=min(14_000_000, int(26e6 * 8 / max(total, 4) * .9)))
        print("review copy", target, f"{target.stat().st_size / 1e6:.1f} MB")


def cut(cid):
    """A clip of a MAGI-2 generation: the generation's seconds from..to, re-encoded at the quality it came in."""
    import clips_fit as cf
    import av

    clip = find(cid)
    gen = CLIPS / "gens" / f"{clip['gen']}.mp4"
    a, b = clip["cut"]
    folder = CLIPS / clip["id"]
    folder.mkdir(parents=True, exist_ok=True)
    box = av.open(str(gen))
    video = box.streams.video[0]
    out = av.open(str(folder / f"{clip['id']}-magi.mp4"), "w", options={"movflags": "+faststart"})
    vs = out.add_stream("libx264", rate=video.average_rate)
    vs.width, vs.height = 1920, 1080
    vs.pix_fmt = "yuv420p"
    vs.options = {"crf": "17", "preset": "slow"}
    a_s = out.add_stream("aac", rate=cf.RATE, layout="mono")
    for frame in box.decode(video):
        t = float(frame.pts * frame.time_base)
        if a <= t < b:
            image = frame.to_image().resize((1920, 1080))
            for p in vs.encode(av.VideoFrame.from_image(image)):
                out.mux(p)
    for p in vs.encode():
        out.mux(p)
    sound = cf.read_audio(gen)[int(a * cf.RATE):int(b * cf.RATE)]
    for i in range(0, len(sound), 1024):
        fr = av.AudioFrame.from_ndarray(sound[i:i + 1024].reshape(1, -1), format="flt", layout="mono")
        fr.sample_rate = cf.RATE
        fr.pts = i
        for p in a_s.encode(fr):
            out.mux(p)
    for p in a_s.encode():
        out.mux(p)
    out.close()
    print("cut", clip["id"])

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("what")
    parser.add_argument("id", nargs="?")
    parser.add_argument("--go", action="store_true")
    parser.add_argument("--source")
    args = parser.parse_args()
    if args.what == "plan":
        plan()
    elif args.what == "gen":
        gen(args.id, args.go)
    elif args.what == "tts":
        tts(args.id)
    elif args.what == "show":
        print(json.dumps(spending(), indent=1))
    elif args.what == "fit":
        fit(args.id)
    elif args.what == "cut":
        cut(args.id)


if __name__ == "__main__":
    main()








