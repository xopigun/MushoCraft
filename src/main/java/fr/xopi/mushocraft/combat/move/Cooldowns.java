package fr.xopi.mushocraft.combat.move;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Running cooldowns, keyed by {@link Move#cooldownKey()}, in overworld game ticks.
 * Saved with the player so that logging out does not reset them.
 */
public record Cooldowns(Map<ResourceLocation, Cooldown> entries) {
    public static final Codec<Cooldowns> CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, Cooldown.CODEC).xmap(Cooldowns::new, Cooldowns::entries);

    public Cooldowns {
        entries = Map.copyOf(entries);
    }

    public static Cooldowns none() {
        return new Cooldowns(Map.of());
    }

    public Optional<Cooldown> active(ResourceLocation key, long now) {
        return Optional.ofNullable(entries.get(key)).filter(cooldown -> cooldown.end() > now);
    }

    public Cooldowns start(ResourceLocation key, long now, int durationTicks) {
        Map<ResourceLocation, Cooldown> copy = new HashMap<>(pruned(now).entries);
        copy.put(key, new Cooldown(now, now + durationTicks));
        return new Cooldowns(copy);
    }

    public Cooldowns pruned(long now) {
        Map<ResourceLocation, Cooldown> copy = new HashMap<>();
        entries.forEach((key, cooldown) -> {
            if (cooldown.end() > now) {
                copy.put(key, cooldown);
            }
        });
        return new Cooldowns(copy);
    }

    public record Cooldown(long start, long end) {
        public static final Codec<Cooldown> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("start").forGetter(Cooldown::start),
                Codec.LONG.fieldOf("end").forGetter(Cooldown::end)
        ).apply(instance, Cooldown::new));

        public int remaining(long now) {
            return (int) Math.max(0L, end - now);
        }

        public int total() {
            return (int) (end - start);
        }
    }
}
