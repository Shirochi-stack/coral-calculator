package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/** D7: hideouts behind strong cracks or on high ledges, a hidden boss in each, and what it drops. */
public class RunSecretsTest {
    private static final float STEP = 1 / 60f;

    private static int[] hideout(RunLevels.Level level) {
        for (int[] room : level.rooms) if (room[0] == RunLevels.HIDEOUT) return room;
        return null;
    }

    @Test public void oneLandInThreeHasAHideoutWithItsHiddenBoss() {
        int lands = 0, high = 0, cracks = 0;
        Set<Integer> bosses = new HashSet<>();
        for (int number = 1; number < RunLevels.OPEN_TO; number++) {
            RunLevels.Level level = RunLevels.level(number);
            int[] room = hideout(level);
            assertEquals("land " + number, RunLevels.hideoutIn(number), room != null);
            if (room == null) continue;
            lands++;
            // Its boss, of its chapter, inside it.
            int boss = -1;
            for (int[] foe : level.foes) if (RunUniques.hidden(foe[0])) {
                boss = foe[0];
                assertTrue(foe[1] > room[3] && foe[1] < room[4]);
            }
            assertEquals(RunUniques.hiddenOf(RunLevels.chapterOf(number)), boss);
            bosses.add(boss);
            // Up on a ledge out of reach of a double jump, or behind a crack.
            if (RunLevels.hideoutHigh(number)) {
                high++;
                assertEquals(RunLevels.top(level, room[1]) - 6, room[2]);
                assertTrue(level.land[room[2]][room[1]] == RunLevels.LEDGE || level.land[room[2]][room[1] - 1] == RunLevels.LEDGE);
            } else {
                cracks++;
                assertEquals(1, room[7]);
            }
        }
        assertTrue("lands " + lands, lands >= 25 && lands <= 40);
        assertTrue(high >= 8 && cracks >= 8);
        assertEquals(RunUniques.HIDDEN_COUNT, bosses.size());
        for (int chapter = 0; chapter < 12; chapter++) assertTrue(RunUniques.hidden(RunUniques.hiddenOf(chapter)));
    }

    @Test public void aStrongCrackBreaksOnlyToAStrongBlow() {
        int number = -1;
        for (int land = RunLevels.HIDEOUTS_FROM; land < RunLevels.OPEN_TO && number < 0; land++) if (RunLevels.hideoutIn(land) && !RunLevels.hideoutHigh(land)) number = land;
        assertTrue(number > 0);
        RunLevels.Level level = RunLevels.level(number);
        int[] room = hideout(level);
        int index = level.rooms.indexOf(room);
        PlusRun weak = new PlusRun(level, new PlusRun.Perks());
        weak.place(room[1] + .5f - PlusRun.WIDE / 2, room[2] - PlusRun.TALL);
        weak.step(STEP, false, false, false);
        assertTrue(weak.enter());
        assertEquals(0, weak.revealed() & 1 << index);
        assertTrue(weak.clangs() > 0);
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.level = 150;
        perks.skills.stats[RunSkills.ATK] = 500;
        PlusRun strong = new PlusRun(level, perks);
        strong.place(room[1] + .5f - PlusRun.WIDE / 2, room[2] - PlusRun.TALL);
        strong.step(STEP, false, false, false);
        assertTrue(strong.enter());
        assertTrue((strong.revealed() & 1 << index) != 0);
        // And broken into, the door takes her in.
        assertTrue(strong.enter());
        assertEquals(index, strong.room());
    }

    @Test public void aHiddenBossDropsThreeThingsGoldOrBetterAndHasItsOwnRose() {
        assertEquals(75, RunUniques.COUNT);
        Set<Integer> signatures = new HashSet<>();
        for (int kind = RunUniques.HIDDEN_FIRST; kind < RunUniques.HIDDEN_FIRST + RunUniques.HIDDEN_COUNT; kind++) {
            int signature = RunUniques.signature(kind);
            assertEquals(RunLoot.ROSE, RunLoot.rarity(signature));
            signatures.add(signature);
            assertTrue(RunUniques.health(kind, 40) > RunUniques.health(RunUniques.FIRST + 10, 40));
            assertTrue(RunUniques.bite(kind, 10) >= 2);
        }
        assertEquals(6, signatures.size());
        // Beaten: three things, a weapon, an armour and an accessory, all gold or better.
        RunLevels.Level flat = new RunLevels.Level(40, 60);
        for (int column = 0; column < 60; column++) for (int row = RunLevels.FLOOR; row < RunLevels.ROWS; row++) flat.land[row][column] = RunLevels.BLOCK;
        flat.gate = 57;
        flat.foes.add(new int[]{RunUniques.hiddenOf(5), 10, RunLevels.FLOOR - 1, 1, 0, 2});
        PlusRun run = new PlusRun(flat, new PlusRun.Perks());
        PlusRun.Foe boss = run.foes().get(0);
        boss.flat = true;
        run.step(STEP, false, false, false);
        assertEquals(1, run.hiddenBeaten());
        int things = 0;
        Set<Integer> types = new HashSet<>();
        for (PlusRun.Drop drop : run.scattered()) {
            if (drop.code == 0) continue;
            things++;
            types.add(RunLoot.type(drop.code));
            assertTrue(RunLoot.rarity(drop.code) >= RunLoot.GOLD);
        }
        assertEquals(3, things);
        assertEquals(3, types.size());
    }

    @Test public void aHiddenBossWaitsInItsHideout() {
        int number = -1;
        for (int land = RunLevels.HIDEOUTS_FROM; land < RunLevels.OPEN_TO && number < 0; land++) if (RunLevels.hideoutIn(land)) number = land;
        RunLevels.Level level = RunLevels.level(number);
        PlusRun run = new PlusRun(level);
        PlusRun.Foe boss = null;
        for (PlusRun.Foe foe : run.foes()) if (RunUniques.hidden(foe.kind)) boss = foe;
        assertTrue(boss != null && boss.x >= level.main);
        float at = boss.x;
        for (int step = 0; step < 120; step++) run.step(STEP, false, false, false);
        assertEquals(at, boss.x, 1e-4f);
    }
}
