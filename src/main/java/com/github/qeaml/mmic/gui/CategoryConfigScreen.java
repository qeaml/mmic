package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class CategoryConfigScreen extends ConfigScreen {
  public CategoryConfigScreen(Screen parent) {
    super(parent, Text.translatable("gui.mmic.config"), true);
  }

  @Override
  protected ClickableWidget[] widgets() {
    return new ClickableWidget[] {
      new ButtonWidget(
        0, 0,
        BUTTON_WIDTH, 20,
        Text.translatable("config.mmic.game"),
      (button) -> {
        client.setScreen(new GameplayConfigScreen(this));
      }),
      new ButtonWidget(
        10, 10,
        BUTTON_WIDTH, 20,
        Text.translatable("config.mmic.cosm"),
      (button) -> {
        client.setScreen(new CosmeticConfigScreen(this));
      }),
      new ButtonWidget(
        10, 10,
        BUTTON_WIDTH, 20,
        Text.translatable("config.mmic.other"),
      (button) -> {
        client.setScreen(new OtherConfigScreen(this));
      })
    };
  }

  @Override
  public void done() {
    Client.config.save();
  }
}
