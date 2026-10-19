package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MonsterAction;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;

/**
 * The animations of one monster species: those its patch plays for movement and hits, and those of its gestures.
 */
public interface MonsterAnimationSet {
    AnimationAccessor<? extends StaticAnimation> action(MonsterAction action);
}
