package com.coral.calculator;

import android.content.Context;
import android.content.SharedPreferences;

/** What the user chose for the Halgakos companion, and where she was last left. */
final class CompanionPrefs {
    static final int SMALL = 0;
    static final int MEDIUM = 1;
    static final int LARGE = 2;

    static final String ENABLED = "enabled";
    /** On a foldable, stay away while the device is folded shut. */
    static final String UNFOLDED_ONLY = "unfolded_only";
    static final String HIDE_IN_APP = "hide_in_app";
    static final String SNAP = "snap";
    static final String FADE = "fade";
    static final String SIZE = "size";
    /** Position as a share of the room she has to move in, so it survives a change of screen. */
    static final String X = "x";
    static final String Y = "y";

    private final SharedPreferences preferences;

    CompanionPrefs(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences("companion", Context.MODE_PRIVATE);
    }

    boolean enabled() { return preferences.getBoolean(ENABLED, false); }
    boolean unfoldedOnly() { return preferences.getBoolean(UNFOLDED_ONLY, false); }
    boolean hideInApp() { return preferences.getBoolean(HIDE_IN_APP, true); }
    boolean snap() { return preferences.getBoolean(SNAP, true); }
    boolean fade() { return preferences.getBoolean(FADE, true); }
    int size() { return Math.max(SMALL, Math.min(LARGE, preferences.getInt(SIZE, MEDIUM))); }
    float x() { return clamp(preferences.getFloat(X, 1f)); }
    float y() { return clamp(preferences.getFloat(Y, .58f)); }

    void set(String key, boolean value) { preferences.edit().putBoolean(key, value).apply(); }
    void setSize(int size) { preferences.edit().putInt(SIZE, size).apply(); }
    void setPosition(float x, float y) { preferences.edit().putFloat(X, clamp(x)).putFloat(Y, clamp(y)).apply(); }

    private static float clamp(float value) { return Float.isNaN(value) ? 0 : Math.max(0, Math.min(1, value)); }
}
