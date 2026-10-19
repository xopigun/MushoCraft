package fr.xopi.mushocraft.magic.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nullable;

/**
 * The look of a {@link SpellPillar} (a pillar, a spike or a mound): which texture, how it breaks up, and how it is
 * drawn. Solids (ice, rock) are Blockbench models made from anime references, played by GeckoLib; soft shapes (frost)
 * are smooth meshes built by the client's code (choice of Xopi, 19/10).
 */
public enum PillarKind {
    /** Ice Pillar: a faceted shaft of ice with crystals at its foot. */
    ICE_PILLAR("ice_pillar", "ice_crystal", 3.4F, true, ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_BREAK),
    /** Earth Lance: a jagged, layered spike of brown rock. */
    EARTH_LANCE("earth_spike", "earth_rock", 2.75F, false, ParticleTypes.ASH, SoundEvents.ROOTED_DIRT_BREAK),
    /** Ice Lance: the same jagged spike, of ice. */
    ICE_LANCE("earth_spike", "ice_crystal", 2.75F, true, ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_BREAK),
    /** Earth Hedgehog: dark stone spikes, as in the anime. */
    EARTH_SPIKE("earth_spike", "dark_stone", 2.75F, false, ParticleTypes.ASH, SoundEvents.STONE_BREAK),
    /** Earth Wall: a wall of rough stone slabs, stacked unevenly, bursting up out of the ground. */
    EARTH_WALL("earth_wall", "earth_rock", 3.0F, false, ParticleTypes.ASH, SoundEvents.STONE_BREAK),
    /** Earth Fortress: four such walls and a roof of flat stones round the caster. */
    EARTH_FORTRESS("earth_fortress", "earth_rock", 3.69F, false, ParticleTypes.ASH, SoundEvents.STONE_BREAK),
    /** Ice Fortress: the walls and roof of the earth fortress, of ice one can half see through. */
    ICE_FORTRESS("earth_fortress", "ice_crystal", 3.69F, true, ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_BREAK),
    /** Icicle Field: a billowing mound of frost that swallows what it catches, as in the anime. */
    FROST_MOUND(null, "frost", 1.0F, false, ParticleTypes.SNOWFLAKE, SoundEvents.POWDER_SNOW_BREAK);

    @Nullable
    private final String model;
    private final String texture;
    private final float modelHeight;
    private final boolean translucent;
    private final ParticleOptions debris;
    private final SoundEvent breakSound;

    PillarKind(@Nullable String model, String texture, float modelHeight, boolean translucent, ParticleOptions debris, SoundEvent breakSound) {
        this.model = model;
        this.texture = texture;
        this.modelHeight = modelHeight;
        this.translucent = translucent;
        this.debris = debris;
        this.breakSound = breakSound;
    }

    /**
     * The GeckoLib model (geo/entity/&lt;model&gt;.geo.json) and its animations (animation.&lt;model&gt;.rise, idle,
     * crumble). Only for {@link #isBlockbench() Blockbench} kinds.
     */
    public String model() {
        if (model == null) {
            throw new IllegalStateException(this + " is drawn as a mesh, it has no Blockbench model");
        }
        return model;
    }

    /** Whether this is a Blockbench model; otherwise a mesh built by the client. */
    public boolean isBlockbench() {
        return model != null;
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
