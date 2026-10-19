package fr.xopi.mushocraft.client.screen;

import fr.xopi.mushocraft.client.ClientMoveState;
import fr.xopi.mushocraft.combat.move.Move;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.network.SetMoveSlotPayload;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Puts known moves into the nine slots of the move bar: click a move, then a slot. Right-click empties a slot.
 */
public class MovesScreen extends Screen {
    private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final int ROW_HEIGHT = 20;
    private static final int LIST_WIDTH = 180;
    private static final int SELECTED_COLOR = 0x6040A0FF;
    private static final int HOVER_COLOR = 0x30FFFFFF;

    private Optional<MoveRef> selected = Optional.empty();

    public MovesScreen() {
        super(Component.translatable("screen.mushocraft.moves"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int listLeft() {
        return width / 2 - LIST_WIDTH / 2;
    }

    private int listTop() {
        return 36;
    }

    private int barLeft() {
        return width / 2 - 91;
    }

    private int barTop() {
        return height - 40;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        List<MoveRef> known = ClientMoveState.known();
        if (known.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("screen.mushocraft.moves.none"), width / 2, listTop() + 10, 0xA0A0A0);
        }
        Optional<Move> hovered = Optional.empty();
        for (int i = 0; i < known.size(); i++) {
            MoveRef ref = known.get(i);
            int y = listTop() + i * ROW_HEIGHT;
            boolean isHovered = inside(mouseX, mouseY, listLeft(), y, LIST_WIDTH, ROW_HEIGHT);
            if (selected.equals(Optional.of(ref))) {
                graphics.fill(listLeft(), y, listLeft() + LIST_WIDTH, y + ROW_HEIGHT, SELECTED_COLOR);
            } else if (isHovered) {
                graphics.fill(listLeft(), y, listLeft() + LIST_WIDTH, y + ROW_HEIGHT, HOVER_COLOR);
            }
            Optional<Move> move = ref.resolve();
            if (move.isPresent()) {
                graphics.blit(move.get().icon(), listLeft() + 2, y + 2, 0, 0, 16, 16, 16, 16);
                graphics.drawString(font, move.get().displayName(), listLeft() + 22, y + 6, 0xFFFFFF);
                if (isHovered) {
                    hovered = move;
                }
            }
        }

        graphics.blitSprite(HOTBAR, barLeft(), barTop(), 182, 22);
        MoveLoadout loadout = ClientMoveState.loadout();
        for (int slot = 0; slot < MoveLoadout.SIZE; slot++) {
            int x = barLeft() + 3 + slot * 20;
            int y = barTop() + 3;
            Optional<Move> move = loadout.get(slot).flatMap(MoveRef::resolve);
            move.ifPresent(m -> graphics.blit(m.icon(), x, y, 0, 0, 16, 16, 16, 16));
            graphics.drawString(font, String.valueOf(slot + 1), x + 11, y + 9, 0xE0E0E0, true);
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                graphics.fill(x, y, x + 16, y + 16, HOVER_COLOR);
                if (move.isPresent()) {
                    hovered = move;
                }
            }
        }
        graphics.drawCenteredString(font, Component.translatable("screen.mushocraft.moves.help"), width / 2, height - 14, 0x909090);
        hovered.ifPresent(move -> graphics.renderComponentTooltip(font, tooltip(move), mouseX, mouseY));
    }

    private static List<Component> tooltip(Move move) {
        List<Component> lines = new ArrayList<>();
        lines.add(move.displayName());
        if (move instanceof Spell spell) {
            lines.add(Component.translatable("screen.mushocraft.moves.spell_line",
                    spell.school().displayName(), spell.rank().displayName()).withStyle(style -> style.withColor(0x7FB8FF)));
            lines.add(Component.translatable("screen.mushocraft.moves.cost",
                    String.format(Locale.ROOT, "%.0f", spell.manaCost()),
                    String.format(Locale.ROOT, "%.1f", spell.cooldownTicks() / 20.0)).withStyle(style -> style.withColor(0xA0A0A0)));
            lines.add(spell.description().copy().withStyle(style -> style.withColor(0xC0C0C0).withItalic(true)));
        } else if (move instanceof Technique technique) {
            lines.add(Component.translatable("screen.mushocraft.moves.spell_line",
                    technique.style().displayName(), technique.rank().displayName()).withStyle(style -> style.withColor(0xFFB070)));
            lines.add(Component.translatable("screen.mushocraft.moves.technique_cooldown",
                    String.format(Locale.ROOT, "%.1f", technique.cooldownTicks() / 20.0)).withStyle(style -> style.withColor(0xA0A0A0)));
            lines.add(technique.description().copy().withStyle(style -> style.withColor(0xC0C0C0).withItalic(true)));
        }
        return lines;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<MoveRef> known = ClientMoveState.known();
        for (int i = 0; i < known.size(); i++) {
            if (inside(mouseX, mouseY, listLeft(), listTop() + i * ROW_HEIGHT, LIST_WIDTH, ROW_HEIGHT)) {
                selected = Optional.of(known.get(i));
                return true;
            }
        }
        for (int slot = 0; slot < MoveLoadout.SIZE; slot++) {
            if (inside(mouseX, mouseY, barLeft() + 3 + slot * 20, barTop() + 3, 16, 16)) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    PacketDistributor.sendToServer(new SetMoveSlotPayload(slot, Optional.empty()));
                } else if (selected.isPresent()) {
                    PacketDistributor.sendToServer(new SetMoveSlotPayload(slot, selected));
                    selected = Optional.empty();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
