package com.github.xray.mixin;

import com.github.xray.Tags;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin (FMLHandshakeMessage.ModList.class)
@SideOnly (Side.CLIENT)
public abstract class HideModId {
        @Shadow (remap = false) private Map<String, String> modTags;
        
        @Inject (method = "<init>(Ljava/util/List;)V", at = @At ("RETURN"), remap = false)
        private void hide(List<ModContainer> list, CallbackInfo cbi) {
                if (!Minecraft.instance.isSingleplayer()) { modTags.remove(Tags.MOD_ID); }
        }
}
