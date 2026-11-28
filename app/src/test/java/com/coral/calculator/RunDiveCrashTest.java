package com.coral.calculator;

import org.junit.Test;

/** Every floor of every dungeon can be laid and walked into without anything giving way. */
public class RunDiveCrashTest {
    @Test public void everyFloorOfEveryDungeonCanBeEnteredAndPlayed() {
        for (int dungeon = 0; dungeon < RunDungeons.COUNT; dungeon++) {
            for (int floor = 0; floor < RunDungeons.floors(dungeon); floor++) {
                for (long dive = 1; dive <= 6; dive++) {
                    try {
                        RunLevels.Level level = RunLevels.dungeon(dungeon, floor, dive * 104729L + dungeon * 7919L);
                        PlusRun.Perks perks = new PlusRun.Perks();
                        perks.hearts = 99;
                        PlusRun run = new PlusRun(level, perks);
                        for (float gone = 0; gone < 4 && run.state() == PlusRun.PLAYING; gone += 1 / 60f) run.step(1 / 60f, gone % 1 < .5f, false, gone % .7f < .1f);
                    } catch (RuntimeException e) {
                        throw new AssertionError("dungeon " + dungeon + " (" + RunDungeons.name(dungeon) + ") floor " + floor + " dive " + dive, e);
                    }
                }
            }
        }
    }
}
