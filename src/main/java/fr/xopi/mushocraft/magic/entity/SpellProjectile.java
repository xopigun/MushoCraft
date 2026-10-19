package fr.xopi.mushocraft.magic.entity;

import fr.xopi.mushocraft.magic.fx.SpellFx;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.core.OrientedParticleOptions;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileShape;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Matter conjured by an attack spell and thrown by the caster. One entity type for every element:
 * the element only changes the look and the impact.
 */
public class SpellProjectile extends ThrowableProjectile implements GeoEntity {
    /** Models made in Blockbench (crystal clusters) burst into shape, then tumble along. */
    private static final RawAnimation FORM_THEN_FLY = RawAnimation.begin()
            .thenPlay("animation.ice_smash.form").thenLoop("animation.ice_smash.fly");

    private static final EntityDataAccessor<Integer> ELEMENT =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SHAPE =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);
    private static final int MAX_LIFETIME_TICKS = 100;

    private ProjectileProfile profile = ProjectileProfile.of(SpellElement.WATER, 0.0F).build();
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    public static SpellProjectile create(Level level, LivingEntity caster, ProjectileProfile profile) {
        SpellProjectile projectile = new SpellProjectile(MushoEntities.SPELL_PROJECTILE.get(), level);
        projectile.setOwner(caster);
        projectile.setProfile(profile);
        return projectile;
    }

    private void setProfile(ProjectileProfile value) {
        this.profile = value;
        entityData.set(ELEMENT, value.element().ordinal());
        entityData.set(SIZE, value.size());
        entityData.set(SHAPE, value.shape().ordinal());
    }

    public ProjectileShape shape() {
        return ProjectileShape.values()[Math.floorMod(entityData.get(SHAPE), ProjectileShape.values().length)];
    }

    public SpellElement element() {
        return SpellElement.values()[Math.floorMod(entityData.get(ELEMENT), SpellElement.values().length)];
    }

    public float visualSize() {
        return entityData.get(SIZE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "shape", 0, state ->
                shape() == ProjectileShape.CRYSTAL_CLUSTER ? state.setAndContinue(FORM_THEN_FLY) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ELEMENT, 0);
        builder.define(SIZE, 0.5F);
        builder.define(SHAPE, 0);
    }

    @Override
    protected double getDefaultGravity() {
        return profile.gravity();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 motion = getDeltaMovement();
            level().addParticle(element().trail(), getX() - motion.x * 0.5, getY() - motion.y * 0.5,
                    getZ() - motion.z * 0.5, 0.0, 0.0, 0.0);
            if (element() == SpellElement.WIND && tickCount % 2 == 0 && motion.lengthSqr() > 0.01) {
                // wind drives rings of torn air before it
                level().addParticle(OrientedParticleOptions.facing(MushoParticles.SHOCK_WAVE.get(), motion,
                                random.nextFloat() * 360.0F, visualSize() * 1.8F, 0xE8FFF4),
                        getX(), getY() + getBbHeight() / 2.0, getZ(), 0.0, 0.0, 0.0);
            }
            if (element() == SpellElement.EARTH && tickCount % 2 == 0) {
                // a hurled stone sheds grit
                level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.COARSE_DIRT.defaultBlockState()),
                        getX() - motion.x * 0.5, getY() + getBbHeight() / 2.0 - motion.y * 0.5, getZ() - motion.z * 0.5, 0.0, 0.0, 0.0);
            }
            if (element() == SpellElement.FIRE) {
                // a fireball sheds sparks (its smoke would wall the view: the ribbon of light is its trail)
                float spread = visualSize() * 0.35F;
                level().addParticle(MushoParticles.EMBER.get(), getX() + (random.nextFloat() - 0.5F) * spread,
                        getY() + getBbHeight() / 2.0 + (random.nextFloat() - 0.5F) * spread,
                        getZ() + (random.nextFloat() - 0.5F) * spread, -motion.x * 0.2, -motion.y * 0.2, -motion.z * 0.2);
            }
            if (element() == SpellElement.WATER) {
                // conjured water sheds drops along its flight
                float spread = visualSize() * 0.4F;
                for (int i = 0; i < 2; i++) {
                    level().addParticle(ParticleTypes.FALLING_WATER,
                            getX() - motion.x * random.nextFloat() + (random.nextFloat() - 0.5F) * spread,
                            getY() + getBbHeight() / 2.0 - motion.y * random.nextFloat() + (random.nextFloat() - 0.5F) * spread,
                            getZ() - motion.z * random.nextFloat() + (random.nextFloat() - 0.5F) * spread, 0.0, 0.0, 0.0);
                }
            }
        } else if (tickCount > MAX_LIFETIME_TICKS) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        float damage = profile.damage();
        if (target.getType().is(EntityTypeTags.UNDEAD)) {
            damage *= profile.undeadMultiplier();
        }
        if (profile.element() == SpellElement.WATER) {
            target.clearFire();
        }
        if (!target.hurt(MushoDamageTypes.spell(level(), this, getOwner()), damage)) {
            return;
        }
        if (target instanceof LivingEntity living) {
            if (profile.knockback() > 0.0F) {
                Vec3 push = getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize();
                living.knockback(profile.knockback(), -push.x, -push.z);
            }
            if (profile.slowTicks() > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, profile.slowTicks(), 1), getOwner());
            }
            if (profile.bindTicks() > 0) {
                // Bound by the wind: no more walking, no more swinging.
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, profile.bindTicks(), 9), getOwner());
                living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, profile.bindTicks(), 3), getOwner());
            }
        }
        if (profile.igniteSeconds() > 0) {
            target.igniteForSeconds(profile.igniteSeconds());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            if (profile.explosionRadius() > 0.0F) {
                SpellBlasts.explode(server, this, MushoDamageTypes.spell(server, this, getOwner()), position(),
                        profile.explosionRadius());
            } else if (element() == SpellElement.FIRE) {
                server.addFreshEntity(SpellFire.blast(server, position(), Math.max(0.6F, visualSize() * 0.6F)));
            }
            if (profile.columnHeight() > 0.0F) {
                // Ex-Flame (anime S2 ep. 1): the great ball comes down and a column of fire roars up where it lands,
                // a ring of blast racing out over the ground around it
                Vec3 ground = position().subtract(0.0, Math.min(1.5, visualSize() * 0.5), 0.0);
                server.addFreshEntity(SpellFire.column(server, ground, visualSize() * 0.45F, profile.columnHeight(), 46));
                SpellFx.shockWave(server, ground.add(0.0, 0.2, 0.0), new Vec3(0.0, 1.0, 0.0), visualSize() * 1.6F, 0xFFC880);
            }
            if (profile.blindRadius() > 0.0F) {
                double radius = profile.blindRadius();
                server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius), entity -> entity != getOwner())
                        .forEach(entity -> entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0), getOwner()));
                server.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY(), getZ(), (int) (radius * radius * 20), radius * 0.6,
                        radius * 0.4, radius * 0.6, 0.02);
                // the sand billows up into a cloud where it burst, as in the anime (S2), and settles
                if (getOwner() instanceof LivingEntity owner) {
                    SpellZone cloud = SpellZone.create(server, owner, (zone, world, age) -> {
                    }, ZoneVisual.DUST_STORM, radius * 1.1, 1.0, 70);
                    cloud.setPos(position().subtract(0.0, 0.5, 0.0));
                    server.addFreshEntity(cloud);
                }
            }
            server.sendParticles(element().trail(), getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.05);
            // a drawn hit spark, tinted by the element; its size is a half-width, kept to the projectile's scale so a big
            // fireball does not throw rays across the whole view (its drawn explosion is the show)
            server.sendParticles(ColorParticleOption.create(MushoParticles.IMPACT.get(), 0xFF000000 | impactTint()),
                    getX(), getY() + getBbHeight() / 2.0, getZ(), 0, Mth.clamp(visualSize() * 0.6, 0.4, 1.2), 0.0, 0.0, 1.0);
            if (element() == SpellElement.WATER) {
                // the mass bursts into a spray, as in the anime
                double spread = visualSize();
                server.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 50, spread * 0.6, spread * 0.4, spread * 0.6, 0.4);
                server.sendParticles(ParticleTypes.FALLING_WATER, getX(), getY() + spread * 0.5, getZ(), 25, spread * 0.8,
                        spread * 0.5, spread * 0.8, 0.0);
                server.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY(), getZ(), 15, spread * 0.5, spread * 0.5, spread * 0.5, 0.1);
            }
            server.playSound(null, getX(), getY(), getZ(), impactSound(), SoundSource.PLAYERS, 0.7F, 1.2F);
            discard();
        }
    }

    /** The colour of the hit spark: a pale shade of the element. */
    private int impactTint() {
        return switch (element()) {
            case WATER -> 0xB8E4FF;
            case ICE -> 0xDDF2FF;
            case FIRE -> 0xFFC070;
            case EARTH -> 0xE8D2B0;
            case WIND -> 0xD8FFEE;
            case LIGHT -> 0xFFF2B0;
        };
    }

    private SoundEvent impactSound() {
        return switch (element()) {
            case WATER -> SoundEvents.GENERIC_SPLASH;
            case ICE -> SoundEvents.GLASS_BREAK;
            case FIRE -> SoundEvents.FIRE_EXTINGUISH;
            case EARTH -> SoundEvents.STONE_BREAK;
            case WIND -> SoundEvents.BREEZE_WIND_CHARGE_BURST.value();
            case LIGHT -> SoundEvents.AMETHYST_BLOCK_CHIME;
        };
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ProjectileProfile.CODEC.encodeStart(NbtOps.INSTANCE, profile).ifSuccess(encoded -> tag.put("profile", encoded));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ProjectileProfile.CODEC.parse(NbtOps.INSTANCE, tag.get("profile")).ifSuccess(this::setProfile);
    }
}
