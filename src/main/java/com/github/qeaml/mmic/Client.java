package com.github.qeaml.mmic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class Client implements ClientModInitializer {
	public static File cfg = new File(MinecraftClient.getInstance().runDirectory, "config.mmic");
	public static byte cfgVer = 1;
	public static int gridColor = 0xFF000000;
	public static final Logger log = LoggerFactory.getLogger("mmic");

	@Override
	public void onInitializeClient() {
		log.info("Hello world");
		loadConfig();
	}

	private static void loadConfig() {
		if(!cfg.exists()) {
			log.warn("Config does not exist.");
			cfg.getParentFile().mkdirs();
			try {
				cfg.createNewFile();
				saveConfig();
			} catch (IOException e) {
				log.warn("Could not create config: "+e.getMessage());
			}
			return;
		}
		/*
		field      | size | offset | note
		-----------|------|--------|-----------------
		magic      | 4    | 0      | always ASCII string "MMIC"
		cfgVersion | 1    | 4      | incremental config version (1)
		gridColor  | 4    | 5      | stored in 8-bit ARGB
		*/
		try(var fr = new FileInputStream(cfg)) {
			// magic
			var magicRaw = fr.readNBytes(4);
			if(magicRaw[0] != 'M' || magicRaw[1] != 'M' || magicRaw[2] != 'I' || magicRaw[3] != 'C') {
				log.warn("Invalid config file: Invalid magic.");
				return;
			};

			// cfgVersion
			var configVerRaw = fr.readNBytes(1);
			if(configVerRaw[0] != cfgVer) {
				log.warn("Invalid config file: Wrong version.");
				return;
			}

			// gridColor
			var gridColorRaw = fr.readNBytes(4);
			gridColor = (gridColorRaw[0] << 24 |
									 gridColorRaw[1] << 16 |
									 gridColorRaw[2] <<  8 |
									 gridColorRaw[3]);
		} catch(IOException e) {
			log.warn("Could not load config: "+e.getMessage());
		} finally {
			log.info("Loaded config.");
		}
	}

	public static void saveConfig() {
		try(var fw = new FileOutputStream(cfg)) {
			// magic
			fw.write(new byte[]{'M', 'M', 'I', 'C'});

			// cfgVersion
			fw.write(new byte[]{cfgVer});

			// gridColor
			byte a = (byte)(gridColor >> 24 & 0xFF);
			byte r = (byte)(gridColor >> 16 & 0xFF);
			byte g = (byte)(gridColor >>  8 & 0xFF);
			byte b = (byte)(gridColor       & 0xFF);
			fw.write(new byte[]{a, r, g, b});
		} catch(IOException e) {
			log.warn("Could not save config: "+e.getMessage());
		} finally {
			log.info("Saved config.");
		}
	}

	public static Text onOff(boolean on) {
		var tkey = "other.mmic." + (on ? "on" : "off");
		var color = on ? Formatting.GREEN : Formatting.RED;
		var style = Style.EMPTY.withColor(color);
		return new TranslatableText(tkey).setStyle(style);
	}

	public static void playClick() {
		MinecraftClient.getInstance().getSoundManager().play(
			new PositionedSoundInstance(
				SoundEvents.UI_BUTTON_CLICK,
				SoundCategory.BLOCKS,
				.25f, .5f,
				0, 0, 0));
	}
}
