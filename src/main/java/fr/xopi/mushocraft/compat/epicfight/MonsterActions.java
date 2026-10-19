package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MonsterAction;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

/**
 * Plays a monster's gesture (its pack call...) with the animation of its species.
 */
public final class MonsterActions {
    private MonsterActions() {
    }

    public static boolean play(LivingEntity monster, MonsterAction action) {
        MonsterPatch<?> patch = EpicFightCapabilities.getEntityPatch(monster, MonsterPatch.class);
        return patch != null && EpicFightAnimations.play(monster, patch.animations().action(action));
    }
}
