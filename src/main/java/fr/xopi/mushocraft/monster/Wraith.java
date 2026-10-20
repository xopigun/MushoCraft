package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.compat.epicfight.MonsterActions;
import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.ai.SpellcasterGoal;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import fr.xopi.mushocraft.magic.spell.DivineStrike;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import fr.xopi.mushocraft.monster.ai.RaiseTheDeadGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
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

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

/**
 * Wraith (wiki, page <i>Monsters</i>): an undead monster formed from a restless soul, a translucent figure without legs
 * floating in the air, impervious to most attacks. Fire spells (and other magic) burn it away for a while, but it forms
 * again: only Divine Strike magic rids the world of it for good. It animates skeletons to fight for it, which rise again
 * as long as it remains. The Deadly Wraith (rank A) also casts attack magic, and only an advanced Divine Strike ends it.
 */
public class Wraith extends MushoMonster {
    private static final EntityDataAccessor<Boolean> DEADLY = SynchedEntityData.defineId(Wraith.class, EntityDataSerializers.BOOLEAN);
    private static final String DEADLY_TAG = "Deadly";
    private static final String REFORMING_TAG = "Reforming";
    /** How long a wraith burnt away takes to form again. */
    public static final int REFORM_TICKS = 600;
    /** Share of wraiths met that are Deadly Wraiths. */
    private static final float DEADLY_SHARE = 0.25F;

    private int reforming;

    public Wraith(EntityType<? extends Wraith> type, Level level) {
        super(type, level);
        xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DEADLY, false);
    }

    public boolean isDeadly() {
        return entityData.get(DEADLY);
    }

    public void setDeadly(boolean deadly) {
        entityData.set(DEADLY, deadly);
    }

    @Override
    public MonsterRank rank() {
        return isDeadly() ? MonsterRank.A : MonsterRank.C;
    }

    /** Lone hunters: the wraith and its skeletons close in together. */
    @Override
    public int engagementPlaces() {
        return 4;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new ReformingGoal());
        goalSelector.addGoal(2, new RaiseTheDeadGoal(this));
        goalSelector.addGoal(3, new SpellcasterGoal(this, this::spells, spell -> 1.0 + 0.1 * Math.max(0, rankOfMagic().ordinal() - spell.rank().ordinal())));
        goalSelector.addGoal(4, new PackHuntGoal(this, 1.2, 1.0, 4.0, 2.4));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    /** The Deadly Wraith casts attack magic; the common one only claws. */
    private List<Spell> spells() {
        if (!isDeadly()) {
            return List.of();
        }
        return List.of(MushoSpells.STONE_CANNON.get(), MushoSpells.ICE_LANCE.get(), MushoSpells.WIND_SLICE.get(), MushoSpells.WATER_CANNON.get());
    }

    private Rank rankOfMagic() {
        return isDeadly() ? Rank.SAINT : Rank.INTERMEDIATE;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        if (spawnType != MobSpawnType.EVENT && random.nextFloat() < DEADLY_SHARE) {
            setDeadly(true);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
    }

    @Override
    public Component getName() {
        return hasCustomName() ? super.getName() : Component.translatable(isDeadly() ? "entity.mushocraft.wraith.deadly" : "entity.mushocraft.wraith");
    }

    public boolean isReforming() {
        return reforming > 0;
    }

    /**
     * Ordinary blows pass through it. Fire and magic hurt it, but what would end it only burns it away for a while;
     * Divine Strike (advanced, for a Deadly Wraith) ends it for good.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isReforming() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        boolean divine = DivineStrike.isDivine(source);
        boolean harmful = divine || source.is(DamageTypeTags.IS_FIRE) || source.is(MushoDamageTypes.SPELL)
                || source.is(MushoDamageTypes.ELECTRIC) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (!harmful) {
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.2, getZ(), 4, 0.3, 0.4, 0.3, 0.01);
            }
            return false;
        }
        if (amount >= getHealth() && !endsForGood(source)) {
            burnAway();
            return true;
        }
        return super.hurt(source, amount);
    }

    private boolean endsForGood(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        Rank needed = isDeadly() ? Rank.ADVANCED : Rank.BEGINNER;
        return DivineStrike.rank(source).map(rank -> rank.isAtLeast(needed)).orElse(false);
    }

    /** Burnt away: it fades, out of reach, and forms again where it stood. */
    public void burnAway() {
        reforming = REFORM_TICKS;
        setHealth(1.0F);
        setTarget(null);
        getNavigation().stop();
        EngagementTokens.release(this);
        ChantBroadcast.stopped(this);
        setInvisible(true);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.0, getZ(), 40, 0.5, 0.8, 0.5, 0.05);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 20, 0.4, 0.6, 0.4, 0.02);
            server.playSound(null, blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.5F, 0.7F);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        if (reforming > 0) {
            if (--reforming == 0) {
                setInvisible(false);
                setHealth(getMaxHealth());
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0, getZ(), 30, 0.4, 0.8, 0.4, 0.02);
                    server.playSound(null, blockPosition(), SoundEvents.VEX_CHARGE, SoundSource.HOSTILE, 1.2F, 0.6F);
                }
            } else if (tickCount % 10 == 0 && level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.5, getZ(), 2, 0.4, 0.2, 0.4, 0.01);
            }
            return;
        }
    }

    /** Its skeletons crumble with it. */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel server) {
            RaiseTheDeadGoal.bound(this).forEach(skeleton -> skeleton.kill());
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(DEADLY_TAG, isDeadly());
        tag.putInt(REFORMING_TAG, reforming);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setDeadly(tag.getBoolean(DEADLY_TAG));
        reforming = tag.getInt(REFORMING_TAG);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.6F + random.nextFloat() * 0.1F;
    }

    /** While it forms again, it does nothing at all. */
    private final class ReformingGoal extends Goal {
        private ReformingGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return isReforming();
        }

        @Override
        public void start() {
            getNavigation().stop();
        }
    }
}
