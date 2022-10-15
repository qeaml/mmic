package com.github.qeaml.mmic.gui;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class CosmeticConfigScreen extends ConfigScreen {
  public CosmeticConfigScreen(Screen parent) {
    super(parent, Text.translatable("config.mmic.cosm"), false);
  }

  @Override
  protected ClickableWidget[] widgets() {
    return new ClickableWidget[] {
      optionColorARGB(Client.config.gridColor),
      optionDoubleSlider(Client.config.gammaStep),
      optionButton(
        Client.config.miniF3,
        CategoryConfigScreen::toggle
      ),
      optionButton(
        Client.config.staticHand,
        CategoryConfigScreen::toggle
      ),
      optionDoubleSlider(Client.config.zoomFovDiv),
      optionDoubleSlider(Client.config.zoomSensDiv),
      optionButton(
        Client.config.zoomSmooth,
        CategoryConfigScreen::toggle
      ),
      optionButton(
        Client.config.centeredSigns,
        CategoryConfigScreen::toggle
      ),
      optionButton(
        Client.config.perfectSigns,
        CategoryConfigScreen::toggle
      ),
      optionButton(
        Client.config.dotXhair,
        CategoryConfigScreen::toggle
      ),
      optionIntegerSlider(Client.config.dotSize),
      optionButton(
        Client.config.dynamicDot,
        CategoryConfigScreen::toggle
      ),
      optionColorRGB(Client.config.blockOutlineColor),
      optionButton(
        Client.config.chunkNames,
        CategoryConfigScreen::toggle
      ),
      optionButton(
        Client.config.noChatIndicators,
        CategoryConfigScreen::toggle
      )
    };
  }
}
