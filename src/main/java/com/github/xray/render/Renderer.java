package com.github.xray.render;

import com.github.xray.config.XRayConfig;
import com.github.xray.control.Controller;
import com.github.xray.stores.BlockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.SortedMap;

@SideOnly (Side.CLIENT)
@Mod.EventBusSubscriber
public class Renderer {
        public static final SortedMap<Vec3i, BlockData> ores = Controller.BLOCKS;
        
        private static double dx;
        private static double dy;
        private static double dz;
        
        public static double distanceTo(@NotNull Vec3i pos) {
                return Math.sqrt(
                        (pos.getX() - dx) * (pos.getX() - dx) +
                        (pos.getY() - dy) * (pos.getY() - dy) +
                        (pos.getZ() - dz) * (pos.getZ() - dz)
                );
        }
        
        public static void renderBlock(@NotNull Vec3i pos, @NotNull BlockData data) {
                if (!data.active) { return; }
                GL11.glBegin(GL11.GL_LINES);
                data.color.progressGLAlpha(XRayConfig.fade ? Math.max(0, (XRayConfig.radius - distanceTo(pos)) / XRayConfig.radius) : 1D);
                
                int xMin = pos.getX(), xMax = xMin + 1;
                int yMin = pos.getY(), yMax = yMin + 1;
                int zMin = pos.getZ(), zMax = zMin + 1;
                
                GL11.glVertex3i(xMin, yMin, zMin);
                GL11.glVertex3i(xMax, yMin, zMin);
                GL11.glVertex3i(xMin, yMin, zMax);
                GL11.glVertex3i(xMax, yMin, zMax);
                GL11.glVertex3i(xMin, yMax, zMin);
                GL11.glVertex3i(xMax, yMax, zMin);
                GL11.glVertex3i(xMin, yMax, zMax);
                GL11.glVertex3i(xMax, yMax, zMax);
                
                GL11.glVertex3i(xMin, yMin, zMin);
                GL11.glVertex3i(xMin, yMax, zMin);
                GL11.glVertex3i(xMax, yMin, zMin);
                GL11.glVertex3i(xMax, yMax, zMin);
                GL11.glVertex3i(xMin, yMin, zMax);
                GL11.glVertex3i(xMin, yMax, zMax);
                GL11.glVertex3i(xMax, yMin, zMax);
                GL11.glVertex3i(xMax, yMax, zMax);
                
                GL11.glVertex3i(xMin, yMin, zMin);
                GL11.glVertex3i(xMin, yMin, zMax);
                GL11.glVertex3i(xMin, yMax, zMin);
                GL11.glVertex3i(xMin, yMax, zMax);
                GL11.glVertex3i(xMax, yMin, zMin);
                GL11.glVertex3i(xMax, yMin, zMax);
                GL11.glVertex3i(xMax, yMax, zMin);
                GL11.glVertex3i(xMax, yMax, zMax);
                
                GL11.glEnd();
        }
        
        public static void render(float particleTick) {
                final EntityPlayerSP player = Minecraft.instance.player;
                dx = player.prevPosX + (player.posX - player.prevPosX) * particleTick;
                dy = player.prevPosY + (player.posY - player.prevPosY) * particleTick;
                dz = player.prevPosZ + (player.posZ - player.prevPosZ) * particleTick;
                GL11.glTranslated(-dx, -dy, -dz);
                
                boolean wasEnabledTexture2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
                boolean wasEnabledDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
                boolean wasEnabledBlend = GL11.glIsEnabled(GL11.GL_BLEND);
                int oldPolygonMode = GL11.glGetInteger(GL11.GL_POLYGON_MODE);
                
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glLineWidth((float) XRayConfig.outlineThickness);
                
                ores.forEach(Renderer::renderBlock);
                
                if (wasEnabledTexture2D) { GL11.glEnable(GL11.GL_TEXTURE_2D); }
                if (wasEnabledDepthTest) { GL11.glEnable(GL11.GL_DEPTH_TEST); }
                GL11.glDepthMask(true);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, oldPolygonMode);
                if (!wasEnabledBlend) { GL11.glDisable(GL11.GL_BLEND); }
                
                GL11.glTranslated(dx, dy, dz);
        }
        
        @SubscribeEvent
        public static void onEvent(@NotNull RenderWorldLastEvent event) {
                if (!Controller.active()) { return; }
                render(event.getPartialTicks());
        }
}
