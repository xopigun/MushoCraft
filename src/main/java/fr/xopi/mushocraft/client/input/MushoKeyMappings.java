package fr.xopi.mushocraft.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Every key of the mod. Defaults avoid Epic Fight's (Alt dodge, R mode, G lock-on, K skills, Y emotes).
 */
public final class MushoKeyMappings {
    public static final String CATEGORY = "key.categories.mushocraft";

    /** Held to pour mana out and train the reservoir. */
    public static final KeyMapping CIRCULATE_MANA = new KeyMapping(
            "key.mushocraft.circulate_mana",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY);

    /** Opens the screen where known moves are put into the move bar. */
    public static final KeyMapping OPEN_MOVES = new KeyMapping(
            "key.mushocraft.open_moves",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            CATEGORY);

    private MushoKeyMappings() {
    }
}
