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
 * Patch of the Snow Drake: in reach, a bite, or the tail lashed round at whoever stands close; rearing to roar the
 * pack together is its own goal (see {@code CallThePackGoal}).
 */
public class SnowDrakePatch<T extends MushoMonster> extends MonsterPatch<T> {
    private final Supplier<SnowDrakeAnimations> animations;

    public SnowDrakePatch(T monster, Supplier<SnowDrakeAnimations> animations) {
        super(monster);
        this.animations = animations;
    }

    @Override
    public SnowDrakeAnimations animations() {
        return animations.get();
    }

    @Override
    protected CombatBehaviors.Builder<MonsterPatch<?>> combatBehaviors() {
        SnowDrakeAnimations a = animations();
        return CombatBehaviors.<MonsterPatch<?>>builder()
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(60.0F).canBeInterrupted(false).looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.bite())
                                .withinDistance(0.0, 2.6).custom(MonsterPatch::mayStrike)))
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(40.0F).canBeInterrupted(false).looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.tailSweep())
                                .withinEyeHeight().withinDistance(0.0, 3.4).custom(MonsterPatch::mayStrike)));
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        SnowDrakeAnimations a = animations();
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
        return EpicFightSounds.WHOOSH_BIG.get();
    }

    @Override
    public SoundEvent getWeaponHitSound(InteractionHand hand) {
        return EpicFightSounds.BLUNT_HIT_HARD.get();
    }
}
