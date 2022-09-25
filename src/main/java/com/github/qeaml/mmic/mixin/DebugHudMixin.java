package com.github.qeaml.mmic.mixin;

import java.util.LinkedList;

import com.github.qeaml.mmic.Config;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//// import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(DebugHud.class)
public class DebugHudMixin extends DrawableHelper {
  @Shadow
  @Final
  private MinecraftClient client;

  @Shadow
  @Final
  private TextRenderer textRenderer;

  @Unique private long lastInfo;
  @Unique private int framecount;
  @Unique private int frametime;
  @Unique private long fps;
  @Unique private int chunkX;
  @Unique private int chunkZ;
  @Unique private WorldChunk chunk;

  @Invoker("getBiomeString")
  private static String getBiomeString(RegistryEntry<Biome> biome) {
    return null;
  }

  @Unique
  private WorldChunk getClientChunk() {
    if(chunk == null) {
      chunk = client.world.getChunk(chunkX, chunkZ);
    }
    return chunk;
  }

  @Inject(
    at = @At("INVOKE"),
    method = "render(Lnet/minecraft/client/util/math/MatrixStack;)V",
    cancellable = true)
  private void hijackRender(MatrixStack matrices, CallbackInfo ci) {
    if(!Config.miniF3) return;

    var ll = new LinkedList<String>();

    framecount++;
    var delta = System.currentTimeMillis()-lastInfo;
    if(delta >= 100L) {
      fps = 1000 * framecount / delta;
      frametime = (int)(delta/framecount);
      framecount = 0;
      lastInfo = System.currentTimeMillis();
    }
    ll.add(String.format("FPS: %d (%dms)", fps, frametime));

    var r = Runtime.getRuntime();
    ll.add(String.format("Memory: %.02f/%.02fMiB",
      toMiB(r.totalMemory() - r.freeMemory()),
      toMiB(r.maxMemory())));

    ////ll.add(String.format("Client: %s %s (%s)",
    ////  ClientBrandRetriever.getClientModName(),
    ////  client.getGameVersion(),
    ////  client.getVersionType()));

    IntegratedServer is;
    if((is = client.getServer()) != null)
      ll.add(String.format("Server: Integrated @ %.2fms", is.getTickTime()));
    else
      ll.add(String.format("Server: %s", client.player.getServerBrand()));

    var cam = client.getCameraEntity();
    var yaw = cam.getYaw() % 360.0f;
    ll.add(String.format("XYZ: %.2f/%.2f/%.2f", cam.getX(), cam.getY(), cam.getZ()));
    ll.add(String.format("Rotation: %.2f/%.2f (%s)", yaw, cam.getPitch(), cam.getHorizontalFacing().getName()));
    
    var pos = client.player.getBlockPos();
    chunkX = pos.getX() >> 4;
    chunkZ = pos.getZ() >> 4;
    ll.add(String.format("Chunk: %d/%d", chunkX, chunkZ));

    if(cam.getY() >= client.world.getBottomY() && cam.getY() < client.world.getTopY())
      ll.add(String.format("Biome: %s", getBiomeString(client.world.getBiome(cam.getBlockPos()))));
    else
      ll.add("Out of world.");
    
    if(!getClientChunk().isEmpty()) {
      var blockPos = client.player.getBlockPos();
      ll.add(String.format("Light: %d block, %d sky = %d total",
        client.world.getLightLevel(LightType.BLOCK, blockPos),
        client.world.getLightLevel(LightType.SKY, blockPos),
        client.world.getChunkManager().getLightingProvider().getLight(blockPos, 0)));
    }

    int lbgw = 0;
    for(String s: ll)
    {
      int w = textRenderer.getWidth(s);
      if(w > lbgw)
        lbgw = w;
    }

    int n = textRenderer.fontHeight/2;
    fillGradient(matrices,
      n, n,
      lbgw+textRenderer.fontHeight+n,
      ll.size()*textRenderer.fontHeight+textRenderer.fontHeight+n,
      0x801F1F1f, 0x800F0F0F);

    var perf = 0xFF00FF00;
    if(fps < 20)
      perf = 0xFFFF0000;
    else if(fps < 30)
      perf = 0xFFFF8000;
    else if(fps < 60)
      perf = 0xFFFFFF00;
    fill(matrices,
      0, n,
      n, ll.size()*textRenderer.fontHeight+textRenderer.fontHeight+n,
      perf);

    leftLines = 0;
    ll.forEach(text -> leftLine(matrices, text));

    if(client.crosshairTarget.getType() == HitResult.Type.BLOCK)
    {
      var cl = new LinkedList<String>();
      pos = ((BlockHitResult)client.crosshairTarget).getBlockPos();
      var state = client.player.getWorld().getBlockState(pos);
      cl.add(String.format("%s at %s", Registry.BLOCK.getId(state.getBlock()), pos.toShortString()));
      state.getProperties().forEach(prop -> cl.add(prop.getName()+"="+String.valueOf(state.get(prop))));
      state.streamTags().forEach(tag -> cl.add("#"+tag.id().toString()));
      int cbgw = 0;
      for(String s: cl)
      {
        int w = textRenderer.getWidth(s);
        if(w > cbgw)
          cbgw = w;
      }
      var sw = client.getWindow().getScaledWidth();
      var sh = client.getWindow().getScaledHeight();
      fillGradient
      (
        matrices,
        sw/2
          -cbgw/2
          -n,
        sh/2
          +textRenderer.fontHeight
          +n,
        sw/2
          +cbgw/2
          +n,
        sh/2
          +textRenderer.fontHeight
          +cl.size()*textRenderer.fontHeight
          +textRenderer.fontHeight
          +n,
        0x801F1F1f, 0x800F0F0F
      );
      centerLines = 0;
      cl.forEach(text -> centerLine(matrices, text));
    }
    else if(client.crosshairTarget.getType() == HitResult.Type.ENTITY)
    {
      var cl = new LinkedList<String>();
      var ent = ((EntityHitResult)client.crosshairTarget).getEntity();
      cl.add(String.format("%s at %.2f, %.2f, %.2f",
        Registry.ENTITY_TYPE.getId(ent.getType()),
        ent.getPos().x, ent.getPos().y, ent.getPos().z));
      if(ent instanceof LivingEntity lent)
        cl.add(String.format("Health: %.1f/%d",
          lent.getHealth(), (int)lent.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH)));
      if(ent instanceof Tameable te)
        if(te.getOwner() != null)
          cl.add("Owner: "+te.getOwner().getDisplayName().getString());
      int cbgw = 0;
      for(String s: cl)
      {
        int w = textRenderer.getWidth(s);
        if(w > cbgw)
          cbgw = w;
      }
      var sw = client.getWindow().getScaledWidth();
      var sh = client.getWindow().getScaledHeight();
      fillGradient
      (
        matrices,
        sw/2
          -cbgw/2
          -n,
        sh/2
          +textRenderer.fontHeight
          +n,
        sw/2
          +cbgw/2
          +n,
        sh/2
          +textRenderer.fontHeight
          +cl.size()*textRenderer.fontHeight
          +textRenderer.fontHeight
          +n,
        0x801F1F1f, 0x800F0F0F
      );
      centerLines = 0;
      cl.forEach(text -> centerLine(matrices, text));
    }

    ci.cancel();
  }

  private @Unique int leftLines = 0;

  @Unique
  private void leftLine(MatrixStack matrices, String text) {
    var immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    ((TextRendererAccessor)textRenderer).internalDraw(
      text,
      textRenderer.fontHeight,
      textRenderer.fontHeight*leftLines+textRenderer.fontHeight,
      0xFFFFFFFF,
      false,
      matrices.peek().getPositionMatrix(),
      immediate,
      false,
      0x00000000,
      15728880,
      false);
    immediate.draw();
    leftLines++;
  }

  private @Unique int centerLines = 0;

  @Unique
  private void centerLine(MatrixStack matrices, String text)
  {
    var immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    ((TextRendererAccessor)textRenderer).internalDraw(
      text,
      client.getWindow().getScaledWidth()/2
        -textRenderer.getWidth(text)/2,
      client.getWindow().getScaledHeight()/2
        +textRenderer.fontHeight*centerLines+textRenderer.fontHeight+textRenderer.fontHeight,
      0xFFFFFFFF,
      false,
      matrices.peek().getPositionMatrix(),
      immediate,
      false,
      0x00000000,
      15728880,
      false);
    immediate.draw();
    centerLines++;
  }

  @Unique
  private double toMiB(long bytes) {
    return (double)bytes / 1024.0 / 1024.0;
  }
}
