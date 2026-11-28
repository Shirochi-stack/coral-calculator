package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The tree's alchemy: quicker drinks (walking at the last rank), steady hands, herbalism (never past half with gear), the second draught; placed mid-tree. */
public class RunAlchemyTest {
    private static final float STEP = 1 / 60f;

    private static PlusRun run(int effect, int much) {
        RunLevels.Level flat = new RunLevels.Level(40, 60);
        for (int column = 0; column < 60; column++) for (int row = RunLevels.FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 57;
        PlusRun.Perks perks = new PlusRun.Perks();
        if (effect >= 0) perks.skills.effects[effect] = much;
        PlusRun run = new PlusRun(flat, perks);
        run.place(10, RunLevels.FLOOR - PlusRun.TALL);
        run.step(STEP, false, false, false);
        return run;
    }

    private static float drinkTime(PlusRun run, int potion) {
        assertTrue(run.drink(potion));
        float gone = 0;
        while (run.drinking() >= 0 && gone < 5) {
            run.step(STEP, false, false, false);
            gone += STEP;
        }
        return gone;
    }

    @Test public void quickSipShortensEveryDrink() {
        float plain = drinkTime(run(-1, 0), RunPotions.HEAL_L), quick = drinkTime(run(RunSkills.E_QUICK_SIP, 30), RunPotions.HEAL_L);
        assertEquals(RunPotions.drinkSeconds(RunPotions.HEAL_L), plain, .05f);
        assertEquals(RunPotions.drinkSeconds(RunPotions.HEAL_L) * .7f, quick, .05f);
        assertEquals(RunSkills.E_QUICK_SIP, RunSkills.NOTABLES.get(RunSkills.ALCHEMY).effects[0]);
        assertEquals(15, RunSkills.NOTABLES.get(RunSkills.ALCHEMY).figures[0]);
        assertEquals(45, 3 * 15);
        assertEquals(RunSkills.QUICK_SIP_WALKS, 45);
    }

    @Test public void herbalismShortensCooldownsButNeverByMoreThanHalf() {
        PlusRun some = run(RunSkills.E_HERBALIST, 20), much = run(RunSkills.E_HERBALIST, 90);
        drinkTime(some, RunPotions.MIGHT);
        drinkTime(much, RunPotions.MIGHT);
        float base = RunPotions.cooldown(RunPotions.MIGHT);
        // (The buff lasts longer than either cooldown begins to show; its own wait is what is set.)
        assertEquals(base * .8f, some.potionWaitFull(RunPotions.MIGHT), .01f);
        assertEquals(base * RunPotions.MOST_OFF, much.potionWaitFull(RunPotions.MIGHT), .01f);
        // With gear, still never past half.
        assertEquals(base * .5f, RunPotions.cooldownFor(RunPotions.MIGHT, 30, 40, false), .01f);
    }

    @Test public void steadyHandsKeepADrinkThroughALightHurtButNotAHardOne() {
        PlusRun shaky = run(-1, 0);
        shaky.drink(RunPotions.HEAL_M);
        shaky.drinkHurt(false);
        assertEquals(-1, shaky.drinking());
        PlusRun steady = run(RunSkills.E_STEADY_HANDS, 1);
        steady.drink(RunPotions.HEAL_M);
        steady.drinkHurt(false);
        assertEquals(RunPotions.HEAL_M, steady.drinking());
        steady.drinkHurt(true);
        assertEquals(-1, steady.drinking());
        assertEquals(1, steady.drinksStopped());
    }

    @Test public void theSecondDraughtSometimesKeepsAPotion() {
        PlusRun run = run(RunSkills.E_SECOND_DRAUGHT, 100);
        drinkTime(run, RunPotions.SWIFT);
        assertTrue(run.keptLast());
        assertEquals(1, run.potionsSaved());
        PlusRun none = run(-1, 0);
        drinkTime(none, RunPotions.SWIFT);
        assertFalse(none.keptLast());
    }

    @Test public void theAlchemyNotablesAreEachInTheTreeOnceMidwayOut() {
        assertEquals(RunSkills.ALCHEMY + RunSkills.ALCHEMY_COUNT, RunSkills.NOTABLES.size());
        int[] seen = new int[RunSkills.ALCHEMY_COUNT];
        for (int id = 0; id < RunSkills.tree().size(); id++) {
            RunSkills.Notable notable = RunSkills.notableAt(id);
            if (notable == null || notable.index < RunSkills.ALCHEMY) continue;
            seen[notable.index - RunSkills.ALCHEMY]++;
        }
        for (int which = 0; which < RunSkills.ALCHEMY_COUNT; which++) assertEquals("alchemy " + which, 1, seen[which]);
        String[] names = {"Quick Sip", "Quicker Sip", "Gulp", "Steady Hands", "Herbalist", "Apothecary", "Master Herbalist", "Second Draught"};
        for (int which = 0; which < names.length; which++) {
            RunSkills.Notable notable = RunSkills.NOTABLES.get(RunSkills.ALCHEMY + which);
            assertEquals(names[which], notable.name);
            assertFalse(notable.costly());
            assertTrue(notable.said().length() > 0);
        }
        assertTrue(new java.io.File("../runart/src/main/assets/runart/tree_notables.webp").isFile());
    }
}
