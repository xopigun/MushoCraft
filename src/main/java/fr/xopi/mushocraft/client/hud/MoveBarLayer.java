package fr.xopi.mushocraft.client.hud;

import fr.xopi.mushocraft.client.ClientMoveState;
import fr.xopi.mushocraft.combat.move.Move;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.compat.epicfight.EpicFightModes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * The move bar: in battle mode it takes the place of the hotbar, same frame and same nine slots,
 * each showing a spell or technique and its cooldown the way vanilla shows item cooldowns.
 * The held weapon is shown in the offhand slot position, since the hotbar is hidden.
 */
public final class MoveBarLayer implements LayeredDraw.Layer {
    private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation HELD_SLOT = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final int COOLDOWN_COLOR = 0x7FFFFFFF;
    private static final int KEY_LABEL_COLOR = 0xFFE0E0E0;
    private static final int MASTERY_BACKGROUND = 0xFF000000;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator() || !EpicFightModes.isBattleMode(player)) {
            return;
        }
        int left = graphics.guiWidth() / 2 - 91;
        int top = graphics.guiHeight() - 22;
        graphics.blitSprite(HOTBAR, left, top, 182, 22);

        MoveLoadout loadout = ClientMoveState.loadout();
        for (int slot = 0; slot < MoveLoadout.SIZE; slot++) {
            int x = left + 3 + slot * 20;
            int y = top + 3;
            Optional<Move> move = loadout.get(slot).flatMap(MoveRef::resolve);
            move.ifPresent(m -> drawMove(graphics, m, x, y));
            graphics.drawString(minecraft.font, String.valueOf(slot + 1), x + 11, y + 9, KEY_LABEL_COLOR, true);
        }
        drawHeldItem(graphics, minecraft, player, left, graphics.guiHeight());
    }

    private static void drawMove(GuiGraphics graphics, Move move, int x, int y) {
        graphics.blit(move.icon(), x, y, 0, 0, 16, 16, 16, 16);
        drawMastery(graphics, move, x, y);
        float fraction = ClientMoveState.cooldownFraction(move.cooldownKey());
        if (fraction > 0.0F) {
            int height = (int) Math.ceil(16 * fraction);
            graphics.fill(x, y + 16 - height, x + 16, y + 16, COOLDOWN_COLOR);
        }
    }

    /** A thin gauge under a move not yet mastered, filling as it is repeated (like an item's durability bar). */
    private static void drawMastery(GuiGraphics graphics, Move move, int x, int y) {
        double progress = move.masteryCurve().progress(ClientMoveState.repetitions(move.ref()));
        if (progress >= 1.0) {
            return;
        }
        int width = (int) Math.round(13 * progress);
        graphics.fill(x + 2, y + 13, x + 15, y + 15, MASTERY_BACKGROUND);
        graphics.fill(x + 2, y + 13, x + 2 + width, y + 14, Mth.hsvToRgb((float) (progress / 3.0), 1.0F, 1.0F) | 0xFF000000);
    }

    private static void drawHeldItem(GuiGraphics graphics, Minecraft minecraft, LocalPlayer player, int barLeft, int screenHeight) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        int x = barLeft - 29;
        int y = screenHeight - 23;
        graphics.blitSprite(HELD_SLOT, x, y, 29, 24);
        graphics.renderItem(held, x + 3, y + 4);
        graphics.renderItemDecorations(minecraft.font, held, x + 3, y + 4);
    }
}
