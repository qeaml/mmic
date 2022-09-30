package com.github.qeaml.mmic.gui;

import java.util.function.Consumer;

import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;
import com.github.qeaml.mmic.gui.widgets.ClickableWidgetList;
import com.github.qeaml.mmic.gui.widgets.OptionButtonWidget;
import com.github.qeaml.mmic.gui.widgets.OptionDoubleSliderWidget;
import com.github.qeaml.mmic.gui.widgets.OptionIntegerSliderWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public abstract class ConfigScreen extends Screen {
  protected Screen parent;

  protected ConfigScreen(Screen parent, Text title) {
    super(title);
    this.parent = parent;
  }

  protected abstract ClickableWidget[] widgets();
  protected abstract void onExit();

  @Override
  protected void init() {
    addDrawableChild(new ClickableWidgetList(
      0, 40,
      this.width,
      2,
      widgets()));

    addDrawableChild(new ButtonWidget(
      width / 2 - 100, height / 6 + 168,
      200, 20,
      ScreenTexts.DONE,
    (button) -> {
      onExit();
      client.setScreen(parent);
    }));
  }

  private static TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    renderBackground(matrices);
    drawCenteredText(matrices, textRenderer, title, width / 2, 20, 0xFFFFFFFF);
    super.render(matrices, mouseX, mouseY, delta);
  }

  public static <T> OptionButtonWidget<T> optionButton(Option<T> opt, Consumer<Option<T>> action) {
    return new OptionButtonWidget<>(
      0, 0,
      200, 20,
      opt,
      action);
  }

  public static OptionDoubleSliderWidget optionDoubleSlider(Option<Double> opt) {
    return new OptionDoubleSliderWidget(
      0, 0,
      200, 20,
      opt);
  }

  public static OptionIntegerSliderWidget optionIntegerSlider(Option<Integer> opt) {
    return new OptionIntegerSliderWidget(
      0, 0,
      200, 20,
      opt);
  }

  protected ButtonWidget optionColorRGB(Option<Color> opt) {
    return new ButtonWidget(
      0, 0,
      200, 20,
      Text.translatable("gui.mmic.submenu", opt.title),
      (button) -> client.setScreen(new OptionRGBScreen(this, opt)));
  }

  protected ButtonWidget optionColorARGB(Option<Color> opt) {
    return new ButtonWidget(
      0, 0,
      200, 20,
      Text.translatable("gui.mmic.submenu", opt.title),
      (button) -> client.setScreen(new OptionRGBAScreen(this, opt)));
  }

  public static void toggle(Option<Boolean> opt) {
    opt.set(!opt.get());
  }
}
