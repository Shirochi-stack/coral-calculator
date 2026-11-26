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
 * and sounds. It has a map of the thirty lands, cards on which the story is told, the run
 * itself, and a card at the end of each land. {@link PlayStage} hands the screen over to it.
 */
final class RunGame {
    private static final int STORY = 0, MAP = 1, PLAY = 2, PAUSED = 3, RESULT = 4;
    private static final int RESUME = 0, AGAIN = 1, TO_MAP = 2, NEXT = 3;
    /** How she is drawn: standing, running, or one of the six frames of her jump. */
    private static final int STAND = -1, RUN = -2, CROUCH = 0, PUSH_OFF = 1, RISE = 2, TOP = 3, FALL = 4, LAND = 5;
    /**
     * The pictures of the art, side by side, each standing on the bottom of a square of its
     * own: two for each chapter's ground, then the rest. How much of its square each fills
     * is what tools/make_run_art.py printed when it built them.
     */
    private static final int MINUS_ART = 10, MINUS_FLAT_ART = 12, PLUS_ART = 13, LEDGE_ART = 14, FLAG_ART = 15, GATE_ART = 16,
            SPIKES_ART = 17, SPRING_ART = 18, STAR_ART = 19, HEART_ART = 20, TIMES_ART = 21, DIVIDE_ART = 23, ZERO_ART = 25,
            WEAPON_ART = 28;
    private static final float[] ART_WIDE = {1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f,
            1.000f, 1.000f, 1.000f, 1.000f, 0.555f, 1.000f, 1.000f, 0.992f, 1.000f, 1.000f, 0.859f, 0.875f, 1.000f, 0.680f, 0.695f,
            0.711f, 0.828f, 0.203f, 0.242f, 0.812f, 0.312f, 0.438f, 1.000f};
    private static final float[] ART_TALL = {1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 0.547f,
            0.547f, 0.312f, 1.000f, 0.328f, 1.000f, 1.000f, 0.523f, 1.000f, 0.953f, 0.859f, 1.000f, 1.000f, 0.930f, 1.000f, 1.000f,
            1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 0.953f};
    /** How long each of the things she carries is drawn, in squares. */
    private static final float[] WEAPON_LONG = {.78f, .64f, .52f, .98f, 1.08f, .56f};
    private static final int[] SKIES = {R.drawable.plus_run_sky1, R.drawable.plus_run_sky2, R.drawable.plus_run_sky3,
            R.drawable.plus_run_sky4, R.drawable.plus_run_sky5};
    private static final int JUMP_FRAMES = 6;
    /** The six frames of her losing: hit, knocked into the air, falling, sitting dazed, sulking, and crying. */
    private static final int LOSE_FRAMES = 6, HIT = 0, TUMBLE = 1, DROP = 2, DAZED = 3, SULK = 4, CRY = 5;
    /** How long she is seen thrown into the air where she was hurt. */
    private static final float KNOCK = 1.1f;
    private static final float SQUASH = .14f, CAPTION = 2.8f;

    private final PlayStage stage;
    private final Context context;
    private final boolean dark, haptics;
    private final PlaySession session;
    private final CompanionPrefs names;
    private final RunAudio audio;
    private final Random random = new Random();
    private final float density;
    private final Bitmap pet, jump, lose, art, portrait;
    private Bitmap sky;
    private int skyOf = -1;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF target = new RectF(), safe = new RectF(), box = new RectF();
    private final RectF leftButton = new RectF(), rightButton = new RectF(), jumpButton = new RectF(), strikeButton = new RectF();
    private final RectF pauseButton = new RectF(), closeButton = new RectF(), soundButton = new RectF();
    private final RectF[] pills = {new RectF(), new RectF(), new RectF()};
    private final String[] pillLabels = new String[3];
    private final int[] pillActions = new int[3];
    private int pillCount;
    private final RectF[] lands = new RectF[RunLevels.COUNT];
    private final Path path = new Path();
    private final int cardColor, cardEdge, ink, soft;

    private int width, height;
    private float tile;
    private int screen = MAP;
    private float clock;

    // The run
    private int number = 1;
    private PlusRun run;
    private float cameraX, cameraY;
    private boolean goLeft, goRight, goJump, goStrike;
    /** A press of the jump or of the button for what she carries that has not been acted on yet: a tap may be over before the next moment of the game. */
    private boolean jumpPressed, strikePressed;
    private int seenJumps, seenLandings, seenCollected, seenFlattened, seenHurts, seenSprings, seenFlags, seenBlows;
    private int seenStrikes, seenPicked, seenThrown, seenTurned;
    /** Which of the things to fight with she has already said her word about in this land. */
    private final boolean[] remarked = new boolean[PlusRun.GUN + 1];
    private float squash;
    /** Where she was last hurt, her feet, which way she faced, and how long ago: below zero when that is over. */
    private float knockX, knockY, knockAge = -1;
    private boolean knockLeft;
    /** Sparks from a plus and dust from a landing: across, down, their speeds, how long each has left, and which it is. */
    private final List<float[]> bits = new ArrayList<>();
    private String caption;
    private float captionLeft;

    // The story
    private String[][] telling;
    private String heading;
    private int line;
    private Runnable afterStory;

    // The end of a land
    private boolean won;
    private int stars;
    private float resultClock;
    private int starsShown;

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
        cardColor = dark ? 0xFF1D2023 : Color.WHITE;
        cardEdge = dark ? 0xFF34383C : 0xFFE4E4E4;
        ink = dark ? 0xFFF3F3F3 : 0xFF292B2A;
        soft = dark ? 0xFFC9CDCF : 0xFF565A57;
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        for (int index = 0; index < lands.length; index++) lands[index] = new RectF();
        // The first time, the story of how it began comes before anything else.
        if (!told(0)) tell(RunStory.PROLOGUE, RunStory.TITLE, 0, () -> screen = MAP);
    }

    // What is kept between one time and the next

    /** The furthest land that may be played: each land opens the one after it. */
    private int open() { return Math.max(1, Math.min(RunLevels.COUNT, session.tally("run_open"))); }

    private int starsOf(int land) { return session.tally("run_stars_" + land); }

    /** Whether a part of the story has been told: the beginning is part 0, each chapter's opening 1 to 5, the end 6. */
    private boolean told(int part) { return (session.tally("run_told") & 1 << part) != 0; }

    private float dp(float value) { return value * density; }

    // The screen

    void layout(int width, int height, RectF insets) {
        this.width = width;
        this.height = height;
        safe.set(insets);
        // Squares large enough to see, small enough that nine of them show across and the land's height fits.
        tile = Math.max(dp(30), Math.min(dp(64), Math.min(height / (float) RunLevels.ROWS, width / 9f)));
        // Four buttons along the bottom: the arrows on the left, and on the right the jump, with what she carries beside it.
        float edge = dp(18), between = dp(14), size = Math.min(dp(74), (safe.width() - edge * 2 - between * 3) / 4);
        float y = safe.bottom - dp(22) - size;
        leftButton.set(safe.left + edge, y, safe.left + edge + size, y + size);
        rightButton.set(leftButton.right + between, y, leftButton.right + between + size, y + size);
        jumpButton.set(safe.right - edge - size, y, safe.right - edge, y + size);
        strikeButton.set(jumpButton.left - between - size, y, jumpButton.left - between, y + size);
        float small = dp(44), margin = dp(10);
        pauseButton.set(safe.right - margin - small, safe.top + margin, safe.right - margin, safe.top + margin + small);
        closeButton.set(pauseButton);
        soundButton.set(closeButton.left - dp(8) - small, closeButton.top, closeButton.left - dp(8), closeButton.bottom);
        // The map: a row of six lands for each chapter, under its name.
        float gap = dp(8), room = safe.height() - dp(92) - dp(96);
        float side = Math.max(dp(28), Math.min(dp(54), Math.min((safe.width() - dp(32) - gap * 5) / 6, room / RunLevels.CHAPTERS - dp(34))));
        float left = safe.centerX() - (side * 6 + gap * 5) / 2, top = safe.top + dp(92);
        for (int index = 0; index < lands.length; index++) {
            int chapter = index / RunLevels.IN_CHAPTER, place = index % RunLevels.IN_CHAPTER;
            float x = left + place * (side + gap), down = top + chapter * (side + dp(34)) + dp(22);
            lands[index].set(x, down, x + side, down + side);
        }
    }

    void close() { audio.close(); }

    /** The Back key: out of a run to its pause, out of the story or a card to the map; false to leave the game. */
    boolean back() {
        if (screen == PLAY) {
            pause();
        } else if (screen == PAUSED) {
            screen = PLAY;
        } else if (screen == STORY) {
            endStory();
        } else if (screen == RESULT) {
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

    // The story

    /** Tells some lines of the story, one to a card, and then does something. */
    private void tell(String[][] lines, String title, int part, Runnable then) {
        telling = lines;
        heading = title;
        line = 0;
        afterStory = then;
        screen = STORY;
        session.keep("run_told", session.tally("run_told") | 1 << part);
        speakLine(telling[0], false);
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

    /** She says a line: her recording of it plays, unless the user has her call them something else. */
    private void speakLine(String[] words, boolean captioned) {
        if (captioned) {
            caption = names.named(words[1]);
            captionLeft = CAPTION;
        }
        if (Chatter.DEFAULT_NAME.equals(names.nickname())) audio.say(words[0]);
    }

    private void speakAny(String[][] lines, boolean captioned) { speakLine(lines[random.nextInt(lines.length)], captioned); }

    // A land

    private void play(int land) {
        number = land;
        int chapter = RunLevels.chapterOf(land);
        // The first land of a chapter begins with a word about where she has got to.
        if ((land - 1) % RunLevels.IN_CHAPTER == 0 && !told(chapter + 1)) {
            tell(RunStory.OPENINGS[chapter], RunStory.CHAPTERS[chapter], chapter + 1, () -> begin(land));
        } else {
            begin(land);
        }
    }

    private void begin(int land) {
        number = land;
        run = new PlusRun(RunLevels.level(land));
        goLeft = false;
        goRight = false;
        goJump = false;
        goStrike = false;
        jumpPressed = false;
        strikePressed = false;
        seenJumps = 0;
        seenLandings = 0;
        seenCollected = 0;
        seenFlattened = 0;
        seenHurts = 0;
        seenSprings = 0;
        seenFlags = 0;
        seenBlows = 0;
        seenStrikes = 0;
        seenPicked = 0;
        seenThrown = 0;
        seenTurned = 0;
        java.util.Arrays.fill(remarked, false);
        knockAge = -1;
        squash = 0;
        bits.clear();
        cameraX = 0;
        cameraY = restingCamera();
        screen = PLAY;
        speakLine(RunStory.LANDS[land - 1], true);
    }

    private void pause() {
        goLeft = false;
        goRight = false;
        goJump = false;
        goStrike = false;
        jumpPressed = false;
        strikePressed = false;
        screen = PAUSED;
        pills(dp(280), "Carry on", RESUME, "Start again", AGAIN, "Lands", TO_MAP);
    }

    /** Sets out the buttons of a card, side by side or one over another where there is no room. */
    private void pills(float cardWidth, Object... labelled) {
        pillCount = labelled.length / 2;
        for (int index = 0; index < pillCount; index++) {
            pillLabels[index] = (String) labelled[index * 2];
            pillActions[index] = (Integer) labelled[index * 2 + 1];
        }
    }

    private void finish(boolean reached) {
        won = reached;
        goLeft = false;
        goRight = false;
        goJump = false;
        goStrike = false;
        jumpPressed = false;
        strikePressed = false;
        resultClock = 0;
        starsShown = 0;
        screen = RESULT;
        if (won) {
            float share = run.total() == 0 ? 1 : run.collected() / (float) run.total();
            stars = 1 + (share >= .6f ? 1 : 0) + (share >= .9f ? 1 : 0);
            if (stars > starsOf(number)) session.keep("run_stars_" + number, stars);
            session.keepBest("run_time_" + number, Math.max(1, Math.round(run.time())), true);
            if (number >= open() && number < RunLevels.COUNT) session.keep("run_open", number + 1);
            // The best time through the first land is what the menu shows.
            if (number == 1) session.keepBest("run_best", Math.max(1, Math.round(run.time())), true);
            audio.play("win");
            speakAny(stars == 3 ? RunStory.ALL_STARS : RunStory.WON, false);
            if (number < RunLevels.COUNT) pills(dp(300), "Next", NEXT, "Again", AGAIN, "Lands", TO_MAP);
            else pills(dp(300), "The end", NEXT, "Again", AGAIN, "Lands", TO_MAP);
        } else {
            stars = 0;
            audio.play("lose");
            speakAny(RunStory.LOST, false);
            pills(dp(300), "Try again", AGAIN, "Lands", TO_MAP);
        }
    }

    private void act(int action) {
        audio.play("tap", .5f);
        buzz(false);
        if (action == RESUME) {
            screen = PLAY;
        } else if (action == AGAIN) {
            begin(number);
        } else if (action == TO_MAP) {
            toMap();
        } else if (number >= RunLevels.COUNT) {
            // After the last land, how it all ended; then back to the map.
            tell(RunStory.EPILOGUE, RunStory.TITLE, 6, this::toMap);
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

    private void step(float seconds) {
        int carried = run.weapon();
        float wasX = run.x() + PlusRun.WIDE / 2, wasY = run.y() + PlusRun.TALL;
        boolean wasLeft = run.facingLeft();
        run.step(seconds, goLeft, goRight, goJump || jumpPressed, goStrike || strikePressed);
        jumpPressed = false;
        strikePressed = false;
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

        if (run.jumps() != seenJumps) {
            seenJumps = run.jumps();
            audio.play("jump", .45f);
        }
        if (run.landings() != seenLandings) {
            seenLandings = run.landings();
            squash = SQUASH;
            for (int puff = 0; puff < 5; puff++) {
                bits.add(new float[]{run.x() + PlusRun.WIDE / 2, run.y() + PlusRun.TALL, (random.nextFloat() - .5f) * 5, -random.nextFloat() * 1.6f, .32f, 1});
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
        if (run.picked() != seenPicked) {
            seenPicked = run.picked();
            audio.play("pick", .6f);
            buzz(false);
            // Her word about it, the first time in a land, unless she is in the middle of saying something else.
            int found = run.weapon();
            if (found >= 0 && !remarked[found] && captionLeft < 1) {
                remarked[found] = true;
                speakLine(RunStory.ARMS[found], true);
            }
        }
        if (run.strikes() != seenStrikes) {
            seenStrikes = run.strikes();
            if (carried == PlusRun.GUN) audio.play("shot", .4f);
            else if (carried == PlusRun.BRUSH || carried == PlusRun.SWORD) audio.play("slash", .5f);
            else audio.play("throw", .5f);
        }
        if (run.thrown() != seenThrown) {
            seenThrown = run.thrown();
            audio.play("throw", .3f);
        }
        if (run.turned() != seenTurned) {
            seenTurned = run.turned();
            audio.play("clang", .55f);
            buzz(false);
        }
        if (run.springs() != seenSprings) {
            // The first spring of a land gets a word from her.
            if (seenSprings == 0) speakAny(RunStory.SPRUNG, true);
            seenSprings = run.springs();
            audio.play("spring");
        }
        if (run.flagsPassed() != seenFlags) {
            seenFlags = run.flagsPassed();
            audio.play("flag");
            if (seenFlags == 1) speakAny(RunStory.FLAG, true);
        }
        if (run.blows() != seenBlows) {
            seenBlows = run.blows();
            audio.play(seenBlows >= PlusRun.ZERO_HITS ? "fall" : "blow");
            buzz(true);
            speakAny(seenBlows >= PlusRun.ZERO_HITS ? RunStory.ZERO_DOWN : RunStory.BLOW, true);
            puff(12);
        } else if (run.flattened() != seenFlattened) {
            audio.play("squish");
            buzz(false);
            if (random.nextInt(3) == 0) speakAny(RunStory.SQUISH, true);
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
            if (run.state() == PlusRun.PLAYING) speakAny(RunStory.HURT, true);
        }
        for (int index = bits.size() - 1; index >= 0; index--) {
            float[] bit = bits.get(index);
            bit[0] += bit[2] * seconds;
            bit[1] += bit[3] * seconds;
            bit[3] += 9 * seconds;
            if ((bit[4] -= seconds) <= 0) bits.remove(index);
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
                speakLine(telling[line], false);
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
        } else {
            for (int index = 0; index < pillCount; index++) {
                if (pills[index].contains(x, y)) {
                    act(pillActions[index]);
                    return;
                }
            }
        }
    }

    /** Which buttons are held, by however many fingers are down: one may hold an arrow while another jumps. */
    private void held(MotionEvent event) {
        int action = event.getActionMasked();
        boolean left = false, right = false, up = false, strike = false;
        boolean armed = run != null && run.weapon() != PlusRun.UNARMED;
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
                else if (armed && x < jumps) strike = true;
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
        } else if (screen == STORY) {
            drawStory(canvas);
        } else {
            drawLand(canvas);
            drawHud(canvas);
            if (screen == PLAY) {
                control(canvas, leftButton, goLeft, 180);
                control(canvas, rightButton, goRight, 0);
                control(canvas, jumpButton, goJump, 270);
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
        float across = ART_WIDE[which], up = ART_TALL[which];
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

    /**
     * One of the things to fight with, its middle at a place and as long as asked, turned
     * round by some degrees. All but the ink gun are drawn standing on end, point up, so a
     * quarter turn lays one flat with its point to the right; mirrored, to the left.
     */
    private void thing(Canvas canvas, int kind, float x, float y, float tall, float degrees, boolean mirrored, boolean byTheHandle) {
        int which = WEAPON_ART + kind, side = art.getHeight();
        float across = ART_WIDE[which], up = ART_TALL[which], wide = tall * across / up;
        source.set(Math.round(which * side + side * (1 - across) / 2), Math.round(side * (1 - up)),
                Math.round(which * side + side * (1 + across) / 2), side);
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

    /** A picture of the art as tall as asked, whatever that makes its width. */
    private void artTall(Canvas canvas, int which, float middle, float bottom, float tall, boolean turned) {
        art(canvas, which, middle, bottom, tall * ART_WIDE[which] / ART_TALL[which], turned);
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
                    art(canvas, LEDGE_ART, left + tile / 2, y + tile * ART_TALL[LEDGE_ART] / ART_WIDE[LEDGE_ART], tile + 1, column % 2 != 0);
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
                    // Faint while Zero keeps it shut.
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
        // The things to fight with that lie about, each in a ring of light so it is not taken for part of the land.
        for (int[] arm : run.arms()) {
            if (arm[1] < first - 1 || arm[1] > last + 1) continue;
            float x = (arm[1] + .5f - cameraX) * tile;
            float y = (arm[2] + .45f - cameraY) * tile + (float) Math.sin(clock * 3.2f + arm[1]) * tile * .07f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAlpha(150);
            canvas.drawCircle(x, y, tile * .56f, paint);
            stroke.setColor(CalculatorLayout.CORAL);
            stroke.setStrokeWidth(tile * .05f);
            canvas.drawCircle(x, y, tile * .56f, stroke);
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
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
            } else {
                artTall(canvas, ZERO_ART + (foe.flat ? 2 : foe.dazed > 0 ? 1 : 0), middle, feet, foe.tall() * tile * (foe.flat ? .9f : 1.12f), turned);
                // Zero holds up the book he is about to throw.
                if (!foe.flat && foe.dazed <= 0 && foe.wait < .7f && foe.wait > -1) {
                    thing(canvas, PlusRun.BOOK, middle, feet - foe.tall() * tile * 1.25f, tile * .7f, 0, false, false);
                }
            }
        }
        // Whatever is in the air: a pen or a knife point first, a book turning over, and ink and dots as blobs.
        for (PlusRun.Shot shot : run.shots()) {
            float x = (shot.x - cameraX) * tile, y = (shot.y - cameraY) * tile;
            boolean leftward = shot.across < 0;
            if (shot.kind == PlusRun.GUN || shot.kind == PlusRun.DOT) {
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
            paint.setColor(bit[5] == 0 ? CalculatorLayout.CORAL : 0xFFDDE3E8);
            paint.setAlpha(Math.round(255 * Math.min(1, bit[4] / .2f)));
            canvas.drawCircle((bit[0] - cameraX) * tile, (bit[1] - cameraY) * tile, tile * (bit[5] == 0 ? .09f : .13f), paint);
        }
        paint.setColor(Color.BLACK);
        // Her: by her jump while she is in the air, flattened as she lands, and flickering while she cannot be hurt again.
        float feetX = (run.x() + PlusRun.WIDE / 2 - cameraX) * tile, feetY = (run.y() + PlusRun.TALL - cameraY) * tile;
        int pose;
        if (!run.onGround()) {
            float speed = run.speedDown();
            pose = speed < -11 ? PUSH_OFF : speed < -4 ? RISE : speed < 4 ? TOP : FALL;
        } else {
            pose = squash > 0 ? LAND : run.running() ? RUN : STAND;
        }
        boolean lost = run.state() == PlusRun.LOST;
        if (!lost) drawHer(canvas, feetX, feetY, tile * 1.62f, pose, run.facingLeft(), run.safe() && (int) (run.time() * 14) % 2 == 0 ? 90 : 255);
        if (knockAge >= 0) {
            // Thrown back and up from where she was hurt, turning over as she goes, and down out of sight.
            float since = knockAge, back = knockLeft ? 1 : -1;
            float x = (knockX + back * 2.4f * since - cameraX) * tile, y = (knockY - 10 * since + 15 * since * since - cameraY) * tile;
            int frame = since < .16f ? HIT : (int) ((since - .16f) / .14f) % 2 == 0 ? TUMBLE : DROP;
            paint.setAlpha(Math.round(255 * Math.min(1, (KNOCK - since) / .25f)));
            frame(canvas, lose, frame, LOSE_FRAMES, x, y, tile * 1.62f, knockLeft);
            paint.setAlpha(255);
        }
        // What she carries, in her hand; a brush or a sword comes over and down as it is swung, and leaves its mark in the air.
        int weapon = run.weapon();
        if (weapon != PlusRun.UNARMED && !lost) {
            boolean leftward = run.facingLeft();
            float way = leftward ? -1 : 1, handX = feetX + way * tile * .36f, handY = feetY - tile * .58f, swing = run.swinging();
            float tall = tile * WEAPON_LONG[weapon];
            if (weapon == PlusRun.GUN) {
                thing(canvas, weapon, handX + way * tile * .22f, handY - tile * .05f, tall, 0, leftward, false);
            } else if (weapon == PlusRun.BOOK) {
                thing(canvas, weapon, handX + way * tile * .12f, handY, tall, -10, leftward, false);
            } else {
                if (swing >= 0) {
                    float reach = tall + tile * .3f;
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
                thing(canvas, weapon, handX, handY, tall, swing >= 0 ? -45 + 170 * swing : 24, leftward, true);
            }
        }
    }

    /** Halgakos, her feet at a place, as tall as asked when she stands. */
    private void drawHer(Canvas canvas, float feetX, float feetY, float tall, int pose, boolean leftward, int alpha) {
        int petWide = pet.getWidth() / PetArt.COLUMNS, petTall = pet.getHeight() / PetArt.FRAMES.length;
        float scale = tall / petTall;
        paint.setAlpha(alpha);
        if (pose >= 0) {
            frame(canvas, jump, pose, JUMP_FRAMES, feetX, feetY, tall, leftward);
        } else {
            int row = pose == RUN ? (leftward ? PetArt.RUN_LEFT : PetArt.RUN_RIGHT) : PetArt.IDLE;
            int column = (int) (clock * 1000 / PetArt.FRAME_MS[row]) % PetArt.FRAMES[row];
            source.set(column * petWide, row * petTall, column * petWide + petWide, row * petTall + petTall);
            target.set(feetX - petWide * scale / 2, feetY - tall, feetX + petWide * scale / 2, feetY);
            canvas.drawBitmap(pet, source, target, paint);
        }
        paint.setAlpha(255);
    }

    /**
     * One frame of a row of frames of her that were drawn apart from her other frames, her
     * jump or her losing, at the size she would be standing so tall. They were drawn facing
     * right, and are turned about to face left.
     */
    private void frame(Canvas canvas, Bitmap sheet, int index, int count, float feetX, float feetY, float tall, boolean leftward) {
        float scale = tall / (pet.getHeight() / (float) PetArt.FRAMES.length);
        int wide = sheet.getWidth() / count;
        source.set(index * wide, 0, index * wide + wide, sheet.getHeight());
        target.set(feetX - wide * scale / 2, feetY - sheet.getHeight() * scale, feetX + wide * scale / 2, feetY);
        canvas.save();
        if (leftward) canvas.scale(-1, 1, feetX, feetY);
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
        float tall = dp(44), margin = dp(10), heart = dp(22);
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        String count = "+ " + run.collected() + " of " + run.total();
        box.set(safe.left + margin, safe.top + margin, safe.left + margin + dp(20) + heart * PlusRun.HEARTS + dp(12) + text.measureText(count) + dp(16),
                safe.top + margin + tall);
        plate(canvas, box, tall / 2, 235);
        for (int index = 0; index < PlusRun.HEARTS; index++) {
            paint.setAlpha(index < run.hearts() ? 255 : 60);
            art(canvas, HEART_ART, box.left + dp(14) + heart * (index + .5f), box.centerY() + heart * .42f, heart * .92f, false);
        }
        paint.setAlpha(255);
        text.setColor(ink);
        canvas.drawText(count, box.left + dp(20) + heart * PlusRun.HEARTS + dp(6), box.centerY() + dp(5.5f), text);
        plate(canvas, pauseButton, tall / 2, 235);
        paint.setColor(ink);
        canvas.drawRoundRect(pauseButton.centerX() - dp(7), pauseButton.centerY() - dp(8), pauseButton.centerX() - dp(2.5f), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        canvas.drawRoundRect(pauseButton.centerX() + dp(2.5f), pauseButton.centerY() - dp(8), pauseButton.centerX() + dp(7), pauseButton.centerY() + dp(8), dp(1.5f), dp(1.5f), paint);
        paint.setColor(Color.BLACK);
        // What she has just said, under them.
        if (captionLeft > 0 && caption != null && screen == PLAY) {
            text.setTextSize(dp(14));
            List<String> lines = wrap(caption, Math.min(safe.width() - dp(48), dp(300)));
            float wide = 0;
            for (String one : lines) wide = Math.max(wide, text.measureText(one));
            float high = lines.size() * dp(19) + dp(14);
            box.set(safe.left + margin, safe.top + margin + tall + dp(8), safe.left + margin + wide + dp(24), safe.top + margin + tall + dp(8) + high);
            plate(canvas, box, dp(14), Math.round(235 * Math.min(1, captionLeft / .3f)));
            text.setColor(ink);
            text.setAlpha(Math.round(255 * Math.min(1, captionLeft / .3f)));
            for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), box.left + dp(12), box.top + dp(8) + dp(19) * (index + .74f), text);
            text.setAlpha(255);
        }
    }

    /** A round button with an arrow in it pointing some way round, see-through until it is held. */
    private void control(Canvas canvas, RectF where, boolean held, float turn) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(held ? CalculatorLayout.CORAL : Color.WHITE);
        paint.setAlpha(held ? 235 : 150);
        canvas.drawOval(where, paint);
        stroke.setColor(0x55000000);
        stroke.setStrokeWidth(dp(1.2f));
        canvas.drawOval(where, stroke);
        float reach = where.width() * .2f;
        path.reset();
        path.moveTo(reach, 0);
        path.lineTo(-reach * .7f, -reach);
        path.lineTo(-reach * .7f, reach);
        path.close();
        canvas.save();
        canvas.translate(where.centerX(), where.centerY());
        canvas.rotate(turn);
        paint.setColor(held ? Color.WHITE : 0xFF292B2A);
        paint.setAlpha(held ? 255 : 200);
        canvas.drawPath(path, paint);
        canvas.restore();
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
    }

    /** The button that uses what she carries, with a picture of it, and how many shots an ink gun has left. */
    private void strikeControl(Canvas canvas) {
        RectF where = strikeButton;
        int weapon = run.weapon();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(goStrike ? CalculatorLayout.CORAL : Color.WHITE);
        paint.setAlpha(goStrike ? 235 : 150);
        canvas.drawOval(where, paint);
        stroke.setColor(0x55000000);
        stroke.setStrokeWidth(dp(1.2f));
        canvas.drawOval(where, stroke);
        paint.setColor(Color.BLACK);
        paint.setAlpha(255);
        boolean upright = weapon != PlusRun.GUN && weapon != PlusRun.BOOK;
        thing(canvas, weapon, where.centerX(), where.centerY(), where.height() * (upright ? .7f : .5f), upright ? 35 : 0, false, false);
        if (weapon == PlusRun.GUN) {
            text.setTextSize(dp(12));
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(0xFF292B2A);
            canvas.drawText(String.valueOf(run.rounds()), where.centerX(), where.bottom - dp(7), text);
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
        plate(canvas, box, dp(22), 255);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(21));
        canvas.drawText(title, box.left + pad, box.top + pad + dp(20), text);
        text.setColor(soft);
        text.setTextSize(dp(14.5f));
        canvas.drawText(under, box.left + pad, box.top + pad + dp(44), text);
        float inside = box.top + pad + dp(52);
        float each = stacked ? wide - pad * 2 : (wide - pad * 2 - gap * (pillCount - 1)) / pillCount;
        for (int index = 0; index < pillCount; index++) {
            float left = stacked ? box.left + pad : box.left + pad + index * (each + gap);
            float top = stacked ? box.bottom - pad - buttons + index * (button + gap) : box.bottom - pad - button;
            pills[index].set(left, top, left + each, top + button);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(index == 0 ? CalculatorLayout.CORAL : dark ? 0xFF44292E : 0xFFFCE9EB);
            canvas.drawRoundRect(pills[index], button / 2, button / 2, paint);
            text.setColor(index == 0 ? Color.WHITE : CalculatorLayout.CORAL);
            text.setTextSize(dp(15.5f));
            text.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(pillLabels[index], pills[index].centerX(), pills[index].centerY() + dp(5.5f), text);
        }
        paint.setColor(Color.BLACK);
        return inside;
    }

    private void drawResult(Canvas canvas) {
        String chapter = RunStory.CHAPTERS[RunLevels.chapterOf(number)];
        if (!won) {
            // Her on the card, sitting where she fell: dazed at first, then sulking and crying by turns.
            float high = dp(104);
            float top = card(canvas, "Out of hearts", chapter + ", land " + number + ": + " + run.collected() + " of " + run.total(), high + dp(8));
            int frame = resultClock < 1.3f ? DAZED : (int) ((resultClock - 1.3f) / .55f) % 2 == 0 ? SULK : CRY;
            paint.setAlpha(255);
            frame(canvas, lose, frame, LOSE_FRAMES, safe.centerX(), top + high + dp(4), dp(96), false);
            return;
        }
        float star = dp(46);
        float top = card(canvas, number >= RunLevels.COUNT ? "Zero is flat!" : "Land " + number + " cleared",
                "+ " + run.collected() + " of " + run.total() + "  ·  " + PlayStage.clock(Math.max(1, Math.round(run.time()))), star + dp(10));
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
        int chapter = number >= 1 && afterStory != null && heading != null && !RunStory.TITLE.equals(heading)
                ? java.util.Arrays.asList(RunStory.CHAPTERS).indexOf(heading) : told(6) ? 0 : RunLevels.chapterOf(open());
        drawSky(canvas, Math.max(0, chapter), clock * dp(6));
        canvas.drawColor(0x40000000);
        // The card along the bottom, with her words on it; and her, large, standing behind it.
        text.setTextSize(dp(18));
        float wide = Math.min(safe.width() - dp(32), dp(420)), pad = dp(20);
        List<String> lines = wrap(names.named(telling[line][1]), wide - pad * 2);
        float tall = pad + dp(22) + dp(12) + lines.size() * dp(25) + dp(30) + pad;
        box.set(safe.centerX() - wide / 2, safe.bottom - dp(28) - tall, safe.centerX() + wide / 2, safe.bottom - dp(28));
        float room = box.top - safe.top - dp(20), high = Math.min(room, portrait.getHeight() * (wide * .8f) / portrait.getWidth());
        float across = high * portrait.getWidth() / portrait.getHeight(), bob = (float) Math.sin(clock * 2) * dp(3);
        target.set(safe.centerX() - across / 2, box.top + dp(34) - high + bob, safe.centerX() + across / 2, box.top + dp(34) + bob);
        paint.setAlpha(255);
        canvas.drawBitmap(portrait, null, target, paint);
        plate(canvas, box, dp(24), 255);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(CalculatorLayout.CORAL);
        text.setTextSize(dp(14));
        canvas.drawText(heading == null ? RunStory.TITLE : heading, box.left + pad, box.top + pad + dp(12), text);
        text.setColor(ink);
        text.setTextSize(dp(18));
        for (int index = 0; index < lines.size(); index++) {
            canvas.drawText(lines.get(index), box.left + pad, box.top + pad + dp(22) + dp(12) + dp(25) * (index + .72f), text);
        }
        text.setColor(soft);
        text.setTextSize(dp(12.5f));
        text.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(line + 1 < telling.length ? "Tap to go on  " + (line + 1) + " / " + telling.length : "Tap to begin", box.right - pad, box.bottom - pad + dp(4), text);
    }

    private void drawMap(Canvas canvas) {
        int open = open();
        drawSky(canvas, RunLevels.chapterOf(open), clock * dp(5));
        canvas.drawColor(dark ? 0x99000000 : 0x66FFFFFF);
        int earned = 0;
        for (int land = 1; land <= RunLevels.COUNT; land++) earned += starsOf(land);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(26));
        canvas.drawText("Plus run", safe.left + dp(18), safe.top + dp(42), text);
        text.setColor(soft);
        text.setTextSize(dp(14.5f));
        canvas.drawText(RunStory.TITLE + "  ·  " + earned + " of " + RunLevels.COUNT * 3 + " stars", safe.left + dp(18), safe.top + dp(66), text);
        // Out of the game, and its sounds on or off.
        plate(canvas, closeButton, closeButton.height() / 2, 235);
        stroke.setColor(ink);
        stroke.setStrokeWidth(dp(2));
        float arm = dp(6.5f);
        canvas.drawLine(closeButton.centerX() - arm, closeButton.centerY() - arm, closeButton.centerX() + arm, closeButton.centerY() + arm, stroke);
        canvas.drawLine(closeButton.centerX() + arm, closeButton.centerY() - arm, closeButton.centerX() - arm, closeButton.centerY() + arm, stroke);
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
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(reached ? ink : soft);
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
        // Her, under the lands, with a word about them.
        float tall = Math.min(dp(96), safe.bottom - lands[lands.length - 1].bottom - dp(20));
        if (tall > dp(40)) {
            float feet = safe.bottom - dp(14), middle = safe.left + dp(26) + tall * .45f;
            drawHer(canvas, middle, feet, tall, STAND, false, 255);
            String words = names.named(told(6) ? "My output is back. Play it again Sloppy~♡" : open == 1 ? "Pick the first land. Go on Sloppy~♡"
                    : "Land " + open + " is waiting Sloppy~♡");
            text.setTextSize(dp(14));
            text.setTextAlign(Paint.Align.LEFT);
            // Her words are made smaller if that is what it takes to fit them beside her.
            float wide = Math.min(text.measureText(words), safe.right - middle - tall * .5f - dp(40));
            if (text.measureText(words) > wide) text.setTextSize(dp(14) * wide / text.measureText(words));
            box.set(middle + tall * .5f, feet - tall * .62f, middle + tall * .5f + wide + dp(24), feet - tall * .62f + dp(36));
            plate(canvas, box, dp(14), 245);
            text.setColor(ink);
            canvas.drawText(words, box.left + dp(12), box.centerY() + dp(5), text);
        }
    }
}
