package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.village.TownsfolkEntity;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

/**
 * Epic Fight patch of the village folk: a peaceful humanoid on Epic Fight's biped skeleton, walking and running with
 * its animations. Epic Fight builds the model matrix at a constant size and ignores the vanilla scale attribute, by
 * which children are small: the patch applies it on top (as {@code PlayerPatchScaleMixin} does for players).
 */
public class TownsfolkPatch<T extends TownsfolkEntity> extends HumanoidMobPatch<T> {
    public TownsfolkPatch(T original) {
        super(original, Factions.VILLAGER);
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.JUMP, Animations.BIPED_JUMP);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    @Override
    public OpenMatrix4f getModelMatrix(float partialTick) {
        OpenMatrix4f matrix = super.getModelMatrix(partialTick);
        float scale = getOriginal().getScale();
        return scale == 1.0F ? matrix : matrix.scale(scale, scale, scale);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonMobUpdateMotion(considerInaction);
    }
}
