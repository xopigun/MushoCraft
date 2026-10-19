package fr.xopi.mushocraft.core;

import com.mojang.serialization.MapCodec;
import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Particles of the mod, drawn for MushoCraft in a cel-shaded anime look (textures/particle).
 */
public final class MushoParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MushoCraft.MODID);

    /** A tongue of flame that licks upward and flickers out. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLAME = simple("flame");
    /** A glowing spark thrown off by fire; it drifts, falls and dies out. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMBER = simple("ember");
    /** A puff of pale smoke that swells as it rises. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMOKE = simple("smoke");
    /** The white-hot burst at the heart of an explosion; its size is given by the x speed. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLASH = simple("flash");
    /** A ring of air thrown out flat along the ground; its final radius is given by the x speed. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHOCK_RING = simple("shock_ring");

    /** A mote of mana drawn into the caster's hand as a spell takes shape; tinted by its school. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> MANA_MOTE =
            PARTICLE_TYPES.register("mana_mote", () -> new ParticleType<ColorParticleOption>(false) {
                @Override
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });

    private MushoParticles() {
    }

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> simple(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
    }

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
