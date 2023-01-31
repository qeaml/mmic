package com.github.qeaml.mmic.gui;

import java.util.Map;

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
      ButtonWidget.builder(
        Text.translatable("config.mmic.game"),
        (button) -> {
          client.setScreen(new GameplayConfigScreen(this));
        }).build(),
      ButtonWidget.builder(
        Text.translatable("config.mmic.cosm"),
        (button) -> {
          client.setScreen(new CosmeticConfigScreen(this));
        }).build(),
      ButtonWidget.builder(
        Text.translatable("config.mmic.other"),
        (button) -> {
          client.setScreen(new OtherConfigScreen(this));
        }).build()
    };
  }

  private Map<String, Object> oldValues;

  @Override
  protected void prep() {
    oldValues = Client.config.freeze();
  }

  @Override
  protected void cancel() {
    Client.config.unfreeze(oldValues);
  }

  @Override
  protected void done() {
    Client.config.save();
  }
}
