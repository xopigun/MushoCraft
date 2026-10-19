package fr.xopi.mushocraft.client;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.sword.ActiveStyles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Forgets the styles of other players when leaving a world.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class ClientStyleView {
    private ClientStyleView() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ActiveStyles.clearClientView();
    }
}
