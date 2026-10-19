package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.fx.CameraShake;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Something that spreads over a whole area in an instant, centred where the caster aims (Flash Over: flames over a
 * very wide area). Every creature inside is hurt, and set alight if the spell burns. Blocks are spared (E14).
 */
public final class AreaBurstEffect implements SpellEffect {
    private final double radius;
    private final float damage;
    private final int igniteSeconds;
    private final double range;
    private final ParticleOptions particle;
    private final SoundEvent sound;
    private final List<CloudEffect.Affliction> afflictions;

    public AreaBurstEffect(double radius, float damage, int igniteSeconds, double range, ParticleOptions particle, SoundEvent sound) {
        this(radius, damage, igniteSeconds, range, particle, sound, List.of());
    }

    /**
     * @param afflictions conditions left on everyone caught (Water Splash: soaked)
     */
    public AreaBurstEffect(double radius, float damage, int igniteSeconds, double range, ParticleOptions particle, SoundEvent sound,
                           List<CloudEffect.Affliction> afflictions) {
        this.radius = radius;
        this.damage = damage;
        this.igniteSeconds = igniteSeconds;
        this.range = range;
        this.particle = particle;
        this.sound = sound;
        this.afflictions = List.copyOf(afflictions);
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        Vec3 center = Vec3.atBottomCenterOf(SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range)));
        double reach = radius * context.scale();
        AABB area = new AABB(center, center).inflate(reach, 3.0, reach);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != context.caster() && entity.distanceToSqr(center) <= reach * reach + 9.0)) {
            if (damage > 0.0F) {
                target.hurt(MushoDamageTypes.spell(level, context.caster(), context.caster()), (float) (damage * context.power()));
            }
            afflictions.forEach(affliction -> target.addEffect(
                    new MobEffectInstance(affliction.effect(), affliction.durationTicks(), affliction.amplifier()), context.caster()));
            if (igniteSeconds > 0) {
                target.igniteForSeconds(igniteSeconds);
            }
        }
        int count = (int) Math.round(reach * reach * 6);
        level.sendParticles(particle, center.x, center.y + 0.6, center.z, count, reach * 0.5, 0.4, reach * 0.5, 0.02);
        level.playSound(null, BlockPos.containing(center), sound, SoundSource.PLAYERS, 2.0F, 0.9F);
        if (damage >= 10.0F) {
            // only the violent ones (Flash Over) shake the ground
            CameraShake.around(level, center, (float) (0.4 + reach * 0.1), 16, reach * 3.0);
        }
        return true;
    }
}
