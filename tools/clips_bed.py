"""
Takes the voices out of the sound of each clip, keeping the effects: the model's own sound is separated with Demucs (htdemucs, two stems:
the voices, and everything else), and what is not the voices is kept as the clip's bed (ID/ID-bed.wav, mono, 48 kHz). Run with a Python that
has demucs and torch (a venv of its own):

    python clips_bed.py PICTURE.mp4 OUT-bed.wav [PICTURE2.mp4 OUT2 ...]

Writes OUT-bed.wav and prints how much of the speech band's energy was left in it, against the original, in decibels.
"""
import sys

import av
import numpy
import scipy.signal
import soundfile
import torch
from demucs.apply import apply_model
from demucs.pretrained import get_model

RATE_OUT = 48000


def read(path, rate):
    box = av.open(str(path))
    resampler = av.AudioResampler(format="flt", layout="stereo", rate=rate)
    parts = []
    for frame in box.decode(audio=0):
        for out in resampler.resample(frame):
            parts.append(out.to_ndarray())
    for out in resampler.resample(None):
        parts.append(out.to_ndarray())
    box.close()
    # Packed stereo comes as one row of interleaved samples.
    data = numpy.concatenate(parts, axis=1)
    return data.reshape(-1, 2).T if data.shape[0] == 1 else data


def speech_db(mono, rate):
    band = scipy.signal.sosfilt(scipy.signal.butter(4, [300, 3400], "bandpass", fs=rate, output="sos"), mono)
    return 10 * numpy.log10(float((band ** 2).mean()) + 1e-12)


def main():
    model = get_model("htdemucs")
    model.eval()
    rate = model.samplerate
    args = sys.argv[1:]
    for i in range(0, len(args), 2):
        source, target = args[i], args[i + 1]
        wave = read(source, rate)
        mix = torch.from_numpy(wave.astype("float32"))[None]
        ref = mix.mean(0)
        with torch.no_grad():
            out = apply_model(model, mix, device="cpu", split=True, overlap=.25, progress=False)[0]
        names = model.sources
        import os
        if os.environ.get("STEMS"):
            for k, nm in enumerate(names):
                soundfile.write(target.replace("-bed", "-" + nm), scipy.signal.resample_poly(out[k].mean(0).numpy(), RATE_OUT, rate).astype("float32"), RATE_OUT)
        vocals = out[names.index("vocals")]
        bed = mix[0] - vocals
        bed_mono = bed.mean(0).numpy()
        orig_mono = mix[0].mean(0).numpy()
        up = scipy.signal.resample_poly(bed_mono, RATE_OUT, rate).astype("float32")
        soundfile.write(target, up, RATE_OUT)
        voc = scipy.signal.resample_poly(vocals.mean(0).numpy(), RATE_OUT, rate).astype("float32")
        soundfile.write(target.replace("-bed", "-vocals"), voc, RATE_OUT)
        print(f"{source}: bed written to {target}; speech band {speech_db(orig_mono, rate):.1f} dB before, {speech_db(bed_mono, rate):.1f} dB after; total {10 * numpy.log10(float((orig_mono ** 2).mean()) + 1e-12):.1f} -> {10 * numpy.log10(float((bed_mono ** 2).mean()) + 1e-12):.1f} dB", flush=True)


if __name__ == "__main__":
    main()
