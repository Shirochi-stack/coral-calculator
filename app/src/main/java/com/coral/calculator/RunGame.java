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
    private static final int STORY = 0, MAP = 1, PLAY = 2, PAUSED = 3, RESULT = 4, SHOP = 5, STALL = 6;
    private static final int RESUME = 0, AGAIN = 1, TO_MAP = 2, NEXT = 3, COUNTER = 4;
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
    private static final float[] WEAPON_LONG = {.78f, .64f, .52f, .98f, 1.08f, .56f, .6f, .7f};
    private static final int[] SKIES = {R.drawable.plus_run_sky1, R.drawable.plus_run_sky2, R.drawable.plus_run_sky3,
            R.drawable.plus_run_sky4, R.drawable.plus_run_sky5, R.drawable.plus_run_sky6, R.drawable.plus_run_sky7, R.drawable.plus_run_sky8,
            R.drawable.plus_run_sky9, R.drawable.plus_run_sky10};
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
            R.drawable.run_pet_chikuwa, R.drawable.run_pet_rune, R.drawable.run_pet_dark, R.drawable.run_pet_light, R.drawable.run_pet_mini};
    private static final String[] PET_NAMES = {"The ink jar", "The kitten", "The puppy", "The duckling", "The monkey",
            "Mini Chikuwa", "Mini Rune", "Mini Halgakos, black wings", "Mini Halgakos, white wings", "Mini Halgakos"};
    private static final String[] PET_DOES = {"spits ink at what comes near", "pounces on what is close", "charges through them along the ground",
            "drops on what flies, and quacks", "throws nuts, and fetches", "throws bread rolls, and has one for her now and then",
            "cries ink that slows them, and flicks it", "swoops at the nearest, again and again", "sends rays of light, and turns one thing aside",
            "does what she does, smaller"};
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
            R.drawable.halgakos_arm3, R.drawable.halgakos_arm4, R.drawable.halgakos_arm5, R.drawable.halgakos_arm6, R.drawable.halgakos_arm7};
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
    /**
     * The board of hunts, open over the guild hall: its card; for each hunt on it the button
     * that takes it on or hands it in, and the one that gives it up, which asks once before
     * it does; what was last paid, and what came with it. And, in a land: how much of what
     * she has dealt with has been counted, which of her hunts something can be done towards
     * there, and how lately each got further.
     */
    private boolean huntsOpen;
    private final RectF huntsCard = new RectF();
    private final RectF[] huntActs = {new RectF(), new RectF(), new RectF()}, huntDrops = {new RectF(), new RectF(), new RectF()};
    private int dropAsk = -1, seenFells;
    private String huntPaid = "", huntMore = "";
    private final boolean[] huntHere = new boolean[RunHunts.SLOTS];
    private final float[] huntFlash = new float[RunHunts.SLOTS];
    /** How many treats the pet shop takes for each pet, in place of pluses; and the colours of the badges of the guild's ranks. */
    private static final int[] PET_TREATS = {2, 2, 2, 2, 3, 4, 4, 4, 4, 5};
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
            0xFFC0392B, 0xFFB8860B, 0xFF1E9C8B, 0xFF5C7FA8, 0xFF4A4A5A, 0xFF3F8F5A, 0xFFE08A2B, 0xFFE0662F, 0xFF7A8088, 0xFFC0392B, 0xFF8A5A33};
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
            "Kennel: stronger pets"};
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
            {"A pet's blow counts twice on a big one", "And a pet takes a whole piece off a shield"}};
    private static final int[][] UP_COSTS = {{120, 320}, {200}, {90, 240}, {80, 220}, {70, 180}, {260}, {160}, {150, 300, 480}, {200, 350, 500, 900},
            {120, 240, 400}, {400, 900}, {250, 600}, {200, 450}, {300, 650}};
    /** For her pets: more of her own beside her, more strays, brisker, stronger. */
    private static final int PET_PLACES = 10, STRAY_ROOM = 11, PET_BRISK = 12, PET_STRONG = 13;
    /** What a pet costs to have for good: the five animals, and the five small stuffed copies of people she knows. */
    private static final int[] PET_COSTS = {300, 300, 350, 350, 400, 600, 600, 700, 700, 800};
    /** The counter's tabs, and what stands under each. */
    private static final String[] TAB_NAMES = {"Hearts", "Moves", "Colours", "Comforts", "Pets", "Arms"};
    private static final int[][] TAB_WARES = {{0, 7, 5, 6}, {1}, {8, 9}, {2, 3, 4}, {PET_PLACES, STRAY_ROOM, PET_BRISK, PET_STRONG}, {}};
    private static final int PETS_TAB = 4, ARMS_TAB = 5, COLOURS_TAB = 2;
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
    private static final int[] UP_ORDER = {0, 7, 8, 9, 1, 2, 3, 4, 5, 6, 10, 11, 12, 13};
    private static final int HER_FACE = -2;
    private static final int[] UP_ART = {BUFF_ART + PlusRun.BREAD, -1, BUFF_ART + PlusRun.MAGNET, BUFF_ART + PlusRun.ICE, WEAPON_ART + PlusRun.GUN,
            FLAG_ART, BUFF_ART + PlusRun.SHIELD, HER_FACE, STAR_ART, STAR_ART, STAR_ART, STAR_ART, STAR_ART, STAR_ART};
    /** How long the ice cream, the magnet and the notes last before anything is bought, as {@link PlusRun} has them. */
    private static final float[] BUFF_LASTS = {0, 8, 0, 10, 6};

    private final PlayStage stage;
    private final Context context;
    private final boolean dark, haptics;
    private final PlaySession session;
    private final CompanionPrefs names;
    private final RunAudio audio;
    private final Random random = new Random();
    private final float density;
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
    private static final int ONE_AT_A_TIME = -1, ALL_I_CAN = -2;
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
        float[] wide, tall;

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
            int[] pixels = new int[side * side];
            for (int which = 0; which < count; which++) {
                bitmap.getPixels(pixels, 0, side, which % across * side, which / across * side, side, side);
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
                wide[which] = right < left ? 1 : Math.min(1, 2 * Math.max(side / 2f - left, right + 1 - side / 2f) / side);
                tall[which] = right < left ? 1 : (side - top) / (float) side;
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
            R.drawable.plus_run_room_pets, R.drawable.plus_run_room_guild, R.drawable.plus_run_room_home, R.drawable.plus_run_room_smithy, R.drawable.plus_run_room_market};
    private static final String[] LOOK_NAMES = {"A wine cellar", "A map room", "A flooded vault", "Inside a clock tower", "A greenhouse", "A printing room", "A crystal cave",
            "A treasure vault", "The pet shop", "The guild hall", "A home", "The smithy", "The black market"};
    /** The buildings of a village from outside: six of them in a row. */
    private Bitmap town;
    /** Its walls, the boards on top of them, and the light that lies over it. */
    private static final int[] LOOK_WALL = {0xFF3A2430, 0xFF2E2A22, 0xFF1F3A36, 0xFF3A2C24, 0xFF1C2440, 0xFF33241C, 0xFF241C3C, 0xFF35302C,
            0xFF3A2A1E, 0xFF36261C, 0xFF3A2C22, 0xFF2E2420, 0xFF2A1C34};
    private static final int[] LOOK_CAP = {0xFF9A6A52, 0xFF8A6A44, 0xFF5FA08A, 0xFFC09552, 0xFF5A78B8, 0xFFA8744A, 0xFF9A7AE0, 0xFFD0AC58,
            0xFFC08A52, 0xFFB8784A, 0xFFC09A6A, 0xFF9A8478, 0xFF8A62B0};
    private static final int[] LOOK_LIGHT = {0x16FFB060, 0x0CFFE0A0, 0x2030C8A8, 0x18FFA070, 0x263050B0, 0x10FFC080, 0x24A070FF, 0x16FFD060,
            0x08FFD090, 0x08FFD090, 0x08FFC890, 0x14FF9040, 0x1C9060E0};
    private Bitmap lookArt, props;
    private int lookOf = -1;
    private int seenKeys, seenLevers, seenPlates, seenShrines;
    private final float[] artWide = new float[ART_COUNT], artTall = new float[ART_COUNT];
    private Bitmap sky;
    private int skyOf = -1;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF target = new RectF(), safe = new RectF(), box = new RectF();
    private final RectF leftButton = new RectF(), rightButton = new RectF(), jumpButton = new RectF(), strikeButton = new RectF();
    private final RectF pauseButton = new RectF(), closeButton = new RectF(), soundButton = new RectF(), shopButton = new RectF(), goButton = new RectF();
    private final RectF[] pills = {new RectF(), new RectF(), new RectF()};
    private final String[] pillLabels = new String[3];
    private final int[] pillActions = new int[3];
    private int pillCount;
    private final RectF[] lands = new RectF[RunLevels.TOTAL];
    private final RectF[] tabs = {new RectF(), new RectF()}, stallWares = {new RectF(), new RectF(), new RectF(), new RectF()};
    private final RectF stallButton = new RectF();
    /** Which volume's lands the map shows; and from where the counter was opened, to go back there. */
    private int page, counterFrom = MAP;
    private final RectF[] wares = new RectF[UP_NAMES.length];
    private final Path path = new Path();
    private final int cardColor, ink, soft;

    private int width, height;
    private float tile;
    private int screen = MAP;
    private float clock;

    // The run
    private int number = 1;
    private PlusRun run;
    private PlusRun.Perks perks = new PlusRun.Perks();
    private float cameraX, cameraY;
    private boolean goLeft, goRight, goJump, goStrike;
    /** A press of the jump or of the button for what she carries that has not been acted on yet: a tap may be over before the next moment of the game. */
    private boolean jumpPressed, strikePressed;
    private int seenJumps, seenLandings, seenCollected, seenFlattened, seenHurts, seenSprings, seenFlags, seenBlows;
    private int seenStrikes, seenPicked, seenThrown, seenTurned, seenTaken, seenSpared, seenCharms, seenFlaps, seenSwings, seenBonus;
    /** Which of the things to fight with she has already said her word about in this land, and what else she has said once in it. */
    private final boolean[] remarked = new boolean[PlusRun.GUN + 1];
    private boolean saidCombo, saidLast, saidFlag, saidCharm;
    private float squash, sinceUse = 9, sinceLook, sinceAim = 9;
    private int seenFormed, seenDoors, seenQuakes, seenParried, seenCensors, seenEntrances, seenSpecials, seenPhases, seenUnlocks, seenWiped;
    private final boolean[] saidForm = new boolean[PlusRun.WHITE + 1];
    /** How long her form lasts in all; how much longer the screen shakes; how long since she walked into a shut way, since one shut, and since they opened. */
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
    private int open() { return Math.max(1, Math.min(RunLevels.TOTAL, session.tally("run_open"))); }

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
    private PlusRun.Perks kept() {
        PlusRun.Perks kept = new PlusRun.Perks();
        kept.hearts = PlusRun.HEARTS + bought(0);
        kept.lives = PlusRun.LIVES + bought(7);
        // How many of her colours she may wear at once: two, and with each step of the prism one more, the last step all seven.
        kept.together = bought(PRISM) >= PRISM_NEEDS.length ? 7 : 2 + bought(PRISM);
        kept.recharge -= bought(QUICK);
        kept.wings = bought(1) > 0;
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
        kept.pack = session.tally("run_pack") | 1 << PlusRun.TABLET | ((mains & 2) != 0 ? 1 << PlusRun.KEYS : 0);
        kept.main = session.tally("run_main") == 1 && (mains & 2) != 0 ? PlusRun.KEYS : PlusRun.TABLET;
        kept.recharge = Math.max(4, kept.recharge);
        // The pieces of a heart found in chests: four of them are a heart, and there are two hearts to be made of them.
        kept.hearts += Math.min(PIECE_HEARTS, session.tally("run_pieces") / PIECES);
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

    // The hunts

    /** The hunt in a place on the board, or nought; and how it stands: nought, on offer; else taken on, and one more than how much of it is done. */
    private int hunt(int slot) { return session.tally("run_hunt_" + slot); }
    private int huntAt(int slot) { return session.tally("run_hunt_at_" + slot); }

    /** What she has to fight with, a bit for each thing: what is in her backpack, the tablet-book, and her keys if they are hers. */
    private int armsOwned() { return session.tally("run_pack") | 1 << PlusRun.TABLET | ((session.tally("run_mains") & 2) != 0 ? 1 << PlusRun.KEYS : 0); }

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
            while (kind < PlusRun.PETS && (own >> kind & 1) != 0) kind++;
            if (kind < PlusRun.PETS) {
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

    /** The board of hunts as it stands in the guild hall: on two posts, with what is on it pinned up; and over it its name, or, when a hunt of hers is done, that it can be handed in. */
    private void drawBoard(Canvas canvas, int[] item) {
        float x = (item[1] + 1 - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF6B4426);
        canvas.drawRect(x - tile * .95f, feet - tile * 1.9f, x - tile * .82f, feet, paint);
        canvas.drawRect(x + tile * .82f, feet - tile * 1.9f, x + tile * .95f, feet, paint);
        box.set(x - tile * 1.15f, feet - tile * 2.05f, x + tile * 1.15f, feet - tile * .7f);
        canvas.drawRoundRect(box, tile * .08f, tile * .08f, paint);
        paint.setColor(0xFFB98A56);
        box.inset(tile * .09f, tile * .09f);
        canvas.drawRoundRect(box, tile * .05f, tile * .05f, paint);
        boolean any = false;
        for (int slot = 0; slot < RunHunts.SLOTS; slot++) {
            int hunt = hunt(slot), at = huntAt(slot);
            boolean taken = at > 0, finished = hunt != 0 && at > RunHunts.many(hunt);
            any |= finished;
            float px = x + (slot - 1) * tile * .66f, py = feet - tile * (1.4f + slot % 2 * .08f);
            canvas.save();
            canvas.rotate((slot - 1) * 4, px, py);
            paint.setColor(finished ? 0xFFDDF2E4 : taken ? 0xFFFCE0E4 : 0xFFFFF7E6);
            canvas.drawRect(px - tile * .25f, py - tile * .34f, px + tile * .25f, py + tile * .34f, paint);
            paint.setColor(0x55000000);
            for (int line = 0; line < 3; line++) {
                canvas.drawRect(px - tile * .17f, py - tile * .14f + line * tile * .13f, px + tile * (line == 2 ? .04f : .17f), py - tile * .1f + line * tile * .13f, paint);
            }
            paint.setColor(CalculatorLayout.CORAL);
            canvas.drawCircle(px, py - tile * .27f, tile * .05f, paint);
            canvas.restore();
        }
        paint.setColor(Color.BLACK);
        if (any) mark(canvas, x, feet - tile * 2.3f - Math.abs((float) Math.sin(clock * 5)) * tile * .12f, "Hand in!", 0xFFE0A21A, false);
        else mark(canvas, x, feet - tile * 2.3f, "Hunts", 0xFF8A5A33, false);
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

    private float dp(float value) { return value * density; }

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

    private Bitmap later(int who) {
        if (laterCast[who] == null) laterCast[who] = BitmapFactory.decodeResource(context.getResources(), LATER_CAST[who]);
        return laterCast[who];
    }

    private Bitmap villagers() {
        if (villagers == null) villagers = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_npc);
        return villagers;
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
            Bitmap[] set = new Bitmap[3 + ARMED.length];
            set[0] = dyed(rows, form);
            set[1] = dyed(jump, form);
            set[2] = dyed(lose, form);
            if (carried != null) set[3 + weapon] = dyed(carried, form);
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

    private Bitmap herPet() {
        Bitmap[] set = looks.get(drawn());
        return set != null ? set[0] : pet;
    }

    private Bitmap herJump() {
        Bitmap[] set = looks.get(drawn());
        return set != null ? set[1] : jump;
    }

    /** The sheet of her hurt, thrown and fallen, in a colour: so that she does not lose in pink what she was wearing in red. */
    private Bitmap herLose(int form) {
        Bitmap[] set = looks.get(form);
        return set != null ? set[2] : lose;
    }

    private Bitmap herArmed(int weapon) {
        int form = drawn();
        Bitmap[] set = looks.get(form);
        if (set == null) return armed(weapon);
        if (set[3 + weapon] == null) set[3 + weapon] = dyed(armed(weapon), form);
        return set[3 + weapon];
    }

    /** Who waits at the end of a land, by her colour, her name, and where she is among those who speak (below nothing for Zero and King Slime, who do not). */
    private static int waitColour(int kind) {
        return kind == RunLevels.ZERO ? CalculatorLayout.CORAL : kind == RunLevels.KING ? 0xFF3FA878 : kind == RunLevels.WYRM ? 0xFFB98A4E
                : kind == RunLevels.BLOT ? 0xFF4A55C4 : kind == RunLevels.GECKO ? 0xFF4FAE6A : SPEAKER_COLOURS[waitFace(kind)];
    }

    private static String waitName(int kind) {
        return kind == RunLevels.WYRM ? "The Margin Wyrm" : kind == RunLevels.BLOT ? "Blot" : kind == RunLevels.GECKO ? "The Bookmark Gecko"
                : kind == RunLevels.ZERO ? "Zero" : kind == RunLevels.ILLYA ? "Illya" : kind == RunLevels.TSUBAKIEH ? "Tsubakieh"
                : kind == RunLevels.GOKHAE ? "Gokhae" : kind == RunLevels.MIRI || kind == RunLevels.GARI ? "The Gazette Twins"
                : kind == RunLevels.YEOBAEK ? "Yeobaek" : kind == RunLevels.KING ? "King Slime" : kind == RunLevels.GUARD ? "The Guild Guard"
                : kind == RunLevels.BUTLER ? "Penchok-jipsa" : kind == RunLevels.BYEORU ? "Byeoru" : kind == RunLevels.JUPAN ? "Jupan"
                : kind == RunLevels.SUMBI ? "Sumbi" : kind == RunLevels.PADS ? "The Brake Pads" : kind == RunLevels.PARTY ? "The Straight-Ahead Party"
                : kind == RunLevels.OLD_BOOK ? "The Old Book" : "The Mawang";
    }

    private static int waitFace(int kind) {
        return kind == RunLevels.ILLYA ? 3 : kind == RunLevels.TSUBAKIEH ? 4 : kind == RunLevels.GOKHAE ? 6 : kind == RunLevels.MIRI ? 7
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
        float room = safe.height() - dp(92) - dp(150) - dp(44);
        for (int volume = 0; volume < 2; volume++) {
            int each = volume == 0 ? RunLevels.IN_CHAPTER : RunLevels.IN_LATER_CHAPTER, from = volume == 0 ? 0 : RunLevels.COUNT;
            float gap = volume == 0 ? dp(8) : dp(5);
            float side = Math.max(dp(22), Math.min(dp(54), Math.min((safe.width() - dp(28) - gap * (each - 1)) / each, room / RunLevels.CHAPTERS - dp(34))));
            float left = safe.centerX() - (side * each + gap * (each - 1)) / 2, top = safe.top + dp(92) + dp(44);
            for (int index = 0; index < RunLevels.CHAPTERS * each; index++) {
                float x = left + index % each * (side + gap), down = top + index / each * (side + dp(34)) + dp(22);
                lands[from + index].set(x, down, x + side, down + side);
            }
        }
        // (The tabs leave room at the end of their row for the button that opens her backpack.)
        float tab = Math.min(dp(132), (safe.width() - dp(44) - dp(84)) / 2);
        tabs[0].set(safe.left + dp(18), safe.top + dp(80), safe.left + dp(18) + tab, safe.top + dp(114));
        tabs[1].set(tabs[0].right + dp(8), tabs[0].top, tabs[0].right + dp(8) + tab, tabs[0].bottom);
        gearButton.set(safe.right - dp(18) - dp(76), tabs[0].top, safe.right - dp(18), tabs[0].bottom);
        // The button that changes what she holds: over the one that uses it, a little smaller.
        float swaps = Math.max(dp(44), size * .66f);
        swapButton.set(strikeButton.centerX() - swaps / 2, strikeButton.top - dp(12) - swaps, strikeButton.centerX() + swaps / 2, strikeButton.top - dp(12));
        turnTo(page);
        // The button that lets her down through a thin ledge: over the arrows and between them, where the thumb that holds them reaches it,
        // and clear of where she walks. It keeps its place whether it can do anything or not.
        float downs = Math.max(dp(48), size * .78f), middle = (leftButton.left + rightButton.right) / 2;
        downButton.set(middle - downs / 2, y - dp(12) - downs, middle + downs / 2, y - dp(12));
        // The stall in a land, or a door: its button over the jump, where her thumb already is.
        stallButton.set(jumpButton.left, jumpButton.top - dp(14) - size, jumpButton.right, jumpButton.top - dp(14));
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
    }

    /**
     * Shows the lands of a volume, with the button that goes on to the next land under the
     * last of them, where a thumb goes first. The counter is up beside the sound, a little
     * lower than the round buttons so that it clears the line under the heading.
     */
    private void turnTo(int volume) {
        page = volume;
        int from = volume == 0 ? 0 : RunLevels.COUNT, last = volume == 0 ? RunLevels.COUNT - 1 : RunLevels.TOTAL - 1;
        float under = lands[last].bottom + dp(16);
        goButton.set(lands[from].left, under, lands[last].right, under + dp(48));
        text.setTextSize(dp(26));
        float from0 = safe.left + dp(18) + text.measureText("Plus run") + dp(12), to = soundButton.left - dp(8);
        shopButton.set(Math.max(from0, to - dp(128)), soundButton.top + dp(3), to, soundButton.bottom - dp(4));
    }

    void close() { audio.close(); }

    /** Left for the home screen: a run stops on its pause, and whoever was speaking stops, so that it can be come back to as it stands. */
    void hold() {
        if (screen == PLAY) pause();
        audio.hush();
    }

    /** The Back key: out of a run to its pause, out of the story, the counter or a card to the map; false to leave the game. */
    boolean back() {
        // Whatever sheet is open over the screen is what Back closes: her backpack, or the colours opened out.
        if (packOpen) {
            packOpen = false;
            audio.play("tap", .3f);
            return true;
        }
        if (huntsOpen) {
            closeHunts();
            return true;
        }
        if (fanOpen) {
            fanOpen = false;
            idle();
            return true;
        }
        if (screen == PLAY) {
            pause();
        } else if (screen == PAUSED) {
            screen = PLAY;
        } else if (screen == STORY) {
            endStory();
        } else if (screen == STALL) {
            screen = PLAY;
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
        audio.hush();
        screen = MAP;
        session.refresh();
    }

    private void buzz(boolean strongly) {
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

    // A land

    private void play(int land) {
        number = land;
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
        packOpen = false;
        huntsOpen = false;
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
        // For looking at what losing looks like in a build made for testing: one heart and one life.
        if (BuildConfig.DEBUG && session.tally("run_frail") > 0) {
            perks.hearts = 1;
            perks.lives = 1;
        }
        run = new PlusRun(RunLevels.level(land), perks);
        huntsHere();
        // For looking at a land's later parts in a build made for testing: she starts by its first flag, its second,
        // inside the place where someone waits, or, for a number over a thousand, at the column that many over it.
        int from = BuildConfig.DEBUG ? session.tally("run_from") : 0;
        if (from > 0) {
            RunLevels.Level here = run.level();
            int column = from > 1000 ? Math.min(here.columns - 2, from - 1000) : from <= here.flags.size() ? here.flags.get(from - 1)
                    : here.arena >= 0 ? here.arena + 4 : here.gate - 10;
            // Over a pit, the first ground to the right of it.
            while (column < here.columns - 2 && RunLevels.top(here, column) < 0) column++;
            run.place(column, RunLevels.top(here, column) - PlusRun.TALL - .05f);
        }
        // And, for the same, she may start in a colour, as from a ribbon, and with something in her hands.
        if (BuildConfig.DEBUG && session.tally("run_wear") > 0) run.lend(session.tally("run_wear"));
        // And the white that is truly hers, which the story has yet to give.
        if (BuildConfig.DEBUG && session.tally("run_true") > 0) run.master();
        PlusRun.ceiling = BuildConfig.DEBUG && session.tally("run_ceiling") > 0 ? session.tally("run_ceiling") : PlusRun.CEILING;
        // And a pet, for looking at one.
        if (BuildConfig.DEBUG && session.tally("run_pet") > 0) run.adopt(session.tally("run_pet") - 1);
        if (BuildConfig.DEBUG && session.tally("run_arm") > 0) run.arm(session.tally("run_arm") - 1);
        idle();
        seenJumps = seenLandings = seenCollected = seenFlattened = seenHurts = seenSprings = seenFlags = seenBlows = 0;
        seenStrikes = seenPicked = seenThrown = seenTurned = seenTaken = seenSpared = seenCharms = seenFlaps = seenSwings = seenBonus = 0;
        seenFormed = seenDoors = seenQuakes = seenParried = seenCensors = seenEntrances = seenSpecials = seenPhases = seenUnlocks = seenWiped = 0;
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
        screen = PLAY;
        captionLeft = 0;
        // The first time she has a colour of her own to call on, she says how; after that, something about setting off.
        if (greeted && perks.colours != 0 && session.tally("run_hint_call") == 0) {
            session.keep("run_hint_call", 1);
            speak(RunStory.PRIZES[3], true);
        } else if (greeted) {
            speakAny(RunStory.START);
        }
    }

    private void idle() {
        goLeft = goRight = goJump = goStrike = false;
        jumpPressed = strikePressed = false;
        ribbonFinger = -1;
        fanOpen = false;
    }

    private void pause() {
        idle();
        screen = PAUSED;
        pills("Carry on", RESUME, "Start again", AGAIN, "Lands", TO_MAP);
    }

    /** Sets out the buttons of a card. */
    private void pills(Object... labelled) {
        pillCount = labelled.length / 2;
        for (int index = 0; index < pillCount; index++) {
            pillLabels[index] = (String) labelled[index * 2];
            pillActions[index] = (Integer) labelled[index * 2 + 1];
        }
    }

    private void finish(boolean reached) {
        won = reached;
        idle();
        resultClock = 0;
        starsShown = 0;
        screen = RESULT;
        captionLeft = 0;
        // Whatever she collected is hers to spend, whether she reached the gate or not.
        banked = run.purse();
        session.keep("run_bank", bank() + banked);
        if (won) {
            float share = run.total() == 0 ? 1 : run.collected() / (float) run.total();
            stars = 1 + (share >= .6f ? 1 : 0) + (share >= .9f ? 1 : 0);
            if (stars > starsOf(number)) session.keep("run_stars_" + number, stars);
            session.keepBest("run_time_" + number, Math.max(1, Math.round(run.time())), true);
            if (number >= open() && number < RunLevels.TOTAL) session.keep("run_open", number + 1);
            if (number == 1) session.keepBest("run_best", Math.max(1, Math.round(run.time())), true);
            audio.play("win");
            // What whoever waited here gave was hers when they fell; had there been no one to see it then, it is hers now. Her word is about how the land went.
            int late = prize(number);
            if (late != RunLevels.NOTHING) prizeWon = late;
            speak(RunStory.WON[stars - 1], false);
            session.keep("run_mobs", session.tally("run_mobs") + run.flattened());
            session.keep("run_fliers", session.tally("run_fliers") + run.fliers());
            session.keep("run_pet_kills", session.tally("run_pet_kills") + run.petKills());
            pills(number < RunLevels.TOTAL ? number == RunLevels.COUNT ? RunStory.SEQUEL : "Next" : "The end", NEXT, "Again", AGAIN, "Lands", TO_MAP);
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
        String[][] outro = screen == RESULT && won ? RunStory.outro(number) : null;
        if (outro != null && untold("run_outro2", number)) tell(outro, RunLevels.chapterOf(number), () -> carryOut(action));
        else carryOut(action);
    }

    private void carryOut(int action) {
        if (action == RESUME) {
            screen = PLAY;
        } else if (action == COUNTER) {
            counterFrom = STALL;
            screen = SHOP;
            speak(RunStory.SHOP[0], false);
        } else if (action == AGAIN) {
            begin(number, true);
        } else if (action == TO_MAP || number >= RunLevels.TOTAL) {
            if (number >= RunLevels.COUNT) turnTo(open() > RunLevels.COUNT ? 1 : 0);
            toMap();
        } else {
            play(number + 1);
        }
    }

    /** How far down the view sits when the whole height of the land fits on the screen: its ground on the bottom. */
    private float restingCamera() { return RunLevels.ROWS - height / tile; }

    // Time

    void advance(float seconds) {
        clock += seconds;
        if (captionLeft > 0) captionLeft -= seconds;
        if (refused > 0) refused -= seconds;
        if (knockAge >= 0 && (knockAge += seconds) > KNOCK) knockAge = -1;
        if (shake > 0) shake -= seconds;
        if (flash > 0) flash -= seconds;
        if (personLeft > 0) personLeft -= seconds;
        // A card that says something was won is read while the land is on the screen: it does not run out behind a pause.
        if (toastLeft > 0 && screen == PLAY) toastLeft -= seconds;
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
        if (screen == PLAY && prizeAge >= 0 && prizeAge < 2) {
            prizeAge += seconds;
        } else if (screen == PLAY && (fanOpen || packOpen || huntsOpen)) {
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
        jumpPressed = false;
        strikePressed = false;
        sinceUse += seconds;
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
        float level = down >= RunLevels.ROWS ? restingCamera()
                : Math.max(0, Math.min(RunLevels.ROWS - down, run.y() + PlusRun.TALL - down * .62f));
        if (run.room() >= 0) {
            // A place off the land has a floor and a ceiling of its own: an arena's are far apart, and the view follows her between them.
            int[] in = run.level().rooms.get(run.room());
            float ceiling = in[12] - 2, ground = in[11] + 3;
            level = ground - ceiling <= down ? ground - down : Math.max(ceiling, Math.min(ground - down, run.y() + PlusRun.TALL - down * .6f));
            if (run.entered() != seenEntered) cameraY = level;
        } else if (run.entered() != seenEntered) {
            cameraY = level;
        }
        if (!held) cameraY += (level - cameraY) * Math.min(1, seconds * 6);
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
                if ((had & 1 << kind) == 0) {
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
            seenShatters = run.shatters();
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
                    : kind == RunLevels.PADS ? "quake" : kind == RunLevels.PARTY ? "bat" : "flap", .7f);
            if (kind == RunLevels.ZERO || kind == RunLevels.KING || kind == RunLevels.PADS) shake = .5f;
            buzz(true);
            speak(RunStory.ENTER[RunStory.waiting(kind)], true);
        }
        if (run.specials() != seenSpecials && boss != null) {
            seenSpecials = run.specials();
            int what = run.special();
            audio.play(what == 63 || what == 72 || what == 32 || what == 181 || what == 31 || what == 184 || what == 202 || what == 241 || what == 251 || what == 263
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
                        : null;
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
            speak(RunStory.PHASES[RunStory.waiting(which) * 2 + Math.max(0, Math.min(1, run.phase(boss) - 1))], true);
        }
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
                    toast(LOOK_NAMES[RunLevels.LOOKS + sort], sort == RunLevels.PET_SHOP ? "Walk up to a pet to look at it; Nabi keeps the shop"
                            : sort == RunLevels.GUILD_HALL ? "Hunts on the board; the counter at Inju's desk" : "Somebody is in", 0);
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
        if (run.chests() != seenChests) {
            seenChests = run.chests();
            audio.play("unlock");
            buzz(true);
            chestAge = 0;
            chestX = run.tookX();
            chestY = run.tookY();
            flyTo((chestX - cameraX) * tile, (chestY - cameraY) * tile, false, 8);
            pop(herX, herY - 1.6f, "+ 10");
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
                roomScore = PlayStage.clock(Math.max(1, Math.round(run.arenaTime()))) + "  ·  " + (run.arenaHurts() == 0 ? "not a hurt" : run.arenaHurts() + (run.arenaHurts() == 1 ? " hurt" : " hurts"))
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
                if (rank < PlusRun.RANK_S) roomGave += (roomGave.isEmpty() ? "" : ". ") + "Rank S: two hurts at most, under " + PlayStage.clock(Math.round(PlusRun.PAR));
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
                speak(kind >= RunLevels.GOKHAE ? RunStory.BLOWS_LATER[kind - RunLevels.GOKHAE]
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
        if (run.state() == PlusRun.WON) finish(true);
        else if (run.state() == PlusRun.LOST && knockAge < 0) finish(false);
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
        for (int[] item : run.items()) if (item[0] > PlusRun.FORM && item[0] < PlusRun.CHEST && item[1] >= from && item[1] <= to && (seen & 1 << 24) == 0) found = 24;
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
        float x = event.getX(), y = event.getY();
        if (packOpen) {
            if (action == MotionEvent.ACTION_DOWN) packTouch(x, y);
            return;
        }
        if (huntsOpen) {
            if (action == MotionEvent.ACTION_DOWN) huntsTouch(x, y);
            return;
        }
        if (screen == PLAY) {
            if (action == MotionEvent.ACTION_DOWN && pauseButton.contains(x, y)) {
                audio.play("tap", .5f);
                pause();
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
            held(event);
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
            } else if (gearButton.contains(x, y)) {
                audio.play("tap", .5f);
                buzz(false);
                openPack(MAP);
            } else if (goButton.contains(x, y)) {
                // On with the furthest land she has reached.
                audio.play("tap", .5f);
                buzz(false);
                play(open());
            } else if (open() > RunLevels.COUNT && (tabs[0].contains(x, y) || tabs[1].contains(x, y))) {
                audio.play("page", .5f);
                turnTo(tabs[1].contains(x, y) ? 1 : 0);
            } else {
                int first = page == 0 ? 0 : RunLevels.COUNT, last = page == 0 ? RunLevels.COUNT : RunLevels.TOTAL;
                for (int index = first; index < last; index++) {
                    if (lands[index].contains(x, y) && index + 1 <= open()) {
                        audio.play("tap", .5f);
                        buzz(false);
                        play(index + 1);
                        return;
                    }
                }
            }
        } else if (screen == SHOP) {
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
                    audio.play("page", .5f);
                    return;
                }
            }
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

    /** Which buttons are held, by however many fingers are down: one may hold an arrow while another jumps. */
    private void held(MotionEvent event) {
        int action = event.getActionMasked();
        boolean left = false, right = false, up = false, strike = false, down = false;
        boolean carrying = run != null && (run.weapon() != PlusRun.UNARMED || run.has(PlusRun.RED) || run.has(PlusRun.BLUE));
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
        if (up && !goJump) jumpPressed = true;
        if (strike && !goStrike) strikePressed = true;
        goLeft = left;
        goRight = right;
        goJump = up;
        goStrike = strike;
        goDown = down;
    }

    // Drawing

    void draw(Canvas canvas) {
        if (width == 0) return;
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
                drawHud(canvas);
            }
            drawStory(canvas);
        } else {
            drawLand(canvas);
            // A moment in which something is won or handed over lies over the land, and under what is said and shown of how she stands.
            if (run.beam() > 0) drawBeam(canvas);
            if (blastAge < .45f) {
                // A book has burst: a ring of light out to as far as it reaches, and a white heart to it.
                float out = blastAge / .45f, at = (blastX - cameraX) * tile, high = (blastY - cameraY) * tile;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFFFF3C4);
                paint.setAlpha(Math.round(170 * (1 - out)));
                canvas.drawCircle(at, high, tile * PlusRun.BLAST * (.35f + .65f * out), paint);
                paint.setColor(Color.WHITE);
                paint.setAlpha(Math.round(230 * (1 - out)));
                canvas.drawCircle(at, high, tile * PlusRun.BLAST * .4f * (1 - out * .5f), paint);
                stroke.setColor(0xFFE0A21A);
                stroke.setAlpha(Math.round(255 * (1 - out)));
                stroke.setStrokeWidth(tile * .1f);
                canvas.drawCircle(at, high, tile * PlusRun.BLAST * out, stroke);
                stroke.setAlpha(255);
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
            }
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
                    if (run.door() >= 0) doorControl(canvas);
                    else if (run.spot() >= 0) {
                        int what = run.spot();
                        button(canvas, stallButton, false, 0xFF8A5A33);
                        paint.setAlpha(255);
                        if (what < 100) cell(canvas, petArt(what), 0, 6, stallButton.centerX(), stallButton.centerY() + stallButton.height() * .16f, stallButton.height() * .62f, false);
                        else if (what == PlusRun.BOARD) pieceIn(canvas, four, SCROLL_PICTURE, stallButton.centerX(), stallButton.centerY() - dp(7), stallButton.width() * .5f, 0);
                        else artIn(canvas, STAR_ART, stallButton.centerX(), stallButton.centerY() - dp(7), stallButton.width() * .42f);
                        label(canvas, stallButton, what < 100 ? "Look" : what == PlusRun.BOARD ? "Hunts" : what == 100 + PETS_TAB ? "Pets" : "Counter");
                    } else if (run.petOffer() >= 0) {
                        button(canvas, stallButton, false, 0xFF8A5A33);
                        paint.setAlpha(255);
                        cell(canvas, petArt(run.petOffer()), 0, 6, stallButton.centerX(), stallButton.centerY() + stallButton.height() * .16f, stallButton.height() * .62f, false);
                        label(canvas, stallButton, "Swap");
                    }
                }
            } else if (screen == STALL) {
                canvas.drawColor(0x8C000000);
                drawStall(canvas);
            } else {
                canvas.drawColor(0x8C000000);
                if (screen == PAUSED) {
                    // (With her backpack to look into.)
                    float inside = card(canvas, "Paused", RunStory.CHAPTERS[RunLevels.chapterOf(number)] + ", land " + number, dp(52));
                    packButton.set(box.left + dp(20), inside, box.right - dp(20), inside + dp(42));
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(dark ? 0xFF33373B : 0xFFF1F3F4);
                    canvas.drawRoundRect(packButton, dp(21), dp(21), paint);
                    paint.setColor(Color.BLACK);
                    thing(canvas, run.weapon() == PlusRun.UNARMED ? PlusRun.TABLET : run.weapon(), packButton.left + dp(26), packButton.centerY(), dp(26), 0, false, false);
                    text.setColor(ink);
                    text.setTextAlign(Paint.Align.LEFT);
                    fitted(canvas, "Backpack  \u203A", packButton.left + dp(50), packButton.centerY() + dp(5.5f), dp(15.5f), packButton.width() - dp(64));
                } else {
                    drawResult(canvas);
                }
            }
        }
        // Her backpack, over whatever is there.
        if (packOpen) drawPack(canvas);
        if (huntsOpen) drawHunts(canvas);
    }

    /** Her backpack is opened: from a land, where the land waits; from the pause; or from the map. */
    private void openPack(int from) {
        packOpen = true;
        packFrom = from;
        swapFinger = -1;
        packOn = from == MAP ? kept().main : run.weapon() != PlusRun.UNARMED ? run.weapon() : PlusRun.TABLET;
        goLeft = goRight = goJump = goStrike = false;
        jumpPressed = strikePressed = false;
        audio.play("page", .6f);
    }

    /** What is in it: in a land, what she has there; on the map, what is kept. */
    private int packed() { return packFrom == MAP || run == null ? kept().pack | 1 << kept().main : run.owned(); }

    private void packTouch(float x, float y) {
        int owned = packed();
        for (int kind = 0; kind < RunArms.COUNT; kind++) {
            if ((owned >> kind & 1) == 0 || packRows[kind] == null || !packRows[kind].contains(x, y)) continue;
            audio.play("tap", .5f);
            packOn = kind;
            return;
        }
        if (!packAct.isEmpty() && packAct.contains(x, y)) {
            // In a land: what is looked at is taken in hand. On the map: it is what she begins every land with.
            if (packFrom == MAP) {
                session.keep("run_main", packOn == PlusRun.KEYS ? 1 : 0);
                perks = kept();
                audio.play("unlock");
            } else if (run.switchTo(packOn)) {
                packOpen = false;
            }
            buzz(false);
            return;
        }
        if (!packCard.contains(x, y)) {
            packOpen = false;
            audio.play("tap", .3f);
        }
    }

    /** A figure as it is written: without a nought after its point. */
    private static String figure(float value) {
        String written = String.format(java.util.Locale.US, "%.1f", value);
        return written.endsWith(".0") ? written.substring(0, written.length() - 2) : written;
    }

    /**
     * Her backpack: a row for each thing in it, with its picture, its name and how far it
     * reaches and how fast it is, to compare one with another; and under the rows the card
     * of the one that is looked at: its figures, which are the rules' own, what it does,
     * what it is good for, and a button that takes it in hand or, on the map, makes it
     * what she begins every land with.
     */
    private void drawPack(Canvas canvas) {
        canvas.drawColor(0x99000000);
        int owned = packed(), count = Integer.bitCount(owned), main = kept().main;
        boolean inLand = packFrom != MAP && run != null;
        float wide = Math.min(safe.width() - dp(24), dp(430)), row = Math.max(dp(40), Math.min(dp(52), (safe.height() - dp(350)) / Math.max(1, count))), pad = dp(16);
        float tall = pad + dp(30) + count * row + dp(10) + dp(216) + pad, left = safe.centerX() - wide / 2, top = Math.max(safe.top + dp(8), safe.centerY() - tall / 2);
        packCard.set(left, top, left + wide, top + tall);
        plate(canvas, packCard, dp(22), 255);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, "Backpack", left + pad, top + pad + dp(18), dp(20), wide - pad * 2);
        text.setTypeface(Typeface.DEFAULT);
        text.setColor(soft);
        text.setTextAlign(Paint.Align.RIGHT);
        fitted(canvas, inLand ? "what she has found, and what is hers" : "what is kept from land to land", left + wide - pad, top + pad + dp(17), dp(12), wide - pad * 2 - dp(110));
        float y = top + pad + dp(30);
        for (int kind = 0; kind < RunArms.COUNT; kind++) {
            if ((owned >> kind & 1) == 0) {
                packRows[kind] = null;
                continue;
            }
            if (packRows[kind] == null) packRows[kind] = new RectF();
            RectF at = packRows[kind];
            at.set(left + dp(8), y, left + wide - dp(8), y + row - dp(4));
            boolean on = kind == packOn, held = inLand ? run.weapon() == kind : main == kind, empty = inLand && !run.usable(kind);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(on ? (dark ? 0xFF44292E : 0xFFFCE9EB) : dark ? 0xFF2C3033 : 0xFFF5F6F7);
            canvas.drawRoundRect(at, dp(14), dp(14), paint);
            if (on) {
                stroke.setColor(CalculatorLayout.CORAL);
                stroke.setStrokeWidth(dp(2));
                canvas.drawRoundRect(at, dp(14), dp(14), stroke);
            }
            paint.setColor(Color.BLACK);
            paint.setAlpha(empty ? 90 : 255);
            boolean upright = kind != PlusRun.GUN && kind != PlusRun.BOOK && kind < PlusRun.TABLET;
            thing(canvas, kind, at.left + dp(26), at.centerY(), at.height() * (upright ? .8f : .62f), upright ? 35 : 0, false, false);
            paint.setAlpha(255);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            fitted(canvas, RunArms.NAMES[kind], at.left + dp(52), at.centerY() - dp(2), dp(15), at.width() * .42f);
            text.setTypeface(Typeface.DEFAULT);
            text.setColor(soft);
            int level = perks.levels[kind];
            fitted(canvas, "reach " + figure(RunArms.reach(kind, level)) + "  \u00B7  " + figure(RunArms.pace(kind, level)) + " a second", at.left + dp(52), at.centerY() + dp(14), dp(11.5f),
                    at.width() - dp(60));
            // What marks it: in her hands; what she begins with; or empty.
            String mark = empty ? "empty" : held ? (inLand ? "in hand" : "begins with") : RunArms.main(kind) ? "hers" : "";
            if (!mark.isEmpty()) {
                text.setTextAlign(Paint.Align.RIGHT);
                text.setColor(held ? CalculatorLayout.CORAL : soft);
                fitted(canvas, mark, at.right - dp(12), at.centerY() + dp(4), dp(12), dp(80));
            }
            y += row;
        }
        // The card of the one that is looked at.
        packAct.setEmpty();
        if (packOn < 0 || (owned >> packOn & 1) == 0) return;
        int kind = packOn, level = perks.levels[kind];
        boolean boosted = inLand && run.boost() > 0 && run.weapon() == kind, swung = RunArms.way(kind) == RunArms.SWUNG;
        y += dp(10);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, RunArms.NAMES[kind] + "  \u00B7  level " + (level + 1), left + pad, y + dp(14), dp(15.5f), wide - pad * 2);
        text.setTypeface(Typeface.DEFAULT);
        // Its figures: the rules' own. Stronger for a find, the changed ones, marked.
        float reach = RunArms.reach(kind, level) * (boosted && !swung ? RunArms.BOOST_FAR : 1) + (boosted && swung ? RunArms.BOOST_SWING : 0);
        float bite = RunArms.bite(kind, level) + (boosted ? RunArms.BOOST_BITE : 0);
        String up = boosted ? " \u2191" : "";
        String first = "Power: 1 blow  \u00B7  reach " + figure(reach) + up + " squares  \u00B7  " + figure(RunArms.pace(kind, level)) + " a second";
        String second = "Shield: " + figure(bite) + up + (bite == 1 ? " piece" : " pieces") + " a blow"
                + (RunArms.area(kind, level) > 0 ? "  \u00B7  " + (kind == PlusRun.BOOK ? "burst " : "sweep ") + figure(RunArms.area(kind, level)) + " squares" : "")
                + (kind == PlusRun.TABLET ? "  \u00B7  panel " + figure(RunArms.PANEL_WIDE) + " by " + figure(RunArms.PANEL_TALL) + ", " + figure(RunArms.PANEL_BITE) + " pieces" : "")
                + (kind == PlusRun.KEYS ? "  \u00B7  fan of " + RunArms.FAN : "");
        int holds = RunArms.holds(kind, perks.shots);
        String third = holds == 0 ? "No end to it" : kind == PlusRun.GUN ? (inLand ? run.rounds() + " shots left of " : "Holds ") + holds + (inLand ? "" : " shots")
                : (inLand ? run.books() + " left of " : "") + holds + " to a land";
        text.setColor(CalculatorLayout.CORAL);
        fitted(canvas, first, left + pad, y + dp(34), dp(12.5f), wide - pad * 2);
        fitted(canvas, second, left + pad, y + dp(52), dp(12.5f), wide - pad * 2);
        text.setColor(soft);
        fitted(canvas, third + (boosted ? "  \u00B7  stronger for " + (int) Math.ceil(run.boost()) + " s more" : ""), left + pad, y + dp(70), dp(12.5f), wide - pad * 2);
        // What it does, and what it is good for: no more than four lines of it.
        text.setColor(ink);
        text.setTextSize(dp(12.5f));
        List<String> lines = wrap(RunArms.does(kind) + " " + RunArms.goodFor(kind), wide - pad * 2);
        for (int index = 0; index < Math.min(5, lines.size()); index++) canvas.drawText(lines.get(index), left + pad, y + dp(92) + index * dp(16.5f), text);
        // The button: take it in hand; or begin every land with it.
        boolean can = inLand ? run.weapon() != kind && run.usable(kind) : RunArms.main(kind) && main != kind;
        if (can) {
            packAct.set(left + pad, top + tall - pad - dp(40), left + wide - pad, top + tall - pad);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(CalculatorLayout.CORAL);
            canvas.drawRoundRect(packAct, dp(20), dp(20), paint);
            paint.setColor(Color.BLACK);
            text.setColor(Color.WHITE);
            text.setTextAlign(Paint.Align.CENTER);
            fitted(canvas, inLand ? "Take it in hand" : "Begin every land with it", packAct.centerX(), packAct.centerY() + dp(5.5f), dp(15.5f), packAct.width() - dp(24));
        } else {
            text.setColor(soft);
            text.setTextAlign(Paint.Align.CENTER);
            String why = inLand ? (run.weapon() == kind ? "It is in her hands" : "Empty: find more for it") : main == kind ? "She begins every land with it" : "Found in the lands: taken in hand there";
            fitted(canvas, why, left + wide / 2, top + tall - pad - dp(14), dp(12.5f), wide - pad * 2);
        }
        text.setTextAlign(Paint.Align.LEFT);
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
        canvas.save();
        canvas.rotate(shot.age * 540, x, y);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF7FE7DA);
        paint.setAlpha(110);
        canvas.drawRoundRect(x - size * .62f, y - size * .78f, x + size * .62f, y + size * .78f, size * .2f, size * .2f, paint);
        paint.setColor(Color.WHITE);
        paint.setAlpha(255);
        canvas.drawRoundRect(x - size * .46f, y - size * .62f, x + size * .46f, y + size * .62f, size * .08f, size * .08f, paint);
        paint.setColor(0xFF2FB5A8);
        for (int line = 0; line < 4; line++) canvas.drawRect(x - size * .3f, y - size * .42f + line * size * .25f, x + size * (line == 3 ? .05f : .3f), y - size * .34f + line * size * .25f, paint);
        paint.setColor(Color.BLACK);
        canvas.restore();
    }

    /** The panel of translation her tablet-book opens: a pane of light in front of her with lines of writing in it, there for a third of a second. */
    private void drawPanel(Canvas canvas) {
        if (panelAge > .36f) return;
        float through = panelAge / .36f, wide = RunArms.PANEL_WIDE * tile, tall = RunArms.PANEL_TALL * tile, open = Math.min(1, through * 4);
        float near = (panelX - cameraX) * tile, top = (panelY - cameraY) * tile, far = near + (panelLeft ? -1 : 1) * wide * open;
        float from = Math.min(near, far), to = Math.max(near, far);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF7FE7DA);
        paint.setAlpha(Math.round(120 * (1 - through)));
        canvas.drawRoundRect(from, top, to, top + tall, tile * .2f, tile * .2f, paint);
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
    private void thing(Canvas canvas, int kind, float x, float y, float tall, float degrees, boolean mirrored, boolean byTheHandle) {
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
        target.set(left, y, left + tile + 1, y + tile + 1);
        canvas.save();
        canvas.scale(mirrored ? -1 : 1, upended ? -1 : 1, left + tile / 2, y + tile / 2);
        canvas.drawBitmap(bitmap, source, target, paint);
        canvas.restore();
    }

    private void drawLand(Canvas canvas) {
        RunLevels.Level level = run.level();
        int chapter = level.chapter;
        if (run.room() >= 0) drawInside(canvas);
        else drawSky(canvas, chapter, cameraX * tile * .2f);
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
            if (decor[0] >= 300) {
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
                char here = run.at(column, row);
                float y = (row - cameraY) * tile;
                char laid = run.room() >= 0 ? run.laid(column, row) : RunLevels.AIR;
                if (laid == RunLevels.LOCKED || laid == RunLevels.SPAN || laid == RunLevels.GRILLE) {
                    if (laid != RunLevels.SPAN || here == RunLevels.AIR) {
                        drawFixture(canvas, laid, here, column, row, left, y);
                        continue;
                    }
                }
                if (here == RunLevels.AIR) continue;
                if (here == RunLevels.BLOCK && run.room() >= 0) {
                    // In a place off the land, its walls are dark, of the colour of the place, with a lighter board where they are stood on.
                    boolean capped = run.at(column, row - 1) != RunLevels.BLOCK;
                    int look = look();
                    int[] in = level.rooms.get(run.room());
                    // (What stands inside it, between its floor and its ceiling, is masonry of the place's own colour, with its courses
                    // showing: a pillar, a tower, a wall with a door in it; not the dark that lies round the place.)
                    boolean within = look >= 0 && row >= in[12] && row < in[11] && column >= in[3] && column <= in[4];
                    paint.setStyle(Paint.Style.FILL);
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
                    boolean capped = run.at(column, row - 1) != RunLevels.BLOCK;
                    if (later) {
                        square(canvas, (chapter - RunLevels.CHAPTERS) * 2 + (capped ? 0 : 1), left, y, column % 2 != 0, !capped && row % 2 != 0);
                        continue;
                    }
                    int which = chapter * 2 + (capped ? 0 : 1);
                    // Taken a hair inside its square, so the picture beside it does not show as a line between blocks.
                    source.set(which * side + 1, 0, which * side + side - 1, side);
                    target.set(left, y, left + tile + 1, y + tile + 1);
                    canvas.save();
                    canvas.scale(column % 2 == 0 ? 1 : -1, capped || row % 2 == 0 ? 1 : -1, left + tile / 2, y + tile / 2);
                    canvas.drawBitmap(art, source, target, paint);
                    canvas.restore();
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
                } else if (here == RunLevels.LAVA || here == RunLevels.WATER) {
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
                    // Faint while someone keeps it shut.
                    paint.setAlpha(run.gateOpen() ? 255 : 110);
                    artTall(canvas, GATE_ART, left + tile / 2, y + tile * 2, tile * 2.9f, false);
                    paint.setAlpha(255);
                }
            }
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
        for (int[] plus : run.pluses()) {
            if (plus[0] < first - 1 || plus[0] > last + 1) continue;
            float x = (plus[0] + .5f - cameraX) * tile;
            float y = (plus[1] + .9f - cameraY) * tile + (float) Math.sin(clock * 4 + plus[0]) * tile * .08f;
            float turned = Math.max(.18f, Math.abs((float) Math.cos(clock * 3 + plus[0] * .7f)));
            art(canvas, PLUS_ART, x, y, tile * .78f * turned, false);
        }
        // Whoever holds something out, where she waits: her friends, and those met in the villages, each beside what she has.
        for (int[] item : level.items) {
            if (item[3] == 0 || item[1] < first - 3 || item[1] > last + 3) continue;
            boolean given = item[0] != PlusRun.STALL && !run.items().contains(item), near = Math.abs(run.x() - item[1]) < 6;
            float x = (item[1] + 1.55f - cameraX) * tile, y = (item[2] + 1 - cameraY) * tile;
            int who = item[3];
            paint.setAlpha(255);
            if (who <= 2) {
                frame(canvas, cast(who - 1), given ? 2 : near ? 1 : 0, CAST_FRAMES[who - 1], x, y, tile * 1.62f, false);
            } else if (who == 3 || who == 4) {
                frame(canvas, cast(who == 3 ? TSUBAKIEH : ILLYA), 0, CAST_FRAMES[who == 3 ? TSUBAKIEH : ILLYA], x, y, tile * 1.62f, false);
            } else if (who == RunLevels.NURI) {
                Bitmap queen = face(10);
                float tall = tile * 1.7f, wide = tall * queen.getWidth() / queen.getHeight();
                target.set(x - wide / 2, y - tall, x + wide / 2, y);
                canvas.drawBitmap(queen, null, target, paint);
            } else {
                // A little hop when she comes near, and when it has been given.
                float hop = near && !given ? Math.abs((float) Math.sin(clock * 6)) * tile * .08f : 0;
                frame(canvas, villagers(), who - RunLevels.SOMI, VILLAGERS, x + (who == RunLevels.ALTTEUL ? tile * .5f : 0), y - hop, tile * 1.62f * .72f, false);
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
            }
        }
        // What lies about to help, in a ring of its own colour; what a friend holds out is in her hands already.
        for (int[] item : run.items()) {
            if (item[3] != 0 || item[1] < first - 1 || item[1] > last + 1) continue;
            if (item[0] == PlusRun.LEVER || item[0] == PlusRun.PLATE || item[0] == PlusRun.SHRINE) {
                drawStanding(canvas, item);
                continue;
            }
            if (item[0] == PlusRun.SPOT && item[4] >= 100) {
                drawBoard(canvas, item);
                continue;
            }
            if (item[0] == PlusRun.SPOT) {
                // A pet on show: sitting on a cushion; one of hers hops now and then, one she has not got has its price by it.
                int kind = item[4];
                float px = (item[1] + .5f - cameraX) * tile, feet = (item[2] + 1 - cameraY) * tile;
                boolean hers = (session.tally("run_pet_own") >> kind & 1) != 0, at = run.spot() == kind;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFE8A0B4);
                canvas.drawOval(px - tile * .5f, feet - tile * .16f, px + tile * .5f, feet + tile * .04f, paint);
                paint.setColor(Color.BLACK);
                float hop = hers && (int) (clock * 1.3f + kind) % 4 == 0 ? Math.abs((float) Math.sin(clock * 6)) * tile * .12f : 0;
                cell(canvas, petArt(kind), hop > 0 ? 5 : 0, 6, px, feet - tile * .08f - hop, tile * .95f * 170 / 100 * (at ? 1.12f : 1), false);
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(tile * .24f);
                text.setColor(hers ? 0xFF2E9E6E : Color.WHITE);
                text.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText(hers ? "hers" : "+ " + PET_COSTS[kind], px, feet + tile * .32f, text);
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
        for (PlusRun.Foe foe : run.foes()) {
            if (foe.x + foe.wide() < first - 9 || foe.x > last + 9) continue;
            float middle = (foe.x + foe.wide() / 2 - cameraX) * tile, feet = (foe.y + foe.tall() - cameraY) * tile;
            boolean second = (int) (clock * 6 + foe.x) % 2 != 0;
            // The pictures face left, and are turned about to go right.
            boolean turned = !foe.leftward && !foe.flat;
            if (RunLevels.boss(foe.kind)) {
                drawWaiting(canvas, foe, middle, feet, second);
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
            } else if (foe.kind >= RunLevels.SLIME) {
                // The signs of the second volume: two pictures of each about its business, and two of its going, after which it fades.
                int which = SIGN_ART + (foe.kind - RunLevels.SLIME) * 4 + (foe.flat ? (foe.act < .22f ? 2 : 3) : second ? 1 : 0);
                if (foe.flat && foe.act > 1.6f) continue;
                if (foe.flat) paint.setAlpha(Math.round(255 * Math.min(1, (1.6f - foe.act) / .5f)));
                else if (foe.dazed > 0 && (int) (clock * 16) % 2 == 0) paint.setAlpha(120);
                boolean stretched = foe.kind == RunLevels.BAR;
                if (stretched) piece(canvas, two, which, middle, feet + tile * .06f, foe.wide() * tile * 1.25f, turned);
                else pieceTall(canvas, two, which, middle, feet + tile * .04f, foe.tall() * tile * 1.3f, turned);
                paint.setAlpha(255);
            } else if (foe.kind == RunLevels.MINUS || foe.kind == RunLevels.THROWER) {
                boolean walking = foe.kind == RunLevels.MINUS && second;
                // (Down, it lies flat a moment and then fades, as everything beaten does.)
                if (foe.flat && foe.act > 2.4f) continue;
                if (foe.flat) paint.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (2.4f - foe.act) / .6f))));
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
                if (foe.act > 2.4f) continue;
                if (timesDown == null) timesDown = BitmapFactory.decodeResource(context.getResources(), R.drawable.run_times_down);
                float up = Math.min(1, foe.act / .45f), tall = foe.tall() * tile * 1.55f * 124 / 110;
                paint.setAlpha(Math.round(255 * Math.max(0, Math.min(1, (2.4f - foe.act) / .6f))));
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
                if (foe.flat && foe.act > 2.4f) continue;
                if (foe.flat) paint.setAlpha(Math.round(110 * Math.max(0, Math.min(1, (2.4f - foe.act) / .6f))));
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
                    pieceIn(canvas, three, BAT_ART, x, y, tile * 1.05f, spin * 1.4f);
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
        drawHer(canvas);
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
    private static int mixed(int one, int other, float much) {
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
                door(canvas, known ? picture : CRACK_PICTURE, one[1], one[2], at == index, known ? "Enter" : "Break");
                // Where a passage comes out looks like a crack too, until she has come out of it.
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
                    ? "Mini game: the range" : kind == RunLevels.TRAMPLE ? "Mini game: flatten them" : kind == RunLevels.STASH ? LOOK_NAMES[Math.max(0, one[13])] + ": a stash"
                    : LOOK_NAMES[Math.max(0, one[13])] + ": a way through";
            String words = kind == RunLevels.ARENA ? (one[10] != 0 ? "An arena. Each wave is different; something sleeps under the last." : "An arena. Each wave is different; lose a life and the wave starts again.")
                    : kind == RunLevels.GAME ? "10 seconds. Catch " + PlusRun.ENOUGH + " for the prize." : kind == RunLevels.RANGE
                    ? "12 seconds, and an ink gun that never empties. Hit " + PlusRun.ENOUGH + "." : kind == RunLevels.TRAMPLE
                    ? "15 seconds. They keep coming: flatten " + PlusRun.ENOUGH + "." : kind == RunLevels.STASH ? (one[14] == RunLevels.HOLDS_PET ? "A pet waits in it, by its chest." : one[14] == RunLevels.HOLDS_SHRINE ? "A shrine stands by its chest." : "Find the way through to its chest.")
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
            if (kind >= RunLevels.GUARD) {
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
        if (who >= FIRST_OTHER) {
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
        if (beaming) weapon = PlusRun.TABLET;
        if (weapon >= PlusRun.TABLET) {
            int pose = beaming ? (run.beam() > 0 ? MAIN_HEAVY : MAIN_GATHER) : sinceHeavy < .3f ? MAIN_HEAVY : sinceHeavy < .5f ? MAIN_SETTLE
                    : run.heldFor() > .12f ? MAIN_GATHER : sinceUse < .09f ? MAIN_BACK : sinceUse < .26f ? MAIN_GO
                    : !run.onGround() ? (run.speedDown() < 3 ? ARMED_RISE : ARMED_FALL) : run.running() ? ARMED_RUN + (int) (clock * 11) % 4 : 0;
            frame(canvas, herArmed(weapon), pose, MAIN_FRAMES, feetX, feetY, tall, leftward);
        } else if (weapon != PlusRun.UNARMED) {
            int pose = sinceUse < .22f || run.swinging() >= 0 ? ARMED_USE : !run.onGround() ? (run.speedDown() < 3 ? ARMED_RISE : ARMED_FALL)
                    : run.running() ? ARMED_RUN + (int) (clock * 11) % 4 : 0;
            frame(canvas, herArmed(weapon), pose, ARMED_FRAMES, feetX, feetY, tall, leftward);
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

    private void drawPet(Canvas canvas, PlusRun.Pet it) {
        int kind = it.kind;
        float x = (it.x - cameraX) * tile, y = (it.y - cameraY) * tile, across = it.x - it.seenX, down = it.y - it.seenY;
        it.seenX = it.x;
        it.seenY = it.y;
        boolean busy = it.busy > 0, floats = PlusRun.floats(kind);
        int pose = busy ? 4 : petGlad < .7f ? 5 : floats ? (Math.abs(across) > .05f ? 3 : (int) (clock * 5) % 2)
                : Math.abs(down) > .03f ? 3 : Math.abs(across) > .01f ? 1 + (int) (clock * 9) % 2 : 0;
        float tall = tile * .95f * 170 / 100, hop = petGlad < .7f ? Math.abs((float) Math.sin(petGlad / .7f * Math.PI * 2)) * tile * .25f : 0;
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
        float beat = .5f + .5f * (float) Math.sin(clock * 6), round = Math.min(button.width(), button.height()) / 2;
        stroke.setColor(0xFFFFD24A);
        stroke.setAlpha(Math.round(120 + 135 * beat));
        stroke.setStrokeWidth(dp(3) + dp(2) * beat);
        canvas.drawCircle(button.centerX(), button.centerY(), round + dp(4) + dp(5) * beat, stroke);
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
        source.set(index * wide, 0, index * wide + wide, sheet.getHeight());
        target.set(feetX - wide * scale / 2, feetY - sheet.getHeight() * scale, feetX + wide * scale / 2, feetY);
        canvas.save();
        if (turned) canvas.scale(-1, 1, feetX, feetY);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
    }

    /** A white card, as the companion's own popups are, with a soft edge to lift it off the screen. */
    private void plate(Canvas canvas, RectF where, float radius, int alpha) {
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
        // Her hearts, her lives and her pluses, in a plate at the top. The hearts are hearts, and empty by halves; the lives are her own
        // face and a count beside it, so that the two are never taken for each other.
        int hearts = run.maxHearts(), halves = run.halves();
        float tall = dp(44), margin = dp(10), heart = hearts > 5 ? dp(15) : hearts > 3 ? dp(18) : dp(22);
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        String count = "+ " + run.collected() + " of " + run.total(), lives = "× " + run.lives();
        float livesWide = dp(26) + text.measureText(lives) + dp(10);
        box.set(safe.left + margin, safe.top + margin, safe.left + margin + dp(20) + heart * hearts + dp(8) + livesWide + text.measureText(count) + dp(16),
                safe.top + margin + tall);
        float plateLeft = box.left, plateMiddle = box.centerY();
        plate(canvas, box, tall / 2, 235);
        for (int index = 0; index < hearts; index++) {
            float middle = plateLeft + dp(14) + heart * (index + .5f), bottom = plateMiddle + heart * .42f;
            int has = Math.max(0, Math.min(2, halves - index * 2));
            // Down to the last of them, they beat.
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
        canvas.drawText(count, livesAt + livesWide, plateMiddle + dp(5.5f), text);
        plate(canvas, pauseButton, tall / 2, 235);
        paint.setColor(ink);
        canvas.drawRoundRect(pauseButton.centerX() - dp(7), pauseButton.centerY() - dp(8), pauseButton.centerX() - dp(2.5f), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        canvas.drawRoundRect(pauseButton.centerX() + dp(2.5f), pauseButton.centerY() - dp(8), pauseButton.centerX() + dp(7), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        paint.setColor(Color.BLACK);
        float under = safe.top + margin + tall + dp(8), round = dp(36), x = safe.left + margin;
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
        // Something won, named in a small bright card at the top left, under whatever is there already: it slides in, stays a moment,
        // and goes by itself. What it is flies to the card from where she won it. Its rim says how much of a find it is: plain, gold, or
        // violet with a sparkle.
        if (toastLeft > 0 && toastTitle != null) {
            float fade = Math.min(1, toastLeft / .35f), in = Math.min(1, (TOAST - toastLeft) / .22f), high = dp(46);
            text.setTextSize(dp(14));
            float title = text.measureText(toastTitle);
            text.setTextSize(dp(11.5f));
            float wide = Math.min(safe.width() - margin * 2 - (run.pet() >= 0 ? dp(50) : 0), Math.max(title, text.measureText(toastWords)) + dp(66)), from = safe.left + margin - (1 - in) * dp(46);
            int rim = toastRarity == 2 ? 0xFF8E5BE8 : toastRarity == 1 ? 0xFFE0A21A : 0xFFB9BEC2;
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
            if (toastPicture == -HER_PICTURE) round(canvas, 0, atX, atY, size * .5f);
            else if (toastPicture < 0) prizePicture(canvas, -toastPicture, atX, atY, size);
            else pieceIn(canvas, four, toastPicture, atX, atY, size, 0);
            paint.setAlpha(255);
            under += high + dp(6);
        }
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
            canvas.drawText(gone ? "Gone" : "+ " + STALL_COSTS[index], box.centerX(), box.centerY() + dp(5), text);
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

    private List<String> wrap(String words, float wide) {
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
    private void fitted(Canvas canvas, String words, float x, float y, float size, float room) {
        text.setTextSize(size);
        float wide = text.measureText(words);
        if (wide > room) text.setTextSize(size * room / wide);
        canvas.drawText(words, x, y, text);
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
        String chapter = RunStory.CHAPTERS[RunLevels.chapterOf(number)];
        String kept = "kept + " + banked + (questLine != null ? "  ·  " + questLine : "");
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
        int boss = RunLevels.bossOf(number);
        String title = boss == RunLevels.ZERO ? "Zero is flat!" : boss == RunLevels.ILLYA ? "Illya gives way" : boss == RunLevels.TSUBAKIEH ? "The bat is broken"
                : boss == RunLevels.GOKHAE ? "Gokhae folds her fan" : boss == RunLevels.MIRI ? "The Gazette is corrected" : boss == RunLevels.YEOBAEK ? "Yeobaek naps"
                : boss == RunLevels.KING ? "King Slime is a puddle" : boss == RunLevels.MAWANG ? "The Mawang comes home" : boss == RunLevels.GUARD ? "Exam passed"
                : boss == RunLevels.BUTLER ? "The butler bows out" : boss == RunLevels.BYEORU ? "Written back in" : boss == RunLevels.JUPAN ? "The toll is refunded"
                : boss == RunLevels.SUMBI ? "Sumbi comes up for air" : boss == RunLevels.PADS ? "The brakes are off" : boss == RunLevels.PARTY ? "Straight home they go"
                : "Land " + number + " cleared";
        float prizeTall = prizeWon != RunLevels.NOTHING ? dp(78) : 0;
        float top = card(canvas, title, "+ " + run.collected() + " of " + run.total() + "  ·  " + PlayStage.clock(Math.max(1, Math.round(run.time())))
                + "  ·  " + kept, star + dp(10) + prizeTall);
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
            boolean lit = which == ONE_AT_A_TIME ? oneAtATime : which == ALL_I_CAN ? false : !oneAtATime && which != PlusRun.PRISM && ticks == PlusRun.mixOf(which);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(lit ? CalculatorLayout.CORAL : dark ? 0xFF3A3F45 : 0xFFF0ECE6);
            canvas.drawRoundRect(where, chipHigh / 2, chipHigh / 2, paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(lit ? Color.WHITE : ink);
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
    private void heading(Canvas canvas, String title, String under) {
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(26));
        canvas.drawText(title, safe.left + dp(18), safe.top + dp(42), text);
        text.setColor(soft);
        fitted(canvas, under, safe.left + dp(18), safe.top + dp(66), dp(14.5f), safe.width() - dp(36));
        plate(canvas, closeButton, closeButton.height() / 2, 235);
        stroke.setColor(ink);
        stroke.setStrokeWidth(dp(2));
        float arm = dp(6.5f);
        canvas.drawLine(closeButton.centerX() - arm, closeButton.centerY() - arm, closeButton.centerX() + arm, closeButton.centerY() + arm, stroke);
        canvas.drawLine(closeButton.centerX() + arm, closeButton.centerY() - arm, closeButton.centerX() - arm, closeButton.centerY() + arm, stroke);
    }

    private void drawMap(Canvas canvas) {
        int open = open();
        drawSky(canvas, RunLevels.chapterOf(open), clock * dp(5));
        canvas.drawColor(dark ? 0x99000000 : 0x66FFFFFF);
        int earned = 0;
        int all = open > RunLevels.COUNT ? RunLevels.TOTAL : RunLevels.COUNT;
        for (int land = 1; land <= all; land++) earned += starsOf(land);
        int guild = RunHunts.rank(session.tally("run_hunts"));
        heading(canvas, "Plus run", RunStory.TITLE + "  ·  " + earned + " of " + all * 3 + " stars" + (guild > 0 ? "  ·  " + RunHunts.RANK_NAMES[guild - 1] + " badge" : ""));
        if (guild > 0) {
            // The badge of her rank in the guild, kept: by the name, where there is room for it.
            text.setTextSize(dp(26));
            float after = safe.left + dp(18) + text.measureText("Plus run") + dp(20);
            if (after + dp(14) < shopButton.left) badge(canvas, after, safe.top + dp(29), dp(22), guild);
        }
        if (open > RunLevels.COUNT) {
            for (int volume = 0; volume < 2; volume++) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(volume == page ? CalculatorLayout.CORAL : dark ? 0xFF2A2E33 : 0xFFFFFFFF);
                paint.setAlpha(volume == page ? 255 : 220);
                canvas.drawRoundRect(tabs[volume], tabs[volume].height() / 2, tabs[volume].height() / 2, paint);
                paint.setAlpha(255);
                paint.setColor(Color.BLACK);
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(volume == page ? Color.WHITE : ink);
                fitted(canvas, volume == 0 ? "Volume One" : RunStory.SEQUEL, tabs[volume].centerX(), tabs[volume].centerY() + dp(5), dp(14), tabs[volume].width() - dp(16));
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
        int firstShown = page == 0 ? 0 : RunLevels.COUNT, lastShown = page == 0 ? RunLevels.COUNT : RunLevels.TOTAL;
        int each = page == 0 ? RunLevels.IN_CHAPTER : RunLevels.IN_LATER_CHAPTER;
        for (int index = firstShown; index < lastShown; index++) {
            RectF where = lands[index];
            int land = index + 1, got = starsOf(land);
            if ((index - firstShown) % each == 0) {
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(land <= open ? ink : soft);
                text.setTextSize(dp(14));
                canvas.drawText(RunStory.CHAPTERS[RunLevels.chapterOf(land)], where.left, where.top - dp(8), text);
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
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(ink);
        fitted(canvas, shopButton.width() < dp(96) ? "Counter" : "Guild counter", shopButton.centerX(), shopButton.top + dp(15.5f), dp(12), shopButton.width() - dp(20));
        text.setColor(CalculatorLayout.CORAL);
        fitted(canvas, "+ " + bank() + "  ›", shopButton.centerX(), shopButton.bottom - dp(6.5f), dp(13), shopButton.width() - dp(20));
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
                    : starsOf(RunLevels.TOTAL) > 0 ? "We read both volumes to the end. Again Sloppy~♡"
                    : open == 1 ? "Pick the first land. Go on Sloppy~♡" : open == RunLevels.COUNT + 1 ? "Volume Two fell on us. Land 31 Sloppy~♡"
                    : waiting == RunLevels.TSUBAKIEH ? "Tsubakieh and her bat are in land " + open + " Sloppy~♡"
                    : waiting == RunLevels.ZERO ? "The Last Page. Zero is waiting Sloppy~♡"
                    : waiting >= 0 && session.tally("run_prize_" + open) == 0
                    ? waitName(waiting) + (waiting == RunLevels.MIRI ? " have " : " has ") + prizeName(RunLevels.prizeOf(open)).toLowerCase(java.util.Locale.ROOT).replace("zero", "Zero")
                    + " to win, in land " + open + " Sloppy~♡"
                    : waiting >= 0 ? waitName(waiting) + (waiting == RunLevels.MIRI ? " are" : " is") + " waiting in land " + open + " Sloppy~♡"
                    : "Land " + open + " is waiting Sloppy~♡");
            text.setTextSize(dp(14));
            text.setTextAlign(Paint.Align.LEFT);
            // Her words are made smaller if that is what it takes to fit them beside her.
            float wide = Math.min(text.measureText(words), safe.right - middle - tall * .5f - dp(40));
            box.set(middle + tall * .5f, feet - tall * .62f, middle + tall * .5f + wide + dp(24), feet - tall * .62f + dp(36));
            float bubbleLeft = box.left, bubbleMiddle = box.centerY();
            plate(canvas, box, dp(14), 245);
            text.setColor(ink);
            fitted(canvas, words, bubbleLeft + dp(12), bubbleMiddle + dp(5), dp(14), wide);
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
        // (With her pets under them, the four that are for her pets are lower rows.)
        float squeeze = counterTab == PETS_TAB ? Math.min(wares[0].height(), dp(52)) : wares[0].height();
        for (int row = 0; row < list.length; row++) {
            RectF where = wares[row];
            where.set(where.left, safe.top + dp(142) + row * (squeeze + dp(6)), where.right, safe.top + dp(142) + row * (squeeze + dp(6)) + squeeze);
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
        float under = safe.top + dp(142) + list.length * (squeeze + dp(6)) + dp(6), left = safe.centerX() - across / 2;
        petAct.setEmpty();
        petTreat.setEmpty();
        armsAct.setEmpty();
        if (counterTab == PETS_TAB) drawPetShop(canvas, left, under, across, bank);
        else if (counterTab == COLOURS_TAB) drawMixHints(canvas, left, under, across);
        else if (counterTab == ARMS_TAB) {
            // What she fights with is worked on by the smith, in the towns; here is the way to her backpack.
            box.set(left, under, left + across, under + dp(116));
            plate(canvas, box, dp(16), 245);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, "What she fights with", left + dp(16), under + dp(26), dp(16), across - dp(32));
            text.setColor(soft);
            fitted(canvas, "What she finds in the lands is kept in her backpack.", left + dp(16), under + dp(46), dp(12.5f), across - dp(32));
            fitted(canvas, "The smith, in the towns, will work on each of them.", left + dp(16), under + dp(63), dp(12.5f), across - dp(32));
            armsAct.set(left + dp(16), under + dp(74), left + across - dp(16), under + dp(108));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(CalculatorLayout.CORAL);
            canvas.drawRoundRect(armsAct, dp(17), dp(17), paint);
            paint.setColor(Color.BLACK);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.WHITE);
            fitted(canvas, "Open her backpack  \u203A", armsAct.centerX(), armsAct.centerY() + dp(5), dp(14.5f), armsAct.width() - dp(24));
        } else if (counterTab == 1) {
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(soft);
            fitted(canvas, "Her wings are what there is to buy for how she moves, so far.", left + dp(6), under + dp(14), dp(12.5f), across - dp(12));
        }
        text.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * Her pets at the counter: the ten kinds in two rows of five. One she has is bright,
     * one that comes along with her is ringed, and one she has not got has its price under
     * it. Under them, the one that is looked at: what it is, what it does, and the button
     * that buys it, takes it along or leaves it at home.
     */
    private void drawPetShop(Canvas canvas, float left, float top, float across, int bank) {
        int own = session.tally("run_pet_own"), with = session.tally("run_pet_with") & own, places = 1 + bought(PET_PLACES);
        float gap = dp(6), side = Math.min(dp(64), (across - gap * 4) / 5), high = side + dp(14);
        for (int kind = 0; kind < PlusRun.PETS; kind++) {
            if (petCells[kind] == null) petCells[kind] = new RectF();
            RectF at = petCells[kind];
            float x = left + (across - (side * 5 + gap * 4)) / 2 + kind % 5 * (side + gap), y = top + kind / 5 * (high + gap);
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
        float y = top + 2 * (high + gap) + dp(4);
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
    }

    /** Under her colours: the mixes that have names. One she has made is named, with what it does; one she has not is hinted at: one of its colours, and a question mark. */
    private void drawMixHints(Canvas canvas, float left, float top, float across) {
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
    }
}
