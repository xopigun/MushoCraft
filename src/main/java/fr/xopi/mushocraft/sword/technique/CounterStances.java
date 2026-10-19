package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.combat.move.CooldownService;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Players waiting to receive an attack and turn it back (Water God Style, Flow). The counter itself is resolved
 * when damage comes in (see {@code SwordEvents}).
 */
public final class CounterStances {
    private static final Map<UUID, Stance> WAITING = new ConcurrentHashMap<>();

    private CounterStances() {
    }

    public static void enter(LivingEntity fighter, int windowTicks, float counterMultiplier, boolean deflectsProjectiles) {
        WAITING.put(fighter.getUUID(), new Stance(CooldownService.now(fighter) + windowTicks, counterMultiplier,
                deflectsProjectiles));
    }

    /** The stance the player is in, if it has not expired yet. One stance turns back one attack: clear it after use. */
    public static Optional<Stance> active(LivingEntity fighter) {
        Stance stance = WAITING.get(fighter.getUUID());
        if (stance == null || CooldownService.now(fighter) > stance.expiresAt()) {
            WAITING.remove(fighter.getUUID());
            return Optional.empty();
        }
        return Optional.of(stance);
    }

    public static void clear(LivingEntity fighter) {
        WAITING.remove(fighter.getUUID());
    }

    public record Stance(long expiresAt, float counterMultiplier, boolean deflectsProjectiles) {
    }
}
