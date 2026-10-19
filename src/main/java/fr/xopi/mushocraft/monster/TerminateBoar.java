package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.monster.ai.CallThePackGoal;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Terminate Boar (wiki, page <i>Monsters</i>: rank D alone, C or even B with its pack; Asura Kingdom): a bipedal,
 * four-armed porcine monster. It usually attacks with a loyal pack of Assault Dogs: one is never met alone, it comes
 * with three or four dogs it leads, roars to set them on the prey, and fights in their midst, raking with its four
 * claws, smashing down with both upper arms, and charging tusks first at prey that keeps its distance.
 */
public class TerminateBoar extends MushoMonster {
    private static final int MIN_DOGS = 3;
    private static final int MAX_DOGS = 4;

    public TerminateBoar(EntityType<? extends TerminateBoar> type, Level level) {
        super(type, level);
        xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.D;
    }

    /** The boar leads: it always fights in the front line, and two of its dogs with it. */
    @Override
    public int engagementPlaces() {
        return 3;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new CallThePackGoal(this, SoundEvents.RAVAGER_ROAR, 44));
        goalSelector.addGoal(3, new PackHuntGoal(this, 1.3, 1.0, 6.0, 3.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    /** It comes with its dogs: a few Assault Dogs are born into its pack around it. */
    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, groupData);
        if (isPackLeader() && spawnType != MobSpawnType.EVENT) {
            bringDogs(level.getLevel());
        }
        return data;
    }

    /** Summons the boar's pack of Assault Dogs around it. */
    public void bringDogs(ServerLevel level) {
        int count = MIN_DOGS + random.nextInt(MAX_DOGS - MIN_DOGS + 1);
        for (int i = 0; i < count; i++) {
            AssaultDog dog = MushoEntities.ASSAULT_DOG.get().create(level);
            if (dog == null) {
                return;
            }
            double angle = 2 * Math.PI * i / count;
            dog.moveTo(getX() + Math.cos(angle) * 2.5, getY(), getZ() + Math.sin(angle) * 2.5, random.nextFloat() * 360.0F, 0.0F);
            dog.joinPack(pack().orElseThrow(), false);
            level.addFreshEntity(dog);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HOGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.HOGLIN_STEP, 0.4F, 0.8F);
    }

    @Override
    public float getVoicePitch() {
        return 0.7F + random.nextFloat() * 0.1F;
    }
}
