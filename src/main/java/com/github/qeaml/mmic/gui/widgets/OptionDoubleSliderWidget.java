package com.github.qeaml.mmic.gui.widgets;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.gui.ConfigScreen;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;

public class OptionDoubleSliderWidget extends SliderWidget {
  private final Option<Double> option;
  private final TooltipRenderer tooltip;

  public OptionDoubleSliderWidget(int x, int y, int width, int height, Option<Double> option, TooltipRenderer tooltipRenderer) {
    super(
      x, y,
      width, height,
      ConfigScreen.optionButtonText(option),
      (option.get()-option.minValue)/(option.maxValue-option.minValue));
    this.option = option;
    this.tooltip = tooltipRenderer;
  }

  @Override
  protected void applyValue() {
    option.set(option.minValue+value*(option.maxValue-option.minValue));
  }

  @Override
  protected void updateMessage() {
    setMessage(ConfigScreen.optionButtonText(option));
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if(clicked(mouseX, mouseY) && button == 1) {
      playDownSound(Client.mc.getSoundManager());
      option.set(option.defaultValue);
      value = (option.get()-option.minValue)/(option.maxValue-option.minValue);
      setMessage(ConfigScreen.optionButtonText(option));
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
      tooltip.render(matrices,
        TooltipRenderer.wrapLines(
          option.tooltip,
          Client.mc.getWindow().getScaledWidth()/2),
        mouseX, mouseY);
  }
}
