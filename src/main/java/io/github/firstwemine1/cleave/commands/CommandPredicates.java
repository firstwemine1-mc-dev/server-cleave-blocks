package io.github.firstwemine1.cleave.commands;

import io.github.firstwemine1.cleave.config.CleaveConfig;
import io.github.firstwemine1.cleave.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.ModConfigSpec;

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
        return cs.hasPermission(Commands.LEVEL_GAMEMASTERS);
    }

    public static boolean isModEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(CleaveConfig.MOD_DISABLED);
    }

    public static boolean isGiantVeinMiningEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(CleaveConfig.GIANT_VEIN_MINING_DISABLED);
    }

    public static boolean isTreeFellingEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(CleaveConfig.TREE_FELLING_DISABLED);
    }

    public static boolean isTunnelingEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(CleaveConfig.TUNNELING_DISABLED);
    }

    public static boolean isForceVeinMiningEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(CleaveConfig.FORCE_DISABLED);
    }

    public static boolean isRuntimeConfigEnabled(CommandSourceStack cs) {
        return isFeatureEnabled(CleaveConfig.RUNTIME_CONFIG_DISABLED);
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

    private static boolean isFeatureEnabled(ModConfigSpec.BooleanValue isDisabled) {
        return !isDisabled.get();
    }

    private static boolean canPlayerAccessFeature(CommandSourceStack cs, Supplier<ModConfigSpec.BooleanValue> isRestricted) {
        return isModerator(cs) || !isRestricted.get().get();
    }
}
