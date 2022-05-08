package com.github.qeaml.mmic;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ConfigScreen implements ModMenuApi {
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
				Config.gridColor)
				.setDefaultValue(0xFF000000)
				.setTooltip(new TranslatableText("config.mmic.gridColor.tip"))
				.setSaveConsumer(i -> Config.gridColor = i)
				.build());
			general.addEntry(entry.startDoubleField(
				new TranslatableText("config.mmic.gammaStep"),
				Config.gammaStep * 100)
				.setDefaultValue(20)
				.setTooltip(new TranslatableText("config.mmic.gammaStep.tip"))
				.setSaveConsumer(d -> Config.gammaStep = d / 100)
				.build());
			general.addEntry(entry.startBooleanToggle(
				new TranslatableText("config.mmic.miniF3"),
				Config.miniF3)
				.setDefaultValue(false)
				.setTooltip(new TranslatableText("config.mmic.miniF3.tip"))
				.setSaveConsumer(b -> Config.miniF3 = b)
				.build());
			general.addEntry(entry.startBooleanToggle(
				new TranslatableText("config.mmic.staticHand"),
				Config.staticHand)
				.setDefaultValue(false)
				.setTooltip(new TranslatableText("config.mmic.staticHand.tip"))
				.setSaveConsumer(b -> Config.staticHand = b)
				.build());

			builder.setSavingRunnable(Config::save);

			return builder.build();
		};
	}
}
