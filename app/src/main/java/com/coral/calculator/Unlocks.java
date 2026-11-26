package com.coral.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * What playing the games unlocks, in secret: more things for Halgakos to say while she keeps
 * the user company, and more tricks for "Prank me". Each has a condition on what the games
 * remember; when one comes to hold it is unlocked and stays so. Nothing here lists what is
 * still locked: only how many there are.
 *
 * The lines are in {@link UnlockLines}, by game; the tricks are drawn by {@link Trick}.
 * Lines and tricks are known by their place in their lists, so new ones go at the end.
 */
final class Unlocks {
    /** What the games remember, and where what is unlocked is kept. */
    interface Store {
        int tally(String key);
        String note(String key);
        void keepNote(String key, String value);
    }

    /** The games that unlock lines, in the order of {@link UnlockLines#ALL}. */
    static final int RUN = 0, FIGHT = 1, TIC = 2, RPS = 3, PAIRS = 4, SUMS = 5, TAG = 6, RAIN = 7, ORDER = 8, CHESS = 9, MINES = 10, CARDS = 11;
    static final String[] GROUPS = {"run", "fight", "tic", "rps", "pairs", "sums", "tag", "rain", "order", "chess", "mines", "cards"};
    /** The lands of Plus run that a condition may count over. */
    private static final int LANDS = 100;
    /** How many lines newly unlocked wait to be said first. */
    private static final int FRESH = 40;
    private static final String FRESH_LINES = "unlock_fresh", TRICKS_HAD = "unlock_tricks", FRESH_TRICKS = "unlock_fresh_tricks";

    /** The first of the numbers the tricks have on a {@link PlayStage}, after everything else that can be on one. */
    static final int FIRST_TRICK = 100;
    /** The tricks: twenty from Plus run, six from Page brawl, and one from each of the ten small games. */
    static final int BOOK = 0, BAT = 1, REWRITE = 2, STAMP = 3, SLIME = 4, SHADOW = 5, COLOURS = 6, STALL = 7, ARENA = 8, NOTHING = 9,
            GAZETTE = 10, FAN = 11, NAP = 12, CAKES = 13, DOOR = 14, INK_GUN = 15, FLIGHT = 16, DIVE = 17, RETREAT = 18, LOST_WORDS = 19,
            KEYS = 20, BRICK = 21, BOOMERANG = 22, FLOOD = 23, BEAM = 24, CLASH = 25,
            GRID = 26, HANDS = 27, FLIPPED = 28, MARKING = 29, DOUBLES = 30, DOWNPOUR = 31, COUNTING = 32, KNIGHT = 33, FIELD = 34, DEAL = 35;
    static final String[] TRICK_NAMES = {"The World's Largest Book", "Normal No. 2", "Heroine: Illya", "Form 12-B", "King Slime", "Her own shadow",
            "Every colour", "Altteul's stall", "Locked in", "Times nothing", "Extra, extra", "It is being said", "Five more minutes",
            "Three rice cakes", "A draught from the crack", "Ink gun", "Green ribbon", "Between the lines", "The retreat", "Lost words",
            "Plenty of keys", "Brick bread", "It comes back", "Through tears", "Heart beam", "Tap battle",
            "Noughts and crosses", "Rock, paper, scissors", "Face down", "Marking", "Catch the real one", "Number rain", "In order",
            "Knight takes", "Minefield", "Last card"};
    /** What unlocks each. The two before Page brawl's are those of the endings of land 100: they are kept for, and not drawn yet. */
    static final String[] TRICK_NEEDS = {
            "run_stars_1", "run_prize_24", "run_prize_18", "q(1-100)>=3", "run_prize_70", "run_prize_80", "@colours>=3", "c(1-100)>=10",
            "run_prize_6", "run_prize_30", "run_prize_50", "run_prize_40", "run_prize_60", "q(1-100)>=1", "r(1-100)>=1", "run_open>=14",
            "run_colours&8", "run_prize_55", "run_ending&1", "run_ending&2",
            "fight_cleared_0|fight_wins_0>=3", "fight_cleared_1|fight_beat_1>=3", "fight_cleared_2|fight_beat_2>=3",
            "fight_cleared_3|fight_beat_3>=3", "fight_cleared_4|fight_beat_4>=3", "fight_clash|fight_won>=5",
            "tic_won>=3", "rps_won>=3", "pairs_done>=2", "sums_best>=10", "tag_best>=10", "rain_best>=15", "order_played>=3",
            "chess_won", "mines_won", "cards_won>=3"};
    static final int TRICKS = TRICK_NEEDS.length;
    /** How many of the tricks come from Plus run and from Page brawl; the rest are one to a small game. */
    static final int RUN_TRICKS = 20, FIGHT_TRICKS = 6;

    private Unlocks() {}

    /** Whether a number on a stage is one of these tricks. */
    static boolean isTrick(int game) { return game >= FIRST_TRICK && game < FIRST_TRICK + TRICKS; }

    /** Whether a trick has been drawn yet: the two of the endings wait for the dragon they are about. */
    static boolean drawn(int trick) { return trick != RETREAT && trick != LOST_WORDS; }

    // Conditions

    /**
     * Whether a condition holds. It is alternatives divided by "|", each a list of requirements
     * divided by ","; a requirement is a name, a sign and a number, as in {@code tic_won>=3},
     * and a name alone asks for one or more. "&amp;" asks that the two have a bit in common. A name is a number the games keep,
     * or a count over lands of Plus run: {@code c(1-30)} is how many of lands 1 to 30 are cleared,
     * {@code t} with three stars, {@code s} their stars, {@code q} with a quest sealed, {@code r}
     * the places found off them and {@code d} those finished. {@code @colours} is how many colours
     * of hair are kept, {@code @ups} what was bought at the guild counter, {@code @prizes} how many
     * bosses have given theirs, and {@code @champs} how many fighters have topped the ladder.
     */
    static boolean holds(String condition, Store store) { return holds(condition, store, null); }

    private static boolean holds(String condition, Store store, Map<String, Integer> counted) {
        int from = 0;
        while (from <= condition.length()) {
            int bar = condition.indexOf('|', from);
            if (bar < 0) bar = condition.length();
            if (all(condition, from, bar, store, counted)) return true;
            from = bar + 1;
        }
        return false;
    }

    private static boolean all(String condition, int from, int to, Store store, Map<String, Integer> counted) {
        while (from < to) {
            int comma = condition.indexOf(',', from);
            if (comma < 0 || comma > to) comma = to;
            if (!one(condition.substring(from, comma), store, counted)) return false;
            from = comma + 1;
        }
        return true;
    }

    private static boolean one(String requirement, Store store, Map<String, Integer> counted) {
        int sign = 0;
        while (sign < requirement.length() && "<>=&".indexOf(requirement.charAt(sign)) < 0) sign++;
        String name = requirement.substring(0, sign);
        int have = value(name, store, counted);
        if (sign == requirement.length()) return have >= 1;
        int number = sign + 1;
        if (number < requirement.length() && requirement.charAt(number) == '=') number++;
        String how = requirement.substring(sign, number);
        int asked = Integer.parseInt(requirement.substring(number));
        switch (how) {
            case ">=": return have >= asked;
            case "<=": return have <= asked;
            case ">": return have > asked;
            case "<": return have < asked;
            case "=": return have == asked;
            default: return (have & asked) != 0;
        }
    }

    private static int value(String name, Store store, Map<String, Integer> counted) {
        boolean plain = name.indexOf('(') < 0 && name.charAt(0) != '@';
        if (plain) return store.tally(name);
        if (counted != null) {
            Integer had = counted.get(name);
            if (had != null) return had;
        }
        int value = 0;
        if (name.charAt(0) == '@') {
            switch (name) {
                case "@colours": value = Integer.bitCount(store.tally("run_colours") & 255); break;
                case "@ups": for (int which = 0; which < 12; which++) value += store.tally("run_up_" + which); break;
                case "@prizes": for (int land = 1; land <= LANDS; land++) if (store.tally("run_prize_" + land) != 0) value++; break;
                case "@champs": for (int who = 0; who < 8; who++) if (store.tally("fight_cleared_" + who) != 0) value++; break;
                default: throw new IllegalArgumentException(name);
            }
        } else {
            int dash = name.indexOf('-'), first = Integer.parseInt(name.substring(2, dash)), last = Integer.parseInt(name.substring(dash + 1, name.length() - 1));
            for (int land = first; land <= last; land++) {
                switch (name.charAt(0)) {
                    case 'c': if (store.tally("run_stars_" + land) > 0) value++; break;
                    case 't': if (store.tally("run_stars_" + land) >= 3) value++; break;
                    case 's': value += store.tally("run_stars_" + land); break;
                    case 'q': if (store.tally("run_quest_" + land) != 0) value++; break;
                    case 'r': value += Integer.bitCount(store.tally("run_rooms_" + land)); break;
                    case 'd': value += Integer.bitCount(store.tally("run_roomsdone_" + land)); break;
                    default: throw new IllegalArgumentException(name);
                }
            }
        }
        if (counted != null) counted.put(name, value);
        return value;
    }

    // What is unlocked

    /** The lines of a game, each with what unlocks it before it. */
    static String[][] lines(int group) { return UnlockLines.ALL[group]; }

    /** How many lines there are to unlock, in all. */
    static int lines() {
        int count = 0;
        for (String[][] group : UnlockLines.ALL) count += group.length;
        return count;
    }

    private static String had(Store store, int group) {
        String had = store.note("unlock_" + GROUPS[group]);
        return had == null ? "" : had;
    }

    private static boolean has(String had, int place) { return place < had.length() && had.charAt(place) == '1'; }

    private static String with(String had, int place) {
        StringBuilder all = new StringBuilder(had);
        while (all.length() <= place) all.append('0');
        all.setCharAt(place, '1');
        return all.toString();
    }

    private static int count(String had) {
        int count = 0;
        for (int place = 0; place < had.length(); place++) if (had.charAt(place) == '1') count++;
        return count;
    }

    /** How many lines have been unlocked. */
    static int found(Store store) {
        int count = 0;
        for (int group = 0; group < GROUPS.length; group++) count += Math.min(count(had(store, group)), lines(group).length);
        return count;
    }

    /** How many tricks have been unlocked. */
    static int tricksFound(Store store) { return count(trickNote(store)); }

    private static String trickNote(Store store) {
        String had = store.note(TRICKS_HAD);
        return had == null ? "" : had;
    }

    /**
     * Looks at what the games remember, and unlocks whatever has come to hold: it is kept, and
     * what is new waits to be said, or played, first.
     *
     * @param testing in a build made for testing, {@code unlock_all} set to 1 unlocks everything
     *                and {@code unlock_none} set to 1 locks everything again
     * @return how many lines and tricks were unlocked by this look
     */
    static int check(Store store, boolean testing) {
        boolean everything = testing && store.tally("unlock_all") == 1;
        if (testing && store.tally("unlock_none") == 1) {
            for (int group = 0; group < GROUPS.length; group++) store.keepNote("unlock_" + GROUPS[group], "");
            store.keepNote(FRESH_LINES, "");
            store.keepNote(TRICKS_HAD, "");
            store.keepNote(FRESH_TRICKS, "");
            return 0;
        }
        Map<String, Integer> counted = new HashMap<>();
        List<String> fresh = new ArrayList<>();
        int unlocked = 0;
        for (int group = 0; group < GROUPS.length; group++) {
            String had = had(store, group), now = had;
            String[][] lines = lines(group);
            for (int place = 0; place < lines.length; place++) {
                if (has(now, place) || !(everything || holds(lines[place][0], store, counted))) continue;
                now = with(now, place);
                fresh.add(group + ":" + place);
                unlocked++;
            }
            if (!now.equals(had)) store.keepNote("unlock_" + GROUPS[group], now);
        }
        if (!fresh.isEmpty()) {
            // The newest come first, and only so many wait: the rest are met as any other line is.
            // Where one sitting unlocked a great many, every few of them waits, and not just the first of them.
            List<String> kept = new ArrayList<>();
            int step = Math.max(1, fresh.size() / FRESH);
            for (int index = 0; index < fresh.size() && kept.size() < FRESH; index += step) kept.add(fresh.get(index));
            String waiting = store.note(FRESH_LINES);
            if (waiting != null) {
                for (String one : waiting.split(",")) if (!one.isEmpty() && kept.size() < FRESH) kept.add(one);
            }
            store.keepNote(FRESH_LINES, String.join(",", kept));
        }
        String tricks = trickNote(store), have = tricks;
        StringBuilder next = null;
        for (int trick = 0; trick < TRICKS; trick++) {
            if (has(have, trick) || !(everything || holds(TRICK_NEEDS[trick], store, counted))) continue;
            have = with(have, trick);
            if (next == null) next = new StringBuilder();
            next.append(trick).append(',');
            unlocked++;
        }
        if (next != null) {
            store.keepNote(TRICKS_HAD, have);
            String waiting = store.note(FRESH_TRICKS);
            store.keepNote(FRESH_TRICKS, next + (waiting == null ? "" : waiting));
        }
        return unlocked;
    }

    /** Whether lines newly unlocked are waiting to be said. */
    static boolean fresh(Store store) {
        String waiting = store.note(FRESH_LINES);
        return waiting != null && !waiting.isEmpty();
    }

    /** One of the lines newly unlocked, which then no longer waits; or null if none does. */
    static String freshLine(Store store, Random random) {
        String waiting = store.note(FRESH_LINES);
        if (waiting == null || waiting.isEmpty()) return null;
        String[] all = waiting.split(",");
        // One of the newest few, so that what one game unlocked is not said in the order it is written in.
        int pick = random.nextInt(Math.min(all.length, 6));
        StringBuilder left = new StringBuilder();
        for (int index = 0; index < all.length; index++) if (index != pick && !all[index].isEmpty()) left.append(all[index]).append(',');
        store.keepNote(FRESH_LINES, left.toString());
        int colon = all[pick].indexOf(':');
        if (colon < 0) return null;
        try {
            int group = Integer.parseInt(all[pick].substring(0, colon)), place = Integer.parseInt(all[pick].substring(colon + 1));
            return group < GROUPS.length && place < lines(group).length ? lines(group)[place][1] : null;
        } catch (NumberFormatException spoilt) {
            return null;
        }
    }

    /** Any one of the lines unlocked, or null if there are none. */
    static String anyLine(Store store, Random random) {
        int found = found(store);
        if (found == 0) return null;
        int pick = random.nextInt(found);
        for (int group = 0; group < GROUPS.length; group++) {
            String had = had(store, group);
            int most = lines(group).length;
            for (int place = 0; place < had.length() && place < most; place++) {
                if (had.charAt(place) == '1' && pick-- == 0) return lines(group)[place][1];
            }
        }
        return null;
    }

    /**
     * How often, of the times she says something of her own accord, it is to be one of the
     * unlocked lines: more as more are unlocked, and most while new ones wait.
     */
    static float share(Store store) {
        if (fresh(store)) return .7f;
        int found = found(store);
        return found == 0 ? 0 : Math.min(.5f, found / (found + 180f));
    }

    /** The tricks unlocked that can be played, as their numbers on a stage. */
    static List<Integer> tricks(Store store) {
        List<Integer> all = new ArrayList<>();
        String had = trickNote(store);
        for (int trick = 0; trick < TRICKS && trick < had.length(); trick++) {
            if (had.charAt(trick) == '1' && drawn(trick)) all.add(FIRST_TRICK + trick);
        }
        return all;
    }

    /** A trick newly unlocked, as its number on a stage, which then no longer waits; or 0 if none does. */
    static int freshTrick(Store store) {
        String waiting = store.note(FRESH_TRICKS);
        while (waiting != null && !waiting.isEmpty()) {
            int comma = waiting.indexOf(',');
            String first = comma < 0 ? waiting : waiting.substring(0, comma);
            waiting = comma < 0 ? "" : waiting.substring(comma + 1);
            store.keepNote(FRESH_TRICKS, waiting);
            try {
                int trick = Integer.parseInt(first);
                if (trick >= 0 && trick < TRICKS && drawn(trick)) return FIRST_TRICK + trick;
            } catch (NumberFormatException spoilt) {
                // The next one, then.
            }
        }
        return 0;
    }
}
