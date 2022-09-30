package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;
import com.github.qeaml.mmic.gui.widgets.GenericSliderWidget;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class OptionRGBScreen extends Screen {
  private Screen parent;
  private Option<Color> opt;
  private double red, green, blue;
  
  public OptionRGBScreen(Screen parent, Option<Color> opt) {
    super(opt.title);
    this.parent = parent;
    this.opt = opt;
    var clr = opt.get();
    this.red = clr.redFloat();
    this.green = clr.greenFloat();
    this.blue = clr.blueFloat();
  }

  @Override
  protected void init() {
    addDrawableChild(new GenericSliderWidget(
      width/2-100, height/6*5-65,
      200, 20,
      redText(red),
      red,
      this::redText,
      v -> red = v));
    addDrawableChild(new GenericSliderWidget(
      width/2-100, height/6*5-45,
      200, 20,
      greenText(green),
      green,
      this::greenText,
      v -> green = v));
    addDrawableChild(new GenericSliderWidget(
      width/2-100, height/6*5-25,
      200, 20,
      blueText(blue),
      blue,
      this::blueText,
      v -> blue = v));

    addDrawableChild(new ButtonWidget(
      width / 2 - 200, height/6*5+10,
      200, 20,
      ScreenTexts.CANCEL,
    (button) -> {
      client.setScreen(new ConfirmScreen(
        ok -> {if(ok) client.setScreen(parent);},
        ScreenTexts.CANCEL,
        Text.translatable("gui.mmic.cancel.confirm")));
    }));
    addDrawableChild(new ButtonWidget(
      width / 2, height/6*5+10,
      200, 20,
      ScreenTexts.DONE,
    (button) -> {
      opt.set(new Color(red, green, blue));
      client.setScreen(parent);
    }));
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    renderBackground(matrices);
    int clr = ((int)(red*255)<<16) | ((int)(green*255)<<8) | (int)(blue*255);
    fill(matrices,
      width/2-105, height/6,
      width/2+105, height/6*5,
      0x40000000|clr);
    fill(matrices,
      width/2-100, height/6+5,
      width/2+100, height/6*5-75,
      0xFF000000|clr);
    drawCenteredText(matrices, textRenderer, title, width / 2, 20, 0xFFFFFFFF);
    super.render(matrices, mouseX, mouseY, delta);
  }

  private Text redText(double red) {
    return Text.translatable("gui.mmic.color.red", Math.round(red*100));
  }

  private Text greenText(double green) {
    return Text.translatable("gui.mmic.color.green", Math.round(green*100));
  }

  private Text blueText(double blue) {
    return Text.translatable("gui.mmic.color.blue", Math.round(blue*100));
  }
}
