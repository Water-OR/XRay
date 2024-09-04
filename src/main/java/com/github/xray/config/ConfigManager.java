package com.github.xray.config;

import com.github.xray.XRay;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@SideOnly (Side.CLIENT)
public class ConfigManager {
        public static final File DIRECTORY = Loader.instance().getConfigDir();
        public static final File CONFIG = new File(DIRECTORY, "xray.json");
        
        public static void safe_load() {
                try {
                        load();
                } catch (IOException exception) {
                        XRay.LOGGER.warn("Failed in loading config!");
                        XRay.LOGGER.error(exception.getLocalizedMessage());
                        exception.printStackTrace();
                }
        }
        
        public static void load() throws IOException {
                if (!CONFIG.exists()) { return; }
                JsonReader reader = new JsonReader(new FileReader(CONFIG));
                reader.beginObject();
                
                while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("Show Active")) {
                                XRayConfig.showActive = reader.nextBoolean();
                        } else if (name.equals("Radius")) {
                                XRayConfig.radius = reader.nextInt();
                        } else if (name.equals("Fade")) {
                                XRayConfig.fade = reader.nextBoolean();
                        } else if (name.equals("Outline Thickness")) {
                                XRayConfig.outlineThickness = Math.max(0D, reader.nextDouble());
                        } else if (name.equals("Block Stores")) {
                                XRayConfig.BLOCK_STORES.read(reader);
                        } else {
                                reader.skipValue();
                        }
                }
                
                reader.endObject();
                reader.close();
        }
        
        public static void safe_save() {
                try {
                        save();
                } catch (IOException exception) {
                        XRay.LOGGER.warn("Failed in saving config!");
                        exception.printStackTrace();
                }
        }
        
        public static void save() throws IOException {
                JsonWriter writer = new JsonWriter(new FileWriter(CONFIG));
                writer.setSerializeNulls(true);
                writer.setIndent("    ");
                writer.beginObject()
                      .name("Show Active").value(XRayConfig.showActive)
                      .name("Radius").value(XRayConfig.radius)
                      .name("Fade").value(XRayConfig.fade)
                      .name("Outline Thickness").value(XRayConfig.outlineThickness);
                XRayConfig.BLOCK_STORES.write(writer.name("Block Stores")).endObject();
                writer.close();
        }
}
