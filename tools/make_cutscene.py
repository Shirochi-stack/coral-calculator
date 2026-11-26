"""
Makes one clip of a cutscene with the video model Seedance 2.5, on BytePlus ModelArk.

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

One run makes one clip, and only when asked to with --go: it costs money. What it would
cost is printed before anything is sent. The task is asked after until it is done, the clip
is saved in the shot's folder with the task's number in its name, and so is what the
service answered. The key is read by its name from the environment or from the project's
.env, whatever the case of the name, and is never printed or written anywhere.

The request is the one BytePlus documents (read 2026-11-27):
https://ai.byteplus.com/ark/region:ap-southeast-1/docs/create-video-generation-task-api
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
PROJECT = pathlib.Path(r"C:\Users\ADMIN\Dev\Halgakos-AI")
BASE = "https://ark.ap-southeast.bytepluses.com/api/v3/contents/generations/tasks"
KEY_NAMES = ("BYTEPLUS_API_KEY", "ARK_API_KEY")
# What a clip is made of, in pixels, for each size and shape the 2.5 series makes; and what a million tokens cost, in dollars, with no video put in.
SIZES = {"480p": {"16:9": (854, 480), "4:3": (752, 560), "1:1": (640, 640), "3:4": (560, 752), "9:16": (480, 854), "21:9": (992, 432)},
         "720p": {"16:9": (1280, 720), "4:3": (1112, 834), "1:1": (960, 960), "3:4": (834, 1112), "9:16": (720, 1280), "21:9": (1470, 630)},
         "1080p": {"16:9": (1920, 1080), "4:3": (1664, 1248), "1:1": (1440, 1440), "3:4": (1248, 1664), "9:16": (1080, 1920), "21:9": (2206, 946)}}
RATES = {"dreamina-seedance-2-5-260628": {"480p": 10.70, "720p": 10.70, "1080p": 11.7}, "dreamina-seedance-2-5-flash-261115": {"480p": 7.0, "720p": 7.0}}
FRAMES_A_SECOND = 24
KINDS = {".png": "image/png", ".jpg": "image/jpeg", ".jpeg": "image/jpeg", ".webp": "image/webp", ".wav": "audio/wav", ".mp3": "audio/mp3"}


def secret():
    """The key, under either of its names, whatever their case: from the environment, or from the project's .env. Never printed."""
    wanted = {name.lower() for name in KEY_NAMES}
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


def estimate(body):
    """About how many tokens a clip is, and about what that costs: from its length and its size, as the service's own pages reckon it."""
    wide, tall = SIZES[body["resolution"]][body["ratio"]]
    tokens = body["duration"] * wide * tall * FRAMES_A_SECOND / 1024
    rate = RATES.get(body["model"], {}).get(body["resolution"])
    return wide, tall, tokens, rate, None if rate is None else tokens * rate / 1e6


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("shot", help="a folder under dist/cutscenes")
    parser.add_argument("--go", action="store_true", help="send it: this costs money")
    parser.add_argument("--task", help="a task already made: ask after it and save its clip, sending nothing new")
    args = parser.parse_args()
    folder = SHOTS / args.shot
    shot = json.loads((folder / "shot.json").read_text(encoding="utf-8"))
    body, shown = request_of(folder, shot)
    wide, tall, tokens, rate, costs = estimate(body)
    print("POST", BASE)
    print(json.dumps(shown, indent=1, ensure_ascii=False))
    print(f"\nOne clip: {body['duration']} s at {wide} x {tall}, about {tokens:,.0f} tokens.")
    print("Estimated cost: " + ("not known for this model and size" if costs is None else f"${costs:.2f} (at ${rate} a million tokens; nothing is charged if it fails)"))
    if not args.go and not args.task:
        print("\nNothing sent. Run again with --go to make the clip.")
        return
    key = secret()
    if not key:
        raise SystemExit(f"No key: put {KEY_NAMES[0]}=... in {PROJECT / '.env'} (or in the environment).")
    headers = {"Authorization": f"Bearer {key}", "Content-Type": "application/json"}
    if args.task:
        task = args.task
    else:
        reply = requests.post(BASE, headers=headers, json=body, timeout=300)
        if reply.status_code != 200:
            raise SystemExit(f"The service refused the request: HTTP {reply.status_code} {reply.text[:600]}")
        task = reply.json().get("id")
    print("task", task, flush=True)
    answer, began = {}, time.time()
    while time.time() - began < 45 * 60:
        time.sleep(0 if args.task and not answer else 10)
        try:
            answer = requests.get(f"{BASE}/{task}", headers=headers, timeout=60).json()
        except requests.RequestException as error:
            print("asking again after", type(error).__name__, flush=True)
            continue
        status = answer.get("status")
        print(f"{int(time.time() - began):4d} s", status, flush=True)
        if status in ("succeeded", "failed", "expired", "cancelled"):
            break
    (folder / f"{args.shot}-{task}.json").write_text(json.dumps(answer, indent=1, ensure_ascii=False), encoding="utf-8")
    if answer.get("status") != "succeeded":
        raise SystemExit(f"No clip: {answer.get('status')} {json.dumps(answer.get('error'), ensure_ascii=False)}")
    clip = requests.get(answer["content"]["video_url"], timeout=600)
    clip.raise_for_status()
    target = folder / f"{args.shot}-{task}.mp4"
    target.write_bytes(clip.content)
    used = (answer.get("usage") or {}).get("completion_tokens")
    print("saved", target, f"{len(clip.content):,} bytes")
    if used and rate:
        print(f"used {used:,} tokens: ${used * rate / 1e6:.2f}")


if __name__ == "__main__":
    sys.exit(main())
