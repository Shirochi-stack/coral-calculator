package com.coral.calculator;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

/** Native, accessible controls placed to preserve the supplied reference's proportions. */
public final class CalculatorLayout extends ViewGroup {
    public static final int BACKGROUND = Color.rgb(250, 250, 250);
    public static final int CORAL = Color.rgb(255, 57, 63);
    private static final int INK = Color.rgb(41, 43, 42);
    private static final int MUTED = Color.rgb(162, 164, 163);
    public interface Listener { void onKey(String key); void onTool(String tool); }
    private final TextView expression;
    private final TextView result;
    private final View[] tools = new View[5];
    private final View[] keys = new View[24];
    private final String[] labels = {"mc", "m+", "m−", "mr", "AC", "⌫", "+/−", "÷",
            "7", "8", "9", "×", "4", "5", "6", "−", "1", "2", "3", "+", "%", "0", ".", "="};
    private boolean dark;
    private float contentWidth;
    private boolean landscape;
    private boolean completed;
    private boolean displayError;
    private boolean hasDisplay;
    private String equationText = "";
    private String answerText = "";
    private DisplayTransition pendingTransition;
    private ValueAnimator displayAnimator;
    private static final long TRANSITION_DURATION_MS = 260;
    private static final ArgbEvaluator COLORS = new ArgbEvaluator();

    public CalculatorLayout(Context context, Listener listener) {
        super(context);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);
        setBackgroundColor(BACKGROUND);
        expression = display(context, INK, "sans-serif-medium");
        expression.setContentDescription("Expression: 0");
        result = display(context, MUTED, "sans-serif-medium");
        result.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        expression.setOnLongClickListener(v -> { listener.onTool("copy"); return true; });
        result.setOnLongClickListener(v -> { listener.onTool("copy"); return true; });
        addView(expression);
        addView(result);
        String[] names = {"fullscreen", "settings", "history", "units", "currency"};
        String[] descriptions = {"Toggle full screen", "Settings", "Calculation history", "Unit converter", "Currency converter"};
        for (int i = 0; i < tools.length; i++) {
            IconButton tool = new IconButton(context, names[i], i < 2 ? INK : MUTED);
            tool.setContentDescription(descriptions[i]);
            final String name = names[i];
            tool.setOnClickListener(v -> listener.onTool(name));
            tools[i] = tool;
            addView(tool);
        }
        String[] spoken = {"Memory clear", "Memory add", "Memory subtract", "Memory recall", "All clear", "Backspace", "Change sign", "Divide",
                "7", "8", "9", "Multiply", "4", "5", "6", "Subtract", "1", "2", "3", "Add", "Percent", "0", "Decimal point", "Equals"};
        for (int i = 0; i < labels.length; i++) {
            final String token = labels[i];
            View key;
            if (i == 5) {
                key = new IconButton(context, "backspace", CORAL);
            } else {
                Button button = new Button(context);
                button.setText(token);
                button.setAllCaps(false);
                button.setGravity(Gravity.CENTER);
                button.setIncludeFontPadding(false);
                button.setTypeface(Typeface.create(i < 8 || i % 4 == 3 ? "sans-serif" : "sans-serif-medium", Typeface.NORMAL));
                button.setMinWidth(0);
                button.setMinHeight(0);
                button.setMinimumWidth(0);
                button.setMinimumHeight(0);
                button.setPadding(0, 0, 0, 0);
                button.setStateListAnimator(null);
                key = button;
            }
            key.setContentDescription(spoken[i]);
            key.setOnClickListener(v -> listener.onKey(token.equals("+/−") ? "±" : token));
            keys[i] = key;
            addView(key);
        }
        setDark(false);
    }

    private TextView display(Context context, int color, String font) {
        TextView view = new TextView(context);
        view.setTextColor(color);
        view.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        view.setIncludeFontPadding(false);
        view.setSingleLine(true);
        view.setHorizontallyScrolling(false);
        view.setTypeface(Typeface.create(font, Typeface.NORMAL));
        view.setPadding(0, 0, 0, 0);
        return view;
    }

    public void update(String equation, String preview, boolean error, boolean memorySet,
                       boolean completed, boolean animate) {
        boolean modeChanged = this.completed != completed;
        boolean answerChanged = completed && (!equationText.equals(equation) || !answerText.equals(preview));
        DisplayTransition transition = animate && hasDisplay && isLaidOut()
                && ValueAnimator.areAnimatorsEnabled() && (modeChanged || answerChanged)
                ? new DisplayTransition(expression, result, !modeChanged) : null;
        stopDisplayAnimation();
        this.completed = completed;
        displayError = error;
        equationText = equation;
        answerText = preview;
        hasDisplay = true;
        SpannableString styled = new SpannableString(equation);
        for (int i = 0; i < equation.length(); i++) {
            if (!completed && "+−×÷%".indexOf(equation.charAt(i)) >= 0)
                styled.setSpan(new ForegroundColorSpan(CORAL), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        expression.setText(styled);
        expression.setContentDescription("Expression: " + equation);
        result.setText(preview);
        applyDisplayColors();
        result.setContentDescription(error ? preview : "Result: " + preview);
        keys[3].setContentDescription(memorySet ? "Memory recall, value stored" : "Memory recall, memory empty");
        ((TextView) keys[3]).setTextColor(memorySet ? CORAL : (dark ? 0xFFACB0B2 : 0xFF878A88));
        pendingTransition = transition;
        requestLayout();
    }

    private int inkColor() { return dark ? 0xFFF3F3F3 : INK; }
    private int mutedColor() { return dark ? 0xFF9CA2A6 : MUTED; }

    private void applyDisplayColors() {
        expression.setTextColor(completed ? mutedColor() : inkColor());
        result.setTextColor(displayError ? CORAL : completed ? inkColor() : mutedColor());
    }

    /** Captures rendered positions so a quick second key can reverse an in-flight transition. */
    private static final class DisplayPose {
        final float right, centerY, textSize, alpha;
        final int color;

        DisplayPose(TextView view) {
            right = view.getRight() + view.getTranslationX();
            centerY = (view.getTop() + view.getBottom()) / 2f + view.getTranslationY();
            textSize = view.getTextSize() * view.getScaleX();
            alpha = view.getAlpha();
            color = view.getCurrentTextColor();
        }
    }

    private static final class DisplayTransition {
        final DisplayPose equation, answer;
        final boolean repeated;

        DisplayTransition(TextView equation, TextView answer, boolean repeated) {
            this.equation = new DisplayPose(equation);
            this.answer = new DisplayPose(answer);
            this.repeated = repeated;
        }
    }

    private void stopDisplayAnimation() {
        pendingTransition = null;
        if (displayAnimator != null) {
            displayAnimator.cancel();
            displayAnimator = null;
        }
        resetTransform(expression);
        resetTransform(result);
    }

    private static void resetTransform(View view) {
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setScaleX(1);
        view.setScaleY(1);
        view.setAlpha(1);
    }

    private void animateDisplay(DisplayTransition transition) {
        if (!ValueAnimator.areAnimatorsEnabled()) return;
        int equationColor = completed ? mutedColor() : inkColor();
        int answerColor = displayError ? CORAL : completed ? inkColor() : mutedColor();
        displayAnimator = ValueAnimator.ofFloat(0, 1);
        displayAnimator.setDuration(TRANSITION_DURATION_MS);
        displayAnimator.setInterpolator(new DecelerateInterpolator());
        displayAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            animatePose(expression, transition.equation, equationColor, progress, transition.repeated);
            animatePose(result, transition.answer, answerColor, progress, transition.repeated);
        });
        // Apply the starting pose before the first frame to avoid flashing the final layout.
        animatePose(expression, transition.equation, equationColor, 0, transition.repeated);
        animatePose(result, transition.answer, answerColor, 0, transition.repeated);
        displayAnimator.start();
    }

    private void animatePose(TextView view, DisplayPose from, int color, float progress, boolean repeated) {
        float remaining = 1 - progress;
        float scale = from.textSize / view.getTextSize();
        float offset = repeated ? contentWidth * .012f : 0;
        view.setTranslationX((from.right - view.getRight()) * remaining);
        view.setTranslationY((from.centerY - (view.getTop() + view.getBottom()) / 2f + offset) * remaining);
        view.setScaleX(1 + (scale - 1) * remaining);
        view.setScaleY(1 + (scale - 1) * remaining);
        view.setAlpha(1 + ((repeated ? .45f : from.alpha) - 1) * remaining);
        view.setTextColor((int) COLORS.evaluate(progress, from.color, color));
    }

    @Override protected void onDetachedFromWindow() {
        stopDisplayAnimation();
        super.onDetachedFromWindow();
    }

    public void setDark(boolean enabled) {
        stopDisplayAnimation();
        dark = enabled;
        setBackgroundColor(enabled ? 0xFF17191B : BACKGROUND);
        applyDisplayColors();
        for (int i = 0; i < keys.length; i++) {
            boolean operator = i >= 7 && i % 4 == 3;
            int background = i == 23 ? CORAL : operator ? (enabled ? 0xFF44292E : 0xFFFCE9EB) : (enabled ? 0xFF24272A : 0xFFF3F3F3);
            keys[i].setBackground(circle(background));
            if (keys[i] instanceof TextView) {
                int color = i == 23 ? Color.WHITE : operator || (i >= 4 && i <= 6) ? CORAL : i < 4 ? (enabled ? 0xFFACB0B2 : 0xFF878A88) : (enabled ? 0xFFEBECEC : 0xFF565A57);
                ((TextView) keys[i]).setTextColor(color);
            }
        }
        for (int i = 0; i < tools.length; i++) ((IconButton) tools[i]).setColor(i < 2 ? (enabled ? 0xFFE5E7E7 : INK) : (enabled ? 0xFF9CA2A6 : MUTED));
    }

    private static RippleDrawable circle(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        mask.setColor(Color.WHITE);
        return new RippleDrawable(ColorStateList.valueOf(0x20FF393F), shape, mask);
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec), height = MeasureSpec.getSize(heightSpec);
        setMeasuredDimension(width, height);
        float availableHeight = height - getPaddingTop() - getPaddingBottom();
        float availableWidth = width - getPaddingLeft() - getPaddingRight();
        landscape = availableWidth > availableHeight;
        contentWidth = landscape ? availableWidth * .47f : Math.min(availableWidth, availableHeight / 1.98f);
        float w = contentWidth;
        int diameter = Math.round(landscape ? Math.min(w * .185f, availableHeight * .145f) : w * .169f);
        for (int i = 0; i < keys.length; i++) {
            keys[i].measure(exact(diameter), exact(diameter));
            if (keys[i] instanceof TextView) {
                float size = i < 4 ? .073f : i == 4 ? .063f : i == 6 ? .065f : i % 4 == 3 ? .098f : .082f;
                ((TextView) keys[i]).setTextSize(TypedValue.COMPLEX_UNIT_PX, (landscape ? diameter / .169f : w) * size);
            }
        }
        int touchSize = Math.round(Math.max(48 * getResources().getDisplayMetrics().density, w * .092f));
        for (View tool : tools) tool.measure(exact(touchSize), exact(touchSize));
        expression.setAutoSizeTextTypeUniformWithConfiguration(12, Math.max(13, Math.round(w * (completed ? .072f : .132f))), 1, TypedValue.COMPLEX_UNIT_PX);
        result.setAutoSizeTextTypeUniformWithConfiguration(12, Math.max(13, Math.round(w * (completed ? .132f : .08f))), 1, TypedValue.COMPLEX_UNIT_PX);
        expression.measure(exact(Math.round(w * .88f)), exact(Math.round(w * (completed ? .10f : .166f))));
        result.measure(exact(Math.round(w * .88f)), exact(Math.round(w * (completed ? .166f : .105f))));
    }

    private static int exact(int size) { return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY); }
    private void place(View view, float x, float y) {
        int left = Math.round(x), top = Math.round(y);
        view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
    }
    private void center(View view, float x, float y) { place(view, x - view.getMeasuredWidth() / 2f, y - view.getMeasuredHeight() / 2f); }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        float w = contentWidth;
        float x = getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight() - w) / 2f;
        float top = getPaddingTop();
        float h = getHeight() - top - getPaddingBottom();
        if (landscape) {
            float start = getPaddingLeft();
            float availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            float gridStart = start + availableWidth * .52f;
            for (int i = 0; i < keys.length; i++) center(keys[i], gridStart + w * (.12f + (i % 4) * .25f), top + h * (.08f + (i / 4) * .165f));
            center(tools[0], start + w * .69f, top + h * .12f);
            center(tools[1], start + w * .89f, top + h * .12f);
            place(expression, start + w * .06f, top + h * (completed ? .39f : .36f));
            place(result, start + w * .06f, top + h * (completed ? .54f : .60f));
            center(tools[2], start + w * .14f, top + h * .86f);
            center(tools[3], start + w * .36f, top + h * .86f);
            center(tools[4], start + w * .58f, top + h * .86f);
            finishDisplayLayout(changed);
            return;
        }
        float lastCenter = top + h - w * .182f;
        for (int i = 0; i < keys.length; i++) center(keys[i], x + w * (.154f + (i % 4) * .238f), lastCenter - (5 - i / 4) * w * .202f);
        float utilityY = lastCenter - w * 1.195f;
        center(tools[2], x + w * .117f, utilityY);
        center(tools[3], x + w * .252f, utilityY);
        center(tools[4], x + w * .388f, utilityY);
        float expressionY = utilityY - w * .385f;
        place(expression, x + w * .072f, expressionY);
        place(result, x + w * .072f, utilityY - w * (completed ? .265f : .202f));
        float headerY = top + Math.max(w * .077f, (expressionY - top) * .46f) + w * .024f;
        center(tools[0], x + w * .793f, headerY);
        center(tools[1], x + w * .925f, headerY);
        finishDisplayLayout(changed);
    }

    private void finishDisplayLayout(boolean changed) {
        expression.setPivotX(expression.getWidth());
        expression.setPivotY(expression.getHeight() / 2f);
        result.setPivotX(result.getWidth());
        result.setPivotY(result.getHeight() / 2f);
        if (pendingTransition != null) {
            DisplayTransition transition = pendingTransition;
            pendingTransition = null;
            animateDisplay(transition);
        } else if (changed) {
            stopDisplayAnimation();
            applyDisplayColors();
        }
    }

    /** Icons remain actual focusable Views, so TalkBack and keyboard navigation work. */
    private static final class IconButton extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF bounds = new RectF();
        private final String kind;
        private int color;
        IconButton(Context context, String kind, int color) {
            super(context);
            this.kind = kind;
            this.color = color;
            setFocusable(true);
            setClickable(true);
            setBackground(circle(Color.TRANSPARENT));
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        void setColor(int color) { this.color = color; invalidate(); }
        @Override public CharSequence getAccessibilityClassName() { return Button.class.getName(); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float size = "backspace".equals(kind) ? getWidth() * .39f : Math.min(getWidth(), getHeight()) * .47f;
            canvas.save();
            canvas.translate((getWidth() - size) / 2, (getHeight() - size) / 2);
            canvas.scale(size / 24f, size / 24f);
            paint.setColor(color);
            paint.setStrokeWidth(1.9f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            path.reset();
            switch (kind) {
                case "backspace":
                    path.moveTo(8, 5); path.lineTo(22, 5); path.lineTo(22, 19); path.lineTo(8, 19); path.lineTo(1, 12); path.close();
                    canvas.drawPath(path, paint);
                    canvas.drawLine(11, 8, 18, 16, paint); canvas.drawLine(18, 8, 11, 16, paint); break;
                case "fullscreen":
                    canvas.drawLine(3, 13, 11, 13, paint); canvas.drawLine(11, 13, 11, 21, paint);
                    canvas.drawLine(14, 2, 14, 10, paint); canvas.drawLine(14, 10, 22, 10, paint); break;
                case "settings":
                    for (int i = 0; i < 6; i++) {
                        double angle = Math.PI / 3 * i - Math.PI / 2;
                        float px = 12 + (float) Math.cos(angle) * 10.5f, py = 12 + (float) Math.sin(angle) * 10.5f;
                        if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
                    }
                    path.close(); canvas.drawPath(path, paint); canvas.drawCircle(12, 12, 4.3f, paint); break;
                case "history":
                    bounds.set(3, 2, 23, 22); canvas.drawArc(bounds, 175, 325, false, paint);
                    canvas.drawLine(12, 7, 12, 13, paint); canvas.drawLine(12, 13, 17, 13, paint);
                    path.moveTo(0, 12); path.lineTo(4, 16); path.lineTo(7, 10); paint.setStyle(Paint.Style.FILL); canvas.drawPath(path, paint); break;
                case "units":
                    bounds.set(3, 2, 21, 22); canvas.drawRoundRect(bounds, 1, 1, paint);
                    paint.setStyle(Paint.Style.FILL); paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD)); paint.setTextSize(8); paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("FX", 12, 11, paint); canvas.drawText("ET", 12, 19, paint); break;
                case "currency":
                    bounds.set(2, 2, 22, 22); canvas.drawArc(bounds, 202, 116, false, paint); canvas.drawArc(bounds, 22, 116, false, paint);
                    path.moveTo(0, 12); path.lineTo(3, 17); path.lineTo(7, 12); path.close();
                    path.moveTo(17, 12); path.lineTo(21, 7); path.lineTo(24, 12); path.close(); paint.setStyle(Paint.Style.FILL); canvas.drawPath(path, paint);
                    paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD)); paint.setTextSize(17); paint.setTextAlign(Paint.Align.CENTER); canvas.drawText("$", 12, 18, paint); break;
                default: break;
            }
            canvas.restore();
        }
    }
}
