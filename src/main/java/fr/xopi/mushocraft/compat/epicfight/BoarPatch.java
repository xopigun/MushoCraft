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
 * Patch of the Terminate Boar: claw swipes from one side then the other, sometimes ended by the two-armed smash that
 * knocks down; the smash alone now and then; and the tusk charge at prey that keeps four to ten blocks away.
 */
public class BoarPatch<T extends MushoMonster> extends MonsterPatch<T> {
    private final Supplier<BoarAnimations> animations;

    public BoarPatch(T monster, Supplier<BoarAnimations> animations) {
        super(monster);
        this.animations = animations;
    }

    @Override
    public BoarAnimations animations() {
        return animations.get();
    }

    @Override
    protected CombatBehaviors.Builder<MonsterPatch<?>> combatBehaviors() {
        BoarAnimations a = animations();
        return CombatBehaviors.<MonsterPatch<?>>builder()
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.swipeRight())
                                .withinEyeHeight().withinDistance(0.0, 3.4).custom(MonsterPatch::mayStrike))
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.swipeLeft()).randomChance(0.7F)
                                .withinDistance(0.0, 3.6).custom(MonsterPatch::mayStrike))
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.smash()).randomChance(0.4F)
                                .withinDistance(0.0, 3.6).custom(MonsterPatch::mayStrike)))
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(40.0F).cooldown(80).canBeInterrupted(false)
                        .looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.smash())
                                .withinEyeHeight().withinDistance(0.0, 3.2).custom(MonsterPatch::mayStrike)))
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(60.0F).cooldown(160).canBeInterrupted(false)
                        .looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.gore())
                                .withinDistance(4.0, 10.0).withinAngleHorizontal(0.0, 20.0).custom(MonsterPatch::mayStrike)));
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        BoarAnimations a = animations();
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
