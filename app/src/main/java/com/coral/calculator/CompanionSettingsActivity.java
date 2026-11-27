package com.coral.calculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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

/**
 * Settings for the floating Halgakos companion. This counts as one of the app's own screens,
 * so she is on it, showing changes as they are made, only if she is not kept out of the app.
 */
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
    /** How much of what playing unlocks has been found: a count, and nothing of what the rest is. */
    private TextView found;
    /** Shows the hair colours again: which are won, and which is worn. */
    private Runnable showHair;
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
        // The page takes the focus itself, so the name field does not bring up the keyboard unasked.
        content.setFocusableInTouchMode(true);

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

        section(content, "Mini calculator");
        toggle(content, "Close it when you tap away", "Touching anywhere outside the mini calculator closes it, "
                        + "and the same goes for the card of games. Switch this off to keep either open while "
                        + "you use the app underneath.",
                prefs.closeOutside(), checked -> { prefs.set(CompanionPrefs.CLOSE_OUTSIDE, checked); apply(); });
        toggle(content, "Open it with a tap", "A tap on Halgakos opens the mini calculator and a hold opens her "
                        + "menu. Switch this off to swap the two.",
                prefs.tapCalculator(), checked -> {
                    prefs.set(CompanionPrefs.TAP_CALCULATOR, checked);
                    describeGestures();
                    apply();
                });
        toggle(content, "Two taps to open it", "Where a tap opens the mini calculator, it takes two taps close together, so a "
                        + "stray touch on Halgakos does not bring it up. One tap still closes it. Switch this off to open it "
                        + "with a single tap.",
                prefs.doubleTap(), checked -> {
                    prefs.set(CompanionPrefs.DOUBLE_TAP, checked);
                    describeGestures();
                    apply();
                });

        section(content, "Play with Halgakos");
        content.addView(text("Games to play with her. Plus run, at the top of them, is a run and jump through a hundred "
                + "lands, with a story told in voices, bosses, quests, and hair colours to win. Page brawl is a fight "
                + "between her and her friends. There are "
                + "also chess, minesweeper, a card game, tic-tac-toe and more, some on a card where the mini "
                + "calculator opens and some over the whole screen. Hold her and choose Play with Halgakos, or "
                + "start here.", 15, muted()));
        TextView play = text("Play with Halgakos  ›", 18, CalculatorLayout.CORAL);
        play.setPadding(0, dp(12), 0, dp(12));
        play.setMinHeight(dp(48));
        play.setGravity(Gravity.CENTER_VERTICAL);
        play.setFocusable(true);
        play.setBackgroundResource(android.R.drawable.list_selector_background);
        play.setOnClickListener(v -> startActivity(new Intent(this, PlayActivity.class)));
        content.addView(play);
        found = text("", 14, muted());
        found.setPadding(0, 0, dp(56), dp(8));
        content.addView(found);
        describeFound();
        toggle(content, "Games first", "The tap on Halgakos that opens the mini calculator opens her games in its place: two taps "
                        + "with the setting above on. The mini calculator is still in her menu.",
                prefs.tapGames(), checked -> {
                    prefs.set(CompanionPrefs.TAP_GAMES, checked);
                    describeGestures();
                    apply();
                });
        toggle(content, "Open the app on the games", "Opened from its icon, the app starts on Play with Halgakos. The calculator "
                        + "is one step back, and the game pad beside her on the calculator opens the games at any time.",
                prefs.startGames(), checked -> prefs.set(CompanionPrefs.START_GAMES, checked));
        toggle(content, "Speech bubbles", "Now and then she has something to say, in a bubble over her head, and "
                        + "most of it is teasing. A tap on the bubble sends it away.",
                prefs.chatter(), checked -> { prefs.set(CompanionPrefs.CHATTER, checked); apply(); });
        nameField(content);
        birthdayField(content);
        toggle(content, "Sounds in Plus run", "Plus run plays sound effects, and Halgakos tells its story aloud. Switch this "
                        + "off to play it in silence, with her words on screen only. The speaker button on its map does "
                        + "the same. Her voice is heard while she calls you Sloppy, the name it was recorded with.",
                prefs.gameSound(), checked -> prefs.set(CompanionPrefs.GAME_SOUND, checked));
        toggle(content, "Surprise pranks", "Left idle, every several minutes she plays a prank over whatever is on "
                        + "screen: binning one of your apps, shaking them all loose, cracking the screen, turning "
                        + "it off, dropping her ice cream on it and more. In between she plays little ones "
                        + "where she stands: a hop, a twirl, a dash across the screen. It is only a drawing over the top. "
                        + "Nothing is deleted or changed, she owns up within seconds, and a tap ends it at once. "
                        + "Prank me, among her games, plays one when you ask.",
                prefs.pranks(), checked -> { prefs.set(CompanionPrefs.PRANKS, checked); apply(); });

        section(content, "Halgakos");
        toggle(content, "Animated Halgakos", "Her pink-haired form breathes and blinks, runs when you drag her, and "
                        + "works alongside the mini calculator. Switch this off for the still Halgakos, who uses "
                        + "less battery.",
                prefs.animated(), checked -> {
                    prefs.set(CompanionPrefs.ANIMATED, checked);
                    apply();
                    showHair.run();
                });
        hairField(content);
        toggle(content, "Keep her out of the calculator app", "She steps aside while the calculator app is on "
                        + "screen, these settings included, and returns when you leave it. This is about "
                        + "Halgakos herself, not the mini calculator.",
                prefs.hideInApp(), checked -> { prefs.set(CompanionPrefs.HIDE_IN_APP, checked); apply(); });
        boolean foldable = FoldState.isFoldable(this);
        Switch unfolded = toggle(content, "Only while unfolded",
                foldable ? "She stays away while the device is folded shut, and returns when you open it."
                        : "For foldables. This device has no hinge to go by.",
                prefs.unfoldedOnly(), checked -> { prefs.set(CompanionPrefs.UNFOLDED_ONLY, checked); apply(); });
        unfolded.setEnabled(foldable);
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

        TextView sizeLabel = text("Her size", 19, ink());
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

        TextView tips = text("To hide her, drag her onto the ✕ that appears at the bottom of the screen. The switch "
                + "at the top brings her back.\n\nClose app in her menu closes the calculator and takes her with "
                + "it. She returns when you next open the calculator.\n\nThe mini calculator "
                + "and the app share one calculation, so either "
                + "carries on where the other left off.", 15, muted());
        tips.setPadding(0, dp(20), 0, 0);
        content.addView(tips);

        section(content, "Android's notification");
        content.addView(text("While she is on screen, Android posts its own notification that Halgakos "
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

        // Which release this is, since one from before 1.2.0 can sit beside it looking the same.
        TextView version = text(getString(R.string.app_title) + " " + BuildConfig.VERSION_NAME, 14, muted());
        version.setPadding(0, dp(16), 0, 0);
        content.addView(version);
        if (OldCopy.installed(this)) {
            TextView old = text("An older copy is installed beside this one, with settings of its own. "
                    + "Remove it  ›", 16, CalculatorLayout.CORAL);
            old.setPadding(0, dp(10), 0, dp(10));
            old.setFocusable(true);
            old.setBackgroundResource(android.R.drawable.list_selector_background);
            old.setOnClickListener(v -> OldCopy.open(this));
            content.addView(old);
        }

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
                // The keyboard as well, so the field being typed in can be scrolled clear of it.
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout()
                        | WindowInsets.Type.ime());
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            } else {
                view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        root.requestApplyInsets();
    }

    @Override protected void onStart() {
        super.onStart();
        Companion.get(this).setAppScreenShown(true);
    }

    @Override protected void onStop() {
        Companion.get(this).setAppScreenShown(false);
        super.onStop();
    }

    @Override protected void onResume() {
        super.onResume();
        if (awaitingPermission) {
            awaitingPermission = false;
            if (Settings.canDrawOverlays(this)) prefs.set(CompanionPrefs.ENABLED, true);
        }
        apply();
        describeFound();
        // A colour may have been won in Plus run since this page was last looked at.
        if (showHair != null) showHair.run();
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
                : prefs.hideInApp() ? "Halgakos appears over other apps once you leave the calculator."
                : "Halgakos is on screen, over this app and your others.");
        Companion.get(this).settingsChanged();
    }

    private void askForPermission() {
        CompanionPermission.request(this, "Let Halgakos float over other apps",
                "She needs Android's permission to appear on top of other apps.",
                () -> awaitingPermission = true,
                () -> masterNote.setText("This device has no setting for displaying over other apps."));
    }

    /**
     * What she calls the user. Her catchphrase is that name with "~♡" after it, at the end of
     * what she says; "Sloppy" is hers, and this is where to have her use another.
     */
    private void nameField(LinearLayout parent) {
        TextView label = text("What she calls you", 19, ink());
        label.setPadding(0, dp(10), 0, 0);
        parent.addView(label);
        EditText name = new EditText(this);
        name.setSingleLine(true);
        // Left empty unless another name was chosen: the hint shows the one she uses then.
        name.setText(prefs.nickname().equals(Chatter.DEFAULT_NAME) ? "" : prefs.nickname());
        name.setHint(Chatter.DEFAULT_NAME);
        name.setTextSize(19);
        name.setTextColor(ink());
        name.setHintTextColor(muted());
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CompanionPrefs.NICKNAME_LENGTH)});
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        name.setImeOptions(EditorInfo.IME_ACTION_DONE);
        name.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        name.setContentDescription("What Halgakos calls you");
        name.setMinHeight(dp(48));
        parent.addView(name, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView example = text("", 15, muted());
        example.setPadding(0, 0, dp(56), dp(8));
        parent.addView(example);
        Runnable show = () -> example.setText("She ends what she says with it: \""
                + prefs.named("Play with me Sloppy~♡") + "\". Leave it empty and she calls you Sloppy.");
        name.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable text) {
                prefs.setNickname(text.toString());
                show.run();
            }
        });
        show.run();
    }

    /**
     * The user's birthday: the day and the month only. On that day she celebrates, with a cake
     * and confetti, when she is first seen and now and then while the day lasts.
     */
    private void birthdayField(LinearLayout parent) {
        TextView label = text("Your birthday", 19, ink());
        label.setPadding(0, dp(10), 0, 0);
        parent.addView(label);
        TextView value = text("", 18, CalculatorLayout.CORAL);
        value.setPadding(0, dp(10), 0, dp(10));
        value.setMinHeight(dp(48));
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setFocusable(true);
        value.setBackgroundResource(android.R.drawable.list_selector_background);
        parent.addView(value);
        TextView note = text("", 15, muted());
        note.setPadding(0, 0, dp(56), dp(8));
        parent.addView(note);
        Runnable show = () -> {
            int day = prefs.birthday();
            String[] months = new java.text.DateFormatSymbols(java.util.Locale.ENGLISH).getMonths();
            value.setText(day == 0 ? "Not set  ›" : day % 100 + " " + months[day / 100 - 1] + "  ›");
            value.setContentDescription(day == 0 ? "Your birthday: not set. Tap to choose the day"
                    : "Your birthday: " + day % 100 + " " + months[day / 100 - 1] + ". Tap to change it");
            note.setText(day == 0 ? "Tell Halgakos the day and the month, and she celebrates it with you when it comes. The year is not asked for, "
                    + "and the date stays on this device."
                    : "On that day she celebrates, with a cake and confetti, when she is first on screen and now and then through the day.");
        };
        value.setOnClickListener(view -> {
            int day = prefs.birthday();
            // A leap year, so that the 29th of February can be chosen; the year itself is thrown away.
            android.app.DatePickerDialog picker = new android.app.DatePickerDialog(this, (dialog, year, month, date) -> {
                prefs.setBirthday((month + 1) * 100 + date);
                show.run();
                apply();
            }, 2000, day == 0 ? java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) : day / 100 - 1,
                    day == 0 ? java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) : day % 100);
            picker.setButton(android.content.DialogInterface.BUTTON_NEUTRAL, "Clear", (dialog, which) -> {
                prefs.setBirthday(0);
                show.run();
                apply();
            });
            picker.show();
        });
        show.run();
    }

    /**
     * The colour of the animated Halgakos's hair: her own pink, and each colour she has won in
     * Plus run. One not yet won is shown locked.
     */
    private void hairField(LinearLayout parent) {
        TextView label = text("Hair colour", 19, ink());
        label.setPadding(0, dp(10), 0, dp(4));
        parent.addView(label);
        LinearLayout across = new LinearLayout(this);
        across.setGravity(Gravity.CENTER_VERTICAL);
        ImageView preview = new ImageView(this);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        across.addView(preview, new LinearLayout.LayoutParams(dp(66), dp(88)));
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rowsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowsParams.leftMargin = dp(6);
        across.addView(rows, rowsParams);
        parent.addView(across);
        TextView note = text("", 15, muted());
        note.setPadding(0, dp(4), dp(56), dp(8));
        parent.addView(note);
        // A colour of the user's own, and a light about her: both open once every colour is won.
        LinearLayout mixer = new LinearLayout(this);
        mixer.setOrientation(LinearLayout.VERTICAL);
        parent.addView(mixer);
        LinearLayout own = new LinearLayout(this);
        own.setOrientation(LinearLayout.VERTICAL);
        mixer.addView(own);
        EditText hex = new EditText(this);
        hex.setSingleLine(true);
        hex.setTextSize(18);
        hex.setTextColor(ink());
        hex.setHintTextColor(muted());
        hex.setHint("#F4A3BE");
        hex.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        hex.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        hex.setImeOptions(EditorInfo.IME_ACTION_DONE);
        hex.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        hex.setContentDescription("Hair colour as a hex code");
        hex.setMinHeight(dp(48));
        TextView hexLabel = text("Hex code", 15, muted());
        own.addView(hexLabel);
        own.addView(hex, new LinearLayout.LayoutParams(dp(150), LinearLayout.LayoutParams.WRAP_CONTENT));
        String[] parts = {"Hue", "Strength", "Brightness"};
        int[] most = {360, 100, 100};
        SeekBar[] bars = new SeekBar[3];
        for (int part = 0; part < 3; part++) {
            TextView name = text(parts[part], 15, muted());
            name.setPadding(0, dp(6), 0, 0);
            own.addView(name);
            bars[part] = new SeekBar(this);
            bars[part].setMax(most[part]);
            bars[part].setContentDescription(parts[part] + " of the hair colour");
            own.addView(bars[part], new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        }
        Switch glow = toggle(mixer, "Glow", "A soft light about her in the colour of her hair, over your other apps.",
                prefs.hairGlow(), checked -> { prefs.set(CompanionPrefs.HAIR_GLOW, checked); apply(); });
        // Set while the fields are filled in from code, so that it is not taken for the user's mixing.
        boolean[] filling = {false};
        Runnable fill = () -> {
            filling[0] = true;
            int colour = 0xFF000000 | prefs.hairCustom();
            float[] of = new float[3];
            android.graphics.Color.colorToHSV(colour, of);
            bars[0].setProgress(Math.round(of[0]));
            bars[1].setProgress(Math.round(of[1] * 100));
            bars[2].setProgress(Math.round(of[2] * 100));
            String code = String.format(java.util.Locale.ROOT, "#%06X", colour & 0xFFFFFF);
            if (!code.equalsIgnoreCase(hex.getText().toString())) hex.setText(code);
            filling[0] = false;
        };
        Swatch[] swatches = new Swatch[Hair.CUSTOM + 1];
        // Which preview was asked for last: one made for an earlier choice is not shown over it.
        int[] asked = {-1};
        showHair = () -> {
            int owned = Hair.owned(this), worn = Hair.chosen(this), won = Integer.bitCount(owned & (1 << Hair.NAMES.length - 1) - 1);
            boolean animated = prefs.animated(), all = Hair.all(this);
            for (Swatch swatch : swatches) {
                swatch.owned = swatch.colour == Hair.CUSTOM ? all : Hair.owns(owned, swatch.colour);
                swatch.worn = swatch.colour == Hair.CUSTOM ? Hair.custom(worn) : swatch.colour == worn;
                swatch.setAlpha(animated ? 1 : .45f);
                swatch.setContentDescription(hairName(swatch.colour) + (swatch.worn ? ", chosen" : swatch.owned ? "" : ", not won yet"));
                swatch.invalidate();
            }
            mixer.setVisibility(all && animated ? View.VISIBLE : View.GONE);
            own.setVisibility(Hair.custom(worn) ? View.VISIBLE : View.GONE);
            setChecked(glow, prefs.hairGlow());
            note.setText(!animated ? "The colours are for the animated Halgakos. The still one keeps her white hair."
                    : all ? "Every colour is won. She wears the one you choose, here and over your other apps. The last round is a colour of "
                    + "your own: mix it below, or type its hex code."
                    : "Pink is her own. The others are won in Plus run, from its bosses: " + won + " of " + (Hair.NAMES.length - 1)
                    + " won so far. One that is won can be worn here.");
            if (asked[0] == worn) return;
            asked[0] = worn;
            new Thread(() -> {
                android.graphics.Bitmap sheet = Hair.art(this, R.drawable.halgakos_pet, worn);
                if (sheet == null) return;
                android.graphics.Bitmap first = android.graphics.Bitmap.createBitmap(sheet, 0, 0, sheet.getWidth() / PetArt.COLUMNS,
                        sheet.getHeight() / PetArt.FRAMES.length);
                runOnUiThread(() -> {
                    if (asked[0] == worn) preview.setImageBitmap(first);
                });
            }, "hair").start();
        };
        for (int colour = 0; colour < swatches.length; colour++) {
            if (colour % 5 == 0) rows.addView(new LinearLayout(this));
            Swatch swatch = new Swatch(colour);
            swatches[colour] = swatch;
            swatch.setOnClickListener(view -> {
                if (!prefs.animated()) return;
                if (!swatch.owned) {
                    note.setText(swatch.colour == Hair.CUSTOM ? "A colour of your own, and a glow, open once all eight colours are won in Plus run."
                            : Hair.NAMES[swatch.colour] + " is not won yet. It is one of the rewards of Plus run.");
                    return;
                }
                prefs.setHair(swatch.colour);
                if (swatch.colour == Hair.CUSTOM) fill.run();
                showHair.run();
                apply();
            });
            ((LinearLayout) rows.getChildAt(rows.getChildCount() - 1)).addView(swatch, new LinearLayout.LayoutParams(dp(46), dp(48)));
        }
        // Mixing: the bars change the code as they move, and she changes when they are let go; a whole code typed changes her at once.
        SeekBar.OnSeekBarChangeListener mixed = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean byUser) {
                if (!byUser || filling[0]) return;
                int colour = android.graphics.Color.HSVToColor(new float[]{bars[0].getProgress(), bars[1].getProgress() / 100f, bars[2].getProgress() / 100f});
                prefs.setHairCustom(colour);
                filling[0] = true;
                hex.setText(String.format(java.util.Locale.ROOT, "#%06X", colour & 0xFFFFFF));
                filling[0] = false;
                swatches[Hair.CUSTOM].invalidate();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {
                showHair.run();
                apply();
            }
        };
        for (SeekBar bar : bars) bar.setOnSeekBarChangeListener(mixed);
        hex.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable text) {
                if (filling[0]) return;
                String code = text.toString().trim();
                if (code.startsWith("#")) code = code.substring(1);
                if (!code.matches("[0-9A-Fa-f]{6}")) return;
                prefs.setHairCustom(Integer.parseInt(code, 16));
                fill.run();
                showHair.run();
                apply();
            }
        });
        fill.run();
        showHair.run();
    }

    private static String hairName(int colour) { return colour == Hair.CUSTOM ? "A colour of your own" : Hair.NAMES[colour] + " hair"; }

    /** One hair colour to choose: a round of it, ringed when it is the one worn, and with a padlock over it until it is won. */
    private final class Swatch extends View {
        final int colour;
        boolean owned, worn;
        private final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.RectF box = new android.graphics.RectF();

        Swatch(int colour) {
            super(CompanionSettingsActivity.this);
            this.colour = colour;
            setFocusable(true);
        }

        @Override protected void onDraw(android.graphics.Canvas canvas) {
            float x = getWidth() / 2f, y = getHeight() / 2f, round = dp(15);
            paint.setStyle(android.graphics.Paint.Style.FILL);
            if (colour == Hair.CUSTOM) {
                // The round for a colour of the user's own: every hue round its rim, and the colour mixed in its middle.
                paint.setShader(new android.graphics.SweepGradient(x, y, new int[]{0xFFE0362F, 0xFFF2C230, 0xFF3FB56A, 0xFF3B7BE0, 0xFF9A4FD0, 0xFFE0362F}, null));
                paint.setAlpha(owned ? 255 : 80);
                canvas.drawCircle(x, y, round, paint);
                paint.setShader(null);
                paint.setColor(0xFF000000 | prefs.hairCustom());
                paint.setAlpha(owned ? 255 : 0);
                canvas.drawCircle(x, y, round * .62f, paint);
            } else {
                paint.setColor(Hair.COLOURS[colour]);
                paint.setAlpha(owned ? 255 : 80);
                canvas.drawCircle(x, y, round, paint);
            }
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(muted());
            paint.setAlpha(owned ? 160 : 90);
            canvas.drawCircle(x, y, round, paint);
            if (worn) {
                paint.setStrokeWidth(dp(2.5f));
                paint.setColor(CalculatorLayout.CORAL);
                canvas.drawCircle(x, y, round + dp(4.5f), paint);
            }
            if (!owned) {
                // A padlock: its bow, then its body.
                paint.setColor(ink());
                paint.setStrokeWidth(dp(1.8f));
                box.set(x - dp(3.6f), y - dp(7.5f), x + dp(3.6f), y + dp(1.5f));
                canvas.drawArc(box, 180, 180, false, paint);
                canvas.drawLine(box.left, box.centerY(), box.left, y - dp(.5f), paint);
                canvas.drawLine(box.right, box.centerY(), box.right, y - dp(.5f), paint);
                paint.setStyle(android.graphics.Paint.Style.FILL);
                box.set(x - dp(6), y - dp(1.5f), x + dp(6), y + dp(7.5f));
                canvas.drawRoundRect(box, dp(1.8f), dp(1.8f), paint);
            }
        }
    }

    /** A heading over a group of settings. */
    private void section(LinearLayout parent, String name) {
        TextView heading = text(name, 15, CalculatorLayout.CORAL);
        heading.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        heading.setAllCaps(true);
        heading.setLetterSpacing(.06f);
        heading.setPadding(0, dp(24), 0, dp(2));
        parent.addView(heading);
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

    /** Playing unlocks more for her to say and more tricks, in secret: this says only how many have been found. */
    private void describeFound() {
        if (found == null) return;
        PlaySession kept = PlaySession.get(this);
        Unlocks.check(kept, BuildConfig.DEBUG);
        found.setText("Things she has to say: " + Unlocks.found(kept) + " of " + Unlocks.lines() + " found. Tricks: "
                + Unlocks.tricksFound(kept) + " of " + Unlocks.TRICKS + ". Playing finds the rest.");
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
        String opens = prefs.tapGames() ? " to play with her" : " for the mini calculator";
        intro.setText("A mini Halgakos who floats over your other apps. " + (prefs.tapCalculator()
                ? (prefs.doubleTap() ? "Tap her twice" : "Tap her") + opens + ", hold her for her menu,"
                : "Tap her for her menu, hold her" + opens + ",")
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
