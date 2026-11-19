package com.coral.calculator;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The mini Halgakos that floats over other apps. She is dragged about, opens a menu when
 * tapped and the mini calculator when held, and can be dropped on a target to send her away.
 *
 * Her window keeps this process running while Android is showing it. She keeps it, unseen,
 * while she waits for a folded device to be opened, so that the hinge is still heard.
 */
final class Companion implements FoldState.Listener, CalculatorSession.Listener {
    /** Height of the figure for each size setting, in dp. */
    private static final int[] HEIGHT_DP = {64, 88, 116};
    private static final long REST_MS = 3500;
    private static final long MENU_MS = 6000;
    private static final float REST_ALPHA = .5f;
    /** The mini calculator shrinks to fit a small screen, but no further than this. */
    private static final float SMALLEST_SCALE = .62f;
    /** It gives up this much of its size to stay on one side of a fold, and no more. */
    private static final float FOLD_SCALE = .85f;
    private static final int NO_HINGE = 0, UPRIGHT_HINGE = 1, LEVEL_HINGE = 2;

    private static Companion instance;

    private final Context app;
    private final CompanionPrefs prefs;
    private final FoldState fold;
    private final DisplayManager displays;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override public void onDisplayAdded(int displayId) {}
        @Override public void onDisplayRemoved(int displayId) {}
        @Override public void onDisplayChanged(int displayId) { screenChanged(); }
    };
    private final Runnable refit = this::refit;
    /** The room and scale she was last placed for; many display changes alter neither. */
    private final Rect placedBounds = new Rect();
    private float placedDensity;
    private final Runnable rest = this::rest;
    private final Runnable closeMenu = this::closeMenu;

    /** Context of the overlay windows, themed for the views in them. */
    private Context ui;
    private WindowManager windows;
    private Bitmap art;
    /** Room around the figure in its window, for her shadow. */
    private int pad;

    private Sprite sprite;
    private WindowManager.LayoutParams spriteParams;
    private View menu;
    private WindowManager.LayoutParams menuParams;
    private View panel;
    private WindowManager.LayoutParams panelParams;
    private MiniCalculatorView calculator;
    private DismissTarget dismiss;
    private WindowManager.LayoutParams dismissParams;
    private ValueAnimator settling;
    private boolean calculatorOpen;
    private boolean serviceStarted;
    private boolean watchingDisplay;
    private long menuClosedAt;

    static Companion get(Context context) {
        if (instance == null) instance = new Companion(context.getApplicationContext());
        return instance;
    }

    private Companion(Context context) {
        app = context;
        prefs = new CompanionPrefs(context);
        fold = new FoldState(context, this);
        displays = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    }

    /** The calculator itself is on screen, where she can be told to keep out of the way. */
    void setCalculatorOpen(boolean open) {
        calculatorOpen = open;
        sync();
    }

    /** Brings what is on screen in line with the settings, the permission and the fold. */
    void sync() {
        boolean on = prefs.enabled() && Settings.canDrawOverlays(app) && prepare();
        if (on && fold.isFoldable()) fold.start(); else fold.stop();
        boolean folded = on && prefs.unfoldedOnly() && !fold.isUnfolded(smallestWidthDp());
        watchDisplay(on);
        if (on && !(prefs.hideInApp() && calculatorOpen)) show(folded); else hide();
        keepService(on);
    }

    /** A setting changed: also sizes and places her again, and redraws what is open. */
    void settingsChanged() {
        sync();
        relayout();
    }

    private void refit() {
        sync();
        if (sprite == null) return;
        if (!bounds().equals(placedBounds) || app.getResources().getDisplayMetrics().density != placedDensity) relayout();
    }

    @Override public void onFoldChanged() { screenChanged(); }

    @Override public void onCalculationChanged() {
        if (calculator != null) calculator.refresh();
    }

    void serviceStopped() { serviceStarted = false; }

    /** Folding, unfolding and turning all change the room she has; wait for it to settle. */
    void screenChanged() {
        handler.removeCallbacks(refit);
        handler.postDelayed(refit, 150);
    }

    private boolean prepare() {
        if (windows != null) return true;
        try {
            Context base = app;
            if (Build.VERSION.SDK_INT >= 30) {
                base = app.createDisplayContext(displays.getDisplay(Display.DEFAULT_DISPLAY))
                        .createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null);
            }
            ui = new ContextThemeWrapper(base, R.style.Theme_CoralCalculator);
            art = BitmapFactory.decodeResource(app.getResources(), R.drawable.halgakos);
            pad = dp(6);
            windows = (WindowManager) base.getSystemService(Context.WINDOW_SERVICE);
            return art != null && windows != null;
        } catch (RuntimeException unavailable) {
            windows = null;
            return false;
        }
    }

    private int smallestWidthDp() {
        return ui.getResources().getConfiguration().smallestScreenWidthDp;
    }

    private void watchDisplay(boolean watch) {
        if (watch == watchingDisplay) return;
        watchingDisplay = watch;
        if (watch) displays.registerDisplayListener(displayListener, handler);
        else displays.unregisterDisplayListener(displayListener);
    }

    /** A started service lets Android bring her back if the process is ever removed. */
    private void keepService(boolean on) {
        Intent service = new Intent(app, CompanionService.class);
        try {
            if (on && !serviceStarted) {
                app.startService(service);
                serviceStarted = true;
            } else if (!on && serviceStarted) {
                serviceStarted = false;
                app.stopService(service);
            }
        } catch (RuntimeException notAllowed) {
            // Started from the background before any window is up: the next sync tries again.
        }
    }

    /**
     * @param away she waits unseen for a folded device to be opened. Her window stays as it
     *             is, drawing nothing and letting touches through: one that Android has
     *             already shown keeps the process running, where a new one put up as the
     *             device locks on folding would never be shown at all.
     */
    private void show(boolean away) {
        if (sprite == null) {
            Sprite view = new Sprite(ui);
            // Her shadow's margin may run past the edge of the screen.
            spriteParams = overlay(1, 1, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            spriteParams.setTitle("Halgakos companion");
            fit();
            setAway(view, away);
            if (!add(view, spriteParams)) return;
            sprite = view;
            if (away) return;
            pop();
        } else if (away != sprite.away) {
            if (away) {
                handler.removeCallbacks(rest);
                closeMenu();
                closePanel();
                hideDismiss();
                if (settling != null) settling.cancel();
                sprite.cancelGesture();
            }
            setAway(sprite, away);
            update(sprite, spriteParams);
            if (!away) pop();
        }
    }

    private void setAway(Sprite view, boolean away) {
        view.away = away;
        view.invalidate();
        view.setImportantForAccessibility(away ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                : View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        // Nearly clear rather than clear, so the window still counts as showing. Android lets
        // touches through a window this faint to the app underneath.
        spriteParams.alpha = away ? .02f : 1;
        if (away) spriteParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        else spriteParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }

    private void pop() {
        sprite.setScaleX(.6f);
        sprite.setScaleY(.6f);
        sprite.animate().scaleX(1).scaleY(1).setDuration(260).setInterpolator(new OvershootInterpolator(2f));
        wake();
    }

    private void hide() {
        handler.removeCallbacks(rest);
        closeMenu();
        closePanel();
        hideDismiss();
        if (settling != null) settling.cancel();
        if (sprite == null) return;
        sprite.cancelGesture();
        remove(sprite);
        sprite = null;
    }

    /** Sizes and places her again for the current settings and screen. */
    private void relayout() {
        if (sprite == null) return;
        if (settling != null) settling.cancel();
        closeMenu();
        fit();
        update(sprite, spriteParams);
        if (panel != null) openPanel();
    }

    private void fit() {
        int height = dp(HEIGHT_DP[prefs.size()]);
        spriteParams.width = Math.round(height * (float) art.getWidth() / art.getHeight()) + pad * 2;
        spriteParams.height = height + pad * 2;
        placedBounds.set(bounds());
        placedDensity = app.getResources().getDisplayMetrics().density;
        Rect room = room();
        float x = prefs.x();
        if (prefs.snap()) x = x < .5f ? 0 : 1;
        spriteParams.x = room.left + Math.round(x * room.width());
        spriteParams.y = room.top + Math.round(prefs.y() * room.height());
    }

    /** The positions her window can take: the safe area, less her own size. */
    private Rect room() {
        Rect bounds = bounds();
        // Her shadow's margin may run off the edge, so the figure itself stands just inside it.
        int reach = pad - dp(2);
        int right = Math.max(bounds.left - reach, bounds.right + reach - spriteParams.width);
        int bottom = Math.max(bounds.top - reach, bounds.bottom + reach - spriteParams.height);
        return new Rect(bounds.left - reach, bounds.top - reach, right, bottom);
    }

    /** The screen in use, without the system bars and cutouts. */
    @SuppressWarnings("deprecation")
    private Rect bounds() {
        Rect bounds = new Rect();
        if (Build.VERSION.SDK_INT >= 30) {
            WindowMetrics metrics = windows.getMaximumWindowMetrics();
            bounds.set(metrics.getBounds());
            Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            bounds.left += insets.left;
            bounds.top += insets.top;
            bounds.right -= insets.right;
            bounds.bottom -= insets.bottom;
        } else {
            Point size = new Point();
            windows.getDefaultDisplay().getRealSize(size);
            bounds.set(0, 0, size.x, size.y);
            WindowInsets insets = sprite == null ? null : sprite.getRootWindowInsets();
            if (insets != null) {
                bounds.left += insets.getStableInsetLeft();
                bounds.top += insets.getStableInsetTop();
                bounds.right -= insets.getStableInsetRight();
                bounds.bottom -= insets.getStableInsetBottom();
            } else {
                bounds.top += dp(24);
                bounds.bottom -= dp(48);
            }
        }
        return bounds;
    }

    private void moveTo(int x, int y) {
        Rect room = room();
        spriteParams.x = Math.max(room.left, Math.min(room.right, x));
        spriteParams.y = Math.max(room.top, Math.min(room.bottom, y));
        update(sprite, spriteParams);
        placePopups();
    }

    /** After a drag: remembers where she was left, and slides to the nearer edge if asked to. */
    private void settle() {
        Rect room = room();
        int target = spriteParams.x;
        if (prefs.snap()) target = spriteParams.x + spriteParams.width / 2 < bounds().centerX() ? room.left : room.right;
        prefs.setPosition(room.width() == 0 ? 0 : (target - room.left) / (float) room.width(),
                room.height() == 0 ? 0 : (spriteParams.y - room.top) / (float) room.height());
        if (target == spriteParams.x) return;
        settling = ValueAnimator.ofInt(spriteParams.x, target);
        settling.setDuration(220);
        settling.setInterpolator(new DecelerateInterpolator());
        settling.addUpdateListener(animation -> {
            if (sprite == null) return;
            spriteParams.x = (int) animation.getAnimatedValue();
            update(sprite, spriteParams);
            placePopups();
        });
        settling.start();
    }

    private void wake() {
        handler.removeCallbacks(rest);
        if (sprite == null) return;
        sprite.animate().alpha(1).setDuration(120);
        handler.postDelayed(rest, REST_MS);
    }

    private void rest() {
        if (sprite == null || !prefs.fade() || menu != null || panel != null) return;
        sprite.animate().alpha(REST_ALPHA).setDuration(500);
    }

    private void tapped() {
        wake();
        if (panel != null) closePanel();
        else if (menu != null) closeMenu();
        // The touch that lands on her also closes the menu from outside; leave it closed.
        else if (SystemClock.uptimeMillis() - menuClosedAt > 400) openMenu();
    }

    private void held() {
        wake();
        if (panel != null) closePanel(); else openPanel();
    }

    private boolean dark() {
        return app.getSharedPreferences(CalculatorSession.PREFERENCES, Context.MODE_PRIVATE).getBoolean("dark", false);
    }

    private boolean haptics() {
        return app.getSharedPreferences(CalculatorSession.PREFERENCES, Context.MODE_PRIVATE).getBoolean("haptics", true);
    }

    private void openMenu() {
        closeMenu();
        boolean dark = dark();
        LinearLayout card = new LinearLayout(ui);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, dp(6), 0, dp(6));
        card.setBackground(cardBackground(dark, dp(18)));
        card.setClipToOutline(true);
        card.addView(menuRow(CompanionIcon.CALCULATOR, "Mini calculator", dark, this::openPanel));
        card.addView(menuRow(CompanionIcon.OPEN, "Open calculator", dark, this::openApp));
        card.addView(menuRow(CompanionIcon.SETTINGS, "Companion settings", dark, this::openSettings));
        Holder holder = new Holder(ui, card, dp(12));
        holder.outside = () -> {
            menuClosedAt = SystemClock.uptimeMillis();
            closeMenu();
        };
        int free = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        holder.measure(free, free);
        menuParams = overlay(holder.getMeasuredWidth(), holder.getMeasuredHeight(),
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        menuParams.setTitle("Halgakos companion menu");
        place(menuParams, holder.inset);
        if (!add(holder, menuParams)) return;
        menu = holder;
        appear(card);
        handler.postDelayed(closeMenu, MENU_MS);
    }

    private View menuRow(String icon, String label, boolean dark, Runnable action) {
        LinearLayout row = new LinearLayout(ui);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), 0, dp(18), 0);
        row.setMinimumHeight(dp(48));
        row.setBackgroundResource(android.R.drawable.list_selector_background);
        row.addView(new CompanionIcon(ui, icon, CalculatorLayout.CORAL, 1f), new LinearLayout.LayoutParams(dp(20), dp(20)));
        TextView text = new TextView(ui);
        text.setText(label);
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text.setTextColor(dark ? 0xFFEBECEC : 0xFF363936);
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        text.setPadding(dp(12), 0, 0, 0);
        row.addView(text);
        row.setContentDescription(label);
        row.setFocusable(true);
        row.setOnClickListener(v -> action.run());
        return row;
    }

    private void closeMenu() {
        handler.removeCallbacks(closeMenu);
        if (menu == null) return;
        remove(menu);
        menu = null;
        wake();
    }

    private void openPanel() {
        closeMenu();
        closePanel();
        if (sprite == null) return;
        Rect bounds = bounds();
        float density = ui.getResources().getDisplayMetrics().density;
        int margin = dp(8);
        float fullWidth = MiniCalculatorView.WIDTH_DP * density, fullHeight = MiniCalculatorView.HEIGHT_DP * density;
        float taken = spriteParams.width - pad * 2 + dp(4) + margin * 2 + 1;
        float scale = Math.min(1, Math.min((bounds.width() - taken) / fullWidth,
                (bounds.height() - margin * 2 - 1) / fullHeight));
        // A little smaller is better than lying across the fold of an open foldable.
        int hinge = hinge();
        float half = hinge == UPRIGHT_HINGE ? (bounds.width() / 2f - taken) / fullWidth
                : hinge == LEVEL_HINGE ? (bounds.height() / 2f - margin * 2 - 1) / fullHeight : 1;
        if (half >= FOLD_SCALE) scale = Math.min(scale, half);
        float unit = density * Math.max(SMALLEST_SCALE, scale);
        calculator = new MiniCalculatorView(ui, unit, dark(), haptics(), this, new MiniCalculatorView.Host() {
            @Override public void onOpenApp() { openApp(); }
            @Override public void onClose() { closePanel(); }
        });
        Holder holder = new Holder(ui, calculator, dp(14));
        panelParams = overlay(Math.round(MiniCalculatorView.WIDTH_DP * unit) + holder.inset * 2,
                Math.round(MiniCalculatorView.HEIGHT_DP * unit) + holder.inset * 2, 0);
        panelParams.setTitle("Halgakos mini calculator");
        place(panelParams, holder.inset);
        if (!add(holder, panelParams)) {
            calculator = null;
            return;
        }
        panel = holder;
        appear(calculator);
        // Keys pressed here go on the end, wherever the app last left its cursor.
        CalculatorSession session = CalculatorSession.get(app);
        session.engine().releaseCaret();
        session.engine().setCaret(Integer.MAX_VALUE);
        session.addListener(this);
        session.changed(this);
    }

    private void closePanel() {
        if (panel == null) return;
        CalculatorSession.get(app).removeListener(this);
        remove(panel);
        panel = null;
        calculator = null;
        wake();
    }

    private void placePopups() {
        if (menu != null) {
            place(menuParams, ((Holder) menu).inset);
            update(menu, menuParams);
        }
        if (panel != null) {
            place(panelParams, ((Holder) panel).inset);
            update(panel, panelParams);
        }
    }

    /**
     * Puts a popup beside her, on the side with more room, and level with her middle.
     *
     * @param inset the transparent margin the popup's window has around what is seen
     */
    private void place(WindowManager.LayoutParams params, int inset) {
        Rect bounds = bounds();
        int margin = dp(8), gap = dp(4);
        int width = params.width - inset * 2, height = params.height - inset * 2;
        int left = spriteParams.x + pad, right = spriteParams.x + spriteParams.width - pad;
        int middle = spriteParams.y + spriteParams.height / 2;
        boolean onLeft = (left + right) / 2 < bounds.centerX();
        int x = onLeft ? right + gap : left - gap - width;
        int y = middle - height / 2;
        Rect span = new Rect(bounds.left + margin, bounds.top + margin, bounds.right - margin, bounds.bottom - margin);
        // On an open foldable, stay on her side of the fold where the popup fits there.
        int hinge = hinge();
        if (hinge == UPRIGHT_HINGE && bounds.width() / 2 - margin * 2 >= width + gap + right - left) {
            if (onLeft) span.right = bounds.centerX() - margin; else span.left = bounds.centerX() + margin;
        } else if (hinge == LEVEL_HINGE && bounds.height() / 2 - margin * 2 >= height) {
            if (middle < bounds.centerY()) span.bottom = bounds.centerY() - margin; else span.top = bounds.centerY() + margin;
        }
        params.x = Math.max(span.left, Math.min(span.right - width, x)) - inset;
        params.y = Math.max(span.top, Math.min(span.bottom - height, y)) - inset;
    }

    /**
     * Which way the fold runs across the screen in use, going by the kind of device: a book
     * folds down its middle when upright, a flip phone across it.
     */
    private int hinge() {
        if (!fold.isFoldable()) return NO_HINGE;
        Configuration configuration = ui.getResources().getConfiguration();
        boolean book = configuration.smallestScreenWidthDp >= 600;
        if (!book && !fold.isUnfolded(configuration.smallestScreenWidthDp)) return NO_HINGE;
        int rotation = displays.getDisplay(Display.DEFAULT_DISPLAY).getRotation();
        boolean natural = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180;
        return book == natural ? UPRIGHT_HINGE : LEVEL_HINGE;
    }

    private void openApp() {
        closeMenu();
        closePanel();
        start(new Intent(app, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private void openSettings() {
        closeMenu();
        start(new Intent(app, CompanionSettingsActivity.class));
    }

    private void start(Intent intent) {
        try {
            app.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } catch (RuntimeException blocked) {
            Toast.makeText(app, "Open Halgakos Calculator from your home screen", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDismiss() {
        if (dismiss != null) return;
        Rect bounds = bounds();
        int size = dp(96);
        DismissTarget view = new DismissTarget(ui);
        dismissParams = overlay(size, size, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dismissParams.x = bounds.centerX() - size / 2;
        dismissParams.y = bounds.bottom - size - dp(12);
        dismissParams.setTitle("Hide Halgakos companion");
        if (!add(view, dismissParams)) return;
        dismiss = view;
        appear(view);
    }

    private void hideDismiss() {
        if (dismiss == null) return;
        remove(dismiss);
        dismiss = null;
    }

    /** Whether she is being held over the target that sends her away. */
    private boolean overDismiss() {
        if (dismiss == null) return false;
        float x = spriteParams.x + spriteParams.width / 2f - (dismissParams.x + dismissParams.width / 2f);
        float y = spriteParams.y + spriteParams.height / 2f - (dismissParams.y + dismissParams.height / 2f);
        return Math.hypot(x, y) < dp(72);
    }

    private void sendAway() {
        prefs.set(CompanionPrefs.ENABLED, false);
        sync();
        Toast.makeText(app, "Halgakos is hidden. Bring her back from Settings in the calculator.", Toast.LENGTH_LONG).show();
    }

    private WindowManager.LayoutParams overlay(int width, int height, int flags) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(width, height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | flags,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        // Positions are in screen coordinates: the bars and cutouts are kept clear of by hand.
        if (Build.VERSION.SDK_INT >= 30) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
            params.setFitInsetsTypes(0);
        } else if (Build.VERSION.SDK_INT >= 28) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return params;
    }

    private boolean add(View view, WindowManager.LayoutParams params) {
        try {
            windows.addView(view, params);
            return true;
        } catch (RuntimeException refused) {
            // The permission was withdrawn since it was last checked.
            return false;
        }
    }

    private void update(View view, WindowManager.LayoutParams params) {
        try {
            windows.updateViewLayout(view, params);
        } catch (RuntimeException gone) {
            // The window was already removed.
        }
    }

    private void remove(View view) {
        try {
            windows.removeViewImmediate(view);
        } catch (RuntimeException gone) {
            // The window was already removed.
        }
    }

    private static void appear(View view) {
        view.setAlpha(0);
        view.setScaleX(.92f);
        view.setScaleY(.92f);
        view.animate().alpha(1).scaleX(1).scaleY(1).setDuration(140).setInterpolator(new DecelerateInterpolator());
    }

    private static GradientDrawable cardBackground(boolean dark, int radius) {
        GradientDrawable card = new GradientDrawable();
        card.setCornerRadius(radius);
        card.setColor(dark ? 0xFF1D2023 : Color.WHITE);
        card.setStroke(1, dark ? 0xFF34383C : 0xFFE4E4E4);
        return card;
    }

    private int dp(float value) {
        return Math.round(value * app.getResources().getDisplayMetrics().density);
    }

    /** A popup's window: the card, with room around it for the shadow it casts. */
    private static final class Holder extends FrameLayout {
        final int inset;
        Runnable outside;

        Holder(Context context, View card, int inset) {
            super(context);
            this.inset = inset;
            setPadding(inset, inset, inset, inset);
            setClipToPadding(false);
            setClipChildren(false);
            card.setElevation(inset * .5f);
            addView(card, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_OUTSIDE) {
                if (outside != null) outside.run();
                return true;
            }
            return super.dispatchTouchEvent(event);
        }
    }

    /** The figure, and the gestures on her. */
    private final class Sprite extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int[] shadowOffset = new int[2];
        private final int slop;
        private final Runnable hold = this::onHold;
        private Bitmap figure;
        private Bitmap shadow;
        private float downX, downY;
        private int startX, startY;
        private boolean dragging;
        private boolean held;
        /** Kept on screen but unseen, waiting for a folded device to be opened. */
        boolean away;

        Sprite(Context context) {
            super(context);
            slop = ViewConfiguration.get(context).getScaledTouchSlop();
            shadowPaint.setColor(0x52000000);
            setContentDescription("Halgakos companion. Tap for a menu, hold for the mini calculator, drag to move");
            setClickable(true);
            setLongClickable(true);
            setOnClickListener(v -> tapped());
            setOnLongClickListener(v -> { held(); return true; });
        }

        @Override public CharSequence getAccessibilityClassName() { return Button.class.getName(); }

        @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            int figureWidth = width - pad * 2, figureHeight = height - pad * 2;
            if (figureWidth <= 0 || figureHeight <= 0) return;
            figure = Bitmap.createScaledBitmap(art, figureWidth, figureHeight, true);
            Paint blur = new Paint();
            blur.setMaskFilter(new BlurMaskFilter(pad * .5f, BlurMaskFilter.Blur.NORMAL));
            shadow = figure.extractAlpha(blur, shadowOffset);
        }

        @Override protected void onDraw(Canvas canvas) {
            if (figure == null || away) return;
            canvas.drawBitmap(shadow, pad + shadowOffset[0], pad + shadowOffset[1] + pad * .3f, shadowPaint);
            canvas.drawBitmap(figure, pad, pad, paint);
        }

        @Override protected void onConfigurationChanged(Configuration configuration) {
            super.onConfigurationChanged(configuration);
            screenChanged();
        }

        void cancelGesture() {
            handler.removeCallbacks(hold);
            dragging = false;
        }

        @Override public boolean performClick() { return super.performClick(); }

        private void onHold() {
            held = true;
            performLongClick();
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (settling != null) settling.cancel();
                    downX = event.getRawX();
                    downY = event.getRawY();
                    startX = spriteParams.x;
                    startY = spriteParams.y;
                    dragging = false;
                    held = false;
                    wake();
                    animate().scaleX(.93f).scaleY(.93f).setDuration(90);
                    handler.postDelayed(hold, ViewConfiguration.getLongPressTimeout());
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (held) return true;
                    float x = event.getRawX() - downX, y = event.getRawY() - downY;
                    if (!dragging && Math.hypot(x, y) > slop) {
                        dragging = true;
                        handler.removeCallbacks(hold);
                        closeMenu();
                        showDismiss();
                    }
                    if (dragging) {
                        moveTo(startX + Math.round(x), startY + Math.round(y));
                        if (dismiss != null) dismiss.setHot(overDismiss());
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    handler.removeCallbacks(hold);
                    animate().scaleX(1).scaleY(1).setDuration(180).setInterpolator(new OvershootInterpolator(3f));
                    boolean dropped = dragging && event.getActionMasked() == MotionEvent.ACTION_UP && overDismiss();
                    boolean tap = !dragging && !held && event.getActionMasked() == MotionEvent.ACTION_UP;
                    boolean wasDragging = dragging;
                    dragging = false;
                    hideDismiss();
                    if (dropped) sendAway();
                    else if (wasDragging) settle();
                    else if (tap) performClick();
                    wake();
                    return true;
                }
                default:
                    return super.onTouchEvent(event);
            }
        }
    }

    /** Where she is dropped to be hidden; it grows and turns coral once she is over it. */
    private static final class DismissTarget extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean hot;

        DismissTarget(Context context) {
            super(context);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        void setHot(boolean value) {
            if (hot == value) return;
            hot = value;
            if (value) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            float middleX = getWidth() / 2f, middleY = getHeight() / 2f;
            float radius = Math.min(middleX, middleY) * (hot ? .78f : .58f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(hot ? CalculatorLayout.CORAL : 0xD9202326);
            canvas.drawCircle(middleX, middleY, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(radius * .09f);
            paint.setColor(Color.WHITE);
            float arm = radius * .3f;
            canvas.drawLine(middleX - arm, middleY - arm, middleX + arm, middleY + arm, paint);
            canvas.drawLine(middleX + arm, middleY - arm, middleX - arm, middleY + arm, paint);
        }
    }
}
