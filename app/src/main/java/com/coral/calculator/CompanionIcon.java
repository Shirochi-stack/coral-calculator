package com.coral.calculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.widget.Button;

/** Line icons for the companion's windows, drawn in a 24 unit box like the app's own. */
final class CompanionIcon extends View {
    static final String BACKSPACE = "backspace";
    static final String OPEN = "open";
    static final String CLOSE = "close";
    static final String POWER = "power";
    static final String CALCULATOR = "calculator";
    static final String SETTINGS = "settings";
    static final String BACK = "back";
    /** A game controller: playing with Halgakos. */
    static final String PLAY = "play";
    /** The games themselves, and the marks and hands played in them. */
    static final String GRID = "grid";
    static final String CARDS = "cards";
    static final String SUMS = "sums";
    static final String NOUGHT = "nought";
    static final String ROCK = "rock";
    static final String PAPER = "paper";
    static final String SCISSORS = "scissors";
    static final String TARGET = "target";
    static final String RAIN = "rain";
    static final String BUBBLES = "bubbles";
    static final String SPARK = "spark";
    static final String SPEECH = "speech";
    static final String PAWN = "pawn";
    static final String MINE = "mine";
    static final String NONE = "";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF bounds = new RectF();
    private String kind;
    /** Share of the view's shorter side the icon takes up. */
    private final float fill;

    CompanionIcon(Context context, String kind, int color, float fill) {
        super(context);
        this.kind = kind;
        this.fill = fill;
        paint.setColor(color);
        paint.setStrokeWidth(1.9f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
    }

    void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    /** Draws another icon in this one's place, as a cell of a game does when it is marked. */
    void set(String kind, int color) {
        this.kind = kind;
        setColor(color);
    }

    @Override public CharSequence getAccessibilityClassName() {
        return isClickable() ? Button.class.getName() : super.getAccessibilityClassName();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight()) * fill;
        canvas.save();
        canvas.translate((getWidth() - size) / 2, (getHeight() - size) / 2);
        canvas.scale(size / 24f, size / 24f);
        path.reset();
        switch (kind) {
            case BACKSPACE:
                path.moveTo(8, 5); path.lineTo(22, 5); path.lineTo(22, 19); path.lineTo(8, 19); path.lineTo(1, 12); path.close();
                canvas.drawPath(path, paint);
                canvas.drawLine(11, 8, 18, 16, paint); canvas.drawLine(18, 8, 11, 16, paint);
                break;
            case OPEN:
                // An arrow leaving the corner of a window.
                path.moveTo(10, 4); path.lineTo(4, 4); path.lineTo(4, 20); path.lineTo(20, 20); path.lineTo(20, 14);
                canvas.drawPath(path, paint);
                canvas.drawLine(11, 13, 20, 4, paint);
                canvas.drawLine(14, 4, 20, 4, paint); canvas.drawLine(20, 4, 20, 10, paint);
                break;
            case CLOSE:
                canvas.drawLine(5, 5, 19, 19, paint); canvas.drawLine(19, 5, 5, 19, paint);
                break;
            case POWER:
                // A ring open at the top, with a stroke standing in the gap.
                bounds.set(3.5f, 4.5f, 20.5f, 21.5f); canvas.drawArc(bounds, -55, 290, false, paint);
                canvas.drawLine(12, 2.5f, 12, 11.5f, paint);
                break;
            case CALCULATOR:
                bounds.set(4, 2, 20, 22); canvas.drawRoundRect(bounds, 2.5f, 2.5f, paint);
                bounds.set(7.5f, 5.5f, 16.5f, 9); canvas.drawRoundRect(bounds, 1, 1, paint);
                for (int row = 0; row < 2; row++) {
                    for (int column = 0; column < 3; column++) canvas.drawPoint(8 + column * 4, 13.5f + row * 4, paint);
                }
                break;
            case SETTINGS:
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 3 * i - Math.PI / 2;
                    float x = 12 + (float) Math.cos(angle) * 10.5f, y = 12 + (float) Math.sin(angle) * 10.5f;
                    if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
                }
                path.close(); canvas.drawPath(path, paint); canvas.drawCircle(12, 12, 4.3f, paint);
                break;
            case BACK:
                canvas.drawLine(19, 12, 5, 12, paint);
                canvas.drawLine(5, 12, 11, 6, paint); canvas.drawLine(5, 12, 11, 18, paint);
                break;
            case PLAY:
                // A controller: a cross of direction keys and two buttons.
                bounds.set(2, 6.5f, 22, 17.5f); canvas.drawRoundRect(bounds, 5.5f, 5.5f, paint);
                canvas.drawLine(6, 12, 10, 12, paint); canvas.drawLine(8, 10, 8, 14, paint);
                canvas.drawPoint(15, 13.5f, paint); canvas.drawPoint(18, 10.5f, paint);
                break;
            case GRID:
                canvas.drawLine(9, 4, 9, 20, paint); canvas.drawLine(15, 4, 15, 20, paint);
                canvas.drawLine(4, 9, 20, 9, paint); canvas.drawLine(4, 15, 20, 15, paint);
                break;
            case CARDS:
                // Two cards side by side, with the same sign on each.
                bounds.set(2.5f, 5, 10.5f, 19); canvas.drawRoundRect(bounds, 2, 2, paint);
                bounds.set(13.5f, 5, 21.5f, 19); canvas.drawRoundRect(bounds, 2, 2, paint);
                canvas.drawLine(5, 12, 8, 12, paint); canvas.drawLine(6.5f, 10.5f, 6.5f, 13.5f, paint);
                canvas.drawLine(16, 12, 19, 12, paint); canvas.drawLine(17.5f, 10.5f, 17.5f, 13.5f, paint);
                break;
            case SUMS:
                // The four signs, as on a calculator's keys.
                canvas.drawLine(4, 7.5f, 10, 7.5f, paint); canvas.drawLine(7, 4.5f, 7, 10.5f, paint);
                canvas.drawLine(14, 7.5f, 20, 7.5f, paint);
                canvas.drawLine(4.8f, 14.3f, 9.2f, 18.7f, paint); canvas.drawLine(9.2f, 14.3f, 4.8f, 18.7f, paint);
                canvas.drawLine(14, 15, 20, 15, paint); canvas.drawLine(14, 18, 20, 18, paint);
                break;
            case NOUGHT:
                canvas.drawCircle(12, 12, 7.5f, paint);
                break;
            case ROCK:
                // A stone, with one of its faces catching the light.
                path.moveTo(4, 14.5f); path.lineTo(6, 8.5f); path.lineTo(11, 5); path.lineTo(17, 6);
                path.lineTo(20.5f, 11); path.lineTo(19, 17); path.lineTo(12.5f, 19.5f); path.lineTo(6.5f, 18.5f);
                path.close(); canvas.drawPath(path, paint);
                canvas.drawLine(8.5f, 11, 11.5f, 9, paint);
                break;
            case PAPER:
                // A sheet with its corner turned down.
                path.moveTo(6, 3); path.lineTo(14.5f, 3); path.lineTo(19, 7.5f); path.lineTo(19, 21); path.lineTo(6, 21);
                path.close(); canvas.drawPath(path, paint);
                path.reset(); path.moveTo(14.5f, 3); path.lineTo(14.5f, 7.5f); path.lineTo(19, 7.5f);
                canvas.drawPath(path, paint);
                canvas.drawLine(9, 12.5f, 16, 12.5f, paint); canvas.drawLine(9, 16.5f, 16, 16.5f, paint);
                break;
            case SCISSORS:
                canvas.drawCircle(7, 18.3f, 2.7f, paint); canvas.drawCircle(17, 18.3f, 2.7f, paint);
                canvas.drawLine(8.6f, 16.1f, 18, 3.5f, paint); canvas.drawLine(15.4f, 16.1f, 6, 3.5f, paint);
                break;
            case TARGET:
                canvas.drawCircle(12, 12, 9, paint); canvas.drawCircle(12, 12, 4.5f, paint);
                canvas.drawPoint(12, 12, paint);
                break;
            case RAIN:
                // A plus and a minus on their way down.
                canvas.drawLine(4.5f, 12, 11.5f, 12, paint); canvas.drawLine(8, 8.5f, 8, 15.5f, paint);
                canvas.drawLine(13.5f, 18, 20, 18, paint);
                canvas.drawLine(8, 2.5f, 8, 4.5f, paint); canvas.drawLine(16.75f, 9.5f, 16.75f, 13.5f, paint);
                break;
            case BUBBLES:
                canvas.drawCircle(9, 14, 6.5f, paint); canvas.drawCircle(17.5f, 6.5f, 3.5f, paint);
                bounds.set(5.5f, 10.5f, 12.5f, 17.5f); canvas.drawArc(bounds, 190, 60, false, paint);
                break;
            case SPARK:
                // A four pointed star: something up her sleeve.
                path.moveTo(12, 2.5f); path.lineTo(14.2f, 9.8f); path.lineTo(21.5f, 12); path.lineTo(14.2f, 14.2f);
                path.lineTo(12, 21.5f); path.lineTo(9.8f, 14.2f); path.lineTo(2.5f, 12); path.lineTo(9.8f, 9.8f);
                path.close(); canvas.drawPath(path, paint);
                break;
            case SPEECH:
                bounds.set(3, 4, 21, 17); canvas.drawRoundRect(bounds, 4, 4, paint);
                path.moveTo(8, 17); path.lineTo(7, 21); path.lineTo(12, 17); canvas.drawPath(path, paint);
                break;
            case PAWN:
                // A chess pawn: a head, a body that widens, and a foot.
                canvas.drawCircle(12, 6.5f, 3.3f, paint);
                path.moveTo(9.5f, 11); path.lineTo(14.5f, 11); path.lineTo(16.5f, 18); path.lineTo(7.5f, 18); path.close();
                canvas.drawPath(path, paint);
                canvas.drawLine(5.5f, 21, 18.5f, 21, paint);
                break;
            case MINE:
                // A mine: a ball with spikes all round it.
                canvas.drawCircle(12, 12, 5.5f, paint);
                canvas.drawLine(12, 2.5f, 12, 5, paint); canvas.drawLine(12, 19, 12, 21.5f, paint);
                canvas.drawLine(2.5f, 12, 5, 12, paint); canvas.drawLine(19, 12, 21.5f, 12, paint);
                canvas.drawLine(5.3f, 5.3f, 7, 7, paint); canvas.drawLine(17, 17, 18.7f, 18.7f, paint);
                canvas.drawLine(18.7f, 5.3f, 17, 7, paint); canvas.drawLine(7, 17, 5.3f, 18.7f, paint);
                break;
            default: break;
        }
        canvas.restore();
    }
}
