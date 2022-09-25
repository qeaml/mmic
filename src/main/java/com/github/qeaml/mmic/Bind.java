package com.github.qeaml.mmic;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;

public class Bind extends KeyBinding {
  private boolean prev;

  public Bind(String translation, int keycode, String category) {
    super(translation, keycode, category);
    KeyBindingHelper.registerKeyBinding(this);
  }

  public boolean wasJustPressed() {
    var ret = isPressed() && !prev;
    prev = isPressed();
    return ret;
  }
}
