package fr.xopi.mushocraft.magic.entity;

/**
 * What a {@link SpellZone} looks like on the client (its behaviour stays on the server).
 */
public enum ZoneVisual {
    /** Nothing but the behaviour's own particles. */
    NONE,
    /** Tornado: a towering, twisting funnel of wind. */
    TORNADO,
    /** Magic Shield: a faint bubble of blue hexagons around the caster. */
    WARD_MAGIC,
    /** Physical Shield: a bubble of amber hexagons around the caster. */
    WARD_PHYSICAL,
    /** Spirit-Sealing Barrier: a great golden dome. */
    SEALING_DOME,
    /** Confinement Barrier: a narrow golden cell round its prisoner. */
    CONFINEMENT_CELL,
    /** Waterfall: water pouring down without end. */
    WATERFALL,
    /** Flood Flush: a sheet of water spreading over the ground. */
    FLOOD,
    /** Deep Mist: banks of thick fog. */
    MIST,
    /** Dust Storm, Sandstorm: banks of dust whirling round. */
    DUST_STORM,
    /** Cumulonimbus: a wide, dark thundercloud overhead. */
    STORM_CLOUD,
    /** Lightning: a dark cloud gathering over its target, flashing before the bolt. */
    THUNDERHEAD,
    /** Electric: the same cloud compressed small and low over its target, gathering fast. */
    CHARGED_CLOUD,
    /** Flamethrower: a torrent of flame poured where the caster it follows looks. */
    FLAME_JET,
    /** Water Splash: a crown of water leaping up and falling back. */
    SPLASH_CROWN
}
