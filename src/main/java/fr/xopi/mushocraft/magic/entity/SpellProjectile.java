package fr.xopi.mushocraft.magic.entity;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Matter conjured by an attack spell and thrown by the caster. One entity type for every element:
 * the element only changes the look and the impact.
 */
public class SpellProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> ELEMENT =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.FLOAT);
    private static final int MAX_LIFETIME_TICKS = 100;

    private ProjectileProfile profile = ProjectileProfile.of(SpellElement.WATER, 0.0F).build();

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
    }

    public SpellElement element() {
        return SpellElement.values()[Math.floorMod(entityData.get(ELEMENT), SpellElement.values().length)];
    }

    public float visualSize() {
        return entityData.get(SIZE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ELEMENT, 0);
        builder.define(SIZE, 0.5F);
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
        }
        if (profile.igniteSeconds() > 0) {
            target.igniteForSeconds(profile.igniteSeconds());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(element().trail(), getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.05);
            server.playSound(null, getX(), getY(), getZ(), impactSound(), SoundSource.PLAYERS, 0.7F, 1.2F);
            discard();
        }
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
