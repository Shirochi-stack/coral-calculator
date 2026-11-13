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
    private static final String STATE_VERSION = "coral:2";
    private static final String LEGACY_STATE_VERSION = "coral:1";

    private String expression = "";
    private BigDecimal memory = BigDecimal.ZERO;
    private boolean evaluated;
    private String errorMessage;
    private Character repeatOperator;
    private BigDecimal repeatOperand;
    private String expressionBeforeEquals;
    /** Index in {@link #expression} where the next key lands; its end unless the user moved it. */
    private int caret;

    public CalculatorEngine() {}

    /** Accepts the labels printed on the calculator, plus ASCII operator aliases. */
    public void input(String token) {
        if (token == null || token.isEmpty()) return;
        if (editingInside() && !"AC".equals(token) && !"=".equals(token)) {
            inputInside(token);
            return;
        }
        inputAtEnd(token);
        caret = expression.length();
    }

    /** Caret position within {@link #getExpression()}; an empty display holds it after the 0. */
    public int getCaret() {
        return expression.isEmpty() ? 1 : Math.min(caret, expression.length());
    }

    /** Moves the caret within the expression being entered. A completed answer has no caret. */
    public void setCaret(int index) {
        if (evaluated) return;
        caret = Math.max(0, Math.min(expression.length(), index));
    }

    private boolean editingInside() {
        return !evaluated && caret < expression.length();
    }

    private void inputAtEnd(String token) {
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
        String source = completedExpression(expression);
        return source.isEmpty() ? getExpression()
                : source.replace('-', '−').replace('*', '×').replace('/', '÷');
    }

    /** The last equation actually evaluated, for display above its completed answer. */
    public String getCompletedEquation() {
        if (!isEvaluated()) return "";
        String source = completedExpression(expressionBeforeEquals == null
                ? expression : expressionBeforeEquals);
        return source.replace('-', '−').replace('*', '×').replace('/', '÷');
    }

    /** Live answer, retaining the subtotal while the next operator awaits a number. */
    public String getPreview() {
        if (errorMessage != null || evaluated || expression.isEmpty()) return "";
        String source = completedExpression(expression);
        if (source.isEmpty()) return "";
        try {
            Evaluation result = evaluate(source);
            if (source.equals(expression) && result.repeatOperator == null
                    && !source.endsWith("%")) return "";
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
            return format(evaluate(completedExpression(expression)).value);
        } catch (ArithmeticException | IllegalArgumentException exception) {
            return "";
        }
    }

    public boolean isError() {
        return errorMessage != null;
    }

    /** Whether the display currently contains a successfully completed answer. */
    public boolean isEvaluated() {
        return evaluated && errorMessage == null;
    }

    /** Whether the next delete press restores the most recently completed equation. */
    public boolean canUndoEquals() {
        return evaluated && expressionBeforeEquals != null;
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
        if (editingInside()) {
            recallInside();
            return;
        }
        recallAtEnd();
        caret = expression.length();
    }

    private void recallAtEnd() {
        expressionBeforeEquals = null;
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
                + (errorMessage == null ? "" : errorMessage) + "|"
                + (expressionBeforeEquals == null ? "" : expressionBeforeEquals);
    }

    /** Restores a full snapshot, or resets display and memory if it is malformed. */
    public void restoreState(String serializedState) {
        clear();
        memory = BigDecimal.ZERO;
        if (serializedState == null || serializedState.length() > MAX_EXPRESSION_LENGTH * 4) return;
        try {
            String[] fields = serializedState.split("\\|", -1);
            boolean legacy = fields.length == 7 && LEGACY_STATE_VERSION.equals(fields[0]);
            if (!legacy && (fields.length != 8 || !STATE_VERSION.equals(fields[0]))) return;
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
                if (savedEvaluated || !isComplete(completedExpression(savedExpression))
                        || !("Cannot divide by zero".equals(savedError)
                        || "Result is outside the supported range".equals(savedError)
                        || "Cannot calculate".equals(savedError))) return;
            }
            // A completed calculation always stores one canonical numeric result.
            if (savedEvaluated) savedDecimal(savedExpression);

            String savedUndo = legacy || fields[7].isEmpty() ? null : fields[7];
            if (savedUndo != null) {
                if (!savedEvaluated || savedError != null
                        || savedUndo.length() > MAX_EXPRESSION_LENGTH
                        || savedUndo.equals(savedExpression) || !structurallyValid(savedUndo)) return;
                // Reject stale equations and impossible repeat state, including out-of-range
                // operands/results, before allowing a snapshot to change the next delete key.
                Evaluation undone = evaluate(completedExpression(savedUndo));
                if (!format(undone.value).equals(savedExpression)
                        || !sameOperator(undone.repeatOperator, savedOperator)
                        || !sameNumber(undone.repeatOperand, savedOperand)) return;
            }

            expression = savedExpression;
            memory = savedMemory;
            evaluated = savedEvaluated;
            repeatOperator = savedOperator;
            repeatOperand = savedOperand;
            errorMessage = savedError;
            expressionBeforeEquals = savedUndo;
            caret = expression.length();
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
        caret = expression.length();
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
        caret = 0;
        evaluated = false;
        errorMessage = null;
        expressionBeforeEquals = null;
        resetRepeat();
    }

    private void prepareNumericInput() {
        expressionBeforeEquals = null;
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
        if (evaluated && expressionBeforeEquals != null) {
            // Continue the visible equation, applying normal precedence to the full input.
            String continued = completedExpression(expressionBeforeEquals);
            if (continued.length() >= MAX_EXPRESSION_LENGTH) return;
            expression = continued;
        }
        expressionBeforeEquals = null;
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
        String source = evaluated && expressionBeforeEquals != null
                ? completedExpression(expressionBeforeEquals) : expression;
        int start = currentNumberStart(source);
        String prefix = source.substring(0, start);
        String number = source.substring(start);
        String candidate = number.startsWith("-") ? prefix + number.substring(1)
                : prefix + "-" + (number.isEmpty() ? "0" : number);
        if (candidate.length() > MAX_EXPRESSION_LENGTH) return;
        expression = candidate;
        expressionBeforeEquals = null;
        evaluated = false;
        resetRepeat();
    }

    private void appendPercent() {
        expressionBeforeEquals = null;
        if (expression.isEmpty() || expression.endsWith("%")
                || expression.length() >= MAX_EXPRESSION_LENGTH) return;
        char last = expression.charAt(expression.length() - 1);
        if (isOperator(last)) return;
        expression += "%";
        evaluated = false;
        resetRepeat();
    }

    private void backspace() {
        String undo = canUndoEquals() ? expressionBeforeEquals : null;
        expressionBeforeEquals = null;
        errorMessage = null;
        evaluated = false;
        resetRepeat();
        if (undo != null) expression = undo;
        else if (!expression.isEmpty()) expression = expression.substring(0, expression.length() - 1);
    }

    private void equalsPressed() {
        if (errorMessage != null) return;
        // Re-evaluating an already finished lone value does not replace its undo target.
        if (evaluated && repeatOperator == null) return;
        errorMessage = null;
        String source = completedExpression(expression);
        if (source.isEmpty()) return;
        try {
            String undo = expression;
            if (evaluated && repeatOperator != null && repeatOperand != null) {
                undo = expression + repeatOperator + format(repeatOperand);
                expression = format(apply(new BigDecimal(expression, MATH),
                        repeatOperator, repeatOperand));
            } else {
                Evaluation result = evaluate(source);
                expression = format(result.value);
                repeatOperator = result.repeatOperator;
                repeatOperand = result.repeatOperand;
            }
            expressionBeforeEquals = undo.equals(expression) ? null : undo;
            evaluated = true;
        } catch (ArithmeticException exception) {
            expressionBeforeEquals = null;
            errorMessage = exception.getMessage() == null ? "Cannot calculate" : exception.getMessage();
            evaluated = false;
            resetRepeat();
        } catch (IllegalArgumentException exception) {
            expressionBeforeEquals = null;
            errorMessage = "Cannot calculate";
            evaluated = false;
            resetRepeat();
        }
    }

    /**
     * Keys pressed while the caret is inside the expression. Every edit is built as a
     * candidate and kept only if the whole expression is still well formed, so the
     * expression can always be evaluated, saved, and continued from its end.
     */
    private void inputInside(String token) {
        errorMessage = null;
        if ("⌫".equals(token) || "DEL".equals(token)) {
            deleteInside();
            return;
        }
        if ("±".equals(token) || "+/-".equals(token)) {
            toggleSignInside();
            return;
        }
        String normalized = normalize(token);
        if (normalized.length() != 1) return;
        char character = normalized.charAt(0);
        String candidate = expression.substring(0, caret) + character + expression.substring(caret);
        if (Character.isDigit(character) && character <= '9') {
            if (digitsAround(candidate, caret) <= MAX_INPUT_DIGITS) commit(candidate, caret + 1);
        } else if (character == '.' || character == '%') {
            commit(candidate, caret + 1);
        } else if (isOperator(character)) {
            insertOperatorInside(character);
        }
    }

    /** An operator typed next to another replaces it, as it does at the end of the expression. */
    private void insertOperatorInside(char operator) {
        String after = expression.substring(caret);
        if (caret > 0 && isOperator(expression.charAt(caret - 1))) {
            if (!isSign(expression, caret - 1)) {
                commit(expression.substring(0, caret - 1) + operator + after, caret);
            } else if (caret > 1) {
                commit(expression.substring(0, caret - 2) + operator + after, caret - 1);
            }
        } else if (caret > 0 && isOperator(after.charAt(0))) {
            commit(expression.substring(0, caret) + operator + after.substring(1), caret + 1);
        } else {
            commit(expression.substring(0, caret) + operator + after, caret + 1);
        }
    }

    private void deleteInside() {
        if (caret == 0) return;
        int at = caret - 1;
        char removed = expression.charAt(at);
        if (isNumberCharacter(removed) && operandStart(expression, at) == at
                && operandEnd(expression, caret) == caret) {
            // An operand's only digit becomes 0 so the expression stays complete; the next
            // digit typed replaces that 0, and deleting again removes the operand.
            if (removed != '0') {
                commit(expression.substring(0, at) + '0' + expression.substring(caret), caret);
            } else {
                deleteEmptyOperand(at);
            }
            return;
        }
        String candidate = expression.substring(0, at) + expression.substring(caret);
        boolean joinsNumbers = isOperator(removed) && !isSign(expression, at);
        if (joinsNumbers && digitsAround(candidate, at) > MAX_INPUT_DIGITS) return;
        commit(candidate, at);
    }

    /** Removes a lone 0 operand: its sign first, otherwise the operator that introduced it. */
    private void deleteEmptyOperand(int at) {
        if (at > 0 && isSign(expression, at - 1)) {
            commit(expression.substring(0, at - 1) + expression.substring(at), at);
        } else if (at > 0) {
            commit(expression.substring(0, at - 1) + expression.substring(at + 1), at - 1);
        } else {
            int end = 1;
            if (end < expression.length() && expression.charAt(end) == '%') end++;
            if (end < expression.length() && isOperator(expression.charAt(end))) end++;
            commit(expression.substring(end), 0);
        }
    }

    private void toggleSignInside() {
        int start = operandAt(caret);
        if (start < 0) return;
        if (start > 0 && isSign(expression, start - 1)) {
            commit(expression.substring(0, start - 1) + expression.substring(start),
                    caret >= start ? caret - 1 : caret);
        } else {
            commit(expression.substring(0, start) + '-' + expression.substring(start),
                    caret >= start ? caret + 1 : caret);
        }
    }

    /** Replaces the number under the caret, as recall replaces the number being typed. */
    private void recallInside() {
        int start = operandAt(caret);
        if (start < 0) return;
        int end = operandEnd(expression, start);
        if (start > 0 && isSign(expression, start - 1)) start--;
        String recalled = format(memory);
        if (commit(expression.substring(0, start) + recalled + expression.substring(end),
                start + recalled.length())) errorMessage = null;
    }

    /** Start of the digits of the number the caret touches, or -1 between two operators. */
    private int operandAt(int position) {
        int start = operandStart(expression, position);
        if (start < operandEnd(expression, position)) return start;
        if (position > 0 && expression.charAt(position - 1) == '%') {
            return operandStart(expression, position - 1);
        }
        return isSign(expression, position) ? position + 1 : -1;
    }

    private static int operandStart(String source, int position) {
        while (position > 0 && isNumberCharacter(source.charAt(position - 1))) position--;
        return position;
    }

    private static int operandEnd(String source, int position) {
        while (position < source.length() && isNumberCharacter(source.charAt(position))) position++;
        return position;
    }

    private static int digitsAround(String source, int position) {
        int digits = 0;
        int end = operandEnd(source, position);
        for (int index = operandStart(source, position); index < end; index++) {
            if (source.charAt(index) != '.') digits++;
        }
        return digits;
    }

    private static boolean isNumberCharacter(char character) {
        return (character >= '0' && character <= '9') || character == '.';
    }

    /** Whether the minus at this index negates the number after it rather than subtracting. */
    private static boolean isSign(String source, int index) {
        return index < source.length() && source.charAt(index) == '-'
                && (index == 0 || isOperator(source.charAt(index - 1)));
    }

    /**
     * Tidies the numbers in an edited expression (07 becomes 7, .5 becomes 0.5), moving the
     * caret with them, and adopts the result only if it is a well-formed expression.
     */
    private boolean commit(String candidate, int position) {
        StringBuilder text = new StringBuilder(candidate.length() + 1);
        int moved = position;
        int index = 0;
        while (index < candidate.length()) {
            char character = candidate.charAt(index);
            if (!isNumberCharacter(character)) {
                text.append(character);
                index++;
                continue;
            }
            int end = index;
            while (end < candidate.length() && candidate.charAt(end) >= '0'
                    && candidate.charAt(end) <= '9') end++;
            if (end == index) {
                // A leading decimal point gains its 0, and a caret at that spot follows it.
                text.append('0');
                if (index <= position) moved++;
            }
            while (index < end - 1 && candidate.charAt(index) == '0') {
                if (index < position) moved--;
                index++;
            }
            text.append(candidate, index, end);
            index = end;
            if (index < candidate.length() && candidate.charAt(index) == '.') {
                text.append('.');
                index++;
                while (index < candidate.length() && candidate.charAt(index) >= '0'
                        && candidate.charAt(index) <= '9') text.append(candidate.charAt(index++));
            }
        }
        String edited = text.toString();
        if (edited.length() > MAX_EXPRESSION_LENGTH || !structurallyValid(edited)) return false;
        expression = edited;
        caret = Math.max(0, Math.min(edited.length(), moved));
        expressionBeforeEquals = null;
        evaluated = false;
        resetRepeat();
        return true;
    }

    private void updateMemory(boolean subtract) {
        if (errorMessage != null) return;
        try {
            String source = completedExpression(expression);
            BigDecimal value = source.isEmpty() ? BigDecimal.ZERO : evaluate(source).value;
            memory = apply(memory, subtract ? '-' : '+', value);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            // An unfinished or invalid calculation never overwrites stored memory.
        }
    }

    private int currentNumberStart() {
        return currentNumberStart(expression);
    }

    private static int currentNumberStart(String source) {
        int index = source.length() - 1;
        if (index >= 0 && source.charAt(index) == '%') index--;
        while (index >= 0) {
            char character = source.charAt(index);
            if ((character >= '0' && character <= '9') || character == '.') index--;
            else break;
        }
        if (index >= 0 && source.charAt(index) == '-'
                && (index == 0 || isOperator(source.charAt(index - 1)))) index--;
        return index + 1;
    }

    private void resetRepeat() {
        repeatOperator = null;
        repeatOperand = null;
    }

    private static boolean sameOperator(Character first, Character second) {
        return first == null ? second == null : first.equals(second);
    }

    private static boolean sameNumber(BigDecimal first, BigDecimal second) {
        return first == null ? second == null : second != null && first.compareTo(second) == 0;
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

    /**
     * An operator at the end is a pending entry, not an extra operand. Use the
     * completed prefix for the subtotal and equals, while leaving editable input
     * untouched. This also handles a pending negative operand, such as "5+3*-".
     * Invalid arithmetic within that prefix must still be reported as an error.
     */
    private static String completedExpression(String source) {
        int end = source.length();
        while (end > 0 && isOperator(source.charAt(end - 1))) end--;
        return source.substring(0, end);
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
