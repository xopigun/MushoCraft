package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A lingering cloud where the caster aims (Dust Storm): everyone inside suffers its conditions.
 * Uses Minecraft's own area effect cloud, with the caster as owner.
 */
public final class CloudEffect implements SpellEffect {
    /** A condition the cloud inflicts. */
    public record Affliction(Holder<MobEffect> effect, int durationTicks, int amplifier) {
    }

    private final float radius;
    private final int durationTicks;
    private final double range;
    private final List<Affliction> afflictions;
    private final ParticleOptions particle;
    private final SoundEvent sound;

    public CloudEffect(float radius, int durationTicks, double range, List<Affliction> afflictions,
                       ParticleOptions particle, SoundEvent sound) {
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.range = range;
        this.afflictions = List.copyOf(afflictions);
        this.particle = particle;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        Vec3 spot = Vec3.atBottomCenterOf(SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range)));
        AreaEffectCloud cloud = new AreaEffectCloud(context.level(), spot.x, spot.y, spot.z);
        cloud.setOwner(context.caster());
        cloud.setRadius((float) (radius * Math.sqrt(context.power())));
        cloud.setDuration((int) Math.round(durationTicks * context.power()));
        cloud.setRadiusPerTick(0.0F);
        cloud.setWaitTime(0);
        cloud.setParticle(particle);
        afflictions.forEach(affliction -> cloud.addEffect(
                new MobEffectInstance(affliction.effect(), affliction.durationTicks(), affliction.amplifier())));
        context.level().addFreshEntity(cloud);
        context.level().playSound(null, BlockPos.containing(spot), sound, SoundSource.PLAYERS, 1.0F, 0.7F);
        return true;
    }
}
