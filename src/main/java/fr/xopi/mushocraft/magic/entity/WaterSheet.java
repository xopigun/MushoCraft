package fr.xopi.mushocraft.magic.entity;

import fr.xopi.mushocraft.core.MushoEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The visible water of Water Shield, as in the anime: a sphere of water swells, then spreads into a living wall that
 * ripples and spills over, and finally collapses. The wall's solidity comes from invisible conjured blocks placed by
 * the spell; this entity only carries the look (drawn by the client) and the splashes.
 */
public class WaterSheet extends Entity {
    /** Ticks the sphere swells, then the ticks it takes to spread into the wall. */
    public static final int BUBBLE_TICKS = 6;
    public static final int SPREAD_TICKS = 8;
    /** Ticks the wall takes to collapse at the end. */
    public static final int COLLAPSE_TICKS = 10;

    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(WaterSheet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(WaterSheet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(WaterSheet.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ARC_RADIUS = SynchedEntityData.defineId(WaterSheet.class, EntityDataSerializers.FLOAT);

    public WaterSheet(EntityType<? extends WaterSheet> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    /**
     * @param centre    the caster's feet: the wall curves round this point
     * @param yaw       the direction the wall stands in, from the centre
     * @param width     length of the wall along its curve
     * @param arcRadius distance from the centre to the wall
     */
    public static WaterSheet create(ServerLevel level, Vec3 centre, float yaw, float width, float height, int lifetime,
                                    float arcRadius) {
        WaterSheet sheet = new WaterSheet(MushoEntities.WATER_SHEET.get(), level);
        sheet.moveTo(centre.x, centre.y, centre.z, yaw, 0.0F);
        sheet.entityData.set(WIDTH, width);
        sheet.entityData.set(HEIGHT, height);
        sheet.entityData.set(LIFETIME, lifetime);
        sheet.entityData.set(ARC_RADIUS, arcRadius);
        return sheet;
    }

    public float arcRadius() {
        return entityData.get(ARC_RADIUS);
    }

    /** A point on the wall at a share of its length (0 to 1) and a height, in world space. */
    public Vec3 pointOnWall(double along, double up) {
        double angle = (along - 0.5) * width() / arcRadius();
        Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot());
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        return position().add(forward.scale(Math.cos(angle) * arcRadius())).add(right.scale(Math.sin(angle) * arcRadius()))
                .add(0.0, up, 0.0);
    }

    public float width() {
        return entityData.get(WIDTH);
    }

    public float height() {
        return entityData.get(HEIGHT);
    }

    public int lifetime() {
        return entityData.get(LIFETIME);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        if (tickCount == BUBBLE_TICKS + SPREAD_TICKS) {
            server.playSound(null, blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        if (tickCount > BUBBLE_TICKS + SPREAD_TICKS && tickCount % 3 == 0) {
            // water spills over the top of the wall
            Vec3 top = pointOnWall(random.nextDouble(), height());
            server.sendParticles(ParticleTypes.FALLING_WATER, top.x, top.y, top.z, 3, 0.1, 0.05, 0.1, 0.0);
        }
        if (tickCount == lifetime() - COLLAPSE_TICKS) {
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        if (tickCount >= lifetime()) {
            Vec3 middle = pointOnWall(0.5, 0.3);
            server.sendParticles(ParticleTypes.SPLASH, middle.x, middle.y, middle.z, 60, width() * 0.4, 0.2, width() * 0.4, 0.3);
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WIDTH, 3.0F);
        builder.define(HEIGHT, 3.0F);
        builder.define(LIFETIME, 200);
        builder.define(ARC_RADIUS, 2.5F);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
