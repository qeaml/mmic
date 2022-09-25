package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Config;
import com.github.qeaml.mmic.State;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    if(State.lagging)
    {
      switch (Config.lagType) {
      case CLOG:
        State.packets.add(packet);
      case BLOCK:
        ci.cancel();
        break;
      case LOSSY_CLOG:
        if(rand.nextBoolean() && rand.nextBoolean()) {
          State.packets.add(packet);
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
