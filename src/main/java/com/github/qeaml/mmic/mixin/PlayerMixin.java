package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;

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

  private ChunkPos chunk = new ChunkPos(0, 0);

  @Inject(
    method = "sendMovementPackets",
    at = @At("HEAD")
  )
  private void injectSendMovementPackets(CallbackInfo ci) {
    var p = (ClientPlayerEntity)(Object)this;
    var currentChunk = new ChunkPos(p.getBlockPos());
    if(currentChunk.x == chunk.x && currentChunk.z == chunk.z)
      return;
    chunk = currentChunk;
    Client.setCurrentChunk(chunk);
  }
}
