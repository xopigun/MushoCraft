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
 * The Armored Warrior's animations, made for MushoCraft (tools/bestiary/armored_warrior.py) on its own four-armed
 * skeleton: guard stance, walk, run, a cross cut of the lower blades, an overhead blow of the upper pair followed by a
 * thrust, Flow's parry, flinch, and the suit falling apart.
 */
public record ArmoredWarriorAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> crossSlash,
        AnimationAccessor<AttackAnimation> overhead,
        AnimationAccessor<ActionAnimation> parry,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    /** A long blade, down the line of the forearm. */
    private static final Collider BLADE = new OBBCollider(0.3, 0.9, 0.3, 0.0, -1.35, 0.0);

    public static ArmoredWarriorAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new ArmoredWarriorAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "cross_slash", accessor -> new AttackAnimation(0.12F, 0.28F, 0.32F, 0.5F, 0.9F, BLADE,
                        armature.get().searchJointByName("Forearm_LR"), accessor, armature)),
                builder.nextAccessor(path + "overhead", accessor -> new AttackAnimation(0.12F, 0.38F, 0.42F, 0.72F, 1.1F, BLADE,
                        armature.get().searchJointByName("Forearm_UR"), accessor, armature)),
                builder.nextAccessor(path + "parry", accessor -> new ActionAnimation(0.05F, accessor, armature)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return switch (action) {
            case COUNTER -> parry;
            default -> null;
        };
    }
}
