package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** What playing unlocks: the lines, the tricks, and how they come to be unlocked. */
public class UnlocksTest {
    /** What the games remember, held here in place of the device. */
    private static final class Kept implements Unlocks.Store {
        final Map<String, Integer> numbers = new HashMap<>();
        final Map<String, String> notes = new HashMap<>();

        @Override public int tally(String key) {
            Integer value = numbers.get(key);
            return value == null ? 0 : value;
        }

        @Override public String note(String key) { return notes.get(key); }

        @Override public void keepNote(String key, String value) { notes.put(key, value); }

        Kept with(String key, int value) {
            numbers.put(key, value);
            return this;
        }
    }

    /** Sets what is needed for one requirement to hold: {@code tic_won>=3}, {@code c(1-30)>=5}, {@code @colours>=2} and the rest. */
    private static void meet(Kept kept, String requirement) {
        int sign = 0;
        while (sign < requirement.length() && "<>=&".indexOf(requirement.charAt(sign)) < 0) sign++;
        String name = requirement.substring(0, sign);
        int number = sign + 1;
        if (number < requirement.length() && requirement.charAt(number) == '=') number++;
        String how = sign == requirement.length() ? ">=" : requirement.substring(sign, number);
        int asked = sign == requirement.length() ? 1 : Integer.parseInt(requirement.substring(number));
        int wanted = how.equals(">") ? asked + 1 : how.equals("<") ? asked - 1 : asked;
        if (name.startsWith("@")) {
            for (int which = 0; which < wanted; which++) {
                if (name.equals("@colours")) kept.with("run_colours", kept.tally("run_colours") | (1 << wanted) - 1);
                else if (name.equals("@ups")) kept.with("run_up_" + which, 1);
                else if (name.equals("@prizes")) kept.with("run_prize_" + (which + 1), 1);
                else kept.with("fight_cleared_" + which, 1);
            }
        } else if (name.indexOf('(') > 0) {
            int dash = name.indexOf('-'), first = Integer.parseInt(name.substring(2, dash));
            for (int which = 0; which < wanted; which++) {
                int land = first + which;
                switch (name.charAt(0)) {
                    case 'c': kept.with("run_stars_" + land, Math.max(1, kept.tally("run_stars_" + land))); break;
                    case 't': kept.with("run_stars_" + land, 3); break;
                    case 'q': kept.with("run_quest_" + land, 1); break;
                    case 'r': kept.with("run_rooms_" + land, 1); break;
                    case 'd': kept.with("run_roomsdone_" + land, 1); break;
                    default: kept.with("run_stars_" + first, 0); break;
                }
            }
            if (name.charAt(0) == 's') for (int land = first; land < first + (wanted + 2) / 3; land++) kept.with("run_stars_" + land, 3);
        } else if (how.equals("&")) {
            kept.with(name, kept.tally(name) | asked);
        } else if (how.equals("<=") || how.equals("<")) {
            // A best that must be so low is set to it, unless it is already lower and still something.
            int had = kept.tally(name);
            if (had == 0 || had > wanted) kept.with(name, wanted);
        } else {
            kept.with(name, Math.max(kept.tally(name), wanted));
        }
    }

    /** What is kept when the first way of meeting a condition has been met, and nothing else. */
    private static Kept meeting(String condition) {
        Kept kept = new Kept();
        String first = condition.split("\\|")[0];
        // What must be low is set after what must be high, so that a best asked to be above nothing and below something ends as the low one.
        for (String requirement : first.split(",")) if (!requirement.contains("<")) meet(kept, requirement);
        for (String requirement : first.split(",")) if (requirement.contains("<")) meet(kept, requirement);
        return kept;
    }

    @Test public void thereAreAsManyLinesAsWereAskedFor() {
        assertTrue("Plus run", Unlocks.lines(Unlocks.RUN).length >= 300);
        assertTrue("Page brawl", Unlocks.lines(Unlocks.FIGHT).length >= 60);
        for (int group = Unlocks.TIC; group < Unlocks.GROUPS.length; group++) {
            int count = Unlocks.lines(group).length;
            assertTrue(Unlocks.GROUPS[group] + " has " + count, count >= 10 && count <= 20);
        }
        assertEquals(12, Unlocks.GROUPS.length);
        assertTrue(Unlocks.lines() >= 510);
    }

    @Test public void thereAreAsManyTricksAsWereAskedFor() {
        assertEquals(Unlocks.TRICKS, Unlocks.TRICK_NAMES.length);
        assertTrue(Unlocks.RUN_TRICKS >= 20);
        assertTrue(Unlocks.FIGHT_TRICKS >= 6);
        assertEquals(10, Unlocks.TRICKS - Unlocks.RUN_TRICKS - Unlocks.FIGHT_TRICKS);
        // Plus run's are about Plus run, Page brawl's about Page brawl, and each small game has one of its own.
        for (int trick = 0; trick < Unlocks.TRICKS; trick++) {
            String needs = Unlocks.TRICK_NEEDS[trick];
            if (trick < Unlocks.RUN_TRICKS) assertTrue(needs, needs.contains("run_") || needs.contains("(") || needs.contains("@colours"));
            else if (trick < Unlocks.RUN_TRICKS + Unlocks.FIGHT_TRICKS) assertTrue(needs, needs.contains("fight_"));
            else assertTrue(needs, needs.startsWith(Unlocks.GROUPS[Unlocks.TIC + trick - Unlocks.RUN_TRICKS - Unlocks.FIGHT_TRICKS] + "_"));
        }
        // The two of the endings are kept for and not played yet; every other one can be.
        int waiting = 0;
        for (int trick = 0; trick < Unlocks.TRICKS; trick++) if (!Unlocks.drawn(trick)) waiting++;
        assertEquals(2, waiting);
        assertTrue(Unlocks.TRICK_NEEDS[Unlocks.RETREAT].contains("run_ending") && Unlocks.TRICK_NEEDS[Unlocks.LOST_WORDS].contains("run_ending"));
    }

    @Test public void everyLineEndsOnHerCatchphraseAndNoTwoAreTheSame() {
        Set<String> seen = new HashSet<>();
        for (String old : Chatter.all()) seen.add(old);
        for (int group = 0; group < Unlocks.GROUPS.length; group++) {
            for (String[] line : Unlocks.lines(group)) {
                assertEquals(2, line.length);
                String words = line[1];
                assertTrue(words, words.endsWith(" " + Chatter.SLOPPY));
                assertFalse("a comma before her catchphrase: " + words, words.endsWith(", " + Chatter.SLOPPY));
                assertEquals("one heart: " + words, words.indexOf('♡'), words.length() - 1);
                assertEquals("her name for the user once: " + words, words.indexOf("Sloppy"), words.lastIndexOf("Sloppy"));
                assertTrue("too long for her bubble: " + words, words.length() <= 110);
                assertTrue("said twice: " + words, seen.add(words));
            }
        }
    }

    @Test public void everyLineAndTrickCanBeUnlockedAndNothingIsUnlockedByNothing() {
        Kept nothing = new Kept();
        for (int group = 0; group < Unlocks.GROUPS.length; group++) {
            for (String[] line : Unlocks.lines(group)) {
                assertFalse(line[0] + " holds with nothing played", Unlocks.holds(line[0], nothing));
                assertTrue(line[0] + " cannot be met", Unlocks.holds(line[0], meeting(line[0])));
            }
        }
        for (String needs : Unlocks.TRICK_NEEDS) {
            assertFalse(needs, Unlocks.holds(needs, nothing));
            for (String way : needs.split("\\|")) assertTrue(way + " cannot be met", Unlocks.holds(needs, meeting(way)));
        }
        assertEquals(0, Unlocks.check(nothing, false));
        assertEquals(0, Unlocks.found(nothing));
        assertNull(Unlocks.anyLine(nothing, new Random(1)));
        assertEquals(0, Unlocks.freshTrick(nothing));
        assertEquals(0f, Unlocks.share(nothing), 0);
    }

    @Test public void conditionsAreReadAsWritten() {
        Kept kept = new Kept().with("tic_won", 3).with("pairs_best", 12).with("run_colours", 0b1010);
        assertTrue(Unlocks.holds("tic_won>=3", kept));
        assertFalse(Unlocks.holds("tic_won>3", kept));
        assertTrue(Unlocks.holds("tic_won=3", kept));
        assertTrue(Unlocks.holds("tic_won", kept));
        assertFalse(Unlocks.holds("tic_lost", kept));
        assertTrue(Unlocks.holds("pairs_best>0,pairs_best<=12", kept));
        assertFalse(Unlocks.holds("pairs_best>0,pairs_best<=10", kept));
        // A best that was never set is not a low one.
        assertFalse(Unlocks.holds("order_best>0,order_best<=50", kept));
        assertTrue(Unlocks.holds("run_colours&8", kept));
        assertFalse(Unlocks.holds("run_colours&4", kept));
        assertTrue(Unlocks.holds("@colours>=2", kept));
        assertFalse(Unlocks.holds("@colours>=3", kept));
        assertTrue(Unlocks.holds("tic_lost|tic_won>=2", kept));
        assertFalse(Unlocks.holds("tic_lost|tic_won>=4", kept));
        assertFalse(Unlocks.holds("tic_won>=3,tic_lost", kept));
        kept.with("run_stars_2", 3).with("run_stars_5", 1).with("run_quest_5", 1).with("run_rooms_2", 0b101);
        assertTrue(Unlocks.holds("c(1-6)>=2", kept));
        assertFalse(Unlocks.holds("c(3-4)>=1", kept));
        assertTrue(Unlocks.holds("t(1-6)=1", kept));
        assertTrue(Unlocks.holds("s(1-100)=4", kept));
        assertTrue(Unlocks.holds("q(4-30)>=1", kept));
        assertTrue(Unlocks.holds("r(1-100)=2", kept));
    }

    @Test public void whatIsUnlockedIsKeptAndIsNotUnlockedTwice() {
        Kept kept = new Kept().with("tic_won", 1);
        assertEquals(1, Unlocks.check(kept, false));
        assertEquals(1, Unlocks.found(kept));
        assertTrue(Unlocks.fresh(kept));
        assertEquals(.7f, Unlocks.share(kept), 0);
        // Nothing new at a second look; and it stays unlocked though what unlocked it is gone.
        assertEquals(0, Unlocks.check(kept, false));
        kept.with("tic_won", 0);
        assertEquals(0, Unlocks.check(kept, false));
        assertEquals(1, Unlocks.found(kept));
        String line = Unlocks.lines(Unlocks.TIC)[0][1];
        assertEquals(line, Unlocks.freshLine(kept, new Random(1)));
        assertFalse(Unlocks.fresh(kept));
        assertNull(Unlocks.freshLine(kept, new Random(1)));
        assertEquals(line, Unlocks.anyLine(kept, new Random(1)));
        assertTrue(Unlocks.share(kept) > 0 && Unlocks.share(kept) < .1f);
        // More play, more lines: only the new ones are counted.
        kept.with("tic_won", 3).with("tic_lost", 1);
        // Two lines (a third win, a first loss) and the trick that three wins unlock.
        assertEquals(3, Unlocks.check(kept, false));
        assertEquals(3, Unlocks.found(kept));
        assertEquals(1, Unlocks.tricksFound(kept));
    }

    @Test public void aNewTrickIsPlayedNextAndOnlyOnce() {
        Kept kept = new Kept().with("chess_won", 1);
        assertTrue(Unlocks.tricks(kept).isEmpty());
        Unlocks.check(kept, false);
        List<Integer> tricks = Unlocks.tricks(kept);
        assertEquals(1, tricks.size());
        assertEquals(Unlocks.FIRST_TRICK + Unlocks.KNIGHT, (int) tricks.get(0));
        assertTrue(Unlocks.isTrick(tricks.get(0)) && PlayStage.isPrank(tricks.get(0)));
        assertEquals(1, Unlocks.tricksFound(kept));
        assertEquals(Unlocks.FIRST_TRICK + Unlocks.KNIGHT, Unlocks.freshTrick(kept));
        assertEquals(0, Unlocks.freshTrick(kept));
        assertEquals(1, Unlocks.tricks(kept).size());
        // No number of one of her older pranks, or of a game, is one of these.
        for (int game = 0; game < Unlocks.FIRST_TRICK; game++) assertFalse(Unlocks.isTrick(game));
    }

    @Test public void nothingOfVolumeThreeIsSaidBeforeItIsReached() {
        // Everything that the first two volumes and every other game can give, and nothing of the third.
        Kept kept = new Kept().with("run_open", 80).with("run_colours", 127).with("run_bank", 5000).with("run_best", 20);
        for (int land = 1; land <= 80; land++) kept.with("run_stars_" + land, 3).with("run_quest_" + land, 1).with("run_rooms_" + land, 3).with("run_roomsdone_" + land, 3).with("run_prize_" + land, 1);
        for (int which = 0; which < 7; which++) kept.with("run_up_" + which, 2);
        Unlocks.check(kept, false);
        String had = kept.note("unlock_run");
        String[][] lines = Unlocks.lines(Unlocks.RUN);
        int said = 0;
        for (int place = 0; place < lines.length; place++) {
            boolean later = lines[place][0].matches(".*(run_stars_(8[1-9]|9\\d|100)|c\\(81-100\\)|run_open>=8[1-9]|run_colours&128|run_ending).*");
            boolean unlocked = place < had.length() && had.charAt(place) == '1';
            if (later) assertFalse(lines[place][1], unlocked);
            else assertTrue(lines[place][0], unlocked);
            if (unlocked) said++;
            // Anything that speaks of the dragon by name, or of what she lost, waits for the third volume.
            String words = lines[place][1];
            if (words.contains("Lucy") || words.contains("dragon's") || words.contains("Onii") || words.contains("Translation Killer")) assertTrue(words, later);
        }
        assertTrue(said >= 240);
        assertEquals(lines.length - said, 60);
        List<Integer> tricks = Unlocks.tricks(kept);
        assertFalse(tricks.contains(Unlocks.FIRST_TRICK + Unlocks.RETREAT));
        assertFalse(tricks.contains(Unlocks.FIRST_TRICK + Unlocks.LOST_WORDS));
        // And with the endings seen, their tricks are unlocked, but are not played until they are drawn.
        kept.with("run_ending", 3);
        Unlocks.check(kept, false);
        assertTrue(kept.note("unlock_tricks").charAt(Unlocks.RETREAT) == '1' && kept.note("unlock_tricks").charAt(Unlocks.LOST_WORDS) == '1');
        assertFalse(Unlocks.tricks(kept).contains(Unlocks.FIRST_TRICK + Unlocks.RETREAT));
    }

    @Test public void aLongSittingLeavesOnlySoManyLinesWaiting() {
        Kept kept = new Kept().with("run_open", 80);
        for (int land = 1; land <= 80; land++) kept.with("run_stars_" + land, 3);
        int unlocked = Unlocks.check(kept, false);
        assertTrue(unlocked > 100);
        String waiting = kept.note("unlock_fresh");
        assertNotNull(waiting);
        assertTrue(waiting.split(",").length <= 40);
        // They are from all through what was unlocked, not only its beginning.
        Set<String> said = new HashSet<>();
        Random random = new Random(3);
        for (String line = Unlocks.freshLine(kept, random); line != null; line = Unlocks.freshLine(kept, random)) said.add(line);
        assertTrue(said.size() >= 30);
        assertTrue(said.contains(Unlocks.lines(Unlocks.RUN)[0][1]));
    }

    @Test public void inTestingEverythingCanBeUnlockedAndLockedAgain() {
        Kept kept = new Kept().with("unlock_all", 1);
        // Not in a build that is not for testing.
        assertEquals(0, Unlocks.check(kept, false));
        assertEquals(Unlocks.lines() + Unlocks.TRICKS, Unlocks.check(kept, true));
        assertEquals(Unlocks.lines(), Unlocks.found(kept));
        assertEquals(Unlocks.TRICKS - 2, Unlocks.tricks(kept).size());
        kept.with("unlock_all", 0).with("unlock_none", 1);
        assertEquals(0, Unlocks.check(kept, true));
        assertEquals(0, Unlocks.found(kept));
        assertTrue(Unlocks.tricks(kept).isEmpty());
        assertFalse(Unlocks.fresh(kept));
    }
}
