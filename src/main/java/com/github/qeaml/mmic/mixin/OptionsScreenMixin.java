package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.gui.MenuScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
  protected OptionsScreenMixin(Text title) {
    super(title);
  }

  @Inject(
    method = "init",
    at = @At("HEAD")
  )
  private void init(CallbackInfo ci) {
    addDrawableChild(ButtonWidget.builder(
      Text.translatable("gui.mmic.menu"),
      (button) -> {
        client.setScreen(new MenuScreen(this));
      })
      .position(10, height - 30)
      .width(60)
      .build());
  }
}
