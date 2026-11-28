---
name: film-clips
description: Edit AI-generated anime cutscene clips (Kling, Seedance, MAGI-2) into finished, voiced, lip-synced films in EN/JA/KO/ZH, the way Halgakos: Sloppy Copy's 34 combat clips were made (v4, which the owner called "phenomenal"). Use when asked to voice, re-voice, lip-sync, fix voiceover, add or fix sound effects, redo, retake, translate, or polish a generated video clip, cutscene, film or idol song; or to make share copies of one. Covers transcribing the model's own speech, rewriting/removing lines to fit mouths and faces, Inworld recording, word-level placement, effects, mix, loudness, seamless lengthening, MAGI-2 retakes, verification tables and delivery.
---

# Film clips: generated video to finished voiced cutscene

The proven method behind the Halgakos combat clips (34 clips, v4, 2026-11-28). Tools are in the game repo
`C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\tools\` (`clips_*.py`, `idol_*.py`); data and outputs in
`dist\cutscenes\clips\`. Read [references/method.md](references/method.md) for each step's details and the
mistakes that were made on the way, before changing anything.

## The core insight

The video model was prompted with our script, so **its own generated speech is the mouth's timing map**: where
its voice is, the mouth moves, word by word. Separate it (Demucs), transcribe it with word times (Whisper), and
build our lines on that map. Everything else follows from this.

## Owner rules (non-negotiable)

1. **Never just retime.** Every line is decided again against the picture: KEPT (with the reason), REWRITTEN
   (old text, new text, why) or REMOVED (why). Fewer, better lines beat a line in every gap. A clip may end with
   one line, only a reaction sound, or no voice.
2. **No voice without a visible mouth.** Check the frames at each phrase. Whisper invents words over music; the
   vocals stem plus the frames decide, never the transcript alone. A face hidden behind a book or magic means no line.
3. **The line fits the face.** Note her expression at each phrase (grin, shock, tears, fury) and write to it. No
   "Careful!" on a grin.
4. **Never narrate actions or name what the picture shows.** "Jump on her after she swings", "A heavy book",
   "Back to back": prompt leaks or captions, not speech. Write what she would *say*.
5. **Never start before the mouth.** Each line's first word sits on the original's onset (aim for ±60 ms, never
   early; the user notices a voice starting before lips move).
6. **Catchphrases are optional in clips** ("Sloppy~♡", Tsubakieh's "Normally"): drop them when lip sync needs
   it. **In the idol song, "Sloppy" stays**, but always inside a real line, never as a lone word.
7. **Our voice is never put through voice removal.** Demucs runs only on the generated video's own audio; our
   lines are mixed on top afterwards.
8. **All four languages at parity** (EN, JA, KO, ZH): own lines per language, written to the original phrase's
   syllable/mora count, recorded fresh, own captions, same effects and mix.
9. **Sound effects must match what is on screen** (ink → wet splash/splat, bat → wooden crack, pages → paper
   rustle, beam → shimmer/roar, slime → squelch, blade → metal shing), timed to the frame of the action. No
   generic fight-set filler; no action stretch over ~1.5 s without one. (v4's weakness was effect choice; clip 4
   lacked a splash for Rune's ink.)
10. **Quality**: 1080p, encoded once from the generated master (x264, CRF ~20, high, never from a phone/720p copy),
    smaller than the master, -18 LUFS integrated, true peak under -1.5 dBTP, no silent opening. **Keep every original
    and every approved version untouched**; new work gets a new version suffix (`-v5`), and approved sets get backed
    up to `dist\cutscenes\clips\candidates\` when the owner says so.
11. **Can't listen? Say so.** Report measurements and frame checks, never claim it sounds right.

## Pipeline (one clip, every language)

1. **Separate**: `clips_bed.py PICTURE.mp4 OUT-bed.wav` in a venv with demucs+torch (Demucs htdemucs) → `ID-bed.wav` (effects bed) and `ID-vocals.wav` (mouth map).
2. **Transcribe**: `clips_transcribe.py` (faster-whisper, word timestamps) → `transcripts\ID.json/.txt`.
3. **Verify mouths**: mouth spans from the vocals-stem energy, not Whisper's word ends; frame montages at each
   phrase; mark speaker (whose mouth) and expression.
4. **Decide lines** (rules 1–4, 6): one line per real mouth phrase, original words when they make sense
   (garbles mended: "Helgocco" → Halgakos), else rewritten to the same phrase breaks and syllable count.
5. **Record** each line fresh in the speaker's Inworld voice (`tools/make_run_voice.py` VOICES: H Halgakos
   `community-5fwhmdpiq45g`, C Chikuwa `community-iyfnceehtmbk`, R Rune the designed voice
   `upbeat-bee-0979__design-voice-02fb12a5`, I Illya `community-blcuaurhzmvi`, T Tsubakieh `community-pwotyqkmtlsi`,
   M the Mawang = H's voice at pitch .84, f the Mother Tongue). `python tools/clips_v4.py record` then `refit`:
   `refit` re-says each line with speakingRate scaled by (take length / mouth span) until the take is within
   0.9-1.1 of the mouth (up to 4 takes, nearest kept; TTS varies by +-30% between takes, so keep the best, never
   the last). Saving designed voices to the owner's Inworld account is pre-approved. Key by name only.
6. **Place**: EN word by word onto the original's words by syllable share (each segment ×0.8–1.25); JA/KO/ZH
   phrase by phrase from the original onset (≤×1.25, ×1.35 hard cap, flag anything over 1.25).
7. **Lengthen only as a last resort**: slow the picture inside one shot by ≤1.5× with motion-compensated
   in-between frames (DIS optical flow, both frames warped and blended), never interpolate across a cut (a
   frame-to-frame jump over 35 = cut; hold the frame before it), music extended by repeating bars, never
   stretched; effects re-timed onto their action. Check every lengthened stretch frame by frame.
8. **Effects** (rule 9): `tools/clips_sfx.py` synthesises the library (original, no licence) and holds `CUES`, a
   hand-made cue list per clip, timed from frame montages at each second; `FX5=1 python tools/clips_v3.py NN --langs en,ja,ko,zh` builds with them (v5 files).
9. **Mix**: bed + effects + faint room tone + game instrumental ducked under voices; loudness and peak as rule 10.
10. **Encode** once from the master with that language's captions burned in.
11. **Verify and table** (`python tools/clips_v4_report.py`, needs the voice dumps `review\v4-voice\`): per clip/language/line: status, speaker, face, original words and
    times, our line, placed time, onset and offset error, voice-with-no-mouth and mouth-with-no-voice seconds.
    Fix anything over target before reporting.

## Retakes (when the picture itself is wrong)

Continuity errors (e.g. still holding the ice cream after dropping it) need a new generation, not an edit.
MAGI-2 on Higgsfield in the owner's Chrome: Frames mode, the clip's original start frame (extract it from the
master), 10 s, 16:9, 1080p, sound on, 25 credits a take. Spell out the continuity in the prompt ("from that
moment her hands are EMPTY…"). Check a frame every second before accepting; ask before downloading. Close
promo pop-ups without upgrading. The retake becomes the new master and goes through the whole pipeline.

## Reporting to the owner

Send the EN versions first (as attachments; over 30 MB won't reach the phone). Give a short per-clip table of
what changed. List exactly what was not checked. The owner reviews clip by clip; record each verdict
(keep / lip sync / missing voice / redo) and keep approved files untouched.
