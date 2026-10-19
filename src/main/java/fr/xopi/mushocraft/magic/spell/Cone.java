package fr.xopi.mushocraft.magic.spell;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * A cone of space from an apex (a caster's eyes) along an axis (where they look), out to a range: what a blast of wind
 * or a jet of flame sweeps through.
 *
 * @param minCosine the cosine of the half angle: a way is inside when its cosine with the axis is at least this
 */
public record Cone(Vec3 apex, Vec3 axis, double range, double minCosine) {
    public static Cone of(Vec3 apex, Vec3 axis, double range, double halfAngleDegrees) {
        return new Cone(apex, axis.normalize(), range, Math.cos(Math.toRadians(halfAngleDegrees)));
    }

    /** Whether a point lies inside the cone, within its range. */
    public boolean contains(Vec3 point) {
        Vec3 toPoint = point.subtract(apex);
        double distance = toPoint.length();
        return distance <= range && distance > 1.0E-4 && toPoint.scale(1.0 / distance).dot(axis) >= minCosine;
    }

    /** The sine of the half angle: how far the cone opens per block along its axis. */
    public double opening() {
        return Math.sqrt(Math.max(0.0, 1.0 - minCosine * minCosine));
    }

    /** A way picked evenly at random inside the cone. */
    public Vec3 randomWay(RandomSource random) {
        double cosine = 1.0 - random.nextDouble() * (1.0 - minCosine);
        double sine = Math.sqrt(Math.max(0.0, 1.0 - cosine * cosine));
        double turn = random.nextDouble() * Math.PI * 2.0;
        Vec3 across = Math.abs(axis.y) < 0.95 ? axis.cross(new Vec3(0.0, 1.0, 0.0)).normalize() : new Vec3(1.0, 0.0, 0.0);
        Vec3 other = axis.cross(across).normalize();
        return axis.scale(cosine).add(across.scale(sine * Math.cos(turn))).add(other.scale(sine * Math.sin(turn))).normalize();
    }
}
