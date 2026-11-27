package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * What is in the gallery, the page behind the Cringe button on Plus run's map, and what
 * opens each thing in it. Everything is locked until it is opened: a locked thing shows a
 * lock and a word about where it opens, never itself.
 *
 * There are three shelves: the films; their storyboards and cast sheets; and the pictures,
 * which open as the story goes on. The films and what belongs to them are listed here. The
 * pictures are listed in one table, a file kept with them (gallery/pictures.tsv), so that
 * more of them are more rows in it and nothing else: a row is a picture's name, its title,
 * the word shown while it is locked, and its rule, with tabs between.
 *
 * A rule is one of: "film:N" (a film has been played: 1 the prologue, 2 the idol song, 4 the
 * clip of the Mother Tongue); "land:N" (land N has been won); "open:N" (land N has been
 * reached); "ending:N" (an ending has been seen: 1 the land unmade, 2 the true one, 4 the
 * Mother Tongue risen); "key:NAME:N" (something the game counts under a name has come to N).
 * All of these only ever become true, so nothing that has opened closes again.
 */
final class RunGallery {
    static final int VIDEOS = 0, BOARDS = 1, PICTURES = 2;
    static final String[] SHELVES = {"Videos", "Storyboards and cast", "Pictures"};
    /** What is kept: how many things were open when she last said so, and when the gallery was last looked into. */
    static final String TOLD = "run_gallery_count", LOOKED = "run_gallery_seen";

    /** One thing in the gallery. */
    static final class Item {
        final String name, title, hint, rule;
        final int shelf, film;

        Item(int shelf, String name, String title, String hint, String rule, int film) {
            this.shelf = shelf;
            this.name = name;
            this.title = title;
            this.hint = hint;
            this.rule = rule;
            this.film = film;
        }
    }

    private static final String FIRST = "After the first boss", TRUE = "The true ending", LAST = "In the last land";
    /** The films, and what belongs to them. */
    static final Item[] FIXED = {
            new Item(VIDEOS, "video_prologue", "Prologue", FIRST, "film:1", RunFilms.PROLOGUE),
            new Item(VIDEOS, "video_idol", "Idol song", TRUE, "film:2", RunFilms.IDOL),
            new Item(VIDEOS, "video_clip", "The Mother Tongue", LAST, "film:4", RunFilms.CLIP),
            new Item(BOARDS, "board_prologue", "Prologue: storyboard", FIRST, "film:1", RunFilms.NONE),
            new Item(BOARDS, "cast_prologue", "Prologue: cast", FIRST, "film:1", RunFilms.NONE),
            new Item(BOARDS, "board_idol_tall", "Idol song: storyboard, upright", TRUE, "film:2", RunFilms.NONE),
            new Item(BOARDS, "board_idol_wide", "Idol song: storyboard, wide", TRUE, "film:2", RunFilms.NONE),
            new Item(BOARDS, "cast_idol", "Idol song: stage costumes", TRUE, "film:2", RunFilms.NONE),
    };

    private RunGallery() {}

    /** The pictures in a table of them: a row each, "name, title, hint, rule" with tabs between; a row that is not that, or begins with #, is left out. */
    static List<Item> pictures(String table) {
        List<Item> items = new ArrayList<>();
        if (table == null) return items;
        for (String row : table.split("\n")) {
            if (row.startsWith("#")) continue;
            String[] parts = row.split("\t");
            if (parts.length < 4 || parts[0].trim().isEmpty() || !valid(parts[3].trim())) continue;
            items.add(new Item(PICTURES, parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), RunFilms.NONE));
        }
        return items;
    }

    /** Everything in the gallery: what is listed here, and then the pictures of a table. */
    static List<Item> all(String table) {
        List<Item> items = new ArrayList<>();
        java.util.Collections.addAll(items, FIXED);
        items.addAll(pictures(table));
        return items;
    }

    /** Everything in the gallery, with the clips of the story (RunClips) in the table kept with them as well. */
    static List<Item> all(String table, String clips) {
        List<Item> items = all(table);
        items.addAll(RunClips.items(RunClips.parse(clips)));
        return items;
    }

    /** Whether a rule is one that can be read. */
    static boolean valid(String rule) {
        String[] parts = rule.split(":");
        try {
            if (parts.length == 2 && (parts[0].equals("film") || parts[0].equals("land") || parts[0].equals("open") || parts[0].equals("ending"))) return Integer.parseInt(parts[1]) > 0;
            return parts.length == 3 && parts[0].equals("key") && !parts[1].isEmpty() && Integer.parseInt(parts[2]) > 0;
        } catch (NumberFormatException notANumber) {
            return false;
        }
    }

    /** Whether a thing is open, by what the game has kept. */
    static boolean open(Item item, Unlocks.Store kept) { return holds(item.rule, kept); }

    /** Whether what a rule asks has come about. */
    static boolean holds(String rule, Unlocks.Store kept) {
        if (!valid(rule)) return false;
        String[] parts = rule.split(":");
        int number = Integer.parseInt(parts[parts.length - 1]);
        switch (parts[0]) {
            case "film": return (kept.tally(RunFilms.SEEN) & number) != 0;
            case "land": return kept.tally("run_stars_" + number) > 0;
            case "open": return Math.max(1, kept.tally("run_open")) >= number;
            case "ending": return (kept.tally("run_ending") & number) != 0;
            default: return kept.tally(parts[1]) >= number;
        }
    }

    /** How many of some things are open. */
    static int open(List<Item> items, Unlocks.Store kept) {
        int count = 0;
        for (Item item : items) if (open(item, kept)) count++;
        return count;
    }
}
