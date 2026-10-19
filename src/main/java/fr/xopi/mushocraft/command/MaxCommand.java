package fr.xopi.mushocraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

/**
 * {@code /mushocraft max [players]}: as strong as possible in every discipline (see {@link PlayerMaximizer}).
 */
public final class MaxCommand implements MushoSubcommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("max")
                .executes(context -> maxOut(context.getSource(), List.of(context.getSource().getPlayerOrException())))
                .then(Commands.argument("players", EntityArgument.players())
                        .executes(this::maxOutTargets));
    }

    private int maxOutTargets(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return maxOut(context.getSource(), EntityArgument.getPlayers(context, "players"));
    }

    private int maxOut(CommandSourceStack source, Collection<ServerPlayer> players) {
        players.forEach(PlayerMaximizer::maxOut);
        source.sendSuccess(() -> Component.translatable("command.mushocraft.max", players.size()), true);
        return players.size();
    }
}
