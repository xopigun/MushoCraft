package fr.xopi.mushocraft.life.event;

import fr.xopi.mushocraft.life.LifeStage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Posted on the game bus when a character enters a new life stage, growing up or going back
 * (the latter only through debug commands).
 */
public class LifeStageChangedEvent extends PlayerEvent {
    private final LifeStage previousStage;
    private final LifeStage newStage;

    public LifeStageChangedEvent(ServerPlayer player, LifeStage previousStage, LifeStage newStage) {
        super(player);
        this.previousStage = previousStage;
        this.newStage = newStage;
    }

    @Override
    public ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }

    public LifeStage getPreviousStage() {
        return previousStage;
    }

    public LifeStage getNewStage() {
        return newStage;
    }
}
