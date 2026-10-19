package fr.xopi.mushocraft.client.hud;

import fr.xopi.mushocraft.client.ClientAuraState;
import fr.xopi.mushocraft.client.ClientManaState;
import fr.xopi.mushocraft.magic.mana.ManaSnapshot;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

/**
 * Thin mana gauge drawn above the food bar, same width as it. Hidden whenever the food bar is
 * (creative, spectator, hidden HUD).
 */
public final class ManaBarLayer implements LayeredDraw.Layer {
    private static final int WIDTH = 81;
    private static final int HEIGHT = 5;
    private static final int SPACING = 2;

    private static final int BORDER_COLOR = 0xFF0B1A33;
    private static final int EMPTY_COLOR = 0xFF1C2A40;
    private static final int FILL_COLOR = 0xFF3D8BFF;
    private static final int SHINE_COLOR = 0xFF9CC8FF;
    /** The gauge is framed in gold while the Battle Aura burns mana. */
    private static final int AURA_BORDER_COLOR = 0xFFF2D36B;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) {
            return;
        }
        ManaSnapshot mana = ClientManaState.get();
        if (mana.capacity() <= 0.0) {
            return;
        }
        int right = graphics.guiWidth() / 2 + 91;
        int left = right - WIDTH;
        int top = graphics.guiHeight() - minecraft.gui.rightHeight - HEIGHT + 9 - SPACING;
        minecraft.gui.rightHeight += HEIGHT + SPACING;

        int border = ClientAuraState.isUp() ? AURA_BORDER_COLOR : BORDER_COLOR;
        graphics.fill(left - 1, top - 1, right + 1, top + HEIGHT + 1, border);
        graphics.fill(left, top, right, top + HEIGHT, EMPTY_COLOR);
        int filled = (int) Math.round(WIDTH * mana.fill());
        if (filled > 0) {
            graphics.fill(right - filled, top, right, top + HEIGHT, FILL_COLOR);
            graphics.fill(right - filled, top, right, top + 1, SHINE_COLOR);
        }
    }
}
