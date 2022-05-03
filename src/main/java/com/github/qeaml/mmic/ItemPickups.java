package com.github.qeaml.mmic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Language;
import net.minecraft.util.Pair;

/**
 * Handles all logic related to the item pickup display.
 */
public class ItemPickups {
	private static Language cachedLang = Language.getInstance();
	private static Map<Item, Pair<Integer, Long>> pickups = new ConcurrentHashMap<>();
	private static Map<Item, Pair<Integer, Long>> drops = new ConcurrentHashMap<>();

	/**
	 * Adds the given ItemStack to the pickups Map. This can either add a new
	 * entry for the stack's item or add this stack's count to an existing
	 * entry for its item (also refreshing said entry's counter).
	 * @param stack ItemStack to add
	 */
	public static void add(ItemStack stack)
	{
		int oldCount = 0;
		if(pickups.containsKey(stack.getItem()))
			oldCount = pickups.get(stack.getItem()).getLeft();
		pickups.put(stack.getItem(), new Pair<>(oldCount+stack.getCount(), 100L));
	}

	public static void drop(ItemStack stack)
	{
		int oldCount = 0;
		if(drops.containsKey(stack.getItem()))
			oldCount = drops.get(stack.getItem()).getLeft();
		drops.put(stack.getItem(), new Pair<>(oldCount+stack.getCount(), 100L));
	}

	/**
	 * Ticks the pickups Map. This decreases each entry's TTL, removing the ones
	 * with 0 TTL.
	 */
	public static void tick()
	{
		Map.copyOf(pickups).forEach((item, values) -> {
			if(values.getRight() <= 1)
				pickups.remove(item);
			else
				pickups.put(item, new Pair<>(values.getLeft(), values.getRight()-1));
		});
		Map.copyOf(drops).forEach((item, values) -> {
			if(values.getRight() <= 1)
				drops.remove(item);
			else
				drops.put(item, new Pair<>(values.getLeft(), values.getRight()-1));
		});
	}

	/**
	 * Iterates and draws each entry in the pickups Map.
	 * @param matrices MatrixStack to draw to
	 */
	public static void draw(MatrixStack matrices)
	{
		var tr = MinecraftClient.getInstance().textRenderer;
		int ln = 0;

		var imap = Map.copyOf(pickups);
		int color = 0x0080FF80;
		for(var e: imap.entrySet())
		{
			int alpha = (e.getValue().getRight() > 20L)
				? 0xFF
				: (int)(((double)e.getValue().getRight()/20.0)*0xFF);
			alpha <<= 24;
			tr.drawWithShadow(matrices,
				String.format("+%d %s",
					e.getValue().getLeft(),
					cachedLang.get(e.getKey().getTranslationKey()),
					e.getValue().getRight()),
				tr.fontHeight,
				tr.fontHeight+(ln*tr.fontHeight),
				color | alpha);
			ln++;
		}

		imap = Map.copyOf(drops);
		color = 0x00FF8080;
		for(var e: imap.entrySet())
		{
			int alpha = (e.getValue().getRight() > 20L)
				? 0xFF
				: (int)(((double)e.getValue().getRight()/20.0)*0xFF);
			alpha <<= 24;
			tr.drawWithShadow(matrices,
				String.format("-%d %s",
					e.getValue().getLeft(),
					cachedLang.get(e.getKey().getTranslationKey()),
					e.getValue().getRight()),
				tr.fontHeight,
				tr.fontHeight+(ln*tr.fontHeight),
				color | alpha);
			ln++;
		}
	}
}
