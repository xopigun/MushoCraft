package fr.xopi.mushocraft.compat.epicfight;

import net.minecraft.world.entity.player.Player;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Façade over Epic Fight's player mode. MushoCraft's combat mode <b>is</b> Epic Fight's battle mode
 * (decision of 19/10/2026): the move bar replaces the hotbar exactly when Epic Fight is in battle mode.
 * Works on both sides, as Epic Fight keeps the mode in sync.
 */
public final class EpicFightModes {
    private EpicFightModes() {
    }

    public static boolean isBattleMode(Player player) {
        PlayerPatch<?> patch = EpicFightCapabilities.getPlayerPatch(player);
        return patch != null && patch.isEpicFightMode();
    }
}
