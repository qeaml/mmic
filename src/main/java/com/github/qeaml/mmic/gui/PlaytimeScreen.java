package com.github.qeaml.mmic.gui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.github.qeaml.mmic.Sessions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
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
    public int getRowWidth() {
      return width * 2 / 3;
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
    public int getRowWidth() {
      return width * 2 / 3;
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
    public int getRowWidth() {
      return width * 2 / 3;
    }

    @Override
    protected void renderBackground(MatrixStack matrices) {
      parent.renderBackground(matrices);
    }
  }
  
  private GameSessionList game;
  private WorldSessionList world;
  private ServerSessionList server;

  private Text totalText;
  private Text maxText;
  private Text minText;
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
    var that = this;

    selectList(game);

    addDrawableChild(ButtonWidget.builder(
      Text.translatable("gui.mmic.sessions.game", Integer.toString(game.count)),
      (button) -> {
        selectList(game);
      })
      .position(width / 2 - 120, height - 52)
      .width(80)
      .build());
    addDrawableChild(ButtonWidget.builder(
      Text.translatable("gui.mmic.sessions.world", Integer.toString(world.count)),
      (button) -> {
        selectList(world);
      })
      .position(width / 2 - 40, height - 52)
      .width(80)
      .build());
    addDrawableChild(ButtonWidget.builder(
      Text.translatable("gui.mmic.sessions.server", Integer.toString(server.count)),
      (button) -> {
        selectList(server);
      })
      .position(width / 2 + 40, height - 52)
      .width(80)
      .build());

    addDrawableChild(ButtonWidget.builder(
      Text.translatable("gui.mmic.sessions.migrate"),
      (button) -> {
        client.setScreen(new ConfirmScreen(
        (ok) -> {
          if(!ok) {
            client.setScreen(that);
            return;
          }
          client.setScreen(new LoadingScreen<>(
            Text.translatable("gui.mmic.sessions.migrate.progress"),
            LoadingScreen.Loader.of(
              Sessions::migrate,
              (success) -> client.setScreen(that)
            )));
        },
        Text.translatable("gui.mmic.sessions.migrate"),
        Text.translatable("gui.mmic.sessions.migrate.confirm")));
      })
      .position(width - 85, height - 45)
      .width(80)
      .build());
    addDrawableChild(ButtonWidget.builder(
      Text.translatable("gui.mmic.sessions.clear"),
      (button) -> {
        client.setScreen(new ConfirmScreen(
        (ok) -> {
          client.setScreen(that);
          if(!ok) return;
          Sessions.game.clear();
          Sessions.world.clear();
          Sessions.server.clear();
          Sessions.save();
        },
        Text.translatable("gui.mmic.sessions.clear"),
        Text.translatable("gui.mmic.sessions.clear.confirm")));
      })
      .position(width - 85, height - 25)
      .width(80)
      .build());

    addDrawableChild(ButtonWidget.builder(
      ScreenTexts.DONE,
      (button) -> {
        client.setScreen(parent);
      })
      .position(width / 2 - 100, height - 28)
      .width(200)
      .build());
  }

  private void selectList(AlwaysSelectedEntryListWidget<?> l) {
    if(selectedList != null)
      remove(selectedList);

    if(l != null) {
      addSelectableChild(l);
      selectedList = l;

      totalText = maxText = minText = Text.empty();

      if(l instanceof GameSessionList)
        updateStatText(Sessions.getGameSessions());
      if(l instanceof WorldSessionList)
        updateStatText(Sessions.getWorldSessions());
      if(l instanceof ServerSessionList)
        updateStatText(Sessions.getServerSessions());
    }
  }

  private void updateStatText(Iterable<Sessions.Session> sl) {
    long total = 0, min = 0, max = 0;
    for(var s: sl) {
      var t = s.end() - s.start();
      total += t;
      if(t > max) max = t;
      if(t < min || min == 0) min = t;
    }
    totalText = Text.translatable("gui.mmic.sessions.total", long2timespan(total));
    maxText = Text.translatable("gui.mmic.sessions.max", long2timespan(max));
    minText = Text.translatable("gui.mmic.sessions.min", long2timespan(min));
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    selectedList.render(matrices, mouseX, mouseY, delta);

    String curr;
    if(selectedList instanceof GameSessionList) {
      curr = long2timespan(Sessions.gameSessionDuration());
      drawTextWithShadow(matrices, textRenderer,
      Text.translatable("gui.mmic.sessions.current", curr),
        5, height - 5 - textRenderer.fontHeight*4,
        0xFFFFFF);
    } else if(Sessions.isInSubSession()) {
      curr = long2timespan(Sessions.subSessionDuration());
      if(selectedList instanceof WorldSessionList && !Sessions.isInServerSession())
        drawTextWithShadow(matrices, textRenderer,
          Text.translatable("gui.mmic.sessions.current", curr),
          5, height - 5 - textRenderer.fontHeight*4,
          0xFFFFFF);
      else if(selectedList instanceof ServerSessionList && Sessions.isInServerSession())
        drawTextWithShadow(matrices, textRenderer,
          Text.translatable("gui.mmic.sessions.current", curr),
          5, height - 5 - textRenderer.fontHeight*4,
          0xFFFFFF);
    }

    drawTextWithShadow(matrices, textRenderer,
      totalText,
      5, height - 5 - textRenderer.fontHeight*3,
      0xFFFFFF);
    drawTextWithShadow(matrices, textRenderer,
      maxText,
      5, height - 5 - textRenderer.fontHeight*2,
      0xFFFFFF);
    drawTextWithShadow(matrices, textRenderer,
      minText,
      5, height - 5 - textRenderer.fontHeight,
      0xFFFFFF);
    drawCenteredText(matrices, textRenderer,
      Text.translatable("gui.mmic.sessions"),
      width/2, 20,
      0xFFFFFFFF);
    super.render(matrices, mouseX, mouseY, delta);
  }
}
