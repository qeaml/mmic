package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Sessions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {
  @Inject(
    method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;)V",
    at = @At("HEAD")
  )
  private void onConnect(MinecraftClient client, ServerAddress address, CallbackInfo ci) {
    Sessions.startServer(address.getAddress());
  }
}
