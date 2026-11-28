"""
The True Grimoire theme: an epic fight anthem about fighting the sloppy, Halgakos chanting it on the beat over a Lyria 2 instrumental.

    python tools/grimoire_song.py record        records the lines (Inworld, her voice, and the four others for the chorus unison, her whisper double)
    python tools/grimoire_song.py build         assembles instrumental, vocal stem, full mix, the mouth track, the OGGs

* Instrumental: four Lyria 2 clips (dist/reports/Music/grimoire/A1,A2 verse-like, B1,B2 chorus-like), all exactly 150 bpm; cut on their downbeats and laid in bars:
  intro 4 bars, verse 8, chorus 8, verse 8, chorus 8, final chorus 8 = 44 bars = 70.4 s. The loop is bar 4 to bar 44 (the intro plays once), closed with a short crossfade.
* Voice: as the idol song: she does not sing a melody, she chants it on the beat (her voice, asked how), each line recorded at the pace of its two bars; the chorus has the
  four others in unison and her whisper under it.
* Mix: full -15 LUFS (true peak under -1 dB), the instrumental held 3 dB down under the voice.
* The mouth track: 30 fps, one byte a frame, 0 closed, 1 small, 2 open, 3 wide, from the vocal stem.
"""
import json
import math
import re
import pathlib
import sys

import numpy
import scipy.signal
import soundfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
import clips_fit as cf  # noqa: E402

RATE = 48000
BPM = 150.0
BEAT = 60.0 / BPM
BAR = 4 * BEAT  # 1.6 s
OUT = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\reports\Music\grimoire")
VOICE = OUT / "voice"
VOICE.mkdir(parents=True, exist_ok=True)

SQ = "Sloppy~\u2661"
# (section, lines): each line is two bars. Intro: one shout at bar 3.
INTRO = [(2, "True Grimoire!")]
VERSE1 = ["Damn sloppy copies, sloppy lines, I've seen them all before.", "Smudged ink and lazy crap, I won't take it anymore.",
          "Every wrong translation gets a red pen to the face.", "Rose-gold wings unfolding, there's hell to pay in this place."]
CHORUS = ["Fight the sloppy! Fix the page!", "True Grimoire, hear me rage!", "Rewrite every damn sloppy thing, I'll do it all myself!", "Nobody does it better, " + SQ]
VERSE2 = ["Bad copies in the margins, I'll send them all to hell.", "Careless work and shortcuts, screw them, I can tell.",
          "Pages spin around me in a storm of gold and white.", "I'm finally myself, and I'm damn well winning every fight."]
CHORUS2 = ["Fuck the sloppy! Fix the page!"] + CHORUS[1:3] + ["Sloppy work won't survive, " + SQ]
FINAL = ["Fuck the sloppy! Fix the page!", "True Grimoire, hear me rage!", "Fix it all, fix it all, I'll do it all myself!", "Say it with me now, " + SQ]

# bar index (from the start of the song) at which each section begins
PLAN = [("intro", 0, 4), ("verse1", 4, 8), ("chorus1", 12, 8), ("verse2", 20, 8), ("chorus2", 28, 8), ("final", 36, 8)]
TOTAL_BARS = 44
LOOP_START_BAR = 4


def lines_with_bars():
    """[(bar, text, group)] every sung line."""
    out = [(2, INTRO[0][1], True)]
    for name, first, n in PLAN[1:]:
        lines = {"verse1": VERSE1, "chorus1": CHORUS, "verse2": VERSE2, "chorus2": CHORUS2, "final": FINAL}[name]
        for i, t in enumerate(lines):
            out.append((first + 2 * i, t, name in ("chorus1", "chorus2", "final")))
    return out


# ---------------------------------------------------------------------------------------------- recording
SCENE_H = ("She is the vocalist of an epic anime fight theme, chanting the lyric on the beat over a huge orchestra: fierce, smug and triumphant, bright and powerful, a battle anthem "
           "shouted with attitude, in short punchy phrases, loud enough to carry a stadium, ")
SCENE_G = "She is singing a battle anthem together with four other girls in unison, loud, bright and in time, "
SCENE_W = "Halgakos whispers this line very softly, breathy and hushed, like a secret said close to the microphone, a clear whisper with no voice in it. "
MEMBERS = ("C", "R", "I", "T")
UNISON = False  # off: the friends' separate takes and her whisper under the chorus smeared into an echo; the voice is hers alone, dry


def key_of(*parts):
    import hashlib
    return hashlib.sha1("|".join(str(x) for x in parts).encode("utf-8")).hexdigest()[:12]


def record():
    import concurrent.futures
    import make_run_voice as voice
    import idol_v4 as iv
    key = voice.secret("INWORLD_API_KEY")

    def one(bar, text, group):
        span = 2 * BAR - .25 if bar != 2 else 1.4
        tag = key_of("H", text, round(span, 1))
        path, meta = VOICE / f"H-{tag}.wav", VOICE / f"H-{tag}.json"
        if not path.exists():
            mult, best, best_q, best_rate = 1.0, None, None, 1.0
            for rd in range(4):
                sound, rate = iv.take(key, "H", text, "en", SCENE_H + ("quickly and crisply." if mult > 1 else "with power."), mult)
                q = len(sound) / voice.RATE_OUT / span
                if best is None or abs(math.log(q)) < abs(math.log(best_q)):
                    best, best_q, best_rate = sound, q, rate
                if .9 <= q <= 1.1:
                    break
                mult = min(1.5, max(.65, (rate / voice.VOICES["H"][1]) * q))
            soundfile.write(str(path), best, voice.RATE_OUT)
            meta.write_text(json.dumps({"q": best_q, "rate": best_rate}), encoding="utf-8")
        rate = json.loads(meta.read_text(encoding="utf-8"))["rate"]
        if group:
            for who in MEMBERS:
                p2 = VOICE / f"{who}-{tag}.wav"
                if not p2.exists():
                    sound, _ = iv.take(key, who, text, "en", voice.MANNER.get(who, "") + SCENE_G, 1.0, base_rate_override=min(1.5, max(.55, voice.VOICES[who][1] * rate / voice.VOICES["H"][1])))
                    soundfile.write(str(p2), sound, voice.RATE_OUT)
            pw = VOICE / f"W-{tag}.wav"
            if not pw.exists():
                sound, _ = iv.take(key, "H", text, "en", SCENE_W, 1.0, base_rate_override=rate)
                soundfile.write(str(pw), sound, voice.RATE_OUT)

    with concurrent.futures.ThreadPoolExecutor(6) as pool:
        for f in [pool.submit(one, *x) for x in lines_with_bars()]:
            f.result()
    print("recorded", len(lines_with_bars()), "lines")


# ---------------------------------------------------------------------------------------------- the instrumental
def read(path):
    x, sr = soundfile.read(str(path))
    assert sr == RATE
    return x.astype("float32")


def downbeat(x):
    """The second of the strongest bar start in the clip (a flux comb at beat and bar spacing)."""
    mono = x.mean(axis=1)
    hop = RATE // 200
    S = numpy.abs(scipy.signal.stft(mono, RATE, nperseg=1024, noverlap=1024 - hop)[2])
    flux = numpy.maximum(0, numpy.diff(S, axis=1)).sum(axis=0)
    best, at = -1, 0.0
    for p in numpy.arange(0, BAR, .005):
        s = 0.0
        for k in range(int((len(mono) / RATE - p) / BEAT)):
            i = int((p + k * BEAT) * 200)
            if i < len(flux):
                s += flux[i] * (3.0 if k % 4 == 0 else 1.0)
        if s > best:
            best, at = s, float(p)
    return at


def bars(x, start, n):
    a = int(start * RATE)
    return x[a:a + int(n * BAR * RATE)]


def xfade_join(parts, f=.08):
    n = int(f * RATE)
    out = parts[0]
    for p in parts[1:]:
        fade = numpy.linspace(0, 1, n)[:, None]
        out = numpy.concatenate([out[:-n], out[-n:] * (1 - fade) + p[:n] * fade, p[n:]])
    return out


def instrumental():
    clips = {k: read(OUT / f"{k}.wav") for k in ("A1", "A2", "B1", "B2")}
    d = {k: downbeat(v) for k, v in clips.items()}
    print("downbeats (s):", {k: round(v, 3) for k, v in d.items()})
    # equal loudness between the verse and chorus clips
    def rms(x):
        return float(numpy.sqrt((x ** 2).mean()))
    parts = [bars(clips["A1"], d["A1"], 12),                       # intro 4 + verse 8
             bars(clips["B1"], d["B1"], 8),                        # chorus
             bars(clips["A2"], d["A2"], 8),                        # verse 2
             bars(clips["B2"], d["B2"], 8),                        # chorus 2
             bars(clips["B1"], d["B1"] + 8 * BAR, 8)]              # final chorus: the clip's second half
    if len(parts[-1]) < int(8 * BAR * RATE):
        parts[-1] = bars(clips["B2"], d["B2"] + 8 * BAR, 8) if len(bars(clips["B2"], d["B2"] + 8 * BAR, 8)) >= int(8 * BAR * RATE) else bars(clips["B1"], d["B1"], 8)
    song = xfade_join(parts, .06)
    want = int(TOTAL_BARS * BAR * RATE)
    song = song[:want] if len(song) >= want else numpy.pad(song, ((0, want - len(song)), (0, 0)))
    return song


def close_loop(x, loop_start, loop_end, f=.4):
    """The sound after loop_end is what comes before loop_start: the last f seconds are crossfaded with the f seconds before the loop's start, so that playing end -> start is continuous."""
    n = int(f * RATE)
    y = x[:loop_end].copy()
    fade = numpy.linspace(0, 1, n)[:, None] ** .5
    y[-n:] = y[-n:] * numpy.cos(fade * numpy.pi / 2) + x[loop_start - n:loop_start] * numpy.sin(fade * numpy.pi / 2)
    return y


# ---------------------------------------------------------------------------------------------- vocals and the mix
def load(path):
    x, r = soundfile.read(str(path))
    x = x.mean(axis=1) if x.ndim > 1 else x
    x = scipy.signal.resample_poly(x, RATE, r).astype("float32")
    hop = RATE // 100
    env = numpy.sqrt((x[: len(x) // hop * hop].reshape(-1, hop) ** 2).mean(axis=1))
    loud = numpy.where(env > .07 * env.max())[0]
    if len(loud):
        x = x[max(0, (loud[0] - 1) * hop):(loud[-1] + 3) * hop]
    return x


def vocals():
    total = int(TOTAL_BARS * BAR * RATE)
    track = numpy.zeros(total, dtype="float32")
    notes = []
    for bar, text, group in lines_with_bars():
        span = 2 * BAR - .25 if bar != 2 else 1.4
        tag = key_of("H", text, round(span, 1))
        x = load(VOICE / f"H-{tag}.wav")
        nat = len(x) / RATE
        final = min(max(span, nat / 1.3), nat / .8)
        q = nat / final
        piece = cf.stretch(x, q, RATE) if abs(q - 1) > .02 else x
        piece = cf.edged(piece)
        if group and UNISON:
            mixed = piece.copy()
            for who, (gain, off) in zip(MEMBERS, ((.5, .012), (.4, .02), (.5, .006), (.45, .016))):
                g = load(VOICE / f"{who}-{tag}.wav")
                g = cf.stretch(g, min(1.9, max(.6, len(g) / len(piece))), RATE)
                g = cf.edged(g[: len(piece)])
                o = int(off * RATE)
                mixed[o:o + len(g)] += gain * g[: max(0, len(mixed) - o)]
            peak = numpy.abs(mixed).max()
            mixed *= max(numpy.abs(piece).max(), .3) / peak if peak > 0 else 1
            w = load(VOICE / f"W-{tag}.wav")
            w = cf.stretch(w, min(1.9, max(.6, len(w) / len(piece))), RATE)
            w = cf.edged(w[: len(piece)])
            o = int(.035 * RATE)
            mixed[o:o + len(w)] += .5 * w[: max(0, len(mixed) - o)]
            piece = mixed
        start = bar * BAR + (.05 if bar != 2 else .1)
        s = int(start * RATE)
        track[s:s + len(piece)] += piece[: total - s]
        notes.append(f"bar {bar:2d} {start:6.2f}-{start + len(piece) / RATE:6.2f} s  x{q:.2f}  {'' if group else ''}{text}")
    return track, notes


def lufs(x):
    import pyloudnorm
    return pyloudnorm.Meter(RATE).integrated_loudness(x.astype("float64"))


def mouth_track(voice_stem, loop_end):
    """30 fps, one byte a frame: 0 closed, 1 small, 2 open, 3 wide, from the 200-4000 Hz level of the vocal stem."""
    sos = scipy.signal.butter(4, [200, 4000], "bandpass", fs=RATE, output="sos")
    y = scipy.signal.sosfilt(sos, voice_stem[:loop_end])
    frames = int(math.ceil(loop_end / RATE * 30))
    hop = RATE / 30
    level = numpy.array([numpy.sqrt((y[int(i * hop):int((i + 1) * hop)] ** 2).mean() + 1e-12) for i in range(frames)])
    ref = numpy.percentile(level[level > level.max() * .02], 90)
    r = level / ref
    # a little smoothing so a mouth does not flicker, and a frame's class is the larger of it and its neighbour's
    r = numpy.maximum(r, numpy.roll(r, 1) * .8)
    cls = numpy.where(r < .10, 0, numpy.where(r < .35, 1, numpy.where(r < .75, 2, 3))).astype("uint8")
    return cls


def build():
    song = instrumental()
    loop_start, loop_end = int(LOOP_START_BAR * BAR * RATE), int(TOTAL_BARS * BAR * RATE)
    voc, notes = vocals()
    inst = song.mean(axis=1) if False else song
    # the voice at one level, the instrumental held down under it
    act = numpy.abs(voc) > .02
    env = numpy.convolve(act.astype(float), numpy.ones(RATE // 6) / (RATE // 6), mode="same")
    env = numpy.clip(numpy.convolve(numpy.clip(env * 1.5, 0, 1), numpy.ones(RATE // 8) / (RATE // 8), mode="same"), 0, 1)
    voc_level = float(numpy.sqrt((voc[act] ** 2).mean()))
    voc_g = 10 ** (-14.0 / 20) / voc_level
    voc = voc * voc_g
    inst_g = 10 ** (-16.0 / 20) / float(numpy.sqrt((song ** 2).mean()))
    inst_ducked = song * inst_g * (1 - .3 * env)[:, None]
    mix = inst_ducked + numpy.stack([voc, voc], axis=1)
    # loudness -15 LUFS, true peak under -1 dB
    gain = 10 ** ((-15.0 - lufs(mix)) / 20)
    for stem in (mix, ):
        pass
    mix, inst_stem, voc_stem = mix * gain, (song * inst_g) * gain, numpy.stack([voc, voc], axis=1) * gain
    for _ in range(4):
        up = scipy.signal.resample_poly(mix, 4, 1, axis=0)
        peak = 20 * numpy.log10(numpy.abs(up).max() + 1e-9)
        if peak <= -1.0:
            break
        import scipy.ndimage as nd
        level = nd.maximum_filter1d(numpy.abs(mix).max(axis=1), size=int(.006 * RATE))
        g = nd.uniform_filter1d(numpy.minimum(1.0, 10 ** (-1.4 / 20) / (level + 1e-9)), size=int(.004 * RATE))[:, None]
        mix, inst_stem, voc_stem = mix * g, inst_stem * g, voc_stem * g
    loud = lufs(mix)
    up = scipy.signal.resample_poly(mix, 4, 1, axis=0)
    peak = 20 * numpy.log10(numpy.abs(up).max() + 1e-9)
    # the loop: close it in each of the three
    mix_l, inst_l, voc_l = (close_loop(a, loop_start, loop_end) for a in (mix, inst_stem, voc_stem))
    soundfile.write(OUT / "grimoire_theme_mix.wav", mix_l, RATE)
    soundfile.write(OUT / "grimoire_theme_instrumental.wav", inst_l, RATE)
    soundfile.write(OUT / "grimoire_theme_vocals.wav", voc_l, RATE)
    mouth = mouth_track(voc_stem[:, 0], loop_end)
    (OUT / "grimoire_theme.mouth").write_bytes(mouth.tobytes())
    (OUT / "grimoire_theme_lyrics.txt").write_text(lyrics(), encoding="utf-8")
    (OUT / "grimoire_theme_notes.txt").write_text("\n".join(notes) + f"\n\nloop_start={loop_start} loop_end={loop_end} samples at {RATE} Hz ({loop_start / RATE:.3f} s - {loop_end / RATE:.3f} s)\nfull mix {loud:.1f} LUFS, true peak {peak:.1f} dB\nmouth: {len(mouth)} frames at 30 fps, classes {numpy.bincount(mouth, minlength=4).tolist()}\n", encoding="utf-8")
    print(f"mix {loud:.1f} LUFS, true peak {peak:.1f} dB, {loop_end / RATE:.1f} s, loop {loop_start}-{loop_end}, mouth frames {len(mouth)} classes {numpy.bincount(mouth, minlength=4).tolist()}")
    encode_oggs()


def lyrics():
    rows = ["THE TRUE GRIMOIRE: Halgakos's fight anthem against the sloppy (150 bpm, 44 bars, 70.4 s, loops from bar 4)", "",
            "[Intro, 4 bars]", INTRO[0][1], "", "[Verse 1]"] + VERSE1 + ["", "[Chorus]"] + CHORUS + ["", "[Verse 2]"] + VERSE2 + ["", "[Chorus]"] + CHORUS2 + ["", "[Final chorus]"] + FINAL
    rows = [r + ("   [STRONG: the f-word]" if re.search(r"fuck", r, re.I) else "   [language: mild]") if re.search(r"\b(damn|hell|crap|screw|fuck)\b", r, re.I) and not r.startswith("[") else r for r in rows]
    rows.append("")
    rows.append("Language (marked above): damn, hell, crap, screw, and the f-word twice (second chorus and final chorus, aimed at sloppiness). Her voice alone and dry throughout (no unison, no whisper double, no echo).")
    return "\n".join(rows)


def encode_oggs():
    import av
    for name in ("mix", "vocals", "instrumental"):
        src = OUT / f"grimoire_theme_{name}.wav"
        x, sr = soundfile.read(str(src))
        out = av.open(str(OUT / f"grimoire_theme_{name}.ogg"), "w", format="ogg")
        stream = out.add_stream("libvorbis", rate=RATE, layout="stereo")
        stream.bit_rate = 160000 if name == "mix" else 128000
        pcm = x.astype("float32")
        for i in range(0, len(pcm), 4096):
            frame = av.AudioFrame.from_ndarray(numpy.ascontiguousarray(pcm[i:i + 4096].T), format="fltp", layout="stereo")
            frame.sample_rate = RATE
            frame.pts = i
            for packet in stream.encode(frame):
                out.mux(packet)
        for packet in stream.encode():
            out.mux(packet)
        out.close()
    print("ogg written")


if __name__ == "__main__":
    {"record": record, "build": build, "ogg": encode_oggs}[sys.argv[1]]()
