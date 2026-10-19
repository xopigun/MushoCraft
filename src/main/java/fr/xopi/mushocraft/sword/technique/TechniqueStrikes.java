package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.combat.move.CooldownService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.annotation.Nullable;

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
    public static void arm(LivingEntity fighter, ResourceLocation animation, float damageMultiplier, int windowTicks,
                           @Nullable Consumer<LivingEntity> onHit) {
        ARMED.put(fighter.getUUID(), new Strike(animation, damageMultiplier,
                CooldownService.now(fighter) + windowTicks, onHit));
    }

    /** The armed strike matching a hit dealt by this animation, if it is still within its window. */
    public static Optional<Strike> matching(LivingEntity fighter, ResourceLocation animation) {
        Strike strike = ARMED.get(fighter.getUUID());
        if (strike == null || CooldownService.now(fighter) > strike.expiresAt()) {
            ARMED.remove(fighter.getUUID());
            return Optional.empty();
        }
        return strike.animation().equals(animation) ? Optional.of(strike) : Optional.empty();
    }

    public static void clear(LivingEntity fighter) {
        ARMED.remove(fighter.getUUID());
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
