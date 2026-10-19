package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.network.ChantSyncPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Tells the clients that see a caster when its chant starts and stops, players and creatures alike.
 */
public final class ChantBroadcast {
    private ChantBroadcast() {
    }

    public static void started(LivingEntity caster, Spell spell, int ticks) {
        if (!caster.level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster,
                    new ChantSyncPayload(caster.getId(), spell.school().ordinal(), ticks));
        }
    }

    public static void stopped(LivingEntity caster) {
        if (!caster.level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster, new ChantSyncPayload(caster.getId(), -1, 0));
        }
    }
}
