package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.monster.ai.CallThePackGoal;
import fr.xopi.mushocraft.monster.ai.FollowPackLeaderGoal;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import fr.xopi.mushocraft.monster.ai.RetreatWhenWoundedGoal;
import net.minecraft.core.BlockPos;
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

/**
 * Assault Dog (wiki, page <i>Monsters</i>: rank E, Asura Kingdom): a canine monster that moves about like a trained
 * Dobermann. Assault Dogs hunt in packs, often led by a Terminate Boar: the leader howls and the pack closes in, two at
 * a time, while the others circle the prey; a badly bitten dog backs off and lets another take its place. They bite,
 * and leap at prey a few blocks away.
 */
public class AssaultDog extends MushoMonster {
    public AssaultDog(EntityType<? extends AssaultDog> type, Level level) {
        super(type, level);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.E;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RetreatWhenWoundedGoal(this, 0.3F, 1.4));
        goalSelector.addGoal(2, new CallThePackGoal(this, SoundEvents.WOLF_HOWL, 44));
        goalSelector.addGoal(3, new PackHuntGoal(this, 1.45, 1.05, 4.5, 2.2));
        goalSelector.addGoal(5, new FollowPackLeaderGoal(this, 1.1, 4.0, 9.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getTarget() != null ? SoundEvents.WOLF_GROWL : SoundEvents.WOLF_PANT;
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
        playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    @Override
    public float getVoicePitch() {
        return 0.75F + random.nextFloat() * 0.1F;
    }
}
