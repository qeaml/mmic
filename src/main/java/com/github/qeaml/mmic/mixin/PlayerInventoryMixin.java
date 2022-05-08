package com.github.qeaml.mmic.mixin;

import com.github.qeaml.mmic.State;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
	@Inject(
		method = "insertStack(ILnet/minecraft/item/ItemStack;)Z",
		at = @At("HEAD")
	)
	private void injectInsertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> ci)
	{
		// FIXME: moving items within inventory does not add to pickups map
		if(!stack.isEmpty())
			State.itemPickup(stack);
	}

	private @Unique boolean dropConfirm = false;

	@Inject(
		method = "removeStack(II)Lnet/minecraft/item/ItemStack;",
		at = @At("RETURN")
	)
	private void injectRemoveStackII(int slot, int amount, CallbackInfoReturnable<ItemStack> ci)
	{
		// FIXME: this method always gets called twice (why???)
		var stack = ci.getReturnValue();
		if(!stack.isEmpty())
			State.itemDrop(stack);
		dropConfirm = true;
	}

	@Inject(
		method = "removeStack(I)Lnet/minecraft/item/ItemStack;",
		at = @At("RETURN")
	)
	private void injectRemoveStackI(int slot, CallbackInfoReturnable<ItemStack> ci)
	{
		var stack = ci.getReturnValue();
		if(!stack.isEmpty())
			State.itemDrop(stack);
	}

	@Inject(
		method = "removeOne(Lnet/minecraft/item/ItemStack;)V",
		at = @At("HEAD")
	)
	private void injectRemoveOne(ItemStack stack, CallbackInfo ci)
	{
		if(!stack.isEmpty())
			State.itemDrop(stack);
	}
}
