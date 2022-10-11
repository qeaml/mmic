package com.github.qeaml.mmic.gui.widgets;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;
import com.github.qeaml.mmic.gui.OptionRGBScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class OptionRGBButtonWidget extends ButtonWidget {
  private Option<Color> option;

  public OptionRGBButtonWidget(int x, int y, int width, int height, Option<Color> option, Screen parent) {
    super(
      x, y,
      width, height,
      Text.translatable("gui.mmic.submenu", option.title),
    (button) -> {
      Client.mc.setScreen(new OptionRGBScreen(parent, option));
    });
    this.option = option;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if(clicked(mouseX, mouseY) && button == 1) {
      playDownSound(Client.mc.getSoundManager());
      option.set(option.defaultValue);
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }
}
