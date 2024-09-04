package com.github.xray.control;

import com.github.xray.render.Color;
import com.github.xray.stores.BlockStores;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
        public static final Minecraft mc = Minecraft.instance;
        public static final SortedMap<Vec3i, Color> ores = Collections.synchronizedSortedMap(new TreeMap<>(Controller::compare));
        public static boolean active = false;
        public static Vec3i prevPos;
        public static int ticks = 0;
        public static int seconds = 0;
        private static ExecutorService executor;
        private static Future<?> task;

        private static int compare(@NotNull Vec3i x, @NotNull Vec3i y) {
                BlockPos pos = mc.player.getPosition();
                long distanceX = (long) (x.getX() - pos.getX()) * (long) (x.getX() - pos.getX()) +
                                 (long) (x.getY() - pos.getY()) * (long) (x.getY() - pos.getY()) +
                                 (long) (x.getZ() - pos.getZ()) * (long) (x.getZ() - pos.getZ());
                long distanceY = (long) (y.getX() - pos.getX()) * (long) (y.getX() - pos.getX()) +
                                 (long) (y.getY() - pos.getY()) * (long) (y.getY() - pos.getY()) +
                                 (long) (y.getZ() - pos.getZ()) * (long) (y.getZ() - pos.getZ());
                return distanceX == distanceY ? x.compareTo(y) : (int) (distanceY - distanceX);
        }
        
        public static void start() {
                if (active) { return; }
                ores.clear();
                executor = Executors.newSingleThreadExecutor();
                active = true;
                scheduleScan(true);
        }
        
        public static synchronized void scheduleScan(boolean force) {
                if (active() && (task == null || task.isDone()) && (force || moved())) {
                        prevPos = mc.player.getPosition();
                        task = executor.submit(() -> scan(prevPos));
                }
        }
        
        public static boolean active() { return active && mc.world != null && mc.player != null; }
        
        public static boolean moved() {
                return prevPos == null ||
                       prevPos.getX() != mc.player.getPosition().getX() ||
                       prevPos.getZ() != mc.player.getPosition().getZ() ||
                       prevPos.getY() != mc.player.getPosition().getY();
        }
        
        public static void scan(Vec3i current) {
                final RangeWrapper range = new RangeWrapper(current);
                final Map<Vec3i, Color> render = new HashMap<>();
                
                range.forEach((pos, state) -> {
                        Color color = BlockStores.color(state);
                        if (color != null) { render.put(pos, color); }
                });
                
                ores.clear();
                ores.putAll(render);
        }
        
        public static void stop() {
                if (!active) { return; }
                try { executor.shutdownNow(); } catch (Exception ignore) { }
                active = false;
        }
        
        @SubscribeEvent
        public static void onBreak(BlockEvent.@NotNull BreakEvent event) { update(event.getPos(), event.getState(), false); }
        
        public static void update(Vec3i pos, IBlockState state, boolean option) {
                if (!active()) { return; }
                if (option) {
                        Color color = BlockStores.color(state);
                        if (color != null) { ores.put(pos, color); }
                } else { ores.remove(pos); }
        }
        
        @SubscribeEvent
        public static void onPlace(BlockEvent.@NotNull EntityPlaceEvent event) { update(event.getPos(), event.getState(), true); }
        
        @SubscribeEvent
        public static void onChunkLoad(ChunkEvent.Load event) { scheduleScan(true); }
        
        @SubscribeEvent
        public static void onTickEnd(TickEvent.@NotNull ClientTickEvent event) {
                if (event.phase == TickEvent.Phase.END) {
                        scheduleScan(false);
                        ticks = (ticks + 1) % 20;
                        if (ticks == 0) { ++seconds; }
                        if (seconds < 0) { seconds = 0; }
                }
        }
}
