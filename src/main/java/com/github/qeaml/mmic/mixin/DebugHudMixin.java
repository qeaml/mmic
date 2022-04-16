package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tag.TagKey;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
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

		leftLines = 0;
		leftLine(matrices, "FPS: %d (%d ms)", fps, frametime);

		var r = Runtime.getRuntime();
		leftLine(matrices, "Memory: %.02f/%.02fMiB",
			toMiB(r.totalMemory() - r.freeMemory()),
			toMiB(r.maxMemory()));

		leftLine(matrices, "Client: %s %s (%s)",
			ClientBrandRetriever.getClientModName(),
			client.getGameVersion(),
			client.getVersionType());

		IntegratedServer is;
		if((is = client.getServer()) != null)
			leftLine(matrices, "Server: Integrated @ %.2fms", is.getTickTime());
		else
			leftLine(matrices, "Server: %s", client.player.getServerBrand());

		leftLine(matrices, "XYZ: %.2f/%.2f/%.2f", cam.getX(), cam.getY(), cam.getZ());
		leftLine(matrices, "Rotation: %.2f/%.2f (%s)", cam.getYaw(), cam.getPitch(), cam.getHorizontalFacing());
		leftLine(matrices, "Chunk: %d/%d", (int)(cam.getX() / 16.0), (int)(cam.getZ() / 16.0));

		if(cam.getY() >= client.world.getBottomY() && cam.getY() < client.world.getTopY())
			leftLine(matrices, "Biome: %s", getBiomeString(client.world.getBiome(cam.getBlockPos())));
		else
			leftLine(matrices, "Out of world.");

		if(client.crosshairTarget.getType() == HitResult.Type.BLOCK)
		{
			var pos = ((BlockHitResult)client.crosshairTarget).getBlockPos();
			var state = client.player.getWorld().getBlockState(pos);
			var info = String.format("%s at %s", Registry.BLOCK.getId(state.getBlock()), pos.toShortString());
			var bgwidth = textRenderer.getWidth(info);
			var sw = client.getWindow().getScaledWidth();
			var sh = client.getWindow().getScaledHeight();
			var props = state.getProperties();
			var tags = state.streamTags().toList();
			for(TagKey<Block> tag: tags)
			{
				int w = textRenderer.getWidth("#"+tag.id().toString());
				if(w > bgwidth)
					bgwidth = w;
			}
			fillGradient
			(
				matrices,
				sw/2
					-bgwidth/2
					-n,
				sh/2
					+textRenderer.fontHeight
					+n,
				sw/2
					+bgwidth/2
					+n,
				sh/2
					+textRenderer.fontHeight
					+textRenderer.fontHeight
					+(props.size()+tags.size())*textRenderer.fontHeight
					+textRenderer.fontHeight
					+n,
				0x801F1F1f, 0x800F0F0F
			);
			centerLines = 0;
			centerLine(matrices, info);
			props.forEach(prop -> centerLine(matrices, "%s=%s", prop.getName(), state.get(prop)));
			tags.forEach(tag -> centerLine(matrices, "#%s", tag.id()));
		}
		else if(client.crosshairTarget.getType() == HitResult.Type.ENTITY)
		{
			var ent = ((EntityHitResult)client.crosshairTarget).getEntity();
			var info = String.format("%s at %.2f, %.2f, %.2f",
				Registry.ENTITY_TYPE.getId(ent.getType()),
				ent.getPos().x, ent.getPos().y, ent.getPos().z);
			var infowidth = textRenderer.getWidth(info);
			var sw = client.getWindow().getScaledWidth();
			var sh = client.getWindow().getScaledHeight();
			//TODO: tags here too?
			int lines = 0;
			if(ent instanceof LivingEntity)
				lines = 1;
			fillGradient
			(
				matrices,
				sw/2
					-infowidth/2
					-n,
				sh/2
					+textRenderer.fontHeight
					+n,
				sw/2
					+infowidth/2
					+n,
				sh/2
					+textRenderer.fontHeight
					+textRenderer.fontHeight
					+lines*textRenderer.fontHeight
					+textRenderer.fontHeight
					+n,
				0x801F1F1f, 0x800F0F0F
			);
			centerLines = 0;
			centerLine(matrices, info);
			if(ent instanceof LivingEntity lent)
				centerLine(matrices, "Health: %.2f/%.2f",
					lent.getHealth(), lent.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH));
		}

		ci.cancel();
	}

	private @Unique int leftLines = 0;

	@Unique
	private void leftLine(MatrixStack matrices, String format, Object... args) {
		var immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		((TextRendererAccessor)textRenderer).internalDraw(
			String.format(format, args),
			textRenderer.fontHeight,
			textRenderer.fontHeight*leftLines+textRenderer.fontHeight,
			0xFFFFFFFF,
			false,
			matrices.peek().getPositionMatrix(),
			immediate,
			false,
			0x00000000,
			15728880,
			false);
		immediate.draw();
		leftLines++;
	}

	private @Unique int centerLines = 0;

	@Unique
	private void centerLine(MatrixStack matrices, String format, Object... args)
	{
		var immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		var text = String.format(format, args);
		((TextRendererAccessor)textRenderer).internalDraw(
			text,
			client.getWindow().getScaledWidth()/2
				-textRenderer.getWidth(text)/2,
			client.getWindow().getScaledHeight()/2
				+textRenderer.fontHeight*centerLines+textRenderer.fontHeight+textRenderer.fontHeight,
			0xFFFFFFFF,
			false,
			matrices.peek().getPositionMatrix(),
			immediate,
			false,
			0x00000000,
			15728880,
			false);
		immediate.draw();
		centerLines++;
	}

	@Unique
	private double toMiB(long bytes) {
		return (double)bytes / 1024.0 / 1024.0;
	}
}
