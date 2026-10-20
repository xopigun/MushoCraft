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
 * The Mad Skull's animations, made for MushoCraft (tools/bestiary/mad_skull.py) on its own headless skeleton: idle
 * (the skull sunk in the mud), heavy walk, run, the two-fisted slam, the mud throw, the casting gesture (the skull out
 * of the mud in all three), flinch, and the giant sinking into a heap.
 */
public record MadSkullAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> slam,
        AnimationAccessor<ActionAnimation> throwMud,
        AnimationAccessor<StaticAnimation> cast,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    /** Both fists coming down. */
    private static final Collider FISTS = MonsterColliders.box(1.2, 0.9, 1.0, 0.0, -1.0, 0.4);

    public static MadSkullAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new MadSkullAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "slam", accessor -> new AttackAnimation(0.15F, 0.55F, 0.6F, 0.78F, 1.4F, FISTS,
                        armature.get().searchJointByName("Chest"), accessor, armature)),
                builder.nextAccessor(path + "throw", accessor -> new ActionAnimation(0.12F, accessor, armature)),
                builder.nextAccessor(path + "cast", accessor -> new StaticAnimation(0.15F, false, accessor, armature)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return switch (action) {
            case THROW -> throwMud;
            case CAST -> cast;
            default -> null;
        };
    }
}
