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
MORE_STORIES = [ROOT / "app" / "src" / "main" / "java" / "com" / "coral" / "calculator" / "RunMerchantStory.java"]
PROJECT = pathlib.Path(r"C:\Users\ADMIN\Dev\Halgakos-AI")
URL = "https://api.inworld.ai/tts/v1/voice:stream"
MODEL = "inworld-tts-2"
# Who speaks in which voice, how fast, and how much lower or higher the recording is made afterwards.
# Her friends have community anime voices, each raised a little so that all of them sit near Halgakos's own
# register (hers is about 370 Hz; the voices they had first were as low as 260 Hz and sounded grown up).
# The Mawang is Halgakos herself, sixty years on: her voice, slower and lower.
VOICES = {
    "H": ("community-5fwhmdpiq45g", 1.0, 1.0),
    "C": ("community-iyfnceehtmbk", 1.0, 1.08),
    "R": ("upbeat-bee-0979__design-voice-02fb12a5", 1.05, 1.0),  # a voice designed for her as a timid crybaby, the user's pick of twenty-two
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
    # Those met in the villages: the same few voices, each said in her own manner, faster or slower, higher or lower.
    "S": ("upbeat-bee-0979__design-voice-9365ced4", 1.0, 1.0),  # a voice designed as a genki little sister, the user's pick of thirty
    "U": ("community-qdnchfkneudi", .98, 1.08),
    "B": ("Eileen", .95, 1.0),
    "O": ("community-mzyrf5zr14rb", 1.08, 1.14),
    "P": ("community-qj9kc2jtun4a", .9, 1.1),
    "E": ("community-mzyrf5zr14rb", .98, 1.08),
    "A": ("upbeat-bee-0979__design-voice-c4643f36", 1.0, 1.0),  # a voice designed as a squeaky kitten, the user's pick of thirty
    "W": ("community-qdnchfkneudi", .92, 1.0),
    "L": ("community-qj9kc2jtun4a", 1.1, 1.2),
    # Five more who wait at the ends of lands: the Guild Guard, Penchok-jipsa, Byeoru, Jupan and Sumbi.
    "D": ("upbeat-bee-0979__design-voice-2d592424", 1.0, 1.0),  # a voice designed as a low, firm lady knight, the user's pick of fourteen
    "F": ("Carla", 1.0, 1.06),  # a young woman's voice, crisp and dry: every character who speaks is a girl or a woman
    "Q": ("community-qj9kc2jtun4a", .88, 1.06),
    "V": ("community-mzyrf5zr14rb", 1.1, 1.12),
    "X": ("community-qdnchfkneudi", 1.12, 1.1),
    # The Brake Pads, who speak as one; the Straight-Ahead Party, for whom its swordswoman speaks; and the Old Book.
    "Z": ("Ashley", 1.0, .96),
    "a": ("community-qdnchfkneudi", 1.08, .98),
    "b": ("Ambrose", .9, 1.0),
    # Moru's two sisters, who keep the smithy with her: library voices of young women, each raised a little toward the others' register.
    "c": ("Zoe", 1.05, 1.08),
    "d": ("Camille", .95, 1.06),
    # Lucy, the Dragon of Lost Words.
    "e": ("upbeat-bee-0979__design-voice-ac8b681f", 1.0, 1.0),  # a voice designed as a bratty little sister ("Lucy 02"), the user's pick of twelve
    # Aelwyn, the elf merchant of the villages: a voice designed as a warm, quick, sly young travelling shopkeeper (kept in the Inworld account as "Aelwyn").
    "g": ("upbeat-bee-0979__design-voice-e234a3af", 1.0, 1.0),
    # The Mother Tongue.
    "f":("upbeat-bee-0979__design-voice-53058b3d", 1.0, 1.0),  # a voice designed as a cold, regal queen, the user's pick of seventeen
}
# Who each of the others is, said before the scene of every line of hers, so that she sounds like herself whatever the moment.
MANNER = {
    "C": "Chikuwa is a sunny, bubbly girl voiced like a Japanese anime voice actress playing the cheerful, energetic one: high, bright and cute, bouncy and sing-song, with big exaggerated reactions and drawn-out happy vowels. ",
    "R": "Rune is a little crybaby with a small, soft, breathy, childlike anime voice, wobbling with tears. She sobs, sniffles and hiccups through every line, whiny, with simple words. Soft and tearful, not shrill or squeaky; never calm, eloquent or grown-up. ",
    "I": "Illya is a cute magical-girl idol with a high, bright, sparkling anime voice: cheerful and sweet, never dark, sultry or mature. ",
    "T": "Tsubakieh is a cheerful, unbothered girl with a light, cute anime voice, perfectly calm and deadpan. ",
    "M": "The Mawang is Halgakos herself from another volume, after sixty years alone: the same girl's voice, but tired, low and gloomy. ",
    "G": "Gokhae is a sly young court interpreter with a smooth, cool anime voice, smug and precise, never old or mature. ",
    "J": "Miri is a bright, gossipy newspaper girl with a high, quick, chirpy anime voice. ",
    "K": "Gari is Miri's shy twin sister, with the same high anime voice, hushed and timid, almost a whisper. ",
    "Y": "Yeobaek is a sleepy young swordswoman with a soft, high, drowsy anime voice, drawling and yawning. ",
    "N": "Nuri is a young queen with a clear, high, earnest anime voice, formal and brave. ",
    "S": "Somi is a tiny, hyper little-sister of an adventurer, voiced like a Japanese anime voice actress playing the genki little girl: very high and squeaky-cute, loud with excitement, words tumbling out, giggling, in clear American English. ",
    "U": "Inju is the guild's receptionist: a young woman with a light, polite, pleasant anime voice. ",
    "B": "Boksun is a kind old baker granny with a warm, gentle, elderly voice. ",
    "O": "Ttosun is a lively, sporty guild girl with a high, loud, cheerful anime voice. ",
    "P": "Galpi is a quiet young witch with a soft, high, dreamy anime voice. ",
    "E": "Yeseo is the careful young leader of an adventuring party, with a clear, high, earnest anime voice. ",
    "A": "Nabi is a very small, squeaky cat girl, voiced like a Japanese anime voice actress playing a kitten: extremely high and cute, bouncy and impatient, mewing 'nya' between words and bursting into giggles, in clear American English. ",
    "W": "Moru is a short-spoken smith girl with a flat, blunt but young and light anime voice. ",
    "L": "Altteul is a thrifty young shopkeeper with a high, brisk, bright anime voice, always selling. ",
    "D": "The Guild Guard is a lady knight in full armour who guards the guild's door and takes its entrance exam very seriously: a low, firm, dutiful voice, stern and formal, barking orders with total conviction, in clear American English. ",
    "F": "Penchok-jipsa is a living fountain pen who is a prim lady butler: a young woman's voice, light, dry, crisp and impeccably polite, unhurried, never old, gruff or deep, in clear American English. ",
    "Q": "Byeoru is a quiet young court scribe with a soft, high, flat anime voice, precise and unhurried. ",
    "V": "Jupan is a sly young merchant girl with a high, quick, bright anime voice, always bargaining. ",
    "X": "Sumbi is a cheerful diver girl with a high, loud, sunny anime voice, daring and out of breath. ",
    "Z": "The Brake Pads are two hard-working little brake pads who speak as one: a tomboyish, gruff but young and cheerful anime voice. ",
    "a": "The leader of the Straight-Ahead Party is a brash red-haired swordswoman with a loud, proud, hot-blooded anime voice. ",
    "b": "The Old Book is a very old, sleepy, bearded book: a slow, warm, grumbling grandfather's voice. ",
    "c": "Pulmu is a young dwarf armourer, Moru's sister: a loud, sunny, warm young woman's voice, laughing easily, big-hearted, in clear American English. ",
    "e": "Lucy is a small dragon child, a bratty little-sister type, voiced like a Japanese anime voice actress playing a cheeky, spoiled little girl: high, loud and sing-song, taunting, quick to pout and quick to cry, in clear American English. Sounds written in her lines are performed, never read out as words: 'Bleh!' is her sticking her tongue out with a short, rude raspberry; 'Ow!' is a real yelp of pain; 'Hmph!' is a huffy snort; 'Nyeh-nyeh' is a sing-song taunt. ",
    "f": "The Mother Tongue is a vast, ancient elder dragon and a mother: a grand, cold, regal woman's voice, low and unhurried, perfectly composed, certain that she knows best, never shouting, in clear English. ",
    "g": "Aelwyn is a young elf merchant who travels every land with her wagon of curios: a warm, bright, quick, slightly husky voice with a smile in it, playful and sly when she bargains, never shrill, in clear American English. ",
    "d": "Sutdol is a young dwarf jeweller-smith, Moru's sister: a calm, level, exact young woman's voice, quiet and dry, never old or deep, in clear American English. ",
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
    found = re.findall(r'\{"([a-z0-9]+)",\s*"([^"]+)",\s*"([A-Za-z0-9])",\s*(PLAYER \+ )?"([^"]+)"( \+ FAST)?\}', text)
    # (The merchants' lines, in the same form, are in a file of their own.)
    for more in MORE_STORIES:
        found += re.findall(r'\{"([a-z0-9]+)",\s*"([^"]+)",\s*"([A-Za-z0-9])",\s*(PLAYER \+ )?"([^"]+)"( \+ FAST)?\}', more.read_text(encoding="utf-8"))
    return [(name, words, who, MANNER.get(who, "") + (player if with_player else "") + scene + (fast if quick else ""))
            for name, words, who, with_player, scene, quick in found]


# A squeal, a yelp or a wail is said as it is in Japanese, whatever letters it is shown in.
SOUNDS = (("Kyaa", "\u304d\u3083\u3042\u3063"), ("waaah", "\u3046\u308f\u3042\u3042\u3093"), ("Waaah", "\u3046\u308f\u3042\u3042\u3093"), ("Uu...", "\u3046\u3045..."),
          ("Eep", "\u3072\u3083\u3063"))


def spoken(text):
    """A line as it is to be said: without the heart, which is not a word, and with its squeals as they are squealed."""
    text = text.replace("\u2661", "").strip()
    # Chikuwa's voice does not say "bread" properly from its spelling: it is given the word as it sounds.
    text = re.sub(r"\bbread", "bred", text)
    text = re.sub(r"\bBread", "Bred", text)
    # A sound that is written as a word in what is shown is acted, not said: a sniff is a shaky breath in.
    text = re.sub(r"\b[Ss]niff(le)?\b[.! ]*", "[breathe] ", text)
    text = re.sub(r"\b[Hh]ic\b[.! ]*", "[breathe] ", text)
    for shown, said in SOUNDS:
        text = text.replace(shown, said)
    # Nabi is a cat: her laugh and her "nya" are said as they are in Japanese.
    text = re.sub(r"\bNyahaha\b", "\u306b\u3083\u306f\u306f", text)
    text = re.sub(r"\b[Nn]ya\b", "\u306b\u3083", text)
    return text


def lucy(said):
    """
    A line of Lucy's as it is said, which is not quite as it is shown. Her sounds are spelled the way they are made, so that she makes
    them and does not read them: "Bleh" is drawn out, and so is "Ow". And "Splash!" is not said at all: it is the sound of her water
    gun, which the game plays with the line.
    """
    said = re.sub(r"^Splash!\s*", "", said)
    said = re.sub(r"\bBleh\b", "Bleeeh~", said)
    said = re.sub(r"\bOw!", "Owww!", said)
    return said


def speak(key, text, who, scene, language=None):
    """Someone's voice saying some words, the scene told to it; in a language named (ja, ko, zh...) if the words are not English, so that the voice speaks it as its own."""
    voice, rate, _ = VOICES[who]
    said = spoken(text)
    if who == "e":
        said = lucy(said)
    # Rune's pauses, written out, made her longer lines take a quarter of a minute: in those they are only commas.
    if who == "R" and len(said) > 60:
        said = said.replace("... ", ", ")
    body = {"text": said, "voiceId": voice, "modelId": MODEL, "deliveryMode": "CREATIVE",
            "audioConfig": {"audioEncoding": "PCM", "sampleRateHertz": RATE_IN, "speakingRate": rate},
            "synthesisContext": {"reasoning": "REASONING_ON", "reasoningContext": scene[:600]}}
    if language:
        body["language"] = language
    headers ={"Authorization": f"Basic {key}", "Content-Type": "application/json", "Accept": "application/json"}
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
    # A soft last sound is quieter than what counts as loud here: a third of a second is kept after it, so no word loses its end.
    sound = sound[max(0, first - RATE_IN // 25): min(len(sound), last + RATE_IN // 3)]
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
