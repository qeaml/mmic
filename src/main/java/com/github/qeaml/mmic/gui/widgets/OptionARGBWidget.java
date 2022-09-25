package com.github.qeaml.mmic.gui.widgets;

import org.lwjgl.glfw.GLFW;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Option;
import com.github.qeaml.mmic.config.value.Color;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class OptionARGBWidget extends ClickableWidget {
  protected final Option<Color> option;
  protected final TextFieldWidget input;
  protected boolean valid;
  private final TextRenderer textRenderer;

  public OptionARGBWidget(TextRenderer textRenderer, int x, int y, int width, int height, Option<Color> option) {
    super(x, y, width, height, Text.empty());
    this.option = option;
    this.valid = true;
    this.textRenderer = textRenderer;
    this.input = new TextFieldWidget(this.textRenderer, x, y, width/2-2, height-2, option.title);
    this.input.setText(option.get().argbString());
    this.input.setChangedListener(raw -> {
      var maybeColor = Color.ofARGB(raw);
      if(maybeColor.isPresent())
        option.set(maybeColor.get());
      valid = maybeColor.isPresent();
    });
    this.input.setTextPredicate(raw -> {
      if(raw == "")
        return true;
      if(raw.length() > 9)
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

  public void tick() {
    input.tick();
  }

  @Override
  protected int getYImage(boolean hovered) {
    return 0;
  }

  @Override
  public void playDownSound(SoundManager soundManager) {}

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    super.render(matrices, mouseX, mouseY, delta);
    input.x = x+width/2+1;
    input.y = y+1;
    input.render(matrices, mouseX, mouseY, delta);
    var textWidth = textRenderer.getWidth(option.title);
    drawTextWithShadow(matrices, textRenderer, option.title, x+width/4-textWidth/2, y+height/2-textRenderer.fontHeight/2, 0xFFFFFF);
    if(valid)
      fill(matrices, x+getWidth()-getHeight(), y, x+getWidth(), y+getHeight(), option.get().rgbAsArgb());
  }

  @Override
  public void appendNarrations(NarrationMessageBuilder builder) {}

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    return input.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    return input.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if(!input.keyPressed(keyCode, scanCode, modifiers))
      switch(keyCode) {
      case GLFW.GLFW_KEY_0: input.write("0"); return true;
      case GLFW.GLFW_KEY_1: input.write("1"); return true;
      case GLFW.GLFW_KEY_2: input.write("2"); return true;
      case GLFW.GLFW_KEY_3: input.write("3"); return true;
      case GLFW.GLFW_KEY_4: input.write("4"); return true;
      case GLFW.GLFW_KEY_5: input.write("5"); return true;
      case GLFW.GLFW_KEY_6: input.write("6"); return true;
      case GLFW.GLFW_KEY_7: input.write("7"); return true;
      case GLFW.GLFW_KEY_8: input.write("8"); return true;
      case GLFW.GLFW_KEY_9: input.write("9"); return true;
      case GLFW.GLFW_KEY_A: input.write("a"); return true;
      case GLFW.GLFW_KEY_B: input.write("b"); return true;
      case GLFW.GLFW_KEY_C: input.write("c"); return true;
      case GLFW.GLFW_KEY_D: input.write("d"); return true;
      case GLFW.GLFW_KEY_E: input.write("e"); return true;
      case GLFW.GLFW_KEY_F: input.write("f"); return true;
      };
    return false;
  }

  @Override
  public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
    return input.keyReleased(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean isFocused() {
    return input.isFocused();
  }
}
