package com.github.qeaml.mmic;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.option.KeyBinding;

public class Keys {
	public static final Bind gammaIncKey = new Bind("key.mmic.gammaInc", GLFW.GLFW_KEY_RIGHT_BRACKET, KeyBinding.MISC_CATEGORY);
	public static final Bind gammaDecKey = new Bind("key.mmic.gammaDec", GLFW.GLFW_KEY_LEFT_BRACKET, KeyBinding.MISC_CATEGORY);
	public static final Bind fullbrightKey = new Bind("key.mmic.fullbright", GLFW.GLFW_KEY_APOSTROPHE, KeyBinding.MISC_CATEGORY);
}
