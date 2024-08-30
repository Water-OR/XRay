package com.github.xray.gui;

import com.github.xray.control.Controller;
import com.github.xray.render.Color;
import com.github.xray.stores.BlockData;
import com.github.xray.stores.BlockInfo;
import com.github.xray.stores.BlockStores;
import com.github.xray.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly (Side.CLIENT)
public class GuiAddBlock extends GuiScreen implements IHasParent, IXrayBG {
        private final GuiConfigScreen parent;
        
        private static final int BUTTON_BACK = 0;
        private static final int BUTTON_ADD_LOOK = 1;
        private static final int BUTTON_ADD_HAND = 2;
        private static final int xSize = 384;
        private static final int ySize = 222;
        private int drawX;
        private int drawY;
        private int scale;
        private String lastSearch = "";
        
        private final GuiTextField searchBar = new GuiTextField(0, Minecraft.instance.fontRenderer, 12, 16, 232, 24);
        private final ActiveList activeList = new ActiveList();
        private final List<ItemStack> blockList = new ArrayList<>();
        private final NonNullList<ItemStack> blockListWrapper = new NonNullList<ItemStack>(blockList, ItemStack.EMPTY) {
                @Override
                public void add(int i, @NotNull ItemStack stack) {
                        if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock && !Controller.blackList.contains(((ItemBlock) stack.getItem()).getBlock()) &&
                            !BlockStores.DATA_MAP.containsKey(BlockInfo.fromStack(stack)) && stack.getDisplayName().contains(searchBar.getText())) { super.add(i, stack); }
                }
        };
        private ItemStack current = ItemStack.EMPTY;
        
        public GuiAddBlock(GuiConfigScreen parent) {
                this.parent = parent;
                searchBar.setText("");
                searchBar.setCanLoseFocus(true);
                
                reload();
        }
        
        @Override
        protected void actionPerformed(@NotNull GuiButton button) {
                if (button.id == BUTTON_BACK) {
                        mc.player.closeScreen();
                } else if (button.id == BUTTON_ADD_HAND) {
                        ItemStack stack = mc.player.getHeldItemMainhand();
                        if (!(stack.getItem() instanceof ItemBlock)) { return; }
                        Block block = ((ItemBlock) stack.getItem()).getBlock();
                        if (BlockStores.DATA_MAP.containsKey(new BlockInfo(block, stack.getMetadata()))) {
                                Utils.sendFormatMessage("xray.msg.duplicate");
                        } else if (Controller.blackList.contains(block)) {
                                Utils.sendFormatMessage("xray.msg.blacklist");
                        } else {
                                BlockData data = new BlockData("", block, stack.getMetadata(), true, Color.fromRGB(16777215));
                                BlockStores.add(data);
                                mc.displayGuiScreen(new GuiEditScreen(this.parent, data));
                        }
                } else if (button.id == BUTTON_ADD_LOOK) {
                        RayTraceResult rayed = mc.player.rayTrace(128, 20);
                        if (rayed != null) {
                                IBlockState state = mc.world.getBlockState(rayed.getBlockPos());
                                if (BlockStores.DATA_MAP.containsKey(BlockInfo.fromState(state))) {
                                        Utils.sendFormatMessage("xray.msg.duplicate");
                                } else if (Controller.blackList.contains(state.getBlock())) {
                                        Utils.sendFormatMessage("xray.msg.blacklist");
                                } else {
                                        BlockData data = new BlockData("", state.getBlock(), state.getBlock().getMetaFromState(state), true, Color.fromRGB(16777215));
                                        BlockStores.add(data);
                                        mc.displayGuiScreen(new GuiEditScreen(this.parent, data));
                                }
                        } else { Utils.sendFormatMessage("xray.msg.nothing_find"); }
                }
        }
        
        public void reload() {
                blockList.clear();
                CreativeTabs.SEARCH.displayAllRelevantItems(blockListWrapper);
                
                activeList.reload();
        }
        
        @Override
        public void initGui() {
                drawX = (width - xSize) / 2;
                drawY = (height - ySize) / 2;
                scale = new ScaledResolution(mc).getScaleFactor();
                
                addButton(new GuiButton(BUTTON_BACK, drawX + 12, drawY + 190, 232, 20, I18n.format("xray.gui.add.button.back")));
                addButton(new GuiButton(BUTTON_ADD_LOOK, drawX + 265, drawY + 52, 114, 20, I18n.format("xray.gui.add.button.add_look")));
                addButton(new GuiButton(BUTTON_ADD_HAND, drawX + 265, drawY + 150, 114, 20, I18n.format("xray.gui.add.button.add_hand")));
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
                
                activeList.draw(mouseX - drawX, mouseY - drawY);
                searchBar.drawTextBox();
                if (searchBar.getText().isEmpty() && !searchBar.isFocused()) {
                        fontRenderer.drawStringWithShadow(I18n.format("xray.gui.config.search").trim(), 16, 24, 0xA7A7A7);
                }
                
                fontRenderer.drawString(I18n.format("xray.gui.add.title"), 8, 6, 4210752);
                GL11.glTranslated(-drawX, -drawY, 0);
                if (!current.isEmpty()) { renderToolTip(current, mouseX, mouseY); }
        }
        
        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
                super.keyTyped(typedChar, keyCode);
                searchBar.textboxKeyTyped(typedChar, keyCode);
                
                if (!searchBar.isFocused() || !lastSearch.equals(searchBar.getText())) {
                        lastSearch = searchBar.getText();
                        reload();
                }
        }
        
        @Override
        public void updateScreen() {
                super.updateScreen();
                searchBar.updateCursorCounter();
        }
        
        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
                super.mouseClicked(mouseX, mouseY, mouseButton);
                searchBar.mouseClicked(mouseX - drawX, mouseY - drawY, mouseButton);
                activeList.mouseClicked(mouseX - drawX, mouseY - drawY);
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
        }
        
        @Override
        public void handleMouseInput() throws IOException {
                super.handleMouseInput();
                activeList.handleMouse();
        }
        
        @Override
        public GuiScreen parent() { return parent; }
        
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
                        elementsHeight = Math.max(height, elementHeight * blockList.size());
                        scrollableHeight = elementsHeight - height;
                        clampScroll();
                }
                
                private void clampScroll() { scrolled = Math.max(0, Math.min(scrollableHeight, scrolled)); }
                
                public void drawElement(int i, int left, int right, int top, int bottom) {
                        final ItemStack block = blockList.get(i);
                        
                        fontRenderer.drawString(block.getDisplayName(), left + 30, top + 10, 16777215);
                        
                        RenderHelper.enableGUIStandardItemLighting();
                        itemRender.renderItemAndEffectIntoGUI(block, left + 5, top + 5);
                        RenderHelper.disableStandardItemLighting();
                        
                        int oldPolygonMode = GL11.glGetInteger(GL11.GL_POLYGON_MODE);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                        Color.progressGL3(167, 167, 167);
                        GL11.glBegin(GL11.GL_QUADS);
                        
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
                        int iMax = Math.min((int) ((scrolled + height) / elementHeight) + 1, blockList.size());
                        GL11.glTranslated(drawX, drawY, 0);
                        GuiUtils.drawGradientRect(0, 0, 0, width, height, 0XC0101010, 0xD0101010);
                        
                        hasBar = height < elementsHeight;
                        
                        if (hasBar) {
                                int oldPolygonMode = GL11.glGetInteger(GL11.GL_POLYGON_MODE);
                                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                                GL11.glDisable(GL11.GL_TEXTURE_2D);
                                GL11.glBegin(GL11.GL_QUADS);
                                
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
                                
                                GL11.glEnd();
                                GL11.glEnable(GL11.GL_TEXTURE_2D);
                                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, oldPolygonMode);
                        }
                        
                        GL11.glTranslated(0, -scrolled, 0);
                        GL11.glEnable(GL11.GL_SCISSOR_TEST);
                        GL11.glScissor((GuiAddBlock.this.drawX + drawX) * scale,
                                       (GuiAddBlock.this.height - GuiAddBlock.this.drawY - drawY - height - 1) * scale + 1,
                                       width * scale,
                                       height * scale
                        );
                        
                        if (hovered && !(hasBar && drawX + width - 6 <= mouseX && mouseX <= drawX + width) && !focusBar) {
                                int j = 0;
                                while ((j + 1) * elementHeight <= mouseY - drawY + scrolled) { ++j; }
                                if (j < blockList.size()) { current = blockList.get(j); }
                        } else { current = ItemStack.EMPTY; }
                        
                        for (; i < iMax; ++i) { drawElement(i, 0, (hasBar ? elementWidth - 6 : elementWidth), i * elementHeight, (i + 1) * elementHeight - 1); }
                        GL11.glDisable(GL11.GL_SCISSOR_TEST);
                        GL11.glTranslated(-drawX, -drawY + scrolled, 0);
                }
                
                public void mouseClicked(int mouseX, int mouseY) {
                        if (!hovered) { return; }
                        
                        if (hasBar && drawX + width - 6 <= mouseX && mouseX <= drawX + width) {
                                if (drawY + barScrolled <= mouseY && mouseY <= drawY + barScrolled + barHeight) {
                                        focusBar = true;
                                        preMouseY = mouseY;
                                        preScrolled = scrolled;
                                }
                                return;
                        }
                        
                        final BlockData newData = new BlockData("", ((ItemBlock) current.getItem()).getBlock(), current.getMetadata(), true, Color.fromRGB(16777215));
                        BlockStores.add(newData);
                        mc.displayGuiScreen(new GuiEditScreen(parent, newData));
                }
                
                public void mouseDrag(int mouseY) {
                        if (hasBar && focusBar) {
                                scrolled = preScrolled + (double) (mouseY - preMouseY) / (height - barHeight) * scrollableHeight;
                                clampScroll();
                        }
                }
                
                public void mouseRelease() {
                        focusBar = false;
                }
                
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
