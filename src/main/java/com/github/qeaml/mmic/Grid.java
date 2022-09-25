package com.github.qeaml.mmic;

import org.lwjgl.glfw.GLFW;

public enum Grid {
  THIRDS("thirds", GLFW.GLFW_KEY_B,
    Rect.line(1f/3f, false),
    Rect.line(2f/3f, false),
    Rect.line(1f/3f, true),
    Rect.line(2f/3f, true)),
  HALVES("halves", GLFW.GLFW_KEY_N,
    Rect.line(1f/2f, false),
    Rect.line(1f/2f, true)),
  NINTHS("ninths", GLFW.GLFW_KEY_M,
    Rect.line(1f/9f, false),
    Rect.line(8f/9f, false),
    Rect.line(1f/9f, true),
    Rect.line(8f/9f, true));

  public String name;
  public Rect[] rects;
  public boolean show = false;
  public Bind toggle;
  public int key;

  private Grid(String n, int key, Rect ...r) {
    rects = r;
    name = n;
    this.key = key;
  }

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

  public record Point(float x, float y) {
    public String toString() {
      return String.format("(%.2f, %.2f)", x, y);
    }
  }
}




