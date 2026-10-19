package fr.xopi.mushocraft.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Locale;

/**
 * Registers {@code /mushocraft ...}, the debug and administration commands (operators only).
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MushoCommands {
    private static final int OPERATOR_LEVEL = 2;

    private static final List<MushoSubcommand> SUBCOMMANDS = List.of(
            new LifeCommand(),
            new TalentCommand()
    );

    private MushoCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(MushoCraft.MODID)
                .requires(source -> source.hasPermission(OPERATOR_LEVEL));
        SUBCOMMANDS.forEach(subcommand -> root.then(subcommand.build()));
        event.getDispatcher().register(root);
    }

    /** Numbers shown in command feedback, always with a dot and two decimals whatever the server locale. */
    static Component number(double value) {
        return Component.literal(String.format(Locale.ROOT, "%.2f", value));
    }
}
