package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.Config;
import com.github.qeaml.mmic.Grid;
import com.github.qeaml.mmic.Keys;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

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

	public final String fullbrightText = "FULLBRIGHT";
	private @Unique boolean fullbright;
	private @Unique double oldGamma;

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V")
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

		if(fullbright) {
			int y = scaledHeight - client.textRenderer.fontHeight - 5;
			client.textRenderer.drawWithShadow(matrices, fullbrightText, 5, y, 0x80FFFFFF);
		}
	}

	@Inject(at = @At("TAIL"), method = "tick()V")
	private void onTick(CallbackInfo ci) {
		for(Grid g: Grid.values()) {
			if(g.toggle.wasJustPressed()) {
				g.show = !g.show;
				notify(new TranslatableText("other.mmic.toggled_grid",
					new TranslatableText("other.mmic.grid."+g.name),
					Client.onOff(g.show)));
			}
		}

		if(Keys.fullbright.wasJustPressed()) {
			fullbright = !fullbright;
			if(fullbright) {
				oldGamma = client.options.gamma;
				client.options.gamma = 10.0;
			} else {
				client.options.gamma = oldGamma;
			}
			notify(new TranslatableText("other.mmic.toggled_fullbright", Client.onOff(fullbright)));
		}

		if(fullbright && client.options.gamma != 10.0) {
			fullbright = false;
			client.options.gamma = oldGamma;
		}

		if(fullbright) return; // the keys below do not matter to us in fullbright

		if(Keys.gammaInc.wasJustPressed() && client.options.gamma <= 3.0) {
			client.options.gamma = Math.min(client.options.gamma + Config.gammaStep, 3.0);
			notify(new TranslatableText("other.mmic.changed_gamma", Math.round(client.options.gamma * 100)));
		}
		if(Keys.gammaDec.wasJustPressed() && client.options.gamma >= -1.0) {
			client.options.gamma = Math.max(client.options.gamma - Config.gammaStep, -1.0);
			notify(new TranslatableText("other.mmic.changed_gamma", Math.round(client.options.gamma * 100)));
		}
	}

	@Unique
	private void notify(Text text) {
		overlayMessage = text;
		overlayRemaining = 30;
		overlayTinted = false;
		Client.playClick();
	}
}
