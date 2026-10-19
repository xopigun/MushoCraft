package fr.xopi.mushocraft.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * A pack that grows by itself while left alone: every so often its leader, out of a fight, brings a new member into
 * the pack (Pax Coyotes divide, goblins breed), until the pack reaches its size. Owned by the leader's species.
 */
public final class PackGrowth {
    private static final String LAST_GROWTH = "LastPackGrowth";

    private final long interval;
    private final int cap;
    private long lastGrowth;

    public PackGrowth(long interval, int cap) {
        this.interval = interval;
        this.cap = cap;
    }

    public int cap() {
        return cap;
    }

    /** Called every few seconds by the monster: the leader grows the pack when the time has come. */
    public void tick(MushoMonster monster) {
        if (monster.level() instanceof ServerLevel level && monster.isPackLeader() && monster.getTarget() == null
                && level.getGameTime() - lastGrowth >= interval) {
            grow(monster);
        }
    }

    /** A new member, fully grown, joins the monster's pack beside it, unless the pack is already full. */
    public Optional<MushoMonster> grow(MushoMonster monster) {
        if (!(monster.level() instanceof ServerLevel level) || monster.pack().isEmpty()
                || MonsterPack.mates(monster).size() + 1 >= cap) {
            return Optional.empty();
        }
        lastGrowth = level.getGameTime();
        if (!(monster.getType().create(level) instanceof MushoMonster newcomer)) {
            return Optional.empty();
        }
        newcomer.moveTo(monster.getX(), monster.getY(), monster.getZ(), monster.getYRot(), 0.0F);
        newcomer.joinPack(monster.pack().orElseThrow(), false);
        level.addFreshEntity(newcomer);
        return Optional.of(newcomer);
    }

    public void save(CompoundTag tag) {
        tag.putLong(LAST_GROWTH, lastGrowth);
    }

    public void load(CompoundTag tag) {
        lastGrowth = tag.getLong(LAST_GROWTH);
    }
}
