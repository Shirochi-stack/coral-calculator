package com.coral.calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity implements CalculatorLayout.Listener {
    private final CalculatorEngine engine = new CalculatorEngine();
    private final List<String[]> history = new ArrayList<>();
    private CalculatorLayout layout;
    private SharedPreferences preferences;
    private boolean haptics;
    private boolean dark;
    private boolean fullScreen;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = getSharedPreferences("calculator", MODE_PRIVATE);
        haptics = preferences.getBoolean("haptics", true);
        dark = preferences.getBoolean("dark", false);
        if (preferences.contains("state")) engine.restoreState(preferences.getString("state", ""));
        else engine.restore(preferences.getString("expression", "0"), preferences.getString("memory", "0"));
        try {
            JSONArray saved = new JSONArray(preferences.getString("history", "[]"));
            for (int i = 0; i < Math.min(saved.length(), 100); i++) {
                JSONArray entry = saved.getJSONArray(i);
                history.add(new String[]{entry.getString(0), entry.getString(1)});
            }
        } catch (JSONException ignored) { history.clear(); }
        layout = new CalculatorLayout(this, this);
        setContentView(layout);
        configureInsets();
        applyTheme();
        refresh();
    }

    @SuppressWarnings("deprecation")
    private void configureInsets() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Build.VERSION.SDK_INT >= 27 ? Color.TRANSPARENT : 0xFF202020);
        if (Build.VERSION.SDK_INT >= 29) {
            getWindow().setStatusBarContrastEnforced(false);
            getWindow().setNavigationBarContrastEnforced(false);
        }
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        layout.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            } else {
                view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        layout.requestApplyInsets();
    }

    @SuppressWarnings("deprecation")
    private void applyTheme() {
        layout.setDark(dark);
        getWindow().getDecorView().setBackgroundColor(dark ? 0xFF17191B : CalculatorLayout.BACKGROUND);
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) controller.setSystemBarsAppearance(dark ? 0 : WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        } else {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            if (!dark) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= 27) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            if (fullScreen) flags |= View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    @Override public void onKey(String key) {
        if (haptics) layout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
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
            if (history.size() > 100) history.remove(history.size() - 1);
        }
        refresh(true);
        save();
    }

    private void refresh() {
        refresh(false);
    }

    private void refresh(boolean animate) {
        boolean completed = engine.isEvaluated();
        String equation = completed ? engine.getCompletedEquation() : engine.getExpression();
        String answer = completed ? engine.getExpression() : engine.getPreview();
        layout.update(NumberFormatter.format(equation),
                engine.isError() ? engine.getErrorMessage() : NumberFormatter.format(answer),
                engine.isError(), !"0".equals(engine.getMemory()), completed, animate);
    }

    private void save() {
        JSONArray saved = new JSONArray();
        for (String[] entry : history) {
            JSONArray item = new JSONArray(); item.put(entry[0]); item.put(entry[1]); saved.put(item);
        }
        preferences.edit().putString("expression", engine.getExpression()).putString("memory", engine.getMemory())
                .putString("state", engine.saveState())
                .putString("history", saved.toString()).putBoolean("haptics", haptics).putBoolean("dark", dark).apply();
    }

    @Override protected void onPause() { super.onPause(); save(); }

    @Override public void onTool(String tool) {
        switch (tool) {
            case "settings": showSettings(); break;
            case "history": showHistory(); break;
            case "units": ConverterDialogs.showUnits(this, currentValue(), this::useValue); break;
            case "currency": ConverterDialogs.showCurrency(this, currentValue(), this::useValue); break;
            case "fullscreen": toggleFullScreen(); break;
            case "copy":
                if (engine.getValue().isEmpty()) {
                    Toast.makeText(this, "Complete a calculation to copy its result", Toast.LENGTH_SHORT).show();
                    break;
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Calculation result", currentValue()));
                Toast.makeText(this, "Result copied", Toast.LENGTH_SHORT).show(); break;
            default: break;
        }
    }

    private String currentValue() { return engine.getValue().isEmpty() ? "0" : engine.getValue(); }
    private void useValue(String value) { engine.restore(value, engine.getMemory()); refresh(); save(); }

    @SuppressWarnings("deprecation")
    private void toggleFullScreen() {
        fullScreen = !fullScreen;
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                if (fullScreen) controller.hide(WindowInsets.Type.systemBars()); else controller.show(WindowInsets.Type.systemBars());
            }
        } else applyTheme();
    }

    private void showSettings() {
        LinearLayout content = dialogColumn();
        Switch vibration = new Switch(this);
        vibration.setText("Key vibration"); vibration.setChecked(haptics); vibration.setPadding(0, dp(12), 0, dp(12));
        vibration.setOnCheckedChangeListener((button, checked) -> { haptics = checked; save(); });
        content.addView(vibration);
        Switch theme = new Switch(this);
        theme.setText("Dark appearance"); theme.setChecked(dark); theme.setPadding(0, dp(12), 0, dp(12));
        theme.setOnCheckedChangeListener((button, checked) -> { dark = checked; applyTheme(); refresh(); save(); });
        content.addView(theme);
        TextView about = new TextView(this);
        about.setText("Coral Calculator " + BuildConfig.VERSION_NAME + "\n\nLong-press the display to copy a result.\nCalculations and history stay on this device.");
        about.setTextSize(14); about.setTextColor(0xFF777777); about.setPadding(0, dp(20), 0, dp(8));
        content.addView(about);
        new AlertDialog.Builder(this).setTitle("Settings").setView(content).setPositiveButton("Done", null).show();
    }

    private void showHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("History").setPositiveButton("Done", null);
        if (history.isEmpty()) {
            builder.setMessage("Your completed calculations will appear here. Tap = to save a calculation.");
            builder.show(); return;
        }
        LinearLayout content = dialogColumn();
        ScrollView scroll = new ScrollView(this); scroll.addView(content);
        AlertDialog dialog = builder.setView(scroll).setNeutralButton("Clear history", (d, which) -> { history.clear(); save(); }).create();
        for (String[] entry : history) {
            TextView row = new TextView(this);
            String equation = NumberFormatter.format(entry[0]);
            String answer = NumberFormatter.format(entry[1]);
            row.setText(equation + "\n= " + answer); row.setTextSize(20); row.setTextColor(0xFF363936);
            row.setPadding(0, dp(12), 0, dp(16)); row.setContentDescription(equation + " equals " + answer + ". Tap to use result");
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            row.setOnClickListener(v -> { useValue(entry[1]); dialog.dismiss(); });
            content.addView(row);
        }
        dialog.show();
    }

    private LinearLayout dialogColumn() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(24), dp(8), dp(24), dp(16));
        return content;
    }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        // Handle both halves before a focused button consumes Enter. Dialog windows
        // retain their own input, while Tab, Space and D-pad navigation work normally.
        if (!event.isCtrlPressed() && !event.isAltPressed() && !event.isMetaPressed()
                && (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.ACTION_UP)) {
            String key = calculatorKey(event);
            if (key != null) {
                if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) onKey(key);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private String calculatorKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) return "=";
        if (keyCode == KeyEvent.KEYCODE_DEL) return "⌫";
        if (keyCode == KeyEvent.KEYCODE_ESCAPE) return "AC";
        char typed = (char) event.getUnicodeChar();
        return "0123456789.+-*/%=".indexOf(typed) >= 0 ? String.valueOf(typed) : null;
    }
}
