package fr.xopi.mushocraft.body;

import com.mojang.serialization.Codec;

import java.util.EnumMap;
import java.util.Map;

/**
 * Training gathered in each physical stat. The bonus it gives is computed by {@link BodyRules}.
 */
public record BodyTraining(Map<BodyStat, Double> experience) {
    public static final Codec<BodyTraining> CODEC = Codec.unboundedMap(BodyStat.CODEC, Codec.DOUBLE)
            .xmap(BodyTraining::new, BodyTraining::experience);

    public BodyTraining {
        experience = Map.copyOf(experience);
    }

    public static BodyTraining none() {
        return new BodyTraining(Map.of());
    }

    public double experience(BodyStat stat) {
        return experience.getOrDefault(stat, 0.0);
    }

    public BodyTraining with(BodyStat stat, double xp) {
        Map<BodyStat, Double> copy = new EnumMap<>(BodyStat.class);
        copy.putAll(experience);
        copy.put(stat, Math.max(0.0, xp));
        return new BodyTraining(copy);
    }
}
