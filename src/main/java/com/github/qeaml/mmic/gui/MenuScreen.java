package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.gui.widgets.ClickableWidgetList;

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

    addDrawableChild(new ClickableWidgetList(
      client,
      this.width,
      this.height,
      32, this.height-34,
      2,
      ButtonWidget.builder(
        Text.translatable("gui.mmic.sessions"),
        (button) -> {
          client.setScreen(new LoadingScreen<>(
            Text.translatable("gui.mmic.sessions.loading"),
            LoadingScreen.Loader.of(
              () -> new PlaytimeScreen(that),
              client::setScreen
            )));
        })
        .width(200)
        .position(width/2 - 100, 40)
        .build(),
      ButtonWidget.builder(
        Text.translatable("gui.mmic.config"),
        (button) -> {
          client.setScreen(new CategoryConfigScreen(that));
        })
        .width(200)
        .position(width/2 - 100, 60)
        .build()
    ));

    addDrawableChild(ButtonWidget.builder(
      ScreenTexts.DONE,
      (button) -> {
        client.setScreen(parent);
      })
      .width(200)
      .position(width/2 - 100, height-27)
      .build()
    );
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    super.render(matrices, mouseX, mouseY, delta);
    drawCenteredText(matrices, textRenderer, title, width / 2, 20, 0xFFFFFFFF);
  }
}
