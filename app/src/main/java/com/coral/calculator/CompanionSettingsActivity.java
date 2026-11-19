package com.coral.calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Switch;
import android.widget.TextView;

/** Settings for the floating Halgakos companion. She stays on screen here to show changes. */
public final class CompanionSettingsActivity extends Activity {
    private static final String AWAITING_PERMISSION = "awaiting_permission";
    private static final String[] SIZES = {"Small", "Medium", "Large"};

    private CompanionPrefs prefs;
    private boolean dark;
    private Switch master;
    private TextView masterNote;
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
        content.addView(title);
        ImageView figure = new ImageView(this);
        figure.setImageResource(R.drawable.halgakos);
        figure.setContentDescription("Halgakos holding a calculator");
        LinearLayout.LayoutParams figureParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(168));
        figureParams.gravity = Gravity.CENTER_HORIZONTAL;
        figureParams.topMargin = dp(16);
        figureParams.bottomMargin = dp(8);
        content.addView(figure, figureParams);
        TextView intro = text("A mini Halgakos who floats over your other apps. Tap her for a menu, hold her "
                + "for the mini calculator, and drag her wherever she is out of the way.", 16, muted());
        intro.setPadding(0, 0, 0, dp(12));
        content.addView(intro);

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

        boolean foldable = FoldState.isFoldable(this);
        Switch unfolded = toggle(content, "Only while unfolded",
                foldable ? "She stays away while the device is folded shut, and returns when you open it."
                        : "For foldables. This device has no hinge to go by.",
                prefs.unfoldedOnly(), checked -> { prefs.set(CompanionPrefs.UNFOLDED_ONLY, checked); apply(); });
        unfolded.setEnabled(foldable);
        toggle(content, "Hide while the calculator is open", "She steps aside when you are already in the app.",
                prefs.hideInApp(), checked -> { prefs.set(CompanionPrefs.HIDE_IN_APP, checked); apply(); });
        toggle(content, "Snap to the edge", "She slides to the nearer side of the screen when you let go.",
                prefs.snap(), checked -> { prefs.set(CompanionPrefs.SNAP, checked); apply(); });
        toggle(content, "Fade when resting", "She turns translucent after a few seconds, so she covers less.",
                prefs.fade(), checked -> { prefs.set(CompanionPrefs.FADE, checked); apply(); });

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

        TextView tips = text("Drag her onto the ✕ that appears at the bottom of the screen to hide her; this "
                + "switch brings her back.\n\nThe mini calculator and the app share one calculation, so either "
                + "carries on where the other left off.", 15, muted());
        tips.setPadding(0, dp(20), 0, 0);
        content.addView(tips);

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
        new AlertDialog.Builder(this)
                .setTitle("Let Halgakos float over other apps")
                .setMessage("Android will show its “Display over other apps” list. Choose Halgakos Calculator "
                        + "there, allow it, and come back.\n\nThe calculator uses this only to draw the companion "
                        + "and her mini calculator.")
                .setNegativeButton("Not now", null)
                .setPositiveButton("Continue", (dialog, which) -> {
                    awaitingPermission = true;
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException missing) {
                        awaitingPermission = false;
                        masterNote.setText("This device has no setting for displaying over other apps.");
                    }
                })
                .show();
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
