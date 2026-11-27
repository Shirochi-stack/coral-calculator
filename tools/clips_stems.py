"""All four stems of a film's sound with Demucs htdemucs: PREFIX-drums.wav, -bass.wav, -other.wav, -vocals.wav (mono, 48 kHz). python clips_stems.py FILM.mp4 PREFIX"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import scipy.signal, soundfile, torch
from clips_bed import read
from demucs.apply import apply_model
from demucs.pretrained import get_model

model = get_model("htdemucs"); model.eval()
wave = read(sys.argv[1], model.samplerate)
mix = torch.from_numpy(wave.astype("float32"))[None]
with torch.no_grad():
    out = apply_model(model, mix, device="cpu", split=True, overlap=.25, progress=False)[0]
for i, name in enumerate(model.sources):
    mono = out[i].mean(0).numpy()
    soundfile.write(f"{sys.argv[2]}-{name}.wav", scipy.signal.resample_poly(mono, 48000, model.samplerate).astype("float32"), 48000)
print("stems written:", model.sources)
