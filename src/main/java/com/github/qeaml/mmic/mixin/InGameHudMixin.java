package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.Grid;
import com.github.qeaml.mmic.Keys;

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
import net.minecraft.text.TranslatableText;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Final
	@Shadow
	private MinecraftClient client;

	private boolean fullbright;
	private double oldGamma;

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V")
	private void injectRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		handleGammaKeys();
		for(Grid g: Grid.values()) {
			handleGridKey(g);
			if(g.show) drawGrid(matrices, g);
		}
	}

	private void handleGammaKeys() {
		if(Keys.fullbright.wasJustPressed()) {
			fullbright = !fullbright;
			if(fullbright) {
				oldGamma = client.options.gamma;
				client.options.gamma = 10.0;
			} else {
				client.options.gamma = oldGamma;
			}
			Client.notify(new TranslatableText("other.mmic.toggled_fullbright", Client.onOff(fullbright)));
		}

		if(fullbright) return; // the keys below do not matter to us in fullbright

		if(Keys.gammaInc.wasJustPressed() && client.options.gamma <= 3.0) {
			client.options.gamma += Client.gammaStep;
			Client.notify(new TranslatableText("other.mmic.changed_gamma", Math.round(client.options.gamma * 100)));
		}
		if(Keys.gammaDec.wasJustPressed() && client.options.gamma >= -1.0) {
			client.options.gamma -= Client.gammaStep;
			Client.notify(new TranslatableText("other.mmic.changed_gamma", Math.round(client.options.gamma * 100)));
		}
	}

	private void drawGrid(MatrixStack matrices, Grid g) {
		for(Grid.Rect r: g.rects) {
			int w = this.client.getWindow().getScaledWidth();
			int h = this.client.getWindow().getScaledHeight();
			int ax = (int)((float)w * r.a().x())-1;
			int ay = (int)((float)h * r.a().y())-1;
			int bx = (int)((float)w * r.b().x())+1;
			int by = (int)((float)h * r.b().y())+1;
			DrawableHelper.fill(matrices, ax, ay, bx, by, Client.gridColor);
		}
	}

	private void handleGridKey(Grid g) {
		if(g.toggle.wasJustPressed()) {
			g.show = !g.show;
			Client.notify(new TranslatableText("other.mmic.toggled_grid",
				new TranslatableText("other.mmic.grid."+g.name),
				Client.onOff(g.show)));
		}
	}
}
