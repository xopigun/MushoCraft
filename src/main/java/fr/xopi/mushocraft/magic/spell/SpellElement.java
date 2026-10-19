package fr.xopi.mushocraft.magic.spell;

import com.mojang.serialization.Codec;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * What a conjured projectile is made of: decides how it looks and what it does on impact.
 */
public enum SpellElement implements StringRepresentable {
    WATER(() -> ParticleTypes.SPLASH),
    ICE(() -> ParticleTypes.SNOWFLAKE),
    FIRE(MushoParticles.FLAME),
    EARTH(() -> ParticleTypes.ASH),
    WIND(() -> ParticleTypes.CLOUD),
    LIGHT(() -> ParticleTypes.END_ROD);

    public static final Codec<SpellElement> CODEC = StringRepresentable.fromEnum(SpellElement::values);

    private final Supplier<? extends ParticleOptions> trail;

    SpellElement(Supplier<? extends ParticleOptions> trail) {
        this.trail = trail;
    }

    public ParticleOptions trail() {
        return trail.get();
    }

    public ResourceLocation texture() {
        return MushoCraft.id("textures/entity/spell/" + getSerializedName() + ".png");
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
