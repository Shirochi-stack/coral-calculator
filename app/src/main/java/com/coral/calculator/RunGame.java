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
    private static final float[] WEAPON_LONG = {.78f, .64f, .52f, .98f, 1.08f, .56f};
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
            R.drawable.run_sumbi, R.drawable.run_pads, R.drawable.run_party, R.drawable.run_oldbook};
    private static final int[] LATER_FRAMES = {4, 3, 3, 6, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4};
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
            R.drawable.halgakos_arm3, R.drawable.halgakos_arm4, R.drawable.halgakos_arm5};
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
    private static final String[] UP_NAMES = {"Chikuwa's lunch box", "Wings", "Glossary magnet", "Longer treats", "Bigger ink tank",
            "Flag picnic", "Thicker ink", "Spare bookmarks", "Prism", "Quick ribbons"};
    private static final String[][] UP_GIVES = {
            {"A fourth heart in every land", "A fifth heart in every land"},
            {"A second jump in the air: press jump again"},
            {"Pluses close by come to her", "Pluses come to her from further off"},
            {"What she picks up lasts half as long again", "What she picks up lasts twice as long"},
            {"The ink gun holds 24 shots", "The ink gun holds 32 shots"},
            {"Every flag she passes gives a heart back"},
            {"A ball of Rune's ink takes two hurts"},
            {"A fourth life in every land", "A fifth life in every land", "A sixth life in every land"},
            {"Three colours worn at once", "Four colours worn at once", "Five colours worn at once", "All seven at once: white"},
            {"A colour is ready a second sooner", "A colour is ready two seconds sooner", "A colour is ready three seconds sooner"}};
    private static final int[][] UP_COSTS = {{120, 320}, {200}, {90, 240}, {80, 220}, {70, 180}, {260}, {160}, {150, 300, 480}, {200, 350, 500, 900},
            {120, 240, 400}};
    /** How many colours must be hers before each step of the prism is for sale: the last, all seven. */
    private static final int[] PRISM_NEEDS = {3, 4, 5, 7};
    private static final int PRISM = 8, QUICK = 9;
    /** The mixes that have names, as {@link PlusRun} numbers them: what each is called, what it is made of, and what it does. */
    private static final String[] MIX_NAMES = {"Steam", "Comet", "Tide", "Mirror", "Gale", "Ember", "Prism", "White"};
    private static final String[] MIX_OF = {"Red + Blue", "Red + Yellow", "Blue + Green", "Brown + Purple", "Green + Yellow", "Red + Brown", "Any three", "All seven"};
    private static final String[] MIX_DOES = {"a cloud that goes through them, and a jump more in the air", "at a run she goes through whatever she meets",
            "she comes down slowly, and water goes both ways", "what is sent back goes on through a row", "she rises much faster",
            "what she knocks over flies off burning", "whatever lands on her lands by half", "every power at once, and she glows"};
    /** The order they stand in at the counter: lives under hearts. And the picture that stands for her own face. */
    private static final int[] UP_ORDER = {0, 7, 8, 9, 1, 2, 3, 4, 5, 6};
    private static final int HER_FACE = -2;
    private static final int[] UP_ART = {BUFF_ART + PlusRun.BREAD, -1, BUFF_ART + PlusRun.MAGNET, BUFF_ART + PlusRun.ICE, WEAPON_ART + PlusRun.GUN,
            FLAG_ART, BUFF_ART + PlusRun.SHIELD, HER_FACE, STAR_ART, STAR_ART};
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
    private Bitmap villagers, dyedPet, dyedJump, dyedLose, wasPet, wasJump, wasLose;
    private Bitmap[] wasArmed = new Bitmap[ARMED.length];
    /** Which form the dyed pictures of her are of, which the ones before them were of, and whether more are being made. */
    private int dyedForm, wasForm;
    private boolean dyeing;
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
    private int ribbonFinger = -1, fanWant, fanDrop, fanOver = -1, seenMixings, seenFaded, seenWhitened, mixShown = -1;
    private float ribbonHeld, sinceStep = 9, mixAge = 9;
    private boolean fanOpen, hintMix;
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
    private final float[] artWide = new float[ART_COUNT], artTall = new float[ART_COUNT];
    private Bitmap sky;
    private int skyOf = -1;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF target = new RectF(), safe = new RectF(), box = new RectF();
    private final RectF leftButton = new RectF(), rightButton = new RectF(), jumpButton = new RectF(), strikeButton = new RectF();
    private final RectF pauseButton = new RectF(), closeButton = new RectF(), soundButton = new RectF(), shopButton = new RectF();
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
    private int seenGreetings, seenEntered, seenReveals, seenWins;
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
    private int roomCard = -1;
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
        kept.colours = colours();
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
        return prize == PlusRun.BLACK ? "an ordinary girl, slow, who can hurt nothing" : prize > 0 ? FORM_GIVES[prize - 1]
                : prize == RunLevels.RING ? "every form lasts half as long again" : prize == RunLevels.HEART ? "one more heart in every land"
                : prize == RunLevels.TANK ? "an ink gun holds eight more shots" : prize == RunLevels.QUICK ? "her colours are ready three seconds sooner"
                : "two hundred pluses, kept";
    }

    /** Says in a card of its own that something has been won: at once, or when the card that is on the screen has gone. */
    private void toast(String title, String words, int picture) {
        // One card at a time: behind the one that is up, or behind the card of a place off the land.
        if (toastLeft > .4f || roomCardAge >= 0 || roomResultAge >= 0) {
            toasts.add(new Object[]{title, words, picture});
            return;
        }
        toastTitle = title;
        toastWords = words;
        toastPicture = picture;
        toastLeft = 3.6f;
        audio.play("star", .7f);
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
     * The colour she is to be drawn in now: her form, if the pictures of her in it are ready.
     * If they are not, they are begun, away from the screen, and until then she is drawn as
     * she was: in the colour before, if she was in one, so that it can fade into the new.
     */
    private int formDrawn() {
        int form = lookWanted();
        if (form == 0 || form == dyedForm) return form;
        if (!dyeing) {
            dyeing = true;
            Bitmap pet = this.pet, jump = this.jump, lose = this.lose;
            new Thread(() -> {
                Bitmap first = dyed(pet, form), second = dyed(jump, form), third = dyed(lose, form);
                stage.post(() -> {
                    wasPet = dyedPet;
                    wasJump = dyedJump;
                    wasLose = dyedLose;
                    wasArmed = dyedArmed.clone();
                    wasForm = dyedForm;
                    dyedPet = first;
                    dyedJump = second;
                    dyedLose = third;
                    java.util.Arrays.fill(dyedArmed, null);
                    dyedForm = form;
                    dyeing = false;
                });
            }).start();
        }
        return look == dyedForm ? look : 0;
    }

    /**
     * How she is to look: in her own hair, 0; in one colour, that colour, by its number;
     * in white, white; under black that was put on her, black; and wearing several, their
     * blend, as a colour in itself with every bit of its top byte set.
     */
    private int lookWanted() {
        if (run == null) return 0;
        if (run.cursed() > 0) return PlusRun.BLACK;
        if (run.white()) return PlusRun.WHITE;
        int mix = run.mix();
        if (mix == 0) return 0;
        if (Integer.bitCount(mix) == 1) return Integer.numberOfTrailingZeros(mix) + 1;
        int red = 0, green = 0, blue = 0, count = Integer.bitCount(mix);
        for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) {
            if ((mix & 1 << colour - 1) == 0) continue;
            red += Color.red(FORM_COLOURS[colour - 1]);
            green += Color.green(FORM_COLOURS[colour - 1]);
            blue += Color.blue(FORM_COLOURS[colour - 1]);
        }
        return 0xFF000000 | red / count << 16 | green / count << 8 | blue / count;
    }

    /** The colour itself of a way she may look. */
    private static int lookColour(int look) { return look == 0 ? CalculatorLayout.CORAL : look > 0 && look <= PlusRun.WHITE ? FORM_COLOURS[look - 1] : look; }

    /** The colour a picture of her is to be drawn in at this moment. */
    private int drawn() { return drawnAs != NO_LOOK ? drawnAs : run == null || screen == MAP || screen == SHOP ? 0 : look; }

    private Bitmap herPet() {
        int form = drawn();
        return form != 0 && form == dyedForm ? dyedPet : form != 0 && form == wasForm && wasPet != null ? wasPet : pet;
    }

    private Bitmap herJump() {
        int form = drawn();
        return form != 0 && form == dyedForm ? dyedJump : form != 0 && form == wasForm && wasJump != null ? wasJump : jump;
    }

    /** The sheet of her hurt, thrown and fallen, in a colour: so that she does not lose in pink what she was wearing in red. */
    private Bitmap herLose(int form) {
        return form != 0 && form == dyedForm && dyedLose != null ? dyedLose : form != 0 && form == wasForm && wasLose != null ? wasLose : lose;
    }

    private Bitmap herArmed(int weapon) {
        int form = drawn();
        if (form != 0 && form == dyedForm) {
            if (dyedArmed[weapon] == null) dyedArmed[weapon] = dyed(armed(weapon), form);
            return dyedArmed[weapon];
        }
        if (form != 0 && form == wasForm) {
            if (wasArmed[weapon] == null) wasArmed[weapon] = dyed(armed(weapon), form);
            return wasArmed[weapon];
        }
        return armed(weapon);
    }

    /** Who waits at the end of a land, by her colour, her name, and where she is among those who speak (below nothing for Zero and King Slime, who do not). */
    private static int waitColour(int kind) {
        return kind == RunLevels.ZERO ? CalculatorLayout.CORAL : kind == RunLevels.KING ? 0xFF3FA878 : SPEAKER_COLOURS[waitFace(kind)];
    }

    private static String waitName(int kind) {
        return kind == RunLevels.ZERO ? "Zero" : kind == RunLevels.ILLYA ? "Illya" : kind == RunLevels.TSUBAKIEH ? "Tsubakieh"
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
        // The map: a page for each volume, with a row of lands for each of its chapters under the chapter's name, and the counter under them all.
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
        float tab = Math.min(dp(132), (safe.width() - dp(44)) / 2);
        tabs[0].set(safe.left + dp(18), safe.top + dp(80), safe.left + dp(18) + tab, safe.top + dp(114));
        tabs[1].set(tabs[0].right + dp(8), tabs[0].top, tabs[0].right + dp(8) + tab, tabs[0].bottom);
        turnTo(page);
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
        float high = Math.max(dp(40), Math.min(dp(78), (safe.height() - dp(120)) / wares.length - dp(8)));
        float across = Math.min(safe.width() - dp(28), dp(460));
        for (int index = 0; index < wares.length; index++) {
            float down = safe.top + dp(100) + index * (high + dp(8));
            wares[index].set(safe.centerX() - across / 2, down, safe.centerX() + across / 2, down + high);
        }
    }

    /** Shows the lands of a volume, with the counter under the last of them. */
    private void turnTo(int volume) {
        page = volume;
        int from = volume == 0 ? 0 : RunLevels.COUNT, last = volume == 0 ? RunLevels.COUNT - 1 : RunLevels.TOTAL - 1;
        float under = lands[last].bottom + dp(16);
        shopButton.set(lands[from].left, under, lands[last].right, under + dp(48));
    }

    void close() { audio.close(); }

    /** Left for the home screen: a run stops on its pause, and whoever was speaking stops, so that it can be come back to as it stands. */
    void hold() {
        if (screen == PLAY) pause();
        audio.hush();
    }

    /** The Back key: out of a run to its pause, out of the story, the counter or a card to the map; false to leave the game. */
    boolean back() {
        if (screen == PLAY) {
            pause();
        } else if (screen == PAUSED) {
            screen = PLAY;
        } else if (screen == STORY) {
            endStory();
        } else if (screen == STALL) {
            screen = PLAY;
        } else if (screen == SHOP && counterFrom == STALL) {
            screen = STALL;
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
        // A line that is still being said is not begun again.
        if (captioned && words[0].equals(saying) && (captionLeft > 0 || audio.speaking())) return;
        saying = words[0];
        String said = who == 0 ? names.named(words[1]) : words[1];
        if (captioned) {
            caption = said;
            captionWho = who;
            captionLeft = CAPTION + said.length() * .025f;
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
        if (personLeft <= 0) speak(words, true);
    }

    /** A passing remark, made only if nothing else is being said. */
    private void remark(String[][] lines) {
        if (captionLeft <= 0 && personLeft <= 0 && !audio.speaking()) speakAny(lines);
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
        if (intro != null && untold("run_intro2", land)) {
            begin(land, false);
            tell(intro, RunLevels.chapterOf(land), () -> screen = PLAY);
        } else {
            begin(land, true);
        }
    }

    private void begin(int land, boolean greeted) {
        number = land;
        perks = kept();
        // For looking at what losing looks like in a build made for testing: one heart and one life.
        if (BuildConfig.DEBUG && session.tally("run_frail") > 0) {
            perks.hearts = 1;
            perks.lives = 1;
        }
        run = new PlusRun(RunLevels.level(land), perks);
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
        prizeAge = -1;
        prizeWon = RunLevels.NOTHING;
        saidRumour = false;
        seenLives = seenCasts = seenBraces = seenPaid = seenReminders = seenChests = 0;
        roomCardAge = roomResultAge = -1;
        tickedAt = 0;
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
            pills(number < RunLevels.TOTAL ? number == RunLevels.COUNT ? RunStory.SEQUEL : "Next" : "The end", NEXT, "Again", AGAIN, "Lands", TO_MAP);
        } else {
            stars = 0;
            session.keep("run_lost", session.tally("run_lost") + 1);
            session.keep("run_mobs", session.tally("run_mobs") + run.flattened());
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
        if (screen == PLAY && ribbonFinger >= 0 && !fanOpen && (ribbonHeld += seconds) >= .3f && Integer.bitCount(perks.colours & 127) > 1) {
            fanOpen = true;
            fanWant = fanDrop = 0;
            fanOver = -1;
            audio.play("page", .5f);
            buzz(true);
        }
        if (toastLeft <= 0 && !toasts.isEmpty() && roomCardAge < 0 && roomResultAge < 0) {
            Object[] next = toasts.remove(0);
            toast((String) next[0], (String) next[1], (Integer) next[2]);
        }
        // While what was won from whoever fell is rising, everything else waits.
        if (screen == PLAY && prizeAge >= 0) {
            if ((prizeAge += seconds) > 5) prizeAge = -1;
        } else if (screen == PLAY && fanOpen) {
            // The land waits while she chooses.
        } else if (screen == PLAY && roomCardAge >= 0 && roomCardAge < 2.4f && roomHolds()) {
            roomCardAge += seconds;
        } else if (screen == PLAY && roomResultAge >= 0 && roomResultAge < 2.6f) {
            roomResultAge += seconds;
        } else if (screen == PLAY && handAge >= 0) {
            handAge += seconds;
            // The seal comes down on it.
            if (handAge > 2 && !handStamped) {
                handStamped = true;
                audio.play("clang", .5f);
                buzz(true);
            }
            // When whoever thanked her has finished, Halgakos has her say; and then it is over.
            if (handAge > 2.4f && !handSaid && (!audio.speaking() || handAge > 5)) {
                handSaid = true;
                speak(RunStory.PRIZES[handPicture == CAKE_PICTURE ? 2 : 1], true);
            }
            if (handAge > 7.5f || handSaid && handAge > 4 && !audio.speaking() && captionLeft <= 0) handAge = -1;
        } else if (screen == PLAY) {
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

    private void step(float seconds) {
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
        // White of her own seven colours: that is white won, and kept where her settings can see it.
        if (run.whitened() != seenWhitened) {
            seenWhitened = run.whitened();
            if ((colours() & 1 << PlusRun.WHITE - 1) == 0) {
                session.keep("run_colours", colours() | 1 << PlusRun.WHITE - 1);
                toast("White is hers", "All seven worn at once. Hers in the assistant's settings too.", -PlusRun.WHITE);
            }
            flash = .5f;
            audio.play("win", .8f);
            speak(RunStory.MIX[2], true);
        }
        if (hintMix && run.time() > 1.2f && captionLeft <= 0 && personLeft <= 0) {
            hintMix = false;
            session.keep("run_hint_mix", 1);
            speak(RunStory.MIX[0], true);
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
            if (found >= 0 && !remarked[found] && personLeft <= 0) {
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
        for (PlusRun.Foe foe : run.foes()) if (RunLevels.boss(foe.kind) && !foe.spawned && (boss == null || boss.flat)) boss = foe;
        for (PlusRun.Foe foe : run.foes()) if (RunLevels.boss(foe.kind) && foe.spawned && run.room() >= 0) boss = foe;
        if (run.woken() != seenWoken && boss != null) {
            seenWoken = run.woken();
            audio.play("page", .8f);
            buzz(true);
            speak(RunStory.ENTER[RunStory.waiting(boss.kind)], true);
        }
        // How whoever waits stands toward a blow: what kept blows off her breaks when she is spent, with a sound of its own; and the first
        // time anyone is met who has such a thing, a word about what it means.
        int guard = boss == null || boss.flat || !(run.locked() || boss.spawned) ? -1 : run.guard(boss);
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
                // Her word about the kind of place it is, the first time in a land; of a cellar or a game, only while it is still to do.
                if ((saidRoom & 1 << kind) == 0 && (kind <= RunLevels.PASSAGE || run.sealed())) {
                    saidRoom |= 1 << kind;
                    aside(RunStory.ROOMS[kind]);
                }
                // And a card that says what it is and what to do in it: before anything in it begins, where something is about to.
                if (todo || kind == RunLevels.PASSAGE) {
                    roomCard = in;
                    roomCardAge = 0;
                    tickedAt = 0;
                    audio.play(run.sealed() ? "star" : "page", .7f);
                }
            } else {
                roomCardAge = -1;
            }
            if (found) {
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
        // A chest opened: ten pluses in it. The one by a passage's far door has a purse in it besides, the first time; a stash's is the
        // doing of the stash, and is told with that.
        if (run.chests() != seenChests) {
            seenChests = run.chests();
            audio.play("unlock");
            buzz(true);
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
            if (kind == RunLevels.ARENA) {
                roomResult = won[10] != 0 ? "The Old Book is shut" : "Cellar cleared";
                roomScore = PlusRun.WAVES + " waves" + (won[10] != 0 ? ", and the Old Book" : "") + "  ·  + " + (won[10] != 0 ? 40 : 20);
                roomPicture = CELLAR_PICTURE;
                if (first) {
                    run.spare();
                    roomGave = "A life, there and then";
                    if (won[10] != 0) roomGave += piece();
                }
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
                    run.lend(PlusRun.WHITE);
                    session.keep("run_bank", bank() + 20);
                    roomGave = "The rare white ribbon, and + 20 to keep";
                } else if (!enough) {
                    roomGave = "Reach " + PlusRun.ENOUGH + " for the prize";
                }
            }
            roomResultAge = 0;
            roomCardAge = -1;
            idle();
            speak(RunStory.ROOM_WON[kind == RunLevels.ARENA ? 0 : kind == RunLevels.RANGE ? 2 : kind == RunLevels.TRAMPLE ? 3 : kind == RunLevels.STASH ? 4 : 1], true);
            session.keep("run_roomsdone_" + number, session.tally("run_roomsdone_" + number) | run.roomsDone());
            perks = kept();
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
                    run.lend(PlusRun.WHITE);
                    session.keep("run_bank", bank() + 30);
                    handTitle = "Galpi's white ribbon";
                    handWords = "Every power at once, there and then; and + 30 to keep";
                    handPicture = -PlusRun.WHITE;
                }
                medal(chapter, true);
                perks = kept();
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
            boolean fell = boss != null && (boss.spawned ? boss.flat : !run.standing());
            audio.play(fell ? "fall" : "blow");
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
                        if (chosen == 0 || given == PlusRun.BLACK) chosen = given;
                        session.keep("run_colour", chosen);
                    } else {
                        perks = kept();
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
                    speakAny(cause == PlusRun.FELL ? RunStory.FELL : cause == PlusRun.STRUCK ? RunStory.SHOT
                            : cause == PlusRun.SPIKED ? RunStory.SPIKED : cause == PlusRun.BATTED ? RunStory.BATTED : cause == PlusRun.BURNED ? RunStory.BURNED
                            : cause == PlusRun.SANK ? RunStory.SANK : cause == PlusRun.QUAKED ? RunStory.QUAKED : cause == PlusRun.SLASHED ? RunStory.SLASHED
                            : RunStory.BITTEN);
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
        if (screen == PLAY) {
            if (action == MotionEvent.ACTION_DOWN && pauseButton.contains(x, y)) {
                audio.play("tap", .5f);
                pause();
                return;
            }
            // The buttons over the arrows: one calls on the colour she has chosen, if what she calls on it with has filled; the other chooses the next of hers.
            // While something won is rising, a touch, once it has been seen, sends it on its way.
            if (prizeAge >= 0) {
                if (action == MotionEvent.ACTION_DOWN && prizeAge > 1.6f) prizeAge = -1;
                return;
            }
            if (handAge >= 0) {
                if (action == MotionEvent.ACTION_DOWN && handAge > 2.2f) handAge = -1;
                return;
            }
            // A card that holds the land is sent on by a touch, once it has been up long enough to be seen.
            if (roomCardAge >= 0 && roomCardAge < 2.4f && roomHolds()) {
                if (action == MotionEvent.ACTION_DOWN && roomCardAge > .5f) roomCardAge = 2.4f;
                return;
            }
            if (roomResultAge >= 0 && roomResultAge < 2.6f) {
                if (action == MotionEvent.ACTION_DOWN && roomResultAge > .8f) roomResultAge = 2.6f;
                return;
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
                if (counterFrom == STALL) screen = STALL;
                else toMap();
                return;
            }
            for (int index = 0; index < wares.length; index++) if (wares[index].contains(x, y)) buy(UP_ORDER[index]);
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

    /**
     * A touch on the ribbon, its arrow, or the colours opened out from it. The arrow is a
     * button of its own, clear of the ribbon, and steps to the next colour that is hers at
     * any time. The ribbon, let go of at once, calls on the colour it shows, which is added
     * to what she wears; held for a third of a second it opens out every colour that is
     * hers, and the land waits: sliding onto one picks it, to be put on if it is not on and
     * let go of if it is, and letting go of the screen does all of that at once. Another
     * finger may be on an arrow or the jump all the while. Returns whether the touch was
     * one of these and is done with.
     */
    private boolean colourTouch(MotionEvent event, int action) {
        int index = event.getActionIndex(), finger = event.getPointerId(index);
        float px = event.getX(index), py = event.getY(index);
        boolean down = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN;
        boolean up = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL;
        if (fanOpen) {
            // Whichever finger is on one of them picks it: the one that opened them, as it slides, or another, as it comes down.
            for (int each = 0; each < event.getPointerCount(); each++) {
                if (!down && event.getPointerId(each) != ribbonFinger) continue;
                if (down && each != index) continue;
                int over = -1;
                for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) {
                    if (fanCells[colour] != null && (perks.colours & 1 << colour - 1) != 0 && fanCells[colour].contains(event.getX(each), event.getY(each))) over = colour;
                }
                if (down && over >= 0) pick(over);
                else if (!down && over != fanOver) {
                    fanOver = over;
                    if (over >= 0) pick(over);
                }
            }
            if (up && (finger == ribbonFinger || action != MotionEvent.ACTION_POINTER_UP)) {
                // Let go of: what was picked is done, all at once.
                for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) if ((fanDrop & 1 << colour - 1) != 0) run.drop(colour);
                if (fanWant != 0 && run.callAll(fanWant) == 0) audio.play("no", .4f);
                fanOpen = false;
                ribbonFinger = -1;
                idle();
            }
            return true;
        }
        if (down && nextButton.contains(px, py)) {
            if (Integer.bitCount(perks.colours & 127) > 1) {
                do chosen = chosen % PlusRun.BLACK + 1;
                while ((perks.colours & 1 << chosen - 1) == 0);
                session.keep("run_colour", chosen);
                sinceStep = 0;
                audio.play("tap", .5f);
                buzz(false);
            }
            return true;
        }
        if (down && colourButton.contains(px, py)) {
            ribbonFinger = finger;
            ribbonHeld = 0;
            return true;
        }
        if (up && finger == ribbonFinger) {
            // Let go of before the colours opened out: a tap, which calls on the colour shown, with whatever she wears.
            ribbonFinger = -1;
            if (!run.call(chosen)) {
                audio.play("no", .4f);
                pop(run.x() + PlusRun.WIDE / 2, run.y() - .4f, run.on(chosen) > 0 ? "Already on" : run.untilCall(chosen) > 0
                        ? "Ready in " + (int) Math.ceil(run.untilCall(chosen)) + " s" : run.cursed() > 0 ? "Not under black" : "No more than " + run.together() + " at once");
            }
            return action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL;
        }
        return false;
    }

    /** One of the colours opened out is picked: on, to be let go of, or not; off and ready, to be put on, or not, if there is room for it. */
    private void pick(int colour) {
        int bit = 1 << colour - 1;
        if (run.on(colour) > 0) {
            fanDrop ^= bit;
        } else if (run.untilCall(colour) > 0 || run.cursed() > 0) {
            audio.play("no", .3f);
            return;
        } else if ((fanWant & bit) != 0) {
            fanWant &= ~bit;
        } else {
            int own = run.mix() & perks.colours & ~fanDrop;
            if (Integer.bitCount(own) + Integer.bitCount(fanWant) >= run.together()) {
                audio.play("no", .3f);
                return;
            }
            fanWant |= bit;
        }
        audio.play("tap", .5f);
        buzz(false);
    }

    /** Which buttons are held, by however many fingers are down: one may hold an arrow while another jumps. */
    private void held(MotionEvent event) {
        int action = event.getActionMasked();
        boolean left = false, right = false, up = false, strike = false;
        boolean carrying = run != null && (run.weapon() != PlusRun.UNARMED || run.has(PlusRun.RED) || run.has(PlusRun.BLUE));
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
                if (run != null && (run.atStall() || run.door() >= 0) && stallButton.contains(x, y)) continue;
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
    }

    // Drawing

    void draw(Canvas canvas) {
        if (width == 0) return;
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
            if (screen == PLAY && prizeAge >= 0) drawPrize(canvas);
            if (screen == PLAY && handAge >= 0) drawHandOver(canvas);
            drawHud(canvas);
            if (screen == PLAY && (roomCardAge >= 0 || roomResultAge >= 0)) drawRoomCards(canvas);
            if (screen == PLAY) {
                // Charmed, the arrows are the wrong way round, and show it.
                boolean charmed = run.charmed() > 0;
                control(canvas, leftButton, goLeft, 180, charmed);
                control(canvas, rightButton, goRight, 0, charmed);
                control(canvas, jumpButton, goJump, 270, false);
                if (perks.colours != 0) colourControl(canvas);
                if (run.weapon() != PlusRun.UNARMED || run.has(PlusRun.RED) || run.has(PlusRun.BLUE)) strikeControl(canvas);
                if (run.atStall()) {
                    stallControl(canvas);
                } else {
                    stallSign.setEmpty();
                    if (run.door() >= 0) doorControl(canvas);
                }
            } else if (screen == STALL) {
                canvas.drawColor(0x8C000000);
                drawStall(canvas);
            } else {
                canvas.drawColor(0x8C000000);
                if (screen == PAUSED) card(canvas, "Paused", RunStory.CHAPTERS[RunLevels.chapterOf(number)] + ", land " + number, 0);
                else drawResult(canvas);
            }
        }
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
        int top = Math.max(0, (int) Math.floor(cameraY)), bottom = Math.min(RunLevels.ROWS - 1, (int) (cameraY + down) + 1);
        int side = art.getHeight();
        boolean later = chapter >= RunLevels.CHAPTERS;
        canvas.save();
        // The ground jumps when something heavy comes down on it.
        if (shake > 0) canvas.translate((random.nextFloat() - .5f) * tile * .28f * Math.min(1, shake * 4), (random.nextFloat() - .5f) * tile * .28f * Math.min(1, shake * 4));
        paint.setAlpha(255);
        // What stands in its villages, behind everything else.
        for (int[] decor : level.decor) {
            if (decor[1] < first - 4 || decor[1] > last + 4) continue;
            // Over a hundred, it is something of the fourth sheet: a chest in a stash.
            if (decor[0] >= 100) pieceTall(canvas, four, decor[0] - 100, (decor[1] + .5f - cameraX) * tile, (decor[2] + 1 - cameraY) * tile, tile * 1.3f, false);
            else pieceTall(canvas, three, decor[0], (decor[1] + .5f - cameraX) * tile, (decor[2] + 1 - cameraY) * tile, tile * DECOR_TALL[decor[0]], false);
        }
        for (int column = first; column <= last; column++) {
            float left = (column - cameraX) * tile;
            for (int row = top; row <= bottom; row++) {
                char here = run.at(column, row);
                if (here == RunLevels.AIR) continue;
                float y = (row - cameraY) * tile;
                if (here == RunLevels.BLOCK && run.room() >= 0) {
                    // In a place off the land, its walls are dark wood, with a lighter board where they are stood on.
                    boolean capped = run.at(column, row - 1) != RunLevels.BLOCK;
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFF2A1D1A);
                    canvas.drawRect(left, y, left + tile + 1, y + tile + 1, paint);
                    if (capped) {
                        paint.setColor(0xFF8A5F3E);
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
                } else if (here == RunLevels.THIN || here == RunLevels.CRUMBLE) {
                    int which = here == RunLevels.THIN ? THIN_ART : CRUMBLE_ART;
                    two.get();
                    // A ledge that has begun to crumble shivers until it goes.
                    float shiver = here == RunLevels.CRUMBLE && run.crumbling(column, row) > 0 ? (float) Math.sin(clock * 70 + column) * tile * .05f : 0;
                    piece(canvas, two, which, left + tile / 2 + shiver, y + (tile + 1) * two.tall[which] / two.wide[which], tile + 1, column % 2 != 0);
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
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF2A1D1A);
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
            if (!given && item[0] != PlusRun.STALL && item[0] != PlusRun.DROPOFF && who >= 3) {
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
            ring(canvas, x, y, CalculatorLayout.CORAL);
            thing(canvas, arm[0], x, y, tile * (arm[0] == PlusRun.GUN ? .66f : .9f) * Math.min(1, WEAPON_LONG[arm[0]] + .2f),
                    arm[0] == PlusRun.GUN || arm[0] == PlusRun.BOOK ? 0 : 28, false, false);
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
                if (!foe.flat && foe.dazed > 0 && (int) (clock * 16) % 2 == 0) paint.setAlpha(120);
                art(canvas, foe.flat ? MINUS_FLAT_ART : MINUS_ART + (walking ? 1 : 0), middle, feet, foe.wide() * tile * 1.3f, turned);
                paint.setAlpha(255);
                // One that throws knives holds the next one: up, when it is about to throw it.
                if (foe.kind == RunLevels.THROWER && !foe.flat && foe.wait < PlusRun.WARNING + 1) {
                    thing(canvas, PlusRun.KNIFE, middle + (foe.leftward ? -1 : 1) * tile * .42f, feet - tile * .22f, tile * .6f,
                            foe.wait < PlusRun.WARNING ? -8 : 72, foe.leftward, true);
                }
            } else if (foe.kind == RunLevels.TIMES) {
                art(canvas, TIMES_ART + (second ? 1 : 0), middle, feet, foe.wide() * tile * 1.25f, turned);
            } else {
                if (foe.flat) paint.setAlpha(110);
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
            switch (shot.kind) {
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
                        while (row < RunLevels.ROWS && !PlusRun.solid(run.at(column, row))) row++;
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
        drawHer(canvas);
        // What has just been won, rising from where it was won.
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(tile * .42f);
        for (int index = 0; index < pops.size(); index++) {
            float[] pop = pops.get(index);
            float x = (pop[0] - cameraX) * tile, y = (pop[1] - pop[2] * 1.4f - cameraY) * tile;
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
        if (inside == null) inside = BitmapFactory.decodeResource(context.getResources(), R.drawable.plus_run_room);
        float wide = height * (float) inside.getWidth() / inside.getHeight(), drift = cameraX * tile * .15f % (wide * 2);
        paint.setAlpha(255);
        for (int copy = 0; copy * wide - drift < width; copy++) {
            float left = copy * wide - drift;
            target.set(left - 1, 0, left + wide + 1, height);
            canvas.save();
            if (copy % 2 != 0) canvas.scale(-1, 1, left + wide / 2, 0);
            canvas.drawBitmap(inside, null, target, paint);
            canvas.restore();
        }
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
            if (in < 0) {
                door(canvas, known ? picture : CRACK_PICTURE, one[1], one[2], at == index, known ? "Enter" : "Break");
                // Where a passage comes out looks like a crack too, until she has come out of it.
                if (one[5] != one[1]) door(canvas, been ? DOOR_PICTURE : CRACK_PICTURE, one[5], one[9], at == index, "Enter");
                // Found, a door has a mark over it that says what kind of place it leads to: and a tick, once that is done with.
                boolean found = been || (session.tally("run_rooms_" + number) & 1 << index) != 0 || known && one[7] != 1;
                boolean done = (run.roomsDone() & 1 << index) != 0;
                if (found && at != index) doorMark(canvas, one[0], one[1], one[2], done);
            } else if (in == index) {
                // Shut until what happens in it is over, its door is dim.
                if (run.sealed()) paint.setAlpha(110);
                door(canvas, picture, one[6], RunLevels.FLOOR, at == index, "Leave");
                if (one[8] >= 0) door(canvas, DOOR_PICTURE, one[8], RunLevels.FLOOR, at == index, "Leave");
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
    private void doorMark(Canvas canvas, int kind, int column, int floor, boolean done) {
        float x = (column + .5f - cameraX) * tile, y = (floor - cameraY) * tile - tile * 2.15f, round = tile * .3f;
        if (x < -tile * 2 || x > width + tile * 2) return;
        y += (float) Math.sin(clock * 2.4f + column) * tile * .04f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33000000);
        canvas.drawCircle(x, y + tile * .03f, round, paint);
        paint.setColor(cardColor);
        paint.setAlpha(done ? 150 : 255);
        canvas.drawCircle(x, y, round, paint);
        int colour = kind == RunLevels.ARENA ? 0xFFC0392B : kind == RunLevels.PASSAGE ? 0xFF3B7BE0 : kind == RunLevels.STASH ? 0xFF8A5A33 : 0xFFE0A21A;
        stroke.setColor(colour);
        stroke.setAlpha(done ? 110 : 255);
        stroke.setStrokeWidth(tile * .045f);
        canvas.drawCircle(x, y, round, stroke);
        paint.setColor(Color.BLACK);
        paint.setAlpha(done ? 110 : 255);
        if (kind == RunLevels.STASH) {
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
                    ? "Mini game: the range" : kind == RunLevels.TRAMPLE ? "Mini game: flatten them" : kind == RunLevels.STASH ? "A stash: climb to the chest"
                    : "A passage: another way through";
            String words = kind == RunLevels.ARENA ? (one[10] != 0 ? "Clear them all. Something sleeps under the last wave." : "Clear every sign: the door opens when they are gone.")
                    : kind == RunLevels.GAME ? "10 seconds. Catch " + PlusRun.ENOUGH + " for the prize." : kind == RunLevels.RANGE
                    ? "12 seconds, and an ink gun that never empties. Hit " + PlusRun.ENOUGH + "." : kind == RunLevels.TRAMPLE
                    ? "15 seconds. They keep coming: flatten " + PlusRun.ENOUGH + "." : kind == RunLevels.STASH ? "It is in sight. Find the way up to it."
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
            if (roomPicture >= 0) pieceIn(canvas, four, roomPicture, left + dp(38), top + dp(42), dp(46), 0);
            else artIn(canvas, STAR_ART, left + dp(38), top + dp(42), dp(40));
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
        for (PlusRun.Foe foe : run.foes()) if (RunLevels.boss(foe.kind) && (boss == null || boss.flat)) boss = foe;
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
        if (arriving) {
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
                // White, she glows: a halo of light about her, with rays that turn.
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                float beat = .5f + .5f * (float) Math.sin(clock * 4);
                for (int ring = 3; ring >= 1; ring--) {
                    paint.setAlpha(Math.round((26 + 14 * beat) * (4 - ring)));
                    canvas.drawCircle(feetX, feetY - tall * .48f, tall * (.42f + ring * .16f + .04f * beat), paint);
                }
                paint.setColor(0xFFFFF3C4);
                for (int ray = 0; ray < 8; ray++) {
                    double turn = clock * .7f + ray * Math.PI / 4;
                    path.reset();
                    path.moveTo(feetX, feetY - tall * .48f);
                    path.lineTo(feetX + (float) Math.cos(turn - .09) * tall * 1.15f, feetY - tall * .48f + (float) Math.sin(turn - .09) * tall * 1.15f);
                    path.lineTo(feetX + (float) Math.cos(turn + .09) * tall * 1.15f, feetY - tall * .48f + (float) Math.sin(turn + .09) * tall * 1.15f);
                    path.close();
                    paint.setAlpha(Math.round(34 + 22 * beat));
                    canvas.drawPath(path, paint);
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
        if (weapon != PlusRun.UNARMED) {
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
        Bitmap pet = herPet();
        int petWide = pet.getWidth() / PetArt.COLUMNS, petTall = pet.getHeight() / PetArt.FRAMES.length;
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
        // Shut in with someone: her name, and a bar of how much more she can take, marked at its thirds.
        boolean den = false;
        for (PlusRun.Foe foe : run.foes()) den |= foe.spawned && !foe.flat && RunLevels.boss(foe.kind) && run.room() >= 0;
        if (run.locked() || run.opening() > 0 || den) {
            PlusRun.Foe boss = null;
            int most = 0, left = 0;
            for (PlusRun.Foe foe : run.foes()) {
                if (!RunLevels.boss(foe.kind) || foe.spawned != den) continue;
                if (boss == null || boss.flat) boss = foe;
                most += foe.most;
                left += Math.max(0, foe.left);
            }
            if (boss != null) {
                int colour = waitColour(boss.kind), face = waitFace(boss.kind);
                float wide = Math.min(safe.width() - margin * 2, dp(300)), high = dp(40), from = safe.centerX() - wide / 2;
                box.set(from, under, from + wide, under + high);
                plate(canvas, box, high / 2, 235);
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
                    canvas.drawText("Shielded", end - dp(17), under + dp(15), text);
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
                paint.setColor(Color.BLACK);
                under += high + dp(8);
            }
        }
        // Something won, in a card of its own in the middle of the screen, which grows as it comes and fades as it goes.
        if (toastLeft > 0 && toastTitle != null) {
            float fade = Math.min(1, toastLeft / .4f), grow = 1 + .12f * Math.max(0, (toastLeft - 3.3f) / .3f);
            float wide = Math.min(safe.width() - dp(40), dp(330)), high = dp(70), from = safe.centerX() - wide / 2, down = safe.top + safe.height() * .36f;
            canvas.saveLayerAlpha(from - dp(20), down - dp(20), from + wide + dp(20), down + high + dp(24), Math.round(255 * fade));
            canvas.scale(grow, grow, safe.centerX(), down + high / 2);
            box.set(from, down, from + wide, down + high);
            plate(canvas, box, dp(20), 250);
            stroke.setColor(0xFFE0B13A);
            stroke.setStrokeWidth(dp(2.5f));
            canvas.drawRoundRect(from, down, from + wide, down + high, dp(20), dp(20), stroke);
            paint.setAlpha(255);
            if (toastPicture == -HER_PICTURE) round(canvas, 0, from + dp(36), down + high / 2, dp(22));
            else if (toastPicture < 0) prizePicture(canvas, -toastPicture, from + dp(36), down + high / 2, dp(46));
            else pieceIn(canvas, four, toastPicture, from + dp(36), down + high / 2, dp(46), 0);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            fitted(canvas, toastTitle, from + dp(68), down + dp(30), dp(16), wide - dp(84));
            text.setColor(soft);
            fitted(canvas, toastWords, from + dp(68), down + dp(51), dp(13), wide - dp(84));
            canvas.restore();
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
            canvas.drawArc(box, -90, 360 * Math.min(1, put ? run.cursed() / 7 : run.on(colour) / Math.max(.1f, run.lasted(colour))), false, stroke);
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
            List<String> lines = wrap(caption, Math.min(safe.width() - dp(48) - face * 2 - dp(8), dp(300)));
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
        canvas.drawColor(Color.argb(Math.round(70 * Math.min(1, age / .4f)), 20, 16, 30));
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
        // The card that names it, with the guild's seal coming down on it.
        if (age > 1.5f) {
            float show = Math.min(1, (age - 1.5f) / .3f), wide = Math.min(safe.width() - dp(36), dp(340)), high = dp(96);
            float left = safe.centerX() - wide / 2, top = safe.top + safe.height() * .38f + dp(16) * (1 - show);
            canvas.saveLayerAlpha(left - dp(8), top - dp(8), left + wide + dp(8), top + high + dp(12), Math.round(255 * show));
            box.set(left, top, left + wide, top + high);
            plate(canvas, box, dp(22), 250);
            stroke.setColor(0xFFE0B13A);
            stroke.setStrokeWidth(dp(3));
            canvas.drawRoundRect(left, top, left + wide, top + high, dp(22), dp(22), stroke);
            paint.setAlpha(255);
            if (handPicture < 0) prizePicture(canvas, -handPicture, left + dp(40), top + high / 2, dp(52));
            else pieceIn(canvas, four, handPicture, left + dp(40), top + high / 2, dp(52), 0);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(soft);
            fitted(canvas, "From " + RunStory.NAMES[FIRST_VILLAGER + handWho - RunLevels.SOMI] + ", with thanks", left + dp(76), top + dp(26), dp(12.5f), wide - dp(150));
            text.setColor(ink);
            fitted(canvas, handTitle, left + dp(76), top + dp(52), dp(19), wide - dp(150));
            text.setColor(soft);
            fitted(canvas, handWords, left + dp(76), top + dp(75), dp(12.5f), wide - dp(92));
            canvas.restore();
            // The seal: down from above, large, onto the corner of the card, where it stays.
            if (age > 1.85f) {
                float fall = Math.min(1, (age - 1.85f) / .15f), size = dp(52) * (2.6f - 1.6f * fall);
                float sealX = left + wide - dp(40), sealY = top + dp(30);
                canvas.save();
                canvas.rotate(-14, sealX, sealY);
                stroke.setColor(0xFFC0392B);
                stroke.setAlpha(Math.round(255 * fall));
                stroke.setStrokeWidth(dp(3));
                canvas.drawCircle(sealX, sealY, size * .46f, stroke);
                stroke.setStrokeWidth(dp(1.5f));
                canvas.drawCircle(sealX, sealY, size * .36f, stroke);
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(0xFFC0392B);
                text.setAlpha(Math.round(255 * fall));
                text.setTextSize(size * .2f);
                canvas.drawText("GUILD", sealX, sealY - size * .02f, text);
                canvas.drawText("SEAL", sealX, sealY + size * .19f, text);
                text.setAlpha(255);
                stroke.setAlpha(255);
                canvas.restore();
            }
        }
        if (age > 2.4f) {
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.WHITE);
            text.setTextSize(dp(13));
            text.setAlpha(Math.round(160 + 80 * (float) Math.sin(clock * 5)));
            canvas.drawText("Tap to go on", safe.centerX(), safe.bottom - dp(36), text);
            text.setAlpha(255);
        }
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
        boolean casts = run.has(PlusRun.RED) || run.has(PlusRun.BLUE);
        if (casts && weapon == PlusRun.UNARMED) {
            pieceIn(canvas, two, run.has(PlusRun.RED) ? FIRE_ART : DROP_ART, where.centerX(), where.centerY(), where.height() * .5f, 0);
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
            pieceIn(canvas, two, run.has(PlusRun.RED) ? FIRE_ART : DROP_ART, x, y, corner, 0);
        }
        if (weapon == PlusRun.GUN) {
            text.setTextSize(dp(12));
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(goStrike ? Color.WHITE : 0xFF292B2A);
            canvas.drawText(String.valueOf(run.rounds()), where.centerX(), where.bottom - dp(8), text);
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
     * The ribbon that calls on a colour of hers, and the arrow over it that steps to the
     * next. The ribbon shows the colour chosen and how that one colour stands, each way
     * unlike the others. On: filled with the colour, a ring round it running down, the
     * seconds left written large, and the word "on". Waiting: grey, the ribbon pale, an
     * hourglass of a ring filling, and "wait" with the seconds to go. Ready: white, the
     * ribbon whole and bright, a ring of the colour beating round it. Round its rim is a
     * dot for each colour she is wearing. For a moment after the arrow has stepped, the
     * name of the colour stepped to is written over the arrow.
     */
    private void colourControl(Canvas canvas) {
        int owned = perks.colours & 127;
        if (chosen == 0 || chosen > PlusRun.BLACK || (owned & 1 << chosen - 1) == 0) chosen = Integer.numberOfTrailingZeros(owned) + 1;
        float x = colourButton.centerX(), y = colourButton.centerY(), round = colourButton.width() / 2;
        boolean on = run.on(chosen) > 0, waiting = !on && run.untilCall(chosen) > 0, held = ribbonFinger >= 0;
        int colour = FORM_COLOURS[chosen - 1];
        boolean pale = chosen == PlusRun.YELLOW;
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
            paint.setColor(Color.BLACK);
            stroke.setColor(pale ? 0xFF3A3F45 : Color.WHITE);
            stroke.setAlpha(run.on(chosen) < 2 && (int) (clock * 10) % 2 == 0 ? 110 : 255);
            stroke.setStrokeWidth(dp(4));
            canvas.drawArc(box, -90, 360 * Math.max(0, Math.min(1, run.on(chosen) / Math.max(.1f, run.lasted(chosen)))), false, stroke);
            stroke.setAlpha(255);
            text.setColor(pale ? 0xFF3A3F45 : Color.WHITE);
            text.setTextSize(round * .8f);
            canvas.drawText(String.valueOf((int) Math.ceil(run.on(chosen))), x, y + round * .16f, text);
            text.setTextSize(round * .34f);
            canvas.drawText("on", x, y + round * .58f, text);
        } else if (waiting) {
            button(canvas, colourButton, false, 0xFF9AA0A6);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF2F3336 : 0xFFDDE0E2);
            canvas.drawCircle(x, y, round - dp(2), paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(70);
            pieceIn(canvas, two, RIBBON_ART + chosen - 1, x, y - round * .26f, round * .7f, 0);
            paint.setAlpha(255);
            stroke.setColor(colour);
            stroke.setStrokeWidth(dp(3.5f));
            canvas.drawArc(box, -90, 360 * Math.max(0, Math.min(1, run.charge(chosen))), false, stroke);
            text.setColor(soft);
            text.setTextSize(round * .34f);
            canvas.drawText("wait " + (int) Math.ceil(run.untilCall(chosen)) + " s", x, y + round * .58f, text);
        } else {
            float beat = .5f + .5f * (float) Math.sin(clock * 5);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colour);
            paint.setAlpha(Math.round(50 + 60 * beat));
            canvas.drawCircle(x, y, round + dp(3) + dp(3) * beat, paint);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            button(canvas, colourButton, false, colour);
            pieceIn(canvas, two, RIBBON_ART + chosen - 1, x, y, round * 1.2f, 0);
            stroke.setColor(colour);
            stroke.setStrokeWidth(dp(3.5f));
            canvas.drawArc(box, -90, 360, false, stroke);
        }
        canvas.restore();
        // A dot on its rim for each colour she is wearing, whichever is shown.
        int wearing = run.mix(), dots = Integer.bitCount(wearing), dot = 0;
        for (int each = PlusRun.RED; each <= PlusRun.BLACK && dots > 0; each++) {
            if ((wearing & 1 << each - 1) == 0) continue;
            double turn = Math.toRadians(-90 + (dot - (dots - 1) / 2f) * 26);
            float at = x + (float) Math.cos(turn) * (round + dp(1)), high = y + (float) Math.sin(turn) * (round + dp(1));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(at, high, dp(5.5f), paint);
            paint.setColor(FORM_COLOURS[each - 1]);
            canvas.drawCircle(at, high, dp(4), paint);
            paint.setColor(Color.BLACK);
            dot++;
        }
        if (fanOpen) {
            drawFan(canvas);
            return;
        }
        // The arrow, when there is more than one colour to step through: with the colour it will step to as a dot beside it.
        if (Integer.bitCount(owned) > 1) {
            button(canvas, nextButton, false, CalculatorLayout.CORAL);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(0xFF3A3F45);
            text.setTextSize(nextButton.height() * .62f);
            canvas.drawText("›", nextButton.centerX() - nextButton.width() * .08f, nextButton.centerY() + nextButton.height() * .2f, text);
            int next = chosen;
            do next = next % PlusRun.BLACK + 1;
            while ((owned & 1 << next - 1) == 0);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(FORM_COLOURS[next - 1]);
            canvas.drawCircle(nextButton.centerX() + nextButton.width() * .2f, nextButton.centerY(), nextButton.width() * .11f, paint);
            paint.setColor(Color.BLACK);
            if (sinceStep < 1.4f) {
                // The colour stepped to, by name, for a moment.
                String name = FORM_NAMES[chosen - 1];
                text.setTextSize(dp(13));
                float wide = text.measureText(name) + dp(20), high = dp(24), top = nextButton.top - high - dp(4);
                box.set(nextButton.centerX() - wide / 2, top, nextButton.centerX() + wide / 2, top + high);
                plate(canvas, box, high / 2, Math.round(240 * Math.min(1, (1.4f - sinceStep) / .3f)));
                text.setColor(colour == FORM_COLOURS[PlusRun.YELLOW - 1] ? 0xFF8A6A00 : colour);
                text.setAlpha(Math.round(255 * Math.min(1, (1.4f - sinceStep) / .3f)));
                canvas.drawText(name, nextButton.centerX(), top + dp(17), text);
                text.setAlpha(255);
            }
        }
    }

    /**
     * The colours that are hers, opened out in a row over the buttons while the ribbon is
     * held, each as it stands: on, with a ring of its time; waiting, grey, with its seconds;
     * or ready. One picked to be put on has a gold ring and a plus; one picked to be let
     * go of is crossed through. Over the row is how many she may wear, and what the colours
     * that would then be on would make, if that has a name.
     */
    private void drawFan(Canvas canvas) {
        int owned = perks.colours & 127, count = Integer.bitCount(owned);
        float cell = Math.min(dp(52), (safe.width() - dp(20)) / count - dp(6)), gap = dp(6), wide = count * cell + (count - 1) * gap;
        float left = Math.max(safe.left + dp(10), Math.min(safe.right - dp(10) - wide, colourButton.centerX() - wide / 2)), bottom = colourButton.top - dp(10);
        // Never over her: where the row would lie across her, it lies over her head instead.
        float herX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, herFeet = (run.y() + PlusRun.TALL - cameraY) * tile, herTop = herFeet - tile * 1.7f;
        if (herX > left - dp(16) && herX < left + wide + dp(16) && herFeet > bottom - cell - dp(36) && herTop < bottom + dp(6)) bottom = herTop - dp(12);
        canvas.drawColor(0x55101018);
        box.set(left - dp(8), bottom - cell - dp(34), left + wide + dp(8), bottom + dp(6));
        plate(canvas, box, dp(20), 245);
        int will = run.mix() & ~fanDrop | fanWant, at = 0;
        for (int colour = PlusRun.RED; colour <= PlusRun.BLACK; colour++) {
            if ((owned & 1 << colour - 1) == 0) continue;
            if (fanCells[colour] == null) fanCells[colour] = new RectF();
            RectF where = fanCells[colour];
            where.set(left + at * (cell + gap), bottom - cell, left + at * (cell + gap) + cell, bottom);
            at++;
            float x = where.centerX(), y = where.centerY(), round = cell / 2;
            boolean on = run.on(colour) > 0, waiting = !on && run.untilCall(colour) > 0, want = (fanWant & 1 << colour - 1) != 0, drop = (fanDrop & 1 << colour - 1) != 0;
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
            if (want) {
                stroke.setColor(0xFFE0A21A);
                stroke.setStrokeWidth(dp(4));
                canvas.drawCircle(x, y, round, stroke);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFE0A21A);
                canvas.drawCircle(x + round * .68f, y - round * .68f, dp(8), paint);
                paint.setColor(Color.BLACK);
                text.setColor(Color.WHITE);
                text.setTextSize(dp(13));
                canvas.drawText("+", x + round * .68f, y - round * .68f + dp(4.5f), text);
            } else if (drop) {
                stroke.setColor(0xFF3A3F45);
                stroke.setStrokeWidth(dp(3.5f));
                canvas.drawLine(x - round * .6f, y - round * .6f, x + round * .6f, y + round * .6f, stroke);
            }
        }
        // What that would make.
        int named = -1;
        boolean red = (will & 1) != 0, blue = (will & 2) != 0, brown = (will & 4) != 0, green = (will & 8) != 0, yellow = (will & 16) != 0, purple = (will & 32) != 0;
        if (will == 127) named = PlusRun.WHITEN;
        else if (Integer.bitCount(will) >= 3) named = PlusRun.PRISM;
        else if (red && blue) named = PlusRun.STEAMS;
        else if (red && yellow) named = PlusRun.COMET;
        else if (blue && green) named = PlusRun.TIDE;
        else if (brown && purple) named = PlusRun.MIRROR;
        else if (green && yellow) named = PlusRun.GALE;
        else if (red && brown) named = PlusRun.EMBER;
        String words = named >= 0 ? MIX_NAMES[named] + ": " + MIX_DOES[named] : "Slide to pick, let go to wear  ·  " + Integer.bitCount(will & perks.colours) + " of " + run.together();
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(named >= 0 ? ink : soft);
        fitted(canvas, words, left + wide / 2, bottom - cell - dp(13), dp(12.5f), wide);
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
        heading(canvas, "Plus run", RunStory.TITLE + "  ·  " + earned + " of " + all * 3 + " stars");
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
        // The counter, where her pluses buy what she keeps.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33000000);
        float round = shopButton.height() / 2;
        canvas.drawRoundRect(shopButton.left, shopButton.top + dp(2), shopButton.right, shopButton.bottom + dp(3), round, round, paint);
        paint.setColor(CalculatorLayout.CORAL);
        canvas.drawRoundRect(shopButton, round, round, paint);
        paint.setColor(Color.BLACK);
        text.setColor(Color.WHITE);
        text.setTextAlign(Paint.Align.RIGHT);
        text.setTextSize(dp(16));
        String has = "+ " + bank() + "  ›";
        canvas.drawText(has, shopButton.right - dp(18), shopButton.centerY() + dp(5.5f), text);
        text.setTextAlign(Paint.Align.LEFT);
        fitted(canvas, "Guild counter: upgrades", shopButton.left + dp(20), shopButton.centerY() + dp(5.5f), dp(16),
                shopButton.width() - dp(52) - text.measureText(has));
        // Her, under it all, with a word about what is next.
        float tall = Math.min(dp(96), safe.bottom - shopButton.bottom - dp(20));
        if (tall > dp(40)) {
            float feet = safe.bottom - dp(14), middle = safe.left + dp(26) + tall * .45f;
            paint.setAlpha(255);
            drawHer(canvas, middle, feet, tall, STAND, false);
            int waiting = RunLevels.bossOf(open);
            String words = names.named(starsOf(RunLevels.TOTAL) > 0 ? "We read both volumes to the end. Again Sloppy~♡"
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

    /** The counter: what her pluses buy, how far along each she is, and what the next step of it costs. */
    private void drawShop(Canvas canvas) {
        drawSky(canvas, RunLevels.chapterOf(open()), clock * dp(5));
        canvas.drawColor(dark ? 0xAA000000 : 0x77FFFFFF);
        int bank = bank();
        heading(canvas, "Guild counter", "What she buys here she keeps in every land  ·  + " + bank + " to spend");
        for (int row = 0; row < wares.length; row++) {
            RectF where = wares[row];
            int index = UP_ORDER[row];
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
            boolean roomy = high > dp(60);
            fitted(canvas, UP_NAMES[index], words, roomy ? top + dp(25) : middle + dp(5), dp(16), room);
            if (roomy) {
                text.setColor(soft);
                fitted(canvas, done ? "All of it is hers" : locked != null ? UP_GIVES[index][has] + ". " + locked : UP_GIVES[index][has], words, top + dp(44),
                        dp(12.5f), room);
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
    }
}
