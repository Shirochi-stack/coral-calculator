package com.coral.calculator;

import static com.coral.calculator.FightFrames.*;

/**
 * Which of her frames a fighter of Page brawl shows, for what she is doing and how long she
 * has been doing it. It goes by the fight's own clocks and nothing else, so two devices
 * showing the same fight show the same frame.
 *
 * Standing she goes through the eight frames of her stance, which were drawn together. A walk is the frames that were drawn together as one
 * cycle. What happens once has frames between its poses: a jump goes through ten, and every
 * blow has a wind-up before it and a follow-through after.
 */
final class FightPose {
    private static final int[] RUNE_THROWS = {0, 2, 3, 4};

    private FightPose() {}

    /**
     * @param hold how much is left of the moment before a round in which nobody moves
     * @param over how long ago the round ended, while it is over
     */
    static int of(Fight fight, Fight.Fighter fighter, float hold, float over) {
        float clock = fighter.clock;
        switch (fighter.state) {
            case Fight.WALK: {
                // The frames of a stride that were drawn together, and so agree with one another: four backward, six forward.
                if (fighter.backing) return BACK1 + (int) (clock / .12f) % 4;
                return WALK1 + (int) (clock / .085f) % 6;
            }
            case Fight.DASH:
                return clock < .05f ? DASH1 : clock > Fight.DASH_SECONDS - .06f ? SKID : (int) (clock / .07f) % 2 == 0 ? DASH2 : DASH3;
            case Fight.BACKSTEP:
                return clock < .06f ? HOP1 : HOP2;
            case Fight.DODGE: {
                float part = clock / Fight.DODGE_SECONDS;
                if (fighter.inAir()) return ADODGE;
                // Which way she slips, against the way she faces. Away from the other she springs back, lands and settles; toward her
                // she ducks past and skids to a stop; with no arrow held she only leans out of the way where she stands.
                float way = fighter.across * (fighter.facingLeft ? -1 : 1);
                if (way < -.01f) return part < .22f ? BDODGE1 : part < .72f ? BDODGE2 : BDODGE3;
                if (way > .01f) return part < .72f ? FDODGE : SKID;
                return part < .5f ? DODGE1 : DODGE2;
            }
            case Fight.JUMP:
                if (fighter.guarding) return AIR_GUARD;
                // The crouch and the push as she leaves the ground; then by how fast she is going up or coming down.
                if (clock < .035f) return PREJUMP;
                if (clock < .08f) return PUSH;
                if (fighter.up > 5.5f) return RISE;
                if (fighter.up > 2) return RISE2;
                if (fighter.up > -2) return TOP;
                if (fighter.up > -5.5f) return TOP2;
                return fighter.y < .9f ? FALL2 : FALL;
            case Fight.LAND:
                return clock < Fight.LAND_SECONDS * .5f ? LAND : LAND2;
            case Fight.BLOCK:
                return fighter.blockAge < .18f ? GUARD_HIT : clock < .07f ? GUARD_UP : GUARD;
            case Fight.HIT: {
                // A hard blow throws her back and leaves her staggering; a light one snaps her head back or doubles
                // her up, by turns, and she comes round from it.
                if (fighter.reel == 2) return clock < .14f ? BLOWN : STAGGER;
                boolean round = clock > fighter.stunned * .5f;
                return fighter.reel == 1 ? (round ? HIT2R : HIT2) : (round ? HIT1R : HIT1);
            }
            case Fight.DOWN:
                return fighter.y > .05f ? (fighter.up > 0 ? AIR_HIT : FALLING) : fighter.health <= 0 ? KO : LYING;
            case Fight.OUT:
                return KO;
            case Fight.RISE:
                return clock < .06f ? PROP : clock < .12f ? SIT_UP : clock < .18f ? KNEEL_UP : clock < .24f ? HALF_UP : DUST;
            case Fight.WON:
                return clock < .3f ? WIN1 : (int) ((clock - .3f) / .4f) % 2 == 0 ? WIN2 : WIN3;
            case Fight.ATTACK:
                // While her home run is announced she calls her shot, the bat pointed at the sky, and then draws it back.
                if (fighter.kind == Fight.TSUBAKIEH && fighter.move == Fight.ULTIMATE && fight.phase() == Fight.FLOURISH && fight.flourisher() == fighter.side) {
                    return fight.flourish() < .5f ? HR1 : HR2;
                }
                // While her brick bread drop is announced she hugs the great loaf, staggering under it, and heaves it over her head.
                if (fighter.kind == Fight.CHIKUWA && fighter.move == Fight.ULTIMATE && fight.phase() == Fight.FLOURISH && fight.flourisher() == fighter.side) {
                    return fight.flourish() < .5f ? DR1 : DR2;
                }
                return striking(fighter);
            default:
                // Beaten on time she sinks to her knees; before a round she has something to say for herself.
                if (fight.phase() == Fight.ROUND_OVER && fight.lastWinner() == 1 - fighter.side) return over < .5f ? LOSE1 : LOSE2;
                if (hold > .45f) return TAUNT;
                if (fighter.turned < .05f) return TURN;
                if (fighter.turned < .1f) return TURN2;
                // Her stance, shifting her weight: eight frames of it drawn together in one picture, round and round.
                return ST1 + (int) (fight.time() / .14f + fighter.side * 4) % 8;
        }
    }

    /** Which of the five ways she throws her special this is, from 0. Rune's third is her knife; the others go through their five in turn. */
    private static int special(Fight.Fighter fighter) {
        if (fighter.kind == Fight.RUNE) return fighter.specials % 3 == 0 ? 1 : RUNE_THROWS[fighter.specials % 4];
        return (fighter.specials + 4) % 5;
    }

    private static int striking(Fight.Fighter fighter) {
        Fight.Move move = fighter.doing();
        float clock = fighter.clock;
        // Coming out, she winds up for it; then the blow; then she follows it through, and at the last draws back to her stance.
        boolean out = clock >= move.startup, struck = clock > move.startup + move.active;
        int which = fighter.move, step = Fight.chainStep(which);
        if (which == Fight.DASH_LIGHT) return out ? DASH_Q : DASH_QW;
        if (which == Fight.DASH_HEAVY) return out ? DASH_H : DASH_HW;
        if (step >= 1) {
            boolean heavy = which == Fight.HEAVY || which >= Fight.HEAVY2 && which <= Fight.HEAVY5;
            if (!out) return (heavy ? HW1 : QW1) + step - 1;
            if (!struck) return (heavy ? H1 : Q1) + step - 1;
            return clock > move.startup + move.active + Math.min(.14f, move.recovery * .7f) ? (heavy ? HS : QS) : (heavy ? HF1 : QF1) + step - 1;
        }
        if (which >= Fight.AIR1 && which <= Fight.AIR3) return !out ? AW : !struck ? A1 + which - Fight.AIR1 : AF1 + which - Fight.AIR1;
        if (which == Fight.SMASH) {
            if (fighter.landed == 0) return out ? SM_FALL : clock < move.startup * .55f ? SM_UP : SM_TIP;
            float since = clock - fighter.mark, whole = move.recovery + (fighter.landed == 2 ? .25f : 0);
            return since < .22f ? SM_HIT : since > whole - .12f ? SM_RISE : fighter.landed == 2 ? SM_MISS : SM_REC;
        }
        if (which == Fight.SPECIAL) {
            int way = special(fighter);
            if (!out) return clock < move.startup * .35f ? SS : SW1 + way;
            return clock < move.startup + .14f ? S1 + way : clock > move.length() - .07f ? SS : SF;
        }
        if (which == Fight.AIR_SPECIAL) return fighter.specials % 2 == 0 ? AX1 : AX2;
        if (which == Fight.BREAK) return clock < move.startup * .3f ? MS : out ? BREAK : BREAK_W;
        if (which == Fight.LAUNCH) return out ? LAUNCH : LAUNCH_W;
        if (which == Fight.SHOVE) return out ? SHOVE : SHOVE_W;
        if (which == Fight.ULTIMATE && fighter.kind == Fight.CHIKUWA) {
            // Up with it, down with it, landed on it, and then a bite of a roll.
            if (!out) return clock < move.startup * .7f ? DR3 : DR4;
            if (!struck) return DR5;
            return clock > move.length() - .3f ? DR6 : DR5;
        }
        if (which == Fight.CLASH && fighter.kind == Fight.TSUBAKIEH) {
            // Her greater home run is swung with the frames of her home run: wound up, a swing for each time it lands, and watching it go.
            float last = move.startup + move.every * (move.times - 1) + .15f;
            if (!out) return HR2;
            if (clock < last) return fighter.landed % 2 == 1 ? HR4 : HR3;
            return clock > move.length() - .25f ? HR6 : HR5;
        }
        if (which == Fight.ULTIMATE && fighter.kind == Fight.TSUBAKIEH) {
            // Wound up as she rushes in; the swing; the bat meeting; then the follow-through held, and watching it go.
            if (!out) return clock < move.startup * .7f ? HR2 : HR3;
            if (!struck) return HR4;
            return clock > move.length() - .2f ? HR6 : HR5;
        }
        float through = move.startup + Math.max(move.active, move.every * (move.times - 1)) + .15f;
        if (which == Fight.CLASH) return !out ? C1 : clock < through ? C2 : C3;
        // Her ultimate: she gathers it, lets it go (each cut of Halgakos's another stroke), and is done.
        if (!out) {
            float part = clock / move.startup;
            return part < .3f ? U1 : part < .5f ? U12 : part < .8f ? U2 : U23;
        }
        if (clock >= through) return clock < through + .08f ? U34 : clock > move.length() - .1f ? U4R : U4;
        return fighter.kind == Fight.HALGAKOS ? (fighter.landed % 2 == 1 ? U2 : U3) : U3;
    }
}
