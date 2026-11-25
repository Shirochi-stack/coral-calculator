package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

public class ChessAndMinesTest {
    /** Squares by name, as written on a board: a8 is 0 and h1 is 63. */
    private static int square(String name) {
        return (8 - (name.charAt(1) - '0')) * 8 + name.charAt(0) - 'a';
    }

    private static void play(Chess game, String... moves) {
        for (String move : moves) {
            int found = game.move(square(move.substring(0, 2)), square(move.substring(2, 4)));
            assertNotEquals("no such move: " + move, Chess.NO_MOVE, found);
            assertTrue(game.play(found));
        }
    }

    // The counts below are the published ones for these positions: they are right only if every rule is.

    @Test public void countsTheWaysFromTheOpeningPosition() {
        Chess game = new Chess();
        assertEquals(20, game.ways(1));
        assertEquals(400, game.ways(2));
        assertEquals(8902, game.ways(3));
        assertEquals(197281, game.ways(4));
    }

    @Test public void countsTheWaysWithCastlingAndPins() {
        Chess game = Chess.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        assertEquals(48, game.ways(1));
        assertEquals(2039, game.ways(2));
        assertEquals(97862, game.ways(3));
    }

    @Test public void countsTheWaysWithCapturesInPassing() {
        Chess game = Chess.of("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");
        assertEquals(14, game.ways(1));
        assertEquals(191, game.ways(2));
        assertEquals(2812, game.ways(3));
        assertEquals(43238, game.ways(4));
    }

    @Test public void countsTheWaysWithPromotions() {
        Chess game = Chess.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
        assertEquals(6, game.ways(1));
        assertEquals(264, game.ways(2));
        assertEquals(9467, game.ways(3));
        Chess other = Chess.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
        assertEquals(44, other.ways(1));
        assertEquals(1486, other.ways(2));
        assertEquals(62379, other.ways(3));
    }

    @Test public void theQuickestCheckmateIsCheckmate() {
        Chess game = new Chess();
        play(game, "f2f3", "e7e5", "g2g4", "d8h4");
        assertEquals(Chess.BLACK_WINS, game.result());
        assertTrue(game.inCheck());
        assertFalse(game.play(game.move(square("a2"), square("a3"))));
    }

    @Test public void noMoveAndNoCheckIsADraw() {
        Chess game = Chess.of("7k/5Q2/6K1/8/8/8/8/8 b - -");
        assertEquals(Chess.DRAW, game.result());
        assertFalse(game.inCheck());
    }

    @Test public void twoKingsAloneAreADraw() {
        assertEquals(Chess.DRAW, Chess.of("8/8/4k3/8/8/3K4/8/8 w - -").result());
        assertEquals(Chess.DRAW, Chess.of("8/8/4k3/8/8/3KN3/8/8 w - -").result());
        assertEquals(Chess.ONGOING, Chess.of("8/8/4k3/8/8/3KR3/8/8 w - -").result());
    }

    @Test public void thePositionComingUpThreeTimesIsADraw() {
        Chess game = new Chess();
        play(game, "g1f3", "g8f6", "f3g1", "f6g8");
        assertEquals(Chess.ONGOING, game.result());
        play(game, "g1f3", "g8f6", "f3g1", "f6g8");
        assertEquals(Chess.DRAW, game.result());
    }

    @Test public void aPawnOnTheLastRowBecomesAQueen() {
        Chess game = Chess.of("8/4P1k1/8/8/8/8/8/4K3 w - -");
        play(game, "e7e8");
        assertEquals(Chess.QUEEN, game.at(square("e8")));
    }

    @Test public void castlingMovesTheRookToo() {
        Chess game = Chess.of("r3k2r/8/8/8/8/8/8/R3K2R w KQkq -");
        play(game, "e1g1", "e8c8");
        assertEquals(Chess.ROOK, game.at(square("f1")));
        assertEquals(Chess.KING, game.at(square("g1")));
        assertEquals(-Chess.ROOK, game.at(square("d8")));
        assertEquals(-Chess.KING, game.at(square("c8")));
        assertEquals(0, game.at(square("h1")));
        assertEquals(0, game.at(square("a8")));
    }

    @Test public void sheTakesAMateInOne() {
        // Black to move: the queen to h4 is checkmate.
        Chess game = new Chess();
        play(game, "f2f3", "e7e5", "g2g4");
        int move = game.choose(new Random(1), 2);
        assertEquals(square("d8"), Chess.from(move));
        assertEquals(square("h4"), Chess.to(move));
    }

    @Test public void sheTakesAQueenLeftHanging() {
        // Black's queen stands on h4, where white's knight on f3 can take it for nothing.
        Chess game = Chess.of("rnb1kbnr/pppp1ppp/8/4p3/4P2q/5N2/PPPP1PPP/RNBQKB1R w KQkq -");
        int move = game.choose(new Random(1), 3);
        assertEquals(square("f3"), Chess.from(move));
        assertEquals(square("h4"), Chess.to(move));
    }

    @Test public void everyMoveSheChoosesIsWithinTheRules() {
        Chess game = new Chess();
        Random random = new Random(7);
        for (int moves = 0; moves < 60 && game.result() == Chess.ONGOING; moves++) {
            int move = game.choose(random, 2);
            assertNotEquals(Chess.NO_MOVE, move);
            assertTrue(game.play(move));
        }
    }

    @Test public void aCopyIsThoughtAboutApart() {
        Chess game = new Chess();
        Chess copy = game.copy();
        play(copy, "e2e4");
        assertEquals(Chess.PAWN, game.at(square("e2")));
        assertEquals(0, copy.at(square("e2")));
    }

    // Minesweeper

    @Test public void theFirstSquareOpenedIsNeverAMineAndOpensRoom() {
        for (int seed = 0; seed < 200; seed++) {
            Mines field = new Mines(9, 12, Mines.fair(9, 12));
            int first = new Random(seed).nextInt(9 * 12);
            int opened = field.open(first, new Random(seed));
            assertEquals(Mines.PLAYING, field.state());
            assertTrue(field.isOpen(first));
            assertEquals(0, field.near(first));
            assertTrue(opened >= 4);
        }
    }

    @Test public void asManyMinesAreLaidAsAsked() {
        Mines field = new Mines(8, 8, 10);
        field.open(0, new Random(3));
        int mines = 0;
        for (int square = 0; square < 64; square++) if (field.isMine(square)) mines++;
        assertEquals(10, mines);
        assertEquals(10, field.left());
    }

    @Test public void openingEverySquareWithoutAMineWins() {
        Mines field = new Mines(8, 8, 10);
        Random random = new Random(5);
        field.open(27, random);
        for (int square = 0; square < 64; square++) if (!field.isMine(square)) field.open(square, random);
        assertEquals(Mines.WON, field.state());
        assertEquals(0, field.left());
    }

    @Test public void openingAMineLoses() {
        Mines field = new Mines(8, 8, 10);
        Random random = new Random(5);
        field.open(27, random);
        int mine = 0;
        while (!field.isMine(mine)) mine++;
        field.open(mine, random);
        assertEquals(Mines.LOST, field.state());
        assertEquals(mine, field.struck());
        assertEquals(0, field.open(0, random));
    }

    @Test public void aFlaggedSquareIsNotOpened() {
        Mines field = new Mines(8, 8, 10);
        Random random = new Random(5);
        field.open(27, random);
        int mine = 0;
        while (!field.isMine(mine)) mine++;
        assertTrue(field.flag(mine));
        assertEquals(9, field.left());
        assertEquals(0, field.open(mine, random));
        assertEquals(Mines.PLAYING, field.state());
        assertTrue(field.flag(mine));
        assertEquals(10, field.left());
        assertFalse(field.flag(27));
    }

    @Test public void anOpenNumberWithItsFlagsOpensTheRest() {
        Mines field = new Mines(8, 8, 10);
        Random random = new Random(5);
        field.open(27, random);
        // Flag every mine, then touch each open number: nothing but safe squares can open.
        for (int square = 0; square < 64; square++) if (field.isMine(square)) field.flag(square);
        for (int pass = 0; pass < 8; pass++) {
            for (int square = 0; square < 64; square++) if (field.isOpen(square)) field.open(square, random);
        }
        assertEquals(Mines.WON, field.state());
    }

    @Test public void aFieldTooSmallForItsMinesStillPlays() {
        Mines field = new Mines(2, 2, 50);
        assertTrue(field.mines >= 1 && field.mines <= 3);
        field.open(0, new Random(1));
        assertTrue(field.isOpen(0));
        assertNotEquals(Mines.LOST, field.state());
    }
}
