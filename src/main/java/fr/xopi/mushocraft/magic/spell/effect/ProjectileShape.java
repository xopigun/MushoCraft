package fr.xopi.mushocraft.magic.spell.effect;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * The form a conjured projectile takes, which the client draws with its own animated model.
 */
public enum ProjectileShape implements StringRepresentable {
    /** A round mass (Water Ball, Fireball): stretches with speed. */
    ORB,
    /** A slender, pointed bolt (Water Arrow). */
    BOLT,
    /** A cluster of jagged crystals (Ice Smash). */
    CRYSTAL_CLUSTER;

    public static final Codec<ProjectileShape> CODEC = StringRepresentable.fromEnum(ProjectileShape::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
