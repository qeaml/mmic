package com.github.qeaml.mmic;

public record Point(float x, float y) {
	public String toString() {
		return String.format("(%.2f, %.2f)", x, y);
	}
}
