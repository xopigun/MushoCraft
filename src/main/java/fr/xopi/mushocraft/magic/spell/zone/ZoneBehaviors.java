package fr.xopi.mushocraft.magic.spell.zone;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The behaviours of lasting spells (see {@link SpellZone}). None of them breaks or burns blocks (docs/ECARTS.md, E14).
 */
public final class ZoneBehaviors {
    private ZoneBehaviors() {
    }

    /**
     * Cumulonimbus: a thundercloud over the area; rain falls and lightning strikes at random inside it.
     * The bolts are visual only: their damage is dealt here, so they set nothing on fire.
     */
    public static SpellZone.Behavior thunderstorm(int strikeInterval, float strikeDamage) {
        return (zone, level, age) -> {
            if (age == 1 && level.dimensionType().hasSkyLight() && !level.getLevelData().isRaining()) {
                level.setWeatherParameters(0, 1200, true, true);
            }
            if (age % 10 == 0) {
                level.sendParticles(ParticleTypes.CLOUD, zone.getX(), zone.getY() + 12.0, zone.getZ(), 30,
                        zone.radius() * 0.6, 0.5, zone.radius() * 0.6, 0.01);
                level.sendParticles(ParticleTypes.FALLING_WATER, zone.getX(), zone.getY() + 10.0, zone.getZ(), 40,
                        zone.radius() * 0.6, 2.0, zone.radius() * 0.6, 0.0);
            }
            if (age % strikeInterval != 0) {
                return;
            }
            RandomSource random = level.getRandom();
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(random.nextDouble()) * zone.radius();
            // One bolt in two seeks a creature inside the storm; the others fall anywhere on the ground beneath it.
            Vec3 strike = zone.creaturesInside(4.0).stream().findAny().map(LivingEntity::position)
                    .filter(target -> random.nextBoolean())
                    .orElseGet(() -> Vec3.atBottomCenterOf(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(
                            zone.getX() + Math.cos(angle) * distance, zone.getY(), zone.getZ() + Math.sin(angle) * distance))));
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) {
                return;
            }
            bolt.moveTo(strike);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bolt.getBoundingBox().inflate(2.5, 4.0, 2.5),
                    entity -> entity != zone.caster())) {
                target.hurt(MushoDamageTypes.spell(level, zone, zone.caster()), (float) (strikeDamage * zone.power()));
            }
        };
    }

    /**
     * Tornado: a towering whirlwind. Creatures and loose objects inside are drawn towards its heart, whirled round and
     * carried up the funnel; whatever reaches the top is flung outwards. Those caught are battered every second.
     */
    public static SpellZone.Behavior vortex(double pull, double lift, float damagePerSecond) {
        return (zone, level, age) -> {
            double height = zone.radius() * 2.6;
            AABB box = new AABB(zone.getX() - zone.radius() * 1.3, zone.getY() - 1.0, zone.getZ() - zone.radius() * 1.3,
                    zone.getX() + zone.radius() * 1.3, zone.getY() + height, zone.getZ() + zone.radius() * 1.3);
            for (Entity target : level.getEntities(zone, box, entity -> entity != zone.caster() && entity.isAlive()
                    && !(entity instanceof SpellZone) && !entity.isSpectator() && (entity instanceof LivingEntity || entity instanceof ItemEntity))) {
                double up = (target.getY() - zone.getY()) / height;
                // the funnel widens with height: so does the region it holds
                double reach = zone.radius() * (0.4 + up);
                Vec3 toCenter = new Vec3(zone.getX() - target.getX(), 0.0, zone.getZ() - target.getZ());
                double distance = toCenter.length();
                if (distance > reach * 1.3) {
                    continue;
                }
                Vec3 inward = distance < 1.0E-4 ? Vec3.ZERO : toCenter.normalize();
                Vec3 swirl = new Vec3(-inward.z, 0.0, inward.x).scale(0.45);
                if (up > 0.85) {
                    // flung out of the top of the funnel
                    target.setDeltaMovement(target.getDeltaMovement().add(inward.scale(-1.2).add(0.0, 0.3, 0.0)));
                } else {
                    target.setDeltaMovement(target.getDeltaMovement().scale(0.85)
                            .add(inward.scale(pull * Math.min(1.0, distance)).add(swirl).add(0.0, lift, 0.0)));
                }
                target.hurtMarked = true;
                target.fallDistance = 0.0F;
                if (age % 20 == 0 && target instanceof LivingEntity living) {
                    living.hurt(MushoDamageTypes.spell(level, zone, zone.caster()), (float) (damagePerSecond * zone.power()));
                }
            }
            if (age % 4 == 0) {
                double angle = age * 0.5;
                double radius = zone.radius() * 0.3;
                level.sendParticles(ParticleTypes.POOF, zone.getX() + Math.cos(angle) * radius, zone.getY() + 0.3,
                        zone.getZ() + Math.sin(angle) * radius, 6, 0.6, 0.1, 0.6, 0.05);
            }
            if (age % 20 == 0) {
                level.playSound(null, zone.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.5F, 0.5F);
            }
        };
    }

    /** Waterfall: water pours down without end, pressing creatures down and away, soaking them. */
    public static SpellZone.Behavior waterfall(double push) {
        return (zone, level, age) -> {
            level.sendParticles(ParticleTypes.FALLING_WATER, zone.getX(), zone.getY() + 6.0, zone.getZ(), 30,
                    zone.radius() * 0.5, 1.5, zone.radius() * 0.5, 0.0);
            if (age % 4 == 0) {
                level.sendParticles(ParticleTypes.SPLASH, zone.getX(), zone.getY() + 0.2, zone.getZ(), 20,
                        zone.radius() * 0.5, 0.0, zone.radius() * 0.5, 0.1);
            }
            for (LivingEntity target : zone.creaturesInside(6.0)) {
                Vec3 away = new Vec3(target.getX() - zone.getX(), 0.0, target.getZ() - zone.getZ());
                Vec3 out = away.lengthSqr() < 1.0E-4 ? Vec3.ZERO : away.normalize().scale(push);
                target.push(out.x, -push, out.z);
                target.hurtMarked = true;
                soak(target, zone);
            }
            if (age % 20 == 0) {
                level.playSound(null, zone.blockPosition(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        };
    }

    /** Flood Flush: the ground floods; creatures wade, slowed and soaked. */
    public static SpellZone.Behavior flood() {
        return (zone, level, age) -> {
            if (age % 3 == 0) {
                level.sendParticles(ParticleTypes.SPLASH, zone.getX(), zone.getY() + 0.1, zone.getZ(), 40,
                        zone.radius() * 0.6, 0.0, zone.radius() * 0.6, 0.2);
                level.sendParticles(ParticleTypes.BUBBLE_POP, zone.getX(), zone.getY() + 0.3, zone.getZ(), 15,
                        zone.radius() * 0.6, 0.1, zone.radius() * 0.6, 0.0);
            }
            for (LivingEntity target : zone.creaturesInside(2.0)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false));
                soak(target, zone);
            }
        };
    }

    /** Heat Island: the surroundings grow hot; the frozen thaw and the soaked dry, foes slowly scorch. */
    public static SpellZone.Behavior heatIsland(float damagePerSecond) {
        return (zone, level, age) -> {
            if (age % 5 == 0) {
                level.sendParticles(ParticleTypes.SMALL_FLAME, zone.getX(), zone.getY() + 0.2, zone.getZ(), 12,
                        zone.radius() * 0.6, 0.1, zone.radius() * 0.6, 0.01);
            }
            LivingEntity caster = zone.caster();
            if (caster != null) {
                caster.setTicksFrozen(0);
                caster.removeEffect(MushoMobEffects.SOAKED);
            }
            for (LivingEntity target : zone.creaturesInside(3.0)) {
                target.removeEffect(MushoMobEffects.SOAKED);
                if (age % 20 == 0) {
                    target.hurt(level.damageSources().hotFloor(), (float) (damagePerSecond * zone.power()));
                }
            }
        };
    }

    /** Burning Place: a wreath of fire around the caster engulfs whoever comes close. */
    public static SpellZone.Behavior burningRing(float damagePerSecond, int igniteSeconds) {
        return (zone, level, age) -> {
            LivingEntity caster = zone.caster();
            if (caster != null) {
                zone.setPos(caster.position());
            }
            if (age % 3 == 0) {
                for (int i = 0; i < 16; i++) {
                    double angle = i * Math.PI / 8.0 + age * 0.05;
                    level.sendParticles(ParticleTypes.FLAME, zone.getX() + Math.cos(angle) * zone.radius(), zone.getY() + 0.3,
                            zone.getZ() + Math.sin(angle) * zone.radius(), 1, 0.0, 0.2, 0.0, 0.01);
                }
            }
            for (LivingEntity target : zone.creaturesInside(2.5)) {
                target.igniteForSeconds(igniteSeconds);
                if (age % 20 == 0) {
                    target.hurt(MushoDamageTypes.spell(level, zone, caster), (float) (damagePerSecond * zone.power()));
                }
            }
        };
    }

    private static void soak(LivingEntity target, SpellZone zone) {
        target.addEffect(new MobEffectInstance(MushoMobEffects.SOAKED, 200, 0), zone.caster());
    }

    /** Spirit-Sealing Barrier: spirits and the undead inside are held in place and slowly worn away. */
    public static SpellZone.Behavior spiritSeal(float damagePerSecond) {
        return (zone, level, age) -> {
            if (age % 5 == 0) {
                for (int i = 0; i < 24; i++) {
                    double angle = i * Math.PI * 2.0 / 24.0;
                    level.sendParticles(ParticleTypes.END_ROD, zone.getX() + Math.cos(angle) * zone.radius(), zone.getY() + 0.2,
                            zone.getZ() + Math.sin(angle) * zone.radius(), 1, 0.0, 0.3, 0.0, 0.0);
                }
            }
            for (LivingEntity target : zone.creaturesInside(4.0)) {
                if (!target.isInvertedHealAndHarm()) {
                    continue;
                }
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 9, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10, 2, false, false));
                if (age % 20 == 0) {
                    target.hurt(MushoDamageTypes.spell(level, zone, zone.caster()), (float) (damagePerSecond * zone.power()));
                }
            }
        };
    }
}
