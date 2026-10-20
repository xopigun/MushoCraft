package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MonsterAction;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
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
 * The Iron Crawler's animations, made for MushoCraft (tools/bestiary/iron_crawler.py) on its own segmented skeleton:
 * idle, a crawl and a run with the legs rippling from head to tail, the ram (rearing, then rushing on more than two
 * blocks with all its weight: it knocks down), the snap of its front plate, flinch, and the crawler rolling over.
 */
public record IronCrawlerAnimations(
        AnimationAccessor<StaticAnimation> idle,
        AnimationAccessor<MovementAnimation> walk,
        AnimationAccessor<MovementAnimation> run,
        AnimationAccessor<AttackAnimation> ram,
        AnimationAccessor<AttackAnimation> bite,
        AnimationAccessor<HitAnimation> hit,
        AnimationAccessor<LongHitAnimation> death
) implements MonsterAnimationSet {
    /** The whole broad front of the carapace. */
    private static final Collider FRONT = MonsterColliders.box(0.8, 0.5, 0.6, 0.0, 0.0, 0.6);
    /** The front plate snapping. */
    private static final Collider SNAP = MonsterColliders.box(0.6, 0.4, 0.5, 0.0, -0.1, 0.5);

    public static IronCrawlerAnimations register(AnimationBuilder builder, String species, AssetAccessor<? extends Armature> armature) {
        String path = species + "/";
        return new IronCrawlerAnimations(
                builder.nextAccessor(path + "idle", accessor -> new StaticAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "walk", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "run", accessor -> new MovementAnimation(true, accessor, armature)),
                builder.nextAccessor(path + "ram", accessor -> new AttackAnimation(0.12F, 0.35F, 0.4F, 0.85F, 1.3F, FRONT,
                        armature.get().searchJointByName("Head"), accessor, armature)
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)),
                builder.nextAccessor(path + "bite", accessor -> new AttackAnimation(0.1F, 0.2F, 0.25F, 0.4F, 0.8F, SNAP,
                        armature.get().searchJointByName("Head"), accessor, armature)),
                builder.nextAccessor(path + "hit", accessor -> new HitAnimation(0.06F, accessor, armature)),
                builder.nextAccessor(path + "death", accessor -> new LongHitAnimation(0.16F, accessor, armature)));
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> action(MonsterAction action) {
        return null;
    }
}
