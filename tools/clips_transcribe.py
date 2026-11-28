"""Transcribes the generated speech of each clip (its Demucs vocals stem) with word timestamps: python tools/clips_transcribe.py [ID ...]  -> dist/cutscenes/clips/<id>/<id>-speech.json"""
import json, pathlib, sys
from faster_whisper import WhisperModel
C = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\clips")
model = WhisperModel("small", device="cpu", compute_type="int8")
ids = sys.argv[1:] or sorted(p.name for p in C.iterdir() if p.is_dir() and p.name[:2].isdigit())
for cid in ids:
    d = C / cid; v = d / f"{cid}-vocals.wav"; out = d / f"{cid}-speech.json"
    if not v.exists() or out.exists():
        continue
    segs, info = model.transcribe(str(v), word_timestamps=True, vad_filter=False, beam_size=3, condition_on_previous_text=False)
    words = []
    for s in segs:
        for w in (s.words or []):
            words.append({"w": w.word.strip(), "a": round(w.start, 2), "b": round(w.end, 2)})
    out.write_text(json.dumps({"language": info.language, "p": round(info.language_probability, 2), "words": words}, ensure_ascii=False), encoding="utf-8")
    print(cid, info.language, round(info.language_probability, 2), " ".join(f"{w['w']}[{w['a']}-{w['b']}]" for w in words), flush=True)
