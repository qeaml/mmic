package com.github.qeaml.mmic.gui.widgets;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ClickableWidgetList extends ClickableWidget {
  private final ClickableWidget[] widgets;

  public ClickableWidgetList(int x, int y, int width, int padding, ClickableWidget... widgets) {
    super(x, y, width, 0, Text.empty());
    this.widgets = widgets;

    int wy = this.y;
    for(int i = 0; i < widgets.length; i += 2) {
      var left = widgets[i];
      if(widgets.length == i+1) {
        left.x = this.x + width/2 - left.getWidth()/2;
        left.y = wy;
        wy += left.getHeight() + padding;
      } else {
        left.x = this.x + width/2 - left.getWidth() - padding/2;
        left.y = wy;
        var right = widgets[i+1];
        right.x = this.x + width/2 + padding/2;
        right.y = wy;
        wy += Math.max(left.getHeight(), right.getHeight()) + padding;
      }
    }

    this.height = wy - this.y;
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    for(var w: widgets)
      w.render(matrices, mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    for(var w: widgets)
      if(w.isMouseOver(mouseX, mouseY))
        return w.mouseClicked(mouseX, mouseY, button);
      else
        w.mouseClicked(mouseX, mouseY, button);
    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    for(var w: widgets)
      if(w.isMouseOver(mouseX, mouseY))
        return w.mouseReleased(mouseX, mouseY, button);
    return false;
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    for(var w: widgets)
      if(w.isMouseOver(mouseX, mouseY))
        return w.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    return false;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
    for(var w: widgets)
      if(w.isMouseOver(mouseX, mouseY))
        return w.mouseScrolled(mouseX, mouseY, amount);
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    for(var w: widgets)
      if(w.isFocused())
        return w.keyPressed(keyCode, scanCode, modifiers);
    return false;
  }

  @Override
  public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
    for(var w: widgets)
      if(w.isFocused())
        return w.keyReleased(keyCode, scanCode, modifiers);
    return false;
  }

  @Override
  public void appendNarrations(NarrationMessageBuilder builder) {
    for(var w: widgets)
      w.appendNarrations(builder);
  }

  @Override
  public SelectionType getType() {
    for(var w: widgets)
      if(w.getType() != SelectionType.NONE)
        return w.getType();
    return SelectionType.NONE;
  }
}
