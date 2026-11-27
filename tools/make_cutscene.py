"""
Makes the clips of a cutscene with a video model, one clip at a time: Seedance 2.5 on
BytePlus ModelArk, or Kling 4.0 on fal; and joins the clips of a storyboard into one film.

One shot, in a folder of its own:

    python tools/make_cutscene.py SHOT            says what would be sent and what it would cost, and sends nothing
    python tools/make_cutscene.py SHOT --go       sends it: one task, one clip
    python tools/make_cutscene.py SHOT --task ID  sends nothing: asks after a task already made, and saves its clip

SHOT is a folder under dist/cutscenes holding shot.json and what it names: the prompt, the
reference pictures, and a recording, if the shot has one. shot.json is

    {"model": "dreamina-seedance-2-5-260628", "prompt": "prompt.txt",
     "images": [{"file": "first_frame.png", "role": "reference_image"}, ...],
     "audio": [{"file": "line.wav"}],
     "resolution": "720p", "ratio": "9:16", "duration": 10, "generate_audio": true}

The pictures are numbered in the prompt in the order they are listed here: @Image 1 is the
first. Roles are those of the service: reference_image, for a shot made from references
(pictures, and a recording as reference_audio), or first_frame and last_frame, which may
not be mixed with references.

A storyboard, which is many shots cut to a song:

    python tools/make_cutscene.py BOARD                      every shot's request, and a table of what each would cost and all of them together; sends nothing
    python tools/make_cutscene.py BOARD --shot S07           that shot's request and cost; sends nothing
    python tools/make_cutscene.py BOARD --shot S07 --go      sends that one shot: one task, one clip
    python tools/make_cutscene.py BOARD --shot S07 --task ID asks after a task already made for it
    python tools/make_cutscene.py BOARD --edit               joins the shots, lays the song under them, and puts the names, the words and the title on

BOARD is the name of a file in tools/cutscenes (opening, for tools/cutscenes/opening.json).
It says the model, the size and shape, the length of a bar of the song, and its shots in
order: each has a name, the bar it begins on and how many bars it has, its first frame (a
still, kept under dist/cutscenes/BOARD/stills) and what happens in it. A shot is as long as
its bars (the clip asked for is that long, rounded up to whole seconds, of which the film
takes what it needs), and is made without sound. The model is a setting of the board, and
of any shot that names another ("model"). So is what a shot is made from ("made_from"):
"first_frame", from its still alone, which is then exactly its first picture; or
"references", from its still and the reference sheet of everyone in it (the board's
"sheets", by the letters of the shot's "cast"), the prompt saying which picture is which.
The service does not take a first frame and references together. A model named
kling/v4/image-to-video is Kling 4.0, asked of fal and not of BytePlus: it makes a shot
from its first frame only, in that picture's own shape, 3 to 30 seconds, and always with
sound, which has no switch and is whatever the prompt asks to be heard; it has no setting
for the camera either, so that too is asked for in words. Its still is put in fal's file
store when the shot is sent, since fal takes pictures by their address.

Sound: the board's "sound" says whether the shots are made with it ("on"), and what the
music and the singing voice are like; a shot's own "sound" says what else is heard in it
("effects") and what words are sung in it ("lyric"). With it on, these are put at the end
of the shot's prompt. Each clip then has a tune of its own: they do not join into a song,
and the film is still laid over one track. Its takes are saved under
dist/cutscenes/BOARD/takes with the task's number in their names. The film is made of the
newest take of each shot, or of the one named for it in dist/cutscenes/BOARD/picks.json
({"S07": "S07-cgt-....mp4"}); a shot with no take yet is its still, held, drifting a little:
so the film can be looked at, cut to its song, before a single clip is paid for. Names,
words and titles are drawn here, never asked of the model, which cannot spell.

One run makes one clip, and only when asked to with --go: it costs money. What it would
cost is printed before anything is sent. The task is asked after until it is done, the clip
is saved with the task's number in its name, and so is what the service answered. The key
is read by its name from the environment or from the project's .env, whatever the case of
the name, and is never printed or written anywhere.

The keys are FAL_KEY for fal and BYTEPLUS_API_KEY (or ARK_API_KEY) for BytePlus.

fal's side is as fal documents it (read 2026-11-27): https://fal.ai/models/kling/v4/image-to-video/llms.txt
(the request), https://fal.ai/docs (the queue: the request is sent, asked after at the
address the answer gives, and fetched when it is done; a picture is first put in the file
store at rest.fal.ai). The request to BytePlus is the one BytePlus documents (read 2026-11-27):
https://docs.byteplus.com/en/docs/modelark/create-video-generation-task-api
and the models, what each makes and takes, are listed at https://docs.byteplus.com/en/docs/modelark/model-list
"""
import argparse
import base64
import json
import os
import pathlib
import sys
import time

import requests

ROOT = pathlib.Path(__file__).resolve().parent.parent
SHOTS = ROOT / "dist" / "cutscenes"
BOARDS = ROOT / "tools" / "cutscenes"
PROJECT = pathlib.Path(r"C:\Users\ADMIN\Dev\Halgakos-AI")
BASE = "https://ark.ap-southeast.bytepluses.com/api/v3/contents/generations/tasks"
KEY_NAMES = ("BYTEPLUS_API_KEY", "ARK_API_KEY")
FAL = "https://queue.fal.run"
FAL_STORE = "https://rest.fal.ai/storage/upload/initiate?storage_type=fal-cdn-v3"
FAL_KEY_NAMES = ("FAL_KEY",)
# What fal's models cost, in dollars a second of what they make, by size (pictures put in cost nothing).
FAL_RATES = {"kling/v4/image-to-video": {"720p": .157, "1080p": .235, "4k": .549}}
# What a clip is made of, in pixels, for each size and shape the 2.5 series makes; and what a million tokens cost, in dollars, with no video put in.
SIZES = {"480p": {"16:9": (854, 480), "4:3": (752, 560), "1:1": (640, 640), "3:4": (560, 752), "9:16": (480, 854), "21:9": (992, 432)},
         "720p": {"16:9": (1280, 720), "4:3": (1112, 834), "1:1": (960, 960), "3:4": (834, 1112), "9:16": (720, 1280), "21:9": (1470, 630)},
         "1080p": {"16:9": (1920, 1080), "4:3": (1664, 1248), "1:1": (1440, 1440), "3:4": (1248, 1664), "9:16": (1080, 1920), "21:9": (2206, 946)}}
# (The flash model makes 480p and 720p only, 4 to 30 seconds, from up to ten reference pictures; the other up to 4K, from up to thirty.)
RATES = {"dreamina-seedance-2-5-260628": {"480p": 10.70, "720p": 10.70, "1080p": 11.7}, "dreamina-seedance-2-5-flash-261115": {"480p": 7.0, "720p": 7.0}}
PICTURES = {"dreamina-seedance-2-5-260628": 30, "dreamina-seedance-2-5-flash-261115": 10}
FRAMES_A_SECOND = 24
KINDS = {".png": "image/png", ".jpg": "image/jpeg", ".jpeg": "image/jpeg", ".webp": "image/webp", ".wav": "audio/wav", ".mp3": "audio/mp3"}
# The letters of the names, the words and the title: the first that this computer has, of each. (The second of each pair has the heart and the notes.)
FONTS = {"heavy": ("seguibl.ttf", "segoeuib.ttf", "arialbd.ttf"), "plain": ("segoeuib.ttf", "arialbd.ttf"), "signs": ("seguisym.ttf", "segoeuib.ttf")}


def secret(names=KEY_NAMES):
    """A key, under any of its names, whatever their case: from the environment, or from the project's .env. Never printed."""
    wanted = {name.lower() for name in names}
    for name, value in os.environ.items():
        if name.lower() in wanted and value.strip():
            return value.strip()
    env = PROJECT / ".env"
    if env.exists():
        for line in env.read_text(encoding="utf-8").splitlines():
            name, _, value = line.partition("=")
            if name.strip().lower() in wanted and value.strip():
                return value.strip().strip("\"'")
    return ""


def inline(path):
    """A file as the service takes one that is not at an address of its own: its kind, and its bytes in base64."""
    return f"data:{KINDS[path.suffix.lower()]};base64,{base64.b64encode(path.read_bytes()).decode('ascii')}"


def request_of(folder, shot):
    """The body of the request for a shot; and the same with what is long left out, for showing."""
    text = (folder / shot["prompt"]).read_text(encoding="utf-8").strip()
    content, shown = [{"type": "text", "text": text}], [{"type": "text", "text": text}]
    for image in shot.get("images", []):
        path = folder / image["file"]
        content.append({"type": "image_url", "image_url": {"url": inline(path)}, "role": image["role"]})
        shown.append({"type": "image_url", "image_url": {"url": f"<{path.name}, {path.stat().st_size} bytes, in base64>"}, "role": image["role"]})
    for sound in shot.get("audio", []):
        path = folder / sound["file"]
        content.append({"type": "audio_url", "audio_url": {"url": inline(path)}, "role": "reference_audio"})
        shown.append({"type": "audio_url", "audio_url": {"url": f"<{path.name}, {path.stat().st_size} bytes, in base64>"}, "role": "reference_audio"})
    rest = {"resolution": shot.get("resolution", "720p"), "ratio": shot.get("ratio", "9:16"), "duration": int(shot.get("duration", 10)),
            "generate_audio": bool(shot.get("generate_audio", True)), "watermark": False}
    # A shot made from references says so, so that what is wrong with it is said at once and not after it has waited its turn.
    if any(image["role"] == "reference_image" for image in shot.get("images", [])) or shot.get("audio"):
        rest["omni_reference_task_type"] = "reference"
    return {"model": shot["model"], "content": content, **rest}, {"model": shot["model"], "content": shown, **rest}


def board_request(board, folder, shot):
    """
    The request for one shot of a storyboard: what happens in it and how it is drawn, the pictures it is made from, and no sound. With a
    picture not made yet, there is nothing to send. Also what is shown of it, which has the shape asked of the service in it even where
    the request itself must leave that to the first frame.
    """
    import math

    model = shot.get("model", board["model"])
    text = shot["prompt"].strip() + " " + board["style"].strip()
    # What is heard, if the shots are made with sound: the music, what sounds in this shot, and the voice with this shot's words to sing.
    sound = board.get("sound") or {}
    heard = bool(sound.get("on", board.get("generate_audio", False)))
    if heard and sound:
        own = shot.get("sound") or {}
        said = ["Sound: " + own.get("music", sound.get("music", "")).strip()]
        if own.get("effects"):
            said.append("Sound effects: " + own["effects"].strip())
        voice = own.get("voice", sound.get("voice", "")).strip()
        if voice and own.get("lyric"):
            said.append(f'{voice} She sings: "{own["lyric"]}"')
        text += " " + " ".join(said)
    elif not heard and sound.get("without"):
        text += " " + sound["without"].strip()
    # The service makes whole seconds, four at the least (fal's, three): the film takes of a clip what its bars need.
    seconds = max(3 if model in FAL_RATES else 4, math.ceil(shot["bars"] * board["bar"] - 1e-9))
    way = shot.get("made_from", board.get("made_from", "first_frame"))
    if model in FAL_RATES:
        # Kling, on fal: a first frame and words. No shape (the picture's own), no switch for sound, nothing for the camera.
        if way != "first_frame":
            raise SystemExit(f"{shot['name']}: {model} makes a shot from its first frame only; give it \"made_from\": \"first_frame\".")
        still = folder / shot["still"]
        rest = {"duration": seconds, "resolution": board.get("resolution", "720p")}
        shown = {"model": model, "prompt": text, "start_image_url": f"<{still.name}, {still.stat().st_size} bytes: put in fal's file store when sent>" if still.exists()
                 else f"<{still.name}: NOT MADE YET>", **rest, "ratio": board.get("ratio", "16:9"), "sound": "always made; what is heard is in the prompt" if heard
                 else "always made by this model; none was asked for, and the film's edit drops it"}
        return ({"model": model, "prompt": text, "still": str(still), **rest} if still.exists() else None), shown
    pictures = [(folder / shot["still"], "first_frame" if way == "first_frame" else "reference_image")]
    rest = {"resolution": board.get("resolution", "720p"), "ratio": board.get("ratio", "16:9"), "duration": seconds,
            "generate_audio": heard, "watermark": False}
    if way == "references":
        # Made from references: its still, which the prompt says to begin from, and the sheet of everyone in it.
        said = ["@Image 1 is the first frame of the shot: begin exactly from it, and keep its drawing style."]
        for letter in shot.get("cast", ""):
            sheet = board.get("sheets", {}).get(letter)
            if sheet:
                pictures.append((folder / sheet["file"], "reference_image"))
                said.append(f"@Image {len(pictures)} is the reference sheet of {sheet['name']}: she looks exactly as on it from every side.")
        text = " ".join(said) + " " + text
        rest["omni_reference_task_type"] = "reference"
        if len(pictures) > PICTURES.get(model, 9):
            raise SystemExit(f"{shot['name']}: {len(pictures)} pictures are more than {model} takes.")
    elif way != "first_frame":
        raise SystemExit(f"{shot['name']}: made_from is first_frame or references, not {way}.")
    content, seen = [{"type": "text", "text": text}], [{"type": "text", "text": text}]
    for picture, role in pictures:
        made = picture.exists()
        seen.append({"type": "image_url", "role": role, "image_url": {"url": f"<{picture.name}, {picture.stat().st_size} bytes, in base64>" if made else f"<{picture.name}: NOT MADE YET>"}})
        content.append({"type": "image_url", "image_url": {"url": inline(picture)}, "role": role} if made else None)
    shown = {"model": model, "content": seen, **rest}
    if None in content:
        return None, shown
    body = {"model": model, "content": content, **rest}
    if way == "first_frame":
        # From a first frame the service takes no shape but the picture's own.
        body["ratio"] = "adaptive"
        shown["sent as ratio"] = "adaptive (the first frame's own shape)"
    return body, shown


def estimate(body):
    """About how many tokens a clip is, and about what that costs: from its length and its size, as the service's own pages reckon it."""
    wide, tall = SIZES.get(body["resolution"], {}).get(body.get("ratio", "16:9"), (0, 0))
    if body["model"] in FAL_RATES:
        # (fal reckons by the second, not by tokens.)
        rate = FAL_RATES[body["model"]].get(body["resolution"])
        return wide, tall, 0, rate, None if rate is None else body["duration"] * rate
    tokens = body["duration"] * wide * tall * FRAMES_A_SECOND / 1024
    rate = RATES.get(body["model"], {}).get(body["resolution"])
    return wide, tall, tokens, rate, None if rate is None else tokens * rate / 1e6


def say(shown):
    """What would be sent, and what it would cost."""
    wide, tall, tokens, rate, costs = estimate(shown)
    if shown["model"] in FAL_RATES:
        print("POST", f"{FAL}/{shown['model']}")
        print(json.dumps({key: value for key, value in shown.items() if key != "model"}, indent=1, ensure_ascii=False))
        print(f"\nOne clip: {shown['duration']} s at {shown['resolution']} ({wide} x {tall} for a picture of this shape), with sound. The prompt is {len(shown['prompt'])} letters.")
        print("Estimated cost: " + ("not known for this size" if costs is None else f"${costs:.2f} (at ${rate} a second; a request that fails on the service's side is not charged)"))
        return
    print("POST", BASE)
    print(json.dumps(shown, indent=1, ensure_ascii=False))
    if shown["resolution"] not in RATES.get(shown["model"], {shown["resolution"]: 0}):
        raise SystemExit(f"{shown['model']} does not make {shown['resolution']}.")
    print(f"\nOne clip: {shown['duration']} s at {wide} x {tall}, about {tokens:,.0f} tokens.")
    print("Estimated cost: " + ("not known for this model and size" if costs is None else f"${costs:.2f} (at ${rate} a million tokens; nothing is charged if it fails)"))


def make_fal(body, folder, stem, task):
    """
    The same of fal: the still is put in fal's file store, the request is sent to its queue (or one already sent is asked after), it is
    waited for, and the clip and what fal answered are saved under a name with the request's number in it.
    """
    key = secret(FAL_KEY_NAMES)
    if not key:
        raise SystemExit(f"No key: put {FAL_KEY_NAMES[0]}=... in {PROJECT / '.env'} (or in the environment).")
    headers = {"Authorization": f"Key {key}", "Content-Type": "application/json"}
    model = body["model"]
    folder.mkdir(parents=True, exist_ok=True)
    if task:
        sent = json.loads((folder / f"{stem}-{task}.sent.json").read_text(encoding="utf-8"))
    else:
        still = pathlib.Path(body["still"])
        kind = KINDS[still.suffix.lower()]
        place = requests.post(FAL_STORE, headers=headers, json={"file_name": still.name, "content_type": kind}, timeout=60)
        if place.status_code != 200:
            raise SystemExit(f"fal's file store refused the picture: HTTP {place.status_code} {place.text[:400]}")
        place = place.json()
        put = requests.put(place["upload_url"], data=still.read_bytes(), headers={"Content-Type": kind}, timeout=300)
        if put.status_code not in (200, 201, 204):
            raise SystemExit(f"The picture could not be put in fal's file store: HTTP {put.status_code} {put.text[:400]}")
        asked = {"prompt": body["prompt"], "start_image_url": place["file_url"], "duration": body["duration"], "resolution": body["resolution"]}
        reply = requests.post(f"{FAL}/{model}", headers=headers, json=asked, timeout=300)
        if reply.status_code not in (200, 201, 202):
            raise SystemExit(f"The service refused the request: HTTP {reply.status_code} {reply.text[:800]}")
        sent = reply.json()
        task = sent["request_id"]
        (folder / f"{stem}-{task}.sent.json").write_text(json.dumps({**sent, "asked": asked}, indent=1, ensure_ascii=False), encoding="utf-8")
    print("request", task, flush=True)
    status, began = {}, time.time()
    while time.time() - began < 45 * 60:
        time.sleep(8)
        try:
            status = requests.get(sent["status_url"], headers=headers, timeout=60).json()
        except (requests.RequestException, ValueError) as error:
            print("asking again after", type(error).__name__, flush=True)
            continue
        print(f"{int(time.time() - began):4d} s", status.get("status"), flush=True)
        if status.get("status") not in ("IN_QUEUE", "IN_PROGRESS"):
            break
    result = requests.get(sent["response_url"], headers=headers, timeout=120)
    try:
        answer = result.json()
    except ValueError:
        answer = {"text": result.text[:2000]}
    (folder / f"{stem}-{task}.json").write_text(json.dumps({"status": status, "http": result.status_code, "answer": answer}, indent=1, ensure_ascii=False), encoding="utf-8")
    if result.status_code != 200 or not isinstance(answer, dict) or "video" not in answer:
        raise SystemExit(f"No clip: HTTP {result.status_code} {json.dumps(answer, ensure_ascii=False)[:800]}")
    clip = requests.get(answer["video"]["url"], timeout=600)
    clip.raise_for_status()
    target = folder / f"{stem}-{task}.mp4"
    target.write_bytes(clip.content)
    print("saved", target, f"{len(clip.content):,} bytes")
    # What fal says a second of this model costs now, asked of fal itself.
    try:
        priced = requests.get("https://api.fal.ai/v1/models/pricing", params={"endpoint_id": model}, headers=headers, timeout=60).json()
        for price in priced.get("prices", []):
            print(f"fal's price for {price.get('endpoint_id')}: {price.get('unit_price')} {price.get('currency')} a {price.get('unit')}")
    except (requests.RequestException, ValueError) as error:
        print("fal's price could not be asked:", type(error).__name__)


def make(body, folder, stem, task):
    """Sends one task, or asks after one already sent; waits for it; and saves the clip and what the service answered under a name with the task's number in it."""
    if body["model"] in FAL_RATES:
        return make_fal(body, folder, stem, task)
    rate = RATES.get(body["model"], {}).get(body["resolution"])
    key = secret()
    if not key:
        raise SystemExit(f"No key: put {KEY_NAMES[0]}=... in {PROJECT / '.env'} (or in the environment).")
    headers = {"Authorization": f"Bearer {key}", "Content-Type": "application/json"}
    asked = bool(task)
    if not task:
        reply = requests.post(BASE, headers=headers, json=body, timeout=300)
        if reply.status_code != 200:
            raise SystemExit(f"The service refused the request: HTTP {reply.status_code} {reply.text[:600]}")
        task = reply.json().get("id")
    print("task", task, flush=True)
    answer, began = {}, time.time()
    while time.time() - began < 45 * 60:
        time.sleep(0 if asked and not answer else 10)
        try:
            answer = requests.get(f"{BASE}/{task}", headers=headers, timeout=60).json()
        except requests.RequestException as error:
            print("asking again after", type(error).__name__, flush=True)
            continue
        status = answer.get("status")
        print(f"{int(time.time() - began):4d} s", status, flush=True)
        if status in ("succeeded", "failed", "expired", "cancelled"):
            break
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{stem}-{task}.json").write_text(json.dumps(answer, indent=1, ensure_ascii=False), encoding="utf-8")
    if answer.get("status") != "succeeded":
        raise SystemExit(f"No clip: {answer.get('status')} {json.dumps(answer.get('error'), ensure_ascii=False)}")
    clip = requests.get(answer["content"]["video_url"], timeout=600)
    clip.raise_for_status()
    target = folder / f"{stem}-{task}.mp4"
    target.write_bytes(clip.content)
    used = (answer.get("usage") or {}).get("completion_tokens")
    print("saved", target, f"{len(clip.content):,} bytes")
    if used and rate:
        print(f"used {used:,} tokens: ${used * rate / 1e6:.2f}")


def takes(folder, board):
    """For each shot, the clip the film is made of: the one picked for it, or its newest take, or none."""
    picks = {}
    if (folder / "picks.json").exists():
        picks = json.loads((folder / "picks.json").read_text(encoding="utf-8"))
    found = {}
    for shot in board["shots"]:
        name = shot["name"]
        if name in picks:
            if not (folder / "takes" / picks[name]).exists():
                raise SystemExit(f"picks.json names {picks[name]} for {name}, and there is no such take.")
            found[name] = folder / "takes" / picks[name]
            continue
        made = sorted((folder / "takes").glob(f"{name}-*.mp4"), key=lambda path: path.stat().st_mtime)
        found[name] = made[-1] if made else None
    return found


def table(board, folder):
    """Every shot of a storyboard: when it is, how long, whether its still is made and how many takes it has, and what one clip of it would cost; and all of them together."""
    have = takes(folder, board)
    total, seconds, all_tokens = 0.0, 0, 0.0
    print(f"\n{'shot':5} {'from':>6} {'asked':>5}  {'still':7} {'takes':>5} {'tokens':>9} {'cost':>7}  {'made from':11} {'model':8} who")
    for shot in board["shots"]:
        _, shown = board_request(board, folder, shot)
        _, _, tokens, rate, costs = estimate(shown)
        count = len(list((folder / "takes").glob(f"{shot['name']}-*.mp4")))
        total += costs or 0
        all_tokens += tokens
        seconds += shown["duration"]
        print(f"{shot['name']:5} {shot['bar'] * board['bar']:5.0f}s {shown['duration']:4d}s  {'made' if (folder / shot['still']).exists() else 'NOT YET':7} {count:5d} "
              f"{tokens:9,.0f} {'?' if costs is None else f'${costs:6.2f}'}  {shot.get('made_from', board.get('made_from', 'first_frame')):11} "
              f"{'kling 4' if shown['model'] in FAL_RATES else 'flash' if 'flash' in shown['model'] else '2.5':8} {shot.get('cast', '') or '-'}"
              + (f"  [{have[shot['name']].name}]" if have[shot["name"]] else ""))
    print(f"{'all':5} {'':6} {seconds:4d}s  {'':7} {'':5} {all_tokens:9,.0f} ${total:6.2f}  one take of each of the {len(board['shots'])} shots (nothing is charged for a take that fails)")
    size = board.get("resolution", "720p")
    pixels = SIZES[size][board.get("ratio", "16:9")]
    for model, rates in RATES.items():
        if size in rates:
            print(f"      the same, all of it with {model}: ${seconds * pixels[0] * pixels[1] * FRAMES_A_SECOND / 1024 * rates[size] / 1e6:.2f}")
    for model, rates in FAL_RATES.items():
        if size in rates:
            print(f"      the same, all of it with {model} (on fal): ${seconds * rates[size]:.2f}")


def edit(board, folder):
    """Joins the shots in their order, each as long as its bars; lays the song under them; and puts the names, the words of the song and the title on."""
    import fractions

    import av
    import cv2
    import numpy
    import soundfile
    from PIL import Image, ImageDraw, ImageFont

    wide, tall = SIZES[board.get("resolution", "720p")][board.get("ratio", "16:9")]
    fps, bar = FRAMES_A_SECOND, board["bar"]
    have = takes(folder, board)

    def font(kind, size):
        for name in FONTS[kind]:
            for place in (pathlib.Path(os.environ.get("WINDIR", r"C:\Windows")) / "Fonts", pathlib.Path("/usr/share/fonts/truetype/dejavu")):
                if (place / name).exists():
                    return ImageFont.truetype(str(place / name), size)
        return ImageFont.load_default()

    def runs(text, letters, signs):
        """A line as the runs of it that one font can draw: the heart and the notes are not in the letters' font."""
        parts = []
        for letter in text:
            which = signs if letter in "\u2661\u2665\u266a\u266b" else letters
            if parts and parts[-1][1] is which:
                parts[-1][0] += letter
            else:
                parts.append([letter, which])
        return parts

    def write(layer, text, middle, top, size, kind, fill, edge, line=5, anchor="middle"):
        """Writes a line on a layer with an edge round its letters; says how wide it came out."""
        letters, signs = font(kind, size), font("signs", size)
        parts = runs(text, letters, signs)
        width = sum(which.getlength(run) for run, which in parts)
        x = middle - width / 2 if anchor == "middle" else middle if anchor == "left" else middle - width
        draw = ImageDraw.Draw(layer)
        for run, which in parts:
            draw.text((x, top), run, font=which, fill=fill, stroke_width=line, stroke_fill=edge)
            x += which.getlength(run)
        return width

    # What is written over the picture, and when: each is a layer, from a second to a second, fading in and out.
    over = []
    words = json.loads((folder / board["words"]).read_text(encoding="utf-8")) if board.get("words") and (folder / board["words"]).exists() else {"lines": []}
    for said in words["lines"]:
        layer = Image.new("RGBA", (wide, tall), (0, 0, 0, 0))
        write(layer, said["shown"], wide / 2, tall - 92, 38, "plain", (255, 255, 255, 255), (60, 20, 60, 255))
        over.append((layer, said["at"], said["at"] + max(said["bars"] * bar - .15, said.get("lasts", 0)), .15))
    for shot in board["shots"]:
        card = shot.get("card")
        if not card:
            continue
        layer = Image.new("RGBA", (wide, tall), (0, 0, 0, 0))
        left = card.get("side", "right") == "left"
        x = 70 if left else wide - 70
        width = write(layer, card["name"], x, tall - 300, 84, "heavy", (255, 255, 255, 255), (214, 51, 108, 255), 7, "left" if left else "right")
        write(layer, card["says"], x, tall - 186, 36, "plain", (255, 255, 255, 255), (60, 20, 60, 255), 4, "left" if left else "right")
        begins = shot["bar"] * bar
        over.append((layer, begins + .35, begins + shot["bars"] * bar - .3, .25))
    total = sum(shot["bars"] for shot in board["shots"]) * bar
    title = board.get("title")
    if title:
        layer = Image.new("RGBA", (wide, tall), (0, 0, 0, 0))
        # As large as its longest line allows; a title may be of more lines than one.
        lines, size = title["text"].split("\n"), 104
        while size > 30 and max(sum(which.getlength(run) for run, which in runs(line, font("heavy", size), font("signs", size))) for line in lines) > wide - 160:
            size -= 2
        top = tall * .1
        for line in lines:
            write(layer, line, wide / 2, top, size, "heavy", (255, 255, 255, 255), (214, 51, 108, 255), 9)
            top += size * 1.22
        if title.get("under"):
            write(layer, title["under"], wide / 2, top + size * .25, 36, "plain", (255, 255, 255, 255), (60, 20, 60, 255), 4)
        over.append((layer, title["from"], total + 1, .5))
    over = [(numpy.asarray(layer, dtype=numpy.float32), begins, ends, fades) for layer, begins, ends, fades in over]

    target = folder / f"{folder.name}.mp4"
    out = av.open(str(target), "w")
    picture = out.add_stream("libx264", rate=fps)
    picture.width, picture.height, picture.pix_fmt = wide, tall, "yuv420p"
    picture.options = {"crf": "20", "preset": "medium", "profile": "high"}
    # (Both of the film's tracks are named before anything is written to it.)
    song = folder / board["song"] if board.get("song") else None
    wave, sound = None, None
    if song and song.exists():
        wave, rate = soundfile.read(song, dtype="float32", always_2d=True)
        wave = wave[: int(total * rate)]
        if wave.shape[1] == 1:
            wave = numpy.repeat(wave, 2, axis=1)
        sound = out.add_stream("aac", rate=rate)
        sound.bit_rate = 160000
        sound.layout = "stereo"
    index, clips = 0, 0
    for shot in board["shots"]:
        count = int(round(shot["bars"] * bar * fps))
        clip = have[shot["name"]]
        video = cv2.VideoCapture(str(clip)) if clip else None
        still = None
        if clip:
            clips += 1
        elif (folder / shot["still"]).exists():
            still = cv2.cvtColor(numpy.asarray(Image.open(folder / shot["still"]).convert("RGB").resize((wide, tall), Image.LANCZOS)), cv2.COLOR_RGB2BGR)
        last = numpy.zeros((tall, wide, 3), numpy.uint8)
        for step in range(count):
            if video is not None:
                ok, frame = video.read()
                # A take a little shorter than its bars is held on its last picture.
                frame = cv2.resize(frame, (wide, tall), interpolation=cv2.INTER_AREA) if ok and frame.shape[:2] != (tall, wide) else frame if ok else last
            elif still is not None:
                # A still, held: it comes a little nearer, so that it is plainly not yet a clip but is not dead either.
                near = 1 + .05 * step / count
                frame = cv2.warpAffine(still, numpy.float32([[near, 0, (1 - near) * wide / 2], [0, near, (1 - near) * tall / 2]]), (wide, tall), flags=cv2.INTER_LINEAR)
            else:
                frame = last
            last = frame
            now = index / fps
            shown = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB).astype(numpy.float32)
            for layer, begins, ends, fades in over:
                if begins <= now < ends:
                    strength = min(1.0, (now - begins) / fades, (ends - now) / fades)
                    alpha = layer[:, :, 3:4] / 255 * strength
                    shown = shown * (1 - alpha) + layer[:, :, :3] * alpha
            # Up out of black at the beginning, and down into it at the end.
            shown *= min(1.0, now / .5 + .0001, (total - now) / 1.0)
            image = av.VideoFrame.from_ndarray(numpy.clip(shown, 0, 255).astype(numpy.uint8), format="rgb24")
            image.pts = index
            image.time_base = fractions.Fraction(1, fps)
            for packet in picture.encode(image):
                out.mux(packet)
            index += 1
        if video is not None:
            video.release()
    for packet in picture.encode():
        out.mux(packet)
    if sound is not None:
        samples = (numpy.clip(wave[:, :2], -1, 1) * 32767).astype(numpy.int16)
        for start in range(0, len(samples), 1024):
            chunk = av.AudioFrame.from_ndarray(numpy.ascontiguousarray(samples[start:start + 1024].reshape(1, -1)), format="s16", layout="stereo")
            chunk.sample_rate = rate
            chunk.pts = start
            chunk.time_base = fractions.Fraction(1, rate)
            for packet in sound.encode(chunk):
                out.mux(packet)
        for packet in sound.encode():
            out.mux(packet)
    else:
        print("no song found: the film is silent")
    out.close()
    print(f"saved {target} {target.stat().st_size:,} bytes: {index / fps:.1f} s, {clips} of its {len(board['shots'])} shots are clips"
          + ("" if clips == len(board["shots"]) else ", the rest their stills, held"))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("shot", help="a folder under dist/cutscenes; or the name of a storyboard in tools/cutscenes")
    parser.add_argument("--shot", dest="one", help="of a storyboard: the one shot meant")
    parser.add_argument("--go", action="store_true", help="send it: this costs money")
    parser.add_argument("--task", help="a task already made: ask after it and save its clip, sending nothing new")
    parser.add_argument("--edit", action="store_true", help="of a storyboard: join its shots into the film, with its song, names, words and title")
    args = parser.parse_args()
    folder = SHOTS / args.shot
    if (BOARDS / f"{args.shot}.json").exists():
        board = json.loads((BOARDS / f"{args.shot}.json").read_text(encoding="utf-8"))
        (folder / "takes").mkdir(parents=True, exist_ok=True)
        if args.edit:
            return edit(board, folder)
        chosen = [shot for shot in board["shots"] if args.one in (None, shot["name"])]
        if not chosen:
            raise SystemExit(f"No shot {args.one} in {args.shot}: it has {', '.join(shot['name'] for shot in board['shots'])}.")
        if (args.go or args.task) and not args.one:
            raise SystemExit("One shot at a time: name it with --shot.")
        for shot in chosen:
            print(f"\n== {shot['name']}: from {shot['bar'] * board['bar']:.0f} s, {shot['bars']} bars")
            body, shown = board_request(board, folder, shot)
            say(shown)
        if not args.one:
            table(board, folder)
        if not args.go and not args.task:
            print("\nNothing sent. To make one shot: --shot NAME --go.")
            return None
        if body is None:
            raise SystemExit(f"{args.one} has no still yet ({folder / chosen[0]['still']}): nothing sent.")
        return make(body, folder / "takes", args.one + ("-kling" if body["model"] in FAL_RATES else ""), args.task)
    if args.edit or args.one:
        raise SystemExit(f"{args.shot} is not a storyboard: there is no {BOARDS / (args.shot + '.json')}.")
    shot = json.loads((folder / "shot.json").read_text(encoding="utf-8"))
    body, shown = request_of(folder, shot)
    say(shown)
    if not args.go and not args.task:
        print("\nNothing sent. Run again with --go to make the clip.")
        return None
    return make(body, folder, args.shot, args.task)


if __name__ == "__main__":
    sys.exit(main())
