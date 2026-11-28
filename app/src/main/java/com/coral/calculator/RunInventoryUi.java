package com.coral.calculator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Her inventory, drawn and touched: five places for what she has on her (main weapon, secondary weapon, armour and two accessories, the
 * empty ones as faint outlines of what goes in them) around a small picture of her in the look she has chosen; a grid of everything she has
 * in tabs and in the order she picks; and the card of the thing looked at, with what it does, how it compares with what she has on, and the
 * buttons that put it on or take it off. Tall screens have the places on top, the grid under them and the card as a sheet at the foot;
 * wide ones the places at the left, the grid in the middle and the card at the right. What is kept, and the rules, are {@link RunInventory}.
 */
final class RunInventoryUi {
    private final RunGame g;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG), line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private boolean inLand;
    private int tab, sort, selected;
    private boolean expanded, keyed, slotFocus;
    private int focusCell, focusSlot;
    private float scroll, scrollMax;
    private PlusRun.Perks kept = new PlusRun.Perks();
    private List<RunInventory.Cell> cells = new ArrayList<>();

    private final RectF panel = new RectF(), equip = new RectF(), figure = new RectF(), grid = new RectF(), detail = new RectF(), close = new RectF(),
            smithy = new RectF(), more = new RectF(), shut = new RectF();
    private final RectF[] slots = new RectF[RunInventory.SLOTS], tabs = new RectF[4], sorts = new RectF[3], acts = new RectF[3];
    private final int[] actions = new int[3];
    private int columns = 4;
    private float side = 50, gap = 6;
    private boolean sheet, landscape;
    private float downX, downY, lastY;
    private boolean downInGrid, dragging;

    private static final int NONE = 0, AS_MAIN = 1, AS_SECOND = 2, WEAR = 3, OFF = 4;
    private static final int[] SLOT_ORDER = {RunInventory.MAIN, RunInventory.SECOND, RunInventory.ARMOUR, RunInventory.FIRST, RunInventory.LAST};

    RunInventoryUi(RunGame game) {
        g = game;
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        fill.setStyle(Paint.Style.FILL);
        for (int index = 0; index < slots.length; index++) slots[index] = new RectF();
        for (int index = 0; index < tabs.length; index++) tabs[index] = new RectF();
        for (int index = 0; index < sorts.length; index++) sorts[index] = new RectF();
        for (int index = 0; index < acts.length; index++) acts[index] = new RectF();
    }

    private float dp(float value) { return g.dp(value); }

    /** It is opened: in a land (what she has there) or from the map or the pause (what is kept). The tab and the order are as she left them. */
    void open(boolean inLand) {
        this.inLand = inLand;
        tab = Math.max(0, Math.min(3, g.session.tally("run_inv_tab")));
        sort = Math.max(0, Math.min(2, g.session.tally("run_inv_sort")));
        selected = 0;
        expanded = false;
        keyed = false;
        slotFocus = false;
        focusCell = 0;
        scroll = 0;
        kept = g.kept();
    }

    /** Back: the card is closed first. Whether that was all it did. */
    boolean back() {
        if (selected != 0) {
            selected = 0;
            expanded = false;
            return true;
        }
        return false;
    }

    private void setTab(int one) {
        tab = (one % 4 + 4) % 4;
        g.session.keep("run_inv_tab", tab);
        scroll = 0;
        focusCell = 0;
    }

    private void setSort(int one) {
        sort = (one % 3 + 3) % 3;
        g.session.keep("run_inv_sort", sort);
        scroll = 0;
        focusCell = 0;
    }

    private void changed(int hold) {
        g.weaponsChanged(hold);
        kept = g.kept();
    }

    private void worn(RunLoot.Worn now) {
        g.wearNow(now);
        kept = g.kept();
    }

    // What is done

    private void act(int what) {
        if (selected == 0) return;
        RunLoot.Worn worn = g.wornNow();
        if (what == AS_MAIN) {
            if (RunInventory.setMain(g.session, selected)) {
                int kind = RunInventory.kindOf(selected);
                for (int index = 0; index < RunArms.MAINS.length; index++) if (RunArms.MAINS[index] == kind) g.session.keep("run_main", index);
                changed(selected);
            }
        } else if (what == AS_SECOND) {
            if (RunInventory.setSecond(g.session, selected)) changed(0);
        } else if (what == WEAR) {
            List<Integer> bag = RunLoot.bag(g.session.note("run_bag"));
            if (RunInventory.canWear(worn, bag, selected)) worn(RunInventory.equip(worn, selected));
        } else if (what == OFF) {
            int slot = RunInventory.slotOf(g.session, selected);
            if (slot == RunInventory.SECOND) {
                if (RunInventory.clearSecond(g.session)) changed(0);
            } else if (slot >= RunInventory.ARMOUR) {
                worn(RunInventory.unequip(worn, slot));
            }
        }
    }

    /** What Enter does with what is looked at: a weapon becomes the main one; what is worn comes off, and the rest goes on. */
    private int primary(int entry) {
        int slot = RunInventory.slotOf(g.session, entry);
        if (RunInventory.weapon(entry)) return slot == RunInventory.MAIN ? NONE : AS_MAIN;
        if (slot >= 0) return OFF;
        return WEAR;
    }

    // Touch and keys

    void touch(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            downX = x;
            downY = lastY = y;
            dragging = false;
            keyed = false;
            downInGrid = grid.contains(x, y) && !(sheet && detail.contains(x, y));
            if (!downInGrid) press(x, y);
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (downInGrid) {
                if (!dragging && Math.abs(y - downY) > dp(8)) dragging = true;
                if (dragging) scroll = Math.max(0, Math.min(scrollMax, scroll - (y - lastY)));
                lastY = y;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (downInGrid && !dragging) pick(x, y);
            downInGrid = false;
        } else if (action == MotionEvent.ACTION_CANCEL) {
            downInGrid = false;
        }
    }

    void wheel(float turned) {
        scroll = Math.max(0, Math.min(scrollMax, scroll - turned * dp(64)));
    }

    private void choose(int entry) {
        selected = entry;
        expanded = false;
        g.audio.play("tap", .5f);
    }

    private void pick(float x, float y) {
        float across = x - grid.left, down = y - grid.top + scroll;
        int col = (int) (across / (side + gap)), row = (int) (down / (side + gap));
        if (across < 0 || down < 0 || col >= columns || across - col * (side + gap) > side || down - row * (side + gap) > side) return;
        int index = row * columns + col;
        if (index < 0 || index >= cells.size()) return;
        focusCell = index;
        slotFocus = false;
        choose(cells.get(index).entry);
    }

    private void press(float x, float y) {
        if (sheet && detail.contains(x, y)) {
            if (!shut.isEmpty() && shut.contains(x, y)) {
                back();
                g.audio.play("tap", .3f);
                return;
            }
            if (!more.isEmpty() && more.contains(x, y)) {
                expanded = !expanded;
                g.audio.play("tap", .4f);
                return;
            }
            for (int index = 0; index < acts.length; index++) {
                if (actions[index] != NONE && acts[index].contains(x, y)) {
                    act(actions[index]);
                    return;
                }
            }
            return;
        }
        if (close.contains(x, y)) {
            g.closePack();
            return;
        }
        if (!smithy.isEmpty() && smithy.contains(x, y)) {
            g.closePack();
            g.openSmithOfWorn();
            return;
        }
        for (int index = 0; index < tabs.length; index++) {
            if (tabs[index].contains(x, y)) {
                setTab(index);
                g.audio.play("tap", .5f);
                return;
            }
        }
        for (int index = 0; index < sorts.length; index++) {
            if (sorts[index].contains(x, y)) {
                setSort(index);
                g.audio.play("tap", .5f);
                return;
            }
        }
        for (int index = 0; index < slots.length; index++) {
            if (slots[index].contains(x, y)) {
                look(index);
                return;
            }
        }
        if (!panel.contains(x, y)) g.closePack();
    }

    /** A place is looked at: its thing is shown; an empty one shows what could go in it. */
    private void look(int slot) {
        int entry = RunInventory.inSlot(g.session, slot);
        if (entry != 0) {
            choose(entry);
            return;
        }
        selected = 0;
        setTab(slot <= RunInventory.SECOND ? RunInventory.WEAPONS : slot == RunInventory.ARMOUR ? RunInventory.ARMOURS : RunInventory.ACCESSORIES);
        g.audio.play("tap", .5f);
    }

    /** A key: the arrows (or W, A, S, D) move over the places and the things, Enter looks or equips, E makes a weapon the secondary one, K takes it off, Q or Tab changes the tab, L the order. */
    void key(int fresh, int keyCode) {
        if (fresh == 0) return;
        keyed = true;
        boolean left = keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_A, right = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_D;
        boolean up = keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_W, down = keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_S;
        boolean confirm = keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_SPACE
                || keyCode == KeyEvent.KEYCODE_J;
        if ((fresh & RunKeys.SWAP) != 0) {
            setTab(tab + 1);
            g.audio.play("tap", .4f);
            return;
        }
        if ((fresh & RunKeys.DASH) != 0) {
            setSort(sort + 1);
            g.audio.play("tap", .4f);
            return;
        }
        if ((fresh & RunKeys.EXTRA) != 0) {
            if (selected != 0 && RunInventory.weapon(selected)) act(AS_SECOND);
            return;
        }
        if ((fresh & RunKeys.SPECIAL) != 0) {
            if (selected != 0 && RunInventory.slotOf(g.session, selected) >= RunInventory.SECOND) act(OFF);
            return;
        }
        int last = Math.max(0, cells.size() - 1);
        if (slotFocus) {
            if (left) focusSlot = Math.max(0, focusSlot - 1);
            else if (right) focusSlot = Math.min(SLOT_ORDER.length - 1, focusSlot + 1);
            else if (down) slotFocus = false;
            else if (confirm) look(SLOT_ORDER[focusSlot]);
            return;
        }
        if (left) focusCell = Math.max(0, focusCell - 1);
        else if (right) focusCell = Math.min(last, focusCell + 1);
        else if (down) focusCell = Math.min(last, focusCell + columns);
        else if (up) {
            if (focusCell - columns < 0) slotFocus = true;
            else focusCell -= columns;
        } else if (confirm && !cells.isEmpty()) {
            int entry = cells.get(Math.min(last, focusCell)).entry;
            if (entry == selected) act(primary(entry));
            else choose(entry);
        }
    }

    // Drawing

    private boolean isDark() { return g.dark; }

    private void say(Canvas canvas, String words, float x, float y, float size, int colour, boolean bold, Paint.Align align, float room) {
        g.text.setTextAlign(align);
        g.text.setColor(colour);
        g.text.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        g.fitted(canvas, words, x, y, dp(size), room);
        g.text.setTypeface(Typeface.DEFAULT);
    }

    private static String name(int entry) { return RunInventory.plain(entry) ? RunArms.NAMES[RunInventory.kindOf(entry)] : RunLoot.name(entry); }

    void draw(Canvas canvas) {
        canvas.drawColor(0x99000000);
        List<Integer> bag = RunLoot.bag(g.session.note("run_bag"));
        RunLoot.Worn worn = g.wornNow();
        int owned = RunInventory.owned(g.session) | g.packedKinds(inLand);
        cells = RunInventory.cells(owned, bag, tab, sort);
        if (cells.size() > 0) focusCell = Math.max(0, Math.min(cells.size() - 1, focusCell));
        RectF safe = g.safe;
        boolean wide = safe.width() > safe.height() * 1.1f;
        float margin = dp(8), pad = dp(12);
        float pw = Math.min(safe.width() - margin * 2, dp(wide ? 1040 : 620)), ph = Math.min(safe.height() - margin * 2, dp(wide ? 600 : 920));
        panel.set(safe.centerX() - pw / 2, safe.centerY() - ph / 2, safe.centerX() + pw / 2, safe.centerY() + ph / 2);
        g.plate(canvas, panel, dp(22), 255);
        sheet = selected != 0;
        landscape = wide;
        // The head: the name, and the ways out.
        float headTop = panel.top + pad;
        close.set(panel.right - pad - dp(32), headTop, panel.right - pad, headTop + dp(32));
        drawClose(canvas, close);
        smithy.setEmpty();
        if (!inLand) {
            smithy.set(close.left - dp(8) - dp(124), headTop + dp(2), close.left - dp(8), headTop + dp(30));
            g.smithButton(canvas, smithy, "What she wears  ›", true, 0xFF3F7FC4);
        }
        say(canvas, "Inventory", panel.left + pad, headTop + dp(23), 20, g.ink, true, Paint.Align.LEFT, dp(120));
        say(canvas, bag.size() + " of " + RunLoot.BAG + " found", panel.left + pad + dp(112), headTop + dp(22), 11.5f, g.soft, false, Paint.Align.LEFT,
                (inLand ? close.left : smithy.left) - dp(8) - (panel.left + pad + dp(112)));
        float top = headTop + dp(40);
        float equipLeft, equipRight, gridLeft, gridRight, gridTop, gridBottom, detailLeft = 0, detailRight = 0;
        if (wide) {
            float column = Math.min(dp(320), panel.width() * .34f);
            equipLeft = panel.left + pad;
            equipRight = equipLeft + column;
            float detailWide = sheet ? Math.min(dp(270), panel.width() * .28f) : 0;
            gridLeft = equipRight + pad;
            gridRight = panel.right - pad - (sheet ? detailWide + pad : 0);
            detailLeft = gridRight + pad;
            detailRight = panel.right - pad;
            gridBottom = panel.bottom - pad;
            float eqBottom = drawEquipment(canvas, equipLeft, top, equipRight, worn, bag);
            drawSetLine(canvas, worn, equipLeft, eqBottom + dp(6), equipRight, gridBottom);
            gridTop = drawTabs(canvas, gridLeft, top, gridRight);
            detail.set(detailLeft, top, detailRight, panel.bottom - pad);
            grid.set(gridLeft, gridTop, gridRight, gridBottom - (keyed ? dp(16) : 0));
            hint(canvas, gridLeft, gridBottom, gridRight);
        } else {
            equipLeft = panel.left + pad;
            equipRight = panel.right - pad;
            float eqBottom = drawEquipment(canvas, equipLeft, top, equipRight, worn, bag);
            float setBottom = drawSetLine(canvas, worn, equipLeft, eqBottom + dp(4), equipRight, eqBottom + dp(40));
            gridTop = drawTabs(canvas, equipLeft, setBottom + dp(2), equipRight);
            gridBottom = panel.bottom - pad;
            grid.set(equipLeft, gridTop, equipRight, gridBottom - (keyed ? dp(16) : 0));
            hint(canvas, equipLeft, gridBottom, equipRight);
            float sheetTall = expanded ? panel.height() * .8f : Math.min(dp(212), panel.height() * .4f);
            detail.set(panel.left + dp(4), panel.bottom - dp(4) - sheetTall, panel.right - dp(4), panel.bottom - dp(4));
        }
        drawGrid(canvas, worn);
        if (sheet) drawDetail(canvas, wide, worn, bag);
        else for (RectF one : acts) one.setEmpty();
    }

    private void hint(Canvas canvas, float left, float bottom, float right) {
        if (!keyed) return;
        say(canvas, "Arrows move  ·  Enter picks, then equips  ·  E secondary  ·  K off  ·  Q tab  ·  L order  ·  Esc back", left, bottom - dp(3), 10, g.soft, false,
                Paint.Align.LEFT, right - left);
    }

    private void drawClose(Canvas canvas, RectF where) {
        fill.setColor(isDark() ? 0xFF3A3F45 : 0xFFECEEF0);
        canvas.drawCircle(where.centerX(), where.centerY(), where.width() / 2, fill);
        line.setColor(g.ink);
        line.setStrokeWidth(dp(2));
        float arm = where.width() * .17f;
        canvas.drawLine(where.centerX() - arm, where.centerY() - arm, where.centerX() + arm, where.centerY() + arm, line);
        canvas.drawLine(where.centerX() - arm, where.centerY() + arm, where.centerX() + arm, where.centerY() - arm, line);
    }

    /** The five places around her picture. Returns the bottom of it. */
    private float drawEquipment(Canvas canvas, float left, float top, float right, RunLoot.Worn worn, List<Integer> bag) {
        float width = right - left, gap = dp(8);
        float portraitWide = Math.max(dp(60), Math.min(dp(118), width * .3f));
        float slot = Math.min(dp(58), (width - portraitWide - gap * 4) / 2);
        float inner = dp(8), tall = slot * 3 + gap * 2 + inner * 2;
        equip.set(left, top, right, top + tall);
        fill.setColor(isDark() ? 0xFF25292C : 0xFFF2F3F5);
        canvas.drawRoundRect(equip, dp(18), dp(18), fill);
        float leftX = left + gap, rightX = right - gap - slot, rowY = top + inner;
        // (The weapons at the left, between the rows of the others.)
        slots[RunInventory.MAIN].set(leftX, rowY + (slot + gap) * .5f, leftX + slot, rowY + (slot + gap) * .5f + slot);
        slots[RunInventory.SECOND].set(leftX, rowY + (slot + gap) * 1.5f, leftX + slot, rowY + (slot + gap) * 1.5f + slot);
        for (int index = 0; index < 3; index++) slots[RunInventory.ARMOUR + index].set(rightX, rowY + index * (slot + gap), rightX + slot, rowY + index * (slot + gap) + slot);
        figure.set(leftX + slot + gap, top + inner, rightX - gap, top + tall - inner);
        drawFigure(canvas, figure);
        for (int index = 0; index < RunInventory.SLOTS; index++) {
            int entry = RunInventory.inSlot(g.session, index);
            boolean picked = entry != 0 && entry == selected;
            boolean focus = keyed && slotFocus && SLOT_ORDER[focusSlot] == index;
            if (entry == 0) emptySlot(canvas, slots[index], index, focus);
            else drawEntry(canvas, entry, slots[index], picked, focus, false);
        }
        return equip.bottom;
    }

    /** What a set adds. Returns the bottom of the line. */
    private float drawSetLine(Canvas canvas, RunLoot.Worn worn, float left, float top, float right, float limit) {
        String words = worn.set >= 2 ? RunLoot.THEMES[Math.min(11, RunLoot.theme(worn.armour))] + " set of three: +1 shield, 10% harder blows, life back"
                : worn.set == 1 ? RunLoot.THEMES[Math.min(11, RunLoot.theme(worn.armour))] + " set of two: +1 shield piece" : "Armour and an accessory of one chapter make a set";
        g.text.setTextSize(dp(11));
        List<String> lines = g.wrap(words, right - left);
        g.text.setTextAlign(Paint.Align.LEFT);
        g.text.setColor(worn.set > 0 ? 0xFF3F7FC4 : g.soft);
        g.text.setTypeface(Typeface.DEFAULT);
        float y = top + dp(12);
        for (int index = 0; index < Math.min(2, lines.size()); index++) {
            g.text.setTextSize(dp(11));
            canvas.drawText(lines.get(index), left + dp(2), y, g.text);
            y += dp(14);
        }
        return Math.max(y, top + dp(14));
    }

    /** Her, in the look she has chosen, at the middle of the places. */
    private void drawFigure(Canvas canvas, RectF where) {
        fill.setColor(isDark() ? 0xFF33292D : 0xFFFDEBEC);
        canvas.drawRoundRect(where, dp(16), dp(16), fill);
        Bitmap pet = g.herPet();
        if (pet != null) {
            int wide = pet.getWidth() / PetArt.COLUMNS, tall = pet.getHeight() / PetArt.FRAMES.length;
            Rect from = new Rect(0, tall * PetArt.IDLE, wide, tall * (PetArt.IDLE + 1));
            float room = dp(14);
            float scale = Math.min((where.width() - dp(6)) / wide, (where.height() - room - dp(4)) / tall);
            float w = wide * scale, h = tall * scale;
            RectF to = new RectF(where.centerX() - w / 2, where.top + dp(2), where.centerX() + w / 2, where.top + dp(2) + h);
            g.paint.setAlpha(255);
            canvas.drawBitmap(pet, from, to, g.paint);
        }
        say(canvas, RunVanity.MODE_NAMES[RunVanity.mode(RunVanity.on(g.session))], where.centerX(), where.bottom - dp(4), 9.5f, g.soft, false, Paint.Align.CENTER, where.width() - dp(6));
    }

    /** The tabs and the order, in two rows. Returns the top of what is under them. */
    private float drawTabs(Canvas canvas, float left, float top, float right) {
        float gapX = dp(6), across = (right - left - gapX * 3) / 4;
        for (int index = 0; index < 4; index++) {
            float x = left + index * (across + gapX);
            tabs[index].set(x, top, x + across, top + dp(30));
            g.smithButton(canvas, tabs[index], RunInventory.TABS[index], tab == index, CalculatorLayout.CORAL);
        }
        float y = top + dp(36);
        say(canvas, "Sort", left + dp(2), y + dp(17), 11.5f, g.soft, false, Paint.Align.LEFT, dp(40));
        float chip = Math.min(dp(96), (right - left - dp(46) - gapX * 2) / 3);
        for (int index = 0; index < 3; index++) {
            float x = left + dp(44) + index * (chip + gapX);
            sorts[index].set(x, y, x + chip, y + dp(24));
            g.smithButton(canvas, sorts[index], RunInventory.SORTS[index], sort == index, 0xFF8A5A33);
        }
        return y + dp(32);
    }

    private void drawGrid(Canvas canvas, RunLoot.Worn worn) {
        float width = grid.width();
        gap = dp(6);
        columns = Math.max(3, (int) ((width + gap) / (dp(54) + gap)));
        side = (width - gap * (columns - 1)) / columns;
        side = Math.min(side, dp(76));
        int rows = (cells.size() + columns - 1) / columns;
        float content = rows * (side + gap) - gap;
        // (A sheet at the foot lies over the end of the grid: it can be scrolled up past it.)
        float bottomRoom = sheet && !landscape ? Math.max(0, grid.bottom - detail.top) : 0;
        scrollMax = Math.max(0, content - grid.height() + bottomRoom);
        if (keyed && !slotFocus && !cells.isEmpty()) {
            float rowTop = focusCell / columns * (side + gap);
            if (rowTop < scroll) scroll = rowTop;
            else if (rowTop + side > scroll + grid.height() - bottomRoom) scroll = rowTop + side - grid.height() + bottomRoom;
        }
        scroll = Math.max(0, Math.min(scrollMax, scroll));
        canvas.save();
        canvas.clipRect(grid);
        float x0 = grid.left + (width - (side * columns + gap * (columns - 1))) / 2;
        for (int index = 0; index < cells.size(); index++) {
            float cx = x0 + (index % columns) * (side + gap), cy = grid.top + (index / columns) * (side + gap) - scroll;
            if (cy + side < grid.top || cy > grid.bottom) continue;
            RectF at = new RectF(cx, cy, cx + side, cy + side);
            int entry = cells.get(index).entry;
            drawEntry(canvas, entry, at, entry == selected, keyed && !slotFocus && index == focusCell, true);
        }
        canvas.restore();
        // The grid starts at its own left edge: the cells are laid from x0, so touches count from there.
        if (x0 != grid.left) {
            // (A narrower grid than its room is centred; the touch rule reads the same from the left edge of the room.)
            grid.left = x0;
        }
        if (cells.isEmpty()) {
            say(canvas, tab == RunInventory.ALL ? "Nothing yet: creatures drop things now and then" : "None of these yet", grid.centerX(), grid.top + dp(60), 13, g.soft, false,
                    Paint.Align.CENTER, grid.width());
        }
        if (scrollMax > 0) {
            float track = grid.height() - dp(8), bar = Math.max(dp(24), track * grid.height() / (grid.height() + scrollMax));
            float barTop = grid.top + dp(4) + (track - bar) * scroll / scrollMax;
            fill.setColor(isDark() ? 0x66FFFFFF : 0x55000000);
            canvas.drawRoundRect(grid.right - dp(3), barTop, grid.right, barTop + bar, dp(2), dp(2), fill);
        }
    }

    /** A thing in a square: in a frame of its rarity, with the work of the smith on it, and a tick if she has it on. */
    private void drawEntry(Canvas canvas, int entry, RectF where, boolean picked, boolean focus, boolean badges) {
        if (RunInventory.plain(entry)) {
            float round = where.width() * .2f;
            fill.setShader(null);
            fill.setColor(isDark() ? 0xFF3A3F45 : 0xFFF4F1EA);
            canvas.drawRoundRect(where, round, round, fill);
            line.setColor(picked ? CalculatorLayout.CORAL : isDark() ? 0xFF6A7075 : 0xFFC9C5BA);
            line.setStrokeWidth(dp(picked ? 3 : 1.8f));
            canvas.drawRoundRect(where, round, round, line);
            int kind = RunInventory.kindOf(entry);
            boolean upright = kind != PlusRun.GUN && kind != PlusRun.BOOK && kind < PlusRun.TABLET;
            g.paint.setAlpha(255);
            g.thing(canvas, kind, where.centerX(), where.centerY(), where.height() * (upright ? .8f : .62f), upright ? 35 : 0, false, false);
            g.paint.setAlpha(255);
        } else {
            g.lootFrame(canvas, entry, where, picked);
        }
        if (focus) {
            line.setColor(0xFF3F7FC4);
            line.setStrokeWidth(dp(2.5f));
            float out = dp(2.5f), round = where.width() * .2f + out;
            canvas.drawRoundRect(where.left - out, where.top - out, where.right + out, where.bottom + out, round, round, line);
        }
        if (!badges) return;
        if (RunInventory.weapon(entry)) {
            int level = kept.levels[RunInventory.kindOf(entry)];
            if (level > 0) {
                String word = "+" + level;
                g.text.setTextSize(dp(9.5f));
                float wide = g.text.measureText(word) + dp(8);
                RectF pill = new RectF(where.left + dp(3), where.bottom - dp(15), where.left + dp(3) + wide, where.bottom - dp(3));
                fill.setColor(0xCC20242A);
                canvas.drawRoundRect(pill, dp(6), dp(6), fill);
                say(canvas, word, pill.centerX(), pill.bottom - dp(3), 9.5f, Color.WHITE, true, Paint.Align.CENTER, wide);
            }
        }
        if (RunInventory.slotOf(g.session, entry) >= 0) tick(canvas, where.right - where.width() * .17f, where.top + where.width() * .17f, where.width() * .12f);
    }

    private void tick(Canvas canvas, float x, float y, float radius) {
        fill.setColor(CalculatorLayout.CORAL);
        canvas.drawCircle(x, y, radius, fill);
        line.setColor(Color.WHITE);
        line.setStrokeWidth(Math.max(dp(1.6f), radius * .28f));
        path.reset();
        path.moveTo(x - radius * .45f, y);
        path.lineTo(x - radius * .1f, y + radius * .4f);
        path.lineTo(x + radius * .5f, y - radius * .35f);
        canvas.drawPath(path, line);
    }

    /** An empty place: a tile with the faint outline of what goes in it. */
    private void emptySlot(Canvas canvas, RectF where, int slot, boolean focus) {
        float round = where.width() * .22f;
        fill.setColor(isDark() ? 0xFF2C3033 : 0xFFFFFFFF);
        canvas.drawRoundRect(where, round, round, fill);
        line.setColor(isDark() ? 0xFF4A4F54 : 0xFFD0D3D6);
        line.setStrokeWidth(dp(1.5f));
        canvas.drawRoundRect(where, round, round, line);
        if (focus) {
            line.setColor(0xFF3F7FC4);
            line.setStrokeWidth(dp(2.5f));
            float out = dp(2.5f);
            canvas.drawRoundRect(where.left - out, where.top - out, where.right + out, where.bottom + out, round + out, round + out, line);
        }
        silhouette(canvas, slot, where);
    }

    /** A sword, a dagger, a chestplate, a ring, an amulet: in outline, faint. */
    private void silhouette(Canvas canvas, int slot, RectF r) {
        float s = r.width(), x = r.left, y = r.top;
        line.setColor(isDark() ? 0x77C8CDD2 : 0x66707880);
        line.setStrokeWidth(Math.max(dp(1.6f), s * .045f));
        if (slot == RunInventory.MAIN) {
            blade(canvas, x + s * .78f, y + s * .22f, -.7071f, .7071f, s * .5f, s * .13f, s * .13f, s);
        } else if (slot == RunInventory.SECOND) {
            blade(canvas, x + s * .26f, y + s * .26f, .7071f, .7071f, s * .3f, s * .1f, s * .1f, s);
        } else if (slot == RunInventory.ARMOUR) {
            path.reset();
            path.moveTo(x + s * .22f, y + s * .27f);
            path.lineTo(x + s * .40f, y + s * .20f);
            path.quadTo(x + s * .50f, y + s * .31f, x + s * .60f, y + s * .20f);
            path.lineTo(x + s * .78f, y + s * .27f);
            path.lineTo(x + s * .72f, y + s * .44f);
            path.lineTo(x + s * .68f, y + s * .78f);
            path.quadTo(x + s * .50f, y + s * .88f, x + s * .32f, y + s * .78f);
            path.lineTo(x + s * .28f, y + s * .44f);
            path.close();
            canvas.drawPath(path, line);
        } else if (slot == RunInventory.FIRST) {
            canvas.drawCircle(x + s * .5f, y + s * .62f, s * .2f, line);
            path.reset();
            path.moveTo(x + s * .5f, y + s * .24f);
            path.lineTo(x + s * .58f, y + s * .34f);
            path.lineTo(x + s * .5f, y + s * .44f - s * .01f);
            path.lineTo(x + s * .42f, y + s * .34f);
            path.close();
            canvas.drawPath(path, line);
        } else {
            path.reset();
            path.moveTo(x + s * .24f, y + s * .2f);
            path.quadTo(x + s * .5f, y + s * .62f, x + s * .76f, y + s * .2f);
            canvas.drawPath(path, line);
            canvas.drawCircle(x + s * .5f, y + s * .66f, s * .13f, line);
        }
    }

    /** A blade from its tip along a direction, with its guard and grip. */
    private void blade(Canvas canvas, float tipX, float tipY, float dx, float dy, float length, float guard, float grip, float s) {
        float baseX = tipX + dx * length, baseY = tipY + dy * length;
        canvas.drawLine(tipX, tipY, baseX, baseY, line);
        canvas.drawLine(baseX - dy * guard, baseY + dx * guard, baseX + dy * guard, baseY - dx * guard, line);
        canvas.drawLine(baseX, baseY, baseX + dx * grip, baseY + dy * grip, line);
        canvas.drawCircle(baseX + dx * (grip + s * .03f), baseY + dy * (grip + s * .03f), s * .03f, line);
    }

    // The card of what is looked at

    /** The figures of a weapon: blows, reach, and strikes a second, with the find's share of them. */
    private float[] figures(int entry) {
        int kind = RunInventory.kindOf(entry), level = kept.levels[kind];
        boolean found = !RunInventory.plain(entry);
        float blows = RunArms.blows(kind, level) + (found ? RunArms.itemBlows(entry) : 0);
        float reach = RunArms.reach(kind, level) * (found ? RunArms.itemReach(entry) : 1);
        float rest = found ? RunArms.itemRest(entry) * (kind == PlusRun.GUN && RunArms.machine(entry) ? .4f : 1) : 1;
        return new float[]{blows, reach, RunArms.pace(kind, level) / rest};
    }

    private static String percent(float now, float then) {
        int change = Math.round((now / Math.max(.0001f, then) - 1) * 100);
        return (change > 0 ? "+" : "") + change + "%";
    }

    /** What it comes to against what she has on: a weapon against the main one (or the secondary one, if it is the main one); the rest against what it would replace. */
    private String compare(int entry, RunLoot.Worn worn) {
        if (RunInventory.weapon(entry)) {
            int main = RunInventory.main(g.session), second = RunInventory.second(g.session);
            int against = entry == main ? second : main;
            if (against == 0 || against == entry) return "";
            float[] now = figures(entry), then = figures(against);
            String reach = percent(now[1], then[1]), pace = percent(now[2], then[2]);
            int blows = Math.round(now[0] - then[0]);
            return "Against " + (against == main ? "the main weapon" : "the secondary") + " (" + name(against) + "): reach " + reach + ", rate " + pace
                    + (blows != 0 ? ", " + (blows > 0 ? "+" : "") + blows + (Math.abs(blows) == 1 ? " blow" : " blows") : "");
        }
        if (RunInventory.slotOf(g.session, entry) >= 0) return "";
        int type = RunLoot.type(entry);
        int replaced = type == RunLoot.ARMOUR ? worn.armour : worn.first == 0 ? 0 : worn.second == 0 ? 0 : RunLoot.rarity(worn.first) <= RunLoot.rarity(worn.second) ? worn.first : worn.second;
        if (replaced == 0) return "Goes on in an empty place";
        int better = RunLoot.rarity(entry) - RunLoot.rarity(replaced);
        return "Takes the place of " + RunLoot.name(replaced) + (better > 0 ? " (a rarer one)" : better < 0 ? " (a less rare one)" : " (as rare)");
    }

    private void drawDetail(Canvas canvas, boolean wide, RunLoot.Worn worn, List<Integer> bag) {
        for (RectF one : acts) one.setEmpty();
        float round = dp(18);
        // (A sheet at the foot, rounded at the top, or a card at the side.)
        g.plate(canvas, detail, round, 255);
        fill.setColor(isDark() ? 0xFF2A2E31 : 0xFFF7F8F9);
        canvas.drawRoundRect(detail, round, round, fill);
        line.setColor(isDark() ? 0xFF4A4F54 : 0xFFDADDE0);
        line.setStrokeWidth(dp(1.2f));
        canvas.drawRoundRect(detail, round, round, line);
        float pad = dp(12), left = detail.left + pad, right = detail.right - pad, y = detail.top + pad;
        int entry = selected, rarity = RunInventory.rarity(entry), type = RunInventory.type(entry);
        boolean weapon = RunInventory.weapon(entry);
        RectF picture = new RectF(left, y, left + dp(54), y + dp(54));
        drawEntry(canvas, entry, picture, false, false, false);
        shut.set(right - dp(28), detail.top + dp(6), right, detail.top + dp(34));
        drawClose(canvas, shut);
        more.setEmpty();
        float words = left + dp(64), room = shut.left - words - dp(4);
        int tint = rarity == RunLoot.WHITE ? g.ink : RunGame.mixed(g.rarityColour(rarity), isDark() ? Color.WHITE : Color.BLACK, isDark() ? .1f : .25f);
        say(canvas, name(entry), words, y + dp(17), 15, tint, true, Paint.Align.LEFT, room);
        int slot = RunInventory.slotOf(g.session, entry);
        String where = slot == RunInventory.MAIN ? "  ·  main weapon" : slot == RunInventory.SECOND ? "  ·  secondary" : slot >= 0 ? "  ·  worn" : "";
        int[] does = weapon ? null : RunLoot.does(entry);
        String kind;
        if (weapon) {
            kind = RunInventory.className(RunInventory.kindOf(entry));
        } else if (type == RunLoot.ARMOUR) {
            int lean = RunLoot.leansTo(RunLoot.base(entry));
            kind = "Armour  ·  " + (lean == RunLoot.ANY ? "every class" : RunLoot.CLASS_NAMES[lean]);
        } else {
            kind = "Accessory  ·  " + (does[0] == RunLoot.ANY ? "any weapon" : RunLoot.CLASS_NAMES[does[0]]);
        }
        say(canvas, RunLoot.RARITIES[rarity] + "  ·  " + kind + where, words, y + dp(35), 11.5f, g.soft, false, Paint.Align.LEFT, room);
        if (weapon) {
            int level = kept.levels[RunInventory.kindOf(entry)];
            say(canvas, level > 0 ? "Worked on to level " + (level + 1) : "Not worked on yet", words, y + dp(51), 11, g.soft, false, Paint.Align.LEFT, room);
        } else if (type != RunLoot.WEAPON) {
            int cls = type == RunLoot.ARMOUR ? RunLoot.leansTo(RunLoot.base(entry)) : does[0];
            if (cls != RunLoot.ANY) say(canvas, RunLoot.CLASS_HOLDS[cls], words, y + dp(51), 10.5f, g.soft, false, Paint.Align.LEFT, room);
        }
        y += dp(64);
        // The buttons are wanted first, to know how much room is left for the words.
        int[] wants = new int[3];
        String[] labels = new String[3];
        int[] colours = new int[3];
        boolean[] can = new boolean[3];
        int count = 0;
        if (weapon) {
            if (slot == RunInventory.MAIN) {
                wants[count] = NONE;
                labels[count] = "Main weapon";
                can[count++] = false;
            } else {
                wants[count] = AS_MAIN;
                labels[count] = "Equip as main";
                colours[count] = CalculatorLayout.CORAL;
                can[count++] = true;
            }
            if (slot == RunInventory.SECOND) {
                wants[count] = OFF;
                labels[count] = "Unequip";
                colours[count] = 0xFF8A5A33;
                can[count++] = true;
            } else {
                wants[count] = AS_SECOND;
                labels[count] = "Equip as secondary";
                colours[count] = 0xFF3F7FC4;
                can[count++] = true;
            }
        } else if (slot >= 0) {
            wants[count] = OFF;
            labels[count] = "Unequip";
            colours[count] = 0xFF8A5A33;
            can[count++] = true;
        } else {
            boolean ok = RunInventory.canWear(worn, bag, entry);
            wants[count] = ok ? WEAR : NONE;
            labels[count] = ok ? "Equip" : "All of it is worn";
            colours[count] = CalculatorLayout.CORAL;
            can[count++] = ok;
        }
        float buttonTall = dp(34), bottom = detail.bottom - pad;
        float buttonsTop;
        if (wide) {
            buttonsTop = bottom - count * (buttonTall + dp(6)) + dp(6);
            for (int index = 0; index < count; index++) acts[index].set(left, buttonsTop + index * (buttonTall + dp(6)), right, buttonsTop + index * (buttonTall + dp(6)) + buttonTall);
        } else {
            buttonsTop = bottom - buttonTall;
            float across = (right - left - dp(8) * (count - 1)) / count;
            for (int index = 0; index < count; index++) acts[index].set(left + index * (across + dp(8)), buttonsTop, left + index * (across + dp(8)) + across, buttonsTop + buttonTall);
        }
        for (int index = 0; index < count; index++) {
            actions[index] = can[index] ? wants[index] : NONE;
            g.smithButton(canvas, acts[index], labels[index], can[index], colours[index]);
        }
        for (int index = count; index < 3; index++) actions[index] = NONE;
        // What it does.
        float limit = buttonsTop - dp(6) - (!wide && !expanded ? dp(14) : 0);
        boolean full = wide || expanded;
        List<String> body = new ArrayList<>();
        List<Integer> shade = new ArrayList<>();
        if (weapon) {
            int kindNo = RunInventory.kindOf(entry), level = kept.levels[kindNo];
            float[] fig = figures(entry);
            boolean swung = RunArms.way(kindNo) == RunArms.SWUNG;
            float bite = RunArms.bite(kindNo, level);
            int blows = Math.round(fig[0]);
            body.add("Power: " + blows + (blows == 1 ? " blow" : " blows") + "  ·  reach " + RunGame.figure(fig[1]) + "  ·  " + RunGame.figure(fig[2]) + " a second");
            shade.add(CalculatorLayout.CORAL);
            body.add("Shield: " + RunGame.figure(bite) + (bite == 1 ? " piece" : " pieces") + " a blow"
                    + (RunArms.area(kindNo, level) > 0 ? "  ·  " + (kindNo == PlusRun.BOOK || kindNo == PlusRun.CANNON ? "burst " : "sweep ") + RunGame.figure(RunArms.area(kindNo, level)) + " squares" : "")
                    + (kindNo == PlusRun.TABLET ? "  ·  panel " + RunGame.figure(RunArms.panelWide(level)) + " by " + RunGame.figure(RunArms.panelTall(level)) : "")
                    + (kindNo == PlusRun.KEYS ? "  ·  fan of " + RunArms.fan(level) : ""));
            shade.add(g.ink);
            int holds = RunArms.holds(kindNo, kept.shots);
            if (holds > 0) {
                body.add(kindNo == PlusRun.GUN ? "Holds " + holds + " shots" : holds + " to a land");
                shade.add(g.soft);
            }
            if (!RunInventory.plain(entry) && RunArms.kindOf(entry) == PlusRun.WAND) {
                body.add("Its element: " + RunArms.ELEMENTS[RunArms.element(entry)]);
                shade.add(g.soft);
            }
        } else {
            for (int pair = 0; pair * 2 + 2 < does.length; pair++) {
                int effect = does[1 + pair * 2];
                boolean leaning = type == RunLoot.ARMOUR && effect == RunLoot.POWER;
                String said = RunLoot.said(effect, does[2 + pair * 2]) + (leaning ? " (" + (RunLoot.leansTo(RunLoot.base(entry)) == RunLoot.ANY ? "every class"
                        : RunLoot.CLASS_NAMES[RunLoot.leansTo(RunLoot.base(entry))]) + ")" : "");
                body.add("•  " + said);
                shade.add(pair == 0 ? CalculatorLayout.CORAL : g.ink);
            }
            if (type == RunLoot.ACCESSORY && rarity < RunLoot.RED && RunLoot.base(entry) != RunLoot.ROSE_OF_HALGAKOS) {
                body.add(rarity < RunLoot.GOLD ? "Gold and better do more besides" : "Red and better do more besides");
                shade.add(g.soft);
            }
        }
        String versus = compare(entry, worn);
        float lineTall = dp(16);
        float x = left;
        for (int index = 0; index < body.size(); index++) {
            g.text.setTypeface(Typeface.DEFAULT);
            g.text.setTextSize(dp(12));
            List<String> lines = g.wrap(body.get(index), right - left);
            for (int one = 0; one < lines.size(); one++) {
                if (y + lineTall > limit) break;
                say(canvas, lines.get(one), x, y + dp(12), 12, shade.get(index), false, Paint.Align.LEFT, right - left);
                y += lineTall;
            }
        }
        if (!versus.isEmpty()) {
            g.text.setTextSize(dp(11.5f));
            List<String> lines = g.wrap(versus, right - left);
            for (int one = 0; one < Math.min(full ? 3 : 2, lines.size()); one++) {
                if (y + lineTall > limit) break;
                say(canvas, lines.get(one), x, y + dp(11), 11.5f, 0xFF3F7FC4, false, Paint.Align.LEFT, right - left);
                y += dp(15);
            }
        }
        // And what it is for, in as many lines as there is room for.
        String told = description(entry);
        g.text.setTextSize(dp(11.5f));
        List<String> lines = g.wrap(told, right - left);
        int shown = 0;
        boolean cut = false;
        for (int one = 0; one < lines.size(); one++) {
            if (y + lineTall > limit) {
                cut = true;
                break;
            }
            say(canvas, lines.get(one), x, y + dp(12), 11.5f, g.ink, false, Paint.Align.LEFT, right - left);
            y += dp(15);
            shown++;
        }
        if (!wide && (cut || expanded)) {
            more.set(detail.centerX() - dp(46), detail.top + dp(2), detail.centerX() + dp(46), detail.top + dp(16));
            fill.setColor(isDark() ? 0xFF55595E : 0xFFC9CDD1);
            canvas.drawRoundRect(detail.centerX() - dp(18), detail.top + dp(5), detail.centerX() + dp(18), detail.top + dp(9), dp(2), dp(2), fill);
        } else if (!wide) {
            more.setEmpty();
        }
        if (!wide && cut && !expanded) {
            say(canvas, "Pull the sheet up for more", detail.centerX(), buttonsTop - dp(5), 10, g.soft, false, Paint.Align.CENTER, detail.width());
        }
    }

    /** What a thing is, in words: a weapon's, with what the smith has made of it; the rest are shown by what they do. */
    private String description(int entry) {
        if (!RunInventory.weapon(entry)) return "";
        int kind = RunInventory.kindOf(entry), level = kept.levels[kind];
        StringBuilder told = new StringBuilder();
        if (!RunInventory.plain(entry) && RunLoot.addition(entry)) told.append("The plus-book she began with, before she had a book of her own: found again. ");
        if (!RunInventory.plain(entry)) {
            told.append(RunArms.NAMES[kind]).append(": ").append(RunArms.itemBlows(entry) > 0 ? "+" + RunArms.itemBlows(entry) + (RunArms.itemBlows(entry) == 1 ? " blow, " : " blows, ") : "")
                    .append(Math.round((RunArms.itemReach(entry) - 1) * 100)).append("% further, ").append(Math.round((1 - RunArms.itemRest(entry)) * 100)).append("% quicker. ");
        }
        told.append(RunArms.does(kind));
        for (int step = 1; step <= level; step++) told.append(' ').append(RunArms.step(kind, step));
        told.append(' ').append(RunArms.goodFor(kind));
        return told.toString();
    }
}
