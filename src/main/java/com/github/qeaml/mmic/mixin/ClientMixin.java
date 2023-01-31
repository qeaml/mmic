package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.Sessions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(MinecraftClient.class)
public abstract class ClientMixin {
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

  @Inject(
    method = "cleanUpAfterCrash",
    at = @At("HEAD")
  )
  private void crash(CallbackInfo ci) {
    Client.stop();
  }

  @Inject(
    method = "startIntegratedServer",
    at = @At("HEAD")
  )
  private void world(String levelName, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci) {
    Sessions.startWorld(levelName);
  }

  @Inject(
    method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V",
    at = @At("HEAD")
  )
  private void disconnect(Screen screen, CallbackInfo ci) {
    if(Client.mc.getNetworkHandler() != null)
      Sessions.endSubSession();
  }
}
