package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public class MouseMixin {
  @Inject(method="onMouseScroll(JDD)V", at=@At("HEAD"), cancellable = true)
  private void scroll(long window, double horizontal, double vertical, CallbackInfo ci) {
    if(Client.isZoomed()) {
      if(vertical > 0)
        Client.zoomIn();
      else if(vertical < 0)
        Client.zoomOut();
      ci.cancel();
    }
  }
}
