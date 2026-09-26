package io.github.firstwemine1.cleave.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.firstwemine1.cleave.config.ClientConfig;
import io.github.firstwemine1.cleave.config.CleaveConfig;
import io.github.firstwemine1.cleave.server.ClientStatus;
import io.github.firstwemine1.cleave.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ForceCommand implements ICommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return net.minecraft.commands.Commands.literal("force")
                .requires(CommandPredicates::isForceVeinMiningEnabled)
                .requires(CommandPredicates::isPlayer)
                .requires(CommandPredicates::canPlayerAccessForceVeinMining)
                .executes(this::defaultExecute); // /cleave force
    }

    public int defaultExecute(CommandContext<CommandSourceStack> cc) {
        if (!checkIsAllowed(cc)) return 0;

        Player player = cc.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        ClientStatus status = ClientStatus.getClientStatus(player.getUUID());
        ClientConfig cfg = CommandUtils.getSourceConfig(cc);
        status.forceNext = !status.forceNext;
        String message = status.forceNext ? cfg.TRIGGER_WHEN.get().formatConditionText("Next block mined{0} will be vein mined", " {0}") : "Cancelled vein mine";

        ServerPlayer serverPlayer = (ServerPlayer) player; 
        serverPlayer.sendSystemMessage(Component.literal(message));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getHelpText(CommandContext<CommandSourceStack> cc) {
        if (CommandUtils.isConsole(cc)) {
            return "";
        }

        ClientConfig cfg = CommandUtils.getSourceConfig(cc);
        if (!CommandUtils.isModerator(cc) && (cfg.MOD_DISABLED.get() || cfg.FORCE_RESTRICTED.get())) {
            return "";
        }

        return """
                - /cleave force
                    Forces a vein mine on the next block mined
                """;
    }

    private boolean checkIsAllowed(CommandContext<CommandSourceStack> cc) {
        if (CommandUtils.isConsole(cc))  {
            CommandUtils.sendMessage(cc, "Only players may use this command");
            return false;
        }

        ClientConfig cfg = CommandUtils.getSourceConfig(cc);
        boolean isModerator = CommandUtils.isModerator(cc);
        boolean isAllowed = isModerator || !(CleaveConfig.FORCE_DISABLED.get() || cfg.FORCE_RESTRICTED.get() || cfg.MOD_RESTRICTED.get());
        if (!isAllowed) {
            CommandUtils.sendMessage(cc, "You do not have access to this command.");
        }
        return isAllowed;
    }
}
