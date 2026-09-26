package io.github.firstwemine1.cleave.server;

import com.mojang.logging.LogUtils;

import io.github.firstwemine1.cleave.config.ClientConfig;
import io.github.firstwemine1.cleave.config.ClientConfigs;
import io.github.firstwemine1.cleave.config.CleaveConfig;
import io.github.firstwemine1.cleave.utils.MinecraftUtils;
import io.github.firstwemine1.cleave.utils.Utils3D;
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
        if (CleaveConfig.TREE_FELLING_DISABLED.get()) return false;

        ClientConfig cfg = ClientConfigs.getClientConfig(player);
        if (cfg.TREE_FELLING_DISABLED.get()) return false;
        if (cfg.TREE_FELLING_RESTRICTED.get()) return false;

        // A forced vein mine is an explicit request for the vein miner, so it wins over felling.
        if (ClientStatus.getClientStatus(player.getUUID()).forceNext) return false;

        if (MinecraftUtils.isBlockInList(minedBlockState, CleaveConfig.TREE_FELLING_BLACKLIST.get())) return false;

        return MinecraftUtils.isBlockInList(minedBlockState, CleaveConfig.TREE_FELLING_LOG_WHITELIST.get());
    }

    public static void fellTree(ServerPlayer player, ServerLevel level, ItemStack heldItem, BlockPos blockPos) {
        Collection<BlockPos> blocks = Utils3D.getTreeBlocks(blockPos, level, CleaveConfig.TREE_FELLING_MAX_BLOCKS.get());
        MinecraftUtils.mineBlocks(level, player, heldItem, blocks);

        if (CleaveConfig.TREE_FELLING_LOG_USAGE.get()) {
            LOGGER.info("Player {} felled a tree at position {} {} {}, breaking {} blocks.",
                    player.getName().getString(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), blocks.size());
        }
    }
}
