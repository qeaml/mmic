package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Config;
import com.github.qeaml.mmic.Grid;
import com.github.qeaml.mmic.State;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3f;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Final
	@Shadow
	private MinecraftClient client;
	@Shadow
	private int scaledWidth;
	@Shadow
	private int scaledHeight;
	@Shadow
	private Text overlayMessage;
	@Shadow
	private int overlayRemaining;
	@Shadow
	private boolean overlayTinted;

	@Invoker
	protected abstract boolean callShouldRenderSpectatorCrosshair(HitResult target);

	@Inject(
		at = @At("HEAD"),
		method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V"
	)
	private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		for(Grid g: Grid.values()) {
			if(!g.show) continue;
			for(Grid.Rect r: g.rects) {
				int ax = (int)((float)scaledWidth * r.a().x())-1;
				int ay = (int)((float)scaledHeight * r.a().y())-1;
				int bx = (int)((float)scaledWidth * r.b().x())+1;
				int by = (int)((float)scaledHeight * r.b().y())+1;
				DrawableHelper.fill(matrices, ax, ay, bx, by, Config.gridColor);
			}
		}

		int y = scaledHeight - client.textRenderer.fontHeight - 5;
		if(State.fullbright) {
			client.textRenderer.drawWithShadow(matrices, "FULLBRIGHT", 5, y, 0x80FFFFFF);
			y -= client.textRenderer.fontHeight;
		}
		if(State.lagging)
		{
			client.textRenderer.drawWithShadow(matrices, "LAGGING", 5, y, 0x80FFFFFF);
			y -= client.textRenderer.fontHeight;
		}
	}

	/*
	Commented out due to bugs:
		1. Attack indicator does not show up while an attack is possible. (i.e.
		   hovering over something)
		2. The attack indicator does not have the proper blending function. It is
		   reset back to the default somewhere along the path, and I could not find
		   where or why. 
	*/

	//@Inject(
	//	at = @At("HEAD"),
	//	method = "renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V",
	//	cancellable = true
	//)
	//private void hijackRenderCrosshair(MatrixStack matrices, CallbackInfo ci) {
	//	if(client.options.hudHidden) return;
	//	if(!client.options.getPerspective().isFirstPerson()) return;
	//	if(client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR && !callShouldRenderSpectatorCrosshair(client.crosshairTarget)) return;
	//
	//	// -- Crosshair --
	//	if(client.options.debugEnabled) {
	//		renderDebugXhair();
	//		return;
	//	} else {
	//		RenderSystem.blendFuncSeparate(SrcFactor.ONE_MINUS_DST_COLOR, DstFactor.ONE_MINUS_SRC_COLOR, SrcFactor.ONE, DstFactor.ZERO);
	//		if(Config.dotXhair)
	//			renderDotXhair(matrices);
	//		else
	//			renderDefaultXhair(matrices);
	//	}
	//
	//	// -- Attack Indicator --
	//	if(client.options.getAttackIndicator().getValue() != AttackIndicator.CROSSHAIR) return;
	//
	//	var cooldown = client.player.getAttackCooldownProgress(0f);
	//	var opp = false;
	//	if(client.targetedEntity != null &&
	//		 client.targetedEntity instanceof LivingEntity &&
	//		 cooldown >= 1f)
	//	{
	//		opp = client.player.getAttackCooldownProgressPerTick() > 5f
	//		   && client.targetedEntity.isAlive();
	//	}
	//
	//	int indX = scaledWidth/2-8;
	//	int indY = scaledHeight/2+11;
	//	if(opp)
	//		drawTexture(matrices, indX, indY, 68, 94, 16, 16);
	//	else if(cooldown < 1f) {
	//		int progH = (int)(cooldown * 17f);
	//		drawTexture(matrices, indX, indY, 36, 94, 16, 4);
	//		drawTexture(matrices, indX, indY, 52, 94, progH, 4);
	//	}
	//
	//	ci.cancel();
	//}

	/*
	The methods below are only ever called by hijackRenderCrosshair. Since it's
	commented out, these will never be called.
	*/

	/** Renders the debug crosshair. (Visible when the debug HUD menu is visible) */
	@Unique
	private void renderDebugXhair() {
		var cam = client.gameRenderer.getCamera();
		var matrices = RenderSystem.getModelViewStack();
		matrices.push();
		int zOffset = ((DrawableHelper)(Object)this).getZOffset();
		matrices.translate(scaledWidth/2, scaledHeight/2, zOffset);
		matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(cam.getPitch()));
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(cam.getYaw()));
		matrices.scale(-1, -1, -1);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.renderCrosshair(10);
		matrices.pop();
		RenderSystem.applyModelViewMatrix();
	}

	/** Renders a small dot in the middle of the screen as a crosshair. */
	@Unique
	private void renderDotXhair(MatrixStack matrices) {
		float x1 = (float)(scaledWidth)/2f,
		      x2 = x1+1,
		      y1 = (float)(scaledHeight)/2f,
		      y2 = y1+1;

		// most of this code is the decompiled source of DrawableHelper#fill
		var matrix = matrices.peek().getPositionMatrix();
		var bufferBuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(255, 255, 255, 255).next();
		BufferRenderer.drawWithShader(bufferBuilder.end());
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	/** Renders the default crosshair, as provided by the current resource pack. */
	@Unique
	private void renderDefaultXhair(MatrixStack matrices) {
		drawTexture(matrices, (scaledWidth-15)/2, (scaledHeight-15)/2, 0, 0, 15, 15);
	}

	/** Shorthand for the DrawableHeler#drawTexture method. */
	@Unique
	private void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
		((DrawableHelper)(Object)this).drawTexture(matrices, x, y, u, v, width, height);
	}
}
