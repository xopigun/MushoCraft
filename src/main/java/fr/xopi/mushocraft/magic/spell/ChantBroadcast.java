package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.network.ChantSyncPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tells the clients that see a caster when its chant starts and stops, players and creatures alike. The server also
 * remembers who is chanting, so that a spell still taking shape can be found and disturbed (Disturb Magic).
 */
public final class ChantBroadcast {
    private static final Map<UUID, Spell> CHANTING = new ConcurrentHashMap<>();

    private ChantBroadcast() {
    }

    /** Whether a spell is taking shape in this caster's hands right now (server side). */
    public static boolean isChanting(LivingEntity caster) {
        return CHANTING.containsKey(caster.getUUID());
    }

    /** The spell taking shape in this caster's hands right now, if any (server side). */
    public static Optional<Spell> chanted(LivingEntity caster) {
        return Optional.ofNullable(CHANTING.get(caster.getUUID()));
    }

    public static void started(LivingEntity caster, Spell spell, int ticks) {
        if (!caster.level().isClientSide) {
            CHANTING.put(caster.getUUID(), spell);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster,
                    new ChantSyncPayload(caster.getId(), spell.school().ordinal(), ticks));
        }
    }

    public static void stopped(LivingEntity caster) {
        if (!caster.level().isClientSide) {
            CHANTING.remove(caster.getUUID());
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster, new ChantSyncPayload(caster.getId(), -1, 0));
        }
    }
}
