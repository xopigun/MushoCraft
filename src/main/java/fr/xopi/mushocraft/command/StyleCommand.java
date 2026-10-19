package fr.xopi.mushocraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.sword.SwordMastery;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * {@code /mushocraft style info [player]}, {@code adopt <players> <style>}, {@code xp <players> <style> <value>},
 * {@code promote <players> <style> <masterRank>}, {@code learn <players> <technique>}.
 */
public final class StyleCommand implements MushoSubcommand {
    private static final DynamicCommandExceptionType UNKNOWN_TECHNIQUE = new DynamicCommandExceptionType(
            id -> Component.translatable("command.mushocraft.style.unknown_technique", String.valueOf(id)));
    private static final SuggestionProvider<CommandSourceStack> TECHNIQUES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(MushoRegistries.TECHNIQUE.keySet(), builder);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        var adopt = Commands.argument("players", EntityArgument.players());
        var xp = Commands.argument("players", EntityArgument.players());
        var promote = Commands.argument("players", EntityArgument.players());
        for (SwordStyle style : SwordStyle.values()) {
            adopt.then(Commands.literal(style.getSerializedName()).executes(context -> adopt(context, style)));
            xp.then(Commands.literal(style.getSerializedName())
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                            .executes(context -> setXp(context, style))));
            var byRank = Commands.literal(style.getSerializedName());
            for (Rank rank : Rank.values()) {
                byRank.then(Commands.literal(rank.getSerializedName()).executes(context -> promote(context, style, rank)));
            }
            promote.then(byRank);
        }
        return Commands.literal("style")
                .then(Commands.literal("info")
                        .executes(context -> info(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> info(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("adopt").then(adopt))
                .then(Commands.literal("xp").then(xp))
                .then(Commands.literal("promote").then(promote))
                .then(Commands.literal("learn")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("technique", ResourceLocationArgument.id()).suggests(TECHNIQUES)
                                        .executes(this::learn))));
    }

    private int info(CommandSourceStack source, ServerPlayer player) {
        SwordMastery mastery = SwordService.mastery(player);
        Component active = mastery.active().map(SwordStyle::displayName)
                .orElse(Component.translatable("command.mushocraft.style.none"));
        source.sendSuccess(() -> Component.translatable("command.mushocraft.style.header", player.getDisplayName(), active,
                SwordService.knownTechniques(player).size()), false);
        mastery.styles().forEach((style, progress) -> source.sendSuccess(() -> Component.translatable(
                "command.mushocraft.style.line", style.displayName(), progress.rank().displayName(),
                MushoCommands.number(progress.experience())), false));
        return 1;
    }

    private int adopt(CommandContext<CommandSourceStack> context, SwordStyle style) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(player -> SwordService.adopt(player, style));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.style.adopt",
                style.displayName(), players.size()), true);
        return players.size();
    }

    private int setXp(CommandContext<CommandSourceStack> context, SwordStyle style) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        double value = DoubleArgumentType.getDouble(context, "value");
        players.forEach(player -> {
            SwordService.adopt(player, style);
            SwordService.setProgress(player, style, SwordService.mastery(player).progress(style).orElseThrow().withExperience(value));
        });
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.style.xp",
                style.displayName(), MushoCommands.number(value), players.size()), true);
        return players.size();
    }

    private int promote(CommandContext<CommandSourceStack> context, SwordStyle style, Rank masterRank)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(player -> SwordService.promote(player, style, masterRank));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.style.promote",
                style.displayName(), masterRank.displayName(), players.size()), true);
        return players.size();
    }

    private int learn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "technique");
        Technique technique = MushoRegistries.TECHNIQUE.getOptional(id).orElseThrow(() -> UNKNOWN_TECHNIQUE.create(id));
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(player -> {
            if (!SwordService.mastery(player).practises(technique.style())) {
                SwordService.adopt(player, technique.style());
            }
            SwordService.learn(player, technique);
        });
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.style.learn",
                technique.displayName(), players.size()), true);
        return players.size();
    }
}
