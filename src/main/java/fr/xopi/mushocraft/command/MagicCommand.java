package fr.xopi.mushocraft.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.magic.spell.MagicProficiency;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
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
 * {@code /mushocraft magic info [player]}, {@code learn <players> <spell>}, {@code learnall <players>},
 * {@code forget <players>}, {@code xp <players> <school> <value>}.
 */
public final class MagicCommand implements MushoSubcommand {
    private static final DynamicCommandExceptionType UNKNOWN_SPELL = new DynamicCommandExceptionType(
            id -> Component.translatable("command.mushocraft.magic.unknown_spell", String.valueOf(id)));
    private static final SuggestionProvider<CommandSourceStack> SPELLS = (context, builder) ->
            SharedSuggestionProvider.suggestResource(MushoRegistries.SPELL.keySet(), builder);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("magic")
                .then(Commands.literal("info")
                        .executes(context -> info(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> info(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("learn")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("spell", ResourceLocationArgument.id()).suggests(SPELLS)
                                        .executes(this::learn))))
                .then(Commands.literal("learnall")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(this::learnAll)))
                .then(Commands.literal("forget")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(this::forget)))
                .then(buildXp());
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildXp() {
        var players = Commands.argument("players", EntityArgument.players());
        for (SpellSchool school : SpellSchool.values()) {
            players.then(Commands.literal(school.getSerializedName())
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                            .executes(context -> setXp(context, school))));
        }
        return Commands.literal("xp").then(players);
    }

    private int info(CommandSourceStack source, ServerPlayer player) {
        MagicProficiency proficiency = MagicService.proficiency(player);
        Component title = MagicService.title(player).map(rank -> rank.displayName())
                .orElse(Component.translatable("command.mushocraft.magic.no_title"));
        source.sendSuccess(() -> Component.translatable("command.mushocraft.magic.header", player.getDisplayName(),
                title, MagicService.knownSpells(player).size()), false);
        for (SpellSchool school : SpellSchool.values()) {
            if (proficiency.experience(school) > 0.0) {
                source.sendSuccess(() -> Component.translatable("command.mushocraft.magic.school", school.displayName(),
                        proficiency.rank(school).displayName(), MushoCommands.number(proficiency.experience(school))), false);
            }
        }
        return 1;
    }

    private int learn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "spell");
        Spell spell = MushoRegistries.SPELL.getOptional(id).orElseThrow(() -> UNKNOWN_SPELL.create(id));
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(player -> MagicService.learn(player, spell));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.magic.learn",
                spell.displayName(), players.size()), true);
        return players.size();
    }

    private int learnAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(player -> MushoRegistries.SPELL.forEach(spell -> MagicService.learn(player, spell)));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.magic.learnall", players.size()), true);
        return players.size();
    }

    private int forget(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        players.forEach(MagicService::forgetAll);
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.magic.forget", players.size()), true);
        return players.size();
    }

    private int setXp(CommandContext<CommandSourceStack> context, SpellSchool school) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        double value = DoubleArgumentType.getDouble(context, "value");
        players.forEach(player -> MagicService.setExperience(player, school, value));
        context.getSource().sendSuccess(() -> Component.translatable("command.mushocraft.magic.xp",
                school.displayName(), MushoCommands.number(value), players.size()), true);
        return players.size();
    }
}
