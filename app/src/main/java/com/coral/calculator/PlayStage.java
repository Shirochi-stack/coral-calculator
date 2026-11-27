package com.coral.calculator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    static final int SNATCH = 9, CRACK = 10, SCRIBBLE = 11, LIGHTS = 12, SPLAT = 13, PAWS = 14, AVALANCHE = 15, GIANT = 16,
            NOTES = 17, CONFETTI = 18, CURTAIN = 19, BITE = 20, CROWD = 21, BUG = 22;
    /** More pranks, numbered on from the games that come after the first of them in {@link PlaySession}. */
    static final int DRIP = 27, SNOW = 28, FLOOD = 29, WEBS = 30;
    /** And seven more, numbered on from Page brawl: stickers of herself, a breath on the glass, a disco, balloons, bubble gum, a fish tank, and a red dot to chase. */
    static final int STICKERS = 32, FOG = 33, DISCO = 34, BALLOONS = 35, GUM = 36, FISH = 37, LASER = 38;
    private static final int[] PRANKS = {SNATCH, CRACK, SCRIBBLE, LIGHTS, SPLAT, PAWS, AVALANCHE, GIANT, NOTES, CONFETTI,
            CURTAIN, BITE, CROWD, BUG, DRIP, SNOW, FLOOD, WEBS, STICKERS, FOG, DISCO, BALLOONS, GUM, FISH, LASER};
    private static final int[] BRIGHTS = {0xFFFF5F7E, 0xFFFFC53D, 0xFF4FC3F7, 0xFF7BD88F, 0xFFB388FF, 0xFFFF9A4D};
    /** What she writes on the notes she sticks to the screen, the paper they are on, and her confetti. */
    private static final String[] NOTES_SAY = {"Sloppy was here", "IOU one ice cream", "7 × 8 = 56", "Feed Halgakos", "Do your sums", "Property of Halgakos", "Tap me"};
    private static final int[] PAPER = {0xFFFFF3A0, 0xFFFFC9D6, 0xFFC9F2DD, 0xFFCFE4FF};
    private static final int[] CONFETTI_COLOURS = {0xFFFF393F, 0xFFFF8FB1, 0xFFFFD84D, 0xFF7BE0B0, 0xFF6FB6FF, 0xFFFFFFFF};
    private static final int PIECES = 110;
    /** How many apps she shakes loose, how many scoops she drops, and the colours they come in. */
    private static final int SHAKEN = 8, SCOOPS = 7;
    private static final int[] FLAVOURS = {0xFFF7A1B5, 0xFFFFF1C9, 0xFFB8EBD0, 0xFF9A6A54};

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
    /** How tall she stands where she keeps the user company: in a prank she is that tall here too, so she does not change size as it begins. */
    private float homeTall = Float.NaN;
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
    private int caught, missed;

    // Number rain: each sign is x, y, speed, and 1 for a plus or 0 for a minus.
    private final List<float[]> signs = new ArrayList<>();
    private float fieldLeft, fieldRight, wantedX, spawnIn, elapsed, hurt;
    private int score, hearts;

    // Bubble count: each bubble is x, y, x speed, y speed, its number, and how long it flashes.
    private final List<float[]> bubbles = new ArrayList<>();

    // Minesweeper: the field, the size of a square and where the field's corner is, and how the touch on it stands.
    private static final long HOLD_MS = 320;
    private static final int[] NUMBER_COLOURS = {0, 0xFF2F7DE1, 0xFF2E9E5B, 0xFFFF393F, 0xFF8257D8, 0xFFC0562F, 0xFF1E9AA6, 0xFF7A7F84, 0xFF7A7F84};
    private Mines field;
    private float square, fieldX, fieldY;
    /** A tap plants a flag, and does not open the square. */
    private boolean flagging;
    private final RectF flagButton = new RectF();
    private int pressed = Mines.NONE;
    private float pressX, pressY;
    private boolean planted, digging;
    /** How many flags have gone up on this field. */
    private int flagsPlanted;
    /** How much longer the field is left on show, mines and all, before the card comes up. */
    private float showing;
    private final Runnable plant = this::plant;

    // Pranks
    private int step;
    private float stepClock;
    /** How much longer she holds still at the start of a prank, saying her piece. */
    private float opening;
    private Drawable loot;
    private String lootName = "";
    private float lootX, lootY, lootHomeX, lootHomeY, lootScale, lootAlpha, binRise;
    private boolean binShut;
    private final Path marks = new Path();
    private float marksAlpha, flash;
    private final List<float[]> waypoints = new ArrayList<>();
    /** Apps picked from the launcher for a prank: their icons and their names. */
    private final List<Drawable> icons = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    /** Time since the prank began, and whether she has reached the spot she does it from. */
    private float clock;
    private boolean there;
    /** How dark she has made the screen, and how many times her own size she has grown. */
    private float darkness, grown = 1;
    /** Scoops of ice cream on the screen: x, y, size, flavour, when it landed, and what shapes it. */
    private final List<float[]> scoops = new ArrayList<>();
    /** Paw prints: x, y, and which way the paw was going. */
    private final List<float[]> prints = new ArrayList<>();
    private float sincePrint;
    private boolean leftPaw;
    /** Icons shaken loose: x, y, speed down, turn, turning speed, where it began, wait, and where it lands. */
    private final List<float[]> loose = new ArrayList<>();
    private final CompanionPrefs names;
    /** The full drawing of her, for when she is shown as large as the screen. */
    private Bitmap large;
    /** Notes stuck to the screen: x, y, tilt, paper, when it went up, and which thing it says. */
    private final List<float[]> notes = new ArrayList<>();
    /** Confetti: x, y, speed across, speed down, turn, turning speed, colour, size, and where it settles. */
    private final List<float[]> pieces = new ArrayList<>();
    /** How far the curtains are drawn, and how much of the corner she has bitten off. */
    private float drawn, bitten;
    private boolean biteLeft;
    /** Her sisters along the bottom of the screen: x, how far up each has come, when each comes up, and where in her wave each is. */
    private final List<float[]> crowd = new ArrayList<>();
    /** The bug she drew on the screen: where it is and which way it is heading. */
    private float bugX, bugY, bugTurn;
    /** The way she is looking, in place of any pose, or {@link PetArt#NOWHERE}. */
    private int gaze = PetArt.NOWHERE;
    /** Paint running down the screen: x, width, speed, when it starts, and how far it runs. */
    private final List<float[]> drips = new ArrayList<>();
    /** Snowflakes coming down, or bubbles going up: x, y, speed, size, and where in its sway each is. */
    private final List<float[]> specks = new ArrayList<>();
    private float sinceSpeck;
    /** What one of the later pranks puts on the screen, each thing a few numbers that the prank gives their meaning. */
    private final List<float[]> props = new ArrayList<>();
    /** The red dot she chases: where it is, and how long it has been still. */
    private float dotX, dotY, dotStill;
    private final Paint wipe = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Plus run is a game of its own, with its own lands, pictures and sounds: it takes the stage over whole. */
    private RunGame runGame;
    /** Page brawl likewise. */
    private FightGame fightGame;
    /** One of the tricks that playing unlocks: a scene of its own, played as a prank is. */
    private Trick trick;

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
        // The two games with a story tell of her as she is, in her own pink, whatever colour the companion wears.
        boolean story = game == PlaySession.RUN || game == PlaySession.FIGHT;
        int hair = pet == null ? Hair.PINK : Hair.chosen(context);
        if (story && hair != Hair.PINK) pet = android.graphics.BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_pet);
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
                : PlaySession.name(game) + ", on the whole screen");
        names = new CompanionPrefs(context);
        if (game == GIANT) {
            // Her frames are small: blown up to the size of the screen they would be a blur.
            large = pet != null ? Hair.art(context, R.drawable.halgakos_pet_large, hair)
                    : android.graphics.BitmapFactory.decodeResource(context.getResources(), R.drawable.halgakos_large);
        }
        if (game == PlaySession.RUN) runGame = new RunGame(this, context, dark, haptics, art, pet);
        if (game == PlaySession.FIGHT) fightGame = new FightGame(this, context, dark, haptics, art, pet);
        if (Unlocks.isTrick(game)) trick = new Trick(context, game - Unlocks.FIRST_TRICK, dark, art, pet);
        if (game == SNATCH || game == AVALANCHE) {
            pickApps(context, game == SNATCH ? 1 : SHAKEN);
            loot = icons.get(0);
            lootName = labels.get(0);
        }
    }

    /**
     * Picks apps at random from those the launcher shows, for a prank to seem to act on.
     * Only their icons and names are read, to be drawn here; nothing is done to the apps.
     */
    private void pickApps(Context context, int count) {
        android.content.pm.PackageManager packages = context.getPackageManager();
        List<android.content.pm.ResolveInfo> found = new ArrayList<>();
        try {
            found.addAll(packages.queryIntentActivities(
                    new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER), 0));
        } catch (RuntimeException unavailable) {
            // The calculator's own icon stands in below.
        }
        java.util.Collections.shuffle(found, random);
        for (android.content.pm.ResolveInfo app : found) {
            if (icons.size() >= count) break;
            try {
                Drawable icon = app.loadIcon(packages);
                CharSequence label = app.loadLabel(packages);
                if (icon == null || label == null || label.toString().trim().isEmpty()) continue;
                String name = label.toString().trim();
                icons.add(icon);
                labels.add(name.length() > 26 ? name.substring(0, 25).trim() + "…" : name);
            } catch (RuntimeException unreadable) {
                // An app that went while the list was being read.
            }
        }
        if (!icons.isEmpty()) return;
        try {
            icons.add(packages.getApplicationIcon(context.getPackageName()));
            labels.add(context.getString(R.string.app_name));
        } catch (RuntimeException | android.content.pm.PackageManager.NameNotFoundException missing) {
            icons.add(new android.graphics.drawable.ColorDrawable(CalculatorLayout.CORAL));
            labels.add(context.getString(R.string.app_name));
        }
    }

    static boolean isPrank(int game) {
        return (game >= SNATCH && game <= BUG) || (game >= DRIP && game <= WEBS) || (game >= STICKERS && game <= LASER) || Unlocks.isTrick(game);
    }

    /** The pranks she has yet to play this time round, in the order she will play them. */
    private static final List<Integer> NEXT_PRANKS = new ArrayList<>();

    /** A prank she has not played since she last got through them all. */
    static int anyPrank(Random random) { return anyPrank(random, null); }

    /** The same, with the tricks that have been unlocked among them. */
    static int anyPrank(Random random, List<Integer> unlocked) {
        if (NEXT_PRANKS.isEmpty()) {
            for (int prank : PRANKS) NEXT_PRANKS.add(prank);
            if (unlocked != null) NEXT_PRANKS.addAll(unlocked);
            java.util.Collections.shuffle(NEXT_PRANKS, random);
        }
        return NEXT_PRANKS.remove(NEXT_PRANKS.size() - 1);
    }

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
        if (trick != null) trick.setHome(x, feet, homeTall);
    }

    /** The same, with how tall she stands there. */
    void setHome(float x, float feet, float tall) {
        setHome(x, feet);
        homeTall = tall;
        if (trick != null) trick.setHome(x, feet, tall);
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        layout();
    }

    /** Fits everything to the room there is: at first, and again when the screen turns or folds. */
    private void layout() {
        play.set(insetLeft, insetTop, getWidth() - insetRight, getHeight() - insetBottom);
        if (play.width() <= 0 || play.height() <= 0) return;
        if (trick != null) {
            trick.layout(play);
            started = true;
            return;
        }
        if (fightGame != null) {
            fightGame.layout(getWidth(), getHeight(), play);
            started = true;
            return;
        }
        if (runGame != null) {
            // Plus run fits itself to the whole screen, and to what the system bars leave of it.
            runGame.layout(getWidth(), getHeight(), play);
            started = true;
            return;
        }
        // In a prank she is the size she was a moment ago; in a game, a size that suits the screen.
        herHeight = isPrank(game) && !Float.isNaN(homeTall) ? homeTall
                : Math.max(dp(84), Math.min(dp(136), Math.min(play.width(), play.height()) * .17f));
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
            fitField();
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
        } else if (game == SPLAT) {
            goalX = sideX();
            goalY = clamp(sideY() + herHeight * .5f, play.top + herHeight, play.bottom);
            marksAlpha = 1;
        } else if (game == PAWS) {
            // Back and forth across the screen, working her way down it.
            for (int i = 0; i < 5; i++) {
                float across = i % 2 == 0 ? .12f + random.nextFloat() * .2f : .68f + random.nextFloat() * .2f;
                waypoints.add(new float[]{play.left + play.width() * across,
                        play.top + herHeight + (play.height() - herHeight) * (i + .3f + random.nextFloat() * .4f) / 5});
            }
            marksAlpha = 1;
        } else if (game == AVALANCHE) {
            float size = dp(54);
            for (int i = 0; i < icons.size(); i++) {
                float x = play.left + size + random.nextFloat() * Math.max(1, play.width() - size * 2);
                float y = play.top + size + random.nextFloat() * Math.max(1, play.height() * .5f);
                loose.add(new float[]{x, y, 0, 0, (random.nextFloat() - .5f) * 260, y, i * .09f + random.nextFloat() * .12f,
                        play.bottom - size / 2 - random.nextFloat() * dp(10)});
            }
            marksAlpha = 0;
        } else if (game == GIANT) {
            goalX = sideX();
            goalY = play.bottom - dp(4);
        } else if (game == NOTES) {
            goalX = sideX();
            goalY = clamp(sideY() + herHeight * .5f, play.top + herHeight, play.bottom);
            marksAlpha = 1;
        } else if (game == CONFETTI) {
            marksAlpha = 1;
        } else if (game == BITE) {
            // The top corner on the other side of the screen from her.
            biteLeft = homeX > play.centerX();
            goalX = biteLeft ? play.left + herWidth * .5f + dp(64) : play.right - herWidth * .5f - dp(64);
            goalY = play.top + herHeight + dp(96);
        } else if (game == CROWD) {
            // As many of her as fit side by side along the bottom of the screen.
            float apart = herWidth * .86f;
            int count = Math.max(3, Math.min(9, (int) (play.width() / apart)));
            float first = play.centerX() - apart * (count - 1) / 2;
            for (int i = 0; i < count; i++) {
                crowd.add(new float[]{first + apart * i, 0, .12f * i + random.nextFloat() * .1f, random.nextInt(4)});
            }
        } else if (game == BUG) {
            // It comes in from the side of the screen away from her, and wanders.
            bugX = homeX > play.centerX() ? play.left - dp(20) : play.right + dp(20);
            bugY = play.top + play.height() * (.25f + random.nextFloat() * .3f);
            for (int i = 0; i < 6; i++) {
                waypoints.add(new float[]{play.left + dp(40) + random.nextFloat() * Math.max(1, play.width() - dp(80)),
                        play.top + dp(60) + random.nextFloat() * Math.max(1, play.height() - dp(160))});
            }
            marksAlpha = 1;
        } else if (game == DRIP) {
            float apart = dp(34);
            for (float x = play.left + apart / 2; x < play.right; x += apart) {
                drips.add(new float[]{x + (random.nextFloat() - .5f) * dp(12), dp(12) + random.nextFloat() * dp(11),
                        dp(130) + random.nextFloat() * dp(150), random.nextFloat() * .6f, play.height() * (.18f + random.nextFloat() * .55f)});
            }
            marksAlpha = 1;
        } else if (game == SNOW || game == FLOOD || game == WEBS) {
            marksAlpha = 1;
        } else if (game == STICKERS) {
            // Stickers of her all over the screen: x, y, tilt, size, and when each is stuck on.
            for (int i = 0; i < 14; i++) {
                props.add(new float[]{play.left + dp(40) + random.nextFloat() * Math.max(1, play.width() - dp(80)),
                        play.top + dp(60) + random.nextFloat() * Math.max(1, play.height() - dp(150)),
                        (random.nextFloat() - .5f) * 50, dp(64) + random.nextFloat() * dp(40), .2f + i * .13f});
            }
            marksAlpha = 1;
        } else if (game == BALLOONS) {
            // Balloons: x, how fast each rises, size, colour, where in its sway it is, when it is let go, and how high it stops.
            for (int i = 0; i < 16; i++) {
                props.add(new float[]{play.left + dp(30) + random.nextFloat() * Math.max(1, play.width() - dp(60)), dp(200) + random.nextFloat() * dp(160),
                        dp(30) + random.nextFloat() * dp(16), random.nextInt(BRIGHTS.length), random.nextFloat() * 6.28f, i * .14f,
                        play.top + dp(50) + random.nextFloat() * play.height() * .45f});
            }
            marksAlpha = 1;
        } else if (game == GUM) {
            // What is left of the bubble when it bursts: blobs of gum round the edges of the screen, x, y and size.
            for (int i = 0; i < 18; i++) {
                boolean side = i % 2 == 0;
                float along = random.nextFloat();
                props.add(new float[]{side ? (i % 4 == 0 ? play.left : play.right) : play.left + along * play.width(),
                        side ? play.top + along * play.height() : (i % 4 == 1 ? play.top : play.bottom), dp(26) + random.nextFloat() * dp(40)});
            }
            marksAlpha = 1;
        } else if (game == FISH) {
            // Fish: how far down each swims, how fast and which way, size, colour, and where in its wriggle it is.
            for (int i = 0; i < 9; i++) {
                props.add(new float[]{play.top + dp(70) + random.nextFloat() * Math.max(1, play.height() - dp(190)),
                        (random.nextBoolean() ? 1 : -1) * (dp(50) + random.nextFloat() * dp(80)), dp(26) + random.nextFloat() * dp(22),
                        random.nextInt(BRIGHTS.length), random.nextFloat() * 6.28f, random.nextFloat() * play.width()});
            }
            marksAlpha = 1;
        } else if (game == LASER) {
            // The places the red dot darts to, the last of them well out of her reach.
            for (int i = 0; i < 7; i++) {
                waypoints.add(new float[]{play.left + dp(50) + random.nextFloat() * Math.max(1, play.width() - dp(100)),
                        play.top + herHeight + dp(30) + random.nextFloat() * Math.max(1, play.height() - herHeight - dp(60))});
            }
            dotX = waypoints.get(0)[0];
            dotY = waypoints.get(0)[1];
            marksAlpha = 1;
        } else if (game == FOG || game == DISCO) {
            marksAlpha = 1;
        }
        if (isPrank(game)) {
            opening = 1.1f;
            say(Chatter.prank(random), 1.2f);
        }
    }

    // The clock

    /**
     * Going home, or to the list of recent apps, takes a game down with it. A game is drawn
     * over whatever is on screen, so without this it would stay there, over the home screen.
     * The system says when either is asked for; a prank, which ends by itself, is left to.
     */
    private final BroadcastReceiver homeward = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            wentHome = true;
            leave();
        }
    };
    private boolean listening, wentHome;
    /** Taken off the screen only to be put back on it later: the game on it is left as it stands, not closed. */
    private boolean kept;

    /** Whether the game was left by going home: then nothing of it is to be put back on the screen. */
    boolean wentHome() { return wentHome; }

    int game() { return game; }

    /**
     * Left by going home, one of the two long games stops where it stands, on its pause, so
     * that it can be come back to. False for anything else, and for a fight against another
     * device: those are done with.
     */
    boolean hold() {
        if (runGame != null) runGame.hold();
        else if (fightGame == null || !fightGame.hold()) return false;
        kept = true;
        return true;
    }

    /** A game that was held is about to be put back on the screen: it may be left again. */
    void comeBack() {
        kept = false;
        wentHome = false;
        left = false;
    }

    /** A game that was held is not coming back: it is closed, as it would have been when it left the screen. */
    void discard() {
        if (!kept) return;
        kept = false;
        if (runGame != null) runGame.close();
        if (fightGame != null) fightGame.close();
    }

    // Before Android 13 there is no flag to give: the system is the only sender of this, and the check for the version is above the call.
    @android.annotation.SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isPrank(game) && !listening) {
            IntentFilter away = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            try {
                if (Build.VERSION.SDK_INT >= 33) getContext().registerReceiver(homeward, away, Context.RECEIVER_EXPORTED);
                else getContext().registerReceiver(homeward, away);
                listening = true;
            } catch (RuntimeException refused) {
                // Then the game stays up until it is left by its own button or by Back.
            }
        }
        if (Build.VERSION.SDK_INT >= 33 && !isPrank(game)) {
            OnBackInvokedCallback callback = this::back;
            findOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
            backCallback = callback;
        }
        requestFocus();
        resume();
    }

    @Override protected void onDetachedFromWindow() {
        if (listening) {
            try {
                getContext().unregisterReceiver(homeward);
            } catch (RuntimeException gone) {
                // It was not registered after all.
            }
            listening = false;
        }
        running = false;
        removeCallbacks(frame);
        if (!kept) {
            if (runGame != null) runGame.close();
            if (fightGame != null) fightGame.close();
        }
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
        if (runGame != null) {
            runGame.advance(seconds);
            return;
        }
        if (fightGame != null) {
            fightGame.advance(seconds);
            return;
        }
        if (trick != null) {
            String said = trick.saying();
            if (trick.advance(seconds)) leave();
            // Read out as it changes: the view is a live region.
            String says = trick.saying();
            if (says != null && !says.equals(said)) setContentDescription(says);
            return;
        }
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
            else if (game == PlaySession.MINES) mines(seconds);
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
        words = names.named(words);
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
        target.set(herX - herWidth * grown / 2, herY - herHeight * grown, herX + herWidth * grown / 2, herY);
        paint.setAlpha(255);
        if (large != null && grown > 1) {
            // As she grows, her small frame gives way to the full drawing of her, which stays sharp.
            float shown = Math.min(1, (grown - 1) / .35f);
            if (shown < 1) drawFrame(canvas);
            float tall = herHeight * grown, wide = tall * large.getWidth() / large.getHeight();
            target.set(herX - wide / 2, herY - tall, herX + wide / 2, herY);
            paint.setAlpha(Math.round(255 * shown));
            canvas.drawBitmap(large, null, target, paint);
            paint.setAlpha(255);
            return;
        }
        drawFrame(canvas);
    }

    /** The frame of her that is showing, or the still drawing, where {@link #target} says. */
    private void drawFrame(Canvas canvas) {
        if (pet != null) {
            int row = once != NONE ? once : pose;
            int width = pet.getWidth() / PetArt.COLUMNS, height = pet.getHeight() / PetArt.FRAMES.length;
            int column = Math.min(frameIndex, PetArt.FRAMES[row] - 1);
            if (gaze != PetArt.NOWHERE && once == NONE) {
                row = PetArt.lookRow(gaze);
                column = PetArt.lookColumn(gaze);
            }
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
        float tall = lines.size() * line + pad * 1.5f;
        float bottom = herY - herHeight * grown - dp(10), top = bottom - tall;
        if (top < play.top + dp(6)) {
            // Under her instead, where there is no room over her head; and where she fills
            // the screen, at the top of it, across her.
            top = grown > 1.5f ? play.top + dp(14) : Math.min(herY + dp(10), play.bottom - tall - dp(6));
            bottom = top + tall;
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
            session.count("tag_played");
            session.add("tag_total", caught);
            session.add("tag_miss", missed);
            if (caught == 0) session.count("tag_none");
            if (caught >= 8 && missed <= 2) session.count("tag_sharp");
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
            say(1.1f, "Eek! Let go Sloppy~♡", "Tch. Lucky Sloppy~♡", "Hey! No fair Sloppy~♡", "Again Sloppy~♡", "You're fast Sloppy~♡", "Hmph. Fine Sloppy~♡");
            dash = .5f;
            rest = 0;
            pickGoal();
        } else {
            missed++;
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
                    // How many she had caught before the first bump is kept: a careful start.
                    if (hearts == HEARTS) session.keepBest("rain_dry", score, false);
                    hearts--;
                    hurt = .6f;
                    buzz();
                    ring(sign[0], sign[1], false);
                    say(1.2f, "Ow! That hurt Sloppy~♡", "Not that one Sloppy~♡", "Hey! Watch where you drag me Sloppy~♡", "Tch. That was your fault Sloppy~♡");
                    if (hearts <= 0) {
                        session.count("rain_played");
                        session.add("rain_total", score);
                        session.add("rain_bumps", HEARTS);
                        if (score <= 2) session.count("rain_quick");
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
                session.count("order_played");
                session.add("order_popped", BUBBLES);
                session.add("order_wrong", missed);
                if (missed == 0) session.count("order_clean");
                // Kept in tenths of a second, the lower the better.
                int tenths = Math.max(1, Math.round(elapsed * 10)), best = session.tally("order_best");
                if (tenths >= 400) session.count("order_slow");
                boolean record = session.keepBest("order_best", tenths, true);
                finish("All popped!", "That took " + seconds(tenths) + " seconds."
                        + (record && best != 0 ? " That's a new best!" : best != 0 ? " Your best is " + seconds(Math.min(best, tenths)) + "." : ""));
            }
        } else {
            missed++;
            elapsed += PENALTY_SECONDS;
            touched[5] = .45f;
            ring(x, y, false);
            say(1.2f, "Smaller first Sloppy~♡", "Can't you count Sloppy~♡", "Pfft~ not that one Sloppy~♡");
        }
    }

    static String seconds(int tenths) { return tenths / 10 + "." + tenths % 10; }

    /** A length of time in minutes and seconds, as a clock shows it. */
    static String clock(int seconds) { return seconds / 60 + ":" + (seconds % 60 < 10 ? "0" : "") + seconds % 60; }

    // Minesweeper

    /** Lays out a new field: as many squares as fit the screen at a size a finger can hit, between the bar on top and her below. */
    private void newField() {
        float room = play.height() - dp(64) - herHeight - dp(18), across = play.width() - dp(16);
        int columns = Math.max(6, Math.min(24, (int) (across / dp(40)))), rows = Math.max(6, Math.min(30, (int) (room / dp(40))));
        field = new Mines(columns, rows, Mines.fair(columns, rows));
        flagging = false;
        digging = false;
        showing = 0;
        pressed = Mines.NONE;
        removeCallbacks(plant);
        fitField();
    }

    /** Fits the field to the room there is: at first, and again when the screen turns or folds. */
    private void fitField() {
        if (field == null) return;
        float room = play.height() - dp(64) - herHeight - dp(18), across = play.width() - dp(16);
        square = Math.max(dp(14), Math.min(across / field.columns, room / field.rows));
        fieldX = play.centerX() - square * field.columns / 2;
        fieldY = play.top + dp(64) + Math.max(0, room - square * field.rows) / 2;
    }

    private void mines(float seconds) {
        // She waits under the field, on her own side of the screen.
        float x = clamp(homeX, play.left + herWidth / 2 + dp(8), play.right - herWidth / 2 - dp(8));
        if (runTo(x, play.bottom - dp(6), dp(760), seconds)) keep(PetArt.IDLE);
        if (digging && field.state() == Mines.PLAYING) elapsed += seconds;
        if (showing > 0 && (showing -= seconds) <= 0) {
            int time = Math.max(1, Math.round(elapsed));
            session.add("mines_flags", flagsPlanted);
            if (field.state() == Mines.WON) {
                session.count("mines_won");
                if (flagsPlanted == 0) session.count("mines_noflag");
                boolean record = session.keepBest("mines_best", time, true);
                finish("Field cleared!", "All " + field.mines + " mines found in " + clock(time) + (record ? ". A new best!" : "."));
            } else {
                session.count("mines_lost");
                if (time <= 5) session.count("mines_quick");
                finish("Boom!", "That was a mine. You lasted " + clock(time) + " among " + field.mines + " of them.");
            }
        }
    }

    private int squareAt(float x, float y) {
        if (field == null || x < fieldX || y < fieldY) return Mines.NONE;
        int column = (int) ((x - fieldX) / square), row = (int) ((y - fieldY) / square);
        return column < field.columns && row < field.rows ? row * field.columns + column : Mines.NONE;
    }

    private void minesTouch(int action, float x, float y) {
        if (field == null || showing > 0 || field.state() != Mines.PLAYING) return;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (flagButton.contains(x, y)) {
                    buzz();
                    flagging = !flagging;
                    say(flagging ? "Flags now. Careful where Sloppy~♡" : "Digging again. Brave Sloppy~♡", 1.2f);
                    return;
                }
                pressed = squareAt(x, y);
                pressX = x;
                pressY = y;
                planted = false;
                removeCallbacks(plant);
                if (pressed != Mines.NONE) postDelayed(plant, HOLD_MS);
                break;
            case MotionEvent.ACTION_MOVE:
                // A finger that wanders off was not a touch on that square.
                if (pressed != Mines.NONE && Math.hypot(x - pressX, y - pressY) > dp(14)) {
                    removeCallbacks(plant);
                    pressed = Mines.NONE;
                }
                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(plant);
                if (pressed != Mines.NONE && !planted) {
                    buzz();
                    if (flagging && !field.isOpen(pressed)) {
                        if (field.flag(pressed)) flagsPlanted++;
                    } else {
                        dig(pressed);
                    }
                }
                pressed = Mines.NONE;
                break;
            default:
                removeCallbacks(plant);
                pressed = Mines.NONE;
                break;
        }
    }

    /** A finger held on a square plants a flag there, or takes one off. */
    private void plant() {
        if (field == null || pressed == Mines.NONE || phase != PLAYING) return;
        planted = true;
        if (!field.flag(pressed)) return;
        flagsPlanted++;
        if (haptics) performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    private void dig(int where) {
        int opened = field.open(where, random);
        if (opened > 0) digging = true;
        if (field.state() == Mines.LOST) {
            if (haptics) performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            flash = .2f;
            showing = 1.8f;
            perform(PetArt.JUMP);
            say(1.8f, "Boom! Pfft~ that was a mine Sloppy~♡", "Kaboom. I saw that coming Sloppy~♡", "Boom. So careless Sloppy~♡");
        } else if (field.state() == Mines.WON) {
            showing = 1.4f;
            perform(PetArt.SAD);
            say(1.4f, "Not one mine?! Hmph Sloppy~♡", "Tch. You cleared it Sloppy~♡");
        } else if (opened >= 10) {
            say(1.2f, "Ooh. Lucky Sloppy~♡", "Ehh~ all that at once Sloppy~♡");
        }
    }

    // Starting and ending a game

    private void play() {
        phase = PLAYING;
        elapsed = 0;
        missed = 0;
        flagsPlanted = 0;
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
            say(1.4f, "Catch me if you can Sloppy~♡", "You'll never catch me Sloppy~♡", "Too slow already Sloppy~♡");
        } else if (game == PlaySession.MINES) {
            newField();
            say(1.6f, "Mind the mines Sloppy~♡", "One wrong tap and boom Sloppy~♡", "I know where they all are Sloppy~♡");
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
            say(1.6f, "Smallest first Sloppy~♡", "Count with me. If you can Sloppy~♡");
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

    /** The Back key or gesture: Plus run may only go back a screen of its own; anything else leaves. */
    private void back() {
        if (fightGame != null ? !fightGame.back() : runGame == null || !runGame.back()) leave();
    }

    /** The stage is to come down: for a game on it that is done with. */
    void done() { leave(); }

    private void leave() {
        if (left) return;
        left = true;
        host.onStageDone(game);
    }

    // Pranks: each is a short scene, and a touch at any point brings on its ending.

    private void prank(float seconds) {
        // First she says what she is about to do, more or less.
        if (opening > 0) {
            opening -= seconds;
            keep(PetArt.IDLE);
            return;
        }
        stepClock += seconds;
        clock += seconds;
        switch (game) {
            case SNATCH: snatch(seconds); break;
            case CRACK: crack(seconds); break;
            case SCRIBBLE: scribble(seconds); break;
            case LIGHTS: lights(seconds); break;
            case SPLAT: splat(seconds); break;
            case PAWS: paws(seconds); break;
            case AVALANCHE: avalanche(seconds); break;
            case NOTES: notes(seconds); break;
            case CONFETTI: confetti(seconds); break;
            case CURTAIN: curtain(seconds); break;
            case BITE: bite(seconds); break;
            case CROWD: crowd(seconds); break;
            case BUG: bug(seconds); break;
            case DRIP: show(seconds, 1.5f, "Oops~ I spilled paint on your screen Sloppy~♡"); break;
            case SNOW: specks(seconds, true); show(seconds, 1.6f, "It's snowing in your phone Sloppy~♡"); break;
            case FLOOD: specks(seconds, false); show(seconds, 1.8f, "Oops~ I left the tap running Sloppy~♡"); break;
            case WEBS: show(seconds, 1.4f, "Nobody has dusted in here for ages Sloppy~♡"); break;
            case STICKERS: show(seconds, 2.2f, "I decorated your screen. With me Sloppy~♡"); break;
            case FOG: show(seconds, 2.6f, "I breathed on your screen Sloppy~♡"); break;
            case DISCO: show(seconds, 1.6f, "Party time! Dance Sloppy~♡"); break;
            case BALLOONS: show(seconds, 2.4f, "I brought balloons. All mine Sloppy~♡"); break;
            case GUM: show(seconds, 2.3f, "Oops~ my bubble gum popped Sloppy~♡"); break;
            case FISH: specks(seconds, false); show(seconds, 1.8f, "Your phone is a fish tank now Sloppy~♡"); break;
            case LASER: laser(seconds); break;
            default: giant(seconds); break;
        }
    }

    /** She is part cat: a red dot darts about the screen, and she cannot help running after it. She never catches it. */
    private void laser(float seconds) {
        switch (step) {
            case 0: {
                if (waypoints.isEmpty() || clock > 9) {
                    keep(PetArt.IDLE);
                    perform(PetArt.JUMP);
                    say("Whose red dot is that?! Sloppy~♡", 2.6f);
                    next();
                    break;
                }
                float[] spot = waypoints.get(0);
                // The dot waits for her to come close, then darts off to the next place.
                dotX += (spot[0] - dotX) * Math.min(1, seconds * 14);
                dotY += (spot[1] - dotY) * Math.min(1, seconds * 14);
                boolean close = Math.hypot(herX - dotX, herY - herHeight * .3f - dotY) < dp(46);
                if (close || (dotStill += seconds) > 1.6f) {
                    dotStill = 0;
                    waypoints.remove(0);
                    if (close) buzz();
                }
                runTo(dotX, dotY + herHeight * .3f, dp(470), seconds);
                break;
            }
            case 1:
                if (stepClock > 2.6f) reveal();
                break;
            case 2:
                marksAlpha = Math.max(0, 1 - stepClock / .5f);
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** A prank that only has to be seen: it comes on, she says what she has done, and it goes when she owns up. */
    private void show(float seconds, float comesOn, String words) {
        switch (step) {
            case 0:
                if (stepClock < seconds * 1.5f) perform(PetArt.JUMP);
                if (stepClock > comesOn) {
                    buzz();
                    say(words, 2.6f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.8f) reveal();
                break;
            case 2:
                marksAlpha = Math.max(0, Math.min(marksAlpha, 1 - stepClock / .6f));
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** Snowflakes drift down from the top, or bubbles rise from the bottom, a few at a time. */
    private void specks(float seconds, boolean falling) {
        sinceSpeck += seconds;
        while (step < 2 && sinceSpeck > .035f && specks.size() < 160) {
            sinceSpeck -= .035f;
            specks.add(new float[]{play.left + random.nextFloat() * play.width(), falling ? play.top - dp(8) : play.bottom + dp(8),
                    dp(70) + random.nextFloat() * dp(90), dp(2.5f) + random.nextFloat() * dp(4), random.nextFloat() * 6.28f});
        }
        for (int index = specks.size() - 1; index >= 0; index--) {
            float[] speck = specks.get(index);
            speck[1] += (falling ? 1 : -1) * speck[2] * seconds;
            speck[4] += seconds * 2.2f;
            if (speck[1] > play.bottom + dp(12) || speck[1] < play.top - dp(12)) specks.remove(index);
        }
    }

    /** She brings her sisters: a row of her comes up along the bottom of the screen, every one waving. */
    private void crowd(float seconds) {
        switch (step) {
            case 0:
                keep(PetArt.WAVE);
                boolean up = true;
                for (float[] sister : crowd) {
                    sister[1] = clamp((stepClock - sister[2]) / .35f, 0, 1);
                    up &= sister[1] >= 1;
                }
                if (up) {
                    buzz();
                    say("Say hi to my sisters Sloppy~♡", 2.8f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.8f) reveal();
                break;
            case 2:
                for (float[] sister : crowd) sister[1] = Math.max(0, Math.min(sister[1], 1 - stepClock / .5f));
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** She draws a bug on the screen, and watches it crawl about as if it had nothing to do with her. */
    private void bug(float seconds) {
        switch (step) {
            case 0:
            case 1:
                if (!waypoints.isEmpty()) {
                    float[] to = waypoints.get(0);
                    float across = to[0] - bugX, down = to[1] - bugY;
                    float distance = (float) Math.hypot(across, down), reach = dp(210) * seconds;
                    if (distance <= reach) {
                        waypoints.remove(0);
                    } else {
                        bugX += across / distance * reach;
                        bugY += down / distance * reach;
                        bugTurn = (float) Math.toDegrees(Math.atan2(across, -down));
                    }
                }
                // Her eyes follow it wherever it goes.
                gaze = PetArt.lookAt(bugX - herX, bugY - (herY - herHeight * .7f));
                if (step == 0 && stepClock > .9f) {
                    buzz();
                    say("Eek! There's a bug on your screen Sloppy~♡", 2.6f);
                    next();
                } else if (step == 1 && (stepClock > 3.6f || waypoints.isEmpty())) {
                    reveal();
                }
                break;
            case 2:
                marksAlpha = Math.max(0, Math.min(marksAlpha, 1 - stepClock / .5f));
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** Runs her to the spot she does the prank from; true once she is there. */
    private boolean arrive(float speed, float seconds) {
        if (there) return true;
        there = runTo(goalX, goalY, speed, seconds);
        if (there) {
            stepClock = 0;
            keep(PetArt.IDLE);
        }
        return there;
    }

    /** She turns the screen off, or so it looks: it goes dark around her. */
    private void lights(float seconds) {
        switch (step) {
            case 0:
                if (stepClock < seconds * 1.5f) perform(PetArt.JUMP);
                darkness = Math.min(1, stepClock / .45f);
                if (darkness >= 1) {
                    buzz();
                    say("Oops~ I turned off your screen Sloppy~♡", 2.6f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.6f) reveal();
                break;
            case 2:
                darkness = Math.max(0, Math.min(darkness, 1 - stepClock / .6f));
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** She drops her ice cream on the screen, a scoop at a time. */
    private void splat(float seconds) {
        switch (step) {
            case 0:
                if (!arrive(dp(600), seconds)) break;
                if (scoops.isEmpty() && stepClock < seconds * 1.5f) perform(PetArt.JUMP);
                while (scoops.size() < SCOOPS && stepClock > scoops.size() * .17f) {
                    float size = dp(22 + random.nextFloat() * 24);
                    scoops.add(new float[]{play.left + size * 2 + random.nextFloat() * Math.max(1, play.width() - size * 4),
                            play.top + size * 2 + random.nextFloat() * Math.max(1, play.height() * .72f - size * 2),
                            size, random.nextInt(FLAVOURS.length), clock, random.nextInt(1 << 20)});
                    buzz();
                }
                if (scoops.size() >= SCOOPS && stepClock > SCOOPS * .17f + .6f) {
                    say("Oops~ I dropped my ice cream on your screen Sloppy~♡", 2.8f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.8f) reveal();
                break;
            case 2: // It slides off as she owns up.
                marksAlpha = Math.max(0, 1 - stepClock / .8f);
                for (float[] scoop : scoops) scoop[1] += dp(150) * seconds;
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** She walks all over the screen and leaves her paw prints on it. */
    private void paws(float seconds) {
        switch (step) {
            case 0: {
                if (waypoints.isEmpty()) {
                    keep(PetArt.IDLE);
                    perform(PetArt.JUMP);
                    say("I walked all over your screen Sloppy~♡", 2.6f);
                    next();
                    break;
                }
                float[] to = waypoints.get(0);
                float fromX = herX, fromY = herY;
                if (runTo(to[0], to[1], dp(430), seconds)) waypoints.remove(0);
                sincePrint += (float) Math.hypot(herX - fromX, herY - fromY);
                if (sincePrint >= dp(30)) {
                    sincePrint = 0;
                    leftPaw = !leftPaw;
                    double heading = Math.atan2(herY - fromY, herX - fromX), side = heading + (leftPaw ? -1 : 1) * Math.PI / 2;
                    prints.add(new float[]{herX + (float) Math.cos(side) * dp(9), herY - dp(4) + (float) Math.sin(side) * dp(9),
                            (float) Math.toDegrees(heading)});
                }
                break;
            }
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

    /** She shakes the screen, and apps come loose and fall to the bottom of it. */
    private void avalanche(float seconds) {
        switch (step) {
            case 0: {
                if (stepClock < seconds * 1.5f) perform(PetArt.JUMP);
                marksAlpha = Math.min(1, stepClock / .2f);
                boolean settled = true;
                for (float[] icon : loose) {
                    if (stepClock < icon[6]) {
                        settled = false;
                        continue;
                    }
                    if (icon[2] == 0 && icon[1] >= icon[7]) continue;
                    icon[2] += dp(1900) * seconds;
                    icon[1] += icon[2] * seconds;
                    icon[3] += icon[4] * seconds;
                    if (icon[1] >= icon[7]) {
                        // It lands, bounces once, and lies still.
                        icon[1] = icon[7];
                        icon[2] = icon[2] > dp(520) ? -icon[2] * .32f : 0;
                        icon[4] *= .4f;
                        if (icon[2] == 0) icon[4] = 0;
                    }
                    if (icon[2] != 0 || icon[1] < icon[7]) settled = false;
                }
                if (settled || stepClock > 3.2f) {
                    buzz();
                    say("Oops~ I shook your apps loose Sloppy~♡", 2.8f);
                    next();
                }
                break;
            }
            case 1:
                if (stepClock > 2.8f) reveal();
                break;
            case 2: { // They float back up to where they were, and are gone into the screen.
                float along = Math.min(1, stepClock / .7f), eased = 1 - (1 - along) * (1 - along);
                for (float[] icon : loose) {
                    icon[1] = icon[7] + (icon[5] - icon[7]) * eased;
                    icon[3] *= 1 - along;
                }
                marksAlpha = stepClock < .7f ? 1 : Math.max(0, 1 - (stepClock - .7f) / .4f);
                if (stepClock > 2.2f) next();
                break;
            }
            default:
                goHome(seconds);
                break;
        }
    }

    /** She sticks notes all over the screen. */
    private void notes(float seconds) {
        switch (step) {
            case 0:
                if (!arrive(dp(600), seconds)) break;
                if (notes.isEmpty() && stepClock < seconds * 1.5f) perform(PetArt.JUMP);
                while (notes.size() < NOTES_SAY.length && stepClock > notes.size() * .2f) {
                    float size = dp(92);
                    notes.add(new float[]{play.left + size * .7f + random.nextFloat() * Math.max(1, play.width() - size * 1.4f),
                            play.top + size * .9f + random.nextFloat() * Math.max(1, play.height() * .78f - size),
                            (random.nextFloat() - .5f) * 22, random.nextInt(PAPER.length), clock, notes.size()});
                    buzz();
                }
                if (notes.size() >= NOTES_SAY.length && stepClock > NOTES_SAY.length * .2f + .6f) {
                    say("Oops~ I left you a few notes Sloppy~♡", 2.8f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.8f) reveal();
                break;
            case 2: // They peel off and drop.
                marksAlpha = Math.max(0, 1 - stepClock / .8f);
                for (float[] note : notes) {
                    note[1] += dp(220) * seconds * (1 + note[5] * .12f);
                    note[2] += 60 * seconds;
                }
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** She throws a party all over the screen. */
    private void confetti(float seconds) {
        if (step <= 2) {
            // It falls, flutters and settles, whatever she is saying meanwhile.
            for (float[] piece : pieces) {
                if (piece[1] >= piece[8]) continue;
                piece[3] += dp(520) * seconds;
                piece[0] += (piece[2] + (float) Math.sin(clock * 5 + piece[6]) * dp(40)) * seconds;
                piece[1] = Math.min(piece[8], piece[1] + Math.min(piece[3], dp(430)) * seconds);
                piece[4] += piece[5] * seconds;
            }
        }
        switch (step) {
            case 0:
                if (stepClock < seconds * 1.5f) perform(PetArt.JUMP);
                while (pieces.size() < PIECES && stepClock > pieces.size() * .012f) {
                    float size = dp(7 + random.nextFloat() * 6);
                    pieces.add(new float[]{play.left + random.nextFloat() * play.width(), play.top - dp(20) - random.nextFloat() * dp(120),
                            (random.nextFloat() - .5f) * dp(120), dp(60 + random.nextFloat() * 160), random.nextFloat() * 360,
                            (random.nextFloat() - .5f) * 720, random.nextInt(CONFETTI_COLOURS.length), size,
                            play.bottom - size / 2 - random.nextFloat() * dp(26)});
                }
                if (pieces.size() >= PIECES && stepClock > 2.2f) {
                    buzz();
                    say("Surprise! I threw a party on your screen Sloppy~♡", 2.8f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.8f) reveal();
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

    /** She draws the curtains across the screen. */
    private void curtain(float seconds) {
        switch (step) {
            case 0:
                if (stepClock < seconds * 1.5f) perform(PetArt.JUMP);
                float along = Math.min(1, stepClock / .7f);
                drawn = 1 - (1 - along) * (1 - along);
                if (along >= 1) {
                    buzz();
                    say("Show's over Sloppy~♡", 2.6f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.6f) reveal();
                break;
            case 2:
                drawn = Math.max(0, Math.min(drawn, 1 - stepClock / .6f));
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** She takes a bite out of the corner of the screen. */
    private void bite(float seconds) {
        switch (step) {
            case 0:
                if (!arrive(dp(640), seconds)) break;
                facingLeft = biteLeft;
                if (stepClock < seconds * 1.5f) perform(PetArt.JUMP);
                if (stepClock > .25f) {
                    bitten = 1;
                    flash = .08f;
                    buzz();
                    say("Nom. Your screen tastes like pixels Sloppy~♡", 2.8f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.8f) reveal();
                break;
            case 2:
                bitten = Math.max(0, Math.min(bitten, 1 - stepClock / .6f));
                if (stepClock > 2.2f) next();
                break;
            default:
                goHome(seconds);
                break;
        }
    }

    /** She comes to the front of the screen and turns out to be enormous. */
    private void giant(float seconds) {
        float most = Math.max(1.5f, play.height() * .82f / herHeight);
        switch (step) {
            case 0:
                if (!arrive(dp(620), seconds)) break;
                float along = Math.min(1, stepClock / .45f);
                grown = 1 + (most - 1) * (1 - (1 - along) * (1 - along));
                if (along >= 1) {
                    buzz();
                    perform(PetArt.JUMP);
                    say("Boo! I'm taking over your screen Sloppy~♡", 2.4f);
                    next();
                }
                break;
            case 1:
                if (stepClock > 2.4f) reveal();
                break;
            case 2:
                grown = Math.max(1, grown - (most - 1) * seconds / .45f);
                if (stepClock > 2.2f) next();
                break;
            default:
                grown = 1;
                goHome(seconds);
                break;
        }
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
                    say("Oops~ I deleted " + lootName + " Sloppy~♡", 2.6f);
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
                    say("Oops~ I cracked your screen Sloppy~♡", 2.8f);
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
                    say("Look! I drew on your screen Sloppy~♡", 2.6f);
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
        opening = 0;
        gaze = PetArt.NOWHERE;
        perform(PetArt.JUMP);
        keep(PetArt.IDLE);
        if (game == SNATCH) {
            step = 5;
            binRise = 1;
            say("Just kidding! " + lootName + " is right where it was Sloppy~♡", 2.4f);
        } else {
            step = 2;
            there = true;
            say(game == CRACK ? "Just kidding! Not a scratch on it Sloppy~♡"
                    : game == SCRIBBLE ? "Just kidding! It wipes right off Sloppy~♡"
                    : game == LIGHTS ? "Just kidding! Lights back on Sloppy~♡"
                    : game == SPLAT ? "Just kidding! Not a drop on it Sloppy~♡"
                    : game == PAWS ? "Just kidding! Clean as ever Sloppy~♡"
                    : game == AVALANCHE ? "Just kidding! They never left Sloppy~♡"
                    : game == NOTES ? "Just kidding! All tidied away Sloppy~♡"
                    : game == CONFETTI ? "Just kidding! All swept up Sloppy~♡"
                    : game == CURTAIN ? "Just kidding! Encore Sloppy~♡"
                    : game == BITE ? "Just kidding! Still in one piece Sloppy~♡"
                    : game == CROWD ? "Just kidding! There's only one of me Sloppy~♡"
                    : game == BUG ? "Just kidding! I drew it myself Sloppy~♡"
                    : game == DRIP ? "Just kidding! Not a drop on it Sloppy~♡"
                    : game == SNOW ? "Just kidding! All melted Sloppy~♡"
                    : game == FLOOD ? "Just kidding! Dry as a bone Sloppy~♡"
                    : game == WEBS ? "Just kidding! Spick and span Sloppy~♡"
                    : game == STICKERS ? "Just kidding! They peel right off Sloppy~♡"
                    : game == FOG ? "Just kidding! Wiped clean Sloppy~♡"
                    : game == DISCO ? "Just kidding! Party's over Sloppy~♡"
                    : game == BALLOONS ? "Just kidding! They all floated off Sloppy~♡"
                    : game == GUM ? "Just kidding! Not a bit stuck Sloppy~♡"
                    : game == FISH ? "Just kidding! Not a fish in it Sloppy~♡"
                    : game == LASER ? "Just kidding! I wasn't chasing it Sloppy~♡"
                    : "Just kidding! Back to pocket size Sloppy~♡", 2.2f);
        }
        stepClock = 0;
    }

    private void prankTouch() {
        buzz();
        boolean owningUp = game == SNATCH ? step >= 5 : step >= 2;
        if (owningUp) leave(); else reveal();
    }

    // Touches and keys

    /** The mouse wheel, for a mouse: Plus run's bag turns its pages with it. */
    @Override public boolean onGenericMotionEvent(MotionEvent event) {
        if (runGame != null && event.getAction() == MotionEvent.ACTION_SCROLL
                && runGame.wheel(event.getAxisValue(MotionEvent.AXIS_VSCROLL))) return true;
        return super.onGenericMotionEvent(event);
    }

    /** Gone from the front: every key let go, so nothing stays held. */
    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (!focused && runGame != null) runGame.letGo();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (runGame != null) {
            runGame.touch(event);
            return true;
        }
        if (fightGame != null) {
            fightGame.touch(event);
            return true;
        }
        float x = event.getX(), y = event.getY();
        int action = event.getActionMasked();
        if (trick != null) {
            if (action == MotionEvent.ACTION_DOWN) {
                buzz();
                if (trick.touch()) leave();
            }
            return true;
        }
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
            if (game == PlaySession.MINES) {
                minesTouch(action, x, y);
            } else if (game == PlaySession.RAIN) {
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
        // Plus run's keys, for a keyboard (Esc is Back, below).
        if (runGame != null && event.getAction() != KeyEvent.ACTION_MULTIPLE
                && runGame.key(event.getKeyCode(), event.getAction() == KeyEvent.ACTION_DOWN)) return true;
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
            if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) back();
            return true;
        }
        // Page brawl is played on a keyboard too.
        if (fightGame != null && fightGame.key(event)) return true;
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
        if (runGame != null) {
            runGame.draw(canvas);
            return;
        }
        if (fightGame != null) {
            fightGame.draw(canvas);
            return;
        }
        if (trick != null) {
            trick.draw(canvas);
            return;
        }
        if (isPrank(game)) {
            drawPrank(canvas);
            return;
        }
        canvas.drawColor(SCRIM);
        // The signs fall behind her; the bubbles drift in front, so she never hides one.
        if (phase == PLAYING && game == PlaySession.RAIN) drawRain(canvas);
        if (game == PlaySession.MINES && field != null && phase != INTRO) drawField(canvas);
        if (flash > 0) canvas.drawColor(Color.argb(Math.round(150 * Math.min(1, flash / .12f)), 255, 255, 255));
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
            drawCard(canvas, PlaySession.name(game), game == PlaySession.MINES
                    ? "Tap a square to open it: its number is how many mines touch it. Hold a square to flag a mine, or tap the flag button to flag with a tap. Open every square that has no mine."
                    : game == PlaySession.TAG
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
                : game == PlaySession.MINES ? clock(Math.round(elapsed)) + "  ·  " + (field == null ? 0 : field.left()) + " mines"
                : game == PlaySession.RAIN ? "Caught " + score
                : seconds(Math.round(elapsed * 10)) + "s  ·  " + bubbles.size() + " to go";
        text.setTextSize(dp(15));
        text.setTextAlign(Paint.Align.LEFT);
        boolean hearted = game == PlaySession.RAIN;
        float width = text.measureText(status) + dp(28) + (hearted ? dp(22) * HEARTS + dp(6) : 0);
        box.set(play.left + margin, play.top + margin, play.left + margin + width, play.top + margin + size);
        plate(canvas, box, size / 2);
        text.setColor(ink);
        canvas.drawText(status, box.left + dp(14), box.centerY() + dp(5.5f), text);
        if (hearted) {
            // A dot for each bump she can still take.
            int left = hearts;
            paint.setStyle(Paint.Style.FILL);
            for (int heart = 0; heart < HEARTS; heart++) {
                paint.setColor(heart < left ? CalculatorLayout.CORAL : cardEdge);
                canvas.drawCircle(box.right - dp(20) - dp(22) * (HEARTS - 1 - heart), box.centerY(), dp(7), paint);
            }
        }
        if (game == PlaySession.MINES) {
            // The flag button: lit while a tap plants a flag.
            flagButton.set(box.right + dp(8), box.top, box.right + dp(8) + size, box.bottom);
            if (flagging) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(CalculatorLayout.CORAL);
                canvas.drawRoundRect(flagButton, size / 2, size / 2, paint);
            } else {
                plate(canvas, flagButton, size / 2);
            }
            drawFlag(canvas, flagButton.centerX(), flagButton.centerY(), size * .8f, flagging ? Color.WHITE : ink,
                    flagging ? Color.WHITE : CalculatorLayout.CORAL);
        }
    }

    /** The minefield: shut squares stand proud, open ones lie flat with their numbers, and at the end every mine shows. */
    private void drawField(Canvas canvas) {
        boolean over = field.state() != Mines.PLAYING;
        float gap = Math.max(1, square * .045f), round = square * .17f;
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(square * .56f);
        float baseline = square / 2 - (text.ascent() + text.descent()) / 2;
        for (int index = 0; index < field.columns * field.rows; index++) {
            float left = fieldX + index % field.columns * square, top = fieldY + index / field.columns * square;
            target.set(left + gap, top + gap, left + square - gap, top + square - gap);
            boolean mine = over && field.isMine(index);
            paint.setStyle(Paint.Style.FILL);
            if (field.isOpen(index)) {
                paint.setColor(dark ? 0xE6101214 : 0xE6DFE2E4);
                canvas.drawRoundRect(target, round, round, paint);
                int near = field.near(index);
                if (near > 0) {
                    text.setColor(near == 7 ? ink : NUMBER_COLOURS[near]);
                    canvas.drawText(String.valueOf(near), left + square / 2, top + baseline, text);
                }
                continue;
            }
            paint.setColor(index == field.struck() ? CalculatorLayout.CORAL : index == pressed ? cardEdge : cardColor);
            canvas.drawRoundRect(target, round, round, paint);
            stroke.setColor(cardEdge);
            stroke.setStrokeWidth(Math.max(1, dp(.75f)));
            canvas.drawRoundRect(target, round, round, stroke);
            if (mine && !(field.isFlagged(index) && field.state() == Mines.WON)) {
                drawMine(canvas, left + square / 2, top + square / 2, square, index == field.struck() ? Color.WHITE : ink);
            } else if (field.isFlagged(index)) {
                drawFlag(canvas, left + square / 2, top + square / 2, square, ink, CalculatorLayout.CORAL);
                // A flag that turned out to have no mine under it is crossed through.
                if (over && !field.isMine(index)) {
                    stroke.setColor(ink);
                    stroke.setStrokeWidth(dp(1.6f));
                    canvas.drawLine(target.left + round, target.top + round, target.right - round, target.bottom - round, stroke);
                }
            }
        }
    }

    private void drawFlag(Canvas canvas, float x, float y, float size, int pole, int cloth) {
        path.reset();
        path.moveTo(x - size * .1f, y - size * .27f);
        path.lineTo(x + size * .24f, y - size * .12f);
        path.lineTo(x - size * .1f, y + size * .03f);
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(cloth);
        canvas.drawPath(path, paint);
        stroke.setColor(pole);
        stroke.setStrokeWidth(Math.max(1.5f, size * .06f));
        canvas.drawLine(x - size * .1f, y - size * .27f, x - size * .1f, y + size * .27f, stroke);
        canvas.drawLine(x - size * .22f, y + size * .27f, x + size * .04f, y + size * .27f, stroke);
    }

    private void drawMine(Canvas canvas, float x, float y, float size, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(x, y, size * .19f, paint);
        stroke.setColor(color);
        stroke.setStrokeWidth(Math.max(1.5f, size * .06f));
        for (int spike = 0; spike < 4; spike++) {
            double turn = Math.PI * spike / 4;
            float across = (float) Math.cos(turn) * size * .3f, down = (float) Math.sin(turn) * size * .3f;
            canvas.drawLine(x - across, y - down, x + across, y + down, stroke);
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
        if (darkness > 0) canvas.drawColor(Color.argb(Math.round(242 * darkness), 6, 7, 10));
        if (drawn > 0) drawCurtains(canvas);
        if (bitten > 0) drawBite(canvas);
        if (game == SNATCH) {
            if (binRise > 0) drawBin(canvas);
            if (loot != null && lootAlpha > 0 && lootScale > 0) {
                int half = Math.round(dp(28) * lootScale);
                loot.setBounds(Math.round(lootX) - half, Math.round(lootY) - half, Math.round(lootX) + half, Math.round(lootY) + half);
                loot.setAlpha(Math.round(255 * lootAlpha));
                loot.draw(canvas);
            }
        }
        if (marksAlpha > 0) {
            for (float[] note : notes) drawNote(canvas, note);
            for (float[] piece : pieces) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(CONFETTI_COLOURS[(int) piece[6]]);
                paint.setAlpha(Math.round(240 * marksAlpha));
                canvas.save();
                canvas.rotate(piece[4], piece[0], piece[1]);
                // Turning end over end, a piece shows now its face and now its edge.
                float thin = piece[7] * (.25f + .75f * Math.abs((float) Math.cos(Math.toRadians(piece[4] * 1.7f))));
                canvas.drawRect(piece[0] - piece[7] / 2, piece[1] - thin / 2, piece[0] + piece[7] / 2, piece[1] + thin / 2, paint);
                canvas.restore();
            }
            paint.setAlpha(255);
            for (float[] scoop : scoops) drawScoop(canvas, scoop);
            for (float[] print : prints) drawPrint(canvas, print);
            int half = Math.round(dp(27));
            for (int i = 0; i < loose.size(); i++) {
                float[] icon = loose.get(i);
                if (stepClock < icon[6] && step == 0) continue;
                Drawable drawn = icons.get(i);
                canvas.save();
                canvas.rotate(icon[3], icon[0], icon[1]);
                drawn.setBounds(Math.round(icon[0]) - half, Math.round(icon[1]) - half, Math.round(icon[0]) + half, Math.round(icon[1]) + half);
                drawn.setAlpha(Math.round(255 * marksAlpha));
                drawn.draw(canvas);
                canvas.restore();
            }
        }
        if (game == CROWD) drawCrowd(canvas);
        if (game == BUG && marksAlpha > 0) drawBug(canvas);
        if (marksAlpha > 0) {
            if (game == DRIP) drawDrips(canvas);
            else if (game == SNOW) drawSnow(canvas);
            else if (game == FLOOD) drawFlood(canvas);
            else if (game == WEBS) drawWebs(canvas);
            else if (game == STICKERS) drawStickers(canvas);
            else if (game == FOG) drawFog(canvas);
            else if (game == DISCO) drawDisco(canvas);
            else if (game == BALLOONS) drawBalloons(canvas);
            else if (game == GUM) drawGum(canvas);
            else if (game == FISH) drawFish(canvas);
            else if (game == LASER && step == 0) drawDot(canvas);
        }
        drawHer(canvas);
        drawSaying(canvas);
    }

    /** Stickers of her stuck all over the screen, each on white paper with a tilt, popping on one after another. */
    private void drawStickers(Canvas canvas) {
        for (float[] sticker : props) {
            float since = clock - sticker[4];
            if (since <= 0) continue;
            // Each lands a little too large and settles.
            float pop = since < .18f ? .6f + since / .18f * .55f : Math.max(1, 1.15f - (since - .18f) * 1.5f);
            float tall = sticker[3] * pop, wide = tall * herWidth / herHeight, edge = dp(5);
            canvas.save();
            canvas.rotate(sticker[2], sticker[0], sticker[1]);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x40000000);
            paint.setAlpha(Math.round(64 * marksAlpha));
            canvas.drawRoundRect(sticker[0] - wide / 2 - edge, sticker[1] - tall / 2 - edge + dp(2), sticker[0] + wide / 2 + edge, sticker[1] + tall / 2 + edge + dp(3), dp(10), dp(10), paint);
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(250 * marksAlpha));
            canvas.drawRoundRect(sticker[0] - wide / 2 - edge, sticker[1] - tall / 2 - edge, sticker[0] + wide / 2 + edge, sticker[1] + tall / 2 + edge, dp(10), dp(10), paint);
            target.set(sticker[0] - wide / 2, sticker[1] - tall / 2, sticker[0] + wide / 2, sticker[1] + tall / 2);
            if (pet != null) {
                int width = pet.getWidth() / PetArt.COLUMNS, height = pet.getHeight() / PetArt.FRAMES.length;
                source.set(0, 0, width, height);
                canvas.drawBitmap(pet, source, target, paint);
            } else {
                canvas.drawBitmap(art, null, target, paint);
            }
            canvas.restore();
        }
        paint.setAlpha(255);
    }

    /** The glass misted over by her breath, and a heart drawn in the mist with a finger: where it has been drawn there is no mist. */
    private void drawFog(Canvas canvas) {
        float misted = Math.min(1, clock / 1.3f), drawnSoFar = clamp((clock - 1.2f) / 1.2f, 0, 1);
        Path mist = new Path();
        mist.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        if (drawnSoFar > 0) {
            // A heart, traced from its point round to its point again, as far as her finger has got.
            float size = Math.min(play.width(), play.height()) * .2f, middleX = play.centerX(), middleY = play.centerY() - size * .2f;
            path.reset();
            int points = Math.max(1, Math.round(60 * drawnSoFar));
            for (int point = 0; point <= points; point++) {
                double turn = Math.PI + point / 60.0 * Math.PI * 2;
                float x = middleX + size * (float) (16 * Math.pow(Math.sin(turn), 3)) / 17f;
                float y = middleY - size * (float) (13 * Math.cos(turn) - 5 * Math.cos(2 * turn) - 2 * Math.cos(3 * turn) - Math.cos(4 * turn)) / 17f;
                if (point == 0) path.moveTo(x, y); else path.lineTo(x, y);
            }
            wipe.setStyle(Paint.Style.STROKE);
            wipe.setStrokeCap(Paint.Cap.ROUND);
            wipe.setStrokeJoin(Paint.Join.ROUND);
            wipe.setStrokeWidth(dp(22));
            // The line her finger left, as a shape, taken out of the mist.
            Path line = new Path();
            wipe.getFillPath(path, line);
            mist.op(line, Path.Op.DIFFERENCE);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFECF2F8);
        paint.setAlpha(Math.round(185 * misted * marksAlpha));
        canvas.drawPath(mist, paint);
        paint.setAlpha(255);
    }

    /** The lights go down, a mirror ball comes down from the top of the screen, and beams of colour sweep round from it. */
    private void drawDisco(Canvas canvas) {
        canvas.drawColor(Color.argb(Math.round(150 * Math.min(1, clock / .5f) * marksAlpha), 12, 8, 30));
        float ballX = play.centerX(), ballY = play.top + Math.min(1, clock / .9f) * play.height() * .16f, round = dp(30);
        float far = (float) Math.hypot(play.width(), play.height());
        paint.setStyle(Paint.Style.FILL);
        for (int beam = 0; beam < 6; beam++) {
            double turn = clock * .9 + beam * Math.PI / 3, spread = .13;
            path.reset();
            path.moveTo(ballX, ballY);
            path.lineTo(ballX + (float) Math.cos(turn - spread) * far, ballY + (float) Math.abs(Math.sin(turn - spread)) * far);
            path.lineTo(ballX + (float) Math.cos(turn + spread) * far, ballY + (float) Math.abs(Math.sin(turn + spread)) * far);
            path.close();
            paint.setColor(BRIGHTS[beam]);
            paint.setAlpha(Math.round(70 * marksAlpha));
            canvas.drawPath(path, paint);
        }
        // Spots of light going round the walls.
        for (int spot = 0; spot < 14; spot++) {
            double turn = clock * 1.3 + spot * 2.4;
            paint.setColor(BRIGHTS[spot % BRIGHTS.length]);
            paint.setAlpha(Math.round(150 * marksAlpha));
            canvas.drawCircle(play.centerX() + (float) Math.cos(turn) * play.width() * (.2f + spot % 5 * .07f),
                    play.centerY() + (float) Math.sin(turn * .8 + spot) * play.height() * .4f, dp(7), paint);
        }
        // The ball on its string, in little mirrors.
        stroke.setColor(0xFFB8C0CC);
        stroke.setAlpha(Math.round(255 * marksAlpha));
        stroke.setStrokeWidth(dp(2));
        canvas.drawLine(ballX, 0, ballX, ballY - round, stroke);
        paint.setColor(0xFFDDE4EE);
        paint.setAlpha(Math.round(255 * marksAlpha));
        canvas.drawCircle(ballX, ballY, round, paint);
        stroke.setColor(0xFF8A94A6);
        stroke.setAlpha(Math.round(200 * marksAlpha));
        stroke.setStrokeWidth(dp(1));
        for (int line = -2; line <= 2; line++) {
            float away = line * round / 3, half = (float) Math.sqrt(Math.max(0, round * round - away * away));
            canvas.drawLine(ballX - half, ballY + away, ballX + half, ballY + away, stroke);
            target.set(ballX - Math.abs(away) - round * .12f, ballY - round, ballX + Math.abs(away) + round * .12f, ballY + round);
            canvas.drawOval(target, stroke);
        }
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.round((150 + 100 * (float) Math.sin(clock * 9)) * marksAlpha));
        canvas.drawCircle(ballX - round * .35f, ballY - round * .35f, round * .16f, paint);
        stroke.setAlpha(255);
        paint.setAlpha(255);
    }

    /** Balloons let go from the bottom of the screen, rising on their strings to bob against the top of it. */
    private void drawBalloons(Canvas canvas) {
        for (float[] balloon : props) {
            float since = clock - balloon[5];
            if (since <= 0) continue;
            float size = balloon[2], x = balloon[0] + (float) Math.sin(clock * 1.6f + balloon[4]) * dp(10);
            float y = Math.max(balloon[6] + (float) Math.sin(clock * 2 + balloon[4]) * dp(5), play.bottom + size * 2 - since * balloon[1]);
            stroke.setColor(0xFFEDEDED);
            stroke.setAlpha(Math.round(220 * marksAlpha));
            stroke.setStrokeWidth(dp(1.3f));
            path.reset();
            path.moveTo(x, y + size * 1.2f);
            path.quadTo(x + dp(8), y + size * 2, x - dp(4), y + size * 2.9f);
            canvas.drawPath(path, stroke);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(BRIGHTS[(int) balloon[3]]);
            paint.setAlpha(Math.round(238 * marksAlpha));
            target.set(x - size * .82f, y - size, x + size * .82f, y + size * 1.1f);
            canvas.drawOval(target, paint);
            path.reset();
            path.moveTo(x, y + size * 1.05f);
            path.lineTo(x - size * .16f, y + size * 1.3f);
            path.lineTo(x + size * .16f, y + size * 1.3f);
            path.close();
            canvas.drawPath(path, paint);
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(130 * marksAlpha));
            target.set(x - size * .5f, y - size * .7f, x - size * .18f, y - size * .1f);
            canvas.drawOval(target, paint);
        }
        stroke.setAlpha(255);
        paint.setAlpha(255);
    }

    /** A bubble of gum blown from where she stands until it fills the screen; and, once it has burst, what is left of it round the edges. */
    private void drawGum(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        float mouthX = herX, mouthY = herY - herHeight * .42f;
        if (step == 0) {
            float blown = Math.min(1, stepClock / 2.3f), round = dp(8) + blown * blown * Math.max(play.width(), play.height()) * .62f;
            // It grows from her and drifts toward the middle of the screen as it does.
            float x = mouthX + (play.centerX() - mouthX) * blown, y = mouthY + (play.centerY() - mouthY) * blown;
            paint.setColor(0xFFFF8FC0);
            paint.setAlpha(Math.round(190 * marksAlpha));
            canvas.drawCircle(x, y, round, paint);
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(120 * marksAlpha));
            target.set(x - round * .6f, y - round * .7f, x - round * .15f, y - round * .25f);
            canvas.drawOval(target, paint);
            stroke.setColor(0xFFE0609A);
            stroke.setAlpha(Math.round(220 * marksAlpha));
            stroke.setStrokeWidth(dp(2));
            canvas.drawCircle(x, y, round, stroke);
            stroke.setAlpha(255);
        } else {
            // Burst: blobs along the edges, and strings of it sagging between them.
            paint.setColor(0xFFFF8FC0);
            paint.setAlpha(Math.round(235 * marksAlpha));
            float[] before = props.get(props.size() - 1);
            stroke.setColor(0xFFFF8FC0);
            stroke.setAlpha(Math.round(200 * marksAlpha));
            stroke.setStrokeWidth(dp(5));
            for (int index = 0; index < props.size(); index++) {
                float[] blob = props.get(index);
                canvas.drawCircle(blob[0], blob[1], blob[2], paint);
                canvas.drawCircle(blob[0] + blob[2] * .7f, blob[1] + blob[2] * .5f, blob[2] * .55f, paint);
                if (index % 3 == 0) {
                    path.reset();
                    path.moveTo(before[0], before[1]);
                    path.quadTo(play.centerX(), play.centerY() + dp(60), blob[0], blob[1]);
                    canvas.drawPath(path, stroke);
                }
                before = blob;
            }
            stroke.setAlpha(255);
        }
        paint.setAlpha(255);
    }

    /** Water over the whole screen, with fish going back and forth through it, weed along the bottom, and bubbles. */
    private void drawFish(Canvas canvas) {
        float filled = Math.min(1, clock / 1f);
        canvas.drawColor(Color.argb(Math.round(120 * filled * marksAlpha), 40, 140, 215));
        paint.setStyle(Paint.Style.FILL);
        // Weed, swaying.
        paint.setColor(0xFF2E9E5B);
        paint.setAlpha(Math.round(200 * filled * marksAlpha));
        for (float x = play.left + dp(18); x < play.right; x += dp(46)) {
            float tall = dp(60) + (x * 7 % dp(50)), sway = (float) Math.sin(clock * 1.5f + x) * dp(10);
            path.reset();
            path.moveTo(x - dp(6), play.bottom);
            path.quadTo(x + sway, play.bottom - tall * .6f, x + sway * 1.6f, play.bottom - tall);
            path.quadTo(x + sway + dp(8), play.bottom - tall * .5f, x + dp(6), play.bottom);
            path.close();
            canvas.drawPath(path, paint);
        }
        float span = play.width() + dp(120);
        for (float[] fish : props) {
            float size = fish[2], travelled = (fish[5] + clock * Math.abs(fish[1])) % span;
            boolean rightward = fish[1] > 0;
            float x = rightward ? play.left - dp(60) + travelled : play.right + dp(60) - travelled;
            float y = fish[0] + (float) Math.sin(clock * 2 + fish[4]) * dp(8), way = rightward ? 1 : -1;
            float wag = (float) Math.sin(clock * 9 + fish[4]) * size * .18f;
            paint.setColor(BRIGHTS[(int) fish[3]]);
            paint.setAlpha(Math.round(240 * filled * marksAlpha));
            target.set(x - size * .6f, y - size * .36f, x + size * .6f, y + size * .36f);
            canvas.drawOval(target, paint);
            path.reset();
            path.moveTo(x - way * size * .5f, y);
            path.lineTo(x - way * size * 1.05f, y - size * .36f + wag);
            path.lineTo(x - way * size * 1.05f, y + size * .36f + wag);
            path.close();
            canvas.drawPath(path, paint);
            paint.setColor(Color.WHITE);
            paint.setAlpha(Math.round(255 * filled * marksAlpha));
            canvas.drawCircle(x + way * size * .32f, y - size * .08f, size * .1f, paint);
            paint.setColor(0xFF20242A);
            paint.setAlpha(Math.round(255 * filled * marksAlpha));
            canvas.drawCircle(x + way * size * .35f, y - size * .08f, size * .05f, paint);
        }
        stroke.setColor(Color.WHITE);
        stroke.setAlpha(Math.round(200 * filled * marksAlpha));
        stroke.setStrokeWidth(dp(1.4f));
        for (float[] bubble : specks) canvas.drawCircle(bubble[0] + (float) Math.sin(bubble[4]) * dp(6), bubble[1], bubble[3] * 1.4f, stroke);
        stroke.setAlpha(255);
        paint.setAlpha(255);
    }

    /** The red dot she chases, with a glow round it. */
    private void drawDot(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFFF2D2D);
        paint.setAlpha(Math.round(70 * marksAlpha));
        canvas.drawCircle(dotX, dotY, dp(16) + (float) Math.sin(clock * 20) * dp(2), paint);
        paint.setAlpha(Math.round(255 * marksAlpha));
        canvas.drawCircle(dotX, dotY, dp(6), paint);
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.round(200 * marksAlpha));
        canvas.drawCircle(dotX - dp(1.5f), dotY - dp(1.5f), dp(1.8f), paint);
        paint.setAlpha(255);
    }

    /** Paint along the top of the screen, running down it in drips of different lengths. */
    private void drawDrips(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(CalculatorLayout.CORAL);
        paint.setAlpha(Math.round(245 * marksAlpha));
        canvas.drawRect(play.left, 0, play.right, play.top + dp(12), paint);
        for (float[] drip : drips) {
            float length = clamp((clock - drip[3]) * drip[2], 0, drip[4]), half = drip[1] / 2;
            if (length <= 0) continue;
            canvas.drawRoundRect(drip[0] - half, play.top, drip[0] + half, play.top + dp(12) + length, half, half, paint);
            canvas.drawCircle(drip[0], play.top + dp(12) + length, half * 1.25f, paint);
        }
        paint.setAlpha(255);
    }

    /** Snow coming down, and lying deeper along the bottom of the screen the longer it falls. */
    private void drawSnow(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.round(235 * marksAlpha));
        for (float[] flake : specks) {
            canvas.drawCircle(flake[0] + (float) Math.sin(flake[4]) * dp(9), flake[1], flake[3], paint);
        }
        float deep = Math.min(dp(84), clock * dp(20));
        path.reset();
        path.moveTo(play.left, getHeight());
        path.lineTo(play.left, play.bottom - deep);
        for (float x = play.left; x <= play.right + dp(24); x += dp(24)) {
            path.lineTo(x, play.bottom - deep - (float) Math.sin(x / dp(46)) * dp(9) - (float) Math.sin(x / dp(17)) * dp(3));
        }
        path.lineTo(play.right, getHeight());
        path.close();
        canvas.drawPath(path, paint);
        paint.setAlpha(255);
    }

    /** Water rising up the screen, its top rolling, with bubbles coming up through it. */
    private void drawFlood(Canvas canvas) {
        float level = play.bottom - Math.min(.78f, clock / 3.4f) * play.height();
        path.reset();
        path.moveTo(play.left, getHeight());
        path.lineTo(play.left, level);
        for (float x = play.left; x <= play.right + dp(20); x += dp(20)) {
            path.lineTo(x, level + (float) Math.sin(x / dp(38) + clock * 2.4f) * dp(6));
        }
        path.lineTo(play.right, getHeight());
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF3B9BE8);
        paint.setAlpha(Math.round(150 * marksAlpha));
        canvas.drawPath(path, paint);
        stroke.setColor(Color.WHITE);
        stroke.setAlpha(Math.round(200 * marksAlpha));
        stroke.setStrokeWidth(dp(1.4f));
        for (float[] bubble : specks) {
            if (bubble[1] > level + dp(10)) canvas.drawCircle(bubble[0] + (float) Math.sin(bubble[4]) * dp(6), bubble[1], bubble[3] * 1.4f, stroke);
        }
        stroke.setAlpha(255);
        paint.setAlpha(255);
    }

    /** A cobweb in each corner of the screen, spun out from it, and a spider let down on a thread. */
    private void drawWebs(Canvas canvas) {
        float spun = Math.min(1, clock / 1.2f), reach = Math.min(play.width(), play.height()) * .34f * spun;
        stroke.setColor(Color.WHITE);
        stroke.setAlpha(Math.round(215 * marksAlpha));
        stroke.setStrokeWidth(dp(1.2f));
        for (int corner = 0; corner < 4; corner++) {
            float x = corner % 2 == 0 ? play.left : play.right, y = corner < 2 ? play.top : play.bottom;
            float across = corner % 2 == 0 ? 1 : -1, down = corner < 2 ? 1 : -1;
            for (int ray = 0; ray <= 4; ray++) {
                double turn = Math.PI / 2 * ray / 4;
                canvas.drawLine(x, y, x + across * (float) Math.cos(turn) * reach, y + down * (float) Math.sin(turn) * reach, stroke);
            }
            for (int ring = 1; ring <= 3; ring++) {
                float far = reach * ring / 3.4f;
                path.reset();
                for (int ray = 0; ray <= 4; ray++) {
                    double turn = Math.PI / 2 * ray / 4;
                    // Each strand sags toward the corner between the rays it hangs from.
                    float px = x + across * (float) Math.cos(turn) * far, py = y + down * (float) Math.sin(turn) * far;
                    if (ray == 0) {
                        path.moveTo(px, py);
                    } else {
                        double before = Math.PI / 2 * (ray - .5f) / 4;
                        path.quadTo(x + across * (float) Math.cos(before) * far * .84f, y + down * (float) Math.sin(before) * far * .84f, px, py);
                    }
                }
                canvas.drawPath(path, stroke);
            }
        }
        // The spider, bobbing on its thread.
        float x = play.left + play.width() * .62f, hang = (dp(130) + (float) Math.sin(clock * 2) * dp(16)) * spun, y = play.top + hang;
        canvas.drawLine(x, 0, x, y, stroke);
        stroke.setColor(0xFF2A2326);
        stroke.setAlpha(Math.round(255 * marksAlpha));
        stroke.setStrokeWidth(dp(1.6f));
        for (int leg = 0; leg < 4; leg++) {
            float lift = dp(3) + leg * dp(3.4f), out = dp(15) - Math.abs(leg - 1.5f) * dp(2);
            canvas.drawLine(x, y + dp(4), x - out, y - dp(6) + lift, stroke);
            canvas.drawLine(x, y + dp(4), x + out, y - dp(6) + lift, stroke);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF2A2326);
        paint.setAlpha(Math.round(255 * marksAlpha));
        canvas.drawCircle(x, y + dp(5), dp(8), paint);
        canvas.drawCircle(x, y - dp(4), dp(4.5f), paint);
        paint.setAlpha(255);
        stroke.setAlpha(255);
    }

    /** Her sisters, each as far up from below the screen as she has come, each at her own point in the wave. */
    private void drawCrowd(Canvas canvas) {
        paint.setAlpha(255);
        for (float[] sister : crowd) {
            if (sister[1] <= 0) continue;
            float risen = 1 - (1 - sister[1]) * (1 - sister[1]);
            float feet = play.bottom + herHeight * (1 - risen);
            target.set(sister[0] - herWidth / 2, feet - herHeight, sister[0] + herWidth / 2, feet);
            if (pet != null) {
                int width = pet.getWidth() / PetArt.COLUMNS, height = pet.getHeight() / PetArt.FRAMES.length;
                int column = ((int) (clock * 1000 / PetArt.FRAME_MS[PetArt.WAVE]) + (int) sister[3]) % PetArt.FRAMES[PetArt.WAVE];
                source.set(column * width, PetArt.WAVE * height, (column + 1) * width, (PetArt.WAVE + 1) * height);
                canvas.drawBitmap(pet, source, target, paint);
            } else {
                canvas.drawBitmap(art, null, target, paint);
            }
        }
    }

    /** The bug: a ladybird in her own colours, legs going, turned the way it is crawling. */
    private void drawBug(Canvas canvas) {
        float size = dp(13), stride = (float) Math.sin(clock * 22) * dp(2);
        int alpha = Math.round(255 * marksAlpha), black = 0xFF2A2326;
        canvas.save();
        canvas.rotate(bugTurn, bugX, bugY);
        stroke.setColor(black);
        stroke.setAlpha(alpha);
        stroke.setStrokeWidth(dp(1.6f));
        for (int leg = -1; leg <= 1; leg++) {
            float y = bugY + leg * size * .45f, lift = leg == 0 ? -stride : stride;
            canvas.drawLine(bugX - size * .5f, y, bugX - size * 1.1f, y + lift, stroke);
            canvas.drawLine(bugX + size * .5f, y, bugX + size * 1.1f, y - lift, stroke);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(black);
        paint.setAlpha(alpha);
        canvas.drawCircle(bugX, bugY - size * .8f, size * .42f, paint);
        paint.setColor(CalculatorLayout.CORAL);
        paint.setAlpha(alpha);
        canvas.drawOval(bugX - size * .72f, bugY - size * .85f, bugX + size * .72f, bugY + size * .85f, paint);
        paint.setColor(black);
        paint.setAlpha(alpha);
        canvas.drawRect(bugX - dp(.7f), bugY - size * .8f, bugX + dp(.7f), bugY + size * .8f, paint);
        canvas.drawCircle(bugX - size * .36f, bugY - size * .25f, size * .15f, paint);
        canvas.drawCircle(bugX + size * .36f, bugY - size * .25f, size * .15f, paint);
        canvas.drawCircle(bugX - size * .32f, bugY + size * .35f, size * .13f, paint);
        canvas.drawCircle(bugX + size * .32f, bugY + size * .35f, size * .13f, paint);
        canvas.restore();
        paint.setAlpha(255);
        stroke.setAlpha(255);
    }

    /**
     * A scoop of ice cream where it landed: a blob with smaller blobs flung out round it, and
     * drips that run down the longer it sits. What it looks like comes from the number it was
     * given when it landed, so it is the same shape in every frame.
     */
    private void drawScoop(Canvas canvas, float[] scoop) {
        float x = scoop[0], y = scoop[1], size = scoop[2], age = clock - scoop[4];
        float pop = Math.min(1, age / .12f);
        int seed = (int) scoop[5];
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(FLAVOURS[(int) scoop[3]]);
        paint.setAlpha(Math.round(245 * marksAlpha));
        canvas.drawCircle(x, y, size * pop, paint);
        for (int blob = 0; blob < 6; blob++) {
            seed = seed * 1103515245 + 12345;
            double way = ((seed >>> 16) & 0x3FF) / 1024.0 * Math.PI * 2;
            seed = seed * 1103515245 + 12345;
            float out = size * (.78f + ((seed >>> 16) & 0xFF) / 255f * .5f) * pop;
            float small = size * (.2f + ((seed >>> 8) & 0xFF) / 255f * .24f) * pop;
            canvas.drawCircle(x + (float) Math.cos(way) * out, y + (float) Math.sin(way) * out, small, paint);
        }
        for (int drip = 0; drip < 3; drip++) {
            seed = seed * 1103515245 + 12345;
            float across = (((seed >>> 16) & 0xFF) / 255f - .5f) * size * 1.2f;
            float wide = size * (.16f + ((seed >>> 8) & 0xFF) / 255f * .12f);
            float run = Math.min(size * (1 + ((seed >>> 4) & 0xFF) / 255f * 1.3f), age * dp(44));
            canvas.drawRoundRect(x + across - wide / 2, y, x + across + wide / 2, y + size * .5f + run, wide / 2, wide / 2, paint);
        }
        // A glint, as on anything wet.
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.round(150 * marksAlpha));
        canvas.drawCircle(x - size * .34f * pop, y - size * .34f * pop, size * .16f * pop, paint);
        paint.setAlpha(255);
    }

    /** A square of coloured paper stuck up at a tilt, with a few words in her hand on it. */
    private void drawNote(Canvas canvas, float[] note) {
        float size = dp(92), pop = Math.min(1, (clock - note[4]) / .12f), half = size / 2 * pop;
        canvas.save();
        canvas.translate(note[0], note[1]);
        canvas.rotate(note[2]);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAlpha(Math.round(50 * marksAlpha));
        canvas.drawRect(-half + dp(2), -half + dp(3), half + dp(2), half + dp(3), paint);
        paint.setColor(PAPER[(int) note[3]]);
        paint.setAlpha(Math.round(250 * marksAlpha));
        canvas.drawRect(-half, -half, half, half, paint);
        // The strip of glue along the top, a shade darker.
        paint.setColor(Color.BLACK);
        paint.setAlpha(Math.round(22 * marksAlpha));
        canvas.drawRect(-half, -half, half, -half + dp(13) * pop, paint);
        if (pop >= 1) {
            text.setTextSize(dp(13));
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(0xFF3A3D3B);
            text.setAlpha(Math.round(255 * marksAlpha));
            List<String> lines = wrap(names.named(NOTES_SAY[(int) note[5]]), size - dp(18));
            float line = dp(17), top = dp(6) - lines.size() * line / 2;
            for (int i = 0; i < lines.size(); i++) canvas.drawText(lines.get(i), 0, top + line * (i + .8f), text);
            text.setAlpha(255);
        }
        paint.setAlpha(255);
        canvas.restore();
    }

    /** Stage curtains drawn from both sides to meet in the middle, in folds. */
    private void drawCurtains(Canvas canvas) {
        float half = getWidth() / 2f, reach = half * drawn, fold = dp(26);
        for (int side = 0; side < 2; side++) {
            float from = side == 0 ? 0 : getWidth() - reach, to = side == 0 ? reach : getWidth();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFD92B35);
            canvas.drawRect(from, 0, to, getHeight(), paint);
            // Folds, running down from the rail: a darker stripe and a lighter one to each.
            for (float x = side == 0 ? to - fold : from; side == 0 ? x > from - fold : x < to; x += side == 0 ? -fold : fold) {
                paint.setColor(0x33000000);
                canvas.drawRect(Math.max(from, x), 0, Math.min(to, x + fold * .38f), getHeight(), paint);
                paint.setColor(0x1FFFFFFF);
                canvas.drawRect(Math.max(from, x + fold * .6f), 0, Math.min(to, x + fold * .78f), getHeight(), paint);
            }
            // A gold edge where the two meet.
            paint.setColor(0xFFFFD36B);
            float edge = side == 0 ? to - dp(5) : from;
            canvas.drawRect(edge, 0, edge + dp(5), getHeight(), paint);
        }
        // The pelmet they hang from.
        paint.setColor(0xFFB8202A);
        canvas.drawRect(0, 0, getWidth(), (play.top + dp(22)) * drawn, paint);
    }

    /** A bite out of a top corner of the screen: nothing where the screen was, with tooth marks round it. */
    private void drawBite(Canvas canvas) {
        float x = biteLeft ? 0 : getWidth(), reach = dp(118);
        path.reset();
        path.addCircle(x, 0, reach, Path.Direction.CW);
        // Each tooth took its own little scoop out of the edge.
        for (int tooth = 0; tooth <= 6; tooth++) {
            double way = Math.PI / 2 * tooth / 6;
            path.addCircle(x + (biteLeft ? 1 : -1) * (float) Math.cos(way) * reach, (float) Math.sin(way) * reach, dp(21), Path.Direction.CW);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF050608);
        paint.setAlpha(Math.round(255 * bitten));
        canvas.drawPath(path, paint);
        paint.setAlpha(255);
    }

    /** A cat's paw print: a pad and four toes, pointing the way she was going. */
    private void drawPrint(Canvas canvas, float[] print) {
        canvas.save();
        canvas.translate(print[0], print[1]);
        canvas.rotate(print[2] + 90);
        for (int pass = 0; pass < 2; pass++) {
            // A light edge under the pink, so the prints show on any screen.
            float more = pass == 0 ? dp(1.2f) : 0;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(pass == 0 ? Color.WHITE : 0xFFFF6F91);
            paint.setAlpha(Math.round((pass == 0 ? 200 : 235) * marksAlpha));
            box.set(-dp(5.5f) - more, -dp(2) - more, dp(5.5f) + more, dp(7) + more);
            canvas.drawOval(box, paint);
            canvas.drawCircle(-dp(6.5f), -dp(5.5f), dp(2.4f) + more, paint);
            canvas.drawCircle(-dp(2.3f), -dp(8.5f), dp(2.4f) + more, paint);
            canvas.drawCircle(dp(2.3f), -dp(8.5f), dp(2.4f) + more, paint);
            canvas.drawCircle(dp(6.5f), -dp(5.5f), dp(2.4f) + more, paint);
        }
        paint.setAlpha(255);
        canvas.restore();
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
