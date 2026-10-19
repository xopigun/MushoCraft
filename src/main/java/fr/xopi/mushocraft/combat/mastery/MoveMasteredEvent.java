package fr.xopi.mushocraft.combat.mastery;

import fr.xopi.mushocraft.combat.move.MoveRef;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Fired on the NeoForge event bus when a player masters a move (Observer pattern): teachers, melded magic and the
 * like react to it without the mastery rules knowing about them.
 */
public class MoveMasteredEvent extends PlayerEvent {
    private final MoveRef move;

    public MoveMasteredEvent(ServerPlayer player, MoveRef move) {
        super(player);
        this.move = move;
    }

    public MoveRef move() {
        return move;
    }

    @Override
    public ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }
}
