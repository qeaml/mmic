package com.github.qeaml.mmic.config;

@FunctionalInterface
public interface Validator<T> {
  boolean validate(T value);

  public static <T> Validator<T> alwaysValid() {
    return val -> true;
  };

  public static Validator<Double> doubleRange(double min, double max) {
    return val -> val >= min && val <= max;
  }

  public static Validator<Integer> intRange(int min, int max) {
    return val -> val >= min && val <= max;
  }
}
