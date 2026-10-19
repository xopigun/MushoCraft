package fr.xopi.mushocraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.mana.ManaSnapshot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * {@code /mushocraft mana info [player]}, {@code fill <players>}, {@code set <players> <value>},
 * {@code trained <players> <value>}.
 */
public final class ManaCommand implements MushoSubcommand {
    private static final double MAX_VALUE = 1.0E7;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("mana")
                .then(Commands.literal("info")
                        .executes(context -> info(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> info(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("fill")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(this::fill)))
                .then(Commands.literal("set")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, MAX_VALUE))
                                        .executes(context -> apply(context, "set", ManaService::setCurrent)))))
                .then(Commands.literal("trained")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, MAX_VALUE))
                                        .executes(context -> apply(context, "trained", ManaService::setTrained)))));
    }

    private int info(CommandSourceStack source, ServerPlayer player) {
        ManaSnapshot mana = ManaService.snapshot(player);
        double trained = player.getData(MushoAttachments.MANA).trained();
        source.sendSuccess(() -> Component.translatable("command.mushocraft.mana.info",
                player.getDisplayName(),
                MushoCommands.number(mana.current()),
                MushoCommands.number(mana.capacity()),
                MushoCommands.number(trained),
                MushoCommands.number(mana.ceiling())), false);
        return 1;
    }

    private int fill(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(ManaService::refill);
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.mana.fill", players.size()), true);
        return players.size();
    }

    private int apply(CommandContext<CommandSourceStack> context, String key, BiConsumer<ServerPlayer, Double> action)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        double value = DoubleArgumentType.getDouble(context, "value");
        players.forEach(player -> action.accept(player, value));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.mana." + key,
                players.size(), MushoCommands.number(value)), true);
        return players.size();
    }
}
