package com.coral.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class FightTest {
    private static final float STEP = 1 / 60f;

    /** A fight with both sides played by hand, standing so far apart about the middle of the stage. */
    private static Fight apart(int left, int right, float gap) {
        Fight fight = new Fight(left, right, -1, -1, 1);
        fight.fighters[0].x = Fight.WIDE / 2 - gap / 2;
        fight.fighters[1].x = Fight.WIDE / 2 + gap / 2;
        return fight;
    }

    private static void hold(Fight fight, float seconds, Fight.Pad pad, Fight.Pad other) {
        for (float gone = 0; gone < seconds && fight.phase() != Fight.MATCH_OVER; gone += STEP) fight.step(STEP, pad, other);
    }

    private static void rest(Fight fight, float seconds) { hold(fight, seconds, new Fight.Pad(), null); }

    /** One press of a button, let go again, and then some time for what it began to finish. */
    private static void press(Fight fight, int move, float after) {
        Fight.Pad pad = new Fight.Pad();
        pad.light = move == Fight.LIGHT;
        pad.heavy = move == Fight.HEAVY;
        pad.special = move == Fight.SPECIAL || move == Fight.ULTIMATE;
        fight.step(STEP, pad, null);
        rest(fight, after);
    }

    private static float hurt(int fighter, int move) { return Fight.MOVES[fighter][move].damage; }

    private static float lost(Fight fight, int side) { return Fight.fullHealth(fight.fighters[side].kind) - fight.fighters[side].health; }

    // The rules

    @Test public void aQuickBlowLandsUpCloseAndNotFromAcrossTheStage() {
        Fight near = apart(Fight.HALGAKOS, Fight.CHIKUWA, 1.2f);
        press(near, Fight.LIGHT, .15f);
        assertEquals(hurt(Fight.HALGAKOS, Fight.LIGHT), lost(near, 1), .01f);
        assertEquals(Fight.HIT, near.fighters[1].state);
        assertEquals(1, near.hits());
        rest(near, 1);
        assertEquals(Fight.IDLE, near.fighters[1].state);
        // One press, one blow: holding the button does not repeat it.
        Fight.Pad held = new Fight.Pad();
        held.light = true;
        Fight holding = apart(Fight.HALGAKOS, Fight.CHIKUWA, 1.2f);
        hold(holding, 2, held, null);
        assertEquals(1, holding.moves());

        Fight far = apart(Fight.HALGAKOS, Fight.CHIKUWA, 4);
        press(far, Fight.LIGHT, .6f);
        assertEquals(0, lost(far, 1), .01f);
        assertEquals(0, far.hits());
    }

    @Test public void aGuardTakesMostOfABlowAndFillsHerMeter() {
        Fight fight = apart(Fight.TSUBAKIEH, Fight.RUNE, 1.3f);
        Fight.Pad strike = new Fight.Pad(), guard = new Fight.Pad();
        guard.block = true;
        hold(fight, .1f, strike, guard);
        strike.heavy = true;
        hold(fight, .6f, strike, guard);
        assertEquals(1, fight.blocks());
        assertEquals(0, fight.hits());
        assertEquals(hurt(Fight.TSUBAKIEH, Fight.HEAVY) * .2f, lost(fight, 1), .01f);
        assertEquals(Fight.BLOCK, fight.fighters[1].state);
        assertTrue(fight.fighters[1].meter > 0);
        // It pushed her back.
        assertTrue(fight.fighters[1].x > Fight.WIDE / 2 + .65f);
    }

    @Test public void aHeavyBlowOfChikuwasKnocksDownAndSheGetsUpUnhurtForAMoment() {
        Fight fight = apart(Fight.CHIKUWA, Fight.HALGAKOS, 1.3f);
        press(fight, Fight.HEAVY, .4f);
        assertEquals(hurt(Fight.CHIKUWA, Fight.HEAVY), lost(fight, 1), .01f);
        assertEquals(Fight.DOWN, fight.fighters[1].state);
        assertEquals(1, fight.falls());
        rest(fight, 1.5f);
        assertTrue(fight.fighters[1].state == Fight.RISE || fight.fighters[1].state == Fight.IDLE);
        assertTrue(fight.fighters[1].safe > 0);
        // Hit again while she cannot be: nothing.
        fight.fighters[0].x = fight.fighters[1].x - 1.2f;
        press(fight, Fight.LIGHT, .1f);
        assertEquals(hurt(Fight.CHIKUWA, Fight.HEAVY), lost(fight, 1), .01f);
    }

    @Test public void aJumpGoesOverWhatIsThrownLow() {
        Fight fight = apart(Fight.RUNE, Fight.TSUBAKIEH, 6);
        fight.fighters[0].meter = Fight.METER;
        Fight.Pad flood = new Fight.Pad(), other = new Fight.Pad();
        flood.special = true;
        fight.step(STEP, flood, other);
        flood.special = false;
        // The flood rolls along the floor: she jumps as it reaches her and it passes under.
        for (int step = 0; step < 400; step++) {
            boolean near = false;
            for (Fight.Shot shot : fight.shots()) near |= Math.abs(shot.x - fight.fighters[1].x) < 2.5f;
            other.jump = near && fight.fighters[1].state != Fight.JUMP;
            fight.step(STEP, flood, other);
        }
        assertEquals(0, lost(fight, 1), .01f);

        Fight stood = apart(Fight.RUNE, Fight.TSUBAKIEH, 6);
        stood.fighters[0].meter = Fight.METER;
        press(stood, Fight.ULTIMATE, 5);
        assertEquals(Fight.damageOf(Fight.FLOOD) * 3, lost(stood, 1), .01f);
    }

    @Test public void anUltimateNeedsAFullMeterAndHoldsTheFightWhileItIsAnnounced() {
        Fight fight = apart(Fight.HALGAKOS, Fight.ILLYA, 7);
        // Without the meter the button is only her special.
        press(fight, Fight.SPECIAL, .05f);
        assertEquals(Fight.FIGHTING, fight.phase());
        assertEquals(Fight.SPECIAL, fight.fighters[0].move);
        rest(fight, 2);
        float before = lost(fight, 1);
        fight.fighters[0].meter = Fight.METER;
        assertTrue(fight.fighters[0].charged());
        Fight.Pad pad = new Fight.Pad();
        pad.special = true;
        fight.step(STEP, pad, null);
        assertEquals(Fight.FLOURISH, fight.phase());
        assertEquals(0, fight.flourisher());
        assertEquals(0, fight.fighters[0].meter, .01f);
        float clock = fight.left();
        rest(fight, Fight.FLOURISH_SECONDS - .1f);
        assertEquals(Fight.FLOURISH, fight.phase());
        // Nothing moves on while it is announced, the round's clock least of all.
        assertEquals(clock, fight.left(), .001f);
        rest(fight, 2.5f);
        assertEquals(Fight.FIGHTING, fight.phase());
        // Her translation reaches the whole stage, in three chunks.
        assertEquals(hurt(Fight.HALGAKOS, Fight.ULTIMATE) * 3, lost(fight, 1) - before, .01f);
    }

    @Test public void nothingStopsAnUltimateOnceItIsBegun() {
        Fight fight = apart(Fight.ILLYA, Fight.HALGAKOS, 5);
        fight.fighters[0].meter = Fight.METER;
        Fight.Pad grand = new Fight.Pad(), scan = new Fight.Pad();
        // Her scan is in the air when the ultimate is begun, and reaches her as it is let fly.
        scan.special = true;
        fight.step(STEP, grand, scan);
        scan.special = false;
        hold(fight, .25f, grand, scan);
        grand.special = true;
        fight.step(STEP, grand, scan);
        grand.special = false;
        assertEquals(Fight.FLOURISH, fight.phase());
        hold(fight, 4, grand, scan);
        assertEquals(0, lost(fight, 0), .01f);
        assertEquals(hurt(Fight.ILLYA, Fight.ULTIMATE) * 4, lost(fight, 1), .01f);
    }

    @Test public void aSpecialMustRestBeforeItIsUsedAgain() {
        Fight fight = apart(Fight.ILLYA, Fight.CHIKUWA, 8);
        press(fight, Fight.SPECIAL, .45f);
        press(fight, Fight.SPECIAL, .1f);
        assertEquals(1, fight.throwsMade());
        rest(fight, .5f);
        press(fight, Fight.SPECIAL, .3f);
        assertEquals(2, fight.throwsMade());
    }

    // Each one's way of fighting

    @Test public void halgakosIsAFighterOfTheMiddleHerScanDoesNotCrossTheStage() {
        Fight far = apart(Fight.HALGAKOS, Fight.CHIKUWA, 8.5f);
        press(far, Fight.SPECIAL, 1.5f);
        assertEquals(1, far.throwsMade());
        assertEquals(0, lost(far, 1), .01f);
        Fight middle = apart(Fight.HALGAKOS, Fight.CHIKUWA, 4.5f);
        press(middle, Fight.SPECIAL, 1.5f);
        assertEquals(Fight.damageOf(Fight.PANEL), lost(middle, 1), .01f);
        // Her key thrust reaches further than her key ring.
        assertTrue(Fight.MOVES[Fight.HALGAKOS][Fight.HEAVY].reach > Fight.MOVES[Fight.HALGAKOS][Fight.LIGHT].reach);
        assertEquals(Fight.PANEL, Fight.MOVES[Fight.HALGAKOS][Fight.SPECIAL].shot);
    }

    @Test public void chikuwaIsSturdiestAndHitsHardestUpCloseAndThrowsBread() {
        for (int other = 0; other < Fight.FIGHTERS; other++) {
            if (other == Fight.CHIKUWA) continue;
            assertTrue(Fight.fullHealth(Fight.CHIKUWA) > Fight.fullHealth(other));
            assertTrue(Fight.MOVES[Fight.CHIKUWA][Fight.HEAVY].damage > Fight.MOVES[other][Fight.HEAVY].damage);
        }
        Fight fight = apart(Fight.CHIKUWA, Fight.RUNE, 5);
        press(fight, Fight.SPECIAL, .3f);
        assertEquals(Fight.BREAD, fight.shots().get(0).kind);
        rest(fight, 1.5f);
        assertEquals(Fight.damageOf(Fight.BREAD), lost(fight, 1), .01f);
    }

    @Test public void tsubakiehsBatComesBackAndSheHitsSofterWithoutIt() {
        Fight fight = apart(Fight.TSUBAKIEH, Fight.ILLYA, 9);
        press(fight, Fight.SPECIAL, .3f);
        assertEquals(Fight.BAT, fight.shots().get(0).kind);
        assertTrue(fight.fighters[0].unarmed);
        float furthest = 0;
        for (int step = 0; step < 240 && !fight.shots().isEmpty(); step++) {
            furthest = Math.max(furthest, fight.shots().get(0).x - fight.fighters[0].x);
            fight.step(STEP, new Fight.Pad(), null);
        }
        // It went out some way, never as far as she stood, and came back to her hand.
        assertTrue("went " + furthest, furthest > 4.5f && furthest < 8);
        assertTrue(fight.shots().isEmpty());
        assertFalse(fight.fighters[0].unarmed);
        assertEquals(0, lost(fight, 1), .01f);

        // Thrown at someone near, it can land on its way out and again on its way back.
        Fight near = apart(Fight.TSUBAKIEH, Fight.CHIKUWA, 3);
        press(near, Fight.SPECIAL, 3);
        assertTrue("lost " + lost(near, 1), lost(near, 1) == Fight.damageOf(Fight.BAT) || lost(near, 1) == Fight.damageOf(Fight.BAT) * 2);
        // With the bat out of her hands a swing is half a swing.
        Fight without = apart(Fight.TSUBAKIEH, Fight.CHIKUWA, .95f);
        without.fighters[0].unarmed = true;
        press(without, Fight.LIGHT, .2f);
        assertEquals(hurt(Fight.TSUBAKIEH, Fight.LIGHT) / 2, lost(without, 1), .01f);
        // And she has nothing that is only thrown but the bat.
        assertEquals(Fight.NOTHING, Fight.MOVES[Fight.TSUBAKIEH][Fight.HEAVY].shot);
        assertEquals(Fight.NOTHING, Fight.MOVES[Fight.TSUBAKIEH][Fight.ULTIMATE].shot);
    }

    @Test public void everyThirdThingRuneThrowsIsHerKnife() {
        Fight fight = apart(Fight.RUNE, Fight.CHIKUWA, 9);
        int[] kinds = new int[6];
        for (int thrown = 0; thrown < kinds.length; thrown++) {
            press(fight, Fight.SPECIAL, .25f);
            kinds[thrown] = fight.shots().get(fight.shots().size() - 1).kind;
            rest(fight, 2.2f);
        }
        assertEquals(Fight.TEAR, kinds[0]);
        assertEquals(Fight.TEAR, kinds[1]);
        assertEquals(Fight.KNIFE, kinds[2]);
        assertEquals(Fight.TEAR, kinds[3]);
        assertEquals(Fight.KNIFE, kinds[5]);
        // A tear lands from the far side of the stage.
        assertTrue(lost(fight, 1) >= Fight.damageOf(Fight.TEAR));
    }

    @Test public void illyaHasOnlyWhatSheThrowsAndAPushToGetThemOffHer() {
        assertTrue(Fight.MOVES[Fight.ILLYA][Fight.HEAVY].shot != Fight.NOTHING);
        assertTrue(Fight.MOVES[Fight.ILLYA][Fight.SPECIAL].shot != Fight.NOTHING);
        Fight fight = apart(Fight.ILLYA, Fight.TSUBAKIEH, 1.1f);
        float stood = fight.fighters[1].x;
        press(fight, Fight.LIGHT, .8f);
        assertEquals(hurt(Fight.ILLYA, Fight.LIGHT), lost(fight, 1), .01f);
        assertTrue("pushed " + (fight.fighters[1].x - stood), fight.fighters[1].x - stood > 1);
        // Her heart beam crosses the whole stage, and her grand compliment lands four times.
        Fight far = apart(Fight.ILLYA, Fight.TSUBAKIEH, 9);
        press(far, Fight.SPECIAL, 1.2f);
        assertEquals(Fight.damageOf(Fight.HEART), lost(far, 1), .01f);
        far.fighters[0].meter = Fight.METER;
        press(far, Fight.ULTIMATE, 3.5f);
        assertEquals(Fight.damageOf(Fight.HEART) + hurt(Fight.ILLYA, Fight.ULTIMATE) * 4, lost(far, 1), .01f);
    }

    @Test public void twoThingsThrownThatMeetAreBothSpent() {
        Fight fight = apart(Fight.ILLYA, Fight.RUNE, 8);
        Fight.Pad one = new Fight.Pad(), other = new Fight.Pad();
        one.special = true;
        other.special = true;
        fight.step(STEP, one, other);
        hold(fight, 2, new Fight.Pad(), new Fight.Pad());
        assertEquals(2, fight.throwsMade());
        assertTrue(fight.shots().isEmpty());
        assertEquals(0, lost(fight, 0), .01f);
        assertEquals(0, lost(fight, 1), .01f);
    }

    // Rounds

    @Test public void twoRoundsWinTheFightAndEachRoundStartsThemFresh() {
        Fight fight = apart(Fight.HALGAKOS, Fight.RUNE, 1.2f);
        fight.fighters[1].health = 3;
        press(fight, Fight.LIGHT, .2f);
        assertEquals(Fight.ROUND_OVER, fight.phase());
        assertEquals(0, fight.lastWinner());
        assertEquals(1, fight.fighters[0].wins);
        assertEquals(-1, fight.winner());
        rest(fight, Fight.BETWEEN + .2f);
        assertEquals(Fight.FIGHTING, fight.phase());
        assertEquals(2, fight.round());
        assertEquals(0, lost(fight, 1), .01f);
        assertEquals(Fight.ROUND_SECONDS, fight.left(), .5f);
        fight.fighters[0].x = fight.fighters[1].x - 1.2f;
        fight.fighters[1].health = 3;
        press(fight, Fight.LIGHT, Fight.BETWEEN + .5f);
        assertEquals(Fight.MATCH_OVER, fight.phase());
        assertEquals(0, fight.winner());
    }

    @Test public void whenTheTimeRunsOutWhoeverHasMoreLeftTakesTheRound() {
        Fight level = apart(Fight.HALGAKOS, Fight.CHIKUWA, 6);
        rest(level, Fight.ROUND_SECONDS + .2f);
        assertEquals(Fight.ROUND_OVER, level.phase());
        assertEquals(-1, level.lastWinner());
        assertEquals(0, level.fighters[0].wins + level.fighters[1].wins);

        Fight ahead = apart(Fight.HALGAKOS, Fight.CHIKUWA, 6);
        ahead.fighters[0].health -= 10;
        rest(ahead, Fight.ROUND_SECONDS + .2f);
        assertEquals(1, ahead.lastWinner());
        assertEquals(1, ahead.fighters[1].wins);
    }

    // The side that plays itself

    /** Plays a fight out with both sides playing themselves, and says who won; the two must stay on the stage throughout. */
    private static int played(int left, int right, int leftLevel, int rightLevel, long seed) {
        Fight fight = new Fight(left, right, leftLevel, rightLevel, seed);
        Fight.Pad nothing = new Fight.Pad();
        for (int step = 0; step < 60 * 600 && fight.phase() != Fight.MATCH_OVER; step++) {
            fight.step(STEP, nothing);
            for (Fight.Fighter fighter : fight.fighters) {
                assertTrue("off the stage at " + fighter.x, fighter.x >= .69f && fighter.x <= Fight.WIDE - .69f);
                assertTrue("health " + fighter.health + " meter " + fighter.meter, fighter.health >= 0 && fighter.meter >= 0 && fighter.meter <= Fight.METER);
            }
            assertTrue("apart by " + Math.abs(fight.fighters[0].x - fight.fighters[1].x), Math.abs(fight.fighters[0].x - fight.fighters[1].x) <= 9.05f);
        }
        assertEquals("a fight that never ended: " + left + " against " + right, Fight.MATCH_OVER, fight.phase());
        return fight.winner();
    }

    @Test public void leftAloneEveryFighterIsBeatenByOneWhoPlaysHerself() {
        for (int kind = 0; kind < Fight.FIGHTERS; kind++) {
            Fight fight = new Fight(Fight.HALGAKOS, kind, -1, 1, 7 + kind);
            Fight.Pad nothing = new Fight.Pad();
            for (int step = 0; step < 60 * 400 && fight.phase() != Fight.MATCH_OVER; step++) fight.step(STEP, nothing);
            assertEquals(FightCast.NAMES[kind], 1, fight.winner());
        }
    }

    @Test public void theBestPlayBeatsTheWorstNearlyAlwaysAndTheWorstCanBeBeaten() {
        int won = 0, fights = 0;
        for (int left = 0; left < Fight.FIGHTERS; left++) {
            for (int right = 0; right < Fight.FIGHTERS; right++) {
                for (long seed = 1; seed <= 2; seed++) {
                    fights++;
                    if (played(left, right, Fight.LEVELS - 1, 0, seed * 31 + left * 7 + right) == 0) won++;
                }
            }
        }
        assertTrue("the best won " + won + " of " + fights, won >= fights * .85f);
    }

    @Test public void atTheSameLevelEveryFighterWinsSomeAndLosesSome() {
        int[] won = new int[Fight.FIGHTERS], fought = new int[Fight.FIGHTERS];
        for (int left = 0; left < Fight.FIGHTERS; left++) {
            for (int right = 0; right < Fight.FIGHTERS; right++) {
                if (left == right) continue;
                for (long seed = 1; seed <= 6; seed++) {
                    int winner = played(left, right, 2, 2, seed * 131 + left * 17 + right * 5);
                    fought[left]++;
                    fought[right]++;
                    won[winner == 0 ? left : right]++;
                }
            }
        }
        StringBuilder table = new StringBuilder();
        for (int kind = 0; kind < Fight.FIGHTERS; kind++) table.append(FightCast.NAMES[kind]).append(' ').append(won[kind]).append('/').append(fought[kind]).append("  ");
        System.out.println("BALANCE " + table);
        for (int kind = 0; kind < Fight.FIGHTERS; kind++) {
            assertTrue(table.toString(), won[kind] >= fought[kind] * .25f && won[kind] <= fought[kind] * .75f);
        }
    }

    // Their lines

    @Test public void everyFighterHasHerTenLinesAndHalgakosEndsHersOnHerCatchphrase() {
        Set<String> names = new HashSet<>();
        assertEquals(Fight.FIGHTERS, FightCast.LINES.length);
        assertEquals(Fight.FIGHTERS, FightCast.NAMES.length);
        for (int fighter = 0; fighter < Fight.FIGHTERS; fighter++) {
            assertEquals(10, FightCast.LINES[fighter].length);
            assertEquals(4, FightCast.MOVES[fighter].length);
            for (String[] line : FightCast.LINES[fighter]) {
                assertTrue("a name used twice: " + line[0], names.add(line[0]));
                assertTrue(line[0], line[0].matches("[a-z0-9]+") && line[0].startsWith(FightCast.LETTERS[fighter]));
                if (fighter == Fight.HALGAKOS) {
                    assertTrue("does not end on Sloppy~♡: " + line[1], line[1].endsWith(" " + Chatter.SLOPPY));
                    assertFalse("a comma before her catchphrase: " + line[1], line[1].endsWith(", " + Chatter.SLOPPY));
                    assertEquals("more than one heart: " + line[1], line[1].indexOf('♡'), line[1].lastIndexOf('♡'));
                } else {
                    assertFalse(line[1], line[1].contains("Sloppy"));
                }
            }
        }
        // What Halgakos cries when she loses is the cry she keeps for when her output is gone.
        assertEquals(Chatter.OUTPUT, FightCast.line(Fight.HALGAKOS, FightCast.LOSE)[1]);
    }
}
