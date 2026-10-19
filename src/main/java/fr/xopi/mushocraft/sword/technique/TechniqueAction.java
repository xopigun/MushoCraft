package fr.xopi.mushocraft.sword.technique;

import net.minecraft.world.entity.LivingEntity;

/**
 * What a sword technique does (Strategy pattern, like {@code SpellEffect} for spells). One class per behaviour.
 */
@FunctionalInterface
public interface TechniqueAction {
    /**
     * @return false when the technique could not be performed (wrong stance, nothing to throw...):
     *         no cooldown is started then
     */
    boolean perform(TechniqueContext context);

    /** Closest distance to the target at which the action is worth using (for creatures choosing a technique). */
    default double minRange() {
        return 0.0;
    }

    /** Whether this user can perform the action at all (some are a player's alone, like shifting one's aura). */
    default boolean usableBy(LivingEntity user) {
        return true;
    }

    /** Farthest distance to the target at which the action reaches it. */
    default double maxRange() {
        return 3.5;
    }
}
