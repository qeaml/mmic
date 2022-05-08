package com.github.qeaml.mmic;

import java.util.List;

import java.util.LinkedList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;

public class State {
	private static MinecraftClient mc = MinecraftClient.getInstance();

	public static boolean fullbright = false;
	private static double oldGamma = 0.5;

	public static void toggleFullbright()
	{
		fullbright = !fullbright;
		if(fullbright) {
			oldGamma = mc.options.gamma;
			mc.options.gamma = 10.0;
		} else {
			mc.options.gamma = oldGamma;
		}
		Client.notify(new TranslatableText("other.mmic.toggled_fullbright", Client.onOff(fullbright)));
	}

	public static boolean lagging = false;

	public static void toggleLag()
	{
		lagging = !lagging;
		Client.notify(new TranslatableText("other.mmic.lag_switched", Client.onOff(lagging)));
	}

	record ItemPickup(int amount, Item item, int ttl) {
		public static ItemPickup of(ItemStack stack)
		{
			return new ItemPickup(stack.getCount(), stack.getItem(), Config.pickupDisplayTime);
		}
		public ItemPickup negate()
		{
			return new ItemPickup(-amount, item, ttl);
		}
		public ItemPickup tick()
		{
			return new ItemPickup(amount, item, ttl-1);
		}
	}
	private static List<ItemPickup> pickups = new LinkedList<>();
	private static Object pickupLock = new Object();

	public static void itemPickup(ItemStack stack)
	{
		var tmpPickups = List.copyOf(pickups);
		for(int i = 0; i < tmpPickups.size(); i++) {
			var ip = tmpPickups.get(i);
			if(ip.item == stack.getItem() && ip.amount > 0)
			{
				synchronized(pickupLock)
				{
					pickups.set(i,
						new ItemPickup(ip.amount+stack.getCount(), ip.item, Config.pickupDisplayTime));
					return;
				}
			}
		}
		pickups.add(ItemPickup.of(stack));
	}

	public static void itemDrop(ItemStack stack)
	{
		var tmpPickups = List.copyOf(pickups);
		for(int i = 0; i < tmpPickups.size(); i++) {
			var ip = tmpPickups.get(i);
			if(ip.item == stack.getItem() && ip.amount < 0)
			{
				synchronized(pickupLock)
				{
					pickups.set(i,
						new ItemPickup(ip.amount-stack.getCount(), ip.item, Config.pickupDisplayTime));
					return;
				}
			}
		}
		pickups.add(ItemPickup.of(stack).negate());
	}

	public static void tickPickups()
	{
		synchronized(pickupLock)
		{
			pickups.clear();
			pickups.stream().map(ItemPickup::tick).forEach(pickups::add);
		}
	}

	private static Language cachedLang = Language.getInstance();
	public static void drawPickups(MatrixStack matrices)
	{
		var tr = MinecraftClient.getInstance().textRenderer;
		int y = tr.fontHeight;
		for(var ip: List.copyOf(pickups))
		{
			int color = ip.amount < 0
				? 0x00FF8080
				: 0x0080FF80;
			int alpha = ip.ttl > 20L
				? 0xFF
				: (int)(((double)ip.ttl/20.0)*0xFF);
			color |= alpha << 24;
			tr.drawWithShadow(matrices,
				String.format("%+d %s",
					ip.amount,
					cachedLang.get(ip.item.getTranslationKey())),
				tr.fontHeight, y, color);
			y += tr.fontHeight;
		}
	}
}
