package com.coral.calculator;

/**
 * Counts what one side of a fight of Page brawl has done that is worth keeping: clashes won
 * and lost, rounds won without being hit, chains of five landed in full, smashes landed,
 * dodges that avoided a blow, and ultimates landed. It looks only at the fight as it stands,
 * which is all a device that joins a game is shown, so each player's own device can count
 * for her. Each thing is counted once.
 */
final class FightTally {
    static final int CLASH = 0, CLASH_LOST = 1, PERFECT = 2, CHAIN = 3, SMASH = 4, DODGE = 5, ULT = 6, COUNT = 7;
    /** The names they are kept under. */
    static final String[] NAMES = {"fight_clash", "fight_clash_lost", "fight_perfect", "fight_chain", "fight_smash", "fight_dodge", "fight_ult"};

    final int[] counts = new int[COUNT];
    private int side, hits, quakes, slips, phase;
    private boolean landed, settled;

    /** Starts counting for a side of a fight, from how it stands now. */
    void begin(Fight fight, int side) {
        this.side = side;
        java.util.Arrays.fill(counts, 0);
        hits = fight.hits();
        quakes = fight.quakes();
        settled = fight.clashWinner() >= 0;
        slips = fight.fighters[side].slips;
        phase = fight.phase();
        landed = false;
    }

    /** Looks at the fight again, and counts what has happened since it last looked. */
    void look(Fight fight) {
        Fight.Fighter me = fight.fighters[side];
        int now = fight.phase();
        // Each ultimate of hers is counted once, however many times it lands: a new one begins with its announcement.
        if (now == Fight.FLOURISH && phase != Fight.FLOURISH && fight.flourisher() == side) landed = false;
        if (fight.hits() != hits) {
            hits = fight.hits();
            if (fight.hitSide() != side) {
                if (fight.hitGrand()) {
                    if (!landed) counts[ULT]++;
                    landed = true;
                } else if (me.state == Fight.ATTACK && Fight.chainStep(me.move) == 5 && me.hitLanded && me.combo >= 5) {
                    // The fifth blow of a chain, on top of the four before it.
                    counts[CHAIN]++;
                }
            }
        }
        if (fight.quakes() != quakes) {
            quakes = fight.quakes();
            if (me.state == Fight.ATTACK && me.move == Fight.SMASH && me.landed == 1) counts[SMASH]++;
        }
        if (me.slips != slips) {
            counts[DODGE] += Math.max(0, me.slips - slips);
            slips = me.slips;
        }
        // A clash is settled for as long as the winner's greater ultimate is announced: counted as that begins.
        boolean won = fight.clashWinner() >= 0;
        if (won && !settled) counts[fight.clashWinner() == side ? CLASH : CLASH_LOST]++;
        settled = won;
        if (now == Fight.ROUND_OVER && phase != Fight.ROUND_OVER && fight.lastWinner() == side && me.health >= Fight.fullHealth(me.kind)) counts[PERFECT]++;
        phase = now;
    }
}
