package fr.xopi.mushocraft.life;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Drives life on the server: a player who has never been born is born on first login,
 * then their stage and size are kept in sync with their age.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class LifeTracker {
    private static final int REFRESH_INTERVAL_TICKS = 20;

    private LifeTracker() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (LifeService.isBorn(player)) {
                LifeService.refresh(player);
            } else {
                LifeService.giveBirth(player);
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LifeService.refresh(player);
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % REFRESH_INTERVAL_TICKS == 0) {
            LifeService.refresh(player);
        }
    }
}
