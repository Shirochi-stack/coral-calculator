package com.coral.calculator;

/**
 * The unique creatures: two of each of the twelve chapters' own, each with a way of fighting of its own and a clear wind-up before it
 * strikes. They are tougher than the signs and bite harder, more so the further on their land, so that better gear matters; a colour of
 * toughness may come on top. Their pictures are rows of six (walking twice, winding up, striking, hurt, beaten) in run_uniques; those of the
 * dungeons in run_delvers; and the twelve of the later chapters, two more for each of the last six, in the run's asset pack.
 */
final class RunUniques {
    private RunUniques() {}

    /** The first of their kinds, after Lucy; how many there are. */
    static final int FIRST = RunLevels.LUCY + 1, COUNT = 59;
    /** And the four of the lagoons, who swim: after the hidden bosses. */
    static final int WATER_FIRST = FIRST + 30, WATER_COUNT = 4;
    /** Of them, the six hidden bosses: after the twenty-four, one for each two chapters, each in the hideouts of its lands. */
    static final int HIDDEN_FIRST = FIRST + 24, HIDDEN_COUNT = 6;
    /** And those of the dungeons: seven that live in them, each a way of fighting of its own, then the six keepers at the bottom of them. */
    static final int DELVER_FIRST = FIRST + 34, DELVER_COUNT = 7, KEEPER_FIRST = FIRST + 41, KEEPER_COUNT = 6;
    /** How many of them are drawn in run_uniques; the rest, of the dungeons, are in run_delvers. */
    static final int SHEET_ROWS = 34;
    /**
     * The twelve of the later chapters: two more for each of chapters six to eleven, each a way of fighting of its own and tougher than those
     * before them; their pictures in the run's asset pack.
     */
    static final int LATER_FIRST = FIRST + 47, LATER_COUNT = 12, LATER_CHAPTER = 6;
    static final String LATER_SHEET = "runart/uniques_later.webp";

    /** The ways of fighting. */
    static final int CHARGER = 0, SHIELDER = 1, MIMIC = 2, GHOST = 3, EXPLODER = 4, SNIPER = 5, BURROWER = 6, SUMMONER = 7, HEALER = 8, MINIBOSS = 9,
            FORMATION = 10, SPLITTER = 11, HIDDEN = 12, SWIMMER = 13, BLINKER = 14, DROPPER = 15, LEAPER = 16, ORBITER = 17, BOUNCER = 18, STAMPER = 19,
            MIRROR = 20, KEEPER = 21, ROLLER = 22, HERALD = 23, LASHER = 24, LINER = 25, PULLER = 26, SMEARER = 27, REFLECTOR = 28, ECHOER = 29,
            HOURGLASS = 30, HERMIT = 31, BREATHER = 32, SWEEPER = 33;
    static final String[] WAY_NAMES = {"charger", "shielder", "mimic", "wall-ghost", "exploder", "sniper", "burrower", "summoner", "healer", "mini-boss",
            "formation flyer", "splitter", "hidden boss", "swimmer", "blinker", "dropper", "leaper", "orbiter", "bouncer", "stamper", "mirror", "keeper",
            "roller", "herald", "lasher", "liner", "puller", "smearer", "reflector", "echoer", "hourglass", "hermit", "breather", "sweeper"};

    static final String[] NAMES = {"Dog-Ear", "Comma Snail", "Dust Jacket", "Shush", "Love Letter", "Cupid Quill", "Tab Goat", "Bracket Mole",
            "Epilogue Wraith", "Erratum", "Gilt Knight", "Seal Wax Golem", "Headline Hornet", "Press Roller", "Gap Shade", "Mirror Typo", "Margin Slug",
            "Glue Bomb", "Index Spider", "Errata Hydra", "Dog-Eared Crab", "Message in a Bottle", "Ember Scribe", "Draftling",
            "The Erasure Moth", "Madame Quillon", "The Last Line", "The Typeset Golem", "Marginalia", "The Unwritten",
            "Squid Scribe", "Pufferquill", "Bookworm Eel", "Lantern Jelly",
            "Inkblink Moth", "Shelf Spider", "Folio Frog", "Quill Wisp", "Bouncing Stamp", "Anvil Tome", "Ink Double",
            "The Card Catalogue", "The Bookbinder Beetle", "The Inkwell Kraken", "The Gargoyle Lectern", "The Kite Dragon", "The Hollow Archivist",
            "Newsreel Pillbug", "Headline Herald", "Ruled-Line Serpent", "Strikethrough", "Gluepot Toad", "Smudge Slime", "Cross-Reference Crab", "Ibid Echo",
            "Sand-Glass Sprite", "Hermit Scroll Crab", "Hoard Wyrmling", "Lantern Sentinel"};
    /** Each one's chapter (two to a chapter, in order) and way of fighting. */
    static final int[] WAY = {CHARGER, SHIELDER, MIMIC, GHOST, EXPLODER, SNIPER, CHARGER, BURROWER, SUMMONER, HEALER, MINIBOSS, SHIELDER, FORMATION, CHARGER,
            GHOST, MIMIC, SPLITTER, EXPLODER, SNIPER, MINIBOSS, SHIELDER, SUMMONER, HEALER, CHARGER, HIDDEN, HIDDEN, HIDDEN, HIDDEN, HIDDEN, HIDDEN,
            SWIMMER, SWIMMER, SWIMMER, SWIMMER, BLINKER, DROPPER, LEAPER, ORBITER, BOUNCER, STAMPER, MIRROR, KEEPER, KEEPER, KEEPER, KEEPER, KEEPER, KEEPER,
            ROLLER, HERALD, LASHER, LINER, PULLER, SMEARER, REFLECTOR, ECHOER, HOURGLASS, HERMIT, BREATHER, SWEEPER};
    /** How many blows of health each has at the start of the game (it grows with the land), by way of fighting. */
    private static final float[] HEALTH = {4, 6, 4, 3, 2, 3, 4, 5, 4, 24, 1.5f, 6, 70, 4, 3, 3.5f, 4, 3, 3, 7, 4, 90, 7, 6, 7, 6, 8, 9, 8, 6, 7, 9, 8, 9};
    /** How wide and tall each is, in squares. */
    static final float[] WIDE = {.95f, .9f, 1.1f, .9f, .9f, .8f, 1.1f, .9f, .95f, .85f, 1.2f, 1.25f, .8f, 1.15f, .8f, .9f, 1.2f, .85f, .95f, 1.5f, 1.1f, .85f, .9f, 1.2f,
            2, 2, 2.4f, 2.2f, 1.8f, 1.6f, .9f, .9f, 1.5f, .8f, .9f, .9f, 1, .8f, .85f, 1.3f, .7f, 2.4f, 2.4f, 2.6f, 2, 2.8f, 1.8f,
            1.1f, .9f, 1.4f, .9f, 1.15f, 1.2f, 1.2f, .85f, .85f, 1.05f, 1.1f, 1.1f};
    static final float[] TALL = {.8f, .85f, .7f, 1.15f, .75f, .85f, .95f, .95f, 1.25f, .75f, 1.5f, 1.35f, .7f, .8f, 1.2f, .85f, .75f, .9f, .75f, 1.45f, .8f, .85f, 1.05f, 1.05f,
            1.8f, 1.7f, 1.6f, 2.4f, 1.9f, 2.4f, .9f, .85f, .6f, 1.1f, .8f, .8f, .85f, .9f, .85f, 1.2f, 1.4f, 2.2f, 1.8f, 2.2f, 2.4f, 2, 2.6f,
            .8f, 1.05f, .7f, 1.2f, .9f, .8f, .85f, .95f, 1.1f, 1, 1.05f, 1.45f};
    /** How fast each walks, squares a second. */
    static final float[] SPEED = {1.5f, .55f, 1.2f, .9f, 1.3f, .8f, 1.4f, 1.6f, .6f, .9f, 1.1f, .5f, 2.2f, 1.6f, 1, 1.4f, .8f, 1.2f, .9f, .9f, .7f, .7f, .9f, 1.7f,
            1.3f, 1.2f, 1.4f, .8f, 1.5f, 1.3f, 1.4f, 1.2f, 2.4f, .7f, 1.4f, 1.6f, 1.2f, 1, 2.2f, .6f, 3, 1.2f, 1.4f, 1.1f, 1.2f, 1.3f, 1.2f,
            1.3f, .9f, 1.2f, 1, .7f, .8f, .9f, 1.4f, .8f, .9f, 1.1f, .5f};

    static boolean is(int kind) { return kind >= FIRST && kind < FIRST + COUNT; }

    static int index(int kind) { return kind - FIRST; }

    static int way(int kind) { return WAY[index(kind)]; }

    static int chapter(int kind) {
        int index = index(kind);
        return index < 24 ? index / 2 : index < 30 ? (index - 24) * 2 : index < 34 ? 10 : index < 41 ? 11 : index < 47 ? (index - 41) * 2 + 1
                : LATER_CHAPTER + (index - 47) / 2;
    }

    /** Whether it is one of the twelve of the later chapters; and the first of a later chapter's two (or -1 for a chapter before them). */
    static boolean later(int kind) { return kind >= LATER_FIRST && kind < LATER_FIRST + LATER_COUNT; }

    static int laterOf(int chapter) { return chapter < LATER_CHAPTER ? -1 : LATER_FIRST + (Math.min(11, chapter) - LATER_CHAPTER) * 2; }

    /** Whether it lives in the dungeons; whether it is one of their keepers; and the keeper of a dungeon (one for every four of them). */
    static boolean delver(int kind) { return kind >= DELVER_FIRST && kind < DELVER_FIRST + DELVER_COUNT; }

    static boolean keeper(int kind) { return kind >= KEEPER_FIRST && kind < KEEPER_FIRST + KEEPER_COUNT; }

    static int keeperOf(int dungeon) { return KEEPER_FIRST + Math.max(0, Math.min(KEEPER_COUNT - 1, dungeon / 4)); }

    /** Whether it is one of the great ones, who drop three things of gold or better: a hidden boss or a keeper. */
    static boolean great(int kind) { return hidden(kind) || keeper(kind); }

    /** Whether it floats in the air (and so is not held to the ground): the moth and the wisp, as well as the hornets. */
    static boolean floats(int kind) { return is(kind) && (way(kind) == BLINKER || way(kind) == ORBITER || way(kind) == ECHOER); }

    /** Whether it is one of those who swim in the lagoons. */
    static boolean swims(int kind) { return is(kind) && way(kind) == SWIMMER; }

    /** Whether it is one of the hidden bosses; and the hidden boss of a chapter. */
    static boolean hidden(int kind) { return is(kind) && way(kind) == HIDDEN; }

    static int hiddenOf(int chapter) { return HIDDEN_FIRST + Math.max(0, Math.min(11, chapter)) / 2; }

    /** What a hidden boss throws: the moth eraser crumbs, Quillon ink, the line full stops, the golem type, Marginalia doodles, the Unwritten pages. */
    static int hiddenShot(int kind) {
        switch (kind - HIDDEN_FIRST) {
            case 0: return PlusRun.DOT;
            case 1: return PlusRun.BLOT_SHOT;
            case 2: return PlusRun.STOP;
            case 3: return PlusRun.KNIFE;
            case 4: return PlusRun.FEATHER;
            default: return PlusRun.SCROLL;
        }
    }

    /** Its own Halgakos Rose piece, the first time it is beaten: an accessory, a pen, an armour, a hammer, an accessory, a sword. */
    static int signature(int kind) {
        int boss = kind - HIDDEN_FIRST;
        int[] types = {RunLoot.ACCESSORY, RunLoot.WEAPON, RunLoot.ARMOUR, RunLoot.WEAPON, RunLoot.ACCESSORY, RunLoot.WEAPON};
        int[] bases = {7, 6, 24, 8, 21, 0};
        return RunLoot.code(types[boss], Math.min(11, boss * 2 + 1), bases[boss], RunLoot.ROSE);
    }

    static String name(int kind) { return NAMES[index(kind)]; }

    /** The two of a chapter. */
    static int first(int chapter) { return FIRST + Math.max(0, Math.min(11, chapter)) * 2; }

    /** How much health one has in a land (in tenths of a blow): its way's, made more as her blows grow with the level the land is for. */
    static int health(int kind, int number) {
        return Math.round(PlusRun.BLOW * HEALTH[way(kind)] * RunLoot.power(RunLoot.recommended(number)) * (1 + number / 100f));
    }

    /** The halves of a heart more than a sign's its touch costs: none in the first two chapters, then one (two for a mini-boss), and one more every fifty lands. */
    static int bite(int kind, int number) {
        return way(kind) == HIDDEN || way(kind) == KEEPER ? 2 + number / 40 : number <= 12 ? 0 : (way(kind) == MINIBOSS || way(kind) == STAMPER ? 2 : 1) + number / 50;
    }

    /** Whether it flies (and so is in the air, not on the ground). */
    static boolean flies(int kind) { return is(kind) && way(kind) == FORMATION; }

    /** Whether it goes through walls. */
    static boolean ghostly(int kind) { return is(kind) && way(kind) == GHOST; }
}
