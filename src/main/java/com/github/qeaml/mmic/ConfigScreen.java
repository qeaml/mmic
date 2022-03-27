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
				Client.gridColor)
				.setDefaultValue(0xFF000000)
				.setTooltip(new TranslatableText("config.mmic.gridColor.tip"))
				.setSaveConsumer(i -> Client.gridColor = i)
				.build());

			builder.setSavingRunnable(Client::saveConfig);

			return builder.build();
		};
	}
}
