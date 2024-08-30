package com.github.xray.stores;

import com.github.xray.render.Color;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

@SideOnly (Side.CLIENT)
public class BlockData {
        public String displayName;
        public Block block;
        public int metadata;
        public boolean active;
        public Color color;
        
        public BlockData() { }
        public BlockData(@NotNull String displayName, Block block, int metadata, boolean active, Color color) {
                this.displayName = displayName;
                this.block = block;
                this.metadata = metadata;
                this.active = active;
                this.color = color;
        }
        
        public void toggle() { active = !active; }
        
        public BlockData read(@NotNull JsonReader reader) throws IOException {
                reader.beginObject();
                
                while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("Display Name")) {
                                displayName = reader.nextString();
                        } else if (name.equals("Block")) {
                                block = Block.getBlockFromName(reader.nextString());
                        } else if (name.equals("Metadata")) {
                                metadata = reader.nextInt();
                        } else if (name.equals("Active")) {
                                active = reader.nextBoolean();
                        } else if (name.equals("Color")) {
                                color = new Color().read(reader);
                        } else {
                                reader.skipValue();
                        }
                }
                
                reader.endObject();
                
                return this;
        }
        
        public JsonWriter write(@NotNull JsonWriter writer) throws IOException {
                writer.beginObject()
                      .name("Display Name").value(displayName)
                      .name("Block").value(Objects.requireNonNull(block.getRegistryName()).toString())
                      .name("Metadata").value(metadata)
                      .name("Active").value(active);
                return color.write(writer.name("Color")).endObject();
        }
        
        public ItemStack getStack() { return new ItemStack(block, 1, metadata); }
        
        public @NotNull String name() { return displayName.isEmpty() ? block.getLocalizedName() : displayName; }
        
        @Override
        public String toString() { return "\"" + displayName + "\"[" + block.toString() + metadata + "]" + (active ? "#" : "%") + color.toString(); }
        
        @Override
        public int hashCode() { return (block.toString() + metadata).hashCode(); }
        
        @Override
        public boolean equals(Object o) {return o instanceof BlockData && block.equals(((BlockData) o).block) && metadata == ((BlockData) o).metadata; }
}
