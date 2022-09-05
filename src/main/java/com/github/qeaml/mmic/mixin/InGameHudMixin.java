package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Config;
import com.github.qeaml.mmic.Grid;
import com.github.qeaml.mmic.State;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Mixin(InGameHud.class)
public class InGameHudMixin {
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
}
