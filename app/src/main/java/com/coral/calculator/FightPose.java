package com.coral.calculator;

import static com.coral.calculator.FightFrames.*;

/**
 * Which of her frames a fighter of Page brawl shows, for what she is doing and how long she
 * has been doing it. It goes by the fight's own clocks and nothing else, so two devices
 * showing the same fight show the same frame.
 */
final class FightPose {
    private static final int[] IDLING = {IDLE1, IDLE2, IDLE3, IDLE4};
    private static final int[] RUNE_THROWS = {S1, S3, S4, S5};

    private FightPose() {}

    /**
     * @param hold how much is left of the moment before a round in which nobody moves
     * @param over how long ago the round ended, while it is over
     */
    static int of(Fight fight, Fight.Fighter fighter, float hold, float over) {
        float clock = fighter.clock;
        switch (fighter.state) {
            case Fight.WALK:
                return fighter.backing ? BACK1 + (int) (clock / .12f) % 4 : WALK1 + (int) (clock / .085f) % 6;
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
                return fighter.guarding ? AIR_GUARD : clock < .06f ? PREJUMP : fighter.up > 4 ? RISE : fighter.up > -4 ? TOP : FALL;
            case Fight.LAND:
                return LAND;
            case Fight.BLOCK:
                return fighter.blockAge < .18f ? GUARD_HIT : GUARD;
            case Fight.HIT:
                // A hard blow throws her back and leaves her staggering; a light one snaps her head back or doubles her up, by turns.
                return fighter.reel == 2 ? (clock < .16f ? BLOWN : STAGGER) : fighter.reel == 1 ? HIT2 : HIT1;
            case Fight.DOWN:
                return fighter.y > .05f ? (fighter.up > 0 ? AIR_HIT : FALLING) : fighter.health <= 0 ? KO : LYING;
            case Fight.OUT:
                return KO;
            case Fight.RISE:
                return clock < .1f ? SIT_UP : clock < .2f ? KNEEL_UP : DUST;
            case Fight.WON:
                return clock < .3f ? WIN1 : (int) ((clock - .3f) / .4f) % 2 == 0 ? WIN2 : WIN3;
            case Fight.ATTACK:
                return striking(fighter);
            default:
                // Beaten on time she sinks to her knees; before a round she has something to say for herself.
                if (fight.phase() == Fight.ROUND_OVER && fight.lastWinner() == 1 - fighter.side) return over < .5f ? LOSE1 : LOSE2;
                if (hold > .45f) return TAUNT;
                if (fighter.turned < .1f) return TURN;
                return IDLING[(int) (fight.time() / .28f + fighter.side * 2) % 4];
        }
    }

    private static int striking(Fight.Fighter fighter) {
        Fight.Move move = fighter.doing();
        float clock = fighter.clock;
        boolean out = clock >= move.startup;
        int which = fighter.move, step = Fight.chainStep(which);
        if (which == Fight.DASH_LIGHT) return DASH_Q;
        if (which == Fight.DASH_HEAVY) return DASH_H;
        if (step >= 1) {
            boolean heavy = which == Fight.HEAVY || which >= Fight.HEAVY2 && which <= Fight.HEAVY5;
            int stance = heavy ? HS : QS, first = heavy ? H1 : Q1;
            // Coming out, it is still the blow before; once it is done and she draws back, her stance again.
            if (!out) return step == 1 ? stance : first + step - 2;
            return clock > move.startup + move.active + move.recovery * .6f ? stance : first + step - 1;
        }
        if (which >= Fight.AIR1 && which <= Fight.AIR3) return A1 + which - Fight.AIR1;
        if (which == Fight.SMASH) {
            if (fighter.landed == 0) return out ? SM_FALL : SM_UP;
            float since = clock - fighter.mark, whole = move.recovery + (fighter.landed == 2 ? .25f : 0);
            return since < .22f ? SM_HIT : since > whole - .12f ? SM_RISE : fighter.landed == 2 ? SM_MISS : SM_REC;
        }
        if (which == Fight.SPECIAL) {
            if (!out) return SS;
            // Rune's third is her knife; the others go through their five in turn.
            if (fighter.kind == Fight.RUNE) return fighter.specials % 3 == 0 ? S2 : RUNE_THROWS[fighter.specials % 4];
            return S1 + (fighter.specials + 4) % 5;
        }
        if (which == Fight.AIR_SPECIAL) return fighter.specials % 2 == 0 ? AX1 : AX2;
        if (which == Fight.BREAK) return clock < move.startup * .6f ? MS : BREAK;
        if (which == Fight.LAUNCH) return LAUNCH;
        if (which == Fight.SHOVE) return SHOVE;
        float through = move.startup + Math.max(move.active, move.every * (move.times - 1)) + .15f;
        if (which == Fight.CLASH) return !out ? C1 : clock < through ? C2 : C3;
        // Her ultimate: she gathers it, lets it go (each cut of Halgakos's another stroke), and is done.
        if (!out) return clock < move.startup * .55f ? U1 : U2;
        if (clock >= through) return U4;
        return fighter.kind == Fight.HALGAKOS ? (fighter.landed % 2 == 1 ? U2 : U3) : U3;
    }
}
