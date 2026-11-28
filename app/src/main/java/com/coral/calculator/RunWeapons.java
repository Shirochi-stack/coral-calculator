package com.coral.calculator;

/**
 * The named weapons: a hundred and twenty-eight more things to fight with, spread over the thirteen classes (nine or ten of each), each its own
 * item with its own name, picture, power, reach and pace, and (most of them) something it does besides: burning, freezing, poisoning, draining
 * life, great blows. They are found in the lands as the plain weapons are, in place of one now and then, by the chapter they belong to and
 * as rare as they may be; a thing found is held in her hands by the same grips as the rest, laid along its own picture's handle and tip.
 *
 * <p>A named weapon is a loot code of a fifth sort: its "raw type" is {@link RunLoot#NAMED}, which {@link RunLoot#type} reads as a weapon, so that
 * everything that treats weapons treats these; its theme is its chapter, and its base the class it is of. The table is {@link RunWeaponData}
 * (made by {@code tools/make_weapons.py} from {@code tools/weapons_table.py}).
 */
final class RunWeapons {
    private RunWeapons() {}

    /** Where a named weapon's picture is among the loot icon numbers: from here up they are in the weapons' own sheet. */
    static final int ICON_BASE = 20000;
    /** How often, in a hundred, a weapon that drops is a named one when there is one of its class, its chapter and its rarity. */
    static final int SHARE = 40;

    private static final int[] INDEX = new int[16 * 16], PASSIVE = new int[RunWeaponData.COUNT];

    static {
        java.util.Arrays.fill(INDEX, -1);
        for (int index = 0; index < RunWeaponData.COUNT; index++) {
            INDEX[RunWeaponData.ERA[index] * 16 + RunWeaponData.KIND[index]] = index;
            PASSIVE[index] = RunWeaponPassives.of(RunWeaponData.EFFECTS[index], index);
        }
    }

    /** What a named weapon does at gold and better besides its figures (see {@link RunWeaponPassives}). */
    static int passiveOf(int index) { return PASSIVE[index]; }

    static int count() { return RunWeaponData.COUNT; }

    /** The code of a named weapon at a rarity (clamped to the rarities it is found as). */
    static int code(int index, int rarity) {
        return RunLoot.code(RunLoot.NAMED, RunWeaponData.ERA[index], RunWeaponData.KIND[index], clamp(index, rarity));
    }

    static int clamp(int index, int rarity) { return Math.max(RunWeaponData.LOW[index], Math.min(RunWeaponData.HIGH[index], rarity)); }

    /** Whether a loot code is a named weapon. */
    static boolean named(int code) { return code > 0 && code / 8 / 64 / 16 >= RunLoot.NAMED; }

    /** Which of the table it is: its place in it; -1 for a code that is none. */
    static int indexOf(int code) {
        if (!named(code)) return -1;
        int era = RunLoot.theme(code), kind = RunLoot.base(code);
        return era < 16 && kind < 16 ? INDEX[era * 16 + kind] : -1;
    }

    static String name(int code) {
        int index = indexOf(code);
        return index < 0 ? "Unknown weapon" : (RunLoot.rarity(code) == RunLoot.ROSE ? "Rose " : "") + RunWeaponData.NAMES[index];
    }

    static String look(int code) {
        int index = indexOf(code);
        return index < 0 ? "" : RunWeaponData.LOOKS[index];
    }

    /** How many blows more than the plain thing it is used as: its own, by its rarity (a rose one a great deal), and by how late a chapter it is. */
    static int blows(int code) {
        int index = indexOf(code);
        if (index < 0) return 0;
        int rarity = RunLoot.rarity(code);
        return RunWeaponData.POWER[index] + (rarity == RunLoot.ROSE ? 5 : rarity * 3 / 4) + RunLoot.theme(code) / 4;
    }

    /** Its reach and its rest, as parts of what the class has, the rarer the further and the quicker. */
    static float reach(int code) {
        int index = indexOf(code);
        return index < 0 ? 1 : (1 + .06f * RunLoot.rarity(code)) * (1 + RunWeaponData.REACH[index] / 100f);
    }

    static float rest(int code) {
        int index = indexOf(code);
        return index < 0 ? 1 : (1 - .05f * RunLoot.rarity(code)) * (1 + RunWeaponData.REST[index] / 100f);
    }

    /** What a named weapon does as an effect of the game's, at its rarity: zero for what it does not do (or for what is not a named weapon). */
    static int effect(int code, int effect) {
        int index = indexOf(code);
        if (index < 0) return 0;
        int sum = 0;
        int[] pairs = RunWeaponData.EFFECTS[index];
        for (int at = 0; at + 1 < pairs.length; at += 2) if (pairs[at] == effect) sum += RunLoot.figure(effect, pairs[at + 1], RunLoot.rarity(code));
        return sum;
    }

    /** Each of its effects as the words of the card: figure included. */
    static java.util.List<String> effectWords(int code) {
        java.util.List<String> words = new java.util.ArrayList<>();
        int index = indexOf(code);
        if (index < 0) return words;
        int[] pairs = RunWeaponData.EFFECTS[index];
        for (int at = 0; at + 1 < pairs.length; at += 2) words.add(RunLoot.said(pairs[at], RunLoot.figure(pairs[at], pairs[at + 1], RunLoot.rarity(code))));
        return words;
    }

    /** Armour of red or better has a nova when she is hurt: a burst round her (see {@link RunWeaponPassives.State#hurt}). */
    static boolean armourNova(int armour) { return armour != 0 && RunLoot.type(armour) == RunLoot.ARMOUR && RunLoot.rarity(armour) >= RunLoot.RED; }

    /** A wand's element: what its gimmick is made of (fire, ice, lightning, ink); the chapter's own when it has none. */
    static int element(int code) {
        int index = indexOf(code);
        if (index >= 0) {
            if (effect(code, RunLoot.BURN) > 0) return RunArms.FIRE;
            if (effect(code, RunLoot.FREEZE) > 0) return RunArms.ICE;
            if (effect(code, RunLoot.SPARK) > 0) return RunArms.LIGHTNING;
            if (effect(code, RunLoot.POISON) > 0) return RunArms.INKY;
        }
        return RunLoot.theme(code) % 4;
    }

    /**
     * A weapon that has dropped (its code a plain one) is, now and then, a named one instead: of its class, of a chapter up to three before
     * the one it dropped in (or its own, or the next), at a rarity it may be. {@code roll} is any number the land's dice gave.
     */
    static int maybe(int plain, int roll) {
        if (RunLoot.type(plain) != RunLoot.WEAPON || named(plain) || Math.floorMod(roll / 13, 100) >= SHARE) return plain;
        int theme = RunLoot.theme(plain), kind = RunLoot.base(plain) % 13, rarity = RunLoot.rarity(plain);
        int[] fit = new int[RunWeaponData.COUNT];
        int found = 0;
        for (int index = 0; index < RunWeaponData.COUNT; index++) {
            if (RunWeaponData.KIND[index] != kind) continue;
            int era = RunWeaponData.ERA[index];
            if (era > theme + 1 || era < theme - 3) continue;
            // (It is not found worse than it may be: but a rarity better than it may be is brought down to its best.)
            if (rarity < RunWeaponData.LOW[index]) continue;
            fit[found++] = index;
        }
        if (found == 0) return plain;
        return code(fit[Math.floorMod(roll / 1300, found)], rarity);
    }
}
