package com.github.qeaml.mmic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import net.minecraft.client.MinecraftClient;

public class Config {
	private static Properties props = new Properties();
	private static File source = new File(MinecraftClient.getInstance().runDirectory, "options.mmic.txt");

	public static int gridColor = 0xFF000000;
	public static double gammaStep = 0.2;

	public static void load() {
		if(!source.exists()) {
			try {
				source.getParentFile().mkdirs();
				source.createNewFile();
				save();
				return;
			} catch(IOException e) {
				Client.log.warn("Could not load config: "+e.getMessage());
			}
			return;
		}
		try(var fr = new FileInputStream(source)) {
			props.load(fr);
			gridColor = Integer.parseInt((String)props.getOrDefault("gridColor", "-16777216"));
			gammaStep = Double.parseDouble((String)props.getOrDefault("gammaStep", "0.2"));
		} catch(IOException e) {
			Client.log.warn("Could not load config: "+e.getMessage());
		}
	}

	public static void save() {
		try(var fw = new FileOutputStream(source)) {
			props.setProperty("gridColor", Integer.toString(gridColor));
			props.setProperty("gammaStep", Double.toString(gammaStep));
			props.store(fw, new Date().toString());
		} catch(IOException e) {
			Client.log.warn("Could not save config: "+e.getMessage());
		}
	}
}
