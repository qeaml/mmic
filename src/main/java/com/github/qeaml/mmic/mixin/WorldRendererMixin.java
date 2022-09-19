package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Config;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
  @Shadow
  private ClientWorld world;

  @Invoker
  protected static void callDrawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {}

  @Inject(
    method = "drawBlockOutline",
    at = @At("HEAD"),
    cancellable = true
  )
  private void hijackDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
    callDrawCuboidShapeOutline(
      matrices, vertexConsumer,
      state.getOutlineShape(world, pos, ShapeContext.of(entity)),
      (double)pos.getX() - cameraX,
      (double)pos.getY() - cameraY,
      (double)pos.getZ() - cameraZ,
      (float)((Config.blockOutlineColor>>16)&0xFF)/255f,
      (float)((Config.blockOutlineColor>>8)&0xFF)/255f,
      (float)(Config.blockOutlineColor&0xFF)/255f,
      0.4F);
    ci.cancel();
  }
}
