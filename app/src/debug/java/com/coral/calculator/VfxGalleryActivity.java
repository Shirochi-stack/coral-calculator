package com.coral.calculator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Debug only: every effect of {@link Vfx} playing, each in its own tile, to judge them by. Tap one to see it big (and again to go back); tap the
 * top corner for a light or a dark ground. Started with
 * {@code adb shell am start -n com.halgakos.calculator/com.coral.calculator.VfxGalleryActivity [--es effect NAME] [--ez light true]}.
 */
public final class VfxGalleryActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Vfx.init(this);
        Gallery gallery = new Gallery(this);
        String one = getIntent().getStringExtra("effect");
        if (one != null) for (int id = 0; id < Vfx.COUNT; id++) if (Vfx.NAMES[id].equals(one)) gallery.big = id;
        gallery.light = getIntent().getBooleanExtra("light", false);
        gallery.columns = getIntent().getIntExtra("columns", 0);
        gallery.speed = getIntent().getFloatExtra("speed", 1);
        setContentView(gallery);
    }

    private static final class Gallery extends View {
        int big = -1, columns;
        boolean light;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG), text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] since = new float[Vfx.COUNT];
        private final int[] handle = new int[Vfx.COUNT];
        private long before;
        private float scroll, downY, lastY, scrollMax;
        private boolean dragging;
        private final RectF toggle = new RectF();
        private float shown = 99;
        float speed = 1;

        Gallery(Context context) {
            super(context);
            text.setTypeface(Typeface.DEFAULT);
            for (int id = 0; id < Vfx.COUNT; id++) handle[id] = -1;
        }

        private float inset() {
            android.view.WindowInsets insets = getRootWindowInsets();
            return insets == null ? 0 : insets.getSystemWindowInsetTop();
        }

        private float density() { return getResources().getDisplayMetrics().density; }

        @Override protected void onDraw(Canvas canvas) {
            long now = System.nanoTime();
            float seconds = before == 0 ? 0 : Math.min(.1f, (now - before) / 1e9f);
            before = now;
            Vfx.update(seconds);
            canvas.drawColor(light ? 0xFFE9EBF0 : 0xFF1B1D27);
            float dp = density(), bar = inset(), top = dp * 56 + bar;
            int width = getWidth();
            int cols = columns > 0 ? columns : width > getHeight() ? 6 : 3;
            float tile = width / (float) cols;
            if (big >= 0) {
                drawBig(canvas, width, top, dp);
            } else {
                int rows = (Vfx.COUNT + cols - 1) / cols;
                scrollMax = Math.max(0, top + rows * tile - getHeight() + dp * 8);
                scroll = Math.max(0, Math.min(scrollMax, scroll));
                canvas.save();
                canvas.clipRect(0, top, width, getHeight());
                for (int id = 0; id < Vfx.COUNT; id++) {
                    float x = (id % cols) * tile, y = top + (id / cols) * tile - scroll;
                    if (y + tile < top || y > getHeight()) {
                        continue;
                    }
                    paint.setColor(light ? 0x12000000 : 0x14FFFFFF);
                    canvas.drawRoundRect(x + dp * 4, y + dp * 4, x + tile - dp * 4, y + tile - dp * 4, dp * 12, dp * 12, paint);
                    again(id, seconds, x + tile / 2, y + tile / 2 - dp * 6, tile * .78f);
                    label(canvas, id, x + tile / 2, y + tile - dp * 12, dp);
                }
                canvas.restore();
            }
            Vfx.draw(canvas);
            // the head
            paint.setColor(light ? 0xFFFFFFFF : 0xFF14161E);
            canvas.drawRect(0, 0, width, top, paint);
            text.setColor(light ? 0xFF222222 : 0xFFFFFFFF);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp * 18);
            canvas.drawText(big >= 0 ? Vfx.NAMES[big] : "Effects (" + Vfx.COUNT + ")", dp * 14, bar + dp * 36, text);
            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp * 12);
            text.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(light ? "light ground" : "dark ground", width - dp * 14, bar + dp * 34, text);
            toggle.set(width - dp * 140, 0, width, top);
            postInvalidateOnAnimation();
        }

        /** An effect begun again when its last is over (a loop begun once). */
        private void again(int id, float seconds, float x, float y, float size) {
            since[id] += seconds;
            boolean loop = Vfx.LOOP[id];
            if (loop) {
                if (handle[id] < 0) handle[id] = Vfx.play(id, x, y, size, 0, Vfx.WHITE, light ? Vfx.NORMAL : 0, 1, 0);
                else Vfx.move(handle[id], x, y);
                return;
            }
            if (since[id] > Vfx.seconds(id) + .55f) {
                since[id] = 0;
                Vfx.play(id, x, y, size, 0, Vfx.WHITE, light ? Vfx.NORMAL : 0, 1, 0);
            }
        }

        private void label(Canvas canvas, int id, float x, float y, float dp) {
            text.setColor(light ? 0xFF333333 : 0xFFDDE0EA);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(dp * 11);
            canvas.drawText(Vfx.NAMES[id], x, y, text);
            text.setColor(light ? 0xFF777777 : 0xFF8C91A3);
            text.setTextSize(dp * 9);
            canvas.drawText((Vfx.ADD[id] ? "add" : "normal") + "  " + Vfx.FPS[id] + " fps" + (Vfx.LOOP[id] ? "  loop" : ""), x, y + dp * 11, text);
        }

        private void drawBig(Canvas canvas, int width, float top, float dp) {
            int id = big;
            float size = Math.min(width, getHeight() - top) * .9f, x = width / 2f, y = top + (getHeight() - top) / 2;
            shown += 1 / 60f;
            if (Vfx.LOOP[id]) {
                if (handle[id] < 0) handle[id] = Vfx.play(id, x, y, size, 0, Vfx.WHITE, Vfx.BLOOM | (light ? Vfx.NORMAL : 0), speed, 0);
                else Vfx.move(handle[id], x, y);
                return;
            }
            if (shown > Vfx.seconds(id) / speed + .6f) {
                shown = 0;
                Vfx.play(id, x, y, size, 0, Vfx.WHITE, Vfx.BLOOM | (light ? Vfx.NORMAL : 0), speed, 0);
            }
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            float y = event.getY();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downY = lastY = y;
                    dragging = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(y - downY) > density() * 8) dragging = true;
                    if (dragging && big < 0) scroll = Math.max(0, Math.min(scrollMax, scroll - (y - lastY)));
                    lastY = y;
                    return true;
                case MotionEvent.ACTION_UP:
                    if (dragging) return true;
                    if (toggle.contains(event.getX(), y)) {
                        light = !light;
                        return true;
                    }
                    if (big >= 0) {
                        handle[big] = -1;
                        Vfx.clear();
                        big = -1;
                        for (int id = 0; id < Vfx.COUNT; id++) {
                            handle[id] = -1;
                            since[id] = 99;
                        }
                        return true;
                    }
                    float dp = density(), top = dp * 56 + inset();
                    int cols = columns > 0 ? columns : getWidth() > getHeight() ? 6 : 3;
                    float tile = getWidth() / (float) cols;
                    int id = (int) ((y - top + scroll) / tile) * cols + (int) (event.getX() / tile);
                    if (y > top && id >= 0 && id < Vfx.COUNT) {
                        Vfx.clear();
                        for (int one = 0; one < Vfx.COUNT; one++) handle[one] = -1;
                        big = id;
                        shown = 99;
                    }
                    return true;
                default:
                    return true;
            }
        }
    }
}
