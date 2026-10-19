package fr.xopi.mushocraft.life.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Posted on the game bus right after a character is born (first login, or rebirth through a command).
 * Systems that roll something once per life (talents, mana reservoir...) listen to it.
 */
public class PlayerBornEvent extends PlayerEvent {
    public PlayerBornEvent(ServerPlayer player) {
        super(player);
    }

    @Override
    public ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }
}
