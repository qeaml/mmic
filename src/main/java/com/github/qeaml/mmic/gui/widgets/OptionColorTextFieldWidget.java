package com.github.qeaml.mmic.gui.widgets;

import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class OptionColorTextFieldWidget extends TextFieldWidget {
  public OptionColorTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Option<Color> option) {
    super(textRenderer, x, y, width, height, option.display(option.get()));
  }
}
