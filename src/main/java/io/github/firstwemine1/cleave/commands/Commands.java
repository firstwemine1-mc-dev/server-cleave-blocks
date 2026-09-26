package io.github.firstwemine1.cleave.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public final class Commands {
    public static final String COMMAND_ROOT = "cleave";
    public static final String LEGACY_COMMAND_ROOT = "svmm";

    private static final List<ICommand> commands = new ArrayList<>() {{
        add(new ToggleFeaturesCommand(true));
        add(new ToggleFeaturesCommand(false));
        add(new RestrictOrAllowFeaturesCommand(true));
        add(new RestrictOrAllowFeaturesCommand(false));
        add(new ForceCommand());
        add(new TunnelCommand());
        add(new ConfigCommand());
        add(new ServerConfigCommand());

        add(new HelpCommand(this));
    }};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var helpCommand = commands.stream()
                .filter(c -> c instanceof HelpCommand)
                .map(c -> (HelpCommand)c)
                .reduce((c1, c2) -> {throw new IllegalStateException("Only one HelpCommand should exist");})
                .orElseThrow(() -> new NoSuchElementException("A HelpCommand is required"));

        LiteralArgumentBuilder<CommandSourceStack> command = net.minecraft.commands.Commands.literal(COMMAND_ROOT)
                .requires(CommandPredicates::isModEnabled)
                .requires(CommandPredicates::canPlayerAccessMod)
                .executes(helpCommand::execute); // /cleave

        for (ICommand c : commands) {
            command = command.then(c.getCommand());
        }

        var registered = dispatcher.register(command);

        // The mod was called svmm before it grew past vein mining. Keep the old root working as a
        // redirect so existing command blocks, scripts and muscle memory do not break.
        dispatcher.register(net.minecraft.commands.Commands.literal(LEGACY_COMMAND_ROOT)
                .requires(CommandPredicates::isModEnabled)
                .requires(CommandPredicates::canPlayerAccessMod)
                .executes(helpCommand::execute)
                .redirect(registered));
    }
}
