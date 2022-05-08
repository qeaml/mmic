package com.github.qeaml.mmic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class Config implements ModMenuApi {
	private static Properties props = new Properties();
	private static File source = new File(MinecraftClient.getInstance().runDirectory, "options.mmic.txt");

	public static int gridColor = 0xFF000000;
	public static double gammaStep = 0.2;
	public static boolean miniF3 = false;
	public static boolean staticHand = false;

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
			miniF3 = Boolean.parseBoolean((String)props.getOrDefault("miniF3", "false"));
			staticHand = Boolean.parseBoolean((String)props.getOrDefault("staticHand", "false"));
		} catch(IOException e) {
			Client.log.warn("Could not load config: "+e.getMessage());
		}
	}

	public static void save() {
		try(var fw = new FileOutputStream(source)) {
			props.setProperty("gridColor", Integer.toString(gridColor));
			props.setProperty("gammaStep", Double.toString(gammaStep));
			props.setProperty("miniF3", Boolean.toString(miniF3));
			props.setProperty("staticHand", Boolean.toString(staticHand));
			props.store(fw, "MMIC Configuration");
		} catch(IOException e) {
			Client.log.warn("Could not save config: "+e.getMessage());
		}
	}

	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if(!FabricLoader.getInstance().isModLoaded("cloth-config2"))
			return parent -> null;
		return parent -> {
			var builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(new TranslatableText("config.mmic.title"))
				.setDefaultBackgroundTexture(new Identifier("minecraft", "textures/block/lapis_block.png"));
			var entry = builder.entryBuilder();
			var general = builder.getOrCreateCategory(new TranslatableText("config.mmic.general"));

			general.addEntry(entry.startAlphaColorField(
				new TranslatableText("config.mmic.gridColor"),
				gridColor)
				.setDefaultValue(0xFF000000)
				.setTooltip(new TranslatableText("config.mmic.gridColor.tip"))
				.setSaveConsumer(i -> gridColor = i)
				.build());
			general.addEntry(entry.startDoubleField(
				new TranslatableText("config.mmic.gammaStep"),
				gammaStep * 100)
				.setDefaultValue(20)
				.setTooltip(new TranslatableText("config.mmic.gammaStep.tip"))
				.setSaveConsumer(d -> gammaStep = d / 100)
				.build());
			general.addEntry(entry.startBooleanToggle(
				new TranslatableText("config.mmic.miniF3"),
				miniF3)
				.setDefaultValue(false)
				.setTooltip(new TranslatableText("config.mmic.miniF3.tip"))
				.setSaveConsumer(b -> miniF3 = b)
				.build());
			general.addEntry(entry.startBooleanToggle(
				new TranslatableText("config.mmic.staticHand"),
				staticHand)
				.setDefaultValue(false)
				.setTooltip(new TranslatableText("config.mmic.staticHand.tip"))
				.setSaveConsumer(b -> staticHand = b)
				.build());

			builder.setSavingRunnable(Config::save);

			return builder.build();
		};
	}
}
