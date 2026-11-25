package com.coral.calculator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

public class PlayGamesTest {
    private static TicTacToe board(String cells) {
        TicTacToe game = new TicTacToe();
        // Turn about, as in play, so that no line is complete before the last mark is down.
        int cross = cells.indexOf('x'), nought = cells.indexOf('o');
        while (cross >= 0 || nought >= 0) {
            if (cross >= 0) {
                assertTrue(game.place(cross, TicTacToe.PLAYER));
                cross = cells.indexOf('x', cross + 1);
            }
            if (nought >= 0) {
                assertTrue(game.place(nought, TicTacToe.HALGAKOS));
                nought = cells.indexOf('o', nought + 1);
            }
        }
        return game;
    }

    @Test public void ticTacToeFindsAWinnerAndItsLine() {
        TicTacToe game = board("xxxoo....");
        assertEquals(TicTacToe.PLAYER, game.result());
        assertArrayEquals(new int[]{0, 1, 2}, game.winningLine());
        assertFalse("no mark goes down once the game is over", game.place(8, TicTacToe.HALGAKOS));
        assertEquals(TicTacToe.NO_CELL, game.choose(new Random(1), 0));
    }

    @Test public void ticTacToeCallsAFullBoardADraw() {
        TicTacToe game = board("xoxxoooxx");
        assertNull(game.winningLine());
        assertEquals(TicTacToe.DRAW, game.result());
    }

    @Test public void ticTacToeRefusesATakenCell() {
        TicTacToe game = new TicTacToe();
        assertTrue(game.place(4, TicTacToe.PLAYER));
        assertFalse(game.place(4, TicTacToe.HALGAKOS));
        assertEquals(TicTacToe.PLAYER, game.at(4));
        assertEquals(TicTacToe.EMPTY, game.result());
    }

    @Test public void halgakosFinishesHerOwnLineBeforeStoppingThePlayers() {
        // Both sides have two in a row: she wins rather than block, even when not attending.
        for (int slip : new int[]{0, 100}) {
            TicTacToe game = board("xx.oo....");
            assertEquals(5, game.choose(new Random(7), slip));
        }
    }

    @Test public void halgakosStopsThePlayerWhenSheIsPayingAttention() {
        for (int seed = 0; seed < 50; seed++) {
            assertEquals(2, board("xx..o....").choose(new Random(seed), 0));
        }
    }

    @Test public void halgakosTakesTheMiddleThenACorner() {
        assertEquals(4, board("x........").choose(new Random(3), 0));
        for (int seed = 0; seed < 50; seed++) {
            int cell = board("....x....").choose(new Random(seed), 0);
            assertTrue("corner expected, got " + cell, cell == 0 || cell == 2 || cell == 6 || cell == 8);
        }
    }

    @Test public void halgakosAlwaysTakesAnEmptyCell() {
        for (int seed = 0; seed < 200; seed++) {
            Random random = new Random(seed);
            TicTacToe game = new TicTacToe();
            int turn = seed % 2 == 0 ? TicTacToe.PLAYER : TicTacToe.HALGAKOS;
            while (game.result() == TicTacToe.EMPTY) {
                int cell = game.choose(random, turn == TicTacToe.PLAYER ? 100 : 30);
                assertNotEquals(TicTacToe.NO_CELL, cell);
                assertTrue(game.place(cell, turn));
                turn = turn == TicTacToe.PLAYER ? TicTacToe.HALGAKOS : TicTacToe.PLAYER;
            }
        }
    }

    @Test public void pairsDealsEachFaceTwice() {
        Pairs pairs = new Pairs(new Random(5));
        int[] count = new int[Pairs.FACES.length];
        for (int card = 0; card < Pairs.CARDS; card++) {
            count[pairs.face(card)]++;
            assertFalse(pairs.isUp(card));
        }
        for (int seen : count) assertEquals(2, seen);
    }

    /** The other card with the same face. */
    private static int partner(Pairs pairs, int card) {
        for (int other = 0; other < Pairs.CARDS; other++) {
            if (other != card && pairs.face(other) == pairs.face(card)) return other;
        }
        throw new AssertionError("no partner for card " + card);
    }

    /** A card with a different face. */
    private static int stranger(Pairs pairs, int card) {
        for (int other = 0; other < Pairs.CARDS; other++) {
            if (pairs.face(other) != pairs.face(card) && !pairs.isMatched(other)) return other;
        }
        throw new AssertionError("no stranger for card " + card);
    }

    @Test public void pairsKeepsAMatchUpAndTurnsAMissDown() {
        Pairs pairs = new Pairs(new Random(9));
        assertEquals(Pairs.FIRST, pairs.flip(0));
        assertEquals("the same card again does nothing", Pairs.NOTHING, pairs.flip(0));
        int wrong = stranger(pairs, 0);
        assertEquals(Pairs.MISS, pairs.flip(wrong));
        assertTrue(pairs.missShowing());
        assertTrue(pairs.isUp(0) && pairs.isUp(wrong));
        assertEquals(1, pairs.moves());
        pairs.settle();
        assertFalse(pairs.isUp(0) || pairs.isUp(wrong));

        assertEquals(Pairs.FIRST, pairs.flip(0));
        assertEquals(Pairs.MATCH, pairs.flip(partner(pairs, 0)));
        assertTrue(pairs.isMatched(0));
        assertEquals(2, pairs.moves());
        assertEquals(1, pairs.found());
        assertEquals("a matched card stays as it is", Pairs.NOTHING, pairs.flip(0));
    }

    @Test public void pairsTurnsAMissDownWhenTheNextCardIsTurned() {
        Pairs pairs = new Pairs(new Random(2));
        pairs.flip(0);
        int wrong = stranger(pairs, 0);
        pairs.flip(wrong);
        int next = 0;
        while (next == 0 || next == wrong) next++;
        assertEquals(Pairs.FIRST, pairs.flip(next));
        assertFalse(pairs.missShowing());
        assertFalse(pairs.isUp(0) || pairs.isUp(wrong));
        assertTrue(pairs.isUp(next));
    }

    @Test public void pairsIsDoneWhenEveryPairIsFound() {
        Pairs pairs = new Pairs(new Random(11));
        for (int card = 0; card < Pairs.CARDS; card++) {
            if (pairs.isMatched(card)) continue;
            assertFalse(pairs.done());
            pairs.flip(card);
            assertEquals(Pairs.MATCH, pairs.flip(partner(pairs, card)));
        }
        assertTrue(pairs.done());
        assertEquals(Pairs.FACES.length, pairs.moves());
    }

    @Test public void sumsAreRightAndOfferTheAnswerOnce() {
        Random random = new Random(42);
        for (int i = 0; i < 2000; i++) {
            Sums.Question question = Sums.next(random);
            String[] parts = question.text.split(" ");
            int a = Integer.parseInt(parts[0]), b = Integer.parseInt(parts[2]);
            int expected;
            switch (parts[1]) {
                case "+": expected = a + b; break;
                case "−": expected = a - b; break;
                case "×": expected = a * b; break;
                case "÷":
                    assertEquals(question.text + " leaves a remainder", 0, a % b);
                    expected = a / b;
                    break;
                default: throw new AssertionError("unknown sign in " + question.text);
            }
            assertEquals(question.text, expected, question.answer);
            assertTrue(question.text + " goes below zero", question.answer >= 0);
            assertEquals(Sums.CHOICES, question.choices.length);
            int right = 0;
            for (int one = 0; one < question.choices.length; one++) {
                assertTrue(question.choices[one] >= 0);
                if (question.choices[one] == question.answer) right++;
                for (int other = 0; other < one; other++) {
                    assertNotEquals(question.text + " repeats a choice", question.choices[one], question.choices[other]);
                }
            }
            assertEquals(1, right);
        }
    }

    @Test public void halgakosHasAHundredLinesOfHerOwn() {
        String[] lines = Chatter.all();
        assertTrue("only " + lines.length + " lines", lines.length >= 160);
        java.util.Set<String> different = new java.util.HashSet<>(java.util.Arrays.asList(lines));
        assertEquals("a line is there twice", lines.length, different.size());
        for (String line : lines) assertFalse(line.trim().isEmpty());
    }

    /** Whether a line ends on her catchphrase, in English or in Japanese. */
    private static boolean endsOnHerCatchphrase(String line) {
        return line.endsWith(" " + Chatter.SLOPPY) || line.endsWith(Chatter.SLOPPY_JAPANESE);
    }

    @Test public void everyLineOfHersEndsOnHerCatchphrase() {
        for (String line : Chatter.all()) {
            assertTrue("does not end on Sloppy~♡: " + line, endsOnHerCatchphrase(line));
            assertFalse("a comma before her catchphrase: " + line, line.endsWith(", " + Chatter.SLOPPY));
            // The heart is the catchphrase's alone: one of them, at the very end.
            assertEquals("a stray heart in: " + line, line.length() - 1, line.indexOf('♡'));
        }
    }

    @Test public void sheHasTheLinesSheWasAskedFor() {
        java.util.List<String> lines = java.util.Arrays.asList(Chatter.all());
        for (String wanted : new String[]{"Watch this Sloppy~♡", "Good Morning Sloppy~♡", "C'mon little Sloppy~♡",
                "Play with me Sloppy~♡"}) {
            assertTrue("missing: " + wanted, lines.contains(wanted));
        }
    }

    @Test public void sheCallsTheUserWhatTheyAsk() {
        assertEquals("Play with me Boss~♡", Chatter.named("Play with me Sloppy~♡", "Boss"));
        assertEquals("So clueless~ My clueless Little One~♡", Chatter.named("So clueless~ My clueless Sloppy~♡", "Little One"));
        assertEquals("the default is left as it is", "Watch this Sloppy~♡", Chatter.named("Watch this Sloppy~♡", "Sloppy"));
        assertEquals("in Japanese too", "よわよわ~ Boss~♡", Chatter.named("よわよわ~ ざぁこ~♡", "Boss"));
        // Every line of hers still ends on the catchphrase, whatever the name.
        for (String line : Chatter.all()) {
            String named = Chatter.named(line, "Boss");
            assertTrue("does not end on Boss~♡: " + named, named.endsWith("Boss~♡"));
            assertFalse("still says Sloppy: " + named, named.contains("Sloppy"));
        }
    }

    @Test public void theNameSheIsGivenIsTidiedUp() {
        assertEquals("Sloppy", Chatter.nickname(null));
        assertEquals("Sloppy", Chatter.nickname("   "));
        assertEquals("Boss", Chatter.nickname("  Boss  "));
        assertEquals("Little One", Chatter.nickname("Little\n   One"));
        assertEquals(20, Chatter.nickname("abcdefghijklmnopqrstuvwxyz").length());
    }

    @Test public void whateverSheSaysWhileIdleEndsOnHerCatchphrase() {
        Random random = new Random(3);
        int output = 0, lines = 6000;
        String last = null;
        for (int i = 0; i < lines; i++) {
            String line = Chatter.idle(random, i % 24, last);
            assertTrue("does not end on Sloppy~♡: " + line, endsOnHerCatchphrase(line));
            assertNotEquals("the same line twice running", last, line);
            if (line.contains("output")) output++;
            last = line;
        }
        // Her missing output turns up now and then, without being all she says.
        assertTrue("her output in " + output + " of " + lines, output > lines * .03 && output < lines * .12);
    }

    @Test public void everyHourOfTheDayHasItsGreeting() {
        Random random = new Random(21);
        for (int hour = 0; hour < 24; hour++) {
            String greeting = hour >= 5 && hour < 12 ? "Good Morning Sloppy~♡" : hour >= 12 && hour < 18 ? "Good Afternoon Sloppy~♡"
                    : hour >= 18 && hour < 23 ? "Good Evening Sloppy~♡" : "Good Night Sloppy~♡";
            boolean greeted = false;
            for (int i = 0; i < 600 && !greeted; i++) greeted = Chatter.idle(random, hour, null).equals(greeting);
            assertTrue("no " + greeting + " at " + hour + " o'clock", greeted);
        }
    }

    @Test public void herCryAboutHerOutputTakesTheLinesPlaceAboutHalfTheTime() {
        Random random = new Random(8);
        int said = 0;
        for (int i = 0; i < 2000; i++) {
            String line = Chatter.orOutput(random, "one", "two");
            assertTrue(line.equals(Chatter.OUTPUT) || line.equals("one") || line.equals("two"));
            if (line.equals(Chatter.OUTPUT)) said++;
        }
        assertTrue("said " + said + " times in 2000", said > 850 && said < 1150);
    }

    @Test public void rockPaperScissorsEachBeatsOneAndLosesToOne() {
        int rock = RockPaperScissors.ROCK, paper = RockPaperScissors.PAPER, scissors = RockPaperScissors.SCISSORS;
        assertEquals(RockPaperScissors.WIN, RockPaperScissors.outcome(rock, scissors));
        assertEquals(RockPaperScissors.WIN, RockPaperScissors.outcome(paper, rock));
        assertEquals(RockPaperScissors.WIN, RockPaperScissors.outcome(scissors, paper));
        assertEquals(RockPaperScissors.LOSS, RockPaperScissors.outcome(scissors, rock));
        assertEquals(RockPaperScissors.LOSS, RockPaperScissors.outcome(rock, paper));
        assertEquals(RockPaperScissors.LOSS, RockPaperScissors.outcome(paper, scissors));
        for (int hand = 0; hand < 3; hand++) {
            assertEquals(RockPaperScissors.TIE, RockPaperScissors.outcome(hand, hand));
            assertNull(RockPaperScissors.reason(hand, hand));
        }
        assertEquals("Paper wraps rock", RockPaperScissors.reason(rock, paper));
        assertEquals("Paper wraps rock", RockPaperScissors.reason(paper, rock));
        assertEquals("Rock blunts scissors", RockPaperScissors.reason(scissors, rock));
        assertEquals("Scissors cut paper", RockPaperScissors.reason(paper, scissors));
    }
}
