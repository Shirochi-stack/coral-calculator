package com.coral.calculator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The compact calculator the companion opens over other apps. It works on the same
 * calculation as the app, so either one carries on where the other left off.
 */
final class MiniCalculatorView extends LinearLayout {
    interface Host {
        void onOpenApp();
        void onClose();
    }

    /** Size at full scale, in dp. Every measurement inside is a share of these. */
    static final int WIDTH_DP = 264;
    static final int HEIGHT_DP = 360;
    /** Five rows of keys take the rest of the height: 42 each at full scale. */
    private static final int SIDE = 12, TOP = 8, HEADER = 32, DISPLAY = 68, GAP = 6;
    private static final int MAJOR_LINE = 41, MINOR_LINE = DISPLAY - MAJOR_LINE;

    private static final String[] KEYS = {"AC", "⌫", "%", "÷", "7", "8", "9", "×",
            "4", "5", "6", "−", "1", "2", "3", "+", "+/−", "0", ".", "="};
    private static final String[] SPOKEN = {"All clear", "Backspace", "Percent", "Divide", "7", "8", "9", "Multiply",
            "4", "5", "6", "Subtract", "1", "2", "3", "Add", "Change sign", "0", "Decimal point", "Equals"};

    private final CalculatorSession session;
    private final CalculatorSession.Listener owner;
    private final float unit;
    private final boolean dark;
    private final boolean haptics;
    private final TextView equation;
    private final TextView answer;

    /**
     * @param unit pixels per dp of the design, smaller than the screen's own where room is short
     * @param owner the listener that is not told about keys pressed here
     */
    MiniCalculatorView(Context context, float unit, boolean dark, boolean haptics,
                       CalculatorSession.Listener owner, Host host) {
        super(context);
        this.unit = unit;
        this.dark = dark;
        this.haptics = haptics;
        this.owner = owner;
        session = CalculatorSession.get(context);
        setOrientation(VERTICAL);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);
        setPadding(px(SIDE), px(TOP), px(SIDE), px(SIDE));
        GradientDrawable card = new GradientDrawable();
        card.setCornerRadius(px(22));
        card.setColor(dark ? 0xFF1D2023 : Color.WHITE);
        card.setStroke(Math.max(1, px(.75f)), dark ? 0xFF34383C : 0xFFE4E4E4);
        setBackground(card);
        setClipToOutline(true);
        // Touches on the card itself must not fall through to the app underneath.
        setClickable(true);

        LinearLayout header = new LinearLayout(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(context);
        title.setText("Halgakos");
        title.setTextColor(dark ? 0xFF9CA2A6 : 0xFF8B8E8C);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(13));
        title.setPadding(px(4), 0, 0, 0);
        header.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        header.addView(headerButton(CompanionIcon.OPEN, "Open in Halgakos Calculator", host::onOpenApp), square(HEADER));
        header.addView(headerButton(CompanionIcon.CLOSE, "Close mini calculator", host::onClose), square(HEADER));
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, px(HEADER)));

        equation = line(context);
        answer = line(context);
        answer.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        LinearLayout display = new LinearLayout(context);
        display.setOrientation(VERTICAL);
        display.setPadding(px(4), 0, px(4), 0);
        display.addView(equation);
        display.addView(answer);
        display.setContentDescription("Display. Long press to copy the result");
        display.setOnLongClickListener(v -> { copy(); return true; });
        addView(display, new LayoutParams(LayoutParams.MATCH_PARENT, px(DISPLAY)));

        for (int row = 0; row < KEYS.length / 4; row++) {
            LinearLayout keys = new LinearLayout(context);
            for (int column = 0; column < 4; column++) {
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                if (column > 0) params.leftMargin = px(GAP);
                keys.addView(key(context, row * 4 + column), params);
            }
            // The rows share what is left of the card between them. Sized one by one, their
            // rounded heights can add up to more than the card and cut the last row short.
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
            params.topMargin = px(GAP);
            addView(keys, params);
        }
        refresh();
    }

    private int px(float dp) { return Math.round(dp * unit); }

    private LayoutParams square(int dp) { return new LayoutParams(px(dp), px(dp)); }

    private View headerButton(String kind, String description, Runnable action) {
        CompanionIcon button = new CompanionIcon(getContext(), kind, dark ? 0xFFC9CDCF : 0xFF5F6360, .44f);
        button.setContentDescription(description);
        button.setFocusable(true);
        button.setClickable(true);
        button.setBackground(CalculatorLayout.pill(Color.TRANSPARENT));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private TextView line(Context context) {
        TextView view = new TextView(context);
        view.setIncludeFontPadding(false);
        view.setMaxLines(1);
        view.setEllipsize(TextUtils.TruncateAt.START);
        view.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return view;
    }

    private View key(Context context, int index) {
        String token = KEYS[index];
        boolean operator = index % 4 == 3;
        boolean equals = "=".equals(token);
        boolean action = index < 3;
        int background = equals ? CalculatorLayout.CORAL : operator ? (dark ? 0xFF44292E : 0xFFFCE9EB)
                : (dark ? 0xFF2A2E31 : 0xFFF3F3F3);
        int color = equals ? Color.WHITE : operator || action ? CalculatorLayout.CORAL : (dark ? 0xFFEBECEC : 0xFF565A57);
        View key;
        if ("⌫".equals(token)) {
            key = new CompanionIcon(context, CompanionIcon.BACKSPACE, color, .43f);
            key.setFocusable(true);
            key.setClickable(true);
        } else {
            Button button = new Button(context);
            button.setText(token);
            button.setAllCaps(false);
            button.setGravity(Gravity.CENTER);
            button.setIncludeFontPadding(false);
            button.setTypeface(Typeface.create(operator || action ? "sans-serif" : "sans-serif-medium", Typeface.NORMAL));
            button.setTextColor(color);
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(operator ? 23 : "AC".equals(token) || "+/−".equals(token) ? 16 : 20));
            button.setMinWidth(0);
            button.setMinHeight(0);
            button.setMinimumWidth(0);
            button.setMinimumHeight(0);
            button.setPadding(0, 0, 0, 0);
            button.setStateListAnimator(null);
            key = button;
        }
        key.setBackground(CalculatorLayout.pill(background));
        key.setContentDescription(SPOKEN[index]);
        key.setOnClickListener(v -> {
            if (haptics) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            session.press("+/−".equals(token) ? "±" : token, owner);
            refresh();
        });
        return key;
    }

    /** Shows the shared calculation as the app does: the larger line is the one being read. */
    void refresh() {
        CalculatorEngine engine = session.engine();
        boolean completed = engine.isEvaluated();
        String shown = NumberFormatter.format(completed ? engine.getCompletedEquation() : engine.getExpression());
        String result = engine.isError() ? engine.getErrorMessage()
                : NumberFormatter.format(completed ? engine.getExpression() : engine.getPreview());
        SpannableString styled = new SpannableString(shown);
        for (int i = 0; i < shown.length() && !completed; i++) {
            if ("+−×÷%".indexOf(shown.charAt(i)) >= 0)
                styled.setSpan(new ForegroundColorSpan(CalculatorLayout.CORAL), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        int ink = dark ? 0xFFF3F3F3 : 0xFF292B2A;
        int muted = dark ? 0xFF9CA2A6 : 0xFFA2A4A3;
        size(equation, !completed);
        size(answer, completed);
        equation.setTextColor(completed ? muted : ink);
        answer.setTextColor(engine.isError() ? CalculatorLayout.CORAL : completed ? ink : muted);
        equation.setText(styled);
        answer.setText(result);
        equation.setContentDescription("Expression: " + shown);
        answer.setContentDescription(engine.isError() ? result : "Result: " + result);
    }

    /** The line being read gets most of the display; either shrinks its text to stay whole. */
    private void size(TextView view, boolean major) {
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, px(major ? MAJOR_LINE : MINOR_LINE)));
        view.setAutoSizeTextTypeUniformWithConfiguration(px(11), px(major ? 30 : 17), Math.max(1, px(1)),
                TypedValue.COMPLEX_UNIT_PX);
    }

    private void copy() {
        String value = session.engine().getValue();
        Context context = getContext().getApplicationContext();
        if (value.isEmpty()) {
            Toast.makeText(context, "Complete a calculation to copy its result", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Calculation result", value));
        Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show();
    }
}
