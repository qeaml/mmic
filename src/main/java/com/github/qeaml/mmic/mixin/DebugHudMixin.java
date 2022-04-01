package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;

@Mixin(DebugHud.class)
public class DebugHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Final
	private TextRenderer textRenderer;

	@Invoker("getBiomeString")
	private static String getBiomeString(RegistryEntry<Biome> biome) {
		return null;
	}

	@Inject(
		at = @At("INVOKE"),
		method = "render(Lnet/minecraft/client/util/math/MatrixStack;)V",
		cancellable = true)
	private void hijackRender(MatrixStack matrices, CallbackInfo ci) {
		var cam = client.getCameraEntity();

		line(matrices, 0, client.fpsDebugString);

		var r = Runtime.getRuntime();
		line(matrices, 1, "Memory Usage: %.02f/%.02fMiB",
			toMiB(r.totalMemory() - r.freeMemory()),
			toMiB(r.maxMemory()));

		line(matrices, 2, "%s %s client (%s)",
			ClientBrandRetriever.getClientModName(),
			client.getGameVersion(),
			client.getVersionType());

		IntegratedServer is;
		if((is = client.getServer()) != null)
			line(matrices, 2, "Integrated server @ %.2fms", is.getTickTime());
		else
			line(matrices, 2, "%s server", client.player.getServerBrand());

		line(matrices, 4, "XYZ: %.2f/%.2f/%.2f", cam.getX(), cam.getY(), cam.getZ());
		line(matrices, 5, "Rotation: %.2f/%.2f (%s)", cam.getYaw(), cam.getPitch(), cam.getHorizontalFacing());
		line(matrices, 6, "Chunk: %d/%d", (int)cam.getX() / 16, (int)cam.getZ() / 16);

		if(cam.getY() >= client.world.getBottomY() && cam.getY() < client.world.getTopY())
			line(matrices, 7, "Biome: %s", getBiomeString(client.world.getBiome(cam.getBlockPos())));
		else
			line(matrices, 7, "Out of world.");

		ci.cancel();
	}

	@Unique
	private void line(MatrixStack matrices, int line, String format, Object... args) {
		textRenderer.drawWithShadow(
			matrices, 
			String.format(format, args),
			textRenderer.fontHeight,
			textRenderer.fontHeight*(line+1),
			0xFFFFFFFF);
	}

	@Unique
	private double toMiB(long bytes) {
		return (double)bytes / 1024.0 / 1024.0;
	}
}
