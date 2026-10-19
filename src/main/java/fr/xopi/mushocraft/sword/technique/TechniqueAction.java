package fr.xopi.mushocraft.sword.technique;

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
}
