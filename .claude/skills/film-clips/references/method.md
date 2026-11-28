# Film clips: method details and lessons

## Where things are (Halgakos repo)

- Masters: `dist\cutscenes\clips\NN-name\NN-name-<model>.mp4` (Kling/Seedance/MAGI-2 generations, cut from the
  `gens\G1..G6.mp4` sequences); retakes in `gens\<NN>-retake\`.
- Per clip: `-bed.wav` (Demucs no-vocals), `-vocals.wav` (mouth map), `voice\v4-<lang>-<k>.wav` (our recordings).
- Transcripts: `dist\cutscenes\clips\transcripts\NN-name.json/.txt`.
- Line decisions: `tools\clips_v4.py` `PHRASES` (who, original span a-b, text in 4 languages, status, why).
- Review copies: `dist\cutscenes\clips\review\NN-name-LANG-vN.mp4`, table `vN-table.md`.
- Approved candidate set: `dist\cutscenes\clips\candidates\v4-candidate-1\` (do not touch).
- Share copies (full quality): `tools\share_copies.py`, CRF 21 veryslow tune animation, from the 1080p masters.
- Idol song: `tools\idol_*.py`, masters `dist\cutscenes\idol\idol-9x16.mp4` and `idol-16x9.mp4`.

## History: what failed, so it is not repeated

| Version | What was done | Owner's verdict |
|---|---|---|
| v2 | Lines fitted to speech-energy spans, coarse | 14 kept, 20 had missing voice, late starts, bad sync |
| v3 | Transcribed, but **kept the old English lines** and only moved them onto word times; JA/KO/ZH squeezed | "You completely ignored the request": voice starting before the mouth (clip 3), narration (clip 7), lines not matching the face (clip 5), missing voice (clip 11) |
| v4 | Every line re-decided from the transcript + frames, recorded fresh at the mouth's rate, word-level placement | "Actually amazing" / phenomenal; only the sound-effect choice and clip 4's ink splash remained |

Root causes found:

- "Missing voice" was **not** Demucs removing our voice: it was late first lines, coarse placement, a thin
  opening bed and no effects layer. Still, keep rule 7 (never run separation on our voice).
- Whisper stretches words across silence and hallucinates on music. Use the vocals-stem energy for spans.
- Squeezing an existing recording into a short mouth sounds wrong and still misses: re-record at the right pace.
- A line that names the picture or narrates the action reads as a caption, not dialogue.

## Placement maths (v4)

- Mouth phrases: speech-band energy envelope of the vocals stem (`idol_sync.envelope/on_mask/spans`), merged
  across gaps under ~150 ms.
- EN: split our line into words, give each a share of the phrase by syllable count, map onto the original's
  word onsets in order; time-stretch each segment within ×0.8–1.25 (pitch kept, atempo via PyAV).
- JA/KO/ZH: first sound on the phrase onset, whole phrase stretched into the span, ≤×1.25 target.
- Targets: onset error 0–90 ms (worst accepted 170 ms), offset within ~300 ms.

## Effects (v5 direction)

- The game's `res/raw/fight_s_*.ogg` set is too generic. Choose per visible action; generated or library effects
  are fine (log source and licence).
- Detect beats from the picture (motion peaks, flashes, cuts), then confirm each against the frames.
- Ink is a liquid: wet launch + splash/splat. Bats crack, pages rustle and flap, blades shing, slimes squelch,
  beams hum then roar, landings thud. Keep effects under voices (ducking) and away from the bed's own effects.

## Idol song specifics

- The original MAGI-2 singing is our lyrics sung: transcribe per shape (9x16, 16x9) and use it as the phrase map.
- Rewrite each phrase to the original's syllables and expression, record at the mouth's rate, place on real onsets.
- Keep: the BGM throughout (the instrumental, not the thin Demucs bed), five-voice unison in group shots, a
  whispered double under the crowd calls, her calls pinned on her mouths (0:23–0:30), and "Sloppy" always inside
  a line. Measure sync before/after with `idol_sync.py` (share of mouth-time hit).
- Nothing ships to the app until the owner approves the review copies.
