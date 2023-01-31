package com.github.qeaml.mmic.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(TextRenderer.class)
public interface TextRendererAccessor {
  // for DebugHudMixin
  @Invoker("drawInternal")
  int internalDraw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider consumers, boolean seeThrough, int backgroundColor, int light, boolean mirror);
}
