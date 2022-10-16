package com.github.qeaml.mmic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Client;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.HoeItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
  @Shadow
  private ClientWorld world;
  @Shadow
  private MinecraftClient client;
  @Shadow
  private BufferBuilderStorage bufferBuilders;

  @Invoker
  protected static void callDrawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {}

  @Inject(
    method = "drawBlockOutline",
    at = @At("HEAD"),
    cancellable = true
  )
  private void hijackDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
    var color = Client.config.blockOutlineColor.get();
    callDrawCuboidShapeOutline(
      matrices, vertexConsumer,
      state.getOutlineShape(world, pos, ShapeContext.of(entity)),
      (double)pos.getX() - cameraX,
      (double)pos.getY() - cameraY,
      (double)pos.getZ() - cameraZ,
      color.redFloat(),
      color.greenFloat(),
      color.blueFloat(),
      0.4F);
    ci.cancel();
  }

  private @Unique BlockPos fluidPos;
  private @Unique float fluidHilightTimer;

  @Inject(
    method = "render",
    at = @At("TAIL")
  )
  private void injectRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
    if(!Client.config.hoeHelper.get())
      return;

    updateFluidHilight();
    if(fluidPos == null)
      return;

    var m = RenderSystem.getModelViewStack();
    m.push();
    m.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
    RenderSystem.applyModelViewMatrix();

    var imm = bufferBuilders.getEntityVertexConsumers();
    var vex = imm.getBuffer(RenderLayer.getLines());
    var camPos = camera.getPos();
    for(int xoff = -4; xoff <= 4; xoff++)
    for(int zoff = -4; zoff <= 4; zoff++) {
      if(xoff == 0 && zoff == 0)
        continue;
      if(world.getBlockState(new BlockPos(fluidPos.getX()+xoff, fluidPos.getY(), fluidPos.getZ()+zoff)).isAir())
        continue;
      callDrawCuboidShapeOutline(matrices, vex,
        VoxelShapes.fullCube(),
        fluidPos.getX()+xoff-camPos.getX(),
        fluidPos.getY()-camPos.getY(),
        fluidPos.getZ()+zoff-camPos.getZ(),
        1.0f,
        0.25f,
        0.25f,
        Math.min(fluidHilightTimer*20, 0.25f));
    }

    RenderSystem.applyModelViewMatrix();
    m.pop();

    fluidHilightTimer = Math.max(0.0f, fluidHilightTimer-tickDelta);
  }

  private @Unique void updateFluidHilight() {
    if(!(client.player.getMainHandStack().getItem() instanceof HoeItem))
      return;

    var hit = client.player.raycast(5.0F, 0.0F, true);
    if(hit.getType() != HitResult.Type.BLOCK)
      return;

    var fluidHit = (BlockHitResult)hit;
    var fluidPos = fluidHit.getBlockPos();
    var fluidState = world.getFluidState(fluidPos);
    if(fluidState.isEmpty())
      return;
    if(!fluidState.isOf(Fluids.WATER) && !fluidState.isOf(Fluids.FLOWING_WATER))
      return;
    
    this.fluidPos = fluidPos;
    this.fluidHilightTimer = 20.0f;
  }
}
