package fr.xopi.mushocraft.magic.spell;

/**
 * What a spell is for, so that a creature casting spells knows when to use it.
 */
public enum SpellIntent {
    /** Aimed at an enemy. */
    ATTACK,
    /** Raised when an enemy comes close: walls, barriers, fortresses. */
    DEFEND,
    /** Cast on oneself when hurt: healing, curing. */
    SUPPORT,
    /** Out of combat or for oneself only: conjured weapons, weather. */
    UTILITY
}
