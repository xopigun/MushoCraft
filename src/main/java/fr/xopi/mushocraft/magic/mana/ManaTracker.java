package fr.xopi.mushocraft.magic.mana;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.event.PlayerBornEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Drives mana on the server: a fresh reservoir at birth, regeneration or training every second,
 * and a full resync whenever the client may have lost track.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class ManaTracker {
    private static final int UPDATE_INTERVAL_TICKS = 20;
    private static final double UPDATE_SECONDS = UPDATE_INTERVAL_TICKS / 20.0;

    private ManaTracker() {
    }

    /** Runs after the talents are rolled, since the reservoir depends on the mana potential. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBorn(PlayerBornEvent event) {
        ManaService.resetForNewLife(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        resync(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        resync(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        resync(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ManaTraining.setCirculating(player, false);
            ManaSync.forget(player);
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % UPDATE_INTERVAL_TICKS != 0) {
            return;
        }
        if (ManaTraining.isCirculating(player)) {
            ManaTraining.tick(player, UPDATE_SECONDS);
        } else {
            ManaService.regenerate(player, UPDATE_SECONDS);
        }
        // Capacity also follows age, so the gauge can change without any spending.
        ManaSync.push(player);
    }

    private static void resync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ManaSync.forget(serverPlayer);
            ManaSync.push(serverPlayer);
        }
    }
}
