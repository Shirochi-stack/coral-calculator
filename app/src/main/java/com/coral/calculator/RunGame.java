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
 * and sounds. It has a map of the thirty lands, cards on which the story is told by whoever
 * is speaking, a counter where the pluses she has collected buy what she keeps, the run
 * itself, and a card at the end of each land. {@link PlayStage} hands the screen over to it.
 */
final class RunGame {
    private static final int STORY = 0, MAP = 1, PLAY = 2, PAUSED = 3, RESULT = 4, SHOP = 5;
    private static final int RESUME = 0, AGAIN = 1, TO_MAP = 2, NEXT = 3;
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
            R.drawable.plus_run_sky4, R.drawable.plus_run_sky5};
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
            R.drawable.run_face_tsubakieh, R.drawable.run_face_mawang};
    private static final int[] SPEAKER_COLOURS = {CalculatorLayout.CORAL, 0xFFD2452F, 0xFF3F7FC4, 0xFFE8779A, 0xFF7A5A9A, 0xFF6B2A3A};
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
    private static final String[] UP_NAMES = {"Chikuwa's lunch box", "Wings", "Glossary magnet", "Longer treats", "Bigger ink tank"};
    private static final String[][] UP_GIVES = {
            {"A fourth heart in every land", "A fifth heart in every land"},
            {"A second jump in the air: press jump again"},
            {"Pluses close by come to her", "Pluses come to her from further off"},
            {"What she picks up lasts half as long again", "What she picks up lasts twice as long"},
            {"The ink gun holds 24 shots", "The ink gun holds 32 shots"}};
    private static final int[][] UP_COSTS = {{120, 320}, {200}, {90, 240}, {80, 220}, {70, 180}};
    private static final int[] UP_ART = {BUFF_ART + PlusRun.BREAD, -1, BUFF_ART + PlusRun.MAGNET, BUFF_ART + PlusRun.ICE, WEAPON_ART + PlusRun.GUN};
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
    private final RectF[] lands = new RectF[RunLevels.COUNT];
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
    private int bossMode;
    /** Where she was last hurt, her feet, which way she faced, and how long ago: below zero when that is over. */
    private float knockX, knockY, knockAge = -1;
    private boolean knockLeft;
    /** Sparks from a plus and dust from a landing: across, down, their speeds, how long each has left, and which it is. */
    private final List<float[]> bits = new ArrayList<>();
    /** Words that rise from where something was won: across, down, how long ago; and what each says. */
    private final List<float[]> pops = new ArrayList<>();
    private final List<String> popWords = new ArrayList<>();
    private String caption;
    private float captionLeft;

    // The story
    private String[][] telling;
    private int line, storyChapter;
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
        this.pet = pet != null ? pet : BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_pet);
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
    private int open() { return Math.max(1, Math.min(RunLevels.COUNT, session.tally("run_open"))); }

    private int starsOf(int land) { return session.tally("run_stars_" + land); }

    /** The pluses she has to spend, and how far along each thing she can buy she is. */
    private int bank() { return session.tally("run_bank"); }

    private int bought(int which) { return Math.min(UP_COSTS[which].length, session.tally("run_up_" + which)); }

    /** What she takes into a land of what she has bought. */
    private PlusRun.Perks kept() {
        PlusRun.Perks kept = new PlusRun.Perks();
        kept.hearts = PlusRun.HEARTS + bought(0);
        kept.wings = bought(1) > 0;
        kept.reach = bought(2) == 0 ? 0 : bought(2) == 1 ? 1.6f : 2.6f;
        kept.lasting = 1 + .5f * bought(3);
        kept.shots = PlusRun.GUN_SHOTS + 8 * bought(4);
        return kept;
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
        // Four buttons along the bottom: the arrows on the left, with room between them, and on the right the jump, with what she carries beside it.
        float edge = dp(18), apart = dp(26), between = dp(16);
        float size = Math.min(dp(72), (safe.width() - edge * 2 - apart - between - dp(14)) / 4);
        float y = safe.bottom - dp(24) - size;
        leftButton.set(safe.left + edge, y, safe.left + edge + size, y + size);
        rightButton.set(leftButton.right + apart, y, leftButton.right + apart + size, y + size);
        jumpButton.set(safe.right - edge - size, y, safe.right - edge, y + size);
        strikeButton.set(jumpButton.left - between - size, y, jumpButton.left - between, y + size);
        float small = dp(44), margin = dp(10);
        pauseButton.set(safe.right - margin - small, safe.top + margin, safe.right - margin, safe.top + margin + small);
        closeButton.set(pauseButton);
        soundButton.set(closeButton.left - dp(8) - small, closeButton.top, closeButton.left - dp(8), closeButton.bottom);
        // The map: a row of six lands for each chapter, under its name, and the counter under them all.
        float gap = dp(8), room = safe.height() - dp(92) - dp(150);
        float side = Math.max(dp(26), Math.min(dp(54), Math.min((safe.width() - dp(32) - gap * 5) / 6, room / RunLevels.CHAPTERS - dp(34))));
        float left = safe.centerX() - (side * 6 + gap * 5) / 2, top = safe.top + dp(92);
        for (int index = 0; index < lands.length; index++) {
            int chapter = index / RunLevels.IN_CHAPTER, place = index % RunLevels.IN_CHAPTER;
            float x = left + place * (side + gap), down = top + chapter * (side + dp(34)) + dp(22);
            lands[index].set(x, down, x + side, down + side);
        }
        float under = lands[lands.length - 1].bottom + dp(16);
        shopButton.set(left, under, left + side * 6 + gap * 5, under + dp(48));
        // The counter: what can be bought, one under another.
        float high = Math.max(dp(44), Math.min(dp(78), (safe.height() - dp(120)) / wares.length - dp(8)));
        float across = Math.min(safe.width() - dp(28), dp(460));
        for (int index = 0; index < wares.length; index++) {
            float down = safe.top + dp(100) + index * (high + dp(8));
            wares[index].set(safe.centerX() - across / 2, down, safe.centerX() + across / 2, down + high);
        }
    }

    void close() { audio.close(); }

    /** The Back key: out of a run to its pause, out of the story, the counter or a card to the map; false to leave the game. */
    boolean back() {
        if (screen == PLAY) {
            pause();
        } else if (screen == PAUSED) {
            screen = PLAY;
        } else if (screen == STORY) {
            endStory();
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
        String said = who == 0 ? names.named(words[1]) : words[1];
        if (captioned) {
            caption = who == 0 ? said : RunStory.NAMES[who] + ": " + said;
            captionLeft = CAPTION + said.length() * .025f;
        }
        if (who != 0 || Chatter.DEFAULT_NAME.equals(names.nickname())) audio.say(words[0]);
    }

    private void speakAny(String[][] lines) { speak(lines[random.nextInt(lines.length)], true); }

    /** A passing remark, made only if nothing else is being said. */
    private void remark(String[][] lines) {
        if (captionLeft <= 0 && !audio.speaking()) speakAny(lines);
    }

    /** Tells some lines of the story, one to a card, and then does something. */
    private void tell(String[][] lines, int chapter, Runnable then) {
        telling = lines;
        storyChapter = chapter;
        line = 0;
        afterStory = then;
        screen = STORY;
        captionLeft = 0;
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
        int told = session.tally(key);
        if ((told & 1 << land - 1) != 0) return false;
        session.keep(key, told | 1 << land - 1);
        return true;
    }

    // A land

    private void play(int land) {
        number = land;
        String[][] intro = RunStory.intro(land);
        // Where the story moves on, it is told before the land, the first time.
        if (intro != null && untold("run_intro2", land)) tell(intro, RunLevels.chapterOf(land), () -> begin(land, false));
        else begin(land, true);
    }

    private void begin(int land, boolean greeted) {
        number = land;
        perks = kept();
        run = new PlusRun(RunLevels.level(land), perks);
        // For looking at a land's later parts in a build made for testing: she starts by its first flag, its second,
        // before the place where someone waits, or inside it.
        int from = BuildConfig.DEBUG ? session.tally("run_from") : 0;
        if (from > 0) {
            RunLevels.Level here = run.level();
            run.place(from <= here.flags.size() ? here.flags.get(from - 1) : here.gate - (from == 3 ? 24 : 19), RunLevels.FLOOR - PlusRun.TALL - .05f);
        }
        idle();
        seenJumps = seenLandings = seenCollected = seenFlattened = seenHurts = seenSprings = seenFlags = seenBlows = 0;
        seenStrikes = seenPicked = seenThrown = seenTurned = seenTaken = seenSpared = seenCharms = seenFlaps = seenSwings = seenBonus = 0;
        java.util.Arrays.fill(remarked, false);
        saidCombo = saidLast = saidFlag = saidCharm = false;
        knockAge = -1;
        squash = 0;
        sinceUse = 9;
        sinceLook = 0;
        sinceAim = 9;
        bossMode = 0;
        bits.clear();
        pops.clear();
        popWords.clear();
        cameraX = 0;
        cameraY = restingCamera();
        screen = PLAY;
        captionLeft = 0;
        if (greeted) speakAny(RunStory.START);
    }

    private void idle() {
        goLeft = goRight = goJump = goStrike = false;
        jumpPressed = strikePressed = false;
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
        banked = run.collected() + run.bonus();
        session.keep("run_bank", bank() + banked);
        if (won) {
            float share = run.total() == 0 ? 1 : run.collected() / (float) run.total();
            stars = 1 + (share >= .6f ? 1 : 0) + (share >= .9f ? 1 : 0);
            if (stars > starsOf(number)) session.keep("run_stars_" + number, stars);
            session.keepBest("run_time_" + number, Math.max(1, Math.round(run.time())), true);
            if (number >= open() && number < RunLevels.COUNT) session.keep("run_open", number + 1);
            if (number == 1) session.keepBest("run_best", Math.max(1, Math.round(run.time())), true);
            audio.play("win");
            speak(RunStory.WON[stars - 1], false);
            pills(number < RunLevels.COUNT ? "Next" : "The end", NEXT, "Again", AGAIN, "Lands", TO_MAP);
        } else {
            stars = 0;
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
        } else if (action == AGAIN) {
            begin(number, true);
        } else if (action == TO_MAP || number >= RunLevels.COUNT) {
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
        if (screen == PLAY) step(seconds);
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
        int carried = run.weapon(), heartsBefore = run.hearts();
        float wasX = run.x() + PlusRun.WIDE / 2, wasY = run.y() + PlusRun.TALL;
        boolean wasLeft = run.facingLeft();
        run.step(seconds, goLeft, goRight, goJump || jumpPressed, goStrike || strikePressed);
        jumpPressed = false;
        strikePressed = false;
        sinceUse += seconds;
        sinceAim += seconds;
        float across = width / tile, down = height / tile;
        float goal = Math.max(0, Math.min(Math.max(0, run.columns() - across), run.x() + PlusRun.WIDE / 2 - across * (run.facingLeft() ? .58f : .4f)));
        // The view stays where she was hurt for a moment, so she is seen to be thrown, before it goes to where she is now.
        boolean held = knockAge >= 0 && knockAge < .5f;
        if (!held) cameraX += (goal - cameraX) * Math.min(1, seconds * 7);
        // Where the land is taller than the screen, the view goes up and down with her.
        float level = down >= RunLevels.ROWS ? restingCamera()
                : Math.max(0, Math.min(RunLevels.ROWS - down, run.y() + PlusRun.TALL - down * .62f));
        if (!held) cameraY += (level - cameraY) * Math.min(1, seconds * 6);
        if (squash > 0) squash -= seconds;
        float herX = run.x() + PlusRun.WIDE / 2, herY = run.y() + PlusRun.TALL;

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
            int kind = run.lastTaken();
            audio.play(kind == PlusRun.BREAD ? "bread" : "buff", .6f);
            buzz(false);
            // Bread with every heart full is worth three pluses instead, and Chikuwa says so.
            boolean full = kind == PlusRun.BREAD && run.bonus() != seenBonus;
            speak(full ? RunStory.FULL[0] : RunStory.TOOK[kind], true);
            if (full) pop(herX, herY - 1.6f, "+ 3");
            seenBonus = run.bonus();
        }
        if (run.bonus() != seenBonus) {
            // One sign after another without touching the ground.
            pop(run.foeX(), run.foeY() - 1, "× " + run.combo() + "   + " + (run.bonus() - seenBonus));
            seenBonus = run.bonus();
            audio.play("combo", .5f);
            if (run.combo() >= 3 && !saidCombo) {
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
            if (found >= 0 && !remarked[found]) {
                remarked[found] = true;
                speak(RunStory.ARMS[found], true);
            }
        }
        if (run.strikes() != seenStrikes) {
            seenStrikes = run.strikes();
            sinceUse = 0;
            if (carried == PlusRun.GUN) audio.play("shot", .4f);
            else if (carried == PlusRun.BRUSH || carried == PlusRun.SWORD) audio.play("slash", .5f);
            else audio.play("throw", .5f);
        }
        PlusRun.Foe boss = null;
        for (PlusRun.Foe foe : run.foes()) if (RunLevels.boss(foe.kind)) boss = foe;
        if (run.thrown() != seenThrown) {
            seenThrown = run.thrown();
            boolean hearts = boss != null && boss.kind == RunLevels.ILLYA && Math.abs(boss.x - run.x()) < 14;
            audio.play(hearts ? "heart" : "throw", hearts ? .5f : .3f);
            if (hearts && sinceAim > 7) {
                sinceAim = 0;
                remark(new String[][]{RunStory.AIM[0]});
            }
        }
        if (boss != null && boss.kind == RunLevels.TSUBAKIEH && boss.mode != bossMode) {
            bossMode = boss.mode;
            // Her bat goes up: a word from her, now and then.
            if (bossMode == 1 && sinceAim > 6) {
                sinceAim = 0;
                remark(new String[][]{RunStory.AIM[1]});
            }
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
            audio.play(seenBlows >= PlusRun.ZERO_HITS ? "fall" : "blow");
            buzz(true);
            // Whoever took it has something to say about it; for Zero, who says nothing, she does.
            if (boss != null) speak(RunStory.BLOWS[boss.kind == RunLevels.ILLYA ? 1 : boss.kind == RunLevels.TSUBAKIEH ? 2 : 0], true);
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
            // Where she was hurt she is thrown into the air and falls away; if she fell out of the land, from the foot of the screen.
            knockX = wasX;
            knockY = Math.min(wasY, cameraY + height / tile + .4f);
            knockLeft = wasLeft;
            knockAge = 0;
            if (run.state() == PlusRun.PLAYING) {
                // What she says is about what hurt her; down to her last heart, about that.
                int cause = run.cause();
                if (run.hearts() == 1 && !saidLast) {
                    saidLast = true;
                    speakAny(RunStory.LAST_HEART);
                } else {
                    speakAny(cause == PlusRun.FELL ? RunStory.FELL : cause == PlusRun.STRUCK ? RunStory.SHOT
                            : cause == PlusRun.SPIKED ? RunStory.SPIKED : cause == PlusRun.BATTED ? RunStory.BATTED : RunStory.BITTEN);
                }
            }
        } else if (run.hearts() > heartsBefore) {
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
        int seen = session.tally("run_seen2");
        if (seen == (1 << RunStory.SEE.length) - 1 || captionLeft > 0 || audio.speaking()) return;
        int from = (int) run.x() - 1, to = (int) (run.x() + 6.5f), found = -1;
        for (PlusRun.Foe foe : run.foes()) {
            int which = foe.kind == RunLevels.MINUS ? 0 : foe.kind == RunLevels.TIMES ? 1 : foe.kind == RunLevels.DIVIDE ? 2
                    : foe.kind == RunLevels.THROWER ? 3 : foe.kind == RunLevels.GUNNER ? 4 : -1;
            if (which >= 0 && !foe.flat && foe.x >= from && foe.x <= to && (seen & 1 << which) == 0) found = which;
        }
        for (int[] arm : run.arms()) if (arm[1] >= from && arm[1] <= to && (seen & 1 << 8) == 0) found = 8;
        for (int column = Math.max(0, from); column <= to && found < 0; column++) {
            for (int row = 0; row < RunLevels.ROWS && found < 0; row++) {
                char here = run.at(column, row);
                int which = here == RunLevels.SPIKE ? 5 : here == RunLevels.SPRING ? 6 : here == RunLevels.FLAG ? 7 : here == RunLevels.GATE ? 9 : -1;
                if (which >= 0 && (seen & 1 << which) == 0) found = which;
            }
        }
        if (found < 0) return;
        session.keep("run_seen2", seen | 1 << found);
        speak(RunStory.SEE[found], true);
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
                screen = SHOP;
                speak(RunStory.SHOP[0], false);
            } else {
                for (int index = 0; index < lands.length; index++) {
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
                toMap();
                return;
            }
            for (int index = 0; index < wares.length; index++) if (wares[index].contains(x, y)) buy(index);
        } else {
            for (int index = 0; index < pillCount; index++) {
                if (pills[index].contains(x, y)) {
                    act(pillActions[index]);
                    return;
                }
            }
        }
    }

    /** Buys the next step of something, if her pluses stretch to it. */
    private void buy(int which) {
        int has = bought(which);
        if (has >= UP_COSTS[which].length) return;
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

    /** Which buttons are held, by however many fingers are down: one may hold an arrow while another jumps. */
    private void held(MotionEvent event) {
        int action = event.getActionMasked();
        boolean left = false, right = false, up = false, strike = false;
        boolean carrying = run != null && run.weapon() != PlusRun.UNARMED;
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
            float low = height * .45f, between = (leftButton.right + rightButton.left) / 2;
            float arrows = Math.min(rightButton.right + dp(26), (rightButton.right + strikeButton.left) / 2);
            float jumps = (strikeButton.right + jumpButton.left) / 2;
            for (int finger = 0; finger < event.getPointerCount(); finger++) {
                if (action == MotionEvent.ACTION_POINTER_UP && finger == event.getActionIndex()) continue;
                float x = event.getX(finger), y = event.getY(finger);
                // The buttons are easy to hit: the whole lower part of the screen belongs to one or another of them.
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
            drawStory(canvas);
        } else {
            drawLand(canvas);
            drawHud(canvas);
            if (screen == PLAY) {
                // Charmed, the arrows are the wrong way round, and show it.
                boolean charmed = run.charmed() > 0;
                control(canvas, leftButton, goLeft, 180, charmed);
                control(canvas, rightButton, goRight, 0, charmed);
                control(canvas, jumpButton, goJump, 270, false);
                if (run.weapon() != PlusRun.UNARMED) strikeControl(canvas);
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
            target.set(left, 0, left + wide + 1, height);
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

    private void drawLand(Canvas canvas) {
        int chapter = run.level().chapter;
        drawSky(canvas, chapter, cameraX * tile * .2f);
        float across = width / tile, down = height / tile;
        int first = Math.max(0, (int) cameraX), last = Math.min(run.columns() - 1, (int) (cameraX + across) + 1);
        int top = Math.max(0, (int) Math.floor(cameraY)), bottom = Math.min(RunLevels.ROWS - 1, (int) (cameraY + down) + 1);
        int side = art.getHeight();
        paint.setAlpha(255);
        for (int column = first; column <= last; column++) {
            float left = (column - cameraX) * tile;
            for (int row = top; row <= bottom; row++) {
                char here = run.at(column, row);
                if (here == RunLevels.AIR) continue;
                float y = (row - cameraY) * tile;
                if (here == RunLevels.BLOCK) {
                    // The top of a pile has the turf on it; the rest is what lies under. Every other one is
                    // turned about, so the same picture side by side does not show as a pattern.
                    boolean capped = run.at(column, row - 1) != RunLevels.BLOCK;
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
        // The pluses, each bobbing a little and turning as it hangs there.
        for (int[] plus : run.pluses()) {
            if (plus[0] < first - 1 || plus[0] > last + 1) continue;
            float x = (plus[0] + .5f - cameraX) * tile;
            float y = (plus[1] + .9f - cameraY) * tile + (float) Math.sin(clock * 4 + plus[0]) * tile * .08f;
            float turned = Math.max(.18f, Math.abs((float) Math.cos(clock * 3 + plus[0] * .7f)));
            art(canvas, PLUS_ART, x, y, tile * .78f * turned, false);
        }
        // Her friends, where they wait: holding out what they have for her as she comes near, and cheering once she has it.
        for (int[] item : run.level().items) {
            if (item[3] == 0 || item[1] < first - 3 || item[1] > last + 3) continue;
            boolean given = !run.items().contains(item), near = Math.abs(run.x() - item[1]) < 6;
            float x = (item[1] + 1.55f - cameraX) * tile, y = (item[2] + 1 - cameraY) * tile;
            int who = item[3] == 1 ? CHIKUWA : RUNE;
            paint.setAlpha(255);
            frame(canvas, cast(who), given ? 2 : near ? 1 : 0, CAST_FRAMES[who], x, y, tile * 1.62f, false);
        }
        // What lies about to help, in a ring of its own colour; what a friend holds out is in her hands already.
        for (int[] item : run.items()) {
            if (item[3] != 0 || item[1] < first - 1 || item[1] > last + 1) continue;
            float x = (item[1] + .5f - cameraX) * tile;
            float y = (item[2] + .45f - cameraY) * tile + (float) Math.sin(clock * 3.2f + item[1]) * tile * .07f;
            ring(canvas, x, y, 0xFFFFC53D);
            artIn(canvas, BUFF_ART + item[0], x, y, tile * .78f);
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
            if (foe.x + foe.wide() < first - 2 || foe.x > last + 2) continue;
            float middle = (foe.x + foe.wide() / 2 - cameraX) * tile, feet = (foe.y + foe.tall() - cameraY) * tile;
            boolean second = (int) (clock * 6 + foe.x) % 2 != 0;
            // The pictures face left, and are turned about to go right.
            boolean turned = !foe.leftward && !foe.flat;
            if (foe.kind == RunLevels.MINUS || foe.kind == RunLevels.THROWER) {
                boolean walking = foe.kind == RunLevels.MINUS && second;
                art(canvas, foe.flat ? MINUS_FLAT_ART : MINUS_ART + (walking ? 1 : 0), middle, feet, foe.wide() * tile * 1.3f, turned);
                // One that throws knives holds the next one: up, when it is about to throw it.
                if (foe.kind == RunLevels.THROWER && !foe.flat && foe.wait < PlusRun.WARNING + 1) {
                    thing(canvas, PlusRun.KNIFE, middle + (foe.leftward ? -1 : 1) * tile * .42f, feet - tile * .22f, tile * .6f,
                            foe.wait < PlusRun.WARNING ? -8 : 72, foe.leftward, true);
                }
            } else if (foe.kind == RunLevels.TIMES) {
                art(canvas, TIMES_ART + (second ? 1 : 0), middle, feet, foe.wide() * tile * 1.25f, turned);
            } else if (foe.kind == RunLevels.DIVIDE || foe.kind == RunLevels.GUNNER) {
                if (foe.flat) paint.setAlpha(110);
                // Low on the ground and stretched in the air.
                artTall(canvas, DIVIDE_ART + (foe.ground - foe.y > .3f ? 1 : 0), middle, feet, foe.tall() * tile * (foe.flat ? .6f : 1.5f), false);
                paint.setAlpha(255);
                // One that shoots carries a gun of its own.
                if (foe.kind == RunLevels.GUNNER && !foe.flat) {
                    thing(canvas, PlusRun.GUN, middle + (foe.leftward ? -1 : 1) * tile * .5f, feet - foe.tall() * tile * .55f, tile * .5f, 0,
                            foe.leftward, false);
                }
            } else if (foe.kind == RunLevels.ILLYA) {
                // Illya: floating, a heart at her fingers as she sends one, flustered when hit, and sitting in tears at the last.
                float every = 2.2f - (PlusRun.ZERO_HITS - foe.left) * .45f;
                int pose = foe.flat ? 3 : foe.dazed > 0 ? 2 : foe.wait > every - .45f ? 1 : 0;
                frame(canvas, cast(ILLYA), pose, CAST_FRAMES[ILLYA], middle, feet + (foe.flat ? 0 : tile * .1f), tile * 1.62f, !foe.leftward && !foe.flat);
            } else if (foe.kind == RunLevels.TSUBAKIEH) {
                // Tsubakieh: walking with her bat on her shoulder, the bat up, the bat down, dizzy, and looking at the two halves of it.
                int pose = foe.flat ? 5 : foe.dazed > 0 ? 4 : foe.mode == 1 ? 2 : foe.mode == 2 ? 3 : second ? 1 : 0;
                frame(canvas, cast(TSUBAKIEH), pose, CAST_FRAMES[TSUBAKIEH], middle, feet, tile * 1.62f, !foe.leftward && !foe.flat);
            } else {
                artTall(canvas, ZERO_ART + (foe.flat ? 2 : foe.dazed > 0 ? 1 : 0), middle, feet, foe.tall() * tile * (foe.flat ? .9f : 1.12f), turned);
                // Zero holds up the book he is about to throw.
                if (!foe.flat && foe.dazed <= 0 && foe.wait < .7f && foe.wait > -1) {
                    thing(canvas, PlusRun.BOOK, middle, feet - foe.tall() * tile * 1.25f, tile * .7f, 0, false, false);
                }
            }
        }
        // Whatever is in the air: a pen or a knife point first, a book turning over, a heart, and ink and dots as blobs.
        for (PlusRun.Shot shot : run.shots()) {
            float x = (shot.x - cameraX) * tile, y = (shot.y - cameraY) * tile;
            boolean leftward = shot.across < 0;
            if (shot.kind == PlusRun.HEART) {
                float beat = 1 + .12f * (float) Math.sin(clock * 14);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFFF8FCB);
                paint.setAlpha(110);
                canvas.drawCircle(x, y, tile * .34f * beat, paint);
                paint.setColor(Color.BLACK);
                paint.setAlpha(255);
                artIn(canvas, HEART_ART, x, y, tile * .46f * beat);
            } else if (shot.kind == PlusRun.GUN || shot.kind == PlusRun.DOT) {
                float round = tile * (shot.kind == PlusRun.GUN ? .13f : .19f);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(shot.kind == PlusRun.GUN ? 0xFF2A3FC8 : shot.hers ? CalculatorLayout.CORAL : 0xFF2FB5A8);
                canvas.drawCircle(x - (leftward ? -1 : 1) * round * 1.3f, y, round * .6f, paint);
                canvas.drawCircle(x, y, round, paint);
                stroke.setColor(0xFF2B2440);
                stroke.setStrokeWidth(tile * .035f);
                canvas.drawCircle(x, y, round, stroke);
                paint.setColor(Color.BLACK);
            } else if (shot.kind == PlusRun.BOOK) {
                thing(canvas, PlusRun.BOOK, x, y, tile * .58f, shot.age * 620, leftward, false);
            } else {
                thing(canvas, shot.kind, x, y, tile * WEAPON_LONG[shot.kind], 90, leftward, false);
            }
        }
        paint.setStyle(Paint.Style.FILL);
        for (float[] bit : bits) {
            paint.setColor(bit[5] == 0 ? CalculatorLayout.CORAL : bit[5] == 2 ? 0xFF27348F : 0xFFDDE3E8);
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
            int alpha = run.safe() && (int) (run.time() * 14) % 2 == 0 ? 90 : 255;
            // Full of ice cream, she leaves herself behind.
            if (run.rush() > 0 && run.running()) {
                her(canvas, feetX - way * tile * .7f, feetY, tall, weapon, leftward, 40);
                her(canvas, feetX - way * tile * .35f, feetY, tall, weapon, leftward, 80);
            }
            her(canvas, feetX, feetY, tall, weapon, leftward, alpha);
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
            // A brush or a sword leaves its mark in the air as it is swung.
            float swing = run.swinging();
            if (swing >= 0 && weapon != PlusRun.UNARMED) {
                float handX = feetX + way * tile * .36f, handY = feetY - tile * .58f, reach = tile * (WEAPON_LONG[weapon] + .3f);
                box.set(handX - reach, handY - reach, handX + reach, handY + reach);
                stroke.setColor(weapon == PlusRun.BRUSH ? 0xFFFF5FA8 : Color.WHITE);
                stroke.setAlpha(Math.round(200 * (1 - swing * .6f)));
                stroke.setStrokeWidth(tile * (weapon == PlusRun.BRUSH ? .26f : .14f));
                canvas.save();
                if (leftward) canvas.scale(-1, 1, handX, handY);
                canvas.drawArc(box, -135, 170 * swing + 10, false, stroke);
                canvas.restore();
                stroke.setAlpha(255);
            }
        }
        if (knockAge >= 0) {
            // Thrown back and up from where she was hurt, turning over as she goes, and down out of sight.
            float since = knockAge, back = knockLeft ? 1 : -1;
            float x = (knockX + back * 2.4f * since - cameraX) * tile, y = (knockY - 10 * since + 15 * since * since - cameraY) * tile;
            int pose = since < .16f ? HIT : (int) ((since - .16f) / .14f) % 2 == 0 ? TUMBLE : DROP;
            paint.setAlpha(Math.round(255 * Math.min(1, (KNOCK - since) / .25f)));
            frame(canvas, lose, pose, LOSE_FRAMES, x, y, tall, knockLeft);
            paint.setAlpha(255);
        }
    }

    /** Halgakos as she is at this moment of the run, her feet at a place: empty-handed, or carrying what she fights with. */
    private void her(Canvas canvas, float feetX, float feetY, float tall, int weapon, boolean leftward, int alpha) {
        paint.setAlpha(alpha);
        if (weapon != PlusRun.UNARMED) {
            int pose = sinceUse < .22f || run.swinging() >= 0 ? ARMED_USE : !run.onGround() ? (run.speedDown() < 3 ? ARMED_RISE : ARMED_FALL)
                    : run.running() ? ARMED_RUN + (int) (clock * 11) % 4 : 0;
            frame(canvas, armed(weapon), pose, ARMED_FRAMES, feetX, feetY, tall, leftward);
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
        if (pose >= 0) {
            frame(canvas, jump, pose, JUMP_FRAMES, feetX, feetY, tall, leftward);
            return;
        }
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
        // Her hearts and her pluses, in a plate at the top.
        int hearts = run.maxHearts();
        float tall = dp(44), margin = dp(10), heart = hearts > 3 ? dp(19) : dp(22);
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        String count = "+ " + run.collected() + " of " + run.total();
        box.set(safe.left + margin, safe.top + margin, safe.left + margin + dp(20) + heart * hearts + dp(12) + text.measureText(count) + dp(16),
                safe.top + margin + tall);
        float plateLeft = box.left, plateMiddle = box.centerY();
        plate(canvas, box, tall / 2, 235);
        for (int index = 0; index < hearts; index++) {
            paint.setAlpha(index < run.hearts() ? 255 : 60);
            art(canvas, HEART_ART, plateLeft + dp(14) + heart * (index + .5f), plateMiddle + heart * .42f, heart * .92f, false);
        }
        paint.setAlpha(255);
        text.setColor(ink);
        canvas.drawText(count, plateLeft + dp(20) + heart * hearts + dp(6), plateMiddle + dp(5.5f), text);
        plate(canvas, pauseButton, tall / 2, 235);
        paint.setColor(ink);
        canvas.drawRoundRect(pauseButton.centerX() - dp(7), pauseButton.centerY() - dp(8), pauseButton.centerX() - dp(2.5f), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        canvas.drawRoundRect(pauseButton.centerX() + dp(2.5f), pauseButton.centerY() - dp(8), pauseButton.centerX() + dp(7), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        paint.setColor(Color.BLACK);
        // What is helping her, each with how much of it is left going round it.
        float under = safe.top + margin + tall + dp(8), round = dp(36), x = safe.left + margin;
        float[] left = {0, run.rush() / (BUFF_LASTS[PlusRun.ICE] * perks.lasting), run.shield() ? 1 : 0,
                run.magnet() / (BUFF_LASTS[PlusRun.MAGNET] * perks.lasting), run.notes() / (BUFF_LASTS[PlusRun.NOTES] * perks.lasting)};
        boolean any = false;
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
        // What has just been said, under them.
        if (captionLeft > 0 && caption != null && screen == PLAY) {
            text.setTextSize(dp(14));
            List<String> lines = wrap(caption, Math.min(safe.width() - dp(48), dp(300)));
            float wide = 0;
            for (String one : lines) wide = Math.max(wide, text.measureText(one));
            float high = lines.size() * dp(19) + dp(14);
            box.set(safe.left + margin, under, safe.left + margin + wide + dp(24), under + high);
            plate(canvas, box, dp(14), Math.round(235 * Math.min(1, captionLeft / .3f)));
            text.setColor(ink);
            text.setAlpha(Math.round(255 * Math.min(1, captionLeft / .3f)));
            for (int index = 0; index < lines.size(); index++) {
                canvas.drawText(lines.get(index), safe.left + margin + dp(12), under + dp(8) + dp(19) * (index + .74f), text);
            }
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
        thing(canvas, weapon, where.centerX(), where.centerY(), where.height() * (upright ? .66f : .48f), upright ? 35 : 0, false, false);
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
        String kept = "kept + " + banked;
        if (!won) {
            // Her on the card, sitting where she fell: dazed at first, then sulking and crying by turns.
            float high = dp(104);
            float top = card(canvas, "Out of hearts", chapter + ", land " + number + "  ·  " + kept, high + dp(8));
            int pose = resultClock < 1.3f ? DAZED : (int) ((resultClock - 1.3f) / .55f) % 2 == 0 ? SULK : CRY;
            paint.setAlpha(255);
            frame(canvas, lose, pose, LOSE_FRAMES, safe.centerX(), top + high + dp(4), dp(96), false);
            return;
        }
        float star = dp(46);
        int boss = RunLevels.bossOf(number);
        String title = boss == RunLevels.ZERO ? "Zero is flat!" : boss == RunLevels.ILLYA ? "Illya gives way" : boss == RunLevels.TSUBAKIEH ? "The bat is broken"
                : "Land " + number + " cleared";
        float top = card(canvas, title, "+ " + run.collected() + " of " + run.total() + "  ·  " + PlayStage.clock(Math.max(1, Math.round(run.time())))
                + "  ·  " + kept, star + dp(10));
        // The stars she earned, each popping up in its turn; the rest stay faint.
        for (int index = 0; index < 3; index++) {
            float since = resultClock - .35f - index * .32f;
            boolean earned = index < stars && since > 0;
            float size = star * (earned ? 1 + .35f * Math.max(0, 1 - since / .18f) : .9f);
            paint.setAlpha(earned ? 255 : 55);
            art(canvas, STAR_ART, safe.centerX() + (index - 1) * (star + dp(12)), top + dp(6) + star + (size - star) / 2, size, false);
        }
        paint.setAlpha(255);
    }

    private void drawStory(Canvas canvas) {
        drawSky(canvas, storyChapter, clock * dp(6));
        canvas.drawColor(0x40000000);
        // The card along the bottom, with the words on it; and whoever says them, large, standing behind it.
        String[] words = telling[line];
        int who = RunStory.speaker(words);
        Bitmap picture = face(who);
        text.setTextSize(dp(18));
        float wide = Math.min(safe.width() - dp(32), dp(420)), pad = dp(20);
        List<String> lines = wrap(who == 0 ? names.named(words[1]) : words[1], wide - pad * 2);
        float tall = pad + dp(22) + dp(12) + lines.size() * dp(25) + dp(30) + pad;
        box.set(safe.centerX() - wide / 2, safe.bottom - dp(28) - tall, safe.centerX() + wide / 2, safe.bottom - dp(28));
        float left = box.left, top = box.top, right = box.right, bottom = box.bottom;
        float room = top - safe.top - dp(20), high = Math.min(room, picture.getHeight() * (wide * .8f) / picture.getWidth());
        float across = high * picture.getWidth() / picture.getHeight(), bob = (float) Math.sin(clock * 2) * dp(3);
        // Halgakos stands to the left of the middle, and whoever she is talking to to the right.
        float middle = safe.centerX() + (who == 0 ? -1 : 1) * Math.min(dp(26), Math.max(0, (wide - across) / 2));
        target.set(middle - across / 2, top + dp(30) - high + bob, middle + across / 2, top + dp(30) + bob);
        paint.setAlpha(255);
        canvas.drawBitmap(picture, null, target, paint);
        plate(canvas, box, dp(24), 255);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(dark && who == 5 ? 0xFFD98A9E : SPEAKER_COLOURS[who]);
        text.setTextSize(dp(14.5f));
        canvas.drawText(RunStory.NAMES[who], left + pad, top + pad + dp(12), text);
        text.setColor(soft);
        text.setTextSize(dp(12));
        text.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(telling == RunStory.PROLOGUE ? RunStory.TITLE : RunStory.CHAPTERS[storyChapter], right - pad, top + pad + dp(12), text);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(18));
        for (int index = 0; index < lines.size(); index++) {
            canvas.drawText(lines.get(index), left + pad, top + pad + dp(22) + dp(12) + dp(25) * (index + .72f), text);
        }
        text.setColor(soft);
        text.setTextSize(dp(12.5f));
        text.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Tap to go on  " + (line + 1) + " / " + telling.length, right - pad, bottom - pad + dp(4), text);
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
        for (int land = 1; land <= RunLevels.COUNT; land++) earned += starsOf(land);
        heading(canvas, "Plus run", RunStory.TITLE + "  ·  " + earned + " of " + RunLevels.COUNT * 3 + " stars");
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
        for (int index = 0; index < lands.length; index++) {
            RectF where = lands[index];
            int land = index + 1, got = starsOf(land);
            if (index % RunLevels.IN_CHAPTER == 0) {
                text.setTextAlign(Paint.Align.LEFT);
                text.setColor(land <= open ? ink : soft);
                text.setTextSize(dp(14));
                canvas.drawText(RunStory.CHAPTERS[index / RunLevels.IN_CHAPTER], where.left, where.top - dp(8), text);
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
            text.setColor(boss == RunLevels.ILLYA ? SPEAKER_COLOURS[3] : boss == RunLevels.TSUBAKIEH ? SPEAKER_COLOURS[4]
                    : boss == RunLevels.ZERO ? CalculatorLayout.CORAL : reached ? ink : soft);
            text.setAlpha(reached ? 255 : 130);
            text.setTextSize(where.width() * .38f);
            canvas.drawText(String.valueOf(land), where.centerX(), where.centerY() + where.width() * (reached ? .02f : .13f), text);
            text.setAlpha(255);
            if (reached) {
                float star = where.width() * .26f;
                for (int count = 0; count < 3; count++) {
                    paint.setAlpha(count < got ? 255 : 50);
                    art(canvas, STAR_ART, where.centerX() + (count - 1) * star, where.bottom - where.width() * .1f, star, false);
                }
                paint.setAlpha(255);
            }
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
            String words = names.named(starsOf(RunLevels.COUNT) > 0 ? "We read it to the end. Play it again Sloppy~♡"
                    : open == 1 ? "Pick the first land. Go on Sloppy~♡" : waiting == RunLevels.ILLYA ? "Illya is waiting in land " + open + " Sloppy~♡"
                    : waiting == RunLevels.TSUBAKIEH ? "Tsubakieh and her bat are in land " + open + " Sloppy~♡"
                    : waiting == RunLevels.ZERO ? "The Last Page. Zero is waiting Sloppy~♡" : "Land " + open + " is waiting Sloppy~♡");
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
        for (int index = 0; index < wares.length; index++) {
            RectF where = wares[index];
            float left = where.left, top = where.top, right = where.right, high = where.height(), middle = where.centerY();
            int has = bought(index), steps = UP_COSTS[index].length;
            boolean done = has >= steps, can = !done && bank >= UP_COSTS[index][has];
            plate(canvas, where, dp(16), 245);
            // Its picture: for her wings, herself at the top of a jump.
            float icon = Math.min(dp(46), high - dp(14));
            paint.setAlpha(255);
            if (UP_ART[index] >= 0) artIn(canvas, UP_ART[index], left + dp(14) + icon / 2, middle, icon);
            else frame(canvas, jump, TOP, JUMP_FRAMES, left + dp(14) + icon / 2, middle + icon / 2, icon * .95f, false);
            float words = left + dp(24) + icon, price = dp(86), room = right - words - price - dp(18);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(ink);
            boolean roomy = high > dp(60);
            fitted(canvas, UP_NAMES[index], words, roomy ? top + dp(25) : middle + dp(5), dp(16), room);
            if (roomy) {
                text.setColor(soft);
                fitted(canvas, done ? "All of it is hers" : UP_GIVES[index][has], words, top + dp(44), dp(12.5f), room);
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
            canvas.drawText(done ? "Owned" : "+ " + UP_COSTS[index][has], right - dp(12) - price / 2, middle + dp(5.5f), text);
        }
    }
}
