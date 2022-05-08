package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;

@Mixin(ClientPlayNetworkHandler.class)
public class NetworkHandlerMixin {
	@Inject(
		method = "sendPacket(Lnet/minecraft/network/Packet;)V",
		at = @At("Head"),
		cancellable = true
	)
	private void onSendPacket(Packet<?> packet, CallbackInfo ci)
	{
		if(Client.lag) ci.cancel();
	}
}
