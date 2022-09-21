package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.Config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
  private Screen parent;

  public MenuScreen(Screen parent) {
    super(Text.translatable("gui.mmic.menu"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    var that = this;

    addDrawableChild(new ButtonWidget(
      width / 2 - 100, 40,
      200, 20,
      Text.translatable("gui.mmic.sessions"),
    (button) -> {
      client.setScreen(new PlaytimeScreen(this));
    }));
    addDrawableChild(new ButtonWidget(
      width / 2 - 100, 60,
      200, 20,
      Text.translatable("gui.mmic.config"),
    (button) -> {
      client.setScreen(new Config().getModConfigScreenFactory().create(that));
    }));

    addDrawableChild(new ButtonWidget(
      width / 2 - 100, height / 6 + 168,
      200, 20,
      ScreenTexts.DONE,
    (button) -> {
      client.setScreen(parent);
    }));
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    renderBackground(matrices);
    drawCenteredText(matrices, textRenderer, title, width / 2, 20, 0xFFFFFFFF);
    super.render(matrices, mouseX, mouseY, delta);
  }
}
