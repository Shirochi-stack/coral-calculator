package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Creatures' own health and colours of toughness; what a blow takes off and how it is shown; and the old plus-book as a thing found. */
public class RunTiersTest {
    private static final float STEP = 1 / 60f;
    private static final int FLOOR = RunLevels.FLOOR;

    private static RunLevels.Level flat(int columns) {
        RunLevels.Level level = new RunLevels.Level(1, columns);
        for (int column = 0; column < columns; column++) for (int row = FLOOR; row < RunLevels.ROWS; row++) level.land[row][column] = RunLevels.BLOCK;
        level.gate = columns - 3;
        return level;
    }

    /** How many taps of her first book a minus of a colour takes, at her first level. */
    private static int tapsFor(int tier) {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 6, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        PlusRun run = new PlusRun(level, perks);
        PlusRun.Foe foe = run.foes().get(0);
        foe.tier = tier;
        foe.left = foe.most = PlusRun.healthOf(RunLevels.MINUS) * PlusRun.TIER_HEALTH[tier];
        int taps = 0, before = foe.left;
        for (int step = 0; step < 60 * 60 && !foe.flat; step++) {
            foe.x = 5;
            boolean tap = step % 40 == 0;
            run.step(STEP, false, false, false, tap);
            if (foe.left < before) {
                taps++;
                before = foe.left;
            }
        }
        assertTrue(foe.flat);
        return taps;
    }

    @Test public void aMinusOfEachColourTakesOneThreeSevenFifteenThirtyBlowsOfHerFirstBook() {
        int[] wanted = {1, 3, 7, 15, 30};
        for (int tier = PlusRun.NORMAL; tier < PlusRun.TIERS; tier++) assertEquals(PlusRun.TIER_NAMES[tier], wanted[tier], tapsFor(tier));
    }

    @Test public void everyKindHasItsOwnHealthAndHerLevelMakesBlowsCountForMore() {
        assertEquals(PlusRun.BLOW, PlusRun.healthOf(RunLevels.MINUS));
        assertTrue(PlusRun.healthOf(RunLevels.MITE) < PlusRun.healthOf(RunLevels.MINUS));
        assertTrue(PlusRun.healthOf(RunLevels.DRAGON) > PlusRun.healthOf(RunLevels.SLIME));
        assertTrue(PlusRun.healthOf(RunLevels.BAR) > PlusRun.healthOf(RunLevels.MINUS));
        // At level ten a red minus takes fewer taps than at level one.
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 6, FLOOR - 1});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        perks.level = 30;
        PlusRun run = new PlusRun(level, perks);
        PlusRun.Foe foe = run.foes().get(0);
        foe.left = foe.most = 3 * PlusRun.BLOW;
        int taps = 0, before = foe.left;
        for (int step = 0; step < 60 * 30 && !foe.flat; step++) {
            foe.x = 5;
            run.step(STEP, false, false, false, step % 40 == 0);
            if (foe.left < before) {
                taps++;
                before = foe.left;
            }
        }
        assertTrue(taps < 3);
    }

    @Test public void colourGrowsWithTheLands() {
        int[][] counts = new int[RunLevels.TOTAL + 1][PlusRun.TIERS];
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            PlusRun run = new PlusRun(RunLevels.level(number));
            for (PlusRun.Foe foe : run.foes()) {
                if (RunLevels.boss(foe.kind) || foe.kind == RunLevels.TARGET) continue;
                counts[number][foe.tier]++;
                // Health as its kind and colour make it, and never colour on those that only water or fire deal with.
                if (foe.kind == RunLevels.WISP || foe.kind == RunLevels.RIME) assertEquals(PlusRun.NORMAL, foe.tier);
                assertEquals(foe.most, foe.left);
            }
        }
        int early = 0, earlyRed = 0, purpleBefore7 = 0, goldBefore41 = 0, inkBefore51 = 0, purpleLater = 0, goldLater = 0, inkLater = 0, all = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            int[] c = counts[number];
            if (number <= 6) {
                for (int count : c) early += count;
                earlyRed += c[PlusRun.RED_TIER];
                purpleBefore7 += c[PlusRun.PURPLE_TIER] + c[PlusRun.GOLD_TIER] + c[PlusRun.INK_TIER];
            }
            if (number <= 40) goldBefore41 += c[PlusRun.GOLD_TIER];
            if (number <= 50) inkBefore51 += c[PlusRun.INK_TIER];
            if (number > 80) {
                for (int count : c) all += count;
                purpleLater += c[PlusRun.PURPLE_TIER];
                goldLater += c[PlusRun.GOLD_TIER];
                inkLater += c[PlusRun.INK_TIER];
            }
        }
        // The first lands: almost all plain, the odd red, nothing tougher.
        assertTrue("red " + earlyRed + " of " + early, earlyRed > 0 && earlyRed < early / 8);
        assertEquals(0, purpleBefore7);
        assertEquals(0, goldBefore41);
        assertEquals(0, inkBefore51);
        // The last: every colour, in about the share the table gives.
        assertTrue(purpleLater > all / 20 && goldLater > all / 40 && inkLater > 0);
    }

    @Test public void aBlowIsShownWithItsNumberAndABarOnlyOnThoseThatTakeMore() {
        RunLevels.Level level = flat(60);
        level.foes.add(new int[]{RunLevels.MINUS, 6, FLOOR - 1});
        level.foes.add(new int[]{RunLevels.MINUS, 40, FLOOR - 1, 3});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.main = PlusRun.TABLET;
        PlusRun run = new PlusRun(level, perks);
        // A plain one takes a blow: no bar. One that takes three: a bar.
        assertEquals(PlusRun.BLOW, run.foes().get(0).most);
        assertTrue(run.foes().get(1).most > PlusRun.BLOW);
        PlusRun.Foe foe = run.foes().get(0);
        for (int step = 0; step < 60 && !foe.flat; step++) {
            foe.x = 5;
            run.step(STEP, false, false, false, step == 0);
        }
        assertTrue(foe.flat);
        assertFalse(run.hits().isEmpty());
        assertEquals(PlusRun.BLOW, run.hits().get(0)[2], 0);
        // Numbers go after a moment.
        for (int step = 0; step < 70; step++) run.step(STEP, false, false, false);
        assertTrue(run.hits().isEmpty());
    }

    @Test public void colouredOnesHitHarderAndGiveMore() {
        assertTrue(PlusRun.TIER_BITE[PlusRun.INK_TIER] > PlusRun.TIER_BITE[PlusRun.NORMAL]);
        assertTrue(PlusRun.TIER_XP[PlusRun.GOLD_TIER] > PlusRun.TIER_XP[PlusRun.RED_TIER]);
        assertTrue(PlusRun.TIER_DROPS[PlusRun.INK_TIER] > PlusRun.TIER_DROPS[PlusRun.PURPLE_TIER]);
    }

    @Test public void theOldPlusBookIsFoundAsTheBookOfAddition() {
        int found = 0;
        for (int roll = 0; roll < 10000; roll++) {
            int code = RunLoot.dropped(RunLoot.ORDINARY, roll % 2, roll % 2 == 0 ? RunLoot.WHITE : RunLoot.BLUE, roll % 5000, roll);
            if (!RunLoot.addition(code)) continue;
            found++;
            assertEquals("Book of Addition", RunLoot.name(code));
            assertTrue(RunLoot.rarity(code) <= RunLoot.BLUE);
        }
        assertTrue(found > 0);
        // Never from the later chapters, nor at a better rarity.
        for (int roll = 0; roll < 10000; roll++) {
            assertFalse(RunLoot.addition(RunLoot.dropped(RunLoot.ORDINARY, 5, RunLoot.WHITE, roll % 5000, roll)));
            assertFalse(RunLoot.addition(RunLoot.dropped(RunLoot.ORDINARY, 0, RunLoot.PURPLE, roll % 5000, roll)));
        }
    }
}
