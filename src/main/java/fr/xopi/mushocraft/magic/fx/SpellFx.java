package fr.xopi.mushocraft.magic.fx;

import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.core.OrientedParticleOptions;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import fr.xopi.mushocraft.magic.spell.zone.ZoneBehaviors;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Shared strokes of effect drawn by the server for every client to see: shock waves across a path, slashes in the plane
 * of a swing, rings of fire or dust racing out over the ground. Techniques from the visual reference
 * (docs/VISUELS_REFERENCE.md), drawn with MushoCraft's own sprites.
 */
public final class SpellFx {
    private SpellFx() {
    }

    /** A ring of torn air across a direction, as a blast of wind or a shock leaves behind it. */
    public static void shockWave(ServerLevel level, Vec3 at, Vec3 facing, float size, int rgb) {
        float roll = level.random.nextFloat() * 360.0F;
        level.sendParticles(OrientedParticleOptions.facing(MushoParticles.SHOCK_WAVE.get(), facing, roll, size, rgb),
                at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    /** Shock waves strung along a path, tilted a little at random: the tunnel of air a violent blow drives before it. */
    public static void shockTunnel(ServerLevel level, Vec3 from, Vec3 direction, double length, double spacing, float size, int rgb) {
        Vec3 dir = direction.normalize();
        RandomSource random = level.random;
        for (double step = spacing; step <= length; step += spacing) {
            Vec3 tilt = dir.add((random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3);
            shockWave(level, from.add(dir.scale(step)), tilt, size * (float) (0.7 + 0.6 * step / length), rgb);
        }
    }

    /**
     * A slash in front of a fighter, in the plane of its swing: the swing turns about the way it looks, at a tilt chosen
     * at random (flat, rising or falling).
     */
    public static void slash(ServerLevel level, LivingEntity fighter, Vec3 look, float size, int rgb) {
        Vec3 dir = look.normalize();
        RandomSource random = level.random;
        // the swing plane's normal: "up" turned about the look by the tilt of the swing
        double tilt = (random.nextDouble() - 0.5) * Math.toRadians(90.0);
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 side = dir.cross(up);
        if (side.lengthSqr() < 1.0E-6) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        side = side.normalize();
        Vec3 planeUp = side.cross(dir).normalize();
        Vec3 normal = planeUp.scale(Math.cos(tilt)).add(side.scale(Math.sin(tilt)));
        Vec3 at = fighter.getEyePosition().add(dir.scale(1.4)).add(0.0, -0.25, 0.0);
        float roll = (float) Math.toDegrees(Math.atan2(dir.x, dir.z)) + (random.nextBoolean() ? 0.0F : 180.0F);
        level.sendParticles(OrientedParticleOptions.facing(MushoParticles.SLASH.get(), normal, roll, size, rgb),
                at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    /** A ring of particles racing out over the ground from a point, hugging the land, gone once it has spread. */
    public static void groundRing(ServerLevel level, LivingEntity caster, Vec3 centre, double radius, int spreadTicks,
                                  ParticleOptions particle) {
        SpellZone zone = SpellZone.create(level, caster, ZoneBehaviors.groundRing(particle, spreadTicks), ZoneVisual.NONE, radius,
                1.0, spreadTicks + 2);
        zone.setPos(centre);
        level.addFreshEntity(zone);
    }

    /**
     * Light gathering on a body as it is healed or blessed: motes drifting up all over it, and a ring of light opening at
     * its feet.
     */
    public static void healingLight(ServerLevel level, LivingEntity target, int rgb) {
        ColorParticleOption mote = ColorParticleOption.create(MushoParticles.RISING_LIGHT.get(), 0xFF000000 | rgb);
        double width = target.getBbWidth();
        level.sendParticles(mote, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 28,
                width * 0.45, target.getBbHeight() * 0.35, width * 0.45, 0.0);
        level.sendParticles(OrientedParticleOptions.facing(MushoParticles.SHOCK_WAVE.get(), new Vec3(0.0, 1.0, 0.0),
                level.random.nextFloat() * 360.0F, (float) (width * 1.4), rgb), target.getX(), target.getY() + 0.05, target.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);
    }

    /** Where a creature's hand is, held a little before it: its right hand, or its left one. */
    public static Vec3 hand(LivingEntity creature, boolean right) {
        double yaw = Math.toRadians(creature.yBodyRot);
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3 side = new Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw)).scale(right ? 1.0 : -1.0);
        return creature.position().add(0.0, creature.getBbHeight() * 0.55, 0.0)
                .add(forward.scale(0.35 * creature.getScale())).add(side.scale(0.38 * creature.getScale()));
    }

    /**
     * An object shaped out of an element in the hand (Ice Blade): motes of the element rush into the hand, which flashes
     * as the object takes form.
     */
    public static void conjureInHand(ServerLevel level, LivingEntity caster, int rgb) {
        Vec3 hand = hand(caster, true);
        ColorParticleOption mote = ColorParticleOption.create(MushoParticles.MANA_MOTE.get(), 0xFF000000 | rgb);
        RandomSource random = level.random;
        for (int i = 0; i < 16; i++) {
            Vec3 from = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5)
                    .normalize().scale(0.9);
            // a mote's speed is aimed so that it reaches the hand at the end of its life (ten ticks)
            level.sendParticles(mote, hand.x + from.x, hand.y + from.y, hand.z + from.z, 0, -from.x / 10.0, -from.y / 10.0,
                    -from.z / 10.0, 1.0);
        }
        level.sendParticles(ColorParticleOption.create(MushoParticles.IMPACT.get(), 0xFF000000 | rgb), hand.x, hand.y, hand.z,
                0, 0.9, 0.0, 0.0, 1.0);
    }

    /** Hands so hot they glow: motes of heat and sparks curling up from both. */
    public static void heatedHands(ServerLevel level, LivingEntity creature) {
        ColorParticleOption heat = ColorParticleOption.create(MushoParticles.RISING_LIGHT.get(), 0xFFFF8A30);
        for (boolean right : new boolean[]{true, false}) {
            Vec3 hand = hand(creature, right);
            level.sendParticles(heat, hand.x, hand.y, hand.z, 2, 0.06, 0.04, 0.06, 0.0);
            if (level.random.nextInt(2) == 0) {
                level.sendParticles(MushoParticles.EMBER.get(), hand.x, hand.y, hand.z, 1, 0.05, 0.05, 0.05, 0.02);
            }
        }
    }
}
