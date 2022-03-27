package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.Grid;
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

	@Inject(at = @At("HEAD"), method = "render(LMatrixStack;F)V")
	private void injectRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		for(Grid g: Grid.values()) {
			handleKeyBind(g);
			if(g.show) drawGrid(matrices, g);
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

	private void handleKeyBind(Grid g) {
		if(g.toggle.isPressed() && !g.togglePrev) {
			g.show = !g.show;
			client.player.sendMessage(
				new TranslatableText("other.mmic.toggled_grid",
					new TranslatableText("other.mmic.grid."+g.name),
					Client.onOff(g.show)),
				true);
			Client.playClick();
		}
		g.togglePrev = g.toggle.isPressed();
	}
}
