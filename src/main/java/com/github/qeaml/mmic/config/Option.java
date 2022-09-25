package com.github.qeaml.mmic.config;

import net.minecraft.text.Text;

public class Option<T> {
  public final Validator<T> validator;
  public final Codec<T> codec;
  public final Text title;
  public final Text tooltip;
  public final T defaultValue;
  public final T minValue;
  public final T maxValue;
  private final Display<T> display;
  private T value;

  private static final String KEY_NAMESPACE = "options.mmic.";
  private static final String TIP_POSTFIX   = ".tip";

  public Option(T defaultValue, T minValue, T maxValue, Validator<T> validator, Codec<T> codec, String name, Display<T> display) {
    this.value = defaultValue;
    this.defaultValue = defaultValue;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.validator = validator;
    this.codec = codec;
    this.title = Text.translatable(KEY_NAMESPACE+name);
    this.tooltip = Text.translatable(KEY_NAMESPACE+name+TIP_POSTFIX);
    this.display = display;
  }

  public Text display(T val) {
    return Text.translatable("options.generic_value", title, display.format(val));
  }

  public boolean set(T newValue) {
    var ok = validator.validate(newValue);
    if(!ok)
      value = defaultValue;
    else
      value = newValue;
    return ok;
  }

  @SuppressWarnings("unchecked")
  public boolean setObject(Object newValue) {
    try {
      return set((T)newValue);
    } catch(Exception e) {
      return false;
    }
  }

  public T get() {
    return value;
  }
}
