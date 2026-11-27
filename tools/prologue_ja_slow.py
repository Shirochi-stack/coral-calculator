"""
The Japanese prologue with its picture slowed between 0:05 and 0:30, so that Halgakos's lines there are said at natural speed.

    python tools/prologue_ja_slow.py spec F        writes tools/cutscenes/prologue-slow-mc.json for a slowing of F (1.25: a quarter slower), and the stretched band
    python tools/prologue_ja_slow.py picture F     the picture: 0:05-0:30 of the shipped picture slowed by F with motion-compensated in-between frames

The map of time: before 5 s nothing changes; from 5 s to 30 s the film is F times as long; after 30 s everything is later by D = 25 (F - 1) seconds.
Every line of the spoken-lyrics spec has its start and its room moved by that map; the band (the instrumental her track is made over) is stretched
over 5 to 30 s with its pitch kept, and the rest of it moved later; make_mc.py then makes the track from the new spec (--tongue ja, as the
other languages are made). The picture is made from the picture the app ships (res/raw/run_film_prologue.mp4), no frame redrawn: the in-between
frames are the two frames either side of them, moved along their motion (OpenCV DIS optical flow) and blended.
"""
import json
import math
import pathlib
import sys

import numpy

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes")
RATE = 48000
A, B = 5.0, 30.0


def mapped(t, f):
    return t if t <= A else A + (t - A) * f if t <= B else t + (B - A) * (f - 1)


def spec(f):
    import clips_fit as cf
    import soundfile

    base = json.loads((ROOT / "tools" / "cutscenes" / "prologue-mc.json").read_text(encoding="utf-8"))
    extra = (B - A) * (f - 1)
    seconds = int(math.ceil(base["seconds"] + extra))
    lines = []
    for line in base["lines"]:
        at, room = line[0], line[1]
        new_at = mapped(at, f)
        new_room = room * f if A < at < B and at + room <= B + .5 else room
        lines.append([round(new_at, 2), round(new_room, 2)] + line[2:])
    base["lines"] = lines
    base["seconds"] = seconds
    base["out"] = "opening/prologue-halgakos-slow"
    # the band, stretched over 5 to 30 s (pitch kept) and the rest moved
    band = cf.read_audio(D / base["band"])
    cut = lambda a, b: band[int(a * RATE):int(b * RATE)]
    middle = cf.stretch(cut(A, B), 1 / f, RATE)
    fade = int(.04 * RATE)
    parts = [cut(0, A), middle, cut(B, 90)]
    out = parts[0]
    for part in parts[1:]:
        ramp = numpy.linspace(0, 1, fade)
        out = numpy.concatenate([out[:-fade], out[-fade:] * (1 - ramp) + part[:fade] * ramp, part[fade:]])
    soundfile.write(str(D / "opening" / "band-slow.wav"), out[: seconds * RATE], RATE)
    base["band"] = "opening/band-slow.wav"
    (ROOT / "tools" / "cutscenes" / "prologue-slow-mc.json").write_text(json.dumps(base, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    print("seconds", seconds, "added", round(extra, 2), "s; band", round(len(out) / RATE, 2), "s")


def picture(f):
    import av
    import cv2
    from PIL import Image

    src = ROOT / "app" / "src" / "main" / "res" / "raw" / "run_film_prologue.mp4"
    box = av.open(str(src))
    video = box.streams.video[0]
    fps = float(video.average_rate)
    frames = [fr.to_ndarray(format="rgb24") for fr in box.decode(video)]
    box.close()
    n = len(frames)
    extra = (B - A) * (f - 1)
    total = int(round((n / fps + extra) * fps))
    dis = cv2.DISOpticalFlow_create(cv2.DISOPTICAL_FLOW_PRESET_MEDIUM)
    out = av.open(str(D / "opening" / "prologue-ja-slow.mp4"), "w", options={"movflags": "+faststart"})
    stream = out.add_stream("libx264", rate=round(fps))
    stream.width, stream.height = frames[0].shape[1], frames[0].shape[0]
    stream.pix_fmt = "yuv420p"
    stream.bit_rate = 1_150_000
    stream.options = {"profile": "main", "preset": "slow", "g": str(int(fps * 2)), "keyint_min": str(int(fps)), "maxrate": str(int(1_150_000 * 1.6)), "bufsize": str(2_300_000)}
    flows = {}

    def warp(i, j, u):
        """The frame u of the way from frame i to frame j, by their motion."""
        a, b = frames[i], frames[j]
        ga, gb = cv2.cvtColor(a, cv2.COLOR_RGB2GRAY), cv2.cvtColor(b, cv2.COLOR_RGB2GRAY)
        if (i, j) not in flows:
            flows[(i, j)] = (dis.calc(ga, gb, None), dis.calc(gb, ga, None))
        fwd, back = flows[(i, j)]
        h, w = ga.shape
        gx, gy = numpy.meshgrid(numpy.arange(w), numpy.arange(h))
        wa = cv2.remap(a, (gx - fwd[..., 0] * u).astype("float32"), (gy - fwd[..., 1] * u).astype("float32"), cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)
        wb = cv2.remap(b, (gx - back[..., 0] * (1 - u)).astype("float32"), (gy - back[..., 1] * (1 - u)).astype("float32"), cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)
        return cv2.addWeighted(wa, 1 - u, wb, u, 0)

    kept = None
    for k in range(total):
        t = k / fps
        # which time of the shipped film is shown at this time of the new one
        if t <= A:
            s = t
        elif t <= B + extra:
            s = A + (t - A) / f
        else:
            s = t - extra
        s = min(s * fps, n - 1.001)
        i = int(s)
        u = s - i
        if t > A and t <= B + extra and u > .02:
            frame = warp(i, i + 1, u)
        else:
            frame = frames[i]
        for packet in stream.encode(av.VideoFrame.from_ndarray(frame, format="rgb24")):
            out.mux(packet)
    for packet in stream.encode():
        out.mux(packet)
    out.close()
    print("picture", total, "frames,", round(total / fps, 2), "s")


if __name__ == "__main__":
    f = float(sys.argv[2])
    {"spec": spec, "picture": picture}[sys.argv[1]](f)
