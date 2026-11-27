"""
Tulle's voice, and every line said at the vanity boutique (VanityStory.java).

    python tools/make_vanity_voice.py design             candidate voices designed from a description, with their pitch
    python tools/make_vanity_voice.py publish VOICE_ID   keeps the chosen designed voice on the account, as "Tulle"
    python tools/make_vanity_voice.py record [--again]   records every line as res/raw/run_v_NAME.ogg

It uses make_run_voice.py's way of speaking (scene told to the voice, reasoning on, trimmed and levelled). The key is read by
name from the Halgakos AI project's .env and never printed; nothing in that project is changed.
"""
import argparse, base64, importlib.util, json, pathlib, re, sys, time

import numpy
import requests
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
spec = importlib.util.spec_from_file_location("voice", ROOT / "tools" / "make_run_voice.py")
voice = importlib.util.module_from_spec(spec)
spec.loader.exec_module(voice)
STORY = ROOT / "app/src/main/java/com/coral/calculator/VanityStory.java"
STATE = pathlib.Path(sys.argv[0]).resolve().parent / "vanity_voice.json"
SCRATCH = pathlib.Path(r"C:\Users\ADMIN\AppData\Local\Temp\claude\b5219ac421f6d431\f0915c1e-5858-4761-9ef4-023c764bbc22\scratchpad\vanity\voice")

MANNER_G = ("Tulle is a young seamstress with a warm, bubbly, slightly fussy anime voice: friendly and sweet, a little breathless when excited, "
            "proud of her craft, in clear American English. ")
DESIGNS = [
    ("warm seamstress", "A warm, sweet young woman's voice, like an anime voice actress playing a friendly, slightly fussy seamstress who runs a dress shop. "
                        "Bubbly and kind, a little breathless when excited, polite and gentle, mid-high pitch. Speaks clear American English with no "
                        "foreign accent. Clean close-mic studio recording."),
    ("gentle shopkeeper", "A gentle, cheerful young woman's voice, like an anime voice actress playing a kind boutique owner: soft, smiling, "
                          "sing-song, a touch of shyness, never shrill. Speaks clear American English with no foreign accent. Clean close-mic studio recording."),
]
PREVIEW = "Welcome to Tulle and Thread! Everything here is cut to fit, and everything fits. Hold still, I only need to measure your wings."


def pitch(sound, rate):
    """The median pitch of the voiced stretches of a recording, in Hz, by autocorrelation."""
    step, size = rate // 50, rate // 20
    lo, hi = rate // 450, rate // 120
    found = []
    for start in range(0, len(sound) - size, step):
        frame = sound[start:start + size]
        if numpy.abs(frame).max() < .05:
            continue
        frame = frame - frame.mean()
        corr = numpy.correlate(frame, frame, "full")[size - 1:]
        if corr[0] <= 0:
            continue
        peak = lo + int(numpy.argmax(corr[lo:hi]))
        if corr[peak] / corr[0] > .5:
            found.append(rate / peak)
    return float(numpy.median(found)) if found else 0.0


def design():
    key = voice.secret("INWORLD_API_KEY")
    http = requests.Session()
    http.headers.update({"Authorization": f"Basic {key}", "Content-Type": "application/json"})
    SCRATCH.mkdir(parents=True, exist_ok=True)
    index, number = [], 0
    for label, prompt in DESIGNS:
        reply = http.post("https://api.inworld.ai/voices/v1/voices:design", timeout=180,
                          json={"designPrompt": prompt, "previewText": PREVIEW, "voiceDesignConfig": {"numberOfSamples": 3}, "languageCode": "en-US"})
        if reply.status_code != 200:
            print("design", label, reply.status_code, reply.text[:160].encode("ascii", "replace").decode())
            continue
        for made in reply.json().get("previewVoices", []):
            number += 1
            audio = base64.b64decode(made.get("previewAudio") or "")
            name = f"Tulle-{number:02d}-{label.replace(' ', '-')}" + (".wav" if audio[:4] == b"RIFF" else ".mp3")
            (SCRATCH / name).write_bytes(audio)
            hz = 0.0
            if name.endswith(".wav"):
                data, rate = soundfile.read(SCRATCH / name)
                hz = pitch(data if data.ndim == 1 else data[:, 0], rate)
            index.append({"file": name, "voiceId": made.get("voiceId"), "label": label, "hz": round(hz)})
            print(name, round(hz), "Hz")
    (SCRATCH / "candidates.json").write_text(json.dumps(index, indent=1), encoding="utf-8")


def publish(voice_id):
    key = voice.secret("INWORLD_API_KEY")
    reply = requests.post(f"https://api.inworld.ai/voices/v1/voices/{voice_id}:publish", timeout=120,
                          headers={"Authorization": f"Basic {key}", "Content-Type": "application/json"},
                          json={"displayName": "Tulle", "description": "Tulle, the seamstress who keeps the vanity boutique in Halgakos Calculator's Plus run",
                                "tags": ["seamstress", "shopkeeper", "anime"]})
    print(reply.status_code, reply.text[:200].encode("ascii", "replace").decode())
    if reply.status_code == 200:
        STATE.write_text(json.dumps({"voiceId": reply.json().get("voiceId"), "displayName": "Tulle"}, indent=1), encoding="utf-8")
        print("kept", reply.json().get("voiceId"))


def lines():
    text = STORY.read_text(encoding="utf-8")
    shop = re.search(r'SHOP = "([^"]+)"', text).group(1)
    found = re.findall(r'\{"([a-z0-9]+)",\s*"([^"]+)",\s*"([A-Za-z0-9])",\s*(SHOP \+ )?"([^"]+)"\}', text)
    return [(name, words, who, (shop if with_shop else "") + scene) for name, words, who, with_shop, scene in found]


def record(again):
    voice_id = json.loads(STATE.read_text(encoding="utf-8"))["voiceId"]
    voice.VOICES["g"] = (voice_id, 1.0, 1.0)
    voice.MANNER["g"] = MANNER_G
    key = voice.secret("INWORLD_API_KEY")
    made = failed = 0
    for name, text, who, scene in lines():
        target = voice.RAW / f"run_v_{name}.ogg"
        if target.exists() and not again:
            continue
        scene = (voice.MANNER.get(who, "") + (voice.re.search(r'PLAYER = "([^"]+)"', voice.STORY.read_text(encoding="utf-8")).group(1) if who == "H" else "") + scene)
        for attempt in range(4):
            try:
                sound, chosen = voice.speak(key, text, who, scene)
                sound = voice.finished(sound, voice.VOICES[who][2])
                break
            except Exception as problem:  # noqa: BLE001
                error = problem
                time.sleep(3 + attempt * 4)
        else:
            failed += 1
            print("FAILED", name, str(error)[:160], flush=True)
            continue
        soundfile.write(target, sound, voice.RATE_OUT, format="OGG", subtype="VORBIS")
        made += 1
        print("ok", name, who, f"{len(sound) / voice.RATE_OUT:.1f}s", flush=True)
    print("made", made, "failed", failed)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("what", choices=["design", "publish", "record"])
    parser.add_argument("voice_id", nargs="?")
    parser.add_argument("--again", action="store_true")
    args = parser.parse_args()
    if args.what == "design":
        design()
    elif args.what == "publish":
        publish(args.voice_id)
    else:
        record(args.again)
