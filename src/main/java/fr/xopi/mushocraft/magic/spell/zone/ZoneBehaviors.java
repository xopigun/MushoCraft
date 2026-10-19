package fr.xopi.mushocraft.magic.spell.zone;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
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

    /** Tornado: a whirlwind drawing creatures in and lifting them, battering those caught inside. */
    public static SpellZone.Behavior vortex(double pull, double lift, float damagePerSecond) {
        return (zone, level, age) -> {
            for (int i = 0; i < 4; i++) {
                double angle = age * 0.4 + i * Math.PI / 2.0;
                double height = (age % 20) / 20.0 * 8.0;
                double ring = 0.5 + height * 0.25;
                level.sendParticles(ParticleTypes.CLOUD, zone.getX() + Math.cos(angle) * ring, zone.getY() + height,
                        zone.getZ() + Math.sin(angle) * ring, 1, 0.0, 0.0, 0.0, 0.0);
            }
            for (LivingEntity target : zone.creaturesInside(8.0)) {
                Vec3 toCenter = new Vec3(zone.getX() - target.getX(), 0.0, zone.getZ() - target.getZ());
                Vec3 swirl = new Vec3(-toCenter.z, 0.0, toCenter.x).normalize().scale(0.3);
                Vec3 inward = toCenter.lengthSqr() < 1.0E-4 ? Vec3.ZERO : toCenter.normalize().scale(pull);
                target.push(inward.x + swirl.x, lift, inward.z + swirl.z);
                target.hurtMarked = true;
                if (age % 20 == 0) {
                    target.hurt(MushoDamageTypes.spell(level, zone, zone.caster()), (float) (damagePerSecond * zone.power()));
                }
            }
            if (age % 20 == 0) {
                level.playSound(null, zone.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.6F, 0.6F);
            }
        };
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
