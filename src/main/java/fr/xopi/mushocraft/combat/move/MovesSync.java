package fr.xopi.mushocraft.combat.move;

import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.network.MovesSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends the owner everything the move bar and the moves screen need: slots, running cooldowns, known moves.
 */
public final class MovesSync {
    private MovesSync() {
    }

    public static void push(ServerPlayer player) {
        if (player.connection == null) {
            return;
        }
        long now = CooldownService.now(player);
        List<CooldownView> cooldowns = new ArrayList<>();
        player.getData(MushoAttachments.COOLDOWNS).pruned(now).entries().forEach((key, cooldown) ->
                cooldowns.add(new CooldownView(key, cooldown.remaining(now), cooldown.total())));
        List<MoveRef> known = new ArrayList<>(MagicService.knownSpells(player).stream().map(MoveRef::spell).toList());
        PacketDistributor.sendToPlayer(player, new MovesSyncPayload(MoveService.loadout(player), cooldowns, known));
    }
}
