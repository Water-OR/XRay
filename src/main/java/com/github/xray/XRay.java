package com.github.xray;

import com.github.xray.config.ConfigManager;
import com.github.xray.control.Controller;
import com.github.xray.control.XRayKeys;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod (modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, clientSideOnly = true)
@SideOnly (Side.CLIENT)
public class XRay {
        public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
        
        /**
         * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
         * Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
         * </a>
         */
        @Mod.EventHandler
        public void preInit(FMLPreInitializationEvent event) {
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
