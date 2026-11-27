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

Long takes: a board's "segments" divide it into a few takes of many shots each, so that a
film is a handful of requests and not one for every shot.

    python tools/make_cutscene.py BOARD --segment A --with kling            what would be sent, its cost, what is left to spend; sends nothing
    python tools/make_cutscene.py BOARD --segment A --with kling --go       sends that one take
    python tools/make_cutscene.py BOARD --segment A --with seedance --go    the same take, of Seedance 2.5
    python tools/make_cutscene.py BOARD --edit --with kling                 joins the takes of one model into the film (BOARD-kling.mp4), with their own sound
    python tools/make_cutscene.py BOARD --edit --with kling --song FILE     the same picture with a song laid over in place of the takes' sound (BOARD-kling-edited.mp4)
Beside each film a copy under 28 MB is written, the same size and sound at a lower bit rate (...-for-phone.mp4).
The edit takes whatever takes are in the folder under a name (A-NAME-....mp4), not only those this tool asked for: takes made
elsewhere (--with magi, for clips made on a website) are joined the same way, whatever their size, speed or length: each is
fitted into the film's picture, read by its own clock, and cut or held on its last frame to its thirty seconds.

Of Kling (kling/v4/reference-to-video on fal) a take is asked shot by shot, each with its
seconds and at most 512 letters, with the first shot's still as the first frame, the other
shots' stills as pictures to go by, and each character as an element (her front, side and
back). Of Seedance it is asked as one prompt that lists the same shots with their times,
with the stills and the characters' sheets as reference pictures. What every shot says is
the same for both: what happens, how it is drawn, the music, what sounds, and the words
sung. What is spent is kept in takes/spending.json, with a ceiling for each service that
no call may pass; a call that makes no clip is not counted.

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
FAL_RATES = {"kling/v4/image-to-video": {"720p": .157, "1080p": .235, "4k": .549}, "kling/v4/reference-to-video": {"720p": .157, "1080p": .235, "4k": .549}}
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
        return ({"model": model, "asked": {"prompt": text, "start_image_url": "file:" + str(still), **rest}, **rest} if still.exists() else None), shown
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


def stored(value, headers, kept):
    """What fal is asked, with every local file in it (a string "file:PATH") put in fal's file store and its address there in its place."""
    if isinstance(value, dict):
        return {key: stored(inner, headers, kept) for key, inner in value.items()}
    if isinstance(value, list):
        return [stored(inner, headers, kept) for inner in value]
    if not (isinstance(value, str) and value.startswith("file:")):
        return value
    local = pathlib.Path(value[5:])
    mark = f"{local}|{local.stat().st_size}|{int(local.stat().st_mtime)}"
    if mark not in kept:
        kind = KINDS[local.suffix.lower()]
        # (The store is asked up to four times: a connection that does not open is tried again, and nothing has been asked of the model yet.)
        for attempt in range(4):
            try:
                place = requests.post(FAL_STORE, headers=headers, json={"file_name": local.name, "content_type": kind}, timeout=60)
                if place.status_code != 200:
                    raise SystemExit(f"fal's file store refused {local.name}: HTTP {place.status_code} {place.text[:400]}")
                place = place.json()
                put = requests.put(place["upload_url"], data=local.read_bytes(), headers={"Content-Type": kind}, timeout=(30, 300))
                if put.status_code not in (200, 201, 204):
                    raise SystemExit(f"{local.name} could not be put in fal's file store: HTTP {put.status_code} {put.text[:400]}")
                break
            except requests.RequestException as error:
                print(f"putting {local.name} in fal's file store: {type(error).__name__}, try {attempt + 1} of 4", flush=True)
                if attempt == 3:
                    raise SystemExit(f"{local.name} could not be put in fal's file store: {type(error).__name__}, four times.")
                time.sleep(5)
        kept[mark] = place["file_url"]
        if "__book__" in kept:
            pathlib.Path(kept["__book__"]).write_text(json.dumps({key: value for key, value in kept.items() if key != "__book__"}, indent=1), encoding="utf-8")
    return kept[mark]


def make_fal(body, folder, stem, task):
    """
    The same of fal: the pictures are put in fal's file store, the request is sent to its queue (or one already sent is asked after), it
    is waited for, and the clip and what fal answered are saved under a name with the request's number in it. Gives the clip's place.
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
        book = folder / "uploads.json"
        kept = json.loads(book.read_text(encoding="utf-8")) if book.exists() else {}
        kept["__book__"] = str(book)
        asked = stored(body["asked"], headers, kept)
        reply = requests.post(f"{FAL}/{model}", headers=headers, json=asked, timeout=300)
        if reply.status_code not in (200, 201, 202):
            raise SystemExit(f"The service refused the request: HTTP {reply.status_code} {reply.text[:1200]}")
        sent = reply.json()
        task = sent["request_id"]
        (folder / f"{stem}-{task}.sent.json").write_text(json.dumps({**sent, "asked": asked}, indent=1, ensure_ascii=False), encoding="utf-8")
    print("request", task, flush=True)
    status, began = {}, time.time()
    while time.time() - began < 5 * 3600:
        time.sleep(15)
        try:
            status = requests.get(sent["status_url"], headers=headers, timeout=60).json()
        except (requests.RequestException, ValueError) as error:
            print("asking again after", type(error).__name__, flush=True)
            continue
        if int(time.time() - began) % 120 < 16 or status.get("status") not in ("IN_QUEUE", "IN_PROGRESS"):
            print(f"{int(time.time() - began):5d} s", status.get("status"), flush=True)
        if status.get("status") not in ("IN_QUEUE", "IN_PROGRESS"):
            break
    result = requests.get(sent["response_url"], headers=headers, timeout=120)
    try:
        answer = result.json()
    except ValueError:
        answer = {"text": result.text[:2000]}
    (folder / f"{stem}-{task}.json").write_text(json.dumps({"status": status, "http": result.status_code, "answer": answer}, indent=1, ensure_ascii=False), encoding="utf-8")
    if result.status_code != 200 or not isinstance(answer, dict) or "video" not in answer:
        raise SystemExit(f"No clip: HTTP {result.status_code} {json.dumps(answer, ensure_ascii=False)[:1500]}")
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
    return target, None


def make(body, folder, stem, task):
    """Sends one task, or asks after one already sent; waits for it; and saves the clip and what the service answered under a name with the task's number in it."""
    if body["model"] in FAL_RATES:
        return make_fal(body, folder, stem, task)
    rate = RATES.get(body["model"], {}).get(body["resolution"])
    body = {key: value for key, value in body.items() if key != "asked"}
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
    while time.time() - began < 5 * 3600:
        time.sleep(0 if asked and not answer else 15)
        try:
            answer = requests.get(f"{BASE}/{task}", headers=headers, timeout=60).json()
        except requests.RequestException as error:
            print("asking again after", type(error).__name__, flush=True)
            continue
        status = answer.get("status")
        if int(time.time() - began) % 120 < 16 or status in ("succeeded", "failed", "expired", "cancelled"):
            print(f"{int(time.time() - began):5d} s", status, flush=True)
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
    return target, (used * rate / 1e6 if used and rate else None)


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


def spending(folder):
    """What has been spent, by service, with the ceiling no call may pass: kept beside the takes."""
    book = folder / "takes" / "spending.json"
    return book, (json.loads(book.read_text(encoding="utf-8")) if book.exists() else {})


def left_of(spent, who):
    """What a service's ceiling still allows: the ceiling less every call that made a clip or is still out."""
    account = spent.get(who) or {}
    if account.get("ceiling") is None:
        raise SystemExit(f"No ceiling is set for {who} in spending.json: nothing is sent without one.")
    return account["ceiling"] - sum(call["price"] for call in account.get("calls", []) if call["state"] != "no clip, not charged")


def take_request(board, folder, part, which):
    """
    One long take: several shots in order, asked of Kling shot by shot, or of Seedance as one prompt listing them. What each shot
    says is the same for both. Gives what is sent (local files as "file:..." for fal), what is shown of it, its seconds and its price.
    """
    plan = board["segments"]
    model, size = plan["models"][which], plan["resolution"]
    shots = [next(shot for shot in board["shots"] if shot["name"] == name) for name in part["shots"]]
    stills = [folder / plan["stills"] / f"{shot['name']}.png" for shot in shots]
    cast = [letter for letter in board["sheets"] if any(letter in shot.get("cast", "") for shot in shots)]
    total = int(round(sum(shot["bars"] for shot in shots) * board["bar"]))

    def said(shot, names, scene, room=None):
        # What happens; how it is drawn; the music; what sounds; and the words sung. If there is not room, what sounds goes first, then how it is drawn.
        told = shot["short"].format(**names)
        sung = f'{plan["voice"]} "{shot["sound"]["lyric"]}"'
        for pieces in ([scene, told, plan["look"], plan["music"], "Sounds: " + shot["heard"], sung], [scene, told, plan["look"], plan["music"], sung], [scene, told, plan["music"], sung]):
            line = " ".join(pieces)
            if room is None or len(line) <= room:
                return line
        raise SystemExit(f"{shot['name']}: its words are {len(line)} letters, and a shot may have {room}.")

    if model in FAL_RATES:
        names = {letter: f"@Element{index + 1}" for index, letter in enumerate(cast)}
        multi = [{"prompt": said(shot, names, "Begins on the first frame." if index == 0 else f"Scene as in @Image{index}.", 512),
                  "duration": int(round(shot["bars"] * board["bar"]))} for index, shot in enumerate(shots)]
        asked = {"multi_prompt": multi, "start_image_url": "file:" + str(stills[0]), "image_urls": ["file:" + str(still) for still in stills[1:]],
                 "elements": [{"frontal_image_url": "file:" + str(folder / board["sheets"][letter]["views"][0]),
                               "reference_image_urls": ["file:" + str(folder / view) for view in board["sheets"][letter]["views"][1:]]} for letter in cast],
                 "resolution": size, "aspect_ratio": board.get("ratio", "16:9")}
        price = total * FAL_RATES[model][size]
        return {"model": model, "asked": asked}, {"POST": f"{FAL}/{model}", **asked, "elements are": [board["sheets"][letter]["name"] for letter in cast]}, total, price, "fal"
    pictures = stills + [folder / board["sheets"][letter]["file"] for letter in cast]
    names = {letter: f"{board['sheets'][letter]['name']} (@Image {len(stills) + index + 1})" for index, letter in enumerate(cast)}
    head = (f"An anime opening: one {total}-second sequence of {len(shots)} shots, with a hard cut between shots at the times given. @Image 1 to @Image {len(stills)} are the first "
            f"pictures of the shots, in order; the sequence begins exactly on @Image 1. "
            + " ".join(f"@Image {len(stills) + index + 1} is the reference sheet of {board['sheets'][letter]['name']}: she looks exactly as on it from every side." for index, letter in enumerate(cast)))
    lines, at = [], 0
    for index, shot in enumerate(shots):
        lasts = int(round(shot["bars"] * board["bar"]))
        lines.append(f"Shot {index + 1} ({at}-{at + lasts} s): " + said(shot, names, "Begins on @Image 1." if index == 0 else f"Scene as in @Image {index + 1}."))
        at += lasts
    words = head + " " + " ".join(lines)
    rest = {"resolution": size, "ratio": board.get("ratio", "16:9"), "duration": total, "generate_audio": True, "watermark": False, "omni_reference_task_type": "reference"}
    body = {"model": model, "content": [{"type": "text", "text": words}] + [{"type": "image_url", "image_url": {"url": inline(picture)}, "role": "reference_image"} for picture in pictures], **rest}
    shown = {"POST": BASE, "model": model, "text": words, "pictures": [picture.name for picture in pictures], **rest}
    wide, tall = SIZES[size][rest["ratio"]]
    price = total * wide * tall * FRAMES_A_SECOND / 1024 * RATES[model][size] / 1e6
    return body, shown, total, price, "byteplus"


def one_take(board, folder, part, which, go, task):
    """Says what a long take would send and cost and what is left to spend; and, asked to, sends it, counting it against the ceiling only if it makes a clip."""
    body, shown, seconds, price, who = take_request(board, folder, part, which)
    print(json.dumps(shown, indent=1, ensure_ascii=False))
    if "multi_prompt" in shown:
        print("letters in each shot's words:", [len(shot["prompt"]) for shot in shown["multi_prompt"]])
    book, spent = spending(folder)
    left = left_of(spent, who)
    size = board["segments"]["resolution"]
    print(f"\nOne take: {seconds} s at {size}, with sound. Estimated cost: ${price:.2f}. Left to spend with {who}: ${left:.2f}; after this, ${left - price:.2f}.")
    if not go and not task:
        print("Nothing sent. To make it: --go.")
        return None
    stem = f"{part['name']}-{which}"
    if task:
        return make(body, folder / "takes", stem, task)
    if price > left + 1e-9:
        raise SystemExit(f"Not sent: it would cost ${price:.2f} and ${left:.2f} is left with {who}.")
    mark = f"{time.time():.3f}-{os.getpid()}"

    def noted(**changes):
        # (Read again each time and written at once: more than one take may be out together, each keeping this book.)
        _, now = spending(folder)
        calls = now[who].setdefault("calls", [])
        mine = next((call for call in calls if call.get("mark") == mark), None)
        if mine is None:
            mine = {"mark": mark}
            calls.append(mine)
        mine.update(changes)
        book.write_text(json.dumps(now, indent=1), encoding="utf-8")
        return now

    noted(when=time.strftime("%Y-%m-%d %H:%M:%S"), what=f"{board_name(folder)} take {part['name']} with {which}", seconds=seconds, resolution=size, price=round(price, 2), state="sent")
    began = time.time()
    try:
        target, real = make(body, folder / "takes", stem, None)
    except BaseException as failed:
        # If the request had gone to the model before this, it may still make its clip and be charged: it stays counted, to be asked after.
        out = [note for note in (folder / "takes").glob(f"{stem}-*.sent.json") if note.stat().st_mtime >= began - 1]
        if out and not isinstance(failed, SystemExit):
            noted(state="sent, not heard from: ask after it with --task " + out[-1].name[len(stem) + 1:-len(".sent.json")], said=type(failed).__name__)
        else:
            noted(state="no clip, not charged", said=(str(failed) or type(failed).__name__)[:600])
        raise
    now = noted(state="made", clip=target.name, **({"price": round(real, 2)} if real is not None else {}))
    print(f"spent with {who} so far: ${now[who]['ceiling'] - left_of(now, who):.2f}; left: ${left_of(now, who):.2f}")
    return target


def board_name(folder):
    return folder.name


def for_phone(film, limit=28_000_000):
    """A copy of a film small enough to send to a phone: the same pictures and sound, at the bit rate that brings it under the limit."""
    import av

    source = av.open(str(film))
    seconds = float(source.duration) / 1e6
    source.close()
    target = film.with_name(film.stem + "-for-phone.mp4")
    rate = int((limit * 8 / seconds - 128_000) * .93)
    while True:
        source = av.open(str(film))
        out = av.open(str(target), "w")
        seen, heard = source.streams.video[0], source.streams.audio[0] if source.streams.audio else None
        picture = out.add_stream("libx264", rate=seen.average_rate)
        picture.width, picture.height, picture.pix_fmt = seen.codec_context.width, seen.codec_context.height, "yuv420p"
        picture.bit_rate = rate
        picture.options = {"preset": "slow", "profile": "high", "maxrate": str(int(rate * 1.3)), "bufsize": str(rate * 2)}
        sound = None
        if heard is not None:
            sound = out.add_stream("aac", rate=48000)
            sound.bit_rate = 128000
            sound.layout = "stereo"
            resampler = av.AudioResampler(format=sound.format, layout="stereo", rate=48000)
        for packet in source.demux():
            for frame in packet.decode():
                if packet.stream.type == "video":
                    frame.pict_type = 0
                    for made in picture.encode(frame.reformat(format="yuv420p")):
                        out.mux(made)
                elif sound is not None:
                    for again in resampler.resample(frame):
                        for made in sound.encode(again):
                            out.mux(made)
        for made in picture.encode():
            out.mux(made)
        if sound is not None:
            for made in sound.encode():
                out.mux(made)
        out.close()
        source.close()
        if target.stat().st_size <= limit:
            break
        rate = int(rate * .9)
    print(f"saved {target} {target.stat().st_size:,} bytes")
    return target


def edit_takes(board, folder, which, song=None):
    """
    The film from the long takes of one model: each take as long as its shots, in order, at the takes' own size; their own sound, eased
    down and up across each join; and the names and the title drawn on. A take not made yet is its shots' stills, held, in silence.
    """
    import fractions

    import av
    import cv2
    import numpy
    from PIL import Image, ImageDraw, ImageFont

    plan = board["segments"]
    wide, tall = SIZES[plan["resolution"]][board.get("ratio", "16:9")]
    fps, bar, rate = FRAMES_A_SECOND, board["bar"], 48000
    scale = tall / 720
    picks = json.loads((folder / "picks.json").read_text(encoding="utf-8")) if (folder / "picks.json").exists() else {}

    def font(kind, size):
        for name in FONTS[kind]:
            place = pathlib.Path(os.environ.get("WINDIR", r"C:\Windows")) / "Fonts" / name
            if place.exists():
                return ImageFont.truetype(str(place), int(size * scale))
        return ImageFont.load_default()

    def write(layer, text, x, top, size, kind, fill, edge, line, anchor):
        letters, signs = font(kind, size), font("signs", size)
        parts = []
        for letter in text:
            mine = signs if letter in "\u2661\u2665\u266a\u266b" else letters
            if parts and parts[-1][1] is mine:
                parts[-1][0] += letter
            else:
                parts.append([letter, mine])
        width = sum(mine.getlength(run) for run, mine in parts)
        x = x - width / 2 if anchor == "middle" else x if anchor == "left" else x - width
        draw = ImageDraw.Draw(layer)
        for run, mine in parts:
            draw.text((x, top), run, font=mine, fill=fill, stroke_width=int(line * scale), stroke_fill=edge)
            x += mine.getlength(run)
        return width

    def chosen(part):
        made = sorted((clip for clip in (folder / "takes").glob(f"{part['name']}-{which}-*.mp4") if "for-phone" not in clip.name), key=lambda clip: clip.stat().st_mtime)
        return folder / "takes" / picks[f"{part['name']}-{which}"] if f"{part['name']}-{which}" in picks else made[-1] if made else None

    # Where each shot really begins and ends: a take cuts near where it was asked to, not on it, so the cuts are looked for in the take
    # itself (a frame quite unlike the one before), and a shot's is the one found nearest where it was asked, within a second and a bit.
    real, base = {}, 0.0
    for part in plan["list"]:
        shots = [next(shot for shot in board["shots"] if shot["name"] == shot_name) for shot_name in part["shots"]]
        asked, at = [], 0.0
        for shot in shots:
            asked.append(at)
            at += shot["bars"] * bar
        found = []
        clip = chosen(part)
        if clip is not None:
            video = cv2.VideoCapture(str(clip))
            speed = video.get(cv2.CAP_PROP_FPS) or fps
            before, steps = None, []
            while True:
                ok, frame = video.read()
                if not ok:
                    break
                small = cv2.resize(frame, (480, 270), interpolation=cv2.INTER_AREA).astype(numpy.float32)
                if before is not None:
                    steps.append(float(numpy.abs(small - before).mean()))
                before = small
            video.release()
            steps = numpy.array(steps)
            found = [(i + 1) / speed for i in range(len(steps)) if steps[i] > 28 and steps[i] > 3.5 * numpy.median(steps[max(0, i - 12):i + 12])]
        begins = [min((cut for cut in found if abs(cut - a) < 1.2), key=lambda cut: abs(cut - a), default=a) if a > 0 else 0.0 for a in asked]
        for index, shot in enumerate(shots):
            real[shot["name"]] = (base + begins[index], base + (begins[index + 1] if index + 1 < len(shots) else at))
        base += at
    over = []
    for shot in board["shots"]:
        card = shot.get("card")
        if not card:
            continue
        layer = Image.new("RGBA", (wide, tall), (0, 0, 0, 0))
        left = card.get("side", "right") == "left"
        x = 70 * scale if left else wide - 70 * scale
        write(layer, card["name"], x, tall - 300 * scale, 84, "heavy", (255, 255, 255, 255), (214, 51, 108, 255), 7, "left" if left else "right")
        write(layer, card["says"], x, tall - 186 * scale, 36, "plain", (255, 255, 255, 255), (60, 20, 60, 255), 4, "left" if left else "right")
        begins, ends = real[shot["name"]]
        over.append((layer, begins + .35, ends - .3, .25))
    total = sum(shot["bars"] for shot in board["shots"]) * bar
    title = board.get("title")
    if title:
        layer = Image.new("RGBA", (wide, tall), (0, 0, 0, 0))
        lines, size = title["text"].split("\n"), 104
        top = tall * .1
        for line in lines:
            write(layer, line, wide / 2, top, size, "heavy", (255, 255, 255, 255), (214, 51, 108, 255), 9, "middle")
            top += size * scale * 1.22
        if title.get("under"):
            write(layer, title["under"], wide / 2, top + size * scale * .25, 36, "plain", (255, 255, 255, 255), (60, 20, 60, 255), 4, "middle")
        over.append((layer, title["from"], total + 1, .5))
    layers = []
    for layer, begins, ends, fades in over:
        box = layer.getbbox()
        piece = numpy.asarray(layer.crop(box), dtype=numpy.float32)
        layers.append((piece[:, :, :3], piece[:, :, 3:4] / 255, box, begins, ends, fades))

    name = f"{folder.name}-{which}-edited.mp4" if song else f"{folder.name}-{which}.mp4"
    out = av.open(str(folder / name), "w")
    picture = out.add_stream("libx264", rate=fps)
    picture.width, picture.height, picture.pix_fmt = wide, tall, "yuv420p"
    picture.options = {"crf": "18", "preset": "medium", "profile": "high"}
    sound = out.add_stream("aac", rate=rate)
    sound.bit_rate = 192000
    sound.layout = "stereo"
    index, heard, used = 0, [], []
    for part in plan["list"]:
        shots = [next(shot for shot in board["shots"] if shot["name"] == shot_name) for shot_name in part["shots"]]
        count = int(round(sum(shot["bars"] for shot in shots) * bar * fps))
        clip = chosen(part)
        used.append(clip.name if clip else "its stills")
        video = cv2.VideoCapture(str(clip)) if clip else None
        speed = (video.get(cv2.CAP_PROP_FPS) or fps) if video is not None else fps
        last = numpy.zeros((tall, wide, 3), numpy.uint8)
        read, fitted = -1, last
        for step in range(count):
            if video is not None:
                # The take's own clock: the picture it shows at this moment, whatever its frames a second; past its end, its last one.
                want = int(round(step / fps * speed))
                while read < want:
                    ok, raw = video.read()
                    if not ok:
                        break
                    read += 1
                    if raw.shape[:2] == (tall, wide):
                        fitted = raw
                    else:
                        # Fitted whole into the film's picture, with black beside it if its shape is another.
                        ratio = min(wide / raw.shape[1], tall / raw.shape[0])
                        size = (int(round(raw.shape[1] * ratio)), int(round(raw.shape[0] * ratio)))
                        fitted = numpy.zeros((tall, wide, 3), numpy.uint8)
                        x, y = (wide - size[0]) // 2, (tall - size[1]) // 2
                        fitted[y:y + size[1], x:x + size[0]] = cv2.resize(raw, size, interpolation=cv2.INTER_LANCZOS4)
                frame = fitted
            else:
                now = step / fps
                at = 0
                for shot in shots:
                    if now < at + shot["bars"] * bar or shot is shots[-1]:
                        frame = cv2.resize(cv2.imread(str(folder / plan["stills"] / f"{shot['name']}.png")), (wide, tall), interpolation=cv2.INTER_LANCZOS4)
                        break
                    at += shot["bars"] * bar
            last = frame
            now = index / fps
            shown = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            if any(begins <= now < ends for _, _, _, begins, ends, _ in layers) or now < .5 or total - now < 1:
                shown = shown.astype(numpy.float32)
                for colour, alpha, box, begins, ends, fades in layers:
                    if begins <= now < ends:
                        strength = min(1.0, (now - begins) / fades, (ends - now) / fades)
                        area = shown[box[1]:box[3], box[0]:box[2]]
                        shown[box[1]:box[3], box[0]:box[2]] = area * (1 - alpha * strength) + colour * alpha * strength
                shown = numpy.clip(shown * min(1.0, now / .5 + .0001, (total - now) / 1.0), 0, 255).astype(numpy.uint8)
            image = av.VideoFrame.from_ndarray(numpy.ascontiguousarray(shown), format="rgb24")
            image.pts = index
            image.time_base = fractions.Fraction(1, fps)
            for packet in picture.encode(image):
                out.mux(packet)
            index += 1
        if video is not None:
            video.release()
        # The take's own sound, as long as its picture; eased up at its beginning and down at its end, which is where the takes join.
        wave = numpy.zeros((int(count / fps * rate), 2), numpy.float32)
        if clip is not None:
            box = av.open(str(clip))
            if box.streams.audio:
                resampler = av.AudioResampler(format="flt", layout="stereo", rate=rate)
                chunks = [made_.to_ndarray().reshape(-1, 2) for frame_ in box.decode(audio=0) for made_ in resampler.resample(frame_)]
                got = numpy.concatenate(chunks)[: len(wave)] if chunks else wave[:0]
                wave[: len(got)] = got
        ease = int(.15 * rate)
        wave[:ease] *= numpy.linspace(0, 1, ease)[:, None]
        wave[-ease:] *= numpy.linspace(1, 0, ease)[:, None]
        heard.append(wave)
    for packet in picture.encode():
        out.mux(packet)
    wave = numpy.concatenate(heard)
    if song:
        # A song laid over: the takes' own sound is left out, and the song is as long as the film, come what may.
        box = av.open(str(song))
        resampler = av.AudioResampler(format="flt", layout="stereo", rate=rate)
        got = numpy.concatenate([made_.to_ndarray().reshape(-1, 2) for frame_ in box.decode(audio=0) for made_ in resampler.resample(frame_)])[: len(wave)]
        wave = numpy.zeros_like(wave)
        wave[: len(got)] = got
        wave *= .97 / max(.97, float(numpy.abs(wave).max()))
    wave[-rate:] *= numpy.linspace(1, 0, rate)[:, None]
    samples = (numpy.clip(wave, -1, 1) * 32767).astype(numpy.int16)
    for start in range(0, len(samples), 1024):
        chunk = av.AudioFrame.from_ndarray(numpy.ascontiguousarray(samples[start:start + 1024].reshape(1, -1)), format="s16", layout="stereo")
        chunk.sample_rate = rate
        chunk.pts = start
        chunk.time_base = fractions.Fraction(1, rate)
        for packet in sound.encode(chunk):
            out.mux(packet)
    for packet in sound.encode():
        out.mux(packet)
    out.close()
    print(f"saved {folder / name} {(folder / name).stat().st_size:,} bytes: {index / fps:.1f} s at {wide} x {tall}, made of {used}" + (f", with {pathlib.Path(song).name} laid over" if song else ""))
    for_phone(folder / name)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("shot", help="a folder under dist/cutscenes; or the name of a storyboard in tools/cutscenes")
    parser.add_argument("--shot", dest="one", help="of a storyboard: the one shot meant")
    parser.add_argument("--go", action="store_true", help="send it: this costs money")
    parser.add_argument("--task", help="a task already made: ask after it and save its clip, sending nothing new")
    parser.add_argument("--edit", action="store_true", help="of a storyboard: join its shots into the film, with its song, names, words and title")
    parser.add_argument("--segment", help="of a storyboard with long takes: the one take meant")
    parser.add_argument("--with", dest="which", help="kling or seedance: whose long takes are meant")
    parser.add_argument("--song", help="with --edit and --with: a song to lay over the film in place of the takes' own sound")
    args = parser.parse_args()
    folder = SHOTS / args.shot
    if (BOARDS / f"{args.shot}.json").exists():
        board = json.loads((BOARDS / f"{args.shot}.json").read_text(encoding="utf-8"))
        (folder / "takes").mkdir(parents=True, exist_ok=True)
        if args.segment:
            part = next((part for part in board["segments"]["list"] if part["name"] == args.segment), None)
            if part is None or args.which not in board["segments"]["models"]:
                raise SystemExit("Name a take of this board (--segment) and whose it is (--with kling, or --with seedance).")
            one_take(board, folder, part, args.which, args.go, args.task)
            return None
        if args.edit and args.which:
            return edit_takes(board, folder, args.which, args.song)
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
        make(body, folder / "takes", args.one + ("-kling" if body["model"] in FAL_RATES else ""), args.task)
        return None
    if args.edit or args.one:
        raise SystemExit(f"{args.shot} is not a storyboard: there is no {BOARDS / (args.shot + '.json')}.")
    shot = json.loads((folder / "shot.json").read_text(encoding="utf-8"))
    body, shown = request_of(folder, shot)
    say(shown)
    if not args.go and not args.task:
        print("\nNothing sent. Run again with --go to make the clip.")
        return None
    make(body, folder, args.shot, args.task)
    return None


if __name__ == "__main__":
    sys.exit(main())
