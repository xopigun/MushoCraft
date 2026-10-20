package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.monster.ai.BacklineTargetGoal;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Devouring Devil (wiki, page <i>Monsters</i>: Teleportation Labyrinth, floors 5-6, rank A): a gaping mouth with
 * razor-sharp fangs, long limbs and claws to scale the walls and get into the middle of a party; quick and hard-hitting,
 * but with low defences. The smoke of burnt Talfro roots makes it come down and lie low on the floor, an easy target.
 * Here it climbs walls, goes for the party's back line (whoever chants first), springs at its prey from afar; the smoke
 * of a lit campfire close by stands in for Talfro: it can no longer climb, it crawls and it hits weakly.
 */
public class DevouringDevil extends ClimbingMonster {
    /** How far the smoke of a campfire reaches. */
    private static final int SMOKE_REACH = 8;
    private static final int SMOKE_CHECK_TICKS = 20;

    private boolean smoked;

    public DevouringDevil(EntityType<? extends DevouringDevil> type, Level level) {
        super(type, level);
        xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.A;
    }

    @Override
    public int engagementPlaces() {
        return 3;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(4, new PackHuntGoal(this, 1.3, 1.1, 5.0, 1.8));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new BacklineTargetGoal(this, 20.0));
    }

    /** Lying low in the smoke of a campfire (Talfro). */
    public boolean isSmoked() {
        return smoked;
    }

    @Override
    protected boolean canClimb() {
        return !smoked;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server && tickCount % SMOKE_CHECK_TICKS == 0) {
            smoked = smokeNearby();
            if (smoked) {
                addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SMOKE_CHECK_TICKS + 5, 2, false, false));
                addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SMOKE_CHECK_TICKS + 5, 1, false, false));
                server.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.4, getZ(), 4, 0.4, 0.1, 0.4, 0.0);
            }
        }
    }

    private boolean smokeNearby() {
        BlockPos at = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(at.offset(-SMOKE_REACH, -3, -SMOKE_REACH), at.offset(SMOKE_REACH, 3, SMOKE_REACH))) {
            if (CampfireBlock.isLitCampfire(level().getBlockState(pos))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SILVERFISH_STEP, 0.4F, 0.6F);
    }

    @Override
    public float getVoicePitch() {
        return 0.5F;
    }
}
