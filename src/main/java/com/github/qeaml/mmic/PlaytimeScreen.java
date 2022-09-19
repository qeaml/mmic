package com.github.qeaml.mmic;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class PlaytimeScreen extends Screen {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
    .ofPattern("yyyy.MM.dd HH:mm:ss")
    .withZone(ZoneId.systemDefault());

  private static String long2timestamp(long time) {
    return DATE_FORMAT.format(Instant.ofEpochMilli(time));
  }

  private static String long2timespan(long time) {
    long hours = time / (60 * 60 * 1000);
    long totalMinutes = time % (60 * 60 * 1000);
    long minutes = totalMinutes / (60 * 1000);
    long totalSeconds = totalMinutes % (60 * 1000);
    long seconds = totalSeconds / 1000;

    return String.format("%02dh%02dm%02ds", hours, minutes, seconds);
  }

  private class GameSessionEntry extends AlwaysSelectedEntryListWidget.Entry<GameSessionEntry> {
    private final Sessions.Game sesh;

    private GameSessionEntry(Sessions.Game s) {
      sesh = s;
    }

    @Override
    public Text getNarration() {
      return ScreenTexts.EMPTY;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
        int mouseY, boolean hovered, float tickDelta)
    {
      DrawableHelper.drawStringWithShadow(matrices, textRenderer,
        String.format(
          "%s for %s",
          long2timestamp(sesh.start()),
          long2timespan(sesh.end()-sesh.start())),
        x, y,
        0xFFFFFFFF);
    }
  }

  private class GameSessionList extends AlwaysSelectedEntryListWidget<GameSessionEntry> {
    private PlaytimeScreen parent;

    public GameSessionList(PlaytimeScreen p, MinecraftClient client) {
      super(client, p.width, p.height, 32, p.height - 64, 10);
      parent = p;
      Sessions.game.forEach(s -> addEntry(new GameSessionEntry(s)));
    }

    @Override
    protected void renderBackground(MatrixStack matrices) {
      parent.renderBackground(matrices);
    }
  }

  private class WorldSessionEntry extends AlwaysSelectedEntryListWidget.Entry<WorldSessionEntry> {
    private final Sessions.World sesh;

    private WorldSessionEntry(Sessions.World s) {
      sesh = s;
    }

    @Override
    public Text getNarration() {
      return ScreenTexts.EMPTY;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
        int mouseY, boolean hovered, float tickDelta)
    {
      DrawableHelper.drawStringWithShadow(matrices, textRenderer,
        String.format(
          "%s for %s on world \"%s\"",
          long2timestamp(sesh.start()),
          long2timespan(sesh.end()-sesh.start()),
          sesh.world()),
        x, y,
        0xFFFFFFFF);
    }
  }

  private class WorldSessionList extends AlwaysSelectedEntryListWidget<WorldSessionEntry> {
    private PlaytimeScreen parent;

    public WorldSessionList(PlaytimeScreen p, MinecraftClient client) {
      super(client, p.width, p.height, 32, p.height - 64, 10);
      parent = p;
      Sessions.world.forEach(s -> addEntry(new WorldSessionEntry(s)));
    }

    @Override
    protected void renderBackground(MatrixStack matrices) {
      parent.renderBackground(matrices);
    }
  }

  private class ServerSessionEntry extends AlwaysSelectedEntryListWidget.Entry<ServerSessionEntry> {
    private final Sessions.Server sesh;

    private ServerSessionEntry(Sessions.Server s) {
      sesh = s;
    }

    @Override
    public Text getNarration() {
      return ScreenTexts.EMPTY;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
        int mouseY, boolean hovered, float tickDelta)
    {
      DrawableHelper.drawStringWithShadow(matrices, textRenderer,
        String.format(
          "%s for %s on server %s",
          long2timestamp(sesh.start()),
          long2timespan(sesh.end()-sesh.start()),
          sesh.ip()),
        x, y,
        0xFFFFFFFF);
    }
  }

  private class ServerSessionList extends AlwaysSelectedEntryListWidget<ServerSessionEntry> {
    private PlaytimeScreen parent;

    public ServerSessionList(PlaytimeScreen p, MinecraftClient client) {
      super(client, p.width, p.height, 32, p.height - 64, 10);
      parent = p;
      Sessions.server.forEach(s -> addEntry(new ServerSessionEntry(s)));
    }

    @Override
    protected void renderBackground(MatrixStack matrices) {
      parent.renderBackground(matrices);
    }
  }
  
  private GameSessionList game;
  private WorldSessionList world;
  private ServerSessionList server;
  private AlwaysSelectedEntryListWidget<?> selectedList;
  private Screen parent;

  public PlaytimeScreen(Screen parent) {
    super(Text.translatable("gui.mmic.sessions"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    game = new GameSessionList(this, client);
    world = new WorldSessionList(this, client);
    server = new ServerSessionList(this, client);
    selectList(game);
    addDrawableChild(new ButtonWidget(width / 2 - 120, height - 52, 80, 20, Text.translatable("gui.mmic.sessions.game"), (button) -> {
      selectList(game);
    }));
    addDrawableChild(new ButtonWidget(width / 2 - 40, height - 52, 80, 20, Text.translatable("gui.mmic.sessions.world"), (button) -> {
      selectList(world);
    }));
    addDrawableChild(new ButtonWidget(width / 2 + 40, height - 52, 80, 20, Text.translatable("gui.mmic.sessions.server"), (button) -> {
      selectList(server);
    }));
    addDrawableChild(new ButtonWidget(width / 2 - 100, height - 28, 200, 20, ScreenTexts.DONE, (button) -> {
      client.setScreen(parent);
    }));
  }

  private void selectList(AlwaysSelectedEntryListWidget<?> l) {
    if(selectedList != null)
      remove(selectedList);
    
    if(l != null) {
      addSelectableChild(l);
      selectedList = l;
    }
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    selectedList.render(matrices, mouseX, mouseY, delta);
    drawCenteredText(matrices, textRenderer, Text.translatable("gui.mmic.sessions"), width/2, 20, 0xFFFFFFFF);
    super.render(matrices, mouseX, mouseY, delta);
  }
}
