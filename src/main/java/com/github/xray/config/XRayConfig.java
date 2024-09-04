package com.github.xray.config;

import com.github.xray.stores.BlockStores;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly (Side.CLIENT)
public class XRayConfig {
        public static final BlockStores BLOCK_STORES = new BlockStores();
        public static boolean showActive = true;
        public static int radius = 64;
        public static boolean fade = true;
        public static double outlineThickness = 1D;
}
