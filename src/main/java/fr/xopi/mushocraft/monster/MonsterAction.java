package fr.xopi.mushocraft.monster;

/**
 * Gestures a monster makes outside its attacks, each played with the species' own animation.
 */
public enum MonsterAction {
    /** The pack's rallying call: a howl, a roar; for a wraith, raising the dead. */
    CALL,
    /** Gathering a spell while chanting it. */
    CAST,
    /** Taking a receiving stance (the Water God Style's Flow). */
    COUNTER,
    /** Throwing something at a foe out of reach. */
    THROW
}
