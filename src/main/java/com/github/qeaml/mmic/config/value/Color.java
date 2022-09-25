package com.github.qeaml.mmic.config.value;

import java.util.Optional;

import com.github.qeaml.mmic.config.Codec;
import com.github.qeaml.mmic.config.Display;

import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class Color {
  private byte r, g, b, a;

  public Color(byte red, byte green, byte blue, byte alpha) {
    r = red; g = green; b = blue; a = alpha;
  }

  public Color(byte red, byte green, byte blue) {
    this(red, green, blue, (byte)0xFF);
  }

  public static Color ofARGB(int argb) {
    return new Color(
      (byte)((argb >> 16) & 0xFF),
      (byte)((argb >> 8) & 0xFF),
      (byte)((argb & 0xFF)),
      (byte)((argb >> 24) & 0xFF)
    );
  }

  public static Color ofRGB(int rgb) {
    return new Color(
      (byte)((rgb >> 16) & 0xFF),
      (byte)((rgb >> 8) & 0xFF),
      (byte)((rgb & 0xFF))
    );
  }

  public byte redByte()   { return r; }
  public byte greenByte() { return g; }
  public byte blueByte()  { return b; }
  public byte alphaByte() { return a; }

  public float redFloat()   { return (float)(r)/255f; }
  public float greenFloat() { return (float)(g)/255f; }
  public float blueFloat()  { return (float)(b)/255f; }
  public float alphaFloat() { return (float)(a)/255f; }

  public int argb() {
    return (
      (int)a << 24 |
      (int)r << 16 |
      (int)g << 8  |
      (int)b
    );
  }
  
  public int rgb() {
    return (
      (int)r << 16 |
      (int)g << 8  |
      (int)b
    );
  }

  public static final Codec<Color> CODEC = Codec.of(
    value -> Integer.toString(value.argb()),
    raw   -> {
      try {
        return Optional.of(Color.ofARGB(Integer.parseInt(raw)));
      } catch(NumberFormatException e) {
        return Optional.empty();
      }
    }
  );

  public static final Display<Color> DISPLAY_ARGB = v ->
    Text.literal(
      String.format(
        "#%02x%02x%02x%02x",
        v.alphaByte(), v.redByte(), v.greenByte(), v.blueByte()))
      .setStyle(Style.EMPTY.withColor(v.rgb()));

  public static final Display<Color> DISPLAY_RGB = v ->
    Text.literal(
      String.format(
        "#%02x%02x%02x",
        v.redByte(), v.greenByte(), v.blueByte()))
      .setStyle(Style.EMPTY.withColor(v.rgb()));
}
