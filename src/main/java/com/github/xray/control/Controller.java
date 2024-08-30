package com.github.xray.control;

import com.github.xray.render.Color;
import com.github.xray.stores.BlockData;
import com.github.xray.stores.BlockInfo;
import com.github.xray.stores.BlockStores;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SideOnly (Side.CLIENT)
@Mod.EventBusSubscriber
public class Controller {
        private static int compare(Vec3i x, Vec3i y) {
                BlockPos pos = mc.player.getPosition();
                long distanceX = (long) (x.getX() - pos.getX()) * (long) (x.getX() - pos.getX()) +
                                 (long) (x.getY() - pos.getY()) * (long) (x.getY() - pos.getY()) +
                                 (long) (x.getZ() - pos.getZ()) * (long) (x.getZ() - pos.getZ());
                long distanceY = (long) (y.getX() - pos.getX()) * (long) (y.getX() - pos.getX()) +
                                 (long) (y.getY() - pos.getY()) * (long) (y.getY() - pos.getY()) +
                                 (long) (y.getZ() - pos.getZ()) * (long) (y.getZ() - pos.getZ());
                return distanceX == distanceY ? x.compareTo(y) : (int) (distanceY - distanceX);
        }
        
        public static final Minecraft mc = Minecraft.instance;
        public static final SortedMap<Vec3i, BlockData> BLOCKS = Collections.synchronizedSortedMap(new TreeMap<>(Controller::compare));
        public static final Set<Block> blackList = new HashSet<>();
        
        private static ExecutorService executor;
        private static Future<?> task;
        
        public static boolean active = false;
        
        public static Vec3i prevPos;
        
        public static boolean moved() {
                return prevPos == null ||
                       prevPos.getX() != mc.player.getPosition().getX() ||
                       prevPos.getZ() != mc.player.getPosition().getZ() ||
                       prevPos.getY() != mc.player.getPosition().getY();
        }
        
        public static boolean active() { return active && mc.world != null && mc.player != null; }
        
        public static void scan(Vec3i current) {
                final RangeWrapper range = new RangeWrapper(current);
                final SortedMap<Vec3i, BlockData> render = new TreeMap<>(Controller::compare);
                
                range.forEach((pos, state) -> {
                        final Block block = state.getBlock();
                        if (blackList.contains(block)) { return; }
                        BlockData data = BlockStores.DATA_MAP.get(BlockInfo.fromState(state));
                        if (data != null && data.active) {
                                if (render.containsKey(pos)) { return; }
                                render.put(pos, data);
                        }
                });
                
                BLOCKS.clear();
                BLOCKS.putAll(render);
        }
        
        public static void update(Vec3i pos, IBlockState state, boolean option) {
                if (!active()) { return; }
                BlockData data = BlockStores.DATA_MAP.get(BlockInfo.fromState(state));
                if (data == null) { return; }
                if (option) { BLOCKS.put(pos, data); } else { BLOCKS.remove(pos); }
        }
        
        public static synchronized void scheduleScan(boolean force) {
                if (active() && (task == null || task.isDone()) && (force || moved())) {
                        prevPos = mc.player.getPosition();
                        task = executor.submit(() -> scan(prevPos));
                }
        }
        
        public static void start() {
                if (active) { return; }
                BLOCKS.clear();
                executor = Executors.newSingleThreadExecutor();
                active = true;
                scheduleScan(true);
        }
        
        public static void stop() {
                if (!active) { return; }
                try { executor.shutdownNow(); } catch (Exception ignore) { }
                active = false;
        }
        
        @SubscribeEvent
        public static void onBreak(BlockEvent.@NotNull BreakEvent event) { update(event.getPos(), event.getState(), false); }
        
        @SubscribeEvent
        public static void onPlace(BlockEvent.@NotNull EntityPlaceEvent event) { update(event.getPos(), event.getState(), true); }
        
        @SubscribeEvent
        public static void onChunkLoad(ChunkEvent.Load event) { scheduleScan(true); }
        
        @SubscribeEvent
        public static void onTickEnd(TickEvent.@NotNull ClientTickEvent event) {
                if (event.phase == TickEvent.Phase.END) { scheduleScan(false); }
        }
        
        static {
                blackList.add(Blocks.AIR);
                blackList.add(Blocks.BEDROCK);
                blackList.add(Blocks.STONE);
        }
}
