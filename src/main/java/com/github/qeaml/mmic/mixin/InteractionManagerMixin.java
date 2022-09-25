package com.github.qeaml.mmic.mixin;

import java.util.Timer;
import java.util.TimerTask;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.qeaml.mmic.Client;

import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class InteractionManagerMixin {
  @Unique
  private Timer timer = new Timer("InteractionManagerMixin-Timer");

  @Invoker
  protected abstract ActionResult callInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);

  @Shadow
  private MinecraftClient client;
  @Shadow
  private GameMode gameMode;

  @Inject(
    method = "breakBlock(Lnet/minecraft/util/math/BlockPos;)Z",
    at = @At("HEAD")
  )
  private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
    if(!Client.config.autoplant.get()) return;
    if(Client.config.sneakAutoplant.get() && !client.player.isSneaking()) return;
    if(client.player.isBlockBreakingRestricted(client.world, pos, gameMode)) return;

    var world = client.world;
    var blockState = world.getBlockState(pos);

    if(!client.player.getMainHandStack().getItem().canMine(blockState, world, pos, client.player))
      return;
  
    var block = blockState.getBlock();

    if(!(block instanceof CropBlock)) return;
    if(blockState.get(CropBlock.AGE) != CropBlock.MAX_AGE) return;

    var seed = block.getPickStack(world, pos, blockState);

    if(seed.isEmpty()) return;

    var inv = client.player.getInventory();
    var seedStack = ItemStack.EMPTY;
    var seedIdx = 0;
    var seedFound = false;
    
    for(int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
      seedStack = inv.getStack(i);
      if(!seedStack.isItemEqualIgnoreDamage(seed)) continue;
      seedFound = true;
      seedIdx = i;
      break;
    }

    if(!seedFound || seedStack.isEmpty()) return;

    inv.selectedSlot = seedIdx;

    var fbp = new BlockPos(pos.getX(), pos.getY()-1, pos.getZ());
    var fhp = new Vec3d(fbp.getX()+0.5, fbp.getY()+0.94, fbp.getZ()+0.5);
    var fbh = new BlockHitResult(fhp, Direction.UP, fbp, false);

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        callInteractBlock(client.player, Hand.MAIN_HAND, fbh);
      }
    }, 10);
  }
}
