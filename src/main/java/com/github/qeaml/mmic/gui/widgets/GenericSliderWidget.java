package com.github.qeaml.mmic.gui.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class GenericSliderWidget extends SliderWidget {
  private Function<Double, Text> displayFunction;
  private Consumer<Double> valueConsumer;

  public GenericSliderWidget(int x, int y, int width, int height, Text text, double value, Function<Double, Text> display, Consumer<Double> valueFunc) {
    super(x, y, width, height, text, value);
    this.displayFunction = display;
    this.valueConsumer = valueFunc;
  }

  @Override
  protected void updateMessage() {
    setMessage(displayFunction.apply(value));
  }

  @Override
  protected void applyValue() {
    valueConsumer.accept(value);
  }
}
