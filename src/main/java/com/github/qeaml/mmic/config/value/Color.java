package com.github.qeaml.mmic.config.value;

import java.util.Optional;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.Codec;
import com.github.qeaml.mmic.config.Display;

import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class Color {
  private short r, g, b, a;

  public Color(short red, short green, short blue, short alpha) {
    r = red; g = green; b = blue; a = alpha;
  }

  public Color(short red, short green, short blue) {
    this(red, green, blue, (short)0xFF);
  }

  public static Color ofARGB(long argb) {
    return new Color(
      (short)((argb >> 16) & 0xFF),
      (short)((argb >> 8) & 0xFF),
      (short)((argb & 0xFF)),
      (short)((argb >> 24) & 0xFF)
    );
  }

  public static Color ofRGB(long rgb) {
    return new Color(
      (short)((rgb >> 16) & 0xFF),
      (short)((rgb >> 8) & 0xFF),
      (short)((rgb & 0xFF))
    );
  }

  public static Optional<Color> ofARGB(String raw) {
    if(raw == "")
      return Optional.empty();
    if(raw.charAt(0) == '#')
      raw = raw.substring(1);
    if(raw.length() < 8)
      return Optional.empty();
    try {
      return Optional.of(Color.ofARGB(Long.parseLong(raw, 16)));
    } catch(NumberFormatException e) {
      Client.log.warn("invalid color "+raw+": "+e.getMessage());
      return Optional.empty();
    }
  }

  public static Optional<Color> ofRGB(String raw) {
    if(raw == "")
      return Optional.empty();
    if(raw.charAt(0) == '#')
      raw = raw.substring(1);
    if(raw.length() < 6)
      return Optional.empty();
    try {
      return Optional.of(Color.ofRGB(Long.parseLong(raw, 16)));
    } catch(NumberFormatException e) {
      Client.log.warn("invalid color "+raw+": "+e.getMessage());
      return Optional.empty();
    }
  }

  public short redShort()   { return r; }
  public short greenShort() { return g; }
  public short blueShort()  { return b; }
  public short alphaShort() { return a; }

  public float redFloat()   { return (float)(r)/255f; }
  public float greenFloat() { return (float)(g)/255f; }
  public float blueFloat()  { return (float)(b)/255f; }
  public float alphaFloat() { return (float)(a)/255f; }

  public String rgbString() {
    return String.format("%02x%02x%02x", r, g, b);
  }

  public String argbString() {
    return String.format("%02x%02x%02x%02x", a, r, g, b);
  }

  public int argb() {
    return (
      ((int)a << 24) |
      ((int)r << 16) |
      ((int)g << 8)  |
      ((int)b)
    );
  }
  
  public int rgb() {
    return (
      ((int)r << 16) |
      ((int)g << 8)  |
      ((int)b)
    );
  }

  public int rgbAsArgb() {
    return rgb() | 0xFE000000;
  }

  public static final Codec<Color> CODEC = Codec.of(
    Color::argbString,
    Color::ofARGB
  );

  public static final Display<Color> DISPLAY_ARGB = v ->
    Text.literal(v.argbString())
      .setStyle(Style.EMPTY.withColor(v.rgb()));

   public static final Display<Color> DISPLAY_RGB = v ->
    Text.literal(v.rgbString())
      .setStyle(Style.EMPTY.withColor(v.rgb()));
}
