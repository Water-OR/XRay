package com.github.xray.render;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

@SideOnly (Side.CLIENT)
public class Color {
        private int rgba;
        
        public Color() { }
        
        public Color(int rgba) { this.rgba = rgba; }
        
        @Contract (value = " -> new", pure = true)
        public static @NotNull Color white() { return Color.fromRGB(16777215); }
        
        @Contract (value = "_ -> new", pure = true)
        public static @NotNull Color fromRGB(int rgb) { return new Color((rgb << 8) | 255); }
        
        public static void progressGL3(int red, int green, int blue) { progressGL4(red, green, blue, 255); }
        
        public static void progressGL4(int red, int green, int blue, int alpha) {
                GL11.glColor4d(red / 255D, green / 255D, blue / 255D, alpha / 255D);
        }
        
        public void progressGL() {
                GL11.glColor4d(
                        red() / 255D, green() / 255D,
                        blue() / 255D, alpha() / 255D
                );
        }
        
        public int red() { return (rgba >> 24) & 0xFF; }
        
        public int green() { return (rgba >> 16) & 0xFF; }
        
        public int blue() { return (rgba >> 8) & 0xFF; }
        
        public int alpha() { return rgba & 0xFF; }
        
        public void progressGLAlpha(double modifier) {
                GL11.glColor4d(
                        red() / 255D, green() / 255D,
                        blue() / 255D, alpha() * modifier / 255D
                );
        }
        
        public @NotNull Color read(@NotNull JsonReader reader) throws IOException {
                reader.beginObject();
                
                while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("Alpha")) {
                                setAlpha(reader.nextInt());
                        } else if (name.equals("Red")) {
                                setRed(reader.nextInt());
                        } else if (name.equals("Green")) {
                                setGreen(reader.nextInt());
                        } else if (name.equals("Blue")) {
                                setBlue(reader.nextInt());
                        }
                }
                
                reader.endObject();
                return this;
        }
        
        public void setAlpha(int alpha) {
                this.rgba &= 0xFFFFFF00;
                this.rgba |= alpha & 0xFF;
        }
        
        public void setRed(int red) {
                this.rgba &= 0x00FFFFFF;
                this.rgba |= (red & 0xFF) << 24;
        }
        
        public void setGreen(int green) {
                this.rgba &= 0xFF00FFFF;
                this.rgba |= (green & 0xFF) << 16;
        }
        
        public void setBlue(int blue) {
                this.rgba &= 0xFFFF00FF;
                this.rgba |= (blue & 0xFF) << 8;
        }
        
        public JsonWriter write(@NotNull JsonWriter writer) throws IOException {
                return writer.beginObject()
                             .name("Alpha").value(alpha()).name("Red").value(red())
                             .name("Green").value(green()).name("Blue").value(blue()).endObject();
        }
        
        @Override
        public String toString() { return "{A:" + alpha() + ",R:" + red() + ",G:" + green() + ",B:" + blue() + "}"; }
        
        @Override
        public int hashCode() { return rgba; }
        
        @Override
        public boolean equals(Object o) { return o instanceof Color && ((Color) o).rgba == rgba; }
}
