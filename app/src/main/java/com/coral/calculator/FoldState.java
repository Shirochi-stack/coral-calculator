package com.coral.calculator;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

/**
 * Whether a foldable is open, read from its hinge. A device without a hinge sensor does not
 * count as a foldable, and is always open.
 */
final class FoldState implements SensorEventListener {
    interface Listener {
        void onFoldChanged();
    }

    /** A hinge opened past this is open; it has to close below {@link #SHUT_DEGREES} again. */
    private static final float OPEN_DEGREES = 30;
    private static final float SHUT_DEGREES = 20;
    /** Unfolded book-style foldables and tablets are at least this wide on their short side. */
    private static final int LARGE_SCREEN_DP = 600;
    /** How long to wait for the hinge to report before taking the device to be open. */
    private static final long FIRST_READING_MS = 2000;

    private final SensorManager sensors;
    private final Sensor hinge;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable giveUp = this::assumeOpen;
    private boolean listening;
    private boolean known;
    private boolean open = true;

    FoldState(Context context, Listener listener) {
        this.listener = listener;
        sensors = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        hinge = Build.VERSION.SDK_INT >= 30 && sensors != null
                ? sensors.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE) : null;
    }

    static boolean isFoldable(Context context) {
        if (Build.VERSION.SDK_INT < 30) return false;
        SensorManager sensors = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        return sensors != null && sensors.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE) != null;
    }

    boolean isFoldable() { return hinge != null; }

    /**
     * @param smallestWidthDp the short side of the screen in use. A large screen is an open
     *                        one whatever the hinge says, and stands in until it has reported.
     */
    boolean isUnfolded(int smallestWidthDp) {
        if (hinge == null || smallestWidthDp >= LARGE_SCREEN_DP) return true;
        return known && open;
    }

    void start() {
        if (hinge == null || listening) return;
        listening = true;
        known = false;
        sensors.registerListener(this, hinge, SensorManager.SENSOR_DELAY_NORMAL, handler);
        handler.postDelayed(giveUp, FIRST_READING_MS);
    }

    void stop() {
        if (!listening) return;
        listening = false;
        handler.removeCallbacks(giveUp);
        sensors.unregisterListener(this);
    }

    private void assumeOpen() {
        if (known) return;
        known = true;
        open = true;
        listener.onFoldChanged();
    }

    @Override public void onSensorChanged(SensorEvent event) {
        float degrees = event.values[0];
        boolean now = known ? (open ? degrees >= SHUT_DEGREES : degrees > OPEN_DEGREES)
                : degrees >= (OPEN_DEGREES + SHUT_DEGREES) / 2;
        if (known && now == open) return;
        handler.removeCallbacks(giveUp);
        known = true;
        open = now;
        listener.onFoldChanged();
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
