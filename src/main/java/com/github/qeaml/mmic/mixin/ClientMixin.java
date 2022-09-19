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
		method = "tick()V",
		at = @At("HEAD")
	)
	private void onTick(CallbackInfo ci)
	{
		Client.tick();
	}

	@Inject(
		method = "stop",
		at = @At("HEAD")
	)
	private void onStop(CallbackInfo ci) {
		Client.stop();
	}
}
