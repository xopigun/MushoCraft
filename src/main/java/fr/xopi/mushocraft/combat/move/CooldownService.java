package fr.xopi.mushocraft.combat.move;

import fr.xopi.mushocraft.core.MushoAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Starts and reads cooldowns. Time is the overworld game time, shared by every dimension.
 */
public final class CooldownService {
    private CooldownService() {
    }

    public static Optional<Cooldowns.Cooldown> active(ServerPlayer player, ResourceLocation key) {
        return player.getData(MushoAttachments.COOLDOWNS).active(key, now(player));
    }

    public static void start(ServerPlayer player, ResourceLocation key, int durationTicks) {
        if (durationTicks <= 0) {
            return;
        }
        player.setData(MushoAttachments.COOLDOWNS,
                player.getData(MushoAttachments.COOLDOWNS).start(key, now(player), durationTicks));
        MovesSync.push(player);
    }

    public static void clear(ServerPlayer player) {
        player.setData(MushoAttachments.COOLDOWNS, Cooldowns.none());
        MovesSync.push(player);
    }

    public static long now(ServerPlayer player) {
        return player.server.overworld().getGameTime();
    }
}
