package com.github.xray.utils;

import com.github.xray.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly (Side.CLIENT)
public class Utils {
        public static void sendMessage(String msg) {
                Minecraft.instance.player.sendMessage(new TextComponentString("[" + Tags.MOD_NAME + "] " + msg));
        }
        
        public static void sendFormatMessage(String key, Object... o) {
                sendMessage(I18n.format(key, o).trim());
        }
}
