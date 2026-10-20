package fr.xopi.mushocraft.monster;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;

/**
 * A monster that climbs walls as spiders do (Tarantula Deathlord, Devouring Devil): it paths up walls and clings to
 * whatever it runs into, as long as {@link #canClimb()} allows.
 */
public abstract class ClimbingMonster extends MushoMonster {
    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(ClimbingMonster.class,
            EntityDataSerializers.BOOLEAN);

    protected ClimbingMonster(EntityType<? extends ClimbingMonster> type, Level level) {
        super(type, level);
    }

    /** Whether it may climb right now. */
    protected boolean canClimb() {
        return true;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            entityData.set(CLIMBING, horizontalCollision && canClimb());
        }
    }

    public boolean isClimbing() {
        return entityData.get(CLIMBING);
    }

    @Override
    public boolean onClimbable() {
        return isClimbing();
    }
}
