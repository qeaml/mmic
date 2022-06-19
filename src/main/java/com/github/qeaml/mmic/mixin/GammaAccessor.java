package com.github.qeaml.mmic.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.option.SimpleOption;

@Mixin(SimpleOption.class)
public interface GammaAccessor {
  @Accessor("value")
  void setValueBypass(Object val);
  @Accessor("changeCallback")
  Consumer<Object> getCallback();
}
