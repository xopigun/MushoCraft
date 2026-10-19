package fr.xopi.mushocraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.xopi.mushocraft.talent.Talent;
import fr.xopi.mushocraft.talent.TalentService;
import fr.xopi.mushocraft.talent.Talents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * {@code /mushocraft talents show [player]}, {@code reroll <players>}, {@code set <players> <talent> <value>}.
 */
public final class TalentCommand implements MushoSubcommand {
    private static final double MAX_VALUE = 10.0;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("talents")
                .then(Commands.literal("show")
                        .executes(context -> show(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> show(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("reroll")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(this::reroll)))
                .then(buildSet());
    }

    /** One literal per talent keeps the syntax checked and auto-completed by the client. */
    private LiteralArgumentBuilder<CommandSourceStack> buildSet() {
        var players = Commands.argument("players", EntityArgument.players());
        for (Talent talent : Talent.values()) {
            players.then(Commands.literal(talent.getSerializedName())
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, MAX_VALUE))
                            .executes(context -> set(context, talent))));
        }
        return Commands.literal("set").then(players);
    }

    private int show(CommandSourceStack source, ServerPlayer player) {
        Talents talents = TalentService.talents(player).orElse(null);
        if (talents == null) {
            source.sendFailure(Component.translatable("command.mushocraft.talents.none", player.getDisplayName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.mushocraft.talents.header", player.getDisplayName()), false);
        for (Talent talent : Talent.values()) {
            source.sendSuccess(() -> Component.translatable("command.mushocraft.talents.line",
                    talent.displayName(), MushoCommands.number(talents.get(talent))), false);
        }
        return 1;
    }

    private int reroll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(TalentService::roll);
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.talents.reroll",
                players.size()), true);
        return players.size();
    }

    private int set(CommandContext<CommandSourceStack> context, Talent talent) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        double value = DoubleArgumentType.getDouble(context, "value");
        players.forEach(player -> TalentService.set(player, talent, value));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.talents.set",
                talent.displayName(), MushoCommands.number(value), players.size()), true);
        return players.size();
    }
}
