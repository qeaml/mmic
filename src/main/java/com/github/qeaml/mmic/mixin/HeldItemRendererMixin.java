package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
  @Shadow
  private MinecraftClient client;
  @Shadow
  private ItemStack mainHand;
  @Shadow
  private float prevEquipProgressMainHand;
  @Shadow
  private float equipProgressMainHand;
  @Shadow
  private ItemStack offHand;
  @Shadow
  private float prevEquipProgressOffHand;
  @Shadow
  private float equipProgressOffHand;

  @Inject(method="updateHeldItems()V", at=@At("TAIL"), cancellable=true)
  private void updateHeldItems(CallbackInfo ci) {
    if(Client.config.staticHand.get()) {
      prevEquipProgressMainHand = 1;
      equipProgressMainHand = 1;
      prevEquipProgressOffHand = 1;
      equipProgressOffHand = 1;

      ItemStack mainHandStack = client.player.getMainHandStack();
      if(!ItemStack.areEqual(mainHandStack, mainHand)) {
        mainHand = mainHandStack;
      }

      ItemStack offHandStack = client.player.getOffHandStack();
      if(!ItemStack.areEqual(offHandStack, offHand)) {
        offHand = offHandStack;
      }
    }
  }
}
