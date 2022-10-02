package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.util.math.random.Random;

@Mixin(ClientPlayNetworkHandler.class)
public class NetworkHandlerMixin {
  private static Random rand = Random.createLocal();

  @Inject(
    method = "sendPacket(Lnet/minecraft/network/Packet;)V",
    at = @At("Head"),
    cancellable = true
  )
  private void onSendPacket(Packet<?> packet, CallbackInfo ci)
  {
    if(Client.isLagging())
    {
      switch (Client.config.lagType.get()) {
      case CLOG:
        Client.clogPacket(packet);
      case BLOCK:
        ci.cancel();
        break;
      case LOSSY_CLOG:
        if(rand.nextBoolean() && rand.nextBoolean()) {
          Client.clogPacket(packet);
          ci.cancel();
        } 
        break;
      case LOSSY_BLOCK:
        if(rand.nextBoolean() && rand.nextBoolean()) {
          ci.cancel();
        }
        break;
      }
    }
  }
}
