package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.npc.TeacherEntity;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

/**
 * Epic Fight patch of the teachers: a humanoid that moves and fights with Epic Fight's animations.
 * {@link HumanoidMobPatch} picks the combat behaviour from the weapon in hand, so each style's weapon
 * gives its master a matching way of fighting.
 */
public class TeacherPatch<T extends TeacherEntity> extends HumanoidMobPatch<T> {
    public TeacherPatch(T original) {
        super(original, Factions.NEUTRAL);
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.JUMP, Animations.BIPED_JUMP);
        animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonAggressiveMobUpdateMotion(considerInaction);
    }
}
