package com.coral.calculator;

import java.util.Random;

/**
 * The Vampire Merchant, who keeps a stall on some floors of the dungeons and nowhere else: what she thinks of Halgakos (an affinity from
 * nought to a hundred, shown as hearts, raised by gifts she likes and lowered by those she does not, and by blows), what she charges (less
 * the more she likes her, more if she has been struck), how she takes being struck (three warnings, each colder, before a fight; they are
 * forgotten after a calm), and the fight itself (she cannot die, and mends, until Halgakos is strong enough late in the game). Her name is
 * not known until the wedding. What is kept between plays goes through a {@link Store}, so that it can be tried without the game.
 */
final class RunLilith {
    private RunLilith() {}

    /** Where what is kept is kept: the game's prefs, or a map in a test. */
    interface Store {
        int get(String key);

        void put(String key, int value);
    }

    /** What she is called before the wedding, and after. */
    static final String TITLE = "the Vampire Merchant", NAME = "Lilith";

    static final String AFFINITY = "run_lil_aff", MET = "run_lil_met", GRUDGE = "run_lil_grudge", ROUTE = "run_lil_route", GIVEN = "run_lil_given_", HAVE = "run_lil_have_";
    /** What sparing her, at the last of her health, raises her affinity by. */
    static final int SPARED = 10;
    /** Her route: none yet; she was beaten, and Halgakos took the vampire's way; they were wed. */
    static final int NO_ROUTE = 0, VAMPIRE = 1, WED = 2;
    static final int MOST = 100;

    static int affinity(Store store) { return Math.max(0, Math.min(MOST, store.get(AFFINITY))); }

    /** Changes her affinity by so much (it stays between nought and a hundred); what it is after. */
    static int befriend(Store store, int by) {
        int after = Math.max(0, Math.min(MOST, affinity(store) + by));
        store.put(AFFINITY, after);
        return after;
    }

    /** Her hearts, in halves: one half for every ten of affinity, five whole hearts at a hundred. */
    static int halfHearts(int affinity) { return Math.max(0, Math.min(10, affinity / 10)); }

    /** How she feels, by affinity: cold, wary, warm, fond, devoted, and beloved at a hundred. */
    static final int COLD = 0, WARY = 1, WARM = 2, FOND = 3, DEVOTED = 4, BELOVED = 5;
    static final String[] TIER_NAMES = {"Cold", "Wary", "Warm", "Fond", "Devoted", "Beloved"};

    static int tier(int affinity) { return affinity >= MOST ? BELOVED : Math.max(0, Math.min(DEVOTED, affinity / 20)); }

    /** How much less she charges at a hundred; and how much more for every time she has been made to fight. */
    static final float MOST_OFF = .4f, GRUDGE_MORE = .25f;

    /** What something of a price is at her stall: less the more she likes Halgakos, more for every grudge she holds, and never under one. */
    static int price(int base, int affinity, int grudge) {
        float off = 1 - MOST_OFF * Math.max(0, Math.min(MOST, affinity)) / MOST, more = 1 + GRUDGE_MORE * Math.max(0, Math.min(4, grudge));
        return Math.max(1, Math.round(base * off * more));
    }

    /**
     * What may be given her: found on the dungeons' floors. Loved, liked, or not: a crimson rose, a moonstone, an old love letter, dark
     * chocolate, a lace ribbon; a garlic bun, a sun charm, a silver mirror (she has no reflection to see in it).
     */
    static final String[] GIFTS = {"Crimson rose", "Moonstone", "Old love letter", "Dark chocolate", "Lace ribbon", "Garlic bun", "Sun charm", "Silver mirror"};
    static final int[] GIFT_WORTH = {8, 6, 5, 4, 3, -6, -4, -3};
    static final int GIFT_KINDS = 8;

    /** Whether she likes a gift. */
    static boolean likes(int gift) { return GIFT_WORTH[gift] > 0; }

    /**
     * A gift given: her affinity goes up (or down) by what it is worth to her, less each time the same thing is given again (never under a
     * quarter of it; a gift she dislikes stings as much every time). What it changed her affinity by.
     */
    static int give(Store store, int gift) {
        int times = store.get(GIVEN + gift), worth = GIFT_WORTH[gift];
        int by = worth > 0 ? Math.max(1, Math.round(worth * Math.max(.25f, 1 - times * .15f))) : worth;
        store.put(GIVEN + gift, times + 1);
        int before = affinity(store);
        return befriend(store, by) - before;
    }

    /** Which gift lies on a floor, if any: one floor in four has one, a disliked one a third of the time. -1 for none. */
    static int giftOn(int dungeon, int floor, long dive) {
        Random dice = new Random(dive * 92821L + dungeon * 6151L + floor * 337L + 5);
        if (dice.nextInt(4) != 0) return -1;
        return dice.nextInt(3) == 0 ? 5 + dice.nextInt(3) : dice.nextInt(5);
    }

    /** Whether her stall stands on a floor of a dive: on one floor of every dive at least and about one in three besides, never the keeper's. */
    static boolean appearsOn(int dungeon, int floor, long dive) {
        int floors = RunDungeons.floors(dungeon);
        if (floor >= floors - 1) return false;
        if (floor == new Random(dive * 4447L + dungeon * 53L + 3).nextInt(floors - 1)) return true;
        return new Random(dive * 7703L + dungeon * 131L + floor * 29L + 11).nextInt(3) == 0;
    }

    /**
     * How she takes being struck, through one dive: the first blow amuses her, the second cools her, the third is her last warning; each
     * costs a little of her affinity and changes her face. Only a blow struck after the last warning starts the fight. Warnings are forgotten
     * after a calm of {@link #CALM} seconds.
     */
    static final class Temper {
        static final int CALM_FACE = 0, AMUSED = 1, COOLER = 2, COLD_FACE = 3, FIGHT = 4;
        static final float CALM = 25;
        static final int[] COSTS = {0, 2, 3, 5};
        /** What starting the fight costs of her affinity, and the grudge it leaves. */
        static final int FIGHT_COST = 15;
        int stage;
        float lastBlow = -1000;

        /** A blow struck at a time: what it brings her to. */
        int struck(Store store, float now) {
            if (now - lastBlow > CALM) stage = CALM_FACE;
            lastBlow = now;
            if (stage >= FIGHT) return FIGHT;
            stage++;
            if (stage == FIGHT) {
                befriend(store, -FIGHT_COST);
                store.put(GRUDGE, Math.min(4, store.get(GRUDGE) + 1));
            } else {
                befriend(store, -COSTS[stage]);
            }
            return stage;
        }

        /** Her face as it is at a time: calm again once the calm has passed. */
        int face(float now) { return stage < FIGHT && now - lastBlow > CALM ? CALM_FACE : stage; }
    }

    /** Her health in a fight, in blows of the land's; how much she mends a second (of her most), and how little once Halgakos is strong enough. */
    static final float HEALTH_BLOWS = 260, MENDS = .06f, MENDS_LATE = .004f;
    /** The level from which she can be beaten. */
    static final int MORTAL_LEVEL = 120;

    static float mends(int level) { return level >= MORTAL_LEVEL ? MENDS_LATE : MENDS; }

    /** Below this much of her health a killing blow is asked about first: it cannot be undone. */
    static final float LAST_BLOW = .05f;
}
