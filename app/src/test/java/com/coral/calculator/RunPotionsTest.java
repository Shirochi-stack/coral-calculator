package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Potions: drinking to the end before anything comes of it, stopped drinks, the shared heal cooldown, buffs' own, the boss rule, the pouch, and the merchant's permanent ones. */
public class RunPotionsTest {
    private static final float STEP = 1 / 60f;

    private static PlusRun flat() {
        RunLevels.Level flat = new RunLevels.Level(40, 60);
        for (int column = 0; column < 60; column++) for (int row = RunLevels.FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 57;
        PlusRun run = new PlusRun(flat, new PlusRun.Perks());
        run.place(10, RunLevels.FLOOR - PlusRun.TALL);
        run.step(STEP, false, false, false);
        return run;
    }

    private static void wait(PlusRun run, float seconds, boolean jump, boolean strike) {
        for (float gone = 0; gone < seconds; gone += STEP) run.step(STEP, false, false, jump, strike);
    }

    @Test public void nothingComesOfAPotionUntilItIsDrunkToTheEnd() {
        PlusRun run = flat();
        assertTrue(run.drink(RunPotions.MIGHT));
        assertEquals(RunPotions.MIGHT, run.drinking());
        wait(run, RunPotions.drinkSeconds(RunPotions.MIGHT) * .8f, false, false);
        assertEquals(0, run.potionOn(RunPotions.MIGHT), 1e-6f);
        assertEquals(0, run.potionWait(RunPotions.MIGHT), 1e-6f);
        assertTrue(run.drinkThrough() > .7f);
        wait(run, RunPotions.drinkSeconds(RunPotions.MIGHT) * .4f, false, false);
        assertEquals(-1, run.drinking());
        assertEquals(1, run.potionsDrunk());
        assertEquals(RunPotions.MIGHT, run.lastPotion());
        assertTrue(run.potionOn(RunPotions.MIGHT) > RunPotions.lasts(RunPotions.MIGHT) - 1);
    }

    @Test public void whileDrinkingSheCannotStrikeAndAJumpSpillsItWithNothingComingOfIt() {
        PlusRun run = flat();
        assertTrue(run.drink(RunPotions.GUARD));
        int swings = run.swings(), strikes = run.strikes(), thrown = run.thrown();
        wait(run, .3f, false, true);
        assertEquals(swings, run.swings());
        assertEquals(strikes, run.strikes());
        assertEquals(thrown, run.thrown());
        assertEquals(RunPotions.GUARD, run.drinking());
        // A jump stops it: nothing comes of it, and it is ready again soon, not after its whole cooldown.
        wait(run, STEP, true, false);
        assertEquals(-1, run.drinking());
        assertEquals(1, run.drinksStopped());
        assertEquals(0, run.potionsDrunk());
        assertEquals(0, run.potionOn(RunPotions.GUARD), 1e-6f);
        assertTrue(run.potionWait(RunPotions.GUARD) <= RunPotions.INTERRUPTED_COOL);
        assertTrue(run.potionWait(RunPotions.GUARD) > 0);
        // And a swap stops a drink too.
        PlusRun other = flat();
        other.drink(RunPotions.SWIFT);
        other.stopDrink();
        assertEquals(1, other.drinksStopped());
    }

    @Test public void healsShareOneCooldownLongerForABiggerHeal() {
        PlusRun run = flat();
        assertTrue(run.drink(RunPotions.HEAL_S));
        wait(run, RunPotions.drinkSeconds(RunPotions.HEAL_S) + .1f, false, false);
        float wait = run.potionWait(RunPotions.HEAL_S);
        assertTrue(wait > RunPotions.cooldown(RunPotions.HEAL_S) - 1);
        // Every heal waits on it, whatever its size.
        assertEquals(wait, run.potionWait(RunPotions.HEAL_L), 1e-4f);
        assertEquals(wait, run.potionWait(RunPotions.REGEN), 1e-4f);
        assertFalse(run.drink(RunPotions.HEAL_M));
        assertTrue(run.cannotDrink(RunPotions.HEAL_L) != null);
        // A buff does not.
        assertNull(run.cannotDrink(RunPotions.SWIFT));
        assertTrue(RunPotions.cooldown(RunPotions.HEAL_L) > RunPotions.cooldown(RunPotions.HEAL_M) && RunPotions.cooldown(RunPotions.HEAL_M) > RunPotions.cooldown(RunPotions.HEAL_S));
        assertTrue(RunPotions.cooldown(RunPotions.HEAL_S) >= 12 && RunPotions.cooldown(RunPotions.HEAL_L) <= 20);
        wait(run, wait + .1f, false, false);
        assertTrue(run.drink(RunPotions.HEAL_M));
    }

    @Test public void eachBuffHasItsOwnTimerAndCannotBeDrunkAgainWhileItLasts() {
        PlusRun run = flat();
        run.drink(RunPotions.MIGHT);
        wait(run, 1.2f, false, false);
        assertFalse(run.drink(RunPotions.MIGHT));
        assertTrue(run.drink(RunPotions.SWIFT));
        wait(run, 1.2f, false, false);
        assertTrue(run.potionOn(RunPotions.MIGHT) > 0 && run.potionOn(RunPotions.SWIFT) > 0);
        assertTrue(run.potionWait(RunPotions.MIGHT) > run.potionWait(RunPotions.SWIFT) - 2);
        assertEquals(0, run.potionWait(RunPotions.GUARD), 1e-6f);
        // Its wait outlasts the buff.
        assertTrue(RunPotions.cooldown(RunPotions.MIGHT) > RunPotions.lasts(RunPotions.MIGHT));
    }

    @Test public void cooldownsAreCutByTheTreeNeverByMoreThanHalfAndHealsAreSlowerInABossFight() {
        float base = RunPotions.cooldown(RunPotions.HEAL_L);
        assertEquals(base * .8f, RunPotions.cooldownFor(RunPotions.HEAL_L, 20, 0, false), 1e-4f);
        assertEquals(base * .5f, RunPotions.cooldownFor(RunPotions.HEAL_L, 40, 30, false), 1e-4f);
        assertEquals(base * .5f, RunPotions.cooldownFor(RunPotions.HEAL_L, 100, 100, false), 1e-4f);
        assertEquals(base * RunPotions.BOSS_HEAL_SLOWER, RunPotions.cooldownFor(RunPotions.HEAL_L, 0, 0, true), 1e-4f);
        assertEquals(RunPotions.cooldown(RunPotions.MIGHT), RunPotions.cooldownFor(RunPotions.MIGHT, 0, 0, true), 1e-4f);
        // Drinks are quicker by quick sipping, never by more than half.
        float drink = RunPotions.drinkSeconds(RunPotions.HEAL_L);
        assertEquals(drink * .7f, RunPotions.drinkFor(RunPotions.HEAL_L, 30), 1e-4f);
        assertEquals(drink * .5f, RunPotions.drinkFor(RunPotions.HEAL_L, 90), 1e-4f);
        assertTrue(RunPotions.drinkSeconds(RunPotions.HEAL_L) > RunPotions.drinkSeconds(RunPotions.HEAL_S));
        for (int potion = 0; potion < RunPotions.COUNT; potion++) {
            assertTrue(RunPotions.drinkSeconds(potion) >= .8f && RunPotions.drinkSeconds(potion) <= 1.2f);
        }
    }

    @Test public void thePouchShowsWhatIsChosenOrTheFirstThereIsAndNeverAPermanentOne() {
        int[] have = new int[RunPotions.COUNT];
        assertEquals(-1, RunPotions.shown(-1, have));
        assertEquals(0, RunPotions.pouch(have).length);
        have[RunPotions.SWIFT] = 2;
        have[RunPotions.HEAL_M] = 1;
        have[RunPotions.LIFE] = 3;
        assertEquals(RunPotions.HEAL_M, RunPotions.shown(-1, have));
        assertEquals(RunPotions.SWIFT, RunPotions.shown(RunPotions.SWIFT, have));
        assertEquals(RunPotions.HEAL_M, RunPotions.shown(RunPotions.LIFE, have));
        assertEquals(2, RunPotions.pouch(have).length);
        have[RunPotions.SWIFT] = 0;
        assertEquals(RunPotions.HEAL_M, RunPotions.shown(RunPotions.SWIFT, have));
    }

    @Test public void herPermanentPotionsAreFewMoreAsSheTrustsAndNoMoreThanFiveOfEach() {
        assertEquals(0, RunPotions.permanentStock(19, 0));
        assertEquals(1, RunPotions.permanentStock(20, 0));
        assertEquals(0, RunPotions.permanentStock(20, 1));
        assertEquals(3, RunPotions.permanentStock(60, 0));
        assertEquals(1, RunPotions.permanentStock(100, 4));
        assertEquals(0, RunPotions.permanentStock(100, 5));
        assertEquals(0, RunPotions.permanentStock(100, 9));
        // Drunk, one gives its health for good, and has no cooldown.
        PlusRun run = flat();
        int most = run.maxHp();
        assertTrue(run.drink(RunPotions.LIFE));
        wait(run, RunPotions.drinkSeconds(RunPotions.LIFE) + .1f, false, false);
        assertEquals(most + RunPotions.LIFE_HEALTH, run.maxHp());
        assertEquals(0, run.potionWait(RunPotions.LIFE), 1e-6f);
        for (int potion = 0; potion < RunPotions.COUNT; potion++) {
            assertEquals(potion >= RunPotions.LIFE, RunPotions.permanent(potion));
            assertTrue(RunPotions.NAMES[potion].length() > 0 && RunPotions.WORDS[potion].length() > 0);
        }
    }

    @Test public void theirPicturesAreInTheRunPack() {
        assertTrue(new java.io.File("../runart/src/main/assets/runart/potions.webp").isFile());
        assertTrue(new java.io.File("../runart/src/main/assets/runart/run_icon_pouch.webp").isFile());
        assertTrue(new java.io.File("../runart/src/main/assets/runart/halgakos_drink.webp").isFile());
        for (String[] line : RunStory.POUCH) assertTrue(line[0], RunVoicesTest.recorded(line[0]));
    }
}
