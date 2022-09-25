package com.github.qeaml.mmic.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.qeaml.mmic.Client;
import com.github.qeaml.mmic.config.value.Color;
import com.github.qeaml.mmic.config.value.LagType;
import com.google.common.io.Files;

public class Config {
  private static final Logger log = LoggerFactory.getLogger(Client.name+"/Config");

  public final Option<Color>   gridColor;
  public final Option<Double>  gammaStep;
  public final Option<Boolean> miniF3;
  public final Option<Boolean> staticHand;
  public final Option<Boolean> autoplant;
  public final Option<Boolean> sneakAutoplant;
  public final Option<Boolean> centeredSigns;
  public final Option<Boolean> perfectSigns;
  public final Option<Boolean> dotXhair;
  public final Option<Integer> dotSize;
  public final Option<Boolean> dynamicDot;
  public final Option<Color>   blockOutlineColor;
  public final Option<Double>  zoomFovDiv;
  public final Option<Double>  zoomSensDiv;
  public final Option<Boolean> zoomSmooth;
  public final Option<Integer> migrationDepth;
  public final Option<LagType> lagType;

  public Config(File optionsFile) {
    file              = optionsFile;
    options           = new HashMap<>();
    gridColor         = colorARGB("gridColor", Color.ofARGB(0));
    gammaStep         = scaledDouble("gammaStep", 0.2, 10, 0.1, 0.5, Display.PERCENT);
    miniF3            = bool("miniF3");
    staticHand        = bool("staticHand");
    autoplant         = bool("autoplant");
    sneakAutoplant    = bool("sneakAutoplant");
    centeredSigns     = bool("centeredSigns");
    perfectSigns      = bool("perfectSigns");
    dotXhair          = bool("dotXhair");
    dotSize           = integer("dotSize", 2, 1, 10);
    dynamicDot        = bool("dynamicDot");
    blockOutlineColor = colorRGB("blockOutlineColor", Color.ofRGB(0));
    zoomFovDiv        = scaledDouble("zoomFovDiv", 5.0, 10, 1.0, 10.0, Display.DOUBLE);
    zoomSensDiv       = scaledDouble("zoomSensDiv", 5.0, 10, 1.0, 10.0, Display.DOUBLE);
    zoomSmooth        = bool("zoomSmooth");
    migrationDepth    = integer("migrationDepth", 2, 1, 5);
    lagType           = lagType();
  }

  private final File file;

  public boolean load() {
    log.info("Loading");

    var rawValues = new Properties();

    try(var r = Files.newReader(file, Charset.forName("utf-8"))) {
      rawValues.load(r);
    } catch(IOException e) {
      log.error("Could not load config options: "+e.getMessage());
      return false;
    }

    rawValues.forEach((key, objValue) -> {
      if(!options.containsKey(key)) {
        log.warn("Unknown option: "+key);
        return;
      }

      var value = (String)objValue;
      var opt = options.get(key);
      var maybeValue = opt.codec.decode(value);
      if(maybeValue.isEmpty()) {
        log.error("Could not decode option "+key);
        return;
      }

      var ok = opt.setObject(maybeValue.get());
      if(!ok)
        log.warn("Got invalid value for option "+key+", using default!");
    });

    return true;
  }

  public boolean save() {
    return true;
  }

  private final Map<String, Option<?>> options;

  private Option<Color> colorRGB(String name, Color def) {
    return register(name, new Option<>(
      def, Color.ofRGB(0), Color.ofRGB(0xFFFFFF),
      Validator.alwaysValid(),
      Color.CODEC,
      name,
      Color.DISPLAY_RGB));
  }

  private Option<Color> colorARGB(String name, Color def) {
    return register(name, new Option<>(
      def, Color.ofARGB(0), Color.ofARGB(0xFFFFFFFF),
      Validator.alwaysValid(),
      Color.CODEC,
      name,
      Color.DISPLAY_ARGB));
  }

  private Option<Double> scaledDouble(String name, double def, int scale, double min, double max, Display<Double> display) {
    return register(name, new Option<>(
      def, min, max,
      Validator.doubleRange(min, max),
      Codec.scaledDouble(scale),
      name,
      display));
  }

  private Option<Boolean> bool(String name, boolean def) {
    return register(name, new Option<>(
      def, false, true,
      Validator.alwaysValid(),
      Codec.BOOL,
      name,
      Display.ON_OFF));
  }

  private Option<Boolean> bool(String name) {
    return bool(name, false);
  }

  private Option<Integer> integer(String name, int def, int min, int max) {
    return register(name, new Option<>(
      def, min, max,
      Validator.intRange(min, max),
      Codec.INTEGER,
      name,
      Display.literal()));
  }

  private Option<LagType> lagType() {
    return register("lagType", new Option<>(
      LagType.BLOCK, LagType.BLOCK, LagType.LOSSY_CLOG,
      Validator.alwaysValid(),
      LagType.CODEC,
      "lagType",
      LagType.DISPLAY));
  }

  private <T> Option<T> register(String name, Option<T> opt) {
    options.put(name, opt);
    return opt;
  }
}
