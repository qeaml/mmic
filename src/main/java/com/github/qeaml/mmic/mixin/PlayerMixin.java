package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.Client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

@Mixin(ClientPlayerEntity.class)
public class PlayerMixin {
  @Shadow
  private ClientPlayNetworkHandler networkHandler;

  @Inject(
    method = "swingHand(Lnet/minecraft/util/Hand;)V",
    at = @At("HEAD"),
    cancellable = true
  )
  private void injectSwingHand(Hand hand, CallbackInfo ci)
  {
    if(Client.config.staticHand.get())
    {
      networkHandler.sendPacket(new HandSwingC2SPacket(hand));
      ci.cancel();
    }
  }
}
