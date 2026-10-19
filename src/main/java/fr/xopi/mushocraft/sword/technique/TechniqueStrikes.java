package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.combat.move.CooldownService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Hits dealt by a technique's animation are stronger than ordinary hits. When a technique starts its animation,
 * it arms a strike here; Epic Fight's damage hook (see {@code SwordEvents}) asks whether a hit comes from an armed
 * strike, by comparing the animation that dealt it.
 */
public final class TechniqueStrikes {
    private static final Map<UUID, Strike> ARMED = new ConcurrentHashMap<>();

    private TechniqueStrikes() {
    }

    /**
     * @param onHit extra effect on every creature the strike hits (disarm, slow...), may be null
     */
    public static void arm(ServerPlayer player, ResourceLocation animation, float damageMultiplier, int windowTicks,
                           @Nullable Consumer<LivingEntity> onHit) {
        ARMED.put(player.getUUID(), new Strike(animation, damageMultiplier,
                CooldownService.now(player) + windowTicks, onHit));
    }

    /** The armed strike matching a hit dealt by this animation, if it is still within its window. */
    public static Optional<Strike> matching(ServerPlayer player, ResourceLocation animation) {
        Strike strike = ARMED.get(player.getUUID());
        if (strike == null || CooldownService.now(player) > strike.expiresAt()) {
            ARMED.remove(player.getUUID());
            return Optional.empty();
        }
        return strike.animation().equals(animation) ? Optional.of(strike) : Optional.empty();
    }

    public static void clear(ServerPlayer player) {
        ARMED.remove(player.getUUID());
    }

    public record Strike(ResourceLocation animation, float damageMultiplier, long expiresAt,
                         @Nullable Consumer<LivingEntity> onHit) {
        public void applyTo(LivingEntity target) {
            if (onHit != null) {
                onHit.accept(target);
            }
        }
    }
}
