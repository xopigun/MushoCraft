package fr.xopi.mushocraft.client.input;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.screen.MovesScreen;
import fr.xopi.mushocraft.compat.epicfight.EpicFightModes;
import fr.xopi.mushocraft.network.UseMovePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * In battle mode, the hotbar keys 1-9 trigger the move bar instead of selecting items. Clicks are taken at the
 * start of the client tick, before vanilla (item selection) and Epic Fight (which drains them after attacks) see them.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class MoveKeyInput {
    private MoveKeyInput() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (MushoKeyMappings.OPEN_MOVES.consumeClick()) {
            minecraft.setScreen(new MovesScreen());
        }
        if (!EpicFightModes.isBattleMode(minecraft.player)) {
            return;
        }
        KeyMapping[] slots = minecraft.options.keyHotbarSlots;
        for (int slot = 0; slot < slots.length; slot++) {
            while (slots[slot].consumeClick()) {
                PacketDistributor.sendToServer(new UseMovePayload(slot));
            }
        }
    }
}
