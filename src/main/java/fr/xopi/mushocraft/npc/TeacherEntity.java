package fr.xopi.mushocraft.npc;

import fr.xopi.mushocraft.core.Rank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A person who teaches: a sword master or a magic tutor. Peaceful unless attacked, they fight back with
 * Epic Fight's humanoid combat AI (given by their entity patch). Right-click to be taught (Template Method:
 * subclasses say what and how they teach).
 */
public abstract class TeacherEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> RANK = SynchedEntityData.defineId(TeacherEntity.class, EntityDataSerializers.INT);

    protected TeacherEntity(EntityType<? extends TeacherEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public Rank rank() {
        return Rank.values()[Math.floorMod(entityData.get(RANK), Rank.values().length)];
    }

    public void setRank(Rank rank) {
        entityData.set(RANK, rank.ordinal());
        refreshName();
    }

    /** Teaches the player what this teacher can teach them now, and tells them how it went. */
    protected abstract void teach(ServerPlayer player);

    /** The title shown above the head, e.g. « Saint of the Water God Style ». */
    protected abstract Component title();

    protected void refreshName() {
        setCustomName(title());
        setCustomNameVisible(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RANK, Rank.INTERMEDIATE.ordinal());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || getTarget() == player) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            getLookControl().setLookAt(player);
            teach(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("rank", rank().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        for (Rank rank : Rank.values()) {
            if (rank.getSerializedName().equals(tag.getString("rank"))) {
                setRank(rank);
            }
        }
    }
}
