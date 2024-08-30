package com.github.xray.stores;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SideOnly (Side.CLIENT)
public class BlockInfo {
        private static final Map<IBlockState, BlockInfo> STATE_INFO_MAP = new HashMap<>();
        private static final Map<BlockData, BlockInfo> DATA_INFO_MAP = new HashMap<>();
        
        public final Block block;
        public final int metadata;
        
        public BlockInfo(Block block, int metadata) {
                this.block = block;
                this.metadata = metadata;
        }
        
        public static @NotNull BlockInfo fromStack(ItemStack stack) {
                return new BlockInfo(((ItemBlock) stack.getItem()).getBlock(), stack.getMetadata());
        }
        
        @Contract ("_ -> new")
        public static @NotNull BlockInfo fromState(@NotNull IBlockState state) {
                return STATE_INFO_MAP.computeIfAbsent(state, state1 -> new BlockInfo(state.getBlock(), state.getBlock().getMetaFromState(state)));
        }
        
        @Contract (value = "_ -> new", pure = true)
        public static @NotNull BlockInfo fromData(@NotNull BlockData data) {
                return DATA_INFO_MAP.computeIfAbsent(data, data1 -> new BlockInfo(data.block, data.metadata));
        }
        
        @Override
        public String toString() { return block.toString() + metadata; }
        
        @Override
        public int hashCode() { return toString().hashCode(); }
        
        @Override
        public boolean equals(Object o) { return o instanceof BlockInfo && block.equals(((BlockInfo) o).block) && metadata == ((BlockInfo) o).metadata; }
}
