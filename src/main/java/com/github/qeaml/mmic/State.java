package com.github.qeaml.mmic;

import java.util.Deque;
import java.util.LinkedList;

import com.github.qeaml.mmic.Config.LagType;
import com.github.qeaml.mmic.mixin.OptionAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.Packet;
import net.minecraft.text.Text;

public class State {
	private static MinecraftClient mc = MinecraftClient.getInstance();

	public static boolean fullbright = false;
	private static double oldGamma = 0.5;

	public static void changeGamma(double amt) {
		var opt = mc.options.getGamma();
		var gamma = opt.getValue() + amt;
		var acc = (OptionAccessor)(Object)opt;
		acc.setValueBypass(gamma);
		acc.getCallback().accept(gamma);
		Client.notify(Text.translatable("other.mmic.changed_gamma", Math.round(gamma * 100)));
	}

	public static void toggleFullbright()
	{
		fullbright = !fullbright;
		var acc = (OptionAccessor)(Object)mc.options.getGamma();
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
		((OptionAccessor)(Object)fov).setValueBypass(newFOV);

		var sens = mc.options.getMouseSensitivity();
		if(saveOld) {
			oldSens = sens.getValue();
		}
		double sensDivMod = (Config.zoomSensDiv/20)*zoomMod;
		double sensDiv = Math.max(Config.zoomSensDiv+sensDivMod, 1.0);
		double newSens = oldSens/sensDiv;
		((OptionAccessor)(Object)sens).setValueBypass(newSens);
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
