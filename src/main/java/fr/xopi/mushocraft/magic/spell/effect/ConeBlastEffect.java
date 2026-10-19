package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * A wide shockwave in front of the caster (Sonic Blast): every creature inside the cone is hurt and thrown back.
 * The wave does not break blocks (docs/ECARTS.md).
 */
public final class ConeBlastEffect implements SpellEffect {
    private final double range;
    private final double minCosine;
    private final float damage;
    private final double knockback;
    private final ParticleOptions particle;
    private final SoundEvent sound;

    /**
     * @param halfAngleDegrees half the opening of the cone
     */
    public ConeBlastEffect(double range, double halfAngleDegrees, float damage, double knockback,
                           ParticleOptions particle, SoundEvent sound) {
        this.range = range;
        this.minCosine = Math.cos(Math.toRadians(halfAngleDegrees));
        this.damage = damage;
        this.knockback = knockback;
        this.particle = particle;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        Vec3 eyes = context.eyes();
        Vec3 look = context.look();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, context.caster().getBoundingBox().inflate(range),
                entity -> entity != context.caster() && entity.isAlive())) {
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eyes);
            double distance = toTarget.length();
            if (distance > range || distance < 1.0E-4 || toTarget.normalize().dot(look) < minCosine
                    || !context.caster().hasLineOfSight(target)) {
                continue;
            }
            target.hurt(MushoDamageTypes.spell(level, context.caster(), context.caster()), (float) (damage * context.power()));
            Vec3 push = toTarget.normalize().scale(knockback * (1.0 - distance / range * 0.5));
            target.push(push.x, 0.2 + push.y * 0.3, push.z);
            target.hurtMarked = true;
        }
        for (double step = 1.0; step <= range; step += 1.0) {
            Vec3 point = eyes.add(look.scale(step));
            double spread = step * Math.sqrt(1.0 - minCosine * minCosine) * 0.5;
            level.sendParticles(particle, point.x, point.y, point.z, 6, spread, spread * 0.5, spread, 0.05);
        }
        level.playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.2F, 0.8F);
        return true;
    }
}
