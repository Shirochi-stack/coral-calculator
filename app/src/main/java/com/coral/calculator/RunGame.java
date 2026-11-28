package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Plus run as it is played: over the whole of the screen, with its own lands, pictures, story
 * and sounds. It has a map of the lands of its two volumes, a counter where the pluses she
 * has collected buy what she keeps, the run itself, in which the story is told by whoever is
 * speaking without the land leaving the screen, and a card at the end of each land. {@link PlayStage} hands the screen over to it.
 */
final class RunGame {
    private static final int STORY = 0, MAP = 1, PLAY = 2, PAUSED = 3, RESULT = 4, SHOP = 5, STALL = 6, DELVE = 7;
    private static final int RESUME = 0, AGAIN = 1, TO_MAP = 2, NEXT = 3, COUNTER = 4, DELVE_GO = 10, BLESS = 11;
    /** How she is drawn: standing, running, or one of the six frames of her jump. */
    private static final int STAND = -1, RUN = -2, PUSH_OFF = 1, RISE = 2, TOP = 3, FALL = 4, LAND = 5;
    /**
     * The pictures of the art, side by side, each standing on the bottom of a square of its
     * own: two for each chapter's ground, then the rest. How much of its square each fills
     * is measured when the game opens.
     */
    private static final int MINUS_ART = 10, MINUS_FLAT_ART = 12, PLUS_ART = 13, LEDGE_ART = 14, FLAG_ART = 15, GATE_ART = 16,
            SPIKES_ART = 17, SPRING_ART = 18, STAR_ART = 19, HEART_ART = 20, TIMES_ART = 21, DIVIDE_ART = 23, ZERO_ART = 25,
            WEAPON_ART = 28, BUFF_ART = 34, ART_COUNT = 40;
    /** How long each of the things she carries is drawn, in squares. */
    private static final float[] WEAPON_LONG = {.78f, .64f, .52f, .98f, 1.08f, .56f, .6f, .7f, 1, 1.1f, .7f, .8f};
    private static final int[] SKIES = {R.drawable.plus_run_sky1, R.drawable.plus_run_sky2, R.drawable.plus_run_sky3,
            R.drawable.plus_run_sky4, R.drawable.plus_run_sky5, R.drawable.plus_run_sky6, R.drawable.plus_run_sky7, R.drawable.plus_run_sky8,
            R.drawable.plus_run_sky9, R.drawable.plus_run_sky10, R.drawable.plus_run_sky11, R.drawable.plus_run_sky12};
    /** The ground of the two chapters of the third volume: a cap and a fill for each, four squares in a row. */
    private Bitmap ground3;
    private int seenPledges;
    /**
     * The pictures of the second sheet, eight to a row: two for the ground of each chapter
     * of the second volume, lava and water, what the ground there is made of, what flies,
     * what her friends hold out, the ribbons, and the signs, each in four pictures of which
     * the last two are its going. And of the third, four to a row: what stands in a village,
     * and what belongs to the places where someone waits.
     */
    private static final int LAVA_ART = 10, WATER_ART = 12, THIN_ART = 14, CRUMBLE_ART = 15, BELT_ART = 16, SLICK_ART = 17, MOVER_ART = 18,
            DOOR_ART = 19, FIRE_ART = 20, DROP_ART = 21, FEATHER_ART = 22, STOP_ART = 23, SAYING_ART = 24, GIFT_ART = 25, RIBBON_ART = 27,
            SIGN_ART = 35;
    private static final int STALL_ART = 5, WALL_ART = 8, SHOCK_ART = 9, BAT_ART = 10, RUBBLE_ART = 11, BLADE_ART = 12, DARK_ART = 13, BAR_ART = 15;
    /** How tall each thing that stands in a village is drawn, in squares. */
    private static final float[] DECOR_TALL = {3.4f, 3.6f, 4, 3, 2, 2.6f, 2.4f, 3.2f};
    /** Those who wait in the second volume, as they are drawn: Gokhae, Miri, Gari, Yeobaek, King Slime and the Mawang. */
    private static final int[] LATER_CAST = {R.drawable.run_gokhae, R.drawable.run_miri, R.drawable.run_gari, R.drawable.run_yeobaek,
            R.drawable.run_kingslime, R.drawable.run_mawang, R.drawable.run_guard, R.drawable.run_butler, R.drawable.run_byeoru, R.drawable.run_jupan,
            R.drawable.run_sumbi, R.drawable.run_pads, R.drawable.run_party, R.drawable.run_oldbook, R.drawable.run_oldbook, R.drawable.run_wyrm,
            R.drawable.run_blot, R.drawable.run_gecko};
    private static final int[] LATER_FRAMES = {4, 3, 3, 6, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
    /** Those that fly, as they are drawn: four pictures of each, wings up, wings down, about to do what it does, and beaten. And each one's colour. */
    private static final int[] FLIER_CAST = {R.drawable.run_fly_drifter, R.drawable.run_fly_swooper, R.drawable.run_fly_bomber, R.drawable.run_fly_shooter,
            R.drawable.run_fly_mite, R.drawable.run_fly_wisp, R.drawable.run_fly_rime};
    private static final int[] FLIER_COLOURS = {0xFF3A3340, 0xFF7FA8E0, 0xFF8E86A6, 0xFFE0A21A, 0xFF3A3340, 0xFFF08A2C, 0xFF7FC4EC};
    private final Bitmap[] flierCast = new Bitmap[FLIER_CAST.length];
    /** The pets, as they are drawn: six pictures of each; and what each is called. */
    private static final int[] PET_CAST = {R.drawable.run_pet_jar, R.drawable.run_pet_cat, R.drawable.run_pet_dog, R.drawable.run_pet_duck, R.drawable.run_pet_monkey,
            R.drawable.run_pet_chikuwa, R.drawable.run_pet_rune, R.drawable.run_pet_dark, R.drawable.run_pet_light, R.drawable.run_pet_mini,
            R.drawable.run_pet_tsubakieh, R.drawable.run_pet_illya, R.drawable.run_pet_mawang, R.drawable.run_pet_lucy};
    private static final String[] PET_NAMES = {"The ink jar", "The kitten", "The puppy", "The duckling", "The monkey",
            "Mini Chikuwa", "Mini Rune", "Mini Halgakos, black wings", "Mini Halgakos, white wings", "Mini Halgakos",
            "Mini Tsubakieh", "Mini Illya", "Mini Mawang", "Mini Lucy"};
    private static final String[] PET_DOES = {"spits ink at what comes near", "pounces on what is close", "charges through them along the ground",
            "drops on what flies, and quacks", "throws nuts, and fetches", "throws bread rolls, and has one for her now and then",
            "cries ink that slows them, and flicks it", "swoops at the nearest, again and again", "sends rays of light, and turns one thing aside",
            "does what she does, smaller", "swings her bat at the nearest, and knocks silly what is left standing", "throws a fan of sparkles from her quill",
            "breathes a short dark flame at what is close", "squirts a quick jet of water from a long way off"};
    /** What one of the small stuffed ones says, written in a tiny bubble over it: they do not speak aloud. */
    private String petWord = "";
    private float petWordLeft, sincePetWord;
    private int seenPetGifts, seenPetWards, seenPetCopies;
    private final Bitmap[] petCast = new Bitmap[PET_CAST.length];
    /** Of the pet: where it was a frame ago, how long since it joined her or last dealt with something, and what of it has been seen. */
    private float petWasX, petWasY, petGlad = 9;
    private int seenPetJoined, seenPetStrikes, seenPetKills;
    /** A times beaten: two pictures of it. */
    private Bitmap timesDown;
    /** Where the five of the seven others who speak come among those who do: after the villagers. */
    private static final int FIRST_OTHER = 20;
    /** How many are met in the villages, each with a picture in the row of them; they come after the rest of those who speak. */
    private static final int VILLAGERS = 9, FIRST_VILLAGER = 11;
    /**
     * The colours her hair takes: for each form the hue, how many times the strength of the
     * colour, how many times its brightness, and what is added to the brightness.
     */
    private static final String[] FORM_NAMES = {"Red", "Blue", "Brown", "Green", "Yellow", "Purple", "Black", "White"};
    private static final String[] FORM_GIVES = {"throws fire, and lava is safe", "throws water, and she swims", "nothing close hurts her, and what is thrown costs half",
            "flies while jump is held", "runs much faster", "sends back what is thrown", "", "everything at once"};
    /** The colour of each form, which is the colour her hair is dyed for it: kept where the dye is, so that there is one of each and no more. */
    private static final int[] FORM_COLOURS = java.util.Arrays.copyOfRange(Hair.COLOURS, 1, PlusRun.WHITE + 1);
    /** What a stall sells for the pluses of the land it stands in, and for how many: the last is a ribbon, a different one from land to land. */
    private static final int[] STALL_SELLS = {PlusRun.BREAD, PlusRun.SHIELD, PlusRun.ICE, -1}, STALL_COSTS = {12, 15, 8, 20};
    private static final String[] STALL_NAMES = {"Bread: a heart back", "A ball of ink: takes a hurt", "Ice cream: faster for a while", ""};
    private static final int[] STALL_FORMS = {PlusRun.RED, PlusRun.BLUE, PlusRun.GREEN, PlusRun.YELLOW, PlusRun.PURPLE, PlusRun.BROWN};
    private static final int[] ARMED = {R.drawable.halgakos_arm0, R.drawable.halgakos_arm1, R.drawable.halgakos_arm2,
            R.drawable.halgakos_arm3, R.drawable.halgakos_arm4, R.drawable.halgakos_arm5, R.drawable.halgakos_arm6, R.drawable.halgakos_arm7,
            R.drawable.halgakos_arm8, R.drawable.halgakos_arm9, R.drawable.halgakos_arm10, R.drawable.halgakos_arm11,
            R.drawable.halgakos_arm12, R.drawable.halgakos_arm13, R.drawable.halgakos_arm14, R.drawable.halgakos_arm15, R.drawable.halgakos_arm16,
            R.drawable.halgakos_arm17, R.drawable.halgakos_arm18};
    /**
     * The katana has fourteen pictures of her: after the seven of standing, running, rising
     * and falling come drawing, the cut, sheathing, the dash gathered, the dash's cut, the
     * cut downward, and standing after it.
     */
    private static final int KATANA_FRAMES = 14, KATANA_DRAW = 7, KATANA_CUT = 8, KATANA_SHEATHE = 9, KATANA_GATHER = 10, KATANA_DASH = 11, KATANA_DOWN = 12, KATANA_AFTER = 13;
    /** What the four won in the black arena have just done, for drawing it: how long ago, where, and which way. */
    private int seenCuts, seenDashes, seenAirCuts, seenThrusts, seenJets, seenShells, seenLashes, seenSpins, cutChain;
    private float cutAge = 9, dashAge = 9, airAge = 9, thrustAge = 9, lashAge = 9, spinAge = 9, doneX, doneY;
    private boolean doneLeft;
    private Bitmap arms3;
    /**
     * With her tablet-book or her ring of keys she has twelve frames: the eight, and then
     * letting go, gathering for the heavy thing, doing it, and settling. (In them the
     * eighth is drawing back.)
     */
    private static final int MAIN_FRAMES = 12, MAIN_BACK = 7, MAIN_GO = 8, MAIN_GATHER = 9, MAIN_HEAVY = 10, MAIN_SETTLE = 11;
    /** The pictures of the tablet-book shut and open, of the ring of keys, and of one key. */
    private Bitmap arms2;
    /** The button over the strike that changes what she holds; the one on the map that opens her backpack; and the one on the pause card. */
    private final RectF swapButton = new RectF(), gearButton = new RectF(), packButton = new RectF(), packAct = new RectF(), packCard = new RectF();
    private final RectF[] packRows = new RectF[RunArms.COUNT];
    private boolean packOpen;
    /** Her inventory, drawn and touched by its own class. */
    private final RunInventoryUi inventory = new RunInventoryUi(this);
    /**
     * The board of hunts, open over the guild hall: its card; for each hunt on it the button
     * that takes it on or hands it in, and the one that gives it up, which asks once before
     * it does; what was last paid, and what came with it. And, in a land: how much of what
     * she has dealt with has been counted, which of her hunts something can be done towards
     * there, and how lately each got further.
     */
    private boolean huntsOpen;
    /**
     * The smithy, open over the land, or, from the map, only what she wears: which of the
     * three sisters' work is looked at; the thing to fight with and the piece to wear that
     * are looked at; its card, its tabs, its rows and cells and buttons; what was last made,
     * in a line; and which sister last made something and how long ago, for her hammer.
     */
    private boolean smithOpen;
    /**
     * The black market, open over the land: which of its three things is looked at, and
     * which rung of the arena; its card, its tabs, its rows and buttons; and what was last
     * done there, in a line. And the rung of the black arena she is on, or none; with what
     * winning it has just given, for the card that says so.
     */
    private boolean marketOpen;
    private int marketTab, rungOn = 1, blackRung;
    private String marketSaid = "", rungWords = "", rungPrize = "";
    /**
     * A dive into a dungeon: which one (or -1); the floor she is on; the dive's own number, from which its floors are laid; her HP and
     * lives carried down (or -1); the blessings taken, as bits, and how many of each that adds up; the pluses kept so far; the dungeon whose
     * stairway she stands at; which card is up (the stairway's, or the blessings'); the three offered; and whether its keeper has been seen.
     */
    private int delve = -1, delveFloor, delveHp = -1, delveLives = -1, delveBlessed, delveHearts, delveMight, delveGold, delveWard, delveKept, delveAt = -1, delveCard;
    private long delveDive;
    private int[] offeredNow = new int[3];
    private boolean keeperSaid;
    private final RectF marketCard = new RectF(), marketAct = new RectF();
    private final RectF[] marketTabs = {new RectF(), new RectF(), new RectF()}, hardRows = {new RectF(), new RectF(), new RectF(), new RectF()},
            rungRows = new RectF[RunMarket.RUNGS], wareBuys = new RectF[RunMarket.WARES];
    private int smithTab, smithFrom = PLAY, forgeOn = PlusRun.TABLET, gearSlot, gearTier = 1, hammerer = -1;
    private float hammered = 9;
    private String smithSaid = "";
    private final RectF smithCard = new RectF(), smithAct = new RectF(), packWear = new RectF();
    private final RectF[] smithTabs = {new RectF(), new RectF(), new RectF(), new RectF(), new RectF()}, tradeBuys = {new RectF(), new RectF(), new RectF()},
            tradeSells = {new RectF(), new RectF(), new RectF()}, forgeRows = new RectF[RunArms.COUNT], reforges = new RectF[RunArms.COUNT],
            gearCells = new RectF[RunGear.SLOTS * RunGear.TIERS];
    private static final int[] SMITH_ART = {R.drawable.run_smith_moru, R.drawable.run_smith_pulmu, R.drawable.run_smith_sutdol};
    private final Bitmap[] smiths = new Bitmap[SMITH_ART.length];
    /** The colours of the three sets of what she wears: tinplate, wrought iron and type-steel. And where those who keep the smithy come among those who speak. */
    private static final int[] SET_COLOURS = {0xFFB9C2C9, 0xFF5B6168, 0xFF3F7FC4};
    private static final int FIRST_SMITH = 28;
    private final RectF huntsCard = new RectF();
    private final RectF[] huntActs = {new RectF(), new RectF(), new RectF()}, huntDrops = {new RectF(), new RectF(), new RectF()};
    private int dropAsk = -1, seenFells;
    private String huntPaid = "", huntMore = "";
    private final boolean[] huntHere = new boolean[RunHunts.SLOTS];
    private final float[] huntFlash = new float[RunHunts.SLOTS];
    /** How many treats the pet shop takes for each pet, in place of pluses; and the colours of the badges of the guild's ranks. */
    private static final int[] PET_TREATS = {2, 2, 2, 2, 3, 4, 4, 4, 4, 5, 5, 5, 6, 6};
    private static final int[] BADGE_COLOURS = {0xFFCFC5AE, 0xFFD9B77E, 0xFF8A5A33, 0xFFE0A21A, 0xFF7A4BC8};
    private final RectF petTreat = new RectF();
    private int packFrom, packOn = PlusRun.UNARMED, swapFinger = -1;
    private float swapHeld, sinceHeavy = 9, panelAge = 9, panelX, panelY;
    private boolean panelLeft;
    private int seenPanels, seenFans, seenDuds, seenFinds, seenRefills, seenSwitches;
    /** Her frames with something in her hands: standing, four of running, rising, falling, and using it. */
    private static final int ARMED_FRAMES = 8, ARMED_RUN = 1, ARMED_RISE = 5, ARMED_FALL = 6, ARMED_USE = 7;
    /** The others, as they are drawn in the game: Chikuwa, Rune, Illya and Tsubakieh. */
    private static final int CHIKUWA = 0, RUNE = 1, ILLYA = 2, TSUBAKIEH = 3;
    private static final int[] CAST = {R.drawable.run_chikuwa, R.drawable.run_rune, R.drawable.run_illya, R.drawable.run_tsubakieh};
    private static final int[] CAST_FRAMES = {3, 3, 4, 6};
    /** Their pictures for the cards the story is told on, in the order {@link RunStory} has those who speak. */
    private static final int[] FACES = {0, R.drawable.run_face_chikuwa, R.drawable.run_face_rune, R.drawable.run_face_illya,
            R.drawable.run_face_tsubakieh, R.drawable.run_face_mawang, R.drawable.run_face_gokhae, R.drawable.run_face_miri,
            R.drawable.run_face_gari, R.drawable.run_face_yeobaek, R.drawable.run_face_nuri};
    private static final int[] SPEAKER_COLOURS = {CalculatorLayout.CORAL, 0xFFD2452F, 0xFF3F7FC4, 0xFFE8779A, 0xFF7A5A9A, 0xFF6B2A3A,
            0xFF6A3FA0, 0xFFC9892B, 0xFF4A5560, 0xFF5E8FA6, 0xFF2E9E6E, 0xFF7D7FD0, 0xFF394A8C, 0xFFB5651D, 0xFF6B7A2A, 0xFF5B4BB5, 0xFF2F8F5B,
            0xFFC0392B, 0xFFB8860B, 0xFF1E9C8B, 0xFF5C7FA8, 0xFF4A4A5A, 0xFF3F8F5A, 0xFFE08A2B, 0xFFE0662F, 0xFF7A8088, 0xFFC0392B, 0xFF8A5A33,
            0xFFC8552B, 0xFF7A4B8A, 0xFF2FA89A, 0xFFB9862C};
    private static final int JUMP_FRAMES = 6;
    /** The six frames of her losing: hit, knocked into the air, falling, sitting dazed, sulking, and crying. */
    private static final int LOSE_FRAMES = 6, HIT = 0, TUMBLE = 1, DROP = 2, DAZED = 3, SULK = 4, CRY = 5;
    /** How long she is seen thrown into the air where she was hurt. */
    private static final float KNOCK = 1.1f;
    private static final float SQUASH = .14f, CAPTION = 2.6f;
    /**
     * What her pluses buy at the counter, to keep: its name, what each step of it gives, what
     * each step costs, and the picture beside it (-1 for her own, with her wings out).
     */
    private static final String[] UP_NAMES = {"Chikuwa's lunch box", "Wings: a second jump", "Glossary magnet", "Longer treats", "Bigger ink tank",
            "Flag picnic", "Thicker ink", "Spare bookmarks", "Prism", "Quick ribbons", "A place by her side", "Room for a stray", "Kennel: brisker pets",
            "Kennel: stronger pets", "Triple jump", "Air dash", "Thorns", "Ground pound", "Dodge roll", "Wall jump", "Glide", "Second wind", "Aerial strike"};
    private static final String[][] UP_GIVES = {
            {"A fourth heart in every land", "A fifth heart in every land"},
            {"A second jump in the air: press jump again"},
            {"Pluses close by come to her", "Pluses come to her from further off"},
            {"What she picks up lasts half as long again", "What she picks up lasts twice as long"},
            {"The ink gun holds 24 shots", "The ink gun holds 32 shots"},
            {"Every flag she passes gives a heart back"},
            {"A ball of Rune's ink takes two hurts"},
            {"A fourth life in every land", "A fifth life in every land", "A sixth life in every land"},
            {"Three colours worn at once", "Four colours worn at once", "Five colours worn at once", "Every colour at once: white, not yet mastered"},
            {"A colour is ready a second sooner", "A colour is ready two seconds sooner", "A colour is ready three seconds sooner"},
            {"Two pets of her own come into every land", "Three pets of her own come into every land"},
            {"Two pets found in a land may follow her", "Three pets found in a land may follow her"},
            {"Her pets wait a fifth less between one thing and the next", "Her pets wait two fifths less"},
            {"A pet's blow counts twice on a big one", "And a pet takes a whole piece off a shield"},
            {"A third jump in the air, a flip: press jump again"},
            {"In the air, double-tap a way (or Shift): a dash that way", "And the dash strikes whatever it goes through"},
            {"Whatever hurts her takes a blow of thorns", "Two blows of thorns", "Three blows of thorns"},
            {"Hold down in the air: she slams the ground with what she holds, even out of an air dash", "A wider slam that hits harder, and cracks two pieces off a shield",
                    "The widest slam: shields broken outright, and a shockwave along the ground"},
            {"On the ground, double-tap a way (or Shift): a roll nothing hurts"},
            {"Against a wall in the air she clings: jump to kick off it"},
            {"Hold jump as she falls: she glides down slowly", "She glides slower, and so further", "Slower still, and every second and a half a gust lifts her"},
            {"Once a land, what would end her leaves half a heart"},
            {"Hold strike in the air: she hangs there, wings out, and strikes all round her", "It holds her up longer, reaches wider and hits harder",
                    "Longest and hardest, with quicker strikes, and twice before she lands"}};
    private static final int[][] UP_COSTS = {{120, 320}, {200}, {90, 240}, {80, 220}, {70, 180}, {260}, {160}, {150, 300, 480}, {200, 350, 500, 900},
            {120, 240, 400}, {400, 900}, {250, 600}, {200, 450}, {300, 650}, {350}, {300, 650}, {250, 500, 900}, {280, 520, 900}, {260}, {320}, {300, 560, 950}, {600},
            {350, 650, 1000}};
    /** The moves bought at the counter, by their places in those lists. */
    private static final int TRIPLE = 14, AIR_DASH = 15, THORNS = 16, POUND = 17, ROLL = 18, WALL_JUMP = 19, GLIDE = 20, SECOND_WIND = 21, AERIAL = 22;
    /** For her pets: more of her own beside her, more strays, brisker, stronger. */
    private static final int PET_PLACES = 10, STRAY_ROOM = 11, PET_BRISK = 12, PET_STRONG = 13;
    /** What a pet costs to have for good: the five animals, and the five small stuffed copies of people she knows. */
    private static final int[] PET_COSTS = {300, 300, 350, 350, 400, 600, 600, 700, 700, 800, 950, 900, 1200, 1500};
    /** The counter's tabs, and what stands under each. */
    private static final String[] TAB_NAMES = {"Hearts", "Moves", "Colours", "Comforts", "Pets", "Arms", "Vanity"};
    private static final int[][] TAB_WARES = {{0, 7, 5, 6}, {1, 14, 15, 18, 19, 17, 20, 22, 16, 21}, {8, 9}, {2, 3, 4}, {PET_PLACES, STRAY_ROOM, PET_BRISK, PET_STRONG}, {}, {}};
    private static final int PETS_TAB = 4, ARMS_TAB = 5, COLOURS_TAB = 2, VANITY_TAB = 6;
    private final RectF[] counterTabs = new RectF[TAB_NAMES.length], petCells = new RectF[PlusRun.PETS];
    private final RectF petAct = new RectF(), armsAct = new RectF();
    private int counterTab, shownWares, petOn;
    /** How many colours must be hers before each step of the prism is for sale: the last, all seven. */
    private static final int[] PRISM_NEEDS = {3, 4, 5, 7};
    private static final int PRISM = 8, QUICK = 9;
    /** The mixes that have names, as {@link PlusRun} numbers them: what each is called, what it is made of, and what it does. */
    private static final String[] MIX_NAMES = {"Steam", "Comet", "Tide", "Mirror", "Gale", "Ember", "Prism", "White, unmastered"};
    private static final String[] MIX_OF = {"Red + Blue", "Red + Yellow", "Blue + Green", "Brown + Purple", "Green + Yellow", "Red + Brown", "Any three", "All six"};
    private static final String[] MIX_DOES = {"a cloud that goes through them, and a jump more in the air", "at a run she goes through whatever she meets",
            "she comes down slowly, and water goes both ways", "what is sent back goes on through a row", "she rises much faster",
            "what she knocks over flies off burning", "whatever lands on her lands by half", "every colour's power for a while: a glow, not yet a light"};
    /** The order they stand in at the counter: lives under hearts. And the picture that stands for her own face. */
    private static final int[] UP_ORDER = {0, 7, 8, 9, 1, 14, 15, 18, 19, 17, 20, 22, 16, 21, 2, 3, 4, 5, 6, 10, 11, 12, 13};
    private static final int HER_FACE = -2;
    /** A ware whose picture is her in one of her moves: below nothing, so that it is never taken for one of the other pictures. */
    private static final int MOVE_ART = -100;
    private static final int[] UP_ART = {BUFF_ART + PlusRun.BREAD, -1, BUFF_ART + PlusRun.MAGNET, BUFF_ART + PlusRun.ICE, WEAPON_ART + PlusRun.GUN,
            FLAG_ART, BUFF_ART + PlusRun.SHIELD, HER_FACE, STAR_ART, STAR_ART, STAR_ART, STAR_ART, STAR_ART, STAR_ART,
            MOVE_ART + PlusRun.MOVE_FLIP + 1, MOVE_ART + PlusRun.MOVE_DASH, STAR_ART, MOVE_ART + PlusRun.MOVE_POUND, MOVE_ART + PlusRun.MOVE_ROLL, MOVE_ART + PlusRun.MOVE_WALL,
            MOVE_ART + PlusRun.MOVE_GLIDE, HER_FACE, MOVE_ART + PlusRun.MOVE_GLIDE};
    /** How long the ice cream, the magnet and the notes last before anything is bought, as {@link PlusRun} has them. */
    private static final float[] BUFF_LASTS = {0, 8, 0, 10, 6};

    private final PlayStage stage;
    private final Context context;
    final boolean dark, haptics;
    final PlaySession session;
    private final CompanionPrefs names;
    final RunAudio audio;
    private final Random random = new Random();
    final float density;
    private final Bitmap pet, jump, lose, art, portrait;
    private final Bitmap[] armed = new Bitmap[ARMED.length], cast = new Bitmap[CAST.length], faces = new Bitmap[FACES.length];
    private final Bitmap[] laterCast = new Bitmap[LATER_CAST.length], dyedArmed = new Bitmap[ARMED.length];
    private Bitmap villagers, petRows;
    /**
     * The pictures of her in each colour she has been drawn in, by the colour: the three
     * rows of her own sheet that a land uses, her jump, the sheet she loses on, and her
     * carrying each thing. A set is made once, away from the screen, and kept; no more than
     * eight are kept. And the colours whose sets are being made.
     */
    private final android.util.SparseArray<Bitmap[]> looks = new android.util.SparseArray<>();
    private final java.util.Set<Integer> dyeing = new java.util.HashSet<>();
    /**
     * How she looks: the colour she is drawn in, the one she was drawn in before it, and how
     * long ago the one became the other, for the moment in which the one fades into the
     * other. And, while a picture of her is drawn, the colour to draw it in, if it is not
     * to be the one she is in.
     */
    private int look, lookFrom, drawnAs = NO_LOOK;
    private static final int NO_LOOK = Integer.MIN_VALUE;
    /**
     * The ribbon and the mixing of colours: which finger is on the ribbon, or -1, and for
     * how long; whether the colours she owns are fanned out to be picked from; which of
     * them are picked to be put on and to be let go of, as bits; which the finger was last
     * over; where each of them is on the screen; how long since the arrow stepped to
     * another colour; and the mix that last formed, and how long ago.
     */
    private int ribbonFinger = -1, fanOver = -1, seenMixings, seenFaded, seenWhitened, mixShown = -1;
    private float ribbonHeld, sinceStep = 9, mixAge = 9;
    private boolean fanOpen, hintMix;
    /**
     * Her mix: the colours a tap on the ribbon calls on, all at once, as bits; it is chosen
     * in the picker and kept from one land to the next and from one day to the next. In
     * the picker: the colours ticked; whether it stays open with no finger on it, which it
     * does when the palette opened it and does not when a hold on the ribbon did; whether
     * one colour at a time is being chosen; and where its card, its quick choices, and its
     * button are on the screen.
     */
    private int herMix, ticks;
    private boolean pickerStays, oneAtATime;
    private final RectF pickerCard = new RectF(), wearButton = new RectF();
    private final List<RectF> chips = new ArrayList<>();
    private final List<Integer> chipMixes = new ArrayList<>();
    private static final int ONE_AT_A_TIME = -1, ALL_I_CAN = -2, TRUE_WHITE = -3;
    private final RectF[] fanCells = new RectF[PlusRun.WHITE];
    private float lookAge = 9;
    private static final float LOOK = .55f;
    /** The colour she wore when she was last hurt, for the pictures of her thrown and fallen; and how long ago a hurt made her flinch. */
    private int knockForm;
    private float flinchAge = 9;
    private int seenLives, seenCasts, seenBraces;
    /** For each of those who wait: how they stood toward a blow a moment ago, and how long since what kept blows off was struck, and since it broke. */
    private int guardWas = -1;
    private float sinceStruck = 9, sinceBroke = 9;

    /**
     * A sheet of pictures in squares, each standing on the bottom of its square, in the
     * middle. It is not read until something on it is wanted, and how much of its square
     * each picture fills is measured then.
     */
    private final class Sheet {
        final int resource, across;
        Bitmap bitmap;
        float[] wide, tall, low;

        Sheet(int resource, int across) {
            this.resource = resource;
            this.across = across;
        }

        Bitmap get() {
            if (bitmap != null) return bitmap;
            bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
            int side = bitmap.getWidth() / across, count = across * (bitmap.getHeight() / side);
            wide = new float[count];
            tall = new float[count];
            low = new float[count];
            int[] pixels = new int[side * side];
            for (int which = 0; which < count; which++) {
                bitmap.getPixels(pixels, 0, side, which % across * side, which / across * side, side, side);
                int left = side, right = -1, top = side, bottom = -1;
                for (int y = 0; y < side; y++) {
                    for (int x = 0; x < side; x++) {
                        if (pixels[y * side + x] >>> 24 > 40) {
                            left = Math.min(left, x);
                            right = Math.max(right, x);
                            top = Math.min(top, y);
                            bottom = Math.max(bottom, y);
                        }
                    }
                }
                wide[which] = right < left ? 1 : Math.min(1, 2 * Math.max(side / 2f - left, right + 1 - side / 2f) / side);
                tall[which] = right < left ? 1 : (side - top) / (float) side;
                low[which] = right < left ? 0 : (side - 1 - bottom) / (float) side;
            }
            return bitmap;
        }
    }

    private final Sheet two = new Sheet(R.drawable.plus_run_art2, 8), three = new Sheet(R.drawable.plus_run_art3, 4);
    /** And a fourth: a door, a crack in a wall, a cellar door, rice cakes, a letter, a stopwatch, a chest, and a scroll. */
    private final Sheet four = new Sheet(R.drawable.plus_run_art4, 4);
    private static final int DOOR_PICTURE = 0, CRACK_PICTURE = 1, CELLAR_PICTURE = 2, CHEST_PICTURE = 6;
    /** How many pieces of a heart make a heart, and how many hearts they can make. */
    private static final int PIECES = 4, PIECE_HEARTS = 2;
    private Bitmap inside;
    /** The inside of a stash, a passage or a room for a game, by its look; what stands about in them; and what each look is called. */
    private static final int[] LOOK_ART = {R.drawable.plus_run_room_wine, R.drawable.plus_run_room_maps, R.drawable.plus_run_room_vault, R.drawable.plus_run_room_clock,
            R.drawable.plus_run_room_green, R.drawable.plus_run_room_print, R.drawable.plus_run_room_crystal, R.drawable.plus_run_room_treasure,
            R.drawable.plus_run_room_pets, R.drawable.plus_run_room_guild, R.drawable.plus_run_room_home, R.drawable.plus_run_room_smithy, R.drawable.plus_run_room_market,
            R.drawable.plus_run_room_gacha, R.drawable.run_home1, R.drawable.run_home2, R.drawable.run_home3, R.drawable.run_home4, R.drawable.run_home5,
            R.drawable.run_home6};
    private static final String[] LOOK_NAMES = {"A wine cellar", "A map room", "A flooded vault", "Inside a clock tower", "A greenhouse", "A printing room", "A crystal cave",
            "A treasure vault", "The pet shop", "The guild hall", "A home", "The smithy", "The black market", "The ribbon gacha", "A cosy kitchen",
            "A reading nook", "An attic bedroom", "A tinkerer's workshop", "A greenhouse home", "A music room"};
    /**
     * The ribbon gacha: its building, and its machine as it stands and as it is when it has
     * blown up; and a roll as it is shown: a capsule comes out and opens. How long ago it
     * was begun, the chance and the price of it, whether it is still to be opened, and what
     * came out of it.
     */
    private Bitmap gachaHouse, gachaMachine;
    /**
     * The end of the last land. Lucy as she is drawn: as a child, with her dragon side out, and as a dragon seen from the front; and
     * Halgakos in the white that is truly hers, from the side and from behind. What has become of the land: nothing yet; the flight,
     * which is flown; the flight won, for the beat before its card; or the land unmade, which is only watched. And what the flight
     * has been seen to do, so that each thing is heard and shown once.
     */
    private static final int NO_FINALE = 0, FLIGHT = 1, FLOWN = 2, UNMADE = 3;
    /**
     * And what comes after the flight, which nothing announces: the Mother Tongue rising; the ride, Halgakos on Lucy's back; and the
     * beat after it is won. What the Mother's blocks say, and the names she has for someone.
     */
    private static final int ARISING = 4, RIDE = 5, RIDDEN = 6;
    /** And while a clip is being watched: nothing of the land goes on under it. The clip, while there is one. */
    private static final int WATCHING = 7;
    private RunVideo video;
    /**
     * The gallery, while it is open; its button on the map and the face on it; everything that is in it; whether something in it
     * has opened that she has yet to say so of, and whether that has been looked for since the game was opened; and the line
     * she said of it last, which she is shown saying on the map for a while.
     */
    private RunShelf shelf;
    private final RectF cringeButton = new RectF();
    private Bitmap cringe;
    private List<RunGallery.Item> gallery;
    private boolean galleryNews, galleryChecked, petNews;
    private String[] galleryWords;
    private float galleryShown;
    private static final String[] BLOCK_WORDS = {"Mr.", "Miss", "Sir", "Madam"}, NAME_WORDS = {"Missy", "young lady", "little one", "dear girl", "Lulu"};
    private RunRide ride;
    private Bitmap rideArt, motherArt, pagesSky;
    private boolean goBreath;
    private int seenRideHits, seenRideHurts, seenRideBeams, seenRideStages, seenRideSent, seenRidePuffs, seenRideChips, seenRideOpens;
    private static final int LUCY_FACE = 30, MOTHER_FACE = 31;
    private static final String[] READ = {"Onii-tan", "...wait for me", "Onii-tan!", "...read it to me", "Onii-tan...", "...don't change it"};
    private final Bitmap[] lucyArt = new Bitmap[3], whiteArt = new Bitmap[2];
    private int finale, unmadeSaid;
    private float finaleAge, cheerAge = 9, flightNoteAge, unmadeAt, lucyRoseAt, sinceBeamEnd = 9;
    private RunFlight flight;
    private String flightNote;
    private int seenFlightHits, seenFlightHurts, seenFlightBeams, seenFlightStages, seenFlightThrown;
    private boolean seenRibboned, beamWas;
    private final List<float[]> flightWords = new ArrayList<>();
    private final Paint drained = new Paint();
    private float capsuleAge = 9;
    private int capsuleChance, capsuleCost, capsuleGave, seenPotShots;
    private boolean capsuleWaits;
    /** The buildings of a village from outside: six of them in a row. */
    private Bitmap town;
    /** Its walls, the boards on top of them, and the light that lies over it. */
    private static final int[] LOOK_WALL = {0xFF3A2430, 0xFF2E2A22, 0xFF1F3A36, 0xFF3A2C24, 0xFF1C2440, 0xFF33241C, 0xFF241C3C, 0xFF35302C,
            0xFF3A2A1E, 0xFF36261C, 0xFF3A2C22, 0xFF2E2420, 0xFF2A1C34, 0xFF4A2A36, 0xFF3A2A1E, 0xFF34261E, 0xFF2E2430, 0xFF33281E, 0xFF22362A, 0xFF36222A};
    private static final int[] LOOK_CAP = {0xFF9A6A52, 0xFF8A6A44, 0xFF5FA08A, 0xFFC09552, 0xFF5A78B8, 0xFFA8744A, 0xFF9A7AE0, 0xFFD0AC58,
            0xFFC08A52, 0xFFB8784A, 0xFFC09A6A, 0xFF9A8478, 0xFF8A62B0, 0xFFE08AA0, 0xFFC09A6A, 0xFFB8865A, 0xFFB09AC8, 0xFFB89060, 0xFF8AB878, 0xFFC08898};
    private static final int[] LOOK_LIGHT = {0x16FFB060, 0x0CFFE0A0, 0x2030C8A8, 0x18FFA070, 0x263050B0, 0x10FFC080, 0x24A070FF, 0x16FFD060,
            0x08FFD090, 0x08FFD090, 0x08FFC890, 0x14FF9040, 0x1C9060E0, 0x0CFFC0D0, 0x0AFFC890, 0x0CFFB060, 0x0C9090FF, 0x0CFFB050, 0x0AA0FFA0, 0x0CFFA0C0};
    private Bitmap lookArt, props;
    private int lookOf = -1;
    private int seenKeys, seenLevers, seenPlates, seenShrines;
    private final float[] artWide = new float[ART_COUNT], artTall = new float[ART_COUNT];
    private Bitmap sky;
    private int skyOf = -1;
    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    final RectF target = new RectF(), safe = new RectF(), box = new RectF();
    private final RectF leftButton = new RectF(), rightButton = new RectF(), jumpButton = new RectF(), strikeButton = new RectF();
    private final RectF pauseButton = new RectF(), closeButton = new RectF(), soundButton = new RectF(), shopButton = new RectF(), goButton = new RectF();
    private final RectF[] pills = {new RectF(), new RectF(), new RectF()};
    private final String[] pillLabels = new String[3];
    private final int[] pillActions = new int[3];
    private int pillCount;
    private final RectF[] lands = new RectF[RunLevels.TOTAL];
    /** How many lands of a chapter are in each of its rows on the map: all of them in portrait; fewer in landscape, where chapters stand side by side. */
    private final int[] mapPerRow = new int[3];
    /** Whether the screen is short and wide (a phone on its side): the map's tabs are put up beside the line under its name. */
    private boolean mapShort;
    private final RectF[] tabs = {new RectF(), new RectF(), new RectF()}, tabs3 = {new RectF(), new RectF(), new RectF()}, stallWares = {new RectF(), new RectF(), new RectF(), new RectF()};
    private final RectF stallButton = new RectF();
    /** Which volume's lands the map shows; and from where the counter was opened, to go back there. */
    private int page, counterFrom = MAP;
    private final RectF[] wares = new RectF[UP_NAMES.length];
    private final Path path = new Path();
    final int cardColor, ink, soft;

    private int width, height;
    private float tile;
    private int screen = MAP;
    float clock;

    // The run
    private int number = 1;
    PlusRun run;
    private PlusRun.Perks perks = new PlusRun.Perks();
    private float cameraX, cameraY;
    private boolean goLeft, goRight, goJump, goStrike;
    /** The keys held, and what the fingers on the buttons hold, which together are what she does; and whether keys are what is being used. */
    private final RunKeys keys = new RunKeys();
    private boolean touchLeft, touchRight, touchJump, touchStrike, touchDown, keyed;
    /** The keys have been used in the first land, and she has yet to say which are which. */
    private boolean keysHintDue;
    /** A press of the jump or of the button for what she carries that has not been acted on yet: a tap may be over before the next moment of the game. */
    private boolean jumpPressed, strikePressed;
    private int seenJumps, seenLandings, seenCollected, seenFlattened, seenHurts, seenSprings, seenFlags, seenBlows;
    private int seenStrikes, seenPicked, seenThrown, seenTurned, seenTaken, seenSpared, seenCharms, seenFlaps, seenSwings, seenBonus;
    /** Which of the things to fight with she has already said her word about in this land, and what else she has said once in it. */
    private final boolean[] remarked = new boolean[PlusRun.GUN + 1];
    private boolean saidCombo, saidLast, saidFlag, saidCharm;
    private float squash, sinceUse = 9, sinceLook, sinceAim = 9;
    /** Knuckles: uppercuts and haymakers seen, and how long since the last of each, for her pose. */
    /** Whether her title line was said on this visit to the map (her line with the game's new name waits for another). */
    private boolean titleThisVisit;
    private float lastBlackLine = -99;
    private int seenUppercuts, seenHaymakers;
    private float uppercutAge = 9, haymakerAge = 9;
    private int seenFormed, seenDoors, seenQuakes, seenParried, seenCensors, seenEntrances, seenSpecials, seenPhases, seenUnlocks, seenWiped;
    private final boolean[] saidForm = new boolean[PlusRun.WHITE + 1];
    /** How long her form lasts in all; how much longer the screen shakes; how long since she walked into a shut way, since one shut, and since they opened. */
    /** How long everything is held still after a heavy blow (a frame or two), and how long that is. */
    private float hitStop;
    static final float HIT_STOP = .04f;
    private float formLasts = 1, shake, sinceBump = 9, sinceShut = 9, sinceOpened = 9, sinceParry = 9, flash;
    private int seenGreetings, seenEntered, seenReveals, seenWins, seenCurses, seenDrops, seenBeams, seenBlasts, jumpsAtFall;
    /** In a build made for testing: frames drawn since a moment, to find how many are drawn a second. */
    private long framesFrom;
    private int frames;
    /** Pieces seen knocked off shields, shields seen broken, and times she was seen thrown back by one; and how much longer everything is slow. */
    private int seenCracks, seenShatters, seenThrownBack;
    private float slow;
    /** In a build made for testing: how long since a blow was last dealt for her. */
    private float sinceDeal;
    /** Where a book last burst, in squares, and how long ago. */
    private float blastX, blastY, blastAge = 9;
    /** How long since she last said where a second jump is to be had. */
    private float sinceWings = 999;
    /**
     * What of the story is still to be told in this land, a line after another, in the
     * bubble at the top, while she plays; whether it is being told; whether the line being
     * begun is one of its lines; and how long the bubble that is up was to be up in all.
     */
    private final List<String[]> talk = new ArrayList<>();
    private boolean talking, told;
    private float captionFor = 1;
    /** In the first land: whether she has yet moved, and jumped, which is what the glow on the arrows and on the jump is for. And whether the palette is to glow. */
    private boolean hintMoved, hintJumped, glowPalette;
    /** The button that lets her down through a thin ledge, over the arrows; and whether it is held. */
    private final RectF downButton = new RectF();
    private boolean goDown;
    /** What a card that names something won is: plain, good, or rare. And where what was won flies from, in squares. */
    private int toastRarity;
    /** A toast's picture that is a thing found: this much and its number. */
    private static final int LOOT_PICTURE = 1000000;
    /**
     * The pictures of everything that can be found and worn, in one sheet of squares sixteen to a row: the weapons, twelve kinds in each
     * chapter's look; the twelve rose weapons; the thirty armours; the thirty accessories; and the smiths' twelve pieces.
     */
    private Bitmap lootIcons;
    /** Her skill tree's screen: open over everything, from where; the facts it shows, read again when they change; the button on the map and
     * on the pause; her active slots on the HUD; and how many times divine power has come in this land, as last seen. */
    private RunTree tree;
    /** The shields creatures carry: their pictures; blows that rang off and shields broken, as last seen; and how long ago the last of each was. */
    private Bitmap mobShields, uniqueSheet, paperSheet;
    /** Paper as seen cut in this land, by grade, and trees cut; the run they were seen in. The smithy's Craft tab: the kind looked at, and its buttons. */
    private final int[] seenPaper = new int[RunCraft.GRADES];
    private int seenTrees;
    private PlusRun paperRun;
    private int craftBase;
    private final RectF[] craftKinds = new RectF[RunCraft.KINDS], craftActs = {new RectF(), new RectF(), new RectF(), new RectF()};
    private static final int CRAFT = 3, QUESTS = 4;
    private int seenHeals, seenWinds, seenHidden;
    /** Foreground pieces, drawn in front of her for depth. */
    private Bitmap foreground;
    /** Her swimming: four pictures (two strokes, a dive, an ink bolt). */
    private Bitmap swim;
    private float healAge = 9;
    private int seenClangs, seenBroken;
    private float clangAge = 9, shatterAge = 9;
    private boolean treeOpen;
    private int treeFrom, seenDivines;
    private final RunTree.Facts treeFacts = new RunTree.Facts();
    private final RectF skillButton = new RectF(), skillPause = new RectF();
    private final RectF[] slotButtons = {new RectF(), new RectF(), new RectF()}, slotPlaces = {new RectF(), new RectF(), new RectF()};
    private static final int LOOT_ICON = 128, LOOT_COLUMNS = 16, ROSE_ICONS = 144, ARMOUR_ICONS = 156, ACCESSORY_ICONS = 186, SMITH_ICONS = 216, ADDITION_ICON = 228;
    /** Her bag, open over the screen: from where; which tab, which page; the thing looked at (its place in the bag) or a worn slot; and its parts. */
    private boolean bagOpen;
    private int bagFrom, bagTab, bagPage, bagOn = -1, bagSlot = -1, seenWardHurts;
    private float wardFlash = 9;
    private final RectF bagCard = new RectF(), bagAct = new RectF(), bagPrev = new RectF(), bagNext = new RectF(), packBag = new RectF();
    private final RectF[] bagCells = new RectF[24], bagTabs = new RectF[4], bagSlots = new RectF[3];
    private int[] bagShown = new int[0];
    private float toastX, toastY;
    /** How much longer the screen is darkened by black being put on her. */
    private float darkened;
    /**
     * One met in the middle of a land: how often it has been seen to be roused, which it is,
     * how long since it last was, and whether its business in this run is over. And of the
     * rest that is new: colours shut off, slimes grown on fire, flights ended, blows that
     * went through what only water or fire deals with, dives; and which kinds of those that
     * fly she has had a word about.
     */
    private int seenRoused, roverKind = -1, seenShuts, seenDrunk, seenGrounded, seenFizzled, seenDives, flySeen;
    private float roverAge = 9;
    private boolean roverOver;
    /** Which cracks she has had a word about in this land, and which kinds of place off it. */
    private int hinted, saidRoom;
    private int seenAsked, seenDone, seenFailed, seenCalled, seenBursts, seenWoken;
    /** How long the moment in which something is won from whoever fell has gone on, below nothing when there is none; and where it rose from, in squares. */
    private float prizeAge = -1, prizeX, prizeY;
    /** What is still to be said in cards of their own, when the one on the screen has gone. */
    private final List<Object[]> toasts = new ArrayList<>();
    private boolean saidRumour;
    /**
     * A place off the land says what it is as she goes in: which kind, and how long its
     * card has been up, below nothing when there is none. And when what happens in it is
     * over, a card of how it went: how long it has been up, its heading, what she did, and
     * what it gave.
     */
    private int roomCard = -1, roomRank = -1, seenPhase;
    /** How long the wave of a cellar has stood as it stands, for its banner. */
    private float sincePhase = 9;
    /** For putting the lights out: a light that takes the dark away where it is drawn, brightest at its middle. */
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float roomCardAge = -1, roomResultAge = -1;
    private String roomResult = "", roomScore = "", roomGave = "";
    private int roomPicture, seenChests, tickedAt;
    /**
     * What was asked of her is handed in: how long the moment of it has gone on, below
     * nothing when there is none; who thanks her, where she stands, and whether she has
     * come running; what she hands over, by its name, what it does and its picture; and
     * whether Halgakos has had her say about it. And what is to be said of it on the card
     * at the end of the land.
     */
    private float handAge = -1, handX, handY;
    private int handWho, handPicture, seenPaid, seenReminders;
    private boolean handRuns, handSaid, handQuiet, handStamped;
    private String handTitle = "", handWords = "", questLine;
    private final android.graphics.ColorFilter shade = new android.graphics.PorterDuffColorFilter(0xFF241030, android.graphics.PorterDuff.Mode.SRC_ATOP);
    /** How someone arriving is to be drawn at this moment: how far along from her place, how far over it, and how solid. */
    private float inSlide, inLift;
    private int inAlpha;
    /** What was won to keep at the end of this land, if anything was: a colour, or below nothing something else. */
    private int prizeWon;
    /** Something won, said in a card of its own for a few seconds: a heading, a line under it, and its picture in the fourth sheet, or a colour as less than nothing. */
    private String toastTitle, toastWords;
    private int toastPicture;
    private float toastLeft;
    /** The colour she will call on, of those that are hers; and the buttons that call on it and choose another. */
    private int chosen;
    private final RectF colourButton = new RectF(), nextButton = new RectF();
    /** How long since what was asked of her was done or lost: its word stays on the screen a while. */
    private float sinceAsked = 9;
    private static final int CAKE_PICTURE = 3, LETTER_PICTURE = 4, WATCH_PICTURE = 5, SCROLL_PICTURE = 7, HER_PICTURE = 99;
    /** The name of the line last said; and how much longer what someone met in the land is saying is not to be talked over. */
    private String saying;
    private float personLeft;
    /** Where on the screen the sign over the stall she stands at is, to be tapped. */
    private final RectF stallSign = new RectF();
    /** Where she was last hurt, her feet, which way she faced, and how long ago: below zero when that is over. */
    private float knockX, knockY, knockAge = -1;
    private boolean knockLeft;
    /** Sparks from a plus and dust from a landing: across, down, their speeds, how long each has left, and which it is. */
    private final List<float[]> bits = new ArrayList<>();
    /** Words that rise from where something was won: across, down, how long ago; and what each says. */
    private final List<float[]> pops = new ArrayList<>();
    private final List<String> popWords = new ArrayList<>();
    private String caption;
    private int captionWho;
    private float captionLeft;

    // The story
    private String[][] telling;
    private int line, storyChapter, storyOver = MAP;
    private Runnable afterStory;

    // The end of a land
    private boolean won;
    private int stars, banked;
    private float resultClock;
    private int starsShown;

    // The counter
    private float refused;

    RunGame(PlayStage stage, Context context, boolean dark, boolean haptics, Bitmap still, Bitmap pet) {
        this.stage = stage;
        this.context = context;
        this.dark = dark;
        this.haptics = haptics;
        session = PlaySession.get(context);
        names = new CompanionPrefs(context);
        audio = new RunAudio(context, names.gameSound());
        density = context.getResources().getDisplayMetrics().density;
        // The game is always played by the pink-haired Halgakos, whichever of the two keeps the user company.
        // Her own pictures, with her own hair, whatever colour the companion wears outside: in a land a colour of hair is a power,
        // and she is in a colour only while she has it.
        this.pet = BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_pet);
        jump = BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_jump);
        lose = BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_lose);
        art = BitmapFactory.decodeResource(context.getResources(), R.drawable.plus_run_art);
        portrait = BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_pet_large);
        measure();
        cardColor = dark ? 0xFF1D2023 : Color.WHITE;
        ink = dark ? 0xFFF3F3F3 : 0xFF292B2A;
        soft = dark ? 0xFFC9CDCF : 0xFF565A57;
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        for (int index = 0; index < lands.length; index++) lands[index] = new RectF();
        for (int index = 0; index < wares.length; index++) wares[index] = new RectF();
        // A chapter in which everything asked was done before there were medals has its medal now.
        for (int chapter = 0; chapter < RunLevels.ALL_CHAPTERS; chapter++) medal(chapter, false);
        // What whoever waits at the end of a land gives is hers if she has got past them, whenever that was.
        for (int land = 1; land <= RunLevels.TOTAL; land++) if (starsOf(land) > 0) prize(land);
        chosen = session.tally("run_colour");
        herMix = session.tally("run_mix");
        // The first time, the story of how it began comes before anything else.
        if (session.tally("run_pro2") == 0) {
            session.keep("run_pro2", 1);
            tell(RunStory.PROLOGUE, 0, () -> screen = MAP);
        }
    }

    /** Finds how much of its square each picture of the art fills: every one stands on the bottom of its square, in the middle. */
    private void measure() {
        int side = art.getHeight(), count = Math.min(ART_COUNT, art.getWidth() / side);
        int[] pixels = new int[side * side];
        for (int which = 0; which < ART_COUNT; which++) {
            artWide[which] = 1;
            artTall[which] = 1;
            if (which >= count) continue;
            art.getPixels(pixels, 0, side, which * side, 0, side, side);
            int left = side, right = -1, top = side;
            for (int y = 0; y < side; y++) {
                for (int x = 0; x < side; x++) {
                    if (pixels[y * side + x] >>> 24 > 40) {
                        left = Math.min(left, x);
                        right = Math.max(right, x);
                        top = Math.min(top, y);
                    }
                }
            }
            if (right >= left) {
                // As wide as the further of its two sides is from the middle, since it stands in the middle.
                artWide[which] = Math.min(1, 2 * Math.max(side / 2f - left, right + 1 - side / 2f) / side);
                artTall[which] = (side - top) / (float) side;
            }
        }
    }

    // What is kept between one time and the next

    /** The furthest land that may be played: each land opens the one after it. */
    private int open() { return Math.max(1, Math.min(RunLevels.OPEN_TO, session.tally("run_open"))); }

    /** The volumes of the map: how many of them are open; and the first land of one, less one, the land after its last, less one, and how many lands a chapter of it has. */
    private int volumes() { return open() > RunLevels.SECOND ? 3 : open() > RunLevels.COUNT ? 2 : 1; }
    private static int firstOf(int volume) { return volume == 0 ? 0 : volume == 1 ? RunLevels.COUNT : RunLevels.SECOND; }
    private static int lastOf(int volume) { return volume == 0 ? RunLevels.COUNT : volume == 1 ? RunLevels.SECOND : RunLevels.TOTAL; }
    private static int eachOf(int volume) { return volume == 0 ? RunLevels.IN_CHAPTER : RunLevels.IN_LATER_CHAPTER; }
    private RectF tabOf(int volume) { return volumes() > 2 ? tabs3[volume] : tabs[volume]; }

    private int starsOf(int land) { return session.tally("run_stars_" + land); }

    /** The pluses she has to spend, and how far along each thing she can buy she is. */
    private int bank() { return session.tally("run_bank"); }

    private int bought(int which) { return Math.min(UP_COSTS[which].length, session.tally("run_up_" + which)); }

    /**
     * What she has to keep is read again, and given to the land she is in: so that what is
     * bought at the counter by a stall, or won, is hers there and then and not only in the
     * next land.
     */
    private void regrant() {
        perks = kept();
        if (run != null) run.grant(perks);
    }

    /** What she takes into a land of what she has bought. */
    /**
     * Her experience in all, kept; and from it her level. Kept from before there were levels, it is given what the lands she has won would
     * have given her, once, so that she comes to them at the level they are for.
     */
    private long herXp() {
        if (session.tally("run_xp_given") == 0) {
            session.keep("run_xp_given", 1);
            long given = 0;
            for (int land = 1; land <= RunLevels.TOTAL; land++) if (starsOf(land) > 0 && land < RunLevels.TOTAL) given = RunLoot.need(RunLoot.recommended(land + 1));
            if (given > session.tally("run_xp")) session.keep("run_xp", (int) Math.min(Integer.MAX_VALUE, given));
        }
        return session.tally("run_xp") & 0xFFFFFFFFL;
    }

    private int herLevel() { return RunLoot.levelAt(herXp()); }

    PlusRun.Perks kept() {
        PlusRun.Perks kept = new PlusRun.Perks();
        kept.hearts = PlusRun.HEARTS + bought(0);
        kept.lives = PlusRun.LIVES + bought(7);
        // How many of her colours she may wear at once: two, and with each step of the prism one more, the last step all seven.
        kept.together = bought(PRISM) >= PRISM_NEEDS.length ? 7 : 2 + bought(PRISM);
        kept.recharge -= bought(QUICK);
        kept.wings = bought(1) > 0;
        kept.triple = bought(TRIPLE) > 0;
        kept.airDash = bought(AIR_DASH) > 0;
        kept.dashStrikes = bought(AIR_DASH) > 1;
        kept.thorns = bought(THORNS);
        kept.pound = bought(POUND) > 0;
        kept.roll = bought(ROLL) > 0;
        kept.wallJump = bought(WALL_JUMP) > 0;
        kept.glide = bought(GLIDE) > 0;
        kept.poundLevel = bought(POUND);
        kept.glideLevel = bought(GLIDE);
        kept.aerial = bought(AERIAL);
        kept.secondWind = bought(SECOND_WIND) > 0;
        kept.reach = bought(2) == 0 ? 0 : bought(2) == 1 ? 1.6f : 2.6f;
        kept.lasting = 1 + .5f * bought(3);
        kept.shots = PlusRun.GUN_SHOTS + 8 * bought(4);
        kept.flagHeart = bought(5) > 0;
        kept.inkHurts = 1 + bought(6);
        // Black is not one of the colours she wears: owned, it is that black lies on her half as long.
        kept.colours = colours() & PlusRun.ALL;
        kept.resists = (colours() & 1 << PlusRun.BLACK - 1) != 0;
        kept.formLonger = session.tally("run_prize_" + RunLevels.COUNT) != 0 ? 1.5f : 1;
        // What doing what was asked has paid, land by land: two shots, a twentieth longer, a quarter of a second sooner; and half a second
        // sooner for each chapter's medal.
        for (int land = 1; land <= RunLevels.TOTAL; land++) {
            if (session.tally("run_quest_" + land) == 0) continue;
            int pays = RunLevels.questPrize(RunLevels.questOf(land));
            if (pays == RunLevels.SHOTS) kept.shots += 2;
            else if (pays == RunLevels.LASTING) kept.lasting += .05f;
            else if (pays == RunLevels.QUICKER) kept.recharge -= .25f;
        }
        for (int chapter = 0; chapter < RunLevels.ALL_CHAPTERS; chapter++) if (session.tally("run_medal_" + chapter) != 0) kept.recharge -= .5f;
        // And what the others have given: a heart, eight shots, three seconds, for each of them got past.
        for (int land = 1; land <= RunLevels.TOTAL; land++) {
            int prize = RunLevels.prizeOf(land);
            if (prize >= RunLevels.NOTHING || prize == RunLevels.RING || session.tally("run_prize_" + land) == 0) continue;
            if (prize == RunLevels.HEART) kept.hearts++;
            else if (prize == RunLevels.TANK) kept.shots += 8;
            else if (prize == RunLevels.QUICK) kept.recharge -= 3;
        }
        // Her pets: those that are hers for good and chosen to come, no more of them than she has places for; room for strays; and
        // what the kennel has made of them.
        int with = session.tally("run_pet_with") & session.tally("run_pet_own");
        while (Integer.bitCount(with) > 1 + bought(PET_PLACES)) with &= ~Integer.highestOneBit(with);
        kept.petWith = with;
        kept.tempSlots = 1 + bought(STRAY_ROOM);
        kept.petBrisk = 1 - .2f * bought(PET_BRISK);
        kept.petStrong = bought(PET_STRONG);
        // Her backpack: every thing to fight with that she has found; her tablet-book, which is hers from the start; and her ring of keys
        // once it is. She begins every land with the one of those two that she has chosen.
        int mains = session.tally("run_mains") | 1;
        kept.pack = session.tally("run_pack");
        for (int index = 0; index < RunArms.MAINS.length; index++) if ((mains >> index & 1) != 0) kept.pack |= 1 << RunArms.MAINS[index];
        int chosen = session.tally("run_main");
        kept.main = chosen >= 0 && chosen < RunArms.MAINS.length && (mains >> chosen & 1) != 0 ? RunArms.MAINS[chosen] : PlusRun.TABLET;
        kept.recharge = Math.max(4, kept.recharge);
        // The pieces of a heart found in chests: four of them are a heart, and there are two hearts to be made of them.
        kept.hearts += Math.min(PIECE_HEARTS, session.tally("run_pieces") / PIECES);
        // What the smithy has made: how far each thing to fight with has been worked on; and what she wears, which is one thing for each
        // piece, and a heart more for all four of one set.
        for (int kind = 0; kind < RunArms.COUNT; kind++) kept.levels[kind] = forged(kind);
        int on = gearOn();
        kept.defence = RunGear.defence(on) + RunLoot.guard(herLevel());
        kept.mercy = RunGear.mercy(on);
        kept.stomp = RunGear.stomp(on);
        if (RunGear.whole(on)) kept.hearts++;
        // Her level: her blows land harder for it, a hurt costs less, and now and then it is a heart more.
        kept.level = herLevel();
        kept.hearts += RunLoot.hearts(kept.level);
        // What her skill tree gives, and the actives in her slots. (Kept from before there was a tree, she has the points her level gives,
        // all to spend.)
        java.util.BitSet taken = skillsTaken();
        kept.skills = RunSkills.built(taken);
        kept.slotted = RunSkills.slotted(taken, skillSlots());
        // What she wears of what she has found.
        kept.worn = RunLoot.worn(session.note("run_worn"));
        // The weapon found that she has taken in hand: in her backpack as its kind, and what she begins every land with.
        int hand = handCode();
        if (hand != 0) {
            int kind = RunArms.kindOf(hand);
            kept.item = hand;
            kept.pack |= 1 << kind;
            kept.main = kind;
        }
        // Her inventory's places: the main weapon may be any weapon of her backpack, and there is a secondary one, which a swap changes it for.
        kept.itemMain = kept.item;
        int mainEntry = RunInventory.main(session);
        if (hand == 0 && RunInventory.plain(mainEntry) && (RunInventory.owned(session) >> RunInventory.kindOf(mainEntry) & 1) != 0) {
            kept.main = RunInventory.kindOf(mainEntry);
            kept.pack |= 1 << kept.main;
        }
        int secondEntry = RunInventory.second(session);
        if (secondEntry != 0) {
            kept.second = RunInventory.kindOf(secondEntry);
            kept.pack |= 1 << kept.second;
            if (!RunInventory.plain(secondEntry)) kept.itemSecond = secondEntry;
        }
        // And how much harder she has chosen to have it, at the black market.
        kept.harder = hard();
        // The white that is truly hers, once it has been won in the last land: hers to call on in any land.
        kept.white = (colours() & 1 << PlusRun.WHITE - 1) != 0;
        // Whether she is whole, for the black ribbon thrown in the last land: every colour hers to keep, and every friend pledged.
        // (In a build made for testing it can be said to be so, or not so.)
        int said = BuildConfig.DEBUG ? session.tally("run_whole") : 0;
        kept.whole = said == 1 || said != 2 && (colours() & 127) == 127
                && (session.tally("run_friends") & (1 << RunLevels.FRIENDS) - 1) == (1 << RunLevels.FRIENDS) - 1;
        return kept;
    }

    /** The colours of hair that are hers to keep, as bits, the first for red: kept where her settings can see them. */
    private int colours() { return session.tally("run_colours"); }

    /**
     * What getting past whoever waits at the end of a land gives is made hers, if it is
     * not already. Returns what it was: a colour, or below nothing something else, or
     * nothing if there was nothing new.
     */
    private int prize(int land) {
        int prize = RunLevels.prizeOf(land);
        if (prize == RunLevels.NOTHING || session.tally("run_prize_" + land) != 0) return RunLevels.NOTHING;
        session.keep("run_prize_" + land, 1);
        if (prize > 0) session.keep("run_colours", colours() | 1 << prize - 1);
        if (prize == RunLevels.BOUNTY) session.keep("run_bank", bank() + 200);
        return prize;
    }

    private static String prizeName(int prize) {
        return prize > 0 ? FORM_NAMES[prize - 1] + " hair" : prize == RunLevels.RING ? "Zero's ring" : prize == RunLevels.HEART ? "A heart more"
                : prize == RunLevels.TANK ? "A bigger ink tank" : prize == RunLevels.QUICK ? "Quicker colours" : "A bounty of 200";
    }

    private static String prizeGives(int prize) {
        return prize == PlusRun.BLACK ? "black put on her lies on her half as long" : prize > 0 ? FORM_GIVES[prize - 1]
                : prize == RunLevels.RING ? "every form lasts half as long again" : prize == RunLevels.HEART ? "one more heart in every land"
                : prize == RunLevels.TANK ? "an ink gun holds eight more shots" : prize == RunLevels.QUICK ? "her colours are ready three seconds sooner"
                : "two hundred pluses, kept";
    }

    /**
     * One met in the middle of a land has been beaten, or, the wyrm, driven off. Beaten for
     * the first time in a land, it gives a purse to keep that grows with the chapters, and
     * a piece of a heart; beaten there again, fifteen pluses to keep. The wyrm seen off
     * without being beaten gives ten, the first time in a land. Which of the three have
     * ever been beaten is kept, and how many times in all.
     */
    private void roverDone(PlusRun.Foe rover, boolean beaten) {
        int kind = rover.kind, had = session.tally("run_rover_" + number);
        String name = waitName(kind);
        if (beaten) {
            for (int slot = 0; slot < RunHunts.SLOTS; slot++) if (RunHunts.countsRover(hunt(slot), kind)) hunted(slot);
            session.keep("run_rovers", session.tally("run_rovers") | 1 << kind - RunLevels.WYRM);
            session.keep("run_roved", session.tally("run_roved") + 1);
            audio.play("win", .8f);
            shake = Math.max(shake, .3f);
            float at = rover.x + rover.wide() / 2, high = rover.y + rover.tall() * .5f;
            for (int spark = 0; spark < 18; spark++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{at, high, (float) Math.cos(way) * 7, (float) Math.sin(way) * 7 - 3, .6f, spark % 3 == 0 ? 1 : 0});
            }
            if (had < 2) {
                session.keep("run_rover_" + number, 2);
                int purse = 30 + 10 * RunLevels.chapterOf(number);
                session.keep("run_bank", bank() + purse);
                toast(name + " is beaten", "+ " + purse + " to keep" + piece(), CHEST_PICTURE);
            } else {
                session.keep("run_bank", bank() + 15);
                toast(name + " is beaten again", "+ 15 to keep", CHEST_PICTURE);
            }
            speak(RunStory.ROVERS[3], true);
        } else {
            if (had < 1) {
                session.keep("run_rover_" + number, 1);
                session.keep("run_bank", bank() + 10);
                toast(name + " is driven off", "+ 10 to keep. Beaten, it gives more", CHEST_PICTURE);
            }
            speak(RunStory.ROVERS[4], true);
        }
    }

    // Her friends

    /**
     * One of her friends, standing in a land of the third volume: each as she is drawn where
     * she was met. Over her head a mark until she has pledged, and a heart once she has.
     */
    private void drawFriend(Canvas canvas, int[] item) {
        int friend = item[4];
        float x = (item[1] + .5f - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile, size = tile * 1.62f, head = feet - tile * 1.9f;
        if (x < -tile * 3 || x > width + tile * 3) return;
        boolean near = Math.abs(run.x() - item[1]) < 6, pledged = (session.tally("run_friends") >> friend & 1) != 0;
        paint.setAlpha(255);
        if (friend <= 3) {
            int who = friend == 0 ? CHIKUWA : friend == 1 ? RUNE : friend == 2 ? ILLYA : TSUBAKIEH;
            if (friend <= 1 && near) frame(canvas, cast(who), 1, CAST_FRAMES[who], x, feet, size, false);
            else frame(canvas, idleSheet(IDLE_CAST[who]), idleFrame(5000 + friend), IDLE_FRAMES, x, feet, size, false);
        } else if (friend == 13) {
            cell(canvas, idleSheet(R.drawable.run_idle_nuri), idleFrame(5000 + friend), IDLE_FRAMES, x, feet, tile * 1.7f, false);
        } else if (friend == 16) {
            Bitmap sheet = smith(0);
            if (near) cell(canvas, sheet, 3, 4, x, feet, tile * 1.3f * sheet.getHeight() / 150f, false);
            else cell(canvas, idleSheet(IDLE_SMITHS[0]), idleFrame(5000 + friend), IDLE_FRAMES, x, feet, tile * 1.3f * sheet.getHeight() / 150f, false);
            head = feet - tile * 1.6f;
        } else if (friend >= 17) {
            frame(canvas, idleSheet(IDLE_VILLAGERS[friend == 17 ? RunLevels.INJU - RunLevels.SOMI : 0]), idleFrame(5000 + friend), IDLE_FRAMES, x, feet, size * .72f, false);
            head = feet - tile * 1.55f;
        } else {
            int kind = friend == 4 ? RunLevels.GUARD : friend == 5 ? RunLevels.BUTLER : friend == 6 ? RunLevels.GOKHAE : friend == 7 ? RunLevels.BYEORU : friend == 8 ? RunLevels.MIRI
                    : friend == 9 ? RunLevels.MAWANG : friend == 10 ? RunLevels.JUPAN : friend == 11 ? RunLevels.YEOBAEK : friend == 12 ? RunLevels.SUMBI : friend == 14 ? RunLevels.PADS
                    : RunLevels.PARTY;
            int which = kind - RunLevels.GOKHAE;
            // (The twins stand together.)
            if (kind == RunLevels.MIRI) frame(canvas, idleSheet(IDLE_LATER[which + 1]), idleFrame(5100 + friend), IDLE_FRAMES, x + tile * .95f, feet, size, false);
            if (IDLE_LATER[which] != 0) frame(canvas, idleSheet(IDLE_LATER[which]), idleFrame(5000 + friend), IDLE_FRAMES, x, feet, size, false);
            else frame(canvas, later(which), 0, LATER_FRAMES[which], x, feet, size, false);
        }
        if (pledged) {
            paint.setAlpha(255);
            art(canvas, HEART_ART, x, head + tile * .1f + (float) Math.sin(clock * 3 + item[1]) * tile * .05f, tile * .36f, false);
        } else {
            mark(canvas, x, head - Math.abs((float) Math.sin(clock * 4)) * tile * .08f, "Pledge", SPEAKER_COLOURS[RunStory.speaker(RunStory.PLEDGES[friend])], false);
        }
    }

    // The ribbon gacha

    /**
     * The machine's handle is turned, if it can be and her pluses stretch to it: the roll
     * is paid for from what she has to keep, and a capsule comes out, which opens a moment
     * later. One at a time.
     */
    private void crank() {
        float x = run.x() + PlusRun.WIDE / 2, y = run.y() - .4f;
        if (capsuleWaits) return;
        if (!run.canRoll()) {
            audio.play("no", .5f);
            pop(x, y, run.gachaBlown() ? "It is in pieces" : run.cursed() > 0 ? "Not while black lies on her" : "It holds nothing she has not had");
            return;
        }
        int cost = run.gachaCost();
        if (bank() < cost) {
            audio.play("no", .5f);
            pop(x, y, "A roll is + " + cost);
            if (refused <= 0) speak(RunStory.SHOP[2], false);
            refused = 5;
            return;
        }
        if (session.tally("run_gacha") == 0) speak(RunStory.GACHA[0], true);
        session.keep("run_bank", bank() - cost);
        session.keep("run_gacha", session.tally("run_gacha") + 1);
        capsuleCost = cost;
        capsuleChance = random.nextInt(1 << 20);
        // (For looking at it blown up, in a build made for testing: the roll of that number is a black one.)
        if (BuildConfig.DEBUG && session.tally("run_gacha_black") == run.gachaRolls() + 1) capsuleChance = PlusRun.GACHA_COLOUR * RunLevels.machineOf(number).length;
        capsuleAge = 0;
        capsuleWaits = true;
        audio.play("door", .6f);
        buzz(false);
    }

    /**
     * The capsule opens. A ribbon she has not had from the machine: it is in her hair. One
     * she has had: half the price of the roll back, in pluses. Every colour the machine
     * holds: white with them, of the kind that is not mastered. A black one: the machine
     * blows up. (And if she has walked away from it meanwhile, her pluses back.)
     */
    private void opened() {
        capsuleWaits = false;
        int whites = run.gachaWhites(), gave = run.roll(capsuleChance);
        float x = run.x() + PlusRun.WIDE / 2, y = run.y() - .4f;
        capsuleGave = gave;
        if (gave == 0) {
            session.keep("run_bank", bank() + capsuleCost);
            return;
        }
        if (gave == PlusRun.BLACK) {
            session.keep("run_gacha_boom", session.tally("run_gacha_boom") + 1);
            audio.play("fall", .9f);
            audio.play("crumble", .8f);
            shake = Math.max(shake, .5f);
            buzz(true);
            for (int spark = 0; spark < 26; spark++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{x, y, (float) Math.cos(way) * 8, (float) Math.sin(way) * 8 - 3, .7f, 0});
            }
            toast("The machine blew up", "A black ribbon: every ribbon is gone, and black lies on her from when she leaves", 0);
            speak(RunStory.GACHA[1], true);
            return;
        }
        if (gave < 0) {
            int back = capsuleCost / 2;
            session.keep("run_bank", bank() + back);
            audio.play("buy", .5f);
            pop(x, y, FORM_NAMES[-gave - 1] + " again: + " + back);
            flyTo((x - cameraX) * tile, (y - cameraY) * tile, false, 4);
            return;
        }
        audio.play("star", .7f);
        buzz(true);
        if (run.gachaWhites() > whites) {
            session.keep("run_gacha_white", session.tally("run_gacha_white") + 1);
            toast("Every colour it holds: white", "All of them at once, for a while; not mastered. Its time stands still in here", 0);
        } else {
            pop(x, y, "A " + FORM_NAMES[gave - 1].toLowerCase(java.util.Locale.ROOT) + " ribbon");
        }
    }

    /**
     * The machine of the ribbon gacha, on its stand; shaking as its handle is turned, and
     * in pieces once it has blown up. Over it, what it holds: a ribbon of each colour,
     * bright if she has had it from the machine, with how likely each is and how likely a
     * black one. And a capsule, as one comes out: it rolls from the chute, hops, and opens
     * on what was in it.
     */
    private void drawMachine(Canvas canvas, int[] item) {
        if (gachaMachine == null) gachaMachine = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_gacha_machine);
        boolean blown = run.gachaBlown();
        float x = (item[1] + 1.2f - cameraX) * tile, floor = (item[2] + 1 - cameraY) * tile, feet = floor - tile * .3f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFC8607C);
        canvas.drawRect(x - tile * .9f, feet, x + tile * .9f, floor, paint);
        paint.setColor(0xFFF0A0B8);
        canvas.drawRect(x - tile * .95f, feet, x + tile * .95f, feet + tile * .1f, paint);
        paint.setColor(Color.BLACK);
        float rattle = capsuleWaits && capsuleAge < .5f ? (float) Math.sin(capsuleAge * 60) * tile * .05f : 0;
        cell(canvas, gachaMachine, blown ? 1 : 0, 2, x + rattle, feet, tile * 2f * gachaMachine.getHeight() / 280f, false);
        // What it holds, and the chances, on a plate over it.
        int[] holds = RunLevels.machineOf(number);
        int held = run.gachaHeld(), black = PlusRun.blackOdds(number), each = Math.round((100f - black) / holds.length);
        float size = tile * .46f, wide = holds.length * size + tile * .3f, top = feet - tile * 3.55f;
        box.set(x - Math.max(wide, tile * 3.3f) / 2, top, x + Math.max(wide, tile * 3.3f) / 2, top + tile * .98f);
        plate(canvas, box, tile * .2f, 235);
        for (int index = 0; index < holds.length; index++) {
            paint.setAlpha(blown ? 60 : (held >> holds[index] - 1 & 1) != 0 ? 255 : 105);
            pieceIn(canvas, two, RIBBON_ART + holds[index] - 1, x + (index - (holds.length - 1) / 2f) * size, top + tile * .3f, size * .9f, 0);
        }
        paint.setAlpha(255);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(blown ? soft : ink);
        fitted(canvas, blown ? "Blown up: shut for this land" : run.gachaDone() ? "She has had all it holds" : "each " + each + "%  ·  black " + black + "%  ·  roll + " + run.gachaCost(), x,
                top + tile * .82f, tile * .25f, box.width() - tile * .2f);
        // A capsule: out of the chute, up, and open.
        if (capsuleAge < 1.5f && (capsuleWaits || capsuleGave != 0)) {
            float out = Math.min(1, capsuleAge / .9f), cx = x - tile * .1f - out * tile * .9f, cy = feet - tile * .25f - (float) Math.sin(out * Math.PI) * tile * 1.1f;
            int shown = capsuleWaits ? 0 : capsuleGave;
            if (capsuleWaits) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFF4F4F8);
                canvas.drawCircle(cx, cy, tile * .2f, paint);
                paint.setColor(0xFFE8779A);
                box.set(cx - tile * .2f, cy - tile * .2f, cx + tile * .2f, cy + tile * .2f);
                canvas.drawArc(box, 0, 180, true, paint);
                stroke.setColor(0xFF5A3040);
                stroke.setStrokeWidth(tile * .03f);
                canvas.drawCircle(cx, cy, tile * .2f, stroke);
                paint.setColor(Color.BLACK);
            } else {
                // Open: its two halves fall away, and what was in it rises.
                float since = capsuleAge - .9f, rise = Math.min(1, since / .35f);
                int colour = Math.abs(shown);
                paint.setAlpha(Math.round(255 * Math.max(0, 1 - since / .6f)));
                pieceIn(canvas, two, RIBBON_ART + Math.min(PlusRun.BLACK, colour) - 1, x - tile, feet - tile * .5f - rise * tile * .8f, tile * (.5f + .3f * rise), 0);
                paint.setAlpha(255);
            }
        }
        if (!blown) mark(canvas, x, top - tile * .05f, "Gacha", 0xFFE8779A, false);
    }

    /** A pot of black ink: squat, with a black bow; it swells as its turn comes round, and a mark over it warns of what it is about to throw. */
    private void drawPot(Canvas canvas, int[] item) {
        float turn = run.potTurn(item), swell = Math.max(0, (turn - (1 - PlusRun.POT_SWELLS / PlusRun.POT_EVERY)) / (PlusRun.POT_SWELLS / PlusRun.POT_EVERY));
        float x = (item[1] + .5f - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile, round = tile * (.3f + .1f * swell);
        if (x < -tile * 2 || x > width + tile * 2) return;
        paint.setStyle(Paint.Style.FILL);
        if (swell > 0) {
            paint.setColor(0xFF7A3FD0);
            paint.setAlpha(Math.round(90 * swell));
            canvas.drawCircle(x, feet - round, round * 1.9f, paint);
            paint.setAlpha(255);
        }
        paint.setColor(0xFF14121C);
        canvas.drawOval(x - round * 1.15f, feet - round * 1.7f, x + round * 1.15f, feet, paint);
        paint.setColor(0xFF3A3050);
        canvas.drawOval(x - round * .7f, feet - round * 1.95f, x + round * .7f, feet - round * 1.45f, paint);
        paint.setColor(0xFF06050A);
        canvas.drawOval(x - round * .5f, feet - round * 1.85f, x + round * .5f, feet - round * 1.55f, paint);
        paint.setColor(0xFF5A4A80);
        canvas.drawOval(x - round * .75f, feet - round * 1.2f, x - round * .35f, feet - round * .6f, paint);
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        pieceIn(canvas, two, RIBBON_ART + PlusRun.BLACK - 1, x + round * .2f, feet - round * .75f, round * 1.1f, 0);
        if (swell > 0) warn(canvas, x, feet - round * 2.6f, 0xFF7A3FD0);
    }

    // The black market

    /** How much harder she has chosen to have it; whether the story is past a land; and whether a rung of the black arena is open to her. */
    private int hard() { return Math.max(0, Math.min(RunMarket.STEPS, session.tally("run_hard"))); }
    private boolean past(int land) { return land < RunLevels.TOTAL ? open() > land : starsOf(land) > 0; }
    private boolean rungOpen(int rung) { return rung >= 1 && rung <= RunMarket.RUNGS && rung <= session.tally("run_black") + 1 && past(RunMarket.LANDS[rung - 1]); }

    /** Whoever deals is walked up to: what she deals in is looked at, and the land waits. The first time, she says what there is; and Halgakos has her word. */
    private void openMarket() {
        marketOpen = true;
        marketSaid = "";
        rungOn = Math.max(1, Math.min(RunMarket.RUNGS, session.tally("run_black") + 1));
        goLeft = goRight = goJump = goStrike = false;
        jumpPressed = strikePressed = false;
        audio.play("page", .6f);
        if (session.tally("run_market_met") == 0) {
            session.keep("run_market_met", 1);
            person(RunStory.MARKET[0]);
        }
    }

    /** She goes down into the black arena, to a rung of it: what she has collected in the land she leaves is kept. */
    private void enterRung(int rung) {
        session.keep("run_bank", bank() + run.purse());
        marketOpen = false;
        blackRung = rung;
        begin(RunMarket.LANDS[rung - 1], true);
        person(RunStory.MARKET[1]);
    }

    /** A rung is over. Won: its tokens, and the first time what it gives; and the next is open, if the story has been past whoever waits on it. */
    private void finishRung(boolean reached) {
        won = reached;
        idle();
        resultClock = 0;
        starsShown = 0;
        stars = 0;
        screen = RESULT;
        captionLeft = 0;
        banked = 0;
        rungWords = rungPrize = "";
        if (won) {
            boolean first = session.tally("run_black") < blackRung;
            int tokens = RunMarket.tokens(blackRung, first);
            session.keep("run_tokens", session.tally("run_tokens") + tokens);
            rungWords = "+ " + tokens + (tokens == 1 ? " token" : " tokens");
            if (first) {
                session.keep("run_black", blackRung);
                rungPrize = RunMarket.prizeWords(blackRung);
                int what = RunMarket.prizeOf(blackRung);
                switch (RunMarket.prize(blackRung)) {
                    case RunMarket.PLUSES:
                        session.keep("run_bank", bank() + what);
                        break;
                    case RunMarket.WEAPON:
                        for (int index = 0; index < RunArms.MAINS.length; index++) {
                            if (RunArms.MAINS[index] == what) session.keep("run_mains", session.tally("run_mains") | 1 | 1 << index);
                        }
                        break;
                    case RunMarket.STUFF:
                        for (int index = 0; index < 3; index++) session.keep("run_mat_" + index, session.tally("run_mat_" + index) + RunMarket.STUFFS[what - 1][index]);
                        break;
                    case RunMarket.TREATS:
                        session.keep("run_treats", session.tally("run_treats") + what);
                        break;
                    default:
                        session.keep("run_sash", 1);
                        session.keep("run_bank", bank() + 1500);
                }
            }
            audio.play("win");
            speak(RunStory.WON[2], false);
            if (blackRung < RunMarket.RUNGS && past(RunMarket.LANDS[blackRung])) pills("Next rung", NEXT, "Again", AGAIN, "Lands", TO_MAP);
            else pills("Again", AGAIN, "Lands", TO_MAP);
        } else {
            audio.play("lose");
            speak(RunStory.LOST[random.nextInt(RunStory.LOST.length)], false);
            pills("Try again", AGAIN, "Lands", TO_MAP);
        }
    }

    private void marketTouch(float x, float y) {
        for (int tab = 0; tab < marketTabs.length; tab++) {
            if (!marketTabs[tab].contains(x, y)) continue;
            marketTab = tab;
            marketSaid = "";
            audio.play("tap", .5f);
            return;
        }
        if (marketTab == 0) {
            for (int step = 0; step < hardRows.length; step++) {
                if (!hardRows[step].contains(x, y)) continue;
                // Chosen: from the next land she begins.
                session.keep("run_hard", step);
                marketSaid = step == 0 ? "As it is, from the next land she begins" : "Harder by " + step + ", from the next land she begins";
                audio.play("unlock", .5f);
                buzz(false);
                return;
            }
        } else if (marketTab == 1) {
            for (int rung = 1; rung <= RunMarket.RUNGS; rung++) {
                if (rungRows[rung - 1] == null || !rungRows[rung - 1].contains(x, y)) continue;
                rungOn = rung;
                audio.play("tap", .5f);
                return;
            }
            if (!marketAct.isEmpty() && marketAct.contains(x, y)) {
                if (rungOpen(rungOn)) enterRung(rungOn);
                else audio.play("no", .5f);
                return;
            }
        } else {
            int tokens = session.tally("run_tokens"), mains = session.tally("run_mains");
            for (int ware = 0; ware < RunMarket.WARES; ware++) {
                if (wareBuys[ware] == null || !wareBuys[ware].contains(x, y)) continue;
                if (tokens < RunMarket.WARE_COSTS[ware] || ware == RunMarket.KEYS && (mains & 2) != 0) {
                    audio.play("no", .5f);
                    return;
                }
                session.keep("run_tokens", tokens - RunMarket.WARE_COSTS[ware]);
                if (ware == RunMarket.KEYS) session.keep("run_mains", mains | 3);
                else if (ware == RunMarket.TREAT) session.keep("run_treats", session.tally("run_treats") + 1);
                else if (ware == RunMarket.METAL) session.keep("run_mat_0", session.tally("run_mat_0") + 3);
                else if (ware == RunMarket.CRYSTAL) session.keep("run_mat_1", session.tally("run_mat_1") + 2);
                else session.keep("run_mat_2", session.tally("run_mat_2") + 1);
                marketSaid = "Hers: " + RunMarket.WARE_NAMES[ware].toLowerCase(java.util.Locale.ROOT);
                regrant();
                audio.play("buy", .6f);
                buzz(true);
                if (!audio.speaking()) person(RunStory.MARKET[2]);
                return;
            }
        }
        if (!marketCard.contains(x, y)) {
            marketOpen = false;
            audio.play("tap", .3f);
            if (session.tally("run_market_met") == 1) {
                session.keep("run_market_met", 2);
                if (!audio.speaking()) speak(RunStory.MARKET[3], true);
            }
        }
    }

    /**
     * The black market, over the land. Its three things, a tab each. Harder: the steps she
     * may have the lands by, with what each asks and pays, and the one she has chosen.
     * The arena: its ten rungs, who waits on each and how each stands, won, open, or what
     * it waits for; and for the one looked at what it gives the first time, and a button
     * that takes her down to it. Wares: what her tokens buy.
     */
    private void drawMarket(Canvas canvas) {
        canvas.drawColor(0x99000000);
        int tokens = session.tally("run_tokens"), won = session.tally("run_black"), hard = hard();
        float wide = Math.min(safe.width() - dp(24), dp(430)), pad = dp(16), body = dp(436);
        float tall = pad + dp(92) + body + pad, left = safe.centerX() - wide / 2, top = Math.max(safe.top + dp(8), safe.centerY() - tall / 2);
        marketCard.set(left, top, left + wide, top + tall);
        plate(canvas, marketCard, dp(22), 255);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, "The black market", left + pad, top + pad + dp(18), dp(20), wide * .6f);
        text.setTypeface(Typeface.DEFAULT);
        text.setTextAlign(Paint.Align.RIGHT);
        text.setColor(0xFF6A3FA0);
        fitted(canvas, tokens + (tokens == 1 ? " token" : " tokens"), left + wide - pad, top + pad + dp(17), dp(15), wide * .3f);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(soft);
        fitted(canvas, "Jupan deals  ·  the arena pays in tokens  ·  " + won + " of " + RunMarket.RUNGS + " rungs won", left + pad, top + pad + dp(38), dp(12), wide - pad * 2);
        float y = top + pad + dp(50), each = (wide - pad * 2 - dp(12)) / 3, bottom = top + tall - pad;
        String[] words = {"Harder", "The arena", "Wares"};
        for (int tab = 0; tab < 3; tab++) {
            marketTabs[tab].set(left + pad + tab * (each + dp(6)), y, left + pad + tab * (each + dp(6)) + each, y + dp(32));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(tab == marketTab ? 0xFF6A3FA0 : dark ? 0xFF2C3033 : 0xFFF0F1F2);
            canvas.drawRoundRect(marketTabs[tab], dp(16), dp(16), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(tab == marketTab ? Color.WHITE : ink);
            fitted(canvas, words[tab], marketTabs[tab].centerX(), marketTabs[tab].centerY() + dp(4.5f), dp(13), each - dp(10));
        }
        y += dp(42);
        marketAct.setEmpty();
        if (marketTab == 0) {
            String[] names = {"As it is", "A step harder", "Two steps harder", "Three steps harder"};
            for (int step = 0; step <= RunMarket.STEPS; step++) {
                RectF at = hardRows[step];
                at.set(left + dp(8), y, left + wide - dp(8), y + dp(80));
                boolean chosen = step == hard;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(chosen ? (dark ? 0xFF33294A : 0xFFEFE8FA) : dark ? 0xFF2C3033 : 0xFFF5F6F7);
                canvas.drawRoundRect(at, dp(14), dp(14), paint);
                if (chosen) {
                    stroke.setColor(0xFF6A3FA0);
                    stroke.setStrokeWidth(dp(2));
                    canvas.drawRoundRect(at, dp(14), dp(14), stroke);
                }
                paint.setColor(Color.BLACK);
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(ink);
                text.setTypeface(Typeface.DEFAULT_BOLD);
                fitted(canvas, names[step], at.left + dp(12), at.top + dp(22), dp(14.5f), at.width() * .6f);
                text.setTypeface(Typeface.DEFAULT);
                if (chosen) {
                    text.setTextAlign(Paint.Align.RIGHT);
                    text.setColor(0xFF6A3FA0);
                    fitted(canvas, "chosen", at.right - dp(12), at.top + dp(22), dp(12.5f), dp(70));
                    text.setTextAlign(Paint.Align.LEFT);
                }
                text.setColor(soft);
                text.setTextSize(dp(12));
                List<String> lines = wrap(RunMarket.stepWords(step), at.width() - dp(24));
                for (int index = 0; index < Math.min(3, lines.size()); index++) canvas.drawText(lines.get(index), at.left + dp(12), at.top + dp(40) + index * dp(15), text);
                y += dp(86);
            }
            text.setColor(marketSaid.isEmpty() ? soft : 0xFF2E9E6E);
            fitted(canvas, marketSaid.isEmpty() ? "It holds from the next land she begins. It can be put back at any black market." : marketSaid, left + pad, bottom - dp(8), dp(12), wide - pad * 2);
        } else if (marketTab == 1) {
            for (int rung = 1; rung <= RunMarket.RUNGS; rung++) {
                if (rungRows[rung - 1] == null) rungRows[rung - 1] = new RectF();
                RectF at = rungRows[rung - 1];
                at.set(left + dp(8), y, left + wide - dp(8), y + dp(27));
                boolean on = rung == rungOn, done = rung <= won, open = rungOpen(rung);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(on ? (dark ? 0xFF33294A : 0xFFEFE8FA) : dark ? 0xFF2C3033 : 0xFFF5F6F7);
                canvas.drawRoundRect(at, dp(10), dp(10), paint);
                paint.setColor(Color.BLACK);
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(done || open ? ink : soft);
                text.setTypeface(on ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                // (Who waits on a rung the story has not been past is not said.)
                fitted(canvas, rung + "   " + (past(RunMarket.LANDS[rung - 1]) ? waitName(RunLevels.bossOf(RunMarket.LANDS[rung - 1])) : "Someone not yet met"), at.left + dp(12),
                        at.centerY() + dp(4.5f), dp(13), at.width() * .58f);
                text.setTypeface(Typeface.DEFAULT);
                text.setTextAlign(Paint.Align.RIGHT);
                text.setColor(done ? 0xFF2E9E6E : open ? 0xFF6A3FA0 : soft);
                fitted(canvas, done ? "won" : open ? "open" : rung > won + 1 ? "after rung " + (rung - 1) : "after land " + RunMarket.LANDS[rung - 1], at.right - dp(12), at.centerY() + dp(4.5f),
                        dp(12), at.width() * .34f);
                y += dp(30);
            }
            // The one looked at: what it asks, and what it gives.
            boolean done = rungOn <= won, open = rungOpen(rungOn);
            y += dp(6);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, "Rung " + rungOn + (done ? "  ·  won: a token each time" : "  ·  first win: " + RunMarket.tokens(rungOn, true) + " tokens, and"), left + pad, y + dp(14), dp(13.5f),
                    wide - pad * 2);
            text.setTypeface(Typeface.DEFAULT);
            text.setColor(done ? soft : 0xFF6A3FA0);
            fitted(canvas, RunMarket.prizeWords(rungOn) + (done ? "  (hers)" : ""), left + pad, y + dp(33), dp(13), wide - pad * 2);
            text.setColor(soft);
            fitted(canvas, "She takes " + RunMarket.takes(rungOn) + " blows more, behind " + RunMarket.shields(rungOn) + " more of a shield"
                    + (RunMarket.bites(rungOn) > 0 ? "; a hurt costs " + (RunMarket.bites(rungOn) == 1 ? "half a heart" : "a heart") + " more" : ""), left + pad, y + dp(51), dp(12), wide - pad * 2);
            marketAct.set(left + pad, bottom - dp(40), left + wide - pad, bottom);
            smithButton(canvas, marketAct, open ? "Go down to rung " + rungOn : rungOn > won + 1 ? "Win rung " + (rungOn - 1) + " first" : "The story has not been there yet", open, 0xFF6A3FA0);
        } else {
            int mains = session.tally("run_mains");
            for (int ware = 0; ware < RunMarket.WARES; ware++) {
                if (wareBuys[ware] == null) wareBuys[ware] = new RectF();
                box.set(left + dp(8), y, left + wide - dp(8), y + dp(50));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(dark ? 0xFF2C3033 : 0xFFF5F6F7);
                canvas.drawRoundRect(box, dp(12), dp(12), paint);
                paint.setColor(Color.BLACK);
                boolean hers = ware == RunMarket.KEYS && (mains & 2) != 0, can = !hers && tokens >= RunMarket.WARE_COSTS[ware];
                wareBuys[ware].set(left + wide - dp(18) - dp(96), y + dp(9), left + wide - dp(18), y + dp(41));
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(ink);
                fitted(canvas, RunMarket.WARE_NAMES[ware], left + dp(20), y + dp(22), dp(13.5f), wareBuys[ware].left - left - dp(28));
                text.setColor(soft);
                fitted(canvas, ware == RunMarket.KEYS ? "To begin a land with: a key leaves what it sticks in unlocked" : ware == RunMarket.TREAT ? "The pet shop gives a pet for treats"
                        : "What a smith works with", left + dp(20), y + dp(39), dp(11.5f), wareBuys[ware].left - left - dp(28));
                smithButton(canvas, wareBuys[ware], hers ? "Hers" : RunMarket.WARE_COSTS[ware] + (RunMarket.WARE_COSTS[ware] == 1 ? " token" : " tokens"), can, 0xFF6A3FA0);
                y += dp(56);
            }
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(marketSaid.isEmpty() ? soft : 0xFF2E9E6E);
            fitted(canvas, marketSaid.isEmpty() ? "Tokens are won in the arena: more the first time a rung is won, one each time after." : marketSaid, left + pad, bottom - dp(8), dp(12),
                    wide - pad * 2);
        }
        text.setTextAlign(Paint.Align.LEFT);
    }

    private Bitmap boutiqueHouse, keeperArt;

    /** Tulle at her counter, in the boutique of a village: her six pictures, one after another (she never moves between them), and a mark over her. */
    private void drawKeeper(Canvas canvas, int[] item) {
        float x = (item[1] + 1.6f - cameraX) * tile, floor = (item[2] + 1 - cameraY) * tile, feet = floor - tile * .12f;
        if (keeperArt == null) keeperArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_vanity_npc);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF8A5A4A);
        canvas.drawRect(x - tile * 1.3f, feet - tile * .5f, x + tile * 1.3f, floor, paint);
        paint.setColor(0xFFB98672);
        canvas.drawRect(x - tile * 1.35f, feet - tile * .55f, x + tile * 1.35f, feet - tile * .43f, paint);
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        cell(canvas, keeperArt, RunBoutique.frameAt(clock), 6, x, feet - tile * .5f, tile * 1.75f, false);
        mark(canvas, x, feet - tile * 2.45f, "Boutique", 0xFFE0709F, false);
    }

    /** Whoever deals in the black market, on a step at the back of it, with what she is over her head. */
    private void drawDealer(Canvas canvas, int[] item) {
        float x = (item[1] + 1.6f - cameraX) * tile, floor = (item[2] + 1 - cameraY) * tile, feet = floor - tile * .55f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF3A2C4A);
        canvas.drawRect(x - tile * 1.2f, feet, x + tile * 1.2f, floor, paint);
        paint.setColor(0xFF5A4670);
        canvas.drawRect(x - tile * 1.25f, feet, x + tile * 1.25f, feet + tile * .12f, paint);
        paint.setColor(Color.BLACK);
        Bitmap sheet = later(RunLevels.JUPAN - RunLevels.GOKHAE);
        cell(canvas, sheet, 0, 4, x, feet, tile * 1.45f * sheet.getHeight() / 188f, false);
        mark(canvas, x, feet - tile * (run.spot() == PlusRun.DEALER ? 1.95f : 1.85f), "Market", 0xFF6A3FA0, false);
    }

    // The smithy

    /** How far a thing to fight with has been worked on; what she has had made to wear, as bits; what of it she has on; and what she has of the three a smith works with. */
    int forged(int kind) { return Math.max(0, Math.min(RunArms.LEVELS, session.tally("run_w_" + kind))); }
    private int gearMade() { return session.tally("run_gear"); }
    private int gearOn() { return RunGear.sound(session.tally("run_gear_on"), gearMade()); }
    private int[] stuff() { return new int[]{session.tally("run_mat_0"), session.tally("run_mat_1"), session.tally("run_mat_2")}; }

    /** Pays a price: pluses, and of each of the three. */
    private void pay(int pluses, int[] needs) {
        session.keep("run_bank", bank() - pluses);
        for (int index = 0; index < needs.length; index++) if (needs[index] != 0) session.keep("run_mat_" + index, session.tally("run_mat_" + index) - needs[index]);
    }

    private Bitmap smith(int which) {
        if (smiths[which] == null) smiths[which] = BitmapFactory.decodeResource(context.getResources(), SMITH_ART[which]);
        return smiths[which];
    }

    /** Whether all three sisters are in the smithy she is in: where they are not, Moru does her sisters' work, and has the word for it. */
    private boolean sisters() { return smithFrom == PLAY && number >= RunLevels.SISTERS_FROM; }

    /** One of the three is walked up to: her work is looked at, and the land waits. The first time, she says who she is. From the map: only what she wears, to put on. */
    void openSmith(int who, int from) {
        int roses = session.tally("run_roses");
        if (RunCraft.roses(roses) != roses) {
            session.keep("run_roses", RunCraft.roses(roses));
            int rose = RunCraft.rose(RunLoot.KNUCKLES);
            intoBag(rose);
            toast(RunLoot.name(rose), "Every Rose made before there were knuckles: this one is yours", LOOT_PICTURE + rose);
        }
        smithOpen = true;
        smithFrom = from;
        smithTab = from == MAP ? RunGear.PULMU : who;
        smithSaid = "";
        if ((kept().pack >> forgeOn & 1) == 0) forgeOn = PlusRun.TABLET;
        goLeft = goRight = goJump = goStrike = false;
        jumpPressed = strikePressed = false;
        audio.play("page", .6f);
        int met = session.tally("run_smiths_met");
        if (from == PLAY && (met >> who & 1) == 0) {
            session.keep("run_smiths_met", met | 1 << who);
            person(RunStory.SMITHS[who * 2]);
        }
    }

    /** Something has been made: whoever made it brings her hammer down on it, and has her word. */
    private void made(int who, String what) {
        smithSaid = what;
        hammerer = sisters() ? who : RunGear.MORU;
        hammered = 0;
        regrant();
        audio.play("blow", .7f);
        audio.play("buy", .5f);
        buzz(true);
        if (smithFrom == PLAY) person(RunStory.SMITHS[hammerer * 2 + 1]);
    }

    private void smithTouch(float x, float y) {
        int bank = bank();
        int[] has = stuff();
        if (smithFrom == PLAY) {
            for (int tab = 0; tab < smithTabs.length; tab++) {
                if (!smithTabs[tab].contains(x, y)) continue;
                smithTab = tab;
                smithSaid = "";
                audio.play("tap", .5f);
                return;
            }
        }
        if (smithTab == QUESTS) {
            if (!questTouch(x, y) && !smithCard.contains(x, y)) {
                smithOpen = false;
                audio.play("tap", .3f);
            }
            return;
        }
        if (smithTab == CRAFT) {
            craftTouch(x, y, bank, has);
            if (!smithCard.contains(x, y)) {
                smithOpen = false;
                audio.play("tap", .3f);
            }
            return;
        }
        if (smithTab == RunGear.MORU) {
            int owned = kept().pack;
            for (int kind = 0; kind < RunArms.COUNT; kind++) {
                if ((owned >> kind & 1) == 0 || forgeRows[kind] == null || !forgeRows[kind].contains(x, y)) continue;
                forgeOn = kind;
                audio.play("tap", .5f);
                return;
            }
            if (!smithAct.isEmpty() && smithAct.contains(x, y)) {
                // A step of work on it: it does more from now on, here and in every land.
                int level = forged(forgeOn);
                if (level >= RunArms.LEVELS || !RunGear.affords(bank, has, RunGear.stepPluses(level + 1), RunGear.stepNeeds(level + 1))) {
                    audio.play("no", .5f);
                    return;
                }
                pay(RunGear.stepPluses(level + 1), RunGear.stepNeeds(level + 1));
                session.keep("run_w_" + forgeOn, level + 1);
                made(RunGear.MORU, RunArms.NAMES[forgeOn] + ", level " + (level + 2) + ": " + RunArms.step(forgeOn, level + 1));
                return;
            }
        } else if (smithTab == RunGear.PULMU) {
            for (int index = 0; index < gearCells.length; index++) {
                if (gearCells[index] == null || !gearCells[index].contains(x, y)) continue;
                gearSlot = index / RunGear.TIERS;
                gearTier = index % RunGear.TIERS + 1;
                audio.play("tap", .5f);
                return;
            }
            if (!smithAct.isEmpty() && smithAct.contains(x, y)) {
                int made = gearMade(), on = gearOn(), bit = RunGear.bit(gearSlot, gearTier);
                if ((made & bit) == 0) {
                    // Made, and put on there and then.
                    if (smithFrom != PLAY || !RunGear.affords(bank, has, RunGear.piecePluses(gearTier), RunGear.pieceNeeds(gearTier))) {
                        audio.play("no", .5f);
                        return;
                    }
                    pay(RunGear.piecePluses(gearTier), RunGear.pieceNeeds(gearTier));
                    session.keep("run_gear", made | bit);
                    session.keep("run_gear_on", RunGear.wear(on, gearSlot, gearTier));
                    made(RunGear.PULMU, "The " + RunGear.name(gearSlot, gearTier).toLowerCase(java.util.Locale.ROOT) + " is made, and on her");
                } else {
                    // One she has is put on, or taken off.
                    session.keep("run_gear_on", RunGear.wear(on, gearSlot, RunGear.worn(on, gearSlot) == gearTier ? 0 : gearTier));
                    smithSaid = "";
                    regrant();
                    audio.play("unlock", .5f);
                    buzz(false);
                }
                return;
            }
        } else {
            for (int index = 0; index < 3; index++) {
                if (tradeBuys[index].contains(x, y)) {
                    if (bank < RunGear.BUYS[index]) {
                        audio.play("no", .5f);
                        return;
                    }
                    pay(RunGear.BUYS[index], new int[]{index == 0 ? -1 : 0, index == 1 ? -1 : 0, index == 2 ? -1 : 0});
                    smithSaid = "Bought: 1 " + RunHunts.MATERIALS[index].toLowerCase(java.util.Locale.ROOT);
                    audio.play("buy", .6f);
                    return;
                }
                if (tradeSells[index].contains(x, y)) {
                    if (has[index] <= 0) {
                        audio.play("no", .5f);
                        return;
                    }
                    pay(-RunGear.SELLS[index], new int[]{index == 0 ? 1 : 0, index == 1 ? 1 : 0, index == 2 ? 1 : 0});
                    smithSaid = "Sold: 1 " + RunHunts.MATERIALS[index].toLowerCase(java.util.Locale.ROOT) + " for + " + RunGear.SELLS[index];
                    audio.play("buy", .5f);
                    return;
                }
            }
            for (int kind = 0; kind < RunArms.COUNT; kind++) {
                if (reforges[kind] == null || reforges[kind].isEmpty() || !reforges[kind].contains(x, y)) continue;
                // Re-forged: as it was found, and what went into it comes back.
                int level = forged(kind);
                if (level <= 0 || bank < RunGear.REFORGE) {
                    audio.play("no", .5f);
                    return;
                }
                int[] back = RunGear.spent(level);
                pay(RunGear.REFORGE, new int[]{-back[0], -back[1], -back[2]});
                session.keep("run_w_" + kind, 0);
                made(RunGear.SUTDOL, RunArms.NAMES[kind] + " is as it was found: its metal is hers again");
                return;
            }
        }
        if (!smithCard.contains(x, y)) {
            smithOpen = false;
            audio.play("tap", .3f);
        }
    }

    /** A small picture of a piece to wear, by its place: a helm, a coat, a boot, a charm on its cord. */
    private void gearIcon(Canvas canvas, int slot, float x, float y, float size, int colour) {
        // Each piece of each set has its own picture; the set is known by its colour (anything else, the best of them).
        int set = 2;
        for (int index = 0; index < SET_COLOURS.length; index++) if (SET_COLOURS[index] == colour) set = index;
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        drawIcon(canvas, SMITH_ICONS + set * RunGear.SLOTS + slot, x, y, size * 1.7f);
    }

    /** The old drawn shape of a piece, no longer used. */
    private void gearShape(Canvas canvas, int slot, float x, float y, float size, int colour) {
        float half = size / 2;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colour);
        path.reset();
        if (slot == RunGear.HELM) {
            box.set(x - half, y - half, x + half, y + half * 1.2f);
            path.addArc(box, 180, 180);
            path.lineTo(x + half * 1.15f, y + half * .1f);
            path.lineTo(x + half * 1.15f, y + half * .45f);
            path.lineTo(x - half * 1.15f, y + half * .45f);
            path.lineTo(x - half * 1.15f, y + half * .1f);
            path.close();
        } else if (slot == RunGear.COAT) {
            path.moveTo(x - half * .45f, y - half);
            path.lineTo(x + half * .45f, y - half);
            path.lineTo(x + half * 1.1f, y - half * .45f);
            path.lineTo(x + half * .8f, y + half * .05f);
            path.lineTo(x + half * .6f, y - half * .1f);
            path.lineTo(x + half * .7f, y + half);
            path.lineTo(x - half * .7f, y + half);
            path.lineTo(x - half * .6f, y - half * .1f);
            path.lineTo(x - half * .8f, y + half * .05f);
            path.lineTo(x - half * 1.1f, y - half * .45f);
            path.close();
        } else if (slot == RunGear.BOOTS) {
            path.moveTo(x - half * .6f, y - half);
            path.lineTo(x + half * .1f, y - half);
            path.lineTo(x + half * .1f, y + half * .2f);
            path.lineTo(x + half, y + half * .45f);
            path.lineTo(x + half, y + half);
            path.lineTo(x - half * .6f, y + half);
            path.close();
        } else {
            path.moveTo(x, y - half * .35f);
            path.lineTo(x + half * .7f, y + half * .3f);
            path.lineTo(x, y + half);
            path.lineTo(x - half * .7f, y + half * .3f);
            path.close();
            stroke.setColor(colour);
            stroke.setStrokeWidth(size * .1f);
            canvas.drawCircle(x, y - half * .6f, half * .32f, stroke);
        }
        canvas.drawPath(path, paint);
        stroke.setColor(mixed(colour, Color.BLACK, .4f));
        stroke.setStrokeWidth(Math.max(1, size * .06f));
        canvas.drawPath(path, stroke);
        paint.setColor(Color.BLACK);
    }

    /** A button of the smithy's: filled where it can be used, grey where it cannot. */
    void smithButton(Canvas canvas, RectF where, String word, boolean can, int colour) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(can ? colour : dark ? 0xFF2F3336 : 0xFFEDEEEE);
        canvas.drawRoundRect(where, where.height() / 2, where.height() / 2, paint);
        paint.setColor(Color.BLACK);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(can ? Color.WHITE : soft);
        fitted(canvas, word, where.centerX(), where.centerY() + dp(5), dp(13.5f), where.width() - dp(14));
    }


    // The smith sisters' quests

    /** Each quest's button on the Quests tab. */
    private final RectF[] questButtons = {new RectF(), new RectF(), new RectF(), new RectF()};

    private RunVanity.Keep keep() { return RunVanity.on(session); }

    /** A line as it is shown under the quests: who says it, and what. */
    private static String said(String[] line) {
        int who = RunStory.SPEAKERS.indexOf(line[2]);
        return (who >= 0 ? RunStory.NAMES[who] + ": " : "") + line[1];
    }

    /**
     * The Quests tab: the sisters' four quests, each with whose it is, the step she is on and how far it has come (or what it asks
     * first, or that it waits for the other three), what it gives, and a button: take it up, hand the step in, or done.
     */
    private void drawQuests(Canvas canvas, float left, float y, float wide, float pad, float bottom) {
        RunVanity.Keep save = keep();
        float each = (bottom - y - (smithSaid.isEmpty() ? 0 : dp(36))) / SmithQuests.COUNT;
        for (int quest = 0; quest < SmithQuests.COUNT; quest++) {
            float top = y + quest * each, button = dp(96);
            int state = SmithQuests.state(save, quest);
            boolean open = SmithQuests.open(save, quest), ready = SmithQuests.ready(save, quest), done = SmithQuests.done(save, quest);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setColor(done ? soft : ink);
            fitted(canvas, SmithQuests.NAMES[quest], left + pad, top + dp(18), dp(14.5f), wide - pad * 2 - button - dp(8));
            text.setTypeface(Typeface.DEFAULT);
            text.setColor(0xFF8A5A33);
            fitted(canvas, SmithQuests.GIVERS[quest], left + pad, top + dp(35), dp(12), wide - pad * 2 - button - dp(8));
            String step;
            if (done) step = "Done: every step handed in";
            else if (state == 0) step = open ? "First: " + SmithQuests.STEPS[quest][0].ask.toLowerCase(java.util.Locale.ROOT) + ", then two more" : "Once the other three are done";
            else {
                int[] now = SmithQuests.progress(save, quest);
                step = "Step " + state + " of 3: " + SmithQuests.STEPS[quest][state - 1].ask + "  ·  " + now[0] + " of " + now[1];
            }
            text.setColor(ink);
            fitted(canvas, step, left + pad, top + dp(54), dp(12.5f), wide - pad * 2);
            text.setColor(soft);
            fitted(canvas, "Gives: " + RunLoot.name(SmithQuests.REWARDS[quest]) + "  ·  + " + SmithQuests.PLUSES[quest], left + pad, top + dp(71), dp(12), wide - pad * 2);
            questButtons[quest].set(left + wide - pad - button, top + dp(6), left + wide - pad, top + dp(38));
            smithButton(canvas, questButtons[quest], done ? "Done" : state == 0 ? (open ? "Take it up" : "Later") : ready ? "Hand in" : "Not yet",
                    !done && (state == 0 ? open : ready), CalculatorLayout.CORAL);
            if (quest < SmithQuests.COUNT - 1) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(dark ? 0xFF33373B : 0xFFECEDEE);
                canvas.drawRect(left + pad, top + each - dp(1), left + wide - pad, top + each, paint);
                paint.setColor(Color.BLACK);
            }
        }
        if (!smithSaid.isEmpty()) {
            // What was last said about them, under them.
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(0xFF8A5A33);
            text.setTextSize(dp(12.5f));
            List<String> lines = wrap(smithSaid, wide - pad * 2);
            for (int index = 0; index < Math.min(2, lines.size()); index++) canvas.drawText(lines.get(index), left + pad, y + SmithQuests.COUNT * each + dp(14) + index * dp(16), text);
        }
    }

    /** A touch on the Quests tab: a quest taken up, or a step handed in, with whoever's quest it is saying so. Returns whether it was on a button. */
    private boolean questTouch(float x, float y) {
        RunVanity.Keep save = keep();
        for (int quest = 0; quest < SmithQuests.COUNT; quest++) {
            if (!questButtons[quest].contains(x, y)) continue;
            int state = SmithQuests.state(save, quest);
            if (state == 0 && SmithQuests.take(save, quest)) {
                audio.play("unlock", .6f);
                person(SmithQuests.line(quest, 0));
                talk.add(SmithQuests.line(quest, 1));
                smithSaid = said(SmithQuests.line(quest, 0));
                return true;
            }
            int handed = SmithQuests.hand(save, quest);
            if (handed == SmithQuests.STEP) {
                audio.play("buy", .6f);
                buzz(true);
                person(SmithQuests.line(quest, state == 1 ? 2 : 3));
                smithSaid = said(SmithQuests.line(quest, state == 1 ? 2 : 3));
                regrant();
                return true;
            }
            if (handed == SmithQuests.FINISHED) {
                int reward = SmithQuests.REWARDS[quest];
                intoBag(reward);
                hammerer = quest == SmithQuests.TOGETHER ? RunGear.MORU : quest;
                hammered = 0;
                audio.play("win", .8f);
                buzz(true);
                person(SmithQuests.line(quest, 4));
                smithSaid = said(SmithQuests.line(quest, 4));
                toast(RunLoot.RARITIES[RunLoot.rarity(reward)] + ": " + RunLoot.name(reward), "From " + (quest == SmithQuests.TOGETHER ? "all three sisters" : SmithQuests.GIVERS[quest]) + ", in her bag",
                        LOOT_PICTURE + reward);
                regrant();
                return true;
            }
            audio.play("no", .5f);
            return true;
        }
        return false;
    }


    // How places under the ground look

    /** The squares of the dungeons by band (tops, then fills), and of the lagoons and the cellars (seabed, sea rock, cellar floor; sand, rock, brick). */
    private Bitmap depthSquares, roomSquares, lagoonBack, cellarBack;
    private final Bitmap[] deeps = new Bitmap[4];
    private Bitmap abyss;
    private static final int[] DEEPS = {R.drawable.run_depths_deep_catacomb, R.drawable.run_depths_deep_inkcrypt, R.drawable.run_depths_deep_papercave,
            R.drawable.run_depths_deep_vault};
    private static final int SEABED = 0, SEA_ROCK_TOP = 1, CELLAR_FLOOR = 2, SAND = 3, SEA_ROCK = 4, BRICK = 5;
    /** How each band of a dungeon is lit: a cool dusk, a grimy green murk, a violet dark. */
    private static final int[] BAND_TINT = {0xFF0E0C22, 0xFF0C1610, 0xFF140820};
    /** Water as it gets deeper: pale and clear at the top, deep blue below. */
    private android.graphics.LinearGradient waterShade, lagoonShade;
    private final android.graphics.Matrix waterMatrix = new android.graphics.Matrix();

    /** Which kind of place she is in, off the land: a lagoon, a hideout or a cellar of waves, which have pictures of their own; or -1. */
    private int roomKind() {
        if (run == null || run.room() < 0) return -1;
        return run.level().rooms.get(run.room())[0];
    }

    /** Whether the place she is in is one of those under the ground (a hideout, or a cellar of waves): a basement. */
    private boolean basement() { int kind = roomKind(); return kind == RunLevels.HIDEOUT || kind == RunLevels.ARENA; }

    /** One square of a sheet of squares, drawn over a square of the land, turned about every other one so the same one side by side shows no pattern. */
    private void groundSquare(Canvas canvas, Bitmap sheet, int which, float left, float y, int column, int row, boolean capped) {
        int square = sheet.getHeight();
        source.set(which * square + 1, 1, which * square + square - 1, square - 1);
        target.set(left - 1, y - 1, left + tile + 1, y + tile + 1);
        canvas.save();
        canvas.scale(column % 2 == 0 ? 1 : -1, capped || row % 2 == 0 ? 1 : -1, left + tile / 2, y + tile / 2);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
    }

    /**
     * A square of rock in a place under the ground or in a dungeon, if it has squares of its own: a lagoon's seabed and sea rock, a basement's
     * floor and brick, a dungeon's stone of its band. Returns whether it drew it.
     */
    private boolean underSquare(Canvas canvas, int column, int row, float left, float y) {
        boolean capped = run.shown(column, row - 1) != RunLevels.BLOCK;
        int kind = roomKind();
        if (kind == RunLevels.LAGOON || kind == RunLevels.HIDEOUT || kind == RunLevels.ARENA) {
            if (roomSquares == null) roomSquares = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_ground_rooms);
            int[] in = run.level().rooms.get(run.room());
            int which;
            if (kind == RunLevels.LAGOON) which = capped ? (row >= in[11] ? SEABED : SEA_ROCK_TOP) : row >= in[11] ? SAND : SEA_ROCK;
            else which = capped ? CELLAR_FLOOR : BRICK;
            groundSquare(canvas, roomSquares, which, left, y, column, row, capped);
            return true;
        }
        if (delve >= 0 && run.room() < 0) {
            if (depthSquares == null) depthSquares = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_ground_depths);
            int band = RunDungeons.band(delveFloor);
            groundSquare(canvas, depthSquares, capped ? band : RunDungeons.BANDS + band, left, y, column, row, capped);
            return true;
        }
        return false;
    }

    /** A place's own picture behind it, where it has one: under the sea for a lagoon, a basement for a hideout. Returns whether it drew one. */
    private boolean drawRoomBack(Canvas canvas) {
        int kind = roomKind();
        Bitmap back;
        if (kind == RunLevels.LAGOON) {
            if (lagoonBack == null) lagoonBack = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_room_lagoon);
            back = lagoonBack;
        } else if (kind == RunLevels.HIDEOUT) {
            if (cellarBack == null) cellarBack = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_room_cellar);
            back = cellarBack;
        } else {
            return false;
        }
        float tall = height, wide = tall * back.getWidth() / back.getHeight(), drift = cameraX * tile * .2f % (wide * 2);
        paint.setAlpha(255);
        for (int copy = 0; copy * wide - drift < width; copy++) {
            float left = copy * wide - drift;
            target.set(left - 1, 0, left + wide + 1, tall);
            canvas.save();
            if (copy % 2 != 0) canvas.scale(-1, 1, left + wide / 2, 0);
            canvas.drawBitmap(back, null, target, paint);
            canvas.restore();
        }
        return true;
    }

    /**
     * Deep water, drawn a run of it down each column at a time, with no squares to be seen: pale and clear at its top, deeper blue the
     * further down, and its top line moving. Drawn from the top square of each run that is in view; the rest of the run is drawn with it.
     */
    private void drawWater(Canvas canvas, int column, int row, int bottom, float left) {
        if (run.at(column, row - 1) == RunLevels.WATER && row > (int) Math.floor(cameraY)) return;
        int surface = row;
        while (surface > 0 && run.at(column, surface - 1) == RunLevels.WATER) surface--;
        int end = row;
        while (end + 1 <= bottom && run.at(column, end + 1) == RunLevels.WATER) end++;
        if (waterShade == null) {
            waterShade = new android.graphics.LinearGradient(0, 0, 0, 1, new int[]{0x9070C8EC, 0xB0389CD8, 0xD01E5EA8, 0xE0103C78},
                    new float[]{0, .18f, .55f, 1}, android.graphics.Shader.TileMode.CLAMP);
        }
        // (In a lagoon, whose picture behind is already under the sea, the water itself is lighter.)
        if (lagoonShade == null) {
            lagoonShade = new android.graphics.LinearGradient(0, 0, 0, 1, new int[]{0x5088D8F4, 0x5848A8DC, 0x702466AE, 0x88124484},
                    new float[]{0, .18f, .55f, 1}, android.graphics.Shader.TileMode.CLAMP);
        }
        android.graphics.LinearGradient shade = roomKind() == RunLevels.LAGOON ? lagoonShade : waterShade;
        float top = (surface - cameraY) * tile, from = (row - cameraY) * tile, to = (end + 1 - cameraY) * tile;
        waterMatrix.setScale(1, tile * 11);
        waterMatrix.postTranslate(0, top);
        shade.setLocalMatrix(waterMatrix);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(shade);
        paint.setAlpha(255);
        // (Column against column with no overlap: where two half-clear columns overlapped, a line would show.)
        canvas.drawRect(Math.round(left), from, Math.round(left + tile), to, paint);
        paint.setShader(null);
        if (row == surface) {
            // Its top: a light line that rises and falls.
            float wave = (float) Math.sin(clock * 2.6f + column * .9f) * tile * .05f;
            paint.setColor(0xCCE6F8FF);
            canvas.drawRect(Math.round(left), from + wave, Math.round(left + tile), from + wave + tile * .07f, paint);
            paint.setColor(0x55FFFFFF);
            canvas.drawRect(Math.round(left), from + wave + tile * .07f, Math.round(left + tile), from + wave + tile * .16f, paint);
        }
        paint.setColor(Color.BLACK);
    }

    /** Over a lagoon: light coming down through the water in slanting rays; over a basement: dust drifting in the lantern light. */
    private void drawRoomOver(Canvas canvas) {
        int kind = roomKind();
        if (kind == RunLevels.LAGOON) {
            int[] in = run.level().rooms.get(run.room());
            float waterTop = (in[12] + 3 - cameraY) * tile, floor = (in[11] - cameraY) * tile;
            paint.setStyle(Paint.Style.FILL);
            for (int ray = 0; ray < 6; ray++) {
                float at = ((ray * 7.3f + in[3] + 6) - cameraX) * tile + (float) Math.sin(clock * .6f + ray) * tile * .4f, wide = tile * (.6f + ray % 3 * .35f);
                path.reset();
                path.moveTo(at, waterTop);
                path.lineTo(at + wide, waterTop);
                path.lineTo(at + wide + tile * 2.2f, floor);
                path.lineTo(at + tile * 1.6f, floor);
                path.close();
                paint.setColor(Color.argb(Math.round(26 + 14 * (float) Math.sin(clock * 1.3f + ray * 2)), 220, 245, 255));
                canvas.drawPath(path, paint);
            }
            // Little lights on the seabed where the rays come down, moving.
            for (int glint = 0; glint < 14; glint++) {
                float x = ((in[3] + 6 + glint * 2.6f) - cameraX) * tile + (float) Math.sin(clock * 1.7f + glint) * tile * .3f;
                paint.setColor(Color.argb(Math.round(60 + 40 * (float) Math.sin(clock * 3 + glint)), 230, 250, 255));
                canvas.drawOval(x - tile * .25f, floor - tile * .06f, x + tile * .25f, floor + tile * .02f, paint);
            }
            paint.setColor(Color.BLACK);
        } else if (basement()) {
            paint.setStyle(Paint.Style.FILL);
            for (int mote = 0; mote < 22; mote++) {
                float drift = (clock * (.05f + mote % 5 * .015f) + mote * .137f) % 1;
                float x = (mote * 97 % 100) / 100f * width + (float) Math.sin(clock * .7f + mote) * tile * .5f, y = height * (1 - drift);
                paint.setColor(Color.argb(Math.round(110 * (float) Math.sin(drift * Math.PI)), 255, 236, 200));
                canvas.drawCircle(x, y, tile * (.025f + mote % 3 * .012f), paint);
            }
            paint.setColor(Color.BLACK);
        }
    }

    /** The backdrop of a dungeon's floor, by its band: its own picture for the first five floors, a grittier one for the next five, the abyss deeper. */
    private Bitmap depthsBack() {
        int band = RunDungeons.band(delveFloor), which = RunDungeons.backdrop(delve);
        if (band == 0) {
            if (depths[which] == null) depths[which] = BitmapFactory.decodeResource(context.getResources(), DEPTHS[which]);
            return depths[which];
        }
        if (band == 1) {
            if (deeps[which] == null) deeps[which] = BitmapFactory.decodeResource(context.getResources(), DEEPS[which]);
            return deeps[which];
        }
        if (abyss == null) abyss = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_depths_abyss);
        return abyss;
    }

    /**
     * The smithy, over the land. At its head what she has to pay with: her pluses, and the
     * three a smith works with. Then the three sisters' work, a tab each. Moru's: every
     * thing to fight with that she has, with how far it has been worked on, and for the one
     * looked at what the next step adds, what it costs, and a button that has it done.
     * Pulmu's: the four pieces in their three sets, and for the one looked at what it does,
     * what it costs, and a button that has it made, puts it on or takes it off. Sutdol's:
     * the three a smith works with, bought and sold; and every thing that has been worked
     * on, to be re-forged. From the map there is only what she wears, to put on.
     */
    private void drawSmithy(Canvas canvas) {
        canvas.drawColor(0x99000000);
        int bank = bank();
        int[] has = stuff();
        boolean here = smithFrom == PLAY;
        float wide = Math.min(safe.width() - dp(24), dp(430)), pad = dp(16);
        float body = dp(here ? 424 + (smithTab == RunGear.MORU ? 30 * Math.max(0, Integer.bitCount(kept().pack) - 8) : 0) : 330);
        float tall = pad + dp(here ? 92 : 52) + body + pad, left = safe.centerX() - wide / 2, top = Math.max(safe.top + dp(8), safe.centerY() - tall / 2);
        smithCard.set(left, top, left + wide, top + tall);
        plate(canvas, smithCard, dp(22), 255);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, here ? "The smithy" : "What she wears", left + pad, top + pad + dp(18), dp(20), wide * .5f);
        text.setTypeface(Typeface.DEFAULT);
        text.setTextAlign(Paint.Align.RIGHT);
        text.setColor(CalculatorLayout.CORAL);
        fitted(canvas, "+ " + bank, left + wide - pad, top + pad + dp(17), dp(15), wide * .3f);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(soft);
        fitted(canvas, has[0] + " type metal  ·  " + has[1] + " ink crystal  ·  " + has[2] + " gold leaf" + (here ? "  ·  hunts pay them" : ""), left + pad, top + pad + dp(38), dp(12),
                wide - pad * 2);
        float y = top + pad + dp(50);
        if (here) {
            // The three sisters' work: a tab each, with whose it is where all three are in; and there, their quests.
            int tabs = sisters() ? 5 : 4;
            if (!sisters() && smithTab == QUESTS) smithTab = RunGear.MORU;
            smithTabs[4].setEmpty();
            float each = (wide - pad * 2 - dp(6) * (tabs - 1)) / tabs;
            String[] words = sisters() ? new String[]{"Moru", "Pulmu", "Sutdol", "Craft", "Quests"} : new String[]{"Forge", "Armour", "Trade", "Craft"};
            for (int tab = 0; tab < tabs; tab++) {
                smithTabs[tab].set(left + pad + tab * (each + dp(6)), y, left + pad + tab * (each + dp(6)) + each, y + dp(32));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(tab == smithTab ? CalculatorLayout.CORAL : dark ? 0xFF2C3033 : 0xFFF0F1F2);
                canvas.drawRoundRect(smithTabs[tab], dp(16), dp(16), paint);
                paint.setColor(Color.BLACK);
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(tab == smithTab ? Color.WHITE : ink);
                fitted(canvas, words[tab], smithTabs[tab].centerX(), smithTabs[tab].centerY() + dp(4.5f), dp(13), each - dp(10));
            }
            y += dp(40);
        }
        float bottom = top + tall - pad;
        smithAct.setEmpty();
        if (smithTab == QUESTS) {
            drawQuests(canvas, left, y, wide, pad, bottom);
        } else if (smithTab == CRAFT) {
            drawCraft(canvas, left, y, wide, pad, bottom, bank, has);
        } else if (smithTab == RunGear.MORU) {
            int owned = kept().pack, count = Integer.bitCount(owned);
            float row = Math.max(dp(30), Math.min(dp(38), (body - dp(150)) / Math.max(1, count)));
            for (int kind = 0; kind < RunArms.COUNT; kind++) {
                if ((owned >> kind & 1) == 0) {
                    forgeRows[kind] = null;
                    continue;
                }
                if (forgeRows[kind] == null) forgeRows[kind] = new RectF();
                RectF at = forgeRows[kind];
                at.set(left + dp(8), y, left + wide - dp(8), y + row - dp(3));
                boolean on = kind == forgeOn;
                int level = forged(kind);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(on ? (dark ? 0xFF44292E : 0xFFFCE9EB) : dark ? 0xFF2C3033 : 0xFFF5F6F7);
                canvas.drawRoundRect(at, dp(12), dp(12), paint);
                paint.setColor(Color.BLACK);
                boolean upright = kind != PlusRun.GUN && kind != PlusRun.BOOK && kind < PlusRun.TABLET;
                thing(canvas, kind, at.left + dp(24), at.centerY(), at.height() * (upright ? .8f : .62f), upright ? 35 : 0, false, false);
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(ink);
                text.setTypeface(on ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                fitted(canvas, RunArms.NAMES[kind], at.left + dp(48), at.centerY() + dp(5), dp(14), at.width() * .45f);
                text.setTypeface(Typeface.DEFAULT);
                // How far it has been worked on: a mark for each step, filled as far as it has got.
                for (int step = 0; step < RunArms.LEVELS; step++) {
                    float cx = at.right - dp(74) + step * dp(13), cy = at.centerY();
                    paint.setColor(step < level ? CalculatorLayout.CORAL : dark ? 0xFF3A3F45 : 0xFFDFE2E4);
                    canvas.drawRoundRect(cx - dp(4.5f), cy - dp(4.5f), cx + dp(4.5f), cy + dp(4.5f), dp(2), dp(2), paint);
                }
                paint.setColor(Color.BLACK);
                text.setTextAlign(Paint.Align.RIGHT);
                text.setColor(level > 0 ? CalculatorLayout.CORAL : soft);
                fitted(canvas, "lv " + (level + 1), at.right - dp(10), at.centerY() + dp(4.5f), dp(12), dp(30));
                y += row;
            }
            // The one looked at: what the next step makes of it, and what the step costs.
            int kind = forgeOn, level = forged(kind);
            y += dp(8);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, RunArms.NAMES[kind] + "  ·  level " + (level + 1) + "  ·  a blow counts for " + RunArms.blows(kind, level), left + pad, y + dp(14), dp(14.5f), wide - pad * 2);
            text.setTypeface(Typeface.DEFAULT);
            if (level >= RunArms.LEVELS) {
                text.setColor(soft);
                fitted(canvas, "It is as far as a smith can take it.", left + pad, y + dp(36), dp(12.5f), wide - pad * 2);
            } else {
                int pluses = RunGear.stepPluses(level + 1);
                int[] needs = RunGear.stepNeeds(level + 1);
                boolean can = RunGear.affords(bank, has, pluses, needs);
                text.setColor(ink);
                text.setTextSize(dp(12.5f));
                List<String> lines = wrap("Level " + (level + 2) + ": " + RunArms.step(kind, level + 1)
                        + (RunArms.blows(kind, level + 1) > RunArms.blows(kind, level) ? " A blow counts for " + RunArms.blows(kind, level + 1) + "." : ""), wide - pad * 2);
                for (int index = 0; index < Math.min(2, lines.size()); index++) canvas.drawText(lines.get(index), left + pad, y + dp(36) + index * dp(16.5f), text);
                smithAct.set(left + pad, bottom - dp(40), left + wide - pad, bottom);
                smithButton(canvas, smithAct, (can ? "Forge  " : "Not enough for  ") + RunGear.price(pluses, needs), can, CalculatorLayout.CORAL);
            }
            if (!smithSaid.isEmpty()) {
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(0xFF2E9E6E);
                fitted(canvas, smithSaid, left + pad, bottom - dp(52), dp(12.5f), wide - pad * 2);
            }
        } else if (smithTab == RunGear.PULMU) {
            int made = gearMade(), on = gearOn();
            float label = dp(58), gap = dp(6), cellWide = (wide - pad * 2 - label - gap * 3) / 3, cellTall = dp(46);
            // The three sets, across; and the four pieces, down.
            text.setTextAlign(Paint.Align.CENTER);
            for (int tier = 1; tier <= RunGear.TIERS; tier++) {
                text.setColor(soft);
                fitted(canvas, RunGear.SET_NAMES[tier - 1], left + pad + label + gap + (tier - 1) * (cellWide + gap) + cellWide / 2, y + dp(12), dp(12), cellWide);
            }
            y += dp(20);
            for (int slot = 0; slot < RunGear.SLOTS; slot++) {
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(ink);
                fitted(canvas, RunGear.SLOT_NAMES[slot], left + pad, y + cellTall / 2 + dp(5), dp(13.5f), label);
                for (int tier = 1; tier <= RunGear.TIERS; tier++) {
                    int index = slot * RunGear.TIERS + tier - 1;
                    if (gearCells[index] == null) gearCells[index] = new RectF();
                    RectF at = gearCells[index];
                    float x = left + pad + label + gap + (tier - 1) * (cellWide + gap);
                    at.set(x, y, x + cellWide, y + cellTall);
                    boolean hers = (made & RunGear.bit(slot, tier)) != 0, worn = RunGear.worn(on, slot) == tier, looked = slot == gearSlot && tier == gearTier;
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(worn ? (dark ? 0xFF44292E : 0xFFFCE9EB) : dark ? 0xFF2C3033 : 0xFFF5F6F7);
                    canvas.drawRoundRect(at, dp(12), dp(12), paint);
                    if (looked || worn) {
                        stroke.setColor(worn ? CalculatorLayout.CORAL : dark ? 0xFF9AA0A6 : 0xFF8A8F94);
                        stroke.setStrokeWidth(dp(worn ? 2.5f : 1.5f));
                        canvas.drawRoundRect(at, dp(12), dp(12), stroke);
                    }
                    paint.setAlpha(hers ? 255 : 110);
                    gearIcon(canvas, slot, at.left + dp(20), at.centerY(), dp(20), hers ? SET_COLOURS[tier - 1] : mixed(SET_COLOURS[tier - 1], dark ? 0xFF2C3033 : 0xFFF5F6F7, .55f));
                    paint.setAlpha(255);
                    text.setTextAlign(Paint.Align.LEFT);
                    text.setColor(worn ? CalculatorLayout.CORAL : hers ? ink : soft);
                    fitted(canvas, worn ? "on her" : hers ? "made" : "+ " + RunGear.piecePluses(tier), at.left + dp(38), at.centerY() + dp(4.5f), dp(12), cellWide - dp(44));
                }
                y += cellTall + gap;
            }
            // The one looked at: what it does, and what it costs.
            boolean hers = (made & RunGear.bit(gearSlot, gearTier)) != 0, worn = RunGear.worn(on, gearSlot) == gearTier;
            y += dp(4);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, RunGear.name(gearSlot, gearTier), left + pad, y + dp(14), dp(14.5f), wide - pad * 2);
            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp(12.5f));
            List<String> lines = wrap(RunGear.does(gearSlot, gearTier) + " All four of one set: a heart more" + (RunGear.whole(on) ? ", which she has." : "."), wide - pad * 2);
            for (int index = 0; index < Math.min(3, lines.size()); index++) canvas.drawText(lines.get(index), left + pad, y + dp(34) + index * dp(16.5f), text);
            smithAct.set(left + pad, bottom - dp(40), left + wide - pad, bottom);
            if (hers) {
                smithButton(canvas, smithAct, worn ? "Take it off" : "Put it on", true, worn ? 0xFF7A8088 : CalculatorLayout.CORAL);
            } else if (here) {
                boolean can = RunGear.affords(bank, has, RunGear.piecePluses(gearTier), RunGear.pieceNeeds(gearTier));
                smithButton(canvas, smithAct, (can ? "Make  " : "Not enough for  ") + RunGear.price(RunGear.piecePluses(gearTier), RunGear.pieceNeeds(gearTier)), can, CalculatorLayout.CORAL);
            } else {
                smithAct.setEmpty();
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(soft);
                fitted(canvas, "Made at a smithy, in a village: " + RunGear.price(RunGear.piecePluses(gearTier), RunGear.pieceNeeds(gearTier)), left + wide / 2, bottom - dp(14), dp(12.5f), wide - pad * 2);
            }
            if (!smithSaid.isEmpty()) {
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(0xFF2E9E6E);
                fitted(canvas, smithSaid, left + pad, bottom - dp(52), dp(12.5f), wide - pad * 2);
            }
        } else {
            // What a smith works with, bought and sold.
            for (int index = 0; index < 3; index++) {
                box.set(left + dp(8), y, left + wide - dp(8), y + dp(44));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(dark ? 0xFF2C3033 : 0xFFF5F6F7);
                canvas.drawRoundRect(box, dp(12), dp(12), paint);
                paint.setColor(index == 0 ? 0xFF8F979E : index == 1 ? 0xFF3F5FD0 : 0xFFE0A21A);
                canvas.drawRoundRect(left + dp(20), y + dp(14), left + dp(36), y + dp(30), dp(index == 1 ? 8 : 3), dp(index == 1 ? 8 : 3), paint);
                paint.setColor(Color.BLACK);
                tradeSells[index].set(left + wide - dp(18) - dp(68), y + dp(7), left + wide - dp(18), y + dp(37));
                tradeBuys[index].set(tradeSells[index].left - dp(6) - dp(76), y + dp(7), tradeSells[index].left - dp(6), y + dp(37));
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(ink);
                fitted(canvas, RunHunts.MATERIALS[index], left + dp(46), y + dp(20), dp(13.5f), tradeBuys[index].left - left - dp(52));
                text.setColor(soft);
                fitted(canvas, "she has " + has[index], left + dp(46), y + dp(36), dp(11.5f), tradeBuys[index].left - left - dp(52));
                smithButton(canvas, tradeBuys[index], "Buy + " + RunGear.BUYS[index], bank >= RunGear.BUYS[index], CalculatorLayout.CORAL);
                smithButton(canvas, tradeSells[index], "Sell + " + RunGear.SELLS[index], has[index] > 0, 0xFF2E9E6E);
                y += dp(50);
            }
            // And her sisters' work undone: a thing re-forged is as it was found, and what went into it comes back.
            y += dp(6);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, "Re-forge  ·  + " + RunGear.REFORGE, left + pad, y + dp(14), dp(14.5f), wide - pad * 2);
            text.setTypeface(Typeface.DEFAULT);
            text.setColor(soft);
            text.setTextSize(dp(12));
            List<String> what = wrap("A thing re-forged is as it was found, and what went into it comes back, to spend on another.", wide - pad * 2);
            for (int index = 0; index < Math.min(2, what.size()); index++) canvas.drawText(what.get(index), left + pad, y + dp(32) + index * dp(15.5f), text);
            y += dp(42) + (what.size() > 1 ? dp(15.5f) : 0);
            boolean any = false;
            for (int kind = 0; kind < RunArms.COUNT; kind++) {
                if (reforges[kind] == null) reforges[kind] = new RectF();
                reforges[kind].setEmpty();
                int level = forged(kind);
                if (level <= 0 || y + dp(34) > bottom - dp(20)) continue;
                any = true;
                int[] back = RunGear.spent(level);
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(ink);
                fitted(canvas, RunArms.NAMES[kind] + ", level " + (level + 1) + ": " + back[0] + " metal, " + back[1] + " crystal, " + back[2] + " leaf back", left + pad, y + dp(21), dp(12.5f),
                        wide - pad * 2 - dp(96));
                reforges[kind].set(left + wide - pad - dp(88), y + dp(2), left + wide - pad, y + dp(30));
                smithButton(canvas, reforges[kind], "Re-forge", bank >= RunGear.REFORGE, 0xFF7A4B8A);
                y += dp(34);
            }
            if (!any) {
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(soft);
                fitted(canvas, "Nothing of hers has been worked on yet.", left + pad, y + dp(18), dp(12.5f), wide - pad * 2);
            }
            if (!smithSaid.isEmpty()) {
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(0xFF2E9E6E);
                fitted(canvas, smithSaid, left + pad, bottom - dp(6), dp(12.5f), wide - pad * 2);
            }
        }
        text.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * One of the three who keep the smithy, at her anvil: at ease; her hammer up and down,
     * now and then, as she works; hard at it for a moment when she has made something, and
     * then pleased with it. Over her head, what she does.
     */
    private void drawSmith(Canvas canvas, int[] item) {
        int which = Math.max(0, Math.min(2, item[4] - PlusRun.SMITH));
        float x = (item[1] + 2.3f - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile, anvil = x - tile * 1.05f;
        // Her anvil on its stump, with its brazier and its bucket, in front of her: on the floor, which inside a building is kept clear of the buttons.
        furniture();
        standing(canvas, anvilSmith, anvil, feet + tile * .03f, tile * 1.45f);
        // What she is at: making something, pleased with it, or now and then a few blows by herself.
        boolean making = hammerer == which && hammered < 1.6f, pleased = hammerer == which && hammered >= 1.6f && hammered < 3.2f;
        boolean idle = (int) (clock * .45f + which * 1.7f) % 5 == 0;
        int frame = pleased ? 3 : making ? ((int) (hammered * 7) % 2 == 0 ? 1 : 2) : idle ? ((int) (clock * 3.2f) % 2 == 0 ? 1 : 2) : 0;
        Bitmap sheet = smith(which);
        if (frame == 0) cell(canvas, idleSheet(IDLE_SMITHS[which]), idleFrame(6000 + which), IDLE_FRAMES, x, feet, tile * 1.3f * sheet.getHeight() / 150f, false);
        else cell(canvas, sheet, frame, 4, x, feet, tile * 1.3f * sheet.getHeight() / 150f, false);
        boolean at = run.spot() == item[4];
        mark(canvas, x, feet - tile * (at ? 1.75f : 1.65f), which == 0 ? "Forge" : which == 1 ? "Armour" : "Trade", SPEAKER_COLOURS[which == 0 ? 18 : FIRST_SMITH + which - 1], false);
    }

    /**
     * What the smith's work leaves in the air: a burst of ink where a pen ended or a blot
     * splashed, of light where a page turned; and the pages of a book, burning for a while
     * where it burst.
     */
    private void drawSmithWork(Canvas canvas) {
        for (float[] boom : run.booms()) {
            int by = (int) boom[3];
            // (A book's own burst is drawn where a book bursts.)
            if (by != PlusRun.PEN && by != PlusRun.GUN && by != PlusRun.PAGE && by != PlusRun.LANCED && by != PlusRun.SHELLED) continue;
            float out = Math.min(1, boom[4] / .4f), x = (boom[0] - cameraX) * tile, y = (boom[1] - cameraY) * tile, round = boom[2] * tile;
            int colour = by == PlusRun.PAGE ? 0xFF3FD8C8 : 0xFF2A3FC8;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colour);
            paint.setAlpha(Math.round(120 * (1 - out)));
            canvas.drawCircle(x, y, round * (.4f + .6f * out), paint);
            stroke.setColor(colour);
            stroke.setAlpha(Math.round(255 * (1 - out)));
            stroke.setStrokeWidth(tile * .09f);
            canvas.drawCircle(x, y, round * out, stroke);
            // Drops of it, flung out.
            paint.setAlpha(Math.round(230 * (1 - out)));
            for (int drop = 0; drop < 7; drop++) {
                double turn = drop * Math.PI * 2 / 7 + boom[0];
                canvas.drawCircle(x + (float) Math.cos(turn) * round * out * 1.05f, y + (float) Math.sin(turn) * round * out * 1.05f, tile * .07f, paint);
            }
            stroke.setAlpha(255);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
        for (float[] fire : run.fires()) {
            float x = (fire[0] - cameraX) * tile, y = (fire[1] - cameraY) * tile, round = fire[2] * tile, fade = Math.min(1, fire[3] / .4f);
            paint.setStyle(Paint.Style.FILL);
            for (int flame = 0; flame < 9; flame++) {
                float across = (flame - 4) / 4f * round * .9f, lick = (float) Math.abs(Math.sin(clock * 9 + flame * 1.9f));
                float high = tile * (.25f + .45f * lick), base = y + round * .25f - (float) Math.abs(across) * .2f;
                if (fire.length > 5) {
                    // (The cannon's ink, lying where it burst: it bubbles.)
                    paint.setColor(0xFF1B2A8C);
                    paint.setAlpha(Math.round(200 * fade));
                    canvas.drawOval(x + across - tile * .2f, base - high * .35f, x + across + tile * .2f, base, paint);
                    paint.setColor(0xFF4F6BFF);
                    paint.setAlpha(Math.round(220 * fade));
                    canvas.drawCircle(x + across, base - high * .5f * lick, tile * .05f, paint);
                    continue;
                }
                paint.setColor(0xFFE8622A);
                paint.setAlpha(Math.round(190 * fade));
                canvas.drawOval(x + across - tile * .14f, base - high, x + across + tile * .14f, base, paint);
                paint.setColor(0xFFFFD25A);
                paint.setAlpha(Math.round(220 * fade));
                canvas.drawOval(x + across - tile * .07f, base - high * .6f, x + across + tile * .07f, base, paint);
            }
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
    }

    /**
     * What the four won in the black arena do, as it is done. The katana: a white crescent
     * as far as the cut reaches, wider and gold-edged for the third of three; the line of a
     * dash, thin until the beat is up and then bright, with a cut across everything that
     * stood on it; and a stroke straight down under her. The lance: a streak of ink as far
     * as it thrusts. The whip: its ribbon, out to where it reaches; and, spun, a ring of it
     * about her.
     */
    private void drawArenaArms(Canvas canvas) {
        float x = (doneX - cameraX) * tile, feet = (doneY + PlusRun.TALL - cameraY) * tile, way = doneLeft ? -1 : 1;
        if (cutAge < .18f) {
            float out = cutAge / .18f, reach = (RunArms.reach(PlusRun.KATANA, perks.levels[PlusRun.KATANA]) + (cutChain == 3 ? RunArms.FINISH_FAR : 0)) * tile;
            float middle = feet - tile * .75f;
            for (int layer = 0; layer < 2; layer++) {
                box.set(x - reach * .15f, middle - tile * (.75f + layer * .12f), x + reach * (way > 0 ? 1.05f : .15f) + (way > 0 ? 0 : 0), middle + tile * (.6f + layer * .12f));
                if (way < 0) box.set(x - reach * 1.05f, box.top, x + reach * .15f, box.bottom);
                stroke.setColor(layer == 0 ? Color.WHITE : cutChain == 3 ? 0xFFFFD25A : 0xFFB9C4D2);
                stroke.setAlpha(Math.round((layer == 0 ? 255 : 150) * (1 - out)));
                stroke.setStrokeWidth(tile * (layer == 0 ? .09f : .2f) * (cutChain == 3 ? 1.5f : 1));
                float sweep = 140 * Math.min(1, out * 2.5f);
                canvas.drawArc(box, way > 0 ? -70 : 250 - sweep, sweep, false, stroke);
            }
            stroke.setAlpha(255);
        }
        if (airAge < .2f) {
            float out = airAge / .2f;
            stroke.setColor(Color.WHITE);
            stroke.setAlpha(Math.round(255 * (1 - out)));
            stroke.setStrokeWidth(tile * .12f);
            float px = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, py = (run.y() + PlusRun.TALL - cameraY) * tile;
            canvas.drawLine(px, py - tile * .4f, px, py + tile * 1.6f * Math.min(1, out * 3), stroke);
            stroke.setAlpha(255);
        }
        if (dashAge < RunArms.BEAT + .22f) {
            float from = (run.dashFrom() - cameraX) * tile, to = (run.dashTo() - cameraX) * tile, y = (run.dashY() + PlusRun.TALL * .5f - cameraY) * tile;
            boolean cut = dashAge >= RunArms.BEAT;
            float fade = cut ? 1 - (dashAge - RunArms.BEAT) / .22f : .5f + .5f * dashAge / RunArms.BEAT;
            stroke.setColor(Color.WHITE);
            stroke.setAlpha(Math.round(255 * fade));
            stroke.setStrokeWidth(tile * (cut ? .22f : .05f));
            canvas.drawLine(from, y, to, y, stroke);
            if (cut) {
                stroke.setColor(0xFFB9C4D2);
                stroke.setAlpha(Math.round(200 * fade));
                stroke.setStrokeWidth(tile * .5f);
                canvas.drawLine(from, y, to, y, stroke);
            }
            stroke.setAlpha(255);
        }
        if (thrustAge < .14f) {
            float out = thrustAge / .14f, reach = RunArms.reach(PlusRun.LANCE, perks.levels[PlusRun.LANCE]) * tile, y = feet - tile * .62f;
            stroke.setColor(0xFF2A3FC8);
            stroke.setAlpha(Math.round(220 * (1 - out)));
            stroke.setStrokeWidth(tile * .14f);
            canvas.drawLine(x + way * tile * .6f, y, x + way * (tile * .35f + reach * Math.min(1, out * 3)), y, stroke);
            stroke.setColor(Color.WHITE);
            stroke.setAlpha(Math.round(255 * (1 - out)));
            stroke.setStrokeWidth(tile * .05f);
            canvas.drawLine(x + way * tile * .6f, y, x + way * (tile * .35f + reach * Math.min(1, out * 3)), y, stroke);
            stroke.setAlpha(255);
        }
        if (lashAge < .2f) {
            // The ribbon, in a long wave out to where it reaches, and its tassel at the end of it.
            float out = lashAge / .2f, reach = (RunArms.reach(PlusRun.WHIP, perks.levels[PlusRun.WHIP]) + .35f) * tile * Math.min(1, out * 3.5f), hand = feet - tile * .8f;
            path.reset();
            path.moveTo(x + way * tile * .3f, hand);
            for (int step = 1; step <= 12; step++) {
                float along = step / 12f;
                path.lineTo(x + way * (tile * .3f + reach * along), hand - tile * (.9f * (float) Math.sin(along * Math.PI) * (1 - out) + .28f * (float) Math.sin(along * 9 + lashAge * 40)));
            }
            stroke.setColor(0xFFD8343C);
            stroke.setAlpha(Math.round(255 * (1 - out * .7f)));
            stroke.setStrokeWidth(tile * .11f);
            canvas.drawPath(path, stroke);
            stroke.setAlpha(255);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFE0A21A);
            paint.setAlpha(Math.round(255 * (1 - out * .7f)));
            canvas.drawCircle(x + way * (tile * .3f + reach), hand - tile * .28f * (float) Math.sin(9 + lashAge * 40), tile * .09f, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
        if (spinAge < .42f) {
            float out = spinAge / .42f, px = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, py = (run.y() + PlusRun.TALL - cameraY) * tile - tile * .7f;
            float round = (RunArms.SPIN + PlusRun.WIDE / 2) * tile * Math.min(1, out * 4);
            box.set(px - round, py - tile * .32f, px + round, py + tile * .32f);
            stroke.setColor(0xFFD8343C);
            stroke.setAlpha(Math.round(255 * (1 - out * out)));
            stroke.setStrokeWidth(tile * .12f);
            canvas.drawArc(box, spinAge * 1500, 300, false, stroke);
            stroke.setAlpha(255);
        }
    }

    /** A slash wave in the air: a crescent, as tall as it reaches, turned the way it flies; white from a sword, thin and pale from a knife, in her colour from a brush. */
    private void drawSlash(Canvas canvas, PlusRun.Shot shot, float x, float y) {
        float high = shot.high * tile, fade = Math.min(1, shot.left / .08f);
        if (shot.of == PlusRun.LANCE) {
            // The lance's jet: a long drop of ink, pointed the way it goes, and what trails after it.
            float way = shot.across < 0 ? -1 : 1, lengthOf = tile * 1.5f;
            stroke.setColor(0xFF2A3FC8);
            stroke.setAlpha(Math.round(150 * fade));
            stroke.setStrokeWidth(tile * .16f);
            canvas.drawLine(x - way * Math.min(shot.age * 26, 4) * tile, y, x - way * lengthOf * .4f, y, stroke);
            stroke.setAlpha(255);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF141A4A);
            paint.setAlpha(Math.round(255 * fade));
            path.reset();
            path.moveTo(x + way * lengthOf * .5f, y);
            path.quadTo(x, y - tile * .3f, x - way * lengthOf * .5f, y);
            path.quadTo(x, y + tile * .3f, x + way * lengthOf * .5f, y);
            canvas.drawPath(path, paint);
            paint.setColor(0xFF4F6BFF);
            paint.setAlpha(Math.round(255 * fade));
            canvas.drawOval(x - lengthOf * .22f, y - tile * .09f, x + lengthOf * .22f, y + tile * .03f, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            return;
        }
        int colour = shot.of == PlusRun.BRUSH ? lookColour(drawn()) : shot.of == PlusRun.KNIFE ? 0xFF9FB2C6 : shot.of == PlusRun.WHIP ? 0xFFD8343C
                : shot.of == PlusRun.KATANA ? 0xFFB9C4D2 : 0xFF6FB4FF;
        canvas.save();
        canvas.rotate((float) Math.toDegrees(Math.atan2(shot.down, shot.across)), x, y);
        float deep = high * (shot.of == PlusRun.KNIFE ? 1.6f : .8f);
        for (int layer = 0; layer < 3; layer++) {
            float back = layer * high * .28f;
            box.set(x - deep - back, y - high, x + deep * .25f - back, y + high);
            stroke.setColor(layer == 0 ? Color.WHITE : colour);
            stroke.setAlpha(Math.round((layer == 0 ? 255 : layer == 1 ? 190 : 90) * fade));
            stroke.setStrokeWidth(tile * (layer == 0 ? .07f : layer == 1 ? .2f : .28f) * (shot.of == PlusRun.KNIFE ? .6f : 1));
            canvas.drawArc(box, -68, 136, false, stroke);
        }
        stroke.setAlpha(255);
        canvas.restore();
    }

    // The hunts

    /** The hunt in a place on the board, or nought; and how it stands: nought, on offer; else taken on, and one more than how much of it is done. */
    private int hunt(int slot) { return session.tally("run_hunt_" + slot); }
    private int huntAt(int slot) { return session.tally("run_hunt_at_" + slot); }

    /** What she has to fight with, a bit for each thing: what is in her backpack, the tablet-book, and her keys if they are hers. */
    private int armsOwned() { return kept().pack; }

    /** Fills the board: every empty place on it gets the next hunt, which is not the same as what is on it. */
    private void stock() {
        int[] on = new int[RunHunts.SLOTS];
        for (int slot = 0; slot < on.length; slot++) on[slot] = hunt(slot);
        for (int slot = 0; slot < on.length; slot++) {
            if (on[slot] != 0) continue;
            int serial = session.tally("run_hunt_serial");
            on[slot] = RunHunts.offer(serial, open(), armsOwned(), session.tally("run_hunts"), on);
            session.keep("run_hunt_serial", serial + 1);
            session.keep("run_hunt_" + slot, on[slot]);
            session.keep("run_hunt_at_" + slot, 0);
        }
        huntsHere();
    }

    /** Which of the hunts she has taken on something can be done towards in the land she is in. */
    private void huntsHere() {
        for (int slot = 0; slot < RunHunts.SLOTS; slot++) huntHere[slot] = run != null && hunt(slot) != 0 && huntAt(slot) > 0 && RunHunts.here(hunt(slot), run.level());
    }

    /** One more done of a hunt she has taken on; and when that is all of it, a card that says so. It is kept there and then, whatever becomes of the land. */
    private void hunted(int slot) {
        int hunt = hunt(slot), at = huntAt(slot), many = RunHunts.many(hunt);
        if (hunt == 0 || at <= 0 || at > many) return;
        session.keep("run_hunt_at_" + slot, at + 1);
        huntFlash[slot] = 1;
        if (at == many) {
            toast("Hunt done: " + RunHunts.brief(hunt), "Hand it in at any guild hall", SCROLL_PICTURE);
            buzz(true);
        }
    }

    /** The board is looked at: the land waits. The first time, Inju says what it is. */
    private void openHunts() {
        stock();
        huntsOpen = true;
        dropAsk = -1;
        huntPaid = huntMore = "";
        goLeft = goRight = goJump = goStrike = false;
        jumpPressed = strikePressed = false;
        audio.play("page", .6f);
        if (session.tally("run_hunts_met") == 0) {
            session.keep("run_hunts_met", 1);
            person(RunStory.HUNTS[0]);
        }
    }

    /** And left: the first time with a hunt taken on, she has her say about it. */
    private void closeHunts() {
        huntsOpen = false;
        audio.play("tap", .3f);
        boolean any = false;
        for (int slot = 0; slot < RunHunts.SLOTS; slot++) any |= huntAt(slot) > 0;
        if (any && session.tally("run_hunts_met") == 1) {
            session.keep("run_hunts_met", 2);
            if (!audio.speaking()) speak(RunStory.HUNTS[4], true);
        }
    }

    private void huntsTouch(float x, float y) {
        for (int slot = 0; slot < RunHunts.SLOTS; slot++) {
            int hunt = hunt(slot), at = huntAt(slot);
            if (hunt == 0) continue;
            boolean finished = at > RunHunts.many(hunt);
            if (!huntActs[slot].isEmpty() && huntActs[slot].contains(x, y)) {
                dropAsk = -1;
                if (at == 0) {
                    // Taken on: from now what she does counts for it, in whatever land.
                    session.keep("run_hunt_at_" + slot, 1);
                    huntPaid = huntMore = "";
                    audio.play("unlock", .6f);
                    buzz(false);
                    if (!audio.speaking()) person(RunStory.HUNTS[1]);
                    huntsHere();
                } else if (finished) {
                    handIn(slot);
                }
                return;
            }
            if (!huntDrops[slot].isEmpty() && huntDrops[slot].contains(x, y)) {
                // Given up, once it has been asked whether she is sure: it comes off the board, and another goes up.
                if (dropAsk != slot) {
                    dropAsk = slot;
                    audio.play("tap", .4f);
                    return;
                }
                dropAsk = -1;
                session.keep("run_hunt_" + slot, 0);
                session.keep("run_hunt_at_" + slot, 0);
                stock();
                audio.play("page", .5f);
                return;
            }
        }
        dropAsk = -1;
        if (!huntsCard.contains(x, y)) closeHunts();
    }

    /**
     * A hunt that is done is handed in: its pluses, what the smith works with and its treat
     * are hers; and with so many handed in, something besides: a pet of her own, a place
     * for one more pet, room for one more stray, a rank in the guild and its badge. Another
     * hunt takes its place on the board.
     */
    private void handIn(int slot) {
        int hunt = hunt(slot), done = session.tally("run_hunts") + 1, was = RunHunts.rank(done - 1), rank = RunHunts.rank(done), more = 0;
        int[] gives = RunHunts.materials(hunt);
        session.keep("run_hunts", done);
        for (int index = 0; index < gives.length; index++) if (gives[index] > 0) session.keep("run_mat_" + index, session.tally("run_mat_" + index) + gives[index]);
        if (RunHunts.treat(hunt)) session.keep("run_treats", session.tally("run_treats") + 1);
        huntPaid = "Paid: " + RunHunts.pays(hunt);
        huntMore = "";
        int brings = RunHunts.brings(done);
        if (brings == RunHunts.A_PET) {
            int own = session.tally("run_pet_own"), kind = 0;
            while (kind < RunPets.FOUND && (own >> kind & 1) != 0) kind++;
            if (kind < RunPets.FOUND) {
                int with = session.tally("run_pet_with") & own;
                session.keep("run_pet_own", own | 1 << kind);
                session.keep("run_pets", session.tally("run_pets") | 1 << kind);
                if (Integer.bitCount(with) < 1 + bought(PET_PLACES)) session.keep("run_pet_with", with | 1 << kind);
                huntMore = PET_NAMES[kind] + " is hers for good";
            } else {
                more = 300;
                huntMore = "Every pet is hers already: + 300";
            }
        } else if (brings != RunHunts.NOTHING) {
            int which = brings == RunHunts.A_PLACE ? PET_PLACES : STRAY_ROOM;
            if (bought(which) < UP_COSTS[which].length) {
                session.keep("run_up_" + which, bought(which) + 1);
                huntMore = brings == RunHunts.A_PLACE ? "A place for one more pet of hers" : "Room for one more stray";
            } else {
                more = 400;
                huntMore = "She has every place there is: + 400";
            }
        }
        if (rank > was) huntMore = (huntMore.isEmpty() ? "" : huntMore + "  ·  ") + "The " + RunHunts.RANK_NAMES[rank - 1] + " badge of the guild";
        session.keep("run_bank", bank() + RunHunts.pluses(hunt) + more);
        session.keep("run_hunt_" + slot, 0);
        session.keep("run_hunt_at_" + slot, 0);
        stock();
        regrant();
        audio.play("buy", .6f);
        audio.play(huntMore.isEmpty() ? "star" : "win", .6f);
        buzz(true);
        person(RunStory.HUNTS[rank > was ? 3 : 2]);
    }

    /** The badge of a rank in the guild: a seal in the rank's colour, with two tails of ribbon and a quill across it. */
    private void badge(Canvas canvas, float x, float y, float size, int rank) {
        int colour = BADGE_COLOURS[Math.max(0, Math.min(BADGE_COLOURS.length - 1, rank - 1))];
        float round = size / 2;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(CalculatorLayout.CORAL);
        for (int side = -1; side <= 1; side += 2) {
            path.reset();
            path.moveTo(x + side * round * .62f, y + round * .4f);
            path.lineTo(x + side * round * .8f, y + round * 1.7f);
            path.lineTo(x + side * round * .38f, y + round * 1.35f);
            path.lineTo(x + side * round * .06f, y + round * 1.62f);
            path.lineTo(x + side * round * .06f, y + round * .4f);
            path.close();
            canvas.drawPath(path, paint);
        }
        paint.setColor(colour);
        canvas.drawCircle(x, y, round, paint);
        stroke.setColor(mixed(colour, Color.BLACK, .35f));
        stroke.setStrokeWidth(size * .09f);
        canvas.drawCircle(x, y, round * .9f, stroke);
        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(size * .11f);
        canvas.drawLine(x - round * .4f, y + round * .45f, x + round * .42f, y - round * .46f, stroke);
        stroke.setStrokeWidth(size * .07f);
        canvas.drawLine(x - round * .02f, y - round * .32f, x + round * .2f, y + round * .08f, stroke);
        paint.setColor(Color.BLACK);
    }

    /**
     * The board of hunts, over the guild hall: her rank in the guild and its badge; the
     * three that are on the board, each with what it asks, what it pays, how far it has got
     * and a button that takes it on, hands it in or gives it up; what comes next for so
     * many handed in; and what she has of what hunts pay.
     */
    private void drawHunts(Canvas canvas) {
        canvas.drawColor(0x99000000);
        int done = session.tally("run_hunts"), rank = RunHunts.rank(done);
        float wide = Math.min(safe.width() - dp(24), dp(430)), pad = dp(16), high = dp(88), gap = dp(8);
        float tall = pad + dp(50) + RunHunts.SLOTS * (high + gap) + dp(80) + pad, left = safe.centerX() - wide / 2, top = Math.max(safe.top + dp(8), safe.centerY() - tall / 2);
        huntsCard.set(left, top, left + wide, top + tall);
        plate(canvas, huntsCard, dp(22), 255);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, "The hunts board", left + pad, top + pad + dp(18), dp(20), wide * .55f);
        text.setTypeface(Typeface.DEFAULT);
        text.setColor(soft);
        fitted(canvas, "Inju keeps it  ·  " + done + " handed in  ·  a hunt is done over any lands", left + pad, top + pad + dp(38), dp(12), wide - pad * 2);
        // Her rank, and its badge.
        text.setTextAlign(Paint.Align.RIGHT);
        text.setColor(rank > 0 ? ink : soft);
        fitted(canvas, rank > 0 ? RunHunts.RANK_NAMES[rank - 1] + " rank" : "No rank yet", left + wide - pad - (rank > 0 ? dp(28) : 0), top + pad + dp(17), dp(13), wide * .34f);
        if (rank > 0) badge(canvas, left + wide - pad - dp(10), top + pad + dp(9), dp(20), rank);
        float y = top + pad + dp(50);
        for (int slot = 0; slot < RunHunts.SLOTS; slot++) {
            int hunt = hunt(slot), at = huntAt(slot), many = Math.max(1, RunHunts.many(hunt));
            boolean taken = at > 0, finished = at > many, asking = dropAsk == slot && taken && !finished;
            box.set(left + dp(8), y, left + wide - dp(8), y + high);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(finished ? (dark ? 0xFF24382E : 0xFFE6F5EC) : taken ? (dark ? 0xFF44292E : 0xFFFCE9EB) : dark ? 0xFF2C3033 : 0xFFF5F6F7);
            canvas.drawRoundRect(box, dp(14), dp(14), paint);
            paint.setColor(Color.BLACK);
            float inner = left + dp(20), room = wide - dp(40) - dp(96);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, RunHunts.words(hunt), inner, y + dp(25), dp(14.5f), room);
            text.setTypeface(Typeface.DEFAULT);
            text.setColor(CalculatorLayout.CORAL);
            fitted(canvas, RunHunts.pays(hunt), inner, y + dp(45), dp(12.5f), room);
            // How far it has got: a bar, and the count.
            int got = taken ? Math.min(many, at - 1) : 0;
            float end = inner + room - dp(74);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
            canvas.drawRoundRect(inner, y + dp(61), end, y + dp(68), dp(4), dp(4), paint);
            paint.setColor(finished ? 0xFF2E9E6E : CalculatorLayout.CORAL);
            if (got > 0) canvas.drawRoundRect(inner, y + dp(61), inner + (end - inner) * got / many, y + dp(68), dp(4), dp(4), paint);
            paint.setColor(Color.BLACK);
            text.setColor(finished ? 0xFF2E9E6E : soft);
            fitted(canvas, finished ? "done" : taken ? got + " of " + many : "on the board", end + dp(8), y + dp(69), dp(12), dp(66));
            // Its button: take it on, or hand it in; or, while it is being done, give it up.
            huntActs[slot].setEmpty();
            huntDrops[slot].setEmpty();
            RectF button = taken && !finished ? huntDrops[slot] : huntActs[slot];
            button.set(left + wide - dp(20) - dp(84), y + high / 2 - dp(18), left + wide - dp(20), y + high / 2 + dp(18));
            if (taken && !finished) {
                stroke.setColor(asking ? CalculatorLayout.CORAL : soft);
                stroke.setStrokeWidth(dp(1.5f));
                canvas.drawRoundRect(button, dp(18), dp(18), stroke);
                text.setColor(asking ? CalculatorLayout.CORAL : soft);
            } else {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(finished ? 0xFF2E9E6E : CalculatorLayout.CORAL);
                canvas.drawRoundRect(button, dp(18), dp(18), paint);
                paint.setColor(Color.BLACK);
                text.setColor(Color.WHITE);
            }
            text.setTextAlign(Paint.Align.CENTER);
            fitted(canvas, finished ? "Hand in" : taken ? (asking ? "Sure?" : "Give up") : "Take", button.centerX(), button.centerY() + dp(5), dp(14), button.width() - dp(12));
            y += high + gap;
        }
        // What comes next for so many handed in; what she has of what hunts pay; and what was just paid, with what came with it.
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        fitted(canvas, "At " + RunHunts.nextAt(done) + " handed in: " + RunHunts.nextIs(done), left + pad, y + dp(16), dp(12.5f), wide - pad * 2);
        text.setColor(soft);
        StringBuilder has = new StringBuilder("She has: ");
        for (int index = 0; index < RunHunts.MATERIALS.length; index++) has.append(session.tally("run_mat_" + index)).append(' ').append(RunHunts.MATERIALS[index].toLowerCase(java.util.Locale.ROOT)).append("  ·  ");
        int treats = session.tally("run_treats");
        has.append(treats).append(treats == 1 ? " treat" : " treats");
        fitted(canvas, has.toString(), left + pad, y + dp(35), dp(12.5f), wide - pad * 2);
        if (!huntPaid.isEmpty()) {
            text.setColor(0xFF2E9E6E);
            fitted(canvas, huntPaid, left + pad, y + dp(54), dp(12.5f), wide - pad * 2);
            if (!huntMore.isEmpty()) fitted(canvas, huntMore, left + pad, y + dp(73), dp(12.5f), wide - pad * 2);
        } else {
            fitted(canvas, "Type metal, ink crystal and gold leaf are what the smith works with", left + pad, y + dp(54), dp(12), wide - pad * 2);
            fitted(canvas, "The pet shop gives a pet for treats  ·  tap outside to close", left + pad, y + dp(73), dp(12), wide - pad * 2);
        }
        text.setTextAlign(Paint.Align.LEFT);
    }

    /** What fell from creatures, as it lies about. */
    private void drawScattered(Canvas canvas) {
        for (PlusRun.Drop drop : run.scattered()) {
            float left = drop.life() - drop.age;
            if (left < drop.blink() && (int) (drop.age * 8) % 2 == 0) continue;
            float x = (drop.x - cameraX) * tile, y = (drop.y - cameraY) * tile;
            if (x < -tile * 2 || x > width + tile * 2) continue;
            if (drop.code == 0) {
                float turned = Math.max(.2f, Math.abs((float) Math.cos(clock * 5 + drop.x * 3)));
                paint.setAlpha(255);
                art(canvas, PLUS_ART, x, y, tile * .5f * turned, false);
                continue;
            }
            int rarity = RunLoot.rarity(drop.code), colour = rarity == RunLoot.ROSE ? RunLoot.rose(clock) : RunLoot.COLOURS[rarity];
            // Its beam: taller and brighter the rarer it is.
            float tall = tile * (2 + rarity * .9f), beat = .6f + .4f * (float) Math.sin(clock * 4 + drop.x);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new android.graphics.LinearGradient(x, y, x, y - tall, colour & 0x00FFFFFF | 0xB0000000, colour & 0x00FFFFFF, android.graphics.Shader.TileMode.CLAMP));
            paint.setAlpha(Math.round(255 * beat));
            canvas.drawRect(x - tile * (.12f + rarity * .03f), y - tall, x + tile * (.12f + rarity * .03f), y, paint);
            paint.setShader(null);
            glowAt(canvas, x, y - tile * .3f, tile * (.5f + rarity * .12f), colour, Math.round(200 * beat));
            paint.setAlpha(255);
            paint.setColor(Color.BLACK);
            float bob = drop.resting ? (float) Math.sin(clock * 3 + drop.x) * tile * .06f : 0;
            lootIcon(canvas, drop.code, x, y - tile * .38f + bob, tile * .62f, colour);
        }
        paint.setAlpha(255);
    }

    /** Which of the pictures in the sheet of them a thing found is: a weapon by its kind and chapter (the rose's its own), armour and accessories each their own. */
    static int iconOf(int code) { return RunLoot.iconOf(code); }

    /** One of the pictures in the sheet, as a square of a size about a place. */
    void drawIcon(Canvas canvas, int index, float x, float y, float size) {
        if (lootIcons == null) lootIcons = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_loot_icons);
        int column = index % LOOT_COLUMNS, row = index / LOOT_COLUMNS;
        source.set(column * LOOT_ICON, row * LOOT_ICON, (column + 1) * LOOT_ICON, (row + 1) * LOOT_ICON);
        target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        canvas.drawBitmap(lootIcons, source, target, paint);
    }

    /** What a thing found looks like: its own picture. */
    private void lootIcon(Canvas canvas, int code, float x, float y, float size, int colour) {
        drawIcon(canvas, iconOf(code), x, y, size * 1.25f);
    }

    /** A rarity's colour, the rose's changing as it is looked at. */
    int rarityColour(int rarity) { return rarity == RunLoot.ROSE ? RunLoot.rose(clock) : RunLoot.COLOURS[Math.max(0, Math.min(RunLoot.ROSE, rarity))]; }

    /** A thing found in a frame of its rarity: a tile tinted with it and a rim of it, glowing from gold up; and its picture in it. Or an empty frame. */
    void lootFrame(Canvas canvas, int code, RectF where, boolean picked) {
        float round = where.width() * .2f;
        paint.setStyle(Paint.Style.FILL);
        if (code == 0) {
            paint.setColor(dark ? 0xFF2C3033 : 0xFFF1F2F3);
            canvas.drawRoundRect(where, round, round, paint);
            stroke.setColor(picked ? CalculatorLayout.CORAL : dark ? 0xFF4A4F54 : 0xFFD0D3D6);
            stroke.setStrokeWidth(dp(picked ? 2.5f : 1.5f));
            canvas.drawRoundRect(where, round, round, stroke);
            paint.setColor(Color.BLACK);
            return;
        }
        int rarity = RunLoot.rarity(code), colour = rarityColour(rarity);
        if (rarity >= RunLoot.GOLD) glowAt(canvas, where.centerX(), where.centerY(), where.width() * .62f, colour, 110 + (int) (40 * Math.sin(clock * 3)));
        paint.setAlpha(255);
        if (rarity == RunLoot.ROSE) {
            // The rose's tile: rose pink, gold and a changing colour across it, so it is never taken for another rarity.
            paint.setShader(new android.graphics.LinearGradient(where.left, where.top, where.right, where.bottom, new int[]{0xFFFFB3D6, 0xFFFFE08A, mixed(colour, Color.WHITE, .35f)},
                    null, android.graphics.Shader.TileMode.CLAMP));
        } else {
            paint.setShader(new android.graphics.LinearGradient(where.left, where.top, where.left, where.bottom, mixed(colour, dark ? 0xFF1E2124 : Color.WHITE, .55f),
                    mixed(colour, dark ? 0xFF15171A : 0xFFE8EAEC, .3f), android.graphics.Shader.TileMode.CLAMP));
        }
        canvas.drawRoundRect(where, round, round, paint);
        paint.setShader(null);
        stroke.setColor(picked ? CalculatorLayout.CORAL : rarity == RunLoot.ROSE ? mixed(0xFFFF4F9A, 0xFFE0A21A, .5f + .5f * (float) Math.sin(clock * 3)) : mixed(colour, Color.BLACK, .15f));
        stroke.setStrokeWidth(dp(picked ? 3 : rarity >= RunLoot.GOLD ? 2.5f : 1.8f));
        canvas.drawRoundRect(where, round, round, stroke);
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        drawIcon(canvas, iconOf(code), where.centerX(), where.centerY(), where.width() * .92f);
    }

    /** In homes: a loose floorboard, a little askew, glinting now and then; and a diary page, glowing softly where it lies. */
    private Bitmap floorboard, diaryPage;

    private void drawHomeSecret(Canvas canvas, int[] item) {
        float x = (item[1] + .5f - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile;
        paint.setAlpha(255);
        if (item[0] == PlusRun.LOOSE) {
            if (floorboard == null) floorboard = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_floorboard);
            float wide = tile * 1.4f, tall = tile * .26f;
            target.set(x - wide / 2, feet - tall * .75f, x + wide / 2, feet + tall * .25f);
            canvas.drawBitmap(floorboard, null, target, paint);
            float glint = (clock * .6f + item[1] * .3f) % 2.5f;
            if (glint < 1) glowAt(canvas, x - wide / 2 + wide * glint, feet - tall * .2f, tile * .25f, 0xFFFFE6A0, Math.round(190 * (float) Math.sin(glint * Math.PI)));
        } else {
            if (diaryPage == null) diaryPage = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_diary);
            float bob = (float) Math.sin(clock * 2.6f + item[1]) * tile * .06f, size = tile * .8f;
            glowAt(canvas, x, feet - tile * .5f + bob, tile * .7f, 0xFFFFF0C0, 120);
            paint.setAlpha(255);
            target.set(x - size / 2, feet - tile * .5f + bob - size / 2, x + size / 2, feet - tile * .5f + bob + size / 2);
            canvas.drawBitmap(diaryPage, null, target, paint);
        }
        paint.setAlpha(255);
    }

    /** A diary page found: what is written on it, on a card of its own for a while; and which of them have been found, kept. */
    private int seenXp, seenFound;
    private float levelAge = 9;
    private String diaryText;
    private int diaryNumber, seenDiaries, seenLifted;
    private float diaryAge = 99;
    static final float DIARY_SHOWN = 9;

    /** Her level and how far she is to the next, under the plate at the top left; at a new level it swells and shines. */
    private void drawLevel(Canvas canvas, float left, float top) {
        int level = herLevel();
        long xp = herXp(), from = RunLoot.need(level), to = RunLoot.need(level + 1);
        float part = to > from ? Math.min(1, (xp - from) / (float) (to - from)) : 0, wide = dp(120), high = dp(22), swell = levelAge < .8f ? 1 + .25f * (1 - levelAge / .8f) : 1;
        canvas.save();
        canvas.scale(swell, swell, left, top + high / 2);
        box.set(left, top, left + wide, top + high);
        plate(canvas, box, high / 2, 235);
        text.setTextAlign(Paint.Align.LEFT);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setColor(levelAge < 1.5f ? 0xFFE0A21A : ink);
        fitted(canvas, "Lv " + level, left + dp(9), top + high * .7f, dp(12.5f), dp(46));
        text.setTypeface(Typeface.DEFAULT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
        canvas.drawRoundRect(left + dp(52), top + high / 2 - dp(3), left + wide - dp(9), top + high / 2 + dp(3), dp(3), dp(3), paint);
        paint.setColor(0xFFE0A21A);
        if (part > 0) canvas.drawRoundRect(left + dp(52), top + high / 2 - dp(3), left + dp(52) + (wide - dp(61)) * part, top + high / 2 + dp(3), dp(3), dp(3), paint);
        paint.setColor(Color.BLACK);
        canvas.restore();
    }

    /** A layer of hearts' colour over the red of the heart: none for the first; gold, purple, orange, blue, green after. */
    private final android.graphics.ColorFilter[] heartFilters = new android.graphics.ColorFilter[RunSkills.LAYER_COLOURS.length];

    private android.graphics.ColorFilter heartFilter(int layer) {
        if (layer <= 0 || layer >= heartFilters.length) return null;
        if (heartFilters[layer] == null) {
            int colour = RunSkills.LAYER_COLOURS[layer];
            heartFilters[layer] = new android.graphics.LightingColorFilter(0xFF000000 | colour & 0xFFFFFF, 0x00202020);
        }
        return heartFilters[layer];
    }

    /** A piece of her shield bar: a small shield, blue as far as it is filled. */
    private void drawWard(Canvas canvas, float x, float y, float size, float full) {
        path.reset();
        float half = size / 2;
        path.moveTo(x - half, y - half * .8f);
        path.quadTo(x, y - half * 1.1f, x + half, y - half * .8f);
        path.lineTo(x + half * .85f, y + half * .1f);
        path.quadTo(x + half * .5f, y + half * .8f, x, y + half * 1.1f);
        path.quadTo(x - half * .5f, y + half * .8f, x - half * .85f, y + half * .1f);
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? 0xFF2C3440 : 0xFFDDE6F0);
        canvas.drawPath(path, paint);
        if (full > 0) {
            canvas.save();
            canvas.clipRect(x - size, y + half * 1.1f - size * 1.1f * full, x + size, y + size);
            paint.setShader(new android.graphics.LinearGradient(x, y - half, x, y + half, 0xFF9AD4FF, 0xFF2F7FE0, android.graphics.Shader.TileMode.CLAMP));
            canvas.drawPath(path, paint);
            paint.setShader(null);
            canvas.restore();
        }
        stroke.setColor(full >= 1 ? 0xFF1F5FB0 : dark ? 0xFF5A6470 : 0xFF8FA3B8);
        stroke.setStrokeWidth(dp(1.4f));
        canvas.drawPath(path, stroke);
        paint.setColor(Color.BLACK);
    }

    /** Her energy, for her specials, beside her level: a bar of ink-blue, bright when there is enough for one. */
    private void drawEnergy(Canvas canvas, float left, float top) {
        float wide = dp(96), high = dp(22), part = run.energy() / PlusRun.ENERGY_MOST, cost = run.specialCost() / PlusRun.ENERGY_MOST;
        box.set(left, top, left + wide, top + high);
        plate(canvas, box, high / 2, 235);
        // A small bolt.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF3F7FE0);
        path.reset();
        float bx = left + dp(12), by = top + high / 2;
        path.moveTo(bx + dp(2), by - dp(7));
        path.lineTo(bx - dp(4), by + dp(1));
        path.lineTo(bx, by + dp(1));
        path.lineTo(bx - dp(2), by + dp(7));
        path.lineTo(bx + dp(4), by - dp(1));
        path.lineTo(bx, by - dp(1));
        path.close();
        canvas.drawPath(path, paint);
        float from = left + dp(22), to = left + wide - dp(9);
        paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
        canvas.drawRoundRect(from, by - dp(3), to, by + dp(3), dp(3), dp(3), paint);
        paint.setColor(run.energy() >= run.specialCost() ? 0xFF3F7FE0 : 0xFF8FA3B8);
        if (part > 0) canvas.drawRoundRect(from, by - dp(3), from + (to - from) * part, by + dp(3), dp(3), dp(3), paint);
        // Where one special's worth ends.
        paint.setColor(dark ? 0xFFF3F3F3 : 0xFF1F2328);
        canvas.drawRect(from + (to - from) * Math.min(1, cost) - dp(.75f), by - dp(4), from + (to - from) * Math.min(1, cost) + dp(.75f), by + dp(4), paint);
        paint.setColor(Color.BLACK);
    }

    /**
     * What her gear does that is seen in the land: on creatures, frost over one frozen, bubbles rising off one ink-poisoned, flames on one
     * burning; and words where a great blow landed, and over her when she dodges or shrugs a hurt off.
     */
    private void drawGearMarks(Canvas canvas) {
        for (PlusRun.Foe foe : run.foes()) {
            if (foe.flat || foe.away || foe.poisoned <= 0 && foe.burning <= 0 && foe.frozen <= 0) continue;
            float left = (foe.x - cameraX) * tile, top = (foe.y - cameraY) * tile, wide = foe.wide() * tile, tall = foe.tall() * tile;
            if (left < -tile * 2 || left > width + tile) continue;
            if (foe.frozen > 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFA8E6FF);
                paint.setAlpha(Math.round(110 * Math.min(1, foe.frozen / .3f)));
                canvas.drawRoundRect(left - tile * .05f, top - tile * .05f, left + wide + tile * .05f, top + tall + tile * .05f, tile * .2f, tile * .2f, paint);
                paint.setColor(Color.WHITE);
                paint.setAlpha(230);
                for (int flake = 0; flake < 3; flake++) {
                    float fx = left + wide * (.2f + .3f * flake), fy = top + tall * (.25f + .2f * (flake % 2));
                    canvas.drawLine(fx - tile * .08f, fy, fx + tile * .08f, fy, stroke(0xFFFFFFFF, dp(1.5f)));
                    canvas.drawLine(fx, fy - tile * .08f, fx, fy + tile * .08f, stroke);
                }
            }
            if (foe.poisoned > 0) {
                paint.setStyle(Paint.Style.FILL);
                for (int bubble = 0; bubble < 3; bubble++) {
                    float rise = (clock * .9f + bubble * .33f) % 1;
                    paint.setColor(bubble == 1 ? 0xFF8E5BE8 : 0xFF5FBF6A);
                    paint.setAlpha(Math.round(220 * (1 - rise)));
                    canvas.drawCircle(left + wide * (.25f + .25f * bubble), top - rise * tile * .7f, tile * (.07f + .03f * bubble), paint);
                }
            }
            if (foe.burning > 0) {
                glowAt(canvas, left + wide / 2, top + tall * .55f, Math.max(wide, tall) * .7f, 0xFFFF8A2A, 150 + (int) (60 * Math.sin(clock * 20)));
                paint.setStyle(Paint.Style.FILL);
                for (int flame = 0; flame < 3; flame++) {
                    float flick = (float) Math.sin(clock * 14 + flame * 2) * tile * .05f, fx = left + wide * (.2f + .3f * flame), fy = top + tall * .25f;
                    path.reset();
                    path.moveTo(fx - tile * .1f, fy + tile * .12f);
                    path.quadTo(fx - tile * .02f + flick, fy - tile * .1f, fx + flick, fy - tile * .28f);
                    path.quadTo(fx + tile * .06f + flick, fy - tile * .05f, fx + tile * .1f, fy + tile * .12f);
                    path.close();
                    paint.setColor(flame == 1 ? 0xFFFFD34A : 0xFFFF6A2A);
                    paint.setAlpha(230);
                    canvas.drawPath(path, paint);
                }
            }
        }
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        if (run.critAge() < .8f) {
            float rise = run.critAge() / .8f;
            text.setColor(0xFFFFC433);
            text.setAlpha(Math.round(255 * (1 - rise * rise)));
            fitted(canvas, "Great blow!", (run.critX() - cameraX) * tile, (run.critY() - cameraY) * tile - rise * tile * .8f, dp(15), dp(160));
        }
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() - cameraY) * tile - tile * .3f;
        if (run.dodgeAge() < .7f || run.shrugAge() < .7f) {
            float age = Math.min(run.dodgeAge(), run.shrugAge()), rise = age / .7f;
            text.setColor(0xFF7FE7DA);
            text.setAlpha(Math.round(255 * (1 - rise)));
            fitted(canvas, run.dodgeAge() <= run.shrugAge() ? "Dodged" : "Shrugged off", herX, herY - rise * tile * .6f, dp(13.5f), dp(140));
        }
        text.setAlpha(255);
        text.setTypeface(Typeface.DEFAULT);
    }

    /** Each colour of toughness's bar of health: plain red, then its own colour. */
    private static final int[] TIER_BARS = {0xFFE0362F, 0xFFFF4A3A, 0xFFB463FF, 0xFFFFC433, 0xFF6A4CC8};
    private final android.graphics.ColorFilter[] tierFilters = new android.graphics.ColorFilter[PlusRun.TIERS];
    private android.graphics.ColorFilter struck;

    /**
     * A colour of toughness laid over a creature's own picture: its light and dark kept, so its outline and face still read, and its colours
     * turned toward red, purple, gold, or the near-black of ink. Plain creatures as they are drawn.
     */
    private android.graphics.ColorFilter tierFilter(int tier) {
        if (tier <= PlusRun.NORMAL || tier >= PlusRun.TIERS) return null;
        if (tierFilters[tier] == null) {
            float[][] tints = {{0, 0, 0}, {1f, .26f, .22f}, {.58f, .24f, 1f}, {1f, .8f, .28f}, {.3f, .25f, .42f}};
            float[] keepOf = {0, .3f, .3f, .28f, .2f}, bright = {0, 1.45f, 1.4f, 1.5f, .95f};
            float k = 1 - keepOf[tier], s = bright[tier];
            float[] c = tints[tier];
            float[] m = new float[20];
            // (Its light lifted a little first, so that a creature drawn near black takes the colour too; its outline stays the darkest of it.)
            for (int row = 0; row < 3; row++) {
                m[row * 5] = keepOf[tier] * (row == 0 ? 1 : 0) + k * s * c[row] * .3f * .75f;
                m[row * 5 + 1] = keepOf[tier] * (row == 1 ? 1 : 0) + k * s * c[row] * .59f * .75f;
                m[row * 5 + 2] = keepOf[tier] * (row == 2 ? 1 : 0) + k * s * c[row] * .11f * .75f;
                m[row * 5 + 4] = k * s * c[row] * (tier == PlusRun.INK_TIER ? 30 : 64);
            }
            m[18] = 1;
            tierFilters[tier] = new android.graphics.ColorMatrixColorFilter(m);
        }
        return tierFilters[tier];
    }

    // The ground pound and the aerial strike, seen

    /** The things she may hold, by the names their pictures go by; and the pictures of the ground pound's four (wind-up, drop, impact, rising) and the aerial strike's four, from the runart pack. */
    static final String[] MOVE_KINDS = RunArms.MOVE_KINDS;
    static final int MOVE_FRAMES = 4;
    private final java.util.Map<String, Bitmap> moveSheets = new java.util.HashMap<>();
    private final java.util.Map<String, android.util.SparseArray<Bitmap>> moveForms = new java.util.HashMap<>();

    /** A sheet of the ground pound's (or the aerial strike's) for what she holds: its name in the pack and the vanity's. */
    static String moveSheet(boolean smash, int weapon) {
        return (smash ? "smash_" : "aerial_") + (weapon < 0 || weapon >= MOVE_KINDS.length ? "unarmed" : MOVE_KINDS[weapon]);
    }

    private Bitmap moveArt(String name) {
        if (!moveSheets.containsKey(name)) {
            Bitmap art = null;
            try (java.io.InputStream in = context.getAssets().open("runart/halgakos_" + name + ".webp")) {
                art = BitmapFactory.decodeStream(in);
            } catch (java.io.IOException missing) {
                art = null;
            }
            moveSheets.put(name, art);
        }
        return moveSheets.get(name);
    }

    /**
     * She is drawn in a ground pound's own pictures (gathering in the air, the drop, the impact as she lands, rising after it) or an aerial
     * strike's (wings spread, the thing in her hands about her): in her form's colour, and dressed as she is where what she wears has them.
     * Whether she was; where what she wears has not got them yet, she is not, and her own moves stand for them.
     */
    private boolean drawSmashOrAerial(Canvas canvas, int weapon, float feetX, float feetY, float tall, boolean leftward) {
        int frame;
        boolean smash;
        int phase = run.poundPhase();
        float landed = run.poundAge();
        if (phase == 1) {
            smash = true;
            frame = 0;
        } else if (phase == 2) {
            smash = true;
            frame = 1;
        } else if (landed < SMASH_AFTER && run.onGround()) {
            smash = true;
            frame = landed < SMASH_IMPACT ? 2 : 3;
        } else if (run.aerialOn()) {
            smash = false;
            float age = run.aerialAge();
            frame = age < AERIAL_WIND ? 0 : 1 + (int) ((age - AERIAL_WIND) / AERIAL_BEAT) % 3;
        } else {
            return false;
        }
        String name = moveSheet(smash, weapon);
        if (!vanityCovers(name)) return false;
        Bitmap art = moveArt(name);
        if (art == null) return false;
        android.util.SparseArray<Bitmap> forms = moveForms.get(name);
        if (forms == null) moveForms.put(name, forms = new android.util.SparseArray<>());
        frame(canvas, dressed(name, formed(art, forms)), frame, MOVE_FRAMES, feetX, feetY, tall, leftward);
        return true;
    }

    /** How long after landing a pound she is seen in its impact, and then rising; how long the aerial strike's wind-up shows, and each of its strikes. */
    static final float SMASH_IMPACT = .14f, SMASH_AFTER = .34f, AERIAL_WIND = .1f, AERIAL_BEAT = .1f;

    /**
     * What the moves throw about: for the ground pound, a ring along the ground as wide as it reaches, dust, cracks, and bits flying in the
     * way of what she holds (ink for brushes, pens and guns, sparks for blades, rubble for blunt things, shards for magic), and at the third
     * step its shockwave running out; for a glide and the aerial strike, wind behind her and feathers off her wings. (Never more than
     * FX_MOST bits at once.)
     */
    private final List<float[]> moveBits = new ArrayList<>();
    static final int FX_MOST = 48;
    private int seenPounds, seenAerials, seenAerialHits;
    private float poundFxAge = 9, poundFxX, poundFxY, poundFxReach, featherClock;
    private int poundFxFlavour, poundFxLevel;

    private void movesAfter(float seconds) {
        poundFxAge += seconds;
        if (run.pounds() != seenPounds) {
            seenPounds = run.pounds();
            poundFxAge = 0;
            poundFxX = run.x() + PlusRun.WIDE / 2;
            poundFxY = run.y() + PlusRun.TALL;
            poundFxReach = run.poundReach();
            poundFxLevel = Math.max(1, perks.poundLevel);
            poundFxFlavour = PlusRun.flavourOf(PlusRun.blowOf(run.weapon()));
            shake = Math.max(shake, .1f + .04f * poundFxLevel);
            audio.play("quake", .5f + .15f * poundFxLevel);
            int bits = 8 + 5 * poundFxLevel;
            for (int bit = 0; bit < bits && moveBits.size() < FX_MOST; bit++) {
                float side = bit % 2 == 0 ? -1 : 1, speed = 2.5f + random.nextFloat() * 3 * poundFxLevel;
                moveBits.add(new float[]{poundFxX + side * random.nextFloat() * .6f, poundFxY - .1f, side * speed * (.4f + random.nextFloat()), -3 - random.nextFloat() * 5, 0, poundFxFlavour, .5f + random.nextFloat() * .5f});
            }
        }
        if (run.aerials() != seenAerials) {
            seenAerials = run.aerials();
            audio.play("flap", .6f);
        }
        if (run.aerialHits() != seenAerialHits) {
            if (run.aerialHits() / 3 != seenAerialHits / 3) audio.play("slash", .35f);
            seenAerialHits = run.aerialHits();
        }
        // Feathers off her wings, and the wind behind her, while she glides or hangs in an aerial strike.
        boolean aloft = run.gliding() || run.aerialOn();
        if (aloft && (featherClock -= seconds) <= 0 && moveBits.size() < FX_MOST) {
            featherClock = run.aerialOn() ? .07f : .16f;
            float cx = run.x() + PlusRun.WIDE / 2 + (run.facingLeft() ? .35f : -.35f), cy = run.y() + PlusRun.TALL * .35f;
            moveBits.add(new float[]{cx + (random.nextFloat() - .5f) * .6f, cy + (random.nextFloat() - .5f) * .4f, (run.facingLeft() ? 1 : -1) * (.6f + random.nextFloat()),
                    -.4f + random.nextFloat() * .5f, 0, FEATHER_BIT + (random.nextInt(2)), 1.1f + random.nextFloat() * .5f});
        }
        for (int index = moveBits.size() - 1; index >= 0; index--) {
            float[] bit = moveBits.get(index);
            bit[4] += seconds;
            if (bit[4] > bit[6]) {
                moveBits.remove(index);
                continue;
            }
            boolean feather = bit[5] >= FEATHER_BIT;
            bit[0] += bit[2] * seconds;
            bit[1] += bit[3] * seconds;
            if (feather) {
                bit[3] = Math.min(.9f, bit[3] + 1.2f * seconds);
                bit[2] *= 1 - seconds * .8f;
            } else {
                bit[3] += 18 * seconds;
            }
        }
    }

    /** Bits that are feathers (white, then dark): above the four ways a blow is seen. */
    static final int FEATHER_BIT = 10;

    private void drawMoveFx(Canvas canvas) {
        if (run == null) return;
        // The pound's ring, along the ground and as wide as it reaches; its cracks; and its dust.
        if (poundFxAge < .6f) {
            float x = (poundFxX - cameraX) * tile, y = (poundFxY - cameraY) * tile, through = Math.min(1, poundFxAge / .35f), reach = poundFxReach * tile;
            int colour = poundFxFlavour == PlusRun.SPLASHES ? 0xFF2A3266 : poundFxFlavour == PlusRun.CUTS ? 0xFFFFE08A : poundFxFlavour == PlusRun.SPARKLES ? 0xFFB9F3FF : 0xFFF2E6CF;
            if (poundFxAge < .35f) {
                stroke.setColor(colour);
                stroke.setAlpha(Math.round(230 * (1 - through)));
                stroke.setStrokeWidth(tile * (.16f - .1f * through));
                box.set(x - reach * through, y - tile * .28f * through, x + reach * through, y + tile * .16f * through);
                canvas.drawOval(box, stroke);
                stroke.setColor(Color.WHITE);
                stroke.setAlpha(Math.round(200 * (1 - through)));
                stroke.setStrokeWidth(tile * .04f);
                box.set(x - reach * through * .7f, y - tile * .2f * through, x + reach * through * .7f, y + tile * .1f * through);
                canvas.drawOval(box, stroke);
            }
            // Cracks running out along the ground, longer the further it reaches.
            stroke.setColor(0xFF2B2420);
            stroke.setAlpha(Math.round(200 * Math.max(0, 1 - poundFxAge / .6f)));
            stroke.setStrokeWidth(Math.max(1.5f, tile * .04f));
            int cracks = 2 + poundFxLevel;
            for (int crack = 0; crack < cracks; crack++) {
                float side = crack % 2 == 0 ? -1 : 1, long_ = reach * (.45f + .5f * ((crack * 37) % 10) / 10f) * Math.min(1, poundFxAge / .12f);
                float px = x, py = y + tile * .02f;
                for (int step = 1; step <= 4; step++) {
                    float nx = x + side * long_ * step / 4, ny = y + tile * .02f + (step % 2 == 0 ? -1 : 1) * tile * .05f * ((crack + step) % 3);
                    canvas.drawLine(px, py, nx, ny, stroke);
                    px = nx;
                    py = ny;
                }
            }
            // Dust, rolling out low on both sides.
            paint.setStyle(Paint.Style.FILL);
            for (int puff = 0; puff < 6 + 2 * poundFxLevel; puff++) {
                float side = puff % 2 == 0 ? -1 : 1, out = reach * (.2f + .8f * through) * (.5f + .5f * (puff % 4) / 3f);
                paint.setColor(0xFFE9E0D0);
                paint.setAlpha(Math.round(170 * Math.max(0, 1 - poundFxAge / .6f)));
                canvas.drawCircle(x + side * out, y - tile * (.08f + .06f * (puff % 3)), tile * (.14f + .1f * through) * (1 + poundFxLevel * .15f), paint);
            }
            stroke.setAlpha(255);
            paint.setAlpha(255);
        }
        // The third step's shockwave: a crest of dust running out both ways along the ground.
        if (run.waveFront() >= 0) {
            float y = (poundFxY - cameraY) * tile;
            for (int side = -1; side <= 1; side += 2) {
                float x = (run.waveX() + side * run.waveFront() - cameraX) * tile;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFF2E6CF);
                paint.setAlpha(190);
                box.set(x - tile * .35f, y - tile * .7f, x + tile * .35f, y + tile * .05f);
                canvas.drawOval(box, paint);
                stroke.setColor(Color.WHITE);
                stroke.setAlpha(220);
                stroke.setStrokeWidth(tile * .06f);
                canvas.drawArc(box, side < 0 ? 180 : 270, 90, false, stroke);
            }
            paint.setAlpha(255);
            stroke.setAlpha(255);
        }
        // The wind behind her, while she glides or strikes from the air.
        if (run.gliding() || run.aerialOn()) {
            float hx = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, hy = (run.y() + PlusRun.TALL * .45f - cameraY) * tile, back = run.facingLeft() ? 1 : -1;
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(Math.max(1.5f, tile * .03f));
            for (int line = 0; line < 3; line++) {
                float drift = (clock * 3 + line * .33f) % 1;
                stroke.setAlpha(Math.round(160 * (1 - drift)));
                float sy = hy + (line - 1) * tile * .32f, sx = hx + back * tile * (.6f + drift * 1.4f);
                canvas.drawLine(sx, sy, sx + back * tile * .55f, sy, stroke);
            }
            stroke.setAlpha(255);
        }
        // The bits: what flew off a pound, and feathers.
        paint.setStyle(Paint.Style.FILL);
        for (float[] bit : moveBits) {
            float x = (bit[0] - cameraX) * tile, y = (bit[1] - cameraY) * tile, left = 1 - bit[4] / bit[6];
            int kind = (int) bit[5];
            if (kind >= FEATHER_BIT) {
                paint.setColor(kind == FEATHER_BIT ? 0xFFFFFFFF : 0xFF3A2E44);
                paint.setAlpha(Math.round(230 * Math.min(1, left * 2)));
                canvas.save();
                canvas.rotate((float) Math.sin(bit[4] * 6 + bit[0]) * 50, x, y);
                box.set(x - tile * .1f, y - tile * .035f, x + tile * .1f, y + tile * .035f);
                canvas.drawOval(box, paint);
                canvas.restore();
                continue;
            }
            paint.setColor(kind == PlusRun.SPLASHES ? 0xFF1B2152 : kind == PlusRun.CUTS ? 0xFFFFD45A : kind == PlusRun.SPARKLES ? (((int) (bit[0] * 10)) % 2 == 0 ? 0xFFB9F3FF : 0xFFF5C8FF) : 0xFF8A7A66);
            paint.setAlpha(Math.round(255 * Math.min(1, left * 2)));
            float r = tile * (kind == PlusRun.CUTS ? .05f : .08f);
            if (kind == PlusRun.THUDS) canvas.drawRect(x - r, y - r, x + r, y + r, paint);
            else canvas.drawCircle(x, y, r, paint);
        }
        paint.setAlpha(255);
        stroke.setAlpha(255);
    }
    /** How long a creature just hit is lifted near to white, and how long it is squashed by the blow. */
    static final float STRUCK_WHITE = .12f, STRUCK_SQUASH = .16f;

    /** The white of a blow landing: everything of the picture lifted near to white for a moment. */
    private android.graphics.ColorFilter struckFilter() {
        if (struck == null) struck = new android.graphics.LightingColorFilter(0xFF606060, 0x00B8B8B8);
        return struck;
    }

    /**
     * Blows landing: a burst where each landed in the way of what dealt it (a slash of light for a blade, a splat of ink for ink and pens,
     * a star of rays for a blunt blow, a scatter of shards for magic), ink splashing out from it (ink, never anything else), and the number
     * it took off rising and fading, gold and bigger for a great blow. A great blow, a shield breaking and a creature beaten burst bigger,
     * with a ring; a heavy one shakes the screen a little and holds everything still for a frame or two. (There are never more than 24 at
     * once, so a swarm stays readable.)
     */
    private void drawHits(Canvas canvas, int first, int last) {
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        for (float[] hit : run.hits()) {
            if (hit[0] < first - 2 || hit[0] > last + 2) continue;
            boolean great = hit[3] > 0, beaten = hit[8] > 0, broke = hit[9] > 0;
            if (hit[6] == 0) {
                hit[6] = 1;
                if (perks.item != 0 && RunLoot.rarity(perks.item) >= RunLoot.RED && RunArms.kindOf(perks.item) == run.weapon()) shake = Math.max(shake, .12f);
                if (great || hit[2] >= 3 * PlusRun.BLOW || broke) shake = Math.max(shake, great || broke ? .14f : .08f);
                // The hit-stop: a heavy blow, a shield broken, or a big one beaten.
                if (great || broke || hit[2] >= 3 * PlusRun.BLOW || beaten && hit[5] > 0) hitStop = Math.max(hitStop, HIT_STOP);
            }
            float age = hit[4], x = (hit[0] - cameraX) * tile, y = (hit[1] - cameraY) * tile + tile * .25f;
            float big = great || broke ? 1.6f : beaten ? 1.35f : 1;
            int flavour = (int) hit[7];
            // The burst.
            if (age < .18f) {
                float through = age / .18f, reach = tile * (.3f + .45f * through) * big;
                int alpha = Math.round(255 * (1 - through));
                if (flavour == PlusRun.CUTS) {
                    // A slash of light across where it landed, a thin crescent sweeping and thinning.
                    stroke.setColor(0xFFFFFFFF);
                    stroke.setAlpha(alpha);
                    stroke.setStrokeCap(Paint.Cap.ROUND);
                    float turn = (float) (Math.sin(hit[0] * 13) * 25) - 35;
                    android.graphics.RectF arc = new android.graphics.RectF(x - reach, y - reach * .7f, x + reach, y + reach * .7f);
                    stroke.setStrokeWidth(tile * .12f * (1 - through) * big);
                    canvas.drawArc(arc, turn, 110 + 40 * through, false, stroke);
                    stroke.setColor(0xFFFFE9A8);
                    stroke.setAlpha(alpha);
                    stroke.setStrokeWidth(tile * .04f * big);
                    canvas.drawArc(arc, turn + 10, 90 + 40 * through, false, stroke);
                    stroke.setStrokeCap(Paint.Cap.BUTT);
                } else if (flavour == PlusRun.SPLASHES) {
                    // A splat of ink, thrown wide.
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFF1B2152);
                    paint.setAlpha(alpha);
                    canvas.drawCircle(x, y, tile * .2f * big * (1 - through * .5f), paint);
                    for (int blob = 0; blob < 8; blob++) {
                        double turn = blob * Math.PI / 4 + hit[1];
                        canvas.drawCircle(x + (float) Math.cos(turn) * reach, y + (float) Math.sin(turn) * reach * .8f, tile * .08f * big * (1 - through * .6f), paint);
                    }
                } else if (flavour == PlusRun.SPARKLES) {
                    // Shards of light, turning as they fly.
                    paint.setStyle(Paint.Style.FILL);
                    for (int shard = 0; shard < 7; shard++) {
                        double turn = shard * Math.PI * 2 / 7 + hit[0] * 3;
                        float sx = x + (float) Math.cos(turn) * reach, sy = y + (float) Math.sin(turn) * reach, r = tile * .09f * big * (1 - through * .5f);
                        paint.setColor(shard % 2 == 0 ? 0xFFB9F3FF : 0xFFF5C8FF);
                        paint.setAlpha(alpha);
                        android.graphics.Path diamond = new android.graphics.Path();
                        diamond.moveTo(sx, sy - r * 1.6f);
                        diamond.lineTo(sx + r, sy);
                        diamond.lineTo(sx, sy + r * 1.6f);
                        diamond.lineTo(sx - r, sy);
                        diamond.close();
                        canvas.drawPath(diamond, paint);
                    }
                } else {
                    // A star of rays, thick, for a blow that thuds.
                    stroke.setColor(0xFFFFF4C2);
                    stroke.setAlpha(alpha);
                    stroke.setStrokeWidth(tile * .08f * big);
                    for (int ray = 0; ray < 8; ray++) {
                        double turn = ray * Math.PI / 4 + hit[0];
                        float long_ = ray % 2 == 0 ? 1 : .65f;
                        canvas.drawLine(x + (float) Math.cos(turn) * reach * .3f, y + (float) Math.sin(turn) * reach * .3f, x + (float) Math.cos(turn) * reach * long_, y + (float) Math.sin(turn) * reach * long_, stroke);
                    }
                }
                // The white of it, at its heart, for a moment.
                if (age < .07f) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.WHITE);
                    paint.setAlpha(Math.round(230 * (1 - age / .07f)));
                    canvas.drawCircle(x, y, tile * .22f * big, paint);
                }
                stroke.setAlpha(255);
                paint.setAlpha(255);
            }
            // A great blow, a shield breaking and a creature beaten: a ring going out.
            if ((great || broke || beaten) && age < .3f) {
                float through = age / .3f;
                stroke.setColor(broke ? 0xFFBFE6FF : great ? 0xFFFFC433 : 0xFFFFFFFF);
                stroke.setAlpha(Math.round(220 * (1 - through)));
                stroke.setStrokeWidth(tile * .07f * (1 - through));
                canvas.drawCircle(x, y, tile * (.3f + 1.1f * through) * big, stroke);
                stroke.setAlpha(255);
            }
            // The ink.
            if (age < .35f) {
                float through = age / .35f;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF1B2152);
                paint.setAlpha(Math.round(220 * (1 - through)));
                for (int drop = 0; drop < (beaten || great ? 10 : 7); drop++) {
                    double turn = drop * 0.9 + hit[1] * 3;
                    float far = tile * (.15f + .5f * through) * (.6f + .4f * (drop % 3) / 2f) * big;
                    canvas.drawCircle(x + (float) Math.cos(turn) * far, y + (float) Math.sin(turn) * far + through * through * tile * .3f, tile * (.07f - .03f * through) * (drop % 2 == 0 ? 1 : .7f), paint);
                }
                paint.setAlpha(255);
            }
            if (hit[2] <= 0) continue;
            // The number.
            float rise = age / .9f, size = dp(great ? 19 : 14) * (age < .1f ? 1 + (1 - age / .1f) * .4f : 1);
            text.setTextSize(size);
            String said = String.valueOf((int) hit[2]) + (great ? "!" : "");
            float tx = x + (float) Math.sin(hit[0] * 7) * tile * .15f, ty = y - tile * .5f - rise * tile * .9f;
            int alpha = Math.round(255 * Math.min(1, (1 - rise) * 2.5f));
            stroke.setColor(0xFF1F2328);
            stroke.setStrokeWidth(dp(3));
            stroke.setAlpha(alpha);
            Paint.Style was = text.getStyle();
            text.setStyle(Paint.Style.STROKE);
            text.setStrokeWidth(dp(3.5f));
            text.setColor(0xFF1F2328);
            text.setAlpha(alpha);
            canvas.drawText(said, tx, ty, text);
            text.setStyle(was);
            text.setStrokeWidth(0);
            text.setColor(great ? 0xFFFFC433 : Color.WHITE);
            text.setAlpha(alpha);
            canvas.drawText(said, tx, ty, text);
            stroke.setAlpha(255);
        }
        text.setAlpha(255);
        text.setTypeface(Typeface.DEFAULT);
        paint.setColor(Color.BLACK);
    }

    /** The stroke paint, in a colour and width. */
    private Paint stroke(int colour, float wide) {
        stroke.setColor(colour);
        stroke.setStrokeWidth(wide);
        return stroke;
    }

    private void drawDiary(Canvas canvas) {
        if (diaryText == null || diaryAge >= DIARY_SHOWN) return;
        float shown = Math.min(1, Math.min(diaryAge / .3f, (DIARY_SHOWN - diaryAge) / .5f));
        float wide = Math.min(safe.width() - dp(32), dp(380)), left = safe.centerX() - wide / 2;
        text.setTextSize(dp(14));
        List<String> lines = wrap(diaryText, wide - dp(36));
        float top = safe.top + Math.max(dp(170), safe.height() * .24f), tall = dp(44) + lines.size() * dp(20) + dp(14);
        canvas.saveLayerAlpha(left - dp(8), top - dp(8), left + wide + dp(8), top + tall + dp(8), Math.round(255 * shown));
        box.set(left, top, left + wide, top + tall);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFF6EBD2);
        canvas.drawRoundRect(box, dp(10), dp(10), paint);
        stroke.setColor(0xFFB98A56);
        stroke.setStrokeWidth(dp(2));
        canvas.drawRoundRect(box, dp(10), dp(10), stroke);
        paint.setColor(Color.BLACK);
        text.setTextAlign(Paint.Align.LEFT);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setColor(0xFF8A5A33);
        fitted(canvas, "A diary page  ·  " + Integer.bitCount(session.tally("run_diary")) + " of " + RunLevels.DIARY_PAGES + " found", left + dp(18), top + dp(28), dp(13), wide - dp(36));
        text.setTypeface(Typeface.DEFAULT);
        text.setColor(0xFF4A3420);
        text.setTextSize(dp(14));
        for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), left + dp(18), top + dp(52) + index * dp(20), text);
        canvas.restore();
    }

    /** Each chapter's village's landmark, and the arch at its entrance that carries its name. */
    private static final int[] LANDMARKS = {R.drawable.run_landmark1, R.drawable.run_landmark2, R.drawable.run_landmark3, R.drawable.run_landmark4,
            R.drawable.run_landmark5, R.drawable.run_landmark6, R.drawable.run_landmark7, R.drawable.run_landmark8, R.drawable.run_landmark9,
            R.drawable.run_landmark10, R.drawable.run_landmark11, R.drawable.run_landmark12};
    private Bitmap landmark, villageArch;
    private int landmarkOf = -1;

    private void drawVillagePiece(Canvas canvas, int[] decor, RunLevels.Level level) {
        float x = (decor[1] + .5f - cameraX) * tile, feet = (decor[2] + 1 - cameraY) * tile;
        paint.setAlpha(255);
        if (decor[0] == 400) {
            if (villageArch == null) villageArch = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_village_arch);
            float wide = tile * 4.2f, tall = wide * villageArch.getHeight() / villageArch.getWidth();
            feet += grounded(villageArch, tall);
            target.set(x - wide / 2, feet - tall, x + wide / 2, feet);
            canvas.drawBitmap(villageArch, null, target, paint);
            // Its name on its board, in letters burnt into the wood.
            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setColor(0xFF4A2A12);
            fitted(canvas, RunLevels.VILLAGES[Math.max(0, Math.min(RunLevels.VILLAGES.length - 1, level.chapter))], x, feet - tall * ARCH_BOARD, tile * .42f, wide * .62f);
            text.setTypeface(Typeface.DEFAULT);
            text.setTextAlign(Paint.Align.LEFT);
            return;
        }
        int chapter = Math.max(0, Math.min(LANDMARKS.length - 1, decor[0] - 410));
        if (landmarkOf != chapter) {
            if (landmark != null) landmark.recycle();
            landmark = BitmapFactory.decodeResource(context.getResources(), LANDMARKS[chapter]);
            landmarkOf = chapter;
        }
        float tall = tile * 5.2f, wide = tall * landmark.getWidth() / landmark.getHeight();
        feet += grounded(landmark, tall);
        target.set(x - wide / 2, feet - tall, x + wide / 2, feet);
        canvas.drawBitmap(landmark, null, target, paint);
    }

    /** Where the village arch's board is, down from its top, as a part of its height. */
    static final float ARCH_BOARD = .79f;

    /** The furniture of the buildings, drawn with the rest of their pictures: Nabi's counter, Inju's desk, the board of hunts, and the anvil each smith works at. */
    private Bitmap counterPets, deskGuild, boardHunts, anvilSmith;

    private void furniture() {
        if (counterPets != null) return;
        counterPets = fxOf(R.drawable.run_counter_pets);
        deskGuild = fxOf(R.drawable.run_desk_guild);
        boardHunts = fxOf(R.drawable.run_board_hunts);
        anvilSmith = fxOf(R.drawable.run_anvil_smith);
    }

    /** A piece of furniture standing on the floor at a place, its middle there, so wide. */
    private void standing(Canvas canvas, Bitmap picture, float x, float floor, float wide) {
        float tall = wide * picture.getHeight() / picture.getWidth();
        floor += grounded(picture, tall);
        paint.setAlpha(255);
        target.set(x - wide / 2, floor - tall, x + wide / 2, floor);
        canvas.drawBitmap(picture, null, target, paint);
    }

    /** For each picture, the part of its height at the bottom that is clear across all of it. */
    private final java.util.Map<Bitmap, Float> clearBottoms = new java.util.IdentityHashMap<>();

    private float clearBottom(Bitmap picture) {
        Float known = clearBottoms.get(picture);
        if (known != null) return known;
        int wide = picture.getWidth(), tall = picture.getHeight(), from = tall * 2 / 3;
        int[] pixels = new int[wide * (tall - from)];
        float share = 0;
        try {
            picture.getPixels(pixels, 0, wide, 0, from, wide, tall - from);
            share = Standing.clearRows(pixels, wide, tall - from) / (float) tall;
        } catch (RuntimeException unreadable) {
            // (A picture whose pixels cannot be read stands where it is drawn.)
        }
        clearBottoms.put(picture, share);
        return share;
    }

    /** How much lower a picture drawn this tall stands, so that its lowest drawn row is on the ground (and a pixel into it). */
    private float grounded(Bitmap picture, float tall) { return tall * clearBottom(picture) + dp(1); }

    /** The pet shop's pedestals, one for each pet, its cushion in that pet's colour, and its shelf. How far up a pedestal its cushion's top is, as a part of its width. */
    private Bitmap pedestals, petShelf;
    static final float PEDESTAL_SEAT = .62f;

    /** Whether she is in a pet shop. */
    private boolean inPetShop() {
        if (run == null || run.room() < 0) return false;
        int[] in = run.level().rooms.get(run.room());
        return in[0] == RunLevels.HOUSE && in.length > 14 && in[14] == RunLevels.PET_SHOP;
    }

    /** The shelf the upper pets stand on, of the shop's honey wood, drawn once along the whole of a ledge, its plank's top where the ledge's top is. */
    private void drawShelf(Canvas canvas, int column, int row, float y) {
        if (petShelf == null) petShelf = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_pet_shelf);
        int from = column, to = column;
        while (run.at(from - 1, row) == RunLevels.LEDGE) from--;
        while (run.at(to + 1, row) == RunLevels.LEDGE) to++;
        float left = (from - cameraX) * tile - tile * .15f, right = (to + 1 - cameraX) * tile + tile * .15f;
        float tall = (right - left) * petShelf.getHeight() / petShelf.getWidth();
        paint.setAlpha(255);
        target.set(left, y - tall * SHELF_TOP, right, y - tall * SHELF_TOP + tall);
        canvas.drawBitmap(petShelf, null, target, paint);
    }

    /** Where the plank's top lies in the shelf's picture, as a part of its height. */
    static final float SHELF_TOP = .04f;

    /** Where the three notices of the board lie on its picture, as parts of it across and down. */
    private static final float[] NOTICES = {.272f, .499f, .724f};
    private static final float NOTICE_WIDE = .177f, NOTICE_TOP = .306f, NOTICE_BOTTOM = .570f;

    /** The board of hunts as it stands in the guild hall, with what is on it written on its notices; and over it its name, or, when a hunt of hers is done, that it can be handed in. */
    private void drawBoard(Canvas canvas, int[] item) {
        furniture();
        float x = (item[1] + 1 - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile, wide = tile * 3.3f, tall = wide * boardHunts.getHeight() / boardHunts.getWidth();
        standing(canvas, boardHunts, x, feet, wide);
        float left = x - wide / 2, top = feet - tall;
        boolean any = false;
        paint.setStyle(Paint.Style.FILL);
        for (int slot = 0; slot < Math.min(RunHunts.SLOTS, NOTICES.length); slot++) {
            int hunt = hunt(slot), at = huntAt(slot);
            boolean taken = at > 0, finished = hunt != 0 && at > RunHunts.many(hunt);
            any |= finished;
            float px = left + NOTICES[slot] * wide, half = NOTICE_WIDE * wide / 2, from = top + NOTICE_TOP * tall, to = top + NOTICE_BOTTOM * tall;
            // Taken, the notice is a little pink; done, a little green; and on each, its lines in ink.
            if (taken || finished) {
                paint.setColor(finished ? 0x4D3FBF7A : 0x40F0607A);
                canvas.drawRect(px - half * .86f, from + (to - from) * .06f, px + half * .86f, to - (to - from) * .04f, paint);
            }
            paint.setColor(0x6B4A2A12);
            for (int line = 0; line < 4; line++) {
                float y = from + (to - from) * (.3f + line * .15f);
                canvas.drawRect(px - half * .6f, y, px + half * (line == 3 ? .1f : .6f), y + (to - from) * .045f, paint);
            }
            // Done: a green seal stamped in its corner.
            if (finished) {
                paint.setColor(0xFF2E9E6E);
                canvas.drawCircle(px + half * .45f, to - (to - from) * .2f, half * .34f, paint);
                paint.setColor(0xFFDDF2E4);
                canvas.drawCircle(px + half * .45f, to - (to - from) * .2f, half * .16f, paint);
            }
        }
        paint.setColor(Color.BLACK);
        if (any) mark(canvas, x, top - tile * .25f - Math.abs((float) Math.sin(clock * 5)) * tile * .12f, "Hand in!", 0xFFE0A21A, false);
        else mark(canvas, x, top - tile * .25f, "Hunts", 0xFF8A5A33, false);
    }

    /** The ribbons a place off this land gives are tied on, there and then: one colour in the first volume, two after; never white. What they are, in words. */
    private String ribbons() {
        int[] given = RunLevels.ribbonsOf(number);
        for (int colour : given) run.lend(colour);
        return given.length > 1 ? FORM_NAMES[given[0] - 1] + " and " + FORM_NAMES[given[1] - 1].toLowerCase(java.util.Locale.ROOT) + " ribbons" : "A " + FORM_NAMES[given[0] - 1].toLowerCase(java.util.Locale.ROOT) + " ribbon";
    }

    /** How long a card that names something won is up. */
    private static final float TOAST = 2.8f;

    /** Says in a card of its own that something has been won: at once, or when the card that is on the screen has gone. */
    private void toast(String title, String words, int picture) {
        // One card at a time: behind the one that is up, or behind the card of a place off the land.
        boolean bannered = run != null && (run.wavePhase() == PlusRun.ANNOUNCED || run.wavePhase() == PlusRun.CLEARED);
        if (toastLeft > .4f || roomCardAge >= 0 || roomResultAge >= 0 || bannered) {
            toasts.add(new Object[]{title, words, picture});
            return;
        }
        toastTitle = title;
        toastWords = words;
        toastPicture = picture;
        toastLeft = TOAST;
        // What it is worth looking at: rare for a piece of a heart, a life, a medal or one of the three beaten; good for thirty pluses or more.
        String all = (title + " " + words).toLowerCase(java.util.Locale.ROOT);
        int most = 0;
        java.util.regex.Matcher numbers = java.util.regex.Pattern.compile("\\+ (\\d+)").matcher(all);
        while (numbers.find()) most = Math.max(most, Integer.parseInt(numbers.group(1)));
        toastRarity = all.contains("piece of a heart") || all.contains("a life,") || all.contains("medal") || all.contains("is beaten") || all.contains("a heart more") ? 2
                : most >= 30 || all.contains("ribbon") ? 1 : 0;
        toastX = run == null ? 0 : run.x() + PlusRun.WIDE / 2;
        toastY = run == null ? 0 : run.y() - .8f;
        if (run != null && all.contains("piece of a heart")) flyTo((toastX - cameraX) * tile, (toastY - cameraY) * tile, true, 1);
        else if (run != null && most >= 10) flyTo((toastX - cameraX) * tile, (toastY - cameraY) * tile, false, Math.min(10, most / 10 + 2));
        audio.play("star", toastRarity == 2 ? .9f : .6f);
        if (toastRarity == 2) audio.play("win", .5f);
        // And it bursts out of where she is: coins for the plain, light for the rest.
        for (int spark = 0; spark < 8 + 6 * toastRarity && run != null; spark++) {
            double way = random.nextFloat() * Math.PI * 2;
            bits.add(new float[]{toastX, toastY, (float) Math.cos(way) * (4 + toastRarity), (float) Math.sin(way) * (4 + toastRarity) - 3, .55f, 0});
        }
    }

    /** Whether everything asked of her in a chapter has been done; and, the first time that is found to be so, its medal: two hundred pluses, and her colours half a second sooner. */
    private boolean medal(int chapter, boolean said) {
        if (session.tally("run_medal_" + chapter) != 0) return true;
        int asked = 0;
        for (int land = 1; land <= RunLevels.TOTAL; land++) {
            if (RunLevels.chapterOf(land) != chapter || RunLevels.questOf(land) == 0) continue;
            asked++;
            if (session.tally("run_quest_" + land) == 0) return false;
        }
        if (asked == 0) return false;
        session.keep("run_medal_" + chapter, 1);
        session.keep("run_bank", bank() + 200);
        if (said) toast("The medal of " + RunStory.CHAPTERS[chapter], "Everything asked there is done: + 200, and her colours half a second sooner", SCROLL_PICTURE);
        return true;
    }

    /** What doing what is asked in a land pays the first time, in a few words. */
    private String pays(int land) {
        int pays = RunLevels.questPrize(RunLevels.questOf(land));
        return pays == RunLevels.PURSE ? "a purse of " + (40 + 20 * RunLevels.chapterOf(land)) : pays == RunLevels.SHOTS ? "ink guns + 2 shots"
                : pays == RunLevels.LASTING ? "longer treats" : pays == RunLevels.QUICKER ? "quicker colours" : "a white ribbon";
    }

    float dp(float value) { return value * density; }

    private Bitmap armed(int weapon) {
        if (armed[weapon] == null) armed[weapon] = BitmapFactory.decodeResource(context.getResources(), ARMED[weapon]);
        return armed[weapon];
    }

    private Bitmap cast(int who) {
        if (cast[who] == null) cast[who] = BitmapFactory.decodeResource(context.getResources(), CAST[who]);
        return cast[who];
    }

    private Bitmap petArt(int which) {
        if (petCast[which] == null) petCast[which] = BitmapFactory.decodeResource(context.getResources(), PET_CAST[which]);
        return petCast[which];
    }

    private Bitmap flier(int which) {
        if (flierCast[which] == null) flierCast[which] = BitmapFactory.decodeResource(context.getResources(), FLIER_CAST[which]);
        return flierCast[which];
    }

    /** Lucy as she is drawn: as a child, with her dragon side out, and as a dragon. */
    private Bitmap lucy(int which) {
        if (lucyArt[which] == null) {
            lucyArt[which] = BitmapFactory.decodeResource(context.getResources(), which == 0 ? R.drawable.run_lucy_girl : which == 1 ? R.drawable.run_lucy_wings
                    : R.drawable.run_lucy_dragon);
        }
        return lucyArt[which];
    }

    /** Halgakos with both wings grown, from the side or from behind, in the colour she is drawn in: which, for these, is white. */
    private Bitmap herWhite(boolean behind) {
        int which = behind ? 1 : 0;
        if (whiteArt[which] == null) whiteArt[which] = BitmapFactory.decodeResource(context.getResources(), behind ? R.drawable.halgakos_back : R.drawable.halgakos_white);
        Bitmap[] set = looks.get(drawn());
        if (set == null) return dressed(behind ? "back" : "white", whiteArt[which]);
        int slot = 3 + ARMED.length + which;
        if (set[slot] == null) set[slot] = dyed(whiteArt[which], drawn());
        return dressed(behind ? "back" : "white", set[slot]);
    }

    private Bitmap later(int who) {
        if (laterCast[who] == null) laterCast[who] = BitmapFactory.decodeResource(context.getResources(), LATER_CAST[who]);
        return laterCast[who];
    }

    // Idling

    /**
     * Six frames of idling for each of those who stand in the world: as they are drawn, a blink, a glance, two of a habit of their own, and
     * a special thing they do now and then. The villagers, by their place in the sheet of them; the four friends; the three smiths; Nuri; and
     * those of the black arena and the story who stand as friends in the third volume, by their place among the later cast (or none).
     */
    private static final int IDLE_FRAMES = 6;
    private static final int[] IDLE_VILLAGERS = {R.drawable.run_idle_somi, R.drawable.run_idle_inju, R.drawable.run_idle_boksun, R.drawable.run_idle_ttosun,
            R.drawable.run_idle_galpi, R.drawable.run_idle_yeseo, R.drawable.run_idle_nabi, R.drawable.run_idle_moru, R.drawable.run_idle_altteul};
    private static final int[] IDLE_CAST = {R.drawable.run_idle_chikuwa, R.drawable.run_idle_rune, R.drawable.run_idle_illya, R.drawable.run_idle_tsubakieh};
    private static final int[] IDLE_SMITHS = {R.drawable.run_idle_smith_moru, R.drawable.run_idle_smith_pulmu, R.drawable.run_idle_smith_sutdol};
    private static final int[] IDLE_LATER = {R.drawable.run_idle_gokhae, R.drawable.run_idle_miri, R.drawable.run_idle_gari, R.drawable.run_idle_yeobaek, 0,
            R.drawable.run_idle_mawang, R.drawable.run_idle_guard, R.drawable.run_idle_butler, R.drawable.run_idle_byeoru, R.drawable.run_idle_jupan,
            R.drawable.run_idle_sumbi, R.drawable.run_idle_pads, R.drawable.run_idle_party, 0, 0, 0, 0, 0};
    private final java.util.Map<Integer, Bitmap> idleSheets = new java.util.HashMap<>();
    /** For each one idling: when the next thing begins, what is being done (or -1), when it began, and how many things have been done. */
    private final java.util.Map<Integer, float[]> idling = new java.util.HashMap<>();

    private Bitmap idleSheet(int drawable) {
        Bitmap sheet = idleSheets.get(drawable);
        if (sheet == null) {
            sheet = BitmapFactory.decodeResource(context.getResources(), drawable);
            idleSheets.put(drawable, sheet);
        }
        return sheet;
    }

    /** What things take: a blink, a glance, a habit, the special thing. */
    private static final float[] IDLE_LASTS = {.14f, 1.2f, 1.6f, 1.8f};

    /**
     * The frame one of them is at, by who it is: as drawn most of the time; and, at moments of its own (never two of them together), a
     * blink, a glance, its habit back and forth, or its special thing. Only the drawn frame changes; nothing is moved.
     */
    private int idleFrame(int id) {
        float[] state = idling.get(id);
        if (state == null) {
            java.util.Random first = new java.util.Random(id * 7919L);
            state = new float[]{clock + .5f + first.nextFloat() * 4, -1, 0, 0};
            idling.put(id, state);
        }
        if (state[0] - clock > 60) state[0] = clock + 2;
        if (state[1] < 0 && clock >= state[0]) {
            java.util.Random dice = new java.util.Random(id * 104729L + (long) state[3]++);
            int roll = dice.nextInt(100);
            state[1] = roll < 50 ? 0 : roll < 65 ? 1 : roll < 90 ? 2 : 3;
            state[2] = clock;
        }
        if (state[1] < 0) return 0;
        int doing = (int) state[1];
        float into = clock - state[2];
        if (into >= IDLE_LASTS[doing] || into < 0) {
            java.util.Random dice = new java.util.Random(id * 15485863L + (long) state[3]);
            state[1] = -1;
            state[0] = clock + 1.4f + dice.nextFloat() * 3.6f;
            return 0;
        }
        return doing == 0 ? 1 : doing == 1 ? 2 : doing == 2 ? (into % .8f < .4f ? 3 : 4) : 5;
    }

    private Bitmap villagers() {
        if (villagers == null) villagers = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_npc);
        return villagers;
    }

    /**
     * The pictures of her with a thing in her hands, in a colour. The whip is a red ribbon,
     * and red is near enough to her pink that the dye takes it in places: so, for the whip,
     * whatever was strongly red before the dye is put back as it was. The ribbon is red in
     * every colour of her hair; and nothing of the dye itself is changed.
     */
    private static Bitmap dyedArm(Bitmap from, int weapon, int form) {
        Bitmap dyed = dyed(from, form);
        if (weapon != PlusRun.WHIP || dyed == from) return dyed;
        int wide = from.getWidth(), tall = from.getHeight();
        if (!dyed.isMutable()) dyed = dyed.copy(Bitmap.Config.ARGB_8888, true);
        int[] was = new int[wide], now = new int[wide];
        for (int row = 0; row < tall; row++) {
            from.getPixels(was, 0, wide, 0, row, wide, 1);
            dyed.getPixels(now, 0, wide, 0, row, wide, 1);
            boolean any = false;
            for (int column = 0; column < wide; column++) {
                int pixel = was[column], red = pixel >> 16 & 255, green = pixel >> 8 & 255, blue = pixel & 255;
                if (pixel >>> 24 < 40 || red <= 120 || red - green <= 95 || red - blue <= 75 || now[column] == pixel) continue;
                now[column] = pixel;
                any = true;
            }
            if (any) dyed.setPixels(now, 0, wide, 0, row, wide, 1);
        }
        return dyed;
    }

    /** A picture of her with her hair the colour of a form: every pink of her hair, ears and tail, and nothing else, is turned to it. */
    private static Bitmap dyed(Bitmap from, int form) { return Hair.dyed(from, form); }

    /**
     * The colour she is to be drawn in now: the one wanted, if the pictures of her in it are
     * ready. If they are not, they are begun, away from the screen, and until then she is
     * drawn as she was, so that the one can fade into the other when it comes.
     */
    private int formDrawn() {
        int form = lookWanted();
        if (form == 0 || looks.get(form) != null) return form;
        dye(form);
        return look != 0 && looks.get(look) != null ? look : 0;
    }

    /** The pictures of her in a colour are made, away from the screen, if they are not there and not being made. */
    private void dye(int form) {
        if (form == 0 || looks.get(form) != null || !dyeing.add(form)) return;
        if (petRows == null) petRows = Bitmap.createBitmap(pet, 0, 0, pet.getWidth(), pet.getHeight() * 3 / PetArt.FRAMES.length);
        Bitmap rows = petRows, jump = this.jump, lose = this.lose;
        int weapon = run == null ? PlusRun.UNARMED : run.weapon();
        Bitmap carried = weapon == PlusRun.UNARMED ? null : armed(weapon);
        new Thread(() -> {
            Bitmap[] set = new Bitmap[3 + ARMED.length + 2];
            set[0] = dyed(rows, form);
            set[1] = dyed(jump, form);
            set[2] = dyed(lose, form);
            if (carried != null) set[3 + weapon] = dyedArm(carried, weapon, form);
            stage.post(() -> {
                // No more than eight are kept: one she is not in, nor was a moment ago, makes room.
                for (int index = looks.size() - 1; index >= 0 && looks.size() >= 8; index--) {
                    int kept = looks.keyAt(index);
                    if (kept != look && kept != lookFrom && kept != knockForm) looks.removeAt(index);
                }
                looks.put(form, set);
                dyeing.remove(form);
            });
        }).start();
    }

    /**
     * The colour two worn together make, when that is a colour and not mud: the hue half
     * way between theirs the short way round, as strong and as bright as they are between
     * them. None, when they lie across from each other, or there are more than two: then
     * her hair does not blend, but goes from one to the next.
     */
    private static int blend(int mix) {
        if (Integer.bitCount(mix) != 2) return 0;
        float[] first = new float[3], second = new float[3];
        Color.colorToHSV(FORM_COLOURS[Integer.numberOfTrailingZeros(mix)], first);
        Color.colorToHSV(FORM_COLOURS[Integer.numberOfTrailingZeros(Integer.highestOneBit(mix))], second);
        float apart = Math.abs(first[0] - second[0]);
        if (apart > 180) apart = 360 - apart;
        if (apart > 150) return 0;
        float hue = Math.abs(first[0] - second[0]) > 180 ? (first[0] + second[0] + 360) / 2 % 360 : (first[0] + second[0]) / 2;
        return Color.HSVToColor(new float[]{hue, Math.max((first[1] + second[1]) / 2, .55f), Math.max((first[2] + second[2]) / 2, .8f)});
    }

    /**
     * How she is to look: in her own hair, 0; in one colour, that colour, by its number;
     * in white, white; under black that was put on her, black. Wearing two that blend, the
     * colour they make, as a colour in itself. Wearing more, or two that do not blend, each
     * of them in turn, a little under a second each, the one fading into the next: so that
     * her hair is always the colours she wears. Black of her own is not one of them: it
     * is the shadow she is drawn as.
     */
    private int lookWanted() {
        if (run == null) return 0;
        if (run.cursed() > 0) return PlusRun.BLACK;
        if (run.white()) return PlusRun.WHITE;
        int mix = run.mix(), bright = mix & ~(1 << PlusRun.BLACK - 1), count = Integer.bitCount(bright);
        if (mix == 0) return 0;
        if (count == 0) return PlusRun.BLACK;
        if (count == 1) return Integer.numberOfTrailingZeros(bright) + 1;
        int blended = blend(bright);
        if (blended != 0) return blended;
        // Each in turn: and the pictures of her in every one of them are made beforehand.
        int turn = (int) (clock / .9f) % count, wanted = 0;
        for (int colour = PlusRun.RED; colour < PlusRun.BLACK; colour++) {
            if ((bright & 1 << colour - 1) == 0) continue;
            dye(colour);
            if (turn-- == 0) wanted = colour;
        }
        return wanted;
    }

    /** The colour itself of a way she may look. */
    private static int lookColour(int look) { return look == 0 ? CalculatorLayout.CORAL : look > 0 && look <= PlusRun.WHITE ? FORM_COLOURS[look - 1] : look; }

    /** The colour a picture of her is to be drawn in at this moment. */
    private int drawn() { return drawnAs != NO_LOOK ? drawnAs : run == null || screen == MAP || screen == SHOP ? 0 : look; }

    Bitmap herPet() {
        Bitmap[] set = looks.get(drawn());
        return dressed("pet", set != null ? set[0] : pet);
    }

    private Bitmap herJump() {
        Bitmap[] set = looks.get(drawn());
        return dressed("jump", set != null ? set[1] : jump);
    }

    /** The sheet of her hurt, thrown and fallen, in a colour: so that she does not lose in pink what she was wearing in red. */
    private Bitmap herLose(int form) {
        Bitmap[] set = looks.get(form);
        return dressed("lose", set != null ? set[2] : lose);
    }

    private Bitmap herArmed(int weapon) {
        int form = drawn();
        Bitmap[] set = looks.get(form);
        if (set == null) return dressed("arm" + weapon, armed(weapon));
        if (set[3 + weapon] == null) set[3 + weapon] = dyedArm(armed(weapon), weapon, form);
        return dressed("arm" + weapon, set[3 + weapon]);
    }

    /** Vanity: what she is dressed in at the moment (her own look, her gear, or her chosen outfit), and a sheet of her in it. See {@link RunVanity}. */
    private VanityArt vanityArt;
    private RunVanity.Style vanityStyle;
    private long vanityAt = -1000;

    private RunVanity.Style vanityNow() {
        long now = System.currentTimeMillis();
        if (now - vanityAt > 250 || now < vanityAt) {
            vanityAt = now;
            RunVanity.Keep kept = RunVanity.on(session);
            vanityStyle = RunVanity.styleFor(RunVanity.mode(kept), RunVanity.outfit(kept), RunVanity.mode(kept) == RunVanity.GEAR ? wornNow() : null, RunVanity.ears(kept), RunVanity.horns(kept));
        }
        return vanityStyle;
    }

    /** Her hands over a weapon she holds, in what she is dressed in: where the layer has her hand or her cuff, what her dressed frame has there. */
    private Bitmap dressedOver(Bitmap dressedBody, Bitmap over) {
        RunVanity.Style style = vanityNow();
        if (style == null || vanityArt == null) return over;
        return vanityArt.over(dressedBody, over, style);
    }

    private Bitmap dressed(String sheet, Bitmap from) {
        RunVanity.Style style = vanityNow();
        if (style == null) return from;
        if (vanityArt == null) vanityArt = new VanityArt(context);
        return vanityArt.sheet(sheet, from, style);
    }

    /** Who waits at the end of a land, by her colour, her name, and where she is among those who speak (below nothing for Zero and King Slime, who do not). */
    private static int waitColour(int kind) {
        return kind == RunLevels.ZERO ? CalculatorLayout.CORAL : kind == RunLevels.KING ? 0xFF3FA878 : kind == RunLevels.WYRM ? 0xFFB98A4E
                : kind == RunLevels.BLOT ? 0xFF4A55C4 : kind == RunLevels.GECKO ? 0xFF4FAE6A : SPEAKER_COLOURS[waitFace(kind)];
    }

    private static String waitName(int kind) {
        return kind == RunLevels.LUCY ? "Lucy" : kind == RunLevels.WYRM ? "The Margin Wyrm" : kind == RunLevels.BLOT ? "Blot" : kind == RunLevels.GECKO ? "The Bookmark Gecko"
                : kind == RunLevels.ZERO ? "Zero" : kind == RunLevels.ILLYA ? "Illya" : kind == RunLevels.TSUBAKIEH ? "Tsubakieh"
                : kind == RunLevels.GOKHAE ? "Gokhae" : kind == RunLevels.MIRI || kind == RunLevels.GARI ? "The Gazette Twins"
                : kind == RunLevels.YEOBAEK ? "Yeobaek" : kind == RunLevels.KING ? "King Slime" : kind == RunLevels.GUARD ? "The Guild Guard"
                : kind == RunLevels.BUTLER ? "Penchok-jipsa" : kind == RunLevels.BYEORU ? "Byeoru" : kind == RunLevels.JUPAN ? "Jupan"
                : kind == RunLevels.SUMBI ? "Sumbi" : kind == RunLevels.PADS ? "The Brake Pads" : kind == RunLevels.PARTY ? "The Straight-Ahead Party"
                : kind == RunLevels.OLD_BOOK ? "The Old Book" : "The Mawang";
    }

    private static int waitFace(int kind) {
        return kind == RunLevels.LUCY ? LUCY_FACE : kind == RunLevels.ILLYA ? 3 : kind == RunLevels.TSUBAKIEH ? 4 : kind == RunLevels.GOKHAE ? 6 : kind == RunLevels.MIRI ? 7
                : kind == RunLevels.GARI ? 8 : kind == RunLevels.YEOBAEK ? 9 : kind == RunLevels.MAWANG ? 5
                : kind >= RunLevels.GUARD && kind <= RunLevels.OLD_BOOK ? FIRST_OTHER + kind - RunLevels.GUARD : -1;
    }

    private Bitmap face(int speaker) {
        if (speaker == 0) return portrait;
        if (faces[speaker] == null) faces[speaker] = BitmapFactory.decodeResource(context.getResources(), FACES[speaker]);
        return faces[speaker];
    }

    // The screen

    void layout(int width, int height, RectF insets) {
        // (The gallery, open while the screen changes shape, is laid again at the new shape: it may be a window of its own, which
        // keeps the size it was given.)
        boolean reshaped = shelf != null && !shelf.closed() && (width != this.width || height != this.height);
        if (reshaped) shelf.close();
        if (boutique != null && !boutique.closed() && (width != this.width || height != this.height)) boutique.close();
        this.width = width;
        this.height = height;
        safe.set(insets);
        // Squares large enough to see, small enough that nine of them show across and the land's height fits.
        tile = Math.max(dp(30), Math.min(dp(64), Math.min(height / (float) RunLevels.ROWS, width / 9f)));
        // Six buttons along the bottom, all in the one row, which lies over the ground and never over where she walks: the arrows on the
        // left, with room between them; and on the right the jump, what she carries beside it, the ribbon that calls on a colour beside
        // that, and beside that again the arrow that steps to the next colour, a button of its own, forty-four across at the least and
        // touching none of the others. On a narrow screen they are smaller and closer, so that all six fit.
        boolean narrow = safe.width() < dp(430);
        float edge = dp(narrow ? 12 : 18), apart = dp(narrow ? 10 : 26), between = dp(narrow ? 8 : 16), stepper = dp(44);
        float size = Math.min(dp(72), (safe.width() - edge * 2 - apart - between * 3 - dp(8) - stepper) / 5);
        float y = safe.bottom - dp(24) - size;
        leftButton.set(safe.left + edge, y, safe.left + edge + size, y + size);
        rightButton.set(leftButton.right + apart, y, leftButton.right + apart + size, y + size);
        jumpButton.set(safe.right - edge - size, y, safe.right - edge, y + size);
        strikeButton.set(jumpButton.left - between - size, y, jumpButton.left - between, y + size);
        float small = dp(44), margin = dp(10);
        pauseButton.set(safe.right - margin - small, safe.top + margin, safe.right - margin, safe.top + margin + small);
        closeButton.set(pauseButton);
        soundButton.set(closeButton.left - dp(8) - small, closeButton.top, closeButton.left - dp(8), closeButton.bottom);
        // The map: a page for each volume, with a row of lands for each of its chapters under the chapter's name, and the button that goes on under them all.
        // (On a screen wider than it is tall, the width is used: its chapters side by side, each in rows of its own, the lands as large as fit.)
        float room = safe.height() - dp(92) - dp(150) - dp(44);
        for (int volume = 0; volume < 3; volume++) mapPerRow[volume] = eachOf(volume);
        mapShort = safe.width() > safe.height() * 1.15f && safe.height() < dp(460);
        float mapTop = mapShort ? dp(94) : dp(136);
        for (int volume = 0; volume < 3 && safe.width() > safe.height() * 1.15f; volume++) {
            int each = eachOf(volume), from = firstOf(volume), chapters = (lastOf(volume) - from) / each;
            float gap = dp(7), colGap = dp(22), label = dp(24), bandGap = dp(10), roomH = safe.height() - mapTop - dp(74), availW = safe.width() - dp(36);
            float best = 0;
            int bestCols = 1, bestPer = each;
            for (int cols = 1; cols <= chapters; cols++) {
                for (int per : new int[]{each, (each + 1) / 2, (each + 2) / 3}) {
                    int rows = (each + per - 1) / per, bands = (chapters + cols - 1) / cols;
                    float colW = (availW - colGap * (cols - 1)) / cols;
                    if (colW < dp(110)) continue;
                    float side = Math.min(dp(84), Math.min((colW - gap * (per - 1)) / per, (roomH / bands - label - bandGap - gap * (rows - 1)) / rows));
                    if (side > best + dp(1)) {
                        best = side;
                        bestCols = cols;
                        bestPer = per;
                    }
                }
            }
            float side = Math.max(dp(22), best);
            int cols = bestCols, per = bestPer, rows = (each + per - 1) / per;
            mapPerRow[volume] = per;
            float blockW = per * side + gap * (per - 1), blockH = label + rows * side + gap * (rows - 1) + bandGap;
            float left = safe.centerX() - (cols * blockW + colGap * (cols - 1)) / 2, top = safe.top + mapTop;
            for (int index = 0; index < lastOf(volume) - from; index++) {
                int chapter = index / each, at = index % each, col = chapter % cols, band = chapter / cols;
                float x = left + col * (blockW + colGap) + at % per * (side + gap), down = top + band * blockH + label + at / per * (side + gap);
                lands[from + index].set(x, down, x + side, down + side);
            }
        }
        for (int volume = 0; volume < 3 && safe.width() <= safe.height() * 1.15f; volume++) {
            int each = eachOf(volume), from = firstOf(volume);
            float gap = volume == 0 ? dp(8) : dp(5);
            float side = Math.max(dp(22), Math.min(dp(54), Math.min((safe.width() - dp(28) - gap * (each - 1)) / each, room / RunLevels.CHAPTERS - dp(34))));
            float left = safe.centerX() - (side * each + gap * (each - 1)) / 2, top = safe.top + dp(92) + dp(44);
            for (int index = 0; index < lastOf(volume) - from; index++) {
                float x = left + index % each * (side + gap), down = top + index / each * (side + dp(34)) + dp(22);
                lands[from + index].set(x, down, x + side, down + side);
            }
        }
        // (The tabs leave room at the end of their row for the buttons that open her skill tree and her backpack.)
        float tab = Math.min(dp(132), (safe.width() - dp(44) - dp(84) - dp(52)) / 2);
        tabs[0].set(safe.left + dp(18), safe.top + dp(80), safe.left + dp(18) + tab, safe.top + dp(114));
        tabs[1].set(tabs[0].right + dp(8), tabs[0].top, tabs[0].right + dp(8) + tab, tabs[0].bottom);
        gearButton.set(safe.right - dp(18) - dp(76), tabs[0].top, safe.right - dp(18), tabs[0].bottom);
        skillButton.set(gearButton.left - dp(8) - dp(44), tabs[0].top, gearButton.left - dp(8), tabs[0].bottom);
        // (With the third volume there are three of them, narrower.)
        float third = Math.min(dp(96), (safe.width() - dp(36) - dp(84) - dp(52) - dp(12)) / 3);
        for (int volume = 0; volume < 3; volume++) {
            tabs3[volume].set(safe.left + dp(18) + volume * (third + dp(6)), tabs[0].top, safe.left + dp(18) + volume * (third + dp(6)) + third, tabs[0].bottom);
        }
        if (mapShort) {
            // (A phone on its side: the tabs, the tree and the backpack in a row at the right, beside the line under the name, so the lands have the height.)
            float y0 = safe.top + dp(58), y1 = y0 + dp(32), tabWide = dp(100);
            gearButton.set(safe.right - dp(18) - dp(76), y0, safe.right - dp(18), y1);
            skillButton.set(gearButton.left - dp(8) - dp(44), y0, gearButton.left - dp(8), y1);
            float right = skillButton.left - dp(10);
            for (int volume = 2; volume >= 0; volume--) {
                tabs3[volume].set(right - tabWide * .86f, y0, right, y1);
                right = tabs3[volume].left - dp(6);
            }
            right = skillButton.left - dp(10);
            for (int volume = 1; volume >= 0; volume--) {
                tabs[volume].set(right - tabWide, y0, right, y1);
                right = tabs[volume].left - dp(6);
            }
        }
        // The button that lets her down through a thin ledge, and that slams the ground from the air: with the strike and the jump, over the
        // gap between them, so that the thumb that strikes rolls onto it. (The arrows' thumb has only the arrows.)
        float downs = Math.max(dp(46), size * .66f), gap = (strikeButton.right + jumpButton.left) / 2;
        downButton.set(gap - downs / 2, y - dp(10) - downs, gap + downs / 2, y - dp(10));
        // The button that changes what she holds: over the one that uses it, a little smaller, and to the left of the down button.
        float swaps = Math.max(dp(44), size * .66f), swapRight = Math.min(strikeButton.centerX() + swaps / 2, downButton.left - dp(8));
        swapButton.set(swapRight - swaps, strikeButton.top - dp(12) - swaps, swapRight, strikeButton.top - dp(12));
        // Her active slots, in a row to the left of it, the first leftmost.
        float slotSide = Math.max(dp(40), size * .6f), slotGap = dp(6), slotsFrom = swapButton.left - between - 3 * slotSide - 2 * slotGap;
        for (int slot = 0; slot < 3; slot++) {
            float slotLeft = slotsFrom + slot * (slotSide + slotGap);
            slotPlaces[slot].set(slotLeft, swapButton.centerY() - slotSide / 2, slotLeft + slotSide, swapButton.centerY() + slotSide / 2);
        }
        turnTo(page);
        // The stall in a land, or a door: its button over the jump, where her thumb already is, and over the down button too.
        float stallBottom = Math.min(jumpButton.top - dp(14), downButton.top - dp(8));
        stallButton.set(jumpButton.left, stallBottom - size, jumpButton.right, stallBottom);
        // The fifth button, on the right in the row with the rest, so that it never lies over her or over what she is about to walk into:
        // the ribbon that calls on a colour of hers, with a small one on its corner that chooses which.
        float ribbon = size * .9f;
        colourButton.set(strikeButton.left - between - ribbon, y + (size - ribbon) / 2, strikeButton.left - between, y + (size + ribbon) / 2);
        // Beside it, clear of it and of every other button, the arrow that steps to the next colour.
        float arrow = Math.max(stepper, Math.min(ribbon * .7f, size * .66f));
        nextButton.set(colourButton.left - between - arrow, y + (size - arrow) / 2, colourButton.left - between, y + (size + arrow) / 2);
        // The counter: what can be bought, one under another.
        float high = Math.max(dp(40), Math.min(dp(74), (safe.height() - dp(170)) / 6 - dp(8)));
        float across = Math.min(safe.width() - dp(28), dp(460));
        for (int index = 0; index < wares.length; index++) {
            float down = safe.top + dp(142) + index * (high + dp(8));
            wares[index].set(safe.centerX() - across / 2, down, safe.centerX() + across / 2, down + high);
        }
        if (reshaped) stage.post(this::openGallery);
    }

    /**
     * Shows the lands of a volume, with the button that goes on to the next land under the
     * last of them, where a thumb goes first. The counter is up beside the sound, a little
     * lower than the round buttons so that it clears the line under the heading.
     */
    private void turnTo(int volume) {
        page = volume;
        int from = firstOf(volume), last = lastOf(volume) - 1;
        float lowest = 0, leftmost = Float.MAX_VALUE, rightmost = 0;
        for (int index = from; index <= last; index++) {
            lowest = Math.max(lowest, lands[index].bottom);
            leftmost = Math.min(leftmost, lands[index].left);
            rightmost = Math.max(rightmost, lands[index].right);
        }
        float under = lowest + dp(16);
        // (In landscape, clear of the way into the gallery at the foot on the right.)
        if (safe.width() > safe.height() * 1.15f) rightmost = Math.min(rightmost, safe.right - dp(14) - dp(66) - dp(12));
        goButton.set(leftmost, under, rightmost, under + dp(48));
        text.setTextSize(dp(26));
        float from0 = safe.left + dp(18) + text.measureText("Plus run") + dp(12), to = soundButton.left - dp(8);
        shopButton.set(Math.max(from0, to - dp(160)), soundButton.top + dp(3), to, soundButton.bottom - dp(4));
        // The way into the gallery: her cringing face with its name under it, at the foot of the map on the right, across from her
        // and clear of what she says. Where there is little room under the lands it is smaller, and loses its name before its face.
        float foot = safe.bottom - dp(16), tall = Math.min(dp(78), Math.max(dp(46), foot - goButton.bottom - dp(10)));
        float across = Math.min(dp(66), tall);
        cringeButton.set(safe.right - dp(14) - across, foot - tall, safe.right - dp(14), foot);
    }

    void close() {
        if (shelf != null) shelf.close();
        if (boutique != null) boutique.close();
        audio.close();
    }

    /** Left for the home screen: a run stops on its pause, and whoever was speaking stops, so that it can be come back to as it stands. */
    void hold() {
        // (A clip being watched is over with: what follows it waits to be gone on with. So is a film; and the gallery is shut.)
        if (video != null) video.skip();
        if (shelf != null) shelf.close();
        if (boutique != null) boutique.close();
        if (screen == PLAY) pause();
        audio.hush();
    }

    /** The Back key: out of a run to its pause, out of the story, the counter or a card to the map; false to leave the game. */
    boolean back() {
        // A clip being watched is what Back ends, as a tap does.
        if (video != null) {
            video.skip();
            return true;
        }
        // The boutique: Back is out of it, to the counter.
        if (boutique != null && !boutique.closed()) {
            boutique.close();
            return true;
        }
        // The gallery: Back is out of a picture being looked at, and then out of the gallery, to the map.
        if (shelf != null && !shelf.closed()) {
            if (!shelf.back()) shelf.close();
            audio.play("tap", .3f);
            return true;
        }
        // Whatever sheet is open over the screen is what Back closes: her skill tree, her backpack, or the colours opened out.
        if (treeOpen) {
            treeOpen = false;
            audio.play("tap", .3f);
            return true;
        }
        if (packOpen) {
            if (!inventory.back()) packOpen = false;
            audio.play("tap", .3f);
            return true;
        }
        if (bagOpen) {
            bagOpen = false;
            audio.play("tap", .3f);
            return true;
        }
        if (huntsOpen) {
            closeHunts();
            return true;
        }
        if (smithOpen) {
            smithOpen = false;
            audio.play("tap", .3f);
            return true;
        }
        if (marketOpen) {
            marketOpen = false;
            audio.play("tap", .3f);
            return true;
        }
        if (fanOpen) {
            fanOpen = false;
            idle();
            return true;
        }
        if (screen == MAP && warping) {
            warpTo(0);
            return true;
        }
        if (screen == PLAY) {
            pause();
        } else if (screen == PAUSED) {
            screen = PLAY;
        } else if (screen == STORY) {
            endStory();
        } else if (screen == STALL || screen == DELVE && delveCard == 0) {
            screen = PLAY;
        } else if (screen == DELVE) {
            // (Between floors a blessing must be chosen.)
            return true;
        } else if (screen == SHOP && counterFrom == STALL) {
            regrant();
            screen = STALL;
        } else if (screen == SHOP && counterFrom == PLAY) {
            regrant();
            screen = PLAY;
        } else if (screen == RESULT || screen == SHOP) {
            toMap();
        } else {
            return false;
        }
        return true;
    }

    private void toMap() {
        blackRung = 0;
        delve = -1;
        audio.hush();
        screen = MAP;
        session.refresh();
        news();
    }

    // The films and the gallery

    /** Everything in the gallery: what is always there, and the pictures of the table kept with them. */
    private List<RunGallery.Item> gallery() {
        if (gallery == null) gallery = RunGallery.all(RunShelf.text(context, "gallery/pictures.tsv"), RunShelf.text(context, "clips/clips.tsv"));
        return gallery;
    }

    /** The gallery is opened over the map. (In a build made for testing, with everything in it open if that is asked for.) */
    private void openGallery() {
        if (shelf != null && !shelf.closed()) return;
        audio.hush();
        galleryShown = 0;
        int[] edges = {Math.round(safe.left), Math.round(safe.top), Math.round(width - safe.right), Math.round(height - safe.bottom)};
        shelf = RunShelf.over(stage, session, dark, BuildConfig.DEBUG && session.tally("run_gallery") == 1, edges, new RunShelf.Host() {
            @Override public void play(int film, android.view.View over) { showFilm(film, over, null); }

            @Override public void tap() { audio.play("tap", .5f); }
        });
    }

    /**
     * A film is played, over the game or over the gallery, and then something is done. Which picture is its picture goes by the
     * shape of what it is played over as it begins, and is not changed while it plays. Its sound is the one chosen for it, in the
     * language chosen if that has been recorded and in English if not; with the game's sound off it is silent. What is written
     * under it is in the language chosen for that, if what is heard has been written out in it; if not, nothing is. That it has
     * been played is kept, and is what opens it in the gallery; if it could not be played nothing is kept, and the game goes on.
     */
    private void showFilm(int film, android.view.View over, Runnable then) {
        if (video != null) return;
        if (film >= RunClips.BASE) {
            showClip(film - RunClips.BASE, over, then);
            return;
        }
        audio.hush();
        captionLeft = 0;
        RunVideo.Ends ends = played -> {
            video = null;
            if (played) {
                session.keep(RunFilms.SEEN, session.tally(RunFilms.SEEN) | RunFilms.bit(film));
                news();
            }
            if (shelf != null && !shelf.closed()) shelf.fill();
            if (then != null) then.run();
        };
        int script = session.tally(RunFilms.CAPTIONS);
        if (film == RunFilms.CLIP) {
            boolean heard = audio.on() && Chatter.DEFAULT_NAME.equals(names.nickname());
            video = RunVideo.over(over, R.raw.run_cut_mother, heard, script < RunFilms.CLIP_SAYS.length ? names.named(RunFilms.CLIP_SAYS[script]) : null, ends);
            return;
        }
        boolean upright = RunFilms.upright(over.getWidth(), over.getHeight());
        String shown = RunFilms.picture(film, upright, session.tally(RunFilms.SOUND + film), session.tally(RunFilms.TONGUE)), sound = null;
        int picture = RunShelf.raw(context, "run_film_" + shown), voice = 0;
        if (picture == 0 && !shown.equals(RunFilms.picture(film, upright))) {
            // (Where the picture made for a language is not there, the usual one is shown.)
            shown = RunFilms.picture(film, upright);
            picture = RunShelf.raw(context, "run_film_" + shown);
        }
        if (picture == 0) {
            ends.ended(false);
            return;
        }
        for (String name : RunFilms.sounds(film, session.tally(RunFilms.SOUND + film), session.tally(RunFilms.TONGUE), upright)) {
            if (voice == 0 && RunShelf.raw(context, "run_film_" + name) != 0) {
                sound = name;
                voice = RunShelf.raw(context, "run_film_" + name);
            }
        }
        String written = sound == null ? null : RunFilms.captions(sound, script);
        List<RunFilms.Line> lines = written == null ? null : RunFilms.lines(RunShelf.text(context, written));
        if (BuildConfig.DEBUG) {
            android.util.Log.i("RunFinale", "film " + film + ": picture " + shown + ", sound " + (audio.on() ? sound : "off (" + sound + ")") + ", written " + (lines == null || lines.isEmpty() ? "nothing" : written));
        }
        video = RunVideo.film(over, picture, audio.on() ? voice : 0, lines, ends);
    }

    private java.util.List<RunClips.Clip> clips;

    /** The next short clip of a land, at a moment, that has not been seen and whose files are there; or RunClips.NONE. */
    private int clipDue(int land, boolean before) {
        if (clips == null) clips = RunClips.parse(RunShelf.text(context, "clips/clips.tsv"));
        java.util.List<RunClips.Clip> there = new java.util.ArrayList<>();
        for (RunClips.Clip clip : clips) {
            if (RunShelf.raw(context, RunClips.picture(clip.number)) != 0) there.add(clip);
        }
        return RunClips.due(there, land, before, session);
    }

    /** The clips that come before a land, one after another, each once; and then what follows them. Nothing is shown over another film. */
    private void beforeClips(int land, Runnable next) {
        int clip = video == null ? clipDue(land, true) : RunClips.NONE;
        if (clip == RunClips.NONE) next.run();
        else showClip(clip, stage, () -> beforeClips(land, next));
    }

    /** The same for the clips that come after a land is won. */
    private void afterClips(int land, Runnable next) {
        int clip = video == null ? clipDue(land, false) : RunClips.NONE;
        if (clip == RunClips.NONE) next.run();
        else showClip(clip, stage, () -> afterClips(land, next));
    }

    /**
     * A short clip is played once, over the game or the gallery, and then something is done. Its sound is in the language chosen
     * for Halgakos if that has been recorded, and in English if not; what is written under it is in the language chosen for that. It
     * is kept as seen however it ends (a touch or Back ends it at once), so that it is never shown twice and never loops.
     */
    private void showClip(int number, android.view.View over, Runnable then) {
        if (video != null) return;
        audio.hush();
        captionLeft = 0;
        RunVideo.Ends ends = played -> {
            video = null;
            session.keep(RunClips.SEEN + number, 1);
            if (played) news();
            if (shelf != null && !shelf.closed()) shelf.fill();
            if (then != null) then.run();
        };
        int picture = RunShelf.raw(context, RunClips.picture(number)), voice = 0;
        if (picture == 0) {
            ends.ended(false);
            return;
        }
        String sound = null;
        for (String name : RunClips.sounds(number, session.tally(RunFilms.TONGUE))) {
            if (voice == 0 && RunShelf.raw(context, "run_" + name) != 0) {
                sound = name;
                voice = RunShelf.raw(context, "run_" + name);
            }
        }
        int script = session.tally(RunFilms.CAPTIONS);
        String written = sound == null ? null : RunFilms.captions(sound, script);
        java.util.List<RunFilms.Line> lines = written == null ? null : RunFilms.lines(RunShelf.text(context, written));
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "clip " + number + ": sound " + (audio.on() ? sound : "off (" + sound + ")") + ", written " + (lines == null || lines.isEmpty() ? "nothing" : written));
        video = RunVideo.film(over, picture, audio.on() ? voice : 0, lines, ends);
    }

    /** Whether more of the gallery is open than when she last said so: if it is, she will say so, once, when she next can. */
    private void news() {
        int open = RunGallery.open(gallery(), session);
        if (open > session.tally(RunGallery.TOLD)) {
            session.keep(RunGallery.TOLD, open);
            galleryNews = true;
        }
        // (And whether a pet of one she has beaten can be had now: the first ten are had from the first, and are not news.)
        int pets = RunPets.open(session);
        if (session.tally(RunPets.TOLD) == 0) session.keep(RunPets.TOLD, RunPets.FOUND);
        if (pets > session.tally(RunPets.TOLD)) {
            session.keep(RunPets.TOLD, pets);
            petNews = true;
        }
    }

    /**
     * She says that something new is in the gallery when she can: on the map, where she is shown saying it; or in a land, in her
     * corner as her other lines are, when nothing is being told and no one is speaking. Never over a film, a card, or the gallery
     * itself. However many things opened, she says it once.
     */
    private void sayNews(float seconds) {
        if (galleryShown > 0) galleryShown -= seconds;
        if (!galleryChecked) {
            galleryChecked = true;
            news();
        }
        // (For looking, in a build made for testing: a film asked for is played from the map, as it would be after its land.)
        if (BuildConfig.DEBUG && screen == MAP && video == null && session.tally("run_film") > 0) {
            int film = session.tally("run_film") - 1;
            session.keep("run_film", 0);
            if (film <= RunFilms.CLIP) showFilm(film, stage, null);
        }
        // (And a clip asked for, by its number, in the same way: for looking at it.)
        if (BuildConfig.DEBUG && screen == MAP && video == null && session.tally("run_clip") > 0) {
            int clip = session.tally("run_clip");
            session.keep("run_clip", 0);
            showClip(clip, stage, null);
        }
        // (And the guild counter, at a tab, for looking at it.)
        if (BuildConfig.DEBUG && screen == MAP && video == null && session.tally("run_counter") > 0) {
            counterTab = Math.min(TAB_NAMES.length - 1, session.tally("run_counter") - 1);
            session.keep("run_counter", 0);
            counterFrom = MAP;
            screen = SHOP;
        }
        // (And her inventory, for looking at it.)
        if (BuildConfig.DEBUG && screen == MAP && video == null && session.tally("run_inv") > 0) {
            session.keep("run_inv", 0);
            openPack(MAP);
        }
        if (!galleryNews && !petNews || video != null || shelf != null && !shelf.closed()) return;
        boolean free = screen == MAP || screen == PLAY && finale == NO_FINALE && !talking && captionLeft <= 0 && personLeft <= 0;
        if (!free || audio.speaking()) return;
        // (A new pet first; the gallery's news waits for the next moment she can speak.)
        String[][] lines = petNews ? RunStory.NEW_PET : RunStory.GALLERY;
        if (petNews) petNews = false;
        else galleryNews = false;
        galleryWords = lines[random.nextInt(lines.length)];
        galleryShown = 8;
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "gallery news: " + galleryWords[0] + (screen == PLAY ? " in land " + number : " on the map"));
        speak(galleryWords, screen == PLAY);
    }

    void buzz(boolean strongly) {
        if (haptics) stage.performHapticFeedback(strongly ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants.KEYBOARD_TAP);
    }

    // What is said

    /**
     * Someone says a line: its recording plays, and its words may be shown over the game.
     * Halgakos is heard while she calls the player Sloppy, the name she was recorded saying.
     */
    private void speak(String[] words, boolean captioned) {
        int who = RunStory.speaker(words);
        // While the story is being told in the land, what is only by the way is not said over it.
        if (captioned && talking && !told) return;
        // A line that is still being said is not begun again.
        if (captioned && words[0].equals(saying) && (captionLeft > 0 || audio.speaking())) return;
        saying = words[0];
        String said = who == 0 ? names.named(words[1]) : words[1];
        if (captioned) {
            caption = said;
            captionWho = who;
            captionLeft = CAPTION + said.length() * (told ? .045f : .025f);
            captionFor = captionLeft;
        }
        if (who != 0 || Chatter.DEFAULT_NAME.equals(names.nickname())) audio.say(words[0]);
    }

    private void speakAny(String[][] lines) { speak(lines[random.nextInt(lines.length)], true); }

    /** Someone met in the land says her line: once, and nothing that is only by the way is said over it. */
    private void person(String[] words) {
        speak(words, true);
        personLeft = captionLeft;
    }

    /** Something said by the way: not while someone met in the land is still speaking. */
    private void aside(String[] words) {
        if (personLeft <= 0 && !talking) speak(words, true);
    }

    /** A passing remark, made only if nothing else is being said. */
    private void remark(String[][] lines) {
        if (captionLeft <= 0 && personLeft <= 0 && !talking && !audio.speaking()) speakAny(lines);
    }

    /**
     * Tells some lines of the story, and then does something. They are told over whatever
     * is on the screen, the map or the land, which waits while they are.
     */
    private void tell(String[][] lines, int chapter, Runnable then) {
        telling = lines;
        storyChapter = chapter;
        line = 0;
        afterStory = then;
        storyOver = run == null || screen == MAP || screen == SHOP ? MAP : PLAY;
        screen = STORY;
        captionLeft = 0;
        idle();
        speak(telling[0], false);
        audio.play("page", .5f);
    }

    private void endStory() {
        audio.hush();
        Runnable then = afterStory;
        afterStory = null;
        telling = null;
        screen = MAP;
        if (then != null) then.run();
    }

    /** Whether a part of the story kept under a name has yet to be told for a land; and remembers that it now has been. */
    private boolean untold(String key, int land) {
        // The lands of the second volume are too many to keep in one number: each has its own.
        if (land > RunLevels.COUNT) {
            if (session.tally(key + "_" + land) != 0) return false;
            session.keep(key + "_" + land, 1);
            return true;
        }
        int told = session.tally(key);
        if ((told & 1 << land - 1) != 0) return false;
        session.keep(key, told | 1 << land - 1);
        return true;
    }

    // The dungeons

    /** The pictures of the dungeons: the stairway, those that live in them, and the four backdrops. */
    private Bitmap stairsArt, delverSheet;
    private final Bitmap[] depths = new Bitmap[4];
    private static final int[] DEPTHS = {R.drawable.run_depths_catacomb, R.drawable.run_depths_inkcrypt, R.drawable.run_depths_papercave, R.drawable.run_depths_vault};

    private int dungeonsCleared() { return Integer.bitCount(session.tally("run_dungeons")); }

    /** What the floor she is on is like. */
    private int delveKind() { return delve < 0 ? RunDungeons.PLAIN : RunDungeons.kindOf(delve, delveFloor, delveDive); }

    /** She walks up to a dungeon's stairway: its card, with what it is and how she has done in it. */
    private void openStairs() {
        delveAt = RunDungeons.inLand(number);
        if (delveAt < 0) return;
        delveCard = 0;
        idle();
        screen = DELVE;
        audio.play("page", .6f);
        pills("Go down", DELVE_GO, "Not now", RESUME);
    }

    /** Down into a dungeon: a new dive, laid afresh; what she collected in the land she leaves is kept. */
    private void startDive(int dungeon) {
        if (run != null && delve < 0) session.keep("run_bank", bank() + run.purse());
        int dives = session.tally("run_dives") + 1;
        session.keep("run_dives", dives);
        delveDive = BuildConfig.DEBUG && session.tally("run_seed") > 0 ? session.tally("run_seed") : dives * 104729L + dungeon * 7919L + random.nextInt(100000);
        delve = dungeon;
        // (In a build made for testing, from a floor asked for.)
        delveFloor = BuildConfig.DEBUG ? Math.min(RunDungeons.floors(dungeon) - 1, session.tally("run_dive_floor")) : 0;
        delveHp = delveLives = -1;
        delveBlessed = delveHearts = delveMight = delveGold = delveWard = delveKept = 0;
        keeperSaid = false;
        begin(RunDungeons.land(dungeon), false);
        flash = .5f;
        floorCard();
        speak(RunStory.DUNGEONS[1], true);
    }

    /** At the start of a floor, a word of which floor it is and what it is like. */
    private void floorCard() {
        int kind = delveKind();
        String words = kind != RunDungeons.PLAIN ? RunDungeons.KIND_NAMES[kind] + ": " + RunDungeons.KIND_WORDS[kind]
                : delveFloor == RunDungeons.floors(delve) - 1 ? "Its keeper waits at the end" : "Laid anew for this dive";
        toast(RunDungeons.NAMES[delve] + "  ·  floor " + (delveFloor + 1) + " of " + RunDungeons.floors(delve), words, 0);
    }

    /** What she takes down to a floor: harder the deeper, harder again on a fierce floor; and what the blessings chosen give. */
    private void diving(PlusRun.Perks perks) {
        perks.harder += RunDungeons.deeper(delveFloor) + (delveKind() == RunDungeons.FIERCE ? 1 : 0);
        perks.hearts += delveHearts;
        perks.mightPct += 25 * delveMight;
        perks.defence += delveWard;
    }

    /** A floor is over: reached, the blessings to choose from (or, at the bottom, the dungeon cleared); lost, the dive ends. */
    private void finishFloor(boolean reached) {
        int kind = delveKind(), floors = RunDungeons.floors(delve);
        int kept = run.purse() * (100 + RunGear.luck(gearOn()) + RunMarket.PAYS * hard() + 50 * delveGold) / 100 * (kind == RunDungeons.GILDED ? 2 : 1);
        session.keep("run_bank", bank() + kept);
        delveKept += kept;
        banked = delveKept;
        session.keep("run_mobs", session.tally("run_mobs") + run.flattened());
        session.keep("run_fliers", session.tally("run_fliers") + run.fliers());
        session.keep("run_pet_kills", session.tally("run_pet_kills") + run.petKills());
        // (How deep she has been in it, kept: the floors she has cleared.)
        session.keep("run_deep_" + delve, Math.max(session.tally("run_deep_" + delve), delveFloor + (reached ? 1 : 0)));
        idle();
        captionLeft = 0;
        if (reached && delveFloor < floors - 1) {
            delveHp = run.hp();
            delveLives = run.lives();
            offeredNow = RunDungeons.offered(delveDive, delveFloor, run.hp() < run.maxHp(), delveBlessed);
            delveCard = 1;
            screen = DELVE;
            audio.play("unlock", .7f);
            pills(RunDungeons.BLESSING_NAMES[offeredNow[0]], BLESS, RunDungeons.BLESSING_NAMES[offeredNow[1]], BLESS + 1, RunDungeons.BLESSING_NAMES[offeredNow[2]], BLESS + 2);
            speak(RunStory.DUNGEONS[2], false);
            return;
        }
        won = reached;
        resultClock = 0;
        starsShown = 0;
        stars = 0;
        screen = RESULT;
        rungWords = rungPrize = "";
        if (won) {
            boolean first = (session.tally("run_dungeons") >> delve & 1) == 0;
            session.keep("run_dungeons", session.tally("run_dungeons") | 1 << delve);
            int pluses = RunDungeons.pluses(delve, first);
            session.keep("run_bank", bank() + pluses);
            rungWords = "+ " + pluses + " for clearing it" + (first ? ", the first time" : "");
            if (first) {
                int relic = RunDungeons.relic(delve);
                intoBag(relic);
                rungPrize = "Its relic: " + RunLoot.name(relic);
                toast(RunLoot.RARITIES[RunLoot.rarity(relic)] + ": " + RunLoot.name(relic), "Its relic, in her bag", LOOT_PICTURE + relic);
            }
            audio.play("win");
            speak(RunStory.DUNGEONS[4], false);
            pills("Dive again", AGAIN, "Lands", TO_MAP);
        } else {
            audio.play("lose");
            speak(RunStory.DUNGEONS[5], false);
            pills("Dive again", AGAIN, "Lands", TO_MAP);
        }
    }

    /** A blessing chosen: it is hers for the rest of the dive; and down she goes to the next floor. */
    private void bless(int which) {
        delveBlessed |= 1 << which;
        switch (which) {
            case RunDungeons.HEART:
                delveHearts++;
                delveHp += RunSkills.HEART;
                break;
            case RunDungeons.MIGHT:
                delveMight++;
                break;
            case RunDungeons.GOLD:
                delveGold++;
                break;
            case RunDungeons.LIFE:
                delveLives++;
                break;
            case RunDungeons.MEND:
                delveHp = Integer.MAX_VALUE;
                break;
            default:
                delveWard++;
        }
        delveFloor++;
        begin(RunDungeons.land(delve), false);
        flash = .5f;
        floorCard();
        int kind = delveKind();
        if (kind == RunDungeons.LIGHTLESS) speak(RunStory.DUNGEONS[6], true);
        else if (kind == RunDungeons.GILDED) speak(RunStory.DUNGEONS[7], true);
        else if (kind == RunDungeons.TEEMING) speak(RunStory.DUNGEONS[8], true);
    }

    private Bitmap stairsArt() {
        if (stairsArt == null) stairsArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_stairs);
        return stairsArt;
    }

    /** A dungeon's stairway in its land: an arch of books over steps down, lit from below; and the dungeon's name over it as she comes near. */
    private void drawStairs(Canvas canvas, int[] item) {
        Bitmap art = stairsArt();
        float px = (item[1] + .5f - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile, tall = tile * 2.7f, wide = tall * art.getWidth() / art.getHeight();
        float pulse = .5f + .5f * (float) Math.sin(clock * 2.2f);
        glowAt(canvas, px, feet - tall * .3f, tall * .55f, 0xFF9A6AE8, Math.round(60 + 50 * pulse));
        paint.setAlpha(255);
        target.set(px - wide / 2, feet - tall + tile * .06f, px + wide / 2, feet + tile * .06f);
        canvas.drawBitmap(art, null, target, paint);
        int dungeon = RunDungeons.inLand(number);
        if (dungeon >= 0 && Math.abs(run.x() - item[1]) < 7) mark(canvas, px, feet - tall - tile * .15f, RunDungeons.NAMES[dungeon], 0xFF6A3FA0, false);
    }

    /** The stairway fitted in a square about a place: for buttons and the map. */
    private void stairsIn(Canvas canvas, float x, float y, float size) {
        Bitmap art = stairsArt();
        float tall = size, wide = tall * art.getWidth() / art.getHeight();
        if (wide > size) {
            wide = size;
            tall = size * art.getHeight() / art.getWidth();
        }
        target.set(x - wide / 2, y - tall / 2, x + wide / 2, y + tall / 2);
        paint.setAlpha(255);
        canvas.drawBitmap(art, null, target, paint);
    }

    /** Behind a dungeon's floor: one of four backdrops, mirrored side by side, darker the deeper she is. */
    private void drawDepths(Canvas canvas, float drift) {
        Bitmap back = depthsBack();
        float wide = height * (float) back.getWidth() / back.getHeight();
        paint.setAlpha(255);
        for (int copy = (int) (drift / wide); copy * wide - drift < width; copy++) {
            float left = copy * wide - drift;
            target.set(left - 1, 0, left + wide + 1, height);
            canvas.save();
            if (copy % 2 != 0) canvas.scale(-1, 1, left + wide / 2, 0);
            canvas.drawBitmap(back, null, target, paint);
            canvas.restore();
        }
        int tint = BAND_TINT[RunDungeons.band(delveFloor)];
        canvas.drawColor(Color.argb(Math.min(160, 30 + delveFloor % RunDungeons.BAND_FLOORS * 18 + RunDungeons.band(delveFloor) * 20), Color.red(tint), Color.green(tint), Color.blue(tint)));
    }

    /** Under black: the edges of what she sees dark (lighter than a lightless floor), and ink dripping off her. */
    private void drawInked(Canvas canvas) {
        if (run == null || !run.inked()) return;
        float hx = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, hy = (run.y() + PlusRun.TALL / 2 - cameraY) * tile;
        float fade = Math.min(1, Math.min(run.cursed(), run.cursedFor() - run.cursed() + .3f) / .6f);
        paint.setShader(new android.graphics.RadialGradient(hx, hy, Math.max(width, height) * .62f, new int[]{0x00000000, 0x00000000, 0xB0080412},
                new float[]{0, .38f, 1}, android.graphics.Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(Math.round(255 * Math.max(0, fade)));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
        paint.setAlpha(255);
        // Drops of ink running off her and falling.
        paint.setColor(0xFF120A1C);
        for (int drop = 0; drop < 4; drop++) {
            float through = (clock * .9f + drop * .27f) % 1, across = (drop - 1.5f) * tile * .16f;
            float y = hy - tile * .35f + through * tile * 1.1f, size = tile * (.05f + .03f * (1 - through));
            paint.setAlpha(Math.round(230 * (1 - through * .7f)));
            canvas.drawOval(hx + across - size * .7f, y - size, hx + across + size * .7f, y + size * 1.3f, paint);
        }
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
    }

    /** The black ribbon on the HUD while black lies on her: the ribbon, and how long it has left. */
    private void drawBlackChip(Canvas canvas, float left, float top) {
        if (!run.inked()) return;
        float wide = dp(70), high = dp(22);
        box.set(left, top, left + wide, top + high);
        plate(canvas, box, high / 2, 235);
        pieceIn(canvas, two, RIBBON_ART + PlusRun.BLACK - 1, left + dp(13), top + high / 2, dp(20), 0);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(0xFF2A1C3A);
        fitted(canvas, (int) Math.ceil(run.cursed()) + " s", left + dp(27), top + high / 2 + dp(4.5f), dp(12), wide - dp(32));
    }

    /** Over a dungeon's floor: on a lightless one, the dark, but for a ring of light about her; and the keeper's name and what it has left, while it is near. */
    private void drawDepthsOver(Canvas canvas) {
        if (delve < 0 || run == null || run.room() >= 0) return;
        if (delveKind() == RunDungeons.LIGHTLESS) {
            float hx = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, hy = (run.y() + PlusRun.TALL / 2 - cameraY) * tile;
            paint.setShader(new android.graphics.RadialGradient(hx, hy, tile * 6, new int[]{0x00000000, 0x00000000, 0xF2050310}, new float[]{0, .4f, 1},
                    android.graphics.Shader.TileMode.CLAMP));
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(255);
            canvas.drawRect(0, 0, width, height, paint);
            paint.setShader(null);
            paint.setColor(Color.BLACK);
        }
        for (PlusRun.Foe foe : run.foes()) {
            if (!RunUniques.keeper(foe.kind) || foe.flat || Math.abs(foe.x - run.x()) > 18) continue;
            // (Below what is said at the top, clear of it.)
            float wide = Math.min(safe.width() - dp(24), dp(300)), from = safe.centerX() - wide / 2, top = safe.top + Math.max(dp(200), safe.height() * .33f);
            box.set(from, top, from + wide, top + dp(40));
            plate(canvas, box, dp(18), 235);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(0xFF6A3FA0);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, RunUniques.name(foe.kind) + "  ·  keeper", from + dp(14), top + dp(16), dp(12.5f), wide - dp(28));
            text.setTypeface(Typeface.DEFAULT);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
            canvas.drawRoundRect(from + dp(14), top + dp(24), from + wide - dp(14), top + dp(32), dp(4), dp(4), paint);
            paint.setColor(0xFF7A4FC0);
            canvas.drawRoundRect(from + dp(14), top + dp(24), from + dp(14) + (wide - dp(28)) * Math.max(0, foe.left) / Math.max(1f, foe.most), top + dp(32), dp(4), dp(4), paint);
            paint.setColor(Color.BLACK);
            return;
        }
    }

    /** Over the buttons along the foot of the screen, in the middle, in a dive: which floor of how many, and what it is like. */
    private void drawDelveLabel(Canvas canvas, float middle, float top) {
        if (delve < 0) return;
        int kind = delveKind();
        String words = "Floor " + (delveFloor + 1) + " of " + RunDungeons.floors(delve) + (kind == RunDungeons.PLAIN ? "" : "  ·  " + RunDungeons.KIND_NAMES[kind]);
        text.setTextSize(dp(12));
        float wide = text.measureText(words) + dp(24), left = middle - wide / 2;
        box.set(left, top, left + wide, top + dp(22));
        plate(canvas, box, dp(11), 235);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(0xFF6A3FA0);
        canvas.drawText(words, left + dp(12), top + dp(15.5f), text);
    }

    /** The cards of a dive: at the stairway, what the dungeon is and how she has done in it; between floors, the three blessings to choose from. */
    private void drawDelveCard(Canvas canvas) {
        float wide = Math.min(safe.width() - dp(32), dp(320)) - dp(40);
        if (delveCard == 0) {
            int dungeon = Math.max(0, delveAt), floors = RunDungeons.floors(dungeon), deepest = session.tally("run_deep_" + dungeon);
            boolean cleared = (session.tally("run_dungeons") >> dungeon & 1) != 0;
            float top = card(canvas, RunDungeons.NAMES[dungeon], floors + " floors  ·  for level " + RunDungeons.recommended(dungeon) + "  ·  "
                    + (cleared ? "cleared" : deepest > 0 ? "deepest: floor " + deepest : "never gone down"), dp(70));
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, "Laid anew every time; her hearts go down with her.", box.left + dp(20), top + dp(18), dp(13), wide);
            fitted(canvas, "A blessing to choose after every floor.", box.left + dp(20), top + dp(38), dp(13), wide);
            text.setColor(0xFF7A3A9A);
            fitted(canvas, "At the bottom: " + RunUniques.name(RunDungeons.keeper(dungeon)) + (cleared ? "" : "  ·  a relic the first time"), box.left + dp(20), top + dp(58), dp(13), wide);
            return;
        }
        int floors = RunDungeons.floors(delve), next = RunDungeons.kindOf(delve, delveFloor + 1, delveDive);
        boolean keeperNext = delveFloor + 1 == floors - 1;
        float top = card(canvas, "Floor " + (delveFloor + 1) + " of " + floors + " cleared", "Kept + " + delveKept + "  ·  choose a blessing for the rest of the dive", dp(92));
        text.setTextAlign(Paint.Align.LEFT);
        for (int one = 0; one < 3; one++) {
            text.setColor(ink);
            fitted(canvas, RunDungeons.BLESSING_NAMES[offeredNow[one]] + ": " + RunDungeons.BLESSING_WORDS[offeredNow[one]], box.left + dp(20), top + dp(18) + one * dp(20), dp(13), wide);
        }
        text.setColor(0xFF7A3A9A);
        fitted(canvas, "Next: " + (keeperNext ? "the keeper's floor" : next == RunDungeons.PLAIN ? "a plain floor" : RunDungeons.KIND_NAMES[next].toLowerCase(java.util.Locale.ROOT) + " floor, "
                + RunDungeons.KIND_WORDS[next]), box.left + dp(20), top + dp(80), dp(13), wide);
    }

    /** A dive over: cleared, what it gave; lost, the floor it ended on, and her on the card, sitting where she fell. */
    private void drawDiveResult(Canvas canvas) {
        String name = RunDungeons.NAMES[delve];
        float wide = Math.min(safe.width() - dp(32), dp(320)) - dp(40);
        if (won) {
            float top = card(canvas, name + " is cleared", RunDungeons.floors(delve) + " floors  ·  kept + " + banked, dp(rungPrize.isEmpty() ? 40 : 66));
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(CalculatorLayout.CORAL);
            fitted(canvas, rungWords, safe.centerX(), top + dp(24), dp(16), wide);
            if (!rungPrize.isEmpty()) {
                text.setColor(ink);
                fitted(canvas, rungPrize, safe.centerX(), top + dp(50), dp(13.5f), wide);
            }
            text.setTextAlign(Paint.Align.LEFT);
            return;
        }
        float high = dp(104);
        float top = card(canvas, "The dive ends on floor " + (delveFloor + 1), name + "  ·  kept + " + banked, high + dp(8));
        int pose = resultClock < 1.3f ? DAZED : (int) ((resultClock - 1.3f) / .55f) % 2 == 0 ? SULK : CRY;
        paint.setAlpha(255);
        frame(canvas, herLose(knockForm), pose, LOSE_FRAMES, safe.centerX(), top + high + dp(4), dp(96), false);
    }

    // A land

    private void play(int land) {
        beforeClips(land, () -> playLand(land));
    }

    private void playLand(int land) {
        number = land;
        blackRung = 0;
        delve = -1;
        String[][] intro = RunStory.intro(land);
        // Where the story moves on, it is told as she stands at the start of the land, the first time.
        // It is told in the bubble at the top, a line after another, while she is already on her way: nothing waits for it.
        if (intro != null && untold("run_intro2", land)) {
            begin(land, false);
            java.util.Collections.addAll(talk, intro);
        } else {
            begin(land, true);
        }
    }

    private void begin(int land, boolean greeted) {
        number = land;
        talk.clear();
        talking = told = false;
        hintMoved = hintJumped = false;
        seenDrops = seenBeams = seenBlasts = jumpsAtFall = seenCracks = seenShatters = seenThrownBack = 0;
        seenPetJoined = seenPetStrikes = seenPetKills = 0;
        seenPetGifts = seenPetWards = seenPetCopies = seenPetSwaps = 0;
        offerSeen = -1;
        seenKeys = seenLevers = seenPlates = seenShrines = 0;
        seenPanels = seenFans = seenDuds = seenFinds = seenRefills = seenSwitches = 0;
        sinceHeavy = panelAge = 9;
        seenCuts = seenDashes = seenAirCuts = seenThrusts = seenJets = seenShells = seenLashes = seenSpins = 0;
        seenPotShots = seenPledges = 0;
        capsuleAge = 9;
        capsuleWaits = false;
        cutAge = dashAge = airAge = thrustAge = lashAge = spinAge = 9;
        packOpen = false;
        huntsOpen = false;
        smithOpen = false;
        marketOpen = false;
        hammerer = -1;
        // (A clip still being watched is put away, and nothing follows it: this is another beginning.)
        if (video != null) {
            finale = NO_FINALE;
            video.skip();
        }
        finale = NO_FINALE;
        flight = null;
        ride = null;
        goBreath = false;
        flightNote = null;
        flightWords.clear();
        seenRibboned = beamWas = false;
        sinceBeamEnd = cheerAge = 9;
        seenFells = 0;
        swapFinger = -1;
        petWordLeft = 0;
        wardAge = 9;
        flights.clear();
        chestAge = 9;
        petGlad = 9;
        slow = 0;
        goDown = false;
        perks = kept();
        perks.black = blackRung;
        // For looking at what losing looks like in a build made for testing: one heart and one life.
        if (BuildConfig.DEBUG && session.tally("run_frail") > 0) {
            perks.hearts = 1;
            perks.lives = 1;
        }
        // The land itself is as its number makes it; its other layers are laid afresh for each play (in a build made for testing, from a
        // number asked for, to look at the same ones again).
        int plays = session.tally("run_plays") + 1;
        session.keep("run_plays", plays);
        long play = BuildConfig.DEBUG && session.tally("run_seed") > 0 ? session.tally("run_seed") : plays * 7919L + land;
        // In a dive, a floor of its dungeon, as the dive lays it, with what she carries down from the floor above.
        if (delve >= 0) diving(perks);
        run = new PlusRun(delve >= 0 ? RunLevels.dungeon(delve, delveFloor, delveDive) : blackRung > 0 ? RunLevels.level(land) : RunLevels.layered(RunLevels.level(land), play), perks);
        if (delve >= 0 && delveHp > 0) run.carry(delveHp, delveLives);
        // (And, for looking at the end of one, a keeper that takes little.)
        if (BuildConfig.DEBUG && delve >= 0 && session.tally("run_weak_keeper") > 0) {
            for (PlusRun.Foe foe : run.foes()) if (RunUniques.keeper(foe.kind)) foe.left = foe.most = Math.max(PlusRun.BLOW, foe.most / 20);
        }
        huntsHere();
        // How tough the land is, said the first time its creatures take more, or a hurt costs more, than in any land she has been in.
        int tougher = run.tough() * 10 + run.bite();
        if (tougher > session.tally("run_tough_told")) {
            session.keep("run_tough_told", tougher);
            toast("A land for level " + RunLoot.recommended(land), "Creatures take " + (1 + run.tough()) + (run.tough() == 0 ? " blow" : " blows") + "  ·  a hurt costs "
                    + hearts(2 + run.bite()), 0);
        }
        // A rung of the black arena: she begins at the door of the place where whoever waits is, and a card says who, and what winning gives.
        if (blackRung > 0) {
            RunLevels.Level here = run.level();
            int column = Math.max(2, here.arena - 3);
            while (column < here.columns - 2 && RunLevels.top(here, column) < 0) column++;
            run.begins(column, RunLevels.top(here, column) - PlusRun.TALL - .05f);
            toast("The black arena: rung " + blackRung, waitName(RunLevels.bossOf(land)) + ", stronger  ·  "
                    + (session.tally("run_black") < blackRung ? RunMarket.prizeWords(blackRung) : "won before: a token"), 0);
        }
        // For looking at a land's later parts in a build made for testing: she starts by its first flag, its second,
        // inside the place where someone waits, or, for a number over a thousand, at the column that many over it.
        int from = BuildConfig.DEBUG && blackRung == 0 && !crossing ? session.tally(delve >= 0 ? "run_dive_from" : "run_from") : 0;
        if (from > 0) {
            RunLevels.Level here = run.level();
            int column = from > 1000 ? Math.min(here.columns - 2, from - 1000) : from <= here.flags.size() ? here.flags.get(from - 1)
                    : here.arena >= 0 ? here.arena + 4 : here.gate - 10;
            // Over a pit, the first ground to the right of it; on a hatch, on the hatch.
            int stand = RunLevels.top(here, column);
            for (int row = 0; row < RunLevels.ROWS && stand < 0; row++) if (here.land[row][column] == RunLevels.THIN) stand = row;
            while (column < here.columns - 2 && stand < 0) stand = RunLevels.top(here, ++column);
            // (Or on the floor of a row asked for: down in a cave, say.)
            if (session.tally("run_from_row") > 0) stand = session.tally("run_from_row");
            run.place(column + (1 - PlusRun.WIDE) / 2, stand - PlusRun.TALL - .05f);
        }
        // And, for the same, she may start in a colour, as from a ribbon, and with something in her hands.
        if (BuildConfig.DEBUG && session.tally("run_wear") > 0) run.lend(session.tally("run_wear"));
        // (And straight into a place off the land of a kind, kind and one, for looking at it.)
        if (BuildConfig.DEBUG && session.tally("run_go_kind") > 0) run.visit(session.tally("run_go_kind") - 1);
        // And the white that is truly hers, which the story has yet to give.
        if (BuildConfig.DEBUG && session.tally("run_true") > 0) run.master();
        PlusRun.ceiling = BuildConfig.DEBUG && session.tally("run_ceiling") > 0 ? session.tally("run_ceiling") : PlusRun.CEILING;
        // And a pet, for looking at one.
        if (BuildConfig.DEBUG && session.tally("run_pet") > 0) run.adopt(session.tally("run_pet") - 1);
        if (BuildConfig.DEBUG && session.tally("run_arm") > 0) run.arm(session.tally("run_arm") - 1);
        // And what drops, of each rarity, for looking at it.
        if (BuildConfig.DEBUG && session.tally("run_drops") > 0) run.scatterAll(run.x() + 1, run.y());
        idle();
        seenJumps = seenLandings = seenCollected = seenFlattened = seenHurts = seenSprings = seenFlags = seenBlows = 0;
        seenStrikes = seenPicked = seenThrown = seenTurned = seenTaken = seenSpared = seenCharms = seenFlaps = seenSwings = seenBonus = 0;
        seenFormed = seenDoors = seenQuakes = seenParried = seenCensors = seenEntrances = seenSpecials = seenPhases = seenUnlocks = seenWiped = 0;
        seenUppercuts = seenHaymakers = 0;
        uppercutAge = haymakerAge = 9;
        java.util.Arrays.fill(remarked, false);
        java.util.Arrays.fill(saidForm, false);
        saidCombo = saidLast = saidFlag = saidCharm = false;
        seenGreetings = seenEntered = seenReveals = seenWins = 0;
        hinted = saidRoom = 0;
        seenAsked = seenDone = seenFailed = seenCalled = seenBursts = seenWoken = 0;
        seenRoused = seenShuts = seenDrunk = seenGrounded = seenFizzled = seenDives = seenCurses = 0;
        darkened = 0;
        roverKind = -1;
        roverAge = 9;
        roverOver = false;
        flySeen = session.tally("run_fly_seen");
        prizeAge = -1;
        prizeWon = RunLevels.NOTHING;
        saidRumour = false;
        seenLives = seenCasts = seenBraces = seenPaid = seenReminders = seenChests = 0;
        seenDiaries = seenLifted = 0;
        seenXp = seenFound = 0;
        diaryAge = 99;
        villageSaid = false;
        roomCardAge = roomResultAge = -1;
        tickedAt = 0;
        seenPhase = 0;
        sincePhase = 9;
        roomRank = -1;
        seenMixings = seenFaded = seenWhitened = 0;
        mixAge = sinceStep = 9;
        ribbonFinger = -1;
        fanOpen = false;
        // The first land begun with two colours hers: a word about wearing them together.
        hintMix = Integer.bitCount(perks.colours & 127) >= 2 && session.tally("run_hint_mix") == 0;
        handAge = -1;
        handQuiet = false;
        questLine = null;
        look = lookFrom = knockForm = 0;
        lookAge = flinchAge = sinceStruck = sinceBroke = 9;
        guardWas = -1;
        sinceAsked = 9;
        personLeft = 0;
        saying = null;
        shake = flash = 0;
        sinceBump = sinceShut = sinceOpened = sinceParry = 9;
        knockAge = -1;
        squash = 0;
        sinceUse = 9;
        sinceLook = 0;
        sinceAim = 9;
        bits.clear();
        pops.clear();
        popWords.clear();
        cameraX = 0;
        cameraY = restingCamera();
        woodReset = true;
        camFeet = -99;
        screen = PLAY;
        captionLeft = 0;
        // And, in the last land, the flight or the land unmade at once, for looking at them.
        if (BuildConfig.DEBUG && land == RunLevels.TOTAL && session.tally("run_finale") == 1) takeOff();
        else if (BuildConfig.DEBUG && land == RunLevels.TOTAL && session.tally("run_finale") == 2) unmake();
        else if (BuildConfig.DEBUG && land == RunLevels.TOTAL && session.tally("run_finale") == 3) rise();
        // The first time she has a colour of her own to call on, she says how; after that, something about setting off.
        if (greeted && perks.colours != 0 && session.tally("run_hint_call") == 0) {
            session.keep("run_hint_call", 1);
            speak(RunStory.PRIZES[3], true);
        } else if (greeted) {
            speakAny(RunStory.START);
        }
    }

    private void idle() {
        goLeft = goRight = goJump = goStrike = goBreath = false;
        jumpPressed = strikePressed = false;
        ribbonFinger = -1;
        fanOpen = false;
    }

    private void pause() {
        idle();
        screen = PAUSED;
        pills("Carry on", RESUME, "Start again", AGAIN, "Lands", TO_MAP);
    }

    /** What a hurt costs, in words, from how many halves of a heart it is. */
    private static String hearts(int halves) {
        String[] whole = {"", "a heart", "two hearts", "three hearts", "four hearts", "five hearts", "six hearts"};
        int full = Math.min(whole.length - 1, halves / 2);
        return halves % 2 == 0 ? whole[full] : full == 0 ? "half a heart" : whole[full] + " and a half";
    }

    /** Sets out the buttons of a card. */
    private void pills(Object... labelled) {
        pillCount = labelled.length / 2;
        for (int index = 0; index < pillCount; index++) {
            pillLabels[index] = (String) labelled[index * 2];
            pillActions[index] = (Integer) labelled[index * 2 + 1];
        }
    }

    /** Whether the land runs on into the next: every land but the last, outside the black arena. */
    private boolean onward() { return blackRung == 0 && delve < 0 && number < RunLevels.OPEN_TO && finale == NO_FINALE; }

    /** The word at the top that a land is cleared: its title, its stars, what was collected and kept. It does not stop her. */
    private float clearedAge = 9;
    private String clearedTitle = "", clearedWords = "";
    private int clearedStars, clearedShown;
    private static final float CLEARED = 4.2f;

    /**
     * The gate as a teleporter. Standing at the gate of a land she has passed, a button takes her away in a column of light, and
     * the lands open over the land: a gate she has found (of a land she has won) takes her to that gate, a land she has not won
     * yet to its start; she comes in a column of light. Shut without choosing, she comes back where she was.
     */
    private boolean warping, arrivedAtGate, villageSaid;

    /** What a village has, in a few words. */
    private String villageWords() {
        StringBuilder words = new StringBuilder("Guild hall, pet shop, smithy");
        for (int sort : RunLevels.townOf(number)) {
            if (sort == RunLevels.GACHA) words.append(", ribbon gacha");
            if (sort == RunLevels.BOUTIQUE) words.append(", Tulle and Thread");
            if (sort == RunLevels.MARKET) words.append(", black market");
        }
        return words.append(" · the arch is a portal").toString();
    }
    /** The portal's pictures: eight of it lit and turning, and one dim. How tall it is drawn, and how far up from its foot its swirl's middle is, in squares. */
    private Bitmap portal;
    static final float PORTAL_TALL = 3.3f, PORTAL_MIDDLE = 1.37f;

    /** Where on the screen the middle of this land's portal is. */
    private float portalX() { return (run.level().gate + .5f - cameraX) * tile; }

    private float portalY() { return (Math.max(0, RunLevels.top(run.level(), run.level().gate)) - cameraY) * tile - tile * PORTAL_MIDDLE; }
    private float warpAge = 9, arriveAge = 9;
    private static final float WARP = .7f, ARRIVE = .8f;

    /** Which part of the story it is, for what is said when someone is talked to: the first lands, the rest of the first volume, the second, the third. */
    private int talkStage() {
        int open = open();
        return open <= 15 ? 0 : open <= RunLevels.COUNT ? 1 : open <= RunLevels.SECOND ? 2 : 3;
    }

    /** Who of those met stands at a thing in the land, or -1: someone holding something out, at a stall, at a desk, or Moru at her anvil. */
    private static int talker(int[] item) {
        if (item[0] == PlusRun.SPOT && item[4] == PlusRun.SMITH) return RunLevels.MORU;
        return item[3] >= RunLevels.SOMI && item[3] <= RunLevels.NURI ? item[3] : -1;
    }

    /** Where in the lines of talk someone's are, or -1. */
    private static int talkIndex(int who) { return who == RunLevels.NURI ? 9 : who >= RunLevels.SOMI && who <= RunLevels.ALTTEUL ? who - RunLevels.SOMI : -1; }

    /** Whether someone has said what she has to say in this part of the story. */
    private boolean heard(int who) {
        int index = talkIndex(who);
        return index < 0 || (session.tally("run_talk_" + talkStage()) >> index & 1) != 0;
    }

    /**
     * A tap on someone who stands in the land (or in a building), near enough to her to be talking distance: she says her line for
     * where the story is, and Halgakos answers after her. Whether it was a tap on someone.
     */
    private boolean talkTo(float x, float y) {
        if (run == null) return false;
        float herX = run.x() + PlusRun.WIDE / 2;
        for (int[] item : run.level().items) {
            int who = talker(item), index = talkIndex(who);
            if (index < 0) continue;
            float atX = item[1] + (item[0] == PlusRun.SPOT && item[4] == PlusRun.SMITH ? 2.3f : 1.55f);
            if (Math.abs(atX - herX) > 7) continue;
            float screenX = (atX - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile;
            if (x < screenX - tile * .75f || x > screenX + tile * .75f || y < feet - tile * 1.6f || y > feet + tile * .1f) continue;
            // (Not where a button lies over them.)
            if (stallButton.contains(x, y) || downButton.contains(x, y) || swapButton.contains(x, y) || jumpButton.contains(x, y) || strikeButton.contains(x, y) || slotAt(x, y) >= 0) return false;
            int stage = talkStage();
            session.keep("run_talk_" + stage, session.tally("run_talk_" + stage) | 1 << index);
            audio.play("tap", .5f);
            // (What was being told of the land stops for her: she is listening to someone else now.)
            talk.clear();
            talking = false;
            audio.hush();
            captionLeft = personLeft = 0;
            person(RunStory.TALK[stage][index]);
            talk.add(RunStory.ANSWERS[stage][index]);
            return true;
        }
        return false;
    }

    /** Whether she stands at the gate of this land, past it: where the teleporter is. */
    private boolean atGate() {
        if (run == null || run.room() >= 0 || !onward() || warpAge < WARP || !run.onGround()) return false;
        float middle = run.x() + PlusRun.WIDE / 2;
        // (A village's arch is a way to the teleporter too, in a land she has won before or now.)
        if (run.level().town >= 0 && Math.abs(middle - (run.level().town + 1.5f)) < 1.6f && (run.passed() || starsOf(number) > 0)) return true;
        return run.passed() && Math.abs(middle - (run.level().gate + .5f)) < 1.9f;
    }

    /** She steps on the gate's light: she goes up in it, and then the lands are opened to choose from. */
    private void warp() {
        audio.play("unlock", .7f);
        buzz(true);
        idle();
        warpAge = 0;
    }

    private void openWarp() {
        warping = true;
        int volume = number > RunLevels.SECOND ? 2 : number > RunLevels.COUNT ? 1 : 0;
        turnTo(Math.min(volumes() - 1, volume));
        screen = MAP;
        speak(RunStory.WARP[random.nextInt(RunStory.WARP.length)], false);
    }

    /** A land is chosen from the gate: its gate, if she has won it; its start, if not. Or none, and she is back where she was. */
    private void warpTo(int land) {
        warping = false;
        clearedAge = CLEARED;
        if (land <= 0) {
            screen = PLAY;
            arriveAge = 0;
            arrivedAtGate = true;
            flash = .3f;
            return;
        }
        boolean found = starsOf(land) > 0 && land < RunLevels.OPEN_TO;
        play(land);
        // (A land with its chapter's village: to the village, under its arch. Any other she has won: to its gate.)
        if (found && run.level().town >= 0) {
            int column = run.level().town + 1;
            run.place(column + (1 - PlusRun.WIDE) / 2, Math.max(0, RunLevels.top(run.level(), column)) - PlusRun.TALL - .05f);
            found = false;
        } else if (found) {
            run.atGate();
        }
        arrivedAtGate = found;
        flash = .3f;
        cameraX = Math.max(0, Math.min(Math.max(0, run.level().main - width / tile), run.x() + PlusRun.WIDE / 2 - width / tile / 2));
        arriveAge = 0;
        audio.play("unlock", .7f);
    }

    /** How far into the way on to the next land she is, as the view goes dark and light again about it; and the land she came from. */
    private float crossAge = 9;
    /** (While she goes on into the next land: where a build made for testing puts her in a land is not where she comes into it.) */
    private boolean crossing;

    /**
     * She has passed the gate of a land: it is won, as it was with the card, but there is no card. What it gave is kept, the
     * next land opens, and she says how it went; a word at the top says it is cleared, with its stars; and on she goes. Where
     * the story has something to tell after it, or a film belongs to it, they come now, and then she goes on.
     */
    private void clearLand() {
        won = true;
        banked = run.purse() * (100 + RunGear.luck(gearOn()) + RunMarket.PAYS * hard()) / 100;
        session.keep("run_bank", bank() + banked);
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "cleared " + number);
        float share = run.total() == 0 ? 1 : run.collected() / (float) run.total();
        stars = PlusRun.starsFor(share);
        if (stars > starsOf(number)) session.keep("run_stars_" + number, stars);
        session.keepBest("run_time_" + number, Math.max(1, Math.round(run.time())), true);
        if (number >= open() && number < RunLevels.OPEN_TO) session.keep("run_open", number + 1);
        if (number == 1) session.keepBest("run_best", Math.max(1, Math.round(run.time())), true);
        audio.play("win");
        int late = prize(number);
        if (late != RunLevels.NOTHING) prizeWon = late;
        session.keep("run_mobs", session.tally("run_mobs") + run.flattened());
        session.keep("run_fliers", session.tally("run_fliers") + run.fliers());
        session.keep("run_pet_kills", session.tally("run_pet_kills") + run.petKills());
        news();
        run.carryOn();
        clearedTitle = landTitle();
        clearedStars = stars;
        clearedWords = "+ " + run.collected() + " of " + run.total() + "  ·  kept + " + banked + (questLine != null ? "  ·  " + questLine : "");
        clearedAge = 0;
        clearedShown = 0;
        speak(RunStory.WON[stars - 1], true);
        if (late != RunLevels.NOTHING) toast("Won to keep: " + prizeName(late), capital(prizeGives(late)), -late);
        String[][] outro = RunStory.outro(number);
        int film = RunFilms.due(number, true, session.tally("run_ending"), session.tally(RunFilms.SEEN));
        Runnable back = () -> screen = PLAY;
        Runnable then = film == RunFilms.NONE ? back : () -> {
            screen = PLAY;
            idle();
            showFilm(film, stage, back);
        };
        if (outro != null && untold("run_outro2", number)) tell(outro, RunLevels.chapterOf(number), then);
        else if (film != RunFilms.NONE) then.run();
    }

    /**
     * Over the land: the dark of the way on as she goes into it and comes out in the next land; the column of light she goes up
     * in from a gate and comes down in; and the word at the top that a land is cleared.
     */
    /** How far into the portal she has been drawn: going (to all the way, as she goes), or coming out (from all the way, as she comes). */
    private float pull() {
        if (warpAge < WARP) return Math.min(1, warpAge / WARP);
        if (arriveAge < ARRIVE) return 1 - Math.min(1, arriveAge / ARRIVE);
        return 0;
    }

    private void drawWayOn(Canvas canvas) {
        if (run == null || finale != NO_FINALE) return;
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, feet = (run.y() + PlusRun.TALL - cameraY) * tile;
        float dark = 0;
        if (run.passed() && run.room() < 0 && onward()) {
            float left = run.level().main - PlusRun.THROUGH - (run.x() + PlusRun.WIDE / 2);
            if (left < 1.8f) dark = 1 - Math.max(0, left) / 1.8f;
        }
        if (crossAge < .6f) dark = Math.max(dark, 1 - crossAge / .6f);
        if (dark > 0) canvas.drawColor(Color.argb(Math.round(225 * dark), 10, 6, 18));
        float pull = pull();
        if (pull > 0) {
            boolean atPortal = warpAge < WARP || arrivedAtGate;
            float x = atPortal ? portalX() : herX, y = atPortal ? portalY() : feet - tile * .8f;
            glowAt(canvas, x, y, tile * (.9f + 1.1f * pull), 0xFFC9B2FF, Math.round(190 * pull));
            glowAt(canvas, x, y, tile * .7f * pull, 0xFFFFF4D8, Math.round(240 * pull));
            paint.setAlpha(255);
        }
        if (clearedAge < CLEARED) {
            float in = Math.min(1, clearedAge / .3f), out = Math.min(1, (CLEARED - clearedAge) / .5f), shown = Math.min(in, out);
            float middle = safe.centerX(), top = safe.top + Math.max(dp(170), safe.height() * .27f) - (1 - in) * dp(18), star = dp(30);
            int alpha = Math.round(255 * shown);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(Math.round(110 * shown), 20, 12, 30));
            float half = Math.min(safe.width() / 2 - dp(12), dp(190));
            canvas.drawRoundRect(middle - half, top - dp(30), middle + half, top + dp(70), dp(22), dp(22), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setColor(Color.argb(alpha, 255, 255, 255));
            glow(true, dp(22));
            fitted(canvas, clearedTitle, middle, top, dp(22), half * 2 - dp(24));
            glow(false, 0);
            text.setTypeface(Typeface.DEFAULT);
            for (int index = 0; index < 3; index++) {
                float since = clearedAge - .5f - index * .3f;
                boolean earned = index < clearedStars && since > 0;
                float size = star * (earned ? 1 + .4f * Math.max(0, 1 - since / .18f) : .85f);
                paint.setAlpha(Math.round((earned ? 255 : 60) * shown));
                art(canvas, STAR_ART, middle + (index - 1) * (star + dp(10)), top + dp(14) + star + (size - star) / 2, size, false);
            }
            paint.setAlpha(255);
            text.setColor(Color.argb(Math.round(230 * shown), 255, 240, 214));
            fitted(canvas, clearedWords, middle, top + dp(62), dp(12.5f), half * 2 - dp(24));
            text.setTextAlign(Paint.Align.LEFT);
        }
    }


    /**
     * She has gone into the way on to the next land, past the gate of this one: she comes out of it in the next, as she went in,
     * still running if she was, while the view goes dark about her for a moment and light again. Nothing waits.
     */
    private void cross() {
        boolean left = goLeft, right = goRight, jump = goJump;
        crossing = true;
        play(number + 1);
        crossing = false;
        goLeft = left;
        goRight = right;
        goJump = jump;
        crossAge = 0;
    }

    private void finish(boolean reached) {
        if (delve >= 0) {
            finishFloor(reached);
            return;
        }
        if (blackRung > 0) {
            finishRung(reached);
            return;
        }
        won = reached;
        idle();
        resultClock = 0;
        starsShown = 0;
        screen = RESULT;
        captionLeft = 0;
        // Whatever she collected is hers to spend, whether she reached the gate or not.
        // (And a charm lets her keep more of them.)
        banked = run.purse() * (100 + RunGear.luck(gearOn()) + RunMarket.PAYS * hard()) / 100;
        session.keep("run_bank", bank() + banked);
        if (won) {
            if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "won " + number);
            float share = run.total() == 0 ? 1 : run.collected() / (float) run.total();
            stars = PlusRun.starsFor(share);
            if (stars > starsOf(number)) session.keep("run_stars_" + number, stars);
            session.keepBest("run_time_" + number, Math.max(1, Math.round(run.time())), true);
            if (number >= open() && number < RunLevels.OPEN_TO) session.keep("run_open", number + 1);
            if (number == 1) session.keepBest("run_best", Math.max(1, Math.round(run.time())), true);
            audio.play("win");
            // What whoever waited here gave was hers when they fell; had there been no one to see it then, it is hers now. Her word is about how the land went.
            int late = prize(number);
            if (late != RunLevels.NOTHING) prizeWon = late;
            speak(RunStory.WON[stars - 1], false);
            session.keep("run_mobs", session.tally("run_mobs") + run.flattened());
            session.keep("run_fliers", session.tally("run_fliers") + run.fliers());
            session.keep("run_pet_kills", session.tally("run_pet_kills") + run.petKills());
            pills(number < RunLevels.OPEN_TO ? number == RunLevels.COUNT ? RunStory.SEQUEL : number == RunLevels.SECOND ? RunStory.THIRD : "Next" : "The end", NEXT, "Again", AGAIN,
                    "Lands", TO_MAP);
            // (What this land opened in the gallery, if anything, she will say when she next can.)
            news();
        } else {
            stars = 0;
            session.keep("run_lost", session.tally("run_lost") + 1);
            session.keep("run_mobs", session.tally("run_mobs") + run.flattened());
            session.keep("run_fliers", session.tally("run_fliers") + run.fliers());
            session.keep("run_pet_kills", session.tally("run_pet_kills") + run.petKills());
            audio.play("lose");
            speak(RunStory.LOST[random.nextInt(RunStory.LOST.length)], false);
            pills("Try again", AGAIN, "Lands", TO_MAP);
        }
    }

    private void act(int action) {
        audio.play("tap", .5f);
        buzz(false);
        // What came of getting past one of them is told once, before whatever was asked for.
        String[][] outro = screen == RESULT && won && delve < 0 ? RunStory.outro(number) : null;
        // And after that, where a film belongs to the story, it is shown, the first time: the prologue after the first of them, the
        // idol song after the true ending.
        int film = screen == RESULT && blackRung == 0 && delve < 0 ? RunFilms.due(number, won, session.tally("run_ending"), session.tally(RunFilms.SEEN)) : RunFilms.NONE;
        Runnable afterFilm = film == RunFilms.NONE ? () -> carryOut(action) : () -> showFilm(film, stage, () -> carryOut(action));
        // (The short clips that belong to the land just won come first, each once.)
        int landWon = number;
        Runnable then = screen == RESULT && won && blackRung == 0 && delve < 0 ? () -> afterClips(landWon, afterFilm) : afterFilm;
        if (outro != null && untold("run_outro2", number)) tell(outro, RunLevels.chapterOf(number), then);
        else then.run();
    }

    private void carryOut(int action) {
        if (action == RESUME) {
            screen = PLAY;
        } else if (action == DELVE_GO) {
            startDive(delveAt);
        } else if (action >= BLESS && action < BLESS + 3) {
            bless(offeredNow[action - BLESS]);
        } else if (delve >= 0 && action == AGAIN) {
            startDive(delve);
        } else if (delve >= 0 && action == TO_MAP) {
            toMap();
        } else if (action == COUNTER) {
            counterFrom = STALL;
            screen = SHOP;
            speak(RunStory.SHOP[0], false);
        } else if (action == AGAIN) {
            begin(number, true);
        } else if (blackRung > 0 && action == NEXT) {
            // The next rung of the black arena.
            blackRung = Math.min(RunMarket.RUNGS, blackRung + 1);
            begin(RunMarket.LANDS[blackRung - 1], true);
        } else if (action == TO_MAP || number >= RunLevels.OPEN_TO) {
            if (number >= RunLevels.COUNT) turnTo(volumes() - 1);
            toMap();
        } else {
            play(number + 1);
        }
    }

    // The end of the last land

    /** The white that is truly hers has formed, and Lucy is a dragon: the fight leaves the ground. */
    private void takeOff() {
        finale = FLIGHT;
        finaleAge = 0;
        cheerAge = 9;
        flightNote = null;
        flightWords.clear();
        idle();
        dye(PlusRun.WHITE);
        // (Every heart she has: the white that formed has made her whole.)
        flight = new RunFlight(run.maxHearts() * 2, Math.max(1, 2 + run.bite() - perks.defence), random.nextLong());
        seenFlightHits = seenFlightHurts = seenFlightBeams = seenFlightStages = seenFlightThrown = 0;
        flash = .8f;
        audio.play("flap", .9f);
        buzz(true);
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "flight");
    }

    /**
     * The clip of the reveal is laid over the land and played. When it is over, by itself or by a tap, the rest of what is read is
     * told; if it could not be played, the line it stands for is told first, as it was before there was a clip. Her voice is in the
     * clip, saying the player's name as she was recorded saying it: so it is heard as her other lines are, with the game's sound on
     * and that name unchanged; if not, the clip is silent, and its words are under it all the same.
     */
    private void watch() {
        screen = PLAY;
        finale = WATCHING;
        finaleAge = 0;
        idle();
        captionLeft = 0;
        String[][] all = RunStory.REVEAL;
        int chapter = RunLevels.chapterOf(number);
        boolean heard = audio.on() && Chatter.DEFAULT_NAME.equals(names.nickname());
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "watch");
        // (Its words are under it in the language chosen for what is written under the films, or not at all.)
        int script = session.tally(RunFilms.CAPTIONS);
        video = RunVideo.over(stage, R.raw.run_cut_mother, heard, script < RunFilms.CLIP_SAYS.length ? names.named(RunFilms.CLIP_SAYS[script]) : null, played -> {
            video = null;
            // (Watched here, it can be watched again in the gallery.)
            if (played) {
                session.keep(RunFilms.SEEN, session.tally(RunFilms.SEEN) | RunFilms.bit(RunFilms.CLIP));
                news();
            }
            if (finale != WATCHING) return;
            finale = FLOWN;
            finaleAge = 0;
            tell(java.util.Arrays.copyOfRange(all, played ? RunStory.CUT_AT + 1 : RunStory.CUT_AT, all.length), chapter, this::rise);
        });
    }

    /** Whether there is anything after the flight: there is, since what is read from the book is there to be told. */
    private boolean fourth() { return RunStory.REVEAL.length > 0 || BuildConfig.DEBUG && session.tally("run_fourth") > 0; }

    /** After the flight, and after what the book says has been read: the sky is another sky, and the Mother Tongue rises into it. That it has been seen is kept. */
    private void rise() {
        screen = PLAY;
        finale = ARISING;
        finaleAge = 0;
        ride = null;
        flightNote = null;
        flightWords.clear();
        talk.clear();
        captionLeft = 0;
        idle();
        dye(PlusRun.WHITE);
        session.keep("run_ending", session.tally("run_ending") | 4);
        if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "rise");
        audio.play("quake", .8f);
        shake = .5f;
        buzz(true);
    }

    /** Halgakos is on Lucy's back, with every heart she has, and the ride begins: from here on a life lost begins it again. */
    private void mount() {
        ride = new RunRide(run.maxHearts() * 2, Math.max(1, 2 + run.bite() - perks.defence), random.nextLong());
        seenRideHits = seenRideHurts = seenRideBeams = seenRideStages = seenRideSent = seenRidePuffs = seenRideChips = seenRideOpens = 0;
        cheerAge = 9;
        flash = .4f;
        audio.play("flap", .9f);
    }

    private int seenRideMet, seenRideBursts;
    private static final float[][] RIDE_WAYS = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
    private boolean seenRideDesperate;

    private void rideTick(float seconds) {
        // (In a build made for testing it can be ridden by itself, to be looked at: at her, with both buttons held.)
        if (BuildConfig.DEBUG && session.tally("run_sweep") == 1) {
            // (As the tests ride it: when something is about to land where they are, the way that keeps them clearest of all of it; otherwise
            // at her, with the breath always and the beam when she is open or nearly.)
            float x = ride.x(), y = ride.y(), pace = RunRide.SPEED * (ride.beam() > 0 ? RunRide.SLOWED : 1);
            boolean threatened = false;
            for (RunRide.Thing thing : ride.things()) {
                threatened |= !thing.gone && thing.far / thing.speed < (thing.kind == RunRide.NAME ? .5f : 1.7f) && RunRide.clear(thing, x, y) < .08f;
            }
            if (threatened) {
                int chosen = 0;
                float best = -9;
                for (int way = 0; way < RIDE_WAYS.length; way++) {
                    float least = 9;
                    for (RunRide.Thing thing : ride.things()) {
                        float until = thing.far / thing.speed;
                        if (thing.gone || until > (thing.kind == RunRide.NAME ? .6f : 1.9f)) continue;
                        float thereX = Math.max(-1, Math.min(1, x + RIDE_WAYS[way][0] * pace * until)), thereY = Math.max(-RunRide.HIGH, Math.min(RunRide.HIGH, y + RIDE_WAYS[way][1] * pace * until));
                        least = Math.min(least, Math.min(.2f, RunRide.clear(thing, thereX, thereY)));
                    }
                    if (least > best + .001f) {
                        best = least;
                        chosen = way;
                    }
                }
                ride.step(seconds, RIDE_WAYS[chosen][0] < 0, RIDE_WAYS[chosen][0] > 0, RIDE_WAYS[chosen][1] < 0, RIDE_WAYS[chosen][1] > 0, false, true);
            } else {
                ride.step(seconds, ride.herX() < x - .06f, ride.herX() > x + .06f, ride.herY() < y - .06f, ride.herY() > y + .06f, ride.open() > 0 || ride.shield() <= 1, true);
            }
        } else {
            ride.step(seconds, goLeft, goRight, goJump, goDown, goStrike, goBreath);
        }
        if (ride.beams() != seenRideBeams) {
            seenRideBeams = ride.beams();
            audio.play("form", .7f);
        }
        if (ride.puffs() != seenRidePuffs) {
            seenRidePuffs = ride.puffs();
            audio.play("flap", .5f);
        }
        if (ride.sent() != seenRideSent) {
            seenRideSent = ride.sent();
            audio.play("page", .5f);
        }
        if (ride.chips() != seenRideChips) {
            seenRideChips = ride.chips();
            audio.play("parry", .6f);
        }
        if (ride.opens() != seenRideOpens) {
            seenRideOpens = ride.opens();
            audio.play("crumble", .7f);
            flash = .3f;
            buzz(true);
        }
        if (ride.hits() != seenRideHits) {
            if (ride.hits() % 4 == 1) audio.play("page", .4f);
            seenRideHits = ride.hits();
        }
        if (ride.hurts() != seenRideHurts) {
            seenRideHurts = ride.hurts();
            audio.play("hurt");
            buzz(true);
            shake = .25f;
        }
        if (ride.met() != seenRideMet) {
            // A blast of Lucy's has met the beam on her: the sky shakes with it.
            seenRideMet = ride.met();
            audio.play("star", .9f);
            audio.play("fire", .7f);
            flash = Math.max(flash, .22f);
            shake = Math.max(shake, .35f);
            buzz(true);
        }
        if (ride.bursts() != seenRideBursts) {
            seenRideBursts = ride.bursts();
            audio.play("pop", .8f);
            shake = Math.max(shake, .15f);
        }
        if (ride.desperate() && !seenRideDesperate) {
            seenRideDesperate = true;
            audio.play("quake", 1);
            flash = Math.max(flash, .4f);
            shake = Math.max(shake, .6f);
            buzz(true);
        }
        if (ride.stages() != seenRideStages) {
            seenRideStages = ride.stages();
            flash = .5f;
            shake = .4f;
            cheerAge = 0;
            audio.play("star", .8f);
            buzz(true);
        }
        if (ride.state() == RunRide.WON) {
            finale = RIDDEN;
            finaleAge = 0;
            if (BuildConfig.DEBUG) android.util.Log.i("RunFinale", "ridden");
            flash = 1;
            idle();
            talk.clear();
            audio.play("star", 1);
            audio.play("win", .6f);
            buzz(true);
        } else if (ride.state() == RunRide.FELL) {
            audio.play("lose", .5f);
            if (run.spend()) {
                flightNote = run.lives() == 1 ? "Last life: back on Lucy's back" : "A life lost, " + run.lives() + " left: back on Lucy's back";
                flightNoteAge = 0;
                mount();
            } else {
                finish(false);
            }
        }
    }

    /**
     * The ride: the sky of pages; the Mother Tongue far ahead, with her shield about her in pieces, or open; what she sends, each
     * thing with a mark where it will arrive: a row of blocks with a way through, a press closing on one height, a name that
     * follows, and her own pass down one side; Lucy's breath crossing the sky; and Halgakos on Lucy's back, from behind, with the
     * beam going away from them. Before it, the Mother rising; and after it, the Mother made small, and listening.
     */
    /** The ride's effects, drawn: Lucy's ink fire, its blast, its flare at her jaws; light bursting with paper; glowing pages and letters; a ring of runes; a column of light; a band of golden script. */
    private Bitmap fxFire, fxBlast, fxMuzzle, fxLight, fxPages, fxRing, fxColumn, fxScript, fxSeal, fxSigil, fxImpact;

    /** Soft round glows, one picture for each colour, made once and drawn at any size: far cheaper than a gradient made every frame. And the red about the sky's edges. */
    private final java.util.Map<Integer, Bitmap> glows = new java.util.HashMap<>();
    private Bitmap vignette;

    void glowAt(Canvas canvas, float x, float y, float radius, int colour, int alpha) {
        if (BuildConfig.DEBUG && (session.tally("run_fxoff") & 4) != 0) return;
        Bitmap glow = glows.get(colour);
        if (glow == null) {
            glow = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
            Paint soft = new Paint(Paint.ANTI_ALIAS_FLAG);
            soft.setShader(new android.graphics.RadialGradient(64, 64, 64, new int[]{colour | 0xFF000000, (colour & 0xFFFFFF) | 0x8C000000, colour & 0xFFFFFF}, new float[]{0, .4f, 1},
                    android.graphics.Shader.TileMode.CLAMP));
            new Canvas(glow).drawCircle(64, 64, 64, soft);
            glows.put(colour, glow);
        }
        paint.setAlpha(Math.max(0, Math.min(255, alpha)));
        target.set(x - radius, y - radius, x + radius, y + radius);
        canvas.drawBitmap(glow, null, target, paint);
    }

    /** A picture laid in a box side by side, each copy as tall as the box and as wide as that makes it. */
    private void tiles(Canvas canvas, Bitmap picture, RectF in) {
        if (picture == null || in.width() <= 0 || in.height() <= 0) return;
        float each = in.height() * picture.getWidth() / picture.getHeight();
        canvas.save();
        canvas.clipRect(in);
        for (float along = in.left; along < in.right; along += each) {
            target.set(along, in.top, along + each + 1, in.bottom);
            canvas.drawBitmap(picture, null, target, paint);
        }
        canvas.restore();
    }

    private Bitmap fxOf(int resource) { return BitmapFactory.decodeResource(context.getResources(), resource); }

    /** A frame of an effect's strip, its middle at a place, so wide and tall, turned by so many degrees. */
    private void fx(Canvas canvas, Bitmap sheet, int count, int index, float x, float y, float wide, float tall, float turn) {
        if (sheet == null) return;
        int side = sheet.getWidth() / count;
        source.set(Math.floorMod(index, count) * side, 0, Math.floorMod(index, count) * side + side, sheet.getHeight());
        target.set(x - wide / 2, y - tall / 2, x + wide / 2, y + tall / 2);
        canvas.save();
        if (turn != 0) canvas.rotate(turn, x, y);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
    }

    /** Glowing words: a gold light behind the letters. */
    private void glow(boolean on, float size) {
        if (on) text.setShadowLayer(size * .45f, 0, 0, 0xFFFFD36A);
        else text.clearShadowLayer();
    }

    /** The words on her wide ones: a sweep of her script, and the rain of it. */
    private static final String[] SWEEP_WORDS = {"Grammar", "Correct", "Proper", "Standard", "Approved", "Official"};
    private static final String[] RAIN_WORDS = {"no", "wrong", "fix", "redo", "sic", "x"};

    private void drawRide(Canvas canvas) {
        if (pagesSky == null) pagesSky = BitmapFactory.decodeResource(context.getResources(), R.drawable.plus_run_sky13);
        if (rideArt == null) rideArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_ride_back);
        if (motherArt == null) motherArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_mother);
        if (fxFire == null) {
            fxFire = fxOf(R.drawable.run_fx_inkfire);
            fxBlast = fxOf(R.drawable.run_fx_inkblast);
            fxMuzzle = fxOf(R.drawable.run_fx_muzzle);
            fxLight = fxOf(R.drawable.run_fx_lightburst);
            fxPages = fxOf(R.drawable.run_fx_pages);
            fxRing = fxOf(R.drawable.run_fx_runering);
            fxColumn = fxOf(R.drawable.run_fx_lightcolumn);
            fxScript = fxOf(R.drawable.run_fx_goldscript);
            fxSeal = fxOf(R.drawable.run_fx_sealwall);
            fxSigil = fxOf(R.drawable.run_fx_sigil);
            fxImpact = fxOf(R.drawable.run_fx_impact);
        }
        float fit = Math.max(width / (float) pagesSky.getWidth(), height / (float) pagesSky.getHeight()) * 1.04f;
        target.set(width / 2f - pagesSky.getWidth() * fit / 2 + (float) Math.sin(clock * .23f) * dp(8), height / 2f - pagesSky.getHeight() * fit / 2 + (float) Math.cos(clock * .19f) * dp(8),
                width / 2f + pagesSky.getWidth() * fit / 2 + (float) Math.sin(clock * .23f) * dp(8), height / 2f + pagesSky.getHeight() * fit / 2 + (float) Math.cos(clock * .19f) * dp(8));
        paint.setAlpha(255);
        canvas.drawBitmap(pagesSky, null, target, paint);
        float cx = safe.centerX(), cy = safe.top + safe.height() * .6f, wide = safe.width() * .4f, high = safe.height() * .17f;
        float farY = safe.top + safe.height() * .36f, far = .5f, riders = tile * 4.4f, grand = tile * 5.4f;
        canvas.save();
        if (shake > 0) canvas.translate((random.nextFloat() - .5f) * tile * .28f * Math.min(1, shake * 4), (random.nextFloat() - .5f) * tile * .28f * Math.min(1, shake * 4));
        paint.setStyle(Paint.Style.FILL);
        for (int page = 0; page < 16; page++) {
            float along = (clock * (.3f + page % 5 * .05f) + page * .137f) % 1, turn = page * 2.399f, reach = along * along * Math.max(width, height) * .75f;
            float x = cx + (float) Math.cos(turn) * reach, y = farY + (float) Math.sin(turn) * reach, half = tile * .18f * (.3f + along);
            canvas.save();
            canvas.rotate(page * 47 + clock * 90, x, y);
            paint.setColor(0xFFFFF7E6);
            paint.setAlpha(Math.round(170 * along));
            canvas.drawRect(x - half, y - half * 1.35f, x + half, y + half * 1.35f, paint);
            canvas.restore();
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        if (finale != RIDE || ride == null) {
            // Before: she rises out of the pages, vast, and her name with her. After: she is small, and listens; and the two of them hang there.
            boolean after = finale == RIDDEN;
            float age = finaleAge, come = Math.min(1, age / 2.2f), ease = come * come * (3 - 2 * come), bob = (float) Math.sin(clock * 2) * tile * .1f;
            if (after) {
                if (age < .9f) {
                    paint.setAlpha(255);
                    fx(canvas, fxLight, 6, (int) (age / .9f * 6), cx, farY + grand * .1f, tile * 11, tile * 11, 0);
                    fx(canvas, fxBlast, 6, (int) (age / .9f * 6), cx, farY + grand * .1f, tile * 8, tile * 8, 0);
                }
                float small = Math.max(0, 1 - age / 1.4f);
                if (small > 0) {
                    paint.setAlpha(Math.round(255 * small));
                    cell(canvas, motherArt, 5, 7, cx, farY + grand * .5f, grand * (.4f + .6f * small), false);
                }
                paint.setAlpha(Math.round(255 * Math.min(1, Math.max(0, (age - 1) / .8f))));
                cell(canvas, motherArt, 6, 7, cx, farY + grand * .85f + bob * .5f, grand * 1.9f, false);
                paint.setAlpha(255);
                cell(canvas, rideArt, (int) (clock * 6) % 4, 6, cx, cy + riders * .55f - bob, riders, false);
                if (age > 1.6f) word(canvas, "Every tongue, as it is spoken", cx, cy + riders * .75f, Math.min(dp(22), safe.width() / 16f), 0xFFB9862C, Math.min(1, (age - 1.6f) / .6f));
            } else {
                paint.setAlpha(Math.round(255 * Math.min(1, age / .8f)));
                cell(canvas, motherArt, age > 2.6f ? 3 : 0, 7, cx, farY + grand * .5f + (1 - ease) * tile * 7, grand * (1.15f - .15f * ease), false);
                paint.setAlpha(255);
                cell(canvas, rideArt, (int) (clock * 7) % 4, 6, cx, cy + riders * .55f + bob, riders, false);
                if (age > 1.2f) word(canvas, "THE MOTHER TONGUE", cx, safe.top + safe.height() * .2f, Math.min(dp(26), safe.width() / 12f), 0xFFB9862C, Math.min(1, Math.min((age - 1.2f) / .4f, (4 - age) / .5f)));
            }
            canvas.restore();
            if (flash > 0) canvas.drawColor(Color.argb(Math.round(255 * Math.min(1, flash / .5f)), 255, 255, 255));
            flightHud(canvas, false);
            return;
        }
        // The Mother, far ahead: serene; ordering, as she sends something; cracked, while she is open; recoiling, as she is read and as a stage ends.
        float herX = cx + ride.herX() * wide * far, herY = farY + ride.herY() * high * far;
        boolean open = ride.open() > 0, away = ride.passing();
        int pose = ride.reel() > 0 && ride.stages() > 0 || ride.sinceRead() < .22f ? 5 : open ? 2 : ride.sinceSent() < .5f ? 4 : ride.sinceChip() < .3f ? 1 : 0;
        if (pose == 5) herX += (float) Math.sin(clock * 40) * tile * .07f;
        paint.setAlpha(away ? 70 : 255);
        cell(canvas, motherArt, pose, 7, herX, herY + grand * .5f, grand, false);
        paint.setAlpha(255);
        // Her shield: a ring of gilt pieces about her, as many as are left; gone, a light on her for as long as she is open.
        if (!away) {
            box.set(herX - grand * .5f, herY - grand * .36f, herX + grand * .5f, herY + grand * .36f);
            int pieces = ride.shieldMost();
            stroke.setStrokeWidth(tile * .13f);
            for (int piece = 0; piece < pieces; piece++) {
                boolean there = piece < ride.shield();
                if (!there && !ride.forming()) continue;
                stroke.setColor(ride.sinceChip() < .2f ? Color.WHITE : 0xFFE9C96A);
                stroke.setAlpha(there ? 235 : Math.round(90 + 60 * (float) Math.sin(clock * 18)));
                canvas.drawArc(box, -90 + clock * 20 + piece * 360f / pieces + 5, 360f / pieces - 10, false, stroke);
            }
            stroke.setAlpha(255);
            if (open) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFFFE9A8);
                paint.setAlpha(Math.round((ride.open() < 1 ? 40 : 70) + 40 * (float) Math.sin(clock * (ride.open() < 1 ? 24 : 9))));
                canvas.drawOval(box, paint);
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
            }
        }
        // What is on its way.
        text.setTextAlign(Paint.Align.CENTER);
        // (Her wide ones first, under the rest: a sweep, a ring, the rain and her beam.)
        java.util.List<RunRide.Thing> order = new java.util.ArrayList<>(ride.things());
        order.sort((one, two) -> Boolean.compare(one.kind < RunRide.SWEEP || one.kind > RunRide.RAY, two.kind < RunRide.SWEEP || two.kind > RunRide.RAY));
        for (RunRide.Thing thing : order) {
            if (thing.gone || BuildConfig.DEBUG && (session.tally("run_fxoff") & 2) != 0) continue;
            float near = 1 - Math.max(0, thing.far), scale = far + (1 - far) * near, level = farY + (cy - farY) * near;
            int warn = Math.round((50 + 130 * near) * (.75f + .25f * (float) Math.sin(clock * 16)));
            paint.setStyle(Paint.Style.FILL);
            if (thing.kind == RunRide.BLOCKS) {
                // A row of blocks, each with a word on it, and one way through: the ways that are shut are marked where they will arrive.
                float gap = RunRide.GAP * wide, tall = high * 1.15f;
                for (int side = -1; side <= 1; side += 2) {
                    float from = side < 0 ? -1.35f * wide : thing.x * wide + gap, to = side < 0 ? thing.x * wide - gap : 1.35f * wide;
                    if (to - from < wide * .05f) continue;
                    paint.setColor(0xFFE0362F);
                    paint.setAlpha(warn / 2);
                    canvas.drawRect(cx + from, cy - tall, cx + to, cy + tall, paint);
                    box.set(cx + from * scale, level - tall * scale, cx + to * scale, level + tall * scale);
                    // A wall of golden rune light, with a bloom of gold about it.
                    paint.setColor(0xFFFFD36A);
                    for (int bloom = 3; bloom >= 1; bloom--) {
                        paint.setAlpha(Math.round((20 + 18 * near) * (4 - bloom) / 3f));
                        canvas.drawRoundRect(box.left - bloom * tile * .16f, box.top - bloom * tile * .16f, box.right + bloom * tile * .16f, box.bottom + bloom * tile * .16f, tile * .3f, tile * .3f, paint);
                    }
                    paint.setAlpha(Math.round(170 + 85 * near));
                    tiles(canvas, fxSeal, box);
                    stroke.setColor(0xFFFFF4C8);
                    stroke.setAlpha(Math.round(150 + 105 * near));
                    stroke.setStrokeWidth(tile * .06f);
                    canvas.drawRect(box, stroke);
                    // (Where it lands, over it.)
                    stroke.setColor(0xFFFF3B30);
                    stroke.setAlpha(Math.round(warn * 1.2f));
                    stroke.setStrokeWidth(tile * .05f);
                    canvas.drawRect(cx + from, cy - tall, cx + to, cy + tall, stroke);
                }
                stroke.setAlpha(255);
                text.setAlpha(255);
            } else if (thing.kind == RunRide.PRESS) {
                // A press: two plates closing from above and below on one height, where the only room is.
                float slit = RunRide.SLIT * high, at = thing.y * high;
                paint.setColor(0xFFE0362F);
                paint.setAlpha(warn / 2);
                canvas.drawRect(0, cy - high * 1.15f, width, cy + at - slit, paint);
                canvas.drawRect(0, cy + at + slit, width, cy + high * 1.15f, paint);
                float reach = safe.width() * (.3f + .35f * near);
                // Two plates of golden rune light closing on one height.
                paint.setAlpha(Math.round(160 + 95 * near));
                box.set(cx - reach, level - high * 1.15f * scale, cx + reach, level + (at - slit) * scale);
                tiles(canvas, fxSeal, box);
                box.set(cx - reach, level + (at + slit) * scale, cx + reach, level + high * 1.15f * scale);
                tiles(canvas, fxSeal, box);
                // Its closing edges blaze.
                for (int edge = 0; edge < 2; edge++) {
                    float ey = level + (edge == 0 ? at - slit : at + slit) * scale;
                    stroke.setColor(0xFFFFD36A);
                    stroke.setAlpha(Math.round(120 * near));
                    stroke.setStrokeWidth(tile * .35f);
                    canvas.drawLine(cx - reach, ey, cx + reach, ey, stroke);
                    stroke.setColor(Color.WHITE);
                    stroke.setAlpha(Math.round(230 * near));
                    stroke.setStrokeWidth(tile * .08f);
                    canvas.drawLine(cx - reach, ey, cx + reach, ey, stroke);
                }
                // (Where it lands, over it.)
                stroke.setColor(0xFFFF3B30);
                stroke.setAlpha(Math.round(warn * 1.2f));
                stroke.setStrokeWidth(tile * .05f);
                canvas.drawLine(0, cy + at - slit, width, cy + at - slit, stroke);
                canvas.drawLine(0, cy + at + slit, width, cy + at + slit, stroke);
                stroke.setAlpha(255);
            } else if (thing.kind == RunRide.SWEEP) {
                // A sweep: a band of her script across the whole sky at one height, coming from far off; where it will be is marked from edge to edge.
                float band = RunRide.BAND * high, at = cy + thing.y * high;
                paint.setColor(0xFFE0362F);
                paint.setAlpha(warn / 2);
                canvas.drawRect(0, at - band, width, at + band, paint);
                float y = level + thing.y * high * scale, half = band * scale, reach = width * (.35f + .65f * near);
                // A wave of radiant golden calligraphy, drifting along, with her words glowing on it.
                paint.setAlpha(Math.round(160 + 95 * near));
                if (fxScript != null) {
                    float tallBand = half * 3.2f, wideOne = tallBand * fxScript.getWidth() / fxScript.getHeight();
                    canvas.save();
                    canvas.clipRect(cx - reach, y - tallBand, cx + reach, y + tallBand);
                    for (float along = cx - reach - (clock * tile * 2.5f) % wideOne; along < cx + reach; along += wideOne) {
                        target.set(along, y - tallBand / 2, along + wideOne + 1, y + tallBand / 2);
                        canvas.drawBitmap(fxScript, null, target, paint);
                    }
                    canvas.restore();
                }
                for (int spark = 0; spark < 10; spark++) {
                    float sx = cx - reach + ((spark * .137f + clock * .4f) % 1) * reach * 2, sy = y + (float) Math.sin(clock * 5 + spark) * half * 1.3f;
                    paint.setColor(spark % 2 == 0 ? 0xFFFFF4C0 : 0xFFFFD36A);
                    paint.setAlpha(Math.round(200 * near));
                    canvas.drawCircle(sx, sy, tile * .08f * (1 + (spark % 3)), paint);
                }
                text.setAlpha(255);
                // (Where it lands, drawn over it, so it stays clear.)
                stroke.setColor(0xFFFF3B30);
                stroke.setAlpha(Math.round(warn * 1.2f));
                stroke.setStrokeWidth(tile * .05f);
                canvas.drawLine(0, at - band, width, at - band, stroke);
                canvas.drawLine(0, at + band, width, at + band, stroke);
                text.setAlpha(255);
                stroke.setAlpha(255);
            } else if (thing.kind == RunRide.RING) {
                // A ring growing out from her, arriving at one width: its band is marked where it will be; inside it or outside it is clear.
                float rx = thing.size * wide, ry = thing.size * high, bx = RunRide.RING_WIDE * wide, by = RunRide.RING_WIDE * high;
                float mx = cx + thing.x * wide, my = cy + thing.y * high;
                stroke.setColor(0xFFE0362F);
                stroke.setAlpha(warn / 2);
                stroke.setStrokeWidth((bx + by));
                canvas.drawOval(mx - rx, my - ry, mx + rx, my + ry, stroke);
                float grow = near, sx = herX + (mx - herX) * near, sy = herY + (my - herY) * near;
                // A shining shockwave of runes, turning as it grows.
                paint.setAlpha(Math.round(255 * Math.min(1, .25f + near)));
                if (fxRing != null) {
                    canvas.save();
                    canvas.rotate(clock * 40, sx, sy);
                    target.set(sx - (rx + bx) * grow * 1.08f, sy - (ry + by) * grow * 1.08f, sx + (rx + bx) * grow * 1.08f, sy + (ry + by) * grow * 1.08f);
                    canvas.drawBitmap(fxRing, null, target, paint);
                    canvas.restore();
                }
                stroke.setColor(0xFFFFF4D6);
                stroke.setAlpha(Math.round(200 * near));
                stroke.setStrokeWidth((bx + by) * .25f);
                canvas.drawOval(sx - rx * grow, sy - ry * grow, sx + rx * grow, sy + ry * grow, stroke);
                // (Where it lands, over it.)
                stroke.setColor(0xFFFF3B30);
                stroke.setAlpha(Math.round(warn * 1.2f));
                stroke.setStrokeWidth(tile * .05f);
                canvas.drawOval(mx - rx - bx, my - ry - by, mx + rx + bx, my + ry + by, stroke);
                canvas.drawOval(mx - rx + bx, my - ry + by, mx + rx - bx, my + ry - by, stroke);
                stroke.setAlpha(255);
            } else if (thing.kind == RunRide.RAIN) {
                // A rain of her words over the whole sky, with two gaps in it: everything but the gaps is marked.
                float g = RunRide.RAIN_GAP * wide, a = cx + thing.x * wide, b = cx + thing.other * wide, low = Math.min(a, b), highGap = Math.max(a, b);
                paint.setColor(0xFFE0362F);
                paint.setAlpha(warn / 2);
                canvas.drawRect(0, safe.top, low - g, cy + high * 1.2f, paint);
                canvas.drawRect(low + g, safe.top, highGap - g, cy + high * 1.2f, paint);
                canvas.drawRect(highGap + g, safe.top, width, cy + high * 1.2f, paint);
                // Luminous pages and letters falling, with trails of light; the gaps are left clear.
                int drops = Math.max(14, Math.round(width / (tile * .9f)));
                for (int drop = 0; drop < drops; drop++) {
                    float x = (drop + .5f) * width / drops;
                    if (Math.abs(x - a) < g || Math.abs(x - b) < g) continue;
                    float y = safe.top + (near + (drop * .37f) % .3f - .3f) * (cy + high * 1.2f - safe.top) * 1.2f;
                    if (y < safe.top) continue;
                    paint.setAlpha(Math.round(150 + 105 * near));
                    float size = tile * (1.0f + (drop % 3) * .2f);
                    fx(canvas, fxPages, 8, drop * 3, x, y, size, size, (float) Math.sin(clock * 3 + drop) * 12);
                }
                // (Where it lands, over it: the edges of the two gaps.)
                stroke.setColor(0xFFFF3B30);
                stroke.setAlpha(Math.round(warn * 1.2f));
                stroke.setStrokeWidth(tile * .05f);
                for (float edge : new float[]{low - g, low + g, highGap - g, highGap + g}) canvas.drawLine(edge, safe.top, edge, cy + high * 1.2f, stroke);
                stroke.setAlpha(255);
            } else if (thing.kind == RunRide.RAY) {
                // A beam of hers that swings into a slant across the sky: the slant is marked; it sweeps into place as it arrives.
                float angle = thing.size, nx = -(float) Math.sin(angle) * RunRide.RAY_WIDE, ny = (float) Math.cos(angle) * RunRide.RAY_WIDE;
                float dx = (float) Math.cos(angle) * 3, dy = (float) Math.sin(angle) * 3;
                path.reset();
                path.moveTo(cx + (thing.x - dx + nx) * wide, cy + (thing.y - dy + ny) * high);
                path.lineTo(cx + (thing.x + dx + nx) * wide, cy + (thing.y + dy + ny) * high);
                path.lineTo(cx + (thing.x + dx - nx) * wide, cy + (thing.y + dy - ny) * high);
                path.lineTo(cx + (thing.x - dx - nx) * wide, cy + (thing.y - dy - ny) * high);
                path.close();
                paint.setColor(0xFFE0362F);
                paint.setAlpha(warn / 2);
                canvas.drawPath(path, paint);
                // Her mouth, where it comes from: a light gathering there, and then a blazing column from it, swinging into its slant.
                float mouthX = herX, mouthY = herY - grand * .25f;
                glowAt(canvas, mouthX, mouthY, tile * (.6f + 1.4f * near), 0xFFFFE08A, 255);
                glowAt(canvas, mouthX, mouthY, tile * (.3f + .6f * near), 0xFFFFFFFF, 255);
                if (near > .45f) {
                    float swing = Math.min(1, (near - .45f) / .55f);
                    float px = cx + thing.x * wide, py = cy + thing.y * high;
                    float aim = (float) Math.atan2(py - mouthY, px - mouthX) + (1 - swing) * (px < mouthX ? -.9f : .9f);
                    float length = Math.max(width, height) * 1.5f, thick = RunRide.RAY_WIDE * Math.min(wide, high) * 4.2f * (1 + .06f * (float) Math.sin(clock * 30));
                    paint.setAlpha(Math.round(255 * Math.min(1, swing * 1.5f)));
                    if (fxColumn != null) {
                        canvas.save();
                        canvas.rotate((float) Math.toDegrees(aim) - 90, mouthX, mouthY);
                        target.set(mouthX - thick / 2, mouthY, mouthX + thick / 2, mouthY + length);
                        canvas.drawBitmap(fxColumn, null, target, paint);
                        canvas.restore();
                    }
                }
                // (Where it lands, over it.)
                stroke.setColor(0xFFFF3B30);
                stroke.setAlpha(Math.round(warn * 1.2f));
                stroke.setStrokeWidth(tile * .05f);
                canvas.drawPath(path, stroke);
                stroke.setAlpha(255);
            } else if (thing.kind == RunRide.BURST || thing.kind == RunRide.SHARD) {
                // A word that will burst where it arrives, and the pieces of one that has: a ring where each will land, and the word itself, glowing.
                boolean piece = thing.kind == RunRide.SHARD;
                float r = (piece ? RunRide.SHARD_NEAR : RunRide.NEAR), mx = cx + thing.x * wide, my = cy + thing.y * high;
                stroke.setColor(0xFFE0362F);
                stroke.setAlpha(warn);
                stroke.setStrokeWidth(tile * .06f);
                canvas.drawOval(mx - r * wide, my - r * high, mx + r * wide, my + r * high, stroke);
                stroke.setAlpha(255);
                float x = piece ? mx : cx + thing.x * wide * scale, y = piece ? my - (1 - near) * tile * 2 : level + thing.y * high * scale, size = tile * (piece ? .9f : .9f + 1.4f * near);
                // A shining sigil of ink, turning, that will burst.
                paint.setAlpha(255);
                if (fxSigil != null) {
                    canvas.save();
                    canvas.rotate(clock * (piece ? 220 : 120) + thing.word * 37, x, y);
                    target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
                    canvas.drawBitmap(fxSigil, null, target, paint);
                    canvas.restore();
                }
            } else if (thing.kind == RunRide.PASS) {
                // Her pass: its side of the sky is marked, and she comes down it, vast, as it arrives.
                paint.setColor(0xFFE0362F);
                paint.setAlpha(warn / 2);
                canvas.drawRect(cx + (thing.x - RunRide.PASS_WIDE) * wide, safe.top, cx + (thing.x + RunRide.PASS_WIDE) * wide, cy + high * 1.3f, paint);
                paint.setColor(Color.BLACK);
                paint.setAlpha(Math.round(255 * Math.min(1, near * 2)));
                cell(canvas, motherArt, 3, 7, cx + thing.x * wide * scale, farY - grand * .2f + near * near * (cy + high - farY + grand * 1.3f), grand * (1 + 1.3f * near), false);
            } else {
                // A name: a tag with the word on it, which comes after them; a ring where it would arrive.
                float x = cx + thing.x * wide * scale, size = tile * (.34f + .5f * near);
                stroke.setColor(0xFFE0362F);
                stroke.setAlpha(warn);
                stroke.setStrokeWidth(tile * .06f);
                canvas.drawOval(cx + thing.x * wide - wide * RunRide.NEAR, cy + thing.y * high - high * RunRide.NEAR, cx + thing.x * wide + wide * RunRide.NEAR, cy + thing.y * high + high * RunRide.NEAR,
                        stroke);
                stroke.setAlpha(255);
                // A seal of her ink that follows them, turning.
                float y = level + thing.y * high * scale, seal = size * 2.4f;
                paint.setAlpha(255);
                if (fxSigil != null) {
                    canvas.save();
                    canvas.rotate(-clock * 160 + thing.word * 53, x, y);
                    target.set(x - seal / 2, y - seal / 2, x + seal / 2, y + seal / 2);
                    canvas.drawBitmap(fxSigil, null, target, paint);
                    canvas.restore();
                }
            }
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        // Where they are; where the beam and the breath would reach, far off: a ring there, gold while she is before it.
        float atX = cx + ride.x() * wide, atY = cy + ride.y() * high, aimX = cx + ride.x() * wide * far, aimY = farY + ride.y() * high * far;
        boolean before = !away && Math.abs(ride.x() - ride.herX()) < RunRide.TRUE && Math.abs(ride.y() - ride.herY()) < RunRide.TRUE;
        stroke.setColor(before ? 0xFFFFD24A : Color.WHITE);
        stroke.setAlpha(before ? 255 : 150);
        stroke.setStrokeWidth(tile * (before ? .08f : .05f));
        canvas.drawCircle(aimX, aimY, tile * (before ? .5f + .06f * (float) Math.sin(clock * 14) : .4f), stroke);
        stroke.setAlpha(255);
        // The two of them, from behind: Lucy's wings going; and Halgakos up on her back with the book out while the beam is gathered or out.
        paint.setAlpha(ride.safe() && (int) (clock * 14) % 2 == 0 ? 90 : 255);
        cell(canvas, rideArt, ride.charge() > 0 || ride.beam() > 0 ? 5 : (int) (clock * 7) % 4, 6, atX, atY + riders * .5f, riders, false);
        paint.setAlpha(255);
        // Where her fire comes from, Lucy's jaws, at her head; and where the beam comes from, the book Halgakos holds up.
        float mouthX = atX, mouthY = atY - riders * .27f, bookX = atX, bookY = atY - riders * .25f;
        // Lucy's breath: fire the colour of ink pouring from her jaws, joined to them all the way to its blazing head, smaller as it goes.
        paint.setStyle(Paint.Style.FILL);
        int seed = 0;
        boolean pouring = false;
        for (RunRide.Breath puff : ride.breaths()) {
            if (BuildConfig.DEBUG && (session.tally("run_fxoff") & 1) != 0) break;
            seed++;
            float gone = Math.min(1, puff.gone);
            float toX = cx + puff.x * wide * far, toY = farY + puff.y * high * far;
            float headX = mouthX + (toX - mouthX) * gone, headY = mouthY + (toY - mouthY) * gone;
            float turn = (float) Math.toDegrees(Math.atan2(toX - mouthX, -(toY - mouthY)));
            float scale = 1 - (1 - far) * gone;
            // The stream, from her jaws to its head.
            int parts = 5;
            for (int part = 0; part <= parts; part++) {
                float along = gone * part / parts, size = tile * (1.3f + 2.2f * (part / (float) parts)) * (1 - (1 - far) * along);
                paint.setAlpha(Math.round(120 + 135 * part / (float) parts));
                fx(canvas, fxFire, 8, (int) (clock * 16) + seed * 3 + part * 2, mouthX + (toX - mouthX) * along, mouthY + (toY - mouthY) * along, size, size, turn);
            }
            paint.setAlpha(255);
            fx(canvas, fxFire, 8, (int) (clock * 14) + seed * 3, headX, headY, tile * 3.6f * scale, tile * 3.6f * scale, turn);
            pouring |= gone < .5f;
        }
        if (pouring || ride.puffs() > 0 && ride.wind() < .32f) {
            // The flame leaving her jaws: a flare of ink fire bursting up from her head.
            float flare = riders * .7f;
            paint.setAlpha(255);
            fx(canvas, fxMuzzle, 4, ride.wind() < .32f ? (int) (ride.wind() / .32f * 4) : 2, mouthX, mouthY - flare * .32f, flare, flare, 0);
        }
        if (ride.beam() > 0) {
            float life = ride.beam() / RunRide.LASTS, swell = (float) Math.sin(Math.min(1, (1 - life) * 6) * Math.PI / 2) * Math.min(1, life * 5);
            // It ends on her, where it strikes her, while she is before it; and otherwise far off, where it goes on into the sky.
            boolean striking = ride.onHer();
            float endX = striking ? herX : aimX, endY = striking ? herY : aimY;
            for (int layer = 0; layer < 3; layer++) {
                float nearHalf = tile * .6f * swell * (1.25f - layer * .42f) * (1 + .08f * (float) Math.sin(clock * 40 + layer)), farHalf = nearHalf * .34f;
                path.reset();
                path.moveTo(bookX - nearHalf * .5f, bookY);
                path.lineTo(bookX + nearHalf * .5f, bookY);
                path.lineTo(endX + farHalf, endY);
                path.lineTo(endX - farHalf, endY);
                path.close();
                paint.setColor(layer == 0 ? 0xFFFFE9A8 : layer == 1 ? 0xFFFFF7DE : Color.WHITE);
                paint.setAlpha(Math.round((layer == 0 ? 110 : layer == 1 ? 190 : 255) * (striking ? 1 : .8f)));
                canvas.drawPath(path, paint);
            }
            // Its light at the book.
            glowAt(canvas, bookX, bookY, tile * 1.5f * swell + 1, 0xFFFFE9A8, 255);
            glowAt(canvas, bookX, bookY, tile * .7f * swell + 1, 0xFFFFFFFF, 255);
            // And where it strikes her: a splash of light on her body.
            paint.setAlpha(255);
            if (striking) fx(canvas, fxImpact, 6, (int) (clock * 18), endX, endY, tile * (ride.reads() ? 4.2f : 3f), tile * (ride.reads() ? 4.2f : 3f), clock * 40);
        }
        paint.setColor(Color.BLACK);
        // Where the last of Lucy's blasts landed, far off: an explosion of ink fire.
        if (ride.sinceBlast() < .45f) {
            float bx = cx + ride.blastX() * wide * far, by = farY + ride.blastY() * high * far;
            paint.setAlpha(255);
            fx(canvas, fxBlast, 6, (int) (ride.sinceBlast() / .45f * 6), bx, by, tile * 4.6f, tile * 4.6f, 0);
        }
        // Where a word burst: light and paper.
        if (ride.sinceBurst() < .45f) {
            float bx = cx + ride.burstX() * wide, by = cy + ride.burstY() * high;
            paint.setAlpha(255);
            fx(canvas, fxLight, 6, (int) (ride.sinceBurst() / .45f * 6), bx, by, tile * 4f, tile * 4f, 0);
        }
        // Where Lucy's fire and the beam meet on her: light bursts out of her, and rings of violet and gold and white, and rays.
        if (ride.met() > 0 && ride.sinceMet() < .5f) {
            paint.setAlpha(255);
            fx(canvas, fxLight, 6, (int) (ride.sinceMet() / .5f * 6), herX, herY, tile * 6.5f, tile * 6.5f, 0);
            fx(canvas, fxBlast, 6, (int) (ride.sinceMet() / .5f * 6), herX, herY, tile * 5f, tile * 5f, 0);
        }
        if (ride.met() > 0 && ride.sinceMet() < .9f) {
            float age = ride.sinceMet() / .9f, out = (float) Math.sqrt(age) * tile * 4.2f, fade = 1 - age;
            for (int ray = 0; ray < 12; ray++) {
                double turn = ray * Math.PI / 6 + clock * 1.5;
                stroke.setColor(ray % 2 == 0 ? 0xFFFFE9A8 : 0xFFB98CFF);
                stroke.setAlpha(Math.round(230 * fade));
                stroke.setStrokeWidth(tile * .12f * fade + 1);
                canvas.drawLine(herX + (float) Math.cos(turn) * out * .25f, herY + (float) Math.sin(turn) * out * .25f, herX + (float) Math.cos(turn) * out * 1.2f, herY + (float) Math.sin(turn) * out * 1.2f, stroke);
            }
            for (int ring = 0; ring < 3; ring++) {
                stroke.setColor(ring == 0 ? 0xFF6A3BD6 : ring == 1 ? 0xFFFFC94A : Color.WHITE);
                stroke.setAlpha(Math.round(240 * fade));
                stroke.setStrokeWidth(tile * (.4f - ring * .12f) * fade + 1);
                canvas.drawCircle(herX, herY, out * (1 - ring * .22f), stroke);
            }
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(200 * fade * fade));
            canvas.drawCircle(herX, herY, tile * 1.4f * (1 + age), paint);
            stroke.setAlpha(255);
            paint.setColor(Color.BLACK);
        }
        if (ride.desperate()) {
            // Her desperation: the sky about the edges pulses red, and she says what she is doing.
            float pulse = .5f + .5f * (float) Math.sin(clock * 6);
            if (vignette == null || vignette.getWidth() != Math.max(1, width / 4) || vignette.getHeight() != Math.max(1, height / 4)) {
                vignette = Bitmap.createBitmap(Math.max(1, width / 4), Math.max(1, height / 4), Bitmap.Config.ARGB_8888);
                Paint soft = new Paint();
                soft.setShader(new android.graphics.RadialGradient(vignette.getWidth() / 2f, vignette.getHeight() / 2f, Math.max(vignette.getWidth(), vignette.getHeight()) * .75f,
                        new int[]{0x00C81428, 0x00C81428, 0xFFC81428}, new float[]{0, .6f, 1}, android.graphics.Shader.TileMode.CLAMP));
                new Canvas(vignette).drawPaint(soft);
            }
            paint.setAlpha(Math.round(70 + 50 * pulse));
            target.set(0, 0, width, height);
            canvas.drawBitmap(vignette, null, target, paint);
            // The whole sky lit from her: a gold glow about her, and rays of light out across it, turning.
            glowAt(canvas, herX, herY, tile * 7, 0xFFFFECAA, Math.round(150 + 60 * pulse));
            for (int ray = 0; ray < 10; ray++) {
                double turnRay = ray * Math.PI / 5 + clock * .5;
                stroke.setColor(0xFFFFE9A8);
                stroke.setAlpha(Math.round(60 + 40 * pulse));
                stroke.setStrokeWidth(tile * .5f);
                canvas.drawLine(herX, herY, herX + (float) Math.cos(turnRay) * width * 1.2f, herY + (float) Math.sin(turnRay) * width * 1.2f, stroke);
            }
            stroke.setAlpha(255);
            if (ride.sinceDesperate() < 3) {
                float in = Math.min(1, ride.sinceDesperate() / .3f), gone = Math.max(0, (ride.sinceDesperate() - 2.4f) / .6f);
                word(canvas, "She throws everything at once!", cx, cy + high * 1.25f, Math.min(dp(17), safe.width() / 19f), 0xFFE0362F, in * (1 - gone));
            }
        }
        // One of her friends, taking a piece off the Mother's shield: her face, going up the sky to her.
        if (ride.sinceHelp() < .9f && ride.helps() > 0) {
            float gone = ride.sinceHelp() / .9f;
            canvas.saveLayerAlpha(0, 0, width, height, Math.round(255 * Math.min(1, (1 - gone) * 3)));
            round(canvas, RunStory.speaker(RunStory.PLEDGES[(ride.helps() - 1) % RunLevels.FRIENDS]), atX + (herX - atX) * gone + (ride.helps() % 2 == 0 ? 1 : -1) * tile * 1.4f * (1 - gone),
                    atY + (herY - atY) * gone, tile * (.5f - .2f * gone));
            canvas.restore();
        }
        if (cheerAge < 2.6f && ride.stages() > 0) {
            float in = Math.min(1, cheerAge / .35f), out = Math.max(0, (cheerAge - 2.1f) / .5f);
            word(canvas, ride.stage() == 1 ? "She is cracking: keep reading" : "The last of her: all together", cx, cy + high * 1.25f, Math.min(dp(16), safe.width() / 20f), 0xFFB9862C, in * (1 - out));
        }
        canvas.restore();
        if (flash > 0) canvas.drawColor(Color.argb(Math.round(255 * Math.min(1, flash / .5f)), 255, 255, 255));
        flightHud(canvas, true);
    }

    /** Over the ride: who she is, how much more of her there is to read, the pieces of her shield, and what is to be done about her just now. Returns where what is under it begins. */
    private float rideBar(Canvas canvas, float under) {
        int colour = 0xFFB9862C;
        float margin = dp(10), wide = Math.min(safe.width() - margin * 2, dp(300)), high = dp(48), from = safe.centerX() - wide / 2;
        box.set(from, under, from + wide, under + high);
        plate(canvas, box, dp(20), 235);
        float bar = from + dp(16), end = from + wide - dp(16);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(dark ? 0xFFF3F3F3 : colour);
        fitted(canvas, "The Mother Tongue", bar, under + dp(15), dp(12.5f), dp(110));
        text.setTextAlign(Paint.Align.RIGHT);
        text.setTextSize(dp(11));
        boolean urgent = ride.open() > 0;
        text.setColor(urgent ? 0xFFE0A21A : soft);
        fitted(canvas, ride.passing() ? "Out of reach: fly clear" : ride.finishing() ? "Beam and breath, together!" : urgent ? "Open: the beam reads her" : ride.forming() ? "Her shield forms"
                : "Lucy's breath breaks her shield", end, under + dp(15), dp(11), end - bar - dp(116));
        text.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
        canvas.drawRoundRect(bar, under + dp(21), end, under + dp(31), dp(5), dp(5), paint);
        paint.setColor(colour);
        paint.setAlpha(urgent ? 255 : 120);
        if (ride.left() > 0) canvas.drawRoundRect(bar, under + dp(21), bar + (end - bar) * ride.left(), under + dp(31), dp(5), dp(5), paint);
        paint.setAlpha(255);
        paint.setColor(cardColor);
        for (int third = 1; third < 3; third++) canvas.drawRect(bar + (end - bar) * third / 3 - dp(1), under + dp(21), bar + (end - bar) * third / 3 + dp(1), under + dp(31), paint);
        // The pieces of her shield, under the bar.
        for (int piece = 0; piece < ride.shieldMost(); piece++) {
            paint.setColor(piece < ride.shield() ? 0xFFE9C96A : dark ? 0xFF3A3F45 : 0xFFE4E6E8);
            canvas.drawRoundRect(bar + piece * dp(16), under + dp(35), bar + piece * dp(16) + dp(12), under + dp(40), dp(2), dp(2), paint);
        }
        paint.setColor(Color.BLACK);
        under += high + dp(8);
        if (flightNote != null && flightNoteAge < 3.2f) {
            word(canvas, flightNote, safe.centerX(), under + dp(18), Math.min(dp(15), safe.width() / 24f), 0xFFE0362F, Math.min(1, Math.min(flightNoteAge / .2f, (3.2f - flightNoteAge) / .5f)));
            under += dp(30);
        }
        return under;
    }

    /** What is held on the ride: the arrows; the beam; and, over the jump, Lucy's breath, with how far she has her wind back. */
    private void rideControls(Canvas canvas) {
        control(canvas, leftButton, goLeft, 180, false);
        control(canvas, rightButton, goRight, 0, false);
        control(canvas, jumpButton, goJump, 270, false);
        control(canvas, downButton, goDown, 90, false);
        RectF where = strikeButton;
        button(canvas, where, goStrike, CalculatorLayout.CORAL);
        stroke.setColor(0xFFE9C96A);
        stroke.setStrokeWidth(dp(3));
        canvas.drawCircle(where.centerX(), where.centerY(), where.width() / 2 - dp(2), stroke);
        box.set(where.left - dp(3), where.top - dp(3), where.right + dp(3), where.bottom + dp(3));
        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(dp(3.5f));
        canvas.drawArc(box, -90, 360 * (ride.beam() > 0 ? ride.beam() / RunRide.LASTS : ride.charge() > 0 ? ride.charge() : ride.rested()), false, stroke);
        paint.setAlpha(ride.rested() >= 1 || ride.beam() > 0 ? 255 : 110);
        artIn(canvas, STAR_ART, where.centerX(), where.centerY(), where.width() * (.44f + .1f * ride.charge()));
        paint.setAlpha(255);
        // Lucy's breath: her own face on it, and her wind coming back round it.
        where = stallButton;
        button(canvas, where, goBreath, 0xFF2FA89A);
        round(canvas, LUCY_FACE, where.centerX(), where.centerY() - dp(7), where.width() * .26f);
        box.set(where.left - dp(3), where.top - dp(3), where.right + dp(3), where.bottom + dp(3));
        stroke.setColor(0xFFBFF3EA);
        stroke.setStrokeWidth(dp(3.5f));
        canvas.drawArc(box, -90, 360 * ride.wind(), false, stroke);
        label(canvas, where, "Breath");
        if (ride.puffs() == 0 && finaleAge > 1.5f) glow(canvas, stallButton);
    }

    /** The black ribbon lay on her for good: the land is unmade, and what follows is watched, not played. */
    private void unmake() {
        finale = UNMADE;
        finaleAge = 0;
        unmadeSaid = 0;
        unmadeAt = 0;
        idle();
        talk.clear();
        captionLeft = 0;
        audio.play("lose", .6f);
    }

    /** The end of the last land goes on by a length of time: the flight, with what is held; the beat after it is won; or the land unmade, line by line. */
    private void finaleTick(float seconds) {
        finaleAge += seconds;
        cheerAge += seconds;
        flightNoteAge += seconds;
        talks();
        for (int index = flightWords.size() - 1; index >= 0; index--) if ((flightWords.get(index)[2] += seconds) > 1.5f) flightWords.remove(index);
        if (finale == FLIGHT) {
            // (In a build made for testing it can be flown by itself, to be looked at: to where Lucy is, with the beam held.)
            if (BuildConfig.DEBUG && session.tally("run_sweep") == 1) {
                flight.step(seconds, flight.lucyX() < flight.x() - .06f, flight.lucyX() > flight.x() + .06f, flight.lucyY() < flight.y() - .06f, flight.lucyY() > flight.y() + .06f, true);
            } else {
                flight.step(seconds, goLeft, goRight, goJump, goDown, goStrike);
            }
            if (flight.beams() != seenFlightBeams) {
                seenFlightBeams = flight.beams();
                audio.play("form", .7f);
                buzz(false);
            }
            if (flight.thrown() != seenFlightThrown) {
                seenFlightThrown = flight.thrown();
                audio.play("throw", .3f);
            }
            if (flight.hits() != seenFlightHits) {
                // The beam says what she means: every few times it lands, a word of it is read off her.
                if (flight.hits() % 3 == 1) {
                    audio.play("page", .45f);
                    flightWords.add(new float[]{random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, 0, flight.hits() / 3 % READ.length});
                }
                seenFlightHits = flight.hits();
            }
            if (flight.hurts() != seenFlightHurts) {
                seenFlightHurts = flight.hurts();
                audio.play("hurt");
                buzz(true);
                shake = .25f;
            }
            if (flight.stages() != seenFlightStages) {
                // A third of it: she reels, her friends are heard from, and she has something to say.
                seenFlightStages = flight.stages();
                flash = .5f;
                shake = .3f;
                cheerAge = 0;
                audio.play("star", .8f);
                buzz(true);
                talk.add(RunStory.LUCY[flight.stage() == 1 ? 7 : 8]);
            }
            if (flight.state() == RunFlight.WON) {
                finale = FLOWN;
                finaleAge = 0;
                flash = 1;
                idle();
                talk.clear();
                audio.play("star", 1);
                buzz(true);
            } else if (flight.state() == RunFlight.FELL) {
                // Out of hearts up there: a life, if she has one, and the flight from its beginning; if not, the land is lost.
                audio.play("lose", .5f);
                if (run.spend()) {
                    flightNote = run.lives() == 1 ? "Last life: the flight begins again" : "A life lost, " + run.lives() + " left: the flight begins again";
                    flightNoteAge = 0;
                    flight = new RunFlight(run.maxHearts() * 2, Math.max(1, 2 + run.bite() - perks.defence), random.nextLong());
                    seenFlightHits = seenFlightHurts = seenFlightBeams = seenFlightStages = seenFlightThrown = 0;
                    flightWords.clear();
                    flash = .4f;
                } else {
                    finish(false);
                }
            }
        } else if (finale == FLOWN) {
            if (finaleAge > 4.4f && fourth()) {
                // What the book says is read, as it was written, as far as where Halgakos names the great evil: that is watched,
                // if it can be; and then the rest is told, and she comes.
                if (RunStory.REVEAL.length > 0) tell(java.util.Arrays.copyOfRange(RunStory.REVEAL, 0, RunStory.CUT_AT), RunLevels.chapterOf(number), this::watch);
                else rise();
            } else if (finaleAge > 4.4f) {
                run.flown();
                finish(true);
            }
        } else if (finale == WATCHING) {
            // (The clip has its own time.)
            finaleAge = 0;
        } else if (finale == ARISING) {
            if (finaleAge > 4) {
                finale = RIDE;
                finaleAge = 0;
                mount();
            }
        } else if (finale == RIDE) {
            rideTick(seconds);
        } else if (finale == RIDDEN) {
            if (finaleAge > 6) {
                // The true ending: reached, and kept; and what is said at it, before its card.
                session.keep("run_ending", session.tally("run_ending") | 2);
                tell(RunStory.READ, RunLevels.chapterOf(number), () -> {
                    screen = PLAY;
                    run.flown();
                    finish(true);
                });
            }
        } else if (unmadeSaid == 0 && finaleAge > 5.6f) {
            unmadeSaid = 1;
            unmadeAt = finaleAge;
            speak(RunStory.LUCY[9], true);
        } else if (unmadeSaid == 1 && finaleAge > unmadeAt + 4.2f && !audio.speaking()) {
            unmadeSaid = 2;
            unmadeAt = finaleAge;
            lucyRoseAt = finaleAge;
            speak(RunStory.LUCY[10], true);
        } else if (unmadeSaid == 2 && finaleAge > unmadeAt + 4.6f && !audio.speaking()) {
            unmadeSaid = 3;
            unmadeAt = finaleAge;
            speak(RunStory.LUCY[11], true);
        } else if (unmadeSaid == 3 && finaleAge > unmadeAt + 4.6f && !audio.speaking()) {
            // It has been seen: that is kept; what she collected is hers; and the card says what was missing.
            session.keep("run_ending", session.tally("run_ending") | 1);
            news();
            won = false;
            idle();
            resultClock = 0;
            starsShown = 0;
            stars = 0;
            screen = RESULT;
            captionLeft = 0;
            banked = run.purse() * (100 + RunGear.luck(gearOn()) + RunMarket.PAYS * hard()) / 100;
            session.keep("run_bank", bank() + banked);
            pills("Lands", TO_MAP, "Try again", AGAIN);
        }
    }

    /**
     * The moment of the black ribbon, over the land: the place darkens as Lucy raises it; it crosses to Halgakos; and, turned, it
     * goes white on her, with every friend who pledged about her, and the land ends in light. Not turned, the dark stays.
     */
    private void drawMoment(Canvas canvas) {
        PlusRun.Foe lucy = null;
        for (PlusRun.Foe foe : run.foes()) if (foe.kind == RunLevels.LUCY) lucy = foe;
        if (lucy == null) return;
        float since = PlusRun.MOMENT - run.moment(), lands = PlusRun.MOMENT - PlusRun.MOMENT_LANDS, after = since - lands;
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() + PlusRun.TALL * .45f - cameraY) * tile;
        float fromX = (lucy.x + lucy.wide() / 2 - cameraX) * tile, fromY = (lucy.y - .55f - cameraY) * tile;
        boolean whole = run.whole();
        float dark = !run.ribboned() ? Math.min(1, since / .8f) : whole ? Math.max(0, 1 - after / .5f) : 1;
        if (dark > 0) canvas.drawColor(Color.argb(Math.round(135 * dark), 10, 6, 22));
        paint.setStyle(Paint.Style.FILL);
        if (!run.ribboned()) {
            float grown = Math.min(1, since), go = Math.max(0, (since - 1) / (lands - 1)), ease = go * go * (3 - 2 * go);
            float x = fromX + (herX - fromX) * ease, y = fromY + (herY - fromY) * ease - (float) Math.sin(ease * Math.PI) * tile * 1.5f;
            paint.setColor(0xFF140A1E);
            paint.setAlpha(Math.round(150 * grown));
            canvas.drawCircle(x, y, tile * (.45f + .45f * grown + .06f * (float) Math.sin(clock * 14)), paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            pieceIn(canvas, two, RIBBON_ART + PlusRun.BLACK - 1, x, y, tile * (.5f + .7f * grown), clock * 220);
        } else if (whole) {
            // Rings of light going out from her; and her friends, every one of them, coming round her.
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(tile * .14f);
            for (int ring = 0; ring < 3; ring++) {
                float out = (after * .9f - ring * .25f) % 1.2f;
                if (out <= 0 || out >= 1) continue;
                stroke.setAlpha(Math.round(230 * (1 - out)));
                canvas.drawCircle(herX, herY, tile * (.6f + 4.5f * out), stroke);
            }
            stroke.setAlpha(255);
            float come = Math.min(1, after / 1.1f), radius = tile * (4.6f - 2.3f * come * come * (3 - 2 * come));
            for (int friend = 0; friend < RunLevels.FRIENDS; friend++) {
                double turn = clock * .7 + friend * Math.PI * 2 / RunLevels.FRIENDS;
                canvas.saveLayerAlpha(0, 0, width, height, Math.round(255 * come));
                round(canvas, RunStory.speaker(RunStory.PLEDGES[friend]), herX + (float) Math.cos(turn) * radius, herY + (float) Math.sin(turn) * radius * .78f, tile * .34f);
                canvas.restore();
            }
            // And the land ends in light.
            if (run.moment() < 1.1f) canvas.drawColor(Color.argb(Math.round(255 * (1 - run.moment() / 1.1f)), 255, 255, 255));
        } else if (run.moment() < 1) {
            canvas.drawColor(Color.argb(Math.round(120 * (1 - run.moment())), 10, 6, 22));
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
    }

    /**
     * The flight: the sky over the library, pages streaming out of the distance; Lucy, a dragon, far ahead; what she sends, growing
     * as it comes, each thing with a mark where it will arrive; Halgakos from behind, on both wings, with the beam going away from
     * her into the distance when it is out; and over it her hearts, how much more Lucy can take, and what is said.
     */
    private void drawFlight(Canvas canvas) {
        drawSky(canvas, RunLevels.chapterOf(RunLevels.TOTAL), clock * tile * 2.4f);
        canvas.drawColor(0x66121A3A);
        // (Where she flies is the lower middle of the screen; where Lucy does, far off, is above it and half the size: so the beam, which
        // goes away from her, always has the height of the sky between them to cross.)
        float cx = safe.centerX(), cy = safe.top + safe.height() * .6f, wide = safe.width() * .4f, high = safe.height() * .17f;
        float farY = safe.top + safe.height() * .37f, far = .5f;
        canvas.save();
        if (shake > 0) canvas.translate((random.nextFloat() - .5f) * tile * .28f * Math.min(1, shake * 4), (random.nextFloat() - .5f) * tile * .28f * Math.min(1, shake * 4));
        paint.setStyle(Paint.Style.FILL);
        for (int page = 0; page < 22; page++) {
            float along = (clock * (.35f + page % 5 * .06f) + page * .137f) % 1, turn = page * 2.399f, reach = along * along * Math.max(width, height) * .75f;
            float x = cx + (float) Math.cos(turn) * reach, y = farY + (float) Math.sin(turn) * reach, half = tile * .18f * (.3f + along);
            canvas.save();
            canvas.rotate(page * 47 + clock * 90, x, y);
            paint.setColor(page % 4 == 0 ? 0xFFFFF3C4 : Color.WHITE);
            paint.setAlpha(Math.round(150 * along));
            canvas.drawRect(x - half, y - half * 1.35f, x + half, y + half * 1.35f, paint);
            canvas.restore();
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        if (finale == FLOWN) {
            // Won: the dragon is a child again, and comes down to where Halgakos waits for her with the book shut.
            float age = finaleAge, shrink = Math.max(0, 1 - age / 1.2f), in = Math.min(1, Math.max(0, (age - .9f) / .8f)), bob = (float) Math.sin(clock * 2) * tile * .08f;
            if (shrink > 0) {
                paint.setAlpha(Math.round(255 * shrink));
                cell(canvas, lucy(2), 3, 4, cx, farY + tile * 1.5f, tile * 3.1f * (.3f + .7f * shrink), false);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(70 * in));
            canvas.drawCircle(cx, cy + tile * .3f, tile * 2.6f, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(Math.round(255 * in));
            frame(canvas, lucy(0), 3, 4, cx + tile * .95f, cy + tile * 1.15f + bob - (1 - in) * tile * 2.5f, tile * 2, false);
            paint.setAlpha(255);
            drawnAs = PlusRun.WHITE;
            frame(canvas, herWhite(false), 11, 12, cx - tile * .95f, cy + tile * 1.15f - bob, tile * 2, false);
            drawnAs = NO_LOOK;
            if (in > 0) word(canvas, "Read, as written", cx, cy + tile * 2.5f, Math.min(dp(24), safe.width() / 13f), 0xFFB9862C, in);
            canvas.restore();
            if (flash > 0) canvas.drawColor(Color.argb(Math.round(255 * Math.min(1, flash / .5f)), 255, 255, 255));
            flightHud(canvas, false);
            return;
        }
        // Lucy, far ahead: her wings going; roaring as she sends something; and struck while the beam is on her, and as a third of it ends.
        float lucyX = cx + flight.lucyX() * wide * far, lucyY = farY + flight.lucyY() * high * far, lucyTall = tile * 3.1f;
        boolean reels = flight.reel() > 0 && flight.stages() > 0;
        int lucyPose = flight.sinceStruck() < .22f || reels ? 3 : flight.sinceThrow() < .4f ? 2 : (int) (clock * 4.5f) % 2;
        if (lucyPose == 3) lucyX += (float) Math.sin(clock * 40) * tile * .06f;
        cell(canvas, lucy(2), lucyPose, 4, lucyX, lucyY + lucyTall / 2, lucyTall, false);
        // What is on its way.
        for (RunFlight.Thing thing : flight.things()) {
            if (thing.gone) continue;
            float near = 1 - Math.max(0, thing.far), scale = far + (1 - far) * near;
            float nearX = cx + thing.x * wide, nearY = cy + thing.y * high;
            float x = cx + thing.x * wide * scale, y = farY + (cy - farY) * near + thing.y * high * scale;
            int warn = Math.round((60 + 150 * near) * (.75f + .25f * (float) Math.sin(clock * 16)));
            paint.setStyle(Paint.Style.FILL);
            if (thing.kind == RunFlight.GUST || thing.kind == RunFlight.PILLAR) {
                boolean level = thing.kind == RunFlight.GUST;
                float band = (level ? high : wide) * RunFlight.BAR, half = tile * (.08f + .32f * near);
                paint.setColor(0xFFE0362F);
                paint.setAlpha(warn / 2);
                if (level) canvas.drawRect(0, nearY - band, width, nearY + band, paint);
                else canvas.drawRect(nearX - band, farY - high * .5f, nearX + band, cy + high * 1.1f, paint);
                paint.setColor(0xFFBFF3EA);
                paint.setAlpha(Math.round(80 + 140 * near));
                if (level) canvas.drawRoundRect(cx - safe.width() * (.22f + .4f * near), y - half, cx + safe.width() * (.22f + .4f * near), y + half, half, half, paint);
                else canvas.drawRoundRect(x - half, farY - high * (.5f + .7f * near), x + half, farY + high * (.6f + .8f * near), half, half, paint);
            } else {
                stroke.setColor(0xFFE0362F);
                stroke.setAlpha(warn);
                stroke.setStrokeWidth(tile * .06f);
                float grown = 1.5f - .5f * near;
                canvas.drawOval(nearX - wide * RunFlight.NEAR * grown, nearY - high * RunFlight.NEAR * grown, nearX + wide * RunFlight.NEAR * grown, nearY + high * RunFlight.NEAR * grown,
                        stroke);
                stroke.setAlpha(255);
                float size = tile * (.3f + 1.1f * near);
                if (thing.kind == RunFlight.PUFF) {
                    // A puff of her mist, rolling.
                    for (int puff = 0; puff < 4; puff++) {
                        double turn = clock * 5 + puff * 1.57;
                        paint.setColor(puff == 0 ? 0xFFBFF3EA : 0xFF8FE0D2);
                        paint.setAlpha(170 + 20 * puff);
                        canvas.drawCircle(x + (float) Math.cos(turn) * size * .22f, y + (float) Math.sin(turn) * size * .18f, size * (.42f - puff * .05f), paint);
                    }
                } else {
                    paint.setAlpha(255);
                    canvas.save();
                    canvas.rotate(clock * 200, x, y);
                    artIn(canvas, STAR_ART, x, y, size);
                    canvas.restore();
                }
            }
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        // Where she is, and where the beam would reach, far off: a ring there, gold while Lucy is before it.
        float herX = cx + flight.x() * wide, herY = cy + flight.y() * high, herTall = tile * 2, aimX = cx + flight.x() * wide * far, aimY = farY + flight.y() * high * far;
        boolean before = Math.abs(flight.x() - flight.lucyX()) < RunFlight.TRUE && Math.abs(flight.y() - flight.lucyY()) < RunFlight.TRUE;
        stroke.setColor(before ? 0xFFFFD24A : Color.WHITE);
        stroke.setAlpha(before ? 255 : 150);
        stroke.setStrokeWidth(tile * (before ? .08f : .05f));
        canvas.drawCircle(aimX, aimY, tile * (before ? .5f + .06f * (float) Math.sin(clock * 14) : .4f), stroke);
        stroke.setAlpha(255);
        if (flight.beam() > 0) {
            float life = flight.beam() / RunFlight.LASTS, swell = (float) Math.sin(Math.min(1, (1 - life) * 6) * Math.PI / 2) * Math.min(1, life * 5);
            paint.setStyle(Paint.Style.FILL);
            for (int layer = 0; layer < 3; layer++) {
                float nearHalf = tile * .6f * swell * (1.25f - layer * .42f) * (1 + .08f * (float) Math.sin(clock * 40 + layer)), farHalf = nearHalf * .34f;
                path.reset();
                path.moveTo(herX - nearHalf, herY - herTall * .2f);
                path.lineTo(herX + nearHalf, herY - herTall * .2f);
                path.lineTo(aimX + farHalf, aimY);
                path.lineTo(aimX - farHalf, aimY);
                path.close();
                paint.setColor(layer == 0 ? 0xFFFFE9A8 : layer == 1 ? 0xFFFFF7DE : Color.WHITE);
                paint.setAlpha(layer == 0 ? 110 : layer == 1 ? 190 : 255);
                canvas.drawPath(path, paint);
                canvas.drawCircle(aimX, aimY, farHalf, paint);
            }
            // Letters stream along it, away from her.
            String[] letters = {"A", "가", "あ", "文", "Z", "한", "ん", "字"};
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(0xFFB9862C);
            for (int index = 0; index < 8; index++) {
                float along = (clock * 2.2f + index * .125f) % 1;
                text.setTextSize(tile * (.5f - .3f * along));
                text.setAlpha(Math.round(230 * swell));
                canvas.drawText(letters[index], herX + (aimX - herX) * along + (float) Math.sin(along * 20 + index) * tile * .2f * (1 - along),
                        herY - herTall * .2f + (aimY - herY + herTall * .2f) * along, text);
            }
            text.setAlpha(255);
            if (flight.onHer()) {
                paint.setColor(Color.WHITE);
                paint.setAlpha(Math.round(120 + 80 * (float) Math.sin(clock * 30)));
                canvas.drawCircle(lucyX, lucyY, tile * 1.1f, paint);
            }
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
        // What the beam has read off her: her own words, as they were meant, rising from her.
        text.setTextAlign(Paint.Align.CENTER);
        for (float[] read : flightWords) {
            float age = read[2], solid = Math.min(1, Math.min(age / .15f, (1.5f - age) / .5f));
            word(canvas, READ[(int) read[3]], lucyX + read[0] * tile * 1.3f, lucyY + read[1] * tile * .7f - age * tile * .9f, tile * .38f, 0xFFB9862C, solid);
        }
        // Halgakos, from behind: a light about her, her wings going, the book up as she gathers, and out before her while the beam is out.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        float beat = .5f + .5f * (float) Math.sin(clock * 4), gathers = flight.charge();
        for (int ring = 3; ring >= 1; ring--) {
            paint.setAlpha(Math.round((22 + 12 * beat) * (4 - ring)));
            canvas.drawCircle(herX, herY, herTall * (.34f + ring * .15f * (1 - .5f * gathers) + .03f * beat), paint);
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(flight.safe() && (int) (clock * 14) % 2 == 0 ? 90 : 255);
        drawnAs = PlusRun.WHITE;
        frame(canvas, herWhite(true), flight.charge() > 0 ? 4 : flight.beam() > 0 ? 5 : (int) (clock * 10) % 4, 6, herX, herY + herTall * .5f, herTall, false);
        drawnAs = NO_LOOK;
        paint.setAlpha(255);
        // Her friends are heard from as a third of it ends: some of them, going by.
        if (cheerAge < 2.6f && flight.stages() > 0) {
            int first = flight.stage() == 1 ? 0 : 9, count = flight.stage() == 1 ? 9 : 10;
            float in = Math.min(1, cheerAge / .35f), out = Math.max(0, (cheerAge - 2.1f) / .5f), row = cy + high * 1.12f;
            canvas.saveLayerAlpha(0, row - dp(40), width, row + dp(46), Math.round(255 * in * (1 - out)));
            for (int index = 0; index < count; index++) {
                float x = safe.left + safe.width() * (index + .5f) / count - (1 - in) * safe.width() * .5f;
                round(canvas, RunStory.speaker(RunStory.PLEDGES[first + index]), x, row + (float) Math.sin(clock * 6 + index) * dp(3), Math.min(dp(19), safe.width() / count * .42f));
            }
            word(canvas, flight.stage() == 1 ? "Everyone is reading with her" : "Every friend, to the last page", cx, row + dp(40), Math.min(dp(15), safe.width() / 22f), 0xFFB9862C, 1);
            canvas.restore();
        }
        canvas.restore();
        if (flash > 0) canvas.drawColor(Color.argb(Math.round(255 * Math.min(1, flash / .5f)), 255, 255, 255));
        flightHud(canvas, true);
    }

    /** Over the flight: her hearts and her lives; how much more Lucy can take, of which two thirds went on the ground; a word when a life is lost; and what is said. */
    private void flightHud(Canvas canvas, boolean flying) {
        int most = ride != null ? ride.most() : flight != null ? flight.most() : run.maxHearts() * 2;
        int hearts = (most + 1) / 2, halves = ride != null ? ride.halves() : flight != null ? flight.halves() : most;
        float tall = dp(44), margin = dp(10), heart = hearts > 5 ? dp(15) : hearts > 3 ? dp(18) : dp(22);
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        String lives = "× " + run.lives();
        box.set(safe.left + margin, safe.top + margin, safe.left + margin + dp(20) + heart * hearts + dp(34) + text.measureText(lives) + dp(14), safe.top + margin + tall);
        float plateLeft = box.left, plateMiddle = box.centerY();
        plate(canvas, box, tall / 2, 235);
        for (int index = 0; index < hearts; index++) {
            float middle = plateLeft + dp(14) + heart * (index + .5f), bottom = plateMiddle + heart * .42f;
            int has = Math.max(0, Math.min(2, halves - index * 2));
            float size = heart * .92f * (halves <= 2 && has > 0 ? 1 + .1f * (float) Math.sin(clock * 9) : 1);
            if (has < 2) {
                paint.setAlpha(60);
                art(canvas, HEART_ART, middle, bottom, size, false);
            }
            if (has > 0) {
                paint.setAlpha(255);
                canvas.save();
                if (has == 1) canvas.clipRect(middle - heart, bottom - heart * 1.2f, middle, bottom + heart * .2f);
                art(canvas, HEART_ART, middle, bottom, size, false);
                canvas.restore();
            }
        }
        paint.setAlpha(255);
        float livesAt = plateLeft + dp(20) + heart * hearts + dp(8);
        drawnAs = PlusRun.WHITE;
        round(canvas, 0, livesAt + dp(11), plateMiddle, dp(11));
        drawnAs = NO_LOOK;
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(run.lives() <= 1 ? 0xFFE0362F : ink);
        canvas.drawText(lives, livesAt + dp(26), plateMiddle + dp(5.5f), text);
        plate(canvas, pauseButton, tall / 2, 235);
        paint.setColor(ink);
        canvas.drawRoundRect(pauseButton.centerX() - dp(7), pauseButton.centerY() - dp(8), pauseButton.centerX() - dp(2.5f), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        canvas.drawRoundRect(pauseButton.centerX() + dp(2.5f), pauseButton.centerY() - dp(8), pauseButton.centerX() + dp(7), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        paint.setColor(Color.BLACK);
        float under = safe.top + margin + tall + dp(8);
        if (flying && ride != null) {
            under = rideBar(canvas, under);
        } else if (flying) {
            int colour = waitColour(RunLevels.LUCY);
            float wide = Math.min(safe.width() - margin * 2, dp(300)), high = dp(48), from = safe.centerX() - wide / 2;
            box.set(from, under, from + wide, under + high);
            plate(canvas, box, dp(20), 235);
            float bar = from + dp(46), end = from + wide - dp(16);
            round(canvas, LUCY_FACE, from + dp(22), under + high / 2, dp(15));
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(dark ? 0xFFF3F3F3 : colour);
            fitted(canvas, "Lucy, Dragon of Lost Words", bar, under + dp(15), dp(12.5f), end - bar - dp(92));
            text.setTextAlign(Paint.Align.RIGHT);
            text.setTextSize(dp(11));
            text.setColor(flight.onHer() ? 0xFFE0A21A : soft);
            canvas.drawText(flight.onHer() ? "Reading her!" : "Only the beam", end, under + dp(15), text);
            text.setTextAlign(Paint.Align.LEFT);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
            canvas.drawRoundRect(bar, under + dp(21), end, under + dp(31), dp(5), dp(5), paint);
            paint.setColor(colour);
            float left = flight.left() / 3;
            if (left > 0) canvas.drawRoundRect(bar, under + dp(21), bar + (end - bar) * left, under + dp(31), dp(5), dp(5), paint);
            paint.setColor(cardColor);
            for (int third = 1; third < 3; third++) canvas.drawRect(bar + (end - bar) * third / 3 - dp(1), under + dp(21), bar + (end - bar) * third / 3 + dp(1), under + dp(31), paint);
            paint.setColor(Color.BLACK);
            under += high + dp(8);
            if (flightNote != null && flightNoteAge < 3.2f) {
                float solid = Math.min(1, Math.min(flightNoteAge / .2f, (3.2f - flightNoteAge) / .5f));
                word(canvas, flightNote, safe.centerX(), under + dp(18), Math.min(dp(15), safe.width() / 24f), 0xFFE0362F, solid);
                under += dp(30);
            }
        }
        finaleCaption(canvas, under);
    }

    /** What has just been said, at the end of the last land: who says it, round, her name in her colour, and her words. */
    private void finaleCaption(Canvas canvas, float under) {
        if (captionLeft <= 0 || caption == null) return;
        float fade = Math.min(1, captionLeft / .3f), face = dp(17), margin = dp(10);
        text.setTextSize(dp(14));
        List<String> lines = wrap(caption, Math.min(safe.width() - dp(48) - face * 2 - dp(8), dp(300)));
        if (lines.size() > 3) lines = lines.subList(0, 3);
        float wide = text.measureText(RunStory.NAMES[captionWho]);
        for (String one : lines) wide = Math.max(wide, text.measureText(one));
        float high = Math.max(lines.size() * dp(19) + dp(30), face * 2 + dp(14));
        box.set(safe.left + margin, under, safe.left + margin + wide + dp(30) + face * 2, under + high);
        canvas.saveLayerAlpha(box.left - dp(4), box.top - dp(4), box.right + dp(4), box.bottom + dp(6), Math.round(255 * fade));
        plate(canvas, box, dp(16), 240);
        if (captionWho == 0) drawnAs = finale == UNMADE ? 0 : PlusRun.WHITE;
        round(canvas, captionWho, safe.left + margin + dp(8) + face, under + dp(7) + face, face);
        drawnAs = NO_LOOK;
        float words = safe.left + margin + dp(16) + face * 2;
        text.setTextAlign(Paint.Align.LEFT);
        text.setTextSize(dp(12));
        text.setColor(SPEAKER_COLOURS[captionWho]);
        canvas.drawText(RunStory.NAMES[captionWho], words, under + dp(17), text);
        text.setTextSize(dp(14));
        text.setColor(ink);
        for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), words, under + dp(22) + dp(19) * (index + .74f), text);
        canvas.restore();
    }

    /** What is held in the flight: the arrows, all four of them; and the beam, with how far it is gathered, or how far through its rest it is. */
    private void flightControls(Canvas canvas) {
        if (finale == RIDE) {
            rideControls(canvas);
            return;
        }
        if (finale != FLIGHT) return;
        control(canvas, leftButton, goLeft, 180, false);
        control(canvas, rightButton, goRight, 0, false);
        control(canvas, jumpButton, goJump, 270, false);
        control(canvas, downButton, goDown, 90, false);
        RectF where = strikeButton;
        button(canvas, where, goStrike, CalculatorLayout.CORAL);
        stroke.setColor(0xFFE9C96A);
        stroke.setStrokeWidth(dp(3));
        canvas.drawCircle(where.centerX(), where.centerY(), where.width() / 2 - dp(2), stroke);
        float ready = flight.rested(), charge = flight.charge();
        box.set(where.left - dp(3), where.top - dp(3), where.right + dp(3), where.bottom + dp(3));
        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(dp(3.5f));
        canvas.drawArc(box, -90, 360 * (flight.beam() > 0 ? flight.beam() / RunFlight.LASTS : charge > 0 ? charge : ready), false, stroke);
        paint.setAlpha(ready >= 1 || flight.beam() > 0 ? 255 : 110);
        artIn(canvas, STAR_ART, where.centerX(), where.centerY(), where.width() * (.44f + .1f * charge));
        paint.setAlpha(255);
        // The first time up, the buttons that matter glow until the beam has been let go once.
        if (flight.beams() == 0 && finaleAge > 1) glow(canvas, strikeButton);
    }

    /**
     * The land unmade: its colour drains; the ribbons of every colour she had leave her and scatter; its pages blow across; and it is
     * white. In the white, Lucy, alone, who then flies off; and at the last Halgakos, where she fell.
     */
    private void drawUnmade(Canvas canvas) {
        float age = finaleAge;
        android.graphics.ColorMatrix grey = new android.graphics.ColorMatrix();
        grey.setSaturation(Math.max(0, 1 - age / 2.6f));
        drained.setColorFilter(new android.graphics.ColorMatrixColorFilter(grey));
        float white = Math.max(0, Math.min(1, (age - 3) / 2.2f));
        if (white < 1) {
            canvas.saveLayer(0, 0, width, height, drained);
            drawGround(canvas);
            canvas.restore();
            float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() + PlusRun.TALL * .45f - cameraY) * tile;
            for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) {
                float gone = (age - .16f * colour) / 1.9f;
                if (gone <= 0 || gone >= 1) continue;
                double way = -Math.PI / 2 + (colour - 4) * .4;
                paint.setAlpha(Math.round(255 * (1 - gone)));
                pieceIn(canvas, two, RIBBON_ART + colour - 1, herX + (float) Math.cos(way) * tile * 5.5f * gone, herY + (float) Math.sin(way) * tile * 5.5f * gone, tile * .7f, gone * 420);
            }
            paint.setStyle(Paint.Style.FILL);
            for (int page = 0; page < 30 && age > 1.6f; page++) {
                float along = ((age - 1.6f) * (.3f + page % 6 * .05f) + page * .071f) % 1, x = -tile + along * (width + tile * 2);
                float y = height * (page * .618f % 1) + (float) Math.sin(along * 9 + page) * tile * .8f, half = tile * (.2f + page % 3 * .08f);
                canvas.save();
                canvas.rotate(page * 53 + age * 140, x, y);
                paint.setColor(page % 5 == 0 ? 0xFFF4EBD2 : Color.WHITE);
                paint.setAlpha(Math.round(230 * Math.min(1, (age - 1.6f) / .6f)));
                canvas.drawRect(x - half, y - half * 1.35f, x + half, y + half * 1.35f, paint);
                canvas.restore();
            }
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
        if (white > 0) canvas.drawColor(Color.argb(Math.round(255 * white), 250, 248, 244));
        if (age > 5.2f) {
            // Lucy, alone in it: sitting, at first; on her feet to say who she is; and then away, on her own wings.
            float in = Math.min(1, (age - 5.2f) / .8f), away = unmadeSaid >= 2 ? Math.max(0, age - lucyRoseAt - 3.4f) : 0;
            float x = safe.centerX() + away * tile * 1.8f, y = safe.top + safe.height() * .52f - away * away * tile * 2.8f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF8A8F99);
            paint.setAlpha(Math.round(60 * in * Math.max(0, 1 - away)));
            canvas.drawOval(safe.centerX() - tile * 1.1f, safe.top + safe.height() * .52f - tile * .12f, safe.centerX() + tile * 1.1f, safe.top + safe.height() * .52f + tile * .14f, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(Math.round(255 * in * Math.max(0, 1 - away / 1.8f)));
            if (away > 0) frame(canvas, lucy(1), 0, 4, x, y, tile * 3.4f, true);
            else frame(canvas, lucy(0), unmadeSaid >= 2 ? 0 : 3, 4, x, y, tile * 3.4f, false);
            paint.setAlpha(255);
        }
        if (unmadeSaid >= 3) {
            // And Halgakos, in her own colour, where she fell.
            paint.setAlpha(Math.round(255 * Math.min(1, (age - unmadeAt) / .8f)));
            frame(canvas, lose, (int) ((age - unmadeAt) / .7f) % 2 == 0 ? SULK : DAZED, LOSE_FRAMES, safe.centerX(), safe.top + safe.height() * .76f, tile * 2.8f, false);
            paint.setAlpha(255);
        }
        plate(canvas, pauseButton, dp(22), 235);
        paint.setColor(ink);
        canvas.drawRoundRect(pauseButton.centerX() - dp(7), pauseButton.centerY() - dp(8), pauseButton.centerX() - dp(2.5f), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        canvas.drawRoundRect(pauseButton.centerX() + dp(2.5f), pauseButton.centerY() - dp(8), pauseButton.centerX() + dp(7), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        paint.setColor(Color.BLACK);
        finaleCaption(canvas, safe.top + dp(64));
    }

    /** How far down the view sits when the whole height of the land fits on the screen: its ground on the bottom. */
    private float restingCamera() { return RunLevels.ROWS - height / tile; }

    // Time

    void advance(float seconds) {
        clock += seconds;
        sayNews(seconds);
        if (captionLeft > 0) captionLeft -= seconds;
        if (refused > 0) refused -= seconds;
        if (knockAge >= 0 && (knockAge += seconds) > KNOCK) knockAge = -1;
        if (shake > 0) shake -= seconds;
        woodSeconds += seconds;
        if (flash > 0) flash -= seconds;
        if (personLeft > 0) personLeft -= seconds;
        // A card that says something was won is read while the land is on the screen: it does not run out behind a pause.
        if (toastLeft > 0 && screen == PLAY) toastLeft -= seconds;
        wardFlash += seconds;
        gripChanged += seconds;
        clangAge += seconds;
        healAge += seconds;
        shatterAge += seconds;
        // The keys, said once the story told as the first land begins is over (said over it, it would not be heard).
        if (keysHintDue && screen == PLAY && !(talking && !told) && captionLeft <= 0) {
            keysHintDue = false;
            session.keep("run_keys_said", 1);
            speak(RunStory.KEYS[0], true);
        }
        // The first time the map is open with nothing else to say: her line with the game's name in it.
        if (screen == MAP && video == null && captionLeft <= 0 && personLeft <= 0 && session.tally("run_title_said") == 0 && clock > 1.5f) {
            session.keep("run_title_said", 1);
            speak(RunStory.TITLE_LINE[0], false);
            titleThisVisit = true;
        }
        // And, on a later visit, the game's own name: the Queen of Sloppies, once.
        if (screen == MAP && video == null && !treeOpen && captionLeft <= 0 && personLeft <= 0 && !titleThisVisit && session.tally("run_title_said") != 0
                && session.tally("run_title2_said") == 0 && clock > 2.5f && !audio.speaking()) {
            session.keep("run_title2_said", 1);
            speak(RunStory.TITLE_LINE[1], false);
        }
        // The tree redrawn and every point back: she says so, once, the next time the map is open.
        if (screen == MAP && video == null && !treeOpen && captionLeft <= 0 && personLeft <= 0 && session.tally("run_tree_notice") == 1 && session.tally("run_tree_said") == 0
                && clock > 4 && !audio.speaking()) {
            session.keep("run_tree_said", 1);
            speak(RunStory.SKILLS[3], false);
        }
        // The first time the map is open with points waiting in her skill tree: she says so.
        if (screen == MAP && video == null && !treeOpen && captionLeft <= 0 && personLeft <= 0 && session.tally("run_title_said") != 0 && session.tally("run_skills_said") == 0
                && clock > 4 && !audio.speaking() && skillsLeft() > 0) {
            session.keep("run_skills_said", 1);
            speak(RunStory.SKILLS[0], false);
        }
        // Paper cut from the trees: kept at once, whatever comes of the land; and a tree down counts for a hunt that asks for them.
        if (run != null) {
            if (run != paperRun) {
                paperRun = run;
                java.util.Arrays.fill(seenPaper, 0);
                seenTrees = 0;
            }
            for (int grade = 0; grade < RunCraft.GRADES; grade++) {
                int now = run.paperCut(grade);
                if (now > seenPaper[grade]) {
                    session.keep("run_paper_" + grade, session.tally("run_paper_" + grade) + now - seenPaper[grade]);
                    toast(RunCraft.PAPER_NAMES[grade] + " + " + (now - seenPaper[grade]), "For the smithy: " + session.tally("run_paper_" + grade) + " in all", 0);
                    audio.play("pick", .8f);
                    seenPaper[grade] = now;
                }
            }
            if (run.treesCut() > seenTrees) {
                for (int felled = seenTrees; felled < run.treesCut(); felled++) {
                    session.keep("run_trees_cut", session.tally("run_trees_cut") + 1);
                    for (int slot = 0; slot < RunHunts.SLOTS; slot++) if (RunHunts.countsTree(hunt(slot))) hunted(slot);
                }
                audio.play("crumble", .7f);
                seenTrees = run.treesCut();
            }
        }
        // A hideout: found, it is marked on the map; near one not yet found, she feels it (the first time ever); its hidden boss beaten, its
        // own Rose is hers the first time, and the land is marked so.
        if (run != null && screen == PLAY) {
            int in = run.room();
            if (in >= 0 && run.level().rooms.get(in)[0] == RunLevels.HIDEOUT) session.keep("run_hideout_" + number, 1);
            if (session.tally("run_hid_said") == 0 && captionLeft <= 0 && !audio.speaking()) {
                for (int[] one : run.level().rooms) {
                    if (one[0] != RunLevels.HIDEOUT || Math.abs(run.x() - one[1]) > 9) continue;
                    session.keep("run_hid_said", 1);
                    speak(RunStory.SECRETS[0], true);
                    break;
                }
            }
            if (run.hiddenBeaten() > seenHidden) {
                int kind = run.hiddenKind(), boss = kind - RunUniques.HIDDEN_FIRST;
                session.keep("run_hidden_" + number, 1);
                if (session.tally("run_sig_" + boss) == 0) {
                    session.keep("run_sig_" + boss, 1);
                    int signature = RunUniques.signature(kind);
                    intoBag(signature);
                    toast(RunLoot.name(signature), RunUniques.name(kind) + "'s own: in her bag", 0);
                }
                speak(RunStory.SECRETS[1], true);
                audio.play("win", .8f);
            }
            seenHidden = run.hiddenBeaten();
        }
        // Knuckles: the first time she holds them, and her first uppercut, haymaker and dash punch.
        if (run != null && screen == PLAY && captionLeft <= 0 && !audio.speaking()) {
            String[] said = null;
            if (run.weapon() == RunArms.KNUCKLES && session.tally("run_knk1") == 0) {
                session.keep("run_knk1", 1);
                said = RunStory.KNUCKLES[0];
            } else if (run.uppercuts() > 0 && session.tally("run_knk2") == 0) {
                session.keep("run_knk2", 1);
                said = RunStory.KNUCKLES[1];
            } else if (run.haymakers() > 0 && session.tally("run_knk3") == 0) {
                session.keep("run_knk3", 1);
                said = RunStory.KNUCKLES[2];
            } else if (run.dashPunches() > 0 && session.tally("run_knk4") == 0) {
                session.keep("run_knk4", 1);
                said = RunStory.KNUCKLES[3];
            }
            if (said != null) speak(said, true);
        }
        // The first time she comes near a stairway down; in a dive, its keeper seen, and her own double in ink.
        if (run != null && screen == PLAY && captionLeft <= 0 && !audio.speaking()) {
            if (delve < 0 && session.tally("run_dng1") == 0) {
                int stairs = RunLevels.stairsOf(run.level());
                if (stairs >= 0 && Math.abs(run.x() - stairs) < 9) {
                    session.keep("run_dng1", 1);
                    speak(RunStory.DUNGEONS[0], true);
                }
            } else if (delve >= 0) {
                for (PlusRun.Foe foe : run.foes()) {
                    if (foe.flat || !RunUniques.is(foe.kind) || Math.abs(foe.x - run.x()) > 10) continue;
                    if (RunUniques.keeper(foe.kind) && !keeperSaid) {
                        keeperSaid = true;
                        speak(RunStory.DUNGEONS[3], true);
                        break;
                    }
                    if (RunUniques.way(foe.kind) == RunUniques.MIRROR && session.tally("run_dng10") == 0) {
                        session.keep("run_dng10", 1);
                        speak(RunStory.DUNGEONS[9], true);
                        break;
                    }
                }
            }
        }
        // Each of the later chapters' creatures, the first time she comes near one: she says what to look out for.
        if (run != null && screen == PLAY && captionLeft <= 0 && !audio.speaking()) {
            for (PlusRun.Foe foe : run.foes()) {
                if (foe.flat || !RunUniques.later(foe.kind) || Math.abs(foe.x - run.x()) > 8 || Math.abs(foe.y - run.y()) > 5) continue;
                int which = foe.kind - RunUniques.LATER_FIRST;
                if (session.tally("run_u2_" + which) != 0) continue;
                session.keep("run_u2_" + which, 1);
                speak(RunStory.LATER_UNIQUES[which], true);
                break;
            }
        }
        // Deep water, the first time: in blue she can swim in it; without it she cannot breathe.
        if (run != null && screen == PLAY && run.inWater() && captionLeft <= 0 && !audio.speaking()) {
            boolean blue = run.has(PlusRun.BLUE);
            String said = blue ? "run_wat1" : "run_wat2";
            if (session.tally(said) == 0) {
                session.keep(said, 1);
                speak(RunStory.WATER[blue ? 0 : 1], true);
            }
        }
        // Her second wind: she says so.
        if (run != null && run.secondWinds() != seenWinds) {
            if (run.secondWinds() > seenWinds && screen == PLAY) {
                speak(RunStory.MOVES[0], true);
                audio.play("heart", .8f);
            }
            seenWinds = run.secondWinds();
        }
        // Divine power, the first time it comes in a land.
        if (run != null && run.divines() != seenDivines) {
            if (seenDivines == 0 && run.divines() == 1 && screen == PLAY) speak(RunStory.SKILLS[1], true);
            seenDivines = run.divines();
        }
        if (screen == PLAY && video == null) {
            clearedAge += seconds;
            diaryAge += seconds;
            levelAge += seconds;
            crossAge += seconds;
            warpAge += seconds;
            arriveAge += seconds;
            // Coming into a village, its name, once a land.
            if (run != null && run.room() < 0 && run.level().town >= 0 && !villageSaid && run.x() > run.level().town + 1) {
                villageSaid = true;
                toast("Welcome to " + RunLevels.VILLAGES[Math.max(0, Math.min(RunLevels.VILLAGES.length - 1, run.level().chapter))], villageWords(), 0);
            }
            if (warpAge >= WARP && warpAge - seconds < WARP) {
                flash = .35f;
                openWarp();
            }
            int due = clearedAge > .5f ? Math.min(clearedStars, (int) ((clearedAge - .5f) / .3f) + 1) : 0;
            if (clearedAge < CLEARED && due > clearedShown) {
                clearedShown = due;
                audio.play("star", .55f);
            }
        }
        sinceAsked += seconds;
        sinceBump += seconds;
        sinceShut += seconds;
        sinceOpened += seconds;
        sinceParry += seconds;
        sinceStruck += seconds;
        sinceBroke += seconds;
        sinceStep += seconds;
        mixAge += seconds;
        for (int slot = 0; slot < huntFlash.length; slot++) huntFlash[slot] = Math.max(0, huntFlash[slot] - seconds / 3);
        hammered += seconds;
        capsuleAge += seconds;
        cutAge += seconds;
        dashAge += seconds;
        airAge += seconds;
        thrustAge += seconds;
        lashAge += seconds;
        spinAge += seconds;
        if (screen == PLAY && ribbonFinger >= 0 && !fanOpen && (ribbonHeld += seconds) >= .3f && Integer.bitCount(perks.colours & 127) > 1) openPicker(false);
        // Held, the button that changes what she holds opens her backpack, and the land waits.
        if (screen == PLAY && swapFinger >= 0 && !packOpen && (swapHeld += seconds) >= .35f) openPack(PLAY);
        sinceHeavy += seconds;
        panelAge += seconds;
        if (toastLeft <= 0 && !toasts.isEmpty() && roomCardAge < 0 && roomResultAge < 0
                && (run == null || run.wavePhase() == 0 || run.wavePhase() == PlusRun.FOUGHT && sincePhase > 1)) {
            Object[] next = toasts.remove(0);
            toast((String) next[0], (String) next[1], (Integer) next[2]);
        }
        // While what was won from whoever fell is rising, everything else waits.
        if (screen == PLAY && finale != NO_FINALE) {
            // The end of the last land has a time of its own.
            finaleTick(seconds);
        } else if (screen == PLAY && prizeAge >= 0 && prizeAge < 2) {
            prizeAge += seconds;
        } else if (screen == PLAY && warpAge < WARP) {
            // Going up in the teleporter's light, the land waits.
        } else if (screen == PLAY && video != null) {
            // A film played over the land, after it was cleared: the land waits under it.
        } else if (screen == PLAY && (fanOpen || packOpen || huntsOpen || smithOpen || marketOpen)) {
            // The land waits while she chooses.
        } else if (screen == PLAY && roomCardAge >= 0 && roomCardAge < 2.4f && roomHolds()) {
            roomCardAge += seconds;
        } else if (screen == PLAY && roomResultAge >= 0 && roomResultAge < 2.6f) {
            roomResultAge += seconds;
        } else if (screen == PLAY && handAge >= 0) {
            handAge += seconds;
            // What it was is named, in a card at the top that goes by itself; and the land does not wait.
            if (handAge > 1.5f && !handStamped) {
                handStamped = true;
                toast(handTitle, handWords, handPicture);
                buzz(true);
            }
            // When whoever thanked her has finished, Halgakos has her say; and then it is over.
            if (handAge > 2.4f && !handSaid && (!audio.speaking() || handAge > 5)) {
                handSaid = true;
                speak(RunStory.PRIZES[handPicture == CAKE_PICTURE ? 2 : 1], true);
            }
            if (handAge > 7.5f || handSaid && handAge > 4 && !audio.speaking() && captionLeft <= 0) handAge = -1;
            step(seconds);
        } else if (screen == PLAY) {
            if (prizeAge >= 2 && (prizeAge += seconds) > 3) prizeAge = -1;
            if (roomCardAge >= 0 && (roomCardAge += seconds) > 3.4f) roomCardAge = -1;
            if (roomResultAge >= 0 && (roomResultAge += seconds) > 5.5f) roomResultAge = -1;
            step(seconds);
        }
        if (screen == RESULT) {
            resultClock += seconds;
            // The stars come up one after another, each with a ring to it.
            int due = Math.min(stars, (int) ((resultClock - .35f) / .32f) + 1);
            if (resultClock > .35f && due > starsShown) {
                starsShown = due;
                audio.play("star", .6f);
            }
        }
    }

    private void pop(float across, float down, String words) {
        pops.add(new float[]{across, down, 0});
        popWords.add(words);
    }

    /** The story of a land is told a line after another, each for as long as it takes to say or to read, while she plays. */
    private void talks() {
        if (talking && audio.speaking()) captionLeft = Math.max(captionLeft, .35f);
        if (captionLeft > 0 || audio.speaking()) return;
        if (talk.isEmpty()) {
            talking = false;
            return;
        }
        talking = told = true;
        speak(talk.remove(0), true);
        told = false;
        personLeft = captionLeft;
    }

    private void step(float seconds) {
        // (Held still a moment by a heavy blow: what she pressed waits for it.)
        if (hitStop > 0) {
            hitStop -= seconds;
            return;
        }
        if (slow > 0) {
            slow -= seconds;
            seconds *= .3f;
        }
        talks();
        if (goLeft || goRight) hintMoved = true;
        if (goJump) hintJumped = true;
        run.down(goDown);
        int carried = run.weapon(), heartsBefore = run.halves(), lookBefore = look, formBefore = run.form();
        boolean colouredBefore = run.coloured();
        float wasX = run.x() + PlusRun.WIDE / 2, wasY = run.y() + PlusRun.TALL;
        boolean wasLeft = run.facingLeft();
        run.step(seconds, goLeft, goRight, goJump || jumpPressed, goStrike || strikePressed);
        movesAfter(seconds);
        jumpPressed = false;
        strikePressed = false;
        sinceUse += seconds;
        uppercutAge += seconds;
        haymakerAge += seconds;
        sinceAim += seconds;
        float across = width / tile, down = height / tile;
        float goal = Math.max(0, Math.min(Math.max(0, run.columns() - across), run.x() + PlusRun.WIDE / 2 - across * (run.facingLeft() ? .58f : .4f)));
        // Shut in with someone, the view holds on the place: all of it, if it fits, and never beyond its ends if it does not.
        if (run.locked() || run.opening() > 0) {
            float from = run.arenaFrom(), to = run.arenaTo() + 1;
            goal = to - from <= across ? from - (across - (to - from)) / 2 : Math.max(from, Math.min(to - across, goal));
            goal = Math.max(0, Math.min(Math.max(0, run.columns() - across), goal));
        }
        // In a place off the land the view keeps to it; in the land, it stops at the land's end, short of them.
        if (run.room() >= 0) {
            float from = run.roomFrom() - 1, to = run.roomTo() + 2;
            goal = to - from <= across ? from - (across - (to - from)) / 2 : Math.max(from, Math.min(to - across, run.x() + PlusRun.WIDE / 2 - across / 2));
        } else {
            goal = Math.min(goal, Math.max(0, run.level().main - across));
        }
        // Through a door she is somewhere else at once, and so is the view.
        if (run.entered() != seenEntered) cameraX = goal;
        // The view stays where she was hurt for a moment, so she is seen to be thrown, before it goes to where she is now.
        boolean held = knockAge >= 0 && knockAge < .5f;
        if (!held) cameraX += (goal - cameraX) * Math.min(1, seconds * 7);
        // Where the land is taller than the screen, the view goes up and down with her.
        // Down in a cave under the land, the view follows her there, as deep as the land goes.
        boolean deep = run.level().under != null && run.room() < 0 && run.y() + PlusRun.TALL > RunLevels.ROWS - .5f;
        // (The height it follows: her feet when she stands somewhere a good way off from the last; in the air, only once she is well above
        // or below it. A step, a jump or a hop onto a ledge leaves it where it is.)
        float feet = run.y() + PlusRun.TALL;
        if (camFeet < -50 || run.entered() != seenEntered) camFeet = feet;
        else if (run.onGround() ? Math.abs(feet - camFeet) > CAMERA_STILL : feet > camFeet + 3 || feet < camFeet - 4.5f) camFeet = feet;
        float level = deep ? Math.max(0, Math.min(run.level().land.length - down, run.y() + PlusRun.TALL - down * .55f))
                : down >= RunLevels.ROWS ? restingCamera()
                : Math.max(0, Math.min(RunLevels.ROWS - down, camFeet - down * .62f));
        if (run.room() >= 0) {
            // A place off the land has a floor and a ceiling of its own: an arena's are far apart, and the view follows her between them.
            int[] in = run.level().rooms.get(run.room());
            float ceiling = in[12] - 2, ground = in[11] + 3;
            // Inside a building, the floor and what stands on it, with its prices, are kept above the buttons over the arrows and the jump,
            // as far as the screen is tall enough to show the whole of the room as well (in a wide screen the buttons are at its sides instead).
            if (in[0] == RunLevels.HOUSE) {
                float buttons = Math.min(Math.min(stallButton.top, downButton.top), swapButton.top) - dp(8);
                ground = Math.max(ground, Math.min(in[11] + .5f + (height - buttons) / tile, in[12] - .5f + down));
            }
            level = ground - ceiling <= down ? ground - down : Math.max(ceiling, Math.min(ground - down, run.y() + PlusRun.TALL - down * .6f));
            if (run.entered() != seenEntered) cameraY = level;
        } else if (run.entered() != seenEntered) {
            cameraY = level;
        }
        // Gently; but quickly when she is near the bottom or the top of the view.
        float fromTop = feet - cameraY, pace = fromTop > down * .86f || fromTop < down * .18f ? 9 : 2.5f;
        if (!held) cameraY += (level - cameraY) * Math.min(1, seconds * pace);
        if (squash > 0) squash -= seconds;
        float herX = run.x() + PlusRun.WIDE / 2, herY = run.y() + PlusRun.TALL;
        // The colour she is drawn in: when it changes, the one fades into the other.
        lookAge += seconds;
        flinchAge += seconds;
        int now = formDrawn();
        if (now != look) {
            lookFrom = look;
            look = now;
            lookAge = 0;
        }
        // A colour gone: it leaves her in a scatter, with a sound of its own.
        if (run.faded() != seenFaded) {
            seenFaded = run.faded();
            audio.play("pop", .45f);
            for (int spark = 0; spark < 14; spark++) {
                double way = -Math.PI / 2 + (random.nextFloat() - .5f) * 2.4f;
                bits.add(new float[]{herX + (random.nextFloat() - .5f) * .7f, herY - 1.1f - random.nextFloat() * .5f, (float) Math.cos(way) * 2.2f,
                        (float) Math.sin(way) * 3.4f - 2, .8f, formBefore > 0 && formBefore < PlusRun.WHITE ? 2 + formBefore : 0});
            }
        }
        // A mix that has a name has formed: its name, what it is made of and what it does, for a moment; and it is kept that she has made it.
        if (run.mixings() != seenMixings) {
            seenMixings = run.mixings();
            mixShown = run.lastMix();
            mixAge = 0;
            audio.play("star", .7f);
            buzz(true);
            int made = session.tally("run_mixes");
            if ((made & 1 << mixShown) == 0) {
                session.keep("run_mixes", made | 1 << mixShown);
                if (made == 0 && mixShown != PlusRun.WHITEN) speak(RunStory.MIX[1], true);
            }
        }
        // White, of every colour at once: a white she has not mastered, and the first time ever she says so. (The white that is kept is
        // not this one: it is given at the end of the story.)
        if (run.whitened() != seenWhitened) {
            seenWhitened = run.whitened();
            flash = .3f;
            audio.play("star", .8f);
            if (run.mastered()) {
                speak(RunStory.TRUE_WHITE[0], true);
            } else if (session.tally("run_hint_white") == 0) {
                session.keep("run_hint_white", 1);
                speak(RunStory.MIX[2], true);
            }
        }
        // Black put on her: a sound of its own, the screen darkens for a moment, and it says what it has done and for how long.
        if (run.curses() != seenCurses) {
            seenCurses = run.curses();
            audio.play("censor", .9f);
            audio.play("lose", .4f);
            buzz(true);
            shake = Math.max(shake, .25f);
            darkened = .6f;
            pop(herX, herY - 1.9f, "Black: no ribbons for " + Math.round(run.cursedFor()) + " s");
            // And she says how it weighs on her (not every time: once in a while).
            if (run.inked() && clock - lastBlackLine > 40) {
                lastBlackLine = clock;
                speak(RunStory.BLACKENED[0], true);
            }
        }
        if (darkened > 0) darkened -= seconds;
        if (hintMix && run.time() > 1.2f && captionLeft <= 0 && personLeft <= 0) {
            hintMix = false;
            session.keep("run_hint_mix", 1);
            speak(RunStory.MIX[0], true);
            glowPalette = true;
        }

        if (run.jumps() != seenJumps) {
            seenJumps = run.jumps();
            audio.play("jump", .45f);
        }
        if (run.flaps() != seenFlaps) {
            seenFlaps = run.flaps();
            audio.play("flap", .6f);
            for (int feather = 0; feather < 6; feather++) {
                bits.add(new float[]{herX, herY - .5f, (random.nextFloat() - .5f) * 5, random.nextFloat() * 2, .4f, 1});
            }
        }
        if (run.landings() != seenLandings) {
            seenLandings = run.landings();
            squash = SQUASH;
            for (int puff = 0; puff < 5; puff++) {
                bits.add(new float[]{herX, herY, (random.nextFloat() - .5f) * 5, -random.nextFloat() * 1.6f, .32f, 1});
            }
        }
        if (run.collected() != seenCollected) {
            seenCollected = run.collected();
            audio.play("plus", .5f);
            for (int spark = 0; spark < 7; spark++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{run.plusX(), run.plusY(), (float) Math.cos(way) * 4.5f, (float) Math.sin(way) * 4.5f - 2, .38f, 0});
            }
        }
        if (run.taken() != seenTaken) {
            seenTaken = run.taken();
            int kind = run.lastTaken(), giver = run.giver();
            audio.play(kind == PlusRun.BREAD ? "bread" : kind > PlusRun.FORM ? "form" : "buff", .6f);
            buzz(false);
            // Bread with every heart full is worth three pluses instead, and Chikuwa says so.
            boolean full = kind == PlusRun.BREAD && run.bonus() != seenBonus;
            // Someone met in a village has her own word as she hands it over; a ribbon's word is said when her hair turns.
            // Whoever has something to ask of her asks it as she hands over what she holds, in place of her usual word.
            boolean asks = run.questGiven() != seenAsked && run.quest() == PlusRun.ASKED;
            seenAsked = run.questGiven();
            if (asks) person(RunStory.ASKS[run.level().quest - 1]);
            else if (giver >= RunLevels.SOMI && giver <= RunLevels.MORU && number > 40 && !saidRumour) {
                // From the second chapter of the second volume, one of those met in a land has heard something.
                saidRumour = true;
                person(RunStory.RUMOURS[giver - RunLevels.SOMI]);
            } else if (giver >= RunLevels.SOMI) person(RunStory.MEET[giver - RunLevels.SOMI]);
            else if (full) person(RunStory.FULL[0]);
            else if (kind == PlusRun.HOMERUN || kind == PlusRun.COMPLIMENT) person(RunStory.GIFTS[kind - PlusRun.HOMERUN]);
            else if (kind <= PlusRun.NOTES && giver != 0) person(RunStory.TOOK[kind]);
            else if (kind <= PlusRun.NOTES) aside(RunStory.TOOK[kind]);
            if (full) pop(herX, herY - 1.6f, "+ 3");
            if (kind == PlusRun.HOMERUN) audio.play("bat", .7f);
            seenBonus = run.bonus();
        }
        if (run.formed() != seenFormed) {
            seenFormed = run.formed();
            int form = run.form();
            flash = .3f;
            // A colour coming on: a ring of it closing on her.
            for (int spark = 0; spark < 16 && form > 0; spark++) {
                double way = spark * Math.PI / 8;
                bits.add(new float[]{herX + (float) Math.cos(way) * 1.6f, herY - .8f + (float) Math.sin(way) * 1.6f, -(float) Math.cos(way) * 3.4f,
                        -(float) Math.sin(way) * 3.4f - 1.6f, .42f, 2 + form});
            }
            if (form > 0 && !saidForm[form] && personLeft <= 0) {
                saidForm[form] = true;
                speak(RunStory.FORMS[form - 1], true);
            }
        }
        // A plus caught as it falls is only a plus.
        if (run.room() >= 0 && run.bonus() != seenBonus && run.roomWins() == seenWins) {
            audio.play("plus", .5f);
            seenBonus = run.bonus();
        }
        if (run.bonus() != seenBonus && run.roomWins() == seenWins) {
            // One sign after another: landed on without touching the ground between, or hit with what she throws.
            int row = run.streak() > 1 ? run.streak() : run.combo();
            pop(run.foeX(), run.foeY() - 1, "× " + row + "   + " + (run.bonus() - seenBonus));
            seenBonus = run.bonus();
            audio.play("combo", .5f);
            if (row >= 3 && !saidCombo) {
                saidCombo = true;
                speakAny(RunStory.COMBO);
            }
        }
        if (run.spared() != seenSpared) {
            seenSpared = run.spared();
            audio.play("pop");
            buzz(true);
            speakAny(RunStory.SPARED);
            for (int drop = 0; drop < 10; drop++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{herX, herY - .6f, (float) Math.cos(way) * 5, (float) Math.sin(way) * 5 - 1, .4f, 2});
            }
        }
        if (run.charms() != seenCharms) {
            seenCharms = run.charms();
            audio.play("charm");
            buzz(true);
            if (!saidCharm) {
                saidCharm = true;
                speakAny(RunStory.CHARMED);
            }
        }
        if (run.picked() != seenPicked) {
            seenPicked = run.picked();
            audio.play("pick", .6f);
            buzz(false);
            // Her word about it, the first time in a land.
            int found = run.weapon();
            if (found >= 0 && found < RunStory.ARMS.length && !remarked[found] && personLeft <= 0) {
                remarked[found] = true;
                speak(RunStory.ARMS[found], true);
            }
        }
        if (run.casts() != seenCasts) {
            // The fire or the water of a colour, thrown: with whatever she carries, or with nothing.
            seenCasts = run.casts();
            sinceUse = 0;
            audio.play("flap", .5f);
        }
        if (run.braces() != seenBraces) {
            // In brown, something at arm's length has been turned aside: it rings, and sparks fly from where it met her.
            seenBraces = run.braces();
            audio.play("clang", .7f);
            buzz(false);
            shake = Math.max(shake, .12f);
            for (int spark = 0; spark < 10; spark++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{run.braceX(), run.braceY(), (float) Math.cos(way) * 6, (float) Math.sin(way) * 6 - 2, .35f, spark % 2 == 0 ? 2 + PlusRun.BROWN : 0});
            }
            pop(run.braceX(), run.braceY() - 1, "Turned aside");
        }
        if (run.uppercuts() != seenUppercuts) {
            seenUppercuts = run.uppercuts();
            uppercutAge = 0;
        }
        if (run.haymakers() != seenHaymakers) {
            seenHaymakers = run.haymakers();
            haymakerAge = 0;
            shake = Math.max(shake, .1f);
        }
        if (run.strikes() != seenStrikes) {
            seenStrikes = run.strikes();
            sinceUse = 0;
            if (carried == PlusRun.GUN) audio.play("shot", .4f);
            else if (carried == PlusRun.BRUSH || carried == PlusRun.SWORD) audio.play("slash", .5f);
            else audio.play("throw", .5f);
        }
        // Whoever waits here: the first of them that still stands, or the last that stood; in a cellar, the Old Book if he is awake.
        PlusRun.Foe boss = null;
        // (One met in the middle of a land is not one of them: it keeps no gate, and is told of by itself.)
        PlusRun.Foe rover = null;
        for (PlusRun.Foe foe : run.foes()) {
            if (RunLevels.rover(foe.kind)) rover = foe;
            else if (RunLevels.boss(foe.kind) && !foe.spawned && (boss == null || boss.flat)) boss = foe;
        }
        for (PlusRun.Foe foe : run.foes()) if (RunLevels.boss(foe.kind) && foe.spawned && run.room() >= 0) boss = foe;
        roverAge += seconds;
        if (run.roused() != seenRoused) {
            seenRoused = run.roused();
            if (rover != null) {
                // It shows itself: its name across the screen, a sound of its own, and the first time a word about it.
                roverKind = rover.kind;
                roverAge = 0;
                audio.play(rover.kind == RunLevels.WYRM ? "flap" : rover.kind == RunLevels.BLOT ? "quake" : "page", .8f);
                audio.play("star", .5f);
                buzz(true);
                if (rover.kind == RunLevels.BLOT) shake = Math.max(shake, .35f);
                if (seenRoused == 1) speak(RunStory.ROVERS[rover.kind - RunLevels.WYRM], true);
            }
        }
        // (In a build made for testing, with the sweeping on, a blow lands on it whenever one would.)
        if (BuildConfig.DEBUG && session.tally("run_sweep") > 0 && (sinceDeal += seconds) > .45f) {
            sinceDeal = 0;
            if (rover != null) run.deal(rover);
            if (boss != null && run.locked()) run.deal(boss);
        }
        // The wyrm gone for the third time and not beaten: it is driven off.
        if (rover != null && rover.kind == RunLevels.WYRM && !roverOver && run.roused() >= 3 && rover.away && !rover.flat) {
            roverOver = true;
            roverDone(rover, false);
        }
        if (boss == null && run.phases() != seenPhases) {
            // It changes, a third of what it can take gone.
            seenPhases = run.phases();
            audio.play("form", .6f);
            flash = .2f;
        }
        if (run.shuts() != seenShuts) {
            // Whoever she fights has shut one of her colours off as she changed.
            seenShuts = run.shuts();
            audio.play("censor", .7f);
            buzz(true);
            pop(herX, herY - 1.9f, FORM_NAMES[run.lastShut() - 1] + " shut off!");
            if (session.tally("run_hint_shut") == 0) {
                session.keep("run_hint_shut", 1);
                speak(RunStory.SHUT_OFF[0], true);
            }
        }
        if (run.drunk() != seenDrunk) {
            seenDrunk = run.drunk();
            audio.play("splash", .6f);
            pop(run.foeX(), run.foeY() - 1.6f, "It drinks fire!");
        }
        if (run.grounded() != seenGrounded) {
            seenGrounded = run.grounded();
            audio.play("censor", .6f);
            buzz(true);
            pop(herX, herY - 1.9f, "Grounded!");
        }
        if (run.fizzled() != seenFizzled) {
            seenFizzled = run.fizzled();
            audio.play("parry", .4f);
            pop(run.foeX(), run.foeY() - 1.4f, run.fizzledAt() == RunLevels.WISP ? "Only water!" : "Only fire!");
        }
        sinceWings += seconds;
        petGlad += seconds;
        if (run.petJoined() != seenPetJoined) {
            // A pet goes with her: a card names it and what it does, and the first time each kind does she has a word about it.
            seenPetJoined = run.petJoined();
            int kind = run.lastJoined(), had = session.tally("run_pets");
            petGlad = 0;
            audio.play("star", .8f);
            buzz(true);
            if (kind >= 0) {
                toast(PET_NAMES[kind] + " goes with her", "A stray, for the whole of this land: it " + PET_DOES[kind], -(100 + kind));
                // (Her word on one the first time it joins her: the strays have one each; those of the ones she has beaten never come as strays.)
                if ((had & 1 << kind) == 0 && kind < RunStory.PETS.length) {
                    session.keep("run_pets", had | 1 << kind);
                    speak(RunStory.PETS[kind], true);
                }
            }
        }
        if (run.petStrikes() != seenPetStrikes) {
            seenPetStrikes = run.petStrikes();
            int one = run.herd().size() == 1 ? run.pet() : -1;
            audio.play(one == PlusRun.DUCK || one == PlusRun.IMP ? "flap" : one == PlusRun.JAR || one == PlusRun.MINI_RUNE ? "splash" : one == PlusRun.CHERUB ? "ding" : "throw", .35f);
        }
        if (run.petKills() != seenPetKills) {
            seenPetKills = run.petKills();
            petGlad = 0;
        }
        // Her backpack: a thing found for the first time is hers from now on, and it is said; one she has already gives something else.
        if (run.finds() != seenFinds) {
            seenFinds = run.finds();
            int kind = run.lastFound();
            session.keep("run_pack", session.tally("run_pack") | run.owned());
            if (kind == PlusRun.KEYS) session.keep("run_mains", session.tally("run_mains") | 3);
            audio.play("unlock");
            buzz(true);
            String does = RunArms.does(kind);
            toast(RunArms.NAMES[kind] + ": in her backpack, for good", does.substring(0, does.indexOf('.') + 1), 0);
        }
        if (run.refills() != seenRefills) {
            seenRefills = run.refills();
            int kind = run.lastRefill();
            pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, kind == PlusRun.GUN ? "+ " + RunArms.INK + " ink" : kind == PlusRun.BOOK ? "+ " + RunArms.PAGES + " books" : "Stronger for a while");
        }
        if (run.duds() != seenDuds) {
            seenDuds = run.duds();
            audio.play("no", .5f);
            pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, "401");
        }
        if (run.panels() != seenPanels) {
            seenPanels = run.panels();
            sinceHeavy = 0;
            panelAge = 0;
            panelX = run.panelX();
            panelY = run.panelY();
            panelLeft = run.panelLeftward();
            audio.play("page", .8f);
            audio.play("ding", .5f);
            shake = Math.max(shake, .12f);
        }
        if (run.fans() != seenFans) {
            seenFans = run.fans();
            sinceHeavy = 0;
            audio.play("throw", .7f);
        }
        // A friend walked up to, in the third volume: she pledges her help, which is kept; and says so.
        if (run.pledges() != seenPledges) {
            seenPledges = run.pledges();
            int friend = run.lastPledge(), had = session.tally("run_friends");
            if (friend >= 0 && friend < RunLevels.FRIENDS) {
                if ((had >> friend & 1) == 0) {
                    session.keep("run_friends", had | 1 << friend);
                    boolean several = friend == 8 || friend == 14 || friend == 15;
                    toast(RunStory.FRIENDS[friend] + (several ? " pledge their help" : " pledges her help"), Integer.bitCount(had | 1 << friend) + " of " + RunLevels.FRIENDS + " are with her", 0);
                    audio.play("star", .7f);
                    buzz(true);
                }
                person(RunStory.PLEDGES[friend]);
            }
        }
        // A capsule of the ribbon gacha, come out: it opens, and what was in it is hers, or is not.
        if (capsuleWaits && capsuleAge >= .9f) opened();
        if (run.potShots() != seenPotShots) {
            seenPotShots = run.potShots();
            audio.play("throw", .6f);
        }
        // The four won in the black arena: where she was and which way she faced as each thing was done, to draw it from.
        boolean did = false;
        if (run.cuts() != seenCuts) {
            seenCuts = run.cuts();
            cutAge = 0;
            cutChain = run.chain();
            did = true;
            audio.play("slash", cutChain == 3 ? .9f : .6f);
            if (cutChain == 3) shake = Math.max(shake, .1f);
        }
        if (run.airCuts() != seenAirCuts) {
            seenAirCuts = run.airCuts();
            airAge = 0;
            did = true;
            audio.play("slash", .6f);
        }
        if (run.dashes() != seenDashes) {
            seenDashes = run.dashes();
            dashAge = 0;
            sinceHeavy = 0;
            did = true;
            audio.play("flap", .7f);
        }
        if (run.thrusts() != seenThrusts) {
            seenThrusts = run.thrusts();
            thrustAge = 0;
            did = true;
            audio.play("slash", .5f);
        }
        if (run.lashes() != seenLashes) {
            seenLashes = run.lashes();
            lashAge = 0;
            did = true;
            audio.play("slash", .5f);
        }
        if (run.spins() != seenSpins) {
            seenSpins = run.spins();
            spinAge = 0;
            sinceHeavy = 0;
            did = true;
            audio.play("slash", .8f);
        }
        if (run.jets() != seenJets) {
            seenJets = run.jets();
            sinceHeavy = 0;
            audio.play("shot", .7f);
        }
        if (run.shells() != seenShells) {
            seenShells = run.shells();
            sinceHeavy = 0;
            shake = Math.max(shake, .14f);
            audio.play("blow", .7f);
        }
        if (did) {
            doneX = run.x() + PlusRun.WIDE / 2;
            doneY = run.y();
            doneLeft = run.facingLeft();
        }
        if (run.switches() != seenSwitches) {
            seenSwitches = run.switches();
            audio.play("tap", .4f);
        }
        // In a place off the land: a key taken, a lever pulled, a plate stood on, a shrine paid.
        if (run.keys() != seenKeys) {
            seenKeys = run.keys();
            audio.play("unlock");
            buzz(true);
            toast("A key", "The locked door of this place is open", 0);
        }
        if (run.levers() != seenLevers) {
            seenLevers = run.levers();
            audio.play("unlock");
            buzz(true);
            shake = Math.max(shake, .2f);
            pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, "A bridge!");
        }
        if (run.plates() != seenPlates) {
            seenPlates = run.plates();
            audio.play("ding", .8f);
            pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, "The bars are up: run!");
        }
        if (run.shrines() != seenShrines) {
            seenShrines = run.shrines();
            audio.play("star", .8f);
            pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, "A heart for a ribbon");
        }
        // The small stuffed ones: a roll handed over, the ring of light used, and a word in a tiny bubble now and then.
        petWordLeft -= seconds;
        sincePetWord += seconds;
        if (run.petGifts() != seenPetGifts) {
            seenPetGifts = run.petGifts();
            petGlad = 0;
            audio.play("star", .7f);
            flyTo((run.petX() - cameraX) * tile, (run.petY() - 1 - cameraY) * tile, true, 1);
            pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, "Half a heart");
            petSays("Here!", PlusRun.MINI_CHIKUWA);
        }
        if (run.petWards() != seenPetWards) {
            seenPetWards = run.petWards();
            audio.play("ding", .6f);
            pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, "Turned aside");
            wardAge = 0;
        }
        wardAge += seconds;
        if (run.petCopies() != seenPetCopies) {
            seenPetCopies = run.petCopies();
            if (sincePetWord > 9) petSays("Me too!", PlusRun.TWIN);
        }
        for (PlusRun.Pet it : run.herd()) {
            if (it.kind == PlusRun.IMP && sincePetWord > 13 && it.busy > 0) petSays(IMP_SAYS[(int) (clock * 7) % IMP_SAYS.length], it.kind);
            if (it.kind == PlusRun.MINI_RUNE && sincePetWord > 11 && boss != null) petSays("Uu...", it.kind);
        }
        // A pet lying where she stands that she has no room for: she is told, once, that she may change a stray for it.
        if (run.petOffer() != offerSeen) {
            offerSeen = run.petOffer();
            if (offerSeen >= 0) pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, "No room for it: Swap?");
        }
        if (run.petSwaps() != seenPetSwaps) {
            seenPetSwaps = run.petSwaps();
            audio.play("star", .7f);
            petGlad = 0;
        }
        if (run.cracks() != seenCracks) {
            // A piece off a shield: it rings, higher the less of it is left, and shards fly.
            seenCracks = run.cracks();
            sinceStruck = 0;
            audio.play("parry", .5f);
            audio.play("clang", .35f);
            buzz(false);
            for (int shard = 0; shard < 7; shard++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{run.foeX(), run.foeY() - .8f, (float) Math.cos(way) * 6, (float) Math.sin(way) * 6 - 2, .4f, 0});
            }
        }
        if (run.shatters() != seenShatters) {
            // The last piece: it breaks, with a flash and a burst, and for a beat everything is slow.
            seenBroken = run.shieldsBroken();
            audio.play("crumble", 1f);
            audio.play("star", .8f);
            buzz(true);
            flash = .3f;
            shake = Math.max(shake, .35f);
            slow = .45f;
            pop(run.foeX(), run.foeY() - 2.2f, "Shield broken!");
            for (int shard = 0; shard < 26; shard++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{run.foeX(), run.foeY() - .8f, (float) Math.cos(way) * (5 + random.nextFloat() * 6), (float) Math.sin(way) * 8 - 3, .7f, shard % 3 == 0 ? 1 : 0});
            }
        }
        if (run.thrownBack() != seenThrownBack) {
            seenThrownBack = run.thrownBack();
            audio.play("quake", .6f);
            pop(herX, herY - 1.9f, "Thrown back!");
        }
        if (run.blasts() != seenBlasts) {
            seenBlasts = run.blasts();
            audio.play("quake", .7f);
            audio.play("page", .9f);
            buzz(true);
            shake = Math.max(shake, .25f);
            blastAge = 0;
            blastX = run.blastX();
            blastY = run.blastY();
            for (int page = 0; page < 22; page++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{blastX, blastY, (float) Math.cos(way) * (3 + random.nextFloat() * 6), (float) Math.sin(way) * 6 - 4, .9f, 1});
            }
        }
        blastAge += seconds;
        if (run.beams() != seenBeams) {
            seenBeams = run.beams();
            audio.play("form", .9f);
            audio.play("fire", .7f);
            buzz(true);
            shake = Math.max(shake, .2f);
            flash = .25f;
        }
        if (run.drops() != seenDrops) {
            seenDrops = run.drops();
            audio.play("flap", .35f);
            buzz(false);
        }
        if (run.droppable() && session.tally("run_hint_down") == 0 && captionLeft <= 0 && personLeft <= 0 && !talking) {
            session.keep("run_hint_down", 1);
            speak(RunStory.DOWN[0], true);
        }
        if (run.dives() != seenDives) {
            seenDives = run.dives();
            audio.play("flap", .5f);
        }
        // Those that fly: the first time one of a kind is near, a word about it.
        if (number >= RunLevels.FLIERS_FROM && captionLeft <= 0 && personLeft <= 0 && run.room() < 0) {
            for (PlusRun.Foe foe : run.foes()) {
                if (!RunLevels.flier(foe.kind) || foe.flat || (flySeen & 1 << foe.kind - RunLevels.DRIFTER) != 0 || Math.abs(foe.x - herX) > 4.5f) continue;
                flySeen |= 1 << foe.kind - RunLevels.DRIFTER;
                session.keep("run_fly_seen", flySeen);
                speak(RunStory.FLIERS[foe.kind - RunLevels.DRIFTER], true);
                break;
            }
        }
        if (run.woken() != seenWoken && boss != null) {
            seenWoken = run.woken();
            audio.play("page", .8f);
            buzz(true);
            speak(RunStory.ENTER[RunStory.waiting(boss.kind)], true);
        }
        // How whoever waits stands toward a blow: what kept blows off her breaks when she is spent, with a sound of its own; and the first
        // time anyone is met who has such a thing, a word about what it means.
        PlusRun.Foe guards = boss != null && !boss.flat && (run.locked() || boss.spawned) ? boss : rover != null && !rover.flat && !rover.away ? rover : null;
        int guard = guards == null ? -1 : run.guard(guards);
        if (guard == PlusRun.BARE && guardWas == PlusRun.SHIELDED) {
            sinceBroke = 0;
            audio.play("crumble", .6f);
        }
        if (guard == PlusRun.SHIELDED && guardWas != PlusRun.SHIELDED && session.tally("run_hint_shield") == 0 && captionLeft <= 0 && personLeft <= 0) {
            session.keep("run_hint_shield", 1);
            speak(RunStory.SHIELD[0], true);
        }
        guardWas = guard;
        if (run.called() != seenCalled) {
            seenCalled = run.called();
            session.keep("run_calls", session.tally("run_calls") + 1);
            // The first time she is a shadow, a word about it.
            if (run.shadow() && session.tally("run_hint_shadow") == 0) {
                session.keep("run_hint_shadow", 1);
                speak(RunStory.SHADOW[0], true);
            }
        }
        if (run.bursts() != seenBursts) {
            seenBursts = run.bursts();
            audio.play("quake", .8f);
            shake = .3f;
            flash = .25f;
            buzz(true);
            for (int spark = 0; spark < 16; spark++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{herX, herY - .7f, (float) Math.cos(way) * 8, (float) Math.sin(way) * 8 - 2, .5f, 2 + PlusRun.BLACK});
            }
        }
        if (run.thrown() != seenThrown) {
            seenThrown = run.thrown();
            boolean hearts = boss != null && boss.kind == RunLevels.ILLYA && run.locked();
            audio.play(hearts ? "heart" : "throw", hearts ? .5f : .3f);
        }
        if (run.entrances() != seenEntrances && boss != null) {
            seenEntrances = run.entrances();
            // The way back shuts, and they arrive, each in her own way, with a word.
            sinceShut = 0;
            audio.play("clang", .7f);
            int kind = boss.kind;
            audio.play(kind == RunLevels.ILLYA ? "heart" : kind == RunLevels.TSUBAKIEH ? "bat" : kind == RunLevels.ZERO || kind == RunLevels.KING ? "quake"
                    : kind == RunLevels.GOKHAE || kind == RunLevels.BYEORU ? "page" : kind == RunLevels.MIRI ? "censor" : kind == RunLevels.YEOBAEK ? "slash"
                    : kind == RunLevels.GUARD ? "clang" : kind == RunLevels.BUTLER || kind == RunLevels.SUMBI ? "splash" : kind == RunLevels.JUPAN ? "buy"
                    : kind == RunLevels.PADS ? "quake" : kind == RunLevels.PARTY ? "bat" : kind == RunLevels.LUCY ? "splash" : "flap", .7f);
            if (kind == RunLevels.ZERO || kind == RunLevels.KING || kind == RunLevels.PADS) shake = .5f;
            buzz(true);
            speak(kind == RunLevels.LUCY ? RunStory.LUCY[0] : RunStory.ENTER[RunStory.waiting(kind)], true);
        }
        if (run.specials() != seenSpecials && boss != null) {
            seenSpecials = run.specials();
            int what = run.special();
            audio.play(what == 391 || what == 393 || what == 394 ? "splash" : what == 395 ? "flap" : what == 63 || what == 72 || what == 32 || what == 181 || what == 31 || what == 184 || what == 202 || what == 241 || what == 251 || what == 263
                    ? "quake" : what == 201 || what == 242 || what == 262 || what == 212 ? "slash" : what == 211 || what == 233 || what == 222 ? "splash"
                    : what == 231 ? "buy" : what == 61 || what == 62 || what == 243 ? "heart"
                    : what == 71 || what == 73 ? "bat" : what == 171 || what == 172 || what == 174 || what == 163 ? "slash" : what == 34 ? "parry"
                    : what == 143 ? "charm" : what == 161 ? "censor" : what == 193 || what == 196 || what == 195 ? "fire" : what == 194 || what == 192 ? "flap"
                    : "throw", .55f);
            if (what == 32 || what == 72 || what == 181) shake = .25f;
            // A word from her as she goes at it, now and then.
            if (sinceAim > 9) {
                sinceAim = 0;
                int who = what / 10;
                String[] says = who == RunLevels.ILLYA ? RunStory.AIM[0] : who == RunLevels.TSUBAKIEH ? RunStory.AIM[1] : who == RunLevels.GOKHAE ? RunStory.AIM_LATER[0]
                        : who == RunLevels.MIRI ? RunStory.AIM_LATER[1] : who == RunLevels.GARI ? RunStory.AIM_LATER[2] : who == RunLevels.YEOBAEK ? RunStory.AIM_LATER[3]
                        : who == RunLevels.MAWANG ? RunStory.AIM_LATER[4] : who >= RunLevels.GUARD && who <= RunLevels.OLD_BOOK ? RunStory.AIM_LATER[5 + who - RunLevels.GUARD]
                        // (Lucy's word is for her water: it is said as she squirts, lobs or rains it, which is when the splash is heard.)
                        : who == RunLevels.LUCY && (what == 391 || what == 393 || what == 394) ? RunStory.LUCY[1] : null;
                if (says != null) remark(new String[][]{says});
            }
        }
        if (run.phases() != seenPhases && boss != null) {
            seenPhases = run.phases();
            // A third of the way: she changes, and says so.
            audio.play("form", .8f);
            flash = .4f;
            shake = .3f;
            buzz(true);
            int which = boss.kind == RunLevels.GARI ? RunLevels.MIRI : boss.kind;
            // (Lucy: her dragon side comes out; and then, where anyone else would change again, she throws the black ribbon.)
            if (which == RunLevels.LUCY) speak(RunStory.LUCY[run.decided() ? 4 : 3], true);
            else speak(RunStory.PHASES[RunStory.waiting(which) * 2 + Math.max(0, Math.min(1, run.phase(boss) - 1))], true);
        }
        // The black ribbon has reached her. Whole, it is turned: the white that is truly hers forms, which is kept from now on, and Lucy
        // has something to say about it, and then she herself about what comes next. Not whole, it lies on her.
        if (run.ribboned() && !seenRibboned) {
            seenRibboned = true;
            flash = .6f;
            shake = .4f;
            buzz(true);
            if (run.whole()) {
                audio.play("star", 1);
                session.keep("run_colours", colours() | 1 << PlusRun.WHITE - 1);
                dye(PlusRun.WHITE);
                talk.add(RunStory.LUCY[5]);
                talk.add(RunStory.LUCY[6]);
            } else {
                audio.play("censor", .9f);
                audio.play("lose", .5f);
            }
        }
        // (How long ago her beam went out, for the picture of her settling after it.)
        if (beamWas && run.beam() <= 0) sinceBeamEnd = 0;
        beamWas = run.beam() > 0;
        sinceBeamEnd += seconds;
        if (run.unlocks() != seenUnlocks) {
            seenUnlocks = run.unlocks();
            sinceOpened = 0;
            audio.play("unlock");
            buzz(true);
        }
        if (run.quakes() != seenQuakes) {
            seenQuakes = run.quakes();
            shake = Math.max(shake, .3f);
        }
        if (run.doorsUsed() != seenDoors) {
            seenDoors = run.doorsUsed();
            audio.play("door", .7f);
        }
        if (run.parried() != seenParried) {
            seenParried = run.parried();
            // A blow wasted on what keeps them off: it rings, flashes, and says so.
            audio.play("parry", .6f);
            audio.play("clang", .5f);
            sinceStruck = 0;
            if (boss != null) {
                float at = boss.x + boss.wide() / 2, high = boss.y + boss.tall() * .5f;
                for (int spark = 0; spark < 8; spark++) {
                    double way = random.nextFloat() * Math.PI * 2;
                    bits.add(new float[]{at + (float) Math.cos(way) * .8f, high + (float) Math.sin(way) * .8f, (float) Math.cos(way) * 5, (float) Math.sin(way) * 5 - 1, .3f, 0});
                }
                pop(at, boss.y - .4f, "Blocked");
            }
            if (sinceParry > 12) {
                sinceParry = 0;
                remark(RunStory.PARRIED);
            }
        }
        if (run.censors() != seenCensors) seenCensors = run.censors();
        if (run.wiped() != seenWiped) {
            seenWiped = run.wiped();
            pop(herX, herY - 1.6f, "× 0");
            buzz(true);
        }
        RunLevels.Level here = run.level();
        if (run.entered() != seenEntered) {
            seenEntered = run.entered();
            session.keep("run_doors", session.tally("run_doors") + 1);
            audio.play("door", .7f);
            buzz(false);
            // The places she has found are kept, land by land.
            int known = session.tally("run_rooms_" + number);
            boolean found = (run.roomsFound() & ~known) != 0;
            session.keep("run_rooms_" + number, known | run.roomsFound());
            int in = run.room();
            if (in >= 0) {
                int kind = here.rooms.get(in)[0];
                boolean todo = (run.roomsDone() & 1 << in) == 0;
                if (BuildConfig.DEBUG && session.tally("run_spot") > 0) {
                    int spot = session.tally("run_spot");
                    run.place(here.rooms.get(in)[3] + spot % 1000, here.rooms.get(in)[11] - spot / 1000 * RunLevels.STOREY - PlusRun.TALL - .05f);
                }
                // Her word about the kind of place it is, the first time in a land; of a cellar or a game, only while it is still to do.
                if ((saidRoom & 1 << kind) == 0 && (kind <= RunLevels.PASSAGE || run.sealed())) {
                    saidRoom |= 1 << kind;
                    aside(RunStory.ROOMS[kind]);
                }
                // And a card that says what it is and what to do in it: before anything in it begins, where something is about to.
                if (kind == RunLevels.HOUSE) {
                    int sort = here.rooms.get(in)[14];
                    toast(sort == RunLevels.BOUTIQUE ? "Tulle & Thread" : LOOK_NAMES[RunLevels.LOOKS + sort], sort == RunLevels.BOUTIQUE ? "Outfits, ears and horns, pictures and Tulle's quests"
                            : sort == RunLevels.PET_SHOP ? "Walk up to a pet to look at it; Nabi keeps the shop"
                            : sort == RunLevels.GUILD_HALL ? "Hunts on the board; the counter at Inju's desk"
                            : sort == RunLevels.SMITHY ? (number >= RunLevels.SISTERS_FROM ? "Moru forges, Pulmu makes armour, Sutdol trades" : "Moru forges, and makes what is worn")
                            : sort == RunLevels.MARKET ? "Harder lands, the black arena, and what tokens buy"
                            : sort == RunLevels.GACHA ? "A ribbon by chance. In here, ribbons' times stand still"
                            : "Somebody is in", 0);
                    audio.play("page", .7f);
                } else if (kind <= RunLevels.PASSAGE) {
                    int[] one = here.rooms.get(in);
                    if (todo || kind == RunLevels.PASSAGE) {
                        toast(LOOK_NAMES[Math.max(0, one[13])] + (kind == RunLevels.STASH ? ": a stash" : ": a way through"), kind == RunLevels.PASSAGE ? "It comes out further on"
                                : one[14] == RunLevels.HOLDS_PET ? "A pet waits in it, by its chest" : one[14] == RunLevels.HOLDS_SHRINE ? "A shrine stands by its chest"
                                : "Find the way through to its chest", 0);
                        audio.play("page", .7f);
                    }
                } else if (todo) {
                    roomCard = in;
                    roomCardAge = 0;
                    tickedAt = 0;
                    audio.play(run.sealed() ? "star" : "page", .7f);
                }
            } else {
                roomCardAge = -1;
            }
            // (A building of a village stands in plain sight: going into one is not finding a place.)
            if (found && !(in >= 0 && here.rooms.get(in)[0] == RunLevels.HOUSE)) {
                // A place found for the first time: ten pluses to keep, said when the card of what it is has gone.
                session.keep("run_bank", bank() + 10);
                toast("A place off the land found", "+ 10 to keep", CHEST_PICTURE);
            }
        }
        if (run.reveals() != seenReveals) {
            seenReveals = run.reveals();
            audio.play("crumble", .8f);
            shake = .2f;
            buzz(true);
            for (int crumb = 0; crumb < 10; crumb++) {
                bits.add(new float[]{herX, herY - .8f, (random.nextFloat() - .5f) * 6, -random.nextFloat() * 4, .5f, 1});
            }
        }
        // A cellar's wave: announced, with a sting and a beat of stillness; its creatures marked, and coming in; cleared, with a breath.
        if (run.wavePhase() != seenPhase) {
            seenPhase = run.wavePhase();
            sincePhase = 0;
            // A card that is up when a wave is announced or cleared steps aside for the banner, and comes back after it.
            if ((seenPhase == PlusRun.ANNOUNCED || seenPhase == PlusRun.CLEARED) && toastLeft > .4f && toastTitle != null) {
                toasts.add(0, new Object[]{toastTitle, toastWords, toastPicture});
                toastLeft = 0;
            }
            if (seenPhase == PlusRun.ANNOUNCED) {
                audio.play("quake", .7f);
                audio.play("star", .6f);
                buzz(true);
                shake = Math.max(shake, .25f);
            } else if (seenPhase == PlusRun.FOUGHT) {
                audio.play("page", .7f);
            } else if (seenPhase == PlusRun.CLEARED) {
                audio.play("unlock", .8f);
                buzz(true);
            }
        }
        sincePhase += seconds;
        // For looking at a cellar from end to end in a build made for testing: every so often the nearest of its wave is dealt with for her.
        if (BuildConfig.DEBUG && session.tally("run_sweep") > 0 && run.wavePhase() == PlusRun.FOUGHT && sincePhase > 2.5f
                && (int) (sincePhase / 1.2f) != (int) ((sincePhase - seconds) / 1.2f)) run.sweep();
        // A chest opened: ten pluses in it. The one by a passage's far door has a purse in it besides, the first time; a stash's is the
        // doing of the stash, and is told with that.
        // (In a build made for testing it can be slowed, to be looked at: run_slow is the hundredths of its pace.)
        chestAge += seconds * (BuildConfig.DEBUG && session.tally("run_slow") > 0 ? session.tally("run_slow") / 100f : 1);
        // Experience from what fell: kept as it comes; a new level is a burst about her and a word from her, and her blows land harder from now.
        if (run.xp() != seenXp) {
            int before = herLevel();
            long gathered = Math.min(0xFFFFFFFFL, herXp() + run.xp() - seenXp);
            seenXp = run.xp();
            session.keep("run_xp", (int) gathered);
            int after = herLevel();
            if (after > before) {
                levelAge = 0;
                run.perks().level = after;
                audio.play("star", 1);
                buzz(true);
                speak(RunStory.LEVEL[random.nextInt(RunStory.LEVEL.length)], true);
                for (int spark = 0; spark < 18; spark++) {
                    double way = random.nextFloat() * Math.PI * 2;
                    bits.add(new float[]{herX, herY - 1, (float) Math.cos(way) * 4, (float) Math.sin(way) * 4 - 2, .9f, 0});
                }
            }
        }
        // Things to keep, picked up: into her bag, and named in a card in the colour of what they are.
        while (seenFound < run.found().size()) {
            int code = run.found().get(seenFound++);
            session.keepNote("run_bag", RunLoot.note(RunLoot.add(RunLoot.bag(session.note("run_bag")), code)));
            int rarity = RunLoot.rarity(code);
            toast(RunLoot.RARITIES[rarity] + ": " + RunLoot.name(code), "In her bag  ·  " + RunLoot.TYPES[RunLoot.type(code)]
                    + (RunLoot.type(code) == RunLoot.WEAPON ? "" : "  ·  open it to wear"), LOOT_PICTURE + code);
            toastRarity = rarity >= RunLoot.GOLD ? 2 : rarity >= RunLoot.BLUE ? 1 : 0;
            audio.play(rarity >= RunLoot.GOLD ? "unlock" : "tap", 1);
            if (rarity == RunLoot.ROSE) speak(RunStory.LEVEL[0], true);
        }
        // A diary page found is read: on a card, for a while, and kept as found.
        if (run.diaries() != seenDiaries) {
            seenDiaries = run.diaries();
            int page = Math.max(0, Math.min(RunStory.DIARY.length - 1, run.lastDiary()));
            session.keep("run_diary", session.tally("run_diary") | 1 << page);
            diaryText = RunStory.DIARY[page];
            diaryNumber = page;
            diaryAge = 0;
            audio.play("page", .8f);
            buzz(false);
        }
        // A loose floorboard comes up: what was under it is there.
        if (run.lifted() != seenLifted) {
            seenLifted = run.lifted();
            audio.play("unlock", .8f);
            buzz(true);
        }
        if (run.chests() != seenChests) {
            seenChests = run.chests();
            audio.play("unlock");
            buzz(true);
            chestAge = 0;
            chestX = run.tookX();
            chestY = run.tookY();
            flyTo((chestX - cameraX) * tile, (chestY - cameraY) * tile, false, 8);
            // (What it held: ten in a plain one; more in a cave's, and more again behind a false wall.)
            pop(herX, herY - 1.6f, "+ " + run.lastChest());
            seenBonus = run.bonus();
            for (int spark = 0; spark < 12; spark++) {
                double way = random.nextFloat() * Math.PI * 2;
                bits.add(new float[]{herX, herY - .6f, (float) Math.cos(way) * 5, (float) Math.sin(way) * 5 - 2, .5f, 0});
            }
            int in = run.room();
            if (in >= 0 && here.rooms.get(in)[0] == RunLevels.PASSAGE && (session.tally("run_chests_" + number) & 1 << in) == 0) {
                session.keep("run_chests_" + number, session.tally("run_chests_" + number) | 1 << in);
                int purse = 20 + 10 * RunLevels.chapterOf(number);
                session.keep("run_bank", bank() + purse);
                toast("The passage's chest: + " + purse + " to keep", "For going the other way", CHEST_PICTURE);
                aside(RunStory.ROOM_WON[4]);
            }
        }
        // What happens in a place off the land is over: a card of how it went and what it gave, and then the way out. What a place gives
        // the first time is kept, whatever becomes of the run: a cellar a life, there and then, and where the Old Book slept a piece of a
        // heart; a game the rare white ribbon, if she did enough; a stash a purse, and a hidden one a piece of a heart.
        // What she deals with counts towards the hunts she has taken on.
        List<int[]> fells = run.fells();
        if (seenFells > fells.size()) seenFells = 0;
        for (; seenFells < fells.size(); seenFells++) {
            int[] one = fells.get(seenFells);
            for (int slot = 0; slot < RunHunts.SLOTS; slot++) if (RunHunts.counts(hunt(slot), one[0], one[1])) hunted(slot);
        }
        if (run.roomWins() != seenWins) {
            seenWins = run.roomWins();
            int in = run.room();
            int[] won = in >= 0 ? here.rooms.get(in) : null;
            int kind = won == null ? -1 : won[0], chapter = RunLevels.chapterOf(number);
            boolean first = in >= 0 && (session.tally("run_roomsdone_" + number) & 1 << in) == 0, enough = run.caught() >= PlusRun.ENOUGH;
            audio.play("unlock");
            audio.play("win", .6f);
            buzz(true);
            seenBonus = run.bonus();
            roomGave = "";
            roomPicture = CHEST_PICTURE;
            roomRank = -1;
            if (kind == RunLevels.ARENA) {
                // How it was fought: the time it took, the hurts she took in it, and a rank from those; and what the rank is worth. The first
                // time it is cleared there is a life, there and then; the best rank, the first time it is had, is a piece of a heart, and the
                // next two are pluses to keep. It can be fought again for a better one.
                int rank = Math.max(0, run.ranked()), best = session.tally("run_rank_" + number) - 1, pays = PlusRun.RANK_PAYS[rank] + (won[10] != 0 ? 20 : 0);
                roomRank = rank;
                for (int slot = 0; slot < RunHunts.SLOTS; slot++) if (RunHunts.countsCellar(hunt(slot), rank)) hunted(slot);
                roomResult = won[10] != 0 ? "The Old Book is shut" : "Cellar cleared";
                roomScore = (run.arenaHurts() == 0 ? "not a hurt" : run.arenaHurts() + (run.arenaHurts() == 1 ? " hurt" : " hurts"))
                        + (run.arenaLives() > 0 ? "  ·  " + run.arenaLives() + (run.arenaLives() == 1 ? " life" : " lives") : "") + "  ·  + " + pays;
                roomPicture = CELLAR_PICTURE;
                if (first) {
                    run.spare();
                    roomGave = "A life, there and then";
                }
                if (rank > best) {
                    session.keep("run_rank_" + number, rank + 1);
                    if (rank == PlusRun.RANK_S) {
                        roomGave += (roomGave.isEmpty() ? "Rank S" : "") + piece();
                    } else if (rank >= PlusRun.RANK_B && best < PlusRun.RANK_B) {
                        int more = rank == PlusRun.RANK_A ? 40 : 20;
                        session.keep("run_bank", bank() + more);
                        roomGave += (roomGave.isEmpty() ? "" : ", ") + "+ " + more + " to keep";
                    }
                }
                if (rank < PlusRun.RANK_S) roomGave += (roomGave.isEmpty() ? "" : ". ") + "Rank S: two hurts at most, and no life lost";
            } else if (kind == RunLevels.STASH) {
                roomResult = "The chest is open";
                roomScore = "+ 10 in it";
                if (first && won[7] != 0) {
                    roomGave = piece().substring(2);
                } else if (first) {
                    int purse = 30 + 10 * chapter;
                    session.keep("run_bank", bank() + purse);
                    roomGave = "A purse: + " + purse + " to keep";
                }
            } else {
                boolean range = kind == RunLevels.RANGE;
                roomResult = "Time!";
                roomScore = (range ? "Targets hit: " : kind == RunLevels.TRAMPLE ? "Flattened: " : "Caught: ") + run.caught() + " of " + PlusRun.ENOUGH
                        + "  ·  + " + (run.caught() * (range ? 2 : 1) + (enough ? 10 : 0));
                roomPicture = -1;
                if (first && enough) {
                    session.keep("run_bank", bank() + 20);
                    roomGave = ribbons() + ", and + 20 to keep";
                } else if (!enough) {
                    roomGave = "Reach " + PlusRun.ENOUGH + " for the prize";
                }
            }
            roomResultAge = 0;
            roomCardAge = -1;
            idle();
            speak(RunStory.ROOM_WON[kind == RunLevels.ARENA ? 0 : kind == RunLevels.RANGE ? 2 : kind == RunLevels.TRAMPLE ? 3 : kind == RunLevels.STASH ? 4 : 1], true);
            session.keep("run_roomsdone_" + number, session.tally("run_roomsdone_" + number) | run.roomsDone());
            regrant();
        }
        // The last seconds of a game tick.
        if (run.room() >= 0 && run.sealed() && run.gameLeft() > 0 && run.gameLeft() <= 3 && (int) Math.ceil(run.gameLeft()) != tickedAt) {
            tickedAt = (int) Math.ceil(run.gameLeft());
            audio.play("tap", .8f);
        }
        // What was asked of her is done, and not yet handed in: it is said at once, and where to go with it.
        if (run.questsDone() != seenDone) {
            seenDone = run.questsDone();
            seenAsked = run.questGiven();
            sinceAsked = 0;
            if (run.quest() == PlusRun.DONE) {
                audio.play("star", .8f);
                buzz(true);
                pop(herX, herY - 2.1f, "Quest done!");
                toast("Quest done: back to " + giverName(here.quest), "She has something for you. Or she will find you at the gate.", questPicture(here.quest));
            }
        }
        // Back with whoever asked it before it is done, she says again what she asked.
        if (run.reminders() != seenReminders) {
            seenReminders = run.reminders();
            if (run.quest() == PlusRun.ASKED) person(RunStory.ASKS[here.quest - 1]);
        }
        // It is handed in: whoever asked it, or whoever it was for, thanks her, in her own voice, however often she has spoken before; and
        // hands over what it pays, which is kept whatever becomes of the run, with a guild seal for the map. The first time in a land that is
        // what that kind of thing pays, and if it was the last thing asked in its chapter, the chapter's medal; after that, fifteen pluses.
        if (run.questsPaid() != seenPaid) {
            seenPaid = run.questsPaid();
            seenBonus = run.bonus();
            sinceAsked = 0;
            int quest = here.quest, chapter = RunLevels.chapterOf(number);
            boolean first = session.tally("run_quest_" + number) == 0;
            handWho = quest == RunLevels.LETTER ? RunLevels.YESEO : RunLevels.giverOf(quest);
            handPicture = SCROLL_PICTURE;
            if (first) {
                session.keep("run_quest_" + number, 1);
                int pays = RunLevels.questPrize(quest);
                if (pays == RunLevels.PURSE) {
                    session.keep("run_bank", bank() + 40 + 20 * chapter);
                    handTitle = "Boksun's purse: + " + (40 + 20 * chapter);
                    handWords = "Pluses to keep, for the guild counter";
                    handPicture = CAKE_PICTURE;
                } else if (pays == RunLevels.SHOTS) {
                    handTitle = "A whetted nib";
                    handWords = "Ink guns hold 2 more shots, for good";
                    handPicture = CELLAR_PICTURE;
                } else if (pays == RunLevels.LASTING) {
                    handTitle = "Yeseo's stamp";
                    handWords = "Treats last a twentieth longer, for good";
                    handPicture = LETTER_PICTURE;
                } else if (pays == RunLevels.QUICKER) {
                    handTitle = "Ttosun's whistle";
                    handWords = "Her colours are ready a quarter of a second sooner, for good";
                    handPicture = WATCH_PICTURE;
                } else {
                    int[] given = RunLevels.ribbonsOf(number);
                    handWords = ribbons() + ", there and then; and + 30 to keep";
                    session.keep("run_bank", bank() + 30);
                    handTitle = given.length > 1 ? "Galpi's ribbons" : "Galpi's ribbon";
                    handPicture = -given[0];
                }
                medal(chapter, true);
                regrant();
            } else {
                handTitle = "Done again: + 15";
                handWords = "And a heart, if she was short of one";
            }
            questLine = "Quest for " + giverName(quest) + ": " + (first ? pays(number) : "+ 15");
            if (handQuiet) {
                // The land was lost with it done and not handed in: it is kept all the same, and the card at the end says so.
                handQuiet = false;
            } else {
                // Where she stands: whoever the letter was for is where it was brought; whoever asked is where she was asked, if that is
                // where Halgakos is; and if it is not, she has come running after her.
                handRuns = false;
                handX = run.questColumn() + 1.55f;
                handY = run.questRow() + 1;
                if (quest == RunLevels.LETTER) {
                    for (int[] item : here.items) {
                        if (item[0] != PlusRun.DROPOFF) continue;
                        handX = item[1] + 1.55f;
                        handY = item[2] + 1;
                    }
                } else if (run.room() >= 0 || Math.abs(herX - handX) > 4.5f || Math.abs(herY - handY) > 2.5f) {
                    handRuns = true;
                    handX = herX + (run.facingLeft() ? 1.5f : -1.5f);
                    handY = herY;
                }
                handAge = 0;
                handSaid = false;
                handStamped = false;
                idle();
                audio.play("win", .7f);
                buzz(true);
                person(RunStory.ASKED[quest - 1]);
            }
        }
        if (run.questFails() != seenFailed) {
            seenFailed = run.questFails();
            sinceAsked = 0;
            audio.play("no", .6f);
            aside(RunStory.UNDONE[0]);
        }
        // Near a crack with a door behind it she has a word about the draught, once.
        for (int index = 0; index < here.rooms.size() && run.room() < 0; index++) {
            int[] one = here.rooms.get(index);
            if (one[7] == 1 && (run.revealed() & 1 << index) == 0 && (hinted & 1 << index) == 0 && Math.abs(run.x() - one[1]) < 2.5f
                    && Math.abs(run.y() + PlusRun.TALL - one[2]) < 2) {
                hinted |= 1 << index;
                aside(RunStory.CRACK[0]);
            }
        }
        // At a stall its keeper calls out the first time she comes up to it, and not again however often she comes back.
        if (run.greetings() != seenGreetings) {
            seenGreetings = run.greetings();
            if (personLeft <= 0) person((number > 60 ? RunStory.RUMOURS : RunStory.MEET)[RunLevels.ALTTEUL - RunLevels.SOMI]);
        }
        // Walking into a way that is shut: it shakes, and she has a word about it.
        if (run.locked() && sinceBump > 1.2f && (goLeft && run.x() <= run.arenaFrom() + 1.02f || goRight && run.x() + PlusRun.WIDE >= run.arenaTo() - .02f)) {
            if (sinceBump > 8 && boss != null && boss.enter <= 0) remark(RunStory.SHUT);
            sinceBump = 0;
            audio.play("parry", .4f);
            buzz(false);
        }
        if (run.swings() != seenSwings) {
            seenSwings = run.swings();
            audio.play("bat", .6f);
        }
        if (run.turned() != seenTurned) {
            seenTurned = run.turned();
            audio.play("clang", .55f);
            buzz(false);
        }
        if (run.springs() != seenSprings) {
            seenSprings = run.springs();
            audio.play("spring");
        }
        if (run.flagsPassed() != seenFlags) {
            seenFlags = run.flagsPassed();
            audio.play("flag");
            if (!saidFlag) {
                saidFlag = true;
                remark(RunStory.FLAG);
            }
        }
        if (run.blows() != seenBlows) {
            seenBlows = run.blows();
            boolean fell = boss != null && (boss.spawned ? boss.flat : !run.standing()), roverFell = rover != null && rover.flat && !roverOver;
            audio.play(fell || roverFell ? "fall" : "blow");
            if (roverFell) {
                roverOver = true;
                roverDone(rover, true);
            }
            buzz(true);
            shake = Math.max(shake, .15f);
            // The last of those who wait here is down: what she gives rises out of her there and then, and is hers; a colour is in her hair at once.
            if (fell && !boss.spawned && prizeAge < 0) {
                int given = prize(number);
                if (given != RunLevels.NOTHING) {
                    prizeWon = given;
                    prizeAge = 0;
                    prizeX = run.foeX();
                    prizeY = run.foeY() - .8f;
                    idle();
                    audio.play("win", .9f);
                    audio.play("form", .9f);
                    if (given > 0) {
                        run.gift(given);
                        if (given != PlusRun.BLACK) {
                            if (chosen == 0) chosen = given;
                            session.keep("run_colour", chosen);
                            if (herMix == 0) herMix = 1 << given - 1;
                        }
                    } else {
                        regrant();
                    }
                }
            }
            // Whoever took it has something to say about it, now and then; for Zero and King Slime, who say nothing, she does.
            if (prizeAge >= 0) {
                speak(RunStory.PRIZES[prizeWon > 0 ? 0 : prizeWon == RunLevels.BOUNTY ? 2 : 1], true);
            } else if (boss != null && (captionLeft <= 0 || fell)) {
                int kind = boss.kind;
                speak(kind == RunLevels.LUCY ? RunStory.LUCY[2] : kind >= RunLevels.GOKHAE ? RunStory.BLOWS_LATER[kind - RunLevels.GOKHAE]
                        : RunStory.BLOWS[kind == RunLevels.ILLYA ? 1 : kind == RunLevels.TSUBAKIEH ? 2 : 0], true);
            }
            puff(12);
        } else if (run.flattened() != seenFlattened) {
            audio.play("squish");
            buzz(false);
            if (random.nextInt(4) == 0) remark(RunStory.SQUISH);
            puff(8);
        }
        seenFlattened = run.flattened();
        if (run.hurts() != seenHurts) {
            seenHurts = run.hurts();
            audio.play("hurt");
            buzz(true);
            knockForm = lookBefore;
            int cause = run.cause();
            boolean life = run.livesLost() != seenLives, fell = cause == PlusRun.FELL || cause == PlusRun.BURNED || cause == PlusRun.SANK;
            seenLives = run.livesLost();
            if (life || fell || run.state() != PlusRun.PLAYING) {
                // Sent somewhere else, she is seen thrown into the air where she was, and falling away; if she fell out of the land, from the foot of the screen.
                knockX = wasX;
                knockY = Math.min(wasY, cameraY + height / tile + .4f);
                knockLeft = wasLeft;
                knockAge = 0;
            } else {
                // Hurt where she stands, she flinches, and goes on.
                flinchAge = 0;
                shake = Math.max(shake, .14f);
            }
            if (run.state() == PlusRun.PLAYING) {
                // A life gone is said and shown; otherwise what she says is about what hurt her; down to her last heart, about that.
                if (life) {
                    audio.play("lose", .5f);
                    toast(run.lives() == 1 ? "Last life" : "A life lost: " + run.lives() + " left", "Back at the flag, with her hearts full", -HER_PICTURE);
                    speak(RunStory.LIFE[run.lives() == 1 ? 1 : 0], true);
                } else if (run.hearts() == 1 && !saidLast) {
                    saidLast = true;
                    speakAny(RunStory.LAST_HEART);
                } else {
                    boolean short_ = cause == PlusRun.FELL && !run.winged() && run.jumps() != jumpsAtFall;
                    jumpsAtFall = run.jumps();
                    if (short_ && (session.tally("run_hint_wings") == 0 || sinceWings > 90 && random.nextInt(3) == 0)) {
                        speak(RunStory.WINGS[session.tally("run_hint_wings") == 0 ? 0 : 1], true);
                        session.keep("run_hint_wings", session.tally("run_hint_wings") + 1);
                        sinceWings = 0;
                    } else {
                        speakAny(cause == PlusRun.FELL ? RunStory.FELL : cause == PlusRun.STRUCK ? RunStory.SHOT
                                : cause == PlusRun.SPIKED ? RunStory.SPIKED : cause == PlusRun.BATTED ? RunStory.BATTED : cause == PlusRun.BURNED ? RunStory.BURNED
                                : cause == PlusRun.SANK ? RunStory.SANK : cause == PlusRun.QUAKED ? RunStory.QUAKED : cause == PlusRun.SLASHED ? RunStory.SLASHED
                                : RunStory.BITTEN);
                    }
                }
            }
        } else if (run.halves() > heartsBefore) {
            pop(herX, herY - 1.6f, "+ ♥");
        }
        for (int index = bits.size() - 1; index >= 0; index--) {
            float[] bit = bits.get(index);
            bit[0] += bit[2] * seconds;
            bit[1] += bit[3] * seconds;
            bit[3] += 9 * seconds;
            if ((bit[4] -= seconds) <= 0) bits.remove(index);
        }
        for (int index = pops.size() - 1; index >= 0; index--) {
            if ((pops.get(index)[2] += seconds) > 1.1f) {
                pops.remove(index);
                popWords.remove(index);
            }
        }
        if ((sinceLook += seconds) > .3f) {
            sinceLook = 0;
            lookAhead();
        }
        // Out of hearts, the card waits until she has fallen away.
        // The land over with something done and not handed in: whoever asked it finds her at the gate, before the card; if the land was
        // lost, it is kept without that, and the card says so.
        if (run.state() != PlusRun.PLAYING && run.quest() == PlusRun.DONE) {
            handQuiet = run.state() == PlusRun.LOST;
            run.settle();
            return;
        }
        if (run.questsPaid() != seenPaid || handAge >= 0) return;
        if (run.state() == PlusRun.WON) {
            if (onward()) clearLand();
            else finish(true);
        } else if (run.through() && onward()) cross();
        else if (run.state() == PlusRun.LOST && knockAge < 0) finish(false);
        else if (run.state() == PlusRun.RISEN && finale == NO_FINALE) takeOff();
        else if (run.state() == PlusRun.UNMADE && finale == NO_FINALE) unmake();
    }

    private void puff(int count) {
        for (int puff = 0; puff < count; puff++) {
            bits.add(new float[]{run.foeX(), run.foeY(), (random.nextFloat() - .5f) * 7, -random.nextFloat() * 3, .36f, 1});
        }
    }

    /**
     * The first time something comes into view, ever, she has a word about it: each of the
     * signs, spikes, a spring, a flag, something to fight with, the gate. One thing at a
     * time, and only when nothing else is being said.
     */
    private void lookAhead() {
        int seen = session.tally("run_seen2"), all = RunStory.SEE.length + RunStory.SEE_LATER.length;
        if (seen == (1 << all) - 1 || captionLeft > 0 || audio.speaking() || run.locked()) return;
        int from = (int) run.x() - 1, to = (int) (run.x() + 6.5f), found = -1;
        for (PlusRun.Foe foe : run.foes()) {
            int kind = foe.kind;
            int which = kind == RunLevels.MINUS ? 0 : kind == RunLevels.TIMES ? 1 : kind == RunLevels.DIVIDE ? 2 : kind == RunLevels.THROWER ? 3
                    : kind == RunLevels.GUNNER ? 4 : kind >= RunLevels.SLIME && kind <= RunLevels.POGO ? 10 + kind - RunLevels.SLIME : -1;
            if (which >= 0 && !foe.flat && foe.x >= from && foe.x <= to && (seen & 1 << which) == 0) found = which;
        }
        for (int[] arm : run.arms()) if (arm[1] >= from && arm[1] <= to && (seen & 1 << 8) == 0) found = 8;
        for (int[] item : run.items()) if (item[0] > PlusRun.FORM && item[0] < PlusRun.CHEST && item[1] >= from && item[1] <= to && (seen & 1 << 24) == 0 && !run.hidden(item[1], item[2])) found = 24;
        for (int index = 0; index < run.level().movers.size(); index++) {
            float x = run.moverX(index, run.time());
            if (x >= from && x <= to && (seen & 1 << 20) == 0) found = 20;
        }
        for (int column = Math.max(0, from); column <= to && found < 0; column++) {
            for (int row = 0; row < RunLevels.ROWS && found < 0; row++) {
                char here = run.at(column, row);
                int which = here == RunLevels.SPIKE ? 5 : here == RunLevels.SPRING ? 6 : here == RunLevels.FLAG ? 7 : here == RunLevels.GATE ? 9
                        : here == RunLevels.CRUMBLE ? 16 : here == RunLevels.BELT_LEFT || here == RunLevels.BELT_RIGHT ? 17 : here == RunLevels.SLICK ? 18
                        : here == RunLevels.DRAUGHT ? 19 : here == RunLevels.DOOR ? 21 : here == RunLevels.LAVA ? 22 : here == RunLevels.WATER ? 23 : -1;
                if (which >= 0 && (seen & 1 << which) == 0) found = which;
            }
        }
        if (found < 0) return;
        session.keep("run_seen2", seen | 1 << found);
        speak(found < RunStory.SEE.length ? RunStory.SEE[found] : RunStory.SEE_LATER[found - RunStory.SEE.length], true);
    }

    // Touches

    void touch(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) keyed = false;
        float x = event.getX(), y = event.getY();
        if (treeOpen) {
            treeTouch(event);
            return;
        }
        if (packOpen) {
            inventory.touch(event);
            return;
        }
        if (huntsOpen) {
            if (action == MotionEvent.ACTION_DOWN) huntsTouch(x, y);
            return;
        }
        if (smithOpen) {
            if (action == MotionEvent.ACTION_DOWN) smithTouch(x, y);
            return;
        }
        if (marketOpen) {
            if (action == MotionEvent.ACTION_DOWN) marketTouch(x, y);
            return;
        }
        if (screen == PLAY) {
            if (action == MotionEvent.ACTION_DOWN && pauseButton.contains(x, y)) {
                audio.play("tap", .5f);
                pause();
                return;
            }
            // In the air over the library there are only the arrows and the beam.
            if (finale != NO_FINALE) {
                held(event);
                return;
            }
            // The buttons over the arrows: one calls on the colour she has chosen, if what she calls on it with has filled; the other chooses the next of hers.
            // While something won is rising, a touch, once it has been seen, sends it on its way.
            // The moment in which something is won from whoever fell is short, and a touch ends it sooner; after it her buttons are hers.
            if (prizeAge >= 0 && prizeAge < 2) {
                if (action == MotionEvent.ACTION_DOWN && prizeAge > .8f) prizeAge = 2;
                return;
            }
            // A card that holds the land is sent on by a touch, once it has been up long enough to be seen.
            if (roomCardAge >= 0 && roomCardAge < 2.4f && roomHolds()) {
                if (action == MotionEvent.ACTION_DOWN && roomCardAge > .5f) roomCardAge = 3.1f;
                return;
            }
            if (roomResultAge >= 0 && roomResultAge < 2.6f) {
                if (action == MotionEvent.ACTION_DOWN && roomResultAge > .8f) roomResultAge = 2.6f;
                return;
            }
            // Someone she has met, tapped where she stands: they talk.
            if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
                    && talkTo(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) {
                return;
            }
            if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && run.door() < 0 && !run.atStall() && run.spot() < 0 && atGate()
                    && stallButton.contains(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) {
                warp();
                return;
            }
            if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && run.door() < 0 && !run.atStall() && run.spot() >= 0
                    && stallButton.contains(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) {
                // In a building: the counter, at the tab of whatever she stands at; a pet on show is the one looked at.
                int what = run.spot();
                audio.play("tap", .5f);
                buzz(false);
                idle();
                if (what == PlusRun.BOARD) {
                    openHunts();
                    return;
                }
                if (what == PlusRun.MACHINE) {
                    crank();
                    return;
                }
                if (what == PlusRun.DEALER) {
                    openMarket();
                    return;
                }
                if (what == PlusRun.BOUTIQUE) {
                    openBoutique(0);
                    return;
                }
                if (what >= PlusRun.SMITH) {
                    openSmith(what - PlusRun.SMITH, PLAY);
                    return;
                }
                counterFrom = PLAY;
                counterTab = what < 100 ? PETS_TAB : what - 100;
                if (what < 100) petOn = what;
                screen = SHOP;
                speak(RunStory.SHOP[0], false);
                return;
            }
            if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && run.door() < 0 && !run.atStall() && run.petOffer() >= 0
                    && stallButton.contains(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) {
                run.swapPet();
                buzz(true);
                return;
            }
            // Her active slots: a touch calls one on.
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int slot = slotAt(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
                if (slot >= 0) {
                    callSlot(slot);
                    held(event);
                    return;
                }
            }
            // The button that changes what she holds: a tap gives her the other of the last two; held, it opens her backpack.
            {
                int index = event.getActionIndex(), finger = event.getPointerId(index);
                boolean pressed = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN;
                boolean let = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL;
                if (pressed && run.nextUp() != PlusRun.UNARMED && swapButton.contains(event.getX(index), event.getY(index))) {
                    swapFinger = finger;
                    swapHeld = 0;
                    return;
                }
                if (let && finger == swapFinger) {
                    swapFinger = -1;
                    run.swap();
                    buzz(false);
                }
            }
            if (perks.colours != 0 && colourTouch(event, action)) return;
            // At a door, the same button uses it: in, out, or through the crack to it.
            if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && !run.atStall() && run.door() >= 0
                    && stallButton.contains(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) {
                idle();
                run.enter();
                return;
            }
            // At a stall, its button opens it, and so does a tap on the sign over its keeper; the land waits.
            if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && run.atStall()
                    && (stallButton.contains(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))
                    || stallSign.contains(event.getX(event.getActionIndex()), event.getY(event.getActionIndex())))) {
                audio.play("tap", .5f);
                buzz(false);
                idle();
                screen = STALL;
                pills("Back to the land", RESUME, "Guild counter", COUNTER);
                return;
            }
            doubleTap(event, action);
            held(event);
            return;
        }
        if (screen == SHOP) {
            shopTouch(action, x, y);
            return;
        }
        if (action != MotionEvent.ACTION_DOWN) return;
        if (screen == STORY) {
            // A touch turns the page.
            if (++line >= telling.length) {
                endStory();
            } else {
                speak(telling[line], false);
                audio.play("page", .5f);
            }
        } else if (screen == MAP && warping) {
            if (closeButton.contains(x, y)) {
                audio.play("tap", .5f);
                warpTo(0);
            } else if (volumes() > 1 && (tabOf(0).contains(x, y) || tabOf(1).contains(x, y) || volumes() > 2 && tabOf(2).contains(x, y))) {
                audio.play("page", .5f);
                turnTo(tabOf(0).contains(x, y) ? 0 : tabOf(1).contains(x, y) ? 1 : 2);
            } else {
                for (int index = firstOf(page); index < lastOf(page); index++) {
                    if (lands[index].contains(x, y) && index + 1 <= open()) {
                        buzz(false);
                        warpTo(index + 1);
                        return;
                    }
                }
            }
        } else if (screen == MAP) {
            if (closeButton.contains(x, y)) {
                audio.play("tap", .5f);
                stage.done();
            } else if (soundButton.contains(x, y)) {
                audio.set(!audio.on());
                names.set(CompanionPrefs.GAME_SOUND, audio.on());
                audio.play("tap", .5f);
            } else if (shopButton.contains(x, y)) {
                audio.play("tap", .5f);
                buzz(false);
                counterFrom = MAP;
                screen = SHOP;
                speak(RunStory.SHOP[0], false);
            } else if (cringeButton.contains(x, y)) {
                audio.play("tap", .5f);
                buzz(false);
                openGallery();
            } else if (gearButton.contains(x, y)) {
                audio.play("tap", .5f);
                buzz(false);
                openPack(MAP);
            } else if (skillButton.contains(x, y)) {
                buzz(false);
                openTree(MAP);
            } else if (goButton.contains(x, y)) {
                // On with the furthest land she has reached.
                audio.play("tap", .5f);
                buzz(false);
                play(open());
            } else if (volumes() > 1 && (tabOf(0).contains(x, y) || tabOf(1).contains(x, y) || volumes() > 2 && tabOf(2).contains(x, y))) {
                audio.play("page", .5f);
                turnTo(tabOf(0).contains(x, y) ? 0 : tabOf(1).contains(x, y) ? 1 : 2);
            } else {
                int first = firstOf(page), last = lastOf(page);
                for (int index = first; index < last; index++) {
                    if (lands[index].contains(x, y) && index + 1 <= open()) {
                        audio.play("tap", .5f);
                        buzz(false);
                        play(index + 1);
                        return;
                    }
                }
            }
        } else if (screen == STALL) {
            for (int index = 0; index < stallWares.length; index++) {
                if (!stallWares[index].contains(x, y)) continue;
                // What she has collected in this land buys it, and it is hers at once.
                int kind = index < 3 ? STALL_SELLS[index] : PlusRun.FORM + STALL_FORMS[number % STALL_FORMS.length];
                int why = run.refusal(kind, STALL_COSTS[index]);
                boolean sold = run.buy(kind, STALL_COSTS[index]);
                audio.play(sold ? "buy" : "no", .6f);
                buzz(sold);
                speak(RunStory.STALL[sold ? 0 : why == PlusRun.SOLD_OUT ? 2 : why == PlusRun.WORN ? 3 : why == PlusRun.NEEDLESS ? 4 : 1], true);
                if (sold) {
                    seenTaken = run.taken();
                    seenFormed = run.formed();
                }
                return;
            }
            for (int index = 0; index < pillCount; index++) {
                if (pills[index].contains(x, y)) {
                    audio.play("tap", .5f);
                    carryOut(pillActions[index]);
                    return;
                }
            }
        } else {
            if (screen == PAUSED && packButton.contains(x, y)) {
                audio.play("tap", .5f);
                openPack(PAUSED);
                return;
            }
            if (screen == PAUSED && skillPause.contains(x, y)) {
                openTree(PAUSED);
                return;
            }
            for (int index = 0; index < pillCount; index++) {
                if (pills[index].contains(x, y)) {
                    act(pillActions[index]);
                    return;
                }
            }
        }
    }

    /** What must be so before the next step of something is for sale, in a few words; or null, if it is. */
    private String locked(int which) {
        int has = bought(which);
        if (which == TRIPLE && bought(1) == 0) return "Needs her wings first";
        if (which != PRISM || has >= PRISM_NEEDS.length) return null;
        int owned = Integer.bitCount(colours() & 127);
        return owned >= PRISM_NEEDS[has] ? null : PRISM_NEEDS[has] == 7 ? "Needs all seven colours: " + owned + " of 7 won" : "Needs " + PRISM_NEEDS[has] + " colours won: "
                + owned + " so far";
    }

    /**
     * The pet that is looked at at the counter: one she has not got is bought, if her
     * pluses stretch to it, and comes along if there is a place for it; one she has comes
     * along, if there is a place, or is left at home.
     */
    private void petDeal() {
        int own = session.tally("run_pet_own"), with = session.tally("run_pet_with") & own, bit = 1 << petOn, places = 1 + bought(PET_PLACES);
        if ((own & bit) == 0) {
            if (bank() < PET_COSTS[petOn]) {
                audio.play("no", .5f);
                if (refused <= 0) speak(RunStory.SHOP[2], false);
                refused = 5;
                return;
            }
            session.keep("run_bank", bank() - PET_COSTS[petOn]);
            session.keep("run_pet_own", own | bit);
            if (Integer.bitCount(with) < places) session.keep("run_pet_with", with | bit);
            session.keep("run_pets", session.tally("run_pets") | bit);
            audio.play("buy", .6f);
            buzz(true);
            speak(RunStory.SHOP[1], false);
        } else if ((with & bit) != 0) {
            session.keep("run_pet_with", with & ~bit);
            audio.play("tap", .5f);
        } else if (Integer.bitCount(with) < places) {
            session.keep("run_pet_with", with | bit);
            audio.play("star", .6f);
        } else {
            audio.play("no", .5f);
        }
    }

    /** The pet that is looked at is had for treats, if she has as many as it takes. */
    private void petTrade() {
        int own = session.tally("run_pet_own"), with = session.tally("run_pet_with") & own, bit = 1 << petOn, treats = session.tally("run_treats");
        if ((own & bit) != 0) return;
        if (treats < PET_TREATS[petOn]) {
            audio.play("no", .5f);
            return;
        }
        session.keep("run_treats", treats - PET_TREATS[petOn]);
        session.keep("run_pet_own", own | bit);
        if (Integer.bitCount(with) < 1 + bought(PET_PLACES)) session.keep("run_pet_with", with | bit);
        session.keep("run_pets", session.tally("run_pets") | bit);
        audio.play("buy", .6f);
        buzz(true);
        speak(RunStory.SHOP[1], false);
    }

    /** Buys the next step of something, if her pluses stretch to it. */
    private void buy(int which) {
        int has = bought(which);
        if (has >= UP_COSTS[which].length) return;
        if (locked(which) != null) {
            audio.play("no", .5f);
            return;
        }
        int cost = UP_COSTS[which][has];
        if (bank() < cost) {
            audio.play("no", .5f);
            if (refused <= 0) speak(RunStory.SHOP[2], false);
            refused = 5;
            return;
        }
        session.keep("run_bank", bank() - cost);
        session.keep("run_up_" + which, has + 1);
        audio.play("buy", .6f);
        buzz(true);
        speak(RunStory.SHOP[1], false);
    }

    /** The picker opens, and the land waits: with her mix ticked, and whatever of hers she is wearing besides, for as far as she can hold them. */
    private void openPicker(boolean stays) {
        glowPalette = false;
        int owned = perks.colours & 127;
        if ((herMix & owned) == 0) herMix = 1 << chosen - 1;
        fanOpen = true;
        pickerStays = stays;
        fanOver = -1;
        int wearing = run.mix() & owned;
        ticks = PlusRun.most(wearing | herMix & owned, wearing, run.together());
        // Colours are ticked on and off with one another unless one at a time is asked for.
        oneAtATime = false;
        audio.play("page", .5f);
        buzz(true);
    }

    /** What is ticked is worn, all at once, and is her mix from now on. */
    private void wearTicks() {
        int owned = perks.colours & 127;
        fanOpen = false;
        ribbonFinger = -1;
        idle();
        if ((ticks & owned) == 0) return;
        herMix = ticks & owned;
        session.keep("run_mix", herMix);
        chosen = Integer.numberOfTrailingZeros(herMix) + 1;
        session.keep("run_colour", chosen);
        int waiting = herMix & ~run.ready() & ~run.mix();
        if (run.wear(herMix) == 0 && (run.mix() & herMix) == 0) {
            audio.play("no", .4f);
            pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, waiting != 0 ? "Still waiting" : "Not now");
        }
    }

    /**
     * A touch on the ribbon, the palette beside it, or the picker they open. The palette
     * opens the picker, which stays open with no finger on it: in it each colour that is
     * hers is tapped on or off, several at once; the quick choices over them tick a whole
     * mix in one tap; "Wear" puts on what is ticked, all at once, and makes it her mix; and
     * a tap outside it closes it and does nothing. The ribbon, let go of at once, calls on
     * her mix: every colour of it that is ready, passing over any still waiting. Held for a
     * third of a second it opens the same picker for as long as the finger is down: sliding
     * onto a colour ticks or unticks it, and letting go wears what is ticked. Another
     * finger may be on an arrow or the jump all the while. Returns whether the touch was
     * one of these and is done with.
     */
    private boolean colourTouch(MotionEvent event, int action) {
        int index = event.getActionIndex(), finger = event.getPointerId(index);
        float px = event.getX(index), py = event.getY(index);
        boolean down = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN;
        boolean up = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL;
        if (fanOpen) {
            if (down) {
                // A tap: on a quick choice, on a colour, on the button, or outside, which closes it.
                for (int chip = 0; chip < chips.size(); chip++) {
                    if (!chips.get(chip).contains(px, py)) continue;
                    choose(chipMixes.get(chip));
                    return true;
                }
                int over = cellAt(px, py);
                if (over >= 0) {
                    pick(over);
                } else if (wearButton.contains(px, py)) {
                    audio.play("tap", .5f);
                    wearTicks();
                } else if (pickerStays && !pickerCard.contains(px, py)) {
                    fanOpen = false;
                    idle();
                    audio.play("tap", .3f);
                }
                return true;
            }
            if (!pickerStays) {
                // Opened by a hold: the finger that holds it ticks what it slides onto, and letting go wears what is ticked.
                for (int each = 0; each < event.getPointerCount(); each++) {
                    if (event.getPointerId(each) != ribbonFinger) continue;
                    int over = cellAt(event.getX(each), event.getY(each));
                    if (over != fanOver) {
                        fanOver = over;
                        if (over >= 0) pick(over);
                    }
                }
                if (up && (finger == ribbonFinger || action != MotionEvent.ACTION_POINTER_UP)) wearTicks();
            }
            return true;
        }
        if (down && nextButton.contains(px, py)) {
            if (Integer.bitCount(perks.colours & 127) > 1) openPicker(true);
            return true;
        }
        if (down && colourButton.contains(px, py)) {
            ribbonFinger = finger;
            ribbonHeld = 0;
            return true;
        }
        if (up && finger == ribbonFinger) {
            // Let go of before the picker opened: a tap, which calls on her mix, with whatever she wears.
            ribbonFinger = -1;
            int owned = perks.colours & 127, mix = herMix & owned;
            if (mix == 0) mix = 1 << chosen - 1;
            if (run.callAll(mix) == 0) {
                audio.play("no", .4f);
                int first = Integer.numberOfTrailingZeros(mix & ~run.mix()) + 1;
                pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, (run.mix() & mix) == mix ? "Already on" : run.cursed() > 0 ? "Not under black"
                        : first <= PlusRun.BLACK && run.untilCall(first) > 0 ? "Ready in " + (int) Math.ceil(run.untilCall(first)) + " s"
                        : "No more than " + run.together() + " at once");
            }
            return action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL;
        }
        return false;
    }

    /** Which of the colours opened out a place on the screen is over, or -1. */
    private int cellAt(float x, float y) {
        for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) {
            if (fanCells[colour] != null && (perks.colours & 1 << colour - 1) != 0 && fanCells[colour].contains(x, y)) return colour;
        }
        return -1;
    }

    /** One of the colours in the picker is ticked or unticked: one at a time, it alone; otherwise with the rest, if there is room for it. */
    private void pick(int colour) {
        int bit = 1 << colour - 1;
        if (oneAtATime) {
            ticks = bit;
        } else if ((ticks & bit) != 0) {
            ticks &= ~bit;
        } else if (Integer.bitCount(ticks) >= run.together()) {
            audio.play("no", .3f);
            return;
        } else {
            ticks |= bit;
        }
        audio.play("tap", .5f);
        buzz(false);
    }

    /** A quick choice is taken: a named mix, as many as she can wear, or one colour at a time. */
    private void choose(int which) {
        int owned = perks.colours & 127;
        if (which == TRUE_WHITE) {
            // The white that is truly hers: called on at once, and the picker is done with.
            fanOpen = false;
            ribbonFinger = -1;
            idle();
            if (run.callWhite()) {
                dye(PlusRun.WHITE);
                flash = .5f;
                audio.play("star", 1);
                buzz(true);
            }
            return;
        }
        if (which == ONE_AT_A_TIME) {
            oneAtATime = true;
            ticks = Integer.lowestOneBit(ticks != 0 ? ticks : herMix & owned);
        } else if (which == ALL_I_CAN) {
            oneAtATime = false;
            ticks = PlusRun.most(owned, run.ready() | run.mix() & owned, run.together());
        } else {
            oneAtATime = false;
            // The prism is any three: the three that are readiest.
            ticks = which == PlusRun.PRISM ? PlusRun.most(owned, run.ready() | run.mix() & owned, 3) : PlusRun.mixOf(which);
        }
        audio.play("tap", .5f);
        buzz(false);
    }

    /** The way last tapped (-1 left, 1 right) and when: a second tap of the same way soon after is a dash in the air, or a roll on the ground. */
    private int tappedWay;
    private float tappedAt = -9;

    private void doubleTap(MotionEvent event, int action) {
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_POINTER_DOWN || run == null) return;
        float x = event.getX(event.getActionIndex()), y = event.getY(event.getActionIndex());
        if (y < height * .45f) return;
        float between = (leftButton.right + rightButton.left) / 2, arrows = Math.min(rightButton.right + dp(26), (rightButton.right + strikeButton.left) / 2);
        int way = x < between ? -1 : x < arrows ? 1 : 0;
        if (way == 0) return;
        if (way == tappedWay && clock - tappedAt < .3f) {
            tappedAt = -9;
            if (run.dash()) audio.play("jump", .5f);
            return;
        }
        tappedWay = way;
        tappedAt = clock;
    }

    /** Which buttons are held, by however many fingers are down: one may hold an arrow while another jumps. */
    private void held(MotionEvent event) {
        int action = event.getActionMasked();
        boolean left = false, right = false, up = false, strike = false, down = false, breath = false;
        boolean carrying = finale == FLIGHT || finale == RIDE || run != null && (run.weapon() != PlusRun.UNARMED || run.has(PlusRun.RED) || run.has(PlusRun.BLUE));
        // (Held, it is held: the beam is gathered by holding it.)
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
            float low = height * .45f, between = (leftButton.right + rightButton.left) / 2;
            float arrows = Math.min(rightButton.right + dp(26), (rightButton.right + strikeButton.left) / 2);
            float jumps = (strikeButton.right + jumpButton.left) / 2;
            for (int finger = 0; finger < event.getPointerCount(); finger++) {
                if (action == MotionEvent.ACTION_POINTER_UP && finger == event.getActionIndex()) continue;
                float x = event.getX(finger), y = event.getY(finger);
                // The buttons are easy to hit: the whole lower part of the screen belongs to one or another of them.
                // A finger on one of the buttons over the arrows or over the jump is on that button, not on what is under it.
                if (perks.colours != 0 && (colourButton.contains(x, y) || nextButton.contains(x, y))) continue;
                if (run != null && (run.atStall() || run.door() >= 0 || run.petOffer() >= 0 || run.spot() >= 0) && stallButton.contains(x, y)) continue;
                if (run != null && run.nextUp() != PlusRun.UNARMED && swapButton.contains(x, y)) continue;
                if (slotAt(x, y) >= 0) continue;
                // (On Lucy's back, the button over the jump is her breath.)
                if (finale == RIDE && x > stallButton.left - dp(10) && x < stallButton.right + dp(10) && y > stallButton.top - dp(10) && y < stallButton.bottom + dp(6)) {
                    breath = true;
                    continue;
                }
                if (x > downButton.left - dp(8) && x < downButton.right + dp(8) && y > downButton.top - dp(10) && y < downButton.bottom + dp(4)) {
                    down = true;
                    continue;
                }
                if (y < low) continue;
                if (x < between) left = true;
                else if (x < arrows) right = true;
                else if (carrying && x < jumps) strike = true;
                else up = true;
            }
        }
        touchLeft = left;
        touchRight = right;
        touchJump = up;
        touchStrike = strike;
        touchDown = down;
        goBreath = breath;
        together();
    }

    /** What she does: what the fingers hold and what the keys hold, either; a jump or a strike begun by either is pressed. */
    private void together() {
        boolean up = touchJump || keys.held(RunKeys.JUMP), strike = touchStrike || keys.held(RunKeys.STRIKE | RunKeys.SPECIAL);
        if (up && !goJump) jumpPressed = true;
        if (strike && !goStrike) strikePressed = true;
        goLeft = touchLeft || keys.held(RunKeys.LEFT);
        goRight = touchRight || keys.held(RunKeys.RIGHT);
        goJump = up;
        goStrike = strike;
        goDown = touchDown || keys.held(RunKeys.DOWN);
    }

    /**
     * A key, down or up. In a land: held keys are added to what the buttons hold; Q or Tab changes what she holds; E is the button over the
     * jump; S or Down at a door goes through it. On a card: Enter, Space or J says yes to its first choice; on a story page, turns it. Esc
     * is Back, which the stage gives as Back. Whether the key was one of the game's.
     */
    boolean key(int keyCode, boolean down) {
        int does = RunKeys.of(keyCode);
        if (does == 0 || does == RunKeys.BACK) return false;
        int fresh = keys.press(keyCode, down);
        if (down) keyed = true;
        boolean sheet = packOpen || bagOpen || huntsOpen || smithOpen || marketOpen || fanOpen || treeOpen;
        if (packOpen) {
            inventory.key(fresh, keyCode);
            return true;
        }
        if (screen == PLAY && run != null && !sheet) {
            // (Once the story being told as the land begins is over: said over it, it would not be heard.)
            if (fresh != 0 && number == 1 && session.tally("run_keys_said") == 0) keysHintDue = true;
            if ((fresh & RunKeys.SWAP) != 0 && run.nextUp() != PlusRun.UNARMED) {
                run.swap();
                buzz(false);
            }
            if ((fresh & RunKeys.EXTRA) != 0) overJump();
            if ((fresh & RunKeys.DASH) != 0 && run.dash()) audio.play("jump", .5f);
            for (int slot = 0; slot < PlusRun.SLOTS; slot++) if ((fresh & RunKeys.SKILL_1 << slot) != 0) callSlot(slot);
            if ((fresh & RunKeys.DOWN) != 0 && !run.atStall() && run.door() >= 0) {
                idle();
                run.enter();
            }
            // (A card that holds the land, or something won rising, is sent on by a key as by a touch.)
            if (fresh != 0) {
                if (prizeAge >= 0 && prizeAge < 2 && prizeAge > .8f) prizeAge = 2;
                if (roomCardAge >= 0 && roomCardAge < 2.4f && roomHolds() && roomCardAge > .5f) roomCardAge = 3.1f;
                if (roomResultAge >= 0 && roomResultAge < 2.6f && roomResultAge > .8f) roomResultAge = 2.6f;
            }
            together();
            return true;
        }
        together();
        if ((fresh & (RunKeys.CONFIRM | RunKeys.JUMP | RunKeys.STRIKE)) == 0 || sheet) return true;
        if (screen == STORY) {
            if (++line >= telling.length) {
                endStory();
            } else {
                speak(telling[line], false);
                audio.play("page", .5f);
            }
        } else if ((screen == PAUSED || screen == RESULT || screen == DELVE) && pillCount > 0 && (fresh & RunKeys.CONFIRM) != 0) {
            act(pillActions[0]);
        } else if (screen == STALL && pillCount > 0 && (fresh & RunKeys.CONFIRM) != 0) {
            carryOut(pillActions[0]);
        }
        return true;
    }

    /** The button over the jump, used by its key: a gate's warp, a building's counter, a pet to swap, a door, a stall. */
    private void overJump() {
        if (run.door() < 0 && !run.atStall() && run.spot() < 0 && atGate()) {
            warp();
        } else if (run.door() < 0 && !run.atStall() && run.spot() >= 0) {
            int what = run.spot();
            audio.play("tap", .5f);
            idle();
            if (what == PlusRun.BOARD) openHunts();
            else if (what == PlusRun.MACHINE) crank();
            else if (what == PlusRun.DEALER) openMarket();
            else if (what == PlusRun.BOUTIQUE) openBoutique(0);
            else if (what == PlusRun.STAIRS) openStairs();
            else if (what >= PlusRun.SMITH) openSmith(what - PlusRun.SMITH, PLAY);
            else {
                counterFrom = PLAY;
                counterTab = what < 100 ? PETS_TAB : what - 100;
                if (what < 100) petOn = what;
                screen = SHOP;
                speak(RunStory.SHOP[0], false);
            }
        } else if (run.door() < 0 && !run.atStall() && run.petOffer() >= 0) {
            run.swapPet();
            buzz(true);
        } else if (!run.atStall() && run.door() >= 0) {
            idle();
            run.enter();
        } else if (run.atStall()) {
            audio.play("tap", .5f);
            idle();
            screen = STALL;
            pills("Back to the land", RESUME, "Guild counter", COUNTER);
        }
    }

    /** The mouse wheel: in her bag, the next page or the one before. */
    boolean wheel(float turned) {
        if (treeOpen && turned != 0) {
            tree().wheel(turned, width / 2f, height / 2f);
            return true;
        }
        if (screen == SHOP && !packOpen && !bagOpen && turned != 0 && shopMax > 0) {
            // At the counter, the wheel scrolls its list.
            shopScroll = Math.max(0, Math.min(shopMax, shopScroll - turned * dp(64)));
            return true;
        }
        if (packOpen && turned != 0) {
            inventory.wheel(turned);
            return true;
        }
        return false;
    }

    /** Everything let go: the window has gone from the front, or the land is left. */
    void letGo() {
        keys.clear();
        together();
    }

    // Drawing

    void draw(Canvas canvas) {
        if (width == 0) return;
        if (run != null && screen == PLAY) perks.item = RunInventory.itemFor(perks, run.weapon());
        if (BuildConfig.DEBUG && run != null && screen == PLAY) {
            long now = System.nanoTime();
            frames++;
            if (now - framesFrom > 2_000_000_000L) {
                int there = 0;
                for (PlusRun.Foe foe : run.foes()) if (foe.spawned && !foe.flat) there++;
                if (framesFrom != 0) android.util.Log.i("RunFps", "fps " + Math.round(frames * 1e9f / (now - framesFrom)) + " there " + there + " foes " + run.foes().size() + " left " + run.waveLeft());
                framesFrom = now;
                frames = 0;
            }
        }
        if (screen == MAP) {
            drawMap(canvas);
        } else if (screen == SHOP) {
            drawShop(canvas);
        } else if (screen == STORY) {
            // The story is told over the map or over the land, whichever was there.
            if (storyOver == MAP || run == null) {
                drawMap(canvas);
            } else {
                drawLand(canvas);
                if (finale == NO_FINALE) drawHud(canvas);
            }
            drawStory(canvas);
        } else if (finale != NO_FINALE) {
            // The end of the last land: the sky over the library, or the land unmade; what is held in it; and its cards.
            drawLand(canvas);
            if (screen == PLAY) {
                flightControls(canvas);
            } else {
                canvas.drawColor(0x8C000000);
                if (screen == PAUSED) {
                    packButton.setEmpty();
                    card(canvas, "Paused", RunStory.CHAPTERS[RunLevels.chapterOf(number)] + ", land " + number + (finale == UNMADE ? "" : "  ·  the sky over it"), 0);
                } else {
                    drawResult(canvas);
                }
            }
        } else {
            drawLand(canvas);
            drawDepthsOver(canvas);
            drawInked(canvas);
            drawWayOn(canvas);
            drawDiary(canvas);
            // A moment in which something is won or handed over lies over the land, and under what is said and shown of how she stands.
            if (run.beam() > 0) drawBeam(canvas);
            if (blastAge < .45f) {
                // A book has burst: a ring of light out to as far as it reaches, and a white heart to it.
                float out = blastAge / .45f, at = (blastX - cameraX) * tile, high = (blastY - cameraY) * tile;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFFFF3C4);
                paint.setAlpha(Math.round(170 * (1 - out)));
                canvas.drawCircle(at, high, tile * run.blastWide() * (.35f + .65f * out), paint);
                paint.setColor(Color.WHITE);
                paint.setAlpha(Math.round(230 * (1 - out)));
                canvas.drawCircle(at, high, tile * run.blastWide() * .4f * (1 - out * .5f), paint);
                stroke.setColor(0xFFE0A21A);
                stroke.setAlpha(Math.round(255 * (1 - out)));
                stroke.setStrokeWidth(tile * .1f);
                canvas.drawCircle(at, high, tile * run.blastWide() * out, stroke);
                stroke.setAlpha(255);
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
            }
            drawSmithWork(canvas);
            drawArenaArms(canvas);
            if (screen == PLAY && prizeAge >= 0) drawPrize(canvas);
            if (screen == PLAY && handAge >= 0) drawHandOver(canvas);
            // With the lights out, the dark lies over the place and under everything that is said and shown of how she stands.
            if (run.dark()) drawDark(canvas);
            if (darkened > 0) canvas.drawColor(Color.argb(Math.round(150 * Math.min(1, darkened / .3f)), 12, 8, 20));
            drawHud(canvas);
            if (screen == PLAY && run.wavePhase() != 0) drawWave(canvas);
            if (screen == PLAY) drawRover(canvas);
            drawFlights(canvas, screen == PLAY ? 1 / 60f : 0);
            if (screen == PLAY && (roomCardAge >= 0 || roomResultAge >= 0)) drawRoomCards(canvas);
            if (screen == PLAY) {
                // (While keys are used, the buttons are all but gone; a touch brings them back.)
                if (keyed) canvas.saveLayerAlpha(0, 0, width, height, 60);
                // Charmed, the arrows are the wrong way round, and show it.
                boolean charmed = run.charmed() > 0;
                control(canvas, leftButton, goLeft, 180, charmed);
                control(canvas, rightButton, goRight, 0, charmed);
                control(canvas, jumpButton, goJump, 270, false);
                // The one that lets her down: lit where it can do something, on a thin ledge or in the air; dim, in its place, where it cannot.
                boolean lets = run.droppable() || !run.onGround();
                // (Where it can do nothing and she stands behind it, as at the start of a land, it is all but gone.)
                float herAt = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herFeet = (run.y() + PlusRun.TALL - cameraY) * tile;
                boolean over = herAt > downButton.left - tile * .6f && herAt < downButton.right + tile * .6f && herFeet > downButton.top - tile * .2f
                        && herFeet - tile * 1.7f < downButton.bottom;
                if (!lets) canvas.saveLayerAlpha(downButton.left - dp(6), downButton.top - dp(6), downButton.right + dp(6), downButton.bottom + dp(8), over ? 36 : 105);
                control(canvas, downButton, goDown, 90, false);
                if (!lets) canvas.restore();
                // In the first land the arrows glow until she has moved, and the jump until she has jumped; and the palette, when she has
                // been told of it, until she opens it.
                if (number == 1 && !hintMoved) {
                    glow(canvas, leftButton);
                    glow(canvas, rightButton);
                } else if (number == 1 && !hintJumped) {
                    glow(canvas, jumpButton);
                }
                if (glowPalette && perks.colours != 0 && !fanOpen) glow(canvas, nextButton);
                if (perks.colours != 0) colourControl(canvas);
                if (perks.colours != 0 && run.cursed() > 0) {
                    // Under black her ribbon and her palette are barred, and the ribbon says for how much longer.
                    barred(canvas, colourButton);
                    barred(canvas, nextButton);
                    text.setTextAlign(Paint.Align.CENTER);
                    text.setColor(Color.WHITE);
                    text.setTextSize(colourButton.height() * .3f);
                    canvas.drawText((int) Math.ceil(run.cursed()) + " s", colourButton.centerX(), colourButton.centerY() + colourButton.height() * .46f, text);
                }
                if (run.weapon() != PlusRun.UNARMED || run.has(PlusRun.RED) || run.has(PlusRun.BLUE)) strikeControl(canvas);
                if (run.nextUp() != PlusRun.UNARMED) swapControl(canvas);
                if (run.atStall()) {
                    stallControl(canvas);
                } else {
                    stallSign.setEmpty();
                    // (No button for a crack not yet broken open: nothing shows it is there but a hairline, close by.)
                    if (run.door() >= 0 && !(run.room() < 0 && run.crackHidden(run.door()))) doorControl(canvas);
                    else if (atGate()) {
                        button(canvas, stallButton, false, 0xFF5B4BC4);
                        paint.setAlpha(255);
                        artIn(canvas, STAR_ART, stallButton.centerX(), stallButton.centerY() - dp(7), stallButton.width() * .42f);
                        label(canvas, stallButton, "Warp");
                    }
                    else if (run.spot() >= 0) {
                        int what = run.spot();
                        button(canvas, stallButton, false, 0xFF8A5A33);
                        paint.setAlpha(255);
                        if (what < 100) cell(canvas, petArt(what), 0, 6, stallButton.centerX(), stallButton.centerY() + stallButton.height() * .16f, stallButton.height() * .62f, false);
                        else if (what == PlusRun.BOARD) pieceIn(canvas, four, SCROLL_PICTURE, stallButton.centerX(), stallButton.centerY() - dp(7), stallButton.width() * .5f, 0);
                        else if (what == PlusRun.MACHINE) pieceIn(canvas, two, RIBBON_ART, stallButton.centerX(), stallButton.centerY() - dp(7), stallButton.width() * .5f, 0);
                        else if (what == PlusRun.SMITH) thing(canvas, PlusRun.SWORD, stallButton.centerX(), stallButton.centerY() - dp(6), stallButton.height() * .5f, 35, false, false);
                        else if (what == PlusRun.SMITH + 1) gearIcon(canvas, RunGear.HELM, stallButton.centerX(), stallButton.centerY() - dp(8), stallButton.width() * .36f, SET_COLOURS[2]);
                        else if (what == PlusRun.SMITH + 2) gearIcon(canvas, RunGear.CHARM, stallButton.centerX(), stallButton.centerY() - dp(7), stallButton.width() * .36f, 0xFFE0A21A);
                        else if (what == PlusRun.STAIRS) stairsIn(canvas, stallButton.centerX(), stallButton.centerY() - dp(7), stallButton.width() * .52f);
                        else artIn(canvas, STAR_ART, stallButton.centerX(), stallButton.centerY() - dp(7), stallButton.width() * .42f);
                        label(canvas, stallButton, what < 100 ? ((session.tally("run_pet_own") >> what & 1) != 0 ? "Pets" : "Buy") : what == PlusRun.BOARD ? "Hunts" : what == PlusRun.SMITH ? "Forge" : what == PlusRun.SMITH + 1 ? "Armour"
                                : what == PlusRun.SMITH + 2 ? "Trade" : what == PlusRun.STAIRS ? "Go down" : what == PlusRun.DEALER ? "Market" : what == PlusRun.BOUTIQUE ? "Boutique" : what == PlusRun.MACHINE ? (run.canRoll() ? "+ " + run.gachaCost() : "Roll")
                                : what == 100 + PETS_TAB ? "Pets" : "Counter");
                    } else if (run.petOffer() >= 0) {
                        button(canvas, stallButton, false, 0xFF8A5A33);
                        paint.setAlpha(255);
                        cell(canvas, petArt(run.petOffer()), 0, 6, stallButton.centerX(), stallButton.centerY() + stallButton.height() * .16f, stallButton.height() * .62f, false);
                        label(canvas, stallButton, "Swap");
                    }
                }
                if (keyed) canvas.restore();
            } else if (screen == STALL) {
                canvas.drawColor(0x8C000000);
                drawStall(canvas);
            } else {
                canvas.drawColor(0x8C000000);
                if (screen == PAUSED) {
                    // (With her backpack to look into.)
                    float inside = card(canvas, "Paused", (delve >= 0 ? RunDungeons.NAMES[delve] + ", floor " + (delveFloor + 1) + " of " + RunDungeons.floors(delve)
                            : blackRung > 0 ? "The black arena, rung " + blackRung : RunStory.CHAPTERS[RunLevels.chapterOf(number)] + ", land " + number + "  ·  level " + RunLevels.levelOf(number))
                            + (hard() > 0 ? "  ·  harder by " + hard() : "") + (run != null && run.inked() ? "  ·  under black " + (int) Math.ceil(run.cursed()) + " s" : ""), dp(run != null && run.inked() ? 112 : 52));
                    // Black, and what it does to her, named.
                    if (run != null && run.inked()) {
                        text.setTextAlign(Paint.Align.LEFT);
                        text.setColor(0xFF6A4A8A);
                        float line = inside + dp(60);
                        for (String name : PlusRun.CURSE_NAMES) {
                            fitted(canvas, "\u2022 " + name, box.left + dp(20), line, dp(11), box.width() - dp(40));
                            line += dp(13);
                        }
                    }
                    packButton.set(box.left + dp(20), inside, box.right - dp(20) - dp(104), inside + dp(42));
                    skillPause.set(packButton.right + dp(8), inside, box.right - dp(20), inside + dp(42));
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(dark ? 0xFF33373B : 0xFFF1F3F4);
                    canvas.drawRoundRect(skillPause, dp(21), dp(21), paint);
                    tree().icon(canvas, RunSkills.SEED_ICON, skillPause.left + dp(22), skillPause.centerY(), dp(32), 255);
                    text.setColor(ink);
                    text.setTextAlign(Paint.Align.LEFT);
                    fitted(canvas, "Skills  \u203A", skillPause.left + dp(40), skillPause.centerY() + dp(5.5f), dp(15.5f), skillPause.width() - dp(46));
                    if (skillsLeft() > 0) {
                        paint.setColor(CalculatorLayout.CORAL);
                        canvas.drawCircle(skillPause.right - dp(8), skillPause.top + dp(7), dp(5), paint);
                    }
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(dark ? 0xFF33373B : 0xFFF1F3F4);
                    canvas.drawRoundRect(packButton, dp(21), dp(21), paint);
                    paint.setColor(Color.BLACK);
                    thing(canvas, run.weapon() == PlusRun.UNARMED ? PlusRun.TABLET : run.weapon(), packButton.left + dp(26), packButton.centerY(), dp(26), 0, false, false);
                    text.setColor(ink);
                    text.setTextAlign(Paint.Align.LEFT);
                    fitted(canvas, "Inventory  \u203A", packButton.left + dp(50), packButton.centerY() + dp(5.5f), dp(15.5f), packButton.width() - dp(64));
                } else {
                    drawResult(canvas);
                }
            }
        }
        // Her backpack, over whatever is there.
        if (packOpen) inventory.draw(canvas);
        if (huntsOpen) drawHunts(canvas);
        if (smithOpen) drawSmithy(canvas);
        if (marketOpen) drawMarket(canvas);
        if (treeOpen) drawTree(canvas);
        // A toast that comes while one of those is open: over it, at the top.
        if (run != null && screen == PLAY && overlaid()) drawToastCard(canvas, safe.top + dp(8));
    }

    /**
     * Something won, named in a small bright card at the top left, under whatever is there already: it slides in, stays a moment,
     * and goes by itself. What it is flies to the card from where she won it. Its rim says how much of a find it is: plain, gold, or
     * violet with a sparkle. Returns how far down what comes under it goes.
     */
    private float drawToastCard(Canvas canvas, float under) {
        if (toastLeft <= 0 || toastTitle == null) return under;
        float margin = dp(10);
            float fade = Math.min(1, toastLeft / .35f), in = Math.min(1, (TOAST - toastLeft) / .22f), high = dp(46);
            text.setTextSize(dp(14));
            float title = text.measureText(toastTitle);
            text.setTextSize(dp(11.5f));
            float wide = Math.min(safe.width() - margin * 2 - (run.pet() >= 0 ? dp(50) : 0), Math.max(title, text.measureText(toastWords)) + dp(66)), from = safe.left + margin - (1 - in) * dp(46);
            int rim = toastPicture >= LOOT_PICTURE ? rarityColour(RunLoot.rarity(toastPicture - LOOT_PICTURE)) : toastRarity == 2 ? 0xFF8E5BE8 : toastRarity == 1 ? 0xFFE0A21A : 0xFFB9BEC2;
            canvas.saveLayerAlpha(from - dp(8), under - dp(8), from + wide + dp(8), under + high + dp(10), Math.round(255 * fade));
            box.set(from, under, from + wide, under + high);
            plate(canvas, box, dp(15), 248);
            stroke.setColor(rim);
            stroke.setStrokeWidth(dp(toastRarity == 0 ? 1.5f : 2.5f));
            canvas.drawRoundRect(from, under, from + wide, under + high, dp(15), dp(15), stroke);
            // What it is: flying in from where it was won for the first moment, then at rest at the head of the card.
            float iconX = from + dp(25), iconY = under + high / 2, fly = Math.min(1, (TOAST - toastLeft) / .45f), eased = fly * fly * (3 - 2 * fly);
            float wonX = (toastX - cameraX) * tile, wonY = (toastY - cameraY) * tile;
            float atX = wonX + (iconX - wonX) * eased, atY = wonY + (iconY - wonY) * eased - (float) Math.sin(eased * Math.PI) * dp(46);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(dp(14));
            text.setColor(ink);
            fitted(canvas, toastTitle, from + dp(50), under + dp(19), dp(14), wide - dp(60));
            text.setColor(soft);
            fitted(canvas, toastWords, from + dp(50), under + dp(36), dp(11.5f), wide - dp(60));
            if (toastRarity == 2) {
                // A sparkle along its top.
                for (int star = 0; star < 3; star++) {
                    float twinkle = .5f + .5f * (float) Math.sin(clock * 9 + star * 2.1f);
                    paint.setAlpha(Math.round(255 * twinkle));
                    art(canvas, STAR_ART, from + wide * (.3f + .28f * star), under + dp(3), dp(9) + dp(4) * twinkle, false);
                }
                paint.setAlpha(255);
            }
            canvas.restore();
            paint.setAlpha(Math.round(255 * fade));
            float size = dp(34) * (1 + .5f * (float) Math.sin(eased * Math.PI));
            if (toastPicture >= LOOT_PICTURE) {
                box.set(atX - size * .55f, atY - size * .55f, atX + size * .55f, atY + size * .55f);
                lootFrame(canvas, toastPicture - LOOT_PICTURE, new RectF(box), false);
            } else if (toastPicture == -HER_PICTURE) round(canvas, 0, atX, atY, size * .5f);
            else if (toastPicture < 0) prizePicture(canvas, -toastPicture, atX, atY, size);
            else pieceIn(canvas, four, toastPicture, atX, atY, size, 0);
            paint.setAlpha(255);
            under += high + dp(6);
        return under;
    }

    /** Whether a card is open over the land, under which a toast would be hidden: the smithy, the backpack, the bag, the hunts, the market, the tree. */
    private boolean overlaid() { return smithOpen || packOpen || bagOpen || huntsOpen || marketOpen || treeOpen; }

    // Creatures' shields

    /**
     * The shield a creature carries, held up on the side it faces, with a bar over it of how much of it is left; shaking as a blow
     * meets it. A blow that rang off: a spark and the words "Too weak". A shield broken: a burst of ink where it was.
     */
    private void drawShields(Canvas canvas, int first, int last) {
        if (run.clangs() != seenClangs) {
            if (run.clangs() > seenClangs) {
                clangAge = 0;
                audio.play("clang", .7f);
            }
            seenClangs = run.clangs();
        }
        if (run.shieldsBroken() != seenBroken) {
            if (run.shieldsBroken() > seenBroken) {
                shatterAge = 0;
                audio.play("crumble", .9f);
                buzz(true);
            }
            seenBroken = run.shieldsBroken();
        }
        if (mobShields == null) mobShields = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_mob_shields);
        for (PlusRun.Foe foe : run.foes()) {
            if (foe.guard <= 0 || foe.flat || foe.x + foe.wide() < first - 1 || foe.x > last + 1) continue;
            float middle = (foe.x + foe.wide() / 2 - cameraX) * tile, feet = (foe.y + foe.tall() - cameraY) * tile, tall = Math.max(tile * .62f, foe.tall() * tile * .78f);
            float shake = foe.guardMet < .2f ? (float) Math.sin(foe.guardMet * 90) * tile * .05f : 0;
            float sx = middle + (foe.leftward ? -1 : 1) * foe.wide() * tile * .42f + shake, sy = feet - foe.tall() * tile * .45f;
            if (mobShields != null && foe.guardKind >= 0) {
                int cell = mobShields.getHeight();
                source.set(foe.guardKind * cell, 0, (foe.guardKind + 1) * cell, cell);
                target.set(sx - tall / 2, sy - tall / 2, sx + tall / 2, sy + tall / 2);
                paint.setAlpha(255);
                canvas.drawBitmap(mobShields, source, target, paint);
            }
            // How much of it is left: a pip for each strong blow it still takes.
            float pip = tile * .1f, gap = tile * .04f, row = (foe.y - cameraY) * tile - tile * .42f, wide = foe.guardMost * pip + (foe.guardMost - 1) * gap;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xCC15171A);
            canvas.drawRoundRect(middle - wide / 2 - tile * .04f, row - pip / 2 - tile * .04f, middle + wide / 2 + tile * .04f, row + pip / 2 + tile * .04f, pip, pip, paint);
            for (int one = 0; one < foe.guardMost; one++) {
                paint.setColor(one < foe.guard ? (foe.guardKind >= 0 ? SHIELD_PIPS[foe.guardKind] : 0xFFE8C77A) : 0xFF4A4F54);
                float px = middle - wide / 2 + one * (pip + gap);
                canvas.drawRect(px, row - pip / 2, px + pip, row + pip / 2, paint);
            }
            paint.setColor(Color.BLACK);
        }
        // A blow too weak: a spark where it rang off, and the words.
        if (clangAge < .7f) {
            float x = (run.clangX() - cameraX) * tile, y = (run.clangY() - cameraY) * tile, through = clangAge / .7f;
            glowAt(canvas, x, y, tile * (.5f + through * .4f), 0xFFFFE9A8, Math.round(230 * (1 - through)));
            stroke.setColor(0xFFFFE9A8);
            stroke.setAlpha(Math.round(255 * (1 - through)));
            stroke.setStrokeWidth(tile * .04f);
            for (int ray = 0; ray < 6; ray++) {
                double angle = ray * Math.PI / 3 + clangAge * 2;
                float from = tile * (.12f + through * .2f), to = tile * (.3f + through * .3f);
                canvas.drawLine(x + (float) Math.cos(angle) * from, y + (float) Math.sin(angle) * from, x + (float) Math.cos(angle) * to, y + (float) Math.sin(angle) * to, stroke);
            }
            stroke.setAlpha(255);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(tile * .34f);
            text.setColor(0xFF1F2328);
            text.setAlpha(Math.round(255 * (1 - through)));
            canvas.drawText("Too weak!", x + tile * .03f, y - tile * (.55f + through * .5f) + tile * .03f, text);
            text.setColor(0xFFFFE9A8);
            text.setAlpha(Math.round(255 * (1 - through)));
            canvas.drawText("Too weak!", x, y - tile * (.55f + through * .5f), text);
            text.setAlpha(255);
            text.setTypeface(Typeface.DEFAULT);
        }
        // A shield broken: ink bursting out from where it was.
        if (shatterAge < .6f) {
            float x = (run.clangX() - cameraX) * tile, y = (run.clangY() - cameraY) * tile, through = shatterAge / .6f;
            glowAt(canvas, x, y, tile * (.6f + through), 0xFF2A1A40, Math.round(220 * (1 - through)));
            paint.setStyle(Paint.Style.FILL);
            for (int drop = 0; drop < 9; drop++) {
                double angle = drop * Math.PI * 2 / 9 + 0.4;
                float far = tile * (.15f + through * .9f), size = tile * .09f * (1 - through * .6f);
                paint.setColor(drop % 3 == 0 ? 0xFF7A4FC0 : 0xFF1F1530);
                paint.setAlpha(Math.round(255 * (1 - through)));
                canvas.drawCircle(x + (float) Math.cos(angle) * far, y + (float) Math.sin(angle) * far + through * through * tile * .4f, size, paint);
            }
            paint.setAlpha(255);
            paint.setColor(Color.BLACK);
        }
    }

    /**
     * A unique creature: walking (two pictures by turns), winding up (lit red-gold and pulsing, so that it is seen coming), striking,
     * hurt, and beaten (its last picture, fading). A mimic lies still as its first picture until it springs; a burrower under the
     * ground is a mound; a wall-ghost faded is a faint shape. A healer's mending is a ring of green light about it.
     */
    private void drawUnique(Canvas canvas, PlusRun.Foe foe, float middle, float feet, boolean turned) {
        if (uniqueSheet == null) uniqueSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_uniques);
        if (uniqueSheet == null) return;
        int way = RunUniques.way(foe.kind);
        if (foe.flat && foe.act > 1.1f) return;
        if (run.heals() != seenHeals) {
            if (run.heals() > seenHeals) healAge = 0;
            seenHeals = run.heals();
        }
        int frame = foe.flat ? 5 : foe.mode == 1 ? 2 : foe.mode == 2 ? 3 : foe.struck < .2f ? 4
                : way == RunUniques.MIMIC && foe.turn == 0 ? 0 : way == RunUniques.HERMIT && foe.turn == 1 ? 2 : (int) (clock * 5 + foe.x) % 2;
        float tall = Math.max(foe.tall(), foe.wide() * .9f) * tile * 1.4f;
        if (way == RunUniques.BURROWER && foe.unseen && !foe.flat) {
            // Under the ground: a mound of earth moving along.
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF5A4630);
            canvas.drawOval(middle - tile * .4f, feet - tile * .18f, middle + tile * .4f, feet + tile * .05f, paint);
            paint.setColor(0xFF7A6240);
            canvas.drawOval(middle - tile * .25f, feet - tile * .26f, middle + tile * .2f, feet - tile * .08f, paint);
            paint.setColor(Color.BLACK);
            return;
        }
        if (foe.mode == 1 && !foe.flat) {
            float pulse = .5f + .5f * (float) Math.sin(clock * 22);
            glowAt(canvas, middle, feet - tall * .4f, tall * (.7f + .15f * pulse), 0xFFFF6A3A, Math.round(140 + 100 * pulse));
        }
        if (way == RunUniques.HEALER && healAge < .8f && foe.mode >= 2) {
            float through = healAge / .8f;
            stroke.setColor(0xFF6AF09A);
            stroke.setAlpha(Math.round(220 * (1 - through)));
            stroke.setStrokeWidth(tile * .08f);
            canvas.drawCircle(middle, feet - tall * .3f, tile * 4.5f * (.3f + .7f * through), stroke);
            stroke.setAlpha(255);
        }
        paint.setAlpha(foe.flat ? Math.round(255 * Math.max(0, Math.min(1, (1.1f - foe.act) / .4f))) : way == RunUniques.GHOST && foe.unseen ? 60
                : foe.dazed > 0 && (int) (clock * 16) % 2 == 0 ? 150 : 255);
        // (Those of the dungeons are in a sheet of their own; and those of the later chapters in the run's asset pack.)
        boolean later = RunUniques.later(foe.kind), delving = !later && RunUniques.index(foe.kind) >= RunUniques.SHEET_ROWS;
        if (delving && delverSheet == null) delverSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_delvers);
        if (later && laterSheet == null && !laterTried) {
            laterTried = true;
            try (java.io.InputStream in = context.getAssets().open(RunUniques.LATER_SHEET)) {
                laterSheet = BitmapFactory.decodeStream(in);
            } catch (java.io.IOException ignored) {
                laterSheet = null;
            }
        }
        Bitmap sheet = later ? laterSheet : delving ? delverSheet : uniqueSheet;
        if (sheet == null) {
            paint.setAlpha(255);
            return;
        }
        int rows = later ? RunUniques.LATER_COUNT : delving ? RunUniques.LATER_FIRST - RunUniques.DELVER_FIRST : RunUniques.SHEET_ROWS;
        int cell = sheet.getHeight() / rows, row = RunUniques.index(foe.kind) - (later ? RunUniques.index(RunUniques.LATER_FIRST) : delving ? RunUniques.SHEET_ROWS : 0);
        if (!foe.flat) laterStrikes(canvas, foe, way, middle, feet);
        if (way == RunUniques.DROPPER && foe.turn == 0 && !foe.flat) {
            // The spider's thread, up out of sight.
            stroke.setColor(0xCCE8E4F0);
            stroke.setStrokeWidth(Math.max(1, tile * .03f));
            canvas.drawLine(middle, Math.min(0, feet - tile * 8), middle, feet - tall * .7f, stroke);
        }
        source.set(frame * cell, row * cell, (frame + 1) * cell, (row + 1) * cell);
        canvas.save();
        if (turned) canvas.scale(-1, 1, middle, feet);
        target.set(middle - tall / 2, feet - tall + tile * .04f, middle + tall / 2, feet + tile * .04f);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
        paint.setAlpha(255);
        if (way == RunUniques.ORBITER && foe.did == 0 && !foe.flat) {
            // The wisp's three quills, going round it.
            for (int one = 0; one < 3; one++) {
                float[] at = PlusRun.quill(foe, one);
                float qx = (at[0] - cameraX) * tile, qy = (at[1] - cameraY) * tile;
                thing(canvas, PlusRun.PEN, qx, qy, tile * .55f, (float) Math.toDegrees(foe.clock * 2.4f + one * Math.PI * 2 / 3) + 90, false, false);
            }
        }
    }

    private Bitmap laterSheet;
    private boolean laterTried;

    /**
     * What the later chapters' creatures do, drawn about them: the strikethrough's line, red as it is raised and bold as it strikes; the
     * serpent's lash and the toad's tongue; the sand-glass's slow time; the sentinel's beam, faint as it gathers and bright as it sweeps.
     */
    private void laterStrikes(Canvas canvas, PlusRun.Foe foe, int way, float middle, float feet) {
        float dir = foe.leftward ? -1 : 1, mid = (foe.y + foe.tall() * .5f - cameraY) * tile, front = middle + dir * foe.wide() * .45f * tile;
        if (way == RunUniques.LINER && foe.mode >= 1 && foe.mode <= 2) {
            boolean striking = foe.mode == 2;
            stroke.setColor(striking ? 0xFFFF2A3A : 0xAAFF5A6A);
            stroke.setStrokeWidth(tile * (striking ? .16f : .05f));
            if (!striking) stroke.setPathEffect(new android.graphics.DashPathEffect(new float[]{tile * .3f, tile * .2f}, clock * tile * 2));
            canvas.drawLine(front, mid, front + dir * PlusRun.LINE * tile, mid, stroke);
            stroke.setPathEffect(null);
        } else if ((way == RunUniques.LASHER || way == RunUniques.PULLER) && foe.mode == 2) {
            float reach = way == RunUniques.LASHER ? PlusRun.LASH : PlusRun.TONGUE, through = (float) Math.sin(Math.min(1, 1 - foe.act / .45f) * Math.PI);
            stroke.setColor(way == RunUniques.LASHER ? 0xFF4C7BE8 : 0xFFF4EEDF);
            stroke.setStrokeWidth(tile * (way == RunUniques.LASHER ? .14f : .12f));
            stroke.setStrokeCap(Paint.Cap.ROUND);
            float y = way == RunUniques.LASHER ? (foe.y + foe.tall() * .7f - cameraY) * tile : mid;
            canvas.drawLine(front, y, front + dir * reach * tile * Math.max(.15f, through), y, stroke);
            stroke.setStrokeCap(Paint.Cap.BUTT);
        } else if (way == RunUniques.HOURGLASS) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFFD27A);
            paint.setAlpha(Math.round(28 + 14 * (float) Math.sin(clock * 3)));
            canvas.drawCircle(middle, mid, PlusRun.SAND_REACH * tile, paint);
            stroke.setColor(0x66FFC44A);
            stroke.setStrokeWidth(tile * .04f);
            canvas.drawCircle(middle, mid, PlusRun.SAND_REACH * tile, stroke);
            paint.setAlpha(255);
        } else if (way == RunUniques.SWEEPER && foe.mode >= 1 && foe.mode <= 2) {
            float[] from = PlusRun.lantern(foe);
            float lx = (from[0] - cameraX) * tile, ly = (from[1] - cameraY) * tile, angle = PlusRun.beamAngle(foe), reach = PlusRun.SWEEP_REACH * tile;
            path.reset();
            path.moveTo(lx, ly);
            for (float side = -.09f; side <= .091f; side += .045f) {
                path.lineTo(lx + dir * (float) Math.cos(angle + side) * reach, ly - (float) Math.sin(angle + side) * reach);
            }
            path.close();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFFF1A8);
            paint.setAlpha(foe.mode == 2 ? 150 : 45);
            canvas.drawPath(path, paint);
            paint.setAlpha(255);
        }
    }

    /** The pips of a shield's bar, by which shield it is. */
    private static final int[] SHIELD_PIPS = {0xFFC08A4A, 0xFFB9C2CC, 0xFFFFC433, 0xFF9A6AE8};

    // Her skill tree

    private RunTree tree() {
        if (tree == null) tree = new RunTree(context.getResources(), dp(1));
        return tree;
    }

    /** What is taken, as it is kept: anything no longer joined to the hidden root let go. */
    private java.util.BitSet skillsTaken() {
        redrawTree();
        return RunSkills.whole(RunSkills.taken(session.note("run_skills")));
    }

    /**
     * The tree was redrawn: the first time a save is read since, what was taken in the old tree is let go, and every point her level has
     * earned is hers to spend again; the tree says so the next time it is open, and she says so on the map. Whether the first
     * respec is free is as it was.
     */
    private void redrawTree() {
        int seen = session.tally(RunSkills.TREE_VERSION_KEY);
        if (seen != 0) return;
        session.keep(RunSkills.TREE_VERSION_KEY, 1);
        if (!RunSkills.fromBefore(seen, session.note("run_skills"))) return;
        session.keepNote("run_skills", "");
        session.keepNote("run_skill_slots", "");
        session.keep("run_tree_notice", 1);
    }

    private int[] skillSlots() { return RunSkills.slots(session.note("run_skill_slots")); }

    private int skillPoints() { return RunSkills.pointsFor(herLevel()); }

    private int skillsLeft() { return skillPoints() - RunSkills.spent(skillsTaken()); }

    /** The first time every point is taken back, it is free. */
    private boolean freeRespec() { return session.tally("run_respecs") == 0; }

    private void openTree(int from) {
        treeOpen = true;
        treeFrom = from;
        readTree();
        tree().open(width, height, treeFacts.taken);
        goLeft = goRight = goJump = goStrike = false;
        jumpPressed = strikePressed = false;
        audio.play("page", .6f);
    }

    /** The facts the tree shows, read again. */
    private void readTree() {
        RunTree.Facts facts = treeFacts;
        facts.taken = skillsTaken();
        facts.slots = skillSlots();
        facts.points = skillPoints();
        facts.level = herLevel();
        facts.bank = bank();
        facts.respecCost = RunSkills.respecCost(facts.taken);
        facts.freeRespec = freeRespec();
        facts.built = RunSkills.built(facts.taken);
        facts.hearts = kept().hearts;
        facts.redrawn = session.tally("run_tree_notice") == 1;
    }

    /** What is taken has changed: felt at once in a land. */
    private void skillsChanged() {
        perks = kept();
        if (run != null) run.learn(perks.skills, perks.slotted);
        readTree();
    }

    private void treeTouch(MotionEvent event) {
        int asked = tree().touch(event), id = tree.picked();
        if (asked == RunTree.CLOSE) {
            treeOpen = false;
            // (Said once: closed, the note that the tree was redrawn is done with.)
            if (session.tally("run_tree_notice") == 1) session.keep("run_tree_notice", 2);
            audio.play("tap", .3f);
        } else if (asked == RunTree.TAKE) {
            java.util.BitSet taken = skillsTaken();
            if (!RunSkills.canTake(taken, id) || skillPoints() - RunSkills.spent(taken) <= 0) {
                audio.play("no", .5f);
                return;
            }
            taken.set(id);
            session.keepNote("run_skills", RunSkills.note(taken));
            session.keepNote("run_skill_slots", RunSkills.slotsNote(RunSkills.slotIn(skillSlots(), taken, id)));
            skillsChanged();
            audio.play("unlock", .8f);
            buzz(true);
        } else if (asked == RunTree.SLOT) {
            // Into the slot (moved there if it was in another, swapping with what was there); in it already, it is taken out.
            int[] slots = skillSlots();
            int slot = tree.pickedSlot();
            if (slots[slot] == id) {
                slots[slot] = 0;
            } else {
                for (int other = 0; other < slots.length; other++) if (slots[other] == id) slots[other] = slots[slot];
                slots[slot] = id;
            }
            session.keepNote("run_skill_slots", RunSkills.slotsNote(slots));
            skillsChanged();
            audio.play("tap", .6f);
            buzz(false);
        } else if (asked == RunTree.RESPEC) {
            java.util.BitSet taken = skillsTaken();
            if (taken.isEmpty()) return;
            int cost = RunSkills.respecCost(taken);
            if (!freeRespec()) {
                if (bank() < cost) {
                    audio.play("no", .5f);
                    return;
                }
                session.keep("run_bank", bank() - cost);
            }
            session.keep("run_respecs", session.tally("run_respecs") + 1);
            session.keepNote("run_skills", "");
            session.keepNote("run_skill_slots", "");
            skillsChanged();
            audio.play("unlock", .8f);
            speak(RunStory.SKILLS[2], false);
        }
    }

    private void drawTree(Canvas canvas) { tree().draw(canvas, safe, treeFacts); }

    /** Where each filled slot is: in order, packed toward the button that changes what she holds. */
    private void placeSlots() {
        int filled = 0;
        for (int slot = 0; slot < PlusRun.SLOTS; slot++) if (run != null && run.active(slot) != null) filled++;
        int at = PlusRun.SLOTS - filled;
        for (int slot = 0; slot < PlusRun.SLOTS; slot++) {
            if (run != null && run.active(slot) != null) slotButtons[slot].set(slotPlaces[at++]);
            else slotButtons[slot].setEmpty();
        }
    }

    /** Her active slots on the HUD: each its own picture; dark while it rests, ringed in gold while it is called on. */
    private void drawSlots(Canvas canvas) {
        placeSlots();
        for (int slot = 0; slot < PlusRun.SLOTS; slot++) {
            RunSkills.Notable skill = run.active(slot);
            if (skill == null) continue;
            RectF where = slotButtons[slot];
            float rest = run.activeRest(slot), on = run.activeLeft(slot);
            boolean ready = rest <= 0 && run.canSpend(PlusRun.ACTIVE_COST);
            if (on > 0) glowAt(canvas, where.centerX(), where.centerY(), where.width() * .85f, 0xFFFFD24A, 200);
            plate(canvas, where, where.height() / 2, 235);
            tree().icon(canvas, skill.icon, where.centerX(), where.centerY(), where.width() * .94f, ready || on > 0 ? 255 : 110);
            if (rest > 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0x88000000);
                canvas.drawArc(where, -90, 360 * rest / PlusRun.ACTIVE_REST, true, paint);
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(Color.WHITE);
                fitted(canvas, String.valueOf((int) Math.ceil(rest)), where.centerX(), where.centerY() + dp(5), dp(14), where.width());
            }
            if (on > 0) {
                stroke.setColor(0xFFFFD24A);
                stroke.setStrokeWidth(dp(3));
                canvas.drawArc(where.left + dp(1.5f), where.top + dp(1.5f), where.right - dp(1.5f), where.bottom - dp(1.5f), -90, 360 * on / PlusRun.ACTIVE_LASTS, false, stroke);
            }
            if (keyed) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF1F2328);
                canvas.drawCircle(where.left + dp(6), where.top + dp(6), dp(8), paint);
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(Color.WHITE);
                fitted(canvas, String.valueOf(slot + 1), where.left + dp(6), where.top + dp(10), dp(11), dp(14));
            }
            paint.setColor(Color.BLACK);
        }
    }

    /** Which active slot is at a point, or -1. */
    private int slotAt(float x, float y) {
        if (run == null) return -1;
        placeSlots();
        for (int slot = 0; slot < PlusRun.SLOTS; slot++) if (run.active(slot) != null && slotButtons[slot].contains(x, y)) return slot;
        return -1;
    }

    /** An active slot used, by touch or by its key. */
    private void callSlot(int slot) {
        if (run == null || run.active(slot) == null) return;
        if (run.callOn(slot)) {
            audio.play("unlock", .7f);
            buzz(true);
        } else {
            audio.play("no", .4f);
        }
    }

    /** Divine power, beside her energy: a bar of gold filling as energy spent becomes it; full, it shines for as long as her blows count three times. */
    private void drawDivine(Canvas canvas, float left, float top) {
        float wide = dp(96), high = dp(22), part = run.divineLeft() > 0 ? run.divineLeft() / PlusRun.DIVINE_LASTS : run.dp() / PlusRun.DP_MOST;
        box.set(left, top, left + wide, top + high);
        if (run.divineLeft() > 0) glowAt(canvas, box.centerX(), box.centerY(), wide * .7f, 0xFFFFD24A, Math.round(150 + 80 * (float) Math.sin(clock * 10)));
        plate(canvas, box, high / 2, 235);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(0xFFC9961A);
        fitted(canvas, run.divineLeft() > 0 ? "×3" : "DP", left + dp(7), top + high / 2 + dp(4), dp(11), dp(18));
        float from = left + dp(28), to = left + wide - dp(9), by = top + high / 2;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
        canvas.drawRoundRect(from, by - dp(3), to, by + dp(3), dp(3), dp(3), paint);
        paint.setColor(0xFFFFC433);
        if (part > 0) canvas.drawRoundRect(from, by - dp(3), from + (to - from) * Math.min(1, part), by + dp(3), dp(3), dp(3), paint);
        paint.setColor(Color.BLACK);
    }

    // Her inventory (her backpack and her bag as one): see RunInventory and RunInventoryUi

    /** Her inventory is opened: from a land, where the land waits; from the pause; or from the map. */
    private void openPack(int from) {
        packOpen = true;
        packFrom = from;
        swapFinger = -1;
        inventory.open(from != MAP && run != null);
        goLeft = goRight = goJump = goStrike = false;
        jumpPressed = strikePressed = false;
        audio.play("page", .6f);
    }

    /** Her inventory is closed, or sent on to the smithy's account of what she wears. */
    void closePack() {
        packOpen = false;
        audio.play("tap", .3f);
    }

    void openSmithOfWorn() { openSmith(RunGear.PULMU, MAP); }

    /** Her bag is her inventory now: opened from where either was. */
    private void openBag(int from) { openPack(from); }

    /** The weapon found that she has taken in hand, or 0; kept as the number of the thing. */
    private int handCode() { return RunInventory.hand(session); }

    /** What she wears, as it is kept. */
    RunLoot.Worn wornNow() { return RunLoot.worn(session.note("run_worn")); }

    /** What she wears is changed: kept, and felt at once in a land. */
    void wearNow(RunLoot.Worn worn) {
        session.keepNote("run_worn", RunLoot.note(worn));
        perks = kept();
        if (run != null) run.wear(worn);
        audio.play("unlock", .8f);
        buzz(false);
    }

    /**
     * The main or the secondary weapon is changed (kept already): what she keeps is made again, and in a land she is told of it. The weapon given
     * as {@code hold} (an entry of RunInventory) is put in her hands at once, if she can hold it.
     */
    void weaponsChanged(int hold) {
        perks = kept();
        if (run != null) {
            run.grant(perks);
            if (hold != 0) {
                if (RunInventory.plain(hold)) run.switchTo(RunInventory.kindOf(hold));
                else run.hold(hold);
            }
        }
        audio.play("unlock", .8f);
        buzz(true);
    }

    /** What her backpack holds, as bits of kinds: in a land what she has there; on the map what is kept. */
    int packedKinds(boolean inLand) { return !inLand || run == null ? kept().pack | 1 << kept().main : run.owned(); }

    /** A figure as it is written: without a nought after its point. */
    static String figure(float value) {
        String written = String.format(java.util.Locale.US, "%.1f", value);
        return written.endsWith(".0") ? written.substring(0, written.length() - 2) : written;
    }

    /** Words wrapped at a size. */
    List<String> wrapSized(String words, float wide, float size) {
        text.setTextSize(size);
        return wrap(words, wide);
    }

    /** The button that changes what she holds, with a picture of what a tap on it would put in her hands; and, as it is held, a ring that closes. */
    private void swapControl(Canvas canvas) {
        int next = run.nextUp();
        RectF where = swapButton;
        boolean held = swapFinger >= 0;
        paint.setStyle(Paint.Style.FILL);
        // (It lies over the ground she walks on, as the button that lets her down does: so it is faint until it is touched.)
        paint.setColor(held ? CalculatorLayout.CORAL : cardColor);
        paint.setAlpha(held ? 235 : 150);
        canvas.drawCircle(where.centerX(), where.centerY(), where.width() / 2, paint);
        paint.setColor(Color.BLACK);
        paint.setAlpha(held ? 255 : 200);
        boolean upright = next != PlusRun.GUN && next != PlusRun.BOOK && next < PlusRun.TABLET;
        thing(canvas, next, where.centerX(), where.centerY(), where.height() * (upright ? .62f : .5f), upright ? 35 : 0, false, false);
        // Two small arrows on its corner: this changes one thing for another.
        stroke.setColor(held ? Color.WHITE : 0xFF8A5A33);
        stroke.setStrokeWidth(dp(1.8f));
        float cx = where.right - dp(9), cy = where.top + dp(9), arm = dp(5);
        canvas.drawLine(cx - arm, cy - dp(2), cx + arm, cy - dp(2), stroke);
        canvas.drawLine(cx + arm, cy - dp(2), cx + arm - dp(3), cy - dp(5), stroke);
        canvas.drawLine(cx + arm, cy + dp(3), cx - arm, cy + dp(3), stroke);
        canvas.drawLine(cx - arm, cy + dp(3), cx - arm + dp(3), cy + dp(6), stroke);
        if (held) {
            box.set(where.left - dp(3), where.top - dp(3), where.right + dp(3), where.bottom + dp(3));
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(dp(3));
            canvas.drawArc(box, -90, 360 * Math.min(1, swapHeld / .35f), false, stroke);
        }
    }

    /** A page of her tablet-book in the air, turning; or a key, pointing the way it flies. One that the small copy of her threw is smaller. */
    private void drawFlick(Canvas canvas, PlusRun.Shot shot, float x, float y) {
        float size = tile * (shot.pets ? .32f : .52f);
        if (shot.kind == PlusRun.THROWN_KEY) {
            float turn = (float) Math.toDegrees(Math.atan2(shot.down, shot.across));
            canvas.save();
            canvas.rotate(turn, x, y);
            thing2(canvas, 3, x, y, size * 1.5f, 0, false);
            canvas.restore();
            return;
        }
        // A page of her own book: pink glow, and two fainter pages behind it fanning out the way it came.
        glowAt(canvas, x, y, size * 1.1f, 0xFFFF7AB8, 150);
        paint.setAlpha(255);
        float back = shot.across < 0 ? 1 : -1;
        for (int behind = 2; behind >= 0; behind--) {
            canvas.save();
            float bx = x + back * behind * size * .55f, by = y + (behind == 1 ? -1 : 1) * behind * size * .18f;
            canvas.rotate(shot.age * 540 - behind * 35, bx, by);
            paint.setAlpha(behind == 0 ? 255 : behind == 1 ? 130 : 70);
            halgaBit(canvas, 0, bx, by, size * 1.6f);
            canvas.restore();
        }
        paint.setAlpha(255);
    }

    /** The panel of translation her tablet-book opens: a pane of light in front of her with lines of writing in it, there for a third of a second. */
    private void drawPanel(Canvas canvas) {
        if (panelAge > .36f) return;
        float through = panelAge / .36f, wide = RunArms.panelWide(perks.levels[PlusRun.TABLET]) * tile, tall = RunArms.panelTall(perks.levels[PlusRun.TABLET]) * tile, open = Math.min(1, through * 4);
        float near = (panelX - cameraX) * tile, top = (panelY - cameraY) * tile, far = near + (panelLeft ? -1 : 1) * wide * open;
        float from = Math.min(near, far), to = Math.max(near, far);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFFF8AC4);
        paint.setAlpha(Math.round(120 * (1 - through)));
        canvas.drawRoundRect(from, top, to, top + tall, tile * .2f, tile * .2f, paint);
        // Her stamp in the middle of it, pressed in and fading.
        paint.setAlpha(Math.round(255 * (1 - through)));
        halgaBit(canvas, 1, (from + to) / 2, top + tall / 2, Math.min(tall, to - from) * (.7f + .3f * Math.min(1, through * 3)));
        paint.setAlpha(255);
        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(tile * .07f);
        stroke.setAlpha(Math.round(255 * (1 - through)));
        canvas.drawRoundRect(from, top, to, top + tall, tile * .2f, tile * .2f, stroke);
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.round(230 * (1 - through)));
        for (int line = 0; line < 5; line++) {
            float y = top + tall * (.16f + line * .16f), length = (to - from) * (line % 2 == 0 ? .78f : .56f);
            canvas.drawRoundRect(from + (to - from) * .1f, y, from + (to - from) * .1f + length, y + tall * .06f, tall * .03f, tall * .03f, paint);
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        stroke.setAlpha(255);
    }

    /** Her own book's page, and its stamp. */
    private Bitmap halgaBits;

    private void halgaBit(Canvas canvas, int which, float x, float y, float size) {
        if (halgaBits == null) halgaBits = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_halga_bits);
        int side = halgaBits.getHeight();
        source.set(which * side, 0, which * side + side, side);
        target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        canvas.drawBitmap(halgaBits, source, target, paint);
    }

    /** One of the four pictures that go with the tablet-book and the keys, its middle at a place. */
    private void thing2(Canvas canvas, int which, float x, float y, float size, float degrees, boolean mirrored) {
        if (arms2 == null) arms2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_arms2);
        int side = arms2.getHeight();
        source.set(which * side, 0, which * side + side, side);
        target.set(-size / 2, -size / 2, size / 2, size / 2);
        canvas.save();
        canvas.translate(x, y);
        if (mirrored) canvas.scale(-1, 1);
        canvas.rotate(degrees);
        canvas.drawBitmap(arms2, source, target, paint);
        canvas.restore();
    }

    /** The sky of a chapter over the whole screen, drifting by at a fifth of the land's pace. */
    /** The paper trees: four drawings, in a strip; and the line on which the wood behind the land stands, as it was last drawn. */
    private Bitmap paperTrees;
    private float woodLine = -1, camFeet = -99;
    /** How far her feet must be from the height the view follows, standing, before it follows them. */
    static final float CAMERA_STILL = 1.2f;
    /** The wood's ground, in rows of the land: where it stands, where it is going, and the view it was last at rest with; and time since it was last drawn. */
    private float woodGround = -1, woodTarget = -1, woodCamera, woodSeconds;
    private boolean woodReset;

    private static final float WOOD_PACE = .52f, WOOD_EVERY = 4.6f;

    /**
     * Behind a land of the Contents Village, a wood of paper trees: some way off, so it goes by at
     * a little under half her pace; on a line a little below where the ground under her is, and
     * following it slowly as the land climbs and drops; larger and smaller by turns, some a little
     * paler for being further off. Where each stands, and which it is, comes from its place in the
     * wood, so the same wood is there each time she comes back to the same place.
     */
    /** What lies under each chapter's ground, behind its caves; and the vine and ladder that lead between layers, and the hatch in the ground. */
    private static final int[] UNDERS = {R.drawable.run_under1, R.drawable.run_under2, R.drawable.run_under3, R.drawable.run_under4, R.drawable.run_under5,
            R.drawable.run_under6, R.drawable.run_under7, R.drawable.run_under8, R.drawable.run_under9, R.drawable.run_under10, R.drawable.run_under11, R.drawable.run_under12};
    private Bitmap under, vineArt, ladderArt, hatchArt;
    private int underOf = -1;

    /** Under the land, where the view goes down into a cave: the chapter's underground, from the bottom of the land's own rows down, going by slowly. */
    private void drawUnder(Canvas canvas, RunLevels.Level level) {
        float top = (RunLevels.ROWS - cameraY) * tile;
        int chapter = Math.max(0, Math.min(UNDERS.length - 1, level.chapter));
        if (underOf != chapter) {
            if (under != null) under.recycle();
            under = BitmapFactory.decodeResource(context.getResources(), UNDERS[chapter]);
            underOf = chapter;
        }
        // (Down the shaft under a hatch, and up the one a vine climbs, it is dark: the cave under the land shows, not the sky.)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1C1626);
        for (int[] way : level.ways) {
            if (way[2] > 1) continue;
            float x = (way[0] - cameraX) * tile;
            if (x < -tile || x > width) continue;
            canvas.drawRect(x - 1, (way[1] + .3f - cameraY) * tile, x + tile + 1, (RunLevels.ROWS - cameraY) * tile + 1, paint);
        }
        paint.setColor(Color.BLACK);
        if (top >= height) return;
        float tall = tile * RunLevels.LOWER, wide = tall * under.getWidth() / under.getHeight(), drift = cameraX * tile * .35f % (wide * 2);
        paint.setAlpha(255);
        for (int copy = -1; copy * wide - drift < width; copy++) {
            float left = copy * wide - drift;
            target.set(left - 1, top, left + wide + 1, top + tall);
            canvas.save();
            if (Math.floorMod(copy, 2) != 0) canvas.scale(-1, 1, left + wide / 2, 0);
            canvas.drawBitmap(under, null, target, paint);
            canvas.restore();
        }
    }

    /** A vine (in the lands of the first volume and the shore) or a ladder (indoors), one square of it. */
    private void drawVine(Canvas canvas, int chapter, float left, float y) {
        boolean ladder = chapter == 1 || chapter >= 5 && chapter != 8 && chapter != 10;
        if (vineArt == null) vineArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_vine);
        if (ladderArt == null) ladderArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_ladder);
        Bitmap shown = ladder ? ladderArt : vineArt;
        float wide = tile * .9f;
        paint.setAlpha(255);
        target.set(left + tile / 2 - wide / 2, y - 1, left + tile / 2 + wide / 2, y + tile + 1);
        canvas.drawBitmap(shown, null, target, paint);
    }

    /** A hatch in the ground: a trapdoor, with a glint running along it now and then, to say it opens. */
    private void drawHatch(Canvas canvas, float left, float y, int column) {
        if (hatchArt == null) hatchArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_hatch);
        float tall = tile * .34f;
        paint.setAlpha(255);
        target.set(left - tile * .06f, y - tall * .2f, left + tile * 1.06f, y - tall * .2f + tall);
        canvas.drawBitmap(hatchArt, null, target, paint);
        float glint = (clock * .7f + column * .37f) % 2.2f;
        if (glint < 1) glowAt(canvas, left + tile * glint, y + tile * .12f, tile * .3f, 0xFFFFE6A0, Math.round(200 * (float) Math.sin(glint * Math.PI)));
        paint.setAlpha(255);
    }

    /** The ways on from one land to the next, one for each chapter: a bridge into a paper tree, a shelf that opens, a mine, a castle gate, a path behind a waterfall... */
    private static final int[] WAYS = {R.drawable.run_way1, R.drawable.run_way2, R.drawable.run_way3, R.drawable.run_way4, R.drawable.run_way5, R.drawable.run_way6,
            R.drawable.run_way7, R.drawable.run_way8, R.drawable.run_way9, R.drawable.run_way10, R.drawable.run_way11, R.drawable.run_way12};
    private final Bitmap[] ways = new Bitmap[WAYS.length];

    private Bitmap way(int chapter) {
        int which = Math.max(0, Math.min(WAYS.length - 1, chapter));
        if (ways[which] == null) {
            // (Two at most are kept: the way out of this land, and the way it was come into by.)
            for (int other = 0; other < ways.length; other++) {
                if (other != which && ways[other] != null && other != RunLevels.chapterOf(Math.max(1, number - 1)) && other != RunLevels.chapterOf(number)) {
                    ways[other].recycle();
                    ways[other] = null;
                }
            }
            ways[which] = BitmapFactory.decodeResource(context.getResources(), WAYS[which]);
        }
        return ways[which];
    }

    /** How tall a way on is drawn, in squares; and how wide it may be at most. */
    static final float WAY_TALL = 5f, WAY_WIDE = 8.5f;

    /** How wide a way on is drawn, so tall as it is drawn, or less where it is a long one. */
    private float wayWide(Bitmap way) { return Math.min(tile * WAY_WIDE, tile * WAY_TALL * way.getWidth() / way.getHeight()); }

    /**
     * The way on at the end of the land, past its gate, that she walks into to come to the next: of the land's chapter; and at its
     * start the way she came into it by, of the land before's, turned about, as she comes out of it. Behind her, standing on the ground.
     */
    private void drawJoins(Canvas canvas, RunLevels.Level level) {
        if (blackRung > 0) return;
        if (delve >= 0) {
            // In a dive: the stairs she came down by at the start; and past the gate, but on the keeper's floor, the stairs on down.
            Bitmap art = stairsArt();
            float tall = tile * 2.7f, wide = tall * art.getWidth() / art.getHeight();
            paint.setAlpha(255);
            for (int end = 0; end < 2; end++) {
                if (end == 1 && delveFloor >= RunDungeons.floors(delve) - 1) continue;
                float at = end == 0 ? 1.5f : level.gate + 3.5f, x = (at - cameraX) * tile;
                if (x < -wide || x > width + wide) continue;
                int column = Math.max(0, Math.min(level.columns - 1, (int) at));
                float feet = (Math.max(0, RunLevels.top(level, column)) - cameraY) * tile;
                target.set(x - wide / 2, feet - tall + tile * .06f, x + wide / 2, feet + tile * .06f);
                canvas.drawBitmap(art, null, target, paint);
            }
            return;
        }
        paint.setAlpha(255);
        if (number < RunLevels.OPEN_TO) {
            Bitmap out = way(level.chapter);
            float wide = wayWide(out);
            float x = (level.main - PlusRun.THROUGH - cameraX) * tile;
            if (x > -wide && x < width + wide) {
                int column = Math.max(0, Math.min(level.columns - 1, Math.round(level.main - PlusRun.THROUGH)));
                float feet = (Math.max(0, RunLevels.top(level, column)) - cameraY) * tile, tall = wide * out.getHeight() / out.getWidth();
                target.set(x - wide / 2, feet - tall + tile * .06f, x + wide / 2, feet + tile * .06f);
                canvas.drawBitmap(out, null, target, paint);
            }
        }
        if (number > 1) {
            Bitmap in = way(RunLevels.chapterOf(number - 1));
            float wide = wayWide(in), x = (1.2f - cameraX) * tile;
            if (x < width + wide) {
                float feet = (Math.max(0, RunLevels.top(level, 1)) - cameraY) * tile, tall = wide * in.getHeight() / in.getWidth();
                canvas.save();
                canvas.scale(-1, 1, x, 0);
                target.set(x - wide / 2, feet - tall + tile * .06f, x + wide / 2, feet + tile * .06f);
                canvas.drawBitmap(in, null, target, paint);
                canvas.restore();
            }
        }
    }

    private Bitmap meadow;

    private void drawPaperTrees(Canvas canvas, RunLevels.Level level) {
        if (paperTrees == null) paperTrees = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_paper_trees);
        if (paperTrees == null) return;
        int middle = Math.max(0, Math.min(run.columns() - 1, (int) (cameraX + width / tile / 2)));
        // The wood's ground: the land's usual height over a wide stretch about the middle of the view (the middle of its heights, so a step,
        // a ledge or a pit makes no difference), moved to only when it is well away from where the wood stands now, and then slowly.
        float usual = RunLevels.usualGround(level, middle);
        if (woodGround < 0 || woodReset) {
            woodGround = woodTarget = usual;
            woodCamera = cameraY;
            woodReset = false;
        }
        if (usual >= 0 && Math.abs(usual - woodTarget) > RunLevels.WOOD_STILL) woodTarget = usual;
        float rate = Math.min(1, woodSeconds * 1.1f);
        woodGround += (woodTarget - woodGround) * rate;
        woodCamera += (cameraY - woodCamera) * Math.min(1, woodSeconds * .9f);
        woodSeconds = 0;
        // (And the view going up and down moves it only by half as much: it is further off than the land.)
        woodLine = (woodGround - woodCamera) * tile - (cameraY - woodCamera) * tile * RunLevels.WOOD_FOLLOWS + tile * .35f;
        int kinds = 4, cellWide = paperTrees.getWidth() / kinds, cellTall = paperTrees.getHeight();
        float back = cameraX * WOOD_PACE;
        int first = (int) Math.floor(back / WOOD_EVERY) - 2, last = (int) Math.ceil((back + width / tile) / WOOD_EVERY) + 2;
        paint.setFilterBitmap(true);
        // The wood has a ground of its own, going by as slowly as it does: a meadow of low paper hills from a little over the trees'
        // feet down to the bottom of the screen, so that every trunk stands in grass, and where the land in front drops away or steps up,
        // meadow is what shows under them, never sky.
        if (meadow == null) meadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_meadow);
        if (meadow != null) {
            float wide = tile * 16, tall = wide * meadow.getHeight() / meadow.getWidth(), top = woodLine - tile * 1.05f;
            float drift = Math.floorMod(Math.round(back * tile), Math.round(wide * 2));
            paint.setAlpha(240);
            for (int copy = -1; copy * wide - drift < width; copy++) {
                float left = copy * wide - drift;
                target.set(left - 1, top, left + wide + 1, top + tall);
                canvas.save();
                // (Every other copy is its mirror, so their edges always meet.)
                if (Math.floorMod(copy, 2) != 0) canvas.scale(-1, 1, left + wide / 2, 0);
                canvas.drawBitmap(meadow, null, target, paint);
                canvas.restore();
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFCDD8BA);
            paint.setAlpha(240);
            canvas.drawRect(0, top + tall - 1, width, height, paint);
            paint.setColor(Color.BLACK);
        }
        for (int slot = first; slot <= last; slot++) {
            // (Its own small chance, the same every time: which tree, how far off, how large.)
            int seed = slot * 7919 + level.number * 104729;
            int which = Math.floorMod(seed >> 3, kinds);
            float offset = Math.floorMod(seed >> 7, 100) / 100f, far = Math.floorMod(seed >> 11, 3) / 2f;
            // (Each tree stands on its roots, which go down past the line of the ground in front, under it.)
            float tall = tile * (6.4f - far * 2f);
            float x = (slot * WOOD_EVERY + offset * WOOD_EVERY * .6f - back) * tile;
            float wide = tall * cellWide / cellTall;
            source.set(which * cellWide, 0, (which + 1) * cellWide, cellTall);
            float foot = woodLine + tile * .55f - far * tile * .5f;
            target.set(x - wide / 2, foot - tall, x + wide / 2, foot);
            paint.setAlpha(Math.round(210 - far * 60));
            canvas.drawBitmap(paperTrees, source, target, paint);
        }
        paint.setAlpha(255);
    }

    private void drawSky(Canvas canvas, int chapter, float drift) {
        if (skyOf != chapter) {
            sky = BitmapFactory.decodeResource(context.getResources(), SKIES[chapter]);
            skyOf = chapter;
        }
        float wide = height * (float) sky.getWidth() / sky.getHeight();
        paint.setAlpha(255);
        // Each copy is the mirror of the one before it, so their edges always meet.
        for (int copy = (int) (drift / wide); copy * wide - drift < width; copy++) {
            float left = copy * wide - drift;
            // A hair over on both sides, so that no line of nothing shows between one copy and the next.
            target.set(left - 1, 0, left + wide + 1, height);
            canvas.save();
            if (copy % 2 != 0) canvas.scale(-1, 1, left + wide / 2, 0);
            canvas.drawBitmap(sky, null, target, paint);
            canvas.restore();
        }
    }

    /**
     * Draws one of the pictures of the art: standing on a line, its middle at a place, as
     * wide as asked and as tall as that makes it, and turned about if it is to face the other way.
     */
    private void art(Canvas canvas, int which, float middle, float bottom, float wide, boolean turned) {
        int side = art.getHeight();
        float across = artWide[which], up = artTall[which];
        source.set(Math.round(which * side + side * (1 - across) / 2), Math.round(side * (1 - up)),
                Math.round(which * side + side * (1 + across) / 2), side);
        // A picture that fills its square is taken a hair inside it, so the one beside it does not show at its edge.
        if (across > .98f) source.inset(1, 0);
        target.set(middle - wide / 2, bottom - wide * up / across, middle + wide / 2, bottom);
        if (turned) {
            canvas.save();
            canvas.scale(-1, 1, middle, bottom);
            canvas.drawBitmap(art, source, target, paint);
            canvas.restore();
        } else {
            canvas.drawBitmap(art, source, target, paint);
        }
    }

    /** A picture of the art as tall as asked, whatever that makes its width. */
    private void artTall(Canvas canvas, int which, float middle, float bottom, float tall, boolean turned) {
        art(canvas, which, middle, bottom, tall * artWide[which] / artTall[which], turned);
    }

    /** A picture of the art fitted inside a square with its middle at a place. */
    private void artIn(Canvas canvas, int which, float x, float y, float size) {
        float wide = artWide[which] >= artTall[which] ? size : size * artWide[which] / artTall[which];
        art(canvas, which, x, y + wide * artTall[which] / artWide[which] / 2, wide, false);
    }

    /**
     * One of the things to fight with, its middle at a place and as long as asked, turned
     * round by some degrees. All but the ink gun are drawn standing on end, point up, so a
     * quarter turn lays one flat with its point to the right; mirrored, to the left.
     */
    void thing(Canvas canvas, int kind, float x, float y, float tall, float degrees, boolean mirrored, boolean byTheHandle) {
        if (kind >= RunArms.GREATSWORD) {
            int held = perks != null ? perks.item : 0;
            int index = held != 0 && RunArms.kindOf(held) == kind ? iconOf(held) : kind == RunArms.KNUCKLES ? RunLoot.KNUCKLE_ICONS : RunArms.baseOf(kind) * 12;
            canvas.save();
            canvas.translate(x, y);
            if (mirrored) canvas.scale(-1, 1);
            drawIcon(canvas, index, 0, 0, tall * 1.4f);
            canvas.restore();
            return;
        }
        if (kind >= PlusRun.KATANA) {
            // The four won in the black arena: pictures of their own, each lying level.
            if (arms3 == null) arms3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_arms3);
            int side = arms3.getHeight(), which = kind - PlusRun.KATANA;
            float size = tall * 1.5f;
            source.set(which * side, 0, which * side + side, side);
            target.set(-size / 2, -size / 2, size / 2, size / 2);
            canvas.save();
            canvas.translate(x, y);
            if (mirrored) canvas.scale(-1, 1);
            canvas.drawBitmap(arms3, source, target, paint);
            canvas.restore();
            return;
        }
        if (kind >= PlusRun.TABLET) {
            // Her tablet-book, and her ring of keys: pictures of their own.
            thing2(canvas, kind == PlusRun.TABLET ? 0 : 2, x, y, tall * 1.5f, 0, mirrored);
            return;
        }
        int which = WEAPON_ART + kind, side = art.getHeight();
        float across = artWide[which], up = artTall[which], wide = tall * across / up;
        source.set(Math.round(which * side + side * (1 - across) / 2), Math.round(side * (1 - up)),
                Math.round(which * side + side * (1 + across) / 2), side);
        if (across > .98f) source.inset(1, 0);
        // Held by the handle it turns about its lower end; thrown, about its middle.
        if (byTheHandle) target.set(-wide / 2, -tall, wide / 2, 0);
        else target.set(-wide / 2, -tall / 2, wide / 2, tall / 2);
        canvas.save();
        canvas.translate(x, y);
        if (mirrored) canvas.scale(-1, 1);
        canvas.rotate(degrees);
        canvas.drawBitmap(art, source, target, paint);
        canvas.restore();
    }

    /** A picture of one of the later sheets: standing on a line, its middle at a place, as wide as asked, and turned about if it is to face the other way. */
    private void piece(Canvas canvas, Sheet sheet, int which, float middle, float bottom, float wide, boolean turned) {
        Bitmap bitmap = sheet.get();
        int side = bitmap.getWidth() / sheet.across, left = which % sheet.across * side, top = which / sheet.across * side;
        float across = sheet.wide[which], up = sheet.tall[which];
        source.set(Math.round(left + side * (1 - across) / 2), Math.round(top + side * (1 - up)), Math.round(left + side * (1 + across) / 2), top + side);
        if (across > .98f) source.inset(1, 0);
        // (Its clear rows at the bottom are under the ground.)
        bottom += wide * sheet.low[which] / across + dp(1);
        target.set(middle - wide / 2, bottom - wide * up / across, middle + wide / 2, bottom);
        canvas.save();
        if (turned) canvas.scale(-1, 1, middle, bottom);
        canvas.drawBitmap(bitmap, source, target, paint);
        canvas.restore();
    }

    private void pieceTall(Canvas canvas, Sheet sheet, int which, float middle, float bottom, float tall, boolean turned) {
        sheet.get();
        piece(canvas, sheet, which, middle, bottom, tall * sheet.wide[which] / sheet.tall[which], turned);
    }

    /** One of them fitted inside a square with its middle at a place, turned round by some degrees. */
    private void pieceIn(Canvas canvas, Sheet sheet, int which, float x, float y, float size, float degrees) {
        sheet.get();
        float wide = sheet.wide[which] >= sheet.tall[which] ? size : size * sheet.wide[which] / sheet.tall[which];
        canvas.save();
        canvas.rotate(degrees, x, y);
        piece(canvas, sheet, which, x, y + wide * sheet.tall[which] / sheet.wide[which] / 2, wide, false);
        canvas.restore();
    }

    /** A whole square of the second sheet over a square of the land: what the ground is made of. */
    private void square(Canvas canvas, int which, float left, float y, boolean mirrored, boolean upended) {
        Bitmap bitmap = two.get();
        int side = bitmap.getWidth() / two.across, fromX = which % two.across * side, fromY = which / two.across * side;
        source.set(fromX + 1, fromY + 1, fromX + side - 1, fromY + side - 1);
        // (A pixel over on every side, so that one turned about its middle covers as much on the one side as on the other.)
        target.set(left - 1, y - 1, left + tile + 1, y + tile + 1);
        canvas.save();
        canvas.scale(mirrored ? -1 : 1, upended ? -1 : 1, left + tile / 2, y + tile / 2);
        canvas.drawBitmap(bitmap, source, target, paint);
        canvas.restore();
    }

    /** What is on the screen behind everything else: the land; or, at the end of the last land, the sky over it, or the land as it is unmade. */
    private void drawLand(Canvas canvas) {
        if (finale == WATCHING) {
            // (Under a clip there is nothing to see.)
            canvas.drawColor(Color.BLACK);
        } else if (finale == FLIGHT || finale == FLOWN) {
            drawFlight(canvas);
        } else if (finale >= ARISING) {
            drawRide(canvas);
        } else if (finale == UNMADE) {
            drawUnmade(canvas);
        } else {
            drawGround(canvas);
            if (run.moment() > 0) drawMoment(canvas);
        }
    }

    private void drawGround(Canvas canvas) {
        RunLevels.Level level = run.level();
        int chapter = level.chapter;
        if (run.room() >= 0) drawInside(canvas);
        else if (delve >= 0) drawDepths(canvas, cameraX * tile * .12f);
        else drawSky(canvas, chapter, cameraX * tile * .12f);
        // In the Contents Village, between the sky and the land, a wood of paper trees.
        if (run.room() < 0 && chapter == 0 && delve < 0) drawPaperTrees(canvas, level);
        if (run.room() < 0) drawJoins(canvas, level);
        if (run.room() < 0 && level.under != null) drawUnder(canvas, level);
        float across = width / tile, down = height / tile;
        int first = Math.max(0, (int) cameraX), last = Math.min(run.columns() - 1, (int) (cameraX + across) + 1);
        int top = Math.max(0, (int) Math.floor(cameraY)), bottom = Math.min(level.land.length - 1, (int) (cameraY + down) + 1);
        int side = art.getHeight();
        boolean later = chapter >= RunLevels.CHAPTERS;
        canvas.save();
        // In a place off the land nothing is drawn beyond its walls: whatever else lies beyond the end of the land is not there to be seen.
        if (run.room() >= 0) {
            int[] in = level.rooms.get(run.room());
            canvas.clipRect((in[3] - 2 - cameraX) * tile, (in[12] - 2 - cameraY) * tile, (in[4] + 3 - cameraX) * tile, (in[11] + 3 - cameraY) * tile);
        }
        // The ground jumps when something heavy comes down on it.
        if (shake > 0) canvas.translate((random.nextFloat() - .5f) * tile * .28f * Math.min(1, shake * 4), (random.nextFloat() - .5f) * tile * .28f * Math.min(1, shake * 4));
        paint.setAlpha(255);
        // What stands in its villages, behind everything else.
        for (int[] decor : level.decor) {
            if (decor[1] < first - 4 || decor[1] > last + 4) continue;
            // Over a hundred, it is something of the fourth sheet: a chest in a stash.
            if (decor[0] >= 400) {
                drawVillagePiece(canvas, decor, level);
                continue;
            }
            if (decor[0] == 307) {
                // (The vanity boutique: a picture of its own.)
                if (boutiqueHouse == null) boutiqueHouse = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_vanity_shop);
                cell(canvas, boutiqueHouse, 0, 1, (decor[1] + .5f - cameraX) * tile, (decor[2] + 1 - cameraY) * tile, tile * 3.9f, false);
            } else if (decor[0] == 306) {
                // (The ribbon gacha: a picture of its own.)
                if (gachaHouse == null) gachaHouse = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_gacha_house);
                cell(canvas, gachaHouse, 0, 1, (decor[1] + .5f - cameraX) * tile, (decor[2] + 1 - cameraY) * tile, tile * 3.9f, false);
            } else if (decor[0] >= 300) {
                if (town == null) town = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_town);
                cell(canvas, town, decor[0] - 300, 6, (decor[1] + .5f - cameraX) * tile, (decor[2] + 1 - cameraY) * tile, tile * 3.9f, false);
            } else if (decor[0] >= 200) {
                if (props == null) props = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_props);
                paint.setAlpha(235);
                cell(canvas, props, decor[0] - 200, 16, (decor[1] + .5f - cameraX) * tile, (decor[2] + 1 - cameraY) * tile, tile * 2.5f, false);
                paint.setAlpha(255);
            } else if (decor[0] >= 100) pieceTall(canvas, four, decor[0] - 100, (decor[1] + .5f - cameraX) * tile, (decor[2] + 1 - cameraY) * tile, tile * 1.3f, false);
            else pieceTall(canvas, three, decor[0], (decor[1] + .5f - cameraX) * tile, (decor[2] + 1 - cameraY) * tile, tile * DECOR_TALL[decor[0]], false);
        }
        for (int column = first; column <= last; column++) {
            float left = (column - cameraX) * tile;
            for (int row = top; row <= bottom; row++) {
                char here = run.shown(column, row);
                float y = (row - cameraY) * tile;
                paint.setAlpha(255);
                // A secret space not yet found is drawn as the rock round it: the same squares, nothing of what is inside. Found, the rock
                // over it fades away, and its false wall is left faint, to be seen through.
                float ago = run.foundAgo(column, row);
                boolean fading = false;
                if (here == RunLevels.FAKE) {
                    here = RunLevels.BLOCK;
                    paint.setAlpha(Math.round(255 - 185 * Math.min(1, Math.max(0, ago) / PlusRun.SECRET_SHOWS)));
                } else if (ago >= 0 && ago < PlusRun.SECRET_SHOWS && here == RunLevels.AIR) {
                    here = RunLevels.BLOCK;
                    fading = true;
                    paint.setAlpha(Math.round(255 * (1 - ago / PlusRun.SECRET_SHOWS)));
                }
                if (here == RunLevels.VINE) {
                    drawVine(canvas, chapter, left, y);
                    continue;
                }
                if (here == RunLevels.THIN && run.room() < 0 && row < RunLevels.ROWS && (run.at(column - 1, row) == RunLevels.BLOCK || run.at(column + 1, row) == RunLevels.BLOCK)
                        && run.at(column, row - 1) != RunLevels.BLOCK) {
                    drawHatch(canvas, left, y, column);
                    continue;
                }
                char laid = run.room() >= 0 ? run.laid(column, row) : RunLevels.AIR;
                if (laid == RunLevels.LOCKED || laid == RunLevels.SPAN || laid == RunLevels.GRILLE) {
                    if (laid != RunLevels.SPAN || here == RunLevels.AIR) {
                        drawFixture(canvas, laid, here, column, row, left, y);
                        continue;
                    }
                }
                if (here == RunLevels.AIR) continue;
                if (here == RunLevels.BLOCK && underSquare(canvas, column, row, left, y)) continue;
                if (here == RunLevels.BLOCK && run.room() >= 0) {
                    // In a place off the land, its walls are dark, of the colour of the place, with a lighter board where they are stood on.
                    boolean capped = run.shown(column, row - 1) != RunLevels.BLOCK;
                    int look = look();
                    int[] in = level.rooms.get(run.room());
                    // (What stands inside it, between its floor and its ceiling, is masonry of the place's own colour, with its courses
                    // showing: a pillar, a tower, a wall with a door in it; not the dark that lies round the place.)
                    boolean within = look >= 0 && row >= in[12] && row < in[11] && column >= in[3] && column <= in[4];
                    paint.setStyle(Paint.Style.FILL);
                    // (In a building of a village, under the board of its floor, its picture's own floor shows, as far down as the screen goes.)
                    if (in[0] == RunLevels.HOUSE && look >= 0 && row >= in[11] && column >= in[3] && column <= in[4]) {
                        if (capped) {
                            paint.setColor(LOOK_CAP[look]);
                            canvas.drawRect(left, y, left + tile + 1, y + tile * .1f, paint);
                            paint.setColor(0x33000000);
                            canvas.drawRect(left, y + tile * .1f, left + tile + 1, y + tile * .22f, paint);
                        }
                        paint.setColor(Color.BLACK);
                        continue;
                    }
                    paint.setColor(look < 0 ? 0xFF2A1D1A : within ? mixed(LOOK_WALL[look], LOOK_CAP[look], .42f) : LOOK_WALL[look]);
                    canvas.drawRect(left, y, left + tile + 1, y + tile + 1, paint);
                    if (within) {
                        paint.setColor(mixed(LOOK_WALL[look], LOOK_CAP[look], .2f));
                        canvas.drawRect(left, y + tile * .94f, left + tile + 1, y + tile + 1, paint);
                        float seam = left + tile * ((row + column) % 2 == 0 ? .5f : .02f);
                        canvas.drawRect(seam, y, seam + tile * .05f, y + tile * .94f, paint);
                        if (run.at(column - 1, row) != RunLevels.BLOCK) {
                            paint.setColor(mixed(LOOK_WALL[look], LOOK_CAP[look], .7f));
                            canvas.drawRect(left, y, left + tile * .07f, y + tile + 1, paint);
                        }
                    }
                    if (capped) {
                        paint.setColor(look < 0 ? 0xFF8A5F3E : LOOK_CAP[look]);
                        canvas.drawRect(left, y, left + tile + 1, y + tile * .16f, paint);
                    }
                    paint.setColor(Color.BLACK);
                } else if (here == RunLevels.BLOCK) {
                    // The top of a pile has the turf on it; the rest is what lies under. Every other one is
                    // turned about, so the same picture side by side does not show as a pattern.
                    // (Rock fading off a secret space just found has no turf on it anywhere.)
                    boolean capped = !fading && run.shown(column, row - 1) != RunLevels.BLOCK;
                    if (chapter >= RunLevels.CHAPTERS * 2) {
                        // (The third volume's ground is in a row of squares of its own.)
                        if (ground3 == null) ground3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.plus_run_art5);
                        int square = ground3.getHeight(), which = (chapter - RunLevels.CHAPTERS * 2) * 2 + (capped ? 0 : 1);
                        source.set(which * square + 1, 1, which * square + square - 1, square - 1);
                        target.set(left - 1, y - 1, left + tile + 1, y + tile + 1);
                        canvas.save();
                        canvas.scale(column % 2 == 0 ? 1 : -1, capped || row % 2 == 0 ? 1 : -1, left + tile / 2, y + tile / 2);
                        canvas.drawBitmap(ground3, source, target, paint);
                        canvas.restore();
                        continue;
                    }
                    if (later) {
                        square(canvas, (chapter - RunLevels.CHAPTERS) * 2 + (capped ? 0 : 1), left, y, column % 2 != 0, !capped && row % 2 != 0);
                        continue;
                    }
                    int which = chapter * 2 + (capped ? 0 : 1);
                    // Taken a hair inside its square, so the picture beside it does not show as a line between blocks; and drawn a pixel over on
                    // every side, so that one turned about its middle (every other one is) still meets the next with no pixel between them half-covered.
                    source.set(which * side + 1, 0, which * side + side - 1, side);
                    target.set(left - 1, y - 1, left + tile + 1, y + tile + 1);
                    canvas.save();
                    canvas.scale(column % 2 == 0 ? 1 : -1, capped || row % 2 == 0 ? 1 : -1, left + tile / 2, y + tile / 2);
                    canvas.drawBitmap(art, source, target, paint);
                    canvas.restore();
                } else if (here == RunLevels.LEDGE && inPetShop()) {
                    // In the pet shop a ledge is its shelf, drawn once from wherever the first of it in view is.
                    if (run.at(column - 1, row) != RunLevels.LEDGE || column == first) drawShelf(canvas, column, row, y);
                } else if (here == RunLevels.LEDGE) {
                    art(canvas, LEDGE_ART, left + tile / 2, y + tile * artTall[LEDGE_ART] / artWide[LEDGE_ART], tile + 1, column % 2 != 0);
                    if (column % 3 == 1) lets(canvas, left + tile / 2, y + tile * .42f, row);
                } else if (here == RunLevels.THIN || here == RunLevels.CRUMBLE) {
                    int which = here == RunLevels.THIN ? THIN_ART : CRUMBLE_ART;
                    two.get();
                    // A ledge that has begun to crumble shivers until it goes.
                    float shiver = here == RunLevels.CRUMBLE && run.crumbling(column, row) > 0 ? (float) Math.sin(clock * 70 + column) * tile * .05f : 0;
                    piece(canvas, two, which, left + tile / 2 + shiver, y + (tile + 1) * two.tall[which] / two.wide[which], tile + 1, column % 2 != 0);
                    if (here == RunLevels.THIN && column % 3 == 1) lets(canvas, left + tile / 2, y + tile * .42f, row);
                } else if (here == RunLevels.BELT_RIGHT || here == RunLevels.BELT_LEFT) {
                    square(canvas, BELT_ART, left, y, here == RunLevels.BELT_LEFT, false);
                    // Arrowheads running along it the way it goes.
                    float way = here == RunLevels.BELT_RIGHT ? 1 : -1, along = (clock * 1.6f * way % 1 + 1) % 1;
                    stroke.setColor(Color.WHITE);
                    stroke.setAlpha(210);
                    stroke.setStrokeWidth(tile * .07f);
                    float x = left + tile * along, tip = tile * .12f * way;
                    canvas.drawLine(x - tip, y + tile * .1f, x + tip, y + tile * .2f, stroke);
                    canvas.drawLine(x + tip, y + tile * .2f, x - tip, y + tile * .3f, stroke);
                    stroke.setAlpha(255);
                } else if (here == RunLevels.SLICK) {
                    square(canvas, SLICK_ART, left, y, column % 2 != 0, false);
                } else if (here == RunLevels.WATER) {
                    drawWater(canvas, column, row, bottom, left);
                } else if (here == RunLevels.LAVA) {
                    boolean surface = run.at(column, row - 1) != here;
                    if (here == RunLevels.WATER) paint.setAlpha(215);
                    square(canvas, (here == RunLevels.LAVA ? LAVA_ART : WATER_ART) + (surface ? 0 : 1), left,
                            y + (surface ? (float) Math.sin(clock * 3 + column) * tile * .04f : 0), column % 2 != 0, false);
                    paint.setAlpha(255);
                } else if (here == RunLevels.DRAUGHT) {
                    // A draught: pale streaks going up.
                    stroke.setColor(Color.WHITE);
                    stroke.setStrokeWidth(tile * .06f);
                    for (int streak = 0; streak < 2; streak++) {
                        float rise = (clock * 1.8f + streak * .5f + column * .37f) % 1;
                        stroke.setAlpha(Math.round(150 * (float) Math.sin(rise * Math.PI)));
                        float x = left + tile * (.3f + streak * .4f), from = y + tile * (1 - rise);
                        canvas.drawLine(x, from, x, from - tile * .4f, stroke);
                    }
                    stroke.setAlpha(255);
                } else if (here == RunLevels.DOOR) {
                    pieceTall(canvas, two, DOOR_ART, left + tile / 2, y + tile, tile * 1.8f, false);
                } else if (here == RunLevels.SPIKE) {
                    art(canvas, SPIKES_ART, left + tile / 2, y + tile, tile + 1, false);
                } else if (here == RunLevels.SPRING) {
                    artTall(canvas, SPRING_ART, left + tile / 2, y + tile, tile * .8f, false);
                } else if (here == RunLevels.FLAG) {
                    // Faint until she has passed it.
                    paint.setAlpha(run.x() + PlusRun.WIDE / 2 >= column ? 255 : 120);
                    artTall(canvas, FLAG_ART, left + tile / 2, y + tile, tile * 1.9f, false);
                    paint.setAlpha(255);
                } else if (here == RunLevels.GATE && run.at(column, row + 1) == RunLevels.GATE) {
                    // The gate is a portal: a golden arch with the equals sign in its keystone, and in it a swirl. Until she has passed it, it is
                    // dim and still (and fainter while someone keeps it shut); passed, the swirl turns and glows, and a few motes drift off it.
                    boolean lit = run.passed() && onward();
                    if (portal == null) portal = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_portal);
                    paint.setAlpha(run.gateOpen() ? 255 : 110);
                    cell(canvas, portal, lit ? (int) (clock * 9) % 8 : 8, 9, left + tile / 2, y + tile * 2 + tile * .04f, tile * PORTAL_TALL, false);
                    paint.setAlpha(255);
                    if (lit) {
                        for (int mote = 0; mote < 4; mote++) {
                            float out = (clock * .45f + mote * .25f) % 1, way = mote * 1.7f + 1;
                            glowAt(canvas, left + tile / 2 + (float) Math.cos(way) * tile * (.4f + out * .9f), y + tile * 2 - tile * PORTAL_MIDDLE - (float) Math.sin(way) * tile * (.4f + out * .7f) - out * tile * .5f,
                                    tile * .12f, 0xFFF1E4FF, Math.round(210 * (1 - out)));
                        }
                        paint.setAlpha(255);
                    }
                }
            }
        }
        // Under the land it is darker: the caves and the rock round them.
        if (run.room() < 0 && level.under != null && (RunLevels.ROWS - cameraY) * tile < height) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x47000814);
            canvas.drawRect(-tile, (RunLevels.ROWS - cameraY) * tile, width + tile, height + tile, paint);
            paint.setColor(Color.BLACK);
        }
        // Of a place off the land only itself is seen: what lies to either side of its walls is dark.
        if (run.room() >= 0) {
            int look = look();
            paint.setStyle(Paint.Style.FILL);
            // (And the light of the place lies over all of it.)
            if (look >= 0) canvas.drawColor(LOOK_LIGHT[look]);
            paint.setColor(look < 0 ? 0xFF2A1D1A : LOOK_WALL[look]);
            float from = (run.roomFrom() - 1 - cameraX) * tile, to = (run.roomTo() + 2 - cameraX) * tile;
            if (from > 0) canvas.drawRect(0, 0, from + 1, height, paint);
            if (to < width) canvas.drawRect(to - 1, 0, width, height, paint);
            paint.setColor(Color.BLACK);
        }
        // The ledges that move: books lying open on the air.
        for (int index = 0; index < level.movers.size(); index++) {
            float x = run.moverX(index, run.time()), y = (run.moverY(index, run.time()) - cameraY) * tile;
            if (x + level.movers.get(index)[2] < first - 1 || x > last + 1) continue;
            two.get();
            for (int along = 0; along < level.movers.get(index)[2]; along++) {
                piece(canvas, two, MOVER_ART, (x + along + .5f - cameraX) * tile, y + (tile + 1) * two.tall[MOVER_ART] / two.wide[MOVER_ART], tile + 1, along % 2 != 0);
            }
        }
        // The ways that are shut while someone waits: the gate ahead, and, once she is in, the way back.
        if (level.arena >= 0 && level.guarded) drawWays(canvas, level);
        drawDoors(canvas, level);
        // The pluses, each bobbing a little and turning as it hangs there.
        drawSecretHints(canvas);
        for (int[] plus : run.pluses()) {
            if (plus[0] < first - 1 || plus[0] > last + 1 || run.hidden(plus[0], plus[1])) continue;
            float x = (plus[0] + .5f - cameraX) * tile;
            float y = (plus[1] + .9f - cameraY) * tile + (float) Math.sin(clock * 4 + plus[0]) * tile * .08f;
            float turned = Math.max(.18f, Math.abs((float) Math.cos(clock * 3 + plus[0] * .7f)));
            art(canvas, PLUS_ART, x, y, tile * .78f * turned, false);
        }
        drawScattered(canvas);
        // Whoever holds something out, where she waits: her friends, and those met in the villages, each beside what she has.
        for (int[] item : level.items) {
            if (item[3] == 0 || item[1] < first - 3 || item[1] > last + 3) continue;
            boolean given = item[0] != PlusRun.STALL && !run.items().contains(item), near = Math.abs(run.x() - item[1]) < 6;
            float x = (item[1] + 1.55f - cameraX) * tile, y = (item[2] + 1 - cameraY) * tile;
            int who = item[3];
            paint.setAlpha(255);
            int idleId = who * 1000 + item[1];
            if (who <= 2) {
                int pose = given ? 2 : near ? 1 : 0;
                if (pose == 0) frame(canvas, idleSheet(IDLE_CAST[who - 1]), idleFrame(idleId), IDLE_FRAMES, x, y, tile * 1.62f, false);
                else frame(canvas, cast(who - 1), pose, CAST_FRAMES[who - 1], x, y, tile * 1.62f, false);
            } else if (who == 3 || who == 4) {
                frame(canvas, idleSheet(IDLE_CAST[who == 3 ? TSUBAKIEH : ILLYA]), idleFrame(idleId), IDLE_FRAMES, x, y, tile * 1.62f, false);
            } else if (who == RunLevels.NURI) {
                cell(canvas, idleSheet(R.drawable.run_idle_nuri), idleFrame(idleId), IDLE_FRAMES, x, y, tile * 1.7f, false);
            } else {
                // (No hop: everyone stands still, and only idles.)
                float hop = 0;
                // Nabi by the pet shop's door looks into her shop, to the right, where her pets are; she and Inju each stand behind a counter of their own.
                boolean keeps = item[0] == PlusRun.SPOT && (who == RunLevels.NABI || who == RunLevels.INJU);
                frame(canvas, idleSheet(IDLE_VILLAGERS[who - RunLevels.SOMI]), idleFrame(idleId), IDLE_FRAMES, x + (who == RunLevels.ALTTEUL ? tile * .5f : 0), y - hop, tile * 1.62f * .72f,
                        keeps && who == RunLevels.NABI);
                if (keeps) {
                    furniture();
                    standing(canvas, who == RunLevels.NABI ? counterPets : deskGuild, x + (who == RunLevels.NABI ? tile * .25f : -tile * .1f), y + tile * .04f, tile * (who == RunLevels.NABI ? 1.6f : 1.8f));
                }
            }
            // What she holds out, over her hands, until it is taken.
            if (!given && item[0] != PlusRun.STALL && item[0] != PlusRun.DROPOFF && item[0] != PlusRun.SPOT && who >= 3) {
                float holdY = y - tile * 1.15f + (float) Math.sin(clock * 3.2f + item[1]) * tile * .05f;
                ring(canvas, x - tile * .95f, holdY, 0xFFFFC53D);
                help(canvas, item[0], x - tile * .95f, holdY, tile * .7f);
            }
            // And over her head a mark, until it is given: she has something for whoever comes up to her. Over a stall's keeper,
            // a sign that says it is a shop, larger when she is at it, when a tap on it opens the stall.
            int speaker = who <= 2 ? who : who == 3 ? 4 : who == 4 ? 3 : who == RunLevels.NURI ? 10 : FIRST_VILLAGER + who - RunLevels.SOMI;
            float head = y - tile * (who <= 4 ? 1.85f : 1.55f);
            if (item[0] == PlusRun.STALL) {
                boolean at = run.atStall() && Math.abs(run.x() - item[1]) < 3.5f;
                float signX = x + tile * .5f;
                canvas.save();
                if (at) canvas.scale(1.25f, 1.25f, signX, head);
                float wide = mark(canvas, signX, head, "Shop", SPEAKER_COLOURS[speaker], true);
                canvas.restore();
                if (at) stallSign.set(signX - wide, head - tile * 1.1f, signX + wide, head + tile * .4f);
            } else if (item[0] == PlusRun.SPOT) {
                // Whoever keeps a building: over her, what her desk is for.
                mark(canvas, x, head, item[4] == 100 + PETS_TAB ? "Pets" : "Counter", SPEAKER_COLOURS[speaker], true);
            } else if (item[0] == PlusRun.DROPOFF) {
                // Whoever a letter is for is marked while there is one on its way to her.
                if (run.quest() == PlusRun.ASKED) mark(canvas, x, head, "Letter", SPEAKER_COLOURS[speaker], false);
            } else if (!given) {
                // Whoever has something to ask of her is marked as that; the rest, as having something for her.
                boolean asks = level.quest != 0 && who == RunLevels.giverOf(level.quest) && run.quest() == 0;
                mark(canvas, x, head, asks ? "Quest" : "!", SPEAKER_COLOURS[speaker], false);
            } else if (level.quest != 0 && who == RunLevels.giverOf(level.quest) && run.quest() == PlusRun.DONE) {
                // And whoever is owed a visit, because what she asked is done: in gold, and it hops.
                mark(canvas, x, head - Math.abs((float) Math.sin(clock * 5)) * tile * .12f, "Done!", 0xFFE0A21A, false);
            } else if (!heard(who) && talkIndex(who) >= 0) {
                // And whoever has something new to say, when she is tapped: a little bubble with three dots.
                mark(canvas, x, head - (float) Math.sin(clock * 3 + item[1]) * tile * .05f, "…", SPEAKER_COLOURS[speaker], false);
            }
        }
        // What lies about to help, in a ring of its own colour; what a friend holds out is in her hands already.
        for (int[] item : run.items()) {
            if (item[3] != 0 || item[1] < first - 1 || item[1] > last + 1 || run.hidden(item[1], item[2])) continue;
            if (item[0] == PlusRun.LEVER || item[0] == PlusRun.PLATE || item[0] == PlusRun.SHRINE) {
                drawStanding(canvas, item);
                continue;
            }
            if (item[0] == PlusRun.INKPOT) {
                drawPot(canvas, item);
                continue;
            }
            if (item[0] == PlusRun.PLEDGE) {
                drawFriend(canvas, item);
                continue;
            }
            if (item[0] == PlusRun.LOOSE || item[0] == PlusRun.DIARY) {
                drawHomeSecret(canvas, item);
                continue;
            }
            if (item[0] == PlusRun.SPOT && item[4] >= 100) {
                if (item[4] == PlusRun.MACHINE) drawMachine(canvas, item);
                else if (item[4] == PlusRun.BOUTIQUE) drawKeeper(canvas, item);
                else if (item[4] == PlusRun.DEALER) drawDealer(canvas, item);
                else if (item[4] == PlusRun.STAIRS) drawStairs(canvas, item);
                else if (item[4] >= PlusRun.SMITH) drawSmith(canvas, item);
                else drawBoard(canvas, item);
                continue;
            }
            if (item[0] == PlusRun.SPOT) {
                // A pet on show: sitting on a pedestal of its own, its cushion in the pet's colour; one of hers hops now and then, one she has not got has its price by it.
                int kind = item[4];
                float px = (item[1] + .5f - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile;
                boolean hers = (session.tally("run_pet_own") >> kind & 1) != 0, at = run.spot() == kind;
                if (pedestals == null) pedestals = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_pedestals);
                int cells = pedestals.getWidth() / pedestals.getHeight();
                float standWide = tile * .98f, seat = standWide * PEDESTAL_SEAT;
                paint.setAlpha(255);
                source.set(Math.floorMod(kind, cells) * pedestals.getHeight(), 0, (Math.floorMod(kind, cells) + 1) * pedestals.getHeight(), pedestals.getHeight());
                target.set(px - standWide / 2, feet - standWide + tile * .03f, px + standWide / 2, feet + tile * .03f);
                canvas.drawBitmap(pedestals, source, target, paint);
                float hop = hers && (int) (clock * 1.3f + kind) % 4 == 0 ? Math.abs((float) Math.sin(clock * 6)) * tile * .12f : 0;
                cell(canvas, petArt(kind), hop > 0 ? 5 : 0, 6, px, feet - seat - hop, tile * .95f * 170 / 100 * (at ? 1.12f : 1), false);
                // Its price on a little dark tag: under the ones on the floor, and over the heads of the ones on the shelf, where the shelf can never hide it.
                String price = hers ? "hers" : "+ " + PET_COSTS[kind];
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(tile * .26f);
                text.setTypeface(Typeface.DEFAULT_BOLD);
                boolean shelf = item[2] < RunLevels.FLOOR - 1;
                float tagY = shelf ? feet - seat - tile * 1.32f - hop : feet + tile * .34f, half = text.measureText(price) / 2 + tile * .12f;
                box.set(px - half, tagY - tile * .26f, px + half, tagY + tile * .09f);
                paint.setColor(hers ? 0xD91C4A38 : 0xD93A2414);
                canvas.drawRoundRect(box, tile * .15f, tile * .15f, paint);
                paint.setColor(Color.BLACK);
                text.setColor(hers ? 0xFF9BF0C4 : 0xFFFFE7A8);
                canvas.drawText(price, px, tagY, text);
                text.setTypeface(Typeface.DEFAULT);
                continue;
            }
            float x = (item[1] + .5f - cameraX) * tile;
            float y = (item[2] + .45f - cameraY) * tile + (float) Math.sin(clock * 3.2f + item[1]) * tile * .07f;
            ring(canvas, x, y, item[0] > PlusRun.FORM && item[0] < PlusRun.CHEST ? FORM_COLOURS[item[0] - PlusRun.FORM - 1] : 0xFFFFC53D);
            help(canvas, item[0], x, y, tile * .78f);
        }
        // The things to fight with that lie about, each in a ring of light so it is not taken for part of the land.
        for (int[] arm : run.arms()) {
            if (arm[1] < first - 1 || arm[1] > last + 1) continue;
            float x = (arm[1] + .5f - cameraX) * tile;
            float y = (arm[2] + .45f - cameraY) * tile + (float) Math.sin(clock * 3.2f + arm[1]) * tile * .07f;
            boolean hers = (run.owned() >> arm[0] & 1) != 0;
            ring(canvas, x, y, hers ? 0xFFFFC53D : CalculatorLayout.CORAL);
            if (hers) paint.setAlpha(arm[0] == PlusRun.GUN || arm[0] == PlusRun.BOOK ? 255 : 120);
            thing(canvas, arm[0], x, y, tile * (arm[0] == PlusRun.GUN ? .66f : .9f) * Math.min(1, WEAPON_LONG[arm[0]] + .2f) * (hers ? .8f : 1),
                    arm[0] == PlusRun.GUN || arm[0] == PlusRun.BOOK || arm[0] >= PlusRun.TABLET ? 0 : 28, false, false);
            paint.setAlpha(255);
            if (hers) {
                // One she has already: ink for the gun, more books, or a while of being stronger: a drop, a plus, or a star on its corner.
                float cx = x + tile * .26f, cy = y - tile * .24f;
                if (arm[0] == PlusRun.GUN) pieceIn(canvas, two, DROP_ART, cx, cy, tile * .34f, 0);
                else if (arm[0] == PlusRun.BOOK) artIn(canvas, PLUS_ART, cx, cy, tile * .3f);
                else artIn(canvas, STAR_ART, x, y, tile * .5f);
            }
        }
        drawTrees(canvas, first, last);
        drawHideouts(canvas, first, last);
        int squashed = -1;
        for (PlusRun.Foe foe : run.foes()) {
            paint.setColorFilter(null);
            if (squashed >= 0) {
                canvas.restoreToCount(squashed);
                squashed = -1;
            }
            if (foe.x + foe.wide() < first - 9 || foe.x > last + 9) continue;
            float middle = (foe.x + foe.wide() / 2 - cameraX) * tile, feet = (foe.y + foe.tall() - cameraY) * tile;
            // Just hit: squashed down and wide, and pushed a little the way the blow went, springing back.
            if (foe.struck < STRUCK_SQUASH && !foe.flat) {
                float give = 1 - foe.struck / STRUCK_SQUASH, wobble = (float) Math.sin(foe.struck / STRUCK_SQUASH * Math.PI) * give;
                squashed = canvas.save();
                canvas.translate(foe.knocked * tile * .12f * give, 0);
                canvas.scale(1 + .16f * wobble, 1 - .14f * wobble, middle, feet);
            }
            // Crouched to leap up to her ledge: lit red-gold and pulsing, so that it is seen coming.
            if (foe.leaping == 1 && !foe.flat) {
                float pulse = .5f + .5f * (float) Math.sin(clock * 24);
                glowAt(canvas, middle, feet - foe.tall() * tile * .5f, foe.tall() * tile * (.8f + .2f * pulse), 0xFFFF6A3A, Math.round(150 + 90 * pulse));
            }
            if (!RunLevels.boss(foe.kind)) {
                // Gold glows faintly; black ink has a dark aura that breathes.
                if (!foe.flat && (foe.tier == PlusRun.RED_TIER || foe.tier == PlusRun.PURPLE_TIER)) glowAt(canvas, middle, feet - foe.tall() * tile * .5f, foe.tall() * tile * .95f, TIER_BARS[foe.tier], 70);
                if (!foe.flat && foe.tier == PlusRun.GOLD_TIER) glowAt(canvas, middle, feet - foe.tall() * tile * .5f, foe.tall() * tile * 1.1f, 0xFFFFD45A, 120 + (int) (40 * Math.sin(clock * 3 + foe.x)));
                if (!foe.flat && foe.tier == PlusRun.INK_TIER) glowAt(canvas, middle, feet - foe.tall() * tile * .5f, foe.tall() * tile * 1.2f, 0xFF1A1030, 170 + (int) (50 * Math.sin(clock * 2 + foe.x)));
                paint.setAlpha(255);
                paint.setColorFilter(foe.struck < STRUCK_WHITE && !foe.flat ? struckFilter() : tierFilter(foe.tier));
            }
            boolean second = (int) (clock * 6 + foe.x) % 2 != 0;
            // The pictures face left, and are turned about to go right.
            boolean turned = !foe.leftward && !foe.flat;
            if (RunLevels.boss(foe.kind)) {
                // (Those who wait are lifted to white by a blow too.)
                if (foe.struck < STRUCK_WHITE && !foe.flat) paint.setColorFilter(struckFilter());
                drawWaiting(canvas, foe, middle, feet, second);
                paint.setColorFilter(null);
            } else if (foe.kind == RunLevels.TARGET) {
                // A paper target on its string: rings of red and white, and a hole through it when it is hit.
                if (foe.flat && foe.act > .5f) continue;
                float round = foe.tall() * tile * .5f * (foe.flat ? 1 + foe.act : 1), high = feet - foe.tall() * tile * .5f;
                stroke.setColor(0xFF7A6A58);
                stroke.setAlpha(foe.flat ? 80 : 255);
                stroke.setStrokeWidth(tile * .03f);
                canvas.drawLine(middle, high - round, middle, high - round - tile * 6, stroke);
                paint.setStyle(Paint.Style.FILL);
                for (int ring = 0; ring < 4; ring++) {
                    paint.setColor(ring % 2 == 0 ? 0xFFE0362F : 0xFFFDF6EE);
                    paint.setAlpha(foe.flat ? Math.round(255 * (1 - foe.act * 2)) : 255);
                    canvas.drawCircle(middle + (float) Math.sin(clock * 2 + foe.x) * tile * .04f, high, round * (1 - ring * .25f), paint);
                }
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
                stroke.setAlpha(255);
            } else if (RunLevels.flier(foe.kind)) {
                drawFlier(canvas, foe, middle, feet, second, turned);
            } else if (RunUniques.is(foe.kind)) {
                drawUnique(canvas, foe, middle, feet, turned);
            } else if (foe.kind >= RunLevels.SLIME) {
                // The signs of the second volume: two pictures of each about its business, and two of its going, after which it fades.
                int which = SIGN_ART + (foe.kind - RunLevels.SLIME) * 4 + (foe.flat ? (foe.act < .22f ? 2 : 3) : second ? 1 : 0);
                if (foe.flat && foe.act > 1f) continue;
                if (foe.flat) paint.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (1f - foe.act) / .4f))));
                else if (foe.dazed > 0 && (int) (clock * 16) % 2 == 0) paint.setAlpha(120);
                boolean stretched = foe.kind == RunLevels.BAR;
                if (stretched) piece(canvas, two, which, middle, feet + tile * .06f, foe.wide() * tile * 1.25f, turned);
                else pieceTall(canvas, two, which, middle, feet + tile * .04f, foe.tall() * tile * 1.3f, turned);
                paint.setAlpha(255);
            } else if (foe.kind == RunLevels.MINUS || foe.kind == RunLevels.THROWER) {
                boolean walking = foe.kind == RunLevels.MINUS && second;
                // (Down, it lies flat a moment and then fades, as everything beaten does.)
                if (foe.flat && foe.act > 1f) continue;
                if (foe.flat) paint.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (1f - foe.act) / .4f))));
                if (!foe.flat && foe.dazed > 0 && (int) (clock * 16) % 2 == 0) paint.setAlpha(120);
                art(canvas, foe.flat ? MINUS_FLAT_ART : MINUS_ART + (walking ? 1 : 0), middle, feet, foe.wide() * tile * 1.3f, turned);
                paint.setAlpha(255);
                // One that throws knives holds the next one: up, when it is about to throw it.
                if (foe.kind == RunLevels.THROWER && !foe.flat && foe.wait < PlusRun.WARNING + 1) {
                    thing(canvas, PlusRun.KNIFE, middle + (foe.leftward ? -1 : 1) * tile * .42f, feet - tile * .22f, tile * .6f,
                            foe.wait < PlusRun.WARNING ? -8 : 72, foe.leftward, true);
                }
            } else if (foe.kind == RunLevels.TIMES && foe.flat) {
                // A times beaten: knocked into the air with its eyes spinning, turning over, and then flat on its back, cracked, its spikes
                // limp; and after a moment it fades.
                if (foe.act > 1.3f) continue;
                if (timesDown == null) timesDown = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_times_down);
                float up = Math.min(1, foe.act / .45f), tall = foe.tall() * tile * 1.55f * 124 / 110;
                paint.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (1.3f - foe.act) / .4f))));
                canvas.save();
                if (up < 1) canvas.rotate((foe.leftward ? 1 : -1) * 200 * up, middle, feet - tall * .4f - (float) Math.sin(up * Math.PI) * tile * 1.1f);
                cell(canvas, timesDown, up < 1 ? 0 : 1, 2, middle, feet + tile * .05f - (up < 1 ? (float) Math.sin(up * Math.PI) * tile * 1.1f : 0), tall, false);
                canvas.restore();
                paint.setAlpha(255);
            } else if (foe.kind == RunLevels.TIMES) {
                if (foe.dazed > 0 && (int) (clock * 16) % 2 == 0) paint.setAlpha(120);
                art(canvas, TIMES_ART + (second ? 1 : 0), middle, feet, foe.wide() * tile * 1.25f, turned);
                paint.setAlpha(255);
            } else {
                if (foe.flat && foe.act > 1f) continue;
                if (foe.flat) paint.setAlpha(Math.round(110 * Math.max(0, Math.min(1, (1f - foe.act) / .4f))));
                // Low on the ground and stretched in the air.
                artTall(canvas, DIVIDE_ART + (foe.ground - foe.y > .3f ? 1 : 0), middle, feet, foe.tall() * tile * (foe.flat ? .6f : 1.5f), false);
                paint.setAlpha(255);
                // One that shoots carries a gun of its own.
                if (foe.kind == RunLevels.GUNNER && !foe.flat) {
                    thing(canvas, PlusRun.GUN, middle + (foe.leftward ? -1 : 1) * tile * .5f, feet - foe.tall() * tile * .55f, tile * .5f, 0,
                            foe.leftward, false);
                }
            }
        }
        if (squashed >= 0) canvas.restoreToCount(squashed);
        paint.setColorFilter(null);
        drawShields(canvas, first, last);
        drawRoomOver(canvas);
        drawForeground(canvas);
        drawHits(canvas, first, last);
        drawMoveFx(canvas);
        // How tough a creature is: one that takes more than a blow has a mark over it for each it still takes, so that it is seen to be going.
        for (PlusRun.Foe foe : run.foes()) {
            if (foe.flat || foe.most <= PlusRun.BLOW || foe.left >= foe.most || RunLevels.boss(foe.kind) || foe.kind == RunLevels.TARGET || foe.x + foe.wide() < first - 1 || foe.x > last + 1) continue;
            float middle = (foe.x + foe.wide() / 2 - cameraX) * tile, over = (foe.y - cameraY) * tile - tile * .22f;
            float wide = Math.max(tile * .9f, foe.wide() * tile), high = tile * .11f, part = Math.max(0, foe.left / (float) foe.most);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xCC15171A);
            canvas.drawRoundRect(middle - wide / 2 - tile * .03f, over - high / 2 - tile * .03f, middle + wide / 2 + tile * .03f, over + high / 2 + tile * .03f, high, high, paint);
            paint.setColor(TIER_BARS[foe.tier]);
            canvas.drawRoundRect(middle - wide / 2, over - high / 2, middle - wide / 2 + wide * part, over + high / 2, high / 2, high / 2, paint);
            if (foe.struck < .2f) {
                paint.setColor(Color.WHITE);
                paint.setAlpha(Math.round(255 * (1 - foe.struck / .2f)));
                canvas.drawRoundRect(middle - wide / 2, over - high / 2, middle + wide / 2, over + high / 2, high / 2, high / 2, paint);
                paint.setAlpha(255);
            }
            paint.setColor(Color.BLACK);
        }
        // Whatever is in the air.
        for (PlusRun.Shot shot : run.shots()) {
            float x = (shot.x - cameraX) * tile, y = (shot.y - cameraY) * tile;
            boolean leftward = shot.across < 0;
            float spin = shot.age * 620;
            // What falls straight down throws a shadow on the ground it will land on, which grows as it comes.
            if (shot.kind == PlusRun.STOP && !shot.hers && shot.across == 0 && run.room() < 0) {
                int under = RunLevels.top(level, Math.max(0, Math.min(level.columns - 1, (int) shot.x)));
                if (under > 0 && under > shot.y) {
                    float near = Math.max(0, 1 - (under - shot.y) / 6f);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.BLACK);
                    paint.setAlpha(Math.round(50 + 110 * near));
                    canvas.drawOval(x - tile * (.2f + .3f * near), (under - cameraY) * tile - tile * .09f, x + tile * (.2f + .3f * near), (under - cameraY) * tile + tile * .07f, paint);
                    paint.setAlpha(255);
                }
            }
            if (shot.kind == PlusRun.PAGE || shot.kind == PlusRun.THROWN_KEY) {
                drawFlick(canvas, shot, x, y);
                continue;
            }
            if (shot.kind == PlusRun.SLASH) {
                drawSlash(canvas, shot, x, y);
                continue;
            }
            if (shot.kind == PlusRun.INKBALL) {
                // A ball of the cannon's ink: the great one as big as her head, wobbling as it goes.
                float round = tile * (shot.high > 0 ? .62f : .26f) * (1 + .06f * (float) Math.sin(shot.age * 30));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF141A4A);
                canvas.drawCircle(x, y, round, paint);
                paint.setColor(0xFF2A3FC8);
                canvas.drawCircle(x - round * .12f, y - round * .12f, round * .78f, paint);
                paint.setColor(0xFFB8C8FF);
                canvas.drawCircle(x - round * .34f, y - round * .36f, round * .2f, paint);
                paint.setColor(Color.BLACK);
                continue;
            }
            if (shot.pets && shot.kind <= PlusRun.GUN) {
                // A small one of what she holds, from the one that copies her.
                if (shot.kind == PlusRun.GUN) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFF2A3FC8);
                    canvas.drawCircle(x, y, tile * .08f, paint);
                    paint.setColor(Color.BLACK);
                } else {
                    thing(canvas, shot.kind, x, y, tile * .34f, shot.kind == PlusRun.BOOK ? spin : 0, leftward, false);
                }
                continue;
            }
            switch (shot.kind) {
                case PlusRun.ROLL: {
                    canvas.save();
                    canvas.rotate(spin, x, y);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFFD9962F);
                    canvas.drawOval(x - tile * .2f, y - tile * .14f, x + tile * .2f, y + tile * .14f, paint);
                    paint.setColor(0xFFF3C56B);
                    canvas.drawOval(x - tile * .12f, y - tile * .1f, x + tile * .06f, y, paint);
                    stroke.setColor(0xFF8A5A1E);
                    stroke.setStrokeWidth(tile * .03f);
                    canvas.drawOval(x - tile * .2f, y - tile * .14f, x + tile * .2f, y + tile * .14f, stroke);
                    paint.setColor(Color.BLACK);
                    canvas.restore();
                    break;
                }
                case PlusRun.RAY: {
                    float length = (float) Math.hypot(shot.across, shot.down), ax = shot.across / length, ay = shot.down / length;
                    stroke.setColor(0xFFFFE9A0);
                    stroke.setStrokeWidth(tile * .2f);
                    stroke.setAlpha(120);
                    canvas.drawLine(x - ax * tile * .7f, y - ay * tile * .7f, x + ax * tile * .2f, y + ay * tile * .2f, stroke);
                    stroke.setColor(Color.WHITE);
                    stroke.setStrokeWidth(tile * .08f);
                    stroke.setAlpha(255);
                    canvas.drawLine(x - ax * tile * .6f, y - ay * tile * .6f, x + ax * tile * .2f, y + ay * tile * .2f, stroke);
                    break;
                }
                case PlusRun.HEART: {
                    float beat = 1 + .12f * (float) Math.sin(clock * 14);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFFFF8FCB);
                    paint.setAlpha(110);
                    canvas.drawCircle(x, y, tile * .34f * beat, paint);
                    paint.setColor(Color.BLACK);
                    paint.setAlpha(255);
                    artIn(canvas, HEART_ART, x, y, tile * .46f * beat);
                    break;
                }
                case PlusRun.GUN:
                case PlusRun.DOT:
                case PlusRun.GLOB: {
                    if (!shot.hers && number == RunLevels.TOTAL && shot.kind != PlusRun.GUN) {
                        // What Lucy sends: water from her gun, a drop of it; and a water balloon.
                        if (shot.kind == PlusRun.DOT) {
                            piece(canvas, two, DROP_ART, x, y + tile * .25f, tile * .5f, leftward);
                        } else {
                            paint.setStyle(Paint.Style.FILL);
                            paint.setColor(0xFF4FB7E8);
                            canvas.drawOval(x - tile * .26f, y - tile * .3f, x + tile * .26f, y + tile * .3f, paint);
                            paint.setColor(Color.WHITE);
                            paint.setAlpha(170);
                            canvas.drawCircle(x - tile * .09f, y - tile * .12f, tile * .07f, paint);
                            paint.setAlpha(255);
                            stroke.setColor(0xFF2B2440);
                            stroke.setStrokeWidth(tile * .035f);
                            canvas.drawOval(x - tile * .26f, y - tile * .3f, x + tile * .26f, y + tile * .3f, stroke);
                            paint.setColor(Color.BLACK);
                        }
                        break;
                    }
                    float round = tile * (shot.kind == PlusRun.GUN ? .13f : shot.kind == PlusRun.DOT ? .19f : .26f);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(shot.kind == PlusRun.GUN ? 0xFF2A3FC8 : shot.kind == PlusRun.GLOB ? 0xFF58C99A : shot.hers ? CalculatorLayout.CORAL : 0xFF2FB5A8);
                    canvas.drawCircle(x - (leftward ? -1 : 1) * round * 1.3f, y, round * .6f, paint);
                    canvas.drawCircle(x, y, round, paint);
                    stroke.setColor(0xFF2B2440);
                    stroke.setStrokeWidth(tile * .035f);
                    canvas.drawCircle(x, y, round, stroke);
                    paint.setColor(Color.BLACK);
                    break;
                }
                case PlusRun.PLUSDROP:
                    art(canvas, PLUS_ART, x, y + tile * .36f, tile * .72f * Math.max(.25f, Math.abs((float) Math.cos(shot.age * 7))), false);
                    break;
                case PlusRun.BOOK:
                    thing(canvas, PlusRun.BOOK, x, y, tile * .58f, spin, leftward, false);
                    break;
                case PlusRun.FIRE:
                    piece(canvas, two, FIRE_ART, x, y + tile * .3f, tile * .7f, leftward);
                    break;
                case PlusRun.WATER:
                    piece(canvas, two, DROP_ART, x, y + tile * .25f, tile * .55f, leftward);
                    break;
                case PlusRun.BLOT_SHOT: {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFF27338F);
                    canvas.drawCircle(x, y, tile * .2f, paint);
                    canvas.drawCircle(x - (leftward ? -1 : 1) * tile * .22f, y, tile * .11f, paint);
                    paint.setColor(Color.WHITE);
                    paint.setAlpha(170);
                    canvas.drawCircle(x - tile * .06f, y - tile * .07f, tile * .05f, paint);
                    paint.setColor(Color.BLACK);
                    paint.setAlpha(255);
                    break;
                }
                case PlusRun.NUT: {
                    canvas.save();
                    canvas.rotate(spin, x, y);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFF8A5A2B);
                    canvas.drawOval(x - tile * .16f, y - tile * .2f, x + tile * .16f, y + tile * .2f, paint);
                    paint.setColor(0xFFC89A5E);
                    canvas.drawOval(x - tile * .07f, y - tile * .16f, x + tile * .03f, y + tile * .02f, paint);
                    paint.setColor(Color.BLACK);
                    canvas.restore();
                    break;
                }
                case PlusRun.STEAM: {
                    // Fire and water thrown as one: a cloud of steam, rolling, with the flame and the drop in it.
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.WHITE);
                    for (int puff = 0; puff < 5; puff++) {
                        double turn = shot.age * 5 + puff * 1.26;
                        paint.setAlpha(150 + 20 * puff);
                        canvas.drawCircle(x + (float) Math.cos(turn) * tile * .22f - (leftward ? -1 : 1) * puff * tile * .09f, y + (float) Math.sin(turn) * tile * .16f,
                                tile * (.3f - puff * .03f), paint);
                    }
                    paint.setColor(Color.BLACK);
                    paint.setAlpha(255);
                    piece(canvas, two, FIRE_ART, x - tile * .12f, y + tile * .22f, tile * .5f, leftward);
                    piece(canvas, two, DROP_ART, x + tile * .16f, y + tile * .2f, tile * .36f, leftward);
                    break;
                }
                case PlusRun.FEATHER:
                    pieceIn(canvas, two, FEATHER_ART, x, y, tile * .6f, (float) Math.toDegrees(Math.atan2(shot.down, shot.across)) + 180);
                    break;
                case PlusRun.STOP:
                    pieceIn(canvas, two, STOP_ART, x, y, tile * .42f, 0);
                    break;
                case PlusRun.SAYING:
                    pieceIn(canvas, two, SAYING_ART, x, y, tile * .7f, 0);
                    break;
                case PlusRun.SCROLL: {
                    // The gazette, rolled up, turning over as it comes.
                    canvas.save();
                    canvas.rotate(spin, x, y);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFFF4EBD2);
                    canvas.drawRoundRect(x - tile * .32f, y - tile * .1f, x + tile * .32f, y + tile * .1f, tile * .08f, tile * .08f, paint);
                    stroke.setColor(0xFF5A4A3A);
                    stroke.setStrokeWidth(tile * .035f);
                    canvas.drawRoundRect(x - tile * .32f, y - tile * .1f, x + tile * .32f, y + tile * .1f, tile * .08f, tile * .08f, stroke);
                    canvas.restore();
                    paint.setColor(Color.BLACK);
                    break;
                }
                case PlusRun.SHOCK:
                    piece(canvas, three, SHOCK_ART, x, y + tile * .32f, tile * 1.5f, !leftward);
                    break;
                case PlusRun.BOOMERANG:
                    if (number == RunLevels.TOTAL) {
                        // Lucy's swim ring, in red and white by turns, turning as it goes.
                        box.set(x - tile * .42f, y - tile * .42f, x + tile * .42f, y + tile * .42f);
                        stroke.setStrokeWidth(tile * .2f);
                        for (int part = 0; part < 8; part++) {
                            stroke.setColor(part % 2 == 0 ? 0xFFE0453A : Color.WHITE);
                            canvas.drawArc(box, spin * 1.4f + part * 45, 45, false, stroke);
                        }
                        stroke.setColor(0xFF2B2440);
                        stroke.setStrokeWidth(tile * .03f);
                        canvas.drawCircle(x, y, tile * .52f, stroke);
                        canvas.drawCircle(x, y, tile * .32f, stroke);
                    } else {
                        pieceIn(canvas, three, BAT_ART, x, y, tile * 1.05f, spin * 1.4f);
                    }
                    break;
                case PlusRun.DEBRIS: {
                    // Where it will come down is marked on whatever is under it for as long as it hangs.
                    if (shot.age < PlusRun.HANGS) {
                        int column = (int) Math.floor(shot.x), row = (int) Math.floor(shot.y);
                        while (row < run.level().land.length && !PlusRun.solid(run.at(column, row))) row++;
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(0xFFE0362F);
                        paint.setAlpha(Math.round(70 + 90 * Math.abs((float) Math.sin(clock * 18))));
                        canvas.drawOval(x - tile * .55f, (row - cameraY) * tile - tile * .1f, x + tile * .55f, (row - cameraY) * tile + tile * .1f, paint);
                        paint.setColor(Color.BLACK);
                        paint.setAlpha(255);
                    }
                    pieceIn(canvas, three, RUBBLE_ART, x + (shot.age < PlusRun.HANGS ? (float) Math.sin(clock * 50) * tile * .04f : 0), y, tile * .95f, 0);
                    break;
                }
                case PlusRun.BLADE:
                    pieceIn(canvas, three, BLADE_ART, x, y, tile * .7f, spin * 1.2f);
                    break;
                case PlusRun.DARK:
                    piece(canvas, three, DARK_ART, x, y + tile * .38f, tile * .75f, false);
                    break;
                case PlusRun.CURSE:
                    pieceIn(canvas, two, RIBBON_ART + PlusRun.BLACK - 1, x, y, tile * .7f, spin * .5f);
                    break;
                default:
                    if (shot.kind <= PlusRun.GUN) thing(canvas, shot.kind, x, y, tile * WEAPON_LONG[shot.kind], 90, leftward, false);
            }
        }
        paint.setStyle(Paint.Style.FILL);
        for (float[] bit : bits) {
            paint.setColor(bit[5] == 0 ? CalculatorLayout.CORAL : bit[5] == 2 ? 0xFF27348F : bit[5] > 2 ? FORM_COLOURS[(int) bit[5] - 3] : 0xFFDDE3E8);
            paint.setAlpha(Math.round(255 * Math.min(1, bit[4] / .2f)));
            canvas.drawCircle((bit[0] - cameraX) * tile, (bit[1] - cameraY) * tile, tile * (bit[5] == 0 ? .09f : .13f), paint);
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        drawChest(canvas);
        drawPanel(canvas);
        drawPetThings(canvas);
        drawPet(canvas);
        // (Going up in the teleporter's light she fades away; coming down in it, she comes back.)
        float gone = pull(), eased = gone * gone * (3 - 2 * gone);
        if (gone > 0) {
            float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() + PlusRun.TALL / 2 - cameraY) * tile;
            boolean atPortal = warpAge < WARP || arrivedAtGate;
            float toX = atPortal ? portalX() : herX, toY = atPortal ? portalY() : herY;
            canvas.saveLayerAlpha(0, 0, width, height, Math.round(255 * (1 - gone * gone)));
            canvas.translate((toX - herX) * eased, (toY - herY) * eased);
            canvas.rotate(eased * 540, herX, herY);
            canvas.scale(1 - eased * .85f, 1 - eased * .85f, herX, herY);
        }
        drawHer(canvas);
        if (gone > 0) canvas.restore();
        // What has just been won, rising from where it was won.
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(tile * .42f);
        for (int index = 0; index < pops.size(); index++) {
            float[] pop = pops.get(index);
            float x = (pop[0] - cameraX) * tile, y = (pop[1] - pop[2] * 1.4f - cameraY) * tile;
            // (All of it on the screen, however near the edge it rose.)
            float half = text.measureText(popWords.get(index)) / 2 + dp(6);
            if (half * 2 < width) x = Math.max(half, Math.min(width - half, x));
            int alpha = Math.round(255 * Math.min(1, (1.1f - pop[2]) / .3f));
            text.setStyle(Paint.Style.STROKE);
            text.setStrokeWidth(tile * .1f);
            text.setColor(Color.WHITE);
            text.setAlpha(alpha);
            canvas.drawText(popWords.get(index), x, y, text);
            text.setStyle(Paint.Style.FILL);
            text.setColor(CalculatorLayout.CORAL);
            text.setAlpha(alpha);
            canvas.drawText(popWords.get(index), x, y, text);
        }
        text.setAlpha(255);
        canvas.restore();
        // Gari's bars, over what there is to see.
        if (run.censored() > 0) {
            paint.setAlpha(Math.round(255 * Math.min(1, run.censored() / .4f)));
            for (int bar = 0; bar < 3; bar++) {
                float y = height * (.26f + bar * .2f) + (float) Math.sin(clock * 2 + bar) * dp(6);
                target.set(-dp(10), y, width + dp(10), y + height * .09f);
                Bitmap bitmap = three.get();
                int cell = bitmap.getWidth() / three.across;
                source.set(BAR_ART % 4 * cell + cell / 16, BAR_ART / 4 * cell + cell * 3 / 4, BAR_ART % 4 * cell + cell - cell / 16, BAR_ART / 4 * cell + cell);
                canvas.drawBitmap(bitmap, source, target, paint);
            }
            paint.setAlpha(255);
        }
        // A flash when her hair turns, or when someone changes.
        if (flash > 0) canvas.drawColor(Color.argb(Math.round(120 * Math.min(1, flash / .3f)), 255, 255, 255));
    }

    /** The inside of a place off the land, behind everything in it: one picture, side by side with itself turned about, drifting a little as she goes. */
    private void drawInside(Canvas canvas) {
        if (drawRoomBack(canvas)) return;
        int look = look();
        Bitmap inside;
        if (look < 0) {
            if (this.inside == null) this.inside = BitmapFactory.decodeResource(context.getResources(), R.drawable.plus_run_room);
            inside = this.inside;
        } else {
            // One look's picture is kept at a time: the next place's takes its room.
            if (lookOf != look) {
                if (lookArt != null) lookArt.recycle();
                lookArt = BitmapFactory.decodeResource(context.getResources(), LOOK_ART[look]);
                lookOf = look;
            }
            inside = lookArt;
        }
        // A cellar's picture fills the screen. One of the eight is as tall as its place: its wall, with what stands along it, from
        // the ceiling to the floor, and the picture's own floor out of sight under the floor she walks on; and it goes by as she does.
        float tall = look < 0 ? height : tile * RunLevels.ROOM_TALL / .66f, top = look < 0 ? 0 : (run.level().rooms.get(run.room())[12] - cameraY) * tile;
        float wide = tall * (float) inside.getWidth() / inside.getHeight(), drift = cameraX * tile * (look < 0 ? .15f : .55f) % (wide * 2);
        // (A building of a village is small: its picture stands still behind it, its middle in the room's middle, and goes by with the room.)
        if (look >= RunLevels.LOOKS) {
            int[] in = run.level().rooms.get(run.room());
            drift = wide / 2 - ((in[3] + in[4] + 1) / 2f - cameraX) * tile;
        }
        if (look >= 0) canvas.drawColor(LOOK_WALL[look]);
        paint.setAlpha(255);
        for (int copy = 0; copy * wide - drift < width; copy++) {
            float left = copy * wide - drift;
            target.set(left - 1, top, left + wide + 1, top + tall);
            canvas.save();
            if (copy % 2 != 0) canvas.scale(-1, 1, left + wide / 2, 0);
            canvas.drawBitmap(inside, null, target, paint);
            canvas.restore();
        }
    }

    /** One colour with so much of another in it. */
    static int mixed(int one, int other, float much) {
        return Color.rgb(Math.round(Color.red(one) + (Color.red(other) - Color.red(one)) * much), Math.round(Color.green(one) + (Color.green(other) - Color.green(one)) * much),
                Math.round(Color.blue(one) + (Color.blue(other) - Color.blue(one)) * much));
    }

    /** The look of the place she is in: one of the eight, or -1 for a cellar, which has its own. */
    private int look() {
        if (run == null || run.room() < 0) return -1;
        int[] in = run.level().rooms.get(run.room());
        return in.length > 13 ? in[13] : -1;
    }

    /**
     * What is only in the places off a land. A locked door: boards banded with iron and a
     * keyhole, which swings away when its key is taken. A bridge not yet raised: its
     * planks in outline. Bars: iron, which lift into the wall over them when a plate is
     * stood on, and blink before they come down.
     */
    private void drawFixture(Canvas canvas, char what, char now, int column, int row, float left, float y) {
        paint.setStyle(Paint.Style.FILL);
        if (what == RunLevels.SPAN) {
            stroke.setColor(Color.WHITE);
            stroke.setAlpha(Math.round(70 + 40 * (float) Math.sin(clock * 3 + column)));
            stroke.setStrokeWidth(tile * .04f);
            canvas.drawLine(left + tile * .1f, y + tile * .12f, left + tile * .5f, y + tile * .12f, stroke);
            canvas.drawLine(left + tile * .7f, y + tile * .12f, left + tile * .95f, y + tile * .12f, stroke);
            stroke.setAlpha(255);
        } else if (what == RunLevels.LOCKED) {
            if (now == RunLevels.AIR) return;
            paint.setColor(0xFF7A4A2A);
            canvas.drawRect(left, y, left + tile + 1, y + tile + 1, paint);
            paint.setColor(0xFF4A2C1A);
            canvas.drawRect(left + tile * .46f, y, left + tile * .54f, y + tile + 1, paint);
            paint.setColor(0xFF8C8C96);
            canvas.drawRect(left, y + tile * .12f, left + tile + 1, y + tile * .24f, paint);
            canvas.drawRect(left, y + tile * .76f, left + tile + 1, y + tile * .88f, paint);
            // Its lock, on the lower of its two squares, where the two columns meet.
            if (run.laid(column, row + 1) != RunLevels.LOCKED && run.laid(column - 1, row) == RunLevels.LOCKED) {
                paint.setColor(0xFFE0B13A);
                canvas.drawCircle(left, y + tile * .2f, tile * .26f, paint);
                paint.setColor(0xFF2B2440);
                canvas.drawCircle(left, y + tile * .14f, tile * .08f, paint);
                canvas.drawRect(left - tile * .04f, y + tile * .14f, left + tile * .04f, y + tile * .34f, paint);
            }
        } else {
            // Bars: down, or up in the wall; with a second and a half left they blink.
            float left_ = run.barsLeft();
            boolean up = now == RunLevels.AIR;
            if (up && left_ < 1.5f && (int) (clock * 8) % 2 == 0) up = false;
            float lift = up ? tile * 1.7f : 0;
            paint.setColor(0xFF9AA0AE);
            for (int bar = 0; bar < 3; bar++) {
                float x = left + tile * (.2f + bar * .3f);
                canvas.drawRect(x - tile * .05f, y - lift, x + tile * .05f, y + tile + 1 - lift, paint);
            }
            paint.setColor(0xFF6E7482);
            canvas.drawRect(left, y + tile * .4f - lift, left + tile + 1, y + tile * .5f - lift, paint);
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
    }

    /** A lever, a plate or a shrine: they stand where they are, and are drawn as what they are. */
    private void drawStanding(Canvas canvas, int[] item) {
        float x = (item[1] + .5f - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile;
        paint.setStyle(Paint.Style.FILL);
        if (item[0] == PlusRun.LEVER) {
            boolean pulled = run.pulled(item[1]);
            paint.setColor(0xFF6E7482);
            canvas.drawRoundRect(x - tile * .3f, feet - tile * .2f, x + tile * .3f, feet, tile * .08f, tile * .08f, paint);
            stroke.setColor(0xFF4A2C1A);
            stroke.setStrokeWidth(tile * .09f);
            float lean = pulled ? .32f : -.32f;
            canvas.drawLine(x, feet - tile * .15f, x + tile * lean, feet - tile * .7f, stroke);
            paint.setColor(pulled ? 0xFF2E9E6E : 0xFFE0483A);
            canvas.drawCircle(x + tile * lean, feet - tile * .72f, tile * .13f, paint);
            if (!pulled) mark(canvas, x, feet - tile * 1.25f + (float) Math.sin(clock * 4) * tile * .05f, "Lever", 0xFFE0483A, false);
        } else if (item[0] == PlusRun.PLATE) {
            boolean down = run.barsLeft() > 0;
            paint.setColor(0xFF6E7482);
            canvas.drawRoundRect(x - tile * .42f, feet - tile * (down ? .05f : .12f), x + tile * .42f, feet, tile * .05f, tile * .05f, paint);
            paint.setColor(down ? 0xFF2E9E6E : 0xFFE0A21A);
            canvas.drawRoundRect(x - tile * .32f, feet - tile * (down ? .09f : .2f), x + tile * .32f, feet - tile * (down ? .04f : .1f), tile * .05f, tile * .05f, paint);
            if (down) {
                // How much longer the bars stay up: a ring over the plate that runs down.
                stroke.setColor(0xFF2E9E6E);
                stroke.setStrokeWidth(tile * .08f);
                box.set(x - tile * .28f, feet - tile * 1.3f, x + tile * .28f, feet - tile * .74f);
                canvas.drawArc(box, -90, 360 * run.barsLeft() / PlusRun.BARS_OPEN, false, stroke);
            }
        } else {
            // A shrine: a small stone house with a ribbon of its colour hung in it, and what it asks over it.
            int colour = FORM_COLOURS[Math.max(0, Math.min(FORM_COLOURS.length - 1, item[4] - 1))];
            paint.setColor(0xFF8C8C96);
            canvas.drawRoundRect(x - tile * .5f, feet - tile * 1.1f, x + tile * .5f, feet, tile * .1f, tile * .1f, paint);
            paint.setColor(0xFF6E7482);
            path.reset();
            path.moveTo(x - tile * .66f, feet - tile * 1.05f);
            path.lineTo(x, feet - tile * 1.6f);
            path.lineTo(x + tile * .66f, feet - tile * 1.05f);
            path.close();
            canvas.drawPath(path, paint);
            paint.setColor(0xFF2B2440);
            canvas.drawRoundRect(x - tile * .3f, feet - tile * .85f, x + tile * .3f, feet - tile * .1f, tile * .2f, tile * .2f, paint);
            paint.setColor(colour);
            paint.setAlpha(Math.round(120 + 60 * (float) Math.sin(clock * 3)));
            canvas.drawCircle(x, feet - tile * .5f, tile * .3f, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            pieceIn(canvas, two, RIBBON_ART + item[4] - 1, x, feet - tile * .5f, tile * .5f, 0);
            artIn(canvas, HEART_ART, x - tile * .22f, feet - tile * 1.95f, tile * .34f);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(tile * .3f);
            text.setColor(Color.WHITE);
            canvas.drawText("for it", x, feet - tile * 1.85f, text);
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        stroke.setAlpha(255);
    }

    /**
     * The doors of the land and of the places off it. A hidden one is a crack in the wall
     * until she has broken through to it; a cellar's is low and banded with iron. Over the
     * one she stands at is what the button over the jump will do.
     */
    private void drawDoors(Canvas canvas, RunLevels.Level level) {
        int at = run.door(), in = run.room();
        for (int index = 0; index < level.rooms.size(); index++) {
            int[] one = level.rooms.get(index);
            boolean known = one[7] != 1 || (run.revealed() & 1 << index) != 0, been = (run.roomsFound() & 1 << index) != 0;
            int picture = one[0] == RunLevels.ARENA ? CELLAR_PICTURE : DOOR_PICTURE;
            if (in < 0 && one[0] == RunLevels.HOUSE) {
                // A building of a village: its door is the one it is drawn with. Over it, what it is; and, at it, the word.
                float x = (one[1] + .5f - cameraX) * tile, y = (one[2] - cameraY) * tile;
                if (x > -tile * 3 && x < width + tile * 3) {
                    if (at == index) mark(canvas, x, y - tile * 2.05f, "Enter", 0xFF8A5A33, false);
                    else doorMark(canvas, 100 + one[14], one[1], one[2], false, 0);
                }
            } else if (in < 0) {
                // A place hidden behind a crack is not to be seen until a blow breaks it open: at most a hairline crack, close by.
                if (!known) {
                    hairline(canvas, one[1], one[2], one[1] + .5f, one[2] - 1);
                    continue;
                }
                door(canvas, picture, one[1], one[2], at == index, "Enter");
                // Where a passage comes out looks like a crack, until she has come out of it.
                if (one[5] != one[1]) door(canvas, been ? DOOR_PICTURE : CRACK_PICTURE, one[5], one[9], at == index, "Enter");
                // Found, a door has a mark over it that says what kind of place it leads to: and a tick, once that is done with.
                boolean found = been || (session.tally("run_rooms_" + number) & 1 << index) != 0 || known && one[7] != 1;
                boolean done = (run.roomsDone() & 1 << index) != 0;
                // (A stash she has not yet been into says only that it is something: a question mark. Been into, it says what it holds.)
                boolean inside = been || (session.tally("run_rooms_" + number) & 1 << index) != 0;
                if (found && at != index) doorMark(canvas, one[0] == RunLevels.STASH && !inside ? -1 : one[0], one[1], one[2], done, one.length > 14 ? one[14] : 0);
            } else if (in == index) {
                // Shut until what happens in it is over, its door is dim.
                if (run.sealed()) paint.setAlpha(110);
                door(canvas, picture, one[6], one[11], at == index, "Leave");
                if (one[8] >= 0) door(canvas, DOOR_PICTURE, one[8], one[11], at == index, "Leave");
                paint.setAlpha(255);
            }
        }
    }

    /** The one hint a secret space gives: the false wall's middle square, a hairline crack in it, only when she is near. */
    private void drawSecretHints(Canvas canvas) {
        java.util.List<int[]> secrets = run.secrets();
        for (int at = 0; at < secrets.size(); at++) {
            if (run.secretFound(at)) continue;
            int[] one = secrets.get(at);
            int row = (one[1] + one[2]) / 2;
            hairline(canvas, one[0], row + 1, one[0] + .5f, row + .5f);
        }
    }

    /**
     * A hairline crack in one square, faint, if she is within a few squares of where it hints at: a thin dark line, broken twice, that
     * glints now and then. Never the shape of what it hides.
     */
    private void hairline(Canvas canvas, int column, int floor, float nearX, float nearY) {
        float far = (float) Math.hypot(run.x() + PlusRun.WIDE / 2 - nearX, run.y() + PlusRun.TALL / 2 - nearY);
        if (far > PlusRun.HINT_NEAR) return;
        float fade = Math.min(1, (PlusRun.HINT_NEAR - far) / 1.2f), left = (column - cameraX) * tile, top = (floor - 1 - cameraY) * tile;
        stroke.setColor(0xFF1A1210);
        stroke.setAlpha(Math.round(80 * fade));
        stroke.setStrokeWidth(Math.max(1, tile * .025f));
        float ax = left + tile * .42f, ay = top + tile * .2f, bx = left + tile * .55f, by = top + tile * .45f, cx = left + tile * .47f, cy = top + tile * .72f;
        canvas.drawLine(ax, ay, bx, by, stroke);
        canvas.drawLine(bx, by, cx, cy, stroke);
        // (A glint along it, now and then.)
        float glint = (clock * .6f + column * .37f) % 3;
        if (glint < .4f) glowAt(canvas, bx, by, tile * .12f, 0xFFFFF4D0, Math.round(120 * fade * (float) Math.sin(glint / .4f * Math.PI)));
        stroke.setAlpha(255);
    }

    private void door(Canvas canvas, int picture, int column, int floor, boolean here, String word) {
        float x = (column + .5f - cameraX) * tile, y = (floor - cameraY) * tile;
        if (x < -tile * 2 || x > width + tile * 2) return;
        pieceTall(canvas, four, picture, x, y, tile * 1.85f, false);
        if (here && Math.abs(run.x() + PlusRun.WIDE / 2 - column - .5f) < .9f) mark(canvas, x, y - tile * 2.05f, word, 0xFF8A5A33, false);
    }

    /**
     * The mark over a door that has been found, by the kind of place behind it: a chest for
     * a stash, two arrows for a passage, a sword for a cellar, and a star for a game. Once
     * the place is done with it is pale, with a tick on it.
     */
    private void doorMark(Canvas canvas, int kind, int column, int floor, boolean done, int holds) {
        float x = (column + .5f - cameraX) * tile, y = (floor - cameraY) * tile - tile * 2.15f, round = tile * .3f;
        if (x < -tile * 2 || x > width + tile * 2) return;
        y += (float) Math.sin(clock * 2.4f + column) * tile * .04f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33000000);
        canvas.drawCircle(x, y + tile * .03f, round, paint);
        paint.setColor(cardColor);
        paint.setAlpha(done ? 150 : 255);
        canvas.drawCircle(x, y, round, paint);
        int colour = kind == RunLevels.ARENA ? 0xFFC0392B : kind == RunLevels.PASSAGE ? 0xFF3B7BE0 : kind <= RunLevels.STASH ? 0xFF8A5A33 : 0xFFE0A21A;
        stroke.setColor(colour);
        stroke.setAlpha(done ? 110 : 255);
        stroke.setStrokeWidth(tile * .045f);
        canvas.drawCircle(x, y, round, stroke);
        paint.setColor(Color.BLACK);
        paint.setAlpha(done ? 110 : 255);
        if (kind >= 100) {
            // A building: a paw for the pet shop, a quill for the guild hall, a roof for a home, a hammer for the smithy, a moon for the black market.
            int sort = kind - 100;
            paint.setColor(0xFF8A5A33);
            stroke.setColor(0xFF8A5A33);
            stroke.setStrokeWidth(round * .16f);
            if (sort == RunLevels.PET_SHOP) {
                canvas.drawOval(x - round * .4f, y - round * .05f, x + round * .4f, y + round * .6f, paint);
                for (int toe = 0; toe < 3; toe++) canvas.drawCircle(x + (toe - 1) * round * .42f, y - round * (toe == 1 ? .5f : .3f), round * .2f, paint);
            } else if (sort == RunLevels.GUILD_HALL) {
                canvas.drawLine(x - round * .45f, y + round * .55f, x + round * .4f, y - round * .5f, stroke);
                path.reset();
                path.moveTo(x + round * .45f, y - round * .6f);
                path.quadTo(x - round * .3f, y - round * .5f, x - round * .2f, y + round * .25f);
                path.quadTo(x + round * .5f, y + round * .1f, x + round * .45f, y - round * .6f);
                canvas.drawPath(path, paint);
            } else if (sort == RunLevels.HOME) {
                path.reset();
                path.moveTo(x - round * .6f, y);
                path.lineTo(x, y - round * .6f);
                path.lineTo(x + round * .6f, y);
                path.close();
                canvas.drawPath(path, paint);
                canvas.drawRect(x - round * .4f, y, x + round * .4f, y + round * .55f, paint);
            } else if (sort == RunLevels.SMITHY) {
                canvas.drawRect(x - round * .5f, y - round * .5f, x + round * .5f, y - round * .1f, paint);
                canvas.drawRect(x - round * .1f, y - round * .1f, x + round * .1f, y + round * .65f, paint);
            } else if (sort == RunLevels.BOUTIQUE) {
                // (A coat hanger, for the boutique.)
                path.reset();
                path.moveTo(x - round * .7f, y + round * .45f);
                path.lineTo(x, y - round * .1f);
                path.lineTo(x + round * .7f, y + round * .45f);
                path.close();
                canvas.drawPath(path, paint);
                canvas.drawCircle(x, y - round * .35f, round * .2f, paint);
            } else if (sort == RunLevels.GACHA) {
                // (A ribbon, for the ribbon gacha.)
                paint.setColor(Color.BLACK);
                paint.setAlpha(done ? 110 : 255);
                pieceIn(canvas, two, RIBBON_ART, x, y, round * 1.3f, 0);
            } else {
                canvas.drawCircle(x, y, round * .55f, paint);
                paint.setColor(cardColor);
                canvas.drawCircle(x + round * .25f, y - round * .12f, round * .45f, paint);
            }
            paint.setColor(Color.BLACK);
        } else if (kind < 0) {
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(round * 1.5f);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setColor(0xFF8A5A33);
            canvas.drawText("?", x, y + round * .52f, text);
            text.setTypeface(Typeface.DEFAULT);
        } else if (kind == RunLevels.STASH && holds == RunLevels.HOLDS_PET) {
            // A paw: a pet waits in it.
            paint.setColor(0xFF8A5A33);
            paint.setAlpha(done ? 110 : 255);
            canvas.drawOval(x - round * .4f, y - round * .05f, x + round * .4f, y + round * .6f, paint);
            for (int toe = 0; toe < 3; toe++) canvas.drawCircle(x + (toe - 1) * round * .42f, y - round * (toe == 1 ? .5f : .3f), round * .2f, paint);
            paint.setColor(Color.BLACK);
        } else if (kind == RunLevels.STASH && holds == RunLevels.HOLDS_SHRINE) {
            pieceIn(canvas, two, RIBBON_ART + RunLevels.ribbonsOf(number)[0] - 1, x, y, round * 1.3f, 0);
        } else if (kind == RunLevels.STASH) {
            pieceIn(canvas, four, CHEST_PICTURE, x, y, round * 1.35f, 0);
        } else if (kind == RunLevels.ARENA) {
            thing(canvas, PlusRun.SWORD, x, y, round * 1.3f, 35, false, false);
        } else if (kind == RunLevels.PASSAGE) {
            stroke.setStrokeWidth(tile * .06f);
            for (int arrow = 0; arrow < 2; arrow++) {
                float at = x - round * .35f + arrow * round * .5f;
                canvas.drawLine(at, y - round * .35f, at + round * .35f, y, stroke);
                canvas.drawLine(at + round * .35f, y, at, y + round * .35f, stroke);
            }
        } else {
            artIn(canvas, STAR_ART, x, y, round * 1.4f);
        }
        paint.setAlpha(255);
        stroke.setAlpha(255);
        if (done) {
            stroke.setColor(0xFF2E9E6E);
            stroke.setStrokeWidth(tile * .07f);
            canvas.drawLine(x - round * .1f, y + round * .45f, x + round * .2f, y + round * .75f, stroke);
            canvas.drawLine(x + round * .2f, y + round * .75f, x + round * .75f, y + round * .1f, stroke);
        }
    }

    /**
     * A cellar's wave, as it stands. In the second, the lights are out: all is dark but for
     * a light about her and a glimmer about each of them. Wherever one of them is about
     * to come in there is a mark, which closes on the place until it is there. While any
     * of them is somewhere she cannot see, an arrow at the edge of the screen points to the
     * nearest. And across the middle of the screen, large, what wave it is as it is
     * announced, and that it is cleared when it is.
     */
    private void drawWave(Canvas canvas) {
        int phase = run.wavePhase();
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() + PlusRun.TALL * .5f - cameraY) * tile;
        if (false) {
            if (glow.getShader() == null) {
                glow.setShader(new android.graphics.RadialGradient(0, 0, 1, new int[]{Color.BLACK, Color.BLACK, Color.TRANSPARENT}, new float[]{0, .55f, 1},
                        android.graphics.Shader.TileMode.CLAMP));
                glow.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT));
            }
            float in = Math.min(1, sincePhase / .6f);
            canvas.saveLayerAlpha(0, 0, width, height, Math.round(238 * in));
            canvas.drawColor(0xFF0A0812);
            lit(canvas, herX, herY, tile * (4.6f + .15f * (float) Math.sin(clock * 3)));
            for (PlusRun.Foe foe : run.foes()) {
                if (!foe.spawned || foe.flat) continue;
                lit(canvas, (foe.x + foe.wide() / 2 - cameraX) * tile, (foe.y + foe.tall() / 2 - cameraY) * tile, tile * 1.5f);
            }
            for (PlusRun.Shot shot : run.shots()) lit(canvas, (shot.x - cameraX) * tile, (shot.y - cameraY) * tile, tile * .9f);
            canvas.restore();
        }
        // The marks.
        float nearX = 0, nearY = 0, nearest = Float.MAX_VALUE;
        for (float[] mark : run.pending()) {
            float x = (mark[1] - cameraX) * tile, y = (mark[2] - .7f - cameraY) * tile, left = Math.max(0, Math.min(1, mark[3] / PlusRun.MARKED));
            float round = tile * (.45f + .9f * left);
            stroke.setColor(0xFFE0362F);
            stroke.setAlpha(Math.round(255 * (1 - left * .5f)));
            stroke.setStrokeWidth(tile * .08f);
            canvas.drawCircle(x, y, round, stroke);
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(tile * .03f);
            canvas.drawCircle(x, y, round + tile * .06f, stroke);
            stroke.setAlpha(255);
            word(canvas, "!", x, y + tile * .22f, tile * (.6f + .1f * (float) Math.sin(clock * 20)), 0xFFE0362F, 1);
            float away = (mark[1] - run.x()) * (mark[1] - run.x()) + (mark[2] - run.y()) * (mark[2] - run.y());
            if (away < nearest) {
                nearest = away;
                nearX = x;
                nearY = y;
            }
        }
        for (PlusRun.Foe foe : run.foes()) {
            if (!foe.spawned || foe.flat) continue;
            float away = (foe.x - run.x()) * (foe.x - run.x()) + (foe.y - run.y()) * (foe.y - run.y());
            if (away < nearest) {
                nearest = away;
                nearX = (foe.x + foe.wide() / 2 - cameraX) * tile;
                nearY = (foe.y + foe.tall() / 2 - cameraY) * tile;
            }
        }
        // The arrow to the nearest of what is left, when that is off the screen: at the edge it lies beyond, pointing at it.
        float edge = dp(34), low = safe.bottom - dp(120);
        if (phase == PlusRun.FOUGHT && nearest < Float.MAX_VALUE && (nearX < safe.left || nearX > safe.right || nearY < safe.top + dp(60) || nearY > low)) {
            float x = Math.max(safe.left + edge, Math.min(safe.right - edge, nearX)), y = Math.max(safe.top + dp(150), Math.min(low, nearY));
            double turn = Math.atan2(nearY - y, nearX - x);
            float beat = 1 + .12f * (float) Math.sin(clock * 8), size = dp(15) * beat;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x44000000);
            canvas.drawCircle(x, y + dp(2), dp(21), paint);
            paint.setColor(0xFFE0362F);
            canvas.drawCircle(x, y, dp(20), paint);
            path.reset();
            path.moveTo(x + (float) Math.cos(turn) * size, y + (float) Math.sin(turn) * size);
            path.lineTo(x + (float) Math.cos(turn + 2.5) * size * .8f, y + (float) Math.sin(turn + 2.5) * size * .8f);
            path.lineTo(x + (float) Math.cos(turn - 2.5) * size * .8f, y + (float) Math.sin(turn - 2.5) * size * .8f);
            path.close();
            paint.setColor(Color.WHITE);
            canvas.drawPath(path, paint);
            paint.setColor(Color.BLACK);
        }
        // The banner.
        boolean announced = phase == PlusRun.ANNOUNCED, cleared = phase == PlusRun.CLEARED;
        boolean carded = roomCardAge >= 0 && roomCardAge < 2.4f;
        if ((announced || cleared) && !carded || phase == PlusRun.FOUGHT && sincePhase < .5f && !carded) {
            boolean last = run.wave() == PlusRun.WAVES;
            String words = cleared ? "Wave cleared!" : last ? "FINAL WAVE" : "WAVE " + run.wave();
            String under = cleared ? "A breath. Pick up what fell." : run.wave() == 2 ? "The lights go out" : last ? "Mind the shelves. A ribbon lies in the middle." : "Marks show where they come in";
            float age = phase == PlusRun.FOUGHT ? PlusRun.ANNOUNCE + sincePhase : sincePhase;
            float show = Math.min(1, age / .18f) * (phase == PlusRun.FOUGHT ? Math.max(0, 1 - sincePhase / .5f) : 1), grow = 1 + .8f * Math.max(0, 1 - age / .22f);
            float y = safe.top + safe.height() * .36f, high = dp(96);
            int colour = cleared ? 0xFF2E9E6E : last ? 0xFFC0392B : 0xFFE0A21A;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF15131C);
            paint.setAlpha(Math.round(190 * show));
            canvas.drawRect(0, y - high / 2, width, y + high / 2, paint);
            paint.setColor(colour);
            paint.setAlpha(Math.round(255 * show));
            canvas.drawRect(0, y - high / 2, width, y - high / 2 + dp(4), paint);
            canvas.drawRect(0, y + high / 2 - dp(4), width, y + high / 2, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
            text.setColor(Color.WHITE);
            text.setAlpha(Math.round(255 * show));
            float size = Math.min(dp(46), safe.width() / (words.length() * .62f)) * grow;
            text.setTextSize(size);
            canvas.drawText(words, safe.centerX(), y + size * .22f, text);
            text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            text.setColor(colour);
            text.setAlpha(Math.round(255 * show));
            fitted(canvas, under, safe.centerX(), y + high / 2 - dp(14), dp(14), safe.width() - dp(32));
            text.setAlpha(255);
        }
    }

    /** The lights are out: all is dark but for a light about her, a glimmer about each of those in there with her, and about whatever is in the air. */
    private void drawDark(Canvas canvas) {
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() + PlusRun.TALL * .5f - cameraY) * tile;
        if (glow.getShader() == null) {
            glow.setShader(new android.graphics.RadialGradient(0, 0, 1, new int[]{Color.BLACK, Color.BLACK, Color.TRANSPARENT}, new float[]{0, .55f, 1},
                    android.graphics.Shader.TileMode.CLAMP));
            glow.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT));
        }
        float in = Math.min(1, sincePhase / .6f);
        canvas.saveLayerAlpha(0, 0, width, height, Math.round(232 * in));
        canvas.drawColor(0xFF0A0812);
        lit(canvas, herX, herY, tile * (5.2f + .15f * (float) Math.sin(clock * 3)));
        for (PlusRun.Foe foe : run.foes()) {
            if (!foe.spawned || foe.flat) continue;
            lit(canvas, (foe.x + foe.wide() / 2 - cameraX) * tile, (foe.y + foe.tall() / 2 - cameraY) * tile, tile * 1.7f);
        }
        for (float[] mark : run.pending()) lit(canvas, (mark[1] - cameraX) * tile, (mark[2] - .7f - cameraY) * tile, tile * 1.6f);
        for (PlusRun.Shot shot : run.shots()) lit(canvas, (shot.x - cameraX) * tile, (shot.y - cameraY) * tile, tile * .9f);
        canvas.restore();
    }

    /** A light in the dark: the dark is taken away about a place, most at its middle. */
    private void lit(Canvas canvas, float x, float y, float round) {
        canvas.save();
        canvas.translate(x, y);
        canvas.scale(round, round);
        canvas.drawCircle(0, 0, 1, glow);
        canvas.restore();
    }

    /** Whether the card that says what a place is holds the land while it is up: where something is about to begin in it. */
    private boolean roomHolds() { return run != null && run.room() >= 0 && run.sealed(); }

    /** A piece of a heart is found, and kept: four of them make a heart for every land, and there are two hearts to be made. What it says of itself, to go after something else. */
    private String piece() {
        int pieces = session.tally("run_pieces");
        if (pieces >= PIECES * PIECE_HEARTS) {
            session.keep("run_bank", bank() + 60);
            return ", + 60 to keep";
        }
        session.keep("run_pieces", pieces + 1);
        return (pieces + 1) % PIECES == 0 ? ", the last piece of a heart: a heart more in every land" : ", a piece of a heart: " + (pieces + 1) % PIECES + " of " + PIECES;
    }

    /**
     * The card that says what a place off the land is, as she goes in: its kind as a
     * heading, and under it what there is to do; where something is about to begin, with
     * the count to beat and "Tap to begin". And the card of how it went, when that is over:
     * what she did, and what it gave.
     */
    private void drawRoomCards(Canvas canvas) {
        if (roomCardAge >= 0 && run.room() >= 0 && roomCard < run.level().rooms.size()) {
            int[] one = run.level().rooms.get(roomCard);
            int kind = one[0];
            boolean holds = roomHolds() && roomCardAge < 2.4f;
            String title = kind == RunLevels.ARENA ? "Cellar: three waves" : kind == RunLevels.GAME ? "Mini game: catch the pluses" : kind == RunLevels.RANGE
                    ? "Mini game: the range" : kind == RunLevels.TRAMPLE ? "Mini game: flatten them" : kind == RunLevels.LAGOON ? "A lagoon: deep water"
                    : kind == RunLevels.HIDEOUT ? "A hideout: someone hidden" : kind == RunLevels.STASH ? LOOK_NAMES[Math.max(0, one[13])] + ": a stash"
                    : LOOK_NAMES[Math.max(0, one[13])] + ": a way through";
            String words = kind == RunLevels.ARENA ? (one[10] != 0 ? "An arena. Each wave is different; something sleeps under the last." : "An arena. Each wave is different; lose a life and the wave starts again.")
                    : kind == RunLevels.GAME ? "10 seconds. Catch " + PlusRun.ENOUGH + " for the prize." : kind == RunLevels.RANGE
                    ? "12 seconds, and an ink gun that never empties. Hit " + PlusRun.ENOUGH + "." : kind == RunLevels.TRAMPLE
                    ? "15 seconds. They keep coming: flatten " + PlusRun.ENOUGH + "." : kind == RunLevels.LAGOON ? "Take the blue ribbon to swim; a chest lies on the bottom."
                    : kind == RunLevels.HIDEOUT ? "Its hidden boss waits at the far end. What it guards is rare." : kind == RunLevels.STASH ? (one[14] == RunLevels.HOLDS_PET ? "A pet waits in it, by its chest." : one[14] == RunLevels.HOLDS_SHRINE ? "A shrine stands by its chest." : "Find the way through to its chest.")
                    : "Spikes, signs, and a chest by the far door.";
            float show = Math.min(1, roomCardAge / .25f) * (holds ? 1 : Math.max(0, Math.min(1, (3.4f - roomCardAge) / .4f)));
            if (holds) canvas.drawColor(Color.argb(Math.round(90 * show), 20, 16, 30));
            float wide = Math.min(safe.width() - dp(36), dp(340)), high = dp(holds ? 104 : 80);
            float left = safe.centerX() - wide / 2, top = safe.top + safe.height() * .3f - dp(14) * (1 - Math.min(1, roomCardAge / .25f));
            canvas.saveLayerAlpha(left - dp(8), top - dp(8), left + wide + dp(8), top + high + dp(12), Math.round(255 * show));
            box.set(left, top, left + wide, top + high);
            plate(canvas, box, dp(22), 250);
            int colour = kind == RunLevels.ARENA ? 0xFFC0392B : kind == RunLevels.PASSAGE ? 0xFF3B7BE0 : kind == RunLevels.STASH ? 0xFF8A5A33 : 0xFFE0A21A;
            stroke.setColor(colour);
            stroke.setStrokeWidth(dp(3));
            canvas.drawRoundRect(left, top, left + wide, top + high, dp(22), dp(22), stroke);
            paint.setAlpha(255);
            if (kind == RunLevels.ARENA) pieceIn(canvas, four, CELLAR_PICTURE, left + dp(38), top + dp(40), dp(46), 0);
            else if (kind == RunLevels.STASH) pieceIn(canvas, four, CHEST_PICTURE, left + dp(38), top + dp(40), dp(46), 0);
            else if (kind == RunLevels.PASSAGE) pieceIn(canvas, four, DOOR_PICTURE, left + dp(38), top + dp(40), dp(46), 0);
            else artIn(canvas, kind == RunLevels.GAME ? PLUS_ART : STAR_ART, left + dp(38), top + dp(40), dp(40));
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, title, left + dp(72), top + dp(36), dp(18), wide - dp(88));
            text.setColor(soft);
            fitted(canvas, words, left + dp(72), top + dp(58), dp(12.5f), wide - dp(88));
            if (holds) {
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(colour);
                text.setTextSize(dp(13));
                text.setAlpha(roomCardAge > .5f ? Math.round(170 + 80 * (float) Math.sin(clock * 6)) : 90);
                canvas.drawText("Tap to begin", safe.centerX(), top + dp(88), text);
                text.setAlpha(255);
            }
            canvas.restore();
        }
        if (roomResultAge >= 0) {
            boolean holds = roomResultAge < 2.6f;
            float show = Math.min(1, roomResultAge / .25f) * (holds ? 1 : Math.max(0, Math.min(1, (5.5f - roomResultAge) / .5f)));
            if (holds) canvas.drawColor(Color.argb(Math.round(90 * show), 20, 16, 30));
            float wide = Math.min(safe.width() - dp(36), dp(340)), high = dp(roomGave.isEmpty() ? 84 : 112);
            float left = safe.centerX() - wide / 2, top = safe.top + safe.height() * .3f - dp(14) * (1 - Math.min(1, roomResultAge / .25f));
            canvas.saveLayerAlpha(left - dp(8), top - dp(8), left + wide + dp(8), top + high + dp(12), Math.round(255 * show));
            box.set(left, top, left + wide, top + high);
            plate(canvas, box, dp(22), 250);
            stroke.setColor(0xFFE0B13A);
            stroke.setStrokeWidth(dp(3));
            canvas.drawRoundRect(left, top, left + wide, top + high, dp(22), dp(22), stroke);
            paint.setAlpha(255);
            if (roomRank >= 0) {
                // The rank, as a letter in a ring of its colour: gold for the best.
                int[] colours = {0xFF8C9296, 0xFF3B7BE0, 0xFF2E9E6E, 0xFFE0A21A};
                float pop = 1 + .5f * Math.max(0, 1 - (roomResultAge - .3f) / .25f);
                stroke.setColor(colours[roomRank]);
                stroke.setStrokeWidth(dp(3.5f));
                canvas.drawCircle(left + dp(38), top + dp(42), dp(24), stroke);
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(colours[roomRank]);
                text.setTextSize(dp(30) * (roomResultAge > .3f ? pop : 0));
                canvas.drawText("CBAS".substring(roomRank, roomRank + 1), left + dp(38), top + dp(53), text);
            } else if (roomPicture >= 0) {
                pieceIn(canvas, four, roomPicture, left + dp(38), top + dp(42), dp(46), 0);
            } else {
                artIn(canvas, STAR_ART, left + dp(38), top + dp(42), dp(40));
            }
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, roomResult, left + dp(72), top + dp(36), dp(19), wide - dp(88));
            text.setColor(soft);
            fitted(canvas, roomScore, left + dp(72), top + dp(59), dp(13.5f), wide - dp(88));
            if (!roomGave.isEmpty()) {
                text.setColor(0xFFB8860B);
                fitted(canvas, roomGave, left + dp(22), top + dp(92), dp(14), wide - dp(44));
            }
            canvas.restore();
        }
    }

    /** The button that uses the door she stands at: round, over the jump, with a door on it and what it will do. */
    private void doorControl(Canvas canvas) {
        int at = run.door();
        int[] one = run.level().rooms.get(at);
        boolean hidden = run.room() < 0 && one[7] == 1 && (run.revealed() & 1 << at) == 0
                && Math.abs(run.x() + PlusRun.WIDE / 2 - one[1] - .5f) < .9f;
        float beat = 1 + .04f * (float) Math.sin(clock * 6);
        canvas.save();
        canvas.scale(beat, beat, stallButton.centerX(), stallButton.centerY());
        button(canvas, stallButton, false, 0xFF8A5A33);
        paint.setAlpha(255);
        pieceIn(canvas, four, hidden ? CRACK_PICTURE : one[0] == RunLevels.ARENA ? CELLAR_PICTURE : DOOR_PICTURE, stallButton.centerX(), stallButton.centerY() - dp(7),
                stallButton.width() * .46f, 0);
        label(canvas, stallButton, run.room() >= 0 ? "Leave" : hidden ? "Break" : "Enter");
        canvas.restore();
    }

    /** Something that helps, by its kind: what lay about in the first volume, what her friends hold out in the second, and the ribbons. */
    private void help(Canvas canvas, int kind, float x, float y, float size) {
        if (kind >= PlusRun.PET) {
            // A pet that lies waiting: it sits there, and hops now and then.
            float hop = Math.abs((float) Math.sin(clock * 3 + x)) * size * .12f;
            cell(canvas, petArt(kind - PlusRun.PET), (int) (clock * 1.2f) % 4 == 0 ? 5 : 0, 6, x, y + size * .62f - hop, size * 1.5f, false);
            return;
        }
        if (kind == PlusRun.KEY) {
            // A key: a ring, a shaft and two teeth, in gold.
            canvas.save();
            canvas.rotate(-35 + 8 * (float) Math.sin(clock * 3), x, y);
            stroke.setColor(0xFFE0A21A);
            stroke.setStrokeWidth(size * .13f);
            canvas.drawCircle(x - size * .28f, y, size * .17f, stroke);
            canvas.drawLine(x - size * .1f, y, x + size * .42f, y, stroke);
            canvas.drawLine(x + size * .28f, y, x + size * .28f, y + size * .18f, stroke);
            canvas.drawLine(x + size * .42f, y, x + size * .42f, y + size * .18f, stroke);
            canvas.restore();
            return;
        }
        if (kind == PlusRun.CHEST) pieceIn(canvas, four, CHEST_PICTURE, x, y, size * 1.5f, 0);
        else if (kind > PlusRun.FORM) pieceIn(canvas, two, RIBBON_ART + kind - PlusRun.FORM - 1, x, y, size, 0);
        else if (kind == PlusRun.HOMERUN || kind == PlusRun.COMPLIMENT) pieceIn(canvas, two, GIFT_ART + kind - PlusRun.HOMERUN, x, y, size, 0);
        else if (kind == PlusRun.CAKE) pieceIn(canvas, four, CAKE_PICTURE, x, y, size, 0);
        else if (kind <= PlusRun.NOTES) artIn(canvas, BUFF_ART + kind, x, y, size);
    }

    /**
     * The ways that are shut while someone waits: a book stood on end and chained, with a
     * lock, in her colour. The gate ahead has her face over it; the way back comes down
     * behind her as she goes in. Walked into, either shakes; and when the last of them is
     * down they shiver for a moment and then go up out of the way.
     */
    private void drawWays(Canvas canvas, RunLevels.Level level) {
        PlusRun.Foe boss = null;
        for (PlusRun.Foe foe : run.foes()) if (RunLevels.boss(foe.kind) && !RunLevels.rover(foe.kind) && (boss == null || boss.flat)) boss = foe;
        if (boss == null) return;
        boolean opening = run.opening() > 0, gone = !run.standing() && !opening;
        // Going up out of the way takes half a second once they open.
        float away = gone ? Math.min(1, sinceOpened / .5f) : 0;
        if (away >= 1) return;
        int colour = waitColour(boss.kind);
        for (int which = 0; which < 2; which++) {
            boolean back = which == 1;
            if (back && !run.locked() && !gone) continue;
            int column = back ? level.arena : level.gate, floor = RunLevels.top(level, column);
            if (floor < 0) continue;
            float x = (column + .5f - cameraX) * tile, y = (floor - cameraY) * tile, tall = tile * 3.3f;
            // The way back comes down as it shuts; both shiver when walked into, and as they are about to open.
            float fall = back ? Math.max(0, 1 - sinceShut / .22f) : 0;
            float bump = sinceBump < .35f && (back == goLeft || !back == goRight) ? (float) Math.sin(clock * 60) * tile * .07f : 0;
            if (opening) bump = (float) Math.sin(clock * 55) * tile * .05f;
            float lift = -fall * tile * 6 - away * tile * 5;
            float beat = .5f + .5f * (float) Math.sin(clock * 5);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colour);
            paint.setAlpha(Math.round((60 + 70 * beat) * (1 - away)));
            canvas.drawRoundRect(x - tile * .75f + bump, y - tall - tile * .1f + lift, x + tile * .75f + bump, y + lift, tile * .3f, tile * .3f, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(Math.round(255 * (1 - away)));
            pieceTall(canvas, three, WALL_ART, x + bump, y + lift, tall, false);
            paint.setAlpha(255);
            // Over the gate, whose it is: her face, or for Zero and King Slime a ring of their colour.
            if (!back && away <= 0) {
                float round = tile * .52f, faceY = y - tall - round * 1.1f + (float) Math.sin(clock * 3) * tile * .05f;
                int face = waitFace(boss.kind);
                if (face >= 0) {
                    round(canvas, face, x, faceY, round);
                } else {
                    paint.setColor(colour);
                    canvas.drawCircle(x, faceY, round, paint);
                    paint.setColor(Color.BLACK);
                    text.setColor(Color.WHITE);
                    text.setTextAlign(Paint.Align.CENTER);
                    text.setTextSize(round * 1.3f);
                    canvas.drawText(boss.kind == RunLevels.ZERO ? "0" : "♛", x, faceY + round * .45f, text);
                }
            }
        }
    }

    /**
     * One of those who wait at the end: arriving, each in her own way; about what she does,
     * with a mark over her when something is coming and stars about her when she is spent,
     * which is when she can be hurt; changing; and down.
     */
    private void drawWaiting(Canvas canvas, PlusRun.Foe foe, float middle, float feet, boolean second) {
        if (foe.away) return;
        int kind = foe.kind, colour = waitColour(kind);
        float size = tile * 1.62f, lift = 0, slide = 0, turn = 0;
        int alpha = 255;
        boolean arriving = foe.enter > 0, changing = foe.flourish > 0, hit = foe.dazed > 0 || changing;
        float came = arriving ? 1 - Math.min(1, foe.enter / (run.entrances() == 1 ? PlusRun.ENTER : PlusRun.ENTER / 2)) : 1;
        float ease = came * came * (3 - 2 * came);
        if (arriving && RunLevels.rover(kind)) {
            // The gecko comes down its ribbon; Blot rises out of a pool of itself; the wyrm is on its way in from ahead.
            float in = 1 - Math.min(1, foe.enter), soft = in * in * (3 - 2 * in);
            if (kind == RunLevels.GECKO) {
                lift = -(1 - soft) * tile * 6;
            } else if (kind == RunLevels.BLOT) {
                alpha = Math.round(255 * soft);
                lift = (1 - soft) * foe.tall() * tile * .5f;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF2B2F7A);
                paint.setAlpha(Math.round(220 * (1 - soft * .6f)));
                canvas.drawOval(middle - foe.wide() * tile * (.3f + .5f * soft), feet - tile * .16f, middle + foe.wide() * tile * (.3f + .5f * soft), feet + tile * .12f, paint);
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
            }
        } else if (arriving) {
            if (kind >= RunLevels.GUARD && kind != RunLevels.LUCY) {
                arrives(canvas, kind, came, middle, feet, size, colour);
                slide = inSlide;
                lift = inLift;
                alpha = inAlpha;
            } else if (kind == RunLevels.TSUBAKIEH) {
                // She strolls in from the far end.
                slide = (1 - ease) * tile * 7;
            } else if (kind == RunLevels.ZERO) {
                // He rolls in.
                slide = (1 - ease) * tile * 9;
                turn = -(1 - ease) * 720;
            } else if (kind == RunLevels.GOKHAE) {
                alpha = Math.round(255 * ease);
            } else if (kind == RunLevels.YEOBAEK) {
                // Out of a slit of white that opens in the air.
                alpha = Math.round(255 * Math.max(0, came * 2 - 1));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setAlpha(Math.round(230 * (float) Math.sin(came * Math.PI)));
                canvas.drawRoundRect(middle - tile * (.08f + .5f * (float) Math.sin(came * Math.PI)), feet - size * 1.15f, middle + tile * (.08f + .5f * (float) Math.sin(came * Math.PI)),
                        feet + tile * .05f, tile * .2f, tile * .2f, paint);
                paint.setColor(Color.BLACK);
            } else {
                // Illya floats down in a light of her own, the twins drop in on a page, King Slime oozes down, and the Mawang descends.
                lift = -(1 - ease) * tile * 7;
                if (kind == RunLevels.ILLYA) {
                    path.reset();
                    path.moveTo(middle - tile * .5f, -tile);
                    path.lineTo(middle + tile * .5f, -tile);
                    path.lineTo(middle + tile * 1.6f, feet + tile * .1f);
                    path.lineTo(middle - tile * 1.6f, feet + tile * .1f);
                    path.close();
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFFFFE9A8);
                    paint.setAlpha(Math.round(90 * (float) Math.sin(came * Math.PI * .5f + .4f)));
                    canvas.drawPath(path, paint);
                    paint.setColor(Color.BLACK);
                } else if (kind == RunLevels.MIRI || kind == RunLevels.GARI) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFFF4EBD2);
                    paint.setAlpha(Math.round(255 * (1 - ease * ease)));
                    canvas.drawRoundRect(middle - tile * 1.1f, feet + lift - tile * .05f, middle + tile * 1.1f, feet + lift + tile * .18f, tile * .05f, tile * .05f, paint);
                    paint.setColor(Color.BLACK);
                }
            }
        }
        // Changing, a ring of her colour opens round her, twice.
        if (changing) {
            float through = 1 - foe.flourish / PlusRun.FLOURISH;
            stroke.setColor(colour);
            stroke.setStrokeWidth(tile * .12f);
            for (int ring = 0; ring < 2; ring++) {
                float wide = (through * 2 - ring * .6f) % 1;
                if (wide <= 0) continue;
                stroke.setAlpha(Math.round(220 * (1 - wide)));
                canvas.drawCircle(middle, feet - size * .45f, tile * (.5f + wide * 2.4f), stroke);
            }
            stroke.setAlpha(255);
        }
        float x = middle + slide, y = feet + lift;
        boolean turned = !foe.leftward && !foe.flat;
        int step = foe.step, mode = foe.mode;
        paint.setAlpha(hit && !foe.flat && (int) (clock * 18) % 2 == 0 ? Math.min(alpha, 150) : alpha);
        if (kind == RunLevels.ZERO) {
            // Zero: rolling when he rolls, dizzy when he is spent, and swelling darkly as he gathers himself to make nothing of what she carries.
            boolean rolling = mode == 1 && step == 1 && !foe.flat;
            if (rolling) turn = clock * 900 * (foe.leftward ? -1 : 1);
            if (mode == 4 && step == 0 && !arriving && !foe.flat) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF1B1B26);
                paint.setAlpha(Math.round(60 + 120 * Math.abs((float) Math.sin(clock * 12))));
                canvas.drawCircle(x, y - foe.tall() * tile * .5f, foe.tall() * tile * (.75f + .15f * (float) Math.sin(clock * 12)), paint);
                paint.setColor(Color.BLACK);
                paint.setAlpha(alpha);
            }
            canvas.save();
            canvas.rotate(turn, x, y - foe.tall() * tile * .5f);
            artTall(canvas, ZERO_ART + (foe.flat ? 2 : hit || step == 2 ? 1 : 0), x, y, foe.tall() * tile * (foe.flat ? .9f : 1.12f), turned);
            canvas.restore();
            // He holds up the book he is about to throw.
            if (!foe.flat && mode == 3 && step == 0 && !arriving) thing(canvas, PlusRun.BOOK, x, y - foe.tall() * tile * 1.25f, tile * .7f, 0, false, false);
        } else if (RunLevels.rover(kind)) {
            // One met in the middle of a land: about its business, at it, hit or spent, and down, each in its own way.
            int pose = foe.flat ? 3 : hit || step == 2 ? 2 : (kind == RunLevels.GECKO ? step == 1 && mode == 3 : step == 0 && !arriving) ? 1 : 0;
            float cell;
            boolean hangs = false;
            if (kind == RunLevels.WYRM) {
                cell = tile * 2.1f * 190 / 160;
                if (foe.flat) {
                    // It comes down out of the air and lies where it lands.
                    int under = RunLevels.top(run.level(), Math.max(0, Math.min(run.columns() - 1, (int) (foe.x + foe.wide() / 2))));
                    y = Math.min(under < 0 ? y + tile * 14 : (under - cameraY) * tile, y + tile * 13 * foe.act * foe.act);
                } else {
                    y += tile * .3f;
                }
            } else if (kind == RunLevels.BLOT) {
                cell = foe.tall() * tile * 1.1f * 230 / 200;
                y += tile * .06f;
                // Beaten, it spreads.
                if (foe.flat) cell *= 1 - .25f * Math.min(1, foe.act / 1.5f);
            } else {
                cell = tile * 1.05f * 160 / 110;
                y += tile * .05f;
                // Up its ribbon it hangs head down; the ribbon goes up out of sight.
                hangs = !foe.flat && (arriving || foe.y < foe.ground - .4f);
                if (hangs) {
                    stroke.setColor(0xFFC8423A);
                    stroke.setAlpha(alpha);
                    stroke.setStrokeWidth(tile * .13f);
                    canvas.drawLine(x, y - tile * 9, x, y - tile * .9f, stroke);
                    stroke.setAlpha(255);
                }
                // Beaten, it is thrown onto its back with a hop.
                if (foe.flat) y -= (float) Math.sin(Math.min(1, foe.act / .5f) * Math.PI) * tile * .8f;
            }
            if (foe.flat && foe.act > 3) {
                paint.setAlpha(255);
                return;
            }
            if (foe.flat) paint.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (3 - foe.act) / .8f))));
            canvas.save();
            if (hangs) canvas.rotate(turned ? 90 : -90, x, y - tile * .5f);
            cell(canvas, later(kind - RunLevels.GOKHAE), pose, 4, x, y, cell, turned);
            canvas.restore();
        } else if (kind == RunLevels.LUCY) {
            // Lucy: standing, at it, hit, and sitting it out while she is spent; as a child, and, once a third of what she can take
            // is gone, with her dragon side out. In the moment of the black ribbon she throws it, and then sees what came of it.
            boolean out = run.phase(foe) >= 1;
            int pose = run.decided() ? (run.ribboned() ? 2 : 1) : foe.flat ? 3 : hit ? 2 : step == 2 ? 3 : step <= 1 && !arriving ? 1 : 0;
            if (run.decided()) paint.setAlpha(255);
            frame(canvas, lucy(out ? 1 : 0), pose, 4, x, y + tile * .04f, size, turned);
        } else if (kind == RunLevels.ILLYA) {
            // Illya: floating, a heart at her fingers as she sends something, flustered when hit or spent, and sitting in tears at the last.
            int pose = foe.flat ? 3 : hit || step == 2 ? 2 : step <= 1 && !arriving ? 1 : 0;
            frame(canvas, cast(ILLYA), pose, CAST_FRAMES[ILLYA], x, y + (foe.flat ? 0 : tile * .1f), size, turned);
        } else if (kind == RunLevels.TSUBAKIEH) {
            // Tsubakieh: walking with her bat on her shoulder, the bat up, the bat down, dizzy, and looking at the two halves of it.
            boolean raised = step == 0 && (mode != 1 || foe.act <= .55f);
            int pose = foe.flat ? 5 : hit ? 4 : arriving ? (came > .75f ? 3 : second ? 1 : 0) : step == 2 ? 4 : step == 1 ? 3 : raised ? 2 : second ? 1 : 0;
            frame(canvas, cast(TSUBAKIEH), pose, CAST_FRAMES[TSUBAKIEH], x, y, size, turned);
        } else {
            int who = kind - RunLevels.GOKHAE, pose;
            if (kind == RunLevels.GOKHAE) {
                // Fan up, fan down while she is spent, startled, and beaten.
                pose = foe.flat ? 3 : hit ? 2 : step == 2 ? 1 : 0;
            } else if (kind == RunLevels.MIRI || kind == RunLevels.GARI) {
                pose = foe.flat ? 2 : step <= 1 && !arriving ? 1 : 0;
            } else if (kind == RunLevels.YEOBAEK) {
                // Sleepy, sword back, the cut, dozing, startled, and beaten. Between the lines she is not to be seen.
                if (foe.unseen && !foe.flat) {
                    // But where her dash will come from is: a line of white along the floor.
                    if (mode == 2 && step == 0) {
                        stroke.setColor(Color.WHITE);
                        stroke.setAlpha(Math.round(120 + 100 * Math.abs((float) Math.sin(clock * 16))));
                        stroke.setStrokeWidth(tile * .1f);
                        canvas.drawLine((run.arenaFrom() + 1 - cameraX) * tile, feet - tile * .6f, (run.arenaTo() - cameraX) * tile, feet - tile * .6f, stroke);
                        stroke.setAlpha(255);
                    }
                    paint.setAlpha(255);
                    return;
                }
                pose = foe.flat ? 5 : hit ? 4 : step == 2 ? 3 : step == 1 ? (mode == 3 || mode == 4 && foe.fromX == 1 ? 1 : 2) : arriving ? 0 : 1;
            } else if (kind == RunLevels.KING) {
                pose = foe.flat ? 3 : hit ? 2 : foe.ground - foe.y > .25f || arriving ? 1 : 0;
                size *= foe.size;
            } else if (kind >= RunLevels.GUARD) {
                // The seven others: standing, at it, hit, and down. Sumbi, under the page, is not to be seen: only a ripple where she is.
                if (foe.unseen && !foe.flat) {
                    stroke.setColor(Color.WHITE);
                    stroke.setAlpha(Math.round(120 + 100 * Math.abs((float) Math.sin(clock * 14))));
                    stroke.setStrokeWidth(tile * .08f);
                    canvas.drawOval(x - tile * .7f, feet - tile * .14f, x + tile * .7f, feet + tile * .1f, stroke);
                    stroke.setAlpha(255);
                    paint.setAlpha(255);
                    return;
                }
                pose = foe.flat ? 3 : hit ? 2 : step <= 1 && !arriving ? 1 : 0;
                paint.setAlpha(alpha);
            } else {
                // The Mawang: on the wing, diving, sending something, out of breath, hit, and at the last smiling over her notebook.
                pose = foe.flat ? 5 : hit ? 4 : arriving ? 0 : step == 2 ? 3 : step == 1 ? (mode == 2 || mode == 5 ? 1 : 2) : 0;
                if (arriving) {
                    // She comes down masked in shadow, and it lifts.
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFF1A1020);
                    paint.setAlpha(Math.round(200 * (1 - ease)));
                    canvas.drawCircle(x, y - size * .5f, size * .7f, paint);
                    paint.setColor(Color.BLACK);
                    paint.setAlpha(alpha);
                }
            }
            frame(canvas, later(who), pose, LATER_FRAMES[who], x, y, size, turned);
            // Gokhae unfolds from behind her fan.
            if (kind == RunLevels.GOKHAE && arriving) {
                paint.setAlpha(Math.round(255 * (1 - ease)));
                pieceIn(canvas, three, BLADE_ART, x, y - size * .45f, tile * (3 - 1.6f * ease), ease * 200);
            }
        }
        paint.setAlpha(255);
        if (!foe.flat && alpha > 120) guarded(canvas, foe, x, y - Math.max(size, foe.tall() * tile) * .48f, Math.max(size, foe.tall() * tile) * .62f, colour);
        if (foe.flat || arriving || changing) return;
        float over = y - Math.max(size, foe.tall() * tile) * (kind == RunLevels.ZERO ? 1.2f : .95f) - tile * .2f;
        if (step == 0 && foe.dazed <= 0) {
            // Something is coming.
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(tile * (.7f + .12f * (float) Math.sin(clock * 20)));
            text.setStyle(Paint.Style.STROKE);
            text.setStrokeWidth(tile * .12f);
            text.setColor(Color.WHITE);
            canvas.drawText("!", x, over, text);
            text.setStyle(Paint.Style.FILL);
            text.setColor(colour);
            canvas.drawText("!", x, over, text);
        } else if (step == 2) {
            // Spent: stars going round her head.
            for (int star = 0; star < 3; star++) {
                double round = clock * 6 + star * Math.PI * 2 / 3;
                art(canvas, STAR_ART, x + (float) Math.cos(round) * tile * .55f, over + tile * .2f + (float) Math.sin(round) * tile * .14f, tile * .3f, false);
            }
        }
    }

    /** A small shield, as a mark: for the bar of whoever waits. */
    private void shieldMark(Canvas canvas, float x, float y, float size, int colour) {
        path.reset();
        path.moveTo(x - size, y - size);
        path.lineTo(x + size, y - size);
        path.lineTo(x + size, y);
        path.quadTo(x + size, y + size * .9f, x, y + size * 1.25f);
        path.quadTo(x - size, y + size * .9f, x - size, y);
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colour);
        canvas.drawPath(path, paint);
        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(size * .22f);
        canvas.drawLine(x, y - size * .55f, x, y + size * .6f, stroke);
        canvas.drawLine(x - size * .5f, y - size * .1f, x + size * .5f, y - size * .1f, stroke);
        paint.setColor(Color.BLACK);
    }

    /**
     * How one of those who wait stands toward a blow, shown on her for as long as it is so.
     * While blows are turned aside there is a shield about her, in her colour, with things
     * of her own going round it: it is there the whole time, not only when it is struck,
     * and when it is struck it flashes white. While she arrives or changes, when nothing
     * touches her at all, it is there too, and brighter. When she is spent it breaks, its
     * pieces flying off, and for as long as a blow would land there is a gold mark over her
     * and a gold light under her; as that comes to its end the mark blinks and the shield
     * flickers back.
     */
    private void guarded(Canvas canvas, PlusRun.Foe foe, float x, float y, float round, int colour) {
        int guard = run.guard(foe), kind = foe.kind;
        float left = run.bareLeft(foe);
        boolean closing = guard == PlusRun.BARE && left < .5f;
        if (guard == PlusRun.SHIELDED || guard == PlusRun.AWAY || closing && (int) (clock * 14) % 2 == 0) {
            boolean struck = sinceStruck < .25f, bright = guard == PlusRun.AWAY;
            float swell = round * (1.06f + .03f * (float) Math.sin(clock * 5) + (struck ? .08f * (1 - sinceStruck / .25f) : 0));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(struck ? Color.WHITE : colour);
            paint.setAlpha(closing ? 30 : struck ? 120 : bright ? 70 : 42);
            canvas.drawCircle(x, y, swell, paint);
            // Two rings, a dark one under a bright one, so that it stands out from any land behind it.
            stroke.setColor(0xFF20242A);
            stroke.setAlpha(closing ? 60 : 130);
            stroke.setStrokeWidth(tile * .11f);
            canvas.drawCircle(x, y, swell, stroke);
            stroke.setColor(struck ? Color.WHITE : colour);
            stroke.setAlpha(closing ? 120 : 255);
            stroke.setStrokeWidth(tile * (struck ? .09f : .06f));
            canvas.drawCircle(x, y, swell, stroke);
            stroke.setColor(Color.WHITE);
            stroke.setAlpha(closing ? 80 : 200);
            stroke.setStrokeWidth(tile * .035f);
            box.set(x - swell * .8f, y - swell * .8f, x + swell * .8f, y + swell * .8f);
            canvas.drawArc(box, 200 + (float) Math.sin(clock * 2) * 12, 50, false, stroke);
            stroke.setAlpha(255);
            // The cracks in it: none while it is whole, and more of them, and longer, the less of it is left to break.
            float gone = guard == PlusRun.SHIELDED && !run.forming(foe) ? 1 - run.shield(foe) : 0;
            if (gone > .01f) {
                stroke.setColor(Color.WHITE);
                stroke.setStrokeWidth(tile * .05f);
                int cracks = 1 + Math.round(gone * 5);
                for (int crack = 0; crack < cracks; crack++) {
                    double way = crack * 2.399 + foe.kind;
                    float from = swell * (.15f + .12f * (crack % 3)), to = swell * Math.min(1, from / swell + .35f + .5f * gone);
                    float bend = (crack % 2 == 0 ? 1 : -1) * .35f;
                    path.reset();
                    path.moveTo(x + (float) Math.cos(way) * from, y + (float) Math.sin(way) * from);
                    path.lineTo(x + (float) Math.cos(way + bend * .5f) * (from + to) / 2, y + (float) Math.sin(way + bend * .5f) * (from + to) / 2);
                    path.lineTo(x + (float) Math.cos(way - bend * .2f) * to, y + (float) Math.sin(way - bend * .2f) * to);
                    canvas.drawPath(path, stroke);
                }
            }
            if (run.forming(foe)) {
                // Forming again: its rim comes round piece by piece.
                stroke.setColor(Color.WHITE);
                stroke.setStrokeWidth(tile * .09f);
                box.set(x - swell, y - swell, x + swell, y + swell);
                canvas.drawArc(box, -90, 360 * run.shield(foe), false, stroke);
            }
            // What goes round it is hers: hearts for Illya, coins for Jupan, drops of ink or water for those made of it, noughts for Zero, and pages for the rest.
            int things = 5;
            for (int thing = 0; thing < things && !closing; thing++) {
                double turn = clock * (bright ? 3.2f : 1.5f) + thing * Math.PI * 2 / things;
                float at = x + (float) Math.cos(turn) * swell, high = y + (float) Math.sin(turn) * swell, small = tile * .2f;
                paint.setAlpha(255);
                if (kind == RunLevels.ILLYA) {
                    artIn(canvas, HEART_ART, at, high, small * 1.5f);
                } else if (kind == RunLevels.JUPAN) {
                    paint.setColor(0xFFF2C230);
                    canvas.drawCircle(at, high, small * .55f, paint);
                    stroke.setColor(0xFFB8860B);
                    stroke.setStrokeWidth(tile * .03f);
                    canvas.drawCircle(at, high, small * .55f, stroke);
                } else if (kind == RunLevels.KING || kind == RunLevels.BUTLER || kind == RunLevels.SUMBI) {
                    paint.setColor(kind == RunLevels.BUTLER ? 0xFF15151C : colour);
                    canvas.drawCircle(at, high, small * .5f, paint);
                    paint.setColor(Color.WHITE);
                    paint.setAlpha(180);
                    canvas.drawCircle(at - small * .15f, high - small * .15f, small * .14f, paint);
                } else if (kind == RunLevels.ZERO) {
                    stroke.setColor(Color.WHITE);
                    stroke.setStrokeWidth(tile * .06f);
                    canvas.drawOval(at - small * .4f, high - small * .55f, at + small * .4f, high + small * .55f, stroke);
                } else {
                    canvas.save();
                    canvas.rotate((float) Math.toDegrees(turn) + 90, at, high);
                    paint.setColor(0xFFFDF6EE);
                    canvas.drawRect(at - small * .5f, high - small * .65f, at + small * .5f, high + small * .65f, paint);
                    stroke.setColor(colour);
                    stroke.setStrokeWidth(tile * .025f);
                    canvas.drawRect(at - small * .5f, high - small * .65f, at + small * .5f, high + small * .65f, stroke);
                    canvas.drawLine(at - small * .28f, high - small * .2f, at + small * .28f, high - small * .2f, stroke);
                    canvas.drawLine(at - small * .28f, high + small * .15f, at + small * .28f, high + small * .15f, stroke);
                    canvas.restore();
                }
            }
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
        if (guard == PlusRun.BARE || guard == PlusRun.REELING && sinceBroke < .4f) {
            // It has broken: its pieces fly off.
            if (sinceBroke < .4f) {
                float out = sinceBroke / .4f;
                stroke.setColor(colour);
                stroke.setAlpha(Math.round(255 * (1 - out)));
                stroke.setStrokeWidth(tile * .07f);
                for (int piece = 0; piece < 8; piece++) {
                    float from = piece * 45 + 8;
                    double way = Math.toRadians(from + 15);
                    float far = out * tile * 1.1f;
                    box.set(x - round + (float) Math.cos(way) * far, y - round + (float) Math.sin(way) * far + out * out * tile * .5f,
                            x + round + (float) Math.cos(way) * far, y + round + (float) Math.sin(way) * far + out * out * tile * .5f);
                    canvas.drawArc(box, from, 30, false, stroke);
                }
                stroke.setAlpha(255);
            }
            if (guard == PlusRun.BARE && (!closing || (int) (clock * 14) % 2 == 0)) {
                // Open: a gold light under her, and a gold mark over her that points down at her.
                float beat = .5f + .5f * (float) Math.sin(clock * 10);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFFFD24A);
                paint.setAlpha(Math.round(60 + 50 * beat));
                canvas.drawOval(x - round * 1.1f, y + round * .7f, x + round * 1.1f, y + round * .98f, paint);
                float top = y - round * 1.75f - tile * .1f * beat, wide = tile * .34f;
                path.reset();
                path.moveTo(x - wide, top);
                path.lineTo(x + wide, top);
                path.lineTo(x, top + wide * 1.25f);
                path.close();
                paint.setAlpha(255);
                canvas.drawPath(path, paint);
                stroke.setColor(0xFF7A4E00);
                stroke.setStrokeWidth(tile * .05f);
                canvas.drawPath(path, stroke);
                paint.setColor(Color.BLACK);
            }
        }
    }

    /** A word in the air, outlined in white: for whoever arrives with one. */
    private void word(Canvas canvas, String words, float x, float y, float size, int colour, float solid) {
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(size);
        text.setStyle(Paint.Style.STROKE);
        text.setStrokeWidth(size * .2f);
        text.setColor(Color.WHITE);
        text.setAlpha(Math.round(255 * Math.max(0, Math.min(1, solid))));
        canvas.drawText(words, x, y, text);
        text.setStyle(Paint.Style.FILL);
        text.setColor(colour);
        text.setAlpha(Math.round(255 * Math.max(0, Math.min(1, solid))));
        canvas.drawText(words, x, y, text);
        text.setAlpha(255);
    }

    /**
     * How each of the seven others, and the Old Book, arrives, drawn behind her at this
     * moment of it, and where it leaves her to be drawn. The Guild Guard marches in a step
     * at a time, plants her halberd so that the ground cracks, and calls a halt. Penchok-jipsa
     * falls from the top of the screen as a drop of ink, bursts into a puddle, and rises out
     * of it. A scroll unrolls along the floor to where Byeoru will stand, and she is on it,
     * reading. A toll bar comes down across the way, coins rain, and Jupan slides in on her
     * abacus. Rings spread on the page, and Sumbi bursts up out of it in a scatter of drops.
     * The Brake Pads skid in, leaving two black streaks and a spray of sparks, and stop dead.
     * The Straight-Ahead Party charges straight through, cannot stop, and comes back. And
     * the Old Book sleeps on, until he wakes with a start.
     */
    private void arrives(Canvas canvas, int kind, float came, float x, float feet, float size, int colour) {
        inSlide = 0;
        inLift = 0;
        inAlpha = 255;
        float ease = came * came * (3 - 2 * came);
        paint.setStyle(Paint.Style.FILL);
        if (kind == RunLevels.GUARD) {
            float march = Math.min(1, came / .7f);
            inSlide = (1 - march) * tile * 7;
            inLift = march < 1 ? -Math.abs((float) Math.sin(march * Math.PI * 5)) * tile * .2f : 0;
            if (march >= 1) {
                float after = (came - .7f) / .3f;
                stroke.setColor(colour);
                stroke.setAlpha(Math.round(255 * (1 - after * .7f)));
                stroke.setStrokeWidth(tile * .07f);
                for (int crack = -2; crack <= 2; crack++) {
                    canvas.drawLine(x - tile * .6f, feet, x - tile * .6f + crack * tile * .9f * Math.min(1, after * 3), feet + tile * (.12f + .1f * Math.abs(crack)), stroke);
                }
                stroke.setAlpha(255);
                word(canvas, "HALT!", x, feet - size * 1.2f - after * tile * .4f, tile * .62f * (1.3f - .3f * Math.min(1, after * 4)), colour, 1.4f - after);
            }
        } else if (kind == RunLevels.BUTLER) {
            paint.setColor(0xFF15151C);
            if (came < .45f) {
                // The drop, falling.
                float fall = came / .45f, at = feet * fall * fall;
                inAlpha = 0;
                canvas.drawOval(x - tile * .26f, at - tile * .8f, x + tile * .26f, at, paint);
                canvas.drawCircle(x, at - tile * .25f, tile * .3f, paint);
            } else {
                float out = (came - .45f) / .55f, wide = tile * 1.5f * Math.min(1, out * 4);
                paint.setAlpha(Math.round(255 * (1 - out * .8f)));
                canvas.drawOval(x - wide, feet - tile * .13f, x + wide, feet + tile * .1f, paint);
                for (int drop = 0; drop < 6; drop++) {
                    float way = (drop - 2.5f) * .5f, far = out * tile * 2;
                    canvas.drawCircle(x + way * far, feet - (float) Math.sin(out * Math.PI) * tile * (1 + Math.abs(way)), tile * .08f, paint);
                }
                // He rises out of it.
                inAlpha = Math.round(255 * Math.min(1, out * 2.5f));
                inLift = (1 - Math.min(1, out * 1.6f)) * size * .5f;
            }
            paint.setAlpha(255);
        } else if (kind == RunLevels.BYEORU) {
            float roll = Math.min(1, came / .6f), from = x + tile * 6 * (1 - roll) - tile * .5f;
            paint.setColor(0xFFF4EBD2);
            canvas.drawRect(from, feet - tile * .1f, x + tile * 6, feet + tile * .03f, paint);
            stroke.setColor(0xFF5A4A3A);
            stroke.setStrokeWidth(tile * .025f);
            for (float line = from + tile * .4f; line < x + tile * 6; line += tile * .5f) canvas.drawLine(line, feet - tile * .07f, line + tile * .25f, feet - tile * .07f, stroke);
            paint.setColor(0xFFE0D3B0);
            canvas.drawCircle(from, feet - tile * .14f, tile * .16f, paint);
            canvas.drawCircle(from, feet - tile * .14f, tile * .16f, stroke);
            inAlpha = Math.round(255 * Math.max(0, Math.min(1, (came - .5f) / .35f)));
        } else if (kind == RunLevels.JUPAN) {
            inSlide = (1 - ease) * tile * 7;
            // The toll bar, striped, coming down across the way in front of where she will stand.
            float swing = -90 * (1 - Math.min(1, came / .5f));
            canvas.save();
            canvas.rotate(swing, x - tile * 1.6f, feet - tile * 1.1f);
            for (int stripe = 0; stripe < 6; stripe++) {
                paint.setColor(stripe % 2 == 0 ? 0xFFE0362F : Color.WHITE);
                canvas.drawRect(x - tile * 1.6f - (stripe + 1) * tile * .45f, feet - tile * 1.18f, x - tile * 1.6f - stripe * tile * .45f, feet - tile * 1.02f, paint);
            }
            canvas.restore();
            paint.setColor(0xFF7A6A58);
            canvas.drawRect(x - tile * 1.66f, feet - tile * 1.2f, x - tile * 1.54f, feet, paint);
            // And coins.
            for (int coin = 0; coin < 12; coin++) {
                float fall = (came * 1.6f + coin * .137f) % 1, across = x + (coin - 5.5f) * tile * .55f + (float) Math.sin(coin * 2.3f) * tile * .3f;
                paint.setColor(0xFFF2C230);
                paint.setAlpha(Math.round(255 * (1 - came)));
                canvas.drawCircle(across, feet * fall, tile * .1f, paint);
            }
            paint.setAlpha(255);
        } else if (kind == RunLevels.SUMBI) {
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(tile * .06f);
            for (int ring = 0; ring < 3; ring++) {
                float wide = (came * 2.2f + ring * .33f) % 1;
                stroke.setAlpha(Math.round(220 * (1 - wide) * (1 - came)));
                canvas.drawOval(x - tile * (.3f + wide * 1.6f), feet - tile * .1f * (1 + wide), x + tile * (.3f + wide * 1.6f), feet + tile * .08f * (1 + wide), stroke);
            }
            stroke.setAlpha(255);
            if (came < .55f) {
                inAlpha = 0;
            } else {
                // Up, over-high, and down into her place.
                float out = (came - .55f) / .45f;
                inLift = -(float) Math.sin(out * Math.PI) * tile * 1.6f + (1 - Math.min(1, out * 4)) * tile;
                paint.setColor(0xFFBFE6FF);
                for (int drop = 0; drop < 8; drop++) {
                    float way = (drop - 3.5f) * .4f;
                    paint.setAlpha(Math.round(255 * (1 - out)));
                    canvas.drawCircle(x + way * out * tile * 2.4f, feet - (float) Math.sin(out * Math.PI) * tile * (1.4f + Math.abs(way) * .6f), tile * .09f, paint);
                }
                paint.setAlpha(255);
            }
        } else if (kind == RunLevels.PADS) {
            float skid = Math.min(1, came / .6f), slow = 1 - (1 - skid) * (1 - skid);
            inSlide = (1 - slow) * tile * 9;
            // Two black streaks where they have been, and sparks where they are.
            paint.setColor(0xFF2A2A30);
            paint.setAlpha(Math.round(150 * (1 - came * .7f)));
            canvas.drawRect(x + inSlide, feet - tile * .05f, x + tile * 9, feet + tile * .01f, paint);
            canvas.drawRect(x + inSlide + tile * .5f, feet + tile * .03f, x + tile * 9, feet + tile * .08f, paint);
            if (skid < 1) {
                paint.setColor(0xFFFFD24A);
                for (int spark = 0; spark < 8; spark++) {
                    paint.setAlpha(120 + random.nextInt(120));
                    canvas.drawCircle(x + inSlide + random.nextFloat() * tile * 1.6f, feet - random.nextFloat() * tile * .6f, tile * .06f, paint);
                }
            } else {
                // Stopped dead, they rock on their heels.
                inLift = -Math.abs((float) Math.sin((came - .6f) / .4f * Math.PI * 3)) * tile * .12f * (1 - (came - .6f) / .4f);
            }
            paint.setAlpha(255);
        } else if (kind == RunLevels.PARTY) {
            // Straight through, past where they meant to stop, and back.
            inSlide = came < .5f ? tile * (9 - came / .5f * 13) : -tile * 4 * (1 - (came - .5f) / .5f) * (1 - (came - .5f) / .5f);
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(tile * .05f);
            if (came < .5f) {
                for (int line = 0; line < 5; line++) {
                    stroke.setAlpha(160);
                    float high = feet - size * (.2f + line * .18f);
                    canvas.drawLine(x + inSlide + tile * 1.2f, high, x + inSlide + tile * (2.6f + line % 2), high, stroke);
                }
            }
            stroke.setAlpha(255);
            paint.setColor(0xFFDDE3E8);
            for (int puff = 0; puff < 5; puff++) {
                paint.setAlpha(Math.round(140 * (1 - came)));
                canvas.drawCircle(x + inSlide + (came < .5f ? 1 : -1) * tile * (.8f + puff * .5f), feet - tile * .12f, tile * (.14f + puff * .03f), paint);
            }
            paint.setAlpha(255);
        } else {
            // The Old Book: asleep, and then not.
            if (came < .7f) {
                for (int z = 0; z < 3; z++) {
                    float rise = (came * 1.8f + z * .33f) % 1;
                    word(canvas, "z", x - tile * (.3f + rise * .5f), feet - size * (1 + rise * .7f), tile * (.3f + rise * .25f), colour, 1 - rise);
                }
                inLift = (float) Math.sin(came * 30) * tile * .015f;
            } else {
                float start = (came - .7f) / .3f;
                inLift = -(float) Math.sin(start * Math.PI) * tile * .5f;
                word(canvas, "!?", x, feet - size * 1.25f, tile * .6f, colour, 1.3f - start);
            }
        }
        paint.setColor(Color.BLACK);
    }

    /**
     * What is won from whoever fell, rising out of her: it comes up from where she lay to
     * the middle of the screen, growing, with light turning behind it; then a card under it
     * says what it is and what it does, and that it is hers to keep.
     */
    private void drawPrize(Canvas canvas) {
        float age = prizeAge, rise = Math.min(1, age / 1.1f), eased = rise * rise * (3 - 2 * rise);
        canvas.drawColor(Color.argb(Math.round(110 * Math.min(1, age / .5f)), 20, 16, 30));
        float fromX = (prizeX - cameraX) * tile, fromY = (prizeY - cameraY) * tile;
        float x = fromX + (safe.centerX() - fromX) * eased, y = fromY + (safe.top + safe.height() * .36f - fromY) * eased;
        float big = dp(46) + dp(70) * eased;
        int colour = prizeWon > 0 ? FORM_COLOURS[prizeWon - 1] : 0xFFE0B13A;
        // Light, turning.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colour);
        for (int ray = 0; ray < 10; ray++) {
            double turn = clock * .9f + ray * Math.PI / 5;
            path.reset();
            path.moveTo(x, y);
            path.lineTo(x + (float) Math.cos(turn - .12) * big * 2.4f, y + (float) Math.sin(turn - .12) * big * 2.4f);
            path.lineTo(x + (float) Math.cos(turn + .12) * big * 2.4f, y + (float) Math.sin(turn + .12) * big * 2.4f);
            path.close();
            paint.setAlpha(Math.round(70 * eased));
            canvas.drawPath(path, paint);
        }
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.round(200 * eased));
        canvas.drawCircle(x, y, big * .78f, paint);
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        prizePicture(canvas, prizeWon, x, y + (float) Math.sin(clock * 3) * dp(4), big * (1 + .08f * (float) Math.sin(clock * 6)));
        // The card that says what it is.
        if (age > 1) {
            float show = Math.min(1, (age - 1) / .35f), wide = Math.min(safe.width() - dp(36), dp(340)), high = dp(92);
            float left = safe.centerX() - wide / 2, top = y + big * .95f + dp(18) * (1 - show);
            canvas.saveLayerAlpha(left - dp(8), top - dp(8), left + wide + dp(8), top + high + dp(12), Math.round(255 * show));
            box.set(left, top, left + wide, top + high);
            plate(canvas, box, dp(22), 250);
            stroke.setColor(colour);
            stroke.setStrokeWidth(dp(3));
            canvas.drawRoundRect(left, top, left + wide, top + high, dp(22), dp(22), stroke);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(soft);
            fitted(canvas, "Won from " + waitName(RunLevels.bossOf(number)) + ", to keep", safe.centerX(), top + dp(24), dp(13), wide - dp(28));
            text.setColor(ink);
            fitted(canvas, prizeName(prizeWon), safe.centerX(), top + dp(50), dp(21), wide - dp(28));
            text.setColor(soft);
            fitted(canvas, capital(prizeGives(prizeWon)) + (prizeWon > 0 ? ". Hers in the assistant's settings too." : "."), safe.centerX(), top + dp(74), dp(12.5f),
                    wide - dp(28));
            canvas.restore();
        }
        if (age > 1.8f) {
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.WHITE);
            text.setTextSize(dp(13));
            text.setAlpha(Math.round(160 + 80 * (float) Math.sin(clock * 5)));
            canvas.drawText("Tap to go on", safe.centerX(), safe.bottom - dp(36), text);
            text.setAlpha(255);
        }
    }

    /** The face of someone who speaks, in a round frame of her colour: cut from the top of her picture. */
    private void round(Canvas canvas, int who, float x, float y, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? 0xFF2A2E33 : 0xFFFDF6EE);
        paint.setAlpha(255);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.BLACK);
        Bitmap picture;
        if (who == MOTHER_FACE) {
            // (The Mother Tongue: her head and her crown, from the first picture of her sheet.)
            if (motherArt == null) motherArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_mother);
            picture = motherArt;
            int cell = picture.getWidth() / 7, sideOf = picture.getHeight() * 44 / 100;
            source.set(cell / 2 - sideOf / 2, picture.getHeight() * 9 / 100, cell / 2 + sideOf / 2, picture.getHeight() * 9 / 100 + sideOf);
        } else if (who == LUCY_FACE) {
            // (Lucy: her head, from the sheet of her as a child.)
            picture = lucy(0);
            int cell = picture.getWidth() / 4, sideOf = picture.getHeight() * 46 / 100;
            source.set(cell / 2 - sideOf / 2, picture.getHeight() * 27 / 100, cell / 2 + sideOf / 2, picture.getHeight() * 27 / 100 + sideOf);
        } else if (who >= FIRST_SMITH) {
            // (One of Moru's sisters: her head, from her own sheet.)
            picture = smith(who - FIRST_SMITH + 1);
            int cell = picture.getWidth() / 4, sideOf = picture.getHeight() * 44 / 100;
            source.set(cell / 2 - sideOf / 2, picture.getHeight() * 38 / 100, cell / 2 + sideOf / 2, picture.getHeight() * 38 / 100 + sideOf);
        } else if (who >= FIRST_OTHER) {
            picture = later(RunLevels.GUARD - RunLevels.GOKHAE + who - FIRST_OTHER);
            int cell = picture.getWidth() / 4, sideOf = Math.min(cell, picture.getHeight() * 55 / 100);
            source.set(cell / 2 - sideOf / 2, picture.getHeight() * 30 / 100, cell / 2 + sideOf / 2, picture.getHeight() * 30 / 100 + sideOf);
        } else if (who >= FIRST_VILLAGER) {
            picture = villagers();
            int cell = picture.getWidth() / VILLAGERS, left = (who - FIRST_VILLAGER) * cell, sideOf = cell * 3 / 4;
            source.set(left + (cell - sideOf) / 2, cell / 20, left + (cell + sideOf) / 2, cell / 20 + sideOf);
        } else if (who == 0) {
            // Her own face, in the colour she is in.
            picture = herPet();
            int wide = pet.getWidth() / PetArt.COLUMNS, tall = pet.getHeight() / PetArt.FRAMES.length, sideOf = tall * 62 / 100;
            source.set(wide / 2 - sideOf / 2, tall * PetArt.IDLE + tall / 30, wide / 2 + sideOf / 2, tall * PetArt.IDLE + tall / 30 + sideOf);
        } else {
            picture = face(who);
            int wide = picture.getWidth(), sideOf = Math.min(wide, picture.getHeight() * 56 / 100);
            source.set((wide - sideOf) / 2, picture.getHeight() / 40, (wide + sideOf) / 2, picture.getHeight() / 40 + sideOf);
        }
        canvas.save();
        path.reset();
        path.addCircle(x, y, radius - dp(1), Path.Direction.CW);
        canvas.clipPath(path);
        target.set(x - radius, y - radius, x + radius, y + radius);
        canvas.drawBitmap(picture, source, target, paint);
        canvas.restore();
        stroke.setColor(SPEAKER_COLOURS[who]);
        stroke.setAlpha(255);
        stroke.setStrokeWidth(Math.max(dp(2), radius * .1f));
        canvas.drawCircle(x, y, radius, stroke);
    }

    /** A ring of light under something that lies about to be picked up. */
    private void ring(Canvas canvas, float x, float y, int colour) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAlpha(150);
        canvas.drawCircle(x, y, tile * .56f, paint);
        stroke.setColor(colour);
        stroke.setStrokeWidth(tile * .05f);
        canvas.drawCircle(x, y, tile * .56f, stroke);
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
    }

    /**
     * Her: by her jump while she is in the air, flattened as she lands, flickering while she
     * cannot be hurt again; with something in her hands, by the frames drawn of her carrying
     * it; and with whatever is helping her about her.
     */
    private void drawHer(Canvas canvas) {
        float feetX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, feetY = (run.y() + PlusRun.TALL - cameraY) * tile;
        boolean lost = run.state() == PlusRun.LOST, leftward = run.facingLeft();
        float way = leftward ? -1 : 1, tall = tile * 1.62f;
        int weapon = run.weapon();
        if (!lost) {
            if (run.notes() > 0) {
                // The notes: a gold light about her, beating.
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFFFD24A);
                paint.setAlpha(Math.round(90 + 50 * (float) Math.sin(clock * 16)));
                canvas.drawCircle(feetX, feetY - tall * .45f, tall * .62f, paint);
                paint.setColor(Color.BLACK);
            }
            if (run.white()) {
                // White she has not mastered: a faint glow about her, and no rays. The light of the white that is truly hers is brighter, and
                // its rays turn; and while she gathers the beam it draws in.
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                boolean hers = run.mastered();
                float beat = .5f + .5f * (float) Math.sin(clock * (hers ? 4 : 3)), gathers = hers ? run.beamCharge() : 0;
                for (int ring = hers ? 3 : 2; ring >= 1; ring--) {
                    paint.setAlpha(Math.round(hers ? (26 + 14 * beat) * (4 - ring) : (12 + 6 * beat) * (3 - ring)));
                    canvas.drawCircle(feetX, feetY - tall * .48f, tall * ((hers ? .42f : .4f) + ring * (hers ? .16f : .13f) * (1 - .5f * gathers) + .03f * beat), paint);
                }
                if (hers) {
                    paint.setColor(0xFFFFF3C4);
                    for (int ray = 0; ray < 8; ray++) {
                        double turn = clock * (.7f + 3 * gathers) + ray * Math.PI / 4;
                        path.reset();
                        path.moveTo(feetX, feetY - tall * .48f);
                        path.lineTo(feetX + (float) Math.cos(turn - .09) * tall * 1.15f, feetY - tall * .48f + (float) Math.sin(turn - .09) * tall * 1.15f);
                        path.lineTo(feetX + (float) Math.cos(turn + .09) * tall * 1.15f, feetY - tall * .48f + (float) Math.sin(turn + .09) * tall * 1.15f);
                        path.close();
                        paint.setAlpha(Math.round(34 + 22 * beat + 80 * gathers));
                        canvas.drawPath(path, paint);
                    }
                }
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
            }
            int alpha = run.safe() && (int) (run.time() * 14) % 2 == 0 ? 90 : 255;
            // Full of ice cream, she leaves herself behind.
            if (run.rush() > 0 && run.running()) {
                her(canvas, feetX - way * tile * .7f, feetY, tall, weapon, leftward, 40);
                her(canvas, feetX - way * tile * .35f, feetY, tall, weapon, leftward, 80);
            }
            if (run.shadow()) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF140A1E);
                paint.setAlpha(Math.round(90 + 40 * (float) Math.sin(clock * 9)));
                canvas.drawCircle(feetX, feetY - tall * .45f, tall * .7f, paint);
                paint.setColor(Color.BLACK);
                paint.setColorFilter(shade);
                her(canvas, feetX - way * tile * .5f, feetY, tall, weapon, leftward, 70);
                her(canvas, feetX, feetY, tall, weapon, leftward, 230);
                paint.setColorFilter(null);
            } else if (flinchAge < .26f) {
                // Hurt where she stands: the moment of it.
                paint.setAlpha(255);
                frame(canvas, herLose(look), HIT, LOSE_FRAMES, feetX, feetY, tall, leftward);
            } else if (lookAge < LOOK && lookFrom != look) {
                // One colour fades into another, or into her own, or out of it: the one drawn, and the other over it, more and more.
                drawnAs = lookFrom;
                her(canvas, feetX, feetY, tall, weapon, leftward, alpha);
                drawnAs = look;
                her(canvas, feetX, feetY, tall, weapon, leftward, Math.round(alpha * lookAge / LOOK));
                drawnAs = NO_LOOK;
            } else if (look != 0 && run.formLeft() > 0 && run.formLeft() < 2) {
                // About to go, the colour flickers toward her own, faster as it nears the end.
                her(canvas, feetX, feetY, tall, weapon, leftward, alpha);
                float beat = .5f + .5f * (float) Math.sin(clock * (10 + (2 - run.formLeft()) * 9));
                drawnAs = 0;
                her(canvas, feetX, feetY, tall, weapon, leftward, Math.round(alpha * .75f * beat));
                drawnAs = NO_LOOK;
            } else {
                her(canvas, feetX, feetY, tall, weapon, leftward, alpha);
            }
            if (run.mastered() && !run.onGround()) {
                // On the wing in white she sheds feathers, a few with every beat.
                for (int feather = 0; feather < 3; feather++) {
                    float fallen = (clock * .9f + feather / 3f) % 1;
                    paint.setAlpha(Math.round(220 * (1 - fallen)));
                    pieceIn(canvas, two, FEATHER_ART, feetX - way * tile * (.35f + .5f * fallen) + (float) Math.sin(fallen * 9 + feather * 2) * tile * .16f,
                            feetY - tall * .6f + fallen * tile * 1.7f, tile * .34f, fallen * 220 + feather * 70);
                }
                paint.setAlpha(255);
            }
            if (run.shield()) {
                // Rune's ink: a dark ball about her.
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF27348F);
                paint.setAlpha(70);
                canvas.drawCircle(feetX, feetY - tall * .45f, tall * .6f, paint);
                stroke.setColor(0xFF27348F);
                stroke.setAlpha(190);
                stroke.setStrokeWidth(tile * .06f);
                canvas.drawCircle(feetX, feetY - tall * .45f, tall * .6f, stroke);
                stroke.setAlpha(255);
                paint.setColor(Color.BLACK);
            }
            paint.setAlpha(255);
            if (run.charmed() > 0) {
                // Charmed: hearts going round her head.
                for (int heart = 0; heart < 3; heart++) {
                    double turn = clock * 5 + heart * Math.PI * 2 / 3;
                    artIn(canvas, HEART_ART, feetX + (float) Math.cos(turn) * tall * .42f, feetY - tall * .95f + (float) Math.sin(turn) * tall * .12f, tile * .3f);
                }
            }
            // A brush or a sword leaves its mark in the air as it is swung: an arc about her shoulder that ends where the blade
            // or the bristles end in the picture of her using it, which was measured, so the mark is where the thing is.
            float swing = run.swinging();
            if (swing >= 0 && (weapon == PlusRun.BRUSH || weapon == PlusRun.SWORD)) {
                boolean brush = weapon == PlusRun.BRUSH;
                float dot = tall / 202f, shoulderX = feetX + way * 6 * dot, shoulderY = feetY - 100 * dot;
                float reach = (brush ? 155 : 117) * dot, ends = brush ? 21 : 40, begins = -130;
                box.set(shoulderX - reach, shoulderY - reach, shoulderX + reach, shoulderY + reach);
                stroke.setColor(brush ? 0xFFFF5FA8 : Color.WHITE);
                stroke.setAlpha(Math.round(210 * (1 - swing * .55f)));
                stroke.setStrokeWidth(tile * (brush ? .2f : .12f));
                canvas.save();
                if (leftward) canvas.scale(-1, 1, shoulderX, shoulderY);
                // It grows from behind her head round to the tip, and its tail follows after.
                float head = begins + (ends - begins) * Math.min(1, swing * 1.6f), tail = begins + (ends - begins) * Math.max(0, swing * 1.6f - .6f);
                canvas.drawArc(box, tail, Math.max(4, head - tail), false, stroke);
                canvas.restore();
                stroke.setAlpha(255);
            }
            // The colour of her form about her feet, so that it is seen at once.
            if (run.form() != 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(lookColour(lookWanted()));
                paint.setAlpha(Math.round(70 + 40 * (float) Math.sin(clock * 8)));
                canvas.drawOval(feetX - tile * .55f, feetY - tile * .1f, feetX + tile * .55f, feetY + tile * .1f, paint);
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
            }
        }
        if (knockAge >= 0) {
            // Thrown back and up from where she was hurt, turning over as she goes, and down out of sight.
            float since = knockAge, back = knockLeft ? 1 : -1;
            float x = (knockX + back * 2.4f * since - cameraX) * tile, y = (knockY - 10 * since + 15 * since * since - cameraY) * tile;
            int pose = since < .16f ? HIT : (int) ((since - .16f) / .14f) % 2 == 0 ? TUMBLE : DROP;
            paint.setAlpha(Math.round(255 * Math.min(1, (KNOCK - since) / .25f)));
            frame(canvas, herLose(knockForm), pose, LOSE_FRAMES, x, y, tall, knockLeft);
            paint.setAlpha(255);
        }
    }

    /** Halgakos as she is at this moment of the run, her feet at a place: empty-handed, or carrying what she fights with. */
    private void her(Canvas canvas, float feetX, float feetY, float tall, int weapon, boolean leftward, int alpha) {
        paint.setAlpha(alpha);
        // (The beam of the white that is truly hers is fired from her tablet-book: she holds it for as long as the beam is gathered or out.)
        boolean beaming = run.mastered() && (run.beamCharge() > 0 || run.beam() > 0);
        if (run.mastered() && (beaming || sinceBeamEnd < .4f || !run.onGround())) {
            // In the white that is truly hers both her wings are grown, and the beam has pictures of its own: gathered with her
            // tablet-book over her head, let go at arm's length, held, and the book shut against her as it ends; on the ground, and
            // in the air. And in the air with nothing to do her wings beat: four pictures to a beat, and ten of them a second.
            boolean ground = run.onGround();
            int pose = run.beamCharge() > 0 ? (ground ? 4 : 8) : run.beam() > 0 ? (run.beam() > PlusRun.BEAM_LASTS - .22f ? (ground ? 5 : 9) : (ground ? 6 : 10))
                    : sinceBeamEnd < .4f ? (ground ? 7 : 11) : (int) (clock * 10) % 4;
            frame(canvas, herWhite(false), pose, 12, feetX, feetY + (ground ? 0 : tile * .08f), tall, leftward);
            paint.setAlpha(255);
            return;
        }
        if (beaming) weapon = PlusRun.TABLET;
        if (RunLoot.ultimate(perks.item) && weapon == RunArms.kindOf(perks.item)) quillAura(canvas, feetX, feetY, tall, leftward);
        // Swimming: two strokes by turns, a dive, and an ink bolt as it goes; bubbles rising off her.
        if (run.swimming()) {
            if (swim == null) swim = BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_swim);
            int stroke = sinceUse < .25f ? 3 : run.speedDown() > 1.8f ? 2 : (int) (clock * 4) % 2;
            frame(canvas, dressed("swim", formed(swim, swims)), stroke, 4, feetX, feetY + tall * .1f, tall, leftward);
            bubbles(canvas, feetX + (leftward ? -1 : 1) * tall * .3f, feetY - tall * .7f, tall);
            paint.setAlpha(255);
            return;
        }
        if (run.inWater()) bubbles(canvas, feetX, feetY - tall * .85f, tall);
        // Climbing a vine, a ladder or a shelf: hand over hand, a picture for each step of the way up or down, held still where she stops.
        // Dressed as she is, where what she wears has its pictures of climbing; where it has not yet, the rise of her jump in it, so that
        // she never climbs in her own look while she wears another.
        if (run.climbing()) {
            int step = Math.floorMod((int) Math.floor(run.y() * 2.4f), 4);
            if (vanityCovers("climb")) {
                if (climb == null) climb = BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_climb);
                frame(canvas, dressed("climb", formed(climb, climbs)), step, 4, feetX, feetY, tall, leftward);
            } else {
                frame(canvas, herJump(), step % 2 == 0 ? RISE : TOP, JUMP_FRAMES, feetX, feetY, tall, leftward);
            }
            paint.setAlpha(255);
            return;
        }
        // The ground pound and the aerial strike have pictures of their own for each thing she may hold.
        if (drawSmashOrAerial(canvas, weapon, feetX, feetY, tall, leftward)) {
            paint.setAlpha(255);
            return;
        }
        // In the middle of one of her moves, it is drawn, whatever she holds.
        int move = run.moveShown();
        if (move != PlusRun.NO_MOVE) {
            drawMove(canvas, move, feetX, feetY, tall, leftward, alpha);
            paint.setAlpha(255);
            return;
        }
        if (weapon == PlusRun.KATANA) {
            // The katana: the dash's cut, and standing after it; the dash gathered while the button is held; the cut downward; and a draw:
            // out, across, and back in its sheath.
            int moving = !run.onGround() ? (run.speedDown() < 3 ? ARMED_RISE : ARMED_FALL) : run.running() ? ARMED_RUN + (int) (clock * 11) % 4 : 0;
            int pose = dashAge < .3f ? KATANA_DASH : dashAge < .62f ? KATANA_AFTER : dashAge < .8f ? KATANA_SHEATHE : run.heldFor() > .14f ? KATANA_GATHER
                    : airAge < .3f ? KATANA_DOWN : cutAge < .05f ? KATANA_DRAW : cutAge < .2f ? KATANA_CUT : cutAge < .33f ? KATANA_SHEATHE : moving;
            boolean fast = pose == KATANA_CUT || pose == KATANA_DASH || pose == KATANA_DOWN || pose == KATANA_DRAW;
            if (!gripped(canvas, weapon, pose, feetX, feetY, tall, leftward, fast)) frame(canvas, herArmed(weapon), pose, KATANA_FRAMES, feetX, feetY, tall, leftward);
        } else if (weapon == PlusRun.KNUCKLES) {
            // Knuckles, a combo that reads as one: a jab is wound (low) and thrown (out); the third of three, the uppercut, is low and then the fist
            // up; the haymaker is gathered while it is held, and thrown long.
            int moving = !run.onGround() ? (run.speedDown() < 3 ? ARMED_RISE : ARMED_FALL) : run.running() ? ARMED_RUN + (int) (clock * 11) % 4 : 0;
            int pose = haymakerAge < .34f ? MAIN_GO : run.heldFor() > .12f ? MAIN_GATHER : uppercutAge < .06f ? MAIN_BACK : uppercutAge < .3f ? MAIN_HEAVY
                    : sinceUse < .07f ? MAIN_BACK : sinceUse < .22f ? MAIN_GO : moving;
            weaponAura(canvas, weapon, feetX + (leftward ? -1 : 1) * tall * .32f, feetY - tall * .5f, tall);
            if (!gripped(canvas, weapon, pose, feetX, feetY, tall, leftward, pose == MAIN_GO || pose == MAIN_HEAVY)) frame(canvas, herArmed(weapon), pose, MAIN_FRAMES, feetX, feetY, tall, leftward);
        } else if (weapon >= PlusRun.TABLET) {
            int pose = beaming ? (run.beam() > 0 ? MAIN_HEAVY : MAIN_GATHER) : sinceHeavy < .3f ? MAIN_HEAVY : sinceHeavy < .5f ? MAIN_SETTLE
                    : run.heldFor() > .12f ? MAIN_GATHER : sinceUse < .09f ? MAIN_BACK : sinceUse < .26f ? MAIN_GO
                    : !run.onGround() ? (run.speedDown() < 3 ? ARMED_RISE : ARMED_FALL) : run.running() ? ARMED_RUN + (int) (clock * 11) % 4 : 0;
            weaponAura(canvas, weapon, feetX + (leftward ? -1 : 1) * tall * .32f, feetY - tall * .5f, tall);
            if (!gripped(canvas, weapon, pose, feetX, feetY, tall, leftward)) frame(canvas, herArmed(weapon), pose, MAIN_FRAMES, feetX, feetY, tall, leftward);
        } else if (weapon != PlusRun.UNARMED) {
            int pose = sinceUse < .22f || run.swinging() >= 0 ? ARMED_USE : !run.onGround() ? (run.speedDown() < 3 ? ARMED_RISE : ARMED_FALL)
                    : run.running() ? ARMED_RUN + (int) (clock * 11) % 4 : 0;
            if (!gripped(canvas, weapon, pose, feetX, feetY, tall, leftward, pose == ARMED_USE)) frame(canvas, herArmed(weapon), pose, ARMED_FRAMES, feetX, feetY, tall, leftward);
        } else {
            int pose;
            if (!run.onGround()) {
                float speed = run.speedDown();
                pose = speed < -11 ? PUSH_OFF : speed < -4 ? RISE : speed < 4 ? TOP : FALL;
            } else {
                pose = squash > 0 ? LAND : run.running() ? RUN : STAND;
            }
            drawHer(canvas, feetX, feetY, tall, pose, leftward);
        }
        paint.setAlpha(255);
    }

    // Water

    /** Bubbles rising from about her head, three at a time. */
    private void bubbles(Canvas canvas, float x, float y, float tall) {
        stroke.setColor(0xFFE8F6FF);
        stroke.setStrokeWidth(Math.max(1, tall * .015f));
        for (int one = 0; one < 3; one++) {
            float age = (clock * .9f + one * .33f) % 1;
            stroke.setAlpha(Math.round(220 * (1 - age)));
            canvas.drawCircle(x + (float) Math.sin(age * 9 + one) * tall * .06f, y - age * tall * .9f, tall * (.025f + one * .012f), stroke);
        }
        stroke.setAlpha(255);
    }

    /** Her breath, under her level, while she is in deep water without the blue ribbon: a bubble for each second of it left. */
    private void drawBreath(Canvas canvas, float left, float top) {
        if (!run.inWater() || run.has(PlusRun.BLUE)) return;
        int bubbles = (int) Math.ceil(Math.max(0, run.breath()));
        float size = dp(9);
        box.set(left, top, left + size * 2.2f * 8 + dp(16), top + dp(22));
        plate(canvas, box, dp(11), 235);
        for (int one = 0; one < 8; one++) {
            float x = left + dp(12) + one * size * 2.2f, y = top + dp(11);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(one < bubbles ? 0xFF6FB8FF : dark ? 0xFF3A3F45 : 0xFFE4E6E8);
            canvas.drawCircle(x, y, size * .8f, paint);
        }
        if (run.breath() <= 0 && (int) (clock * 6) % 2 == 0) {
            stroke.setColor(0xFFE0362F);
            stroke.setStrokeWidth(dp(2));
            canvas.drawRoundRect(box, dp(11), dp(11), stroke);
        }
        paint.setColor(Color.BLACK);
    }

    // Hideouts

    /** Where a hideout's way in is and not yet broken into: a glint now and then, so that a sharp eye finds it; in its hideout, its boss's name and bar. */
    private void drawHideouts(Canvas canvas, int first, int last) {
        RunLevels.Level level = run.level();
        if (run.room() < 0) {
            for (int index = 0; index < level.rooms.size(); index++) {
                int[] one = level.rooms.get(index);
                if (one[0] != RunLevels.HIDEOUT || (run.roomsFound() & 1 << index) != 0 || one[1] < first - 2 || one[1] > last + 2) continue;
                float x = (one[1] + .5f - cameraX) * tile, y = (one[2] - 1 - cameraY) * tile, beat = (clock * .6f + index * .37f) % 1;
                if (beat < .35f) {
                    float through = beat / .35f, size = tile * (.25f + .35f * (float) Math.sin(through * Math.PI));
                    glowAt(canvas, x, y, size * 1.8f, 0xFFFFF2B0, Math.round(200 * (float) Math.sin(through * Math.PI)));
                    stroke.setColor(Color.WHITE);
                    stroke.setStrokeWidth(tile * .04f);
                    canvas.drawLine(x - size, y, x + size, y, stroke);
                    canvas.drawLine(x, y - size, x, y + size, stroke);
                }
            }
            return;
        }
        if (level.rooms.get(run.room())[0] != RunLevels.HIDEOUT) return;
        for (PlusRun.Foe foe : run.foes()) {
            if (!RunUniques.hidden(foe.kind) || foe.flat) continue;
            float wide = Math.min(safe.width() - dp(24), dp(300)), from = safe.centerX() - wide / 2, top = safe.top + dp(118);
            box.set(from, top, from + wide, top + dp(40));
            plate(canvas, box, dp(18), 235);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(0xFF7A3A9A);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, RunUniques.name(foe.kind) + "  ·  hidden", from + dp(14), top + dp(16), dp(12.5f), wide - dp(28));
            text.setTypeface(Typeface.DEFAULT);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
            canvas.drawRoundRect(from + dp(14), top + dp(24), from + wide - dp(14), top + dp(32), dp(4), dp(4), paint);
            paint.setColor(0xFF9A4AC0);
            canvas.drawRoundRect(from + dp(14), top + dp(24), from + dp(14) + (wide - dp(28)) * Math.max(0, foe.left) / Math.max(1f, foe.most), top + dp(32), dp(4), dp(4), paint);
            paint.setColor(Color.BLACK);
            return;
        }
    }

    /**
     * Pieces in front of her for depth: paper grass, a stack of books, a fern, a column, crumpled paper, along the foot of the screen, going by
     * faster than the land does; the same ones in the same places every time.
     */
    private void drawForeground(Canvas canvas) {
        if (run == null || run.room() >= 0) return;
        if (foreground == null) foreground = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_foreground);
        if (foreground == null) return;
        int cell = foreground.getHeight();
        float pace = 1.35f, view = cameraX * pace, size = tile * 1.7f;
        int from = (int) Math.floor(view / 7) - 1, to = (int) Math.ceil((view + width / tile) / 7) + 1;
        paint.setAlpha(225);
        for (int slot = from; slot <= to; slot++) {
            long dice = (slot * 2654435761L + number * 97L) & 0xFFFFFFFFL;
            if (dice % 3 != 0) continue;
            int piece = (int) (dice / 3 % 8);
            // (The two that hang, from the top; the rest stand at the foot.)
            boolean hangs = piece == 3 || piece == 7;
            float x = (slot * 7 + (dice >> 8) % 5 - view) * tile;
            float y = hangs ? -tile * .2f : height - size * .82f;
            source.set(piece * cell, 0, (piece + 1) * cell, cell);
            target.set(x - size / 2, y, x + size / 2, y + size);
            canvas.drawBitmap(foreground, source, target, paint);
        }
        paint.setAlpha(255);
    }

    // Paper trees, and crafting with paper

    /** The paper trees: each standing, shaking as it is chopped, with pips of the blows it still takes; once down, a stump. */
    private void drawTrees(Canvas canvas, int first, int last) {
        List<int[]> trees = run.trees();
        if (trees.isEmpty()) return;
        if (paperSheet == null) paperSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_cut_trees);
        if (paperSheet == null) return;
        int cell = paperSheet.getHeight();
        for (int index = 0; index < trees.size(); index++) {
            int[] tree = trees.get(index);
            if (tree[0] < first - 3 || tree[0] > last + 3) continue;
            boolean down = tree[3] <= 0, struck = run.treeAt() == index && run.treeStruck() < .25f;
            float middle = (tree[0] + .5f - cameraX) * tile + (struck && !down ? (float) Math.sin(run.treeStruck() * 70) * tile * .06f : 0), feet = (tree[1] + 1 - cameraY) * tile;
            float size = (down ? 1.3f : tree[2] == RunCraft.GOLDEN ? 3.1f : tree[2] == RunCraft.FINE ? 2.9f : 2.7f) * tile;
            int frame = down ? 3 : tree[2];
            source.set(frame * cell, 0, (frame + 1) * cell, cell);
            target.set(middle - size / 2, feet - size + tile * .05f, middle + size / 2, feet + tile * .05f);
            paint.setAlpha(255);
            if (!down && tree[2] == RunCraft.GOLDEN) glowAt(canvas, middle, feet - size * .6f, size * .55f, 0xFFFFD45A, 90 + (int) (40 * Math.sin(clock * 3 + index)));
            canvas.drawBitmap(paperSheet, source, target, paint);
            if (!down && struck) {
                int most = RunCraft.treeHolds(tree[2]);
                float pip = tile * .1f, gap = tile * .04f, wide = most * pip + (most - 1) * gap, row = feet - size - tile * .1f;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xCC15171A);
                canvas.drawRoundRect(middle - wide / 2 - tile * .04f, row - pip / 2 - tile * .04f, middle + wide / 2 + tile * .04f, row + pip / 2 + tile * .04f, pip, pip, paint);
                for (int one = 0; one < most; one++) {
                    paint.setColor(one < tree[3] ? 0xFFF5EEDC : 0xFF4A4F54);
                    canvas.drawRect(middle - wide / 2 + one * (pip + gap), row - pip / 2, middle - wide / 2 + one * (pip + gap) + pip, row + pip / 2, paint);
                }
                paint.setColor(Color.BLACK);
            }
        }
    }

    /** A picture from the strip of the trees: 4 plain paper, 5 parchment, 6 a golden page, 7 a hatchet. */
    private void paperIcon(Canvas canvas, int frame, float x, float y, float size) {
        if (paperSheet == null) paperSheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_cut_trees);
        if (paperSheet == null) return;
        int cell = paperSheet.getHeight();
        source.set(frame * cell, 0, (frame + 1) * cell, cell);
        target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        paint.setAlpha(255);
        canvas.drawBitmap(paperSheet, source, target, paint);
    }

    private int paper(int grade) { return session.tally("run_paper_" + grade); }

    private void spendPaper(int grade, int many) { session.keep("run_paper_" + grade, Math.max(0, paper(grade) - many)); }

    /** What she has made of the Roses, a bit for each kind; and whether the Quill is made. */
    private int rosesMade() { return session.tally("run_roses"); }

    /** What she can afford, on top of the smith's: paper of each grade. */
    private boolean affordsPaper(int plain, int fine, int golden) { return paper(RunCraft.PLAIN) >= plain && paper(RunCraft.FINE) >= fine && paper(RunCraft.GOLDEN) >= golden; }

    /** Something made at the Craft tab goes into her bag. */
    private void intoBag(int code) {
        List<Integer> bag = RunLoot.bag(session.note("run_bag"));
        session.keepNote("run_bag", RunLoot.note(RunLoot.add(bag, code)));
    }

    /**
     * The Craft tab: her paper at its head; the twelve kinds to choose from; and four things to make. A paper weapon of the kind looked at,
     * folded (blue with fine paper in it). The weapon in her hands honed a rarity higher. A Halgakos Rose of the kind looked at. And, once
     * every Rose is made, the Mother Tongue's Quill.
     */
    private void drawCraft(Canvas canvas, float left, float y, float wide, float pad, float bottom, int bank, int[] has) {
        // Her paper.
        float icon = dp(26);
        for (int grade = 0; grade < RunCraft.GRADES; grade++) {
            float x = left + pad + grade * (wide - pad * 2) / 3;
            paperIcon(canvas, 4 + grade, x + icon / 2, y + icon / 2, icon);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, paper(grade) + " " + RunCraft.PAPER_NAMES[grade].toLowerCase(java.util.Locale.ROOT), x + icon + dp(4), y + icon / 2 + dp(5), dp(12), (wide - pad * 2) / 3 - icon - dp(8));
        }
        y += icon + dp(8);
        // The thirteen kinds, seven to a row.
        float cell = (wide - pad * 2) / 7;
        for (int base = 0; base < RunCraft.KINDS; base++) {
            if (craftKinds[base] == null) craftKinds[base] = new RectF();
            RectF at = craftKinds[base];
            at.set(left + pad + base % 7 * cell + dp(2), y + base / 7 * cell + dp(2), left + pad + (base % 7 + 1) * cell - dp(2), y + (base / 7 + 1) * cell - dp(2));
            boolean made = (rosesMade() >> base & 1) != 0;
            lootFrame(canvas, made ? RunCraft.rose(base) : RunCraft.folded(base, false), at, base == craftBase);
        }
        y += cell * 2 + dp(8);
        // The four things to make.
        int level = herLevel(), hand = handCode();
        String kind = RunLoot.WEAPONS[craftBase];
        boolean blue = paper(RunCraft.FINE) >= RunCraft.FOLD_FINE;
        boolean roseMade = (rosesMade() >> craftBase & 1) != 0, quillMade = session.tally("run_quill") != 0;
        String[][] rows = {
                {"Fold a paper " + kind.toLowerCase(java.util.Locale.ROOT) + (blue ? " (blue)" : " (white)"),
                        RunCraft.FOLD_PLAIN + " plain" + (blue ? ", " + RunCraft.FOLD_FINE + " fine" : "") + ", + " + RunCraft.FOLD_PLUSES},
                {RunCraft.honeable(hand) ? "Hone " + RunLoot.name(hand) + " to " + RunLoot.RARITIES[RunLoot.rarity(hand) + 1].toLowerCase(java.util.Locale.ROOT) : "Hone the weapon in her hands",
                        RunCraft.honeable(hand) ? RunCraft.honeFine(RunLoot.rarity(hand)) + " fine, " + RunCraft.honeLeaf(RunLoot.rarity(hand)) + " gold leaf, + " + RunCraft.honePluses(RunLoot.rarity(hand))
                                : "Take a found weapon (white to gold) in hand first"},
                {"Halgakos Rose " + kind, roseMade ? "Made" : RunCraft.ROSE_GOLDEN + " golden, " + RunCraft.ROSE_LEAF + " leaf, " + RunCraft.ROSE_CRYSTAL + " crystal, + " + RunCraft.ROSE_PLUSES
                        + (level < RunCraft.ROSE_LEVEL ? "  ·  from level " + RunCraft.ROSE_LEVEL : "")},
                {"The Mother Tongue's Quill", quillMade ? "Made: the one above all others" : Integer.bitCount(rosesMade()) + " of " + RunCraft.KINDS + " Roses made  ·  " + RunCraft.QUILL_GOLDEN + " golden, "
                        + RunCraft.QUILL_LEAF + " leaf, + " + RunCraft.QUILL_PLUSES + (level < RunCraft.QUILL_LEVEL ? ", level " + RunCraft.QUILL_LEVEL : "")}};
        boolean[] can = {
                affordsPaper(RunCraft.FOLD_PLAIN, blue ? RunCraft.FOLD_FINE : 0, 0) && bank >= RunCraft.FOLD_PLUSES,
                RunCraft.honeable(hand) && affordsPaper(0, RunCraft.honeFine(RunLoot.rarity(hand)), 0) && has[2] >= RunCraft.honeLeaf(RunLoot.rarity(hand)) && bank >= RunCraft.honePluses(RunLoot.rarity(hand)),
                !roseMade && level >= RunCraft.ROSE_LEVEL && affordsPaper(0, 0, RunCraft.ROSE_GOLDEN) && has[2] >= RunCraft.ROSE_LEAF && has[1] >= RunCraft.ROSE_CRYSTAL && bank >= RunCraft.ROSE_PLUSES,
                !quillMade && RunCraft.quillOpen(rosesMade(), level) && affordsPaper(0, 0, RunCraft.QUILL_GOLDEN) && has[2] >= RunCraft.QUILL_LEAF && bank >= RunCraft.QUILL_PLUSES};
        float row = Math.min(dp(62), (bottom - y - dp(20)) / 4);
        for (int index = 0; index < 4; index++) {
            float top = y + index * row;
            box.set(left + dp(8), top, left + wide - dp(8), top + row - dp(6));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(index == 3 ? (dark ? 0xFF3A2A33 : 0xFFFCEFF3) : dark ? 0xFF2C3033 : 0xFFF5F6F7);
            canvas.drawRoundRect(box, dp(12), dp(12), paint);
            if (index == 3) drawIcon(canvas, RunLoot.QUILL_ICON, box.left + dp(22), box.centerY(), row * .9f);
            float words = box.left + (index == 3 ? dp(44) : dp(12)), button = dp(84);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, rows[index][0], words, box.top + dp(20), dp(13), box.right - button - dp(12) - words);
            text.setTypeface(Typeface.DEFAULT);
            text.setColor(soft);
            fitted(canvas, rows[index][1], words, box.top + dp(38), dp(11), box.right - button - dp(12) - words);
            craftActs[index].set(box.right - button - dp(6), box.centerY() - dp(16), box.right - dp(6), box.centerY() + dp(16));
            smithButton(canvas, craftActs[index], index == 0 ? "Fold" : index == 1 ? "Hone" : "Craft", can[index], index >= 2 ? 0xFFC0407A : CalculatorLayout.CORAL);
            if (!can[index]) craftActs[index].setEmpty();
        }
        if (!smithSaid.isEmpty()) {
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(0xFF2E9E6E);
            fitted(canvas, smithSaid, left + pad, bottom - dp(4), dp(12.5f), wide - pad * 2);
        }
    }

    private void craftTouch(float x, float y, int bank, int[] has) {
        for (int base = 0; base < RunCraft.KINDS; base++) {
            if (craftKinds[base] != null && craftKinds[base].contains(x, y)) {
                craftBase = base;
                smithSaid = "";
                audio.play("tap", .5f);
                return;
            }
        }
        int hand = handCode();
        for (int index = 0; index < 4; index++) {
            if (craftActs[index].isEmpty() || !craftActs[index].contains(x, y)) continue;
            if (index == 0) {
                boolean blue = paper(RunCraft.FINE) >= RunCraft.FOLD_FINE;
                spendPaper(RunCraft.PLAIN, RunCraft.FOLD_PLAIN);
                if (blue) spendPaper(RunCraft.FINE, RunCraft.FOLD_FINE);
                pay(RunCraft.FOLD_PLUSES, new int[]{0, 0, 0});
                int code = RunCraft.folded(craftBase, blue);
                intoBag(code);
                made(RunGear.MORU, RunLoot.name(code) + " is folded, and in her bag");
            } else if (index == 1) {
                int rarity = RunLoot.rarity(hand);
                spendPaper(RunCraft.FINE, RunCraft.honeFine(rarity));
                pay(RunCraft.honePluses(rarity), new int[]{0, 0, RunCraft.honeLeaf(rarity)});
                int honed = RunCraft.honed(hand);
                List<Integer> bag = RunLoot.bag(session.note("run_bag"));
                int place = bag.indexOf(hand);
                if (place >= 0) bag.set(place, honed);
                else bag = RunLoot.add(bag, honed);
                session.keepNote("run_bag", RunLoot.note(bag));
                session.keepNote("run_hand", String.valueOf(honed));
                if (run != null) run.hold(honed);
                made(RunGear.MORU, RunLoot.name(honed) + " is honed to " + RunLoot.RARITIES[rarity + 1].toLowerCase(java.util.Locale.ROOT));
            } else if (index == 2) {
                spendPaper(RunCraft.GOLDEN, RunCraft.ROSE_GOLDEN);
                pay(RunCraft.ROSE_PLUSES, new int[]{0, RunCraft.ROSE_CRYSTAL, RunCraft.ROSE_LEAF});
                session.keep("run_roses", rosesMade() | 1 << craftBase);
                int code = RunCraft.rose(craftBase);
                intoBag(code);
                made(RunGear.MORU, RunLoot.name(code) + ": made of golden pages, and in her bag");
            } else {
                spendPaper(RunCraft.GOLDEN, RunCraft.QUILL_GOLDEN);
                pay(RunCraft.QUILL_PLUSES, new int[]{0, 0, RunCraft.QUILL_LEAF});
                session.keep("run_quill", 1);
                intoBag(RunLoot.QUILL);
                made(RunGear.MORU, "The Mother Tongue's Quill. Take it in hand from her bag");
            }
            return;
        }
    }

    /**
     * About her, holding the Mother Tongue's Quill: a slow glow of rose and gold and letters drifting up off it; and, as its wave goes out,
     * a ring of light and letters spreading as far as it reaches.
     */
    private void quillAura(Canvas canvas, float feetX, float feetY, float tall, boolean leftward) {
        float hand = feetX + (leftward ? -1 : 1) * tall * .3f, high = feetY - tall * .55f;
        glowAt(canvas, hand, high, tall * .7f, RunLoot.rose(clock), 110 + (int) (40 * Math.sin(clock * 3)));
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        String letters = "SLOPPY";
        for (int one = 0; one < 5; one++) {
            float age = (clock * .7f + one * .2f) % 1;
            text.setTextSize(tall * .12f * (1 - age * .4f));
            text.setColor(0xFFFFD45A);
            text.setAlpha(Math.round(220 * (1 - age)));
            canvas.drawText(String.valueOf(letters.charAt(one)), hand + (float) Math.sin(one * 2.3f + clock) * tall * .25f, high - age * tall * .8f, text);
        }
        text.setAlpha(255);
        if (run.quillAge() < .8f) {
            float through = run.quillAge() / .8f, reach = PlusRun.QUILL_REACH * tile * through;
            stroke.setColor(0xFFFFD45A);
            stroke.setAlpha(Math.round(230 * (1 - through)));
            stroke.setStrokeWidth(tile * .18f * (1 - through) + 2);
            canvas.drawCircle(feetX, feetY - tall * .5f, reach, stroke);
            stroke.setColor(RunLoot.rose(clock));
            canvas.drawCircle(feetX, feetY - tall * .5f, reach * .85f, stroke);
            stroke.setAlpha(255);
            glowAt(canvas, feetX, feetY - tall * .5f, reach * .6f, 0xFFFFE9A8, Math.round(140 * (1 - through)));
        }
        text.setTypeface(Typeface.DEFAULT);
    }

    // Her moves

    private Bitmap moves;

    private Bitmap movesSheet() {
        if (moves == null) moves = BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_moves);
        return dressed("moves", formed(moves, movesDyed));
    }

    /** Her swimming, her moves and her climbing in the colour of each form she has been drawn in, kept as they are made. */
    private final android.util.SparseArray<Bitmap> swims = new android.util.SparseArray<>(), movesDyed = new android.util.SparseArray<>(), climbs = new android.util.SparseArray<>();
    /** Her climbing: four pictures, hand over hand. */
    private Bitmap climb;
    /** For each sheet and look, whether what she wears has its pictures of that sheet in the app (her own look always has). */
    private final java.util.Map<String, Boolean> covered = new java.util.HashMap<>();

    /**
     * Whether what she wears now has its pictures of a sheet: an outfit drawn whole has its own picture of it, one dyed has its mask, and
     * each head part (ears, horns) its patch. Where something has not, the sheet would show her own look, and is not to be used.
     */
    private boolean vanityCovers(String sheet) {
        RunVanity.Style style = vanityNow();
        if (style == null) return true;
        String key = sheet + "|" + style.key();
        Boolean known = covered.get(key);
        if (known != null) return known;
        boolean all = true;
        if (style.art != null) all = asset("vanity/" + style.art + "/" + sheet + ".webp");
        else if (style.mid != 0) all = VanityLayers.geometry(sheet) != null && context.getResources().getIdentifier("vanity_mask_" + sheet, "drawable", context.getPackageName()) != 0;
        String horns = RunVanity.HORNS_ART[style.horns], ears = RunVanity.EARS_ART[style.ears];
        if (horns != null) all &= asset("vanity/heads/" + horns + "/" + sheet + ".webp");
        if (ears != null) all &= asset("vanity/heads/" + ears + "/" + sheet + ".webp");
        covered.put(key, all);
        return all;
    }

    private boolean asset(String path) {
        try (java.io.InputStream in = context.getAssets().open(path)) {
            return in != null;
        } catch (java.io.IOException missing) {
            return false;
        }
    }

    /** A sheet of her in the colour she is drawn in now: as it is in her own colour, or dyed once and kept. */
    private Bitmap formed(Bitmap from, android.util.SparseArray<Bitmap> kept) {
        int form = drawn();
        if (form == 0) return from;
        Bitmap dyed = kept.get(form);
        if (dyed == null) {
            dyed = dyed(from, form);
            kept.put(form, dyed);
        }
        return dyed;
    }

    /**
     * One of her moves: the three pictures of a flip, a dash with two fading copies of her behind it, the drop of a ground pound (and the
     * ring it sends out as she lands), a roll turning over, clinging to a wall (facing it), and gliding under her book.
     */
    private void drawMove(Canvas canvas, int move, float feetX, float feetY, float tall, boolean leftward, int alpha) {
        Bitmap sheet = movesSheet();
        int count = PlusRun.MOVE_GLIDE + 1;
        if (move == PlusRun.MOVE_DASH) {
            float back = leftward ? 1 : -1;
            for (int ghost = 2; ghost >= 1; ghost--) {
                paint.setAlpha(Math.round(alpha * (ghost == 2 ? .22f : .45f)));
                frame(canvas, sheet, move, count, feetX + back * ghost * tile * .45f, feetY, tall, leftward);
            }
            paint.setAlpha(alpha);
            frame(canvas, sheet, move, count, feetX, feetY, tall, leftward);
        } else if (move == PlusRun.MOVE_ROLL) {
            canvas.save();
            canvas.rotate((leftward ? -1 : 1) * 360 * run.rollThrough(), feetX, feetY - tall * .3f);
            frame(canvas, sheet, move, count, feetX, feetY, tall, leftward);
            canvas.restore();
        } else if (move == PlusRun.MOVE_WALL) {
            frame(canvas, sheet, move, count, feetX, feetY, tall, run.wallSide() < 0);
        } else {
            frame(canvas, sheet, move, count, feetX, feetY, tall, leftward);
        }
    }

    // Her hands, for a weapon found

    private final Bitmap[] gripSheets = new Bitmap[RunGrips.KINDS.length], gripOvers = new Bitmap[RunGrips.KINDS.length];
    /** The frame drawn last for the weapon in her hands, and how long since it changed: for the smear of a fast swing. */
    private int lastGripFrame = -1;
    private float gripChanged = 9;

    /** The icon of the weapon she holds, if it is a weapon found of this kind; or -1. */
    int heldIcon(int weapon) { return RunGrips.heldIcon(perks.item, weapon); }

    /**
     * She holds a weapon found, of a kind with a grip: drawn from her grip frame (in her form's colour, and dressed as she is), the weapon the
     * item's own picture along the frame's line, and her hands over it. Whether she was drawn so.
     */
    private boolean gripped(Canvas canvas, int weapon, int pose, float feetX, float feetY, float tall, boolean leftward) {
        return gripped(canvas, weapon, pose, feetX, feetY, tall, leftward, pose == MAIN_GO || pose == MAIN_HEAVY || pose == MAIN_BACK);
    }

    private boolean gripped(Canvas canvas, int weapon, int pose, float feetX, float feetY, float tall, boolean leftward, boolean fast) {
        int grip = RunGrips.of(weapon), icon = heldIcon(weapon);
        if (grip < 0 || icon < 0) return false;
        float[] line = pose < RunGrips.LINES[grip].length ? RunGrips.LINES[grip][pose] : null;
        // (A katana found, out of its sheath in her hands: its blade drawn out; at her hip, sheathed as its icon.)
        if (weapon == PlusRun.KATANA && !RunLoot.paper(perks.item) && pose < RunGrips.HIP[grip].length && !RunGrips.HIP[grip][pose]) icon = RunGrips.bladeIcon(perks.item);
        short[] axis = RunGrips.axis(icon);
        if (line == null || axis == null) return false;
        if (gripSheets[grip] == null) {
            int body = context.getResources().getIdentifier("run_grip_" + RunGrips.NAMES[grip], "drawable", context.getPackageName());
            int over = context.getResources().getIdentifier("run_grip_" + RunGrips.NAMES[grip] + "_over", "drawable", context.getPackageName());
            if (body == 0 || over == 0) return false;
            gripSheets[grip] = BitmapFactory.decodeResource(context.getResources(), body);
            gripOvers[grip] = BitmapFactory.decodeResource(context.getResources(), over);
        }
        int frames = RunGrips.FRAMES[grip], form = drawn();
        Bitmap body = dressed("grip" + weapon, form == 0 ? gripSheets[grip] : dyedGrip(grip, form)), over = dressedOver(body, gripOvers[grip]);
        // Where a point of the cell is on the screen: as frame() places the cell.
        float scale = tall / (pet.getHeight() / (float) PetArt.FRAMES.length), cw = body.getWidth() / (float) frames, ch = body.getHeight();
        float down = grounded(body, ch * scale);
        // The smear: on a fast frame, the weapon where it was a moment ago too, fading.
        if (pose != lastGripFrame) {
            gripChanged = 0;
        }
        float[] was = lastGripFrame >= 0 && lastGripFrame < RunGrips.LINES[grip].length ? RunGrips.LINES[grip][lastGripFrame] : null;
        canvas.save();
        if (leftward) canvas.scale(-1, 1, feetX, feetY);
        canvas.translate(feetX - cw * scale / 2, feetY + down - ch * scale);
        canvas.scale(scale, scale);
        if (fast && was != null && gripChanged < .2f) {
            for (int ghost = 3; ghost >= 1; ghost--) {
                float through = ghost / 4f;
                float[] mix = {was[0] + (line[0] - was[0]) * (1 - through), was[1] + (line[1] - was[1]) * (1 - through), was[2] + (line[2] - was[2]) * (1 - through),
                        was[3] + (line[3] - was[3]) * (1 - through)};
                heldWeapon(canvas, icon, axis, mix, weapon, Math.round(90 * (1 - through) * (1 - gripChanged / .2f)));
            }
        }
        // The slash trail along where its tip went: the real thing's bright, in its rarity's colour and wider the rarer; paper's thin and plain.
        if (fast && was != null && gripChanged < .25f) {
            boolean plain = RunLoot.paper(perks.item);
            int rarity = RunLoot.rarity(perks.item);
            float fade = 1 - gripChanged / .25f;
            path.reset();
            path.moveTo(was[2], was[3]);
            float midX = (was[2] + line[2]) / 2 + (line[1] - was[1]) * .3f, midY = (was[3] + line[3]) / 2 - (line[0] - was[0]) * .3f;
            path.quadTo(midX, midY, line[2], line[3]);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setColor(plain ? 0xFFE8E4DA : rarityColour(Math.max(RunLoot.BLUE, rarity)));
            stroke.setAlpha(Math.round((plain ? 120 : 200) * fade));
            stroke.setStrokeWidth(plain ? 4 : 8 + rarity * 3);
            canvas.drawPath(path, stroke);
            if (!plain) {
                stroke.setColor(Color.WHITE);
                stroke.setAlpha(Math.round(230 * fade));
                stroke.setStrokeWidth(3 + rarity);
                canvas.drawPath(path, stroke);
            }
            stroke.setAlpha(255);
            stroke.setStrokeCap(Paint.Cap.BUTT);
        }
        heldWeapon(canvas, icon, axis, line, weapon, 255);
        // A gleam along a real blade now and then.
        if (!RunLoot.paper(perks.item)) {
            float gleam = (clock * .7f + weapon * .3f) % 2.2f;
            if (gleam < 1) {
                float gx = line[0] + (line[2] - line[0]) * (.4f + .6f * gleam), gy = line[1] + (line[3] - line[1]) * (.4f + .6f * gleam);
                glowAt(canvas, gx, gy, 14, 0xFFFFFFFF, Math.round(200 * (float) Math.sin(gleam * Math.PI)));
                paint.setAlpha(255);
            }
        }
        canvas.restore();
        lastGripFrame = pose;
        frame(canvas, body, pose, frames, feetX, feetY, tall, leftward);
        frame(canvas, over, pose, frames, feetX, feetY, tall, leftward);
        return true;
    }

    /** The weapon's own picture laid along a grip line (in the cell's own pixels): its handle at the grip, its tip toward where the line points. */
    private void heldWeapon(Canvas canvas, int icon, short[] axis, float[] line, int weapon, int alpha) {
        float hx = axis[1], hy = axis[2], tx = axis[3], ty = axis[4];
        float iconLength = (float) Math.hypot(tx - hx, ty - hy), lineLength = (float) Math.hypot(line[2] - line[0], line[3] - line[1]);
        // (Never smaller than reads at the game's size: a knife or a pen is shown as long as a third of her.)
        float length = Math.max(lineLength, weapon == PlusRun.KNIFE || weapon == PlusRun.PEN ? 75 : weapon == PlusRun.KNUCKLES ? 40 : 0) * 1.05f;
        float scale = length / Math.max(1, iconLength);
        double angle = Math.atan2(line[3] - line[1], line[2] - line[0]) - Math.atan2(ty - hy, tx - hx);
        canvas.save();
        canvas.translate(line[0], line[1]);
        canvas.rotate((float) Math.toDegrees(angle));
        canvas.scale(scale, scale);
        canvas.translate(-hx, -hy);
        if (lootIcons == null) lootIcons = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_loot_icons);
        int column = icon % LOOT_COLUMNS, row = icon / LOOT_COLUMNS;
        source.set(column * LOOT_ICON, row * LOOT_ICON, (column + 1) * LOOT_ICON, (row + 1) * LOOT_ICON);
        target.set(0, 0, LOOT_ICON, LOOT_ICON);
        int was = paint.getAlpha();
        paint.setAlpha(alpha);
        canvas.drawBitmap(lootIcons, source, target, paint);
        paint.setAlpha(was);
        canvas.restore();
    }

    /** A grip sheet in her form's colour, made once for each. */
    private final android.util.SparseArray<Bitmap> dyedGrips = new android.util.SparseArray<>();

    private Bitmap dyedGrip(int grip, int form) {
        int key = grip * 64 + form;
        Bitmap made = dyedGrips.get(key);
        if (made == null) {
            made = dyedArm(gripSheets[grip], RunGrips.KINDS[grip], form);
            dyedGrips.put(key, made);
        }
        return made;
    }

    /** About the weapon found in her hands, by its rarity: a glow from gold; flames and lightning from red; the rose's changing colour and petals. */
    private void weaponAura(Canvas canvas, int weapon, float x, float y, float tall) {
        int item = perks.item;
        if (item == 0 || RunArms.kindOf(item) != weapon) return;
        int rarity = RunLoot.rarity(item);
        if (rarity < RunLoot.GOLD) return;
        int colour = rarityColour(rarity);
        glowAt(canvas, x, y, tall * (.35f + .05f * rarity), colour, 90 + (int) (40 * Math.sin(clock * 5)));
        paint.setStyle(Paint.Style.FILL);
        if (rarity == RunLoot.RED) {
            // Flames licking up, and now and then a crack of lightning.
            for (int flame = 0; flame < 4; flame++) {
                float rise = (clock * 1.6f + flame * .25f) % 1, fx = x + (float) Math.sin(clock * 7 + flame * 1.7f) * tall * .12f, fy = y + tall * .1f - rise * tall * .35f;
                paint.setColor(flame % 2 == 0 ? 0xFFFF6A2A : 0xFFFFD34A);
                paint.setAlpha(Math.round(220 * (1 - rise)));
                canvas.drawCircle(fx, fy, tall * .045f * (1 - rise * .6f), paint);
            }
            if ((int) (clock * 3) % 4 == 0) {
                stroke.setColor(0xFFFFF4C2);
                stroke.setStrokeWidth(tall * .012f);
                path.reset();
                path.moveTo(x - tall * .1f, y - tall * .2f);
                path.lineTo(x, y - tall * .08f);
                path.lineTo(x - tall * .04f, y + tall * .02f);
                path.lineTo(x + tall * .1f, y + tall * .15f);
                canvas.drawPath(path, stroke);
            }
        } else if (rarity == RunLoot.ROSE) {
            // Petals turning about it.
            for (int petal = 0; petal < 5; petal++) {
                double turn = clock * 1.8 + petal * Math.PI * 2 / 5;
                paint.setColor(RunLoot.rose(clock + petal * .4f));
                paint.setAlpha(220);
                canvas.drawOval(x + (float) Math.cos(turn) * tall * .22f - tall * .03f, y + (float) Math.sin(turn) * tall * .14f - tall * .018f,
                        x + (float) Math.cos(turn) * tall * .22f + tall * .03f, y + (float) Math.sin(turn) * tall * .14f + tall * .018f, paint);
            }
        }
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
    }

    /** Halgakos with nothing in her hands, her feet at a place, as tall as asked when she stands. */
    private void drawHer(Canvas canvas, float feetX, float feetY, float tall, int pose, boolean leftward) {
        // In the colour of her form, where there is a land and she has one.
        if (pose >= 0) {
            frame(canvas, herJump(), pose, JUMP_FRAMES, feetX, feetY, tall, leftward);
            return;
        }
        // (In a colour, the sheet is only the three rows of it that a land uses, which are its first three.)
        Bitmap pet = herPet();
        int petWide = this.pet.getWidth() / PetArt.COLUMNS, petTall = this.pet.getHeight() / PetArt.FRAMES.length;
        float scale = tall / petTall;
        int row = pose == RUN ? (leftward ? PetArt.RUN_LEFT : PetArt.RUN_RIGHT) : PetArt.IDLE;
        int column = (int) (clock * 1000 / PetArt.FRAME_MS[row]) % PetArt.FRAMES[row];
        source.set(column * petWide, row * petTall, column * petWide + petWide, row * petTall + petTall);
        target.set(feetX - petWide * scale / 2, feetY - tall, feetX + petWide * scale / 2, feetY);
        canvas.drawBitmap(pet, source, target, paint);
    }

    /**
     * One frame of a row of frames drawn apart from her own, hers or one of the others', at
     * the size she would be standing so tall, and turned about if asked. Hers were drawn
     * facing right and the others' facing left.
     */
    /**
     * The beam of the white that is truly hers: wide and bright, from her along the way
     * she faced when it began, as far as it reaches; nothing stops it, and where it goes
     * through a ledge, a wall or the ground there is a glow on what it goes through.
     * Letters and feathers stream along it.
     */
    private void drawBeam(Canvas canvas) {
        float way = run.beamLeft() ? -1 : 1, fromX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile + way * tile * .4f, y = (run.y() + PlusRun.TALL * .5f - cameraY) * tile;
        float toX = fromX + way * PlusRun.BEAM_REACH * tile, life = run.beam() / PlusRun.BEAM_LASTS;
        float swell = (float) Math.sin(Math.min(1, (1 - life) * 6) * Math.PI / 2) * Math.min(1, life * 5), half = tile * PlusRun.BEAM_WIDE * swell;
        paint.setStyle(Paint.Style.FILL);
        for (int layer = 0; layer < 3; layer++) {
            paint.setColor(layer == 0 ? 0xFFFFE9A8 : layer == 1 ? 0xFFFFF7DE : Color.WHITE);
            paint.setAlpha(layer == 0 ? 110 : layer == 1 ? 190 : 255);
            float thick = half * (1.25f - layer * .42f) * (1 + .08f * (float) Math.sin(clock * 40 + layer));
            canvas.drawRoundRect(Math.min(fromX, toX), y - thick, Math.max(fromX, toX), y + thick, thick, thick, paint);
        }
        // Where it goes through something solid, that glows.
        int row = (int) Math.floor(run.y() + PlusRun.TALL * .5f), from = (int) Math.floor(run.x() + PlusRun.WIDE / 2);
        RunLevels.Level here = run.level();
        for (int step = 1; step <= PlusRun.BEAM_REACH; step++) {
            int column = from + (int) way * step;
            if (column < 0 || column >= here.columns || row < 0 || row >= here.land.length || !PlusRun.solid(here.land[row][column])) continue;
            float at = (column + .5f - cameraX) * tile;
            paint.setColor(0xFFFFD24A);
            paint.setAlpha(Math.round(150 * swell));
            canvas.drawCircle(at, y, tile * (.62f + .08f * (float) Math.sin(clock * 30 + column)), paint);
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(200 * swell));
            canvas.drawCircle(at, y, tile * .3f, paint);
        }
        // Letters and feathers along it.
        String[] letters = {"A", "가", "あ", "文", "Z", "한", "ん", "字"};
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(tile * .42f);
        for (int index = 0; index < 10; index++) {
            float along = (clock * 2.6f + index * .1f) % 1, at = fromX + way * along * PlusRun.BEAM_REACH * tile;
            float high = y + (float) Math.sin(along * 20 + index) * half * .55f;
            if (index % 3 == 2) {
                paint.setAlpha(Math.round(230 * swell));
                pieceIn(canvas, two, FEATHER_ART, at, high, tile * .45f, way > 0 ? 180 : 0);
            } else {
                text.setColor(0xFFB9862C);
                text.setAlpha(Math.round(230 * swell));
                canvas.drawText(letters[index % letters.length], at, high + tile * .14f, text);
            }
        }
        text.setAlpha(255);
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
    }

    /**
     * Her pet, where it is: one that runs, standing, running, in the air or at something;
     * one that floats, hovering or at something; and either of them pleased, for a moment
     * after it joins her and after it has dealt with something.
     */
    private void drawPet(Canvas canvas) {
        for (PlusRun.Pet it : run.herd()) drawPet(canvas, it);
    }

    /** The row of each pet that runs in the sheet of climbing pets (-1 for one that flies, which never climbs). */
    private static final int[] PET_CLIMB_ROW = {-1, 0, 1, -1, 2, 3, 4, -1, -1, 5, 6, 7, -1, 8};
    static final String PET_CLIMB_SHEET = "runart/pet_climb.webp";
    private Bitmap petClimb;
    private boolean petClimbTried;

    /** A pet that runs, climbing: one of its two pictures. Whether it was drawn (it is not, without the sheet). */
    private boolean drawPetClimb(Canvas canvas, int kind, int step, float x, float y, float tall) {
        int row = kind >= 0 && kind < PET_CLIMB_ROW.length ? PET_CLIMB_ROW[kind] : -1;
        if (row < 0) return false;
        if (petClimb == null && !petClimbTried) {
            petClimbTried = true;
            try (java.io.InputStream in = context.getAssets().open(PET_CLIMB_SHEET)) {
                petClimb = BitmapFactory.decodeStream(in);
            } catch (java.io.IOException ignored) {
                petClimb = null;
            }
        }
        if (petClimb == null) return false;
        int cell = petClimb.getWidth() / 2;
        source.set(step * cell, row * cell, (step + 1) * cell, (row + 1) * cell);
        target.set(x - tall / 2, y - tall + tile * .04f, x + tall / 2, y + tile * .04f);
        // (The second step drawn the other way about too, so that the hand over hand reads even where its two pictures are alike.)
        canvas.save();
        if (step == 1) canvas.scale(-1, 1, x, y);
        canvas.drawBitmap(petClimb, source, target, paint);
        canvas.restore();
        return true;
    }

    private void drawPet(Canvas canvas, PlusRun.Pet it) {
        int kind = it.kind;
        float x = (it.x - cameraX) * tile, y = (it.y - cameraY) * tile, across = it.x - it.seenX, down = it.y - it.seenY;
        it.seenX = it.x;
        it.seenY = it.y;
        boolean busy = it.busy > 0, floats = PlusRun.floats(kind);
        int pose = busy ? 4 : petGlad < .7f ? 5 : floats ? (Math.abs(across) > .05f ? 3 : (int) (clock * 5) % 2)
                : Math.abs(down) > .03f ? 3 : Math.abs(across) > .01f ? 1 + (int) (clock * 9) % 2 : 0;
        float tall = tile * .95f * 170 / 100, hop = petGlad < .7f ? Math.abs((float) Math.sin(petGlad / .7f * Math.PI * 2)) * tile * .25f : 0;
        // Climbing after her: its own two pictures of climbing, hand over hand by turns as it goes, from the run's asset pack.
        if (it.climbing && drawPetClimb(canvas, kind, (int) (it.climbed * 2.6f) % 2, x, y, tall)) return;
        // A soft shadow under one that floats, so that it is seen to be in the air.
        cell(canvas, petArt(kind), pose, 6, x, y + tile * .04f - hop, tall, !it.left);
        if (petWordLeft > 0 && petWordOf == kind) {
            float show = Math.min(1, Math.min(petWordLeft, 1.8f - petWordLeft) / .2f), size = tile * .26f, over = y - tile * 1.25f - hop;
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(size);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            float wide = text.measureText(petWord) + size * 1.1f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(235 * show));
            box.set(x - wide / 2, over - size * .9f, x + wide / 2, over + size * .55f);
            canvas.drawRoundRect(box, size * .6f, size * .6f, paint);
            text.setColor(0xFF2B2440);
            text.setAlpha(Math.round(255 * show));
            canvas.drawText(petWord, x, over + size * .2f, text);
            text.setAlpha(255);
            text.setTypeface(Typeface.DEFAULT);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
    }

    /** The mark under a thin ledge that says she can be let down through it: a small arrowhead, pointing down. */
    private void lets(Canvas canvas, float x, float y, int row) {
        boolean hers = run.onGround() && (int) Math.floor(run.y() + PlusRun.TALL + .05f) == row;
        float size = tile * .13f, bob = hers ? Math.abs((float) Math.sin(clock * 5)) * tile * .06f : 0;
        stroke.setStrokeWidth(tile * .05f);
        stroke.setColor(0xFF20242A);
        stroke.setAlpha(hers || run.dark() ? 200 : 90);
        path.reset();
        path.moveTo(x - size, y + bob);
        path.lineTo(x, y + size + bob);
        path.lineTo(x + size, y + bob);
        canvas.drawPath(path, stroke);
        stroke.setStrokeWidth(tile * .028f);
        stroke.setColor(Color.WHITE);
        stroke.setAlpha(hers || run.dark() ? 255 : 150);
        canvas.drawPath(path, stroke);
        stroke.setAlpha(255);
    }

    /**
     * What has just been won flies to its place at the top: pluses to the count of them, a
     * piece of a heart to her hearts. Each is a small picture on an arc.
     */
    private final List<float[]> flights = new ArrayList<>();

    private void flyTo(float fromX, float fromY, boolean heart, int many) {
        float toX = safe.left + dp(heart ? 44 : 150), toY = safe.top + dp(32);
        for (int one = 0; one < many; one++) flights.add(new float[]{fromX + (random.nextFloat() - .5f) * tile, fromY + (random.nextFloat() - .5f) * tile * .6f, toX, toY, -one * .06f, heart ? 1 : 0});
    }

    private void drawFlights(Canvas canvas, float seconds) {
        for (int index = flights.size() - 1; index >= 0; index--) {
            float[] one = flights.get(index);
            one[4] += seconds;
            if (one[4] < 0) continue;
            float through = one[4] / .7f;
            if (through >= 1) {
                flights.remove(index);
                continue;
            }
            float eased = through * through * (3 - 2 * through);
            float x = one[0] + (one[2] - one[0]) * eased, y = one[1] + (one[3] - one[1]) * eased - (float) Math.sin(eased * Math.PI) * dp(60);
            if (one[5] > 0) artIn(canvas, HEART_ART, x, y, dp(22));
            else artIn(canvas, PLUS_ART, x, y, dp(16));
        }
    }

    /**
     * A chest is picked up: it blinks white, goes up over her head and is gone, with a
     * few rays where it stood, and what was in it goes up to the count at the top.
     */
    private float chestAge = 9, chestX, chestY;

    private void drawChest(Canvas canvas) {
        // Half a second: it blinks white twice, as a thing picked up does, and goes up over her head, smaller and fainter as it goes,
        // with a few thin rays where it stood. Nothing of it is across her for more than a blink.
        if (chestAge > .5f) return;
        float through = chestAge / .5f, x = (chestX - cameraX) * tile, y = (chestY - cameraY) * tile - tile * 1.5f * through * (2 - through) / 1f;
        stroke.setColor(0xFFFFE08A);
        stroke.setStrokeWidth(tile * .05f);
        stroke.setAlpha(Math.round(200 * (1 - through)));
        for (int ray = 0; ray < 8; ray++) {
            double turn = ray * Math.PI / 4 + .3;
            float from = tile * (.35f + .5f * through), to = tile * (.6f + .9f * through), baseY = (chestY - cameraY) * tile;
            canvas.drawLine(x + (float) Math.cos(turn) * from, baseY + (float) Math.sin(turn) * from, x + (float) Math.cos(turn) * to, baseY + (float) Math.sin(turn) * to, stroke);
        }
        stroke.setAlpha(255);
        boolean white = chestAge < .06f || chestAge > .12f && chestAge < .18f;
        if (white) paint.setColorFilter(WHITENED);
        paint.setColor(Color.BLACK);
        paint.setAlpha(Math.round(255 * (through < .35f ? 1 : Math.max(0, 1 - (through - .35f) / .65f))));
        pieceIn(canvas, four, CHEST_PICTURE, x, y, tile * 1.1f * (1 - .55f * through), 0);
        paint.setColorFilter(null);
        paint.setAlpha(255);
    }

    private static final android.graphics.ColorFilter WHITENED = new android.graphics.PorterDuffColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_ATOP);

    private static final String[] IMP_SAYS = {"Too slow~", "Hehe~", "Mine~", "Sloppy~"};
    private float wardAge = 9;

    private int petWordOf = -1, offerSeen = -1, seenPetSwaps;

    private void petSays(String word, int kind) {
        petWordOf = kind;
        petWord = word;
        petWordLeft = 1.8f;
        sincePetWord = 0;
    }

    /** What lies about a pet and is its doing: the puddles of ink a mini Rune has left, and the ring of light when it is ready or has just been used. */
    private void drawPetThings(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (float[] puddle : run.puddles()) {
            float x = (puddle[0] - cameraX) * tile, y = (puddle[1] - cameraY) * tile, show = Math.min(1, puddle[2] / .8f);
            paint.setColor(0xFF27338F);
            paint.setAlpha(Math.round(200 * show));
            canvas.drawOval(x - tile * .8f * show, y - tile * .07f, x + tile * .8f * show, y + tile * .09f, paint);
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(90 * show));
            canvas.drawOval(x - tile * .45f * show, y - tile * .04f, x - tile * .1f * show, y + tile * .01f, paint);
        }
        for (float[] smear : run.smears()) {
            float x = (smear[0] - cameraX) * tile, y = (smear[1] - cameraY) * tile, show = Math.min(1, smear[2] / .8f);
            paint.setColor(0xFF2E2A33);
            paint.setAlpha(Math.round(190 * show));
            canvas.drawOval(x - tile * .75f, y - tile * .08f, x + tile * .75f, y + tile * .08f, paint);
        }
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() + PlusRun.TALL / 2 - cameraY) * tile;
        if (run.warded() || wardAge < .4f) {
            float burst = wardAge < .4f ? wardAge / .4f : 0, round = tile * (.95f + .05f * (float) Math.sin(clock * 5) + burst * .8f);
            stroke.setColor(0xFFFFF1B8);
            stroke.setStrokeWidth(tile * (.07f + .05f * burst));
            stroke.setAlpha(Math.round((wardAge < .4f ? 255 * (1 - burst) : 120 + 60 * (float) Math.sin(clock * 5))));
            canvas.drawCircle(herX, herY, round, stroke);
            stroke.setAlpha(255);
        }
    }

    /** A ring of light that beats about a button: here is the thing that was just spoken of. */
    private void glow(Canvas canvas, RectF button) {
        // The button's own rim turns yellow, beating gently: inside its circle, so it never reaches the button beside it.
        float beat = .5f + .5f * (float) Math.sin(clock * 5), round = Math.min(button.width(), button.height()) / 2, width = dp(3.5f) + dp(1) * beat;
        stroke.setColor(0xFFFFC928);
        stroke.setAlpha(Math.round(170 + 85 * beat));
        stroke.setStrokeWidth(width);
        canvas.drawCircle(button.centerX(), button.centerY(), round - width / 2 - dp(.5f), stroke);
        stroke.setAlpha(255);
    }

    /** A button that is no use for now: dark, with a red ring and a bar across it. */
    private void barred(Canvas canvas, RectF button) {
        float x = button.centerX(), y = button.centerY(), round = Math.min(button.width(), button.height()) / 2;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF15151C);
        paint.setAlpha(200);
        canvas.drawCircle(x, y, round, paint);
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
        stroke.setColor(0xFFE0362F);
        stroke.setStrokeWidth(dp(3));
        canvas.drawCircle(x, y, round - dp(2), stroke);
        canvas.drawLine(x - round * .6f, y + round * .6f, x + round * .6f, y - round * .6f, stroke);
    }

    /** One picture of a row of them, its foot at a point, the whole of its cell this tall. */
    private void cell(Canvas canvas, Bitmap sheet, int index, int count, float feetX, float feetY, float tall, boolean turned) {
        int wide = sheet.getWidth() / count;
        float scale = tall / sheet.getHeight();
        feetY += grounded(sheet, tall);
        source.set(index * wide, 0, index * wide + wide, sheet.getHeight());
        target.set(feetX - wide * scale / 2, feetY - tall, feetX + wide * scale / 2, feetY);
        canvas.save();
        if (turned) canvas.scale(-1, 1, feetX, feetY);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
    }

    /** A mark over something that is about to do what it does. */
    private void warn(Canvas canvas, float x, float y, int colour) {
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(tile * (.7f + .12f * (float) Math.sin(clock * 20)));
        text.setStyle(Paint.Style.STROKE);
        text.setStrokeWidth(tile * .12f);
        text.setColor(Color.WHITE);
        canvas.drawText("!", x, y, text);
        text.setStyle(Paint.Style.FILL);
        text.setColor(colour);
        canvas.drawText("!", x, y, text);
    }

    /**
     * One of those that fly: its wings going; the picture of it about to do what it does
     * while it is; a mark over the swallow that has marked her, with a line to where, and
     * over the asterisk taking aim; a glow round the wisp and the flake, which are not to
     * be dealt with as the rest are. Beaten, it turns over as it falls, and fades; the wisp
     * goes up in smoke and the flake runs down as water.
     */
    private void drawFlier(Canvas canvas, PlusRun.Foe foe, float middle, float feet, boolean second, boolean turned) {
        int kind = foe.kind, which = kind - RunLevels.DRIFTER, colour = FLIER_COLOURS[which];
        if (foe.flat && foe.act > 1.2f) return;
        boolean elemental = kind == RunLevels.WISP || kind == RunLevels.RIME;
        boolean telling = foe.mode == 1 && (kind == RunLevels.SWOOPER || kind == RunLevels.BOMBER || kind == RunLevels.SHOOTER || kind == RunLevels.WISP);
        int pose = foe.flat ? 3 : telling || kind == RunLevels.SWOOPER && foe.mode == 2 ? 2 : second ? 1 : 0;
        float tall = foe.tall() * tile * 1.5f * 160 / 96, x = middle, y = feet + foe.tall() * tile * .3f, heart = feet - foe.tall() * tile * .5f;
        if (!foe.flat && elemental) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colour);
            paint.setAlpha(Math.round(50 + 30 * (float) Math.sin(clock * 5 + foe.x)));
            canvas.drawCircle(x, heart, foe.tall() * tile * .95f, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
        if (!foe.flat && kind == RunLevels.SWOOPER && foe.mode == 1) {
            // The mark: a line from it to her, which is where it will go.
            float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() + PlusRun.TALL * .5f - cameraY) * tile;
            stroke.setColor(colour);
            stroke.setAlpha(Math.round(110 + 90 * Math.abs((float) Math.sin(clock * 16))));
            stroke.setStrokeWidth(tile * .06f);
            canvas.drawLine(x, heart, herX, herY, stroke);
            canvas.drawCircle(herX, herY, tile * .45f, stroke);
            stroke.setAlpha(255);
        }
        canvas.save();
        if (foe.flat) {
            paint.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (1.2f - foe.act) / .4f))));
            if (kind == RunLevels.WISP) {
                y -= tile * 1.2f * foe.act;
            } else {
                y += Math.min(tile * 8, tile * (kind == RunLevels.RIME ? 5 : 11) * foe.act * foe.act);
                if (!elemental) canvas.rotate(foe.act * 320 * (foe.leftward ? -1 : 1), x, y - tall * .3f);
            }
        } else if (foe.dazed > 0 && (int) (clock * 16) % 2 == 0) {
            paint.setAlpha(120);
        }
        cell(canvas, flier(which), pose, 4, x, y, tall, turned);
        canvas.restore();
        paint.setAlpha(255);
        if (!foe.flat && foe.mode == 1 && (kind == RunLevels.SWOOPER || kind == RunLevels.SHOOTER)) {
            boolean black = kind == RunLevels.SHOOTER && foe.step == 1;
            if (black) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFF15151C);
                paint.setAlpha(Math.round(120 + 100 * Math.abs((float) Math.sin(clock * 12))));
                canvas.drawCircle(x, heart, foe.tall() * tile * (.9f + .25f * (1 - foe.act / .9f)), paint);
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
            }
            warn(canvas, x, feet - foe.tall() * tile * 1.5f, black ? 0xFF15151C : colour);
        }
    }

    /**
     * One met in the middle of a land shows itself: its name across the screen, in its
     * colour, and under it that it is no gatekeeper; the wyrm come back, that it is back.
     * And while the wyrm is still out of sight ahead, a mark at the edge it is coming from.
     */
    private void drawRover(Canvas canvas) {
        if (roverKind < 0) return;
        int colour = waitColour(roverKind);
        for (PlusRun.Foe foe : run.foes()) {
            if (foe.kind != RunLevels.WYRM || foe.away || foe.flat) continue;
            float at = (foe.x + foe.wide() / 2 - cameraX) * tile;
            if (at > width + tile * .5f) warn(canvas, safe.right - dp(18), Math.max(safe.top + dp(120), (foe.y + foe.tall() - cameraY) * tile), colour);
        }
        if (roverAge > 2.6f) return;
        float solid = Math.min(1, Math.min(roverAge / .2f, (2.6f - roverAge) / .5f)), grow = 1.25f - .25f * Math.min(1, roverAge * 5), y = safe.top + safe.height() * .3f;
        boolean again = roverKind == RunLevels.WYRM && seenRoused > 1;
        word(canvas, again ? "It is back!" : waitName(roverKind).toUpperCase(java.util.Locale.ROOT), safe.centerX(), y, Math.min(dp(26), safe.width() / 12f) * grow, colour, solid);
        if (!again) word(canvas, "No gatekeeper: beat it for a prize, or run on", safe.centerX(), y + dp(26), Math.min(dp(13.5f), safe.width() / 26f), 0xFF3A3F45, solid);
    }

    private void frame(Canvas canvas, Bitmap sheet, int index, int count, float feetX, float feetY, float tall, boolean turned) {
        float scale = tall / (pet.getHeight() / (float) PetArt.FRAMES.length);
        int wide = sheet.getWidth() / count;
        feetY += grounded(sheet, sheet.getHeight() * scale);
        source.set(index * wide, 0, index * wide + wide, sheet.getHeight());
        target.set(feetX - wide * scale / 2, feetY - sheet.getHeight() * scale, feetX + wide * scale / 2, feetY);
        canvas.save();
        if (turned) canvas.scale(-1, 1, feetX, feetY);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
    }

    /** A white card, as the companion's own popups are, with a soft edge to lift it off the screen. */
    void plate(Canvas canvas, RectF where, float radius, int alpha) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33000000);
        canvas.drawRoundRect(where.left, where.top + dp(2), where.right, where.bottom + dp(3), radius, radius, paint);
        paint.setColor(cardColor);
        paint.setAlpha(alpha);
        canvas.drawRoundRect(where, radius, radius, paint);
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
    }

    private void drawHud(Canvas canvas) {
        drawGearMarks(canvas);
        if (run.wardHurts() != seenWardHurts) {
            seenWardHurts = run.wardHurts();
            wardFlash = 0;
            audio.play("tap", .8f);
        }
        // Her hearts, her lives and her pluses, in a plate at the top. The hearts are hearts, and empty by halves; the lives are her own
        // face and a count beside it, so that the two are never taken for each other.
        int mostHp = run.maxHp(), hp = run.hp(), allHearts = Math.max(1, (mostHp + RunSkills.HEART - 1) / RunSkills.HEART), hearts = Math.min(RunSkills.ROW, allHearts),
                halves = run.halves(), wards = run.wardMost();
        float tall = dp(wards > 0 ? 58 : 44), margin = dp(10), heart = hearts > 5 ? dp(15) : hearts > 3 ? dp(18) : dp(22);
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        String count = "+ " + run.collected() + " of " + run.total(), lives = "× " + run.lives();
        float livesWide = dp(26) + text.measureText(lives) + dp(10);
        box.set(safe.left + margin, safe.top + margin, safe.left + margin + dp(20) + heart * hearts + dp(8) + livesWide + text.measureText(count) + dp(16),
                safe.top + margin + tall);
        float plateLeft = box.left, plateMiddle = box.centerY(), plateTop = box.top;
        plate(canvas, box, dp(22), 235);
        // Her shield bar, over her hearts: a shield for each piece, filling as it comes back; a flash as one takes a hurt.
        if (wards > 0) {
            plateMiddle += dp(7);
            float ward = run.ward();
            for (int index = 0; index < wards; index++) drawWard(canvas, plateLeft + dp(16) + dp(15) * index, plateTop + dp(10), dp(12), Math.max(0, Math.min(1, ward - index)));
            if (wardFlash < .5f) glowAt(canvas, plateLeft + dp(16) + dp(15) * Math.min(wards - 1, (int) ward), plateTop + dp(10), dp(16), 0xFF6FB8FF, Math.round(220 * (1 - wardFlash / .5f)));
            paint.setAlpha(255);
        }
        // Life coming back: a glow over her hearts.
        if (run.regenAge() < .8f) {
            float through = run.regenAge() / .8f;
            glowAt(canvas, plateLeft + dp(14) + heart * Math.min(hearts - .5f, halves / 2f), plateMiddle, heart * (1 + through), 0xFFFF7A9A, Math.round(200 * (1 - through)));
            paint.setAlpha(255);
        }
        for (int index = 0; index < hearts; index++) {
            float middle = plateLeft + dp(14) + heart * (index + .5f), bottom = plateMiddle + heart * .42f;
            // The layer showing at this place, and how full its heart is; under it, the layer below, full, if there is one.
            float[] shown = RunSkills.heart(index, hp, mostHp);
            int layer = (int) shown[0];
            float fill = shown[1];
            // Down to the last of them, they beat.
            float size = heart * .92f * (hp <= RunSkills.HEART && fill > 0 ? 1 + .1f * (float) Math.sin(clock * 9) : 1);
            if (layer > 0) {
                paint.setAlpha(255);
                paint.setColorFilter(heartFilter(layer - 1));
                art(canvas, HEART_ART, middle, bottom, size, false);
            } else if (fill < 1) {
                paint.setColorFilter(null);
                paint.setAlpha(60);
                art(canvas, HEART_ART, middle, bottom, size, false);
            }
            if (fill > 0) {
                paint.setAlpha(255);
                paint.setColorFilter(heartFilter(layer));
                canvas.save();
                if (fill < 1) canvas.clipRect(middle - heart, bottom - heart * 1.2f, middle - heart * .46f + heart * .92f * fill, bottom + heart * .2f);
                art(canvas, HEART_ART, middle, bottom, size, false);
                canvas.restore();
            }
            paint.setColorFilter(null);
        }
        // Past three hearts, her HP, small, under them.
        if (allHearts > 3) {
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(soft);
            fitted(canvas, hp + " / " + mostHp + " HP", plateLeft + dp(14), plateMiddle + heart * .42f + dp(10), dp(9.5f), heart * hearts);
        }
        paint.setAlpha(255);
        // What she wears, under her hearts: a small picture of each piece, in the colour of its set.
        int wearing = gearOn();
        if (wearing != 0) {
            float at = plateLeft + dp(18);
            for (int slot = 0; slot < RunGear.SLOTS; slot++) {
                int tier = RunGear.worn(wearing, slot);
                if (tier == 0) continue;
                gearIcon(canvas, slot, at, plateMiddle + dp(15.5f), dp(8.5f), SET_COLOURS[tier - 1]);
                at += dp(13);
            }
        }
        float livesAt = plateLeft + dp(20) + heart * hearts + dp(8);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? 0xFF4A4F54 : 0xFFD9DCDD);
        canvas.drawRect(livesAt - dp(5), plateMiddle - dp(11), livesAt - dp(4), plateMiddle + dp(11), paint);
        paint.setColor(Color.BLACK);
        round(canvas, 0, livesAt + dp(11), plateMiddle, dp(11));
        stroke.setColor(run.lives() <= 1 ? 0xFFE0362F : CalculatorLayout.CORAL);
        stroke.setStrokeWidth(dp(1.5f));
        canvas.drawCircle(livesAt + dp(11), plateMiddle, dp(11), stroke);
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(run.lives() <= 1 ? 0xFFE0362F : ink);
        canvas.drawText(lives, livesAt + dp(26), plateMiddle + dp(5.5f), text);
        text.setColor(ink);
        fitted(canvas, count, livesAt + livesWide, plateMiddle + dp(5.5f), dp(15), dp(400));
        plate(canvas, pauseButton, tall / 2, 235);
        paint.setColor(ink);
        canvas.drawRoundRect(pauseButton.centerX() - dp(7), pauseButton.centerY() - dp(8), pauseButton.centerX() - dp(2.5f), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        canvas.drawRoundRect(pauseButton.centerX() + dp(2.5f), pauseButton.centerY() - dp(8), pauseButton.centerX() + dp(7), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        paint.setColor(Color.BLACK);
        float under = safe.top + margin + tall + dp(8), round = dp(36), x = safe.left + margin;
        drawLevel(canvas, x, under);
        boolean anySlot = run.active(0) != null || run.active(1) != null || run.active(2) != null;
        if (RunArms.heavy(run.weapon()) || anySlot || run.energy() < PlusRun.ENERGY_MOST - .5f) drawEnergy(canvas, x + dp(128), under);
        boolean divine = run.dpMaking() > 0 || run.dp() > 0 || run.divineLeft() > 0;
        if (divine) drawDivine(canvas, x + dp(232), under);
        drawBlackChip(canvas, x + dp(divine ? 336 : 232), under);
        if (anySlot) drawSlots(canvas);
        drawBreath(canvas, x, under + dp(26));
        drawDelveLabel(canvas, safe.centerX(), leftButton.top - dp(30));
        under += dp(30);
        if (run.pet() >= 0) {
            float face = dp(run.herd().size() > 2 ? 30 : 42), left = pauseButton.right - face, top = pauseButton.bottom + dp(8);
            for (PlusRun.Pet it : run.herd()) {
                box.set(left, top, left + face, top + face);
                plate(canvas, box, face / 2, 235);
                cell(canvas, petArt(it.kind), petGlad < .7f ? 5 : 0, 6, left + face / 2, top + face * .86f, face * 1.05f, false);
                if (it.stays) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(CalculatorLayout.CORAL);
                    canvas.drawCircle(left + dp(4), top + dp(4), dp(3.5f), paint);
                    paint.setColor(Color.BLACK);
                }
                top += face + dp(4);
            }
        }
        // Shut in with someone: her name, and a bar of how much more she can take, marked at its thirds.
        boolean den = false;
        for (PlusRun.Foe foe : run.foes()) den |= foe.spawned && !foe.flat && RunLevels.boss(foe.kind) && run.room() >= 0;
        // (And one met in the middle of a land has its name and its bar for as long as it is there.)
        PlusRun.Foe roving = null;
        for (PlusRun.Foe foe : run.foes()) if (RunLevels.rover(foe.kind) && !foe.away && run.room() < 0 && (!foe.flat || foe.act < 1.5f)) roving = foe;
        if (run.locked() || run.opening() > 0 || den || roving != null) {
            PlusRun.Foe boss = null;
            int most = 0, left = 0;
            for (PlusRun.Foe foe : run.foes()) {
                if (!RunLevels.boss(foe.kind) || RunLevels.rover(foe.kind) || foe.spawned != den || roving != null) continue;
                if (boss == null || boss.flat) boss = foe;
                most += foe.most;
                left += Math.max(0, foe.left);
            }
            if (roving != null) {
                boss = roving;
                most = roving.most;
                left = Math.max(0, roving.left);
            }
            if (boss != null) {
                int colour = waitColour(boss.kind), face = waitFace(boss.kind);
                float wide = Math.min(safe.width() - margin * 2, dp(300)), high = dp(48), from = safe.centerX() - wide / 2;
                box.set(from, under, from + wide, under + high);
                plate(canvas, box, dp(20), 235);
                float bar = from + dp(16) + (face >= 0 ? dp(30) : 0), end = from + wide - dp(16);
                if (face >= 0) round(canvas, face, from + dp(22), under + high / 2, dp(15));
                text.setTextSize(dp(12.5f));
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(dark ? 0xFFF3F3F3 : colour);
                fitted(canvas, waitName(boss.kind), bar, under + dp(15), dp(12.5f), end - bar - dp(88));
                // How she stands toward a blow, in a word and a mark at the end of her name's line: a shield while blows are turned aside,
                // which flashes when one is; and, while one would land, the word for it in gold, blinking when that is about to be over.
                int guard = run.guard(boss);
                boolean bare = guard == PlusRun.BARE, kept = !boss.flat && (guard == PlusRun.SHIELDED || guard == PlusRun.AWAY);
                boolean closing = bare && run.bareLeft(boss) < .5f && (int) (clock * 12) % 2 == 0;
                if (kept) {
                    int tint = sinceStruck < .25f ? Color.WHITE : colour;
                    shieldMark(canvas, end - dp(7), under + dp(11), dp(6.5f), tint);
                    text.setTextAlign(Paint.Align.RIGHT);
                    text.setTextSize(dp(11));
                    text.setColor(soft);
                    canvas.drawText(run.forming(boss) ? "Forming" : "Break the shield", end - dp(17), under + dp(15), text);
                } else if (bare && !closing) {
                    text.setTextAlign(Paint.Align.RIGHT);
                    text.setTextSize(dp(12));
                    text.setColor(0xFFE0A21A);
                    canvas.drawText("Open: strike!", end, under + dp(15), text);
                }
                text.setTextAlign(Paint.Align.LEFT);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
                canvas.drawRoundRect(bar, under + dp(21), end, under + dp(31), dp(5), dp(5), paint);
                paint.setColor(colour);
                // Behind her shield what she has left is pale; open, it is its full colour, with gold round it.
                paint.setAlpha(kept ? 120 : 255);
                if (left > 0) canvas.drawRoundRect(bar, under + dp(21), bar + (end - bar) * left / Math.max(1, most), under + dp(31), dp(5), dp(5), paint);
                paint.setAlpha(255);
                if (bare && !closing) {
                    stroke.setColor(0xFFE0A21A);
                    stroke.setStrokeWidth(dp(2));
                    canvas.drawRoundRect(bar - dp(2), under + dp(19), end + dp(2), under + dp(33), dp(7), dp(7), stroke);
                } else if (kept) {
                    stroke.setColor(sinceStruck < .25f ? Color.WHITE : dark ? 0xFF9AA0A6 : 0xFF8C9296);
                    stroke.setStrokeWidth(dp(1.2f));
                    for (float tick = bar + dp(6); tick < end - dp(2); tick += dp(9)) canvas.drawLine(tick, under + dp(30), tick + dp(5), under + dp(22), stroke);
                }
                paint.setColor(cardColor);
                for (int third = 1; third < 3; third++) canvas.drawRect(bar + (end - bar) * third / 3 - dp(1), under + dp(21), bar + (end - bar) * third / 3 + dp(1), under + dp(31), paint);
                // Her shield, under what she has left: how much of it there is still to break. It falls with each blow that lands on it, is
                // empty while it is broken, and fills as it forms again. Beside it, what it gives way to soonest.
                float meter = boss.flat ? 0 : run.shield(boss);
                int weak = PlusRun.weak(boss.kind);
                float last = weak != 0 ? end - dp(16) : end;
                paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
                canvas.drawRoundRect(bar, under + dp(36), last, under + dp(41), dp(2.5f), dp(2.5f), paint);
                paint.setColor(run.forming(boss) ? 0xFFB9BEC2 : sinceStruck < .2f ? Color.WHITE : 0xFF5B9BF0);
                if (meter > 0) canvas.drawRoundRect(bar, under + dp(36), bar + (last - bar) * meter, under + dp(41), dp(2.5f), dp(2.5f), paint);
                paint.setColor(Color.BLACK);
                if (weak != 0) pieceIn(canvas, two, weak == PlusRun.FIRE ? FIRE_ART : DROP_ART, end - dp(5), under + dp(38), dp(14), 0);
                paint.setColor(Color.BLACK);
                under += high + dp(8);
            }
        }
        // (Over a card open over the land, it is drawn over that card instead, where it can be seen.)
        if (!overlaid()) under = drawToastCard(canvas, under);
        // What is asked of her in this land, and how it stands: a scroll, and a few words.
        int asked = run.level().quest, stands = run.quest();
        if (asked != 0 && stands != 0 && (stands == PlusRun.ASKED || stands == PlusRun.DONE || sinceAsked < 4)) {
            String words = stands == PlusRun.DONE ? "Done: back to " + giverName(asked) : stands == PlusRun.PAID ? "Handed in" : stands == PlusRun.FAILED ? "Not this time"
                    : asked == RunLevels.CAKES ? "Rice cakes  " + run.cakes() + " / 3" : asked == RunLevels.CELLAR ? "Clear the cellar"
                    : asked == RunLevels.LETTER ? "Letter for Yeseo, unhurt" : asked == RunLevels.TIME ? "Next flag in  " + (int) Math.ceil(run.questLeft()) + " s"
                    : "Find the hidden room";
            // What doing it pays, so that there is something to go after.
            if (stands == PlusRun.ASKED) words += session.tally("run_quest_" + number) == 0 ? "   · " + pays(number) : "   · + 15";
            int picture = asked == RunLevels.CAKES ? CAKE_PICTURE : asked == RunLevels.CELLAR ? CELLAR_PICTURE : asked == RunLevels.LETTER ? LETTER_PICTURE
                    : asked == RunLevels.TIME ? WATCH_PICTURE : CRACK_PICTURE;
            text.setTextSize(dp(13.5f));
            float high = dp(34), wide = Math.min(text.measureText(words) + dp(50), safe.width() - margin * 2);
            box.set(safe.left + margin, under, safe.left + margin + wide, under + high);
            plate(canvas, box, high / 2, 235);
            paint.setAlpha(255);
            pieceIn(canvas, four, stands == PlusRun.ASKED ? picture : SCROLL_PICTURE, safe.left + margin + dp(19), under + high / 2, dp(24), 0);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(stands == PlusRun.DONE || stands == PlusRun.PAID ? 0xFF2E9E6E : stands == PlusRun.FAILED ? soft : ink);
            fitted(canvas, words, safe.left + margin + dp(36), under + dp(22), dp(13.5f), wide - dp(50));
            under += high + dp(8);
        }
        // The hunts she has taken on that something can be done towards in this land: a small plate for each, with how far it has got.
        for (int slot = 0; slot < RunHunts.SLOTS; slot++) {
            if (!huntHere[slot]) continue;
            int hunt = hunt(slot), at = huntAt(slot), many = RunHunts.many(hunt);
            boolean finished = at > many;
            if (hunt == 0 || at <= 0 || finished && huntFlash[slot] <= 0) continue;
            String words = RunHunts.brief(hunt) + "  " + Math.min(many, at - 1) + " / " + many + (finished ? "  ·  done" : "");
            text.setTextSize(dp(12.5f));
            float high = dp(28), wide = Math.min(text.measureText(words) + dp(46), safe.width() - margin * 2);
            box.set(safe.left + margin, under, safe.left + margin + wide, under + high);
            plate(canvas, box, high / 2, 225);
            if (huntFlash[slot] > 0) {
                stroke.setColor(finished ? 0xFF2E9E6E : CalculatorLayout.CORAL);
                stroke.setAlpha(Math.round(255 * Math.min(1, huntFlash[slot] * 2)));
                stroke.setStrokeWidth(dp(2));
                canvas.drawRoundRect(box, high / 2, high / 2, stroke);
                stroke.setAlpha(255);
            }
            paint.setAlpha(255);
            pieceIn(canvas, four, SCROLL_PICTURE, safe.left + margin + dp(16), under + high / 2, dp(19), 0);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(finished ? 0xFF2E9E6E : ink);
            fitted(canvas, words, safe.left + margin + dp(30), under + dp(18.5f), dp(12.5f), wide - dp(46));
            under += high + dp(6);
        }
        // In a place off the land where something is going on, a plate of its own, apart from the land's: for a cellar, which wave it is
        // and how many of it still stand; for a game, the seconds left, as a number and as a bar that runs down, and her count against
        // the count to beat. The last three seconds are red, and beat.
        if (run.room() >= 0 && run.sealed()) {
            int kind = run.level().rooms.get(run.room())[0];
            boolean cellar = kind == RunLevels.ARENA, last = !cellar && run.gameLeft() <= 3;
            float wide = Math.min(safe.width() - margin * 2, dp(300)), high = dp(cellar ? 44 : 58), from = safe.centerX() - wide / 2;
            box.set(from, under, from + wide, under + high);
            plate(canvas, box, dp(20), 240);
            if (last) {
                stroke.setColor(0xFFE0362F);
                stroke.setAlpha(Math.round(140 + 110 * (float) Math.sin(clock * 14)));
                stroke.setStrokeWidth(dp(2.5f));
                canvas.drawRoundRect(from, under, from + wide, under + high, dp(20), dp(20), stroke);
                stroke.setAlpha(255);
            }
            text.setTextAlign(Paint.Align.LEFT);
            if (cellar) {
                paint.setAlpha(255);
                pieceIn(canvas, four, CELLAR_PICTURE, from + dp(24), under + high / 2, dp(28), 0);
                text.setColor(ink);
                text.setTextSize(dp(15.5f));
                canvas.drawText("Wave " + run.wave() + " of " + PlusRun.WAVES, from + dp(46), under + dp(27.5f), text);
                text.setTextAlign(Paint.Align.RIGHT);
                text.setColor(soft);
                text.setTextSize(dp(13.5f));
                canvas.drawText(run.waveLeft() + " left", from + wide - dp(16), under + dp(27), text);
            } else {
                String what = kind == RunLevels.RANGE ? "Targets" : kind == RunLevels.TRAMPLE ? "Flattened" : "Caught";
                boolean enough = run.caught() >= PlusRun.ENOUGH;
                text.setColor(last ? 0xFFE0362F : ink);
                text.setTextSize(dp(last ? 24 + 2 * (float) Math.sin(clock * 14) : 22));
                canvas.drawText(String.valueOf((int) Math.ceil(run.gameLeft())), from + dp(18), under + dp(30), text);
                text.setTextSize(dp(12));
                text.setColor(soft);
                canvas.drawText("s", from + dp(18) + dp(last ? 30 : 28), under + dp(30), text);
                text.setTextAlign(Paint.Align.RIGHT);
                text.setTextSize(dp(15.5f));
                text.setColor(enough ? 0xFFE0A21A : ink);
                canvas.drawText(what + "  " + run.caught() + " / " + PlusRun.ENOUGH + (enough ? "  ★" : ""), from + wide - dp(16), under + dp(29), text);
                // The bar: what is left of the time, running down from the right.
                float left = from + dp(16), right = from + wide - dp(16), share = Math.max(0, Math.min(1, run.gameLeft() / Math.max(.1f, run.gameLasts())));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(dark ? 0xFF3A3F45 : 0xFFE4E6E8);
                canvas.drawRoundRect(left, under + dp(40), right, under + dp(48), dp(4), dp(4), paint);
                paint.setColor(last ? 0xFFE0362F : CalculatorLayout.CORAL);
                canvas.drawRoundRect(left, under + dp(40), left + (right - left) * share, under + dp(48), dp(4), dp(4), paint);
                paint.setColor(Color.BLACK);
            }
            under += high + dp(8);
        }
        // Her form: its ribbon, with how much of it is left going round it.
        for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) {
            boolean put = colour == PlusRun.BLACK && run.cursed() > 0;
            if (run.on(colour) <= 0 && !put) continue;
            box.set(x, under, x + round, under + round);
            plate(canvas, box, round / 2, 235);
            pieceIn(canvas, two, RIBBON_ART + colour - 1, x + round / 2, under + round / 2, round * .62f, 0);
            stroke.setColor(FORM_COLOURS[colour - 1]);
            stroke.setStrokeWidth(dp(2.5f));
            box.set(x + dp(2), under + dp(2), x + round - dp(2), under + round - dp(2));
            canvas.drawArc(box, -90, 360 * Math.min(1, put ? run.cursed() / Math.max(.1f, run.cursedFor()) : run.on(colour) / Math.max(.1f, run.lasted(colour))), false, stroke);
            x += round * (run.mix() != 0 && Integer.bitCount(run.mix()) > 3 ? .72f : 1) + dp(Integer.bitCount(run.mix()) > 3 ? 0 : 6);
        }
        // And, joined to them, what the mix they make is called, when it has a name: for a moment as it forms, larger, with what it does.
        if (mixShown >= 0 && mixAge < 3.2f && run.mix() != 0) {
            float show = Math.min(1, mixAge / .2f) * Math.max(0, Math.min(1, (3.2f - mixAge) / .4f));
            String name = MIX_NAMES[mixShown] + "!", does = MIX_OF[mixShown] + ": " + MIX_DOES[mixShown];
            // (The white that is truly hers is not that white: it has a name and a line of its own.)
            if (mixShown == PlusRun.WHITEN && run.mastered()) {
                name = "White, and hers!";
                does = "Every colour, for good: the light, both wings, and the beam";
            }
            text.setTextSize(dp(12));
            float wide = Math.min(safe.width() - margin * 2, Math.max(dp(150), text.measureText(does) + dp(28))), high = dp(52);
            float left = safe.centerX() - wide / 2, top = safe.top + safe.height() * .32f;
            canvas.saveLayerAlpha(left - dp(6), top - dp(6), left + wide + dp(6), top + high + dp(10), Math.round(255 * show));
            box.set(left, top, left + wide, top + high);
            plate(canvas, box, dp(18), 245);
            stroke.setColor(lookColour(lookWanted()));
            stroke.setStrokeWidth(dp(2.5f));
            canvas.drawRoundRect(left, top, left + wide, top + high, dp(18), dp(18), stroke);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(ink);
            fitted(canvas, name, safe.centerX(), top + dp(24), dp(18), wide - dp(24));
            text.setColor(soft);
            fitted(canvas, does, safe.centerX(), top + dp(42), dp(12), wide - dp(24));
            canvas.restore();
            box.set(x, under, x + round, under + round);
        }
        // What is helping her, each with how much of it is left going round it.
        float[] left = {0, run.rush() / (BUFF_LASTS[PlusRun.ICE] * perks.lasting), run.shield() ? 1 : 0,
                run.magnet() / (BUFF_LASTS[PlusRun.MAGNET] * perks.lasting), run.notes() / (BUFF_LASTS[PlusRun.NOTES] * perks.lasting)};
        boolean any = run.form() != 0;
        for (int kind = PlusRun.ICE; kind <= PlusRun.NOTES; kind++) {
            if (left[kind] <= 0) continue;
            any = true;
            box.set(x, under, x + round, under + round);
            plate(canvas, box, round / 2, 235);
            artIn(canvas, BUFF_ART + kind, x + round / 2, under + round / 2, round * .62f);
            stroke.setColor(CalculatorLayout.CORAL);
            stroke.setStrokeWidth(dp(2.5f));
            box.set(x + dp(2), under + dp(2), x + round - dp(2), under + round - dp(2));
            canvas.drawArc(box, -90, 360 * Math.min(1, left[kind]), false, stroke);
            x += round + dp(6);
        }
        if (any) under += round + dp(8);
        // What has just been said, under them: who says it, round, her name in her colour, and her words.
        if (captionLeft > 0 && caption != null && (screen == PLAY || screen == STALL)) {
            float fade = Math.min(1, captionLeft / .3f), face = dp(17);
            text.setTextSize(dp(14));
            // (Kept clear of the pet's plate at the top right, when there is one.)
            float room = Math.min(safe.width() - dp(48) - face * 2 - dp(8) - (run.pet() >= 0 ? dp(50) : 0), dp(300));
            List<String> lines = wrap(caption, room);
            // No more than two lines at once: what is longer is said in two bubbles in turn, parted where a sentence ends, each for its
            // share of the time.
            if (lines.size() > 2) {
                int part = -1, middle = caption.length() / 2;
                for (int at = 1; at < caption.length() - 1; at++) {
                    char mark = caption.charAt(at - 1);
                    if (caption.charAt(at) == ' ' && (mark == '.' || mark == '!' || mark == '?' || mark == ':' || mark == ',')
                            && (part < 0 || Math.abs(at - middle) < Math.abs(part - middle))) part = at;
                }
                if (part > 0) {
                    boolean first = 1 - captionLeft / Math.max(.1f, captionFor) < part / (float) caption.length();
                    lines = wrap(first ? caption.substring(0, part) : caption.substring(part + 1), room);
                }
                if (lines.size() > 2) lines = lines.subList(0, 2);
            }
            float wide = text.measureText(RunStory.NAMES[captionWho]);
            for (String one : lines) wide = Math.max(wide, text.measureText(one));
            float high = Math.max(lines.size() * dp(19) + dp(30), face * 2 + dp(14));
            box.set(safe.left + margin, under, safe.left + margin + wide + dp(30) + face * 2, under + high);
            canvas.saveLayerAlpha(box.left - dp(4), box.top - dp(4), box.right + dp(4), box.bottom + dp(6), Math.round(255 * fade));
            plate(canvas, box, dp(16), 240);
            round(canvas, captionWho, safe.left + margin + dp(8) + face, under + dp(7) + face, face);
            float words = safe.left + margin + dp(16) + face * 2;
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(dp(12));
            text.setColor(dark && captionWho == 5 ? 0xFFD98A9E : SPEAKER_COLOURS[captionWho]);
            canvas.drawText(RunStory.NAMES[captionWho], words, under + dp(17), text);
            text.setTextSize(dp(14));
            text.setColor(ink);
            for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), words, under + dp(22) + dp(19) * (index + .74f), text);
            canvas.restore();
        }
    }

    /** The button that opens a stall she stands at: round, like the others, over the jump, with a basket on it and what she has to spend. */
    private void stallControl(Canvas canvas) {
        int colour = SPEAKER_COLOURS[FIRST_VILLAGER + RunLevels.ALTTEUL - RunLevels.SOMI];
        float beat = 1 + .04f * (float) Math.sin(clock * 6);
        canvas.save();
        canvas.scale(beat, beat, stallButton.centerX(), stallButton.centerY());
        button(canvas, stallButton, false, colour);
        basket(canvas, stallButton.centerX(), stallButton.centerY() - dp(8), stallButton.width() * .34f, colour);
        label(canvas, stallButton, "Shop");
        canvas.restore();
        // What she has to spend there, apart from the button: a small pill over it.
        String purse = "+ " + run.purse();
        text.setTextSize(dp(12.5f));
        float wide = text.measureText(purse) + dp(18), high = dp(22), top = stallButton.top - high - dp(4);
        box.set(stallButton.centerX() - wide / 2, top, stallButton.centerX() + wide / 2, top + high);
        plate(canvas, box, high / 2, 240);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(colour);
        canvas.drawText(purse, stallButton.centerX(), top + dp(15.5f), text);
    }

    /**
     * A word along the bottom of a round button, inside it: it is measured against the
     * width the circle has at the height the word is written at, less a margin, and written
     * smaller until it fits, so that nothing written on a round button crosses its rim.
     */
    private void label(Canvas canvas, RectF button, String word) {
        float round = button.width() / 2, size = Math.min(dp(12.5f), round * .36f), down = round * .5f;
        float room = 2 * (float) Math.sqrt(Math.max(0, round * round - (down + size * .3f) * (down + size * .3f))) - dp(8);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(0xFF292B2A);
        fitted(canvas, word, button.centerX(), button.centerY() + down + size * .35f, size, room);
    }

    /** A little shopping basket, drawn: a bowl with a handle over it. */
    private void basket(Canvas canvas, float x, float y, float size, int colour) {
        stroke.setColor(colour);
        stroke.setAlpha(255);
        stroke.setStrokeWidth(size * .16f);
        box.set(x - size * .42f, y - size * .7f, x + size * .42f, y + size * .3f);
        canvas.drawArc(box, 180, 180, false, stroke);
        path.reset();
        path.moveTo(x - size * .62f, y - size * .12f);
        path.lineTo(x + size * .62f, y - size * .12f);
        path.lineTo(x + size * .44f, y + size * .5f);
        path.lineTo(x - size * .44f, y + size * .5f);
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colour);
        paint.setAlpha(255);
        canvas.drawPath(path, paint);
        paint.setColor(Color.BLACK);
    }

    /**
     * A mark over someone in the land who has something for her, bobbing: a bubble with a
     * word or a sign in it, in her colour, and a little tail under it pointing at her.
     * Returns how wide it is.
     */
    private float mark(Canvas canvas, float x, float y, String word, int colour, boolean shop) {
        text.setTextSize(tile * (shop ? .44f : .4f));
        float wide = Math.max(tile * .66f, text.measureText(word) + tile * (shop ? .86f : .34f)), high = tile * (shop ? .68f : .6f);
        float top = y - high + (float) Math.sin(clock * 4.5f + x * .01f) * tile * .07f;
        box.set(x - wide / 2, top, x + wide / 2, top + high);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x40000000);
        canvas.drawRoundRect(box.left, box.top + tile * .04f, box.right, box.bottom + tile * .05f, high / 2, high / 2, paint);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(box, high / 2, high / 2, paint);
        path.reset();
        path.moveTo(x - tile * .1f, top + high - 1);
        path.lineTo(x + tile * .1f, top + high - 1);
        path.lineTo(x, top + high + tile * .14f);
        path.close();
        canvas.drawPath(path, paint);
        stroke.setColor(colour);
        stroke.setAlpha(255);
        stroke.setStrokeWidth(tile * .045f);
        canvas.drawRoundRect(box, high / 2, high / 2, stroke);
        paint.setColor(Color.BLACK);
        float left = box.left, middle = box.centerY();
        if (shop) basket(canvas, left + tile * .32f, middle + tile * .02f, tile * .3f, colour);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(colour);
        canvas.drawText(word, x + (shop ? tile * .22f : 0), middle + tile * .15f, text);
        return wide;
    }

    /** A stall, open: what it sells for the pluses collected in this land, and the way to the counter where what she has banked buys what she keeps. */
    private void drawStall(Canvas canvas) {
        float row = dp(48), gap = dp(6);
        float top = card(canvas, "Altteul's stall", "+ " + run.purse() + " collected in this land to spend", stallWares.length * (row + gap) + dp(4));
        float wide = Math.min(safe.width() - dp(32), dp(320)), left = safe.centerX() - wide / 2 + dp(14), right = safe.centerX() + wide / 2 - dp(14);
        int form = STALL_FORMS[number % STALL_FORMS.length];
        for (int index = 0; index < stallWares.length; index++) {
            RectF where = stallWares[index];
            where.set(left, top + index * (row + gap), right, top + index * (row + gap) + row);
            int kind = index < 3 ? STALL_SELLS[index] : PlusRun.FORM + form, why = run.refusal(kind, STALL_COSTS[index]);
            boolean can = why == PlusRun.SOLD, gone = why == PlusRun.SOLD_OUT;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF2A2E33 : 0xFFF6F2EC);
            canvas.drawRoundRect(where, dp(12), dp(12), paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            paint.setAlpha(gone ? 70 : 255);
            help(canvas, kind, left + dp(24), where.centerY(), dp(30));
            paint.setAlpha(255);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(gone ? soft : ink);
            // Its name, and under it how many are left here, or why it is not to be had just now.
            int left1 = PlusRun.stock(kind) - run.stallSold(kind);
            String under = gone ? "Sold out here: the next stall has more" : why == PlusRun.WORN ? "Not while she wears a colour" : why == PlusRun.NEEDLESS
                    ? (index == 0 ? "Her hearts are full" : index == 1 ? "She has one about her" : "She is full of it") : left1 + " left at this stall";
            fitted(canvas, index < 3 ? STALL_NAMES[index] : FORM_NAMES[form - 1] + " ribbon: " + FORM_GIVES[form - 1], left + dp(46), where.centerY() - dp(1), dp(13.5f),
                    right - left - dp(46) - dp(70));
            text.setColor(soft);
            fitted(canvas, under, left + dp(46), where.centerY() + dp(15), dp(11), right - left - dp(46) - dp(70));
            box.set(right - dp(64), where.centerY() - dp(15), right - dp(8), where.centerY() + dp(15));
            paint.setColor(can ? CalculatorLayout.CORAL : dark ? 0xFF44292E : 0xFFFCE9EB);
            canvas.drawRoundRect(box, dp(15), dp(15), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(can ? Color.WHITE : CalculatorLayout.CORAL);
            text.setTextSize(dp(14));
            fitted(canvas, gone ? "Gone" : "+ " + STALL_COSTS[index], box.centerX(), box.centerY() + dp(5), text.getTextSize(), box.width() - dp(8));
        }
    }

    /** Who asks a thing, by the name she is called by; and the picture that stands for the thing. */
    private static String giverName(int quest) {
        String name = RunStory.NAMES[FIRST_VILLAGER + RunLevels.giverOf(quest) - RunLevels.SOMI];
        int cut = name.indexOf('-');
        return cut > 0 ? name.substring(0, cut) : name;
    }

    private static int questPicture(int quest) {
        return quest == RunLevels.CAKES ? CAKE_PICTURE : quest == RunLevels.CELLAR ? CELLAR_PICTURE : quest == RunLevels.LETTER ? LETTER_PICTURE
                : quest == RunLevels.TIME ? WATCH_PICTURE : CRACK_PICTURE;
    }

    /**
     * What was asked of her is handed in, as a moment of its own, in which the land waits.
     * Whoever thanks her is there: where she stands, or come running up behind Halgakos if
     * she was not gone back to. What she gives leaves her hands and crosses to Halgakos in
     * an arc, with light about it; then a card names it and says what it does, and a guild
     * seal comes down on the card with a thud. A touch, once that has been seen, sends it on.
     */
    private void drawHandOver(Canvas canvas) {
        float age = handAge;
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herY = (run.y() + PlusRun.TALL - cameraY) * tile;
        float came = handRuns ? Math.min(1, age / .55f) : 1, eased = 1 - (1 - came) * (1 - came);
        float way = handX < run.x() ? -1 : 1;
        float x = (handX - cameraX) * tile + (1 - eased) * way * tile * 7, y = (handY - cameraY) * tile;
        if (handRuns) {
            // She has come running: dust behind her, and a hop as she pulls up.
            paint.setAlpha(255);
            float hop = came < 1 ? Math.abs((float) Math.sin(age * 22)) * tile * .12f : 0;
            frame(canvas, villagers(), handWho - RunLevels.SOMI, VILLAGERS, x, y - hop, tile * 1.62f * .72f, way > 0);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFDDE3E8);
            for (int puff = 0; puff < 4 && came < 1; puff++) {
                paint.setAlpha(Math.round(150 * (1 - came)));
                canvas.drawCircle(x + way * tile * (.5f + puff * .45f), y - tile * .1f, tile * (.12f + puff * .03f), paint);
            }
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
        }
        // What she gives: out of her hands, and over to Halgakos.
        float go = Math.max(0, Math.min(1, (age - .7f) / .9f)), smooth = go * go * (3 - 2 * go);
        float fromX = x - way * tile * .5f, fromY = y - tile * 1.05f, toX = herX, toY = herY - tile * 1.05f;
        float atX = fromX + (toX - fromX) * smooth, atY = fromY + (toY - fromY) * smooth - (float) Math.sin(smooth * Math.PI) * tile * 1.6f;
        if (age > .45f && age < 2.2f) {
            float big = tile * (.75f + .35f * (float) Math.sin(smooth * Math.PI));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFFD24A);
            for (int ray = 0; ray < 8; ray++) {
                double turn = clock * 1.4f + ray * Math.PI / 4;
                path.reset();
                path.moveTo(atX, atY);
                path.lineTo(atX + (float) Math.cos(turn - .14) * big * 1.5f, atY + (float) Math.sin(turn - .14) * big * 1.5f);
                path.lineTo(atX + (float) Math.cos(turn + .14) * big * 1.5f, atY + (float) Math.sin(turn + .14) * big * 1.5f);
                path.close();
                paint.setAlpha(90);
                canvas.drawPath(path, paint);
            }
            paint.setColor(Color.WHITE);
            paint.setAlpha(210);
            canvas.drawCircle(atX, atY, big * .62f, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            if (handPicture < 0) prizePicture(canvas, -handPicture, atX, atY, big);
            else pieceIn(canvas, four, handPicture, atX, atY, big, 0);
        }
        // (What it is, is named in a card at the top, as anything won is.)
    }

    /** The round body of a button: a soft shadow under it, a rim of light inside its edge, and a little smaller while it is held. */
    private float button(Canvas canvas, RectF where, boolean held, int colour) {
        float x = where.centerX(), y = where.centerY(), round = where.width() / 2 * (held ? .93f : 1);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAlpha(held ? 30 : 46);
        canvas.drawCircle(x, y + dp(held ? 1.5f : 3), round, paint);
        paint.setColor(held ? colour : Color.WHITE);
        paint.setAlpha(held ? 242 : 178);
        canvas.drawCircle(x, y, round, paint);
        stroke.setColor(Color.WHITE);
        stroke.setAlpha(held ? 110 : 220);
        stroke.setStrokeWidth(dp(2));
        canvas.drawCircle(x, y, round - dp(3.5f), stroke);
        stroke.setColor(Color.BLACK);
        stroke.setAlpha(50);
        stroke.setStrokeWidth(dp(1));
        canvas.drawCircle(x, y, round, stroke);
        stroke.setAlpha(255);
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
        return round;
    }

    /** A round button with an arrowhead in it pointing some way round; pink, when the arrows have been turned about on her. */
    private void control(Canvas canvas, RectF where, boolean held, float turn, boolean charmed) {
        float reach = button(canvas, where, held, charmed ? 0xFFFF6FB5 : CalculatorLayout.CORAL) * .3f;
        path.reset();
        path.moveTo(-reach * .5f, -reach);
        path.lineTo(reach * .6f, 0);
        path.lineTo(-reach * .5f, reach);
        canvas.save();
        canvas.translate(where.centerX(), where.centerY());
        canvas.rotate(turn);
        stroke.setColor(held ? Color.WHITE : charmed ? 0xFFE8479A : 0xFF3A3F45);
        stroke.setStrokeWidth(where.width() * .085f);
        canvas.drawPath(path, stroke);
        canvas.restore();
    }

    /** The button that uses what she carries, with a picture of it, and how many shots an ink gun has left. */
    private void strikeControl(Canvas canvas) {
        RectF where = strikeButton;
        int weapon = run.weapon();
        button(canvas, where, goStrike, CalculatorLayout.CORAL);
        boolean upright = weapon != PlusRun.GUN && weapon != PlusRun.BOOK;
        paint.setAlpha(255);
        boolean casts = run.has(PlusRun.RED) || run.has(PlusRun.BLUE), both = run.has(PlusRun.RED) && run.has(PlusRun.BLUE), white = run.white();
        if (white) {
            // In white the button wears white and gold; in the white that is truly hers, the beam's mark: gathering while it is held, a
            // ring that closes while it waits, and bright when it is ready.
            stroke.setColor(0xFFE9C96A);
            stroke.setStrokeWidth(dp(3));
            canvas.drawCircle(where.centerX(), where.centerY(), where.width() / 2 - dp(2), stroke);
            if (run.mastered()) {
                float ready = run.beamWait() > 0 ? 1 - run.beamWait() / PlusRun.BEAM_WAIT : 1, charge = run.beamCharge();
                box.set(where.left - dp(3), where.top - dp(3), where.right + dp(3), where.bottom + dp(3));
                stroke.setColor(Color.WHITE);
                stroke.setStrokeWidth(dp(3.5f));
                canvas.drawArc(box, -90, 360 * (charge > 0 ? charge : ready), false, stroke);
                paint.setAlpha(ready >= 1 ? 255 : 110);
                art(canvas, STAR_ART, where.right - dp(9), where.top + dp(13), dp(16) + dp(5) * charge, false);
                paint.setAlpha(255);
            }
        }
        if (casts && weapon == PlusRun.UNARMED) {
            if (both) {
                pieceIn(canvas, two, FIRE_ART, where.centerX() - where.width() * .14f, where.centerY(), where.height() * .42f, 0);
                pieceIn(canvas, two, DROP_ART, where.centerX() + where.width() * .17f, where.centerY() + where.height() * .03f, where.height() * .32f, 0);
            } else {
                pieceIn(canvas, two, run.has(PlusRun.RED) ? FIRE_ART : DROP_ART, where.centerX(), where.centerY(), where.height() * .5f, 0);
            }
            return;
        }
        thing(canvas, weapon, where.centerX(), where.centerY(), where.height() * (upright ? .66f : .48f), upright ? 35 : 0, false, false);
        // In red or in blue the button does both: what she carries, and on its corner the fire or the water that goes with it.
        if (casts) {
            float corner = where.height() * .34f, x = where.right - corner * .55f, y = where.top + corner * .55f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(cardColor);
            canvas.drawCircle(x, y, corner * .62f, paint);
            paint.setColor(Color.BLACK);
            if (both) {
                pieceIn(canvas, two, FIRE_ART, x - corner * .18f, y, corner * .8f, 0);
                pieceIn(canvas, two, DROP_ART, x + corner * .24f, y + corner * .05f, corner * .6f, 0);
            } else {
                pieceIn(canvas, two, run.has(PlusRun.RED) ? FIRE_ART : DROP_ART, x, y, corner, 0);
            }
        }
        if (weapon == PlusRun.GUN || weapon == PlusRun.BOOK) {
            text.setTextSize(dp(12));
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(goStrike ? Color.WHITE : 0xFF292B2A);
            canvas.drawText(String.valueOf(weapon == PlusRun.GUN ? run.rounds() : run.books()), where.centerX(), where.bottom - dp(8), text);
        }
        if (weapon >= 0 && perks.levels[weapon] > 0) {
            // How far the smith has worked on it: its level, on the button's corner.
            float cx = where.left + dp(9), cy = where.top + dp(9);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(cardColor);
            canvas.drawCircle(cx, cy, dp(9), paint);
            paint.setColor(Color.BLACK);
            text.setTextSize(dp(11.5f));
            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setColor(CalculatorLayout.CORAL);
            canvas.drawText(String.valueOf(perks.levels[weapon] + 1), cx, cy + dp(4), text);
            text.setTypeface(Typeface.DEFAULT);
        }
        if (weapon >= 0 && RunArms.heavy(weapon) && run.heldFor() > 0 && !run.mastered()) {
            box.set(where.left - dp(3), where.top - dp(3), where.right + dp(3), where.bottom + dp(3));
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(dp(3.5f));
            canvas.drawArc(box, -90, 360 * Math.min(1, run.heldFor() / RunArms.CHARGE), false, stroke);
        }
        if (run.boost() > 0) {
            box.set(where.left + dp(2), where.top + dp(2), where.right - dp(2), where.bottom - dp(2));
            stroke.setColor(0xFFFFC53D);
            stroke.setStrokeWidth(dp(3));
            canvas.drawArc(box, -90, 360 * run.boost() / RunArms.BOOSTED, false, stroke);
        }
    }

    List<String> wrap(String words, float wide) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : words.split(" ")) {
            if (current.length() > 0 && text.measureText(current + " " + word) > wide) {
                lines.add(current.toString());
                current.setLength(0);
            }
            if (current.length() > 0) current.append(' ');
            current.append(word);
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    /** Writes a line no wider than there is room for: smaller, if that is what it takes. */
    void fitted(Canvas canvas, String words, float x, float y, float size, float room) {
        if (words.contains("+ ") && PLUS_AMOUNT.matcher(words).find()) {
            plusFitted(canvas, words, x, y, size, room);
            return;
        }
        text.setTextSize(size);
        float wide = text.measureText(words);
        if (wide > room) text.setTextSize(size * room / wide);
        canvas.drawText(words, x, y, text);
    }

    /** An amount of pluses, as it is written: a "+" before a number. */
    private static final java.util.regex.Pattern PLUS_AMOUNT = java.util.regex.Pattern.compile("\\+ (?=\\d)");
    /** Her plus: the glossy one, and the plain bold one for small sizes. */
    private Bitmap plusCoin;

    private void plusIcon(Canvas canvas, float x, float y, float size, int alpha) {
        if (plusCoin == null) plusCoin = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_plus_coin);
        int which = size < dp(22) ? 1 : 0, side = plusCoin.getHeight();
        source.set(which * side, 0, which * side + side, side);
        target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        int was = paint.getAlpha();
        paint.setAlpha(alpha);
        canvas.drawBitmap(plusCoin, source, target, paint);
        paint.setAlpha(was);
    }

    /** Words with amounts of pluses in them: each "+" drawn as her plus, the whole fitted to the room and placed as the text is aligned. */
    private void plusFitted(Canvas canvas, String words, float x, float y, float size, float room) {
        String[] parts = PLUS_AMOUNT.split(words, -1);
        text.setTextSize(size);
        float wide = plusWide(parts, size);
        if (wide > room) {
            size *= room / wide;
            text.setTextSize(size);
            wide = plusWide(parts, size);
        }
        Paint.Align align = text.getTextAlign();
        float at = align == Paint.Align.LEFT ? x : align == Paint.Align.CENTER ? x - wide / 2 : x - wide, icon = size * 1.12f;
        text.setTextAlign(Paint.Align.LEFT);
        for (int index = 0; index < parts.length; index++) {
            canvas.drawText(parts[index], at, y, text);
            at += text.measureText(parts[index]);
            if (index < parts.length - 1) {
                plusIcon(canvas, at + icon / 2, y - size * .36f, icon, text.getAlpha());
                at += icon + size * .14f;
            }
        }
        text.setTextAlign(align);
    }

    private float plusWide(String[] parts, float size) {
        float wide = 0;
        for (String part : parts) wide += text.measureText(part);
        return wide + (parts.length - 1) * (size * 1.12f + size * .14f);
    }

    /**
     * A card in the middle of the screen: a heading, a line under it, room for something
     * between, and its buttons along the bottom. Returns where the room for something is.
     */
    private float card(Canvas canvas, String title, String under, float room) {
        float wide = Math.min(safe.width() - dp(32), dp(320)), pad = dp(20), button = dp(46), gap = dp(10);
        boolean stacked = pillCount == 3 && wide < dp(300);
        float buttons = stacked ? pillCount * button + (pillCount - 1) * gap : button;
        float tall = pad + dp(26) + dp(26) + room + dp(14) + buttons + pad;
        box.set(safe.centerX() - wide / 2, safe.centerY() - tall / 2, safe.centerX() + wide / 2, safe.centerY() + tall / 2);
        float left = box.left, top = box.top, bottom = box.bottom;
        plate(canvas, box, dp(22), 255);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        fitted(canvas, title, left + pad, top + pad + dp(20), dp(21), wide - pad * 2);
        text.setColor(soft);
        fitted(canvas, under, left + pad, top + pad + dp(44), dp(14.5f), wide - pad * 2);
        float inside = top + pad + dp(52);
        float each = stacked ? wide - pad * 2 : (wide - pad * 2 - gap * (pillCount - 1)) / pillCount;
        for (int index = 0; index < pillCount; index++) {
            float x = stacked ? left + pad : left + pad + index * (each + gap);
            float y = stacked ? bottom - pad - buttons + index * (button + gap) : bottom - pad - button;
            pills[index].set(x, y, x + each, y + button);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(index == 0 ? CalculatorLayout.CORAL : dark ? 0xFF44292E : 0xFFFCE9EB);
            canvas.drawRoundRect(pills[index], button / 2, button / 2, paint);
            text.setColor(index == 0 ? Color.WHITE : CalculatorLayout.CORAL);
            text.setTextAlign(Paint.Align.CENTER);
            fitted(canvas, pillLabels[index], pills[index].centerX(), pills[index].centerY() + dp(5.5f), dp(15.5f), each - dp(12));
        }
        paint.setColor(Color.BLACK);
        return inside;
    }

    private void drawResult(Canvas canvas) {
        if (screen == DELVE) {
            drawDelveCard(canvas);
            return;
        }
        if (delve >= 0) {
            drawDiveResult(canvas);
            return;
        }
        String chapter = RunStory.CHAPTERS[RunLevels.chapterOf(number)];
        String kept = "kept + " + banked + (questLine != null ? "  ·  " + questLine : "");
        if (finale == UNMADE) {
            // The land unmade: what the black ribbon would have been turned by, and how much of it she had.
            int colours = Integer.bitCount(colours() & 127), friends = Integer.bitCount(session.tally("run_friends") & (1 << RunLevels.FRIENDS) - 1);
            StringBuilder still = new StringBuilder();
            for (int friend = 0; friend < RunLevels.FRIENDS; friend++) {
                if ((session.tally("run_friends") >> friend & 1) == 0) still.append(still.length() == 0 ? "" : ", ").append(RunLevels.SECOND + 1 + friend);
            }
            float wide = Math.min(safe.width() - dp(32), dp(320)) - dp(40);
            text.setTextSize(dp(12.5f));
            List<String> where = wrap(still.length() == 0 ? "" : "Still to pledge, in lands " + still, wide);
            float top = card(canvas, "The land is unmade", chapter + ", land " + number + "  ·  " + kept, dp(74) + where.size() * dp(17));
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, "Only someone whole turns the black ribbon:", box.left + dp(20), top + dp(18), dp(13.5f), wide);
            text.setColor(colours == 7 ? 0xFF2E9E6E : 0xFFE0362F);
            fitted(canvas, (colours == 7 ? "✓  " : "✗  ") + "Every colour hers: " + colours + " of 7", box.left + dp(20), top + dp(40), dp(14), wide);
            text.setColor(friends == RunLevels.FRIENDS ? 0xFF2E9E6E : 0xFFE0362F);
            fitted(canvas, (friends == RunLevels.FRIENDS ? "✓  " : "✗  ") + "Every friend's pledge: " + friends + " of " + RunLevels.FRIENDS, box.left + dp(20), top + dp(60), dp(14), wide);
            text.setColor(soft);
            text.setTextSize(dp(12.5f));
            for (int index = 0; index < where.size(); index++) canvas.drawText(where.get(index), box.left + dp(20), top + dp(80) + index * dp(17), text);
            return;
        }
        if (blackRung > 0 && won) {
            // A rung of the black arena won: its tokens, and what it gave the first time.
            float top = card(canvas, "Rung " + blackRung + " is won", waitName(RunLevels.bossOf(number)) + ", stronger  ·  " + PlayStage.clock(Math.max(1, Math.round(run.time()))),
                    dp(rungPrize.isEmpty() ? 40 : 66));
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(CalculatorLayout.CORAL);
            fitted(canvas, rungWords, safe.centerX(), top + dp(24), dp(17), Math.min(safe.width() - dp(32), dp(320)) - dp(40));
            if (!rungPrize.isEmpty()) {
                text.setColor(ink);
                fitted(canvas, rungPrize, safe.centerX(), top + dp(50), dp(13.5f), Math.min(safe.width() - dp(32), dp(320)) - dp(40));
            }
            text.setTextAlign(Paint.Align.LEFT);
            return;
        }
        if (!won) {
            // Her on the card, sitting where she fell: dazed at first, then sulking and crying by turns.
            float high = dp(104);
            float top = card(canvas, "Out of lives", chapter + ", land " + number + "  ·  " + kept, high + dp(8));
            int pose = resultClock < 1.3f ? DAZED : (int) ((resultClock - 1.3f) / .55f) % 2 == 0 ? SULK : CRY;
            paint.setAlpha(255);
            frame(canvas, herLose(knockForm), pose, LOSE_FRAMES, safe.centerX(), top + high + dp(4), dp(96), false);
            return;
        }
        float star = dp(46);
        String title = landTitle();
        float prizeTall = prizeWon != RunLevels.NOTHING ? dp(78) : 0;
        float top = card(canvas, title, "+ " + run.collected() + " of " + run.total() + "  ·  " + kept, star + dp(10) + prizeTall);
        drawResultRest(canvas, star, top);
    }

    /** What a land won is called: for one where someone waited, what became of her; otherwise its number, cleared. */
    private String landTitle() {
        int boss = RunLevels.bossOf(number);
        return boss == RunLevels.ZERO ? "Zero is flat!" : boss == RunLevels.ILLYA ? "Illya gives way" : boss == RunLevels.TSUBAKIEH ? "The bat is broken"
                : boss == RunLevels.GOKHAE ? "Gokhae folds her fan" : boss == RunLevels.MIRI ? "The Gazette is corrected" : boss == RunLevels.YEOBAEK ? "Yeobaek naps"
                : boss == RunLevels.KING ? "King Slime is a puddle" : boss == RunLevels.MAWANG ? "The Mawang comes home" : boss == RunLevels.GUARD ? "Exam passed"
                : boss == RunLevels.BUTLER ? "The butler bows out" : boss == RunLevels.BYEORU ? "Written back in" : boss == RunLevels.JUPAN ? "The toll is refunded"
                : boss == RunLevels.SUMBI ? "Sumbi comes up for air" : boss == RunLevels.PADS ? "The brakes are off" : boss == RunLevels.PARTY ? "Straight home they go"
                : boss == RunLevels.LUCY ? (finale == RIDDEN ? "The Mother Tongue listens" : "Lucy is read, as written") : "Land " + number + " cleared";
    }

    private void drawResultRest(Canvas canvas, float star, float top) {
        // The stars she earned, each popping up in its turn; the rest stay faint.
        for (int index = 0; index < 3; index++) {
            float since = resultClock - .35f - index * .32f;
            boolean earned = index < stars && since > 0;
            float size = star * (earned ? 1 + .35f * Math.max(0, 1 - since / .18f) : .9f);
            paint.setAlpha(earned ? 255 : 55);
            art(canvas, STAR_ART, safe.centerX() + (index - 1) * (star + dp(12)), top + dp(6) + star + (size - star) / 2, size, false);
        }
        paint.setAlpha(255);
        // What was won to keep: its picture, its name, and what it does, coming up after the stars.
        if (prizeWon != RunLevels.NOTHING && resultClock > 1.2f) {
            float wide = Math.min(safe.width() - dp(32), dp(320)) - dp(40), left = safe.centerX() - wide / 2, down = top + star + dp(18);
            float grow = Math.min(1, (resultClock - 1.2f) / .25f);
            box.set(left, down, left + wide, down + dp(66));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(prizeWon > 0 ? FORM_COLOURS[prizeWon - 1] : 0xFFE0B13A);
            paint.setAlpha(Math.round(46 * grow));
            canvas.drawRoundRect(box, dp(16), dp(16), paint);
            paint.setAlpha(255);
            paint.setColor(Color.BLACK);
            prizePicture(canvas, prizeWon, left + dp(32), down + dp(33), dp(46) * (1 + .3f * (1 - grow)));
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, "Won to keep: " + prizeName(prizeWon), left + dp(62), down + dp(27), dp(15.5f), wide - dp(72));
            text.setColor(soft);
            fitted(canvas, prizeWon > 0 ? "Call on it with the ribbon by what she carries. " + capital(prizeGives(prizeWon)) + "." : capital(prizeGives(prizeWon)) + ".", left + dp(62), down + dp(48), dp(12.5f),
                    wide - dp(72));
        }
    }

    private static String capital(String words) { return words.isEmpty() ? words : Character.toUpperCase(words.charAt(0)) + words.substring(1); }

    /** The picture of something won to keep: a ribbon of its colour, or Zero's ring. */
    private void prizePicture(Canvas canvas, int prize, float x, float y, float size) {
        if (prize >= 100) {
            // A pet: as it stands.
            cell(canvas, petArt(prize - 100), 0, 6, x, y + size * .55f, size * 1.25f, false);
            return;
        }
        if (prize > 0) {
            pieceIn(canvas, two, RIBBON_ART + prize - 1, x, y, size, 0);
            return;
        }
        if (prize == RunLevels.HEART) {
            artIn(canvas, HEART_ART, x, y, size * .8f);
        } else if (prize == RunLevels.TANK) {
            thing(canvas, PlusRun.GUN, x, y, size * .62f, 0, false, false);
        } else if (prize == RunLevels.QUICK) {
            artIn(canvas, STAR_ART, x, y, size * .8f);
        } else if (prize == RunLevels.BOUNTY) {
            artIn(canvas, PLUS_ART, x, y, size * .8f);
        } else {
            stroke.setColor(0xFFE0B13A);
            stroke.setAlpha(255);
            stroke.setStrokeWidth(size * .2f);
            canvas.drawCircle(x, y, size * .3f, stroke);
        }
    }

    /** The button that calls on the colour she has chosen, with how full what she calls on it with is going round it; and, with more than one to choose from, a smaller one that chooses the next. */
    /**
     * The ribbon that calls on her mix, and the palette beside it that opens the picker in
     * which the mix is chosen. The ribbon is the colour of the mix, with a dot on its rim
     * for each colour in it: bright if that one is on, grey if it is waiting. How the mix
     * stands is one of three things, each unlike the others. On, when any of it is: filled
     * with its colour, a ring running down, the seconds the first of it to go has left
     * written large, and the word "on". Ready, when some of it could be called: white, its
     * ribbon whole and bright, a ring of its colour beating round it. And waiting, when
     * none of it could: grey, the ribbon pale, and "wait" with the seconds until the first
     * of it is ready.
     */
    private void colourControl(Canvas canvas) {
        int owned = perks.colours & 127;
        if (chosen == 0 || chosen > PlusRun.BLACK || (owned & 1 << chosen - 1) == 0) chosen = Integer.numberOfTrailingZeros(owned) + 1;
        if ((herMix & owned) == 0) herMix = 1 << chosen - 1;
        int mix = herMix & owned, wearing = run.mix() & mix, ready = run.ready() & mix, count = Integer.bitCount(mix);
        float x = colourButton.centerX(), y = colourButton.centerY(), round = colourButton.width() / 2;
        boolean on = wearing != 0, waiting = !on && ready == 0, held = ribbonFinger >= 0;
        // The colour of the mix, the least time any of it that is on has left, and the least wait of any of it.
        int red = 0, green = 0, blue = 0, first = Integer.numberOfTrailingZeros(mix) + 1;
        float left = 99, lasts = 1, until = 99;
        for (int each = PlusRun.RED; each <= PlusRun.BLACK; each++) {
            if ((mix & 1 << each - 1) == 0) continue;
            red += Color.red(FORM_COLOURS[each - 1]);
            green += Color.green(FORM_COLOURS[each - 1]);
            blue += Color.blue(FORM_COLOURS[each - 1]);
            if (run.on(each) > 0 && run.on(each) < left) {
                left = run.on(each);
                lasts = Math.max(.1f, run.lasted(each));
            }
            if (run.on(each) <= 0) until = Math.min(until, run.untilCall(each));
        }
        if (run.mastered()) {
            // The white that is truly hers has no clock: the ribbon wears white and gold, with its star, and says nothing of seconds.
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x33000000);
            canvas.drawCircle(x, y + dp(2), round, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, round, paint);
            paint.setColor(Color.BLACK);
            stroke.setColor(0xFFE9C96A);
            stroke.setStrokeWidth(dp(4));
            canvas.drawCircle(x, y, round - dp(3), stroke);
            artIn(canvas, STAR_ART, x, y, round * .9f);
            return;
        }
        int made = blend(mix);
        boolean parts = count > 1 && count < 6 && made == 0;
        int colour = count == 6 ? FORM_COLOURS[PlusRun.WHITE - 1] : count == 1 ? FORM_COLOURS[first - 1] : made != 0 ? made : FORM_COLOURS[first - 1];
        boolean pale = !parts && Color.red(colour) * .3f + Color.green(colour) * .59f + Color.blue(colour) * .11f > 170;
        box.set(colourButton.left + dp(3), colourButton.top + dp(3), colourButton.right - dp(3), colourButton.bottom - dp(3));
        text.setTextAlign(Paint.Align.CENTER);
        canvas.save();
        if (held) canvas.scale(.93f, .93f, x, y);
        if (on) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x33000000);
            canvas.drawCircle(x, y + dp(2), round, paint);
            paint.setColor(colour);
            canvas.drawCircle(x, y, round, paint);
            if (parts) {
                // A mix that does not blend: the round in as many parts as it has colours, each its own.
                int part = 0;
                box.set(x - round, y - round, x + round, y + round);
                for (int each = PlusRun.RED; each <= PlusRun.BLACK; each++) {
                    if ((mix & 1 << each - 1) == 0) continue;
                    paint.setColor(FORM_COLOURS[each - 1]);
                    canvas.drawArc(box, -90 + part++ * 360f / count, 360f / count, true, paint);
                }
                paint.setColor(0x55000000);
                canvas.drawCircle(x, y, round * .62f, paint);
                box.set(colourButton.left + dp(3), colourButton.top + dp(3), colourButton.right - dp(3), colourButton.bottom - dp(3));
            }
            paint.setColor(Color.BLACK);
            stroke.setColor(pale ? 0xFF3A3F45 : Color.WHITE);
            stroke.setAlpha(left < 2 && (int) (clock * 10) % 2 == 0 ? 110 : 255);
            stroke.setStrokeWidth(dp(4));
            canvas.drawArc(box, -90, 360 * Math.max(0, Math.min(1, left / lasts)), false, stroke);
            stroke.setAlpha(255);
            text.setColor(pale ? 0xFF3A3F45 : Color.WHITE);
            text.setTextSize(round * .8f);
            canvas.drawText(String.valueOf((int) Math.ceil(left)), x, y + round * .16f, text);
            text.setTextSize(round * .34f);
            canvas.drawText("on", x, y + round * .58f, text);
        } else if (waiting) {
            button(canvas, colourButton, false, 0xFF9AA0A6);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF2F3336 : 0xFFDDE0E2);
            canvas.drawCircle(x, y, round - dp(2), paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(70);
            pieceIn(canvas, two, RIBBON_ART + first - 1, x, y - round * .26f, round * .7f, 0);
            paint.setAlpha(255);
            stroke.setColor(colour);
            stroke.setStrokeWidth(dp(3.5f));
            canvas.drawArc(box, -90, 360 * Math.max(0, Math.min(1, 1 - until / 10f)), false, stroke);
            text.setColor(soft);
            text.setTextSize(round * .34f);
            canvas.drawText("wait " + (int) Math.ceil(until) + " s", x, y + round * .58f, text);
        } else {
            float beat = .5f + .5f * (float) Math.sin(clock * 5);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colour);
            paint.setAlpha(Math.round(50 + 60 * beat));
            canvas.drawCircle(x, y, round + dp(3) + dp(3) * beat, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            button(canvas, colourButton, false, colour);
            if (count == 1) {
                pieceIn(canvas, two, RIBBON_ART + first - 1, x, y, round * 1.2f, 0);
            } else {
                // A mix: the ribbons of what is in it, small, round the middle; of all seven, the white one.
                if (count == 6) pieceIn(canvas, two, RIBBON_ART + PlusRun.WHITE - 1, x, y, round * 1.2f, 0);
                int at = 0;
                for (int each = PlusRun.RED; each <= PlusRun.BLACK && count < 6; each++) {
                    if ((mix & 1 << each - 1) == 0) continue;
                    double turn = -Math.PI / 2 + at++ * Math.PI * 2 / count;
                    float far = count == 2 ? round * .3f : round * .38f;
                    pieceIn(canvas, two, RIBBON_ART + each - 1, x + (float) Math.cos(turn) * far, y + (float) Math.sin(turn) * far, round * (count <= 3 ? .72f : .56f), 0);
                }
            }
            stroke.setColor(colour);
            stroke.setStrokeWidth(dp(3.5f));
            canvas.drawArc(box, -90, 360, false, stroke);
        }
        canvas.restore();
        // A dot on its rim for each colour of the mix, and for anything else she is wearing: its colour if it is on, grey with a rim of its colour if it waits.
        int shown = mix | run.mix(), dots = Integer.bitCount(shown), dot = 0;
        for (int each = PlusRun.RED; each <= PlusRun.BLACK && dots > 1; each++) {
            if ((shown & 1 << each - 1) == 0) continue;
            double turn = Math.toRadians(-90 + (dot - (dots - 1) / 2f) * 24);
            float at = x + (float) Math.cos(turn) * (round + dp(1)), high = y + (float) Math.sin(turn) * (round + dp(1));
            boolean lit = run.on(each) > 0, late = !lit && run.untilCall(each) > 0;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(late ? FORM_COLOURS[each - 1] : Color.WHITE);
            canvas.drawCircle(at, high, dp(5.5f), paint);
            paint.setColor(late ? 0xFFB9BEC2 : FORM_COLOURS[each - 1]);
            canvas.drawCircle(at, high, dp(lit ? 4.4f : 3.4f), paint);
            paint.setColor(Color.BLACK);
            dot++;
        }
        if (fanOpen) {
            drawPicker(canvas);
            return;
        }
        // The palette, when there is more than one colour to choose from: three dabs of colour, hers, on a board.
        if (Integer.bitCount(owned) > 1) {
            button(canvas, nextButton, false, CalculatorLayout.CORAL);
            float px = nextButton.centerX(), py = nextButton.centerY(), size = nextButton.width() * .5f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF4A4F54 : 0xFFEADFCF);
            canvas.drawOval(px - size * .86f, py - size * .7f, px + size * .86f, py + size * .7f, paint);
            int dab = 0;
            for (int each = PlusRun.RED; each <= PlusRun.BLACK && dab < 4; each++) {
                if ((owned & 1 << each - 1) == 0) continue;
                double turn = Math.PI * (1.1 + dab * .27);
                paint.setColor(FORM_COLOURS[each - 1]);
                canvas.drawCircle(px + (float) Math.cos(turn) * size * .5f, py + (float) Math.sin(turn) * size * .36f + size * .05f, size * .17f, paint);
                dab++;
            }
            paint.setColor(cardColor);
            canvas.drawCircle(px + size * .36f, py + size * .26f, size * .15f, paint);
            paint.setColor(Color.BLACK);
        }
    }

    /**
     * The picker: a card over the buttons, in which the land waits. At its top, quick
     * choices, each a whole mix in one tap: one colour at a time; as many as she can wear;
     * and each mix that has a name, that she has made before, and that she owns the
     * colours of and can hold. Under them, every colour that is hers, each as it stands:
     * on, with a ring of its time and the seconds; waiting, grey, with its seconds; or
     * ready. One that is ticked has a gold ring and a tick. Under those, how many are
     * ticked of how many she may wear, and what they would make, if that has a name; and
     * the button that wears them.
     */
    private void drawPicker(Canvas canvas) {
        int owned = perks.colours & 127, count = Integer.bitCount(owned), made = session.tally("run_mixes");
        canvas.drawColor(0x66101018);
        float wide = Math.min(safe.width() - dp(16), dp(400)), pad = dp(12), inner = wide - pad * 2;
        float cell = Math.min(dp(52), (inner - (count - 1) * dp(6)) / count), gap = dp(6);
        // The quick choices, flowed into as many rows as they need.
        chips.clear();
        chipMixes.clear();
        List<String> names = new ArrayList<>();
        // (Once it has been won: the white that is truly hers, first among them.)
        if (run.canMaster()) {
            names.add("White, and hers");
            chipMixes.add(TRUE_WHITE);
        }
        names.add("One at a time");
        chipMixes.add(ONE_AT_A_TIME);
        if (run.together() > 1) {
            names.add("All I can wear");
            chipMixes.add(ALL_I_CAN);
        }
        for (int which = PlusRun.STEAMS; which <= PlusRun.WHITEN; which++) {
            int of = PlusRun.mixOf(which), needs = which == PlusRun.PRISM ? 3 : Integer.bitCount(of);
            if ((made & 1 << which) == 0 || (of & owned) != of || needs > run.together() || which == PlusRun.PRISM && count < 3) continue;
            names.add(MIX_NAMES[which]);
            chipMixes.add(which);
        }
        text.setTextSize(dp(13));
        float chipHigh = dp(30), across = 0;
        int rows = 1;
        float[] widths = new float[names.size()];
        for (int chip = 0; chip < names.size(); chip++) {
            widths[chip] = text.measureText(names.get(chip)) + dp(22);
            if (across > 0 && across + widths[chip] > inner) {
                rows++;
                across = 0;
            }
            across += widths[chip] + dp(6);
        }
        float high = pad + rows * (chipHigh + dp(6)) + dp(4) + cell + dp(10) + dp(40) + pad;
        float left = Math.max(safe.left + dp(8), Math.min(safe.right - dp(8) - wide, colourButton.centerX() - wide / 2)), bottom = colourButton.top - dp(10);
        // Never over her: where the card would lie across her, it lies over her head instead, if there is room for it there.
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herFeet = (run.y() + PlusRun.TALL - cameraY) * tile, herTop = herFeet - tile * 1.7f;
        if (herX > left - dp(12) && herX < left + wide + dp(12) && herFeet > bottom - high && herTop - dp(12) - high > safe.top + dp(60)) bottom = herTop - dp(12);
        float top = bottom - high;
        pickerCard.set(left, top, left + wide, bottom);
        plate(canvas, pickerCard, dp(22), 250);
        float at = left + pad, down = top + pad;
        for (int chip = 0; chip < names.size(); chip++) {
            if (at > left + pad && at + widths[chip] > left + pad + inner) {
                at = left + pad;
                down += chipHigh + dp(6);
            }
            RectF where = new RectF(at, down, at + widths[chip], down + chipHigh);
            chips.add(where);
            int which = chipMixes.get(chip);
            boolean lit = which == ONE_AT_A_TIME ? oneAtATime : which == ALL_I_CAN || which == TRUE_WHITE ? false : !oneAtATime && which != PlusRun.PRISM && ticks == PlusRun.mixOf(which);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(which == TRUE_WHITE ? 0xFFE9C96A : lit ? CalculatorLayout.CORAL : dark ? 0xFF3A3F45 : 0xFFF0ECE6);
            canvas.drawRoundRect(where, chipHigh / 2, chipHigh / 2, paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(which == TRUE_WHITE ? 0xFF3A2E12 : lit ? Color.WHITE : ink);
            text.setTextSize(dp(13));
            canvas.drawText(names.get(chip), where.centerX(), where.centerY() + dp(4.5f), text);
            at += widths[chip] + dp(6);
        }
        down += chipHigh + dp(10);
        // The colours.
        float row = count * cell + (count - 1) * gap, from = left + (wide - row) / 2;
        int index = 0;
        for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) {
            if ((owned & 1 << colour - 1) == 0) continue;
            if (fanCells[colour] == null) fanCells[colour] = new RectF();
            RectF where = fanCells[colour];
            where.set(from + index * (cell + gap), down, from + index * (cell + gap) + cell, down + cell);
            index++;
            float x = where.centerX(), y = where.centerY(), round = cell / 2;
            boolean on = run.on(colour) > 0, waiting = !on && run.untilCall(colour) > 0, ticked = (ticks & 1 << colour - 1) != 0;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(waiting ? (dark ? 0xFF2F3336 : 0xFFDDE0E2) : on ? FORM_COLOURS[colour - 1] : cardColor);
            canvas.drawCircle(x, y, round - dp(2), paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(waiting ? 70 : 255);
            if (!on) pieceIn(canvas, two, RIBBON_ART + colour - 1, x, y - (waiting ? round * .2f : 0), round * (waiting ? .8f : 1.15f), 0);
            paint.setAlpha(255);
            stroke.setColor(waiting ? 0xFF9AA0A6 : FORM_COLOURS[colour - 1]);
            stroke.setStrokeWidth(dp(2.5f));
            box.set(x - round + dp(2), y - round + dp(2), x + round - dp(2), y + round - dp(2));
            canvas.drawArc(box, -90, 360 * (on ? Math.min(1, run.on(colour) / Math.max(.1f, run.lasted(colour))) : waiting ? run.charge(colour) : 1), false, stroke);
            text.setTextAlign(Paint.Align.CENTER);
            if (on) {
                text.setColor(colour == PlusRun.YELLOW ? 0xFF3A3F45 : Color.WHITE);
                text.setTextSize(round * .8f);
                canvas.drawText(String.valueOf((int) Math.ceil(run.on(colour))), x, y + round * .28f, text);
            } else if (waiting) {
                text.setColor(soft);
                text.setTextSize(round * .42f);
                canvas.drawText((int) Math.ceil(run.untilCall(colour)) + " s", x, y + round * .68f, text);
            }
            if (ticked) {
                stroke.setColor(0xFFE0A21A);
                stroke.setStrokeWidth(dp(4));
                canvas.drawCircle(x, y, round, stroke);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFE0A21A);
                canvas.drawCircle(x + round * .68f, y - round * .68f, dp(8.5f), paint);
                paint.setColor(Color.BLACK);
                stroke.setColor(Color.WHITE);
                stroke.setStrokeWidth(dp(2));
                float cx = x + round * .68f, cy = y - round * .68f;
                canvas.drawLine(cx - dp(4), cy, cx - dp(1), cy + dp(3), stroke);
                canvas.drawLine(cx - dp(1), cy + dp(3), cx + dp(4), cy - dp(3), stroke);
            }
        }
        down += cell + dp(10);
        // What that would make, and the button that wears it.
        int named = PlusRun.naming(ticks), picked = Integer.bitCount(ticks);
        float button = dp(86);
        wearButton.set(left + wide - pad - button, down, left + wide - pad, down + dp(40));
        boolean any = picked > 0;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(any ? CalculatorLayout.CORAL : dark ? 0xFF3A3F45 : 0xFFE4E6E8);
        canvas.drawRoundRect(wearButton, dp(20), dp(20), paint);
        paint.setColor(Color.BLACK);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(any ? Color.WHITE : soft);
        text.setTextSize(dp(16));
        canvas.drawText("Wear", wearButton.centerX(), wearButton.centerY() + dp(5.5f), text);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        float room = wide - pad * 2 - button - dp(10);
        fitted(canvas, named >= 0 ? MIX_NAMES[named] + "  ·  " + picked + " of " + run.together() : picked + " of " + run.together() + " she may wear at once", left + pad,
                down + dp(16), dp(14.5f), room);
        text.setColor(soft);
        fitted(canvas, named >= 0 ? capital(MIX_DOES[named]) : oneAtATime ? "Tap a colour: a tap on the ribbon calls it" : "Tap colours on and off, then Wear", left + pad, down + dp(34),
                dp(12), room);
    }

    private void drawStory(Canvas canvas) {
        canvas.drawColor(0x33000000);
        String[] words = telling[line];
        int who = RunStory.speaker(words);
        float face = dp(30), pad = dp(14);
        float wide = Math.min(safe.width() - dp(24), dp(470));
        text.setTextSize(dp(16));
        List<String> lines = wrap(who == 0 ? names.named(words[1]) : words[1], wide - pad * 2 - face * 2 - dp(10));
        float tall = Math.max(pad + dp(18) + lines.size() * dp(22) + dp(22) + pad * .6f, face * 2 + pad * 2);
        box.set(safe.centerX() - wide / 2, safe.bottom - dp(20) - tall, safe.centerX() + wide / 2, safe.bottom - dp(20));
        float left = box.left, top = box.top, right = box.right, bottom = box.bottom;
        plate(canvas, box, dp(20), 245);
        round(canvas, who, left + pad + face, top + pad + face, face);
        float from = left + pad + face * 2 + dp(10);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(dark && who == 5 ? 0xFFD98A9E : SPEAKER_COLOURS[who]);
        text.setTextSize(dp(13.5f));
        canvas.drawText(RunStory.NAMES[who], from, top + pad + dp(11), text);
        text.setColor(soft);
        text.setTextSize(dp(11.5f));
        text.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(telling == RunStory.PROLOGUE ? RunStory.TITLE : RunStory.CHAPTERS[storyChapter], right - pad, top + pad + dp(11), text);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(16));
        for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), from, top + pad + dp(18) + dp(22) * (index + .74f), text);
        text.setColor(soft);
        text.setTextSize(dp(11.5f));
        text.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Tap  ›  " + (line + 1) + " / " + telling.length, right - pad, bottom - dp(9), text);
    }

    /** The heading of the map and of the counter: a name, a line under it, and the round button that leaves. */
    /** The guild counter's picture: Halgakos sitting in a shopping cart, as she was drawn, in a square about a place. */
    private Bitmap cartArt;

    private void cart(Canvas canvas, float x, float y, float size) {
        if (cartArt == null) cartArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_cart);
        paint.setAlpha(255);
        target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        canvas.drawBitmap(cartArt, null, target, paint);
    }

    private void heading(Canvas canvas, String title, String under) {
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(26));
        canvas.drawText(title, safe.left + dp(18), safe.top + dp(42), text);
        text.setColor(soft);
        fitted(canvas, under, safe.left + dp(18), safe.top + dp(66), dp(14.5f), mapShort && screen == MAP ? tabOf(0).left - safe.left - dp(30) : safe.width() - dp(36));
        plate(canvas, closeButton, closeButton.height() / 2, 235);
        stroke.setColor(ink);
        stroke.setStrokeWidth(dp(2));
        float arm = dp(6.5f);
        canvas.drawLine(closeButton.centerX() - arm, closeButton.centerY() - arm, closeButton.centerX() + arm, closeButton.centerY() + arm, stroke);
        canvas.drawLine(closeButton.centerX() + arm, closeButton.centerY() - arm, closeButton.centerX() - arm, closeButton.centerY() + arm, stroke);
    }

    private void drawMap(Canvas canvas) {
        drawMapItself(canvas);
        if (!warping) return;
        // Over the lands opened from a gate: which of them have gates she has found, lit; and what choosing does.
        for (int index = firstOf(page); index < lastOf(page); index++) {
            if (starsOf(index + 1) <= 0 || index + 1 >= RunLevels.OPEN_TO) continue;
            float beat = .5f + .5f * (float) Math.sin(clock * 3 + index * .4f);
            stroke.setColor(0xFF8E6BFF);
            stroke.setAlpha(Math.round(150 + 100 * beat));
            stroke.setStrokeWidth(dp(2.5f));
            canvas.drawRoundRect(lands[index].left - dp(3), lands[index].top - dp(3), lands[index].right + dp(3), lands[index].bottom + dp(3), dp(10), dp(10), stroke);
            stroke.setAlpha(255);
        }
        float wide = Math.min(safe.width() - dp(24), dp(420));
        box.set(safe.centerX() - wide / 2, safe.bottom - dp(84), safe.centerX() + wide / 2, safe.bottom - dp(16));
        plate(canvas, box, dp(18), 250);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(0xFF5B4BC4);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, "The gate is a teleporter", box.centerX(), box.top + dp(26), dp(15), wide - dp(24));
        text.setTypeface(Typeface.DEFAULT);
        text.setColor(soft);
        fitted(canvas, "A lit land: to its gate  ·  any other open land: to its start  ·  ✕ back", box.centerX(), box.top + dp(50), dp(12), wide - dp(24));
        text.setTextAlign(Paint.Align.LEFT);
    }

    private void drawMapItself(Canvas canvas) {
        int open = open();
        drawSky(canvas, RunLevels.chapterOf(open), clock * dp(5));
        canvas.drawColor(dark ? 0x99000000 : 0x66FFFFFF);
        int earned = 0;
        int all = lastOf(volumes() - 1);
        for (int land = 1; land <= all; land++) earned += starsOf(land);
        int guild = RunHunts.rank(session.tally("run_hunts"));
        heading(canvas, "Plus run", RunStory.TITLE + "  ·  Lv " + herLevel() + "  ·  " + earned + " of " + all * 3 + " stars" + (guild > 0 ? "  ·  " + RunHunts.RANK_NAMES[guild - 1] + " badge" : "")
                + (session.tally("run_sash") != 0 ? "  ·  black sash" : "") + (dungeonsCleared() > 0 ? "  ·  " + dungeonsCleared() + " of " + RunDungeons.COUNT + " dungeons" : ""));
        if (guild > 0) {
            // The badge of her rank in the guild, kept: by the name, where there is room for it.
            text.setTextSize(dp(26));
            float after = safe.left + dp(18) + text.measureText("Plus run") + dp(20);
            if (after + dp(14) < shopButton.left) badge(canvas, after, safe.top + dp(29), dp(22), guild);
        }
        if (open > RunLevels.COUNT) {
            int volumes = volumes();
            for (int volume = 0; volume < volumes; volume++) {
                RectF tab = tabOf(volume);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(volume == page ? CalculatorLayout.CORAL : dark ? 0xFF2A2E33 : 0xFFFFFFFF);
                paint.setAlpha(volume == page ? 255 : 220);
                canvas.drawRoundRect(tab, tab.height() / 2, tab.height() / 2, paint);
                paint.setAlpha(255);
                paint.setColor(Color.BLACK);
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(volume == page ? Color.WHITE : ink);
                // (Three of them have room only for the short of it.)
                fitted(canvas, volumes > 2 || tab.width() < dp(100) ? "Vol. " + (volume + 1) : volume == 0 ? "Volume One" : RunStory.SEQUEL, tab.centerX(), tab.centerY() + dp(5), dp(14), tab.width() - dp(16));
            }
        }
        // Its sounds on or off.
        plate(canvas, soundButton, soundButton.height() / 2, 235);
        float x = soundButton.centerX(), y = soundButton.centerY();
        path.reset();
        path.moveTo(x - dp(9), y - dp(3.5f));
        path.lineTo(x - dp(4.5f), y - dp(3.5f));
        path.lineTo(x + dp(1), y - dp(8));
        path.lineTo(x + dp(1), y + dp(8));
        path.lineTo(x - dp(4.5f), y + dp(3.5f));
        path.lineTo(x - dp(9), y + dp(3.5f));
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ink);
        canvas.drawPath(path, paint);
        stroke.setColor(ink);
        stroke.setStrokeWidth(dp(2));
        if (audio.on()) {
            box.set(x - dp(3), y - dp(6), x + dp(9), y + dp(6));
            canvas.drawArc(box, -50, 100, false, stroke);
        } else {
            canvas.drawLine(x + dp(4), y - dp(4.5f), x + dp(10), y + dp(4.5f), stroke);
            canvas.drawLine(x + dp(10), y - dp(4.5f), x + dp(4), y + dp(4.5f), stroke);
        }
        int firstShown = firstOf(page), lastShown = lastOf(page);
        int each = eachOf(page);
        for (int index = firstShown; index < lastShown; index++) {
            RectF where = lands[index];
            int land = index + 1, got = starsOf(land);
            if ((index - firstShown) % each == 0) {
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(land <= open ? ink : soft);
                text.setTextSize(dp(14));
                // (As wide as its row of lands, and no wider: in landscape another chapter stands beside it.)
                float block = lands[Math.min(lastShown - 1, index + mapPerRow[page] - 1)].right - where.left;
                text.setTextSize(dp(14));
                if (text.measureText(RunStory.CHAPTERS[RunLevels.chapterOf(land)]) > block) text.setTextSize(dp(14) * block / text.measureText(RunStory.CHAPTERS[RunLevels.chapterOf(land)]));
                canvas.drawText(RunStory.CHAPTERS[RunLevels.chapterOf(land)], where.left, where.top - dp(8), text);
                // The levels of its lands: how tough they are.
                float named = text.measureText(RunStory.CHAPTERS[RunLevels.chapterOf(land)]);
                // (The levels she would be in them, having played her way there: what her level is measured against.)
                int low = RunLoot.recommended(land), high = RunLoot.recommended(Math.min(RunLevels.TOTAL, land + each - 1));
                text.setColor(soft);
                text.setTextSize(dp(11.5f));
                String levels = low == high ? "level " + low : "levels " + low + " to " + high;
                if (named + dp(26) + text.measureText(levels) <= block) canvas.drawText(levels, where.left + named + dp(26), where.top - dp(8), text);
                text.setColor(land <= open ? ink : soft);
                text.setTextSize(dp(14));
                if (session.tally("run_medal_" + RunLevels.chapterOf(land)) != 0) {
                    paint.setAlpha(255);
                    art(canvas, STAR_ART, where.left + text.measureText(RunStory.CHAPTERS[RunLevels.chapterOf(land)]) + dp(12), where.top - dp(4), dp(16), false);
                }
            }
            boolean reached = land <= open;
            plate(canvas, where, dp(13), reached ? 245 : 110);
            if (land == open && got == 0) {
                stroke.setColor(CalculatorLayout.CORAL);
                stroke.setStrokeWidth(dp(2.5f));
                canvas.drawRoundRect(where, dp(13), dp(13), stroke);
            }
            // The lands where someone waits at the end are marked in her colour.
            int boss = RunLevels.bossOf(land);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(boss >= 0 ? waitColour(boss) : reached ? ink : soft);
            text.setAlpha(reached ? 255 : 130);
            text.setTextSize(where.width() * .38f);
            canvas.drawText(String.valueOf(land), where.centerX(), where.centerY() + where.width() * (reached ? .02f : .13f), text);
            text.setAlpha(255);
            int prize = RunLevels.prizeOf(land);
            if (prize != RunLevels.NOTHING && session.tally("run_prize_" + land) == 0) {
                paint.setAlpha(reached ? 255 : 150);
                prizePicture(canvas, prize, where.left + where.width() * .12f, where.top + where.width() * .12f, where.width() * .44f);
                paint.setAlpha(255);
            }
            if (reached) {
                float star = where.width() * .26f;
                for (int count = 0; count < 3; count++) {
                    paint.setAlpha(count < got ? 255 : 50);
                    art(canvas, STAR_ART, where.centerX() + (count - 1) * star, where.bottom - where.width() * .1f, star, false);
                }
                paint.setAlpha(255);
            }
        }
        // A scroll in the corner of each land where what was asked of her has been done.
        for (int index = firstShown; index < lastShown; index++) {
            if (session.tally("run_quest_" + (index + 1)) == 0) continue;
            RectF where = lands[index];
            paint.setAlpha(255);
            pieceIn(canvas, four, SCROLL_PICTURE, where.right - where.width() * .1f, where.top + where.width() * .1f, where.width() * .42f, 0);
        }
        // A hideout found: a crack in the corner of its land; its hidden boss beaten: a gold star there instead.
        for (int index = firstShown; index < lastShown; index++) {
            if (session.tally("run_hideout_" + (index + 1)) == 0) continue;
            RectF where = lands[index];
            boolean beaten = session.tally("run_hidden_" + (index + 1)) != 0;
            paint.setAlpha(255);
            if (beaten) artIn(canvas, STAR_ART, where.left + where.width() * .12f, where.top + where.width() * .12f, where.width() * .36f);
            else pieceIn(canvas, four, CRACK_PICTURE, where.left + where.width() * .12f, where.top + where.width() * .12f, where.width() * .34f, 0);
        }
        // A land with a dungeon: its stairway in the other corner; cleared, with a star by it.
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) {
            int index = RunDungeons.land(dungeon) - 1;
            if (index < firstShown || index >= lastShown || index + 1 > open()) continue;
            RectF where = lands[index];
            stairsIn(canvas, where.right - where.width() * .15f, where.top + where.width() * .15f, where.width() * .3f);
            if ((session.tally("run_dungeons") >> dungeon & 1) != 0) artIn(canvas, STAR_ART, where.right - where.width() * .07f, where.top + where.width() * .27f, where.width() * .2f);
        }
        // Under the lands, on with the furthest land she has reached: the button a thumb finds first.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33000000);
        float round = goButton.height() / 2;
        canvas.drawRoundRect(goButton.left, goButton.top + dp(2), goButton.right, goButton.bottom + dp(3), round, round, paint);
        paint.setColor(CalculatorLayout.CORAL);
        canvas.drawRoundRect(goButton, round, round, paint);
        paint.setColor(Color.BLACK);
        text.setColor(Color.WHITE);
        text.setTextAlign(Paint.Align.RIGHT);
        text.setTextSize(dp(16));
        String next = "Land " + open + "  ›";
        canvas.drawText(next, goButton.right - dp(18), goButton.centerY() + dp(5.5f), text);
        text.setTextAlign(Paint.Align.LEFT);
        fitted(canvas, open == 1 && starsOf(1) == 0 ? "Start" : starsOf(open) > 0 ? "Play again" : "Continue", goButton.left + dp(20), goButton.centerY() + dp(5.5f), dp(16),
                goButton.width() - dp(52) - text.measureText(next));
        // The counter, where her pluses buy what she keeps: up beside the sound, with what she has to spend under its name, and a dot
        // on its corner when she has enough for something.
        boolean enough = false;
        for (int which : UP_ORDER) {
            if (bought(which) < UP_COSTS[which].length && locked(which) == null && bank() >= UP_COSTS[which][bought(which)]) enough = true;
        }
        plate(canvas, shopButton, shopButton.height() / 2, 235);
        // Its own picture: Halgakos in her cart, at its start; its words beside her.
        float cartSize = shopButton.height() * .9f, wordsAt = shopButton.left + cartSize + dp(4), wordsRoom = shopButton.right - dp(10) - wordsAt;
        cart(canvas, shopButton.left + dp(4) + cartSize / 2, shopButton.centerY(), cartSize);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(ink);
        fitted(canvas, wordsRoom < dp(86) ? "Counter" : "Guild counter", wordsAt + wordsRoom / 2, shopButton.top + dp(15.5f), dp(12), wordsRoom);
        text.setColor(CalculatorLayout.CORAL);
        fitted(canvas, "+ " + bank() + "  ›", wordsAt + wordsRoom / 2, shopButton.bottom - dp(6.5f), dp(13), wordsRoom);
        if (enough) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(CalculatorLayout.CORAL);
            canvas.drawCircle(shopButton.right - dp(7), shopButton.top + dp(5), dp(5), paint);
            paint.setColor(Color.BLACK);
        }
        // Her backpack: at the end of the row of the volumes, with what she begins every land with on it.
        plate(canvas, gearButton, gearButton.height() / 2, 235);
        paint.setAlpha(255);
        thing(canvas, kept().main, gearButton.left + dp(17), gearButton.centerY(), dp(20), 0, false, false);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        fitted(canvas, "Gear", gearButton.left + dp(32), gearButton.centerY() + dp(5), dp(13.5f), gearButton.width() - dp(40));
        // The gallery: her face as she cringes, and the word for it; and a dot on its corner, as on the counter, while something in
        // it has opened since it was last looked into.
        plate(canvas, cringeButton, dp(16), 235);
        if (cringe == null) cringe = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_cringe);
        boolean worded = cringeButton.height() >= dp(64);
        float face = Math.min(cringeButton.width() - dp(8), cringeButton.height() - dp(worded ? 22 : 8));
        box.set(cringeButton.centerX() - face / 2, cringeButton.top + dp(4), cringeButton.centerX() + face / 2, cringeButton.top + dp(4) + face);
        paint.setAlpha(255);
        paint.setFilterBitmap(true);
        if (cringe != null) canvas.drawBitmap(cringe, null, box, paint);
        if (worded) {
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(ink);
            fitted(canvas, "Cringe", cringeButton.centerX(), cringeButton.bottom - dp(6.5f), dp(12), cringeButton.width() - dp(8));
        }
        if (RunGallery.open(gallery(), session) > session.tally(RunGallery.LOOKED)) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(CalculatorLayout.CORAL);
            canvas.drawCircle(cringeButton.right - dp(7), cringeButton.top + dp(6), dp(5), paint);
            paint.setColor(Color.BLACK);
        }
        text.setTextAlign(Paint.Align.LEFT);
        // Her skill tree, beside her backpack: its seed, and a dot while there are points to spend.
        plate(canvas, skillButton, skillButton.height() / 2, 235);
        tree().icon(canvas, RunSkills.SEED_ICON, skillButton.centerX(), skillButton.centerY(), skillButton.height() * 1.02f, 255);
        if (skillsLeft() > 0) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(CalculatorLayout.CORAL);
            canvas.drawCircle(skillButton.right - dp(7), skillButton.top + dp(6), dp(5), paint);
            paint.setColor(Color.BLACK);
        }
        text.setTextAlign(Paint.Align.LEFT);
        // Her, under it all, with a word about what is next.
        float tall = Math.min(dp(96), safe.bottom - goButton.bottom - dp(20));
        if (tall > dp(40)) {
            float feet = safe.bottom - dp(14), middle = safe.left + dp(26) + tall * .45f;
            paint.setAlpha(255);
            drawHer(canvas, middle, feet, tall, STAND, false);
            int waiting = RunLevels.bossOf(open);
            String afford = null;
            for (int which : UP_ORDER) {
                if (afford == null && bought(which) < UP_COSTS[which].length && locked(which) == null && bank() >= UP_COSTS[which][bought(which)]) afford = UP_NAMES[which];
            }
            if (bought(1) == 0 && bank() >= UP_COSTS[1][0]) afford = UP_NAMES[1];
            String words = names.named(afford != null && open > 1 ? "Pluses enough for " + afford.toLowerCase(java.util.Locale.ROOT).replace("chikuwa", "Chikuwa") + ". The counter Sloppy~♡"
                    : starsOf(RunLevels.OPEN_TO) > 0 ? "A hundred lands, read to the end Sloppy~♡"
                    : open == RunLevels.SECOND + 1 ? "Volume Three fell on us. Land 81 Sloppy~♡"
                    : open == 1 ? "Pick the first land. Go on Sloppy~♡" : open == RunLevels.COUNT + 1 ? "Volume Two fell on us. Land 31 Sloppy~♡"
                    : waiting == RunLevels.TSUBAKIEH ? "Tsubakieh and her bat are in land " + open + " Sloppy~♡"
                    : waiting == RunLevels.ZERO ? "The Last Page. Zero is waiting Sloppy~♡"
                    : waiting >= 0 && session.tally("run_prize_" + open) == 0
                    ? waitName(waiting) + (waiting == RunLevels.MIRI ? " have " : " has ") + prizeName(RunLevels.prizeOf(open)).toLowerCase(java.util.Locale.ROOT).replace("zero", "Zero")
                    + " to win, in land " + open + " Sloppy~♡"
                    : waiting >= 0 ? waitName(waiting) + (waiting == RunLevels.MIRI ? " are" : " is") + " waiting in land " + open + " Sloppy~♡"
                    : "Land " + open + " is waiting Sloppy~♡");
            // (For a while after something has opened in the gallery, what she says is that.)
            if (galleryShown > 0 && galleryWords != null) words = names.named(galleryWords[1]);
            text.setTextSize(dp(14));
            text.setTextAlign(Paint.Align.LEFT);
            // Her words go onto as many lines as it takes to fit them beside her at a size that can be read: they are never made small.
            float room = cringeButton.left - dp(10) - middle - tall * .5f - dp(24);
            List<String> said = wrap(words, room);
            float wide = 0;
            for (String one : said) wide = Math.max(wide, Math.min(room, text.measureText(one)));
            float bubbleTall = dp(17) + said.size() * dp(19);
            box.set(middle + tall * .5f, feet - tall * .62f - (said.size() - 1) * dp(9.5f), middle + tall * .5f + wide + dp(24), feet - tall * .62f - (said.size() - 1) * dp(9.5f) + bubbleTall);
            float bubbleLeft = box.left, bubbleTop = box.top;
            plate(canvas, box, dp(14), 245);
            text.setColor(ink);
            for (int index = 0; index < said.size(); index++) fitted(canvas, said.get(index), bubbleLeft + dp(12), bubbleTop + dp(8.5f) + dp(19) * (index + .74f), dp(14), wide);
        }
    }

    /**
     * The counter, in tabs: her hearts and lives, how she moves, her colours, her comforts,
     * her pets, and what she fights with. Under the open tab: what her pluses buy there,
     * how far along each she is, and what the next step of it costs.
     */
    private void drawShop(Canvas canvas) {
        drawSky(canvas, RunLevels.chapterOf(open()), clock * dp(5));
        canvas.drawColor(dark ? 0xAA000000 : 0x77FFFFFF);
        int bank = bank();
        heading(canvas, "Guild counter", "What she buys here she keeps in every land  \u00B7  + " + bank + " to spend");
        // Its picture beside its name: Halgakos in her cart.
        text.setTextSize(dp(26));
        cart(canvas, safe.left + dp(18) + text.measureText("Guild counter") + dp(28), safe.top + dp(29), dp(44));
        // The tabs: one row; the open one is filled.
        // (The row is shared out by the length of their words, so that every word is the same size.)
        float across = Math.min(safe.width() - dp(28), dp(460)), from = safe.centerX() - across / 2, worded = 0, lettered = dp(12.5f);
        text.setTextSize(lettered);
        for (String name : TAB_NAMES) worded += text.measureText(name);
        float spare = (across - dp(3) * (TAB_NAMES.length - 1) - worded) / TAB_NAMES.length;
        if (spare < dp(8)) {
            lettered *= Math.max(.7f, (across - dp(3) * (TAB_NAMES.length - 1) - dp(8) * TAB_NAMES.length) / worded);
            text.setTextSize(lettered);
            worded = 0;
            for (String name : TAB_NAMES) worded += text.measureText(name);
            spare = (across - dp(3) * (TAB_NAMES.length - 1) - worded) / TAB_NAMES.length;
        }
        float next = from;
        for (int index = 0; index < TAB_NAMES.length; index++) {
            if (counterTabs[index] == null) counterTabs[index] = new RectF();
            RectF at = counterTabs[index];
            text.setTextSize(lettered);
            float tab = text.measureText(TAB_NAMES[index]) + spare;
            at.set(next, safe.top + dp(98), next + tab, safe.top + dp(132));
            next += tab + dp(3);
            boolean enough = false;
            for (int which : TAB_WARES[index]) enough |= bought(which) < UP_COSTS[which].length && locked(which) == null && bank >= UP_COSTS[which][bought(which)];
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(index == counterTab ? CalculatorLayout.CORAL : dark ? 0xFF2F3336 : 0xFFF1F3F4);
            canvas.drawRoundRect(at, dp(17), dp(17), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(index == counterTab ? Color.WHITE : ink);
            text.setTextSize(lettered);
            canvas.drawText(TAB_NAMES[index], at.centerX(), at.centerY() + lettered * .36f, text);
            if (enough && index != counterTab) {
                paint.setColor(CalculatorLayout.CORAL);
                canvas.drawCircle(at.right - dp(5), at.top + dp(5), dp(3.5f), paint);
                paint.setColor(Color.BLACK);
            }
        }
        int[] list = TAB_WARES[counterTab];
        shownWares = list.length;
        // Everything under the tabs scrolls when it is taller than the screen (a phone on its side): it is drawn moved up by the scroll, and cut off under the tabs.
        shopListTop = safe.top + dp(136);
        float listed = safe.top + dp(142) - shopScroll;
        canvas.save();
        canvas.clipRect(0, shopListTop, width, height);
        // (With her pets under them, the four that are for her pets are lower rows.)
        float squeeze = counterTab == PETS_TAB ? Math.min(wares[0].height(), dp(52)) : wares[0].height();
        // (A long list, as her moves are, is made to fit under the tabs, down to rows that still have room for what each gives; past that, it scrolls.)
        squeeze = Math.max(Math.min(squeeze, dp(52)), Math.min(squeeze, (safe.bottom - safe.top - dp(150)) / list.length - dp(6)));
        for (int row = 0; row < list.length; row++) {
            RectF where = wares[row];
            where.set(where.left, listed + row * (squeeze + dp(6)), where.right, listed + row * (squeeze + dp(6)) + squeeze);
            int index = list[row];
            float left = where.left, top = where.top, right = where.right, high = where.height(), middle = where.centerY();
            int has = bought(index), steps = UP_COSTS[index].length;
            String locked = locked(index);
            boolean done = has >= steps, can = !done && locked == null && bank >= UP_COSTS[index][has];
            plate(canvas, where, dp(16), 245);
            // Its picture: for her wings, herself at the top of a jump.
            float icon = Math.min(dp(46), high - dp(14));
            paint.setAlpha(255);
            if (UP_ART[index] >= 0) artIn(canvas, UP_ART[index], left + dp(14) + icon / 2, middle, icon);
            else if (UP_ART[index] == HER_FACE) round(canvas, 0, left + dp(14) + icon / 2, middle, icon * .46f);
            else if (UP_ART[index] >= MOVE_ART && UP_ART[index] <= MOVE_ART + PlusRun.MOVE_GLIDE) frame(canvas, movesSheet(), UP_ART[index] - MOVE_ART, PlusRun.MOVE_GLIDE + 1, left + dp(14) + icon / 2, middle + icon / 2, icon * .95f, false);
            else frame(canvas, jump, TOP, JUMP_FRAMES, left + dp(14) + icon / 2, middle + icon / 2, icon * .95f, false);
            float words = left + dp(24) + icon, price = dp(86), room = right - words - price - dp(18);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            boolean roomy = high > dp(60), two = high >= dp(46);
            fitted(canvas, UP_NAMES[index], words, roomy ? top + dp(25) : two ? middle - dp(2) : middle + dp(5), dp(roomy ? 16 : 14.5f), room);
            if (two) {
                text.setColor(soft);
                fitted(canvas, done ? "All of it is hers" : locked != null ? UP_GIVES[index][has] + ". " + locked : UP_GIVES[index][has], words, roomy ? top + dp(44) : middle + dp(15),
                        dp(roomy ? 12.5f : 11.5f), room);
            }
            if (roomy) {
                // How far along it she is: a dot for each step.
                for (int step = 0; step < steps; step++) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(step < has ? CalculatorLayout.CORAL : dark ? 0xFF4A4F54 : 0xFFD9DCDD);
                    canvas.drawCircle(words + dp(5) + step * dp(15), top + dp(60), dp(4.5f), paint);
                }
                paint.setColor(Color.BLACK);
            }
            // What the next step costs, in a pill: bright if she can pay it.
            box.set(right - dp(12) - price, middle - dp(18), right - dp(12), middle + dp(18));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(done ? (dark ? 0xFF2F3336 : 0xFFEDEEEE) : can ? CalculatorLayout.CORAL : dark ? 0xFF44292E : 0xFFFCE9EB);
            canvas.drawRoundRect(box, dp(18), dp(18), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(done ? soft : can ? Color.WHITE : CalculatorLayout.CORAL);
            text.setTextSize(dp(15));
            fitted(canvas, done ? "Owned" : locked != null ? "Locked" : "+ " + UP_COSTS[index][has], right - dp(12) - price / 2, middle + dp(5.5f), dp(15), price - dp(14));
        }
        float under = listed + list.length * (squeeze + dp(6)) + dp(6), left = safe.centerX() - across / 2, end = under;
        petAct.setEmpty();
        petTreat.setEmpty();
        armsAct.setEmpty();
        if (counterTab == PETS_TAB) end = drawPetShop(canvas, left, under, across, bank);
        else if (counterTab == COLOURS_TAB) end = drawMixHints(canvas, left, under, across);
        else if (counterTab == ARMS_TAB) {
            end = under + dp(116);
            // What she fights with is worked on by the smith, in the towns; here is the way to her backpack.
            box.set(left, under, left + across, under + dp(116));
            plate(canvas, box, dp(16), 245);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, "What she fights with", left + dp(16), under + dp(26), dp(16), across - dp(32));
            text.setColor(soft);
            fitted(canvas, "What she finds in the lands is kept in her inventory.", left + dp(16), under + dp(46), dp(12.5f), across - dp(32));
            fitted(canvas, "The smith, in the towns, will work on each of them.", left + dp(16), under + dp(63), dp(12.5f), across - dp(32));
            armsAct.set(left + dp(16), under + dp(74), left + across - dp(16), under + dp(108));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(CalculatorLayout.CORAL);
            canvas.drawRoundRect(armsAct, dp(17), dp(17), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.WHITE);
            fitted(canvas, "Open her inventory  \u203A", armsAct.centerX(), armsAct.centerY() + dp(5), dp(14.5f), armsAct.width() - dp(24));
        } else if (counterTab == VANITY_TAB) {
            end = drawVanityTab(canvas, left, under, across, bank);
        }
        canvas.restore();
        // How far it can scroll: as far as its end coming up to the bottom of the screen; and a thin bar at the side to show there is more.
        shopMax = Math.max(0, end + shopScroll + dp(12) - safe.bottom);
        shopScroll = Math.max(0, Math.min(shopMax, shopScroll));
        if (shopMax > 0) {
            float track = safe.bottom - dp(8) - (shopListTop + dp(6)), shown = track * track / (track + shopMax), at = shopListTop + dp(6) + (track - shown) * shopScroll / shopMax;
            float x = Math.min(width - dp(6), left + across + dp(10));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0x66FFFFFF : 0x55000000);
            box.set(x - dp(2), at, x + dp(2), at + shown);
            canvas.drawRoundRect(box, dp(2), dp(2), paint);
            paint.setColor(Color.BLACK);
        }
        text.setTextAlign(Paint.Align.LEFT);
    }

    /** The counter's scroll: how far down its list is moved, how far it can be, where its list begins, and a finger that may be dragging it. */
    private float shopScroll, shopMax, shopListTop, shopDownX, shopDownY, shopDownScroll;
    private boolean shopDragged;

    /** A tap at the counter: its close button, a tab (which opens at its top), or what is under the tabs, if it is where the list shows. */
    private void shopTap(float x, float y) {
        if (closeButton.contains(x, y)) {
            audio.play("tap", .5f);
            if (counterFrom == STALL) {
                regrant();
                screen = STALL;
            } else if (counterFrom == PLAY) {
                regrant();
                screen = PLAY;
            } else {
                toMap();
            }
            return;
        }
        for (int tab = 0; tab < TAB_NAMES.length; tab++) {
            if (counterTabs[tab] != null && counterTabs[tab].contains(x, y)) {
                counterTab = tab;
                shopScroll = 0;
                audio.play("page", .5f);
                return;
            }
        }
        if (y < shopListTop) return;
        for (int index = 0; index < shownWares; index++) if (wares[index].contains(x, y)) buy(TAB_WARES[counterTab][index]);
        if (counterTab == PETS_TAB) {
            for (int kind = 0; kind < PlusRun.PETS; kind++) {
                if (petCells[kind] != null && petCells[kind].contains(x, y)) {
                    petOn = kind;
                    audio.play("tap", .5f);
                }
            }
            if (!petAct.isEmpty() && petAct.contains(x, y)) petDeal();
            else if (!petTreat.isEmpty() && petTreat.contains(x, y)) petTrade();
        } else if (counterTab == ARMS_TAB && armsAct.contains(x, y)) {
            audio.play("tap", .5f);
            openPack(MAP);
        } else if (counterTab == VANITY_TAB) {
            vanityTouch(x, y);
        }
    }

    /** A touch at the counter: a finger dragged up or down scrolls it; one let go where it came down is a tap. */
    private void shopTouch(int action, float x, float y) {
        if (action == MotionEvent.ACTION_DOWN) {
            shopDownX = x;
            shopDownY = y;
            shopDownScroll = shopScroll;
            shopDragged = false;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!shopDragged && Math.abs(y - shopDownY) > dp(10)) shopDragged = true;
            if (shopDragged) shopScroll = Math.max(0, Math.min(shopMax, shopDownScroll - (y - shopDownY)));
        } else if (action == MotionEvent.ACTION_UP && !shopDragged) {
            shopTap(shopDownX, shopDownY);
        }
    }

    /** Vanity at the counter: the toggle's three settings, and a row for each outfit. */
    private final RectF[] vanityModes = {new RectF(), new RectF(), new RectF()}, vanityRows = new RectF[RunVanity.count()];

    private RunVanity.Keep vanityKeep() { return RunVanity.on(session); }

    private final RectF boutiqueAct = new RectF(), picturesAct = new RectF();
    private final RectF[] vanityParts = {new RectF(), new RectF()};
    private RunBoutique boutique;

    /** The boutique is laid over the game, with the game's voice and sounds lent to it. */
    private void openBoutique() { openBoutique(0); }

    private void openBoutique(int page) {
        if (boutique != null && !boutique.closed()) return;
        audio.hush();
        int[] edges = {Math.round(safe.left), Math.round(safe.top), Math.round(width - safe.right), Math.round(height - safe.bottom)};
        boutique = RunBoutique.over(stage, session, dark, edges, new RunBoutique.Host() {
            @Override public void say(String name) { audio.say(name); }

            @Override public boolean speaking() { return audio.speaking(); }

            @Override public void hush() { audio.hush(); }

            @Override public void play(String effect) { audio.play(effect, .5f); }

            @Override public void closed() { vanityAt = -1000; }
        });
        if (boutique != null) boutique.page(page);
    }

    private void vanityTouch(float x, float y) {
        for (int part = 0; part < 2; part++) {
            if (vanityParts[part].isEmpty() || !vanityParts[part].contains(x, y)) continue;
            boolean ears = part == 0;
            int now = ears ? RunVanity.ears(vanityKeep()) : RunVanity.horns(vanityKeep()), count = ears ? RunVanity.EARS.length : RunVanity.HORNS.length;
            // The next she has, round to her own.
            for (int step = 1; step <= count; step++) {
                int next = (now + step) % count;
                if (RunVanity.wearPart(vanityKeep(), ears, next)) break;
            }
            if (RunVanity.mode(vanityKeep()) != RunVanity.VANITY) RunVanity.setMode(vanityKeep(), RunVanity.VANITY);
            vanityAt = -1000;
            audio.play("star", .6f);
            return;
        }
        if (!picturesAct.isEmpty() && picturesAct.contains(x, y)) {
            audio.play("tap", .5f);
            openBoutique(2);
            return;
        }
        if (!boutiqueAct.isEmpty() && boutiqueAct.contains(x, y)) {
            audio.play("tap", .5f);
            openBoutique();
            return;
        }
        for (int mode = 0; mode < vanityModes.length; mode++) {
            if (vanityModes[mode].contains(x, y)) {
                RunVanity.setMode(vanityKeep(), mode);
                vanityAt = -1000;
                audio.play("tap", .5f);
                return;
            }
        }
        for (int outfit = 0; outfit < vanityRows.length; outfit++) {
            if (vanityRows[outfit] == null || !vanityRows[outfit].contains(x, y)) continue;
            if (RunVanity.owns(vanityKeep(), outfit)) {
                // Tapping the one she has on takes it off.
                RunVanity.choose(vanityKeep(), RunVanity.outfit(vanityKeep()) == outfit ? -1 : outfit);
                if (RunVanity.mode(vanityKeep()) == RunVanity.DEFAULT) RunVanity.setMode(vanityKeep(), RunVanity.VANITY);
                audio.play("star", .6f);
            } else if (RunVanity.QUEST[outfit] >= 0) {
                openBoutique();
                return;
            } else if (RunVanity.buy(vanityKeep(), outfit)) {
                RunVanity.setMode(vanityKeep(), RunVanity.VANITY);
                audio.play("buy", .6f);
                buzz(true);
            } else {
                audio.play("no", .5f);
            }
            vanityAt = -1000;
            return;
        }
    }

    private float drawVanityTab(Canvas canvas, float left, float top, float across, int bank) {
        int mode = RunVanity.mode(vanityKeep()), chosen = RunVanity.outfit(vanityKeep());
        float gap = dp(6), each = (across - gap * 2) / 3;
        for (int one = 0; one < 3; one++) {
            RectF at = vanityModes[one];
            at.set(left + one * (each + gap), top, left + one * (each + gap) + each, top + dp(34));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(one == mode ? CalculatorLayout.CORAL : dark ? 0xFF2F3336 : 0xFFF1F3F4);
            canvas.drawRoundRect(at, dp(17), dp(17), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(one == mode ? Color.WHITE : ink);
            fitted(canvas, RunVanity.MODE_NAMES[one], at.centerX(), at.centerY() + dp(5), dp(13.5f), each - dp(10));
        }
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(soft);
        fitted(canvas, RunVanity.MODE_DOES[mode] + "  \u00B7  it never changes what she can do", left + dp(6), top + dp(52), dp(12), across - dp(12));
        float y = top + dp(62);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(soft);
        for (int part = 0; part < 2; part++) {
            boolean ears = part == 0;
            int now = ears ? RunVanity.ears(vanityKeep()) : RunVanity.horns(vanityKeep());
            vanityParts[part].set(left, y, left + across, y + dp(40));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF2F3336 : 0xFFF1F3F4);
            canvas.drawRoundRect(vanityParts[part], dp(20), dp(20), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(soft);
            fitted(canvas, ears ? "Ears" : "Horns", left + dp(16), vanityParts[part].centerY() + dp(5), dp(13), dp(60));
            text.setColor(ink);
            fitted(canvas, (ears ? RunVanity.EARS[now] : RunVanity.HORNS[now]) + "  \u203A", left + dp(76), vanityParts[part].centerY() + dp(5), dp(14.5f), across - dp(92));
            y += dp(46);
        }
        float split = across * .62f;
        boutiqueAct.set(left, y, left + split, y + dp(36));
        picturesAct.set(left + split + dp(6), y, left + across, y + dp(36));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(CalculatorLayout.CORAL);
        canvas.drawRoundRect(boutiqueAct, dp(18), dp(18), paint);
        canvas.drawRoundRect(picturesAct, dp(18), dp(18), paint);
        paint.setColor(Color.BLACK);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(Color.WHITE);
        fitted(canvas, "Tulle & Thread, the boutique  \u203A", boutiqueAct.centerX(), boutiqueAct.centerY() + dp(5), dp(13.5f), boutiqueAct.width() - dp(20));
        fitted(canvas, "Pictures  \u203A", picturesAct.centerX(), picturesAct.centerY() + dp(5), dp(13.5f), picturesAct.width() - dp(16));
        y += dp(44);
        text.setTextAlign(Paint.Align.LEFT);
        // How she looks in it now: in the air, hurt, and with the sword in hand.
        float tall = dp(96), feet = y + tall;
        frame(canvas, herJump(), 1, JUMP_FRAMES, left + across * .2f, feet, tall, false);
        frame(canvas, herLose(0), HIT, LOSE_FRAMES, left + across * .5f, feet, tall, false);
        frame(canvas, herArmed(PlusRun.SWORD), 7, ARMED_FRAMES, left + across * .8f, feet, tall, false);
        y = feet + dp(8);
        for (int outfit = 0; outfit < vanityRows.length; outfit++) {
            if (vanityRows[outfit] == null) vanityRows[outfit] = new RectF();
            RectF at = vanityRows[outfit];
            if (!RunVanity.ready(outfit)) {
                at.setEmpty();
                continue;
            }
            at.set(left, y, left + across, y + dp(50));
            y += dp(56);
            boolean hers = RunVanity.owns(vanityKeep(), outfit), on = chosen == outfit, quest = RunVanity.QUEST[outfit] >= 0, can = !hers && !quest && bank >= RunVanity.PRICES[outfit];
            plate(canvas, at, dp(16), 245);
            RunVanity.Style style = RunVanity.STYLES[outfit];
            int[] swatch = {style.dark, style.mid, style.light, style.trim != 0 ? style.trim : style.mid};
            paint.setStyle(Paint.Style.FILL);
            for (int dot = 0; dot < 4 && style.art == null; dot++) {
                paint.setColor(swatch[dot]);
                canvas.drawCircle(at.left + dp(22), at.top + dp(10) + dot * dp(9.5f), dp(4f), paint);
            }
            paint.setColor(Color.BLACK);
            float words = at.left + dp(42), price = dp(86), room = at.right - words - price - dp(18);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, RunVanity.NAMES[outfit], words, at.top + dp(21), dp(14.5f), room);
            text.setColor(soft);
            fitted(canvas, RunVanity.DOES[outfit], words, at.top + dp(39), dp(10.5f), room);
            box.set(at.right - dp(12) - price, at.centerY() - dp(16), at.right - dp(12), at.centerY() + dp(16));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(on ? CalculatorLayout.CORAL : hers ? (dark ? 0xFF2F3336 : 0xFFEDEEEE) : can ? CalculatorLayout.CORAL : dark ? 0xFF44292E : 0xFFFCE9EB);
            canvas.drawRoundRect(box, dp(18), dp(18), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(on || can ? Color.WHITE : hers ? soft : CalculatorLayout.CORAL);
            fitted(canvas, on ? "Wearing" : hers ? "Wear" : quest ? "Quest" : "+ " + RunVanity.PRICES[outfit], box.centerX(), box.centerY() + dp(5.5f), dp(14), price - dp(14));
        }
        return y;
    }

    /**
     * Her pets at the counter: the kinds that can be had, in rows of five (those of the ones she has
     * beaten are not there at all until she has). One she has is bright,
     * one that comes along with her is ringed, and one she has not got has its price under
     * it. Under them, the one that is looked at: what it is, what it does, and the button
     * that buys it, takes it along or leaves it at home.
     */
    private float drawPetShop(Canvas canvas, float left, float top, float across, int bank) {
        int own = session.tally("run_pet_own"), with = session.tally("run_pet_with") & own, places = 1 + bought(PET_PLACES);
        float gap = dp(6), side = Math.min(dp(64), (across - gap * 4) / 5), high = side + dp(14);
        int shown = 0;
        for (int kind = 0; kind < PlusRun.PETS; kind++) {
            if (petCells[kind] == null) petCells[kind] = new RectF();
            RectF at = petCells[kind];
            if (!RunPets.open(kind, session)) {
                at.setEmpty();
                continue;
            }
            float x = left + (across - (side * 5 + gap * 4)) / 2 + shown % 5 * (side + gap), y = top + shown / 5 * (high + gap);
            shown++;
            at.set(x, y, x + side, y + high);
            boolean hers = (own >> kind & 1) != 0, comes = (with >> kind & 1) != 0;
            plate(canvas, at, dp(14), 245);
            if (kind == petOn || comes) {
                stroke.setColor(comes ? CalculatorLayout.CORAL : dark ? 0xFF9AA0A6 : 0xFF8A8F94);
                stroke.setStrokeWidth(dp(comes ? 2.5f : 1.5f));
                canvas.drawRoundRect(at, dp(14), dp(14), stroke);
            }
            paint.setAlpha(hers ? 255 : 120);
            cell(canvas, petArt(kind), hers && (int) (clock * 1.5f + kind) % 5 == 0 ? 5 : 0, 6, at.centerX(), at.top + side * .84f, side * 1.02f, false);
            paint.setAlpha(255);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(hers ? (comes ? CalculatorLayout.CORAL : soft) : ink);
            fitted(canvas, comes ? "with her" : hers ? "hers" : "+ " + PET_COSTS[kind], at.centerX(), at.bottom - dp(5), dp(11), side - dp(6));
        }
        // The one that is looked at.
        if (!RunPets.open(petOn, session)) petOn = 0;
        float y = top + (shown + 4) / 5 * (high + gap) + dp(4);
        boolean hers = (own >> petOn & 1) != 0, comes = (with >> petOn & 1) != 0, room = Integer.bitCount(with) < places;
        box.set(left, y, left + across, y + dp(118));
        plate(canvas, box, dp(16), 245);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, PET_NAMES[petOn], left + dp(16), y + dp(24), dp(15.5f), across - dp(32));
        text.setTypeface(Typeface.DEFAULT);
        text.setColor(soft);
        fitted(canvas, "It " + PET_DOES[petOn] + ". " + (PlusRun.floats(petOn) ? "It flies." : "It runs."), left + dp(16), y + dp(43), dp(12.5f), across - dp(32));
        fitted(canvas, "Hers for good, it comes into every land  \u00B7  " + Integer.bitCount(with) + " of " + places + (places == 1 ? " place" : " places") + " taken", left + dp(16), y + dp(60),
                dp(12), across - dp(32));
        // One she has not got is had for pluses, or for treats, which hunts pay: a button for each.
        int treats = session.tally("run_treats"), asks = PET_TREATS[petOn];
        petAct.set(left + dp(16), y + dp(72), left + across - dp(16) - (hers ? 0 : across * .44f), y + dp(108));
        if (!hers) {
            petTreat.set(petAct.right + dp(8), petAct.top, left + across - dp(16), petAct.bottom);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(treats >= asks ? 0xFF2E9E6E : dark ? 0xFF2F3336 : 0xFFEDEEEE);
            canvas.drawRoundRect(petTreat, dp(18), dp(18), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(treats >= asks ? Color.WHITE : soft);
            fitted(canvas, "or " + asks + " treats (has " + treats + ")", petTreat.centerX(), petTreat.centerY() + dp(5), dp(13.5f), petTreat.width() - dp(16));
        }
        boolean can = hers ? comes || room : bank >= PET_COSTS[petOn];
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(can ? (hers && comes ? (dark ? 0xFF44292E : 0xFFFCE9EB) : CalculatorLayout.CORAL) : dark ? 0xFF2F3336 : 0xFFEDEEEE);
        canvas.drawRoundRect(petAct, dp(18), dp(18), paint);
        paint.setColor(Color.BLACK);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(can ? (hers && comes ? CalculatorLayout.CORAL : Color.WHITE) : soft);
        fitted(canvas, !hers ? "Buy  + " + PET_COSTS[petOn] : comes ? "Leave at home" : room ? "Take along" : "No place free: buy one above", petAct.centerX(), petAct.centerY() + dp(5), dp(14.5f),
                petAct.width() - dp(24));
        return y + dp(118);
    }

    /** Under her colours: the mixes that have names. One she has made is named, with what it does; one she has not is hinted at: one of its colours, and a question mark. */
    private float drawMixHints(Canvas canvas, float left, float top, float across) {
        int made = session.tally("run_mixes");
        box.set(left, top, left + across, top + dp(30) + MIX_NAMES.length * dp(19) + dp(8));
        plate(canvas, box, dp(16), 245);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        fitted(canvas, "Mixes: colours worn together", left + dp(16), top + dp(24), dp(14.5f), across - dp(32));
        for (int mix = 0; mix < MIX_NAMES.length; mix++) {
            boolean known = (made >> mix & 1) != 0;
            String of = MIX_OF[mix];
            int plus = of.indexOf(" + ");
            String words = known ? MIX_NAMES[mix] + ": " + of + ": " + MIX_DOES[mix] : "???: " + (plus > 0 ? of.substring(0, plus) + " + ?" : mix == 6 ? "three of them, any" : "every one of them");
            text.setColor(known ? ink : soft);
            fitted(canvas, words, left + dp(16), top + dp(46) + mix * dp(19), dp(12), across - dp(32));
        }
        return top + dp(30) + MIX_NAMES.length * dp(19) + dp(8);
    }
}
