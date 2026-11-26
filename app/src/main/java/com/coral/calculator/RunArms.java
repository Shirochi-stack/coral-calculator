package com.coral.calculator;

/**
 * What Halgakos fights with in Plus run, in one table. The rules read their figures from
 * it, and so do the cards that describe each thing to the player, so that what is written
 * on a card is what happens.
 *
 * Six of them lie about in the lands: a pen, a knife, a book, a brush, a sword and an ink
 * gun. One found is hers from then on, in every land: it is in her backpack, and she may
 * change to it whenever she likes. Two are hers to begin a land with: her tablet-book,
 * which she has from the start, and her ring of API keys. A level is how far the smith has
 * worked on a thing, from nothing: three steps, each of which changes what the thing does,
 * a wave that flies from a blade, a burst, something that goes on through, and makes its
 * blows count for more against the tougher creatures of the later lands.
 *
 * And four are won in the black arena, each hers to begin a land with from then on: a
 * katana, kept in its sheath, that draws, cuts and sheathes again, and dashes; a lance that
 * is a fountain pen, which thrusts through a row and throws a jet of ink; a cannon that is
 * an inkwell, slow, with a wide burst; and a whip that is a bookmark's ribbon, which takes
 * what is over her head and, spun, what is on both sides of her.
 */
final class RunArms {
    static final int PEN = 0, KNIFE = 1, BOOK = 2, BRUSH = 3, SWORD = 4, GUN = 5, TABLET = 6, KEYS = 7, KATANA = 8, LANCE = 9, CANNON = 10, WHIP = 11, COUNT = 12;
    static final String[] NAMES = {"Pen", "Knife", "Book", "Brush", "Sword", "Ink gun", "Tablet-book", "API keys", "Katana", "Fountain-pen lance", "Ink-well cannon",
            "Ribbon whip"};
    /** Those she may begin a land with, in the order they are kept: the two that were always hers to have, and the four won in the black arena. */
    static final int[] MAINS = {TABLET, KEYS, KATANA, LANCE, CANNON, WHIP};
    /** How a thing is used: thrown straight, lobbed, or swung. */
    static final int THROWN = 0, LOBBED = 1, SWUNG = 2;
    private static final int[] WAY = {THROWN, THROWN, LOBBED, SWUNG, SWUNG, THROWN, THROWN, THROWN, SWUNG, SWUNG, LOBBED, SWUNG};
    /** How long each rests between one use and the next, in seconds. */
    private static final float[] REST = {.42f, .24f, .75f, .42f, .3f, .15f, .4f, .3f, .34f, .45f, 1, .5f};
    /** How fast what is thrown flies, in squares a second; nothing, for what is swung. */
    private static final float[] SPEED = {13, 19, 7.5f, 0, 0, 22, 25, 16, 0, 0, 8, 0};
    /** How far it reaches, in squares: how far what is thrown flies, where what is lobbed comes down, how far ahead of her a swing takes. */
    private static final float[] REACH = {13, 9.5f, 5.5f, 1.05f, .9f, 19.8f, 5, 7, 2.6f, 2.6f, 6, 3.4f};
    /** How wide what it hits is, in squares: a book's burst, a brush's sweep from over her head to her feet, a sword's stroke; nothing for a single thing thrown. */
    private static final float[] AREA = {0, 0, 2.3f, 2.1f, 1.35f, 0, 0, 0, 1.3f, .75f, 2.6f, 2.6f};
    /** How many pieces of a shield a blow of it takes. A book's is its burst's. */
    private static final float[] BITE = {1, 1, 3, 1, 2, .5f, 1, 1, 2, 1.5f, 3, 1};

    /** How many books she has to throw in a land, how many more a find of books gives, and how many shots a find of ink puts in the gun. */
    static final int BOOKS = 6, PAGES = 2, INK = 8;
    /** For how long a thing found that she has already makes what is in her hands stronger: a third as far again for what is thrown, half a square for a swing, and a piece more off a shield. */
    static final float BOOSTED = 15, BOOST_FAR = 1.3f, BOOST_SWING = .5f, BOOST_BITE = 1;
    /** How long the button is held before the heavy thing a tablet-book or a ring of keys does; and how long each rests after it. */
    static final float CHARGE = .45f;
    private static final float[] HEAVY_REST = {0, 0, 0, 0, 0, 0, .9f, .8f, 1, 1.1f, 1.6f, 1};
    /**
     * The katana: within how long of one cut the next tap is the next of three in a row;
     * how much further and heavier the third is; how far she dashes; and how long after the
     * dash everything on her line is cut. The lance's jet, the cannon's great ball, and the
     * whip's spin: how far each reaches.
     */
    static final float RHYTHM = .75f, FINISH_FAR = .6f, DASH = 4.5f, BEAT = .28f, SPIN = 2.6f, SPIN_TALL = 2.2f, SHELL = 9, SHELL_WIDE = 3.2f;
    /** The tablet-book's page: how long it is on its way out before it turns back. Its panel: how far ahead of her it opens, how tall it is, and how much of a shield it takes. */
    static final float PAGE_TURNS = .4f, PANEL_WIDE = 2.2f, PANEL_TALL = 1.9f, PANEL_BITE = 2;
    /** The keys: how many in a fan and how far apart, in parts of a turn; for how long a key leaves what it sticks in unlocked; and how often one fails, one throw in so many. */
    static final int FAN = 5, FAILS_IN = 13;
    static final float FAN_APART = .15f, UNLOCKS = 4;

    private RunArms() {}

    static int way(int weapon) { return WAY[weapon]; }
    static float rest(int weapon, int level) { return REST[weapon]; }
    static float heavyRest(int weapon) { return HEAVY_REST[weapon]; }
    static float speed(int weapon) { return SPEED[weapon]; }
    /** (A katana, a lance and a whip the smith has worked on reach further.) */
    static float reach(int weapon, int level) { return REACH[weapon] + (level >= 1 ? (weapon == KATANA ? .7f : weapon == LANCE || weapon == WHIP ? .6f : 0) : 0); }
    static float area(int weapon, int level) { return weapon == BOOK || weapon == CANNON ? burst(weapon, level) : AREA[weapon]; }
    /** How far the lance's jet goes; how many balls the cannon lobs; for how long its ink lies where it burst; how many times the whip's spin goes round; and how soon she can dash again. */
    static float jet(int level) { return level >= 2 ? 8.5f : 6; }
    static int balls(int level) { return level >= 3 ? 2 : 1; }
    static float lies(int level) { return level >= 2 ? 2 : 0; }
    static int spins(int level) { return level >= 2 ? 2 : 1; }
    static float dashRest(int level) { return HEAVY_REST[KATANA] * (level >= 3 ? .45f : 1); }
    /** How much of a shield the third cut of three takes. */
    static float finish(int level) { return level >= 3 ? 5 : 3; }
    /** (The master key, the last the smith makes of the ring, takes a piece more.) */
    static float bite(int weapon, int level) { return BITE[weapon] + (weapon == KEYS && level >= 3 ? 1 : 0); }

    /** How far the smith can work on a thing. */
    static final int LEVELS = 3;

    /** How many of a creature's blows one blow of it counts for: one, two from the first step, three at the last. */
    static int blows(int weapon, int level) { return 1 + (level + 1) / 2; }

    /**
     * The slash waves the smith puts into a sword, a knife and a brush: how many fly with
     * a stroke or a throw, how far they go, how tall they are, how fast, and whether they
     * go on through what they meet.
     */
    static int waves(int weapon, int level) {
        return level < 1 ? 0 : weapon == SWORD ? (level >= 3 ? 2 : 1) : weapon == KNIFE ? (level >= 2 ? 3 : 1) : weapon == BRUSH ? 1
                : weapon == KATANA && level >= 2 || weapon == WHIP && level >= 3 ? 1 : 0;
    }
    static float waveReach(int weapon, int level) {
        return weapon == SWORD ? (level >= 2 ? 6.5f : 4) : weapon == KNIFE ? (level >= 3 ? 6 : 4.5f) : weapon == KATANA || weapon == WHIP ? 5 : level >= 3 ? 7 : level == 2 ? 5 : 3;
    }
    static float waveTall(int weapon, int level) {
        return weapon == SWORD ? (level >= 3 ? 2 : 1.35f) : weapon == KNIFE ? .6f : weapon == KATANA ? 1.3f : weapon == WHIP ? 2.2f : level >= 3 ? 2.6f : 2.1f;
    }
    static float waveSpeed(int weapon) { return weapon == SWORD ? 17 : weapon == KNIFE ? 24 : weapon == KATANA ? 20 : weapon == WHIP ? 14 : 9; }
    static boolean waveThrough(int weapon, int level) {
        return weapon == BRUSH || weapon == KATANA || weapon == WHIP || weapon == SWORD && level >= 2 || weapon == KNIFE && level >= 3;
    }
    /** Whether a brush's wave wipes away what is thrown at her as it goes. */
    static boolean waveWipes(int weapon, int level) { return weapon == BRUSH && level >= 2; }

    /** How many things what is thrown goes on through before it stops: a pen, and a key. Ninety-nine is all of them. */
    static int pierces(int weapon, int level) { return weapon == PEN ? (level >= 3 ? 99 : level >= 1 ? 1 : 0) : weapon == KEYS && level >= 2 ? 99 : 0; }

    /** How wide the burst of a thing is, in squares from its middle: a book's always; a pen's where it ends, an ink blot's splash, and a page's where it turns, once the smith has made them. */
    static float burst(int weapon, int level) {
        return weapon == BOOK ? (level >= 3 ? 3.5f : level >= 1 ? 2.9f : 2.3f) : weapon == PEN ? (level >= 3 ? 2 : level == 2 ? 1.4f : 0)
                : weapon == GUN ? (level >= 3 ? 1.3f : level >= 1 ? .9f : 0) : weapon == TABLET && level >= 3 ? 1.5f
                : weapon == CANNON ? (level >= 1 ? 3.1f : 2.6f) : weapon == LANCE && level >= 3 ? 1.2f : 0;
    }

    /** For how long a book's pages burn where it burst; how many times an ink blot jumps on to the next creature; and how many pages the tablet-book flicks. */
    static float burns(int level) { return level >= 3 ? 2.5f : level == 2 ? 1.5f : 0; }
    static int chains(int level) { return level >= 3 ? 2 : level == 2 ? 1 : 0; }
    static int pages(int level) { return level >= 1 ? 2 : 1; }
    /** The tablet-book's panel, and the keys' fan, as the smith has made them. */
    static float panelWide(int level) { return level >= 2 ? 3.2f : PANEL_WIDE; }
    static float panelTall(int level) { return level >= 2 ? 2.4f : PANEL_TALL; }
    static int fan(int level) { return level >= 1 ? 7 : FAN; }

    /** What each step of the smith's work adds to a thing, in words. */
    private static final String[][] STEPS = {
            {"It goes on through the first thing it meets.", "It bursts where it ends.", "It goes through everything, and its burst is wider."},
            {"A thin slash wave flies with every knife.", "Three waves, in a fan.", "The waves go further, through whatever they meet."},
            {"A wider burst.", "Its pages burn for a while where it burst.", "Wider again, and it burns longer."},
            {"Every sweep throws a wide wave of paint.", "The wave goes further, and wipes away what is thrown at her.", "Further and taller again."},
            {"Every stroke sends a slash wave ahead.", "The wave goes further, through whatever it meets.", "Two waves, and taller."},
            {"Its blots splash.", "A blot that hits jumps on to the next creature.", "A wider splash, and it jumps twice."},
            {"Two pages at a time.", "A wider, taller panel.", "A page bursts where it turns back."},
            {"Seven keys in the fan.", "Keys go on through whatever they meet.", "The master key: a piece more off a shield with every key."},
            {"A longer draw.", "Every draw sends a slash wave ahead.", "She can dash again almost at once, and the third cut in a row takes five pieces off a shield."},
            {"A longer thrust.", "Its jet of ink goes further.", "A thrust bursts at its point."},
            {"A wider burst.", "Its ink lies where it burst for a while, and takes what walks into it.", "Two balls at a time."},
            {"A longer lash.", "The spin goes round twice.", "Every lash sends a wave ahead."}};

    /** What the step that takes a thing to a level adds: for a level from one to three. */
    static String step(int weapon, int level) { return STEPS[weapon][level - 1]; }
    /** Whether it has something heavy that it does when the button is held. */
    static boolean heavy(int weapon) { return HEAVY_REST[weapon] > 0; }
    /** Whether it is one she begins a land with. */
    static boolean main(int weapon) { return weapon >= TABLET; }
    /** How many times it can be used before it is empty, or 0 where there is no end to it: an ink gun's shots are what the counter has made them. */
    static int holds(int weapon, int shots) { return weapon == GUN ? shots : weapon == BOOK ? BOOKS : 0; }
    /** How many times a second it can be used. */
    static float pace(int weapon, int level) { return 1 / rest(weapon, level); }

    /** What it does, in a sentence or two. */
    static String does(int weapon) {
        switch (weapon) {
            case PEN: return "Thrown straight ahead like a dart. One thing at a time, from a long way off.";
            case KNIFE: return "Thrown fast and flat. Shorter than the pen, and quick to throw again.";
            case BOOK: return "Lobbed. It bursts where it lands and takes everything within its burst; it goes through the first thing it meets. A few to a land.";
            case BRUSH: return "A wide sweep, from over her head to her feet and a little behind her. It wipes away what is thrown at her.";
            case SWORD: return "A quick stroke at arm's length. It sends back what is thrown at her, and cuts a shield hard.";
            case GUN: return "Held down, it goes on firing until its tank is empty. Each shot takes little off a shield.";
            case TABLET: return "A tap flicks a page out and back through everything in its way. Held, a panel of translation opens in front of her and takes all that stands in it.";
            case KATANA: return "Kept in its sheath. A tap draws it, cuts further than it looks, and sheathes it again; three taps in rhythm are three cuts, the last the heaviest. "
                    + "Held, she dashes, and everything on her line is cut a beat later. In the air it cuts downward, harder with the down button.";
            case LANCE: return "A thrust through everything in front of her, a long way out. Held, a jet of ink from its nib that goes further still.";
            case CANNON: return "Lobs a ball of ink that bursts wide where it lands. Held, a great ball goes straight through everything and bursts at the end. Slow.";
            case WHIP: return "A long lash that takes what is in front of her from her feet to well over her head. Held, she spins, and it takes what is on both sides of her.";
            default: return "A tap throws a key that leaves what it sticks in unlocked: the next blow on it counts twice. Held, the whole ring goes out in a fan. Now and then a key fails: 401.";
        }
    }

    /** What it is good for; and, where there is something, what it is not. */
    static String goodFor(int weapon) {
        switch (weapon) {
            case PEN: return "Good for what stands far off or throws. Slow against a pack.";
            case KNIFE: return "Good for one after another in a line.";
            case BOOK: return "Good for packs and for shields. Not to be wasted on one.";
            case BRUSH: return "Good for packs at arm's length and for what flies low. Slow to recover.";
            case SWORD: return "Good for a boss's shield and for what throws. Short.";
            case GUN: return "Good for a crowd in a line and for what flies. Poor against shields.";
            case TABLET: return "Good at middle range and for a row of them. It is always hers.";
            case KATANA: return "Good for a line of them and for a quick answer. Not for a crowd on every side.";
            case LANCE: return "Good for keeping things off, and for a row. Nothing over her head.";
            case CANNON: return "Good for packs and for shields. Poor against one quick thing.";
            case WHIP: return "Good for what flies and for being surrounded. Light on a shield.";
            default: return "Good for big ones and for shields, with something else to follow. Light by itself.";
        }
    }
}
