package io.github.firstwemine1.cleave.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public interface ICommand {
    LiteralArgumentBuilder<CommandSourceStack> getCommand();
    String getHelpText(CommandContext<CommandSourceStack> commandContext);
}
