package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import fr.xopi.mushocraft.monster.ai.WebSpitGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Tarantula Deathlord (wiki, page <i>Monsters</i>: Teleportation Labyrinth): a huge poisonous spider. It spits webs
 * so accurately that they tear the weapon from its prey's hand, and its bite carries a venom that elementary
 * detoxification magic cures. In the anime they come down the walls and from the ceilings of the labyrinth: here it
 * climbs walls as spiders do, and webs do not hold it.
 */
public class TarantulaDeathlord extends MushoMonster {
    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(TarantulaDeathlord.class,
            EntityDataSerializers.BOOLEAN);
    private static final int VENOM_TICKS = 140;
    private static final int VENOM_LEVEL = 1;

    public TarantulaDeathlord(EntityType<? extends TarantulaDeathlord> type, Level level) {
        super(type, level);
        xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.B;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new WebSpitGoal(this, 3.0, 14.0, 120));
        goalSelector.addGoal(4, new PackHuntGoal(this, 1.2, 1.0, 3.5, 2.6));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Its bite poisons. */
    @Override
    public void onStrike(LivingEntity victim) {
        victim.addEffect(new MobEffectInstance(MobEffects.POISON, VENOM_TICKS, VENOM_LEVEL), this);
    }

    // ---- climbing, as spiders do

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
            entityData.set(CLIMBING, horizontalCollision);
        }
    }

    public boolean isClimbing() {
        return entityData.get(CLIMBING);
    }

    @Override
    public boolean onClimbable() {
        return isClimbing();
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motion) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motion);
        }
    }

    /** Its own venom does nothing to it. */
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SPIDER_STEP, 0.3F, 0.7F);
    }

    @Override
    public float getVoicePitch() {
        return 0.6F;
    }
}
