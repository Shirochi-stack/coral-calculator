package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Plus run: a run and jump from one end of a land of signs to the other. Halgakos runs along
 * the ground and over the gaps in it, collects the pluses, keeps clear of spikes, takes to
 * the air off springs, deals with the signs that walk and hop about, and reaches the gate at
 * the far end. Along the way she finds things to fight with, a pen, a knife, a book, a brush,
 * a sword and an ink gun, and some of the signs throw and shoot things of their own. The
 * lands themselves are made by {@link RunLevels}.
 *
 * Everything is measured in squares of the land and in seconds. Across is from the left, and
 * down is from the top. This class knows nothing of screens: it is moved on by {@link #step}.
 */
final class PlusRun {
    static final int PLAYING = 0, WON = 1, LOST = 2;
    static final int HEARTS = 3;
    /** How many lives she starts a land with: each is a set of hearts, and the land is lost with the last of them. */
    static final int LIVES = 3;
    /** Her size, a little narrower than a square and a little taller. */
    static final float WIDE = .6f, TALL = 1.2f;
    /** How many times Zero has to be landed on. */
    static final int ZERO_HITS = 3;
    /**
     * What she may carry: nothing, or one of six things. A pen and a knife are thrown straight,
     * the knife faster and not as far; a book is lobbed and goes through whatever it meets; a
     * brush and a sword are swung, the brush over her head as well and the sword further, and
     * the sword sends back what is thrown at her; an ink gun fires for as long as its button is
     * held, until it is empty. The last of these numbers is the dot a divide shoots.
     */
    static final int UNARMED = -1, PEN = 0, KNIFE = 1, BOOK = 2, BRUSH = 3, SWORD = 4, GUN = 5, DOT = 6;
    /** And the heart Illya fires: it does not hurt, it charms, and for a while the arrows work the wrong way round. */
    static final int HEART = 7;
    /**
     * What may be picked up to help for a while: bread gives a heart back; ice cream makes
     * her faster and her jump higher; a ball of Rune's ink takes the next hurt for her; a
     * magnet brings the pluses near her to her; and the notes make her untouchable, so that
     * whatever she touches is dealt with.
     */
    static final int BREAD = 0, ICE = 1, SHIELD = 2, MAGNET = 3, NOTES = 4;
    private static final float[] LASTS = {0, 8, 0, 10, 6};
    static final float MAGNET_REACH = 3.5f, CHARM = 2.5f;
    /** What it was that last hurt her: a fall, a sign she touched, something thrown, spikes, or Tsubakieh's bat. */
    static final int FELL = 0, TOUCHED = 1, STRUCK = 2, SPIKED = 3, BATTED = 4;
    /** And in the second volume: lava, water she could not swim in, the ground jumping under King Slime, and a sword. */
    static final int BURNED = 5, SANK = 6, QUAKED = 7, SLASHED = 8;
    /**
     * More of what flies: the fire and the water she throws in two of her forms; the full
     * stop a punctuation dragon spits; the twisted words Gokhae says, which wind up and down
     * as they come; the gazette Miri throws at where she is about to be; and the Mawang's feathers.
     */
    static final int FIRE = 8, WATER = 9, STOP = 10, SAYING = 11, SCROLL = 12, FEATHER = 13;
    /**
     * And what those who wait at the end send: a shock that runs along the ground, to be
     * jumped; Tsubakieh's bat, which goes out and comes back; rubble, which hangs a moment
     * over where it will fall; a blade of Gokhae's fan; the Mawang's dark fire; a lump of
     * King Slime; and the Mawang's black ribbon, which does not hurt but leaves her an
     * ordinary girl for a while.
     */
    static final int SHOCK = 14, BOOMERANG = 15, DEBRIS = 16, BLADE = 17, DARK = 18, GLOB = 19, CURSE = 20;
    /** And a plus that falls, in the room where they are caught: it hurts no one, and is hers if she is under it. */
    static final int PLUSDROP = 21;
    /** A cloud of steam, which red and blue worn together throw in place of fire and water: it goes on through whatever it meets. */
    static final int STEAM = 22;
    /**
     * The colours worn together that have a name and something of their own, each a bit:
     * steam, red and blue; the comet, red and yellow; the tide, blue and green; the mirror,
     * brown and purple; the gale, green and yellow; the ember, red and brown; the prism,
     * any three; and white, all seven.
     */
    static final int STEAMS = 0, COMET = 1, TIDE = 2, MIRROR = 3, GALE = 4, EMBER = 5, PRISM = 6, WHITEN = 7, MIXES = 8;
    /** How many waves of signs a cellar fills with, how long the pluses fall, and how many of them caught win something more. */
    static final int WAVES = 3, ENOUGH = 8;
    /**
     * How a cellar's wave stands: announced, when for a beat nothing moves; fought, from
     * the moment the places where its creatures will come in are marked; and cleared, when
     * there is a breath before the next is announced.
     */
    static final int ANNOUNCED = 1, FOUGHT = 2, CLEARED = 3;
    /** How long a wave is announced for, how long a place is marked before something comes in at it, and how long the breath after a wave is. */
    static final float ANNOUNCE = 1.8f, MARKED = 1.1f, BREATH = 2.2f;
    /** How a cellar was fought, from the best: no hurt and quick; few hurts; no life lost; and done. And what each is worth in pluses. */
    static final int RANK_S = 3, RANK_A = 2, RANK_B = 1, RANK_C = 0;
    static final int[] RANK_PAYS = {15, 25, 40, 60};
    static final float PAR = 360;
    /**
     * How many of a cellar's wave are there at once, counting those marked to come in: the
     * rest wait their turn, and one comes for each that falls. (It can be set, to find what
     * a phone draws without faltering.)
     */
    static final int CEILING = 24;
    static int ceiling = CEILING;
    static final float FALLING = 10;
    /** How long rubble hangs before it falls, how long someone takes to arrive, and how long they take over changing. */
    static final float HANGS = .55f, ENTER = 2, FLOURISH = 1.2f;
    /**
     * More of what may be picked up: Tsubakieh's mended bat, which sends every sign near her
     * over the fence, and a compliment from Illya, which leaves them standing charmed.
     */
    static final int HOMERUN = 5, COMPLIMENT = 6;
    /** And a stall, which is not picked up: standing at it, what she has collected in this land buys things. */
    static final int STALL = 7;
    /** And two that have to do with what is asked of her: a rice cake, to be found; and whoever a letter is for, who stays where she is. */
    static final int CAKE = 8, DROPOFF = 9;
    /** A chest in a place off the land: opened by touching it, and what is in it is kept. */
    static final int CHEST = 20;
    /** How what is asked of her stands: not asked yet, being done, done, or not to be done this time. */
    static final int ASKED = 1, DONE = 2, FAILED = 3;
    /** And handed in: whoever asked it, or whoever it was for, has thanked her and paid her for it. */
    static final int PAID = 4;
    /** Why a stall will not sell something: she cannot pay; it has sold what it had of it; she is wearing a colour; or she has no use for it yet. */
    static final int SOLD = 0, POOR = 1, SOLD_OUT = 2, WORN = 3, NEEDLESS = 4;
    /**
     * Her forms, each from a ribbon of its colour, which is the colour her hair turns. Red
     * throws fire and walks on lava. Blue throws water and swims. Brown knocks over whatever
     * she touches, though what is thrown still hurts. Green flies for as long as the jump is
     * held. Yellow runs much faster. Purple sends back what is thrown at her. Black is no
     * gift: an ordinary girl, slow, who jumps less far and can hurt nothing. White is all of
     * the others but black at once. A ribbon is the item numbered {@link #FORM} more than its form.
     */
    static final int RED = 1, BLUE = 2, BROWN = 3, GREEN = 4, YELLOW = 5, PURPLE = 6, BLACK = 7, WHITE = 8, FORM = 10;
    private static final float CRUMBLES = .45f, CRUMBLE_BACK = 4, BELT = 2.6f, WORD_HANGS = 4.2f;

    /** What she has been given to keep, from one land to the next: she starts with none of it. */
    static final class Perks {
        /** How many hearts she starts a land with, and how many lives. */
        int hearts = HEARTS;
        int lives = LIVES;
        /** Her wings carry her a second jump in the air. */
        boolean wings;
        /** From how far a plus comes to her without being touched, in squares. */
        float reach;
        /** How many times as long what she picks up lasts. */
        float lasting = 1;
        /** How many shots an ink gun holds. */
        int shots = GUN_SHOTS;
        /** A flag gives a heart back as she passes it. */
        boolean flagHeart;
        /** How many hurts a ball of Rune's ink takes for her. */
        int inkHurts = 1;
        /**
         * The colours of hair that are hers to keep, as bits, the first for red: any of them
         * she may call on herself, once what she calls on them with has filled again, which
         * takes this many seconds from the moment the colour she wore is gone. And how many
         * times as long any form lasts.
         */
        int colours;
        float recharge = 10, formLonger = 1;
        /** Whether black on her lasts no longer than a ribbon does, and not twice as long: what the Mawang gave. */
        boolean resists;
        /** How many of the colours that are hers she may wear at once. */
        int together = 2;
    }
    static final int GUN_SHOTS = 16;
    /** How long a swing of the brush or the sword lasts. */
    static final float SWING = .18f;
    /** How long before it throws a sign that throws knives can be seen to raise one. */
    static final float WARNING = .8f;

    private static final float RUN = 6.2f, PUSH = 40, PUSH_IN_AIR = 24, DRAG = 50;
    private static final float GRAVITY = 42, LEAP = 15.5f, SHORT_LEAP = 6, FALL = 22, BOUNCE = 10, SPRING = 22.5f;
    /** A jump still counts this long after she has run off an edge, and this long before she lands. */
    private static final float LATE = .08f, EARLY = .1f;
    private static final float SAFE = 1.2f, STEP = 1 / 60f;
    /** How fast each sign walks, how wide and tall it is, and how high a divide hops. */
    private static final float[] SPEED = {1.6f, 1.3f, 0, 1.9f, 0, 0, 0, 2.4f, 1.6f, 2.2f, 1, 2.7f, 0, 2.4f, 0, 0, 1.5f, 0, 3.6f, 3,
            1.6f, 2, 1.4f, 2, 2.4f, 2, 2.2f, 1.2f, 0, 6.5f, 1.2f, 3, 1.8f, 2, 2.6f, 2.2f, 3, 1.3f, 1.3f};
    private static final float[] FOE_WIDE = {.8f, .85f, .8f, 1.8f, .8f, .8f, .9f, .8f, .8f, .9f, 1.3f, .7f, 1, .7f, .9f, .8f, .8f, .8f, 2.2f, .9f,
            .9f, .8f, .8f, .9f, .9f, 1.6f, 2, 1.6f, .9f, 2.6f, 1.5f, 1.9f, .9f, 1, 1.2f, .9f, .5f, .8f, .9f};
    private static final float[] FOE_TALL = {.55f, .85f, .8f, 2.2f, .55f, .8f, 1.5f, 1.3f, .7f, .7f, .45f, 1.2f, .9f, 1.2f, 1.5f, 1.3f, 1.3f, 1.4f, 1.9f, 1.5f,
            1.4f, 1.4f, 1.3f, 1.3f, 1.3f, 1.3f, 1.5f, 1.7f, .9f, 1.5f, 1.2f, .8f, .7f, .8f, .8f, .9f, .45f, .9f, .9f};
    /** Tsubakieh: how near she comes before she raises her bat, how long she holds it up, how far it reaches, and how long she is open after. */
    private static final float BAT_NEAR = 1.7f, BAT_RAISED = .55f, BAT_REACH = 1.5f, BAT_SPENT = .8f;
    /** Illya: how far off she keeps, how fast she drifts to keep it, and how far back from where she began she will go. */
    private static final float ILLYA_KEEPS = 4.2f, ILLYA_DRIFT = 2.4f, ILLYA_ROOM = 11.5f;
    private static final float HOP_HIGH = 1.9f, HOP_EVERY = 1.3f, STUNNED = 1.1f;
    /** How long each of her things must rest between one use and the next. */
    private static final float[] REST = {.42f, .24f, .75f, .42f, .3f, .15f};
    /** How near she must be for a knife to be thrown at her, how often one is, and how fast it flies; and the same for a dot and for Zero's books. */
    private static final float THROW_REACH = 5.2f, THROW_EVERY = 2.6f, THROWN = 4.2f;
    private static final float DOT_REACH = 6, DOT_SPEED = 6.5f, BOOK_REACH = 9, BOOK_EVERY = 3.4f, BOOK_WEIGHT = 30;

    /** Something flying through the air: hers, or thrown at her. Its place is its middle. */
    static final class Shot {
        /** One of hers that goes on through whatever it meets. */
        boolean through;
        final int kind;
        boolean hers;
        float x, y, across, down, left, age;
        /** How fast a bat was thrown: it slows, stops and comes back at that speed. */
        float spin;

        Shot(int kind) { this.kind = kind; }
    }

    /** One of the signs in her way: where it is, which way it is going, and how it stands. */
    static final class Foe {
        final int kind;
        float x, y;
        /** Where it stands when it is on the ground: a divide leaves it as it hops. */
        float ground;
        boolean leftward = true, flat;
        /** How long it has been about, how long Zero stays dazed after a landing, and how many more he can take. */
        float clock, dazed;
        int left;
        /** How long until it next throws something, if it is one that does. */
        float wait = 1;
        /** Where it began; and for Tsubakieh what she is doing, walking (0), bat raised (1) or bat swung (2), and for how much longer. */
        float home, act;
        int mode;

        /** How many times its usual size it is: King Slime loses some of himself with every blow. */
        float size = 1;
        /** Whether it was put into a land to make up its numbers: such a one keeps still while she is far off. */
        boolean crowd;
        /**
         * For those who wait at the end. How much they can take at most. What they are doing
         * is one of the things they do, numbered from 1 in {@link #mode}, and where they are
         * in it: showing that it is coming (0), doing it (1), or spent after it (2), which is
         * the only time they can be hurt; {@link #act} is how much longer that lasts. How many
         * things they have done, and which, as bits. How much longer they take to arrive, and
         * to change when a third of what they can take is gone.
         */
        int most, step, turn, did;
        float enter, flourish, fromX, toX;
        /** Not come yet; there but not to be seen or touched; and made by one of those who wait. */
        boolean away, unseen, spawned;

        Foe(int kind) { this.kind = kind; }

        float wide() { return FOE_WIDE[kind] * size; }
        float tall() { return FOE_TALL[kind] * size; }
    }

    private final RunLevels.Level level;
    private final int columns;
    private final List<int[]> pluses = new ArrayList<>();
    private final List<Foe> foes = new ArrayList<>();
    /** The things to fight with that still lie about: the kind, column and row of each. */
    private final List<int[]> arms = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final int total;
    private final Perks perks;
    /** What may still be picked up to help: the kind, column and row of each, and who holds it out, if anyone. */
    private final List<int[]> items = new ArrayList<>();
    private float rush, magnet, notes, charmed;
    private boolean shield;
    private int combo, bonus, airJumps, flaps, taken, lastTaken = -1, spared, charms, swings, cause;
    private int formed, inkLeft, doorsUsed, quakes, cleared, parried, censors, riding = -1;
    private float censored, doorWait, offIt;
    /**
     * The colours she wears, by their numbers: how much longer each is on, how long it was
     * put on for, and how much longer each must wait, once it is gone, before she can call
     * on it again. Which of them came from a ribbon or were given or lent, and so do not
     * count against how many of her own she may wear; and which was put on last.
     */
    private final float[] worn = new float[WHITE], lasted = new float[WHITE], waits = new float[WHITE];
    private int lent, last;
    /** How much longer black that was put on her, which is no colour of hers, lies on her; and how much longer she is white from a ribbon. */
    private float cursed, pale;
    /** Which named mixes she wears now and has ever worn in this land, as bits; which was the last to form, and how many have; how many colours have gone; and how many times she has been white of her own colours. */
    private int mixed, mixes, lastMix = -1, mixings, faded, whitened;
    private int spent, giver, greetings;
    /** Which place off the land she is in, or -1; which she has been into, which she has finished, and which hidden doors she has broken through to. */
    private int room = -1, roomsFound, roomsDone, revealed;
    private int entered, reveals, roomWins, wave, caught, dice = 12345;
    private float gameLeft, sinceDrop, gameLasts;
    private int chests;
    private boolean quitting, rewave;
    /** How the wave of the cellar she is in stands, and for how much longer; and what is about to come in: its kind, across, the top of where it will stand, and in how long. */
    private int wavePhase;
    private float waveClock, sinceMoved, sinceShelf;
    private final List<float[]> pending = new ArrayList<>();
    /** Those of the wave who have yet to be marked, in the order they will come. */
    private final List<Integer> reserve = new ArrayList<>();
    /** How the cellar has been fought so far: for how long, with how many hurts, and how many lives lost; and how the last one cleared was ranked. */
    private float arenaTime;
    private int arenaHurts, arenaLives, ranked = -1;
    private int heldWeapon = UNARMED, heldRounds, woken, bursts;
    /** In the range, with its gun in her hands; and called on in black, a shadow that nothing touches. */
    private boolean ranging;
    private int quest, cakes, questGiven, questsDone, questFails, questFlags;
    /** Where whoever asked it stands; how many times it has been handed in, and she has been reminded of it; and whether she has been away from whoever asked. */
    private int questColumn = -1, questRow, questsPaid, reminders;
    private boolean questAway;
    /** The stall she is at, by its column, and what each stall has sold of each thing it sells. */
    private int stallAt = -1;
    private final java.util.Map<Integer, Integer> stallSold = new java.util.HashMap<>();
    /** How full what she calls a colour on with is, from nothing to 1; and how many times she has. */
    private int called;
    private float questLeft;
    private boolean atStall;
    /** The stalls she has been up to, by the column each stands in: its keeper greets her the first time only. */
    private final java.util.Set<Integer> greeted = new java.util.HashSet<>();
    private boolean locked, leaving;
    private float opening, rearm, sinceShot = 9;
    private int entrances, phases, specials, special, unlocks, streak, wiped;
    /** What those who wait have made this moment, to join the rest once everything has moved. */
    private final List<Foe> born = new ArrayList<>();
    private boolean wet;
    /** The ledges that crumble: how long each that has been stood on has before it falls, or, below nothing, until it is back. */
    private final java.util.Map<Integer, Float> crumble = new java.util.HashMap<>();
    private int weapon = UNARMED, rounds, swungWith = UNARMED;
    private float sinceStrike = 1, swing;
    private boolean strikeHeld;
    private int strikes, picked, thrown, turned, casts;
    private float x, y, speedAcross, speedDown;
    private float startX, startY;
    private boolean onGround, facingLeft, jumpHeld;
    /** She is in the air off a spring, and stays so until she lands. */
    private boolean sprung;
    private float sinceGround = 1, sinceJump = 1, safe;
    /** Her hearts, counted in halves; her lives; and how many of those she has lost here. */
    private int halves = HEARTS * 2, lives = LIVES, livesLost, state = PLAYING;
    /** The last firm ground she stood on, where a fall puts her back; how much longer she flinches from a hurt, and which way it sent her. */
    private float firmX, firmY, flinch, knock;
    private float sinceCast = 1, sinceBrace = 1, braceX, braceY;
    private int braces;
    /** Counts that only go up: one met in the middle of a land roused; Blot come apart; a colour shut off by whoever she fights; a slime grown on fire; and she brought down out of the air. */
    private int roused, splits, shuts, drunk, grounded, lastShut;
    private float time;
    /** Counts that only go up, for whoever shows the game to tell what has just happened. */
    private int collected, flattened, hurts, jumps, landings, springs, flagsPassed, blows;
    /** Where the last plus was collected and the last sign flattened: across and down, in squares. */
    private float plusX, plusY, foeX, foeY;

    PlusRun(RunLevels.Level level) { this(level, new Perks()); }

    /** A run through a land with what she has been given to keep. */
    PlusRun(RunLevels.Level level, Perks perks) {
        this.level = level;
        this.perks = perks;
        halves = perks.hearts * 2;
        lives = perks.lives;
        columns = level.columns;
        pluses.addAll(level.pluses);
        arms.addAll(level.weapons);
        items.addAll(level.items);
        total = pluses.size();
        for (int[] spec : level.foes) {
            Foe foe = new Foe(spec[0]);
            boolean waits = RunLevels.boss(spec[0]);
            // One that is given more than a blow to take is a big one.
            if (spec.length > 3 && !waits) foe.size = 1.3f;
            foe.x = spec[1] + (1 - foe.wide()) / 2;
            foe.ground = spec[2] + 1 - foe.tall();
            foe.y = foe.ground;
            foe.home = foe.x;
            foe.left = waits ? health(spec[0]) : spec.length > 3 ? spec[3] : 1;
            foe.crowd = RunLevels.crowd(spec);
            if (spec[0] == RunLevels.WORD) foe.y = foe.ground - WORD_HANGS;
            // Each divide starts its hop at a different moment.
            foe.clock = spec[1] * .37f % HOP_EVERY;
            if (waits) {
                // Whoever waits is not there until she has come in, where there is a place to come into.
                foe.most = foe.left;
                foe.step = 3;
                foe.act = .6f;
                foe.away = level.arena >= 0;
                // One met in the middle of a land is not there until she comes near; Blot is twice the size of anything.
                if (RunLevels.rover(spec[0])) {
                    foe.away = true;
                    foe.step = 3;
                    if (spec[0] == RunLevels.BLOT) resize(foe, 2);
                    foe.home = foe.x;
                }
            }
            foes.add(foe);
        }
        startX = 2;
        startY = RunLevels.FLOOR - TALL;
        x = startX;
        y = startY;
        firmX = x;
        firmY = y;
    }

    private PlusRun(PlusRun other) {
        level = other.level;
        perks = other.perks;
        items.addAll(other.items);
        rush = other.rush;
        magnet = other.magnet;
        notes = other.notes;
        charmed = other.charmed;
        shield = other.shield;
        combo = other.combo;
        bonus = other.bonus;
        airJumps = other.airJumps;
        flaps = other.flaps;
        taken = other.taken;
        lastTaken = other.lastTaken;
        spared = other.spared;
        charms = other.charms;
        swings = other.swings;
        cause = other.cause;
        formed = other.formed;
        System.arraycopy(other.worn, 0, worn, 0, WHITE);
        System.arraycopy(other.lasted, 0, lasted, 0, WHITE);
        System.arraycopy(other.waits, 0, waits, 0, WHITE);
        lent = other.lent;
        last = other.last;
        cursed = other.cursed;
        pale = other.pale;
        mixed = other.mixed;
        mixes = other.mixes;
        lastMix = other.lastMix;
        mixings = other.mixings;
        faded = other.faded;
        whitened = other.whitened;
        inkLeft = other.inkLeft;
        doorsUsed = other.doorsUsed;
        quakes = other.quakes;
        cleared = other.cleared;
        parried = other.parried;
        censors = other.censors;
        riding = other.riding;
        censored = other.censored;
        doorWait = other.doorWait;
        offIt = other.offIt;
        spent = other.spent;
        giver = other.giver;
        atStall = other.atStall;
        greetings = other.greetings;
        room = other.room;
        roomsFound = other.roomsFound;
        roomsDone = other.roomsDone;
        revealed = other.revealed;
        entered = other.entered;
        reveals = other.reveals;
        roomWins = other.roomWins;
        wave = other.wave;
        caught = other.caught;
        dice = other.dice;
        gameLeft = other.gameLeft;
        gameLasts = other.gameLasts;
        rewave = other.rewave;
        wavePhase = other.wavePhase;
        waveClock = other.waveClock;
        sinceMoved = other.sinceMoved;
        sinceShelf = other.sinceShelf;
        for (float[] one : other.pending) pending.add(one.clone());
        arenaTime = other.arenaTime;
        arenaHurts = other.arenaHurts;
        arenaLives = other.arenaLives;
        ranked = other.ranked;
        chests = other.chests;
        sinceDrop = other.sinceDrop;
        quitting = other.quitting;
        heldWeapon = other.heldWeapon;
        heldRounds = other.heldRounds;
        woken = other.woken;
        bursts = other.bursts;
        ranging = other.ranging;
        quest = other.quest;
        called = other.called;
        cakes = other.cakes;
        questGiven = other.questGiven;
        questsDone = other.questsDone;
        questFails = other.questFails;
        questFlags = other.questFlags;
        questColumn = other.questColumn;
        questRow = other.questRow;
        questsPaid = other.questsPaid;
        reminders = other.reminders;
        questAway = other.questAway;
        stallAt = other.stallAt;
        stallSold.putAll(other.stallSold);
        questLeft = other.questLeft;
        greeted.addAll(other.greeted);
        locked = other.locked;
        leaving = other.leaving;
        opening = other.opening;
        rearm = other.rearm;
        sinceShot = other.sinceShot;
        entrances = other.entrances;
        phases = other.phases;
        specials = other.specials;
        special = other.special;
        unlocks = other.unlocks;
        streak = other.streak;
        wiped = other.wiped;
        wet = other.wet;
        crumble.putAll(other.crumble);
        columns = other.columns;
        pluses.addAll(other.pluses);
        total = other.total;
        for (Foe from : other.foes) {
            Foe foe = new Foe(from.kind);
            foe.x = from.x;
            foe.y = from.y;
            foe.ground = from.ground;
            foe.leftward = from.leftward;
            foe.flat = from.flat;
            foe.clock = from.clock;
            foe.dazed = from.dazed;
            foe.left = from.left;
            foe.wait = from.wait;
            foe.home = from.home;
            foe.act = from.act;
            foe.mode = from.mode;
            foe.size = from.size;
            foe.most = from.most;
            foe.step = from.step;
            foe.turn = from.turn;
            foe.did = from.did;
            foe.enter = from.enter;
            foe.flourish = from.flourish;
            foe.fromX = from.fromX;
            foe.toX = from.toX;
            foe.away = from.away;
            foe.unseen = from.unseen;
            foe.spawned = from.spawned;
            foes.add(foe);
        }
        arms.addAll(other.arms);
        for (Shot from : other.shots) {
            Shot shot = new Shot(from.kind);
            shot.hers = from.hers;
            shot.x = from.x;
            shot.y = from.y;
            shot.across = from.across;
            shot.down = from.down;
            shot.left = from.left;
            shot.age = from.age;
            shot.spin = from.spin;
            shots.add(shot);
        }
        weapon = other.weapon;
        rounds = other.rounds;
        swungWith = other.swungWith;
        sinceStrike = other.sinceStrike;
        swing = other.swing;
        strikeHeld = other.strikeHeld;
        strikes = other.strikes;
        picked = other.picked;
        thrown = other.thrown;
        turned = other.turned;
        casts = other.casts;
        x = other.x;
        y = other.y;
        speedAcross = other.speedAcross;
        speedDown = other.speedDown;
        startX = other.startX;
        startY = other.startY;
        onGround = other.onGround;
        facingLeft = other.facingLeft;
        jumpHeld = other.jumpHeld;
        sprung = other.sprung;
        sinceGround = other.sinceGround;
        sinceJump = other.sinceJump;
        safe = other.safe;
        halves = other.halves;
        lives = other.lives;
        livesLost = other.livesLost;
        firmX = other.firmX;
        firmY = other.firmY;
        flinch = other.flinch;
        knock = other.knock;
        sinceCast = other.sinceCast;
        sinceBrace = other.sinceBrace;
        braces = other.braces;
        roused = other.roused;
        splits = other.splits;
        shuts = other.shuts;
        drunk = other.drunk;
        grounded = other.grounded;
        lastShut = other.lastShut;
        state = other.state;
        time = other.time;
        collected = other.collected;
        flattened = other.flattened;
        hurts = other.hurts;
        jumps = other.jumps;
        landings = other.landings;
        springs = other.springs;
        flagsPassed = other.flagsPassed;
        blows = other.blows;
    }

    /** This run as it stands, to try something on without touching the run itself. */
    PlusRun copy() { return new PlusRun(this); }

    // What there is to see

    RunLevels.Level level() { return level; }

    int columns() { return columns; }

    char at(int column, int row) {
        if (column < 0 || column >= columns || row < 0 || row >= level.land.length) return RunLevels.AIR;
        char here = level.land[row][column];
        // A ledge that has crumbled is not there until it is back.
        if (here == RunLevels.CRUMBLE && !crumble.isEmpty() && crumbling(column, row) < 0) return RunLevels.AIR;
        return here;
    }

    static boolean solid(char square) {
        return square == RunLevels.BLOCK || square == RunLevels.LEDGE || square == RunLevels.CRUMBLE || square == RunLevels.BELT_RIGHT
                || square == RunLevels.BELT_LEFT || square == RunLevels.SLICK;
    }

    /**
     * What stops her and holds her up: whatever is solid but a ledge, and lava while she is
     * fireproof and water while she can swim. A ledge is as thin as it looks: she walks under
     * it and jumps up through it, and it holds her only when she comes down onto it.
     */
    private boolean firm(char square) {
        return square != RunLevels.LEDGE && solid(square) || square == RunLevels.LAVA && has(RED) || square == RunLevels.WATER && has(BLUE);
    }

    /** The pluses not yet collected: the column and row of each. */
    List<int[]> pluses() { return pluses; }

    List<Foe> foes() { return foes; }

    /** The things to fight with not yet picked up: the kind, column and row of each. */
    List<int[]> arms() { return arms; }

    /** Everything in the air, hers and theirs. */
    List<Shot> shots() { return shots; }

    /** What may still be picked up to help: the kind, column and row of each, and who holds it out (0 no one, 1 Chikuwa, 2 Rune). */
    List<int[]> items() { return items; }

    int maxHearts() { return perks.hearts; }
    /** How much longer the ice cream, the magnet and the notes last; whether a ball of ink is about her; how much longer she is charmed. */
    float rush() { return rush; }
    float magnet() { return magnet; }
    float notes() { return notes; }
    boolean shield() { return shield; }
    float charmed() { return charmed; }
    /** How many signs she has landed on without touching the ground between, and the pluses that has won her over what she collected. */
    int combo() { return combo; }
    int bonus() { return bonus; }
    /** Counts that only go up: second jumps on her wings, things picked up to help, hurts the ink took for her, times charmed, swings of Tsubakieh's bat. */
    int flaps() { return flaps; }
    int taken() { return taken; }
    int lastTaken() { return lastTaken; }
    int spared() { return spared; }
    int charms() { return charms; }
    int swings() { return swings; }
    /** What last hurt her. */
    int cause() { return cause; }
    /** How many times she has thrown the fire or the water of a colour: a count that only goes up. */
    int casts() { return casts; }
    /** Who held out the last thing she took, as {@link RunLevels} numbers them: 0 if it lay about. */
    int giver() { return giver; }
    /** Whether she stands at a stall; what she has to spend there, which is what she has collected in this land less what she has spent; and what she has spent. */
    boolean atStall() { return atStall; }
    /** How many stalls she has come up to for the first time: going away and coming back does not count again. */
    int greetings() { return greetings; }
    int purse() { return collected + bonus - spent; }
    int spent() { return spent; }

    /** How many of a thing one stall has to sell: a ribbon, one; a ball of ink, one; bread and ice cream, two. */
    static int stock(int kind) { return kind > FORM || kind == SHIELD ? 1 : 2; }

    /**
     * Whether the stall she is at will sell her something, and if not, why not. Each stall
     * has only so many of a thing, and the next stall has its own. A ribbon is not sold
     * while she wears a colour, so that a colour always ends; nor bread while her hearts
     * are full, a ball of ink while she has one, or ice cream while she is full of it.
     */
    int refusal(int kind, int cost) {
        if (stallSold(kind) >= stock(kind)) return SOLD_OUT;
        if (kind > FORM && (coloured() || cursed > 0)) return WORN;
        if (kind == BREAD && halves >= perks.hearts * 2 || kind == SHIELD && shield || kind == ICE && rush > 0) return NEEDLESS;
        return purse() < cost ? POOR : SOLD;
    }

    /** How many of a thing the stall she is at has sold. */
    int stallSold(int kind) {
        Integer sold = stallSold.get(stallAt * 64 + Math.min(kind, FORM + 1));
        return sold == null ? 0 : sold;
    }

    /** Buys something at a stall for some of what she has collected: it is hers at once, as if she had picked it up. Returns whether it was sold. */
    boolean buy(int kind, int cost) {
        if (refusal(kind, cost) != SOLD) return false;
        spent += cost;
        stallSold.put(stallAt * 64 + Math.min(kind, FORM + 1), stallSold(kind) + 1);
        take(kind);
        return true;
    }

    /**
     * Which of the land's doors she stands at and may use, or -1: outside, either door of
     * a place; inside, its door or its far door, unless the place is shut until what
     * happens in it is over.
     */
    int door() {
        if (state != PLAYING || !onGround) return -1;
        float middle = x + WIDE / 2, feet = y + TALL;
        if (room >= 0) {
            int[] in = level.rooms.get(room);
            if (sealed() || Math.abs(feet - in[11]) > .3f) return -1;
            return Math.abs(middle - in[6] - .5f) < .8f || in[8] >= 0 && Math.abs(middle - in[8] - .5f) < .8f ? room : -1;
        }
        for (int index = 0; index < level.rooms.size(); index++) {
            int[] one = level.rooms.get(index);
            if (Math.abs(middle - one[1] - .5f) < .8f && Math.abs(feet - one[2]) < .3f) return index;
            if (one[5] != one[1] && Math.abs(middle - one[5] - .5f) < .8f && Math.abs(feet - one[9]) < .3f) return index;
        }
        return -1;
    }

    /**
     * She uses the door she stands at: into the place it leads to, or out of it again, to
     * where she went in or, by the far door of a passage, to where it comes out. A hidden
     * door is first broken through to, and only then gone in by. Returns whether there was
     * a door to use.
     */
    boolean enter() {
        int at = door();
        if (at < 0) return false;
        int[] one = level.rooms.get(at);
        float middle = x + WIDE / 2;
        boolean far;
        if (room < 0) {
            far = one[5] != one[1] && Math.abs(middle - one[5] - .5f) < .8f;
            if (!far && one[7] == 1 && (revealed & 1 << at) == 0) {
                revealed |= 1 << at;
                reveals++;
                return true;
            }
            room = at;
            roomsFound |= 1 << at;
            revealed |= 1 << at;
            put(far ? one[8] : one[6], one[11]);
            // A cellar shuts, and its first wave is announced: every time she comes into it, for it may be fought again for how well it is fought.
            if (one[0] == RunLevels.ARENA) {
                wave = 1;
                wavePhase = ANNOUNCED;
                waveClock = ANNOUNCE;
                pending.clear();
                reserve.clear();
                arenaTime = 0;
                arenaHurts = 0;
                arenaLives = 0;
            }
            if ((roomsDone & 1 << at) == 0) {
                if (one[0] == RunLevels.ARENA) {
                    // What is in it is its waves.
                } else if (one[0] == RunLevels.TRAMPLE) {
                    // Signs keep coming, for fifteen seconds.
                    gameLeft = 15;
                    gameLasts = 15;
                    caught = 0;
                    sinceDrop = 0;
                } else if (one[0] == RunLevels.RANGE) {
                    // An ink gun that does not run dry, for twelve seconds; what she carried is hers again after.
                    gameLeft = 12;
                    gameLasts = 12;
                    caught = 0;
                    sinceDrop = 1;
                    heldWeapon = weapon;
                    heldRounds = rounds;
                    ranging = true;
                    weapon = GUN;
                    rounds = 999;
                } else if (one[0] == RunLevels.GAME) {
                    gameLeft = FALLING;
                    gameLasts = FALLING;
                    caught = 0;
                    sinceDrop = 0;
                } else if (one[0] == RunLevels.PASSAGE) {
                    // A way through is a way through: there is nothing to be done in it but go.
                    roomsDone |= 1 << at;
                }
            }
        } else {
            far = one[8] >= 0 && Math.abs(middle - one[8] - .5f) < .8f;
            room = -1;
            put(far ? one[5] : one[1], far ? one[9] : one[2]);
        }
        entered++;
        return true;
    }

    private void put(int column, int floor) {
        x = column + (1 - WIDE) / 2;
        y = floor - TALL;
        speedAcross = 0;
        speedDown = 0;
        riding = -1;
        safe = Math.max(safe, .5f);
    }

    /** Whether the place she is in is shut until what happens in it is over. */
    boolean sealed() {
        return room >= 0 && (level.rooms.get(room)[0] == RunLevels.ARENA && wave > 0 || level.rooms.get(room)[0] >= RunLevels.GAME && gameLeft > 0);
    }

    /**
     * A wave is mustered: for each of those who are to come in it, a place in the arena
     * is chosen and marked, and a moment later that one is there. Those that walk come in
     * on the floor and the platforms; those that throw and shoot, on the perches; a dragon
     * in the open air; an outsized one on the floor; and the Old Book in the middle of it.
     * None comes in on top of her. Each is marked a little after the one before.
     */
    private void muster(int[] one) {
        reserve.clear();
        for (int kind : RunLevels.host(level.number, wave, one[10] != 0)) reserve.add(kind);
        fed = 0;
        feed(one);
        armoury(one);
    }

    /**
     * Those of the wave who wait their turn are marked to come in, for as far as there is
     * room: no more are there at once, with those already marked, than the place is to
     * hold. The first of a wave are marked one a little after another; after that, one for
     * each that falls.
     */
    private void feed(int[] one) {
        int there = pending.size(), batch = 0;
        for (Foe foe : foes) if (foe.spawned && !foe.flat) there++;
        while (there < ceiling && !reserve.isEmpty()) {
            int next = reserve.remove(0), kind = next % RunLevels.GIANT;
            boolean giant = next >= RunLevels.GIANT, ranged = kind == RunLevels.THROWER || kind == RunLevels.GUNNER;
            int type = kind == RunLevels.DRAGON ? RunLevels.IN_AIR : ranged ? RunLevels.ON_PERCH : giant || kind == RunLevels.OLD_BOOK || fed % 2 == 0
                    ? RunLevels.ON_FLOOR : RunLevels.ON_PLATFORM;
            int[] where = kind == RunLevels.OLD_BOOK ? new int[]{room, RunLevels.ON_FLOOR, (one[3] + one[4]) / 2, one[11]} : place(type, 7, 60);
            if (where == null) where = place(RunLevels.ON_FLOOR, 7, 200);
            if (where == null) where = place(RunLevels.ON_PLATFORM, 3, 200);
            // (No place free for it just now: it waits, and those behind it with it.)
            if (where == null) {
                reserve.add(0, next);
                break;
            }
            fed++;
            pending.add(new float[]{next, where[2] + .5f, where[3], MARKED + batch++ * .22f, where[1]});
            there++;
        }
    }
    private int fed;

    /**
     * Things to fight with lie about a cellar for each of its waves, on three of its
     * storeys, a different kind on each and different from wave to wave; in the last wave
     * one of them is a book. What lies there already is left.
     */
    private void armoury(int[] one) {
        int[] kinds = {PEN, KNIFE, BRUSH, SWORD, GUN};
        for (int which = 0; which < 3; which++) {
            int wanted = one[3] + (one[4] - one[3]) * (which * 2 + 1) / 6, storey = one[11] - (1 + which * 2) * RunLevels.STOREY;
            int[] where = null;
            for (int[] spot : level.spawns) {
                if (spot[0] != room || spot[1] != RunLevels.ON_PLATFORM) continue;
                if (where == null || Math.abs(spot[2] - wanted) + Math.abs(spot[3] - storey) * 3 < Math.abs(where[2] - wanted) + Math.abs(where[3] - storey) * 3) where = spot;
            }
            if (where == null) continue;
            boolean there = false;
            for (int[] arm : arms) there |= Math.abs(arm[1] - where[2]) < 3 && Math.abs(arm[2] - where[3] + 1) < 2;
            boolean book = wave == WAVES && which == 1;
            // (The book of the last wave takes the place of whatever lies where it is to lie.)
            if (there && book) for (int[] arm : arms) if (Math.abs(arm[1] - where[2]) < 3 && Math.abs(arm[2] - where[3] + 1) < 2) arm[0] = BOOK;
            if (!there) arms.add(new int[]{book ? BOOK : kinds[(level.number + wave + which) % kinds.length], where[2], where[3] - 1});
        }
    }

    /** A place in the arena she is in where something of a kind may come in, not too near her and not too far, and not where something else is about to; or none. */
    private int[] place(int type, float nearest, float furthest) {
        int[] best = null;
        int count = 0;
        for (int[] spot : level.spawns) if (spot[0] == room && spot[1] == type) count++;
        if (count == 0) return null;
        dice = dice * 1103515245 + 12345;
        int from = (dice >>> 16) % count, seen = 0;
        for (int turn = 0; turn < 2 && best == null; turn++) {
            seen = 0;
            for (int[] spot : level.spawns) {
                if (spot[0] != room || spot[1] != type) continue;
                if (seen++ < from && turn == 0) continue;
                float away = Math.abs(spot[2] + .5f - x - WIDE / 2);
                boolean taken = false;
                for (float[] other : pending) taken |= Math.abs(other[1] - spot[2] - .5f) < 1.5f && Math.abs(other[2] - spot[3]) < 1.5f;
                if (taken || away < nearest && Math.abs(spot[3] - y - TALL) < 3 || away > furthest) continue;
                best = spot;
                break;
            }
        }
        return best;
    }

    /** One who was marked is there: standing on the place it was marked at, or, a dragon, in the air over it. */
    private void arrive(float[] mark) {
        int kind = (int) mark[0] % RunLevels.GIANT;
        boolean giant = mark[0] >= RunLevels.GIANT;
        Foe foe = new Foe(kind);
        foe.spawned = true;
        if (kind == RunLevels.OLD_BOOK) {
            foe.left = health(RunLevels.OLD_BOOK);
            foe.most = foe.left;
            foe.step = 3;
            foe.act = .5f;
            // He takes a moment to wake.
            foe.enter = 1.6f;
            woken++;
        } else {
            // An outsized one is nearly twice the size, and takes six blows.
            foe.left = giant ? 6 : 1;
            if (giant) foe.size = 1.9f;
            foe.dazed = .5f;
        }
        foe.x = mark[1] - foe.wide() / 2;
        foe.ground = (kind == RunLevels.DRAGON ? mark[2] + 3 : mark[2]) - foe.tall();
        foe.y = foe.ground;
        if (kind == RunLevels.WORD) foe.y = foe.ground - WORD_HANGS;
        foe.home = foe.x;
        foe.leftward = x + WIDE / 2 < mark[1];
        foe.clock = mark[1] * .37f % HOP_EVERY;
        foes.add(foe);
    }

    /**
     * How well a cellar was fought, from how long it took, how many times she was hurt in
     * it, and how many lives it cost her: without a hurt and inside the time, the best;
     * with three hurts at most and no life lost, the next; with no life lost, the next;
     * and otherwise only done.
     */
    static int rank(float time, int hurts, int lives) {
        return lives > 0 ? RANK_C : hurts <= 2 && time <= PAR ? RANK_S : hurts <= 8 ? RANK_A : RANK_B;
    }

    /** How the wave stands and for how much longer; what is marked to come in; whether the lights are out; how the cellar is going; and how the last one cleared was ranked, or -1. */
    int wavePhase() { return room >= 0 && wave > 0 ? wavePhase : 0; }
    float waveClock() { return waveClock; }
    List<float[]> pending() { return pending; }
    boolean dark() { return room >= 0 && wave == 2 && wavePhase == FOUGHT && level.rooms.get(room)[0] == RunLevels.ARENA; }
    float arenaTime() { return arenaTime; }
    int arenaHurts() { return arenaHurts; }
    int arenaLives() { return arenaLives; }
    int ranked() { return ranked; }

    /** What happens in the place she is in: a cellar's waves, one after another until the last is cleared; and pluses falling for a while. */
    private void rooms(float seconds) {
        int[] one = level.rooms.get(room);
        if (one[0] == RunLevels.ARENA && wave > 0) {
            if (wavePhase == ANNOUNCED) {
                // A beat of stillness; then the places are marked.
                if ((waveClock -= seconds) > 0) return;
                wavePhase = FOUGHT;
                sinceMoved = 0;
                sinceShelf = 0;
                muster(one);
                // For the last wave something to help lies in the middle of the place: a ribbon, a different one from land to land.
                if (wave == WAVES) {
                    int[] ribbons = {YELLOW, GREEN, RED, BROWN, PURPLE, BLUE};
                    int[] where = null;
                    for (int[] spot : level.spawns) {
                        if (spot[0] == room && spot[1] == RunLevels.ON_PLATFORM && (where == null
                                || Math.abs(spot[2] - (one[3] + one[4]) / 2) < Math.abs(where[2] - (one[3] + one[4]) / 2))) where = spot;
                    }
                    if (where != null) items.add(new int[]{FORM + ribbons[level.number % ribbons.length], where[2] + 2, where[3] - 1, 0});
                }
                return;
            }
            if (wavePhase == CLEARED) {
                // A breath; then the next is announced.
                if ((waveClock -= seconds) > 0) return;
                wave++;
                wavePhase = ANNOUNCED;
                waveClock = ANNOUNCE;
                return;
            }
            arenaTime += seconds;
            // Those that are down and have had their moment are gone from the place; and for each that has fallen one more is marked.
            for (int index = foes.size() - 1; index >= 0; index--) {
                Foe gone = foes.get(index);
                if (gone.spawned && gone.flat && gone.act > 3 && !RunLevels.boss(gone.kind)) foes.remove(index);
            }
            feed(one);
            for (int index = pending.size() - 1; index >= 0; index--) {
                float[] mark = pending.get(index);
                if ((mark[3] -= seconds) <= 0) {
                    pending.remove(index);
                    arrive(mark);
                }
            }
            // One of them that is a long way from her, on some other storey or at the far end, comes after her: it is gone from where it
            // was, and marked to come in again nearer.
            if ((sinceMoved += seconds) > 5) {
                sinceMoved = 0;
                for (int index = 0; index < foes.size(); index++) {
                    Foe foe = foes.get(index);
                    if (!foe.spawned || foe.flat || RunLevels.boss(foe.kind) || foe.kind == RunLevels.DRAGON || foe.size > 1.5f) continue;
                    if (Math.abs(foe.x - x) < 24 && Math.abs(foe.y + foe.tall() - y - TALL) < 9) continue;
                    boolean ranged = foe.kind == RunLevels.THROWER || foe.kind == RunLevels.GUNNER;
                    int[] where = place(ranged ? RunLevels.ON_PERCH : RunLevels.ON_PLATFORM, 6, 16);
                    if (where == null) where = place(RunLevels.ON_FLOOR, 6, 20);
                    if (where == null) continue;
                    foes.remove(index);
                    pending.add(new float[]{foe.kind, where[2] + .5f, where[3], MARKED, where[1]});
                    break;
                }
            }
            // In the last wave what is on the shelves comes down: it hangs over where she is going for a moment, and falls.
            if (wave == WAVES && (sinceShelf += seconds) > 3.2f) {
                sinceShelf = 0;
                shoot(DEBRIS, false, x + WIDE / 2 + speedAcross * .6f, Math.max(one[12] + .5f, y - 7), 0, 0, 3);
            }
            if (!pending.isEmpty() || !reserve.isEmpty()) return;
            for (Foe foe : foes) if (foe.spawned && !foe.flat) return;
            if (wave < WAVES) {
                wavePhase = CLEARED;
                waveClock = BREATH;
                for (Shot shot : shots) if (!shot.hers) shot.left = 0;
                return;
            }
            // Cleared: what it is worth is by how well it was fought, and twenty more where the Old Book slept; and a heart if she is short of one.
            wave = 0;
            wavePhase = 0;
            ranked = rank(arenaTime, arenaHurts, arenaLives);
            roomsDone |= 1 << room;
            roomWins++;
            bonus += RANK_PAYS[ranked] + (one[10] != 0 ? 20 : 0);
            for (Shot shot : shots) if (!shot.hers) shot.left = 0;
            heal();
        } else if (one[0] == RunLevels.RANGE && gameLeft > 0) {
            // Three targets hang at a time; one hit, another is hung somewhere else.
            int hung = 0;
            for (Foe foe : foes) if (foe.kind == RunLevels.TARGET && !foe.flat) hung++;
            if (hung < 3 && (sinceDrop += seconds) > .35f) {
                sinceDrop = 0;
                dice = dice * 1103515245 + 12345;
                Foe target = new Foe(RunLevels.TARGET);
                target.spawned = true;
                target.left = 1;
                target.x = one[3] + 4 + (dice >>> 16) % 1000 / 1000f * (one[4] - one[3] - 6);
                target.ground = one[11] - target.tall() - (dice >>> 8) % 4 * .9f;
                target.y = target.ground;
                target.home = target.x;
                foes.add(target);
            }
            if ((gameLeft -= seconds) <= 0) {
                gameLeft = 0;
                roomsDone |= 1 << room;
                roomWins++;
                if (caught >= ENOUGH) bonus += 10;
                unarm();
            }
        } else if (one[0] == RunLevels.TRAMPLE && gameLeft > 0) {
            // Never more than five of them at once: one more every so often, somewhere along the room, facing her.
            int about = 0;
            for (Foe foe : foes) if (foe.spawned && !foe.flat) about++;
            if (about < 5 && (sinceDrop += seconds) > .7f) {
                sinceDrop = 0;
                dice = dice * 1103515245 + 12345;
                Foe foe = new Foe(level.chapter >= 2 && (dice >>> 12) % 3 == 0 ? RunLevels.SLIME : RunLevels.MINUS);
                foe.spawned = true;
                foe.left = 1;
                foe.x = one[3] + 3 + (dice >>> 16) % 1000 / 1000f * (one[4] - one[3] - 6);
                // Not on top of her.
                if (Math.abs(foe.x - x) < 2.5f) foe.x = x + (x < (one[3] + one[4]) / 2f ? 4 : -4);
                foe.ground = one[11] - foe.tall();
                foe.y = foe.ground;
                foe.home = foe.x;
                foe.leftward = foe.x > x;
                foe.dazed = .5f;
                foes.add(foe);
            }
            if ((gameLeft -= seconds) <= 0) {
                gameLeft = 0;
                roomsDone |= 1 << room;
                roomWins++;
                if (caught >= ENOUGH) bonus += 10;
                for (int index = foes.size() - 1; index >= 0; index--) if (foes.get(index).spawned && !RunLevels.boss(foes.get(index).kind)) foes.remove(index);
            }
        } else if (one[0] == RunLevels.GAME && gameLeft > 0) {
            if ((sinceDrop += seconds) > .42f) {
                sinceDrop = 0;
                dice = dice * 1103515245 + 12345;
                // Over the middle of the room, where she can get under any of them in time.
                float at = (one[3] + one[4]) / 2f - 4.5f + (dice >>> 16) % 1000 / 1000f * 9;
                shoot(PLUSDROP, false, at, one[11] - 7.6f, 0, 5.5f, 2);
            }
            if ((gameLeft -= seconds) <= 0) {
                gameLeft = 0;
                roomsDone |= 1 << room;
                roomWins++;
                if (caught >= ENOUGH) bonus += 10;
                for (Shot shot : shots) if (shot.kind == PLUSDROP) shot.left = 0;
            }
        }
    }

    /** Out of the place she was in, sent back to the flag: it is as it was before she went in, and what was in it with her is gone. */
    /** The range is over: its targets are gone, and what she carried before it is in her hands again. */
    private void unarm() {
        for (Foe foe : foes) if (foe.kind == RunLevels.TARGET) foe.flat = true;
        if (!ranging) return;
        ranging = false;
        weapon = heldWeapon;
        rounds = heldRounds;
    }

    private void quit() {
        quitting = false;
        room = -1;
        wave = 0;
        wavePhase = 0;
        pending.clear();
        reserve.clear();
        gameLeft = 0;
        unarm();
        if (!locked) for (int index = foes.size() - 1; index >= 0; index--) if (foes.get(index).spawned) foes.remove(index);
    }

    /** The place off the land she is in, or -1, and where it begins and ends; which she has been into, finished, and broken through to, as bits. */
    int room() { return room; }
    int roomFrom() { return level.rooms.get(room)[3]; }
    int roomTo() { return level.rooms.get(room)[4]; }
    int roomsFound() { return roomsFound; }
    int roomsDone() { return roomsDone; }
    int revealed() { return revealed; }
    /** Counts that only go up: doors used, hidden doors broken through to, and cellars cleared or falls of pluses seen out. */
    int entered() { return entered; }
    int reveals() { return reveals; }
    int roomWins() { return roomWins; }
    /** Which wave a cellar is at, 0 when none; how much longer pluses fall; and how many she has caught. */
    int wave() { return wave; }
    float gameLeft() { return gameLeft; }
    int caught() { return caught; }
    /** How long the game in a room lasts in all; how many of a cellar's wave still stand; and how many chests she has opened, a count that only goes up. */
    float gameLasts() { return gameLasts; }
    int waveLeft() {
        int left = pending.size() + reserve.size();
        for (Foe foe : foes) if (foe.spawned && !foe.flat) left++;
        return left;
    }
    int chests() { return chests; }
    /** A life more, won in this land. */
    void spare() { lives++; }

    /**
     * Her colour, taken all together, 0 for her own: black while black lies on her that was
     * put there; white when she is white; and otherwise whichever of those she wears was
     * put on last. And how much longer that lasts.
     */
    int form() {
        if (cursed > 0) return BLACK;
        if (white()) return WHITE;
        if (last != 0 && worn[last] > 0) return last;
        for (int colour = BLACK; colour >= RED; colour--) if (worn[colour] > 0) return colour;
        return 0;
    }
    float formLeft() {
        int form = form();
        if (cursed > 0) return cursed;
        if (form != WHITE) return form == 0 ? 0 : worn[form];
        float least = pale > 0 ? pale : 99;
        for (int colour = RED; colour <= BLACK; colour++) if (worn[colour] > 0) least = Math.min(least, worn[colour]);
        return least;
    }
    /** Whether she has a power, by the colour it belongs to: each colour she wears keeps its own, however many she wears. */
    boolean has(int power) { return power >= RED && power <= BLACK && worn[power] > 0 || mastered && power >= RED && power < BLACK; }

    /**
     * The white that is truly hers: every colour at once, with no time to it, and more of
     * its own: she runs faster than yellow runs, flies freely, and what she holds the
     * strike for is the beam, which goes through ledges, walls and the ground and lands on
     * everything along it, several times a second, for as long as it lasts. Black cannot be
     * put on her. It is given by the story; nothing in a land gives it.
     */
    private boolean mastered, beamLeft;
    private float beam, beamHeld, beamWait, beamTick;
    private int beams;
    static final float BEAM_CHARGE = .45f, BEAM_LASTS = 1.3f, BEAM_WAIT = 2.5f, BEAM_REACH = 16, BEAM_WIDE = .7f;

    void master() {
        mastered = true;
        cursed = 0;
        mingle();
    }

    boolean mastered() { return mastered; }

    /**
     * What she has to keep has changed while she is in a land: bought at the counter by a
     * stall, or won. It is hers there and then: a heart more is a heart more now, a life
     * more a life more now, and the rest holds from the next time it matters, the second
     * jump in the air from her next jump.
     */
    void grant(Perks now) {
        if (now.hearts > perks.hearts) halves = Math.min(now.hearts * 2, halves + (now.hearts - perks.hearts) * 2);
        if (now.lives > perks.lives) lives += now.lives - perks.lives;
        perks.hearts = now.hearts;
        perks.lives = now.lives;
        perks.wings = now.wings;
        perks.reach = now.reach;
        perks.lasting = now.lasting;
        perks.shots = now.shots;
        perks.flagHeart = now.flagHeart;
        perks.inkHurts = now.inkHurts;
        perks.colours |= now.colours;
        perks.recharge = now.recharge;
        perks.formLonger = now.formLonger;
        perks.together = now.together;
        perks.resists |= now.resists;
    }

    /** Whether she has a second jump in the air to her name. */
    boolean winged() { return perks.wings; }
    /** How much longer the beam lasts; which way it goes; how far it is gathered, from nothing to one; how long before it can be again; and how many there have been. */
    float beam() { return beam; }
    boolean beamLeft() { return beamLeft; }
    float beamCharge() { return beam > 0 ? 0 : Math.min(1, beamHeld / BEAM_CHARGE); }
    float beamWait() { return beamWait; }
    int beams() { return beams; }

    private void shine(float seconds, boolean strike) {
        if (beamWait > 0) beamWait -= seconds;
        if (beam > 0) {
            beam -= seconds;
            if ((beamTick -= seconds) <= 0) {
                beamTick = .12f;
                float middle = y + TALL * .5f, from = x + WIDE / 2;
                for (Foe foe : foes) {
                    float at = foe.x + foe.wide() / 2;
                    if (foe.flat || absent(foe) || foe.dazed > 0 || (beamLeft ? at > from || at < from - BEAM_REACH : at < from || at > from + BEAM_REACH)
                            || foe.y > middle + BEAM_WIDE || foe.y + foe.tall() < middle - BEAM_WIDE) continue;
                    // Whoever waits behind her shield is not touched by it; whoever is spent is.
                    if (RunLevels.boss(foe.kind) && !open(foe)) continue;
                    beat(foe, SURE);
                }
                // And what is thrown at her along it is gone.
                for (Shot shot : shots) {
                    if (!shot.hers && Math.abs(shot.y - middle) < BEAM_WIDE && (beamLeft ? shot.x < from : shot.x > from) && Math.abs(shot.x - from) < BEAM_REACH) shot.left = 0;
                }
            }
            if (beam <= 0) beamWait = BEAM_WAIT;
            return;
        }
        if (strike && beamWait <= 0) {
            if ((beamHeld += seconds) >= BEAM_CHARGE) {
                beam = BEAM_LASTS;
                beamHeld = 0;
                beamTick = 0;
                beamLeft = facingLeft;
                beams++;
            }
        } else {
            beamHeld = 0;
        }
    }
    /** The colours she wears, a bit for each, the first for red; and how much longer one of them is on, how long it was put on for, and how long it must still wait. */
    int mix() {
        int mix = 0;
        if (mastered) return ALL;
        for (int colour = RED; colour <= BLACK; colour++) if (worn[colour] > 0) mix |= 1 << colour - 1;
        return mix;
    }
    float on(int colour) { return worn[colour]; }
    float lasted(int colour) { return lasted[colour]; }
    float waiting(int colour) { return waits[colour]; }
    /** Whether she is white: all seven worn at once, or white from the rare ribbon. */
    boolean white() { return pale > 0 || (mix() & ALL) == ALL; }
    /** Which named mixes she wears now and has worn in this land, as bits; which formed last, or -1; and counts that only go up: mixes formed, colours gone, and times white of her own seven. */
    int mixed() { return mixed; }
    int mixes() { return mixes; }
    int lastMix() { return lastMix; }
    int mixings() { return mixings; }
    int faded() { return faded; }
    int whitened() { return whitened; }
    /** How much longer black that was put on her lies on her. */
    float cursed() { return cursed; }
    private boolean named(int which) { return (mixed & 1 << which) != 0; }
    /** Counts that only go up: ribbons taken, doors gone through, landings that shook the ground, signs cleared at a stroke, blows turned aside, the censor's strokes. */
    int formed() { return formed; }
    int doorsUsed() { return doorsUsed; }
    int quakes() { return quakes; }
    int cleared() { return cleared; }
    int parried() { return parried; }
    int censors() { return censors; }
    /** How much longer the censor's bars lie over what she sees. */
    float censored() { return censored; }
    /** Which of the land's moving ledges she stands on, or -1; and where each is now: its left end and its top. */
    int riding() { return riding; }
    float moverX(int index, float at) {
        int[] mover = level.movers.get(index);
        return mover[0] + (mover[4] == 0 ? sway(mover, at) : 0);
    }
    float moverY(int index, float at) {
        int[] mover = level.movers.get(index);
        return mover[1] - (mover[4] != 0 ? sway(mover, at) : 0);
    }
    private static float sway(int[] mover, float at) { return mover[3] * (.5f - .5f * (float) Math.cos(at * 2 * Math.PI / (mover[5] / 10f))); }
    /** How long a ledge that crumbles has before it falls, 0 if no one has stood on it; below 0 while it is gone. */
    float crumbling(int column, int row) {
        Float left = crumble.get(column * 64 + row);
        return left == null ? 0 : left;
    }
    /** Whether a sign can be hurt just now: those who wait at the end, only while they are spent after something they have done. */
    boolean open(Foe foe) { return !RunLevels.boss(foe.kind) || foe.step == 2 && !absent(foe); }
    /**
     * How one of those who wait stands toward a blow just now: bare, when one would land;
     * shielded, when it would be turned aside; reeling, for the moment after one has landed,
     * when nothing more does; and away, while arriving, changing or out of sight, when
     * nothing touches them at all. And for how much longer they are bare, if they are.
     */
    static final int BARE = 0, SHIELDED = 1, REELING = 2, AWAY = 3;
    int guard(Foe foe) { return foe.flat ? REELING : absent(foe) ? AWAY : !open(foe) ? SHIELDED : foe.dazed > 0 ? REELING : BARE; }
    float bareLeft(Foe foe) { return RunLevels.boss(foe.kind) && foe.step == 2 ? Math.max(0, foe.act) : 0; }
    /** Whether one of those who wait is not there to be touched: not come yet, arriving, changing, or out of sight. */
    boolean absent(Foe foe) { return foe.away || foe.enter > 0 || foe.flourish > 0 || foe.unseen; }
    /** How much each of those who wait can take. */
    static int health(int kind) {
        switch (kind) {
            case RunLevels.ILLYA:
            case RunLevels.TSUBAKIEH:
                return 6;
            case RunLevels.GOKHAE:
                return 8;
            case RunLevels.ZERO:
            case RunLevels.YEOBAEK:
                return 9;
            case RunLevels.MIRI:
            case RunLevels.GARI:
            case RunLevels.GUARD:
            case RunLevels.BUTLER:
            case RunLevels.OLD_BOOK:
            case RunLevels.GECKO:
                return 5;
            case RunLevels.WYRM:
                return 4;
            case RunLevels.BLOT:
                return 6;
            case RunLevels.BYEORU:
            case RunLevels.JUPAN:
                return 7;
            case RunLevels.SUMBI:
            case RunLevels.PADS:
                return 8;
            case RunLevels.PARTY:
                return 9;
            case RunLevels.KING:
                return 11;
            default:
                return 12;
        }
    }
    /** How far through the fight one of those who wait is: 0, then 1 once a third of what they can take is gone, then 2. The twins count as one. */
    int phase(Foe foe) {
        int most = foe.most, gone = foe.most - Math.max(0, foe.left);
        if (foe.kind == RunLevels.MIRI || foe.kind == RunLevels.GARI) {
            for (Foe other : foes) {
                if (other != foe && (other.kind == RunLevels.MIRI || other.kind == RunLevels.GARI)) {
                    most += other.most;
                    gone += other.most - Math.max(0, other.left);
                }
            }
        }
        return most <= 0 ? 0 : Math.min(2, gone * 3 / most);
    }
    /** Whether she is shut in with whoever waits; where that place begins and ends; and how much longer its ways stay shut after the last of them is down. */
    boolean locked() { return locked; }
    int arenaFrom() { return level.arena; }
    int arenaTo() { return level.gate; }
    float opening() { return opening; }
    /** Whether any of those who wait still stands. */
    boolean standing() {
        // (Not the Old Book in his cellar, nor one met in the middle of a land: they are no one's gatekeepers.)
        for (Foe foe : foes) if (RunLevels.boss(foe.kind) && !RunLevels.rover(foe.kind) && !foe.flat && !foe.spawned) return true;
        return false;
    }
    /**
     * Counts that only go up: times she has been shut in, times one of those who wait has
     * changed, things they have done, with the last of them as ten times who did it and
     * which, times the ways have opened again, and times Zero has taken what she carried.
     * And how many signs in a row she has hit with something thrown.
     */
    int entrances() { return entrances; }
    int phases() { return phases; }
    int specials() { return specials; }
    int special() { return special; }
    int unlocks() { return unlocks; }
    int wiped() { return wiped; }
    int streak() { return sinceShot < 1.6f ? streak : 0; }

    /** What she carries, and how many shots an ink gun has left in it. */
    int weapon() { return weapon; }
    int rounds() { return rounds; }
    /** How far through a swing of the brush or sword she is, from 0 to 1; below zero when she is not swinging. */
    float swinging() { return swing > 0 ? 1 - swing / SWING : -1; }
    /** How many times she has used what she carries, picked something up, had something thrown at her, and sent or wiped it away. */
    int strikes() { return strikes; }
    int picked() { return picked; }
    int thrown() { return thrown; }
    int turned() { return turned; }

    float x() { return x; }
    float y() { return y; }
    boolean onGround() { return onGround; }
    boolean facingLeft() { return facingLeft; }
    /** Whether she is moving across fast enough to be running. */
    boolean running() { return Math.abs(speedAcross) > .6f; }
    /** Whether she has just been knocked back, and cannot be again for a moment. */
    boolean safe() { return safe > 0; }
    /** Her hearts: how many she has any of, and the same counted in halves; her lives; and how many of those she has lost in this land. */
    int hearts() { return (halves + 1) / 2; }
    int halves() { return halves; }
    int lives() { return lives; }
    int livesLost() { return livesLost; }
    /** Whether she is flinching from a hurt just now. */
    boolean flinching() { return flinch > 0; }
    int state() { return state; }
    float time() { return time; }
    int total() { return total; }
    int collected() { return collected; }
    int flattened() { return flattened; }
    int hurts() { return hurts; }
    /** How many times she has left the ground by jumping, come down onto it again, and been thrown up by a spring. */
    int jumps() { return jumps; }
    int landings() { return landings; }
    int springs() { return springs; }
    /** How many of the land's flags she has passed, and how many times Zero has been landed on. */
    int flagsPassed() { return flagsPassed; }
    int blows() { return blows; }
    /** How fast she is moving down, in squares a second: below zero while she rises. */
    float speedDown() { return speedDown; }
    float speedAcross() { return speedAcross; }
    /** The middle of the plus last collected, and where the sign last flattened stood. */
    float plusX() { return plusX; }
    float plusY() { return plusY; }
    float foeX() { return foeX; }
    float foeY() { return foeY; }

    /** Whether the gate will let her through: always, but for the land where Zero stands in front of it. */
    boolean gateOpen() { return !level.guarded || !standing() && opening <= 0; }

    /** The things one of those who wait does in a third of the fight, in order: for checking that each is done. */
    static int[] does(int kind, int phase) { return PATTERNS[kind][phase]; }

    /** Takes from her the notes and the ink that keep her from harm: for checking the rules. */
    void bare() {
        notes = 0;
        shield = false;
        safe = 0;
    }

    /** Lands a blow on one of them, if one would land: for looking at one met in the middle of a land from its coming to its going, in a build made for testing. */
    void deal(Foe foe) {
        if (!foe.flat && !absent(foe) && open(foe) && foe.dazed <= 0) beat(foe);
    }

    /** Deals with the nearest of a cellar's wave that is there, as if she had: for looking at a cellar from end to end in a build made for testing. */
    void sweep() {
        Foe nearest = null;
        for (Foe foe : foes) {
            if (!foe.spawned || foe.flat) continue;
            if (nearest == null || Math.abs(foe.x - x) + Math.abs(foe.y - y) < Math.abs(nearest.x - x) + Math.abs(nearest.y - y)) nearest = foe;
        }
        if (nearest == null) return;
        nearest.flat = true;
        nearest.act = 0;
        foeX = nearest.x + nearest.wide() / 2;
        foeY = nearest.y + nearest.tall();
        flattened++;
    }

    /** Puts something in her hands: for checking the rules, and for looking at them. */
    void arm(int with) {
        weapon = with;
        rounds = with == GUN ? perks.shots : 0;
    }

    /** Puts her somewhere, standing still: for checking the rules from a place of one's choosing. */
    void place(float across, float down) {
        x = across;
        y = down;
        speedAcross = 0;
        speedDown = 0;
        onGround = false;
    }

    // Moving it on

    /**
     * Moves the game on by a length of time, with the arrows and the jump button held or not.
     * A long time is taken in small steps, so nothing passes through anything.
     */
    void step(float seconds, boolean left, boolean right, boolean jump) { step(seconds, left, right, jump, false); }

    /** The same, with the button that uses what she carries held or not. */
    void step(float seconds, boolean left, boolean right, boolean jump, boolean strike) {
        seconds = Math.min(seconds, .1f);
        while (seconds > 0 && state == PLAYING) {
            float slice = Math.min(STEP, seconds);
            once(slice, left, right, jump, strike);
            seconds -= slice;
        }
    }

    private void once(float seconds, boolean left, boolean right, boolean jump, boolean strike) {
        time += seconds;
        if (safe > 0) safe -= seconds;
        // Each colour she wears has its own time; each that is gone waits its own wait, which begins when it goes.
        for (int colour = RED; colour <= BLACK; colour++) {
            if (worn[colour] > 0) {
                if ((worn[colour] -= seconds) <= 0) end(colour, true);
            } else if (waits[colour] > 0) {
                waits[colour] = Math.max(0, waits[colour] - seconds);
            }
        }
        if (cursed > 0) cursed -= seconds;
        if (pale > 0) pale -= seconds;
        if (censored > 0) censored -= seconds;
        if (doorWait > 0) doorWait -= seconds;
        if (offIt > 0) offIt -= seconds;
        sinceShot += seconds;
        if (leaving) leave();
        if (quitting) quit();
        if (rewave) {
            // A life lost in a cellar: she is back at its door, and the wave she was in is announced again, from its beginning.
            rewave = false;
            for (int index = foes.size() - 1; index >= 0; index--) if (foes.get(index).spawned) foes.remove(index);
            pending.clear();
            reserve.clear();
            wavePhase = ANNOUNCED;
            waveClock = ANNOUNCE;
        }
        if (quest == ASKED && level.quest == RunLevels.TIME) asked(seconds);
        if (room >= 0) rooms(seconds);
        // A moment after the last of them is down, both ways open.
        if (opening > 0 && (opening -= seconds) <= 0) {
            locked = false;
            unlocks++;
        }
        // Shut in with nothing in her hands, what will do against them comes back.
        if (!locked || weapon != UNARMED) rearm = 0;
        else if ((rearm += seconds) > 3) {
            rearm = 0;
            restock(true);
        }
        // A moving ledge carries whoever stands on it.
        if (riding >= 0) {
            x += moverX(riding, time) - moverX(riding, time - seconds);
            y = moverY(riding, time) - TALL;
        }
        crumbling(seconds);
        char under = onGround && riding < 0 ? at((int) Math.floor(x + WIDE / 2), (int) Math.floor(y + TALL + .05f)) : RunLevels.AIR;
        sinceGround = onGround ? 0 : sinceGround + seconds;
        boolean pressed = jump && !jumpHeld;
        sinceJump = pressed ? 0 : sinceJump + seconds;
        if (rush > 0) rush -= seconds;
        if (magnet > 0) magnet -= seconds;
        if (notes > 0) notes -= seconds;
        if (onGround) {
            combo = 0;
            airJumps = 0;
        }
        // Charmed, she goes the way she did not mean to.
        if (charmed > 0) {
            charmed -= seconds;
            boolean swap = left;
            left = right;
            right = swap;
        }

        // Across: she speeds up toward the arrow that is held, and slows when neither is.
        float fast = rush > 0 ? RUN * 1.3f : RUN, leap = rush > 0 ? LEAP * 1.1f : LEAP;
        if (has(YELLOW)) fast = RUN * (mastered ? 2.1f : 1.7f);
        float wanted = left == right ? 0 : left ? -fast : fast;
        // On slime there is little to push against, and little to stop her.
        float push = (wanted == 0 ? DRAG : onGround ? PUSH : PUSH_IN_AIR) * seconds * (under == RunLevels.SLICK ? .16f : 1);
        // Flinching, she goes the way she was sent, whatever is held.
        if (flinch > 0) flinch -= seconds;
        else speedAcross = speedAcross < wanted ? Math.min(wanted, speedAcross + push) : Math.max(wanted, speedAcross - push);
        if (wanted != 0) facingLeft = wanted < 0;
        // Firm ground under both feet, with nothing in it to hurt her, is where a fall will put her back.
        if (onGround && riding < 0 && under != RunLevels.AIR) {
            int row = (int) Math.floor(y + TALL + .05f), from = (int) Math.floor(x), to = (int) Math.floor(x + WIDE - .001f);
            boolean sound = true;
            for (int column = from; column <= to; column++) {
                char below = at(column, row), at = at(column, row - 1);
                sound &= below != RunLevels.LEDGE && solid(below) && below != RunLevels.SLICK && at != RunLevels.SPIKE && at != RunLevels.LAVA
                        && crumbling(column, row) == 0;
            }
            if (sound) {
                firmX = x;
                firmY = y;
            }
        }

        // Up: a press of the button a moment early, or a moment after the edge, still jumps.
        if (sinceJump <= EARLY && sinceGround <= LATE) {
            speedDown = -leap;
            sinceJump = 1;
            sinceGround = 1;
            onGround = false;
            jumps++;
        } else if (pressed && !onGround && airJumps < (perks.wings ? 1 : 0) + (named(STEAMS) ? 1 : 0)) {
            // With her wings, one more jump in the air; and in steam, which lifts her, another.
            // With her wings, one more jump in the air.
            speedDown = -LEAP * .88f;
            sinceJump = 1;
            sprung = false;
            airJumps++;
            flaps++;
        }
        // Letting go early makes for a lower jump, but not for a lower flight off a spring.
        if (!jump && !sprung && speedDown < -SHORT_LEAP) speedDown = -SHORT_LEAP;
        jumpHeld = jump;
        // Held down in the air, she comes down faster than she falls.
        boolean plunging = down && !onGround;
        speedDown = Math.min(plunging ? PLUNGE : FALL, speedDown + GRAVITY * (plunging ? 1.8f : 1) * seconds);
        if (plunging && speedDown > FALL) plunged = true;
        char about = at((int) Math.floor(x + WIDE / 2), (int) Math.floor(y + TALL * .6f));
        // A draught lifts her; and with the wind in her hair she flies for as long as the jump is held.
        if (about == RunLevels.DRAUGHT) speedDown = Math.max(-5, speedDown - (GRAVITY + 24) * seconds);
        else if (has(GREEN) && jump && !down && !onGround && y > 1) speedDown = Math.max(mastered ? -10 : named(GALE) ? -8 : -4.5f, speedDown - (GRAVITY + 24) * seconds);
        // On the tide she does not drop out of the air: let go of, the jump lets her down slowly.
        else if (named(TIDE) && !onGround && !down) speedDown = Math.min(speedDown, 2.6f);
        // Water she cannot swim in lets her down slowly, and out through the bottom.
        if (about == RunLevels.WATER && !has(BLUE)) {
            speedDown = Math.min(speedDown, 3.5f);
            speedAcross *= .92f;
            wet = true;
        }

        // A belt carries her along with it.
        moveAcross((speedAcross + (under == RunLevels.BELT_RIGHT ? BELT : under == RunLevels.BELT_LEFT ? -BELT : 0)) * seconds);
        moveDown(speedDown * seconds);
        // A few steps into the place where someone waits, the way back shuts behind her, and they come.
        if (!locked && opening <= 0 && level.arena >= 0 && x + WIDE / 2 >= level.arena + 2.5f && x + WIDE / 2 < level.gate && standing()) lock();

        for (int index = pluses.size() - 1; index >= 0; index--) {
            int[] plus = pluses.get(index);
            float far = Math.max(perks.reach, magnet > 0 ? MAGNET_REACH : 0);
            boolean drawn = far > 0 && Math.hypot(plus[0] + .5f - x - WIDE / 2, plus[1] + .5f - y - TALL / 2) < far;
            if (drawn || x < plus[0] + .8f && x + WIDE > plus[0] + .2f && y < plus[1] + .8f && y + TALL > plus[1] + .2f) {
                pluses.remove(index);
                collected++;
                plusX = plus[0] + .5f;
                plusY = plus[1] + .5f;
            }
        }
        // A thing to fight with is picked up by touching it, in place of whatever she carried.
        for (int index = arms.size() - 1; index >= 0; index--) {
            int[] arm = arms.get(index);
            if (x < arm[1] + .85f && x + WIDE > arm[1] + .15f && y < arm[2] + .9f && y + TALL > arm[2] + .1f) {
                arms.remove(index);
                weapon = arm[0];
                rounds = weapon == GUN ? perks.shots : 0;
                picked++;
            }
        }
        atStall = false;
        for (int index = items.size() - 1; index >= 0; index--) {
            int[] item = items.get(index);
            // A stall stays where it is, and she is at it anywhere along its counter.
            if (item[0] == STALL) {
                if (x < item[1] + 2.2f && x + WIDE > item[1] - 1.2f && y < item[2] + .9f && y + TALL > item[2] + .1f) {
                    atStall = true;
                    stallAt = item[1];
                    if (greeted.add(item[1])) greetings++;
                }
                continue;
            }
            // Whoever a letter is for stays too: she has it when it is brought to her.
            if (item[0] == DROPOFF) {
                if (quest == ASKED && level.quest == RunLevels.LETTER && x < item[1] + 2.4f && x + WIDE > item[1] - .4f && y < item[2] + .9f
                        && y + TALL > item[2] + .1f) done();
                continue;
            }
            if (x < item[1] + .85f && x + WIDE > item[1] + .15f && y < item[2] + .9f && y + TALL > item[2] + .1f) {
                if (cursed > 0 && item[0] > FORM && item[0] < CHEST) continue;
                items.remove(index);
                // A chest is opened: ten pluses in it, whatever else; and a stash is done with when its chest is.
                if (item[0] == CHEST) {
                    chests++;
                    bonus += 10;
                    if (room >= 0 && level.rooms.get(room)[0] == RunLevels.STASH && (roomsDone & 1 << room) == 0) {
                        roomsDone |= 1 << room;
                        roomWins++;
                    }
                    continue;
                }
                // A rice cake is only found: found before it was asked for, it still counts.
                if (item[0] == CAKE) {
                    cakes++;
                    if (quest == ASKED && cakes >= 3) done();
                    continue;
                }
                taken++;
                lastTaken = item[0];
                giver = item[3];
                take(item[0]);
                // Whoever has something to ask of her asks it as she hands over what she holds.
                if (quest == 0 && level.quest != 0 && item[3] == RunLevels.giverOf(level.quest)) {
                    questColumn = item[1];
                    questRow = item[2];
                    ask();
                }
            }
        }
        // A press of the button uses it, once it has rested; the ink gun goes on for as long as the button is held.
        sinceStrike += seconds;
        if (swing > 0) swing -= seconds;
        // In black nothing she carries is any use to her. In red and in blue the button throws fire and water, and whatever she
        // carries is used as well: the one is never lost to the other.
        sinceCast += seconds;
        sinceBrace += seconds;
        if (mastered) shine(seconds, strike);
        {
            boolean used = weapon != UNARMED && strike && (!strikeHeld || weapon == GUN) && sinceStrike >= REST[weapon];
            if (used) use();
            if ((has(RED) || has(BLUE)) && strike && (!strikeHeld || used) && sinceCast >= .32f) cast();
        }
        strikeHeld = strike;
        // What she is standing in: a spring throws her up, and spikes cost a heart.
        int feetRow = (int) Math.floor(y + TALL - .05f);
        for (int column = (int) Math.floor(x + .08f); column <= (int) Math.floor(x + WIDE - .08f); column++) {
            char here = at(column, feetRow);
            if (here == RunLevels.SPRING && speedDown >= 0 && y + TALL >= feetRow + .45f) {
                speedDown = -SPRING;
                onGround = false;
                sprung = true;
                airJumps = 0;
                springs++;
            } else if (here == RunLevels.SPIKE && y + TALL >= feetRow + .5f && safe <= 0) {
                cause = SPIKED;
                if (hurt(false)) return;
            } else if (here == RunLevels.LAVA && !has(RED) && y + TALL >= feetRow + .3f) {
                // Lava sends her back to the flag as a fall does.
                cause = BURNED;
                hurt(true);
                return;
            }
        }
        // A bookmark door lets her out at the door it leads to.
        if (doorWait <= 0) {
            for (int[] door : level.doors) {
                int floor = door.length > 2 ? door[2] : RunLevels.FLOOR;
                if (x + WIDE / 2 < door[0] || x + WIDE / 2 >= door[0] + 1 || Math.abs(y + TALL - floor) > 1.2f) continue;
                x = door[1] + (1 - WIDE) / 2;
                y = floor - TALL;
                speedDown = 0;
                doorWait = 1;
                doorsUsed++;
                break;
            }
        }
        for (Foe foe : foes) move(foe, seconds);
        if (!born.isEmpty()) {
            foes.addAll(born);
            born.clear();
        }
        if (swing > 0) swung();
        if (fly(seconds)) return;
        for (Foe foe : foes) {
            // Whoever is arriving, changing or out of sight is not there to be touched.
            if (foe.flat || absent(foe) || foe.kind == RunLevels.TARGET || x >= foe.x + foe.wide() || x + WIDE <= foe.x || y >= foe.y + foe.tall()
                    || y + TALL <= foe.y) continue;
            // Coming down onto a sign deals with it and sends her up again; any other touch knocks her back.
            // A times has a spike on every arm and cannot be landed on at all.
            boolean onto = speedDown > 0 && y + TALL - speedDown * seconds <= foe.y + foe.tall() * .6f;
            // The comet: at a run in red and yellow, whatever she meets she goes through as she would in brown.
            boolean comet = named(COMET) && Math.abs(speedAcross) > RUN * 1.4f && !RunLevels.boss(foe.kind);
            if (comet) {
                foe.left = 1;
                beat(foe, SURE);
            } else if (notes > 0 || has(BROWN)) {
                // With the notes about her, or in brown, whatever she touches is dealt with; one of those who wait, unless spent, only stops against her.
                if (RunLevels.boss(foe.kind) && !open(foe) && has(BROWN)) {
                    braced(foe.x + foe.wide() / 2);
                } else if (has(BROWN) && !RunLevels.boss(foe.kind)) {
                    // Any sign, from any side, spikes and all, and however much it can take.
                    foe.left = 1;
                    beat(foe, SURE);
                    // The ember: in red as well, it goes off burning, the way she faces, into whatever is beyond it.
                    if (named(EMBER)) shoot(FIRE, true, foe.x + foe.wide() / 2, foe.y + foe.tall() * .5f, (facingLeft ? -1 : 1) * 12, 0, .8f);
                } else if (foe.dazed <= 0) {
                    beat(foe, SURE);
                }
            } else if (onto && foe.kind != RunLevels.TIMES && foe.kind != RunLevels.WISP && foe.dazed <= 0) {
                speedDown = -BOUNCE;
                airJumps = 0;
                // Whatever she has come down on, she is clear of it for a moment as she goes up again.
                offIt = .3f;
                if (plunged && !RunLevels.boss(foe.kind) && foe.left > 1) foe.left--;
                beat(foe);
                // One sign after another without touching the ground: each is worth one plus more than the last.
                if (foe.flat && ++combo > 1) bonus += combo - 1;
            } else if (safe <= 0 && offIt <= 0 && foe.dazed <= 0) {
                cause = foe.kind == RunLevels.YEOBAEK ? SLASHED : TOUCHED;
                knock = x + WIDE / 2 < foe.x + foe.wide() / 2 ? -1 : 1;
                if (hurt(false)) return;
            }
        }
        if (y > (room >= 0 ? level.land.length : RunLevels.ROWS) + 1) {
            cause = wet ? SANK : FELL;
            hurt(true);
            return;
        }
        // A flag she has passed, at any height, is where she comes back to; passing the gate is the end.
        float middle = x + WIDE / 2;
        asked(0);
        // In a place off the land she is neither further on in it nor at its end.
        if (room >= 0) return;
        for (int flag : level.flags) {
            if (middle >= flag && startX < flag) {
                startX = flag + (1 - WIDE) / 2;
                startY = Math.max(0, RunLevels.top(level, flag)) - TALL;
                flagsPassed++;
                if (perks.flagHeart) heal();
            }
        }
        if (middle >= level.gate && middle < level.main && gateOpen()) state = WON;
    }

    /**
     * She calls on a colour that is hers to keep, and wears it with whatever else she
     * wears: each keeps its power, and each has its own time, shorter than a ribbon gives.
     * She cannot call on one she is wearing, nor one that is waiting since it last went,
     * nor more of her own at once than she can hold. Black, which is the Mawang's, makes
     * her a shadow that nothing can touch; it is short, it ends in a burst, and it waits
     * twice as long. Returns whether she could.
     */
    boolean call(int colour) {
        if (!callable(colour)) return false;
        put(colour, (colour == BLACK ? 5 : 9) * perks.lasting * perks.formLonger, false);
        called++;
        return true;
    }

    /** Whether a colour could be called on now. */
    boolean callable(int colour) {
        return state == PLAYING && !mastered && cursed <= 0 && colour >= RED && colour < BLACK && (perks.colours & 1 << colour - 1) != 0 && worn[colour] <= 0
                && waits[colour] <= 0 && Integer.bitCount(mix() & ~lent) < perks.together;
    }

    /** She calls on several at once, those given as bits, in their order, for as far as she can hold them. Returns those she could, as bits. */
    int callAll(int colours) {
        int could = 0;
        for (int colour = RED; colour <= BLACK; colour++) if ((colours & 1 << colour - 1) != 0 && call(colour)) could |= 1 << colour - 1;
        return could;
    }

    /** She lets go of a colour she is wearing: it is gone at once, quietly, and its wait begins. */
    boolean drop(int colour) {
        if (colour < RED || colour > BLACK || worn[colour] <= 0) return false;
        end(colour, false);
        return true;
    }

    /** How many of her own colours she may wear at once. */
    int together() { return perks.together; }

    /** The colours that are hers and could be called on now, were there room for them: neither on nor waiting. */
    int ready() {
        int ready = 0;
        for (int colour = RED; colour < BLACK; colour++) {
            if ((perks.colours & 1 << colour - 1) != 0 && worn[colour] <= 0 && waits[colour] <= 0 && cursed <= 0) ready |= 1 << colour - 1;
        }
        return ready;
    }

    /** The colours a named mix is made of, as bits: none for the prism, which is any three. */
    static int mixOf(int which) {
        return which == STEAMS ? 1 | 2 : which == COMET ? 1 | 16 : which == TIDE ? 2 | 8 : which == MIRROR ? 4 | 32 : which == GALE ? 8 | 16 : which == EMBER ? 1 | 4
                : which == WHITEN ? ALL : 0;
    }

    /** The six colours that are worn, as bits: black is not one of them. */
    static final int ALL = (1 << PURPLE) - 1;
    /** How long a ribbon lasts; and black put on her, which is twice that. */
    static final float RIBBON = 14, BLACKENED = 2 * RIBBON;

    /** The name some colours would have, worn together: white for all six, the prism for any three or more, one of the pairs, or -1 for none. */
    static int naming(int colours) {
        if ((colours & ALL) == ALL) return WHITEN;
        if (Integer.bitCount(colours) >= 3) return PRISM;
        for (int which = STEAMS; which <= EMBER; which++) if (colours == mixOf(which)) return which;
        return -1;
    }

    /** As many of the colours owned as may be worn at once: those that are ready before those that are not, each in its order. */
    static int most(int owned, int ready, int limit) {
        int chosen = 0;
        for (int turn = 0; turn < 2; turn++) {
            for (int colour = RED; colour < BLACK; colour++) {
                int bit = 1 << colour - 1;
                if ((owned & bit) == 0 || (chosen & bit) != 0 || Integer.bitCount(chosen) >= limit) continue;
                if (turn == 1 || (ready & bit) != 0) chosen |= bit;
            }
        }
        return chosen;
    }

    /**
     * She wears a mix: of the colours given, those that are on and not among them are let
     * go of, and those among them that are ready are called on, for as far as she can
     * hold them; those still waiting are passed over. Returns those called on, as bits.
     */
    int wear(int colours) {
        for (int colour = RED; colour <= BLACK; colour++) {
            int bit = 1 << colour - 1;
            if (worn[colour] > 0 && (colours & bit) == 0 && (lent & bit) == 0) drop(colour);
        }
        return callAll(colours);
    }

    /** A colour is given her there and then, to keep: it is in her hair at once, and it is hers to call on from now. */
    void gift(int colour) {
        // Black is not worn: what the Mawang gives is that it lies on her half as long.
        if (colour == BLACK) {
            perks.resists = true;
            return;
        }
        perks.colours |= 1 << colour - 1;
        wear(colour, 9 * perks.lasting * perks.formLonger);
    }

    /** Whether she wears a colour: black that was put on her, which is no use to her, does not count. */
    boolean coloured() { return pale > 0 || mix() != 0; }

    /** A colour is put on, with whatever she wears already. */
    private void put(int colour, float lasts, boolean ribbon) {
        worn[colour] = lasts;
        lasted[colour] = lasts;
        waits[colour] = 0;
        last = colour;
        if (ribbon) lent |= 1 << colour - 1;
        else lent &= ~(1 << colour - 1);
        formed++;
        mingle();
    }

    /** A colour is gone, however that came about, and only now does its wait begin: twice as long for black, which goes in a burst when it has run its time. */
    private void end(int colour, boolean ran) {
        if (worn[colour] <= 0 && !ran) return;
        worn[colour] = 0;
        waits[colour] = perks.recharge * (colour == BLACK ? 2 : 1);
        lent &= ~(1 << colour - 1);
        faded++;
        if (colour == BLACK && ran) burst();
        mingle();
    }

    /** Everything she wears is gone, quietly. */
    private void fade(boolean ran) {
        for (int colour = RED; colour <= BLACK; colour++) if (worn[colour] > 0) end(colour, false);
        pale = 0;
    }

    /** What she wears is looked at again for the mixes that have names: one that has just formed is the last to have formed. */
    private void mingle() {
        int mix = mix(), now = 0;
        boolean red = (mix & 1) != 0, blue = (mix & 2) != 0, brown = (mix & 4) != 0, green = (mix & 8) != 0, yellow = (mix & 16) != 0, purple = (mix & 32) != 0;
        if (red && blue) now |= 1 << STEAMS;
        if (red && yellow) now |= 1 << COMET;
        if (blue && green) now |= 1 << TIDE;
        if (brown && purple) now |= 1 << MIRROR;
        if (green && yellow) now |= 1 << GALE;
        if (red && brown) now |= 1 << EMBER;
        if (Integer.bitCount(mix) >= 3) now |= 1 << PRISM;
        if ((mix & ALL) == ALL) {
            now |= 1 << WHITEN;
            // White, of every colour at once, however she came by them: a white she has not mastered.
            if ((mixed & 1 << WHITEN) == 0) whitened++;
        }
        for (int which = MIXES - 1; which >= 0; which--) {
            if ((now & 1 << which) != 0 && (mixed & 1 << which) == 0) {
                lastMix = which;
                mixings++;
                break;
            }
        }
        mixed = now;
        mixes |= now;
    }

    /**
     * Her hair turns to a colour by a ribbon, or by being given it or lent it: it is worn
     * with whatever she wears already, and does not count against how many of her own she
     * may wear. The rare white ribbon is the six that have powers, all at once, and she is
     * white for as long as it lasts. A black one is black put on her, unless black is hers:
     * then it is her own shadow.
     */
    private void wear(int colour, float lasts) {
        if (colour == WHITE) {
            for (int each = RED; each <= PURPLE; each++) put(each, lasts, true);
            pale = lasts;
        } else if (colour == BLACK) {
            curse(blackened());
        } else {
            put(colour, lasts, true);
        }
    }

    /** How long black lies on her: twice as long as a ribbon lasts; half that for one the Mawang has given her gift. */
    float blackened() { return perks.resists ? RIBBON : BLACKENED; }

    /**
     * Black is put on her, by a black ribbon or by a curse that is thrown: every colour she
     * wore is gone, and for as long as it lies on her she can call on none and pick up none.
     * It does not slow her, and what she carries still strikes.
     */
    private void curse(float lasts) {
        // On the white that is truly hers black does not lie: it is one of her colours.
        if (mastered) return;
        fade(false);
        cursed = lasts;
        cursedFor = lasts;
        curses++;
        formed++;
    }

    /** How long the black that is on her was to lie on her in all; and how many times black has been put on her, a count that only goes up. */
    float cursedFor() { return cursedFor; }
    int curses() { return curses; }
    private float cursedFor;
    private int curses;

    /** How long it will be before she can call on a colour again, in seconds, once it has gone; and the same as a share of the whole wait that is over. */
    float untilCall(int colour) { return colour >= RED && colour <= BLACK ? waits[colour] : 0; }
    float charge(int colour) {
        return colour >= RED && colour <= BLACK ? 1 - waits[colour] / (perks.recharge * (colour == BLACK ? 2 : 1)) : 1;
    }

    /** A ribbon's worth of a colour that is not hers: her hair turns to it for as long as a ribbon lasts. */
    void lend(int colour) { take(FORM + colour); }

    /** The shadow passes: every sign near her, but those who wait, goes over, and whatever was in the air near her is gone. */
    private void burst() {
        bursts++;
        float middle = x + WIDE / 2;
        for (Foe foe : foes) {
            if (foe.flat || RunLevels.boss(foe.kind) || foe.kind == RunLevels.TARGET || Math.abs(foe.x + foe.wide() / 2 - middle) > 4.5f
                    || Math.abs(foe.y - y) > 4) continue;
            foe.flat = true;
            foe.y = foe.ground;
            foe.act = 0;
            flattened++;
        }
        for (Shot shot : shots) if (!shot.hers && Math.abs(shot.x - middle) < 6) shot.left = 0;
    }

    /** Whether she is a shadow; how many times one has passed in a burst; and how many times the Old Book has been woken. */
    boolean shadow() { return worn[BLACK] > 0; }
    int bursts() { return bursts; }
    int woken() { return woken; }

    /** How many times she has called on a colour. */
    int called() { return called; }

    /** Something is asked of her: if it is done already it is done; a count begins if it is against one. */
    private void ask() {
        quest = ASKED;
        questGiven++;
        questFlags = flagsPassed;
        if (level.quest == RunLevels.TIME) {
            // A second for every four squares to the next flag, and five over.
            float far = level.gate;
            for (int flag : level.flags) if (flag > x && flag < far) far = flag;
            questLeft = (far - x) / 4 + 5;
        }
        if (level.quest == RunLevels.CAKES && cakes >= 3) done();
        asked(0);
    }

    /** How what is asked stands is looked at again: a cellar cleared, the room behind the crack found, the flag reached or the count run out. */
    private void asked(float seconds) {
        // Back with whoever asked it: done, it is handed in; not yet done, she is reminded of it, each time she comes back from somewhere else.
        if (quest == DONE && byGiver() && state == PLAYING) pay();
        if (quest == ASKED) {
            boolean by = byGiver();
            if (!by && questColumn >= 0 && Math.abs(x - questColumn) > 6) questAway = true;
            if (by && questAway) {
                questAway = false;
                reminders++;
            }
        }
        if (quest != ASKED) return;
        if (level.quest == RunLevels.CELLAR || level.quest == RunLevels.ROOM) {
            for (int index = 0; index < level.rooms.size(); index++) {
                int[] one = level.rooms.get(index);
                boolean cellar = level.quest == RunLevels.CELLAR;
                if (cellar ? one[0] == RunLevels.ARENA && (roomsDone & 1 << index) != 0 : one[7] != 0 && (roomsFound & 1 << index) != 0) {
                    done();
                    return;
                }
            }
        } else if (level.quest == RunLevels.TIME) {
            if (flagsPassed > questFlags) done();
            else if ((questLeft -= seconds) <= 0) fail();
        }
    }

    /**
     * It is done, and is now to be handed in: to whoever asked it, by going back to her;
     * or, if she does not go back, when the land is over. A letter is handed to whoever it
     * was for, there and then; and whoever counted a race has run after her, and is there.
     */
    private void done() {
        quest = DONE;
        questsDone++;
        questLeft = 0;
        if (level.quest == RunLevels.LETTER || level.quest == RunLevels.TIME) pay();
    }

    /** It is handed in: fifteen pluses, and a heart if she is short of one. */
    private void pay() {
        quest = PAID;
        questsPaid++;
        bonus += 15;
        heal();
    }

    /** The land is over, or she is leaving it: what was done and not handed in is handed in now, so that nothing earned is lost. Returns whether there was anything. */
    boolean settle() {
        if (quest != DONE) return false;
        pay();
        return true;
    }

    /** Whether she is by whoever asked something of her. */
    private boolean byGiver() {
        return questColumn >= 0 && room < 0 && x < questColumn + 2.6f && x + WIDE > questColumn - .6f && Math.abs(y + TALL - questRow - 1) < 1.6f;
    }

    /** Where whoever asked something of her stands, the column and the row; how many times something has been handed in; and how many times she has come back to be reminded of it. */
    int questColumn() { return questColumn; }
    int questRow() { return questRow; }
    int questsPaid() { return questsPaid; }
    int reminders() { return reminders; }

    /** A heart back, if she is short of one: a whole one, or as much of one as she is short of. */
    private boolean heal() {
        if (halves >= perks.hearts * 2) return false;
        halves = Math.min(perks.hearts * 2, halves + 2);
        return true;
    }

    private void fail() {
        quest = FAILED;
        questFails++;
        questLeft = 0;
    }

    /** How what is asked of her stands; how many rice cakes she has found; how much longer a count has to run; and counts that only go up of things asked, done and failed. */
    int quest() { return quest; }
    int cakes() { return cakes; }
    float questLeft() { return questLeft; }
    int questGiven() { return questGiven; }
    int questsDone() { return questsDone; }
    int questFails() { return questFails; }

    /** What comes of taking something that helps, picked up or bought. */
    private void take(int kind) {
        if (kind == BREAD) {
            // Bread she has no need of is worth three pluses.
            if (!heal()) bonus += 3;
        } else if (kind == SHIELD) {
            shield = true;
            inkLeft = perks.inkHurts;
        } else if (kind == HOMERUN || kind == COMPLIMENT) {
            // Normal No. 3 sends every sign near her over the fence; a compliment leaves them standing, charmed.
            for (Foe foe : foes) {
                if (foe.flat || RunLevels.boss(foe.kind) || Math.abs(foe.x - x) > 9) continue;
                if (kind == HOMERUN) beat(foe, SURE);
                else foe.dazed = 6;
            }
            cleared++;
        } else if (kind > FORM) {
            int colour = kind - FORM;
            wear(colour, (colour == WHITE ? 12 : RIBBON) * perks.lasting * perks.formLonger);
        } else if (kind == ICE) {
            rush = LASTS[ICE] * perks.lasting;
        } else if (kind == MAGNET) {
            magnet = LASTS[MAGNET] * perks.lasting;
        } else if (kind == NOTES) {
            notes = LASTS[NOTES] * perks.lasting;
        }
    }

    /**
     * A sign is landed on, struck or hit. Most go flat at once. Those who wait at the end
     * turn it aside unless they are spent; if not, they are dazed for a moment, and when a
     * third of what they can take is gone they change, which takes a moment in which nothing
     * touches them and whatever they had in the air is gone.
     */
    private void beat(Foe foe, int by) {
        with = by;
        beat(foe);
        with = PLAIN;
    }

    /** What the blow being dealt is made with: nothing in particular; something nothing stands against; or the kind of thing thrown. */
    private static final int PLAIN = -1, SURE = -2;
    private int with = PLAIN;

    private void beat(Foe foe) {
        if (!open(foe)) {
            parried++;
            return;
        }
        // A candle wisp is put out only by water, and a rime flake melted only by fire; steam does for both. Anything else goes through them.
        if ((foe.kind == RunLevels.WISP || foe.kind == RunLevels.RIME) && with != SURE && with != STEAM && with != (foe.kind == RunLevels.WISP ? WATER : FIRE)) {
            fizzled++;
            fizzledAt = foe.kind;
            foeX = foe.x + foe.wide() / 2;
            foeY = foe.y + foe.tall();
            return;
        }
        boolean waits = RunLevels.boss(foe.kind);
        int phase = waits ? phase(foe) : 0;
        foeX = foe.x + foe.wide() / 2;
        foeY = foe.y + foe.tall();
        if (waits) blows++;
        if (--foe.left <= 0) {
            foe.flat = true;
            foe.y = foe.ground;
            foe.act = 0;
            flattened++;
            if (RunLevels.flier(foe.kind)) fliers++;
            // A paper target is only hit: two pluses for it. And where signs keep coming, each one dealt with counts, and is a plus.
            if (foe.kind == RunLevels.TARGET) {
                caught++;
                bonus += 2;
            } else if (foe.spawned && room >= 0 && level.rooms.get(room)[0] == RunLevels.TRAMPLE && gameLeft > 0) {
                caught++;
                bonus++;
            }
            if (foe.kind == RunLevels.BLOT) split(foe);
            if (RunLevels.rover(foe.kind)) {
                for (Shot shot : shots) if (!shot.hers) shot.left = 0;
                return;
            }
            if (waits && !foe.spawned && !standing()) won();
            else if (waits && !foe.spawned && phase(foe) > phase) changed();
            return;
        }
        if (!waits) {
            // A big one flinches, and can be hit again at once.
            foe.dazed = .15f;
            return;
        }
        foe.dazed = STUNNED;
        // And then begins the next thing it does.
        foe.step = 3;
        foe.act = 0;
        foe.unseen = false;
        // A chunk of King Slime is gone: he is smaller, and stands where he stood.
        if (foe.kind == RunLevels.KING) resize(foe, foe.size * .93f);
        // And a slime's worth of Blot is gone: it is smaller, and the slime hops off.
        if (foe.kind == RunLevels.BLOT) {
            resize(foe, Math.max(1.1f, foe.size - .16f));
            split(foe);
        }
        if (phase(foe) > phase) {
            if (foe.spawned || RunLevels.rover(foe.kind)) {
                // The Old Book changes by himself, and so does one met in the middle of a land.
                phases++;
                foe.flourish = FLOURISH;
            } else {
                changed();
            }
        }
    }

    /** Blot comes apart: a slime hops off it, to one side or the other by turns. */
    private void split(Foe foe) {
        splits++;
        float side = splits % 2 == 0 ? -1.4f : 1.4f;
        spawn(RunLevels.SLIME, foe.x + foe.wide() / 2 + side * foe.size, foe.ground + foe.tall());
    }

    /** A third of what they can take is gone: they change, which takes a moment, and whatever they had in the air is gone. */
    private void changed() {
        phases++;
        // And as they change they shut off one of the colours she wears, the one with the longest still to go: it is gone, and waits as if it had run its time.
        int longest = 0;
        for (int colour = RED; colour < BLACK; colour++) if (worn[colour] > 0 && (longest == 0 || worn[colour] > worn[longest])) longest = colour;
        if (longest != 0 && pale <= 0) {
            end(longest, false);
            lastShut = longest;
            shuts++;
        }
        for (Foe other : foes) if (RunLevels.boss(other.kind) && !other.flat) other.flourish = FLOURISH;
        for (Shot shot : shots) if (!shot.hers) shot.left = 0;
    }

    /** Makes one of them some other size, standing where it stood. */
    private void resize(Foe foe, float size) {
        float floor = foe.ground + foe.tall(), middle = foe.x + foe.wide() / 2;
        foe.size = size;
        foe.ground = floor - foe.tall();
        foe.x = middle - foe.wide() / 2;
        foe.y = foe.ground;
    }

    /** The way back shuts, and whoever waits arrives: the first time they take their time over it. */
    private void lock() {
        locked = true;
        entrances++;
        for (Foe foe : foes) {
            if (!RunLevels.boss(foe.kind) || foe.flat) continue;
            foe.away = false;
            foe.enter = entrances == 1 ? ENTER : ENTER / 2;
            foe.x = foe.home;
            foe.y = foe.ground;
            foe.mode = 0;
            foe.step = 3;
            foe.act = .3f;
            foe.turn = 0;
            foe.dazed = 0;
            foe.flourish = 0;
            foe.unseen = false;
            foe.leftward = true;
        }
    }

    /**
     * She has lost a life and been sent back to the flag at the door: the place is as it was
     * before she came in. Whoever waits is gone again, and has back all they had lost, since
     * she left; of the twins, one who is down stays down. What they made is gone; and what
     * will do against them lies at the door again.
     */
    private void leave() {
        leaving = false;
        locked = false;
        for (int index = foes.size() - 1; index >= 0; index--) {
            Foe foe = foes.get(index);
            if (foe.spawned) {
                foes.remove(index);
                continue;
            }
            if (!RunLevels.boss(foe.kind) || foe.flat) continue;
            foe.left = foe.most;
            if (foe.kind == RunLevels.KING) resize(foe, 1);
            foe.away = true;
            foe.enter = 0;
            foe.flourish = 0;
            foe.dazed = 0;
            foe.unseen = false;
            foe.x = foe.home;
            foe.y = foe.ground;
        }
        restock(false);
    }

    /** The last of them is down: what they made goes with them, and in a moment the ways open. */
    private void won() {
        opening = level.arena >= 0 ? 1.4f : 0;
        for (Foe foe : foes) {
            if (foe.spawned && !foe.flat) {
                foe.flat = true;
                foe.act = 0;
            }
        }
        for (Shot shot : shots) if (!shot.hers) shot.left = 0;
    }

    /** What lies at the door to be used against whoever waits is put back if it has been taken: at the door, or in the middle of the place, up on its ledge. */
    private void restock(boolean inside) {
        for (int[] spec : level.weapons) {
            if (level.arena < 0 || spec[1] < level.arena - 3 || spec[1] > level.arena) continue;
            int column = inside ? level.arena + 10 : spec[1], row = inside ? spec[2] - 4 : spec[2];
            boolean there = false;
            for (int[] arm : arms) if (arm[1] == column && arm[2] == row) there = true;
            if (!there) arms.add(new int[]{spec[0], column, row});
        }
    }

    /** Something made by one of those who wait: there are never more than three of them. */
    private void spawn(int kind, float middle, float floor) {
        int living = born.size();
        for (Foe foe : foes) if (foe.spawned && !foe.flat) living++;
        if (living >= 3) return;
        Foe foe = new Foe(kind);
        foe.spawned = true;
        foe.left = 1;
        float lo = level.arena >= 0 ? level.arena + 1.3f : 0, hi = (level.arena >= 0 ? level.gate - .3f : columns) - foe.wide();
        foe.x = Math.max(lo, Math.min(hi, middle - foe.wide() / 2));
        foe.ground = floor - foe.tall();
        foe.y = foe.ground;
        foe.home = foe.x;
        // A moment in which it can neither hurt nor be hurt.
        foe.dazed = .6f;
        born.add(foe);
    }

    /** She uses what she carries: throws it, fires it, or begins a swing of it. */
    private void use() {
        sinceStrike = 0;
        strikes++;
        float way = facingLeft ? -1 : 1, fromX = x + WIDE / 2 + way * .5f, fromY = y + TALL * .45f;
        if (weapon == PEN) {
            shoot(PEN, true, fromX, fromY, way * 13, 0, 1);
        } else if (weapon == KNIFE) {
            shoot(KNIFE, true, fromX, fromY, way * 19, 0, .5f);
        } else if (weapon == BOOK) {
            shoot(BOOK, true, fromX, fromY - .3f, way * 7.5f + speedAcross * .4f, -11, 3);
        } else if (weapon == GUN) {
            shoot(GUN, true, fromX, fromY, way * 22, 0, .9f);
            if (--rounds <= 0) weapon = UNARMED;
        } else {
            swing = SWING;
            swungWith = weapon;
        }
    }

    /** In red she throws fire, which goes on through whatever it meets, and in blue water, which falls as it flies; in white, both. */
    private void cast() {
        sinceCast = 0;
        casts++;
        float way = facingLeft ? -1 : 1, fromX = x + WIDE / 2 + way * .5f, fromY = y + TALL * .45f;
        if (named(STEAMS)) {
            // Red and blue together are steam: a cloud, slower and longer in the air, that goes on through whatever it meets.
            shoot(STEAM, true, fromX, fromY, way * 8, 0, 1.5f);
            return;
        }
        if (has(RED)) shoot(FIRE, true, fromX, fromY, way * 13, 0, .9f);
        if (has(BLUE)) {
            shoot(WATER, true, fromX, fromY - .1f, way * 10, -1.5f, 1.1f);
            // On the tide the water goes both ways.
            if (named(TIDE)) shoot(WATER, true, fromX - way, fromY - .1f, -way * 10, -1.5f, 1.1f);
        }
    }

    /** Something is sent at her from a place: at where she is, or at where she will be if she keeps on as she is going. */
    private void aim(int kind, float fromX, float fromY, float speed, float ahead, float lasting) {
        float across = x + WIDE / 2 + speedAcross * ahead - fromX, down = y + TALL / 2 - fromY;
        float length = Math.max(.5f, (float) Math.hypot(across, down));
        shoot(kind, false, fromX, fromY, across / length * speed, down / length * speed, lasting);
        thrown++;
    }

    private Shot shoot(int kind, boolean hers, float fromX, float fromY, float across, float down, float lasting) {
        Shot shot = new Shot(kind);
        shot.hers = hers;
        shot.x = fromX;
        shot.y = fromY;
        shot.across = across;
        shot.down = down;
        shot.left = lasting;
        shots.add(shot);
        return shot;
    }

    /** Several of something, sent at her in a fan. */
    private void spread(int kind, float fromX, float fromY, int count, float gap, float speed, float lasting) {
        double way = Math.atan2(y + TALL / 2 - fromY, x + WIDE / 2 - fromX);
        for (int which = 0; which < count; which++) {
            double turn = way + (which - (count - 1) / 2f) * gap;
            shoot(kind, false, fromX, fromY, (float) Math.cos(turn) * speed, (float) Math.sin(turn) * speed, lasting);
        }
        thrown++;
    }

    /** Something lobbed from a place to come down at another. */
    private void lob(int kind, float fromX, float fromY, float to) {
        shoot(kind, false, fromX, fromY, Math.max(-9, Math.min(9, (to - fromX) / .78f)), -10, 4);
    }

    /** A swing of a bat or a sword: whoever stands in front of it, and no higher than the one who swings, is hit. */
    private void cut(Foe foe, float reach, int with) {
        float from = foe.leftward ? foe.x - reach : foe.x + foe.wide();
        if (safe <= 0 && x < from + reach && x + WIDE > from && y + TALL > foe.y - .2f && y < foe.y + foe.tall()) {
            if (braced(foe.x + foe.wide() / 2)) return;
            cause = with;
            knock = foe.leftward ? -1 : 1;
            hurt(false);
        }
    }

    /** Someone heavy comes down, and the ground jumps: whoever is standing on it near them is thrown. */
    private void quake(float away, float floor) {
        quakes++;
        if (safe <= 0 && onGround && y + TALL > floor - .2f && away < 2.9f) {
            if (braced(x + WIDE / 2)) return;
            cause = QUAKED;
            hurt(false);
        }
    }

    /**
     * What a swing reaches: the signs in front of her, and whatever has been thrown at her,
     * as far as the blade or the bristles are drawn to reach and no further. The brush goes
     * over her head and a little behind, and wipes away what is thrown; the sword is the
     * quicker, and sends it back the way it came.
     */
    private void swung() {
        boolean brush = swungWith == BRUSH;
        float ahead = brush ? 1.05f : .9f, behind = brush ? .3f : 0, top = y - (brush ? .9f : .15f), bottom = y + TALL;
        float from = facingLeft ? x - ahead : x - behind, to = facingLeft ? x + WIDE + behind : x + WIDE + ahead;
        for (Foe foe : foes) {
            if (foe.flat || absent(foe) || foe.dazed > 0 || foe.x >= to || foe.x + foe.wide() <= from || foe.y >= bottom || foe.y + foe.tall() <= top) continue;
            beat(foe);
        }
        for (int index = 0; index < level.rooms.size() && room < 0; index++) {
            int[] one = level.rooms.get(index);
            if (one[7] == 1 && (revealed & 1 << index) == 0 && one[1] + 1 > from && one[1] < to && one[2] > top && one[2] - 1 < bottom) {
                revealed |= 1 << index;
                reveals++;
            }
        }
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            if (shot.hers || shot.x < from - .2f || shot.x > to + .2f || shot.y < top - .2f || shot.y > bottom + .2f) continue;
            turned++;
            if (brush) {
                shots.remove(index);
            } else {
                shot.hers = true;
                shot.across = (facingLeft ? -1 : 1) * Math.max(9, Math.abs(shot.across) * 1.6f);
                shot.down = Math.min(shot.down, 0);
                shot.left = 1;
            }
        }
    }

    /**
     * A book bursts: loose pages, which deal a blow to everything within a little way of
     * where it landed, whoever waits behind her shield excepted. It does nothing to her.
     */
    static final float BLAST = 2.3f;
    private int blasts;
    private float blastX, blastY;

    int blasts() { return blasts; }
    float blastX() { return blastX; }
    float blastY() { return blastY; }

    private void blast(float atX, float atY) {
        blasts++;
        blastX = atX;
        blastY = atY;
        for (int index = 0; index < foes.size(); index++) {
            Foe foe = foes.get(index);
            float across = foe.x + foe.wide() / 2 - atX, down = foe.y + foe.tall() / 2 - atY;
            if (foe.flat || absent(foe) || foe.dazed > 0 || across * across + down * down > BLAST * BLAST || RunLevels.boss(foe.kind) && !open(foe)) continue;
            beat(foe, BOOK);
        }
    }

    /** Moves on everything in the air, and sees what it has met. Returns whether she was hurt by it. */
    private boolean fly(float seconds) {
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            shot.x += shot.across * seconds;
            shot.y += shot.down * seconds;
            shot.age += seconds;
            if (shot.kind == BOOK) shot.down += BOOK_WEIGHT * seconds;
            else if (shot.kind == WATER) shot.down += 6 * seconds;
            else if (shot.kind == SAYING) shot.down = (float) Math.cos(shot.age * 7) * 3.2f;
            else if (shot.kind == GLOB) shot.down += BOOK_WEIGHT * seconds;
            else if (shot.kind == BOOMERANG) shot.across = shot.spin * (1 - shot.age);
            else if (shot.kind == DEBRIS) shot.down = shot.age < HANGS ? 0 : 13;
            // A ledge is thin: it stops only what comes down on it, which makes it somewhere to shelter.
            char in = at((int) Math.floor(shot.x), (int) Math.floor(shot.y));
            if ((shot.left -= seconds) <= 0 || shot.x < 0 || shot.x > columns || shot.y > (room >= 0 ? level.land.length : RunLevels.ROWS)
                    || (in == RunLevels.LEDGE ? shot.down > Math.abs(shot.across) : solid(in))) {
                // A book of hers bursts where it lands, on the ground, a ledge or a wall, or where it falls out of the air.
                if (shot.kind == BOOK && shot.hers) blast(shot.x, shot.y);
                shots.remove(index);
            } else if (shot.kind == PLUSDROP) {
                // A falling plus is hers if she is under it.
                if (shot.x > x - .3f && shot.x < x + WIDE + .3f && shot.y > y - .2f && shot.y < y + TALL) {
                    shots.remove(index);
                    caught++;
                    bonus++;
                }
            } else if (shot.hers) {
                for (Foe foe : foes) {
                    if (foe.flat || absent(foe) || shot.x < foe.x - .2f || shot.x > foe.x + foe.wide() + .2f || shot.y < foe.y - .2f
                            || shot.y > foe.y + foe.tall() + .2f) continue;
                    // Nothing thrown gets through Zero: anything times nothing is nothing.
                    if (foe.kind == RunLevels.ZERO) {
                        shots.remove(index);
                    } else if (foe.kind == RunLevels.TSUBAKIEH && !open(foe) && foe.dazed <= 0) {
                        // Tsubakieh bats it straight back, unless she is spent.
                        shot.hers = false;
                        shot.across = -shot.across;
                        shot.left = 1.2f;
                        turned++;
                    } else if (foe.kind == RunLevels.SLIME && (shot.kind == FIRE || shot.kind == STEAM) && foe.size < 1.2f) {
                        // A slime drinks fire, and steam: it is not beaten by it but grows on it, half as big again, and takes two blows after.
                        shots.remove(index);
                        resize(foe, 1.5f);
                        foe.left = 2;
                        drunk++;
                    } else if (foe.dazed <= 0) {
                        beat(foe, shot.kind);
                        // One sign after another dealt with by what she throws: each is worth one plus more than the last, up to four.
                        if (foe.flat && !RunLevels.boss(foe.kind)) {
                            if (foe.kind == RunLevels.TARGET) break;
                            streak = sinceShot < 1.6f ? streak + 1 : 1;
                            sinceShot = 0;
                            if (streak > 1) bonus += Math.min(streak - 1, 4);
                        }
                        // A book, fire and steam go on through, and so does what the mirror has sent back, but not through those who wait at the end.
                        if (shot.kind != BOOK && shot.kind != FIRE && shot.kind != STEAM && !shot.through || RunLevels.boss(foe.kind)) shots.remove(index);
                    }
                    break;
                }
            } else if (safe <= 0 && !(shot.kind == DEBRIS && shot.age < HANGS) && shot.x > x - .15f && shot.x < x + WIDE + .15f && shot.y > y + .05f
                    && shot.y < y + TALL) {
                if (has(PURPLE)) {
                    // In purple, what is thrown at her goes back where it came from; and from the mirror, it goes on through a row of them.
                    shot.hers = true;
                    shot.through = named(MIRROR);
                    shot.across = -shot.across;
                    shot.down = -shot.down;
                    shot.left = 1.2f;
                    // And it goes back faster than it came, so that it reaches the row behind whoever threw it.
                    if (shot.through && Math.abs(shot.across) < 12) shot.across = Math.signum(shot.across) * 12;
                    turned++;
                    continue;
                }
                shots.remove(index);
                if (shot.kind == HEART && charmed <= 0) {
                    charmed = CHARM;
                    charms++;
                } else if (shot.kind == CURSE) {
                    // Black that is thrown: in brown it lies on her half as long.
                    curse(blackened() * (has(BROWN) ? .5f : 1));
                } else {
                    // A dragon's full stop is the end of a flight: the wind goes out of her hair, and waits as if it had run its time.
                    if (shot.kind == STOP && worn[GREEN] > 0 && !onGround) {
                        end(GREEN, false);
                        grounded++;
                    }
                    // In brown what is thrown at her still lands, but it costs her half a heart.
                    cause = STRUCK;
                    knock = Math.signum(shot.across);
                    if (hurt(false, has(BROWN) ? 1 : 2)) return true;
                }
            }
        }
        return false;
    }

    /**
     * In brown, and in white, nothing that reaches her by touch or from arm's length hurts
     * her: a body, a swing, a slam. It is turned aside, where it met her. Returns whether it was.
     */
    private boolean braced(float from) {
        if (!has(BROWN)) return false;
        if (sinceBrace > .4f) {
            sinceBrace = 0;
            braces++;
            float middle = x + WIDE / 2;
            braceX = middle + Math.max(-.6f, Math.min(.6f, from - middle));
            braceY = y + TALL * .5f;
        }
        return true;
    }

    /** Counts that only go up: one met in the middle of a land roused; Blot come apart; a colour of hers shut off, and which; a slime grown on fire; and she grounded. */
    int roused() { return roused; }
    int splits() { return splits; }
    int shuts() { return shuts; }
    int lastShut() { return lastShut; }
    int drunk() { return drunk; }
    int grounded() { return grounded; }
    /** And: those that fly beaten; blows that went through one that only water or only fire deals with, and which of the two it was; and dives begun. */
    int fliers() { return fliers; }
    int fizzled() { return fizzled; }
    int fizzledAt() { return fizzledAt; }
    int dives() { return dives; }
    private int fliers, fizzled, fizzledAt, dives;

    /** How many times something has been turned aside by her in brown, a count that only goes up; and where the last was. */
    int braces() { return braces; }
    float braceX() { return braceX; }
    float braceY() { return braceY; }

    /** She is hurt, by something or by a fall, and it costs her a heart. */
    private boolean hurt(boolean fell) { return hurt(fell, 2); }

    /**
     * She is hurt, by something or by a fall, and it costs her hearts, counted in halves, and
     * nothing else: she flinches, a step back from whatever it was, cannot be hurt again for
     * a moment, and goes on from where she is; whoever she was fighting keeps what they had.
     * A fall puts her back on the last firm ground she stood on. When her hearts are gone she
     * loses a life: she comes back at the last flag with her hearts full again, and a place
     * where someone waits, or a place off the land, is as it was before she came in. When her
     * lives are gone the land is lost. The notes keep her from everything but a fall, and a
     * ball of ink takes one hurt for her. Returns whether anything came of it.
     */
    private boolean hurt(boolean fell, int costs) {
        if (!fell && (notes > 0 || worn[BLACK] > 0)) return false;
        // Three colours and more are a prism, through which whatever lands on her lands by half.
        if (!fell && named(PRISM)) costs = 1;
        if (shield) {
            if (--inkLeft <= 0) shield = false;
            spared++;
            safe = SAFE;
            // From a fall she still has to be put back, but she keeps her heart.
            if (!fell) return true;
        } else {
            hurts++;
            if (room >= 0 && wave > 0) arenaHurts++;
            // A letter that has been through a hurt is not one to hand over.
            if (quest == ASKED && level.quest == RunLevels.LETTER) fail();
            halves -= costs;
        }
        safe = SAFE;
        if (halves <= 0) {
            halves = 0;
            if (--lives <= 0) {
                lives = 0;
                state = LOST;
                return true;
            }
            livesLost++;
            halves = perks.hearts * 2;
            fade(false);
            cursed = 0;
            // Whatever was in the air is gone by the time she is back.
            shots.clear();
            swing = 0;
            charmed = 0;
            riding = -1;
            wet = false;
            x = startX;
            y = startY;
            firmX = x;
            firmY = y;
            speedAcross = 0;
            speedDown = 0;
            flinch = 0;
            // Out of the place where someone waits, it is as it was before she came in; and so is a place off the land. But a cellar is
            // not left for a life: she is back at its door, inside, and the wave she was in begins again.
            if (locked) leaving = true;
            if (room >= 0 && wave > 0 && level.rooms.get(room)[0] == RunLevels.ARENA) {
                int[] in = level.rooms.get(room);
                x = in[6] + (1 - WIDE) / 2;
                y = in[11] - TALL;
                firmX = x;
                firmY = y;
                arenaLives++;
                rewave = true;
            } else if (room >= 0) {
                quitting = true;
            }
            return true;
        }
        if (fell) {
            // Back onto the last firm ground: or, if that has gone from under where she stood, to the flag.
            boolean holds = false;
            for (int column = (int) Math.floor(firmX); column <= (int) Math.floor(firmX + WIDE - .001f); column++) {
                holds |= firm(at(column, (int) Math.floor(firmY + TALL + .05f)));
            }
            x = holds ? firmX : startX;
            y = holds ? firmY : startY;
            speedAcross = 0;
            speedDown = 0;
            swing = 0;
            riding = -1;
            wet = false;
            return true;
        }
        // A flinch: a step back from it, if there is ground to step back onto.
        flinch = .2f;
        float way = knock != 0 ? knock : speedAcross != 0 ? -Math.signum(speedAcross) : facingLeft ? 1 : -1;
        knock = 0;
        int column = (int) Math.floor(x + WIDE / 2 + way * 1.3f), feet = (int) Math.floor(y + TALL + .05f);
        boolean ground = false;
        for (int row = feet; row <= feet + 2; row++) {
            char under = at(column, row);
            ground |= firm(under) || under == RunLevels.THIN || under == RunLevels.LEDGE;
        }
        speedAcross = ground && riding < 0 ? way * 4.5f : 0;
        return true;
    }

    private void moveAcross(float by) {
        x += by;
        if (x < 0) {
            x = 0;
            speedAcross = 0;
        }
        if (x > columns - WIDE) {
            x = columns - WIDE;
            speedAcross = 0;
        }
        // Shut in, the way back is a wall as well.
        if (locked && x < level.arena + 1) {
            x = level.arena + 1;
            speedAcross = 0;
        }
        // A gate that is still shut stops her like a wall.
        if (room < 0 && !gateOpen() && x + WIDE > level.gate) {
            x = level.gate - WIDE;
            speedAcross = 0;
        }
        int top = (int) Math.floor(y), bottom = (int) Math.floor(y + TALL - .001f);
        if (by > 0) {
            int column = (int) Math.floor(x + WIDE);
            for (int row = top; row <= bottom; row++) {
                if (firm(at(column, row))) {
                    x = column - WIDE - .001f;
                    speedAcross = 0;
                    break;
                }
            }
        } else if (by < 0) {
            int column = (int) Math.floor(x);
            for (int row = top; row <= bottom; row++) {
                if (firm(at(column, row))) {
                    x = column + 1 + .001f;
                    speedAcross = 0;
                    break;
                }
            }
        }
    }

    /** How fast she comes down with the button that lets her down held: half as fast again as she falls. */
    private static final float PLUNGE = FALL * 1.5f;
    /** Whether that button is held; whether she has come down faster than a fall since she last stood; and how many ledges she has gone down through. */
    private boolean down, plunged;
    private int drops;

    /** The button that lets her down is held, or let go of. */
    void down(boolean held) { down = held; }

    int drops() { return drops; }

    /**
     * Whether she stands on a ledge she could be let down through: a thin one, with
     * something under it to come down on. Solid ground is never one, nor a ledge over
     * nothing, nor one over spikes, lava or water.
     */
    boolean droppable() {
        if (!onGround || riding >= 0) return false;
        int row = (int) Math.floor(y + TALL + .05f), column = (int) Math.floor(x + WIDE / 2);
        char under = at(column, row);
        return (under == RunLevels.THIN || under == RunLevels.LEDGE) && lands(column, row);
    }

    /** Whether, under a square, the first thing there is can be stood on. */
    private boolean lands(int column, int row) {
        int rows = room >= 0 ? level.land.length : RunLevels.ROWS;
        for (int below = row + 1; below < rows; below++) {
            char here = at(column, below);
            if (here == RunLevels.AIR) continue;
            return here == RunLevels.THIN || here == RunLevels.LEDGE || here == RunLevels.BLOCK || here == RunLevels.CRUMBLE || here == RunLevels.BELT_LEFT
                    || here == RunLevels.BELT_RIGHT || here == RunLevels.SLICK;
        }
        return false;
    }

    private void moveDown(float by) {
        y += by;
        boolean wasOnGround = onGround;
        float fell = speedDown;
        onGround = false;
        riding = -1;
        int left = (int) Math.floor(x), right = (int) Math.floor(x + WIDE - .001f);
        if (by > 0) {
            int row = (int) Math.floor(y + TALL);
            // With the button that lets her down held, a thin ledge does not hold her, if there is something under it to come down on.
            boolean through = down && lands((int) Math.floor(x + WIDE / 2), row);
            for (int column = left; column <= right; column++) {
                char here = at(column, row);
                boolean thin = here == RunLevels.THIN || here == RunLevels.LEDGE;
                if (thin && through && y + TALL - by <= row + .02f) {
                    if (column == left && y + TALL - by <= row + .0001f) drops++;
                    continue;
                }
                // A ledge, of either kind, holds her only when she comes down onto it from above.
                if (firm(here) || thin && y + TALL - by <= row + .02f) {
                    y = row - TALL;
                    speedDown = 0;
                    onGround = true;
                    plunged = false;
                    sprung = false;
                    wet = false;
                    // Coming down from a height counts as a landing; standing on the ground does not.
                    if (!wasOnGround && fell > 4) landings++;
                    break;
                }
            }
            // And so does a ledge that moves.
            for (int index = 0; index < level.movers.size() && !onGround; index++) {
                float from = moverX(index, time), top = moverY(index, time);
                if (x + WIDE <= from || x >= from + level.movers.get(index)[2] || y + TALL < top - .03f || y + TALL - by > top + .15f) continue;
                y = top - TALL;
                speedDown = 0;
                onGround = true;
                sprung = false;
                riding = index;
                if (!wasOnGround && fell > 4) landings++;
            }
        } else if (by < 0) {
            int row = (int) Math.floor(y);
            for (int column = left; column <= right; column++) {
                if (firm(at(column, row))) {
                    y = row + 1 + .001f;
                    speedDown = 0;
                    break;
                }
            }
        }
    }

    /**
     * A minus and a times walk until they meet a wall or the end of the ground under them,
     * and then turn. A divide hops on the spot. A minus with a knife stands where it is and
     * throws knives along the ground at her, to be jumped. A divide that shoots fires one of
     * its dots from the top of every other hop, over her head if she keeps to the ground.
     */
    private void move(Foe foe, float seconds) {
        if (foe.flat) {
            // How long it has been down, for whoever shows it going.
            foe.act += seconds;
            return;
        }
        if (foe.away && !RunLevels.rover(foe.kind) || foe.kind == RunLevels.TARGET) return;
        if (foe.crowd && (room >= 0 || Math.abs(foe.x - x) > 26)) return;
        foe.clock += seconds;
        if (foe.dazed > 0) {
            foe.dazed -= seconds;
            return;
        }
        float middle = foe.x + foe.wide() / 2, away = Math.abs(x + WIDE / 2 - middle);
        boolean toLeft = x + WIDE / 2 < middle;
        if (RunLevels.rover(foe.kind)) {
            roam(foe, seconds, middle, toLeft);
            return;
        }
        if (RunLevels.boss(foe.kind)) {
            waits(foe, seconds, middle, away, toLeft);
            return;
        }
        if (foe.kind >= RunLevels.SLIME) {
            beyond(foe, seconds, middle, away, toLeft);
            return;
        }
        if (foe.kind == RunLevels.DIVIDE || foe.kind == RunLevels.GUNNER) {
            float before = (foe.clock - seconds) % HOP_EVERY / HOP_EVERY, turn = foe.clock % HOP_EVERY / HOP_EVERY;
            foe.y = foe.ground - (float) Math.sin(turn * Math.PI) * HOP_HIGH;
            if (foe.kind == RunLevels.GUNNER) {
                foe.leftward = toLeft;
                if (before < .5f && turn >= .5f && (int) (foe.clock / HOP_EVERY) % 2 == 0 && away < DOT_REACH) {
                    shoot(DOT, false, middle, foe.y + .25f, toLeft ? -DOT_SPEED : DOT_SPEED, 0, 1.2f);
                    thrown++;
                }
            }
            return;
        }
        if (foe.kind == RunLevels.THROWER) {
            foe.leftward = toLeft;
            // It holds its knife until she is near enough to throw it at.
            foe.wait = Math.max(foe.wait - seconds, away < THROW_REACH + 1.5f ? -1 : WARNING);
            if (foe.wait <= 0 && away < THROW_REACH && Math.abs(y + TALL - foe.y - foe.tall()) < 2.5f) {
                foe.wait = THROW_EVERY;
                shoot(KNIFE, false, middle, foe.y + foe.tall() - .45f, toLeft ? -THROWN : THROWN, 0, 1.9f);
                thrown++;
            }
            return;
        }
        walk(foe, SPEED[foe.kind] * seconds, true);
    }

    /**
     * The three met in the middle of a land. Each is as those who wait are in this: it shows
     * what is coming, does it, and is then spent, which is the only time a blow lands; a
     * third of what it can take gone, it changes. The Margin Wyrm is roused three times,
     * as she comes a quarter, half and three quarters of the way through the land: each
     * time it flies in high from ahead, dropping blots along her way, turns behind her,
     * comes down level with her, hangs there spent, and is gone again; after the third time
     * it is driven off, if it has not been beaten. Blot sits in the way where she finds
     * it: it gathers itself and leaps at her, shaking the ground where it lands, or spits
     * two lumps of itself; and each blow that lands takes a slime's worth out of it, which
     * hops off as a slime. The Bookmark Gecko hangs over her on its ribbon, shows itself,
     * drops, runs at her, lashes its tongue, and is spent; and then goes back up its ribbon.
     */
    private void roam(Foe foe, float seconds, float middle, boolean toLeft) {
        float px = x + WIDE / 2, feet = y + TALL;
        int phase = phase(foe);
        if (foe.away) {
            if (room >= 0 || state != PLAYING) return;
            if (foe.kind == RunLevels.WYRM) {
                // (How many times it has come is kept where its things done would be.)
                if (foe.did >= 3 || px < level.gate * (foe.did + 1) / 4f - 6) return;
                foe.did++;
                foe.x = px + 9;
                foe.ground = feet - 6 - foe.tall();
                foe.leftward = true;
            } else if (Math.abs(px - foe.home - foe.wide() / 2) > 9) {
                return;
            }
            foe.away = false;
            foe.enter = foe.did <= 1 ? 1 : .4f;
            foe.step = 0;
            foe.mode = foe.kind == RunLevels.WYRM ? 0 : 1;
            foe.act = foe.kind == RunLevels.WYRM ? 0 : 1;
            foe.wait = .5f;
            foe.y = foe.kind == RunLevels.GECKO ? foe.ground - 7 : foe.ground;
            if (foe.kind == RunLevels.GECKO) foe.x = Math.max(foe.home - 7, Math.min(foe.home + 7, px - foe.wide() / 2));
            roused++;
            return;
        }
        if (foe.kind != RunLevels.WYRM && Math.abs(px - foe.home - foe.wide() / 2) > 18) {
            foe.away = true;
            foe.x = foe.home;
            foe.y = foe.ground;
            foe.step = 3;
            foe.enter = foe.flourish = 0;
            return;
        }
        if (foe.enter > 0) {
            foe.enter -= seconds;
            return;
        }
        if (foe.flourish > 0) {
            foe.flourish -= seconds;
            return;
        }
        float floor = foe.ground + foe.tall();
        if (foe.kind == RunLevels.WYRM) {
            if (foe.step == 0) {
                // Over her from ahead, and back over her from behind, letting blots fall all the way: twice over, then.
                foe.x += (foe.leftward ? -1 : 1) * (SPEED[foe.kind] - 1.7f) * seconds;
                foe.y = foe.ground + (float) Math.sin(foe.clock * 3) * .3f;
                if ((foe.wait -= seconds) <= 0 && Math.abs(middle - px) < 5) {
                    foe.wait = .55f - .07f * phase;
                    shoot(STOP, false, middle, foe.y + foe.tall(), 0, 6.5f, 3);
                    thrown++;
                }
                if (foe.leftward && middle < px - 5) {
                    foe.leftward = false;
                } else if (!foe.leftward && middle > px + 4) {
                    foe.step = 1;
                    foe.leftward = true;
                }
            } else if (foe.step == 1) {
                // Down, level with her, a little way ahead.
                float low = feet - foe.tall() - .7f, beside = px + 3.2f;
                foe.y += Math.signum(low - foe.y) * Math.min(Math.abs(low - foe.y), 7 * seconds);
                foe.x += Math.signum(beside - middle) * Math.min(Math.abs(beside - middle), 5 * seconds);
                foe.leftward = toLeft;
                if (Math.abs(low - foe.y) < .05f) {
                    foe.step = 2;
                    foe.act = 2.2f - .3f * phase;
                }
            } else if (foe.step == 2) {
                // Spent, and not hit: that was its coming low for this time.
                if ((foe.act -= seconds) <= 0) {
                    foe.step = 3;
                    foe.mode = 2;
                }
            } else if (foe.mode < 1) {
                // Hit once, it hangs there a moment longer, and can be hit once more before it goes.
                foe.mode++;
                foe.step = 2;
                foe.act = 1.3f;
            } else {
                // Up and away, until the next time.
                foe.y -= 6 * seconds;
                foe.x -= 7 * seconds;
                foe.leftward = true;
                if (foe.y < feet - 13) foe.away = true;
            }
            return;
        }
        if (foe.kind == RunLevels.BLOT) {
            float lo = foe.home - 6, hi = foe.home + 6;
            if (foe.step == 3) {
                foe.y = foe.ground;
                if ((foe.act -= seconds) <= 0) {
                    foe.mode = foe.turn++ % 2 == 0 ? 1 : 2;
                    foe.step = 0;
                    foe.act = .8f - .1f * phase;
                    foe.leftward = toLeft;
                }
            } else if (foe.step == 0) {
                if ((foe.act -= seconds) <= 0) {
                    foe.step = 1;
                    foe.act = foe.mode == 1 ? .85f : .3f;
                    // A leap is aimed at where she is, no further than it may go from where it sat.
                    foe.fromX = foe.x;
                    foe.toX = Math.max(lo, Math.min(hi, px - foe.wide() / 2));
                    if (foe.mode == 2) {
                        lob(GLOB, middle, foe.y + .3f, px - 1.2f);
                        lob(GLOB, middle, foe.y + .3f, px + 1.2f);
                        thrown++;
                    }
                }
            } else if (foe.step == 1) {
                foe.act -= seconds;
                if (foe.mode == 1) {
                    float through = 1 - Math.max(0, foe.act) / .85f;
                    foe.x = foe.fromX + (foe.toX - foe.fromX) * through;
                    foe.y = foe.ground - (float) Math.sin(through * Math.PI) * 3.2f;
                }
                if (foe.act <= 0) {
                    foe.y = foe.ground;
                    if (foe.mode == 1) quake(Math.abs(px - foe.x - foe.wide() / 2) - foe.wide() / 2 + 1, floor);
                    foe.step = 2;
                    foe.act = 1.6f - .15f * phase;
                }
            } else if ((foe.act -= seconds) <= 0) {
                foe.step = 3;
                foe.act = .7f;
            }
            return;
        }
        // The Bookmark Gecko.
        float hangs = foe.ground - 7;
        if (foe.step == 3) {
            // Back up its ribbon, and along to over where she is.
            foe.y = Math.max(hangs, foe.y - 8 * seconds);
            if (foe.y <= hangs && (foe.act -= seconds) <= 0) {
                float over = Math.max(foe.home - 7, Math.min(foe.home + 7, px - foe.wide() / 2));
                foe.x += Math.signum(over - foe.x) * Math.min(Math.abs(over - foe.x), 6 * seconds);
                if (Math.abs(over - foe.x) < .3f) {
                    foe.step = 0;
                    foe.mode = 1;
                    foe.act = .9f - .12f * phase;
                }
            }
        } else if (foe.step == 0) {
            if ((foe.act -= seconds) <= 0) foe.step = 1;
        } else if (foe.step == 1) {
            if (foe.mode == 1) {
                // The drop.
                if ((foe.y += 16 * seconds) >= foe.ground) {
                    foe.y = foe.ground;
                    foe.mode = 2;
                    foe.act = .6f;
                    foe.leftward = toLeft;
                }
            } else if (foe.mode == 2) {
                // The run at her.
                foe.act -= seconds;
                if (!walk(foe, 9 * seconds, false) || foe.act <= 0) {
                    foe.mode = 3;
                    foe.act = .35f;
                    foe.leftward = toLeft;
                }
            } else if ((foe.act -= seconds) <= 0) {
                // The tongue.
                cut(foe, 2.4f, STRUCK);
                foe.step = 2;
                foe.act = 1.5f - .15f * phase;
            }
        } else if ((foe.act -= seconds) <= 0) {
            foe.step = 3;
            foe.act = .5f;
        }
    }

    /** Walks something on the way it faces, if there is ground to walk on and nothing in the way; if not, it may turn. Returns whether it moved. */
    private boolean walk(Foe foe, float by, boolean turns) {
        float ahead = foe.leftward ? foe.x - by : foe.x + foe.wide() + by;
        int column = (int) Math.floor(ahead), row = (int) Math.floor(foe.ground + foe.tall() - .001f);
        if (ahead < 0 || ahead > columns || solid(at(column, row)) || !solid(at(column, row + 1)) || at(column, row) == RunLevels.SPIKE
                || foe.spawned && level.arena >= 0 && foe.x < level.main && (ahead < level.arena + 1.2f || ahead > level.gate - .2f)
                // (And one put in to make up a land's numbers does not follow her into the place where someone waits.)
                || foe.crowd && level.arena >= 0 && foe.x < level.arena && ahead > level.arena - 1.5f) {
            if (turns) foe.leftward = !foe.leftward;
            return false;
        }
        foe.x += foe.leftward ? -by : by;
        return true;
    }

    /** Counts down the ledges that crumble: one she stands on begins to, falls, and after a while is back. */
    private void crumbling(float seconds) {
        if (onGround && riding < 0) {
            int row = (int) Math.floor(y + TALL + .05f);
            for (int column = (int) Math.floor(x); column <= (int) Math.floor(x + WIDE - .001f); column++) {
                if (at(column, row) == RunLevels.CRUMBLE && !crumble.containsKey(column * 64 + row)) crumble.put(column * 64 + row, CRUMBLES);
            }
        }
        if (crumble.isEmpty()) return;
        for (java.util.Iterator<java.util.Map.Entry<Integer, Float>> each = crumble.entrySet().iterator(); each.hasNext(); ) {
            java.util.Map.Entry<Integer, Float> one = each.next();
            float left = one.getValue();
            if (left > 0) {
                left -= seconds;
                if (left <= 0) left = -CRUMBLE_BACK;
            } else {
                left += seconds;
                if (left >= 0) {
                    each.remove();
                    continue;
                }
            }
            one.setValue(left);
        }
    }

    /**
     * The signs and the people of the second volume. A slime in a tie hops at her; a question
     * mark bounds to and fro; a punctuation dragon flies over where it began and spits full
     * stops; a censor bar tiptoes, then dashes; one of the Heart Guard marches after her; and
     * one of the queen's heavy words hangs until she is under it, then falls. Gokhae hides
     * behind her fan and can only be hurt while she lowers it to speak. Miri throws her
     * gazette at where her visitor is about to be, while Gari paints bars over what the
     * visitor sees. Yeobaek steps out of the white space behind her, cuts, and dozes off.
     * King Slime comes down hard enough to throw whoever stands near. And the Mawang hovers,
     * sends feathers, swoops, and has to catch her breath.
     */
    private void beyond(Foe foe, float seconds, float middle, float away, boolean toLeft) {
        switch (foe.kind) {
            case RunLevels.SLIME:
            case RunLevels.POGO: {
                boolean slime = foe.kind == RunLevels.SLIME;
                float every = slime ? .8f : 1.1f, turn = foe.clock % every / every;
                foe.y = foe.ground - (float) Math.sin(turn * Math.PI) * (slime ? .6f : 2.4f);
                if (slime && away < 8) foe.leftward = toLeft;
                if (turn > .12f) walk(foe, SPEED[foe.kind] * seconds, !slime || away >= 8);
                break;
            }
            case RunLevels.DRAGON: {
                if (Math.abs(foe.x - foe.home) > 3) foe.leftward = foe.x > foe.home;
                foe.x += (foe.leftward ? -1 : 1) * SPEED[foe.kind] * seconds;
                foe.y = foe.ground - 2.6f - (float) Math.sin(foe.clock * 2);
                if ((foe.wait -= seconds) <= 0 && away < 6) {
                    foe.wait = 2.8f;
                    aim(STOP, middle, foe.y + foe.tall() / 2, 5.5f, 0, 2);
                }
                break;
            }
            case RunLevels.BAR: {
                // Tiptoeing (0); seen her, and gathering itself (1); dashing (2); getting its breath back (3).
                if (foe.mode == 0) {
                    walk(foe, SPEED[foe.kind] * seconds, true);
                    if (away < 4.5f && Math.abs(y + TALL - foe.ground - foe.tall()) < 1.5f) {
                        foe.mode = 1;
                        foe.act = .5f;
                        foe.leftward = toLeft;
                    }
                } else if ((foe.act -= seconds) <= 0) {
                    foe.mode = foe.mode == 3 ? 0 : foe.mode + 1;
                    foe.act = foe.mode == 2 ? .6f : 1.4f;
                } else if (foe.mode == 2 && !walk(foe, 9 * seconds, false)) {
                    foe.mode = 3;
                    foe.act = 1.4f;
                }
                break;
            }
            case RunLevels.FAN: {
                if (away < 7 && away > .2f) {
                    foe.leftward = toLeft;
                    walk(foe, SPEED[foe.kind] * seconds, false);
                }
                break;
            }
            case RunLevels.WORD: {
                // Hanging (0); about to fall (1); falling (2); lying where it fell (3); going back up (4).
                float hangs = foe.ground - WORD_HANGS;
                if (foe.mode == 0) {
                    foe.y = hangs;
                    if (away < 1.4f) {
                        foe.mode = 1;
                        foe.act = .35f;
                    }
                } else if (foe.mode == 1 || foe.mode == 3) {
                    if ((foe.act -= seconds) <= 0) foe.mode++;
                } else if (foe.mode == 2) {
                    if ((foe.y += 14 * seconds) >= foe.ground) {
                        foe.y = foe.ground;
                        foe.mode = 3;
                        foe.act = 1.6f;
                    }
                } else if ((foe.y -= 3 * seconds) <= hangs) {
                    foe.mode = 0;
                }
                break;
            }
            case RunLevels.DRIFTER:
            case RunLevels.MITE: {
                // To and fro over where it began, bobbing: a tittle quicker, and each a little after the one before it in the line.
                boolean mite = foe.kind == RunLevels.MITE;
                if (Math.abs(foe.x - foe.home) > (mite ? 4 : 3.5f)) foe.leftward = foe.x > foe.home;
                foe.x += (foe.leftward ? -1 : 1) * SPEED[foe.kind] * seconds;
                foe.y = foe.ground + (float) Math.sin(foe.clock * (mite ? 3.2f : 2.4f)) * (mite ? .4f : .45f);
                break;
            }
            case RunLevels.SWOOPER: {
                // Hanging (0); it has marked her (1); the dive, down through where she stood and up the other side (2); getting its height back (3).
                if (foe.mode == 0) {
                    foe.y = foe.ground + (float) Math.sin(foe.clock * 2) * .25f;
                    foe.leftward = toLeft;
                    if ((foe.wait -= seconds) <= 0 && away < 4.5f && y + TALL > foe.y + 1.5f) {
                        foe.mode = 1;
                        foe.act = .6f;
                    }
                } else if (foe.mode == 1) {
                    if ((foe.act -= seconds) <= 0) {
                        foe.mode = 2;
                        foe.act = 0;
                        foe.fromX = foe.x;
                        foe.toX = Math.max(foe.x - 6.5f, Math.min(foe.x + 6.5f, x + WIDE / 2 - foe.wide() / 2));
                        // (How far down it goes is kept where its wait would be.)
                        foe.wait = Math.max(1.5f, Math.min(7, y + TALL - foe.tall() - foe.ground));
                        foe.leftward = foe.toX < foe.fromX;
                        dives++;
                    }
                } else if (foe.mode == 2) {
                    float through = Math.min(1, (foe.act += seconds) / 1.1f);
                    foe.x = foe.fromX + (foe.toX - foe.fromX) * 2 * through;
                    foe.y = foe.ground + foe.wait * (float) Math.sin(through * Math.PI);
                    if (through >= 1) {
                        foe.mode = 3;
                        foe.act = 1.4f;
                    }
                } else {
                    // Back to over where it began.
                    float back = foe.home - foe.x;
                    foe.y = foe.ground;
                    foe.x += Math.signum(back) * Math.min(Math.abs(back), 2.5f * seconds);
                    if (back != 0) foe.leftward = back < 0;
                    if ((foe.act -= seconds) <= 0) {
                        foe.mode = 0;
                        foe.wait = .5f;
                    }
                }
                break;
            }
            case RunLevels.BOMBER: {
                if (Math.abs(foe.x - foe.home) > 6) foe.leftward = foe.x > foe.home;
                foe.x += (foe.leftward ? -1 : 1) * SPEED[foe.kind] * seconds;
                foe.y = foe.ground + (float) Math.sin(foe.clock * 1.6f) * .2f;
                // What it lets go falls straight down: one every so often while she is anywhere under its way; it puffs itself up first.
                foe.wait = Math.max(away < 5 ? -1 : .45f, foe.wait - seconds);
                foe.mode = foe.wait < .45f && away < 5 ? 1 : 0;
                if (foe.wait <= 0 && away < 5) {
                    foe.wait = 1.5f;
                    shoot(STOP, false, middle, foe.y + foe.tall(), 0, 6.5f, 4);
                    thrown++;
                }
                break;
            }
            case RunLevels.SHOOTER: {
                foe.leftward = toLeft;
                foe.y = foe.ground + (float) Math.sin(foe.clock * 2.2f) * .3f;
                if (foe.mode == 1) {
                    // Taking aim; and then it fires at where she is.
                    if ((foe.act -= seconds) <= 0) {
                        foe.mode = 0;
                        foe.wait = 2.6f;
                        aim(foe.step == 1 ? CURSE : DOT, middle, foe.y + foe.tall() / 2, foe.step == 1 ? 5 : 6.5f, 0, 2.6f);
                        foe.step = 0;
                        thrown++;
                    }
                } else {
                    // It keeps its distance: from nearer than four squares it backs off, as far as it may go from where it began.
                    float to = foe.x + (toLeft ? 1 : -1) * SPEED[foe.kind] * seconds;
                    if (away < 4 && Math.abs(to - foe.home) <= 5) foe.x = to;
                    if ((foe.wait -= seconds) <= 0 && away < 5) {
                        foe.mode = 1;
                        // From the fifty-first land on, every third thing it fires is black, and it takes longer over it.
                        foe.step = level.number >= RunLevels.CURSES_FROM && ++foe.turn % 3 == 0 ? 1 : 0;
                        foe.act = foe.step == 1 ? .9f : .55f;
                    }
                }
                break;
            }
            case RunLevels.WISP: {
                // It comes for her, slowly, no further than eight squares from where it began, and goes back when she has gone.
                float px = x + WIDE / 2;
                boolean after = away < 5.5f && Math.abs(px - foe.home) < 8;
                float toX = after ? px - foe.wide() / 2 : foe.home, toY = after ? y + TALL / 2 - foe.tall() / 2 : foe.ground;
                float far = Math.max(.01f, (float) Math.hypot(toX - foe.x, toY - foe.y)), by = Math.min(far, SPEED[foe.kind] * seconds);
                foe.x += (toX - foe.x) / far * by;
                foe.y += (toY - foe.y) / far * by;
                foe.leftward = toLeft;
                foe.mode = after ? 1 : 0;
                break;
            }
            case RunLevels.RIME: {
                // Round and round where it began.
                foe.x = foe.home + (float) Math.cos(foe.clock * SPEED[foe.kind]) * 2;
                foe.y = foe.ground + (float) Math.sin(foe.clock * SPEED[foe.kind]) * 1.2f;
                foe.leftward = toLeft;
                break;
            }
            default:
                break;
        }
    }

    /** What each of those who wait does, in order, in each third of the fight: the later thirds bring in more. */
    private static final int[][][] PATTERNS = new int[32][][];
    /** How long each of them is spent after something they have done. */
    private static final float[] WINDOW = new float[32];
    static {
        PATTERNS[RunLevels.ILLYA] = new int[][]{{1, 3}, {1, 2, 3}, {4, 2, 1, 3}};
        PATTERNS[RunLevels.TSUBAKIEH] = new int[][]{{1, 2}, {1, 3, 2}, {4, 1, 3, 2}};
        PATTERNS[RunLevels.ZERO] = new int[][]{{3, 1}, {3, 4, 1, 2}, {5, 1, 2, 4, 3}};
        PATTERNS[RunLevels.GOKHAE] = new int[][]{{1, 2}, {1, 3, 2}, {3, 2, 1, 2}};
        PATTERNS[RunLevels.MIRI] = new int[][]{{1}, {1, 2}, {2, 3, 1}};
        PATTERNS[RunLevels.GARI] = new int[][]{{1}, {1, 2}, {3, 1, 2}};
        PATTERNS[RunLevels.YEOBAEK] = new int[][]{{1, 2}, {1, 3, 2}, {4, 3, 2}};
        PATTERNS[RunLevels.KING] = new int[][]{{1, 2}, {1, 3, 2}, {4, 3, 1, 2}};
        PATTERNS[RunLevels.MAWANG] = new int[][]{{1, 3, 2}, {1, 5, 3, 4, 2}, {6, 5, 4, 1, 3, 2}};
        // The seven others each do three things: two from the first, the third from the second third of the fight on.
        for (int kind = RunLevels.GUARD; kind <= RunLevels.OLD_BOOK; kind++) {
            PATTERNS[kind] = kind == RunLevels.SUMBI ? new int[][]{{2, 1}, {2, 3, 1}, {3, 1, 2, 1}} : new int[][]{{1, 2}, {1, 3, 2}, {3, 2, 1, 2}};
            WINDOW[kind] = kind <= RunLevels.BUTLER ? 1.7f : 1.4f;
        }
        WINDOW[RunLevels.ILLYA] = 1.5f;
        WINDOW[RunLevels.TSUBAKIEH] = .9f;
        WINDOW[RunLevels.ZERO] = 1.3f;
        WINDOW[RunLevels.GOKHAE] = 1.5f;
        WINDOW[RunLevels.MIRI] = 1.2f;
        WINDOW[RunLevels.GARI] = 1.4f;
        WINDOW[RunLevels.YEOBAEK] = 1.5f;
        WINDOW[RunLevels.KING] = 1.4f;
        WINDOW[RunLevels.MAWANG] = 1.5f;
        // In the second volume they are spent for a little less long than they were.
        for (int kind = RunLevels.GOKHAE; kind <= RunLevels.PARTY; kind++) if (kind != RunLevels.GUARD && kind != RunLevels.BUTLER) WINDOW[kind] *= .88f;
    }

    /**
     * Those who wait at the end. Each does one thing after another from a list of her own:
     * she shows that it is coming, does it, and is then spent for a moment, which is when she
     * can be hurt. A thing is named here by ten times who does it and which of hers it is.
     *
     * Illya (6): three hearts in a fan (61); hearts that close on where her visitor stands
     * (62); the grand compliment, a shock along the floor (63); and two of her Heart Guard
     * (64). Tsubakieh (7): the swing (71); a leap, and a shock both ways where she comes
     * down (72); the bat thrown, which comes back (73); and a run the length of the place
     * (74). Zero (3): a roll that comes back off the ends (31); a leap and rubble (32); three
     * books (33); times nothing, which takes what her visitor carries and her form if she
     * stands on the floor (34); and two minuses (35). Gokhae (14): three twisted sayings
     * (141); the blades of her fan (142); and a twist that turns the arrows round for whoever
     * stands on the ground (143). Miri (15): the gazette, thrown ahead (151); three of them
     * (152); and a headline dropped where her visitor is going (153). Gari (16): bars over
     * what her visitor sees (161); a censor bar of her own (162); and a dash with her brush
     * (163). Yeobaek (17): out of the white space behind her visitor, and a cut (171); a dash
     * the length of the place (172); three strokes from above (173); and three cuts one after
     * another (174). King Slime (18): three hops that shake the ground (181); three lumps of
     * himself (182); slimes (183); and a slide the length of the place (184). The Mawang
     * (19): feathers (191); a swoop (192); dark fire, low, high and low (193); feathers
     * falling the length of the place (194); a dash (195); and a black ribbon (196).
     */
    private void waits(Foe foe, float seconds, float middle, float away, boolean toLeft) {
        if (foe.enter > 0) {
            foe.enter -= seconds;
            return;
        }
        if (foe.flourish > 0) {
            foe.flourish -= seconds;
            return;
        }
        int phase = phase(foe);
        // Each third of the fight she is quicker to begin and quicker to recover.
        float quick = 1 - .14f * phase, floor = foe.ground + foe.tall(), px = x + WIDE / 2;
        // She keeps to the open: never nearer the ends of her place than this.
        float lo = level.arena >= 0 ? level.arena + 2 : foe.home - 12, hi = level.arena >= 0 ? level.gate - 1.5f - foe.wide() : foe.home + 2.5f;
        if (foe.spawned && room >= 0) {
            lo = level.rooms.get(room)[3] + 1;
            hi = level.rooms.get(room)[4] - foe.wide();
        }
        if ((foe.act -= seconds) <= 0) {
            int what = foe.kind * 10 + foe.mode;
            if (foe.step == 1) done(foe, what, phase, px, floor);
            if (++foe.step > 2) {
                int[] pattern = PATTERNS[foe.kind][phase];
                foe.mode = pattern[foe.turn++ % pattern.length];
                foe.step = 0;
                foe.wait = 0;
                foe.leftward = toLeft;
                foe.act = tell(foe, px, lo, hi) * quick;
            } else if (foe.step == 1) {
                foe.did |= 1 << foe.mode;
                specials++;
                special = what;
                foe.wait = 0;
                foe.unseen = false;
                foe.act = strike(foe, what, phase, middle, away, toLeft, px, floor, lo, hi);
            } else {
                foe.unseen = false;
                // With her bat out of her hands Tsubakieh is spent until it is back.
                foe.act = (what == 73 ? 1.9f : WINDOW[foe.kind]) * quick;
            }
        }
        during(foe, foe.kind * 10 + foe.mode, seconds, phase, middle, away, toLeft, px, floor, lo, hi);
        if (foe.kind != RunLevels.MIRI) foe.x = Math.max(lo, Math.min(hi, foe.x));
    }

    /** How long she shows what is coming, and where she shows it from. */
    private float tell(Foe foe, float px, float lo, float hi) {
        int what = foe.kind * 10 + foe.mode;
        if (foe.kind == RunLevels.YEOBAEK) {
            // Yeobaek shows nothing: she is between the lines, and what is coming is seen on the floor.
            foe.unseen = true;
            foe.y = foe.ground;
            if (what == 172) {
                boolean right = px < (lo + hi) / 2;
                foe.x = right ? hi : lo;
                foe.leftward = right;
            }
        }
        // Sumbi dives: she is under the ground until she comes up.
        if (what == 241) foe.unseen = true;
        if (foe.kind == RunLevels.GUARD || foe.kind == RunLevels.BUTLER) return 1;
        switch (what) {
            case 242:
                return 1.6f;
            case 71:
                return 1.8f;
            case 63:
            case 34:
            case 171:
                return 1;
            case 143:
                return .9f;
            case 62:
            case 172:
                return .8f;
            case 33:
            case 73:
            case 151:
            case 174:
            case 181:
                return .5f;
            case 61:
            case 72:
            case 32:
            case 35:
            case 141:
            case 142:
            case 161:
            case 162:
            case 182:
            case 183:
            case 191:
            case 192:
            case 196:
                return .6f;
            default:
                return .7f;
        }
    }

    /** She does it. Returns how long the doing takes. */
    private float strike(Foe foe, int what, int phase, float middle, float away, boolean toLeft, float px, float floor, float lo, float hi) {
        float py = y + TALL / 2, way = toLeft ? -1 : 1;
        switch (what) {
            case 61:
                spread(HEART, middle, foe.y + foe.tall() * .4f, 3, .3f, 5.5f, 2.6f);
                return .5f;
            case 62: {
                // From either side along the ground and from above on either side, to where she stands now.
                float[][] from = {{-5, 0}, {5, 0}, {-3.5f, -3.5f}, {3.5f, -3.5f}};
                for (float[] one : from) {
                    float far = (float) Math.hypot(one[0], one[1]);
                    shoot(HEART, false, px + one[0], py + one[1], -one[0] / far * 3.4f, -one[1] / far * 3.4f, far / 3.4f);
                }
                thrown++;
                return 1.4f;
            }
            case 63:
                shoot(SHOCK, false, middle, floor - .3f, way * 9, 0, 2.6f);
                thrown++;
                return .5f;
            case 64:
                spawn(RunLevels.FAN, lo - .5f, floor);
                spawn(RunLevels.FAN, hi + foe.wide() + 1.2f, floor);
                return .4f;
            case 71:
                cut(foe, BAT_REACH, BATTED);
                swings++;
                return .25f;
            case 72:
            case 32:
                foe.fromX = foe.x;
                foe.toX = Math.max(lo, Math.min(hi, px - foe.wide() / 2));
                return what == 72 ? .8f : 1;
            case 73: {
                Shot bat = shoot(BOOMERANG, false, middle, floor - .55f, way * 11, 0, 2);
                bat.spin = way * 11;
                thrown++;
                return .2f;
            }
            case 74:
                foe.leftward = toLeft;
                return 1.6f;
            case 163:
                foe.leftward = toLeft;
                return 1;
            case 184:
            case 195:
                foe.leftward = toLeft;
                return 2;
            case 172:
                return 2;
            case 31:
                foe.leftward = toLeft;
                // He comes back off the end of the place once, and once more for each third of the fight gone.
                foe.wait = 1 + phase;
                return 5;
            case 33:
            case 182:
                for (int which = -1; which <= 1; which++) lob(what == 33 ? BOOK : GLOB, middle, foe.y + .3f, px + which * 2.5f);
                thrown++;
                return .5f;
            case 34:
                // Anything times nothing is nothing: but only what stands on his floor.
                if (onGround && y + TALL > floor - .2f) {
                    weapon = UNARMED;
                    fade(false);
                    rush = 0;
                    magnet = 0;
                    notes = 0;
                    wiped++;
                }
                return .4f;
            case 35:
                spawn(RunLevels.MINUS, foe.x - 2, floor);
                spawn(RunLevels.MINUS, foe.x + foe.wide() + 2, floor);
                return .4f;
            case 141:
                foe.fromX = 0;
                return 1.3f;
            case 142:
                spread(BLADE, middle, foe.y + .5f, phase == 2 ? 5 : 3, .26f, 7, 2.2f);
                return .4f;
            case 143:
                if (onGround) {
                    charmed = CHARM;
                    charms++;
                }
                return .4f;
            case 151:
                aim(SCROLL, middle, foe.y + .3f, 6.5f, away / 6.5f, 2.2f);
                return .3f;
            case 152:
                for (int which = 0; which < 3; which++) aim(SCROLL, middle, foe.y + .3f, 6.5f, away / 6.5f * which, 2.2f);
                return .4f;
            case 153:
                for (int which = -1; which <= 1; which++) shoot(DEBRIS, false, px + speedAcross * .9f + which * 2, Math.max(.5f, y - 7), 0, 0, 3);
                thrown++;
                return .4f;
            case 161:
                censored = 2.6f;
                censors++;
                return .3f;
            case 162:
                spawn(RunLevels.BAR, middle + way * 1.5f, floor);
                return .4f;
            case 171:
                cut(foe, 1.7f, SLASHED);
                swings++;
                return .3f;
            case 173:
                foe.x = px - foe.wide() / 2;
                foe.y = foe.ground - 4.2f;
                foe.fromX = 0;
                return .8f;
            case 174:
                foe.unseen = true;
                foe.fromX = 0;
                foe.toX = 0;
                return 2.1f;
            case 181:
                return 2.7f;
            case 183:
                spawn(RunLevels.SLIME, foe.x - 1.5f, floor);
                spawn(RunLevels.SLIME, foe.x + foe.wide() + 1.5f, floor);
                if (phase == 2) spawn(RunLevels.SLIME, middle, floor);
                return .5f;
            case 191:
                spread(FEATHER, middle, foe.y + .5f, phase == 2 ? 5 : 3, .32f, 6.5f, 2);
                return .4f;
            // The Guild Guard: a thrust of the halberd, a low sweep that runs along the ground, and a march the length of the place.
            case 201:
                cut(foe, 1.6f, STRUCK);
                swings++;
                return .3f;
            case 202:
                shoot(SHOCK, false, middle, floor - .3f, way * 6, 0, 3);
                thrown++;
                return .5f;
            case 203:
            case 212:
            case 232:
            case 252:
            case 262:
                foe.leftward = toLeft;
                return what == 252 ? .7f : 2;
            // Penchok-jipsa: three blots lobbed, a dash on his nib, and a signature of three drops in a fan.
            case 211:
            case 233:
                for (int which = -1; which <= 1; which++) lob(GLOB, middle, foe.y + .3f, px + which * 2.5f);
                thrown++;
                return .5f;
            case 213:
                spread(DOT, middle, foe.y + .5f, 3, .24f, 6, 2.2f);
                return .4f;
            // Byeoru: two scrolls thrown ahead of her visitor, ink that falls where it is marked, and a censor bar.
            case 221:
                aim(SCROLL, middle, foe.y + .3f, 6.5f, 0, 2.2f);
                aim(SCROLL, middle, foe.y + .3f, 6.5f, away / 6.5f, 2.2f);
                return .4f;
            case 222:
                for (int which = -1; which <= 1; which++) shoot(DEBRIS, false, px + speedAcross * .6f + which * 2, Math.max(.5f, y - 7), 0, 0, 3);
                thrown++;
                return .4f;
            case 223:
                spawn(RunLevels.BAR, middle + way * 1.5f, floor);
                return .4f;
            // Jupan: five coins in a fan, a slide on her abacus, and beads lobbed.
            case 231:
                spread(DOT, middle, foe.y + .5f, 5, .22f, 6.5f, 2.2f);
                return .4f;
            // Sumbi: up from under her visitor, the crowbar, and a net that turns the arrows round.
            case 241:
                foe.x = Math.max(lo, Math.min(hi, px - foe.wide() / 2));
                foe.y = foe.ground;
                if (safe <= 0 && onGround && y + TALL > floor - .2f && Math.abs(px - foe.x - foe.wide() / 2) < 1.1f && !braced(foe.x + foe.wide() / 2)) {
                    cause = TOUCHED;
                    hurt(false);
                }
                quakes++;
                return .4f;
            case 242:
                cut(foe, 1.7f, BATTED);
                swings++;
                return .3f;
            case 243:
                spread(HEART, middle, foe.y + .5f, 1, 0, 5.5f, 2.6f);
                return .4f;
            // The Brake Pads: brakes slammed on, which runs both ways along the ground; a skid; and sparks.
            case 251:
                shoot(SHOCK, false, middle, floor - .3f, -8, 0, 1.8f);
                shoot(SHOCK, false, middle, floor - .3f, 8, 0, 1.8f);
                quakes++;
                thrown++;
                return .5f;
            case 253:
                spread(BLADE, middle, foe.y + .5f, 5, .3f, 7, 2.2f);
                return .4f;
            // The Straight-Ahead Party: three arrows, low, high and low; the sword, the length of the place; and the shield, along the ground.
            case 261:
                foe.fromX = 0;
                return 1;
            case 263:
                shoot(SHOCK, false, middle, floor - .3f, way * 9, 0, 2.6f);
                thrown++;
                return .5f;
            // The Old Book: three bubbles of a snore, lobbed; a flurry of his pages; and himself slammed shut, which runs both ways along the floor.
            case 271:
                for (int which = -1; which <= 1; which++) lob(GLOB, middle, foe.y + .3f, px + which * 2.5f);
                thrown++;
                return .5f;
            case 272:
                spread(BLADE, middle, foe.y + .5f, phase == 2 ? 5 : 3, .28f, 6.5f, 2.2f);
                return .4f;
            case 273:
                shoot(SHOCK, false, middle, floor - .3f, -7, 0, 1.8f);
                shoot(SHOCK, false, middle, floor - .3f, 7, 0, 1.8f);
                quakes++;
                thrown++;
                return .5f;
            case 192:
                foe.fromX = px;
                foe.toX = Math.min(foe.ground, y + TALL - foe.tall());
                return 1.1f;
            case 193:
                foe.fromX = 0;
                return 1;
            case 194:
                // Feathers falling the length of the place, with room between them; under a ledge there is none.
                for (float column = lo - 1 + foe.turn % 2 * 1.4f; column < hi + foe.wide() + 1.5f; column += 2.8f) {
                    shoot(FEATHER, false, column, Math.max(.5f, floor - 8.5f), 0, 6.5f, 1.5f);
                }
                thrown++;
                return .6f;
            default:
                // 196, the black ribbon.
                aim(CURSE, middle, foe.y + .5f, 6, 0, 2.4f);
                return .4f;
        }
    }

    /** What she does for as long as a thing lasts: where she goes, and what she sends one after another. */
    private void during(Foe foe, int what, float seconds, int phase, float middle, float away, boolean toLeft, float px, float floor, float lo, float hi) {
        float way = foe.leftward ? -1 : 1;
        boolean doing = foe.step == 1;
        switch (foe.kind) {
            case RunLevels.ILLYA: {
                // She floats a little up and a little down and keeps her distance; for the grand compliment, and spent, she comes down.
                foe.leftward = toLeft;
                boolean down = foe.step == 2 || foe.mode == 3;
                float to = down ? foe.ground : foe.ground - .6f - (float) Math.sin(foe.clock * 2.2f) * .5f;
                foe.y += Math.max(-4 * seconds, Math.min(4 * seconds, to - foe.y));
                if (!down) {
                    float keeps = px + (toLeft ? ILLYA_KEEPS : -ILLYA_KEEPS) - foe.wide() / 2;
                    foe.x += Math.max(-ILLYA_DRIFT * seconds, Math.min(ILLYA_DRIFT * seconds, keeps - foe.x));
                }
                break;
            }
            case RunLevels.TSUBAKIEH: {
                if (foe.step == 0) foe.leftward = toLeft;
                if (what == 71 && foe.step == 0 && foe.act > BAT_RAISED) {
                    // She comes for her visitor, and raises her bat when she is near, or when she has walked long enough.
                    if (away > BAT_NEAR) foe.x += (toLeft ? -1 : 1) * SPEED[foe.kind] * (1 + .3f * phase) * seconds;
                    else foe.act = BAT_RAISED;
                } else if (what == 72 && doing) {
                    float turn = 1 - Math.max(0, foe.act) / .8f;
                    foe.x = foe.fromX + (foe.toX - foe.fromX) * turn;
                    foe.y = foe.ground - (float) Math.sin(turn * Math.PI) * 3;
                } else if (what == 74 && doing) {
                    foe.x += way * 10 * seconds;
                    if (foe.x <= lo || foe.x >= hi) foe.act = 0;
                }
                break;
            }
            case RunLevels.ZERO: {
                if (foe.step == 0) {
                    foe.leftward = toLeft;
                    if ((foe.mode == 3 || foe.mode == 5) && away > 3.5f) foe.x += (toLeft ? -1 : 1) * SPEED[foe.kind] * seconds;
                } else if (what == 31 && doing) {
                    foe.x += way * 9 * seconds;
                    if (foe.x <= lo || foe.x >= hi) {
                        if (foe.wait-- > 0) foe.leftward = !foe.leftward;
                        else foe.act = 0;
                    }
                } else if (what == 32 && doing) {
                    float turn = 1 - Math.max(0, foe.act);
                    foe.x = foe.fromX + (foe.toX - foe.fromX) * turn;
                    foe.y = foe.ground - (float) Math.sin(turn * Math.PI) * 4;
                }
                break;
            }
            case RunLevels.GOKHAE: {
                foe.leftward = toLeft;
                if (foe.step == 0) {
                    float keeps = px + (toLeft ? 5 : -5) - foe.wide() / 2;
                    foe.x += Math.max(-1.6f * seconds, Math.min(1.6f * seconds, keeps - foe.x));
                } else if (what == 141 && doing && foe.fromX < 3 && (foe.wait -= seconds) <= 0) {
                    foe.wait = .45f;
                    foe.fromX++;
                    shoot(SAYING, false, middle, foe.y + .5f, toLeft ? -5.5f : 5.5f, 0, 2.4f);
                    thrown++;
                }
                break;
            }
            case RunLevels.MIRI:
                foe.leftward = toLeft;
                break;
            case RunLevels.GARI: {
                if (foe.step == 0) {
                    foe.x += way * SPEED[foe.kind] * seconds;
                    if (foe.x <= lo || foe.x >= hi) foe.leftward = foe.x >= hi;
                } else if (what == 163 && doing) {
                    foe.x += way * 9 * seconds;
                    if (foe.x <= lo || foe.x >= hi) foe.act = 0;
                }
                break;
            }
            case RunLevels.YEOBAEK: {
                if (what == 171 && foe.step == 0 && foe.unseen && foe.act <= .6f) {
                    appear(foe, px, lo, hi);
                } else if (what == 172 && doing) {
                    foe.x += way * 13 * seconds;
                    if (foe.x <= lo || foe.x >= hi) foe.act = 0;
                } else if (what == 173 && doing && foe.fromX < 3 && (foe.wait -= seconds) <= 0) {
                    foe.wait = .22f;
                    shoot(BLADE, false, px + (foe.fromX - 1) * 1.6f, foe.y + foe.tall(), 0, 9, 1.2f);
                    thrown++;
                    foe.fromX++;
                } else if (what == 174 && doing && foe.toX < 3 && (foe.wait -= seconds) <= 0) {
                    if (foe.fromX == 0) {
                        appear(foe, px, lo, hi);
                        foe.fromX = 1;
                        foe.wait = .4f;
                    } else {
                        cut(foe, 1.7f, SLASHED);
                        swings++;
                        foe.fromX = 0;
                        foe.toX++;
                        foe.wait = .3f;
                    }
                }
                break;
            }
            case RunLevels.KING: {
                if (foe.step == 0) foe.leftward = toLeft;
                if (what == 181 && doing) {
                    float turn = (2.7f - Math.max(0, foe.act)) % .9f / .9f;
                    // He has come down, and goes up again toward wherever she is now.
                    if (turn < foe.wait) {
                        quake(away, floor);
                        foe.leftward = toLeft;
                    }
                    foe.wait = turn;
                    foe.y = foe.ground - (float) Math.sin(turn * Math.PI) * 2.2f;
                    foe.x += (foe.leftward ? -1 : 1) * SPEED[foe.kind] * seconds;
                } else if (what == 184 && doing) {
                    foe.x += way * 10 * seconds;
                    if (foe.x <= lo || foe.x >= hi) foe.act = 0;
                }
                break;
            }
            case RunLevels.GUARD:
            case RunLevels.BUTLER:
            case RunLevels.BYEORU:
            case RunLevels.JUPAN:
            case RunLevels.SUMBI:
            case RunLevels.PADS:
            case RunLevels.PARTY:
            case RunLevels.OLD_BOOK: {
                if (foe.step == 0) foe.leftward = toLeft;
                // Each has a way of going the length of the place, at a pace of her own; Sumbi walks up before she swings.
                float pace = what == 203 ? 4.5f : what == 212 ? 9 : what == 232 ? 10 : what == 252 || what == 262 ? 11 : 0;
                if (pace > 0 && doing) {
                    foe.x += way * pace * seconds;
                    if (foe.x <= lo || foe.x >= hi) foe.act = 0;
                } else if (what == 242 && foe.step == 0 && foe.act > .5f && away > 1.6f) {
                    foe.x += (toLeft ? -1 : 1) * 3.2f * seconds;
                } else if (what == 261 && doing && foe.fromX < 3 && (foe.wait -= seconds) <= 0) {
                    foe.wait = .33f;
                    shoot(BLADE, false, middle, floor - (foe.fromX == 1 ? 1.7f : .4f), toLeft ? -9 : 9, 0, 2.4f);
                    thrown++;
                    foe.fromX++;
                } else if (foe.step == 0 && pace == 0 && foe.kind >= RunLevels.BYEORU && foe.kind != RunLevels.SUMBI && foe.kind != RunLevels.PADS) {
                    // Those who throw keep their distance while they make ready.
                    float keeps = px + (toLeft ? 5 : -5) - foe.wide() / 2;
                    foe.x += Math.max(-1.6f * seconds, Math.min(1.6f * seconds, keeps - foe.x));
                }
                break;
            }
            default: {
                // The Mawang: on the wing for most of what she does, on the ground for her fire and her dash, and when she is spent.
                boolean swoop = what == 192 && doing, dash = what == 195 && doing;
                if (!swoop && !dash) foe.leftward = toLeft;
                boolean low = foe.step == 2 || foe.mode == 3 || foe.mode == 5;
                if (!swoop) foe.y += Math.max(-6 * seconds, Math.min(6 * seconds, (low ? foe.ground : foe.ground - (foe.mode == 4 ? 5 : 3)) - foe.y));
                if (foe.step == 0 && foe.mode != 5) {
                    float keeps = px + (toLeft ? 4 : -4) - foe.wide() / 2;
                    foe.x += Math.max(-3 * seconds, Math.min(3 * seconds, keeps - foe.x));
                }
                if (swoop) {
                    float across = foe.fromX - middle, down = foe.toX - foe.y, length = Math.max(.3f, (float) Math.hypot(across, down));
                    foe.x += across / length * 9 * seconds;
                    foe.y = Math.min(foe.ground, foe.y + down / length * 9 * seconds);
                    if (length < .4f || foe.y >= foe.ground) foe.act = 0;
                } else if (what == 193 && doing && foe.fromX < 3 && (foe.wait -= seconds) <= 0) {
                    // Low, to be jumped; high, to be stood under; and low again.
                    foe.wait = .33f;
                    shoot(DARK, false, middle, floor - (foe.fromX == 1 ? 1.7f : .4f), toLeft ? -8 : 8, 0, 2.4f);
                    thrown++;
                    foe.fromX++;
                } else if (dash) {
                    foe.x += way * 12 * seconds;
                    if (foe.x <= lo || foe.x >= hi) foe.act = 0;
                }
            }
        }
    }

    /** Yeobaek steps out of the white space behind her visitor, sword back. */
    private void appear(Foe foe, float px, float lo, float hi) {
        float side = facingLeft ? 1 : -1;
        foe.x = Math.max(lo, Math.min(hi, px + side * 1.7f - foe.wide() / 2));
        foe.leftward = px < foe.x + foe.wide() / 2;
        foe.y = foe.ground;
        foe.unseen = false;
    }

    /** What comes of a thing when it is over: where a leap comes down, the ground jumps or rubble falls. */
    private void done(Foe foe, int what, int phase, float px, float floor) {
        float middle = foe.x + foe.wide() / 2;
        if (what == 72 || what == 32 || what == 181 || what == 173 || what == 192) foe.y = foe.ground;
        if (what == 72) {
            shoot(SHOCK, false, middle, floor - .3f, -8, 0, 1.8f);
            shoot(SHOCK, false, middle, floor - .3f, 8, 0, 1.8f);
            quakes++;
            thrown++;
        } else if (what == 32) {
            int each = phase == 2 ? 2 : 1;
            for (int which = -each; which <= each; which++) shoot(DEBRIS, false, px + which * 2.2f, Math.max(.5f, floor - 8), 0, 0, 3);
            quakes++;
            thrown++;
        } else if (what == 181) {
            quake(Math.abs(px - middle), floor);
        }
    }
}
