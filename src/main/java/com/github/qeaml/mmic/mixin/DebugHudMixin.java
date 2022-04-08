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
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;

@Mixin(DebugHud.class)
public class DebugHudMixin extends DrawableHelper {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Final
	private TextRenderer textRenderer;

	@Unique private long lastInfo;
	@Unique private int framecount;
	@Unique private int frametime;
	@Unique private long fps;

	@Invoker("getBiomeString")
	private static String getBiomeString(RegistryEntry<Biome> biome) {
		return null;
	}

	@Inject(
		at = @At("INVOKE"),
		method = "render(Lnet/minecraft/client/util/math/MatrixStack;)V",
		cancellable = true)
	private void hijackRender(MatrixStack matrices, CallbackInfo ci) {
		if(!client.options.reducedDebugInfo) return;

		framecount++;
		var cam = client.getCameraEntity();

		var n = textRenderer.fontHeight/2;
		fillGradient(
			matrices,
			n, n,
			50*n+n+n+n,
			9*textRenderer.fontHeight+n,
			0x801F1F1f, 0x800F0F0F);

		var delta = System.currentTimeMillis()-lastInfo;
		if(delta >= 100L) {
			fps = 1000 * framecount / delta;
			frametime = (int)(delta/framecount);
			framecount = 0;
			lastInfo = System.currentTimeMillis();
		}
		var perf = 0xFF00FF00;
		if(fps < 20)
			perf = 0xFFFF0000;
		else if(fps < 30)
			perf = 0xFFFF8000;
		else if(fps < 60)
			perf = 0xFFFFFF00;
		fill(
			matrices,
			50*n+n+n+n, n,
			50*n+n+n+n+n, 9*textRenderer.fontHeight+n,
			perf);
		line(matrices, 0, "FPS: %d (%d ms)", fps, frametime);

		var r = Runtime.getRuntime();
		line(matrices, 1, "Memory: %.02f/%.02fMiB",
			toMiB(r.totalMemory() - r.freeMemory()),
			toMiB(r.maxMemory()));

		line(matrices, 2, "Client: %s %s (%s)",
			ClientBrandRetriever.getClientModName(),
			client.getGameVersion(),
			client.getVersionType());

		IntegratedServer is;
		if((is = client.getServer()) != null)
			line(matrices, 3, "Server: Integrated @ %.2fms", is.getTickTime());
		else
			line(matrices, 3, "Server: %s", client.player.getServerBrand());

		line(matrices, 4, "XYZ: %.2f/%.2f/%.2f", cam.getX(), cam.getY(), cam.getZ());
		line(matrices, 5, "Rotation: %.2f/%.2f (%s)", cam.getYaw(), cam.getPitch(), cam.getHorizontalFacing());
		line(matrices, 6, "Chunk: %d/%d", (int)(cam.getX() / 16.0), (int)(cam.getZ() / 16.0));

		if(cam.getY() >= client.world.getBottomY() && cam.getY() < client.world.getTopY())
			line(matrices, 7, "Biome: %s", getBiomeString(client.world.getBiome(cam.getBlockPos())));
		else
			line(matrices, 7, "Out of world.");

		ci.cancel();
	}

	@Unique
	private void line(MatrixStack matrices, int line, String format, Object... args) {
		var immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		((TextRendererAccessor)textRenderer).internalDraw(
			String.format(format, args),
			textRenderer.fontHeight,
			textRenderer.fontHeight*line+textRenderer.fontHeight,
			0xFFFFFFFF,
			false,
			matrices.peek().getPositionMatrix(),
			immediate,
			false,
			0x00000000,
			15728880,
			false);
		immediate.draw();
	}

	@Unique
	private double toMiB(long bytes) {
		return (double)bytes / 1024.0 / 1024.0;
	}
}
