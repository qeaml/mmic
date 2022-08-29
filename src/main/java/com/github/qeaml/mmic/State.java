package com.github.qeaml.mmic;

import java.util.List;

import com.github.qeaml.mmic.Config.LagType;
import com.github.qeaml.mmic.mixin.GammaAccessor;

import java.util.Deque;
import java.util.LinkedList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

public class State {
	private static MinecraftClient mc = MinecraftClient.getInstance();

	public static boolean fullbright = false;
	private static double oldGamma = 0.5;

	public static void toggleFullbright()
	{
		fullbright = !fullbright;
		var acc = (GammaAccessor)(Object)mc.options.getGamma();
		if(fullbright) {
			oldGamma = mc.options.getGamma().getValue();
			acc.setValueBypass(10.0);
      acc.getCallback().accept(10.0);
		} else {
			acc.setValueBypass(oldGamma);
      acc.getCallback().accept(oldGamma);
		}
		Client.notify(Text.translatable("other.mmic.toggled_fullbright", Client.onOff(fullbright)));
	}

	public static boolean lagging = false;
	public static Deque<Packet<?>> packets = new LinkedList<>();

	public static void toggleLag()
	{
		lagging = !lagging;
		if(!lagging && (Config.lagType == LagType.CLOG || Config.lagType == LagType.LOSSY_CLOG))
		{
			packets.forEach(mc.getNetworkHandler()::sendPacket);
			packets.clear();
		}
		Client.notify(Text.translatable("other.mmic.lag_switched", Client.onOff(lagging)));
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
		var tr = mc.textRenderer;
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

	public static boolean zoomed = false;
	private static int oldFOV = 90;
	private static double oldSens = 0.5;
	private static boolean oldSmooth = false;

	private static int zoomMod = 0;

	public static void zoom() {
		applyZoom(true);

		if(Config.zoomSmooth) {
			oldSmooth = mc.options.smoothCameraEnabled;
			mc.options.smoothCameraEnabled = true;
		}

		zoomed = true;
	}
	
	public static void unzoom() {
		zoomMod = 0;
		mc.options.getFov().setValue(oldFOV);
		mc.options.getMouseSensitivity().setValue(oldSens);
		
		if(Config.zoomSmooth) {
			mc.options.smoothCameraEnabled = oldSmooth;
		}
		
		zoomed = false;
	}

	private static void applyZoom(boolean saveOld) {
		var fov = mc.options.getFov();
		if(saveOld) {
			oldFOV = fov.getValue();
		}
		double fovDivMod = (Config.zoomFovDiv/15)*zoomMod;
		double fovDiv = Math.max(Config.zoomFovDiv+fovDivMod, 1.0);
		int newFOV = (int)Math.round(oldFOV/fovDiv);
		((GammaAccessor)(Object)fov).setValueBypass(newFOV);

		var sens = mc.options.getMouseSensitivity();
		if(saveOld) {
			oldSens = sens.getValue();
		}
		double sensDivMod = (Config.zoomSensDiv/20)*zoomMod;
		double sensDiv = Math.max(Config.zoomSensDiv+sensDivMod, 1.0);
		double newSens = oldSens/sensDiv;
		((GammaAccessor)(Object)sens).setValueBypass(newSens);
	}

	// TODO: figure out a max zoomMod for any given fov+divider combo

	public static void zoomIn() {
		if(zoomMod >= 10) return;
		zoomMod++;
		applyZoom(false);
	}
	
	public static void zoomOut() {
		if(zoomMod <= -10) return;
		zoomMod--;
		applyZoom(false);
	}
}
