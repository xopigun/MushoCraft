package fr.xopi.mushocraft.magic.entity;

import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.MushoParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Conjured fire with a body, drawn by the client as an animated mesh (after the anime: a white-hot heart wrapped in
 * cel-shaded orange flame):
 * <ul>
 *     <li>{@link Kind#BLAST}: an explosion. A flash, a swelling fireball that darkens into a cloud of smoke, a ring of
 *     air thrown along the ground, sparks.</li>
 *     <li>{@link Kind#COLUMN}: a pillar of flame roaring up from the ground.</li>
 * </ul>
 * Only the look: what the fire does is up to the spell. Not saved.
 */
public class SpellFire extends Entity {
    public enum Kind {
        BLAST,
        COLUMN
    }

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(SpellFire.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(SpellFire.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(SpellFire.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(SpellFire.class, EntityDataSerializers.INT);

    public SpellFire(EntityType<? extends SpellFire> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    /** An explosion of the given radius, centred on a point. */
    public static SpellFire blast(ServerLevel level, Vec3 centre, float radius) {
        return create(level, Kind.BLAST, centre, radius, radius, 18 + Math.round(radius * 4.0F));
    }

    /** A pillar of flame standing on a point of the ground. */
    public static SpellFire column(ServerLevel level, Vec3 base, float radius, float height, int lifetime) {
        return create(level, Kind.COLUMN, base, radius, height, lifetime);
    }

    private static SpellFire create(ServerLevel level, Kind kind, Vec3 position, float radius, float height, int lifetime) {
        SpellFire fire = new SpellFire(MushoEntities.SPELL_FIRE.get(), level);
        fire.moveTo(position.x, position.y, position.z, level.random.nextFloat() * 360.0F, 0.0F);
        fire.entityData.set(KIND, kind.ordinal());
        fire.entityData.set(RADIUS, radius);
        fire.entityData.set(HEIGHT, height);
        fire.entityData.set(LIFETIME, lifetime);
        return fire;
    }

    public Kind kind() {
        return Kind.values()[Math.floorMod(entityData.get(KIND), Kind.values().length)];
    }

    public float radius() {
        return entityData.get(RADIUS);
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
        if (kind() == Kind.BLAST) {
            tickBlast(server);
        } else {
            tickColumn(server);
        }
        if (tickCount >= lifetime()) {
            discard();
        }
    }

    private void tickBlast(ServerLevel server) {
        float radius = radius();
        if (tickCount == 1) {
            // count 0: the "speed" is the size of the flash and the reach of the ring
            server.sendParticles(MushoParticles.FLASH.get(), getX(), getY(), getZ(), 0, radius * 1.5, 0.0, 0.0, 1.0);
            server.sendParticles(MushoParticles.SHOCK_RING.get(), getX(), getY() - radius * 0.5 + 0.1, getZ(), 0, radius * 2.2, 0.0, 0.0, 1.0);
            server.sendParticles(MushoParticles.EMBER.get(), getX(), getY(), getZ(), Math.round(30 * radius),
                    radius * 0.4, radius * 0.4, radius * 0.4, 0.35);
            server.sendParticles(MushoParticles.FLAME.get(), getX(), getY(), getZ(), Math.round(20 * radius),
                    radius * 0.5, radius * 0.4, radius * 0.5, 0.08);
        }
        if (tickCount > lifetime() / 2 && tickCount % 2 == 0) {
            server.sendParticles(MushoParticles.SMOKE.get(), getX(), getY() + radius * 0.4, getZ(), Math.round(3 * radius),
                    radius * 0.5, radius * 0.3, radius * 0.5, 0.01);
        }
    }

    private void tickColumn(ServerLevel server) {
        float radius = radius();
        if (tickCount < lifetime() - 6) {
            server.sendParticles(MushoParticles.FLAME.get(), getX(), getY() + height() * 0.3, getZ(), 4,
                    radius * 0.5, height() * 0.3, radius * 0.5, 0.04);
            if (tickCount % 2 == 0) {
                server.sendParticles(MushoParticles.EMBER.get(), getX(), getY() + height() * 0.6, getZ(), 3,
                        radius * 0.4, height() * 0.3, radius * 0.4, 0.15);
            }
        }
        if (tickCount > lifetime() - 10 && tickCount % 2 == 0) {
            server.sendParticles(MushoParticles.SMOKE.get(), getX(), getY() + height(), getZ(), 3, radius * 0.4, 0.3, radius * 0.4, 0.02);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, 0);
        builder.define(RADIUS, 1.0F);
        builder.define(HEIGHT, 1.0F);
        builder.define(LIFETIME, 20);
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
