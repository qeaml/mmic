package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.qeaml.mmic.Client;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;

@Mixin(ChatHud.class)
public class ChatHudMixin {
  @Redirect(
    method = "render(Lnet/minecraft/client/util/math/MatrixStack;I)V",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"
    )
  )
  private MessageIndicator redirectIndicator0(ChatHudLine.Visible that) {
    if(Client.config.noChatIndicators.get())
      return null;
    return that.indicator();
  }

  @Redirect(
    method = "getIndicatorAt(DD)Lnet/minecraft/client/gui/hud/MessageIndicator;",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"
    )
  )
  private MessageIndicator redirectIndicator1(ChatHudLine.Visible that) {
    if(Client.config.noChatIndicators.get())
      return null;
    return that.indicator();
  }
}
