package com.github.qeaml.mmic.gui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.github.qeaml.mmic.Sessions;

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
        sesh.version(),
        x, y,
        0xFFFFFF);
      DrawableHelper.drawStringWithShadow(matrices, textRenderer,
        String.format(
          "%s for %s",
          long2timestamp(sesh.start()),
          long2timespan(sesh.end()-sesh.start())),
        x, y+textRenderer.fontHeight,
        0x909090);
    }
  }

  private class GameSessionList extends AlwaysSelectedEntryListWidget<GameSessionEntry> {
    private PlaytimeScreen parent;
    public int count;

    public GameSessionList(PlaytimeScreen p, MinecraftClient client) {
      super(client, p.width, p.height, 32, p.height - 64, textRenderer.fontHeight*2);
      parent = p;
      count = Sessions.game.size();
      Sessions.game.forEach(s -> addEntry(new GameSessionEntry(s)));
    }

    @Override
    public int getRowLeft() {
      return parent.width/6;
    }

    @Override
    public int getRowRight() {
      return parent.width*5/6;
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
        sesh.world(),
        x, y,
        0xFFFFFF);
      DrawableHelper.drawStringWithShadow(matrices, textRenderer,
        String.format(
          "%s for %s",
          long2timestamp(sesh.start()),
          long2timespan(sesh.end()-sesh.start())),
        x, y+textRenderer.fontHeight,
        0x909090);
    }
  }

  private class WorldSessionList extends AlwaysSelectedEntryListWidget<WorldSessionEntry> {
    private PlaytimeScreen parent;
    public int count;

    public WorldSessionList(PlaytimeScreen p, MinecraftClient client) {
      super(client, p.width, p.height, 32, p.height - 64, textRenderer.fontHeight*2);
      parent = p;
      count = Sessions.world.size();
      Sessions.world.forEach(s -> addEntry(new WorldSessionEntry(s)));
    }

    @Override
    public int getRowLeft() {
      return parent.width/6;
    }

    @Override
    public int getRowRight() {
      return parent.width*5/6;
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
        sesh.ip(),
        x, y,
        0xFFFFFF);
      DrawableHelper.drawStringWithShadow(matrices, textRenderer,
        String.format(
          "%s for %s",
          long2timestamp(sesh.start()),
          long2timespan(sesh.end()-sesh.start())),
        x, y+textRenderer.fontHeight,
        0x909090);
    }
  }

  private class ServerSessionList extends AlwaysSelectedEntryListWidget<ServerSessionEntry> {
    private PlaytimeScreen parent;
    public int count;

    public ServerSessionList(PlaytimeScreen p, MinecraftClient client) {
      super(client, p.width, p.height, 32, p.height - 64, textRenderer.fontHeight*2);
      parent = p;
      count = Sessions.server.size();
      Sessions.server.forEach(s -> addEntry(new ServerSessionEntry(s)));
    }

    @Override
    public int getRowLeft() {
      return parent.width/6;
    }

    @Override
    public int getRowRight() {
      return parent.width*5/6;
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
    addDrawableChild(new ButtonWidget(
      width / 2 - 120, height - 52,
      80, 20,
      Text.translatable("gui.mmic.sessions.game", Integer.toString(game.count)),
    (button) -> {
      selectList(game);
    }));
    addDrawableChild(new ButtonWidget(
      width / 2 - 40, height - 52,
      80, 20,
      Text.translatable("gui.mmic.sessions.world", Integer.toString(world.count)),
    (button) -> {
      selectList(world);
    }));
    addDrawableChild(new ButtonWidget(
      width / 2 + 40, height - 52,
      80, 20,
      Text.translatable("gui.mmic.sessions.server", Integer.toString(server.count)),
    (button) -> {
      selectList(server);
    }));
    addDrawableChild(new ButtonWidget(
      width / 2 - 100, height - 28,
      200, 20,
      ScreenTexts.DONE,
    (button) -> {
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
