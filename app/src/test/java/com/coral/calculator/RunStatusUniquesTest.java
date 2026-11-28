package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The twelve made of fire, frost and ink, or that stun: in the first six chapters' lands, tough, voiced, and doing to her what they are made of. */
public class RunStatusUniquesTest {
    @Test public void twelveTwoForEachOfTheFirstSixChaptersWithTheirPicturesAndLines() {
        assertEquals(12, RunUniques.STATUS_COUNT);
        assertEquals(RunUniques.SHADOW_FIRST, RunUniques.STATUS_FIRST + RunUniques.STATUS_COUNT);
        assertTrue(new java.io.File("../runart/src/main/assets/" + RunUniques.STATUS_SHEET).isFile());
        int fire = 0, cold = 0, ink = 0, stun = 0;
        for (int index = 0; index < 12; index++) {
            int kind = RunUniques.STATUS_FIRST + index;
            assertEquals(index / 2, RunUniques.chapter(kind));
            assertTrue(RunUniques.status(kind));
            int made = RunUniques.resists(kind);
            if (made == PlusRun.RESISTS_FIRE) fire++;
            if (made == PlusRun.RESISTS_COLD) cold++;
            if (made == PlusRun.RESISTS_INK) ink++;
            if (RunUniques.stuns(kind)) stun++;
            assertTrue(PlusRun.defeatOf(kind) != PlusRun.DEFEAT_FADE);
            String[] line = RunStory.STATUS_UNIQUES[index];
            assertTrue(line[1], line[1].endsWith(" Sloppy~♡"));
            assertTrue(new java.io.File("src/main/res/raw/run_v_" + line[0] + ".ogg").isFile());
        }
        assertEquals(3, fire);
        assertEquals(3, cold);
        assertEquals(4, ink);
        assertEquals(2, stun);
    }

    @Test public void theFirstSixChaptersLandsMeetThem() {
        int[] seen = new int[12];
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            for (int[] foe : RunLevels.level(number).foes) if (RunUniques.status(foe[0])) seen[foe[0] - RunUniques.STATUS_FIRST]++;
        }
        for (int index = 0; index < 12; index++) assertTrue(RunUniques.name(RunUniques.STATUS_FIRST + index) + " " + seen[index], seen[index] > 0);
    }

    @Test public void whatTheyThrowAndTheirTouchBurnChillPoisonOrStunHer() {
        RunLevels.Level level = new RunLevels.Level(1, 40);
        for (int c = 0; c < 40; c++) for (int r = 10; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 37;
        level.foes.add(new int[]{RunUniques.STATUS_FIRST, 12, 9});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 40;
        PlusRun run = new PlusRun(level, perks);
        run.place(4, 10 - PlusRun.TALL - .02f);
        // The cinder sprite flicks embers at her: one that lands burns her.
        for (float gone = 0; gone < 8 && run.herBurn() <= 0; gone += 1 / 60f) run.step(1 / 60f, false, false, false, false);
        assertTrue("burnt; hurts " + run.hurts() + " shots " + run.shots().size() + " foe at " + run.foes().get(0).x + " mode " + run.foes().get(0).mode, run.herBurn() > 0);
    }
}