package fr.xopi.mushocraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.xopi.mushocraft.body.BodyService;
import fr.xopi.mushocraft.body.BodyStat;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * {@code /mushocraft body info [player]}, {@code train <players> <stat> <xp>}, {@code aura <players> <mastery>}.
 */
public final class BodyCommand implements MushoSubcommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        var train = Commands.argument("players", EntityArgument.players());
        for (BodyStat stat : BodyStat.values()) {
            train.then(Commands.literal(stat.getSerializedName())
                    .then(Commands.argument("xp", DoubleArgumentType.doubleArg(0.0))
                            .executes(context -> setTraining(context, stat))));
        }
        return Commands.literal("body")
                .then(Commands.literal("info")
                        .executes(context -> info(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> info(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("train").then(train))
                .then(Commands.literal("aura")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("mastery", DoubleArgumentType.doubleArg(0.0))
                                        .executes(this::setAura))));
    }

    private int info(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.translatable("command.mushocraft.body.header", player.getDisplayName(),
                BattleAuraService.level(player).displayName(),
                MushoCommands.number(BattleAuraService.data(player).mastery())), false);
        for (BodyStat stat : BodyStat.values()) {
            source.sendSuccess(() -> Component.translatable("command.mushocraft.body.line", stat.displayName(),
                    MushoCommands.number(BodyService.progress(player, stat) * 100.0),
                    MushoCommands.number(BodyService.training(player).experience(stat))), false);
        }
        return 1;
    }

    private int setTraining(CommandContext<CommandSourceStack> context, BodyStat stat) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        double xp = DoubleArgumentType.getDouble(context, "xp");
        players.forEach(player -> {
            BodyService.setExperience(player, stat, xp);
            BodyService.apply(player);
        });
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.body.train",
                stat.displayName(), MushoCommands.number(xp), players.size()), true);
        return players.size();
    }

    private int setAura(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        double mastery = DoubleArgumentType.getDouble(context, "mastery");
        players.forEach(player -> BattleAuraService.setMastery(player, mastery));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.body.aura",
                MushoCommands.number(mastery), players.size()), true);
        return players.size();
    }
}
