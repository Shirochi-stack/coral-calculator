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
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The stage that one of the unlocked tricks is played on: a little scene with Halgakos in it,
 * drawn over whatever is on the screen. It opens with a word from her, plays its scene, and
 * ends with her owning up, when all that the scene put on the screen fades away; a touch brings
 * that ending on at once, and a touch after it ends the trick. Nothing on the device is changed.
 *
 * A scene is drawn from the time since it began: where everything is, and what she is saying,
 * follow from that, so a scene keeps almost nothing of its own. What is here is what the scenes
 * draw with; the scenes are in {@link TrickRun} and {@link Trick}.
 */
abstract class TrickBase {
    private static final int OPEN = 0, SCENE = 1, OWN = 2, HOME = 3;
    /** How long her opening word and her owning up stay, and how fast a scene plays: slower than it was written, so that what she says can be read and what happens reacted to. */
    private static final float OPENING = 2.2f, OWNING = 4f, PACE = .62f;
    /** The pictures the scenes borrow from the games: how many frames across each strip has. */
    static final int FX = TrickArt.fight_fx, PIECES = TrickArt.plus_run_art, FACES = TrickArt.fight_faces, NPC = TrickArt.run_npc;
    static final int LOSE = TrickArt.halgakos_lose, JUMP = TrickArt.halgakos_jump;
    /** In {@link #FX}: her tablet, a key, a loaf, brick bread, the bat, an ink tear, a paper knife, a heart, pink magic and a burst. */
    static final int TABLET = 0, KEY = 1, LOAF = 2, BRICK = 3, BAT = 4, TEAR = 5, KNIFE = 6, HEART = 7, MAGIC = 8, BURST = 9;
    /** In {@link #PIECES}: a plus, a minus, a flag, a gate, a star, a heart, a times and a divide sign, Zero, and what she picks up. */
    static final int PLUS = 13, MINUS = 14, FLAG = 15, GATE = 16, STAR = 19, RED_HEART = 20, TIMES = 21, DIVIDE = 23, ZERO = 25, PEN = 28,
            BOOK = 30, BRUSH = 31, SWORD = 32, GUN = 33, BREAD = 34, ICE = 35, INK = 36, MAGNET = 37, NOTES = 38;
    /** In {@link #NPC}: the villagers. */
    static final int SOMI = 0, INJU = 1, BOKSUN = 2, TTOSUN = 3, GALPI = 4, YESEO = 5, NABI = 6, MORU = 7, ALTTEUL = 8;

    final Context context;
    final int kind;
    final boolean dark;
    final Random random = new Random();
    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    final RectF box = new RectF();
    final Path path = new Path();
    /** Apps picked from the launcher for a scene to seem to act on: only their icons and names are read. */
    final List<Drawable> icons = new ArrayList<>();
    final List<String> labels = new ArrayList<>();
    private final Bitmap art, pet;
    private final CompanionPrefs names;
    private final float density;
    private final int hair;
    private final RectF play = new RectF(), target = new RectF();
    private final Rect source = new Rect();
    private final SparseArray<Bitmap> sheets = new SparseArray<>();

    /** The room there is: its edges, its size and its middle; and the line her feet are on. */
    float L, T, R, B, W, H, cx, cy, floor;
    /** The time since the scene began, and since the frame before. */
    float t, dt;
    /** Her feet, her size, and which way she faces. */
    float hx, hy, tall, wide;
    boolean leftward;
    /** How she is drawn in this frame of a scene: how flat, how far off the ground, turned how far, how faint and how large. */
    float squash = 1, lift, spin, herAlpha = 1, herScale = 1;

    private float homeX = Float.NaN, homeY = Float.NaN, homeTall = Float.NaN;
    private int phase = OPEN;
    private float clock, fade = 1;
    private boolean owning, herDrawn, started;
    private int row = PetArt.IDLE, wornFrames, wornFrame;
    private Bitmap worn;
    private float rowClock, wornSize = 1;
    private String saying;
    private float sayLeft;

    TrickBase(Context context, int kind, boolean dark, Bitmap art, Bitmap pet) {
        this.context = context;
        this.kind = kind;
        this.dark = dark;
        this.art = art;
        this.pet = pet;
        names = new CompanionPrefs(context);
        density = context.getResources().getDisplayMetrics().density;
        hair = pet == null ? Hair.PINK : Hair.chosen(context);
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
    }

    /** How long the scene runs, in seconds, if it is left to. */
    abstract float length();

    /** Draws the scene as it stands at {@link #t}, and has her do what she does in it. */
    abstract void scene(Canvas canvas);

    /** What she says as she owns up. */
    abstract String owningUp();

    /** Where she stands when this begins, and how tall: she starts from there, and goes back. */
    void setHome(float x, float feet, float height) {
        homeX = x;
        homeY = feet;
        homeTall = height;
    }

    /** Fits the scene to the room there is: at first, and again when the screen turns or folds. */
    void layout(RectF room) {
        play.set(room);
        L = room.left;
        T = room.top;
        R = room.right;
        B = room.bottom;
        W = room.width();
        H = room.height();
        cx = room.centerX();
        cy = room.centerY();
        tall = !Float.isNaN(homeTall) ? homeTall : Math.max(dp(84), Math.min(dp(136), Math.min(W, H) * .17f));
        wide = tall * (pet != null ? (pet.getWidth() / PetArt.COLUMNS) / (float) (pet.getHeight() / PetArt.FRAMES.length)
                : art.getWidth() / (float) art.getHeight());
        if (Float.isNaN(homeX)) {
            homeX = R - wide / 2 - dp(8);
            homeY = B - dp(8);
        }
        homeX = clamp(homeX, L + wide / 2, R - wide / 2);
        homeY = clamp(homeY, T + tall, B);
        // A scene is played out along a line a little above the bottom of the screen, or where she stood if that is higher.
        floor = clamp(homeY, T + H * .45f, B - dp(56));
        if (!started) {
            started = true;
            hx = homeX;
            hy = homeY;
            say(Chatter.prank(random), OPENING + .1f);
        }
        hx = clamp(hx, L + wide / 2, R - wide / 2);
        hy = clamp(hy, T + tall, B);
    }

    /** Moves the trick on by the time since the frame before; and says whether it is over. */
    boolean advance(float seconds) {
        dt = seconds;
        clock += seconds;
        rowClock += seconds;
        if (sayLeft > 0 && (sayLeft -= seconds) <= 0 && phase != SCENE) saying = null;
        switch (phase) {
            case OPEN:
                if (clock >= OPENING) {
                    phase = SCENE;
                    saying = null;
                    t = 0;
                }
                break;
            case SCENE:
                // (The scene's own time runs slower than the clock, and so does her running in it: its lines stay with what happens.)
                dt = seconds * PACE;
                t += dt;
                if (t >= length()) ownUp();
                break;
            case OWN:
                fade = Math.max(0, 1 - clock / .7f);
                if (clock >= OWNING) {
                    phase = HOME;
                    clock = 0;
                }
                break;
            default:
                // Home at a run that is seen to be a run, not a blink; given time to get there from anywhere on the screen.
                if (reach(homeX, homeY, dp(HOME_RUN)) || clock > 7) return true;
                break;
        }
        return false;
    }

    /** A touch: it brings on the ending, and after that ends the trick. Says whether the trick is over. */
    boolean touch() {
        if (phase == OPEN || phase == SCENE) {
            ownUp();
            return false;
        }
        // While she owns up, a touch sends her home now; and once she is on her way, it lets her get there. (It used to end the trick at
        // once, and she was simply back where she started, without the run back.)
        if (phase == OWN) {
            phase = HOME;
            clock = 0;
        }
        return false;
    }

    /** How fast she runs home after a prank, in dp a second. */
    private static final float HOME_RUN = 340;

    private void ownUp() {
        phase = OWN;
        clock = 0;
        owning = true;
        row = PetArt.JUMP;
        rowClock = 0;
        squash = 1;
        lift = 0;
        spin = 0;
        herAlpha = 1;
        herScale = 1;
        worn = null;
        hx = clamp(hx, L + wide / 2, R - wide / 2);
        hy = clamp(hy, T + tall, B);
        say(owningUp(), OWNING - .1f);
    }

    void draw(Canvas canvas) {
        if (!started) return;
        herDrawn = false;
        if (phase == SCENE) {
            saying = null;
            worn = null;
            squash = 1;
            lift = 0;
            spin = 0;
            herAlpha = 1;
            herScale = 1;
            scene(canvas);
        } else if (phase == OWN && fade > 0) {
            // What the scene put on the screen is left as it was, and fades; she is no longer part of it.
            int layer = canvas.saveLayerAlpha(play, Math.round(255 * fade));
            scene(canvas);
            canvas.restoreToCount(layer);
            // Whatever the scene would have had her look like, she is herself again.
            squash = 1;
            lift = 0;
            spin = 0;
            herAlpha = 1;
            herScale = 1;
            if (clock > .5f && row == PetArt.JUMP) pose(PetArt.IDLE);
        }
        paint.setAlpha(255);
        stroke.setAlpha(255);
        if (!herDrawn) drawHer(canvas);
        drawSaying(canvas);
    }

    // What a scene has her do. None of it does anything once she has owned up.

    /** How far along it is between two moments of the scene, from 0 to 1. */
    float p(float from, float to) { return t <= from ? 0 : t >= to ? 1 : (t - from) / (to - from); }

    boolean in(float from, float to) { return t >= from && t < to; }

    /** A smooth start and stop; a quick start that settles; and a slow start that hurries. */
    static float ease(float x) { return x * x * (3 - 2 * x); }

    static float out(float x) { return 1 - (1 - x) * (1 - x); }

    static float into(float x) { return x * x; }

    /** Something between 0 and 1 that is the same every time for the same two numbers: what scatters a scene's things. */
    static float rnd(int which, int what) {
        int mixed = which * 374761393 + what * 668265263;
        mixed = (mixed ^ mixed >>> 13) * 1274126177;
        return ((mixed ^ mixed >>> 16) & 0xFFFF) / 65536f;
    }

    /** She says this from a moment of the scene, for so long. */
    void line(float at, float seconds, String words) {
        if (!owning && t >= at && t < at + seconds) saying = names.named(words);
    }

    /** Runs her toward a place, and says whether she has reached it. */
    boolean go(float x, float y, float speed) {
        if (owning) return true;
        return reach(x, y, speed);
    }

    private boolean reach(float x, float y, float speed) {
        float across = x - hx, down = y - hy, distance = (float) Math.hypot(across, down), step = speed * dt;
        if (Math.abs(across) > 1) leftward = across < 0;
        if (distance <= step || distance < 1) {
            hx = x;
            hy = y;
            if (row == PetArt.RUN_LEFT || row == PetArt.RUN_RIGHT) pose(PetArt.IDLE);
            return true;
        }
        hx += across / distance * step;
        hy += down / distance * step;
        pose(leftward ? PetArt.RUN_LEFT : PetArt.RUN_RIGHT);
        return false;
    }

    /** Puts her somewhere. */
    void put(float x, float y) {
        if (owning) return;
        hx = x;
        hy = y;
    }

    /** The pose she holds: one of the rows of {@link PetArt}. */
    void pose(int value) {
        if (row == value) return;
        row = value;
        rowClock = 0;
    }

    /** Has her hold a pose in a scene. */
    void act(int value) {
        if (!owning) pose(value);
    }

    /** In this frame she is drawn from one of her other strips: knocked over, in the air, or holding something. */
    void wear(int strip, int frames, int frame, float size) {
        if (owning || pet == null) return;
        wear(sheet(strip), frames, frame, size);
    }

    /** The same, from a strip of her made for the scene. */
    void wear(Bitmap strip, int frames, int frame, float size) {
        if (owning || pet == null || strip == null) return;
        worn = strip;
        wornFrames = frames;
        wornFrame = Math.max(0, Math.min(frames - 1, frame));
        wornSize = size;
    }

    /** A frame of her holding what she fights with in Plus run: 0 a pen, then a knife, a book, a brush, a sword and the ink gun. */
    void armed(int weapon, int frame) {
        int[] strips = {TrickArt.halgakos_arm0, TrickArt.halgakos_arm1, TrickArt.halgakos_arm2, TrickArt.halgakos_arm3,
                TrickArt.halgakos_arm4, TrickArt.halgakos_arm5};
        wear(strips[weapon], 8, frame, 1.2f);
    }

    /** Draws her where the scene has her among its things: what is drawn after this is in front of her. */
    void her(Canvas canvas) {
        if (owning) return;
        herDrawn = true;
        drawHer(canvas);
    }

    /** Another of her, as she is drawn in this frame, standing somewhere else. */
    void copy(Canvas canvas, float x, float feet) {
        if (owning) return;
        float wasX = hx, wasY = hy;
        hx = x;
        hy = feet;
        drawHer(canvas);
        hx = wasX;
        hy = wasY;
    }

    /** Her animated frames, or null where she is the still one. */
    Bitmap pet() { return pet; }

    /** Words with the user called what they asked to be called. */
    String named(String words) { return names.named(words); }

    private void drawHer(Canvas canvas) {
        float height = tall * herScale, width = wide * herScale;
        canvas.save();
        canvas.translate(hx, hy - lift);
        if (spin != 0) canvas.rotate(spin, 0, -height / 2);
        if (squash != 1) canvas.scale(1 + (1 - squash) * .35f, squash);
        paint.setAlpha(Math.round(255 * Math.max(0, Math.min(1, herAlpha))));
        if (pet != null && worn != null) {
            Bitmap strip = worn;
            int cell = strip.getWidth() / wornFrames;
            float high = height * wornSize, across = high * cell / strip.getHeight();
            if (leftward) canvas.scale(-1, 1);
            source.set(wornFrame * cell, 0, (wornFrame + 1) * cell, strip.getHeight());
            target.set(-across / 2, -high, across / 2, 0);
            canvas.drawBitmap(strip, source, target, paint);
        } else if (pet != null) {
            int cellWide = pet.getWidth() / PetArt.COLUMNS, cellHigh = pet.getHeight() / PetArt.FRAMES.length;
            int column = (int) (rowClock * 1000 / PetArt.FRAME_MS[row]);
            // A jump is played through once, and held at its end; the rest go round.
            column = row == PetArt.JUMP ? Math.min(column, PetArt.FRAMES[row] - 1) : column % PetArt.FRAMES[row];
            source.set(column * cellWide, row * cellHigh, (column + 1) * cellWide, (row + 1) * cellHigh);
            target.set(-width / 2, -height, width / 2, 0);
            canvas.drawBitmap(pet, source, target, paint);
        } else {
            if (leftward) canvas.scale(-1, 1);
            target.set(-width / 2, -height, width / 2, 0);
            canvas.drawBitmap(art, null, target, paint);
        }
        paint.setAlpha(255);
        canvas.restore();
    }

    // What a scene draws with

    float dp(float value) { return value * density; }

    static float clamp(float value, float low, float high) { return high < low ? low : Math.max(low, Math.min(high, value)); }

    /** A picture borrowed from the games. Those of her are in the colour of hair she is wearing. */
    Bitmap sheet(int resource) {
        Bitmap had = sheets.get(resource);
        if (had != null) return had;
        boolean hers = resource == LOSE || resource == JUMP || resource == TrickArt.halgakos_arm0 || resource == TrickArt.halgakos_arm1
                || resource == TrickArt.halgakos_arm2 || resource == TrickArt.halgakos_arm3 || resource == TrickArt.halgakos_arm4
                || resource == TrickArt.halgakos_arm5 || resource == TrickArt.halgakos_party;
        Bitmap made = hers && hair != Hair.PINK ? Hair.dyed(BitmapFactory.decodeResource(context.getResources(), resource), hair)
                : BitmapFactory.decodeResource(context.getResources(), resource);
        sheets.put(resource, made);
        return made;
    }

    /** One frame of a strip of them, standing on a line: so tall, and turned to face left if asked. */
    void strip(Canvas canvas, int resource, int frames, int frame, float x, float feet, float height, boolean flip, float alpha) {
        Bitmap sheet = sheet(resource);
        if (sheet == null || alpha <= 0) return;
        int cell = sheet.getWidth() / frames;
        float width = height * cell / sheet.getHeight();
        source.set(frame * cell, 0, (frame + 1) * cell, sheet.getHeight());
        target.set(x - width / 2, feet - height, x + width / 2, feet);
        paint.setAlpha(Math.round(255 * Math.min(1, alpha)));
        canvas.save();
        if (flip) canvas.scale(-1, 1, x, feet);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
        paint.setAlpha(255);
    }

    /** One square picture out of a row of them, about a middle: so large, turned so far. */
    private void square(Canvas canvas, int resource, int across, int which, float x, float y, float size, float turn, float alpha) {
        Bitmap sheet = sheet(resource);
        if (sheet == null || alpha <= 0 || size <= 0) return;
        int cell = sheet.getWidth() / across;
        source.set(which * cell, 0, (which + 1) * cell, sheet.getHeight());
        target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        paint.setAlpha(Math.round(255 * Math.min(1, alpha)));
        canvas.save();
        if (turn != 0) canvas.rotate(turn, x, y);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
        paint.setAlpha(255);
    }

    /** Something a fighter of Page brawl fights with. */
    void fx(Canvas canvas, int which, float x, float y, float size, float turn, float alpha) { square(canvas, FX, 10, which, x, y, size, turn, alpha); }

    /** Something out of the lands of Plus run. */
    void piece(Canvas canvas, int which, float x, float y, float size, float turn, float alpha) { square(canvas, PIECES, 40, which, x, y, size, turn, alpha); }

    /** The face of one of the five, in a round frame of her colour. */
    void portrait(Canvas canvas, int who, float x, float y, float radius, float alpha) {
        Bitmap sheet = sheet(FACES);
        if (sheet == null || alpha <= 0 || radius <= 0) return;
        int cell = sheet.getWidth() / 5;
        canvas.save();
        path.reset();
        path.addCircle(x, y, radius, Path.Direction.CW);
        canvas.clipPath(path);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.round(255 * alpha));
        canvas.drawCircle(x, y, radius, paint);
        source.set(who * cell, 0, (who + 1) * cell, sheet.getHeight());
        target.set(x - radius, y - radius, x + radius, y + radius);
        canvas.drawBitmap(sheet, source, target, paint);
        canvas.restore();
        stroke.setColor(FightCast.COLOURS[who]);
        stroke.setAlpha(Math.round(255 * alpha));
        stroke.setStrokeWidth(dp(3));
        canvas.drawCircle(x, y, radius, stroke);
        stroke.setAlpha(255);
        paint.setAlpha(255);
    }

    /** One of the apps' icons, about a middle. */
    void icon(Canvas canvas, int which, float x, float y, float size, float turn, float alpha) {
        if (icons.isEmpty() || alpha <= 0 || size <= 0) return;
        Drawable icon = icons.get(which % icons.size());
        int half = Math.round(size / 2);
        canvas.save();
        canvas.translate(x, y);
        if (turn != 0) canvas.rotate(turn);
        icon.setBounds(-half, -half, half, half);
        icon.setAlpha(Math.round(255 * Math.min(1, alpha)));
        icon.draw(canvas);
        icon.setAlpha(255);
        canvas.restore();
    }

    String label(int which) { return labels.isEmpty() ? "Calculator" : labels.get(which % labels.size()); }

    /** The same, cut short enough to go under its icon in a row of them. */
    String tag(int which) {
        String name = label(which);
        return name.length() > 10 ? name.substring(0, 9).trim() + "…" : name;
    }

    /** Words about a middle, in a colour. */
    void words(Canvas canvas, String words, float x, float y, float size, int colour, float alpha) {
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(size);
        text.setColor(colour);
        text.setAlpha(Math.round(255 * Math.max(0, Math.min(1, alpha))));
        canvas.drawText(words, x, y + size * .35f, text);
        text.setAlpha(255);
    }

    /** Words on a rounded plate, about a middle. */
    void plate(Canvas canvas, String words, float x, float y, float size, int ink, int back, float alpha) {
        if (alpha <= 0) return;
        text.setTextSize(size);
        float width = text.measureText(words) + size * 1.3f, height = size * 1.9f;
        box.set(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(back);
        paint.setAlpha(Math.round(Color.alpha(back) * Math.min(1, alpha)));
        canvas.drawRoundRect(box, height / 2, height / 2, paint);
        paint.setAlpha(255);
        words(canvas, words, x, y, size, ink, alpha);
    }

    /** A colour over the whole screen. */
    void wash(Canvas canvas, int colour, float alpha) {
        if (alpha <= 0) return;
        canvas.drawColor(Color.argb(Math.round(Color.alpha(colour) * Math.min(1, alpha)), Color.red(colour), Color.green(colour), Color.blue(colour)));
    }

    void fill(int colour, float alpha) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colour);
        paint.setAlpha(Math.round(Color.alpha(colour) * Math.max(0, Math.min(1, alpha))));
    }

    void pen(int colour, float width, float alpha) {
        stroke.setColor(colour);
        stroke.setStrokeWidth(width);
        stroke.setAlpha(Math.round(Color.alpha(colour) * Math.max(0, Math.min(1, alpha))));
    }

    /** A shake of the whole scene for a moment after something lands: how far across and down to move everything. */
    float shake(float since, float strength) {
        return since < 0 || since > .45f ? 0 : (float) Math.sin(since * 70) * strength * (1 - since / .45f);
    }

    /** Picks apps at random from those the launcher shows, for a scene to seem to act on. Nothing is done to the apps. */
    void pickApps(int count) {
        if (!icons.isEmpty()) return;
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
                labels.add(name.length() > 16 ? name.substring(0, 15).trim() + "…" : name);
            } catch (RuntimeException unreadable) {
                // An app that went while the list was being read.
            }
        }
        if (!icons.isEmpty()) return;
        try {
            icons.add(packages.getApplicationIcon(context.getPackageName()));
        } catch (RuntimeException | android.content.pm.PackageManager.NameNotFoundException missing) {
            icons.add(new android.graphics.drawable.ColorDrawable(CalculatorLayout.CORAL));
        }
        labels.add(context.getString(TrickArt.app_name));
    }

    // Her words

    private void say(String words, float seconds) {
        saying = names.named(words);
        sayLeft = seconds;
    }

    /** What she is saying, for it to be read out as it changes. */
    String saying() { return saying; }

    private void drawSaying(Canvas canvas) {
        if (saying == null) return;
        text.setTextSize(dp(14));
        text.setTextAlign(Paint.Align.LEFT);
        text.setAlpha(255);
        float pad = dp(11), high = dp(19);
        List<String> lines = new ArrayList<>();
        StringBuilder one = new StringBuilder();
        float room = Math.min(W - dp(40), dp(236));
        for (String word : saying.split(" ")) {
            if (one.length() > 0 && text.measureText(one + " " + word) > room) {
                lines.add(one.toString());
                one.setLength(0);
            }
            if (one.length() > 0) one.append(' ');
            one.append(word);
        }
        if (one.length() > 0) lines.add(one.toString());
        float width = 0;
        for (String each : lines) width = Math.max(width, text.measureText(each));
        float left = clamp(hx - width / 2 - pad, L + dp(8), R - dp(8) - width - pad * 2);
        float height = lines.size() * high + pad * 1.5f;
        float bottom = hy - lift - tall * herScale - dp(10), top = bottom - height;
        if (top < T + dp(6)) {
            top = Math.min(hy + dp(10), B - height - dp(6));
            bottom = top + height;
        }
        top = clamp(top, T + dp(6), B - height - dp(6));
        bottom = top + height;
        box.set(left, top, left + width + pad * 2, bottom);
        float radius = dp(14);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33000000);
        canvas.drawRoundRect(box.left, box.top + dp(2), box.right, box.bottom + dp(3), radius, radius, paint);
        paint.setColor(dark ? 0xFF1D2023 : Color.WHITE);
        canvas.drawRoundRect(box, radius, radius, paint);
        stroke.setColor(dark ? 0xFF34383C : 0xFFE4E4E4);
        stroke.setStrokeWidth(Math.max(1, dp(.75f)));
        canvas.drawRoundRect(box, radius, radius, stroke);
        text.setColor(dark ? 0xFFF3F3F3 : 0xFF292B2A);
        for (int index = 0; index < lines.size(); index++) {
            canvas.drawText(lines.get(index), left + pad, top + pad * .75f + high * (index + .76f), text);
        }
    }
}
