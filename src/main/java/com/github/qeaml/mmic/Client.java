package com.github.qeaml.mmic;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class Client implements ClientModInitializer {
	public static final Logger log = LoggerFactory.getLogger("mmic");

	@Override
	public void onInitializeClient() {
		log.info("Hello world");

		Config.load();

		var visuals = "key.categories.mmic.visuals";
		Keys.gammaInc = new Bind("key.mmic.gammaInc", GLFW.GLFW_KEY_RIGHT_BRACKET, visuals);
		Keys.gammaDec = new Bind("key.mmic.gammaDec", GLFW.GLFW_KEY_LEFT_BRACKET, visuals);
		Keys.fullbright = new Bind("key.mmic.fullbright", GLFW.GLFW_KEY_APOSTROPHE, visuals);

		for(Grid g: Grid.values()) {
			g.toggle = new Bind("other.mmic.grid."+g.name, g.key, "key.categories.mmic.grids");
		}

		ClientPickBlockGatherCallback.EVENT.register((player, hitResult) -> {
			if(hitResult.getType() != HitResult.Type.BLOCK)
				return ItemStack.EMPTY;
			var hit = hitResult.getPos();
			var pos = new BlockPos(hit.x, hit.y, hit.z);
			if(player.getHorizontalFacing() == Direction.WEST)
				pos = pos.subtract(new Vec3i(1, 0, 0));
			if(player.getHorizontalFacing() == Direction.NORTH)
				pos = pos.subtract(new Vec3i(0, 0, 1));
			// if(hit.y == Math.floor(hit.y)) {
			// 	if(player.getPitch() > 0)
			// 		pos = pos.subtract(new Vec3i(0, 1, 0));
			// 	if(player.getPitch() < 0)
			// 		pos = pos.add(new Vec3i(0, 1, 0));
			// }
			var block = player.world.getBlockState(pos);
			log.info("Hit "+block.toString()+" at "+pos.toShortString()+", exact "+hit.toString());
			MinecraftServer server = MinecraftClient.getInstance().getServer();
			if(server == null)
				server = player.getServer();
			var stax = Block.getDroppedStacks(block, server.getWorld(player.world.getRegistryKey()), pos, null);
			for(ItemStack i: stax)
				if(player.getInventory().contains(i))
					return i;
			return ItemStack.EMPTY;
		});
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
				.25f, 1f,
				0, 0, 0));
	}
}
