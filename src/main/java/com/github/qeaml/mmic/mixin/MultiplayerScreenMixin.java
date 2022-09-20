package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Sessions;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin {
  @Inject(
    method = "connect(Lnet/minecraft/client/network/ServerInfo;)V",
    at = @At("HEAD")
  )
  private void connect(ServerInfo server, CallbackInfo ci) {
    Sessions.startServer(server.address);
  }
}
