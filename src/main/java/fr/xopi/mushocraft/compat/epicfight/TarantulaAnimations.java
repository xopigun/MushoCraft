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
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.model.Armature;

/**
 * The Tarantula Deathlord's animations, made for MushoCraft (tools/bestiary/tarantula_deathlord.py) on its own
 * eight-legged skeleton: idle, a walk and a run in alternating tetrapods, the bite (front legs reared, a lunge, the fangs
 * driven in), the web spit, flinch, and the spider dying with its legs curled in.
 */
public record TarantulaAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> bite,
        AnimationAccessor<ActionAnimation> spit,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    /** The fangs, in front of the head. */
    private static final Collider FANGS = new OBBCollider(0.7, 0.6, 0.8, 0.0, -0.1, -0.7);

    public static TarantulaAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new TarantulaAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "bite", accessor -> new AttackAnimation(0.1F, 0.25F, 0.32F, 0.5F, 0.8F, FANGS,
                        armature.get().searchJointByName("Head"), accessor, armature)),
                builder.nextAccessor(path + "spit", accessor -> new ActionAnimation(0.1F, accessor, armature)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return switch (action) {
            case THROW -> spit;
            default -> null;
        };
    }
}
