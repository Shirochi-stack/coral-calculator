package com.coral.calculator;

/**
 * The ultimates: the last active of six of the skill tree's branches. An ordinary active is a handful of numbers turned up for eight seconds;
 * an ultimate is a different way of playing for a while, with a rule of its own, a look of its own and a sound of its own. Each costs a great
 * deal of energy, three points of the tree, and a long rest before the next; only one can be going at a time. This is where their numbers and
 * their words are kept; {@link PlusRun} has the rules and {@link RunUltimateFx} the pictures.
 *
 * <p>What keeps the bosses from being beaten in a moment by any of them: the damage the ultimates add is only ever added to ordinary creatures
 * (a blow on one who waits at the end counts for one blow, however hard she hits, as it always has, and he can only be hurt when he is spent
 * and never again for a moment after); the extra blows an ultimate makes of its own (cleaves, echoes, ink, bursts of roses) land on a boss only
 * {@link #BOSS_HITS} times in all, and only when he is open to them; and a boss only half feels the rest. He is slowed to six tenths by the
 * stilled hour and not to a sixth, his blows are not held back to be struck together later; he is not staggered by the giant's steps; the
 * ink of the tide does not poison him; berserk's blood gives back one HP for a blow on him and no more than {@link #BOSS_LEECH_MOST} in all.
 */
final class RunUltimates {
    private RunUltimates() {}

    /** Which: berserk, the stilled hour, the tide of ink, the giant, the legion of echoes, the rose bloom. (0 is none.) */
    static final int NONE = 0, BERSERK = 1, STILL = 2, TIDE = 3, TITAN = 4, LEGION = 5, BLOOM = 6, COUNT = 6;

    static final String[] NAMES = {"", "Rampage of the Red Page", "The Stilled Hour", "Tide of Ink", "Colossus of Contents", "Legion of Echoes", "Rose Bloom"};
    /** The class whose branch each is the last active of: greatswords, the katana, pens and pencils, hammers, summoning staffs, scythes. */
    static final int[] BRANCH = {-1, 2, 1, 6, 8, 4, 11};
    /** What calling one on costs in energy (all she can hold but a fifth), and in points of the tree. */
    static final float COST = 80;
    static final int POINTS = 3;
    /** How long each lasts, and how long it rests after it is called on. */
    static final float[] LASTS = {0, 10, 6, 3.4f, 10, 12, 10};
    static final float[] REST = {0, 90, 100, 80, 100, 110, 100};

    /** Berserk: how much harder her blows land on ordinary creatures, how much faster they come, how much more a hurt costs, and how long she is spent after. */
    static final float BERSERK_MIGHT = 2f, BERSERK_REST = .4f, BERSERK_HURT = 1.5f, EXHAUSTED_LASTS = 3, EXHAUSTED_REST = 1.6f, EXHAUSTED_RUN = .7f;
    /** Berserk: HP back for every blow (on a boss, one), no more than so much from bosses in all, and the cleave: how wide it starts and grows. */
    static final int BERSERK_LEECH = 2, BOSS_LEECH_MOST = 20;
    static final float CLEAVE_WIDE = 2.3f, CLEAVE_GROWS = .3f;
    static final int CLEAVE_COMBO_MOST = 5;
    /** The stilled hour: how fast ordinary creatures and what they throw go, and how fast a boss. And how many blows are held back at most. */
    static final float STILL_RATE = .15f, STILL_BOSS_RATE = .6f;
    static final int STILL_QUEUE = 40;
    /** The tide: how fast she goes, how far on each side the first wave of ink reaches, and how wide the burst is where she forms again. */
    static final float TIDE_SPEED = 15, TIDE_REACH = 11, TIDE_REFORM = 3.4f;
    /** The giant: how many times as big, how far apart her steps are, how far a step shakes, how long it staggers. */
    static final float TITAN_SIZE = 2.5f, TITAN_STRIDE = 1.5f, TITAN_QUAKE = 6, TITAN_DAZE = 1.2f, TITAN_MIGHT = 1.5f, TITAN_CRUSH = 3;
    /** The legion: how many echoes, how far behind her each follows (in seconds of her own path), and how much of a blow theirs is. */
    static final int ECHOES = 3;
    static final float ECHO_LAG = .32f, ECHO_BLOW = .6f;
    /** The bloom: HP back every second, how wide a burst of roses is, how long a link of the chain waits, and how many links at most. */
    static final int BLOOM_HEAL = 3, BLOOM_CHAIN = 24;
    static final float BLOOM_WIDE = 2.6f, BLOOM_LINK = .12f;
    /** The most blows of its own that an ultimate lands on one who waits at the end, in one calling on. */
    static final int BOSS_HITS = 4;

    /** What each does, in plain words, for its card in the tree. */
    static String does(int which) {
        switch (which) {
            case BERSERK: return "Her blows land twice as hard, come two and a half times as fast and give back HP, and every swing cleaves all round her, wider with every strike; "
                    + "nothing knocks her back. But she cannot guard (no dodge, no shrug, no shield, no ink ball) and a hurt costs half as much again, and when it ends she is spent for "
                    + (int) EXHAUSTED_LASTS + " seconds: slow, and slower to strike. A boss feels the blows as one each, and gives back little";
            case STILL: return "The world slows to a sixth: creatures, what they throw, all of it. She does not. Every blow she lands is held where it fell, marked with a clock, and when "
                    + "time runs again they all land together. A boss is only slowed to six tenths, and takes his blows as they come";
            case TIDE: return "She becomes a wave of ink and sweeps across the land, nothing able to touch her: every creature on the screen is ink-poisoned as she breaks, and every one she "
                    + "passes through is struck twice. Then she forms again in a burst of ink. She cannot strike while she is ink. A boss is struck once, and not poisoned";
            case TITAN: return "She grows two and a half times as big, her blows reach that much further and land half as hard again; whatever she walks into is crushed, every step "
                    + "shakes the ground and staggers every creature near, and the cracks and false walls in her way break open. Bosses are not staggered, and take her blows as one each";
            case LEGION: return "Three echoes of her follow her path a moment behind and strike whenever she strikes, from where they stand: three more of every swing and every shot. "
                    + "They take no hurts and give none. A boss can only be struck so many times by them";
            case BLOOM: return "A rose blooms round her: nothing hurts her, and she heals as it grows. Every creature she beats bursts into roses that beat what is near, and what those beat "
                    + "bursts in turn, in a chain of up to " + BLOOM_CHAIN + ". It ends in one last bloom. A boss is struck by the roses only so many times";
            default: return "";
        }
    }

    /** The line a card says under it: what calling it on costs. */
    static String costs(int which) {
        return "Ultimate: " + (int) COST + " energy, " + (int) LASTS[which] + " seconds, then " + (int) REST[which] + " seconds to rest; one at a time. It costs " + POINTS + " points to take, and needs both of the " + RunSkills.CLASSES[BRANCH[which]].toLowerCase(java.util.Locale.ROOT) + "' actives first";
    }

    /** The word she shows as it begins (and the colour of its light, lit by each in its own way). */
    static final String[] SHOUT = {"", "RAMPAGE!", "TIME STOPS", "TIDE OF INK", "COLOSSUS!", "LEGION!", "ROSE BLOOM"};
    static final int[] COLOURS = {0, 0xFFE0362F, 0xFFB9C8D8, 0xFF27348F, 0xFFFFC433, 0xFF9A6AE8, 0xFFFF6FA0};
    /** The sound each begins with, and ends with ({@link RunAudio}). */
    static final String[] START_SOUND = {"", "ult_berserk", "ult_time", "ult_tide", "ult_titan", "ult_legion", "ult_bloom"};
    static final String[] END_SOUND = {"", "ult_berserk_end", "ult_time_end", "ult_tide_end", "ult_titan_end", "ult_legion_end", "ult_bloom_end"};
}
