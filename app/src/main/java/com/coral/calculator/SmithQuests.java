package com.coral.calculator;

/**
 * The smith sisters' quest lines, taken up at the smithy where all three keep it: one of Moru's, one of Pulmu's, one of Sutdol's,
 * and, once those three are done, one of all three together. Each has three steps, each a thing the game already counts
 * (creatures flattened, hunts handed in, paper trees cut, dungeons cleared, hidden bosses beaten, how deep she has gone) or
 * something handed over (a smith's material); a step that counts counts from when it began. When the last step is handed in,
 * what the quest gives is hers. Kept as numbers under run_sq_ keys; a save with none has taken up no quest.
 */
final class SmithQuests {
    private SmithQuests() {}

    static final int MOBS = 0, HUNTS = 1, TREES = 2, GIVE = 3, DUNGEONS = 4, HIDDEN = 5, DEEP = 6, CLEAR = 7;

    /** One step: what kind, how many (or which dungeon), and for a gift which material (0 type metal, 1 ink crystal, 2 gold leaf, 3 all three). */
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

    static final int MORU_QUEST = 0, PULMU_QUEST = 1, SUTDOL_QUEST = 2, TOGETHER = 3, COUNT = 4;
    static final String[] NAMES = {"Moru's lost hammer", "A coat of scales", "Weighed twice", "The three anvils"};
    /** Whose each is (as RunGear numbers the sisters; all three for the last). */
    static final String[] GIVERS = {"Moru", "Pulmu", "Sutdol", "All three sisters"};
    static final Step[][] STEPS = {
            {new Step(MOBS, 60, 0, "Flatten 60 creatures"), new Step(GIVE, 6, 0, "Bring 6 type metal"), new Step(DUNGEONS, 1, 0, "Clear a dungeon, to the keeper")},
            {new Step(HUNTS, 4, 0, "Hand in 4 guild hunts"), new Step(GIVE, 4, 1, "Bring 4 ink crystals"), new Step(HIDDEN, 1, 0, "Beat a hidden boss in its hideout")},
            {new Step(TREES, 5, 0, "Cut down 5 paper trees"), new Step(GIVE, 3, 2, "Bring 3 gold leaf"), new Step(DEEP, 4, 0, "Reach the fourth floor of a dungeon")},
            {new Step(DUNGEONS, 3, 0, "Clear 3 dungeons"), new Step(GIVE, 5, 3, "Bring 5 of each metal"), new Step(CLEAR, 16, 0, "Clear The Inkwell Depths")}};

    /** What each gives: Moru's hammer, Pulmu's coat, Sutdol's charm, all red; and, from all three, a Halgakos Rose signet. And pluses. */
    static final int[] REWARDS = {RunLoot.code(RunLoot.WEAPON, 2, 8, RunLoot.RED), RunLoot.code(RunLoot.ARMOUR, 2, 12, RunLoot.RED),
            RunLoot.code(RunLoot.ACCESSORY, 2, 9, RunLoot.RED), RunLoot.code(RunLoot.ACCESSORY, 2, RunLoot.SIGNET, RunLoot.ROSE)};
    static final int[] PLUSES = {300, 300, 300, 1500};

    private static String key(int quest) { return "run_sq_" + quest; }

    /** 0 not taken up; 1 to 3, on that step; 4, done. */
    static int state(RunVanity.Keep save, int quest) { return Math.max(0, Math.min(STEPS[quest].length + 1, save.tally(key(quest)))); }

    static boolean done(RunVanity.Keep save, int quest) { return state(save, quest) > STEPS[quest].length; }

    /** Whether it may be taken up: each sister's at once; all three's once those are done. */
    static boolean open(RunVanity.Keep save, int quest) {
        return quest != TOGETHER || done(save, MORU_QUEST) && done(save, PULMU_QUEST) && done(save, SUTDOL_QUEST);
    }

    /** What the game has counted so far of what a step asks for. */
    private static int total(RunVanity.Keep save, Step step) {
        switch (step.kind) {
            case MOBS: return save.tally("run_mobs");
            case HUNTS: return save.tally("run_hunts");
            case TREES: return save.tally("run_trees_cut");
            case DUNGEONS: return Integer.bitCount(save.tally("run_dungeons"));
            case HIDDEN: {
                int beaten = 0;
                for (int land = 1; land <= RunLevels.TOTAL; land++) if (save.tally("run_hidden_" + land) != 0) beaten++;
                return beaten;
            }
            default: return 0;
        }
    }

    /** Takes the quest up: its first step counts from now. */
    static boolean take(RunVanity.Keep save, int quest) {
        if (!open(save, quest) || state(save, quest) != 0) return false;
        begin(save, quest, 1);
        return true;
    }

    private static void begin(RunVanity.Keep save, int quest, int step) {
        save.keep(key(quest), step);
        if (step <= STEPS[quest].length) save.keep(key(quest) + "_from", total(save, STEPS[quest][step - 1]));
    }

    /** How far the step she is on has come and has to: {have, need}; for one not taken up or done, {0, 0}. */
    static int[] progress(RunVanity.Keep save, int quest) {
        int state = state(save, quest);
        if (state < 1 || state > STEPS[quest].length) return new int[]{0, 0};
        Step step = STEPS[quest][state - 1];
        switch (step.kind) {
            case GIVE: {
                if (step.which < 3) return new int[]{Math.min(step.amount, save.tally("run_mat_" + step.which)), step.amount};
                int least = Integer.MAX_VALUE;
                for (int material = 0; material < 3; material++) least = Math.min(least, save.tally("run_mat_" + material));
                return new int[]{Math.min(step.amount, least), step.amount};
            }
            case DEEP: {
                int deepest = 0;
                for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) if (save.tally("run_deep_" + dungeon) > 0) deepest = Math.max(deepest, save.tally("run_deep_" + dungeon) + 1);
                return new int[]{Math.min(step.amount, deepest), step.amount};
            }
            case CLEAR: return new int[]{(save.tally("run_dungeons") >> step.amount & 1) != 0 ? 1 : 0, 1};
            default: return new int[]{Math.min(step.amount, Math.max(0, total(save, step) - save.tally(key(quest) + "_from"))), step.amount};
        }
    }

    /** Whether the step she is on can be handed in. */
    static boolean ready(RunVanity.Keep save, int quest) {
        int state = state(save, quest);
        int[] now = progress(save, quest);
        return state >= 1 && state <= STEPS[quest].length && now[0] >= now[1];
    }

    static final int NOTHING = 0, STEP = 1, FINISHED = 2;

    /** Hands in the step she is on, if it is done: a gift is taken and the next step begins; the last, and what the quest gives is hers. */
    static int hand(RunVanity.Keep save, int quest) {
        if (!ready(save, quest)) return NOTHING;
        int state = state(save, quest);
        Step step = STEPS[quest][state - 1];
        if (step.kind == GIVE) {
            for (int material = 0; material < 3; material++) {
                if (step.which == material || step.which == 3) save.keep("run_mat_" + material, save.tally("run_mat_" + material) - step.amount);
            }
        }
        if (state == STEPS[quest].length) {
            save.keep(key(quest), state + 1);
            save.keep("run_bank", save.tally("run_bank") + PLUSES[quest]);
            return FINISHED;
        }
        begin(save, quest, state + 1);
        return STEP;
    }

    /** Who speaks each line of a quest (0 asked, 1 her answer, 2 and 3 a step handed in, 4 the end): as RunStory has them. */
    static String[] line(int quest, int which) { return RunStory.SMITH_QUESTS[quest * 5 + which]; }
}
