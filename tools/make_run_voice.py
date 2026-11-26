"""
Records everything that is said in Plus run.

    python tools/make_run_voice.py [--only NAME ...] [--again] [--prune]

Reads every line out of RunStory.java: its name, its words, who says it and the scene it is
said in. Each is spoken by Inworld's hosted text to speech, the service the Halgakos AI
project uses, in the voice of whoever says it: Halgakos in her own voice from that project,
"Hana", and her friends in anime voices picked for them. The scene is passed along with
Inworld's Reasoning switched on, as it is in that project: the service reads the scene and
the words and chooses how the line should be said, so a yelp sounds like a yelp and a sulk
like a sulk. What it chose is printed beside each line.

Nothing in that project is run or changed; only its key is read, from its .env, by name, and
it is never printed. Each recording is trimmed, levelled, brought down to 24 kHz and saved as
res/raw/run_v_NAME.ogg. A line that already has its recording is skipped unless --again;
--prune removes recordings of lines that are no longer in the story.

Needs requests, numpy, scipy and soundfile.
"""

import argparse
import base64
import json
import os
import pathlib
import re
import sys
import time

import numpy
import requests
import scipy.signal
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
STORY = ROOT / "app" / "src" / "main" / "java" / "com" / "coral" / "calculator" / "RunStory.java"
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
PROJECT = pathlib.Path(r"C:\Users\ADMIN\Dev\Halgakos-AI")
URL = "https://api.inworld.ai/tts/v1/voice:stream"
MODEL = "inworld-tts-2"
# Who speaks in which voice, how fast, and how much lower or higher the recording is made afterwards.
# The Mawang is Halgakos herself, sixty years on: her voice, slower and lower.
VOICES = {
    "H": ("community-5fwhmdpiq45g", 1.0, 1.0),
    "C": ("community-iyfnceehtmbk", .95, 1.06),
    "R": ("community-ae0qjkqan5hx", .96, 1.05),
    "I": ("community-blcuaurhzmvi", .95, 1.06),
    "T": ("community-pwotyqkmtlsi", .92, 1.1),
    "M": ("community-5fwhmdpiq45g", 1.0, .84),
    # The second volume. Gokhae, who is up to no good, speaks lower than the rest on purpose. The twins share
    # a voice: Miri quick and bright, Gari slow and hushed.
    "G": ("community-mzyrf5zr14rb", .95, 1.06),
    "J": ("community-2e7y7ype1d0u", 1.1, 1.0),
    "K": ("community-2e7y7ype1d0u", .88, .97),
    "Y": ("community-qdnchfkneudi", .82, 1.12),
    "N": ("community-qj9kc2jtun4a", .95, 1.15),
}
# Who each of the others is, said before the scene of every line of hers, so that she sounds like herself whatever the moment.
MANNER = {
    "C": "Chikuwa is a sunny, bubbly girl with a high, bright, cute anime voice. ",
    "R": "Rune is a shy girl who is always crying: a high, fragile, tearful anime voice, sniffling and sad. ",
    "I": "Illya is a cute magical-girl idol with a high, bright, sparkling anime voice: cheerful and sweet, never dark, sultry or mature. ",
    "T": "Tsubakieh is a cheerful, unbothered girl with a light, cute anime voice, perfectly calm and deadpan. ",
    "M": "The Mawang is Halgakos herself from another volume, after sixty years alone: the same girl's voice, but tired, low and gloomy. ",
    "G": "Gokhae is a sly young court interpreter with a smooth, cool anime voice, smug and precise, never old or mature. ",
    "J": "Miri is a bright, gossipy newspaper girl with a high, quick, chirpy anime voice. ",
    "K": "Gari is Miri's shy twin sister, with the same high anime voice, hushed and timid, almost a whisper. ",
    "Y": "Yeobaek is a sleepy young swordswoman with a soft, high, drowsy anime voice, drawling and yawning. ",
    "N": "Nuri is a young queen with a clear, high, earnest anime voice, formal and brave. ",
}
RATE_IN, RATE_OUT = 48000, 24000


def secret(name):
    """The key under a name: from the environment, or from the project's .env. Never printed."""
    value = os.environ.get(name, "").strip()
    if value:
        return value
    env = PROJECT / ".env"
    if env.exists():
        for line in env.read_text(encoding="utf-8").splitlines():
            key, _, found = line.partition("=")
            if key.strip() == name:
                return found.strip().strip("\"'")
    return ""


def story():
    """Every line of the story: its name, words, speaker and scene."""
    text = STORY.read_text(encoding="utf-8")
    player = re.search(r'PLAYER = "([^"]+)"', text).group(1)
    fast = re.search(r'FAST = "([^"]+)"', text).group(1)
    found = re.findall(r'\{"([a-z0-9]+)",\s*"([^"]+)",\s*"([A-Z])",\s*(PLAYER \+ )?"([^"]+)"( \+ FAST)?\}', text)
    return [(name, words, who, MANNER.get(who, "") + (player if with_player else "") + scene + (fast if quick else ""))
            for name, words, who, with_player, scene, quick in found]


def spoken(text):
    """A line as it is to be said: without the heart, which is not a word."""
    return text.replace("♡", "").strip()


def speak(key, text, who, scene):
    voice, rate, _ = VOICES[who]
    body = {"text": spoken(text), "voiceId": voice, "modelId": MODEL, "deliveryMode": "CREATIVE",
            "audioConfig": {"audioEncoding": "PCM", "sampleRateHertz": RATE_IN, "speakingRate": rate},
            "synthesisContext": {"reasoning": "REASONING_ON", "reasoningContext": scene[:600]}}
    headers = {"Authorization": f"Basic {key}", "Content-Type": "application/json", "Accept": "application/json"}
    pcm, chosen = b"", ""
    with requests.post(URL, json=body, headers=headers, stream=True, timeout=(15, 120)) as reply:
        if reply.status_code != 200:
            raise RuntimeError(f"HTTP {reply.status_code} {reply.text[:160]}")
        for line in reply.iter_lines():
            if not line.strip():
                continue
            message = json.loads(line)
            if "error" in message:
                raise RuntimeError(str(message["error"])[:200])
            result = message.get("result") or {}
            chosen = result.get("reasoningInstruction") or chosen
            piece = base64.b64decode(result.get("audioContent") or "")
            # A piece may come wrapped as a little WAV file of its own: only the sound in it is kept.
            if piece[:4] == b"RIFF":
                piece = piece[44:]
            pcm += piece
    pcm = pcm[: len(pcm) - len(pcm) % 2]
    return numpy.frombuffer(pcm, dtype="<i2").astype(numpy.float32) / 32768, chosen


def finished(sound, pitch):
    """A recording with the silence taken off both ends, as loud as it will go, at the rate it is kept at."""
    loud = numpy.abs(sound) > .012
    if not loud.any():
        raise RuntimeError("silence")
    first, last = numpy.argmax(loud), len(loud) - numpy.argmax(loud[::-1])
    sound = sound[max(0, first - RATE_IN // 50): min(len(sound), last + RATE_IN // 8)]
    # Played back slower, a voice is lower: the recording is stretched by as much as it is to be lowered.
    sound = scipy.signal.resample_poly(sound, round(RATE_OUT / pitch), RATE_IN)
    sound = sound / max(1e-6, numpy.max(numpy.abs(sound))) * .92
    fade = min(len(sound), RATE_OUT // 100)
    sound[:fade] *= numpy.linspace(0, 1, fade)
    sound[-fade:] *= numpy.linspace(1, 0, fade)
    return sound.astype(numpy.float32)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--only", nargs="*")
    parser.add_argument("--again", action="store_true")
    parser.add_argument("--prune", action="store_true")
    args = parser.parse_args()
    lines = story()
    names = [name for name, _, _, _ in lines]
    if len(set(names)) != len(names):
        raise SystemExit("two lines share a name")
    if args.prune:
        for old in RAW.glob("run_v_*.ogg"):
            if old.stem[len("run_v_"):] not in names:
                old.unlink()
                print("removed", old.name)
    key = secret("INWORLD_API_KEY")
    if not key:
        raise SystemExit("no key for the voice")
    RAW.mkdir(parents=True, exist_ok=True)
    made = failed = 0
    for name, text, who, scene in lines:
        target = RAW / f"run_v_{name}.ogg"
        if (args.only and name not in args.only) or (target.exists() and not args.again):
            continue
        error = None
        for attempt in range(4):
            try:
                sound, chosen = speak(key, text, who, scene)
                sound = finished(sound, VOICES[who][2])
                error = None
                break
            except Exception as problem:  # noqa: BLE001 - one line failing should not stop the rest
                error = problem
                time.sleep(3 + attempt * 4)
        if error is not None:
            failed += 1
            print("FAILED", name, type(error).__name__, str(error)[:200], flush=True)
            continue
        soundfile.write(target, sound, RATE_OUT, format="OGG", subtype="VORBIS")
        made += 1
        print("ok", name, who, f"{len(sound) / RATE_OUT:.1f}s", "|", (chosen or "no direction chosen")[:110].encode("ascii", "replace").decode(), flush=True)
    print("lines", len(lines), "made", made, "failed", failed, "have", sum(1 for name in names if (RAW / f"run_v_{name}.ogg").exists()))
    if failed:
        sys.exit(1)


if __name__ == "__main__":
    main()
