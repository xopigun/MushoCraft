package fr.xopi.mushocraft.magic.summon;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Binds summoned creatures to their summoner: while the covenant lasts, the creature follows its master, defends it,
 * strikes what it strikes, and never turns on it or on its fellow servants. The bond is saved with the creature; its
 * behaviour is given back when the creature is loaded again.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class SummonBonds {
    /** Summoners seen binding a creature, for those not in the server's player list (fake players). */
    private static final Map<UUID, WeakReference<ServerPlayer>> SUMMONERS = new ConcurrentHashMap<>();

    private SummonBonds() {
    }

    public static void bind(Mob creature, ServerPlayer summoner, int ticks) {
        SUMMONERS.put(summoner.getUUID(), new WeakReference<>(summoner));
        creature.setData(MushoAttachments.SUMMON_BOND, SummonBond.to(summoner.getUUID(), creature.level().getGameTime() + ticks));
        creature.setTarget(null);
        creature.setPersistenceRequired();
        SummonerGoals.addTo(creature);
    }

    public static SummonBond bondOf(LivingEntity creature) {
        return creature.hasData(MushoAttachments.SUMMON_BOND) ? creature.getData(MushoAttachments.SUMMON_BOND) : SummonBond.none();
    }

    public static boolean isBound(LivingEntity creature) {
        return bondOf(creature).binds(creature.level().getGameTime());
    }

    /** The master this creature serves, if the covenant still holds and the master is here. */
    public static Optional<ServerPlayer> summoner(LivingEntity creature) {
        SummonBond bond = bondOf(creature);
        if (!bond.binds(creature.level().getGameTime()) || !(creature.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        return bond.summoner().map(id -> find(level, id))
                .filter(player -> !player.isRemoved() && player.level() == creature.level());
    }

    private static ServerPlayer find(ServerLevel level, UUID id) {
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(id);
        if (online != null) {
            return online;
        }
        WeakReference<ServerPlayer> known = SUMMONERS.get(id);
        return known == null ? null : known.get();
    }

    /** Whether these two are on the same side: a master and its servant, or two servants of one master. */
    public static boolean allied(LivingEntity first, LivingEntity second) {
        long now = first.level().getGameTime();
        SummonBond a = bondOf(first);
        SummonBond b = bondOf(second);
        return a.servant(second.getUUID(), now) || b.servant(first.getUUID(), now)
                || (a.binds(now) && b.binds(now) && a.summoner().equals(b.summoner()));
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob creature
                && creature.hasData(MushoAttachments.SUMMON_BOND)) {
            SummonerGoals.addTo(creature);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget != null && allied(event.getEntity(), newTarget)) {
            event.setCanceled(true);
        }
    }
}
