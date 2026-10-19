package fr.xopi.mushocraft.magic.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * The look of a {@link SpellPillar} (a pillar, a spike or a mound): which Blockbench model (made from anime references, played by GeckoLib), which
 * texture, and how it breaks up.
 */
public enum PillarKind {
    /** Ice Pillar: a faceted shaft of ice with crystals at its foot. */
    ICE_PILLAR("ice_pillar", "ice_crystal", 3.4F, true, ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_BREAK),
    /** Earth Lance: a jagged, layered spike of brown rock. */
    EARTH_LANCE("earth_spike", "earth_rock", 2.75F, false, ParticleTypes.ASH, SoundEvents.ROOTED_DIRT_BREAK),
    /** Earth Hedgehog: dark stone spikes, as in the anime. */
    EARTH_SPIKE("earth_spike", "dark_stone", 2.75F, false, ParticleTypes.ASH, SoundEvents.STONE_BREAK),
    /** Icicle Field: a billowing mound of frost that swallows what it catches, as in the anime. */
    FROST_MOUND("frost_mound", "frost", 2.13F, false, ParticleTypes.SNOWFLAKE, SoundEvents.POWDER_SNOW_BREAK);

    private final String model;
    private final String texture;
    private final float modelHeight;
    private final boolean translucent;
    private final ParticleOptions debris;
    private final SoundEvent breakSound;

    PillarKind(String model, String texture, float modelHeight, boolean translucent, ParticleOptions debris, SoundEvent breakSound) {
        this.model = model;
        this.texture = texture;
        this.modelHeight = modelHeight;
        this.translucent = translucent;
        this.debris = debris;
        this.breakSound = breakSound;
    }

    /** The GeckoLib model (geo/entity/&lt;model&gt;.geo.json) and its animations (animation.&lt;model&gt;.rise, idle, crumble). */
    public String model() {
        return model;
    }

    /** textures/entity/spell/&lt;texture&gt;.png */
    public String texture() {
        return texture;
    }

    /** Height of the model at scale 1, in blocks. */
    public float modelHeight() {
        return modelHeight;
    }

    public boolean translucent() {
        return translucent;
    }

    public ParticleOptions debris() {
        return debris;
    }

    public SoundEvent breakSound() {
        return breakSound;
    }
}
