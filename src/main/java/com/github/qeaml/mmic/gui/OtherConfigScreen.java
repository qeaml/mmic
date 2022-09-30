package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class OtherConfigScreen extends ConfigScreen {
  public OtherConfigScreen(Screen parent) {
    super(parent, Text.translatable("config.mmic.other"));
  }

  @Override
  protected ClickableWidget[] widgets() {
    return new ClickableWidget[] {
      optionIntegerSlider(Client.config.migrationDepth)
    };
  }

  @Override
  protected void onExit() {}
}
