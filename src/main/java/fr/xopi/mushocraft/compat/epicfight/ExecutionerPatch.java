package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.Executioner;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

/**
 * Epic Fight patch of the Executioner: an undead humanoid on Epic Fight's biped skeleton, whose fighting comes from the
 * greatsword it holds (combos, chase), as for any swordsman. It walks heavily, as a zombie does, under its armour.
 */
public class ExecutionerPatch<T extends Executioner> extends HumanoidMobPatch<T> {
    public ExecutionerPatch(T original) {
        super(original, Factions.UNDEAD);
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.ZOMBIE_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonAggressiveMobUpdateMotion(considerInaction);
    }
}
