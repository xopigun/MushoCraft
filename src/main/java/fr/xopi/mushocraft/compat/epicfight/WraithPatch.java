package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.Behavior;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.BehaviorSeries;

import java.util.function.Supplier;

/**
 * Patch of the wraiths: a raking claw at close range, sometimes twice. Its magic is the creature's own (see
 * {@code SpellcasterGoal}), its skeletons are Epic Fight's own patched skeletons.
 */
public class WraithPatch<T extends MushoMonster> extends MonsterPatch<T> {
    private final Supplier<WraithAnimations> animations;

    public WraithPatch(T monster, Supplier<WraithAnimations> animations) {
        super(monster);
        this.animations = animations;
    }

    @Override
    public WraithAnimations animations() {
        return animations.get();
    }

    @Override
    protected CombatBehaviors.Builder<MonsterPatch<?>> combatBehaviors() {
        WraithAnimations a = animations();
        return CombatBehaviors.<MonsterPatch<?>>builder()
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.claw())
                                .withinEyeHeight().withinDistance(0.0, 2.6).custom(MonsterPatch::mayStrike))
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.claw()).randomChance(0.4F)
                                .withinDistance(0.0, 2.8).custom(MonsterPatch::mayStrike)));
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        WraithAnimations a = animations();
        animator.addLivingAnimation(LivingMotions.IDLE, a.idle());
        animator.addLivingAnimation(LivingMotions.WALK, a.walk());
        animator.addLivingAnimation(LivingMotions.CHASE, a.run());
        animator.addLivingAnimation(LivingMotions.RUN, a.run());
        animator.addLivingAnimation(LivingMotions.DEATH, a.death());
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return animations().hit();
    }

    @Override
    public SoundEvent getSwingSound(InteractionHand hand) {
        return EpicFightSounds.WHOOSH.get();
    }

    @Override
    public SoundEvent getWeaponHitSound(InteractionHand hand) {
        return EpicFightSounds.BLADE_HIT.get();
    }
}
