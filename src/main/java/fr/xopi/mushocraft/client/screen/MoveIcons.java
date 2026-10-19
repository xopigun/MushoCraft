package fr.xopi.mushocraft.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Draws the icon of a move (spell or technique). Icons are painted at 64 px and shown 16 GUI pixels wide: they are
 * sampled smoothly, so they stay clean whatever the GUI scale, instead of losing every other pixel.
 */
public final class MoveIcons {
    public static final int SIZE = 16;
    private static final Set<ResourceLocation> SMOOTHED = new HashSet<>();

    private MoveIcons() {
    }

    public static void draw(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        if (SMOOTHED.add(icon)) {
            Minecraft.getInstance().getTextureManager().getTexture(icon).setFilter(true, false);
        }
        graphics.blit(icon, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
    }

    /** Forgets which icons were smoothed: a resource reload makes their textures anew. */
    public static void reset() {
        SMOOTHED.clear();
    }
}
