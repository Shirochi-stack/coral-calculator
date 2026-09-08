package com.coral.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Pure Java calculator state, independent of Android views and lifecycle. */
public final class CalculatorEngine {
    private static final MathContext MATH = new MathContext(16, RoundingMode.HALF_UP);
    private static final int MAX_INPUT_DIGITS = 16;
    private static final int MAX_EXPRESSION_LENGTH = 256;
    private static final int MAX_EXPONENT = 100;
    private static final String STATE_VERSION = "coral:1";

    private String expression = "";
    private BigDecimal memory = BigDecimal.ZERO;
    private boolean evaluated;
    private String errorMessage;
    private Character repeatOperator;
    private BigDecimal repeatOperand;

    public CalculatorEngine() {}

    /** Accepts the labels printed on the calculator, plus ASCII operator aliases. */
    public void input(String token) {
        if (token == null || token.isEmpty()) return;
        if ("AC".equals(token)) {
            clear();
            return;
        }
        if ("⌫".equals(token) || "DEL".equals(token)) {
            backspace();
            return;
        }
        if ("=".equals(token)) {
            equalsPressed();
            return;
        }
        if ("±".equals(token) || "+/-".equals(token)) {
            errorMessage = null;
            toggleSign();
            return;
        }
        if ("%".equals(token)) {
            errorMessage = null;
            appendPercent();
            return;
        }
        String normalized = normalize(token);
        if (normalized.length() != 1) return;
        char character = normalized.charAt(0);
        if (Character.isDigit(character) && character <= '9') {
            prepareNumericInput();
            appendDigit(character);
        } else if (character == '.') {
            prepareNumericInput();
            appendDecimal();
        } else if (isOperator(character)) {
            errorMessage = null;
            appendOperator(character);
        }
    }

    public String getExpression() {
        if (expression.isEmpty()) return "0";
        return expression.replace('-', '−').replace('*', '×').replace('/', '÷');
    }

    /** The equation an equals press will perform, including a repeated operation. */
    public String getEquationForEquals() {
        if (evaluated && repeatOperator != null && repeatOperand != null) {
            return (expression + repeatOperator + format(repeatOperand))
                    .replace('-', '−').replace('*', '×').replace('/', '÷');
        }
        return getExpression();
    }

    /** A live answer for a complete calculation, or empty while entering a value. */
    public String getPreview() {
        if (errorMessage != null || evaluated || expression.isEmpty()) return "";
        try {
            Evaluation result = evaluate(expression);
            if (result.repeatOperator == null && !expression.endsWith("%")) return "";
            return format(result.value).replace('-', '−');
        } catch (ArithmeticException | IllegalArgumentException exception) {
            return "";
        }
    }

    public String getMemory() {
        return format(memory);
    }

    /** Current value in plain decimal notation for copying, history, or converters. */
    public String getValue() {
        if (errorMessage != null) return "";
        if (expression.isEmpty()) return "0";
        try {
            return format(evaluate(expression).value);
        } catch (ArithmeticException | IllegalArgumentException exception) {
            return "";
        }
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public String getErrorMessage() {
        return errorMessage == null ? "" : errorMessage;
    }

    public void memoryClear() {
        memory = BigDecimal.ZERO;
    }

    public void memoryAdd() {
        updateMemory(false);
    }

    public void memorySubtract() {
        updateMemory(true);
    }

    public void memoryRecall() {
        String recalled = format(memory);
        String candidate;
        if (evaluated || expression.isEmpty()) {
            candidate = recalled;
        } else {
            int start = currentNumberStart();
            candidate = expression.substring(0, start) + recalled;
        }
        if (candidate.length() > MAX_EXPRESSION_LENGTH) return;
        expression = candidate;
        errorMessage = null;
        evaluated = false;
        resetRepeat();
    }

    /**
     * Versioned, dependency-free state including the behavior of the next key press.
     * Fields contain only validated numeric/operator text or fixed error messages,
     * so the pipe delimiter needs no escaping.
     */
    public String saveState() {
        return STATE_VERSION + "|" + expression + "|" + format(memory) + "|"
                + (evaluated ? "1" : "0") + "|"
                + (repeatOperator == null ? "" : repeatOperator.toString()) + "|"
                + (repeatOperand == null ? "" : format(repeatOperand)) + "|"
                + (errorMessage == null ? "" : errorMessage);
    }

    /** Restores a full snapshot, or resets display and memory if it is malformed. */
    public void restoreState(String serializedState) {
        clear();
        memory = BigDecimal.ZERO;
        if (serializedState == null || serializedState.length() > MAX_EXPRESSION_LENGTH * 3) return;
        try {
            String[] fields = serializedState.split("\\|", -1);
            if (fields.length != 7 || !STATE_VERSION.equals(fields[0])) return;
            String savedExpression = fields[1];
            if (savedExpression.length() > MAX_EXPRESSION_LENGTH
                    || !structurallyValid(savedExpression)) return;
            BigDecimal savedMemory = savedDecimal(fields[2]);
            if (!"0".equals(fields[3]) && !"1".equals(fields[3])) return;
            boolean savedEvaluated = "1".equals(fields[3]);
            Character savedOperator = null;
            BigDecimal savedOperand = null;
            if (!fields[4].isEmpty() || !fields[5].isEmpty()) {
                if (!savedEvaluated || fields[4].length() != 1
                        || !isOperator(fields[4].charAt(0))) return;
                savedOperator = fields[4].charAt(0);
                savedOperand = savedDecimal(fields[5]);
                if (savedOperator == '/' && savedOperand.signum() == 0) return;
            }
            String savedError = fields[6].isEmpty() ? null : fields[6];
            if (savedError != null) {
                if (savedEvaluated || !isComplete(savedExpression)
                        || !("Cannot divide by zero".equals(savedError)
                        || "Result is outside the supported range".equals(savedError)
                        || "Cannot calculate".equals(savedError))) return;
            }
            // A completed calculation always stores one canonical numeric result.
            if (savedEvaluated) savedDecimal(savedExpression);

            expression = savedExpression;
            memory = savedMemory;
            evaluated = savedEvaluated;
            repeatOperator = savedOperator;
            repeatOperand = savedOperand;
            errorMessage = savedError;
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            // All fields are checked before any restored state is committed.
        }
    }

    /** Restores persisted display text and memory; malformed state safely resets. */
    public void restore(String savedExpression, String savedMemory) {
        clear();
        String candidate = savedExpression == null ? "" : normalize(savedExpression);
        if (candidate.length() <= MAX_EXPRESSION_LENGTH && structurallyValid(candidate)) {
            expression = candidate;
        }
        memory = BigDecimal.ZERO;
        if (savedMemory != null && savedMemory.length() <= MAX_EXPRESSION_LENGTH) {
            try {
                String normalized = normalize(savedMemory);
                if (normalized.matches("-?[0-9]+(\\.[0-9]+)?")) {
                    memory = bounded(new BigDecimal(normalized, MATH));
                }
            } catch (ArithmeticException | IllegalArgumentException ignored) {
                memory = BigDecimal.ZERO;
            }
        }
    }

    private void clear() {
        expression = "";
        evaluated = false;
        errorMessage = null;
        resetRepeat();
    }

    private void prepareNumericInput() {
        if (evaluated || errorMessage != null) expression = "";
        evaluated = false;
        errorMessage = null;
        resetRepeat();
    }

    private void appendDigit(char digit) {
        if (expression.endsWith("%") || expression.length() >= MAX_EXPRESSION_LENGTH) return;
        int start = currentNumberStart();
        String number = expression.substring(start);
        String unsigned = number.startsWith("-") ? number.substring(1) : number;
        int digitCount = unsigned.replace(".", "").length();
        if ("0".equals(unsigned)) {
            expression = expression.substring(0, expression.length() - 1) + digit;
        } else if (digitCount < MAX_INPUT_DIGITS) {
            expression += digit;
        }
    }

    private void appendDecimal() {
        if (expression.endsWith("%") || expression.length() >= MAX_EXPRESSION_LENGTH - 1) return;
        String number = expression.substring(currentNumberStart());
        if (number.contains(".")) return;
        expression += (number.isEmpty() || "-".equals(number)) ? "0." : ".";
    }

    private void appendOperator(char operator) {
        evaluated = false;
        resetRepeat();
        if (expression.isEmpty() || "-".equals(expression)) {
            if (operator == '-') expression = "-";
            return;
        }
        int last = expression.length() - 1;
        if (expression.charAt(last) == '-' && last > 0
                && isOperator(expression.charAt(last - 1))) {
            expression = expression.substring(0, last);
            last--;
        }
        if (isOperator(expression.charAt(last))) {
            expression = expression.substring(0, last) + operator;
        } else if (expression.length() < MAX_EXPRESSION_LENGTH) {
            expression += operator;
        }
    }

    private void toggleSign() {
        evaluated = false;
        resetRepeat();
        int start = currentNumberStart();
        String prefix = expression.substring(0, start);
        String number = expression.substring(start);
        if (number.startsWith("-")) {
            expression = prefix + number.substring(1);
        } else if (expression.length() < MAX_EXPRESSION_LENGTH - 1) {
            expression = prefix + "-" + (number.isEmpty() ? "0" : number);
        }
    }

    private void appendPercent() {
        if (expression.isEmpty() || expression.endsWith("%")
                || expression.length() >= MAX_EXPRESSION_LENGTH) return;
        char last = expression.charAt(expression.length() - 1);
        if (isOperator(last)) return;
        expression += "%";
        evaluated = false;
        resetRepeat();
    }

    private void backspace() {
        errorMessage = null;
        evaluated = false;
        resetRepeat();
        if (!expression.isEmpty()) expression = expression.substring(0, expression.length() - 1);
    }

    private void equalsPressed() {
        if (errorMessage != null) return;
        errorMessage = null;
        if (expression.isEmpty() || !isComplete(expression)) return;
        try {
            if (evaluated && repeatOperator != null && repeatOperand != null) {
                expression = format(apply(new BigDecimal(expression, MATH),
                        repeatOperator, repeatOperand));
            } else {
                Evaluation result = evaluate(expression);
                expression = format(result.value);
                repeatOperator = result.repeatOperator;
                repeatOperand = result.repeatOperand;
            }
            evaluated = true;
        } catch (ArithmeticException exception) {
            errorMessage = exception.getMessage() == null ? "Cannot calculate" : exception.getMessage();
            evaluated = false;
            resetRepeat();
        } catch (IllegalArgumentException exception) {
            errorMessage = "Cannot calculate";
            evaluated = false;
            resetRepeat();
        }
    }

    private void updateMemory(boolean subtract) {
        if (errorMessage != null) return;
        try {
            String source = expression;
            if (!source.isEmpty() && !isComplete(source)) {
                int end = currentNumberStart();
                source = end > 0 ? source.substring(0, end - 1) : "";
            }
            BigDecimal value = source.isEmpty() ? BigDecimal.ZERO : evaluate(source).value;
            memory = apply(memory, subtract ? '-' : '+', value);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            // An unfinished or invalid calculation never overwrites stored memory.
        }
    }

    private int currentNumberStart() {
        int index = expression.length() - 1;
        if (index >= 0 && expression.charAt(index) == '%') index--;
        while (index >= 0) {
            char character = expression.charAt(index);
            if ((character >= '0' && character <= '9') || character == '.') index--;
            else break;
        }
        if (index >= 0 && expression.charAt(index) == '-'
                && (index == 0 || isOperator(expression.charAt(index - 1)))) index--;
        return index + 1;
    }

    private void resetRepeat() {
        repeatOperator = null;
        repeatOperand = null;
    }

    private static String normalize(String value) {
        return value.replace('−', '-').replace('×', '*').replace('÷', '/');
    }

    private static BigDecimal savedDecimal(String text) {
        if (text.length() > MAX_EXPRESSION_LENGTH || !text.matches("-?[0-9]+(\\.[0-9]+)?")) {
            throw new IllegalArgumentException("Invalid saved number");
        }
        BigDecimal value = bounded(new BigDecimal(text, MATH));
        if (!format(value).equals(text)) throw new IllegalArgumentException("Noncanonical saved number");
        return value;
    }

    private static boolean isOperator(char character) {
        return character == '+' || character == '-' || character == '*' || character == '/';
    }

    private static boolean isComplete(String source) {
        return !source.isEmpty() && !isOperator(source.charAt(source.length() - 1));
    }

    private static boolean structurallyValid(String source) {
        int position = 0;
        while (position < source.length()) {
            if (source.charAt(position) == '-') position++;
            if (position == source.length()) return true;
            int digits = 0;
            boolean decimal = false;
            while (position < source.length()) {
                char character = source.charAt(position);
                if (character >= '0' && character <= '9') {
                    digits++;
                    position++;
                } else if (character == '.' && !decimal) {
                    decimal = true;
                    position++;
                } else break;
            }
            if (digits == 0) return false;
            if (position < source.length() && source.charAt(position) == '%') position++;
            if (position == source.length()) return true;
            if (!isOperator(source.charAt(position++))) return false;
        }
        return true;
    }

    private static BigDecimal apply(BigDecimal left, char operator, BigDecimal right) {
        switch (operator) {
            case '+': return bounded(left.add(right, MATH));
            case '-': return bounded(left.subtract(right, MATH));
            case '*': return bounded(left.multiply(right, MATH));
            case '/':
                if (right.signum() == 0) throw new ArithmeticException("Cannot divide by zero");
                return bounded(left.divide(right, MATH));
            default: throw new IllegalArgumentException("Unknown operator");
        }
    }

    private static BigDecimal bounded(BigDecimal value) {
        if (value.signum() == 0) return BigDecimal.ZERO;
        int exponent = value.precision() - value.scale() - 1;
        if (exponent > MAX_EXPONENT || exponent < -MAX_EXPONENT) {
            throw new ArithmeticException("Result is outside the supported range");
        }
        return value;
    }

    private static String format(BigDecimal value) {
        return bounded(value).stripTrailingZeros().toPlainString();
    }

    private static Evaluation evaluate(String source) {
        return new Parser(source).parse();
    }

    private static final class Evaluation {
        BigDecimal value;
        boolean singlePercent;
        Character repeatOperator;
        BigDecimal repeatOperand;

        Evaluation(BigDecimal value, boolean singlePercent) {
            this.value = value;
            this.singlePercent = singlePercent;
        }
    }

    private static final class Parser {
        private final String source;
        private int position;

        Parser(String source) {
            this.source = source;
        }

        Evaluation parse() {
            Evaluation left = term();
            while (position < source.length()) {
                char operator = source.charAt(position++);
                if (operator != '+' && operator != '-') throw new IllegalArgumentException("Invalid expression");
                Evaluation right = term();
                BigDecimal operand = right.singlePercent
                        ? bounded(left.value.multiply(right.value, MATH)) : right.value;
                left.value = apply(left.value, operator, operand);
                left.singlePercent = false;
                left.repeatOperator = operator;
                left.repeatOperand = operand;
            }
            return left;
        }

        private Evaluation term() {
            Evaluation left = number();
            while (position < source.length()) {
                char operator = source.charAt(position);
                if (operator != '*' && operator != '/') break;
                position++;
                Evaluation right = number();
                left.value = apply(left.value, operator, right.value);
                left.singlePercent = false;
                left.repeatOperator = operator;
                left.repeatOperand = right.value;
            }
            return left;
        }

        private Evaluation number() {
            int start = position;
            if (position < source.length() && source.charAt(position) == '-') position++;
            boolean decimal = false;
            int digits = 0;
            while (position < source.length()) {
                char character = source.charAt(position);
                if (character >= '0' && character <= '9') {
                    position++;
                    digits++;
                } else if (character == '.' && !decimal) {
                    position++;
                    decimal = true;
                } else break;
            }
            if (digits == 0) throw new IllegalArgumentException("Incomplete expression");
            BigDecimal value = bounded(new BigDecimal(source.substring(start, position), MATH));
            boolean percent = position < source.length() && source.charAt(position) == '%';
            if (percent) {
                position++;
                value = bounded(value.movePointLeft(2));
            }
            return new Evaluation(value, percent);
        }
    }
}
