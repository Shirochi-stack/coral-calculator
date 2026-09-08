package com.coral.calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Consumer;

/** Offline converters which return a plain decimal to the calculator. */
public final class ConverterDialogs {
    private static final int CORAL = Color.rgb(255, 57, 63);
    private static final int INK = Color.rgb(48, 48, 48);
    private static final int MUTED = Color.rgb(112, 112, 112);
    private static final int SURFACE = Color.rgb(250, 250, 250);
    private static final MathContext PRECISION = new MathContext(16, RoundingMode.HALF_UP);
    private static final int MAX_DECIMAL_LENGTH = 256;
    private static final int MAX_RESULT_EXPONENT = 100;

    private ConverterDialogs() {}

    public static void showUnits(Activity activity, String initialValue,
                                 Consumer<String> onUseResult) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        new UnitsDialog(activity, initialValue, onUseResult).show();
    }

    public static void showCurrency(Activity activity, String initialValue,
                                    Consumer<String> onUseResult) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        new CurrencyDialog(activity, initialValue, onUseResult).show();
    }

    private abstract static class ConversionDialog {
        final Context context;
        final LinearLayout content;
        final ScrollView scroll;
        final TextView result;
        final TextView status;
        final String title;
        final Consumer<String> callback;
        AlertDialog dialog;
        String resultValue;

        ConversionDialog(Activity activity, String title, Consumer<String> callback) {
            this.context = new ContextThemeWrapper(activity,
                    android.R.style.Theme_Material_Light_Dialog_Alert);
            this.title = title;
            this.callback = callback;
            scroll = new ScrollView(context);
            scroll.setFillViewport(true);
            content = new LinearLayout(context);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dp(24), dp(4), dp(24), dp(8));
            scroll.addView(content);
            result = text("", 29, INK);
            result.setTextIsSelectable(true);
            result.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            result.setPadding(0, dp(8), 0, dp(2));
            status = text("", 14, MUTED);
            status.setPadding(0, dp(2), 0, dp(6));
            status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        }

        final void show() {
            dialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setView(scroll)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Use result", null)
                    .create();
            dialog.setOnShowListener(ignored -> {
                Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positive.setTextColor(CORAL);
                positive.setAllCaps(false);
                positive.setOnClickListener(view -> {
                    if (resultValue == null) return;
                    String selectedResult = resultValue;
                    dialog.dismiss();
                    if (callback != null) callback.accept(selectedResult);
                });
                Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negative.setTextColor(MUTED);
                negative.setAllCaps(false);
                Window window = dialog.getWindow();
                if (window != null) {
                    GradientDrawable background = new GradientDrawable();
                    background.setColor(SURFACE);
                    background.setCornerRadius(dp(24));
                    window.setBackgroundDrawable(background);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
                update();
            });
            dialog.show();
        }

        abstract void update();

        final void showResult(BigDecimal number, String suffix, String explanation) {
            BigDecimal rounded = number.round(PRECISION).stripTrailingZeros();
            int exponent = rounded.precision() - rounded.scale() - 1;
            if (rounded.signum() != 0
                    && (exponent > MAX_RESULT_EXPONENT || exponent < -MAX_RESULT_EXPONENT)) {
                showInvalid("Result is outside the calculator's supported range.");
                return;
            }
            String converted = rounded.toPlainString();
            if (converted.length() > MAX_DECIMAL_LENGTH) {
                showInvalid("Result is outside the calculator's supported range.");
                return;
            }
            resultValue = converted;
            result.setText(resultValue + (suffix.isEmpty() ? "" : " " + suffix));
            result.setContentDescription("Result: " + resultValue + " " + suffix);
            status.setText(explanation);
            status.setTextColor(MUTED);
            if (dialog != null && dialog.isShowing()) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }

        final void showInvalid(String message) {
            resultValue = null;
            result.setText("—");
            result.setContentDescription("No conversion result");
            status.setText(message);
            status.setTextColor(MUTED);
            if (dialog != null && dialog.isShowing()) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }

        final int dp(int value) {
            return Math.round(value * context.getResources().getDisplayMetrics().density);
        }

        final TextView text(String value, int size, int color) {
            TextView view = new TextView(context);
            view.setText(value);
            view.setTextSize(size);
            view.setTextColor(color);
            view.setLineSpacing(dp(2), 1f);
            return view;
        }

        final TextView addLabel(String value) {
            TextView label = text(value, 13, MUTED);
            label.setPadding(0, dp(14), 0, dp(2));
            content.addView(label);
            return label;
        }

        final EditText addNumberInput(String label, String value, boolean signed) {
            TextView labelView = addLabel(label);
            EditText input = new EditText(context);
            input.setId(View.generateViewId());
            labelView.setLabelFor(input.getId());
            input.setTextColor(INK);
            input.setTextSize(22);
            input.setSingleLine(true);
            input.setSelectAllOnFocus(true);
            input.setMinHeight(dp(52));
            input.setPadding(dp(2), dp(8), dp(2), dp(8));
            input.setBackgroundTintList(ColorStateList.valueOf(CORAL));
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | (signed ? InputType.TYPE_NUMBER_FLAG_SIGNED : 0));
            input.setFilters(new InputFilter[] {(source, start, end, destination,
                                                 destinationStart, destinationEnd) -> {
                int proposedLength = destination.length() - (destinationEnd - destinationStart)
                        + (end - start);
                if (proposedLength > MAX_DECIMAL_LENGTH) {
                    input.setError("Use at most " + MAX_DECIMAL_LENGTH + " characters.");
                    // Reject the whole edit so pasted or supplied numbers never change magnitude.
                    return destination.subSequence(destinationStart, destinationEnd);
                }
                input.setError(null);
                return null;
            }});
            input.setText(value);
            input.setContentDescription(label);
            content.addView(input, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return input;
        }

        final Spinner addSpinner(String label, String[] choices) {
            TextView labelView = addLabel(label);
            Spinner spinner = makeSpinner(label, choices);
            labelView.setLabelFor(spinner.getId());
            content.addView(spinner, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
            return spinner;
        }

        final Spinner makeSpinner(String label, String[] choices) {
            Spinner spinner = new Spinner(context, Spinner.MODE_DROPDOWN);
            spinner.setId(View.generateViewId());
            spinner.setContentDescription(label);
            spinner.setMinimumHeight(dp(52));
            spinner.setBackgroundTintList(ColorStateList.valueOf(MUTED));
            setChoices(spinner, choices);
            return spinner;
        }

        final void setChoices(Spinner spinner, String[] choices) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, choices);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        final Button addSwap(String description) {
            Button swap = new Button(context);
            swap.setText("⇄  Swap");
            swap.setTextSize(14);
            swap.setAllCaps(false);
            swap.setTextColor(CORAL);
            swap.setContentDescription(description);
            swap.setMinHeight(dp(48));
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.rgb(255, 234, 235));
            background.setCornerRadius(dp(24));
            swap.setBackground(background);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(120), dp(48));
            params.gravity = Gravity.END;
            params.topMargin = dp(8);
            content.addView(swap, params);
            return swap;
        }

        final void addResultViews() {
            addLabel("Result");
            content.addView(result);
            content.addView(status);
        }

        final void watch(EditText input) {
            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count,
                                                         int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before,
                                                     int count) { update(); }
                @Override public void afterTextChanged(Editable editable) {}
            });
        }

        final void onSelection(Spinner spinner, Runnable action) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view,
                                                     int position, long id) { action.run(); }
                @Override public void onNothingSelected(AdapterView<?> parent) { action.run(); }
            });
        }
    }

    private static final class Unit {
        final String name;
        final String symbol;
        final BigDecimal factor;

        Unit(String name, String symbol, String factor) {
            this.name = name;
            this.symbol = symbol;
            this.factor = new BigDecimal(factor);
        }
    }

    private static final Unit[][] UNITS = {
            {new Unit("Meters", "m", "1"), new Unit("Kilometers", "km", "1000"),
                    new Unit("Centimeters", "cm", "0.01"),
                    new Unit("Millimeters", "mm", "0.001"),
                    new Unit("Inches", "in", "0.0254"), new Unit("Feet", "ft", "0.3048"),
                    new Unit("Yards", "yd", "0.9144"), new Unit("Miles", "mi", "1609.344")},
            {new Unit("Kilograms", "kg", "1"), new Unit("Grams", "g", "0.001"),
                    new Unit("Milligrams", "mg", "0.000001"),
                    new Unit("Ounces", "oz", "0.028349523125"),
                    new Unit("Pounds", "lb", "0.45359237")},
            {new Unit("Celsius", "°C", "1"), new Unit("Fahrenheit", "°F", "1"),
                    new Unit("Kelvin", "K", "1")}
    };

    private static final class UnitsDialog extends ConversionDialog {
        final Spinner category;
        final Spinner from;
        final Spinner to;
        final EditText amount;
        int loadedCategory = -1;

        UnitsDialog(Activity activity, String initialValue, Consumer<String> callback) {
            super(activity, "Unit converter", callback);
            category = addSpinner("Measurement", new String[] {"Length", "Mass", "Temperature"});
            amount = addNumberInput("Amount", initial(initialValue), true);
            from = addSpinner("From", names(UNITS[0]));
            to = addSpinner("To", names(UNITS[0]));
            to.setSelection(5);
            loadedCategory = 0;
            Button swap = addSwap("Swap source and destination units");
            addResultViews();
            swap.setOnClickListener(view -> {
                int oldFrom = from.getSelectedItemPosition();
                from.setSelection(to.getSelectedItemPosition());
                to.setSelection(oldFrom);
                update();
            });
            onSelection(category, () -> {
                int selected = category.getSelectedItemPosition();
                if (selected >= 0 && selected < UNITS.length && loadedCategory != selected) {
                    loadedCategory = selected;
                    setChoices(from, names(UNITS[selected]));
                    setChoices(to, names(UNITS[selected]));
                    to.setSelection(1);
                }
                update();
            });
            onSelection(from, this::update);
            onSelection(to, this::update);
            watch(amount);
        }

        @Override void update() {
            BigDecimal input = parse(amount.getText().toString());
            if (input == null) {
                showInvalid("Enter an amount to convert.");
                return;
            }
            int c = category.getSelectedItemPosition();
            int source = from.getSelectedItemPosition();
            int destination = to.getSelectedItemPosition();
            if (c < 0 || c >= UNITS.length || source < 0 || destination < 0
                    || source >= UNITS[c].length || destination >= UNITS[c].length) {
                showInvalid("Choose the source and destination units.");
                return;
            }
            try {
                BigDecimal converted;
                if (source == destination) {
                    converted = input;
                } else if (c == 2) {
                    BigDecimal celsius = input;
                    if (source == 1) celsius = input.subtract(new BigDecimal("32"))
                            .multiply(new BigDecimal("5"))
                            .divide(new BigDecimal("9"), PRECISION);
                    if (source == 2) celsius = input.subtract(new BigDecimal("273.15"));
                    converted = celsius;
                    if (destination == 1) converted = celsius.multiply(new BigDecimal("9"))
                            .divide(new BigDecimal("5"), PRECISION).add(new BigDecimal("32"));
                    if (destination == 2) converted = celsius.add(new BigDecimal("273.15"));
                } else {
                    converted = input.multiply(UNITS[c][source].factor)
                            .divide(UNITS[c][destination].factor, PRECISION);
                }
                showResult(converted, UNITS[c][destination].symbol,
                        "Use result sends this value to the calculator.");
            } catch (ArithmeticException exception) {
                showInvalid("This amount is too large to convert.");
            }
        }
    }

    private static final class CurrencyDialog extends ConversionDialog {
        private static final String[] CURRENCIES = {
                "USD", "AED", "EUR", "GBP", "INR", "JPY", "CAD", "AUD", "SAR", "Other"
        };
        final EditText amount;
        final EditText rate;
        final Spinner from;
        final Spinner to;
        final TextView rateHint;
        int pairFrom = 0;
        int pairTo = 1;

        CurrencyDialog(Activity activity, String initialValue, Consumer<String> callback) {
            super(activity, "Currency converter", callback);
            TextView note = text("Enter your exchange rate", 17, INK);
            note.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            note.setPadding(0, dp(8), 0, dp(4));
            content.addView(note);
            content.addView(text("Manual conversion. Rates are not fetched or updated online.",
                    14, MUTED));
            amount = addNumberInput("Amount", initial(initialValue), true);
            from = addSpinner("From currency", CURRENCIES);
            to = addSpinner("To currency", CURRENCIES);
            to.setSelection(pairTo);
            rate = addNumberInput("Exchange rate", "", false);
            rate.setHint("Enter a positive rate");
            rateHint = text("", 13, MUTED);
            content.addView(rateHint);
            Button swap = addSwap("Swap currencies and invert the entered exchange rate");
            addResultViews();
            swap.setOnClickListener(view -> {
                BigDecimal oldRate = parse(rate.getText().toString());
                int oldFrom = from.getSelectedItemPosition();
                pairFrom = to.getSelectedItemPosition();
                pairTo = oldFrom;
                from.setSelection(pairFrom);
                to.setSelection(pairTo);
                if (oldRate != null && oldRate.signum() > 0) {
                    rate.setText(format(BigDecimal.ONE.divide(oldRate, PRECISION)));
                } else {
                    rate.setText("");
                }
                update();
            });
            onSelection(from, this::pairChanged);
            onSelection(to, this::pairChanged);
            watch(amount);
            watch(rate);
        }

        private void pairChanged() {
            int currentFrom = from.getSelectedItemPosition();
            int currentTo = to.getSelectedItemPosition();
            if (currentFrom != pairFrom || currentTo != pairTo) {
                pairFrom = currentFrom;
                pairTo = currentTo;
                rate.setText("");
            }
            update();
        }

        @Override void update() {
            String source = currency(from);
            String destination = currency(to);
            rateHint.setText("1 " + source + " = your rate in " + destination);
            BigDecimal input = parse(amount.getText().toString());
            BigDecimal exchangeRate = parse(rate.getText().toString());
            if (input == null) {
                showInvalid("Enter an amount to convert.");
            } else if (exchangeRate == null || exchangeRate.signum() <= 0) {
                showInvalid("Enter an exchange rate greater than zero.");
            } else {
                try {
                    showResult(input.multiply(exchangeRate, PRECISION), destination,
                            "Calculated with the rate you entered. Fees are not included.");
                } catch (ArithmeticException exception) {
                    showInvalid("This amount or rate is too large to convert.");
                }
            }
        }

        private String currency(Spinner spinner) {
            int selection = spinner.getSelectedItemPosition();
            return selection >= 0 && selection < CURRENCIES.length
                    ? CURRENCIES[selection] : "currency";
        }
    }

    private static String[] names(Unit[] units) {
        String[] choices = new String[units.length];
        for (int i = 0; i < units.length; i++) {
            choices[i] = units[i].name + " (" + units[i].symbol + ")";
        }
        return choices;
    }

    private static BigDecimal parse(String input) {
        if (input == null) return null;
        String normalized = input.trim().replace('−', '-');
        if (normalized.isEmpty() || normalized.length() > MAX_DECIMAL_LENGTH) return null;
        // These fields accept decimals, not exponents, grouping, or calculator expressions.
        if (!normalized.matches("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")) return null;
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String initial(String value) {
        BigDecimal parsed = parse(value);
        return parsed == null ? "0" : format(parsed);
    }

    private static String format(BigDecimal value) {
        return value.round(PRECISION).stripTrailingZeros().toPlainString();
    }
}
