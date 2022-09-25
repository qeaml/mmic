package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.gui.widgets.ClickableWidgetList;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import static com.github.qeaml.mmic.gui.ConfigScreen.*;

import com.github.qeaml.mmic.Client;

public class CosmeticConfigScreen extends Screen {
  private Screen parent;

  public CosmeticConfigScreen(Screen parent) {
    super(Text.translatable("config.mmic.cosm"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    addDrawableChild(new ClickableWidgetList(
      0, 40,
      this.width,
      2,
      optionColorText(Client.config.gridColor),
      optionDoubleSlider(Client.config.gammaStep),
      optionButton(
        Client.config.miniF3,
        ConfigScreen::toggle
      ),
      optionButton(
        Client.config.staticHand,
        ConfigScreen::toggle
      ),
      optionDoubleSlider(Client.config.zoomFovDiv),
      optionDoubleSlider(Client.config.zoomSensDiv),
      optionButton(
        Client.config.zoomSmooth,
        ConfigScreen::toggle
      ),
      optionButton(
        Client.config.centeredSigns,
        ConfigScreen::toggle
      ),
      optionButton(
        Client.config.perfectSigns,
        ConfigScreen::toggle
      ),
      optionButton(
        Client.config.dotXhair,
        ConfigScreen::toggle
      ),
      optionIntegerSlider(Client.config.dotSize),
      optionButton(
        Client.config.dynamicDot,
        ConfigScreen::toggle
      ),
      optionColorText(Client.config.blockOutlineColor)
    ));

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
