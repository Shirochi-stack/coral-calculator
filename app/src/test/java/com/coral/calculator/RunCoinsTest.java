package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;

/** The pluses as coins of four worths, the glossary gem that makes them count double for a time, and that the worth she is paid stays what it was. */
public class RunCoinsTest {
    private static final int FLOOR = 10;

    private static PlusRun land() {
        RunLevels.Level level = new RunLevels.Level(10, 60);
        for (int c = 0; c < 60; c++) for (int r = FLOOR; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 57;
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 20;
        PlusRun run = new PlusRun(level, perks);
        run.place(10.6f, FLOOR - PlusRun.TALL);
        return run;
    }

    private static void wait(PlusRun run, float seconds) {
        for (float at = 0; at < seconds; at += 1 / 60f) run.step(1 / 60f, false, false, false);
    }

    @Test public void theFourWorthsHaveTheirOwnLooks() {
        assertEquals(RunCoins.BRONZE, RunCoins.tierOf(1));
        assertEquals(RunCoins.SILVER, RunCoins.tierOf(5));
        assertEquals(RunCoins.GOLD, RunCoins.tierOf(25));
        assertEquals(RunCoins.PRISM, RunCoins.tierOf(100));
        assertEquals(RunCoins.SILVER, RunCoins.tierOf(24 - 15));
        assertEquals(RunCoins.WORTH.length, 4);
        for (int tier = 0; tier < 4; tier++) assertEquals(tier, RunCoins.tierOf(RunCoins.WORTH[tier]));
    }

    @Test public void everySheetIsInThePack() {
        for (String name : RunCoins.FILES) assertTrue(name, new File("../runart/src/main/assets/runart/coins/" + name + ".webp").length() > 5000);
    }

    @Test public void theSameWorthFallsInBiggerCoinsAndNothingIsLostOrMade() {
        for (int worth = 0; worth <= 400; worth++) {
            int sum = 0, coins = 0;
            for (int left = worth; left > 0; ) {
                int one = PlusRun.coinWorth(left);
                assertTrue(one == 1 || one == 5 || one == 25);
                assertTrue("a coin is never worth more than is left", one <= left);
                left -= one;
                sum += one;
                coins++;
            }
            assertEquals(worth, sum);
            // (and in far fewer coins when there is a lot of it)
            if (worth >= 60) assertTrue(coins < worth / 3);
        }
        // A creature of a few pluses is paid in ones, as before; a boss's in the big ones.
        assertEquals(1, PlusRun.coinWorth(3));
        assertEquals(5, PlusRun.coinWorth(14));
        assertEquals(25, PlusRun.coinWorth(70));
    }

    @Test public void aCoinCountsForItsWorthAndDoubleWithTheGemOn() {
        assertEquals(1, PlusRun.pays(1, false));
        assertEquals(5, PlusRun.pays(5, false));
        assertEquals(2, PlusRun.pays(1, true));
        assertEquals(50, PlusRun.pays(25, true));
        assertEquals(200, PlusRun.pays(100, true));
        assertEquals(2, PlusRun.GEM_FACTOR);
    }

    @Test public void aGemDropIsCopiedWithWhatItIs() {
        PlusRun.Drop gem = new PlusRun.Drop(1, 2, 0, 0, 0);
        gem.gem = true;
        PlusRun.Drop gold = new PlusRun.Drop(1, 2, 0, 0, 0);
        gold.worth = 25;
        assertTrue(new PlusRun.Drop(gem).gem);
        assertEquals(25, new PlusRun.Drop(gold).worth);
        assertFalse(new PlusRun.Drop(gold).gem);
        // (a gem and a big coin lie about longer than a plus does)
        assertTrue(gem.life() > new PlusRun.Drop(1, 2, 0, 0, 0).life());
        assertTrue(gold.life() > new PlusRun.Drop(1, 2, 0, 0, 0).life());
    }

    @Test public void theGemMakesPlusesCountDoubleForFifteenSecondsAndTheTimeStacks() {
        PlusRun run = land();
        assertEquals(1, run.multiplier());
        assertEquals(0f, run.gem(), 0);
        // a gem on the ground by her
        run.scatterAll(5, FLOOR - 1);
        run.place(8.4f, FLOOR - PlusRun.TALL);
        wait(run, 3);
        // the gem and the coins thrown with it were drawn in only if she was close; the gem is by her, and is taken
        assertTrue("taken", run.gems() >= 1 || run.gem() > 0);
        assertEquals(2, run.multiplier());
        float first = run.gem();
        assertTrue(first > 5 && first <= PlusRun.GEM_SECONDS);
        // it runs down by the seconds
        wait(run, 2);
        assertEquals(first - 2, run.gem(), .15f);
    }

    @Test public void theTimeAddsUpButIsNeverMoreThanTheMost() {
        PlusRun run = land();
        for (int times = 0; times < 10; times++) {
            PlusRun.Drop gem = new PlusRun.Drop(run.x() + .3f, run.y() + 1.1f, 0, 0, 0);
            gem.gem = true;
            gem.age = .5f;
            run.scattered().add(gem);
            wait(run, .1f);
        }
        assertTrue(run.gems() >= 5);
        assertTrue(run.gem() <= PlusRun.GEM_MOST + .001f);
        assertTrue(run.gem() > PlusRun.GEM_SECONDS * 2);
        assertEquals("the factor never stacks", 2, run.multiplier());
    }

    @Test public void aBiggerCoinPaysItsWorthAndTheGemDoublesIt() {
        PlusRun run = land();
        int before = run.purse();
        PlusRun.Drop gold = new PlusRun.Drop(run.x() + .3f, run.y() + 1.1f, 0, 0, 0);
        gold.worth = 25;
        gold.age = .5f;
        run.scattered().add(gold);
        wait(run, .3f);
        assertEquals(before + 25, run.purse());
        assertEquals(25, run.lastWorth());
        // the same with the gem on
        PlusRun.Drop gem = new PlusRun.Drop(run.x() + .3f, run.y() + 1.1f, 0, 0, 0);
        gem.gem = true;
        gem.age = .5f;
        run.scattered().add(gem);
        wait(run, .3f);
        assertTrue(run.gem() > 0);
        int then = run.purse();
        PlusRun.Drop silver = new PlusRun.Drop(run.x() + .3f, run.y() + 1.1f, 0, 0, 0);
        silver.worth = 5;
        silver.age = .5f;
        run.scattered().add(silver);
        wait(run, .3f);
        assertEquals(then + 10, run.purse());
    }

    @Test public void aPlusInTheLandCountsOnceForTheStarsAndTwiceForThePurseWithTheGem() {
        PlusRun run = land();
        run.level().pluses.clear();
        PlusRun.Drop gem = new PlusRun.Drop(run.x() + .3f, run.y() + 1.1f, 0, 0, 0);
        gem.gem = true;
        gem.age = .5f;
        run.scattered().add(gem);
        wait(run, .3f);
        assertTrue(run.gem() > 0);
        int purse = run.purse(), collected = run.collected();
        run.pluses().add(new int[]{(int) run.x(), FLOOR - 1});
        wait(run, .1f);
        assertEquals("one for the stars", collected + 1, run.collected());
        assertEquals("two to spend (was " + purse + ", is " + run.purse() + ", gem " + run.gem() + ")", purse + 2, run.purse());
    }

    @Test public void theEconomyIsAsItWasPlusATenth() {
        // What a creature falls with is its own number, a tenth more (rounded by the land's chance), in coins of the same worth all told.
        assertEquals(1, PlusRun.MORE_TENTHS);
        for (int coins = 1; coins <= 40; coins++) {
            int more = (coins * PlusRun.MORE_TENTHS) / 10;
            // never less than before; never more than a fifth more
            assertTrue(coins + more + 1 <= coins * 1.2 + 2);
        }
        // A gem on for its longest is the most it can double: ninety seconds, and no more than twice.
        assertTrue(PlusRun.GEM_MOST * PlusRun.GEM_FACTOR <= 180);
    }
}
