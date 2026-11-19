package com.coral.calculator;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * The one calculation the app and the companion's mini calculator both work on, with its
 * memory and history. Used on the main thread only.
 */
final class CalculatorSession {
    /** Told when the calculation was changed from somewhere else. */
    interface Listener {
        void onCalculationChanged();
    }

    static final String PREFERENCES = "calculator";
    private static final int HISTORY_LIMIT = 100;
    private static CalculatorSession instance;

    private final SharedPreferences preferences;
    private final CalculatorEngine engine = new CalculatorEngine();
    private final List<String[]> history = new ArrayList<>();
    private final List<Listener> listeners = new ArrayList<>();

    static CalculatorSession get(Context context) {
        if (instance == null) instance = new CalculatorSession(context.getApplicationContext());
        return instance;
    }

    private CalculatorSession(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        if (preferences.contains("state")) engine.restoreState(preferences.getString("state", ""));
        else engine.restore(preferences.getString("expression", "0"), preferences.getString("memory", "0"));
        try {
            JSONArray saved = new JSONArray(preferences.getString("history", "[]"));
            for (int i = 0; i < Math.min(saved.length(), HISTORY_LIMIT); i++) {
                JSONArray entry = saved.getJSONArray(i);
                history.add(new String[]{entry.getString(0), entry.getString(1)});
            }
        } catch (JSONException ignored) { history.clear(); }
    }

    CalculatorEngine engine() { return engine; }

    /** Completed calculations, newest first: the equation and its answer. */
    List<String[]> history() { return history; }

    void addListener(Listener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    void removeListener(Listener listener) { listeners.remove(listener); }

    /** Applies a calculator key, records a completed calculation and saves. */
    void press(String key, Listener source) {
        String before = engine.getEquationForEquals();
        boolean complete = !engine.getValue().isEmpty();
        switch (key) {
            case "mc": engine.memoryClear(); break;
            case "m+": engine.memoryAdd(); break;
            case "m−": engine.memorySubtract(); break;
            case "mr": engine.memoryRecall(); break;
            default: engine.input(key); break;
        }
        if ("=".equals(key) && complete && !engine.isError() && !engine.getValue().isEmpty()) {
            history.add(0, new String[]{before, engine.getValue()});
            if (history.size() > HISTORY_LIMIT) history.remove(history.size() - 1);
        }
        changed(source);
    }

    /** Saves, and tells everyone but {@code source} that the calculation is different now. */
    void changed(Listener source) {
        save();
        for (Listener listener : new ArrayList<>(listeners)) {
            if (listener != source) listener.onCalculationChanged();
        }
    }

    void save() {
        JSONArray saved = new JSONArray();
        for (String[] entry : history) {
            JSONArray item = new JSONArray(); item.put(entry[0]); item.put(entry[1]); saved.put(item);
        }
        preferences.edit().putString("expression", engine.getExpression()).putString("memory", engine.getMemory())
                .putString("state", engine.saveState()).putString("history", saved.toString()).apply();
    }
}
