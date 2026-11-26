package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The thirty lands of Plus run, in five chapters of six. Each land is put together from
 * stretches of a few kinds, a gap here and a wall there, chosen by a number kept for that
 * land, so it is the same land every time. Later lands are longer, ask for more, and bring
 * in what their chapter adds: spikes, springs, and the signs that walk about.
 */
final class RunLevels {
    static final int COUNT = 30, CHAPTERS = 5, IN_CHAPTER = 6;
    /** With the second volume: fifty more lands, in five chapters of ten. */
    static final int TOTAL = 80, ALL_CHAPTERS = 10, IN_LATER_CHAPTER = 10;
    static final int ROWS = 18;
    /** The row the ground's top squares are in. */
    static final int FLOOR = 15;

    static final char AIR = ' ', BLOCK = '#', LEDGE = '=', SPIKE = '^', SPRING = 'S', FLAG = 'f', GATE = 'G';
    /**
     * The ground of the second volume: a thin ledge that can be jumped up through; a ledge
     * that crumbles a moment after it is stood on; belts that carry whatever is on them one
     * way or the other; slime, on which there is no stopping; a draught that lifts; a bookmark
     * door; and lava and water, which fill pits.
     */
    /**
     * A door into a place of its own, off the land: a hidden room with a stash in it, a
     * secret passage that comes out further on, a cellar that shuts behind her and fills
     * with signs until they are cleared, or a room where pluses fall to be caught. The door
     * is not solid; she goes in when she chooses to.
     */
    static final char ENTRY = 'H';
    /**
     * What someone in a land may ask of her, which she may do or not: find Boksun's three
     * rice cakes, which lie out of the way about the land; clear the slimes out of the
     * cellar, for Inju; carry Somi's letter to Yeseo at the gate without being hurt; reach
     * the next flag before Ttosun has finished counting; or find the room behind the crack,
     * for Galpi.
     */
    static final int CAKES = 1, CELLAR = 2, LETTER = 3, TIME = 4, ROOM = 5;

    /**
     * What getting past whoever waits at the end of a land wins, to keep: a colour of hair,
     * as the number of its form; or, below nothing, something else. The seven colours are
     * won one at a time through both volumes, black last, from the Mawang, who is the dark
     * one; white is not won here. Zero has no colour to give: his ring makes every form last
     * half as long again.
     */
    static final int RING = -1, NOTHING = 0;
    /** And what the seven others give: a heart more in every land, eight more shots in an ink gun, her colours ready sooner, or a bounty of pluses. */
    static final int HEART = -2, TANK = -3, QUICK = -4, BOUNTY = -5;

    static int prizeOf(int number) {
        int who = bossOf(number);
        return who == ILLYA ? PlusRun.RED : who == TSUBAKIEH ? PlusRun.BROWN : who == ZERO ? RING : who == GOKHAE ? PlusRun.PURPLE
                : who == MIRI ? PlusRun.YELLOW : who == YEOBAEK ? PlusRun.GREEN : who == KING ? PlusRun.BLUE : who == MAWANG ? PlusRun.BLACK
                : who == GUARD || who == SUMBI ? HEART : who == BUTLER || who == PARTY ? TANK : who == BYEORU ? QUICK : who == JUPAN || who == PADS ? BOUNTY
                : NOTHING;
    }

    /** What is asked in a land: nothing where someone waits at the end, or before the lands have places off them. */
    static int questOf(int number) {
        if (number < QUESTS_FROM || bossOf(number) >= 0) return 0;
        boolean other = number / 3 % 2 != 0;
        // In the first chapter there is neither a cellar nor a crack in a wall yet.
        if (chapterOf(number) == 0) return number % 3 == 2 ? CAKES : other ? TIME : LETTER;
        return number % 3 == 0 ? CELLAR : number % 3 == 2 ? other ? CAKES : ROOM : other ? TIME : LETTER;
    }

    /**
     * What doing what is asked pays, the first time in a land, besides its seal: a purse of
     * pluses, bigger with every chapter, for the rice cakes; two more shots in every ink gun
     * for the cellar; treats that last a twentieth longer for the letter; colours ready half
     * a second sooner for the race; and for the hidden room a white ribbon there and then.
     * And when everything asked in a chapter is done, a medal: two hundred pluses, and her
     * colours ready a second sooner.
     */
    static final int PURSE = 1, SHOTS = 2, LASTING = 3, QUICKER = 4, RIBBON = 5;

    static int questPrize(int quest) { return quest == CAKES ? PURSE : quest == CELLAR ? SHOTS : quest == LETTER ? LASTING : quest == TIME ? QUICKER : RIBBON; }

    /** Who asks it. */
    static int giverOf(int quest) {
        return quest == CAKES ? BOKSUN : quest == CELLAR ? INJU : quest == LETTER ? SOMI : quest == TIME ? TTOSUN : GALPI;
    }
    static final int STASH = 0, PASSAGE = 1, ARENA = 2, GAME = 3;
    /** And a range, where paper targets hang in the air and an ink gun that does not run dry is put in her hands for twelve seconds. */
    static final int RANGE = 4;
    /** And a room where signs keep coming for a quarter of a minute, to be dealt with as fast as they come. */
    static final int TRAMPLE = 5;
    /** How tall a place off the land is, from its floor to its ceiling. */
    static final int ROOM_TALL = 13;
    static final char THIN = '-', CRUMBLE = 'c', BELT_RIGHT = '>', BELT_LEFT = '<', SLICK = '~', DRAUGHT = 'u', DOOR = 'D', LAVA = 'L', WATER = 'w';
    /** The signs that get in her way: a minus walks, a times walks and cannot be landed on, a divide hops, and Zero is the last of them. */
    static final int MINUS = 0, TIMES = 1, DIVIDE = 2, ZERO = 3;
    /** And two that fight back from where they are: a minus that throws knives, and a divide that shoots its dots. */
    static final int THROWER = 4, GUNNER = 5;
    /** And two who are not signs at all: Illya, who floats and fires hearts, and Tsubakieh with her bat. Like Zero, each keeps a gate shut. */
    static final int ILLYA = 6, TSUBAKIEH = 7;
    /** The lands at whose end one of them waits. */
    static final int ILLYA_LAND = 18, TSUBAKIEH_LAND = 24;

    /**
     * The signs of the second volume: a slime in a tie that hops at her, a punctuation dragon
     * that flies and spits full stops, a censor bar that tiptoes and dashes, one of the Heart
     * Guard who marches after her, one of the queen's heavy words that falls, and a question
     * mark that bounds. And those who wait at the end of its chapters: Gokhae, the Gazette
     * Twins Miri and Gari, Yeobaek, King Slime and the Mawang.
     */
    static final int SLIME = 8, DRAGON = 9, BAR = 10, FAN = 11, WORD = 12, POGO = 13;
    static final int GOKHAE = 14, MIRI = 15, GARI = 16, YEOBAEK = 17, KING = 18, MAWANG = 19;
    /**
     * And seven more who wait, so that no ten lands go by without someone: the Guild Guard
     * at the end of the sixth land and Penchok-jipsa, the pen who is a butler, at the end of
     * the twelfth; and in the middle of each chapter of the second volume Byeoru the scribe,
     * Jupan with her abacus, Sumbi the diver, the Brake Pads, and the Straight-Ahead Party.
     */
    static final int GUARD = 20, BUTLER = 21, BYEORU = 22, JUPAN = 23, SUMBI = 24, PADS = 25, PARTY = 26;
    /** The Old Book, who sleeps at the bottom of some cellars and is woken by whoever clears them; and a paper target, which is no one. */
    static final int OLD_BOOK = 27, TARGET = 28;
    static final int GUARD_LAND = 6, BUTLER_LAND = 12;

    static boolean boss(int kind) { return kind == ZERO || kind == ILLYA || kind == TSUBAKIEH || kind >= GOKHAE && kind != TARGET; }

    /** Who waits at the end of a land, or -1: for the twins, Miri. */
    static int bossOf(int number) {
        if (number > COUNT) {
            // The middle of each chapter of the second volume, and its end.
            if (number % IN_LATER_CHAPTER == 5) return BYEORU + (number - COUNT) / IN_LATER_CHAPTER;
            return number % IN_LATER_CHAPTER != 0 ? -1 : new int[]{GOKHAE, MIRI, YEOBAEK, KING, MAWANG}[(number - COUNT) / IN_LATER_CHAPTER - 1];
        }
        return number == GUARD_LAND ? GUARD : number == BUTLER_LAND ? BUTLER : number == ILLYA_LAND ? ILLYA : number == TSUBAKIEH_LAND ? TSUBAKIEH
                : number == COUNT ? ZERO : -1;
    }

    /**
     * Every land is a place, not a line: ground that climbs and drops, and, as the chapters
     * go on, a tower to go up and over, a cave with a low roof, islands that float at
     * different heights, a high road and a low one that meet again; and in every land a
     * village, with someone in it and a stall. The first lands are the gentlest: each
     * chapter adds to what the one before it was made of. A door to a place off the land
     * comes with the second land, something asked of her with the fourth, and crowds with
     * the second chapter.
     */
    static final int SHAPED_FROM = 1, PLACES_FROM = 2, QUESTS_FROM = 4;
    private static final int ISLANDS = 40, ASCENT = 41, DESCENT = 42, TOWER = 43, SPLIT = 44, CAVE = 45, VILLAGE = 46;
    /**
     * And two that go round: a ring of ledges up over a middle and down again, with the
     * ground under it as the other way and something hung in the middle of it; and a tower
     * climbed by one side and come down by the other, from the top of which a high way leads
     * back over where she came from, to something out of the way.
     */
    private static final int LOOP = 47, ROUNDABOUT = 48;
    /** The shapes each chapter of the second volume adds to what it is made of. */
    private static final int[][] LATER_SHAPES = {
            {TOWER, ROUNDABOUT, SPLIT, ASCENT, DESCENT, ISLANDS, LOOP},
            {CAVE, CAVE, SPLIT, ASCENT, DESCENT, ROUNDABOUT, LOOP},
            {ISLANDS, ISLANDS, LOOP, LOOP, ASCENT, DESCENT, SPLIT},
            {CAVE, CAVE, ISLANDS, ASCENT, DESCENT, SPLIT, ROUNDABOUT},
            {ISLANDS, TOWER, SPLIT, CAVE, ASCENT, DESCENT, LOOP, ROUNDABOUT},
    };
    /**
     * Who may be met in a village, as the number of who holds something out: after Chikuwa
     * (1), Rune (2), Tsubakieh (3) and Illya (4) come Somi, Inju, Boksun, Ttosun, Galpi,
     * Yeseo, Nabi, Moru, Altteul at her stall, and the queen.
     */
    static final int SOMI = 5, INJU = 6, BOKSUN = 7, TTOSUN = 8, GALPI = 9, YESEO = 10, NABI = 11, MORU = 12, ALTTEUL = 13, NURI = 14;
    /** What each of them gives, from Somi to Moru; the queen gives her notes. */
    private static final int[] GIVES = {PlusRun.MAGNET, PlusRun.NOTES, PlusRun.BREAD, PlusRun.ICE, PlusRun.FORM + PlusRun.GREEN, PlusRun.MAGNET, PlusRun.ICE,
            PlusRun.SHIELD};
    private static final int THINS = 20, CRUMBLES = 21, BELTS = 22, SLICKS = 23, DRAUGHTS = 24, MOVERS = 25, LIFT = 26, DOORS = 27, LAVAS = 28,
            WATERS = 29, SLIMES = 30, DRAGONS = 31, BARS = 32, FANS = 33, WORDS = 34, POGOS = 35;
    /** What each chapter of the second volume is made of: the Half-Title Palace, the Gazette press, Between the Lines, the Slime Margins, the Appendix. */
    private static final int[][] LATER_POOLS = {
            {THINS, THINS, DOORS, DOORS, WORDS, WORDS, FANS, WATERS, 8, 6, 4, 1},
            {BELTS, BELTS, CRUMBLES, CRUMBLES, BARS, BARS, DRAGONS, LAVAS, 11, 6, 10, THINS},
            {MOVERS, MOVERS, LIFT, THINS, DRAGONS, POGOS, POGOS, BARS, DRAUGHTS, 4, 7, 8},
            {SLICKS, SLICKS, SLIMES, SLIMES, DRAUGHTS, DRAUGHTS, CRUMBLES, WATERS, POGOS, 9, 10, WORDS},
            {LAVAS, LAVAS, MOVERS, DOORS, WORDS, DRAGONS, BARS, SLIMES, FANS, SLICKS, DRAUGHTS, CRUMBLES, BELTS, THINS, LIFT, POGOS},
    };
    /** The numbers the lands of the second volume are made from, picked as those of the first were. */
    static final long[] LATER_SEEDS = {
            1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1,
    };

    private static final int PLAIN = 0, GAP = 1, STEPS = 2, WALL = 3, BRIDGE = 4, HIGH_ROAD = 5, SPIKES = 6, SPRING_WALL = 7,
            PEN = 8, HOPPER = 9, PILLARS = 10, PATROL = 11;
    /**
     * And from there on the lands are busier: a pack walking together, a swarm of slimes, two
     * that fly at different heights, a line coming down steps, and a big one that takes three
     * blows; and where an ink gun, a book or a brush lies, a stretch built for it follows:
     * a crowd in the open for the gun, a line for the book that goes through them, and a ring
     * of slimes for the brush. Each has a way over it for whoever has nothing in her hands.
     */
    private static final int PACK = 50, SWARM = 51, FLYERS = 52, SLOPE = 53, TOUGH = 54, GALLERY = 55, LINE = 56, CROWD = 57;
    private static final int[] MOBS = {PACK, PACK, SWARM, FLYERS, SLOPE, TOUGH};
    /** What the lands of each of the first three chapters are made of: what they were, and the shapes each is ready for. */
    private static final int[][] EARLY_POOLS = {
            {PLAIN, PLAIN, GAP, GAP, STEPS, WALL, PEN, HIGH_ROAD, ASCENT, DESCENT},
            {PLAIN, GAP, STEPS, SPIKES, SPIKES, BRIDGE, PEN, HIGH_ROAD, WALL, ASCENT, DESCENT, TOWER, CAVE},
            {GAP, STEPS, SPIKES, SPRING_WALL, PATROL, HIGH_ROAD, BRIDGE, PEN, ASCENT, DESCENT, TOWER, SPLIT, ISLANDS, PACK, LOOP, ROUNDABOUT},
    };
    private static final int[] HIGH_POOL = {ISLANDS, ISLANDS, ASCENT, DESCENT, TOWER, SPLIT, CAVE, GAP, SPIKES, HOPPER, PILLARS, BRIDGE, SPRING_WALL, PATROL,
            PACK, PACK, SWARM, SLOPE, TOUGH, LOOP, ROUNDABOUT};
    /** The stretches each chapter draws on; one that is named more than once comes up more often. */
    private static final int[][] POOLS = {
            {PLAIN, PLAIN, GAP, GAP, STEPS, WALL, PEN, HIGH_ROAD, BRIDGE},
            {PLAIN, GAP, STEPS, SPIKES, SPIKES, BRIDGE, PEN, HIGH_ROAD, WALL},
            {GAP, STEPS, SPIKES, SPRING_WALL, SPRING_WALL, PATROL, HIGH_ROAD, BRIDGE, PEN},
            {GAP, SPIKES, HOPPER, HOPPER, PILLARS, BRIDGE, SPRING_WALL, PATROL, STEPS},
            {GAP, SPIKES, HOPPER, PILLARS, BRIDGE, SPRING_WALL, PATROL, PEN, STEPS},
    };
    /**
     * The number each land is made from. They were picked by trying numbers until a player
     * that only runs right and jumps when something is in the way could cross the land:
     * see the tests.
     */
    static final long[] SEEDS = {
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1,
    };

    /** One land: its squares, what is in it, and where it begins and ends. */
    static final class Level {
        final int number, chapter, columns;
        final char[][] land;
        /** The pluses: the column and row of each. */
        final List<int[]> pluses = new ArrayList<>();
        /** The signs in her way: the kind, the column and the row each starts in. */
        final List<int[]> foes = new ArrayList<>();
        /** The things to fight with that lie about: the kind, as {@link PlusRun} numbers them, the column and the row. */
        final List<int[]> weapons = new ArrayList<>();
        /** What may be picked up to help: the kind, the column, the row, and who holds it out (0 no one, 1 Chikuwa, 2 Rune). */
        final List<int[]> items = new ArrayList<>();
        /** The columns of the flags she comes back to, in order, and of the gate. */
        final List<Integer> flags = new ArrayList<>();
        /** The ledges that move: the column and row each starts at, how long it is, how far it goes, whether up (1) or along (0), and how many tenths of a second there and back takes. */
        final List<int[]> movers = new ArrayList<>();
        /** The bookmark doors: the column of each, and of the door it lets her out at. */
        final List<int[]> doors = new ArrayList<>();
        /** What stands about in its villages to be looked at: which picture, the column and the row it stands in. */
        final List<int[]> decor = new ArrayList<>();
        /**
         * The places off the land that its doors lead to: for each, its kind; the column and
         * the floor of its door in the land; the first and last columns of the place itself,
         * which lies beyond the end of the land; the column she comes out at, which for a
         * passage is further on; the column of its door inside; whether its door in the land
         * is hidden until she breaks through to it; the column of its far door inside, for a
         * passage, or -1; and the floor she comes out onto.
         */
        final List<int[]> rooms = new ArrayList<>();
        /** The signs that walk in the places off the land, kept apart while the land itself is put together. */
        final List<int[]> lodgers = new ArrayList<>();
        /** How many columns the land proper has: what lies beyond is the places its doors lead to. */
        int main;
        /** What is asked of her in it, or 0; and the out-of-the-way places in it where something may be hidden: the column and row of each. */
        int quest;
        final List<int[]> nooks = new ArrayList<>();
        /** Where the place begins in which someone waits, or -1. */
        int arena = -1;
        int gate;
        /** Whether the gate stays shut until Zero has been dealt with. */
        boolean guarded;

        Level(int number, int columns) {
            this.number = number;
            this.quest = questOf(number);
            this.chapter = chapterOf(number);
            this.columns = columns;
            this.main = columns;
            land = new char[ROWS][columns];
            for (char[] row : land) java.util.Arrays.fill(row, AIR);
        }
    }

    private RunLevels() {}

    /** Which chapter a land belongs to, from 0; lands are numbered from 1. */
    static int chapterOf(int number) { return number > COUNT ? CHAPTERS + (number - COUNT - 1) / IN_LATER_CHAPTER : (number - 1) / IN_CHAPTER; }

    /** The land of a number from 1 to {@link #TOTAL}, made from the number kept for it. */
    static Level level(int number) { return level(number, number > COUNT ? LATER_SEEDS[number - COUNT - 1] : SEEDS[number - 1]); }

    /**
     * A land of the second volume: about twice as long as one of the first, with three flags,
     * laid from what its chapter is made of.
     */
    private static Level later(int number, long seed) {
        Random random = new Random(seed * 6151 + number);
        int chapter = chapterOf(number), boss = bossOf(number);
        Level draft = new Level(number, 900);
        Builder builder = new Builder(draft, random, 1, chapter);
        builder.ground(6);
        int stretches = boss >= 0 ? 12 : 16 + (number - COUNT - 1) / 7, last = -1;
        int[] pool = LATER_POOLS[chapter - CHAPTERS], shapes = LATER_SHAPES[chapter - CHAPTERS];
        for (int stretch = 0; stretch < stretches; stretch++) {
            int kind;
            // Two stretches in five are a shape of the ground; the rest are what the chapter is made of.
            do {
                int from = random.nextInt(10);
                kind = from < 3 ? shapes[random.nextInt(shapes.length)] : from < 5 ? MOBS[random.nextInt(MOBS.length)] : pool[random.nextInt(pool.length)];
            } while (kind == last);
            last = kind;
            builder.stretch(kind);
            if (stretch == stretches / 4 || stretch == stretches / 2 || stretch == stretches * 3 / 4) builder.flag();
            builder.rest();
            // After the first flag a book or a brush and what it is good against; after the second an ink gun and a crowd; and in half the lands another after the third.
            if (stretch == stretches / 4 || stretch == stretches / 2 || stretch == stretches * 3 / 4 && number % 2 == 1) {
                boolean gallery = stretch != stretches / 4;
                int start = builder.at;
                builder.stretch(gallery ? GALLERY : number % 2 == 0 ? LINE : CROWD);
                builder.before(gallery, start + 2);
                builder.rest();
                builder.after();
            }
            if (stretch == 1 || stretch == stretches * 5 / 8) {
                builder.stretch(VILLAGE);
                builder.rest();
            }
        }
        if (boss >= 0) builder.arena(boss);
        builder.ground(3);
        draft.gate = builder.at;
        int gateFloor = builder.floor;
        builder.ground(6);
        draft.main = builder.at;
        builder.rooms();
        builder.asked(draft.gate, gateFloor);
        Level level = new Level(number, builder.at);
        level.main = draft.main;
        level.rooms.addAll(draft.rooms);
        for (int row = 0; row < ROWS; row++) System.arraycopy(draft.land[row], 0, level.land[row], 0, level.columns);
        level.land[gateFloor - 1][draft.gate] = GATE;
        level.land[gateFloor - 2][draft.gate] = GATE;
        level.pluses.addAll(draft.pluses);
        level.foes.addAll(draft.foes);
        level.flags.addAll(draft.flags);
        level.items.addAll(draft.items);
        level.decor.addAll(draft.decor);
        level.weapons.addAll(draft.weapons);
        level.movers.addAll(draft.movers);
        level.doors.addAll(draft.doors);
        level.gate = draft.gate;
        level.arena = draft.arena;
        level.guarded = draft.guarded;
        armLater(level, new Random(seed * 104729 + number * 31L));
        level.foes.addAll(draft.lodgers);
        return level;
    }

    /** The row of the top of the ground at a column: of what stands on the bottom of the land there, whatever floats over it. None, -1, over a pit. */
    static int top(Level level, int column) {
        int row = ROWS;
        while (row > 0 && level.land[row - 1][column] == BLOCK) row--;
        return row == ROWS ? -1 : row;
    }

    /** The flat, empty columns of a land where something may be laid down: nothing on or over them, and nothing near. */
    private static List<Integer> free(Level level, int until) {
        List<Integer> free = new ArrayList<>();
        for (int column = 12; column < until; column++) {
            int floor = top(level, column);
            boolean clear = floor > 4;
            for (int beside = column - 1; beside <= column + 1 && clear; beside++) {
                clear = top(level, beside) == floor && level.land[floor - 1][beside] == AIR && level.land[floor - 2][beside] == AIR
                        && level.land[floor - 3][beside] == AIR;
            }
            for (int[] foe : level.foes) if (Math.abs(foe[1] - column) < 4) clear = false;
            for (int[] plus : level.pluses) if (plus[0] == column && clear && plus[1] >= floor - 3) clear = false;
            for (int[] weapon : level.weapons) if (Math.abs(weapon[1] - column) < 2) clear = false;
            for (int[] item : level.items) if (Math.abs(item[1] - column) < 2) clear = false;
            if (clear) free.add(column);
        }
        return free;
    }

    /** Takes one of the free columns, and with it those too near it to be used as well. */
    private static int take(List<Integer> free, Random random) {
        int column = free.remove(random.nextInt(free.size()));
        for (int index = free.size() - 1; index >= 0; index--) if (Math.abs(free.get(index) - column) < 6) free.remove(index);
        return column;
    }

    /**
     * What lies about a land of the second volume: something to fight with by where she
     * starts, by each flag and before whoever waits; her friends by the flags, Chikuwa, Rune
     * and then Tsubakieh with her mended bat or Illya with a compliment; signs that throw and
     * shoot; a red ribbon before lava and a blue one before water, most times; and here and
     * there another ribbon or something else to help.
     */
    private static void armLater(Level level, Random random) {
        int number = level.number, who = bossOf(number), waits = level.arena;
        level.weapons.add(new int[]{(number - 2) % PlusRun.GUN, 4, FLOOR - 1});
        for (int index = 0; index < 3; index++) {
            int flag = level.flags.get(index);
            level.weapons.add(new int[]{index == 2 && random.nextBoolean() ? PlusRun.GUN : (number + index) % PlusRun.GUN, flag + 1, top(level, flag + 1) - 1});
            int gives = index == 0 ? PlusRun.BREAD : index == 1 ? PlusRun.SHIELD : number % 2 == 0 ? PlusRun.HOMERUN : PlusRun.COMPLIMENT;
            level.items.add(new int[]{gives, flag + 3, top(level, flag + 3) - 1, index == 0 ? 1 : index == 1 ? 2 : number % 2 == 0 ? 3 : 4});
        }
        for (int[] foe : level.foes) {
            if (foe[0] == MINUS && random.nextBoolean()) foe[0] = THROWER;
            else if (foe[0] == DIVIDE && random.nextBoolean()) foe[0] = GUNNER;
        }
        if (waits >= 0) {
            // Before whoever waits lies what will do against her; and the Mawang's visitor has her friends beside her in there.
            level.weapons.add(new int[]{who == MIRI ? PlusRun.GUN : who == GOKHAE ? PlusRun.PEN : PlusRun.SWORD, waits - 2, top(level, waits - 2) - 1});
            if (who == MAWANG) {
                level.items.add(new int[]{PlusRun.BREAD, waits + 2, top(level, waits + 2) - 1, 1});
                level.items.add(new int[]{PlusRun.SHIELD, waits + 5, top(level, waits + 5) - 1, 2});
            }
        }
        // A ribbon for the lava or the water, two columns before it, two times in three.
        for (int column = 14; column < level.gate; column++) {
            int floor = top(level, column - 2);
            if (floor < 0 || top(level, column - 1) != floor) continue;
            char here = level.land[floor][column];
            if ((here == LAVA || here == WATER) && random.nextInt(3) != 0) {
                level.items.add(new int[]{PlusRun.FORM + (here == LAVA ? PlusRun.RED : PlusRun.BLUE), column - 2, floor - 1, 0});
            }
        }
        List<Integer> free = free(level, waits >= 0 ? waits - 4 : level.gate - 6);
        int[] ribbons = {PlusRun.BROWN, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.PURPLE, PlusRun.BROWN, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.PURPLE,
                PlusRun.BLACK, PlusRun.BLACK, PlusRun.WHITE};
        int[] treats = {PlusRun.ICE, PlusRun.MAGNET, PlusRun.NOTES, PlusRun.MAGNET};
        for (int count = 0; count < 4 && !free.isEmpty(); count++) {
            int column = take(free, random);
            if (count == 3) {
                level.items.add(new int[]{treats[random.nextInt(treats.length)], column, top(level, column) - 1, 0});
                continue;
            }
            int ribbon = ribbons[random.nextInt(ribbons.length)];
            // The black one hangs where only a jump will reach it: it is not to be walked into.
            level.items.add(new int[]{PlusRun.FORM + ribbon, column, top(level, column) - (ribbon == PlusRun.BLACK ? 3 : 1), 0});
        }
    }

    /** The land of a number as some other number would make it: for choosing the numbers kept. */
    static Level level(int number, long seed) {
        if (number > COUNT) return later(number, seed);
        Random random = new Random(seed * 7919 + number);
        int chapter = chapterOf(number);
        float hard = (number - 1) / (float) (COUNT - 1);
        // Worked out on a strip longer than any land needs, then cut to length.
        Level draft = new Level(number, 700);
        Builder builder = new Builder(draft, random, hard, chapter);
        builder.ground(6);
        int boss = bossOf(number);
        boolean shaped = number >= SHAPED_FROM;
        int stretches = boss >= 0 ? (shaped ? 8 : 6) : 6 + number / 3;
        int last = -1;
        for (int stretch = 0; stretch < stretches; stretch++) {
            int[] pool = chapter < EARLY_POOLS.length ? EARLY_POOLS[chapter] : HIGH_POOL;
            int kind;
            do kind = pool[random.nextInt(pool.length)];
            while (kind == last || (number <= 2 && (kind == BRIDGE || kind == HIGH_ROAD)));
            last = kind;
            builder.stretch(kind);
            // Somewhere to stand between one stretch and the next, with a flag on it a third and two thirds of the way.
            if (stretches >= 6 && (stretch == stretches / 3 || stretch == stretches * 2 / 3)) builder.flag();
            builder.rest();
            // After the first flag a book or a brush and what it is good against, from the second chapter; after the second an ink gun and a crowd, from the third.
            if (stretch == stretches / 3 && chapter >= 1 || stretch == stretches * 2 / 3 && chapter >= 2) {
                boolean gallery = stretch != stretches / 3;
                int start = builder.at;
                builder.stretch(gallery ? GALLERY : number % 2 == 0 || chapter < 2 ? LINE : CROWD);
                builder.before(gallery, start + 2);
                builder.rest();
                builder.after();
            }
            // Its village, a little way in.
            if (shaped && stretch == 1) {
                builder.stretch(VILLAGE);
                builder.rest();
            }
        }
        if (boss >= 0) builder.arena(boss);
        builder.ground(3);
        draft.gate = builder.at;
        int gateFloor = builder.floor;
        builder.ground(6);
        draft.main = builder.at;
        builder.rooms();
        builder.asked(draft.gate, gateFloor);
        Level level = new Level(number, builder.at);
        level.main = draft.main;
        level.rooms.addAll(draft.rooms);
        for (int row = 0; row < ROWS; row++) System.arraycopy(draft.land[row], 0, level.land[row], 0, level.columns);
        level.land[gateFloor - 1][draft.gate] = GATE;
        level.land[gateFloor - 2][draft.gate] = GATE;
        level.pluses.addAll(draft.pluses);
        level.foes.addAll(draft.foes);
        level.flags.addAll(draft.flags);
        level.items.addAll(draft.items);
        level.decor.addAll(draft.decor);
        level.weapons.addAll(draft.weapons);
        level.gate = draft.gate;
        level.arena = draft.arena;
        level.guarded = draft.guarded;
        arm(level, new Random(seed * 104729 + number * 31L));
        // The signs that walk in the places off the land join the rest only now, so that what the land is given is what it was given without them.
        level.foes.addAll(draft.lodgers);
        return level;
    }

    /**
     * Puts into a land the things to fight with, and gives some of its signs something to
     * fight back with. This is done once the land is laid, from numbers of its own, so the
     * ground is the same whatever is put on it.
     */
    private static void arm(Level level, Random random) {
        int number = level.number, chapter = level.chapter;
        // From the second land on there is something by where she starts, a different thing each land;
        // and by each flag another, in the later chapters now and then an ink gun.
        if (number >= 2) level.weapons.add(new int[]{(number - 2) % PlusRun.GUN, 4, FLOOR - 1});
        for (int index = 0; index < Math.min(2, level.flags.size()); index++) {
            boolean gun = chapter >= 1 && index == 1 && random.nextInt(chapter >= 3 ? 2 : 3) == 0;
            level.weapons.add(new int[]{gun ? PlusRun.GUN : (number + index) % PlusRun.GUN, level.flags.get(index) + 1, top(level, level.flags.get(index) + 1) - 1});
        }
        int waits = -1, who = -1;
        for (int[] foe : level.foes) {
            if (foe[0] == MINUS && chapter >= 1 && random.nextInt(chapter >= 3 ? 2 : 3) == 0) {
                foe[0] = THROWER;
            } else if (foe[0] == DIVIDE && random.nextBoolean()) {
                foe[0] = GUNNER;
            } else if (boss(foe[0])) {
                who = foe[0];
                waits = level.arena;
            }
        }
        // Before the place where one of them waits lies what will do against them: something to throw at Illya, who
        // keeps her distance, and something to swing at the other two, who send back or swallow what is thrown.
        if (waits >= 0) level.weapons.add(new int[]{who == ILLYA ? PlusRun.PEN : PlusRun.SWORD, waits - 2, top(level, waits - 2) - 1});
        // And before Zero, the notes of the one who came this way alone.
        if (who == ZERO) level.items.add(new int[]{PlusRun.NOTES, waits - 1, top(level, waits - 1) - 1, 0});
        // Her friends wait by the flags once they have joined her: Chikuwa with bread by the first, from the third land,
        // and Rune with a ball of her ink by the second, from the ninth.
        for (int index = 0; index < level.flags.size(); index++) {
            if (index == 0 && number >= 3) level.items.add(new int[]{PlusRun.BREAD, level.flags.get(index) + 3, top(level, level.flags.get(index) + 3) - 1, 1});
            if (index == 1 && number >= 9) level.items.add(new int[]{PlusRun.SHIELD, level.flags.get(index) + 3, top(level, level.flags.get(index) + 3) - 1, 2});
        }
        // Here and there on flat, empty ground lies something else to help: ice cream from the fourth land,
        // and from the second chapter a magnet or, now and then, a page of notes.
        List<Integer> free = new ArrayList<>();
        for (int column = 12; column < (waits >= 0 ? waits - 4 : level.gate - 6); column++) {
            int floor = top(level, column);
            boolean clear = floor > 4;
            for (int beside = column - 1; beside <= column + 1 && clear; beside++) {
                clear = top(level, beside) == floor && level.land[floor - 1][beside] == AIR && level.land[floor - 2][beside] == AIR;
            }
            for (int[] foe : level.foes) if (Math.abs(foe[1] - column) < 4) clear = false;
            for (int[] plus : level.pluses) if (plus[0] == column && clear && plus[1] >= floor - 2) clear = false;
            for (int[] weapon : level.weapons) if (Math.abs(weapon[1] - column) < 2) clear = false;
            for (int[] item : level.items) if (Math.abs(item[1] - column) < 2) clear = false;
            if (clear) free.add(column);
        }
        int wanted = number < 4 ? 0 : chapter == 0 ? 1 : 2;
        for (int count = 0; count < wanted && !free.isEmpty(); count++) {
            int column = free.remove(random.nextInt(free.size()));
            int kind = chapter == 0 || count == 0 && random.nextBoolean() ? PlusRun.ICE : random.nextInt(4) == 0 ? PlusRun.NOTES : PlusRun.MAGNET;
            level.items.add(new int[]{kind, column, top(level, column) - 1, 0});
            for (int index = free.size() - 1; index >= 0; index--) if (Math.abs(free.get(index) - column) < 6) free.remove(index);
        }
        // And from the eighth land on a ribbon, brown or purple by turns: the two that change nothing of how she moves.
        if (number >= 8 && !free.isEmpty()) {
            int column = free.get(new Random(number * 977L).nextInt(free.size()));
            level.items.add(new int[]{PlusRun.FORM + (number % 2 == 0 ? PlusRun.BROWN : PlusRun.PURPLE), column, top(level, column) - 1, 0});
        }
    }

    /** Lays a land down from left to right. */
    private static final class Builder {
        private final Level level;
        private final Random random;
        private final float hard;
        private final int chapter;
        /** The next column to be laid. */
        int at;
        /** The row the ground's top squares are in where it is being laid now: from the twenty-third land on, the ground rises and falls. */
        int floor = FLOOR;

        Builder(Level level, Random random, float hard, int chapter) {
            this.level = level;
            this.random = random;
            this.hard = hard;
            this.chapter = chapter;
        }

        /** The doors asked for so far: the kind of place each leads to, its column and floor, whether it is hidden, and for a passage where it comes out and onto what floor. */
        final List<int[]> wanted = new ArrayList<>();
        private int[] passage;
        private boolean inVillage;

        /** A door in the land, where the ground is being laid now, to a place of a kind. */
        int[] door(int kind, int column, boolean hidden) { return door(kind, column, hidden ? 1 : 0, floor); }

        /** The same, standing on something at a row of its own: hidden behind a crack (1), or up out of the way (2), or plain. */
        int[] door(int kind, int column, int hidden, int row) {
            level.land[row - 1][column] = ENTRY;
            int[] want = {kind, column, row, hidden, column, row};
            wanted.add(want);
            return want;
        }

        /**
         * At the near end of a crowd, between what lies there to fight with and its shelf: in
         * one land in three a cellar door, by the book or the brush; in another a crack in the
         * wall there with a stash behind it; and in the third a crack by the ink gun, with a
         * passage behind it that comes out beyond the crowd.
         */
        void before(boolean gallery, int column) {
            int number = level.number;
            if (gallery) {
                // In two lands of three, a crack by the ink gun with a passage behind it.
                if (number % 3 != 2 && passage == null && !has(PASSAGE)) passage = door(PASSAGE, column, true);
            } else if (number % 3 == 0) {
                if (!has(ARENA)) door(ARENA, column, false);
            } else if (number % 3 == 2 && !has(STASH)) {
                // A stash: behind a crack in the wall, or, in every other such land, up out of the way, on a ledge of its own over the crowd's.
                if (number / 3 % 2 == 0) {
                    door(STASH, column, true);
                } else {
                    ledge(column + 6, floor - 5, 2);
                    door(STASH, column + 6, 2, floor - 5);
                }
            }
        }

        private boolean has(int kind) {
            for (int[] want : wanted) if (want[0] == kind && (kind != STASH || want[3] != 0)) return true;
            return false;
        }

        /** After it: where a passage begun before it comes out. */
        void after() {
            if (passage == null || passage[4] != passage[1]) return;
            passage[4] = at - 1;
            passage[5] = floor;
            level.land[floor - 1][at - 1] = ENTRY;
        }

        /** What the land's quest needs lying about: three rice cakes in out-of-the-way places, or Yeseo waiting by the gate for a letter. */
        void asked(int gate, int gateFloor) {
            if (level.quest == CAKES) {
                List<int[]> nooks = new ArrayList<>(level.nooks);
                // Short of three such places, the rest are up on the ledges of the places off the land.
                for (int[] room : level.rooms) nooks.add(new int[]{room[3] + 5, FLOOR - 3});
                for (int which : new int[]{0, nooks.size() / 2, nooks.size() - 1}) {
                    level.items.add(new int[]{PlusRun.CAKE, nooks.get(which)[0], nooks.get(which)[1], 0});
                }
            } else if (level.quest == LETTER) {
                level.items.add(new int[]{PlusRun.DROPOFF, gate - 3, gateFloor - 1, YESEO});
            }
        }

        /** A wall from the top of the land to the bottom, some columns thick. */
        void wall(int columns) {
            for (int column = at; column < at + columns; column++) {
                for (int row = 0; row < ROWS; row++) level.land[row][column] = BLOCK;
            }
            at += columns;
        }

        /**
         * The places the doors lead to, laid one after another beyond the end of the land,
         * each walled all round, thirteen squares from floor to ceiling, and each a place for
         * something. A stash is a climb, with a chest at the top of it that is seen from the
         * door and not reached from it: up steps that are thin, and in the second volume
         * crumble, or by two springs. A passage is another way through the land, three
         * screens long, with spikes to cross on ledges, signs walking in it, something worth
         * having up out of the way, and a chest by its far door. A cellar is wide, with ledges
         * at two heights to fight from. And the rooms where a game is played have ledges to
         * play it from. Nothing here is drawn from the numbers the land is made from, so the
         * land is the same land whatever is behind its doors.
         */
        void rooms() {
            floor = FLOOR;
            int ceiling = FLOOR - ROOM_TALL, number = level.number;
            boolean second = number > COUNT;
            for (int[] want : wanted) {
                // In one cellar in two the Old Book sleeps under the last wave, and it is wider for him.
                boolean den = want[0] == ARENA && number % 6 == 0;
                int kind = want[0], wide = kind == PASSAGE ? 46 : kind == ARENA ? den ? 30 : 26 : kind == GAME ? 20 : 22;
                wall(2);
                int from = at, to = from + wide - 1, far = -1, middle = (from + to) / 2;
                for (int column = from; column <= to; column++) {
                    for (int row = 0; row < ROWS; row++) if (row < ceiling || row >= FLOOR) level.land[row][column] = BLOCK;
                }
                at += wide;
                wall(2);
                level.land[FLOOR - 1][from + 1] = ENTRY;
                char step = second ? CRUMBLE : THIN;
                int[] treats = {PlusRun.ICE, PlusRun.MAGNET, PlusRun.BREAD, PlusRun.NOTES};
                int treat = want[3] != 0 ? PlusRun.FORM + PlusRun.YELLOW : treats[number % treats.length];
                if (kind == STASH) {
                    pluses(from + 3, from + 8, FLOOR - 1);
                    if (number % 4 < 2) {
                        // Steps up the room, each two squares over the last, to a shelf along its far wall with the chest on it.
                        put(from + 5, FLOOR - 2, 3, THIN);
                        put(from + 9, FLOOR - 4, 3, step);
                        put(from + 13, FLOOR - 6, 3, step);
                        ledge(from + 17, FLOOR - 8, to - from - 16);
                        pluses(from + 9, from + 11, FLOOR - 6);
                        pluses(from + 13, from + 15, FLOOR - 8);
                        level.items.add(new int[]{treat, from + 14, FLOOR - 7, 0});
                        level.items.add(new int[]{PlusRun.CHEST, to - 1, FLOOR - 9, 0});
                    } else {
                        // A spring on the floor to a shelf half way up, and a spring at the end of that to a shelf under the ceiling, where the chest is.
                        level.land[FLOOR - 1][from + 9] = SPRING;
                        ledge(from + 12, FLOOR - 5, to - from - 11);
                        level.land[FLOOR - 6][to - 3] = SPRING;
                        ledge(from + 11, FLOOR - 10, 6);
                        pluses(from + 13, from + 17, FLOOR - 7);
                        level.items.add(new int[]{treat, from + 15, FLOOR - 6, 0});
                        level.items.add(new int[]{PlusRun.CHEST, from + 12, FLOOR - 11, 0});
                    }
                } else if (kind == PASSAGE) {
                    // A way through: two stretches of spikes, each crossed on ledges; signs that walk between them; an upper way to
                    // something worth having; and a chest by the door at the far end.
                    far = to - 1;
                    level.land[FLOOR - 1][far] = ENTRY;
                    if (chapter >= 1) put(from + 7, FLOOR - 1, 6, SPIKE);
                    put(from + 9, FLOOR - 3, 2, THIN);
                    pluses(from + 8, from + 11, FLOOR - 5);
                    ledge(from + 15, FLOOR - 3, 4);
                    pluses(from + 15, from + 18, FLOOR - 4);
                    level.lodgers.add(new int[]{MINUS, from + 17, FLOOR - 1});
                    if (chapter >= 1) put(from + 22, FLOOR - 1, 9, SPIKE);
                    put(from + 24, FLOOR - 3, 2, step);
                    put(from + 28, FLOOR - 3, 2, step);
                    pluses(from + 24, from + 29, FLOOR - 5);
                    level.lodgers.add(new int[]{chapter >= 2 ? TIMES : MINUS, from + 35, FLOOR - 1});
                    ledge(from + 32, FLOOR - 3, 3);
                    ledge(from + 36, FLOOR - 5, 3);
                    pluses(from + 32, from + 34, FLOOR - 4);
                    level.items.add(new int[]{PlusRun.FORM + (number % 2 == 0 ? PlusRun.YELLOW : PlusRun.GREEN), from + 37, FLOOR - 6, 0});
                    pluses(from + 39, to - 6, FLOOR - 1);
                    level.items.add(new int[]{PlusRun.CHEST, to - 4, FLOOR - 1, 0});
                } else if (kind == ARENA) {
                    ledge(from + 3, FLOOR - 3, 4);
                    ledge(to - 6, FLOOR - 3, 4);
                    ledge(middle - 2, FLOOR - 5, 4);
                } else if (kind == GAME) {
                    ledge(from + 4, FLOOR - 3, 3);
                    ledge(to - 6, FLOOR - 3, 3);
                } else if (kind == RANGE) {
                    ledge(from + 5, FLOOR - 3, 3);
                    ledge(to - 7, FLOOR - 3, 3);
                    ledge(middle - 1, FLOOR - 5, 3);
                } else {
                    ledge(from + 6, FLOOR - 3, 3);
                    ledge(to - 8, FLOOR - 3, 3);
                }
                level.rooms.add(new int[]{kind, want[1], want[2], from, to, want[4], from + 1, want[3], far, want[5], den ? 1 : 0});
            }
        }

        /** Flat ground for some columns. */
        void ground(int columns) {
            for (int column = at; column < at + columns; column++) {
                for (int row = floor; row < ROWS; row++) level.land[row][column] = BLOCK;
            }
            at += columns;
        }

        void gap(int columns) { at += columns; }

        /** A pile of blocks standing on the ground at a column. */
        void pile(int column, int high) {
            for (int up = 1; up <= high; up++) level.land[floor - up][column] = BLOCK;
        }

        /**
         * A low shelf at the near end of a crowd: what walks about inside turns at it as at a
         * wall, but she walks under it, what she throws goes under it, and from on top of it
         * the ledge over the crowd is a jump away.
         */
        void shelf(int column) { level.land[floor - 1][column] = LEDGE; }

        void ledge(int column, int row, int length) {
            for (int along = 0; along < length; along++) level.land[row][column + along] = LEDGE;
        }

        void pluses(int from, int to, int row) {
            for (int column = from; column <= to; column++) level.pluses.add(new int[]{column, row});
        }

        void foe(int kind, int column) { level.foes.add(new int[]{kind, column, floor - 1}); }

        /** The sign that walks about in this chapter: a minus at first, a times as well from the third. */
        int walker() { return chapter >= 2 && random.nextInt(3) == 0 ? TIMES : MINUS; }

        /** A few columns of flat ground between two stretches. */
        void rest() {
            int from = at;
            ground(3 + random.nextInt(2));
            if (random.nextBoolean()) pluses(from + 1, from + 2, floor - 1);
        }

        void flag() {
            int from = at;
            ground(3);
            level.land[floor - 1][from + 1] = FLAG;
            level.flags.add(from + 1);
            level.nooks.add(new int[]{from, floor - 3});
        }

        /** Fills a pit, some columns of nothing, with lava or with water, from the level of the ground down. */
        void lake(int from, int wide, char with) {
            for (int column = from; column < from + wide; column++) {
                for (int row = floor; row < ROWS; row++) level.land[row][column] = with;
            }
        }

        void put(int column, int row, int length, char what) {
            for (int along = 0; along < length; along++) level.land[row][column + along] = what;
        }

        void stretch(int kind) {
            int from = at;
            switch (kind) {
                case ISLANDS: {
                    // Three islands in the sky over nothing, each a jump from the last, none more than two squares higher than the one before.
                    int high = floor - 1 - random.nextInt(2);
                    gap(16);
                    for (int island = 0; island < 3; island++) {
                        int column = from + 1 + island * 5;
                        for (int along = 0; along < 3; along++) {
                            level.land[high][column + along] = BLOCK;
                            level.land[high + 1][column + along] = BLOCK;
                        }
                        pluses(column, column + 2, high - 2);
                        if (island == 1 && random.nextBoolean()) level.foes.add(new int[]{MINUS, column + 1, high - 1});
                        high = Math.max(5, Math.min(floor - 1, high + random.nextInt(4) - 2));
                    }
                    break;
                }
                case ASCENT:
                case DESCENT: {
                    // The ground climbs by steps, or drops away; whichever there is room for.
                    boolean up = kind == ASCENT ? floor > 10 : floor >= FLOOR;
                    if (up) {
                        int steps = 2 + random.nextInt(2);
                        ground(2);
                        for (int step = 0; step < steps; step++) {
                            floor--;
                            ground(2);
                        }
                        pluses(from + 2, from + 1 + steps * 2, floor - 2);
                    } else {
                        ground(3);
                        floor = Math.min(FLOOR, floor + 2 + random.nextInt(3));
                        ground(4);
                        pluses(from + 3, from + 5, floor - 3);
                    }
                    break;
                }
                case LOOP: {
                    // A ring: up by two ledges, over the top, and down by two more; or along the ground under it, where something walks. In its middle, something hung.
                    ground(15);
                    ledge(from + 1, floor - 2, 2);
                    ledge(from + 3, floor - 4, 2);
                    ledge(from + 6, floor - 6, 3);
                    ledge(from + 10, floor - 4, 2);
                    ledge(from + 12, floor - 2, 2);
                    pluses(from + 1, from + 2, floor - 4);
                    pluses(from + 3, from + 4, floor - 6);
                    pluses(from + 6, from + 8, floor - 8);
                    pluses(from + 10, from + 11, floor - 6);
                    pluses(from + 12, from + 13, floor - 4);
                    foe(MINUS, from + 8);
                    level.nooks.add(new int[]{from + 7, floor - 4});
                    break;
                }
                case ROUNDABOUT: {
                    // A tower with ledges up one side and down the other; and from its top a high way back over where she came from, to something out of the way.
                    ground(13);
                    for (int column = from + 5; column <= from + 7; column++) pile(column, 5);
                    ledge(from + 2, floor - 2, 3);
                    ledge(from + 3, floor - 4, 2);
                    ledge(from + 8, floor - 3, 2);
                    ledge(from + 1, floor - 7, 3);
                    pluses(from + 5, from + 7, floor - 7);
                    pluses(from + 1, from + 3, floor - 9);
                    pluses(from + 8, from + 9, floor - 5);
                    level.nooks.add(new int[]{from + 1, floor - 8});
                    break;
                }
                case TOWER: {
                    // A tower to go up and over: ledges up its near side, its top, and a drop beyond.
                    ground(10);
                    for (int column = from + 4; column <= from + 6; column++) pile(column, 5);
                    level.nooks.add(new int[]{from + 5, floor - 6});
                    ledge(from + 1, floor - 2, 3);
                    ledge(from + 2, floor - 4, 2);
                    pluses(from + 4, from + 6, floor - 7);
                    pluses(from + 1, from + 2, floor - 4);
                    break;
                }
                case SPLIT: {
                    // A low road with spikes and something walking on it, and a high road over it with more to collect: they meet again.
                    ground(13);
                    pile(from + 1, 1);
                    ledge(from + 3, floor - 3, 8);
                    level.land[floor - 1][from + 5] = SPIKE;
                    level.land[floor - 1][from + 6] = SPIKE;
                    foe(TIMES, from + 9);
                    pile(from + 12, 1);
                    pluses(from + 3, from + 10, floor - 5);
                    break;
                }
                case CAVE: {
                    // A cave: the roof comes down low for a way, with spikes to hop under it.
                    ground(13);
                    for (int column = from + 2; column <= from + 10; column++) {
                        for (int row = floor - 7; row <= floor - 4; row++) level.land[row][column] = BLOCK;
                    }
                    level.land[floor - 1][from + 6] = SPIKE;
                    pluses(from + 3, from + 5, floor - 1);
                    pluses(from + 7, from + 9, floor - 1);
                    break;
                }
                case VILLAGE: {
                    // A village: houses, a lantern and a sign to look at, someone to meet, and a stall.
                    ground(23);
                    int who = SOMI + (level.number + level.items.size()) % (ALTTEUL - SOMI);
                    if (level.chapter == CHAPTERS && random.nextBoolean()) who = NURI;
                    // In its first village stands whoever has something to ask of her.
                    if (!inVillage && level.quest != 0) who = giverOf(level.quest);
                    level.decor.add(new int[]{random.nextInt(3), from + 2, floor - 1});
                    level.decor.add(new int[]{3 + random.nextInt(2), from + 6, floor - 1});
                    level.decor.add(new int[]{random.nextInt(3), from + 12, floor - 1});
                    level.decor.add(new int[]{5, from + 17, floor - 1});
                    level.decor.add(new int[]{6 + random.nextInt(2), from + 21, floor - 1});
                    level.items.add(new int[]{who == NURI ? PlusRun.NOTES : GIVES[who - SOMI], from + 7, floor - 1, who});
                    level.items.add(new int[]{PlusRun.STALL, from + 17, floor - 1, ALTTEUL});
                    if (!inVillage) {
                        inVillage = true;
                        // A stash in the even lands; in the odd ones a game: the room where pluses fall, the range, and the room where signs keep coming, by turns.
                        if (level.number >= PLACES_FROM) {
                            door(level.number % 2 == 0 ? STASH : level.number % 6 == 1 ? GAME : level.number % 6 == 3 ? RANGE : TRAMPLE, from + 14, false);
                        }
                    }                    break;
                }
                case PACK: {
                    // Three to five walking together between two blocks, and a ledge over them.
                    int count = 3 + random.nextInt(3);
                    ground(13);
                    shelf(from);
                    pile(from + 12, 1);
                    ledge(from + 2, floor - 3, 9);
                    for (int which = 0; which < count; which++) foe(which == 2 ? walker() : MINUS, from + 3 + which * 2 - (which == 4 ? 1 : 0));
                    level.nooks.add(new int[]{from + 6, floor - 4});
                    pluses(from + 2, from + 10, floor - 5);
                    break;
                }
                case SWARM: {
                    ground(12);
                    shelf(from);
                    pile(from + 11, 1);
                    ledge(from + 2, floor - 3, 8);
                    for (int column : new int[]{3, 5, 6, 8, 10}) foe(SLIME, from + column);
                    level.nooks.add(new int[]{from + 6, floor - 4});
                    pluses(from + 2, from + 9, floor - 5);
                    break;
                }
                case FLYERS: {
                    ground(11);
                    level.foes.add(new int[]{DRAGON, from + 3, floor - 1});
                    level.foes.add(new int[]{DRAGON, from + 7, floor - 3});
                    pluses(from + 2, from + 8, floor - 2);
                    break;
                }
                case SLOPE: {
                    // Steps, up or down, with one walking on each.
                    boolean up = floor > 9;
                    ground(2);
                    for (int step = 0; step < 3; step++) {
                        floor += up ? -1 : 1;
                        int start = at;
                        ground(3);
                        foe(MINUS, start + 1);
                        pluses(start + 1, start + 1, floor - 4);
                    }
                    break;
                }
                case TOUGH: {
                    ground(9);
                    shelf(from);
                    pile(from + 8, 1);
                    ledge(from + 2, floor - 3, 5);
                    level.foes.add(new int[]{MINUS, from + 5, floor - 1, 3});
                    level.nooks.add(new int[]{from + 4, floor - 4});
                    pluses(from + 2, from + 6, floor - 5);
                    break;
                }
                case GALLERY: {
                    // An ink gun, and a long open stretch with a crowd in it to empty it into.
                    ground(21);
                    level.weapons.add(new int[]{PlusRun.GUN, from + 1, floor - 1});
                    level.nooks.add(new int[]{from + 11, floor - 4});
                    shelf(from + 3);
                    pile(from + 20, 1);
                    ledge(from + 5, floor - 3, 13);
                    for (int column = from + 6; column <= from + 15; column += 3) foe(column == from + 12 ? walker() : MINUS, column);
                    foe(SLIME, from + 8);
                    foe(SLIME, from + 14);
                    level.foes.add(new int[]{MINUS, from + 18, floor - 1, 3});
                    pluses(from + 5, from + 17, floor - 5);
                    break;
                }
                case LINE: {
                    // A book, and four in a line for it to go through.
                    ground(14);
                    level.weapons.add(new int[]{PlusRun.BOOK, from + 1, floor - 1});
                    level.nooks.add(new int[]{from + 8, floor - 4});
                    shelf(from + 3);
                    pile(from + 13, 1);
                    ledge(from + 5, floor - 3, 7);
                    for (int column = from + 5; column <= from + 11; column += 2) foe(MINUS, column);
                    pluses(from + 5, from + 11, floor - 5);
                    break;
                }
                case CROWD: {
                    // A brush, and slimes that crowd round whoever comes in.
                    ground(12);
                    level.weapons.add(new int[]{PlusRun.BRUSH, from + 1, floor - 1});
                    level.nooks.add(new int[]{from + 7, floor - 4});
                    shelf(from + 3);
                    pile(from + 11, 1);
                    ledge(from + 5, floor - 3, 5);
                    for (int column : new int[]{5, 6, 8, 9}) foe(SLIME, from + column);
                    pluses(from + 5, from + 9, floor - 5);
                    break;
                }
                case THINS: {
                    // A long gap, and two thin ledges over it to go by.
                    int rise = random.nextInt(2);
                    gap(10);
                    put(from + 2, floor - 1, 2, THIN);
                    put(from + 6, floor - 2 - rise, 2, THIN);
                    pluses(from + 2, from + 3, floor - 3);
                    pluses(from + 6, from + 7, floor - 4 - rise);
                    break;
                }
                case CRUMBLES: {
                    // A gap bridged by ledges that will not bear standing on for long.
                    int wide = 5 + random.nextInt(3);
                    gap(wide);
                    put(from, floor, wide, CRUMBLE);
                    pluses(from + 1, from + wide - 2, floor - 1);
                    break;
                }
                case BELTS: {
                    // A belt: against her, or with her and into spikes.
                    boolean with = random.nextBoolean();
                    ground(9);
                    put(from + 1, floor, 6, with ? BELT_RIGHT : BELT_LEFT);
                    if (with) level.land[floor - 1][from + 7] = SPIKE;
                    pluses(from + 2, from + 5, floor - (with ? 3 : 1));
                    break;
                }
                case SLICKS: {
                    // Slime underfoot, and spikes in the middle of it.
                    ground(10);
                    put(from + 1, floor, 8, SLICK);
                    level.land[floor - 1][from + 5] = SPIKE;
                    pluses(from + 2, from + 3, floor - 1);
                    pluses(from + 5, from + 5, floor - 4);
                    pluses(from + 7, from + 8, floor - 1);
                    break;
                }
                case DRAUGHTS: {
                    // A gap too wide to jump, with a draught coming up it that carries her over.
                    int wide = 6 + random.nextInt(2);
                    gap(wide);
                    for (int column = from; column < from + wide; column++) {
                        for (int row = floor - 6; row < ROWS; row++) level.land[row][column] = DRAUGHT;
                    }
                    pluses(from + 1, from + wide - 2, floor - 6);
                    break;
                }
                case MOVERS: {
                    // A gap too wide to jump, and a ledge that goes to and fro across it.
                    gap(7);
                    level.movers.add(new int[]{from, floor, 2, 5, 0, 44 + random.nextInt(3) * 6});
                    pluses(from + 2, from + 4, floor - 3);
                    break;
                }
                case LIFT: {
                    // A wall too high to jump, and a ledge that goes up and down beside it.
                    ground(1);
                    gap(2);
                    level.movers.add(new int[]{from + 1, floor, 2, 5, 1, 50});
                    ground(4);
                    pile(from + 3, 5);
                    pile(from + 4, 5);
                    pluses(from + 3, from + 4, floor - 7);
                    pluses(from + 1, from + 2, floor - 4);
                    break;
                }
                case DOORS: {
                    // A wall far too high to jump, with a bookmark door on each side of it.
                    ground(9);
                    pile(from + 3, 6);
                    pile(from + 4, 6);
                    level.land[floor - 1][from + 1] = DOOR;
                    level.land[floor - 1][from + 6] = DOOR;
                    level.doors.add(new int[]{from + 1, from + 6, floor});
                    pluses(from + 7, from + 8, floor - 1);
                    break;
                }
                case LAVAS:
                case WATERS: {
                    // A pit of lava or of water, with thin ledges over it; and pluses just over its top, for whoever can walk on it.
                    gap(8);
                    lake(from, 8, kind == LAVAS ? LAVA : WATER);
                    put(from + 1, floor - 2, 2, THIN);
                    put(from + 5, floor - 2, 2, THIN);
                    pluses(from + 1, from + 6, floor - 1);
                    pluses(from + 3, from + 4, floor - 5);
                    break;
                }
                case SLIMES: {
                    ground(9);
                    foe(SLIME, from + 4);
                    foe(SLIME, from + 7);
                    pluses(from + 1, from + 2, floor - 1);
                    break;
                }
                case DRAGONS: {
                    ground(10);
                    foe(DRAGON, from + 5);
                    pluses(from + 3, from + 7, floor - 1);
                    break;
                }
                case BARS: {
                    ground(11);
                    pile(from, 1);
                    pile(from + 10, 1);
                    foe(BAR, from + 8);
                    pluses(from + 2, from + 5, floor - 3);
                    break;
                }
                case FANS: {
                    ground(9);
                    pile(from, 1);
                    pile(from + 8, 1);
                    foe(FAN, from + 6);
                    pluses(from + 2, from + 6, floor - 1);
                    break;
                }
                case WORDS: {
                    // The queen's words hang over the way, with something under each to tempt her to stop.
                    ground(10);
                    foe(WORD, from + 3);
                    foe(WORD, from + 7);
                    pluses(from + 3, from + 3, floor - 1);
                    pluses(from + 7, from + 7, floor - 1);
                    break;
                }
                case POGOS: {
                    ground(10);
                    pile(from, 1);
                    pile(from + 9, 1);
                    foe(POGO, from + 5);
                    pluses(from + 3, from + 6, floor - 5);
                    break;
                }
                case GAP: {
                    int wide = 2 + (hard > .35f && random.nextBoolean() ? 1 : 0);
                    gap(wide);
                    pluses(from, from + wide - 1, floor - 3);
                    break;
                }
                case STEPS: {
                    // Up a step at a time and off the far end.
                    int top = hard > .45f && random.nextBoolean() ? 3 : 2;
                    ground(top + 1);
                    for (int step = 1; step <= top; step++) pile(from + step, step);
                    pluses(from + top, from + top, floor - top - 2);
                    break;
                }
                case WALL: {
                    ground(4);
                    pile(from + 1, 2);
                    pile(from + 2, 2);
                    pluses(from + 1, from + 2, floor - 4);
                    break;
                }
                case BRIDGE: {
                    // Ledges over a long gap, each a short jump from the last and no more than two squares higher.
                    int ledges = 2 + (hard > .5f ? 1 : 0), row = floor - 1;
                    gap(2);
                    for (int index = 0; index < ledges; index++) {
                        int length = hard > .6f ? 2 + random.nextInt(2) : 3 + random.nextInt(2);
                        row = Math.max(floor - 4, Math.min(floor - 1, row + random.nextInt(3) - 1 - (index == 0 ? 0 : 0)));
                        ledge(at, row, length);
                        pluses(at, at + length - 1, row - 2);
                        gap(length);
                        gap(2);
                    }
                    break;
                }
                case HIGH_ROAD: {
                    // Ground all the way, and a way up over it that has more to collect.
                    ground(9);
                    ledge(from + 1, floor - 2, 3);
                    ledge(from + 5, floor - 4, 3);
                    pluses(from + 1, from + 3, floor - 4);
                    pluses(from + 5, from + 7, floor - 6);
                    level.nooks.add(new int[]{from + 6, floor - 5});
                    if (hard > .15f) foe(walker(), from + 6);
                    break;
                }
                case SPIKES: {
                    int wide = 1 + random.nextInt(hard > .4f ? 3 : 2);
                    ground(wide);
                    for (int column = from; column < from + wide; column++) level.land[floor - 1][column] = SPIKE;
                    pluses(from, from + wide - 1, floor - 4);
                    break;
                }
                case SPRING_WALL: {
                    // A spring, and a wall only a spring will get her over.
                    ground(7);
                    level.land[floor - 1][from + 1] = SPRING;
                    for (int column = from + 3; column <= from + 4; column++) pile(column, 4);
                    pluses(from + 3, from + 4, floor - 7);
                    pluses(from + 1, from + 1, floor - 5);
                    break;
                }
                case PEN: {
                    // A stretch walled in at both ends by a block, with something walking about inside.
                    int inside = 5 + random.nextInt(2);
                    ground(inside + 2);
                    pile(from, 1);
                    pile(from + inside + 1, 1);
                    foe(walker(), from + 2 + random.nextInt(inside - 2));
                    pluses(from + 2, from + inside - 1, floor - 1);
                    break;
                }
                case HOPPER: {
                    ground(5);
                    level.foes.add(new int[]{DIVIDE, from + 2, floor - 1});
                    pluses(from + 1, from + 3, floor - 5);
                    break;
                }
                case PILLARS: {
                    // Single blocks standing in a gap: each a jump from the last.
                    int pillars = 2 + (hard > .7f ? 1 : 0);
                    gap(2);
                    for (int index = 0; index < pillars; index++) {
                        int row = floor - random.nextInt(2);
                        for (int down = row; down < ROWS; down++) level.land[down][at] = BLOCK;
                        pluses(at, at, row - 3);
                        gap(1);
                        gap(2);
                    }
                    break;
                }
                case PATROL: {
                    // A times walking a stretch, and a ledge to go over it by.
                    ground(8);
                    pile(from, 1);
                    pile(from + 7, 1);
                    foe(TIMES, from + 4);
                    ledge(from + 2, floor - 3, 4);
                    pluses(from + 2, from + 5, floor - 5);
                    break;
                }
                default: {
                    int wide = 5 + random.nextInt(3);
                    ground(wide);
                    pluses(from + 1, from + wide - 2, random.nextBoolean() ? floor - 1 : floor - 3);
                    if (hard > .03f && random.nextInt(3) != 0) foe(MINUS, from + wide - 2);
                    break;
                }
            }
        }

        /**
         * Where someone waits: a wide floor with somewhere to stand above it on either side
         * and, for most of them, higher in the middle. Its door is open until she is in; then
         * it shuts behind her, as the gate beyond is shut, until they are dealt with.
         */
        void arena(int who) {
            int from = at;
            level.arena = from;
            // A flag at its door, so that a hurt in there does not send her back through half the land.
            level.land[floor - 1][from - 3] = FLAG;
            level.flags.add(from - 3);
            ground(22);
            boolean heavy = who == ZERO || who == KING;
            ledge(from + 4, floor - 2, heavy ? 3 : 4);
            ledge(from + (heavy ? 15 : 14), floor - 2, heavy ? 3 : 4);
            if (who != YEOBAEK) ledge(from + 9, floor - 4, 4);
            if (who == MIRI) {
                // The twins: Miri up on a ledge, where she can see, and Gari on the floor with her brush.
                level.foes.add(new int[]{MIRI, from + 16, floor - 3});
                level.foes.add(new int[]{GARI, from + 11, floor - 1});
            } else {
                level.foes.add(new int[]{who, from + 14, floor - 1});
            }
            level.guarded = true;
        }
    }
}
