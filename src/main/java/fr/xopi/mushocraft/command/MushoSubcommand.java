package fr.xopi.mushocraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

/**
 * One branch of the {@code /mushocraft} command tree.
 */
public interface MushoSubcommand {
    LiteralArgumentBuilder<CommandSourceStack> build();
}
