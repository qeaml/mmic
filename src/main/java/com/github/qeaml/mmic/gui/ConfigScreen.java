package com.github.qeaml.mmic.gui;

import java.util.function.Consumer;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;
import com.github.qeaml.mmic.gui.widgets.ClickableWidgetList;
import com.github.qeaml.mmic.gui.widgets.OptionButtonWidget;
import com.github.qeaml.mmic.gui.widgets.OptionColorTextFieldWidget;
import com.github.qeaml.mmic.gui.widgets.OptionDoubleSliderWidget;
import com.github.qeaml.mmic.gui.widgets.OptionIntegerSliderWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
  private Screen parent;

  public ConfigScreen(Screen parent) {
    super(Text.translatable("gui.mmic.config"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    var that = this;

    addDrawableChild(new ClickableWidgetList(
      0, 40,
      this.width,
      2,
      new ButtonWidget(
        0, 0,
        200, 20,
        Text.translatable("config.mmic.game"),
      (button) -> {
        client.setScreen(new GameplayConfigScreen(that));
      }),
      new ButtonWidget(
        10, 10,
        200, 20,
        Text.translatable("config.mmic.cosm"),
      (button) -> {
        client.setScreen(new CosmeticConfigScreen(that));
      }),
      new ButtonWidget(
        10, 10,
        200, 20,
        Text.translatable("config.mmic.other"),
      (button) -> {
        client.setScreen(new OtherConfigScreen(that));
      })
    ));

    addDrawableChild(new ButtonWidget(
      width / 2 - 100, height / 6 + 168,
      200, 20,
      ScreenTexts.DONE,
    (button) -> {
      Client.config.save();
      client.setScreen(parent);
    }));
  }

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

  public static OptionColorTextFieldWidget optionColorText(Option<Color> opt) {
    return new OptionColorTextFieldWidget(
      MinecraftClient.getInstance().textRenderer,
      0, 0,
      200, 20,
      opt);
  }

  public static void toggle(Option<Boolean> opt) {
    opt.set(!opt.get());
  }
}
