package com.coral.calculator;

import java.math.BigDecimal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberFormatterTest {
    @Test public void groupsThousandsAtEachBoundary() {
        assertEquals("999", NumberFormatter.format("999"));
        assertEquals("1,000", NumberFormatter.format("1000"));
        assertEquals("10,000", NumberFormatter.format("10000"));
        assertEquals("100,000", NumberFormatter.format("100000"));
        assertEquals("1,000,000", NumberFormatter.format("1000000"));
    }

    @Test public void formatsTheRequestedEquationAndResult() {
        assertEquals("5,645+44,646", NumberFormatter.format("5645+44646"));
        assertEquals("50,291", NumberFormatter.format("50291"));
    }

    @Test public void preservesFractionalDigitsAndSigns() {
        assertEquals("−1,234.000056789", NumberFormatter.format("−1234.000056789"));
        assertEquals("-10,000.125000", NumberFormatter.format("-10000.125000"));
        assertEquals("0.12345678901234567890", NumberFormatter.format("0.12345678901234567890"));
    }

    @Test public void retainsEditingStatesAndLeadingZeros() {
        assertEquals("", NumberFormatter.format(""));
        assertEquals("", NumberFormatter.format(null));
        assertEquals("−", NumberFormatter.format("−"));
        assertEquals(".", NumberFormatter.format("."));
        assertEquals(".123456", NumberFormatter.format(".123456"));
        assertEquals("1,234.", NumberFormatter.format("1234."));
        assertEquals("00,001.20+", NumberFormatter.format("00001.20+"));
        assertEquals("1,000+−", NumberFormatter.format("1000+−"));
    }

    @Test public void preservesAllOperatorsAndPercentages() {
        assertEquals("1,000×2,000÷3,000−4,000+5,000%",
                NumberFormatter.format("1000×2000÷3000−4000+5000%"));
        assertEquals("-1,000*2,000/3,000-4,000+5,000%",
                NumberFormatter.format("-1000*2000/3000-4000+5000%"));
        assertEquals("(1,000+2,000)×0.123456",
                NumberFormatter.format("(1000+2000)×0.123456"));
    }

    @Test public void formatsLargePlainDecimalsWithoutLosingPrecision() {
        assertEquals("123,456,789,012,345,678,901,234,567,890.12345678901234567890",
                NumberFormatter.format("123456789012345678901234567890.12345678901234567890"));
        String oneGoogol = new BigDecimal("1E+100").toPlainString();
        String formatted = NumberFormatter.format(oneGoogol);
        assertEquals(oneGoogol, formatted.replace(",", ""));
        assertEquals(134, formatted.length());
        assertEquals("10,000,000", formatted.substring(0, 10));
    }

    @Test public void formattingIsIdempotent() {
        String formatted = "−1,234,567.890123+44,646÷1,000%";
        assertEquals(formatted, NumberFormatter.format(formatted));
        assertEquals(formatted, NumberFormatter.format(NumberFormatter.format(formatted)));
    }

    @Test public void preservesExponentNotationAndNonNumericMessages() {
        assertEquals("1.234567e+1000", NumberFormatter.format("1.234567e+1000"));
        assertEquals("1E-1000", NumberFormatter.format("1E-1000"));
        assertEquals("Cannot divide by zero", NumberFormatter.format("Cannot divide by zero"));
    }
}
