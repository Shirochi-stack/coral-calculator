package com.coral.calculator;

/**
 * Potions: what each is (its name, colour, bottle and price), what it does, how long it takes to drink and to be ready again, and the rules
 * round them. Heals share one cooldown, longer for a bigger heal, and in a fight with one who waits or a keeper it is longer still and only so
 * many heals may be drunk; each buff has its own, and cannot be drunk again while it lasts. A potion takes effect only when it has been drunk
 * to the end; a hard blow, a jump or a swap stops the drink, and the potion is kept (with a short cooldown). Permanent potions, the Vampire
 * Merchant's, are drunk from the bag, few of them and more as she trusts Halgakos, and no more than a few in a lifetime. The tree's alchemy
 * shortens drinks and cooldowns, but cooldowns never by more than half.
 */
final class RunPotions {
    private RunPotions() {}

    static final int HEAL_S = 0, HEAL_M = 1, HEAL_L = 2, REGEN = 3, MIGHT = 4, GUARD = 5, SWIFT = 6, EMBER = 7, FROST = 8, VENOM = 9, LUCK = 10, RICHES = 11,
            STAR = 12, CLEANSE = 13, LIFE = 14, FURY = 15, BASTION = 16, INSIGHT = 17, COUNT = 18;
    static final String[] NAMES = {"Little crimson draught", "Draught of the night", "Grand moonlit draught", "Heartsease", "Draught of wrath", "Ironbark tonic",
            "Featherfoot", "Emberward", "Frostward", "Venomward", "Clover cordial", "Gilded elixir", "Starveil", "Lotus water",
            "Heartblood", "Fangfire", "Bastion's draught", "Moon-eye"};
    static final String[] WORDS = {"Mends a quarter of her", "Mends half of her", "Mends all of her", "Mends two fifths of her over ten seconds",
            "Her blows hit a third harder for 30 seconds", "Hurts take a third less for 30 seconds", "A quarter faster for 30 seconds",
            "Nothing burns her for 40 seconds", "Nothing chills her for 40 seconds", "Nothing poisons her for 40 seconds",
            "Creatures drop pluses more often for 45 seconds", "Every plus is worth two for 30 seconds", "Nothing can hurt her for 5 seconds",
            "Burning, chill and poison washed away", "Ten more health, for ever", "Her blows a little harder, for ever", "Hurts a little less, for ever",
            "Her tomes, staffs and wands a little stronger, for ever"};
    static final int[] COLOURS = {0xFFE0304A, 0xFFC8203A, 0xFFA01030, 0xFFF07AA0, 0xFFF0602A, 0xFF5A7FA8, 0xFF7AD8F0, 0xFFF0A030, 0xFF9AD0F8, 0xFF5AC060,
            0xFF9AC83A, 0xFFF0C83A, 0xFFE8D8FF, 0xFFF4ECD0, 0xFFB0102A, 0xFFE06A20, 0xFF2A3A8A, 0xFF8A4AD0};
    /** What each costs at the merchant's stall before she likes Halgakos. */
    static final int[] PRICE = {30, 70, 150, 90, 80, 80, 70, 60, 60, 60, 100, 120, 200, 50, 900, 900, 900, 900};

    static final int HEAL = 0, BUFF = 1, PERMANENT = 2;

    static int family(int potion) { return potion <= REGEN ? HEAL : potion <= CLEANSE ? BUFF : PERMANENT; }

    static boolean heals(int potion) { return family(potion) == HEAL; }

    static boolean permanent(int potion) { return family(potion) == PERMANENT; }

    /** What a heal mends, in a hundred of her most (the heartsease over its ten seconds). */
    static final int[] HEAL_PERCENT = {25, 50, 100, 40};
    static final float REGEN_SECONDS = 10;
    /** How long a buff lasts, by potion (nought for those that are not buffs, or are done at once). */
    static float lasts(int potion) {
        switch (potion) {
            case MIGHT: case GUARD: case SWIFT: case RICHES: return 30;
            case EMBER: case FROST: case VENOM: return 40;
            case LUCK: return 45;
            case STAR: return 5;
            default: return 0;
        }
    }

    /** How long it takes to drink, before the tree: a small heal is a quick gulp, a grand one longer. */
    static float drinkSeconds(int potion) {
        switch (potion) {
            case HEAL_S: case CLEANSE: return .8f;
            case HEAL_M: case REGEN: return 1;
            case HEAL_L: return 1.2f;
            default: return .9f;
        }
    }

    /** Its cooldown before the tree: heals 12 to 20 seconds by size (shared by all heals); a buff its time and 20 seconds more. */
    static float cooldown(int potion) {
        switch (potion) {
            case HEAL_S: return 12;
            case HEAL_M: case REGEN: return 16;
            case HEAL_L: return 20;
            case CLEANSE: return 15;
            default: return permanent(potion) ? 0 : lasts(potion) + 20;
        }
    }

    /** In a fight with one who waits or a keeper: heals take half as long again to be ready, and no more than three are drunk in it. */
    static final float BOSS_HEAL_SLOWER = 1.5f;
    static final int BOSS_HEALS = 3;
    /** A drink stopped part way: the potion is kept, and is ready again after this. */
    static final float INTERRUPTED_COOL = 3;
    /** The most the tree and what she wears may take off a cooldown, together: half. */
    static final float MOST_OFF = .5f;

    /** A cooldown as it is for her: less by the tree's herbalism and her gear's (in a hundred), never by more than half; longer for heals in a fight with a boss. */
    static float cooldownFor(int potion, int herbalism, int gear, boolean boss) {
        float off = Math.min(MOST_OFF, Math.max(0, herbalism + gear) / 100f);
        return cooldown(potion) * (1 - off) * (boss && heals(potion) ? BOSS_HEAL_SLOWER : 1);
    }

    /** How long a drink takes for her: shorter by quick sipping (in a hundred), at most by half. */
    static float drinkFor(int potion, int quickSip) { return drinkSeconds(potion) * (1 - Math.min(.5f, Math.max(0, quickSip) / 100f)); }

    /** The permanent potions of each kind she may have bought, by the merchant's affinity: one at 20, and one more every 20 after, five at a hundred. */
    static final int PERMANENT_MOST = 5;

    static int permanentUnlocked(int affinity) { return Math.max(0, Math.min(PERMANENT_MOST, affinity / 20)); }

    /** How many of a permanent potion are still on her shelf: what she unlocks less what has been bought; never past the lifetime five. */
    static int permanentStock(int affinity, int bought) { return Math.max(0, permanentUnlocked(affinity) - Math.max(0, bought)); }

    /** What a permanent potion gives each time: ten health; and in a hundred, attack, defence or intelligence. */
    static final int LIFE_HEALTH = 10, STAT_PERCENT = 3;

    /** What is in her pouch, from what she carries: every potion she has one of that can be drunk in a land (not the permanent ones), in order. */
    static int[] pouch(int[] have) {
        int count = 0;
        for (int potion = 0; potion < COUNT; potion++) if (!permanent(potion) && have[potion] > 0) count++;
        int[] out = new int[count];
        count = 0;
        for (int potion = 0; potion < COUNT; potion++) if (!permanent(potion) && have[potion] > 0) out[count++] = potion;
        return out;
    }

    /** The one her pouch button shows: the one chosen if she still has one, else the first in the pouch; -1 for an empty pouch. */
    static int shown(int chosen, int[] have) {
        if (chosen >= 0 && chosen < COUNT && !permanent(chosen) && have[chosen] > 0) return chosen;
        int[] in = pouch(have);
        return in.length > 0 ? in[0] : -1;
    }

    /** Keys of what is kept: how many of a potion she carries; how many permanent ones of a kind she has bought, and drunk. */
    static final String HAVE = "run_pot_", BOUGHT = "run_pot_bought_", DRUNK = "run_pot_drunk_", CHOSEN = "run_pot_chosen";
}
