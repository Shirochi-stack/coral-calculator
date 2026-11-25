package com.coral.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A game of chess: the rules in full, with castling, capturing in passing, promotion, and
 * every way a game is drawn, and a way of choosing a move for whoever is to play.
 *
 * Squares are numbered across each row from the top of the board as white sees it: 0 is a8,
 * 7 is h8, 56 is a1 and 63 is h1. A piece is a number, {@link #PAWN} to {@link #KING}, above
 * zero for white and below it for black. A move is one number too, read with {@link #from},
 * {@link #to} and {@link #promotion}.
 */
final class Chess {
    static final int PAWN = 1, KNIGHT = 2, BISHOP = 3, ROOK = 4, QUEEN = 5, KING = 6;
    static final int ONGOING = 0, WHITE_WINS = 1, BLACK_WINS = 2, DRAW = 3;
    static final int NO_MOVE = -1;

    /** What kind of move a move is, beyond a piece going from one square to another. */
    private static final int PLAIN = 0, IN_PASSING = 1, CASTLE = 2, TWO_STEPS = 3;
    /** The four rights to castle, each a bit of {@link #castling}. */
    private static final int WHITE_SHORT = 1, WHITE_LONG = 2, BLACK_SHORT = 4, BLACK_LONG = 8;
    /** What each piece is worth, in hundredths of a pawn. */
    private static final int[] WORTH = {0, 100, 320, 330, 500, 900, 0};
    private static final int MATE = 100_000;
    /** How deep the moves in hand can be stacked, and how many moves a position can have. */
    private static final int DEPTH = 64, MOVES = 256;
    /** How many captures deep a line is followed past the end of a search, so no piece is left hanging. */
    private static final int CAPTURES_DEEP = 4;
    /** A move this close to the best one, in hundredths of a pawn, is as likely to be chosen. */
    private static final int AS_GOOD = 12;
    private static final int[][] KNIGHT_STEPS = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
    private static final int[][] KING_STEPS = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    private static final int[][] DIAGONALS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private static final int[][] STRAIGHTS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    /** The rights to castle that are left once a piece has moved from, or to, each square. */
    private static final int[] RIGHTS = new int[64];

    static {
        java.util.Arrays.fill(RIGHTS, 15);
        RIGHTS[63] = 15 & ~WHITE_SHORT;
        RIGHTS[56] = 15 & ~WHITE_LONG;
        RIGHTS[7] = 15 & ~BLACK_SHORT;
        RIGHTS[0] = 15 & ~BLACK_LONG;
    }

    private final int[] board = new int[64];
    private boolean whiteToMove = true;
    private int castling = 15;
    /** The square a pawn has just stepped over, where it can be captured in passing, or -1. */
    private int passed = -1;
    /** Moves since a pawn moved or a piece was taken: fifty by each side draws the game. */
    private int quiet;
    private int lastMove = NO_MOVE;
    private int result = ONGOING;
    /** Every position the game has been through, to tell when one has come up three times. */
    private final List<Long> seen = new ArrayList<>();

    /** What each move in hand changed, so that it can be taken back. */
    private final int[] takenWas = new int[DEPTH], castlingWas = new int[DEPTH], passedWas = new int[DEPTH], quietWas = new int[DEPTH];
    private int inHand;
    private final int[][] lists = new int[DEPTH][MOVES];
    private final int[][] weights = new int[DEPTH][MOVES];

    /** The pieces set out for a new game, white to move. */
    Chess() {
        int[] back = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};
        for (int file = 0; file < 8; file++) {
            board[file] = -back[file];
            board[8 + file] = -PAWN;
            board[48 + file] = PAWN;
            board[56 + file] = back[file];
        }
        seen.add(key());
    }

    private Chess(Chess other) {
        System.arraycopy(other.board, 0, board, 0, 64);
        whiteToMove = other.whiteToMove;
        castling = other.castling;
        passed = other.passed;
        quiet = other.quiet;
        lastMove = other.lastMove;
        result = other.result;
        seen.addAll(other.seen);
    }

    /** This game as it stands, to think about apart from the one on show. */
    Chess copy() { return new Chess(this); }

    /**
     * A position written the way chess positions usually are (Forsyth-Edwards notation): the
     * rows from the top, who is to move, who may still castle, and any square open to a
     * capture in passing.
     */
    static Chess of(String written) {
        Chess game = new Chess();
        java.util.Arrays.fill(game.board, 0);
        String[] parts = written.trim().split("\\s+");
        int square = 0;
        for (char letter : parts[0].toCharArray()) {
            if (letter == '/') continue;
            if (Character.isDigit(letter)) {
                square += letter - '0';
                continue;
            }
            int piece = "pnbrqk".indexOf(Character.toLowerCase(letter)) + 1;
            game.board[square++] = Character.isUpperCase(letter) ? piece : -piece;
        }
        game.whiteToMove = parts.length < 2 || parts[1].equals("w");
        game.castling = 0;
        if (parts.length > 2) {
            if (parts[2].indexOf('K') >= 0) game.castling |= WHITE_SHORT;
            if (parts[2].indexOf('Q') >= 0) game.castling |= WHITE_LONG;
            if (parts[2].indexOf('k') >= 0) game.castling |= BLACK_SHORT;
            if (parts[2].indexOf('q') >= 0) game.castling |= BLACK_LONG;
        }
        game.passed = parts.length > 3 && parts[3].length() == 2 ? (8 - (parts[3].charAt(1) - '0')) * 8 + parts[3].charAt(0) - 'a' : -1;
        game.seen.clear();
        game.seen.add(game.key());
        game.result = game.judge();
        return game;
    }

    // What is on the board

    int at(int square) { return board[square]; }

    boolean whiteToMove() { return whiteToMove; }

    /** The last move made, or {@link #NO_MOVE} before the first. */
    int lastMove() { return lastMove; }

    /** How the game stands: still going, won by a side, or drawn. */
    int result() { return result; }

    /** Whether the side to move is in check. */
    boolean inCheck() { return inCheck(whiteToMove); }

    /** The square the king of the side to move stands on. */
    int king() { return king(whiteToMove); }

    static int from(int move) { return move & 63; }

    static int to(int move) { return move >> 6 & 63; }

    /** The piece a pawn becomes by this move, or 0. */
    static int promotion(int move) { return move >> 12 & 7; }

    private static int kind(int move) { return move >> 15 & 3; }

    private static int move(int from, int to, int promotion, int kind) { return from | to << 6 | promotion << 12 | kind << 15; }

    /** Where the piece on a square may go: the squares it can be moved to within the rules. */
    int[] goals(int square) {
        int[] list = new int[MOVES];
        int count = legal(list), found = 0;
        int[] goals = new int[32];
        for (int index = 0; index < count; index++) {
            if (from(list[index]) != square || (promotion(list[index]) != 0 && promotion(list[index]) != QUEEN)) continue;
            goals[found++] = to(list[index]);
        }
        return java.util.Arrays.copyOf(goals, found);
    }

    /**
     * The move that takes the piece on one square to another, if the rules allow one, or
     * {@link #NO_MOVE}. A pawn that reaches the far row becomes a queen.
     */
    int move(int from, int to) {
        int[] list = new int[MOVES];
        int count = legal(list);
        for (int index = 0; index < count; index++) {
            int move = list[index];
            if (from(move) == from && to(move) == to && (promotion(move) == 0 || promotion(move) == QUEEN)) return move;
        }
        return NO_MOVE;
    }

    /** Makes a move for the side whose turn it is, if the rules allow it; says whether they did. */
    boolean play(int move) {
        if (result != ONGOING) return false;
        int[] list = new int[MOVES];
        int count = legal(list);
        boolean allowed = false;
        for (int index = 0; index < count; index++) allowed |= list[index] == move;
        if (!allowed) return false;
        make(move);
        inHand = 0;
        lastMove = move;
        seen.add(key());
        result = judge();
        return true;
    }

    private int judge() {
        if (legal(new int[MOVES]) == 0) return !inCheck(whiteToMove) ? DRAW : whiteToMove ? BLACK_WINS : WHITE_WINS;
        return quiet >= 100 || repeated() || bare() ? DRAW : ONGOING;
    }

    /** Whether the position on the board has now come up three times. */
    private boolean repeated() {
        long now = key();
        int times = 0;
        for (long before : seen) if (before == now) times++;
        return times >= 3;
    }

    /** Whether too little is left on the board for either side to give checkmate. */
    private boolean bare() {
        int minor = 0;
        for (int piece : board) {
            int sort = Math.abs(piece);
            if (sort == PAWN || sort == ROOK || sort == QUEEN) return false;
            if (sort == KNIGHT || sort == BISHOP) minor++;
        }
        return minor <= 1;
    }

    private long key() {
        long key = 1469598103934665603L;
        for (int piece : board) key = (key ^ (piece + 7)) * 1099511628211L;
        key = (key ^ (whiteToMove ? 1 : 2)) * 1099511628211L;
        key = (key ^ (castling + 16)) * 1099511628211L;
        return (key ^ (passed + 64)) * 1099511628211L;
    }

    // The rules

    /** Every move the side to move may make within the rules. Returns how many there are. */
    private int legal(int[] list) {
        int[] all = lists[inHand];
        int count = generate(all), kept = 0;
        for (int index = 0; index < count; index++) {
            int move = all[index];
            make(move);
            if (!inCheck(!whiteToMove)) list[kept++] = move;
            unmake(move);
        }
        return kept;
    }

    /** Every move the pieces can make, whether or not it leaves their own king in check. */
    private int generate(int[] list) {
        int count = 0, side = whiteToMove ? 1 : -1;
        for (int from = 0; from < 64; from++) {
            int piece = board[from] * side;
            if (piece <= 0) continue;
            switch (piece) {
                case PAWN: {
                    // White's pawns go up the board, toward square 0; black's go down it.
                    int ahead = from - 8 * side, file = from & 7;
                    boolean last = ahead >> 3 == (whiteToMove ? 0 : 7);
                    if (board[ahead] == 0) {
                        count = pawn(list, count, from, ahead, last);
                        if (from >> 3 == (whiteToMove ? 6 : 1) && board[ahead - 8 * side] == 0) {
                            list[count++] = move(from, ahead - 8 * side, 0, TWO_STEPS);
                        }
                    }
                    for (int across = -1; across <= 1; across += 2) {
                        if (file + across < 0 || file + across > 7) continue;
                        int to = ahead + across;
                        if (board[to] * side < 0) count = pawn(list, count, from, to, last);
                        else if (to == passed) list[count++] = move(from, to, 0, IN_PASSING);
                    }
                    break;
                }
                case KNIGHT:
                    count = steps(list, count, from, KNIGHT_STEPS, side);
                    break;
                case BISHOP:
                    count = slides(list, count, from, DIAGONALS, side);
                    break;
                case ROOK:
                    count = slides(list, count, from, STRAIGHTS, side);
                    break;
                case QUEEN:
                    count = slides(list, count, from, DIAGONALS, side);
                    count = slides(list, count, from, STRAIGHTS, side);
                    break;
                default:
                    count = steps(list, count, from, KING_STEPS, side);
                    count = castles(list, count, from);
                    break;
            }
        }
        return count;
    }

    private static int pawn(int[] list, int count, int from, int to, boolean last) {
        if (!last) {
            list[count++] = move(from, to, 0, PLAIN);
            return count;
        }
        for (int piece = QUEEN; piece >= KNIGHT; piece--) list[count++] = move(from, to, piece, PLAIN);
        return count;
    }

    private int steps(int[] list, int count, int from, int[][] steps, int side) {
        int row = from >> 3, file = from & 7;
        for (int[] step : steps) {
            int toRow = row + step[0], toFile = file + step[1];
            if (toRow < 0 || toRow > 7 || toFile < 0 || toFile > 7) continue;
            int to = toRow * 8 + toFile;
            if (board[to] * side <= 0) list[count++] = move(from, to, 0, PLAIN);
        }
        return count;
    }

    private int slides(int[] list, int count, int from, int[][] ways, int side) {
        for (int[] way : ways) {
            int row = (from >> 3) + way[0], file = (from & 7) + way[1];
            while (row >= 0 && row <= 7 && file >= 0 && file <= 7) {
                int to = row * 8 + file;
                if (board[to] * side > 0) break;
                list[count++] = move(from, to, 0, PLAIN);
                if (board[to] != 0) break;
                row += way[0];
                file += way[1];
            }
        }
        return count;
    }

    /**
     * Castling: the king and the rook have not moved, nothing stands between them, and the
     * king is not in check, does not pass through check, and does not end in it. The last
     * of those is seen to with every other move, once the move has been made.
     */
    private int castles(int[] list, int count, int from) {
        int home = whiteToMove ? 60 : 4, rook = whiteToMove ? ROOK : -ROOK;
        int rights = whiteToMove ? castling : castling >> 2;
        if (from != home || (rights & 3) == 0 || attacked(home, !whiteToMove)) return count;
        if ((rights & 1) != 0 && board[home + 1] == 0 && board[home + 2] == 0 && board[home + 3] == rook
                && !attacked(home + 1, !whiteToMove)) {
            list[count++] = move(home, home + 2, 0, CASTLE);
        }
        if ((rights & 2) != 0 && board[home - 1] == 0 && board[home - 2] == 0 && board[home - 3] == 0 && board[home - 4] == rook
                && !attacked(home - 1, !whiteToMove)) {
            list[count++] = move(home, home - 2, 0, CASTLE);
        }
        return count;
    }

    /** Whether a piece of one side could take whatever stood on a square. */
    private boolean attacked(int square, boolean byWhite) {
        int side = byWhite ? 1 : -1, row = square >> 3, file = square & 7;
        // A pawn takes one row ahead of itself: white's from the row below the square, black's from above.
        int pawnRow = row + side;
        if (pawnRow >= 0 && pawnRow <= 7) {
            if (file > 0 && board[pawnRow * 8 + file - 1] == PAWN * side) return true;
            if (file < 7 && board[pawnRow * 8 + file + 1] == PAWN * side) return true;
        }
        for (int[] step : KNIGHT_STEPS) {
            int r = row + step[0], f = file + step[1];
            if (r >= 0 && r <= 7 && f >= 0 && f <= 7 && board[r * 8 + f] == KNIGHT * side) return true;
        }
        for (int[] step : KING_STEPS) {
            int r = row + step[0], f = file + step[1];
            if (r >= 0 && r <= 7 && f >= 0 && f <= 7 && board[r * 8 + f] == KING * side) return true;
        }
        return reached(row, file, DIAGONALS, BISHOP * side, QUEEN * side) || reached(row, file, STRAIGHTS, ROOK * side, QUEEN * side);
    }

    /** Whether the first piece met along any of some lines from a square is one of two. */
    private boolean reached(int row, int file, int[][] ways, int one, int other) {
        for (int[] way : ways) {
            int r = row + way[0], f = file + way[1];
            while (r >= 0 && r <= 7 && f >= 0 && f <= 7) {
                int piece = board[r * 8 + f];
                if (piece != 0) {
                    if (piece == one || piece == other) return true;
                    break;
                }
                r += way[0];
                f += way[1];
            }
        }
        return false;
    }

    private int king(boolean white) {
        int king = white ? KING : -KING;
        for (int square = 0; square < 64; square++) if (board[square] == king) return square;
        return -1;
    }

    private boolean inCheck(boolean white) {
        int king = king(white);
        return king >= 0 && attacked(king, !white);
    }

    private void make(int move) {
        int from = from(move), to = to(move), kind = kind(move), piece = board[from], side = piece > 0 ? 1 : -1;
        int taken = board[to];
        castlingWas[inHand] = castling;
        passedWas[inHand] = passed;
        quietWas[inHand] = quiet;
        if (kind == IN_PASSING) {
            // The pawn taken stands beside the one taking it, a row behind the square it lands on.
            taken = board[to + 8 * side];
            board[to + 8 * side] = 0;
        }
        takenWas[inHand++] = taken;
        board[to] = promotion(move) != 0 ? promotion(move) * side : piece;
        board[from] = 0;
        if (kind == CASTLE) {
            if (to > from) {
                board[to - 1] = board[to + 1];
                board[to + 1] = 0;
            } else {
                board[to + 1] = board[to - 2];
                board[to - 2] = 0;
            }
        }
        if (piece == KING) castling &= ~(WHITE_SHORT | WHITE_LONG);
        else if (piece == -KING) castling &= ~(BLACK_SHORT | BLACK_LONG);
        castling &= RIGHTS[from] & RIGHTS[to];
        passed = kind == TWO_STEPS ? (from + to) / 2 : -1;
        quiet = piece == PAWN || piece == -PAWN || taken != 0 ? 0 : quiet + 1;
        whiteToMove = !whiteToMove;
    }

    private void unmake(int move) {
        int from = from(move), to = to(move), kind = kind(move);
        whiteToMove = !whiteToMove;
        int side = whiteToMove ? 1 : -1, taken = takenWas[--inHand];
        castling = castlingWas[inHand];
        passed = passedWas[inHand];
        quiet = quietWas[inHand];
        board[from] = promotion(move) != 0 ? PAWN * side : board[to];
        board[to] = taken;
        if (kind == IN_PASSING) {
            board[to] = 0;
            board[to + 8 * side] = taken;
        } else if (kind == CASTLE) {
            if (to > from) {
                board[to + 1] = board[to - 1];
                board[to - 1] = 0;
            } else {
                board[to - 2] = board[to + 1];
                board[to + 1] = 0;
            }
        }
    }

    /** How many ways the game can go on for a number of moves: a check on the rules above. */
    long ways(int moves) {
        if (moves == 0) return 1;
        int[] list = lists[inHand];
        int count = generate(list);
        long total = 0;
        for (int index = 0; index < count; index++) {
            make(list[index]);
            if (!inCheck(!whiteToMove)) total += ways(moves - 1);
            unmake(list[index]);
        }
        return total;
    }

    // Choosing a move

    /**
     * A move for the side whose turn it is, found by looking some moves ahead, or
     * {@link #NO_MOVE} if there is none. Where several are about as good, any of them.
     *
     * @param ahead how many moves ahead to look, counting each side's: at least 1
     */
    int choose(Random random, int ahead) {
        int[] list = new int[MOVES];
        int count = legal(list), best = -2 * MATE;
        if (count == 0) return NO_MOVE;
        int[] scores = new int[count];
        for (int index = 0; index < count; index++) {
            make(list[index]);
            scores[index] = -search(ahead - 1, -2 * MATE, 2 * MATE);
            unmake(list[index]);
            best = Math.max(best, scores[index]);
        }
        int good = 0;
        for (int index = 0; index < count; index++) if (scores[index] >= best - AS_GOOD) list[good++] = list[index];
        return list[random.nextInt(good)];
    }

    /** The best the side to move can come out with, seen from its side, between two bounds. */
    private int search(int ahead, int low, int high) {
        if (ahead <= 0) return captures(low, high, CAPTURES_DEEP);
        int[] list = lists[inHand];
        int count = generate(list);
        sort(list, count);
        boolean any = false;
        for (int index = 0; index < count; index++) {
            make(list[index]);
            if (inCheck(!whiteToMove)) {
                unmake(list[index]);
                continue;
            }
            any = true;
            int score = -search(ahead - 1, -high, -low);
            unmake(list[index]);
            if (score > low) {
                low = score;
                if (low >= high) break;
            }
        }
        // No move at all is checkmate, the sooner the worse, or stalemate.
        if (!any) return inCheck(whiteToMove) ? -MATE + inHand : 0;
        return low;
    }

    /** The same, following only captures and promotions, so that a count is not made mid-exchange. */
    private int captures(int low, int high, int deep) {
        int standing = weigh();
        if (standing >= high || deep == 0) return standing;
        if (standing > low) low = standing;
        int[] list = lists[inHand];
        int count = generate(list);
        sort(list, count);
        for (int index = 0; index < count; index++) {
            int move = list[index];
            if (board[to(move)] == 0 && kind(move) != IN_PASSING && promotion(move) == 0) continue;
            make(move);
            if (inCheck(!whiteToMove)) {
                unmake(move);
                continue;
            }
            int score = -captures(-high, -low, deep - 1);
            unmake(move);
            if (score > low) {
                low = score;
                if (low >= high) break;
            }
        }
        return low;
    }

    /** Puts the likeliest moves first: taking the most with the least, and promoting. */
    private void sort(int[] list, int count) {
        int[] weight = weights[inHand];
        for (int index = 0; index < count; index++) {
            int move = list[index], taken = Math.abs(board[to(move)]);
            weight[index] = (taken == 0 ? 0 : 10 * WORTH[taken] - WORTH[Math.abs(board[from(move)])] / 10 + 1000)
                    + (promotion(move) == QUEEN ? 900 : 0);
        }
        for (int index = 1; index < count; index++) {
            int move = list[index], heavy = weight[index], place = index - 1;
            while (place >= 0 && weight[place] < heavy) {
                list[place + 1] = list[place];
                weight[place + 1] = weight[place];
                place--;
            }
            list[place + 1] = move;
            weight[place + 1] = heavy;
        }
    }

    /**
     * How the position looks to the side to move, in hundredths of a pawn: what each side
     * has, and how well it is placed. Knights and bishops want the middle, pawns want to
     * advance, and a king wants a corner until the board empties and then the middle.
     */
    private int weigh() {
        int pieces = 0;
        for (int piece : board) {
            int sort = Math.abs(piece);
            if (sort > PAWN && sort < KING) pieces += WORTH[sort];
        }
        boolean ending = pieces <= 2600;
        int score = 0;
        for (int square = 0; square < 64; square++) {
            int piece = board[square];
            if (piece == 0) continue;
            int row = square >> 3, file = square & 7;
            // How far the piece is from its own back row, and how near the middle of the board.
            int forward = piece > 0 ? 7 - row : row;
            int middle = Math.min(file, 7 - file) + Math.min(row, 7 - row);
            int sort = Math.abs(piece), worth = WORTH[sort];
            switch (sort) {
                case PAWN:
                    worth += (forward - 1) * 6 + ((file == 3 || file == 4) && forward >= 3 ? 12 : 0);
                    break;
                case KNIGHT:
                    worth += middle * 8 - 16;
                    break;
                case BISHOP:
                    worth += middle * 4 - 6;
                    break;
                case ROOK:
                    worth += forward == 6 ? 15 : 0;
                    break;
                case QUEEN:
                    worth += middle * 2;
                    break;
                default:
                    worth += ending ? middle * 10 : forward == 0 ? (file <= 2 || file >= 6 ? 30 : 0) : -25 * forward;
                    break;
            }
            score += piece > 0 ? worth : -worth;
        }
        return whiteToMove ? score : -score;
    }
}
