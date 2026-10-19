package fr.xopi.mushocraft.life;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.event.LifeStageChangedEvent;
import fr.xopi.mushocraft.life.event.PlayerBornEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Tells the player when they are born and when they reach a new life stage.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class LifeStageNotifier {
    private LifeStageNotifier() {
    }

    @SubscribeEvent
    public static void onBorn(PlayerBornEvent event) {
        event.getEntity().sendSystemMessage(Component.translatable("message.mushocraft.life.born"));
    }

    @SubscribeEvent
    public static void onStageChanged(LifeStageChangedEvent event) {
        event.getEntity().sendSystemMessage(Component.translatable(
                "message.mushocraft.life.stage_changed", event.getNewStage().displayName()));
    }
}
