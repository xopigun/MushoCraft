package fr.xopi.mushocraft.magic.entity;

import fr.xopi.mushocraft.core.MushoEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import javax.annotation.Nullable;

/**
 * The place where a lasting spell rages for a few seconds: a storm cloud, a tornado, a sealing barrier.
 * Invisible itself; its {@link Behavior} acts on the world each tick (Strategy pattern). Spell zones are not saved:
 * a spell does not outlast the world being reloaded.
 */
public class SpellZone extends Entity {
    /** What a zone does while it lasts. */
    @FunctionalInterface
    public interface Behavior {
        void tick(SpellZone zone, ServerLevel level, int age);
    }

    private static final EntityDataAccessor<Integer> VISUAL = SynchedEntityData.defineId(SpellZone.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(SpellZone.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(SpellZone.class, EntityDataSerializers.INT);

    private Behavior behavior;
    private LivingEntity caster;
    private double radius;
    private double power;
    private int lifetime;

    public SpellZone(EntityType<? extends SpellZone> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static SpellZone create(ServerLevel level, LivingEntity caster, Behavior behavior, ZoneVisual visual, double radius,
                                   double power, int lifetime) {
        SpellZone zone = new SpellZone(MushoEntities.SPELL_ZONE.get(), level);
        zone.caster = caster;
        zone.behavior = behavior;
        zone.radius = radius;
        zone.power = power;
        zone.lifetime = lifetime;
        zone.entityData.set(VISUAL, visual.ordinal());
        zone.entityData.set(RADIUS, (float) radius);
        zone.entityData.set(LIFETIME, lifetime);
        return zone;
    }

    /** What the client draws. */
    public ZoneVisual visual() {
        return ZoneVisual.values()[Math.floorMod(entityData.get(VISUAL), ZoneVisual.values().length)];
    }

    /** The zone's radius, as the client knows it. */
    public float visualRadius() {
        return entityData.get(RADIUS);
    }

    /** The zone's lifetime in ticks, as the client knows it. */
    public int visualLifetime() {
        return entityData.get(LIFETIME);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        if (behavior == null || tickCount > lifetime) {
            discard();
            return;
        }
        behavior.tick(this, server, tickCount);
    }

    /** Living creatures inside the zone (a vertical cylinder of the zone's radius), the caster excepted. */
    public List<LivingEntity> creaturesInside(double height) {
        AABB box = new AABB(getX() - radius, getY() - 1.0, getZ() - radius, getX() + radius, getY() + height, getZ() + radius);
        return level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity != caster && entity.isAlive()
                && entity.distanceToSqr(getX(), entity.getY(), getZ()) <= radius * radius);
    }

    @Nullable
    public LivingEntity caster() {
        return caster;
    }

    public double radius() {
        return radius;
    }

    public double power() {
        return power;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(VISUAL, 0);
        builder.define(RADIUS, 1.0F);
        builder.define(LIFETIME, 100);
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
