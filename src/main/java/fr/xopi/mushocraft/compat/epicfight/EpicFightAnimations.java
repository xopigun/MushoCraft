package fr.xopi.mushocraft.compat.epicfight;

import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * Façade for playing Epic Fight animations from MushoCraft. Existing Epic Fight animations are only referenced,
 * never copied (their assets are All Rights Reserved).
 */
public final class EpicFightAnimations {
    private EpicFightAnimations() {
    }

    /** Whether the player is free to start a technique (not stunned, not already in the middle of an action). */
    public static boolean canAct(ServerPlayer player) {
        ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
        return patch != null && patch.getEntityState().canUseSkill();
    }

    /** Plays the animation for everyone around; attack animations deal their hits by themselves. */
    public static boolean play(ServerPlayer player, AssetAccessor<? extends StaticAnimation> animation) {
        ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch == null) {
            return false;
        }
        patch.playAnimationSynchronized(animation, 0.0F);
        return true;
    }
}
