package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

    public AreaBurstEffect(double radius, float damage, int igniteSeconds, double range, ParticleOptions particle, SoundEvent sound) {
        this.radius = radius;
        this.damage = damage;
        this.igniteSeconds = igniteSeconds;
        this.range = range;
        this.particle = particle;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        Vec3 center = Vec3.atBottomCenterOf(SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range)));
        double reach = radius * context.scale();
        AABB area = new AABB(center, center).inflate(reach, 3.0, reach);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != context.caster() && entity.distanceToSqr(center) <= reach * reach + 9.0)) {
            target.hurt(MushoDamageTypes.spell(level, context.caster(), context.caster()), (float) (damage * context.power()));
            if (igniteSeconds > 0) {
                target.igniteForSeconds(igniteSeconds);
            }
        }
        int count = (int) Math.round(reach * reach * 6);
        level.sendParticles(particle, center.x, center.y + 0.6, center.z, count, reach * 0.5, 0.4, reach * 0.5, 0.02);
        level.playSound(null, BlockPos.containing(center), sound, SoundSource.PLAYERS, 2.0F, 0.9F);
        return true;
    }
}
