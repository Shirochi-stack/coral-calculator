package com.coral.calculator;

/**
 * Tulle's quest lines. Each has a few steps, each step a thing that the game already counts (creatures flattened, hunts
 * handed in, a land cleared) or something handed over (a smith's material); a step counts from when it began, so what she
 * did before she took the quest up is not counted. When the last step is handed in, the quest's outfit is hers. The state is
 * kept as numbers under run_vq_ keys; a save with none has taken up no quest.
 */
final class VanityQuests {
    private VanityQuests() {}

    static final int MOBS = 0, HUNTS = 1, LAND = 2, GIVE = 3;

    /** One step: what kind, how many (or which land), and for a gift, which material (0 type metal, 1 ink crystal, 2 gold leaf). */
    static final class Step {
        final int kind, amount, which;
        final String ask;

        Step(int kind, int amount, int which, String ask) {
            this.kind = kind;
            this.amount = amount;
            this.which = which;
            this.ask = ask;
        }
    }

    static final String[] NAMES = {"Fan letters", "The lost bolt of silk", "Royal blue dye"};
    /** The outfit each gives. */
    static final int[] REWARD = {5, 6, 2};
    static final Step[][] STEPS = {
            {new Step(MOBS, 40, 0, "Flatten 40 creatures"), new Step(HUNTS, 3, 0, "Hand in 3 guild hunts"), new Step(LAND, 4, 0, "Clear land 4")},
            {new Step(LAND, 5, 0, "Clear land 5 and find the silk"), new Step(GIVE, 4, 0, "Bring 4 type metal"), new Step(MOBS, 60, 0, "Flatten 60 creatures")},
            {new Step(LAND, 8, 0, "Beat the boss of land 8 for the dye"), new Step(GIVE, 3, 1, "Bring 3 ink crystals"), new Step(HUNTS, 4, 0, "Hand in 4 guild hunts")}};

    static int count() { return NAMES.length; }

    private static String key(int quest) { return "run_vq_" + quest; }

    /** 0 not taken up; 1 to the number of steps, on that step; steps + 1, done. */
    static int state(RunVanity.Keep save, int quest) { return Math.max(0, Math.min(STEPS[quest].length + 1, save.tally(key(quest)))); }

    static boolean done(RunVanity.Keep save, int quest) { return state(save, quest) > STEPS[quest].length; }

    /** Whether her outfit for it can be had at all yet: its pictures are in the app. */
    static boolean open(int quest) { return RunVanity.ready(REWARD[quest]); }

    private static int total(RunVanity.Keep save, Step step) { return step.kind == MOBS ? save.tally("run_mobs") : step.kind == HUNTS ? save.tally("run_hunts") : 0; }

    /** Takes the quest up: its first step counts from now. */
    static boolean take(RunVanity.Keep save, int quest) {
        if (!open(quest) || state(save, quest) != 0) return false;
        begin(save, quest, 1);
        return true;
    }

    private static void begin(RunVanity.Keep save, int quest, int step) {
        save.keep(key(quest), step);
        if (step <= STEPS[quest].length) save.keep(key(quest) + "_from", total(save, STEPS[quest][step - 1]));
    }

    /** How far the step she is on has come, and how far it has to: {have, need}; for a quest not taken up or done, {0, 0}. */
    static int[] progress(RunVanity.Keep save, int quest) {
        int state = state(save, quest);
        if (state < 1 || state > STEPS[quest].length) return new int[]{0, 0};
        Step step = STEPS[quest][state - 1];
        switch (step.kind) {
            case LAND: return new int[]{save.tally("run_stars_" + step.amount) > 0 ? 1 : 0, 1};
            case GIVE: return new int[]{Math.min(step.amount, save.tally("run_mat_" + step.which)), step.amount};
            default: return new int[]{Math.min(step.amount, Math.max(0, total(save, step) - save.tally(key(quest) + "_from"))), step.amount};
        }
    }

    static final int NOTHING = 0, STEP = 1, FINISHED = 2;

    /** Hands in the step she is on, if it is done: what it asks is taken (a gift) and the next step begins, or the outfit is hers. */
    static int hand(RunVanity.Keep save, int quest) {
        int state = state(save, quest);
        int[] now = progress(save, quest);
        if (state < 1 || state > STEPS[quest].length || now[0] < now[1]) return NOTHING;
        Step step = STEPS[quest][state - 1];
        if (step.kind == GIVE) save.keep("run_mat_" + step.which, save.tally("run_mat_" + step.which) - step.amount);
        if (state == STEPS[quest].length) {
            save.keep(key(quest), state + 1);
            RunVanity.give(save, REWARD[quest]);
            return FINISHED;
        }
        begin(save, quest, state + 1);
        return STEP;
    }

    /** The line Tulle says: asked, Halgakos agreeing, as each step is done (1, 2), and the end. */
    static String[] line(int quest, int which) { return VanityStory.QUESTS[quest][which]; }
}
