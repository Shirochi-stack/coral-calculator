package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Lasting ribbons at the counter, and the tree's ribbons: each felt only in its colour; Monochrome, one ribbon at a time. */
public class RunRibbonSkillsTest {
    private static final int FLOOR = 10;

    private static RunLevels.Level flat() {
        RunLevels.Level level = new RunLevels.Level(1, 40);
        for (int c = 0; c < 40; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 37;
        return level;
    }

    private static PlusRun run(PlusRun.Perks perks) {
        perks.colours = 0xFF;
        PlusRun run = new PlusRun(flat(), perks);
        run.place(2, FLOOR - PlusRun.TALL - .02f);
        run.step(1 / 60f, false, false, false, false);
        return run;
    }

    @Test public void lastingRibbonsMakeEveryRibbonButBlackLastLonger() {
        PlusRun.Perks plain = new PlusRun.Perks(), lasting = new PlusRun.Perks();
        lasting.ribbonLonger = 1.7f;
        PlusRun one = run(plain), two = run(lasting);
        one.lend(PlusRun.RED);
        two.lend(PlusRun.RED);
        assertEquals(one.on(PlusRun.RED) * 1.7f, two.on(PlusRun.RED), .05f);
        PlusRun three = run(new PlusRun.Perks()), four = run(lasting);
        three.lend(PlusRun.BLACK);
        four.lend(PlusRun.BLACK);
        assertEquals(three.on(PlusRun.BLACK), four.on(PlusRun.BLACK), .01f);
    }

    @Test public void kindledInkBurnsOnlyInRed() {
        for (boolean red : new boolean[]{false, true}) {
            PlusRun.Perks perks = new PlusRun.Perks();
            perks.main = PlusRun.SWORD;
            perks.skills.effects[RunSkills.E_RED] = 100;
            RunLevels.Level level = flat();
            level.foes.add(new int[]{RunLevels.TIMES, 6, FLOOR - 1, 40});
            perks.colours = 0xFF;
            PlusRun run = new PlusRun(level, perks);
            PlusRun.Foe foe = run.foes().get(0);
            if (red) run.lend(PlusRun.RED);
            run.place(foe.x - PlusRun.WIDE - .3f, FLOOR - PlusRun.TALL - .02f);
            for (int step = 0; step < 6; step++) run.step(1 / 60f, false, false, false, step == 0);
            assertEquals("red " + red, red, foe.burning > 0);
        }
    }

    @Test public void bedrockGuardsOnlyInBrownAndMonochromeAllowsOneRibbon() {
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.skills.effects[RunSkills.E_BROWN] = 25;
        perks.together = 3;
        PlusRun run = run(perks);
        int before = run.defencePercent();
        run.lend(PlusRun.BROWN);
        assertTrue(run.defencePercent() > before);
        assertEquals(3, run.together());
        PlusRun.Perks mono = new PlusRun.Perks();
        mono.together = 3;
        mono.skills.effects[RunSkills.E_MONO] = -1;
        assertEquals(1, run(mono).together());
        // Monochrome's notable is a keystone with a cost, and every ribbon notable says which colour it needs.
        RunSkills.Notable keystone = RunSkills.NOTABLES.get(RunSkills.RIBBONS + RunSkills.RIBBON_COUNT - 1);
        assertTrue(keystone.keystone && keystone.costly());
        for (int index = RunSkills.RIBBONS; index < RunSkills.RIBBONS + RunSkills.RIBBON_COUNT - 1; index++) {
            String said = RunSkills.NOTABLES.get(index).said().toLowerCase(java.util.Locale.ROOT);
            assertTrue(said, said.contains("in red") || said.contains("in blue") || said.contains("in brown") || said.contains("in green") || said.contains("in yellow")
                    || said.contains("in purple") || said.contains("in white"));
            assertFalse(said, RunSkills.NOTABLES.get(index).costly());
        }
    }
}