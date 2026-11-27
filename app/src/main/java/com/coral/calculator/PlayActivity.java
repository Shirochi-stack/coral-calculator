package com.coral.calculator;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Random;

/**
 * The games played with Halgakos, on a screen of their own in the app: the same card the
 * companion opens over other apps, as large as the screen allows, with Halgakos beside it.
 * On an open foldable she takes one side of the fold and the card the other. The games that
 * are played all over the screen, and her pranks, go over this screen as they would any other.
 */
public final class PlayActivity extends Activity {
    private final Random random = new Random();
    private boolean dark;
    private boolean haptics;
    private FrameLayout root;
    private ImageView figure;
    private PlayView card;
    private PlayStage stage;
    private Bitmap art;
    private Bitmap pet;
    private FoldState fold;
    private int safeLeft, safeTop, safeRight, safeBottom;
    private boolean arranging;

    @Override protected void onCreate(Bundle state) {
        dark = getSharedPreferences(CalculatorSession.PREFERENCES, MODE_PRIVATE).getBoolean("dark", false);
        haptics = getSharedPreferences(CalculatorSession.PREFERENCES, MODE_PRIVATE).getBoolean("haptics", true);
        setTheme(dark ? R.style.Theme_CoralCalculator_Dark : R.style.Theme_CoralCalculator);
        super.onCreate(state);
        fold = new FoldState(this, this::arrangeSoon);
        art = BitmapFactory.decodeResource(getResources(), R.drawable.halgakos);
        if (new CompanionPrefs(this).animated()) pet = Hair.art(this, R.drawable.halgakos_pet, Hair.chosen(this));
        root = new FrameLayout(this);
        root.setBackgroundColor(dark ? 0xFF17191B : CalculatorLayout.BACKGROUND);
        setContentView(root);
        configureInsets();
        root.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) arrangeSoon();
        });
    }

    @SuppressWarnings("deprecation")
    private void configureInsets() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            int light = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            if (controller != null) controller.setSystemBarsAppearance(dark ? 0 : light, light);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | (dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR));
        }
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int left, top, right, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                left = safe.left; top = safe.top; right = safe.right; bottom = safe.bottom;
            } else {
                left = insets.getSystemWindowInsetLeft(); top = insets.getSystemWindowInsetTop();
                right = insets.getSystemWindowInsetRight(); bottom = insets.getSystemWindowInsetBottom();
            }
            if (left != safeLeft || top != safeTop || right != safeRight || bottom != safeBottom) {
                safeLeft = left; safeTop = top; safeRight = right; safeBottom = bottom;
                arrangeSoon();
            }
            return insets;
        });
        root.requestApplyInsets();
    }

    @Override protected void onStart() {
        super.onStart();
        fold.start();
        Companion.get(this).setAppScreenShown(true);
    }

    @Override protected void onStop() {
        fold.stop();
        Companion.get(this).setAppScreenShown(false);
        super.onStop();
    }

    @Override public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        arrangeSoon();
    }

    /** Not from inside a layout pass or an insets callback, which is where the news comes from. */
    private void arrangeSoon() {
        if (arranging) return;
        arranging = true;
        root.post(() -> {
            arranging = false;
            arrange();
        });
    }

    private int hinge() {
        return fold.hinge(getResources().getConfiguration().smallestScreenWidthDp,
                Build.VERSION.SDK_INT >= 30 && getDisplay() != null ? getDisplay().getRotation() : 0);
    }

    /** Sizes and places Halgakos and the card for the room there is, and for the fold if there is one. */
    private void arrange() {
        int width = root.getWidth() - safeLeft - safeRight, height = root.getHeight() - safeTop - safeBottom;
        if (width <= 0 || height <= 0 || isFinishing()) return;
        float density = getResources().getDisplayMetrics().density;
        int hinge = hinge();
        // The two places: hers, and the card's. Side by side on a wide screen and across an
        // upright fold; one over the other on a tall one and across a level fold.
        boolean beside = hinge == FoldState.UPRIGHT_HINGE || (hinge == FoldState.NO_HINGE && width > height * 1.15f);
        int margin = dp(16);
        float figureShape = art.getWidth() / (float) art.getHeight();
        int herLeft, herTop, herWidth, herHeight, cardLeft, cardTop, cardWidth, cardHeight;
        // The menu is what the screen is for: her picture keeps a modest share, and the card has the rest.
        if (beside) {
            int hers = hinge == FoldState.UPRIGHT_HINGE ? Math.round(width * .3f) : Math.min(Math.round(width * .26f), dp(240));
            herLeft = 0; herTop = 0; herWidth = hers; herHeight = height;
            cardLeft = hers; cardTop = 0; cardWidth = width - hers; cardHeight = height;
        } else {
            int hers = hinge == FoldState.LEVEL_HINGE ? Math.round(height * .3f) : Math.min(Math.round(height * .15f), dp(140));
            herLeft = 0; herTop = 0; herWidth = width; herHeight = hers;
            cardLeft = 0; cardTop = hers; cardWidth = width; cardHeight = height - hers;
        }
        float scale = Math.min((cardWidth - margin * 2) / (PlayView.WIDTH_DP * density),
                (cardHeight - margin * 2) / (PlayView.HEIGHT_DP * density));
        // Without her picture where a small screen has no room for both.
        boolean pictured = scale >= .95f || hinge != FoldState.NO_HINGE;
        if (!pictured) {
            cardLeft = 0; cardTop = 0; cardWidth = width; cardHeight = height;
            scale = Math.min((cardWidth - margin * 2) / (PlayView.WIDTH_DP * density),
                    (cardHeight - margin * 2) / (PlayView.HEIGHT_DP * density));
        }
        float unit = density * Math.max(.62f, Math.min(2.3f, scale));
        // The card fills the room it is given, rather than keeping the mini calculator's shape and
        // leaving the rest of an unfolded or tablet screen empty. Its tiles stretch with it; it only
        // stops short of growing so wide that a row of two tiles becomes a strip.
        int roomWide = cardWidth - margin * 2, roomHigh = cardHeight - margin * 2;
        int cardPixelsHigh = Math.max(Math.round(PlayView.HEIGHT_DP * unit), roomHigh);
        int cardPixelsWide = Math.max(Math.round(PlayView.WIDTH_DP * unit), Math.min(roomWide, Math.round(cardPixelsHigh * 1.35f)));

        if (card != null) root.removeView(card);
        if (figure != null) root.removeView(figure);
        figure = null;
        int high = Math.min(herHeight - margin * 2, Math.min(dp(320), Math.round((herWidth - margin * 2) / figureShape)));
        int wide = Math.round(high * figureShape);
        int herY = herTop + (herHeight - high) / 2, cardY = cardTop + Math.max(0, (cardHeight - cardPixelsHigh) / 2);
        if (pictured && !beside) {
            if (hinge == FoldState.NO_HINGE) {
                // One over the other with no fold between: the two are centred as one.
                herY = Math.max(margin, (height - high - margin / 2 - cardPixelsHigh) / 2);
                cardY = herY + high + margin / 2;
            } else {
                // Across a level fold she stands on it, at the foot of the upper half.
                herY = herTop + herHeight - high - margin / 2;
            }
        }
        if (pictured) {
            figure = new ImageView(this);
            figure.setImageBitmap(art);
            figure.setContentDescription("Halgakos holding a calculator");
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(wide, high, Gravity.TOP | Gravity.LEFT);
            params.leftMargin = safeLeft + herLeft + (herWidth - wide) / 2;
            params.topMargin = safeTop + herY;
            root.addView(figure, 0, params);
            if (stage != null) figure.setVisibility(View.INVISIBLE);
        }
        card = new PlayView(this, unit, dark, haptics, true, new PlayView.Host() {
            @Override public void onOpenApp() {}
            @Override public void onClose() { finish(); }
            @Override public void onMood(boolean cheerful) { react(cheerful); }
            @Override public void onStage(int game) { openStage(game); }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cardPixelsWide, cardPixelsHigh, Gravity.TOP | Gravity.LEFT);
        params.leftMargin = safeLeft + cardLeft + (cardWidth - cardPixelsWide) / 2;
        params.topMargin = safeTop + cardY;
        card.setElevation(dp(6));
        root.addView(card, figure == null ? 0 : 1, params);
        if (stage != null) fitStage();
    }

    /** Her picture hops when a game has pleased her, and sinks a little when it has not. */
    private void react(boolean cheerful) {
        ImageView her = figure;
        if (her == null) return;
        her.animate().cancel();
        her.setTranslationY(0);
        her.animate().translationY(cheerful ? -dp(14) : dp(6)).setDuration(cheerful ? 140 : 220).withEndAction(() ->
                her.animate().translationY(0).setDuration(260).setInterpolator(new OvershootInterpolator(cheerful ? 3f : 1f)));
    }

    /** She steps out of her picture and has the whole screen, for a game or a prank. */
    private void openStage(int game) {
        if (stage != null || root.getWidth() == 0) return;
        PlaySession kept = PlaySession.get(this);
        Unlocks.check(kept, BuildConfig.DEBUG);
        int kind = game == PlaySession.PRANK ? PlayStage.anyPrank(random, Unlocks.tricks(kept)) : game;
        // A trick newly unlocked is the next one played.
        int fresh = game == PlaySession.PRANK ? Unlocks.freshTrick(kept) : 0;
        if (fresh != 0) kind = fresh;
        stage = new PlayStage(this, kind, dark, haptics, art, pet, played -> {
            // Left for the home screen, a long game waits here on its pause, and is found as it was when the app is come back to.
            if (stage.wentHome() && stage.hold()) {
                stage.comeBack();
                return;
            }
            root.removeView(stage);
            stage = null;
            if (figure != null) figure.setVisibility(View.VISIBLE);
            // What was played may have unlocked something: she says so in her own time, over the other apps.
            Unlocks.check(PlaySession.get(this), BuildConfig.DEBUG);
            PlaySession.get(this).refresh();
        });
        if (figure != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) figure.getLayoutParams();
            stage.setHome(params.leftMargin + params.width / 2f, params.topMargin + params.height);
            figure.setVisibility(View.INVISIBLE);
        }
        stage.setElevation(dp(12));
        root.addView(stage, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        fitStage();
    }

    private void fitStage() {
        stage.setSafeInsets(safeLeft, safeTop, safeRight, safeBottom);
        stage.setHinge(hinge());
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
