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
    /**
     * And two ways the last land of all may stand that are neither: risen, when the white that is truly hers has formed and the
     * fight has left the ground, to be gone on with in the air (see {@link RunFlight}); and unmade, when the black ribbon lay on her
     * for good.
     */
    static final int RISEN = 3, UNMADE = 4;
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
    static final int CEILING = 32;
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
    /**
     * And what else is in such places: a key, which opens the locked door of the place it
     * lies in; a lever, which raises that place's bridge for good; a plate, which lifts its
     * bars for a few seconds every time it is stood on; and a shrine, which takes a heart
     * from whoever has more than one and ties a ribbon of its colour on her.
     */
    static final int KEY = 21, LEVER = 22, PLATE = 23, SHRINE = 24;
    /**
     * And in a building of a village: something to be walked up to, which stays where it
     * is. What it is is its fifth number: a pet on show, by its kind; or, from a hundred, a
     * desk at which the counter is opened, at one of its tabs.
     */
    static final int SPOT = 25;
    /** What the board of hunts in the guild hall is, as something to be walked up to. */
    static final int BOARD = 200;
    /** And the three who keep a smithy, each at her anvil: Moru, and the two after her. */
    static final int SMITH = 300;
    /** And whoever deals in the black market. */
    static final int DEALER = 400;
    /** And the machine of the ribbon gacha. */
    static final int MACHINE = 500;
    /** And Tulle, who keeps the vanity boutique that stands in Rosemarrow: a counter she is at. */
    static final int BOUTIQUE = 600;
    /** And the stairway down into a land's dungeon. */
    static final int STAIRS = 700;
    /**
     * A pot of black ink, which stands in the later lands and throws the black curse: how
     * often, for how long it swells before it does, from how far off it throws, and how
     * fast what it throws flies.
     */
    static final int INKPOT = 26;
    /** A friend, standing in a land of the third volume to pledge her help: which of them is her fifth number. She is walked up to. */
    static final int PLEDGE = 27;
    /**
     * In homes: a diary page, which is kept and read; and a loose floorboard, which comes up when it is stood on with the down button held,
     * and what was under it is a chest (its fifth number is what is in it).
     */
    static final int DIARY = 28, LOOSE = 29;
    private int diaries, lastDiary = -1, lifted;

    int diaries() { return diaries; }

    int lastDiary() { return lastDiary; }

    int lifted() { return lifted; }

    /** What the last chest opened held. */
    private int lastChest;

    int lastChest() { return lastChest; }
    private int pledges, lastPledge = -1;
    private boolean pledged;

    /** How many times a friend has been walked up to, a count that only goes up; and which was the last. */
    int pledges() { return pledges; }
    int lastPledge() { return lastPledge; }
    static final float POT_EVERY = 4.5f, POT_SWELLS = .9f, POT_REACH = 9, POT_FAST = 6.5f;

    /** How many times a pot has thrown: a count that only goes up. */
    int potShots() { return potShots; }
    private int potShots;

    /** How far through its turn a pot of black ink is, from nothing to one: it swells through the last of it. */
    float potTurn(int[] pot) { return (time + pot[4] * .37f) % POT_EVERY / POT_EVERY; }

    /**
     * The ribbon gacha. A roll gives, by chance, a ribbon of one of the colours the machine
     * holds, or a black one. Inside its room every ribbon's time stands still, and runs
     * again when she leaves. One she has had from the machine already in this land is not
     * given again: she has pluses for it instead; and after two such, the next is always
     * one she has not. Every colour the machine holds, in one land, is a white ribbon: all
     * six at once, for a while, and not mastered. A black one blows the machine up: it is
     * shut for the rest of the land, every ribbon she wears is gone, and black lies on her
     * for twice a ribbon's time from the moment she leaves.
     */
    static final int GACHA_COLOUR = 10, GACHA_BLACK = 6, GACHA_FIRST = 20, GACHA_MORE = 10, GACHA_PITY = 2;
    private int gachaHeld, gachaRolls, gachaDupes, gachaWhites;
    private boolean gachaBlown;

    /** Whether she is in the room of a ribbon gacha, where ribbons' times stand still. */
    boolean frozen() { return room >= 0 && level.rooms.get(room)[0] == RunLevels.HOUSE && level.rooms.get(room)[14] == RunLevels.GACHA; }

    /** What the next roll costs, which rises with every roll in a land; what she has had from the machine, as bits; how many she had already; whether it has blown up; and whether she has had all it holds. */
    int gachaCost() { return GACHA_FIRST + GACHA_MORE * gachaRolls; }
    int gachaHeld() { return gachaHeld; }
    int gachaRolls() { return gachaRolls; }
    int gachaDupes() { return gachaDupes; }
    int gachaWhites() { return gachaWhites; }
    boolean gachaBlown() { return gachaBlown; }
    boolean gachaDone() {
        for (int colour : RunLevels.machineOf(level.number)) if ((gachaHeld >> colour - 1 & 1) == 0) return false;
        return true;
    }

    /** Whether the machine can be rolled: she stands at it, it is whole, it has something left that she has not had, and no black lies on her. */
    boolean canRoll() { return spot == MACHINE && !gachaBlown && !gachaDone() && cursed <= 0 && state == PLAYING; }

    /** How likely, in a hundred, a roll is to be black. */
    static int blackOdds(int number) { return Math.round(100f * GACHA_BLACK / (GACHA_COLOUR * RunLevels.machineOf(number).length + GACHA_BLACK)); }

    /**
     * A roll of the machine, by a chance that whoever shows the game supplies. Returns
     * the colour of the ribbon it gave, which is in her hair; less than nothing, a colour she
     * had already, for which she is given pluses; black, if it blew up; or nothing, if it
     * could not be rolled.
     */
    int roll(int chance) {
        if (!canRoll()) return 0;
        int[] holds = RunLevels.machineOf(level.number);
        int pick = Math.floorMod(chance, GACHA_COLOUR * holds.length + GACHA_BLACK);
        gachaRolls++;
        if (pick >= GACHA_COLOUR * holds.length) {
            gachaBlown = true;
            gachaHeld = 0;
            curse(blackened());
            return BLACK;
        }
        int at = pick / GACHA_COLOUR, colour = holds[at];
        boolean had = (gachaHeld >> colour - 1 & 1) != 0;
        if (had && gachaDupes >= GACHA_PITY) {
            // Two she had already are enough: this one is the next she has not.
            while ((gachaHeld >> holds[at] - 1 & 1) != 0) at = (at + 1) % holds.length;
            colour = holds[at];
            had = false;
        }
        if (had) {
            gachaDupes++;
            return -colour;
        }
        gachaHeld |= 1 << colour - 1;
        take(FORM + colour);
        if (gachaDone()) {
            take(FORM + WHITE);
            gachaWhites++;
        }
        return colour;
    }
    private int spot = -1;

    /** What she stands at in a building, or -1: a pet on show, by its kind; or a desk, a hundred and the tab of the counter it opens. */
    int spot() { return spot; }
    static final float BARS_OPEN = 6;
    private int unlocked, raised, barsRoom = -1, keys, levers, plates, shrines;
    private float bars;
    private final java.util.Set<Integer> pulled = new java.util.HashSet<>();

    int keys() { return keys; }
    int levers() { return levers; }
    int plates() { return plates; }
    int shrines() { return shrines; }
    /** For how much longer the bars of the place she is in are lifted. */
    float barsLeft() { return bars; }
    boolean pulled(int column) { return pulled.contains(column); }
    /** A square of the land as it was laid, whatever has become of it since. */
    char laid(int column, int row) {
        return column < 0 || column >= columns || row < 0 || row >= level.land.length ? RunLevels.AIR : level.land[row][column];
    }

    /** Which place off the land a column is in, or -1. */
    private int placeOf(int column) {
        for (int index = 0; index < level.rooms.size(); index++) if (column >= level.rooms.get(index)[3] && column <= level.rooms.get(index)[4]) return index;
        return -1;
    }
    /**
     * A pet that lies waiting in a land or a cellar to be taken along: this and its kind.
     * Taken, it keeps beside her for the whole of the land, through doors and a life lost,
     * and goes at whatever is near her by itself, each kind in its own way. An ink jar
     * floats, and spits a blot at the nearest from a distance; what the blot does not deal
     * with stands dazed. A cat pounces on what is close, often. A dog charges along the
     * ground and knocks over all it runs through. A duck flies, drops on what is in the
     * air before anything else, and with every third drop its quack dazes all that is
     * near. A monkey lobs nuts, and fetches her the pluses and the ribbons it passes. What
     * a pet deals with counts as hers. Nothing hurts a pet. Against one of those who wait
     * a pet only helps: it takes half a piece off a shield and never the last of it, it
     * lands a blow on one whose shield is broken, and it never lands the last blow.
     *
     * And five that are small stuffed copies of people she knows. A mini Chikuwa runs,
     * throws bread rolls, and now and then, while she is short of a heart, hands her a roll
     * that gives half of one back. A mini Rune runs, cries ink: a puddle where she has been
     * that slows whatever walks through it, and a flick of ink at what is close; with one of
     * those who wait near, she keeps on the far side of Halgakos from her, and flicks all
     * the same. A mini Halgakos with two black wings flies, and swoops at the nearest again
     * and again, from close. One with two white wings flies, sends a small ray of light a
     * long way on a slow beat, and every so often has a ring of light ready that turns
     * aside one thing thrown at Halgakos. And one with no wings runs and copies her: when
     * she uses what she holds, it uses a small one of the same, and it does nothing else.
     *
     * And four more, small stuffed copies of those she has beaten, to be had only once she has
     * (see {@link RunPets}). A mini Tsubakieh runs up to the nearest and swings her bat: all
     * within the swing are hit, and what stands after it stands knocked silly. A mini Illya
     * runs, and throws a fan of three sparkles from her quill. A mini Mawang flies, and breathes
     * a short dark flame, three tongues of it, at what is close. A mini Lucy runs, and squirts
     * a quick, weak jet of water from further off than the rest.
     */
    static final int PET = 30, JAR = 0, CAT = 1, DOG = 2, DUCK = 3, MONKEY = 4;
    static final int MINI_CHIKUWA = 5, MINI_RUNE = 6, IMP = 7, CHERUB = 8, TWIN = 9;
    static final int MINI_TSUBAKIEH = 10, MINI_ILLYA = 11, MINI_MAWANG = 12, MINI_LUCY = 13, PETS = 14;
    /** What a pet throws: a blot of ink, a nut, a bread roll, and a ray of light. */
    static final int BLOT_SHOT = 23, NUT = 24, ROLL = 25, RAY = 26;
    /** How often a mini Chikuwa has a roll for her, how long a puddle of ink lies, how much it slows, and how often the ring of light is ready. */
    static final float GIFT_EVERY = 18, PUDDLE_LASTS = 5, PUDDLE_SLOWS = .45f, WARD_EVERY = 9;
    private static final float[] PET_EVERY = {1.6f, 1, 2.2f, 1.5f, 1.8f, 1.7f, 1.3f, .7f, 2.4f, .35f, 2, 2.2f, 2.6f, 1.2f},
            PET_NEAR = {7, 3.2f, 6, 7, 6, 6.5f, 3.5f, 3.6f, 9, 0, 4, 7, 4, 7.5f};
    /** How long after one pet begins on something before another may. */
    private static final float PET_TURN = .22f;

    /**
     * One pet that is with her: its kind; whether it is hers for good and came into the
     * land with her, or is a stray found in this land; where its feet are and which way it
     * faces; how long until it next does what it does, and how much longer it is busy at
     * it; and what its kind keeps count of.
     */
    static final class Pet {
        final int kind;
        boolean stays, left;
        float x, y, wait = 1, busy, fromX, fromY, toX, toY, down, gift = GIFT_EVERY, ward = WARD_EVERY, puddle;
        /** Whether it climbs after her up what she climbs, and how far it has climbed (for the step of its climbing it is at). */
        boolean climbing;
        float climbed;
        /** Where whoever draws it last saw it. */
        float seenX, seenY;
        Foe at;
        int drops;

        Pet(int kind) { this.kind = kind; }
    }

    /** Her pets, those that are hers for good first: as many of each sort as she has room for. */
    private final List<Pet> herd = new ArrayList<>();
    private int petKills, petJoined, petStrikes, petGifts, petWards, petCopies, lastJoined = -1, petSwaps;
    private float sincePetAct = 9;
    /** A pet lying where she stands that she has no room for: she may change one of her strays for it. */
    private int[] offered;

    List<Pet> herd() { return herd; }
    /** How many of her pets are strays found in this land. */
    int strays() {
        int strays = 0;
        for (Pet it : herd) if (!it.stays) strays++;
        return strays;
    }
    int lastJoined() { return lastJoined; }
    int petSwaps() { return petSwaps; }
    /** The kind of pet she stands at and has no room for, or -1. */
    int petOffer() { return offered == null ? -1 : offered[0] - PET; }

    /** The stray that has been with her longest gives up its place to the one she stands at. Returns whether there was one to change for. */
    boolean swapPet() {
        if (offered == null) return false;
        for (int index = 0; index < herd.size(); index++) {
            if (herd.get(index).stays) continue;
            herd.remove(index);
            break;
        }
        items.remove(offered);
        Pet joined = join(offered[0] - PET, false);
        joined.x = offered[1] + .5f;
        joined.y = offered[2] + 1;
        offered = null;
        petSwaps++;
        return true;
    }

    private Pet join(int kind, boolean stays) {
        Pet it = new Pet(kind);
        it.stays = stays;
        it.x = x + WIDE / 2;
        it.y = y + TALL;
        // (Those that are hers for good go before the strays.)
        int at = herd.size();
        if (stays) for (at = 0; at < herd.size() && herd.get(at).stays; ) at++;
        herd.add(at, it);
        if (!stays) {
            petJoined++;
            lastJoined = kind;
        }
        return it;
    }
    /** The puddles a mini Rune has left: where each lies, the row its top is in, and how much longer it lies. */
    private final List<float[]> puddles = new ArrayList<>();

    List<float[]> puddles() { return puddles; }
    int petGifts() { return petGifts; }
    int petWards() { return petWards; }
    int petCopies() { return petCopies; }
    /** Whether the ring of light is ready. */
    boolean warded() {
        for (Pet it : herd) if (it.kind == CHERUB && it.ward <= 0) return true;
        return false;
    }

    /** Which pet is with her, or -1; where its feet are; which way it faces; how much longer it is busy with what it is at; and counts that only go up. */
    /** Where the last thing she took lay: the middle of its square. */
    float tookX() { return tookX; }
    float tookY() { return tookY; }
    private float tookX, tookY;

    /** The first of her pets: its kind, or -1 if she has none; where its feet are; which way it faces; and how much longer it is busy. */
    int pet() { return herd.isEmpty() ? -1 : herd.get(0).kind; }
    float petX() { return herd.isEmpty() ? 0 : herd.get(0).x; }
    float petY() { return herd.isEmpty() ? 0 : herd.get(0).y; }
    boolean petLeftward() { return !herd.isEmpty() && herd.get(0).left; }
    float petBusy() { return herd.isEmpty() ? 0 : herd.get(0).busy; }
    int petKills() { return petKills; }
    int petJoined() { return petJoined; }
    int petStrikes() { return petStrikes; }
    static boolean floats(int pet) { return pet == JAR || pet == DUCK || pet == IMP || pet == CHERUB || pet == MINI_MAWANG; }

    /** A pet is hers, there and then: for looking at one, and for the tests. */
    void adopt(int kind) {
        // (With no room for another stray, the one that has been with her longest makes way.)
        if (strays() >= perks.tempSlots) {
            for (int index = 0; index < herd.size(); index++) {
                if (herd.get(index).stays) continue;
                herd.remove(index);
                break;
            }
        }
        join(kind, false);
    }

    /** Whether something stands in a puddle of ink: it does everything at less than half its pace while it does. */
    boolean mired(Foe foe) {
        if (puddles.isEmpty() || RunLevels.boss(foe.kind) || RunLevels.flier(foe.kind)) return false;
        float middle = foe.x + foe.wide() / 2, feet = foe.y + foe.tall();
        for (float[] puddle : puddles) if (Math.abs(puddle[0] - middle) < .9f && Math.abs(puddle[1] - feet) < .4f) return true;
        return false;
    }

    /** The one with no wings does as she does: a small one of what she throws, from where it is; or a swing at what is in front of it. */
    private void copied() {
        if (weapon == UNARMED) return;
        for (int each = 0; each < herd.size(); each++) {
            Pet it = herd.get(each);
            if (it.kind != TWIN || it.wait > 0) continue;
            it.wait = PET_EVERY[TWIN];
            petCopies++;
            petStrikes++;
            it.left = facingLeft;
            float way = facingLeft ? -1 : 1;
            if (weapon == TABLET || weapon == KEYS) {
                Shot shot = shoot(weapon == TABLET ? PAGE : THROWN_KEY, true, it.x + way * .3f, it.y - .45f, way * 14, 0, .4f);
                shot.pets = true;
                if (weapon == TABLET) shot.spin = way * 14;
                continue;
            }
            if (weapon == PEN || weapon == KNIFE || weapon == GUN || weapon == BOOK) {
                Shot shot = weapon == BOOK ? shoot(BOOK, true, it.x + way * .3f, it.y - .7f, way * 6, -9, 2.5f) : shoot(weapon, true, it.x + way * .3f, it.y - .45f, way * 14, 0, .55f);
                shot.pets = true;
                continue;
            }
            it.busy = .18f;
            it.fromX = it.toX = it.x;
            it.fromY = it.toY = it.y;
            it.at = null;
            for (int index = 0; index < foes.size(); index++) {
                Foe foe = foes.get(index);
                float across = (foe.x + foe.wide() / 2 - it.x) * way;
                if (across > -.3f && across < 1.4f && Math.abs(foe.y + foe.tall() - it.y) < 1.3f) petted(foe);
            }
        }
    }

    /** What a pet does to something: it deals with a sign as a blow would; one of those who wait it only helps against. Returns whether anything came of it. */
    private boolean petted(Foe foe) {
        if (foe.flat || absent(foe) || foe.kind == RunLevels.TARGET || foe.kind == RunLevels.WISP || foe.kind == RunLevels.RIME) return false;
        if (RunLevels.boss(foe.kind)) {
            if (!open(foe)) {
                // Half a piece off her shield, and never the last of it.
                if (foe.forming > 0 || foe.shield <= .5f) return false;
                foe.shield = Math.max(.5f, foe.shield - (perks.petStrong >= 2 ? 1 : .5f));
                cracks++;
                foeX = foe.x + foe.wide() / 2;
                foeY = foe.y + foe.tall();
                return true;
            }
            if (foe.left <= 1 || foe.dazed > 0) return false;
        }
        boolean was = foe.flat;
        // (From stronger pets a blow on a big one counts twice.)
        if (perks.petStrong >= 1 && !RunLevels.boss(foe.kind) && foe.left > BLOW) foe.left -= BLOW;
        beat(foe, SURE);
        if (foe.flat && !was) petKills++;
        return true;
    }

    /** Her pets for a step: each keeps its place by her, and they go at things in turn. */
    private void pets(float seconds) {
        sincePetAct += seconds;
        // A puddle dries.
        for (int index = puddles.size() - 1; index >= 0; index--) if ((puddles.get(index)[2] -= seconds) <= 0) puddles.remove(index);
        int floating = 0, running = 0;
        for (int index = 0; index < herd.size(); index++) {
            Pet it = herd.get(index);
            tend(it, seconds, floats(it.kind) ? floating++ : running++);
        }
    }

    /**
     * One pet for a step: it keeps by her, in its place among the others of its kind, and
     * when it has rested and it is its turn it goes at something. Those that run stand in a
     * line behind her, one after another; those that float hang behind her shoulder, each
     * a little further back than the last and by turns higher and lower, so that none is
     * over her or over another.
     */
    private void tend(Pet it, float seconds, int place) {
        int kind = it.kind;
        float herX = x + WIDE / 2, herFeet = y + TALL;
        boolean floats = floats(kind);
        // Left too far behind, or under the land, it is beside her again.
        if (Math.abs(it.x - herX) > 12 || Math.abs(it.y - herFeet) > 9) {
            it.x = herX + (facingLeft ? .9f : -.9f);
            it.y = herFeet - (floats ? 1.2f : 0);
            it.busy = 0;
            it.at = null;
            it.down = 0;
        }
        // A mini Rune on the ground leaves a puddle where she is, every so often.
        if (kind == MINI_RUNE && (it.puddle -= seconds) <= 0 && it.busy <= 0 && solid(at((int) Math.floor(it.x), (int) Math.floor(it.y + .05f)))) {
            it.puddle = 1.1f;
            puddles.add(new float[]{it.x, it.y, PUDDLE_LASTS});
        }
        // A mini Chikuwa has a roll for her every so often while she is short of a heart: half of one back.
        if (kind == MINI_CHIKUWA && hp < maxHp() && state == PLAYING && (it.gift -= seconds) <= 0) {
            it.gift = GIFT_EVERY;
            hp = Math.min(maxHp(), hp + HALF);
            petGifts++;
        }
        // The ring of light: when it is ready, the first thing thrown at her that comes near is turned aside.
        if (kind == CHERUB && (it.ward -= seconds) <= 0) {
            for (int index = shots.size() - 1; index >= 0; index--) {
                Shot shot = shots.get(index);
                if (shot.hers || shot.kind == PLUSDROP || Math.hypot(shot.x - herX, shot.y - y - TALL / 2) > 1.5f) continue;
                shots.remove(index);
                it.ward = WARD_EVERY;
                petWards++;
                break;
            }
        }
        if (it.busy > 0) {
            // On its way to what it is going at; and there, it deals with it.
            it.busy -= seconds;
            float through = 1 - Math.max(0, it.busy) / .28f;
            if (it.at != null) {
                it.toX = it.at.x + it.at.wide() / 2;
                it.toY = it.at.y + it.at.tall() * (kind == DUCK ? .2f : 1);
            }
            it.x = it.fromX + (it.toX - it.fromX) * through;
            it.y = it.fromY + (it.toY - it.fromY) * through - (float) Math.sin(through * Math.PI) * (kind == DOG ? 0 : .8f);
            if (kind == DOG) {
                // Whatever it runs through on its way goes over.
                for (int index = 0; index < foes.size(); index++) {
                    Foe foe = foes.get(index);
                    if (!RunLevels.boss(foe.kind) && Math.abs(foe.x + foe.wide() / 2 - it.x) < .7f && Math.abs(foe.y + foe.tall() - it.y) < 1.2f) petted(foe);
                }
            }
            if (it.busy <= 0) {
                if (kind == MINI_TSUBAKIEH) {
                    // Her swing: all within it are hit, and what stands after it stands knocked silly.
                    for (int index = 0; index < foes.size(); index++) {
                        Foe foe = foes.get(index);
                        if (foe == it.at || RunLevels.boss(foe.kind) || Math.abs(foe.x + foe.wide() / 2 - it.x) > 1.5f || Math.abs(foe.y + foe.tall() - it.y) > 1.4f) continue;
                        if (petted(foe) && !foe.flat) foe.dazed = Math.max(foe.dazed, 1.5f);
                    }
                    if (it.at != null && petted(it.at) && !it.at.flat && !RunLevels.boss(it.at.kind)) it.at.dazed = Math.max(it.at.dazed, 1.5f);
                    it.at = null;
                }
                if (it.at != null && kind != DOG) petted(it.at);
                if (it.at != null && kind == DOG && RunLevels.boss(it.at.kind)) petted(it.at);
                if (kind == DUCK && ++it.drops % 3 == 0) {
                    // Its quack: whatever is near stands dazed.
                    for (Foe foe : foes) {
                        if (!foe.flat && !RunLevels.boss(foe.kind) && Math.abs(foe.x - it.x) < 3 && Math.abs(foe.y - it.y) < 3) foe.dazed = Math.max(foe.dazed, 1.2f);
                    }
                }
                it.at = null;
            }
            return;
        }
        // While she climbs, one that runs climbs after her, on what she climbs, just under her feet (one under another); one that flies flies.
        // When she lets go it lets go too, and drops back to following her.
        if (!floats && climbing) {
            float wantY = herFeet + 1.05f + place * 1.1f, before = it.y;
            it.x += (herX - it.x) * Math.min(1, 10 * seconds);
            it.y += (wantY - it.y) * Math.min(1, 10 * seconds);
            it.climbed += Math.abs(it.y - before);
            it.left = facingLeft;
            it.down = 0;
            it.climbing = true;
            return;
        }
        it.climbing = false;
        // By her: a floater in the air at her shoulder, behind her; one that runs, on the ground behind her, down onto it and up onto it as she goes.
        float wantX = herX + (facingLeft ? 1 : -1) * (floats ? .8f + .75f * place : 1.1f + .85f * place);
        if (kind == MONKEY) {
            // A monkey goes after the nearest plus or ribbon within a little way of her, and comes back.
            float nearest = 5.5f;
            for (int[] plus : pluses) {
                float away = Math.abs(plus[0] + .5f - herX);
                if (away < nearest && Math.abs(plus[1] + .5f - herFeet) < 3) {
                    nearest = away;
                    wantX = plus[0] + .5f;
                }
            }
            for (int[] item : items) {
                float away = Math.abs(item[1] + .5f - herX);
                if (cursed <= 0 && item[0] > FORM && item[0] < FORM + BLACK && item[3] == 0 && away < nearest && Math.abs(item[2] + .5f - herFeet) < 3) {
                    nearest = away;
                    wantX = item[1] + .5f;
                }
            }
        }
        if (kind == MINI_RUNE) {
            for (Foe foe : foes) {
                if (!RunLevels.boss(foe.kind) || foe.flat || absent(foe) || Math.abs(foe.x + foe.wide() / 2 - herX) > 10) continue;
                wantX = herX + (foe.x + foe.wide() / 2 < herX ? 1.1f : -1.1f);
            }
        }
        float gap = wantX - it.x;
        it.x += Math.signum(gap) * Math.min(Math.abs(gap), (Math.abs(gap) > 3 ? 13 : 8) * seconds);
        if (Math.abs(gap) > .1f) it.left = gap < 0;
        else it.left = facingLeft;
        if (floats) {
            float wantY = herFeet - 1.25f - (place % 2) * .55f + (float) Math.sin(time * 3 + place) * .12f;
            it.y += (wantY - it.y) * Math.min(1, 9 * seconds);
        } else {
            int column = (int) Math.floor(it.x), floor = -1;
            for (int row = (int) Math.floor(herFeet) - 3; row <= (int) Math.floor(herFeet) + 6 && floor < 0; row++) {
                char here = at(column, row);
                if ((firm(here) || here == RunLevels.THIN || here == RunLevels.LEDGE) && !solid(at(column, row - 1))) floor = row;
            }
            if (floor < 0 || !onGround && it.y >= herFeet - .01f && it.down >= 0 && herFeet < it.y - .6f) {
                // Nothing under it, or she is in the air: it jumps as she does.
                it.y += (herFeet - it.y) * Math.min(1, 7 * seconds);
                it.down = 0;
            } else if (it.y < floor) {
                it.down += GRAVITY * seconds;
                it.y = Math.min(floor, it.y + it.down * seconds);
                if (it.y >= floor) it.down = 0;
            } else {
                // Up onto where she has gone.
                it.y = Math.max(floor, it.y - 14 * seconds);
                it.down = 0;
            }
        }
        // And a monkey fetches: the pluses it passes are hers, and a ribbon near it is brought to her.
        if (kind == MONKEY) {
            for (int index = pluses.size() - 1; index >= 0; index--) {
                int[] plus = pluses.get(index);
                if (Math.hypot(plus[0] + .5f - it.x, plus[1] + .5f - it.y + .4f) > 1.8f) continue;
                pluses.remove(index);
                collected++;
                plusX = plus[0] + .5f;
                plusY = plus[1] + .5f;
            }
            for (int index = items.size() - 1; index >= 0 && cursed <= 0; index--) {
                int[] item = items.get(index);
                if (item[0] <= FORM || item[0] >= FORM + BLACK || item[3] != 0 || Math.hypot(item[1] + .5f - it.x, item[2] + .5f - it.y + .4f) > 1.8f) continue;
                items.remove(index);
                taken++;
                lastTaken = item[0];
                take(item[0]);
            }
        }
        // (They take turns: none begins on something within a moment of another's beginning.)
        if ((it.wait -= seconds) > 0 || kind == TWIN || sincePetAct < PET_TURN) return;
        // Rested, it looks for something near her to go at: a duck, what is in the air first; the rest, whatever is nearest it.
        Foe best = null;
        float nearest = PET_NEAR[kind];
        boolean flying = false;
        for (Foe foe : foes) {
            if (foe.flat || absent(foe) || foe.kind == RunLevels.TARGET || foe.kind == RunLevels.WISP || foe.kind == RunLevels.RIME) continue;
            if (RunLevels.boss(foe.kind) && (open(foe) ? foe.left <= 1 || foe.dazed > 0 : foe.forming > 0 || foe.shield <= .5f)) continue;
            boolean own = kind == CAT || kind == DOG || kind == MINI_RUNE;
            float across = foe.x + foe.wide() / 2 - (own ? it.x : herX), down = foe.y + foe.tall() - (own ? it.y : herFeet);
            float away = (float) Math.hypot(across, down);
            boolean air = RunLevels.flier(foe.kind) || foe.kind == RunLevels.DRAGON || foe.y + foe.tall() < foe.ground + foe.tall() - 1;
            if (kind == DOG && Math.abs(down) > 1.5f) continue;
            if (away > PET_NEAR[kind]) continue;
            if (kind == DUCK && air != flying ? air : away < nearest) {
                best = foe;
                nearest = away;
                flying = air;
            }
        }
        if (best == null) return;
        it.wait = PET_EVERY[kind] * perks.petBrisk * Math.max(.4f, 1 - Math.max(0, skillOf(RunSkills.E_PET)) / 100f);
        sincePetAct = 0;
        petStrikes++;
        it.left = best.x + best.wide() / 2 < it.x;
        if (kind == MINI_ILLYA || kind == MINI_MAWANG || kind == MINI_LUCY) {
            // A fan of three sparkles; three short tongues of dark flame; one quick jet of water. Each from where it is, at what it chose.
            float toX = best.x + best.wide() / 2, toY = best.y + best.tall() / 2;
            float length = Math.max(.5f, (float) Math.hypot(toX - it.x, toY - it.y + .5f));
            float speed = kind == MINI_LUCY ? 16 : kind == MINI_MAWANG ? 10 : 13, aim = (float) Math.atan2(toY - it.y + .5f, toX - it.x);
            int many = kind == MINI_LUCY ? 1 : 3;
            for (int one = 0; one < many; one++) {
                float turn = aim + (one - (many - 1) / 2f) * (kind == MINI_MAWANG ? .22f : .3f);
                Shot shot = shoot(kind == MINI_ILLYA ? RAY : kind == MINI_MAWANG ? FIRE : WATER, true, it.x, it.y - .5f, (float) Math.cos(turn) * speed, (float) Math.sin(turn) * speed,
                        kind == MINI_MAWANG ? .42f : kind == MINI_LUCY ? .5f : .55f);
                shot.pets = true;
            }
            if (length < 0) return;
            return;
        }
        if (kind == JAR || kind == MONKEY || kind == MINI_CHIKUWA || kind == MINI_RUNE || kind == CHERUB) {
            // From where it is: a blot or a flick of ink, straight; a nut or a roll, lobbed; a ray, straight and fast.
            float toX = best.x + best.wide() / 2, toY = best.y + best.tall() / 2;
            Shot shot;
            if (kind == JAR || kind == MINI_RUNE || kind == CHERUB) {
                float length = Math.max(.5f, (float) Math.hypot(toX - it.x, toY - it.y + .5f)), speed = kind == CHERUB ? 17 : 11;
                shot = shoot(kind == CHERUB ? RAY : BLOT_SHOT, true, it.x, it.y - .5f, (toX - it.x) / length * speed, (toY - it.y + .5f) / length * speed, kind == JAR ? 1.2f : kind == CHERUB ? .7f : .45f);
            } else {
                shot = shoot(kind == MONKEY ? NUT : ROLL, true, it.x, it.y - .6f, Math.max(-10, Math.min(10, (toX - it.x) / .7f)), -9, 2.5f);
            }
            shot.pets = true;
            return;
        }
        it.busy = .28f;
        it.fromX = it.x;
        it.fromY = it.y;
        it.at = best;
        it.toX = best.x + best.wide() / 2;
        it.toY = best.y + best.tall();
        if (kind == DOG) {
            // Its charge is a line along the ground, on past what it goes at.
            it.at = RunLevels.boss(best.kind) ? best : null;
            it.toX = it.x + Math.signum(it.toX - it.x) * 6;
            it.toY = it.y;
        }
    }
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
        /**
         * Whether she is whole: every colour hers to keep, and every friend met on the way to the last land pledged to her. It is
         * what the black ribbon thrown there is turned by.
         */
        boolean whole;
        /** Whether the white that is truly hers has been won, in the last land: from then on it is hers to call on in any land. */
        boolean white;
        /** Whether black on her lasts no longer than a ribbon does, and not twice as long: what the Mawang gave. */
        boolean resists;
        /** How many of the colours that are hers she may wear at once. */
        int together = 2;
        /**
         * Her backpack: the things to fight with that she has found, as bits by their
         * numbers in {@link RunArms}; the one she begins every land with, or none; and how
         * far the smith has worked on each.
         */
        int pack;
        /**
         * Her pets: which of those that are hers for good come into the land with her, as
         * bits by their kinds; how many strays found in a land may follow her besides; how
         * much of their usual wait her pets take between one thing and the next; and how
         * much stronger they are (one: a blow counts twice on a big one; two: and takes a
         * whole piece off a shield).
         */
        int petWith, tempSlots = 1, petStrong;
        float petBrisk = 1;
        int main = UNARMED;
        int[] levels = new int[RunArms.COUNT];
        /**
         * How much harder she has chosen to have it, at the black market: so many more
         * blows to every creature and halves of a heart to every hurt. And which rung of the
         * black arena this is, or none: whoever waits there takes more, behind more of a
         * shield, and a hurt costs more.
         */
        int harder, black;
        /**
         * What she wears: how many halves of a heart it takes off what a hurt costs, never
         * the last of it; how many more blows her coming down on a creature counts for; and
         * how many times as long nothing hurts her again after a hurt.
         */
        int defence, stomp;
        /** Her level: how much harder her blows land comes from it. */
        int level = 1;
        /** The weapon found in the lands that she holds as her own, or 0: what it is counts for more in her hands. */
        int item;
        float mercy = 1;
        /** What her skill tree gives her; and the active skills in her slots. */
        RunSkills.Built skills = new RunSkills.Built();
        List<RunSkills.Notable> slotted = new ArrayList<>();
        /** What she wears of what she has found: an armour and two accessories, added up. */
        RunLoot.Worn worn = RunLoot.NOTHING;
        /**
         * Moves bought at the guild counter: a third jump, an air dash (and whether it strikes what it goes through), a ground pound, a
         * dodge roll, a wall jump, a glide, a second wind once a land; and blows of thorns for whatever hurts her.
         */
        boolean triple, airDash, dashStrikes, pound, roll, wallJump, glide, secondWind;
        int thorns;
        /** In a dungeon, how much harder her blows land, in percent, from the blessings chosen on the way down. */
        int mightPct;
    }

    // How much creatures take

    /** A blow of her first book at her first level, in the tenths creatures' health is counted in. */
    static final int BLOW = 10;
    /** Each kind's own health: the minus a blow, the times a little more, a mite less, the big ones more. (Those who wait count blows of their own.) */
    private static final int[] HEALTH = new int[RunLevels.RIME + 1];

    static {
        java.util.Arrays.fill(HEALTH, BLOW);
        HEALTH[RunLevels.DIVIDE] = 8;
        HEALTH[RunLevels.THROWER] = 9;
        HEALTH[RunLevels.SLIME] = 12;
        HEALTH[RunLevels.DRAGON] = 18;
        HEALTH[RunLevels.BAR] = 16;
        HEALTH[RunLevels.FAN] = 9;
        HEALTH[RunLevels.POGO] = 9;
        HEALTH[RunLevels.DRIFTER] = 9;
        HEALTH[RunLevels.SWOOPER] = 8;
        HEALTH[RunLevels.BOMBER] = 12;
        HEALTH[RunLevels.MITE] = 6;
    }

    static int healthOf(int kind) { return kind >= 0 && kind < HEALTH.length ? HEALTH[kind] : BLOW; }

    /** The colours of toughness: their names, how many times the health each has, the halves of a heart more its touch costs, and how many times the experience and the chance of a drop. */
    static final int NORMAL = 0, RED_TIER = 1, PURPLE_TIER = 2, GOLD_TIER = 3, INK_TIER = 4, TIERS = 5;
    static final String[] TIER_NAMES = {"", "Red", "Purple", "Gold", "Black Ink"};
    static final int[] TIER_HEALTH = {1, 3, 7, 15, 30}, TIER_BITE = {0, 0, 1, 1, 2}, TIER_XP = {1, 2, 4, 7, 12};
    static final float[] TIER_DROPS = {1, 1.5f, 2.5f, 4, 6};

    /** In ten thousand creatures of a land, how many are red, purple, gold and black ink: more, and tougher, the further on. */
    static int[] tierOdds(int number) {
        if (number <= 6) return new int[]{500, 0, 0, 0};
        if (number <= 12) return new int[]{1000, 200, 0, 0};
        if (number <= 18) return new int[]{1300, 400, 0, 0};
        if (number <= 30) return new int[]{1500, 600, 0, 0};
        if (number <= 50) return new int[]{1800, 800, number > 40 ? 150 : 0, 0};
        if (number <= 80) return new int[]{2000, 1100, 400, 50};
        return new int[]{2000, 1400, 800, 250};
    }

    /** A creature's colour, the same every time for the same one in the same land. */
    static int tierOf(int number, int kind, int column, int row) {
        if (RunLevels.boss(kind) || kind == RunLevels.TARGET || kind == RunLevels.WISP || kind == RunLevels.RIME) return NORMAL;
        long dice = number * 7919L + column * 104729L + row * 1299709L + kind * 15485863L;
        dice = dice * 6364136223846793005L + 1442695040888963407L;
        int roll = (int) ((dice >>> 33) % 10000), sum = 0;
        int[] odds = tierOdds(number);
        for (int tier = odds.length; tier >= 1; tier--) {
            sum += odds[tier - 1];
            if (roll < sum) return tier;
        }
        return NORMAL;
    }

    /** How many of a swarm give what one of a land's own gives (experience, pluses, a chance of a drop); and what is gathered toward the next of it. */
    static final int SWARM_SHARE = 25;
    private float swarmXp;

    // Water

    /** How fast she swims; how long she can hold her breath; and how fast an ink bolt she shoots underwater goes. */
    static final float SWIM = 3.6f, HOLD_BREATH = 8, INK_BOLT = 9;
    private boolean inWater, swimming;
    private float breath = HOLD_BREATH, breathTick, swum;
    private int inkBolts;

    boolean inWater() { return inWater; }
    boolean swimming() { return swimming; }
    float breath() { return breath; }
    float swum() { return swum; }
    int inkBolts() { return inkBolts; }

    /** Underwater, a use of what she holds also sends a bolt of ink the way she faces. */
    private void inkBolt() {
        Shot shot = shoot(BLOT_SHOT, true, x + WIDE / 2 + (facingLeft ? -.4f : .4f), y + TALL * .5f, (facingLeft ? -1 : 1) * INK_BOLT, 0, .8f);
        shot.by = PLAIN;
        inkBolts++;
    }

    // Hideouts

    /** Hidden bosses beaten in this land, and the last of them; strong cracks struck too weakly. */
    private int hiddenBeaten, hiddenKind = -1;

    int hiddenBeaten() { return hiddenBeaten; }
    int hiddenKind() { return hiddenKind; }

    /** What a blow must count for to break the crack in front of a hideout: as a land's shields ask, less a part. */
    static int crackNeeds(int number) { return Math.round(shieldNeeds(Math.max(SHIELDS_FROM, number)) * .7f); }

    /** Whether a hidden door is a hideout's strong crack. */
    private boolean strongCrack(int[] one) { return one[0] == RunLevels.HIDEOUT && one[7] == 1; }

    // Paper trees

    /** Its paper trees: column, row, grade, and blows left (0 once down); paper cut, by grade; trees cut; and how long since one was struck, and where. */
    private final List<int[]> trees = new ArrayList<>();
    private final int[] paperCut = new int[RunCraft.GRADES];
    private int treesCut;
    private float treeStruck = 9;
    private int treeAt = -1;

    List<int[]> trees() { return trees; }
    int paperCut(int grade) { return paperCut[grade]; }
    int treesCut() { return treesCut; }
    float treeStruck() { return treeStruck; }
    int treeAt() { return treeAt; }

    /** A use of what she carries near a paper tree in front of her chops at it; enough chops and it comes down, and gives its paper. */
    private void chop() {
        float middle = x + WIDE / 2, reach = Math.max(1.5f, weapon == UNARMED ? 1.2f : Math.min(3, RunArms.reach(Math.max(0, weapon), perks.levels[Math.max(0, Math.min(perks.levels.length - 1, weapon))]) + .6f));
        for (int index = 0; index < trees.size(); index++) {
            int[] tree = trees.get(index);
            if (tree[3] <= 0) continue;
            float across = tree[0] + .5f - middle;
            if (Math.abs(across) > reach || across < 0 == !facingLeft && Math.abs(across) > .6f) continue;
            if (Math.abs(tree[1] + 1 - (y + TALL)) > 1.6f) continue;
            treeStruck = 0;
            treeAt = index;
            if (--tree[3] <= 0) {
                paperCut[tree[2]] += RunCraft.treeGives(tree[2]);
                treesCut++;
            }
            return;
        }
    }

    // The Mother Tongue's Quill

    /** How far its wave reaches; how many waves, and how long since the last. */
    static final float QUILL_REACH = 9;
    private int quillWaves;
    private float quillAge = 9;

    int quillWaves() { return quillWaves; }
    float quillAge() { return quillAge; }

    /** Its held attack: a wave of the first words, out from her, that every creature near her takes as a blow (one who waits, as one). */
    private void quillWave() {
        quillWaves++;
        quillAge = 0;
        float middle = x + WIDE / 2, mid = y + TALL / 2;
        for (Foe foe : foes) {
            if (foe.flat || absent(foe) || foe.kind == RunLevels.TARGET) continue;
            if (Math.abs(foe.x + foe.wide() / 2 - middle) > QUILL_REACH || Math.abs(foe.y + foe.tall() / 2 - mid) > 4) continue;
            beat(foe, SURE);
        }
    }

    // Moves bought at the counter

    /** How fast and how long an air dash and a dodge roll go; how slowly she slides down a wall and glides; how far a ground pound reaches. */
    static final float DASH_SPEED = 12, DASH_LASTS = .22f, ROLL_SPEED = 8.5f, ROLL_LASTS = .38f, WALL_SLIDE = 2, GLIDE_FALL = 1.5f, POUND_REACH = 2.4f, FLIP_LASTS = .45f;
    private float airDashLeft, rollLeft, flipAge = 9, poundAge = 9;
    private boolean dashUsed, windUsed, clinging, gliding, poundNow;
    private int wallSide, flips, airDashes, rolls, pounds, wallJumps, secondWinds, dashStruck;
    private final java.util.Set<Foe> dashHit = new java.util.HashSet<>();

    int flips() { return flips; }
    int airDashes() { return airDashes; }
    int rolls() { return rolls; }
    int pounds() { return pounds; }
    int wallJumps() { return wallJumps; }
    int secondWinds() { return secondWinds; }
    int dashStruck() { return dashStruck; }
    boolean clinging() { return clinging; }
    boolean gliding() { return gliding; }
    float poundAge() { return poundAge; }
    float rollThrough() { return rollLeft > 0 ? 1 - rollLeft / ROLL_LASTS : 0; }

    /** The picture of a move she is in the middle of, in her sheet of moves (flip 0 to 2, dash, pound, roll, wall, glide), or -1. */
    static final int NO_MOVE = -1, MOVE_FLIP = 0, MOVE_DASH = 3, MOVE_POUND = 4, MOVE_ROLL = 5, MOVE_WALL = 6, MOVE_GLIDE = 7;

    int moveShown() {
        if (rollLeft > 0) return MOVE_ROLL;
        if (airDashLeft > 0) return MOVE_DASH;
        if (onGround) return NO_MOVE;
        if (flipAge < FLIP_LASTS) return MOVE_FLIP + Math.min(2, (int) (flipAge / (FLIP_LASTS / 3)));
        if (clinging) return MOVE_WALL;
        if (plunged) return MOVE_POUND;
        if (gliding) return MOVE_GLIDE;
        return NO_MOVE;
    }

    /** Which side the wall she clings to is on: -1 left, 1 right. */
    int wallSide() { return wallSide; }

    /**
     * A dash: in the air, once until she lands, a burst the way she faces with no falling; on the ground, a dodge roll, a moment in
     * which nothing hurts her. Whether she did.
     */
    boolean dash() {
        if (state != PLAYING) return false;
        float way = facingLeft ? -1 : 1;
        if (onGround && perks.roll && rollLeft <= 0) {
            rollLeft = ROLL_LASTS;
            speedAcross = way * ROLL_SPEED;
            safe = Math.max(safe, ROLL_LASTS + .1f);
            rolls++;
            return true;
        }
        if (!onGround && perks.airDash && !dashUsed && !climbing) {
            dashUsed = true;
            airDashLeft = DASH_LASTS;
            speedAcross = way * DASH_SPEED;
            speedDown = 0;
            dashHit.clear();
            airDashes++;
            return true;
        }
        return false;
    }

    /** The moves as time goes: a dash and a roll carry her; with the strike, a dash beats what it goes through. */
    private void moves(float seconds) {
        treeStruck += seconds;
        quillAge += seconds;
        flipAge += seconds;
        poundAge += seconds;
        if (onGround) dashUsed = false;
        if (rollLeft > 0) {
            rollLeft -= seconds;
            speedAcross = (facingLeft ? -1 : 1) * ROLL_SPEED;
        }
        if (airDashLeft > 0) {
            airDashLeft -= seconds;
            speedAcross = (facingLeft ? -1 : 1) * DASH_SPEED;
            speedDown = 0;
            if (perks.dashStrikes) {
                for (Foe foe : foes) {
                    if (foe.flat || absent(foe) || RunLevels.boss(foe.kind) || foe.kind == RunLevels.TARGET || dashHit.contains(foe)) continue;
                    if (x >= foe.x + foe.wide() || x + WIDE <= foe.x || y >= foe.y + foe.tall() || y + TALL <= foe.y) continue;
                    dashHit.add(foe);
                    dashStruck++;
                    beat(foe, LANDED);
                }
            }
        }
    }

    /** A ground pound landed: what stands on the ground near her is struck, as if landed on. */
    private void poundWave() {
        poundNow = false;
        pounds++;
        poundAge = 0;
        float middle = x + WIDE / 2, feet = y + TALL;
        for (Foe foe : foes) {
            if (foe.flat || absent(foe) || foe.kind == RunLevels.TARGET || RunLevels.flier(foe.kind)) continue;
            if (Math.abs(foe.x + foe.wide() / 2 - middle) > POUND_REACH || Math.abs(foe.y + foe.tall() - feet) > 1.1f) continue;
            beat(foe, LANDED);
        }
    }

    /** Whether a wall is beside her, on a side (-1 left, 1 right), the height of her. */
    private boolean wallBeside(int side) {
        float edge = side < 0 ? x - .06f : x + WIDE + .06f;
        int column = (int) Math.floor(edge);
        for (float at = y + .15f; at < y + TALL - .1f; at += .4f) if (!solid(at(column, (int) Math.floor(at)))) return false;
        return true;
    }

    // The unique creatures

    /** How many times what a sign gives one of them gives: experience, and the chance of a drop. */
    static final int UNIQUE_XP = 3;
    /** How long each way of fighting winds up before it strikes; and how long its strike lasts. */
    private static final float[] WIND_UP = {.6f, .5f, .35f, .5f, .9f, .7f, .7f, 1, .8f, .7f, .5f, .5f, .8f, .7f, .55f, .45f, .5f, .7f, .4f, .9f, .5f, .7f,
            .6f, .8f, .55f, .9f, .7f, .7f, .5f, .6f, .8f, .35f, .8f, .9f},
            STRIKE = {.9f, .3f, .45f, .35f, .15f, .1f, .55f, .2f, .3f, .4f, .7f, .35f, .45f, .45f, .35f, .6f, .7f, .1f, .8f, .2f, .35f, .5f,
            1.6f, .3f, .4f, .25f, .5f, .35f, .35f, .2f, .3f, .4f, 1.1f, 2.2f};
    /** Wind-ups begun, things they made (minions, shots, heals), and how many came apart. */
    private int windUps, uniqueShots, calledUp, heals, halves;

    int windUps() { return windUps; }
    int uniqueShots() { return uniqueShots; }
    int calledUp() { return calledUp; }
    int heals() { return heals; }
    int halvings() { return halves; }

    /**
     * What a unique creature does: about its business ({@link Foe#mode} 0), winding up (1), striking (2), or getting its breath back (3),
     * with {@link Foe#act} how much longer. Every strike has its wind-up first, long enough to be seen: a crouch, a glow, a swell.
     * A charger runs at her; a shielder comes on behind its shield and shoves; a mimic lies still as what it looks like until she is near,
     * then springs; a wall-ghost drifts through walls, fades now and then, and lunges; an exploder swells and bursts in ink; a sniper keeps
     * its distance and shoots; a burrower goes under, comes up beneath her, and is open a moment after; a summoner calls up minus signs; a
     * healer mends whoever is hurt near it; a mini-boss fights in three phases, faster and fiercer at each; formation flyers come over in a
     * line and dive one at a time; a splitter comes apart into two smaller ones.
     */
    private void unique(Foe foe, float seconds, float middle, float away, boolean toLeft) {
        int way = RunUniques.way(foe.kind);
        // A mini-boss, a hidden boss and a keeper come for her where she stands, as those who wait do.
        if ((way == RunUniques.MINIBOSS || way == RunUniques.HIDDEN || way == RunUniques.KEEPER) && reach(foe, seconds, foe.home - 14, foe.home + 14, foe.mode == 0)) return;
        float speed = SPEED[foe.kind] * seconds, feet = y + TALL, down = feet - foe.y - foe.tall(), level_ = Math.abs(down);
        boolean near = away < 9 && level_ < 3;
        foe.wait -= seconds;
        if (way == RunUniques.FORMATION || way == RunUniques.GHOST || RunUniques.floats(foe.kind)) foe.ground = foe.y;
        if (foe.mode == 1) {
            foe.act -= seconds;
            if (foe.act <= 0) {
                foe.mode = 2;
                foe.act = STRIKE[way];
                strikes(foe, way, middle, toLeft);
            }
            return;
        }
        if (foe.mode == 2) {
            striking(foe, way, seconds, speed, middle, toLeft);
            foe.act -= seconds;
            if (foe.act <= 0) {
                foe.mode = 3;
                boolean big = way == RunUniques.MINIBOSS || way == RunUniques.HIDDEN || way == RunUniques.KEEPER;
                foe.act = way == RunUniques.BURROWER ? 1.6f : way == RunUniques.ROLLER ? 1.4f : big ? .6f - .15f * phase3(foe) : .8f;
                foe.wait = way == RunUniques.SUMMONER ? 6 : way == RunUniques.HEALER ? 4 : way == RunUniques.SNIPER ? 2.2f : big ? 1.4f - .4f * phase3(foe)
                        : way == RunUniques.ORBITER ? 3.5f : way == RunUniques.BLINKER ? 2.6f : way == RunUniques.STAMPER ? 2.2f : way == RunUniques.BOUNCER ? 1.2f
                        : way == RunUniques.SWEEPER ? 2.6f : way == RunUniques.LINER || way == RunUniques.BREATHER || way == RunUniques.HERALD ? 2.2f
                        : way == RunUniques.HOURGLASS || way == RunUniques.ECHOER ? 2.4f : 1.6f;
            }
            return;
        }
        if (foe.mode == 3) {
            foe.act -= seconds;
            // (A formation flyer climbs back to its line.)
            if (way == RunUniques.FORMATION) foe.y += Math.max(-speed, Math.min(speed, foe.home - foe.y));
            if (foe.act <= 0) {
                foe.mode = 0;
                if (way == RunUniques.BURROWER) foe.unseen = true;
            }
            return;
        }
        switch (way) {
            case RunUniques.CHARGER:
            case RunUniques.SPLITTER:
                if (near && level_ < 1.3f) {
                    foe.leftward = toLeft;
                    if (way == RunUniques.CHARGER && away < 6 && foe.wait <= 0) windUp(foe, way);
                    else walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.SHIELDER:
                if (near && level_ < 1.3f) {
                    foe.leftward = toLeft;
                    if (away < 1.9f && foe.wait <= 0) windUp(foe, way);
                    else walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.MIMIC:
                // Still as what it looks like, until she is near; then it is a creature.
                if (foe.turn == 0) {
                    if (away < 2.6f && level_ < 1.5f) {
                        foe.turn = 1;
                        foe.leftward = toLeft;
                        windUp(foe, way);
                    }
                } else if (near && level_ < 1.3f) {
                    foe.leftward = toLeft;
                    if (away < 3 && foe.wait <= 0) windUp(foe, way);
                    else walk(foe, speed, false);
                }
                break;
            case RunUniques.GHOST: {
                // Through walls, toward her; and every few seconds it fades a while, where nothing touches it.
                foe.clock %= 6;
                foe.unseen = foe.clock > 4.5f;
                if (!near && away > 14) break;
                foe.leftward = toLeft;
                foe.x += (toLeft ? -1 : 1) * Math.min(speed, Math.max(0, away - .5f));
                foe.y += Math.max(-speed, Math.min(speed, y + TALL * .3f - foe.y));
                if (!foe.unseen && away < 2.3f && level_ < 1.8f && foe.wait <= 0) windUp(foe, way);
                break;
            }
            case RunUniques.EXPLODER:
                if (near && level_ < 1.3f) {
                    foe.leftward = toLeft;
                    if (away < 1.7f) windUp(foe, way);
                    else walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.SNIPER:
                foe.leftward = toLeft;
                if (near && away < 3.5f) {
                    // (Backs off, keeping its distance.)
                    foe.leftward = !toLeft;
                    walk(foe, speed, false);
                    foe.leftward = toLeft;
                } else if (away < 10 && level_ < 2.5f && foe.wait <= 0) {
                    windUp(foe, way);
                }
                break;
            case RunUniques.BURROWER:
                // Under the ground it comes after her, unseen; beneath her, it comes up.
                if (!near) {
                    foe.unseen = false;
                    walk(foe, speed * .5f, true);
                    break;
                }
                foe.unseen = true;
                foe.leftward = toLeft;
                if (away < .7f && level_ < 1.3f && foe.wait <= 0) {
                    foe.unseen = false;
                    windUp(foe, way);
                } else {
                    walk(foe, Math.min(speed * 1.3f, away), false);
                }
                break;
            case RunUniques.SUMMONER:
                foe.leftward = toLeft;
                if (near && away < 4.5f) {
                    foe.leftward = !toLeft;
                    walk(foe, speed, false);
                    foe.leftward = toLeft;
                } else if (near && foe.wait <= 0 && foe.did < 4) {
                    windUp(foe, way);
                }
                break;
            case RunUniques.HEALER: {
                // To whoever near it is hurt, and mends them.
                Foe hurt = null;
                float best = 8;
                for (Foe other : foes) {
                    if (other == foe || other.flat || other.left >= other.most || RunLevels.boss(other.kind) || absent(other)) continue;
                    float far = Math.abs(other.x - foe.x);
                    if (far < best && Math.abs(other.y - foe.y) < 3) {
                        best = far;
                        hurt = other;
                    }
                }
                if (hurt == null) {
                    if (near && level_ < 1.3f) {
                        foe.leftward = !toLeft;
                        walk(foe, speed * .6f, false);
                    } else {
                        walk(foe, speed * .4f, true);
                    }
                } else {
                    foe.leftward = hurt.x < foe.x;
                    if (best < 4 && foe.wait <= 0) windUp(foe, way);
                    else if (best > 1.5f) walk(foe, speed, false);
                }
                break;
            }
            case RunUniques.HIDDEN:
            case RunUniques.MINIBOSS: {
                int phase = phase3(foe);
                if (near && level_ < 2) {
                    foe.leftward = toLeft;
                    if (away < 2.5f && foe.wait <= 0 || phase >= 1 && away < 8 && foe.wait < -2.5f) windUp(foe, way);
                    else walk(foe, speed * (1 + .4f * phase), false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            }
            case RunUniques.SWIMMER: {
                // In the water only: toward her, and then each its own way. The eel winds up and darts; the squid keeps off and squirts ink;
                // the puffer swells and bursts; the jelly drifts up and down after her, crackling.
                int which = RunUniques.index(foe.kind) - (RunUniques.WATER_FIRST - RunUniques.FIRST);
                float dx = x + WIDE / 2 - middle, dy = y + TALL / 2 - (foe.y + foe.tall() / 2), far = (float) Math.hypot(dx, dy);
                if (far > 12) break;
                foe.leftward = dx < 0;
                float keep = which == 0 ? 5 : 0, pace = speed * (which == 3 ? .7f : 1);
                float toward = far > keep ? 1 : -1;
                if (which == 3) dy += (float) Math.sin(foe.clock * 2) * 1.2f;
                swimBy(foe, toward * dx / Math.max(.3f, far) * pace, toward * dy / Math.max(.3f, far) * pace);
                boolean ready = foe.wait <= 0;
                if (which == 0 && far < 8 && ready) windUp(foe, way);
                else if (which == 1 && far < 1.8f) windUp(foe, way);
                else if (which == 2 && far < 6 && ready) windUp(foe, way);
                break;
            }
            case RunUniques.FORMATION:
                // Over in a line, toward her, bobbing; over her, one at a time dives.
                foe.leftward = toLeft;
                if (away < 16) foe.x += (toLeft ? -1 : 1) * Math.min(speed, Math.max(0, away - .3f));
                foe.y = foe.home + (float) Math.sin(foe.clock * 3 + foe.home) * .2f;
                if (away < 1.4f && down > 0 && foe.wait <= 0) {
                    boolean diving = false;
                    for (Foe other : foes) diving |= other != foe && other.kind == foe.kind && other.mode == 2 && Math.abs(other.x - foe.x) < 8;
                    if (!diving) windUp(foe, way);
                }
                break;
            case RunUniques.BLINKER: {
                // The moth flutters about her, bobbing; then, lit, it blinks to behind her and lunges.
                float wantY = y + TALL * .25f + (float) Math.sin(foe.clock * 3) * .3f, dx = middle - (x + WIDE / 2);
                foe.leftward = toLeft;
                if (Math.abs(dx) > 2.5f && away < 12) driftBy(foe, (toLeft ? -1 : 1) * speed, 0);
                driftBy(foe, 0, Math.max(-speed, Math.min(speed, wantY - foe.y)));
                if (away < 7 && level_ < 3 && foe.wait <= 0) windUp(foe, way);
                break;
            }
            case RunUniques.DROPPER:
                // Hanging on its thread over the way until she is under it; then down it comes, and after that it runs at her.
                if (foe.turn == 0) {
                    foe.y = foe.ground - DROP_HANG + (float) Math.sin(foe.clock * 2) * .12f;
                    if (away < 1.3f && down > 0) windUp(foe, way);
                } else if (near && level_ < 1.3f) {
                    foe.leftward = toLeft;
                    if (away < 4 && foe.wait <= 0) windUp(foe, way);
                    else walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.LEAPER:
            case RunUniques.BOUNCER: {
                // The frog hops toward her and then leaps; the stamp bounces as it goes, and then springs at her.
                if (way == RunUniques.BOUNCER) foe.y = foe.ground - Math.abs((float) Math.sin(foe.clock * Math.PI / .55f)) * .7f;
                if (near && level_ < 2.5f) {
                    foe.leftward = toLeft;
                    if (away < 6 && foe.wait <= 0 && leapTo(foe, x + WIDE / 2) != 0) windUp(foe, way);
                    else walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            }
            case RunUniques.ORBITER: {
                // The wisp floats a little way off, at the height of her head, its three quills about it; it flings them, and grows them again.
                float wantX = x + WIDE / 2 + (toLeft ? 3.2f : -3.2f), wantY = y - .2f + (float) Math.sin(foe.clock * 2) * .25f;
                foe.leftward = toLeft;
                if (away < 12) driftBy(foe, Math.max(-speed, Math.min(speed, wantX - middle)), Math.max(-speed, Math.min(speed, wantY - foe.y)));
                if (foe.did == 1 && foe.wait <= 0) foe.did = 0;
                if (foe.did == 0) {
                    // (Its quills cut whatever they meet as they go round.)
                    for (int one = 0; one < 3; one++) {
                        float[] at = quill(foe, one);
                        if (at[0] > x && at[0] < x + WIDE && at[1] > y && at[1] < y + TALL && safe <= 0 && offIt <= 0) {
                            cause = STRUCK;
                            knock = x + WIDE / 2 < at[0] ? -1 : 1;
                            if (hurt(false, 2 + RunUniques.bite(foe.kind, level.number))) return;
                        }
                    }
                    if (away < 7 && level_ < 3 && foe.wait <= 0) windUp(foe, way);
                }
                break;
            }
            case RunUniques.STAMPER:
                // Slow, and heavy: near her it rears up and slams the ground, and the blow runs along it both ways.
                if (near && level_ < 1.6f) {
                    foe.leftward = toLeft;
                    if (away < 5 && foe.wait <= 0) windUp(foe, way);
                    else walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.MIRROR: {
                // Her own shadow in ink: it goes where she would be in a mirror set at where it began, jumps as she jumps, and near her it lunges.
                float want = Math.max(foe.home - 7, Math.min(foe.home + 7, 2 * foe.home + foe.wide() / 2 - x - WIDE / 2));
                foe.leftward = want < foe.x;
                if (Math.abs(want - foe.x) > .1f) walk(foe, Math.min(speed, Math.abs(want - foe.x)), false);
                boolean up = !onGround && speedDown < -4;
                if (up && foe.turn == 0 && foe.y >= foe.ground - .01f) foe.toX = -9;
                foe.turn = up ? 1 : 0;
                if (foe.y < foe.ground - .001f || foe.toX < 0) {
                    foe.toX += 24 * seconds;
                    foe.y = Math.min(foe.ground, foe.y + foe.toX * seconds);
                    if (foe.y >= foe.ground) foe.toX = 0;
                }
                foe.leftward = toLeft;
                if (away < 1.8f && level_ < 1.6f && foe.wait <= 0) windUp(foe, way);
                break;
            }
            case RunUniques.KEEPER: {
                // A keeper fights in three phases, faster at each: it charges, it slams the ground, it sends a volley of what is its own;
                // and in its last third it calls up those of its dungeon.
                int phase = phase3(foe);
                if (away < 15 && level_ < 4) {
                    foe.leftward = toLeft;
                    if (away < 9 && foe.wait <= 0) windUp(foe, way);
                    else if (away > 2.2f) walk(foe, speed * (1 + .35f * phase), false);
                } else {
                    walk(foe, speed * .4f, true);
                }
                break;
            }
            case RunUniques.REFLECTOR:
                // The crab turns round to her only slowly, its mirror shell to the front: from behind, before it has turned, it can be hit.
                if (near && level_ < 1.4f) {
                    if (toLeft != foe.leftward) {
                        if ((foe.toX += seconds) > CRAB_TURNS) {
                            foe.leftward = toLeft;
                            foe.toX = 0;
                        }
                        break;
                    }
                    foe.toX = 0;
                    if (away < 1.9f && foe.wait <= 0) windUp(foe, way);
                    else walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.ROLLER:
                // The pillbug walks at her, curls up, and bowls along the ground; off a wall it comes back once, and after it is dizzy a while.
            case RunUniques.LASHER:
                // The serpent comes near enough and whips its whole length at her, from well out of arm's reach.
            case RunUniques.LINER:
                // Strikethrough raises its ruler, a red line along it the length of the way ahead, and strikes it through: jump it.
            case RunUniques.PULLER:
                // The toad swells and shoots its tongue: what it catches is pulled in to it.
            case RunUniques.BREATHER: {
                float reach = way == RunUniques.ROLLER ? 7 : way == RunUniques.LASHER ? LASH : way == RunUniques.LINER ? LINE : way == RunUniques.PULLER ? TONGUE
                        : way == RunUniques.BREATHER ? 4 : 1.9f;
                if (near && level_ < (way == RunUniques.LINER ? 1.2f : 1.4f)) {
                    foe.leftward = toLeft;
                    if (away < reach && foe.wait <= 0) windUp(foe, way);
                    else if (away > (way == RunUniques.LASHER ? 2 : way == RunUniques.BREATHER ? 2.5f : .5f)) walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            }
            case RunUniques.HERALD:
                // The herald keeps a few squares off and shouts the headlines at her: rings of words that wave as they go.
                foe.leftward = toLeft;
                if (near && away < 3) {
                    foe.leftward = !toLeft;
                    walk(foe, speed, false);
                    foe.leftward = toLeft;
                } else if (away < 9 && level_ < 2.5f && foe.wait <= 0) {
                    windUp(foe, way);
                } else if (near) {
                    walk(foe, speed * .6f, false);
                }
                break;
            case RunUniques.SMEARER:
                // The smudge leaves a smear wherever it goes, that she wades through at half her pace; near her, it splats.
                if ((foe.fromX -= seconds) <= 0 && smears.size() < 24 && !foe.crowd) {
                    smears.add(new float[]{middle, foe.ground + foe.tall(), SMEAR_LASTS});
                    foe.fromX = 1.1f;
                }
                if (near && level_ < 1.4f) {
                    foe.leftward = toLeft;
                    if (away < 1.8f && foe.wait <= 0) windUp(foe, way);
                    else walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.ECHOER: {
                // The echo floats a little way off, at her height; whatever she does near it, it answers in kind.
                float wantX = x + WIDE / 2 + (toLeft ? 4 : -4), wantY = y - .3f + (float) Math.sin(foe.clock * 2) * .3f;
                foe.leftward = toLeft;
                if (away < 13) driftBy(foe, Math.max(-speed, Math.min(speed, wantX - middle)), Math.max(-speed, Math.min(speed, wantY - foe.y)));
                if (strikes != foe.did) {
                    foe.did = strikes;
                    if (away < 9 && level_ < 4 && foe.wait <= 0) windUp(foe, way);
                }
                break;
            }
            case RunUniques.HOURGLASS:
                // Time runs slow about the sand-glass: near it she goes at half her pace; and it throws sand.
                if (near && level_ < 2) {
                    foe.leftward = toLeft;
                    if (away < 5 && foe.wait <= 0) windUp(foe, way);
                    else if (away > 1.5f) walk(foe, speed, false);
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.HERMIT:
                // The hermit pulls into its scroll when she comes near, where nothing from in front of it tells; then it pops out and jabs, and
                // is open a moment after.
                if (foe.turn == 1) {
                    if ((foe.toX -= seconds) <= 0) {
                        foe.turn = 0;
                        foe.leftward = toLeft;
                        windUp(foe, way);
                    }
                    break;
                }
                if (near && level_ < 1.4f) {
                    foe.leftward = toLeft;
                    if (away < 3.5f && foe.wait <= 0) {
                        foe.turn = 1;
                        foe.toX = HIDES;
                    } else {
                        walk(foe, speed, false);
                    }
                } else {
                    walk(foe, speed * .5f, true);
                }
                break;
            case RunUniques.SWEEPER:
                // The sentinel's lantern sweeps a beam up from the ground ahead to over its head: out of the light, she is safe.
                if (away < 11 && level_ < 4) {
                    foe.leftward = toLeft;
                    if (away < SWEEP_REACH && foe.wait <= 0) windUp(foe, way);
                    else if (away > 4) walk(foe, speed, false);
                } else {
                    walk(foe, speed * .4f, true);
                }
                break;
            default:
                walk(foe, speed, true);
        }
    }

    // The later chapters' creatures

    /** How far the serpent lashes, the strikethrough's line, and the toad's tongue reach; the sentinel's beam; the sand-glass's slow time; how long the hermit hides; how long a smear lies; how slow she goes in one; and how long the crab takes to turn round. */
    static final float LASH = 3.4f, LINE = 9, TONGUE = 6, SWEEP_REACH = 7.5f, SAND_REACH = 3.2f, HIDES = 1.4f, SMEAR_LASTS = 6, SLOWED = .5f, CRAB_TURNS = .8f;
    /** The smears a smudge has left: where each lies, the height of the ground under it, and how much longer it lies. */
    private final List<float[]> smears = new ArrayList<>();
    /** Counts that only go up: shots sent back by a mirror shell, and pulls by a tongue. */
    private int mirrored, tongued;

    List<float[]> smears() { return smears; }
    int mirrored() { return mirrored; }
    int tongued() { return tongued; }

    /** Whether one of them, striking, reaches her in front of it: so far ahead of its middle, and at its height. */
    private boolean inFront(Foe foe, float middle, float reach, float slack) {
        float ahead = (x + WIDE / 2 - middle) * (foe.leftward ? -1 : 1);
        return ahead > -.3f && ahead < reach + foe.wide() / 2 && y < foe.y + foe.tall() + slack && y + TALL > foe.y - slack;
    }

    /** What one of them lands on her when its strike reaches her: a hurt, as its touch would be. Whether anything came of it. */
    private boolean zap(Foe foe) {
        if (safe > 0 || offIt > 0) return false;
        cause = STRUCK;
        knock = x + WIDE / 2 < foe.x + foe.wide() / 2 ? -1 : 1;
        return hurt(false, 2 + RunUniques.bite(foe.kind, level.number));
    }

    /** Where the sentinel's beam points, in radians up from level ahead of it: low at first, up over its head by the end of its sweep. */
    static float beamAngle(Foe foe) {
        float through = foe.mode == 2 ? 1 - Math.max(0, foe.act) / STRIKE[RunUniques.SWEEPER] : 0;
        return -.45f + through * 1.6f;
    }

    /** Where the beam comes from: the lantern at the top of the sentinel. */
    static float[] lantern(Foe foe) { return new float[]{foe.x + foe.wide() / 2 + (foe.leftward ? -.2f : .2f), foe.y + foe.tall() * .2f}; }

    /** Whether the beam is on her now. */
    private boolean inBeam(Foe foe) {
        float[] from = lantern(foe);
        float dx = (x + WIDE / 2 - from[0]) * (foe.leftward ? -1 : 1), dy = from[1] - (y + TALL * .5f), far = (float) Math.hypot(dx, dy);
        if (dx < 0 || far > SWEEP_REACH) return false;
        // (She is a body, not a point: the nearer, the wider the angle she takes up.)
        float angle = (float) Math.atan2(dy, dx), wide = (float) Math.atan2(TALL * .5f, Math.max(.5f, far));
        return Math.abs(angle - beamAngle(foe)) < wide;
    }

    /** How fast she goes for what is about her: half her pace in a smear or by a sand-glass. */
    private float slowedBy() {
        float middle = x + WIDE / 2, feet = y + TALL;
        if (onGround) for (float[] smear : smears) if (Math.abs(smear[0] - middle) < .9f && Math.abs(smear[1] - feet) < .35f) return SLOWED;
        for (Foe foe : foes) {
            if (foe.flat || absent(foe) || !RunUniques.is(foe.kind) || RunUniques.way(foe.kind) != RunUniques.HOURGLASS) continue;
            if (Math.hypot(foe.x + foe.wide() / 2 - middle, foe.y + foe.tall() / 2 - y - TALL / 2) < SAND_REACH) return SLOWED;
        }
        return 1;
    }

    // The dungeons' creatures

    /** How far over its place a spider hangs; how fast what leaps falls; and how far a leap goes at most. */
    static final float DROP_HANG = 4, LEAP_FALL = 22, LEAP_MOST = 5;
    /** Blinks made, spiders dropped, leaps taken, stamps of the ground made, and keepers beaten, in this land. */
    private int blinks, drops_, leaps, stamps, keepersBeaten;

    int blinks() { return blinks; }
    int spidersDropped() { return drops_; }
    int leaps() { return leaps; }
    int stamps() { return stamps; }
    int keepersBeaten() { return keepersBeaten; }

    /** Where one of a wisp's three quills is now: the middle of it. */
    static float[] quill(Foe foe, int which) {
        double angle = foe.clock * 2.4f + which * Math.PI * 2 / 3;
        float reach = 1.15f * foe.size;
        return new float[]{foe.x + foe.wide() / 2 + (float) Math.cos(angle) * reach, foe.y + foe.tall() / 2 + (float) Math.sin(angle) * reach * .8f};
    }

    /** One that floats moves so far, unless that would put it inside something solid. */
    private void driftBy(Foe foe, float across, float down) {
        if (roomFor(foe, foe.x + across, foe.y)) foe.x += across;
        if (roomFor(foe, foe.x, foe.y + down)) foe.y += down;
        foe.ground = foe.y;
    }

    /** Whether one would fit at a place: nothing solid where it would be, and inside the land. */
    private boolean roomFor(Foe foe, float fx, float fy) {
        if (fx < 0 || fx + foe.wide() > columns || fy < 0) return false;
        for (int column = (int) Math.floor(fx + .05f); column <= (int) Math.floor(fx + foe.wide() - .05f); column++) {
            for (int row = (int) Math.floor(fy + .05f); row <= (int) Math.floor(fy + foe.tall() - .05f); row++) if (solid(at(column, row))) return false;
        }
        return true;
    }

    /**
     * How far across a leap at her would take one (toward her, no further than a leap goes): as far as there is ground at its own height
     * to come down on, and nothing in the way between; 0 if there is none.
     */
    private float leapTo(Foe foe, float target) {
        float middle = foe.x + foe.wide() / 2, want = Math.max(-LEAP_MOST, Math.min(LEAP_MOST, target - middle));
        int row = (int) Math.floor(foe.ground + foe.tall() + .05f);
        for (float across = want; Math.abs(across) >= 1; across -= Math.signum(want) * .5f) {
            float lands = foe.x + across;
            boolean ground = solid(at((int) Math.floor(lands + .1f), row)) && solid(at((int) Math.floor(lands + foe.wide() - .1f), row));
            if (ground && roomFor(foe, lands, foe.ground)) return across;
        }
        return 0;
    }

    /** A slam: the ground jumps, and the blow runs along it both ways from under one. */
    private void stomp(Foe foe, float middle, float fast) {
        float feet = foe.ground + foe.tall();
        shoot(SHOCK, false, middle - .5f, feet - .3f, -fast, 0, 1.6f);
        shoot(SHOCK, false, middle + .5f, feet - .3f, fast, 0, 1.6f);
        stamps++;
        uniqueShots += 2;
    }

    /** What a keeper sends at her: index cards, needles, ink, chips of stone, paper streamers, the archivist's motes. */
    static int keeperShot(int kind) {
        switch (kind - RunUniques.KEEPER_FIRST) {
            case 0: return SCROLL;
            case 1: return KNIFE;
            case 2: return BLOT_SHOT;
            case 3: return STOP;
            case 4: return FEATHER;
            default: return DOT;
        }
    }

    // Reaching her wherever she stands

    /** How long she may stand above one of them before it comes for her; its wind-up; its leap; and how fast it falls. */
    static final float PERCH = 1.6f, LEAP_WIND = .55f, LEAP_TIME = .7f, BOSS_FALL = 24;
    /** Leaps made up to her, shots sent up at her where it could not leap, and falls back down. */
    private int bossLeaps, bossReaches, bossDrops;

    int bossLeaps() { return bossLeaps; }
    int bossReaches() { return bossReaches; }
    int bossDrops() { return bossDrops; }

    /** Whether something solid (or a ledge) is under one at a place, under both its edges. */
    private boolean standsAt(Foe foe, float fx, float feet) {
        int row = (int) Math.floor(feet + .05f);
        return standable(at((int) Math.floor(fx + .15f), row)) || standable(at((int) Math.floor(fx + foe.wide() - .15f), row));
    }

    /** Whether, falling straight down from where it stands, one would come down on something, before water, lava or the bottom of the land. */
    private boolean groundBelow(Foe foe, float feet) {
        int middle = (int) Math.floor(foe.x + foe.wide() / 2);
        for (int row = (int) Math.floor(feet + .05f) + 1; row < level.land.length; row++) {
            char square = at(middle, row);
            if (square == RunLevels.WATER || square == RunLevels.LAVA) return false;
            if (standsAt(foe, foe.x, row)) return true;
        }
        return false;
    }

    private boolean standable(char square) {
        return solid(square) || square == RunLevels.LEDGE || square == RunLevels.THIN;
    }

    /**
     * One who waits (and a mini-boss, a hidden boss, a keeper) is never to be stood above for nothing: once she has stood on a ledge over
     * it for a moment, it crouches, glowing, and leaps up onto where she is, coming down with a slam that runs along the ledge; where it
     * cannot fit there, it sends something up at her instead. When she is below it again, or the ledge is gone from under it, it comes
     * down. Returns whether it is in the middle of that, so that what it does otherwise waits.
     */
    private boolean reach(Foe foe, float seconds, float lo, float hi, boolean free) {
        if (foe.flat || absent(foe) || foe.away) return false;
        float feet = foe.ground + foe.tall(), herFeet = y + TALL, px = x + WIDE / 2, middle = foe.x + foe.wide() / 2;
        if (foe.base == 0) foe.base = foe.ground;
        switch (foe.leaping) {
            case 1:
                // The wind-up: crouched and glowing.
                if ((foe.leap -= seconds) <= 0) {
                    foe.leaping = 2;
                    foe.leap = 0;
                    foe.leapFX = foe.x;
                    foe.leapFY = foe.y;
                }
                return true;
            case 2: {
                foe.leap += seconds;
                float through = Math.min(1, foe.leap / LEAP_TIME), high = 2.2f + Math.max(0, foe.leapFY - foe.leapTY);
                foe.x = foe.leapFX + (foe.leapTX - foe.leapFX) * through;
                foe.y = foe.leapFY + (foe.leapTY - foe.leapFY) * through - (float) Math.sin(through * Math.PI) * high;
                if (through >= 1) {
                    foe.leaping = 0;
                    foe.ground = foe.y = foe.leapTY;
                    foe.perch = 0;
                    // The slam it comes down with, along the ledge both ways.
                    float under = foe.ground + foe.tall();
                    shoot(SHOCK, false, foe.x + foe.wide() / 2 - .5f, under - .3f, -7, 0, .9f);
                    shoot(SHOCK, false, foe.x + foe.wide() / 2 + .5f, under - .3f, 7, 0, .9f);
                }
                return true;
            }
            case 3: {
                // Coming down: falling until there is something to stand on, and never through the floor it began on.
                foe.fall += BOSS_FALL * seconds;
                float next = foe.y + foe.fall * seconds;
                if (next >= foe.base && standsAt(foe, foe.x, foe.base + foe.tall()) || standsAt(foe, foe.x, next + foe.tall()) && Math.floor(next + foe.tall()) > Math.floor(foe.y + foe.tall())) {
                    float landed = next >= foe.base ? foe.base : (float) Math.floor(next + foe.tall()) - foe.tall();
                    foe.ground = foe.y = landed;
                    foe.leaping = 0;
                    foe.fall = 0;
                    foe.perch = 0;
                } else {
                    foe.y = next;
                }
                return true;
            }
            default:
                break;
        }
        // Raised up and with nothing under it any more, or with her down below again for a moment: down it comes.
        boolean raised = foe.ground < foe.base - .2f;
        if (raised && (!standsAt(foe, foe.x, feet) || herFeet > feet + 1.2f && (foe.perch -= seconds) < -1)) {
            foe.leaping = 3;
            foe.fall = 0;
            bossDrops++;
            // (With nothing to come down onto straight below, over water or a gap, it leaps back down to its own floor instead.)
            if (!groundBelow(foe, feet)) {
                float back = Float.NaN;
                for (float shift = 0; shift <= 16 && Float.isNaN(back); shift += .5f) {
                    for (float side : new float[]{-1, 1}) {
                        float at = Math.max(lo, Math.min(hi, foe.x + side * shift));
                        if (standsAt(foe, at, foe.base + foe.tall()) && roomFor(foe, at, foe.base - .02f)) {
                            back = at;
                            break;
                        }
                    }
                }
                if (Float.isNaN(back)) back = Math.max(lo, Math.min(hi, foe.home));
                foe.leaping = 2;
                foe.leap = 0;
                foe.leapFX = foe.x;
                foe.leapFY = foe.y;
                foe.leapTX = back;
                foe.leapTY = foe.base;
            }
            return true;
        }
        // Stood above it, on a ledge, within its reach: it comes for her.
        boolean above = onGround && herFeet < feet - 1.2f && Math.abs(px - middle) < 15 && room == roomOf(foe);
        if (!above) {
            if (!raised) foe.perch = Math.max(0, foe.perch - seconds * 2);
            return false;
        }
        if (!free || (foe.perch += seconds) < PERCH) return false;
        foe.perch = 0;
        float want = Math.max(lo, Math.min(hi, px - foe.wide() / 2)), top = herFeet - foe.tall();
        float to = Float.NaN;
        for (float shift : new float[]{0, -.5f, .5f, -1, 1, -1.5f, 1.5f, -2, 2}) {
            float at = Math.max(lo, Math.min(hi, want + shift));
            if (roomFor(foe, at, top - .02f) && standsAt(foe, at, herFeet)) {
                to = at;
                break;
            }
        }
        if (Float.isNaN(to)) {
            // No room up there for it: what it has, aimed at her where she stands.
            float cy = foe.y + foe.tall() * .4f, toX = px - middle, toY = y + TALL * .45f - cy, far = Math.max(.5f, (float) Math.hypot(toX, toY));
            for (int one = -1; one <= 1; one++) shoot(DOT, false, middle, cy, toX / far * 7 + one * .8f, toY / far * 7, 2.4f);
            bossReaches++;
            return false;
        }
        foe.leaping = 1;
        foe.leap = LEAP_WIND;
        foe.leapTX = to;
        foe.leapTY = top;
        foe.leftward = px < middle;
        bossLeaps++;
        return true;
    }

    /** Which place a creature is in: the land (-1), or the place off it whose walls it stands between. */
    private int roomOf(Foe foe) {
        if (foe.x < level.main) return -1;
        for (int at = 0; at < level.rooms.size(); at++) {
            int[] one = level.rooms.get(at);
            if (foe.x >= one[3] - 1 && foe.x <= one[4] + 1) return at;
        }
        return -1;
    }

    /** Whether a keeper still stands in the land: while one does, the way out stays shut. */
    boolean keeperStands() {
        for (Foe foe : foes) if (RunUniques.keeper(foe.kind) && !foe.flat) return true;
        return false;
    }

    /** She comes down onto a floor of a dungeon with the hearts and lives she had on the floor above. */
    void carry(int hpLeft, int livesLeft) {
        hp = Math.max(1, Math.min(maxHp(), hpLeft));
        lives = Math.max(1, livesLeft);
    }

    /** Her hearts are mended, all of them: a blessing. */
    void mend() { hp = maxHp(); }

    /** One that swims goes so far, across and down, as far as the water lets it. */
    private void swimBy(Foe foe, float across, float down) {
        float nx = foe.x + across, ny = foe.y + down;
        if (wet(nx, foe.y, foe)) foe.x = nx;
        if (wet(foe.x, ny, foe)) foe.y = ny;
        foe.ground = foe.y;
    }

    /** Whether every corner of one, at a place, is in water. */
    private boolean wet(float fx, float fy, Foe foe) {
        return at((int) Math.floor(fx + .05f), (int) Math.floor(fy + .05f)) == RunLevels.WATER && at((int) Math.floor(fx + foe.wide() - .05f), (int) Math.floor(fy + .05f)) == RunLevels.WATER
                && at((int) Math.floor(fx + .05f), (int) Math.floor(fy + foe.tall() - .05f)) == RunLevels.WATER
                && at((int) Math.floor(fx + foe.wide() - .05f), (int) Math.floor(fy + foe.tall() - .05f)) == RunLevels.WATER;
    }

    /** Which third of what it can take a creature has lost: 0, 1 or 2. */
    private static int phase3(Foe foe) { return foe.most <= 0 ? 0 : Math.min(2, (foe.most - Math.max(0, foe.left)) * 3 / foe.most); }

    private void windUp(Foe foe, int way) {
        foe.mode = 1;
        foe.act = way == RunUniques.MINIBOSS || way == RunUniques.HIDDEN || way == RunUniques.KEEPER ? WIND_UP[way] - .15f * phase3(foe) : WIND_UP[way];
        windUps++;
    }

    /** The moment a strike begins: a shot, a burst, minions, a heal. */
    private void strikes(Foe foe, int way, float middle, boolean toLeft) {
        float cy = foe.y + foe.tall() * .5f;
        switch (way) {
            case RunUniques.EXPLODER:
                // It bursts in ink all round: whatever of that meets her hurts; and it is gone.
                for (int drop = 0; drop < 8; drop++) {
                    double angle = drop * Math.PI / 4;
                    shoot(BLOT_SHOT, false, middle, cy, (float) Math.cos(angle) * 5, (float) Math.sin(angle) * 5 - 1, .35f);
                }
                uniqueShots += 8;
                foe.left = 0;
                foe.flat = true;
                foe.act = 0;
                flattened++;
                break;
            case RunUniques.SNIPER: {
                float toX = x + WIDE / 2 - middle, toY = y + TALL * .45f - cy, far = Math.max(.5f, (float) Math.hypot(toX, toY)), fast = 7;
                shoot(RunUniques.index(foe.kind) == 5 ? FEATHER : DOT, false, middle, cy, toX / far * fast, toY / far * fast, 2.2f);
                uniqueShots++;
                break;
            }
            case RunUniques.SUMMONER:
                for (int one = 0; one < 2 && foe.did < 4; one++) {
                    Foe minion = new Foe(RunLevels.MINUS);
                    minion.left = minion.most = healthOf(RunLevels.MINUS);
                    float at = middle + (one == 0 ? -1 : 1) * .9f;
                    int column = (int) Math.floor(at), row = (int) Math.floor(foe.ground + foe.tall() - .001f);
                    if (solid(at(column, row)) || !solid(at(column, row + 1))) at = middle;
                    minion.x = at - minion.wide() / 2;
                    minion.ground = foe.ground + foe.tall() - minion.tall();
                    minion.y = minion.ground;
                    minion.home = minion.x;
                    minion.leftward = toLeft;
                    minion.dazed = .5f;
                    born.add(minion);
                    foe.did++;
                    calledUp++;
                }
                break;
            case RunUniques.HEALER:
                for (Foe other : foes) {
                    if (other.flat || RunLevels.boss(other.kind) || other.left >= other.most || Math.abs(other.x - foe.x) > 4.5f || Math.abs(other.y - foe.y) > 3) continue;
                    other.left = Math.min(other.most, other.left + Math.max(BLOW, other.most * 3 / 10));
                    heals++;
                }
                break;
            case RunUniques.SWIMMER: {
                int which = RunUniques.index(foe.kind) - (RunUniques.WATER_FIRST - RunUniques.FIRST);
                if (which == 0) {
                    // The squid's ink: three blots at her, fanned.
                    float toX = x + WIDE / 2 - middle, toY = y + TALL / 2 - cy, far = Math.max(.5f, (float) Math.hypot(toX, toY));
                    for (int one = -1; one <= 1; one++) shoot(BLOT_SHOT, false, middle, cy, toX / far * 6 + one * toY / far * 1.5f, toY / far * 6 - one * toX / far * 1.5f, 1.6f);
                    uniqueShots += 3;
                } else if (which == 1) {
                    // The puffer bursts, quills all round, and is gone.
                    for (int drop = 0; drop < 8; drop++) {
                        double angle = drop * Math.PI / 4;
                        shoot(KNIFE, false, middle, cy, (float) Math.cos(angle) * 5, (float) Math.sin(angle) * 5, .4f);
                    }
                    uniqueShots += 8;
                    foe.left = 0;
                    foe.flat = true;
                    foe.act = 0;
                    flattened++;
                }
                break;
            }
            case RunUniques.HIDDEN: {
                // A hidden boss throws what is its own, more of it at each phase.
                int many = 3 + phase3(foe) * 2;
                for (int one = 0; one < many; one++) {
                    float spread = (one - (many - 1) / 2f) * 1.1f;
                    shoot(RunUniques.hiddenShot(foe.kind), false, middle, cy, (toLeft ? -1 : 1) * 6.5f, spread, 1.8f);
                }
                uniqueShots += many;
                break;
            }
            case RunUniques.BLINKER: {
                // To behind her, at her height, if there is room there.
                float to = x + WIDE / 2 + (facingLeft ? 1.7f : -1.7f) - foe.wide() / 2, high = y + TALL * .2f;
                if (roomFor(foe, to, high)) {
                    foe.x = to;
                    foe.y = high;
                    blinks++;
                }
                foe.leftward = x + WIDE / 2 < foe.x + foe.wide() / 2;
                break;
            }
            case RunUniques.DROPPER:
                if (foe.turn == 0) drops_++;
                break;
            case RunUniques.LEAPER:
            case RunUniques.BOUNCER: {
                float across = leapTo(foe, x + WIDE / 2), time = STRIKE[way];
                foe.fromX = across / time;
                foe.toX = -.5f * LEAP_FALL * time * (way == RunUniques.BOUNCER ? 1.25f : 1);
                foe.y = foe.ground;
                leaps++;
                break;
            }
            case RunUniques.ORBITER:
                // Its three quills, at her.
                for (int one = 0; one < 3; one++) {
                    float[] at = quill(foe, one);
                    float toX = x + WIDE / 2 - at[0], toY = y + TALL * .45f - at[1], far = Math.max(.5f, (float) Math.hypot(toX, toY));
                    shoot(FEATHER, false, at[0], at[1], toX / far * 6.5f, toY / far * 6.5f, 1.8f);
                }
                uniqueShots += 3;
                foe.did = 1;
                break;
            case RunUniques.STAMPER:
                stomp(foe, middle, 7);
                break;
            case RunUniques.KEEPER: {
                int phase = phase3(foe), move = foe.turn++ % 3;
                if (move == 0) {
                    // A charge, the length of the hall.
                    foe.act = 1.1f;
                } else if (move == 1) {
                    stomp(foe, middle, 8 + phase);
                    if (phase >= 1) for (int one = -1; one <= 1; one++) shoot(keeperShot(foe.kind), false, middle, foe.y + .3f, one * 2.5f, -7, 1.6f);
                } else {
                    int many = 3 + phase * 2;
                    for (int one = 0; one < many; one++) {
                        float spread = (one - (many - 1) / 2f) * 1.1f;
                        shoot(keeperShot(foe.kind), false, middle, cy, (toLeft ? -1 : 1) * 6.5f, spread, 1.8f);
                    }
                    uniqueShots += many;
                    // In its last third, two of its dungeon's own, one each side.
                    if (phase >= 2 && foe.did < 6) {
                        int dungeon = (RunUniques.index(foe.kind) - RunUniques.index(RunUniques.KEEPER_FIRST)) * 4 + foe.did / 2 % 4;
                        int[] kinds = RunDungeons.delvers(dungeon);
                        for (int side = -1; side <= 1; side += 2) {
                            int kind = kinds[(foe.did + side + 1) % kinds.length];
                            if (RunUniques.way(kind) == RunUniques.DROPPER) kind = kinds[(foe.did + side + 2) % kinds.length];
                            if (RunUniques.way(kind) == RunUniques.DROPPER) kind = RunUniques.DELVER_FIRST + 4;
                            Foe minion = new Foe(kind);
                            minion.left = minion.most = Math.max(BLOW, RunUniques.health(kind, level.number) / 2);
                            float at = middle + side * (foe.wide() / 2 + 1);
                            minion.x = at - minion.wide() / 2;
                            minion.ground = foe.ground + foe.tall() - minion.tall();
                            minion.y = RunUniques.floats(kind) ? minion.ground - 2 : minion.ground;
                            minion.home = RunUniques.floats(kind) ? minion.y : minion.x;
                            minion.leftward = toLeft;
                            minion.dazed = .6f;
                            minion.spawned = true;
                            born.add(minion);
                            foe.did++;
                            calledUp++;
                        }
                    }
                }
                break;
            }
            case RunUniques.ROLLER:
                foe.turn = 0;
                break;
            case RunUniques.LASHER:
            case RunUniques.LINER:
            case RunUniques.PULLER:
            case RunUniques.SWEEPER:
                // (Each strike reaches her once at most.)
                foe.did = 0;
                break;
            case RunUniques.HERALD:
                for (int one = 0; one < 3; one++) shoot(SAYING, false, middle, cy - .5f + one * .5f, (toLeft ? -1 : 1) * (4.5f + one * .6f), 0, 2.4f);
                uniqueShots += 3;
                break;
            case RunUniques.SMEARER:
                for (int drop = 0; drop < 6; drop++) {
                    double angle = Math.PI + drop * Math.PI / 5;
                    shoot(BLOT_SHOT, false, middle, cy, (float) Math.cos(angle) * 5, (float) Math.sin(angle) * 5, .32f);
                }
                uniqueShots += 6;
                break;
            case RunUniques.ECHOER: {
                float toX = x + WIDE / 2 - middle, toY = y + TALL * .45f - cy, far = Math.max(.5f, (float) Math.hypot(toX, toY));
                for (int one = -1; one <= 1; one += 2) shoot(SCROLL, false, middle, cy, toX / far * 6.5f - one * toY / far * 1.2f, toY / far * 6.5f + one * toX / far * 1.2f, 2);
                uniqueShots += 2;
                break;
            }
            case RunUniques.HOURGLASS:
                for (int one = 0; one < 5; one++) {
                    double angle = -.6 + one * .2;
                    shoot(DOT, false, middle, cy, (toLeft ? -1 : 1) * (float) Math.cos(angle) * 6, (float) Math.sin(angle) * 6, 1.4f);
                }
                uniqueShots += 5;
                break;
            case RunUniques.MINIBOSS:
                // From its second phase on, three knives as well, fanned.
                if (phase3(foe) >= 1) {
                    for (int one = -1; one <= 1; one++) shoot(KNIFE, false, middle, cy, (toLeft ? -1 : 1) * 7, one * 1.6f, 1.6f);
                    uniqueShots += 3;
                }
                break;
            default:
                break;
        }
    }

    /** The strike as it goes on: a run, a shove, a spring, a lunge, a dive. */
    private void striking(Foe foe, int way, float seconds, float speed, float middle, boolean toLeft) {
        switch (way) {
            case RunUniques.CHARGER:
                if (!walk(foe, speed * 3.4f, false)) foe.act = 0;
                break;
            case RunUniques.SHIELDER:
            case RunUniques.HIDDEN:
            case RunUniques.MINIBOSS:
                walk(foe, speed * (way == RunUniques.SHIELDER ? 5 : 4.5f + phase3(foe)), false);
                break;
            case RunUniques.MIMIC: {
                walk(foe, speed * 4, false);
                float through = 1 - foe.act / STRIKE[way];
                foe.y = foe.ground - (float) Math.sin(through * Math.PI) * .9f;
                break;
            }
            case RunUniques.GHOST:
                foe.x += (toLeft ? -1 : 1) * speed * 4;
                break;
            case RunUniques.BURROWER: {
                float through = 1 - foe.act / STRIKE[way];
                foe.y = foe.ground - (float) Math.sin(through * Math.PI) * .7f;
                break;
            }
            case RunUniques.SWIMMER: {
                // The eel's dart.
                int which = RunUniques.index(foe.kind) - (RunUniques.WATER_FIRST - RunUniques.FIRST);
                if (which == 2) {
                    float dx = x + WIDE / 2 - middle, dy = y + TALL / 2 - (foe.y + foe.tall() / 2), far = Math.max(.3f, (float) Math.hypot(dx, dy));
                    swimBy(foe, dx / far * speed * 3.2f, dy / far * speed * 3.2f);
                }
                break;
            }
            case RunUniques.BLINKER:
                driftBy(foe, (foe.leftward ? -1 : 1) * speed * 4, 0);
                break;
            case RunUniques.DROPPER:
                if (foe.turn == 0) {
                    foe.y = Math.min(foe.ground, foe.y + speed / SPEED[foe.kind] * 14);
                    if (foe.y >= foe.ground) {
                        foe.turn = 1;
                        foe.act = 0;
                    }
                } else {
                    if (!walk(foe, speed * 3.2f, false)) foe.act = 0;
                }
                break;
            case RunUniques.LEAPER:
            case RunUniques.BOUNCER: {
                float step = speed / SPEED[foe.kind];
                float to = foe.x + foe.fromX * step;
                if (roomFor(foe, to, foe.y)) foe.x = to;
                foe.toX += LEAP_FALL * step;
                foe.y = Math.min(foe.ground, foe.y + foe.toX * step);
                if (foe.act - seconds <= 0) foe.y = foe.ground;
                break;
            }
            case RunUniques.MIRROR:
                if (!walk(foe, speed * 3, false)) foe.act = 0;
                break;
            case RunUniques.KEEPER:
                if ((foe.turn - 1) % 3 == 0 && !walk(foe, speed * (4 + phase3(foe)), false)) foe.act = 0;
                break;
            case RunUniques.FORMATION:
                foe.y += speed * 5;
                if (foe.y + foe.tall() > y + TALL) foe.y = y + TALL - foe.tall();
                break;
            case RunUniques.ROLLER:
                // Off a wall it comes back the other way, once.
                if (!walk(foe, speed * 5, false)) {
                    if (foe.turn++ < 1) foe.leftward = !foe.leftward;
                    else foe.act = 0;
                }
                break;
            case RunUniques.LASHER:
                if (foe.did == 0 && inFront(foe, middle, LASH, .6f)) {
                    foe.did = 1;
                    if (zap(foe)) return;
                }
                break;
            case RunUniques.LINER: {
                float line = foe.y + foe.tall() * .5f, ahead = (x + WIDE / 2 - middle) * (foe.leftward ? -1 : 1);
                if (foe.did == 0 && y < line && y + TALL > line && ahead > -.3f && ahead < LINE) {
                    foe.did = 1;
                    if (zap(foe)) return;
                }
                break;
            }
            case RunUniques.PULLER:
                if (foe.did == 0 && inFront(foe, middle, TONGUE, .5f)) {
                    // Caught: pulled in to it, whatever she was doing.
                    foe.did = 1;
                    speedAcross = (middle < x + WIDE / 2 ? -1 : 1) * 13;
                    flinch = .3f;
                    tongued++;
                }
                break;
            case RunUniques.REFLECTOR:
                walk(foe, speed * 3, false);
                break;
            case RunUniques.HERMIT:
                walk(foe, speed * 4, false);
                break;
            case RunUniques.BREATHER:
                // A stream of fire, a puff at a time.
                if ((foe.fromX -= seconds) <= 0) {
                    foe.fromX = .12f;
                    float way2 = foe.leftward ? -1 : 1, wobble = ((foe.clock * 7.3f) % 1 - .5f) * 1.6f;
                    shoot(FIRE, false, middle + way2 * foe.wide() * .45f, foe.y + foe.tall() * .45f, way2 * 8, wobble, .45f);
                    uniqueShots++;
                }
                break;
            case RunUniques.SWEEPER:
                if (foe.did == 0 && inBeam(foe)) {
                    foe.did = 1;
                    if (zap(foe)) return;
                }
                break;
            default:
                break;
        }
        if (way == RunUniques.MIMIC || way == RunUniques.BURROWER) {
            if (foe.act - seconds <= 0) foe.y = foe.ground;
        }
    }

    /** A splitter beaten: it comes apart into two smaller ones, each a third of what it could take, once. */
    private void halve(Foe foe) {
        for (int one = 0; one < 2; one++) {
            Foe half = new Foe(foe.kind);
            half.size = foe.size * .7f;
            half.did = 1;
            half.left = half.most = Math.max(BLOW, foe.most / 3);
            half.tier = foe.tier;
            half.x = foe.x + foe.wide() / 2 - half.wide() / 2 + (one == 0 ? -.45f : .45f);
            half.ground = foe.ground + foe.tall() - half.tall();
            half.y = half.ground;
            half.home = half.x;
            half.leftward = one == 0;
            half.dazed = .4f;
            born.add(half);
        }
        halves++;
    }

    // Shields

    /**
     * From the sixth land on some creatures carry a shield, more of them further on: a book cover, then steel, gold, and at the last
     * black ink. Held up in front, it takes a blow that comes at it, and only a blow that counts for enough tells on it: her level,
     * what she holds and how rare it is, and her attack all go into that. Weaker blows ring off it. Enough strong ones break it, and
     * then it is a creature like any other. A blow from behind, or coming down on it from above, goes round it. Which ones carry one
     * is the same every time for the same one in the same land.
     */
    static final int SHIELDS_FROM = 6;

    static int shieldOdds(int number) { return number < SHIELDS_FROM ? 0 : Math.min(2600, 500 + number * 21); }

    static boolean shielded(int number, int kind, int column, int row) {
        if (number < SHIELDS_FROM || RunLevels.boss(kind) || RunLevels.flier(kind) || kind == RunLevels.TARGET || kind == RunLevels.ZERO || kind == RunLevels.WORD
                || kind == RunLevels.DRAGON) return false;
        long dice = number * 15485863L + column * 7919L + row * 104729L + kind * 1299709L;
        dice = dice * 6364136223846793005L + 1442695040888963407L;
        return (int) ((dice >>> 29) % 10000) < shieldOdds(number);
    }

    /** What a blow must count for (in tenths, as creatures' health is) to tell on a shield in a land: what the level the land is for gives, and more. */
    static int shieldNeeds(int number) { return Math.round(BLOW * RunLoot.power(RunLoot.recommended(number)) * (2.5f + 1.5f * number / RunLevels.TOTAL)); }

    /** How many strong blows break one: five from the first, more further on. */
    static int shieldHolds(int number) { return 5 + number / 25; }

    static int shieldKind(int number) { return number < 25 ? 0 : number < 50 ? 1 : number < 80 ? 2 : 3; }

    /** Blows that rang off a shield, shields broken, and where the last of either was. */
    private int clangs, shieldsBroken;
    private float clangX, clangY;

    int clangs() { return clangs; }
    int shieldsBroken() { return shieldsBroken; }
    float clangX() { return clangX; }
    float clangY() { return clangY; }

    /** Whether a blow now comes at the front of a creature's shield: not from above, and from the side it faces. */
    private boolean atShield(Foe foe) {
        if (foe.guard <= 0 || with == LANDED || with == PLUNGED) return false;
        return (x + WIDE / 2 < foe.x + foe.wide() / 2) == foe.leftward;
    }

    // The tree's own mechanics

    /**
     * How long blows keep a combo meter growing, and how many it holds; how long a rest makes the next use a charged one, and how long a
     * charged swing lasts; how long a frenzy lasts after a creature is beaten; how long a creature is knocked off its feet, and how slowly it
     * goes meanwhile; how long a pool of ink lies; how wide a shockwave is; how long before a swing strikes again; and how far apart the
     * shots of a fan are.
     */
    static final float METER_WINDOW = 2, CHARGE_REST = 1.5f, CHARGED_SWING = .45f, FRENZY_LASTS = 3, STUN_LASTS = 1, STUN_PACE = .1f, POOL_LASTS = 3,
            QUAKE_WIDE = 2.2f, ECHO_BEAT = .2f, FAN = .16f;
    static final int METER_MOST = 10;
    private int meter, stuns, pools, shockwaves, echoes, fanned, hopped, bounced, bled, treeKills;
    private float sinceMeter = 9, chargedLeft, frenzyLeft, echoLeft, quakeX, quakeY;
    private boolean quakeNow, quaking, sweeping, echoSwing;
    /** The shot whose blow is being dealt, if it is one. */
    private Shot current;
    /** The boxes the use just made swept, and those a swing's echo will sweep again. */
    private final List<float[]> swept = new ArrayList<>(), echoBoxes = new ArrayList<>();

    /** The combo meter, and counts that only go up: creatures knocked off their feet, pools left, shockwaves, echoes, shots fanned out, jumps on, bounces, blood spent, creatures beaten. */
    int meter() { return sinceMeter < METER_WINDOW ? meter : 0; }
    int stuns() { return stuns; }
    int pools() { return pools; }
    int shockwaves() { return shockwaves; }
    int echoes() { return echoes; }
    int fanned() { return fanned; }
    int hopped() { return hopped; }
    int bounced() { return bounced; }
    int bled() { return bled; }
    int treeKills() { return treeKills; }
    float frenzyLeft() { return frenzyLeft; }

    /** What the combo meter adds to her attack now, in percent. */
    private int metered() { return sinceMeter < METER_WINDOW ? Math.max(0, skillOf(RunSkills.E_METER)) * meter : 0; }

    /** How much harder, in percent, the tree makes a blow on this creature: nearly beaten, not yet hurt, or after a rest. */
    private int bonus(Foe foe) {
        int more = 0;
        if (foe.left * 3 <= foe.most) more += skillOf(RunSkills.E_EXECUTE);
        if (foe.left >= foe.most) more += skillOf(RunSkills.E_AMBUSH);
        if (current != null) more += current.boost;
        else if (chargedLeft > 0) more += skillOf(RunSkills.E_CHARGE);
        return Math.max(0, more);
    }

    /** A blow of hers has landed: the combo meter grows; and by chance it leaves a pool of ink, or sends out a shockwave once it is done. */
    private void landed(Foe foe, boolean waits) {
        meter = sinceMeter < METER_WINDOW ? Math.min(METER_MOST, meter + 1) : 1;
        sinceMeter = 0;
        if (puddles.size() < 10 && !RunLevels.flier(foe.kind) && chance(skillOf(RunSkills.E_POOL))) {
            puddles.add(new float[]{foe.x + foe.wide() / 2, foe.y + foe.tall(), POOL_LASTS});
            pools++;
        }
        if (!quaking && !quakeNow && chance(skillOf(RunSkills.E_QUAKE))) {
            quakeNow = true;
            quakeX = foe.x + foe.wide() / 2;
            quakeY = foe.y + foe.tall() * .6f;
        }
    }

    /** She has beaten a creature: HP and energy back, and a frenzy. */
    private void killed() {
        treeKills++;
        int heal = skillOf(RunSkills.E_KILL_HEAL), more = skillOf(RunSkills.E_KILL_ENERGY);
        if (heal > 0 && hp > 0 && hp < maxHp()) {
            hp = Math.min(maxHp(), hp + heal);
            regenAge = 0;
        }
        if (more > 0) energy = Math.min(ENERGY_MOST, energy + more);
        frenzyLeft = FRENZY_LASTS;
    }

    /**
     * What the tree does to a use just made: what she threw or fired goes out more times in a fan, goes through more, jumps on, bounces, and
     * lands harder for a rest before it; and a swing may strike again a beat later.
     */
    private void afterUse(int before, boolean rested) {
        int more = Math.min(4, skillOf(RunSkills.E_PROJECTILES)), made = shots.size();
        if (more > 0 && made - before <= 6) {
            for (int index = before; index < made; index++) {
                Shot shot = shots.get(index);
                if (!shot.hers || shot.pets) continue;
                for (int copy = 1; copy <= more; copy++) {
                    twin(shot, (copy + 1) / 2 * FAN * (copy % 2 == 1 ? 1 : -1));
                    fanned++;
                }
            }
        }
        int pierce = Math.max(0, skillOf(RunSkills.E_PIERCE)), hops = Math.max(0, skillOf(RunSkills.E_CHAIN)), bounces = Math.max(0, skillOf(RunSkills.E_RICOCHET));
        int boost = rested ? Math.max(0, skillOf(RunSkills.E_CHARGE)) : 0;
        for (int index = before; index < shots.size(); index++) {
            Shot shot = shots.get(index);
            if (!shot.hers || shot.pets) continue;
            if (shot.pierce < 99) shot.pierce += pierce;
            shot.hops += hops;
            shot.bounces += bounces;
            shot.boost += boost;
        }
        // (Her hands may be empty by now: a gun or a book used up.)
        if (weapon >= 0 && weapon < RunArms.COUNT && RunArms.way(weapon) == RunArms.SWUNG && chance(skillOf(RunSkills.E_ECHO))) {
            echoes++;
            // What the use swept is swept again; a swing of the brush or the sword, which sweeps as it goes, swings again when it is done.
            if (!swept.isEmpty()) {
                echoBoxes.clear();
                echoBoxes.addAll(swept);
                echoLeft = ECHO_BEAT;
            } else if (swing > 0) {
                echoSwing = true;
            }
        }
    }

    /** One of hers again, turned a little: one of a fan. */
    private Shot twin(Shot shot, double turn) {
        Shot twin = new Shot(shot.kind);
        float c = (float) Math.cos(turn), s = (float) Math.sin(turn);
        twin.hers = true;
        twin.x = shot.x;
        twin.y = shot.y;
        twin.across = shot.across * c - shot.down * s;
        twin.down = shot.across * s + shot.down * c;
        twin.left = shot.left;
        twin.spin = shot.spin;
        twin.through = shot.through;
        twin.by = shot.by;
        twin.of = shot.of;
        twin.pierce = shot.pierce;
        twin.chain = shot.chain;
        twin.high = shot.high;
        twin.wipes = shot.wipes;
        twin.element = shot.element;
        shots.add(twin);
        return twin;
    }

    /** One of hers has hit something and has jumps left: it jumps on to the nearest other creature still standing, if one is near. */
    private void hop(Shot shot, Foe hit) {
        Foe next = null;
        float best = 6 * 6;
        for (Foe foe : foes) {
            if (foe == hit || foe.flat || absent(foe) || foe.kind == RunLevels.TARGET) continue;
            float across = foe.x + foe.wide() / 2 - shot.x, down = foe.y + foe.tall() / 2 - shot.y, far = across * across + down * down;
            if (far < best) {
                best = far;
                next = foe;
            }
        }
        if (next == null) return;
        float across = next.x + next.wide() / 2 - shot.x, down = next.y + next.tall() / 2 - shot.y, far = Math.max(.3f, (float) Math.hypot(across, down));
        float speed = Math.max(9, (float) Math.hypot(shot.across, shot.down));
        // (What falls as it flies jumps on as a wave, straight at it.)
        int kind = shot.kind == BOOK || shot.kind == INKBALL || shot.kind == PAGE ? SLASH : shot.kind;
        Shot jump = shoot(kind, true, shot.x, shot.y, across / far * speed, down / far * speed, far / speed + .12f);
        jump.by = shot.by;
        jump.of = shot.of;
        jump.high = Math.max(.3f, shot.high);
        jump.element = shot.element;
        jump.boost = shot.boost;
        jump.hops = shot.hops - 1;
        jump.last = hit;
        shot.hops = 0;
        hopped++;
    }

    /** What was thrown at her goes back the way she faces, hers now. */
    private void sendBack(Shot shot) {
        shot.hers = true;
        shot.across = (facingLeft ? -1 : 1) * Math.max(9, Math.abs(shot.across) * 1.6f);
        shot.down = Math.min(shot.down, 0);
        shot.left = 1;
    }

    /** How long she can hold her breath. */
    float holdBreath() { return HOLD_BREATH * (1 + Math.max(0, skillOf(RunSkills.E_SWIM)) / 100f); }

    /** How many times as long her forms and ribbons last, by the tree. */
    private float forms() { return 1 + Math.max(0, skillOf(RunSkills.E_FORMS)) / 100f; }

    /** What a blow of hers counts for against a creature, in tenths: her weapon's blows, made harder by her level. */
    private int dealt() {
        // (Attack and the tree's attack make every blow harder; intelligence, those of tomes, staffs and wands.)
        int thing = thingOf(with);
        boolean minded = thing == BOOK || thing == TABLET || thing == RunArms.STAFF || thing == RunArms.WAND;
        float more = 1 + (perks.skills.stats[RunSkills.ATK] + skillOf(RunSkills.E_ATK_PCT) + (minded ? intelligence() : 0) + perks.mightPct + metered()
                + (hp * 3 <= maxHp() ? skillOf(RunSkills.E_LAST_STAND) : 0) - (inked() ? curseWeak() : 0)) / 100f;
        return Math.max(1, Math.round(BLOW * power() * RunLoot.power(perks.level) * Math.max(.1f, more) * (divineLeft > 0 ? 3 : 1)));
    }

    /** Her intelligence: the tree's, with its percent, and what an active called on adds to that. */
    int intelligence() { return RunSkills.intelligence(perks.skills, skillOf(RunSkills.E_INT_PCT) - perks.skills.effects[RunSkills.E_INT_PCT]); }

    /** How much less a hurt costs, in percent: the tree's (a strong passive may make it more), and an active's. */
    int defencePercent() { return RunSkills.defencePercent(skillOf(RunSkills.E_DEF_PCT) + (skillOf(RunSkills.E_IRON) > 0 ? dodgeRaw() : 0)); }

    /** How much of energy spent becomes divine power, in percent, now. */
    int dpMaking() { return Math.max(0, skillOf(RunSkills.E_DP)); }

    /** Blows that landed, for showing: where, how much, whether a great one, and how long ago. */
    private final List<float[]> hits = new ArrayList<>();

    List<float[]> hits() { return hits; }

    // What she wears, felt

    /** Energy, for her specials (what a held button does): how much she has at most, how fast it comes back, what a special costs, and what a blow gives. */
    static final float ENERGY_MOST = 100, ENERGY_BACK = 12, SPECIAL_COST = 34, SPARK = 2;
    /** Her shield bar: how long after a hurt before it fills again, and how long each piece takes. */
    static final float WARD_WAIT = 3, WARD_FILL = 5;
    /** How long ink-poison lasts on a creature, burning, and freezing; and how often the first two take a blow. */
    static final float POISON_LASTS = 4, BURN_LASTS = 2, FREEZE_LASTS = 1.4f, POISON_EVERY = 1, BURN_EVERY = .5f;
    /** What a blow of ink-poison, of burning, and of thorns is made with. */
    static final int POISONED = -20, BURNT = -21, THORNY = -22;
    /** HP the last hurt took. */
    private int lastLost;

    int lastLost() { return lastLost; }

    private float energy = ENERGY_MOST, ward, wardWait, regenClock, critAge = 9, dodgeAge = 9, shrugAge = 9, regenAge = 9;
    private int regens, dodges, shrugs, crits, wardHurts, poisons, burns, freezes, leeches, thorned, specialsUsed;
    private float critX, critY;

    /** How much of an effect what she wears gives her, with what is in her hands. */
    int gear(int effect) {
        if (effect == RunLoot.DODGE && skillOf(RunSkills.E_IRON) > 0) return 0;
        int sum = perks.worn.of(effect, RunLoot.classOf(weapon)) + skill(effect) + (effect == RunLoot.THORNS ? perks.thorns : 0);
        return GEAR_MOST[effect] > 0 ? Math.min(GEAR_MOST[effect], sum) : sum;
    }

    /** The most some of gear's effects come to with the tree's added: never a rest of nothing, a hurt slipped for certain, a special for free. */
    private static final int[] GEAR_MOST = new int[RunLoot.EFFECTS];

    static {
        GEAR_MOST[RunLoot.HASTE] = 75;
        GEAR_MOST[RunLoot.DODGE] = 60;
        GEAR_MOST[RunLoot.SHRUG] = 60;
        GEAR_MOST[RunLoot.THRIFT] = 80;
        GEAR_MOST[RunLoot.SPECIAL] = 80;
        GEAR_MOST[RunLoot.CRIT] = 90;
        GEAR_MOST[RunLoot.LEECH] = 60;
        GEAR_MOST[RunLoot.WARD_HIT] = 80;
    }

    /** Her chance to slip a hurt, before iron reflexes make it defence. */
    private int dodgeRaw() { return Math.min(GEAR_MOST[RunLoot.DODGE], perks.worn.of(RunLoot.DODGE, RunLoot.classOf(weapon)) + skill(RunLoot.DODGE)); }

    /** How much of one of the tree's effects she has now: what is felt whatever she holds, what is felt with what is in her hands, and the actives called on. */
    int skillOf(int effect) { return perks.skills.with(effect, RunSkills.classOfWeapon(weapon)) + activeEffect(effect); }

    /** What her skill tree adds to one of the effects gear has: its passives, and the actives called on just now. */
    private int skill(int effect) {
        int[] from = SKILL_OF[effect];
        if (from == null) return 0;
        int sum = 0;
        for (int index = 0; index < from.length; index += 2) sum += skillOf(from[index]) * from[index + 1];
        return sum;
    }

    /** For each of gear's effects, which of the tree's effects add to it and how many times over. */
    private static final int[][] SKILL_OF = new int[RunLoot.EFFECTS][];

    static {
        SKILL_OF[RunLoot.CRIT] = new int[]{RunSkills.E_CRIT, 1};
        SKILL_OF[RunLoot.CRITX] = new int[]{RunSkills.E_CRITX, 1};
        SKILL_OF[RunLoot.HASTE] = new int[]{RunSkills.E_HASTE, 1, RunSkills.E_COMBO, 5};
        SKILL_OF[RunLoot.LEECH] = new int[]{RunSkills.E_LEECH, 1};
        SKILL_OF[RunLoot.POISON] = new int[]{RunSkills.E_POISON, 1};
        SKILL_OF[RunLoot.BURN] = new int[]{RunSkills.E_BURN, 1};
        SKILL_OF[RunLoot.FREEZE] = new int[]{RunSkills.E_FREEZE, 1};
        SKILL_OF[RunLoot.REACH] = new int[]{RunSkills.E_REACH, 1};
        SKILL_OF[RunLoot.AREA] = new int[]{RunSkills.E_AREA, 1};
        // (More pierce, and more of what is thrown, are the tree's own: see afterUse.)
        SKILL_OF[RunLoot.SHRUG] = new int[]{RunSkills.E_SHRUG, 1};
        SKILL_OF[RunLoot.AIR] = new int[]{RunSkills.E_AIR, 1};
        SKILL_OF[RunLoot.PLUSES] = new int[]{RunSkills.E_PLUSES, 1};
        // (Gear draws pluses from tenths of a square further; the tree from whole squares.)
        SKILL_OF[RunLoot.MAGNET] = new int[]{RunSkills.E_MAGNET, 10};
        SKILL_OF[RunLoot.SPARK] = new int[]{RunSkills.E_SPARK, 1};
        SKILL_OF[RunLoot.SPECIAL] = new int[]{RunSkills.E_SPECIAL, 1};
        SKILL_OF[RunLoot.WARD_HIT] = new int[]{RunSkills.E_WARD_HIT, 1};
        SKILL_OF[RunLoot.DODGE] = new int[]{RunSkills.E_DODGE, 1};
        SKILL_OF[RunLoot.THORNS] = new int[]{RunSkills.E_THORNS, 1};
        SKILL_OF[RunLoot.WARD] = new int[]{RunSkills.E_SHIELD, 1};
        SKILL_OF[RunLoot.THRIFT] = new int[]{RunSkills.E_SPECIAL_COST, 1};
        // (Life back: the tree counts HP a second, gear halves of a heart a minute; one HP a second is six halves a minute.)
        SKILL_OF[RunLoot.REGEN] = new int[]{RunSkills.E_REGEN, 6};
    }

    // Active skills

    /** Up to three active skills in her slots, how long each is called on for, and what it costs and how long until it can be called again. */
    static final int SLOTS = 3;
    static final float ACTIVE_LASTS = RunSkills.ACTIVE_LASTS, ACTIVE_REST = RunSkills.ACTIVE_REST, ACTIVE_COST = RunSkills.ACTIVE_COST;
    private final float[] activeLeft = new float[SLOTS], activeRest = new float[SLOTS];
    /** Divine power: made of energy spent while an active that makes it is called on; full, her next blows are divine for a while. */
    static final float DP_MOST = 100, DIVINE_LASTS = 6;
    private float dp, divineLeft;
    private int activesUsed, divines;

    float dp() { return dp; }
    float divineLeft() { return divineLeft; }
    int activesUsed() { return activesUsed; }
    int divines() { return divines; }
    float activeLeft(int slot) { return activeLeft[slot]; }
    float activeRest(int slot) { return activeRest[slot]; }

    /** The active skill in a slot, or null. */
    RunSkills.Notable active(int slot) { return slot < perks.slotted.size() ? perks.slotted.get(slot) : null; }

    /** She calls on an active skill: if it is there, rested, and she has the energy. Whether she did. */
    boolean callOn(int slot) {
        RunSkills.Notable skill = active(slot);
        if (skill == null || activeRest[slot] > 0 || !canSpend(ACTIVE_COST) || state != PLAYING) return false;
        spend(ACTIVE_COST);
        activeLeft[slot] = ACTIVE_LASTS;
        activeRest[slot] = ACTIVE_REST;
        activesUsed++;
        return true;
    }

    /** What the actives called on just now add to one of the tree's effects. */
    private int activeEffect(int effect) {
        int sum = 0;
        for (int slot = 0; slot < SLOTS; slot++) {
            RunSkills.Notable skill = active(slot);
            if (skill != null && activeLeft[slot] > 0) sum += skill.of(effect);
        }
        return sum;
    }

    /** Whether she can spend so much energy: or, with blood ink, the HP it comes to, keeping the last of her HP. */
    boolean canSpend(float cost) { return skillOf(RunSkills.E_BLOOD) > 0 ? hp - bloodOf(cost) >= RunSkills.BLOOD_KEEPS : energy >= cost; }

    /** She spends so much energy, or with blood ink its HP; either way, divine power may be made of it. */
    private void spend(float cost) {
        if (skillOf(RunSkills.E_BLOOD) > 0) {
            hp -= bloodOf(cost);
            bled++;
        } else {
            energy -= cost;
        }
        noteSpent(cost);
    }

    /** The HP so much energy comes to, with blood ink: one for every two. */
    static int bloodOf(float cost) { return (int) Math.ceil(cost / 2); }

    /** Energy spent: while an active that makes divine power is called on, a part of it becomes divine power; full, it is let go. */
    private void noteSpent(float spent) {
        int making = dpMaking();
        if (making <= 0) return;
        dp = Math.min(DP_MOST, dp + spent * making / 100f);
        if (dp >= DP_MOST) {
            dp = 0;
            divineLeft = DIVINE_LASTS;
            divines++;
        }
    }

    private void actives(float seconds) {
        for (int slot = 0; slot < SLOTS; slot++) {
            if (activeLeft[slot] > 0) activeLeft[slot] -= seconds;
            if (activeRest[slot] > 0) activeRest[slot] -= seconds;
        }
        if (divineLeft > 0) divineLeft -= seconds;
    }

    float energy() { return energy; }
    float specialCost() { return SPECIAL_COST * (1 - gear(RunLoot.THRIFT) / 100f); }
    float ward() { return ward; }
    int wardMost() { return gear(RunLoot.WARD) + (gear(RunLoot.WARD_HIT) > 0 && gear(RunLoot.WARD) == 0 ? 1 : 0); }
    /** Counts that only go up: life come back; dodges; hurts shrugged off; great blows; hurts the shield bar took; poisonings, burnings and freezings begun; halves of a heart back from blows; blows of thorns; specials used. */
    int regens() { return regens; }
    int dodges() { return dodges; }
    int shrugs() { return shrugs; }
    int crits() { return crits; }
    int wardHurts() { return wardHurts; }
    int poisons() { return poisons; }
    int burns() { return burns; }
    int freezes() { return freezes; }
    int leeches() { return leeches; }
    int thorned() { return thorned; }
    int specialsUsed() { return specialsUsed; }
    /** How long since the last of these, for showing them; and where the last great blow landed. */
    float critAge() { return critAge; }
    float dodgeAge() { return dodgeAge; }
    float shrugAge() { return shrugAge; }
    float regenAge() { return regenAge; }
    float critX() { return critX; }
    float critY() { return critY; }

    /** What she wears is changed in the middle of a land: it is felt at once, the shield bar no fuller than it now can be. */
    void wear(RunLoot.Worn worn) {
        perks.worn = worn;
        ward = Math.min(ward, wardMost());
    }

    /** What she wears does, as time goes: energy and life come back, the shield bar fills, and what burns or is poisoned or frozen goes on being so. */
    private void wearing(float seconds) {
        energy = Math.min(ENERGY_MOST, energy + ENERGY_BACK * Math.max(.2f, 1 + (gear(RunLoot.ENERGY) + intelligence() + skillOf(RunSkills.E_ENERGY)) / 100f) * seconds);
        critAge += seconds;
        dodgeAge += seconds;
        shrugAge += seconds;
        regenAge += seconds;
        drip(seconds);
        sinceMeter += seconds;
        if (chargedLeft > 0) chargedLeft -= seconds;
        if (frenzyLeft > 0) frenzyLeft -= seconds;
        int most = wardMost();
        if (ward > most) ward = most;
        if (wardWait > 0) wardWait -= seconds;
        else if (ward < most) ward = Math.min(most, ward + seconds / WARD_FILL * (1 + gear(RunLoot.WARD_FAST) / 100f));
        int regen = gear(RunLoot.REGEN);
        if (regen > 0 && hp > 0 && hp < maxHp()) {
            regenClock += seconds * regen / 60f;
            if (regenClock >= 1) {
                regenClock -= 1;
                hp = Math.min(maxHp(), hp + HALF);
                regens++;
                regenAge = 0;
            }
        } else {
            regenClock = 0;
        }
        for (int index = hits.size() - 1; index >= 0; index--) if ((hits.get(index)[4] += seconds) > .9f) hits.remove(index);
        for (Foe foe : foes) {
            foe.struck += seconds;
            if (foe.flat || foe.away) {
                foe.poisoned = foe.burning = foe.frozen = 0;
                continue;
            }
            boolean waits = RunLevels.boss(foe.kind);
            if (foe.frozen > 0) {
                foe.frozen -= seconds;
                foe.dazed = Math.max(foe.dazed, Math.min(.12f, Math.max(0, foe.frozen)));
            }
            // (Those who wait take a blow of it half as often.)
            if (foe.poisoned > 0) {
                foe.poisoned -= seconds;
                if ((foe.poisonTick += seconds) >= POISON_EVERY * (waits ? 2 : 1)) {
                    foe.poisonTick = 0;
                    if (open(foe)) beat(foe, POISONED);
                }
            }
            if (!foe.flat && foe.burning > 0) {
                foe.burning -= seconds;
                if ((foe.burnTick += seconds) >= BURN_EVERY * (waits ? 2 : 1)) {
                    foe.burnTick = 0;
                    if (open(foe)) beat(foe, BURNT);
                }
            }
        }
    }

    /** A chance, in a hundred, comes off (a roll only when there is a chance, so that what drops is as it would be without it). */
    private boolean chance(int inHundred) { return inHundred > 0 && lootRoll(100) < inHundred; }

    /** What she wears does as a blow of hers lands on a creature: how many blows it counts for, harder for her gear and now and then a great blow. */
    private int geared(Foe foe, int dealt, boolean waits) {
        if (thingOf(with) == UNARMED) return dealt;
        if (!waits) {
            int power = gear(RunLoot.POWER);
            if (power > 0) {
                gearSurplus += dealt * power / 100f;
                int more = (int) gearSurplus;
                gearSurplus -= more;
                dealt += more;
            }
        }
        if (chance(gear(RunLoot.CRIT))) {
            crits++;
            critAge = 0;
            critX = foe.x + foe.wide() / 2;
            critY = foe.y;
            dealt = waits ? dealt + 1 : Math.round(dealt * (2 + gear(RunLoot.CRITX) / 100f));
        }
        // And after it: energy; a piece of shield back, half a heart back, ink-poison, burning, freezing, by chance.
        energy = Math.min(ENERGY_MOST, energy + SPARK + gear(RunLoot.SPARK));
        if (ward < wardMost() && chance(gear(RunLoot.WARD_HIT))) ward = Math.min(wardMost(), (float) Math.floor(ward) + 1);
        if (hp > 0 && hp < maxHp() && chance(gear(RunLoot.LEECH))) {
            hp = Math.min(maxHp(), hp + HALF);
            leeches++;
            regenAge = 0;
        }
        if (chance(gear(RunLoot.POISON))) {
            if (foe.poisoned <= 0) poisons++;
            foe.poisoned = POISON_LASTS;
        }
        if (chance(gear(RunLoot.BURN))) {
            if (foe.burning <= 0) burns++;
            foe.burning = BURN_LASTS;
        }
        if (!waits && chance(gear(RunLoot.FREEZE))) {
            if (foe.frozen <= 0) freezes++;
            foe.frozen = FREEZE_LASTS;
        }
        return dealt;
    }

    private float gearSurplus;

    /** Before a hurt lands: thorns on whatever is touching her; then she may dodge it, shrug it off, or her shield bar takes it. Whether one of those did. */
    private boolean gearTakes() {
        int thorns = gear(RunLoot.THORNS);
        if (thorns > 0) {
            for (Foe foe : new ArrayList<>(foes)) {
                if (foe.flat || absent(foe) || foe.kind == RunLevels.TARGET || RunLevels.boss(foe.kind) && !open(foe)) continue;
                if (foe.x > x + WIDE + .6f || foe.x + foe.wide() < x - .6f || foe.y > y + TALL + .6f || foe.y + foe.tall() < y - .6f) continue;
                for (int blow = 0; blow < thorns && !foe.flat; blow++) {
                    beat(foe, THORNY);
                    thorned++;
                }
            }
        }
        wardWait = WARD_WAIT;
        if (chance(gear(RunLoot.DODGE))) {
            dodges++;
            dodgeAge = 0;
            return true;
        }
        if (chance(gear(RunLoot.SHRUG))) {
            shrugs++;
            shrugAge = 0;
            return true;
        }
        if (ward >= 1) {
            ward -= 1;
            wardHurts++;
            return true;
        }
        return false;
    }

    /** How far a thing she fights with reaches, and how wide it bursts, and how many it goes through, with what she wears. */
    private float reachGear(int kind, int level) { return RunArms.reach(kind, level) * (1 + gear(RunLoot.REACH) / 100f); }

    private float areaGear(int kind, int level) { return RunArms.area(kind, level) * (1 + gear(RunLoot.AREA) / 100f); }

    private int piercesGear(int kind, int level) { return RunArms.pierces(kind, level) + gear(RunLoot.PIERCE); }

    /** How many more blows a creature of this land takes, and how many more halves of a heart a hurt costs in it: by the land's level. */
    private int tough, bite;

    int tough() { return tough; }
    int bite() { return bite; }

    /** A slash wave, from a blade or a brush the smith has worked on. */
    static final int SLASH = 29;
    /**
     * The four won in the black arena, and a ball of the cannon's ink as it flies. What a
     * blow of each is made with. And, of them, counts that only go up, for whoever shows
     * the game: a katana's draws, how many in a row the last was, its dashes and its cuts
     * downward; a lance's thrusts and jets; a cannon's great balls; a whip's lashes and
     * spins. The line of the katana's last dash, and for how much longer it waits to be cut.
     */
    static final int KATANA = RunArms.KATANA, LANCE = RunArms.LANCE, CANNON = RunArms.CANNON, WHIP = RunArms.WHIP, INKBALL = 30;
    static final int GREATSWORD = RunArms.GREATSWORD, STAFF = RunArms.STAFF, MACE = RunArms.MACE, HAMMER = RunArms.HAMMER, WAND = RunArms.WAND, SCYTHE = RunArms.SCYTHE;
    /** What a blow of each of the six is made with. */
    static final int GREAT = -13, MACED = -14, HAMMERED = -15, BOLTED = -16, SUMMONED = -17, REAPED = -18;
    /** Knuckles: the thing, and what its blows are made with (a punch, an uppercut, the haymaker); and counts of them. */
    static final int KNUCKLES = RunArms.KNUCKLES, PUNCHED = -19, UPPERCUT = -23, HAYMAKER = -24;
    private int punches, punchChain, uppercuts, dashPunches, haymakers;
    private float sincePunch = 9;

    int punches() { return punches; }
    int punchChain() { return punchChain; }
    int uppercuts() { return uppercuts; }
    int dashPunches() { return dashPunches; }
    int haymakers() { return haymakers; }
    private int greats, summons, smashes, slams, bolts, reaps, whirls, quakesMade;
    private float whirlLeft, reapLeft;
    /** Where ink splashed as blows landed: the place of each, and how long since. */
    private final List<float[]> splashes = new ArrayList<>();

    List<float[]> splashes() { return splashes; }

    int greats() { return greats; }
    int summons() { return summons; }
    int smashes() { return smashes; }
    int slams() { return slams; }
    int bolts() { return bolts; }
    int reaps() { return reaps; }
    static final int CUT = -9, LANCED = -10, SHELLED = -11, WHIPPED = -12;
    private int cuts, chain, dashes, airCuts, thrusts, jets, shells, lashes, spins;
    private float sinceCut = 9, dashFrom, dashTo, dashY, dashLeft, spinLeft;

    int cuts() { return cuts; }
    int chain() { return chain; }
    int dashes() { return dashes; }
    int airCuts() { return airCuts; }
    int thrusts() { return thrusts; }
    int jets() { return jets; }
    int shells() { return shells; }
    int lashes() { return lashes; }
    int spins() { return spins; }
    float dashFrom() { return dashFrom; }
    float dashTo() { return dashTo; }
    float dashY() { return dashY; }
    float dashLeft() { return dashLeft; }

    /**
     * What the smith's work leaves in the air for a moment: bursts, each where it is, how
     * wide, what made it and how long ago; and where a book's pages burn, each with how
     * wide, for how much longer and how long until it next takes what stands in it. And
     * counts that only go up: waves sent, and blots that jumped on.
     */
    private final List<float[]> booms = new ArrayList<>(), fires = new ArrayList<>();
    private int waves, chains;

    List<float[]> booms() { return booms; }
    List<float[]> fires() { return fires; }
    int waves() { return waves; }
    int chains() { return chains; }

    /** Her tablet-book and her ring of keys, which are hers to begin a land with; a page of the one, a key of the other, as they fly; and a blow from the panel the tablet-book opens. */
    static final int TABLET = RunArms.TABLET, KEYS = RunArms.KEYS, PAGE = 27, THROWN_KEY = 28;
    private int owned, newly, books, previous = UNARMED, finds, lastFound = UNARMED, refills, lastRefill = UNARMED, switches, duds, keyThrows, panels, fans, doubled, laidDown = UNARMED;
    private float boost, disarmed, heldFor, sinceDud = 99, panelX, panelY;
    private boolean heavied, panelLeft;

    /** What is in her backpack now, as bits; what of it she found in this land; and how many books she has left to throw. */
    int owned() { return owned; }
    int newly() { return newly; }
    int books() { return books; }
    /** What she held before what she holds; for how much longer what she holds is the stronger for a find; and for how much longer her hands are empty. */
    int previous() { return previous; }
    float boost() { return boost; }
    float disarmed() { return disarmed; }
    /** For how long the button has been held with something that does a heavy thing when it is; and counts that only go up. */
    float heldFor() { return heldFor; }
    int finds() { return finds; }
    int lastFound() { return lastFound; }
    int refills() { return refills; }
    int lastRefill() { return lastRefill; }
    int switches() { return switches; }
    int duds() { return duds; }
    int panels() { return panels; }
    int fans() { return fans; }
    int doubled() { return doubled; }
    /** Where the last panel opened: its near edge, its top, and whether it opened to the left. */
    float panelX() { return panelX; }
    float panelY() { return panelY; }
    boolean panelLeftward() { return panelLeft; }

    /** Whether something in her backpack can be taken in hand: it is there, and it is not empty. */
    boolean usable(int kind) {
        return kind >= 0 && kind < RunArms.COUNT && (owned >> kind & 1) != 0 && (kind != GUN || rounds > 0) && (kind != BOOK || books > 0);
    }

    /** A weapon found is taken in hand in the middle of a land: into her backpack as its kind, counting for more, and in her hands. */
    void hold(int code) {
        int kind = RunArms.kindOf(code);
        perks.item = code;
        owned |= 1 << kind;
        if (state == PLAYING && disarmed <= 0 && !ranging) {
            previous = weapon;
            weapon = kind;
            heldFor = 0;
            heavied = false;
        }
    }

    /** She takes something else out of her backpack: at once, whatever she is doing. Returns whether she did. */
    boolean switchTo(int kind) {
        if (!usable(kind) || kind == weapon || disarmed > 0 || ranging || state != PLAYING) return false;
        previous = weapon;
        weapon = kind;
        switches++;
        heldFor = 0;
        heavied = false;
        return true;
    }

    /** The other of the last two she has used; or, with no other, the next thing in her backpack. */
    boolean swap() { return switchTo(nextUp()); }

    /** What a swap would put in her hands, or nothing. */
    int nextUp() {
        if (previous != weapon && usable(previous)) return previous;
        for (int step = 1; step < RunArms.COUNT; step++) {
            int kind = (Math.max(0, weapon) + step) % RunArms.COUNT;
            if (kind != weapon && usable(kind)) return kind;
        }
        return UNARMED;
    }

    /** What lay about is hers: one she has not got goes into her backpack and into her hands; one she has gives ink, or books, or makes what she holds stronger for a while. */
    private void found(int kind) {
        if ((owned >> kind & 1) == 0) {
            owned |= 1 << kind;
            newly |= 1 << kind;
            finds++;
            lastFound = kind;
            if (kind == GUN) rounds = perks.shots;
            if (kind == BOOK) books = RunArms.BOOKS;
            previous = weapon;
            weapon = kind;
            return;
        }
        refills++;
        lastRefill = kind;
        if (kind == GUN) rounds = Math.min(perks.shots * 2, rounds + RunArms.INK);
        else if (kind == BOOK) books = Math.min(RunArms.BOOKS * 2, books + RunArms.PAGES);
        else boost = RunArms.BOOSTED;
    }

    /** What she holds is used up: she holds what she begins a land with, or nothing. */
    private void emptied() {
        previous = weapon;
        weapon = perks.main != weapon && usable(perks.main) ? perks.main : UNARMED;
    }
    static final int GUN_SHOTS = 16;
    /** A blow from the panel of translation that her tablet-book opens. */
    private static final int PANELLED = -7;
    /** How long a swing of the brush or the sword lasts. */
    static final float SWING = .18f;
    /** How long before it throws a sign that throws knives can be seen to raise one. */
    static final float WARNING = .8f;

    private static final float RUN = 6.2f, PUSH = 40, PUSH_IN_AIR = 24, DRAG = 50;
    /** How fast she climbs a vine; and whether she is on one. */
    static final float CLIMB = 5.5f;
    private boolean climbing;

    boolean climbing() { return climbing; }

    private static final float GRAVITY = 42, LEAP = 15.5f, SHORT_LEAP = 6, FALL = 22, BOUNCE = 10, SPRING = 22.5f;
    /** A jump still counts this long after she has run off an edge, and this long before she lands. */
    private static final float LATE = .08f, EARLY = .1f;
    private static final float SAFE = 1.2f, STEP = 1 / 60f;
    /** How fast each sign walks, how wide and tall it is, and how high a divide hops. */
    private static final float[] SPEED_SIGNS = {1.6f, 1.3f, 0, 1.9f, 0, 0, 0, 2.4f, 1.6f, 2.2f, 1, 2.7f, 0, 2.4f, 0, 0, 1.5f, 0, 3.6f, 3,
            1.6f, 2, 1.4f, 2, 2.4f, 2, 2.2f, 1.2f, 0, 6.5f, 1.2f, 3, 1.8f, 2, 2.6f, 2.2f, 3, 1.3f, 1.3f, 2.4f};
    private static final float[] FOE_WIDE_SIGNS = {.8f, .85f, .8f, 1.8f, .8f, .8f, .9f, .8f, .8f, .9f, 1.3f, .7f, 1, .7f, .9f, .8f, .8f, .8f, 2.2f, .9f,
            .9f, .8f, .8f, .9f, .9f, 1.6f, 2, 1.6f, .9f, 2.6f, 1.5f, 1.9f, .9f, 1, 1.2f, .9f, .5f, .8f, .9f, .8f};
    private static final float[] FOE_TALL_SIGNS = {.55f, .85f, .8f, 2.2f, .55f, .8f, 1.5f, 1.3f, .7f, .7f, .45f, 1.2f, .9f, 1.2f, 1.5f, 1.3f, 1.3f, 1.4f, 1.9f, 1.5f,
            1.4f, 1.4f, 1.3f, 1.3f, 1.3f, 1.3f, 1.5f, 1.7f, .9f, 1.5f, 1.2f, .8f, .7f, .8f, .8f, .9f, .45f, .9f, .9f, 1.15f};
    /** And the unique creatures' after them. */
    private static final float[] SPEED = grown(SPEED_SIGNS, RunUniques.SPEED), FOE_WIDE = grown(FOE_WIDE_SIGNS, RunUniques.WIDE), FOE_TALL = grown(FOE_TALL_SIGNS, RunUniques.TALL);

    private static float[] grown(float[] signs, float[] uniques) {
        float[] all = java.util.Arrays.copyOf(signs, RunUniques.FIRST + RunUniques.COUNT);
        System.arraycopy(uniques, 0, all, RunUniques.FIRST, RunUniques.COUNT);
        return all;
    }

    /** Tsubakieh: how near she comes before she raises her bat, how long she holds it up, how far it reaches, and how long she is open after. */
    private static final float BAT_NEAR = 1.7f, BAT_RAISED = .55f, BAT_REACH = 1.5f, BAT_SPENT = .8f;
    /** Illya: how far off she keeps, how fast she drifts to keep it, and how far back from where she began she will go. */
    private static final float ILLYA_KEEPS = 4.2f, ILLYA_DRIFT = 2.4f, ILLYA_ROOM = 11.5f;
    private static final float HOP_HIGH = 1.9f, HOP_EVERY = 1.3f, STUNNED = 1.1f;
    /** How near she must be for a knife to be thrown at her, how often one is, and how fast it flies; and the same for a dot and for Zero's books. */
    private static final float THROW_REACH = 5.2f, THROW_EVERY = 2.6f, THROWN = 4.2f;
    private static final float DOT_REACH = 6, DOT_SPEED = 6.5f, BOOK_REACH = 9, BOOK_EVERY = 3.4f, BOOK_WEIGHT = 30;

    /** Something flying through the air: hers, or thrown at her. Its place is its middle. */
    static final class Shot {
        /** One of hers that goes on through whatever it meets. */
        boolean through;
        /** One that a pet threw. */
        boolean pets;
        final int kind;
        boolean hers;
        float x, y, across, down, left, age;
        /** How fast a bat was thrown: it slows, stops and comes back at that speed. */
        float spin;
        /**
         * What the smith's work has made of one of hers. What the blow it deals is made
         * with, which for a wave is the thing it flew from; that thing, for a wave; how far
         * over and under its middle it reaches; how many more things it goes on through;
         * the last it met, which it does not meet twice; how many more times a blot jumps
         * on; whether it wipes away what is thrown at her; and whether it has burst.
         */
        int by, of = -1, pierce, chain;
        float high;
        Foe last;
        boolean wipes, spent;
        /** A wand's bolt: its element. */
        int element = -1;
        /**
         * What her skill tree has made of one of hers: how many more creatures it jumps on to, how many more walls it bounces off, and how
         * much harder its blow lands, in percent, for being let fly after a rest.
         */
        int hops, bounces, boost;

        Shot(int kind) {
            this.kind = kind;
            by = kind;
        }
    }

    /**
     * The creatures of the lands are four fifths of the size they were drawn at first, so
     * that there is room for five times as many of them: those that walk, hop, throw, shoot
     * and fly. Those who wait, those met in the middle of a land, a word that falls and a
     * paper target are the size they were; and so is an outsized one, which is its kind's
     * old size and more.
     */
    static final float SMALL = .8f;
    /** How near she must come before one put in to make up a land's numbers is about its business: until then it keeps still. */
    static final float WAKES = 16;

    static boolean little(int kind) { return !RunLevels.boss(kind) && kind != RunLevels.TARGET && kind != RunLevels.WORD && !RunUniques.is(kind); }

    /**
     * How much further than it reaches what she throws or swings takes a small one: as far
     * as the size it used to be, to either side and over it, so that nothing of hers that
     * would have met it goes by it now.
     */
    private static float spare(Foe foe, boolean across) {
        return foe.size >= 1 ? 0 : (across ? FOE_WIDE[foe.kind] * .5f : FOE_TALL[foe.kind]) * (1 - foe.size);
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
        /**
         * For those who wait: how many pieces of her shield are left and how many it has
         * whole; for how much longer it is broken; for how much longer it is forming; and
         * how many blows have landed on her since it broke.
         */
        float shield, shieldMost, broken, forming;
        int taken;
        /** Whether it was put into a land to make up its numbers: such a one keeps still while she is far off. And whether as one of a swarm. */
        boolean crowd, swarm;
        /** For how much longer a key is stuck in it: the next blow on it that is not a key counts twice. */
        float unlocked;
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
        /** Whether what it drops has dropped. */
        boolean dropped;
        /** For how much longer it is ink-poisoned, burning, frozen; and how long since it last took a blow of poison or of fire. */
        float poisoned, burning, frozen, poisonTick, burnTick;
        /** Its colour of toughness (0 as it is drawn, then red, purple, gold, black ink); and how long since a blow last landed on it. */
        int tier;
        float struck = 9;
        /** How fast one that walks is falling, having nothing under it; and which way the last blow knocked it. */
        float dropping, knocked;
        /**
         * A shield it carries, if any: how many more strong blows it takes, how many it took whole, what a blow must count for to tell
         * on it, which of the four it is (book cover, steel, gold, black ink), and how long since a blow last met it.
         */
        int guard, guardMost, guardNeeds, guardKind;
        float guardMet = 9;
        /**
         * Reaching her: how long she has stood above it, where it stands to begin with, what it is doing about her (0 nothing, 1 crouched
         * to leap, 2 in the air, 3 coming down) and for how long, where its leap is from and to, and how fast it falls.
         */
        float perch, base, leap, leapFX, leapFY, leapTX, leapTY, fall;
        int leaping;
        /** For how much longer a blow of hers has knocked it off its feet: it does everything at a crawl, and can still be hit. */
        float stunned;

        Foe(int kind) {
            this.kind = kind;
            if (little(kind)) size = SMALL;
        }

        float wide() { return FOE_WIDE[kind] * size; }
        float tall() { return FOE_TALL[kind] * size; }
    }

    /**
     * What has fallen from creatures as they fell, as it lies about: pluses, like coins, and things to keep. Each flies out, falls, bounces and
     * comes to rest; a plus comes to her from close by, as if drawn, and a thing to keep is picked up by walking into it; each blinks a while
     * before it is gone. Its place, how it moves, how long it has been about, and what it is (0 a plus, otherwise a thing to keep, as {@link RunLoot} numbers it).
     */
    static final class Drop {
        float x, y, across, down, age;
        final int code;
        boolean resting;

        Drop(float x, float y, float across, float down, int code) {
            this.x = x;
            this.y = y;
            this.across = across;
            this.down = down;
            this.code = code;
        }

        Drop(Drop other) {
            this(other.x, other.y, other.across, other.down, other.code);
            age = other.age;
            resting = other.resting;
        }

        /** How long a plus lies about before it is gone, and a thing to keep; and for how much of the end of that it blinks. */
        float life() { return code == 0 ? 11 : 40; }

        float blink() { return code == 0 ? 3 : 6; }
    }

    private final List<Drop> scattered = new ArrayList<>();
    /** How much room a drop takes: half its width, and its height (its place is the middle of its bottom). */
    static final float DROP_HALF = .3f, DROP_TALL = .6f;
    /** The furthest a drop moves before it is looked at again: well under its height, so nothing is ever passed through. */
    private static final float DROP_STEP = .2f;

    /** Whether a drop with its bottom middle at a place would be inside anything solid. */
    boolean dropStuck(float atX, float atY) {
        int left = (int) Math.floor(atX - DROP_HALF), right = (int) Math.floor(atX + DROP_HALF - .001f);
        int top = (int) Math.floor(atY - DROP_TALL + .001f), bottom = (int) Math.floor(atY - .001f);
        for (int column = left; column <= right; column++) for (int row = top; row <= bottom; row++) if (solid(at(column, row))) return true;
        return false;
    }

    /** Whether there is something to rest on along the top of a row, under any part of a drop at a place across. */
    private boolean dropFloor(float atX, int row) {
        int left = (int) Math.floor(atX - DROP_HALF), right = (int) Math.floor(atX + DROP_HALF - .001f);
        for (int column = left; column <= right; column++) {
            char square = at(column, row);
            if (solid(square) || square == RunLevels.THIN || square == RunLevels.FAKE && at(column, row - 1) != RunLevels.FAKE) return true;
        }
        return false;
    }

    /** A drop begun inside something solid, or left in it, is put in the nearest open place: the middle of its own column if that is free, else straight up. */
    private void unstick(Drop drop) {
        if (!dropStuck(drop.x, drop.y)) return;
        float middle = (float) Math.floor(drop.x) + .5f;
        if (!dropStuck(middle, drop.y)) {
            drop.x = middle;
            return;
        }
        for (int up = 0; up <= level.land.length; up++) {
            float y = (float) Math.floor(drop.y) - up;
            if (!dropStuck(drop.x, y)) {
                drop.y = y;
                return;
            }
            if (!dropStuck(middle, y)) {
                drop.x = middle;
                drop.y = y;
                return;
            }
        }
    }

    /** Something has fallen: it is let loose where it is, out of anything solid. */
    private void loose(Drop drop) {
        unstick(drop);
        scattered.add(drop);
    }
    /** What she has picked up of what fell: pluses, and the things to keep, in order; and how much experience the falls gave her. */
    private int dropPluses, xp;
    private final List<Integer> found = new ArrayList<>();
    /** The chance, made the same way every time for the same land, of what drops. */
    private long lootDice;
    /** What is gathered of the parts of a blow more her level gives, until it makes a whole one. */
    private float surplus;

    List<Drop> scattered() { return scattered; }

    /** One of each rarity of thing to keep, and a handful of pluses, thrown out round a place: for looking at them. */
    void scatterAll(float atX, float atY) {
        for (int rarity = RunLoot.WHITE; rarity <= RunLoot.ROSE; rarity++) {
            loose(new Drop(atX + 1.4f * rarity + 1, atY, 0, -5, RunLoot.code(rarity % 3, Math.min(11, level.chapter), rarity * 3, rarity)));
        }
        for (int coin = 0; coin < 8; coin++) loose(new Drop(atX + 3 + coin * .3f, atY - 1, (coin - 4) * .8f, -7, 0));
    }

    /** What she keeps that has to do with this land: her level among it, which grows while she plays. */
    Perks perks() { return perks; }

    int dropPluses() { return dropPluses; }

    List<Integer> found() { return found; }

    int xp() { return xp; }

    /** A roll of the land's own chance, from 0 to below a number. */
    private int lootRoll(int bound) {
        lootDice = lootDice * 6364136223846793005L + 1442695040888963407L;
        return (int) ((lootDice >>> 33) % bound);
    }

    /**
     * A creature has fallen: its pluses fly out of it, and now and then something to keep (always from one who waits), of its land's
     * chapter, its rarity by its chances; and it gives her the experience it is worth to her.
     */
    private void fall(Foe foe) {
        if (foe.kind == RunLevels.TARGET) return;
        boolean boss = RunLevels.boss(foe.kind) && !foe.spawned, hard = level.chapter >= RunLevels.CHAPTERS * 2;
        int landLevel = RunLevels.levelOf(level.number);
        // (One of a swarm gives a share of what one of the land's own gives, gathered up: her levels go on as the lands make them.)
        if (foe.swarm) {
            swarmXp += RunLoot.xpFor(boss, tough, level.number, perks.level) * TIER_XP[foe.tier] / (float) SWARM_SHARE;
            xp += (int) swarmXp;
            swarmXp -= (int) swarmXp;
            if (lootRoll(SWARM_SHARE) != 0) return;
        } else {
            xp += RunLoot.xpFor(boss, tough, level.number, perks.level) * TIER_XP[foe.tier] * (RunUniques.is(foe.kind) ? UNIQUE_XP : 1);
        }
        float middle = foe.x + foe.wide() / 2, top = foe.y + foe.tall() * .4f;
        int coins = RunLoot.plusesFor(boss, tough), more = gear(RunLoot.PLUSES);
        if (more > 0) coins += (coins * more + lootRoll(100)) / 100;
        // (Under black, half of them, rounded up.)
        if (inked()) coins = (coins + 1) / 2;
        for (int coin = 0; coin < coins; coin++) {
            float way = (lootRoll(1000) / 1000f - .5f) * 1.4f;
            loose(new Drop(middle, top, way * (boss ? 7 : 4), -6 - lootRoll(1000) / 250f, 0));
        }
        if (RunUniques.great(foe.kind)) {
            if (RunUniques.hidden(foe.kind)) {
                hiddenBeaten++;
                hiddenKind = foe.kind;
            } else {
                keepersBeaten++;
            }
            int theme = Math.min(RunLoot.THEMES.length - 1, RunUniques.chapter(foe.kind) + 1);
            for (int type = 0; type < 3; type++) {
                int roll = lootRoll(100), rarity = roll < 10 ? RunLoot.ROSE : roll < 40 ? RunLoot.RED : RunLoot.GOLD;
                int typeRoll = type == 0 ? 0 : type == 1 ? 6000 : 9999;
                int code = RunLoot.dropped(RunLoot.WAITS, theme, rarity, typeRoll, lootRoll(10000));
                loose(new Drop(middle, top, (type - 1) * 2.2f, -9, code));
            }
            return;
        }
        // (Gold and black ink drop as the hardest do.)
        hard |= foe.tier >= GOLD_TIER;
        if (lootRoll(10000) < RunLoot.gearChance(boss, hard, landLevel) * TIER_DROPS[foe.tier] * (RunUniques.is(foe.kind) ? UNIQUE_XP : 1)) {
            int rarity = RunLoot.rarityOf(boss ? RunLoot.BOSS : hard ? RunLoot.HARD : RunLoot.CREATURE, lootRoll(10000));
            int code = RunLoot.dropped(boss ? RunLoot.WAITS : hard ? RunLoot.HARDEST : RunLoot.ORDINARY, Math.min(RunLoot.THEMES.length - 1, level.chapter), rarity, lootRoll(10000), lootRoll(10000));
            loose(new Drop(middle, top, (lootRoll(1000) / 1000f - .5f) * 2, -8, code));
        }
    }

    /** What lies about falls, bounces and comes to rest; pluses near her come to her; things to keep are picked up; what has lain long enough is gone. */
    private void scatter(float seconds) {
        float drawn = gear(RunLoot.MAGNET) / 10f, reach = Math.max(1.4f + drawn, Math.max(perks.reach + drawn, magnet > 0 ? MAGNET_REACH : 0)), herX = x + WIDE / 2, herY = y + TALL / 2;
        for (int index = scattered.size() - 1; index >= 0; index--) {
            Drop drop = scattered.get(index);
            drop.age += seconds;
            if (drop.age > drop.life()) {
                scattered.remove(index);
                continue;
            }
            float far = (float) Math.hypot(drop.x - herX, drop.y - .3f - herY);
            if (drop.code == 0 && drop.age > .35f && far < reach) {
                // Drawn to her, faster the nearer it is.
                float pull = 14 * seconds / Math.max(.3f, far);
                drop.x += (herX - drop.x) * Math.min(1, pull);
                drop.y += (herY - drop.y) * Math.min(1, pull);
                drop.resting = false;
                if (far < .55f) {
                    scattered.remove(index);
                    dropPluses++;
                    bonus++;
                }
                continue;
            }
            if (drop.code != 0 && drop.age > .5f && far < .8f) {
                scattered.remove(index);
                found.add(drop.code);
                continue;
            }
            // What it rests on can go (a ledge crumbles, a bridge is let down): then it falls again.
            if (drop.resting && (Math.abs(drop.y - Math.round(drop.y)) > .001f || !dropFloor(drop.x, Math.round(drop.y)))) drop.resting = false;
            if (drop.resting) continue;
            // (Drawn towards her and let go, it may have been drawn into something.)
            unstick(drop);
            drop.down = Math.min(16, drop.down + GRAVITY * .85f * seconds);
            move(drop, seconds);
            // (Below the land, it is gone.)
            if (drop.y > level.land.length + 2) scattered.remove(index);
        }
    }

    /**
     * A drop moves for a while, in steps short enough that it never passes into or through anything: it bounces off what it flies into,
     * and lands on the top of whatever is under any part of it (a ledge, a hatch, the ground of a cave below a pit), bouncing a little less each time.
     */
    private void move(Drop drop, float seconds) {
        float across = drop.across * seconds, down = drop.down * seconds;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(across), Math.abs(down)) / DROP_STEP));
        for (int step = 0; step < steps; step++) {
            float nextX = drop.x + across / steps;
            if (dropStuck(nextX, drop.y)) {
                drop.across = -drop.across * .5f;
                across = -across * .5f;
            } else {
                drop.x = nextX;
            }
            float nextY = drop.y + down / steps;
            if (down > 0) {
                int row = (int) Math.floor(nextY);
                if (drop.y <= row && dropFloor(drop.x, row)) {
                    drop.y = row;
                    drop.down = -drop.down * .42f;
                    drop.across *= .55f;
                    if (Math.abs(drop.down) < 1.6f) {
                        drop.down = 0;
                        drop.across = 0;
                        drop.resting = true;
                    }
                    return;
                }
            } else if (dropStuck(drop.x, nextY)) {
                // Its top meets something: it stops rising and falls.
                drop.down = 0;
                return;
            }
            drop.y = nextY;
        }
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
    private int hp = HEARTS * RunSkills.HEART, lives = LIVES, livesLost, state = PLAYING;
    /** HP in half a heart. */
    static final int HALF = RunSkills.HEART / 2;

    /** The most HP she can have: her hearts, and what her skill tree gives. */
    int maxHp() {
        int base = perks.hearts * RunSkills.HEART + perks.skills.stats[RunSkills.HP];
        return Math.max(RunSkills.HEART / 2, base + base * perks.skills.effects[RunSkills.E_HP_PCT] / 100);
    }
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
        tough = RunLevels.toughOf(level.number) + perks.harder;
        bite = RunLevels.biteOf(level.number) + perks.harder + RunMarket.bites(perks.black);
        hp = maxHp();
        ward = wardMost();
        lives = perks.lives;
        // What is in her backpack is with her from the start, and what she begins a land with is in her hands.
        for (int kind = 0; kind < PETS; kind++) if ((perks.petWith >> kind & 1) != 0) join(kind, true);
        owned = perks.pack | (perks.main != UNARMED ? 1 << perks.main : 0);
        weapon = perks.main;
        rounds = perks.shots;
        books = RunArms.BOOKS;
        columns = level.columns;
        pluses.addAll(level.pluses);
        arms.addAll(level.weapons);
        items.addAll(level.items);
        total = pluses.size();
        for (int[] spec : level.foes) {
            Foe foe = new Foe(spec[0]);
            boolean waits = RunLevels.boss(spec[0]);
            // One that is given more than a blow to take is a big one. (One put in to make up the numbers takes one, and is not.)
            if (spec.length > 3 && spec[3] > 1 && !waits) foe.size = 1.3f;
            foe.x = spec[1] + (1 - foe.wide()) / 2 + (spec.length > 4 && spec[4] == 2 ? .5f : 0);
            foe.ground = spec[2] + 1 - foe.tall();
            foe.y = foe.ground;
            foe.home = foe.x;
            // (One of a swarm is never of a colour of toughness: a swarm is many, not tough.)
            foe.tier = waits || !level.tiered || RunLevels.swarmed(spec) || RunUniques.keeper(spec[0]) ? NORMAL : tierOf(level.number, spec[0], spec[1], spec[2]);
            foe.left = waits ? health(spec[0]) + (RunLevels.rover(spec[0]) ? 0 : RunMarket.takes(perks.black))
                    : RunUniques.is(spec[0]) ? RunUniques.health(spec[0], level.number) * TIER_HEALTH[foe.tier] + tougher(spec[0]) * BLOW
                    : healthOf(spec[0]) * (spec.length > 3 ? spec[3] : 1) * TIER_HEALTH[foe.tier] + tougher(spec[0]) * BLOW;
            foe.most = foe.left;
            foe.crowd = RunLevels.crowd(spec);
            foe.swarm = RunLevels.swarmed(spec);
            if (RunUniques.is(spec[0]) && RunUniques.way(spec[0]) == RunUniques.SHIELDER) {
                // (One that fights with its own shield: its shell, its claw, its stamp.)
                foe.guard = foe.guardMost = shieldHolds(level.number) + 2;
                foe.guardNeeds = level.number < SHIELDS_FROM ? BLOW : Math.round(shieldNeeds(level.number) * .8f);
                foe.guardKind = -1;
            } else if (RunUniques.flies(spec[0]) || RunUniques.floats(spec[0])) {
                foe.home = foe.y;
            } else if (!waits && level.tiered && !RunUniques.keeper(spec[0]) && shielded(level.number, spec[0], spec[1], spec[2])) {
                foe.guard = foe.guardMost = shieldHolds(level.number);
                foe.guardNeeds = shieldNeeds(level.number);
                foe.guardKind = shieldKind(level.number);
            }
            if (spec[0] == RunLevels.WORD) foe.y = foe.ground - WORD_HANGS;
            if (RunUniques.is(spec[0]) && RunUniques.way(spec[0]) == RunUniques.DROPPER) foe.y = foe.ground - DROP_HANG;
            // Each divide starts its hop at a different moment.
            foe.clock = spec[1] * .37f % HOP_EVERY;
            if (waits) {
                // Whoever waits is not there until she has come in, where there is a place to come into.
                foe.most = foe.left;
                foe.step = 3;
                foe.act = .6f;
                foe.away = level.arena >= 0;
                foe.shieldMost = strength(spec[0]) + (RunLevels.rover(spec[0]) || strength(spec[0]) <= 0 ? 0 : RunMarket.shields(perks.black));
                foe.shield = foe.shieldMost;
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
        lootDice = level.number * 977L + 12345;
        findSecrets();
        for (int[] tree : level.trees) trees.add(new int[]{tree[0], tree[1], tree[2], RunCraft.treeHolds(tree[2])});
    }

    private PlusRun(PlusRun other) {
        level = other.level;
        perks = other.perks;
        items.addAll(other.items);
        lootDice = other.lootDice;
        surplus = other.surplus;
        xp = other.xp;
        dropPluses = other.dropPluses;
        energy = other.energy;
        ward = other.ward;
        wardWait = other.wardWait;
        found.addAll(other.found);
        for (Drop drop : other.scattered) scattered.add(new Drop(drop));
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
        foe.poisoned = from.poisoned;
        foe.tier = from.tier;
        foe.burning = from.burning;
        foe.frozen = from.frozen;
            foe.left = from.left;
            foe.wait = from.wait;
            foe.home = from.home;
            foe.act = from.act;
            foe.mode = from.mode;
            foe.size = from.size;
            foe.unlocked = from.unlocked;
            foe.dropped = from.dropped;
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
            foe.crowd = from.crowd;
            foe.shield = from.shield;
            foe.shieldMost = from.shieldMost;
            foe.broken = from.broken;
            foe.forming = from.forming;
            foe.taken = from.taken;
            foe.swarm = from.swarm;
            foe.guard = from.guard;
            foe.guardMost = from.guardMost;
            foe.guardNeeds = from.guardNeeds;
            foe.guardKind = from.guardKind;
            foe.guardMet = from.guardMet;
            foe.struck = from.struck;
            foe.dropping = from.dropping;
            foe.knocked = from.knocked;
            foe.perch = from.perch;
            foe.base = from.base;
            foe.leap = from.leap;
            foe.leapFX = from.leapFX;
            foe.leapFY = from.leapFY;
            foe.leapTX = from.leapTX;
            foe.leapTY = from.leapTY;
            foe.fall = from.fall;
            foe.leaping = from.leaping;
            foe.stunned = from.stunned;
            foes.add(foe);
        }
        for (int[] tree : other.trees) trees.add(tree.clone());
        System.arraycopy(other.paperCut, 0, paperCut, 0, paperCut.length);
        treesCut = other.treesCut;
        inWater = other.inWater;
        swimming = other.swimming;
        breath = other.breath;
        breathTick = other.breathTick;
        // (Her moves, as they are.)
        airDashLeft = other.airDashLeft;
        rollLeft = other.rollLeft;
        flipAge = other.flipAge;
        poundAge = other.poundAge;
        dashUsed = other.dashUsed;
        windUsed = other.windUsed;
        clinging = other.clinging;
        gliding = other.gliding;
        wallSide = other.wallSide;
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
            shot.through = from.through;
            shot.pets = from.pets;
            shot.by = from.by;
            shot.of = from.of;
            shot.pierce = from.pierce;
            shot.chain = from.chain;
            shot.high = from.high;
            shot.wipes = from.wipes;
            shot.spent = from.spent;
            shot.element = from.element;
            shot.hops = from.hops;
            shot.bounces = from.bounces;
            shot.boost = from.boost;
            shots.add(shot);
        }
        tough = other.tough;
        bite = other.bite;
        secrets.addAll(other.secrets);
        secretsFound = other.secretsFound;
        secretAge = other.secretAge.clone();
        for (float[] boom : other.booms) booms.add(boom.clone());
        for (float[] fire : other.fires) fires.add(fire.clone());
        for (float[] smear : other.smears) smears.add(smear.clone());
        weapon = other.weapon;
        rounds = other.rounds;
        owned = other.owned;
        newly = other.newly;
        books = other.books;
        previous = other.previous;
        boost = other.boost;
        disarmed = other.disarmed;
        laidDown = other.laidDown;
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
        climbing = other.climbing;
        facingLeft = other.facingLeft;
        jumpHeld = other.jumpHeld;
        sprung = other.sprung;
        sinceGround = other.sinceGround;
        sinceJump = other.sinceJump;
        safe = other.safe;
        hp = other.hp;
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
        // In a place off the land: a door is a wall until its key is taken, a bridge is nothing until its lever is pulled, and bars are
        // a wall but while they are lifted.
        if (here == RunLevels.LOCKED) return (unlocked >> placeOf(column) & 1) != 0 ? RunLevels.AIR : RunLevels.BLOCK;
        if (here == RunLevels.SPAN) return (raised >> placeOf(column) & 1) != 0 ? RunLevels.LEDGE : RunLevels.AIR;
        if (here == RunLevels.GRILLE) return bars > 0 && barsRoom == placeOf(column) ? RunLevels.AIR : RunLevels.BLOCK;
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
        // (Water holds her up, in blue, only at its top and only while she does not mean to go down into it.)
        return square != RunLevels.LEDGE && solid(square) || square == RunLevels.LAVA && has(RED) || square == RunLevels.WATER && has(BLUE) && !down && !inWater;
    }

    /** What stops her from the side or from below: what is firm, but never water, which she swims into. */
    private boolean wall(char square) {
        return square != RunLevels.WATER && firm(square);
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
        if (kind == BREAD && hp >= maxHp() || kind == SHIELD && shield || kind == ICE && rush > 0) return NEEDLESS;
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
                if (strongCrack(one) && dealt() < crackNeeds(level.number)) {
                    clangX = one[1] + .5f;
                    clangY = one[2] - 1;
                    clangs++;
                    return true;
                }
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
                } else if (one[0] == RunLevels.PASSAGE || one[0] == RunLevels.HOUSE) {
                    // A way through is a way through: there is nothing to be done in it but go. Nor in a building, but what she chooses to.
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

    /** For looking at a place off the land in a build made for testing: she is put straight into the first of a kind, as if through its door. */
    void visit(int kind) {
        for (int at = 0; at < level.rooms.size(); at++) {
            int[] one = level.rooms.get(at);
            if (one[0] != kind) continue;
            room = at;
            roomsFound |= 1 << at;
            revealed |= 1 << at;
            put(one[6], one[11]);
            return;
        }
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
            if (wave == 1 && which == 0 && herd.isEmpty()) {
                boolean waits = false;
                for (int[] item : items) waits |= item[0] >= PET && item[0] < PET + PETS && item[1] >= one[3] && item[1] <= one[4];
                if (!waits) items.add(new int[]{PET + (level.number + 2) % RunPets.FOUND, where[2] + 2, where[3] - 1, 0});
            }
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

    /** How many more blows a creature of a kind takes in this land: none for a paper target, nor for a wisp or a flake, which one thing puts out. */
    private int tougher(int kind) {
        return RunLevels.boss(kind) || kind == RunLevels.TARGET || kind == RunLevels.WISP || kind == RunLevels.RIME ? 0 : tough;
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
            foe.shieldMost = strength(RunLevels.OLD_BOOK);
            foe.shield = foe.shieldMost;
            foe.step = 3;
            foe.act = .5f;
            // He takes a moment to wake.
            foe.enter = 1.6f;
            woken++;
        } else {
            // An outsized one is nearly twice the size, and takes six blows.
            foe.left = healthOf(kind) * (giant ? 6 : 1) + tougher(kind) * BLOW;
            foe.most = foe.left;
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
        // (How long it took counts for nothing: only how she came through it.)
        return lives > 0 ? RANK_C : hurts <= 2 ? RANK_S : hurts <= 8 ? RANK_A : RANK_B;
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
                foe.left = healthOf(foe.kind);
                foe.most = foe.left;
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

    /**
     * Lucy, at the end of the last land. Two thirds beaten, she does what the Mawang did, and means it: she throws the black
     * ribbon, and nothing turns it aside. For as long as that takes nothing else moves. If she is whole, every colour hers and
     * every friend pledged, the black goes in with the rest and what comes of it is the white that is truly hers, and the fight
     * leaves the ground: the land is risen. If she is not, the black lies on her for good: the land is unmade. How long the
     * moment is, and how much of it is left when the ribbon reaches her.
     */
    static final float MOMENT = 5.4f, MOMENT_LANDS = 3.2f;
    private float moment;
    private boolean decided, ribboned;

    /**
     * Once it has been won, the white that is truly hers is hers to call on in any land: every colour at once, with no time to it,
     * for the rest of the land. Not while black lies on her; and not in the last land before the ribbon is thrown, where it is what
     * the ribbon turns into. Whether she can call on it just now; and she calls on it, which returns whether she did.
     */
    boolean canMaster() { return perks.white && !mastered && cursed <= 0 && state == PLAYING && moment <= 0 && level.number != RunLevels.TOTAL; }

    boolean callWhite() {
        if (!canMaster()) return false;
        master();
        return true;
    }

    /** How much longer the moment lasts; whether it has come; whether the ribbon has reached her; and whether she is whole. */
    float moment() { return moment; }
    boolean decided() { return decided; }
    boolean ribboned() { return ribboned; }
    boolean whole() { return perks.whole; }

    private void decide(Foe foe) {
        phases++;
        decided = true;
        moment = MOMENT;
        // She is not to be touched again down here.
        foe.flourish = 1e6f;
        foe.dazed = 0;
        foe.unseen = false;
        for (Foe other : foes) {
            if (other.spawned && !other.flat) {
                other.flat = true;
                other.act = 0;
            }
        }
        for (Shot shot : shots) shot.left = 0;
        swing = 0;
    }

    private void momentous(float seconds) {
        moment -= seconds;
        if (!ribboned && moment <= MOMENT_LANDS) {
            ribboned = true;
            if (perks.whole) {
                master();
            } else {
                fade(false);
                cursed = BLACKENED;
                cursedFor = BLACKENED;
            }
        }
        if (moment <= 0) {
            moment = 0;
            state = perks.whole ? RISEN : UNMADE;
        }
    }

    /** In the air over the library a life is lost: it is one of those she came into the land with. Returns whether she has one left to go on with. */
    boolean spend() {
        if (state != RISEN) return false;
        if (--lives <= 0) {
            lives = 0;
            state = LOST;
            return false;
        }
        livesLost++;
        return true;
    }

    /** The flight is won: Lucy is down, and the last land with her. */
    void flown() {
        if (state != RISEN) return;
        for (Foe foe : foes) {
            if (foe.kind != RunLevels.LUCY) continue;
            foe.left = 0;
            foe.flat = true;
            foe.flourish = 0;
            foe.y = foe.ground;
        }
        state = WON;
    }

    boolean mastered() { return mastered; }

    /**
     * What she has to keep has changed while she is in a land: bought at the counter by a
     * stall, or won. It is hers there and then: a heart more is a heart more now, a life
     * more a life more now, and the rest holds from the next time it matters, the second
     * jump in the air from her next jump.
     */
    /** What her skill tree gives is changed in the middle of a land: felt at once; HP more at most is HP more now. */
    void learn(RunSkills.Built skills, List<RunSkills.Notable> slotted) {
        int before = maxHp();
        perks.skills = skills;
        perks.slotted = slotted;
        int after = maxHp();
        if (hp > 0) hp = Math.max(1, Math.min(after, hp + Math.max(0, after - before)));
        for (int slot = 0; slot < SLOTS; slot++) {
            if (active(slot) == null) {
                activeLeft[slot] = 0;
                activeRest[slot] = 0;
            }
        }
    }

    void grant(Perks now) {
        learn(now.skills, now.slotted);
        if (now.hearts > perks.hearts) hp = Math.min(now.hearts * RunSkills.HEART + perks.skills.stats[RunSkills.HP], hp + (now.hearts - perks.hearts) * RunSkills.HEART);
        if (now.lives > perks.lives) lives += now.lives - perks.lives;
        perks.hearts = now.hearts;
        perks.lives = now.lives;
        perks.wings = now.wings;
        perks.triple = now.triple;
        perks.airDash = now.airDash;
        perks.dashStrikes = now.dashStrikes;
        perks.pound = now.pound;
        perks.roll = now.roll;
        perks.wallJump = now.wallJump;
        perks.glide = now.glide;
        perks.secondWind = now.secondWind;
        perks.thorns = now.thorns;
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
        // Her pets: those that are hers for good are those now chosen; and there is as much room for strays as there now is.
        for (int index = herd.size() - 1; index >= 0; index--) if (herd.get(index).stays && (now.petWith >> herd.get(index).kind & 1) == 0) herd.remove(index);
        for (int kind = 0; kind < PETS; kind++) {
            boolean there = false;
            for (Pet it : herd) there |= it.stays && it.kind == kind;
            if ((now.petWith >> kind & 1) != 0 && !there) join(kind, true);
        }
        perks.petWith = now.petWith;
        perks.tempSlots = now.tempSlots;
        perks.petStrong = now.petStrong;
        perks.petBrisk = now.petBrisk;
        perks.pack |= now.pack;
        owned |= now.pack;
        perks.main = now.main;
        if (now.main != UNARMED) owned |= 1 << now.main;
        perks.levels = now.levels.clone();
        // What she wears, put on or taken off: a heart that went with it goes with it.
        perks.whole = now.whole;
        perks.white = now.white;
        perks.defence = now.defence;
        perks.stomp = now.stomp;
        perks.mercy = now.mercy;
        hp = Math.max(HALF, Math.min(hp, maxHp()));
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
                    // (On a shield it takes three pieces at a time: nothing strips one faster.)
                    beat(foe, BEAMED);
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
    /**
     * Whether a sign can be hurt just now: those who wait, only while their shield is
     * broken. A shield is not waited out: it is broken by blows. Each blow that lands on it
     * takes a piece off it; when the last piece is gone it shatters, and she reels, and for
     * a moment blows land on her, two at the most; then it forms again, and while it is
     * forming nothing touches it. What she does herself never opens her: she does it to
     * keep whoever is at her shield off it.
     */
    boolean open(Foe foe) { return !RunLevels.boss(foe.kind) || foe.broken > 0 && !absent(foe); }

    /** How many pieces the shield of each of those who wait is made of, to begin with: one more in each later third of the fight. */
    static int strength(int kind) {
        switch (kind) {
            case RunLevels.GUARD:
            case RunLevels.WYRM:
            case RunLevels.GECKO:
                return 3;
            case RunLevels.BUTLER:
                return 3;
            case RunLevels.ILLYA:
            case RunLevels.TSUBAKIEH:
            case RunLevels.BLOT:
                return 4;
            case RunLevels.ZERO:
            case RunLevels.BYEORU:
            case RunLevels.GOKHAE:
            case RunLevels.JUPAN:
            case RunLevels.MIRI:
            case RunLevels.GARI:
            case RunLevels.OLD_BOOK:
                return 4;
            case RunLevels.SUMBI:
            case RunLevels.YEOBAEK:
            case RunLevels.PADS:
            case RunLevels.KING:
            case RunLevels.PARTY:
            case RunLevels.LUCY:
                return 5;
            default:
                return 6;
        }
    }

    /** What the shield of one of them gives way to soonest: fire for those made of paper, water for those of ink and of fire; or nothing in particular. */
    static int weak(int kind) {
        return kind == RunLevels.OLD_BOOK || kind == RunLevels.BYEORU || kind == RunLevels.GOKHAE || kind == RunLevels.MIRI || kind == RunLevels.WYRM
                || kind == RunLevels.PARTY ? FIRE
                : kind == RunLevels.BUTLER || kind == RunLevels.GARI || kind == RunLevels.MAWANG || kind == RunLevels.PADS || kind == RunLevels.BLOT ? WATER : 0;
    }

    /** How long she can be hurt for once her shield is broken, and how long it takes to form again: both shorter in each later third. */
    float opens(Foe foe) { return 2.4f - .35f * stage(foe); }
    float forms(Foe foe) { return 2.2f - .5f * stage(foe); }

    /**
     * How far through the fight she is, for what her shield does: the Guild Guard and
     * Penchok-jipsa, who are the first anyone meets, are as they began all the way through:
     * their shields get no stronger, break for as long, form as slowly, and throw no one
     * back.
     */
    private int stage(Foe foe) { return foe.kind == RunLevels.GUARD || foe.kind == RunLevels.BUTLER ? 0 : phase(foe); }

    /** How much of her shield is left, from nothing to one; whether it is forming again; and counts that only go up: pieces knocked off shields, shields broken, and times a breaking shield threw her back. */
    float shield(Foe foe) { return foe.shieldMost <= 0 ? 0 : Math.max(0, Math.min(1, foe.shield / foe.shieldMost)); }
    boolean forming(Foe foe) { return foe.forming > 0; }
    int cracks() { return cracks; }
    int shatters() { return shatters; }
    int thrownBack() { return thrownBack; }
    private int cracks, shatters, thrownBack;

    /** A blow lands on a shield: a piece of it is gone, more for what is heavy and for what it is weak to; the last piece gone, it is broken. */
    private void chip(Foe foe) {
        float bite = with == BEAMED ? 3 : with == BLASTED ? RunArms.bite(BOOK, perks.levels[BOOK]) : with == SWORDED ? RunArms.bite(SWORD, perks.levels[SWORD])
                : with == PLUNGED || with == BOOK || with == STEAM ? 2 : with == GUN ? RunArms.bite(GUN, perks.levels[GUN]) : with == PANELLED ? RunArms.PANEL_BITE
                : with == PEN || with == KNIFE ? RunArms.bite(with, perks.levels[with]) : with == PAGE ? RunArms.bite(TABLET, perks.levels[TABLET])
                : with == THROWN_KEY ? RunArms.bite(KEYS, perks.levels[KEYS])
                : with == CUT ? (chain == 3 && !dashCut ? RunArms.finish(perks.levels[KATANA]) : RunArms.bite(KATANA, perks.levels[KATANA]))
                : with == LANCED || with == SHELLED || with == WHIPPED ? RunArms.bite(thingOf(with), perks.levels[thingOf(with)]) : 1;
        int weak = weak(foe.kind);
        if (weak != 0 && (with == weak || with == STEAM)) bite += 1;
        // What she holds, the stronger for a find; and a shield with a key in it, from which the next blow that is not a key takes a piece more.
        if (boost > 0 && with != BEAMED) bite += RunArms.BOOST_BITE;
        if (foe.unlocked > 0 && with != THROWN_KEY) {
            bite += 1;
            foe.unlocked = 0;
            doubled++;
        }
        if (absent(foe) || foe.forming > 0 || foe.shieldMost <= 0) {
            parried++;
            return;
        }
        if (thingOf(with) != UNARMED) bite *= 1 + Math.max(0, skillOf(RunSkills.E_SHIELD_BREAK)) / 100f;
        foeX = foe.x + foe.wide() / 2;
        foeY = foe.y + foe.tall();
        foe.shield -= bite;
        cracks++;
        if (foe.shield > 0) return;
        foe.shield = 0;
        foe.broken = opens(foe);
        foe.taken = 0;
        shatters++;
        // What she was at is broken off; and whatever hid her does not.
        foe.step = 3;
        foe.act = .3f;
        foe.unseen = false;
        // In the last third it throws whoever broke it back as it goes, if she is near.
        if (stage(foe) >= 2 && !RunLevels.rover(foe.kind) && Math.abs(x + WIDE / 2 - foeX) < 3) {
            speedAcross = (x + WIDE / 2 < foeX ? -1 : 1) * 11;
            speedDown = -6;
            onGround = false;
            thrownBack++;
        }
    }

    /** Her shield is whole again, as strong as this third of the fight makes it. */
    private void shielded(Foe foe) {
        foe.shieldMost = strength(foe.kind) + stage(foe);
        foe.shield = foe.shieldMost;
        foe.broken = 0;
        foe.forming = 0;
        foe.taken = 0;
    }
    /**
     * How one of those who wait stands toward a blow just now: bare, when one would land;
     * shielded, when it would be turned aside; reeling, for the moment after one has landed,
     * when nothing more does; and away, while arriving, changing or out of sight, when
     * nothing touches them at all. And for how much longer they are bare, if they are.
     */
    static final int BARE = 0, SHIELDED = 1, REELING = 2, AWAY = 3;
    int guard(Foe foe) { return foe.flat ? REELING : absent(foe) ? AWAY : !open(foe) ? SHIELDED : foe.dazed > 0 ? REELING : BARE; }
    float bareLeft(Foe foe) { return RunLevels.boss(foe.kind) ? Math.max(0, foe.broken) : 0; }
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
            // (Lucy, twelve: four as a child, four with her dragon side out; and the last four are not taken on the ground.)
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

    // Secret spaces

    /**
     * The secret spaces of a land: a false wall and the little room behind it, a cave's or a home's. Until it is found, each is drawn as the
     * rock round it (see {@link #shown}), and what is in it is not drawn at all; it is found by walking into the false wall, which fades, or by
     * striking it, which breaks it open. Each: the false wall's column, its top and bottom rows, and the first and last columns of all of it.
     */
    private final List<int[]> secrets = new ArrayList<>();
    private int secretsFound, secretsBroken;
    private float[] secretAge = new float[0];

    /** How long a secret space takes to show once found. */
    static final float SECRET_SHOWS = .5f;

    private void findSecrets() {
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < level.land.length; row++) {
                if (level.land[row][column] != RunLevels.FAKE || row > 0 && level.land[row - 1][column] == RunLevels.FAKE) continue;
                int bottom = row;
                while (bottom + 1 < level.land.length && level.land[bottom + 1][column] == RunLevels.FAKE) bottom++;
                // The room behind it: the side where, a few squares on, there is rock again the height of the false wall.
                for (int side = -1; side <= 1; side += 2) {
                    int wide = 0;
                    while (wide < 6 && open(column + side * (wide + 1), row, bottom)) wide++;
                    if (wide < 1 || wide > 5 || !closed(column + side * (wide + 1), row, bottom)) continue;
                    int far = column + side * wide;
                    secrets.add(new int[]{column, row, bottom, Math.min(column, far), Math.max(column, far)});
                    break;
                }
                row = bottom;
            }
        }
        secretAge = new float[secrets.size()];
    }

    private boolean open(int column, int top, int bottom) {
        if (column < 0 || column >= columns) return false;
        for (int row = top; row <= bottom; row++) if (level.land[row][column] != RunLevels.AIR) return false;
        return true;
    }

    private boolean closed(int column, int top, int bottom) {
        if (column < 0 || column >= columns) return false;
        for (int row = top; row <= bottom; row++) if (!solid(level.land[row][column])) return false;
        return true;
    }

    /** Whether a square is in a secret space not yet found: drawn as the rock round it, and nothing in it drawn. */
    boolean hidden(int column, int row) {
        for (int at = 0; at < secrets.size(); at++) {
            int[] one = secrets.get(at);
            if ((secretsFound & 1 << at) == 0 && column >= one[3] && column <= one[4] && row >= one[1] && row <= one[2]) return true;
        }
        return false;
    }

    /** What a square is drawn as: rock, in a secret space not yet found; otherwise what it is. */
    char shown(int column, int row) { return hidden(column, row) ? RunLevels.BLOCK : at(column, row); }

    /** How long ago the secret space a square is in was found; -1 for a square in none, or in one not yet found. */
    float foundAgo(int column, int row) {
        for (int at = 0; at < secrets.size(); at++) {
            int[] one = secrets.get(at);
            if ((secretsFound & 1 << at) != 0 && column >= one[3] && column <= one[4] && row >= one[1] && row <= one[2]) return secretAge[at];
        }
        return -1;
    }

    /** How near she must be to a secret space for its one hairline crack to show. */
    static final float HINT_NEAR = 3;

    /** The secret spaces, whether each is found and how long ago, and how many were broken open; for drawing them and for the tests. */
    List<int[]> secrets() { return secrets; }
    boolean secretFound(int at) { return (secretsFound & 1 << at) != 0; }
    float secretAge(int at) { return at < secretAge.length ? secretAge[at] : 9; }
    int secretsBroken() { return secretsBroken; }

    /** A secret space is found: by her walking into its false wall, which fades away; or broken open by a blow, in a burst of rubble. */
    private void foundSecret(int at, boolean broken) {
        if ((secretsFound & 1 << at) != 0) return;
        secretsFound |= 1 << at;
        secretAge[at] = 0;
        if (!broken) return;
        int[] one = secrets.get(at);
        secretsBroken++;
        booms.add(new float[]{one[0] + .5f, (one[1] + one[2] + 1) / 2f, 1.1f, PLAIN, 0});
        if (splashes.size() < 12) splashes.add(new float[]{one[0] + .5f, (one[1] + one[2] + 1) / 2f, 0});
    }

    /** A blow of hers over a box: whatever crack hides a place off the land there breaks open, unless it is too strong for the blow. */
    private void crack(float from, float to, float top, float bottom) {
        for (int index = 0; index < level.rooms.size() && room < 0; index++) {
            int[] one = level.rooms.get(index);
            if (one[7] == 1 && (revealed & 1 << index) == 0 && one[1] + 1 > from && one[1] < to && one[2] > top && one[2] - 1 < bottom) {
                if (strongCrack(one) && dealt() < crackNeeds(level.number)) {
                    clangX = one[1] + .5f;
                    clangY = one[2] - 1;
                    clangs++;
                    continue;
                }
                revealed |= 1 << index;
                reveals++;
            }
        }
    }

    /** Whether a place off the land is still hidden behind its crack: no button is shown for it, though a use of it there still breaks through. */
    boolean crackHidden(int index) { return index >= 0 && index < level.rooms.size() && level.rooms.get(index)[7] == 1 && (revealed & 1 << index) == 0; }

    /** The secret space whose false wall a square is in, not yet found; or -1. */
    private int secretAt(int column, int row) {
        for (int at = 0; at < secrets.size(); at++) {
            int[] one = secrets.get(at);
            if ((secretsFound & 1 << at) == 0 && one[0] == column && row >= one[1] && row <= one[2]) return at;
        }
        return -1;
    }

    /** A blow of hers over a box: whatever false wall of a secret space it meets breaks open. */
    private void breakSecrets(float from, float to, float top, float bottom) {
        for (int at = 0; at < secrets.size(); at++) {
            int[] one = secrets.get(at);
            if ((secretsFound & 1 << at) == 0 && one[0] + 1 > from && one[0] < to && one[2] + 1 > top && one[1] < bottom) foundSecret(at, true);
        }
    }

    /** As time goes: walked into, a secret space is found; and those found show, a little more at a time. */
    private void secrets(float seconds) {
        for (int at = 0; at < secrets.size(); at++) {
            if ((secretsFound & 1 << at) != 0) {
                secretAge[at] += seconds;
                continue;
            }
            int[] one = secrets.get(at);
            if (x + WIDE > one[0] + .15f && x < one[0] + .85f && y + TALL > one[1] + .2f && y < one[2] + .8f) foundSecret(at, false);
        }
    }
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
    int hearts() { return (hp + RunSkills.HEART - 1) / RunSkills.HEART; }
    int halves() { return (hp + HALF - 1) / HALF; }
    int hp() { return hp; }
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
    boolean gateOpen() { return (!level.guarded || !standing() && opening <= 0) && !keeperStands(); }

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
        if (!foe.flat && !absent(foe) && foe.dazed <= 0 && foe.forming <= 0) beat(foe);
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

    /** Takes a heart from her, and never her last: for checking what gives one back. */
    void wound() { hp = Math.max(HALF, hp - RunSkills.HEART); }

    /** Puts something in her hands: for checking the rules, and for looking at them. */
    void arm(int with) {
        weapon = with;
        if (with >= 0) owned |= 1 << with;
        if (with == GUN) rounds = perks.shots;
        if (with == BOOK) books = RunArms.BOOKS;
    }

    /** She begins somewhere other than the start of the land: and that is where she comes back to. */
    void begins(float across, float down) {
        place(across, down);
        startX = across;
        startY = down;
        firmX = across;
        firmY = down;
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
        for (Foe foe : foes) {
            if (foe.flat && !foe.dropped) {
                foe.dropped = true;
                fall(foe);
            }
        }
        scatter(seconds);
        wearing(seconds);
        actives(seconds);
        // The moment of the black ribbon: nothing else moves.
        if (moment > 0) {
            momentous(seconds);
            return;
        }
        time += seconds;
        if (safe > 0) safe -= seconds;
        // Each colour she wears has its own time; each that is gone waits its own wait, which begins when it goes.
        // (In the room of a ribbon gacha a ribbon's time stands still, and so does black's: they run again when she leaves.)
        boolean still = frozen();
        for (int colour = RED; colour <= BLACK; colour++) {
            if (worn[colour] > 0) {
                if (!still && (worn[colour] -= seconds) <= 0) end(colour, true);
            } else if (waits[colour] > 0) {
                waits[colour] = Math.max(0, waits[colour] - seconds);
            }
        }
        if (cursed > 0 && !still) cursed -= seconds;
        if (pale > 0 && !still) pale -= seconds;
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
        float run = RUN * Math.max(.5f, 1 + skillOf(RunSkills.E_SPEED) / 100f) * slowedBy() * (inked() ? curseSlow() : 1);
        float fast = rush > 0 ? run * 1.3f : run, leap = (rush > 0 ? LEAP * 1.1f : LEAP) * (inked() ? CURSE_JUMP : 1);
        if (has(YELLOW)) fast = run * (mastered ? 2.1f : 1.7f);
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
        } else if (pressed && !onGround && clinging) {
            // Off the wall she clings to, up and away from it.
            speedDown = -LEAP * .92f;
            speedAcross = -wallSide * RUN * 1.3f;
            facingLeft = wallSide > 0;
            flinch = .16f;
            sinceJump = 1;
            clinging = false;
            airJumps = 0;
            wallJumps++;
        } else if (pressed && !onGround && airJumps < (perks.wings ? 1 : 0) + (perks.wings && perks.triple ? 1 : 0) + (named(STEAMS) ? 1 : 0) + gear(RunLoot.AIR)) {
            // (The third jump in the air, with the third jump bought, is a flip.)
            if (perks.triple && perks.wings && airJumps == 1) {
                flipAge = 0;
                flips++;
            }
            // With her wings, one more jump in the air; and in steam, which lifts her, another.
            // With her wings, one more jump in the air.
            speedDown = -LEAP * .88f * (inked() ? CURSE_JUMP : 1);
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
        // (In an air dash she does not fall.)
        if (airDashLeft > 0) speedDown = 0;
        char about = at((int) Math.floor(x + WIDE / 2), (int) Math.floor(y + TALL * .6f));
        // A draught lifts her; and with the wind in her hair she flies for as long as the jump is held.
        if (about == RunLevels.DRAUGHT) speedDown = Math.max(-5, speedDown - (GRAVITY + 24) * seconds);
        else if (has(GREEN) && jump && !down && !onGround && y > 1) speedDown = Math.max(mastered ? -10 : named(GALE) ? -8 : -4.5f, speedDown - (GRAVITY + 24) * seconds);
        // On the tide she does not drop out of the air: let go of, the jump lets her down slowly.
        else if (named(TIDE) && !onGround && !down) speedDown = Math.min(speedDown, 2.6f);
        // On a vine (or a ladder) she climbs: up while the jump is held, down with the down button, and holds on otherwise; off the top of it,
        // still climbing, she is given a little lift, onto whatever it leads up to.
        // Against a wall in the air, pushing toward it, she clings and slides down it slowly; and holding the jump as she falls, she glides.
        clinging = false;
        if (perks.wallJump && !onGround && speedDown > 0 && (left != right)) {
            int side = left ? -1 : 1;
            if (wallBeside(side)) {
                clinging = true;
                wallSide = side;
                speedDown = Math.min(speedDown, WALL_SLIDE);
                airJumps = 0;
            }
        }
        gliding = perks.glide && !onGround && !clinging && jump && !down && speedDown > GLIDE_FALL && airDashLeft <= 0 && sinceJump > .25f;
        if (gliding) speedDown = GLIDE_FALL;
        boolean onVine = about == RunLevels.VINE || at((int) Math.floor(x + WIDE / 2), (int) Math.floor(y + TALL - .05f)) == RunLevels.VINE;
        if (onVine && (jump || down || climbing)) {
            climbing = !(onGround && !jump && !down);
            if (climbing) {
                float climb = CLIMB * (1 + Math.max(0, skillOf(RunSkills.E_CLIMB)) / 100f);
                speedDown = jump ? -climb : down ? climb : 0;
                speedAcross *= .8f;
                airJumps = 0;
                // Climbing and nothing else, she keeps to the middle of it, clear of the sides of the shaft it goes up.
                if (left == right && (jump || down)) {
                    float aim = (float) Math.floor(x + WIDE / 2) + .5f - WIDE / 2;
                    x += (aim - x) * Math.min(1, seconds * 14);
                }
            }
        } else if (climbing) {
            climbing = false;
            if (jump) speedDown = Math.min(speedDown, -CLIMB * 2);
        }
        // In water, in blue, she swims: up while the jump is held, down with the down button, along with the arrows; at the top, a jump
        // takes her out. Without it she holds her breath, and when that is gone it costs her.
        inWater = about == RunLevels.WATER;
        swimming = inWater && has(BLUE);
        if (swimming) {
            float swim = SWIM * (1 + Math.max(0, skillOf(RunSkills.E_SWIM)) / 100f), wantDown = jump ? -swim : down ? swim : .4f, wantAcross = left == right ? 0 : left ? -swim : swim;
            speedDown += (wantDown - speedDown) * Math.min(1, seconds * 7);
            speedAcross += (wantAcross - speedAcross) * Math.min(1, seconds * 7);
            airJumps = 0;
            sinceGround = 0;
            int head = (int) Math.floor(y + TALL * .25f);
            if (pressed && at((int) Math.floor(x + WIDE / 2), head) != RunLevels.WATER) {
                speedDown = -LEAP * .9f;
                swimming = false;
            }
            swum += seconds;
        }
        if (inWater && !has(BLUE)) {
            breath -= seconds;
            if (breath <= 0 && (breathTick -= seconds) <= 0) {
                breathTick = 1;
                if (safe <= 0) {
                    cause = SANK;
                    if (hurt(false)) return;
                }
            }
        } else {
            breath = Math.min(holdBreath(), breath + seconds * 3);
            breathTick = 0;
        }
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
            float far = Math.max(perks.reach + gear(RunLoot.MAGNET) / 10f, magnet > 0 ? MAGNET_REACH : 0);
            boolean drawn = far > 0 && Math.hypot(plus[0] + .5f - x - WIDE / 2, plus[1] + .5f - y - TALL / 2) < far;
            if (drawn || x < plus[0] + .8f && x + WIDE > plus[0] + .2f && y < plus[1] + .8f && y + TALL > plus[1] + .2f) {
                pluses.remove(index);
                collected++;
                plusX = plus[0] + .5f;
                plusY = plus[1] + .5f;
            }
        }
        // A thing to fight with is picked up by touching it: into her backpack, and into her hands if she had none of its kind.
        for (int index = arms.size() - 1; index >= 0; index--) {
            int[] arm = arms.get(index);
            if (x < arm[1] + .85f && x + WIDE > arm[1] + .15f && y < arm[2] + .9f && y + TALL > arm[2] + .1f) {
                arms.remove(index);
                found(arm[0]);
                picked++;
            }
        }
        if (bars > 0 && (bars -= seconds) <= 0) {
            for (int column = (int) Math.floor(x); column <= (int) Math.floor(x + WIDE - .001f); column++) {
                for (int row = (int) Math.floor(y); row <= (int) Math.floor(y + TALL - .001f); row++) if (laid(column, row) == RunLevels.GRILLE) bars = .05f;
            }
        }
        atStall = false;
        offered = null;
        spot = -1;
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
            // A friend stays where she is: walked up to, she pledges, once.
            if (item[0] == PLEDGE) {
                if (!pledged && room < 0 && Math.abs(item[1] + .5f - x - WIDE / 2) < 1.6f && Math.abs(item[2] + 1 - y - TALL) < 1.5f) {
                    pledged = true;
                    pledges++;
                    lastPledge = item[4];
                }
                continue;
            }
            // A pot of black ink stays where it is: every so often, having swelled, it throws the black curse at her if she is near.
            if (item[0] == INKPOT) {
                float turn = time + item[4] * .37f;
                if (room < 0 && (int) (turn / POT_EVERY) != (int) ((turn - seconds) / POT_EVERY) && Math.abs(item[1] + .5f - x - WIDE / 2) < POT_REACH && Math.abs(item[2] - y) < 5) {
                    aim(CURSE, item[1] + .5f, item[2] + .2f, POT_FAST, 0, 2.6f);
                    potShots++;
                }
                continue;
            }
            // What is in a building stays where it is: a desk is stood at anywhere along it, as a stall is; a pet on show, at it.
            if (item[0] == SPOT) {
                boolean desk = item[4] >= 100;
                if (x < item[1] + (desk ? 2.4f : .9f) && x + WIDE > item[1] + (desk ? -.8f : .1f) && y < item[2] + 1.1f && y + TALL > item[2] - .1f) spot = item[4];
                continue;
            }
            // A lever and a plate stay where they are: the one is pulled once, the other lifts the bars again every time it is stood on.
            if (item[0] == LEVER || item[0] == PLATE) {
                if (x < item[1] + .9f && x + WIDE > item[1] + .1f && y < item[2] + 1 && y + TALL > item[2]) {
                    if (item[0] == LEVER) {
                        if (pulled.add(item[1])) {
                            raised |= 1 << room;
                            levers++;
                        }
                    } else {
                        if (bars <= 0) plates++;
                        bars = BARS_OPEN;
                        barsRoom = room;
                    }
                }
                continue;
            }
            // A shrine takes a heart from whoever has more than one, and gives its ribbon; from anyone else it takes nothing and gives nothing.
            if (item[0] == SHRINE) {
                if (x < item[1] + .85f && x + WIDE > item[1] + .15f && y < item[2] + .9f && y + TALL > item[2] + .1f && hp > RunSkills.HEART && cursed <= 0) {
                    items.remove(index);
                    hp -= RunSkills.HEART;
                    shrines++;
                    tookX = item[1] + .5f;
                    tookY = item[2] + .5f;
                    take(FORM + item[4]);
                }
                continue;
            }
            if (item[0] == LOOSE) {
                if (down && onGround && x < item[1] + .85f && x + WIDE > item[1] + .15f && Math.abs(y + TALL - item[2] - 1) < .2f) {
                    item[0] = CHEST;
                    lifted++;
                }
                continue;
            }
            if (x < item[1] + .85f && x + WIDE > item[1] + .15f && y < item[2] + .9f && y + TALL > item[2] + .1f) {
                if (cursed > 0 && item[0] > FORM && item[0] < CHEST && item[0] != FORM + WHITE) continue;
                if (item[0] == DIARY) {
                    items.remove(index);
                    diaries++;
                    lastDiary = item.length > 4 ? item[4] : 0;
                    tookX = item[1] + .5f;
                    tookY = item[2] + .5f;
                    continue;
                }
                if (item[0] >= PET && item[0] < PET + PETS && strays() >= perks.tempSlots) {
                    offered = item;
                    continue;
                }
                items.remove(index);
                tookX = item[1] + .5f;
                tookY = item[2] + .5f;
                // A key opens the locked door of the place it lay in.
                if (item[0] == KEY) {
                    unlocked |= 1 << room;
                    keys++;
                    continue;
                }
                // A pet that lay waiting goes with her from here on: a stray, for this land.
                if (item[0] >= PET && item[0] < PET + PETS) {
                    Pet joined = join(item[0] - PET, false);
                    joined.x = item[1] + .5f;
                    joined.y = item[2] + 1;
                    continue;
                }
                // A chest is opened: ten pluses in it, whatever else; and a stash is done with when its chest is.
                if (item[0] == CHEST) {
                    chests++;
                    lastChest = item.length > 4 && item[4] > 0 ? item[4] : 10;
                    bonus += lastChest;
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
        sinceDud += seconds;
        if (boost > 0) boost -= seconds;
        // Her hands were emptied: after a while what she held is in them again, if she has a backpack to take it from.
        if (disarmed > 0 && (disarmed -= seconds) <= 0 && weapon == UNARMED && perks.main != UNARMED) weapon = usable(laidDown) ? laidDown : perks.main;
        if (swing > 0) swing -= seconds;
        if (swing <= 0 && echoSwing) {
            echoSwing = false;
            swing = SWING;
        }
        // In black nothing she carries is any use to her. In red and in blue the button throws fire and water, and whatever she
        // carries is used as well: the one is never lost to the other.
        sinceCast += seconds;
        sinceBrace += seconds;
        if (mastered) shine(seconds, strike);
        {
            int haste = Math.min(80, gear(RunLoot.HASTE) + (frenzyLeft > 0 ? Math.max(0, skillOf(RunSkills.E_FRENZY)) : 0));
            boolean used = weapon != UNARMED && strike && (!strikeHeld || weapon == GUN) && sinceStrike >= restOf(weapon) * (1 - haste / 100f);
            if (used) {
                // (What the tree does to every use: a charge for a rest before it, more of what is thrown, a swing that strikes again.)
                boolean rested = sinceStrike >= CHARGE_REST;
                int before = shots.size();
                chargedLeft = rested && skillOf(RunSkills.E_CHARGE) > 0 ? CHARGED_SWING : 0;
                swept.clear();
                sweeping = true;
                use();
                sweeping = false;
                afterUse(before, rested);
            }
            // Held, a tablet-book opens its panel and a ring of keys goes out in a fan: once for each hold. (With the white that is truly
            // hers the hold is her beam's.)
            // (The Mother Tongue's Quill is held for its wave, whatever kind it is.)
            boolean quill = RunLoot.ultimate(perks.item) && weapon == RunArms.kindOf(perks.item);
            if (weapon != UNARMED && (RunArms.heavy(weapon) || quill) && strike && !mastered) {
                heldFor += seconds;
                if (heldFor >= RunArms.CHARGE && !heavied && canSpend(specialCost())) {
                    heavied = true;
                    spend(specialCost());
                    specialsUsed++;
                    if (RunArms.heavy(weapon)) heavy();
                    if (quill) quillWave();
                }
            } else {
                heldFor = 0;
                heavied = false;
            }
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
        pets(seconds);
        secrets(seconds);
        if (!born.isEmpty()) {
            foes.addAll(born);
            born.clear();
        }
        if (swing > 0) swung();
        linger(seconds);
        if (fly(seconds)) return;
        moves(seconds);
        if (poundNow) poundWave();
        for (Foe foe : foes) {
            // Whoever is arriving, changing or out of sight is not there to be touched.
            // (Coming down onto a small one she may be a hair to one side of it: what is forgiven is the landing, never a touch.)
            boolean falling = speedDown > 0 && y + TALL - speedDown * seconds <= foe.y + foe.tall() * (foe.size < 1 ? .75f : .6f);
            float hair = falling && foe.size < 1 && foe.kind != RunLevels.TIMES && foe.kind != RunLevels.WISP ? .1f : 0;
            if (foe.flat || absent(foe) || foe.kind == RunLevels.TARGET || x >= foe.x + foe.wide() + hair || x + WIDE <= foe.x - hair || y >= foe.y + foe.tall()
                    || y + TALL <= foe.y) continue;
            // Coming down onto a sign deals with it and sends her up again; any other touch knocks her back.
            // A times has a spike on every arm and cannot be landed on at all.
            boolean onto = falling;
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
                if (plunged && !RunLevels.boss(foe.kind) && foe.left > BLOW) foe.left -= BLOW;
                beat(foe, plunged ? PLUNGED : LANDED);
                // One sign after another without touching the ground: each is worth one plus more than the last.
                if (foe.flat && ++combo > 1) bonus += Math.min(4, combo - 1);
            } else if (safe <= 0 && offIt <= 0 && foe.dazed <= 0) {
                cause = foe.kind == RunLevels.YEOBAEK ? SLASHED : TOUCHED;
                knock = x + WIDE / 2 < foe.x + foe.wide() / 2 ? -1 : 1;
                // (One of a swarm, small as it is, bites for half a heart; one of the unique creatures harder.)
                if (hurt(false, (foe.swarm ? 1 : 2) + TIER_BITE[foe.tier] + (RunUniques.is(foe.kind) ? RunUniques.bite(foe.kind, level.number) : 0))) return;
            }
        }
        if (y > bottomAt((int) Math.floor(x + WIDE / 2)) + 1) {
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
        if (!passed && middle >= level.gate && middle < level.main && gateOpen()) state = WON;
    }

    /** Whether she has passed the gate of this land and gone on: what passing it gave has been had, and the way on to the next land is open. */
    private boolean passed;

    boolean passed() { return passed; }

    /** Passing the gate cleared the land, and she goes on past it, on her way to the next: nothing waits. */
    void carryOn() {
        if (state != WON) return;
        passed = true;
        state = PLAYING;
    }

    /** She is put at the gate of a land she has passed before, which takes her nowhere, as she has been through it: from there she goes on to the next. */
    void atGate() {
        passed = true;
        // (Whoever waited at it was dealt with when she won it: she is not there now.)
        for (Foe foe : foes) {
            if (!RunLevels.boss(foe.kind) || RunLevels.rover(foe.kind)) continue;
            foe.left = 0;
            foe.flat = true;
            foe.away = true;
        }
        locked = false;
        int column = Math.min(level.main - 4, level.gate + 1);
        place(column + (1 - WIDE) / 2, Math.max(0, RunLevels.top(level, column)) - TALL - .05f);
        startX = x;
        startY = y;
    }

    /** The stars a land won gives: one for reaching its gate, a second for six in ten of its pluses, a third for nine in ten. Nothing else counts, how long it took least of all. */
    static int starsFor(float share) { return 1 + (share >= .6f ? 1 : 0) + (share >= .9f ? 1 : 0); }

    /** How far before the end of the land the way into the next one opens. */
    static final float THROUGH = 3.2f;

    /** She has gone into the way that leads on to the next land. */
    boolean through() { return passed && state == PLAYING && room < 0 && x + WIDE / 2 >= level.main - THROUGH; }

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
        put(colour, (colour == BLACK ? 5 : 9) * perks.lasting * perks.formLonger * forms(), false);
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
        wear(colour, 9 * perks.lasting * perks.formLonger * forms());
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
        // White washes black off her.
        if (colour == WHITE && cursed > 0) {
            cursed = 0;
            cleansed++;
        }
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
     * wore is gone, and for as long as it lies on her she can call on none and pick up none
     * (but a white ribbon, which washes it off). And it weighs on her, a little more the
     * further on the land: heavy ink, slower and with lower jumps; ink in her eyes, the edges
     * of what she sees dark; weak, her blows lighter and the pluses she wins halved; and it
     * drips off her, a little of her HP at a time, never her last heart.
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

    // What black does to her while it lies on her

    /** Whether black lies on her now, and weighs on her (on the white that is truly hers it is one of her colours, and does not). */
    boolean inked() { return cursed > 0 && !mastered; }

    /** Black's weights: how much slower she runs, how much lower she jumps, how much lighter her blows, in percent; how often it drips. */
    static final float CURSE_JUMP = .88f, DRIP_EVERY = 5;
    static final String[] CURSE_NAMES = {"Heavy ink: slower, lower jumps", "Ink in her eyes: the edges dark", "Weak: lighter blows, half the pluses",
            "Dripping: a little HP at a time, never the last heart"};

    /** How fast she runs under black: a little slower, more so the further on. */
    float curseSlow() { return 1 - Math.min(.26f, .18f + level.number / 1000f); }

    /** How much lighter her blows are under black, in percent. */
    int curseWeak() { return Math.min(30, 20 + level.number / 20); }

    /** How much HP a drip of black takes. */
    int curseDrip() { return Math.min(10, 3 + level.number / 30); }

    private float dripClock;
    private int drips, cleansed;

    /** Counts that only go up: drips of black, and black washed off by a white ribbon. */
    int drips() { return drips; }
    int cleansed() { return cleansed; }

    /** As time goes, black drips off her: a little HP, never so much that she has less than a heart. */
    private void drip(float seconds) {
        if (!inked() || state != PLAYING) {
            dripClock = 0;
            return;
        }
        if ((dripClock += seconds) < DRIP_EVERY) return;
        dripClock = 0;
        if (hp <= RunSkills.HEART) return;
        hp = Math.max(RunSkills.HEART, hp - curseDrip());
        drips++;
    }
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
            fell(foe, SURE);
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
        if (hp >= maxHp()) return false;
        hp = Math.min(maxHp(), hp + RunSkills.HEART);
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
            wear(colour, (colour == WHITE ? 12 : RIBBON) * perks.lasting * perks.formLonger * forms());
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
        // Ink splashes where it lands (no more than a few at once).
        if (splashes.size() < 12) splashes.add(new float[]{foe.x + foe.wide() / 2, foe.y + foe.tall() * .5f, 0});
        // A mace's smash, and a hammer's blow worked on to the last step, leave what they hit reeling (not those who wait).
        if (!foe.flat && !RunLevels.boss(foe.kind) && (by == MACED || by == HAMMERED && perks.levels[HAMMER] >= 3)) {
            foe.dazed = Math.max(foe.dazed, perks.levels[MACE] >= 1 || by == HAMMERED ? .9f : .6f);
        }
        // The tree's shockwave, once the blow is done with: a burst round where it landed, which sends out none of its own.
        if (quakeNow) {
            quakeNow = false;
            quaking = true;
            shockwaves++;
            blast(quakeX, quakeY, QUAKE_WIDE, by);
            quaking = false;
        }
    }

    /** A wand's bolt has hit something: what its element does. */
    private void elemental(Shot shot, Foe hit) {
        if (shot.element == RunArms.FIRE) fires.add(new float[]{shot.x, shot.y, 1.1f, 1.6f, .45f});
        else if (shot.element == RunArms.ICE && !hit.flat && !RunLevels.boss(hit.kind)) hit.dazed = Math.max(hit.dazed, 1.1f);
        else if (shot.element == RunArms.INKY) blast(shot.x, shot.y, 1.2f, BOLTED);
        else if (shot.element == RunArms.LIGHTNING && shot.chain > 0) {
            Foe next = null;
            float best = 5 * 5;
            for (Foe foe : foes) {
                if (foe == hit || foe.flat || absent(foe) || foe.kind == RunLevels.TARGET) continue;
                float across = foe.x + foe.wide() / 2 - shot.x, down = foe.y + foe.tall() / 2 - shot.y, far = across * across + down * down;
                if (far < best) {
                    best = far;
                    next = foe;
                }
            }
            if (next == null) return;
            Shot jump = sendAt(next, BOLTED, WAND, shot.x, shot.y, RunArms.speed(WAND) * 1.4f, 6, 1);
            jump.element = RunArms.LIGHTNING;
            jump.chain = shot.chain - 1;
            jump.last = hit;
        }
    }

    /** What the blow being dealt is made with: nothing in particular; something nothing stands against; or the kind of thing thrown. */
    private static final int PLAIN = -1, SURE = -2, BLASTED = -3, BEAMED = -4, SWORDED = -5, PLUNGED = -6;
    /** And a blow that is her coming down on something: as plain as a blow can be, but told from a swing. */
    private static final int LANDED = -8;
    private int with = PLAIN;

    /**
     * Everything she has dealt with, in order, for the hunts she has taken on: the kind of
     * each, and the way of it as {@link RunHunts} counts ways: landed on, nought; with a
     * thing she holds or throws, its number and one; anything else, a colour of hers, a pet,
     * the notes, -1.
     */
    private final List<int[]> fells = new ArrayList<>();

    List<int[]> fells() { return fells; }

    /** How many blows the blow being dealt counts for against a creature: by what the smith has made of the thing it is dealt with; or, landed on, by what she wears on her feet. */
    private int power() {
        // (Coming down hard with the katana in her hands, its edge comes down with her.)
        if (with == LANDED || with == PLUNGED) return 1 + perks.stomp + Math.max(0, skillOf(RunSkills.E_STOMP)) + (with == PLUNGED && weapon == KATANA ? RunArms.blows(KATANA, perks.levels[KATANA]) : 0);
        int thing = thingOf(with);
        // (The third cut of three in a row, and the cut that follows a dash, count for one more.)
        int found = perks.item != 0 && thing == RunArms.kindOf(perks.item) ? RunArms.itemBlows(perks.item) : 0;
        return thing == UNARMED ? 1 : Math.max(1, RunArms.blows(thing, perks.levels[thing]) + found) + (with == CUT && (chain == 3 || dashCut) ? 1 : 0);
    }

    /** The thing a blow was dealt with, from what the blow is made with; or nothing. */
    private static int thingOf(int by) {
        return by == PLAIN ? BRUSH : by == SWORDED ? SWORD : by == BLASTED || by == BOOK ? BOOK : by == PEN || by == KNIFE || by == GUN ? by
                : by == PAGE || by == PANELLED ? TABLET : by == THROWN_KEY ? KEYS : by == CUT ? KATANA : by == LANCED ? LANCE : by == SHELLED ? CANNON
                : by == WHIPPED ? WHIP : by == GREAT ? GREATSWORD : by == SUMMONED ? STAFF : by == MACED ? MACE : by == HAMMERED ? HAMMER : by == BOLTED ? WAND
                : by == REAPED ? SCYTHE : by == PUNCHED || by == UPPERCUT || by == HAYMAKER ? KNUCKLES : UNARMED;
    }

    /** How a blow is seen landing, by what dealt it: a cut (0), a splash of ink (1), a thud (2), or a sparkle of magic (3). */
    static final int CUTS = 0, SPLASHES = 1, THUDS = 2, SPARKLES = 3;

    static int flavourOf(int by) {
        if (by == BEAMED || by == SURE || by == STEAM || by == FIRE || by == WATER) return SPARKLES;
        switch (thingOf(by)) {
            case SWORD: case KATANA: case GREATSWORD: case SCYTHE: case LANCE: case KNIFE: case WHIP: return CUTS;
            case BRUSH: case PEN: case GUN: return SPLASHES;
            case WAND: case STAFF: return SPARKLES;
            default: return THUDS;
        }
    }

    private boolean dashCut;

    private void fell(Foe foe, int by) {
        int thing = thingOf(by), way = by == LANDED || by == PLUNGED ? RunHunts.LANDING : thing == UNARMED ? -1 : RunHunts.wayOf(thing);
        fells.add(new int[]{foe.kind, way});
    }

    private void beat(Foe foe) {
        if (!open(foe)) {
            chip(foe);
            return;
        }
        // A candle wisp is put out only by water, and a rime flake melted only by fire; steam does for both. Anything else goes through them.
        if ((foe.kind == RunLevels.WISP || foe.kind == RunLevels.RIME) && with != SURE && with != BEAMED && with != STEAM && with != (foe.kind == RunLevels.WISP ? WATER : FIRE)) {
            fizzled++;
            fizzledAt = foe.kind;
            foeX = foe.x + foe.wide() / 2;
            foeY = foe.y + foe.tall();
            return;
        }
        boolean waits = RunLevels.boss(foe.kind);
        int phase = waits ? phase(foe) : 0;
        if (!waits && RunUniques.is(foe.kind) && RunUniques.way(foe.kind) == RunUniques.HERMIT && foe.turn == 1 && with != SURE && with != LANDED && with != PLUNGED) {
            clangs++;
            clangX = foe.x + foe.wide() / 2;
            clangY = foe.y + foe.tall() * .5f;
            return;
        }
        foeX = foe.x + foe.wide() / 2;
        foeY = foe.y + foe.tall();
        if (waits) blows++;
        // One with a key in it: the next blow that is not a key counts twice.
        if (!waits && foe.unlocked > 0 && with != THROWN_KEY && foe.left > BLOW) {
            foe.left -= BLOW;
            foe.unlocked = 0;
            doubled++;
        }
        // Against a creature a blow counts for as many as the smith has made what dealt it count for; against those who wait, for one.
        // (And her level makes a part of a blow more, gathered up into whole ones.)
        int dealt = geared(foe, waits ? 1 : dealt(), waits);
        // (Her tree makes some blows harder: on one nearly beaten, on one not yet hurt, after a rest.)
        boolean hers = thingOf(with) != UNARMED;
        if (!waits && hers) dealt = Math.max(1, Math.round(dealt * (1 + bonus(foe) / 100f)));
        // A shield in the way: a blow too weak rings off it; a strong one takes a piece of it (two, with the tree's shieldbreaking at its
        // strongest), and the last one breaks it.
        if (!waits && atShield(foe)) {
            foe.guardMet = 0;
            clangX = foe.x + foe.wide() / 2 + (foe.leftward ? -.3f : .3f);
            clangY = foe.y + foe.tall() * .45f;
            int breaking = hers ? Math.max(0, skillOf(RunSkills.E_SHIELD_BREAK)) : 0;
            if (dealt * (1 + breaking / 100f) < foe.guardNeeds) {
                clangs++;
            } else if ((foe.guard -= breaking >= 100 ? 2 : 1) <= 0) {
                foe.guard = 0;
                shieldsBroken++;
                // (A shield breaking is seen as a great blow is, without a number.)
                if (hits.size() < 24) hits.add(new float[]{clangX, clangY - foe.tall() * .2f, 0, 1, 0, 1, 0, flavourOf(with), 0, 1});
            }
            return;
        }
        foe.struck = 0;
        if (hers) landed(foe, waits);
        boolean stun = hers && !waits && chance(skillOf(RunSkills.E_STUN));
        // (What it was dealt with, for how the blow is seen: a cut, a splash of ink, a thud, a sparkle; and, below, whether it was the last.)
        float[] hit = new float[]{foe.x + foe.wide() / 2, foe.y, dealt, critAge == 0 ? 1 : 0, 0, foe.most > BLOW ? 1 : 0, 0, flavourOf(with), 0, 0};
        if (hits.size() < 24) hits.add(hit);
        // A creature is knocked back a little, away from her, where there is room.
        foe.knocked = foe.x + foe.wide() / 2 < x + WIDE / 2 ? -1 : 1;
        if (!waits && foe.left > dealt && !RunLevels.flier(foe.kind)) {
            // (And not off the edge of what it stands on: one that walks is knocked back only as far as there is ground.)
            float push = foe.knocked * .22f, to = foe.x + push, from = foe.x;
            int row = (int) Math.floor(foe.y + foe.tall() - .05f);
            if (!solid(at((int) Math.floor(push < 0 ? to : to + foe.wide()), row))) {
                foe.x = to;
                if (walks(foe.kind) && heldAt(foe, from) && !held(foe)) foe.x = from;
            }
        }
        if ((foe.left -= dealt) <= 0) {
            hit[8] = 1;
            foe.left = 0;
            foe.flat = true;
            foe.y = foe.ground;
            foe.act = 0;
            flattened++;
            fell(foe, with);
            if (hers || with == LANDED || with == PLUNGED) killed();
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
            if (RunUniques.is(foe.kind) && RunUniques.way(foe.kind) == RunUniques.SPLITTER && foe.did == 0) halve(foe);
            if (RunLevels.rover(foe.kind)) {
                for (Shot shot : shots) if (!shot.hers) shot.left = 0;
                return;
            }
            if (waits && !foe.spawned && !standing()) won();
            else if (waits && !foe.spawned && phase(foe) > phase) changed();
            return;
        }
        if (!waits) {
            // A big one flinches, and can be hit again at once; knocked off its feet, it goes at a crawl for a moment.
            foe.dazed = .15f;
            if (stun) {
                foe.stunned = STUN_LASTS;
                stuns++;
            }
            return;
        }
        foe.dazed = STUNNED;
        foe.taken++;
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
        // Lucy's last third is not taken from her on the ground: it is the moment of the black ribbon.
        if (foe.kind == RunLevels.LUCY && phase(foe) >= 2) {
            decide(foe);
            return;
        }
        if (phase(foe) > phase) {
            if (foe.spawned || RunLevels.rover(foe.kind)) {
                // The Old Book changes by himself, and so does one met in the middle of a land.
                phases++;
                foe.flourish = FLOURISH;
                shielded(foe);
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
        for (Foe other : foes) {
            if (!RunLevels.boss(other.kind) || other.flat) continue;
            other.flourish = FLOURISH;
            shielded(other);
        }
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
            // (Lucy with her dragon side out keeps it: that part of the fight begins again from its own beginning.)
            foe.left = foe.kind == RunLevels.LUCY && phase(foe) >= 1 ? foe.most - (foe.most + 2) / 3 : foe.most;
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
        foe.left = healthOf(kind);
        foe.most = foe.left;
        float lo = level.arena >= 0 ? level.arena + 1.3f : 0, hi = (level.arena >= 0 ? level.gate - .3f : columns) - foe.wide();
        foe.x = Math.max(lo, Math.min(hi, middle - foe.wide() / 2));
        foe.ground = floor - foe.tall();
        foe.y = foe.ground;
        foe.home = foe.x;
        // A moment in which it can neither hurt nor be hurt.
        foe.dazed = .6f;
        born.add(foe);
    }

    /** How long the thing in her hands rests between uses: less for a weapon found of a better rarity; an ink gun that is a machine gun, far less. */
    private float restOf(int kind) {
        float rest = RunArms.rest(kind, perks.levels[kind]);
        if (perks.item == 0 || RunArms.kindOf(perks.item) != kind) return rest;
        return rest * RunArms.itemRest(perks.item) * (kind == GUN && RunArms.machine(perks.item) ? .4f : 1);
    }

    /** How far the thing in her hands reaches: further for a weapon found of a better rarity. */
    private float reachOf(int kind) {
        float reach = reachGear(kind, perks.levels[kind]) + (boost > 0 ? RunArms.BOOST_SWING : 0);
        return perks.item != 0 && RunArms.kindOf(perks.item) == kind ? reach * RunArms.itemReach(perks.item) : reach;
    }

    /** The creatures nearest her, up to so many, within a reach: what spirits and bolts go after. */
    private List<Foe> nearest(int count, float within) {
        List<Foe> near = new ArrayList<>();
        for (Foe foe : foes) if (!foe.flat && !absent(foe) && foe.kind != RunLevels.TARGET && Math.hypot(foe.x + foe.wide() / 2 - x - WIDE / 2, foe.y + foe.tall() / 2 - y - TALL / 2) < within) near.add(foe);
        near.sort((one, other) -> Float.compare(Math.abs(one.x - x) + Math.abs(one.y - y), Math.abs(other.x - x) + Math.abs(other.y - y)));
        return near.size() > count ? near.subList(0, count) : near;
    }

    /** A spirit or a bolt sent at a creature, or straight ahead if there is none. */
    private Shot sendAt(Foe at, int by, int of, float fromX, float fromY, float speed, float reach, float way) {
        float across = way, down = 0;
        if (at != null) {
            float toX = at.x + at.wide() / 2 - fromX, toY = at.y + at.tall() / 2 - fromY, far = Math.max(.3f, (float) Math.hypot(toX, toY));
            across = toX / far;
            down = toY / far;
        }
        Shot shot = shoot(SLASH, true, fromX, fromY, across * speed, down * speed, reach / speed);
        shot.by = by;
        shot.of = of;
        shot.high = .4f;
        return shot;
    }

    /** The six found in the lands, used: what a tap does with each. Whether it was one of them. */
    private boolean useFound(int level, float way, float fromX, float fromY) {
        if (weapon == GREATSWORD) {
            // A great swing: what is in front of her and well over her head; worked on, a wave ahead too.
            greats++;
            float reach = reachOf(GREATSWORD), from = facingLeft ? x - reach : x + WIDE;
            sweep(GREAT, from, from + reach, y + TALL - areaGear(GREATSWORD, level), y + TALL);
            swing = SWING * 1.5f;
            swungWith = GREATSWORD;
            if (level >= 3) wave(SWORD, 2, way, fromX, fromY);
        } else if (weapon == STAFF) {
            // Spirits, at the nearest creatures.
            summons++;
            List<Foe> near = nearest(level >= 1 ? 3 : 2, reachOf(STAFF));
            int count = level >= 1 ? 3 : 2;
            for (int spirit = 0; spirit < count; spirit++) {
                Shot sent = sendAt(near.isEmpty() ? null : near.get(spirit % near.size()), SUMMONED, STAFF, fromX, fromY - .3f - spirit * .25f, RunArms.speed(STAFF), reachOf(STAFF), way);
                sent.pierce = level >= 2 ? 99 : 0;
            }
        } else if (weapon == MACE) {
            // A smash at arm's length: what it hits reels.
            smashes++;
            float reach = reachOf(MACE), from = facingLeft ? x - reach : x + WIDE;
            sweep(MACED, from, from + reach, y + TALL - areaGear(MACE, level), y + TALL);
            swing = SWING;
            swungWith = MACE;
        } else if (weapon == HAMMER) {
            // Slammed down: what is in front of her, and a shockwave along the ground both ways.
            slams++;
            float reach = reachOf(HAMMER), from = facingLeft ? x - reach : x + WIDE, quake = level >= 1 ? 3.4f : 2.6f;
            sweep(HAMMERED, from, from + reach, y + TALL - 1.4f, y + TALL);
            sweep(HAMMERED, x - quake, x + WIDE + quake, y + TALL - .7f, y + TALL + .2f);
            booms.add(new float[]{x + WIDE / 2, y + TALL, quake, 0, 0});
            quakesMade++;
            swing = SWING;
            swungWith = HAMMER;
        } else if (weapon == WAND) {
            // A bolt of its element, at the nearest thing in front of her, or straight ahead.
            bolts++;
            int element = perks.item != 0 && RunArms.kindOf(perks.item) == WAND ? RunArms.element(perks.item) : RunArms.ICE;
            List<Foe> near = nearest(2, reachOf(WAND));
            for (int which = 0; which < (level >= 2 ? 2 : 1); which++) {
                Foe at = null;
                for (Foe foe : near) if (at == null && (foe.x > x) == !facingLeft) at = foe;
                Shot bolt = sendAt(at, BOLTED, WAND, fromX, fromY - which * .4f, RunArms.speed(WAND), reachOf(WAND), way);
                bolt.element = element;
                bolt.chain = element == RunArms.LIGHTNING ? 2 : 0;
                bolt.pierce = level >= 3 ? 99 : 0;
            }
        } else if (weapon == KNUCKLES) {
            // Punches: jab, jab, and an uppercut that leaves what it hits reeling; at a run, a dash punch that carries her into it.
            punches++;
            punchChain = sincePunch < RunArms.PUNCH_RHYTHM ? punchChain % 3 + 1 : 1;
            sincePunch = 0;
            float reach = reachOf(KNUCKLES), from = facingLeft ? x - reach : x + WIDE;
            if (onGround && Math.abs(speedAcross) > RUN * .8f) {
                dashPunches++;
                speedAcross = (facingLeft ? -1 : 1) * RUN * 2.4f;
                float far = RunArms.DASH_PUNCH;
                sweep(PUNCHED, facingLeft ? x - far : x + WIDE, facingLeft ? x : x + WIDE + far, y + TALL - areaGear(KNUCKLES, level), y + TALL);
            } else if (punchChain == 3) {
                uppercuts++;
                for (Foe foe : foes) {
                    if (foe.flat || absent(foe) || RunLevels.boss(foe.kind) || foe.x >= from + reach || foe.x + foe.wide() <= from || foe.y >= y + TALL || foe.y + foe.tall() <= y - 1.4f) continue;
                    beat(foe, UPPERCUT);
                    if (!foe.flat) foe.dazed = Math.max(foe.dazed, level >= 2 ? 1.2f : .8f);
                }
            } else {
                sweep(PUNCHED, from, from + reach, y + TALL - areaGear(KNUCKLES, level), y + TALL);
            }
            swing = SWING * .6f;
            swungWith = KNUCKLES;
        } else if (weapon == SCYTHE) {
            // A wide arc in front of her, and behind her too.
            reaps++;
            float reach = reachOf(SCYTHE), from = facingLeft ? x - reach : x + WIDE, behind = facingLeft ? x + WIDE : x - reach * .55f;
            float top = y + TALL - areaGear(SCYTHE, level) - (level >= 1 ? .3f : 0);
            sweep(REAPED, from, from + reach, top, y + TALL);
            sweep(REAPED, behind, behind + reach * .55f, top, y + TALL);
            swing = SWING * 1.3f;
            swungWith = SCYTHE;
        } else {
            return false;
        }
        return true;
    }

    /** The six found in the lands, held: the heavy thing of each. Whether it was one of them. */
    private boolean heavyFound(int level, float way) {
        if (weapon == GREATSWORD) {
            // A spinning slam: both sides of her, and the ground cracks.
            greats++;
            float wide = 2.5f + (level >= 2 ? .8f : 0);
            sweep(GREAT, x - wide, x + WIDE + wide, y + TALL - 2, y + TALL + .2f);
            booms.add(new float[]{x + WIDE / 2, y + TALL, wide, 0, 0});
            quakesMade++;
            return true;
        }
        if (weapon == STAFF) {
            // The great summoning: a ring of spirits going out after everything near.
            summons++;
            int count = level >= 3 ? 16 : 8;
            List<Foe> near = nearest(count, 12);
            for (int spirit = 0; spirit < count; spirit++) {
                double turn = spirit * Math.PI * 2 / count;
                Foe at = near.isEmpty() ? null : near.get(spirit % near.size());
                Shot sent = sendAt(at, SUMMONED, STAFF, x + WIDE / 2 + (float) Math.cos(turn) * .8f, y + TALL / 2 + (float) Math.sin(turn) * .8f, RunArms.speed(STAFF) * .9f, 12,
                        (float) Math.cos(turn));
                sent.pierce = 99;
            }
            return true;
        }
        if (weapon == MACE) {
            // A whirl round her, and round again once the smith has worked on it.
            whirls++;
            sweep(MACED, x - 1.8f, x + WIDE + 1.8f, y + TALL - 1.8f, y + TALL);
            whirlLeft = level >= 2 ? .25f : 0;
            return true;
        }
        if (weapon == HAMMER) {
            // A leap and a slam: a shockwave far along the ground both ways.
            slams++;
            float quake = level >= 2 ? 6.5f : 5;
            speedDown = Math.min(speedDown, -7);
            sweep(HAMMERED, x - quake, x + WIDE + quake, y + TALL - 1, y + TALL + .3f);
            booms.add(new float[]{x + WIDE / 2, y + TALL, quake, 0, 0});
            quakesMade++;
            return true;
        }
        if (weapon == WAND) {
            // A fan of bolts.
            bolts++;
            int element = perks.item != 0 && RunArms.kindOf(perks.item) == WAND ? RunArms.element(perks.item) : RunArms.ICE;
            for (int which = 0; which < 5; which++) {
                double turn = (which - 2) * .22;
                Shot bolt = shoot(SLASH, true, x + WIDE / 2 + way * .5f, y + TALL * .45f, way * (float) Math.cos(turn) * RunArms.speed(WAND), (float) Math.sin(turn) * RunArms.speed(WAND),
                        reachOf(WAND) / RunArms.speed(WAND));
                bolt.by = BOLTED;
                bolt.of = WAND;
                bolt.high = .4f;
                bolt.element = element;
                bolt.chain = element == RunArms.LIGHTNING ? 1 : 0;
            }
            return true;
        }
        if (weapon == KNUCKLES) {
            // The haymaker: every shield in front of her broken outright, and a blow that carries well past her fist.
            haymakers++;
            float far = RunArms.HAYMAKER_REACH * (level >= 3 ? 2 : 1), from = facingLeft ? x - far : x + WIDE, to = from + far;
            for (Foe foe : foes) {
                if (foe.flat || absent(foe) || foe.guard <= 0 || foe.x >= to || foe.x + foe.wide() <= from || Math.abs(foe.y + foe.tall() - y - TALL) > 1.6f) continue;
                foe.guard = 0;
                foe.guardMet = 0;
                shieldsBroken++;
            }
            sweep(HAYMAKER, from, to, y + TALL - 1.8f, y + TALL);
            return true;
        }
        if (weapon == SCYTHE) {
            // The reaping spin; and its hook pulls what stands in front of her close.
            reaps++;
            float hook = level >= 3 ? 6 : 4.5f;
            for (Foe foe : foes) {
                if (foe.flat || absent(foe) || RunLevels.boss(foe.kind) || foe.kind == RunLevels.TARGET || Math.abs(foe.y + foe.tall() - y - TALL) > 1.5f) continue;
                float ahead = facingLeft ? x - foe.x - foe.wide() : foe.x - x - WIDE;
                if (ahead < 0 || ahead > hook) continue;
                foe.x = facingLeft ? x - foe.wide() - .4f : x + WIDE + .4f;
            }
            sweep(REAPED, x - 3, x + WIDE + 3, y + TALL - 2.2f, y + TALL);
            reapLeft = level >= 2 ? .25f : 0;
            return true;
        }
        return false;
    }

    /** She uses what she carries: throws it, fires it, or begins a swing of it. */
    private void use() {
        sinceStrike = 0;
        strikes++;
        chop();
        if (inWater) inkBolt();
        copied();
        float way = facingLeft ? -1 : 1, fromX = x + WIDE / 2 + way * .5f, fromY = y + TALL * .45f;
        // How fast it flies and how far it reaches are the table's; a find of something she has already makes it reach a third as far again.
        int level = perks.levels[weapon];
        float speed = RunArms.speed(weapon), lasts = speed > 0 ? reachGear(weapon, level) / speed * (boost > 0 ? RunArms.BOOST_FAR : 1) : 0;
        if (useFound(level, way, fromX, fromY)) return;
        if (weapon == PEN || weapon == KNIFE) {
            shoot(weapon, true, fromX, fromY, way * speed, 0, lasts).pierce = piercesGear(weapon, level);
            if (weapon == KNIFE) wave(KNIFE, level, way, fromX, fromY);
        } else if (weapon == BOOK) {
            shoot(BOOK, true, fromX, fromY - .3f, way * speed + speedAcross * .4f, -11, 3);
            if (--books <= 0) emptied();
        } else if (weapon == GUN) {
            shoot(GUN, true, fromX, fromY, way * speed, 0, lasts).chain = RunArms.chains(level);
            if (--rounds <= 0) emptied();
        } else if (weapon == TABLET) {
            // A page, flicked out and back: it slows as it goes, turns, and comes back through whatever is in its way.
            // (Two of them, one over the other, once the smith has worked on it.)
            for (int which = 0; which < RunArms.pages(level); which++) {
                Shot page = shoot(PAGE, true, fromX, fromY - which * .6f, way * speed, 0, RunArms.PAGE_TURNS * 2);
                page.spin = way * speed * (boost > 0 ? RunArms.BOOST_FAR : 1);
                page.through = true;
            }
        } else if (weapon == KEYS) {
            // A key. Once in a while one fails, and nothing leaves her hand: never twice running, never while one of those who wait can
            // be hurt, and the next is ready almost at once.
            boolean wide = false;
            for (Foe foe : foes) wide |= RunLevels.boss(foe.kind) && !foe.flat && !absent(foe) && open(foe);
            if (++keyThrows % RunArms.FAILS_IN == 0 && sinceDud > 20 && !wide) {
                duds++;
                sinceDud = 0;
                sinceStrike = RunArms.rest(KEYS, level) - .12f;
                return;
            }
            shoot(THROWN_KEY, true, fromX, fromY, way * speed, 0, lasts).pierce = piercesGear(KEYS, level);
        } else if (weapon == KATANA) {
            drawn(level, way);
        } else if (weapon == LANCE) {
            // A thrust: everything in front of her, a long way out and no higher than the lance.
            thrusts++;
            float reach = reachGear(LANCE, level) + (boost > 0 ? RunArms.BOOST_SWING : 0), from = facingLeft ? x - reach : x + WIDE;
            sweep(LANCED, from, from + reach, y + TALL * .15f, y + TALL * .15f + areaGear(LANCE, level));
            if (RunArms.burst(LANCE, level) > 0) blast(facingLeft ? from : from + reach, y + TALL * .5f, RunArms.burst(LANCE, level), LANCED);
        } else if (weapon == CANNON) {
            // A ball of ink, lobbed: it bursts where it lands, or on the first thing it meets.
            for (int which = 0; which < RunArms.balls(level); which++) {
                shoot(INKBALL, true, fromX, fromY - .3f, way * (speed + which * 3.5f) + speedAcross * .4f, -9, 3).by = SHELLED;
            }
        } else if (weapon == WHIP) {
            // A lash: what is in front of her, from her feet to well over her head.
            lashes++;
            float reach = reachGear(WHIP, level) + (boost > 0 ? RunArms.BOOST_SWING : 0), from = facingLeft ? x - reach : x + WIDE;
            sweep(WHIPPED, from, from + reach, y + TALL - areaGear(WHIP, level), y + TALL);
            wave(WHIP, level, way, fromX, fromY);
        } else {
            swing = SWING;
            swungWith = weapon;
            wave(weapon, level, way, fromX, fromY);
        }
    }

    /**
     * A stroke that takes everything in a box: every creature in it is dealt a blow as with
     * something, and what was thrown at her into it is gone. Returns how many it met.
     */
    private int sweep(int by, float from, float to, float top, float bottom) {
        if (sweeping) swept.add(new float[]{by, from, to, top, bottom});
        breakSecrets(from, to, top, bottom);
        crack(from, to, top, bottom);
        int met = 0;
        for (int index = 0; index < foes.size(); index++) {
            Foe foe = foes.get(index);
            if (foe.flat || absent(foe) || foe.dazed > 0 || foe.x - spare(foe, true) >= to || foe.x + foe.wide() + spare(foe, true) <= from || foe.y - spare(foe, false) >= bottom
                    || foe.y + foe.tall() <= top) continue;
            beat(foe, by);
            met++;
        }
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            if (shot.hers || shot.x < from - .2f || shot.x > to + .2f || shot.y < top - .2f || shot.y > bottom + .2f) continue;
            if (chance(skillOf(RunSkills.E_REFLECT))) sendBack(shot);
            else shots.remove(index);
            turned++;
        }
        return met;
    }

    /**
     * The katana is drawn: a cut, further than it looks, and it is back in its sheath. A
     * tap that comes soon after the last is the next of three in a row, and the third is
     * the longest and the heaviest. In the air the cut is downward, and she comes down the
     * faster for it.
     */
    private void drawn(int level, float way) {
        if (!onGround) {
            airCuts++;
            chain = 0;
            sweep(CUT, x - .5f, x + WIDE + .5f, y + TALL * .4f, y + TALL + 1.7f);
            speedDown = Math.max(speedDown, 11);
            return;
        }
        chain = cuts > 0 && sinceCut <= RunArms.RHYTHM && chain > 0 && chain < 3 ? chain + 1 : 1;
        sinceCut = 0;
        cuts++;
        float reach = reachGear(KATANA, level) + (boost > 0 ? RunArms.BOOST_SWING : 0) + (chain == 3 ? RunArms.FINISH_FAR : 0), from = facingLeft ? x - reach : x + WIDE;
        sweep(CUT, from, from + reach, y + TALL - areaGear(KATANA, level), y + TALL);
        wave(KATANA, level, way, x + WIDE / 2 + way * .5f, y + TALL * .45f);
    }

    /** Whether there is room for her at a place along her line: nothing firm in it, and no edge of the land. */
    private boolean clear(float across) {
        if (across < 0 || across + WIDE > columns) return false;
        for (int row = (int) Math.floor(y + .05f); row <= (int) Math.floor(y + TALL - .05f); row++) {
            for (int column = (int) Math.floor(across + .02f); column <= (int) Math.floor(across + WIDE - .02f); column++) if (wall(at(column, row))) return false;
        }
        return true;
    }

    /**
     * The slash waves the smith has put into a sword, a knife or a brush fly from it: a
     * sword's and a brush's from her feet to as tall as they are, a knife's along its line,
     * three of them in a fan. Each deals its blow as the thing it flew from does.
     */
    private void wave(int of, int level, float way, float fromX, float fromY) {
        int count = RunArms.waves(of, level);
        if (count == 0) return;
        float speed = RunArms.waveSpeed(of), tall = RunArms.waveTall(of, level), lasts = RunArms.waveReach(of, level) / speed;
        for (int which = 0; which < count; which++) {
            double turn = of == KNIFE ? (which - (count - 1) / 2f) * .2 : 0;
            // (A sword's second wave follows a little behind its first.)
            Shot wave = shoot(SLASH, true, fromX - (of == SWORD ? which * way * 1.3f : 0), of == KNIFE ? fromY : y + TALL - tall / 2, way * (float) Math.cos(turn) * speed,
                    (float) Math.sin(turn) * speed, lasts + (of == SWORD ? which * 1.3f / speed : 0));
            wave.by = of == SWORD ? SWORDED : of == KNIFE ? KNIFE : of == KATANA ? CUT : of == WHIP ? WHIPPED : PLAIN;
            wave.of = of;
            wave.high = tall / 2;
            wave.pierce = RunArms.waveThrough(of, level) ? 99 : 0;
            wave.wipes = RunArms.waveWipes(of, level);
            waves++;
        }
    }

    /** Something of hers has come to its end, against something or where its reach ran out: a pen the smith has worked on bursts there. */
    private void ended(Shot shot) {
        // (And a ball of the cannon's ink bursts wherever it ends: the great one wider; and its ink may lie there for a while.)
        boolean ball = shot.kind == INKBALL && shot.by == SHELLED;
        float wide = shot.kind == PEN && shot.by == PEN ? RunArms.burst(PEN, perks.levels[PEN]) : ball ? (shot.high > 0 ? RunArms.SHELL_WIDE : RunArms.burst(CANNON, perks.levels[CANNON])) : 0;
        if (wide <= 0 || shot.spent || shot.pets || !shot.hers) return;
        shot.spent = true;
        blast(shot.x, shot.y, wide, ball ? SHELLED : PEN);
        if (ball && RunArms.lies(perks.levels[CANNON]) > 0) fires.add(new float[]{shot.x, shot.y, wide * .8f, RunArms.lies(perks.levels[CANNON]), .45f, 1});
    }

    /** An ink blot the smith has worked on has hit something: it splashes, and jumps on to the nearest creature that still stands. */
    private void blotted(Shot shot, Foe hit) {
        float wide = RunArms.burst(GUN, perks.levels[GUN]);
        if (wide > 0) blast(shot.x, shot.y, wide, GUN);
        if (shot.chain <= 0) return;
        Foe next = null;
        float best = 4.5f * 4.5f;
        for (Foe foe : foes) {
            if (foe == hit || foe.flat || absent(foe) || RunLevels.boss(foe.kind) || foe.kind == RunLevels.TARGET) continue;
            float across = foe.x + foe.wide() / 2 - shot.x, down = foe.y + foe.tall() / 2 - shot.y, far = across * across + down * down;
            if (far < best) {
                best = far;
                next = foe;
            }
        }
        if (next == null) return;
        float across = next.x + next.wide() / 2 - shot.x, down = next.y + next.tall() / 2 - shot.y, far = Math.max(.3f, (float) Math.hypot(across, down)), speed = RunArms.speed(GUN);
        Shot jump = shoot(GUN, true, shot.x, shot.y, across / far * speed, down / far * speed, far / speed + .08f);
        jump.chain = shot.chain - 1;
        jump.last = hit;
        chains++;
    }

    /**
     * What is left in the air a moment goes on: a burst is seen for a little while; pages
     * that burn take what stands in them, every so often, for as long as they burn; and a
     * wave of paint the smith has worked on wipes away what is thrown at her as it goes.
     */
    private void linger(float seconds) {
        for (int index = booms.size() - 1; index >= 0; index--) if ((booms.get(index)[4] += seconds) > .45f) booms.remove(index);
        for (int index = fires.size() - 1; index >= 0; index--) {
            float[] fire = fires.get(index);
            if ((fire[3] -= seconds) <= 0) {
                fires.remove(index);
                continue;
            }
            if ((fire[4] -= seconds) > 0) continue;
            fire[4] = .45f;
            for (int at = 0; at < foes.size(); at++) {
                Foe foe = foes.get(at);
                float across = foe.x + foe.wide() / 2 - fire[0], down = foe.y + foe.tall() / 2 - fire[1];
                if (foe.flat || absent(foe) || foe.dazed > 0 || across * across + down * down > fire[2] * fire[2]) continue;
                beat(foe, fire.length > 5 ? SHELLED : BLASTED);
            }
        }
        // A beat after a dash, everything on its line is cut; and a spin that goes round twice goes round again.
        sinceCut += seconds;
        sincePunch += seconds;
        if (dashLeft > 0 && (dashLeft -= seconds) <= 0) {
            dashCut = true;
            sweep(CUT, dashFrom, dashTo, dashY, dashY + TALL);
            dashCut = false;
        }
        if (spinLeft > 0 && (spinLeft -= seconds) <= 0) sweep(WHIPPED, x - RunArms.SPIN, x + WIDE + RunArms.SPIN, y + TALL - RunArms.SPIN_TALL, y + TALL);
        if (whirlLeft > 0 && (whirlLeft -= seconds) <= 0) sweep(MACED, x - 1.8f, x + WIDE + 1.8f, y + TALL - 1.8f, y + TALL);
        if (reapLeft > 0 && (reapLeft -= seconds) <= 0) sweep(REAPED, x - 3, x + WIDE + 3, y + TALL - 2.2f, y + TALL);
        // A swing's echo: what it swept is swept again.
        if (echoLeft > 0 && (echoLeft -= seconds) <= 0) {
            List<float[]> again = new ArrayList<>(echoBoxes);
            echoBoxes.clear();
            for (float[] box : again) sweep((int) box[0], box[1], box[2], box[3], box[4]);
        }
        for (int index = splashes.size() - 1; index >= 0; index--) if ((splashes.get(index)[2] += seconds) > .5f) splashes.remove(index);
        for (int index = smears.size() - 1; index >= 0; index--) if ((smears.get(index)[2] -= seconds) <= 0) smears.remove(index);
        for (int index = 0; index < shots.size(); index++) {
            Shot wave = shots.get(index);
            if (wave.kind != SLASH || !wave.wipes) continue;
            for (int other = shots.size() - 1; other >= 0; other--) {
                Shot thrown = shots.get(other);
                if (thrown.hers || Math.abs(thrown.x - wave.x) > .7f || Math.abs(thrown.y - wave.y) > wave.high + .2f) continue;
                shots.remove(other);
                if (other < index) index--;
                turned++;
            }
        }
    }

    /** The heavy thing: the tablet-book's panel, which takes all that stands in it and wipes away what is thrown into it; or the whole ring of keys, in a fan. */
    private void heavy() {
        float way = facingLeft ? -1 : 1;
        sinceStrike = restOf(weapon) - RunArms.heavyRest(weapon) * (1 - gear(RunLoot.SPECIAL) / 100f);
        strikes++;
        if (heavyFound(perks.levels[weapon], way)) return;
        if (weapon == KEYS) {
            fans++;
            float fromX = x + WIDE / 2 + way * .5f, fromY = y + TALL * .45f, speed = RunArms.speed(KEYS), lasts = reachGear(KEYS, perks.levels[KEYS]) / speed;
            int count = RunArms.fan(perks.levels[KEYS]);
            for (int key = 0; key < count; key++) {
                double turn = (key - (count - 1) / 2f) * RunArms.FAN_APART * 2 * Math.PI / 6;
                shoot(THROWN_KEY, true, fromX, fromY, way * (float) Math.cos(turn) * speed, (float) Math.sin(turn) * speed, lasts).pierce = piercesGear(KEYS, perks.levels[KEYS]);
            }
            return;
        }
        if (weapon == KATANA) {
            // The dash: along her line as far as there is room and no further than a wall, through whatever stands on it; nothing touches
            // her as she goes, and a beat later everything on the line is cut.
            dashes++;
            float fromX = x;
            for (float gone = 0; gone < RunArms.DASH && clear(x + way * .25f); gone += .25f) x += way * .25f;
            dashFrom = Math.min(fromX, x);
            dashTo = Math.max(fromX, x) + WIDE;
            dashY = y;
            dashLeft = RunArms.BEAT;
            safe = Math.max(safe, RunArms.BEAT + .2f);
            speedAcross = 0;
            sinceStrike = RunArms.rest(KATANA, perks.levels[KATANA]) - RunArms.dashRest(perks.levels[KATANA]);
            return;
        }
        if (weapon == LANCE) {
            // A jet of ink from its nib: straight, fast, and through everything.
            jets++;
            Shot jet = shoot(SLASH, true, x + WIDE / 2 + way * .6f, y + TALL * .5f, way * 26, 0, RunArms.jet(perks.levels[LANCE]) / 26);
            jet.by = LANCED;
            jet.of = LANCE;
            jet.high = .35f;
            jet.pierce = 99;
            return;
        }
        if (weapon == CANNON) {
            // The great ball: slow, straight, through everything, and a wide burst where it ends.
            shells++;
            Shot ball = shoot(INKBALL, true, x + WIDE / 2 + way * .6f, y + TALL * .4f, way * 10, 0, RunArms.SHELL / 10);
            ball.by = SHELLED;
            ball.high = .7f;
            ball.pierce = 99;
            return;
        }
        if (weapon == WHIP) {
            // The spin: what is on both sides of her; and, worked on, round again.
            spins++;
            sweep(WHIPPED, x - RunArms.SPIN, x + WIDE + RunArms.SPIN, y + TALL - RunArms.SPIN_TALL, y + TALL);
            spinLeft = RunArms.spins(perks.levels[WHIP]) > 1 ? .2f : 0;
            return;
        }
        panels++;
        float wide = RunArms.panelWide(perks.levels[TABLET]) + (boost > 0 ? RunArms.BOOST_SWING : 0), from = facingLeft ? x - wide : x + WIDE, to = from + wide;
        float top = y + TALL - RunArms.panelTall(perks.levels[TABLET]), bottom = y + TALL;
        panelX = facingLeft ? x : x + WIDE;
        panelY = top;
        panelLeft = facingLeft;
        for (Foe foe : foes) {
            if (foe.flat || absent(foe) || foe.dazed > 0 || foe.x - spare(foe, true) >= to || foe.x + foe.wide() + spare(foe, true) <= from || foe.y - spare(foe, false) >= bottom
                    || foe.y + foe.tall() <= top) continue;
            beat(foe, PANELLED);
        }
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            if (!shot.hers && shot.x > from - .2f && shot.x < to + .2f && shot.y > top - .2f && shot.y < bottom + .2f) {
                shots.remove(index);
                turned++;
            }
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
        // (Only the brush and the sword sweep as they swing: the rest strike once, when they are used, and swing only to be seen to.)
        if (swungWith != BRUSH && swungWith != SWORD) return;
        boolean brush = swungWith == BRUSH;
        float ahead = reachGear(swungWith, perks.levels[swungWith]) + (boost > 0 ? RunArms.BOOST_SWING : 0), behind = brush ? .3f : 0;
        float top = y + TALL - areaGear(swungWith, perks.levels[swungWith]), bottom = y + TALL;
        float from = facingLeft ? x - ahead : x - behind, to = facingLeft ? x + WIDE + behind : x + WIDE + ahead;
        for (Foe foe : foes) {
            if (foe.flat || absent(foe) || foe.dazed > 0 || foe.x - spare(foe, true) >= to || foe.x + foe.wide() + spare(foe, true) <= from || foe.y - spare(foe, false) >= bottom
                    || foe.y + foe.tall() <= top) continue;
            beat(foe, swungWith == SWORD ? SWORDED : PLAIN);
        }
        crack(from, to, top, bottom);
        breakSecrets(from, to, top, bottom);
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            if (shot.hers || shot.x < from - .2f || shot.x > to + .2f || shot.y < top - .2f || shot.y > bottom + .2f) continue;
            turned++;
            if (brush && !chance(skillOf(RunSkills.E_REFLECT))) {
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
        int level = perks.levels[BOOK];
        blast(atX, atY, RunArms.burst(BOOK, level), BLASTED);
        // Its pages burn for a while where it burst, once the smith has worked on it.
        if (RunArms.burns(level) > 0) fires.add(new float[]{atX, atY, RunArms.burst(BOOK, level) * .8f, RunArms.burns(level), .45f});
    }

    /** A burst of some width, dealt as with something: everything within it is dealt a blow, whoever waits behind her shield excepted. */
    private void blast(float atX, float atY, float wide, int by) {
        // (Wider for what she wears and her tree.)
        wide *= Math.max(.5f, 1 + gear(RunLoot.AREA) / 100f);
        booms.add(new float[]{atX, atY, wide, by, 0});
        for (int index = 0; index < foes.size(); index++) {
            Foe foe = foes.get(index);
            float across = foe.x + foe.wide() / 2 - atX, down = foe.y + foe.tall() / 2 - atY;
            if (foe.flat || absent(foe) || foe.dazed > 0 || across * across + down * down > wide * wide) continue;
            beat(foe, by);
        }
    }

    /** How wide the last book's burst was. */
    float blastWide() { return RunArms.burst(BOOK, perks.levels[BOOK]); }

    /** Moves on everything in the air, and sees what it has met. Returns whether she was hurt by it. */
    private boolean fly(float seconds) {
        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            shot.x += shot.across * seconds;
            shot.y += shot.down * seconds;
            shot.age += seconds;
            if (shot.kind == BOOK || shot.kind == NUT || shot.kind == ROLL || shot.kind == INKBALL && shot.high == 0) shot.down += BOOK_WEIGHT * seconds;
            else if (shot.kind == WATER) shot.down += 6 * seconds;
            else if (shot.kind == SAYING) shot.down = (float) Math.cos(shot.age * 7) * 3.2f;
            else if (shot.kind == GLOB) shot.down += BOOK_WEIGHT * seconds;
            else if (shot.kind == BOOMERANG) shot.across = shot.spin * (1 - shot.age);
            else if (shot.kind == PAGE) {
                shot.across = shot.spin * (1 - shot.age / RunArms.PAGE_TURNS);
                // Where it turns back it bursts, once the smith has made it do so.
                if (shot.hers && !shot.spent && shot.age >= RunArms.PAGE_TURNS && RunArms.burst(TABLET, perks.levels[TABLET]) > 0) {
                    shot.spent = true;
                    blast(shot.x, shot.y, RunArms.burst(TABLET, perks.levels[TABLET]), PAGE);
                }
            } else if (shot.kind == DEBRIS) shot.down = shot.age < HANGS ? 0 : 13;
            // A ledge is thin: it stops only what comes down on it, which makes it somewhere to shelter.
            char in = at((int) Math.floor(shot.x), (int) Math.floor(shot.y));
            // One of hers that meets the false wall of a secret space breaks it open, and is spent on it.
            if (shot.hers && !shot.pets && in == RunLevels.FAKE) {
                int secret = secretAt((int) Math.floor(shot.x), (int) Math.floor(shot.y));
                if (secret >= 0) {
                    foundSecret(secret, true);
                    ended(shot);
                    shots.remove(index);
                    continue;
                }
            }
            // One of hers that bounces turns back from a wall, while it has bounces left.
            if (shot.hers && shot.bounces > 0 && shot.left > seconds && in != RunLevels.LEDGE && solid(in) && Math.abs(shot.across) >= Math.abs(shot.down)) {
                shot.x -= shot.across * seconds * 1.5f;
                shot.across = -shot.across;
                shot.bounces--;
                shot.last = null;
                bounced++;
                continue;
            }
            if ((shot.left -= seconds) <= 0 || shot.x < 0 || shot.x > columns || shot.y > bottomAt((int) Math.floor(shot.x))
                    || (in == RunLevels.LEDGE ? shot.down > Math.abs(shot.across) : solid(in))) {
                // A book of hers bursts where it lands, on the ground, a ledge or a wall, or where it falls out of the air.
                if (shot.kind == BOOK && shot.hers && !shot.pets) blast(shot.x, shot.y);
                else ended(shot);
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
                    if (foe.flat || absent(foe) || foe == shot.last || shot.x < foe.x - .2f - spare(foe, true) || shot.x > foe.x + foe.wide() + .2f + spare(foe, true)
                            || shot.y < foe.y - .2f - spare(foe, false) - shot.high || shot.y > foe.y + foe.tall() + .2f + shot.high) continue;
                    // Nothing thrown gets through Zero: anything times nothing is nothing.
                    if (shot.pets) {
                        // What a pet threw: as a pet's blow; and what a blot does not deal with stands dazed.
                        boolean came = petted(foe);
                        if (shot.kind == BLOT_SHOT && !foe.flat && !RunLevels.boss(foe.kind)) foe.dazed = Math.max(foe.dazed, 1.5f);
                        if (came || !RunLevels.boss(foe.kind)) shots.remove(index);
                    } else if (foe.kind == RunLevels.ZERO) {
                        shots.remove(index);
                    } else if (foe.kind == RunLevels.TSUBAKIEH && !open(foe) && foe.dazed <= 0) {
                        // Tsubakieh bats it straight back, unless she is spent.
                        shot.hers = false;
                        shot.across = -shot.across;
                        shot.left = 1.2f;
                        turned++;
                    } else if (RunUniques.is(foe.kind) && RunUniques.way(foe.kind) == RunUniques.REFLECTOR && foe.mode != 3 && !foe.flat && (shot.across > 0) == foe.leftward
                            && !RunLevels.boss(foe.kind)) {
                        // The cross-reference crab's mirror shell: what comes at its front goes back at her. From behind, it tells.
                        shot.hers = false;
                        shot.across = -shot.across;
                        shot.down = -shot.down * .5f;
                        shot.left = 1.2f;
                        mirrored++;
                        turned++;
                    } else if (foe.kind == RunLevels.SLIME && (shot.kind == FIRE || shot.kind == STEAM) && foe.size < 1.2f) {
                        // A slime drinks fire, and steam: it is not beaten by it but grows on it, half as big again, and takes two blows after.
                        shots.remove(index);
                        resize(foe, 1.5f);
                        foe.left = 2 * BLOW;
                        foe.most = Math.max(foe.most, foe.left);
                        drunk++;
                    } else if (foe.dazed <= 0) {
                        current = shot;
                        beat(foe, shot.by);
                        current = null;
                        if (shot.hops > 0) hop(shot, foe);
                        if (shot.kind == THROWN_KEY && !foe.flat) foe.unlocked = RunArms.UNLOCKS;
                        // One sign after another dealt with by what she throws: each is worth one plus more than the last, up to four.
                        if (foe.flat && !RunLevels.boss(foe.kind)) {
                            if (foe.kind == RunLevels.TARGET) break;
                            streak = sinceShot < 1.6f ? streak + 1 : 1;
                            sinceShot = 0;
                            if (streak > 1) bonus += Math.min(streak - 1, 4);
                        }
                        // What the smith has made of an ink gun: its blot splashes, and jumps on.
                        if (shot.kind == GUN) blotted(shot, foe);
                        if (shot.element >= 0) elemental(shot, foe);
                        // A book, fire and steam go on through, and so does what the mirror has sent back, but not through those who wait at the end.
                        // And so does what the smith has made go through: a wave or a key through all it meets, a pen through so many, none of them twice.
                        boolean on = (shot.kind == BOOK || shot.kind == FIRE || shot.kind == STEAM || shot.through || shot.pierce > 0) && !RunLevels.boss(foe.kind);
                        if (on) {
                            if (shot.kind == SLASH || shot.pierce > 0) shot.last = foe;
                            if (shot.pierce > 0 && shot.pierce < 99) shot.pierce--;
                        } else {
                            ended(shot);
                            shots.remove(index);
                        }
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
        if (!fell && gearTakes()) {
            safe = SAFE * perks.mercy;
            return true;
        }
        // The further on the land, the more a hurt costs; and what she wears takes some of it off again, never the last half.
        if (!fell) costs = Math.max(1, costs + bite - perks.defence);
        // Three colours and more are a prism, through which whatever lands on her lands by half.
        if (!fell && named(PRISM)) costs = (costs + 1) / 2;
        if (shield) {
            if (--inkLeft <= 0) shield = false;
            spared++;
            safe = SAFE * perks.mercy;
            // From a fall she still has to be put back, but she keeps her heart.
            if (!fell) return true;
        } else {
            hurts++;
            if (room >= 0 && wave > 0) arenaHurts++;
            // A letter that has been through a hurt is not one to hand over.
            if (quest == ASKED && level.quest == RunLevels.LETTER) fail();
            int lost = costs * HALF;
            if (!fell) lost = Math.max(Math.max(1, lost / 10), lost - perks.skills.stats[RunSkills.DEF] - lost * defencePercent() / 100);
            hp -= lost;
            lastLost = lost;
        }
        safe = SAFE * perks.mercy;
        // Once a land, the second wind: what would have been the end of her leaves her half a heart (or, from the tree, its own HP).
        if (hp <= 0 && !fell && (perks.secondWind || skillOf(RunSkills.E_SECOND_WIND) > 0) && !windUsed) {
            hp = Math.max(HALF, Math.min(maxHp(), skillOf(RunSkills.E_SECOND_WIND)));
            windUsed = true;
            secondWinds++;
            safe = SAFE * perks.mercy * 2;
        }
        if (hp <= 0) {
            hp = 0;
            if (--lives <= 0) {
                lives = 0;
                state = LOST;
                return true;
            }
            livesLost++;
            hp = maxHp();
        ward = wardMost();
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
                if (wall(at(column, row))) {
                    x = column - WIDE - .001f;
                    speedAcross = 0;
                    break;
                }
            }
        } else if (by < 0) {
            int column = (int) Math.floor(x);
            for (int row = top; row <= bottom; row++) {
                if (wall(at(column, row))) {
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
    /** How deep a fall goes at a column before it is the end: the bottom of the land, or under the land's lower layer where it has one there. */
    private int bottomAt(int column) {
        if (room >= 0) return level.land.length;
        return level.under != null && column >= 0 && column < level.under.length && level.under[column] ? level.land.length : RunLevels.ROWS;
    }

    private boolean lands(int column, int row) {
        int rows = bottomAt(column);
        for (int below = row + 1; below < rows; below++) {
            char here = at(column, below);
            if (here == RunLevels.AIR) continue;
            return here == RunLevels.THIN || here == RunLevels.LEDGE || here == RunLevels.BLOCK || here == RunLevels.CRUMBLE || here == RunLevels.BELT_LEFT
                    || here == RunLevels.BELT_RIGHT || here == RunLevels.SLICK || here == RunLevels.VINE;
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
                // A ledge, of either kind, holds her only when she comes down onto it from above; and so does water, in blue.
                if (wall(here) || (thin || here == RunLevels.WATER && firm(here)) && y + TALL - by <= row + .02f) {
                    y = row - TALL;
                    speedDown = 0;
                    onGround = true;
                    if (plunged && perks.pound) poundNow = true;
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
                if (wall(at(column, row))) {
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
    /** How far from where it was put one of a swarm goes after her: it keeps to its place, and does not follow her along the land. */
    static final float LEASH = 6;

    /**
     * Whether a creature of a kind goes on the ground, and so must have something under it: not one that flies, floats, hangs, swims or
     * haunts, and not one who waits (who keeps to the place made for them), nor a paper target.
     */
    static boolean walks(int kind) {
        if (RunLevels.flier(kind) || RunLevels.boss(kind) || RunLevels.rover(kind) || kind == RunLevels.DRAGON || kind == RunLevels.TARGET) return false;
        if (!RunUniques.is(kind)) return true;
        int way = RunUniques.way(kind);
        return !RunUniques.flies(kind) && !RunUniques.floats(kind) && way != RunUniques.GHOST && way != RunUniques.SWIMMER && !RunUniques.great(kind);
    }

    private void move(Foe foe, float seconds) {
        moveFoe(foe, seconds);
        if (foe.swarm && !foe.flat && Math.abs(foe.x - foe.home) > LEASH) {
            float was = foe.x;
            foe.x = foe.home + Math.signum(foe.x - foe.home) * LEASH;
            // (Kept to its place only where there is ground to keep to.)
            if (walks(foe.kind) && !held(foe)) foe.x = was;
            foe.leftward = foe.x > foe.home;
        }
        if (!foe.flat && foe.leaping == 0 && walks(foe.kind)) settle(foe, seconds);
    }

    /** How fast one that walks falls, and the fastest it falls; and counts that only go up: those that fell, and those that fell into water, lava or out of the land. */
    static final float FOE_FALLS = 34, FOE_FALLS_MOST = 20;
    private int foesFell, foesSank;

    int foesFell() { return foesFell; }

    int foesSank() { return foesSank; }

    /** Whether there is something under one that walks, where it is. */
    boolean held(Foe foe) { return heldAt(foe, foe.x); }

    private boolean heldAt(Foe foe, float at) { return heldOn(foe, at, (int) Math.floor(foe.ground + foe.tall() + .05f)); }

    private boolean heldOn(Foe foe, float at, int row) {
        for (int column = (int) Math.floor(at + .12f); column <= (int) Math.floor(at + foe.wide() - .12f); column++) if (standable(at(column, row))) return true;
        return false;
    }

    /**
     * One that walks with nothing under it (knocked off an edge, its ledge crumbled, or laid where there is none) falls, as she does,
     * until it lands on something; into water or lava, or out of the bottom of the land, it is gone, and gives nothing.
     */
    private void settle(Foe foe, float seconds) {
        if (held(foe)) {
            foe.dropping = 0;
            return;
        }
        if (foe.dropping == 0) foesFell++;
        float feet = foe.ground + foe.tall();
        foe.dropping = Math.min(FOE_FALLS_MOST, foe.dropping + FOE_FALLS * seconds);
        float to = feet + Math.max(.02f, foe.dropping * seconds);
        int middle = (int) Math.floor(foe.x + foe.wide() / 2);
        for (int row = (int) Math.floor(feet + .05f); row <= (int) Math.floor(to); row++) {
            // (Coming down onto the top of a square it can stand on; or going into water or lava.)
            if (row > feet + .01f && heldOn(foe, foe.x, row)) {
                to = row;
                foe.dropping = 0;
                break;
            }
            char square = at(middle, row);
            if (to > row && (square == RunLevels.WATER || square == RunLevels.LAVA)) {
                sink(foe);
                return;
            }
        }
        foe.y += to - feet;
        foe.ground = to - foe.tall();
        if (to > level.land.length + 1) sink(foe);
    }

    /** One that walks has fallen where it cannot stand: it is gone, giving nothing. */
    private void sink(Foe foe) {
        foe.flat = true;
        foe.left = 0;
        foe.act = 0;
        foe.dropping = 0;
        // (Nothing flies out of it.)
        foe.dropped = true;
        foesSank++;
    }

    private void moveFoe(Foe foe, float seconds) {
        if (foe.flat) {
            // How long it has been down, for whoever shows it going.
            foe.act += seconds;
            return;
        }
        if (foe.away && !RunLevels.rover(foe.kind) || foe.kind == RunLevels.TARGET) return;
        if (foe.crowd && (room >= 0 || Math.abs(foe.x - x) > WAKES)) return;
        // (One of them in the land keeps still while she is in a place off it; a hidden boss in its hideout, while she is not.)
        if (RunUniques.is(foe.kind) && ((room >= 0) != (foe.x >= level.main) || Math.abs(foe.x - x) > WAKES)) return;
        if (!puddles.isEmpty() && mired(foe)) seconds *= PUDDLE_SLOWS;
        if (foe.stunned > 0) {
            // (Its flinch from a blow passes as fast as ever: knocked down, it can still be hit.)
            foe.stunned -= seconds;
            if (foe.dazed > 0) foe.dazed = Math.max(0, foe.dazed - seconds * (1 - STUN_PACE));
            seconds *= STUN_PACE;
        }
        foe.clock += seconds;
        foe.guardMet += seconds;
        if (foe.broken > 0 && !absent(foe)) {
            if ((foe.broken -= seconds) <= 0 || foe.taken >= 2 && foe.dazed <= seconds) {
                foe.forming = forms(foe);
                foe.shieldMost = strength(foe.kind) + stage(foe);
                foe.shield = 0;
                foe.broken = 0;
                foe.taken = 0;
            }
        } else if (foe.forming > 0) {
            foe.forming -= seconds;
            foe.shield = foe.forming <= 0 ? foe.shieldMost : foe.shieldMost * (1 - foe.forming / forms(foe));
        }
        if (foe.dazed > 0) {
            foe.dazed -= seconds;
            return;
        }
        // Her shield broken, she reels: she does nothing until it is forming again.
        if (foe.broken > 0) return;
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
        if (RunUniques.is(foe.kind)) {
            unique(foe, seconds, middle, away, toLeft);
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
        if (foe.guard > 0 && away < 5 && Math.abs(y + TALL - foe.y - foe.tall()) < 1.5f) foe.leftward = toLeft;
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
    private static final int[][][] PATTERNS = new int[40][][];
    /** How long each of them is spent after something they have done. */
    private static final float[] WINDOW = new float[40];
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
        // Lucy: three things as a child, and two more with her dragon side out. (Her last third is not fought on the ground.)
        PATTERNS[RunLevels.LUCY] = new int[][]{{1, 3, 2}, {4, 1, 5, 2}, {4, 5, 1, 2}};
        WINDOW[RunLevels.LUCY] = 1.35f;
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
        float quick = 1 - .14f * phase, px = x + WIDE / 2;
        // She keeps to the open: never nearer the ends of her place than this.
        float lo = level.arena >= 0 ? level.arena + 2 : foe.home - 12, hi = level.arena >= 0 ? level.gate - 1.5f - foe.wide() : foe.home + 2.5f;
        if (foe.spawned && room >= 0) {
            lo = level.rooms.get(room)[3] + 1;
            hi = level.rooms.get(room)[4] - foe.wide();
        }
        // Stood above her, on a ledge: she comes for her there (between one thing she does and the next).
        if (reach(foe, seconds, lo, hi, foe.step != 1)) return;
        float floor = foe.ground + foe.tall();
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
        // Lucy is quick about it: she shows what is coming for a little less long than anyone.
        if (foe.kind == RunLevels.LUCY) return what == 392 ? .45f : .6f;
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
            case 392:
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
                    if (weapon != UNARMED) laidDown = weapon;
                    weapon = UNARMED;
                    disarmed = 3;
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
            case 395:
                foe.fromX = px;
                foe.toX = Math.min(foe.ground, y + TALL - foe.tall());
                return 1.1f;
            // Lucy, as a child: three squirts of her water gun in a fan; her swim ring, thrown, which comes back; and three water
            // balloons, lobbed. And with her dragon side out: five squirts; mist that falls the length of the place, with room
            // between; and a swoop at her visitor (395).
            case 391:
                spread(DOT, middle, foe.y + foe.tall() * .45f, phase >= 1 ? 5 : 3, .26f, 6.5f, 2.2f);
                return .4f;
            case 392: {
                Shot ring = shoot(BOOMERANG, false, middle, floor - .55f, way * 10, 0, 2);
                ring.spin = way * 10;
                thrown++;
                return .2f;
            }
            case 393:
                for (int which = -1; which <= 1; which++) lob(GLOB, middle, foe.y + .3f, px + which * 2.5f);
                thrown++;
                return .5f;
            case 394:
                for (float column = lo - 1 + foe.turn % 2 * 1.5f; column < hi + foe.wide() + 1.5f; column += 3) {
                    shoot(DOT, false, column, Math.max(.5f, floor - 8.5f), 0, 6, 1.6f);
                }
                thrown++;
                return .6f;
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
            case RunLevels.LUCY: {
                // Lucy: on her feet as a child, keeping her distance while she makes ready; on the wing once her dragon side is out,
                // and down only when she is spent, or at the end of a swoop.
                boolean swoop = what == 395 && doing;
                if (!swoop) foe.leftward = toLeft;
                boolean low = phase == 0 || foe.step == 2;
                if (!swoop) foe.y += Math.max(-6 * seconds, Math.min(6 * seconds, (low ? foe.ground : foe.ground - 2.6f) - foe.y));
                if (foe.step == 0) {
                    float keeps = px + (toLeft ? 4.5f : -4.5f) - foe.wide() / 2;
                    foe.x += Math.max(-2.6f * seconds, Math.min(2.6f * seconds, keeps - foe.x));
                }
                if (swoop) {
                    float across = foe.fromX - middle, down = foe.toX - foe.y, length = Math.max(.3f, (float) Math.hypot(across, down));
                    foe.x += across / length * 8.5f * seconds;
                    foe.y = Math.min(foe.ground, foe.y + down / length * 8.5f * seconds);
                    if (length < .4f || foe.y >= foe.ground) foe.act = 0;
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
        if (what == 72 || what == 32 || what == 181 || what == 173 || what == 192 || what == 395) foe.y = foe.ground;
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
