package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MonsterAction;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.HitAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;

/**
 * The Snow Drake's animations, made for MushoCraft (tools/bestiary/snow_drake.py) on its own lizard skeleton: idle,
 * a sprawling walk and run with the spine bending in an S, the bite (head drawn back, a lunge, the jaws snapping shut),
 * the tail lash, rearing up to roar the pack together, flinch, and the drake rolling over onto its side.
 */
public record SnowDrakeAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> bite,
        AnimationAccessor<AttackAnimation> tailSweep,
        AnimationAccessor<ActionAnimation> rear,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    /** The jaws, in front of the head. */
    private static final Collider JAWS = MonsterColliders.box(0.6, 0.45, 0.7, 0.0, -0.1, 0.6);
    /** The tail lashing round: wide, to catch whoever stands beside or behind. */
    private static final Collider TAIL = MonsterColliders.box(1.4, 0.6, 1.4, 0.0, 0.0, -0.6);

    public static SnowDrakeAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new SnowDrakeAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "bite", accessor -> new AttackAnimation(0.12F, 0.25F, 0.3F, 0.45F, 0.9F, JAWS,
                        armature.get().searchJointByName("Head"), accessor, armature)),
                builder.nextAccessor(path + "tail_sweep", accessor -> new AttackAnimation(0.12F, 0.3F, 0.4F, 0.72F, 1.2F, TAIL,
                        armature.get().searchJointByName("Tail2"), accessor, armature)),
                builder.nextAccessor(path + "rear", accessor -> new ActionAnimation(0.15F, accessor, armature)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return switch (action) {
            case CALL -> rear;
            default -> null;
        };
    }
}
