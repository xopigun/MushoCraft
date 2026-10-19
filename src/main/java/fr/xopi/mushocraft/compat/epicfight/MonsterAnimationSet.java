package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MonsterAction;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;

import javax.annotation.Nullable;

/**
 * The animations of one monster species: those its patch plays for movement and hits, and those of its gestures.
 */
public interface MonsterAnimationSet {
    /** The species' animation for a gesture, or null if it does not make that gesture. */
    @Nullable
    AnimationAccessor<? extends StaticAnimation> action(MonsterAction action);
}
