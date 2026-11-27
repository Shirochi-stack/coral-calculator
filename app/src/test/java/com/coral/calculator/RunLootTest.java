package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/** Her level without a cap, and what creatures drop: pluses like coins, things to keep of six rarities, the rose only from the hardest. */
public class RunLootTest {
    private static final float STEP = 1 / 60f;

    /** The level each land is for, as the lands make it: seven creatures in ten beaten, and whoever waits. */
    static int[] recommendedFromTheLands() {
        int[] table = new int[RunLevels.TOTAL];
        long xp = 0;
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            int level = RunLoot.levelAt(xp);
            table[number - 1] = level;
            RunLevels.Level land = RunLevels.level(number);
            int tough = RunLevels.toughOf(number), creatures = 0, swarmed = 0;
            boolean boss = false;
            for (int[] foe : land.foes) {
                if (foe[0] == RunLevels.TARGET || foe[1] >= land.main) continue;
                if (RunLevels.boss(foe[0]) && !RunLevels.rover(foe[0])) boss = true;
                else if (RunLevels.swarmed(foe)) swarmed++;
                else creatures++;
            }
            // (One of a swarm gives its share.)
            xp += Math.round((creatures + swarmed / (double) PlusRun.SWARM_SHARE) * .7 * RunLoot.xpFor(false, tough, number, level));
            if (boss) xp += RunLoot.xpFor(true, tough, number, level);
        }
        return table;
    }

    /** Not a check: run with RUN_LEVELS set to print the table of the levels the lands are for. */
    @Test public void printsTheLevelsTheLandsAreFor() {
        if (System.getenv("RUN_LEVELS") == null) return;
        int[] table = recommendedFromTheLands();
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < table.length; index++) text.append(table[index]).append(index % 20 == 19 ? ",\n" : ", ");
        throw new AssertionError("LEVELS:\n" + text);
    }

    @Test public void theLevelsTheLandsAreForAreWhatTheLandsMake() {
        int[] table = recommendedFromTheLands();
        for (int number = 1; number <= RunLevels.TOTAL; number++) {
            assertTrue("land " + number + ": " + RunLoot.recommended(number) + " but the lands make " + table[number - 1], Math.abs(RunLoot.recommended(number) - table[number - 1]) <= 1);
        }
        // Up as the lands go on, never down.
        for (int number = 2; number <= RunLevels.TOTAL; number++) assertTrue(RunLoot.recommended(number) >= RunLoot.recommended(number - 1));
    }

    @Test public void herLevelHasNoCapAndEachTakesMore() {
        for (int level = 1; level < 2000; level++) {
            assertTrue(RunLoot.toNext(level + 1) > RunLoot.toNext(level));
            assertEquals(level, RunLoot.levelAt(RunLoot.need(level)));
            assertEquals(level, RunLoot.levelAt(RunLoot.need(level + 1) - 1));
        }
        assertEquals(1, RunLoot.levelAt(0));
        // What a level gives, at the levels in the report: harder blows, cheaper hurts, more hearts.
        assertEquals(1, RunLoot.power(1), 0);
        assertEquals(1.27f, RunLoot.power(10), .001f);
        assertEquals(2.47f, RunLoot.power(50), .001f);
        assertEquals(3.97f, RunLoot.power(100), .001f);
        assertEquals(15.97f, RunLoot.power(500), .001f);
        assertEquals(0, RunLoot.guard(10));
        assertEquals(1, RunLoot.guard(50));
        assertEquals(2, RunLoot.guard(100));
        assertEquals(12, RunLoot.guard(500));
        assertEquals(0, RunLoot.hearts(10));
        assertEquals(2, RunLoot.hearts(50));
        assertEquals(3, RunLoot.hearts(100));
        assertEquals(5, RunLoot.hearts(500));
    }

    @Test public void lessForCreaturesFarBelowHer() {
        int number = 10, recommended = RunLoot.recommended(number);
        int fair = RunLoot.xpFor(false, 1, number, recommended), above = RunLoot.xpFor(false, 1, number, recommended + 20), far = RunLoot.xpFor(false, 1, number, recommended + 400);
        assertTrue(above < fair);
        assertEquals(Math.max(1, fair / 10), far);
        assertEquals(10 * RunLoot.xpFor(false, 1, number, recommended), RunLoot.xpFor(true, 1, number, recommended));
    }

    /** The odds add up; the rose never falls from an ordinary creature; what is dropped is named for its chapter and kind, and its rarity kept. */
    @Test public void rarityOddsAndNames() {
        for (int[] odds : new int[][]{RunLoot.CREATURE, RunLoot.HARD, RunLoot.BOSS}) {
            int sum = 0;
            for (int chance : odds) sum += chance;
            assertEquals(10000, sum);
        }
        assertEquals(0, RunLoot.CREATURE[RunLoot.ROSE]);
        assertEquals(0, RunLoot.BOSS[RunLoot.WHITE]);
        assertEquals(RunLoot.WHITE, RunLoot.rarityOf(RunLoot.CREATURE, 0));
        assertEquals(RunLoot.ROSE, RunLoot.rarityOf(RunLoot.BOSS, 9999));
        for (int type = 0; type < 3; type++) {
            for (int theme = 0; theme < RunLoot.THEMES.length; theme++) {
                for (int base = 0; base < 30; base++) {
                    for (int rarity = 0; rarity <= RunLoot.ROSE; rarity++) {
                        int code = RunLoot.code(type, theme, base, rarity);
                        assertEquals(type, RunLoot.type(code));
                        assertEquals(theme, RunLoot.theme(code));
                        assertEquals(base, RunLoot.base(code));
                        assertEquals(rarity, RunLoot.rarity(code));
                    }
                }
            }
        }
        assertEquals("Archive Katana", RunLoot.name(RunLoot.code(RunLoot.WEAPON, 1, 1, RunLoot.BLUE)));
        assertEquals("Halgakos Rose Scythe", RunLoot.name(RunLoot.code(RunLoot.WEAPON, 11, 11, RunLoot.ROSE)));
        assertEquals(30, RunLoot.ARMOURS.length);
        assertEquals(30, RunLoot.ACCESSORIES.length);
        assertEquals(12, RunLoot.WEAPONS.length);
        List<Integer> bag = RunLoot.bag("");
        for (int index = 0; index < RunLoot.BAG + 10; index++) RunLoot.add(bag, RunLoot.code(0, 0, 0, index < 10 ? RunLoot.WHITE : RunLoot.GOLD));
        assertEquals(RunLoot.BAG, bag.size());
        for (int code : bag) assertEquals(RunLoot.GOLD, RunLoot.rarity(code));
        assertEquals(bag, RunLoot.bag(RunLoot.note(bag)));
    }

    /** A creature that falls drops pluses that fall, bounce, rest and come to her; something to keep now and then; and whoever waits always drops one. */
    @Test public void whatFallsFallsAndIsPickedUp() {
        RunLevels.Level level = RunLevels.level(6);
        PlusRun run = new PlusRun(level);
        int creatures = 0;
        for (PlusRun.Foe foe : run.foes()) {
            if (RunLevels.boss(foe.kind) || foe.kind == RunLevels.TARGET) continue;
            foe.flat = true;
            if (!foe.swarm) creatures++;
        }
        run.step(STEP, false, false, false);
        assertTrue(run.scattered().size() >= creatures);
        assertTrue(run.xp() > 0);
        // They come to rest on the ground, and lie there until they are gone, blinking at the end.
        for (float gone = 0; gone < 3; gone += STEP) run.step(STEP, false, false, false);
        int resting = 0;
        for (PlusRun.Drop drop : run.scattered()) if (drop.resting) resting++;
        assertTrue(resting > run.scattered().size() / 2);
        for (float gone = 0; gone < 45; gone += STEP) run.step(STEP, false, false, false);
        assertEquals(0, run.scattered().size());
        // Over many lands' creatures, things to keep fall about as often as their chances say, and the rose not at all.
        int dropped = 0;
        double falls = 0;
        for (int number = 1; number <= 40; number++) {
            PlusRun one = new PlusRun(RunLevels.level(number));
            for (PlusRun.Foe foe : one.foes()) {
                if (RunLevels.boss(foe.kind) || foe.kind == RunLevels.TARGET) continue;
                foe.flat = true;
                // (One of a swarm counts for its share.)
                falls += foe.swarm ? 1.0 / PlusRun.SWARM_SHARE : 1;
            }
            one.step(STEP, false, false, false);
            for (PlusRun.Drop drop : one.scattered()) {
                if (drop.code == 0) continue;
                dropped++;
                assertTrue(RunLoot.rarity(drop.code) < RunLoot.ROSE);
            }
        }
        assertTrue("things " + dropped + " of " + falls + " falls", dropped > falls * .015 && dropped < falls * .08);
        // Whoever waits drops something every time, never white.
        PlusRun boss = new PlusRun(RunLevels.level(6));
        for (PlusRun.Foe foe : boss.foes()) if (RunLevels.boss(foe.kind) && !RunLevels.rover(foe.kind)) foe.flat = true;
        boss.step(STEP, false, false, false);
        int things = 0;
        for (PlusRun.Drop drop : boss.scattered()) {
            if (drop.code == 0) continue;
            things++;
            assertTrue(RunLoot.rarity(drop.code) > RunLoot.WHITE);
        }
        assertEquals(1, things);
    }

    /** Pluses near her come to her; a thing to keep is picked up by walking into it. */
    @Test public void sheGathersWhatFell() {
        RunLevels.Level level = RunLevels.level(3);
        PlusRun run = new PlusRun(level);
        run.place(10, RunLevels.top(level, 10) - PlusRun.TALL - .05f);
        for (float gone = 0; gone < .3f; gone += STEP) run.step(STEP, false, false, false);
        PlusRun.Foe near = null;
        for (PlusRun.Foe foe : run.foes()) if (!RunLevels.boss(foe.kind) && (near == null || Math.abs(foe.x - 10) < Math.abs(near.x - 10))) near = foe;
        near.x = 10.5f;
        near.y = run.y();
        near.flat = true;
        int before = run.bonus();
        for (float gone = 0; gone < 3; gone += STEP) run.step(STEP, false, false, false);
        assertTrue(run.dropPluses() > 0);
        assertEquals(before + run.dropPluses(), run.bonus());
    }
}
