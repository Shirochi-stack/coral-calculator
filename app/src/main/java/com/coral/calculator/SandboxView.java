package com.coral.calculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * The 2.5D sandbox test: a top-down three-quarter view of a tile map (see {@link SandboxWorld}), with Halgakos walking about it, gathering,
 * building a house and trying her weapons on creatures and dummies. Everything is drawn by hand on a canvas, as Plus run's is: the ground
 * first, then everything that stands, flies or walks, in order of how far down the screen its foot is ({@link SandboxDepth}), so that she
 * is behind a tree when she is above it and in front when below.
 */
final class SandboxView extends View {
    private static final String PREFS = "sandbox25d";
    private static final float WALL_H = 1.25f, ROOF_LIFT = 1.4f, SPEED = 4.4f, REACH = 7.5f;
    private static final int INK = 0xFF3B2F2F, PAPER = 0xFFFFF7E6, SOFT = 0xFF8A7B6B, GOOD = 0x9936D26B, BAD = 0x99E5484D;
    private static final int K_NATURAL = 0, K_WALL = 1, K_ROOF = 2, K_FURNITURE = 3, K_PICKUP = 4, K_FOE = 5, K_HER = 6, K_SHOT = 7;
    private static final int N = SandboxWorld.N, E = SandboxWorld.E, S = SandboxWorld.S, W = SandboxWorld.W;

    private final Runnable onClose;
    private final SandboxArt art;
    private final SharedPreferences prefs;
    private final Random random = new Random();
    private SandboxWorld world;
    private final float density;

    // Paints and scratch objects
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint text = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Rect src = new Rect();
    private final RectF dst = new RectF(), box = new RectF();
    private final Path path = new Path();
    private final LightingColorFilter whiteFlash = new LightingColorFilter(0x00000000, 0x00FFFFFF);

    // Her
    private float px = SandboxWorld.SPAWN + .5f, py = SandboxWorld.SPAWN + 2.2f, pvx, pvy;
    private float angle = (float) Math.PI / 2, walkClock, attackT, attackFor = .3f, cooldown, hurtClock, sinceHurt = 9, health = 100;
    private int facing; // 0 down, 1 up, 2 left, 3 right
    private boolean moving, attackHeld, autoAttack;
    private final int[] inv = {30, 15, 10};
    private int weaponIndex = 3, rarity = 1;
    private SandboxCombat.Weapon weapon;
    private float attackAngle;
    private final HashSet<Object> struck = new HashSet<>();

    // Camera
    private float camX = px, camY = py, zoom = 100, userZoom = 1, userZoomTarget = 1;
    private int width, height;
    private float insetL, insetT, insetR, insetB;

    // The mode
    private boolean building, helpOpen, weaponOpen, dirty, roofDirty = true;
    private int buildKind = SandboxWorld.B_WALL;
    private int cursorX, cursorY;
    private boolean cursorFollows = true;
    private int chestX = -1, chestY = -1;
    private int lastPaintX = -1, lastPaintY = -1;
    private float saveClock;
    private String note = "";
    private float noteClock;

    // Things in the world
    private final ArrayList<SandboxCombat.Foe> foes = new ArrayList<>();
    private final ArrayList<Shot> shots = new ArrayList<>();
    private final ArrayList<Pickup> pickups = new ArrayList<>();
    private final ArrayList<Spark> sparks = new ArrayList<>();
    private final ArrayList<Label> labels = new ArrayList<>();
    private final ArrayList<Slash> slashes = new ArrayList<>();
    private final HashMap<Integer, Float> shakes = new HashMap<>();
    private final float[] roofAlpha = new float[SandboxWorld.SIZE * SandboxWorld.SIZE];
    private BitSet roofRegion = new BitSet();
    private int roofTile = -1;
    private final ArrayList<float[]> respawns = new ArrayList<>();

    // Drawing order
    private SandboxDepth.Item[] items = new SandboxDepth.Item[6000];
    private int itemCount;
    private float clock;
    private long lastFrame;

    // Touch
    private final HashMap<Integer, Integer> roles = new HashMap<>();
    private static final int JOY = 1, BUTTON = 2, WORLD = 3, PANEL = 4;
    private int joyPointer = -1, attackPointer = -1;
    private float joyX0, joyY0, joyX, joyY, joyNowX, joyNowY;
    private float pinchFrom;
    private float keyX, keyY;
    private boolean keyLeft, keyRight, keyUp, keyDown;

    // Buttons
    private final RectF attackButton = new RectF(), buildButton = new RectF(), useButton = new RectF(), closeButton = new RectF(), helpButton = new RectF();
    private final RectF weaponButton = new RectF(), zoomIn = new RectF(), zoomOut = new RectF();
    private final RectF[] hotbar = new RectF[SandboxWorld.KINDS];
    private final RectF panel = new RectF(), panelDone = new RectF(), rarityLess = new RectF(), rarityMore = new RectF(), spawnButton = new RectF(), clearButton = new RectF();
    private final RectF[] weaponCells = new RectF[SandboxCombat.weapons()];
    private final RectF chestPut = new RectF(), chestTake = new RectF(), chestPanel = new RectF();

    private static final class Shot {
        float x, y, vx, vy, range, damage, life, tx, ty, total, sx, sy;
        boolean lob, through;
        SandboxCombat.Weapon w;
        float angle;
        final HashSet<SandboxCombat.Foe> hit = new HashSet<>();
    }

    private static final class Pickup { float x, y, vx, vy, age; int item; }
    private static final class Spark { float x, y, z, vx, vy, vz, life, max, size; int colour; }
    private static final class Label { float x, y, age; String text; int colour; float size; }
    private static final class Slash { float x, y, angle, spread, reach, age, life; boolean ring, both; int colour; float burst; }

    SandboxView(Context context, Runnable onClose) {
        super(context);
        this.onClose = onClose;
        density = context.getResources().getDisplayMetrics().density;
        art = new SandboxArt(context);
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        setFocusable(true);
        setFocusableInTouchMode(true);
        for (int i = 0; i < hotbar.length; i++) hotbar[i] = new RectF();
        for (int i = 0; i < weaponCells.length; i++) weaponCells[i] = new RectF();
        for (int i = 0; i < items.length; i++) items[i] = new SandboxDepth.Item();
        text.setTypeface(Typeface.DEFAULT);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        load();
        setWeapon(weaponIndex, rarity);
        if (foes.isEmpty()) spawnStart();
        for (int i = 0; i < roofAlpha.length; i++) roofAlpha[i] = 1;
        roofDirty = true;
    }

    private float dp(float value) { return value * density; }

    // Saving: kept in its own preferences, apart from Plus run's

    private void load() {
        SandboxWorld saved = SandboxWorld.load(prefs.getString("world", null));
        world = saved != null ? saved : new SandboxWorld(20260);
        world.ensureAll();
        px = prefs.getFloat("px", px);
        py = prefs.getFloat("py", py);
        String[] have = prefs.getString("inv", "30,15,10").split(",");
        try {
            for (int i = 0; i < 3; i++) inv[i] = Integer.parseInt(have[i]);
        } catch (RuntimeException broken) {
            inv[0] = 30;
            inv[1] = 15;
            inv[2] = 10;
        }
        weaponIndex = Math.max(0, Math.min(SandboxCombat.weapons() - 1, prefs.getInt("weapon", 3)));
        rarity = Math.max(0, Math.min(5, prefs.getInt("rarity", 1)));
        helpOpen = !prefs.getBoolean("helped", false);
        camX = px;
        camY = py;
        if (world.solid((int) px, (int) py)) {
            px = SandboxWorld.SPAWN + .5f;
            py = SandboxWorld.SPAWN + 2.2f;
        }
    }

    void save() {
        prefs.edit().putString("world", world.save()).putFloat("px", px).putFloat("py", py).putString("inv", inv[0] + "," + inv[1] + "," + inv[2])
                .putInt("weapon", weaponIndex).putInt("rarity", rarity).putBoolean("helped", !helpOpen || prefs.getBoolean("helped", false)).apply();
        dirty = false;
    }

    void pause() {
        if (dirty) save();
    }

    void resume() {
        lastFrame = 0;
        postInvalidateOnAnimation();
    }

    /** A fresh world, for a test run (see the intent extra "fresh"). */
    void fresh() {
        world = new SandboxWorld(20260);
        world.ensureAll();
        inv[0] = 30;
        inv[1] = 15;
        inv[2] = 10;
        px = SandboxWorld.SPAWN + .5f;
        py = SandboxWorld.SPAWN + 2.2f;
        camX = px;
        camY = py;
        roofDirty = true;
        dirty = true;
    }

    // Test scenes: reached by intent extras, for taking pictures

    void demo(String scene, int weapon, int rarityGiven, float zoomGiven, float atX, float atY, boolean attack, boolean fightScene) {
        helpOpen = false;
        if (scene != null) {
            switch (scene) {
                case "house": buildHouse(SandboxWorld.SPAWN - 3, SandboxWorld.SPAWN - 7, 6, 5); px = SandboxWorld.SPAWN; py = SandboxWorld.SPAWN - 4.4f; break;
                case "frame":
                    buildFrame(SandboxWorld.SPAWN - 3, SandboxWorld.SPAWN - 7, 6, 5);
                    px = SandboxWorld.SPAWN + 1.5f;
                    py = SandboxWorld.SPAWN - 1.2f;
                    building = true;
                    buildKind = SandboxWorld.B_WALL;
                    cursorX = SandboxWorld.SPAWN + 3;
                    cursorY = SandboxWorld.SPAWN - 3;
                    cursorFollows = false;
                    break;
                case "tree": {
                    int[] tree = nearestOf(SandboxWorld.TREE);
                    px = tree[0] + .5f;
                    py = tree[1] - .45f;
                    break;
                }
                case "treefront": {
                    int[] tree = nearestOf(SandboxWorld.TREE);
                    px = tree[0] + .5f;
                    py = tree[1] + 1.35f;
                    facing = 1;
                    break;
                }
                default: break;
            }
        }
        if (atX > 0) {
            px = atX;
            py = atY;
        }
        if (weapon >= 0) setWeapon(weapon, rarityGiven >= 0 ? rarityGiven : rarity);
        if (zoomGiven > 0) userZoom = userZoomTarget = zoomGiven;
        if (fightScene) {
            foes.clear();
            foes.add(new SandboxCombat.Foe(SandboxCombat.DUMMY, px + 2.2f, py + .2f));
            foes.add(new SandboxCombat.Foe(SandboxCombat.WOLF, px + 3.3f, py - 1.4f));
            foes.add(new SandboxCombat.Foe(SandboxCombat.DUMMY, px + 4.4f, py + 1.6f));
            facing = 3;
            angle = 0;
        }
        autoAttack = attack;
        camX = px;
        camY = py;
        roofDirty = true;
        dirty = true;
    }

    private int[] nearestOf(byte what) {
        int best = -1;
        float bestDistance = 1e9f;
        for (int y = 1; y < SandboxWorld.SIZE - 1; y++) {
            for (int x = 1; x < SandboxWorld.SIZE - 1; x++) {
                if (world.naturalOf(x, y) != what || world.solid(x, y - 1) || world.solid(x, y + 1) || world.solid(x, y + 2)) continue;
                float dx = x - SandboxWorld.SPAWN, dy = y - SandboxWorld.SPAWN;
                float d = dx * dx + dy * dy;
                if (d < bestDistance) {
                    bestDistance = d;
                    best = y * SandboxWorld.SIZE + x;
                }
            }
        }
        return best < 0 ? new int[]{SandboxWorld.SPAWN, SandboxWorld.SPAWN} : new int[]{best % SandboxWorld.SIZE, best / SandboxWorld.SIZE};
    }

    private void clearSite(int x0, int y0, int w, int h) {
        for (int y = y0; y < y0 + h; y++) for (int x = x0; x < x0 + w; x++) world.natural[SandboxWorld.at(x, y)] = SandboxWorld.NONE;
    }

    private void buildFrame(int x0, int y0, int w, int h) {
        clearSite(x0, y0, w, h);
        for (int y = y0; y < y0 + h; y++) for (int x = x0; x < x0 + w; x++) world.place(SandboxWorld.FLOOR, x, y);
        for (int x = x0; x < x0 + w; x++) world.place(SandboxWorld.B_WALL, x, y0 + h - 1);
        for (int y = y0; y < y0 + h; y++) world.place(SandboxWorld.B_WALL, x0, y);
        for (int y = y0 + 1; y < y0 + h - 2; y++) world.place(SandboxWorld.B_WALL, x0 + w - 1, y);
    }

    private void buildHouse(int x0, int y0, int w, int h) {
        clearSite(x0, y0, w, h);
        for (int y = y0; y < y0 + h; y++) for (int x = x0; x < x0 + w; x++) world.place(SandboxWorld.FLOOR, x, y);
        for (int x = x0; x < x0 + w; x++) {
            world.place(SandboxWorld.B_WALL, x, y0);
            if (x != x0 + w / 2) world.place(SandboxWorld.B_WALL, x, y0 + h - 1);
        }
        world.place(SandboxWorld.B_DOOR, x0 + w / 2, y0 + h - 1);
        for (int y = y0 + 1; y < y0 + h - 1; y++) {
            world.place(SandboxWorld.B_WALL, x0, y);
            world.place(SandboxWorld.B_WALL, x0 + w - 1, y);
        }
        world.place(SandboxWorld.B_BED, x0 + 1, y0 + 1);
        world.place(SandboxWorld.B_TABLE, x0 + w - 2, y0 + 2);
        world.place(SandboxWorld.B_CHEST, x0 + w - 2, y0 + 1);
        world.place(SandboxWorld.B_LAMP, x0 + 1, y0 + h - 2);
        for (int y = y0; y < y0 + h; y++) for (int x = x0; x < x0 + w; x++) world.place(SandboxWorld.ROOF, x, y);
        int[] chest = world.chest(x0 + w - 2, y0 + 1);
        if (chest != null) chest[SandboxWorld.PAPER] = 3;
    }

    // Weapons

    private void setWeapon(int index, int rare) {
        weaponIndex = (index % SandboxCombat.weapons() + SandboxCombat.weapons()) % SandboxCombat.weapons();
        rarity = Math.max(0, Math.min(RunLoot.ROSE, rare));
        weapon = SandboxCombat.weapon(weaponIndex, rarity);
        dirty = true;
    }

    private void spawnStart() {
        foes.clear();
        int c = SandboxWorld.SPAWN;
        foes.add(new SandboxCombat.Foe(SandboxCombat.DUMMY, c - 3.5f, c + 3.5f));
        foes.add(new SandboxCombat.Foe(SandboxCombat.DUMMY, c, c + 5.2f));
        foes.add(new SandboxCombat.Foe(SandboxCombat.DUMMY, c + 3.5f, c + 3.5f));
        foes.add(new SandboxCombat.Foe(SandboxCombat.SLIME, c + 8, c + 1));
        foes.add(new SandboxCombat.Foe(SandboxCombat.BAT, c - 8, c + 4));
        foes.add(new SandboxCombat.Foe(SandboxCombat.WOLF, c + 2, c + 9));
    }

    private void spawnGroup() {
        for (int i = 0; i < 4; i++) {
            float a = random.nextFloat() * 6.28f, d = 5 + random.nextFloat() * 3;
            float x = px + (float) Math.cos(a) * d, y = py + (float) Math.sin(a) * d;
            if (blocked(x, y, .4f)) continue;
            foes.add(new SandboxCombat.Foe(i % 3, x, y));
        }
        say("Foes spawned");
    }

    private void say(String message) {
        note = message;
        noteClock = 2.4f;
    }

    // Frame

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        layout();
    }

    @Override public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            insetL = bars.left;
            insetT = bars.top;
            insetR = bars.right;
            insetB = bars.bottom;
        } else {
            insetL = insets.getSystemWindowInsetLeft();
            insetT = insets.getSystemWindowInsetTop();
            insetR = insets.getSystemWindowInsetRight();
            insetB = insets.getSystemWindowInsetBottom();
        }
        layout();
        return insets;
    }

    private void layout() {
        if (width == 0) return;
        float l = insetL + dp(8), t = insetT + dp(8), r = width - insetR - dp(8), b = height - insetB - dp(8);
        float big = dp(44), small = dp(31);
        attackButton.set(r - dp(80) - big, b - dp(84) - big, r - dp(80) + big, b - dp(84) + big);
        attackButton.offset(dp(36), dp(26));
        buildButton.set(attackButton.left - dp(8) - 2 * small, attackButton.centerY() - small + dp(18), attackButton.left - dp(8), attackButton.centerY() + small + dp(18));
        useButton.set(attackButton.centerX() - small + dp(2), attackButton.top - dp(10) - 2 * small, attackButton.centerX() + small + dp(2), attackButton.top - dp(10));
        float cell = dp(40), gap = dp(4);
        float x = r - cell;
        RectF[] top = {closeButton, helpButton, zoomOut, zoomIn, weaponButton};
        for (RectF button : top) {
            button.set(x, t, x + cell, t + cell);
            x -= cell + gap;
        }
        // The build bar: under the top row, as wide as fits.
        float each = Math.min(dp(42), (width - insetL - insetR - dp(16)) / SandboxWorld.KINDS), barW = each * SandboxWorld.KINDS;
        float barX = (width - barW) / 2f, barY = t + cell + dp(24);
        for (int i = 0; i < SandboxWorld.KINDS; i++) hotbar[i].set(barX + i * each + dp(2), barY, barX + (i + 1) * each - dp(2), barY + each - dp(2));
        // Panels
        float pw = Math.min(width - dp(16) - insetL - insetR, dp(560)), ph = Math.min(height - dp(16) - insetT - insetB, dp(500));
        panel.set((width - pw) / 2f, (height - ph) / 2f, (width + pw) / 2f, (height + ph) / 2f);
        boolean wide = pw > ph * 1.25f;
        float gridW = wide ? pw * .5f : pw - dp(24);
        int columns = wide ? 4 : 4;
        float cellW = gridW / columns, cellH = wide ? (ph - dp(60)) / 4f : Math.min(dp(66), (ph - dp(230)) / 4f);
        for (int i = 0; i < weaponCells.length; i++) {
            float cx = panel.left + dp(12) + (i % columns) * cellW, cy = panel.top + dp(40) + (i / columns) * cellH;
            weaponCells[i].set(cx + dp(2), cy + dp(2), cx + cellW - dp(2), cy + cellH - dp(2));
        }
        float dx = wide ? panel.left + pw * .5f + dp(18) : panel.left + dp(12), dw = wide ? pw * .5f - dp(30) : pw - dp(24);
        float rowY = wide ? panel.bottom - dp(112) : panel.bottom - dp(112);
        rarityLess.set(dx, rowY, dx + dp(40), rowY + dp(34));
        rarityMore.set(dx + dw - dp(40), rowY, dx + dw, rowY + dp(34));
        spawnButton.set(dx, panel.bottom - dp(66), dx + dw / 2f - dp(3), panel.bottom - dp(34));
        clearButton.set(dx + dw / 2f + dp(3), panel.bottom - dp(66), dx + dw, panel.bottom - dp(34));
        panelDone.set(dx, panel.bottom - dp(30), dx + dw, panel.bottom - dp(6));
        float cw = Math.min(width - dp(16) - insetL - insetR, dp(340)), ch = dp(170);
        chestPanel.set(width / 2f - cw / 2, height / 2f - ch / 2, width / 2f + cw / 2, height / 2f + ch / 2);
        chestPut.set(chestPanel.left + dp(10), chestPanel.bottom - dp(52), chestPanel.centerX() - dp(4), chestPanel.bottom - dp(14));
        chestTake.set(chestPanel.centerX() + dp(4), chestPanel.bottom - dp(52), chestPanel.right - dp(10), chestPanel.bottom - dp(14));
        zoom = Math.min(width, height) / 7.4f * userZoom;
    }

    @Override protected void onDraw(Canvas canvas) {
        long now = System.nanoTime();
        float dt = lastFrame == 0 ? 0 : Math.min(.05f, (now - lastFrame) / 1e9f);
        lastFrame = now;
        clock += dt;
        update(dt);
        render(canvas);
        postInvalidateOnAnimation();
    }

    // Rules

    private boolean modal() { return helpOpen || weaponOpen || chestX >= 0; }

    private void update(float dt) {
        if (modal()) dt = 0;
        hurtClock -= dt;
        // Moving
        float ix = joyX + keyX, iy = joyY + keyY;
        float length = (float) Math.hypot(ix, iy);
        if (length > 1) {
            ix /= length;
            iy /= length;
            length = 1;
        }
        boolean was = moving;
        moving = length > .12f;
        if (moving) {
            if (!was) cursorFollows = true;
            angle = (float) Math.atan2(iy, ix);
            facing = Math.abs(ix) > Math.abs(iy) * .8f ? (ix < 0 ? 2 : 3) : (iy < 0 ? 1 : 0);
            walkClock += dt * (6 + 3 * length);
        }
        float speed = SPEED * (building ? .9f : 1);
        pvx *= (float) Math.exp(-dt * 9);
        pvy *= (float) Math.exp(-dt * 9);
        float mx = (moving ? ix * speed : 0) + pvx, my = (moving ? iy * speed : 0) + pvy;
        moveBy(dt * mx, dt * my);
        // Attacking and building
        attackT = Math.max(0, attackT - dt);
        cooldown -= dt;
        if ((attackHeld || autoAttack) && cooldown <= 0 && !modal()) {
            if (building) placeAtCursor();
            else attack();
            if (building) cooldown = .16f;
        }
        updateCursor();
        // Creatures, shots, pickups
        sinceHurt += dt;
        if (sinceHurt > 4 && health < 100) health = Math.min(100, health + 8 * dt);
        updateFoes(dt);
        updateShots(dt);
        updatePickups(dt);
        for (int i = sparks.size() - 1; i >= 0; i--) {
            Spark s = sparks.get(i);
            s.life -= dt;
            s.x += s.vx * dt;
            s.y += s.vy * dt;
            s.z += s.vz * dt;
            s.vz -= 9 * dt;
            if (s.z < 0) {
                s.z = 0;
                s.vz *= -.3f;
            }
            if (s.life <= 0) sparks.remove(i);
        }
        for (int i = labels.size() - 1; i >= 0; i--) {
            Label label = labels.get(i);
            label.age += dt;
            if (label.age > 1.1f) labels.remove(i);
        }
        for (int i = slashes.size() - 1; i >= 0; i--) {
            slashes.get(i).age += dt;
            if (slashes.get(i).age > slashes.get(i).life) slashes.remove(i);
        }
        if (!shakes.isEmpty()) {
            ArrayList<Integer> done = new ArrayList<>();
            for (java.util.Map.Entry<Integer, Float> e : shakes.entrySet()) {
                float left = e.getValue() - dt;
                if (left <= 0) done.add(e.getKey());
                else e.setValue(left);
            }
            for (int key : done) shakes.remove(key);
        }
        noteClock -= dt;
        // Doors open to whoever is near
        float[] xs = new float[foes.size() + 1], ys = new float[foes.size() + 1];
        int count = 0;
        xs[count] = px;
        ys[count++] = py;
        for (SandboxCombat.Foe foe : foes) {
            xs[count] = foe.x;
            ys[count++] = foe.y;
        }
        world.updateDoors(xs, ys, count);
        // The roof over her fades
        int tile = SandboxWorld.at(Math.max(0, Math.min(SandboxWorld.SIZE - 1, (int) Math.floor(px))), Math.max(0, Math.min(SandboxWorld.SIZE - 1, (int) Math.floor(py))));
        if (tile != roofTile || roofDirty) {
            roofTile = tile;
            roofRegion = world.roofOver(tile % SandboxWorld.SIZE, tile / SandboxWorld.SIZE);
            roofDirty = false;
        }
        // Camera and zoom
        userZoom += (userZoomTarget - userZoom) * Math.min(1, dt * 7);
        float base = Math.min(width, height) / 7.4f;
        zoom = base * userZoom;
        float follow = Math.min(1, dt * 6);
        camX += (px - camX) * follow;
        camY += (py - .5f - camY) * follow;
        saveClock += dt;
        if (saveClock > 15) {
            saveClock = 0;
            if (dirty) save();
        }
        // Death is only a rest
        if (health <= 0) {
            health = 100;
            px = SandboxWorld.SPAWN + .5f;
            py = SandboxWorld.SPAWN + 2.2f;
            say("Knocked out. Back at the start.");
        }
    }

    private boolean blocked(float x, float y, float r) {
        for (int ty = (int) Math.floor(y - r); ty <= (int) Math.floor(y + r); ty++) {
            for (int tx = (int) Math.floor(x - r); tx <= (int) Math.floor(x + r); tx++) {
                if (!world.solid(tx, ty)) continue;
                float nx = Math.max(tx, Math.min(x, tx + 1)), ny = Math.max(ty, Math.min(y, ty + 1));
                if ((x - nx) * (x - nx) + (y - ny) * (y - ny) < r * r) return true;
            }
        }
        return false;
    }

    private void moveBy(float dx, float dy) {
        if (!blocked(px + dx, py, .3f)) px += dx;
        if (!blocked(px, py + dy, .3f)) py += dy;
    }

    private void updateCursor() {
        if (!building || !cursorFollows) return;
        cursorX = (int) Math.floor(px + Math.cos(angle) * 1.5f);
        cursorY = (int) Math.floor(py - .2f + Math.sin(angle) * 1.5f);
    }

    private void updateFoes(float dt) {
        for (int i = foes.size() - 1; i >= 0; i--) {
            SandboxCombat.Foe f = foes.get(i);
            if (f.dead) {
                foes.remove(i);
                for (int s = 0; s < 10; s++) spark(f.x, f.y, .5f, 0xFF3B2F2F, 1.4f);
                respawns.add(new float[]{f.kind, 7});
                continue;
            }
            f.flash -= dt;
            f.sinceHit += dt;
            f.bite -= dt;
            f.vx *= (float) Math.exp(-dt * 6);
            f.vy *= (float) Math.exp(-dt * 6);
            float tx = 0, ty = 0;
            float dx = px - f.x, dy = py - f.y, d = (float) Math.hypot(dx, dy);
            if (f.kind != SandboxCombat.DUMMY) {
                if (d < 7 && f.sinceHit > .12f) {
                    tx = dx / Math.max(.01f, d) * f.pace();
                    ty = dy / Math.max(.01f, d) * f.pace();
                } else {
                    f.wander -= dt;
                    if (f.wander <= 0) {
                        f.wander = 1 + random.nextFloat() * 2;
                        f.vx += (random.nextFloat() - .5f) * 3;
                        f.vy += (random.nextFloat() - .5f) * 3;
                    }
                }
                if (Math.abs(tx) > .05f) f.face = tx < 0 ? -1 : 1;
                float mx = (tx + f.vx) * dt, my = (ty + f.vy) * dt;
                if (!blocked(f.x + mx, f.y, f.radius())) f.x += mx;
                if (!blocked(f.x, f.y + my, f.radius())) f.y += my;
                if (d < f.radius() + .45f && f.bite <= 0 && sinceHurt > .0f) {
                    f.bite = 1;
                    health -= f.damage();
                    sinceHurt = 0;
                    hurtClock = .25f;
                    pvx += dx / Math.max(.1f, d) * -.0f + (px - f.x) / Math.max(.1f, d) * 4;
                    pvy += (py - f.y) / Math.max(.1f, d) * 4;
                    label(px, py - 1.5f, "-" + Math.round(f.damage()), 0xFFE5484D, 1);
                }
            } else {
                // A dummy that was hit stands back up after a moment and mends.
                f.x += f.vx * dt;
                f.y += f.vy * dt;
                if (blocked(f.x, f.y, f.radius())) {
                    f.x -= f.vx * dt;
                    f.y -= f.vy * dt;
                }
                if (f.sinceHit > 3 && f.health < f.max) f.health = Math.min(f.max, f.health + 60 * dt);
            }
        }
        for (int i = respawns.size() - 1; i >= 0; i--) {
            respawns.get(i)[1] -= dt;
            if (respawns.get(i)[1] > 0) continue;
            float a = random.nextFloat() * 6.28f, d = 9 + random.nextFloat() * 4;
            float x = px + (float) Math.cos(a) * d, y = py + (float) Math.sin(a) * d;
            if (!blocked(x, y, .5f)) {
                foes.add(new SandboxCombat.Foe((int) respawns.get(i)[0], x, y));
                respawns.remove(i);
            } else {
                respawns.get(i)[1] = .5f;
            }
        }
    }

    private void attack() {
        SandboxCombat.Weapon w = weapon;
        cooldown = w.rest;
        attackFor = Math.max(.18f, Math.min(.34f, w.rest * .85f));
        attackT = attackFor;
        // Ranged weapons lean toward the nearest creature in front of her; a swing goes where she faces.
        float aim = angle;
        SandboxCombat.Foe best = null;
        float bestScore = 1e9f;
        for (SandboxCombat.Foe f : foes) {
            float dx = f.x - px, dy = f.y - py, d = (float) Math.hypot(dx, dy);
            float off = Math.abs(SandboxCombat.wrap((float) Math.atan2(dy, dx) - angle));
            if (d > w.reach + 2.5f || off > .9f || d < .01f) continue;
            if (d + off * 3 < bestScore) {
                bestScore = d + off * 3;
                best = f;
            }
        }
        if (best != null) aim = (float) Math.atan2(best.y - py, best.x - px);
        angle = aim;
        attackAngle = aim;
        facing = Math.abs(Math.cos(aim)) > Math.abs(Math.sin(aim)) * .8f ? (Math.cos(aim) < 0 ? 2 : 3) : (Math.sin(aim) < 0 ? 1 : 0);
        float cx = px, cy = py - .15f;
        struck.clear();
        switch (w.shape) {
            case SandboxCombat.ARC:
            case SandboxCombat.RING: {
                Slash slash = new Slash();
                slash.x = cx;
                slash.y = cy;
                slash.angle = aim;
                slash.spread = w.spread;
                slash.reach = w.reach;
                slash.life = Math.max(.2f, attackFor);
                slash.ring = w.shape == SandboxCombat.RING;
                slash.burst = w.burst;
                slash.both = w.both;
                slash.colour = w.kind == RunArms.KATANA ? 0xFFBFE9FF : w.kind == RunArms.SCYTHE ? 0xFFC9A6FF : 0xFFFFFFFF;
                slashes.add(slash);
                int landed = 0;
                for (SandboxCombat.Foe f : foes) {
                    if (f.dead || !SandboxCombat.reaches(w, cx, cy, aim, f.x, f.y, f.radius())) continue;
                    landed += hitFoe(f, w.damage, f.x - px, f.y - py, w.knock) ? 1 : 0;
                }
                gather(w, cx, cy, aim);
                if (landed > 0) buzz(landed);
                break;
            }
            case SandboxCombat.SHOT:
            case SandboxCombat.FAN: {
                for (int i = 0; i < w.shots; i++) {
                    float a = aim + (w.shots > 1 ? (i - (w.shots - 1) / 2f) * w.spread : (random.nextFloat() - .5f) * w.spread);
                    Shot shot = new Shot();
                    shot.x = shot.sx = cx + (float) Math.cos(a) * .6f;
                    shot.y = shot.sy = cy + (float) Math.sin(a) * .6f - .35f;
                    shot.vx = (float) Math.cos(a) * w.speed;
                    shot.vy = (float) Math.sin(a) * w.speed;
                    shot.range = w.reach;
                    shot.damage = w.damage;
                    shot.through = w.through;
                    shot.w = w;
                    shot.angle = a;
                    shots.add(shot);
                }
                break;
            }
            default: {
                Shot shot = new Shot();
                shot.lob = true;
                shot.w = w;
                shot.sx = shot.x = cx + (float) Math.cos(aim) * .5f;
                shot.sy = shot.y = cy + (float) Math.sin(aim) * .5f;
                float reach = Math.max(2.2f, Math.min(w.reach, 7.5f));
                shot.tx = cx + (float) Math.cos(aim) * reach;
                shot.ty = cy + (float) Math.sin(aim) * reach;
                shot.total = Math.max(.35f, reach / w.speed);
                shot.damage = w.damage;
                shot.angle = aim;
                shots.add(shot);
            }
        }
    }

    private void buzz(int hits) {
        performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private boolean hitFoe(SandboxCombat.Foe f, float damage, float dx, float dy, float knock) {
        float dealt = SandboxCombat.hit(f, damage, dx, dy, knock);
        if (dealt <= 0) return false;
        label(f.x, f.y - 1.3f, String.valueOf(Math.round(dealt)), f.kind == SandboxCombat.DUMMY ? 0xFFFFD34D : 0xFFFFFFFF, 1);
        for (int i = 0; i < 6; i++) spark(f.x, f.y, .6f, f.kind == SandboxCombat.SLIME ? 0xFF3B2F2F : 0xFFFFE9A8, 1);
        return true;
    }

    /** What a swing does to trees, rocks and the rest that grow: the same blow that hurts a creature breaks them, and they give what they hold. */
    private void gather(SandboxCombat.Weapon w, float cx, float cy, float aim) {
        float far = w.reach + 1;
        for (int ty = (int) Math.floor(cy - far); ty <= (int) Math.floor(cy + far); ty++) {
            for (int tx = (int) Math.floor(cx - far); tx <= (int) Math.floor(cx + far); tx++) {
                byte what = world.naturalOf(tx, ty);
                if (what == SandboxWorld.NONE) continue;
                if (!SandboxCombat.reaches(w, cx, cy, aim, tx + .5f, ty + .5f, .45f)) continue;
                int[] gave = world.hurtNatural(tx, ty, w.damage, random);
                shakes.put(ty * SandboxWorld.SIZE + tx, .25f);
                for (int i = 0; i < 4; i++) spark(tx + .5f, ty + .5f, .8f, what == SandboxWorld.ROCK ? 0xFFB9B2A4 : what == SandboxWorld.TREE ? 0xFF8BC34A : 0xFF9ACD50, 1.2f);
                dirty = true;
                if (gave != null) {
                    for (int item = 0; item < SandboxWorld.ITEMS; item++) {
                        for (int n = 0; n < gave[item]; n++) {
                            Pickup p = new Pickup();
                            p.x = tx + .5f;
                            p.y = ty + .6f;
                            float a = random.nextFloat() * 6.28f;
                            p.vx = (float) Math.cos(a) * 2.2f;
                            p.vy = (float) Math.sin(a) * 2.2f;
                            p.item = item;
                            pickups.add(p);
                        }
                    }
                    roofDirty = true;
                }
            }
        }
    }

    private void updateShots(float dt) {
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot s = shots.get(i);
            if (s.lob) {
                s.life += dt;
                float t = Math.min(1, s.life / s.total);
                s.x = s.sx + (s.tx - s.sx) * t;
                s.y = s.sy + (s.ty - s.sy) * t;
                if (t >= 1) {
                    burst(s);
                    shots.remove(i);
                }
                continue;
            }
            float step = dt * (float) Math.hypot(s.vx, s.vy);
            s.x += s.vx * dt;
            s.y += s.vy * dt;
            s.life += step;
            boolean stop = s.life >= s.range;
            for (SandboxCombat.Foe f : foes) {
                if (f.dead || s.hit.contains(f)) continue;
                if (Math.hypot(f.x - s.x, f.y + .35f - s.y) < f.radius() + .3f) {
                    s.hit.add(f);
                    hitFoe(f, s.damage, s.vx, s.vy, s.w.knock);
                    if (!s.through) stop = true;
                }
            }
            if (world.solid((int) Math.floor(s.x), (int) Math.floor(s.y + .35f))) {
                stop = true;
                for (int n = 0; n < 4; n++) spark(s.x, s.y + .35f, .5f, s.w.colour, 1);
            }
            if (stop) shots.remove(i);
        }
    }

    private void burst(Shot s) {
        Slash slash = new Slash();
        slash.x = s.x;
        slash.y = s.y;
        slash.ring = true;
        slash.burst = s.w.burst;
        slash.reach = 0;
        slash.life = .35f;
        slash.colour = 0xFFFFE9A8;
        slashes.add(slash);
        for (SandboxCombat.Foe f : foes) {
            if (!f.dead && Math.hypot(f.x - s.x, f.y - s.y) < s.w.burst + f.radius()) hitFoe(f, s.damage, f.x - s.x, f.y - s.y, s.w.knock + 1);
        }
        for (int n = 0; n < 12; n++) spark(s.x, s.y, .3f, 0xFFFFE9A8, 2.2f);
    }

    private void updatePickups(float dt) {
        for (int i = pickups.size() - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);
            p.age += dt;
            p.vx *= (float) Math.exp(-dt * 4);
            p.vy *= (float) Math.exp(-dt * 4);
            float dx = px - p.x, dy = py - p.y, d = (float) Math.hypot(dx, dy);
            if (p.age > .5f && d < 2.6f) {
                p.vx += dx / Math.max(.1f, d) * 22 * dt;
                p.vy += dy / Math.max(.1f, d) * 22 * dt;
            }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            if (p.age > .4f && d < .7f) {
                inv[p.item]++;
                dirty = true;
                label(px, py - 1.6f, "+1 " + SandboxWorld.ITEM_NAMES[p.item], ITEM_COLOURS[p.item], .8f);
                pickups.remove(i);
            }
        }
    }

    private static final int[] ITEM_COLOURS = {0xFFD9904A, 0xFFFFF3C4, 0xFFC9C5BD};

    private void spark(float x, float y, float z, int colour, float speed) {
        if (sparks.size() > 300) return;
        Spark s = new Spark();
        s.x = x;
        s.y = y;
        s.z = z;
        float a = random.nextFloat() * 6.28f, v = (.5f + random.nextFloat()) * speed;
        s.vx = (float) Math.cos(a) * v;
        s.vy = (float) Math.sin(a) * v * .6f;
        s.vz = 2 + random.nextFloat() * 2;
        s.life = s.max = .4f + random.nextFloat() * .3f;
        s.size = .05f + random.nextFloat() * .06f;
        s.colour = colour;
        sparks.add(s);
    }

    private void label(float x, float y, String message, int colour, float size) {
        Label label = new Label();
        label.x = x + (random.nextFloat() - .5f) * .3f;
        label.y = y;
        label.text = message;
        label.colour = colour;
        label.size = size;
        labels.add(label);
    }

    // Building

    private void placeAtCursor() {
        placeAt(cursorX, cursorY);
    }

    private void placeAt(int x, int y) {
        if (!SandboxWorld.inside(x, y)) return;
        float dx = px - (x + .5f), dy = py - (y + .5f);
        if (dx * dx + dy * dy > REACH * REACH) {
            say("Too far away");
            return;
        }
        if (buildKind == SandboxWorld.DEMOLISH) {
            int[] refund = new int[SandboxWorld.ITEMS];
            int what = world.demolish(x, y, refund);
            if (what >= 0) {
                for (int i = 0; i < SandboxWorld.ITEMS; i++) inv[i] += refund[i];
                label(x + .5f, y - .2f, "Refund", 0xFF6BCB77, 1);
                for (int n = 0; n < 8; n++) spark(x + .5f, y + .5f, .5f, 0xFFD9B38C, 1.6f);
                dirty = roofDirty = true;
            }
            return;
        }
        if (!world.canPlace(buildKind, x, y)) return;
        if (!SandboxWorld.affords(inv, buildKind)) {
            say("Not enough materials");
            return;
        }
        // A solid thing cannot be put where she or a creature stands.
        boolean solidKind = buildKind == SandboxWorld.B_WALL || buildKind >= SandboxWorld.B_BED;
        if (solidKind) {
            if (px > x - .3f && px < x + 1.3f && py > y - .3f && py < y + 1.3f) {
                say("You're standing there");
                return;
            }
            for (SandboxCombat.Foe f : foes) if (Math.abs(f.x - (x + .5f)) < .8f && Math.abs(f.y - (y + .5f)) < .8f) return;
        }
        world.place(buildKind, x, y);
        int[] cost = SandboxWorld.cost(buildKind);
        for (int i = 0; i < SandboxWorld.ITEMS; i++) inv[i] -= cost[i];
        for (int n = 0; n < 5; n++) spark(x + .5f, y + .5f, .3f, 0xFFFFE9A8, 1.2f);
        dirty = roofDirty = true;
    }

    private void interact() {
        if (building) return;
        int bx = (int) Math.floor(px), by = (int) Math.floor(py);
        int best = -1;
        float bestDistance = 2.1f;
        for (int y = by - 2; y <= by + 2; y++) {
            for (int x = bx - 2; x <= bx + 2; x++) {
                if (!SandboxWorld.inside(x, y) || world.furniture[SandboxWorld.at(x, y)] == SandboxWorld.NO_FURNITURE) continue;
                float d = (float) Math.hypot(px - (x + .5f), py - (y + .5f));
                if (d < bestDistance) {
                    bestDistance = d;
                    best = SandboxWorld.at(x, y);
                }
            }
        }
        if (best < 0) {
            say("Nothing to use here");
            return;
        }
        int x = best % SandboxWorld.SIZE, y = best / SandboxWorld.SIZE;
        switch (world.furniture[best]) {
            case SandboxWorld.CHEST: chestX = x; chestY = y; break;
            case SandboxWorld.BED:
                health = 100;
                say("Zzz... rested. Health full.");
                label(x + .5f, y - .6f, "Zzz", 0xFFB7A6FF, 1.2f);
                break;
            case SandboxWorld.LAMP:
                world.toggleLamp(x, y);
                dirty = true;
                say(world.lit(x, y) ? "Lamp lit" : "Lamp out");
                break;
            default: say("A sturdy table"); break;
        }
    }

    // Input

    boolean back() {
        if (helpOpen) { closeHelp(); return true; }
        if (weaponOpen) { weaponOpen = false; return true; }
        if (chestX >= 0) { chestX = -1; return true; }
        if (building) { building = false; return true; }
        pause();
        return false;
    }

    private void closeHelp() {
        helpOpen = false;
        prefs.edit().putBoolean("helped", true).apply();
    }

    boolean key(int code, boolean down) {
        switch (code) {
            case KeyEvent.KEYCODE_W: case KeyEvent.KEYCODE_DPAD_UP: keyUp = down; break;
            case KeyEvent.KEYCODE_S: case KeyEvent.KEYCODE_DPAD_DOWN: keyDown = down; break;
            case KeyEvent.KEYCODE_A: case KeyEvent.KEYCODE_DPAD_LEFT: keyLeft = down; break;
            case KeyEvent.KEYCODE_D: case KeyEvent.KEYCODE_DPAD_RIGHT: keyRight = down; break;
            case KeyEvent.KEYCODE_SPACE: case KeyEvent.KEYCODE_J: case KeyEvent.KEYCODE_F:
                if (helpOpen && down) { closeHelp(); return true; }
                attackHeld = down;
                break;
            case KeyEvent.KEYCODE_B: if (down) { building = !building; cursorFollows = true; } return true;
            case KeyEvent.KEYCODE_E: if (down) interact(); return true;
            case KeyEvent.KEYCODE_Q: if (down) { setWeapon(weaponIndex + 1, rarity); say(weapon.name); } return true;
            case KeyEvent.KEYCODE_R: if (down) { setWeapon(weaponIndex, (rarity + 1) % 6); say(RunLoot.RARITIES[rarity] + " " + weapon.name); } return true;
            case KeyEvent.KEYCODE_TAB: if (down) weaponOpen = !weaponOpen; return true;
            case KeyEvent.KEYCODE_H: case KeyEvent.KEYCODE_SLASH: if (down) helpOpen = !helpOpen; return true;
            case KeyEvent.KEYCODE_Z: case KeyEvent.KEYCODE_EQUALS: case KeyEvent.KEYCODE_PLUS: if (down) userZoomTarget = Math.min(2.2f, userZoomTarget * 1.25f); return true;
            case KeyEvent.KEYCODE_X: case KeyEvent.KEYCODE_MINUS: if (down) userZoomTarget = Math.max(.45f, userZoomTarget / 1.25f); return true;
            case KeyEvent.KEYCODE_ESCAPE: if (down && !back()) onClose.run(); return true;
            default:
                if (code >= KeyEvent.KEYCODE_1 && code <= KeyEvent.KEYCODE_9) {
                    if (down) {
                        buildKind = code - KeyEvent.KEYCODE_1;
                        building = true;
                    }
                    return true;
                }
                return false;
        }
        keyX = (keyRight ? 1 : 0) - (keyLeft ? 1 : 0);
        keyY = (keyDown ? 1 : 0) - (keyUp ? 1 : 0);
        return true;
    }

    @Override public boolean onGenericMotionEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_SCROLL) {
            userZoomTarget = Math.max(.45f, Math.min(2.2f, userZoomTarget * (event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0 ? 1.15f : 1 / 1.15f)));
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    private int tileAt(float sx, float sy, boolean wantX) {
        float wx = camX + (sx - width / 2f) / zoom, wy = camY + (sy - height / 2f) / zoom;
        return (int) Math.floor(wantX ? wx : wy);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int index = event.getActionIndex();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchDown(event.getPointerId(index), event.getX(index), event.getY(index));
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < event.getPointerCount(); i++) touchMove(event.getPointerId(i), event.getX(i), event.getY(i), event);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
            if (action == MotionEvent.ACTION_CANCEL) {
                for (int i = 0; i < event.getPointerCount(); i++) touchUp(event.getPointerId(i));
            } else {
                touchUp(event.getPointerId(index));
            }
        }
        return true;
    }

    private void touchDown(int id, float x, float y) {
        if (modal()) {
            roles.put(id, PANEL);
            panelTouch(x, y);
            return;
        }
        if (closeButton.contains(x, y)) { roles.put(id, BUTTON); pause(); onClose.run(); return; }
        if (helpButton.contains(x, y)) { roles.put(id, BUTTON); helpOpen = true; return; }
        if (zoomIn.contains(x, y)) { roles.put(id, BUTTON); userZoomTarget = Math.min(2.2f, userZoomTarget * 1.3f); return; }
        if (zoomOut.contains(x, y)) { roles.put(id, BUTTON); userZoomTarget = Math.max(.45f, userZoomTarget / 1.3f); return; }
        if (weaponButton.contains(x, y)) { roles.put(id, BUTTON); weaponOpen = true; return; }
        if (buildButton.contains(x, y)) { roles.put(id, BUTTON); building = !building; cursorFollows = true; return; }
        if (useButton.contains(x, y)) { roles.put(id, BUTTON); interact(); return; }
        if (attackButton.contains(x, y) && attackPointer < 0) { roles.put(id, BUTTON); attackPointer = id; attackHeld = true; return; }
        if (building) {
            for (int i = 0; i < hotbar.length; i++) {
                if (hotbar[i].contains(x, y)) { roles.put(id, BUTTON); buildKind = i; return; }
            }
        }
        if (x < width * .42f && y > height * .3f && joyPointer < 0) {
            roles.put(id, JOY);
            joyPointer = id;
            joyX0 = x;
            joyY0 = y;
            joyNowX = x;
            joyNowY = y;
            joyX = joyY = 0;
            return;
        }
        roles.put(id, WORLD);
        int worldPointers = 0;
        for (int role : roles.values()) if (role == WORLD) worldPointers++;
        if (worldPointers >= 2) {
            pinchFrom = -1;
            return;
        }
        if (building) {
            cursorX = tileAt(x, y, true);
            cursorY = tileAt(x, y, false);
            cursorFollows = false;
            lastPaintX = cursorX;
            lastPaintY = cursorY;
            placeAtCursor();
        }
    }

    private void touchMove(int id, float x, float y, MotionEvent event) {
        Integer role = roles.get(id);
        if (role == null) return;
        if (role == JOY) {
            float radius = dp(56);
            float dx = x - joyX0, dy = y - joyY0, d = (float) Math.hypot(dx, dy);
            if (d > radius) {
                joyX0 += dx / d * (d - radius);
                joyY0 += dy / d * (d - radius);
                dx = x - joyX0;
                dy = y - joyY0;
                d = radius;
            }
            joyNowX = x;
            joyNowY = y;
            joyX = d < dp(8) ? 0 : dx / radius;
            joyY = d < dp(8) ? 0 : dy / radius;
        } else if (role == WORLD) {
            // Two fingers on the world pinch the zoom; one, in build mode, paints.
            int a = -1, b = -1;
            for (int i = 0; i < event.getPointerCount(); i++) {
                Integer r = roles.get(event.getPointerId(i));
                if (r != null && r == WORLD) {
                    if (a < 0) a = i;
                    else if (b < 0) b = i;
                }
            }
            if (b >= 0) {
                float d = (float) Math.hypot(event.getX(a) - event.getX(b), event.getY(a) - event.getY(b));
                if (pinchFrom > 0) userZoomTarget = Math.max(.45f, Math.min(2.2f, userZoomTarget * d / pinchFrom));
                pinchFrom = d;
            } else if (building) {
                int tx = tileAt(x, y, true), ty = tileAt(x, y, false);
                cursorX = tx;
                cursorY = ty;
                cursorFollows = false;
                if (tx != lastPaintX || ty != lastPaintY) {
                    lastPaintX = tx;
                    lastPaintY = ty;
                    placeAtCursor();
                }
            }
        }
    }

    private void touchUp(int id) {
        Integer role = roles.remove(id);
        if (role == null) return;
        if (role == JOY && id == joyPointer) {
            joyPointer = -1;
            joyX = joyY = 0;
        } else if (role == BUTTON && id == attackPointer) {
            attackPointer = -1;
            attackHeld = false;
        } else if (role == WORLD) {
            pinchFrom = -1;
            lastPaintX = lastPaintY = -1;
        }
    }

    private void panelTouch(float x, float y) {
        if (helpOpen) {
            closeHelp();
            return;
        }
        if (chestX >= 0) {
            int[] held = world.chest(chestX, chestY);
            if (held == null) {
                chestX = -1;
                return;
            }
            if (chestPut.contains(x, y)) {
                for (int i = 0; i < 3; i++) {
                    held[i] += inv[i];
                    inv[i] = 0;
                }
                dirty = true;
            } else if (chestTake.contains(x, y)) {
                for (int i = 0; i < 3; i++) {
                    inv[i] += held[i];
                    held[i] = 0;
                }
                dirty = true;
            } else if (!chestPanel.contains(x, y)) {
                chestX = -1;
            }
            return;
        }
        if (weaponOpen) {
            for (int i = 0; i < weaponCells.length; i++) {
                if (weaponCells[i].contains(x, y)) {
                    setWeapon(i, rarity);
                    return;
                }
            }
            if (rarityLess.contains(x, y)) setWeapon(weaponIndex, (rarity + 5) % 6);
            else if (rarityMore.contains(x, y)) setWeapon(weaponIndex, (rarity + 1) % 6);
            else if (spawnButton.contains(x, y)) spawnGroup();
            else if (clearButton.contains(x, y)) {
                foes.clear();
                respawns.clear();
                spawnStart();
                say("Foes reset");
            } else if (panelDone.contains(x, y) || !panel.contains(x, y)) weaponOpen = false;
        }
    }

    // Drawing

    private void render(Canvas canvas) {
        canvas.drawColor(0xFF9CC77B);
        if (!art.ok || width == 0) {
            text.setColor(Color.BLACK);
            text.setTextSize(dp(16));
            canvas.drawText("The sandbox art pack is missing.", dp(20), dp(60), text);
            return;
        }
        canvas.save();
        canvas.translate(width / 2f, height / 2f);
        canvas.scale(zoom, zoom);
        canvas.translate(-camX, -camY);
        float halfW = width / 2f / zoom, halfH = height / 2f / zoom;
        int x0 = (int) Math.floor(camX - halfW) - 2, x1 = (int) Math.ceil(camX + halfW) + 2;
        int y0 = (int) Math.floor(camY - halfH) - 1, y1 = (int) Math.ceil(camY + halfH) + 5;
        drawGround(canvas, x0, y0, x1, y1);
        itemCount = 0;
        collect(canvas, x0, y0, x1, y1);
        SandboxDepth.sort(items, itemCount);
        for (int i = 0; i < itemCount; i++) drawItem(canvas, items[i]);
        drawEffects(canvas);
        if (building && !modal()) drawGhost(canvas);
        canvas.restore();
        drawLabels(canvas);
        drawUi(canvas);
    }

    private void drawGround(Canvas canvas, int x0, int y0, int x1, int y1) {
        for (int ty = y0; ty <= y1; ty++) {
            for (int tx = x0; tx <= x1; tx++) {
                byte g = world.groundOf(tx, ty);
                int texture = g == SandboxWorld.PATH ? SandboxArt.PATH : g == SandboxWorld.STONE ? SandboxArt.STONE : g == SandboxWorld.WATER ? SandboxArt.WATER : SandboxArt.GRASS;
                paint.setShader(art.shader(texture, 2));
                canvas.drawRect(tx - .004f, ty - .004f, tx + 1.004f, ty + 1.004f, paint);
            }
        }
        paint.setShader(null);
        // A little variety so that no tile looks stamped, shorelines, and the edges of paths.
        for (int ty = y0; ty <= y1; ty++) {
            for (int tx = x0; tx <= x1; tx++) {
                byte g = world.groundOf(tx, ty);
                float h = SandboxWorld.hash(tx, ty, 99);
                fill.setColor(h < .5f ? Color.argb((int) ((.5f - h) * 9), 0, 40, 0) : Color.argb((int) ((h - .5f) * 8), 255, 255, 220));
                canvas.drawRect(tx, ty, tx + 1, ty + 1, fill);
                if (g == SandboxWorld.WATER) {
                    fill.setColor(Color.argb(60 + (int) (30 * Math.sin(clock * 1.6 + tx * .8 + ty * .5)), 255, 255, 255));
                    float ox = (float) Math.sin(clock * .7f + ty * .9f) * .12f;
                    canvas.drawRect(tx + .15f + ox, ty + .3f, tx + .55f + ox, ty + .34f, fill);
                    canvas.drawRect(tx + .45f - ox, ty + .7f, tx + .85f - ox, ty + .74f, fill);
                }
                if (g != SandboxWorld.GRASS) edge(canvas, tx, ty, g);
            }
        }
        // Floors she has laid
        paint.setShader(art.shader(SandboxArt.FLOOR, 2));
        for (int ty = Math.max(0, y0); ty <= Math.min(SandboxWorld.SIZE - 1, y1); ty++) {
            for (int tx = Math.max(0, x0); tx <= Math.min(SandboxWorld.SIZE - 1, x1); tx++) {
                if (world.floor[SandboxWorld.at(tx, ty)] != 0) canvas.drawRect(tx - .004f, ty - .004f, tx + 1.004f, ty + 1.004f, paint);
            }
        }
        paint.setShader(null);
        stroke.setStrokeWidth(.025f);
        stroke.setColor(0x33000000);
        for (int ty = Math.max(0, y0); ty <= Math.min(SandboxWorld.SIZE - 1, y1); ty++) {
            for (int tx = Math.max(0, x0); tx <= Math.min(SandboxWorld.SIZE - 1, x1); tx++) {
                if (world.floor[SandboxWorld.at(tx, ty)] == 0) continue;
                if (!floorAt(tx, ty - 1)) canvas.drawLine(tx, ty + .012f, tx + 1, ty + .012f, stroke);
                if (!floorAt(tx, ty + 1)) canvas.drawLine(tx, ty + .988f, tx + 1, ty + .988f, stroke);
                if (!floorAt(tx - 1, ty)) canvas.drawLine(tx + .012f, ty, tx + .012f, ty + 1, stroke);
                if (!floorAt(tx + 1, ty)) canvas.drawLine(tx + .988f, ty, tx + .988f, ty + 1, stroke);
            }
        }
    }

    private boolean floorAt(int x, int y) { return SandboxWorld.inside(x, y) && world.floor[SandboxWorld.at(x, y)] != 0; }

    private void edge(Canvas canvas, int tx, int ty, byte g) {
        int[][] around = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int side = 0; side < 4; side++) {
            byte other = world.groundOf(tx + around[side][0], ty + around[side][1]);
            if (other == g) continue;
            if (g != SandboxWorld.WATER && other != SandboxWorld.GRASS) continue;
            if (g == SandboxWorld.WATER && other == SandboxWorld.WATER) continue;
            float w = g == SandboxWorld.WATER ? .1f : .07f;
            fill.setColor(g == SandboxWorld.WATER ? 0xB0FFFFFF : g == SandboxWorld.PATH ? 0x555A3A1A : 0x55202020);
            switch (side) {
                case 0: canvas.drawRect(tx, ty, tx + 1, ty + w, fill); break;
                case 1: canvas.drawRect(tx + 1 - w, ty, tx + 1, ty + 1, fill); break;
                case 2: canvas.drawRect(tx, ty + 1 - w, tx + 1, ty + 1, fill); break;
                default: canvas.drawRect(tx, ty, tx + w, ty + 1, fill);
            }
        }
    }

    private void add(float x, float y, int layer, int kind, int index) {
        if (itemCount >= items.length) return;
        items[itemCount++].set(x, y, layer, kind, index);
    }

    /** Gathers what stands in view for the depth sort, and puts down the soft shadows beneath it as it goes. */
    private void collect(Canvas canvas, int x0, int y0, int x1, int y1) {
        for (int ty = Math.max(0, y0); ty <= Math.min(SandboxWorld.SIZE - 1, y1); ty++) {
            for (int tx = Math.max(0, x0); tx <= Math.min(SandboxWorld.SIZE - 1, x1); tx++) {
                int tile = SandboxWorld.at(tx, ty);
                byte n = world.natural[tile];
                if (n != SandboxWorld.NONE) {
                    float foot = ty + (n == SandboxWorld.TREE ? .86f : .8f);
                    if (n == SandboxWorld.TREE) shadow(canvas, tx + .55f, foot, 1.1f, .42f, 46);
                    else if (n == SandboxWorld.ROCK) shadow(canvas, tx + .55f, foot, .8f, .3f, 46);
                    else if (n == SandboxWorld.BUSH) shadow(canvas, tx + .5f, foot, .55f, .2f, 40);
                    add(tx + .5f, foot, SandboxDepth.STANDING, K_NATURAL, tile);
                }
                if (world.wall[tile] != 0) {
                    // (A soft shadow on the ground in front of the wall.)
                    int sm = world.wallMask(tx, ty);
                    fill.setColor(0x24000000);
                    box.set(tx + ((sm & SandboxWorld.W) != 0 ? 0 : .3f), ty + .67f, tx + ((sm & SandboxWorld.E) != 0 ? 1.12f : .8f), ty + (hasWallAt(tx, ty + 1) ? .67f : .98f));
                    canvas.drawRect(box, fill);
                    add(tx + .5f, ty + 1, SandboxDepth.STANDING, K_WALL, tile);
                }
                if (world.furniture[tile] != 0) {
                    shadow(canvas, tx + .55f, ty + .92f, .9f, .3f, 55);
                    add(tx + .5f, ty + .95f, SandboxDepth.STANDING, K_FURNITURE, tile);
                }
                if (world.roof[tile] != 0) add(tx + .5f, ty + 1.002f, SandboxDepth.ROOF, K_ROOF, tile);
            }
        }
        for (int i = 0; i < pickups.size(); i++) add(pickups.get(i).x, pickups.get(i).y, SandboxDepth.STANDING, K_PICKUP, i);
        for (int i = 0; i < foes.size(); i++) {
            SandboxCombat.Foe f = foes.get(i);
            shadow(canvas, f.x, f.y + .05f, f.radius() * 2.2f, f.radius() * .8f, 60);
            add(f.x, f.y, SandboxDepth.STANDING, K_FOE, i);
        }
        shadow(canvas, px, py + .04f, .75f, .26f, 70);
        add(px, py, SandboxDepth.STANDING, K_HER, 0);
        for (int i = 0; i < shots.size(); i++) add(shots.get(i).x, shots.get(i).y, SandboxDepth.FLYING, K_SHOT, i);
    }

    private void shadow(Canvas canvas, float cx, float cy, float w, float h, int alpha) {
        fill.setColor(Color.argb(alpha / 3, 20, 25, 10));
        for (int i = 0; i < 3; i++) {
            float k = 1 - i * .27f;
            box.set(cx - w * k / 2, cy - h * k / 2, cx + w * k / 2, cy + h * k / 2);
            canvas.drawOval(box, fill);
        }
    }

    private void drawItem(Canvas canvas, SandboxDepth.Item item) {
        switch (item.kind) {
            case K_NATURAL: drawNatural(canvas, item.index); break;
            case K_WALL: drawWall(canvas, item.index % SandboxWorld.SIZE, item.index / SandboxWorld.SIZE); break;
            case K_ROOF: drawRoof(canvas, item.index % SandboxWorld.SIZE, item.index / SandboxWorld.SIZE); break;
            case K_FURNITURE: drawFurniture(canvas, item.index % SandboxWorld.SIZE, item.index / SandboxWorld.SIZE); break;
            case K_PICKUP: drawPickup(canvas, pickups.get(item.index)); break;
            case K_FOE: drawFoe(canvas, foes.get(item.index)); break;
            case K_HER: drawHer(canvas); break;
            default: drawShot(canvas, shots.get(item.index)); break;
        }
    }

    private void sprite(Canvas canvas, android.graphics.Bitmap sheet, int col, int row, int cw, int ch, float x, float footY, float w, float footFraction, boolean flip, boolean flash, int alpha) {
        src.set(col * cw, row * ch, (col + 1) * cw, (row + 1) * ch);
        float h = w * ch / cw;
        dst.set(x - w / 2, footY - h * footFraction, x + w / 2, footY - h * footFraction + h);
        paint.setAlpha(alpha);
        paint.setColorFilter(flash ? whiteFlash : null);
        if (flip) {
            canvas.save();
            canvas.scale(-1, 1, x, footY);
            canvas.drawBitmap(sheet, src, dst, paint);
            canvas.restore();
        } else {
            canvas.drawBitmap(sheet, src, dst, paint);
        }
        paint.setColorFilter(null);
        paint.setAlpha(255);
    }

    private void drawNatural(Canvas canvas, int tile) {
        int tx = tile % SandboxWorld.SIZE, ty = tile / SandboxWorld.SIZE;
        byte n = world.natural[tile];
        Float shaking = shakes.get(tile);
        float sway = shaking != null ? (float) Math.sin(clock * 55) * .05f * shaking * 4 : (n == SandboxWorld.TREE ? (float) Math.sin(clock * 1.3f + tx) * .01f : 0);
        float hurt = world.hurtPart(tx, ty);
        int variant = world.variantOf(tx, ty);
        if (n == SandboxWorld.TREE) {
            sprite(canvas, art.trees, variant, 0, SandboxArt.TREE_W, SandboxArt.TREE_H, tx + .5f + sway, ty + .86f, 2.25f, SandboxArt.TREE_FOOT / (float) SandboxArt.TREE_H, false, false, hurt > .5f ? 235 : 255);
        } else {
            int cell = n == SandboxWorld.ROCK ? variant % 2 : n == SandboxWorld.BUSH ? 2 : 3;
            float w = n == SandboxWorld.ROCK ? 1.7f : n == SandboxWorld.BUSH ? 1.25f : 1.15f;
            sprite(canvas, art.rocks, cell % 2, cell / 2, SandboxArt.CELL, SandboxArt.CELL, tx + .5f + sway, ty + .82f, w, SandboxArt.CELL_FOOT / (float) SandboxArt.CELL, (tx + ty) % 2 == 0 && n != SandboxWorld.ROCK, false, 255);
        }
        if (hurt > 0 && (n == SandboxWorld.TREE || n == SandboxWorld.ROCK)) {
            // The blows it has taken, as a little bar over it.
            float bx = tx + .5f, by = ty + (n == SandboxWorld.TREE ? .1f : -.3f);
            fill.setColor(0x88000000);
            canvas.drawRect(bx - .4f, by, bx + .4f, by + .09f, fill);
            fill.setColor(0xFFE5784D);
            canvas.drawRect(bx - .4f, by, bx - .4f + .8f * (1 - hurt), by + .09f, fill);
        }
    }

    private void box(Canvas canvas, float l, float t, float r, float b, float h, boolean leftEnd, boolean rightEnd, boolean topEdge, int tint) {
        // Front face, with the board texture.
        paint.setShader(art.shader(SandboxArt.WALL, 1.4f));
        if (tint != 0) paint.setColorFilter(new LightingColorFilter(tint, 0));
        canvas.drawRect(l, b - h, r, b, paint);
        paint.setShader(null);
        paint.setColorFilter(null);
        // The top: pale plaster.
        fill.setColor(0xFFF3E6C8);
        canvas.drawRect(l, t - h, r, b - h, fill);
        // Ink along the edges that show.
        stroke.setColor(INK);
        stroke.setStrokeWidth(.03f);
        canvas.drawLine(l, b - h, r, b - h, stroke);
        canvas.drawLine(l, b, r, b, stroke);
        if (topEdge) canvas.drawLine(l, t - h, r, t - h, stroke);
        if (leftEnd) {
            canvas.drawLine(l, t - h, l, b, stroke);
        }
        if (rightEnd) {
            canvas.drawLine(r, t - h, r, b, stroke);
        }
        fill.setColor(0x26000000);
        canvas.drawRect(l, b - .1f, r, b, fill);
    }

    private void drawWall(Canvas canvas, int tx, int ty) {
        int tile = SandboxWorld.at(tx, ty);
        int mask = world.wallMask(tx, ty);
        float L = .33f, R = .67f, H = WALL_H;
        boolean door = world.wall[tile] == SandboxWorld.DOOR;
        boolean across = world.doorAcross(tx, ty);
        if (door && across) {
            // A doorway in a wall that runs along the row: a post at each side and a lintel; the leaf closed across the gap or swung open.
            boolean open = world.doorOpen[tile];
            if ((mask & W) != 0) box(canvas, tx, ty + L, tx + .2f, ty + R, H, false, false, true, 0);
            else box(canvas, tx, ty + L, tx + .2f, ty + R, H, true, false, true, 0);
            if ((mask & E) != 0) box(canvas, tx + .8f, ty + L, tx + 1, ty + R, H, false, false, true, 0);
            else box(canvas, tx + .8f, ty + L, tx + 1, ty + R, H, false, true, true, 0);
            // The lintel over the gap: its top, then its front face down to the top of the opening.
            fill.setColor(0xFFF3E6C8);
            canvas.drawRect(tx + .2f, ty + L - H, tx + .8f, ty + R - H, fill);
            paint.setShader(art.shader(SandboxArt.WALL, 1.4f));
            canvas.drawRect(tx + .2f, ty + R - H, tx + .8f, ty + R - H * .74f, paint);
            paint.setShader(null);
            stroke.setColor(INK);
            stroke.setStrokeWidth(.03f);
            canvas.drawLine(tx + .2f, ty + R - H, tx + .8f, ty + R - H, stroke);
            canvas.drawLine(tx + .2f, ty + L - H, tx + .8f, ty + L - H, stroke);
            stroke.setColor(INK);
            stroke.setStrokeWidth(.03f);
            canvas.drawLine(tx + .2f, ty + R - H * .74f, tx + .8f, ty + R - H * .74f, stroke);
            if (open) {
                fill.setColor(0xFFA4682F);
                canvas.drawRect(tx + .2f, ty + R - H * .74f, tx + .3f, ty + R, fill);
                stroke.setStrokeWidth(.02f);
                canvas.drawRect(tx + .2f, ty + R - H * .74f, tx + .3f, ty + R, stroke);
            } else {
                fill.setColor(0xFFB7742F);
                canvas.drawRect(tx + .2f, ty + R - H * .74f, tx + .8f, ty + R, fill);
                stroke.setColor(0xFF6B3F17);
                stroke.setStrokeWidth(.025f);
                for (int i = 1; i < 4; i++) canvas.drawLine(tx + .2f + .15f * i, ty + R - H * .74f, tx + .2f + .15f * i, ty + R, stroke);
                fill.setColor(0xFFFFD34D);
                canvas.drawCircle(tx + .68f, ty + R - H * .33f, .045f, fill);
            }
            return;
        }
        float alpha = door && !across && world.doorOpen[tile] ? .35f : 1;
        int save = alpha < 1 ? canvas.saveLayerAlpha(tx - .1f, ty - H - .2f, tx + 1.1f, ty + 1.2f, 90) : -1;
        if ((mask & N) != 0) box(canvas, tx + L, ty, tx + R, ty + L, H, false, false, (mask & N) == 0, 0);
        if ((mask & W) != 0) box(canvas, tx, ty + L, tx + L, ty + R, H, false, false, true, 0);
        if ((mask & E) != 0) box(canvas, tx + R, ty + L, tx + 1, ty + R, H, false, false, true, 0);
        box(canvas, tx + L, ty + L, tx + R, ty + R, H, (mask & W) == 0, (mask & E) == 0, (mask & N) == 0, 0);
        if ((mask & S) != 0) box(canvas, tx + L, ty + R, tx + R, ty + 1, H, true, true, false, 0);
        if (door) {
            fill.setColor(0xFFB7742F);
            canvas.drawRect(tx + L + .04f, ty + 1 - H * .74f, tx + R - .04f, ty + 1, fill);
            fill.setColor(0xFFFFD34D);
            canvas.drawCircle(tx + .5f, ty + 1 - H * .34f, .04f, fill);
        }
        if (save >= 0) canvas.restoreToCount(save);
    }

    private void drawRoof(Canvas canvas, int tx, int ty) {
        int tile = SandboxWorld.at(tx, ty);
        float target = roofRegion.get(tile) ? .1f : 1;
        float a = roofAlpha[tile];
        a += (target - a) * .18f;
        roofAlpha[tile] = a;
        if (a < .03f) return;
        float lift = ROOF_LIFT;
        float left = tx - (hasRoof(tx - 1, ty) ? .004f : .14f), right = tx + 1 + (hasRoof(tx + 1, ty) ? .004f : .14f);
        float top = ty - lift - (hasRoof(tx, ty - 1) ? .004f : .14f), bottom = ty + 1 - lift + (hasRoof(tx, ty + 1) ? .004f : .06f);
        boolean fascia = !hasRoof(tx, ty + 1);
        int layer = canvas.saveLayerAlpha(left - .1f, top - .1f, right + .1f, bottom + .35f, Math.round(255 * a));
        paint.setShader(art.shader(SandboxArt.ROOF, 1.6f));
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setShader(null);
        if (fascia) {
            fill.setColor(0xFF9A4A2E);
            canvas.drawRect(left, bottom, right, bottom + .2f, fill);
            fill.setColor(0x33000000);
            canvas.drawRect(left, bottom + .16f, right, bottom + .2f, fill);
        }
        stroke.setColor(INK);
        stroke.setStrokeWidth(.035f);
        if (!hasRoof(tx, ty - 1)) canvas.drawLine(left, top, right, top, stroke);
        if (!hasRoof(tx - 1, ty)) canvas.drawLine(left, top, left, bottom + (fascia ? .2f : 0), stroke);
        if (!hasRoof(tx + 1, ty)) canvas.drawLine(right, top, right, bottom + (fascia ? .2f : 0), stroke);
        if (fascia) canvas.drawLine(left, bottom + .2f, right, bottom + .2f, stroke);
        canvas.restoreToCount(layer);
    }

    private boolean hasWallAt(int x, int y) { return world.hasWall(x, y); }

    private boolean hasRoof(int x, int y) { return SandboxWorld.inside(x, y) && world.roof[SandboxWorld.at(x, y)] != 0; }

    private void drawFurniture(Canvas canvas, int tx, int ty) {
        int tile = SandboxWorld.at(tx, ty);
        int kind = world.furniture[tile];
        int cell = kind - 1;
        float[] widths = {1.35f, 1.5f, 1.25f, 1.05f};
        float w = widths[cell];
        if (kind == SandboxWorld.LAMP && world.lit(tx, ty)) {
            fill.setColor(0x33FFD27A);
            canvas.drawCircle(tx + .5f, ty + .2f, 1.7f, fill);
            fill.setColor(0x33FFD27A);
            canvas.drawCircle(tx + .5f, ty + .2f, 1.1f, fill);
        }
        sprite(canvas, art.furniture, cell % 2, cell / 2, SandboxArt.CELL, SandboxArt.CELL, tx + .5f, ty + .93f, w, SandboxArt.CELL_FOOT / (float) SandboxArt.CELL, false, false, 255);
    }

    private void drawPickup(Canvas canvas, Pickup p) {
        float bob = (float) Math.sin(clock * 5 + p.x * 3) * .04f;
        shadow(canvas, p.x, p.y + .03f, .3f, .12f, 70);
        float x = p.x, y = p.y - .22f + bob;
        stroke.setColor(INK);
        stroke.setStrokeWidth(.025f);
        if (p.item == SandboxWorld.WOOD) {
            fill.setColor(0xFFC98746);
            box.set(x - .17f, y - .07f, x + .17f, y + .08f);
            canvas.drawRoundRect(box, .05f, .05f, fill);
            canvas.drawRoundRect(box, .05f, .05f, stroke);
            fill.setColor(0xFFE8B678);
            canvas.drawOval(x + .1f, y - .07f, x + .2f, y + .08f, fill);
        } else if (p.item == SandboxWorld.PAPER) {
            fill.setColor(0xFFFFF7DA);
            path.reset();
            path.moveTo(x - .14f, y - .1f);
            path.lineTo(x + .13f, y - .13f);
            path.lineTo(x + .15f, y + .1f);
            path.lineTo(x - .12f, y + .12f);
            path.close();
            canvas.drawPath(path, fill);
            canvas.drawPath(path, stroke);
        } else {
            fill.setColor(0xFFBFBAB0);
            path.reset();
            path.moveTo(x - .15f, y + .09f);
            path.lineTo(x - .08f, y - .1f);
            path.lineTo(x + .1f, y - .08f);
            path.lineTo(x + .16f, y + .09f);
            path.close();
            canvas.drawPath(path, fill);
            canvas.drawPath(path, stroke);
        }
    }

    private void drawFoe(Canvas canvas, SandboxCombat.Foe f) {
        int cell = f.kind;
        float[] widths = {1.25f, 1.45f, 1.55f, 1.35f};
        float hover = f.kind == SandboxCombat.BAT ? .55f + (float) Math.sin(clock * 9 + f.x) * .08f : 0;
        float bounce = f.kind == SandboxCombat.SLIME ? Math.abs((float) Math.sin(clock * 5 + f.x)) * .12f : 0;
        float lean = f.kind == SandboxCombat.DUMMY ? Math.max(0, f.flash) * 1.2f * (f.vx >= 0 ? 1 : -1) : 0;
        canvas.save();
        if (lean != 0) canvas.rotate(lean * 12, f.x, f.y);
        sprite(canvas, art.creatures, cell % 2, cell / 2, SandboxArt.CELL, SandboxArt.CELL, f.x, f.y - hover - bounce, widths[cell], SandboxArt.CELL_FOOT / (float) SandboxArt.CELL, f.face < 0, f.flash > 0, 255);
        canvas.restore();
        // Its health, once it has been hurt.
        if (f.health < f.max) {
            float w = .9f, y = f.y - hover - widths[cell] * .85f;
            fill.setColor(0xAA000000);
            canvas.drawRoundRect(f.x - w / 2 - .02f, y - .02f, f.x + w / 2 + .02f, y + .11f, .06f, .06f, fill);
            fill.setColor(f.kind == SandboxCombat.DUMMY ? 0xFFFFC94D : 0xFFE5484D);
            canvas.drawRoundRect(f.x - w / 2, y, f.x - w / 2 + w * Math.max(0, f.health / f.max), y + .09f, .05f, .05f, fill);
        }
    }

    private void drawHer(Canvas canvas) {
        android.graphics.Bitmap sheet = facing == 1 ? art.herUp : facing == 0 ? art.herDown : art.herSide;
        int frame;
        if (attackT > 0) frame = 5;
        else if (moving) frame = (int) (walkClock % 4);
        else frame = 4;
        boolean flip = facing == 3;
        float bob = moving && attackT <= 0 ? Math.abs((float) Math.sin(walkClock * Math.PI / 2)) * .05f : (attackT <= 0 ? (float) Math.sin(clock * 2.2f) * .01f : 0);
        sprite(canvas, sheet, frame, 0, SandboxArt.HER_W, SandboxArt.HER_H, px, py - bob, 2.3f, SandboxArt.HER_FEET / (float) SandboxArt.HER_H, flip, hurtClock > 0 && (int) (clock * 24) % 2 == 0, 255);
        // Her weapon, swung or held out while she strikes.
        if (attackT > 0 && (weapon.shape == SandboxCombat.ARC || weapon.shape == SandboxCombat.RING)) {
            float t = 1 - attackT / attackFor;
            float sweep = weapon.spread * (2 * t - 1);
            if (weapon.shape == SandboxCombat.RING) sweep = (t - .5f) * 2.4f;
            float a = attackAngle + (weapon.kind == RunArms.KATANA ? 0 : sweep);
            float reachNow = weapon.kind == RunArms.KATANA || weapon.kind == RunArms.KNUCKLES ? .5f + t * weapon.reach * .5f : .55f;
            icon(canvas, RunLoot.iconOf(weapon.code), px + (float) Math.cos(a) * reachNow, py - .75f + (float) Math.sin(a) * reachNow * .6f, 1.25f, (float) Math.toDegrees(a) + 45 + (weapon.shape == SandboxCombat.RING ? -50 * (1 - t) : 0), 255);
        } else if (attackT > 0) {
            icon(canvas, RunLoot.iconOf(weapon.code), px + (float) Math.cos(attackAngle) * .6f, py - .8f + (float) Math.sin(attackAngle) * .4f, .9f, (float) Math.toDegrees(attackAngle) + 45, 255);
        }
    }

    private void icon(Canvas canvas, int index, float x, float y, float size, float degrees, int alpha) {
        if (art.icons == null) return;
        src.set((index % SandboxArt.ICON_COLUMNS) * SandboxArt.ICON, (index / SandboxArt.ICON_COLUMNS) * SandboxArt.ICON, (index % SandboxArt.ICON_COLUMNS + 1) * SandboxArt.ICON, (index / SandboxArt.ICON_COLUMNS + 1) * SandboxArt.ICON);
        dst.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        canvas.save();
        canvas.rotate(degrees, x, y);
        paint.setAlpha(alpha);
        canvas.drawBitmap(art.icons, src, dst, paint);
        paint.setAlpha(255);
        canvas.restore();
    }

    private void drawShot(Canvas canvas, Shot s) {
        float lift = 0, y = s.y;
        if (s.lob) {
            float t = Math.min(1, s.life / s.total);
            lift = (float) Math.sin(Math.PI * t) * 1.6f + .6f;
            shadow(canvas, s.x, s.y, .5f, .2f, 80);
            icon(canvas, RunLoot.iconOf(s.w.code), s.x, y - lift, .85f, clock * 500, 255);
            return;
        }
        float deg = (float) Math.toDegrees(s.angle);
        shadow(canvas, s.x, s.y + .35f, .3f, .1f, 60);
        if (s.w.kind == RunArms.PEN || s.w.kind == RunArms.KNIFE) {
            icon(canvas, RunLoot.iconOf(s.w.code), s.x, s.y, .85f, deg + 45 + (s.w.kind == RunArms.KNIFE ? clock * 700 : 0), 255);
        } else {
            stroke.setColor(s.w.colour);
            stroke.setStrokeWidth(.09f);
            canvas.drawLine(s.x, s.y, s.x - s.vx * .03f, s.y - s.vy * .03f, stroke);
            fill.setColor(0x55FFFFFF & s.w.colour | 0x55000000);
            canvas.drawCircle(s.x, s.y, .2f, fill);
            fill.setColor(s.w.colour);
            canvas.drawCircle(s.x, s.y, .1f, fill);
        }
    }

    private void drawEffects(Canvas canvas) {
        for (Slash s : slashes) {
            float t = s.age / s.life;
            if (s.ring) {
                float r = s.burst * (.35f + .65f * (float) Math.sqrt(t));
                float cx = s.reach > 0 ? s.x + (float) Math.cos(s.angle) * s.reach : s.x, cy = s.reach > 0 ? s.y + (float) Math.sin(s.angle) * s.reach : s.y;
                box.set(cx - r, cy - r * .62f, cx + r, cy + r * .62f);
                fill.setColor(Color.argb((int) (90 * (1 - t)), 255, 233, 168));
                canvas.drawOval(box, fill);
                stroke.setColor(Color.argb((int) (230 * (1 - t)), 255, 255, 255));
                stroke.setStrokeWidth(.08f);
                canvas.drawOval(box, stroke);
            } else {
                arcSlash(canvas, s, t, s.angle);
                if (s.both) arcSlash(canvas, s, t, s.angle + (float) Math.PI);
            }
        }
        for (Spark s : sparks) {
            fill.setColor((s.colour & 0x00FFFFFF) | ((int) (255 * Math.max(0, s.life / s.max)) << 24));
            canvas.drawCircle(s.x, s.y - s.z, s.size, fill);
        }
    }

    private void arcSlash(Canvas canvas, Slash s, float t, float angle) {
        // A crescent that sweeps across the arc and fades: the outer edge at the reach, the inner a little inside.
        float sweep = (float) Math.toDegrees(s.spread);
        float centre = (float) Math.toDegrees(angle);
        float from = centre - sweep + 2 * sweep * Math.min(1, t * 1.6f) - sweep * .9f;
        float span = Math.min(sweep * 1.8f, sweep * 2 * Math.min(1, t * 2.2f) + 8);
        float r = s.reach * (.85f + .15f * t);
        float cy = s.y - .55f;
        canvas.save();
        canvas.scale(1, .62f, s.x, cy);
        box.set(s.x - r, cy - r, s.x + r, cy + r);
        stroke.setColor(Color.argb((int) (235 * (1 - t)), Color.red(s.colour), Color.green(s.colour), Color.blue(s.colour)));
        stroke.setStrokeWidth(.2f * (1 - t * .6f));
        stroke.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(box, centre - sweep + (sweep * 2 - span) * Math.min(1, t * 1.4f), span, false, stroke);
        canvas.restore();
    }

    private void drawGhost(Canvas canvas) {
        if (!SandboxWorld.inside(cursorX, cursorY)) return;
        float dx = px - (cursorX + .5f), dy = py - (cursorY + .5f);
        boolean far = dx * dx + dy * dy > REACH * REACH;
        boolean valid = !far && world.canPlace(buildKind, cursorX, cursorY) && (buildKind == SandboxWorld.DEMOLISH || SandboxWorld.affords(inv, buildKind));
        fill.setColor(buildKind == SandboxWorld.DEMOLISH ? (valid ? 0x66E5A02D : BAD) : valid ? GOOD : BAD);
        canvas.drawRect(cursorX, cursorY, cursorX + 1, cursorY + 1, fill);
        stroke.setColor(valid ? 0xFF1F9D4F : 0xFFC73038);
        stroke.setStrokeWidth(.05f);
        canvas.drawRect(cursorX + .03f, cursorY + .03f, cursorX + .97f, cursorY + .97f, stroke);
        if (valid && buildKind != SandboxWorld.DEMOLISH) {
            // What it would look like, drawn faintly: a wall's post, a roof's rectangle, furniture's picture.
            int layer = canvas.saveLayerAlpha(cursorX - 1, cursorY - 3, cursorX + 2, cursorY + 2, 120);
            if (buildKind == SandboxWorld.B_WALL || buildKind == SandboxWorld.B_DOOR) box(canvas, cursorX + .33f, cursorY + .33f, cursorX + .67f, cursorY + .67f, WALL_H, true, true, true, 0);
            else if (buildKind == SandboxWorld.ROOF) {
                paint.setShader(art.shader(SandboxArt.ROOF, 1.6f));
                canvas.drawRect(cursorX, cursorY - ROOF_LIFT, cursorX + 1, cursorY + 1 - ROOF_LIFT, paint);
                paint.setShader(null);
            } else if (buildKind >= SandboxWorld.B_BED) {
                int cell = buildKind - SandboxWorld.B_BED;
                sprite(canvas, art.furniture, cell % 2, cell / 2, SandboxArt.CELL, SandboxArt.CELL, cursorX + .5f, cursorY + .93f, new float[]{1.35f, 1.5f, 1.25f, 1.05f}[cell], SandboxArt.CELL_FOOT / (float) SandboxArt.CELL, false, false, 255);
            }
            canvas.restoreToCount(layer);
        }
    }

    // Screen-space drawing

    private void drawLabels(Canvas canvas) {
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.CENTER);
        for (Label label : labels) {
            float sx = (label.x - camX) * zoom + width / 2f, sy = (label.y - camY) * zoom + height / 2f - label.age * dp(26);
            float fade = Math.max(0, Math.min(1, 1.3f - label.age));
            text.setTextSize(dp(16) * label.size);
            text.setColor(Color.argb((int) (255 * fade), 40, 25, 20));
            canvas.drawText(label.text, sx + dp(1), sy + dp(1.5f), text);
            text.setColor((label.colour & 0x00FFFFFF) | ((int) (255 * fade) << 24));
            canvas.drawText(label.text, sx, sy, text);
        }
        text.setTextAlign(Paint.Align.LEFT);
    }

    private void plate(Canvas canvas, RectF area, float radius, int alpha) {
        fill.setColor((PAPER & 0x00FFFFFF) | (alpha << 24));
        canvas.drawRoundRect(area, radius, radius, fill);
        stroke.setColor(INK);
        stroke.setStrokeWidth(dp(1.6f));
        canvas.drawRoundRect(area, radius, radius, stroke);
    }

    private void label(Canvas canvas, String message, float x, float y, float size, int colour, boolean bold, Paint.Align align) {
        text.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        text.setTextSize(dp(size));
        text.setColor(colour);
        text.setTextAlign(align);
        canvas.drawText(message, x, y, text);
        text.setTextAlign(Paint.Align.LEFT);
    }

    private void drawUi(Canvas canvas) {
        // The joystick
        float jx = joyPointer >= 0 ? joyX0 : insetL + dp(78), jy = joyPointer >= 0 ? joyY0 : height - insetB - dp(84);
        fill.setColor(0x33FFFFFF);
        canvas.drawCircle(jx, jy, dp(56), fill);
        stroke.setColor(0x99FFFFFF);
        stroke.setStrokeWidth(dp(2.5f));
        canvas.drawCircle(jx, jy, dp(56), stroke);
        fill.setColor(joyPointer >= 0 ? 0xCCFFFFFF : 0x88FFFFFF);
        canvas.drawCircle(joyPointer >= 0 ? joyNowX : jx, joyPointer >= 0 ? joyNowY : jy, dp(24), fill);
        // The buttons on the right
        button(canvas, attackButton, building ? "Place" : "Strike", true, attackHeld);
        button(canvas, buildButton, building ? "Done" : "Build", building, false);
        button(canvas, useButton, "Use", false, false);
        // The top row
        plate(canvas, closeButton, dp(10), 235);
        label(canvas, "X", closeButton.centerX(), closeButton.centerY() + dp(6), 18, INK, true, Paint.Align.CENTER);
        plate(canvas, helpButton, dp(10), 235);
        label(canvas, "?", helpButton.centerX(), helpButton.centerY() + dp(7), 20, INK, true, Paint.Align.CENTER);
        plate(canvas, zoomOut, dp(10), 235);
        label(canvas, "-", zoomOut.centerX(), zoomOut.centerY() + dp(7), 22, INK, true, Paint.Align.CENTER);
        plate(canvas, zoomIn, dp(10), 235);
        label(canvas, "+", zoomIn.centerX(), zoomIn.centerY() + dp(7), 22, INK, true, Paint.Align.CENTER);
        plate(canvas, weaponButton, dp(10), 235);
        iconScreen(canvas, RunLoot.iconOf(weapon.code), weaponButton.centerX(), weaponButton.centerY(), dp(34));
        // Health and what she carries, top left
        float l = insetL + dp(8), t = insetT + dp(8);
        box.set(l, t, l + dp(126), t + dp(60));
        plate(canvas, box, dp(10), 225);
        fill.setColor(0x33000000);
        canvas.drawRoundRect(l + dp(8), t + dp(8), l + dp(118), t + dp(18), dp(5), dp(5), fill);
        fill.setColor(health > 35 ? 0xFFE0709F : 0xFFE5484D);
        canvas.drawRoundRect(l + dp(8), t + dp(8), l + dp(8) + dp(110) * Math.max(0, health / 100f), t + dp(18), dp(5), dp(5), fill);
        for (int i = 0; i < 3; i++) {
            float cx = l + dp(8) + i * dp(40);
            itemIcon(canvas, i, cx + dp(8), t + dp(38), dp(9));
            label(canvas, String.valueOf(inv[i]), cx + dp(19), t + dp(43), 13, INK, true, Paint.Align.LEFT);
        }
        // The build bar
        if (building) {
            for (int i = 0; i < SandboxWorld.KINDS; i++) {
                RectF cell = hotbar[i];
                plate(canvas, cell, dp(8), i == buildKind ? 255 : 205);
                if (i == buildKind) {
                    stroke.setColor(CalculatorLayout.CORAL);
                    stroke.setStrokeWidth(dp(3));
                    canvas.drawRoundRect(cell, dp(8), dp(8), stroke);
                }
                kindIcon(canvas, i, cell);
            }
            int[] cost = SandboxWorld.cost(buildKind);
            StringBuilder line = new StringBuilder(SandboxWorld.KIND_NAMES[buildKind]);
            if (buildKind != SandboxWorld.DEMOLISH) {
                line.append("  (");
                boolean first = true;
                for (int i = 0; i < 3; i++) {
                    if (cost[i] == 0) continue;
                    line.append(first ? "" : ", ").append(cost[i]).append(' ').append(SandboxWorld.ITEM_NAMES[i].toLowerCase());
                    first = false;
                }
                line.append(')');
            } else {
                line.append("  (tap to take down and refund)");
            }
            boolean can = buildKind == SandboxWorld.DEMOLISH || SandboxWorld.affords(inv, buildKind);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp(13));
            float tw = text.measureText(line.toString()) + dp(20);
            box.set(width / 2f - tw / 2, hotbar[0].bottom + dp(6), width / 2f + tw / 2, hotbar[0].bottom + dp(30));
            plate(canvas, box, dp(10), 235);
            label(canvas, line.toString(), width / 2f, box.centerY() + dp(5), 13, can ? INK : 0xFFC73038, true, Paint.Align.CENTER);
        }
        if (noteClock > 0) {
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp(14));
            float tw = text.measureText(note) + dp(24);
            box.set(width / 2f - tw / 2, height * .62f, width / 2f + tw / 2, height * .62f + dp(30));
            plate(canvas, box, dp(14), (int) (240 * Math.min(1, noteClock)));
            label(canvas, note, width / 2f, box.centerY() + dp(5), 14, INK, true, Paint.Align.CENTER);
        }
        if (helpOpen) drawHelp(canvas);
        if (weaponOpen) drawWeapons(canvas);
        if (chestX >= 0) drawChest(canvas);
    }

    private void button(Canvas canvas, RectF r, String label, boolean main, boolean held) {
        float radius = r.width() / 2;
        fill.setColor(main ? (held ? 0xFFD95B4F : CalculatorLayout.CORAL) : 0xEEFFF7E6);
        canvas.drawCircle(r.centerX(), r.centerY(), radius, fill);
        stroke.setColor(INK);
        stroke.setStrokeWidth(dp(2));
        canvas.drawCircle(r.centerX(), r.centerY(), radius, stroke);
        label(canvas, label, r.centerX(), r.centerY() + dp(main && r.width() > dp(70) ? 6 : 5), main && r.width() > dp(70) ? 16 : 12, main ? Color.WHITE : INK, true, Paint.Align.CENTER);
    }

    private void iconScreen(Canvas canvas, int index, float cx, float cy, float size) {
        if (art.icons == null) return;
        src.set((index % SandboxArt.ICON_COLUMNS) * SandboxArt.ICON, (index / SandboxArt.ICON_COLUMNS) * SandboxArt.ICON, (index % SandboxArt.ICON_COLUMNS + 1) * SandboxArt.ICON, (index / SandboxArt.ICON_COLUMNS + 1) * SandboxArt.ICON);
        dst.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
        canvas.drawBitmap(art.icons, src, dst, paint);
    }

    private void itemIcon(Canvas canvas, int item, float cx, float cy, float r) {
        stroke.setColor(INK);
        stroke.setStrokeWidth(dp(1.4f));
        fill.setColor(ITEM_COLOURS[item]);
        if (item == SandboxWorld.WOOD) {
            canvas.drawRoundRect(cx - r, cy - r * .7f, cx + r, cy + r * .7f, dp(3), dp(3), fill);
            canvas.drawRoundRect(cx - r, cy - r * .7f, cx + r, cy + r * .7f, dp(3), dp(3), stroke);
        } else if (item == SandboxWorld.PAPER) {
            canvas.drawRect(cx - r * .8f, cy - r, cx + r * .8f, cy + r, fill);
            canvas.drawRect(cx - r * .8f, cy - r, cx + r * .8f, cy + r, stroke);
        } else {
            canvas.drawCircle(cx, cy, r, fill);
            canvas.drawCircle(cx, cy, r, stroke);
        }
    }

    private void kindIcon(Canvas canvas, int kind, RectF cell) {
        float cx = cell.centerX(), cy = cell.centerY(), s = cell.width() * .36f;
        switch (kind) {
            case SandboxWorld.FLOOR: texIcon(canvas, SandboxArt.FLOOR, cx, cy, s); break;
            case SandboxWorld.B_WALL: texIcon(canvas, SandboxArt.WALL, cx, cy, s); break;
            case SandboxWorld.B_DOOR:
                texIcon(canvas, SandboxArt.WALL, cx, cy, s);
                fill.setColor(0xFFB7742F);
                canvas.drawRect(cx - s * .5f, cy - s * .5f, cx + s * .5f, cy + s, fill);
                fill.setColor(0xFFFFD34D);
                canvas.drawCircle(cx + s * .25f, cy + s * .3f, dp(1.8f), fill);
                break;
            case SandboxWorld.ROOF: texIcon(canvas, SandboxArt.ROOF, cx, cy, s); break;
            case SandboxWorld.DEMOLISH:
                stroke.setColor(0xFFC73038);
                stroke.setStrokeWidth(dp(3.2f));
                canvas.drawLine(cx - s * .8f, cy - s * .8f, cx + s * .8f, cy + s * .8f, stroke);
                canvas.drawLine(cx + s * .8f, cy - s * .8f, cx - s * .8f, cy + s * .8f, stroke);
                break;
            default: {
                int c = kind - SandboxWorld.B_BED;
                src.set((c % 2) * SandboxArt.CELL, (c / 2) * SandboxArt.CELL, (c % 2 + 1) * SandboxArt.CELL, (c / 2 + 1) * SandboxArt.CELL);
                dst.set(cx - s * 1.15f, cy - s * 1.15f, cx + s * 1.15f, cy + s * 1.15f);
                canvas.drawBitmap(art.furniture, src, dst, paint);
            }
        }
    }

    private void texIcon(Canvas canvas, int texture, float cx, float cy, float s) {
        src.set(0, 0, 128, 128);
        dst.set(cx - s, cy - s, cx + s, cy + s);
        canvas.drawBitmap(art.textures[texture], src, dst, paint);
        stroke.setColor(INK);
        stroke.setStrokeWidth(dp(1.3f));
        canvas.drawRect(dst, stroke);
    }

    private void drawPanelBackdrop(Canvas canvas) {
        fill.setColor(0x88000000);
        canvas.drawRect(0, 0, width, height, fill);
        plate(canvas, panel, dp(18), 250);
    }

    private void paragraph(Canvas canvas, String message, float x, float y, float w, float size, int colour, boolean bold) {
        text.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        text.setTextSize(dp(size));
        text.setColor(colour);
        StaticLayout layout = StaticLayout.Builder.obtain(message, 0, message.length(), text, (int) w).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0, 1.05f).build();
        canvas.save();
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
    }

    private void drawHelp(Canvas canvas) {
        drawPanelBackdrop(canvas);
        float x = panel.left + dp(16), w = panel.width() - dp(32);
        label(canvas, "Sandbox (test) - how to play", x, panel.top + dp(30), 18, INK, true, Paint.Align.LEFT);
        String message = "MOVE  Drag on the left half of the screen (joystick), or W A S D / arrows.\n"
                + "STRIKE  The big round button (hold to keep going), or Space / J. Swings gather too: hit trees for wood and paper, rocks for stone; walk over the drops.\n"
                + "WEAPON  The icon at the top right opens the picker: all 13 weapon classes with their real stats, rarity with the arrows. Q cycles, R changes rarity.\n"
                + "BUILD  Tap Build (or B). Pick a piece on the bar (or 1-9): floor, wall (corners join by themselves), door, roof, bed, table, chest, lamp, or demolish. Tap a tile to place it, drag to paint a row; the Place button puts it in front of her. Green is good, red is not allowed. Demolish refunds everything.\n"
                + "ROOFS  Walk in under one and the whole roof fades away. Doors open as you near them.\n"
                + "USE  The Use button (or E) opens a chest, rests in a bed, lights a lamp.\n"
                + "LOOK  Pinch or the + / - buttons (or the mouse wheel, Z / X) zoom smoothly. Your house is saved by itself.\n\n"
                + "Tap anywhere to start.";
        paragraph(canvas, message, x, panel.top + dp(42), w, 13, INK, false);
    }

    private void drawWeapons(Canvas canvas) {
        drawPanelBackdrop(canvas);
        label(canvas, "Weapons (Plus run's own classes)", panel.left + dp(14), panel.top + dp(28), 16, INK, true, Paint.Align.LEFT);
        for (int i = 0; i < weaponCells.length; i++) {
            RectF cell = weaponCells[i];
            boolean on = i == weaponIndex;
            fill.setColor(on ? 0xFFFFD9CF : 0xFFFFFFFF);
            canvas.drawRoundRect(cell, dp(8), dp(8), fill);
            stroke.setColor(on ? CalculatorLayout.CORAL : 0x55000000);
            stroke.setStrokeWidth(on ? dp(2.6f) : dp(1));
            canvas.drawRoundRect(cell, dp(8), dp(8), stroke);
            float is = Math.min(cell.width() * .6f, cell.height() * .62f);
            iconScreen(canvas, RunLoot.iconOf(RunLoot.code(RunLoot.WEAPON, SandboxCombat.THEME, i, rarity)), cell.centerX(), cell.top + is * .55f + dp(2), is);
            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp(9.5f));
            text.setColor(INK);
            text.setTextAlign(Paint.Align.CENTER);
            String name = RunLoot.WEAPONS[i].replace("Summoning ", "");
            canvas.drawText(name, cell.centerX(), cell.bottom - dp(5), text);
            text.setTextAlign(Paint.Align.LEFT);
        }
        boolean wide = panel.width() > panel.height() * 1.25f;
        float dx = wide ? panel.left + panel.width() * .5f + dp(18) : panel.left + dp(12), dw = wide ? panel.width() * .5f - dp(30) : panel.width() - dp(24);
        float top = wide ? panel.top + dp(40) : weaponCells[weaponCells.length - 1].bottom + dp(6);
        label(canvas, RunLoot.RARITIES[rarity] + " " + weapon.name, dx, top + dp(16), 15, INK, true, Paint.Align.LEFT);
        label(canvas, SandboxCombat.stats(weapon), dx, top + dp(34), 11, SOFT, false, Paint.Align.LEFT);
        paragraph(canvas, weapon.does, dx, top + dp(40), dw, 11, INK, false);
        button2(canvas, rarityLess, "<");
        button2(canvas, rarityMore, ">");
        label(canvas, RunLoot.RARITIES[rarity], (rarityLess.right + rarityMore.left) / 2f, rarityLess.centerY() + dp(5), 13, INK, true, Paint.Align.CENTER);
        button2(canvas, spawnButton, "Spawn foes");
        button2(canvas, clearButton, "Reset foes");
        fill.setColor(CalculatorLayout.CORAL);
        canvas.drawRoundRect(panelDone, dp(10), dp(10), fill);
        label(canvas, "Done", panelDone.centerX(), panelDone.centerY() + dp(5), 13, Color.WHITE, true, Paint.Align.CENTER);
    }

    private void button2(Canvas canvas, RectF r, String name) {
        fill.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(r, dp(10), dp(10), fill);
        stroke.setColor(INK);
        stroke.setStrokeWidth(dp(1.4f));
        canvas.drawRoundRect(r, dp(10), dp(10), stroke);
        label(canvas, name, r.centerX(), r.centerY() + dp(5), 12, INK, true, Paint.Align.CENTER);
    }

    private void drawChest(Canvas canvas) {
        int[] held = world.chest(chestX, chestY);
        if (held == null) return;
        fill.setColor(0x88000000);
        canvas.drawRect(0, 0, width, height, fill);
        plate(canvas, chestPanel, dp(18), 250);
        label(canvas, "Chest", chestPanel.centerX(), chestPanel.top + dp(30), 18, INK, true, Paint.Align.CENTER);
        for (int i = 0; i < 3; i++) {
            float cx = chestPanel.left + dp(14) + i * (chestPanel.width() - dp(28)) / 3f;
            itemIcon(canvas, i, cx + dp(10), chestPanel.top + dp(62), dp(9));
            label(canvas, "in chest " + held[i], cx + dp(24), chestPanel.top + dp(58), 11, INK, true, Paint.Align.LEFT);
            label(canvas, "you have " + inv[i], cx + dp(24), chestPanel.top + dp(76), 10, SOFT, false, Paint.Align.LEFT);
        }
        button2(canvas, chestPut, "Put everything in");
        button2(canvas, chestTake, "Take everything");
    }

    // Test access

    int inventory(int item) { return inv[item]; }
    SandboxWorld world() { return world; }
}
