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
 * The goblin's animations, made for MushoCraft (tools/bestiary/goblin.py) on its own small hunched skeleton: idle,
 * walk, run, a sideways club blow, an overhead blow, the screech that gathers the gang, flinch and death.
 */
public record GoblinAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> swing,
        AnimationAccessor<AttackAnimation> overhead,
        AnimationAccessor<ActionAnimation> shout,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    /** The club's head, held forwards from the right fist. */
    private static final Collider CLUB = new OBBCollider(0.3, 0.3, 0.6, 0.0, -0.4, -0.55);

    public static GoblinAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new GoblinAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "swing", accessor -> new AttackAnimation(0.1F, 0.24F, 0.28F, 0.44F, 0.8F, CLUB,
                        armature.get().searchJointByName("Forearm_R"), accessor, armature)),
                builder.nextAccessor(path + "overhead", accessor -> new AttackAnimation(0.1F, 0.38F, 0.42F, 0.58F, 1.0F, CLUB,
                        armature.get().searchJointByName("Forearm_R"), accessor, armature)),
                builder.nextAccessor(path + "shout", accessor -> new ActionAnimation(0.15F, accessor, armature)
                        .addProperty(ActionAnimationProperty.STOP_MOVEMENT, true)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return switch (action) {
            case CALL -> shout;
        };
    }
}
