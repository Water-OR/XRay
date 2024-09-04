package com.github.xray.control;

import com.github.xray.Tags;
import com.github.xray.gui.GuiConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SideOnly (Side.CLIENT)
@Mod.EventBusSubscriber
public class XRayKeys {
        public static final KeyBinding OPEN_MENU;
        public static final KeyBinding TOGGLE_XRAY;
        private static final List<KeyBinding> KEYS = new ArrayList<>();
        
        static {
                OPEN_MENU = add("open_menu", 26);
                TOGGLE_XRAY = add("toggle_xray", 27);
        }
        
        private static @NotNull KeyBinding add(String name, int key) {
                KeyBinding result = new KeyBinding("xray.key." + name, key, Tags.MOD_NAME);
                KEYS.add(result);
                return result;
        }
        
        public static void init() { KEYS.forEach(ClientRegistry::registerKeyBinding); }
        
        @SubscribeEvent
        public static void onKey(InputEvent.KeyInputEvent event) {
                if (Minecraft.instance.currentScreen != null || Minecraft.instance.world == null) { return; }
                
                if (OPEN_MENU.isPressed()) {
                        Minecraft.instance.displayGuiScreen(new GuiConfigScreen());
                } else if (TOGGLE_XRAY.isPressed()) {
                        if (Controller.active) { Controller.stop(); } else { Controller.start(); }
                }
        }
}
