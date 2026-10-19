package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Icicle Field: everything in the area freezes. Soaked targets freeze twice as hard and their water turns to ice
 * (wiki: more effective against wet targets). Uses Minecraft's own freezing (frost overlay, slowed movement).
 */
public final class FreezeFieldEffect implements SpellEffect {
    private static final int SOAKED_FACTOR = 2;

    private final double radius;
    private final float damage;
    private final int freezeTicks;
    private final double range;

    public FreezeFieldEffect(double radius, float damage, int freezeTicks, double range) {
        this.radius = radius;
        this.damage = damage;
        this.freezeTicks = freezeTicks;
        this.range = range;
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        Vec3 center = Vec3.atBottomCenterOf(SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range)));
        double reach = radius * context.scale();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(reach, 2.0, reach),
                entity -> entity != context.caster() && entity.distanceToSqr(center) <= reach * reach + 4.0)) {
            boolean soaked = target.hasEffect(MushoMobEffects.SOAKED);
            int factor = soaked ? SOAKED_FACTOR : 1;
            target.hurt(MushoDamageTypes.spell(level, context.caster(), context.caster()), (float) (damage * factor * context.power()));
            target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + freezeTicks * factor));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, freezeTicks * factor, soaked ? 4 : 2));
            if (soaked) {
                target.removeEffect(MushoMobEffects.SOAKED);
            }
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.3, center.z, (int) (reach * reach * 10),
                reach * 0.5, 0.2, reach * 0.5, 0.01);
        level.playSound(null, BlockPos.containing(center), SoundEvents.POWDER_SNOW_PLACE, SoundSource.PLAYERS, 1.5F, 0.7F);
        return true;
    }
}
