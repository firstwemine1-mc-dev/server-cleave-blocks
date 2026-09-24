package dev.tourmi.svmm.utils;

import dev.tourmi.svmm.config.SVMMConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public final class MinecraftUtils {
    public static final HashMap<BlockPos, Player> BLOCKS_MINED = new HashMap<>();

    public static Stream<String> getColorNames() {
        return Stream.of(
                "white",
                "orange",
                "magenta",
                "light_blue",
                "yellow",
                "lime",
                "pink",
                "gray",
                "light_gray",
                "cyan",
                "purple",
                "blue",
                "brown",
                "green",
                "red",
                "black");
    }

    public static String getBlockName(BlockState bs) {
        String stateString = bs.toString();
        if (!stateString.contains("{") || !stateString.contains("}")) {
            return stateString;
        }
        return stateString.substring(stateString.indexOf('{') + 1, stateString.indexOf('}'));
    }

    public static boolean isBlockInList(BlockState bs, List<? extends String> list) {
        if (list.contains(MinecraftUtils.getBlockName(bs))) {
            return true;
        }
        var tags = list.stream().filter((e) -> e.startsWith("#")).map(e -> e.substring(1)).toList();
        return tags
                .stream()
                .map((t) -> TagKey.create(Registries.BLOCK, Identifier.parse(t)))
                .anyMatch(bs::is);
    }

    public static void mineBlock(ServerLevel level, ServerPlayer player, BlockPos blockPos, ItemStack heldItem) {
        if (level.isClientSide()) return;

        var blockState = level.getBlockState(blockPos);
        heldItem.mineBlock(level, blockState, blockPos, player);
        applyExtraDurabilityCost(heldItem, blockState);
        // NeoForge drops the block experience itself, from Block#dropResources -> CommonHooks#handleBlockDrops,
        // which fires BlockDropsEvent and pops EnchantmentHelper.processBlockExperience(level, tool, state.getExpDrop(...)).
        // playerDestroy below therefore already awards Fortune/Silk-Touch-correct XP; popping it again would double it.
        blockState.getBlock().playerDestroy(level, player, blockPos, blockState, level.getBlockEntity(blockPos), heldItem);
        level.removeBlock(blockPos, false);

        if (SVMMConfig.TELEPORT_ITEMS_TO_PLAYER.get()) {
            BLOCKS_MINED.put(blockPos, player);
            level.getServer().execute(() -> {
                BLOCKS_MINED.remove(blockPos);
            });
        }
    }

    public static void mineBlocks(ServerLevel level, ServerPlayer player, ItemStack heldItem, Collection<BlockPos> blocks) {
        boolean letItemBreak = !SVMMConfig.STOP_WHEN_ABOUT_TO_BREAK.get();

        for (BlockPos pos : blocks) {
            if (shouldStopMining(letItemBreak, heldItem)) break;

            mineBlock(level, player, pos, heldItem);
        }
    }

    /**
     * Vanilla's {@code mineBlock} already costs one durability point per block, so the mod is
     * charged per block and not per vein. This adds the extra cost on top when the server wants
     * vein mining to be more expensive than mining by hand.
     * <p>
     * Uses {@code getDamageValue}/{@code setDamageValue} rather than {@code hurtAndBreak}, whose
     * signature differs across the Minecraft versions this mod is ported to. Breaking the tool is
     * left to vanilla and to {@link #shouldStopMining}.
     */
    private static void applyExtraDurabilityCost(ItemStack item, BlockState blockState) {
        int extra = SVMMConfig.DURABILITY_MULTIPLIER.get() - 1;
        if (extra <= 0 || !item.isDamageableItem()) return;
        // Vanilla only damages the tool for blocks that take time to break, so match that.
        if (blockState.getDestroySpeed(null, null) == 0.0F) return;

        item.setDamageValue(Math.min(item.getDamageValue() + extra, item.getMaxDamage()));
    }

    private static boolean shouldStopMining(boolean letItemBreak, ItemStack item) {
        if (!letItemBreak && item.getMaxDamage() - item.getDamageValue() <= 2) {
            return true;
        }

        return item.isEmpty();
    }
}
