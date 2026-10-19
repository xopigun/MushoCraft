package fr.xopi.mushocraft.magic.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Stored part of a character's mana. Total capacity is not stored: it is the natural capacity given by age and
 * talent (see {@link ManaRules}) plus {@code trained}, the extra capacity earned by using mana.
 */
public record ManaData(double trained, double current) {
    public static final Codec<ManaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("trained").forGetter(ManaData::trained),
            Codec.DOUBLE.fieldOf("current").forGetter(ManaData::current)
    ).apply(instance, ManaData::new));

    public static ManaData empty() {
        return new ManaData(0.0, 0.0);
    }

    public ManaData withTrained(double value) {
        return new ManaData(Math.max(0.0, value), current);
    }

    public ManaData withCurrent(double value) {
        return new ManaData(trained, Math.max(0.0, value));
    }
}
