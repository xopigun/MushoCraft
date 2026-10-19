package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.fx.SpellFx;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Icicle Field: everything in the area freezes. Soaked targets freeze twice as hard and their water turns to ice
 * (wiki: more effective against wet targets). Uses Minecraft's own freezing (frost overlay, slowed movement).
 * As in the anime, billowing mounds of frost (Blockbench models) burst up over the field and swallow those caught.
 * A field can be centred on the caster rather than on the aimed spot, and can freeze the surface of the water in it
 * (frosted ice, which thaws back by itself, as under Frost Walker).
 */
public final class FreezeFieldEffect implements SpellEffect {
    private static final int SOAKED_FACTOR = 2;

    private final double radius;
    private final float damage;
    private final int freezeTicks;
    private final double range;
    private final boolean aroundCaster;
    private final boolean freezesWater;

    public FreezeFieldEffect(double radius, float damage, int freezeTicks, double range) {
        this(radius, damage, freezeTicks, range, false, false);
    }

    private FreezeFieldEffect(double radius, float damage, int freezeTicks, double range, boolean aroundCaster, boolean freezesWater) {
        this.radius = radius;
        this.damage = damage;
        this.freezeTicks = freezeTicks;
        this.range = range;
        this.aroundCaster = aroundCaster;
        this.freezesWater = freezesWater;
    }

    /** The field spreads all around the caster, who is spared, instead of at the aimed spot. */
    public FreezeFieldEffect aroundCaster() {
        return new FreezeFieldEffect(radius, damage, freezeTicks, range, true, freezesWater);
    }

    /** The surface of the water in the field freezes over. */
    public FreezeFieldEffect freezingWater() {
        return new FreezeFieldEffect(radius, damage, freezeTicks, range, aroundCaster, true);
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        Vec3 center = aroundCaster ? context.caster().position()
                : Vec3.atBottomCenterOf(SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range)));
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
        if (freezesWater) {
            freezeWater(level, BlockPos.containing(center), (int) Math.ceil(reach));
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.3, center.z, (int) (reach * reach * 10),
                reach * 0.5, 0.2, reach * 0.5, 0.01);
        SpellFx.groundRing(level, context.caster(), center, reach, 8, ParticleTypes.SNOWFLAKE);
        level.playSound(null, BlockPos.containing(center), SoundEvents.POWDER_SNOW_PLACE, SoundSource.PLAYERS, 1.5F, 0.7F);
        return true;
    }

    /** Every still water surface within the radius, a few blocks above or below the centre, turns to frosted ice. */
    private static void freezeWater(ServerLevel level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                for (int dy = 3; dy >= -4; dy--) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (level.getFluidState(pos).is(Fluids.WATER) && level.getFluidState(pos).isSource()
                            && level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(pos.above()).isAir()) {
                        level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                        // the ice starts thawing at once, as Frost Walker's does
                        level.scheduleTick(pos, Blocks.FROSTED_ICE, Mth.nextInt(level.getRandom(), 60, 120));
                        break;
                    }
                }
            }
        }
    }

    private static void raiseMound(ServerLevel level, Vec3 spot, float height, int lifetime, RandomSource random) {
        level.addFreshEntity(SpellPillar.create(level, PillarKind.FROST_MOUND, spot, height, lifetime, random.nextFloat() * 360.0F, 0.0F));
    }
}
