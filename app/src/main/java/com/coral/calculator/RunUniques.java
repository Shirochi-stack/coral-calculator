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
    static final int FIRST = RunLevels.LUCY + 1, COUNT = 93;
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
    /**
     * And twelve made of fire, frost and ink, or that stun, two more for each of the first six chapters (which had fewest): each burns,
     * chills, poisons or stuns her, shrugs off what it is made of, goes in its own way when beaten, and is tougher than those of its chapter.
     */
    static final int STATUS_FIRST = FIRST + 59, STATUS_COUNT = 12;
    static final String STATUS_SHEET = "runart/uniques_status.webp";
    static final float STATUS_TOUGHER = 1.6f;
    /**
     * The Mirror Depths' own: three of her shadow (one that mirrors her, one hidden until she is near, one that shoots), whose touch blinds
     * her for a moment; and at the bottom the Imperfect Copy, its keeper.
     */
    static final int SHADOW_FIRST = FIRST + 71, SHADOW_COUNT = 3, COPY = FIRST + 74;
    static final String COPY_SHEET = "mirror/imperfect_copy.webp";
    /**
     * Seven of her from timelines that ended badly, the Echoes, met at the bottom of the mirror's first two bands (one on its fifth floor,
     * one on its tenth): each in an outfit of her vanity (by its place in {@link RunVanity#STYLES}) and with a weapon of hers, shaded as
     * her timeline left her, and tougher than a mini-boss.
     */
    static final int ECHO_FIRST = FIRST + 75, ECHO_COUNT = 7;
    static final int[] ECHO_OUTFIT = {6, 1, 2, 5, 17, 4, 19};
    static final int[] ECHO_WEAPON = {RunArms.WAND, RunArms.SCYTHE, RunArms.GREATSWORD, RunArms.PEN, RunArms.STAFF, RunArms.KNIFE, RunArms.KATANA};
    static final float ECHO_TOUGHER = 1.5f;
    /**
     * And the mirror's shadows of those she knows, met as it goes down (one on each of its third, sixth, ninth and twelfth floors, by turns):
     * Chikuwa, who leaps; Rune, who throws her ink; Illya, who floats with her hearts about her; Tsubakieh, who charges with her bat; and the
     * Mawang, who darts. Drawn from their own pictures in shadow; their touch blinds her as her own shadows' does.
     */
    static final int CAST_FIRST = FIRST + 82, CAST_COUNT = 5;
    static final float CAST_TOUGHER = 5;
    /**
     * Six more hidden bosses, so that each chapter with hideouts has one of its own (the first six now in the odd chapters, these in
     * the even ones from the third, and the last two chapters' by turns in the last): the Overdue Notice, the Papercut Queen, the Gilded Bookworm, the Wax Seal Tyrant, the Footnote King, the Last
     * Librarian. Their pictures in the run's asset pack.
     */
    static final int HIDDEN2_FIRST = FIRST + 87, HIDDEN2_COUNT = 6;
    static final String HIDDEN2_SHEET = "runart/uniques_hidden2.webp";
    /** What each of them is made of (see {@link PlusRun#resists}), or nought for those that stun. */
    static final int[] STATUS_MADE = {PlusRun.RESISTS_FIRE, PlusRun.RESISTS_COLD, PlusRun.RESISTS_INK, PlusRun.RESISTS_INK, 0, PlusRun.RESISTS_COLD,
            PlusRun.RESISTS_FIRE, PlusRun.RESISTS_INK, PlusRun.RESISTS_COLD, PlusRun.RESISTS_FIRE, PlusRun.RESISTS_INK, 0};

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
            "Sand-Glass Sprite", "Hermit Scroll Crab", "Hoard Wyrmling", "Lantern Sentinel",
            "Cinder Sprite", "Frost Folio", "Venom Quill", "Ink Bomb Beetle", "Static Bookend", "Glacier Tome", "Flame Fan", "Plague Page",
            "Snowglobe Golem", "Ashen Scribe", "Smog Moth", "Thunder Clapper",
            "Shadow Self", "Hidden Reflection", "Shadow Archer", "The Imperfect Copy",
            "The Drowned Halgakos", "The Ashen Halgakos", "The Knight Who Stayed", "The Last Idol", "The Frozen Scholar", "The Hollow Maid", "The Blank Page",
            "Shadow Chikuwa", "Shadow Rune", "Shadow Illya", "Shadow Tsubakieh", "Shadow Mawang",
            "The Overdue Notice", "The Papercut Queen", "The Gilded Bookworm", "The Wax Seal Tyrant", "The Footnote King", "The Last Librarian"};
    /** Each one's chapter (two to a chapter, in order) and way of fighting. */
    static final int[] WAY = {CHARGER, SHIELDER, MIMIC, GHOST, EXPLODER, SNIPER, CHARGER, BURROWER, SUMMONER, HEALER, MINIBOSS, SHIELDER, FORMATION, CHARGER,
            GHOST, MIMIC, SPLITTER, EXPLODER, SNIPER, MINIBOSS, SHIELDER, SUMMONER, HEALER, CHARGER, HIDDEN, HIDDEN, HIDDEN, HIDDEN, HIDDEN, HIDDEN,
            SWIMMER, SWIMMER, SWIMMER, SWIMMER, BLINKER, DROPPER, LEAPER, ORBITER, BOUNCER, STAMPER, MIRROR, KEEPER, KEEPER, KEEPER, KEEPER, KEEPER, KEEPER,
            ROLLER, HERALD, LASHER, LINER, PULLER, SMEARER, REFLECTOR, ECHOER, HOURGLASS, HERMIT, BREATHER, SWEEPER,
            SNIPER, STAMPER, SNIPER, EXPLODER, CHARGER, SHIELDER, LEAPER, SPLITTER, STAMPER, SNIPER, BLINKER, BOUNCER,
            MIRROR, GHOST, SNIPER, KEEPER,
            MINIBOSS, MINIBOSS, MINIBOSS, MINIBOSS, MINIBOSS, MINIBOSS, MINIBOSS,
            LEAPER, SNIPER, ORBITER, CHARGER, BLINKER,
            HIDDEN, HIDDEN, HIDDEN, HIDDEN, HIDDEN, HIDDEN};
    /** How many blows of health each has at the start of the game (it grows with the land), by way of fighting. */
    private static final float[] HEALTH = {4, 6, 4, 3, 2, 3, 4, 5, 4, 24, 1.5f, 6, 70, 4, 3, 3.5f, 4, 3, 3, 7, 4, 90, 7, 6, 7, 6, 8, 9, 8, 6, 7, 9, 8, 9};
    /** How wide and tall each is, in squares. */
    static final float[] WIDE = {.95f, .9f, 1.1f, .9f, .9f, .8f, 1.1f, .9f, .95f, .85f, 1.2f, 1.25f, .8f, 1.15f, .8f, .9f, 1.2f, .85f, .95f, 1.5f, 1.1f, .85f, .9f, 1.2f,
            2, 2, 2.4f, 2.2f, 1.8f, 1.6f, .9f, .9f, 1.5f, .8f, .9f, .9f, 1, .8f, .85f, 1.3f, .7f, 2.4f, 2.4f, 2.6f, 2, 2.8f, 1.8f,
            1.1f, .9f, 1.4f, .9f, 1.15f, 1.2f, 1.2f, .85f, .85f, 1.05f, 1.1f, 1.1f,
            .8f, 1.1f, .9f, .95f, 1.1f, 1.25f, 1, .9f, 1.3f, .9f, 1, 1,
            .7f, .7f, .7f, 1.3f,
            .8f, .8f, .8f, .8f, .8f, .8f, .8f,
            .8f, .8f, .8f, .9f, 1.1f,
            2.2f, 1.8f, 2.4f, 2, 2, 1.7f};
    static final float[] TALL = {.8f, .85f, .7f, 1.15f, .75f, .85f, .95f, .95f, 1.25f, .75f, 1.5f, 1.35f, .7f, .8f, 1.2f, .85f, .75f, .9f, .75f, 1.45f, .8f, .85f, 1.05f, 1.05f,
            1.8f, 1.7f, 1.6f, 2.4f, 1.9f, 2.4f, .9f, .85f, .6f, 1.1f, .8f, .8f, .85f, .9f, .85f, 1.2f, 1.4f, 2.2f, 1.8f, 2.2f, 2.4f, 2, 2.6f,
            .8f, 1.05f, .7f, 1.2f, .9f, .8f, .85f, .95f, 1.1f, 1, 1.05f, 1.45f,
            .85f, .95f, .8f, .8f, .85f, 1.1f, .95f, .95f, 1.4f, 1.15f, .85f, .9f,
            1.4f, 1.4f, 1.4f, 2.4f,
            1.6f, 1.6f, 1.6f, 1.6f, 1.6f, 1.6f, 1.6f,
            1.3f, 1.4f, 1.4f, 1.5f, 1.6f,
            2, 2.4f, 1.8f, 2.2f, 2.2f, 2.5f};
    /** How fast each walks, squares a second. */
    static final float[] SPEED = {1.5f, .55f, 1.2f, .9f, 1.3f, .8f, 1.4f, 1.6f, .6f, .9f, 1.1f, .5f, 2.2f, 1.6f, 1, 1.4f, .8f, 1.2f, .9f, .9f, .7f, .7f, .9f, 1.7f,
            1.3f, 1.2f, 1.4f, .8f, 1.5f, 1.3f, 1.4f, 1.2f, 2.4f, .7f, 1.4f, 1.6f, 1.2f, 1, 2.2f, .6f, 3, 1.2f, 1.4f, 1.1f, 1.2f, 1.3f, 1.2f,
            1.3f, .9f, 1.2f, 1, .7f, .8f, .9f, 1.4f, .8f, .9f, 1.1f, .5f,
            .9f, .6f, .8f, 1.3f, 1.6f, .5f, 1.2f, .8f, .55f, .8f, 1.4f, 1.2f,
            1.4f, 1.1f, .9f, 1.3f,
            1.2f, 1.1f, 1, 1.3f, .9f, 1.4f, 1.3f,
            1.3f, .9f, 1.1f, 1.5f, 1.4f,
            1.2f, 1.4f, 1.1f, 1, 1.3f, 1.2f};

    static boolean is(int kind) { return kind >= FIRST && kind < FIRST + COUNT; }

    static int index(int kind) { return kind - FIRST; }

    static int way(int kind) { return WAY[index(kind)]; }

    static int chapter(int kind) {
        int index = index(kind);
        return index < 24 ? index / 2 : index < 30 ? (index - 24) * 2 + 1 : index < 34 ? 10 : index < 41 ? 11 : index < 47 ? (index - 41) * 2 + 1
                : index < 59 ? LATER_CHAPTER + (index - 47) / 2 : index < 71 ? (index - 59) / 2 : index < 87 ? 11 : Math.min(11, (index - 87) * 2 + 2);
    }

    /** Whether it is one of the twelve made of fire, frost and ink; and the first of a chapter's two of them (or -1 past the sixth chapter). */
    static boolean status(int kind) { return kind >= STATUS_FIRST && kind < STATUS_FIRST + STATUS_COUNT; }

    static int statusOf(int chapter) { return chapter < 0 || chapter > 5 ? -1 : STATUS_FIRST + chapter * 2; }

    /** Whether it is one of the twelve of the later chapters; and the first of a later chapter's two (or -1 for a chapter before them). */
    static boolean later(int kind) { return kind >= LATER_FIRST && kind < LATER_FIRST + LATER_COUNT; }

    static int laterOf(int chapter) { return chapter < LATER_CHAPTER ? -1 : LATER_FIRST + (Math.min(11, chapter) - LATER_CHAPTER) * 2; }

    /** Whether it lives in the dungeons; whether it is one of their keepers; and the keeper of a dungeon (one for every four of them). */
    static boolean delver(int kind) { return kind >= DELVER_FIRST && kind < DELVER_FIRST + DELVER_COUNT; }

    static boolean keeper(int kind) { return kind >= KEEPER_FIRST && kind < KEEPER_FIRST + KEEPER_COUNT || kind == COPY; }

    /** Whether it is one of her shadows in the Mirror Depths; and whether it is the Imperfect Copy. */
    static boolean shadow(int kind) { return kind >= SHADOW_FIRST && kind < SHADOW_FIRST + SHADOW_COUNT; }

    static boolean copy(int kind) { return kind == COPY; }

    /** Whether it is one of the Echoes, her from a doomed timeline. */
    static boolean echo(int kind) { return kind >= ECHO_FIRST && kind < ECHO_FIRST + ECHO_COUNT; }

    /** Whether it is the shadow of one she knows. */
    static boolean castShadow(int kind) { return kind >= CAST_FIRST && kind < CAST_FIRST + CAST_COUNT; }

    static int keeperOf(int dungeon) { return KEEPER_FIRST + Math.max(0, Math.min(KEEPER_COUNT - 1, dungeon / 4)); }

    /** Whether it is one of the great ones, who drop three things of gold or better: a hidden boss or a keeper. */
    static boolean great(int kind) { return hidden(kind) || keeper(kind); }

    /** Whether it floats in the air (and so is not held to the ground): the moth and the wisp, as well as the hornets. */
    static boolean floats(int kind) { return is(kind) && (way(kind) == BLINKER || way(kind) == ORBITER || way(kind) == ECHOER); }

    /** Whether it is one of those who swim in the lagoons. */
    static boolean swims(int kind) { return is(kind) && way(kind) == SWIMMER; }

    /** What one shrugs off (see {@link PlusRun#resists}), by what it is made of, as its name tells it: ink, fire, frost. */
    static int resists(int kind) {
        if (!is(kind) || shadow(kind) || kind == COPY || echo(kind) || castShadow(kind)) return 0;
        if (status(kind)) return STATUS_MADE[kind - STATUS_FIRST];
        String name = name(kind).toLowerCase(java.util.Locale.ROOT);
        int resists = 0;
        if (name.contains("ink") || name.contains("blot") || name.contains("squid")) resists |= PlusRun.RESISTS_INK;
        if (name.contains("ember") || name.contains("flame") || name.contains("fire") || name.contains("candle") || name.contains("cinder") || name.contains("lantern")) resists |= PlusRun.RESISTS_FIRE;
        if (name.contains("frost") || name.contains("rime") || name.contains("ice") || name.contains("snow") || name.contains("glacier")) resists |= PlusRun.RESISTS_COLD;
        return resists;
    }

    /** Whether a touch of one stuns her: the heavy-handed (stampers, rollers, chargers, sweepers). */
    static boolean stuns(int kind) {
        if (!is(kind)) return false;
        int way = way(kind);
        // (Only the two of the twelve made of nothing in particular are made to stun: the bookend and the clapper.)
        return status(kind) && STATUS_MADE[kind - STATUS_FIRST] == 0;
    }

    /** How one is seen going when beaten (see {@link PlusRun#defeatOf}), by what it is made of, as its name tells it. */
    static int defeat(int kind) {
        if (!is(kind)) return PlusRun.DEFEAT_FADE;
        // (The Copy cracks apart like the mirror she is; her shadows go as smoke does.)
        if (kind == COPY || echo(kind)) return PlusRun.DEFEAT_SHATTER;
        if (shadow(kind) || castShadow(kind)) return PlusRun.DEFEAT_FADE;
        // (The later hidden bosses each as they are made: slips, paper, gold leaf, wax, marks, pages.)
        if (hidden2(kind)) return new int[]{PlusRun.DEFEAT_CRUMPLE, PlusRun.DEFEAT_CRUMPLE, PlusRun.DEFEAT_PAGES, PlusRun.DEFEAT_MELT, PlusRun.DEFEAT_FADE,
                PlusRun.DEFEAT_PAGES}[kind - HIDDEN2_FIRST];
        if (status(kind)) {
            int made = STATUS_MADE[kind - STATUS_FIRST];
            return made == PlusRun.RESISTS_FIRE ? PlusRun.DEFEAT_ASH : made == PlusRun.RESISTS_COLD ? PlusRun.DEFEAT_SHATTER : made == PlusRun.RESISTS_INK ? PlusRun.DEFEAT_MELT
                    : PlusRun.DEFEAT_CRUMBLE;
        }
        String name = name(kind).toLowerCase(java.util.Locale.ROOT);
        if (name.contains("slime") || name.contains("jelly") || name.contains("blob")) return PlusRun.DEFEAT_SPLAT;
        if (name.contains("ink") || name.contains("blot") || name.contains("squid")) return PlusRun.DEFEAT_MELT;
        if (name.contains("book") || name.contains("tome") || name.contains("page") || name.contains("novel") || name.contains("scroll") || name.contains("bookworm")) return PlusRun.DEFEAT_PAGES;
        if (name.contains("golem") || name.contains("type") || name.contains("press") || name.contains("stamp") || name.contains("knight")) return PlusRun.DEFEAT_CRUMBLE;
        if (name.contains("paper") || name.contains("origami") || name.contains("crane") || name.contains("moth") || name.contains("headline") || name.contains("newsreel")) return PlusRun.DEFEAT_CRUMPLE;
        return PlusRun.DEFEAT_FADE;
    }

    /** Whether it is one of the hidden bosses; and the hidden boss of a chapter. */
    static boolean hidden(int kind) { return is(kind) && way(kind) == HIDDEN; }

    static int hiddenOf(int chapter) {
        // (No land of the first chapter has a hideout, so the moth waits in the second's: the first six in the odd chapters, the later six in the
        // even ones from the third, and the last chapter's two by turns.)
        int at = Math.max(1, Math.min(11, chapter));
        return at % 2 == 1 ? HIDDEN_FIRST + (at - 1) / 2 : HIDDEN2_FIRST + (at - 2) / 2;
    }

    /** The hidden boss in a land's hideout: its chapter's, but in the last chapter the Unwritten and the Last Librarian by turns. */
    static int hiddenIn(int number) {
        int chapter = RunLevels.chapterOf(number);
        if (chapter >= 11) return number % 2 == 1 ? HIDDEN_FIRST + 5 : HIDDEN2_FIRST + 5;
        return hiddenOf(chapter);
    }
    /** Whether it is one of the six later hidden bosses. */
    static boolean hidden2(int kind) { return kind >= HIDDEN2_FIRST && kind < HIDDEN2_FIRST + HIDDEN2_COUNT; }

    /** What a hidden boss throws: the moth eraser crumbs, Quillon ink, the line full stops, the golem type, Marginalia doodles, the Unwritten pages. */
    static int hiddenShot(int kind) {
        // (The later six: overdue slips, paper blades, gold flakes, molten wax, footnote marks, pale words.)
        if (hidden2(kind)) return new int[]{PlusRun.SCROLL, PlusRun.KNIFE, PlusRun.DOT, PlusRun.BLOT_SHOT, PlusRun.STOP, PlusRun.FEATHER}[kind - HIDDEN2_FIRST];
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
        // (The later six's: an armour, a weapon, an accessory, an armour, an accessory, a weapon, of their own (even) chapters.)
        if (hidden2(kind)) {
            int boss = kind - HIDDEN2_FIRST;
            int[] types = {RunLoot.ARMOUR, RunLoot.WEAPON, RunLoot.ACCESSORY, RunLoot.ARMOUR, RunLoot.ACCESSORY, RunLoot.WEAPON};
            int[] bases = {12, 3, 15, 18, 9, 11};
            return RunLoot.code(types[boss], Math.min(11, boss * 2 + 2), bases[boss], RunLoot.ROSE);
        }
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
        // (The twelve made of fire, frost and ink are tougher than others of their way.)
        return Math.round(PlusRun.BLOW * HEALTH[way(kind)] * (status(kind) ? STATUS_TOUGHER : echo(kind) ? ECHO_TOUGHER : castShadow(kind) ? CAST_TOUGHER : 1) * RunLoot.power(RunLoot.recommended(number)) * (1 + number / 100f));
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
