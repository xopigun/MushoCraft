package fr.xopi.mushocraft.talent;

import com.mojang.serialization.Codec;

import java.util.EnumMap;
import java.util.Map;

/**
 * Immutable set of talent values. A talent missing from the map (for instance one added in a later version)
 * reads as {@link #NEUTRAL}.
 */
public record Talents(Map<Talent, Double> values) {
    public static final double NEUTRAL = 1.0;

    public static final Codec<Talents> CODEC =
            Codec.unboundedMap(Talent.CODEC, Codec.DOUBLE).xmap(Talents::new, Talents::values);

    public Talents {
        values = Map.copyOf(values);
    }

    public static Talents neutral() {
        return new Talents(Map.of());
    }

    public double get(Talent talent) {
        return values.getOrDefault(talent, NEUTRAL);
    }

    public Talents with(Talent talent, double value) {
        Map<Talent, Double> copy = new EnumMap<>(Talent.class);
        copy.putAll(values);
        copy.put(talent, value);
        return new Talents(copy);
    }
}
