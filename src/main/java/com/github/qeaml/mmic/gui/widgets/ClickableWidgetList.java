package com.github.qeaml.mmic.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

public class ClickableWidgetList extends ElementListWidget<ClickableWidgetPair> {
  public ClickableWidgetList(MinecraftClient client, int x, int y, int width, int height, int padding, ClickableWidget... widgets) {
    super(client, width, height, y, y+height, 20+2*padding);
    for(int i = 0; i < widgets.length; i += 2) {
      var left = widgets[i];
      if(widgets.length == i+1) {
        addEntry(new ClickableWidgetPair(left, width, padding));
      } else {
        var right = widgets[i+1];
        addEntry(new ClickableWidgetPair(left, right, width, padding));
      }
    }
  }

  @Override
  public int getRowWidth() {
    return 400;
  }

  @Override
  protected int getScrollbarPositionX() {
    return super.getScrollbarPositionX() + 32;
  }
}
