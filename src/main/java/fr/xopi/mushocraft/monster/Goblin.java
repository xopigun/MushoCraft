package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.monster.ai.CallThePackGoal;
import fr.xopi.mushocraft.monster.ai.FleeWhenOutnumberedGoal;
import fr.xopi.mushocraft.monster.ai.FollowPackLeaderGoal;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import fr.xopi.mushocraft.monster.ai.RetreatWhenWoundedGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Goblin (wiki, page <i>Monsters</i>: rank E, forest boundaries): humanoid monsters with crude weapons, unable to
 * understand human speech. In small numbers they are mostly harmless, and a goblin with too few of its gang around keeps
 * away from people; but left alone they breed fast and raid nearby villages. A gang screeches to gather, two club the
 * prey while the rest close in around it.
 */
public class Goblin extends MushoMonster {
    /** A gang grows by one every two in-game days while left alone (the novel: they reproduce rapidly). */
    public static final long BREEDING_TICKS = 2 * 24000L;
    public static final int GANG_CAP = 8;
    /** Packmates a goblin needs around it to dare face a person. */
    public static final int BRAVE_FROM = 2;

    private final PackGrowth breeding = new PackGrowth(BREEDING_TICKS, GANG_CAP);

    public Goblin(EntityType<? extends Goblin> type, Level level) {
        super(type, level);
        xpReward = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.29)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.E;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new FleeWhenOutnumberedGoal(this, BRAVE_FROM, 1.35));
        goalSelector.addGoal(1, new RetreatWhenWoundedGoal(this, 0.4F, 1.4));
        goalSelector.addGoal(2, new CallThePackGoal(this, SoundEvents.PIGLIN_ANGRY, 28));
        goalSelector.addGoal(3, new PackHuntGoal(this, 1.3, 1.0, 4.0, 2.0));
        goalSelector.addGoal(5, new FollowPackLeaderGoal(this, 1.0, 3.5, 8.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (tickCount % 200 == 0) {
            breeding.tick(this);
        }
    }

    /** The gang breeds: one more goblin joins it, unless it is already at full size. */
    public Optional<MushoMonster> breed() {
        return breeding.grow(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        breeding.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        breeding.load(tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.PIGLIN_STEP, 0.15F, 1.3F);
    }

    @Override
    public float getVoicePitch() {
        return 1.35F + random.nextFloat() * 0.15F;
    }
}
