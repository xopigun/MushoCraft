package fr.xopi.mushocraft.sword.entity;

import fr.xopi.mushocraft.core.MushoEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A weapon flung end over end at high speed (North God Style, Sword Throwing Techniques). It hurts what it hits,
 * then falls to the ground where its owner must pick it back up.
 */
public class ThrownWeapon extends ThrowableProjectile {
    private static final EntityDataAccessor<ItemStack> WEAPON =
            SynchedEntityData.defineId(ThrownWeapon.class, EntityDataSerializers.ITEM_STACK);
    private static final int MAX_LIFETIME_TICKS = 200;

    private float damage;

    public ThrownWeapon(EntityType<? extends ThrownWeapon> type, Level level) {
        super(type, level);
    }

    public static ThrownWeapon create(Level level, LivingEntity thrower, ItemStack weapon, float damage) {
        ThrownWeapon thrown = new ThrownWeapon(MushoEntities.THROWN_WEAPON.get(), level);
        thrown.setOwner(thrower);
        thrown.entityData.set(WEAPON, weapon.copy());
        thrown.damage = damage;
        return thrown;
    }

    public ItemStack weapon() {
        return entityData.get(WEAPON);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WEAPON, ItemStack.EMPTY);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.02;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > MAX_LIFETIME_TICKS) {
            dropWeapon();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(damageSources().thrown(this, getOwner()), damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 1.2F);
            dropWeapon();
        }
    }

    private void dropWeapon() {
        if (!weapon().isEmpty()) {
            spawnAtLocation(weapon());
            entityData.set(WEAPON, ItemStack.EMPTY);
        }
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!weapon().isEmpty()) {
            tag.put("weapon", weapon().save(registryAccess()));
        }
        tag.putFloat("damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ItemStack.parse(registryAccess(), tag.getCompound("weapon")).ifPresent(stack -> entityData.set(WEAPON, stack));
        damage = tag.getFloat("damage");
    }
}
