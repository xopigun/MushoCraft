package fr.xopi.mushocraft.sword;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.network.StyleSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Keeps clients told of the style each visible player fights in.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class StyleSync {
    private StyleSync() {
    }

    /** To call whenever a player's active style changes. */
    public static void push(ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payloadFor(player));
    }

    private static StyleSyncPayload payloadFor(ServerPlayer player) {
        return new StyleSyncPayload(player.getId(), SwordService.activeStyle(player));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            push(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            push(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            push(player);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer seen && event.getEntity() instanceof ServerPlayer viewer) {
            PacketDistributor.sendToPlayer(viewer, payloadFor(seen));
        }
    }
}
