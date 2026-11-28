package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The ground pound, worked on at the counter and in the tree; the glide's steps; and the aerial strike. */
public class RunAirMovesTest {
    private static final int FLOOR = 10;
    private static final float STEP = 1 / 60f;

    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(40, columns);
        for (int c = 0; c < columns; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    private static PlusRun.Perks perks(int pound, int glide, int aerial) {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 40;
        perks.main = PlusRun.SWORD;
        perks.pound = pound > 0;
        perks.poundLevel = pound;
        perks.glide = glide > 0;
        perks.glideLevel = glide;
        perks.aerial = aerial;
        perks.airDash = true;
        return perks;
    }

    private static void hold(PlusRun run, float seconds, boolean jump, boolean strike, boolean down) {
        run.down(down);
        for (float gone = 0; gone < seconds && run.state() == PlusRun.PLAYING; gone += STEP) run.step(STEP, false, false, jump, strike);
        run.down(false);
    }

    /** Up into the air from the floor: a jump held until near its top. */
    private static void jumpUp(PlusRun run) {
        hold(run, .1f, false, false, false);
        hold(run, .35f, true, false, false);
    }

    /** A creature that takes many blows, standing on the floor at a column. */
    private static void tough(RunLevels.Level level, int column) {
        level.foes.add(new int[]{RunLevels.TIMES, column, FLOOR - 1, 40});
    }

    @Test public void heldDownInTheAirSheGathersThenSlamsAndStrikesWhatIsNearWhereSheLands() {
        RunLevels.Level level = flat(40);
        tough(level, 12);
        tough(level, 25);
        PlusRun run = new PlusRun(level, perks(1, 0, 0));
        run.place(10, FLOOR - PlusRun.TALL - .02f);
        jumpUp(run);
        assertFalse(run.onGround());
        run.down(true);
        run.step(STEP, false, false, false, false);
        assertEquals(1, run.poundPhase());
        float high = run.y();
        for (int step = 0; step < 4; step++) run.step(STEP, false, false, false, false);
        // Gathering, she hangs where she is.
        assertEquals(high, run.y(), .02f);
        hold(run, 1, false, false, true);
        assertTrue(run.onGround());
        assertEquals(1, run.pounds());
        assertEquals(0, run.poundPhase());
        PlusRun.Foe near = run.foes().get(0), far = run.foes().get(1);
        assertTrue(near.left < near.most);
        assertEquals(far.most, far.left);
        assertEquals(1, run.poundHits());
        assertTrue(run.popped() >= 1);
    }

    @Test public void itCutsAnAirDashShort() {
        PlusRun run = new PlusRun(flat(40), perks(1, 0, 0));
        run.place(10, FLOOR - PlusRun.TALL - .02f);
        jumpUp(run);
        assertTrue(run.dash());
        run.step(STEP, false, false, false, false);
        assertEquals(PlusRun.MOVE_DASH, run.moveShown());
        run.down(true);
        run.step(STEP, false, false, false, false);
        assertEquals(1, run.poundPhase());
        assertTrue(run.moveShown() != PlusRun.MOVE_DASH);
        hold(run, 1, false, false, true);
        assertEquals(1, run.pounds());
    }

    @Test public void withoutTheMoveHoldingDownOnlyBringsHerDownFaster() {
        PlusRun run = new PlusRun(flat(40), perks(0, 0, 0));
        run.place(10, FLOOR - PlusRun.TALL - .02f);
        jumpUp(run);
        run.down(true);
        run.step(STEP, false, false, false, false);
        assertEquals(0, run.poundPhase());
        hold(run, 1, false, false, true);
        assertEquals(0, run.pounds());
    }

    @Test public void eachStepReachesWiderAndHitsHarderAndTheTreeAddsToIt() {
        float[] reach = new float[4], harder = new float[4];
        for (int level = 1; level <= 3; level++) {
            PlusRun run = new PlusRun(flat(40), perks(level, 0, 0));
            reach[level] = run.poundReach();
            harder[level] = run.poundHarder();
        }
        assertTrue(reach[2] > reach[1] && reach[3] > reach[2]);
        assertTrue(harder[2] > harder[1] && harder[3] > harder[2]);
        PlusRun.Perks treed = perks(1, 0, 0);
        treed.skills.effects[RunSkills.E_POUND_AREA] = 40;
        treed.skills.effects[RunSkills.E_POUND_HIT] = 50;
        PlusRun run = new PlusRun(flat(40), treed);
        assertEquals(reach[1] * 1.4f, run.poundReach(), .01f);
        assertEquals(harder[1] * 1.5f, run.poundHarder(), .01f);
        // One at a distance only the third step reaches is struck by it alone.
        for (int level = 1; level <= 3; level += 2) {
            RunLevels.Level land = flat(40);
            tough(land, 14);
            PlusRun pounding = new PlusRun(land, perks(level, 0, 0));
            pounding.place(10, FLOOR - PlusRun.TALL - .02f);
            jumpUp(pounding);
            hold(pounding, 1.2f, false, false, true);
            PlusRun.Foe foe = pounding.foes().get(0);
            assertEquals("level " + level, level == 3, foe.left < foe.most);
        }
    }

    @Test public void itCracksShieldsByItsStepAndTheThirdBreaksThemAndSendsAShockwave() {
        int[] left = new int[4];
        for (int level = 1; level <= 3; level++) {
            RunLevels.Level land = flat(60);
            tough(land, 11);
            tough(land, 24);
            PlusRun run = new PlusRun(land, perks(level, 0, 0));
            PlusRun.Foe near = run.foes().get(0);
            near.guard = near.guardMost = 4;
            near.guardNeeds = 999;
            run.place(10, FLOOR - PlusRun.TALL - .02f);
            jumpUp(run);
            hold(run, 1.2f, false, false, true);
            left[level] = near.guard;
            hold(run, 2, false, false, false);
            PlusRun.Foe far = run.foes().get(1);
            if (level == 3) {
                assertTrue("the wave reached " + far.x, far.left < far.most);
                assertTrue(run.waveHits() >= 1);
                assertTrue(near.left < near.most);
            } else {
                assertEquals(far.most, far.left);
                // (Its shield still up, the blow does not get through.)
                assertEquals(near.most, near.left);
            }
        }
        assertEquals(3, left[1]);
        assertEquals(2, left[2]);
        assertEquals(0, left[3]);
    }

    @Test public void heldInTheAirTheAerialStrikeHoldsHerUpStrikesRoundHerAndRunsOut() {
        RunLevels.Level level = flat(40);
        PlusRun run = new PlusRun(level, perks(0, 0, 1));
        run.place(10, FLOOR - PlusRun.TALL - .02f);
        jumpUp(run);
        hold(run, .05f, false, true, false);
        assertFalse(run.aerialOn());
        hold(run, .15f, false, true, false);
        assertTrue(run.aerialOn());
        float high = run.y();
        hold(run, .6f, false, true, false);
        assertTrue(run.aerialOn());
        // It sinks her only a little.
        assertTrue("sank " + (run.y() - high), run.y() - high < PlusRun.AERIAL_SINK * .6f + .05f);
        hold(run, 1, false, true, false);
        assertFalse("it runs out", run.aerialOn());
        // Once before she lands, at its first step: held again, nothing.
        hold(run, .1f, false, false, false);
        hold(run, .3f, false, true, false);
        assertFalse(run.aerialOn());
        assertEquals(1, run.aerials());
    }

    @Test public void itStrikesWhatIsRoundHerAndLettingGoEndsIt() {
        RunLevels.Level level = flat(40);
        level.foes.add(new int[]{RunLevels.DRIFTER, 12, FLOOR - 4, 40});
        PlusRun run = new PlusRun(level, perks(0, 0, 3));
        run.place(10, FLOOR - PlusRun.TALL - .02f);
        jumpUp(run);
        PlusRun.Foe foe = run.foes().get(0);
        foe.x = run.x() + 1;
        foe.y = foe.ground = run.y();
        hold(run, .6f, false, true, false);
        assertTrue(run.aerialOn());
        assertTrue(run.aerialHits() >= 2);
        hold(run, STEP * 2, false, false, false);
        assertFalse(run.aerialOn());
        // At the third step, twice before she lands.
        hold(run, .3f, false, true, false);
        assertTrue(run.aerialOn());
        assertEquals(2, run.aerials());
    }

    @Test public void withoutItAHoldInTheAirIsWhatItWasAndOnTheGroundAlways() {
        PlusRun run = new PlusRun(flat(40), perks(0, 0, 0));
        run.place(10, FLOOR - PlusRun.TALL - .02f);
        jumpUp(run);
        hold(run, .5f, false, true, false);
        assertFalse(run.aerialOn());
        PlusRun owned = new PlusRun(flat(40), perks(0, 0, 2));
        owned.place(10, FLOOR - PlusRun.TALL - .02f);
        hold(owned, .1f, false, false, false);
        hold(owned, .5f, false, true, false);
        assertTrue(owned.onGround());
        assertFalse(owned.aerialOn());
    }

    @Test public void theGlideIsSlowerByItsStepsAndTheThirdLiftsHer() {
        float[] fall = new float[4];
        for (int level = 1; level <= 3; level++) fall[level] = new PlusRun(flat(40), perks(0, level, 0)).glideFall();
        assertEquals(PlusRun.GLIDE_FALL, fall[1], .001f);
        assertTrue(fall[2] < fall[1] && fall[3] < fall[2]);
        PlusRun.Perks treed = perks(0, 1, 0);
        treed.skills.effects[RunSkills.E_GLIDE] = 50;
        assertEquals(PlusRun.GLIDE_FALL / 1.5f, new PlusRun(flat(40), treed).glideFall(), .001f);
        // From high up, held: gusts at the third step.
        RunLevels.Level tall = flat(40);
        PlusRun run = new PlusRun(tall, perks(0, 3, 0));
        run.place(10, 1);
        hold(run, 4, true, false, false);
        assertTrue(run.gusts() >= 1);
    }

    @Test public void skyborneAttacksOnlyFromTheAirAndDoublesTheAerialStrike() {
        PlusRun.Perks plain = perks(0, 0, 1), sky = perks(0, 0, 1);
        sky.skills.effects[RunSkills.E_SKYBORNE] = -1;
        sky.skills.effects[RunSkills.E_AERIAL_HIT] = 100;
        assertEquals(new PlusRun(flat(40), plain).aerialShare() * 2, new PlusRun(flat(40), sky).aerialShare(), .001f);
        RunLevels.Level level = flat(40);
        level.foes.add(new int[]{RunLevels.TIMES, 12, FLOOR - 1, 40});
        PlusRun run = new PlusRun(level, sky);
        assertTrue(run.skyborne());
        run.place(10.6f, FLOOR - PlusRun.TALL - .02f);
        for (int step = 0; step < 60; step++) run.step(STEP, false, false, false, step > 5 && step % 10 == 0);
        PlusRun.Foe foe = run.foes().get(0);
        assertEquals(foe.most, foe.left);
    }

    /** In the air the aerial strike has the hold: a hammer held there never lets its special go, and the quill's wave and the haymaker wait for the ground. */
    @Test public void theAerialStrikeTakesTheHoldFromAHeavyThingsSpecial() {
        PlusRun.Perks perks = perks(0, 0, 3);
        perks.main = RunArms.HAMMER;
        PlusRun run = new PlusRun(flat(40), perks);
        run.place(10, FLOOR - PlusRun.TALL - .02f);
        jumpUp(run);
        hold(run, .5f, false, true, false);
        assertTrue(run.aerialOn());
        hold(run, 1.6f, false, true, false);
        assertEquals(0, run.specialsUsed());
        // On the ground the same hold is the special, as ever.
        hold(run, 1, false, false, false);
        assertTrue(run.onGround());
        hold(run, 1, false, true, false);
        assertEquals(1, run.specialsUsed());
    }
}