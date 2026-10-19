package fr.xopi.mushocraft.talent;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.event.PlayerBornEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Talents are given at birth, once per life.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class TalentBirthListener {
    private TalentBirthListener() {
    }

    @SubscribeEvent
    public static void onBorn(PlayerBornEvent event) {
        TalentService.roll(event.getEntity());
    }
}
