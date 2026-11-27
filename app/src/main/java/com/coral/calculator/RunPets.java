package com.coral.calculator;

/**
 * Which of Plus run's pets can be had. The first ten are in the pet shop from the first: they
 * are found as strays in lands, sit in the pet shops of the villages, and are at the counter.
 * The others are small stuffed copies of those she has beaten, and are nowhere at all until
 * she has beaten them: then they are at the counter, and only there. Mini Illya when Illya is
 * beaten in her land; mini Tsubakieh in hers; the mini Mawang when the Mawang is, at the end of
 * the second volume; and mini Lucy when Lucy is, in the last land, whichever way it ends.
 */
final class RunPets {
    /** How many kinds are had from the first, and found and sold everywhere. */
    static final int FOUND = 10;
    /** What is kept: how many pets could be had when she last said a new one could. */
    static final String TOLD = "run_pets_told";

    private RunPets() {}

    /** The land whose winning opens a pet that is opened by a land, or 0. */
    static int land(int kind) {
        switch (kind) {
            case PlusRun.MINI_ILLYA: return RunLevels.ILLYA_LAND;
            case PlusRun.MINI_TSUBAKIEH: return RunLevels.TSUBAKIEH_LAND;
            case PlusRun.MINI_MAWANG: return RunLevels.SECOND;
            default: return 0;
        }
    }

    /** Whether a pet can be had, by what the game keeps. */
    static boolean open(int kind, Unlocks.Store kept) {
        if (kind < 0 || kind >= PlusRun.PETS) return false;
        if (kind < FOUND) return true;
        // Lucy is beaten when the flight is won: the land unmade and the true ending both come after that.
        if (kind == PlusRun.MINI_LUCY) return (kept.tally("run_ending") & 3) != 0;
        return land(kind) > 0 && kept.tally("run_stars_" + land(kind)) > 0;
    }

    /** How many pets can be had. */
    static int open(Unlocks.Store kept) {
        int count = 0;
        for (int kind = 0; kind < PlusRun.PETS; kind++) if (open(kind, kept)) count++;
        return count;
    }
}
