package com.coral.calculator;

/** Adds display-only thousands separators without changing calculator input or precision. */
public final class NumberFormatter {
    private NumberFormatter() {}

    /**
     * Groups the integer part of each number in an expression. Fractional digits,
     * signs, operators, and incomplete decimal input are kept as entered.
     */
    public static String format(String text) {
        if (text == null || text.isEmpty()) return "";

        StringBuilder result = new StringBuilder(text.length());
        int index = 0;
        while (index < text.length()) {
            char character = text.charAt(index);
            if (!isDigit(character) && character != '.') {
                result.append(character);
                index++;
                continue;
            }

            StringBuilder integer = new StringBuilder();
            while (index < text.length()) {
                character = text.charAt(index);
                if (isDigit(character)) {
                    integer.append(character);
                    index++;
                } else if (character == ',' && integer.length() > 0
                        && index + 1 < text.length() && isDigit(text.charAt(index + 1))) {
                    // Accept an already formatted number without duplicating its separators.
                    index++;
                } else {
                    break;
                }
            }
            for (int digit = 0; digit < integer.length(); digit++) {
                if (digit > 0 && (integer.length() - digit) % 3 == 0) result.append(',');
                result.append(integer.charAt(digit));
            }

            if (index < text.length() && text.charAt(index) == '.') {
                result.append('.');
                index++;
                while (index < text.length() && isDigit(text.charAt(index))) {
                    result.append(text.charAt(index++));
                }
            }

            // Exponents are not integer operands and must never gain grouping commas.
            if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                int exponentEnd = index + 1;
                if (exponentEnd < text.length()
                        && (text.charAt(exponentEnd) == '+' || text.charAt(exponentEnd) == '-')) {
                    exponentEnd++;
                }
                int exponentStart = exponentEnd;
                while (exponentEnd < text.length() && isDigit(text.charAt(exponentEnd))) {
                    exponentEnd++;
                }
                if (exponentEnd > exponentStart) {
                    result.append(text, index, exponentEnd);
                    index = exponentEnd;
                }
            }
        }
        return result.toString();
    }

    /**
     * Where a position in unformatted text falls in its formatted form. A position that
     * precedes a separator stays before it, next to the digit a backspace would remove.
     */
    public static int formattedOffset(String formatted, int rawOffset) {
        int seen = 0;
        for (int index = 0; index < formatted.length(); index++) {
            if (seen >= rawOffset) return index;
            if (formatted.charAt(index) != ',') seen++;
        }
        return formatted.length();
    }

    /** The reverse of {@link #formattedOffset}: separators before the position are not counted. */
    public static int rawOffset(String formatted, int formattedOffset) {
        int count = 0;
        int end = Math.min(formattedOffset, formatted.length());
        for (int index = 0; index < end; index++) {
            if (formatted.charAt(index) != ',') count++;
        }
        return count;
    }

    private static boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }
}
