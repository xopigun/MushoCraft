package fr.xopi.mushocraft.client.hud;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.compat.epicfight.EpicFightModes;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Hides the vanilla hotbar in battle mode: the move bar is drawn in its place.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class HotbarHider {
    private HotbarHider() {
    }

    @SubscribeEvent
    public static void onRenderLayer(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getName().equals(VanillaGuiLayers.HOTBAR) && minecraft.player != null
                && EpicFightModes.isBattleMode(minecraft.player)) {
            event.setCanceled(true);
        }
    }
}
