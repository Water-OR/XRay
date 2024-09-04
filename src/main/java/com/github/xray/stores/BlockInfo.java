package com.github.xray.stores;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SideOnly (Side.CLIENT)
public class BlockInfo {
        public final Block block;
        public final int metadata;
        public final String string;
        public final int hash;
        
        public BlockInfo(@NotNull Block block, int metadata) {
                this.block = block;
                this.metadata = metadata;
                string = block.toString() + '#' + metadata;
                hash = string.hashCode();
        }
        
        @Contract ("_ -> new")
        public static @NotNull BlockInfo fromStack(@NotNull ItemStack stack) {
                return stack.getItem() instanceof ItemBlock ? new BlockInfo(((ItemBlock) stack.getItem()).getBlock(), stack.getMetadata()) : new BlockInfo(Blocks.AIR, 0);
        }
        
        @Contract ("_ -> new")
        public static @NotNull BlockInfo fromState(@NotNull IBlockState state) {
                return new BlockInfo(state.getBlock(), state.getBlock().getMetaFromState(state));
        }
        
        @Override
        public final String toString() { return string; }
        
        @Override
        public final int hashCode() { return hash; }
        
        @Override
        public boolean equals(Object o) { return o instanceof BlockInfo && block.equals(((BlockInfo) o).block) && metadata == ((BlockInfo) o).metadata; }
}
