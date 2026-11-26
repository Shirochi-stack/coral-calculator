package com.coral.calculator;

import static org.junit.Assert.assertArrayEquals;
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
        pad.special = move == Fight.SPECIAL;
        pad.ultimate = move == Fight.ULTIMATE;
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
        flood.ultimate = true;
        fight.step(STEP, flood, other);
        flood.ultimate = false;
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
        // Without the meter its button does nothing, and her special is only her special, with or without it.
        press(fight, Fight.ULTIMATE, .05f);
        assertEquals(Fight.FIGHTING, fight.phase());
        assertEquals(0, fight.moves());
        fight.fighters[0].meter = Fight.METER;
        press(fight, Fight.SPECIAL, .05f);
        assertEquals(Fight.FIGHTING, fight.phase());
        assertEquals(Fight.SPECIAL, fight.fighters[0].move);
        rest(fight, 2);
        float before = lost(fight, 1);
        fight.fighters[0].meter = Fight.METER;
        assertTrue(fight.fighters[0].charged());
        Fight.Pad pad = new Fight.Pad();
        pad.ultimate = true;
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
        grand.ultimate = true;
        fight.step(STEP, grand, scan);
        grand.ultimate = false;
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

    // Chains

    /** Presses a button again and again, a tenth of a second apart, and says which moves the left side began, in order. */
    private static java.util.List<Integer> mashed(Fight fight, String buttons, float seconds) {
        java.util.List<Integer> begun = new java.util.ArrayList<>();
        Fight.Pad pad = new Fight.Pad();
        int seen = fight.moves();
        for (int frame = 0; frame < seconds * 60; frame++) {
            boolean down = frame % 6 < 3;
            // The button for the move that comes next.
            char which = buttons.charAt(Math.min(buttons.length() - 1, begun.size()));
            pad.light = down && which == 'q';
            pad.heavy = down && which == 'h';
            fight.step(STEP, pad, null);
            if (fight.moves() != seen) {
                seen = fight.moves();
                begun.add(fight.fighters[0].move);
            }
        }
        return begun;
    }

    @Test public void aBlowPressedAgainInTimeGoesOnThroughAChainOfFiveDifferentBlows() {
        for (int kind = 0; kind < Fight.FIGHTERS; kind++) {
            Fight fight = apart(kind, Fight.CHIKUWA, 1.2f);
            java.util.List<Integer> begun = mashed(fight, "q", 3);
            assertEquals(FightCast.NAMES[kind] + " " + begun, java.util.Arrays.asList(Fight.LIGHT, Fight.QUICK2, Fight.QUICK2 + 1, Fight.QUICK2 + 2, Fight.QUICK5),
                    begun.subList(0, 5));
            // After the fifth it begins again from the first.
            assertEquals(Fight.LIGHT, (int) begun.get(5));
            // Each is its own move, and the fifth is the hardest and knocks down.
            Fight.Move[] moves = Fight.MOVES[kind];
            assertTrue(moves[Fight.QUICK5].floors);
            assertTrue(moves[Fight.QUICK5].damage > moves[Fight.LIGHT].damage * 1.5f);
            for (int step = Fight.QUICK2; step <= Fight.QUICK5; step++) assertFalse(moves[step - 1].floors && step - 1 != Fight.ULTIMATE);
        }
        // Up close every one of Halgakos's five lands, each taking off less than it would alone, and the last throws her down.
        Fight fight = apart(Fight.HALGAKOS, Fight.CHIKUWA, 1.2f);
        Fight.Pad pad = new Fight.Pad();
        int most = 0;
        for (int frame = 0; frame < 120 && fight.falls() == 0; frame++) {
            pad.light = frame % 6 < 3;
            fight.step(STEP, pad, null);
            most = Math.max(most, fight.fighters[0].combo);
        }
        assertEquals(5, most);
        assertEquals(1, fight.falls());
        float alone = 0;
        for (int move : new int[]{Fight.LIGHT, Fight.QUICK2, Fight.QUICK2 + 1, Fight.QUICK2 + 2, Fight.QUICK5}) alone += hurt(Fight.HALGAKOS, move);
        assertEquals(fight.fighters[0].comboDamage, lost(fight, 1), .01f);
        assertTrue("lost " + lost(fight, 1) + " of " + alone, lost(fight, 1) < alone * .8f && lost(fight, 1) > alone * .5f);
    }

    @Test public void quickAndHeavyBlowsMayBeMixedInAChainAndAPauseEndsIt() {
        Fight fight = apart(Fight.TSUBAKIEH, Fight.CHIKUWA, 1.2f);
        assertEquals(java.util.Arrays.asList(Fight.LIGHT, Fight.HEAVY2, Fight.QUICK2 + 1, Fight.HEAVY2 + 2, Fight.HEAVY5), mashed(fight, "qhqhh", 4).subList(0, 5));
        Fight heavy = apart(Fight.CHIKUWA, Fight.HALGAKOS, 1.2f);
        assertEquals(java.util.Arrays.asList(Fight.HEAVY, Fight.HEAVY2, Fight.HEAVY2 + 1, Fight.HEAVY2 + 2, Fight.HEAVY5), mashed(heavy, "h", 5).subList(0, 5));
        // Left too long, the next press is the first blow again.
        Fight slow = apart(Fight.HALGAKOS, Fight.CHIKUWA, 4);
        press(slow, Fight.LIGHT, .25f + Fight.CHAIN_WINDOW + .1f);
        press(slow, Fight.LIGHT, .05f);
        assertEquals(Fight.LIGHT, slow.fighters[0].move);
        // In time, it is the second.
        Fight quick = apart(Fight.HALGAKOS, Fight.CHIKUWA, 4);
        press(quick, Fight.LIGHT, .25f + Fight.CHAIN_WINDOW - .1f);
        press(quick, Fight.LIGHT, .05f);
        assertEquals(Fight.QUICK2, quick.fighters[0].move);
        assertEquals(2, Fight.chainStep(Fight.QUICK2));
        assertEquals(5, Fight.chainStep(Fight.HEAVY5));
        assertEquals(0, Fight.chainStep(Fight.SPECIAL));
    }

    @Test public void aPressMadeWhileABlowHoldsTheFightStillIsNotLost() {
        Fight fight = apart(Fight.CHIKUWA, Fight.HALGAKOS, 1.2f);
        Fight.Pad pad = new Fight.Pad();
        pad.heavy = true;
        fight.step(STEP, pad, null);
        pad.heavy = false;
        // Her brick bread lands, and everything stops for a moment: the button is tapped and let go inside that moment.
        while (fight.hits() == 0) fight.step(STEP, pad, null);
        float time = fight.left();
        pad.heavy = true;
        fight.step(STEP, pad, null);
        pad.heavy = false;
        assertEquals(time, fight.left(), 0);
        rest(fight, .6f);
        assertEquals(2, fight.moves());
        assertEquals(Fight.HEAVY2, fight.fighters[0].move);
    }

    /** The frame a fighter shows in the middle of a move. */
    private static int shown(int kind, int move, float clock, int landed, int specials) {
        Fight fight = apart(kind, Fight.HALGAKOS, 3);
        Fight.Fighter fighter = fight.fighters[0];
        fighter.state = Fight.ATTACK;
        fighter.move = move;
        fighter.clock = clock;
        fighter.landed = landed;
        fighter.mark = clock;
        fighter.specials = specials;
        return FightPose.of(fight, fighter, 0, 0);
    }

    @Test public void everyFighterHasThirtyAttackFramesAndNoBlowOfAChainLooksLikeAnother() {
        for (int kind = 0; kind < Fight.FIGHTERS; kind++) {
            Fight.Move[] moves = Fight.MOVES[kind];
            Set<Integer> frames = new HashSet<>(), chain = new HashSet<>();
            for (int move = 0; move < Fight.MOVE_COUNT; move++) {
                Fight.Move doing = moves[move];
                int blow = shown(kind, move, doing.startup + .01f, 0, 1);
                frames.add(blow);
                if (Fight.chainStep(move) >= 1 && move != Fight.DASH_LIGHT) assertTrue("a blow of a chain shown twice: " + move, chain.add(blow));
                frames.add(shown(kind, move, .01f, 0, 1));
            }
            assertEquals(10, chain.size());
            // Her special goes through five; the smash comes down and lands; her ultimate and the greater one are gathered, let go and done.
            for (int specials = 1; specials <= 12; specials++) frames.add(shown(kind, Fight.SPECIAL, moves[Fight.SPECIAL].startup + .01f, 1, specials));
            frames.add(shown(kind, Fight.AIR_SPECIAL, 1, 1, 2));
            frames.add(shown(kind, Fight.SMASH, 1, 1, 0));
            for (int grand : new int[]{Fight.ULTIMATE, Fight.CLASH}) {
                frames.add(shown(kind, grand, moves[grand].startup * .8f, 0, 0));
                frames.add(shown(kind, grand, moves[grand].length() - .01f, moves[grand].times, 0));
            }
            assertTrue(FightCast.NAMES[kind] + " has " + frames.size(), frames.size() >= 30);
            for (int frame : frames) assertTrue(frame >= FightFrames.QS && frame < FightFrames.COUNT);
            if (!FightFrames.FULL[kind]) continue;
            // And each of them is a picture of its own.
            Set<String> places = new HashSet<>();
            for (int frame : frames) {
                short[] at = FightFrames.FRAMES[kind][frame];
                assertTrue("two frames in one place: " + frame, places.add(at[0] + " " + at[1] + " " + at[2]));
                assertTrue(at[3] > 40 && at[4] > 40);
            }
        }
    }

    @Test public void aChainPlayedOutShowsFiveBlowsOneAfterAnother() {
        Fight fight = apart(Fight.HALGAKOS, Fight.CHIKUWA, 1.2f);
        Fight.Pad pad = new Fight.Pad();
        java.util.List<Integer> blows = new java.util.ArrayList<>();
        for (int frame = 0; frame < 120 && fight.falls() == 0; frame++) {
            pad.light = frame % 6 < 3;
            fight.step(STEP, pad, null);
            Fight.Fighter her = fight.fighters[0];
            if (her.state != Fight.ATTACK || her.clock < her.doing().startup || her.clock > her.doing().startup + her.doing().active) continue;
            int pose = FightPose.of(fight, her, 0, 0);
            if (blows.isEmpty() || blows.get(blows.size() - 1) != pose) blows.add(pose);
        }
        assertEquals(java.util.Arrays.asList(FightFrames.Q1, FightFrames.Q2, FightFrames.Q3, FightFrames.Q4, FightFrames.Q5), blows);
    }

    // In the air

    @Test public void threeQuickBlowsInTheAirAndNoMore() {
        Fight fight = apart(Fight.TSUBAKIEH, Fight.CHIKUWA, 6);
        Fight.Pad pad = new Fight.Pad();
        pad.jump = true;
        fight.step(STEP, pad, null);
        pad.jump = false;
        Set<Integer> begun = new HashSet<>();
        for (int frame = 0; frame < 150; frame++) {
            pad.light = frame > 6 && frame % 6 < 3;
            fight.step(STEP, pad, null);
            if (fight.fighters[0].state == Fight.ATTACK && fight.fighters[0].y > 0) begun.add(fight.fighters[0].move);
            if (fight.fighters[0].y == 0 && frame > 20) break;
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList(Fight.AIR1, Fight.AIR1 + 1, Fight.AIR3)), begun);
    }

    /** Jumps, and at the top of the jump comes down with a smash; then waits. */
    private static void smash(Fight fight, float after) {
        Fight.Pad pad = new Fight.Pad();
        pad.jump = true;
        fight.step(STEP, pad, null);
        pad.jump = false;
        hold(fight, .3f, pad, null);
        pad.heavy = true;
        fight.step(STEP, pad, null);
        pad.heavy = false;
        hold(fight, after, pad, null);
    }

    @Test public void aSmashComesStraightDownAndThrowsDownWhoeverIsNear() {
        for (int kind = 0; kind < Fight.FIGHTERS; kind++) {
            Fight fight = apart(kind, Fight.HALGAKOS, 1.4f);
            smash(fight, .5f);
            assertEquals(FightCast.NAMES[kind], 1, fight.quakes());
            assertEquals(0, fight.fighters[0].y, 0);
            assertEquals(Fight.SMASH, fight.fighters[0].move);
            assertEquals(1, fight.fighters[0].landed);
            assertEquals(hurt(kind, Fight.SMASH), lost(fight, 1), .01f);
            assertEquals(Fight.DOWN, fight.fighters[1].state);
            assertEquals(fight.fighters[0].x, fight.quakeX(), .001f);
            assertTrue(hurt(kind, Fight.SMASH) >= 12);
        }
    }

    @Test public void aSmashThatMissesLeavesHerOpenForLonger() {
        Fight hit = apart(Fight.CHIKUWA, Fight.HALGAKOS, 1.4f), miss = apart(Fight.CHIKUWA, Fight.HALGAKOS, 7);
        Fight.Pad guard = new Fight.Pad();
        smash(hit, 0);
        smash(miss, 0);
        float gone = 0;
        while (miss.quakes() == 0 || hit.quakes() == 0) {
            hit.step(STEP, guard, null);
            miss.step(STEP, guard, null);
        }
        assertEquals(2, miss.fighters[0].landed);
        assertEquals(0, lost(miss, 1), 0);
        float open = 0, openMissed = 0;
        for (int frame = 0; frame < 120; frame++) {
            hit.step(STEP, guard, null);
            miss.step(STEP, guard, null);
            if (hit.fighters[0].state == Fight.ATTACK) open += STEP;
            if (miss.fighters[0].state == Fight.ATTACK) openMissed += STEP;
        }
        assertTrue(open + " and " + openMissed, openMissed > open + .1f && open > .3f);
        // And while she is, she can be hit.
        Fight punished = apart(Fight.CHIKUWA, Fight.HALGAKOS, 7);
        smash(punished, 0);
        while (punished.quakes() == 0) punished.step(STEP, guard, null);
        punished.fighters[1].x = punished.fighters[0].x + 1.2f;
        Fight.Pad strike = new Fight.Pad();
        strike.light = true;
        hold(punished, .2f, guard, strike);
        assertEquals(hurt(Fight.HALGAKOS, Fight.LIGHT), lost(punished, 0), .01f);
    }

    @Test public void aBlowFromBehindHerGuardBreaksItAndALaunchThrowsHerUpToBeHitAgain() {
        Fight fight = apart(Fight.HALGAKOS, Fight.RUNE, 1.3f);
        Fight.Pad strike = new Fight.Pad(), guard = new Fight.Pad();
        guard.block = true;
        hold(fight, .1f, strike, guard);
        // Heavy with the arrow that points away from her held.
        strike.left = true;
        strike.heavy = true;
        fight.step(STEP, strike, guard);
        assertEquals(Fight.BREAK, fight.fighters[0].move);
        strike.left = false;
        strike.heavy = false;
        hold(fight, .7f, strike, guard);
        assertEquals(0, fight.blocks());
        assertEquals(hurt(Fight.HALGAKOS, Fight.BREAK), lost(fight, 1), .01f);
        assertTrue(Fight.MOVES[Fight.HALGAKOS][Fight.BREAK].startup > Fight.MOVES[Fight.HALGAKOS][Fight.HEAVY].startup);

        Fight up = apart(Fight.HALGAKOS, Fight.RUNE, 1.3f);
        Fight.Pad launch = new Fight.Pad();
        launch.block = true;
        launch.heavy = true;
        up.step(STEP, launch, null);
        assertEquals(Fight.LAUNCH, up.fighters[0].move);
        launch.block = false;
        launch.heavy = false;
        float highest = 0;
        while (up.fighters[0].state == Fight.ATTACK) {
            up.step(STEP, launch, null);
            highest = Math.max(highest, up.fighters[1].y);
        }
        assertTrue("went up " + highest, highest > 1);
        assertEquals(Fight.DOWN, up.fighters[1].state);
        assertTrue(up.fighters[1].y > .25f);
        // Still in the air, she is hit again; it counts as the second of the same run, and takes off less.
        press(up, Fight.LIGHT, .2f);
        assertEquals(2, up.hits());
        assertEquals(2, up.fighters[0].combo);
        assertEquals(hurt(Fight.HALGAKOS, Fight.LAUNCH) + hurt(Fight.HALGAKOS, Fight.LIGHT) * .83f, lost(up, 1), .01f);
    }

    @Test public void anArrowTwiceIsADashOrAHopBackAndABlowOutOfADashIsItsOwn() {
        Fight fight = apart(Fight.TSUBAKIEH, Fight.CHIKUWA, 6);
        Fight.Pad pad = new Fight.Pad();
        float from = fight.fighters[0].x;
        for (int tap = 0; tap < 2; tap++) {
            pad.right = true;
            fight.step(STEP, pad, null);
            fight.step(STEP, pad, null);
            pad.right = false;
            if (tap == 0) fight.step(STEP, pad, null);
        }
        assertEquals(Fight.DASH, fight.fighters[0].state);
        hold(fight, .1f, pad, null);
        pad.light = true;
        fight.step(STEP, pad, null);
        assertEquals(Fight.DASH_LIGHT, fight.fighters[0].move);
        pad.light = false;
        assertTrue(fight.fighters[0].x - from > 1);
        // Away from her it is a hop back.
        Fight back = apart(Fight.TSUBAKIEH, Fight.CHIKUWA, 3);
        from = back.fighters[0].x;
        Fight.Pad away = new Fight.Pad();
        for (int tap = 0; tap < 2; tap++) {
            away.left = true;
            back.step(STEP, away, null);
            away.left = false;
            back.step(STEP, away, null);
        }
        assertEquals(Fight.BACKSTEP, back.fighters[0].state);
        rest(back, .4f);
        assertEquals(Fight.IDLE, back.fighters[0].state);
        assertTrue(from - back.fighters[0].x > 1.5f);
    }

    // The dodge

    /** A heavy swing of Tsubakieh's at someone who dodges so long after it is begun; what it took off her. */
    private static Fight swungAt(float dodgeAt, boolean left) {
        Fight fight = apart(Fight.TSUBAKIEH, Fight.HALGAKOS, 1.3f);
        Fight.Pad strike = new Fight.Pad(), slip = new Fight.Pad();
        strike.heavy = true;
        for (float gone = 0; gone < .8f; gone += STEP) {
            slip.dodge = dodgeAt >= 0 && gone >= dodgeAt && gone < dodgeAt + .05f;
            slip.left = slip.dodge && left;
            fight.step(STEP, strike, slip);
        }
        return fight;
    }

    @Test public void aDodgeInTimeMakesABlowMissAndOneTooEarlyOrTooLateDoesNot() {
        float swing = hurt(Fight.TSUBAKIEH, Fight.HEAVY);
        Fight inTime = swungAt(.2f, false);
        assertEquals(0, lost(inTime, 1), 0);
        assertEquals(0, inTime.hits());
        assertEquals(1, inTime.dodges());
        // Too early: nothing can touch her for a moment, but the moment is over before she can move again.
        Fight early = swungAt(0, false);
        assertEquals(1, early.dodges());
        assertEquals(swing, lost(early, 1), .01f);
        // Too late: the blow is already on her.
        Fight late = swungAt(.32f, false);
        assertEquals(0, late.dodges());
        assertEquals(swing, lost(late, 1), .01f);
        assertEquals(swing, lost(swungAt(-1, false), 1), .01f);
        assertTrue(Fight.DODGE_SAFE < Fight.DODGE_SECONDS);
        // With an arrow held she slips that way, through the other and out behind her.
        Fight through = swungAt(.2f, true);
        assertEquals(0, lost(through, 1), 0);
        assertTrue(through.fighters[1].x < through.fighters[0].x);
    }

    @Test public void aDodgeLetsAThingThrownPassAndMustRestBeforeTheNext() {
        Fight fight = apart(Fight.ILLYA, Fight.CHIKUWA, 5);
        Fight.Pad heart = new Fight.Pad(), slip = new Fight.Pad();
        heart.special = true;
        for (float gone = 0; gone < 1.2f; gone += STEP) {
            slip.dodge = gone >= .34f && gone < .4f;
            fight.step(STEP, heart, slip);
        }
        assertEquals(1, fight.throwsMade());
        assertEquals(0, lost(fight, 1), 0);
        assertTrue(fight.shots().isEmpty());
        // Pressed again at once it does nothing; after its rest it does.
        Fight again = apart(Fight.ILLYA, Fight.CHIKUWA, 5);
        Fight.Pad none = new Fight.Pad();
        for (float gone = 0; gone < 2; gone += STEP) {
            slip.dodge = gone < .05f || gone >= .5f && gone < .55f;
            again.step(STEP, none, slip);
        }
        assertEquals(1, again.dodges());
        assertTrue(Fight.DODGE_REST > Fight.DODGE_SECONDS + .4f);
        slip.dodge = true;
        again.step(STEP, none, slip);
        assertEquals(2, again.dodges());
        assertEquals(Fight.DODGE, again.fighters[1].state);
        // In the air too, once a jump.
        Fight air = apart(Fight.ILLYA, Fight.CHIKUWA, 5);
        Fight.Pad jump = new Fight.Pad();
        jump.jump = true;
        air.step(STEP, none, jump);
        jump.jump = false;
        hold(air, .2f, none, jump);
        jump.dodge = true;
        air.step(STEP, none, jump);
        assertEquals(Fight.DODGE, air.fighters[1].state);
        assertTrue(air.fighters[1].y > 0);
    }

    /** The frames a fighter shows through a dodge begun with an arrow held or none, on the ground or in the air: each once, in order. */
    private static java.util.List<Integer> slipped(int kind, int side, boolean left, boolean right, boolean air) throws Exception {
        Fight fight = side == 0 ? apart(kind, Fight.CHIKUWA, 5) : apart(Fight.CHIKUWA, kind, 5);
        Fight shown = new Fight(fight.fighters[0].kind, fight.fighters[1].kind, -1, -1, 0);
        Fight.Pad pad = new Fight.Pad(), none = new Fight.Pad();
        if (air) {
            pad.jump = true;
            fight.step(STEP, side == 0 ? pad : none, side == 0 ? none : pad);
            pad.jump = false;
            hold(fight, .2f, side == 0 ? pad : none, side == 0 ? none : pad);
        }
        pad.dodge = true;
        pad.left = left;
        pad.right = right;
        java.util.List<Integer> frames = new java.util.ArrayList<>();
        for (int frame = 0; frame < 40; frame++) {
            fight.step(STEP, side == 0 ? pad : none, side == 0 ? none : pad);
            Fight.Fighter her = fight.fighters[side];
            if (her.state != Fight.DODGE) break;
            int pose = FightPose.of(fight, her, 0, 0);
            // A device that is only shown the fight shows the same frame of her.
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            fight.write(new java.io.DataOutputStream(bytes));
            shown.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
            assertEquals(pose, FightPose.of(shown, shown.fighters[side], 0, 0));
            if (frames.isEmpty() || frames.get(frames.size() - 1) != pose) frames.add(pose);
        }
        assertEquals(1, fight.dodges());
        return frames;
    }

    @Test public void aDodgeAwayShowsHerSpringingBackAndADodgeTowardShowsHerSlippingPast() throws Exception {
        java.util.List<Integer> back = java.util.Arrays.asList(FightFrames.BDODGE1, FightFrames.BDODGE2, FightFrames.BDODGE3);
        java.util.List<Integer> forward = java.util.Arrays.asList(FightFrames.FDODGE, FightFrames.SKID);
        java.util.List<Integer> still = java.util.Arrays.asList(FightFrames.DODGE1, FightFrames.DODGE2);
        java.util.List<Integer> air = java.util.Collections.singletonList(FightFrames.ADODGE);
        for (int kind = 0; kind < Fight.FIGHTERS; kind++) {
            String name = FightCast.NAMES[kind];
            // On the left she faces right: left is away. On the right she faces left: right is away.
            assertEquals(name, back, slipped(kind, 0, true, false, false));
            assertEquals(name, forward, slipped(kind, 0, false, true, false));
            assertEquals(name, back, slipped(kind, 1, false, true, false));
            assertEquals(name, forward, slipped(kind, 1, true, false, false));
            assertEquals(name, still, slipped(kind, 0, false, false, false));
            assertEquals(name, still, slipped(kind, 1, false, false, false));
            // In the air it is the dodge of the air, whichever way.
            assertEquals(name, air, slipped(kind, 0, true, false, true));
            assertEquals(name, air, slipped(kind, 0, false, true, true));
            assertEquals(name, air, slipped(kind, 1, false, false, true));
            if (!FightFrames.FULL[kind]) continue;
            // Each of them a picture of its own, and none the hop back's.
            Set<String> places = new HashSet<>();
            for (int frame : new int[]{FightFrames.BDODGE1, FightFrames.BDODGE2, FightFrames.BDODGE3, FightFrames.FDODGE, FightFrames.ADODGE, FightFrames.DODGE1,
                    FightFrames.DODGE2, FightFrames.HOP1, FightFrames.HOP2, FightFrames.SKID}) {
                short[] at = FightFrames.FRAMES[kind][frame];
                assertTrue(name + ": two frames in one place: " + frame, places.add(at[0] + " " + at[1] + " " + at[2]));
                assertTrue(at[3] > 40 && at[4] > 40);
            }
        }
        // She goes the way she was asked: back, away from the other; forward, past her.
        Fight away = apart(Fight.RUNE, Fight.CHIKUWA, 5), toward = apart(Fight.RUNE, Fight.CHIKUWA, 1.2f);
        Fight.Pad pad = new Fight.Pad();
        pad.dodge = true;
        pad.left = true;
        hold(away, Fight.DODGE_SECONDS - .05f, pad, null);
        assertTrue(away.fighters[0].x < Fight.WIDE / 2 - 2.5f - 2);
        assertFalse(away.fighters[0].facingLeft);
        pad.left = false;
        pad.right = true;
        hold(toward, Fight.DODGE_SECONDS - .05f, pad, null);
        assertTrue(toward.fighters[0].x > toward.fighters[1].x);
    }

    // The clash

    /** Two played by hand with their meters full: the left goes for her ultimate, and the right for hers so long after. */
    private static Fight answered(float after) {
        Fight fight = apart(Fight.HALGAKOS, Fight.ILLYA, 5);
        fight.fighters[0].meter = Fight.METER;
        fight.fighters[1].meter = Fight.METER;
        Fight.Pad one = new Fight.Pad(), two = new Fight.Pad();
        one.ultimate = true;
        fight.step(STEP, one, two);
        one.ultimate = false;
        hold(fight, after, one, two);
        two.ultimate = true;
        fight.step(STEP, one, two);
        return fight;
    }

    /** Taps a clash out: so many taps from each side, spread over it. */
    private static void tapped(Fight fight, int left, int right) {
        Fight.Pad one = new Fight.Pad(), two = new Fight.Pad();
        int frames = Math.round(Fight.CLASH_SECONDS * 60) - 4, had = fight.taps(0), hadToo = fight.taps(1);
        for (int frame = 0; frame < frames; frame++) {
            one.taps = (frame + 1) * left / frames - frame * left / frames;
            two.taps = (frame + 1) * right / frames - frame * right / frames;
            fight.step(STEP, one, two);
        }
        assertEquals(Fight.CLASHING, fight.phase());
        assertEquals(had + left, fight.taps(0));
        assertEquals(hadToo + right, fight.taps(1));
        hold(fight, Fight.CLASH_GRACE + .1f, one, two);
    }

    @Test public void twoUltimatesAtOnceClashAndWhoeverTapsFasterHasTheGreaterOne() throws Exception {
        Fight fight = answered(.5f);
        assertEquals(Fight.CLASHING, fight.phase());
        assertEquals(1, fight.clashes());
        assertEquals(1, fight.clashSecond());
        assertEquals(0, fight.fighters[0].meter, 0);
        assertEquals(0, fight.fighters[1].meter, 0);
        assertEquals(Fight.CLASH_SECONDS, fight.clashLeft(), .02f);
        // It is all there for a device that is only shown the fight.
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        Fight.Pad one = new Fight.Pad(), two = new Fight.Pad();
        one.taps = 3;
        two.taps = 1;
        fight.step(STEP, one, two);
        fight.write(new java.io.DataOutputStream(bytes));
        Fight shown = new Fight(Fight.HALGAKOS, Fight.ILLYA, -1, -1, 0);
        shown.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
        assertEquals(Fight.CLASHING, shown.phase());
        assertEquals(3, shown.taps(0));
        assertEquals(1, shown.taps(1));
        assertEquals(1, shown.clashes());
        assertEquals(1, shown.clashSecond());
        assertEquals(fight.clashLeft(), shown.clashLeft(), 0);
        // Nothing moves while they tap.
        float clock = fight.left();
        tapped(fight, 27, 19);
        assertEquals(clock, fight.left(), 0);
        // The left tapped more: hers is the greater ultimate, and the other can only take it.
        assertEquals(Fight.FLOURISH, fight.phase());
        assertEquals(0, fight.flourisher());
        assertEquals(Fight.CLASH, fight.fighters[0].move);
        rest(fight, 4);
        Fight.Move greater = Fight.MOVES[Fight.HALGAKOS][Fight.CLASH], usual = Fight.MOVES[Fight.HALGAKOS][Fight.ULTIMATE];
        assertEquals(greater.damage * greater.times, lost(fight, 1), .01f);
        assertEquals(0, lost(fight, 0), 0);
        assertEquals(1, fight.falls());
        assertFalse(fight.fighters[1].charged());
        assertEquals(1, fight.clashes());
        // It is more than her usual one, and never the whole of anyone's health.
        for (int kind = 0; kind < Fight.FIGHTERS; kind++) {
            Fight.Move grand = Fight.MOVES[kind][Fight.CLASH];
            float most = Fight.MOVES[kind][Fight.ULTIMATE].shot != Fight.NOTHING ? Fight.damageOf(Fight.FLOOD) * 3
                    : Fight.MOVES[kind][Fight.ULTIMATE].damage * Fight.MOVES[kind][Fight.ULTIMATE].times;
            assertTrue(FightCast.NAMES[kind], grand.damage * grand.times > most * 1.2f);
            for (int other = 0; other < Fight.FIGHTERS; other++) assertTrue(grand.damage * grand.times < Fight.fullHealth(other) * .7f);
        }
        assertTrue(greater.times > usual.times);
    }

    @Test public void theBarOfAClashShowsWhoIsWinningAndFillsAtOnceForWhoeverWon() throws Exception {
        // In the middle while they are level; toward the right end as the left leads, and the other way about.
        assertEquals(.5f, Fight.clashBar(0, 0), 0);
        assertEquals(.5f, Fight.clashBar(17, 17), 0);
        for (int lead = 1; lead <= 60; lead++) {
            float at = Fight.clashBar(lead, 0);
            assertTrue(at > Fight.clashBar(lead - 1, 0) || lead > 40);
            assertEquals(1, at + Fight.clashBar(0, lead), .0001f);
            assertEquals(at, Fight.clashBar(lead + 9, 9), .0001f);
            // However far ahead, it is never at the end while they still tap.
            assertTrue(at < .95f && Fight.clashBar(0, lead) > .05f);
        }
        // A tap of a lead can be seen, and a few taps plainly.
        assertTrue(Fight.clashBar(1, 0) - .5f > .04f);
        assertTrue(Fight.clashBar(4, 0) - .5f > .18f);
        assertTrue(Fight.clashBar(12, 0) > .88f);

        Fight fight = answered(.5f), shown = new Fight(Fight.HALGAKOS, Fight.ILLYA, -1, -1, 0);
        assertEquals(-1, fight.clashWinner());
        Fight.Pad one = new Fight.Pad(), two = new Fight.Pad();
        one.taps = 2;
        two.taps = 7;
        fight.step(STEP, one, two);
        assertEquals(Fight.clashBar(2, 7), fight.clashBar(), 0);
        assertTrue(fight.clashBar() < .5f);
        // A device that is only shown the fight has the same bar and the same number.
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        fight.write(new java.io.DataOutputStream(bytes));
        shown.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
        assertEquals(fight.clashBar(), shown.clashBar(), 0);
        assertEquals(fight.clashLeft(), shown.clashLeft(), 0);
        assertEquals(-1, shown.clashWinner());
        // The right taps more and wins: for a moment after, the bar is hers, filling quickly and then full.
        tapped(fight, 10, 21);
        assertEquals(1, fight.clashWinner());
        assertTrue(fight.clashSweep() < 1);
        float before = fight.clashSweep();
        fight.step(STEP, one, two);
        assertTrue(fight.clashSweep() > before);
        bytes.reset();
        fight.write(new java.io.DataOutputStream(bytes));
        shown.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
        assertEquals(1, shown.clashWinner());
        assertEquals(fight.clashSweep(), shown.clashSweep(), 0);
        assertEquals(fight.clashAge(), shown.clashAge(), 0);
        hold(fight, Fight.CLASH_SWEEP, one, two);
        assertEquals(1, fight.clashSweep(), 0);
        assertEquals(1, fight.clashWinner());
        assertTrue(Fight.CLASH_SWEEP < .2f && Fight.CLASH_SHOWN < Fight.FLOURISH_SECONDS / 2);
        // Once her greater ultimate is let go the clash is done with.
        rest(fight, Fight.FLOURISH_SECONDS);
        assertEquals(-1, fight.clashWinner());
        assertEquals(0, fight.clashSweep(), 0);
        // Level, the bar is in the middle to the end, and then it is whoever went second who has it.
        Fight level = answered(.3f);
        tapped(level, 15, 15);
        assertEquals(.5f, level.clashBar(), 0);
        assertEquals(level.clashSecond(), level.clashWinner());
    }

    @Test public void theOtherWinsAClashByTappingMoreAndLevelItGoesToWhoeverWentSecond() {
        Fight fight = answered(.9f);
        assertEquals(Fight.CLASHING, fight.phase());
        tapped(fight, 12, 31);
        assertEquals(1, fight.flourisher());
        assertEquals(Fight.CLASH, fight.fighters[1].move);
        rest(fight, 4);
        assertEquals(0, lost(fight, 1), 0);
        assertEquals(Fight.MOVES[Fight.ILLYA][Fight.CLASH].damage * 5, lost(fight, 0), .01f);
        Fight level = answered(.3f);
        tapped(level, 20, 20);
        assertEquals(1, level.flourisher());
        // A tap in the moment after the time is up still counts.
        Fight last = answered(.3f);
        Fight.Pad one = new Fight.Pad(), two = new Fight.Pad();
        hold(last, Fight.CLASH_SECONDS + .05f, one, two);
        assertEquals(Fight.CLASHING, last.phase());
        one.taps = 1;
        last.step(STEP, one, two);
        hold(last, Fight.CLASH_GRACE, one, two);
        assertEquals(0, last.flourisher());
    }

    @Test public void anUltimateMetBeforeAnyOfItLandsIsAClashAndAfterItIsNot() {
        // While it is announced; and after that, while it is still coming out.
        assertEquals(1, answered(.5f).clashes());
        Fight coming = answered(Fight.FLOURISH_SECONDS + .1f);
        assertEquals(Fight.CLASHING, coming.phase());
        assertEquals(1, coming.clashes());
        assertEquals(1, coming.clashSecond());
        assertEquals(0, lost(coming, 1), 0);
        tapped(coming, 9, 20);
        assertEquals(1, coming.flourisher());
        rest(coming, 4);
        // Neither of the two that met went off: only the greater one of whoever won.
        assertEquals(0, lost(coming, 1), 0);
        assertEquals(Fight.MOVES[Fight.ILLYA][Fight.CLASH].damage * 5, lost(coming, 0), .01f);

        // A flood is thrown and on its way: until it reaches her it can still be met.
        Fight flood = apart(Fight.RUNE, Fight.TSUBAKIEH, 7);
        flood.fighters[0].meter = Fight.METER;
        flood.fighters[1].meter = Fight.METER;
        Fight.Pad one = new Fight.Pad(), two = new Fight.Pad();
        one.ultimate = true;
        flood.step(STEP, one, two);
        one.ultimate = false;
        hold(flood, Fight.FLOURISH_SECONDS + Fight.MOVES[Fight.RUNE][Fight.ULTIMATE].startup + .3f, one, two);
        assertEquals(1, flood.shots().size());
        assertEquals(Fight.FLOOD, flood.shots().get(0).kind);
        assertEquals(Fight.IDLE, flood.fighters[1].state);
        two.ultimate = true;
        flood.step(STEP, one, two);
        two.ultimate = false;
        assertEquals(Fight.CLASHING, flood.phase());
        tapped(flood, 5, 30);
        // The flood is gone with the clash, and she has her greater one.
        assertTrue(flood.shots().isEmpty());
        rest(flood, 4);
        assertEquals(0, lost(flood, 1), 0);
        assertEquals(Fight.MOVES[Fight.TSUBAKIEH][Fight.CLASH].damage * 5, lost(flood, 0), .01f);

        // She need not be standing ready: in the middle of a jump she may meet it all the same.
        Fight jumping = apart(Fight.CHIKUWA, Fight.HALGAKOS, 6);
        jumping.fighters[0].meter = Fight.METER;
        jumping.fighters[1].meter = Fight.METER;
        one.ultimate = true;
        jumping.step(STEP, one, two);
        one.ultimate = false;
        hold(jumping, Fight.FLOURISH_SECONDS, one, two);
        two.jump = true;
        hold(jumping, .15f, one, two);
        two.jump = false;
        assertEquals(Fight.JUMP, jumping.fighters[1].state);
        two.ultimate = true;
        jumping.step(STEP, one, two);
        two.ultimate = false;
        assertEquals(Fight.CLASHING, jumping.phase());

        // Once the first of it has landed, even on her guard, it is too late: hers is simply begun in its turn.
        Fight late = apart(Fight.HALGAKOS, Fight.ILLYA, 5);
        late.fighters[0].meter = Fight.METER;
        late.fighters[1].meter = Fight.METER;
        one.ultimate = true;
        two.block = true;
        late.step(STEP, one, two);
        one.ultimate = false;
        hold(late, Fight.FLOURISH_SECONDS + Fight.MOVES[Fight.HALGAKOS][Fight.ULTIMATE].startup + .1f, one, two);
        assertEquals(1, late.blocks());
        two.block = false;
        two.ultimate = true;
        late.step(STEP, one, two);
        two.ultimate = false;
        assertEquals(0, late.clashes());
        assertEquals(Fight.FLOURISH, late.phase());
        assertEquals(1, late.flourisher());
        assertEquals(Fight.ULTIMATE, late.fighters[1].move);
        // And a flood that has already rolled into her is not to be met either.
        Fight wet = apart(Fight.RUNE, Fight.TSUBAKIEH, 3);
        wet.fighters[0].meter = Fight.METER;
        wet.fighters[1].meter = Fight.METER;
        one.ultimate = true;
        two.block = true;
        wet.step(STEP, one, two);
        one.ultimate = false;
        for (int frame = 0; frame < 300 && wet.blocks() == 0; frame++) wet.step(STEP, one, two);
        assertEquals(1, wet.blocks());
        two.block = false;
        two.ultimate = true;
        wet.step(STEP, one, two);
        assertEquals(0, wet.clashes());
    }

    @Test public void oneWhoPlaysHerselfTapsFasterTheBetterSheIsAndCanBeOutTappedAtHerBest() {
        int[] tapped = new int[Fight.LEVELS];
        for (int level = 0; level < Fight.LEVELS; level++) {
            // She is the one to go for her ultimate; the player answers.
            Fight fight = new Fight(Fight.CHIKUWA, Fight.HALGAKOS, -1, level, 5);
            fight.fighters[1].meter = Fight.METER;
            Fight.Pad pad = new Fight.Pad();
            for (int frame = 0; frame < 1200 && fight.phase() != Fight.FLOURISH; frame++) {
                fight.fighters[0].health = Fight.fullHealth(Fight.CHIKUWA);
                fight.step(STEP, pad, null);
            }
            assertEquals("at level " + level, Fight.FLOURISH, fight.phase());
            assertEquals(1, fight.flourisher());
            fight.fighters[0].meter = Fight.METER;
            pad.ultimate = true;
            fight.step(STEP, pad, null);
            pad.ultimate = false;
            assertEquals(Fight.CLASHING, fight.phase());
            assertEquals(0, fight.clashSecond());
            // Nine taps a second from the player, which two thumbs manage.
            int frames = Math.round(Fight.CLASH_SECONDS * 60) - 2;
            for (int frame = 0; frame < frames; frame++) {
                pad.taps = (frame + 1) * 36 / frames - frame * 36 / frames;
                fight.step(STEP, pad, null);
            }
            tapped[level] = fight.taps(1);
            assertEquals(Fight.TAP_RATE[level] * Fight.CLASH_SECONDS, tapped[level], 2.5f);
            hold(fight, Fight.CLASH_GRACE + .1f, pad, null);
            assertEquals(0, fight.flourisher());
            assertEquals(Fight.CLASH, fight.fighters[0].move);
            if (level > 0) assertTrue(tapped[level] > tapped[level - 1]);
        }
        assertTrue(tapped[Fight.LEVELS - 1] < 30);
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

    // Two players

    @Test public void whatIsHeldDownSurvivesBeingSentAsANumber() {
        for (int bits = 0; bits < 512; bits++) {
            Fight.Pad pad = new Fight.Pad();
            pad.set(bits);
            assertEquals(bits, pad.bits());
            assertEquals((bits & 4) != 0, pad.jump);
            assertEquals((bits & 64) != 0, pad.special);
            assertEquals((bits & 128) != 0, pad.dodge);
            assertEquals((bits & 256) != 0, pad.ultimate);
        }
        // It goes in two bytes, as the device that joins sends it.
        Fight.Pad all = new Fight.Pad();
        all.set(511);
        Fight.Pad got = new Fight.Pad();
        got.set((all.bits() & 0xFF) | ((all.bits() >> 8) & 0xFF) << 8);
        assertTrue(got.dodge && got.ultimate && got.left && got.special);
    }

    @Test public void aFightWrittenDownAndReadBackIsTheSameFightToLookAt() throws Exception {
        // One fight plays itself; another is only ever told how the first stands, as a device that joins is.
        Fight host = new Fight(Fight.HALGAKOS, Fight.ILLYA, 2, 2, 7), shown = new Fight(Fight.HALGAKOS, Fight.ILLYA, -1, -1, 0);
        Fight.Pad none = new Fight.Pad();
        int thrown = 0;
        for (int step = 0; step < 2400 && host.phase() != Fight.MATCH_OVER; step++) {
            host.step(1 / 60f, none);
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            host.write(new java.io.DataOutputStream(bytes));
            assertTrue("a fight takes " + bytes.size() + " bytes to send", bytes.size() < 4000);
            shown.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
            assertEquals(host.phase(), shown.phase());
            assertEquals(host.round(), shown.round());
            assertEquals(host.hits(), shown.hits());
            assertEquals(host.blocks(), shown.blocks());
            assertEquals(host.moves(), shown.moves());
            assertEquals(host.left(), shown.left(), 0);
            assertEquals(host.flourisher(), shown.flourisher());
            assertEquals(host.shots().size(), shown.shots().size());
            thrown = Math.max(thrown, host.shots().size());
            for (int index = 0; index < host.shots().size(); index++) {
                assertEquals(host.shots().get(index).kind, shown.shots().get(index).kind);
                assertEquals(host.shots().get(index).x, shown.shots().get(index).x, 0);
            }
            for (int side = 0; side < 2; side++) {
                Fight.Fighter hers = host.fighters[side], seen = shown.fighters[side];
                assertEquals(hers.x, seen.x, 0);
                assertEquals(hers.y, seen.y, 0);
                assertEquals(hers.state, seen.state);
                assertEquals(hers.move, seen.move);
                assertEquals(hers.clock, seen.clock, 0);
                assertEquals(hers.health, seen.health, 0);
                assertEquals(hers.meter, seen.meter, 0);
                assertEquals(hers.wins, seen.wins);
                assertEquals(hers.facingLeft, seen.facingLeft);
                assertEquals(hers.unarmed, seen.unarmed);
                assertEquals(hers.chain, seen.chain);
                assertEquals(hers.combo, seen.combo);
                assertEquals(hers.comboDamage, seen.comboDamage, 0);
                assertEquals(hers.reel, seen.reel);
                assertEquals(hers.backing, seen.backing);
                assertEquals(hers.guarding, seen.guarding);
                assertEquals(hers.dodgeRest, seen.dodgeRest, 0);
                assertEquals(hers.mark, seen.mark, 0);
                assertEquals(hers.landed, seen.landed);
                // And so it is the same frame of her that both show.
                assertEquals(FightPose.of(host, hers, 0, 0), FightPose.of(shown, seen, 0, 0));
            }
            assertEquals(host.quakes(), shown.quakes());
            assertEquals(host.dodges(), shown.dodges());
            assertEquals(host.clashes(), shown.clashes());
            assertEquals(host.quakeX(), shown.quakeX(), 0);
            assertEquals(host.time(), shown.time(), 0);
        }
        assertTrue(host.hits() > 3);
        assertTrue(thrown > 0);
    }

    @Test public void twoPlayersByHandFightEachOther() {
        Fight fight = new Fight(Fight.TSUBAKIEH, Fight.CHIKUWA, -1, -1, 1);
        Fight.Pad one = new Fight.Pad(), two = new Fight.Pad();
        float started = fight.fighters[1].x;
        // The second player walks left on her own buttons while the first stands; neither is played for her.
        two.left = true;
        for (int step = 0; step < 30; step++) fight.step(1 / 60f, one, two);
        assertTrue(fight.fighters[1].x < started - .5f);
        assertEquals(Fight.WIDE / 2 - 2.2f, fight.fighters[0].x, .01f);
        // And hits her when she is close enough.
        float health = fight.fighters[0].health;
        for (int step = 0; step < 600 && fight.fighters[0].health == health; step++) {
            two.left = Math.abs(fight.fighters[1].x - fight.fighters[0].x) > 1f;
            two.light = !two.left && step % 20 < 6;
            fight.step(1 / 60f, one, two);
        }
        assertTrue(fight.fighters[0].health < health);
    }

    @Test public void twoDevicesTalkOverALine() throws Exception {
        int port;
        try (java.net.ServerSocket free = new java.net.ServerSocket(0)) {
            port = free.getLocalPort();
        }
        FightLink host = FightLink.host(port);
        Thread.sleep(300);
        FightLink guest = FightLink.join("127.0.0.1", port);
        for (int tries = 0; tries < 100 && !(host.connected() && guest.connected()); tries++) Thread.sleep(50);
        assertTrue(host.connected());
        assertTrue(guest.connected());
        guest.send(FightLink.HELLO, FightLink.VERSION, Fight.RUNE, 90);
        byte[] hello = null;
        for (int tries = 0; tries < 100 && hello == null; tries++) {
            hello = host.poll();
            if (hello == null) Thread.sleep(20);
        }
        assertArrayEquals(new byte[]{FightLink.HELLO, FightLink.VERSION, Fight.RUNE, 90}, hello);
        // How the fight stands, sent again and again: the last one sent is the last one to arrive.
        for (int count = 1; count <= 50; count++) {
            byte[] state = new byte[300];
            state[0] = (byte) count;
            host.send(FightLink.STATE, state);
            if (count % 10 == 0) Thread.sleep(15);
        }
        guest.send(FightLink.PAD, 21);
        byte[] last = null, pad = null;
        for (int tries = 0; tries < 100 && (last == null || last[1] != 50 || pad == null); tries++) {
            byte[] message = guest.poll();
            if (message != null) last = message;
            byte[] other = host.poll();
            if (other != null) pad = other;
            if (message == null && other == null) Thread.sleep(20);
        }
        assertEquals(FightLink.STATE, last[0]);
        assertEquals(50, last[1]);
        assertEquals(301, last.length);
        assertArrayEquals(new byte[]{FightLink.PAD, 21}, pad);
        // One leaves; the other finds the line over.
        guest.close();
        for (int tries = 0; tries < 100 && !host.closed(); tries++) {
            host.poll();
            Thread.sleep(30);
        }
        assertTrue(host.closed());
        host.close();
    }}
