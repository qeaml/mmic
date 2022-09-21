package com.github.qeaml.mmic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import net.minecraft.client.MinecraftClient;

public class Sessions {
  private static MinecraftClient mc = MinecraftClient.getInstance();
  private static Logger log = LoggerFactory.getLogger(Client.name+"/Sessions");

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
      String[] row;
      try(var fr = new FileReader(gameSessions); var csv = new CSVReader(fr)) {
        game.clear();
        while((row = csv.readNext()) != null) {
          var ver = row.length < 3 ? "Unknown" : row[2];
          
          game.addLast(new Game(ver, Long.parseLong(row[0]), Long.parseLong(row[1])));
        }
      } catch(CsvValidationException | IOException e) {
        log.error(String.format("Could not load game session data: %s", e));
      }

      try(var fr = new FileReader(worldSessions); var csv = new CSVReader(fr)) {
        world.clear();
        while((row = csv.readNext()) != null)
        world.addLast(new World(row[2], Long.parseLong(row[0]), Long.parseLong(row[1])));
      } catch(CsvValidationException | IOException e) {
        log.error(String.format("Could not load world session data: %s", e));
      }
      
      try(var fr = new FileReader(serverSessions); var csv = new CSVReader(fr)) {
        server.clear();
        while((row = csv.readNext()) != null)
          server.addLast(new Server(row[2], Long.parseLong(row[0]), Long.parseLong(row[1])));
      } catch(CsvValidationException | IOException e) {
        log.error(String.format("Could not load server session data: %s", e));
      }
    }

    log.info(String.format(
      "Loaded %d game sessions, %d world sessions, %d server sessions",
      game.size(), world.size(), server.size()));
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

  public static void migrate() {

  }

  private static long sessionStart;
  private static boolean isServer;
  private static String worldName;
  private static String serverIp;

  public static void startWorld(String name) {
    log.info(String.format("Starting world session on %s", name));
    sessionStart = System.currentTimeMillis();
    isServer = false;
    worldName = name;
  }

  public static void startServer(String ip) {
    sessionStart = System.currentTimeMillis();
    isServer = true;
    serverIp = ip;
  }

  public static void end() {
    log.info("Session ended");
    if(isServer)
      server(serverIp, sessionStart, System.currentTimeMillis());
    else
      world(worldName, sessionStart, System.currentTimeMillis());
  }

  public static void game(String version, long start, long end) {
    log.info(String.format("Game session: %d-%d", start, end));
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
}
