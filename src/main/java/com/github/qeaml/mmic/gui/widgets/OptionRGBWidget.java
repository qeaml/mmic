package com.github.qeaml.mmic.gui.widgets;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;

import net.minecraft.client.font.TextRenderer;

public class OptionRGBWidget extends OptionARGBWidget {
  public OptionRGBWidget(TextRenderer textRenderer, int x, int y, int width, int height, Option<Color> option) {
    super(textRenderer, x, y, width, height, option);
    this.input.setText(option.get().rgbString());
    this.input.setChangedListener(raw -> {
      var maybeColor = Color.ofRGB(raw);
      if(!maybeColor.isEmpty())
        option.set(maybeColor.get());
      valid = maybeColor.isPresent();
    });
    this.input.setTextPredicate(raw -> {
      if(raw == "")
        return true;
      if(raw.length() > 6)
        return false;
      try {
        Long.parseLong(raw, 16);
      } catch(NumberFormatException e) {
        Client.log.info("invalid color: "+raw+": "+e.getMessage());
        return false;
      }
      return true;
    });
  }
}
