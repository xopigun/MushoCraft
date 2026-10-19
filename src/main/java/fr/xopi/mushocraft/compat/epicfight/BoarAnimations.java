package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MonsterAction;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.HitAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.damagesource.StunType;

/**
 * The Terminate Boar's animations, made for MushoCraft (tools/bestiary/terminate_boar.py) on its own four-armed
 * skeleton: idle, heavy walk, charging run, a swipe of each side's claws, the two-armed smash, the tusk charge, the
 * roar that calls the dogs, flinch and death.
 */
public record BoarAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> swipeRight,
        AnimationAccessor<AttackAnimation> swipeLeft,
        AnimationAccessor<AttackAnimation> smash,
        AnimationAccessor<AttackAnimation> gore,
        AnimationAccessor<ActionAnimation> roar,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    /** Claws at the end of a forearm. */
    private static final Collider CLAWS = new OBBCollider(0.6, 0.8, 0.6, 0.0, -0.9, -0.2);
    /** Both fists coming down. */
    private static final Collider SMASH = new OBBCollider(0.9, 0.9, 0.9, 0.0, -1.0, -0.3);
    /** The tusks, in front of the head. */
    private static final Collider TUSKS = new OBBCollider(1.3, 0.7, 0.8, 0.0, -0.2, -0.9);

    public static BoarAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new BoarAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "swipe_r", accessor -> new AttackAnimation(0.12F, 0.28F, 0.32F, 0.5F, 0.9F, CLAWS,
                        armature.get().searchJointByName("Forearm_UR"), accessor, armature)),
                builder.nextAccessor(path + "swipe_l", accessor -> new AttackAnimation(0.12F, 0.28F, 0.32F, 0.5F, 0.9F, CLAWS,
                        armature.get().searchJointByName("Forearm_UL"), accessor, armature)),
                builder.nextAccessor(path + "smash", accessor -> new AttackAnimation(0.15F, 0.55F, 0.6F, 0.78F, 1.4F, SMASH,
                        armature.get().searchJointByName("Forearm_UR"), accessor, armature)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)),
                builder.nextAccessor(path + "gore", accessor -> new AttackAnimation(0.15F, 0.4F, 0.45F, 0.95F, 1.5F, TUSKS,
                        armature.get().searchJointByName("Head"), accessor, armature)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)),
                builder.nextAccessor(path + "roar", accessor -> new ActionAnimation(0.2F, accessor, armature)
                        .addProperty(ActionAnimationProperty.STOP_MOVEMENT, true)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return switch (action) {
            case CALL -> roar;
            default -> null;
        };
    }
}
