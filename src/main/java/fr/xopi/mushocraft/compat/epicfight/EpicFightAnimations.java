package fr.xopi.mushocraft.compat.epicfight;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Façade for playing Epic Fight animations from MushoCraft. Existing Epic Fight animations are only referenced,
 * never copied (their assets are All Rights Reserved).
 */
public final class EpicFightAnimations {
    private EpicFightAnimations() {
    }

    /** Whether the fighter (player or creature) is free to start a technique: not stunned, not mid-action. */
    public static boolean canAct(LivingEntity fighter) {
        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(fighter, LivingEntityPatch.class);
        return patch != null && patch.getEntityState().canUseSkill();
    }

    /** Plays the animation for everyone around; attack animations deal their hits by themselves. */
    public static boolean play(LivingEntity fighter, AssetAccessor<? extends StaticAnimation> animation) {
        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(fighter, LivingEntityPatch.class);
        if (patch == null) {
            return false;
        }
        patch.playAnimationSynchronized(animation, 0.0F);
        return true;
    }
}
