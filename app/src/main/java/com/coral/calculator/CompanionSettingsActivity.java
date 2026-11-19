package com.coral.calculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** Settings for the floating Halgakos companion. She stays on screen here to show changes. */
public final class CompanionSettingsActivity extends Activity {
    private static final String AWAITING_PERMISSION = "awaiting_permission";
    private static final String[] SIZES = {"Small", "Medium", "Large"};
    private static final int FADE_STEP_MS = 500;

    private CompanionPrefs prefs;
    private boolean dark;
    private Switch master;
    private TextView masterNote;
    private TextView intro;
    private TextView fadeLabel;
    /** Set while a switch is moved from code, so that it is not taken for the user's choice. */
    private boolean updating;
    /** The system's permission screen was opened to turn her on. */
    private boolean awaitingPermission;

    @Override protected void onCreate(Bundle state) {
        dark = getSharedPreferences(CalculatorSession.PREFERENCES, MODE_PRIVATE).getBoolean("dark", false);
        setTheme(dark ? R.style.Theme_CoralCalculator_Dark : R.style.Theme_CoralCalculator);
        super.onCreate(state);
        prefs = new CompanionPrefs(this);
        awaitingPermission = state != null && state.getBoolean(AWAITING_PERMISSION);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(20), dp(24), dp(32));

        TextView title = text("Halgakos companion", 26, ink());
        title.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(48));
        closeParams.leftMargin = dp(12);
        heading.addView(closeButton(), closeParams);
        content.addView(heading);
        ImageView figure = new ImageView(this);
        figure.setImageResource(R.drawable.halgakos);
        figure.setContentDescription("Halgakos holding a calculator");
        LinearLayout.LayoutParams figureParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(168));
        figureParams.gravity = Gravity.CENTER_HORIZONTAL;
        figureParams.topMargin = dp(16);
        figureParams.bottomMargin = dp(8);
        content.addView(figure, figureParams);
        intro = text("", 16, muted());
        intro.setPadding(0, 0, 0, dp(12));
        content.addView(intro);
        describeGestures();

        master = toggle(content, "Show Halgakos", null, false, checked -> {
            if (checked && !Settings.canDrawOverlays(this)) {
                setChecked(master, false);
                askForPermission();
                return;
            }
            prefs.set(CompanionPrefs.ENABLED, checked);
            apply();
        });
        masterNote = text("", 15, muted());
        masterNote.setPadding(0, 0, dp(56), dp(8));
        content.addView(masterNote);

        toggle(content, "Animated Halgakos", "Her pink-haired form breathes and blinks, runs when you drag her, and "
                        + "works alongside the mini calculator. The still Halgakos uses less battery.",
                prefs.animated(), checked -> { prefs.set(CompanionPrefs.ANIMATED, checked); apply(); });
        toggle(content, "Tap for the mini calculator", "Holding her opens her menu. Switch this off to have a tap "
                        + "open the menu and a hold the mini calculator.",
                prefs.tapCalculator(), checked -> {
                    prefs.set(CompanionPrefs.TAP_CALCULATOR, checked);
                    describeGestures();
                    apply();
                });
        toggle(content, "Hide the mini calculator when you tap away", "A touch anywhere outside it closes it. Switch "
                        + "this off to keep it open while you use the app underneath.",
                prefs.closeOutside(), checked -> { prefs.set(CompanionPrefs.CLOSE_OUTSIDE, checked); apply(); });
        boolean foldable = FoldState.isFoldable(this);
        Switch unfolded = toggle(content, "Only while unfolded",
                foldable ? "She stays away while the device is folded shut, and returns when you open it."
                        : "For foldables. This device has no hinge to go by.",
                prefs.unfoldedOnly(), checked -> { prefs.set(CompanionPrefs.UNFOLDED_ONLY, checked); apply(); });
        unfolded.setEnabled(foldable);
        toggle(content, "Hide while the calculator is open", "She steps aside when you are already in the app.",
                prefs.hideInApp(), checked -> { prefs.set(CompanionPrefs.HIDE_IN_APP, checked); apply(); });
        toggle(content, "Snap to the edge", "She slides to the nearer side of the screen when you let go, rather "
                        + "than staying where you drop her.",
                prefs.snap(), checked -> { prefs.set(CompanionPrefs.SNAP, checked); apply(); });
        SeekBar fadeAfter = new SeekBar(this);
        toggle(content, "Fade when resting", "She turns translucent once she has been left alone, so she covers less.",
                prefs.fade(), checked -> {
                    prefs.set(CompanionPrefs.FADE, checked);
                    fadeAfter.setEnabled(checked);
                    apply();
                });
        fadeLabel = text("", 16, ink());
        fadeLabel.setPadding(0, dp(4), 0, 0);
        content.addView(fadeLabel);
        fadeAfter.setMax((CompanionPrefs.FADE_LONGEST_MS - CompanionPrefs.FADE_SHORTEST_MS) / FADE_STEP_MS);
        fadeAfter.setProgress((prefs.fadeAfterMs() - CompanionPrefs.FADE_SHORTEST_MS) / FADE_STEP_MS);
        fadeAfter.setEnabled(prefs.fade());
        fadeAfter.setContentDescription("How long before she fades");
        fadeAfter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (!fromUser) return;
                prefs.setFadeAfterMs(CompanionPrefs.FADE_SHORTEST_MS + progress * FADE_STEP_MS);
                describeFade();
                // She wakes and fades again, to show the new timing.
                Companion.get(CompanionSettingsActivity.this).settingsChanged();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        // Room at each end for the whole of the thumb, which is otherwise cut off at the ends.
        fadeAfter.setPadding(dp(14), 0, dp(14), 0);
        content.addView(fadeAfter, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        describeFade();

        TextView sizeLabel = text("Size", 19, ink());
        sizeLabel.setPadding(0, dp(14), 0, dp(4));
        content.addView(sizeLabel);
        RadioGroup sizes = new RadioGroup(this);
        sizes.setOrientation(RadioGroup.HORIZONTAL);
        for (int i = 0; i < SIZES.length; i++) {
            RadioButton choice = new RadioButton(this);
            choice.setId(View.generateViewId());
            choice.setText(SIZES[i]);
            choice.setTextSize(17);
            choice.setTextColor(ink());
            choice.setMinHeight(dp(48));
            choice.setPadding(dp(2), 0, dp(18), 0);
            choice.setChecked(i == prefs.size());
            final int size = i;
            choice.setOnCheckedChangeListener((button, checked) -> {
                if (!checked) return;
                prefs.setSize(size);
                apply();
            });
            sizes.addView(choice);
        }
        content.addView(sizes);

        TextView tips = text("To hide her, drag her onto the ✕ that appears at the bottom of the screen, or choose "
                + "Hide Halgakos in her menu. The switch at the top brings her back.\n\nThe mini calculator "
                + "and the app share one calculation, so either "
                + "carries on where the other left off.", 15, muted());
        tips.setPadding(0, dp(20), 0, 0);
        content.addView(tips);

        TextView noticeLabel = text("Android's notification", 19, ink());
        noticeLabel.setPadding(0, dp(22), 0, dp(4));
        content.addView(noticeLabel);
        content.addView(text("While she is on screen, Android posts its own notification that Halgakos Calculator "
                + "is displaying over other apps. It belongs to Android, so its icon and wording are not the "
                + "calculator's to change, but you can switch it off.", 15, muted()));
        TextView notice = text("Turn off that notification  ›", 18, CalculatorLayout.CORAL);
        notice.setPadding(0, dp(12), 0, dp(12));
        notice.setMinHeight(dp(48));
        notice.setGravity(Gravity.CENTER_VERTICAL);
        notice.setFocusable(true);
        notice.setBackgroundResource(android.R.drawable.list_selector_background);
        notice.setOnClickListener(v -> openSystemNotice());
        content.addView(notice);

        // The page is a long one: it can be closed from its foot as well as its head.
        LinearLayout.LayoutParams footParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        footParams.topMargin = dp(20);
        content.addView(closeButton(), footParams);

        // On unfolded and tablet screens the settings keep a readable width in the middle.
        FrameLayout column = new FrameLayout(this);
        int width = Math.min(dp(getResources().getConfiguration().screenWidthDp), dp(600));
        column.addView(content, new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.addView(column);
        scroll.setBackgroundColor(dark ? 0xFF17191B : CalculatorLayout.BACKGROUND);
        setContentView(scroll);
        configureInsets(scroll);
    }

    @SuppressWarnings("deprecation")
    private void configureInsets(View root) {
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            int light = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            if (controller != null) controller.setSystemBarsAppearance(dark ? 0 : light, light);
        }
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            } else {
                view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        root.requestApplyInsets();
    }

    @Override protected void onResume() {
        super.onResume();
        if (awaitingPermission) {
            awaitingPermission = false;
            if (Settings.canDrawOverlays(this)) prefs.set(CompanionPrefs.ENABLED, true);
        }
        apply();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(AWAITING_PERMISSION, awaitingPermission);
    }

    /** Shows what is in effect, and has the companion follow the settings. */
    private void apply() {
        boolean permitted = Settings.canDrawOverlays(this);
        setChecked(master, prefs.enabled() && permitted);
        masterNote.setText(prefs.enabled() && !permitted
                ? "Android no longer lets the calculator display over other apps. Switch this on to allow it again."
                : "Halgakos appears over other apps once you leave the calculator.");
        Companion.get(this).settingsChanged();
    }

    private void askForPermission() {
        CompanionPermission.request(this, "Let Halgakos float over other apps",
                "She needs Android's permission to appear on top of other apps.",
                () -> awaitingPermission = true,
                () -> masterNote.setText("This device has no setting for displaying over other apps."));
    }

    private TextView closeButton() {
        TextView button = new TextView(this);
        button.setText("Close menu  ✕");
        button.setTextSize(17);
        button.setTextColor(android.graphics.Color.WHITE);
        button.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(20), 0, dp(20), 0);
        button.setBackground(CalculatorLayout.pill(CalculatorLayout.CORAL));
        button.setContentDescription("Close menu");
        button.setFocusable(true);
        button.setOnClickListener(v -> finish());
        return button;
    }

    private void describeFade() {
        int milliseconds = prefs.fadeAfterMs();
        String seconds = milliseconds % 1000 == 0 ? String.valueOf(milliseconds / 1000)
                : String.valueOf(milliseconds / 1000f);
        fadeLabel.setText("Fades after " + seconds + (milliseconds == 1000 ? " second" : " seconds"));
    }

    /**
     * Opens Android's setting for the notice it posts about this app's windows over other
     * apps. The notice is the system's, filed under a channel of its own for each app.
     */
    private void openSystemNotice() {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, "android")
                .putExtra(Settings.EXTRA_CHANNEL_ID, "com.android.server.wm.AlertWindowNotification - " + getPackageName());
        try {
            startActivity(intent);
        } catch (RuntimeException unavailable) {
            Toast.makeText(this, "Press and hold Android's notification, then switch it off there.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void describeGestures() {
        intro.setText("A mini Halgakos who floats over your other apps. " + (prefs.tapCalculator()
                ? "Tap her for the mini calculator, hold her for her menu,"
                : "Tap her for her menu, hold her for the mini calculator,")
                + " and drag her wherever she is out of the way.");
    }

    private interface Choice {
        void changed(boolean checked);
    }

    private Switch toggle(LinearLayout parent, String title, String summary, boolean checked, Choice choice) {
        Switch view = new Switch(this);
        view.setText(title);
        view.setTextSize(19);
        view.setTextColor(ink());
        view.setChecked(checked);
        view.setMinHeight(dp(48));
        view.setPadding(0, dp(10), 0, dp(2));
        view.setOnCheckedChangeListener((button, value) -> {
            if (!updating) choice.changed(value);
        });
        parent.addView(view);
        if (summary != null) {
            TextView note = text(summary, 15, muted());
            note.setPadding(0, 0, dp(56), dp(8));
            parent.addView(note);
        }
        return view;
    }

    private void setChecked(Switch view, boolean checked) {
        updating = true;
        view.setChecked(checked);
        updating = false;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setLineSpacing(0, 1.1f);
        return view;
    }

    private int ink() { return dark ? 0xFFF3F3F3 : 0xFF292B2A; }
    private int muted() { return dark ? 0xFF9CA2A6 : 0xFF6E716F; }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
