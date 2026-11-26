"""
Records the fighters' lines for Page brawl.

    python tools/make_fight_voice.py [--only NAME ...] [--who LETTERS] [--again]

Reads every line out of FightCast.java, a name and what is said, and has each spoken by
Inworld's hosted text to speech, the service the Halgakos AI project uses. The first letter
of a line's name says whose it is: Halgakos keeps the voice she has in that project, "Hana",
and the others have the community anime voices they have in Plus run, each raised a little
so that all of them sit near her register. The rest of the name says what kind of line it
is, and from the two the service is told the scene, with its Reasoning switched on, so that
a shout is shouted and a sob is sobbed.

Nothing in that project is run or changed; only its key is read, from its .env, by name, and
it is never printed. Each recording is trimmed, levelled, brought down to 24 kHz and saved
as res/raw/fight_v_NAME.ogg. A line that already has its recording is skipped unless --again.

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
STORY = ROOT / "app" / "src" / "main" / "java" / "com" / "coral" / "calculator" / "FightCast.java"
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
PROJECT = pathlib.Path(r"C:\Users\ADMIN\Dev\Halgakos-AI")
URL = "https://api.inworld.ai/tts/v1/voice:stream"
MODEL = "inworld-tts-2"
# Whose voice a line is in, by the first letter of its name: the voice, how fast she speaks, and how much
# higher the recording is made afterwards. The same voices as tools/make_run_voice.py.
VOICES = {
    "h": ("community-5fwhmdpiq45g", 1.0, 1.0),
    "c": ("community-iyfnceehtmbk", .95, 1.06),
    "t": ("community-pwotyqkmtlsi", .92, 1.1),
    "r": ("community-ae0qjkqan5hx", .96, 1.05),
    "i": ("community-blcuaurhzmvi", .95, 1.06),
}
# Who each of them is, for the voice to know.
WHO = {
    "h": "Halgakos, a smug, teasing translator girl with a cheeky sing-song voice, in a fighting game. She talks fast.",
    "c": "Chikuwa, a sunny, bubbly baker girl in a fighting game, who thinks of everything in bread. A high, bright, cute anime voice.",
    "t": "Tsubakieh, a cheerful, unbothered girl with a baseball bat who calls everything normal, in a fighting game. A light, cute, deadpan anime voice.",
    "r": "Rune, a shy, lonely librarian girl who is always crying, in a fighting game. Stammering, sniffling, fragile, tearful; her devotion can turn sweetly and quietly frightening.",
    "i": "Illya, a lovely magical-girl idol in a fighting game. A sweet, bright, high, sparkling anime voice; polite and sincere, never sultry.",
}
# What each kind of line is said in the middle of.
SCENES = {
    "pick": "The player has just chosen her, and she says her catchphrase: confident, in character.",
    "intro": "Just before a fight, she speaks to her opponent, in character.",
    "reply": "Just before a fight, she answers what her opponent said, in character.",
    "special": "She calls out her special move as she uses it: short, sharp, energetic.",
    "ult": "She unleashes her ultimate move at full power: big, dramatic, triumphant.",
    "hit": "She has just been hit hard in a fight: a cry of pain, startled, hurt.",
    "win": "She has just won the fight: pleased, in character.",
    "lose": "She has just lost the fight and is beaten: defeated, hurt, close to tears.",
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


def spoken(text):
    """A line as it is to be said: without the heart, which is not a word."""
    return text.replace("♡", "").replace("~", "").strip()


def scene(name):
    """The scene a line is said in: who says it, and what kind of line it is."""
    kind = re.sub(r"\d+$", "", name[1:])
    return WHO[name[0]] + " " + SCENES[kind]


def speak(key, name, text):
    voice, pace, _ = VOICES[name[0]]
    body = {"text": spoken(text), "voiceId": voice, "modelId": MODEL, "deliveryMode": "CREATIVE",
            "audioConfig": {"audioEncoding": "PCM", "sampleRateHertz": RATE_IN, "speakingRate": pace},
            "synthesisContext": {"reasoning": "REASONING_ON", "reasoningContext": scene(name)[:600]}}
    headers = {"Authorization": f"Basic {key}", "Content-Type": "application/json", "Accept": "application/json"}
    pcm, chosen = b"", ""
    with requests.post(URL, json=body, headers=headers, stream=True, timeout=(15, 120)) as reply:
        if reply.status_code != 200:
            raise RuntimeError(f"HTTP {reply.status_code}")
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
    # Played back faster, a voice is higher: the recording is squeezed by as much as it is to be raised.
    sound = scipy.signal.resample_poly(sound, round(RATE_OUT / pitch), RATE_IN)
    sound = sound / max(1e-6, numpy.max(numpy.abs(sound))) * .92
    fade = min(len(sound), RATE_OUT // 100)
    sound[:fade] *= numpy.linspace(0, 1, fade)
    sound[-fade:] *= numpy.linspace(1, 0, fade)
    return sound.astype(numpy.float32)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--only", nargs="*")
    parser.add_argument("--who", default="", help="the letters of the fighters whose lines to record: h c t r i")
    parser.add_argument("--again", action="store_true")
    args = parser.parse_args()
    lines = re.findall(r'\{"([a-z0-9]+)",\s*"([^"]+)"\}', STORY.read_text(encoding="utf-8"))
    names = [name for name, _ in lines]
    if len(set(names)) != len(names):
        raise SystemExit("two lines share a name")
    key = secret("INWORLD_API_KEY")
    if not key:
        raise SystemExit("no key for the voice")
    RAW.mkdir(parents=True, exist_ok=True)
    made = failed = 0
    for name, text in lines:
        target = RAW / f"fight_v_{name}.ogg"
        if (args.only and name not in args.only) or (args.who and name[0] not in args.who) or (target.exists() and not args.again):
            continue
        error = None
        for attempt in range(4):
            try:
                sound, chosen = speak(key, name, text)
                sound = finished(sound, VOICES[name[0]][2])
                error = None
                break
            except Exception as problem:  # noqa: BLE001 - one line failing should not stop the rest
                error = problem
                time.sleep(3 + attempt * 4)
        if error is not None:
            failed += 1
            print("FAILED", name, type(error).__name__, str(error)[:160], flush=True)
            continue
        soundfile.write(target, sound, RATE_OUT, format="OGG", subtype="VORBIS")
        made += 1
        print("ok", name, f"{len(sound) / RATE_OUT:.1f}s", "|", (chosen or "no direction chosen")[:100].encode("ascii", "replace").decode(), flush=True)
    print("lines", len(lines), "made", made, "failed", failed, "have", sum(1 for name in names if (RAW / f"fight_v_{name}.ogg").exists()))
    if failed:
        sys.exit(1)


if __name__ == "__main__":
    main()
