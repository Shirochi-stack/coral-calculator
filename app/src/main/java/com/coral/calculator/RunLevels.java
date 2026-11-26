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
    static final int ROWS = 18;
    /** The row the ground's top squares are in. */
    static final int FLOOR = 15;

    static final char AIR = ' ', BLOCK = '#', LEDGE = '=', SPIKE = '^', SPRING = 'S', FLAG = 'f', GATE = 'G';
    /** The signs that get in her way: a minus walks, a times walks and cannot be landed on, a divide hops, and Zero is the last of them. */
    static final int MINUS = 0, TIMES = 1, DIVIDE = 2, ZERO = 3;
    /** And two that fight back from where they are: a minus that throws knives, and a divide that shoots its dots. */
    static final int THROWER = 4, GUNNER = 5;
    /** And two who are not signs at all: Illya, who floats and fires hearts, and Tsubakieh with her bat. Like Zero, each keeps a gate shut. */
    static final int ILLYA = 6, TSUBAKIEH = 7;
    /** The lands at whose end one of them waits. */
    static final int ILLYA_LAND = 18, TSUBAKIEH_LAND = 24;

    static boolean boss(int kind) { return kind == ZERO || kind == ILLYA || kind == TSUBAKIEH; }

    /** Who waits at the end of a land, or -1. */
    static int bossOf(int number) { return number == ILLYA_LAND ? ILLYA : number == TSUBAKIEH_LAND ? TSUBAKIEH : number == COUNT ? ZERO : -1; }

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
    static int chapterOf(int number) { return (number - 1) / IN_CHAPTER; }

    /** The land of a number from 1 to {@link #COUNT}, made from the number kept for it. */
    static Level level(int number) { return level(number, SEEDS[number - 1]); }

    /** The land of a number as some other number would make it: for choosing the numbers kept. */
    static Level level(int number, long seed) {
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

        void stretch(int kind) {
            int from = at;
            switch (kind) {
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
            // A flag at its door, so that a hurt in there does not send her back through half the land.
            level.land[FLOOR - 1][from - 3] = FLAG;
            level.flags.add(from - 3);
            ground(18);
            pile(from, 2);
            pile(from + 17, 2);
            level.foes.add(new int[]{who, from + (who == ILLYA ? 13 : 11), FLOOR - 1});
            level.guarded = true;
        }
    }
}
