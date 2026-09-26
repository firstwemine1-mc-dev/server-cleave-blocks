package io.github.firstwemine1.cleave.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.firstwemine1.cleave.config.ClientConfig;
import io.github.firstwemine1.cleave.config.CleaveConfig;
import io.github.firstwemine1.cleave.server.ClientStatus;
import io.github.firstwemine1.cleave.server.Tunneler;
import io.github.firstwemine1.cleave.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class TunnelCommand implements ICommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand(){
        return Commands.literal("tunnel")
                .requires(CommandPredicates::isTunnelingEnabled)
                .requires(CommandPredicates::isPlayer)
                .requires(CommandPredicates::canPlayerAccessTunneling)
                .then(Commands.literal("cancel")
                        .executes(this::cancelTunnel)) // /cleave tunnel cancel
                .then(Commands.argument("width", IntegerArgumentType.integer(1))
                        .then(Commands.argument("height", IntegerArgumentType.integer(1))
                                .then(Commands.argument("maxDepth", IntegerArgumentType.integer(1))
                                        .executes(this::tunnelWidthHeightMaxDepth)) // /cleave tunnel {width} {height} {maxDepth}
                                .executes(this::tunnelWidthHeight))) // /cleave tunnel {width} {height}
                .executes(this::tunnel); // /cleave tunnel
    }

    @Override
    public String getHelpText(CommandContext<CommandSourceStack> cc) {
        if (CommandUtils.isConsole(cc)) {
            return "";
        }

        ClientConfig cfg = CommandUtils.getSourceConfig(cc);
        if (!CommandUtils.isModerator(cc) && (cfg.MOD_RESTRICTED.get() || cfg.MOD_DISABLED.get() || cfg.TUNNELING_RESTRICTED.get())) {
            return "";
        }

        return """
                - /cleave tunnel
                    run /cleave help tunnel for more details about this command
                """;
    }

    private int tunnel(CommandContext<CommandSourceStack> cc) {
        if (!checkIsAllowed(cc)) return 0;

        ClientStatus status = Tunneler.toggleTunneler(cc.getSource().getEntity().getUUID());
        CommandUtils.sendMessage(cc, status.lastMessage);

        return Command.SINGLE_SUCCESS;
    }

    private int tunnelWidthHeight(CommandContext<CommandSourceStack> cc) {
        if (!checkIsAllowed(cc)) return 0;

        ClientStatus status = Tunneler.toggleTunneler(cc.getSource().getEntity().getUUID(),
                cc.getArgument("width", Integer.class),
                cc.getArgument("height", Integer.class));
        CommandUtils.sendMessage(cc, status.lastMessage);

        return Command.SINGLE_SUCCESS;
    }

    private int tunnelWidthHeightMaxDepth(CommandContext<CommandSourceStack> cc) {
        if (!checkIsAllowed(cc)) return 0;

        ClientStatus status = Tunneler.toggleTunneler(cc.getSource().getEntity().getUUID(),
                cc.getArgument("width", Integer.class),
                cc.getArgument("height", Integer.class),
                cc.getArgument("maxDepth", Integer.class));
        CommandUtils.sendMessage(cc, status.lastMessage);

        return Command.SINGLE_SUCCESS;
    }

    private int cancelTunnel(CommandContext<CommandSourceStack> cc) {
        if (!checkIsAllowed(cc)) return 0;

        Tunneler.cancelTunnel(cc.getSource().getEntity().getUUID());

        return Command.SINGLE_SUCCESS;
    }

    private boolean checkIsAllowed(CommandContext<CommandSourceStack> cc) {
        if (!cc.getSource().isPlayer())  {
            CommandUtils.sendMessage(cc, "Only players may use this command");
            return false;
        }

        ClientConfig cfg = CommandUtils.getSourceConfig(cc);
        boolean isModerator = CommandUtils.isModerator(cc);
        boolean isAllowed = isModerator || !(CleaveConfig.TUNNELING_DISABLED.get() || cfg.TUNNELING_RESTRICTED.get() || cfg.MOD_RESTRICTED.get());
        if (!isAllowed) {
            CommandUtils.sendMessage(cc, "You do not have access to this command.");
        }
        return isAllowed;
    }
}
