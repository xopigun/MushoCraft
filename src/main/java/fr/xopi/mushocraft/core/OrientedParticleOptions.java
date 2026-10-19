package fr.xopi.mushocraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * A particle drawn as a flat plane facing a given way rather than the eye: a shock wave across a projectile's path, a
 * slash in the plane of the swing. Carries the plane's facing (yaw, pitch, and a roll within the plane), its size and its
 * tint.
 */
public record OrientedParticleOptions(ParticleType<OrientedParticleOptions> type, float yaw, float pitch, float roll,
                                      float size, int rgb) implements ParticleOptions {

    public static MapCodec<OrientedParticleOptions> codec(ParticleType<OrientedParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("yaw").forGetter(OrientedParticleOptions::yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(OrientedParticleOptions::pitch),
                Codec.FLOAT.optionalFieldOf("roll", 0.0F).forGetter(OrientedParticleOptions::roll),
                Codec.FLOAT.fieldOf("size").forGetter(OrientedParticleOptions::size),
                Codec.INT.optionalFieldOf("rgb", 0xFFFFFF).forGetter(OrientedParticleOptions::rgb)
        ).apply(instance, (yaw, pitch, roll, size, rgb) -> new OrientedParticleOptions(type, yaw, pitch, roll, size, rgb)));
    }

    public static StreamCodec<ByteBuf, OrientedParticleOptions> streamCodec(ParticleType<OrientedParticleOptions> type) {
        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, OrientedParticleOptions::yaw,
                ByteBufCodecs.FLOAT, OrientedParticleOptions::pitch,
                ByteBufCodecs.FLOAT, OrientedParticleOptions::roll,
                ByteBufCodecs.FLOAT, OrientedParticleOptions::size,
                ByteBufCodecs.INT, OrientedParticleOptions::rgb,
                (yaw, pitch, roll, size, rgb) -> new OrientedParticleOptions(type, yaw, pitch, roll, size, rgb));
    }

    /** A plane facing along a direction (its normal), e.g. a projectile's flight. */
    public static OrientedParticleOptions facing(ParticleType<OrientedParticleOptions> type, Vec3 direction, float roll,
                                                 float size, int rgb) {
        Vec3 dir = direction.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
        float yaw = (float) (Mth.atan2(-dir.x, dir.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Math.asin(Mth.clamp(-dir.y, -1.0, 1.0)) * Mth.RAD_TO_DEG);
        return new OrientedParticleOptions(type, yaw, pitch, roll, size, rgb);
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
