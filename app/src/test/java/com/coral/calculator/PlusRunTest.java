package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlusRunTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;
    private static final float GROUND = FLOOR - PlusRun.TALL;

    /** A land of flat ground, to put things on: the gate three columns from its end. */
    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(1, columns);
        for (int column = 0; column < columns; column++) {
            for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        }
        level.gate = columns - 3;
        return level;
    }

    private static void hole(RunLevels.Level level, int from, int to) {
        for (int column = from; column <= to; column++) {
            for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.AIR;
        }
    }

    private static void hold(PlusRun run, float seconds, boolean left, boolean right, boolean jump) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, left, right, jump);
    }

    // The rules

    @Test public void sheStandsOnTheGroundWhereSheStarts() {
        PlusRun run = new PlusRun(flat(40));
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        assertEquals(GROUND, run.y(), .01f);
        assertEquals(2, run.x(), .01f);
        assertEquals(PlusRun.HEARTS, run.hearts());
    }

    @Test public void sheRunsTheWayTheArrowPoints() {
        PlusRun run = new PlusRun(flat(40));
        hold(run, 1, false, true, false);
        assertTrue(run.x() > 6);
        assertFalse(run.facingLeft());
        hold(run, .5f, true, false, false);
        assertTrue(run.facingLeft());
    }

    @Test public void aWallStopsHer() {
        RunLevels.Level level = flat(40);
        level.land[FLOOR - 1][12] = RunLevels.BLOCK;
        level.land[FLOOR - 2][12] = RunLevels.BLOCK;
        PlusRun run = new PlusRun(level);
        hold(run, 4, false, true, false);
        assertTrue(run.x() <= 12 - PlusRun.WIDE);
        assertTrue(run.x() > 11);
    }

    @Test public void aJumpGoesUpAndComesDownAndLettingGoEarlyJumpsLower() {
        PlusRun run = new PlusRun(flat(40));
        hold(run, .2f, false, false, false);
        float highest = run.y();
        for (int step = 0; step < 30; step++) {
            run.step(STEP, false, false, true);
            highest = Math.min(highest, run.y());
        }
        assertTrue("rose " + (GROUND - highest), GROUND - highest > 2.4f && GROUND - highest < 3.2f);
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        run.step(STEP, false, false, true);
        highest = run.y();
        for (int step = 0; step < 40; step++) {
            run.step(STEP, false, false, false);
            highest = Math.min(highest, run.y());
        }
        assertTrue(GROUND - highest < 1.5f);
    }

    @Test public void aPlusIsCollectedByTouchingIt() {
        RunLevels.Level level = flat(40);
        level.pluses.add(new int[]{5, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, 1, false, true, false);
        assertEquals(1, run.collected());
        assertEquals(1, run.total());
        assertTrue(run.pluses().isEmpty());
    }

    @Test public void fallingIntoAGapCostsAHeartAndAFlagIsWhereSheComesBackTo() {
        RunLevels.Level level = flat(60);
        hole(level, 16, 18);
        hole(level, 40, 42);
        level.flags.add(30);
        PlusRun run = new PlusRun(level);
        run.place(17, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
        assertEquals(2, run.x(), .01f);
        run.place(31, GROUND);
        hold(run, .2f, false, false, false);
        assertEquals(1, run.flagsPassed());
        run.place(41, 6);
        hold(run, 2, false, false, false);
        assertEquals(PlusRun.HEARTS - 2, run.hearts());
        assertEquals(30, (int) run.x());
    }

    @Test public void landingOnAMinusFlattensItAndWalkingIntoOneCostsAHeart() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 20, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.place(19.9f, FLOOR - 6);
        hold(run, 1, false, false, false);
        assertEquals(1, run.flattened());
        assertEquals(PlusRun.HEARTS, run.hearts());

        PlusRun other = new PlusRun(level);
        other.place(17, GROUND);
        for (int step = 0; step < 180 && other.hurts() == 0; step++) other.step(STEP, false, true, false);
        assertEquals(1, other.hurts());
        assertEquals(0, other.flattened());
    }

    @Test public void aTimesCannotBeLandedOn() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.TIMES, 20, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.place(19.9f, FLOOR - 6);
        hold(run, 1, false, false, false);
        assertEquals(0, run.flattened());
        assertEquals(PlusRun.HEARTS - 1, run.hearts());
    }

    @Test public void aDivideHopsOnTheSpotAndCanBeLandedOn() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.DIVIDE, 20, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        float highest = -Float.MAX_VALUE, across = run.foes().get(0).x;
        for (int step = 0; step < 240; step++) {
            run.step(STEP, false, false, false);
            highest = Math.max(highest, FLOOR - run.foes().get(0).y);
        }
        assertEquals(across, run.foes().get(0).x, .001f);
        assertTrue("hopped " + highest, highest > 2 && highest < 3.2f);
        run.place(19.9f, FLOOR - 9);
        hold(run, 1.5f, false, false, false);
        assertEquals(1, run.flattened());
    }

    @Test public void spikesCostAHeartAndASpringThrowsHerHigh() {
        RunLevels.Level level = flat(60);
        level.land[FLOOR - 1][10] = RunLevels.SPIKE;
        level.land[FLOOR - 1][30] = RunLevels.SPRING;
        PlusRun run = new PlusRun(level);
        run.place(7, GROUND);
        for (int step = 0; step < 180 && run.hurts() == 0; step++) run.step(STEP, false, true, false);
        assertEquals(1, run.hurts());

        PlusRun other = new PlusRun(level);
        other.place(28.5f, GROUND);
        float highest = other.y();
        for (int step = 0; step < 120; step++) {
            other.step(STEP, false, true, false);
            highest = Math.min(highest, other.y());
        }
        assertEquals(1, other.springs());
        assertTrue("rose " + (GROUND - highest), GROUND - highest > 5);
    }

    @Test public void zeroTakesThreeLandingsAndTheGateStaysShutUntilThen() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.ZERO, 30, FLOOR - 1});
        level.guarded = true;
        level.gate = 50;
        level.land[FLOOR - 1][25] = RunLevels.BLOCK;
        level.land[FLOOR - 1][36] = RunLevels.BLOCK;
        PlusRun run = new PlusRun(level);
        assertFalse(run.gateOpen());
        // The shut gate stops her.
        run.place(48, GROUND);
        hold(run, 1, false, true, false);
        assertEquals(PlusRun.PLAYING, run.state());
        assertTrue(run.x() + PlusRun.WIDE <= 50);
        for (int blow = 1; blow <= PlusRun.ZERO_HITS; blow++) {
            // Wait for him to shake it off, then come down on him from above.
            hold(run, 1.3f, false, false, false);
            PlusRun.Foe zero = run.foes().get(0);
            run.place(zero.x + .6f, zero.y - PlusRun.TALL - .3f);
            for (int step = 0; step < 120 && run.blows() < blow; step++) run.step(STEP, false, false, false);
            assertEquals(blow, run.blows());
            run.place(48, GROUND);
        }
        assertEquals(PlusRun.HEARTS, run.hearts());
        assertTrue(run.foes().get(0).flat);
        assertTrue(run.gateOpen());
        hold(run, 1, false, true, false);
        assertEquals(PlusRun.WON, run.state());
    }

    @Test public void threeHurtsEndTheRun() {
        RunLevels.Level level = flat(60);
        hole(level, 16, 18);
        PlusRun run = new PlusRun(level);
        for (int fall = 0; fall < PlusRun.HEARTS; fall++) {
            run.place(17, 6);
            hold(run, 2, false, false, false);
        }
        assertEquals(PlusRun.LOST, run.state());
    }

    // What she fights with, and what is thrown at her

    /** Flat ground with something to fight with lying two columns from where she starts. */
    private static RunLevels.Level armed(int kind) {
        RunLevels.Level level = flat(60);
        level.weapons.add(new int[]{kind, 4, FLOOR - 1});
        return level;
    }

    /** Stands her where she is, using what she carries whenever something thrown at her is close in front. */
    private static void fend(PlusRun run, float seconds) {
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) {
            boolean close = false;
            for (PlusRun.Shot shot : run.shots()) {
                if (!shot.hers && shot.x > run.x() && shot.x - run.x() - PlusRun.WIDE < 1.1f) close = true;
            }
            run.step(STEP, false, false, false, close);
        }
    }

    @Test public void aPenIsPickedUpAndThrownAndDealsWithATimes() {
        RunLevels.Level level = armed(PlusRun.PEN);
        level.foes.add(new int[]{RunLevels.TIMES, 12, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        assertEquals(PlusRun.UNARMED, run.weapon());
        run.step(STEP, false, false, false, true);
        assertEquals(0, run.strikes());
        hold(run, .5f, false, true, false);
        assertEquals(PlusRun.PEN, run.weapon());
        assertEquals(1, run.picked());
        assertTrue(run.arms().isEmpty());
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.strikes());
        assertEquals(1, run.shots().size());
        // Holding the button does not throw another: it takes a new press, and a rest between.
        run.step(STEP, false, false, false, true);
        assertEquals(1, run.strikes());
        hold(run, 1, false, false, false);
        assertEquals(1, run.flattened());
        assertTrue(run.shots().isEmpty());
        assertEquals(PlusRun.HEARTS, run.hearts());
    }

    @Test public void aKnifeThrownAtHerCostsAHeartUnlessSheJumpsIt() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.THROWER, 10, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        run.place(6, GROUND);
        float across = run.foes().get(0).x;
        for (int step = 0; step < 240 && run.hurts() == 0; step++) run.step(STEP, false, false, false);
        assertEquals(1, run.hurts());
        assertTrue(run.thrown() >= 1);
        // It stands where it is.
        assertEquals(across, run.foes().get(0).x, .001f);

        PlusRun other = new PlusRun(level);
        other.place(6, GROUND);
        for (int step = 0; step < 400; step++) {
            boolean close = false;
            for (PlusRun.Shot shot : other.shots()) if (shot.x - other.x() - PlusRun.WIDE < .9f) close = true;
            other.step(STEP, false, false, close);
        }
        assertTrue(other.thrown() >= 2);
        assertEquals(0, other.hurts());
    }

    @Test public void aSwordSendsAKnifeBackAndABrushWipesItAway() {
        RunLevels.Level level = armed(PlusRun.SWORD);
        level.foes.add(new int[]{RunLevels.THROWER, 10, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        run.place(6, GROUND);
        fend(run, 4);
        assertTrue(run.turned() >= 1);
        assertEquals(1, run.flattened());
        assertEquals(PlusRun.HEARTS, run.hearts());

        level = armed(PlusRun.BRUSH);
        level.foes.add(new int[]{RunLevels.THROWER, 10, FLOOR - 1});
        PlusRun other = new PlusRun(level);
        hold(other, .5f, false, true, false);
        other.place(6, GROUND);
        fend(other, 6);
        assertTrue(other.turned() >= 2);
        assertEquals(0, other.flattened());
        assertEquals(PlusRun.HEARTS, other.hearts());
    }

    @Test public void aSwingDealsWithWhatIsInFrontOfHerAndABookGoesThroughMoreThanOne() {
        RunLevels.Level level = armed(PlusRun.SWORD);
        level.foes.add(new int[]{RunLevels.TIMES, 9, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        run.place(run.foes().get(0).x - 1.2f - PlusRun.WIDE, GROUND);
        run.step(STEP, false, false, false, true);
        assertTrue(run.swinging() >= 0);
        assertEquals(1, run.flattened());

        level = armed(PlusRun.BOOK);
        // A book is lobbed: it goes over what is close and comes down some six columns on, on two that stand together.
        level.foes.add(new int[]{RunLevels.MINUS, 7, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 11, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.THROWER, 11, FLOOR - 1});
        level.land[FLOOR - 1][6] = RunLevels.BLOCK;
        level.land[FLOOR - 1][9] = RunLevels.BLOCK;
        PlusRun other = new PlusRun(level);
        hold(other, .4f, false, true, false);
        other.place(5, GROUND);
        other.step(STEP, false, false, false, true);
        hold(other, 1, false, false, false);
        assertEquals(2, other.flattened());
        assertFalse(other.foes().get(0).flat);
    }

    @Test public void anInkGunFiresWhileItsButtonIsHeldUntilItIsEmpty() {
        PlusRun run = new PlusRun(armed(PlusRun.GUN));
        hold(run, .5f, false, true, false);
        assertEquals(PlusRun.GUN, run.weapon());
        assertEquals(PlusRun.GUN_SHOTS, run.rounds());
        for (int step = 0; step < 600; step++) run.step(STEP, false, false, false, true);
        assertEquals(PlusRun.GUN_SHOTS, run.strikes());
        assertEquals(PlusRun.UNARMED, run.weapon());
    }

    @Test public void nothingThrownGetsThroughZeroButASwordDoes() {
        RunLevels.Level level = armed(PlusRun.PEN);
        level.foes.add(new int[]{RunLevels.ZERO, 30, FLOOR - 1});
        PlusRun run = new PlusRun(level);
        hold(run, .5f, false, true, false);
        run.place(run.foes().get(0).x - 3, GROUND);
        run.step(STEP, false, false, false, true);
        hold(run, .5f, false, false, false);
        assertEquals(0, run.blows());
        assertTrue(run.shots().isEmpty());

        level = armed(PlusRun.SWORD);
        level.foes.add(new int[]{RunLevels.ZERO, 30, FLOOR - 1});
        PlusRun other = new PlusRun(level);
        hold(other, .5f, false, true, false);
        other.place(other.foes().get(0).x - .6f - PlusRun.WIDE, GROUND);
        other.step(STEP, false, false, false, true);
        assertEquals(1, other.blows());
        assertEquals(PlusRun.HEARTS, other.hearts());
    }

    @Test public void zeroLobsABookAtHerWhenSheKeepsHerDistance() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.ZERO, 30, FLOOR - 1});
        level.land[FLOOR - 1][24] = RunLevels.BLOCK;
        level.land[FLOOR - 1][36] = RunLevels.BLOCK;
        PlusRun run = new PlusRun(level);
        run.place(22, GROUND);
        for (int step = 0; step < 600 && run.hurts() == 0; step++) run.step(STEP, false, false, false);
        assertTrue(run.thrown() >= 1);
        assertEquals(1, run.hurts());
    }

    // Her lines

    @Test public void everyLineOfTheStoryEndsOnHerCatchphraseAndHasANameOfItsOwn() {
        java.util.Set<String> names = new java.util.HashSet<>();
        int lines = 0;
        for (String[][] group : RunStory.all()) {
            for (String[] line : group) {
                lines++;
                assertTrue("a name used twice: " + line[0], names.add(line[0]));
                assertTrue(line[0], line[0].matches("[a-z0-9]+"));
                assertTrue("does not end on Sloppy~♡: " + line[1], line[1].endsWith(" " + Chatter.SLOPPY));
                assertFalse("a comma before her catchphrase: " + line[1], line[1].endsWith(", " + Chatter.SLOPPY));
                assertEquals("more than one heart: " + line[1], line[1].indexOf('♡'), line[1].lastIndexOf('♡'));
            }
        }
        assertEquals(73, lines);
        assertEquals(RunLevels.COUNT, RunStory.LANDS.length);
        assertEquals(RunLevels.CHAPTERS, RunStory.OPENINGS.length);
        assertEquals(RunLevels.CHAPTERS, RunStory.CHAPTERS.length);
        assertEquals(PlusRun.GUN + 1, RunStory.ARMS.length);
    }

    // The thirty lands

    private static final int WALK = 0, WAIT = 1, BACK = 2;
    /** How many steps the jump button is held in each of the jumps the player tries. */
    private static final int[] HOLDS = {1, 4, 8, 13, 22};

    /** Plays a plan out on a run: what the buttons do at each step of it. */
    private static void act(PlusRun run, int plan, int step) {
        if (plan == WALK) run.step(STEP, false, true, false);
        else if (plan == WAIT) run.step(STEP, false, false, false);
        else if (plan == BACK) run.step(STEP, true, false, false);
        else run.step(STEP, false, true, step < HOLDS[plan - 3]);
    }

    private static int length(int plan) { return plan == WALK ? 4 : plan == WAIT ? 12 : plan == BACK ? 14 : 6; }

    /**
     * How a plan would come out: it is tried on a copy of the run and followed until she is
     * back on the ground, and then the best of what she could do next is tried too. A plan
     * that costs a heart is worth nothing; otherwise the further on she is, the better.
     */
    private static float worth(PlusRun run, int plan, int further) {
        PlusRun trial = run.copy();
        int hurts = trial.hurts(), blows = trial.blows();
        int step = 0;
        for (; step < length(plan) && trial.state() == PlusRun.PLAYING; step++) act(trial, plan, step);
        for (; step < 200 && !trial.onGround() && trial.state() == PlusRun.PLAYING; step++) act(trial, plan, step);
        if (trial.hurts() > hurts) return -1000;
        float here = (trial.blows() - blows) * 6 + (trial.state() == PlusRun.WON ? 50 : 0) - (plan == WAIT ? .4f : plan == BACK ? 3 : 0);
        if (further <= 0 || trial.state() != PlusRun.PLAYING) return trial.x() + here;
        float most = -1000;
        for (int next = 0; next < 3 + HOLDS.length; next++) most = Math.max(most, worth(trial, next, further - 1));
        return most + here;
    }
    /**
     * A player with no skill, only foresight: at each moment it tries each thing it might do
     * on a copy of the run, and does the one that comes out furthest on without a hurt. If
     * it can reach the gate, the land can be crossed.
     */
    private static PlusRun cross(RunLevels.Level level, float seconds) {
        PlusRun run = new PlusRun(level);
        float furthest = run.x(), since = 0;
        while (run.state() == PlusRun.PLAYING && run.time() < seconds) {
            // Getting nowhere, it backs off for a run at whatever is in the way.
            if (run.x() > furthest + .05f) {
                furthest = run.x();
                since = run.time();
            } else if (run.time() - since > 1.2f && run.onGround() && worth(run, BACK, 0) > -1000) {
                for (int step = 0; step < 28 && run.state() == PlusRun.PLAYING; step++) run.step(STEP, true, false, false);
                furthest = run.x();
                since = run.time();
            }
            int best = WAIT;
            float most = -Float.MAX_VALUE;
            if (run.onGround()) {
                for (int plan = 0; plan < 3 + HOLDS.length; plan++) {
                    float worth = worth(run, plan, 1);
                    if (worth > most) {
                        most = worth;
                        best = plan;
                    }
                }
            } else {
                best = WALK;
            }
            int step = 0;
            for (; step < length(best) && run.state() == PlusRun.PLAYING; step++) act(run, best, step);
            if (best >= 3) {
                for (; step < 200 && !run.onGround() && run.state() == PlusRun.PLAYING; step++) act(run, best, step);
            }
        }
        return run;
    }

    /** The land round where she is, as text, for a message about where she got stuck. */
    private static String around(PlusRun run) {
        StringBuilder text = new StringBuilder();
        for (int row = FLOOR - 7; row <= FLOOR; row++) {
            text.append('|');
            for (int column = (int) run.x() - 8; column <= (int) run.x() + 14; column++) {
                char here = run.at(column, row);
                for (PlusRun.Foe foe : run.foes()) if (!foe.flat && (int) foe.x == column && (int) (foe.y + foe.tall() - .01f) == row) here = (char) ('0' + foe.kind);
                if (column == (int) run.x() && row == (int) (run.y() + PlusRun.TALL - .01f)) here = '@';
                text.append(here == ' ' ? '.' : here);
            }
        }
        return text.toString();
    }

    /** A land without Zero in it: getting past him is a matter of skill, not of whether the land can be crossed. */
    private static RunLevels.Level unguarded(RunLevels.Level level) {
        for (int index = level.foes.size() - 1; index >= 0; index--) {
            if (level.foes.get(index)[0] == RunLevels.ZERO) level.foes.remove(index);
        }
        level.guarded = false;
        return level;
    }

    @Test public void everyLandCanBeCrossedWithoutLosingAHeart() {
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            PlusRun run = cross(unguarded(RunLevels.level(number)), 240);
            assertEquals("land " + number + " stopped at " + run.x() + " of " + run.columns() + " with " + run.hearts() + " hearts" + around(run),
                    PlusRun.WON, run.state());
            assertEquals("land " + number, PlusRun.HEARTS, run.hearts());
        }
    }

    @Test public void theLandsGrowAndEachHasEnoughInIt() {
        int before = 0;
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            RunLevels.Level level = RunLevels.level(number);
            assertEquals(number, level.number);
            assertEquals((number - 1) / 6, level.chapter);
            assertTrue("land " + number + " has " + level.pluses.size() + " pluses", level.pluses.size() >= 15);
            assertEquals(2, level.flags.size());
            assertEquals(RunLevels.GATE, level.land[FLOOR - 1][level.gate]);
            assertTrue(level.columns > 60 && level.columns < 400);
            // Made from the number kept for it, it is the same land every time.
            assertEquals(level.columns, RunLevels.level(number).columns);
            // Something to fight with by each flag, and from the second land on by where she starts; each lies on the ground.
            assertEquals("land " + number, number == 1 ? 2 : number == RunLevels.COUNT ? 4 : 3, level.weapons.size());
            for (int[] weapon : level.weapons) {
                assertTrue(weapon[0] >= PlusRun.PEN && weapon[0] <= PlusRun.GUN);
                assertEquals("land " + number + " column " + weapon[1], RunLevels.BLOCK, level.land[FLOOR][weapon[1]]);
                assertFalse(PlusRun.solid(level.land[FLOOR - 1][weapon[1]]));
            }
            if (number % 6 == 0 && number < RunLevels.COUNT) {
                assertTrue("land " + number + " is " + level.columns + " long, after " + before, level.columns > before - 30);
                before = level.columns;
            }
        }
        RunLevels.Level last = RunLevels.level(RunLevels.COUNT);
        assertTrue(last.guarded);
        int zeros = 0;
        for (int[] foe : last.foes) if (foe[0] == RunLevels.ZERO) zeros++;
        assertEquals(1, zeros);
    }

    /**
     * Not a check: run with RUN_SEEDS set in the environment to get, for each land, the first number from which
     * a land comes that the player above can cross without a hurt.
     */
    @Test public void findsTheNumbersTheLandsAreMadeFrom() {
        if (System.getenv("RUN_SEEDS") == null) return;
        StringBuilder found = new StringBuilder();
        for (int number = 1; number <= RunLevels.COUNT; number++) {
            long chosen = -1;
            for (long seed = 1; seed <= 400 && chosen < 0; seed++) {
                PlusRun run = cross(unguarded(RunLevels.level(number, seed)), 240);
                if (run.state() == PlusRun.WON && run.hearts() == PlusRun.HEARTS) chosen = seed;
            }
            found.append(chosen).append(number % 15 == 0 ? ",\n" : ", ");
        }
        System.out.println("SEEDS:\n" + found);
        throw new AssertionError("SEEDS:\n" + found);
    }
}
