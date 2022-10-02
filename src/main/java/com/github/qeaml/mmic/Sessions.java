package com.github.qeaml.mmic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Sessions {
  private static final MinecraftClient mc = MinecraftClient.getInstance();
  private static final Logger log = LoggerFactory.getLogger(Client.name+"/Sessions");

  public static final File
  gameSessions = new File(mc.runDirectory, "mmic.gameSessions.csv");
  public static final File
  worldSessions = new File(mc.runDirectory, "mmic.worldSessions.csv");
  public static final File
  serverSessions = new File(mc.runDirectory, "mmic.serverSessions.csv");

  public static interface Session {
    public long start();
    public long end();
  }

  public static record Game(String version, long start, long end) implements Session {}
  public static record World(String world, long start, long end) implements Session {}
  public static record Server(String ip, long start, long end) implements Session {}

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
    log.info("Loading session data");

    try {
      if(!gameSessions.exists())
        gameSessions.createNewFile();
      if(!worldSessions.exists())
        worldSessions.createNewFile();
      if(!serverSessions.exists())
        serverSessions.createNewFile();
    } catch(IOException e) {
      log.error(String.format("Could not create session files: %s", e));
    }

    synchronized(lock) {
      game.clear();
      game.addAll(loadGame(gameSessions));

      world.clear();
      world.addAll(loadWorld(worldSessions));

      server.clear();
      server.addAll(loadServer(serverSessions));
    }

    log.info(String.format(
      "Loaded %d game sessions, %d world sessions, %d server sessions",
      game.size(), world.size(), server.size()));
  }

  private static List<Game> loadGame(File src) {
    var out = new LinkedList<Game>();

    try(var fr = new FileReader(src); var csv = new CSVReader(fr)) {
      String[] row;
      while((row = csv.readNext()) != null) {
        var ver = row.length < 3 ? "Unknown" : row[2];
        out.addLast(new Game(ver, Long.parseLong(row[0]), Long.parseLong(row[1])));
      }
    } catch(CsvValidationException | IOException e) {
      log.error(String.format("Could not load game session data: %s", e));
    }

    return out;
  }

  private static List<World> loadWorld(File src) {
    var out = new LinkedList<World>();

    try(var fr = new FileReader(src); var csv = new CSVReader(fr)) {
      String[] row;
      while((row = csv.readNext()) != null)
        out.addLast(new World(row[2], Long.parseLong(row[0]), Long.parseLong(row[1])));
    } catch(CsvValidationException | IOException e) {
      log.error(String.format("Could not load world session data: %s", e));
    }

    return out;
  }

  private static List<Server> loadServer(File src) {
    var out = new LinkedList<Server>();

    try(var fr = new FileReader(src); var csv = new CSVReader(fr)) {
      String[] row;
      while((row = csv.readNext()) != null)
        out.addLast(new Server(row[2], Long.parseLong(row[0]), Long.parseLong(row[1])));
    } catch(CsvValidationException | IOException e) {
      log.error(String.format("Could not load server session data: %s", e));
    }

    return out;
  }

  public static void save() {
    log.info("Saving session data");

    synchronized(lock) {
      try(var fw = new FileWriter(gameSessions); var csv = new CSVWriter(fw)) {
        game.forEach(g -> csv.writeNext(new String[]{
          Long.toString(g.start), Long.toString(g.end), g.version
        }));
      } catch(IOException e) {
        log.error(String.format("Could not save game session data: %s", e));
      }

      try(var fw = new FileWriter(worldSessions); var csv = new CSVWriter(fw)) {
        world.forEach(w -> csv.writeNext(new String[]{
          Long.toString(w.start), Long.toString(w.end), w.world
        }));
      } catch(IOException e) {
        log.error(String.format("Could not save world session data: %s", e));
      }

      try(var fw = new FileWriter(serverSessions); var csv = new CSVWriter(fw)) {
        server.forEach(s -> csv.writeNext(new String[]{
          Long.toString(s.start), Long.toString(s.end), s.ip
        }));
      } catch(IOException e) {
        log.error(String.format("Could not save server session data: %s", e));
      }
    }
  }

  public static boolean migrate() {
    var root = Path.of(mc.runDirectory.getAbsolutePath());
    for(int i = 0; i < Client.config.migrationDepth.get(); i++) {
      root = root.getParent();
    }

    var all = Arrays.asList(new File(root.toString()).listFiles())
      .parallelStream()
      .filter(f -> f.isDirectory())
      .map(Sessions::tryMigrateDir)
      .reduce((sum, curr) -> {sum.addAll(curr); return sum;})
      .orElse(new LinkedList<>());

    var cmp = Comparator.comparingLong((Session s) -> s.start()).reversed();
    var newGame = new LinkedList<Game>();
    newGame.addAll(game);
    newGame.addAll(all.stream()
      .filter(s -> {return (s instanceof Game);})
      .map(s -> (Game)s)
      .toList());
    game.clear();
    game.addAll(newGame.stream().sorted(cmp).toList());

    var newWorld = new LinkedList<World>();
    newWorld.addAll(world);
    newWorld.addAll(all.stream()
      .filter(s -> {return (s instanceof World);})
      .map(s -> (World)s)
      .toList());
    world.clear();
    world.addAll(newWorld.stream().sorted(cmp).toList());

    var newServer = new LinkedList<Server>();
    newServer.addAll(server);
    newServer.addAll(all.stream()
      .filter(s -> {return (s instanceof Server);})
      .map(s -> (Server)s)
      .toList());
    server.clear();
    server.addAll(newServer.stream().sorted(cmp).toList());

    save();

    Client.notify(Text.translatable("gui.mmic.sessions.migrate.finish", all.size()));

    return true;
  }

  private static List<Session> tryMigrateDir(File dir) {
    var out = new LinkedList<Session>();
    for(File f: dir.listFiles()) {
      if(f.getAbsolutePath().startsWith(mc.runDirectory.getAbsolutePath()))
        continue;
      if(f.isDirectory())
        out.addAll(tryMigrateDir(f));
      else switch(f.getName()) {
        case "mmic.gameSessions.csv":
          out.addAll(loadGame(f));
          f.delete();
          break;
        case "mmic.worldSessions.csv":
          out.addAll(loadWorld(f));
          f.delete();
          break;
        case "mmic.serverSessions.csv":
          out.addAll(loadServer(f));
          f.delete();
          break;
      }
    }
    return out;
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
