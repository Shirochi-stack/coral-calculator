package com.coral.calculator;

/**
 * What she looks like, apart from what she is: a layer of cosmetics that never touches a stat. A toggle has three
 * settings: {@link #DEFAULT} (her own look, ignoring both), {@link #GEAR} (what she wears of what she finds shows on her)
 * and {@link #VANITY} (the outfit she chose at the guild counter shows instead of her gear). Nothing in here is read by
 * {@link PlusRun}; it is drawn over the same pixels of every frame by {@link VanityPaint}.
 * <p>
 * Saves: all of it lives under its own keys, and a save that has none of them is {@link #DEFAULT}.
 */
final class RunVanity {
    private RunVanity() {}

    static final int DEFAULT = 0, GEAR = 1, VANITY = 2;
    static final String[] MODE_NAMES = {"Default", "Show gear", "Vanity"};
    static final String[] MODE_DOES = {"Her own look, whatever she wears", "What she wears shows on her", "The outfit she chose shows instead"};
    static final String MODE_KEY = "run_van_mode", OUTFIT_KEY = "run_van_outfit", OWN_KEY = "run_van_own",
            EARS_KEY = "run_van_ears", EARS_OWN_KEY = "run_van_ears_own", HORNS_KEY = "run_van_horns", HORNS_OWN_KEY = "run_van_horns_own";

    /** Head parts: set apart from the outfit, and combined with any. Their art comes with the shop; the choice is kept now. */
    static final int EARS_CAT = 0, EARS_NONE = 1, EARS_FOX = 2, EARS_BUNNY = 3, EARS_APRON = 4;
    static final int HORNS_DEFAULT = 0, HORNS_NONE = 1, HORNS_ELF = 2;
    static final String[] EARS = {"Cat ears", "No ears", "Fox ears", "Bunny ears", "Apron headband"};
    static final String[] HORNS = {"Horns", "No horns", "Pointed elf ears"};

    /** How a cloth is drawn: a ramp from its darkest to its lightest, a trim, a pattern and what is added on her. */
    static final class Style {
        final int dark, mid, light, trim, gem, pattern;
        final boolean shimmer, pauldron, brooch;

        Style(int dark, int mid, int light, int trim, int pattern, boolean shimmer, boolean pauldron, boolean brooch, int gem) {
            this.dark = dark;
            this.mid = mid;
            this.light = light;
            this.trim = trim;
            this.pattern = pattern;
            this.shimmer = shimmer;
            this.pauldron = pauldron;
            this.brooch = brooch;
            this.gem = gem;
        }

        String key() {
            return Integer.toHexString(dark) + Integer.toHexString(mid) + Integer.toHexString(light) + Integer.toHexString(trim) + pattern
                    + (shimmer ? "s" : "") + (pauldron ? "p" : "") + (brooch ? "b" + Integer.toHexString(gem) : "");
        }
    }

    static final int PLAIN = 0, STARS = 1, STRIPES = 2, DIAMONDS = 3, ROSES = 4;

    /** The outfits made so far, all of them over every frame she is drawn in. */
    static final String[] NAMES = {"Pajama Set", "Gothic Dress", "Knight's Tabard"};
    static final String[] DOES = {"Soft lavender with stars, for resting", "Black and violet lace, a red stone at the throat", "Blue and white, a gold edge, one pauldron"};
    static final int[] PRICES = {400, 700, 900};
    static final Style[] STYLES = {
            new Style(0xFF3B3F7A, 0xFF7F86D6, 0xFFE9EBFF, 0xFFFFE08A, STARS, false, false, false, 0),
            new Style(0xFF1A1020, 0xFF4A2A5E, 0xFFB89AD0, 0xFFD8C8EC, DIAMONDS, false, false, true, 0xFFE0334A),
            new Style(0xFF2B3A55, 0xFF5C7CB0, 0xFFEAF0FA, 0xFFE8C65A, PLAIN, false, true, true, 0xFFE8C65A)};
    static int count() { return NAMES.length; }

    /** What it needs of a save: numbers by name. */
    interface Keep {
        int tally(String key);
        void keep(String key, int value);
    }

    static Keep on(PlaySession session) {
        return new Keep() {
            @Override public int tally(String key) { return session.tally(key); }
            @Override public void keep(String key, int value) { session.keep(key, value); }
        };
    }

    // The choice

    static int mode(Keep session) { return Math.max(DEFAULT, Math.min(VANITY, session.tally(MODE_KEY))); }

    /** The outfit chosen (a place in {@link #NAMES}), or -1 for none; only one she owns. */
    static int outfit(Keep session) {
        int chosen = session.tally(OUTFIT_KEY) - 1;
        return chosen >= 0 && chosen < count() && owns(session, chosen) ? chosen : -1;
    }

    static boolean owns(Keep session, int outfit) { return (session.tally(OWN_KEY) >> outfit & 1) != 0; }

    /** Buys an outfit with pluses, and chooses it. False, and nothing taken, if she has it or cannot pay. */
    static boolean buy(Keep session, int outfit) {
        int bank = session.tally("run_bank");
        if (outfit < 0 || outfit >= count() || owns(session, outfit) || bank < PRICES[outfit]) return false;
        session.keep("run_bank", bank - PRICES[outfit]);
        session.keep(OWN_KEY, session.tally(OWN_KEY) | 1 << outfit);
        session.keep(OUTFIT_KEY, outfit + 1);
        return true;
    }

    /** Chooses an outfit she owns (or -1 for none). */
    static boolean choose(Keep session, int outfit) {
        if (outfit >= 0 && (outfit >= count() || !owns(session, outfit))) return false;
        session.keep(OUTFIT_KEY, outfit + 1);
        return true;
    }

    static void setMode(Keep session, int mode) { session.keep(MODE_KEY, Math.max(DEFAULT, Math.min(VANITY, mode))); }

    // What is drawn

    /**
     * The style she is drawn in, or null for her own look. In {@link #GEAR} the armour she wears gives the cloth and its
     * rarity the trim and the light on it; the first accessory is a stone at her chest. In {@link #VANITY} the outfit
     * chosen, and nothing of her gear; with none chosen, her own look.
     */
    static Style styleFor(int mode, int outfit, RunLoot.Worn worn) {
        if (mode == VANITY) return outfit >= 0 && outfit < count() ? STYLES[outfit] : null;
        if (mode != GEAR || worn == null || !worn.any()) return null;
        int gem = worn.first != 0 ? RunLoot.COLOURS[RunLoot.rarity(worn.first) % RunLoot.COLOURS.length]
                : worn.second != 0 ? RunLoot.COLOURS[RunLoot.rarity(worn.second) % RunLoot.COLOURS.length] : 0;
        if (worn.armour == 0) return new Style(0, 0, 0, 0, PLAIN, false, false, true, gem);
        int base = RunLoot.base(worn.armour), rarity = RunLoot.rarity(worn.armour), theme = RunLoot.theme(worn.armour);
        if (base == RunLoot.ARMOURS.length - 1 || rarity == RunLoot.ROSE) {
            return new Style(0xFF8E2F5E, 0xFFE0709F, 0xFFFFE3EE, 0xFFF2C65A, ROSES, true, true, true, gem != 0 ? gem : 0xFFFF6FB5);
        }
        float hue = (theme * 47 + base * 13) % 360;
        int dark = android.graphics.Color.HSVToColor(new float[]{hue, .55f, .35f}), mid = android.graphics.Color.HSVToColor(new float[]{hue, .45f, .75f}),
                light = android.graphics.Color.HSVToColor(new float[]{hue, .12f, .98f});
        int trim = rarity >= RunLoot.BLUE ? RunLoot.COLOURS[rarity] : 0;
        return new Style(dark, mid, light, trim, 1 + base % 4, rarity >= RunLoot.GOLD, rarity >= RunLoot.PURPLE, gem != 0, gem);
    }
}
