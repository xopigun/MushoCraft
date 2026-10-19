package fr.xopi.mushocraft.body.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A character's Battle Aura: how well they master it, and whether they hold it up on purpose right now.
 */
public record BattleAuraData(double mastery, boolean raised) {
    public static final Codec<BattleAuraData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("mastery").forGetter(BattleAuraData::mastery),
            Codec.BOOL.fieldOf("raised").forGetter(BattleAuraData::raised)
    ).apply(instance, BattleAuraData::new));

    public static BattleAuraData none() {
        return new BattleAuraData(0.0, false);
    }

    public AuraLevel level() {
        return AuraLevel.at(mastery);
    }

    public BattleAuraData withMastery(double value) {
        return new BattleAuraData(Math.max(0.0, value), raised);
    }

    public BattleAuraData withRaised(boolean value) {
        return new BattleAuraData(mastery, value);
    }
}
