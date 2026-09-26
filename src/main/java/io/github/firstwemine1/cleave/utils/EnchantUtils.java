package io.github.firstwemine1.cleave.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class EnchantUtils {
    public static int getExp(Level level, ItemStack heldItem, BlockPos pos, BlockState st) {
        // On 1.20.1 enchantments are plain objects, there is no registry holder lookup.
        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem);
        int silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, heldItem);
        return st.getExpDrop(level, level.random, pos, fortuneLevel, silkTouchLevel);
    }
}
