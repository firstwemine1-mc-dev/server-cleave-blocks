package io.github.firstwemine1.cleave.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.firstwemine1.cleave.commands.ICommand;
import io.github.firstwemine1.cleave.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Function;

public class ConfigEnumValueCommand<T extends Enum<T>> implements ICommand {
    private final String name;
    private final Class<T> clazz;
    private final Function<CommandSourceStack, ModConfigSpec.EnumValue<T>> configGetter;

    public ConfigEnumValueCommand(String name, Class<T> clazz, Function<CommandSourceStack, ModConfigSpec.EnumValue<T>> configGetter) {
        this.name = name;
        this.clazz = clazz;
        this.configGetter = configGetter;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        var command = Commands.literal(name)
                .executes(this::defaultExecute);

        for (T value : clazz.getEnumConstants()) {
            command = command.then(Commands.literal(value.toString())
                    .executes(cc -> setValue(cc, value)));
        }

        return command;
    }

    @Override
    public String getHelpText(CommandContext<CommandSourceStack> commandContext) {
        return "";
    }

    private int defaultExecute(CommandContext<CommandSourceStack> cc) {
        CommandUtils.sendMessage(cc, String.format("'%s' value: '%s'", name, configGetter.apply(cc.getSource()).get().toString()));

        return Command.SINGLE_SUCCESS;
    }

    private int setValue(CommandContext<CommandSourceStack> cc, T value) {
        ModConfigSpec.EnumValue<T> config = this.configGetter.apply(cc.getSource());
        config.set(value);
        config.save();

        CommandUtils.sendMessage(cc, String.format("'%s' value is now '%s'", name, configGetter.apply(cc.getSource()).get().toString()));

        return Command.SINGLE_SUCCESS;
    }
}
