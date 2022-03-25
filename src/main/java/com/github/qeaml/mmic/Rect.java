package com.github.qeaml.mmic;

public record Rect(Point a, Point b) {
	public Rect(float x1, float y1, float x2, float y2) {
		this(new Point(x1, y1), new Point(x2, y2));
	}

	public static Rect line(float p, boolean horiz) {
		if(horiz)
			return new Rect(0, p, 1, p);
		return new Rect(p, 0, p, 1);
	}

	public String toString() {
		return String.format("[ %s / %s ]", a, b);
	}
}
