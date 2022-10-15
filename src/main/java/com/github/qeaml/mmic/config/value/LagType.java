package com.github.qeaml.mmic.config.value;

import java.util.HashMap;
import java.util.Optional;

import com.github.qeaml.mmic.config.Codec;
import com.github.qeaml.mmic.config.Display;

import net.minecraft.text.Text;

public enum LagType {
  BLOCK,
  CLOG,
  LOSSY_BLOCK,
  LOSSY_CLOG,
  TELEPORT;

  public static final Codec<LagType> CODEC = Codec.of(
    value -> value.toString(),
    raw   -> Optional.of(LagType.valueOf(raw))
  );

  public static final Display<LagType> DISPLAY = Display.mapped(() -> {
    var m = new HashMap<LagType, Text>();
    m.put(BLOCK, Text.translatable("options.mmic.lagType.block"));
    m.put(CLOG, Text.translatable("options.mmic.lagType.clog"));
    m.put(LOSSY_BLOCK, Text.translatable("options.mmic.lagType.lossy_block"));
    m.put(LOSSY_CLOG, Text.translatable("options.mmic.lagType.lossy_clog"));
    m.put(TELEPORT, Text.translatable("options.mmic.lagType.teleport"));
    return m;
  });

  public LagType next() {
    var vals = LagType.values();
    return vals[(ordinal()+1)%vals.length];
  }

  public boolean clogs() {
    switch(this) {
    case CLOG, LOSSY_CLOG, TELEPORT:
      return true;
    default:
      return false;
    }
  }
}
