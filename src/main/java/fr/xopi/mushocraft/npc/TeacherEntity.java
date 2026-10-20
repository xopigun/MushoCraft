package fr.xopi.mushocraft.npc;

import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.core.Rank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * A person who teaches: a sword master or a magic tutor. Peaceful unless attacked, they fight back with
 * Epic Fight's humanoid combat AI (given by their entity patch). Right-click to be taught (Template Method:
 * subclasses say what and how they teach).
 */
public abstract class TeacherEntity extends PathfinderMob {
    /** How far a resident teacher strays from its post. */
    private static final int RESIDENT_RANGE = 6;
    private boolean resident;

    private static final EntityDataAccessor<Integer> RANK = SynchedEntityData.defineId(TeacherEntity.class, EntityDataSerializers.INT);

    protected TeacherEntity(EntityType<? extends TeacherEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    /** Teachers are rare: a natural one never appears within this distance of another. */
    public static final double SPAWN_SPACING = 128.0;
    private static final int SPAWN_RARITY = 4;

    /**
     * Natural spawn rule: usual ground rules, in daylight-lit open land, one chance in {@link #SPAWN_RARITY},
     * and no other teacher around, so a region has at most one master or tutor to seek out.
     * (While a chunk is generated only its neighbours are visible: the rarity roll keeps them apart there.)
     */
    public static boolean checkTeacherSpawnRules(EntityType<? extends TeacherEntity> type, ServerLevelAccessor level,
                                                 MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return random.nextInt(SPAWN_RARITY) == 0
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random)
                && level.getRawBrightness(pos, 0) > 8
                && level.getEntitiesOfClass(TeacherEntity.class, new AABB(pos).inflate(SPAWN_SPACING)).isEmpty();
    }

    public Rank rank() {
        return Rank.values()[Math.floorMod(entityData.get(RANK), Rank.values().length)];
    }

    public void setRank(Rank rank) {
        entityData.set(RANK, rank.ordinal());
        refreshName();
    }

    /**
     * One lesson, the same for every teacher (Template Method). The teacher goes through the ranks from Beginner to
     * their own and stops at the first thing the pupil lacks:
     * <ol>
     *     <li>for a rank above the pupil's, a requirement (a trained body, enough mana);</li>
     *     <li>a move of that rank the pupil has never seen: the teacher shows it, one move per lesson;</li>
     *     <li>a move shown but not yet mastered: the pupil must keep repeating it;</li>
     *     <li>every move mastered: the teacher grants the rank, if nothing else stands in the way.</li>
     * </ol>
     */
    protected final void teach(ServerPlayer player) {
        welcome(player);
        for (Rank tier : Rank.values()) {
            if (!rank().isAtLeast(tier)) {
                break;
            }
            boolean promotion = tier.ordinal() > pupilRank(player).ordinal();
            if (promotion) {
                Optional<Component> unready = unreadyFor(player, tier);
                if (unready.isPresent()) {
                    say(player, unready.get());
                    return;
                }
            }
            for (MoveRef move : movesOf(tier)) {
                if (!knows(player, move)) {
                    show(player, move);
                    say(player, Component.translatable("message.mushocraft.lesson.show", MoveService.describe(move)));
                    return;
                }
                if (!MasteryService.isMastered(player, move)) {
                    say(player, Component.translatable("message.mushocraft.lesson.practise", MoveService.describe(move),
                            MasteryService.progressText(player, move)));
                    return;
                }
            }
            if (promotion) {
                grant(player, tier).ifPresent(refusal -> say(player, refusal));
                return;
            }
        }
        say(player, Component.translatable("message.mushocraft.lesson.nothing_more"));
    }

    /** The teacher speaks: « Name: "words" ». */
    protected void say(ServerPlayer player, Component words) {
        player.sendSystemMessage(Component.translatable("message.mushocraft.lesson.says", getDisplayName(), words));
    }

    /** First words of a lesson (a sword master takes the pupil as a disciple). */
    protected void welcome(ServerPlayer player) {
    }

    /** The pupil's rank in what this teacher teaches. */
    protected abstract Rank pupilRank(ServerPlayer player);

    /** The moves of one rank, in the order they are taught. */
    protected abstract List<MoveRef> movesOf(Rank tier);

    /** What the pupil still lacks before being taught a rank above theirs, if anything. */
    protected abstract Optional<Component> unreadyFor(ServerPlayer player, Rank tier);

    protected abstract boolean knows(ServerPlayer player, MoveRef move);

    /** The teacher demonstrates a move: the pupil now knows it, and must master it by repetition. */
    protected abstract void show(ServerPlayer player, MoveRef move);

    /**
     * Grants the next rank once its moves are mastered.
     *
     * @return why the rank could not be granted, if it was not
     */
    protected abstract Optional<Component> grant(ServerPlayer player, Rank tier);

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

    /**
     * A resident teacher lives in a place of its style or school (a dojo, a church of Milis): it keeps the style or
     * school the place gave it, and stays within a few blocks of its post.
     */
    public boolean isResident() {
        return resident;
    }

    /** Settles the teacher at its post: it will not wander far from it. */
    protected void settle() {
        resident = true;
        restrictTo(blockPosition(), RESIDENT_RANGE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("rank", rank().getSerializedName());
        tag.putBoolean("resident", resident);
        if (resident && hasRestriction()) {
            tag.putLong("post", getRestrictCenter().asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        resident = tag.getBoolean("resident");
        if (resident && tag.contains("post")) {
            restrictTo(BlockPos.of(tag.getLong("post")), RESIDENT_RANGE);
        }
        for (Rank rank : Rank.values()) {
            if (rank.getSerializedName().equals(tag.getString("rank"))) {
                setRank(rank);
            }
        }
    }
}
