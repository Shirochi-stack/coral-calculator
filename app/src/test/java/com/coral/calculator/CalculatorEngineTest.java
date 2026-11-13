package com.coral.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CalculatorEngineTest {
    private final CalculatorEngine calculator = new CalculatorEngine();

    private void enter(String... tokens) {
        for (String token : tokens) calculator.input(token);
    }

    private void type(String text) {
        for (int index = 0; index < text.length(); index++) {
            calculator.input(text.substring(index, index + 1));
        }
    }

    @Test public void startsEmptyAndMatchesReferenceCalculation() {
        assertEquals("0", calculator.getExpression());
        assertEquals("", calculator.getPreview());
        type("52+96");
        assertEquals("52+96", calculator.getExpression());
        assertEquals("148", calculator.getPreview());
        enter("=");
        assertEquals("148", calculator.getExpression());
        assertEquals("", calculator.getPreview());
    }

    @Test public void observesPrecedenceAndLeftAssociativeDivision() {
        type("2+3×4−8÷2");
        assertEquals("10", calculator.getPreview());
        enter("AC");
        type("24÷3×2");
        enter("=");
        assertEquals("16", calculator.getExpression());
    }

    @Test public void decimalArithmeticAvoidsBinaryFloatingPointArtifacts() {
        type("0.1+0.2");
        enter("=");
        assertEquals("0.3", calculator.getExpression());
        enter("AC");
        type("1÷3");
        enter("=");
        assertEquals("0.3333333333333333", calculator.getExpression());
    }

    @Test public void percentIsContextualForAdditionAndSubtraction() {
        type("200+10%");
        assertEquals("220", calculator.getPreview());
        enter("AC");
        type("200−10%");
        assertEquals("180", calculator.getPreview());
        enter("AC");
        type("200×10%");
        assertEquals("20", calculator.getPreview());
        enter("AC");
        type("50%");
        assertEquals("0.5", calculator.getPreview());
    }

    @Test public void compoundPercentUsesItsImmediateArithmeticContext() {
        type("200+10%+10%");
        assertEquals("242", calculator.getPreview());
        enter("AC");
        type("100+5×10%");
        assertEquals("100.5", calculator.getPreview());
    }

    @Test public void signToggleWorksForCurrentOperandAndEmptyInput() {
        enter("±", "5", "×", "2", "±");
        assertEquals("−5×−2", calculator.getExpression());
        assertEquals("10", calculator.getPreview());
        enter("±");
        assertEquals("−10", calculator.getPreview());
        enter("AC", "2", "+", "±", "3");
        assertEquals("2+−3", calculator.getExpression());
        assertEquals("−1", calculator.getPreview());
    }

    @Test public void signAfterEqualsResumesTheLastOperandAndPreservesNewUndo() {
        type("5+3");
        enter("=", "+/-");
        assertEquals("5+−3", calculator.getExpression());
        assertEquals("2", calculator.getPreview());
        assertFalse(calculator.canUndoEquals());
        enter("±");
        assertEquals("5+3", calculator.getExpression());
        enter("±", "=", "⌫");
        assertEquals("5+−3", calculator.getExpression());
        enter("=", "±");
        assertEquals("5+3", calculator.getExpression());
        enter("AC", "5", "=", "±");
        assertEquals("−5", calculator.getExpression());
        calculator.restoreState("coral:1|148|25|1|+|96|");
        enter("±");
        assertEquals("−148", calculator.getExpression());
    }

    @Test public void signContinuationRestoresPendingPercentAndTheLatestRepeatedEquation() {
        calculator.restore("200+10%×−", "25");
        enter("=");
        calculator.restoreState(calculator.saveState());
        enter("±");
        assertEquals("200+−10%", calculator.getExpression());
        assertEquals("180", calculator.getPreview());
        assertEquals("25", calculator.getMemory());
        enter("=");
        calculator.restoreState(calculator.saveState());
        assertEquals("200+−10%", calculator.getCompletedEquation());
        enter("⌫");
        assertEquals("200+−10%", calculator.getExpression());
        enter("AC");
        type("5+3");
        enter("=", "=");
        calculator.restoreState(calculator.saveState());
        enter("±");
        assertEquals("8+−3", calculator.getExpression());
        assertEquals("5", calculator.getPreview());
    }

    @Test public void signContinuationCommitsOnlyWhenTheChangedOperandFits() {
        String full = "1+".repeat(127) + "10";
        calculator.restore(full, "25");
        enter("=");
        String savedResult = calculator.saveState();
        enter("±");
        assertEquals(savedResult, calculator.saveState());
        enter("⌫");
        assertEquals(full, calculator.getExpression());

        String nearlyFull = "1+".repeat(127) + "1";
        calculator.restore(nearlyFull, "25");
        enter("=", "±");
        assertEquals(256, calculator.getExpression().length());
        assertEquals("126", calculator.getPreview());
        enter("=", "±");
        assertEquals(nearlyFull, calculator.getExpression());
        assertEquals("128", calculator.getPreview());
    }

    @Test public void repeatedEqualsRepeatsTheResolvedLastOperation() {
        type("2+3×4");
        enter("=", "=");
        assertEquals("26", calculator.getExpression());
        enter("=");
        assertEquals("38", calculator.getExpression());
        enter("AC");
        type("5×2");
        enter("=", "=");
        assertEquals("20", calculator.getExpression());
    }

    @Test public void digitAfterEqualsStartsOverAndOperatorContinues() {
        type("2+3");
        enter("=", "×", "4", "=");
        assertEquals("14", calculator.getExpression());
        assertEquals("2+3×4", calculator.getCompletedEquation());
        enter("9");
        assertEquals("9", calculator.getExpression());
    }

    @Test public void plusAfterEqualsContinuesTheOriginalCalculationAndKeepsNewUndo() {
        type("5645+44646");
        enter("=", "+");
        assertEquals("5645+44646+", calculator.getExpression());
        assertEquals("50291", calculator.getPreview());
        assertFalse(calculator.isEvaluated());
        assertFalse(calculator.canUndoEquals());
        assertEquals("", calculator.getCompletedEquation());
        enter("9");
        assertEquals("5645+44646+9", calculator.getExpression());
        assertEquals("50300", calculator.getPreview());
        enter("=");
        assertEquals("50300", calculator.getExpression());
        assertEquals("5645+44646+9", calculator.getCompletedEquation());
        enter("⌫");
        assertEquals("5645+44646+9", calculator.getExpression());
        enter("⌫");
        assertEquals("5645+44646+", calculator.getExpression());
    }

    @Test public void operatorsAfterRepeatedEqualsContinueTheMostRecentEquation() {
        String[] operators = {"+", "−", "×", "÷"};
        String[] expected = {"15", "7", "20", "8.75"};
        for (int index = 0; index < operators.length; index++) {
            enter("AC");
            type("5+3");
            enter("=", "=", operators[index]);
            assertEquals("8+3" + operators[index], calculator.getExpression());
            assertEquals("11", calculator.getPreview());
            enter("4", "=");
            assertEquals(operators[index], expected[index], calculator.getExpression());
            assertEquals("8+3" + operators[index] + "4", calculator.getCompletedEquation());
        }
    }

    @Test public void operatorContinuationNormalizesPendingOperatorsAndRetainsLaterUndoExactly() {
        for (String suffix : new String[] {"+", "−", "×", "÷", "×−"}) {
            for (String operator : new String[] {"+", "−", "×", "÷"}) {
                calculator.restore("5+3" + suffix, "0");
                enter("=", operator);
                assertEquals(suffix, "5+3" + operator, calculator.getExpression());
                assertEquals(suffix, "8", calculator.getPreview());
                enter("=", "⌫");
                assertEquals(suffix, "5+3" + operator, calculator.getExpression());
            }
        }
    }

    @Test public void operatorContinuationSurvivesRecreationBeforeAndAfterEditing() {
        String[] operators = {"+", "−", "×", "÷"};
        String[] expected = {"150", "146", "244", "100"};
        for (int index = 0; index < operators.length; index++) {
            calculator.restore("52+96", "148");
            enter("=");
            CalculatorEngine recreated = new CalculatorEngine();
            recreated.restoreState(calculator.saveState());
            recreated.input(operators[index]);
            assertEquals("52+96" + operators[index], recreated.getExpression());
            calculator.restoreState(recreated.saveState());
            assertEquals("148", calculator.getMemory());
            enter("2", "=");
            assertEquals(operators[index], expected[index], calculator.getExpression());
            recreated.restoreState(calculator.saveState());
            String continuedEquation = "52+96" + operators[index] + "2";
            assertEquals(continuedEquation, recreated.getCompletedEquation());
            recreated.input("⌫");
            assertEquals(continuedEquation, recreated.getExpression());
        }
    }

    @Test public void operatorContinuationPreservesPercentContextAndRoundedSubtotals() {
        String[] operators = {"+", "−", "×", "÷"};
        String[] expected = {"242", "198", "200.01", "201"};
        for (int index = 0; index < operators.length; index++) {
            enter("AC");
            type("200+10%");
            enter("=", operators[index]);
            assertEquals("200+10%" + operators[index], calculator.getExpression());
            assertEquals("220", calculator.getPreview());
            type("10%");
            enter("=");
            assertEquals(operators[index], expected[index], calculator.getExpression());
        }
        enter("AC");
        type("50%");
        enter("=", "+", "1", "=");
        assertEquals("1.5", calculator.getExpression());
        assertEquals("50%+1", calculator.getCompletedEquation());

        enter("AC");
        type("9999999999999999+2");
        enter("=");
        String subtotal = calculator.getExpression();
        assertEquals("10000000000000000", subtotal);
        enter("+");
        assertEquals(subtotal, calculator.getPreview());
        enter("9");
        assertEquals("10000000000000010", calculator.getPreview());
        assertEquals("9999999999999999+2+9", calculator.getExpression());
    }

    @Test public void allOperatorsAfterEqualsContinueTheEquationWithNormalPrecedence() {
        String[] operators = {"+", "−", "×", "÷"};
        String[] expected = {"10", "6", "11", "6.5"};
        for (int index = 0; index < operators.length; index++) {
            enter("AC");
            type("5+3");
            enter("=", operators[index]);
            assertEquals("5+3" + operators[index], calculator.getExpression());
            enter("2", "=");
            assertEquals(operators[index], expected[index], calculator.getExpression());
            enter("⌫");
            assertEquals("5+3" + operators[index] + "2", calculator.getExpression());
        }
    }

    @Test public void operatorContinuationFallsBackForLoneLegacyNewInputAndErrors() {
        for (String operator : new String[] {"+", "−", "×", "÷"}) {
            enter("AC", "5", "=", operator);
            assertEquals("5" + operator, calculator.getExpression());
            calculator.restoreState("coral:1|148|25|1|+|96|");
            enter(operator);
            assertEquals("148" + operator, calculator.getExpression());
            enter("AC");
            type("5+3");
            enter("=", "9", operator);
            assertEquals("9" + operator, calculator.getExpression());
        }
        enter("AC");
        type("5+3");
        enter("=", "÷", "0", "=", "+");
        assertEquals("5+3÷0+", calculator.getExpression());
        assertFalse(calculator.isEvaluated());
        assertFalse(calculator.canUndoEquals());
        assertEquals("", calculator.getPreview());
        enter("=");
        assertTrue(calculator.isError());
    }

    @Test public void operatorContinuationHonorsTheExpressionLimitWithoutDiscardingTheResult() {
        String expressionAtLimit = "1+".repeat(127) + "10";
        assertEquals(256, expressionAtLimit.length());
        calculator.restore(expressionAtLimit, "25");
        enter("=");
        String savedResult = calculator.saveState();
        for (String operator : new String[] {"+", "−", "×", "÷"}) {
            enter(operator);
            assertEquals(operator, savedResult, calculator.saveState());
        }
        assertTrue(calculator.isEvaluated());
        assertTrue(calculator.canUndoEquals());
        enter("⌫");
        assertEquals(expressionAtLimit, calculator.getExpression());

        String nearlyFull = "1+".repeat(126) + "10";
        assertEquals(254, nearlyFull.length());
        calculator.restore(nearlyFull, "25");
        enter("=", "+", "9");
        assertEquals(nearlyFull + "+9", calculator.getExpression());
        assertEquals(256, calculator.getExpression().length());
        enter("=");
        assertEquals("145", calculator.getExpression());
        enter("⌫");
        assertEquals(nearlyFull + "+9", calculator.getExpression());
    }

    @Test public void deleteAfterEqualsRestoresEditableEquationThenDeletesNormally() {
        type("5645+44646");
        enter("=");
        assertEquals("50291", calculator.getExpression());
        assertTrue(calculator.canUndoEquals());
        enter("⌫");
        assertEquals("5645+44646", calculator.getExpression());
        assertEquals("50291", calculator.getPreview());
        assertFalse(calculator.canUndoEquals());
        enter("⌫");
        assertEquals("5645+4464", calculator.getExpression());
        assertEquals("10109", calculator.getPreview());
        enter("7", "=");
        assertEquals("50292", calculator.getExpression());
        enter("DEL");
        assertEquals("5645+44647", calculator.getExpression());
    }

    @Test public void repeatedEqualsUndoRestoresTheMostRecentResolvedOperation() {
        type("5+3");
        enter("=", "=");
        assertEquals("11", calculator.getExpression());
        enter("⌫");
        assertEquals("8+3", calculator.getExpression());
        assertEquals("11", calculator.getPreview());
        enter("=", "=", "⌫");
        assertEquals("11+3", calculator.getExpression());

        enter("AC");
        type("2+3×4");
        enter("=", "=", "⌫");
        assertEquals("14+12", calculator.getExpression());
        assertEquals("26", calculator.getPreview());
    }

    @Test public void undoPreservesTypedDecimalsNegativeOperandsAndPercentContext() {
        type("200.00+10%");
        enter("=", "⌫");
        assertEquals("200.00+10%", calculator.getExpression());
        assertEquals("220", calculator.getPreview());
        enter("=", "=", "⌫");
        assertEquals("220+20", calculator.getExpression());
        assertEquals("240", calculator.getPreview());
        enter("AC");
        type("2×3");
        enter("±", "=", "⌫");
        assertEquals("2×−3", calculator.getExpression());
        assertEquals("−6", calculator.getPreview());
        enter("=", "=", "⌫");
        assertEquals("−6×−3", calculator.getExpression());
        assertEquals("18", calculator.getPreview());
    }

    @Test public void undoPreservesAllPendingOperatorsAndPendingUnarySign() {
        for (String operator : new String[] {"+", "−", "×", "÷"}) {
            enter("AC");
            type("5+3" + operator);
            enter("=", "⌫");
            assertEquals("5+3" + operator, calculator.getExpression());
            assertEquals("8", calculator.getPreview());
            enter("2", "=");
            assertFalse(calculator.isError());
        }
        calculator.restore("5+3×−", "0");
        enter("=", "⌫");
        assertEquals("5+3×−", calculator.getExpression());
        assertEquals("8", calculator.getPreview());
        enter("2", "=");
        assertEquals("−1", calculator.getExpression());
    }

    @Test public void repeatedNoOpEqualsRetainsPercentAndPendingOperatorUndo() {
        type("50%");
        enter("=", "=", "=", "⌫");
        assertEquals("50%", calculator.getExpression());
        assertEquals("0.5", calculator.getPreview());
        enter("AC");
        type("5+");
        enter("=", "=", "⌫");
        assertEquals("5+", calculator.getExpression());
        enter("AC");
        type("5.");
        enter("=", "=", "⌫");
        assertEquals("5.", calculator.getExpression());
        enter("AC", "5", "=");
        assertFalse(calculator.canUndoEquals());
        enter("⌫");
        assertEquals("0", calculator.getExpression());
    }

    @Test public void editingAfterResultClearsStaleUndoWithoutChangingDeleteBehavior() {
        String[] edits = {"9", ".", "+", "−", "×", "÷", "±", "%", "AC"};
        String[] beforeDelete = {"9", "0.", "5+3+", "5+3−", "5+3×", "5+3÷", "5+−3", "8%", "0"};
        String[] afterDelete = {"0", "0", "5+3", "5+3", "5+3", "5+3", "5+−", "8", "0"};
        for (int index = 0; index < edits.length; index++) {
            enter("AC");
            type("5+3");
            enter("=", edits[index]);
            assertFalse(edits[index], calculator.canUndoEquals());
            assertEquals(edits[index], beforeDelete[index], calculator.getExpression());
            enter("⌫");
            assertEquals(edits[index], afterDelete[index], calculator.getExpression());
        }
    }

    @Test public void memoryChangesPreserveUndoButRecallStartsEditingTheRecalledValue() {
        type("5+3");
        enter("=");
        calculator.memoryAdd();
        calculator.memorySubtract();
        calculator.memoryClear();
        assertTrue(calculator.canUndoEquals());
        calculator.memoryAdd();
        enter("⌫");
        assertEquals("5+3", calculator.getExpression());
        assertEquals("8", calculator.getMemory());
        enter("=");
        calculator.memoryRecall();
        assertFalse(calculator.canUndoEquals());
        enter("⌫");
        assertEquals("0", calculator.getExpression());
    }

    @Test public void errorsNeverUndoAnEarlierSuccessfulCalculation() {
        type("5+3");
        enter("=", "÷", "0", "=");
        assertTrue(calculator.isError());
        assertFalse(calculator.canUndoEquals());
        enter("⌫");
        assertEquals("5+3÷", calculator.getExpression());
        assertFalse(calculator.isError());
        enter("2", "=", "⌫");
        assertEquals("5+3÷2", calculator.getExpression());
        assertEquals("6.5", calculator.getPreview());

        enter("AC");
        type("9999999999999999×9999999999999999");
        for (int index = 0; index < 10 && !calculator.isError(); index++) enter("=");
        assertTrue(calculator.isError());
        assertFalse(calculator.canUndoEquals());
        String failedExpression = calculator.getExpression();
        enter("⌫");
        assertEquals(failedExpression.substring(0, failedExpression.length() - 1),
                calculator.getExpression());
    }

    @Test public void zeroDivisionIsRecoverableByEditingOrNewInput() {
        type("8÷0");
        assertEquals("", calculator.getPreview());
        enter("=");
        assertTrue(calculator.isError());
        assertEquals("Cannot divide by zero", calculator.getErrorMessage());
        enter("⌫", "2", "=");
        assertFalse(calculator.isError());
        assertEquals("4", calculator.getExpression());
        enter("÷", "0", "=", "7");
        assertFalse(calculator.isError());
        assertEquals("7", calculator.getExpression());
    }

    @Test public void malformedKeystrokesNeverCreateMalformedNumbers() {
        enter("×", "0", "0", ".", ".", "5", "+", "×", "2", "=", "garbage");
        assertEquals("1", calculator.getExpression());
        enter("AC", "5", "+", "=");
        assertEquals("5", calculator.getExpression());
        assertFalse(calculator.isError());
        assertEquals("", calculator.getPreview());
    }

    @Test public void memoryUsesCurrentResultAndSurvivesAllClear() {
        type("52+96");
        calculator.memoryAdd();
        assertEquals("148", calculator.getMemory());
        enter("AC", "8");
        calculator.memorySubtract();
        assertEquals("140", calculator.getMemory());
        enter("AC", "2", "×");
        calculator.memoryRecall();
        assertEquals("2×140", calculator.getExpression());
        assertEquals("280", calculator.getPreview());
        calculator.memoryClear();
        assertEquals("0", calculator.getMemory());
    }

    @Test public void memoryRecallReplacesCurrentOperand() {
        type("42");
        calculator.memoryAdd();
        enter("AC");
        type("100−7");
        calculator.memoryRecall();
        assertEquals("100−42", calculator.getExpression());
        assertEquals("58", calculator.getPreview());
    }

    @Test public void persistenceRestoresExpressionMemoryAndIncompleteEntry() {
        calculator.restore("52+96", "−5.25");
        assertEquals("148", calculator.getPreview());
        assertEquals("-5.25", calculator.getMemory());
        calculator.restore("2×−", "7");
        enter("4");
        assertEquals("2×−4", calculator.getExpression());
        assertEquals("−8", calculator.getPreview());
        assertEquals("7", calculator.getMemory());
    }

    @Test public void invalidPersistedStateCannotCrashTheCalculator() {
        calculator.restore("2..3<script>", "NaN");
        assertEquals("0", calculator.getExpression());
        assertEquals("0", calculator.getMemory());
        calculator.restore("1÷0", "0");
        enter("=");
        assertTrue(calculator.isError());
    }

    @Test public void numericInputHasAReasonableDigitLimit() {
        type("123456789012345678901234567890");
        assertEquals("1234567890123456", calculator.getExpression());
    }

    @Test public void valueSupportsLoneNumbersEqualsAndExternalConsumers() {
        assertEquals("0", calculator.getValue());
        enter("5", "±");
        assertEquals("-5", calculator.getValue());
        enter("+", "2");
        assertEquals("-3", calculator.getValue());
        enter("=");
        assertEquals("-3", calculator.getValue());
        enter("+");
        assertEquals("-3", calculator.getValue());
        enter("⌫", "÷", "0", "=");
        assertEquals("", calculator.getValue());
    }

    @Test public void reportedLongSumShowsSubtotalAndEqualsCompletesIt() {
        String sum = "2265+500+1700+1000+15800+12200+2500+19300+3500";
        type(sum + "+");
        assertEquals("58765", calculator.getPreview());
        assertEquals("58765", calculator.getValue());
        assertEquals(sum, calculator.getEquationForEquals());
        assertEquals(sum + "+", calculator.getExpression());
        enter("=");
        assertFalse(calculator.isError());
        assertEquals("58765", calculator.getExpression());
        assertEquals("", calculator.getPreview());
        assertEquals("58765+3500", calculator.getEquationForEquals());
        enter("=");
        assertEquals("62265", calculator.getExpression());
    }

    @Test public void equalsIgnoresEachPendingOperatorAndKeepsCompletedPrecedence() {
        for (String operator : new String[] {"+", "−", "×", "÷"}) {
            enter("AC");
            type("2+3×4" + operator);
            assertEquals(operator, "14", calculator.getPreview());
            assertEquals(operator, "2+3×4", calculator.getEquationForEquals());
            enter("=", "=");
            assertFalse(calculator.isError());
            assertEquals(operator, "26", calculator.getExpression());
        }
    }

    @Test public void subtotalDoesNotCommitEntryBeforeTheNextOperandIsTyped() {
        type("52+96+");
        assertEquals("148", calculator.getPreview());
        assertEquals("148", calculator.getValue());
        assertEquals("52+96", calculator.getEquationForEquals());
        assertEquals("52+96+", calculator.getExpression());
        type("2");
        assertEquals("52+96+2", calculator.getExpression());
        assertEquals("150", calculator.getPreview());
        enter("=");
        assertEquals("150", calculator.getExpression());
    }

    @Test public void pendingOperationAfterPercentKeepsItsArithmeticContext() {
        type("200+10%+");
        assertEquals("220", calculator.getPreview());
        assertEquals("200+10%", calculator.getEquationForEquals());
        enter("=", "=");
        assertEquals("240", calculator.getExpression());
    }

    @Test public void restoredPendingUnarySignCanFinishOrAcceptItsOperand() {
        calculator.restore("5+3×−", "0");
        assertEquals("8", calculator.getPreview());
        assertEquals("8", calculator.getValue());
        assertEquals("5+3", calculator.getEquationForEquals());
        enter("=");
        assertEquals("8", calculator.getExpression());
        calculator.restore("5+3×−", "0");
        enter("2", "=");
        assertEquals("−1", calculator.getExpression());
    }

    @Test public void pendingOperatorNeverHidesDivisionByZeroInCompletedPrefix() {
        type("8÷0+");
        assertEquals("", calculator.getPreview());
        assertEquals("", calculator.getValue());
        assertEquals("8÷0", calculator.getEquationForEquals());
        enter("=");
        assertTrue(calculator.isError());
        assertEquals("Cannot divide by zero", calculator.getErrorMessage());
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(calculator.saveState());
        assertTrue(recreated.isError());
        assertEquals("Cannot divide by zero", recreated.getErrorMessage());
        assertEquals("", recreated.getValue());
    }

    @Test public void pendingSubtotalSurvivesRecreationAndUsesCurrentMemoryValue() {
        type("52+96+");
        calculator.memoryAdd();
        assertEquals("148", calculator.getMemory());
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(calculator.saveState());
        assertEquals("52+96+", recreated.getExpression());
        assertEquals("148", recreated.getPreview());
        recreated.input("=");
        calculator.restoreState(recreated.saveState());
        assertEquals("148", calculator.getExpression());
        assertEquals("148", calculator.getMemory());
        enter("=");
        assertEquals("244", calculator.getExpression());
    }

    @Test public void resultRangeLimitIsReportedWithoutAnUnboundedString() {
        type("9999999999999999×9999999999999999");
        enter("=");
        for (int index = 0; index < 8; index++) enter("=");
        assertTrue(calculator.isError());
        assertEquals("Result is outside the supported range", calculator.getErrorMessage());
    }

    @Test public void recreatedResultStartsANewNumberWhenTyping() {
        type("5+3");
        enter("=");
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(calculator.saveState());
        assertEquals("8", recreated.getExpression());
        assertEquals("", recreated.getPreview());
        recreated.input("9");
        assertEquals("9", recreated.getExpression());
    }

    @Test public void recreatedResultPreservesRepeatedEqualsAcrossMultipleRestarts() {
        type("5+3");
        enter("=");
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(calculator.saveState());
        recreated.input("=");
        assertEquals("11", recreated.getExpression());
        calculator.restoreState(recreated.saveState());
        enter("=");
        assertEquals("14", calculator.getExpression());

        enter("AC");
        type("2+3×4");
        enter("=");
        recreated.restoreState(calculator.saveState());
        recreated.input("=");
        assertEquals("26", recreated.getExpression());
    }

    @Test public void snapshotPreservesUndoThenOrdinaryEditingAcrossRecreation() {
        type("200.00+10%+");
        calculator.memoryAdd();
        enter("=");
        String resultSnapshot = calculator.saveState();
        assertTrue(resultSnapshot.startsWith("coral:2|"));
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(resultSnapshot);
        assertEquals(resultSnapshot, recreated.saveState());
        assertTrue(recreated.canUndoEquals());
        recreated.input("⌫");
        assertEquals("200.00+10%+", recreated.getExpression());
        assertEquals("220", recreated.getPreview());
        assertEquals("220", recreated.getMemory());
        calculator.restoreState(recreated.saveState());
        assertFalse(calculator.canUndoEquals());
        enter("⌫");
        assertEquals("200.00+10%", calculator.getExpression());
        enter("=", "=");
        recreated.restoreState(calculator.saveState());
        recreated.input("⌫");
        assertEquals("220+20", recreated.getExpression());
        assertEquals("240", recreated.getPreview());
    }

    @Test public void legacySnapshotsMigrateWithoutInventingAnUndoEquation() {
        calculator.restoreState("coral:1|148|25|1|+|96|");
        assertEquals("148", calculator.getExpression());
        assertEquals("25", calculator.getMemory());
        assertFalse(calculator.canUndoEquals());
        enter("⌫");
        assertEquals("14", calculator.getExpression());
        calculator.restoreState("coral:1|148|25|1|+|96|");
        enter("=");
        assertEquals("244", calculator.getExpression());
        enter("⌫");
        assertEquals("148+96", calculator.getExpression());

        calculator.restoreState("coral:1|52+96+|25|0|||");
        assertEquals("148", calculator.getPreview());
        enter("=", "⌫");
        assertEquals("52+96+", calculator.getExpression());
        calculator.restoreState("coral:1|8/0|25|0|||Cannot divide by zero");
        assertTrue(calculator.isError());
        assertEquals("25", calculator.getMemory());
        assertFalse(calculator.canUndoEquals());
        enter("⌫");
        assertEquals("8÷", calculator.getExpression());
    }

    @Test public void malformedOrStaleUndoSnapshotsResetAllState() {
        String[] malformed = {
                "coral:2|8|17|1|+|3|",
                "coral:2|8|17|1|+|3||5+3|extra",
                "coral:2|8|17|0|||5+3|5+3",
                "coral:2|8|17|0||||5+3",
                "coral:2|8|17|1|+|3||5+4",
                "coral:2|8|17|1|+|4||5+3",
                "coral:2|8|17|1|-|3||5+3",
                "coral:2|8|17|1|||5+3|5+3",
                "coral:2|8|17|1|||Cannot divide by zero|5+3",
                "coral:2|8|17|1|+|3||5+3<script>",
                "coral:2|8|17|1|+|3||5++3",
                "coral:2|8|17|1|+|3||5,000+3",
                "coral:2|8|17|1|+|3||5+3||",
                "coral:2|8|17|1|+|3||8/0",
                "coral:2|8|17|1|+|3||-",
                "coral:2|8|17|1||||8",
                "coral:2|8|17|1|+|3||" + "1".repeat(257),
                "coral:2|8|17|1|+|3||" + "1" + "0".repeat(101),
                "coral:2|8|17|1|+|3||0." + "0".repeat(100) + "1"
        };
        for (String snapshot : malformed) {
            enter("AC");
            type("5+3");
            enter("=");
            calculator.restoreState(snapshot);
            assertEquals(snapshot, "0", calculator.getExpression());
            assertEquals(snapshot, "0", calculator.getMemory());
            assertFalse(snapshot, calculator.canUndoEquals());
            assertFalse(snapshot, calculator.isError());
        }
    }

    @Test public void equalsEquationExplainsRepeatedOperationInHistory() {
        type("52+96");
        assertEquals("52+96", calculator.getEquationForEquals());
        enter("=");
        assertEquals("148+96", calculator.getEquationForEquals());
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(calculator.saveState());
        assertEquals("148+96", recreated.getEquationForEquals());
        recreated.input("=");
        assertEquals("244", recreated.getExpression());
        enter("AC", "2", "×", "3", "±", "=");
        assertEquals("−6×−3", calculator.getEquationForEquals());
    }

    @Test public void completedEquationDescribesTheCurrentAnswerRatherThanTheNextRepeat() {
        assertFalse(calculator.isEvaluated());
        assertEquals("", calculator.getCompletedEquation());
        type("5645+44646");
        assertFalse(calculator.isEvaluated());
        assertEquals("", calculator.getCompletedEquation());
        enter("=");
        assertTrue(calculator.isEvaluated());
        assertEquals("50291", calculator.getExpression());
        assertEquals("5645+44646", calculator.getCompletedEquation());
        assertEquals("50291+44646", calculator.getEquationForEquals());
        enter("=");
        assertEquals("94937", calculator.getExpression());
        assertEquals("50291+44646", calculator.getCompletedEquation());
        assertEquals("94937+44646", calculator.getEquationForEquals());
    }

    @Test public void completedEquationKeepsPercentContextThenShowsTheResolvedRepeatedOperand() {
        type("200.00+10%");
        enter("=");
        assertEquals("200.00+10%", calculator.getCompletedEquation());
        assertEquals("220", calculator.getExpression());
        enter("=");
        assertEquals("220+20", calculator.getCompletedEquation());
        assertEquals("240", calculator.getExpression());
        enter("AC");
        type("50%");
        enter("=", "=");
        assertTrue(calculator.isEvaluated());
        assertEquals("50%", calculator.getCompletedEquation());
        assertEquals("0.5", calculator.getExpression());
    }

    @Test public void completedEquationOmitsPendingInputWithoutLosingExactUndoText() {
        calculator.restore("−5+3×−2÷", "0");
        enter("=");
        assertEquals("−5+3×−2", calculator.getCompletedEquation());
        assertEquals("−11", calculator.getExpression());
        enter("⌫");
        assertFalse(calculator.isEvaluated());
        assertEquals("", calculator.getCompletedEquation());
        assertEquals("−5+3×−2÷", calculator.getExpression());
        calculator.restore("5+3×−", "0");
        enter("=");
        assertEquals("5+3", calculator.getCompletedEquation());
        enter("⌫");
        assertEquals("5+3×−", calculator.getExpression());
    }

    @Test public void completedEquationSurvivesResultRecreationWithoutChangingItsSnapshot() {
        type("200+10%+");
        enter("=");
        String snapshot = calculator.saveState();
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(snapshot);
        assertTrue(recreated.isEvaluated());
        assertEquals("200+10%", recreated.getCompletedEquation());
        assertEquals(snapshot, recreated.saveState());
        recreated.input("=");
        calculator.restoreState(recreated.saveState());
        assertTrue(calculator.isEvaluated());
        assertEquals("220+20", calculator.getCompletedEquation());
        enter("⌫");
        recreated.restoreState(calculator.saveState());
        assertFalse(recreated.isEvaluated());
        assertEquals("", recreated.getCompletedEquation());
        assertEquals("220+20", recreated.getExpression());
    }

    @Test public void completedEquationIsClearedOnEditingRecallAndErrors() {
        for (String edit : new String[] {"9", ".", "+", "−", "×", "÷", "±", "%", "AC", "⌫"}) {
            enter("AC");
            type("5+3");
            enter("=", edit);
            assertFalse(edit, calculator.isEvaluated());
            assertEquals(edit, "", calculator.getCompletedEquation());
        }
        enter("AC");
        type("5+3");
        enter("=");
        calculator.memoryAdd();
        assertEquals("5+3", calculator.getCompletedEquation());
        calculator.memoryRecall();
        assertFalse(calculator.isEvaluated());
        assertEquals("", calculator.getCompletedEquation());
        enter("÷", "0", "=");
        assertTrue(calculator.isError());
        assertFalse(calculator.isEvaluated());
        assertEquals("", calculator.getCompletedEquation());
        calculator.restoreState(calculator.saveState());
        assertTrue(calculator.isError());
        assertEquals("", calculator.getCompletedEquation());
    }

    @Test public void completedEquationUsesTheCurrentValueForLoneAndLegacyResults() {
        type("500");
        enter("=", "=");
        assertTrue(calculator.isEvaluated());
        assertEquals("500", calculator.getCompletedEquation());
        assertFalse(calculator.canUndoEquals());
        calculator.restoreState("coral:1|148|25|1|+|96|");
        assertTrue(calculator.isEvaluated());
        assertEquals("148", calculator.getCompletedEquation());
        assertEquals("148+96", calculator.getEquationForEquals());
        assertFalse(calculator.canUndoEquals());
        enter("=");
        assertEquals("148+96", calculator.getCompletedEquation());
        assertEquals("244", calculator.getExpression());
        calculator.restoreState("coral:2|-8|0|1||||");
        assertTrue(calculator.isEvaluated());
        assertEquals("−8", calculator.getCompletedEquation());
    }

    @Test public void recreatedEntryKeepsEditingAndPreservesMemory() {
        calculator.restore("2×−", "−5.25");
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(calculator.saveState());
        assertEquals("2×−", recreated.getExpression());
        assertEquals("-5.25", recreated.getMemory());
        recreated.input("3");
        assertEquals("−6", recreated.getPreview());

        enter("AC");
        type("5+3");
        recreated.restoreState(calculator.saveState());
        recreated.input("9");
        assertEquals("5+39", recreated.getExpression());
        assertEquals("44", recreated.getPreview());
    }

    @Test public void recreatedErrorRemainsVisibleAndEditable() {
        type("8÷0");
        enter("=");
        CalculatorEngine recreated = new CalculatorEngine();
        recreated.restoreState(calculator.saveState());
        assertTrue(recreated.isError());
        assertEquals("Cannot divide by zero", recreated.getErrorMessage());
        assertEquals("8÷0", recreated.getExpression());
        assertEquals("", recreated.getValue());
        recreated.input("=");
        assertTrue(recreated.isError());
        recreated.input("⌫");
        recreated.input("2");
        recreated.input("=");
        assertFalse(recreated.isError());
        assertEquals("4", recreated.getExpression());
    }

    @Test public void malformedSnapshotsResetAllStateAndNeverExecuteText() {
        String[] malformed = {
                null, "", "coral:2|8|0|1|+|3|", "coral:1|8|0|1|+|3",
                "coral:1|2..3|0|0|||", "coral:1|8|NaN|1|+|3|",
                "coral:1|8|0|maybe|||", "coral:1|8|0|0|+|3|",
                "coral:1|8|0|1|+||", "coral:1|8|0|1|/|0|",
                "coral:1|8|0|1|||Unknown error", "coral:1|8|0|1|||Cannot divide by zero",
                "coral:1|5+3|0|1|||", "coral:1|8|0|1|+|1E1000000000|",
                "coral:1|8|0|1|+|0.00000000000000001|extra|field"
        };
        for (String snapshot : malformed) {
            calculator.restore("42", "17");
            calculator.restoreState(snapshot);
            assertEquals("0", calculator.getExpression());
            assertEquals("0", calculator.getMemory());
            assertFalse(calculator.isError());
            enter("9");
            assertEquals("9", calculator.getExpression());
        }
    }

    @Test public void snapshotRoundTripPreservesFreshAndRangeErrorStates() {
        String fresh = calculator.saveState();
        calculator.restoreState(fresh);
        assertEquals(fresh, calculator.saveState());
        type("9999999999999999×9999999999999999");
        enter("=");
        for (int index = 0; index < 10 && !calculator.isError(); index++) enter("=");
        assertTrue(calculator.isError());
        String failed = calculator.saveState();
        calculator.restoreState(failed);
        assertTrue(calculator.isError());
        assertEquals(failed, calculator.saveState());
        enter("7");
        assertEquals("7", calculator.getExpression());
    }

    private void typeAt(int position, String text) {
        calculator.setCaret(position);
        type(text);
    }

    private void assertEditing(String expression, int caret) {
        assertEquals(expression, calculator.getExpression());
        assertEquals(caret, calculator.getCaret());
    }

    @Test public void caretStaysAtTheEndUntilItIsMoved() {
        assertEquals(1, calculator.getCaret());
        type("12+34");
        assertEditing("12+34", 5);
        enter("⌫");
        assertEditing("12+3", 4);
        calculator.setCaret(99);
        assertEquals(4, calculator.getCaret());
        calculator.setCaret(-3);
        assertEquals(0, calculator.getCaret());
    }

    @Test public void digitsAndDecimalPointsAreInsertedAtTheCaret() {
        type("52444");
        typeAt(2, "9");
        assertEditing("529444", 3);
        type("0");
        assertEditing("5290444", 4);
        typeAt(0, "7");
        assertEditing("75290444", 1);
        typeAt(3, ".");
        assertEditing("752.90444", 4);
        type(".");
        assertEditing("752.90444", 4);
        assertEquals("", calculator.getPreview());
    }

    @Test public void insertingInsideAnEarlierNumberUpdatesTheAnswer() {
        type("12+34×5");
        typeAt(1, "0");
        assertEditing("102+34×5", 2);
        assertEquals("272", calculator.getPreview());
        enter("=");
        assertEquals("272", calculator.getExpression());
        assertEquals("102+34×5", calculator.getCompletedEquation());
    }

    @Test public void backspaceRemovesTheCharacterBeforeTheCaret() {
        type("1234+56");
        calculator.setCaret(2);
        enter("⌫");
        assertEditing("134+56", 1);
        enter("⌫");
        assertEditing("34+56", 0);
        enter("⌫");
        assertEditing("34+56", 0);
        assertEquals("90", calculator.getPreview());
    }

    @Test public void deletingAnOperatorJoinsItsNumbers() {
        type("12+34");
        calculator.setCaret(3);
        enter("⌫");
        assertEditing("1234", 2);
        enter("AC");
        type("1.5+2.5");
        calculator.setCaret(4);
        enter("⌫");
        assertEditing("1.5+2.5", 4);
        enter("AC");
        type("50%+3");
        calculator.setCaret(4);
        enter("⌫");
        assertEditing("50%+3", 4);
    }

    @Test public void deletingAnOperandsOnlyDigitLeavesAZeroToReplace() {
        type("2+5×3");
        calculator.setCaret(3);
        enter("⌫");
        assertEditing("2+0×3", 3);
        assertEquals("2", calculator.getPreview());
        type("7");
        assertEditing("2+7×3", 3);
        assertEquals("23", calculator.getPreview());
        enter("⌫", "⌫");
        assertEditing("2×3", 1);
    }

    @Test public void deletingTheFirstOperandRemovesItsOperatorToo() {
        type("5+3");
        calculator.setCaret(1);
        enter("⌫");
        assertEditing("0+3", 1);
        enter("⌫");
        assertEditing("3", 0);
        enter("AC");
        type("9");
        enter("±");
        type("+3");
        assertEquals("−9+3", calculator.getExpression());
        calculator.setCaret(2);
        enter("⌫");
        assertEditing("−0+3", 2);
        enter("⌫");
        assertEditing("0+3", 1);
    }

    @Test public void editingKeepsNumbersTidy() {
        type("105+2");
        calculator.setCaret(1);
        enter("⌫");
        assertEditing("5+2", 0);
        enter("AC");
        type("1.5+2");
        calculator.setCaret(1);
        enter("⌫");
        assertEditing("0.5+2", 1);
        type("7");
        assertEditing("7.5+2", 1);
        enter("AC");
        type("2+5");
        typeAt(2, ".");
        assertEditing("2+0.5", 4);
        typeAt(0, "0");
        assertEditing("2+0.5", 0);
    }

    @Test public void operatorsSplitReplaceAndRespectTheirNeighbours() {
        type("1234");
        typeAt(2, "+");
        assertEditing("12+34", 3);
        enter("×");
        assertEditing("12×34", 3);
        calculator.setCaret(2);
        enter("÷");
        assertEditing("12÷34", 3);
        typeAt(0, "×");
        assertEditing("12÷34", 0);
        enter("−");
        assertEditing("−12÷34", 1);
        enter("AC");
        type("50%+3");
        typeAt(2, "+");
        assertEditing("50%+3", 2);
        typeAt(3, "7");
        assertEditing("50%+3", 3);
    }

    @Test public void operatorTypedAfterASignReplacesTheSignedOperator() {
        type("2+5");
        enter("±");
        assertEquals("2+−5", calculator.getExpression());
        typeAt(3, "×");
        assertEditing("2×5", 2);
    }

    @Test public void percentAndSignApplyToTheNumberAtTheCaret() {
        type("200+10×3");
        typeAt(6, "%");
        assertEditing("200+10%×3", 7);
        calculator.setCaret(5);
        enter("±");
        assertEditing("200+−10%×3", 6);
        enter("±");
        assertEditing("200+10%×3", 5);
        calculator.setCaret(1);
        enter("±");
        assertEditing("−200+10%×3", 2);
        calculator.setCaret(8);
        enter("±");
        assertEditing("−200+−10%×3", 9);
        enter("⌫");
        assertEditing("−200+−10×3", 8);
    }

    @Test public void insertedDigitsRespectTheSixteenDigitLimit() {
        type("1234567890123456+1");
        typeAt(3, "9");
        assertEditing("1234567890123456+1", 3);
        calculator.setCaret(17);
        enter("⌫");
        assertEditing("1234567890123456+1", 17);
        typeAt(18, "2");
        assertEditing("1234567890123456+12", 19);
    }

    @Test public void memoryRecallReplacesTheNumberAtTheCaret() {
        type("7");
        calculator.memoryAdd();
        enter("AC");
        type("12+34×5");
        calculator.setCaret(4);
        calculator.memoryRecall();
        assertEditing("12+7×5", 4);
        assertEquals("47", calculator.getPreview());
    }

    @Test public void equalsClearAndCompletedAnswersResetTheCaret() {
        type("12+34");
        calculator.setCaret(1);
        enter("=");
        assertEquals("46", calculator.getExpression());
        calculator.setCaret(0);
        assertEquals(2, calculator.getCaret());
        enter("+");
        assertEditing("12+34+", 6);
        calculator.setCaret(1);
        enter("AC");
        assertEquals(1, calculator.getCaret());
        type("5");
        assertEditing("5", 1);
    }

    @Test public void editingInsideAfterAnErrorKeepsTheExpression() {
        type("8÷0");
        enter("=");
        assertTrue(calculator.isError());
        calculator.setCaret(2);
        enter("⌫");
        assertFalse(calculator.isError());
        assertEditing("80", 1);
        enter("AC");
        type("8÷0+1");
        enter("=");
        assertTrue(calculator.isError());
        typeAt(3, "4");
        assertFalse(calculator.isError());
        assertEditing("8÷4+1", 3);
        assertEquals("3", calculator.getPreview());
    }

    @Test public void everyInsideEditLeavesASnapshotThatRestores() {
        String[] keys = {"1", "0", ".", "+", "−", "×", "÷", "%", "±", "⌫", "7"};
        java.util.Random random = new java.util.Random(7);
        for (int round = 0; round < 400; round++) {
            enter("AC");
            type("12.5+30×4%−7÷2");
            for (int step = 0; step < 25; step++) {
                String before = calculator.getExpression();
                calculator.setCaret(random.nextInt(before.length() + 1));
                enter(keys[random.nextInt(keys.length)]);
                String expression = calculator.getExpression();
                int caret = calculator.getCaret();
                assertTrue(expression + " @" + caret, caret >= 0 && caret <= expression.length());
                String saved = calculator.saveState();
                CalculatorEngine copy = new CalculatorEngine();
                copy.restoreState(saved);
                assertEquals(before + " -> " + expression, saved, copy.saveState());
                assertEquals(expression, copy.getExpression());
            }
        }
    }
}
