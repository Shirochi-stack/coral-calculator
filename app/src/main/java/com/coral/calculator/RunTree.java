package com.coral.calculator;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Her skill tree's own screen, over everything: the tree on a night sky, dragged to look round and pinched (or the + and − buttons, or the
 * mouse wheel) to come closer or go further off; each path's emblem past its keystone; each node drawn as its own picture, bright when
 * taken, lit round when it can be taken. A node touched shows its card: what it is and does, and a button to take it, or for an active
 * taken, the slot to put it in. At the top, the points she has, her stats, and taking every point back at a cost.
 * The game keeps what is taken; this only shows it and says what was asked for.
 */
final class RunTree {
    /** What a touch asked for: nothing, to close, to take {@link #picked}, to put it in {@link #pickedSlot}, or to take every point back. */
    static final int NOTHING = 0, CLOSE = 1, TAKE = 2, SLOT = 3, RESPEC = 4;

    /** What is shown: set by the game before each drawing. */
    static final class Facts {
        BitSet taken = new BitSet();
        int[] slots = new int[RunSkills.SLOTS];
        int points, level = 1, bank, respecCost, hearts = 3;
        boolean freeRespec;
        RunSkills.Built built = new RunSkills.Built();
    }

    private static final int ICON = 96, COLUMNS = 8, NODE_ICONS = RunSkills.PATHS;
    /** How far out the tree reaches, in its own units: the keystones, and the emblems past them. */
    static final float REACH = 4 + RunSkills.PATH_LENGTH * 1.15f, EMBLEMS = REACH + 5.5f;

    private final Resources resources;
    private final float density;
    private Bitmap icons;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF target = new RectF();

    /** Where it is looked at (in the tree's units), how close (pixels to a unit), the node looked at, and what is open over it. */
    private float lookX, lookY, zoom;
    private int on = -1;
    private boolean statsOpen, asking;
    private float clock;
    /** A finger or two: where it went down, where it was last, whether it has moved enough to be a drag; two fingers' spread and middle. */
    private float downX, downY, lastX, lastY, spread, midX, midY;
    private boolean moved, onMap, pinching;
    private int picked = -1, pickedSlot = -1;

    private final RectF closeButton = new RectF(), statsButton = new RectF(), respecButton = new RectF(), zoomIn = new RectF(), zoomOut = new RectF();
    private final RectF card = new RectF(), act = new RectF(), statsCard = new RectF(), askCard = new RectF(), askYes = new RectF(), askNo = new RectF(), top = new RectF();
    private final RectF[] slotButtons = {new RectF(), new RectF(), new RectF()};
    private float width, height;

    RunTree(Resources resources, float density) {
        this.resources = resources;
        this.density = density;
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);
        ring.setStyle(Paint.Style.STROKE);
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }

    private float dp(float value) { return value * density; }

    /** Opened afresh: at the centre, close enough to read the first steps of every path. */
    void open(float width, float height) {
        this.width = width;
        this.height = height;
        lookX = lookY = 0;
        zoom = Math.max(least(), dp(16));
        on = -1;
        statsOpen = asking = false;
        pinching = false;
    }

    int picked() { return picked; }
    int pickedSlot() { return pickedSlot; }
    float zoom() { return zoom; }
    int on() { return on; }

    /** The furthest off it can be seen from: the whole tree in the screen. */
    private float least() { return Math.min(width, height) / (2 * (EMBLEMS + 7)); }

    private float most() { return dp(64); }

    private void zoomBy(float times, float aboutX, float aboutY) {
        float before = zoom;
        zoom = Math.max(least(), Math.min(most(), zoom * times));
        // (What is under the fingers stays under them.)
        float wx = lookX + (aboutX - width / 2) / before, wy = lookY + (aboutY - height / 2) / before;
        lookX = wx - (aboutX - width / 2) / zoom;
        lookY = wy - (aboutY - height / 2) / zoom;
        hold();
    }

    private void hold() {
        float far = EMBLEMS + 2;
        lookX = Math.max(-far, Math.min(far, lookX));
        lookY = Math.max(-far, Math.min(far, lookY));
    }

    private float sx(float x) { return width / 2 + (x - lookX) * zoom; }
    private float sy(float y) { return height / 2 + (y - lookY) * zoom; }

    /** A node's size, in the tree's units. */
    static float size(RunSkills.Node node) {
        if (node.type == RunSkills.CENTRE) return 1.6f;
        if (node.type == RunSkills.NOTABLE) return RunSkills.keystone(node) ? 1.05f : .8f;
        return node.type == RunSkills.ROOT ? .58f : .38f;
    }

    /** Which picture in the sheet a node is. */
    static int iconOf(RunSkills.Node node) {
        if (node.type == RunSkills.CENTRE) return NODE_ICONS + 11;
        if (node.type == RunSkills.SMALL) return NODE_ICONS + node.stat;
        if (node.type == RunSkills.ROOT) return NODE_ICONS + 4 + node.stat;
        if (RunSkills.keystone(node)) return NODE_ICONS + 10;
        return NODE_ICONS + (RunSkills.notables(node.path).get(node.notable).kind == RunSkills.ACTIVE ? 9 : 8);
    }

    /** One of the sheet's pictures, at a middle and a size. */
    void icon(Canvas canvas, int index, float x, float y, float size, int alpha) {
        if (icons == null) icons = BitmapFactory.decodeResource(resources, R.drawable.run_skill_icons);
        if (icons == null) return;
        int column = index % COLUMNS, row = index / COLUMNS;
        source.set(column * ICON, row * ICON, (column + 1) * ICON, (row + 1) * ICON);
        target.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        paint.setAlpha(alpha);
        canvas.drawBitmap(icons, source, target, paint);
        paint.setAlpha(255);
    }

    /** A path's colour, lighter, to be seen on the night sky. */
    private static int lit(int colour, float toward) {
        int r = Color.red(colour), g = Color.green(colour), b = Color.blue(colour);
        return Color.rgb(Math.round(r + (255 - r) * toward), Math.round(g + (255 - g) * toward), Math.round(b + (255 - b) * toward));
    }

    // Touches

    /** A touch on the screen: what it asks for, if anything (see {@link #picked} and {@link #pickedSlot}). */
    int touch(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX(), y = event.getY();
        picked = -1;
        pickedSlot = -1;
        if (action == MotionEvent.ACTION_DOWN) {
            downX = lastX = x;
            downY = lastY = y;
            moved = false;
            pinching = false;
            onMap = false;
            // A card asking whether to take every point back.
            if (asking) {
                if (askYes.contains(x, y)) {
                    asking = false;
                    return RESPEC;
                }
                if (askNo.contains(x, y) || !askCard.contains(x, y)) asking = false;
                return NOTHING;
            }
            if (closeButton.contains(x, y)) return CLOSE;
            if (statsButton.contains(x, y)) {
                statsOpen = !statsOpen;
                return NOTHING;
            }
            if (statsOpen) {
                if (!statsCard.contains(x, y)) statsOpen = false;
                return NOTHING;
            }
            if (respecButton.contains(x, y)) {
                asking = true;
                return NOTHING;
            }
            if (zoomIn.contains(x, y)) {
                zoomBy(1.5f, width / 2, height / 2);
                return NOTHING;
            }
            if (zoomOut.contains(x, y)) {
                zoomBy(1 / 1.5f, width / 2, height / 2);
                return NOTHING;
            }
            if (on >= 0 && card.contains(x, y)) {
                if (!act.isEmpty() && act.contains(x, y)) {
                    picked = on;
                    return TAKE;
                }
                for (int slot = 0; slot < RunSkills.SLOTS; slot++) {
                    if (!slotButtons[slot].isEmpty() && slotButtons[slot].contains(x, y)) {
                        picked = on;
                        pickedSlot = slot;
                        return SLOT;
                    }
                }
                return NOTHING;
            }
            if (top.contains(x, y)) return NOTHING;
            onMap = true;
            return NOTHING;
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount() >= 2 && onMap) {
            pinching = true;
            moved = true;
            spread = spread(event);
            midX = (event.getX(0) + event.getX(1)) / 2;
            midY = (event.getY(0) + event.getY(1)) / 2;
            return NOTHING;
        }
        if (action == MotionEvent.ACTION_MOVE && onMap) {
            if (pinching && event.getPointerCount() >= 2) {
                float now = spread(event), mx = (event.getX(0) + event.getX(1)) / 2, my = (event.getY(0) + event.getY(1)) / 2;
                if (spread > dp(10) && now > dp(10)) zoomBy(now / spread, mx, my);
                lookX -= (mx - midX) / zoom;
                lookY -= (my - midY) / zoom;
                hold();
                spread = now;
                midX = mx;
                midY = my;
                return NOTHING;
            }
            if (pinching) return NOTHING;
            if (!moved && Math.hypot(x - downX, y - downY) > dp(8)) moved = true;
            if (moved) {
                lookX -= (x - lastX) / zoom;
                lookY -= (y - lastY) / zoom;
                hold();
            }
            lastX = x;
            lastY = y;
            return NOTHING;
        }
        if (action == MotionEvent.ACTION_POINTER_UP && pinching) {
            // The finger left down goes on dragging from where it is.
            int left = event.getActionIndex() == 0 ? 1 : 0;
            lastX = event.getX(left);
            lastY = event.getY(left);
            pinching = false;
            return NOTHING;
        }
        if (action == MotionEvent.ACTION_UP && onMap && !moved) {
            on = nodeAt(x, y);
            return NOTHING;
        }
        return NOTHING;
    }

    private static float spread(MotionEvent event) { return (float) Math.hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1)); }

    /** The mouse wheel: closer or further off, about the pointer. */
    void wheel(float turned, float x, float y) { zoomBy(turned > 0 ? 1.25f : 1 / 1.25f, x, y); }

    /** The node nearest a point on the screen, within a finger's reach of it; or -1. */
    int nodeAt(float x, float y) {
        List<RunSkills.Node> nodes = RunSkills.tree();
        int best = -1;
        float bestFar = Float.MAX_VALUE;
        for (RunSkills.Node node : nodes) {
            float far = (float) Math.hypot(sx(node.x) - x, sy(node.y) - y), reach = Math.max(dp(20), size(node) * zoom);
            if (far < reach && far < bestFar) {
                best = node.id;
                bestFar = far;
            }
        }
        return best;
    }

    /** The node looked at is brought into the middle (after it is taken, to follow along). */
    void look(int id) {
        on = id;
    }

    // Drawing

    void draw(Canvas canvas, RectF safe, Facts facts) {
        width = canvas.getWidth();
        height = canvas.getHeight();
        if (zoom <= 0) open(width, height);
        clock = (System.nanoTime() / 1_000_000L % 1_000_000L) / 1000f;
        canvas.drawColor(0xFF110E20);
        // A soft light at the heart of it.
        float cx = sx(0), cy = sy(0);
        paint.setStyle(Paint.Style.FILL);
        for (int glow = 5; glow >= 1; glow--) {
            paint.setColor(Color.argb(10, 150, 120, 255));
            canvas.drawCircle(cx, cy, glow * 7 * zoom, paint);
        }
        // A few fixed stars, the same each time.
        paint.setColor(0x55FFFFFF);
        long seed = 7;
        for (int star = 0; star < 90; star++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            float fx = ((seed >>> 33) % 10000) / 10000f, fy = ((seed >>> 13) % 10000) / 10000f;
            canvas.drawCircle(fx * width, fy * height, dp(.8f + (star % 3) * .4f), paint);
        }
        List<RunSkills.Node> nodes = RunSkills.tree();
        BitSet taken = facts.taken;
        int left = facts.points - RunSkills.spent(taken);
        float margin = dp(40);
        // The links: bright in the path's colour where both ends are taken, lit where one is, faint elsewhere.
        for (RunSkills.Node node : nodes) {
            float ax = sx(node.x), ay = sy(node.y);
            for (int other : node.next) {
                if (other < node.id) continue;
                RunSkills.Node far = nodes.get(other);
                float bx = sx(far.x), by = sy(far.y);
                if (Math.max(ax, bx) < -margin || Math.min(ax, bx) > width + margin || Math.max(ay, by) < -margin || Math.min(ay, by) > height + margin) continue;
                boolean one = node.id == 0 || taken.get(node.id), two = taken.get(other);
                int path = far.path >= 0 ? far.path : node.path;
                boolean across = node.path >= 0 && far.path >= 0 && node.path != far.path;
                int colour = across ? 0xFFFFD27A : lit(RunSkills.PATH_COLOURS[Math.max(0, path)], .25f);
                if (one && two) {
                    line.setColor(colour);
                    line.setStrokeWidth(Math.max(dp(2), Math.min(dp(5), zoom * .18f)));
                } else if (one || two) {
                    line.setColor(colour);
                    line.setAlpha(110);
                    line.setStrokeWidth(Math.max(dp(1.5f), Math.min(dp(3.5f), zoom * .12f)));
                } else {
                    line.setColor(across ? 0x55FFD27A : 0x33FFFFFF);
                    line.setStrokeWidth(Math.max(dp(1), Math.min(dp(2.5f), zoom * .08f)));
                }
                canvas.drawLine(ax, ay, bx, by, line);
            }
        }
        // The nodes.
        float pulse = .5f + .5f * (float) Math.sin(clock * 4);
        for (RunSkills.Node node : nodes) {
            float x = sx(node.x), y = sy(node.y), r = size(node) * zoom;
            if (x < -r - margin || x > width + r + margin || y < -r - margin || y > height + r + margin) continue;
            boolean has = node.id == 0 || taken.get(node.id), can = !has && RunSkills.canTake(taken, node.id);
            int colour = node.path >= 0 ? lit(RunSkills.PATH_COLOURS[node.path], .3f) : 0xFFFFE9A8;
            if (r < dp(5)) {
                // Far off: a dot.
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(has ? colour : can ? 0xCCFFFFFF : 0x55FFFFFF);
                canvas.drawCircle(x, y, Math.max(dp(1.2f), r * .8f), paint);
                continue;
            }
            if (has) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(70, Color.red(colour), Color.green(colour), Color.blue(colour)));
                canvas.drawCircle(x, y, r * 1.35f, paint);
            }
            icon(canvas, iconOf(node), x, y, r * 2.1f, has ? 255 : can ? 225 : 90);
            if (can) {
                ring.setColor(0xFFFFD24A);
                ring.setAlpha(Math.round(120 + 135 * pulse * (left > 0 ? 1 : .3f)));
                ring.setStrokeWidth(Math.max(dp(1.5f), r * .12f));
                canvas.drawCircle(x, y, r * 1.12f, ring);
            } else if (has && node.id != 0) {
                ring.setColor(colour);
                ring.setStrokeWidth(Math.max(dp(1.5f), r * .1f));
                canvas.drawCircle(x, y, r * 1.1f, ring);
            }
            if (node.id == on) {
                ring.setColor(Color.WHITE);
                ring.setStrokeWidth(Math.max(dp(2.5f), r * .14f));
                canvas.drawCircle(x, y, r * 1.3f, ring);
            }
            // An active in a slot: its slot's number on it.
            for (int slot = 0; slot < RunSkills.SLOTS; slot++) {
                if (facts.slots[slot] != node.id || !has || node.type != RunSkills.NOTABLE) continue;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFE0362F);
                float bx = x + r * .8f, by = y - r * .8f, br = Math.max(dp(7), r * .32f);
                canvas.drawCircle(bx, by, br, paint);
                text.setColor(Color.WHITE);
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(br * 1.3f);
                canvas.drawText(String.valueOf(slot + 1), bx, by + br * .45f, text);
            }
        }
        // Each path's emblem past its keystone, with its name under it.
        for (int path = 0; path < RunSkills.PATHS; path++) {
            double angle = path * Math.PI * 2 / RunSkills.PATHS;
            float x = sx((float) Math.cos(angle) * EMBLEMS), y = sy((float) Math.sin(angle) * EMBLEMS), size = Math.max(dp(30), Math.min(dp(120), zoom * 5));
            if (x < -size || x > width + size || y < -size || y > height + size) continue;
            icon(canvas, path, x, y, size, 255);
            float words = Math.max(dp(9), Math.min(dp(15), zoom * .9f));
            text.setTextSize(words);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(lit(RunSkills.PATH_COLOURS[path], .45f));
            canvas.drawText(RunSkills.PATH_NAMES[path], x, y + size * .5f + words * 1.1f, text);
        }
        drawTop(canvas, safe, facts, left);
        drawCard(canvas, safe, facts, left);
        if (statsOpen) drawStats(canvas, safe, facts);
        if (asking) drawAsk(canvas, safe, facts);
    }

    private void plate(Canvas canvas, RectF where, float round, int colour) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x66000000);
        canvas.drawRoundRect(where.left, where.top + dp(2), where.right, where.bottom + dp(3), round, round, paint);
        paint.setColor(colour);
        canvas.drawRoundRect(where, round, round, paint);
    }

    private void button(Canvas canvas, RectF where, String word, boolean can, int colour) {
        plate(canvas, where, where.height() / 2, can ? colour : 0xFF3A3650);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(can ? Color.WHITE : 0xFF9A96B0);
        fitted(canvas, word, where.centerX(), where.centerY() + dp(5), dp(14), where.width() - dp(16));
    }

    private void fitted(Canvas canvas, String words, float x, float y, float size, float room) {
        text.setTextSize(size);
        float wide = text.measureText(words);
        if (wide > room && room > 0) text.setTextSize(size * room / wide);
        canvas.drawText(words, x, y, text);
    }

    private List<String> wrap(String words, float room, float size) {
        text.setTextSize(size);
        List<String> lines = new ArrayList<>();
        StringBuilder now = new StringBuilder();
        for (String word : words.split(" ")) {
            String tried = now.length() == 0 ? word : now + " " + word;
            if (text.measureText(tried) > room && now.length() > 0) {
                lines.add(now.toString());
                now = new StringBuilder(word);
            } else {
                now = new StringBuilder(tried);
            }
        }
        if (now.length() > 0) lines.add(now.toString());
        return lines;
    }

    /** The bar at the top: its name, the points she has, and the buttons. */
    private void drawTop(Canvas canvas, RectF safe, Facts facts, int left) {
        float pad = dp(10), tall = dp(48);
        top.set(safe.left + pad, safe.top + pad, safe.right - pad, safe.top + pad + tall);
        plate(canvas, top, dp(24), 0xEE221C3A);
        closeButton.set(top.right - dp(44), top.top + dp(4), top.right - dp(4), top.bottom - dp(4));
        plate(canvas, closeButton, closeButton.height() / 2, 0xFF3A3258);
        line.setColor(Color.WHITE);
        line.setStrokeWidth(dp(2.2f));
        float c = dp(7);
        canvas.drawLine(closeButton.centerX() - c, closeButton.centerY() - c, closeButton.centerX() + c, closeButton.centerY() + c, line);
        canvas.drawLine(closeButton.centerX() + c, closeButton.centerY() - c, closeButton.centerX() - c, closeButton.centerY() + c, line);
        boolean narrow = top.width() < dp(420);
        // (Taking back is there only once something is taken.)
        respecButton.set(closeButton.left - dp(6) - dp(narrow ? 66 : 92), top.top + dp(6), closeButton.left - dp(6), top.bottom - dp(6));
        if (RunSkills.spent(facts.taken) > 0) button(canvas, respecButton, narrow ? "Respec" : "Take back", true, 0xFF8A4A9A);
        else respecButton.setEmpty();
        float statsRight = (respecButton.isEmpty() ? closeButton.left : respecButton.left) - dp(6);
        statsButton.set(statsRight - dp(narrow ? 54 : 64), top.top + dp(6), statsRight, top.bottom - dp(6));
        button(canvas, statsButton, "Stats", true, statsOpen ? 0xFFE0475A : 0xFF3F5FC0);
        icon(canvas, NODE_ICONS + 11, top.left + dp(24), top.centerY(), dp(38), 255);
        float room = statsButton.left - dp(8) - (top.left + dp(48));
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(Color.WHITE);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, narrow ? "Skills" : "Skill tree", top.left + dp(48), top.top + dp(21), dp(16), room);
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        text.setColor(left > 0 ? 0xFFFFD24A : 0xFFB8B2D8);
        fitted(canvas, (left > 0 ? left + (narrow ? (left == 1 ? " point" : " points") : left == 1 ? " point to spend" : " points to spend") : narrow ? "None left" : "No points left") + (narrow ? " · Lv " : "  ·  level ") + facts.level, top.left + dp(48), top.top + dp(38), dp(12), room);
        // Closer and further off, at the bottom right.
        float side = dp(44);
        float bottom = (on >= 0 && !card.isEmpty() ? card.top : safe.bottom) - dp(12);
        zoomIn.set(safe.right - pad - side, bottom - side * 2 - dp(8), safe.right - pad, bottom - side - dp(8));
        zoomOut.set(safe.right - pad - side, bottom - side, safe.right - pad, bottom);
        for (RectF zoomButton : new RectF[]{zoomIn, zoomOut}) {
            plate(canvas, zoomButton, side / 2, 0xEE2B2448);
            line.setColor(Color.WHITE);
            line.setStrokeWidth(dp(2.5f));
            canvas.drawLine(zoomButton.centerX() - dp(8), zoomButton.centerY(), zoomButton.centerX() + dp(8), zoomButton.centerY(), line);
            if (zoomButton == zoomIn) canvas.drawLine(zoomButton.centerX(), zoomButton.centerY() - dp(8), zoomButton.centerX(), zoomButton.centerY() + dp(8), line);
        }
        if (on < 0) {
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(0xAAFFFFFF);
            fitted(canvas, "Touch a node to see it  ·  drag to look round  ·  pinch to come closer", safe.centerX() - side / 2, safe.bottom - dp(18), dp(12), safe.width() - side * 2 - dp(40));
        }
    }

    /** The card of the node looked at. */
    private void drawCard(Canvas canvas, RectF safe, Facts facts, int left) {
        card.setEmpty();
        act.setEmpty();
        for (RectF slot : slotButtons) slot.setEmpty();
        List<RunSkills.Node> nodes = RunSkills.tree();
        if (on < 0 || on >= nodes.size()) return;
        RunSkills.Node node = nodes.get(on);
        float wide = Math.min(safe.width() - dp(20), dp(480)), pad = dp(14), iconSide = dp(54);
        float textRoom = wide - pad * 3 - iconSide;
        List<String> lines = wrap(RunSkills.does(node), textRoom, dp(13));
        RunSkills.Notable notable = RunSkills.notableAt(on);
        boolean has = on == 0 || facts.taken.get(on), can = RunSkills.canTake(facts.taken, on), active = notable != null && notable.kind == RunSkills.ACTIVE;
        float tall = pad + dp(20) + dp(18) + lines.size() * dp(17) + dp(10) + dp(40) + pad;
        card.set(safe.centerX() - wide / 2, safe.bottom - dp(10) - tall, safe.centerX() + wide / 2, safe.bottom - dp(10));
        plate(canvas, card, dp(20), 0xF2221C3A);
        int colour = node.path >= 0 ? lit(RunSkills.PATH_COLOURS[node.path], .35f) : 0xFFFFE9A8;
        ring.setColor(colour);
        ring.setStrokeWidth(dp(2));
        canvas.drawRoundRect(card, dp(20), dp(20), ring);
        icon(canvas, iconOf(node), card.left + pad + iconSide / 2, card.top + pad + iconSide / 2, iconSide, 255);
        if (node.path >= 0) icon(canvas, node.path, card.left + pad + iconSide * .85f, card.top + pad + iconSide * .85f, iconSide * .5f, 255);
        float x = card.left + pad * 2 + iconSide, y = card.top + pad;
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(Color.WHITE);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, RunSkills.title(node), x, y + dp(15), dp(16), textRoom);
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        text.setColor(colour);
        fitted(canvas, RunSkills.kindWords(node), x, y + dp(33), dp(11.5f), textRoom);
        text.setColor(0xFFE8E4FF);
        text.setTextSize(dp(13));
        for (int index = 0; index < lines.size(); index++) canvas.drawText(lines.get(index), x, y + dp(52) + index * dp(17), text);
        float buttonTop = card.bottom - pad - dp(40);
        if (on == 0) return;
        if (has && active) {
            // Taken: which slot it is in, or a slot to put it in.
            float each = (wide - pad * 2 - dp(8) * 2) / 3;
            for (int slot = 0; slot < RunSkills.SLOTS; slot++) {
                slotButtons[slot].set(card.left + pad + slot * (each + dp(8)), buttonTop, card.left + pad + slot * (each + dp(8)) + each, buttonTop + dp(40));
                boolean here = facts.slots[slot] == on;
                RunSkills.Notable in = RunSkills.slotted(facts.taken, facts.slots).get(slot);
                button(canvas, slotButtons[slot], here ? "In slot " + (slot + 1) : in == null ? "Slot " + (slot + 1) : "Slot " + (slot + 1) + " (swap)", true, here ? 0xFFE0362F : 0xFF3F5FC0);
            }
        } else if (has) {
            act.set(card.left + pad, buttonTop, card.right - pad, buttonTop + dp(40));
            button(canvas, act, "Taken", false, 0);
            act.setEmpty();
        } else {
            act.set(card.left + pad, buttonTop, card.right - pad, buttonTop + dp(40));
            String word = !can ? "Take one next to it first" : left <= 0 ? "No points left: her next level gives one" : "Take it  ·  1 point";
            button(canvas, act, word, can && left > 0, 0xFFE0475A);
            if (!can || left <= 0) act.setEmpty();
        }
    }

    /** Her stats, and what each comes to. */
    private void drawStats(Canvas canvas, RectF safe, Facts facts) {
        RunSkills.Built built = facts.built;
        List<String[]> rows = new ArrayList<>();
        int most = RunSkills.mostHp(facts.hearts, built), intel = RunSkills.intelligence(built, 0), defence = RunSkills.defencePercent(built.effects[RunSkills.E_DEF_PCT]);
        int attack = built.stats[RunSkills.ATK] + built.effects[RunSkills.E_ATK_PCT];
        rows.add(new String[]{"HP", most + " at most: " + facts.hearts + " hearts of " + RunSkills.HEART + (built.stats[RunSkills.HP] != 0 ? ", +" + built.stats[RunSkills.HP] + " from the tree" : "")
                + (built.effects[RunSkills.E_HP_PCT] != 0 ? ", " + signed(built.effects[RunSkills.E_HP_PCT]) + "%" : "")});
        rows.add(new String[]{"DEF", built.stats[RunSkills.DEF] + "  ·  each hurt " + built.stats[RunSkills.DEF] + " HP less" + (defence != 0 ? ", and " + Math.abs(defence) + "% " + (defence > 0 ? "less" : "more") : "")});
        rows.add(new String[]{"INT", intel + "  ·  +" + intel + "% energy back, +" + intel + "% blows with books, the tablet, staffs and wands"});
        rows.add(new String[]{"ATK", built.stats[RunSkills.ATK] + "  ·  " + signed(attack) + "% to every blow"});
        rows.add(new String[]{"MP", (int) PlusRun.ENERGY_MOST + " energy at most, back " + (int) PlusRun.ENERGY_BACK + " a second" + (intel + built.effects[RunSkills.E_ENERGY] != 0
                ? " (" + signed(intel + built.effects[RunSkills.E_ENERGY]) + "%)" : "") + "; a special costs " + (int) PlusRun.SPECIAL_COST + ", an active " + (int) RunSkills.ACTIVE_COST});
        int dp = built.effects[RunSkills.E_DP];
        rows.add(new String[]{"DP", dp > 0 ? dp + "% of energy spent becomes divine power; at " + (int) PlusRun.DP_MOST + ", her blows count three times for " + (int) PlusRun.DIVINE_LASTS + " seconds"
                : "Divine power comes from some notables, and from their actives while called on"});
        StringBuilder more = new StringBuilder();
        for (int effect = 0; effect < RunSkills.E_EFFECTS; effect++) {
            if (effect == RunSkills.E_ATK_PCT || effect == RunSkills.E_DEF_PCT || effect == RunSkills.E_HP_PCT || effect == RunSkills.E_DP || built.effects[effect] == 0) continue;
            more.append(more.length() == 0 ? "" : ",  ").append(signed(built.effects[effect])).append(RunSkills.EFFECT_WORDS[effect]);
        }
        rows.add(new String[]{"Passives", more.length() == 0 ? "None taken yet" : more.toString()});
        int actives = built.actives.size(), slotted = 0;
        for (RunSkills.Notable one : RunSkills.slotted(facts.taken, facts.slots)) if (one != null) slotted++;
        rows.add(new String[]{"Actives", actives + " taken, " + slotted + " in her slots"});
        rows.add(new String[]{"Points", RunSkills.spent(facts.taken) + " spent of " + facts.points + " (one a level, one more each tenth level)"});
        float wide = Math.min(safe.width() - dp(24), dp(520)), pad = dp(16), labelWide = dp(64), room = wide - pad * 2 - labelWide;
        List<List<String>> wrapped = new ArrayList<>();
        float tall = pad + dp(28);
        for (String[] row : rows) {
            List<String> lines = wrap(row[1], room, dp(12.5f));
            wrapped.add(lines);
            tall += lines.size() * dp(16) + dp(8);
        }
        tall += pad;
        tall = Math.min(tall, safe.height() - dp(80));
        statsCard.set(safe.centerX() - wide / 2, top.bottom + dp(10), safe.centerX() + wide / 2, top.bottom + dp(10) + tall);
        plate(canvas, statsCard, dp(20), 0xF5221C3A);
        canvas.save();
        canvas.clipRect(statsCard);
        float y = statsCard.top + pad + dp(16);
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(Color.WHITE);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, "Her stats  ·  level " + facts.level, statsCard.left + pad, y, dp(16), wide - pad * 2);
        y += dp(18);
        int[] colours = {0xFFFF6F7A, 0xFF6FB8FF, 0xFFB98CFF, 0xFFFFB04A, 0xFF5F9BFF, 0xFFFFE08A, 0xFFE8E4FF, 0xFFE8E4FF, 0xFFE8E4FF};
        for (int index = 0; index < rows.size(); index++) {
            y += dp(6);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setColor(colours[Math.min(index, colours.length - 1)]);
            fitted(canvas, rows.get(index)[0], statsCard.left + pad, y + dp(12), dp(12.5f), labelWide - dp(6));
            text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            text.setColor(0xFFE8E4FF);
            text.setTextSize(dp(12.5f));
            for (String one : wrapped.get(index)) {
                canvas.drawText(one, statsCard.left + pad + labelWide, y + dp(12), text);
                y += dp(16);
            }
            y += dp(2);
        }
        canvas.restore();
    }

    private static String signed(int figure) { return (figure > 0 ? "+" : "") + figure; }

    /** Whether to take every point back. */
    private void drawAsk(Canvas canvas, RectF safe, Facts facts) {
        float wide = Math.min(safe.width() - dp(40), dp(340)), pad = dp(18), tall = dp(150);
        askCard.set(safe.centerX() - wide / 2, safe.centerY() - tall / 2, safe.centerX() + wide / 2, safe.centerY() + tall / 2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x88000000);
        canvas.drawRect(0, 0, width, height, paint);
        plate(canvas, askCard, dp(20), 0xFF2A2246);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(Color.WHITE);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        fitted(canvas, "Take back all " + RunSkills.spent(facts.taken) + " points?", askCard.centerX(), askCard.top + pad + dp(16), dp(16), wide - pad * 2);
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        boolean can = facts.freeRespec || facts.bank >= facts.respecCost;
        text.setColor(can ? 0xFFFFD24A : 0xFFFF8A8A);
        fitted(canvas, facts.freeRespec ? "The first time is free" : "It costs + " + facts.respecCost + "  (she has + " + facts.bank + ")", askCard.centerX(), askCard.top + pad + dp(42), dp(13), wide - pad * 2);
        float each = (wide - pad * 2 - dp(10)) / 2;
        askNo.set(askCard.left + pad, askCard.bottom - pad - dp(42), askCard.left + pad + each, askCard.bottom - pad);
        askYes.set(askNo.right + dp(10), askNo.top, askNo.right + dp(10) + each, askNo.bottom);
        button(canvas, askNo, "Keep them", true, 0xFF3F5FC0);
        button(canvas, askYes, "Take back", can, 0xFF8A4A9A);
        if (!can) askYes.setEmpty();
    }
}
