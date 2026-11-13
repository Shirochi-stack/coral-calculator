package com.coral.calculator;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.OverScroller;
import android.widget.TextView;

import java.util.Arrays;
import java.util.function.IntConsumer;

/** Native, accessible controls placed to preserve the supplied reference's proportions. */
public final class CalculatorLayout extends ViewGroup {
    public static final int BACKGROUND = Color.rgb(250, 250, 250);
    public static final int CORAL = Color.rgb(255, 57, 63);
    private static final int INK = Color.rgb(41, 43, 42);
    private static final int MUTED = Color.rgb(162, 164, 163);
    /** Keys are never more than twice as wide as they are tall. */
    private static final float MAX_KEY_STRETCH = 2f;
    public interface Listener {
        void onKey(String key);
        void onTool(String tool);
        /** The caret was placed at this position in the displayed expression. */
        void onCaret(int offset);
        /** The line under the expression was tapped at this position in its text. */
        void onAnswerCaret(int offset);
    }
    private final DisplayLine expression;
    private final DisplayLine result;
    private final View[] tools = new View[5];
    private final View[] keys = new View[24];
    private final String[] labels = {"mc", "m+", "m−", "mr", "AC", "⌫", "+/−", "÷",
            "7", "8", "9", "×", "4", "5", "6", "−", "1", "2", "3", "+", "%", "0", ".", "="};
    private boolean dark;
    /** The unit every proportion is based on: the full width of a phone held upright. */
    private float contentWidth;
    /** Upright only: the width the column spans, wider than the unit on unfolded screens. */
    private float spanWidth;
    /** Upright only: the width each key gains over a circle to fill that span. */
    private float keyStretch;
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
        result.setStartFirst(true);
        expression.setOnLongClickListener(v -> { listener.onTool("copy"); return true; });
        expression.setCaretListener(listener::onCaret);
        result.setCaretListener(listener::onAnswerCaret);
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

    private DisplayLine display(Context context, int color, String font) {
        DisplayLine view = new DisplayLine(context, font);
        view.setTextColor(color);
        return view;
    }

    /** @param caret position of the caret in {@code equation} while it is being entered */
    public void update(String equation, String preview, boolean error, boolean memorySet,
                       boolean completed, boolean animate, int caret) {
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
        expression.setText(styled, completed ? -1 : caret);
        expression.showCaret(!completed);
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

        DisplayPose(DisplayLine view) {
            right = view.getRight() - view.getCaretRoom() + view.getTranslationX();
            centerY = (view.getTop() + view.getBottom()) / 2f + view.getTranslationY();
            textSize = view.getTextSize() * view.getScaleX();
            alpha = view.getAlpha();
            color = view.getCurrentTextColor();
        }
    }

    private static final class DisplayTransition {
        final DisplayPose equation, answer;
        final boolean repeated;

        DisplayTransition(DisplayLine equation, DisplayLine answer, boolean repeated) {
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

    private void animatePose(DisplayLine view, DisplayPose from, int color, float progress, boolean repeated) {
        float remaining = 1 - progress;
        float scale = from.textSize / view.getTextSize();
        float offset = repeated ? contentWidth * .012f : 0;
        view.setTranslationX((from.right - (view.getRight() - view.getCaretRoom())) * remaining);
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
            keys[i].setBackground(pill(background));
            if (keys[i] instanceof TextView) {
                int color = i == 23 ? Color.WHITE : operator || (i >= 4 && i <= 6) ? CORAL : i < 4 ? (enabled ? 0xFFACB0B2 : 0xFF878A88) : (enabled ? 0xFFEBECEC : 0xFF565A57);
                ((TextView) keys[i]).setTextColor(color);
            }
        }
        for (int i = 0; i < tools.length; i++) ((IconButton) tools[i]).setColor(i < 2 ? (enabled ? 0xFFE5E7E7 : INK) : (enabled ? 0xFF9CA2A6 : MUTED));
    }

    /** Fully rounded ends: a circle when square, a pill when a key is wider than it is tall. */
    private static RippleDrawable pill(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(100000f);
        shape.setColor(color);
        GradientDrawable mask = new GradientDrawable();
        mask.setCornerRadius(100000f);
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
        int keyWidth, keyHeight;
        if (landscape) {
            // Two panes either side of the centre, where a book-style foldable has its hinge.
            float widest = w * .215f;
            keyHeight = Math.round(Math.min(widest, availableHeight * .145f));
            keyWidth = Math.round(Math.min(widest, keyHeight * MAX_KEY_STRETCH));
            keyStretch = 0;
            spanWidth = w;
        } else {
            // Unfolded and tablet screens are wider than the phone proportions need. The keys
            // widen into pills and the display takes the whole span instead of leaving margins.
            keyHeight = Math.round(w * .169f);
            keyStretch = Math.min((availableWidth - w) / 4f, keyHeight * (MAX_KEY_STRETCH - 1));
            spanWidth = w + keyStretch * 4;
            keyWidth = Math.round(keyHeight + keyStretch);
        }
        for (int i = 0; i < keys.length; i++) {
            keys[i].measure(exact(keyWidth), exact(keyHeight));
            if (keys[i] instanceof TextView) {
                float size = i < 4 ? .084f : i == 4 ? .078f : i == 6 ? .074f : i % 4 == 3 ? .116f : .102f;
                ((TextView) keys[i]).setTextSize(TypedValue.COMPLEX_UNIT_PX, (landscape ? keyHeight / .169f : w) * size);
            }
        }
        int touchSize = Math.round(Math.max(48 * getResources().getDisplayMetrics().density, w * .092f));
        for (View tool : tools) tool.measure(exact(touchSize), exact(touchSize));
        // Text keeps its full size; lines longer than the display are swiped sideways instead.
        int lineWidth = Math.round(spanWidth - w * .12f);
        expression.setCaretRoom(completed ? 0 : Math.round(w * .03f));
        expression.setTextSizePx(Math.max(13, Math.round(w * (completed ? .095f : .16f))));
        float resultSize = Math.max(13, Math.round(w * (completed ? .16f : .11f)));
        // Error messages are sentences, not numbers: fit them rather than hiding their start.
        result.setTextSizePx(displayError ? result.fittedSize(resultSize, lineWidth) : resultSize);
        int oneLine = Math.round(w * (completed ? .125f : .20f));
        int oneResult = Math.round(w * (completed ? .20f : .135f));
        // An unfolded screen held sideways has room under the tools for a long calculation to
        // continue on further lines. A phone held sideways does not, and keeps its single line.
        boolean wrap = landscape && availableHeight >= 480 * getResources().getDisplayMetrics().density;
        expression.setWrap(wrap);
        expression.setLineHeight(oneLine);
        result.setWrap(wrap && !displayError);
        result.setLineHeight(oneResult);
        if (wrap) {
            // A long answer continues on a second or third line, which is enough for sixteen
            // digits; the calculation gets the rest of the room.
            result.measure(exact(lineWidth),
                    MeasureSpec.makeMeasureSpec(oneResult * (completed ? 3 : 2), MeasureSpec.AT_MOST));
            float room = availableHeight * .96f - toolRowY(availableHeight, touchSize) - touchSize * .6f
                    - lineGap() - result.getMeasuredHeight();
            expression.measure(exact(lineWidth),
                    MeasureSpec.makeMeasureSpec(Math.max(oneLine, Math.round(room)), MeasureSpec.AT_MOST));
        } else {
            result.measure(exact(lineWidth), exact(oneResult));
            expression.measure(exact(lineWidth), exact(oneLine));
        }
    }

    /** Sideways: centre of the row of tools above the display, below the top of the content. */
    private static float toolRowY(float height, float touch) { return Math.max(height * .12f, touch * .55f); }

    /** Sideways: space between the calculation and the line under it. */
    private float lineGap() { return contentWidth * (completed ? .01f : .005f); }

    private static int exact(int size) { return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY); }
    private void place(View view, float x, float y) {
        int left = Math.round(x), top = Math.round(y);
        view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
    }
    private void center(View view, float x, float y) { place(view, x - view.getMeasuredWidth() / 2f, y - view.getMeasuredHeight() / 2f); }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        float w = contentWidth;
        float availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float top = getPaddingTop();
        float h = getHeight() - top - getPaddingBottom();
        if (landscape) {
            float start = getPaddingLeft();
            float gridStart = start + availableWidth * .52f;
            for (int i = 0; i < keys.length; i++) center(keys[i], gridStart + w * (.12f + (i % 4) * .25f), top + h * (.08f + (i / 4) * .165f));
            // One row of tools above the display, which sits level with the middle of the keys.
            float touch = tools[0].getMeasuredWidth();
            float toolStep = Math.max(touch * 1.05f, w * .135f);
            float toolY = top + toolRowY(h, touch);
            for (int i = 2; i < tools.length; i++) center(tools[i], start + w * .11f + (i - 2) * toolStep, toolY);
            center(tools[0], start + w * .89f - toolStep, toolY);
            center(tools[1], start + w * .89f, toolY);
            float lineStep = expression.getMeasuredHeight() + lineGap();
            float displayY = Math.max(toolY + touch * .6f, top + (h - lineStep - result.getMeasuredHeight()) / 2f);
            place(expression, start + w * .06f, displayY);
            place(result, start + w * .06f, displayY + lineStep);
            finishDisplayLayout(changed);
            return;
        }
        float x = getPaddingLeft() + (availableWidth - spanWidth) / 2f;
        float lastCenter = top + h - w * .182f;
        for (int i = 0; i < keys.length; i++) {
            center(keys[i], x + w * .154f + keyStretch / 2f + (i % 4) * (w * .238f + keyStretch),
                    lastCenter - (5 - i / 4) * w * .202f);
        }
        float utilityY = lastCenter - w * 1.195f;
        center(tools[2], x + w * .117f, utilityY);
        center(tools[3], x + w * .252f, utilityY);
        center(tools[4], x + w * .388f, utilityY);
        float expressionY = utilityY - w * .42f;
        place(expression, x + w * .072f, expressionY);
        place(result, x + w * .072f, utilityY - w * (completed ? .285f : .215f));
        float headerY = top + Math.max(w * .077f, (expressionY - top) * .46f) + w * .024f;
        center(tools[0], x + spanWidth - w * .207f, headerY);
        center(tools[1], x + spanWidth - w * .075f, headerY);
        finishDisplayLayout(changed);
    }

    private void finishDisplayLayout(boolean changed) {
        expression.setPivotX(expression.getWidth() - expression.getCaretRoom());
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

    /**
     * One line of the display at a fixed text size, with a blinking caret marking where the
     * next key lands: the end of the text, or wherever it was last tapped.
     *
     * Content wider than the line normally stays on one line, fades out at the clipped edge
     * and is swiped sideways. In wrapping mode it continues on further lines instead, and is
     * swiped up and down once it has more lines than there is room for.
     */
    private static final class DisplayLine extends ViewGroup {
        private static final long BLINK_MS = 500;
        private static final int[] NO_BREAKS = {};
        private final TextView text;
        private final Paint caretPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fadePaint = new Paint();
        private final Matrix fadeMatrix = new Matrix();
        private final LinearGradient fadeShader =
                new LinearGradient(0, 0, 1, 0, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        private final OverScroller scroller;
        private final int touchSlop;
        private final int minimumFling;
        private final int maximumFling;
        private final Runnable blink = new Runnable() {
            @Override public void run() {
                caretOn = !caretOn;
                invalidate();
                postDelayed(this, BLINK_MS);
            }
        };
        private VelocityTracker velocity;
        private float downX;
        private float downY;
        private float last;
        private boolean dragging;
        /** How far the content is moved from showing its end: sideways, or down when wrapping. */
        private float scroll;
        private int caretRoom;
        private boolean caret;
        private boolean caretOn;
        /** The text as given, without the line breaks wrapping adds to what is shown. */
        private CharSequence source = "";
        /** Character the caret precedes, or -1 when it follows the last one. */
        private int caretIndex = -1;
        /** At a line break, whether the caret starts the next line rather than ending this one. */
        private boolean caretLeading;
        private boolean tapLeading;
        private boolean showStart;
        private boolean revealCaret;
        private boolean pinStart;
        /** Wrapped lines that do not all fit open on the first ones rather than the last. */
        private boolean startFirst;
        private IntConsumer caretListener;
        private boolean wrap;
        /** Positions in {@link #source} that start a new line when wrapping. */
        private int[] breaks = NO_BREAKS;
        private int wrapWidth = -1;
        private float wrapSize;
        /** Height of this view when it holds a single line. */
        private int lineHeight;
        /** Space shared above and below the lines when wrapping. */
        private int verticalPad;
        /** When wrapped lines scroll, the strip in which the next hidden line shows, fading out. */
        private int peek;

        DisplayLine(Context context, String font) {
            super(context);
            text = new TextView(context);
            text.setIncludeFontPadding(false);
            text.setSingleLine(true);
            text.setTypeface(Typeface.create(font, Typeface.NORMAL));
            text.setPadding(0, 0, 0, 0);
            text.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(text);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            caretPaint.setColor(CORAL);
            fadePaint.setShader(fadeShader);
            fadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            scroller = new OverScroller(context);
            ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
            minimumFling = configuration.getScaledMinimumFlingVelocity();
            maximumFling = configuration.getScaledMaximumFlingVelocity();
        }

        void setText(CharSequence value) { setText(value, -1); }

        /**
         * New content snaps back to its end, where the next key press lands. With the caret
         * placed inside the text, the view stays where it is and only moves to keep it visible.
         */
        void setText(CharSequence value, int caretAt) {
            // Swiped back to the start, the start stays put while digits are edited near it.
            pinStart = caretAt >= 0 && maxScroll() > 0 && scroll >= maxScroll() - 1;
            source = value;
            showText();
            scroller.abortAnimation();
            caretIndex = caretAt >= 0 && caretAt < value.length() ? caretAt : -1;
            caretLeading = tapLeading && caretIndex >= 0;
            showStart = caretIndex < 0 && wrap && startFirst;
            if (caretIndex < 0) setScroll(0);
            else revealCaret = true;
        }

        /** For a number, whose leading digits matter most when it is too long to show whole. */
        void setStartFirst(boolean enabled) { startFirst = enabled; }

        /** Wrapping continues long content on further lines, breaking before operators. */
        void setWrap(boolean enabled) {
            if (wrap == enabled) return;
            wrap = enabled;
            wrapWidth = -1;
            scroller.abortAnimation();
            scroll = 0;
            text.setTranslationX(0);
            text.setTranslationY(0);
            text.setSingleLine(!enabled);
            text.setGravity(enabled ? Gravity.END : Gravity.START);
            showText();
        }

        /** The height of this view while its content fits on one line. */
        void setLineHeight(int height) { lineHeight = height; }

        private void showText() {
            breaks = wrap && wrapWidth > 0 ? lineBreaks(source, text.getPaint(), wrapWidth) : NO_BREAKS;
            if (breaks.length == 0) {
                text.setText(source);
                return;
            }
            SpannableStringBuilder shown = new SpannableStringBuilder(source);
            for (int index = breaks.length - 1; index >= 0; index--) shown.insert(breaks[index], "\n");
            text.setText(shown);
        }

        /**
         * Where each further line starts. A line ends before an operator where it can, so a
         * number stays whole; a number too long for one line is split after a separator.
         */
        private static int[] lineBreaks(CharSequence value, Paint paint, float width) {
            int length = value.length();
            if (length == 0 || paint.measureText(value, 0, length) <= width) return NO_BREAKS;
            int[] found = new int[length];
            int count = 0;
            int start = 0;
            while (start < length) {
                int end = start + Math.max(1, paint.breakText(value, start, length, true, width, null));
                if (end >= length) break;
                int at = -1;
                for (int index = end; index > start && at < 0; index--) {
                    if (isOperator(value.charAt(index)) && !isOperator(value.charAt(index - 1))) at = index;
                }
                for (int index = end; index > start + 1 && at < 0; index--) {
                    if (value.charAt(index - 1) == ',') at = index;
                }
                start = at < 0 ? end : at;
                found[count++] = start;
            }
            return Arrays.copyOf(found, count);
        }

        private static boolean isOperator(char character) { return "+−×÷".indexOf(character) >= 0; }

        /** Position in the shown text, which has one extra character for every earlier break. */
        private int shownIndex(int position, boolean leading) {
            int shown = position;
            for (int start : breaks) if (start < position || (leading && start == position)) shown++;
            return shown;
        }

        private int sourceIndex(int shown) {
            CharSequence value = text.getText();
            int position = shown;
            for (int index = Math.min(shown, value.length()) - 1; index >= 0; index--) {
                if (value.charAt(index) == '\n') position--;
            }
            return position;
        }

        /** Taps and accessibility actions report the position they ask the caret to take. */
        void setCaretListener(IntConsumer listener) { caretListener = listener; }

        private int caretPosition() { return caretIndex < 0 ? source.length() : caretIndex; }

        private int caretShown() {
            return caretIndex < 0 ? text.length() : Math.min(text.length(), shownIndex(caretIndex, caretLeading));
        }

        /** Caret position in the unscrolled text, measured from this view's left edge. */
        private float caretOffset() {
            Layout layout = text.getLayout();
            if (caretIndex < 0 || layout == null) return text.getRight();
            return text.getLeft() + layout.getPrimaryHorizontal(caretShown());
        }

        private int caretLine() {
            Layout layout = text.getLayout();
            return layout == null ? 0 : layout.getLineForOffset(caretShown());
        }

        private void bringCaretIntoView() {
            Layout layout = text.getLayout();
            if (wrap) {
                if (layout == null) return;
                // The caret's line stays clear of the strips where neighbouring lines fade out.
                int line = caretLine();
                float lowest = verticalPad / 2f + (line > 0 ? peek : 0) - text.getTop() - layout.getLineTop(line);
                float highest = getHeight() - verticalPad / 2f - (line < layout.getLineCount() - 1 ? peek : 0)
                        - text.getTop() - layout.getLineBottom(line);
                setScroll(Math.max(lowest, Math.min(highest, scroll)));
                return;
            }
            // Wider than the faded edge, so the caret and the digit before it stay legible.
            float margin = text.getTextSize() * .7f;
            float offset = caretOffset();
            float highest = getWidth() - caretRoom - margin - offset;
            setScroll(Math.max(margin - offset, Math.min(highest, scroll)));
        }

        void setTextColor(int color) { text.setTextColor(color); }
        int getCurrentTextColor() { return text.getCurrentTextColor(); }
        float getTextSize() { return text.getTextSize(); }
        int getCaretRoom() { return caretRoom; }

        void setTextSizePx(float size) {
            if (text.getTextSize() != size) text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }

        /** Largest size up to {@code size} at which the whole text fits in {@code available} pixels. */
        float fittedSize(float size, float available) {
            Paint paint = text.getPaint();
            float current = paint.getTextSize();
            paint.setTextSize(size);
            float width = paint.measureText(source.toString());
            paint.setTextSize(current);
            return width > available ? (float) Math.floor(size * available / width * .98f) : size;
        }

        void setCaretRoom(int room) {
            if (caretRoom == room) return;
            caretRoom = room;
            requestLayout();
        }

        /** Also restarts the blink, so the caret stays solid while keys are being pressed. */
        void showCaret(boolean show) {
            caret = show;
            restartBlink();
        }

        private void restartBlink() {
            removeCallbacks(blink);
            caretOn = caret;
            invalidate();
            if (caret && isAttachedToWindow() && getWindowVisibility() == VISIBLE) postDelayed(blink, BLINK_MS);
        }

        private float maxScroll() {
            return Math.max(0, wrap ? text.getHeight() - (getHeight() - verticalPad)
                    : text.getWidth() - (getWidth() - caretRoom));
        }

        private void setScroll(float value) {
            scroll = Math.max(0, Math.min(maxScroll(), value));
            text.setTranslationX(wrap ? 0 : scroll);
            text.setTranslationY(wrap ? scroll : 0);
            invalidate();
        }

        @Override public CharSequence getAccessibilityClassName() { return TextView.class.getName(); }

        @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setText(source);
            if (!caret || caretListener == null) return;
            if (caretPosition() > 0) info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    R.id.action_caret_left, "Move cursor left"));
            if (caretPosition() < source.length()) info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    R.id.action_caret_right, "Move cursor right"));
        }

        @Override public boolean performAccessibilityAction(int action, Bundle arguments) {
            boolean left = action == R.id.action_caret_left;
            if (!caret || caretListener == null || !(left || action == R.id.action_caret_right)) {
                return super.performAccessibilityAction(action, arguments);
            }
            int target = caretPosition() + (left ? -1 : 1);
            // A thousands separator is not a stop of its own: step past it to the next digit.
            if (!left && target < source.length() && source.charAt(target - 1) == ',') target++;
            caretListener.accept(Math.max(0, Math.min(source.length(), target)));
            int position = caretPosition();
            announceForAccessibility(position == 0 ? "Cursor at start"
                    : "Cursor after " + source.charAt(position - 1));
            return true;
        }

        @Override protected void onMeasure(int widthSpec, int heightSpec) {
            int width = MeasureSpec.getSize(widthSpec);
            if (wrap && (wrapWidth != width - caretRoom || wrapSize != text.getTextSize())) {
                wrapWidth = width - caretRoom;
                wrapSize = text.getTextSize();
                showText();
            }
            int unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            text.measure(unspecified, unspecified);
            int height = MeasureSpec.getSize(heightSpec);
            if (wrap) {
                // Grow by whole lines up to the room on offer. Past that the lines scroll, with
                // part of the next hidden line left showing as a sign that there is more.
                int lines = Math.max(1, text.getLineCount());
                int each = Math.max(1, text.getMeasuredHeight() / lines);
                verticalPad = Math.max(0, lineHeight - each);
                int room = height - verticalPad;
                peek = lines * each > room ? Math.round(each * .45f) : 0;
                height = verticalPad + (peek == 0 ? text.getMeasuredHeight()
                        : Math.max(1, (room - peek) / each) * each + peek);
            }
            setMeasuredDimension(width, height);
        }

        @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
            // Right-aligned: the end of the text sits at the caret, earlier digits run off the left edge.
            // When wrapping, the last line sits at the bottom and earlier lines run off the top.
            int right = getWidth() - caretRoom;
            int top = wrap ? getHeight() - verticalPad / 2 - text.getMeasuredHeight()
                    : (getHeight() - text.getMeasuredHeight()) / 2;
            text.layout(right - text.getMeasuredWidth(), top, right, top + text.getMeasuredHeight());
            setScroll(showStart || (revealCaret && pinStart) ? maxScroll() : scroll);
            showStart = false;
            if (revealCaret) {
                revealCaret = false;
                bringCaretIntoView();
            }
        }

        @Override protected void dispatchDraw(Canvas canvas) {
            // Text that runs past an edge fades out there instead of being cut mid-digit.
            float hiddenBefore = maxScroll() - scroll, hiddenAfter = scroll;
            boolean fade = hiddenBefore > 1 || hiddenAfter > 1;
            int layer = fade ? canvas.saveLayer(0, 0, getWidth(), getHeight(), null) : 0;
            super.dispatchDraw(canvas);
            drawCaret(canvas);
            if (!fade) return;
            // About half a digit wide, so a digit that is fully in view stays readable.
            float edge = wrap ? peek : text.getTextSize() * .35f;
            if (hiddenBefore > 1) fadeEdge(canvas, Math.min(edge, hiddenBefore), false);
            if (hiddenAfter > 1) fadeEdge(canvas, Math.min(edge, hiddenAfter), true);
            canvas.restoreToCount(layer);
        }

        /** Erases towards the left or top edge, or towards the right or bottom one. */
        private void fadeEdge(Canvas canvas, float size, boolean far) {
            if (wrap) {
                fadeMatrix.setRotate(90);
                fadeMatrix.postScale(1, far ? -size : size);
                fadeMatrix.postTranslate(0, far ? getHeight() : 0);
            } else {
                fadeMatrix.setScale(far ? -size : size, 1);
                fadeMatrix.postTranslate(far ? getWidth() : 0, 0);
            }
            fadeShader.setLocalMatrix(fadeMatrix);
            if (wrap) canvas.drawRect(0, far ? getHeight() - size : 0, getWidth(), far ? getHeight() : size, fadePaint);
            else canvas.drawRect(far ? getWidth() - size : 0, 0, far ? getWidth() : size, getHeight(), fadePaint);
        }

        private void drawCaret(Canvas canvas) {
            Layout layout = text.getLayout();
            if (!caret || !caretOn || layout == null) return;
            float size = text.getTextSize();
            // Slimmer between two characters, where it has to fit in the gap between them.
            float thickness = Math.max(2 * getResources().getDisplayMetrics().density,
                    size * (caretIndex < 0 ? .05f : .035f));
            float left = (wrap ? 0 : scroll) + (caretIndex < 0
                    ? text.getRight() + Math.min(size * .06f, Math.max(0, caretRoom - thickness))
                    : caretOffset() - thickness / 2);
            float baseline = text.getTop() + layout.getLineBaseline(caretLine()) + (wrap ? scroll : 0);
            canvas.drawRoundRect(left, baseline + text.getPaint().ascent() * .92f, left + thickness,
                    baseline + text.getPaint().descent() * .6f, thickness / 2, thickness / 2, caretPaint);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (velocity == null) velocity = VelocityTracker.obtain();
            velocity.addMovement(event);
            // Content moves along one axis: sideways on a single line, up and down when wrapping.
            float along = wrap ? event.getY() : event.getX();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    scroller.abortAnimation();
                    downX = event.getX();
                    downY = event.getY();
                    last = along;
                    dragging = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!dragging && maxScroll() > 0 && Math.abs(along - (wrap ? downY : downX)) > touchSlop) {
                        dragging = true;
                        cancelLongPress();
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if (dragging) setScroll(scroll + along - last);
                    last = along;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    boolean wasDragging = dragging;
                    dragging = false;
                    if (wasDragging && event.getActionMasked() == MotionEvent.ACTION_UP) {
                        velocity.computeCurrentVelocity(1000, maximumFling);
                        float speed = wrap ? velocity.getYVelocity() : velocity.getXVelocity();
                        if (Math.abs(speed) > minimumFling) {
                            scroller.fling(Math.round(scroll), 0, Math.round(speed), 0, 0, Math.round(maxScroll()), 0, 0);
                            postInvalidateOnAnimation();
                        }
                    }
                    velocity.recycle();
                    velocity = null;
                    if (wasDragging) {
                        // Finish the gesture without letting the swipe count as a tap or long press.
                        setPressed(false);
                        return true;
                    }
                    // A short tap moves the caret to the nearest gap between characters. On the
                    // equation above an answer, which shows no caret, it reopens the equation there.
                    Layout layout = text.getLayout();
                    if (caretListener != null && layout != null
                            && event.getActionMasked() == MotionEvent.ACTION_UP
                            && Math.abs(event.getX() - downX) <= touchSlop
                            && Math.abs(event.getY() - downY) <= touchSlop
                            && event.getEventTime() - event.getDownTime() < ViewConfiguration.getLongPressTimeout()) {
                        float x = event.getX() - text.getLeft() - (wrap ? 0 : scroll);
                        float y = event.getY() - text.getTop() - (wrap ? scroll : 0);
                        int shown = layout.getOffsetForHorizontal(layout.getLineForVertical(Math.round(y)), x);
                        // Tapping the start of a wrapped line keeps the caret on that line.
                        tapLeading = shown > 0 && text.getText().charAt(shown - 1) == '\n';
                        caretListener.accept(sourceIndex(shown));
                        tapLeading = false;
                    }
                    break;
                default: break;
            }
            super.onTouchEvent(event);
            return true;
        }

        @Override public void computeScroll() {
            if (scroller.computeScrollOffset()) {
                setScroll(scroller.getCurrX());
                postInvalidateOnAnimation();
            }
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            restartBlink();
        }

        @Override protected void onDetachedFromWindow() {
            removeCallbacks(blink);
            super.onDetachedFromWindow();
        }

        @Override protected void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            restartBlink();
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
            setBackground(pill(Color.TRANSPARENT));
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        void setColor(int color) { this.color = color; invalidate(); }
        @Override public CharSequence getAccessibilityClassName() { return Button.class.getName(); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float size = Math.min(getWidth(), getHeight()) * ("backspace".equals(kind) ? .39f : .47f);
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
