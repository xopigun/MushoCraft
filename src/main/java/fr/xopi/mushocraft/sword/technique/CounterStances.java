package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.combat.move.CooldownService;
import net.minecraft.server.level.ServerPlayer;

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

    public static void enter(ServerPlayer player, int windowTicks, float counterMultiplier, boolean deflectsProjectiles) {
        WAITING.put(player.getUUID(), new Stance(CooldownService.now(player) + windowTicks, counterMultiplier,
                deflectsProjectiles));
    }

    /** The stance the player is in, if it has not expired yet. One stance turns back one attack: clear it after use. */
    public static Optional<Stance> active(ServerPlayer player) {
        Stance stance = WAITING.get(player.getUUID());
        if (stance == null || CooldownService.now(player) > stance.expiresAt()) {
            WAITING.remove(player.getUUID());
            return Optional.empty();
        }
        return Optional.of(stance);
    }

    public static void clear(ServerPlayer player) {
        WAITING.remove(player.getUUID());
    }

    public record Stance(long expiresAt, float counterMultiplier, boolean deflectsProjectiles) {
    }
}
