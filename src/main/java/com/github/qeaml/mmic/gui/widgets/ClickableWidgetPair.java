package com.github.qeaml.mmic.gui.widgets;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

public class ClickableWidgetPair extends ElementListWidget.Entry<ClickableWidgetPair> {
  private final ClickableWidget left;
  private final @Nullable ClickableWidget right;

  public ClickableWidgetPair(ClickableWidget left, int width, int padding) {
    this.left = left;
    this.left.x = width/2-this.left.getWidth()/2-padding;
    this.right = null;
  }

  public ClickableWidgetPair(ClickableWidget left, ClickableWidget right, int width, int padding) {
    this.left = left;
    this.left.x = width/2-this.left.getWidth()-padding;
    this.right = right;
    this.right.x = width/2+padding;
  }

  @Override
  public List<? extends Element> children() {
    if(this.right == null)
      return List.of(this.left);
    return List.of(this.left, this.right);
  }

  @Override
  public List<? extends Selectable> selectableChildren() {
    if(this.right == null)
      return List.of(this.left);
    return List.of(this.left, this.right);
  }

  @Override
  public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
      int mouseY, boolean hovered, float tickDelta)
  {
    this.left.y = y;
    this.left.render(matrices, mouseX, mouseY, tickDelta);
    if(this.right != null) {
      this.right.y = y;
      this.right.render(matrices, mouseX, mouseY, tickDelta);
    }
  }
}
