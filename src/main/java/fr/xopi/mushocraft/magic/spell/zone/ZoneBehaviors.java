package fr.xopi.mushocraft.magic.spell.zone;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.fx.CameraShake;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.Cone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
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
            CameraShake.around(level, strike, 0.7F, 10, 24.0);
            for (int i = 0; i < 4; i++) {
                level.sendParticles(MushoParticles.ARC.get(), strike.x, strike.y + 0.3, strike.z, 0, 1.6, 0.0, 0.0, 1.0);
            }
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
                // the ground trembles under the funnel
                CameraShake.around(level, zone.position(), 0.35F, 22, zone.radius() * 3.0);
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
                level.sendParticles(MushoParticles.FLAME.get(), zone.getX(), zone.getY() + 0.2, zone.getZ(), 12,
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
                    level.sendParticles(MushoParticles.FLAME.get(), zone.getX() + Math.cos(angle) * zone.radius(), zone.getY() + 0.3,
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

    /**
     * Flamethrower: a jet of flame pours from the caster for as long as the zone lasts, wherever they look (wiki, Fire;
     * LN vol. 7 ch. 3). Whoever stands in the jet burns; a blow lands twice a second (a creature hurt cannot be hurt
     * again for half a second).
     */
    public static SpellZone.Behavior flameJet(double range, double halfAngleDegrees, float damage, int igniteSeconds) {
        return (zone, level, age) -> {
            LivingEntity caster = zone.caster();
            if (caster == null || !caster.isAlive()) {
                zone.discard();
                return;
            }
            zone.setPos(caster.position());
            Cone cone = Cone.of(caster.getEyePosition(), caster.getLookAngle(), range, halfAngleDegrees);
            SpellFx.jet(level, cone, MushoParticles.FLAME.get(), 14, 1.0);
            if (age % 3 == 0) {
                SpellFx.jet(level, cone, MushoParticles.EMBER.get(), 3, 1.0);
            }
            // a zone's first tick is its age 1
            if (age % 10 == 1) {
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(range),
                        entity -> entity != caster && entity.isAlive() && cone.contains(entity.getBoundingBox().getCenter())
                                && caster.hasLineOfSight(entity))) {
                    target.hurt(MushoDamageTypes.spell(level, zone, caster), (float) (damage * zone.power()));
                    target.igniteForSeconds(igniteSeconds);
                }
            }
            if (age % 6 == 0) {
                level.playSound(null, caster.blockPosition(), SoundEvents.BLAZE_BURN, SoundSource.PLAYERS, 0.8F, 0.8F);
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

    // ---- Mixed magic ----

    /**
     * Deep Mist: a thick fog that hides those inside (wiki: conceals the user from enemies). Creatures in it cannot see;
     * whatever hunts someone hidden in it, from inside or outside, loses track of its prey unless it is right next to
     * it.
     */
    public static SpellZone.Behavior deepMist() {
        return (zone, level, age) -> {
            double radius = zone.radius();
            if (age % 2 == 0) {
                level.sendParticles(ParticleTypes.CLOUD, zone.getX(), zone.getY() + 1.2, zone.getZ(), (int) (radius * 2),
                        radius * 0.6, 0.8, radius * 0.6, 0.0);
            }
            if (age % 10 != 0) {
                return;
            }
            for (LivingEntity inside : zone.creaturesInside(4.0)) {
                inside.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false));
            }
            AABB around = new AABB(zone.position(), zone.position()).inflate(radius + 16.0, 8.0, radius + 16.0);
            for (Mob hunter : level.getEntitiesOfClass(Mob.class, around, mob -> mob.getTarget() != null)) {
                LivingEntity prey = hunter.getTarget();
                boolean hidden = prey.distanceToSqr(zone.getX(), prey.getY(), zone.getZ()) <= radius * radius;
                if (hidden && hunter.distanceToSqr(prey) > 9.0) {
                    hunter.setTarget(null);
                }
            }
        };
    }

    /** Quagmire: the ground turns into deep mud that holds fast whoever stands in it (wiki: immobilizes the enemy). */
    public static SpellZone.Behavior quagmire() {
        BlockParticleOption mud = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState());
        return (zone, level, age) -> {
            if (age % 4 == 0) {
                level.sendParticles(mud, zone.getX(), zone.getY() + 0.1, zone.getZ(), (int) (zone.radius() * 3),
                        zone.radius() * 0.5, 0.05, zone.radius() * 0.5, 0.0);
            }
            for (LivingEntity stuck : zone.creaturesInside(1.5)) {
                stuck.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 5, false, false));
                Vec3 motion = stuck.getDeltaMovement();
                // the mud drags at every step and keeps the feet down
                stuck.setDeltaMovement(motion.x * 0.3, Math.min(motion.y, 0.0), motion.z * 0.3);
                stuck.hurtMarked = true;
            }
            if (age % 20 == 0) {
                level.playSound(null, zone.blockPosition(), SoundEvents.MUD_STEP, SoundSource.PLAYERS, 1.0F, 0.6F);
            }
        };
    }

    /**
     * Mud Rain: a rain of mud that leaves the ground slippery and loose (wiki). No one inside can run, all are slowed,
     * and now and then someone slips.
     */
    public static SpellZone.Behavior mudRain() {
        BlockParticleOption mud = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.MUD.defaultBlockState());
        return (zone, level, age) -> {
            level.sendParticles(mud, zone.getX(), zone.getY() + 7.0, zone.getZ(), (int) (zone.radius() * 2),
                    zone.radius() * 0.6, 1.0, zone.radius() * 0.6, 0.0);
            if (age % 10 != 0) {
                return;
            }
            RandomSource random = level.getRandom();
            for (LivingEntity target : zone.creaturesInside(7.0)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false));
                target.setSprinting(false);
                if (target.onGround() && random.nextFloat() < 0.08F) {
                    // a slip: the feet go from under them
                    double angle = random.nextDouble() * Math.PI * 2.0;
                    target.push(Math.cos(angle) * 0.4, 0.05, Math.sin(angle) * 0.4);
                    target.hurtMarked = true;
                }
            }
        };
    }

    /** Magma Gush: the ground stays molten for a while and burns whoever stands on it. */
    public static SpellZone.Behavior moltenGround(float damagePerSecond, int igniteSeconds) {
        return (zone, level, age) -> {
            if (age % 3 == 0) {
                level.sendParticles(ParticleTypes.LAVA, zone.getX(), zone.getY() + 0.2, zone.getZ(), 2,
                        zone.radius() * 0.5, 0.0, zone.radius() * 0.5, 0.0);
                level.sendParticles(ParticleTypes.SMOKE, zone.getX(), zone.getY() + 0.2, zone.getZ(), 3,
                        zone.radius() * 0.5, 0.0, zone.radius() * 0.5, 0.01);
            }
            for (LivingEntity target : zone.creaturesInside(1.0)) {
                if (!target.onGround() || target.fireImmune()) {
                    continue;
                }
                target.igniteForSeconds(igniteSeconds);
                if (age % 20 == 0) {
                    target.hurt(level.damageSources().hotFloor(), (float) (damagePerSecond * zone.power()));
                }
            }
            if (age % 30 == 0) {
                level.playSound(null, zone.blockPosition(), SoundEvents.LAVA_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        };
    }

    // ---- King rank ----

    /**
     * Lightning: a thundercloud gathers over the target and condenses all its energy into a single bolt (wiki, Magic
     * Spells; LN vol. 13 ch. 6). The cloud follows the target while it gathers; then the bolt falls and the zone ends.
     * The bolt is visual only: its damage is dealt here.
     */
    public static SpellZone.Behavior lightning(LivingEntity target, int gatherTicks, float damage) {
        return (zone, level, age) -> {
            if (target != null && target.isAlive() && age < gatherTicks) {
                zone.setPos(target.position());
            }
            if (age < gatherTicks) {
                double spread = 4.0 * (1.0 - (double) age / gatherTicks) + 0.8;
                level.sendParticles(ParticleTypes.CLOUD, zone.getX(), zone.getY() + 9.0, zone.getZ(), 12, spread, 0.4, spread, 0.0);
                if (age % 3 == 0) {
                    // the gathered energy crackles inside the cloud
                    RandomSource random = level.getRandom();
                    level.sendParticles(MushoParticles.ARC.get(), zone.getX() + (random.nextDouble() - 0.5) * spread * 2.0,
                            zone.getY() + 8.5, zone.getZ() + (random.nextDouble() - 0.5) * spread * 2.0, 0, 1.6, 0.0, 0.0, 1.0);
                }
                if (age % 5 == 0) {
                    level.playSound(null, zone.blockPosition().above(8), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
                            0.6F, 1.6F);
                }
                return;
            }
            if (age == gatherTicks) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(zone.position());
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
                for (LivingEntity struck : zone.creaturesInside(4.0)) {
                    struck.hurt(MushoDamageTypes.spell(level, zone, zone.caster()), (float) (damage * zone.power()));
                    struck.igniteForSeconds(4);
                    level.sendParticles(MushoParticles.ARC.get(), struck.getX(), struck.getY() + struck.getBbHeight() * 0.5,
                            struck.getZ(), 0, 1.2, 0.0, 0.0, 1.0);
                }
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4.0;
                    level.sendParticles(MushoParticles.ARC.get(), zone.getX() + Math.cos(angle) * 1.2, zone.getY() + 0.3,
                            zone.getZ() + Math.sin(angle) * 1.2, 0, 2.2, 0.0, 0.0, 1.0);
                }
                level.playSound(null, zone.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 3.0F, 0.8F);
                CameraShake.around(level, zone.position(), 2.2F, 20, 40.0);
                zone.discard();
            }
        };
    }

    /**
     * Electric: Lightning compressed into a small cloud low over the target, for a weaker discharge that still shocks
     * and paralyses everyone near it (wiki, Magic Spells; LN vol. 13 ch. 6). Electricity strikes the nerves directly,
     * so it goes through Battle Aura ({@link MushoDamageTypes#ELECTRIC}). Then the zone ends.
     */
    public static SpellZone.Behavior electric(LivingEntity target, int gatherTicks, float damage, int paralysisTicks) {
        return (zone, level, age) -> {
            if (target != null && target.isAlive() && age < gatherTicks) {
                zone.setPos(target.position());
            }
            RandomSource random = level.getRandom();
            if (age < gatherTicks) {
                if (age % 2 == 0) {
                    level.sendParticles(MushoParticles.ARC.get(), zone.getX() + (random.nextDouble() - 0.5) * 1.6, zone.getY() + 3.6,
                            zone.getZ() + (random.nextDouble() - 0.5) * 1.6, 0, 0.9, 0.0, 0.0, 1.0);
                }
                if (age % 4 == 0) {
                    level.playSound(null, zone.blockPosition().above(3), SoundEvents.BEEHIVE_WORK, SoundSource.PLAYERS, 0.8F, 1.8F);
                }
                return;
            }
            if (age == gatherTicks) {
                // the discharge: three crackling strands from the cloud down to the ground, a flash, a ring along the ground
                for (int strand = 0; strand < 3; strand++) {
                    double offsetX = (random.nextDouble() - 0.5) * zone.radius();
                    double offsetZ = (random.nextDouble() - 0.5) * zone.radius();
                    for (double y = 3.2; y > 0.0; y -= 0.9) {
                        level.sendParticles(MushoParticles.ARC.get(), zone.getX() + offsetX * y / 3.2, zone.getY() + y,
                                zone.getZ() + offsetZ * y / 3.2, 0, 2.0, 0.0, 0.0, 1.0);
                    }
                }
                level.sendParticles(MushoParticles.FLASH.get(), zone.getX(), zone.getY() + 1.0, zone.getZ(), 0, 1.0, 0.0, 0.0, 1.0);
                level.sendParticles(MushoParticles.SHOCK_RING.get(), zone.getX(), zone.getY() + 0.1, zone.getZ(), 0, zone.radius() * 1.4,
                        0.0, 0.0, 1.0);
                for (LivingEntity struck : zone.creaturesInside(3.0)) {
                    struck.hurt(MushoDamageTypes.electric(level, zone, zone.caster()), (float) (damage * zone.power()));
                    struck.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, paralysisTicks, 9), zone.caster());
                    struck.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, paralysisTicks, 3), zone.caster());
                    struck.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralysisTicks, 1), zone.caster());
                    for (int i = 0; i < 3; i++) {
                        level.sendParticles(MushoParticles.ARC.get(), struck.getX() + (random.nextDouble() - 0.5) * struck.getBbWidth(),
                                struck.getY() + random.nextDouble() * struck.getBbHeight(),
                                struck.getZ() + (random.nextDouble() - 0.5) * struck.getBbWidth(), 0, 0.8, 0.0, 0.0, 1.0);
                    }
                }
                level.playSound(null, zone.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.2F, 1.6F);
                CameraShake.around(level, zone.position(), 0.8F, 8, 16.0);
                zone.discard();
            }
        };
    }

    /**
     * Confinement Barrier: the target is shut inside a barrier it cannot leave, where magic cannot be used (wiki,
     * Magic Spells; LN vol. 6 ch. 4). The zone holds it until the spell ends.
     */
    public static SpellZone.Behavior confinement(LivingEntity prisoner) {
        return (zone, level, age) -> {
            if (prisoner == null || !prisoner.isAlive() || prisoner.level() != level) {
                zone.discard();
                return;
            }
            prisoner.addEffect(new MobEffectInstance(MushoMobEffects.CONFINED, 10, 0, false, true));
            Vec3 centre = zone.position();
            Vec3 offset = prisoner.position().subtract(centre);
            double limit = Math.max(0.2, zone.radius() - prisoner.getBbWidth() / 2.0);
            if (offset.horizontalDistance() > limit || Math.abs(offset.y) > zone.radius() + 1.0) {
                // the barrier throws it back inside
                Vec3 flat = new Vec3(offset.x, 0.0, offset.z);
                Vec3 inside = flat.lengthSqr() < 1.0E-6 ? centre : centre.add(flat.normalize().scale(limit * 0.8));
                prisoner.teleportTo(inside.x, centre.y, inside.z);
                prisoner.setDeltaMovement(Vec3.ZERO);
                prisoner.hurtMarked = true;
            }
            if (age % 4 == 0) {
                RandomSource random = level.getRandom();
                for (int i = 0; i < 6; i++) {
                    double angle = random.nextDouble() * Math.PI * 2.0;
                    double height = random.nextDouble() * (prisoner.getBbHeight() + 0.6);
                    level.sendParticles(ParticleTypes.END_ROD, centre.x + Math.cos(angle) * zone.radius(), centre.y + height,
                            centre.z + Math.sin(angle) * zone.radius(), 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        };
    }

    // ---- Effects only ----

    /** A ward around its caster: it keeps with the caster, and is gone as soon as the status it shows wears off. */
    public static SpellZone.Behavior ward(Holder<MobEffect> status) {
        return (zone, level, age) -> {
            LivingEntity caster = zone.caster();
            if (caster == null || !caster.isAlive() || !caster.hasEffect(status)) {
                zone.discard();
                return;
            }
            zone.setPos(caster.position());
        };
    }

    /**
     * A ring racing out over the ground from the zone's centre, hugging the lie of the land (technique of the visual
     * reference, docs/VISUELS_REFERENCE.md): fire spreading, frost creeping, dust thrown up. Does nothing else.
     */
    public static SpellZone.Behavior groundRing(ParticleOptions particle, int spreadTicks) {
        return (zone, level, age) -> {
            if (age > spreadTicks) {
                zone.discard();
                return;
            }
            double radius = zone.radius() * age / spreadTicks;
            int points = Math.max(8, (int) (radius * 7.0));
            for (int i = 0; i < points; i++) {
                double angle = i * Math.PI * 2.0 / points + age * 0.1;
                double x = zone.getX() + Math.cos(angle) * radius;
                double z = zone.getZ() + Math.sin(angle) * radius;
                double ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(x, 0, z).getX(),
                        BlockPos.containing(x, 0, z).getZ());
                // only follow the ground near the centre's height: no ring on a cliff top far above
                double y = Math.abs(ground - zone.getY()) <= 3.0 ? ground : zone.getY();
                level.sendParticles(particle, x, y + 0.15, z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        };
    }
}
