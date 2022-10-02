package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.biome.source.BiomeAccess;

@Mixin(BiomeAccess.class)
public interface BiomeAccessAccess {
  @Accessor
  public long getSeed();
}
