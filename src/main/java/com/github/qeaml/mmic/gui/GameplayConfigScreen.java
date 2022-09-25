package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.gui.widgets.ClickableWidgetList;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import static com.github.qeaml.mmic.gui.ConfigScreen.*;

public class GameplayConfigScreen extends Screen {
  private Screen parent;

  public GameplayConfigScreen(Screen parent) {
    super(Text.translatable("config.mmic.game"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    addDrawableChild(new ClickableWidgetList(
      0, 40,
      this.width,
      2,
      optionButton(
        Client.config.autoplant,
        ConfigScreen::toggle
      ),
      optionButton(
        Client.config.sneakAutoplant,
        ConfigScreen::toggle
      ),
      optionButton(
        Client.config.lagType,
        opt -> opt.set(opt.get().next())
      )
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