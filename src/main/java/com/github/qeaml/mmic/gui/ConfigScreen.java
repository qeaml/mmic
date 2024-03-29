package com.github.qeaml.mmic.gui;

import java.util.function.Consumer;

import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;
import com.github.qeaml.mmic.gui.widgets.ClickableWidgetList;
import com.github.qeaml.mmic.gui.widgets.OptionButtonWidget;
import com.github.qeaml.mmic.gui.widgets.OptionDoubleSliderWidget;
import com.github.qeaml.mmic.gui.widgets.OptionIntegerSliderWidget;
import com.github.qeaml.mmic.gui.widgets.OptionRGBAButtonWidget;
import com.github.qeaml.mmic.gui.widgets.OptionRGBButtonWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public abstract class ConfigScreen extends Screen {
  private static final Text RESET_TEXT = Text.translatable("gui.mmic.config.reset");

  protected Screen parent;
  protected boolean cancellable;

  protected ConfigScreen(Screen parent, Text title, boolean canCancel) {
    super(title);
    this.parent = parent;
    this.cancellable = canCancel;
  }

  protected abstract ClickableWidget[] widgets();

  protected void prep() {}

  protected void cancel() {}

  protected void done() {}

  @Override
  protected void init() {
    addDrawableChild(new ClickableWidgetList(
      client,
      this.width,
      this.height,
      32, this.height-43,
      2,
      widgets()));

    if(cancellable) {
      addDrawableChild(ButtonWidget.builder(
        ScreenTexts.CANCEL,
        (button) -> {
          cancel();
          client.setScreen(parent);
        })
        .position(width/2 - 154, height-27)
        .build());
      addDrawableChild(ButtonWidget.builder(
        ScreenTexts.DONE,
        (button) -> {
          done();
          client.setScreen(parent);
        })
        .position(width/2 + 4, height-27)
        .build());
    } else {
      addDrawableChild(ButtonWidget.builder(
        ScreenTexts.DONE,
        (button) -> {
          done();
          client.setScreen(parent);
        })
        .width(200)
        .position(width/2 - 100, height-27)
        .build());
    }

    prep();
  }

  private static TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    super.render(matrices, mouseX, mouseY, delta);
    drawCenteredText(matrices, textRenderer, title, width / 2, 20, 0xFFFFFFFF);
    drawCenteredText(matrices, textRenderer, RESET_TEXT, width/2, height-27-textRenderer.fontHeight-2, 0xFFFFFFFF);
  }

  protected static final int BUTTON_WIDTH = 150;

  protected <T> OptionButtonWidget<T> optionButton(Option<T> opt, Consumer<Option<T>> action) {
    return new OptionButtonWidget<>(
      0, 0,
      BUTTON_WIDTH, 20,
      opt,
      action);
  }

  protected OptionDoubleSliderWidget optionDoubleSlider(Option<Double> opt) {
    return new OptionDoubleSliderWidget(
      0, 0,
      BUTTON_WIDTH, 20,
      opt);
  }

  protected OptionIntegerSliderWidget optionIntegerSlider(Option<Integer> opt) {
    return new OptionIntegerSliderWidget(
      0, 0,
      BUTTON_WIDTH, 20,
      opt);
  }

  protected ButtonWidget optionColorRGB(Option<Color> opt) {
    return new OptionRGBButtonWidget(
      0, 0,
      BUTTON_WIDTH, 20,
      opt, this);
  }

  protected ButtonWidget optionColorARGB(Option<Color> opt) {
    return new OptionRGBAButtonWidget(
      0, 0,
      BUTTON_WIDTH, 20,
      opt, this);
  }

  public static <T> Text optionButtonText(Option<T> opt) {
    return optionButtonText(opt, opt.display(opt.get()));
  }

  public static <T> Text optionButtonText(Option<T> opt, Text txt) {
    return MutableText
      .of(txt.getContent())
      .setStyle(Style.EMPTY.withItalic(!opt.get().equals(opt.defaultValue)));
  }

  public static void toggle(Option<Boolean> opt) {
    opt.set(!opt.get());
  }
}
