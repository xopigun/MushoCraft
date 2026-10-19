package fr.xopi.mushocraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.xopi.mushocraft.life.LifeData;
import fr.xopi.mushocraft.life.LifeService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * {@code /mushocraft life info [player]}, {@code setage <players> <years>}, {@code rebirth <players>}.
 */
public final class LifeCommand implements MushoSubcommand {
    private static final double MAX_AGE = 200.0;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("life")
                .then(Commands.literal("info")
                        .executes(context -> info(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> info(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("setage")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("years", DoubleArgumentType.doubleArg(0.0, MAX_AGE))
                                        .executes(this::setAge))))
                .then(Commands.literal("rebirth")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(this::rebirth)));
    }

    private int info(CommandSourceStack source, ServerPlayer player) {
        if (!LifeService.isBorn(player)) {
            source.sendFailure(Component.translatable("command.mushocraft.life.unborn", player.getDisplayName()));
            return 0;
        }
        LifeData data = LifeService.lifeData(player).orElseThrow();
        double age = LifeService.ageInYears(player).orElseThrow();
        source.sendSuccess(() -> Component.translatable("command.mushocraft.life.info",
                player.getDisplayName(),
                MushoCommands.number(age),
                data.stage().displayName(),
                data.birthTick()), false);
        return 1;
    }

    private int setAge(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        double years = DoubleArgumentType.getDouble(context, "years");
        players.forEach(player -> LifeService.setAge(player, years));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.life.setage",
                players.size(), MushoCommands.number(years)), true);
        return players.size();
    }

    private int rebirth(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(LifeService::rebirth);
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.life.rebirth",
                players.size()), true);
        return players.size();
    }
}
