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
    /** Her birthday frames: holding the cake, offering it, a party popper, a cheer, a party horn, clapping; and the order she goes through them in. */
    private static final int PARTY_FRAMES = 6;
    private static final int[] PARTY_ORDER = {0, 0, 1, 1, 0, 2, 2, 3, 3, 4, 4, 4, 5, 3, 5, 3, 5, 3};
    private static final int[] CONFETTI = {0xFFFF5F7E, 0xFFFFC53D, 0xFF4FC3F7, 0xFF7BD88F, 0xFFB388FF};
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
            POSE_JUMP = PetArt.JUMP, POSE_SAD = PetArt.SAD, POSE_WORK = PetArt.WORK, POSE_WAVE = PetArt.WAVE;
    private static final int PET_COLUMNS = PetArt.COLUMNS;
    private static final int[] PET_FRAMES = PetArt.FRAMES;
    private static final int[] PET_FRAME_MS = PetArt.FRAME_MS;
    /** Idle is prank time: where she is allowed them, one comes this long after the last. */
    private static final long PRANK_SOONEST_MS = 6 * 60_000L, PRANK_LATEST_MS = 20 * 60_000L;
    /** Between those she plays small ones in her own window, this long apart, each over in a few seconds. */
    private static final long ANTIC_SOONEST_MS = 90_000L, ANTIC_LATEST_MS = 240_000L;
    private static final long ANTIC_MS = 3600, BRIEF_ANTIC_MS = 1500, WAVE_ANTIC_MS = 2000, LOOK_ANTIC_MS = 3200;
    private static final int NO_ANTIC = -1;
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
    private final Runnable antic = this::antic;
    private final Runnable endAntic = this::endAntic;
    private final Random random = new Random();

    /** Context of the overlay windows, themed for the views in them. */
    private Context ui;
    private WindowManager windows;
    private Bitmap art;
    /** The animated figure's frames, one pose to a row; loaded only while she is animated. */
    private Bitmap pet;
    /** The colour of the hair in those frames, and whether frames in another colour are being made. */
    private int hair;
    private boolean dyeing;
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
    /** The small prank she is playing in her own window, where she stood when it began, and her hop. */
    private boolean anticDue;
    private int playing = NO_ANTIC;
    private int anticX, anticY;
    private ValueAnimator hopping;
    /** Her look round, as one of her small pranks. */
    private ValueAnimator looking;
    /** When she was last tapped once, how long a second tap has to follow, and how often she has said so. */
    private static final long DOUBLE_TAP_MS = 400;
    private long lastTap;
    private int tapHints;
    private final Runnable tapHint = this::tapHint;
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
        if (shown && prefs.birthdayToday()) {
            if (!partyDue) {
                partyDue = true;
                handler.postDelayed(birthday, prefs.partiedToday() ? PARTY_AGAIN_MS : PARTY_FIRST_MS);
            }
        } else {
            partyDue = false;
            handler.removeCallbacks(birthday);
        }
        if (shown && prefs.pranks()) {
            if (!anticDue) {
                anticDue = true;
                handler.postDelayed(antic, ANTIC_SOONEST_MS + (long) (random.nextFloat() * (ANTIC_LATEST_MS - ANTIC_SOONEST_MS)));
            }
        } else {
            anticDue = false;
            handler.removeCallbacks(antic);
        }
    }

    // Small pranks of her own: a few seconds of mischief in her own window, while she is idle

    // Her user's birthday: she celebrates it when she is first seen that day, and again now and then while the day lasts

    private static final long PARTY_FIRST_MS = 4000, PARTY_AGAIN_MS = 40 * 60_000, PARTY_MS = 11_000;
    private boolean partyDue;
    private final Runnable birthday = this::birthday;

    private void birthday() {
        partyDue = false;
        if (sprite == null || sprite.away || !prefs.birthdayToday()) return;
        // Not in the middle of something else: she waits her moment.
        if (!idle() || playing != NO_ANTIC || bubble != null) {
            partyDue = true;
            handler.postDelayed(birthday, 8000);
            return;
        }
        prefs.setPartied();
        sprite.celebrate(PARTY_MS);
        if (prefs.chatter()) say(Chatter.birthday(random));
        partyDue = true;
        handler.postDelayed(birthday, PARTY_AGAIN_MS);
    }

    private void antic() {
        anticDue = false;
        // Not while she is still sliding somewhere: where she stands now is where she goes back to.
        boolean still = settling == null || !settling.isRunning();
        // Waving and looking round are drawn only for the animated Halgakos: the still one keeps to the rest.
        if (idle() && playing == NO_ANTIC && bubble == null && still) {
            int which;
            do which = random.nextInt(Chatter.antics());
            while (pet == null && (which == Chatter.WAVE || which == Chatter.LOOK || which == Chatter.BOUNCE));
            play(which);
        }
        schedule();
    }

    private void play(int which) {
        playing = which;
        anticX = spriteParams.x;
        anticY = spriteParams.y;
        // She says what she is up to, unless she has been asked to keep quiet.
        if (prefs.chatter()) say(Chatter.antic(which));
        Rect room = room();
        // Every turn and change of size is about her middle, unless the prank says otherwise.
        sprite.setPivotX(sprite.getWidth() / 2f);
        sprite.setPivotY(sprite.getHeight() / 2f);
        switch (which) {
            case Chatter.FLIP:
                // Head over heels, and she stays that way a while.
                sprite.animate().rotationX(180).setDuration(420);
                break;
            case Chatter.TWIRL:
                sprite.animate().rotationY(720).setDuration(1300);
                break;
            case Chatter.SHRINK:
                sprite.animate().scaleX(.35f).scaleY(.35f).setDuration(260);
                break;
            case Chatter.DASH:
                // Across to the far side of the screen, at a run.
                sprite.setPose(onLeft() ? POSE_RUN_RIGHT : POSE_RUN_LEFT);
                slideTo(onLeft() ? room.right : room.left);
                // She runs there, and stands once she has arrived.
                handler.postDelayed(this::pose, 260);
                break;
            case Chatter.PEEK:
                // Most of the way off the nearer edge, with only a little of her left showing.
                sprite.setPose(onLeft() ? POSE_RUN_LEFT : POSE_RUN_RIGHT);
                slideTo(onLeft() ? room.left - Math.round(spriteParams.width * .56f)
                        : room.right + Math.round(spriteParams.width * .56f));
                handler.postDelayed(this::pose, 260);
                break;
            case Chatter.WAVE:
                // A wave from where she stands, three times over.
                sprite.play(POSE_WAVE, 3);
                break;
            case Chatter.LOOK:
                // All the way round, twice, as if something were circling her.
                looking = ValueAnimator.ofInt(0, PetArt.LOOKS * 2);
                looking.setInterpolator(null);
                looking.setDuration(LOOK_ANTIC_MS - 200);
                looking.addUpdateListener(animation -> {
                    if (sprite != null) sprite.look((int) animation.getAnimatedValue() % PetArt.LOOKS);
                });
                looking.start();
                break;
            case Chatter.SWAY:
                // Rocking from side to side, as if to a tune only she can hear.
                looking = ValueAnimator.ofFloat(0, (float) (Math.PI * 6));
                looking.setInterpolator(null);
                looking.setDuration(ANTIC_MS - 400);
                looking.addUpdateListener(animation -> {
                    if (sprite != null) sprite.setRotation((float) Math.sin((float) animation.getAnimatedValue()) * 12);
                });
                looking.start();
                break;
            case Chatter.DUCK:
                // Down flat against the ground, as if something had been thrown at her.
                sprite.setPivotY(sprite.getHeight());
                sprite.animate().scaleY(.45f).setDuration(140);
                break;
            case Chatter.BOUNCE:
                sprite.play(POSE_JUMP, 3);
                break;
            default:
                // Three hops on the spot. Her window is no taller than she is, so it hops with her.
                hopping = ValueAnimator.ofFloat(0, (float) (Math.PI * 3));
                hopping.setDuration(BRIEF_ANTIC_MS - 200);
                hopping.addUpdateListener(animation -> {
                    if (sprite == null) return;
                    spriteParams.y = anticY - Math.round(Math.abs((float) Math.sin((float) animation.getAnimatedValue())) * dp(24));
                    update(sprite, spriteParams);
                    moveBubble();
                });
                hopping.start();
                break;
        }
        handler.postDelayed(endAntic, which == Chatter.TWIRL || which == Chatter.HOP || which == Chatter.BOUNCE
                || which == Chatter.DUCK ? BRIEF_ANTIC_MS
                : which == Chatter.WAVE ? WAVE_ANTIC_MS : which == Chatter.LOOK ? LOOK_ANTIC_MS : ANTIC_MS);
    }

    /** Puts her back as she was: the right way up, her own size, and where she stood. */
    private void endAntic() {
        handler.removeCallbacks(endAntic);
        int which = playing;
        playing = NO_ANTIC;
        if (hopping != null) hopping.cancel();
        hopping = null;
        if (looking != null) looking.cancel();
        looking = null;
        if (sprite == null || which == NO_ANTIC) return;
        sprite.look(PetArt.NOWHERE);
        sprite.setRotation(0);
        // Two turns are no turn: she is facing front again, so there is nothing to unwind.
        sprite.setRotationY(0);
        sprite.animate().rotationX(0).scaleX(1).scaleY(1).setDuration(260).setInterpolator(new OvershootInterpolator(2f));
        if (which == Chatter.HOP && spriteParams.y != anticY) {
            spriteParams.y = anticY;
            update(sprite, spriteParams);
            moveBubble();
        }
        if (which == Chatter.DASH || which == Chatter.PEEK) {
            sprite.setPose(anticX < spriteParams.x ? POSE_RUN_LEFT : POSE_RUN_RIGHT);
            slideTo(anticX);
            handler.postDelayed(this::pose, 260);
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
        dye();
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
        if (pet == null) {
            pet = BitmapFactory.decodeResource(app.getResources(), R.drawable.halgakos_pet);
            hair = Hair.PINK;
            dye();
        }
        return pet != null;
    }

    /**
     * Has her wear the hair colour chosen. Her frames in it take a moment to make, so they are made
     * away from the screen, and she changes when they are ready.
     */
    private void dye() {
        int wanted = Hair.chosen(app);
        if (pet == null || wanted == hair || dyeing) return;
        dyeing = true;
        new Thread(() -> {
            Bitmap frames = Hair.art(app, R.drawable.halgakos_pet, wanted);
            handler.post(() -> {
                dyeing = false;
                if (pet == null || frames == null) return;
                pet = frames;
                hair = wanted;
                if (sprite != null) sprite.dyed();
                // It may have been changed again while these were made.
                dye();
            });
        }, "hair").start();
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
        endAntic();
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
        endAntic();
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

    /** She fades once she has been left alone, but never with something open or while she is talking. */
    private void rest() {
        if (sprite == null || !prefs.fade() || menu != null || panel != null) return;
        if (bubble != null) {
            // Still talking: look again once she has had time to finish.
            handler.removeCallbacks(rest);
            handler.postDelayed(rest, prefs.fadeAfterMs());
            return;
        }
        sprite.animate().alpha(REST_ALPHA).setDuration(300);
    }

    /** A tap and a hold each open one of her two popups; which is which is a setting. */
    private void tapped() {
        wake();
        // With the setting on, opening the calculator takes two taps close together: a stray touch does not bring it up.
        // Whatever is open still closes at one.
        if (prefs.tapCalculator() && prefs.doubleTap() && panel == null) {
            long now = SystemClock.uptimeMillis();
            boolean second = now - lastTap <= DOUBLE_TAP_MS;
            lastTap = second ? 0 : now;
            handler.removeCallbacks(tapHint);
            if (!second) {
                // A lone tap gets a word from her about it, the first couple of times.
                if (tapHints < 2 && prefs.chatter()) handler.postDelayed(tapHint, DOUBLE_TAP_MS + 60);
                return;
            }
        }
        toggle(prefs.tapCalculator());
    }

    private void tapHint() {
        if (sprite == null || panel != null || menu != null || stage != null) return;
        tapHints++;
        say("Tap me twice for the calculator Sloppy~♡");
    }

    private void held() {
        wake();
        toggle(!prefs.tapCalculator());
    }

    private void toggle(boolean calculator) {
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
        moveBubble();
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
        moveBubble();
        // Not at once: the mini calculator may be about to open in the menu's place.
        handler.post(stepBack);
        wake();
    }

    /** Opens the mini calculator beside her, or her games in its place and at its size. */
    private void openPanel(boolean games) {
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
                    say(Chatter.lostOutput(random));
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
        moveBubble();
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
        return within(spriteParams, x, y) || (menu != null && within(menuParams, x, y))
                || (bubble != null && within(bubbleParams, x, y));
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
        moveBubble();
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
        moveBubble();
    }

    // What she says

    /**
     * Puts up a bubble with something she has to say, its tail pointing at her. It stays for
     * as long as it takes to read, through whatever is done to her meanwhile: tapped, held or
     * dragged, she keeps talking. Only a touch on the bubble itself sends it away early.
     */
    private void say(String words) {
        closeBubble();
        if (sprite == null) return;
        words = prefs.named(words);
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
        Speech speech = new Speech(ui, text, dark ? 0xFF1D2023 : Color.WHITE, dark ? 0xFF34383C : 0xFFE4E4E4, dp(8));
        speech.measure(View.MeasureSpec.makeMeasureSpec(Math.min(dp(236), span().width()), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        // The window is no bigger than the bubble and its tail: it has no margin to lie over her.
        bubbleParams = overlay(speech.getMeasuredWidth(), speech.getMeasuredHeight(), POPUP_FLAGS);
        bubbleParams.setTitle("Halgakos says");
        bubble = speech;
        placeBubble();
        if (!add(speech, bubbleParams)) {
            bubble = null;
            return;
        }
        appear(text);
        wake();
        // At full strength at once, not a moment later: she and her words arrive together.
        sprite.setAlpha(1);
        handler.postDelayed(closeBubble, BUBBLE_MS + BUBBLE_LETTER_MS * words.length());
    }

    private void closeBubble() {
        handler.removeCallbacks(closeBubble);
        if (bubble == null) return;
        remove(bubble);
        bubble = null;
        // She was kept from fading while she spoke: the wait before she fades starts again now.
        wake();
    }

    /** Puts the bubble where it belongs now that she, her menu or her card has moved, come or gone. */
    private void moveBubble() {
        if (bubble == null) return;
        placeBubble();
        update(bubble, bubbleParams);
    }

    /** What is seen of a popup's window: the card, without the margin for its shadow. */
    private static Rect seen(WindowManager.LayoutParams window, int inset) {
        return new Rect(window.x + inset, window.y + inset, window.x + window.width - inset, window.y + window.height - inset);
    }

    /**
     * Over her head, or under her feet where there is no room above, and never on top of
     * her, her menu, the mini calculator or her games: where one of those stands in the
     * bubble's column, the bubble goes over the top of it, or under it.
     */
    private void placeBubble() {
        Rect span = span();
        int width = bubbleParams.width, height = bubbleParams.height;
        Rect her = new Rect(spriteParams.x + pad, spriteParams.y + pad,
                spriteParams.x + spriteParams.width - pad, spriteParams.y + spriteParams.height - pad);
        int x = Math.max(span.left, Math.min(span.right - width, her.centerX() - width / 2));
        int top = her.top, bottom = her.bottom;
        Rect[] others = {menu == null ? null : seen(menuParams, ((Holder) menu).inset),
                panel == null ? null : seen(panelParams, ((Holder) panel).inset)};
        for (Rect other : others) {
            if (other == null || other.left >= x + width || other.right <= x) continue;
            top = Math.min(top, other.top);
            bottom = Math.max(bottom, other.bottom);
        }
        // Above unless only below has the room.
        boolean above = top - height >= span.top || bottom + height > span.bottom;
        bubbleParams.x = x;
        bubbleParams.y = above ? Math.max(span.top, top - height) : bottom;
        ((Speech) bubble).setTail(her.centerX() - x, !above);
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
        // In a build made for testing, a number kept under this name is the prank she plays, to look at one in particular.
        int asked = BuildConfig.DEBUG ? PlaySession.get(app).tally("prank_next") : 0;
        if (game == PlaySession.PRANK && PlayStage.isPrank(asked)) kind = asked;
        boolean prank = PlayStage.isPrank(kind);
        PlayStage view = new PlayStage(ui, kind, dark(), haptics(), art, animated() ? pet : null, played -> {
            // Left by going home, the game leaves nothing behind it on the home screen.
            boolean home = stage != null && stage.wentHome();
            closeStage();
            // After a game the others are offered again; a prank just ends.
            if (!PlayStage.isPrank(played) && !home) openPanel(true);
        });
        Rect screen = screen();
        stageParams = overlay(screen.width(), screen.height(), POPUP_FLAGS);
        stageParams.x = screen.left;
        stageParams.y = screen.top;
        stageParams.setTitle(prank ? "Halgakos's prank" : "Play with Halgakos, whole screen");
        // A game takes the keys as well, so that Back leaves it and not the app underneath.
        if (!prank) stageParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        view.setHome(spriteParams.x + spriteParams.width / 2f - screen.left,
                spriteParams.y + spriteParams.height - pad - screen.top, spriteParams.height - pad * 2);
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

    /** Something she says: a card of words, with a tail that points at her from above or below. */
    private static final class Speech extends FrameLayout {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.Path tail = new android.graphics.Path();
        private final int fill, edge, reach;
        private float tailX;
        private boolean tailUp;

        /** @param reach how far the tail reaches out of the card toward her */
        Speech(Context context, View words, int fill, int edge, int reach) {
            super(context);
            this.fill = fill;
            this.edge = edge;
            this.reach = reach;
            setWillNotDraw(false);
            // Room for the tail on both sides, so the bubble is one size whichever way it points.
            setPadding(0, reach, 0, reach);
            addView(words, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        /** @param x where she is, across this bubble; @param up she is above it, not below */
        void setTail(float x, boolean up) {
            tailX = x;
            tailUp = up;
            invalidate();
        }

        @Override protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            float half = reach * .85f;
            float x = Math.max(half * 2.2f, Math.min(getWidth() - half * 2.2f, tailX));
            // The base sits just inside the card, over its border, so the two read as one shape.
            float base = tailUp ? reach + 1.5f : getHeight() - reach - 1.5f, tip = tailUp ? 1 : getHeight() - 1;
            tail.reset();
            tail.moveTo(x - half, base);
            tail.lineTo(x, tip);
            tail.lineTo(x + half, base);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fill);
            canvas.drawPath(tail, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            paint.setColor(edge);
            canvas.drawPath(tail, paint);
        }
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
        /** How many more times the pose that is playing is to be played through. */
        private int again;
        /** The way she is looking, in place of any pose, or {@link PetArt#NOWHERE}. */
        private int gaze = PetArt.NOWHERE;
        /** Whether the next frame is on its way. */
        private boolean moving;
        /** Her birthday frames, read in when first wanted; which of them shows, or -1; and when the celebration ends. */
        private Bitmap party;
        private int partyFrame = -1, partyStep;
        private long partyUntil;
        private final Runnable revel = this::revel;
        private final Paint confettiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /** Kept on screen but unseen, waiting for a folded device to be opened. */
        boolean away;
        /** She has left this window for the whole screen, for a game or a prank. */
        boolean onStage;
        /** The touch now on her ended a small prank of hers, and is to do nothing else. */
        private boolean ignoring;

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
                    ? "Halgakos companion. " + (prefs.doubleTap() ? "Tap twice" : "Tap") + " for the mini calculator, hold for the menu, drag to move"
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
                int row = gaze != PetArt.NOWHERE ? PetArt.lookRow(gaze) : playing >= 0 ? playing : pose;
                int column = gaze != PetArt.NOWHERE ? PetArt.lookColumn(gaze) : frame;
                int width = pet.getWidth() / PET_COLUMNS, height = pet.getHeight() / PET_FRAMES.length;
                frameSource.set(column * width, row * height, (column + 1) * width, (row + 1) * height);
                // Celebrating, she is drawn from her birthday frames, which are the size and shape of her others.
                Bitmap sheet = pet;
                if (partyFrame >= 0 && party != null) {
                    sheet = party;
                    int wide = party.getWidth() / PARTY_FRAMES;
                    frameSource.set(partyFrame * wide, 0, (partyFrame + 1) * wide, party.getHeight());
                }
                frameTarget.set(pad, pad + pad * .3f, getWidth() - pad, getHeight() - pad + pad * .3f);
                canvas.drawBitmap(sheet, frameSource, frameTarget, silhouette);
                frameTarget.offset(0, -pad * .3f);
                canvas.drawBitmap(sheet, frameSource, frameTarget, paint);
                if (partyFrame >= 0) confetti(canvas);
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
        void play(int value) { play(value, 1); }

        /** Her frames are now in another colour of hair: her birthday frames are made again when next wanted. */
        void dyed() {
            party = null;
            invalidate();
        }

        /** Plays a pose through a number of times, then goes back to the one she keeps. */
        void play(int value, int times) {
            if (pet == null) return;
            playing = value;
            again = times - 1;
            frame = 0;
            resume();
        }

        /** Has her celebrate for a while: her birthday frames one after another, and confetti falling about her. */
        void celebrate(long forMs) {
            if (pet == null) return;
            if (party == null) party = Hair.art(app, R.drawable.halgakos_party, hair);
            partyUntil = android.os.SystemClock.uptimeMillis() + forMs;
            partyStep = 0;
            handler.removeCallbacks(revel);
            revel();
        }

        private void revel() {
            if (android.os.SystemClock.uptimeMillis() >= partyUntil || !isAttachedToWindow() || away) {
                partyFrame = -1;
                invalidate();
                return;
            }
            partyFrame = PARTY_ORDER[partyStep++ % PARTY_ORDER.length];
            invalidate();
            handler.postDelayed(revel, 300);
        }

        /** Bits of coloured paper falling past her, for as long as she celebrates. */
        private void confetti(Canvas canvas) {
            long now = android.os.SystemClock.uptimeMillis();
            float bit = pad * .42f;
            for (int piece = 0; piece < 16; piece++) {
                float speed = .3f + piece * 37 % 10 / 22f, fall = (now % 200_000 / 1000f * speed + piece * .173f) % 1f;
                float x = getWidth() * (piece * 61 % 100 / 100f) + (float) Math.sin(now / 300.0 + piece) * pad, y = fall * getHeight();
                confettiPaint.setColor(CONFETTI[piece % CONFETTI.length]);
                canvas.save();
                canvas.rotate((now / 4f + piece * 40) % 360, x, y);
                canvas.drawRect(x - bit, y - bit * .55f, x + bit, y + bit * .55f, confettiPaint);
                canvas.restore();
            }
            postInvalidateOnAnimation();
        }

        /** Has her look a way, in place of whatever pose she is in; {@link PetArt#NOWHERE} lets the pose show again. */
        void look(int way) {
            if (gaze == way) return;
            gaze = way;
            invalidate();
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
                if (again > 0) again--; else playing = -1;
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
            // A touch in the middle of one of her small pranks ends the prank, and does no more.
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && Companion.this.playing != NO_ANTIC) {
                endAntic();
                wake();
                ignoring = true;
                return true;
            }
            if (ignoring) {
                if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) ignoring = false;
                return true;
            }
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
