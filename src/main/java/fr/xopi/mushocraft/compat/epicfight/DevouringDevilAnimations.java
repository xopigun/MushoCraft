package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MonsterAction;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.HitAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.model.Armature;

/**
 * The Devouring Devil's animations, made for MushoCraft (tools/bestiary/devouring_devil.py) on its own crawling
 * skeleton: idle, a quick crawl and run, the raking claw, the spring at a prey from afar (root motion of more than two
 * blocks), the stab of its tusked head, flinch, and the devil collapsing flat.
 */
public record DevouringDevilAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> claw,
        AnimationAccessor<AttackAnimation> pounce,
        AnimationAccessor<AttackAnimation> bite,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    /** The raking hand. */
    private static final Collider HAND = new OBBCollider(0.5, 0.7, 0.6, 0.0, -0.6, -0.3);
    /** Both hands thrust ahead as it lands. */
    private static final Collider LANDING = new OBBCollider(0.9, 0.6, 0.9, 0.0, 0.0, -0.8);
    /** The tusks at the end of its long head. */
    private static final Collider TUSKS = new OBBCollider(0.5, 0.5, 0.6, 0.0, -0.1, -0.9);

    public static DevouringDevilAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new DevouringDevilAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "claw", accessor -> new AttackAnimation(0.08F, 0.2F, 0.25F, 0.4F, 0.7F, HAND,
                        armature.get().searchJointByName("Forearm_R"), accessor, armature)),
                builder.nextAccessor(path + "pounce", accessor -> new AttackAnimation(0.08F, 0.3F, 0.35F, 0.6F, 1.0F, LANDING,
                        armature.get().searchJointByName("Chest"), accessor, armature)),
                builder.nextAccessor(path + "bite", accessor -> new AttackAnimation(0.08F, 0.25F, 0.3F, 0.45F, 0.8F, TUSKS,
                        armature.get().searchJointByName("Head"), accessor, armature)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return null;
    }
}
