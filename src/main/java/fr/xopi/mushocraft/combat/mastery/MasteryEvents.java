package fr.xopi.mushocraft.combat.mastery;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.event.PlayerBornEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * A new life starts with no move mastered.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MasteryEvents {
    private MasteryEvents() {
    }

    @SubscribeEvent
    public static void onBorn(PlayerBornEvent event) {
        MasteryService.forgetAll(event.getEntity());
    }
}
