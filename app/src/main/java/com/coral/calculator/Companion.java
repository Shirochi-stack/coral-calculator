package com.coral.calculator;

import android.animation.ValueAnimator;
import android.app.ActivityManager;
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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
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

import java.util.Random;

/**
 * The mini Halgakos that floats over other apps. She is dragged about, opens a menu when
 * tapped and the mini calculator when held, and can be dropped on a target to send her away.
 * From her menu she plays games, some of them all over the screen, and now and then she has
 * something to say.
 *
 * Her window keeps this process running while Android is showing it. She keeps it, unseen,
 * while she waits for a folded device to be opened, so that the hinge is still heard.
 */
final class Companion implements FoldState.Listener, CalculatorSession.Listener {
    /** Height of the figure for each size setting, in dp. */
    private static final int[] HEIGHT_DP = {64, 88, 116};
    private static final long MENU_MS = 6000;
    private static final float REST_ALPHA = .5f;
    /** The mini calculator shrinks to fit a small screen, but no further than this. */
    private static final float SMALLEST_SCALE = .62f;
    /** It gives up this much of its size to stay on one side of a fold, and no more. */
    private static final float FOLD_SCALE = .85f;
    private static final int NO_HINGE = FoldState.NO_HINGE, UPRIGHT_HINGE = FoldState.UPRIGHT_HINGE,
            LEVEL_HINGE = FoldState.LEVEL_HINGE;
    /** How long she waits before the first thing she says, and between one and the next. */
    private static final long FIRST_WORDS_MS = 20_000, FIRST_WORDS_SPREAD_MS = 25_000;
    private static final long NEXT_WORDS_MS = 60_000, NEXT_WORDS_SPREAD_MS = 90_000;
    /** How long a bubble stays up: a base, and more for each letter there is to read. */
    private static final long BUBBLE_MS = 3200, BUBBLE_LETTER_MS = 45;
    /**
     * A popup's window has a margin for its shadow, which may run past the edge of the screen.
     * Without this Android moves the whole window back inside, and the popup with it: toward
     * her, when it stands between her and the edge.
     */
    private static final int POPUP_FLAGS = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
    /** Rows of the animated figure's frames. */
    private static final int POSE_IDLE = PetArt.IDLE, POSE_RUN_RIGHT = PetArt.RUN_RIGHT, POSE_RUN_LEFT = PetArt.RUN_LEFT,
            POSE_JUMP = PetArt.JUMP, POSE_SAD = PetArt.SAD, POSE_WORK = PetArt.WORK;
    private static final int PET_COLUMNS = PetArt.COLUMNS;
    private static final int[] PET_FRAMES = PetArt.FRAMES;
    private static final int[] PET_FRAME_MS = PetArt.FRAME_MS;
    /** Idle is prank time: where she is allowed them, one comes this long after the last. */
    private static final long PRANK_SOONEST_MS = 6 * 60_000L, PRANK_LATEST_MS = 20 * 60_000L;
    /** She breathes and blinks once, then holds still this long before the next breath. */
    private static final long IDLE_PAUSE_MS = 700;
    private static final long NO_TOUCH = -1;
    /** One touch reaches her window and the menu's this far apart at most. */
    private static final long SAME_TOUCH_MS = 150;

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
    private final Runnable stepBack = this::stepBack;
    private final Runnable speak = this::speak;
    private final Runnable closeBubble = this::closeBubble;
    private final Runnable surprise = this::surprise;
    private final Random random = new Random();

    /** Context of the overlay windows, themed for the views in them. */
    private Context ui;
    private WindowManager windows;
    private Bitmap art;
    /** The animated figure's frames, one pose to a row; loaded only while she is animated. */
    private Bitmap pet;
    /** Room around the figure in its window, for her shadow. */
    private int pad;

    private Sprite sprite;
    private WindowManager.LayoutParams spriteParams;
    private View menu;
    private WindowManager.LayoutParams menuParams;
    private View panel;
    private WindowManager.LayoutParams panelParams;
    private MiniCalculatorView calculator;
    /** The panel holds her games and not the mini calculator. */
    private boolean panelGames;
    /** Something she is saying, in a bubble over her head. */
    private View bubble;
    private WindowManager.LayoutParams bubbleParams;
    private boolean wordsDue;
    /** What she last said of her own accord, so as not to say it twice running. */
    private String lastWords;
    /** A game or a prank that has the whole screen, drawn over everything else of hers. */
    private PlayStage stage;
    private WindowManager.LayoutParams stageParams;
    private boolean prankDue;
    private DismissTarget dismiss;
    private WindowManager.LayoutParams dismissParams;
    private ValueAnimator settling;
    /** Where she stood before stepping aside to give a popup room, if she has. */
    private int steppedFrom = PopupPlacement.NOWHERE;
    /** How many of the app's own screens are showing. */
    private int appScreens;
    /** The app was closed from her menu: she stays away until one of its screens is opened. */
    private boolean closed;
    private boolean serviceStarted;
    private boolean watchingDisplay;
    /** When the touch now on her was felt, and when one outside the menu last closed it. */
    private long touchStarted = NO_TOUCH;
    private long menuClosedBy = NO_TOUCH;

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

    /**
     * One of the app's own screens, the calculator or these settings, has come on or gone
     * off the screen. She can be told to keep out of the way while any of them is showing.
     * Going from one to the other, the new one arrives before the old one leaves.
     */
    void setAppScreenShown(boolean shown) {
        if (shown) closed = false;
        appScreens = Math.max(0, appScreens + (shown ? 1 : -1));
        sync();
    }

    /** Brings what is on screen in line with the settings, the permission and the fold. */
    void sync() {
        boolean on = !closed && prefs.enabled() && Settings.canDrawOverlays(app) && prepare();
        if (on && fold.isFoldable()) fold.start(); else fold.stop();
        boolean folded = on && prefs.unfoldedOnly() && !fold.isUnfolded(smallestWidthDp());
        watchDisplay(on);
        if (on && !(prefs.hideInApp() && appScreens > 0)) show(folded); else hide();
        keepService(on);
        schedule();
    }

    /** Sets her up to speak, and to play a prank, some while from now, where she may do either. */
    private void schedule() {
        boolean shown = sprite != null && !sprite.away;
        if (shown && prefs.chatter()) {
            if (!wordsDue) {
                wordsDue = true;
                handler.postDelayed(speak, FIRST_WORDS_MS + (long) (random.nextFloat() * FIRST_WORDS_SPREAD_MS));
            }
        } else {
            wordsDue = false;
            handler.removeCallbacks(speak);
            closeBubble();
        }
        if (shown && prefs.pranks()) {
            if (!prankDue) {
                prankDue = true;
                handler.postDelayed(surprise, PRANK_SOONEST_MS
                        + (long) (random.nextFloat() * (PRANK_LATEST_MS - PRANK_SOONEST_MS)));
            }
        } else {
            prankDue = false;
            handler.removeCallbacks(surprise);
        }
    }

    /** Whether she is on screen with nothing open and nothing being done to her. */
    private boolean idle() {
        Display display = displays.getDisplay(Display.DEFAULT_DISPLAY);
        return sprite != null && !sprite.away && !sprite.dragging && menu == null && panel == null && stage == null
                && display != null && display.getState() == Display.STATE_ON;
    }

    private void speak() {
        wordsDue = false;
        if (idle()) {
            lastWords = Chatter.idle(random, java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY), lastWords);
            say(lastWords);
        }
        if (sprite != null && !sprite.away && prefs.chatter()) {
            wordsDue = true;
            handler.postDelayed(speak, NEXT_WORDS_MS + (long) (random.nextFloat() * NEXT_WORDS_SPREAD_MS));
        }
    }

    /** A prank she was not asked for: only while she is idle, and never over the app's own screens. */
    private void surprise() {
        prankDue = false;
        if (idle() && appScreens == 0) openStage(PlaySession.PRANK);
        schedule();
    }

    /** A setting changed: also sizes and places her again, and redraws what is open. */
    void settingsChanged() {
        sync();
        relayout();
        wake();
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
        // The screen coming back on is one of these changes: she starts moving again.
        if (sprite != null) sprite.keepMoving();
    }

    /** Loads the animated frames while they are wanted, and lets them go when they are not. */
    private boolean animated() {
        if (!prefs.animated()) {
            pet = null;
            return false;
        }
        if (pet == null) pet = BitmapFactory.decodeResource(app.getResources(), R.drawable.halgakos_pet);
        return pet != null;
    }

    /** The pose she holds when nothing briefer is playing. */
    private void pose() {
        if (sprite == null) return;
        sprite.setPose(dismiss != null && overDismiss() ? POSE_SAD
                : sprite.dragging ? (sprite.leftward ? POSE_RUN_LEFT : POSE_RUN_RIGHT)
                : panel != null && !panelGames ? POSE_WORK : POSE_IDLE);
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
                closeBubble();
                closeStage();
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
        closeBubble();
        closeStage();
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
        // Whichever figure is not in use is redrawn from scratch if it is chosen again.
        sprite.figure = null;
        sprite.describe();
        sprite.resume();
        if (panel != null) openPanel(panelGames);
        closeBubble();
        if (stage != null) fitStage();
    }

    private void fit() {
        int height = dp(HEIGHT_DP[prefs.size()]);
        // The two figures differ in shape: the animated one has room in its frames to run.
        float shape = animated() ? (float) (pet.getWidth() / PET_COLUMNS) / (pet.getHeight() / PET_FRAMES.length)
                : (float) art.getWidth() / art.getHeight();
        spriteParams.width = Math.round(height * shape) + pad * 2;
        spriteParams.height = height + pad * 2;
        placedBounds.set(bounds());
        placedDensity = app.getResources().getDisplayMetrics().density;
        Rect room = room();
        float x = prefs.x();
        if (prefs.snap()) x = x < .5f ? 0 : 1;
        spriteParams.x = room.left + Math.round(x * room.width());
        spriteParams.y = room.top + Math.round(prefs.y() * room.height());
        steppedFrom = PopupPlacement.NOWHERE;
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

    /** Takes her where she is dragged, which is then her place: she steps back to no other. */
    private void moveTo(int x, int y) {
        Rect room = room();
        steppedFrom = PopupPlacement.NOWHERE;
        spriteParams.x = Math.max(room.left, Math.min(room.right, x));
        spriteParams.y = Math.max(room.top, Math.min(room.bottom, y));
        update(sprite, spriteParams);
        placePopups();
    }

    /** After a drag: remembers where she was left, and slides to the nearer edge if asked to. */
    private void settle() {
        Rect room = room();
        int target = spriteParams.x;
        if (prefs.snap()) target = onLeft() ? room.left : room.right;
        prefs.setPosition(room.width() == 0 ? 0 : (target - room.left) / (float) room.width(),
                room.height() == 0 ? 0 : (spriteParams.y - room.top) / (float) room.height());
        if (target == spriteParams.x) makeRoom(); else slideTo(target);
    }

    /** Slides her along the screen, with whatever popup is open keeping beside her. */
    private void slideTo(int target) {
        if (settling != null) settling.cancel();
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

    /** Whether she stands in the left half of the screen. */
    private boolean onLeft() {
        return spriteParams.x + spriteParams.width / 2 < bounds().centerX();
    }

    /** The part of the screen a popup may take: the safe area, less a margin. */
    private Rect span() {
        Rect bounds = bounds();
        int margin = dp(8);
        return new Rect(bounds.left + margin, bounds.top + margin, bounds.right - margin, bounds.bottom - margin);
    }

    /** Where a popup this wide stands beside her within a span, if it fits on either side. */
    private int beside(int width, Rect span) {
        return PopupPlacement.beside(spriteParams.x + pad, spriteParams.x + spriteParams.width - pad, width,
                dp(4), span.left, span.right, onLeft());
    }

    /**
     * Left where a popup that is open fits on neither side of her, she steps toward the edge
     * she is nearer, far enough to give it the other side. She steps back once it has closed.
     */
    private void makeRoom() {
        if (sprite == null || sprite.dragging) return;
        int width = 0;
        if (menu != null) width = menuParams.width - ((Holder) menu).inset * 2;
        if (panel != null) width = Math.max(width, panelParams.width - ((Holder) panel).inset * 2);
        Rect span = span();
        if (width == 0 || beside(width, span) != PopupPlacement.NOWHERE) return;
        int left = spriteParams.x + pad, right = spriteParams.x + spriteParams.width - pad;
        Rect room = room();
        int target = PopupPlacement.aside(left, right, width, dp(4), span.left, span.right, onLeft()) - pad;
        target = Math.max(room.left, Math.min(room.right, target));
        if (target == spriteParams.x) return;
        if (steppedFrom == PopupPlacement.NOWHERE) steppedFrom = spriteParams.x;
        slideTo(target);
    }

    /** Back to where she stood, once nothing is open that she stepped aside for. */
    private void stepBack() {
        if (steppedFrom == PopupPlacement.NOWHERE || sprite == null || menu != null || panel != null) return;
        Rect room = room();
        int home = Math.max(room.left, Math.min(room.right, steppedFrom));
        steppedFrom = PopupPlacement.NOWHERE;
        if (home != spriteParams.x) slideTo(home);
    }

    private void wake() {
        handler.removeCallbacks(rest);
        if (sprite == null) return;
        sprite.animate().alpha(1).setDuration(120);
        handler.postDelayed(rest, prefs.fadeAfterMs());
    }

    private void rest() {
        if (sprite == null || !prefs.fade() || menu != null || panel != null) return;
        sprite.animate().alpha(REST_ALPHA).setDuration(300);
    }

    /** A tap and a hold each open one of her two popups; which is which is a setting. */
    private void tapped() {
        wake();
        toggle(prefs.tapCalculator());
    }

    private void held() {
        wake();
        toggle(!prefs.tapCalculator());
    }

    private void toggle(boolean calculator) {
        closeBubble();
        if (calculator) {
            // Whichever of the two is open, the touch that would open the calculator closes it.
            if (panel != null) {
                closePanel();
            } else {
                openPanel(false);
                if (sprite != null) sprite.play(POSE_JUMP);
            }
        } else if (menu != null) {
            closeMenu();
        } else if (menuClosedBy == NO_TOUCH || menuClosedBy < touchStarted - SAME_TOUCH_MS) {
            // The touch that lands on her also closes the menu from outside; it stays closed.
            openMenu();
        }
        menuClosedBy = NO_TOUCH;
    }

    /** From her menu: opens the mini calculator or her games, or closes the one that is open. */
    private void toggleFromMenu(boolean games) {
        closeMenu();
        if (panel != null && panelGames == games) closePanel(); else openPanel(games);
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
        card.addView(menuRow(CompanionIcon.CALCULATOR, panel != null && !panelGames ? "Close mini calculator"
                : "Mini calculator", dark, () -> toggleFromMenu(false)));
        card.addView(menuRow(CompanionIcon.PLAY, panel != null && panelGames ? "Close the games"
                : "Play with Halgakos", dark, () -> toggleFromMenu(true)));
        card.addView(menuRow(CompanionIcon.OPEN, "Open calculator", dark, this::openApp));
        card.addView(menuRow(CompanionIcon.SETTINGS, "Companion settings", dark, this::openSettings));
        card.addView(menuRow(CompanionIcon.POWER, "Close app", dark, this::closeApp));
        Holder holder = new Holder(ui, card, dp(12));
        holder.outside = (x, y) -> {
            menuClosedBy = SystemClock.uptimeMillis();
            closeMenu();
        };
        int free = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        holder.measure(free, free);
        menuParams = overlay(holder.getMeasuredWidth(), holder.getMeasuredHeight(),
                POPUP_FLAGS | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        menuParams.setTitle("Halgakos companion menu");
        place(menuParams, holder.inset);
        if (!add(holder, menuParams)) return;
        menu = holder;
        makeRoom();
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
        // Not at once: the mini calculator may be about to open in the menu's place.
        handler.post(stepBack);
        wake();
    }

    /** Opens the mini calculator beside her, or her games in its place and at its size. */
    private void openPanel(boolean games) {
        closeMenu();
        closePanel();
        closeBubble();
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
        View content;
        if (games) {
            content = new PlayView(ui, unit, dark(), haptics(), false, new PlayView.Host() {
                @Override public void onOpenApp() { openPlayScreen(); }
                @Override public void onClose() { closePanel(); }
                @Override public void onMood(boolean cheerful) {
                    if (sprite != null) sprite.play(cheerful ? POSE_JUMP : POSE_SAD);
                }
                @Override public void onStage(int game) { openStage(game); }
            });
        } else {
            calculator = new MiniCalculatorView(ui, unit, dark(), haptics(), this, new MiniCalculatorView.Host() {
                @Override public void onOpenApp() { openApp(); }
                @Override public void onClose() { closePanel(); }
                @Override public void onError() {
                    // A sum with no answer is exactly what her cry about her output is for.
                    if (!prefs.chatter() || sprite == null) return;
                    say(Chatter.OUTPUT);
                    sprite.play(POSE_SAD);
                }
            });
            content = calculator;
        }
        Holder holder = new Holder(ui, content, dp(14));
        boolean closesOutside = prefs.closeOutside();
        // A touch on Halgakos or her menu is not one that leaves the calculator: she closes
        // it herself when tapped, and it follows her when dragged.
        if (closesOutside) holder.outside = (x, y) -> { if (!onOwnWindow(x, y)) closePanel(); };
        panelParams = overlay(Math.round(MiniCalculatorView.WIDTH_DP * unit) + holder.inset * 2,
                Math.round(MiniCalculatorView.HEIGHT_DP * unit) + holder.inset * 2,
                POPUP_FLAGS | (closesOutside ? WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH : 0));
        panelParams.setTitle(games ? "Play with Halgakos" : "Halgakos mini calculator");
        place(panelParams, holder.inset);
        if (!add(holder, panelParams)) {
            calculator = null;
            return;
        }
        panel = holder;
        panelGames = games;
        makeRoom();
        pose();
        appear(content);
        if (games) return;
        // Keys pressed here go on the end, wherever the app last left its cursor.
        CalculatorSession session = CalculatorSession.get(app);
        session.engine().releaseCaret();
        session.engine().setCaret(Integer.MAX_VALUE);
        session.addListener(this);
        session.changed(this);
    }

    /**
     * Whether a touch outside a popup, at this place on the screen, landed on her or her menu.
     * Android gives the place only for touches on this app's own windows; one on another
     * app comes with none.
     */
    private boolean onOwnWindow(float x, float y) {
        if (x == 0 && y == 0) return false;
        return within(spriteParams, x, y) || (menu != null && within(menuParams, x, y));
    }

    private static boolean within(WindowManager.LayoutParams window, float x, float y) {
        return window != null && x >= window.x && x < window.x + window.width
                && y >= window.y && y < window.y + window.height;
    }

    private void closePanel() {
        if (panel == null) return;
        CalculatorSession.get(app).removeListener(this);
        remove(panel);
        panel = null;
        calculator = null;
        // Not at once: it may be about to open again, redrawn for a new setting or screen.
        handler.post(stepBack);
        pose();
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
        if (bubble != null) {
            placeBubble();
            update(bubble, bubbleParams);
        }
    }

    // What she says

    /** Puts up a bubble over her head with something she has to say; a touch dismisses it. */
    private void say(String words) {
        closeBubble();
        if (sprite == null) return;
        boolean dark = dark();
        TextView text = new TextView(ui);
        text.setText(words);
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        text.setTextColor(dark ? 0xFFEBECEC : 0xFF363936);
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        text.setLineSpacing(0, 1.1f);
        text.setPadding(dp(13), dp(9), dp(13), dp(9));
        text.setBackground(cardBackground(dark, dp(16)));
        text.setContentDescription("Halgakos says: " + words);
        text.setOnClickListener(v -> closeBubble());
        Holder holder = new Holder(ui, text, dp(10));
        Rect span = span();
        holder.measure(View.MeasureSpec.makeMeasureSpec(Math.min(dp(236), span.width()) + holder.inset * 2, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        bubbleParams = overlay(holder.getMeasuredWidth(), holder.getMeasuredHeight(), POPUP_FLAGS);
        bubbleParams.setTitle("Halgakos says");
        placeBubble(holder.inset);
        if (!add(holder, bubbleParams)) return;
        bubble = holder;
        appear(text);
        wake();
        handler.postDelayed(closeBubble, BUBBLE_MS + BUBBLE_LETTER_MS * words.length());
    }

    private void closeBubble() {
        handler.removeCallbacks(closeBubble);
        if (bubble == null) return;
        remove(bubble);
        bubble = null;
    }

    private void placeBubble() { placeBubble(((Holder) bubble).inset); }

    /** Over her head, or under her feet where there is no room above; never off the screen. */
    private void placeBubble(int inset) {
        Rect span = span();
        int width = bubbleParams.width - inset * 2, height = bubbleParams.height - inset * 2, gap = dp(2);
        int x = spriteParams.x + spriteParams.width / 2 - width / 2;
        int y = spriteParams.y + pad - gap - height;
        if (y < span.top) y = spriteParams.y + spriteParams.height - pad + gap;
        x = Math.max(span.left, Math.min(span.right - width, x));
        y = Math.max(span.top, Math.min(span.bottom - height, y));
        if (panel != null) {
            // Clear of the mini calculator or the games: over the top of the card, or under it.
            int inner = ((Holder) panel).inset;
            int left = panelParams.x + inner, top = panelParams.y + inner;
            int right = panelParams.x + panelParams.width - inner, bottom = panelParams.y + panelParams.height - inner;
            if (x < right && x + width > left && y < bottom && y + height > top) {
                y = top - dp(6) - height >= span.top ? top - dp(6) - height : Math.min(span.bottom - height, bottom + dp(6));
            }
        }
        bubbleParams.x = x - inset;
        bubbleParams.y = y - inset;
    }

    // The whole screen: her games that are played all over it, and her pranks

    /** The whole of the screen in use, bars and cutouts included. */
    @SuppressWarnings("deprecation")
    private Rect screen() {
        if (Build.VERSION.SDK_INT >= 30) return new Rect(windows.getMaximumWindowMetrics().getBounds());
        Point size = new Point();
        windows.getDefaultDisplay().getRealSize(size);
        return new Rect(0, 0, size.x, size.y);
    }

    /**
     * Gives her the whole screen, over whatever app is in use, for a game or a prank. She
     * leaves her own window for it, and is back in it where she stood once it is over.
     *
     * @param game one of the games of {@link PlaySession} that take a stage, or its prank
     */
    private void openStage(int game) {
        closeMenu();
        closePanel();
        closeBubble();
        closeStage();
        if (sprite == null || sprite.away) return;
        if (settling != null) settling.cancel();
        int kind = game == PlaySession.PRANK ? PlayStage.anyPrank(random) : game;
        boolean prank = PlayStage.isPrank(kind);
        PlayStage view = new PlayStage(ui, kind, dark(), haptics(), art, animated() ? pet : null, played -> {
            closeStage();
            // After a game the others are offered again; a prank just ends.
            if (!PlayStage.isPrank(played)) openPanel(true);
        });
        Rect screen = screen();
        stageParams = overlay(screen.width(), screen.height(), POPUP_FLAGS);
        stageParams.x = screen.left;
        stageParams.y = screen.top;
        stageParams.setTitle(prank ? "Halgakos's prank" : "Play with Halgakos, whole screen");
        // A game takes the keys as well, so that Back leaves it and not the app underneath.
        if (!prank) stageParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        view.setHome(spriteParams.x + spriteParams.width / 2f - screen.left,
                spriteParams.y + spriteParams.height - pad - screen.top);
        if (!add(view, stageParams)) return;
        stage = view;
        fitStage();
        handler.removeCallbacks(rest);
        sprite.onStage = true;
        sprite.invalidate();
    }

    /** Tells the stage how much of the screen is clear of the bars, and where the fold is. */
    private void fitStage() {
        Rect screen = screen(), safe = bounds();
        if (stageParams.width != screen.width() || stageParams.height != screen.height()) {
            stageParams.width = screen.width();
            stageParams.height = screen.height();
            stageParams.x = screen.left;
            stageParams.y = screen.top;
            update(stage, stageParams);
        }
        stage.setSafeInsets(safe.left - screen.left, safe.top - screen.top, screen.right - safe.right, screen.bottom - safe.bottom);
        stage.setHinge(hinge());
    }

    private void closeStage() {
        if (stage == null) return;
        remove(stage);
        stage = null;
        if (sprite != null) {
            sprite.onStage = false;
            sprite.invalidate();
        }
        wake();
    }

    /** Carries on with the games on a full screen in the app. */
    private void openPlayScreen() {
        closeMenu();
        closePanel();
        start(new Intent(app, PlayActivity.class));
    }

    /**
     * Puts a popup beside her, level with her middle: on the side with more room if it fits
     * there, on the other side if not, and never over her.
     *
     * @param inset the transparent margin the popup's window has around what is seen
     */
    private void place(WindowManager.LayoutParams params, int inset) {
        Rect bounds = bounds();
        int margin = dp(8);
        int width = params.width - inset * 2, height = params.height - inset * 2;
        int middle = spriteParams.y + spriteParams.height / 2;
        Rect span = span();
        // On an open foldable, stay on her side of the fold where the popup fits beside her there.
        Rect near = new Rect(span);
        int hinge = hinge();
        if (hinge == UPRIGHT_HINGE) {
            if (onLeft()) near.right = bounds.centerX() - margin; else near.left = bounds.centerX() + margin;
        } else if (hinge == LEVEL_HINGE && bounds.height() / 2 - margin * 2 >= height) {
            if (middle < bounds.centerY()) near.bottom = bounds.centerY() - margin; else near.top = bounds.centerY() + margin;
        }
        int x = beside(width, near);
        if (x == PopupPlacement.NOWHERE) x = beside(width, span);
        // No room on either side, as when she is dragged across the middle of a narrow screen:
        // it waits against the far edge, which is where it stands once she has stepped aside.
        if (x == PopupPlacement.NOWHERE) x = Math.max(span.left, onLeft() ? span.right - width : span.left);
        int y = middle - height / 2;
        params.x = x - inset;
        params.y = Math.max(near.top, Math.min(near.bottom - height, y)) - inset;
    }

    /**
     * Which way the fold runs across the screen in use, going by the kind of device: a book
     * folds down its middle when upright, a flip phone across it.
     */
    private int hinge() {
        return fold.hinge(ui.getResources().getConfiguration().smallestScreenWidthDp,
                displays.getDisplay(Display.DEFAULT_DISPLAY).getRotation());
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

    /**
     * Closes the whole app: she goes, and so do its screens, from the recent apps too. Unlike
     * {@link #sendAway} it changes no setting, so opening the app again brings her back.
     */
    private void closeApp() {
        closed = true;
        sync();
        ActivityManager manager = (ActivityManager) app.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            for (ActivityManager.AppTask task : manager.getAppTasks()) task.finishAndRemoveTask();
        } catch (RuntimeException gone) {
            // A task that went while the list was being read.
        }
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

    private interface Outside {
        void touched(float x, float y);
    }

    /** A popup's window: the card, with room around it for the shadow it casts. */
    private static final class Holder extends FrameLayout {
        final int inset;
        /** Told when a touch lands outside the popup, and where on the screen if that is known. */
        Outside outside;

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
                if (outside != null) outside.touched(event.getRawX(), event.getRawY());
                return true;
            }
            return super.dispatchTouchEvent(event);
        }
    }

    /** The figure, and the gestures on her. */
    private final class Sprite extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /** Draws a frame of the animated figure as its own shadow. */
        private final Paint silhouette = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Rect frameSource = new Rect();
        private final RectF frameTarget = new RectF();
        private final int[] shadowOffset = new int[2];
        private final int slop;
        private final Runnable hold = this::onHold;
        private final Runnable step = this::step;
        private Bitmap figure;
        private Bitmap shadow;
        private float downX, downY, lastX;
        private int startX, startY;
        private boolean dragging;
        /** Which way she was last dragged, and so which way she runs. */
        private boolean leftward;
        private boolean held;
        /** The pose she keeps, one played through once over it (or -1), and the frame shown. */
        private int pose = POSE_IDLE;
        private int playing = -1;
        private int frame;
        /** Whether the next frame is on its way. */
        private boolean moving;
        /** Kept on screen but unseen, waiting for a folded device to be opened. */
        boolean away;
        /** She has left this window for the whole screen, for a game or a prank. */
        boolean onStage;

        Sprite(Context context) {
            super(context);
            slop = ViewConfiguration.get(context).getScaledTouchSlop();
            shadowPaint.setColor(0x52000000);
            silhouette.setColorFilter(new PorterDuffColorFilter(0x3D000000, PorterDuff.Mode.SRC_IN));
            describe();
            setClickable(true);
            setLongClickable(true);
            setOnClickListener(v -> tapped());
            setOnLongClickListener(v -> { held(); return true; });
        }

        void describe() {
            setContentDescription(prefs.tapCalculator()
                    ? "Halgakos companion. Tap for the mini calculator, hold for the menu, drag to move"
                    : "Halgakos companion. Tap for the menu, hold for the mini calculator, drag to move");
        }

        @Override public CharSequence getAccessibilityClassName() { return Button.class.getName(); }

        @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            int figureWidth = width - pad * 2, figureHeight = height - pad * 2;
            if (figureWidth <= 0 || figureHeight <= 0 || pet != null) return;
            figure = Bitmap.createScaledBitmap(art, figureWidth, figureHeight, true);
            Paint blur = new Paint();
            blur.setMaskFilter(new BlurMaskFilter(pad * .5f, BlurMaskFilter.Blur.NORMAL));
            shadow = figure.extractAlpha(blur, shadowOffset);
        }

        @Override protected void onDraw(Canvas canvas) {
            // While she has the whole screen she is drawn there, not here.
            if (away || onStage) return;
            if (pet != null) {
                int row = playing >= 0 ? playing : pose;
                int width = pet.getWidth() / PET_COLUMNS, height = pet.getHeight() / PET_FRAMES.length;
                frameSource.set(frame * width, row * height, (frame + 1) * width, (row + 1) * height);
                frameTarget.set(pad, pad + pad * .3f, getWidth() - pad, getHeight() - pad + pad * .3f);
                canvas.drawBitmap(pet, frameSource, frameTarget, silhouette);
                frameTarget.offset(0, -pad * .3f);
                canvas.drawBitmap(pet, frameSource, frameTarget, paint);
            } else if (figure != null) {
                canvas.drawBitmap(shadow, pad + shadowOffset[0], pad + shadowOffset[1] + pad * .3f, shadowPaint);
                canvas.drawBitmap(figure, pad, pad, paint);
            }
        }

        /** Changes the pose she keeps. One that is playing through once finishes first. */
        void setPose(int value) {
            if (pose == value) return;
            pose = value;
            if (playing >= 0) return;
            frame = 0;
            resume();
        }

        /** Plays a pose through once, then goes back to the one she keeps. */
        void play(int value) {
            if (pet == null) return;
            playing = value;
            frame = 0;
            resume();
        }

        /** Starts the frames moving again: after a change of pose, of figure, or of screen. */
        void resume() {
            handler.removeCallbacks(step);
            moving = false;
            if (pet == null && figure == null && getWidth() > 0) onSizeChanged(getWidth(), getHeight(), 0, 0);
            invalidate();
            schedule();
        }

        /**
         * Starts the frames again if they had stopped, as they do while the screen is off.
         * Unlike {@link #resume} this leaves a frame that is already due alone: the screen
         * reports changes many times a second on some devices, and restarting the wait for
         * each would keep her from ever taking her next breath.
         */
        void keepMoving() {
            if (!moving) schedule();
        }

        private void schedule() {
            // Nothing is drawn while she is unseen or the screen is off, so nothing is scheduled.
            Display display = displays.getDisplay(Display.DEFAULT_DISPLAY);
            moving = pet != null && !away && isAttachedToWindow() && display != null
                    && display.getState() == Display.STATE_ON;
            if (!moving) return;
            int row = playing >= 0 ? playing : pose;
            handler.postDelayed(step, row == POSE_IDLE && frame == 0 ? IDLE_PAUSE_MS : PET_FRAME_MS[row]);
        }

        private void step() {
            moving = false;
            int row = playing >= 0 ? playing : pose;
            if (++frame >= PET_FRAMES[row]) {
                frame = 0;
                playing = -1;
            }
            invalidate();
            schedule();
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            resume();
        }

        @Override protected void onDetachedFromWindow() {
            handler.removeCallbacks(step);
            moving = false;
            super.onDetachedFromWindow();
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
                    lastX = downX;
                    startX = spriteParams.x;
                    startY = spriteParams.y;
                    dragging = false;
                    held = false;
                    touchStarted = SystemClock.uptimeMillis();
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
                        // She runs the way she is being taken, once that is clear of a wobble.
                        if (Math.abs(event.getRawX() - lastX) > slop * .5f) {
                            leftward = event.getRawX() < lastX;
                            lastX = event.getRawX();
                        }
                        moveTo(startX + Math.round(x), startY + Math.round(y));
                        if (dismiss != null) dismiss.setHot(overDismiss());
                        pose();
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
                    pose();
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
