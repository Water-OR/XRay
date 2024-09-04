package com.github.xray.gui;

import com.github.xray.config.ConfigManager;
import com.github.xray.stores.BlockData;
import com.github.xray.stores.BlockStores;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

@SideOnly (Side.CLIENT)
@Mod.EventBusSubscriber
public class GuiEditScreen extends GuiScreen implements IHasParent, IXrayBG {
        private static final int BUTTON_SAVE = 0;
        private static final int BUTTON_DELETE = 1;
        private static final int BUTTON_NO_META = 2;
        private static final int xSize = 384;
        private static final int ySize = 222;
        private final BlockData data;
        private final GuiConfigScreen parent;
        private final GuiTextField nameBar = new GuiTextField(0, Minecraft.instance.fontRenderer, 12, 16, 198, 24);
        private final GuiSlider sliderAlpha;
        private final GuiSlider sliderRed;
        private final GuiSlider sliderGreen;
        private final GuiSlider sliderBlue;
        private int drawX;
        private int drawY;
        
        public GuiEditScreen(GuiConfigScreen parent, @NotNull BlockData data) {
                this.parent = parent;
                this.data = data;
                nameBar.setText(data.displayName);
                nameBar.setCanLoseFocus(true);
                sliderAlpha = new GuiSlider(0, 12, 190, 232, 20,
                                            I18n.format("xray.gui.edit.slider.alpha.prefix").trim() + " ",
                                            " " + I18n.format("xray.gui.edit.slider.alpha.suffix").trim(),
                                            0, 255, data.color.alpha(), false, true,
                                            slider -> this.data.color.setAlpha(slider.getValueInt()));
                sliderRed = new GuiSlider(0, 12, 124, 232, 20,
                                          I18n.format("xray.gui.edit.slider.red.prefix").trim() + " ",
                                          " " + I18n.format("xray.gui.edit.slider.red.suffix").trim(),
                                          0, 255, data.color.red(), false, true,
                                          slider -> this.data.color.setRed(slider.getValueInt()));
                sliderGreen = new GuiSlider(0, 12, 146, 232, 20,
                                            I18n.format("xray.gui.edit.slider.green.prefix").trim() + " ",
                                            " " + I18n.format("xray.gui.edit.slider.green.suffix").trim(),
                                            0, 255, data.color.green(), false, true,
                                            slider -> this.data.color.setGreen(slider.getValueInt()));
                sliderBlue = new GuiSlider(0, 12, 168, 232, 20,
                                           I18n.format("xray.gui.edit.slider.blue.prefix").trim() + " ",
                                           " " + I18n.format("xray.gui.edit.slider.blue.suffix").trim(),
                                           0, 255, data.color.blue(), false, true,
                                           slider -> this.data.color.setBlue(slider.getValueInt()));
        }
        
        @Override
        public void initGui() {
                drawX = (width - xSize) / 2;
                drawY = (height - ySize) / 2;
                
                addButton(new GuiButton(BUTTON_SAVE, drawX + 265, drawY + 150, 114, 20, I18n.format("xray.gui.edit.save").trim()));
                addButton(new GuiButton(BUTTON_DELETE, drawX + 265, drawY + 128, 114, 20, I18n.format("xray.gui.edit.delete").trim()));
                addButton(new GuiButton(BUTTON_NO_META, drawX + 323, drawY + 52, 56, 20, ""));
        }
        
        @Override
        protected void actionPerformed(@NotNull GuiButton button) {
                if (button.id == BUTTON_SAVE) {
                        mc.player.closeScreen();
                } else if (button.id == BUTTON_DELETE) {
                        BlockStores.remove(data.info);
                        mc.player.closeScreen();
                } else if (button.id == BUTTON_NO_META) {
                        data.toggleNoMeta();
                }
        }
        
        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                drawDefaultBackground();
                GL11.glTranslated(drawX, drawY, 0);
                mc.renderEngine.bindTexture(BG1);
                drawTexturedModalRect(0, 0, 0, 0, 256, ySize);
                mc.renderEngine.bindTexture(BG2);
                drawTexturedModalRect(256, 0, 0, 0, xSize - 256, ySize);
                GL11.glTranslated(-drawX, -drawY, 0);
                
                super.drawScreen(mouseX, mouseY, partialTicks);
                
                GL11.glTranslated(drawX, drawY, 0);
                fontRenderer.drawStringWithShadow(I18n.format("xray.gui.edit.no_meta").trim(), 264, 58, 16777215);
                drawCenteredString(fontRenderer, I18n.format("xray.gui." + (data.noMeta ? "enabled" : "disabled")).trim(),
                                   351, 58, data.noMeta ? 65280 : 16711680);
                
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemAndEffectIntoGUI(data.getStack(), 223, 19);
                RenderHelper.disableStandardItemLighting();
                
                nameBar.drawTextBox();
                if (nameBar.getText().isEmpty() && !nameBar.isFocused()) {
                        fontRenderer.drawStringWithShadow(data.getStack().getDisplayName(), 16, 24, 0xA7A7A7);
                }
                
                sliderAlpha.drawButton(mc, mouseX - drawX, mouseY - drawY, partialTicks);
                sliderRed.drawButton(mc, mouseX - drawX, mouseY - drawY, partialTicks);
                sliderGreen.drawButton(mc, mouseX - drawX, mouseY - drawY, partialTicks);
                sliderBlue.drawButton(mc, mouseX - drawX, mouseY - drawY, partialTicks);
                
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                int oldPolygonMode = GL11.glGetInteger(GL11.GL_POLYGON_MODE);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                
                data.color.progressGL();
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2d(12, 48);
                GL11.glVertex2d(12, 123);
                GL11.glVertex2d(244, 123);
                GL11.glVertex2d(244, 48);
                GL11.glEnd();
                
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, oldPolygonMode);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                
                fontRenderer.drawString(I18n.format("xray.gui.edit.title").trim() + " - " + data.block.getRegistryName(), 8, 6, 4210752);
                GL11.glTranslated(-drawX, -drawY, 0);
                
                if (drawX + 223 <= mouseX && mouseX < drawX + 239 &&
                    drawY + 19 <= mouseY && mouseY < drawY + 35) { renderToolTip(data.getStack(), mouseX, mouseY); }
        }
        
        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
                super.mouseClicked(mouseX, mouseY, mouseButton);
                nameBar.mouseClicked(mouseX - drawX, mouseY - drawY, mouseButton);
                
                sliderAlpha.mousePressed(mc, mouseX - drawX, mouseY - drawY);
                sliderRed.mousePressed(mc, mouseX - drawX, mouseY - drawY);
                sliderGreen.mousePressed(mc, mouseX - drawX, mouseY - drawY);
                sliderBlue.mousePressed(mc, mouseX - drawX, mouseY - drawY);
        }
        
        @Override
        protected void mouseReleased(int mouseX, int mouseY, int state) {
                super.mouseReleased(mouseX, mouseY, state);
                
                sliderAlpha.mouseReleased(mouseX - drawX, mouseY - drawY);
                sliderRed.mouseReleased(mouseX - drawX, mouseY - drawY);
                sliderGreen.mouseReleased(mouseX - drawX, mouseY - drawY);
                sliderBlue.mouseReleased(mouseX - drawX, mouseY - drawY);
        }
        
        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
                super.keyTyped(typedChar, keyCode);
                
                nameBar.textboxKeyTyped(typedChar, keyCode);
                data.displayName = nameBar.getText();
        }
        
        @Override
        public void updateScreen() {
                super.updateScreen();
                
                nameBar.updateCursorCounter();
        }
        
        @Override
        public GuiScreen parent() { return parent.reload(); }
        
        @Override
        public void onGuiClosed() {
                super.onGuiClosed();
                ConfigManager.safe_save();
        }
}
