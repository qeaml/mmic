package com.github.qeaml.mmic.gui.widgets;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;
import com.github.qeaml.mmic.gui.ConfigScreen;
import com.github.qeaml.mmic.gui.OptionRGBAScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class OptionRGBAButtonWidget extends ButtonWidget {
  private Option<Color> option;
  private final TooltipRenderer tooltip;

  public OptionRGBAButtonWidget(int x, int y, int width, int height, Option<Color> option, Screen parent, TooltipRenderer tooltipRenderer) {
    super(
      x, y,
      width, height,
      ConfigScreen.optionButtonText(
        option, Text.translatable("gui.mmic.submenu", option.title)),
    (button) -> {
      Client.mc.setScreen(new OptionRGBAScreen(parent, option));
    });
    this.tooltip = tooltipRenderer;
    this.option = option;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if(clicked(mouseX, mouseY) && button == 1) {
      playDownSound(Client.mc.getSoundManager());
      option.set(option.defaultValue);
      setMessage(ConfigScreen.optionButtonText(
        option, Text.translatable("gui.mmic.submenu", option.title)));
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
