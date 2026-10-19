package fr.xopi.mushocraft.client;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.hud.ManaBarLayer;
import fr.xopi.mushocraft.client.input.MushoKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Client-only registrations: HUD layers and key mappings.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class MushoClientSetup {
    private MushoClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, MushoCraft.id("mana_bar"), new ManaBarLayer());
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MushoKeyMappings.CIRCULATE_MANA);
    }
}
