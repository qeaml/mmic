package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class ClientMixin {
	@Inject(
		method = "render(Z)V",
		at = @At("HEAD")
	)
	private void onRender(boolean tick, CallbackInfo ci)
	{
		if(tick)
			Client.tick();
	}
}
