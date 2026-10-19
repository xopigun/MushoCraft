package fr.xopi.mushocraft.magic.spell;

import com.mojang.serialization.Codec;

/**
 * How finely a character can mould their mana without words, built by circulating mana (see
 * {@link IncantationRules#controlGain}). Once it reaches {@link IncantationRules#SILENT_CASTING_CONTROL}, the
 * character can cast the spells they master silently.
 */
public record ManaControl(double practice) {
    public static final Codec<ManaControl> CODEC = Codec.DOUBLE.xmap(ManaControl::new, ManaControl::practice);

    public static ManaControl none() {
        return new ManaControl(0.0);
    }

    public ManaControl plus(double amount) {
        return new ManaControl(practice + Math.max(0.0, amount));
    }
}
