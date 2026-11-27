package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Her level, and what creatures drop. Her level has no cap: every creature and every one who waits gives her experience as it falls,
 * less for one far below her, and each level makes her blows land harder, hurts cost her less, and, now and then, gives her another
 * heart. What falls when a creature does is pluses, always, like coins; and now and then something to keep, of one of six rarities:
 * white, blue, purple, gold, red, and the Halgakos Rose, whose colour never stays still. The rose falls only from those who wait, from
 * the hardest creatures of the last lands, or is made at the smithy.
 */
final class RunLoot {
    private RunLoot() {}

    // Rarities

    static final int WHITE = 0, BLUE = 1, PURPLE = 2, GOLD = 3, RED = 4, ROSE = 5;
    static final String[] RARITIES = {"White", "Blue", "Purple", "Gold", "Red", "Halgakos Rose"};
    /** Each one's colour; the rose's is where its colours begin, and it goes round all of them. */
    static final int[] COLOURS = {0xFFF4F1EA, 0xFF4C9BFF, 0xFFB463FF, 0xFFFFC433, 0xFFFF4040, 0xFFFF6FB5};

    /** The rose's colour at a moment: round the colours of a rose, pink, gold, violet and back. */
    static int rose(float clock) {
        float hue = (330 + 120 * (float) Math.sin(clock * 1.7f)) % 360;
        return android.graphics.Color.HSVToColor(new float[]{hue < 0 ? hue + 360 : hue, .55f, 1});
    }

    /**
     * The chances of each rarity, in ten thousandths, for what an ordinary creature drops, for one of the hardest (the last lands'
     * and the champions'), and for one who waits (never white). The rose falls from none but the last two.
     */
    static final int[] CREATURE = {6300, 2600, 850, 220, 30, 0};
    static final int[] HARD = {4400, 3000, 1650, 650, 270, 30};
    static final int[] BOSS = {0, 3400, 3500, 2200, 800, 100};

    /** A rarity, by its chances, from a roll of 0 to 9999. */
    static int rarityOf(int[] odds, int roll) {
        int sum = 0;
        for (int rarity = 0; rarity < odds.length; rarity++) {
            sum += odds[rarity];
            if (roll < sum) return rarity;
        }
        return WHITE;
    }

    /** The chance, in ten thousandths, that a creature of a land of some level drops something to keep as well as its pluses: one who waits, always. */
    static int gearChance(boolean boss, boolean hard, int landLevel) { return boss ? 10000 : (hard ? 600 : 220) + landLevel * 8; }

    /** How many pluses a creature drops: one, and more for a tougher one; those who wait, a shower. */
    static int plusesFor(boolean boss, int tough) { return boss ? 14 + tough * 2 : 1 + (tough + 1) / 2; }

    // What is dropped

    static final int WEAPON = 0, ARMOUR = 1, ACCESSORY = 2;
    static final String[] TYPES = {"weapon", "armour", "accessory"};
    /** The twelve kinds of weapon. */
    static final String[] WEAPONS = {"Sword", "Katana", "Greatsword", "Book", "Summoning Staff", "Knife", "Pen", "Mace", "Hammer", "Wand", "Ink Gun", "Scythe"};
    /** The thirty armours, and the thirty accessories. */
    static final String[] ARMOURS = {"Paper Vest", "Linen Tunic", "Bookbinder's Apron", "Leather Jerkin", "Ribbon Mail", "Footnote Coat", "Archivist's Robe", "Inkproof Cloak",
            "Margin Plate", "Errata Brigandine", "Folio Cuirass", "Gilded Spine", "Index Scale", "Quill-Feather Mantle", "Vellum Gown", "Ledger Hauberk",
            "Chapter Breastplate", "Serif Lamellar", "Typeset Armour", "Gazette Duster", "Lantern Coat", "Slime-Glaze Shell", "Appendix Harness",
            "Tidewrack Raiment", "Palace Regalia", "Bookmark Banner Coat", "Dragonhide Jacket", "Colophon Aegis", "Mother Tongue Vestment", "Rosebound Armour"};
    static final String[] ACCESSORIES = {"Paper Ring", "Ribbon Bow", "Pencil Pin", "Bookmark Charm", "Ink Drop Pendant", "Lucky Plus", "Thimble", "Monocle",
            "Feather Earring", "Clockwork Brooch", "Comma Anklet", "Star Sticker", "Wax Seal", "Lantern Locket", "Compass Rose", "Slime Bead",
            "Shell Bracelet", "Crown Shard", "Quill Hairpin", "Dragon Scale Charm", "Footnote Tag", "Gazette Stamp", "Hourglass Pendant", "Index Card",
            "Moon Bookmark", "Sun Bookmark", "Mirror of Margins", "Heart Locket", "Colophon Signet", "Rose of Halgakos"};
    /** Each chapter's own word for what comes from it, and those of the lands' last pages. */
    static final String[] THEMES = {"Paper", "Archive", "Heartflutter", "Index", "Last-Page", "Half-Title", "Gazette", "Interlinear", "Slime", "Appendix",
            "Shoreline", "Dragonscript"};

    /** A thing to keep, as one number: what it is, the chapter it is of, which of its kind, and its rarity. */
    static int code(int type, int theme, int base, int rarity) { return ((type * 16 + theme) * 64 + base) * 8 + rarity; }

    static int type(int code) { return code / 8 / 64 / 16; }

    static int theme(int code) { return code / 8 / 64 % 16; }

    static int base(int code) { return code / 8 % 64; }

    static int rarity(int code) { return code % 8; }

    /** What it is called: its rarity is said by its colour, not its name; the rose is named for her. */
    static String name(int code) {
        int type = type(code), base = base(code), theme = Math.min(THEMES.length - 1, theme(code));
        String what = type == WEAPON ? WEAPONS[base % WEAPONS.length] : type == ARMOUR ? ARMOURS[base % ARMOURS.length] : ACCESSORIES[base % ACCESSORIES.length];
        return (rarity(code) == ROSE ? "Halgakos Rose " : THEMES[theme] + " ") + what;
    }

    /** What a creature of a chapter drops, from rolls of 0 to 9999: of which kind, and of its chapter. */
    static int dropped(int theme, int rarity, int typeRoll, int baseRoll) {
        int type = typeRoll < 5000 ? WEAPON : typeRoll < 7500 ? ARMOUR : ACCESSORY;
        int count = type == WEAPON ? WEAPONS.length : type == ARMOUR ? ARMOURS.length : ACCESSORIES.length;
        // (The better armours and accessories come from the later chapters: of thirty, a chapter's run of them goes up with it.)
        int base = type == WEAPON ? baseRoll % count : Math.min(count - 1, theme * 2 + baseRoll % 8);
        return code(type, theme, base, rarity);
    }

    // Her level

    /** How much experience takes her from one level to the next: more each time, without end. */
    static long toNext(int level) { return Math.round(24 * Math.pow(level, 1.55)); }

    /** How much experience in all a level takes, from the first. */
    static long need(int level) {
        long sum = 0;
        for (int below = 1; below < level; below++) sum += toNext(below);
        return sum;
    }

    /** Her level, from her experience in all. */
    static int levelAt(long xp) {
        int level = 1;
        long sum = 0;
        while (sum + toNext(level) <= xp && level < 100000) {
            sum += toNext(level);
            level++;
        }
        return level;
    }

    /**
     * What a creature gives her as it falls: by how tough it is and the level of its land; ten times that for one who waits; and less
     * for one whose land is far below her, down to a tenth of it.
     */
    static int xpFor(boolean boss, int tough, int number, int herLevel) {
        int base = (3 + tough * 2) * (2 + RunLevels.levelOf(number));
        if (boss) base *= 10;
        int far = herLevel - recommended(number) - 8;
        if (far > 0) base = Math.max(base / 10, Math.round(base * Math.max(.1f, 1 - far * .06f)));
        return Math.max(1, base);
    }

    /**
     * The level each land is for: the level she would come to it at, having played her way there, beating seven creatures in ten of
     * those in her way and whoever waits. Worked out from the lands themselves by a test, which checks it still is what they make.
     */
    static int recommended(int number) { return RECOMMENDED[Math.max(1, Math.min(RECOMMENDED.length, number)) - 1]; }

    static final int[] RECOMMENDED = {
            1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 7, 8, 9, 10, 11, 11, 12, 13, 14, 14,
            15, 16, 17, 18, 19, 20, 22, 23, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 37,
            37, 38, 39, 41, 41, 42, 43, 44, 45, 46, 47, 48, 49, 51, 52, 53, 54, 55, 57, 58,
            58, 60, 61, 62, 63, 64, 65, 66, 67, 68, 68, 70, 71, 72, 73, 74, 75, 77, 78, 80,
            81, 82, 84, 86, 87, 89, 91, 92, 94, 95, 97, 98, 100, 101, 103, 104, 106, 108, 109, 110};

    /** How much harder her blows land, at a level: a part of a blow more for each level, gathered up. */
    static float power(int level) { return 1 + .03f * (level - 1); }

    /** How many halves of a heart less a hurt costs her: one more every forty levels (a hurt always costs half a heart at least). */
    static int guard(int level) { return (level - 1) / 40; }

    /** How many more hearts she has: one at the twentieth level, and one more every time her level doubles after that. */
    static int hearts(int level) {
        int more = 0;
        for (int at = 20; at <= level; at *= 2) more++;
        return more;
    }

    // Her bag

    /** What she has found and keeps, as it is written down, most recent last. */
    static List<Integer> bag(String note) {
        List<Integer> bag = new ArrayList<>();
        if (note == null || note.isEmpty()) return bag;
        for (String one : note.split(",")) {
            try {
                bag.add(Integer.parseInt(one.trim()));
            } catch (NumberFormatException ignored) {
                // (Something written there that is not a thing to keep is left out.)
            }
        }
        return bag;
    }

    static String note(List<Integer> bag) {
        StringBuilder text = new StringBuilder();
        for (int code : bag) text.append(text.length() == 0 ? "" : ",").append(code);
        return text.toString();
    }

    /** How many things her bag holds; when it is full, the least of what is in it makes room. */
    static final int BAG = 240;

    static List<Integer> add(List<Integer> bag, int code) {
        bag.add(code);
        while (bag.size() > BAG) {
            int least = 0;
            for (int index = 1; index < bag.size(); index++) if (rarity(bag.get(index)) < rarity(bag.get(least))) least = index;
            bag.remove(least);
        }
        return bag;
    }
}
