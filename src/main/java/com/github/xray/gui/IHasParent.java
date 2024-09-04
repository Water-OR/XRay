package com.github.xray.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly (Side.CLIENT)
@Mod.EventBusSubscriber
public interface IHasParent {
        @SubscribeEvent
        static void onGuiOpen(@NotNull GuiOpenEvent event) {
                if (event.getGui() == null && Minecraft.instance.currentScreen instanceof IHasParent) {
                        event.setGui(((IHasParent) Minecraft.instance.currentScreen).parent());
                }
        }
        
        GuiScreen parent();
}
