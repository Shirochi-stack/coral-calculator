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
 * Page brawl as it is played: over the whole of the screen. A fighter is picked from the five
 * of {@link FightCast}; she then meets the other four in turn, each harder than the last,
 * with a word exchanged before every fight. The fighting itself is {@link Fight}; this draws
 * it, sounds it, and turns touches into what the player holds down. {@link PlayStage} hands
 * the screen over to it.
 */
final class FightGame {
    private static final int SELECT = 0, VERSUS = 1, FIGHT = 2, PAUSED = 3, RESULT = 4, LOBBY = 5;
    /** Who plays: one player up the ladder, or two on two devices, this one hosting the fight or joining it. */
    private static final int SOLO = 0, HOST = 2, GUEST = 3;
    private static final int RESUME = 0, AGAIN = 1, TO_SELECT = 2, NEXT = 3;
    /** The buttons, in the order their places are kept. */
    private static final int LEFT = 0, RIGHT = 1, JUMP = 2, GUARD = 3, LIGHT = 4, HEAVY = 5, SPECIAL = 6, DODGE = 7, ULT = 8, BUTTONS = 9;
    private static final int[] STAGES = {R.drawable.fight_stage1, R.drawable.fight_stage2, R.drawable.fight_stage3};
    /** How tall a fighter stands in her frames, which {@link FightFrames} knows the places of and {@link FightPose} chooses among. */
    private static final int STANDING = FightFrames.STAND;
    /** The things that fly and flash, side by side in squares; and how much of its square each fills. */
    private static final int PANEL_FX = 0, KEY_FX = 1, ROLL_FX = 2, BRICK_FX = 3, BAT_FX = 4, TEAR_FX = 5, KNIFE_FX = 6, HEART_FX = 7,
            ORB_FX = 8, BURST_FX = 9;
    private static final float[] FX_WIDE = {1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f, 1.000f};
    private static final float[] FX_TALL = {0.694f, 0.544f, 0.856f, 0.713f, 0.206f, 0.569f, 0.175f, 0.894f, 0.981f, 0.875f};
    /** What each fighter's special button shows. */
    private static final int[] SPECIAL_FX = {PANEL_FX, ROLL_FX, BAT_FX, TEAR_FX, HEART_FX};
    /** What bursts where each fighter's smash comes down. */
    private static final int[] SMASH_FX = {PANEL_FX, BRICK_FX, BURST_FX, TEAR_FX, HEART_FX};
    private static final float HOLD = 1.3f, CAPTION = 2.4f;
    private static final int CYAN = 0xFF35D6E8, GOLD = 0xFFFFC53A;

    private final PlayStage stage;
    private final Context context;
    private final boolean dark, haptics;
    private final PlaySession session;
    private final CompanionPrefs names;
    private final FightAudio audio;
    private final Random random = new Random();
    private final float density;
    private final Bitmap faces, fx;
    private final Bitmap[][] atlases = new Bitmap[Fight.FIGHTERS][];
    /** How much smaller than they are kept her pictures are read in: no larger than this screen has use for. */
    private float detail = 1;
    private Bitmap backdrop;
    private int backdropOf = -1;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF target = new RectF(), safe = new RectF(), box = new RectF();
    private final RectF[] buttons = new RectF[BUTTONS];
    private final boolean[] held = new boolean[BUTTONS], pressed = new boolean[BUTTONS];
    private final RectF pauseButton = new RectF(), closeButton = new RectF(), soundButton = new RectF(), fightButton = new RectF();
    private final RectF[] tiles = new RectF[Fight.FIGHTERS];
    private final RectF[] pills = {new RectF(), new RectF(), new RectF()};
    private final String[] pillLabels = new String[3];
    private final int[] pillActions = new int[3];
    private int pillCount;
    private final Path path = new Path();
    private final Fight.Pad pad = new Fight.Pad();
    private final int cardColor, ink, soft;

    // Two players
    private int mode = SOLO, second, twoStage;
    private final Fight.Pad pad2 = new Fight.Pad();
    private final RectF hostButton = new RectF(), joinButton = new RectF();
    /** Taps on the screen during a clash: how many this player has made, how many of them the fight has been told of, and the other device's. */
    private int tapCount, tapsGiven, theirTaps, theirTapSeen, sentTaps = -1;
    /** The line to the other device, how it finds this one, and how far the two have got in meeting. */
    private FightLink link;
    private FightNearby nearby;
    private boolean greeted;
    /** What the other device's player holds, and has pressed since the fight last moved on; what this one last sent, and how long ago. */
    private int theirHeld, theirPressed, sentPad = -1;
    private float sincePad, theirSpan = Fight.WIDE;
    private String notice;
    private float noticeLeft;
    private final java.io.ByteArrayOutputStream packed = new java.io.ByteArrayOutputStream(512);

    private int width, height;
    private int screen = SELECT;
    private float clock;

    // Who fights whom
    private int player, rung;
    private final int[] ladder = new int[Fight.FIGHTERS - 1];
    private Fight fight;
    private int spoken;
    private String[] opening;

    // The fight as it is shown
    private float unit, groundY, cameraX, hold, shake;
    private int seenHits, seenBlocks, seenThrows, seenFalls, seenMoves, seenRound, seenPhase, seenQuakes, seenDodges, seenClashes;
    /** How long her ultimate's button has been on show, for each side; how long ago the round ended; and where each was a moment ago, for what she leaves behind her as she dodges. */
    private float grandAge = 9, sinceOver, trailAge;
    private final float[][] trail = new float[2][6];
    private final int[] seenState = new int[2], seenLanded = new int[2];
    private final boolean[] seenCharged = new boolean[2];
    private final float[] shownHealth = new float[2];
    /** Flashes where blows land: across and up in the stage's lengths, how long ago, and what kind. */
    private final List<float[]> sparks = new ArrayList<>();
    /** The cuts of Halgakos's translation: how long ago, which of the three, and where. */
    private final List<float[]> slices = new ArrayList<>();
    private final String[] captions = new String[2];
    private final float[] captionLeft = new float[2];
    private String banner, bannerUnder;
    private float bannerLeft;
    private boolean won;

    FightGame(PlayStage stage, Context context, boolean dark, boolean haptics, Bitmap still, Bitmap pet) {
        this.stage = stage;
        this.context = context;
        this.dark = dark;
        this.haptics = haptics;
        session = PlaySession.get(context);
        names = new CompanionPrefs(context);
        audio = new FightAudio(context, names.gameSound());
        density = context.getResources().getDisplayMetrics().density;
        faces = BitmapFactory.decodeResource(context.getResources(), R.drawable.fight_faces);
        fx = BitmapFactory.decodeResource(context.getResources(), R.drawable.fight_fx);
        cardColor = dark ? 0xFF1D2023 : Color.WHITE;
        ink = dark ? 0xFFF3F3F3 : 0xFF292B2A;
        soft = dark ? 0xFFC9CDCF : 0xFF565A57;
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        for (int index = 0; index < buttons.length; index++) buttons[index] = new RectF();
        for (int index = 0; index < tiles.length; index++) tiles[index] = new RectF();
        player = Math.max(0, Math.min(Fight.FIGHTERS - 1, session.tally("fight_last")));
        sheet(player);
    }

    private float dp(float value) { return value * density; }

    /** One of the pictures a fighter's frames are packed into, read in when it is first wanted, at the size this screen has use for. */
    private Bitmap atlas(int fighter, int index) {
        if (atlases[fighter] == null) atlases[fighter] = new Bitmap[FightFrames.ATLASES[fighter].length];
        if (atlases[fighter][index] == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = true;
            options.inDensity = 1000;
            options.inTargetDensity = Math.round(1000 * detail);
            atlases[fighter][index] = BitmapFactory.decodeResource(context.getResources(), FightFrames.ATLASES[fighter][index], options);
        }
        return atlases[fighter][index];
    }

    /** The picture a fighter stands in: enough to show her where she is picked. */
    private void sheet(int fighter) { atlas(fighter, FightFrames.FRAMES[fighter][FightFrames.IDLE1][0]); }

    /** All of a fighter's pictures, read in before a fight so that none has to be in the middle of it. */
    private void preload(int fighter) {
        for (int index = 0; index < FightFrames.ATLASES[fighter].length; index++) atlas(fighter, index);
    }

    /** Lets go of the pictures of everyone but those named: they are large. */
    private void keepOnly(int one, int other) {
        for (int fighter = 0; fighter < atlases.length; fighter++) {
            if (fighter == one || fighter == other || atlases[fighter] == null) continue;
            for (Bitmap picture : atlases[fighter]) if (picture != null) picture.recycle();
            atlases[fighter] = null;
        }
    }

    private Bitmap backdrop(int which) {
        if (backdropOf != which) {
            if (backdrop != null) backdrop.recycle();
            backdrop = BitmapFactory.decodeResource(context.getResources(), STAGES[which]);
            backdropOf = which;
        }
        return backdrop;
    }

    // The screen

    void layout(int width, int height, RectF insets) {
        this.width = width;
        this.height = height;
        safe.set(insets);
        boolean landscape = width > height * 1.15f;
        // The stage is shown as large as lets enough of it be seen: all of it on a wide screen, the middle of it on a tall one.
        unit = Math.max(width / Fight.WIDE, Math.min(width / 6.4f, height * .13f));
        groundY = landscape ? height * .8f : height * .6f;
        if (fight != null) fight.setSpan(width / unit - 1.5f);
        arrange();
        float small = dp(40), margin = dp(10);
        closeButton.set(safe.right - margin - small, safe.top + margin, safe.right - margin, safe.top + margin + small);
        soundButton.set(closeButton.left - dp(8) - small, closeButton.top, closeButton.left - dp(8), closeButton.bottom);
        pauseButton.set(safe.centerX() - dp(17), safe.top + dp(50), safe.centerX() + dp(17), safe.top + dp(84));
        // Picking a fighter: the five across the top; on a wide screen they keep to the left half.
        float room = landscape ? safe.width() * .5f - dp(24) : safe.width() - dp(32);
        float tile = Math.min(dp(86), (room - dp(8) * 4) / 5), from = landscape ? safe.left + dp(16) : safe.centerX() - (tile * 5 + dp(32)) / 2;
        for (int index = 0; index < tiles.length; index++) {
            float x = from + index * (tile + dp(8)), y = safe.top + dp(66);
            tiles[index].set(x, y, x + tile, y + tile);
        }
        float buttonLeft = landscape ? safe.centerX() + dp(8) : safe.left + dp(16);
        fightButton.set(buttonLeft, safe.bottom - dp(22) - dp(54), safe.right - dp(16), safe.bottom - dp(22));
        // Over it, the three ways for two to play: on this screen, or on two devices, hosting or joining.
        // On a wide screen they have the left half, beside the button, under whoever is picked.
        float waysLeft = landscape ? safe.left + dp(16) : buttonLeft, waysRight = landscape ? safe.centerX() - dp(8) : fightButton.right;
        float each = (waysRight - waysLeft - dp(8)) / 2, pillTop = landscape ? fightButton.centerY() - dp(20) : fightButton.top - dp(10) - dp(40);
        hostButton.set(waysLeft, pillTop, waysLeft + each, pillTop + dp(40));
        joinButton.set(hostButton.right + dp(8), pillTop, waysRight, pillTop + dp(40));
    }

    /**
     * The buttons: on the left the arrows with the guard and the jump over them; on the right
     * the quick and the heavy blow with the dodge and the special over them, and over those
     * a place for the ultimate, which is only there while her meter is full.
     */
    private void arrange() {
        boolean landscape = width > height * 1.15f;
        float edge = dp(18), size = Math.min(dp(landscape ? 62 : 68), (safe.width() - edge * 2 - dp(56)) / 4.1f), gap = size * .3f;
        float low = safe.bottom - dp(24) - size, high = low - size - dp(12);
        float left = safe.left + edge, right = safe.right - edge;
        buttons[LEFT].set(left, low, left + size, low + size);
        buttons[RIGHT].set(left + size + gap, low, left + size * 2 + gap, low + size);
        buttons[GUARD].set(left, high, left + size, high + size);
        buttons[JUMP].set(left + size + gap, high, left + size * 2 + gap, high + size);
        buttons[HEAVY].set(right - size, low, right, low + size);
        buttons[LIGHT].set(right - size * 2 - gap, low, right - size - gap, low + size);
        buttons[SPECIAL].set(right - size, high, right, high + size);
        buttons[DODGE].set(right - size * 2 - gap, high, right - size - gap, high + size);
        float grand = size * 1.14f;
        if (landscape) {
            // On a wide screen it goes beside the others, clear of where the two stand.
            float middle = right - size * 2 - gap * 2 - grand / 2, level = (high + low + size) / 2;
            buttons[ULT].set(middle - grand / 2, level - grand / 2, middle + grand / 2, level + grand / 2);
        } else {
            float middle = right - size - gap / 2, top = high - grand - dp(12);
            buttons[ULT].set(middle - grand / 2, top, middle + grand / 2, top + grand);
        }
        groundY = landscape ? height * .8f : height * .6f;
        // Her pictures are read in no larger than she is drawn here, in tenths; a change of screen reads them in again.
        float need = Math.max(.5f, Math.min(1, (float) Math.ceil(Fight.TALL * unit / STANDING * 10.4f) / 10));
        if (need != detail) {
            detail = need;
            keepOnly(-1, -1);
        }
    }

    void close() {
        unlink();
        audio.close();
        keepOnly(-1, -1);
        if (backdrop != null) backdrop.recycle();
        backdrop = null;
    }

    /** The Back key: out of a fight to its pause, out of anything else to the fighters; false to leave the game. */
    boolean back() {
        if (screen == FIGHT) {
            pause();
        } else if (screen == PAUSED) {
            screen = FIGHT;
        } else if (screen == VERSUS || screen == RESULT || screen == LOBBY) {
            toSelect();
        } else {
            return false;
        }
        return true;
    }

    /** Lets go of the other device, if there is one. */
    private void unlink() {
        if (link != null) link.close();
        if (nearby != null) nearby.stop();
        link = null;
        nearby = null;
        greeted = false;
    }

    private void toSelect() {
        audio.hush();
        unlink();
        mode = SOLO;
        arrange();
        screen = SELECT;
        fight = null;
        keepOnly(player, -1);
        sheet(player);
        session.refresh();
    }

    private void buzz(boolean strongly) {
        if (haptics) stage.performHapticFeedback(strongly ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants.KEYBOARD_TAP);
    }

    // Who says what

    /** A line as it is shown: Halgakos calls the user what they asked to be called. */
    private String shown(int fighter, String[] line) { return fighter == Fight.HALGAKOS ? names.named(line[1]) : line[1]; }

    /** Has a fighter say one of her lines. Halgakos's recordings call the user Sloppy, so they are kept for when she does. */
    private void say(int fighter, int which) {
        String[] line = FightCast.line(fighter, which);
        if (fighter != Fight.HALGAKOS || Chatter.DEFAULT_NAME.equals(names.nickname())) audio.say(line[0]);
    }

    /** The same in the middle of a fight, with her words over her head. */
    private void call(int side, int which) {
        int fighter = fight.fighters[side].kind;
        captions[side] = shown(fighter, FightCast.line(fighter, which));
        captionLeft[side] = CAPTION;
        say(fighter, which);
    }

    // From one screen to the next

    private void pick(int fighter) {
        player = fighter;
        session.keep("fight_last", fighter);
        keepOnly(fighter, -1);
        sheet(fighter);
        audio.play("pick", .6f);
        say(fighter, FightCast.PICK);
        buzz(false);
    }

    /** She meets the other four in the order they stand in, starting with the one after her. */
    private void climb() {
        for (int index = 0; index < ladder.length; index++) ladder[index] = (player + 1 + index) % Fight.FIGHTERS;
        rung = 0;
        versus();
    }

    private void versus() {
        screen = VERSUS;
        spoken = 0;
        int which = random.nextBoolean() ? FightCast.INTRO : FightCast.INTRO_TOO;
        opening = FightCast.line(player, which);
        keepOnly(player, ladder[rung]);
        backdrop(rung % STAGES.length);
        say(player, which);
    }

    /** Opens a game for another device to join, and waits for it. */
    private void host() {
        unlink();
        mode = HOST;
        link = FightLink.host(FightLink.PORT);
        nearby = new FightNearby(context);
        nearby.announce(FightLink.PORT);
        screen = LOBBY;
        pills("Cancel", TO_SELECT);
    }

    /** Looks for a game another device has opened on this Wi-Fi, to join it. */
    private void join() {
        unlink();
        mode = GUEST;
        // In a build made for testing, a number kept under this name joins a game through the machine the emulator runs on.
        if (BuildConfig.DEBUG && session.tally("fight_direct") > 0) {
            link = FightLink.join("10.0.2.2", FightLink.PORT);
        } else {
            nearby = new FightNearby(context);
            nearby.look();
        }
        screen = LOBBY;
        pills("Cancel", TO_SELECT);
    }

    /** The two devices meeting: each tells the other who it fights as, and the one that hosts says when to begin. */
    private void lobby() {
        if (mode == GUEST && link == null && nearby != null && nearby.address() != null) link = FightLink.join(nearby.address(), nearby.port());
        if (link == null) return;
        if (link.closed()) {
            // It did not work: the one that joins goes on looking, the one that hosts gives up.
            String why = link.problem();
            if (mode == GUEST && nearby != null) {
                link = null;
                greeted = false;
                nearby.stop();
                nearby = new FightNearby(context);
                nearby.look();
            } else {
                toSelect();
                say("Could not connect" + (why == null ? "" : ": " + why));
            }
            return;
        }
        if (!link.connected()) return;
        if (mode == GUEST && !greeted) {
            greeted = true;
            link.send(FightLink.HELLO, FightLink.VERSION, player, Math.round(Math.min(Fight.WIDE, width / unit - 1.5f) * 10));
        }
        for (byte[] message = link.poll(); message != null; message = link.poll()) {
            if (message[0] == FightLink.HELLO && mode == HOST && message.length >= 4) {
                if (message[1] != FightLink.VERSION) {
                    toSelect();
                    say("The other device has a different version of the app");
                    return;
                }
                second = Math.max(0, Math.min(Fight.FIGHTERS - 1, message[2]));
                theirSpan = (message[3] & 0xFF) / 10f;
                twoStage = random.nextInt(STAGES.length);
                link.send(FightLink.START, player, second, twoStage);
                keepOnly(player, second);
                backdrop(twoStage);
                begin();
                return;
            } else if (message[0] == FightLink.START && mode == GUEST && message.length >= 4) {
                second = Math.max(0, Math.min(Fight.FIGHTERS - 1, message[1]));
                twoStage = Math.max(0, Math.min(STAGES.length - 1, message[3]));
                keepOnly(player, second);
                backdrop(twoStage);
                begin();
                return;
            } else if (message[0] == FightLink.BYE) {
                toSelect();
                say("The other player left");
                return;
            }
        }
    }

    /** A word across the top of the screen for a few seconds. */
    private void say(String words) {
        notice = words;
        noticeLeft = 4;
    }

    /**
     * What has come over the line during a fight on two devices, and what goes back. The
     * host hears what the other player holds down and sends how the fight stands; the one
     * that joined is sent the fight and shows it. Returns false if the other has gone.
     */
    private boolean net() {
        byte[] state = null;
        for (byte[] message = link.poll(); message != null; message = link.poll()) {
            if (message[0] == FightLink.PAD && mode == HOST && message.length >= 2) {
                // What the other player holds down, in two bytes, and how many times she has tapped the screen, counted round.
                int bits = (message[1] & 0xFF) | (message.length >= 3 ? (message[2] & 0xFF) << 8 : 0);
                theirPressed |= bits & ~theirHeld;
                theirHeld = bits;
                if (message.length >= 4) {
                    int count = message[3] & 0xFF;
                    if (fight.phase() == Fight.CLASHING) theirTaps += (count - theirTapSeen) & 0xFF;
                    theirTapSeen = count;
                }
            } else if (message[0] == FightLink.STATE && mode == GUEST) {
                state = message;
            } else if (message[0] == FightLink.START && mode == GUEST && message.length >= 4) {
                twoStage = Math.max(0, Math.min(STAGES.length - 1, message[3]));
                backdrop(twoStage);
                begin();
                state = null;
            } else if (message[0] == FightLink.AGAIN && mode == HOST && screen == RESULT) {
                again();
            } else if (message[0] == FightLink.BYE) {
                toSelect();
                say("The other player left");
                return false;
            }
        }
        if (link.closed()) {
            toSelect();
            say("The other player left");
            return false;
        }
        try {
            if (state != null) {
                fight.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(state, 1, state.length - 1)));
            } else if (mode == HOST) {
                packed.reset();
                fight.write(new java.io.DataOutputStream(packed));
                link.send(FightLink.STATE, packed.toByteArray());
            }
        } catch (java.io.IOException garbled) {
            toSelect();
            say("The two devices stopped understanding each other");
            return false;
        }
        return true;
    }

    /** Another fight between the same two: the host begins it and tells the other. */
    private void again() {
        twoStage = random.nextInt(STAGES.length);
        link.send(FightLink.START, player, second, twoStage);
        backdrop(twoStage);
        begin();
    }

    private void begin() {
        // One player meets the next on the ladder; two meet each other, the host's fighter on the left on both devices.
        if (mode == SOLO) fight = new Fight(player, ladder[rung], -1, Math.min(Fight.LEVELS - 1, rung), System.nanoTime());
        else if (mode == GUEST) fight = new Fight(second, player, -1, -1, 0);
        else fight = new Fight(player, second, -1, -1, System.nanoTime());
        arrange();
        // They may get no further apart than the narrower of the two screens shows.
        fight.setSpan(Math.min(width / unit - 1.5f, mode == HOST ? theirSpan : Fight.WIDE));
        preload(fight.fighters[0].kind);
        preload(fight.fighters[1].kind);
        tapCount = 0;
        tapsGiven = 0;
        theirTaps = 0;
        theirTapSeen = 0;
        sentTaps = -1;
        seenQuakes = 0;
        seenDodges = 0;
        seenClashes = 0;
        grandAge = 9;
        // In a build made for testing, a number kept under this name starts both of them with their meters full.
        if (BuildConfig.DEBUG && session.tally("fight_full") > 0) for (Fight.Fighter fighter : fight.fighters) fighter.meter = Fight.METER;
        java.util.Arrays.fill(held, false);
        java.util.Arrays.fill(pressed, false);
        pad2.clear();
        theirHeld = 0;
        theirPressed = 0;
        sentPad = -1;
        seenHits = 0;
        seenBlocks = 0;
        seenThrows = 0;
        seenFalls = 0;
        seenMoves = 0;
        seenRound = 0;
        seenPhase = Fight.FIGHTING;
        for (int side = 0; side < 2; side++) {
            seenState[side] = Fight.IDLE;
            seenLanded[side] = 0;
            seenCharged[side] = false;
            shownHealth[side] = 1;
            captionLeft[side] = 0;
        }
        sparks.clear();
        slices.clear();
        cameraX = Fight.WIDE / 2 - width / unit / 2;
        screen = FIGHT;
        audio.hush();
    }

    private void pause() {
        java.util.Arrays.fill(held, false);
        screen = PAUSED;
        // On two devices the fight goes on behind the card: there is no stopping the other player's clock.
        pills("Carry on", RESUME, mode == SOLO ? "Fighters" : "Leave", TO_SELECT);
    }

    private void pills(Object... labelled) {
        pillCount = labelled.length / 2;
        for (int index = 0; index < pillCount; index++) {
            pillLabels[index] = (String) labelled[index * 2];
            pillActions[index] = (Integer) labelled[index * 2 + 1];
        }
    }

    private void finish() {
        won = fight.winner() == (mode == GUEST ? 1 : 0);
        java.util.Arrays.fill(held, false);
        screen = RESULT;
        if (mode != SOLO) {
            // Between two players nothing is kept: whoever won says so, and they may go again.
            audio.play(won ? "win" : "lose");
            say(fight.fighters[fight.winner()].kind, FightCast.WIN);
            pills("Rematch", AGAIN, "Fighters", TO_SELECT);
            return;
        }
        session.keep(won ? "fight_won" : "fight_lost", session.tally(won ? "fight_won" : "fight_lost") + 1);
        if (won) {
            if (rung + 1 > session.tally("fight_best")) session.keep("fight_best", rung + 1);
            boolean last = rung + 1 >= ladder.length;
            if (last) session.keep("fight_cleared_" + player, session.tally("fight_cleared_" + player) + 1);
            audio.play("win");
            say(player, FightCast.WIN);
            if (last) pills("Fighters", TO_SELECT, "Again", AGAIN);
            else pills("Next fight", NEXT, "Fighters", TO_SELECT);
        } else {
            audio.play("lose");
            say(player, FightCast.LOSE);
            pills("Try again", AGAIN, "Fighters", TO_SELECT);
        }
    }

    private void act(int action) {
        audio.play("tap", .5f);
        buzz(false);
        if (action == RESUME) {
            screen = FIGHT;
        } else if (action == AGAIN && mode == HOST) {
            again();
        } else if (action == AGAIN && mode == GUEST) {
            link.send(FightLink.AGAIN);
            say("Asked the other player for another");
        } else if (action == TO_SELECT) {
            toSelect();
        } else if (action == NEXT) {
            rung++;
            versus();
        } else if (won) {
            climb();
        } else {
            versus();
        }
    }

    // Time

    void advance(float seconds) {
        clock += seconds;
        if (noticeLeft > 0) noticeLeft -= seconds;
        if (screen == LOBBY) {
            lobby();
            return;
        }
        boolean linked = (mode == HOST || mode == GUEST) && fight != null && link != null;
        if (linked && !net()) return;
        if (screen != FIGHT && !(linked && screen == PAUSED)) return;
        for (int side = 0; side < 2; side++) if (captionLeft[side] > 0) captionLeft[side] -= seconds;
        if (bannerLeft > 0) bannerLeft -= seconds;
        if (shake > 0) shake -= seconds;
        for (int index = sparks.size() - 1; index >= 0; index--) if ((sparks.get(index)[2] += seconds) > .32f) sparks.remove(index);
        for (int index = slices.size() - 1; index >= 0; index--) if ((slices.get(index)[0] += seconds) > .5f) slices.remove(index);
        grandAge += seconds;
        sinceOver = fight.phase() == Fight.ROUND_OVER ? sinceOver + seconds : 0;
        // Where each of them was a moment ago, and a moment before that: noted every so often, so that what she leaves behind her trails well back.
        if ((trailAge += seconds) >= .045f) {
            trailAge = 0;
            for (int side = 0; side < 2; side++) {
                float[] was = trail[side];
                System.arraycopy(was, 0, was, 2, 4);
                was[0] = fight.fighters[side].x;
                was[1] = fight.fighters[side].y;
            }
        }
        // A round is announced before it starts.
        if (fight.round() != seenRound) {
            seenRound = fight.round();
            hold = HOLD;
            banner = "Round " + seenRound;
            bannerUnder = "First to " + Fight.ROUNDS;
            bannerLeft = HOLD;
            java.util.Arrays.fill(pressed, false);
            audio.play("bell", .6f);
        }
        if (mode == GUEST) {
            // What this player holds down goes to the device that runs the fight, whenever it changes and a few times a second besides.
            held(pad, held, pressed);
            int bits = pad.bits();
            if (bits != sentPad || tapCount != sentTaps || (sincePad += seconds) > .05f) {
                link.send(FightLink.PAD, bits & 0xFF, bits >> 8, tapCount & 0xFF);
                sentPad = bits;
                sentTaps = tapCount;
                sincePad = 0;
            }
            if (hold > 0) hold -= seconds;
            heard();
        } else if (hold > 0) {
            hold -= seconds;
        } else {
            held(pad, held, pressed);
            pad.taps = tapCount - tapsGiven;
            tapsGiven = tapCount;
            if (mode == SOLO) {
                fight.step(seconds, pad);
            } else {
                // The other side is played by hand too, on the other device.
                pad2.set(theirHeld | theirPressed);
                pad2.taps = theirTaps;
                theirPressed = 0;
                theirTaps = 0;
                fight.step(seconds, pad, pad2);
            }
            heard();
        }
        Fight.Fighter one = fight.fighters[0], other = fight.fighters[1];
        float across = width / unit;
        float goal = across >= Fight.WIDE ? (Fight.WIDE - across) / 2
                : Math.max(0, Math.min(Fight.WIDE - across, (one.x + other.x) / 2 - across / 2));
        cameraX += (goal - cameraX) * Math.min(1, seconds * 8);
        for (int side = 0; side < 2; side++) {
            float now = fight.fighters[side].health / Fight.fullHealth(fight.fighters[side].kind);
            shownHealth[side] = now > shownHealth[side] ? now : Math.max(now, shownHealth[side] - seconds * .45f);
        }
        if (fight.phase() == Fight.MATCH_OVER) finish();
    }

    /** What a player holds down: a button held, or pressed since the fight last moved on. */
    private void held(Fight.Pad into, boolean[] holding, boolean[] tapped) {
        into.left = holding[LEFT];
        into.right = holding[RIGHT];
        into.block = holding[GUARD];
        into.jump = holding[JUMP] || tapped[JUMP];
        into.light = holding[LIGHT] || tapped[LIGHT];
        into.heavy = holding[HEAVY] || tapped[HEAVY];
        into.special = holding[SPECIAL] || tapped[SPECIAL];
        into.dodge = holding[DODGE] || tapped[DODGE];
        into.ultimate = holding[ULT] || tapped[ULT];
        java.util.Arrays.fill(tapped, false);
    }

    /** What has just happened in the fight, made into sounds, flashes and words. */
    private void heard() {
        Fight.Fighter[] two = fight.fighters;
        if (fight.moves() != seenMoves) {
            seenMoves = fight.moves();
            for (int side = 0; side < 2; side++) {
                Fight.Fighter fighter = two[side];
                if (fighter.state != Fight.ATTACK || fighter.clock > .05f) continue;
                if (fighter.move == Fight.ULTIMATE || fighter.move == Fight.CLASH) {
                    audio.play("ultimate", .8f);
                    call(side, FightCast.ULTIMATE);
                    buzz(true);
                } else if (fighter.doing().shot == Fight.NOTHING) {
                    // The last blow of a chain, and the heavier ones, are swung harder.
                    boolean big = Fight.chainStep(fighter.move) == 5 || fighter.move == Fight.BREAK || fighter.move == Fight.DASH_HEAVY;
                    audio.play(big ? "whoosh" : "swing", big ? .6f : .4f);
                } else if (fighter.move == Fight.SPECIAL && captionLeft[side] <= 0 && !audio.speaking() && random.nextInt(3) == 0) {
                    call(side, FightCast.SPECIAL);
                }
            }
        }
        if (fight.throwsMade() != seenThrows) {
            seenThrows = fight.throwsMade();
            List<Fight.Shot> shots = fight.shots();
            int kind = shots.isEmpty() ? Fight.BREAD : shots.get(shots.size() - 1).kind;
            audio.play(kind == Fight.PANEL ? "scan" : kind == Fight.TEAR || kind == Fight.FLOOD ? "ink" : kind == Fight.HEART ? "heart"
                    : kind == Fight.ORB ? "magic" : "throw", .5f);
        }
        if (fight.hits() != seenHits) {
            seenHits = fight.hits();
            boolean hard = fight.hitHard();
            int by = two[1 - fight.hitSide()].kind;
            audio.play(hard ? by == Fight.TSUBAKIEH ? "bat" : by == Fight.CHIKUWA ? "bread" : "heavy" : "hit", hard ? .8f : .55f);
            if (hard) audio.play("heavy", .5f);
            sparks.add(new float[]{fight.hitX(), fight.hitY(), 0, hard ? 1 : 0});
            if (hard) shake = .22f;
            buzz(hard);
            // Now and then, a word from whoever was hit.
            int side = fight.hitSide();
            if (hard && captionLeft[side] <= 0 && !audio.speaking() && two[side].health > 0 && random.nextInt(2) == 0) {
                call(side, random.nextBoolean() ? FightCast.HIT : FightCast.HIT_TOO);
            }
        }
        if (fight.quakes() != seenQuakes) {
            // A smash has come down: the ground shakes, and what she brought down with her bursts where she landed.
            seenQuakes = fight.quakes();
            int who = 0;
            for (int side = 0; side < 2; side++) if (two[side].move == Fight.SMASH && Math.abs(two[side].x - fight.quakeX()) < .01f) who = side;
            audio.play("smash", .8f);
            sparks.add(new float[]{fight.quakeX(), 0, 0, 3, SMASH_FX[two[who].kind]});
            shake = .28f;
            buzz(true);
        }
        if (fight.dodges() != seenDodges) {
            seenDodges = fight.dodges();
            audio.play("dodge", .5f);
        }
        if (fight.clashes() != seenClashes) {
            seenClashes = fight.clashes();
            audio.play("clash", .8f);
            shake = .3f;
            buzz(true);
        }
        if (fight.blocks() != seenBlocks) {
            seenBlocks = fight.blocks();
            audio.play("block", .55f);
            sparks.add(new float[]{fight.hitX(), fight.hitY(), 0, 2});
        }
        if (fight.falls() != seenFalls) {
            seenFalls = fight.falls();
            audio.play("fall", .5f);
        }
        for (int side = 0; side < 2; side++) {
            Fight.Fighter fighter = two[side];
            if (fighter.state == Fight.JUMP && seenState[side] != Fight.JUMP) audio.play("jump", .35f);
            seenState[side] = fighter.state;
            if (fighter.charged() && !seenCharged[side] && side == (mode == GUEST ? 1 : 0)) {
                // Her ultimate's button comes on with a flourish.
                audio.play("meter", .7f);
                buzz(true);
                grandAge = 0;
            }
            seenCharged[side] = fighter.charged();
            // Each chunk of Halgakos's translation cuts where the other stands; each pulse of Illya's beam rings.
            boolean grand = fighter.state == Fight.ATTACK && (fighter.move == Fight.ULTIMATE || fighter.move == Fight.CLASH);
            if (grand && fighter.landed != seenLanded[side] && fighter.landed > 0) {
                if (fighter.kind == Fight.HALGAKOS) {
                    audio.play("slice", .6f);
                    slices.add(new float[]{0, fighter.landed, two[1 - side].x, two[1 - side].y, fighter.doing().times});
                } else if (fighter.kind == Fight.ILLYA) {
                    audio.play("heart", .5f);
                }
            }
            seenLanded[side] = grand ? fighter.landed : 0;
        }
        if (fight.phase() != seenPhase) {
            seenPhase = fight.phase();
            if (seenPhase == Fight.ROUND_OVER) {
                audio.play("ko", .8f);
                shake = .3f;
                buzz(true);
                int winner = fight.lastWinner();
                boolean time = two[0].health > 0 && two[1].health > 0;
                banner = time ? "Time" : winner < 0 ? "Both down" : "Knockout";
                bannerUnder = winner < 0 ? "Nobody takes the round" : FightCast.NAMES[two[winner].kind] + " takes the round";
                bannerLeft = Fight.BETWEEN - .3f;
            }
        }
    }

    // Touches

    void touch(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX(), y = event.getY();
        if (screen == FIGHT) {
            if (fight.phase() == Fight.CLASHING) {
                // In a clash every finger that comes down anywhere is a tap.
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                    tapCount++;
                    audio.play("tick", .35f);
                    buzz(false);
                }
                java.util.Arrays.fill(held, false);
                return;
            }
            if (action == MotionEvent.ACTION_DOWN && pauseButton.contains(x, y)) {
                audio.play("tap", .5f);
                pause();
                return;
            }
            holding(event);
            return;
        }
        if (action != MotionEvent.ACTION_DOWN) return;
        if (screen == SELECT) {
            if (closeButton.contains(x, y)) {
                audio.play("tap", .5f);
                stage.done();
            } else if (soundButton.contains(x, y)) {
                audio.set(!audio.on());
                names.set(CompanionPrefs.GAME_SOUND, audio.on());
                audio.play("tap", .5f);
            } else if (fightButton.contains(x, y)) {
                audio.play("tap", .5f);
                buzz(false);
                climb();
            } else if (hostButton.contains(x, y)) {
                audio.play("tap", .5f);
                buzz(false);
                host();
            } else if (joinButton.contains(x, y)) {
                audio.play("tap", .5f);
                buzz(false);
                join();
            } else {
                for (int index = 0; index < tiles.length; index++) {
                    if (tiles[index].contains(x, y)) {
                        pick(index);
                        return;
                    }
                }
            }
        } else if (screen == VERSUS) {
            // A touch turns to the other's answer, and then to the fight.
            if (++spoken == 1) {
                say(ladder[rung], FightCast.REPLY);
                audio.play("tap", .4f);
            } else {
                begin();
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

    /**
     * Which buttons are held, by however many fingers are down. A finger holds the button
     * it is nearest, if it is near one; a button newly held is also remembered as pressed,
     * so a tap that is over before the fight next moves on still counts. The ultimate's
     * button is only there to be touched while her meter is full.
     */
    private void holding(MotionEvent event) {
        int action = event.getActionMasked();
        boolean[] now = new boolean[BUTTONS];
        boolean charged = fight.fighters[mode == GUEST ? 1 : 0].charged();
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
            for (int finger = 0; finger < event.getPointerCount(); finger++) {
                if (action == MotionEvent.ACTION_POINTER_UP && finger == event.getActionIndex()) continue;
                float x = event.getX(finger), y = event.getY(finger), nearest = Float.MAX_VALUE;
                int which = -1;
                for (int index = 0; index < BUTTONS; index++) {
                    if (index == ULT && !charged) continue;
                    float away = (float) Math.hypot(x - buttons[index].centerX(), y - buttons[index].centerY());
                    if (away < nearest) {
                        nearest = away;
                        which = index;
                    }
                }
                if (which >= 0 && nearest < buttons[which].width() * .95f) now[which] = true;
            }
        }
        for (int index = 0; index < BUTTONS; index++) {
            if (now[index] && !held[index]) pressed[index] = true;
            held[index] = now[index];
        }
    }

    // Drawing

    void draw(Canvas canvas) {
        if (width == 0) return;
        if (screen == SELECT) {
            drawSelect(canvas);
        } else if (screen == LOBBY) {
            drawLobby(canvas);
        } else if (screen == VERSUS) {
            drawVersus(canvas);
        } else {
            drawFight(canvas);
            if (screen == PAUSED) {
                canvas.drawColor(0x99000000);
                card(canvas, mode == HOST || mode == GUEST ? "Leave the fight?" : "Paused",
                        FightCast.NAMES[fight.fighters[0].kind] + " against " + FightCast.NAMES[fight.fighters[1].kind], 0);
            } else if (screen == RESULT) {
                canvas.drawColor(0x99000000);
                drawResult(canvas);
            }
        }
        // A word across the top, when there is one to say.
        if (noticeLeft > 0 && notice != null) {
            text.setTextSize(dp(14));
            text.setTextAlign(Paint.Align.CENTER);
            float wide = Math.min(safe.width() - dp(32), text.measureText(notice) + dp(32));
            box.set(safe.centerX() - wide / 2, safe.top + dp(96), safe.centerX() + wide / 2, safe.top + dp(134));
            plate(canvas, box, dp(19), Math.round(240 * Math.min(1, noticeLeft / .4f)));
            text.setColor(ink);
            text.setAlpha(Math.round(255 * Math.min(1, noticeLeft / .4f)));
            if (text.measureText(notice) > wide - dp(24)) text.setTextSize(dp(14) * (wide - dp(24)) / text.measureText(notice));
            canvas.drawText(notice, safe.centerX(), safe.top + dp(120), text);
            text.setAlpha(255);
        }
    }

    /** Waiting for the other device: what is being waited for, and what the other player has to do. */
    private void drawLobby(Canvas canvas) {
        boolean landscape = width > height * 1.15f;
        drawBackdrop(canvas, 0, height * (landscape ? .86f : .62f), (float) Math.sin(clock * .2f) * .3f);
        canvas.drawColor(0x99000000);
        String dots = "...".substring(0, 1 + (int) (clock * 2) % 3);
        boolean joined = link != null && link.connected();
        String title = joined ? "Found each other" + dots : mode == HOST ? "Waiting for player two" + dots : "Looking for a game" + dots;
        text.setTextSize(dp(14));
        float wide = Math.min(safe.width() - dp(32), dp(320)) - dp(40);
        List<String> lines = wrap(mode == HOST ? "On the other device: open Page brawl, pick a fighter and tap Join. Both devices have to be on the same Wi-Fi."
                : "On the other device: open Page brawl, pick a fighter and tap Host. Both devices have to be on the same Wi-Fi.", wide);
        float high = lines.size() * dp(19) + dp(8);
        float top = card(canvas, title, "You fight as " + FightCast.NAMES[player], high);
        text.setTextSize(dp(14));
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(soft);
        float left = safe.centerX() - wide / 2;
        for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), left, top + dp(19) * (index + .8f), text);
    }

    /** A stage over the whole screen, the line its fighters stand on at a height asked for, slid a little by how far along the view is. */
    private void drawBackdrop(Canvas canvas, int which, float ground, float slide) {
        Bitmap picture = backdrop(which);
        // The floor of each picture is its lower third; fighters stand four fifths of the way down it.
        float stand = .8f, scale = Math.max(width * 1.12f / picture.getWidth(), ground / (picture.getHeight() * stand));
        float wide = picture.getWidth() * scale, tall = picture.getHeight() * scale;
        float left = (width - wide) / 2 - slide * (wide - width) / 2, top = ground - tall * stand;
        target.set(left, top, left + wide, top + tall);
        paint.setAlpha(255);
        canvas.drawBitmap(picture, null, target, paint);
        if (top + tall < height) {
            // On a tall screen the picture ends above the foot of it: the last of its floor is drawn out to reach.
            source.set(0, Math.round(picture.getHeight() * .9f), picture.getWidth(), picture.getHeight());
            target.set(left, top + tall - 1, left + wide, height);
            canvas.drawBitmap(picture, source, target, paint);
        }
    }

    /** One of a fighter's frames, her feet at a place, so many pixels to each pixel of the frame as it is kept. */
    private void frame(Canvas canvas, int fighter, int which, float feetX, float feetY, float scale, boolean leftward) {
        short[] at = FightFrames.FRAMES[fighter][which];
        Bitmap sheet = atlas(fighter, at[0]);
        float kept = sheet.getWidth() / (float) FightFrames.WIDE;
        source.set(Math.round(at[1] * kept), Math.round(at[2] * kept), Math.round((at[1] + at[3]) * kept), Math.round((at[2] + at[4]) * kept));
        target.set(feetX - at[5] * scale, feetY - at[6] * scale, feetX + (at[3] - at[5]) * scale, feetY + (at[4] - at[6]) * scale);
        canvas.save();
        // They were drawn facing right, and are turned about to face left.
        if (leftward) canvas.scale(-1, 1, feetX, feetY);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
    }

    /** One of the things that fly and flash, its middle at a place, as wide as asked, turned and mirrored as asked. */
    private void thing(Canvas canvas, int which, float x, float y, float wide, float degrees, boolean mirrored) {
        int side = fx.getHeight();
        float across = FX_WIDE[which], up = FX_TALL[which], tall = wide * up / across;
        source.set(Math.round(which * side + side * (1 - across) / 2), Math.round(side * (1 - up) / 2),
                Math.round(which * side + side * (1 + across) / 2), Math.round(side * (1 + up) / 2));
        target.set(-wide / 2, -tall / 2, wide / 2, tall / 2);
        canvas.save();
        canvas.translate(x, y);
        if (mirrored) canvas.scale(-1, 1);
        canvas.rotate(degrees);
        canvas.drawBitmap(fx, source, target, paint);
        canvas.restore();
    }

    /** A fighter's portrait in a ring of her colour. */
    private void face(Canvas canvas, int fighter, float x, float y, float size, boolean leftward, boolean ringed) {
        int side = faces.getHeight();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? 0xFF2A2E31 : 0xFFF6F1EC);
        canvas.drawCircle(x, y, size / 2, paint);
        paint.setColor(Color.BLACK);
        source.set(fighter * side, 0, fighter * side + side, side);
        target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        path.reset();
        path.addCircle(x, y, size / 2, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(path);
        if (leftward) canvas.scale(-1, 1, x, y);
        canvas.drawBitmap(faces, source, target, paint);
        canvas.restore();
        stroke.setColor(ringed ? FightCast.COLOURS[fighter] : 0x66FFFFFF);
        stroke.setStrokeWidth(size * (ringed ? .07f : .035f));
        canvas.drawCircle(x, y, size / 2, stroke);
    }

    private float across(float x) { return (x - cameraX) * unit; }

    private float up(float y) { return groundY - y * unit; }

    /** Which of her frames a fighter shows for what she is doing. */
    private int pose(Fight.Fighter fighter) { return FightPose.of(fight, fighter, hold, sinceOver); }

    private void drawFight(Canvas canvas) {
        Fight.Fighter[] two = fight.fighters;
        float jolt = shake > 0 ? (float) Math.sin(clock * 90) * dp(5) * Math.min(1, shake / .2f) : 0;
        float span = Math.max(.01f, Fight.WIDE - width / unit);
        drawBackdrop(canvas, mode == SOLO ? rung % STAGES.length : twoStage, groundY, width / unit >= Fight.WIDE ? 0 : (cameraX / span) * 2 - 1);
        canvas.save();
        canvas.translate(jolt, 0);
        float scale = Fight.TALL * unit / STANDING;
        // A soft shadow under each, smaller the higher she is.
        paint.setStyle(Paint.Style.FILL);
        for (Fight.Fighter fighter : two) {
            float wide = unit * (.75f - Math.min(.4f, fighter.y * .12f));
            paint.setColor(0x33000000);
            box.set(across(fighter.x) - wide, groundY - unit * .09f, across(fighter.x) + wide, groundY + unit * .09f);
            canvas.drawOval(box, paint);
        }
        paint.setColor(Color.BLACK);
        // Illya's grand beam, behind them both.
        for (Fight.Fighter fighter : two) {
            if (fighter.kind != Fight.ILLYA || fighter.state != Fight.ATTACK || fighter.move != Fight.ULTIMATE) continue;
            float since = fighter.clock - fighter.doing().startup;
            if (since < 0 || since > fighter.doing().every * 3 + .2f) continue;
            float way = fighter.facingLeft ? -1 : 1, from = across(fighter.x) + way * unit * .7f, y = up(1.75f);
            float thick = unit * (.42f + .1f * (float) Math.sin(clock * 40));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFF7EB6);
            paint.setAlpha(120);
            box.set(way > 0 ? from : -unit, y - thick, way > 0 ? width + unit : from, y + thick);
            canvas.drawRoundRect(box, thick, thick, paint);
            paint.setColor(Color.WHITE);
            paint.setAlpha(210);
            box.inset(0, thick * .55f);
            canvas.drawRoundRect(box, thick, thick, paint);
            paint.setAlpha(255);
            paint.setColor(Color.BLACK);
            for (int heart = 0; heart < 7; heart++) {
                float along = (clock * 9 + heart * 1.4f) % 9.8f;
                thing(canvas, HEART_FX, from + way * along * unit, y + (float) Math.sin(heart * 2.1f + clock * 6) * thick, unit * .5f, 0, false);
            }
        }
        // The two of them; whoever is in the middle of a move is drawn over the other.
        int front = two[0].state == Fight.ATTACK ? 0 : 1;
        for (int turn = 0; turn < 2; turn++) {
            Fight.Fighter fighter = two[turn == 0 ? 1 - front : front];
            float x = across(fighter.x), y = up(fighter.y);
            // She breathes as she stands, and leans into a blow as it goes out.
            float breath = fighter.state == Fight.IDLE ? 1 + .012f * (float) Math.sin(clock * 3 + fighter.side * 2) : 1;
            float lunge = fighter.state == Fight.ATTACK && fighter.doing().shot == Fight.NOTHING && fighter.clock >= fighter.doing().startup
                    && fighter.clock < fighter.doing().startup + .12f ? unit * .18f * (fighter.facingLeft ? -1 : 1) : 0;
            // A hop back lifts her off the ground a little; a dodge, a dash or a hop leaves her likeness behind her where she was.
            if (fighter.state == Fight.BACKSTEP) y -= unit * .3f * (float) Math.sin(Math.PI * Math.min(1, fighter.clock / Fight.BACKSTEP_SECONDS));
            if (fighter.state == Fight.DODGE || fighter.state == Fight.DASH || fighter.state == Fight.BACKSTEP) {
                float[] was = trail[fighter.side];
                for (int ghost = 2; ghost >= 1; ghost--) {
                    paint.setAlpha(ghost == 1 ? 110 : 55);
                    frame(canvas, fighter.kind, pose(fighter), across(was[ghost * 2]) - (x - across(fighter.x)), up(was[ghost * 2 + 1]), scale, fighter.facingLeft);
                }
            }
            // Getting up, she cannot be hit, and flickers to show it.
            paint.setAlpha(fighter.safe > 0 && fighter.state != Fight.ATTACK && (int) (clock * 16) % 2 == 0 ? 120 : 255);
            canvas.save();
            canvas.scale(1, breath, x, y);
            frame(canvas, fighter.kind, pose(fighter), x + lunge, y, scale, fighter.facingLeft);
            canvas.restore();
            paint.setAlpha(255);
        }
        // What is in the air.
        for (Fight.Shot shot : fight.shots()) {
            float x = across(shot.x), y = up(shot.y);
            boolean leftward = shot.across < 0;
            switch (shot.kind) {
                case Fight.PANEL:
                    // Glossarion's scan: a pane of light, and the line it reads along.
                    stroke.setColor(CYAN);
                    stroke.setAlpha(170);
                    stroke.setStrokeWidth(unit * .07f);
                    canvas.drawLine(x - (leftward ? -1 : 1) * unit * 1.1f, y, x, y, stroke);
                    stroke.setAlpha(255);
                    thing(canvas, PANEL_FX, x, y, unit * .72f, leftward ? -78 : 78, false);
                    break;
                case Fight.BREAD: thing(canvas, ROLL_FX, x, y, unit * .6f, shot.age * 500, leftward); break;
                case Fight.BAT: thing(canvas, BAT_FX, x, y, unit * 1.5f, shot.age * 900, false); break;
                case Fight.TEAR: thing(canvas, TEAR_FX, x, y, unit * .65f, (float) Math.toDegrees(Math.atan2(-shot.up, Math.abs(shot.across))), leftward); break;
                case Fight.KNIFE: thing(canvas, KNIFE_FX, x, y, unit * .95f, 0, leftward); break;
                case Fight.HEART: thing(canvas, HEART_FX, x, y, unit * (.55f + .06f * (float) Math.sin(clock * 30)), 0, false); break;
                case Fight.ORB: thing(canvas, ORB_FX, x, y, unit * (1.05f + .08f * (float) Math.sin(clock * 22)), clock * 120, false); break;
                default: {
                    // A flood of ink tears: a dark wave rolling along the floor, drops flying off its crest.
                    float way = leftward ? -1 : 1, tall = unit * 1.3f, wide = unit * 1.5f;
                    path.reset();
                    path.moveTo(x - way * wide, groundY);
                    path.quadTo(x - way * wide * .4f, groundY - tall * .5f, x, groundY - tall);
                    path.quadTo(x + way * wide * .8f, groundY - tall * 1.05f, x + way * wide * .7f, groundY - tall * .55f);
                    path.quadTo(x + way * wide * .3f, groundY - tall * .5f, x + way * wide * .55f, groundY);
                    path.close();
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFF1C2F8F);
                    paint.setAlpha(225);
                    canvas.drawPath(path, paint);
                    paint.setAlpha(255);
                    paint.setColor(Color.BLACK);
                    for (int drop = 0; drop < 4; drop++) {
                        float lift = (clock * 2.2f + drop * .27f) % 1;
                        thing(canvas, TEAR_FX, x + way * wide * (.1f + drop * .18f), groundY - tall * (1 + lift * .45f), unit * .34f, -90, false);
                    }
                }
            }
        }
        // Halgakos's translation: a cut of light for each chunk, and the panel that counts it.
        for (float[] slice : slices) {
            float fade = 1 - slice[0] / .5f, x = across(slice[2]), y = up(slice[3] + 1.2f), lean = (slice[1] - 2) * 34;
            stroke.setColor(Color.WHITE);
            stroke.setAlpha(Math.round(255 * fade));
            stroke.setStrokeWidth(unit * .1f);
            canvas.save();
            canvas.rotate(-28 + lean, x, y);
            canvas.drawLine(x - unit * 2.4f, y, x + unit * 2.4f, y, stroke);
            stroke.setColor(CYAN);
            stroke.setAlpha(Math.round(200 * fade));
            stroke.setStrokeWidth(unit * .26f);
            canvas.drawLine(x - unit * 2.2f * Math.min(1, slice[0] / .1f), y, x + unit * 2.2f * Math.min(1, slice[0] / .1f), y, stroke);
            canvas.restore();
            stroke.setAlpha(255);
            panel(canvas, x, y - unit * 1.9f, "[CHUNKING] " + (int) slice[1] + " / " + (int) slice[4], fade);
        }
        for (Fight.Fighter fighter : two) {
            if (fighter.kind != Fight.HALGAKOS || fighter.state != Fight.ATTACK || fighter.move != Fight.ULTIMATE) continue;
            float x = across(fighter.x), y = up(fighter.y + Fight.TALL);
            panel(canvas, x, y - unit * .9f, "[GLOSSARY] names stay names", 1);
            panel(canvas, x, y - unit * .35f, "[INDEX] honorifics retained: -nim", 1);
        }
        // Where blows land.
        for (float[] spark : sparks) {
            float fade = 1 - spark[2] / .32f, x = across(spark[0]), y = up(spark[1]);
            if (spark[3] == 3) {
                // Where a smash came down: a ring running out along the ground, and what burst there.
                float grow = spark[2] / .32f;
                stroke.setColor(Color.WHITE);
                stroke.setAlpha(Math.round(220 * fade));
                stroke.setStrokeWidth(unit * .1f);
                box.set(x - unit * (.6f + grow * 2.4f), groundY - unit * (.1f + grow * .3f), x + unit * (.6f + grow * 2.4f), groundY + unit * (.1f + grow * .3f));
                canvas.drawOval(box, stroke);
                stroke.setAlpha(255);
                paint.setAlpha(Math.round(255 * fade));
                for (int piece = -1; piece <= 1; piece++) {
                    thing(canvas, (int) spark[4], x + piece * unit * (.5f + grow), groundY - unit * (.5f + grow * (1.3f - Math.abs(piece) * .5f)), unit * .75f,
                            piece * 40 * grow, false);
                }
                paint.setAlpha(255);
            } else if (spark[3] == 2) {
                stroke.setColor(Color.WHITE);
                stroke.setAlpha(Math.round(230 * fade));
                stroke.setStrokeWidth(unit * .08f);
                canvas.drawCircle(x, y, unit * (.25f + spark[2] * 1.6f), stroke);
                stroke.setAlpha(255);
            } else {
                paint.setAlpha(Math.round(255 * fade));
                thing(canvas, BURST_FX, x, y, unit * (spark[3] == 1 ? 1.9f : 1.1f) * (.7f + spark[2] * 1.4f), spark[0] * 50, false);
                paint.setAlpha(255);
            }
        }
        // What they say, over their heads.
        for (int side = 0; side < 2; side++) {
            if (captionLeft[side] <= 0 || captions[side] == null) continue;
            bubble(canvas, captions[side], across(two[side].x), up(two[side].y + Fight.TALL) - dp(14), Math.min(1, captionLeft[side] / .3f));
        }
        canvas.restore();
        drawHud(canvas);
        // Blows landed one after another without letting her go: how many, and what they have taken off.
        for (int side = 0; side < 2; side++) {
            Fight.Fighter fighter = two[side];
            if (fighter.combo < 2 || fighter.comboAge > 1.1f) continue;
            float fade = Math.min(1, (1.1f - fighter.comboAge) / .25f), pop = 1 + .35f * Math.max(0, 1 - fighter.comboAge / .12f);
            float x = side == 0 ? safe.left + dp(18) : safe.right - dp(18), y = safe.top + dp(132);
            text.setTextAlign(side == 0 ? Paint.Align.LEFT : Paint.Align.RIGHT);
            text.setTextSize(dp(30) * pop);
            outlined(canvas, fighter.combo + " hits", x, y, GOLD, fade);
            text.setTextSize(dp(14));
            outlined(canvas, Math.round(fighter.comboDamage) + " off", x, y + dp(20), Color.WHITE, fade);
        }
        if (bannerLeft > 0 && banner != null) {
            float fade = Math.min(1, bannerLeft / .25f);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(dp(46));
            float y = Math.min(groundY - Fight.TALL * unit - dp(60), height * .36f);
            outlined(canvas, banner, safe.centerX(), y, Color.WHITE, fade);
            text.setTextSize(dp(17));
            outlined(canvas, bannerUnder, safe.centerX(), y + dp(28), Color.WHITE, fade);
        }
        if (screen == FIGHT) {
            // The buttons show her own fighter's special; on the device that joined, that is the one on the right.
            for (int index = 0; index < BUTTONS; index++) button(canvas, index, fight.fighters[mode == GUEST ? 1 : 0]);
            plate(canvas, pauseButton, pauseButton.height() / 2, 200);
            paint.setColor(ink);
            canvas.drawRoundRect(pauseButton.centerX() - dp(5.5f), pauseButton.centerY() - dp(6.5f), pauseButton.centerX() - dp(2), pauseButton.centerY() + dp(6.5f), dp(1.2f), dp(1.2f), paint);
            canvas.drawRoundRect(pauseButton.centerX() + dp(2), pauseButton.centerY() - dp(6.5f), pauseButton.centerX() + dp(5.5f), pauseButton.centerY() + dp(6.5f), dp(1.2f), dp(1.2f), paint);
            paint.setColor(Color.BLACK);
        }
        if (fight.phase() == Fight.FLOURISH) drawFlourish(canvas);
        else if (fight.phase() == Fight.CLASHING) drawClash(canvas);
    }

    /** One of Glossarion's panes of light, with what it reads. */
    private void panel(Canvas canvas, float x, float y, String words, float alpha) {
        text.setTextSize(dp(12));
        text.setTextAlign(Paint.Align.CENTER);
        float wide = text.measureText(words) + dp(18), tall = dp(22);
        box.set(x - wide / 2, y - tall / 2, x + wide / 2, y + tall / 2);
        box.offset(Math.max(0, dp(6) - box.left) - Math.max(0, box.right - (width - dp(6))), 0);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF0B3540);
        paint.setAlpha(Math.round(170 * alpha));
        canvas.drawRoundRect(box, dp(4), dp(4), paint);
        stroke.setColor(CYAN);
        stroke.setAlpha(Math.round(255 * alpha));
        stroke.setStrokeWidth(dp(1.3f));
        canvas.drawRoundRect(box, dp(4), dp(4), stroke);
        stroke.setAlpha(255);
        text.setColor(0xFFC8FAFF);
        text.setAlpha(Math.round(255 * alpha));
        canvas.drawText(words, box.centerX(), box.centerY() + dp(4.3f), text);
        text.setAlpha(255);
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
    }

    /** Words with a dark edge round them, to be read over anything. */
    private void outlined(Canvas canvas, String words, float x, float y, int color, float alpha) {
        text.setStyle(Paint.Style.STROKE);
        text.setStrokeWidth(text.getTextSize() * .16f);
        text.setStrokeJoin(Paint.Join.ROUND);
        text.setColor(0xFF2B2440);
        text.setAlpha(Math.round(220 * alpha));
        canvas.drawText(words, x, y, text);
        text.setStyle(Paint.Style.FILL);
        text.setColor(color);
        text.setAlpha(Math.round(255 * alpha));
        canvas.drawText(words, x, y, text);
        text.setAlpha(255);
    }

    /** What a fighter says, in a bubble over her head, kept on the screen. */
    private void bubble(Canvas canvas, String words, float x, float bottom, float alpha) {
        text.setTextSize(dp(13));
        text.setTextAlign(Paint.Align.LEFT);
        List<String> lines = wrap(words, Math.min(dp(210), width - dp(40)));
        float wide = 0;
        for (String one : lines) wide = Math.max(wide, text.measureText(one));
        float tall = lines.size() * dp(17) + dp(12);
        float left = Math.max(dp(8), Math.min(width - dp(8) - wide - dp(20), x - wide / 2 - dp(10)));
        float top = Math.max(safe.top + dp(96), bottom - tall);
        box.set(left, top, left + wide + dp(20), top + tall);
        plate(canvas, box, dp(12), Math.round(240 * alpha));
        text.setColor(ink);
        text.setAlpha(Math.round(255 * alpha));
        for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), box.left + dp(10), box.top + dp(6) + dp(17) * (index + .76f), text);
        text.setAlpha(255);
    }

    private void drawHud(Canvas canvas) {
        Fight.Fighter[] two = fight.fighters;
        float top = safe.top + dp(10), size = dp(40), middle = safe.centerX(), timer = dp(24);
        for (int side = 0; side < 2; side++) {
            Fight.Fighter fighter = two[side];
            int colour = FightCast.COLOURS[fighter.kind];
            float way = side == 0 ? 1 : -1, faceX = side == 0 ? safe.left + dp(10) + size / 2 : safe.right - dp(10) - size / 2;
            face(canvas, fighter.kind, faceX, top + size / 2, size, side == 1, true);
            // Her health empties toward her portrait; what she has just lost shows white for a moment.
            float inner = middle - way * (timer + dp(6)), outer = faceX + way * (size / 2 + dp(6));
            float left = Math.min(inner, outer), right = Math.max(inner, outer), barTop = top + dp(3), tall = dp(14);
            float full = right - left, now = fighter.health / Fight.fullHealth(fighter.kind);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x99201A2C);
            box.set(left, barTop, right, barTop + tall);
            canvas.drawRoundRect(box, tall / 2, tall / 2, paint);
            paint.setColor(Color.WHITE);
            box.set(side == 0 ? right - full * shownHealth[side] : left, barTop, side == 0 ? right : left + full * shownHealth[side], barTop + tall);
            canvas.drawRoundRect(box, tall / 2, tall / 2, paint);
            paint.setColor(now < .25f && (int) (clock * 5) % 2 == 0 ? 0xFFFF393F : colour);
            box.set(side == 0 ? right - full * now : left, barTop, side == 0 ? right : left + full * now, barTop + tall);
            canvas.drawRoundRect(box, tall / 2, tall / 2, paint);
            stroke.setColor(0xCCFFFFFF);
            stroke.setStrokeWidth(dp(1.3f));
            box.set(left, barTop, right, barTop + tall);
            canvas.drawRoundRect(box, tall / 2, tall / 2, stroke);
            // Her meter under it: gold, and pulsing, when her ultimate is ready.
            float meterTop = barTop + tall + dp(4), low = dp(6), charge = fighter.meter / Fight.METER;
            paint.setColor(0x99201A2C);
            box.set(left, meterTop, right, meterTop + low);
            canvas.drawRoundRect(box, low / 2, low / 2, paint);
            paint.setColor(fighter.charged() ? GOLD : CYAN);
            paint.setAlpha(fighter.charged() ? 180 + Math.round(75 * (float) Math.abs(Math.sin(clock * 6))) : 255);
            box.set(side == 0 ? right - full * charge : left, meterTop, side == 0 ? right : left + full * charge, meterTop + low);
            canvas.drawRoundRect(box, low / 2, low / 2, paint);
            paint.setAlpha(255);
            // Her name, and a pip for each round she has taken.
            text.setTextSize(dp(12.5f));
            text.setTextAlign(side == 0 ? Paint.Align.LEFT : Paint.Align.RIGHT);
            // With two playing, whose she is goes after her name.
            String name = FightCast.NAMES[fighter.kind] + (mode != SOLO && (mode == GUEST) == (side == 1) ? "  you" : "");
            outlined(canvas, name, side == 0 ? left + dp(2) : right - dp(2), meterTop + low + dp(15), Color.WHITE, 1);
            for (int pip = 0; pip < Fight.ROUNDS; pip++) {
                float x = inner - way * (dp(7) + pip * dp(14)), y = meterTop + low + dp(10);
                paint.setColor(pip < fighter.wins ? GOLD : 0x99201A2C);
                canvas.drawCircle(x, y, dp(4.5f), paint);
                stroke.setColor(0xCCFFFFFF);
                stroke.setStrokeWidth(dp(1.2f));
                canvas.drawCircle(x, y, dp(4.5f), stroke);
            }
        }
        paint.setColor(Color.BLACK);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(dp(24));
        outlined(canvas, String.valueOf(Math.max(0, (int) Math.ceil(fight.left()))), middle, top + dp(25), Color.WHITE, 1);
    }

    /**
     * One of the round buttons: see-through, with a soft edge, and filled in while it is
     * held. The ultimate's is not there at all until her meter is full; then it bursts in,
     * larger than it will be, with rays of gold turning behind it for as long as it waits.
     */
    private void button(Canvas canvas, int which, Fight.Fighter mine) {
        RectF where = buttons[which];
        boolean down = held[which], grand = which == ULT;
        if (grand && !mine.charged()) return;
        float x = where.centerX(), y = where.centerY(), round = where.width() / 2;
        if (grand) {
            float pop = Math.max(0, 1 - grandAge / .3f);
            round *= 1 + .7f * pop + .04f * (float) Math.sin(clock * 7);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(FightCast.COLOURS[mine.kind]);
            paint.setAlpha(Math.round(150 * pop));
            canvas.drawCircle(x, y, round * (1.2f + (1 - pop) * 1.6f), paint);
            stroke.setColor(GOLD);
            stroke.setAlpha(170);
            stroke.setStrokeWidth(dp(3));
            for (int ray = 0; ray < 8; ray++) {
                double angle = clock * 1.6 + ray * Math.PI / 4;
                canvas.drawLine(x + (float) Math.cos(angle) * round * 1.12f, y + (float) Math.sin(angle) * round * 1.12f,
                        x + (float) Math.cos(angle) * round * 1.38f, y + (float) Math.sin(angle) * round * 1.38f, stroke);
            }
            stroke.setAlpha(255);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x26000000);
        canvas.drawCircle(x, y + dp(2), round + dp(2), paint);
        paint.setColor(down ? CalculatorLayout.CORAL : grand ? GOLD : Color.WHITE);
        paint.setAlpha(down ? 235 : grand ? 235 : 135);
        canvas.drawCircle(x, y, round, paint);
        paint.setAlpha(255);
        stroke.setColor(Color.WHITE);
        stroke.setAlpha(down ? 255 : 215);
        stroke.setStrokeWidth(dp(1.6f));
        canvas.drawCircle(x, y, round - dp(.8f), stroke);
        stroke.setAlpha(255);
        int mark = down ? Color.WHITE : 0xFF3A3340;
        paint.setColor(mark);
        paint.setAlpha(down ? 255 : 215);
        float reach = round * .42f;
        if (which == LEFT || which == RIGHT || which == JUMP) {
            path.reset();
            path.moveTo(reach, 0);
            path.lineTo(-reach * .7f, -reach);
            path.lineTo(-reach * .7f, reach);
            path.close();
            canvas.save();
            canvas.translate(x, y);
            canvas.rotate(which == LEFT ? 180 : which == JUMP ? 270 : 0);
            canvas.drawPath(path, paint);
            canvas.restore();
        } else if (which == GUARD) {
            // A shield.
            path.reset();
            path.moveTo(x - reach, y - reach);
            path.lineTo(x + reach, y - reach);
            path.lineTo(x + reach, y + reach * .1f);
            path.quadTo(x + reach * .8f, y + reach * 1.05f, x, y + reach * 1.3f);
            path.quadTo(x - reach * .8f, y + reach * 1.05f, x - reach, y + reach * .1f);
            path.close();
            canvas.drawPath(path, paint);
        } else if (which == DODGE) {
            // Someone slipping aside: a figure leaning into it, with the air she leaves behind her. Fainter while she cannot dodge again yet.
            paint.setAlpha(mine.dodgeRest > 0 ? 80 : down ? 255 : 215);
            canvas.drawCircle(x + reach * .7f, y - reach * .85f, reach * .36f, paint);
            canvas.save();
            canvas.rotate(24, x + reach * .35f, y + reach * .2f);
            box.set(x + reach * .02f, y - reach * .5f, x + reach * .68f, y + reach * 1.15f);
            canvas.drawRoundRect(box, reach * .33f, reach * .33f, paint);
            canvas.restore();
            for (int line = 0; line < 3; line++) {
                float at = y - reach * .7f + line * reach * .7f, far = reach * (line == 1 ? 1.5f : 1.05f);
                box.set(x - reach * .45f - far, at - reach * .1f, x - reach * .45f, at + reach * .1f);
                canvas.drawRoundRect(box, reach * .1f, reach * .1f, paint);
            }
        } else if (which == LIGHT || which == HEAVY || grand) {
            // A burst: small for the quick blow, large and with more points for the heavy one, and the largest for the ultimate.
            int points = which == LIGHT ? 4 : grand ? 12 : 8;
            float outer = reach * (which == LIGHT ? 1.05f : grand ? 1.5f : 1.35f), inner = outer * (which == LIGHT ? .38f : grand ? .7f : .55f);
            path.reset();
            for (int point = 0; point < points * 2; point++) {
                double angle = Math.PI * point / points - Math.PI / 2 + (grand ? clock * .8 : 0);
                float far = point % 2 == 0 ? outer : inner;
                if (point == 0) path.moveTo(x + (float) Math.cos(angle) * far, y + (float) Math.sin(angle) * far);
                else path.lineTo(x + (float) Math.cos(angle) * far, y + (float) Math.sin(angle) * far);
            }
            path.close();
            if (grand) paint.setColor(down ? Color.WHITE : FightCast.COLOURS[mine.kind]);
            canvas.drawPath(path, paint);
            if (grand) {
                // Her face in the middle of it.
                face(canvas, mine.kind, x, y, round * 1.5f, false, false);
            }
        } else {
            // Her special: what she throws, fainter while it rests.
            paint.setAlpha(mine.rest[Fight.SPECIAL] > 0 ? 110 : 255);
            int picture = SPECIAL_FX[mine.kind];
            thing(canvas, picture, x, y, round * (picture == BAT_FX ? 1.5f : 1.05f), picture == BAT_FX ? -40 : 0, false);
        }
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
    }

    /**
     * Two ultimates at once: the fight stops, the two faces push against each other across
     * the screen, and whoever taps the screen faster pushes the other back.
     */
    private void drawClash(Canvas canvas) {
        Fight.Fighter[] two = fight.fighters;
        canvas.drawColor(0xCC120A22);
        int left = fight.taps(0), right = fight.taps(1), mine = mode == GUEST ? 1 : 0;
        // Where the two meet moves toward whoever is behind, gently at first: a tap or two does not throw it across.
        float share = (left + 4) / (float) (left + right + 8);
        float meet = safe.left + safe.width() * (.5f + .56f * (share - .5f)) + (float) Math.sin(clock * 60) * dp(3);
        float middle = safe.centerY() - dp(30), tall = dp(118), lean = dp(26);
        paint.setStyle(Paint.Style.FILL);
        for (int side = 0; side < 2; side++) {
            path.reset();
            path.moveTo(side == 0 ? 0 : width, middle - tall / 2);
            path.lineTo(meet + lean, middle - tall / 2);
            path.lineTo(meet - lean, middle + tall / 2);
            path.lineTo(side == 0 ? 0 : width, middle + tall / 2);
            path.close();
            paint.setColor(FightCast.COLOURS[two[side].kind]);
            paint.setAlpha(235);
            canvas.drawPath(path, paint);
        }
        paint.setAlpha(255);
        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(dp(5));
        canvas.drawLine(meet + lean, middle - tall / 2, meet - lean, middle + tall / 2, stroke);
        for (int spark = 0; spark < 6; spark++) {
            float along = (clock * 3 + spark * .37f) % 1, away = (spark % 2 == 0 ? 1 : -1) * dp(10 + 40 * along);
            thing(canvas, BURST_FX, meet + lean - lean * 2 * ((spark * .19f) % 1) + away, middle - tall / 2 + tall * ((spark * .19f) % 1), dp(34) * (1 - along * .6f), spark * 60, false);
        }
        float size = dp(104);
        face(canvas, two[0].kind, Math.max(safe.left + size * .6f, meet - lean - size * .62f), middle, size, false, true);
        face(canvas, two[1].kind, Math.min(safe.right - size * .6f, meet + lean + size * .62f), middle, size, true, true);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(dp(64) * (1 + .1f * (float) Math.sin(clock * 16)));
        outlined(canvas, "TAP!", safe.centerX(), middle - tall / 2 - dp(34), GOLD, 1);
        text.setTextSize(dp(15));
        outlined(canvas, "Tap anywhere, as fast as you can", safe.centerX(), middle - tall / 2 - dp(10), Color.WHITE, 1);
        text.setTextSize(dp(34));
        text.setTextAlign(Paint.Align.LEFT);
        outlined(canvas, String.valueOf(left), safe.left + dp(18), middle + tall / 2 + dp(44), mine == 0 ? GOLD : Color.WHITE, 1);
        text.setTextAlign(Paint.Align.RIGHT);
        outlined(canvas, String.valueOf(right), safe.right - dp(18), middle + tall / 2 + dp(44), mine == 1 ? GOLD : Color.WHITE, 1);
        // How much of it is left, as a bar running down.
        float part = Math.max(0, Math.min(1, fight.clashLeft() / Fight.CLASH_SECONDS)), bar = Math.min(safe.width() - dp(140), dp(260));
        paint.setColor(0x99201A2C);
        box.set(safe.centerX() - bar / 2, middle + tall / 2 + dp(24), safe.centerX() + bar / 2, middle + tall / 2 + dp(34));
        canvas.drawRoundRect(box, dp(5), dp(5), paint);
        paint.setColor(GOLD);
        box.set(safe.centerX() - bar / 2, middle + tall / 2 + dp(24), safe.centerX() - bar / 2 + bar * part, middle + tall / 2 + dp(34));
        canvas.drawRoundRect(box, dp(5), dp(5), paint);
        paint.setColor(Color.BLACK);
    }

    /** An ultimate announced: the fight dims, and a band in her colour crosses the screen with her face and the name of it. */
    private void drawFlourish(Canvas canvas) {
        int side = fight.flourisher();
        Fight.Fighter fighter = fight.fighters[side];
        float along = fight.flourish(), in = Math.min(1, along / .18f), out = Math.min(1, (1 - along) / .15f), show = Math.min(in, out);
        canvas.drawColor(Color.argb(Math.round(150 * show), 12, 8, 24));
        float tall = dp(132) * show, y = Math.min(height * .34f, groundY - Fight.TALL * unit * 1.25f), lean = dp(34);
        path.reset();
        path.moveTo(-dp(10), y - tall / 2 + lean / 2);
        path.lineTo(width + dp(10), y - tall / 2 - lean / 2);
        path.lineTo(width + dp(10), y + tall / 2 - lean / 2);
        path.lineTo(-dp(10), y + tall / 2 + lean / 2);
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(FightCast.COLOURS[fighter.kind]);
        paint.setAlpha(235);
        canvas.drawPath(path, paint);
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(dp(2.5f));
        canvas.drawLine(-dp(10), y - tall / 2 + lean / 2, width + dp(10), y - tall / 2 - lean / 2, stroke);
        canvas.drawLine(-dp(10), y + tall / 2 + lean / 2, width + dp(10), y + tall / 2 - lean / 2, stroke);
        if (show < .6f) return;
        // Her face comes in from her own side.
        float size = dp(118), slide = (1 - in) * width * .5f;
        float faceX = side == 0 ? safe.left + dp(20) + size / 2 - slide : safe.right - dp(20) - size / 2 + slide;
        face(canvas, fighter.kind, faceX, y, size, side == 1, false);
        float wordsX = side == 0 ? faceX + size / 2 + dp(14) : faceX - size / 2 - dp(14);
        text.setTextAlign(side == 0 ? Paint.Align.LEFT : Paint.Align.RIGHT);
        float room = (side == 0 ? width - wordsX : wordsX) - dp(12);
        text.setTextSize(dp(27));
        boolean greater = fighter.move == Fight.CLASH;
        String name = greater ? FightCast.GREATER[fighter.kind] : FightCast.MOVES[fighter.kind][Fight.ULTIMATE];
        if (text.measureText(name) > room) text.setTextSize(dp(27) * room / text.measureText(name));
        outlined(canvas, name, wordsX, y + dp(4), Color.WHITE, 1);
        text.setTextSize(dp(14));
        outlined(canvas, FightCast.NAMES[fighter.kind] + (greater ? " wins the clash" : "'s ultimate"), wordsX, y - dp(26), greater ? GOLD : Color.WHITE, 1);
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

    /** A card in the middle of the screen: a heading, a line under it, room for something, and its buttons. Returns where the room is. */
    private float card(Canvas canvas, String title, String under, float room) {
        float wide = Math.min(safe.width() - dp(32), dp(320)), pad = dp(20), button = dp(46), gap = dp(10);
        float tall = pad + dp(26) + dp(26) + room + dp(14) + button + pad;
        box.set(safe.centerX() - wide / 2, safe.centerY() - tall / 2, safe.centerX() + wide / 2, safe.centerY() + tall / 2);
        plate(canvas, box, dp(22), 255);
        float left = box.left, top = box.top, bottom = box.bottom;
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(21));
        canvas.drawText(title, left + pad, top + pad + dp(20), text);
        text.setColor(soft);
        text.setTextSize(dp(14.5f));
        canvas.drawText(under, left + pad, top + pad + dp(44), text);
        float each = (wide - pad * 2 - gap * (pillCount - 1)) / pillCount;
        for (int index = 0; index < pillCount; index++) {
            float x = left + pad + index * (each + gap);
            pills[index].set(x, bottom - pad - button, x + each, bottom - pad);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(index == 0 ? CalculatorLayout.CORAL : dark ? 0xFF44292E : 0xFFFCE9EB);
            canvas.drawRoundRect(pills[index], button / 2, button / 2, paint);
            text.setColor(index == 0 ? Color.WHITE : CalculatorLayout.CORAL);
            text.setTextSize(dp(15.5f));
            text.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(pillLabels[index], pills[index].centerX(), pills[index].centerY() + dp(5.5f), text);
        }
        paint.setColor(Color.BLACK);
        return top + pad + dp(52);
    }

    private void drawResult(Canvas canvas) {
        boolean last = rung + 1 >= ladder.length;
        String title = won ? last ? "Top of the ladder!" : FightCast.NAMES[player] + " wins" : FightCast.NAMES[ladder[rung]] + " wins";
        String under = won && last ? "All four beaten with " + FightCast.NAMES[player] : "Fight " + (rung + 1) + " of " + ladder.length;
        int player = this.player;
        boolean won = this.won;
        if (mode != SOLO) {
            // Between two players the card is the winner's, with what she says about it.
            int winner = fight.winner();
            player = fight.fighters[winner].kind;
            title = won ? "You win!" : "You lose";
            under = FightCast.NAMES[player] + " takes it, " + fight.fighters[winner].wins + " rounds to " + fight.fighters[1 - winner].wins;
            won = true;
        }
        // Her on the card, with what she says about it.
        text.setTextSize(dp(14));
        List<String> lines = wrap("“" + shown(player, FightCast.line(player, won ? FightCast.WIN : FightCast.LOSE)) + "”",
                Math.min(safe.width() - dp(32), dp(320)) - dp(40) - dp(84));
        float high = Math.max(dp(84), lines.size() * dp(19) + dp(10));
        float top = card(canvas, title, under, high);
        float left = safe.centerX() - Math.min(safe.width() - dp(32), dp(320)) / 2 + dp(20);
        face(canvas, player, left + dp(36), top + high / 2, dp(72), false, true);
        text.setTextSize(dp(14));
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        for (int index = 0; index < lines.size(); index++) {
            canvas.drawText(lines.get(index), left + dp(84), top + high / 2 - lines.size() * dp(19) / 2 + dp(19) * (index + .76f), text);
        }
    }

    /** The round button of a corner of the screen, with a cross or a speaker in it. */
    private void corner(Canvas canvas, RectF where, boolean speaker) {
        plate(canvas, where, where.height() / 2, 235);
        stroke.setColor(ink);
        stroke.setStrokeWidth(dp(2));
        float x = where.centerX(), y = where.centerY(), arm = dp(6);
        if (!speaker) {
            canvas.drawLine(x - arm, y - arm, x + arm, y + arm, stroke);
            canvas.drawLine(x + arm, y - arm, x - arm, y + arm, stroke);
            return;
        }
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
        paint.setColor(Color.BLACK);
        if (audio.on()) {
            box.set(x - dp(3), y - dp(6), x + dp(9), y + dp(6));
            canvas.drawArc(box, -50, 100, false, stroke);
        } else {
            canvas.drawLine(x + dp(4), y - dp(4.5f), x + dp(10), y + dp(4.5f), stroke);
            canvas.drawLine(x + dp(10), y - dp(4.5f), x + dp(4), y + dp(4.5f), stroke);
        }
    }

    private void drawSelect(Canvas canvas) {
        boolean landscape = width > height * 1.15f;
        drawBackdrop(canvas, 0, height * (landscape ? .86f : .62f), (float) Math.sin(clock * .2f) * .3f);
        canvas.drawColor(dark ? 0x8C000000 : 0x59FFFFFF);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(ink);
        text.setTextSize(dp(26));
        canvas.drawText(FightCast.GAME, safe.left + dp(18), safe.top + dp(40), text);
        int cleared = 0;
        for (int fighter = 0; fighter < Fight.FIGHTERS; fighter++) if (session.tally("fight_cleared_" + fighter) > 0) cleared++;
        text.setColor(soft);
        text.setTextSize(dp(13.5f));
        canvas.drawText("Won " + session.tally("fight_won") + "  ·  Ladder topped with " + cleared + " of " + Fight.FIGHTERS,
                safe.left + dp(18), safe.top + dp(58), text);
        corner(canvas, closeButton, false);
        corner(canvas, soundButton, true);
        for (int index = 0; index < tiles.length; index++) {
            RectF where = tiles[index];
            boolean chosen = index == player;
            face(canvas, index, where.centerX(), where.centerY(), where.width() * (chosen ? 1 : .86f), false, chosen);
            if (session.tally("fight_cleared_" + index) > 0) {
                // A gold mark on one the ladder has been topped with.
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(GOLD);
                canvas.drawCircle(where.right - dp(8), where.top + dp(8), dp(6), paint);
                paint.setColor(Color.BLACK);
            }
        }
        // What there is to know about the one chosen, on a card; and her, standing, in whatever room is left.
        float cardLeft = landscape ? safe.centerX() + dp(8) : safe.left + dp(16), cardRight = safe.right - dp(16), pad = dp(16);
        text.setTextSize(dp(13.5f));
        List<String> tip = wrap(FightCast.TIPS[player], cardRight - cardLeft - pad * 2);
        List<String> phrase = wrap("“" + shown(player, FightCast.line(player, FightCast.PICK)) + "”", cardRight - cardLeft - pad * 2);
        float cardBottom = (landscape ? fightButton.top : hostButton.top) - dp(12);
        // On a short screen what there is no room for is left off the card: first the tip, then what she says.
        float most = landscape ? cardBottom - safe.top - dp(66) : Float.MAX_VALUE, fixed = pad + dp(30) + dp(20) + dp(8) + 4 * dp(19) + dp(8) + dp(6) + pad;
        if (fixed + (tip.size() + phrase.size()) * dp(18) > most) tip = new ArrayList<>();
        if (fixed + (tip.size() + phrase.size()) * dp(18) > most) phrase = new ArrayList<>();
        float tall = fixed + (tip.size() + phrase.size()) * dp(18);
        float cardTop = cardBottom - tall;
        float bandTop = tiles[0].bottom + dp(8), bandBottom = landscape ? hostButton.top - dp(8) : cardTop - dp(4);
        float standing = Math.min(dp(300), bandBottom - bandTop - dp(6));
        if (standing > dp(70)) {
            float scale = standing / (STANDING * 1.12f), x = landscape ? safe.left + safe.width() * .25f : safe.centerX();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x33000000);
            box.set(x - standing * .28f, bandBottom - standing * .03f, x + standing * .28f, bandBottom + standing * .03f);
            canvas.drawOval(box, paint);
            paint.setColor(Color.BLACK);
            canvas.save();
            canvas.scale(1, 1 + .012f * (float) Math.sin(clock * 3), x, bandBottom);
            frame(canvas, player, FightFrames.IDLE1 + (int) (clock / .3f) % 4, x, bandBottom, scale, false);
            canvas.restore();
        }
        box.set(cardLeft, cardTop, cardRight, cardBottom);
        plate(canvas, box, dp(20), dark ? 240 : 236);
        float y = cardTop + pad;
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(FightCast.COLOURS[player]);
        text.setTextSize(dp(24));
        canvas.drawText(FightCast.NAMES[player], cardLeft + pad, y + dp(22), text);
        text.setTextAlign(Paint.Align.RIGHT);
        text.setColor(soft);
        text.setTextSize(dp(13));
        canvas.drawText(FightCast.STYLES[player], cardRight - pad, y + dp(20), text);
        text.setTextAlign(Paint.Align.LEFT);
        y += dp(30);
        text.setColor(ink);
        text.setTextSize(dp(13.5f));
        canvas.drawText(FightCast.WEAPONS[player], cardLeft + pad, y + dp(15), text);
        y += dp(28);
        String[] kinds = {"Quick", "Heavy", "Special", "Ultimate"};
        for (int move = 0; move < 4; move++) {
            text.setColor(soft);
            canvas.drawText(kinds[move], cardLeft + pad, y + dp(14), text);
            text.setColor(move == Fight.ULTIMATE ? CalculatorLayout.CORAL : ink);
            canvas.drawText(FightCast.MOVES[player][move], cardLeft + pad + dp(72), y + dp(14), text);
            y += dp(19);
        }
        y += dp(8);
        text.setColor(soft);
        for (String line : tip) {
            canvas.drawText(line, cardLeft + pad, y + dp(13), text);
            y += dp(18);
        }
        y += dp(6);
        text.setColor(ink);
        for (String line : phrase) {
            canvas.drawText(line, cardLeft + pad, y + dp(13), text);
            y += dp(18);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33000000);
        canvas.drawRoundRect(fightButton.left, fightButton.top + dp(2), fightButton.right, fightButton.bottom + dp(3), dp(27), dp(27), paint);
        paint.setColor(CalculatorLayout.CORAL);
        canvas.drawRoundRect(fightButton, dp(27), dp(27), paint);
        paint.setColor(Color.BLACK);
        text.setColor(Color.WHITE);
        text.setTextSize(dp(18));
        text.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Fight", fightButton.centerX(), fightButton.centerY() + dp(6.5f), text);
        // For two to play: a device each, over the same Wi-Fi.
        RectF[] ways = {hostButton, joinButton};
        String[] names = {"Host a game", "Join a game"};
        for (int index = 0; index < ways.length; index++) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(dark ? 0xFF44292E : 0xFFFCE9EB);
            canvas.drawRoundRect(ways[index], dp(20), dp(20), paint);
            paint.setColor(Color.BLACK);
            text.setColor(CalculatorLayout.CORAL);
            text.setTextSize(dp(13.5f));
            float room = ways[index].width() - dp(14), wide = text.measureText(names[index]);
            if (wide > room) text.setTextSize(dp(13.5f) * room / wide);
            canvas.drawText(names[index], ways[index].centerX(), ways[index].centerY() + dp(5), text);
        }
    }

    private void drawVersus(Canvas canvas) {
        int foe = ladder[rung];
        boolean landscape = width > height * 1.15f;
        drawBackdrop(canvas, rung % STAGES.length, height * (landscape ? .86f : .62f), 0);
        canvas.drawColor(0x66120A22);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(dp(15));
        outlined(canvas, "Fight " + (rung + 1) + " of " + ladder.length, safe.centerX(), safe.top + dp(40), Color.WHITE, 1);
        float size = Math.min(dp(150), Math.min(safe.width() * .36f, safe.height() * .3f)), y = safe.top + dp(70) + size / 2;
        float apart = Math.min(safe.width() * .27f, size * .5f + dp(60));
        face(canvas, player, safe.centerX() - apart, y, size, false, true);
        face(canvas, foe, safe.centerX() + apart, y, size, true, true);
        text.setTextSize(dp(30));
        outlined(canvas, "vs", safe.centerX(), y + dp(10), Color.WHITE, 1);
        text.setTextSize(dp(17));
        outlined(canvas, FightCast.NAMES[player], safe.centerX() - apart, y + size / 2 + dp(24), FightCast.COLOURS[player], 1);
        outlined(canvas, FightCast.NAMES[foe], safe.centerX() + apart, y + size / 2 + dp(24), FightCast.COLOURS[foe], 1);
        text.setTextSize(dp(12.5f));
        outlined(canvas, FightCast.STYLES[player], safe.centerX() - apart, y + size / 2 + dp(43), Color.WHITE, 1);
        outlined(canvas, FightCast.STYLES[foe], safe.centerX() + apart, y + size / 2 + dp(43), Color.WHITE, 1);
        // What is said: hers first, then the other's answer.
        int speaker = spoken == 0 ? player : foe;
        String words = spoken == 0 ? shown(player, opening) : FightCast.line(foe, FightCast.REPLY)[1];
        text.setTextSize(dp(17));
        float wide = Math.min(safe.width() - dp(32), dp(420)), pad = dp(20);
        List<String> lines = wrap(words, wide - pad * 2);
        float tall = pad + dp(20) + dp(10) + lines.size() * dp(24) + dp(28) + pad;
        float top = Math.min(safe.bottom - dp(28) - tall, Math.max(y + size / 2 + dp(64), safe.centerY() + dp(20)));
        box.set(safe.centerX() - wide / 2, top, safe.centerX() + wide / 2, top + tall);
        plate(canvas, box, dp(22), 250);
        float left = box.left, right = box.right, bottom = box.bottom;
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(FightCast.COLOURS[speaker]);
        text.setTextSize(dp(14));
        canvas.drawText(FightCast.NAMES[speaker], left + pad, top + pad + dp(12), text);
        text.setColor(ink);
        text.setTextSize(dp(17));
        for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), left + pad, top + pad + dp(30) + dp(24) * (index + .72f), text);
        text.setColor(soft);
        text.setTextSize(dp(12.5f));
        text.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(spoken == 0 ? "Tap to go on" : "Tap to fight", right - pad, bottom - pad + dp(4), text);
    }
}
