package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.Cone;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A wide shockwave in front of the caster (Sonic Blast): every creature inside the cone is hurt and thrown back.
 * The wave can also be scorching (Blast: hot wind sets alight) or freezing (Bora Blast: an icy gust that freezes and
 * numbs). The wave does not break blocks (docs/ECARTS.md).
 */
public final class ConeBlastEffect implements SpellEffect {
    private static final int WAVE_COLOUR = 0xEEF6FF;

    private static final int MOTES_PER_BLOCK = 12;

    private final double range;
    private final double minCosine;
    private final float damage;
    private final double knockback;
    private final ParticleOptions particle;
    private final SoundEvent sound;
    private final int igniteSeconds;
    private final int freezeTicks;
    private final List<CloudEffect.Affliction> afflictions;

    /**
     * @param halfAngleDegrees half the opening of the cone
     */
    public ConeBlastEffect(double range, double halfAngleDegrees, float damage, double knockback,
                           ParticleOptions particle, SoundEvent sound) {
        this(range, Math.cos(Math.toRadians(halfAngleDegrees)), damage, knockback, particle, sound, 0, 0, List.of());
    }

    private ConeBlastEffect(double range, double minCosine, float damage, double knockback, ParticleOptions particle,
                            SoundEvent sound, int igniteSeconds, int freezeTicks, List<CloudEffect.Affliction> afflictions) {
        this.range = range;
        this.minCosine = minCosine;
        this.damage = damage;
        this.knockback = knockback;
        this.particle = particle;
        this.sound = sound;
        this.igniteSeconds = igniteSeconds;
        this.freezeTicks = freezeTicks;
        this.afflictions = List.copyOf(afflictions);
    }

    /** The same wave, scorching: it sets alight whoever it catches. */
    public ConeBlastEffect igniting(int seconds) {
        return new ConeBlastEffect(range, minCosine, damage, knockback, particle, sound, seconds, freezeTicks, afflictions);
    }

    /** The same wave, freezing: whoever it catches is frozen for a while and suffers the given conditions. */
    public ConeBlastEffect freezing(int ticks, CloudEffect.Affliction... conditions) {
        return new ConeBlastEffect(range, minCosine, damage, knockback, particle, sound, igniteSeconds, ticks, List.of(conditions));
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        Vec3 eyes = context.eyes();
        Vec3 look = context.look();
        Cone cone = new Cone(eyes, look, range, minCosine);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, context.caster().getBoundingBox().inflate(range),
                entity -> entity != context.caster() && entity.isAlive())) {
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eyes);
            double distance = toTarget.length();
            if (!cone.contains(target.getBoundingBox().getCenter()) || !context.caster().hasLineOfSight(target)) {
                continue;
            }
            if (damage > 0.0F) {
                // a mere gust (Wind Blast) pushes without hurting
                target.hurt(MushoDamageTypes.spell(level, context.caster(), context.caster()), (float) (damage * context.power()));
            }
            Vec3 push = toTarget.normalize().scale(knockback * (1.0 - distance / range * 0.5));
            target.push(push.x, 0.2 + push.y * 0.3, push.z);
            target.hurtMarked = true;
            if (igniteSeconds > 0) {
                target.igniteForSeconds(igniteSeconds);
            }
            if (freezeTicks > 0) {
                target.clearFire();
                target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + freezeTicks));
            }
            afflictions.forEach(affliction -> target.addEffect(
                    new MobEffectInstance(affliction.effect(), affliction.durationTicks(), affliction.amplifier()), context.caster()));
        }
        SpellFx.jet(level, cone, particle, (int) Math.round(range * MOTES_PER_BLOCK), 1.6);
        // the wave drives a tunnel of torn air before it
        SpellFx.shockTunnel(level, eyes, look, range, 1.5, (float) (cone.opening() * 2.0), WAVE_COLOUR);
        level.playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.2F, 0.8F);
        return true;
    }
}
