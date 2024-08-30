package com.github.xray.stores;

import com.github.xray.render.Color;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

@SideOnly (Side.CLIENT)
public class BlockStores {
        public static final Set<BlockData> DATA_SET = new HashSet<>();
        public static final Map<BlockInfo, BlockData> DATA_MAP = new HashMap<>();
        public static final List<Block> BLOCK_LIST = new ArrayList<>();
        
        public static void clear() {
                DATA_SET.clear();
                DATA_MAP.clear();
        }
        
        public static void remove(BlockData data) {
                DATA_SET.remove(data);
                DATA_MAP.remove(BlockInfo.fromData(data));
        }
        
        public static void add(BlockData data) {
                if (DATA_SET.contains(data)) { return; }
                DATA_SET.add(data);
                DATA_MAP.put(BlockInfo.fromData(data), data);
        }
        
        public void read(@NotNull JsonReader reader) throws IOException {
                clear();
                reader.beginArray();
                
                while (reader.hasNext()) { add(new BlockData().read(reader)); }
                if (DATA_SET.isEmpty()) { add(new BlockData("Gold Ore", Blocks.GOLD_ORE, 0, true, new Color(-2147483648))); }
                
                reader.endArray();
        }
        
        public JsonWriter write(@NotNull JsonWriter writer) throws IOException {
                writer.beginArray();
                
                for (BlockData data : DATA_SET) { data.write(writer); }
                
                return writer.endArray();
        }
}
