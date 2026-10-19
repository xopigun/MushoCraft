package fr.xopi.mushocraft.magic.entity;

import fr.xopi.mushocraft.core.MushoEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * The visible body of a ray (Tornado Impact's drill of wind): drawn by the client from this entity's position to an end
 * point, as a hot core inside a coloured sheath, both glowing, the energy flowing along it (technique of the visual
 * reference, docs/VISUELS_REFERENCE.md). Only the look: the ray's harm is dealt by the spell. Not saved.
 */
public class SpellBeam extends Entity {
    private static final EntityDataAccessor<Vector3f> END = SynchedEntityData.defineId(SpellBeam.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(SpellBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SHEATH = SynchedEntityData.defineId(SpellBeam.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CORE = SynchedEntityData.defineId(SpellBeam.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(SpellBeam.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SPIN = SynchedEntityData.defineId(SpellBeam.class, EntityDataSerializers.BOOLEAN);

    public SpellBeam(EntityType<? extends SpellBeam> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    /**
     * @param spin whether the beam twists about itself (a drill of wind)
     */
    public static SpellBeam create(ServerLevel level, Vec3 from, Vec3 to, float width, int sheathRgb, int coreRgb, int lifetime,
                                   boolean spin) {
        SpellBeam beam = new SpellBeam(MushoEntities.SPELL_BEAM.get(), level);
        beam.moveTo(from.x, from.y, from.z);
        beam.entityData.set(END, new Vector3f((float) to.x, (float) to.y, (float) to.z));
        beam.entityData.set(WIDTH, width);
        beam.entityData.set(SHEATH, sheathRgb);
        beam.entityData.set(CORE, coreRgb);
        beam.entityData.set(LIFETIME, lifetime);
        beam.entityData.set(SPIN, spin);
        return beam;
    }

    public Vec3 end() {
        Vector3f end = entityData.get(END);
        return new Vec3(end.x, end.y, end.z);
    }

    public float width() {
        return entityData.get(WIDTH);
    }

    public int sheathRgb() {
        return entityData.get(SHEATH);
    }

    public int coreRgb() {
        return entityData.get(CORE);
    }

    public int lifetime() {
        return entityData.get(LIFETIME);
    }

    public boolean spins() {
        return entityData.get(SPIN);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount >= lifetime()) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(END, new Vector3f());
        builder.define(WIDTH, 0.5F);
        builder.define(SHEATH, 0xFFFFFF);
        builder.define(CORE, 0xFFFFFF);
        builder.define(LIFETIME, 10);
        builder.define(SPIN, false);
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
