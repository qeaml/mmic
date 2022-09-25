package com.github.qeaml.mmic.config;

import java.util.Map;
import java.util.function.Supplier;

import com.github.qeaml.mmic.Client;

import net.minecraft.text.Text;

@FunctionalInterface
public interface Display<T> {
  Text format(T val);

  public static final Display<Double> PERCENT = v ->
    Text.literal(String.format("%d%%", (int)(v*100)));

  public static final Display<Boolean> ON_OFF = Client::onOff;

  public static final Display<Double> DOUBLE = v ->
    Text.literal(String.format("%.1f", v));

  public static <T> Display<T> mapped(Supplier<Map<T, Text>> init) {
    var m = init.get();
    return v -> m.get(v);
  }

  public static <T> Display<T> literal() {
    return v -> Text.literal(String.valueOf(v));
  }
}
