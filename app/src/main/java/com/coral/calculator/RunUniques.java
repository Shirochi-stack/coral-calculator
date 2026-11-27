package com.coral.calculator;

/**
 * The unique creatures: two of each of the twelve chapters' own, each with a way of fighting of its own and a clear wind-up before it
 * strikes. They are tougher than the signs and bite harder, more so the further on their land, so that better gear matters; a colour of
 * toughness may come on top. Their pictures are rows of six (walking twice, winding up, striking, hurt, beaten) in run_uniques.
 */
final class RunUniques {
    private RunUniques() {}

    /** The first of their kinds, after Lucy; how many there are. */
    static final int FIRST = RunLevels.LUCY + 1, COUNT = 24;

    /** The ways of fighting. */
    static final int CHARGER = 0, SHIELDER = 1, MIMIC = 2, GHOST = 3, EXPLODER = 4, SNIPER = 5, BURROWER = 6, SUMMONER = 7, HEALER = 8, MINIBOSS = 9,
            FORMATION = 10, SPLITTER = 11;
    static final String[] WAY_NAMES = {"charger", "shielder", "mimic", "wall-ghost", "exploder", "sniper", "burrower", "summoner", "healer", "mini-boss",
            "formation flyer", "splitter"};

    static final String[] NAMES = {"Dog-Ear", "Comma Snail", "Dust Jacket", "Shush", "Love Letter", "Cupid Quill", "Tab Goat", "Bracket Mole",
            "Epilogue Wraith", "Erratum", "Gilt Knight", "Seal Wax Golem", "Headline Hornet", "Press Roller", "Gap Shade", "Mirror Typo", "Margin Slug",
            "Glue Bomb", "Index Spider", "Errata Hydra", "Dog-Eared Crab", "Message in a Bottle", "Ember Scribe", "Draftling"};
    /** Each one's chapter (two to a chapter, in order) and way of fighting. */
    static final int[] WAY = {CHARGER, SHIELDER, MIMIC, GHOST, EXPLODER, SNIPER, CHARGER, BURROWER, SUMMONER, HEALER, MINIBOSS, SHIELDER, FORMATION, CHARGER,
            GHOST, MIMIC, SPLITTER, EXPLODER, SNIPER, MINIBOSS, SHIELDER, SUMMONER, HEALER, CHARGER};
    /** How many blows of health each has at the start of the game (it grows with the land), by way of fighting. */
    private static final float[] HEALTH = {4, 6, 4, 3, 2, 3, 4, 5, 4, 24, 1.5f, 6};
    /** How wide and tall each is, in squares. */
    static final float[] WIDE = {.95f, .9f, 1.1f, .9f, .9f, .8f, 1.1f, .9f, .95f, .85f, 1.2f, 1.25f, .8f, 1.15f, .8f, .9f, 1.2f, .85f, .95f, 1.5f, 1.1f, .85f, .9f, 1.2f};
    static final float[] TALL = {.8f, .85f, .7f, 1.15f, .75f, .85f, .95f, .95f, 1.25f, .75f, 1.5f, 1.35f, .7f, .8f, 1.2f, .85f, .75f, .9f, .75f, 1.45f, .8f, .85f, 1.05f, 1.05f};
    /** How fast each walks, squares a second. */
    static final float[] SPEED = {1.5f, .55f, 1.2f, .9f, 1.3f, .8f, 1.4f, 1.6f, .6f, .9f, 1.1f, .5f, 2.2f, 1.6f, 1, 1.4f, .8f, 1.2f, .9f, .9f, .7f, .7f, .9f, 1.7f};

    static boolean is(int kind) { return kind >= FIRST && kind < FIRST + COUNT; }

    static int index(int kind) { return kind - FIRST; }

    static int way(int kind) { return WAY[index(kind)]; }

    static int chapter(int kind) { return index(kind) / 2; }

    static String name(int kind) { return NAMES[index(kind)]; }

    /** The two of a chapter. */
    static int first(int chapter) { return FIRST + Math.max(0, Math.min(11, chapter)) * 2; }

    /** How much health one has in a land (in tenths of a blow): its way's, made more as her blows grow with the level the land is for. */
    static int health(int kind, int number) {
        return Math.round(PlusRun.BLOW * HEALTH[way(kind)] * RunLoot.power(RunLoot.recommended(number)) * (1 + number / 100f));
    }

    /** The halves of a heart more than a sign's its touch costs: none in the first two chapters, then one (two for a mini-boss), and one more every forty lands. */
    static int bite(int kind, int number) { return number <= 12 ? 0 : (way(kind) == MINIBOSS ? 2 : 1) + number / 40; }

    /** Whether it flies (and so is in the air, not on the ground). */
    static boolean flies(int kind) { return is(kind) && way(kind) == FORMATION; }

    /** Whether it goes through walls. */
    static boolean ghostly(int kind) { return is(kind) && way(kind) == GHOST; }
}
