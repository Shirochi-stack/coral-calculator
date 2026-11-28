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
    static final String[] WEAPONS = {"Sword", "Katana", "Greatsword", "Book", "Summoning Staff", "Knife", "Pen", "Mace", "Hammer", "Wand", "Ink Gun", "Scythe", "Knuckles"};
    /** The knuckles' place among the kinds of weapon. */
    static final int KNUCKLES = 12;
    /** The thirty armours, and the thirty accessories. */
    static final String[] ARMOURS = {"Paper Vest", "Linen Tunic", "Bookbinder's Apron", "Leather Jerkin", "Ribbon Mail", "Footnote Coat", "Archivist's Robe", "Inkproof Cloak",
            "Margin Plate", "Errata Brigandine", "Folio Cuirass", "Gilded Spine", "Index Scale", "Quill-Feather Mantle", "Vellum Gown", "Ledger Hauberk",
            "Chapter Breastplate", "Serif Lamellar", "Typeset Armour", "Gazette Duster", "Lantern Coat", "Slime-Glaze Shell", "Appendix Harness",
            "Tidewrack Raiment", "Palace Regalia", "Bookmark Banner Coat", "Dragonhide Jacket", "Colophon Aegis", "Mother Tongue Vestment", "Rosebound Armour",
            "Brawler's Gi"};
    /** The armour that leans to every class; the knuckle fighters' armour; and how many armours drop as a chapter's own (the class gear drops apart). */
    static final int ROSEBOUND = 29, BRAWLER_GI = 30, ARMOUR_DROPS = 30;
    static final String[] ACCESSORIES = {"Heartstring Locket", "Margin Ward Brooch", "Inkwell Heart", "Thimble of Turning", "Second Wind Feather", "Lucky Plus Coin",
            "Lodestone Compass", "Hare's-Foot Anklet", "Thornbriar Ring", "Hourglass Pendant", "Bladesong Charm", "Whetstone Earring", "Duelist's Ribbon",
            "Archivist's Seal", "Footnote Venom Tag", "Summoner's Bell", "Quickdraw Quill Brooch", "Venom Nib Pendant", "Ricochet Charm", "Anvil Heart", "Quake Bangle",
            "Ironbound Amulet", "Gunslinger's Drum Pendant", "Ember Vial Pendant", "Frostglass Focus", "Stormcall Gem", "Reaper's Hook Earring", "Crescent Pin",
            "Colophon Signet", "Rose of Halgakos", "Champion's Wraps"};
    /** The knuckle fighters' accessory. */
    static final int CHAMPION_WRAPS = 30;

    // Classes: the weapons a thing she wears is for

    /** The classes of weapon, each a few of the things she fights with; and, for what is for none in particular, any. */
    static final int ANY = -1, BLADES = 0, TOMES = 1, THROWING = 2, HEAVY = 3, GUNS = 4, WANDS = 5, REAPERS = 6, BRAWLERS = 7, CLASSES = 8;
    static final String[] CLASS_NAMES = {"Blades", "Tomes", "Throwing", "Heavy", "Ink guns", "Wands", "Reapers", "Brawlers"};
    static final String[] CLASS_HOLDS = {"swords, katanas, greatswords and the brush", "books, the tablet-book and summoning staffs", "pens, knives and API keys",
            "maces, hammers and the ink-well cannon", "ink guns", "wands", "scythes, the ribbon whip and the fountain-pen lance", "knuckles"};

    /** The class of a thing she fights with, by its number in {@link RunArms}; or none, empty-handed. */
    static int classOf(int kind) {
        switch (kind) {
            case RunArms.SWORD: case RunArms.KATANA: case RunArms.BRUSH: return BLADES;
            case RunArms.BOOK: case RunArms.TABLET: return TOMES;
            case RunArms.PEN: case RunArms.KNIFE: case RunArms.KEYS: return THROWING;
            case RunArms.CANNON: return HEAVY;
            case RunArms.GUN: return GUNS;
            case RunArms.WHIP: case RunArms.LANCE: return REAPERS;
            case RunArms.KNUCKLES: return BRAWLERS;
            default: return ANY;
        }
    }

    // What she wears does

    /**
     * What a thing she wears can do: life coming back (halves of a heart a minute); a shield bar (pieces, each taking a hurt for her) and
     * how much faster it fills again; a chance on a blow of a piece of it back; energy for her specials coming back faster, and costing
     * less; a chance a hurt is shrugged off, and a chance she slips it; jumps more in the air; more pluses; pluses drawn from further (in
     * tenths of a square); blows back on what touches her; specials ready sooner; a chance of a great blow, and how much more it counts
     * for; uses sooner; chances on a blow of ink-poison, burning and freezing; a chance a blow gives half a heart back, as ink; further
     * reach; thrown things going through more; wider bursts; harder blows; and energy back on a blow.
     */
    static final int REGEN = 0, WARD = 1, WARD_FAST = 2, WARD_HIT = 3, ENERGY = 4, THRIFT = 5, SHRUG = 6, DODGE = 7, AIR = 8, PLUSES = 9, MAGNET = 10,
            THORNS = 11, SPECIAL = 12, CRIT = 13, CRITX = 14, HASTE = 15, POISON = 16, BURN = 17, FREEZE = 18, LEECH = 19, REACH = 20, PIERCE = 21,
            AREA = 22, POWER = 23, SPARK = 24, EFFECTS = 25;
    /** The most each can come to, however much she wears of it. */
    static final int[] MOST = {30, 4, 200, 60, 300, 70, 50, 40, 2, 300, 60, 6, 60, 60, 300, 50, 100, 100, 100, 25, 60, 4, 100, 200, 20};
    /** Those that are counted, which go up by one at gold and again at the rose; the rest grow with rarity. */
    private static boolean counted(int effect) { return effect == WARD || effect == AIR || effect == PIERCE || effect == THORNS; }

    /** How many times more each rarity's figures are. */
    static final float[] MORE = {1, 1.35f, 1.75f, 2.2f, 2.75f, 3.5f};

    /**
     * The thirty accessories: the class each is for, then what it does at any rarity, what more it does from gold, and what more from
     * red, each an effect and its figure at white. The rose's own does a fourth thing.
     */
    static final int[][] ACCESSORY_DOES = {
            {ANY, REGEN, 2, SHRUG, 4, WARD, 1},
            {ANY, WARD, 1, WARD_FAST, 30, SHRUG, 6},
            {ANY, ENERGY, 25, SPARK, 2, THRIFT, 10},
            {ANY, SHRUG, 8, REGEN, 1, DODGE, 5},
            {ANY, AIR, 1, DODGE, 5, ENERGY, 20},
            {ANY, PLUSES, 20, MAGNET, 10, LEECH, 2},
            {ANY, MAGNET, 20, PLUSES, 10, SPARK, 2},
            {ANY, DODGE, 7, HASTE, 5, AIR, 1},
            {ANY, THORNS, 1, SHRUG, 5, POISON, 10},
            {ANY, SPECIAL, 15, THRIFT, 15, ENERGY, 20},
            {BLADES, WARD_HIT, 12, WARD, 1, POWER, 10},
            {BLADES, CRIT, 12, CRITX, 50, HASTE, 8},
            {BLADES, HASTE, 15, REACH, 10, CRIT, 8},
            {TOMES, THRIFT, 30, SPARK, 3, AREA, 20},
            {TOMES, POISON, 25, POWER, 10, THRIFT, 15},
            {TOMES, POWER, 20, HASTE, 10, POISON, 15},
            {THROWING, HASTE, 20, PIERCE, 1, CRIT, 8},
            {THROWING, POISON, 30, HASTE, 8, LEECH, 3},
            {THROWING, PIERCE, 1, CRIT, 10, POWER, 10},
            {HEAVY, FREEZE, 25, POWER, 10, SHRUG, 8},
            {HEAVY, AREA, 25, POWER, 15, FREEZE, 10},
            {HEAVY, SHRUG, 15, WARD, 1, THORNS, 1},
            {GUNS, HASTE, 25, PIERCE, 1, BURN, 10},
            {GUNS, BURN, 30, HASTE, 8, CRIT, 8},
            {WANDS, FREEZE, 30, POWER, 10, THRIFT, 15},
            {WANDS, CRIT, 15, ENERGY, 25, CRITX, 50},
            {REAPERS, LEECH, 5, POWER, 10, REACH, 15},
            {REAPERS, REACH, 20, HASTE, 10, LEECH, 3},
            {ANY, CRIT, 10, CRITX, 60, POWER, 10},
            {ANY, POWER, 15, REGEN, 2, WARD, 1, ENERGY, 25},
            // (Champion's Wraps: for knuckles only, and much of it: faster, harder, great blows, and a piece of shield back now and then.)
            {BRAWLERS, HASTE, 25, POWER, 20, CRIT, 15, WARD_HIT, 10}};
    /** The accessory only gold and better is, and the one only the rose is. */
    static final int SIGNET = 28, ROSE_OF_HALGAKOS = 29;

    /** The class an armour leans to: its blows with that class land harder. The last of them leans to every class. */
    static int leansTo(int base) { return base == ROSEBOUND ? ANY : base == BRAWLER_GI ? BRAWLERS : base % 7; }

    /**
     * What a thing she wears does, as effect and figure pairs, with its class first: an accessory as the table says, by its rarity; an
     * armour, a chance a hurt is shrugged off (better the later the armour), harder blows with the class it leans to, and from gold life
     * coming back, from red a piece of shield. A weapon, nothing here.
     */
    static int[] does(int code) {
        int type = type(code), base = base(code), rarity = rarity(code);
        if (type == ACCESSORY) {
            int[] row = ACCESSORY_DOES[base % ACCESSORY_DOES.length];
            int available = (row.length - 1) / 2, pairs = available == 4 ? 4 : Math.min(available, rarity >= RED ? 3 : rarity >= GOLD ? 2 : 1);
            int[] out = new int[1 + pairs * 2];
            out[0] = row[0];
            for (int pair = 0; pair < pairs; pair++) {
                out[1 + pair * 2] = row[1 + pair * 2];
                out[2 + pair * 2] = figure(row[1 + pair * 2], row[2 + pair * 2], rarity);
            }
            return out;
        }
        if (type == ARMOUR && base == BRAWLER_GI) {
            // The Brawler's Gi: light, for slipping blows, and for punching harder: a chance to dodge, harder blows with knuckles, and from gold life back.
            int[] out = {ANY, DODGE, figure(DODGE, 8, rarity), POWER, figure(POWER, 15, rarity), HASTE, figure(HASTE, 10, rarity), REGEN, figure(REGEN, 1, rarity)};
            return rarity >= GOLD ? out : java.util.Arrays.copyOf(out, 7);
        }
        if (type == ARMOUR) {
            int pairs = rarity >= RED ? 4 : rarity >= GOLD ? 3 : 2;
            int[] out = new int[1 + pairs * 2];
            out[0] = ANY;
            out[1] = SHRUG;
            out[2] = figure(SHRUG, 3 + base / 6, rarity);
            // (Its leaning: for the class's blows only, marked by the class in a place of its own.)
            out[3] = POWER;
            out[4] = figure(POWER, 8, rarity);
            if (pairs > 2) {
                out[5] = REGEN;
                out[6] = figure(REGEN, 1, rarity);
            }
            if (pairs > 3) {
                out[7] = WARD;
                out[8] = 1;
            }
            return out;
        }
        return new int[]{ANY};
    }

    /** An effect's figure at a rarity, from its figure at white. */
    static int figure(int effect, int white, int rarity) {
        if (counted(effect)) return white + (rarity >= GOLD ? 1 : 0) + (rarity == ROSE ? 1 : 0);
        return Math.max(1, Math.round(white * MORE[Math.max(0, Math.min(ROSE, rarity))]));
    }

    /** Whether two things she wears are of one set: an armour and an accessory of the same chapter. */
    static boolean sameSet(int armour, int accessory) { return armour != 0 && accessory != 0 && theme(armour) == theme(accessory); }

    /**
     * What she wears, all added up: an armour and two accessories (0 for none). What is for any class, and what is for each; with an
     * armour and an accessory of one set a piece of shield more, and with both accessories of its set too, harder blows and life coming back.
     */
    static final class Worn {
        final int[] any = new int[EFFECTS];
        final int[][] byClass = new int[CLASSES][EFFECTS];
        final int armour, first, second;
        final int set;

        Worn(int armour, int first, int second) {
            this.armour = armour;
            this.first = first;
            this.second = second;
            for (int code : new int[]{armour, first, second}) {
                if (code == 0) continue;
                int[] does = does(code);
                for (int pair = 0; pair * 2 + 2 < does.length; pair++) {
                    int effect = does[1 + pair * 2], figure = does[2 + pair * 2];
                    // An armour's harder blows are for the class it leans to.
                    int to = type(code) == ARMOUR && (effect == POWER || base(code) == BRAWLER_GI && effect == HASTE) ? leansTo(base(code)) : does[0];
                    if (to == ANY) any[effect] += figure;
                    else byClass[to][effect] += figure;
                }
            }
            set = (sameSet(armour, first) ? 1 : 0) + (sameSet(armour, second) ? 1 : 0);
            if (set >= 1) any[WARD]++;
            if (set >= 2) {
                any[POWER] += 10;
                any[REGEN] += 1;
            }
        }

        /** How much of an effect she has, holding a thing of a class (or none). */
        int of(int effect, int holding) {
            int sum = any[effect] + (holding >= 0 && holding < CLASSES ? byClass[holding][effect] : 0);
            return Math.min(MOST[effect], sum);
        }

        boolean any() { return armour != 0 || first != 0 || second != 0; }
    }

    static final Worn NOTHING = new Worn(0, 0, 0);

    /** How what she wears is written down: armour, then the two accessories. */
    static Worn worn(String note) {
        List<Integer> codes = bag(note);
        int armour = codes.size() > 0 && type(codes.get(0)) == ARMOUR ? codes.get(0) : 0;
        int first = codes.size() > 1 && type(codes.get(1)) == ACCESSORY ? codes.get(1) : 0;
        int second = codes.size() > 2 && type(codes.get(2)) == ACCESSORY ? codes.get(2) : 0;
        return new Worn(armour, first, second);
    }

    static String note(Worn worn) { return worn.armour + "," + worn.first + "," + worn.second; }

    /** An effect and its figure, in words: "Life back: half a heart every 30 s". */
    static String said(int effect, int figure) {
        switch (effect) {
            case REGEN: return "Life back: half a heart every " + Math.max(1, Math.round(60f / figure)) + " s";
            case WARD: return "Shield bar +" + figure + (figure == 1 ? " piece" : " pieces");
            case WARD_FAST: return "Shield fills " + figure + "% faster";
            case WARD_HIT: return figure + "% of blows give a shield piece back";
            case ENERGY: return "Energy back " + figure + "% faster";
            case THRIFT: return "Specials cost " + figure + "% less energy";
            case SHRUG: return figure + "% of hurts shrugged off";
            case DODGE: return figure + "% chance to dodge";
            case AIR: return "+" + figure + (figure == 1 ? " jump" : " jumps") + " in the air";
            case PLUSES: return figure + "% more pluses";
            case MAGNET: return "Pluses come from " + (figure / 10) + (figure % 10 != 0 ? "." + figure % 10 : "") + " squares further";
            case THORNS: return "Thorns: " + figure + (figure == 1 ? " blow" : " blows") + " back on what hurts her";
            case SPECIAL: return "Specials ready " + figure + "% sooner";
            case CRIT: return figure + "% chance of a great blow";
            case CRITX: return "Great blows " + figure + "% harder";
            case HASTE: return figure + "% faster attacks";
            case POISON: return figure + "% of blows ink-poison";
            case BURN: return figure + "% of blows set burning";
            case FREEZE: return figure + "% of blows freeze";
            case LEECH: return figure + "% of blows give half a heart back";
            case REACH: return figure + "% further reach";
            case PIERCE: return "Thrown things go through " + figure + " more";
            case AREA: return figure + "% wider bursts";
            case POWER: return figure + "% harder blows";
            case SPARK: return "+" + figure + " energy a blow";
            default: return "";
        }
    }
    /** Each chapter's own word for what comes from it, and those of the lands' last pages. */
    static final String[] THEMES = {"Paper", "Archive", "Heartflutter", "Index", "Last-Page", "Half-Title", "Gazette", "Interlinear", "Slime", "Appendix",
            "Shoreline", "Dragonscript"};

    /** A thing to keep, as one number: what it is, the chapter it is of, which of its kind, and its rarity. */
    static int code(int type, int theme, int base, int rarity) { return ((type * 16 + theme) * 64 + base) * 8 + rarity; }

    static int type(int code) { return code / 8 / 64 / 16; }

    static int theme(int code) { return code / 8 / 64 % 16; }

    static int base(int code) { return code / 8 % 64; }

    static int rarity(int code) { return code % 8; }

    /**
     * The old plus-book her first book once was: a book of the first chapters' own, found now and then in place of one of their books, white
     * or blue. As a thing found it is a book of the "chapter" after the last.
     */
    static final int ADDITION = 12, BOOK = 3;

    static boolean addition(int code) { return type(code) == WEAPON && base(code) == BOOK && theme(code) == ADDITION; }

    /**
     * The paper variants: the cheap, early version of a weapon, folded from plain paper; a "chapter" of their own after the Book of Addition's.
     * Never better than blue, weaker than the real thing, and plain. For each, the kind of weapon it is (by its place among the twelve), its name and its picture.
     */
    static final int PAPER = 13, SCYTHE = 11;
    /** Of the paper weapons, those that drop (the rest are folded at the smithy); and where each kind's picture is (the scythe's first, the rest after the katana blades). */
    static final int[] PAPER_DROPS = {SCYTHE};
    /** The knuckles' pictures: the twelve chapters', the rose's, the paper ones', the Brawler's Gi and the Champion's Wraps. */
    static final int KNUCKLE_ICONS = 256, KNUCKLE_ROSE_ICON = 268, KNUCKLE_PAPER_ICON = 269, GI_ICON = 270, WRAPS_ICON = 271;
    static final int PAPER_ICONS = 229, MORE_PAPER_ICONS = 243;

    /**
     * The Mother Tongue's Quill: the one weapon above all others, a quill from the page wing of the Mother Tongue herself, crafted at the
     * very last at the smithy. A pen, of the rose, of a "chapter" of its own.
     */
    static final int MOTHER = 14, PEN = 6, QUILL_ICON = 254;
    static final int QUILL = code(WEAPON, MOTHER, PEN, ROSE);

    static boolean ultimate(int code) { return code == QUILL; }

    static boolean paper(int code) { return type(code) == WEAPON && theme(code) == PAPER; }

    static int paperIndex(int code) { return base(code) % WEAPONS.length; }

    /** What it is called: its rarity is said by its colour, not its name; the rose is named for her. */
    static String name(int code) {
        if (addition(code)) return "Book of Addition";
        if (paper(code)) return "Folded Page " + WEAPONS[paperIndex(code)];
        if (ultimate(code)) return "The Mother Tongue's Quill";
        int type = type(code), base = base(code), theme = Math.min(THEMES.length - 1, theme(code));
        String what = type == WEAPON ? WEAPONS[base % WEAPONS.length] : type == ARMOUR ? ARMOURS[base % ARMOURS.length] : ACCESSORIES[base % ACCESSORIES.length];
        // (The rose's own accessory is named for her already.)
        if (type == ACCESSORY && base == ROSE_OF_HALGAKOS) return rarity(code) == ROSE ? what : THEMES[theme] + " " + what;
        return (rarity(code) == ROSE ? "Halgakos Rose " : THEMES[theme] + " ") + what;
    }

    /**
     * Of what drops, how much in ten thousand is a weapon and how much armour (the rest an accessory): from an ordinary creature, from one
     * of the hardest, and from one who waits. Accessories are rare: three in a hundred things from a creature, and never better than blue;
     * seven in a hundred from the hardest; one in five from those who wait, and those always purple or better.
     */
    static final int[][] KINDS = {{5200, 4500}, {5000, 4300}, {4500, 3500}};
    static final int ORDINARY = 0, HARDEST = 1, WAITS = 2;

    /** What a creature of a chapter drops, from rolls of 0 to 9999: of which kind, of its chapter, and its rarity as its kind allows. */
    static int dropped(int from, int theme, int rarity, int typeRoll, int baseRoll) {
        int[] kinds = KINDS[from];
        int type = typeRoll < kinds[0] ? WEAPON : typeRoll < kinds[0] + kinds[1] ? ARMOUR : ACCESSORY;
        // (From one who waits, gold or better, now and then the knuckle fighters' own gear: rare.)
        if (from == WAITS && rarity >= GOLD && type != WEAPON && baseRoll % 17 == 0) return code(type, theme, type == ARMOUR ? BRAWLER_GI : CHAMPION_WRAPS, rarity);
        if (type == ACCESSORY) {
            if (from == ORDINARY) rarity = Math.min(rarity, BLUE);
            if (from == WAITS) rarity = Math.max(rarity, PURPLE);
            // (Any of them but the signet, which is only gold or better, and the rose's own, which is only the rose.)
            int base = baseRoll % SIGNET;
            if (rarity == ROSE && baseRoll % 3 == 0) base = ROSE_OF_HALGAKOS;
            else if (rarity >= GOLD && baseRoll % 5 == 0) base = SIGNET;
            return code(ACCESSORY, theme, base, rarity);
        }
        // (In the first two chapters, a white or blue book is now and then the old plus-book.)
        if (type == WEAPON && baseRoll % WEAPONS.length == BOOK && theme <= 1 && rarity <= BLUE && baseRoll % 3 == 0) return code(WEAPON, ADDITION, BOOK, rarity);
        // (And in the first three chapters, a white or blue weapon that has a paper version is that, half the time.)
        if (type == WEAPON && theme <= 2 && rarity <= BLUE && baseRoll / WEAPONS.length % 2 == 0) {
            for (int paperBase : PAPER_DROPS) if (baseRoll % WEAPONS.length == paperBase) return code(WEAPON, PAPER, paperBase, rarity);
        }
        int count = type == WEAPON ? WEAPONS.length : ARMOUR_DROPS;
        // (The better armours come from the later chapters: of thirty, a chapter's run of them goes up with it.)
        int base = type == WEAPON ? baseRoll % count : Math.min(count - 1, theme * 2 + baseRoll % 8);
        return code(type, theme, base, rarity);
    }

    /** Where a thing found's picture is in the sheet of them: a weapon by its kind and chapter (a rose its own), armour and accessories each their own. */
    static final int ROSE_ICONS = 144, ARMOUR_ICONS = 156, ACCESSORY_ICONS = 186, ADDITION_ICON = 228;

    static int iconOf(int code) {
        if (addition(code)) return ADDITION_ICON;
        if (paper(code)) return paperIndex(code) == SCYTHE ? PAPER_ICONS : paperIndex(code) == KNUCKLES ? KNUCKLE_PAPER_ICON : MORE_PAPER_ICONS + paperIndex(code);
        if (ultimate(code)) return QUILL_ICON;
        int type = type(code), base = base(code);
        if (type == WEAPON && base == KNUCKLES) return rarity(code) == ROSE ? KNUCKLE_ROSE_ICON : KNUCKLE_ICONS + Math.min(11, theme(code));
        if (type == WEAPON) return rarity(code) == ROSE ? ROSE_ICONS + base % 12 : base % 12 * 12 + Math.min(11, theme(code));
        if (type == ARMOUR && base == BRAWLER_GI) return GI_ICON;
        if (type == ACCESSORY && base == CHAMPION_WRAPS) return WRAPS_ICON;
        return type == ARMOUR ? ARMOUR_ICONS + base % ARMOUR_DROPS : ACCESSORY_ICONS + base % 30;
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
            1, 2, 3, 3, 4, 5, 6, 7, 7, 8, 9, 10, 10, 12, 12, 13, 14, 15, 16, 16,
            17, 19, 20, 21, 21, 22, 24, 26, 28, 29, 30, 31, 33, 34, 35, 36, 37, 38, 39, 41,
            42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 54, 55, 56, 58, 59, 60, 62, 63, 64,
            65, 67, 68, 69, 70, 71, 72, 73, 74, 76, 76, 78, 79, 80, 82, 82, 84, 86, 88, 89,
            90, 92, 94, 95, 97, 99, 101, 103, 105, 106, 108, 109, 111, 113, 114, 116, 118, 120, 121, 123};

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
