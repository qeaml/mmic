package com.github.qeaml.mmic.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Sessions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {
  @Inject(
    method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V",
    at = @At("HEAD")
  )
  private void onConnect(MinecraftClient client, ServerAddress address, @Nullable ServerInfo serverInfo, CallbackInfo ci) {
    Sessions.startServer(address.getAddress());
  }
}
