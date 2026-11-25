package com.coral.calculator;

import java.util.Random;

/** Quick sums: a small calculation and four answers to pick the right one from. */
final class Sums {
    static final int CHOICES = 4;

    /** One calculation, with its answer among three that are wrong. */
    static final class Question {
        final String text;
        final String spoken;
        final int answer;
        final int[] choices;

        Question(String text, String spoken, int answer, int[] choices) {
            this.text = text;
            this.spoken = spoken;
            this.answer = answer;
            this.choices = choices;
        }
    }

    private Sums() {}

    /** A calculation done in the head: whole numbers, nothing below zero, division without remainder. */
    static Question next(Random random) {
        int a, b, answer;
        String sign, word;
        switch (random.nextInt(4)) {
            case 0:
                a = 2 + random.nextInt(48);
                b = 2 + random.nextInt(48);
                answer = a + b;
                sign = "+";
                word = "plus";
                break;
            case 1:
                a = 5 + random.nextInt(56);
                b = 2 + random.nextInt(a - 2);
                answer = a - b;
                sign = "−";
                word = "minus";
                break;
            case 2:
                a = 2 + random.nextInt(11);
                b = 2 + random.nextInt(11);
                answer = a * b;
                sign = "×";
                word = "times";
                break;
            default:
                b = 2 + random.nextInt(11);
                answer = 2 + random.nextInt(11);
                a = b * answer;
                sign = "÷";
                word = "divided by";
                break;
        }
        int[] choices = new int[CHOICES];
        choices[0] = answer;
        for (int filled = 1; filled < CHOICES; ) {
            // Close enough to the answer that it has to be worked out, not guessed at.
            int wrong = answer + (random.nextBoolean() ? 1 : -1) * (1 + random.nextInt(10));
            if (wrong < 0 || contains(choices, filled, wrong)) continue;
            choices[filled++] = wrong;
        }
        for (int i = CHOICES - 1; i > 0; i--) {
            int other = random.nextInt(i + 1);
            int choice = choices[i];
            choices[i] = choices[other];
            choices[other] = choice;
        }
        return new Question(a + " " + sign + " " + b, a + " " + word + " " + b, answer, choices);
    }

    private static boolean contains(int[] values, int count, int value) {
        for (int i = 0; i < count; i++) {
            if (values[i] == value) return true;
        }
        return false;
    }
}
