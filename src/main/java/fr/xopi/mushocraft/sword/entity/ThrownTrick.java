package fr.xopi.mushocraft.sword.entity;

import fr.xopi.mushocraft.core.MushoEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * A North God trick in flight (Teardrop Bullet's spice bag, Crimson Ink's red ball): it bursts on whatever it touches.
 */
public class ThrownTrick extends ThrowableItemProjectile {
    private static final String KIND = "Trick";

    private TrickKind kind;

    public ThrownTrick(EntityType<? extends ThrownTrick> type, Level level) {
        super(type, level);
    }

    public static ThrownTrick create(Level level, LivingEntity thrower, TrickKind kind) {
        ThrownTrick trick = new ThrownTrick(MushoEntities.THROWN_TRICK.get(), level);
        trick.setOwner(thrower);
        trick.setPos(thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ());
        trick.kind = kind;
        trick.setItem(new ItemStack(kind.item()));
        return trick;
    }

    public TrickKind kind() {
        return kind != null ? kind : TrickKind.SPICE;
    }

    /** Asked while the entity is being built, before its kind is set. */
    @Override
    protected Item getDefaultItem() {
        return (kind != null ? kind : TrickKind.SPICE).item();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            kind().burstAt(level, result.getLocation(), getOwner());
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(KIND, kind().name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        try {
            kind = TrickKind.valueOf(tag.getString(KIND));
        } catch (IllegalArgumentException e) {
            kind = TrickKind.SPICE;
        }
    }
}
