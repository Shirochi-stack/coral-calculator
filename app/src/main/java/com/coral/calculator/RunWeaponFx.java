package com.coral.calculator;

/**
 * How gear of gold and better looks when it is held or worn, in the game's effect library ({@link Vfx}): a weapon takes the look of what it is
 * made of (flames for a burning one, frost for a freezing one, green for poison, red for draining, white-gold for a sharp one, cyan for a quick one,
 * yellow for lightning); armour and accessories glow in their rarity's colour. Gold glows softly and now and then throws a spark, red glows
 * brighter and throws them often, the rose has a pulsing aura and its sparks keep her company. And what a passive does as it goes off
 * ({@link RunWeaponPassives}) is shown by {@link #proc}. This only chooses: it draws nothing itself.
 */
final class RunWeaponFx {
    private RunWeaponFx() {}

    /** A look: the effect that is thrown now and then, the colour of the glow and the tint of what is thrown. */
    static final class Style {
        final int effect, colour;
        Style(int effect, int colour) {
            this.effect = effect;
            this.colour = colour;
        }
    }

    static final int GOLD_GLOW = 0xFFFFC94D;

    /** The look of a named weapon, by its first gimmick; plain gold if it has none. */
    static Style of(int code) {
        int index = RunWeapons.indexOf(code);
        if (index < 0) return new Style(Vfx.SPARKLE, GOLD_GLOW);
        int[] pairs = RunWeaponData.EFFECTS[index];
        for (int at = 0; at + 1 < pairs.length; at += 2) {
            switch (pairs[at]) {
                case RunLoot.BURN: return new Style(Vfx.FIRE_BURST, 0xFFFF8A3D);
                case RunLoot.FREEZE: return new Style(Vfx.ICE_BURST, 0xFF8FD8FF);
                case RunLoot.POISON: return new Style(Vfx.POISON, 0xFF8BD450);
                case RunLoot.LEECH: return new Style(Vfx.HEAL, 0xFFE0405A);
                case RunLoot.CRIT: case RunLoot.CRITX: return new Style(Vfx.CRIT, 0xFFFFE066);
                case RunLoot.HASTE: return new Style(Vfx.SPARKLE, 0xFF6FE3FF);
                case RunLoot.SPARK: return new Style(Vfx.LIGHTNING, 0xFFFFE94A);
                default: break;
            }
        }
        return new Style(Vfx.SPARKLE, GOLD_GLOW);
    }

    /** The best rarity of what she holds (a named weapon) and wears (armour and accessories), from gold up; or -1 where none is gold. */
    static int best(int held, int armour, int first, int second) {
        int best = -1;
        for (int code : new int[]{held, armour, first, second}) {
            if (code == 0 || (code == held && !RunWeapons.named(code))) continue;
            if (RunLoot.rarity(code) >= RunLoot.GOLD) best = Math.max(best, RunLoot.rarity(code));
        }
        return best;
    }

    /** How long between sparks, in seconds: gold now and then, red often, the rose always. */
    static float every(int rarity) { return rarity >= RunLoot.ROSE ? .16f : rarity >= RunLoot.RED ? .3f : .6f; }

    /** How strong the glow is, from 0 to 255, at a rarity and a point of its pulse (0 to 1). */
    static int glow(int rarity, float pulse) { return Math.round(36 + 22 * pulse + 26 * (rarity - RunLoot.GOLD)); }

    /** The effect that shows a passive going off, and how many squares across. */
    static int procEffect(int kind) {
        switch (kind) {
            case RunWeaponPassives.ECHO: return Vfx.SLASH_CROSS;
            case RunWeaponPassives.SHATTER: return Vfx.ICE_BURST;
            case RunWeaponPassives.RHYTHM: return Vfx.IMPACT_STAR;
            case RunWeaponPassives.BURST: return Vfx.EXPLOSION;
            case RunWeaponPassives.ARC: return Vfx.LIGHTNING;
            case RunWeaponPassives.RUSH: return Vfx.HEAL;
            default: return Vfx.SHOCKWAVE;
        }
    }

    static float procSize(int kind) { return kind == RunWeaponPassives.BURST ? 2.6f : kind == RunWeaponPassives.NOVA ? 4.6f : kind == RunWeaponPassives.RUSH ? 1.8f : 1.7f; }
}
