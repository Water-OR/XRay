package com.github.xray.gui;

import com.github.xray.config.ConfigManager;
import com.github.xray.config.XRayConfig;
import com.github.xray.control.Controller;
import com.github.xray.render.Color;
import com.github.xray.stores.BlockData;
import com.github.xray.stores.BlockStores;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly (Side.CLIENT)
public class GuiConfigScreen extends GuiScreen implements IXrayBG {
        private static final int BUTTON_CLOSE = 0;
        private static final int BUTTON_RELOAD = 1;
        private static final int BUTTON_SET_FADE = 2;
        private static final int BUTTON_ADD_BLOCK = 3;
        private static final int BUTTON_SHOW_ACTIVE = 4;
        private static final int xSize = 384;
        private static final int ySize = 222;
        private int drawX;
        private int drawY;
        private int scale;
        private String lastSearch = "";
        private double newWidth;
        
        private final GuiSlider radiusSlider = new GuiSlider(0, 12, 190, 232, 20,
                                                             I18n.format("xray.gui.config.slider.prefix").trim() + " ",
                                                             " " + I18n.format("xray.gui.config.slider.suffix").trim(),
                                                             16, 256, XRayConfig.radius, false, true,
                                                             slider -> XRayConfig.radius = slider.getValueInt());
        private final GuiTextField widthBar = new GuiTextField(0, Minecraft.instance.fontRenderer, 323, 128, 56, 20);
        private final GuiTextField searchBar = new GuiTextField(0, Minecraft.instance.fontRenderer, 12, 16, 232, 24);
        private final ActiveList activeList = new ActiveList();
        private List<BlockData> dataList = new ArrayList<>();
        
        public GuiConfigScreen() {
                widthBar.setText(String.valueOf(newWidth = XRayConfig.outlineThickness));
                widthBar.setCanLoseFocus(true);
                searchBar.setText("");
                searchBar.setCanLoseFocus(true);
                dataList.addAll(BlockStores.DATA_SET);
                activeList.reload();
        }
        
        @Override
        protected void actionPerformed(@NotNull GuiButton button) {
                if (button.id == BUTTON_CLOSE) {
                        mc.player.closeScreen();
                } else if (button.id == BUTTON_RELOAD) {
                        ConfigManager.safe_load();
                        radiusSlider.setValue(XRayConfig.radius);
                        reload();
                } else if (button.id == BUTTON_SET_FADE) {
                        XRayConfig.fade = !XRayConfig.fade;
                } else if (button.id == BUTTON_ADD_BLOCK) {
                        mc.displayGuiScreen(new GuiAddBlock(this));
                } else if (button.id == BUTTON_SHOW_ACTIVE) {
                        XRayConfig.showActive = !XRayConfig.showActive;
                }
        }
        
        public GuiConfigScreen reload() {
                if (searchBar.getText().isEmpty()) {
                        dataList.clear();
                        dataList.addAll(BlockStores.DATA_SET);
                } else {
                        dataList = BlockStores.DATA_SET.stream().filter(
                                data -> data.name().toLowerCase().contains(searchBar.getText().toLowerCase())
                        ).collect(Collectors.toList());
                }
                
                activeList.reload();
                return this;
        }
        
        @Override
        public void initGui() {
                drawX = (width - xSize) / 2;
                drawY = (height - ySize) / 2;
                scale = new ScaledResolution(mc).getScaleFactor();
                
                addButton(new GuiButton(BUTTON_CLOSE, drawX + 265, drawY + 150, 56, 20, I18n.format("xray.gui.config.button.close")));
                addButton(new GuiButton(BUTTON_RELOAD, drawX + 323, drawY + 150, 56, 20, I18n.format("xray.gui.config.button.reload")));
                addButton(new GuiButton(BUTTON_SET_FADE, drawX + 323, drawY + 74, 56, 20, ""));
                addButton(new GuiButton(BUTTON_ADD_BLOCK, drawX + 265, drawY + 52, 114, 20, I18n.format("xray.gui.config.button.add_block")));
                addButton(new GuiButton(BUTTON_SHOW_ACTIVE, drawX + 323, drawY + 96, 56, 20, ""));
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
                fontRenderer.drawStringWithShadow(I18n.format("xray.gui.config.fade").trim(), 264, 80, 16777215);
                drawCenteredString(fontRenderer, I18n.format("xray.gui.config." + (XRayConfig.fade ? "enabled" : "disabled")).trim(),
                                   351, 80, XRayConfig.fade ? 65280 : 16711680);
                
                fontRenderer.drawStringWithShadow(I18n.format("xray.gui.config.show_active").trim(), 264, 102, 16777215);
                drawCenteredString(fontRenderer, I18n.format("xray.gui.config." + (XRayConfig.showActive ? "enabled" : "disabled")).trim(),
                                   351, 102, XRayConfig.showActive ? 65280 : 16711680);
                
                fontRenderer.drawStringWithShadow(I18n.format("xray.gui.config.width").trim(), 264, 134, 16777215);
                XRayConfig.outlineThickness = newWidth;
                if (!widthBar.isFocused()) {
                        widthBar.setText(String.valueOf(newWidth = XRayConfig.outlineThickness));
                }
                widthBar.drawTextBox();
                
                activeList.draw(mouseX - drawX, mouseY - drawY);
                searchBar.drawTextBox();
                if (searchBar.getText().isEmpty() && !searchBar.isFocused()) {
                        fontRenderer.drawStringWithShadow(I18n.format("xray.gui.config.search").trim(), 16, 24, 0xA7A7A7);
                }
                radiusSlider.drawButton(mc, mouseX - drawX, mouseY - drawY, partialTicks);
                fontRenderer.drawString(I18n.format("xray.gui.config.title"), 8, 6, 4210752);
                GL11.glTranslated(-drawX, -drawY, 0);
        }
        
        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
                super.keyTyped(typedChar, keyCode);
                widthBar.textboxKeyTyped(typedChar, keyCode);
                searchBar.textboxKeyTyped(typedChar, keyCode);
                
                if (!searchBar.isFocused() || !lastSearch.equals(searchBar.getText())) {
                        lastSearch = searchBar.getText();
                        reload();
                }
                
                try {
                        newWidth = Double.parseDouble(widthBar.getText());
                } catch (NumberFormatException ignored) { }
                
        }
        
        @Override
        public void updateScreen() {
                widthBar.updateCursorCounter();
                searchBar.updateCursorCounter();
        }
        
        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
                super.mouseClicked(mouseX, mouseY, mouseButton);
                widthBar.mouseClicked(mouseX - drawX, mouseY - drawY, mouseButton);
                searchBar.mouseClicked(mouseX - drawX, mouseY - drawY, mouseButton);
                activeList.mouseClicked(mouseX - drawX, mouseY - drawY, mouseButton);
                radiusSlider.mousePressed(mc, mouseX - drawX, mouseY - drawY);
        }
        
        @Override
        protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
                super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
                activeList.mouseDrag(mouseY - drawY);
        }
        
        @Override
        protected void mouseReleased(int mouseX, int mouseY, int state) {
                super.mouseReleased(mouseX, mouseY, state);
                activeList.mouseRelease();
                radiusSlider.mouseReleased(mouseX - drawX, mouseY - drawY);
        }
        
        @Override
        public void onGuiClosed() {
                ConfigManager.safe_save();
                
                Controller.scheduleScan(true);
        }
        
        @Override
        public void handleMouseInput() throws IOException {
                super.handleMouseInput();
                activeList.handleMouse();
        }
        
        public class ActiveList {
                private static final int drawX = 12;
                private static final int drawY = 48;
                private static final int width = 232;
                private static final int height = 140;
                
                private static final int elementWidth = 232;
                private static final int elementHeight = 30;
                private int elementsHeight;
                private int scrollableHeight;
                
                private double scrolled;
                private double barScrolled;
                private boolean hovered;
                private boolean hasBar;
                private double barHeight;
                
                private boolean focusBar;
                private double preScrolled;
                private int preMouseY;
                
                public ActiveList() { }
                
                public void reload() {
                        elementsHeight = Math.max(height, elementHeight * dataList.size());
                        scrollableHeight = elementsHeight - height;
                        clampScroll();
                }
                
                private void clampScroll() { scrolled = Math.max(0, Math.min(scrollableHeight, scrolled)); }
                
                public void drawElement(int i, int left, int right, int top, int bottom) {
                        final BlockData data = dataList.get(i);
                        fontRenderer.drawString(data.getStack().getDisplayName(), left + 30, top + 5, -1);
                        fontRenderer.drawString(I18n.format("xray.gui.config." + (data.active ? "enabled" : "disabled")).trim(),
                                                left + 30, top + 15, data.active ? 65280 : 16711680);
                        
                        RenderHelper.enableGUIStandardItemLighting();
                        itemRender.renderItemAndEffectIntoGUI(data.getStack(), left + 5, top + 5);
                        RenderHelper.disableStandardItemLighting();
                        
                        int mid = top + elementHeight / 2;
                        
                        int oldPolygonMode = GL11.glGetInteger(GL11.GL_POLYGON_MODE);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                        Color.progressGL3(32, 32, 32);
                        GL11.glBegin(GL11.GL_QUADS);
                        
                        GL11.glVertex2d(right - 20, mid - 6);
                        GL11.glVertex2d(right - 20, mid + 4);
                        GL11.glVertex2d(right - 10, mid + 4);
                        GL11.glVertex2d(right - 10, mid - 6);
                        
                        data.color.progressGL();
                        
                        GL11.glVertex2d(right - 18, mid - 4);
                        GL11.glVertex2d(right - 18, mid + 2);
                        GL11.glVertex2d(right - 12, mid + 2);
                        GL11.glVertex2d(right - 12, mid - 4);
                        
                        Color.progressGL3(167, 167, 167);
                        
                        GL11.glVertex2d(left, bottom - 2);
                        GL11.glVertex2d(left, bottom);
                        GL11.glVertex2d(right, bottom);
                        GL11.glVertex2d(right, bottom - 2);
                        
                        GL11.glEnd();
                        Color.progressGL3(255, 255, 255);
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, oldPolygonMode);
                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                }
                
                public void draw(int mouseX, int mouseY) {
                        hovered = drawX <= mouseX && mouseX <= drawX + width &&
                                  drawY <= mouseY && mouseY <= drawY + height;
                        
                        int i = (int) (scrolled / elementHeight);
                        int iMax = Math.min((int) ((scrolled + height) / elementHeight) + 1, dataList.size());
                        GL11.glTranslated(drawX, drawY, 0);
                        GuiUtils.drawGradientRect(0, 0, 0, width, height, 0XC0101010, 0xD0101010);
                        
                        hasBar = height < elementsHeight;
                        int oldPolygonMode = GL11.glGetInteger(GL11.GL_POLYGON_MODE);
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glBegin(GL11.GL_QUADS);
                        
                        if (hasBar) {
                                barHeight = Math.max(32, Math.min((double) height / elementsHeight, 1D) * height);
                                barScrolled = scrolled / scrollableHeight * (height - barHeight);
                                
                                Color.progressGL3(0, 0, 0);
                                
                                GL11.glVertex2d(width - 6, 0);
                                GL11.glVertex2d(width - 6, height);
                                GL11.glVertex2d(width, height);
                                GL11.glVertex2d(width, 0);
                                
                                Color.progressGL3(128, 128, 128);
                                
                                GL11.glVertex2d(width - 6, barScrolled);
                                GL11.glVertex2d(width - 6, barScrolled + barHeight);
                                GL11.glVertex2d(width, barScrolled + barHeight);
                                GL11.glVertex2d(width, barScrolled);
                                
                                Color.progressGL3(192, 192, 192);
                                
                                GL11.glVertex2d(width - 6, barScrolled);
                                GL11.glVertex2d(width - 6, barScrolled + barHeight - 1);
                                GL11.glVertex2d(width - 1, barScrolled + barHeight - 1);
                                GL11.glVertex2d(width - 1, barScrolled);
                        }
                        Color.progressGL3(192, 192, 192);
                        
                        GL11.glVertex2d(0, height - 1);
                        GL11.glVertex2d(0, height);
                        GL11.glVertex2d(width, height);
                        GL11.glVertex2d(width, height - 1);
                        
                        GL11.glEnd();
                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, oldPolygonMode);
                        
                        GL11.glTranslated(0, -scrolled, 0);
                        GL11.glEnable(GL11.GL_SCISSOR_TEST);
                        GL11.glScissor((GuiConfigScreen.this.drawX + drawX) * scale,
                                       (GuiConfigScreen.this.height - GuiConfigScreen.this.drawY - drawY - height - 1) * scale + 1,
                                       width * scale,
                                       height * scale
                        );
                        
                        for (; i < iMax; ++i) { drawElement(i, 0, (hasBar ? elementWidth - 6 : elementWidth), i * elementHeight, (i + 1) * elementHeight - 1); }
                        GL11.glDisable(GL11.GL_SCISSOR_TEST);
                        GL11.glTranslated(-drawX, -drawY + scrolled, 0);
                }
                
                public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
                        if (!hovered) { return; }
                        
                        if (hasBar && drawX + width - 6 <= mouseX && mouseX <= drawX + width) {
                                if (drawY + barScrolled <= mouseY && mouseY <= drawY + barScrolled + barHeight) {
                                        focusBar = true;
                                        preMouseY = mouseY;
                                        preScrolled = scrolled;
                                }
                                return;
                        }
                        
                        int i = 0;
                        while ((i + 1) * elementHeight <= mouseY - drawY + scrolled) { ++i; }
                        if (i >= dataList.size()) { return; }
                        
                        if (mouseButton == 0) {
                                dataList.get(i).toggle();
                        } else if (mouseButton == 1) {
                                mc.displayGuiScreen(new GuiEditScreen(GuiConfigScreen.this, dataList.get(i)));
                        }
                }
                
                public void mouseDrag(int mouseY) {
                        if (hasBar && focusBar) {
                                scrolled = preScrolled + (double) (mouseY - preMouseY) / (height - barHeight) * scrollableHeight;
                                clampScroll();
                        }
                }
                
                public void mouseRelease() { focusBar = false; }
                
                public void handleMouse() {
                        if (!hovered) { return; }
                        
                        int scroll = Mouse.getEventDWheel();
                        scroll = Math.max(-1, Math.min(1, scroll)) * 7;
                        if (scroll != 0) {
                                if (isShiftKeyDown()) { scroll *= 4; }
                                scrolled -= scroll;
                                clampScroll();
                        }
                }
        }
}
