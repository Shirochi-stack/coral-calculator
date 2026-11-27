package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * The films Plus run shows: which there are, when one follows a land, which picture is its
 * picture on a screen of a given shape, which sound goes with it for what was chosen, and
 * what is written under it while it plays. Nothing here touches the phone: the names it
 * gives are those of files, which whoever plays a film looks for.
 *
 * A film is a picture without sound and a sound of its own, begun together. The prologue has
 * one picture, wider than tall; the idol song has two, one for an upright screen and one for
 * a wide one, and the one shown is chosen by the screen's shape as the film begins. Each has
 * three sounds: Halgakos saying its words (in English, and in Japanese, Korean and Chinese where
 * those have been recorded), the sound the model that drew the picture made with it, and Halgakos
 * as she first did it. The clip of the Mother Tongue is a film too, of the older kind: its
 * sound is in its file.
 */
final class RunFilms {
    static final int NONE = -1, PROLOGUE = 0, IDOL = 1, CLIP = 2;
    static final int HALGAKOS = 0, MODEL = 1, LEGACY = 2;
    static final int ENGLISH = 0, JAPANESE = 1, KOREAN = 2, CHINESE = 3, NO_CAPTIONS = 4;
    /** What is kept: which films have been played (a bit each), the sound chosen for each of the two, the language she speaks in them, and the language under them. */
    static final String SEEN = "run_films", SOUND = "run_film_sound_", TONGUE = "run_film_tongue", CAPTIONS = "run_film_captions";
    static final String[] TONGUES = {"en", "ja", "ko", "zh"};
    static final String[] TONGUE_NAMES = {"EN", "JP", "KR", "CN"};
    static final String[] TITLES = {"Prologue", "Idol song", "The Mother Tongue"};
    static final String[][] SOUND_NAMES = {{"Halgakos", "Original", "Halgakos, legacy"}, {"Halgakos", "MAGI-2", "Halgakos, legacy"}};
    /** What Halgakos says in the clip, in each language it can be written in under it. */
    static final String[] CLIP_SAYS = {RunStory.CUT_SAYS, "あんたが大いなる災いってわけ。マザー・タング Sloppy~♡", "네가 그 큰 재앙이구나. 마더 텅 Sloppy~♡", "原来你就是那个大灾祸。母语 Sloppy~♡"};
    /** From when the film's own title is on its picture, in seconds: what is written under it keeps clear of that. */
    static final float TITLE_FROM = 86f;

    private RunFilms() {}

    /** The film that follows a land won, if any: the prologue after the first land where someone waits, once; the idol song after the true ending, once. */
    static int due(int land, boolean won, int ending, int seen) {
        if (!won) return NONE;
        if (land == RunLevels.GUARD_LAND && (seen & bit(PROLOGUE)) == 0) return PROLOGUE;
        if (land == RunLevels.TOTAL && (ending & 2) != 0 && (seen & bit(IDOL)) == 0) return IDOL;
        return NONE;
    }

    /** A film's bit among those that have been played. */
    static int bit(int film) { return 1 << film; }

    /** Whether a screen is the upright kind: taller than it is wide. */
    static boolean upright(int width, int height) { return height > width; }

    /** The name of a film's picture for a screen of one kind or the other. */
    static String picture(int film, boolean upright) {
        return film == PROLOGUE ? "prologue" : upright ? "idol_tall" : "idol_wide";
    }

    /**
     * The names of the sound that goes with a film, the one wanted first and then what to fall back on: what was
     * chosen for it, in the language chosen if it is Halgakos, or in English where that language has not been recorded.
     * The model's own sound belongs to its picture, so the idol song's is the one for the shape shown.
     */
    static String[] sounds(int film, int choice, int tongue, boolean upright) {
        String base = film == PROLOGUE ? "prologue" : "idol";
        if (choice == MODEL) return new String[]{film == PROLOGUE ? "prologue_o" : upright ? "idol_tall_m" : "idol_wide_m"};
        if (choice == LEGACY) return new String[]{base + "_l"};
        if (tongue > ENGLISH && tongue < TONGUES.length) return new String[]{base + "_h_" + TONGUES[tongue], base + "_h"};
        return new String[]{base + "_h"};
    }

    /** The name of the file of what is written under a sound in a language, or null where nothing is written (captions off). */
    static String captions(String sound, int language) {
        return language < ENGLISH || language >= TONGUES.length ? null : "films/" + sound + "." + TONGUES[language] + ".txt";
    }

    /** One line of what is written under a film: from when to when, in seconds, and its words. */
    static final class Line {
        final float from, to;
        final String words;

        Line(float from, float to, String words) {
            this.from = from;
            this.to = to;
            this.words = words;
        }
    }

    /** The lines in a file of them: each "begins, ends, words", with tabs between. A line that is not that is left out. */
    static List<Line> lines(String text) {
        List<Line> lines = new ArrayList<>();
        if (text == null) return lines;
        for (String row : text.split("\n")) {
            String[] parts = row.split("\t", 3);
            if (parts.length < 3 || parts[2].trim().isEmpty()) continue;
            try {
                float from = Float.parseFloat(parts[0].trim()), to = Float.parseFloat(parts[1].trim());
                if (to > from) lines.add(new Line(from, to, parts[2].trim()));
            } catch (NumberFormatException notALine) {
                // Left out.
            }
        }
        return lines;
    }

    /** What is written at a moment, or null: the line that has begun and not ended; of two that overlap, the later. */
    static String at(List<Line> lines, float seconds) {
        String words = null;
        if (lines == null) return null;
        for (Line line : lines) {
            if (seconds >= line.from && seconds < line.to) words = line.words;
        }
        return words;
    }

    /**
     * How far down a film's picture the foot of what is written lies when it has to lie on the picture, from 0 at the top
     * to 1 at the bottom: on the upright one, over the names that come up at its lower left and over its title; on a wide
     * one, under them, at the very foot.
     */
    static float foot(boolean tallPicture) { return tallPicture ? .78f : .985f; }

    /** How large what is written is, in dots, for a picture shown so many dots wide: by the picture's shape, and never smaller than can be read. */
    static float size(float shownWide, boolean tallPicture, float least) {
        return Math.max(least, shownWide * (tallPicture ? .043f : .024f));
    }
}
