package fr.xopi.mushocraft.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.screen.SpellbookScreen;
import fr.xopi.mushocraft.compat.epicfight.EpicFightModes;
import fr.xopi.mushocraft.network.ReleaseMovePayload;
import fr.xopi.mushocraft.network.ToggleAuraPayload;
import fr.xopi.mushocraft.network.UseMovePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/**
 * In battle mode, the hotbar keys 1-9 trigger the move bar instead of selecting items. Clicks are taken at the
 * start of the client tick, before vanilla (item selection) and Epic Fight (which drains them after attacks) see them.
 * A spell is chanted only while its key is held: letting it go tells the server to abandon the chant. Only a fresh press
 * starts a move: the repeats the keyboard sends while a key is held down are ignored, so holding the key never starts
 * the move again once it is done.
 * <p>
 * Whether a key is held is read from the keyboard itself, not from Minecraft's key state: Epic Fight marks the hotbar
 * keys as released whenever the player is busy with an action, which would otherwise abandon every chant at once.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class MoveKeyInput {
    /** The slot whose key started the move in progress, -1 if none. */
    private static int heldSlot = -1;
    /** Whether each hotbar key was down at the previous tick. */
    private static final boolean[] DOWN_LAST_TICK = new boolean[9];

    private MoveKeyInput() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (MushoKeyMappings.OPEN_MOVES.consumeClick()) {
            minecraft.setScreen(new SpellbookScreen());
        }
        while (MushoKeyMappings.TOGGLE_AURA.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleAuraPayload());
        }
        if (!EpicFightModes.isBattleMode(minecraft.player)) {
            Arrays.fill(DOWN_LAST_TICK, false);
            return;
        }
        KeyMapping[] slots = minecraft.options.keyHotbarSlots;
        if (heldSlot >= 0 && !physicallyDown(minecraft, slots[heldSlot])) {
            PacketDistributor.sendToServer(new ReleaseMovePayload(heldSlot));
            heldSlot = -1;
        }
        for (int slot = 0; slot < slots.length; slot++) {
            boolean clicked = false;
            while (slots[slot].consumeClick()) {
                clicked = true;
            }
            boolean down = physicallyDown(minecraft, slots[slot]);
            // a press since last tick: the key went down, or a tap too quick to be seen held
            boolean freshPress = (down || clicked) && !DOWN_LAST_TICK[slot];
            DOWN_LAST_TICK[slot] = down;
            if (freshPress) {
                PacketDistributor.sendToServer(new UseMovePayload(slot));
                heldSlot = slot;
            }
        }
    }

    /** Whether the key bound to this mapping is held down on the keyboard or mouse right now. */
    private static boolean physicallyDown(Minecraft minecraft, KeyMapping mapping) {
        InputConstants.Key key = mapping.getKey();
        long window = minecraft.getWindow().getWindow();
        return switch (key.getType()) {
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            case SCANCODE -> mapping.isDown();
        };
    }
}
