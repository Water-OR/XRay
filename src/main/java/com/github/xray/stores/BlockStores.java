package com.github.xray.stores;

import com.github.xray.render.Color;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SideOnly (Side.CLIENT)
public class BlockStores {
        public static final Map<BlockInfo, BlockData> DATA_MAP = new HashMap<>();
        public static final Map<Block, Set<BlockInfo>> INFO_MAP = new HashMap<>();
        public static final Set<Block> blackList = new HashSet<>();
        
        static {
                BlockStores.blackList.add(Blocks.AIR);
                BlockStores.blackList.add(Blocks.BEDROCK);
                BlockStores.blackList.add(Blocks.STONE);
        }
        
        public static void remove(Block block) {
                INFO_MAP.get(block).forEach(DATA_MAP::remove);
                INFO_MAP.get(block).clear();
        }
        
        public static void remove(BlockInfo info) {
                DATA_MAP.remove(info);
                INFO_MAP.get(info.block).remove(info);
        }
        
        public static void add(BlockData data) {
                if (DATA_MAP.containsValue(data)) { return; }
                DATA_MAP.put(data.info, data);
                INFO_MAP.computeIfAbsent(data.block, block -> new HashSet<>()).add(data.info);
        }
        
        public static @Nullable Color color(@NotNull IBlockState state) {
                final Block block = state.getBlock();
                if (blackList.contains(block)) { return null; }
                Set<BlockInfo> infos = INFO_MAP.get(block);
                if (infos == null || infos.isEmpty()) { return null; }
                BlockData data;
                if (!(data = DATA_MAP.get(infos.toArray(new BlockInfo[0])[0])).noMeta && (data = DATA_MAP.get(BlockInfo.fromState(state))) == null || !data.active) { return null; }
                return data.color;
        }
        
        public void read(@NotNull JsonReader reader) throws IOException {
                clear();
                reader.beginArray();
                
                while (reader.hasNext()) { BlockData.read(reader); }
                
                reader.endArray();
        }
        
        public static void clear() {
                DATA_MAP.clear();
                INFO_MAP.values().forEach(Set::clear);
        }
        
        public JsonWriter write(@NotNull JsonWriter writer) throws IOException {
                writer.beginArray();
                
                for (BlockData data : DATA_MAP.values()) { data.write(writer); }
                
                return writer.endArray();
        }
}
