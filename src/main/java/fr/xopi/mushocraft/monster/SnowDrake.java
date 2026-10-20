package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.monster.ai.CallThePackGoal;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import fr.xopi.mushocraft.monster.ai.ShunTheLightGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Snow Drake (wiki, page <i>Monsters</i>: Northern Territories): a lesser, wingless dragon with pure white scales,
 * three or four metres long. Drakes nest deep in caves in large groups and hate bright light, so they seldom come above
 * ground; they are docile and rarely attack unless their nest is threatened, but a pack of them is an S-rank threat.
 * Their scales strongly resist ice magic. Here a drake keeps to its nest, turns on whoever comes into it or strikes one
 * of the pack (the leader roars and the pack answers), chases the intruder away but not far, and leaves the light for
 * the shadow.
 */
public class SnowDrake extends MushoMonster {
    private static final String NEST = "Nest";
    /** Coming this close to the nest is a threat. */
    private static final double NEST_RADIUS = 9.0;
    /** An intruder that did not wound a drake is let go once this far from the nest. */
    private static final double CHASE_LIMIT = 22.0;
    private static final float ICE_SHARE = 0.25F;

    @Nullable
    private BlockPos nest;

    public SnowDrake(EntityType<? extends SnowDrake> type, Level level) {
        super(type, level);
        xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    /** A drake alone is rank A; the pack makes them an S-rank threat (wiki). */
    @Override
    public MonsterRank rank() {
        return MonsterRank.A;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new ShunTheLightGoal(this, 13, 7, 1.2));
        goalSelector.addGoal(2, new CallThePackGoal(this, SoundEvents.RAVAGER_ROAR, 32));
        goalSelector.addGoal(4, new PackHuntGoal(this, 1.15, 1.0, 4.0, 3.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::intrudes));
    }

    public BlockPos nest() {
        return nest != null ? nest : blockPosition();
    }

    public void settle(BlockPos nest) {
        this.nest = nest.immutable();
        restrictTo(this.nest, 16);
    }

    /** Whoever comes into the nest threatens it. */
    public boolean intrudes(LivingEntity someone) {
        return someone.distanceToSqr(nest().getCenter()) <= NEST_RADIUS * NEST_RADIUS;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        LivingEntity target = getTarget();
        if (!level().isClientSide && target != null && target != getLastHurtByMob()
                && target.distanceToSqr(nest().getCenter()) > CHASE_LIMIT * CHASE_LIMIT) {
            setTarget(null); // driven off: the drake goes back to its nest
        }
    }

    /** Its scales shrug off ice magic and the cold. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean ice = source.is(DamageTypeTags.IS_FREEZING)
                || source.getDirectEntity() instanceof SpellProjectile projectile && projectile.element() == SpellElement.ICE;
        if (ice) {
            amount *= ICE_SHARE;
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 0.8, getZ(), 12, 0.6, 0.3, 0.6, 0.02);
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        settle(blockPosition());
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
    }

    /** Drakes nest deep down, in the dark, under a roof of rock: never out under the sky. */
    public static boolean checkSnowDrakeSpawn(EntityType<? extends MushoMonster> type, ServerLevelAccessor level,
                                              MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return !level.canSeeSky(pos) && Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (nest != null) {
            tag.put(NEST, NbtUtils.writeBlockPos(nest));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        NbtUtils.readBlockPos(tag, NEST).ifPresent(this::settle);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.HOGLIN_STEP, 0.6F, 0.8F);
    }

    @Override
    public float getVoicePitch() {
        return 1.25F;
    }
}
