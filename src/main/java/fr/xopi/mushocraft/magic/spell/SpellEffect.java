package fr.xopi.mushocraft.magic.spell;

/**
 * What a spell does once its incantation is complete (Strategy pattern). One class per behaviour:
 * spells that share a behaviour share the class and differ only by their parameters.
 */
@FunctionalInterface
public interface SpellEffect {
    /**
     * @return false when the spell could not take shape (no target, no room...): the mana is then not spent
     */
    boolean cast(SpellContext context);
}
