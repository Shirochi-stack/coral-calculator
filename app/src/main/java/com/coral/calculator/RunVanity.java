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
    /** What each part says it is, what it costs in pluses and treats (the first of each, her own, is hers already), and where its pictures are. */
    static final String[] EARS_DOES = {"Her own, pink and soft", "Smooth hair on top, no ears", "Russet, tipped in brown, with cream fur", "White and pink, the tips bent forward",
            "A white frilled maid headband instead"};
    static final String[] HORNS_DOES = {"Her own, dark and curled", "Smooth hair, no horns", "Long and pointed, out to the sides, in place of the horns"};
    static final int[] EARS_PRICE = {0, 300, 600, 600, 500}, EARS_TREATS = {0, 2, 4, 4, 3};
    static final int[] HORNS_PRICE = {0, 400, 700}, HORNS_TREATS = {0, 3, 5};
    static final String[] EARS_ART = {null, "noears", "fox", "bunny", "apron"}, HORNS_ART = {null, "nohorns", "elf"};
    /** Whether the pictures of the parts are in the app. */
    static final boolean[] EARS_READY = {true, false, false, false, false}, HORNS_READY = {true, false, false};

    /**
     * How she is drawn: either a cloth (a ramp from its darkest to its lightest, a trim, a pattern and what is added on her,
     * painted over the frames she already has) or an outfit that was drawn whole, every frame of every sheet of it, which is
     * found by {@code art} (assets/vanity/ART/SHEET.webp) and takes the place of her own frames.
     */
    static final class Style {
        final int dark, mid, light, trim, gem, pattern;
        final boolean shimmer, pauldron, brooch;
        final String art;
        /** Which ears and horns (a place in {@link #EARS} and {@link #HORNS}) are shown instead of her own, drawn over the same frames. */
        int ears, horns;

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
            this.art = null;
        }

        Style(String art) {
            dark = mid = light = trim = gem = pattern = 0;
            shimmer = pauldron = brooch = false;
            this.art = art;
        }

        /** This look with ears and horns chosen. */
        Style heads(int ears, int horns) {
            Style copy = art != null ? new Style(art) : new Style(dark, mid, light, trim, pattern, shimmer, pauldron, brooch, gem);
            copy.ears = ears;
            copy.horns = horns;
            return copy;
        }

        /** Only the ears and the horns, in her own clothes. */
        static Style onlyHeads(int ears, int horns) { return new Style(0, 0, 0, 0, PLAIN, false, false, false, 0).heads(ears, horns); }

        String key() {
            return body() + (ears != 0 || horns != 0 ? "e" + ears + "h" + horns : "");
        }

        private String body() {
            if (art != null) return "art:" + art;
            return Integer.toHexString(dark) + Integer.toHexString(mid) + Integer.toHexString(light) + Integer.toHexString(trim) + pattern
                    + (shimmer ? "s" : "") + (pauldron ? "p" : "") + (brooch ? "b" + Integer.toHexString(gem) : "");
        }
    }

    static final int PLAIN = 0, STARS = 1, STRIPES = 2, DIAMONDS = 3, ROSES = 4;

    /**
     * The outfits, in the order of their bits in what she owns (never reorder: add at the end). Each has its name, what it is,
     * what it costs in pluses and in treats, the quest that gives it instead of a price (or -1; a quest's outfit is not sold),
     * and how it is drawn. An outfit drawn whole is on sale once its pictures are in the app ({@link #READY}).
     */
    static final String[] NAMES = {"Pajama Set", "Gothic Dress", "Knight's Tabard", "Bunny Girl", "Maid Dress", "Sloppy\u2606Stage", "Seaside Swimsuit", "Bunny Girl (Gacha)"};
    static final String[] DOES = {"Soft lavender with stars, for resting", "Black and violet lace, a red stone at the throat", "Blue and white, a gold edge, one pauldron",
            "The classic bunny suit: black satin, tights, a collar, cuffs and a bow tie", "A short black dress, a frilled apron, white stockings with ribbon garters", "The idol stage dress from her song: ribbons, frills and a layered skirt",
            "A striped sailor swim dress with a frilled hem, a red ribbon and sandals",
            "Red satin bodysuit, a short black tailcoat, tights, a collar and bow tie, and a pom tail"};
    static final int[] PRICES = {400, 700, 0, 1500, 1200, 0, 0, 1500};
    static final int[] TREATS = {3, 5, 0, 8, 6, 0, 0, 8};
    static final int[] QUEST = {-1, -1, 2, -1, -1, 0, 1, -1};
    static final Style[] STYLES = {
            new Style(0xFF5B4FA8, 0xFF9A8CE0, 0xFFF0E9FF, 0xFFFFE08A, STARS, false, false, false, 0),
            new Style(0xFF1A1020, 0xFF4A2A5E, 0xFFB89AD0, 0xFFD8C8EC, DIAMONDS, false, false, true, 0xFFE0334A),
            new Style(0xFF2B3A55, 0xFF5C7CB0, 0xFFEAF0FA, 0xFFE8C65A, PLAIN, false, true, true, 0xFFE8C65A),
            new Style("bunny"), new Style("maid"), new Style("idol"), new Style("swim1"), new Style("bunnyg")};
    /** Which outfits can be had now: those whose pictures are all there. */
    static final boolean[] READY = {true, true, true, true, true, true, true, false};
    static int count() { return NAMES.length; }
    static boolean ready(int outfit) { return outfit >= 0 && outfit < count() && READY[outfit]; }

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
        if (outfit < 0 || outfit >= count() || !ready(outfit) || QUEST[outfit] >= 0 || owns(session, outfit) || bank < PRICES[outfit]) return false;
        session.keep("run_bank", bank - PRICES[outfit]);
        session.keep(OWN_KEY, session.tally(OWN_KEY) | 1 << outfit);
        VanityPictures.unlockOutfit(session, outfit);
        session.keep(OUTFIT_KEY, outfit + 1);
        return true;
    }

    /** Buys an outfit with treats instead. */
    static boolean buyWithTreats(Keep session, int outfit) {
        int treats = session.tally("run_treats");
        if (outfit < 0 || outfit >= count() || !ready(outfit) || QUEST[outfit] >= 0 || owns(session, outfit) || treats < TREATS[outfit]) return false;
        session.keep("run_treats", treats - TREATS[outfit]);
        session.keep(OWN_KEY, session.tally(OWN_KEY) | 1 << outfit);
        VanityPictures.unlockOutfit(session, outfit);
        session.keep(OUTFIT_KEY, outfit + 1);
        return true;
    }

    /** Gives an outfit as a quest's reward. */
    static void give(Keep session, int outfit) {
        if (outfit >= 0 && outfit < count()) {
            session.keep(OWN_KEY, session.tally(OWN_KEY) | 1 << outfit);
            VanityPictures.unlockOutfit(session, outfit);
        }
    }

    /** Chooses an outfit she owns (or -1 for none). */
    static boolean choose(Keep session, int outfit) {
        if (outfit >= 0 && (outfit >= count() || !owns(session, outfit))) return false;
        session.keep(OUTFIT_KEY, outfit + 1);
        return true;
    }

    // The head: ears and horns, chosen apart from the outfit

    static int ears(Keep save) {
        int one = save.tally(EARS_KEY);
        return one > 0 && one < EARS.length && earsOwned(save, one) ? one : 0;
    }

    static int horns(Keep save) {
        int one = save.tally(HORNS_KEY);
        return one > 0 && one < HORNS.length && hornsOwned(save, one) ? one : 0;
    }

    static boolean earsOwned(Keep save, int one) { return one == 0 || (save.tally(EARS_OWN_KEY) >> one & 1) != 0; }

    static boolean hornsOwned(Keep save, int one) { return one == 0 || (save.tally(HORNS_OWN_KEY) >> one & 1) != 0; }

    static boolean earsReady(int one) { return one >= 0 && one < EARS.length && EARS_READY[one]; }

    static boolean hornsReady(int one) { return one >= 0 && one < HORNS.length && HORNS_READY[one]; }

    /** Buys a part (ears or horns, with pluses or treats) and puts it on. */
    static boolean buyPart(Keep save, boolean ears, int one, boolean treats) {
        int count = ears ? EARS.length : HORNS.length;
        if (one <= 0 || one >= count || !(ears ? earsReady(one) : hornsReady(one)) || (ears ? earsOwned(save, one) : hornsOwned(save, one))) return false;
        String pay = treats ? "run_treats" : "run_bank";
        int price = treats ? (ears ? EARS_TREATS[one] : HORNS_TREATS[one]) : (ears ? EARS_PRICE[one] : HORNS_PRICE[one]);
        if (save.tally(pay) < price) return false;
        save.keep(pay, save.tally(pay) - price);
        save.keep(ears ? EARS_OWN_KEY : HORNS_OWN_KEY, save.tally(ears ? EARS_OWN_KEY : HORNS_OWN_KEY) | 1 << one);
        VanityPictures.unlockPart(save, ears, one);
        save.keep(ears ? EARS_KEY : HORNS_KEY, one);
        return true;
    }

    /** Puts on a part she has (0 for her own). */
    static boolean wearPart(Keep save, boolean ears, int one) {
        if (one < 0 || one >= (ears ? EARS.length : HORNS.length) || !(ears ? earsOwned(save, one) : hornsOwned(save, one))) return false;
        save.keep(ears ? EARS_KEY : HORNS_KEY, one);
        return true;
    }

    static void setMode(Keep session, int mode) { session.keep(MODE_KEY, Math.max(DEFAULT, Math.min(VANITY, mode))); }

    // What is drawn

    /**
     * The style she is drawn in, or null for her own look. In {@link #GEAR} the armour she wears gives the cloth and its
     * rarity the trim and the light on it; the first accessory is a stone at her chest. In {@link #VANITY} the outfit
     * chosen, and nothing of her gear; with none chosen, her own look.
     */
    static Style styleFor(int mode, int outfit, RunLoot.Worn worn) { return styleFor(mode, outfit, worn, 0, 0); }

    /** As above, and with ears and horns: they show in {@link #VANITY} only, with the outfit or without one. */
    static Style styleFor(int mode, int outfit, RunLoot.Worn worn, int ears, int horns) {
        if (mode == VANITY) {
            Style style = outfit >= 0 && outfit < count() ? STYLES[outfit] : null;
            if (ears == 0 && horns == 0) return style;
            return style != null ? style.heads(ears, horns) : Style.onlyHeads(ears, horns);
        }
        if (mode != GEAR || worn == null || !worn.any()) return null;
        int gem = worn.first != 0 ? RunLoot.COLOURS[RunLoot.rarity(worn.first) % RunLoot.COLOURS.length]
                : worn.second != 0 ? RunLoot.COLOURS[RunLoot.rarity(worn.second) % RunLoot.COLOURS.length] : 0;
        if (worn.armour == 0) return new Style(0, 0, 0, 0, PLAIN, false, false, true, gem);
        int base = RunLoot.base(worn.armour), rarity = RunLoot.rarity(worn.armour), theme = RunLoot.theme(worn.armour);
        if (base == RunLoot.ARMOURS.length - 1 || rarity == RunLoot.ROSE) {
            return new Style(0xFF8E2F5E, 0xFFE0709F, 0xFFFFE3EE, 0xFFF2C65A, ROSES, true, true, true, gem != 0 ? gem : 0xFFFF6FB5);
        }
        float hue = (theme * 47 + base * 13) % 360;
        int dark = hsv(hue, .55f, .35f), mid = hsv(hue, .45f, .75f), light = hsv(hue, .12f, .98f);
        int trim = rarity >= RunLoot.BLUE ? RunLoot.COLOURS[rarity] : 0;
        return new Style(dark, mid, light, trim, 1 + base % 4, rarity >= RunLoot.GOLD, rarity >= RunLoot.PURPLE, gem != 0, gem);
    }

    private static int hsv(float hue, float saturation, float value) {
        int c = VanityPaint.hue(hue, saturation);
        int r = (int) ((c >> 16 & 255) * value), g = (int) ((c >> 8 & 255) * value), b = (int) ((c & 255) * value);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
