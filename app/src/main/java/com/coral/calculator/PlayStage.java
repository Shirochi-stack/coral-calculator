package com.coral.calculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Halgakos with the whole screen to herself: the games that are played all over it, and the
 * pranks she plays on it. The companion puts this over whatever app is in use, and the app
 * puts it over its own games screen. It draws nothing of the screen underneath and changes
 * nothing there; whatever a prank appears to do is drawn here and gone when it ends.
 *
 * Everything is placed inside the safe area it is given, and where an open foldable's fold
 * is known, what has to be read or pressed is kept to one side of it.
 */
final class PlayStage extends View {
    interface Host {
        /** The game was left, or the prank has run its course: take the stage down. */
        void onStageDone(int game);
    }

    /** The pranks, numbered on from the games of {@link PlaySession}. */
    static final int SNATCH = 9, CRACK = 10, SCRIBBLE = 11;
    private static final int[] PRANKS = {SNATCH, CRACK, SCRIBBLE};

    private static final int INTRO = 0, PLAYING = 1, OVER = 2;
    private static final int NONE = -1;
    private static final int SCRIM = 0x73000000;
    private static final float TAG_SECONDS = 30;
    private static final int HEARTS = 3, BUBBLES = 10;
    private static final float PENALTY_SECONDS = 2;

    private final int game;
    private final boolean dark;
    private final boolean haptics;
    private final Bitmap art;
    private final Bitmap pet;
    private final Host host;
    private final PlaySession session;
    private final Random random = new Random();
    private final float density;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF target = new RectF();
    private final RectF play = new RectF();
    private final RectF box = new RectF();
    private final RectF exit = new RectF();
    /** Where the card before or after a game was last drawn. */
    private final RectF card = new RectF();
    private final RectF[] buttons = {new RectF(), new RectF()};
    private final Path path = new Path();
    private final Runnable frame = this::frame;
    private final int cardColor, cardEdge, ink, soft;

    private int insetLeft, insetTop, insetRight, insetBottom;
    private int hinge = FoldState.NO_HINGE;
    private float homeX = Float.NaN, homeY = Float.NaN;
    private boolean started;
    private boolean running;
    /** The host has been told to take the stage down. */
    private boolean left;
    private long last;

    /** Halgakos: the middle of her feet, her size, and the frame of her that is showing. */
    private float herX, herY, herWidth, herHeight;
    private int pose = PetArt.IDLE, once = NONE, frameIndex;
    private float frameClock;
    private boolean facingLeft;
    private String saying;
    private float sayingLeft;

    private int phase = INTRO;
    private String resultTitle = "", resultBody = "";
    /** Rings that spread from a touch, a catch or a popped bubble: x, y, age, and whether it counted. */
    private final List<float[]> rings = new ArrayList<>();

    // Catch Halgakos
    private float goalX, goalY, rest, dash, timeLeft;
    private int caught;

    // Number rain: each sign is x, y, speed, and 1 for a plus or 0 for a minus.
    private final List<float[]> signs = new ArrayList<>();
    private float fieldLeft, fieldRight, wantedX, spawnIn, elapsed, hurt;
    private int score, hearts;

    // Bubble count: each bubble is x, y, x speed, y speed, its number, and how long it flashes.
    private final List<float[]> bubbles = new ArrayList<>();

    // Pranks
    private int step;
    private float stepClock;
    private Drawable loot;
    private String lootName = "";
    private float lootX, lootY, lootHomeX, lootHomeY, lootScale, lootAlpha, binRise;
    private boolean binShut;
    private final Path marks = new Path();
    private float marksAlpha, flash;
    private final List<float[]> waypoints = new ArrayList<>();

    private Object backCallback;

    /**
     * @param game one of {@link PlaySession#TAG}, {@link PlaySession#RAIN},
     *             {@link PlaySession#ORDER}, or a prank
     * @param pet  the animated Halgakos's frames, or null to draw the still one
     */
    PlayStage(Context context, int game, boolean dark, boolean haptics, Bitmap art, Bitmap pet, Host host) {
        super(context);
        this.game = game;
        this.dark = dark;
        this.haptics = haptics;
        this.art = art;
        this.pet = pet;
        this.host = host;
        session = PlaySession.get(context);
        density = getResources().getDisplayMetrics().density;
        cardColor = dark ? 0xFF1D2023 : Color.WHITE;
        cardEdge = dark ? 0xFF34383C : 0xFFE4E4E4;
        ink = dark ? 0xFFF3F3F3 : 0xFF292B2A;
        soft = dark ? 0xFFC9CDCF : 0xFF565A57;
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        setFocusable(true);
        setFocusableInTouchMode(true);
        // It takes the focus for the Back key alone: Android's wash over a focused view is not wanted.
        setDefaultFocusHighlightEnabled(false);
        setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        setContentDescription(isPrank(game) ? "Halgakos is playing a prank. Tap to end it"
                : PlaySession.NAMES[game] + ", on the whole screen");
        if (game == SNATCH) {
            // Her own app is the one she makes off with: it is the only one she can see.
            try {
                loot = context.getPackageManager().getApplicationIcon(context.getPackageName());
            } catch (RuntimeException | android.content.pm.PackageManager.NameNotFoundException missing) {
                loot = null;
            }
            lootName = context.getString(R.string.app_name);
        }
    }

    static boolean isPrank(int game) { return game >= SNATCH; }

    static int anyPrank(Random random) { return PRANKS[random.nextInt(PRANKS.length)]; }

    /** How far in from each edge of this view the system bars and cutouts reach. */
    void setSafeInsets(int left, int top, int right, int bottom) {
        insetLeft = left;
        insetTop = top;
        insetRight = right;
        insetBottom = bottom;
        if (getWidth() > 0) layout();
    }

    /** Which way an open foldable's fold runs across the screen, as {@link FoldState} names it. */
    void setHinge(int value) {
        hinge = value;
        if (getWidth() > 0) layout();
    }

    /** Where Halgakos stands when this goes up, in this view: she starts from there, and returns. */
    void setHome(float x, float feet) {
        homeX = x;
        homeY = feet;
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        layout();
    }

    /** Fits everything to the room there is: at first, and again when the screen turns or folds. */
    private void layout() {
        play.set(insetLeft, insetTop, getWidth() - insetRight, getHeight() - insetBottom);
        if (play.width() <= 0 || play.height() <= 0) return;
        herHeight = Math.max(dp(84), Math.min(dp(136), Math.min(play.width(), play.height()) * .17f));
        herWidth = herHeight * (pet != null ? (pet.getWidth() / PetArt.COLUMNS) / (float) (pet.getHeight() / PetArt.FRAMES.length)
                : art.getWidth() / (float) art.getHeight());
        if (Float.isNaN(homeX)) {
            homeX = play.right - herWidth / 2 - dp(8);
            homeY = play.bottom - dp(8);
        }
        homeX = clamp(homeX, play.left + herWidth / 2, play.right - herWidth / 2);
        homeY = clamp(homeY, play.top + herHeight, play.bottom);
        // Number rain keeps to a width a thumb can cover, on her side of a fold where it fits.
        float width = Math.min(play.width(), dp(560));
        float middle = hinge == FoldState.UPRIGHT_HINGE && play.width() / 2 >= dp(300) ? sideX() : play.centerX();
        if (hinge == FoldState.UPRIGHT_HINGE && play.width() / 2 >= dp(300)) width = Math.min(width, play.width() / 2);
        fieldLeft = clamp(middle - width / 2, play.left, play.right - width);
        fieldRight = fieldLeft + width;
        if (!started) {
            started = true;
            begin();
        } else {
            herX = clamp(herX, play.left + herWidth / 2, play.right - herWidth / 2);
            herY = clamp(herY, play.top + herHeight, play.bottom);
            if (game == PlaySession.RAIN) {
                herX = clamp(herX, fieldLeft + herWidth / 2, fieldRight - herWidth / 2);
                herY = play.bottom - dp(6);
                wantedX = herX;
            }
            for (float[] bubble : bubbles) {
                bubble[0] = clamp(bubble[0], play.left + dp(34), play.right - dp(34));
                bubble[1] = clamp(bubble[1], play.top + dp(92), play.bottom - dp(34));
            }
            goalX = clamp(goalX, play.left + herWidth / 2, play.right - herWidth / 2);
            goalY = clamp(goalY, play.top + herHeight + dp(56), play.bottom);
        }
    }

    /** The middle of the half of an upright fold that she stands in, or of the whole screen. */
    private float sideX() {
        if (hinge != FoldState.UPRIGHT_HINGE || play.width() / 2 < dp(280)) return play.centerX();
        return play.left + play.width() * (homeX < play.centerX() ? .25f : .75f);
    }

    private float sideY() {
        if (hinge != FoldState.LEVEL_HINGE || play.height() / 2 < dp(260)) return play.centerY();
        return play.top + play.height() * (homeY < play.centerY() ? .25f : .75f);
    }

    private void begin() {
        herX = homeX;
        herY = homeY;
        phase = INTRO;
        step = 0;
        stepClock = 0;
        if (game == CRACK) {
            goalX = sideX();
            goalY = clamp(sideY() + herHeight * .5f, play.top + herHeight, play.bottom);
        } else if (game == SNATCH) {
            // The icon turns up well away from her and from the edges, and above the bin.
            float margin = dp(48);
            do {
                lootX = play.left + margin + random.nextFloat() * Math.max(1, play.width() - margin * 2);
                lootY = play.top + margin + random.nextFloat() * Math.max(1, play.height() * .55f - margin);
            } while (Math.hypot(lootX - herX, lootY - (herY - herHeight / 2)) < Math.min(play.width(), play.height()) * .28f
                    && random.nextInt(8) != 0);
            lootHomeX = lootX;
            lootHomeY = lootY;
            lootScale = 0;
            lootAlpha = 1;
        } else if (game == SCRIBBLE) {
            for (int i = 0; i < 6; i++) {
                waypoints.add(new float[]{play.left + herWidth / 2 + random.nextFloat() * Math.max(1, play.width() - herWidth),
                        play.top + herHeight + random.nextFloat() * Math.max(1, play.height() - herHeight)});
            }
            marks.reset();
            marks.moveTo(herX, herY - herHeight * .35f);
            marksAlpha = 1;
        }
    }

    // The clock

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= 33 && !isPrank(game)) {
            OnBackInvokedCallback callback = this::leave;
            findOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
            backCallback = callback;
        }
        requestFocus();
        resume();
    }

    @Override protected void onDetachedFromWindow() {
        running = false;
        removeCallbacks(frame);
        if (Build.VERSION.SDK_INT >= 33 && backCallback != null) {
            OnBackInvokedDispatcher dispatcher = findOnBackInvokedDispatcher();
            if (dispatcher != null) dispatcher.unregisterOnBackInvokedCallback((OnBackInvokedCallback) backCallback);
            backCallback = null;
        }
        super.onDetachedFromWindow();
    }

    @Override public void onVisibilityAggregated(boolean visible) {
        super.onVisibilityAggregated(visible);
        if (visible) {
            resume();
        } else {
            running = false;
            removeCallbacks(frame);
        }
    }

    private void resume() {
        if (running || !isAttachedToWindow()) return;
        running = true;
        last = 0;
        postOnAnimation(frame);
    }

    private void frame() {
        if (!running) return;
        long now = System.nanoTime();
        // A long gap, as after the screen was off, counts as a short one: nothing leaps.
        float seconds = last == 0 ? 0 : Math.min(.05f, (now - last) / 1e9f);
        last = now;
        if (started) advance(seconds);
        invalidate();
        if (running) postOnAnimation(frame);
    }

    private void advance(float seconds) {
        int row = once != NONE ? once : pose;
        frameClock += seconds * 1000;
        while (frameClock >= PetArt.FRAME_MS[row]) {
            frameClock -= PetArt.FRAME_MS[row];
            if (++frameIndex >= PetArt.FRAMES[row]) {
                frameIndex = 0;
                once = NONE;
                row = pose;
            }
        }
        if (sayingLeft > 0 && (sayingLeft -= seconds) <= 0) saying = null;
        for (int i = rings.size() - 1; i >= 0; i--) {
            if ((rings.get(i)[2] += seconds) > .45f) rings.remove(i);
        }
        if (flash > 0) flash -= seconds;
        if (isPrank(game)) {
            prank(seconds);
        } else if (phase == PLAYING) {
            if (game == PlaySession.TAG) tag(seconds);
            else if (game == PlaySession.RAIN) rain(seconds);
            else bubbles(seconds);
        } else if (card.isEmpty() || runTo(clamp(card.left + herWidth * .65f, play.left + herWidth / 2, play.right - herWidth / 2),
                card.top + dp(3), dp(760), seconds)) {
            // Before a game and after one she stands on top of the card, never behind it.
            keep(PetArt.IDLE);
        }
    }

    // Halgakos herself

    /** The pose she holds when nothing briefer is playing. */
    private void keep(int value) {
        if (pose == value) return;
        pose = value;
        if (once == NONE) {
            frameIndex = 0;
            frameClock = 0;
        }
    }

    /** Plays a pose through once over the one she holds. */
    private void perform(int value) {
        once = value;
        frameIndex = 0;
        frameClock = 0;
    }

    private void say(String words, float seconds) {
        saying = words;
        sayingLeft = seconds;
        // Read out as it changes: the view is a live region.
        setContentDescription(words);
    }

    private void say(float seconds, String... words) {
        say(words[random.nextInt(words.length)], seconds);
    }

    /** Runs her toward a place, and says whether she has reached it. */
    private boolean runTo(float x, float y, float speed, float seconds) {
        float across = x - herX, down = y - herY;
        float distance = (float) Math.hypot(across, down), reach = speed * seconds;
        if (Math.abs(across) > 1) facingLeft = across < 0;
        if (distance <= reach || distance < 1) {
            herX = x;
            herY = y;
            return true;
        }
        herX += across / distance * reach;
        herY += down / distance * reach;
        keep(facingLeft ? PetArt.RUN_LEFT : PetArt.RUN_RIGHT);
        return false;
    }

    private void drawHer(Canvas canvas) {
        target.set(herX - herWidth / 2, herY - herHeight, herX + herWidth / 2, herY);
        paint.setAlpha(255);
        if (pet != null) {
            int row = once != NONE ? once : pose;
            int width = pet.getWidth() / PetArt.COLUMNS, height = pet.getHeight() / PetArt.FRAMES.length;
            int column = Math.min(frameIndex, PetArt.FRAMES[row] - 1);
            source.set(column * width, row * height, (column + 1) * width, (row + 1) * height);
            canvas.drawBitmap(pet, source, target, paint);
        } else {
            // The still Halgakos has the one drawing: she is turned to face the way she runs.
            canvas.save();
            if (facingLeft) canvas.scale(-1, 1, herX, herY);
            canvas.drawBitmap(art, null, target, paint);
            canvas.restore();
        }
    }

    private void drawSaying(Canvas canvas) {
        if (saying == null) return;
        text.setTextSize(dp(14));
        text.setTextAlign(Paint.Align.LEFT);
        float pad = dp(11), line = dp(19);
        List<String> lines = wrap(saying, Math.min(play.width() - dp(40), dp(236)));
        float width = 0;
        for (String one : lines) width = Math.max(width, text.measureText(one));
        float left = clamp(herX - width / 2 - pad, play.left + dp(8), play.right - dp(8) - width - pad * 2);
        float bottom = herY - herHeight - dp(10), top = bottom - lines.size() * line - pad * 1.5f;
        // Under her instead, where there is no room over her head.
        if (top < play.top + dp(6)) {
            top = Math.min(herY + dp(10), play.bottom - lines.size() * line - pad * 1.5f - dp(6));
            bottom = top + lines.size() * line + pad * 1.5f;
        }
        box.set(left, top, left + width + pad * 2, bottom);
        plate(canvas, box, dp(14));
        text.setColor(ink);
        for (int i = 0; i < lines.size(); i++) {
            canvas.drawText(lines.get(i), left + pad, top + pad * .75f + line * (i + .76f), text);
        }
    }

    /** A white card, as the companion's own popups are, with a soft edge to lift it off the screen. */
    private void plate(Canvas canvas, RectF where, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33000000);
        canvas.drawRoundRect(where.left, where.top + dp(2), where.right, where.bottom + dp(3), radius, radius, paint);
        paint.setColor(cardColor);
        canvas.drawRoundRect(where, radius, radius, paint);
        stroke.setColor(cardEdge);
        stroke.setStrokeWidth(Math.max(1, dp(.75f)));
        canvas.drawRoundRect(where, radius, radius, stroke);
    }

    private List<String> wrap(String words, float width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : words.split(" ")) {
            if (line.length() > 0 && text.measureText(line + " " + word) > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    // Catch Halgakos

    private void pickGoal() {
        float far = Math.min(play.width(), play.height()) * .3f;
        for (int tries = 0; tries < 12; tries++) {
            goalX = play.left + herWidth / 2 + random.nextFloat() * Math.max(1, play.width() - herWidth);
            goalY = play.top + herHeight + dp(56) + random.nextFloat() * Math.max(1, play.height() - herHeight - dp(56));
            if (Math.hypot(goalX - herX, goalY - herY) >= far) return;
        }
    }

    private void tag(float seconds) {
        timeLeft -= seconds;
        if (timeLeft <= 0) {
            int best = session.tally("tag_best");
            boolean record = session.keepBest("tag_best", caught, false);
            finish("Time!", "You caught Halgakos " + caught + (caught == 1 ? " time." : " times.")
                    + (record && best != 0 ? " That's a new best!" : best != 0 ? " Your best is " + Math.max(best, caught) + "." : ""));
            return;
        }
        if (dash > 0) dash -= seconds;
        if (rest > 0) {
            rest -= seconds;
            keep(PetArt.IDLE);
            return;
        }
        // The more she is caught, the faster she runs and the less she stops to catch her breath.
        float speed = dp(Math.min(640, 230 + caught * 26)) * (dash > 0 ? 1.6f : 1);
        if (runTo(goalX, goalY, speed, seconds)) {
            rest = Math.max(.12f, .6f - caught * .035f);
            pickGoal();
        }
    }

    private void tagTouch(float x, float y) {
        float slop = dp(12);
        if (x >= herX - herWidth / 2 - slop && x <= herX + herWidth / 2 + slop && y >= herY - herHeight - slop && y <= herY + slop) {
            caught++;
            buzz();
            ring(x, y, true);
            perform(PetArt.JUMP);
            // Caught is when her catchphrase comes out, every third time or so.
            if (random.nextInt(3) == 0) say(Chatter.CATCHPHRASE, 1.4f);
            else say(1.1f, "Eek!", "Got me!", "Again!", "Hey!", "Too quick!", "You're fast!");
            dash = .5f;
            rest = 0;
            pickGoal();
        } else {
            ring(x, y, false);
        }
    }

    // Number rain

    private void rain(float seconds) {
        elapsed += seconds;
        if (hurt > 0) hurt -= seconds;
        float across = wantedX - herX, reach = dp(1500) * seconds;
        if (Math.abs(across) <= reach) {
            herX = wantedX;
            keep(hurt > 0 ? PetArt.SAD : PetArt.IDLE);
        } else {
            facingLeft = across < 0;
            herX += Math.signum(across) * reach;
            keep(hurt > 0 ? PetArt.SAD : facingLeft ? PetArt.RUN_LEFT : PetArt.RUN_RIGHT);
        }
        float radius = dp(20);
        if ((spawnIn -= seconds) <= 0) {
            // They come thicker and faster the longer the round lasts.
            spawnIn = Math.max(.3f, .85f - elapsed * .012f) * (.75f + random.nextFloat() * .5f);
            float speed = dp(Math.min(540, 170 + elapsed * 7)) * (.85f + random.nextFloat() * .3f);
            signs.add(new float[]{fieldLeft + radius + random.nextFloat() * Math.max(1, fieldRight - fieldLeft - radius * 2),
                    play.top - radius, speed, random.nextInt(100) < 68 ? 1 : 0});
        }
        float left = herX - herWidth * .36f, right = herX + herWidth * .36f;
        float top = herY - herHeight * .92f, bottom = herY - herHeight * .2f;
        for (int i = signs.size() - 1; i >= 0; i--) {
            float[] sign = signs.get(i);
            sign[1] += sign[2] * seconds;
            float nearX = clamp(sign[0], left, right), nearY = clamp(sign[1], top, bottom);
            if (Math.hypot(sign[0] - nearX, sign[1] - nearY) <= radius) {
                signs.remove(i);
                if (sign[3] == 1) {
                    score++;
                    ring(sign[0], sign[1], true);
                } else {
                    hearts--;
                    hurt = .6f;
                    buzz();
                    ring(sign[0], sign[1], false);
                    say(1.2f, "Ow!", "Oof! Not that one!", Chatter.CATCHPHRASE, "Ow! " + Chatter.CATCHPHRASE);
                    if (hearts <= 0) {
                        int best = session.tally("rain_best");
                        boolean record = session.keepBest("rain_best", score, false);
                        finish("That's three bumps", "You caught " + score + (score == 1 ? " plus." : " pluses.")
                                + (record && best != 0 ? " That's a new best!" : best != 0 ? " Your best is " + Math.max(best, score) + "." : ""));
                        return;
                    }
                }
            } else if (sign[1] - radius > play.bottom) {
                signs.remove(i);
            }
        }
    }

    // Bubble count

    private void bubbles(float seconds) {
        elapsed += seconds;
        float radius = dp(30);
        for (float[] bubble : bubbles) {
            bubble[0] += bubble[2] * seconds;
            bubble[1] += bubble[3] * seconds;
            if (bubble[0] < play.left + radius || bubble[0] > play.right - radius) {
                bubble[2] = -bubble[2];
                bubble[0] = clamp(bubble[0], play.left + radius, play.right - radius);
            }
            if (bubble[1] < play.top + dp(60) + radius || bubble[1] > play.bottom - radius) {
                bubble[3] = -bubble[3];
                bubble[1] = clamp(bubble[1], play.top + dp(60) + radius, play.bottom - radius);
            }
            if (bubble[5] > 0) bubble[5] -= seconds;
        }
        // Two that meet bounce off one another, so that none hides behind another.
        for (int one = 0; one < bubbles.size(); one++) {
            for (int other = one + 1; other < bubbles.size(); other++) {
                float[] a = bubbles.get(one), b = bubbles.get(other);
                float across = b[0] - a[0], down = b[1] - a[1];
                float apart = (float) Math.hypot(across, down), touching = radius * 2.1f;
                if (apart >= touching) continue;
                if (apart < 1) {
                    across = 1;
                    down = 0;
                    apart = 1;
                }
                float push = (touching - apart) / 2;
                a[0] -= across / apart * push;
                a[1] -= down / apart * push;
                b[0] += across / apart * push;
                b[1] += down / apart * push;
                // They trade the speed they had toward each other, and keep the rest.
                float closing = (a[2] - b[2]) * across / apart + (a[3] - b[3]) * down / apart;
                if (closing > 0) {
                    a[2] -= closing * across / apart;
                    a[3] -= closing * down / apart;
                    b[2] += closing * across / apart;
                    b[3] += closing * down / apart;
                }
            }
        }
    }

    private void bubbleTouch(float x, float y) {
        float radius = dp(30) * 1.15f, nearest = Float.MAX_VALUE;
        float[] touched = null;
        float smallest = Float.MAX_VALUE;
        for (float[] bubble : bubbles) {
            smallest = Math.min(smallest, bubble[4]);
            float distance = (float) Math.hypot(bubble[0] - x, bubble[1] - y);
            if (distance <= radius && distance < nearest) {
                nearest = distance;
                touched = bubble;
            }
        }
        if (touched == null) return;
        if (touched[4] == smallest) {
            bubbles.remove(touched);
            buzz();
            ring(touched[0], touched[1], true);
            if (bubbles.isEmpty()) {
                // Kept in tenths of a second, the lower the better.
                int tenths = Math.max(1, Math.round(elapsed * 10)), best = session.tally("order_best");
                boolean record = session.keepBest("order_best", tenths, true);
                finish("All popped!", "That took " + seconds(tenths) + " seconds."
                        + (record && best != 0 ? " That's a new best!" : best != 0 ? " Your best is " + seconds(Math.min(best, tenths)) + "." : ""));
            }
        } else {
            elapsed += PENALTY_SECONDS;
            touched[5] = .45f;
            ring(x, y, false);
            say(1.2f, "Smaller first!", "Not yet! A smaller one.", "Find the smallest!");
        }
    }

    static String seconds(int tenths) { return tenths / 10 + "." + tenths % 10; }

    // Starting and ending a game

    private void play() {
        phase = PLAYING;
        elapsed = 0;
        saying = null;
        signs.clear();
        bubbles.clear();
        rings.clear();
        if (game == PlaySession.TAG) {
            caught = 0;
            timeLeft = TAG_SECONDS;
            rest = 0;
            dash = 0;
            pickGoal();
            say(1.4f, "Catch me if you can!", "You'll never catch me!");
        } else if (game == PlaySession.RAIN) {
            score = 0;
            hearts = HEARTS;
            hurt = 0;
            spawnIn = .5f;
            herX = clamp(herX, fieldLeft + herWidth / 2, fieldRight - herWidth / 2);
            herY = play.bottom - dp(6);
            wantedX = herX;
        } else {
            float radius = dp(30);
            List<Integer> used = new ArrayList<>();
            for (int i = 0; i < BUBBLES; i++) {
                int number;
                do number = 1 + random.nextInt(99); while (used.contains(number));
                used.add(number);
                float x = 0, y = 0;
                // Apart from one another to begin with, where there is room for that.
                for (int tries = 0; tries < 30; tries++) {
                    x = play.left + radius + random.nextFloat() * Math.max(1, play.width() - radius * 2);
                    y = play.top + dp(60) + radius + random.nextFloat() * Math.max(1, play.height() - dp(60) - radius * 2);
                    boolean clear = true;
                    for (float[] other : bubbles) clear &= Math.hypot(other[0] - x, other[1] - y) > radius * 2.2f;
                    if (clear) break;
                }
                double way = random.nextFloat() * Math.PI * 2;
                float speed = dp(18 + random.nextFloat() * 26);
                bubbles.add(new float[]{x, y, (float) Math.cos(way) * speed, (float) Math.sin(way) * speed, number, 0});
            }
            say(1.6f, "Smallest first!");
        }
    }

    private void finish(String title, String body) {
        phase = OVER;
        resultTitle = title;
        resultBody = body;
        saying = null;
        signs.clear();
        keep(PetArt.IDLE);
        perform(PetArt.JUMP);
    }

    private void leave() {
        if (left) return;
        left = true;
        host.onStageDone(game);
    }

    // Pranks: each is a short scene, and a touch at any point brings on its ending.

    private void prank(float seconds) {
        stepClock += seconds;
        if (game == SNATCH) snatch(seconds);
        else if (game == CRACK) crack(seconds);
        else scribble(seconds);
    }

    private void next() {
        step++;
        stepClock = 0;
    }

    /** She runs back to where she stood, and the stage comes down. */
    private void goHome(float seconds) {
        if (runTo(homeX, homeY, dp(560), seconds)) {
            keep(PetArt.IDLE);
            leave();
        }
    }

    private void snatch(float seconds) {
        float size = dp(56), binX = sideX(), binWidth = dp(64);
        switch (step) {
            case 0: // The icon comes up off the screen.
                lootScale = Math.min(1, stepClock / .3f);
                keep(PetArt.IDLE);
                if (stepClock > .5f) next();
                break;
            case 1: // She runs to it.
                if (runTo(clamp(lootX, play.left + herWidth / 2, play.right - herWidth / 2),
                        clamp(lootY + size / 2 + herHeight * .9f, play.top + herHeight, play.bottom), dp(540), seconds)) next();
                break;
            case 2: { // She carries it over her head to the bin, which comes up to meet her.
                binRise = Math.min(1, binRise + seconds / .35f);
                float beside = binX + (herX < binX ? -1 : 1) * (binWidth / 2 + herWidth * .42f);
                boolean there = runTo(clamp(beside, play.left + herWidth / 2, play.right - herWidth / 2), play.bottom - dp(8),
                        dp(430), seconds);
                lootX = herX;
                lootY = herY - herHeight - size * .42f;
                if (there && binRise >= 1) {
                    facingLeft = herX > binX;
                    keep(PetArt.IDLE);
                    next();
                }
                break;
            }
            case 3: { // In it goes.
                float along = Math.min(1, stepClock / .4f);
                lootX += (binX - lootX) * along;
                lootY += (binTop() + dp(26) - lootY) * along;
                lootScale = 1 - along * .45f;
                if (along >= 1) {
                    lootAlpha = 0;
                    binShut = true;
                    buzz();
                    perform(PetArt.JUMP);
                    say("Oops! I deleted " + lootName + ".", 2.6f);
                    next();
                }
                break;
            }
            case 4: // She lets that sink in.
                if (stepClock > 2.6f) reveal();
                break;
            case 5: { // Out it comes again, and back to where she took it from.
                float along = Math.min(1, stepClock / .6f), eased = 1 - (1 - along) * (1 - along);
                binShut = false;
                lootAlpha = 1;
                lootX = binX + (lootHomeX - binX) * eased;
                lootY = binTop() + dp(26) + (lootHomeY - binTop() - dp(26)) * eased;
                lootScale = .55f + .45f * along + (along < 1 ? .15f * (float) Math.sin(along * Math.PI) : 0);
                if (stepClock > 2.4f) next();
                break;
            }
            default: // The icon goes back into the screen, the bin sinks, and she leaves.
                lootAlpha = Math.max(0, lootAlpha - seconds / .3f);
                binRise = Math.max(0, binRise - seconds / .35f);
                goHome(seconds);
                break;
        }
    }

    private float binTop() {
        float height = dp(76);
        return play.bottom - dp(10) - height + (1 - binRise) * (height + dp(48));
    }

    private void crack(float seconds) {
        switch (step) {
            case 0: // She runs flat out at the middle of the screen.
                if (runTo(goalX, goalY, dp(980), seconds)) {
                    shatter(herX, herY - herHeight * .55f);
                    marksAlpha = 1;
                    flash = .12f;
                    buzz();
                    herX += (facingLeft ? 1 : -1) * dp(18);
                    keep(PetArt.SAD);
                    say(Chatter.CATCHPHRASE + " …Oops. I think I cracked your screen.", 2.8f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.8f) reveal();
                break;
            case 2: // The cracks fade as she owns up.
                marksAlpha = Math.max(0, 1 - stepClock / .7f);
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** Cracks running out from where she hit: jagged spokes, a few of them forked. */
    private void shatter(float x, float y) {
        marks.reset();
        float reach = (float) Math.hypot(play.width(), play.height());
        int spokes = 9 + random.nextInt(4);
        for (int spoke = 0; spoke < spokes; spoke++) {
            double way = Math.PI * 2 * spoke / spokes + (random.nextFloat() - .5f) * .5f;
            jag(x, y, way, reach * (.2f + random.nextFloat() * .36f), true);
        }
    }

    private void jag(float x, float y, double way, float length, boolean forks) {
        marks.moveTo(x, y);
        for (float gone = 0; gone < length; ) {
            float stride = dp(16 + random.nextFloat() * 26);
            way += (random.nextFloat() - .5f) * .55f;
            x += (float) Math.cos(way) * stride;
            y += (float) Math.sin(way) * stride;
            marks.lineTo(x, y);
            gone += stride;
            if (forks && gone > length * .25f && random.nextInt(5) == 0) {
                jag(x, y, way + (random.nextBoolean() ? .7f : -.7f), (length - gone) * (.3f + random.nextFloat() * .3f), false);
                marks.moveTo(x, y);
            }
        }
    }

    private void scribble(float seconds) {
        switch (step) {
            case 0: // She runs about with a crayon, leaving a line behind her.
                if (waypoints.isEmpty()) {
                    keep(PetArt.IDLE);
                    perform(PetArt.JUMP);
                    say("Look! I drew on your screen.", 2.6f);
                    next();
                    break;
                }
                float[] to = waypoints.get(0);
                if (runTo(to[0], to[1], dp(620), seconds)) waypoints.remove(0);
                // Her hand wanders as she runs, so the line loops and wobbles like a scribble.
                marks.lineTo(herX + (float) Math.sin(stepClock * 17) * dp(13),
                        herY - herHeight * .35f + (float) Math.cos(stepClock * 13) * dp(13));
                break;
            case 1:
                if (stepClock > 2.6f) reveal();
                break;
            case 2:
                marksAlpha = Math.max(0, 1 - stepClock / .7f);
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** The ending of a prank: she owns up, and what she seemed to do is undone. */
    private void reveal() {
        waypoints.clear();
        perform(PetArt.JUMP);
        keep(PetArt.IDLE);
        if (game == SNATCH) {
            step = 5;
            binRise = 1;
            say("Just kidding! " + lootName + " is right where it was.", 2.4f);
        } else {
            step = 2;
            say(game == CRACK ? "Just kidding! Not a scratch on it." : "Just kidding! It wipes right off.", 2.2f);
        }
        stepClock = 0;
    }

    private void prankTouch() {
        buzz();
        boolean owningUp = game == SNATCH ? step >= 5 : step >= 2;
        if (owningUp) leave(); else reveal();
    }

    // Touches and keys

    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        int action = event.getActionMasked();
        if (isPrank(game)) {
            if (action == MotionEvent.ACTION_DOWN) prankTouch();
            return true;
        }
        if (action == MotionEvent.ACTION_DOWN && exit.contains(x, y)) {
            buzz();
            leave();
            return true;
        }
        if (phase == PLAYING) {
            if (game == PlaySession.RAIN) {
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE)
                    wantedX = clamp(x, fieldLeft + herWidth / 2, fieldRight - herWidth / 2);
            } else if (action == MotionEvent.ACTION_DOWN) {
                if (game == PlaySession.TAG) tagTouch(x, y); else bubbleTouch(x, y);
            }
            return true;
        }
        if (action != MotionEvent.ACTION_DOWN) return true;
        if (phase == INTRO) {
            if (box.contains(x, y) || buttons[0].contains(x, y)) {
                buzz();
                play();
            }
        } else if (buttons[0].contains(x, y)) {
            buzz();
            play();
        } else if (buttons[1].contains(x, y)) {
            buzz();
            leave();
        }
        return true;
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
            if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) leave();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void buzz() {
        if (haptics) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void ring(float x, float y, boolean counted) {
        rings.add(new float[]{x, y, 0, counted ? 1 : 0});
    }

    // Drawing

    @Override protected void onDraw(Canvas canvas) {
        if (!started) return;
        if (isPrank(game)) {
            drawPrank(canvas);
            return;
        }
        canvas.drawColor(SCRIM);
        // The signs fall behind her; the bubbles drift in front, so she never hides one.
        if (phase == PLAYING && game == PlaySession.RAIN) drawRain(canvas);
        drawHer(canvas);
        if (phase == PLAYING && game == PlaySession.ORDER) drawBubbles(canvas);
        for (float[] ring : rings) {
            float age = ring[2] / .45f;
            stroke.setColor(ring[3] == 1 ? CalculatorLayout.CORAL : Color.WHITE);
            stroke.setAlpha(Math.round(255 * (1 - age)));
            stroke.setStrokeWidth(dp(2.5f));
            canvas.drawCircle(ring[0], ring[1], dp(14) + dp(30) * age, stroke);
        }
        stroke.setAlpha(255);
        drawSaying(canvas);
        drawHud(canvas);
        if (phase == INTRO) {
            drawCard(canvas, PlaySession.NAMES[game], game == PlaySession.TAG
                    ? "She runs all over your screen. Tap her as many times as you can in 30 seconds."
                    : game == PlaySession.RAIN
                    ? "Drag anywhere to move Halgakos. Catch each + and keep clear of each −. Three bumps and it's over."
                    : "Pop the bubbles from the smallest number to the largest, as fast as you can. A wrong one costs two seconds.",
                    "Start", null);
        } else if (phase == OVER) {
            drawCard(canvas, resultTitle, resultBody, "Play again", "Done");
        }
    }

    private void drawHud(Canvas canvas) {
        float size = dp(44), margin = dp(10);
        exit.set(play.right - margin - size, play.top + margin, play.right - margin, play.top + margin + size);
        plate(canvas, exit, size / 2);
        stroke.setColor(ink);
        stroke.setStrokeWidth(dp(2));
        float arm = dp(6.5f);
        canvas.drawLine(exit.centerX() - arm, exit.centerY() - arm, exit.centerX() + arm, exit.centerY() + arm, stroke);
        canvas.drawLine(exit.centerX() + arm, exit.centerY() - arm, exit.centerX() - arm, exit.centerY() + arm, stroke);
        if (phase != PLAYING) return;
        String status = game == PlaySession.TAG ? "Caught " + caught + "  ·  " + (int) Math.ceil(timeLeft) + "s"
                : game == PlaySession.RAIN ? "Caught " + score
                : seconds(Math.round(elapsed * 10)) + "s  ·  " + bubbles.size() + " to go";
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        float width = text.measureText(status) + dp(28) + (game == PlaySession.RAIN ? dp(22) * HEARTS + dp(6) : 0);
        box.set(play.left + margin, play.top + margin, play.left + margin + width, play.top + margin + size);
        plate(canvas, box, size / 2);
        text.setColor(ink);
        canvas.drawText(status, box.left + dp(14), box.centerY() + dp(5.5f), text);
        if (game == PlaySession.RAIN) {
            // A dot for each bump she can still take.
            paint.setStyle(Paint.Style.FILL);
            for (int heart = 0; heart < HEARTS; heart++) {
                paint.setColor(heart < hearts ? CalculatorLayout.CORAL : cardEdge);
                canvas.drawCircle(box.right - dp(20) - dp(22) * (HEARTS - 1 - heart), box.centerY(), dp(7), paint);
            }
        }
    }

    /** A card with a heading, a few lines, and one or two buttons under them. */
    private void drawCard(Canvas canvas, String heading, String body, String first, String second) {
        float width = Math.min(play.width() - dp(32), dp(300)), pad = dp(20), line = dp(21), button = dp(44);
        text.setTextSize(dp(15));
        List<String> lines = wrap(body, width - pad * 2);
        float height = pad + dp(26) + dp(8) + lines.size() * line + dp(16) + button + pad;
        float left = clamp(sideX() - width / 2, play.left + dp(16), play.right - dp(16) - width);
        // Room is kept over the card for her to stand on it.
        float least = play.top + Math.max(dp(64), herHeight + dp(12));
        float top = clamp(sideY() - height / 2, least, Math.max(least, play.bottom - dp(16) - height));
        box.set(left, top, left + width, top + height);
        card.set(box);
        plate(canvas, box, dp(22));
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(20));
        canvas.drawText(heading, left + pad, top + pad + dp(20), text);
        text.setColor(soft);
        text.setTextSize(dp(15));
        for (int i = 0; i < lines.size(); i++) {
            canvas.drawText(lines.get(i), left + pad, top + pad + dp(34) + line * (i + .8f), text);
        }
        float bottom = top + height - pad, gap = dp(10);
        float each = second == null ? width - pad * 2 : (width - pad * 2 - gap) / 2;
        buttons[0].set(left + pad, bottom - button, left + pad + each, bottom);
        buttons[1].set(second == null ? 0 : buttons[0].right + gap, second == null ? 0 : bottom - button,
                second == null ? 0 : left + width - pad, second == null ? 0 : bottom);
        pill(canvas, buttons[0], first, true);
        if (second != null) pill(canvas, buttons[1], second, false);
    }

    private void pill(Canvas canvas, RectF where, String label, boolean standsOut) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(standsOut ? CalculatorLayout.CORAL : dark ? 0xFF44292E : 0xFFFCE9EB);
        canvas.drawRoundRect(where, where.height() / 2, where.height() / 2, paint);
        text.setColor(standsOut ? Color.WHITE : CalculatorLayout.CORAL);
        text.setTextSize(dp(16));
        text.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, where.centerX(), where.centerY() + dp(5.5f), text);
    }

    private void drawRain(Canvas canvas) {
        // Where the signs fall, if that is narrower than the screen.
        if (fieldRight - fieldLeft < play.width() - 1) {
            stroke.setColor(0x55FFFFFF);
            stroke.setStrokeWidth(dp(1));
            canvas.drawLine(fieldLeft, play.top, fieldLeft, play.bottom, stroke);
            canvas.drawLine(fieldRight, play.top, fieldRight, play.bottom, stroke);
        }
        float radius = dp(20), arm = dp(8);
        for (float[] sign : signs) {
            boolean plus = sign[3] == 1;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(plus ? CalculatorLayout.CORAL : 0xFF2A2E31);
            canvas.drawCircle(sign[0], sign[1], radius, paint);
            stroke.setColor(Color.WHITE);
            stroke.setStrokeWidth(dp(2));
            canvas.drawCircle(sign[0], sign[1], radius, stroke);
            stroke.setStrokeWidth(dp(3));
            canvas.drawLine(sign[0] - arm, sign[1], sign[0] + arm, sign[1], stroke);
            if (plus) canvas.drawLine(sign[0], sign[1] - arm, sign[0], sign[1] + arm, stroke);
        }
    }

    private void drawBubbles(Canvas canvas) {
        float radius = dp(30);
        text.setTextSize(dp(21));
        text.setTextAlign(Paint.Align.CENTER);
        for (float[] bubble : bubbles) {
            boolean wrong = bubble[5] > 0;
            float x = bubble[0] + (wrong ? (float) Math.sin(bubble[5] * 60) * dp(4) : 0);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(cardColor);
            canvas.drawCircle(x, bubble[1], radius, paint);
            stroke.setColor(CalculatorLayout.CORAL);
            stroke.setStrokeWidth(dp(wrong ? 4 : 2.5f));
            canvas.drawCircle(x, bubble[1], radius, stroke);
            text.setColor(wrong ? CalculatorLayout.CORAL : ink);
            canvas.drawText(String.valueOf((int) bubble[4]), x, bubble[1] + dp(7.5f), text);
        }
    }

    private void drawPrank(Canvas canvas) {
        if (marksAlpha > 0 && !marks.isEmpty()) {
            if (game == CRACK) {
                // A dark line under a light one, so the cracks show on any screen.
                stroke.setColor(Color.BLACK);
                stroke.setAlpha(Math.round(120 * marksAlpha));
                stroke.setStrokeWidth(dp(3.2f));
                canvas.save();
                canvas.translate(dp(1), dp(1));
                canvas.drawPath(marks, stroke);
                canvas.restore();
                stroke.setColor(Color.WHITE);
                stroke.setAlpha(Math.round(235 * marksAlpha));
                stroke.setStrokeWidth(dp(1.6f));
            } else {
                stroke.setColor(CalculatorLayout.CORAL);
                stroke.setAlpha(Math.round(235 * marksAlpha));
                stroke.setStrokeWidth(dp(7));
            }
            canvas.drawPath(marks, stroke);
            stroke.setAlpha(255);
        }
        if (flash > 0) canvas.drawColor(Color.argb(Math.round(150 * Math.min(1, flash / .12f)), 255, 255, 255));
        if (game == SNATCH) {
            if (binRise > 0) drawBin(canvas);
            if (loot != null && lootAlpha > 0 && lootScale > 0) {
                int half = Math.round(dp(28) * lootScale);
                loot.setBounds(Math.round(lootX) - half, Math.round(lootY) - half, Math.round(lootX) + half, Math.round(lootY) + half);
                loot.setAlpha(Math.round(255 * lootAlpha));
                loot.draw(canvas);
            }
        }
        drawHer(canvas);
        drawSaying(canvas);
    }

    /** A bin drawn the way her menus are: a white card with coral lines, not the system's own. */
    private void drawBin(Canvas canvas) {
        float width = dp(64), height = dp(76), x = sideX(), top = binTop();
        box.set(x - width / 2, top, x + width / 2, top + height);
        plate(canvas, box, dp(16));
        stroke.setColor(CalculatorLayout.CORAL);
        stroke.setStrokeWidth(dp(2.2f));
        float left = x - dp(15), right = x + dp(15), rim = top + dp(26), base = top + height - dp(12);
        path.reset();
        path.moveTo(left, rim);
        path.lineTo(left + dp(3), base);
        path.lineTo(right - dp(3), base);
        path.lineTo(right, rim);
        canvas.drawPath(path, stroke);
        for (int bar = -1; bar <= 1; bar++) canvas.drawLine(x + bar * dp(7), rim + dp(7), x + bar * dp(6), base - dp(6), stroke);
        // The lid stands open until something has gone in.
        canvas.save();
        if (!binShut) canvas.rotate(-28, left - dp(3), rim - dp(5));
        canvas.drawLine(left - dp(4), rim - dp(5), right + dp(4), rim - dp(5), stroke);
        canvas.drawLine(x - dp(5), rim - dp(10), x + dp(5), rim - dp(10), stroke);
        canvas.restore();
    }

    private float dp(float value) { return value * density; }

    private static float clamp(float value, float least, float most) {
        return Math.max(least, Math.min(Math.max(least, most), value));
    }
}
