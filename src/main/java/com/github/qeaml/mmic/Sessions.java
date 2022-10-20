package com.github.qeaml.mmic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Sessions {
  private static final MinecraftClient mc = MinecraftClient.getInstance();
  private static final Logger log = LoggerFactory.getLogger(Client.name+"/Sessions");
  private static final Gson gson = new Gson();

  private static final String
  sessionsFilename = "mmic.sessions.json";

  public static final File
  sessionsFile = new File(mc.runDirectory, sessionsFilename);

  public static interface Session {
    public long start();
    public long end();
  }

  public static record Game(String version, long start, long end) implements Session {}
  public static record World(String world, long start, long end) implements Session {}
  public static record Server(String ip, long start, long end) implements Session {}

  public static record SessionCollection(List<Game> game, List<World> world, List<Server> server) {
    public int amt() {
      return game.size()+world.size()+server.size();
    }
  }

  private static Object lock = new Object();

  public static Deque<Game> game = new LinkedList<>();
  public static Deque<World> world = new LinkedList<>();
  public static Deque<Server> server = new LinkedList<>();

  public static Iterable<Session> getGameSessions() {
    return game.stream().map(g -> (Session)g).toList();
  }

  public static Iterable<Session> getWorldSessions() {
    return world.stream().map(w -> (Session)w).toList();
  }

  public static Iterable<Session> getServerSessions() {
    return server.stream().map(s -> (Session)s).toList();
  }

  public static void load() {
    game.clear();
    world.clear();
    server.clear();
    var sessions = loadFrom(sessionsFile, true);
    if(sessions != null) {
      game.addAll(sessions.game);
      world.addAll(sessions.world);
      server.addAll(sessions.server);
    }
  }

  private static SessionCollection loadFrom(File src, boolean ensure) {
    log.info("Loading session data from {}", src.getPath());

    if(ensure) {
      try {
        if(!src.exists()) {
          src.createNewFile();
          try(var w = new FileWriter(src)) {
            w.write("{\"game\":[],\"world\":[],\"server\":[]}");
          }
        }
      } catch(IOException e) {
        log.error(String.format("Could not create session file: %s", e));
        return null;
      }
    }

    List<Game>   games   = new LinkedList<>();
    List<World>  worlds  = new LinkedList<>();
    List<Server> servers = new LinkedList<>();

    try(var fr = new FileReader(src); var json = gson.newJsonReader(fr)) {
      json.beginObject();
      while(json.hasNext()) {
        switch(json.nextName()) {
        case "game":
          games.addAll(readGame(json));
          break;
        case "world":
          worlds.addAll(readWorld(json));
          break;
        case "server":
          servers.addAll(readServer(json));
          break;
        }
      }
      json.endObject();
    } catch(IOException | IllegalStateException e) {
      log.error(String.format("Could not read session file: %s", e));
      return null;
    }

    log.info(String.format(
      "Loaded %d game sessions, %d world sessions, %d server sessions",
      games.size(), worlds.size(), servers.size()));

    var out = new SessionCollection(games, worlds, servers);
    return out;
  }

  private static List<Game> readGame(JsonReader json) throws IOException {
    var out = new LinkedList<Game>();
    json.beginArray();
    while(json.hasNext()) {
      json.beginArray();
      var ver = json.nextString();
      var start = json.nextLong();
      var end = json.nextLong();
      out.add(new Game(ver, start, end));
      json.endArray();
    }
    json.endArray();
    return out;
  }

  private static List<World> readWorld(JsonReader json) throws IOException {
    var out = new LinkedList<World>();
    json.beginArray();
    while(json.hasNext()) {
      json.beginArray();
      var worldName = json.nextString();
      var start = json.nextLong();
      var end = json.nextLong();
      out.add(new World(worldName, start, end));
      json.endArray();
    }
    json.endArray();
    return out;
  }

  private static List<Server> readServer(JsonReader json) throws IOException {
    var out = new LinkedList<Server>();
    json.beginArray();
    while(json.hasNext()) {
      json.beginArray();
      var ip = json.nextString();
      var start = json.nextLong();
      var end = json.nextLong();
      out.add(new Server(ip, start, end));
      json.endArray();
    }
    json.endArray();
    return out;
  }

  public static void save() {
    log.info("Saving session data");

    synchronized(lock) {
      try(var fw = new FileWriter(sessionsFile); var json = gson.newJsonWriter(fw)) {
        json.beginObject();

        json.name("game");
        json.beginArray();
        for(var g: game)
          json.beginArray()
              .value(g.version)
              .value(g.start)
              .value(g.end)
              .endArray();
        json.endArray();
        
        json.name("world");
        json.beginArray();
        for(var w: world)
          json.beginArray()
              .value(w.world)
              .value(w.start)
              .value(w.end)
              .endArray();
        json.endArray();
        
        json.name("server");
        json.beginArray();
        for(var s: server)
          json.beginArray()
              .value(s.ip)
              .value(s.start)
              .value(s.end)
              .endArray();
        json.endArray();

        json.endObject();
      } catch(IOException | IllegalStateException e) {
        log.error(String.format("Could not save world session data: %s", e));
        e.printStackTrace();
      }
    }
  }

  public static boolean migrate() {
    var root = Path.of(mc.runDirectory.getAbsolutePath());
    for(int i = 0; i < Client.config.migrationDepth.get(); i++) {
      root = root.getParent();
    }

    var amt = Arrays.asList(new File(root.toString()).listFiles())
      .parallelStream()
      .filter(f -> f.isDirectory())
      .map(Sessions::tryMigrateDir)
      .reduce((sum, curr) -> sum + curr)
      .get();

    save();

    Client.notify(Text.translatable("gui.mmic.sessions.migrate.finish", amt));

    return true;
  }

  private static int tryMigrateDir(File dir) {
    int amt = 0;
    for(File f: dir.listFiles()) {
      if(f.getAbsolutePath().startsWith(mc.runDirectory.getAbsolutePath()))
        continue;
      if(f.isDirectory())
        amt += tryMigrateDir(f);
      else if(f.getName() == sessionsFilename) {
        var sessions = loadFrom(f, false);
        game.addAll(sessions.game);
        world.addAll(sessions.world);
        server.addAll(sessions.server);
        amt += sessions.amt();
      }
    }
    return amt;
  }

  private static long gameSessionStart;
  private static long subSessionStart;
  private static boolean inSubSession;
  private static boolean isOnServer;
  private static String worldName;
  private static String serverIp;

  public static void startGameSession() {
    log.info(String.format("Starting game session"));
    gameSessionStart = System.currentTimeMillis();
  }

  public static void startWorld(String name) {
    log.info(String.format("Starting world session on %s", name));
    inSubSession = true;
    subSessionStart = System.currentTimeMillis();
    isOnServer = false;
    worldName = name;
  }
  
  public static void startServer(String ip) {
    log.info(String.format("Starting server session on %s", ip));
    inSubSession = true;
    subSessionStart = System.currentTimeMillis();
    isOnServer = true;
    serverIp = ip;
  }

  public static void endSubSession() {
    if(!inSubSession) return;
    log.info("Subsession ended");
    if(isOnServer)
      server(serverIp, subSessionStart, System.currentTimeMillis());
    else
      world(worldName, subSessionStart, System.currentTimeMillis());
    inSubSession = false;
    Client.clearCurrentChunk();
    if(Client.isFullbright())
      Client.toggleFullbright();
  }

  public static void endGameSession() {
    log.info("Game session ended.");
    game(
      SharedConstants.getGameVersion().getName(),
      gameSessionStart,
      System.currentTimeMillis());
    save();
  }

  public static void game(String version, long start, long end) {
    log.info(String.format("Game session: %d-%d on %s", start, end, version));
    synchronized(lock) {
      game.addFirst(new Game(version, start, end));
    }
  }

  public static void world(String w, long start, long end) {
    log.info(String.format("World session: %d-%d on %s", start, end, w));
    synchronized(lock) {
      world.addFirst(new World(w, start, end));
    }
  }

  public static void server(String ip, long start, long end) {
    log.info(String.format("Server session: %d-%d on %s", start, end, ip));
    synchronized(lock) {
      server.addFirst(new Server(ip, start, end));
    }
  }

  public static long gameSessionDuration() {
    return System.currentTimeMillis()-gameSessionStart;
  }

  public static long subSessionDuration() {
    return System.currentTimeMillis()-subSessionStart;
  }

  public static boolean isInSubSession() {
    return inSubSession;
  }

  public static boolean isInServerSession() {
    return isOnServer;
  }
}
