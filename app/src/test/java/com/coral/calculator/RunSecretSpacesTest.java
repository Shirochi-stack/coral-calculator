package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Secret spaces are secret: until found, a false wall and the room behind it are drawn as the rock round them (the same squares, so the
 * same seams and no turf where none would be) and nothing in them is drawn; a crack hiding a place off the land shows no button. They are
 * found by walking into the false wall, or by a blow; a crack by a blow, or by using it where it is. And a pet that runs climbs after her.
 */
public class RunSecretSpacesTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    /** A land with a cave in it, and its false wall: the eighth, as one play lays it. */
    private static PlusRun cave() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 40;
        return new PlusRun(RunLevels.layered(RunLevels.level(8), 77), perks);
    }

    private static int[] caveSecret(PlusRun run) {
        for (int[] one : run.secrets()) if (one[1] > RunLevels.ROWS) return one;
        throw new AssertionError("no cave secret");
    }

    @Test public void aSecretSpaceIsDrawnAsTheRockRoundItAndNothingInItShows() {
        PlusRun run = cave();
        int[] secret = caveSecret(run);
        int floor = secret[2] + 1;
        for (int column = secret[3]; column <= secret[4]; column++) {
            for (int row = secret[1]; row <= secret[2]; row++) {
                assertTrue(run.hidden(column, row));
                // (Drawn from the same squares as solid ground: what it is shown as is rock.)
                assertEquals(RunLevels.BLOCK, run.shown(column, row));
            }
            // And the ground under it has no turf on it: what is over it is drawn as rock too.
            assertEquals(RunLevels.BLOCK, run.shown(column, floor - 1));
            assertTrue(RunLevels.BLOCK == run.shown(column, floor) || RunLevels.BLOCK == run.at(column, floor));
        }
        // What lies in it is hidden with it: the better chest and its prize.
        int inside = 0;
        for (int[] item : run.items()) if (run.hidden(item[1], item[2])) inside++;
        assertTrue(inside >= 2);
        // Outside it, the cave is what it is.
        int open = secret[0] == secret[3] ? secret[3] - 1 : secret[4] + 1;
        assertEquals(RunLevels.AIR, run.shown(open, secret[2]));
        assertEquals(-1f, run.foundAgo(secret[3], secret[1]), 0);
    }

    @Test public void walkedIntoItsFalseWallFadesAndAShotBreaksItOpen() {
        PlusRun run = cave();
        int[] secret = caveSecret(run);
        boolean right = secret[0] == secret[3];
        int at = run.secrets().indexOf(secret);
        run.place(secret[0] + (right ? -1.6f : 1.6f), secret[2] + 1 - PlusRun.TALL);
        for (int step = 0; step < 60 && !run.secretFound(at); step++) run.step(STEP, !right, right, false);
        assertTrue(run.secretFound(at));
        assertEquals(0, run.secretsBroken());
        assertFalse(run.hidden(secret[3], secret[1]));
        for (int step = 0; step < 40; step++) run.step(STEP, false, false, false);
        assertTrue(run.foundAgo(secret[3], secret[1]) >= PlusRun.SECRET_SHOWS);
        // Another play: a pen thrown at it breaks it open.
        PlusRun thrown = cave();
        thrown.arm(RunArms.PEN);
        thrown.place(secret[0] + (right ? -3.5f : 3.5f), secret[2] + 1 - PlusRun.TALL);
        if (!right) thrown.step(STEP, true, false, false);
        thrown.step(STEP, false, false, false, true);
        for (int step = 0; step < 60; step++) thrown.step(STEP, false, false, false);
        assertTrue(thrown.secretFound(at));
        assertEquals(1, thrown.secretsBroken());
    }

    @Test public void aCrackIsNoDoorUntilABlowBreaksItOpen() {
        RunLevels.Level level = RunLevels.layered(RunLevels.level(8), 77);
        int crack = -1;
        for (int index = 0; index < level.rooms.size() && crack < 0; index++) if (level.rooms.get(index)[7] == 1 && level.rooms.get(index)[0] != RunLevels.HOUSE) crack = index;
        assertTrue(crack >= 0);
        int[] one = level.rooms.get(crack);
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 40;
        PlusRun run = new PlusRun(level, perks);
        run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        // (A use of it there would break through; but no button shows it.)
        assertTrue(run.crackHidden(crack));
        // A blow there, and it is a door.
        run.arm(RunArms.SWORD);
        run.step(STEP, false, false, false, true);
        for (int step = 0; step < 20; step++) run.step(STEP, false, false, false);
        assertTrue((run.revealed() & 1 << crack) != 0);
        run.place(one[1] + (1 - PlusRun.WIDE) / 2, one[2] - PlusRun.TALL);
        run.step(STEP, false, false, false);
        assertEquals(crack, run.door());
        assertFalse(run.crackHidden(crack));
    }

    @Test public void aPetThatRunsClimbsAfterHerAndOneThatFliesFlies() {
        RunLevels.Level level = new RunLevels.Level(10, 40);
        for (int c = 0; c < 40; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        for (int r = FLOOR - 9; r < FLOOR; r++) level.land[r][12] = RunLevels.VINE;
        level.gate = 37;
        PlusRun.Perks two = new PlusRun.Perks();
        two.tempSlots = 2;
        PlusRun run = new PlusRun(level, two);
        run.adopt(PlusRun.CAT);
        run.adopt(PlusRun.JAR);
        run.place(12 + (1 - PlusRun.WIDE) / 2, FLOOR - PlusRun.TALL);
        for (int step = 0; step < 30; step++) run.step(STEP, false, false, false);
        for (int step = 0; step < 50; step++) run.step(STEP, false, false, true);
        assertTrue(run.climbing());
        PlusRun.Pet cat = null, jar = null;
        for (PlusRun.Pet it : run.herd()) {
            if (it.kind == PlusRun.CAT) cat = it;
            if (it.kind == PlusRun.JAR) jar = it;
        }
        assertTrue(cat.climbing);
        assertTrue(Math.abs(cat.x - (run.x() + PlusRun.WIDE / 2)) < .4f);
        assertTrue(cat.y > run.y() + PlusRun.TALL && cat.y < run.y() + PlusRun.TALL + 2.2f);
        assertTrue(cat.climbed > 1);
        assertFalse(jar.climbing);
        // She lets go, and it drops back to following her.
        for (int step = 0; step < 90; step++) run.step(STEP, false, true, false);
        assertFalse(run.climbing());
        assertFalse(cat.climbing);
        assertTrue(new java.io.File("../runart/src/main/assets/runart/pet_climb.webp").isFile());
    }
}
