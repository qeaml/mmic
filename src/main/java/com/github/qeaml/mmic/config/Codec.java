package com.github.qeaml.mmic.config;

import java.util.Optional;

import com.google.common.base.Function;

public interface Codec<T> {
  Optional<T> decode(String raw);
  String encode(T value);

  @SuppressWarnings("unchecked")
  public static <T> String encodeObject(Codec<T> codec, Object o) {
    return codec.encode((T)o);
  }

  public static <E> Codec<E> of(Function<E, String> encoder, Function<String, Optional<E>> decoder) {
    return new Codec<E>() {
      @Override
      public Optional<E> decode(String raw) {
        return decoder.apply(raw);
      }

      @Override
      public String encode(E value) {
        return encoder.apply(value);
      }
    };
  }

  public static final Codec<String> STRING = of(
    value -> value,
    raw   -> Optional.of(raw  )
  );

  public static final Codec<Boolean> BOOL = of(
    value -> Boolean.toString(value),
    raw   -> Optional.of(Boolean.parseBoolean(raw))
  );

  public static final Codec<Integer> INTEGER = of(
    value -> Integer.toString(value),
    raw   -> {
      try {
        return Optional.of(Integer.parseInt(raw));
      } catch(NumberFormatException e) {
        return Optional.empty();
      }
    }
  );

  public static Codec<Double> scaledDouble(int scale) {
    return of(
      value -> Integer.toString((int)(value * scale)),
      raw   -> {
        try {
          return Optional.of((double)(Integer.parseInt(raw)) / scale);
        } catch(NumberFormatException e) {
          return Optional.empty();
        }
      }
    );
  }
}
