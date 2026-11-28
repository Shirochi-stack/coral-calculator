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
    static final int TOTAL = 100, ALL_CHAPTERS = 12, IN_LATER_CHAPTER = 10;
    /**
     * The third volume: twenty lands in two chapters, the Unread Shore and the Dragon's
     * Library, made as the lands of the second are, of everything the second had, and
     * longer. No one waits at the end of any of them but the last; in each of the others
     * one of those she has met stands, to pledge her help against what is coming.
     */
    static final int SECOND = 80, FRIENDS = 19;
    /** How far the lands are open: to the last but one, until whoever waits at the end of the hundredth is there. */
    static final int OPEN_TO = 100;

    /** Which of her friends stands in a land of the third volume to pledge her help, from nought; or none. */
    static int friendOf(int number) { return number > SECOND && number < TOTAL ? number - SECOND - 1 : -1; }

    /**
     * The friend of a land stands a little past its second flag, on flat ground where
     * nothing else stands or lies. She is put in last of all: the land under her is the land
     * it was.
     */
    private static void friend(Level level) {
        int friend = friendOf(level.number);
        if (friend < 0 || level.flags.size() < 2) return;
        int flag = level.flags.get(1);
        // (Clear of creatures by two squares, if there is such a place; if not, anywhere that none stands.)
        for (int clear = 2; clear >= 0; clear -= 2) {
            for (int column = flag + 3; column < level.gate - 6; column++) {
                int floor = top(level, column);
                boolean free = floor > 4 && level.land[floor - 1][column] == AIR && level.land[floor - 2][column] == AIR && level.land[floor - 3][column] == AIR
                        && top(level, column - 1) == floor && top(level, column + 1) == floor;
                for (int[] weapon : level.weapons) free &= Math.abs(weapon[1] - column) > 1;
                for (int[] item : level.items) if (item[1] < level.main) free &= Math.abs(item[1] - column) > 2;
                for (int[] thing : level.decor) free &= Math.abs(thing[1] - column) > 6;
                // (Nor at a door to a place off the land, seen or hidden.)
                for (int[] room : level.rooms) free &= Math.abs(room[1] - column) > 4 && Math.abs(room[5] - column) > 4;
                for (int[] foe : level.foes) if (foe[1] < level.main && foe[2] == floor - 1) free &= Math.abs(foe[1] - column) > clear;
                if (!free) continue;
                level.items.add(new int[]{PlusRun.PLEDGE, column, floor - 1, 0, friend});
                return;
            }
        }
    }
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
    /** And a hideout, where a hidden boss waits: behind a crack that only a strong blow breaks, or up on a ledge out of reach of a jump. */
    static final int HIDEOUT = 8;
    /** And a lagoon: a hall full of deep water, swum in with the blue ribbon (one lies by its door), with creatures that swim and a chest at the bottom. */
    static final int LAGOON = 9;
    /** The first land that may have a lagoon; and which do: one land in four, and every land of the Unread Shore. */
    static final int LAGOONS_FROM = 10;

    static boolean lagoonIn(int number) {
        if (number < LAGOONS_FROM || number >= OPEN_TO) return false;
        return chapterOf(number) == 10 || number % 4 == 1;
    }
    /** The first land that may have a hideout. */
    static final int HIDEOUTS_FROM = 8;

    /** Whether a land has a hideout: one in three from the eighth on, which ones by the land's own numbers; and how it is reached. */
    static boolean hideoutIn(int number) {
        if (number < HIDEOUTS_FROM || number >= OPEN_TO) return false;
        long dice = number * 2654435761L + 97;
        return (dice >>> 7) % 3 == 0;
    }

    /** A hideout up out of the way (true), or behind a strong crack (false). */
    static boolean hideoutHigh(int number) { return (number * 7 + 3) % 2 == 0; }
    /** And a range, where paper targets hang in the air and an ink gun that does not run dry is put in her hands for twelve seconds. */
    static final int RANGE = 4;
    /** And a room where signs keep coming for a quarter of a minute, to be dealt with as fast as they come. */
    static final int TRAMPLE = 5;
    /**
     * And the buildings of a village, each of which can be gone into: a pet shop, where
     * the ten pets sit to be looked at and bought; the guild hall, where the counter is; a
     * home, with someone in who has something for her; and, in time, a smithy and a black
     * market. Nothing in a building hurts her. A village has two of them, of different
     * sorts, by the land and by which of the land's villages it is.
     */
    static final int HOUSE = 6, PET_SHOP = 0, GUILD_HALL = 1, HOME = 2, SMITHY = 3, MARKET = 4, GACHA = 5, BOUTIQUE = 7;
    /** The chapter whose village, Rosemarrow, has Tulle and Thread, the vanity boutique, among its buildings. */
    static final int BOUTIQUE_CHAPTER = 2;

    /**
     * Every land has a level, one more with every five lands. The further on, the tougher
     * its creatures: those of a land take more blows than those of the first lands, and a
     * hurt costs more of a heart. What the smith makes is what answers it: a thing to fight
     * with worked on counts for more blows, and what she wears takes some of a hurt off.
     * Those who wait at the end of a land and those met in the middle of one are as they
     * were; and so is a land looked at with none of it, with skill enough.
     */
    static int levelOf(int number) { return number < 1 ? 1 : 1 + (number - 1) / 5; }

    /**
     * How many more blows a creature of a land takes than one of the first lands; and how many more halves of a heart a hurt costs
     * there. By its level, through the first two volumes. The third is harder than its levels alone would make it: its creatures take
     * five blows throughout; and a hurt costs two hearts and a half in its first chapter, and three in its last.
     */
    static int toughOf(int number) { return number > SECOND ? 4 : (levelOf(number) - 1) / 5; }
    static int biteOf(int number) { return number > SECOND + IN_LATER_CHAPTER ? 4 : number > SECOND ? 3 : (levelOf(number) - 1) / 6; }

    /**
     * The villages: one in each chapter, in its first land, and nowhere else. Each has its own name and its own landmark, and
     * every building there is by then: the guild hall, the pet shop, the smithy, a home, and from their lands on the ribbon
     * gacha and the black market. Where the other lands had villages there is a homestead: one house, with whoever lives in it
     * at her door, the stall, and a well or a paper tree.
     */
    static final String[] VILLAGES = {"Contents Village", "Hushwick", "Rosemarrow", "Cairn Index", "Endleaf", "Gildcrest", "Inkwell Row", "Margin Ford", "Goopvale",
            "Footnote Hollow", "Dog-Ear Bay", "Colophon"};

    /** The first land of a chapter. */
    static int firstOf(int chapter) { return chapter < CHAPTERS ? chapter * IN_CHAPTER + 1 : COUNT + (chapter - CHAPTERS) * IN_LATER_CHAPTER + 1; }

    /** Whether a land has its chapter's village. */
    static boolean villageLand(int number) { return number == firstOf(chapterOf(number)); }

    /** The sorts of the buildings of a chapter's village, in the order they stand along it. */
    static int[] townOf(int number) {
        List<Integer> sorts = new ArrayList<>(java.util.Arrays.asList(GUILD_HALL, PET_SHOP, SMITHY, HOME));
        if (number >= GACHA_FROM) sorts.add(GACHA);
        if (number >= MARKET_FROM) sorts.add(MARKET);
        if (chapterOf(number) == BOUTIQUE_CHAPTER) sorts.add(BOUTIQUE);
        int[] out = new int[sorts.size()];
        for (int index = 0; index < out.length; index++) out[index] = sorts.get(index);
        return out;
    }

    /** How long a village is, with so many buildings in it. */
    static int townWide(int buildings) { return 12 + buildings * TOWN_GAP + 8; }

    static final int TOWN_GAP = 9;

    /** The insides of homes: six of them, after the buildings' own; which one a home has goes by the land and which room it is. */
    static final int HOME_LOOKS = 6, HOME_LOOK = 14;
    /** Those who may live in a home, from Somi to Moru; and whoever lives in a home (from Somi), or -1 for a room that is not one. */
    static final int OWNERS = 8;

    static int ownerOf(int[] room) { return room.length > 15 && room[0] == HOUSE ? room[15] : -1; }

    /** The diary pages there are to find, one at a time, in the homes; and what a home's secret is, if it has one: 0 a hidden room, 1 a loose floorboard, 2 a diary page out on its table, -1 none. */
    static int homeSecret(int number, int room) { return Math.floorMod(number * 5 + room * 3, 4) - 1; }

    /** The sorts of the two buildings of a village that can be gone into. */
    static int[] buildingsOf(int number, int village) {
        int first = (number + village) % 4, second = (first + 1 + (number / 4 + village) % 3) % 4;
        // In one village in six, from the fifth land on, the second of them is the ribbon gacha.
        if (number >= GACHA_FROM && (number + village * 2) % 6 == 2) second = GACHA;
        // And in one village in five, from the ninth land on, the second of them is the black market.
        if (number >= MARKET_FROM && (number + village * 3) % 5 == 4) second = MARKET;
        return new int[]{first, second};
    }

    /**
     * The ribbon gacha: a machine that gives, for pluses, a ribbon of one of the colours it
     * holds, by chance, or now and then a black one. It is in some villages from the fifth
     * land on. The machines of the first twenty lands hold four colours, those of the next
     * twenty five, and the rest all six.
     */
    static final int GACHA_FROM = 5;

    static int[] machineOf(int number) {
        return number <= 20 ? new int[]{PlusRun.RED, PlusRun.BLUE, PlusRun.GREEN, PlusRun.YELLOW}
                : number <= 40 ? new int[]{PlusRun.RED, PlusRun.BLUE, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.BROWN}
                : new int[]{PlusRun.RED, PlusRun.BLUE, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.BROWN, PlusRun.PURPLE};
    }

    /**
     * From the forty-first land on, pots of black ink stand here and there in a land: each
     * swells, as a warning, and throws the black curse at whoever is near. More of them the
     * further on. They are put in last of all, with chances of their own, on flat ground
     * clear of everything else: the land under them is the land it was.
     */
    static final int POTS_FROM = 41;

    private static void pots(Level level) {
        int number = level.number;
        if (number < POTS_FROM) return;
        Random random = new Random(977L * number + 5);
        int until = (level.arena >= 0 ? level.arena : level.gate) - 12, want = 1 + (number - POTS_FROM) / 15, placed = 0;
        for (int tries = 0; tries < 400 && placed < want; tries++) {
            int column = 40 + random.nextInt(Math.max(1, until - 40)), floor = top(level, column);
            boolean free = floor > 4 && level.land[floor][column] == BLOCK && level.land[floor - 1][column] == AIR && level.land[floor - 2][column] == AIR
                    && top(level, column - 1) == floor && top(level, column + 1) == floor;
            for (int flag : level.flags) free &= Math.abs(flag - column) > 8;
            for (int[] thing : level.decor) free &= Math.abs(thing[1] - column) > 10;
            for (int[] room : level.rooms) free &= Math.abs(room[1] - column) > 5 && Math.abs(room[5] - column) > 5;
            for (int[] item : level.items) if (item[1] < level.main) free &= Math.abs(item[1] - column) > (item[0] == PlusRun.INKPOT ? 30 : 4);
            for (int[] weapon : level.weapons) free &= Math.abs(weapon[1] - column) > 3;
            // (It may stand among creatures: only not where one of them stands, nor near one met in the middle of the land.)
            for (int[] foe : level.foes) if (foe[1] < level.main && (rover(foe[0]) || foe[2] == floor - 1)) free &= Math.abs(foe[1] - column) > (rover(foe[0]) ? 14 : 0);
            if (!free) continue;
            level.items.add(new int[]{PlusRun.INKPOT, column, floor - 1, 0, random.nextInt(100)});
            placed++;
        }
    }

    /** The first land with a black market in it. */
    static final int MARKET_FROM = 9;

    /** The first land whose smithy is large enough for all three sisters: before it Moru keeps it alone, and does her sisters' work besides. */
    static final int SISTERS_FROM = 13;
    /** How tall a place off the land is, from its floor to its ceiling. */
    static final int ROOM_TALL = 13;

    /** How long the ground runs on past the gate of a land: to the way that leads on to the next. */
    static final int PAST_GATE = 11;
    /**
     * A cellar is no room but an arena: a hundred and twenty squares across and forty-three
     * from floor to ceiling, which is more than two screens either way. A land that has one
     * is that many rows deep where it lies, beyond the end of the land; the land itself is
     * in the top eighteen of them, as every land is.
     */
    static final int DEEP = 48, ARENA_WIDE = 120, ARENA_FLOOR = DEEP - 3, STOREY = 4, STOREYS = 8;
    /** Where in an arena something may come in: on its floor, on a platform, on a high perch, or in the open air. */
    static final int ON_FLOOR = 0, ON_PLATFORM = 1, ON_PERCH = 2, IN_AIR = 3;
    /** One of the last wave that is outsized is its kind and this much more. */
    static final int GIANT = 1000;
    /** How many times over the troop of a wave comes; and how many outsized ones the last wave ends on. */
    static final int SWELL = 20, GIANTS = 4;

    /**
     * Everyone who comes in a wave of a cellar, in the order they come: its troop twenty
     * times over, mixed as the troop is; and at the end of the last wave four outsized
     * ones, or, where the Old Book sleeps, three of them and then he.
     */
    static int[] host(int number, int wave, boolean den) {
        int[] troop = wave(number, wave, den), plain = wave(number, wave, false);
        List<Integer> all = new ArrayList<>();
        for (int time = 0; time < SWELL; time++) for (int kind : troop) if (kind < GIANT && kind != OLD_BOOK) all.add(kind);
        if (wave == 3) {
            for (int giant = 0; giant < (den ? GIANTS - 1 : GIANTS); giant++) all.add(plain[plain.length - 1]);
            if (den) all.add(OLD_BOOK);
        }
        int[] host = new int[all.size()];
        for (int index = 0; index < host.length; index++) host[index] = all.get(index);
        return host;
    }

    /**
     * Who comes in a wave of a land's cellar, by kind: three waves, no two alike, each
     * with more in it than the last, made of what walks in that land's chapter. The first
     * is of those that walk; the second brings those that hop, throw, shoot and fly, onto
     * the platforms and into the air; and the last is some of everything, and ends on
     * something particular: an outsized one, or, where he sleeps, the Old Book. After the
     * first chapter no wave is all of one kind.
     */
    static int[] wave(int number, int wave, boolean den) {
        int chapter = chapterOf(number);
        int[][] waves;
        if (chapter == 0) {
            waves = new int[][]{{MINUS, MINUS, MINUS, MINUS}, {MINUS, MINUS, MINUS, DIVIDE, DIVIDE}, {MINUS, MINUS, DIVIDE, DIVIDE, TIMES, TIMES, MINUS + GIANT}};
        } else if (chapter == 1) {
            waves = new int[][]{{MINUS, MINUS, MINUS, TIMES, TIMES}, {THROWER, THROWER, DIVIDE, DIVIDE, TIMES, TIMES},
                    {TIMES, TIMES, TIMES, THROWER, THROWER, GUNNER, MINUS, TIMES + GIANT}};
        } else if (chapter < CHAPTERS) {
            waves = new int[][]{{MINUS, MINUS, MINUS, TIMES, TIMES, TIMES}, {THROWER, THROWER, THROWER, GUNNER, GUNNER, DIVIDE, DIVIDE},
                    {TIMES, TIMES, TIMES, THROWER, THROWER, GUNNER, GUNNER, MINUS, MINUS, TIMES + GIANT}};
        } else {
            // The second volume: each chapter has a walker, a bounder, and one that comes after her, of its own; and its dragons fly.
            int[] walker = {SLIME, POGO, BAR, FAN, BAR, SLIME, FAN}, bounder = {POGO, BAR, SLIME, POGO, FAN, POGO, BAR}, hunter = {BAR, SLIME, FAN, SLIME, SLIME, BAR, SLIME};
            int which = chapter - CHAPTERS, one = walker[which], two = bounder[which], three = hunter[which];
            waves = new int[][]{{one, one, one, MINUS, MINUS, TIMES}, {DRAGON, DRAGON, two, two, two, THROWER, THROWER, GUNNER},
                    {three, three, three, one, one, DRAGON, DRAGON, DRAGON, THROWER, GUNNER, three + GIANT}};
        }
        int[] made = waves[wave - 1].clone();
        // Where the Old Book sleeps, he is what the last wave ends on, in place of the outsized one.
        if (den && wave == 3) made[made.length - 1] = OLD_BOOK;
        return made;
    }
    /**
     * And three that are only in the places off a land: a door that is shut until its key
     * has been taken; a bridge that is not there until its lever has been pulled; and bars
     * that lift for a few seconds when a plate before them is stood on.
     */
    static final char LOCKED = 'K', SPAN = 'b', GRILLE = 'g';
    /**
     * What the inside of a place off the land looks like: a wine cellar, a map room, a
     * flooded vault, the inside of a clock tower, a greenhouse at night, a printing room,
     * a crystal cave or a treasure vault. Each chapter has three of them, and which a
     * place is goes by the land and by which of the land's places it is, so that no two
     * places of one land look alike.
     */
    static final int WINE = 0, MAPS = 1, VAULT = 2, CLOCK = 3, GREENHOUSE = 4, PRINT = 5, CRYSTAL = 6, TREASURE = 7, LOOKS = 8;
    private static final int[][] LOOKS_OF = {{WINE, MAPS, GREENHOUSE}, {MAPS, PRINT, WINE}, {GREENHOUSE, TREASURE, CLOCK}, {CRYSTAL, CLOCK, VAULT},
            {PRINT, VAULT, TREASURE}, {TREASURE, WINE, MAPS}, {PRINT, CLOCK, MAPS}, {VAULT, GREENHOUSE, CRYSTAL}, {VAULT, CRYSTAL, GREENHOUSE},
            {CLOCK, TREASURE, PRINT}};

    static int lookOf(int number, int which) {
        int[] of = LOOKS_OF[Math.min(LOOKS_OF.length - 1, chapterOf(number))];
        return of[(number * 2 + which) % of.length];
    }

    /** What a stash holds besides its chest: something to help, a pet that waits to be taken along, or a shrine that sells a ribbon for a heart. */
    static final int HOLDS_TREAT = 0, HOLDS_PET = 1, HOLDS_SHRINE = 2;
    /**
     * What a stash or a passage is put together from, in an order of its own in every
     * land. Each piece begins and ends on the floor and can be crossed either way: spikes
     * hopped over on ledges; a tower of blocks to climb and come down; a spring under a
     * shelf with something on it; a run of ledges that crumble over spikes; a ledge that
     * rides across them; a locked door, whose key is up somewhere before it; a pit of
     * spikes with a bridge that a lever raises; bars that a plate lifts for a while; a
     * place where creatures stand guard; and a wall that only a spring gets her over.
     */
    static final int HOP = 0, STACK = 1, SHELF = 2, CRUMBLING = 3, RIDE = 4, LOCK = 5, SPAN_PIT = 6, BARRED = 7, GUARDS = 8, SPRING_OVER = 9, PIECES = 10;

    /**
     * What leads between a land's layers: a vine (or ladder) she climbs, up with the jump held and down with the down button; and a false
     * wall, drawn as rock until she is in it, which she walks through.
     */
    static final char VINE = 'v', FAKE = 'x';

    static final char THIN = '-', CRUMBLE = 'c', BELT_RIGHT = '>', BELT_LEFT = '<', SLICK = '~', DRAUGHT = 'u', DOOR = 'D', LAVA = 'L', WATER = 'w';

    /**
     * Cracked ground: a square of the floor that looks sound until you look at it, and that only a ground pound breaks. Walking on it, striking
     * at it and falling onto it do nothing; a pound of the step that is asked for (one, two or three, by which of the three it is) breaks it,
     * and what is under it is there to be dropped into. Cracked paper, slate, old floorboards or ice, by the land.
     */
    static final char CRACK = 'k', CRACK2 = 'j', CRACK3 = 'q';

    /** How strong a ground pound has to be to break a square: 1, 2 or 3; 0 for a square that is not cracked. */
    static int crackNeed(char square) { return square == CRACK ? 1 : square == CRACK2 ? 2 : square == CRACK3 ? 3 : 0; }

    /**
     * What lies under cracked ground. A vault: a small chamber with chests and something rare. A shortcut: a long low way under the land that
     * comes up again further on. A basement: a few creatures to beat, and then the chest they guard. A cache, in a dungeon: a lower chamber with
     * a chest that may hold a blessing or a relic. Each has a vine up out of it, so that there is always a way back.
     */
    static final int V_VAULT = 0, V_SHORTCUT = 1, V_BASEMENT = 2, V_CACHE = 3;
    /** The two ways between layers that a vault has, in a land's list of them: the shaft under its cracked ground, and the vine that comes up out of it. */
    static final int WAY_SHAFT = 4, WAY_CLIMB = 5;
    /** The first of the bits of a land's found places that are for vaults (the places of the doors take the low ones). */
    static final int VAULT_BIT = 24;
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
    /**
     * And three who do not wait at the end of anything, but are met in the middle of some
     * lands from the middle of the first volume on: the Margin Wyrm, a dragon of folded
     * pages, which crosses the land three times, strafing it, and comes low once each time;
     * Blot, a slime as big as a doorway, which fills the way, leaps, spits, and comes apart
     * into slimes as it is beaten; and the Bookmark Gecko, which hangs by its ribbon over
     * the way and drops on whoever passes under. None of them has to be beaten; each that
     * is gives something.
     */
    static final int WYRM = 29, BLOT = 30, GECKO = 31;
    private static final int[] WYRM_LANDS = {22, 28, 33, 39, 46, 53, 59, 66, 72, 79, 83, 89, 96}, BLOT_LANDS = {15, 27, 32, 38, 44, 51, 57, 63, 69, 76, 86, 92, 98},
            GECKO_LANDS = {20, 29, 36, 42, 48, 54, 62, 68, 74, 78, 84, 91, 97};

    static boolean rover(int kind) { return kind >= WYRM && kind <= GECKO; }

    /**
     * And those that fly, which are in the lands themselves from the twenty-first on, more
     * of them and more kinds as the lands go by. A comma moth goes to and fro at a height
     * and is in the way of a jump. A caret swallow hangs high, marks whoever comes under it,
     * and dives through where she stood and up again. An ellipsis cloud flies over and lets
     * full stops fall straight down. An asterisk hovers at a distance, takes aim and fires,
     * and backs off from whoever comes near. Tittles, the dots of the letter i, fly four in
     * a line, and are worth more each landed on without touching the ground. A candle wisp
     * comes slowly after whoever passes, and only water, or steam, puts it out; a rime flake
     * goes round and round, and only fire, or steam, melts it. Neither of those two has to
     * be beaten: a wisp can be outrun and a flake gone under or over. And a full stop that
     * lands on her in the air is the end of a flight in green, whoever let it fall.
     */
    static final int DRIFTER = 32, SWOOPER = 33, BOMBER = 34, SHOOTER = 35, MITE = 36, WISP = 37, RIME = 38;
    /**
     * And who waits at the end of the last land of all: Lucy, the Dragon of Lost Words, whom they call the Great Translation Killer.
     * A child, to look at, until her dragon side comes out; and at the last a dragon.
     */
    static final int LUCY = 39;
    /** The first land that has any of them; the land each is first met in, which has three of it before anything else that flies; and how far over the ground each keeps. */
    static final int FLIERS_FROM = 21;
    private static final int[] FLIES_FROM = {21, 23, 26, 28, 32, 37, 43}, FLIES_HIGH = {2, 5, 4, 3, 2, 3, 3};

    static boolean flier(int kind) { return kind >= DRIFTER && kind <= RIME; }

    /**
     * How many times as many creatures a land has as it was laid with: twice in the first
     * three lands, which are for learning in; five times after them; and one time more with
     * every three levels of the land, so that the last lands have ten times. Those that make
     * up the difference are of the kinds the land has already, in packs of two to four, on
     * its ground and on its ledges, spread along it, as many of them as there is room for
     * with room left to come down among them; and from the twenty-first land on twice as
     * many again of those that fly as it had, and once more again with every five levels. They keep clear of the start, of flags, of
     * villages and of doors. They are put in last, with chances of their own, so that the
     * land under them is the land it was; and each is marked as one of them.
     */
    static int crowdOf(int number) { return number <= 3 ? 2 : 5 + (levelOf(number) - 1) / 3; }

    /** How many times as many of those that fly are put in as a land was laid with. */
    static int flocksOf(int number) { return 2 + (levelOf(number) - 1) / 5; }

    /** The first land with paper trees in it; and how many a land has. */
    static final int TREES_FROM = 2;

    static int treesIn(int number) { return number < TREES_FROM ? 0 : 2 + number / 25; }

    /**
     * Paper trees: a few in every land from the second, on flat open ground clear of everything else, each of a grade by the land's
     * chapter (plain, fine from the fourth chapter, golden from the eighth). Drawn from numbers of their own.
     */
    private static void plant(Level level) {
        int number = level.number, want = treesIn(number), until = level.arena >= 0 ? level.arena - 10 : level.gate - 6, chapter = Math.min(11, chapterOf(number));
        if (want == 0) return;
        Random random = new Random(104729L * number + 7919);
        List<Integer> free = new ArrayList<>();
        for (int column = 14; column < until; column++) {
            int floor = top(level, column);
            boolean clear = floor > 5 && floor < level.land.length && top(level, column - 1) == floor && top(level, column + 1) == floor;
            for (int row = floor - 4; row < floor && clear; row++) clear = row >= 0 && level.land[row][column] == AIR;
            for (int flag : level.flags) clear &= Math.abs(flag - column) > 4;
            for (int[] thing : level.decor) clear &= Math.abs(thing[1] - column) > 6;
            for (int[] room : level.rooms) clear &= Math.abs(room[1] - column) > 3;
            for (int[] item : level.items) clear &= item[1] >= level.main || Math.abs(item[1] - column) > 2;
            for (int[] weapon : level.weapons) clear &= Math.abs(weapon[1] - column) > 2;
            // (Those put in to make up the numbers may stand by a tree; nothing else does.)
            for (int[] foe : level.foes) clear &= crowd(foe) || foe[1] >= level.main || Math.abs(foe[1] - column) > 1 || foe[2] < floor - 5;
            if (clear) free.add(column);
        }
        for (int count = 0; count < want && !free.isEmpty(); count++) {
            int column = free.get(random.nextInt(free.size()));
            level.trees.add(new int[]{column, top(level, column) - 1, RunCraft.grade(chapter, random.nextInt(100))});
            for (int index = free.size() - 1; index >= 0; index--) if (Math.abs(free.get(index) - column) < 14) free.remove(index);
        }
    }

    /** Whether one was put in as one of a swarm (1), or is one of the unique creatures (2). */
    static boolean swarmed(int[] foe) { return foe.length > 5 && foe[5] == 1; }

    static boolean uniqueSpec(int[] foe) { return foe.length > 5 && foe[5] == 2; }

    /** How many of its chapter's unique creatures a land has: four at first, sixteen in the last lands. */
    static int uniquesIn(int number) { return 4 + number / 8; }

    /**
     * The unique creatures of a land's chapter: in small groups of one kind, on the ground along the land (the hornets in a line in the
     * air over it), with room about each group; a mini-boss alone, once, two thirds of the way along. They keep clear of the start,
     * flags, doors, villages and anything else that is a place of its own. Drawn from numbers of their own, so that the land is as it was.
     */
    private static void uniques(Level level) {
        int number = level.number, chapter = Math.min(11, chapterOf(number)), until = level.arena >= 0 ? level.arena - 10 : level.gate - 6;
        Random random = new Random(1299709L * number + 15485863);
        List<int[]> spots = new ArrayList<>();
        for (int column = 30; column < until; column++) {
            boolean near = false;
            for (int flag : level.flags) near |= Math.abs(flag - column) < 6;
            for (int[] thing : level.decor) near |= Math.abs(thing[1] - column) < 8;
            for (int[] room : level.rooms) near |= Math.abs(room[1] - column) < 4 || Math.abs(room[5] - column) < 4;
            for (int[] item : level.items) if (item[1] < level.main) near |= Math.abs(item[1] - column) < 4;
            for (int[] weapon : level.weapons) near |= Math.abs(weapon[1] - column) < 2;
            for (int[] foe : level.foes) if (rover(foe[0]) || uniqueSpec(foe)) near |= Math.abs(foe[1] - column) < (rover(foe[0]) ? 16 : 3);
            if (near) continue;
            int floor = top(level, column);
            boolean flat = floor > 5 && floor < level.land.length && level.land[floor][column] == BLOCK && level.land[floor - 1][column] == AIR && level.land[floor - 2][column] == AIR
                    && level.land[floor - 3][column] == AIR && top(level, column + 1) == floor && top(level, column - 1) == floor;
            if (flat) spots.add(new int[]{column, floor});
        }
        if (spots.isEmpty()) return;
        int first = RunUniques.first(chapter), want = uniquesIn(number);
        boolean bossy = RunUniques.way(first) == RunUniques.MINIBOSS || RunUniques.way(first + 1) == RunUniques.MINIBOSS;
        // The mini-boss, alone, two thirds of the way along.
        if (bossy) {
            int kind = RunUniques.way(first) == RunUniques.MINIBOSS ? first : first + 1;
            int[] spot = spots.get(Math.min(spots.size() - 1, spots.size() * 2 / 3));
            level.foes.add(new int[]{kind, spot[0], spot[1] - 1, 1, 0, 2});
            want--;
        }
        List<Integer> taken = new ArrayList<>();
        for (int tries = 0; tries < 200 && want > 0; tries++) {
            int[] spot = spots.get(random.nextInt(spots.size()));
            boolean clear = true;
            for (int column : taken) clear &= Math.abs(column - spot[0]) >= 9;
            for (int[] foe : level.foes) if (uniqueSpec(foe)) clear &= Math.abs(foe[1] - spot[0]) >= 9;
            if (!clear) continue;
            // (From the sixth chapter on, the chapter's two later ones as well: four to choose from; before it, its two made of fire, frost and ink.)
            int later = RunUniques.laterOf(chapter);
            if (later < 0) later = RunUniques.statusOf(chapter);
            int pick = random.nextInt(later < 0 ? 2 : 4);
            int kind = pick < 2 ? first + pick : later + pick - 2;
            if (RunUniques.way(kind) == RunUniques.MINIBOSS) kind = kind == first ? first + 1 : first;
            taken.add(spot[0]);
            if (RunUniques.flies(kind)) {
                // A line of hornets in the air, three squares up.
                int size = Math.min(want, 3 + random.nextInt(3)), put = 0;
                for (int one = 0; one < size; one++) {
                    boolean clearOfFlags = spot[0] + one < until;
                    for (int flag : level.flags) clearOfFlags &= Math.abs(flag - spot[0] - one) >= 6;
                    if (!clearOfFlags) break;
                    level.foes.add(new int[]{kind, spot[0] + one, spot[1] - 4, 1, 0, 2});
                    put++;
                }
                want -= Math.max(1, put);
                continue;
            }
            int size = Math.min(want, 1 + random.nextInt(number < 12 ? 2 : 3));
            for (int one = 0; one < size; one++) {
                int column = spot[0] + one * 2;
                boolean clearOfFlags = column < until;
                for (int flag : level.flags) clearOfFlags &= Math.abs(flag - column) >= 6;
                boolean flatThere = false;
                for (int[] other : spots) flatThere |= other[0] == column && other[1] == spot[1];
                if (!flatThere || !clearOfFlags) break;
                level.foes.add(new int[]{kind, column, spot[1] - 1, 1, 0, 2});
                want--;
            }
        }
    }

    /** Whether something of a land was put in to make up its numbers. */
    static boolean crowd(int[] foe) { return foe.length > 4 && foe[4] != 0; }

    private static void crowd(Level level) {
        int number = level.number, base = 0, until = level.arena >= 0 ? level.arena - 10 : level.gate - 6;
        Random random = new Random(31337L * number + 7);
        List<Integer> walkers = new ArrayList<>(), shooters = new ArrayList<>();
        for (int[] foe : level.foes) {
            if (foe[1] >= level.main || boss(foe[0]) || flier(foe[0]) || foe[0] == TARGET) continue;
            base++;
            if (foe[0] == THROWER || foe[0] == GUNNER) shooters.add(foe[0]);
            else if (foe[0] != WORD && foe[0] != DRAGON && foe[0] != ZERO) walkers.add(foe[0]);
        }
        if (walkers.isEmpty()) walkers.add(MINUS);
        // Where one could stand: on the ground, or on a ledge, with room over it. They are small: one stands on a single square.
        List<int[]> spots = new ArrayList<>();
        for (int column = 24; column < until; column++) {
            boolean near = false;
            for (int flag : level.flags) near |= Math.abs(flag - column) < 6;
            for (int[] thing : level.decor) near |= Math.abs(thing[1] - column) < 8;
            for (int[] room : level.rooms) near |= Math.abs(room[1] - column) < 4 || Math.abs(room[5] - column) < 4;
            for (int[] item : level.items) if (item[1] < level.main && (item[3] != 0 || item[0] >= PlusRun.CHEST)) near |= Math.abs(item[1] - column) < 6;
            // (Well clear of one met in the middle of the land: its place is its own.)
            for (int[] foe : level.foes) if (rover(foe[0])) near |= Math.abs(foe[1] - column) < 16;
            if (near) continue;
            int floor = top(level, column);
            // (Nor where one the land was laid with stands.)
            boolean taken = false;
            for (int[] foe : level.foes) taken |= foe[2] == floor - 1 && Math.abs(foe[1] - column) < 2;
            if (floor > 4 && !taken && level.land[floor][column] == BLOCK && level.land[floor - 1][column] == AIR && level.land[floor - 2][column] == AIR) {
                spots.add(new int[]{column, floor});
            }
            for (int row = 4; row < (floor > 0 ? floor - 2 : ROWS - 2); row++) {
                if (level.land[row][column] == LEDGE && level.land[row - 1][column] == AIR && level.land[row - 2][column] == AIR) spots.add(new int[]{column, row});
            }
        }
        // They stand in packs along each run of such squares, at places half a square apart. The members of a pack are a square and a
        // half from one another: between any two of them there is room for her to come down, with room to spare. And from the last of
        // one pack to the first of the next is two squares and a half: room to stand. A times cannot be landed on: it stands at the far
        // end of its pack, two squares from the one before it, so that there is a way over it from among them; and never within five
        // squares of another of its kind, so that there is no row of them across the way.
        java.util.Collections.sort(spots, (one, other) -> one[1] != other[1] ? one[1] - other[1] : one[0] - other[0]);
        List<List<int[]>> packs = new ArrayList<>();
        int wasRow = -1, wasPlace = -99;
        for (int index = 0; index < spots.size(); ) {
            int last = index;
            while (last + 1 < spots.size() && spots.get(last + 1)[1] == spots.get(index)[1] && spots.get(last + 1)[0] == spots.get(last)[0] + 1) last++;
            int places = (last - index) * 2 + 1, first = spots.get(index)[0] * 2, row = spots.get(index)[1];
            for (int at = places > 4 ? random.nextInt(2) : 0; at < places; ) {
                // (Where one run of squares ends a square short of the next, the packs keep their distance across the break.)
                if (row == wasRow && first + at - wasPlace < 5) {
                    at = wasPlace + 5 - first;
                    continue;
                }
                // (In the lands of the first two chapters, which are short, they come one to three together; then two to four; and in
                // the second volume three to five.)
                int size = Math.min(number <= 12 ? 1 + random.nextInt(3) : number <= COUNT ? 2 + random.nextInt(3) : 3 + random.nextInt(3), (places - 1 - at) / 3 + 1);
                int kind = !shooters.isEmpty() && random.nextInt(5) == 0 ? shooters.get(random.nextInt(shooters.size())) : walkers.get(random.nextInt(walkers.size()));
                boolean times = kind == TIMES;
                int spiked = first + at + (size - 1) * 3 + (size > 1 ? 1 : 0);
                if (times) {
                    times = spiked < first + places;
                    for (int[] other : level.foes) times &= other[0] != TIMES || other[2] != row - 1 || Math.abs(other[1] * 2 - spiked) >= 11;
                    for (List<int[]> pack : packs) for (int[] other : pack) times &= other[0] != TIMES || other[2] != row - 1 || Math.abs(other[1] * 2 + other[4] - 1 - spiked) >= 10;
                    kind = MINUS;
                    for (int walker : walkers) if (walker != TIMES) kind = walker;
                }
                List<int[]> pack = new ArrayList<>();
                int place = first + at;
                for (int each = 0; each < size; each++) {
                    boolean end = times && each == size - 1;
                    place = end ? spiked : first + at + each * 3;
                    pack.add(new int[]{end ? TIMES : kind, place / 2, row - 1, 1, place % 2 == 0 ? 1 : 2});
                }
                packs.add(pack);
                wasRow = row;
                wasPlace = place;
                at = place - first + 5;
            }
            index = last + 1;
        }
        // Which of the packs there is room for are there: taken as they come, shuffled, so that they are spread along the land.
        java.util.Collections.shuffle(packs, random);
        int want = base * (crowdOf(number) - 1);
        for (List<int[]> pack : packs) {
            for (int[] one : pack) {
                // (A pack cut short keeps its near end: never a times left by itself where it was to have had company.)
                if (want <= 0) break;
                level.foes.add(one);
                want--;
            }
        }
        // And in the air.
        int known = 0;
        for (int kind = DRIFTER; kind <= RIME; kind++) if (FLIES_FROM[kind - DRIFTER] <= number) known++;
        for (int more = 0; more < fliersIn(number) * flocksOf(number); more++) perch(level, DRIFTER + random.nextInt(known), 18 + random.nextInt(Math.max(1, until - 30)), until - 2, true);
    }

    /**
     * How many times as many creatures as it was laid with a land has at last: twenty in the first three lands, and from there up to
     * fifty in the last. (The packs that make up its numbers, {@link #crowd}, give two to ten times; the swarms give the rest.)
     */
    static int swarmOf(int number) { return number <= 3 ? 20 : Math.min(50, 20 + 30 * (number - 3) / (TOTAL - 3)); }

    /** How many a swarm has: small in the first lands, a crowd in the last. */
    static int swarmSize(int number, Random random) { return number <= 3 ? 4 + random.nextInt(3) : Math.min(22, 4 + number / 5 + random.nextInt(4)); }

    /**
     * Swarms: what a land is short of its count is made up of creatures gathered in a few places, not spread along it. Each swarm is
     * round one place on the ground or on a ledge, and takes the squares near it on the ground and on every ledge over it, two to a
     * square where it is thick: a crowd at the foot of a climb, a ledge stacked with them, a pack waiting where the way narrows. They
     * keep clear of the start, of flags, villages, doors and of anything else that is a place of its own; there is a stretch with
     * nobody in it between one swarm and the next, for her to get her breath back. They are of the kinds the land has (never a times,
     * which cannot be landed on, and now and then one that throws or shoots), marked as put in to make up the numbers: they keep still
     * until she is near. Drawn from numbers of their own, so that the land under them is the land it was.
     */
    private static void swarm(Level level) {
        int number = level.number, until = level.arena >= 0 ? level.arena - 10 : level.gate - 6, base = 0, now = 0;
        Random random = new Random(7919L * number + 104729);
        List<Integer> walkers = new ArrayList<>(), shooters = new ArrayList<>();
        for (int[] foe : level.foes) {
            if (foe[1] >= level.main || boss(foe[0]) || flier(foe[0]) || foe[0] == TARGET || rover(foe[0])) continue;
            now++;
            if (!crowd(foe)) base++;
            if (foe[0] == THROWER || foe[0] == GUNNER) shooters.add(foe[0]);
            else if (foe[0] != WORD && foe[0] != DRAGON && foe[0] != ZERO && foe[0] != TIMES) walkers.add(foe[0]);
        }
        if (walkers.isEmpty()) walkers.add(MINUS);
        int want = Math.max(1, base) * swarmOf(number) - now;
        if (want <= 0) return;
        // Where one could stand, as crowd() has it: the ground and the ledges, with room over them, clear of every place of its own.
        List<int[]> spots = new ArrayList<>();
        for (int column = 24; column < until; column++) {
            boolean near = false;
            for (int flag : level.flags) near |= Math.abs(flag - column) < 6;
            for (int[] thing : level.decor) near |= Math.abs(thing[1] - column) < 8;
            for (int[] room : level.rooms) near |= Math.abs(room[1] - column) < 4 || Math.abs(room[5] - column) < 4;
            for (int[] item : level.items) if (item[1] < level.main) near |= Math.abs(item[1] - column) < (item[3] != 0 || item[0] >= PlusRun.CHEST ? 6 : 3);
            for (int[] weapon : level.weapons) near |= Math.abs(weapon[1] - column) < 2;
            for (int[] foe : level.foes) if (rover(foe[0])) near |= Math.abs(foe[1] - column) < 16;
            if (near) continue;
            int floor = top(level, column);
            // (On the ground, never by the edge of a pit, where a knock would send her in.)
            boolean edge = false;
            for (int beside = column - 3; beside <= column + 3; beside++) edge |= top(level, beside) < 0 || top(level, beside) > floor + 1;
            if (!edge && floor > 4 && floor < level.land.length && level.land[floor][column] == BLOCK && level.land[floor - 1][column] == AIR && level.land[floor - 2][column] == AIR) {
                spots.add(new int[]{column, floor});
            }
            // (Ledges, but not the highest of them: those are for what is hidden.)
            for (int row = Math.max(4, floor - 5); row < (floor > 0 ? floor - 2 : ROWS - 2); row++) {
                if (level.land[row][column] == LEDGE && level.land[row - 1][column] == AIR && level.land[row - 2][column] == AIR) spots.add(new int[]{column, row});
            }
        }
        if (spots.isEmpty()) return;
        java.util.Set<Long> standable = new java.util.HashSet<>();
        for (int[] spot : spots) standable.add(((long) spot[0] << 16) | spot[1]);
        // How many are on each half square already.
        java.util.Map<Long, Integer> on = new java.util.HashMap<>();
        for (int[] foe : level.foes) {
            long key = ((long) (foe[1] * 2 + (foe.length > 4 && foe[4] == 2 ? 1 : 0)) << 16) | (foe[2] + 1);
            on.merge(key, 1, Integer::sum);
        }
        // The swarms, one at a time, each round a place no nearer than a gap to the last ones.
        List<Integer> centres = new ArrayList<>();
        int gap = number <= 3 ? 14 : 10, tries = 0, most = 2, blocked = 0;
        while (want > 0 && tries++ < 1500) {
            int[] middle = spots.get(random.nextInt(spots.size()));
            boolean clear = true;
            for (int centre : centres) clear &= Math.abs(centre - middle[0]) >= gap;
            if (!clear) {
                // Out of room for more so far apart: they come closer together, and then the swarms there grow thicker.
                if (++blocked > 60) {
                    blocked = 0;
                    if (gap > 6) gap -= 2;
                    else if (most < 3) most++;
                    centres.clear();
                }
                continue;
            }
            centres.add(middle[0]);
            int size = Math.min(want, swarmSize(number, random)), reach = 3 + size / 6;
            // (Of walkers only: a swarm that threw or shot would fill the air.)
            int kind = walkers.get(random.nextInt(walkers.size()));
            // Its squares: those near its middle, on the ground and on the ledges over and under it, nearest first.
            List<int[]> near = new ArrayList<>();
            for (int[] spot : spots) if (Math.abs(spot[0] - middle[0]) <= reach && Math.abs(spot[1] - middle[1]) <= 7) near.add(spot);
            near.sort((one, other) -> Math.abs(one[0] - middle[0]) * 3 + Math.abs(one[1] - middle[1]) - Math.abs(other[0] - middle[0]) * 3 - Math.abs(other[1] - middle[1]));
            int put = 0;
            for (int layer = 1; layer <= most && put < size; layer++) {
                for (int[] spot : near) {
                    for (int half = 0; half < 2 && put < size; half++) {
                        // (Half a square over only where the square beyond is one to stand on too: never into a wall.)
                        if (half == 1 && !standable.contains(((long) (spot[0] + 1) << 16) | spot[1])) continue;
                        long key = ((long) (spot[0] * 2 + half) << 16) | spot[1];
                        if (on.getOrDefault(key, 0) >= layer) continue;
                        on.merge(key, 1, Integer::sum);
                        // (A second kind now and then in a big swarm.)
                        int one = size > 8 && put % 5 == 4 ? walkers.get(random.nextInt(walkers.size())) : kind;
                        level.foes.add(new int[]{one, spot[0], spot[1] - 1, 1, half == 0 ? 1 : 2, 1});
                        put++;
                    }
                    if (put >= size) break;
                }
            }
            want -= put;
        }
    }

    /**
     * With five times the creatures a land has more to fight with lying in it: one more
     * thing for every forty squares or so of its length, on flat ground where nothing else
     * lies, of the kinds that are not a book, turn about.
     */
    private static void arsenal(Level level) {
        int until = (level.arena >= 0 ? level.arena : level.gate) - 10, which = level.number;
        int[] kinds = {PlusRun.PEN, PlusRun.KNIFE, PlusRun.BRUSH, PlusRun.SWORD, PlusRun.GUN};
        for (int from = 40; from < until; from += 40) {
            for (int column = from; column < from + 14 && column < until; column++) {
                int floor = top(level, column);
                boolean free = floor > 3 && level.land[floor - 1][column] == AIR && level.land[floor - 2][column] == AIR;
                for (int[] weapon : level.weapons) free &= Math.abs(weapon[1] - column) > 6;
                for (int[] item : level.items) free &= item[1] != column;
                for (int[] thing : level.decor) free &= Math.abs(thing[1] - column) > 6;
                if (!free) continue;
                // (In one land in five from the seventh on, the first of them is a ring of keys: hers for good once she has found one.)
                boolean keys = level.number >= 7 && level.number % 5 == 2 && from == 40;
                level.weapons.add(new int[]{keys ? RunArms.KEYS : kinds[which++ % kinds.length], column, floor - 1});
                break;
            }
        }
    }

    /**
     * From the third land on a pet lies waiting in every land, a different kind from land
     * to land, a little past the first flag, on flat ground where nothing else lies.
     */
    static int petOf(int number) { return number < 3 ? -1 : PET_ROUND[number % PET_ROUND.length]; }

    /** The round of them: the five animals come twice in it, and the five small copies of people she knows once each. */
    private static final int[] PET_ROUND = {PlusRun.CAT, PlusRun.JAR, PlusRun.MINI_CHIKUWA, PlusRun.DOG, PlusRun.DUCK, PlusRun.MINI_RUNE, PlusRun.MONKEY, PlusRun.JAR,
            PlusRun.IMP, PlusRun.CAT, PlusRun.DOG, PlusRun.CHERUB, PlusRun.DUCK, PlusRun.MONKEY, PlusRun.TWIN};

    private static void kennel(Level level) {
        int pet = petOf(level.number);
        if (pet < 0 || level.flags.isEmpty()) return;
        int flag = level.flags.get(0);
        for (int column = flag + 3; column < flag + 12; column++) {
            int floor = top(level, column);
            boolean free = floor > 3 && level.land[floor - 1][column] == AIR && level.land[floor - 2][column] == AIR;
            for (int[] weapon : level.weapons) free &= weapon[1] != column;
            for (int[] item : level.items) free &= item[1] != column;
            if (!free) continue;
            level.items.add(new int[]{PlusRun.PET + pet, column, floor - 1, 0});
            return;
        }
    }

    /** One land in three has a book lying in it, and no land more than one: the rest are knives. A book is a find. */
    private static void shelve(Level level) {
        boolean one = level.number % 3 != 0;
        for (int[] weapon : level.weapons) {
            if (weapon[0] != PlusRun.BOOK) continue;
            if (one || weapon[1] < 8) weapon[0] = weapon[1] < 8 ? PlusRun.PEN : PlusRun.KNIFE;
            else one = true;
        }
    }

    /**
     * Black, which is a curse, is met from the middle of the first volume on: a black
     * ribbon lies a little past a good one in the land, for whoever takes both without
     * looking, and another in a stash, after its pluses. And from the fifty-first land on
     * an asterisk throws it. No place gives a white ribbon: what a place off the land or a
     * game gives is a ribbon of one of the six colours, for a while, in the first volume,
     * and two in the second.
     */
    static final int BLACK_FROM = 13, CURSES_FROM = 51;

    /** The colours of the ribbons a place off a land gives when it is won there: one in the first volume, two after; never white, never black. */
    static int[] ribbonsOf(int number) {
        int first = PlusRun.RED + number % 6, second = PlusRun.RED + (number + 3) % 6;
        return number > COUNT ? new int[]{first, second} : new int[]{first};
    }

    /** A black ribbon is laid a little past the first good one of a land, from the thirteenth on, where the ground is flat and nothing lies. */
    private static void tempt(Level level) {
        if (level.number < BLACK_FROM || level.number > COUNT) return;
        for (int[] item : new ArrayList<>(level.items)) {
            if (item[0] <= PlusRun.FORM || item[0] >= PlusRun.CHEST || item[0] == PlusRun.FORM + PlusRun.BLACK || item[1] >= level.main || item[3] != 0) continue;
            for (int column = item[1] + 2; column <= item[1] + 4; column++) {
                int floor = top(level, column);
                boolean clear = floor > 3 && floor == top(level, item[1]) && level.land[floor - 1][column] == AIR;
                for (int[] other : level.items) clear &= other[1] != column;
                for (int[] foe : level.foes) clear &= Math.abs(foe[1] - column) > 1;
                if (!clear) continue;
                level.items.add(new int[]{PlusRun.FORM + PlusRun.BLACK, column, floor - 1, 0});
                return;
            }
        }
    }

    /**
     * Which of the ways they might be spread over a land is used for it: the first, but
     * for the lands named here, where the first left the land one that could not be crossed
     * without a hurt. Found as the numbers the lands are made from were. (And a way to try
     * another, for the finding.)
     */
    static final int[] FLOCKS = new int[100];
    static int flockTry = -1;
    static {
        // (Found again with the creatures of the later lands as tough as their levels make them.)
        // (And again for the first land of a chapter, its village now the chapter's whole village.)
        for (int land : new int[]{40, 51, 57, 63}) FLOCKS[land - 1] = 1;
        for (int land : new int[]{46, 67, 68, 71, 72, 78, 79}) FLOCKS[land - 1] = 2;
    }

    static int flierFrom(int kind) { return FLIES_FROM[kind - DRIFTER]; }

    /** How many of them, or lines of them, a land has: five in the twenty-first, one more every three lands, two more in the second volume; one fewer where someone waits. */
    static int fliersIn(int number) {
        if (number < FLIERS_FROM) return 0;
        return Math.min(18, 5 + (number - FLIERS_FROM) / 3 + (number > COUNT ? 2 : 0) - (bossOf(number) >= 0 ? 1 : 0));
    }

    /**
     * Those that fly are put into a land, spread along it from a little after its start to
     * a little before its end or whoever waits there: each over the ground it keeps to, or
     * by a pit over the nearer side of it, in open air, away from the flags and from each
     * other. They are put in when everything else has been, with chances of their own, so
     * that the land under them is the land it was.
     */
    private static void flock(Level level) {
        int number = level.number, count = fliersIn(number);
        if (count == 0) return;
        Random random = new Random(7919L * number + 13 + 104729L * (flockTry >= 0 ? flockTry : FLOCKS[number - 1]));
        int from = 18, until = (level.arena >= 0 ? level.arena : level.gate) - 8, newest = -1, known = 0;
        for (int kind = DRIFTER; kind <= RIME; kind++) {
            if (FLIES_FROM[kind - DRIFTER] == number) newest = kind;
            if (FLIES_FROM[kind - DRIFTER] <= number) known++;
        }
        for (int index = 0; index < count; index++) {
            int kind = newest >= 0 && index < 3 ? newest : DRIFTER + random.nextInt(known);
            perch(level, kind, from + (until - from) * (index * 2 + 1) / (count * 2) + random.nextInt(5) - 2, until, false);
        }
    }

    private static void perch(Level level, int kind, int column, int until, boolean crowd) {
        int high = FLIES_HIGH[kind - DRIFTER], wide = kind == MITE ? 4 : 1;
        for (int tries = 0; tries < 30; tries++, column += 2) {
            // (Past the end of where they may be, it looks again from the start of it.)
            if (column + wide + 2 >= until) column = 18 + tries;
            if (column < 14) return;
            int floor = -1;
            for (int reach = 0; reach <= 5 && floor < 0; reach++) {
                floor = top(level, column - reach);
                if (floor < 0) floor = top(level, column + reach);
            }
            int row = floor - high - 1;
            boolean clear = floor > 0 && row >= 2;
            // (A line of them keeps over ground that is all of a height.)
            for (int each = 1; each < wide && clear; each++) clear = top(level, column + each) == floor;
            for (int beside = column - 2; beside <= column + wide + 1 && clear; beside++) {
                for (int up = row - 1; up <= row + 1 && clear; up++) clear = level.land[up][beside] == AIR;
            }
            for (int flag : level.flags) clear &= Math.abs(flag - column) > 9;
            // (One put in to make up a land's numbers may be nearer another that flies where the two keep to different heights: three squares
            // off, and two up or down, so that there is a way between them.)
            for (int[] foe : level.foes) {
                if (flier(foe[0]) || rover(foe[0])) clear &= Math.abs(foe[1] - column) > (rover(foe[0]) ? 9 : crowd && Math.abs(foe[2] - row) >= 2 ? 2 : 5);
            }
            // (And of villages, and of whoever stands in the land to be spoken to or bought from.)
            for (int[] thing : level.decor) clear &= Math.abs(thing[1] - column) > 12;
            for (int[] item : level.items) if (item[1] < level.main && (item[3] != 0 || item[0] >= PlusRun.CHEST)) clear &= Math.abs(item[1] - column) > 9;
            if (!clear) continue;
            for (int each = 0; each < wide; each++) level.foes.add(crowd ? new int[]{kind, column + each, row, 1, 1} : new int[]{kind, column + each, row});
            return;
        }
    }

    /** Which of the three is met in a land, or -1: in no land where someone waits at the end. */
    static int roverOf(int number) {
        if (bossOf(number) >= 0) return -1;
        for (int land : WYRM_LANDS) if (land == number) return WYRM;
        for (int land : BLOT_LANDS) if (land == number) return BLOT;
        for (int land : GECKO_LANDS) if (land == number) return GECKO;
        return -1;
    }

    /**
     * The one met in a land is put into it: on flat, empty ground a little past its middle,
     * where there is room for it. Where a land has no such stretch there, a narrower one
     * will do, and then any firm ground at all between its middle and its gate.
     */
    private static void rove(Level level) {
        int rover = roverOf(level.number);
        if (rover < 0) return;
        for (int reach = 4; reach >= 0; reach -= 2) {
            for (int column = level.gate * 11 / 20; column < level.gate - 12; column++) {
                int floor = top(level, column);
                boolean clear = floor > 6;
                for (int beside = column - reach; beside <= column + reach && clear; beside++) {
                    clear = top(level, beside) == floor;
                    for (int up = 1; up <= 4 && clear; up++) clear = level.land[floor - up][beside] == AIR;
                }
                if (reach > 0) {
                    for (int[] room : level.rooms) clear &= Math.abs(room[1] - column) > 10 && Math.abs(room[5] - column) > 10;
                    for (int flag : level.flags) clear &= Math.abs(flag - column) > 6;
                    for (int[] item : level.items) if (item[1] < level.main) clear &= Math.abs(item[1] - column) > 5;
                }
                if (!clear) continue;
                level.foes.add(new int[]{rover, column, floor - 1});
                return;
            }
        }
    }
    static final int GUARD_LAND = 6, BUTLER_LAND = 12;

    static boolean boss(int kind) { return kind == ZERO || kind == ILLYA || kind == TSUBAKIEH || kind >= GOKHAE && kind <= GECKO && kind != TARGET || kind == LUCY; }

    /** Who waits at the end of a land, or -1: for the twins, Miri. */
    static int bossOf(int number) {
        // (In the third volume no one waits but at its end.)
        if (number > SECOND) return number == TOTAL ? LUCY : -1;
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
            {ISLANDS, ISLANDS, SPLIT, LOOP, ASCENT, DESCENT, ROUNDABOUT, TOWER},
            {ISLANDS, TOWER, SPLIT, CAVE, CAVE, ASCENT, DESCENT, LOOP, ROUNDABOUT},
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
            // The Unread Shore: water, slick sand, what rides and what lifts; and the Dragon's Library: everything.
            {WATERS, WATERS, SLICKS, SLICKS, MOVERS, MOVERS, DRAUGHTS, POGOS, DRAGONS, SLIMES, THINS, CRUMBLES, LIFT, BARS},
            {LAVAS, WATERS, MOVERS, DOORS, WORDS, WORDS, DRAGONS, BARS, SLIMES, FANS, SLICKS, DRAUGHTS, CRUMBLES, BELTS, THINS, LIFT, POGOS},
    };
    /** The numbers the lands of the second volume are made from, picked as those of the first were. */
    static final long[] LATER_SEEDS = {
            1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1,
            // The third volume.
            1, 2, 3, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1,
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
        /** Its squares (a dungeon's floor is let down a layer deeper when it has a place under it). */
        char[][] land;
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
        /** Its paper trees, to be cut down for paper: the column and row each stands on, and its grade (plain, fine, golden). */
        final List<int[]> trees = new ArrayList<>();
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
        /**
         * Where in the land's arenas something may come in: which place, whether on its floor,
         * a platform, a perch or in the air, the column, and the row it would stand on. And the
         * lofts: a platform apart from the rest of an arena, across a gap too wide to jump, with
         * a chest on it for whoever can fly: which place, from which column to which, and its row.
         */
        final List<int[]> spawns = new ArrayList<>(), lofts = new ArrayList<>();
        /** What each stash and passage was put together from: which place, and then its pieces in order. */
        final List<int[]> made = new ArrayList<>();
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
        /** Where its chapter's village begins, if it has it; or -1. */
        int town = -1;
        /** Whether its creatures come in colours of toughness: every land made from its number does; one made by hand does not. */
        boolean tiered;
        /** Where it has a lower layer: for each column of the land, whether there is a cave under it (a fall there lands in it); null if it has none. */
        boolean[] under;
        /** The hatches in its ground that drop into a cave, the vines up from one, and its high ledges' ways up: the column and row of each, and what it is (0 hatch, 1 vine foot, 2 hidden). */
        final List<int[]> ways = new ArrayList<>();
        /**
         * The places under cracked ground, for each: its kind; the column of its shaft and the row of the ground over it; the first and last columns
         * of the cracked patch; how many squares deep the crack is where the shaft is; how strong a pound breaks it (1 to 3); the first and last
         * columns of the chamber, and its floor row; and the column of the vine up out of it and the row of the hatch it comes up through.
         */
        final List<int[]> vaults = new ArrayList<>();

        Level(int number, int columns) { this(number, columns, ROWS); }

        /** A land so many rows deep: as deep as every land is, or, with an arena beyond its end, as deep as that. */
        Level(int number, int columns, int rows) {
            this.number = number;
            this.quest = questOf(number);
            this.chapter = chapterOf(number);
            this.columns = columns;
            this.main = columns;
            land = new char[rows][columns];
            for (char[] row : land) java.util.Arrays.fill(row, AIR);
        }
    }

    private RunLevels() {}

    /** How many rows a land's lower layer has, under the rows of the land itself; the row its caves' floor is at its lowest. */
    static final int LOWER = 16, CAVE_FLOOR = ROWS + LOWER - 3;

    /** How many diary pages there are. */
    static final int DIARY_PAGES = 18;

    /**
     * A land as it is for one play: the land itself, as the number kept for it makes it, always the same and always fair; and laid over it
     * afresh for each play, from the play's own number, its other layers. Under it, a cave or more, each dropped into by a hatch in the
     * ground (or by a pit over it, which is not the end there but a way down), with creatures, pluses and a chest in it, a false wall at an
     * end with a better chest behind it, and a vine up out of it further on. Over it, where the sky is clear, high ledges climbed to by a
     * vine, with pluses and a chest along them, and a spring at their end up to one more, hidden, at the top. More of them later on. Not
     * in the last land, nor anywhere for the black arena.
     */
    static Level layered(Level base, long play) {
        if (base.number >= OPEN_TO) return base;
        Random dice = new Random(play * 7349L + base.number * 1299721L);
        int rows = Math.max(base.land.length, ROWS + LOWER);
        Level level = new Level(base.number, base.columns, rows);
        for (int row = 0; row < base.land.length; row++) System.arraycopy(base.land[row], 0, level.land[row], 0, base.columns);
        level.main = base.main;
        level.quest = base.quest;
        level.arena = base.arena;
        level.gate = base.gate;
        level.guarded = base.guarded;
        level.town = base.town;
        level.tiered = base.tiered;
        level.pluses.addAll(base.pluses);
        level.foes.addAll(base.foes);
        level.weapons.addAll(base.weapons);
        level.items.addAll(base.items);
        level.flags.addAll(base.flags);
        level.movers.addAll(base.movers);
        level.doors.addAll(base.doors);
        level.decor.addAll(base.decor);
        level.trees.addAll(base.trees);
        level.rooms.addAll(base.rooms);
        level.lodgers.addAll(base.lodgers);
        level.spawns.addAll(base.spawns);
        level.lofts.addAll(base.lofts);
        level.made.addAll(base.made);
        level.nooks.addAll(base.nooks);
        level.ways.addAll(base.ways);
        level.vaults.addAll(base.vaults);
        level.under = new boolean[level.columns];
        // Under the land proper, rock; but under its pits nothing, as deep as ever.
        for (int column = 0; column < level.main; column++) {
            if (level.land[ROWS - 1][column] != BLOCK) continue;
            for (int row = ROWS; row < ROWS + LOWER; row++) level.land[row][column] = BLOCK;
        }
        int caves = 1 + (level.chapter >= CHAPTERS ? 1 : 0) + (dice.nextInt(3) == 0 ? 1 : 0);
        for (int tries = 0; tries < 160 && caves > 0; tries++) if (cave(level, dice)) caves--;
        // Under cracked ground, the places that only a ground pound gets into: laid in what room the caves leave (towards the ends of the land, which
        // is where they leave it), from numbers of their own, so that the caves and the high ledges come out of the same numbers as ever.
        crackedPlaces(level, new Random(play * 6700417L + base.number * 15485863L + 11));
        // (More heights than there were: routes over and under the way along.)
        int heights = 2 + (dice.nextInt(2) == 0 ? 1 : 0) + (level.chapter >= CHAPTERS * 2 ? 1 : 0);
        for (int tries = 0; tries < 160 && heights > 0; tries++) if (heights(level, dice)) heights--;
        return level;
    }

    /** How far from the start, and from the gate or whoever waits, a layer keeps. */
    private static int layersTo(Level level) { return (level.arena >= 0 ? level.arena : level.gate) - 12; }

    /** Whether a column is plain ground: rock at the top of it, open sky over it, and nothing there that a hole would spoil. */
    private static boolean plain(Level level, int column) {
        int floor = top(level, column);
        if (floor < 6 || floor >= ROWS) return false;
        for (int row = floor - 3; row < floor; row++) if (level.land[row][column] != AIR) return false;
        for (int row = floor; row < ROWS; row++) if (level.land[row][column] != BLOCK) return false;
        // (On flat ground, not by a pit or a step, where a jump might come down on it.)
        for (int near = column - 5; near <= column + 5; near++) if (near < 0 || near >= level.columns || top(level, near) != floor) return false;
        for (int[] item : level.items) if (Math.abs(item[1] - column) < 2) return false;
        for (int[] decor : level.decor) if (Math.abs(decor[1] - column) < 2) return false;
        for (int flag : level.flags) if (Math.abs(flag - column) < 2) return false;
        for (int[] mover : level.movers) if (Math.abs(mover[0] - column) < 3) return false;
        // (Well clear of whatever walks the land: a creature turns at a hatch, and must not be left pacing a step or two of ground.)
        for (int[] foe : level.foes) if (foe[2] < ROWS && Math.abs(foe[1] - column) < 5) return false;
        for (int near = column - 3; near <= column + 3; near++) {
            for (int row = Math.max(0, floor - 2); row < floor; row++) {
                char thing = level.land[row][Math.max(0, Math.min(level.columns - 1, near))];
                if (thing == SPIKE || thing == SPRING || thing == ENTRY || thing == GATE || thing == FLAG) return false;
            }
        }
        return true;
    }

    /** A cave under the land, if there is room for one at a place chosen by chance. */
    private static boolean cave(Level level, Random dice) {
        int wide = 24 + dice.nextInt(17), last = layersTo(level) - wide;
        if (last <= 18) return false;
        int from = 16 + dice.nextInt(last - 16), to = from + wide - 1;
        for (int column = from - 8; column <= to + 8; column++) {
            if (column < 0 || column >= level.columns || !level.under[column]) continue;
            // (A chamber under cracked ground asks only a few squares of rock between it and a cave.)
            if (!chamber(level, column) || column >= from - 3 && column <= to + 3) return false;
        }
        // (Its walls are rock under the ground, not under a pit, which would let her fall out of it.)
        if (level.land[ROWS - 1][from - 1] != BLOCK || level.land[ROWS - 1][to + 1] != BLOCK) return false;
        // Its hatch near its near end, and the vine up near its far end, each in plain ground.
        int hatch = -1, vine = -1;
        for (int column = from + 1; column < from + 10 && hatch < 0; column++) if (plain(level, column)) hatch = column;
        for (int column = to - 1; column > to - 10 && vine < 0; column--) if (plain(level, column)) vine = column;
        if (hatch < 0 || vine < 0) return false;
        int top = ROWS, bottom = CAVE_FLOOR - 1;
        // Its floor goes up and down a little; here and there the roof hangs down, and a ledge or two to climb on.
        int floor = bottom;
        int[] floors = new int[wide];
        for (int along = 0; along < wide; along++) {
            if (along > 2 && along < wide - 3 && dice.nextInt(4) == 0) floor = Math.max(bottom - 2, Math.min(bottom, floor + (dice.nextBoolean() ? 1 : -1)));
            floors[along] = floor;
            int column = from + along;
            level.under[column] = true;
            for (int row = top; row <= floors[along]; row++) level.land[row][column] = AIR;
            // (And rock under its floor, even where a pit over it went all the way down.)
            for (int row = floors[along] + 1; row < ROWS + LOWER; row++) level.land[row][column] = BLOCK;
            if (along > 3 && along < wide - 4 && dice.nextInt(7) == 0) {
                int hang = 1 + dice.nextInt(2);
                for (int row = top; row < top + hang; row++) level.land[row][column] = BLOCK;
            }
        }
        // (Its walls stand under the land's own ground at either end; under a pit at its ends there is rock to stand on.)
        for (int column = from; column <= to; column++) if (level.land[ROWS - 1][column] != BLOCK) level.land[ROWS - 1][column] = AIR;
        for (int ledge = 0; ledge < 1 + wide / 14; ledge++) {
            int at = from + 5 + dice.nextInt(Math.max(1, wide - 12)), row = floors[at - from] - 3 - dice.nextInt(2), length = 3 + dice.nextInt(3);
            for (int column = at; column < Math.min(to - 2, at + length); column++) if (level.land[row][column] == AIR) level.land[row][column] = LEDGE;
            pluses(level, at, Math.min(to - 2, at + length) - 1, row - 1);
        }
        // The hatch: a trapdoor in the ground, stood on like the ground and dropped through with the down button; a shaft under it.
        int hatchTop = top(level, hatch);
        level.land[hatchTop][hatch] = THIN;
        // (Clear all the way down to the cave's floor: nothing hangs from its roof under a hatch.)
        for (int row = hatchTop + 1; row <= floors[hatch - from]; row++) level.land[row][hatch] = AIR;
        level.ways.add(new int[]{hatch, hatchTop, 0});
        // The vine up: from the cave's floor to the ground, and a hatch at its top she climbs up through.
        int vineTop = top(level, vine);
        level.land[vineTop][vine] = THIN;
        for (int row = vineTop + 1; row <= floors[vine - from]; row++) level.land[row][vine] = VINE;
        level.ways.add(new int[]{vine, vineTop, 1});
        // What is in it: pluses along its floor, creatures walking on it, and a chest.
        for (int along = 3; along < wide - 3; along += 3 + dice.nextInt(3)) level.pluses.add(new int[]{from + along, floors[along] - 1});
        int[] walkers = walkersOf(level);
        int creatures = 2 + dice.nextInt(3) + level.chapter / 3;
        for (int one = 0; one < creatures; one++) {
            int along = 6 + dice.nextInt(Math.max(1, wide - 12));
            if (Math.abs(from + along - vine) < 3) continue;
            level.foes.add(new int[]{walkers[dice.nextInt(walkers.length)], from + along, floors[along]});
        }
        int chest = from + wide / 2 + dice.nextInt(Math.max(1, wide / 3));
        level.items.add(new int[]{PlusRun.CHEST, chest, floors[chest - from], 0, 15 + 5 * dice.nextInt(4)});
        // And at one end, more often the far one, a false wall with a better chest behind it, and a ribbon or a treat with it.
        boolean far = dice.nextInt(3) != 0;
        int wall = far ? to + 1 : from - 1, way = far ? 1 : -1;
        boolean room = wall + way * 4 > 2 && wall + way * 4 < level.main - 2;
        for (int deep = 0; deep <= 4 && room; deep++) room = level.land[ROWS - 1][wall + way * deep] == BLOCK && !level.under[wall + way * deep];
        if (room) {
            int row = floors[far ? wide - 1 : 0];
            for (int deep = 0; deep < 4; deep++) {
                int column = wall + way * deep;
                level.under[column] = true;
                for (int up = row - 2; up <= row; up++) level.land[up][column] = deep == 0 ? FAKE : AIR;
            }
            level.items.add(new int[]{PlusRun.CHEST, wall + way * 2, row, 0, 40 + 10 * dice.nextInt(3)});
            level.items.add(new int[]{secretPrize(dice), wall + way * 3, row, 0});
            level.ways.add(new int[]{wall, row, 2});
        }
        return true;
    }

    /** Something worth finding behind a false wall: a ribbon of any colour, or a treat. */
    private static int secretPrize(Random dice) {
        int[] prizes = {PlusRun.FORM + PlusRun.GREEN, PlusRun.FORM + PlusRun.YELLOW, PlusRun.FORM + PlusRun.PURPLE, PlusRun.FORM + PlusRun.BROWN, PlusRun.MAGNET, PlusRun.NOTES, PlusRun.ICE};
        return prizes[dice.nextInt(prizes.length)];
    }

    /** The creatures that walk in a land, as its own are: what walks in a cave under it. */
    private static int[] walkersOf(Level level) {
        List<Integer> kinds = new ArrayList<>();
        for (int[] foe : level.foes) if ((foe[0] == MINUS || foe[0] == TIMES || foe[0] == SLIME || foe[0] == DIVIDE || foe[0] == THROWER) && !kinds.contains(foe[0])) kinds.add(foe[0]);
        if (kinds.isEmpty()) kinds.add(MINUS);
        int[] out = new int[kinds.size()];
        for (int index = 0; index < out.length; index++) out[index] = kinds.get(index);
        return out;
    }

    /** Pluses along a row, from one column to another. */
    private static void pluses(Level level, int from, int to, int row) {
        for (int column = from; column <= to; column++) if (row > 0 && level.land[row][column] == AIR) level.pluses.add(new int[]{column, row});
    }

    /** High ledges over the land, where the sky over it is clear, if there is room for them at a place chosen by chance. */
    private static boolean heights(Level level, Random dice) {
        int wide = 14 + dice.nextInt(11), last = layersTo(level) - wide;
        if (last <= 18) return false;
        int from = 16 + dice.nextInt(last - 16), to = from + wide - 1;
        int lowest = ROWS;
        // (Clear of any other high ledges by a few squares: one run of them is not taken for the next.)
        for (int column = Math.max(0, from - 6); column <= Math.min(level.columns - 1, to + 6); column++) {
            for (int row = 0; row < 9; row++) if (level.land[row][column] == LEDGE || level.land[row][column] == VINE) return false;
        }
        for (int column = from - 2; column <= to + 2; column++) {
            int floor = top(level, column);
            if (floor >= 0) lowest = Math.min(lowest, floor);
            if (floor >= 0 && floor < 9) return false;
            for (int row = 0; row < floor - 2; row++) if (level.land[row][column] != AIR) return false;
            for (int[] way : level.ways) if (way[2] == 3 && Math.abs(way[0] - column) < 6) return false;
        }
        if (!plain(level, from)) return false;
        // A vine up from the ground to the first of them, and a ledge after another along the way, a jump apart, with pluses over each.
        int high = Math.min(7, lowest - 4);
        if (high < 3) return false;
        int row = Math.min(high, 4 + dice.nextInt(3)), column = from + 1;
        int foot = top(level, from);
        // (The vine goes up past the first ledge's top, so that she holds on there with her feet over it and steps across onto it.)
        for (int up = row - 2; up < foot; up++) level.land[up][from] = VINE;
        level.ways.add(new int[]{from, foot - 1, 3});
        while (column < to - 2) {
            int length = 3 + dice.nextInt(3);
            for (int along = 0; along < length && column + along <= to; along++) level.land[row][column + along] = LEDGE;
            pluses(level, column, Math.min(to, column + length - 1), row - 1);
            column += length + 1 + dice.nextInt(2);
            row = Math.max(3, Math.min(high, row + dice.nextInt(3) - 1));
        }
        // At the end a chest, and a spring up to one more ledge, hidden at the very top, with a better chest on it.
        int end = Math.min(to, column - 2);
        int endRow = 0;
        for (int up = 2; up < 9; up++) if (level.land[up][end] == LEDGE) endRow = up;
        if (endRow > 0) {
            level.items.add(new int[]{PlusRun.CHEST, end, endRow - 1, 0, 20});
            if (endRow - 1 >= 3 && level.land[endRow - 1][end - 1] == AIR) {
                level.land[endRow - 1][end - 1] = SPRING;
                for (int along = -1; along <= 1; along++) if (level.land[0][end - 1 + along] == AIR) level.land[1][end - 1 + along] = LEDGE;
                level.items.add(new int[]{PlusRun.CHEST, end - 1, 0, 0, 35});
                level.ways.add(new int[]{end - 1, 1, 2});
            }
        }
        return true;
    }

    // Under cracked ground

    /**
     * Places in a land that only a ground pound opens, from the second land on: one, then two from the third chapter, then three from the
     * seventh. Each is a patch of cracked ground (the strength of pound it takes grows with the chapter) over a shaft that drops into a chamber
     * under the land; and from the chamber a vine climbs back up. The first of them is a vault, then a shortcut, then a basement, and so round.
     */
    private static void crackedPlaces(Level level, Random dice) {
        if (level.number < 2 || level.under == null) return;
        int want = 1 + (level.chapter >= 2 ? 1 : 0) + (level.chapter >= 6 ? 1 : 0);
        int[] kinds = {V_VAULT, V_SHORTCUT, V_BASEMENT};
        for (int tries = 0; tries < 4000 && want > 0; tries++) {
            // (The kind that is its turn; and, if there is no room for that one, a vault, which is the smallest.)
            int kind = tries < (level.main < 130 ? 500 : 2000) ? kinds[(level.vaults.size() + level.number) % kinds.length] : V_VAULT;
            if (vault(level, dice, kind, needIn(level.chapter, dice), layersTo(level), false)) want--;
        }
    }

    /** How strong a pound the cracked ground of a chapter asks for: the first, in the first chapters; up to the third, in the later ones. */
    private static int needIn(int chapter, Random dice) { return chapter < 2 ? 1 : chapter < 5 ? 1 + dice.nextInt(2) : 1 + dice.nextInt(3); }

    /**
     * Whether ground is sound for cracked squares or a hatch: level for so many squares to either side, open sky over it, rock all the way down
     * under it, and nothing laid on or by it. (Less to ask than a cave's hatch, which wants room for a jump and for nobody to stand by it: a creature
     * that walks over cracked ground is no harm to it.)
     */
    private static boolean sound(Level level, int column, int around) {
        int floor = top(level, column);
        if (floor < 6 || floor >= ROWS) return false;
        for (int near = column - around; near <= column + around; near++) {
            if (near < 0 || near >= level.columns || top(level, near) != floor) return false;
            for (int row = floor - 2; row < floor; row++) if (level.land[row][near] != AIR) return false;
        }
        for (int row = floor; row < ROWS; row++) if (level.land[row][column] != BLOCK) return false;
        for (int[] item : level.items) if (Math.abs(item[1] - column) < 2) return false;
        for (int[] decor : level.decor) if (Math.abs(decor[1] - column) < 2) return false;
        for (int flag : level.flags) if (Math.abs(flag - column) < 2) return false;
        for (int[] mover : level.movers) if (Math.abs(mover[0] - column) < 3) return false;
        return true;
    }

    /** Whether a column is one that a chamber under cracked ground lies under. */
    private static boolean chamber(Level level, int column) {
        for (int[] vault : level.vaults) if (column >= vault[7] && column <= vault[8]) return true;
        return false;
    }

    /** Whether a column is clear of what a vault's shaft or its vine would be in the way of: doors, things to pick up, trees, other ways, other vaults. */
    private static boolean clearOf(Level level, int column) {
        for (int[] room : level.rooms) if (Math.abs(room[1] - column) < 5 || Math.abs(room[5] - column) < 5) return false;
        for (int[] door : level.doors) if (Math.abs(door[0] - column) < 3) return false;
        for (int[] weapon : level.weapons) if (Math.abs(weapon[1] - column) < 3) return false;
        for (int[] tree : level.trees) if (Math.abs(tree[0] - column) < 3) return false;
        for (int[] way : level.ways) if (Math.abs(way[0] - column) < 4) return false;
        return true;
    }

    /** A floor of a dungeon let down to as deep as a land's rows go, if it was not already: the rock under its ground, and a lower layer that nothing was in. */
    private static void deepen(Level level) {
        if (level.land.length < ROWS + LOWER) {
            char[][] deeper = new char[ROWS + LOWER][level.columns];
            for (char[] row : deeper) java.util.Arrays.fill(row, AIR);
            for (int row = 0; row < level.land.length; row++) System.arraycopy(level.land[row], 0, deeper[row], 0, level.columns);
            level.land = deeper;
        }
        if (level.under == null) level.under = new boolean[level.columns];
    }

    /**
     * One place under cracked ground, if there is room for it where chance puts it. A chamber lies under the land, its floor at the foot of the
     * lower layer; its shaft comes up near its near end to cracked ground (a stack of squares as deep as the pound it takes is strong, and a
     * patch of cracked squares to either side of it, wider for a stronger one), and a vine goes up near its far end to a hatch that cannot be
     * dropped through from above (so the way in is the crack and only the crack). Nothing in it can hurt her but what walks there.
     */
    private static boolean vault(Level level, Random dice, int kind, int need, int limit, boolean dungeon) {
        // The shaft goes where there is plain ground, and the vine comes up in plain ground a way along: so far for a vault, further for a basement, further still for a shortcut.
        int span = kind == V_SHORTCUT ? 18 + dice.nextInt(7) : kind == V_BASEMENT ? 11 + dice.nextInt(4) : 8 + dice.nextInt(2);
        int last = limit - span - 3;
        if (last <= 22) return false;
        int shaft = 20 + dice.nextInt(last - 20), exit = -1;
        // The cracked patch: deepest over the shaft, a single layer of it to either side (wider for a stronger pound), on ground level a square past it.
        int side = need >= 3 ? 2 : need == 2 ? 1 : dice.nextInt(2);
        if (!sound(level, shaft, side + (dungeon ? 0 : 1)) || !clearOf(level, shaft)) return false;
        for (int nudge = 0; nudge <= 2 && exit < 0; nudge++) {
            for (int sign = -1; sign <= 1 && exit < 0; sign += 2) {
                int column = shaft + span + sign * nudge;
                if (sound(level, column, 1) && clearOf(level, column)) exit = column;
            }
        }
        if (exit < 0) return false;
        int from = shaft - 2, to = exit + 2, wide = to - from + 1;
        // (Clear of any cave or other chamber by a few squares; under rock all along, and not under a pit.)
        for (int column = Math.max(0, from - 2); column <= Math.min(level.columns - 1, to + 2); column++) if (level.under != null && level.under[column]) return false;
        for (int column = from - 1; column <= to + 1; column++) if (level.land[ROWS - 1][column] != BLOCK) return false;
        deepen(level);
        int ground = top(level, shaft), hatch = top(level, exit), stack = need >= 2 ? 2 : 1;
        int floor = CAVE_FLOOR - 1, high = kind == V_SHORTCUT ? 3 : 4, ceiling = floor - high + 1;
        // Rock under all of it, and the chamber cut out of the rock.
        for (int column = from - 1; column <= to + 1; column++) for (int row = ROWS; row < ROWS + LOWER; row++) level.land[row][column] = BLOCK;
        for (int column = from; column <= to; column++) {
            level.under[column] = true;
            for (int row = ceiling; row <= floor; row++) level.land[row][column] = AIR;
        }
        // The shaft: clear from under the cracked squares down into the chamber.
        for (int row = ground + stack; row < ceiling; row++) level.land[row][shaft] = AIR;
        // The vine up, from the chamber's floor to a hatch in the ground.
        for (int row = hatch + 1; row <= floor; row++) level.land[row][exit] = VINE;
        level.land[hatch][exit] = THIN;
        // The cracked ground.
        char crack = need >= 3 ? CRACK3 : need == 2 ? CRACK2 : CRACK;
        for (int column = shaft - side; column <= shaft + side; column++) {
            for (int row = ground; row < ground + (column == shaft ? stack : 1); row++) level.land[row][column] = crack;
        }
        level.vaults.add(new int[]{kind, shaft, ground, shaft - side, shaft + side, stack, need, from, to, floor, exit, hatch});
        level.ways.add(new int[]{shaft, ground, WAY_SHAFT});
        level.ways.add(new int[]{exit, hatch, WAY_CLIMB});
        // What is in it.
        int chapter = level.chapter, rich = 10 * need + 5 * dice.nextInt(3);
        if (kind == V_VAULT) {
            // Two chests, one of them a rich one, and something rare beside them; pluses between.
            level.items.add(new int[]{PlusRun.CHEST, from + wide / 2, floor, 0, 50 + rich + 4 * chapter});
            level.items.add(new int[]{PlusRun.CHEST, from + wide / 2 + 2, floor, 0, 25 + rich});
            if (!dungeon) level.items.add(new int[]{secretPrize(dice), from + wide / 2 - 2, floor, 0});
            for (int along = from + 2; along <= to - 2; along += 2) if (Math.abs(along - shaft) > 1 && Math.abs(along - exit) > 1) level.pluses.add(new int[]{along, floor - 1});
        } else if (kind == V_SHORTCUT) {
            // A long low way under the land, with pluses along it and one chest half way; it comes up a good way further on.
            for (int along = shaft + 3; along <= exit - 2; along += 2) level.pluses.add(new int[]{along, floor - 1});
            level.items.add(new int[]{PlusRun.CHEST, (shaft + exit) / 2, floor, 0, 30 + rich});
        } else if (kind == V_BASEMENT) {
            // A few creatures walking it, and at the far end the chest they guard.
            int[] walkers = walkersOf(level);
            int creatures = 3 + chapter / 4 + dice.nextInt(2);
            for (int one = 0; one < Math.min(5, creatures); one++) {
                int along = shaft + 4 + (exit - shaft - 6) * one / Math.max(1, Math.min(5, creatures) - 1);
                level.foes.add(new int[]{walkers[dice.nextInt(walkers.length)], Math.max(from + 4, Math.min(to - 3, along)), floor});
            }
            level.items.add(new int[]{PlusRun.CHEST, Math.min(to - 1, exit + 2), floor, 0, 60 + rich + 3 * chapter});
            for (int along = shaft + 2; along < exit; along += 4) level.pluses.add(new int[]{along, floor - 1});
        } else {
            // A cache, in a dungeon: one chest, marked, that may hold a blessing or a relic; and a few pluses by it.
            int middle = from + wide / 2;
            level.items.add(new int[]{PlusRun.CHEST, middle, floor, 0, 60 + rich, PlusRun.CACHE});
            for (int along = middle - 3; along <= middle + 3; along += 2) if (along != middle) level.pluses.add(new int[]{along, floor - 1});
        }
        return true;
    }

    /**
     * The places under cracked ground on a floor of a dungeon: from the second floor, a cache with a chest that may hold a blessing or a relic,
     * and, from the fourth, a vault as well. Drawn from numbers of their own, so that the floor is as it was. (Not in the mirror.)
     */
    private static void crackedCaches(Level level, int dungeon, int floor, int hall, long dive) {
        if (floor < 1 || dungeon == RunDungeons.MIRROR) return;
        Random dice = new Random(dive * 15485863L + dungeon * 104729L + floor * 31L + 5);
        int limit = (hall >= 0 ? hall : level.gate) - 12, need = Math.min(3, 1 + floor / 2);
        for (int tries = 0; tries < 1500; tries++) if (vault(level, dice, V_CACHE, need, limit, true)) break;
        if (floor >= 3) for (int tries = 0; tries < 1500; tries++) if (vault(level, dice, V_VAULT, need, limit, true)) break;
    }

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
        Level draft = new Level(number, 1700, DEEP);
        Builder builder = new Builder(draft, random, 1, chapter);
        builder.ground(6);
        // (Longer as they go: eighteen stretches in the thirty-first, twenty-seven in the seventy-ninth.)
        int stretches = boss >= 0 ? 12 : 18 + (number - COUNT - 1) / 5, last = -1;
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
            // Its hideout, in the lands that have one.
            if (stretch == stretches * 3 / 8 && hideoutIn(number)) builder.hideoutDoor(builder.at - 3);
            if (stretch == stretches - 2 && lagoonIn(number)) builder.door(LAGOON, builder.at - 3, false);
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
        // (Past the gate, the way on to the next land.)
        builder.ground(PAST_GATE);
        draft.main = builder.at;
        builder.rooms();
        builder.asked(draft.gate, gateFloor);
        boolean deep = false;
        for (int[] room : draft.rooms) deep |= room[0] == ARENA;
        Level level = new Level(number, builder.at, deep ? DEEP : ROWS);
        level.main = draft.main;
        level.rooms.addAll(draft.rooms);
        level.spawns.addAll(draft.spawns);
        level.lofts.addAll(draft.lofts);
        level.made.addAll(draft.made);
        for (int row = 0; row < level.land.length; row++) System.arraycopy(draft.land[row], 0, level.land[row], 0, level.columns);
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
        level.town = draft.town;
        level.tiered = true;
        armLater(level, new Random(seed * 104729 + number * 31L));
        level.foes.addAll(draft.lodgers);
        rove(level);
        flock(level);
        tempt(level);
        crowd(level);
        shelve(level);
        kennel(level);
        arsenal(level);
        pots(level);
        friend(level);
        swarm(level);
        uniques(level);
        plant(level);
        stairs(level);
        return level;
    }

    /** The row of the top of the ground at a column: of what stands on the bottom of the land there, whatever floats over it. None, -1, over a pit. */
    /** How far the land's usual ground must be from the wood's before the wood moves, and how much of the view's going up and down it follows. */
    static final float WOOD_STILL = 1.6f, WOOD_FOLLOWS = .5f;

    /** The land's usual ground about a column: the middle of the heights of the ground over twelve columns each way, leaving out where there is none. */
    static float usualGround(RunLevels.Level level, int middle) {
        int[] heights = new int[25];
        int count = 0;
        for (int column = middle - 12; column <= middle + 12; column++) {
            if (column < 0 || column >= level.columns) continue;
            int top = RunLevels.top(level, column);
            if (top < 0 || top >= RunLevels.ROWS - 1) continue;
            heights[count++] = top;
        }
        if (count == 0) return -1;
        java.util.Arrays.sort(heights, 0, count);
        return heights[count / 2];
    }

    static int top(Level level, int column) {
        int row = ROWS;
        // (Cracked ground is ground: it is only that a pound breaks it.)
        while (row > 0 && (level.land[row - 1][column] == BLOCK || crackNeed(level.land[row - 1][column]) > 0)) row--;
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
                PlusRun.BLACK, PlusRun.BLACK, PlusRun.BLACK};
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
        Level draft = new Level(number, 1500, DEEP);
        Builder builder = new Builder(draft, random, hard, chapter);
        builder.ground(6);
        int boss = bossOf(number);
        boolean shaped = number >= SHAPED_FROM;
        // (From the twenty-first on, three stretches more than the count of the lands before would give.)
        int stretches = boss >= 0 ? (shaped ? 8 : 6) : 6 + number / 3 + (number >= FLIERS_FROM ? 3 : 0);
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
            // Its hideout, half way along, in the lands that have one; and its lagoon, near the end.
            if (stretch == stretches / 2 && hideoutIn(number)) {
                builder.hideoutDoor(builder.at - 3);
            }
            if (stretch == stretches - 2 && lagoonIn(number)) builder.door(LAGOON, builder.at - 3, false);
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
        // (Past the gate, the way on to the next land.)
        builder.ground(PAST_GATE);
        draft.main = builder.at;
        builder.rooms();
        builder.asked(draft.gate, gateFloor);
        boolean deep = false;
        for (int[] room : draft.rooms) deep |= room[0] == ARENA;
        Level level = new Level(number, builder.at, deep ? DEEP : ROWS);
        level.main = draft.main;
        level.rooms.addAll(draft.rooms);
        level.spawns.addAll(draft.spawns);
        level.lofts.addAll(draft.lofts);
        level.made.addAll(draft.made);
        for (int row = 0; row < level.land.length; row++) System.arraycopy(draft.land[row], 0, level.land[row], 0, level.columns);
        level.land[gateFloor - 1][draft.gate] = GATE;
        level.land[gateFloor - 2][draft.gate] = GATE;
        level.pluses.addAll(draft.pluses);
        level.foes.addAll(draft.foes);
        level.flags.addAll(draft.flags);
        level.items.addAll(draft.items);
        level.decor.addAll(draft.decor);
        level.weapons.addAll(draft.weapons);
        level.movers.addAll(draft.movers);
        level.gate = draft.gate;
        level.arena = draft.arena;
        level.guarded = draft.guarded;
        level.town = draft.town;
        level.tiered = true;
        arm(level, new Random(seed * 104729 + number * 31L));
        // The signs that walk in the places off the land join the rest only now, so that what the land is given is what it was given without them.
        level.foes.addAll(draft.lodgers);
        rove(level);
        flock(level);
        tempt(level);
        crowd(level);
        shelve(level);
        kennel(level);
        arsenal(level);
        pots(level);
        friend(level);
        swarm(level);
        uniques(level);
        plant(level);
        stairs(level);
        return level;
    }

    // The dungeons

    /**
     * A floor of a dungeon as it is laid for one dive (from the dive's own number, so that every dive is new): made of what its land's
     * chapter is made of, shorter than a land, with a flag half way and no village, nothing asked and no one waiting; its own creatures
     * along it (twice as many on a teeming floor, few on a quiet one, with bread to mend), a chest, pluses (more on a gilded floor); and on
     * the last floor a hall before the way out where its keeper stands. It is drawn as its land is, and as hard as its land, more deeply
     * down, as {@link RunDungeons#deeper} says.
     */
    static Level dungeon(int dungeon, int floor, long dive) {
        int number = RunDungeons.land(dungeon), chapter = chapterOf(number), sort = RunDungeons.kindOf(dungeon, floor, dive);
        boolean last = floor >= RunDungeons.floors(dungeon) - 1;
        Random random = new Random(dive * 50021L + dungeon * 7919L + floor * 104729L + 17);
        float hard = number > COUNT ? 1 : (number - 1) / (float) (COUNT - 1);
        Level draft = new Level(number, 1400, DEEP);
        Builder builder = new Builder(draft, random, hard, chapter);
        builder.ground(6);
        int stretches = 12 + floor + (number > COUNT ? 6 : 0), previous = -1;
        for (int stretch = 0; stretch < stretches; stretch++) {
            int piece;
            do {
                if (number > COUNT) {
                    int from = random.nextInt(10);
                    int[] pool = LATER_POOLS[chapter - CHAPTERS], shapes = LATER_SHAPES[chapter - CHAPTERS];
                    piece = from < 3 ? shapes[random.nextInt(shapes.length)] : from < 5 ? MOBS[random.nextInt(MOBS.length)] : pool[random.nextInt(pool.length)];
                } else {
                    int[] pool = chapter < EARLY_POOLS.length ? EARLY_POOLS[chapter] : HIGH_POOL;
                    piece = pool[random.nextInt(pool.length)];
                }
            } while (piece == previous || number <= 2 && (piece == BRIDGE || piece == HIGH_ROAD));
            previous = piece;
            builder.stretch(piece);
            if (stretch == stretches / 2) builder.flag();
            builder.rest();
        }
        // The keeper's hall: flat, and long enough to fight in.
        int hall = -1, hallFloor = FLOOR;
        if (last) {
            builder.ground(4);
            hall = builder.at;
            hallFloor = builder.floor;
            builder.ground(KEEPER_HALL);
        }
        builder.ground(3);
        draft.gate = builder.at;
        int gateFloor = builder.floor;
        builder.ground(PAST_GATE);
        draft.main = builder.at;
        builder.rooms();
        boolean deep = false;
        for (int[] room : draft.rooms) deep |= room[0] == ARENA;
        Level level = new Level(number, builder.at, deep ? DEEP : ROWS);
        level.main = draft.main;
        level.quest = 0;
        level.rooms.addAll(draft.rooms);
        level.spawns.addAll(draft.spawns);
        level.lofts.addAll(draft.lofts);
        level.made.addAll(draft.made);
        for (int row = 0; row < level.land.length; row++) System.arraycopy(draft.land[row], 0, level.land[row], 0, level.columns);
        level.land[gateFloor - 1][draft.gate] = GATE;
        level.land[gateFloor - 2][draft.gate] = GATE;
        level.pluses.addAll(draft.pluses);
        level.foes.addAll(draft.foes);
        level.flags.addAll(draft.flags);
        level.items.addAll(draft.items);
        level.weapons.addAll(draft.weapons);
        level.movers.addAll(draft.movers);
        level.doors.addAll(draft.doors);
        level.gate = draft.gate;
        level.tiered = true;
        // Something to fight with by where she comes down, and by the flag.
        level.weapons.add(new int[]{(number + floor + dungeon) % PlusRun.GUN, 4, FLOOR - 1});
        for (int flag : level.flags) level.weapons.add(new int[]{(number + floor + dungeon + 3) % PlusRun.GUN, flag + 1, top(level, flag + 1) - 1});
        level.foes.addAll(draft.lodgers);
        delve(level, dungeon, floor, sort, hall, hallFloor, random);
        crackedCaches(level, dungeon, floor, hall, dive);
        return level;
    }

    /** How long the keeper's hall is. */
    static final int KEEPER_HALL = 26;

    /** Where on a floor something of a dungeon may stand: flat ground with room over it, clear of the start, the flag, the doors and what lies about. */
    private static List<int[]> standings(Level level, int until) {
        List<int[]> spots = new ArrayList<>();
        for (int column = 16; column < until; column++) {
            boolean near = false;
            for (int flag : level.flags) near |= Math.abs(flag - column) < 5;
            for (int[] room : level.rooms) near |= Math.abs(room[1] - column) < 4 || Math.abs(room[5] - column) < 4;
            for (int[] item : level.items) if (item[1] < level.main) near |= Math.abs(item[1] - column) < 3;
            for (int[] weapon : level.weapons) near |= Math.abs(weapon[1] - column) < 2;
            if (near) continue;
            int floor = top(level, column);
            boolean flat = floor > 7 && floor < level.land.length && level.land[floor][column] == BLOCK && top(level, column + 1) == floor && top(level, column - 1) == floor;
            for (int row = floor - 4; row < floor && flat; row++) flat = level.land[row][column] == AIR && level.land[row][column - 1] == AIR && level.land[row][column + 1] == AIR;
            if (flat) spots.add(new int[]{column, floor});
        }
        return spots;
    }

    /** What lives on a floor of a dungeon: its own creatures, now and then one of its chapter's; a chest; and what the floor is like. */
    private static void delve(Level level, int dungeon, int floor, int sort, int hall, int hallFloor, Random random) {
        int until = hall >= 0 ? hall - 4 : level.gate - 6;
        List<int[]> spots = standings(level, until);
        int want = 5 + floor * 2 + level.number / 12;
        if (sort == RunDungeons.TEEMING) want *= 2;
        if (sort == RunDungeons.QUIET) want = Math.max(2, want / 3);
        int[] kinds = RunDungeons.delvers(dungeon);
        int chapterFirst = RunUniques.first(Math.min(11, level.chapter));
        List<Integer> taken = new ArrayList<>();
        for (int tries = 0; tries < 300 && want > 0 && !spots.isEmpty(); tries++) {
            int[] spot = spots.get(random.nextInt(spots.size()));
            boolean clear = true;
            for (int column : taken) clear &= Math.abs(column - spot[0]) >= (sort == RunDungeons.TEEMING ? 5 : 7);
            if (!clear) continue;
            int kind = kinds[random.nextInt(kinds.length)];
            // (One in five is one of its chapter's own; never its mini-boss, nor its hornets.)
            // (Never in the mirror: only her shadows live there.)
            if (random.nextInt(5) == 0 && dungeon != RunDungeons.MIRROR) {
                int own = chapterFirst + random.nextInt(2), status = RunUniques.statusOf(Math.min(11, level.chapter));
                // (And in the first six chapters' dungeons, every other time, one of its two made of fire, frost and ink.)
                if (status >= 0 && spot[0] % 2 == 0) own = status + own - chapterFirst;
                if (RunUniques.way(own) != RunUniques.MINIBOSS && !RunUniques.flies(own)) kind = own;
            }
            int way = RunUniques.way(kind);
            // The spider hangs over the way from something high enough to hang from: room over it, five squares at least.
            if (way == RunUniques.DROPPER && (spot[1] < 9 || level.land[spot[1] - 6][spot[0]] != AIR)) continue;
            int row = RunUniques.floats(kind) ? spot[1] - 3 : spot[1] - 1;
            level.foes.add(new int[]{kind, spot[0], row, 1, 0, 2});
            taken.add(spot[0]);
            want--;
        }
        // A chest, somewhere along it; on a gilded floor more pluses, in lines over the ground.
        if (!spots.isEmpty()) {
            int[] spot = spots.get(random.nextInt(spots.size()));
            level.items.add(new int[]{PlusRun.CHEST, spot[0], spot[1] - 1, 0, 20 + floor * 5});
        }
        if (sort == RunDungeons.GILDED) {
            for (int line = 0; line < 4 && !spots.isEmpty(); line++) {
                int[] spot = spots.get(random.nextInt(spots.size()));
                pluses(level, spot[0] - 2, spot[0] + 2, spot[1] - 3);
            }
        }
        if (sort == RunDungeons.QUIET) {
            for (int loaf = 0; loaf < 2 && !spots.isEmpty(); loaf++) {
                int[] spot = spots.get(random.nextInt(spots.size()));
                level.items.add(new int[]{PlusRun.BREAD, spot[0], spot[1] - 1, 0});
            }
        }
        // In the mirror: on its third, sixth, ninth and twelfth floors the shadow of one she knows, by turns; on its fifth and tenth an Echo, a her
        // from a timeline that ended badly. Two thirds of the way along, where she meets it at the end of what she has come through.
        if (dungeon == RunDungeons.MIRROR && !spots.isEmpty() && (floor % 3 == 2 && floor < 12 || floor == 4 || floor == 9)) {
            int kind = floor == 4 || floor == 9 ? RunUniques.ECHO_FIRST + random.nextInt(RunUniques.ECHO_COUNT) : RunUniques.CAST_FIRST + random.nextInt(RunUniques.CAST_COUNT);
            int[] spot = spots.get(Math.min(spots.size() - 1, spots.size() * 2 / 3));
            int row = RunUniques.floats(kind) ? spot[1] - 3 : spot[1] - 1;
            level.foes.add(new int[]{kind, spot[0], row, 1, 0, 2});
        }
        // The keeper, in the middle of its hall; and pluses along the hall for the fight.
        if (hall >= 0) {
            level.foes.add(new int[]{RunDungeons.keeper(dungeon), hall + KEEPER_HALL / 2 + 2, hallFloor - 1, 1, 0, 2});
            pluses(level, hall + 2, hall + 5, hallFloor - 3);
            pluses(level, hall + KEEPER_HALL - 6, hall + KEEPER_HALL - 3, hallFloor - 3);
        }
    }

    /**
     * The stairway down into a land's dungeon, if it has one: on flat, open ground about two fifths of the way along, clear of
     * everything else. It is put in last, so that the land is as it was.
     */
    private static void stairs(Level level) {
        if (RunDungeons.inLand(level.number) < 0) return;
        int until = (level.arena >= 0 ? level.arena : level.gate) - 10, want = level.main * 2 / 5, best = -1;
        // (Well clear of everything if it can be; if not, clear of what stands where it would.)
        for (int pass = 0; pass < 2 && best < 0; pass++) {
            int room_ = pass == 0 ? 1 : 0;
            for (int column = 12; column < until; column++) {
                int floor = top(level, column);
                boolean clear = floor > 5 && top(level, column - 1) == floor && top(level, column + 1) == floor;
                for (int row = floor - 4; row < floor && clear; row++) clear = level.land[row][column] == AIR && level.land[row][column - 1] == AIR && level.land[row][column + 1] == AIR;
                for (int flag : level.flags) clear &= Math.abs(flag - column) > 2 + room_ * 2;
                for (int[] thing : level.decor) clear &= Math.abs(thing[1] - column) > 2 + room_ * 4;
                for (int[] room : level.rooms) clear &= Math.abs(room[1] - column) > 2 + room_ && Math.abs(room[5] - column) > 2 + room_;
                for (int[] item : level.items) clear &= item[1] >= level.main || Math.abs(item[1] - column) > 1 + room_;
                for (int[] weapon : level.weapons) clear &= Math.abs(weapon[1] - column) > 1 + room_;
                for (int[] tree : level.trees) clear &= Math.abs(tree[0] - column) > 1 + room_;
                for (int[] foe : level.foes) clear &= foe[1] >= level.main || Math.abs(foe[1] - column) > 1 + room_ * 2;
                for (int[] way : level.ways) clear &= Math.abs(way[0] - column) > 2 + room_;
                if (clear && (best < 0 || Math.abs(column - want) < Math.abs(best - want))) best = column;
            }
        }
        if (best >= 0) level.items.add(new int[]{PlusRun.SPOT, best, top(level, best) - 1, 0, PlusRun.STAIRS});
    }

    /** The column of a land's stairway down, or -1. */
    static int stairsOf(Level level) {
        for (int[] item : level.items) if (item[0] == PlusRun.SPOT && item.length > 4 && item[4] == PlusRun.STAIRS) return item[1];
        return -1;
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
        /** The doors of the buildings of its villages: laid after every other place, so that the places a land had before them keep their numbers. */
        final List<int[]> built = new ArrayList<>();
        private int villages;
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
                for (int[] room : level.rooms) if (room[0] != HOUSE) nooks.add(new int[]{room[3] + 5, room[11] - 3});
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
                int kind = want[0], wide = kind == PASSAGE ? 46 : kind == ARENA ? ARENA_WIDE : 30;
                if (kind == ARENA) {
                    arena(want, den);
                    continue;
                }
                if (kind == STASH || kind == PASSAGE) {
                    secret(want);
                    continue;
                }
                // (A hideout is laid after the rest, so that the places a land had before there were hideouts keep their numbers.)
                if (kind == HIDEOUT || kind == LAGOON) continue;
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
                if (kind == GAME) {
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
                level.rooms.add(new int[]{kind, want[1], want[2], from, to, want[4], from + 1, want[3], far, want[5], 0, FLOOR, ceiling, lookOf(number, level.rooms.size()), 0});
            }
            for (int[] want : wanted) if (want[0] == HIDEOUT) hideout(want);
            for (int[] want : wanted) if (want[0] == LAGOON) lagoon(want);
            for (int[] want : built) house(want);
        }

        /**
         * A hideout: a long hall with ledges at two heights, its hidden boss at the far end of it, waiting.
         */
        void hideout(int[] want) {
            int number = level.number, ceiling = FLOOR - ROOM_TALL;
            wall(2);
            int from = at;
            open(36);
            int to = at - 1;
            wall(2);
            level.land[FLOOR - 1][from + 1] = ENTRY;
            ledge(from + 7, FLOOR - 3, 4);
            ledge(from + 16, FLOOR - 5, 4);
            ledge(to - 10, FLOOR - 3, 4);
            level.foes.add(new int[]{RunUniques.hiddenIn(number), to - 5, FLOOR - 1, 1, 0, 2});
            pluses(from + 3, from + 6, FLOOR - 1);
            level.rooms.add(new int[]{HIDEOUT, want[1], want[2], from, to, want[4], from + 1, want[3], -1, want[5], 0, FLOOR, ceiling, -1, 0});
        }

        /**
         * A lagoon: a dry landing by the door, with the blue ribbon on it; then deep water to the far wall, ten rows of it, with pillars of
         * coral standing up and hanging down, pluses all through it, a chest on the bottom at the far end, and creatures that swim.
         */
        void lagoon(int[] want) {
            int number = level.number, ceiling = FLOOR - ROOM_TALL;
            wall(2);
            int from = at;
            open(40);
            int to = at - 1;
            wall(2);
            level.land[FLOOR - 1][from + 1] = ENTRY;
            for (int column = from + 6; column <= to; column++) for (int row = ceiling + 3; row < FLOOR; row++) level.land[row][column] = WATER;
            // Coral: standing up from the bottom, and hanging from the top.
            for (int row = FLOOR - 4; row < FLOOR; row++) level.land[row][from + 14] = BLOCK;
            for (int row = ceiling + 3; row < ceiling + 7; row++) level.land[row][from + 22] = BLOCK;
            for (int row = FLOOR - 5; row < FLOOR; row++) level.land[row][from + 30] = BLOCK;
            level.items.add(new int[]{PlusRun.FORM + PlusRun.BLUE, from + 3, FLOOR - 1, 0});
            pluses(from + 8, from + 12, FLOOR - 6);
            pluses(from + 16, from + 20, FLOOR - 2);
            pluses(from + 24, from + 28, ceiling + 8);
            pluses(from + 32, from + 36, FLOOR - 7);
            level.items.add(new int[]{PlusRun.CHEST, to - 2, FLOOR - 1, 0, 60});
            // Its creatures, of the four that swim, more of them further on.
            int many = 3 + Math.min(4, number / 25);
            for (int one = 0; one < many; one++) {
                int kind = RunUniques.WATER_FIRST + (number + one) % RunUniques.WATER_COUNT;
                int column = from + 10 + one * (to - from - 14) / many, row = ceiling + 5 + (one * 3 + number) % 6;
                while (level.land[row][column] != WATER && column < to - 1) column++;
                level.foes.add(new int[]{kind, column, row, 1, 0, 2});
            }
            level.rooms.add(new int[]{LAGOON, want[1], want[2], from, to, want[4], from + 1, want[3], -1, want[5], 0, FLOOR, ceiling, -1, 0});
        }

        /** Its door: up on a high ledge, out of reach of any jump but the third or a dash; or behind a strong crack in the ground's wall. */
        void hideoutDoor(int column) {
            if (hideoutHigh(level.number)) {
                // (Six rows up: past a double jump, within a third.)
                ledge(column, floor - 6, 3);
                door(HIDEOUT, column + 1, 2, floor - 6);
            } else {
                door(HIDEOUT, column, 1, floor);
            }
        }

        /**
         * The inside of a building of a village. A pet shop: the ten pets, five along its
         * floor and five on a shelf over them, each to be walked up to; and Nabi, who keeps
         * it, at its far end. The guild hall: Inju at the counter, and her board of hunts. The smithy: Moru, and in
         * the larger villages her two sisters, each at an anvil. A home: someone in, with
         * something for her, and a few pluses on the floor.
         */
        void house(int[] want) {
            int number = level.number, sort = want[6], ceiling = FLOOR - ROOM_TALL, owner = -1;
            wall(2);
            int from = at;
            open(sort == PET_SHOP ? 19 : sort == GUILD_HALL || sort == MARKET || sort == BOUTIQUE ? 16 : sort == SMITHY ? 18 : sort == HOME ? 19 : 14);
            int to = at - 1;
            wall(2);
            level.land[FLOOR - 1][from + 1] = ENTRY;
            if (sort == PET_SHOP) {
                // Nabi at her counter by the door, looking into the shop; and her pets past her, along the floor and on the shelf.
                ledge(from + 7, FLOOR - 2, 10);
                for (int pet = 0; pet < RunPets.FOUND; pet++) level.items.add(new int[]{PlusRun.SPOT, from + 7 + pet % 5 * 2, pet < 5 ? FLOOR - 1 : FLOOR - 3, 0, pet});
                level.items.add(new int[]{PlusRun.SPOT, from + 2, FLOOR - 1, NABI, 100 + 4});
            } else if (sort == GUILD_HALL) {
                level.items.add(new int[]{PlusRun.SPOT, from + 11, FLOOR - 1, INJU, 100});
                // And her board of hunts, between the door and her desk.
                level.items.add(new int[]{PlusRun.SPOT, from + 7, FLOOR - 1, 0, PlusRun.BOARD});
                pluses(from + 4, from + 6, FLOOR - 1);
            } else if (sort == GACHA) {
                // The machine, in the middle of it.
                level.items.add(new int[]{PlusRun.SPOT, from + 7, FLOOR - 1, 0, PlusRun.MACHINE});
            } else if (sort == BOUTIQUE) {
                // Tulle at her counter, in the middle of it; the vanity boutique is opened there.
                level.items.add(new int[]{PlusRun.SPOT, from + 8, FLOOR - 1, 0, PlusRun.BOUTIQUE});
                pluses(from + 3, from + 5, FLOOR - 1);
            } else if (sort == MARKET) {
                // Whoever deals there, at the back of it.
                level.items.add(new int[]{PlusRun.SPOT, from + 8, FLOOR - 1, 0, PlusRun.DEALER});
            } else if (sort == SMITHY) {
                // Moru at her anvil; and, where the village is large enough to keep all three, her sisters, each at her own.
                level.items.add(new int[]{PlusRun.SPOT, from + 5, FLOOR - 1, 0, PlusRun.SMITH});
                if (number >= SISTERS_FROM) {
                    level.items.add(new int[]{PlusRun.SPOT, from + 9, FLOOR - 1, 0, PlusRun.SMITH + 1});
                    level.items.add(new int[]{PlusRun.SPOT, from + 13, FLOOR - 1, 0, PlusRun.SMITH + 2});
                }
            } else {
                // (Not whoever has something to ask of her in this land: she is out in the village. Nor whoever keeps the building next door.)
                int who = SOMI + (number * 3 + level.rooms.size()) % (ALTTEUL - SOMI);
                for (int turn = 0; turn < ALTTEUL - SOMI; turn++) {
                    boolean keeps = false;
                    for (int[] other : built) {
                        int sort2 = other[6];
                        keeps |= who == (sort2 == PET_SHOP ? NABI : sort2 == GUILD_HALL ? INJU : sort2 == SMITHY ? MORU : -1);
                    }
                    boolean asks = level.quest != 0 && who == giverOf(level.quest);
                    boolean outside = false;
                    for (int[] item : level.items) outside |= item[3] == who && item[0] != PlusRun.STALL && item[1] < at;
                    if (!keeps && !asks && !outside) break;
                    who = SOMI + (who - SOMI + 1) % (ALTTEUL - SOMI);
                }
                level.items.add(new int[]{GIVES[who - SOMI], from + 8, FLOOR - 1, who});
                pluses(from + 3, from + 6, FLOOR - 1);
                owner = who - SOMI;
                // And its secret, if it has one: a false wall at its far end with a little room behind it, a chest and a diary page in it; a loose
                // floorboard with a chest under it; or a diary page left out.
                int secret = homeSecret(number, level.rooms.size()), page = Math.floorMod(number * 31 + level.rooms.size() * 17 + number / 7, DIARY_PAGES);
                if (secret == 0) {
                    for (int row = FLOOR - 3; row < FLOOR; row++) level.land[row][to - 3] = FAKE;
                    for (int column = to - 2; column <= to; column++) for (int row = FLOOR - 3; row < FLOOR; row++) level.land[row][column] = AIR;
                    level.items.add(new int[]{PlusRun.CHEST, to - 1, FLOOR - 1, 0, 45});
                    level.items.add(new int[]{PlusRun.DIARY, to, FLOOR - 1, 0, page});
                } else if (secret == 1) {
                    level.items.add(new int[]{PlusRun.LOOSE, from + 13, FLOOR - 1, 0, 30});
                } else if (secret == 2) {
                    level.items.add(new int[]{PlusRun.DIARY, from + 12, FLOOR - 1, 0, page});
                }
                // And what is dear to whoever lives there, to be walked up to and looked at.
                level.items.add(new int[]{PlusRun.SPOT, from + 11, FLOOR - 1, 0, PlusRun.KEEPSAKE + owner});
            }
            int look = sort == HOME ? HOME_LOOK + Math.floorMod(number * 3 + level.rooms.size(), HOME_LOOKS) : sort == BOUTIQUE ? HOME_LOOK + 1 : LOOKS + sort;
            // (A home's sixteenth number is whoever lives in it, from Somi; -1 for any other building.)
            level.rooms.add(new int[]{HOUSE, want[1], want[2], from, to, want[4], from + 1, 0, -1, want[5], 0, FLOOR, ceiling, look, sort, owner});
        }

        /** Some columns of a place off the land: floor under them, ceiling over them, and nothing between. */
        private void open(int columns) {
            for (int column = at; column < at + columns; column++) {
                for (int row = 0; row < ROWS; row++) if (row < FLOOR - ROOM_TALL || row >= FLOOR) level.land[row][column] = BLOCK;
            }
            at += columns;
        }

        /** Blocks standing on the floor of a place, so many high. */
        private void rise(int column, int high) {
            for (int up = 1; up <= high; up++) level.land[FLOOR - up][column] = BLOCK;
        }

        /**
         * A stash or a passage: several screens long, put together from pieces chosen and
         * ordered by chances of its own, so that no two lands' are alike. A stash is four or
         * five pieces and then a chamber, where the chest stands on a shelf to be climbed or
         * sprung to, something guards it from the second chapter on, and there is one thing
         * more: something to help, a pet that waits, or a shrine. A passage is six or seven
         * pieces, with a chest by its far door and a ribbon up out of the way. There is a
         * puzzle in it at most, two in the second volume: a locked door, a bridge to raise, or
         * bars to lift. The first chapter's have no spikes in them, as its lands have none.
         */
        void secret(int[] want) {
            // (Where a land has two places of a kind, the second is laid and looks otherwise than the first.)
            int number = level.number, kind = want[0], ceiling = FLOOR - ROOM_TALL, which = level.rooms.size(), look = lookOf(number, which);
            boolean second = number > COUNT, through = kind == PASSAGE;
            Random dice = new Random(number * 7477L + kind * 131 + 17 + which * 7919L);
            int count = through ? 6 + dice.nextInt(2) : 4 + dice.nextInt(2), puzzles = 0, used = 0;
            int[] plan = new int[count];
            for (int index = 0; index < count; index++) {
                int piece;
                boolean puzzle;
                do {
                    piece = dice.nextInt(PIECES);
                    puzzle = piece == LOCK || piece == SPAN_PIT || piece == BARRED;
                } while ((used >> piece & 1) != 0 || puzzle && puzzles >= (second ? 2 : 1) || piece == LOCK && through
                        || chapter == 0 && (piece == CRUMBLING || piece == RIDE || piece == SPAN_PIT || piece == BARRED));
                if (puzzle) puzzles++;
                plan[index] = piece;
                used |= 1 << piece;
            }
            int walker = second ? new int[]{SLIME, POGO, BAR, FAN, BAR}[Math.min(4, chapter - CHAPTERS)] : chapter >= 2 && dice.nextInt(3) == 0 ? TIMES : MINUS;
            char step = second ? CRUMBLE : THIN;
            List<int[]> perches = new ArrayList<>();
            wall(2);
            int from = at;
            open(10);
            level.land[FLOOR - 1][from + 1] = ENTRY;
            pluses(from + 3, from + 8, FLOOR - 1);
            for (int piece : plan) {
                int x0 = at;
                switch (piece) {
                    case HOP:
                        open(11);
                        if (chapter == 0) {
                            // (In the first chapter: a low wall to hop.)
                            rise(x0 + 5, 2);
                            rise(x0 + 6, 2);
                        } else {
                            put(x0 + 3, FLOOR - 1, 5, SPIKE);
                            put(x0 + 3, FLOOR - 2, 2, THIN);
                            put(x0 + 6, FLOOR - 2, 2, step);
                        }
                        pluses(x0 + 3, x0 + 7, FLOOR - 4);
                        break;
                    case STACK:
                        open(12);
                        for (int column = 3; column <= 9; column++) rise(x0 + column, column >= 5 && column <= 7 ? 4 : 2);
                        perches.add(new int[]{x0 + 6, FLOOR - 5});
                        pluses(x0 + 5, x0 + 7, FLOOR - 7);
                        break;
                    case SHELF:
                        open(10);
                        level.land[FLOOR - 1][x0 + 2] = SPRING;
                        ledge(x0 + 4, FLOOR - 5, 4);
                        perches.add(new int[]{x0 + 6, FLOOR - 6});
                        pluses(x0 + 4, x0 + 5, FLOOR - 6);
                        if (chapter >= 1) level.lodgers.add(new int[]{walker, x0 + 6, FLOOR - 1});
                        break;
                    case CRUMBLING:
                        open(14);
                        put(x0 + 2, FLOOR - 1, 10, SPIKE);
                        put(x0 + 2, FLOOR - 2, 10, CRUMBLE);
                        for (int column = x0 + 3; column <= x0 + 10; column += 2) pluses(column, column, FLOOR - 4);
                        break;
                    case RIDE:
                        open(14);
                        put(x0 + 2, FLOOR - 1, 10, SPIKE);
                        level.movers.add(new int[]{x0 + 2, FLOOR - 2, 2, 8, 0, 60});
                        pluses(x0 + 4, x0 + 9, FLOOR - 4);
                        break;
                    case LOCK: {
                        // Its key is up on the first high place before it; or, where there is none, on a shelf of its own over a spring.
                        boolean own = perches.isEmpty();
                        open(own ? 16 : 10);
                        if (own) {
                            level.land[FLOOR - 1][x0 + 1] = SPRING;
                            ledge(x0 + 3, FLOOR - 5, 4);
                            level.items.add(new int[]{PlusRun.KEY, x0 + 5, FLOOR - 6, 0});
                        } else {
                            int[] perch = perches.remove(0);
                            level.items.add(new int[]{PlusRun.KEY, perch[0], perch[1], 0});
                        }
                        for (int column = at - 4; column <= at - 3; column++) {
                            for (int row = ceiling; row < FLOOR; row++) level.land[row][column] = row >= FLOOR - 2 ? LOCKED : BLOCK;
                        }
                        break;
                    }
                    case SPAN_PIT:
                        // The lever is up on a shelf over a spring; a way through has another on its far side, on the floor.
                        open(16);
                        level.land[FLOOR - 1][x0 + 1] = SPRING;
                        ledge(x0 + 3, FLOOR - 5, 3);
                        level.items.add(new int[]{PlusRun.LEVER, x0 + 4, FLOOR - 6, 0});
                        put(x0 + 6, FLOOR - 1, 8, SPIKE);
                        put(x0 + 6, FLOOR - 2, 8, SPAN);
                        if (through) level.items.add(new int[]{PlusRun.LEVER, x0 + 15, FLOOR - 1, 0});
                        break;
                    case BARRED:
                        // A plate on either side of the bars, and two spikes to be jumped on the way from the near one.
                        open(14);
                        level.items.add(new int[]{PlusRun.PLATE, x0 + 1, FLOOR - 1, 0});
                        put(x0 + 4, FLOOR - 1, 2, SPIKE);
                        for (int column = x0 + 8; column <= x0 + 9; column++) {
                            for (int row = ceiling; row < FLOOR; row++) level.land[row][column] = row >= FLOOR - 2 ? GRILLE : BLOCK;
                        }
                        level.items.add(new int[]{PlusRun.PLATE, x0 + 12, FLOOR - 1, 0});
                        break;
                    case GUARDS:
                        open(10);
                        ledge(x0 + 3, FLOOR - 2, 4);
                        level.lodgers.add(new int[]{walker, x0 + 7, FLOOR - 1});
                        if (chapter >= 2) level.lodgers.add(new int[]{second ? GUNNER : THROWER, x0 + 5, FLOOR - 3});
                        pluses(x0 + 3, x0 + 6, FLOOR - 4);
                        break;
                    default:
                        // A wall five blocks high, with a spring on either side of it.
                        open(12);
                        rise(x0 + 5, 5);
                        rise(x0 + 6, 5);
                        level.land[FLOOR - 1][x0 + 3] = SPRING;
                        level.land[FLOOR - 1][x0 + 8] = SPRING;
                        perches.add(new int[]{x0 + 5, FLOOR - 6});
                        pluses(x0 + 5, x0 + 6, FLOOR - 8);
                        break;
                }
            }
            int end = at, far = -1, holds = HOLDS_TREAT;
            if (through) {
                // The far end: a chest by its door; and a ribbon up out of the way, on the first high place there is, or on a ledge here.
                open(8);
                far = at - 1;
                level.land[FLOOR - 1][far] = ENTRY;
                level.items.add(new int[]{PlusRun.CHEST, far - 3, FLOOR - 1, 0});
                int ribbon = PlusRun.FORM + (number % 2 == 0 ? PlusRun.YELLOW : PlusRun.GREEN);
                if (perches.isEmpty()) {
                    put(end + 1, FLOOR - 2, 2, THIN);
                    level.items.add(new int[]{ribbon, end + 2, FLOOR - 4, 0});
                } else {
                    level.items.add(new int[]{ribbon, perches.get(0)[0], perches.get(0)[1], 0});
                }
            } else {
                // The chamber: the chest on a shelf, climbed to by blocks or sprung to; what else it holds on the floor under it; and its guard.
                open(13);
                int[] treats = {PlusRun.ICE, PlusRun.MAGNET, PlusRun.BREAD, PlusRun.NOTES};
                int treat = want[3] != 0 ? PlusRun.FORM + PlusRun.YELLOW : treats[number % treats.length];
                if (dice.nextBoolean()) {
                    rise(end + 1, 2);
                    rise(end + 2, 2);
                    rise(end + 3, 4);
                    rise(end + 4, 4);
                    // (And a block beyond them, to get back up from under the shelf.)
                    rise(end + 5, 2);
                    ledge(end + 6, FLOOR - 6, 5);
                    level.items.add(new int[]{PlusRun.CHEST, end + 9, FLOOR - 7, 0});
                } else {
                    level.land[FLOOR - 1][end + 2] = SPRING;
                    ledge(end + 4, FLOOR - 5, 7);
                    level.items.add(new int[]{PlusRun.CHEST, end + 9, FLOOR - 6, 0});
                }
                holds = number < 4 ? HOLDS_TREAT : dice.nextInt(3);
                if (holds == HOLDS_PET) level.items.add(new int[]{PlusRun.PET + dice.nextInt(RunPets.FOUND), end + 7, FLOOR - 1, 0});
                else if (holds == HOLDS_SHRINE) level.items.add(new int[]{PlusRun.SHRINE, end + 7, FLOOR - 1, 0, ribbonsOf(number)[0]});
                else level.items.add(new int[]{treat, end + 7, FLOOR - 1, 0});
                // (Black hangs in its far corner, where only a jump takes it.)
                if (number >= BLACK_FROM) level.items.add(new int[]{PlusRun.FORM + PlusRun.BLACK, end + 11, FLOOR - 3, 0});
                if (chapter >= 1) level.lodgers.add(new int[]{walker, end + 8, FLOOR - 1});
            }
            int to = at - 1;
            wall(2);
            // What stands about in it to be looked at: two things of its kind, on clear floor, well apart.
            int stood = 0;
            for (int column = from + 4; column < to - 3 && stood < 4; column++) {
                boolean clear = level.land[FLOOR - 1][column] == AIR && level.land[FLOOR - 2][column] == AIR && level.land[FLOOR - 1][column + 1] == AIR
                        && level.land[FLOOR - 1][column - 1] == AIR;
                for (int[] item : level.items) clear &= Math.abs(item[1] - column) > 1 || item[2] < FLOOR - 2;
                if (!clear || dice.nextInt(3) != 0) continue;
                level.decor.add(new int[]{200 + look * 2 + stood % 2, column, FLOOR - 1});
                stood++;
                column += 11;
            }
            int[] record = new int[count + 1];
            record[0] = level.rooms.size();
            System.arraycopy(plan, 0, record, 1, count);
            level.made.add(record);
            level.rooms.add(new int[]{kind, want[1], want[2], from, to, want[4], from + 1, want[3], far, want[5], 0, FLOOR, ceiling, look, holds});
        }

        /**
         * A cellar: an arena, walled all round, with a floor and eight storeys of platforms
         * over it, four squares apart, which is more than a jump. Between a storey and the
         * one over it there are steps: two squares of thin ledge half way up, under the end
         * of a platform, from which she jumps up through it; and where there is nothing
         * under a platform to step from, a spring on what is under it. There are springs on
         * the floor as well, short runs of spikes between them, and a ledge that rides up
         * and down the middle. There are three ways it may be laid, by the land: stacks, in
         * which the platforms of each storey lie over the gaps of the one below; towers, two
         * of solid block with platforms out from them and bridges between; and terraces, two
         * flights of platforms that climb from either end and cross. Bread and a ball of ink
         * lie up in it. In a top corner is a loft, across a gap too wide to jump, with a
         * chest on it for whoever can fly. And all over it are the places where a wave may
         * come in.
         */
        void arena(int[] want, boolean den) {
            int number = level.number, style = number / 3 % 3, index = level.rooms.size();
            for (int wall = 0; wall < 2; wall++) {
                for (int row = 0; row < DEEP; row++) level.land[row][at + wall] = BLOCK;
            }
            at += 2;
            int from = at, to = from + ARENA_WIDE - 1, floor = ARENA_FLOOR, middle = (from + to) / 2;
            for (int column = from; column <= to + 2; column++) {
                for (int row = 0; row < DEEP; row++) level.land[row][column] = row < 2 || row >= floor || column > to ? BLOCK : AIR;
            }
            at += ARENA_WIDE + 2;
            level.land[floor - 1][from + 1] = ENTRY;
            // The storeys.
            for (int storey = 1; storey <= STOREYS; storey++) {
                int row = floor - storey * STOREY;
                if (style == 0) {
                    for (int start = from + 5 + storey % 2 * 7; start + 9 <= to - 3; start += 15) ledge(start, row, 10);
                } else if (style == 1) {
                    if (storey <= 6) {
                        // Out from the towers on the odd storeys and on the sixth, which is level with their tops; and further out on the others.
                        boolean odd = storey % 2 == 1 || storey == 6;
                        ledge(odd ? from + 22 : from + 8, row, 12);
                        ledge(odd ? to - 33 : to - 19, row, 12);
                        if (storey % 2 == 0) {
                            ledge(from + 40, row, middle - 3 - from - 40);
                            ledge(middle + 4, row, to - 39 - middle - 4);
                        }
                    } else {
                        ledge(from + 26, row, 26);
                        ledge(to - 51, row, 26);
                    }
                } else {
                    int step = 11;
                    ledge(from + 5 + (storey - 1) * step, row, 15);
                    ledge(to - 19 - (storey - 1) * step, row, 15);
                    // And on every other storey a platform in the middle, by the ledge that rides.
                    if (storey % 2 == 1) ledge(middle - 4, row, 8);
                }
            }
            if (style == 1) {
                // The towers: solid, from over a way through each at the floor to the sixth storey, which their tops are part of.
                for (int row = floor - 6 * STOREY; row < floor - 3; row++) {
                    for (int column = 0; column < 6; column++) {
                        level.land[row][from + 34 + column] = BLOCK;
                        level.land[row][to - 39 + column] = BLOCK;
                    }
                }
            }
            // The loft, in the top corner by the far wall: nothing is within a jump of it.
            int loftRow = floor - STOREYS * STOREY;
            for (int column = to - 17; column <= to; column++) {
                for (int row = loftRow - 3; row <= loftRow + STOREY + 2; row++) if (level.land[row][column] == LEDGE) level.land[row][column] = AIR;
            }
            ledge(to - 8, loftRow, 7);
            level.items.add(new int[]{PlusRun.CHEST, to - 5, loftRow - 1, 0});
            level.lofts.add(new int[]{index, to - 8, to - 2, loftRow});
            // Spikes on the floor, in short runs, away from the door; and springs between them.
            for (int start = from + 24; start + 4 < to - 6; start += 31) put(start, floor - 1, 4, SPIKE);
            for (int spring = from + 12; spring < to - 4; spring += 19) {
                if (level.land[floor - 1][spring] == AIR) level.land[floor - 1][spring] = SPRING;
            }
            // The steps from each storey to the next, and from the floor to the first.
            for (int storey = 1; storey <= STOREYS; storey++) {
                int row = floor - storey * STOREY, under = row + STOREY;
                for (int column = from; column <= to; column++) {
                    if (level.land[row][column] != LEDGE || column > from && level.land[row][column - 1] == LEDGE) continue;
                    int end = column;
                    while (end < to && level.land[row][end + 1] == LEDGE) end++;
                    if (row == loftRow && column >= to - 8) continue;
                    boolean joined = false;
                    // A step under one end of it or the other, where there is something below to jump to the step from.
                    for (int tries = 0; tries < 2 && !joined; tries++) {
                        int at = tries == 0 ? column : end - 1;
                        if (level.land[row + 2][at] != AIR || level.land[row + 2][at + 1] != AIR || level.land[row + 1][at] != AIR) continue;
                        for (int near = at - 3; near <= at + 4 && !joined; near++) {
                            if (near < from || near > to) continue;
                            char ground = level.land[under][near], over = level.land[under - 1][near];
                            joined = (ground == LEDGE || ground == BLOCK) && (over == AIR || over == SPRING);
                        }
                        if (joined) put(at, row + 2, 2, THIN);
                    }
                    // Or a spring on what is under it, to go up through it.
                    for (int below = column + 1; below < end && !joined; below++) {
                        char ground = level.land[under][below];
                        if ((ground == LEDGE || ground == BLOCK) && level.land[under - 1][below] == AIR && level.land[under - 2][below] == AIR) {
                            level.land[under - 1][below] = SPRING;
                            joined = true;
                        }
                    }
                }
            }
            // A ledge that rides up and down the middle, from the floor to over the sixth storey: two of them between the towers.
            level.movers.add(new int[]{middle - 1, floor - 1, 3, 6 * STOREY + 2, 1, 80});
            if (style == 1) level.movers.add(new int[]{middle + 12, floor - 1, 3, 3 * STOREY, 1, 56});
            // Bread up on the fourth storey and a ball of ink on the sixth, each on the first platform there is there.
            int[] treats = {PlusRun.BREAD, PlusRun.SHIELD};
            for (int which = 0; which < 2; which++) {
                int row = floor - (which == 0 ? 4 : 6) * STOREY;
                for (int column = which == 0 ? from + 6 : middle; column <= to - 12; column++) {
                    if (level.land[row][column] != LEDGE || level.land[row][column + 2] != LEDGE) continue;
                    level.items.add(new int[]{treats[which], column + 2, row - 1, 0});
                    break;
                }
            }
            // Where a wave may come in: along the floor; in the middle of every platform, the higher ones perches; and in the open air.
            for (int column = from + 16; column <= to - 6; column += 9) {
                if (level.land[floor - 1][column] == AIR) level.spawns.add(new int[]{index, ON_FLOOR, column, floor});
            }
            for (int storey = 1; storey <= STOREYS; storey++) {
                int row = floor - storey * STOREY;
                for (int column = from; column <= to; column++) {
                    if (level.land[row][column] != LEDGE || column > from && level.land[row][column - 1] == LEDGE) continue;
                    int end = column;
                    while (end < to && level.land[row][end + 1] == LEDGE) end++;
                    if (row == loftRow && column >= to - 8 || end - column < 4) continue;
                    level.spawns.add(new int[]{index, storey >= 5 ? ON_PERCH : ON_PLATFORM, (column + end) / 2, row});
                }
            }
            for (int storey = 2; storey <= 6; storey += 2) {
                int row = floor - storey * STOREY - 2;
                for (int column = from + 14; column <= to - 14; column += 17) {
                    boolean open = true;
                    for (int across = -1; across <= 1; across++) for (int down = -1; down <= 1; down++) open &= level.land[row + down][column + across] == AIR;
                    if (open) level.spawns.add(new int[]{index, IN_AIR, column, row});
                }
            }
            level.rooms.add(new int[]{ARENA, want[1], want[2], from, to, want[4], from + 1, want[3], -1, want[5], den ? 1 : 0, floor, 2, -1, 0});
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
                    // Someone to meet; and in a land's first such place, whoever has something to ask of her. (What is drawn from the numbers
                    // the land is made from is drawn as it always was, so that the rest of the land is the same land.)
                    int who = SOMI + (level.number + level.items.size()) % (ALTTEUL - SOMI);
                    if (level.chapter == CHAPTERS && random.nextBoolean()) who = NURI;
                    if (!inVillage && level.quest != 0) who = giverOf(level.quest);
                    int house = random.nextInt(3), lamp = 3 + random.nextInt(2), other = random.nextInt(3), yard = 6 + random.nextInt(2);
                    int gives = who == NURI ? PlusRun.NOTES : GIVES[who - SOMI];
                    if (!inVillage && villageLand(level.number)) {
                        // The chapter's village: its arch with its name, whoever has something for her, its buildings one after another with
                        // its landmark among them, lanterns between, the stall at its far end, and a well or a paper tree.
                        int[] sorts = townOf(level.number);
                        ground(townWide(sorts.length));
                        level.town = from;
                        level.decor.add(new int[]{400, from + 1, floor - 1});
                        level.items.add(new int[]{gives, from + 4, floor - 1, who});
                        int column = from + 7;
                        for (int which = 0; which < sorts.length; which++) {
                            if (which == 2) {
                                level.decor.add(new int[]{410 + level.chapter, column + 1, floor - 1});
                                column += TOWN_GAP;
                            }
                            int sort = sorts[which];
                            level.decor.add(new int[]{sort == PET_SHOP ? 300 + level.number % 2 : sort == GUILD_HALL ? 302 + level.number % 2 : sort == SMITHY ? 304
                                    : sort == MARKET ? 305 : sort == GACHA ? 306 : sort == BOUTIQUE ? 307 : (house + which) % 3, column, floor - 1});
                            built.add(new int[]{HOUSE, column, floor, 0, column, floor, sort});
                            if (which % 2 == 1) level.decor.add(new int[]{lamp, column + 5, floor - 1});
                            column += TOWN_GAP;
                        }
                        level.decor.add(new int[]{5, column + 1, floor - 1});
                        level.items.add(new int[]{PlusRun.STALL, column + 1, floor - 1, ALTTEUL});
                        level.decor.add(new int[]{yard, column + 5, floor - 1});
                        inVillage = true;
                        if (level.number >= PLACES_FROM) {
                            door(level.number % 2 == 0 ? STASH : level.number % 6 == 1 ? GAME : level.number % 6 == 3 ? RANGE : TRAMPLE, column - 4, false);
                        }
                        break;
                    }
                    // A homestead: one house she can go into, whoever lives there at her door, a lantern, the stall, and a well or a paper tree.
                    ground(23);
                    level.decor.add(new int[]{house, from + 2, floor - 1});
                    level.decor.add(new int[]{lamp, from + 6, floor - 1});
                    level.decor.add(new int[]{yard, from + 11, floor - 1});
                    level.decor.add(new int[]{5, from + 17, floor - 1});
                    level.decor.add(new int[]{4, from + 21, floor - 1});
                    level.items.add(new int[]{gives, from + 7, floor - 1, who});
                    level.items.add(new int[]{PlusRun.STALL, from + 17, floor - 1, ALTTEUL});
                    built.add(new int[]{HOUSE, from + 2, floor, 0, from + 2, floor, HOME});
                    villages++;
                    if (!inVillage) {
                        inVillage = true;
                        // A stash in the even lands; in the odd ones a game: the room where pluses fall, the range, and the room where signs keep coming, by turns.
                        if (level.number >= PLACES_FROM) {
                            door(level.number % 2 == 0 ? STASH : level.number % 6 == 1 ? GAME : level.number % 6 == 3 ? RANGE : TRAMPLE, from + 14, false);
                        }
                    }
                    break;
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
