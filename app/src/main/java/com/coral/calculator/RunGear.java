package com.coral.calculator;

/**
 * What the smithy makes, and what it asks. Three sisters keep it. Moru forges: she works on
 * a thing to fight with, a step at a time, and each step changes what the thing does (see
 * {@link RunArms}). Pulmu makes what Halgakos wears: a helm, a coat, boots and a charm,
 * each in three sets, tinplate, wrought iron and type-steel. Sutdol trades in what a smith
 * works with, and undoes her sisters' work: a thing re-forged is as it was found, and what
 * went into it comes back.
 * <p>
 * What she wears is there for what it does, and is one thing for each piece. A helm takes
 * so many halves of a heart off what a hurt costs, never the last half. A coat keeps her
 * from being hurt again for longer after a hurt. Boots make her coming down on a creature
 * count for more blows. A charm lets her keep more of the pluses she ends a land with. And
 * all four of one set are a heart more.
 * <p>
 * Everything is paid for in pluses and in what hunts pay: type metal, ink crystal and gold
 * leaf, in the order {@link RunHunts#MATERIALS} has them.
 */
final class RunGear {
    private RunGear() {}

    static final int HELM = 0, COAT = 1, BOOTS = 2, CHARM = 3, SLOTS = 4, TIERS = 3;
    static final String[] SLOT_NAMES = {"Helm", "Coat", "Boots", "Charm"};
    static final String[] SET_NAMES = {"Tinplate", "Wrought-iron", "Type-steel"};

    /** The three who keep the smithy, in the order of what they do: forging, what she wears, and trade. */
    static final int MORU = 0, PULMU = 1, SUTDOL = 2;
    static final String[] SMITHS = {"Moru", "Pulmu", "Sutdol"};
    static final String[] TRADES = {"Moru forges what she fights with", "Pulmu makes what she wears", "Sutdol trades, and re-forges"};

    /** A piece, as it is called. */
    static String name(int slot, int tier) { return SET_NAMES[tier - 1] + " " + SLOT_NAMES[slot].toLowerCase(java.util.Locale.ROOT); }

    /** A piece as a bit of what she has had made; and, of what she has on, the set of the piece in a place, or nought. */
    static int bit(int slot, int tier) { return 1 << slot * TIERS + tier - 1; }
    static int worn(int on, int slot) { return on >> slot * 2 & 3; }
    static int wear(int on, int slot, int tier) { return on & ~(3 << slot * 2) | tier << slot * 2; }

    /** What she has on, with nothing in it that she has not had made. */
    static int sound(int on, int made) {
        for (int slot = 0; slot < SLOTS; slot++) {
            int tier = worn(on, slot);
            if (tier > TIERS || tier > 0 && (made & bit(slot, tier)) == 0) on = wear(on, slot, 0);
        }
        return on;
    }

    /** What what she has on does: halves of a heart off a hurt; how many times as long nothing hurts her after one; more blows to a landing; a hundredth more pluses kept for each; and whether it is all of one set. */
    static int defence(int on) { return worn(on, HELM); }
    static float mercy(int on) { return 1 + .25f * worn(on, COAT); }
    static int stomp(int on) { return worn(on, BOOTS); }
    static int luck(int on) { return 10 * worn(on, CHARM); }
    static boolean whole(int on) {
        int first = worn(on, HELM);
        return first > 0 && worn(on, COAT) == first && worn(on, BOOTS) == first && worn(on, CHARM) == first;
    }

    /** What a piece does, in words. */
    static String does(int slot, int tier) {
        switch (slot) {
            case HELM: return "A hurt costs " + (tier == 1 ? "half a heart" : tier == 2 ? "a heart" : "a heart and a half") + " less, never less than half a heart.";
            case COAT: return "After a hurt nothing hurts her again for " + (tier == 1 ? "a quarter" : tier == 2 ? "half" : "three quarters") + " as long again.";
            case BOOTS: return "Coming down on a creature counts for " + (tier + 1) + " blows.";
            default: return "She keeps a " + (tier == 1 ? "tenth" : tier == 2 ? "fifth" : "third, nearly,") + " more of the pluses she ends a land with.";
        }
    }

    /** What a piece of a set costs: pluses, and of each of the three that a smith works with. */
    private static final int[] PIECE_PLUSES = {150, 350, 700};
    private static final int[][] PIECE_NEEDS = {{2, 0, 0}, {4, 2, 0}, {6, 4, 2}};
    /** What a step of work on a thing to fight with costs, by the level it takes it to. */
    private static final int[] STEP_PLUSES = {120, 300, 600};
    private static final int[][] STEP_NEEDS = {{2, 0, 0}, {4, 2, 0}, {6, 4, 1}};
    /** What Sutdol sells each of the three for, what she gives for one, and what she takes to re-forge a thing. */
    static final int[] BUYS = {60, 150, 400}, SELLS = {20, 50, 130};
    static final int REFORGE = 50;

    static int piecePluses(int tier) { return PIECE_PLUSES[tier - 1]; }
    static int[] pieceNeeds(int tier) { return PIECE_NEEDS[tier - 1]; }
    static int stepPluses(int level) { return STEP_PLUSES[level - 1]; }
    static int[] stepNeeds(int level) { return STEP_NEEDS[level - 1]; }

    /** What went into a thing that has been worked on as far as a level: which is what re-forging it gives back. */
    static int[] spent(int level) {
        int[] all = new int[3];
        for (int step = 1; step <= level; step++) for (int index = 0; index < 3; index++) all[index] += STEP_NEEDS[step - 1][index];
        return all;
    }

    /** Whether she has enough: pluses, and of each of the three. */
    static boolean affords(int bank, int[] has, int pluses, int[] needs) {
        if (bank < pluses) return false;
        for (int index = 0; index < 3; index++) if (has[index] < needs[index]) return false;
        return true;
    }

    /** A price, in a line. */
    static String price(int pluses, int[] needs) {
        StringBuilder line = new StringBuilder("+ " + pluses);
        for (int index = 0; index < 3; index++) if (needs[index] > 0) line.append("  ·  ").append(needs[index]).append(' ').append(RunHunts.MATERIALS[index].toLowerCase(java.util.Locale.ROOT));
        return line.toString();
    }
}
