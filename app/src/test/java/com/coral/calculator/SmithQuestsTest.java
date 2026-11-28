package com.coral.calculator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** The smith sisters' questline: each sister's quest, then all three's; steps counted from when they began; gifts taken; rewards given. */
public class SmithQuestsTest {
    /** A save in memory. */
    private static final class Save implements RunVanity.Keep {
        final Map<String, Integer> kept = new HashMap<>();

        @Override public int tally(String key) { return kept.getOrDefault(key, 0); }

        @Override public void keep(String key, int value) { kept.put(key, value); }

        void add(String key, int more) { keep(key, tally(key) + more); }
    }

    @Test public void fourQuestsOfThreeStepsEachWithTheirLinesAndRewards() {
        assertEquals(4, SmithQuests.COUNT);
        assertEquals(SmithQuests.COUNT * 5, RunStory.SMITH_QUESTS.length);
        Set<Integer> rewards = new HashSet<>();
        for (int quest = 0; quest < SmithQuests.COUNT; quest++) {
            assertEquals(3, SmithQuests.STEPS[quest].length);
            assertTrue(rewards.add(SmithQuests.REWARDS[quest]));
            assertTrue(RunLoot.rarity(SmithQuests.REWARDS[quest]) >= RunLoot.RED);
            // Asked and ended by a sister; answered by Halgakos.
            assertEquals("H", SmithQuests.line(quest, 1)[2]);
            for (int which : new int[]{0, 2, 3, 4}) assertTrue("Wcd".contains(SmithQuests.line(quest, which)[2]));
        }
        assertEquals(RunLoot.ROSE, RunLoot.rarity(SmithQuests.REWARDS[SmithQuests.TOGETHER]));
        // Every quest's lines end with her catchphrase where she speaks.
        for (int quest = 0; quest < SmithQuests.COUNT; quest++) assertTrue(SmithQuests.line(quest, 1)[1].endsWith("Sloppy~♡"));
    }

    @Test public void moruQuestCountsFromWhenItBeganTakesTheMetalAndGivesHerHammer() {
        Save save = new Save();
        save.keep("run_mobs", 500);
        save.keep("run_mat_0", 10);
        assertEquals(0, SmithQuests.state(save, SmithQuests.MORU_QUEST));
        assertTrue(SmithQuests.take(save, SmithQuests.MORU_QUEST));
        assertFalse(SmithQuests.take(save, SmithQuests.MORU_QUEST));
        // The five hundred before she took it up do not count.
        assertArrayEquals(new int[]{0, 60}, SmithQuests.progress(save, SmithQuests.MORU_QUEST));
        assertEquals(SmithQuests.NOTHING, SmithQuests.hand(save, SmithQuests.MORU_QUEST));
        save.add("run_mobs", 60);
        assertTrue(SmithQuests.ready(save, SmithQuests.MORU_QUEST));
        assertEquals(SmithQuests.STEP, SmithQuests.hand(save, SmithQuests.MORU_QUEST));
        // Six type metal, taken when handed in.
        assertArrayEquals(new int[]{6, 6}, SmithQuests.progress(save, SmithQuests.MORU_QUEST));
        assertEquals(SmithQuests.STEP, SmithQuests.hand(save, SmithQuests.MORU_QUEST));
        assertEquals(4, save.tally("run_mat_0"));
        // A dungeon cleared since.
        assertEquals(SmithQuests.NOTHING, SmithQuests.hand(save, SmithQuests.MORU_QUEST));
        save.keep("run_dungeons", 1 << 3);
        int bank = save.tally("run_bank");
        assertEquals(SmithQuests.FINISHED, SmithQuests.hand(save, SmithQuests.MORU_QUEST));
        assertTrue(SmithQuests.done(save, SmithQuests.MORU_QUEST));
        assertEquals(bank + SmithQuests.PLUSES[SmithQuests.MORU_QUEST], save.tally("run_bank"));
        assertEquals(SmithQuests.NOTHING, SmithQuests.hand(save, SmithQuests.MORU_QUEST));
    }

    @Test public void pulmuAndSutdolAskForHuntsTreesHiddenBossesAndDepth() {
        Save save = new Save();
        save.keep("run_mat_1", 4);
        save.keep("run_mat_2", 3);
        assertTrue(SmithQuests.take(save, SmithQuests.PULMU_QUEST));
        save.add("run_hunts", 4);
        assertEquals(SmithQuests.STEP, SmithQuests.hand(save, SmithQuests.PULMU_QUEST));
        assertEquals(SmithQuests.STEP, SmithQuests.hand(save, SmithQuests.PULMU_QUEST));
        assertEquals(0, save.tally("run_mat_1"));
        save.keep("run_hidden_26", 1);
        assertEquals(SmithQuests.FINISHED, SmithQuests.hand(save, SmithQuests.PULMU_QUEST));

        assertTrue(SmithQuests.take(save, SmithQuests.SUTDOL_QUEST));
        save.add("run_trees_cut", 5);
        assertEquals(SmithQuests.STEP, SmithQuests.hand(save, SmithQuests.SUTDOL_QUEST));
        assertEquals(SmithQuests.STEP, SmithQuests.hand(save, SmithQuests.SUTDOL_QUEST));
        // Never gone down: nowhere. Three floors cleared in one: the fourth reached.
        assertArrayEquals(new int[]{0, 4}, SmithQuests.progress(save, SmithQuests.SUTDOL_QUEST));
        save.keep("run_deep_5", 2);
        assertFalse(SmithQuests.ready(save, SmithQuests.SUTDOL_QUEST));
        save.keep("run_deep_5", 3);
        assertEquals(SmithQuests.FINISHED, SmithQuests.hand(save, SmithQuests.SUTDOL_QUEST));
    }

    @Test public void allThreeTogetherOnlyOnceTheirOwnAreDone() {
        Save save = new Save();
        assertFalse(SmithQuests.open(save, SmithQuests.TOGETHER));
        assertFalse(SmithQuests.take(save, SmithQuests.TOGETHER));
        for (int quest = 0; quest < 3; quest++) save.keep("run_sq_" + quest, 4);
        assertTrue(SmithQuests.open(save, SmithQuests.TOGETHER));
        save.keep("run_dungeons", 1);
        assertTrue(SmithQuests.take(save, SmithQuests.TOGETHER));
        // Three more dungeons cleared since it was taken up.
        save.keep("run_dungeons", 1 | 2 | 4 | 8);
        assertEquals(SmithQuests.STEP, SmithQuests.hand(save, SmithQuests.TOGETHER));
        // Five of each metal: four of one is not enough.
        save.keep("run_mat_0", 5);
        save.keep("run_mat_1", 4);
        save.keep("run_mat_2", 9);
        assertFalse(SmithQuests.ready(save, SmithQuests.TOGETHER));
        save.keep("run_mat_1", 5);
        assertEquals(SmithQuests.STEP, SmithQuests.hand(save, SmithQuests.TOGETHER));
        assertEquals(0, save.tally("run_mat_0"));
        assertEquals(0, save.tally("run_mat_1"));
        assertEquals(4, save.tally("run_mat_2"));
        // The Inkwell Depths, and no other.
        assertFalse(SmithQuests.ready(save, SmithQuests.TOGETHER));
        save.keep("run_dungeons", save.tally("run_dungeons") | 1 << 16);
        assertEquals("The Inkwell Depths", RunDungeons.NAMES[16]);
        assertEquals(SmithQuests.FINISHED, SmithQuests.hand(save, SmithQuests.TOGETHER));
    }
}
