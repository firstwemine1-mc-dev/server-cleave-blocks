package dev.tourmi.svmm.commands;

import dev.tourmi.svmm.config.SVMMConfig;
import dev.tourmi.svmm.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import dev.tourmi.svmm.config.spec.ConfigSpec;

import java.util.function.Supplier;

public final class CommandPredicates {
    private CommandPredicates() {}

    public static boolean isPlayer(CommandSourceStack cs) {
        return cs.isPlayer();
    }

    public static boolean isConsole(CommandSourceStack cs) {
        return !isPlayer(cs);
    }

    public static boolean isModerator(CommandSourceStack cs) {
        return Commands.LEVEL_GAMEMASTERS.check(cs.permissions());
    }

    public static boolean isModEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(SVMMConfig.MOD_DISABLED);
    }

    public static boolean isGiantVeinMiningEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(SVMMConfig.GIANT_VEIN_MINING_DISABLED);
    }

    public static boolean isTreeFellingEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(SVMMConfig.TREE_FELLING_DISABLED);
    }

    public static boolean isTunnelingEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(SVMMConfig.TUNNELING_DISABLED);
    }

    public static boolean isForceVeinMiningEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(SVMMConfig.FORCE_DISABLED);
    }

    public static boolean isRuntimeConfigEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(SVMMConfig.RUNTIME_CONFIG_DISABLED);
    }

    public static boolean canPlayerAccessMod(CommandSourceStack cs) {
        return canPlayerAccessFeature(cs, () -> CommandUtils.getSourceConfig(cs).MOD_RESTRICTED);
    }

    public static boolean canPlayerAccessGiantVeinMining(CommandSourceStack cs) {
        return canPlayerAccessFeature(cs, () -> CommandUtils.getSourceConfig(cs).GIANT_VEIN_MINING_RESTRICTED);
    }

    public static boolean canPlayerAccessTreeFelling(CommandSourceStack cs) {
        return canPlayerAccessFeature(cs, () -> CommandUtils.getSourceConfig(cs).TREE_FELLING_RESTRICTED);
    }

    public static boolean canPlayerAccessTunneling(CommandSourceStack cs) {
        return canPlayerAccessFeature(cs, () -> CommandUtils.getSourceConfig(cs).TUNNELING_RESTRICTED);
    }

    public static boolean canPlayerAccessForceVeinMining(CommandSourceStack cs) {
        return canPlayerAccessFeature(cs, () -> CommandUtils.getSourceConfig(cs).FORCE_RESTRICTED);
    }

    private static boolean isFeatureEnabled(ConfigSpec.BooleanValue isDisabled) {
        return !isDisabled.get();
    }

    private static boolean canPlayerAccessFeature(CommandSourceStack cs, Supplier<ConfigSpec.BooleanValue> isRestricted) {
        if (isModerator(cs)) {
            return true;
        }

        // The command tree is built and sent during login, before the player entity is attached to
        // the source, so there is no per-player config to consult yet. Stay permissive here: the
        // predicate is evaluated again, with a real player, when the command is actually run.
        if (cs.getPlayer() == null) {
            return true;
        }

        return !isRestricted.get().get();
    }
}
