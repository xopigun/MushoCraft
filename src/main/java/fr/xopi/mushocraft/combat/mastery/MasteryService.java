package fr.xopi.mushocraft.combat.mastery;

import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MovesSync;
import fr.xopi.mushocraft.core.MushoAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Single entry point for move mastery: how well a character knows each move, practising it, and the risk of
 * botching it. Spells and techniques go through the same rules.
 */
public final class MasteryService {
    private MasteryService() {
    }

    public static MoveMastery mastery(ServerPlayer player) {
        return player.getData(MushoAttachments.MOVE_MASTERY);
    }

    public static double repetitions(ServerPlayer player, MoveRef move) {
        return mastery(player).repetitions(move);
    }

    public static boolean isMastered(ServerPlayer player, MoveRef move) {
        return MasteryRules.isMastered(repetitions(player, move));
    }

    public static double powerFactor(ServerPlayer player, MoveRef move) {
        return MasteryRules.powerFactor(repetitions(player, move));
    }

    public static double speedFactor(ServerPlayer player, MoveRef move) {
        return MasteryRules.speedFactor(repetitions(player, move));
    }

    /**
     * Rolls whether this attempt fails for lack of mastery. A failed attempt still teaches a little.
     *
     * @return true if the move is botched
     */
    public static boolean botches(ServerPlayer player, MoveRef move) {
        if (player.getRandom().nextDouble() >= MasteryRules.failureChance(repetitions(player, move))) {
            return false;
        }
        practise(player, move, MasteryRules.FAILED_ATTEMPT);
        return true;
    }

    /** A successful repetition. */
    public static void succeeded(ServerPlayer player, MoveRef move) {
        practise(player, move, 1.0);
    }

    public static void practise(ServerPlayer player, MoveRef move, double amount) {
        boolean wasMastered = isMastered(player, move);
        setRepetitions(player, move, repetitions(player, move) + amount);
        if (!wasMastered && isMastered(player, move)) {
            player.sendSystemMessage(Component.translatable("message.mushocraft.move.mastered",
                    move.resolve().map(m -> m.displayName()).orElse(Component.literal(move.id().toString()))));
        }
    }

    public static void setRepetitions(ServerPlayer player, MoveRef move, double count) {
        player.setData(MushoAttachments.MOVE_MASTERY, mastery(player).with(move, count));
        MovesSync.push(player);
    }

    public static void forgetAll(ServerPlayer player) {
        player.setData(MushoAttachments.MOVE_MASTERY, MoveMastery.none());
        MovesSync.push(player);
    }

    /** « 12/50 » for messages. */
    public static Component progressText(ServerPlayer player, MoveRef move) {
        return Component.literal((int) Math.floor(repetitions(player, move)) + "/" + (int) MasteryRules.REPETITIONS_TO_MASTER);
    }
}
