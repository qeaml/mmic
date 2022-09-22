package com.github.qeaml.mmic.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class LoadingScreen<T> extends Screen {
  public interface Loader<T> {
    T load();
    void callback(T result);
  
    public static <E> Loader<E> of(Supplier<E> j, Consumer<E> c) {
      return new Loader<E>() {
        @Override
        public E load() {
          return j.get();
        }

        @Override
        public void callback(E result) {
          c.accept(result);
        }

        // @Override
        // public String progress() {
        //   return p.get();
        // }
      };
    }
  }

  private Loader<T> ld;
  private T result;
  private boolean complete;

  protected LoadingScreen(Text msg, Loader<T> loader) {
    super(msg);
    ld = loader;
  }

  @Override
  protected void init() {
    var t = new Thread(() -> {
      result = ld.load();
      complete = true;
    });
    t.setName("LoadingScreen job");
    t.start();
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    renderBackgroundTexture(0);
    drawCenteredText(matrices, textRenderer, title, width / 2, 70, 0xFFFFFF);
    // drawCenteredText(matrices, textRenderer, ld.progress(), width / 2, 90, 0xFFFFFF);
    super.render(matrices, mouseX, mouseY, delta);

    if(complete)
      ld.callback(result);
  }
}
