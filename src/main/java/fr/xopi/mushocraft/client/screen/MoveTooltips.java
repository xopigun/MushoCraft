package fr.xopi.mushocraft.client.screen;

import fr.xopi.mushocraft.client.ClientMoveState;
import fr.xopi.mushocraft.combat.mastery.MasteryRules;
import fr.xopi.mushocraft.combat.move.Move;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.magic.spell.IncantationRules;
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
 * The tooltip of a spell or technique: school or style and rank, cost, description, incantation and mastery.
 */
public final class MoveTooltips {
    private MoveTooltips() {
    }

    public static List<Component> of(Move move) {
        List<Component> lines = new ArrayList<>();
        lines.add(move.displayName());
        if (move instanceof Spell spell) {
            lines.add(Component.translatable("screen.mushocraft.moves.spell_line",
                    spell.school().displayName(), spell.rank().displayName()).withStyle(style -> style.withColor(0x7FB8FF)));
            lines.add(Component.translatable("screen.mushocraft.moves.cost",
                    String.format(Locale.ROOT, "%.0f", spell.manaCost()),
                    String.format(Locale.ROOT, "%.1f", spell.cooldownTicks() / 20.0)).withStyle(style -> style.withColor(0xA0A0A0)));
            lines.add(spell.description().copy().withStyle(style -> style.withColor(0xC0C0C0).withItalic(true)));
            lines.add(Component.translatable("screen.mushocraft.moves.incantation", IncantationRules.mode(
                    ClientMoveState.repetitions(move.ref()), ClientMoveState.manaControl()).displayName())
                    .withStyle(style -> style.withColor(0x9FD8FF)));
        } else if (move instanceof Technique technique) {
            lines.add(Component.translatable("screen.mushocraft.moves.spell_line",
                    technique.style().displayName(), technique.rank().displayName()).withStyle(style -> style.withColor(0xFFB070)));
            lines.add(Component.translatable("screen.mushocraft.moves.technique_cooldown",
                    String.format(Locale.ROOT, "%.1f", technique.cooldownTicks() / 20.0)).withStyle(style -> style.withColor(0xA0A0A0)));
            lines.add(technique.description().copy().withStyle(style -> style.withColor(0xC0C0C0).withItalic(true)));
        }
        lines.add(masteryLine(move));
        return lines;
    }

    private static Component masteryLine(Move move) {
        float repetitions = ClientMoveState.repetitions(move.ref());
        if (MasteryRules.isMastered(repetitions)) {
            return Component.translatable("screen.mushocraft.moves.mastered").withStyle(style -> style.withColor(0x80FF80));
        }
        return Component.translatable("screen.mushocraft.moves.mastery", (int) Math.floor(repetitions),
                (int) MasteryRules.REPETITIONS_TO_MASTER,
                String.format(Locale.ROOT, "%.0f", MasteryRules.failureChance(repetitions) * 100.0))
                .withStyle(style -> style.withColor(0xFFD060));
    }
}
