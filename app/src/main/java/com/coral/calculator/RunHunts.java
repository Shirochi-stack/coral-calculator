package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * The hunts of the guild hall. Inju keeps a board of them: three are on it at a time.
 * One that is taken on is done over as many lands as it takes, and is handed in at any
 * guild hall, at which another takes its place on the board. There are four sorts: so
 * many of a kind of creature beaten; one of the three that are met in the middle of a
 * land beaten; a cellar cleared at a rank or better; and so many creatures beaten in
 * one way, by landing on them or with one thing she fights with. Only what she can do is
 * asked: creatures of the lands that are open to her, and ways she has the things for.
 * <p>
 * The more hunts she has handed in, the more a hunt asks and the better it pays: pluses
 * to keep, what the smith works with, and with every third a treat, for which the pet
 * shop gives a pet. Now and then a hunt handed in brings something besides: a pet of
 * her own, a place for one more pet, room for one more stray; and her rank in the guild
 * rises, whose badge is kept.
 * <p>
 * A hunt is one number, so that it is kept as it was offered whatever opens later.
 */
final class RunHunts {
    private RunHunts() {}

    /** How many are on the board at a time. */
    static final int SLOTS = 3;
    /** The sorts of them. */
    static final int OF_KIND = 0, ROVER = 1, CELLAR = 2, BY_WAY = 3;
    /** How many steps there are of what they ask and pay, and how many hunts handed in make a step. */
    static final int TIERS = 5, TIER_EVERY = 4;
    /** The first land with a cellar in it. */
    static final int CELLAR_FROM = 9;
    /** The way of landing on something; the way of a thing she fights with is its number and one. */
    static final int LANDING = 0;

    /** The kinds of creature a hunt may be for: what is counted as each, what they are called, the first land they are in, and how many are asked at first. */
    static final int[][] GROUPS = {{RunLevels.MINUS, RunLevels.THROWER}, {RunLevels.TIMES}, {RunLevels.SLIME}, {RunLevels.DRAGON}, {RunLevels.BAR}, {RunLevels.FAN},
            {RunLevels.POGO}, {RunLevels.DRIFTER}, {RunLevels.SWOOPER}, {RunLevels.BOMBER}, {RunLevels.SHOOTER}, {RunLevels.MITE}};
    private static final String[] GROUP_NAMES = {"minus signs", "times signs", "slimes", "punctuation dragons", "censor bars", "Heart Guards", "question marks", "comma moths",
            "caret swallows", "ellipsis clouds", "asterisks", "tittles"};
    static final int[] GROUP_FROM = {1, 13, 13, 31, 41, 31, 51, 21, 23, 26, 28, 32};
    private static final int[] GROUP_MANY = {16, 6, 16, 3, 6, 4, 6, 10, 8, 8, 6, 12};
    /** The three met in the middle of a land, in the order they are numbered from the wyrm: what each is called, and the first land it is in. */
    private static final String[] ROVER_NAMES = {"the Margin Wyrm", "Blot", "the Bookmark Gecko"};
    static final int[] ROVER_FROM = {22, 15, 20};
    /** The ways: how each is said, and how many are asked at first. */
    private static final String[] WAY_WORDS = {"by landing on them", "with the pen", "with a knife", "with a thrown book", "with the brush", "with the sword", "with the ink gun",
            "with the tablet-book", "with her keys"};
    private static final int[] WAY_MANY = {12, 8, 8, 6, 8, 8, 8, 10, 8};

    /** What the smith works with, which hunts pay: the names of the three. */
    static final String[] MATERIALS = {"Type metal", "Ink crystal", "Gold leaf"};

    /** Her ranks in the guild: how many hunts handed in each is had at, and what each is called. Its badge is hers to keep. */
    static final int[] RANK_AT = {1, 5, 12, 22, 35};
    static final String[] RANK_NAMES = {"Paper", "Vellum", "Leather-bound", "Gilt-edged", "Illuminated"};

    /** What a hunt handed in may bring besides its pay. */
    static final int NOTHING = 0, A_PET = 1, A_PLACE = 2, A_STRAY = 3;

    static int sort(int hunt) { return hunt & 3; }
    static int what(int hunt) { return hunt >> 2 & 63; }
    static int many(int hunt) { return hunt >> 8 & 1023; }
    static int tier(int hunt) { return hunt >> 18 & 7; }
    static boolean treat(int hunt) { return (hunt >> 21 & 1) != 0; }

    /** A hunt, from what it is. */
    static int of(int sort, int what, int many, int tier, boolean treat) {
        return sort | what << 2 | many << 8 | tier << 18 | (treat ? 1 << 21 : 0) | 1 << 24;
    }

    /** The step of what is asked and paid, from how many hunts have been handed in. */
    static int tierOf(int done) { return Math.min(TIERS - 1, done / TIER_EVERY); }

    /**
     * The hunt that goes up on the board: by how many have gone up before it, the furthest
     * land open to her, what she has to fight with (a bit for each thing, by its number),
     * how many hunts she has handed in, and what else is on the board, which it is not
     * the same as.
     */
    static int offer(int serial, int open, int owned, int done, int[] others) {
        int tier = tierOf(done);
        // Everything that may be asked, sort by sort; the sort that is this one's turn first.
        List<int[]> can = new ArrayList<>();
        int[] turns = {OF_KIND, BY_WAY, OF_KIND, CELLAR, ROVER, BY_WAY};
        int turn = turns[Math.floorMod(serial, turns.length)];
        for (int pass = 0; pass < 2; pass++) {
            for (int sort = 0; sort < 4; sort++) {
                if (sort == turn != (pass == 0)) continue;
                List<int[]> of = new ArrayList<>();
                if (sort == OF_KIND) {
                    for (int group = 0; group < GROUPS.length; group++) if (GROUP_FROM[group] <= open) of.add(new int[]{sort, group});
                } else if (sort == ROVER) {
                    for (int rover = 0; rover < 3; rover++) if (ROVER_FROM[rover] <= open) of.add(new int[]{sort, rover});
                } else if (sort == CELLAR) {
                    if (open >= CELLAR_FROM) of.add(new int[]{sort, tier >= 3 ? PlusRun.RANK_S : PlusRun.RANK_A});
                } else {
                    of.add(new int[]{sort, LANDING});
                    for (int weapon = 0; weapon < RunArms.COUNT; weapon++) if ((owned >> weapon & 1) != 0) of.add(new int[]{sort, weapon + 1});
                }
                // Which of a sort comes first goes round with the hunts that have gone up.
                int from = of.isEmpty() ? 0 : Math.floorMod(serial / turns.length * 5 + serial * 3, of.size());
                for (int index = 0; index < of.size(); index++) can.add(of.get((from + index) % of.size()));
            }
        }
        int[] chosen = can.get(0);
        for (int[] one : can) {
            boolean taken = false;
            for (int other : others) taken |= other != 0 && sort(other) == one[0] && what(other) == one[1];
            if (!taken) {
                chosen = one;
                break;
            }
        }
        int sort = chosen[0], what = chosen[1];
        // What it asks grows by half of what it asked at first with every step.
        int first = sort == OF_KIND ? GROUP_MANY[what] : sort == BY_WAY ? WAY_MANY[what] : 1, many = sort == OF_KIND || sort == BY_WAY ? first * (2 + tier) / 2 : 1;
        return of(sort, what, many, tier, Math.floorMod(serial, 3) == 2);
    }

    /** Whether something dealt with counts for a hunt: by its kind, and by the way of it. */
    static boolean counts(int hunt, int kind, int way) {
        if (kind == RunLevels.TARGET) return false;
        if (sort(hunt) == OF_KIND) {
            for (int one : GROUPS[what(hunt)]) if (one == kind) return true;
            return false;
        }
        return sort(hunt) == BY_WAY && way == what(hunt) && !RunLevels.boss(kind);
    }

    /** Whether one of the three beaten counts for it; and a cellar cleared at a rank. */
    static boolean countsRover(int hunt, int kind) { return sort(hunt) == ROVER && what(hunt) == kind - RunLevels.WYRM; }
    static boolean countsCellar(int hunt, int rank) { return sort(hunt) == CELLAR && rank >= what(hunt); }

    /** Whether anything can be done towards it in a land. */
    static boolean here(int hunt, RunLevels.Level level) {
        int sort = sort(hunt);
        if (sort == BY_WAY) return true;
        if (sort == CELLAR) {
            for (int[] room : level.rooms) if (room[0] == RunLevels.ARENA) return true;
            return false;
        }
        for (int[] foe : level.foes) {
            int kind = foe[0] % RunLevels.GIANT;
            if (sort == ROVER ? kind == RunLevels.WYRM + what(hunt) : counts(hunt, kind, -1)) return true;
        }
        return false;
    }

    /** What it asks, in words; and in a few, for where there is little room. */
    static String words(int hunt) {
        int sort = sort(hunt), what = what(hunt), many = many(hunt);
        return sort == OF_KIND ? "Beat " + many + " " + GROUP_NAMES[what] : sort == ROVER ? "Beat " + ROVER_NAMES[what]
                : sort == CELLAR ? "Clear a cellar at rank " + (what == PlusRun.RANK_S ? "S" : "A or better") : "Beat " + many + " creatures " + WAY_WORDS[what];
    }

    static String brief(int hunt) {
        int sort = sort(hunt), what = what(hunt);
        String words = sort == OF_KIND ? GROUP_NAMES[what] : sort == ROVER ? ROVER_NAMES[what] : sort == CELLAR ? "a cellar at rank " + (what == PlusRun.RANK_S ? "S" : "A")
                : WAY_WORDS[what];
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    /** What it pays in pluses to keep. */
    static int pluses(int hunt) {
        int sort = sort(hunt), tenths = sort == ROVER ? 20 : sort == CELLAR ? 15 : sort == BY_WAY ? 12 : 10;
        return (30 + 15 * tier(hunt)) * tenths / 50 * 5;
    }

    /** And in what the smith works with: how much of each of the three. */
    static int[] materials(int hunt) {
        int sort = sort(hunt), tier = tier(hunt);
        boolean big = sort == ROVER || sort == CELLAR;
        return new int[]{big ? 1 : 1 + tier / 2, big ? 1 + tier / 3 : tier >= 2 ? 1 : 0, sort == ROVER && tier >= 2 || sort == CELLAR && what(hunt) == PlusRun.RANK_S ? 1 : 0};
    }

    /** What it pays, in a line. */
    static String pays(int hunt) {
        StringBuilder line = new StringBuilder("+ " + pluses(hunt));
        int[] gives = materials(hunt);
        for (int index = 0; index < gives.length; index++) if (gives[index] > 0) line.append("  ·  ").append(gives[index]).append(' ').append(MATERIALS[index].toLowerCase(java.util.Locale.ROOT));
        if (treat(hunt)) line.append("  ·  a treat");
        return line.toString();
    }

    /** Her rank, from none, by how many hunts she has handed in. */
    static int rank(int done) {
        int rank = 0;
        for (int at : RANK_AT) if (done >= at) rank++;
        return rank;
    }

    /** What the hunt that makes it so many handed in brings besides its pay. */
    static int brings(int done) {
        return done == 4 || done == 18 ? A_PLACE : done == 10 || done == 28 ? A_STRAY : done % 6 == 3 ? A_PET : NOTHING;
    }

    /** The next count of hunts handed in at which something comes besides the pay, and what it is, in words. */
    static int nextAt(int done) {
        int at = done + 1;
        while (brings(at) == NOTHING && rank(at) == rank(at - 1)) at++;
        return at;
    }

    static String nextIs(int done) {
        int at = nextAt(done), brings = brings(at);
        String rank = rank(at) > rank(at - 1) ? "the " + RANK_NAMES[rank(at) - 1] + " badge" : "";
        String thing = brings == A_PET ? "a pet of her own" : brings == A_PLACE ? "a place for one more pet of hers" : brings == A_STRAY ? "room for one more stray" : "";
        return rank.isEmpty() ? thing : thing.isEmpty() ? rank : rank + ", and " + thing;
    }
}
