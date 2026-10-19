package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Icicle Field: everything in the area freezes. Soaked targets freeze twice as hard and their water turns to ice
 * (wiki: more effective against wet targets). Uses Minecraft's own freezing (frost overlay, slowed movement).
 * As in the anime, billowing mounds of frost (Blockbench models) burst up over the field and swallow those caught.
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
        RandomSource random = context.caster().getRandom();
        int lifetime = freezeTicks + 20;
        for (int i = 0; i < Math.round(reach * 1.5); i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(random.nextDouble()) * reach;
            Vec3 spot = new Vec3(center.x + Math.cos(angle) * distance, center.y, center.z + Math.sin(angle) * distance);
            raiseMound(level, spot, (float) ((1.3 + random.nextDouble() * 0.8) * context.scale()), lifetime, random);
        }
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
            // the frost swallows whoever it catches
            raiseMound(level, target.position(), (float) Math.max(2.0, target.getBbHeight() * 1.1), lifetime, random);
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.3, center.z, (int) (reach * reach * 10),
                reach * 0.5, 0.2, reach * 0.5, 0.01);
        level.playSound(null, BlockPos.containing(center), SoundEvents.POWDER_SNOW_PLACE, SoundSource.PLAYERS, 1.5F, 0.7F);
        return true;
    }

    private static void raiseMound(ServerLevel level, Vec3 spot, float height, int lifetime, RandomSource random) {
        level.addFreshEntity(SpellPillar.create(level, PillarKind.FROST_MOUND, spot, height, lifetime, random.nextFloat() * 360.0F, 0.0F));
    }
}
