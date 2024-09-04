package com.github.xray.stores;

import com.github.xray.control.Controller;
import com.github.xray.render.Color;
import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

@SideOnly (Side.CLIENT)
public class BlockData {
        public final Block block;
        public final int metadata;
        public final ImmutableList<ItemStack> subTypes;
        public final ItemStack stack;
        public final BlockInfo info;
        public String displayName;
        public boolean active;
        public boolean noMeta;
        public Color color;
        
        public BlockData(Block block, int metadata) {
                this.displayName = "";
                this.block = block;
                this.metadata = metadata;
                this.active = true;
                this.noMeta = false;
                this.color = Color.white();
                stack = new ItemStack(block, 1, metadata);
                info = new BlockInfo(block, metadata);
                NonNullList<ItemStack> stacks = NonNullList.create();
                block.getSubBlocks(CreativeTabs.SEARCH, stacks);
                subTypes = stacks.stream().filter(Objects::nonNull).filter(stack1 -> !stack1.isEmpty()).collect(ImmutableList.toImmutableList());
        }
        
        public static void read(@NotNull JsonReader reader) throws IOException {
                String displayName = "";
                Block block = Blocks.AIR;
                int metadata = 0;
                boolean active = true;
                boolean noMeta = false;
                Color color = Color.white();
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
                                case "NoMeta":
                                        noMeta = reader.nextBoolean();
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
                final BlockData result = new BlockData(block, metadata);
                result.displayName = displayName;
                result.active = active;
                result.color = color;
                BlockStores.add(result);
                if (noMeta) { result.toggleNoMeta(); }
        }
        
        public void toggleNoMeta() {
                noMeta = !noMeta;
                if (noMeta) {
                        BlockStores.remove(block);
                        BlockStores.add(this);
                }
        }
        
        public void toggleActive() { active = !active; }
        
        public void write(@NotNull JsonWriter writer) throws IOException {
                writer.beginObject()
                      .name("Display Name").value(displayName)
                      .name("Block").value(Objects.requireNonNull(block.getRegistryName()).toString())
                      .name("Metadata").value(metadata)
                      .name("Active").value(active)
                      .name("NoMeta").value(noMeta);
                color.write(writer.name("Color")).endObject();
        }
        
        public ItemStack getStack() {
                return noMeta ? subTypes.get(Controller.seconds % subTypes.size()) : stack;
        }
        
        public @NotNull String name() { return displayName.isEmpty() ? block.getLocalizedName() : displayName; }
        
        @Override
        public String toString() { return displayName + '[' + info.string + ']' + (active ? 'T' : 'F') + (noMeta ? 'T' : 'F') + color.toString(); }
        
        @Override
        public int hashCode() { return block.toString().hashCode(); }
        
        @Override
        public boolean equals(Object o) { return o instanceof BlockData && block.equals(((BlockData) o).block) && metadata == ((BlockData) o).metadata; }
}
