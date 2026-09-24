package dev.tourmi.svmm.server;

import com.mojang.logging.LogUtils;

import dev.tourmi.svmm.config.ClientConfig;
import dev.tourmi.svmm.config.ClientConfigs;
import dev.tourmi.svmm.config.SVMMConfig;
import dev.tourmi.svmm.utils.MinecraftUtils;
import dev.tourmi.svmm.utils.Utils3D;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * Whole-tree felling: breaking one log takes the rest of the tree, and optionally its leaves.
 * <p>
 * Kept separate from {@link VeinMiner} rather than folded into it, because a tree is not a vein.
 * It has its own block lists, a much larger block budget than an ore vein needs, and its own
 * per-player toggle -- sharing any of those with vein mining would give the wrong behaviour for
 * both.
 */
public final class TreeFeller {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TreeFeller() {}

    public static boolean canFellTree(ServerPlayer player, BlockState minedBlockState) {
        if (SVMMConfig.TREE_FELLING_DISABLED.get()) return false;

        ClientConfig cfg = ClientConfigs.getClientConfig(player);
        if (cfg.TREE_FELLING_DISABLED.get()) return false;
        if (cfg.TREE_FELLING_RESTRICTED.get()) return false;

        // A forced vein mine is an explicit request for the vein miner, so it wins over felling.
        if (ClientStatus.getClientStatus(player.getUUID()).forceNext) return false;

        if (MinecraftUtils.isBlockInList(minedBlockState, SVMMConfig.TREE_FELLING_BLACKLIST.get())) return false;

        return MinecraftUtils.isBlockInList(minedBlockState, SVMMConfig.TREE_FELLING_LOG_WHITELIST.get());
    }

    public static void fellTree(ServerPlayer player, ServerLevel level, ItemStack heldItem, BlockPos blockPos) {
        Collection<BlockPos> blocks = Utils3D.getTreeBlocks(blockPos, level, SVMMConfig.TREE_FELLING_MAX_BLOCKS.get());
        MinecraftUtils.mineBlocks(level, player, heldItem, blocks);

        if (SVMMConfig.TREE_FELLING_LOG_USAGE.get()) {
            LOGGER.info("Player {} felled a tree at position {} {} {}, breaking {} blocks.",
                    player.getName().getString(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), blocks.size());
        }
    }
}
