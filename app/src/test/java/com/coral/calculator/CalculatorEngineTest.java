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
        type("5+3");
        enter("=", "×", "2", "=");
        assertEquals("16", calculator.getExpression());
        enter("9");
        assertEquals("9", calculator.getExpression());
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
        String[] edits = {"9", ".", "+", "±", "%", "AC"};
        String[] beforeDelete = {"9", "0.", "8+", "−8", "8%", "0"};
        String[] afterDelete = {"0", "0", "8", "−", "8", "0"};
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
        assertEquals("8÷", calculator.getExpression());
        assertFalse(calculator.isError());
        enter("2", "=", "⌫");
        assertEquals("8÷2", calculator.getExpression());
        assertEquals("4", calculator.getPreview());

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
}
