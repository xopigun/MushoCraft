package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.monster.ai.CallThePackGoal;
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
 * Pax Coyote (wiki, page <i>Monsters</i>: rank C, Biegoya Region of the Demon Continent): canine monsters about two
 * metres long that come by the dozen. A group is one coyote that multiplied by dividing itself every few months, all the
 * new ones under the complete control of their leader; if the leader falls, another takes its place at once. Their
 * strength is their number and their perfect discipline: three close in at a time while the rest hem the prey in.
 */
public class PaxCoyote extends MushoMonster {
    /** A leader divides about once every few in-game days while its group is small (the novel: every few months). */
    public static final long DIVISION_TICKS = 3 * 24000L;
    public static final int GROUP_CAP = 10;
    private final PackGrowth division = new PackGrowth(DIVISION_TICKS, GROUP_CAP);

    public PaxCoyote(EntityType<? extends PaxCoyote> type, Level level) {
        super(type, level);
        xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.C;
    }

    @Override
    public int engagementPlaces() {
        return 3;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RetreatWhenWoundedGoal(this, 0.25F, 1.4));
        goalSelector.addGoal(2, new CallThePackGoal(this, SoundEvents.WOLF_HOWL, 44));
        goalSelector.addGoal(3, new PackHuntGoal(this, 1.5, 1.1, 5.0, 2.2));
        goalSelector.addGoal(5, new FollowPackLeaderGoal(this, 1.1, 3.5, 8.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (tickCount % 200 == 0) {
            division.tick(this);
        }
    }

    /** The leader divides: a new coyote, fully grown, joins the group under its control. */
    public Optional<MushoMonster> divide() {
        return division.grow(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        division.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        division.load(tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getTarget() != null ? SoundEvents.WOLF_GROWL : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.WOLF_STEP, 0.15F, 1.1F);
    }

    @Override
    public float getVoicePitch() {
        return 0.85F + random.nextFloat() * 0.1F;
    }
}
