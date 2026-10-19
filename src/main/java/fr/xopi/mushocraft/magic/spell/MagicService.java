package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.combat.move.MoveKind;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.combat.move.MovesSync;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.core.MushoRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Single entry point for what a character knows of magic.
 */
public final class MagicService {
    private MagicService() {
    }

    public static boolean knows(ServerPlayer player, Spell spell) {
        return player.getData(MushoAttachments.MAGIC_KNOWLEDGE).knows(spell.id());
    }

    public static boolean knows(ServerPlayer player, MoveRef move) {
        return move.kind() == MoveKind.SPELL && player.getData(MushoAttachments.MAGIC_KNOWLEDGE).knows(move.id());
    }

    /** Known spells in a stable order (school, then rank, then name), for display. Removed spells are skipped. */
    public static List<ResourceLocation> knownSpells(ServerPlayer player) {
        return player.getData(MushoAttachments.MAGIC_KNOWLEDGE).spells().stream()
                .map(MushoRegistries.SPELL::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Spell::school)
                        .thenComparing(Spell::rank)
                        .thenComparing(spell -> spell.id().toString()))
                .map(Spell::id)
                .toList();
    }

    /**
     * Teaches a spell and puts it in the move bar if a slot is free.
     *
     * @return false if the spell was already known
     */
    public static boolean learn(ServerPlayer player, Spell spell) {
        if (knows(player, spell)) {
            return false;
        }
        player.setData(MushoAttachments.MAGIC_KNOWLEDGE, player.getData(MushoAttachments.MAGIC_KNOWLEDGE).with(spell.id()));
        player.sendSystemMessage(Component.translatable("message.mushocraft.magic.learned", spell.displayName()));
        MoveService.offer(player, spell.ref());
        MovesSync.push(player);
        return true;
    }

    public static void forgetAll(ServerPlayer player) {
        player.setData(MushoAttachments.MAGIC_KNOWLEDGE, MagicKnowledge.none());
        MovesSync.push(player);
    }
}
