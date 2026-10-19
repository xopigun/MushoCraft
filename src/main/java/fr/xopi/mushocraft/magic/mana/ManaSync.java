package fr.xopi.mushocraft.magic.mana;

import fr.xopi.mushocraft.network.ManaSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sends the mana gauge to its owner, only when what they would see has changed.
 */
public final class ManaSync {
    private static final Map<UUID, ManaSnapshot> LAST_SENT = new ConcurrentHashMap<>();

    private ManaSync() {
    }

    public static void push(ServerPlayer player) {
        ManaSnapshot snapshot = ManaService.snapshot(player);
        if (!snapshot.equals(LAST_SENT.get(player.getUUID())) && player.connection != null) {
            LAST_SENT.put(player.getUUID(), snapshot);
            PacketDistributor.sendToPlayer(player, new ManaSyncPayload(snapshot));
        }
    }

    /** Forces the next push, for a client that just joined or respawned and knows nothing yet. */
    public static void forget(ServerPlayer player) {
        LAST_SENT.remove(player.getUUID());
    }
}
