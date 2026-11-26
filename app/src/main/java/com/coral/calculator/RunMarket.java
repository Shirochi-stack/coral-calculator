package com.coral.calculator;

/**
 * The black market, which is in one village in five. Three things are done there.
 * <p>
 * She may have the lands harder, by choice, a step at a time and back again: with every
 * step every creature takes a blow more and every hurt costs half a heart more, and she
 * keeps a quarter more of the pluses she ends a land with.
 * <p>
 * She may go down into the black arena, which is a ladder of ten rungs. On each waits one
 * of those she has already got past in the story, in the place she met them, but stronger:
 * she takes more, behind more of a shield, and from the fourth rung a hurt costs more. A
 * rung is open once the one before it is won and the story has been past whoever waits on
 * it. Every rung gives something the first time it is won, which is shown before it is
 * fought, and tokens every time. Four of the rungs give a thing to fight with that is
 * found nowhere else and is hers to begin a land with from then on: a katana, a
 * fountain-pen lance, an ink-well cannon and a ribbon whip.
 * <p>
 * And tokens are spent there: on her ring of API keys, on treats, and on what a smith
 * works with.
 */
final class RunMarket {
    private RunMarket() {}

    static final int RUNGS = 10;
    /** The land in whose arena each rung is fought, and so who waits on it. */
    static final int[] LANDS = {6, 12, 18, 24, 30, 35, 50, 60, 70, 80};

    /** On a rung, from one: how many more blows whoever waits takes, how many more pieces her shield has, and how many more halves of a heart a hurt costs. */
    static int takes(int rung) { return rung <= 0 ? 0 : 2 + rung / 2; }
    static int shields(int rung) { return rung <= 0 ? 0 : 1 + rung / 3; }
    static int bites(int rung) { return rung <= 0 ? 0 : rung >= 8 ? 2 : rung >= 4 ? 1 : 0; }

    /** How many tokens a rung gives: so many the first time it is won, and one every time after. */
    static int tokens(int rung, boolean first) { return first ? 2 + rung / 2 : 1; }

    /** What a rung gives the first time, besides its tokens. */
    static final int PLUSES = 0, WEAPON = 1, STUFF = 2, TREATS = 3, SASH = 4;
    private static final int[][] PRIZES = {{PLUSES, 200}, {WEAPON, RunArms.KATANA}, {STUFF, 1}, {WEAPON, RunArms.LANCE}, {TREATS, 3}, {WEAPON, RunArms.CANNON}, {STUFF, 2},
            {WEAPON, RunArms.WHIP}, {PLUSES, 1000}, {SASH, 0}};
    /** What of the three a smith works with a rung that gives them gives: the lesser lot, and the greater. */
    static final int[][] STUFFS = {{4, 3, 1}, {8, 6, 4}};

    static int prize(int rung) { return PRIZES[rung - 1][0]; }
    static int prizeOf(int rung) { return PRIZES[rung - 1][1]; }

    /** What a rung gives the first time, in words. */
    static String prizeWords(int rung) {
        int what = prizeOf(rung);
        switch (prize(rung)) {
            case PLUSES: return "+ " + what + " to keep";
            case WEAPON: return "The " + RunArms.NAMES[what].toLowerCase(java.util.Locale.ROOT) + ": hers to begin a land with";
            case STUFF: return STUFFS[what - 1][0] + " type metal, " + STUFFS[what - 1][1] + " ink crystal, " + STUFFS[what - 1][2] + " gold leaf";
            case TREATS: return what + " treats, for the pet shop";
            default: return "The black sash of the arena, to keep, and + 1500";
        }
    }

    /** How many steps harder she may have it; and how many hundredths more of her pluses she keeps for each. */
    static final int STEPS = 3, PAYS = 25;

    /** What a step is, in words. */
    static String stepWords(int step) {
        return step == 0 ? "As it is: nothing more, and nothing more for it."
                : "Every creature takes " + step + (step == 1 ? " blow" : " blows") + " more, every hurt costs " + (step == 1 ? "half a heart" : step == 2 ? "a heart" : "a heart and a half")
                + " more; and she keeps " + (step == 1 ? "a quarter" : step == 2 ? "half" : "three quarters") + " more of her pluses.";
    }

    /** What tokens buy: her ring of keys, once; a treat; and lots of what a smith works with. What each costs, in tokens. */
    static final int KEYS = 0, TREAT = 1, METAL = 2, CRYSTAL = 3, LEAF = 4, WARES = 5;
    static final String[] WARE_NAMES = {"The ring of API keys", "A treat", "3 type metal", "2 ink crystal", "1 gold leaf"};
    static final int[] WARE_COSTS = {6, 2, 1, 1, 2};
}
