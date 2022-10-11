package com.github.qeaml.mmic.gui.widgets;

import java.util.function.Consumer;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.gui.ConfigScreen;

import net.minecraft.client.gui.widget.ButtonWidget;

public class OptionButtonWidget<T> extends ButtonWidget {
  private Option<T> option;
  private Consumer<Option<T>> action;

  public OptionButtonWidget(int x, int y, int width, int height, Option<T> option, Consumer<Option<T>> action) {
    super(
      x, y,
      width, height,
      ConfigScreen.optionButtonText(option),
      (button) -> {});
    this.option = option;
    this.action = action;
  }

  @Override
  public void onPress() {
    action.accept(option);
    setMessage(ConfigScreen.optionButtonText(option));
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if(clicked(mouseX, mouseY) && button == 1) {
      playDownSound(Client.mc.getSoundManager());
      option.set(option.defaultValue);
      setMessage(ConfigScreen.optionButtonText(option));
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }
}
