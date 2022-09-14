package com.github.qeaml.mmic;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Config implements ModMenuApi {
	public static Integer gridColor = 0xFF000000;
	public static Double gammaStep = 0.2;
	public static Boolean miniF3 = false;
	public static Boolean staticHand = false;
	public static Boolean autoplant = false;
	public static Boolean sneakAutoplant = true;
	public static Boolean centeredSigns = false;
	public static Boolean perfectSigns = false;
	public static Boolean dotXhair = false;

	public enum LagType
	{
		BLOCK,
		CLOG,
		LOSSY_BLOCK,
		LOSSY_CLOG
	}
	public static LagType lagType = LagType.BLOCK;

	public static Double zoomFovDiv = 5.0;
	public static Double zoomSensDiv = 2.0;
	public static Boolean zoomSmooth = false;

	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if(!FabricLoader.getInstance().isModLoaded("cloth-config2"))
			return parent -> null;
		return parent -> {
			var builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Text.translatable("config.mmic.title"))
				.setDefaultBackgroundTexture(new Identifier("minecraft", "textures/block/lapis_block.png"));
			var entry = builder.entryBuilder();

			//
			// ─── GAMEPLAY ────────────────────────────────────────────────────
			//

			var game = builder.getOrCreateCategory(Text.translatable("config.mmic.game"));

			game.addEntry(entry.startEnumSelector(
				Text.translatable("config.mmic.lagType"),
				LagType.class,
				lagType)
				.setDefaultValue(LagType.BLOCK)
				.setTooltip(Text.translatable("config.mmic.lagType.tip"))
				.setSaveConsumer(l -> lagType = l)
				.build());
			game.addEntry(entry.startBooleanToggle(
				Text.translatable("config.mmic.autoplant"),
				autoplant)
				.setDefaultValue(false)
				.setTooltip(Text.translatable("config.mmic.autoplant.tip"))
				.setSaveConsumer(b -> autoplant = b)
				.build());
			game.addEntry(entry.startBooleanToggle(
				Text.translatable("config.mmic.sneakAutoplant"),
				sneakAutoplant)
				.setDefaultValue(true)
				.setTooltip(Text.translatable("config.mmic.sneakAutoplant.tip"))
				.setSaveConsumer(b -> sneakAutoplant = b)
				.build());

			//
			// ─── COSMETIC ────────────────────────────────────────────────────
			//

			var cosm = builder.getOrCreateCategory(Text.translatable("config.mmic.cosm"));

			cosm.addEntry(entry.startAlphaColorField(
				Text.translatable("config.mmic.gridColor"),
				gridColor)
				.setDefaultValue(0xFF000000)
				.setTooltip(Text.translatable("config.mmic.gridColor.tip"))
				.setSaveConsumer(i -> gridColor = i)
				.build());
			cosm.addEntry(entry.startDoubleField(
				Text.translatable("config.mmic.gammaStep"),
				gammaStep * 100)
				.setDefaultValue(20)
				.setTooltip(Text.translatable("config.mmic.gammaStep.tip"))
				.setSaveConsumer(d -> gammaStep = d / 100)
				.build());
			cosm.addEntry(entry.startBooleanToggle(
				Text.translatable("config.mmic.miniF3"),
				miniF3)
				.setDefaultValue(false)
				.setTooltip(Text.translatable("config.mmic.miniF3.tip"))
				.setSaveConsumer(b -> miniF3 = b)
				.build());
			cosm.addEntry(entry.startBooleanToggle(
				Text.translatable("config.mmic.staticHand"),
				staticHand)
				.setDefaultValue(false)
				.setTooltip(Text.translatable("config.mmic.staticHand.tip"))
				.setSaveConsumer(b -> staticHand = b)
				.build());
			cosm.addEntry(entry.startDoubleField(
				Text.translatable("config.mmic.zoomFovDiv"),
				zoomFovDiv)
				.setDefaultValue(5)
				.setTooltip(Text.translatable("config.mmic.zoomFovDiv.tip"))
				.setSaveConsumer(d -> zoomFovDiv = d)
				.setMin(1.0)
				.setMax(10.0)
				.build());
			cosm.addEntry(entry.startDoubleField(
				Text.translatable("config.mmic.zoomSensDiv"),
				zoomSensDiv)
				.setDefaultValue(2)
				.setTooltip(Text.translatable("config.mmic.zoomSensDiv.tip"))
				.setSaveConsumer(d -> zoomSensDiv = d)
				.setMin(1.0)
				.setMax(10.0)
				.build());
			cosm.addEntry(entry.startBooleanToggle(
				Text.translatable("config.mmic.zoomSmooth"),
				zoomSmooth)
				.setDefaultValue(false)
				.setTooltip(Text.translatable("config.mmic.zoomSmooth.tip"))
				.setSaveConsumer(b -> zoomSmooth = b)
				.build());
			cosm.addEntry(entry.startBooleanToggle(
				Text.translatable("config.mmic.centeredSigns"),
				centeredSigns)
				.setDefaultValue(false)
				.setTooltip(Text.translatable("config.mmic.centeredSigns.tip"))
				.setSaveConsumer(b -> centeredSigns = b)
				.build());
			cosm.addEntry(entry.startBooleanToggle(
				Text.translatable("config.mmic.perfectSigns"),
				perfectSigns)
				.setDefaultValue(false)
				.setTooltip(Text.translatable("config.mmic.perfectSigns.tip"))
				.setSaveConsumer(b -> perfectSigns = b)
				.build());
			cosm.addEntry(entry.startBooleanToggle(
				Text.translatable("config.mmic.dotXhair"),
				dotXhair)
				.setDefaultValue(false)
				.setTooltip(Text.translatable("config.mmic.dotXhair.tip"))
				.setSaveConsumer(b -> dotXhair = b)
				.build());

			builder.setSavingRunnable(Client.cfgMan::save);

			return builder.build();
		};
	}
}
