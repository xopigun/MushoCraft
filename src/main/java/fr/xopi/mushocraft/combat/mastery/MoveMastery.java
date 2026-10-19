package fr.xopi.mushocraft.combat.mastery;

import com.mojang.serialization.Codec;
import fr.xopi.mushocraft.combat.move.MoveRef;

import java.util.HashMap;
import java.util.Map;

/**
 * How many times a character has repeated each move (spell or technique). Failed attempts count for half.
 */
public record MoveMastery(Map<String, Double> repetitions) {
    public static final Codec<MoveMastery> CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
            .xmap(MoveMastery::new, MoveMastery::repetitions);

    public MoveMastery {
        repetitions = Map.copyOf(repetitions);
    }

    public static MoveMastery none() {
        return new MoveMastery(Map.of());
    }

    public double repetitions(MoveRef move) {
        return repetitions.getOrDefault(key(move), 0.0);
    }

    public MoveMastery with(MoveRef move, double count) {
        Map<String, Double> copy = new HashMap<>(repetitions);
        copy.put(key(move), Math.max(0.0, count));
        return new MoveMastery(copy);
    }

    private static String key(MoveRef move) {
        return move.kind().getSerializedName() + "/" + move.id();
    }
}
