package com.github.qeaml.mmic.gui.widgets;

import java.util.List;

import com.github.qeaml.mmic.Client;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@FunctionalInterface
public interface TooltipRenderer {
  void render(MatrixStack matrices, List<Text> list, int x, int y);

  static List<Text> wrapLines(Text txt, int width) {
    return Client.mc.textRenderer.wrapLines(txt, width).stream().map(ot -> {
      var ln = new StringBuilder();
      ot.accept((idx, style, codepoint) -> {
        ln.append(Character.toChars(codepoint));
        return true;
      });
      return Text.of(ln.toString());
    }).toList();
  }
}
