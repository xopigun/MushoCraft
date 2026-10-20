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
 * Patch of the dog-like monsters: they bite at close range, sometimes twice in a row, and leap at prey a few blocks
 * away. Every attack waits for the monster to hold a place against its prey.
 */
public class CaninePatch<T extends MushoMonster> extends MonsterPatch<T> {
    private final Supplier<CanineAnimations> animations;

    public CaninePatch(T monster, Supplier<CanineAnimations> animations) {
        super(monster);
        this.animations = animations;
    }

    @Override
    public CanineAnimations animations() {
        return animations.get();
    }

    @Override
    protected CombatBehaviors.Builder<MonsterPatch<?>> combatBehaviors() {
        CanineAnimations a = animations();
        return CombatBehaviors.<MonsterPatch<?>>builder()
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.bite())
                                .withinDistance(0.0, 2.6).custom(MonsterPatch::mayStrike))
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.bite()).randomChance(0.35F)
                                .withinDistance(0.0, 2.6).custom(MonsterPatch::mayStrike)))
                .newBehaviorSeries(BehaviorSeries.<MonsterPatch<?>>builder().weight(45.0F).cooldown(100).canBeInterrupted(false)
                        .looping(false)
                        .nextBehavior(Behavior.<MonsterPatch<?>>builder().animationBehavior(a.pounce())
                                .withinDistance(3.0, 6.5).withinAngleHorizontal(0.0, 25.0).custom(MonsterPatch::mayStrike)));
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        CanineAnimations a = animations();
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
        return EpicFightSounds.BLUNT_HIT.get();
    }
}
