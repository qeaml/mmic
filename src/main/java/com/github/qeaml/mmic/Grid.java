package com.github.qeaml.mmic;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;

public enum Grid {
	THIRDS("thirds", GLFW.GLFW_KEY_B,
		Rect.line(1f/3f, false),
		Rect.line(2f/3f, false),
		Rect.line(1f/3f, true),
		Rect.line(2f/3f, true)),
	HALVES("halves", GLFW.GLFW_KEY_N,
		Rect.line(1f/2f, false),
		Rect.line(1f/2f, true));

	public String name;
	public Rect[] rects;
	public boolean show = false;
	public KeyBinding toggle;
	public boolean togglePrev = false;

	private Grid(String n, int key, Rect ...r) {
		rects = r;
		name = n;
		toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.mmic.grid." + n,
			key,
			"key.categories.mmic.grids"));
	}
}
