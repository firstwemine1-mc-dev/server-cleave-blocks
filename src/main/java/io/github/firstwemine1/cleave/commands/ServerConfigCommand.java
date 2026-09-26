package io.github.firstwemine1.cleave.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.firstwemine1.cleave.config.CleaveConfig;
import io.github.firstwemine1.cleave.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ServerConfigCommand implements ICommand {
    private enum ConfigListKeys {
        BLOCK_WHITE_LIST(CleaveConfig.BLOCK_WHITELIST),
        BLOCK_BLACK_LIST(CleaveConfig.BLOCK_BLACKLIST),
        GIANT_VEIN_STARTER_ORE(CleaveConfig.GIANT_VEIN_STARTER_BLOCKS),
        GIANT_VEIN_WHITELIST(CleaveConfig.GIANT_VEIN_WHITELIST),
        GIANT_VEIN_BLACKLIST(CleaveConfig.GIANT_VEIN_BLACKLIST),
        TREE_FELLING_LOG_WHITELIST(CleaveConfig.TREE_FELLING_LOG_WHITELIST),
        TREE_FELLING_LEAF_WHITELIST(CleaveConfig.TREE_FELLING_LEAF_WHITELIST),
        TREE_FELLING_BLACKLIST(CleaveConfig.TREE_FELLING_BLACKLIST),
        TUNNELING_WHITELIST(CleaveConfig.TUNNELING_WHITELIST),
        TUNNELING_BLACKLIST(CleaveConfig.TUNNELING_BLACKLIST),
        FORCE_BLACKLIST(CleaveConfig.FORCE_BLACKLIST);

        final ModConfigSpec.ConfigValue<List<? extends String>> config;

        ConfigListKeys(ModConfigSpec.ConfigValue<List<? extends String>> config) {
            this.config = config;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        var configCommand = Commands.literal("serverConfig")
                .requires(CommandPredicates::isRuntimeConfigEnabled)
                .requires(CommandPredicates::isModerator)
                .executes(this::defaultExecute);

        for (var key : ConfigListKeys.values()) {
            configCommand = configCommand.then(Commands.literal(key.toString())
                    .executes(ctx -> this.executeConfigList(ctx, key)) // /cleave config {configKey}
                    .then(Commands.literal("add")
                            .then(Commands.argument("value", StringArgumentType.string())
                                    .executes(ctx -> this.executeAdd(ctx, key)))) // /cleave config {configKey} add {value}
                    .then(Commands.literal("remove")
                            .then(Commands.argument("value", StringArgumentType.string())
                                    .executes(ctx -> this.executeRemove(ctx, key))))); // /cleave config {configKey} remove {value}
        }

        return configCommand;
    }

    @Override
    public String getHelpText(CommandContext<CommandSourceStack> cc) {
        if (!CommandUtils.isModerator(cc) || CleaveConfig.RUNTIME_CONFIG_DISABLED.get()) {
            return "";
        }

        return """
                - /cleave serverConfig
                    Lists the available config keys that can be modified
                - /cleave serverConfig {configKey}
                    Displays the value of the config key
                - /cleave serverConfig {configKey} [add|remove] {value}
                    Adds or removes the given {value} to the given {configKey}
                """;
    }

    private int defaultExecute(CommandContext<CommandSourceStack> cc) {
        CommandUtils.sendMessage(cc, "Available configuration keys:");
        CommandUtils.sendMessage(cc, Arrays
                .stream(ConfigListKeys.values())
                .map(Enum::name)
                .collect(Collectors.joining(", ")));

        return Command.SINGLE_SUCCESS;
    }

    private int executeConfigList(CommandContext<CommandSourceStack> cc, ConfigListKeys key) {
        CommandUtils.sendMessage(cc, "Values in " + key + ":\n" + String.join(", ", key.config.get()));

        return Command.SINGLE_SUCCESS;
    }

    private int executeAdd(CommandContext<CommandSourceStack> cc, ConfigListKeys key) {
        var newList = new ArrayList<>(key.config.get().stream().map(String::toString).toList());
        var value = cc.getArgument("value", String.class);
        newList.add(value);
        key.config.set(newList);
        key.config.save();

        CommandUtils.sendMessage(cc, "Successfully added " + value + " to " + key.name() + " list");

        return Command.SINGLE_SUCCESS;
    }

    private int executeRemove(CommandContext<CommandSourceStack> cc, ConfigListKeys key) {
        var newList = new ArrayList<>(key.config.get().stream().map(String::toString).toList());
        var value = cc.getArgument("value", String.class);
        newList.remove(value);
        key.config.set(newList);
        key.config.save();

        CommandUtils.sendMessage(cc, "Successfully removed " + value + " from " + key.name() + " list");

        return Command.SINGLE_SUCCESS;
    }
}
