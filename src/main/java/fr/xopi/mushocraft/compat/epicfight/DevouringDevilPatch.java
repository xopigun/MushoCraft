package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.DevouringDevil;
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
 * Patch of the Devouring Devil: from afar it springs at its prey; close in, it rakes with its claws or stabs with its
 * tusked head. Lying low in campfire smoke, it no longer springs (see {@code DevouringDevil#isSmoked}).
 */
public class DevouringDevilPatch<T extends MushoMonster> extends MonsterPatch<T> {
    private final Supplier<DevouringDevilAnimations> animations;

    public DevouringDevilPatch(T monster, Supplier<DevouringDevilAnimations> animations) {
        super(monster);
        this.animations = animations;
    }

    @Override
    public DevouringDevilAnimations animations() {
        return animations.get();
    }

    @Override
    protected CombatBehaviors.Builder<MonsterPatch<?>> combatBehaviors() {
        DevouringDevilAnimations a = animations();
        return CombatBehaviors.<MonsterPatch<?>>builder()
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(40.0F).canBeInterrupted(false).looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.pounce())
                                .withinDistance(3.2, 6.5).custom(DevouringDevilPatch::maySpring)))
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(50.0F).canBeInterrupted(false).looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.claw())
                                .withinDistance(0.0, 2.6).custom(MonsterPatch::mayStrike)))
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(30.0F).canBeInterrupted(false).looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.bite())
                                .withinDistance(0.0, 2.4).custom(MonsterPatch::mayStrike)));
    }

    private static boolean maySpring(MonsterPatch<?> patch) {
        return patch.mayStrike() && !(patch.getOriginal() instanceof DevouringDevil devil && devil.isSmoked());
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        DevouringDevilAnimations a = animations();
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
