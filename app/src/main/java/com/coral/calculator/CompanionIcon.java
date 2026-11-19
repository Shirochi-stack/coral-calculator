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
    static final String CALCULATOR = "calculator";
    static final String SETTINGS = "settings";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF bounds = new RectF();
    private final String kind;
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
            default: break;
        }
        canvas.restore();
    }
}
