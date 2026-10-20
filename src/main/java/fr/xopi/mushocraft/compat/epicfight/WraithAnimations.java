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
 * The wraith's animations, made for MushoCraft (tools/bestiary/wraith.py) on its own legless skeleton: floating idle,
 * drifting, a raking claw, hands held out while a spell gathers, arms flung up to raise the dead, flinch and death.
 */
public record WraithAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> claw,
        AnimationAccessor<StaticAnimation> cast,
        AnimationAccessor<ActionAnimation> raiseDead,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    private static final Collider CLAWS = new OBBCollider(0.5, 0.6, 0.5, 0.0, -0.75, -0.2);

    public static WraithAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new WraithAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "claw", accessor -> new AttackAnimation(0.1F, 0.26F, 0.3F, 0.48F, 0.9F, CLAWS,
                        armature.get().searchJointByName("Forearm_R"), accessor, armature)),
                builder.nextAccessor(path + "cast", accessor -> new StaticAnimation(0.15F, false, accessor, armature)),
                builder.nextAccessor(path + "raise_dead", accessor -> new ActionAnimation(0.2F, accessor, armature)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return switch (action) {
            case CALL -> raiseDead;
            case CAST -> cast;
            default -> null;
        };
    }
}
