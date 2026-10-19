package fr.xopi.mushocraft.combat.move;

import fr.xopi.mushocraft.core.MushoAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Optional;

/**
 * Single entry point for the move bar on the server: what is in each slot, and pressing a slot.
 */
public final class MoveService {
    private MoveService() {
    }

    public static MoveLoadout loadout(ServerPlayer player) {
        return player.getData(MushoAttachments.LOADOUT);
    }

    public static void setSlot(ServerPlayer player, int slot, Optional<MoveRef> move) {
        if (slot < 0 || slot >= MoveLoadout.SIZE) {
            return;
        }
        MoveLoadout loadout = loadout(player);
        if (move.isPresent() && loadout.contains(move.get())) {
            // A move lives in one slot only: moving it empties its previous slot.
            for (int i = 0; i < MoveLoadout.SIZE; i++) {
                if (loadout.get(i).equals(move)) {
                    loadout = loadout.with(i, Optional.empty());
                }
            }
        }
        player.setData(MushoAttachments.LOADOUT, loadout.with(slot, move));
        MovesSync.push(player);
    }

    /** Puts a newly learned move in the first free slot, if any and if it is not already in the bar. */
    public static void offer(ServerPlayer player, MoveRef move) {
        MoveLoadout loadout = loadout(player);
        if (!loadout.contains(move)) {
            loadout.firstEmptySlot().ifPresent(slot -> setSlot(player, slot, Optional.of(move)));
        }
    }

    public static MoveUseResult use(ServerPlayer player, int slot) {
        Optional<Move> move = loadout(player).get(slot).flatMap(MoveRef::resolve);
        if (move.isEmpty()) {
            return MoveUseResult.fail("message.mushocraft.move.empty_slot");
        }
        Optional<Cooldowns.Cooldown> cooldown = CooldownService.active(player, move.get().cooldownKey());
        if (cooldown.isPresent()) {
            return MoveUseResult.fail("message.mushocraft.move.cooldown", move.get().displayName(),
                    String.format(Locale.ROOT, "%.1f", cooldown.get().remaining(CooldownService.now(player)) / 20.0));
        }
        return move.get().use(player);
    }

    public static void useAndReport(ServerPlayer player, int slot) {
        MoveUseResult result = use(player, slot);
        result.failure().ifPresent(message -> player.displayClientMessage(message.copy().withStyle(
                style -> style.withColor(0xFF7070)), true));
    }

    public static Component describe(MoveRef ref) {
        return ref.resolve().map(Move::displayName).orElse(Component.literal(ref.id().toString()));
    }
}
