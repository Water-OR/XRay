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
        public ItemStack stack;
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
                update();
        }
        
        public void toggle() { active = !active; }
        
        public BlockData read(@NotNull JsonReader reader) throws IOException {
                reader.beginObject();
                
                while (reader.hasNext()) {
                        String name = reader.nextName();
                        switch (name) {
                                case "Display Name":
                                        displayName = reader.nextString();
                                        break;
                                case "Block":
                                        block = Block.getBlockFromName(reader.nextString());
                                        break;
                                case "Metadata":
                                        metadata = reader.nextInt();
                                        break;
                                case "Active":
                                        active = reader.nextBoolean();
                                        break;
                                case "Color":
                                        color = new Color().read(reader);
                                        break;
                                default:
                                        reader.skipValue();
                                        break;
                        }
                }
                
                reader.endObject();
                update();
                return this;
        }
        
        public void write(@NotNull JsonWriter writer) throws IOException {
                writer.beginObject()
                      .name("Display Name").value(displayName)
                      .name("Block").value(Objects.requireNonNull(block.getRegistryName()).toString())
                      .name("Metadata").value(metadata)
                      .name("Active").value(active);
                color.write(writer.name("Color")).endObject();
        }
        
        public void update() { stack = new ItemStack(block, 1, metadata); }
        
        public ItemStack getStack() { return stack; }
        
        public @NotNull String name() { return displayName.isEmpty() ? block.getLocalizedName() : displayName; }
        
        @Override
        public String toString() { return "\"" + displayName + "\"[" + block.toString() + metadata + "]" + (active ? "#" : "%") + color.toString(); }
        
        @Override
        public int hashCode() { return (block.toString() + metadata).hashCode(); }
        
        @Override
        public boolean equals(Object o) {return o instanceof BlockData && block.equals(((BlockData) o).block) && metadata == ((BlockData) o).metadata; }
}
