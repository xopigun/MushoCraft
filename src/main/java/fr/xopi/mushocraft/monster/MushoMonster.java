package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.compat.epicfight.MonsterActions;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * A monster of the world (wiki, page <i>Monsters</i>): an animal mutated by mana, ranked by the Adventurers' Guild.
 * Many hunt in packs; a pack has one leader, and its members may be of several species (Terminate Boars lead Assault
 * Dogs). Members never harm each other. Each monster is an Epic Fight creature with its own skeleton and animations.
 */
public abstract class MushoMonster extends Monster {
    private static final String PACK = "MushoPack";
    private static final String LEADER = "MushoPackLeader";

    @Nullable
    private UUID pack;
    private boolean leader;
    private boolean wasChanting;

    protected MushoMonster(EntityType<? extends MushoMonster> type, Level level) {
        super(type, level);
    }

    public abstract MonsterRank rank();

    /** How many members of a pack may close in on the same prey at once; the others circle and wait their turn. */
    public int engagementPlaces() {
        return 2;
    }

    public Optional<UUID> pack() {
        return Optional.ofNullable(pack);
    }

    public boolean isPackLeader() {
        return leader;
    }

    public void joinPack(UUID pack, boolean leader) {
        this.pack = pack;
        this.leader = leader;
    }

    public boolean inSamePack(Entity other) {
        return pack != null && other instanceof MushoMonster monster && pack.equals(monster.pack);
    }

    /** Whether this monster holds one of the places to attack its current target (see {@link EngagementTokens}). */
    public boolean mayStrike() {
        LivingEntity target = getTarget();
        return target != null && EngagementTokens.isEngaged(this, target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != getTarget()) {
            EngagementTokens.release(this);
        }
        super.setTarget(target);
    }

    /** A pack defends each of its members: whoever wounds one has the whole pack upon them. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && isAlive() && source.getEntity() instanceof LivingEntity attacker && !inSamePack(attacker)
                && level() instanceof ServerLevel) {
            MonsterPack.callToHunt(this, attacker);
        }
        return hurt;
    }

    /** A blow of its own has just wounded `victim` (see {@link MonsterStrikes}): venom, a grip, a curse... */
    public void onStrike(LivingEntity victim) {
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return inSamePack(other) || super.isAlliedTo(other);
    }

    @Override
    public void die(DamageSource source) {
        EngagementTokens.release(this);
        super.die(source);
        if (leader && level() instanceof ServerLevel) {
            MonsterPack.succeed(this);
        }
    }

    /**
     * Monsters met together form one pack: the first of a spawned group leads it.
     */
    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        if (groupData instanceof PackSpawn spawn) {
            joinPack(spawn.pack(), false);
        } else if (pack == null) {
            UUID id = UUID.randomUUID();
            joinPack(id, true);
            groupData = new PackSpawn(id);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
    }

    /** A monster that chants a spell shows it: its species' casting gesture plays as the chant begins. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            boolean chanting = ChantBroadcast.isChanting(this);
            if (chanting && !wasChanting) {
                MonsterActions.play(this, MonsterAction.CAST);
            }
            wasChanting = chanting;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (pack != null) {
            tag.putUUID(PACK, pack);
            tag.putBoolean(LEADER, leader);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(PACK)) {
            joinPack(tag.getUUID(PACK), tag.getBoolean(LEADER));
        }
    }

    /**
     * Monsters of the world roam by day as by night (unlike the undead), on solid ground, outside peaceful games.
     */
    public static boolean checkMonsterSpawn(EntityType<? extends MushoMonster> type, ServerLevelAccessor level,
                                            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    /** Shared by the monsters of one spawned group, so they form one pack. */
    public record PackSpawn(UUID pack) implements SpawnGroupData {
    }
}
