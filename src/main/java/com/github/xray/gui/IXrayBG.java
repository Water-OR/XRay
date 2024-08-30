package com.github.xray.gui;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly (Side.CLIENT)
public interface IXrayBG {
        ResourceLocation BG1 = new ResourceLocation("xray", "textures/config_bg1.png");
        ResourceLocation BG2 = new ResourceLocation("xray", "textures/config_bg2.png");
}
