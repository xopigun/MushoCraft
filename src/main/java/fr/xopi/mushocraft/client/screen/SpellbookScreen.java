package fr.xopi.mushocraft.client.screen;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.ClientMoveState;
import fr.xopi.mushocraft.combat.move.Move;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.network.SetMoveSlotPayload;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The spellbook: a leather-bound book whose ribbons open a school of magic (left) or a sword style (right). Its pages
 * list every move of the category under the title of its rank; moves not learned yet are shown greyed and locked.
 * Drag a learned move onto a slot of the move bar below (or click it, then the slot); right-click a slot to empty it.
 */
public class SpellbookScreen extends Screen {
    private static final ResourceLocation TEXTURE = MushoCraft.id("textures/gui/spellbook.png");
    private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final int TEX = 512;
    private static final int TEX_H = 256;
    private static final int BOOK_W = 300;
    private static final int BOOK_H = 190;
    private static final int PAGE_W = 112;
    private static final int PAGE_TOP = 34;
    private static final int PAGE_BOTTOM = 166;
    private static final int RANK_HEIGHT = 13;
    private static final int ENTRY_HEIGHT = 21;
    private static final int RIBBON_STEP = 20;
    private static final int INK = 0xFF3B2A1A;
    private static final int INK_FADED = 0xFF9A8A70;
    private static final int RANK_INK = 0xFF8A5A20;
    private static final int HOVER = 0x30FFFFFF;

    /** A line of a page: a rank title or a move. */
    private record Line(Optional<Rank> rankTitle, Optional<SpellbookCategory.Entry> entry) {
        int height() {
            return rankTitle.isPresent() ? RANK_HEIGHT : ENTRY_HEIGHT;
        }
    }

    /** Two facing pages. */
    private record Spread(List<Line> left, List<Line> right) {
    }

    private final List<SpellbookCategory> categories = SpellbookCategory.all();
    private int category;
    private int spread;
    private Optional<MoveRef> selected = Optional.empty();
    private boolean dragging;

    public SpellbookScreen() {
        super(Component.translatable("screen.mushocraft.spellbook"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int bookLeft() {
        return (width - BOOK_W) / 2;
    }

    private int bookTop() {
        // Room above for the ribbons (17 px), below for the move bar (28 px).
        return Math.max(19, (height - BOOK_H - 45) / 2 + 17);
    }

    private int barLeft() {
        return width / 2 - 91;
    }

    private int barTop() {
        return bookTop() + BOOK_H + 6;
    }

    // ---- layout ----

    private List<Spread> spreads() {
        List<Line> lines = new ArrayList<>();
        if (!categories.isEmpty()) {
            Optional<Rank> current = Optional.empty();
            for (SpellbookCategory.Entry entry : categories.get(category).entries()) {
                if (current.isEmpty() || current.get() != entry.rank()) {
                    current = Optional.of(entry.rank());
                    lines.add(new Line(current, Optional.empty()));
                }
                lines.add(new Line(Optional.empty(), Optional.of(entry)));
            }
        }
        List<List<Line>> pages = new ArrayList<>();
        List<Line> page = new ArrayList<>();
        int used = 0;
        for (Line line : lines) {
            // a rank title is never left alone at the bottom of a page
            int needed = line.height() + (line.rankTitle().isPresent() ? ENTRY_HEIGHT : 0);
            if (used + needed > PAGE_BOTTOM - PAGE_TOP && !page.isEmpty()) {
                pages.add(page);
                page = new ArrayList<>();
                used = 0;
            }
            page.add(line);
            used += line.height();
        }
        pages.add(page);
        List<Spread> result = new ArrayList<>();
        for (int i = 0; i < pages.size(); i += 2) {
            result.add(new Spread(pages.get(i), i + 1 < pages.size() ? pages.get(i + 1) : List.of()));
        }
        return result;
    }

    private int pageLeft(boolean rightPage) {
        return bookLeft() + (rightPage ? 162 : 26);
    }

    // ---- rendering ----

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = bookLeft();
        int top = bookTop();
        renderRibbons(graphics, mouseX, mouseY, false);
        graphics.blit(TEXTURE, left, top, 0, 0, BOOK_W, BOOK_H, TEX, TEX_H);
        renderRibbons(graphics, mouseX, mouseY, true);

        Optional<SpellbookCategory.Entry> hovered = Optional.empty();
        List<Spread> spreads = spreads();
        spread = Mth.clamp(spread, 0, spreads.size() - 1);
        if (!categories.isEmpty()) {
            SpellbookCategory current = categories.get(category);
            drawCentered(graphics, current.title(), left + 85, top + 19, INK);
            drawCentered(graphics, Component.translatable("screen.mushocraft.spellbook.page", spread + 1, spreads.size()),
                    left + 215, top + 19, INK_FADED);
            Spread pages = spreads.get(spread);
            hovered = renderPage(graphics, pages.left(), false, mouseX, mouseY).or(() -> renderPage(graphics, pages.right(), true, mouseX, mouseY));
            renderArrows(graphics, mouseX, mouseY, spreads.size());
        }
        Optional<Move> hoveredSlot = renderBar(graphics, mouseX, mouseY);
        Component help = Component.translatable("screen.mushocraft.spellbook.help");
        if (barTop() + 36 <= height && font.width(help) <= width - 8) {
            graphics.drawCenteredString(font, help, width / 2, barTop() + 26, 0xA0A0A0);
        }

        if (dragging && selected.isPresent()) {
            selected.flatMap(MoveRef::resolve).ifPresent(move -> graphics.blit(move.icon(), mouseX - 8, mouseY - 8, 0, 0, 16, 16, 16, 16));
        } else if (hovered.isPresent()) {
            graphics.renderComponentTooltip(font, entryTooltip(hovered.get()), mouseX, mouseY);
        } else {
            hoveredSlot.ifPresent(move -> graphics.renderComponentTooltip(font, MoveTooltips.of(move), mouseX, mouseY));
        }
    }

    private void renderRibbons(GuiGraphics graphics, int mouseX, int mouseY, boolean activeOnly) {
        for (int i = 0; i < categories.size(); i++) {
            boolean active = i == category;
            if (active != activeOnly) {
                continue;
            }
            int x = ribbonX(i);
            int y = bookTop() - (active ? 17 : 13);
            graphics.blit(TEXTURE, x, y, active ? 22 : 0, 192, 22, 20, TEX, TEX_H);
            graphics.pose().pushPose();
            graphics.pose().translate(x + 6, y + 3, 0);
            graphics.pose().scale(0.625F, 0.625F, 1.0F);
            graphics.blit(categories.get(i).icon(), 0, 0, 0, 0, 16, 16, 16, 16);
            graphics.pose().popPose();
            if (!active && inside(mouseX, mouseY, x + 3, y, 16, 13)) {
                graphics.fill(x + 3, y, x + 19, y + 13, HOVER);
            }
        }
    }

    /** Schools of magic hang from the left half of the book, sword styles from the right half. */
    private int ribbonX(int index) {
        int swordStart = (int) categories.stream().takeWhile(c -> !isSwordCategory(c)).count();
        if (index < swordStart) {
            return bookLeft() + 16 + index * RIBBON_STEP;
        }
        return bookLeft() + BOOK_W - 38 - (categories.size() - 1 - index) * RIBBON_STEP;
    }

    private static boolean isSwordCategory(SpellbookCategory category) {
        return category.entries().getFirst().move() instanceof Technique;
    }

    private Optional<SpellbookCategory.Entry> renderPage(GuiGraphics graphics, List<Line> lines, boolean rightPage, int mouseX, int mouseY) {
        Optional<SpellbookCategory.Entry> hovered = Optional.empty();
        int x = pageLeft(rightPage);
        int y = bookTop() + PAGE_TOP;
        for (Line line : lines) {
            if (line.rankTitle().isPresent()) {
                Component title = line.rankTitle().get().displayName().copy().withStyle(style -> style.withItalic(true));
                graphics.drawString(font, title, x, y + 2, RANK_INK, false);
                int after = x + font.width(title) + 4;
                graphics.fill(after, y + 6, x + PAGE_W, y + 7, 0x60A07840);
            } else {
                SpellbookCategory.Entry entry = line.entry().orElseThrow();
                renderEntry(graphics, entry, x, y);
                if (inside(mouseX, mouseY, x, y, PAGE_W, 19)) {
                    graphics.fill(x, y, x + PAGE_W, y + 19, HOVER);
                    hovered = Optional.of(entry);
                }
            }
            y += line.height();
        }
        return hovered;
    }

    private void renderEntry(GuiGraphics graphics, SpellbookCategory.Entry entry, int x, int y) {
        Move move = entry.move();
        boolean known = isKnown(move);
        boolean isSelected = selected.equals(Optional.of(move.ref()));
        graphics.blit(TEXTURE, x, y, isSelected ? 134 : 116, 192, 18, 18, TEX, TEX_H);
        graphics.blit(move.icon(), x + 1, y + 1, 0, 0, 16, 16, 16, 16);
        if (!known) {
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xB0E2D4B0);
            graphics.blit(TEXTURE, x + 5, y + 4, 152, 191, 8, 11, TEX, TEX_H);
        }
        List<FormattedCharSequence> name = font.split(move.displayName(), PAGE_W - 24);
        graphics.drawString(font, name.getFirst(), x + 22, y + (known ? 2 : 5), known ? INK : INK_FADED, false);
        if (known) {
            double progress = move.masteryCurve().progress(ClientMoveState.repetitions(move.ref()));
            int barWidth = PAGE_W - 24;
            graphics.fill(x + 22, y + 13, x + 22 + barWidth, y + 15, 0x40000000);
            int color = progress >= 1.0 ? 0xFFD4A84A : 0xFF4E8AC8;
            graphics.fill(x + 22, y + 13, x + 22 + (int) Math.round(barWidth * progress), y + 15, color);
        }
    }

    private void renderArrows(GuiGraphics graphics, int mouseX, int mouseY, int spreadCount) {
        if (spread > 0) {
            int x = bookLeft() + 24;
            int y = bookTop() + BOOK_H - 26;
            boolean hover = inside(mouseX, mouseY, x, y, 18, 10);
            graphics.blit(TEXTURE, x, y, hover ? 98 : 80, 192, 18, 10, TEX, TEX_H);
        }
        if (spread < spreadCount - 1) {
            int x = bookLeft() + BOOK_W - 42;
            int y = bookTop() + BOOK_H - 26;
            boolean hover = inside(mouseX, mouseY, x, y, 18, 10);
            graphics.blit(TEXTURE, x, y, hover ? 62 : 44, 192, 18, 10, TEX, TEX_H);
        }
    }

    private Optional<Move> renderBar(GuiGraphics graphics, int mouseX, int mouseY) {
        Optional<Move> hovered = Optional.empty();
        graphics.blitSprite(HOTBAR, barLeft(), barTop(), 182, 22);
        MoveLoadout loadout = ClientMoveState.loadout();
        for (int slot = 0; slot < MoveLoadout.SIZE; slot++) {
            int x = barLeft() + 3 + slot * 20;
            int y = barTop() + 3;
            Optional<Move> move = loadout.get(slot).flatMap(MoveRef::resolve);
            move.ifPresent(m -> graphics.blit(m.icon(), x, y, 0, 0, 16, 16, 16, 16));
            graphics.drawString(font, String.valueOf(slot + 1), x + 11, y + 9, 0xE0E0E0, true);
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                graphics.fill(x, y, x + 16, y + 16, HOVER);
                if (move.isPresent()) {
                    hovered = move;
                }
            }
        }
        return hovered;
    }

    private List<Component> entryTooltip(SpellbookCategory.Entry entry) {
        if (isKnown(entry.move())) {
            return MoveTooltips.of(entry.move());
        }
        return List.of(entry.move().displayName().copy().withStyle(style -> style.withColor(0xB0B0B0)),
                Component.translatable("screen.mushocraft.spellbook.locked", entry.rank().displayName())
                        .withStyle(style -> style.withColor(0x909090).withItalic(true)));
    }

    private void drawCentered(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x - font.width(text) / 2, y, color, false);
    }

    private static boolean isKnown(Move move) {
        return ClientMoveState.known().contains(move.ref());
    }

    // ---- input ----

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < categories.size(); i++) {
            int x = ribbonX(i);
            int y = bookTop() - (i == category ? 17 : 13);
            if (inside(mouseX, mouseY, x + 3, y, 16, 14) && i != category) {
                category = i;
                spread = 0;
                pageTurn();
                return true;
            }
        }
        if (clickedArrow(mouseX, mouseY)) {
            return true;
        }
        Optional<SpellbookCategory.Entry> entry = entryAt(mouseX, mouseY);
        if (entry.isPresent() && isKnown(entry.get().move())) {
            selected = Optional.of(entry.get().move().ref());
            dragging = true;
            return true;
        }
        int slot = slotAt(mouseX, mouseY);
        if (slot >= 0) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                PacketDistributor.sendToServer(new SetMoveSlotPayload(slot, Optional.empty()));
            } else if (selected.isPresent()) {
                PacketDistributor.sendToServer(new SetMoveSlotPayload(slot, selected));
                selected = Optional.empty();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            int slot = slotAt(mouseX, mouseY);
            if (slot >= 0 && selected.isPresent()) {
                PacketDistributor.sendToServer(new SetMoveSlotPayload(slot, selected));
                selected = Optional.empty();
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int count = spreads().size();
        int next = Mth.clamp(spread + (scrollY < 0 ? 1 : -1), 0, count - 1);
        if (next != spread) {
            spread = next;
            pageTurn();
        }
        return true;
    }

    private boolean clickedArrow(double mouseX, double mouseY) {
        int y = bookTop() + BOOK_H - 26;
        if (spread > 0 && inside(mouseX, mouseY, bookLeft() + 24, y, 18, 10)) {
            spread--;
            pageTurn();
            return true;
        }
        if (spread < spreads().size() - 1 && inside(mouseX, mouseY, bookLeft() + BOOK_W - 42, y, 18, 10)) {
            spread++;
            pageTurn();
            return true;
        }
        return false;
    }

    private Optional<SpellbookCategory.Entry> entryAt(double mouseX, double mouseY) {
        if (categories.isEmpty()) {
            return Optional.empty();
        }
        Spread pages = spreads().get(Mth.clamp(spread, 0, spreads().size() - 1));
        for (boolean rightPage : new boolean[]{false, true}) {
            int x = pageLeft(rightPage);
            int y = bookTop() + PAGE_TOP;
            for (Line line : rightPage ? pages.right() : pages.left()) {
                if (line.entry().isPresent() && inside(mouseX, mouseY, x, y, PAGE_W, 19)) {
                    return line.entry();
                }
                y += line.height();
            }
        }
        return Optional.empty();
    }

    private int slotAt(double mouseX, double mouseY) {
        for (int slot = 0; slot < MoveLoadout.SIZE; slot++) {
            if (inside(mouseX, mouseY, barLeft() + 3 + slot * 20, barTop() + 3, 16, 16)) {
                return slot;
            }
        }
        return -1;
    }

    private void pageTurn() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
