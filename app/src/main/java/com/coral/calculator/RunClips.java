package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * The short clips of Plus run: combat scenes of the story, each shown once where it belongs, before a land is
 * begun or after it is won. Which there are is a table kept with them (assets/clips/clips.tsv), so that one more
 * clip is one more row: its number, its title, the word shown in the gallery while it is locked, the land it
 * belongs to, and whether it is shown "before" that land or "after" it, with tabs between. A row that is not that,
 * or begins with #, is left out.
 *
 * A clip is a picture without sound (res/raw/run_clip_NN.mp4) and a sound of its own for each language Halgakos
 * can be heard in (run_clip_NN_h.m4a in English; _h_ja, _h_ko, _h_zh), begun together, with what is written under
 * it in a file for each pair of voice and language (assets/films/clip_NN_h[_ja].xx.txt), as the other films are.
 * That a clip has been seen is kept under its own name (run_clip_N), which is also what opens it in the gallery.
 */
final class RunClips {
    static final int NONE = -1;
    /** The number a clip's film has among the films of the gallery's items: its own number is added to this. */
    static final int BASE = 100;
    static final String SEEN = "run_clip_";

    /** One row of the table. */
    static final class Clip {
        final int number;
        final String title, hint;
        final int land;
        final boolean before;

        Clip(int number, String title, String hint, int land, boolean before) {
            this.number = number;
            this.title = title;
            this.hint = hint;
            this.land = land;
            this.before = before;
        }
    }

    private RunClips() {}

    /** The clips in a table of them. */
    static List<Clip> parse(String table) {
        List<Clip> clips = new ArrayList<>();
        if (table == null) return clips;
        for (String row : table.split("\n")) {
            if (row.startsWith("#")) continue;
            String[] parts = row.split("\t");
            if (parts.length < 5) continue;
            try {
                int number = Integer.parseInt(parts[0].trim()), land = Integer.parseInt(parts[3].trim());
                String when = parts[4].trim();
                if (number > 0 && land > 0 && (when.equals("before") || when.equals("after"))) {
                    clips.add(new Clip(number, parts[1].trim(), parts[2].trim(), land, when.equals("before")));
                }
            } catch (NumberFormatException notARow) {
                // Left out.
            }
        }
        return clips;
    }

    /** The next clip that belongs to a land at a moment (before it, or after it was won) and has not been seen, or NONE. */
    static int due(List<Clip> clips, int land, boolean before, Unlocks.Store kept) {
        for (Clip clip : clips) {
            if (clip.land == land && clip.before == before && kept.tally(SEEN + clip.number) == 0) return clip.number;
        }
        return NONE;
    }

    /** What the number of a clip is called in the names of its files: two figures. */
    static String two(int number) { return number < 10 ? "0" + number : String.valueOf(number); }

    /** The name of a clip's picture among the raw resources. */
    static String picture(int number) { return "run_clip_" + two(number); }

    /**
     * The names of the sound of a clip, the one wanted first and then what to fall back on: in the language chosen if it has
     * been recorded, and in English if not. (Without the "run_" in front: that is put to them as they are looked for.)
     */
    static String[] sounds(int number, int tongue) {
        String base = "clip_" + two(number) + "_h";
        if (tongue > RunFilms.ENGLISH && tongue < RunFilms.TONGUES.length) return new String[]{base + "_" + RunFilms.TONGUES[tongue], base};
        return new String[]{base};
    }

    /** The gallery's items for the clips: a film of each, and its storyboard, both opened by the clip having been seen. */
    static List<RunGallery.Item> items(List<Clip> clips) {
        List<RunGallery.Item> items = new ArrayList<>();
        for (Clip clip : clips) {
            String rule = "key:" + SEEN + clip.number + ":1";
            items.add(new RunGallery.Item(RunGallery.VIDEOS, "video_clip" + two(clip.number), clip.title, clip.hint, rule, BASE + clip.number));
            items.add(new RunGallery.Item(RunGallery.BOARDS, "board_clip" + two(clip.number), clip.title + ": storyboard", clip.hint, rule, RunFilms.NONE));
        }
        return items;
    }
}
