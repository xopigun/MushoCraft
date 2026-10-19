package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MonsterAction;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
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
 * The animations of a dog-like monster, made for MushoCraft (tools/bestiary/canine.py) on that species' own skeleton:
 * idle, walk, gallop, bite, pounce, flinch, death and the pack's howl.
 */
public record CanineAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> bite,
        AnimationAccessor<AttackAnimation> pounce,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death,
        AnimationAccessor<ActionAnimation> howl
) implements MonsterAnimationSet {
    /** The jaws, in front of the head joint. */
    private static final Collider JAWS = new OBBCollider(0.4, 0.35, 0.55, 0.0, -0.1, -0.55);
    /** The whole forequarters, in a leap. */
    private static final Collider FOREQUARTERS = new OBBCollider(0.55, 0.5, 0.75, 0.0, 0.0, -0.5);

    public static CanineAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new CanineAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "bite", accessor -> new AttackAnimation(0.1F, 0.3F, 0.33F, 0.48F, 0.8F, JAWS,
                        armature.get().searchJointByName("Head"), accessor, armature)),
                builder.nextAccessor(path + "pounce", accessor -> new AttackAnimation(0.1F, 0.36F, 0.42F, 0.66F, 1.15F,
                        FOREQUARTERS, armature.get().searchJointByName("Chest"), accessor, armature)
                        .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)),
                builder.nextAccessor(path + "howl", accessor -> new ActionAnimation(0.2F, accessor, armature)
                        .addProperty(ActionAnimationProperty.STOP_MOVEMENT, true)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return switch (action) {
            case CALL -> howl;
        };
    }
}
