package fr.xopi.mushocraft.magic.entity;

import fr.xopi.mushocraft.core.MushoEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The visible body of a pillar or spike that a spell raises from the ground (Ice Pillar, Earth Lance, Earth
 * Hedgehog, the frost of Icicle Field): a model that bursts out, lives a little, then sinks and crumbles. Its solidity comes from
 * invisible conjured blocks placed by the spell; this entity is only its look. Not saved.
 */
public class SpellPillar extends Entity implements GeoEntity {
    /** Ticks before the end at which the pillar starts to crumble. */
    public static final int CRUMBLE_TICKS = 8;

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(SpellPillar.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(SpellPillar.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(SpellPillar.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public SpellPillar(EntityType<? extends SpellPillar> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    /**
     * @param tilt how far the pillar leans from upright, in degrees (spikes of a hedgehog lean every way)
     */
    public static SpellPillar create(ServerLevel level, PillarKind kind, Vec3 base, float height, int lifetime, float yaw, float tilt) {
        SpellPillar pillar = new SpellPillar(MushoEntities.SPELL_PILLAR.get(), level);
        pillar.moveTo(base.x, base.y, base.z, yaw, tilt);
        pillar.entityData.set(KIND, kind.ordinal());
        pillar.entityData.set(HEIGHT, height);
        pillar.entityData.set(LIFETIME, lifetime);
        return pillar;
    }

    public PillarKind kind() {
        return PillarKind.values()[Math.floorMod(entityData.get(KIND), PillarKind.values().length)];
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
        if (level() instanceof ServerLevel server) {
            if (tickCount == lifetime() - CRUMBLE_TICKS) {
                server.playSound(null, blockPosition(), kind().breakSound(), SoundSource.PLAYERS, 0.8F, 0.9F);
                server.sendParticles(kind().debris(), getX(), getY() + height() * 0.5, getZ(), 30, 0.4, height() * 0.3, 0.4, 0.05);
            }
            if (tickCount >= lifetime()) {
                discard();
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        if (!kind().isBlockbench()) {
            return;
        }
        String model = kind().model();
        RawAnimation riseThenIdle = RawAnimation.begin().thenPlay("animation." + model + ".rise").thenLoop("animation." + model + ".idle");
        RawAnimation crumble = RawAnimation.begin().thenPlay("animation." + model + ".crumble");
        controllers.add(new AnimationController<>(this, "pillar", 0, state ->
                state.setAndContinue(tickCount >= lifetime() - CRUMBLE_TICKS ? crumble : riseThenIdle)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, 0);
        builder.define(HEIGHT, 3.0F);
        builder.define(LIFETIME, 80);
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
