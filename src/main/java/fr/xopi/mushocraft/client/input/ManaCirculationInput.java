package fr.xopi.mushocraft.client.input;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.network.ManaCirculationPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Tells the server when the circulation key starts or stops being held. Only edges are sent, not every tick.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class ManaCirculationInput {
    private static boolean wasHeld;

    private ManaCirculationInput() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean held = minecraft.player != null && minecraft.screen == null && MushoKeyMappings.CIRCULATE_MANA.isDown();
        if (held != wasHeld && minecraft.getConnection() != null) {
            PacketDistributor.sendToServer(new ManaCirculationPayload(held));
        }
        wasHeld = held;
    }
}
