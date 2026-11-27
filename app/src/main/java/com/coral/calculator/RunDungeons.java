package com.coral.calculator;

import java.util.Random;

/**
 * The dungeons: twenty-four of them, two in every chapter, each gone down into by a stairway that stands in a land of it. Each is five
 * floors deep or more (more the further on), laid afresh every time she goes down, so that no two dives are the same. Her hearts and
 * lives go down with her from floor to floor; between floors she chooses one of three blessings for the rest of the dive; most floors
 * have something about them (gilded, teeming, lightless, quiet, fierce); and at the bottom waits its keeper, who must be beaten before
 * the way out opens. The first time a dungeon is cleared it gives a relic of its own.
 */
final class RunDungeons {
    private RunDungeons() {}

    static final int COUNT = 24;

    static final String[] NAMES = {"The Dog-Eared Cellar", "The Margin Burrows", "The Love-Letter Vault", "The Whispering Stacks", "The Bracket Mines",
            "The Errata Warrens", "The Gilt Undercroft", "The Wax-Seal Crypt", "The Press Room Below", "The Hollow Headlines", "The Glue Pits",
            "The Index Labyrinth", "The Bottle Grotto", "The Ember Archive", "The Draft Tunnels", "The Moth-Eaten Halls", "The Inkwell Depths",
            "The Typeset Foundry", "The Doodle Caverns", "The Blank Abyss", "The Mother Tongue's Roots", "The Sunken Reading Room", "The Last Chapter Down",
            "The Unbound Deep"};
    /** The land each one's stairway stands in: two to a chapter, neither where someone waits at the end. */
    static final int[] LANDS = {3, 5, 8, 10, 14, 16, 20, 22, 26, 28, 33, 37, 43, 47, 53, 57, 63, 67, 73, 77, 83, 87, 93, 97};

    /** Which backdrop each is drawn against: the catacomb, the ink crypt, the paper cavern, the vault. */
    static int backdrop(int dungeon) { return (dungeon / 2 + dungeon % 2) % 4; }

    /** How many floors deep it is: five in the first chapters, eight at the last. */
    static int floors(int dungeon) { return 5 + dungeon / 7; }

    /** Its land; and which dungeon, if any, has its stairway in a land. */
    static int land(int dungeon) { return LANDS[dungeon]; }

    static int inLand(int number) {
        for (int dungeon = 0; dungeon < COUNT; dungeon++) if (LANDS[dungeon] == number) return dungeon;
        return -1;
    }

    /** The three kinds of the seven that live in dungeons that this one has, each different. */
    static int[] delvers(int dungeon) {
        int[] kinds = new int[3];
        for (int one = 0; one < 3; one++) kinds[one] = RunUniques.DELVER_FIRST + (dungeon + one * 3) % RunUniques.DELVER_COUNT;
        return kinds;
    }

    /** Its keeper. */
    static int keeper(int dungeon) { return RunUniques.keeperOf(dungeon); }

    /** The level it is for, as the land it is in. */
    static int recommended(int dungeon) { return RunLoot.recommended(LANDS[dungeon]); }

    /** How much harder a floor is than the land: a step more every third floor down. */
    static int deeper(int floor) { return floor / 3; }

    // What a floor is like

    static final int PLAIN = 0, GILDED = 1, TEEMING = 2, LIGHTLESS = 3, QUIET = 4, FIERCE = 5, KINDS = 6;
    static final String[] KIND_NAMES = {"", "Gilded", "Teeming", "Lightless", "Quiet", "Fierce"};
    static final String[] KIND_WORDS = {"", "pluses count double", "twice the creatures", "only her lantern to see by", "few creatures, and bread to mend her",
            "creatures tougher, and more to drop"};

    /** What a floor of a dive is like: the first and the keeper's are plain; the rest, by the dive's own chance. */
    static int kindOf(int dungeon, int floor, long dive) {
        if (floor == 0 || floor >= floors(dungeon) - 1) return PLAIN;
        Random dice = new Random(dive * 7919L + dungeon * 104729L + floor * 15485863L);
        int roll = dice.nextInt(10);
        return roll < 3 ? PLAIN : 1 + (roll - 3) % (KINDS - 1);
    }

    // Blessings, chosen between floors

    static final int HEART = 0, MIGHT = 1, GOLD = 2, LIFE = 3, MEND = 4, WARD = 5, BLESSINGS = 6;
    static final String[] BLESSING_NAMES = {"Heart of Ink", "Sharp Nib", "Golden Pages", "Spare Bookmark", "Fresh Binding", "Thick Cover"};
    static final String[] BLESSING_WORDS = {"a heart more", "blows a quarter harder", "pluses half as much again", "a life more", "every heart mended",
            "a hurt costs half a heart less"};

    /** The three offered after a floor: different, by the dive's own chance. Mending is offered only when she is hurt. */
    static int[] offered(long dive, int floor, boolean hurt, int taken) {
        Random dice = new Random(dive * 31337L + floor * 7919L);
        int[] three = new int[3];
        int count = 0;
        for (int tries = 0; tries < 60 && count < 3; tries++) {
            int one = dice.nextInt(BLESSINGS);
            if (one == MEND && !hurt) continue;
            // (A life more, once a dive; and none already offered.)
            if (one == LIFE && (taken >> LIFE & 1) != 0) continue;
            boolean again = false;
            for (int index = 0; index < count; index++) again |= three[index] == one;
            if (!again) three[count++] = one;
        }
        for (int fill = 0; count < 3; fill++) {
            boolean again = false;
            for (int index = 0; index < count; index++) again |= three[index] == fill;
            if (!again && (fill != MEND || hurt)) three[count++] = fill;
        }
        return three;
    }

    // What it gives

    /** Pluses for clearing it: more the deeper and further on; the first time, three times as many. */
    static int pluses(int dungeon, boolean first) { return (60 + LANDS[dungeon] * 6 + floors(dungeon) * 20) * (first ? 3 : 1); }

    /** Its relic, the first time it is cleared: a thing to keep of its chapter, red or better (a Rose at the last). */
    static int relic(int dungeon) {
        int[] types = {RunLoot.ACCESSORY, RunLoot.ARMOUR, RunLoot.WEAPON};
        int chapter = Math.min(11, dungeon / 2);
        int rarity = dungeon >= 20 ? RunLoot.ROSE : RunLoot.RED;
        return RunLoot.code(types[dungeon % 3], chapter, (dungeon * 5 + 3) % 12, rarity);
    }
}
