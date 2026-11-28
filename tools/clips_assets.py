"""
Makes what goes into the app for the approved clips, from the 1080p masters (which are never touched): for each clip ID (its two-digit number),

    python tools/clips_assets.py 01 07 13 ...

  * res/raw/run_clip_NN.mp4: the picture alone, 1280 x 720 H.264 (or HEVC with --hevc), no sound, a key frame every second or two;
  * res/raw/run_clip_NN_h.m4a, _h_ja, _h_ko, _h_zh: the voice and the effects, AAC mono 64 kb/s, in the four languages;
  * assets/films/clip_NN_h[_ja|_ko|_zh].{en,ja,ko,zh}.txt: the lines to be written under it, in each language, for each voice;
  * assets/gallery/video_clipNN.webp (a frame, for its tile) and board_clipNN.webp (its storyboard: four frames).
The masters and their beds and voices stay in dist/cutscenes/clips/ID/. The held spans of a clip (HOLDS in make_clips.py) are held again here.
"""
import pathlib
import sys

import av
import numpy
import soundfile
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import clips_data as data  # noqa: E402
import make_clips as mk  # noqa: E402

RAW = ROOT / "films" / "src" / "main" / "assets" / "films"
ASSETS = ROOT / "app" / "src" / "main" / "assets"
LANGS = ("en", "ja", "ko", "zh")


def picture(master, target, size=(1280, 720), bitrate=1_100_000, holds=(), codec="libx264"):
    box = av.open(str(master))
    video = box.streams.video[0]
    out = av.open(str(target), "w", options={"movflags": "+faststart"})
    stream = out.add_stream(codec, rate=video.average_rate)
    stream.width, stream.height = size
    stream.pix_fmt = "yuv420p"
    stream.bit_rate = bitrate
    fps = int(round(float(video.average_rate)))
    stream.options = ({"profile": "main", "preset": "slow", "g": str(fps * 2), "keyint_min": str(fps), "maxrate": str(int(bitrate * 1.5)), "bufsize": str(bitrate * 2)}
                      if codec == "libx264" else {"preset": "slow", "x265-params": f"keyint={fps * 2}"})
    kept = None
    for frame in box.decode(video):
        t = float(frame.pts * frame.time_base)
        image = frame.to_image().resize(size, Image.LANCZOS)
        if any(a <= t <= b for a, b in holds) and kept is not None:
            image = kept
        else:
            kept = image
        for packet in stream.encode(av.VideoFrame.from_image(image)):
            out.mux(packet)
    for packet in stream.encode():
        out.mux(packet)
    out.close()
    box.close()


def sound(wav, target, bitrate=64000):
    data_, rate = soundfile.read(str(wav))
    if data_.ndim > 1:
        data_ = data_.mean(axis=1)
    out = av.open(str(target), "w")
    stream = out.add_stream("aac", rate=rate, layout="mono")
    stream.bit_rate = bitrate
    pcm = data_.astype("float32")
    for i in range(0, len(pcm), 1024):
        frame = av.AudioFrame.from_ndarray(pcm[i:i + 1024].reshape(1, -1), format="flt", layout="mono")
        frame.sample_rate = rate
        frame.pts = i
        for packet in stream.encode(frame):
            out.mux(packet)
    for packet in stream.encode():
        out.mux(packet)
    out.close()


def frames(master, fractions):
    box = av.open(str(master))
    video = box.streams.video[0]
    every = [f.to_image() for f in box.decode(video)]
    box.close()
    return [every[min(len(every) - 1, int(len(every) * f))] for f in fractions]


def make(number, codec="libx264", bitrate=1_100_000):
    clip = next(c for c in data.CLIPS if c["id"].startswith(f"{number}-"))
    folder = mk.CLIPS / clip["id"]
    master = mk.source_of(clip)
    holds = mk.HOLDS.get(number, ())
    picture(master, RAW / f"run_clip_{number}.mp4", holds=holds, bitrate=bitrate, codec=codec)
    for lang in LANGS:
        suffix = "" if lang == "en" else f"_{lang}"
        sound(folder / f"{clip['id']}-{lang}.wav", RAW / f"run_clip_{number}_h{suffix}.m4a")
        for cap in LANGS:
            text = (folder / f"{clip['id']}.{cap}.txt").read_text(encoding="utf-8")
            # What is written is placed by the voice in use: the same lines, in the language of the caption.
            rows = []
            for row, line in zip(text.strip().split("\n"), (folder / f"{clip['id']}.{lang}.txt").read_text(encoding="utf-8").strip().split("\n")):
                a, b, _ = line.split("\t")
                rows.append(f"{a}\t{b}\t{row.split(chr(9))[2]}")
            (ASSETS / "films" / f"clip_{number}_h{suffix}.{cap}.txt").write_text("\n".join(rows) + "\n", encoding="utf-8")
    shots = frames(master, (.1, .35, .6, .88))
    tile = shots[1].copy()
    tile.thumbnail((640, 640))
    tile.save(ASSETS / "gallery" / f"video_clip{number}.webp", "WEBP", quality=80)
    sheet = Image.new("RGB", (1280, 720))
    for i, im in enumerate(shots):
        sheet.paste(im.resize((640, 360)), ((i % 2) * 640, (i // 2) * 360))
    sheet.save(ASSETS / "gallery" / f"board_clip{number}.webp", "WEBP", quality=80)
    print(number, clip["id"], "done", (RAW / f"run_clip_{number}.mp4").stat().st_size)


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    for n in args:
        make(n)
