package com.github.xray.control;

import com.github.xray.config.XRayConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

@SideOnly (Side.CLIENT)
public class RangeWrapper {
        public int xMin;
        public int yMin;
        public int zMin;
        public int xMax;
        public int yMax;
        public int zMax;
        
        public int xChunkMin;
        public int xChunkMax;
        public int yChunkMax;
        public int yChunkMin;
        public int zChunkMin;
        public int zChunkMax;
        
        public RangeWrapper(@NotNull Vec3i pos) {
                xMin = pos.getX() - XRayConfig.radius;
                xMax = pos.getX() + XRayConfig.radius;
                zMin = pos.getZ() - XRayConfig.radius;
                zMax = pos.getZ() + XRayConfig.radius;
                yMin = Math.max(pos.getY() - XRayConfig.radius, 0);
                yMax = Math.min(pos.getY() + XRayConfig.radius, 255);
                
                xChunkMax = xMax >> 4;
                xChunkMin = xMin >> 4;
                zChunkMax = zMax >> 4;
                zChunkMin = zMin >> 4;
                yChunkMax = yMax >> 4;
                yChunkMin = yMin >> 4;
        }
        
        public void forEach(BiConsumer<Vec3i, IBlockState> consumer) {
                final World world = Minecraft.instance.world;
                for (int xChunk = xChunkMin; xChunk <= xChunkMax; ++xChunk) {
                        int xl = Math.max(xMin - (xChunk << 4), 0);
                        int xh = Math.min(xMax - (xChunk << 4), 15);
                        
                        for (int zChunk = zChunkMin; zChunk <= zChunkMax; ++zChunk) {
                                Chunk chunk = world.getChunk(xChunk, zChunk);
                                if (!chunk.isLoaded()) { continue; }
                                
                                int zl = Math.max(zMin - (zChunk << 4), 0);
                                int zh = Math.min(zMax - (zChunk << 4), 15);
                                
                                for (int yChunk = yChunkMin; yChunk <= yChunkMax; ++yChunk) {
                                        ExtendedBlockStorage blockStorage = chunk.storageArrays[yChunk];
                                        if (blockStorage == null) { continue; }
                                        
                                        int yl = Math.max(yMin - (yChunk << 4), 0);
                                        int yh = Math.min(yMax - (yChunk << 4), 15);
                                        
                                        for (int x = xl; x <= xh; ++x) {
                                                for (int y = yl; y <= yh; ++y) {
                                                        for (int z = zl; z <= zh; ++z) {
                                                                consumer.accept(new Vec3i(
                                                                        (xChunk << 4) + x,
                                                                        (yChunk << 4) + y,
                                                                        (zChunk << 4) + z
                                                                ), blockStorage.get(x, y, z));
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
        
        public void forEachSimple(BiConsumer<Vec3i, IBlockState> consumer) {
                final World world = Minecraft.instance.world;
                for (int x = xMin; x <= xMax; ++x) {
                        for (int z = zMin; z <= zMax; ++z) {
                                for (int y = yMin; y <= yMax; ++y) {
                                        final BlockPos pos = new BlockPos(x, y, z);
                                        consumer.accept(pos, world.getBlockState(pos));
                                }
                        }
                }
        }
}
