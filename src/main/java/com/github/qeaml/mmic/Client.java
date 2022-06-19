package com.github.qeaml.mmic;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.random.Random;

import com.github.qeaml.mmic.mixin.GammaAccessor;

public class Client implements ClientModInitializer {
	public static final Logger log = LoggerFactory.getLogger("mmic");
	private static MinecraftClient mc = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {
		log.info("Hello world");

		Config.load();

		var visuals = "key.categories.mmic.visuals";
		Keys.gammaInc = new Bind("key.mmic.gammaInc", GLFW.GLFW_KEY_RIGHT_BRACKET, visuals);
		Keys.gammaDec = new Bind("key.mmic.gammaDec", GLFW.GLFW_KEY_LEFT_BRACKET, visuals);
		Keys.fullbright = new Bind("key.mmic.fullbright", GLFW.GLFW_KEY_APOSTROPHE, visuals);
		Keys.lagSwitch = new Bind("key.mmic.lagSwitch", GLFW.GLFW_KEY_BACKSLASH, KeyBinding.GAMEPLAY_CATEGORY);

		for(Grid g: Grid.values()) {
			g.toggle = new Bind("other.mmic.grid."+g.name, g.key, "key.categories.mmic.grids");
		}

		ClientPickBlockGatherCallback.EVENT.register((player, hitResult) -> {
			if(hitResult.getType() != HitResult.Type.BLOCK)
				return ItemStack.EMPTY;
			var pos = ((BlockHitResult)hitResult).getBlockPos();
			var block = player.world.getBlockState(pos);
			log.info("Hit "+block.toString()+" at "+pos.toShortString());
			MinecraftServer server = MinecraftClient.getInstance().getServer();
			if(server == null)
				return ItemStack.EMPTY;
			var stax = Block.getDroppedStacks(block, server.getWorld(player.world.getRegistryKey()), pos, null);
			for(ItemStack i: stax)
				if(player.getInventory().contains(i))
					return i;
			return ItemStack.EMPTY;
		});
	}

	public static void tick()
	{
		var p = mc.getProfiler();
		p.push("mmicTick");
		if(Keys.lagSwitch.wasJustPressed())
			State.toggleLag();
		if(Keys.fullbright.wasJustPressed())
			State.toggleFullbright();
		if(State.fullbright && mc.options.getGamma().getValue() != 10.0)
			State.toggleFullbright();
		if(!State.fullbright)
		{
			if(Keys.gammaInc.wasJustPressed() && mc.options.getGamma().getValue() < 3.0) {
				changeGamma(Config.gammaStep);
			}
			if(Keys.gammaDec.wasJustPressed() && mc.options.getGamma().getValue() > 0.0) {
				changeGamma(-Config.gammaStep);
      }
		}
		for(var g: Grid.values())
			if(g.toggle.wasJustPressed())
			{
				g.show = !g.show;
				notify(Text.translatable("other.mmic.toggled_grid",
					Text.translatable("other.mmic.grid."+g.name), onOff(g.show)));
			}
		p.pop();
	}

	public static Text onOff(boolean on) {
		var tkey = "other.mmic." + (on ? "on" : "off");
		var color = on ? Formatting.GREEN : Formatting.RED;
		var style = Style.EMPTY.withColor(color);
		return Text.translatable(tkey).setStyle(style);
	}

	public static void playClick() {
		mc.getSoundManager().play(
			new PositionedSoundInstance(
				SoundEvents.UI_BUTTON_CLICK,
				SoundCategory.BLOCKS,
				.25f, 1f,
				Random.create(),
        0, 0, 0));
	}

	public static void notify(Text message)
	{
		playClick();
		if(mc.currentScreen == null)
			mc.player.sendMessage(message, true);
		else
			SystemToast.show(mc.getToastManager(),
				SystemToast.Type.PERIODIC_NOTIFICATION,
				Text.of("MMIC"),
				message);
		log.info(message.getString());
	}

  private static void changeGamma(double amt) {
    var opt = mc.options.getGamma();
    var gamma = opt.getValue() + amt;
    var acc = (GammaAccessor)(Object)opt;
    acc.setValueBypass(gamma);
    acc.getCallback().accept(gamma);
    notify(Text.translatable("other.mmic.changed_gamma", Math.round(gamma * 100)));
  }
}
