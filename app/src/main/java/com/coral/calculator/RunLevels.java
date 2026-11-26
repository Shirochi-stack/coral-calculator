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

    static boolean boss(int kind) { return kind == ZERO || kind == ILLYA || kind == TSUBAKIEH || kind >= GOKHAE; }

    /** Who waits at the end of a land, or -1: for the twins, Miri. */
    static int bossOf(int number) {
        if (number > COUNT) return number % IN_LATER_CHAPTER != 0 ? -1 : new int[]{GOKHAE, MIRI, YEOBAEK, KING, MAWANG}[(number - COUNT) / IN_LATER_CHAPTER - 1];
        return number == ILLYA_LAND ? ILLYA : number == TSUBAKIEH_LAND ? TSUBAKIEH : number == COUNT ? ZERO : -1;
    }

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
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    };

    private static final int PLAIN = 0, GAP = 1, STEPS = 2, WALL = 3, BRIDGE = 4, HIGH_ROAD = 5, SPIKES = 6, SPRING_WALL = 7,
            PEN = 8, HOPPER = 9, PILLARS = 10, PATROL = 11;
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
            1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 2, 1, 1, 1, 1,
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
        /** Where the place begins in which someone waits, or -1. */
        int arena = -1;
        int gate;
        /** Whether the gate stays shut until Zero has been dealt with. */
        boolean guarded;

        Level(int number, int columns) {
            this.number = number;
            this.chapter = chapterOf(number);
            this.columns = columns;
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
        int[] pool = LATER_POOLS[chapter - CHAPTERS];
        for (int stretch = 0; stretch < stretches; stretch++) {
            int kind;
            do kind = pool[random.nextInt(pool.length)];
            while (kind == last);
            last = kind;
            builder.stretch(kind);
            if (stretch == stretches / 4 || stretch == stretches / 2 || stretch == stretches * 3 / 4) builder.flag();
            builder.rest();
        }
        if (boss >= 0) builder.arena(boss);
        builder.ground(3);
        draft.gate = builder.at;
        builder.ground(6);
        Level level = new Level(number, builder.at);
        for (int row = 0; row < ROWS; row++) System.arraycopy(draft.land[row], 0, level.land[row], 0, level.columns);
        level.land[FLOOR - 1][draft.gate] = GATE;
        level.land[FLOOR - 2][draft.gate] = GATE;
        level.pluses.addAll(draft.pluses);
        level.foes.addAll(draft.foes);
        level.flags.addAll(draft.flags);
        level.movers.addAll(draft.movers);
        level.doors.addAll(draft.doors);
        level.gate = draft.gate;
        level.arena = draft.arena;
        level.guarded = draft.guarded;
        armLater(level, new Random(seed * 104729 + number * 31L));
        return level;
    }

    /** The flat, empty columns of a land where something may be laid down: nothing on or over them, and nothing near. */
    private static List<Integer> free(Level level, int until) {
        List<Integer> free = new ArrayList<>();
        for (int column = 12; column < until; column++) {
            boolean clear = true;
            for (int beside = column - 1; beside <= column + 1 && clear; beside++) {
                clear = level.land[FLOOR][beside] == BLOCK && level.land[FLOOR - 1][beside] == AIR && level.land[FLOOR - 2][beside] == AIR
                        && level.land[FLOOR - 3][beside] == AIR;
            }
            for (int[] foe : level.foes) if (Math.abs(foe[1] - column) < 4) clear = false;
            for (int[] plus : level.pluses) if (plus[0] == column && plus[1] >= FLOOR - 3) clear = false;
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
            level.weapons.add(new int[]{index == 2 && random.nextBoolean() ? PlusRun.GUN : (number + index) % PlusRun.GUN, flag + 1, FLOOR - 1});
            int gives = index == 0 ? PlusRun.BREAD : index == 1 ? PlusRun.SHIELD : number % 2 == 0 ? PlusRun.HOMERUN : PlusRun.COMPLIMENT;
            level.items.add(new int[]{gives, flag + 3, FLOOR - 1, index == 0 ? 1 : index == 1 ? 2 : number % 2 == 0 ? 3 : 4});
        }
        for (int[] foe : level.foes) {
            if (foe[0] == MINUS && random.nextBoolean()) foe[0] = THROWER;
            else if (foe[0] == DIVIDE && random.nextBoolean()) foe[0] = GUNNER;
        }
        if (waits >= 0) {
            // Before whoever waits lies what will do against her; and the Mawang's visitor has her friends beside her in there.
            level.weapons.add(new int[]{who == MIRI ? PlusRun.GUN : who == GOKHAE ? PlusRun.PEN : PlusRun.SWORD, waits - 2, FLOOR - 1});
            if (who == MAWANG) {
                level.items.add(new int[]{PlusRun.BREAD, waits + 2, FLOOR - 1, 1});
                level.items.add(new int[]{PlusRun.SHIELD, waits + 5, FLOOR - 1, 2});
            }
        }
        // A ribbon for the lava or the water, two columns before it, two times in three.
        for (int column = 14; column < level.gate; column++) {
            char here = level.land[FLOOR][column];
            if ((here == LAVA || here == WATER) && level.land[FLOOR][column - 1] == BLOCK && random.nextInt(3) != 0) {
                level.items.add(new int[]{PlusRun.FORM + (here == LAVA ? PlusRun.RED : PlusRun.BLUE), column - 2, FLOOR - 1, 0});
            }
        }
        List<Integer> free = free(level, waits >= 0 ? waits - 4 : level.gate - 6);
        int[] ribbons = {PlusRun.BROWN, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.PURPLE, PlusRun.BROWN, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.PURPLE,
                PlusRun.BLACK, PlusRun.BLACK, PlusRun.WHITE};
        int[] treats = {PlusRun.ICE, PlusRun.MAGNET, PlusRun.NOTES, PlusRun.MAGNET};
        for (int count = 0; count < 4 && !free.isEmpty(); count++) {
            int column = take(free, random);
            if (count == 3) {
                level.items.add(new int[]{treats[random.nextInt(treats.length)], column, FLOOR - 1, 0});
                continue;
            }
            int ribbon = ribbons[random.nextInt(ribbons.length)];
            // The black one hangs where only a jump will reach it: it is not to be walked into.
            level.items.add(new int[]{PlusRun.FORM + ribbon, column, ribbon == PlusRun.BLACK ? FLOOR - 3 : FLOOR - 1, 0});
        }
    }

    /** The land of a number as some other number would make it: for choosing the numbers kept. */
    static Level level(int number, long seed) {
        if (number > COUNT) return later(number, seed);
        Random random = new Random(seed * 7919 + number);
        int chapter = chapterOf(number);
        float hard = (number - 1) / (float) (COUNT - 1);
        // Worked out on a strip longer than any land needs, then cut to length.
        Level draft = new Level(number, 400);
        Builder builder = new Builder(draft, random, hard, chapter);
        builder.ground(6);
        int boss = bossOf(number);
        int stretches = boss >= 0 ? 6 : 6 + number / 3;
        int last = -1;
        for (int stretch = 0; stretch < stretches; stretch++) {
            int[] pool = POOLS[chapter];
            int kind;
            do kind = pool[random.nextInt(pool.length)];
            while (kind == last || (number <= 2 && (kind == BRIDGE || kind == HIGH_ROAD)));
            last = kind;
            builder.stretch(kind);
            // Somewhere to stand between one stretch and the next, with a flag on it a third and two thirds of the way.
            if (stretches >= 6 && (stretch == stretches / 3 || stretch == stretches * 2 / 3)) builder.flag();
            builder.rest();
        }
        if (boss >= 0) builder.arena(boss);
        builder.ground(3);
        draft.gate = builder.at;
        builder.ground(6);
        Level level = new Level(number, builder.at);
        for (int row = 0; row < ROWS; row++) System.arraycopy(draft.land[row], 0, level.land[row], 0, level.columns);
        level.land[FLOOR - 1][draft.gate] = GATE;
        level.land[FLOOR - 2][draft.gate] = GATE;
        level.pluses.addAll(draft.pluses);
        level.foes.addAll(draft.foes);
        level.flags.addAll(draft.flags);
        level.gate = draft.gate;
        level.guarded = draft.guarded;
        arm(level, new Random(seed * 104729 + number * 31L));
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
            level.weapons.add(new int[]{gun ? PlusRun.GUN : (number + index) % PlusRun.GUN, level.flags.get(index) + 1, FLOOR - 1});
        }
        int waits = -1, who = -1;
        for (int[] foe : level.foes) {
            if (foe[0] == MINUS && chapter >= 1 && random.nextInt(chapter >= 3 ? 2 : 3) == 0) {
                foe[0] = THROWER;
            } else if (foe[0] == DIVIDE && random.nextBoolean()) {
                foe[0] = GUNNER;
            } else if (boss(foe[0])) {
                who = foe[0];
                waits = foe[1] - (who == ILLYA ? 13 : 11);
            }
        }
        // Before the place where one of them waits lies what will do against them: something to throw at Illya, who
        // keeps her distance, and something to swing at the other two, who send back or swallow what is thrown.
        if (waits >= 0) level.weapons.add(new int[]{who == ILLYA ? PlusRun.PEN : PlusRun.SWORD, waits - 2, FLOOR - 1});
        // And before Zero, the notes of the one who came this way alone.
        if (who == ZERO) level.items.add(new int[]{PlusRun.NOTES, waits - 1, FLOOR - 1, 0});
        // Her friends wait by the flags once they have joined her: Chikuwa with bread by the first, from the third land,
        // and Rune with a ball of her ink by the second, from the ninth.
        for (int index = 0; index < level.flags.size(); index++) {
            if (index == 0 && number >= 3) level.items.add(new int[]{PlusRun.BREAD, level.flags.get(index) + 3, FLOOR - 1, 1});
            if (index == 1 && number >= 9) level.items.add(new int[]{PlusRun.SHIELD, level.flags.get(index) + 3, FLOOR - 1, 2});
        }
        // Here and there on flat, empty ground lies something else to help: ice cream from the fourth land,
        // and from the second chapter a magnet or, now and then, a page of notes.
        List<Integer> free = new ArrayList<>();
        for (int column = 12; column < (waits >= 0 ? waits - 4 : level.gate - 6); column++) {
            boolean clear = true;
            for (int beside = column - 1; beside <= column + 1 && clear; beside++) {
                clear = level.land[FLOOR][beside] == BLOCK && level.land[FLOOR - 1][beside] == AIR && level.land[FLOOR - 2][beside] == AIR;
            }
            for (int[] foe : level.foes) if (Math.abs(foe[1] - column) < 4) clear = false;
            for (int[] plus : level.pluses) if (plus[0] == column && plus[1] >= FLOOR - 2) clear = false;
            for (int[] weapon : level.weapons) if (Math.abs(weapon[1] - column) < 2) clear = false;
            for (int[] item : level.items) if (Math.abs(item[1] - column) < 2) clear = false;
            if (clear) free.add(column);
        }
        int wanted = number < 4 ? 0 : chapter == 0 ? 1 : 2;
        for (int count = 0; count < wanted && !free.isEmpty(); count++) {
            int column = free.remove(random.nextInt(free.size()));
            int kind = chapter == 0 || count == 0 && random.nextBoolean() ? PlusRun.ICE : random.nextInt(4) == 0 ? PlusRun.NOTES : PlusRun.MAGNET;
            level.items.add(new int[]{kind, column, FLOOR - 1, 0});
            for (int index = free.size() - 1; index >= 0; index--) if (Math.abs(free.get(index) - column) < 6) free.remove(index);
        }
        // And from the eighth land on a ribbon, brown or purple by turns: the two that change nothing of how she moves.
        if (number >= 8 && !free.isEmpty()) {
            int column = free.get(new Random(number * 977L).nextInt(free.size()));
            level.items.add(new int[]{PlusRun.FORM + (number % 2 == 0 ? PlusRun.BROWN : PlusRun.PURPLE), column, FLOOR - 1, 0});
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

        Builder(Level level, Random random, float hard, int chapter) {
            this.level = level;
            this.random = random;
            this.hard = hard;
            this.chapter = chapter;
        }

        /** Flat ground for some columns. */
        void ground(int columns) {
            for (int column = at; column < at + columns; column++) {
                for (int row = FLOOR; row < ROWS; row++) level.land[row][column] = BLOCK;
            }
            at += columns;
        }

        void gap(int columns) { at += columns; }

        /** A pile of blocks standing on the ground at a column. */
        void pile(int column, int high) {
            for (int up = 1; up <= high; up++) level.land[FLOOR - up][column] = BLOCK;
        }

        void ledge(int column, int row, int length) {
            for (int along = 0; along < length; along++) level.land[row][column + along] = LEDGE;
        }

        void pluses(int from, int to, int row) {
            for (int column = from; column <= to; column++) level.pluses.add(new int[]{column, row});
        }

        void foe(int kind, int column) { level.foes.add(new int[]{kind, column, FLOOR - 1}); }

        /** The sign that walks about in this chapter: a minus at first, a times as well from the third. */
        int walker() { return chapter >= 2 && random.nextInt(3) == 0 ? TIMES : MINUS; }

        /** A few columns of flat ground between two stretches. */
        void rest() {
            int from = at;
            ground(3 + random.nextInt(2));
            if (random.nextBoolean()) pluses(from + 1, from + 2, FLOOR - 1);
        }

        void flag() {
            int from = at;
            ground(3);
            level.land[FLOOR - 1][from + 1] = FLAG;
            level.flags.add(from + 1);
        }

        /** Fills a pit, some columns of nothing, with lava or with water, from the level of the ground down. */
        void lake(int from, int wide, char with) {
            for (int column = from; column < from + wide; column++) {
                for (int row = FLOOR; row < ROWS; row++) level.land[row][column] = with;
            }
        }

        void put(int column, int row, int length, char what) {
            for (int along = 0; along < length; along++) level.land[row][column + along] = what;
        }

        void stretch(int kind) {
            int from = at;
            switch (kind) {
                case THINS: {
                    // A long gap, and two thin ledges over it to go by.
                    int rise = random.nextInt(2);
                    gap(10);
                    put(from + 2, FLOOR - 1, 2, THIN);
                    put(from + 6, FLOOR - 2 - rise, 2, THIN);
                    pluses(from + 2, from + 3, FLOOR - 3);
                    pluses(from + 6, from + 7, FLOOR - 4 - rise);
                    break;
                }
                case CRUMBLES: {
                    // A gap bridged by ledges that will not bear standing on for long.
                    int wide = 5 + random.nextInt(3);
                    gap(wide);
                    put(from, FLOOR, wide, CRUMBLE);
                    pluses(from + 1, from + wide - 2, FLOOR - 1);
                    break;
                }
                case BELTS: {
                    // A belt: against her, or with her and into spikes.
                    boolean with = random.nextBoolean();
                    ground(9);
                    put(from + 1, FLOOR, 6, with ? BELT_RIGHT : BELT_LEFT);
                    if (with) level.land[FLOOR - 1][from + 7] = SPIKE;
                    pluses(from + 2, from + 5, FLOOR - (with ? 3 : 1));
                    break;
                }
                case SLICKS: {
                    // Slime underfoot, and spikes in the middle of it.
                    ground(10);
                    put(from + 1, FLOOR, 8, SLICK);
                    level.land[FLOOR - 1][from + 5] = SPIKE;
                    pluses(from + 2, from + 3, FLOOR - 1);
                    pluses(from + 5, from + 5, FLOOR - 4);
                    pluses(from + 7, from + 8, FLOOR - 1);
                    break;
                }
                case DRAUGHTS: {
                    // A gap too wide to jump, with a draught coming up it that carries her over.
                    int wide = 6 + random.nextInt(2);
                    gap(wide);
                    for (int column = from; column < from + wide; column++) {
                        for (int row = FLOOR - 6; row < ROWS; row++) level.land[row][column] = DRAUGHT;
                    }
                    pluses(from + 1, from + wide - 2, FLOOR - 6);
                    break;
                }
                case MOVERS: {
                    // A gap too wide to jump, and a ledge that goes to and fro across it.
                    gap(7);
                    level.movers.add(new int[]{from, FLOOR, 2, 5, 0, 44 + random.nextInt(3) * 6});
                    pluses(from + 2, from + 4, FLOOR - 3);
                    break;
                }
                case LIFT: {
                    // A wall too high to jump, and a ledge that goes up and down beside it.
                    ground(1);
                    gap(2);
                    level.movers.add(new int[]{from + 1, FLOOR, 2, 5, 1, 50});
                    ground(4);
                    pile(from + 3, 5);
                    pile(from + 4, 5);
                    pluses(from + 3, from + 4, FLOOR - 7);
                    pluses(from + 1, from + 2, FLOOR - 4);
                    break;
                }
                case DOORS: {
                    // A wall far too high to jump, with a bookmark door on each side of it.
                    ground(9);
                    pile(from + 3, 6);
                    pile(from + 4, 6);
                    level.land[FLOOR - 1][from + 1] = DOOR;
                    level.land[FLOOR - 1][from + 6] = DOOR;
                    level.doors.add(new int[]{from + 1, from + 6});
                    pluses(from + 7, from + 8, FLOOR - 1);
                    break;
                }
                case LAVAS:
                case WATERS: {
                    // A pit of lava or of water, with thin ledges over it; and pluses just over its top, for whoever can walk on it.
                    gap(8);
                    lake(from, 8, kind == LAVAS ? LAVA : WATER);
                    put(from + 1, FLOOR - 2, 2, THIN);
                    put(from + 5, FLOOR - 2, 2, THIN);
                    pluses(from + 1, from + 6, FLOOR - 1);
                    pluses(from + 3, from + 4, FLOOR - 5);
                    break;
                }
                case SLIMES: {
                    ground(9);
                    foe(SLIME, from + 4);
                    foe(SLIME, from + 7);
                    pluses(from + 1, from + 2, FLOOR - 1);
                    break;
                }
                case DRAGONS: {
                    ground(10);
                    foe(DRAGON, from + 5);
                    pluses(from + 3, from + 7, FLOOR - 1);
                    break;
                }
                case BARS: {
                    ground(11);
                    pile(from, 1);
                    pile(from + 10, 1);
                    foe(BAR, from + 8);
                    pluses(from + 2, from + 5, FLOOR - 3);
                    break;
                }
                case FANS: {
                    ground(9);
                    pile(from, 1);
                    pile(from + 8, 1);
                    foe(FAN, from + 6);
                    pluses(from + 2, from + 6, FLOOR - 1);
                    break;
                }
                case WORDS: {
                    // The queen's words hang over the way, with something under each to tempt her to stop.
                    ground(10);
                    foe(WORD, from + 3);
                    foe(WORD, from + 7);
                    pluses(from + 3, from + 3, FLOOR - 1);
                    pluses(from + 7, from + 7, FLOOR - 1);
                    break;
                }
                case POGOS: {
                    ground(10);
                    pile(from, 1);
                    pile(from + 9, 1);
                    foe(POGO, from + 5);
                    pluses(from + 3, from + 6, FLOOR - 5);
                    break;
                }
                case GAP: {
                    int wide = 2 + (hard > .35f && random.nextBoolean() ? 1 : 0);
                    gap(wide);
                    pluses(from, from + wide - 1, FLOOR - 3);
                    break;
                }
                case STEPS: {
                    // Up a step at a time and off the far end.
                    int top = hard > .45f && random.nextBoolean() ? 3 : 2;
                    ground(top + 1);
                    for (int step = 1; step <= top; step++) pile(from + step, step);
                    pluses(from + top, from + top, FLOOR - top - 2);
                    break;
                }
                case WALL: {
                    ground(4);
                    pile(from + 1, 2);
                    pile(from + 2, 2);
                    pluses(from + 1, from + 2, FLOOR - 4);
                    break;
                }
                case BRIDGE: {
                    // Ledges over a long gap, each a short jump from the last and no more than two squares higher.
                    int ledges = 2 + (hard > .5f ? 1 : 0), row = FLOOR - 1;
                    gap(2);
                    for (int index = 0; index < ledges; index++) {
                        int length = hard > .6f ? 2 + random.nextInt(2) : 3 + random.nextInt(2);
                        row = Math.max(FLOOR - 4, Math.min(FLOOR - 1, row + random.nextInt(3) - 1 - (index == 0 ? 0 : 0)));
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
                    ledge(from + 1, FLOOR - 2, 3);
                    ledge(from + 5, FLOOR - 4, 3);
                    pluses(from + 1, from + 3, FLOOR - 4);
                    pluses(from + 5, from + 7, FLOOR - 6);
                    if (hard > .15f) foe(walker(), from + 6);
                    break;
                }
                case SPIKES: {
                    int wide = 1 + random.nextInt(hard > .4f ? 3 : 2);
                    ground(wide);
                    for (int column = from; column < from + wide; column++) level.land[FLOOR - 1][column] = SPIKE;
                    pluses(from, from + wide - 1, FLOOR - 4);
                    break;
                }
                case SPRING_WALL: {
                    // A spring, and a wall only a spring will get her over.
                    ground(7);
                    level.land[FLOOR - 1][from + 1] = SPRING;
                    for (int column = from + 3; column <= from + 4; column++) pile(column, 4);
                    pluses(from + 3, from + 4, FLOOR - 7);
                    pluses(from + 1, from + 1, FLOOR - 5);
                    break;
                }
                case PEN: {
                    // A stretch walled in at both ends by a block, with something walking about inside.
                    int inside = 5 + random.nextInt(2);
                    ground(inside + 2);
                    pile(from, 1);
                    pile(from + inside + 1, 1);
                    foe(walker(), from + 2 + random.nextInt(inside - 2));
                    pluses(from + 2, from + inside - 1, FLOOR - 1);
                    break;
                }
                case HOPPER: {
                    ground(5);
                    level.foes.add(new int[]{DIVIDE, from + 2, FLOOR - 1});
                    pluses(from + 1, from + 3, FLOOR - 5);
                    break;
                }
                case PILLARS: {
                    // Single blocks standing in a gap: each a jump from the last.
                    int pillars = 2 + (hard > .7f ? 1 : 0);
                    gap(2);
                    for (int index = 0; index < pillars; index++) {
                        int row = FLOOR - random.nextInt(2);
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
                    ledge(from + 2, FLOOR - 3, 4);
                    pluses(from + 2, from + 5, FLOOR - 5);
                    break;
                }
                default: {
                    int wide = 5 + random.nextInt(3);
                    ground(wide);
                    pluses(from + 1, from + wide - 2, random.nextBoolean() ? FLOOR - 1 : FLOOR - 3);
                    if (hard > .03f && random.nextInt(3) != 0) foe(MINUS, from + wide - 2);
                    break;
                }
            }
        }

        /** Where Zero, Illya or Tsubakieh waits: a long floor walled at both ends, and the gate beyond it shut until they are dealt with. */
        void arena(int who) {
            int from = at;
            level.arena = from;
            // A flag at its door, so that a hurt in there does not send her back through half the land.
            level.land[FLOOR - 1][from - 3] = FLAG;
            level.flags.add(from - 3);
            ground(18);
            pile(from, 2);
            pile(from + 17, 2);
            if (who == MIRI) {
                // The twins: Miri up on a pile at the far end, where she can see, and Gari on the floor with her brush.
                pile(from + 14, 2);
                level.foes.add(new int[]{MIRI, from + 14, FLOOR - 3});
                level.foes.add(new int[]{GARI, from + 9, FLOOR - 1});
                level.guarded = true;
                return;
            }
            level.foes.add(new int[]{who, from + (who == ILLYA || who == GOKHAE ? 13 : 11), FLOOR - 1});
            level.guarded = true;
        }
    }
}
