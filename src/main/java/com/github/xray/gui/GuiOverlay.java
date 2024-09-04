package com.github.xray.gui;

import com.github.xray.config.XRayConfig;
import com.github.xray.control.Controller;
import com.github.xray.render.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly (Side.CLIENT)
@Mod.EventBusSubscriber
public class GuiOverlay {
        
        @SubscribeEvent
        public static void onOverlay(RenderGameOverlayEvent.Post event) {
                if (!Controller.active() || !XRayConfig.showActive || event.getType() != RenderGameOverlayEvent.ElementType.TEXT) { return; }
                
                boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
                int oldPolygonMode = GL11.glGetInteger(GL11.GL_POLYGON_MODE);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                GL11.glBegin(GL11.GL_QUADS);
                
                Color.progressGL4(0, 127, 0, 127);
                GL11.glVertex2d(6, 6);
                GL11.glVertex2d(6, 11);
                GL11.glVertex2d(11, 11);
                GL11.glVertex2d(11, 6);
                
                Color.progressGL4(0, 255, 0, 255);
                GL11.glVertex2d(5, 5);
                GL11.glVertex2d(5, 10);
                GL11.glVertex2d(10, 10);
                GL11.glVertex2d(10, 5);
                
                GL11.glEnd();
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, oldPolygonMode);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                if (!wasBlendEnabled) { GL11.glDisable(GL11.GL_BLEND); }
                
                Minecraft.instance.fontRenderer.drawStringWithShadow(I18n.format("xray.overlay.text").trim(), 15, 4, 16777215);
        }
}
