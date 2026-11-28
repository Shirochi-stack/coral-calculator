package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The Mirror Depths: past the twenty-four, in the ninety-ninth land; her shadows on every floor, blinding her; the Imperfect Copy at the bottom. */
public class RunMirrorTest {
    @Test public void itIsADungeonOfItsOwnInTheNinetyNinthLandFifteenFloorsDeep() {
        assertEquals(RunDungeons.COUNT, RunDungeons.MIRROR);
        assertEquals(RunDungeons.MIRROR, RunDungeons.inLand(99));
        assertEquals(15, RunDungeons.floors(RunDungeons.MIRROR));
        assertEquals("The Mirror Depths", RunDungeons.name(RunDungeons.MIRROR));
        assertEquals(RunUniques.COPY, RunDungeons.keeper(RunDungeons.MIRROR));
        assertTrue(RunUniques.keeper(RunUniques.COPY) && RunUniques.great(RunUniques.COPY));
        assertEquals(RunLoot.ROSE, RunLoot.rarity(RunDungeons.relic(RunDungeons.MIRROR)));
        assertEquals(2, RunDungeons.band(RunDungeons.MIRROR_FLOORS - 1));
        assertTrue(new java.io.File("../mirror/src/main/assets/" + RunUniques.COPY_SHEET).isFile());
        for (String[] line : RunStory.MIRROR) assertTrue(new java.io.File("src/main/res/raw/run_v_" + line[0] + ".ogg").isFile());
    }

    @Test public void itsFloorsHaveOnlyHerShadowsAndTheLastTheCopy() {
        for (int floor : new int[]{0, 5, 9, 14}) {
            RunLevels.Level level = RunLevels.dungeon(RunDungeons.MIRROR, floor, 77);
            int shadows = 0, copies = 0;
            for (int[] foe : level.foes) {
                // (Signs of the land may be there too; of the unique creatures, only hers.)
                if (RunUniques.is(foe[0])) assertTrue(RunUniques.name(foe[0]), RunUniques.shadow(foe[0]) || RunUniques.copy(foe[0]) || RunUniques.castShadow(foe[0]) || RunUniques.echo(foe[0]));
                if (RunUniques.shadow(foe[0])) shadows++;
                if (RunUniques.copy(foe[0])) copies++;
            }
            assertTrue(shadows > 0);
            assertEquals(floor == 14 ? 1 : 0, copies);
        }
    }

    @Test public void aShadowsTouchBlindsHerForAMoment() {
        RunLevels.Level level = new RunLevels.Level(99, 40);
        for (int c = 0; c < 40; c++) for (int r = 10; r < RunLevels.ROWS; r++) level.land[r][c] = RunLevels.BLOCK;
        level.gate = 37;
        level.foes.add(new int[]{RunUniques.SHADOW_FIRST, 6, 9});
        PlusRun.Perks perks = new PlusRun.Perks();
        perks.hearts = 60;
        PlusRun run = new PlusRun(level, perks);
        run.place(3, 10 - PlusRun.TALL - .02f);
        for (float gone = 0; gone < 10 && run.herBlind() <= 0; gone += 1 / 60f) run.step(1 / 60f, false, true, false, false);
        assertTrue(run.herBlind() > 0);
        assertEquals(PlusRun.PLAYING, run.state());
        for (float gone = 0; gone < 3; gone += 1 / 60f) run.step(1 / 60f, false, false, false, false);
        assertTrue("it passes", run.herBlind() <= 0 || run.herBlinds() > 1);
    }

    /** Shadows of those she knows on its third, sixth, ninth and twelfth floors; Echoes on its fifth and tenth; each with its line. */
    @Test public void itsMiddleFloorsHaveTheShadowCastAndTheEchoes() {
        for (int floor = 0; floor < RunDungeons.MIRROR_FLOORS - 1; floor++) {
            RunLevels.Level level = RunLevels.dungeon(RunDungeons.MIRROR, floor, 31);
            int casts = 0, echoes = 0;
            for (int[] foe : level.foes) {
                if (RunUniques.castShadow(foe[0])) casts++;
                if (RunUniques.echo(foe[0])) echoes++;
            }
            assertEquals("floor " + floor, floor % 3 == 2 && floor < 12 ? 1 : 0, casts);
            assertEquals("floor " + floor, floor == 4 || floor == 9 ? 1 : 0, echoes);
        }
        assertEquals(RunUniques.ECHO_COUNT, RunStory.ECHOES.length);
        assertEquals(RunUniques.ECHO_COUNT, RunStory.ECHO_ANSWERS.length);
        assertEquals(RunUniques.CAST_COUNT, RunStory.CAST_SHADOWS.length);
        for (String[][] group : new String[][][]{RunStory.ECHOES, RunStory.ECHO_ANSWERS, RunStory.CAST_SHADOWS}) {
            for (String[] line : group) assertTrue(line[0], new java.io.File("src/main/res/raw/run_v_" + line[0] + ".ogg").isFile());
        }
        // Tougher than a mini-boss, the Echoes; the shadows of the cast five times one of their way.
        for (int which = 0; which < RunUniques.ECHO_COUNT; which++) assertTrue(RunUniques.health(RunUniques.ECHO_FIRST + which, 99) > RunUniques.health(RunUniques.FIRST + 10, 99));
        for (int which = 0; which < RunUniques.CAST_COUNT; which++) assertEquals(PlusRun.DEFEAT_FADE, PlusRun.defeatOf(RunUniques.CAST_FIRST + which));
    }
}