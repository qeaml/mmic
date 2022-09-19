package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.PlaytimeScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
  protected TitleScreenMixin(Text title) {
    super(title);
  }

  @Inject(
    method = "init",
    at = @At("HEAD")
  )
  private void init(CallbackInfo ci) {
    addDrawableChild(new ButtonWidget(20, height - 40, 98, 20, Text.translatable("gui.mmic.sessions"), button -> {
      client.setScreen(new PlaytimeScreen(this));
    }));
  }
}
