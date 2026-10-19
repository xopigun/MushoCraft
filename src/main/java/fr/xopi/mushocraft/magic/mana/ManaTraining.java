package fr.xopi.mushocraft.magic.mana;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mana circulation: the player deliberately pours mana out without casting anything, to make the reservoir grow.
 * This is how Rudeus trains as a small child, by emptying his mana again and again (wiki, page <i>Mana</i>).
 * No spell knowledge is needed, so babies can train too.
 */
public final class ManaTraining {
    private static final Set<UUID> CIRCULATING = ConcurrentHashMap.newKeySet();

    private ManaTraining() {
    }

    public static void setCirculating(ServerPlayer player, boolean active) {
        if (active) {
            CIRCULATING.add(player.getUUID());
        } else {
            CIRCULATING.remove(player.getUUID());
        }
    }

    public static boolean isCirculating(ServerPlayer player) {
        return CIRCULATING.contains(player.getUUID());
    }

    /** Spends this second's worth of mana; circulation stops by itself once the reservoir is empty. */
    public static void tick(ServerPlayer player, double seconds) {
        ManaSnapshot snapshot = ManaService.snapshot(player);
        double drain = Math.min(snapshot.current(),
                snapshot.capacity() * ManaRules.fromConfig().trainingDrainPerSecond() * seconds);
        if (drain <= 0.0 || !ManaService.trySpend(player, drain)) {
            setCirculating(player, false);
            return;
        }
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(),
                8, 0.3, 0.4, 0.3, 0.5);
    }
}
