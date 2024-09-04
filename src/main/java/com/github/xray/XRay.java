package com.github.xray;

import com.github.xray.config.ConfigManager;
import com.github.xray.control.Controller;
import com.github.xray.control.XRayKeys;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod (modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, clientSideOnly = true)
@SideOnly (Side.CLIENT)
public class XRay {
        public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
        
        @Mod.EventHandler
        public void postInit(FMLPostInitializationEvent event) {
                LOGGER.info("Config File at: {}", ConfigManager.CONFIG);
                
                XRayKeys.init();
                
                ConfigManager.safe_load();
                ConfigManager.safe_save();
        }
        
        @Mod.EventHandler
        public void onExit(FMLServerStoppingEvent event) {
                Controller.stop();
        }
}
