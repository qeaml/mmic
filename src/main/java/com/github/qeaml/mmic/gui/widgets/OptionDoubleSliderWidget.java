package com.github.qeaml.mmic.gui.widgets;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Option;

import net.minecraft.client.gui.widget.SliderWidget;

public class OptionDoubleSliderWidget extends SliderWidget {
  private final Option<Double> option;

  public OptionDoubleSliderWidget(int x, int y, int width, int height, Option<Double> option) {
    super(
      x, y,
      width, height,
      option.display(option.get()),
      (option.get()-option.minValue)/(option.maxValue-option.minValue));
    this.option = option;
  }

  @Override
  protected void applyValue() {
    option.set(option.minValue+value*(option.maxValue-option.minValue));
  }

  @Override
  protected void updateMessage() {
    setMessage(option.display(option.get()));
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if(clicked(mouseX, mouseY) && button == 1) {
      playDownSound(Client.mc.getSoundManager());
      option.set(option.defaultValue);
      value = (option.get()-option.minValue)/(option.maxValue-option.minValue);
      setMessage(option.display(option.get()));
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }
}
