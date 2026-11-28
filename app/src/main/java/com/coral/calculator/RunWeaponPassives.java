package com.coral.calculator;

/**
 * What a named weapon of gold or better does that changes how she plays, beyond its figures: a proc or a state, by what the weapon is made of.
 * A burning one bursts, a freezing one shatters what is nearly beaten, a poisoning one jumps from one creature to the next, a draining one
 * rushes her on after a kill, a quick one builds a rhythm, a sharp one echoes. Gold does a little of it, red more, the rose most. What is
 * here is only the rules (it draws nothing and knows nothing of creatures): the game asks, per blow, how many more blows to count and
 * whether something is to burst or jump, and tells it when a creature is beaten.
 */
final class RunWeaponPassives {
    private RunWeaponPassives() {}

    static final int NONE = 0, ECHO = 1, SHATTER = 2, RHYTHM = 3, BURST = 4, ARC = 5, RUSH = 6, NOVA = 7, KINDS = 8;
    static final String[] NAMES = {"", "Echo", "Shatter", "Rhythm", "Burst", "Arc", "Rush", "Nova"};
    /** What each does, in words, with the figures for gold, red and the rose in order. */
    private static final String[] WORDS = {"",
            "Every %s blow strikes again for three quarters of its force",
            "Blows on a creature nearly beaten (a quarter of its strength left) count %s more",
            "Blows in quick succession build a rhythm: up to %s extra blows while it holds",
            "Every %s blow bursts where it lands, over a wide space",
            "%s of blows jump to the nearest other creature for half their force",
            "A creature beaten sends her rushing: %s quicker for a few seconds"};
    private static final String[][] FIGURES = {{}, {"fourth", "third", "second"}, {"half", "three quarters", "twice as many"}, {"1", "2", "3"}, {"sixth", "fifth", "fourth"},
            {"A third", "Nearly half", "More than half"}, {"a fifth", "a quarter", "a third"}};

    /** From gold: the rarity at which a weapon has its passive, and which of the three strengths it has. */
    static final int FROM = RunLoot.GOLD;

    static int strength(int rarity) { return rarity >= RunLoot.ROSE ? 2 : rarity >= RunLoot.RED ? 1 : 0; }

    /** What a named weapon's passive is by what it is made of: its gimmicks pick it, so that a flame sword bursts and a frost one shatters. */
    static int of(int[] effects, int index) {
        for (int at = 0; at + 1 < effects.length; at += 2) {
            int effect = effects[at];
            if (effect == RunLoot.BURN) return BURST;
            if (effect == RunLoot.FREEZE) return SHATTER;
            if (effect == RunLoot.POISON) return ARC;
            if (effect == RunLoot.LEECH) return RUSH;
            if (effect == RunLoot.CRIT || effect == RunLoot.CRITX) return ECHO;
            if (effect == RunLoot.HASTE) return RHYTHM;
            if (effect == RunLoot.SPARK) return ARC;
        }
        // (The rest, by their place: power and the plain ones alternate.)
        return index % 3 == 0 ? BURST : index % 3 == 1 ? SHATTER : RHYTHM;
    }

    /** The passive a thing found has: none unless it is a named weapon of gold or better. */
    static int passive(int code) {
        if (!RunWeapons.named(code) || RunLoot.rarity(code) < FROM) return NONE;
        int index = RunWeapons.indexOf(code);
        return index < 0 ? NONE : RunWeapons.passiveOf(index);
    }

    static String describe(int code) {
        int kind = passive(code);
        return kind == NONE ? "" : NAMES[kind] + ": " + String.format(WORDS[kind], FIGURES[kind][strength(RunLoot.rarity(code))]);
    }

    /** What is going on between blows. */
    static final class State {
        /** Blows dealt with this weapon (for echo and burst), the rhythm built and how long ago the last blow was, and how long the rush has to run. */
        int blows, rhythm;
        float since = 99, rush;
        /** How many times something has proc'd, what it was, and where (squares), for the game to show; and what is to happen when the blow is done. */
        int procs, lastProc;
        float procX, procY;
        boolean burst, arc;

        void tick(float seconds) {
            since += seconds;
            if (since > 1.4f) rhythm = 0;
            rush = Math.max(0, rush - seconds);
        }

        /** How much quicker the rush makes her, in the way of the game's haste effect (percent). */
        int haste(int code) {
            if (rush <= 0) return 0;
            return new int[]{20, 25, 33}[strength(RunLoot.rarity(code))];
        }

        /** Armour of red or better, hurt: a nova. Whether it goes off (not every time: once in a few hurts). */
        boolean hurt(int armour, float x, float y) {
            if (!RunWeapons.armourNova(armour)) return false;
            nova++;
            if (nova % (RunLoot.rarity(armour) >= RunLoot.ROSE ? 2 : 3) != 0) return false;
            proc(NOVA, x, y);
            return true;
        }

        int nova;

        private void proc(int kind, float x, float y) {
            procs++;
            lastProc = kind;
            procX = x;
            procY = y;
        }

        /**
         * A blow of the weapon lands for {@code dealt} on a creature with {@code fraction} of its strength left (0 to 1), at a place. Returns how many
         * more blows it counts for, and sets {@code burst} or {@code arc} for what is to happen when the blow is done.
         */
        int hit(int code, int dealt, float fraction, float x, float y, java.util.Random dice, int unit) {
            int kind = passive(code);
            burst = arc = false;
            if (kind == NONE) return 0;
            int strong = strength(RunLoot.rarity(code));
            int extra = 0;
            blows++;
            since = 0;
            switch (kind) {
                case ECHO:
                    if (blows % (4 - strong) == 0) {
                        extra = Math.max(1, Math.round(dealt * .75f));
                        proc(kind, x, y);
                    }
                    break;
                case SHATTER:
                    if (fraction <= .25f) {
                        extra = Math.max(1, Math.round(dealt * (strong == 0 ? .5f : strong == 1 ? .75f : 1f)));
                        proc(kind, x, y);
                    }
                    break;
                case RHYTHM:
                    rhythm = Math.min(3 + strong, rhythm + 1);
                    extra = Math.min(strong + 1, rhythm / 2) * unit;
                    if (extra > 0 && rhythm % 2 == 0) proc(kind, x, y);
                    break;
                case BURST:
                    if (blows % (6 - strong) == 0) {
                        burst = true;
                        proc(kind, x, y);
                    }
                    break;
                case ARC:
                    if (dice.nextInt(100) < 33 + strong * 14) {
                        arc = true;
                        proc(kind, x, y);
                    }
                    break;
                default:
                    break;
            }
            return extra;
        }

        /** A creature is beaten by a blow of the weapon. */
        void kill(int code, float x, float y) {
            if (passive(code) != RUSH) return;
            rush = 3 + strength(RunLoot.rarity(code));
            proc(RUSH, x, y);
        }
    }
}
