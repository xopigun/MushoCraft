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
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.damagesource.StunType;

/**
 * A bear's animations, made for MushoCraft (tools/bestiary/luster_grizzly.py) on its own four-legged skeleton: idle,
 * walk, run, a mauling bite, rearing up to bring both forepaws down, the standing roar, flinch and death.
 */
public record BearAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> maul,
        AnimationAccessor<AttackAnimation> swipe,
        AnimationAccessor<ActionAnimation> roar,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    private static final Collider JAWS = MonsterColliders.box(0.55, 0.5, 0.6, 0.0, -0.1, 0.6);
    /** Both forepaws coming down in front of the chest. */
    private static final Collider FOREPAWS = MonsterColliders.box(0.95, 0.8, 0.9, 0.0, -0.6, 1.0);

    public static BearAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new BearAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "maul", accessor -> new AttackAnimation(0.12F, 0.32F, 0.36F, 0.52F, 0.9F, JAWS,
                        armature.get().searchJointByName("Head"), accessor, armature)),
                builder.nextAccessor(path + "swipe", accessor -> new AttackAnimation(0.15F, 0.6F, 0.68F, 0.88F, 1.5F, FOREPAWS,
                        armature.get().searchJointByName("Chest"), accessor, armature)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)),
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
