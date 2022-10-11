package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class GameplayConfigScreen extends ConfigScreen {
  public GameplayConfigScreen(Screen parent) {
    super(parent, Text.translatable("config.mmic.game"), false);
  }

  @Override
  protected ClickableWidget[] widgets() {
    return new ClickableWidget[] {
      optionButton(
        Client.config.autoplant,
        CategoryConfigScreen::toggle
      ),
      optionButton(
        Client.config.sneakAutoplant,
        CategoryConfigScreen::toggle
      ),
      optionButton(
        Client.config.autoplantOldOnly,
        CategoryConfigScreen::toggle
      ),
      optionButton(
        Client.config.lagType,
        opt -> opt.set(opt.get().next())
      )
    };
  }
}
