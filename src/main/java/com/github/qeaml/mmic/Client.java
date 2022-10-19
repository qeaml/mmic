package com.github.qeaml.mmic;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.qeaml.mmic.config.Config;
import com.github.qeaml.mmic.mixin.BiomeAccessAccess;
import com.github.qeaml.mmic.mixin.OptionAccessor;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class Client {
  public static final String name = "MMIC";

  public static final Logger log = LoggerFactory.getLogger(name);
  public static final MinecraftClient mc = MinecraftClient.getInstance();

  public static Config config;

  public static void init() {
    log.info("Hello world");

    config = new Config(new File(mc.runDirectory, "options_mmic.txt"));
    config.load();

    var visuals = "key.categories.mmic.visuals";
    Keys.gammaInc = new Bind("key.mmic.gammaInc", GLFW.GLFW_KEY_RIGHT_BRACKET, visuals);
    Keys.gammaDec = new Bind("key.mmic.gammaDec", GLFW.GLFW_KEY_LEFT_BRACKET, visuals);
    Keys.fullbright = new Bind("key.mmic.fullbright", GLFW.GLFW_KEY_APOSTROPHE, visuals);
    Keys.lagSwitch = new Bind("key.mmic.lagSwitch", GLFW.GLFW_KEY_BACKSLASH, KeyBinding.GAMEPLAY_CATEGORY);
    Keys.zoom = new Bind("key.mmic.zoom", GLFW.GLFW_KEY_C, KeyBinding.GAMEPLAY_CATEGORY);

    for(Grid g: Grid.values()) {
      g.toggle = new Bind("other.mmic.grid."+g.name, g.key, "key.categories.mmic.grids");
    }

    Sessions.load();
    Sessions.startGameSession();
  }

  public static void tick() {
    var p = mc.getProfiler();
    p.push(name);
    if(Keys.lagSwitch.wasJustPressed())
      toggleLag();
    if(Keys.fullbright.wasJustPressed())
      toggleFullbright();
    if(fullbright && mc.options.getGamma().getValue() != 10.0)
      toggleFullbright();
    if(!fullbright)
    {
      if(Keys.gammaInc.wasJustPressed() && mc.options.getGamma().getValue() < 3.0) {
        changeGamma(config.gammaStep.get());
      }
      if(Keys.gammaDec.wasJustPressed() && mc.options.getGamma().getValue() > 0.0) {
        changeGamma(-config.gammaStep.get());
      }
    }
    for(var g: Grid.values())
      if(g.toggle.wasJustPressed())
      {
        g.show = !g.show;
        notify(Text.translatable("other.mmic.toggled_grid",
          Text.translatable("other.mmic.grid."+g.name), onOff(g.show)));
      }
    
    if(zoomed && !Keys.zoom.isPressed())
      unzoom();
    else if(!zoomed && Keys.zoom.isPressed())
      zoom();

    p.pop();
  }

  public static void stop() {
    log.info("Goodbye world");

    Sessions.endSubSession();
    Sessions.endGameSession();
  }

  //
  // ─── FULLBRIGHT ─────────────────────────────────────────────────────────────────
  //

  private static boolean fullbright = false;
  private static double oldGamma = 0.5;

  public static boolean isFullbright() {
    return fullbright;
  }

  public static void toggleFullbright() {
    fullbright = !fullbright;
    var acc = (OptionAccessor)(Object)mc.options.getGamma();
    if(fullbright) {
      oldGamma = mc.options.getGamma().getValue();
      acc.setValueBypass(10.0);
      acc.getCallback().accept(10.0);
    } else {
      acc.setValueBypass(oldGamma);
      acc.getCallback().accept(oldGamma);
    }
    notify(Text.translatable("other.mmic.toggled_fullbright", onOff(fullbright)));
  }

  private static void changeGamma(double amt) {
    var opt = mc.options.getGamma();
    var gamma = opt.getValue() + amt;
    var acc = (OptionAccessor)(Object)opt;
    acc.setValueBypass(gamma);
    acc.getCallback().accept(gamma);
    notify(Text.translatable("other.mmic.changed_gamma", Math.round(gamma * 100)));
  }

  //
  // ─── LAG SWITCH ─────────────────────────────────────────────────────────────────
  //

  private static boolean lagging = false;
  private static Deque<Packet<?>> packets = new LinkedList<>();

  public static boolean isLagging() {
    return lagging;
  }

  public static void toggleLag() {
    lagging = !lagging;
    if(!lagging && config.lagType.get().clogs())
    {
      packets.forEach(mc.getNetworkHandler()::sendPacket);
      packets.clear();
    }
    notify(Text.translatable("other.mmic.lag_switched", onOff(lagging)));
  }

  public static void clogPacket(Packet<?> packet) {
    packets.add(packet);
  }

  //
  // ─── ZOOM ───────────────────────────────────────────────────────────────────────
  //

  private static boolean zoomed = false;
  private static int oldFOV = 90;
  private static double oldSens = 0.5;
  private static boolean oldSmooth = false;
  private static int zoomMod = 0;

  public static boolean isZoomed() {
    return zoomed;
  }

  public static void zoom() {
    applyZoom(true);

    if(config.zoomSmooth.get()) {
      oldSmooth = mc.options.smoothCameraEnabled;
      mc.options.smoothCameraEnabled = true;
    }

    zoomed = true;
  }
  
  public static void unzoom() {
    zoomMod = 0;
    mc.options.getFov().setValue(oldFOV);
    mc.options.getMouseSensitivity().setValue(oldSens);
    
    if(config.zoomSmooth.get()) {
      mc.options.smoothCameraEnabled = oldSmooth;
    }

    zoomed = false;
  }

  private static void applyZoom(boolean saveOld) {
    var fov = mc.options.getFov();
    if(saveOld) {
      oldFOV = fov.getValue();
    }
    double fovDivMod = (config.zoomFovDiv.get()/15)*zoomMod;
    double fovDiv = Math.max(config.zoomFovDiv.get()+fovDivMod, 1.0);
    int newFOV = (int)Math.round(oldFOV/fovDiv);
    ((OptionAccessor)(Object)fov).setValueBypass(newFOV);

    var sens = mc.options.getMouseSensitivity();
    if(saveOld) {
      oldSens = sens.getValue();
    }
    double sensDivMod = (config.zoomSensDiv.get()/20)*zoomMod;
    double sensDiv = Math.max(config.zoomSensDiv.get()+sensDivMod, 1.0);
    double newSens = oldSens/sensDiv;
    ((OptionAccessor)(Object)sens).setValueBypass(newSens);
  }

  // TODO: figure out a max zoomMod for any given fov+divider combo

  public static void zoomIn() {
    if(zoomMod >= 10) return;
    zoomMod++;
    applyZoom(false);
  }
  
  public static void zoomOut() {
    if(zoomMod <= -10) return;
    zoomMod--;
    applyZoom(false);
  }

  //
  // ─── UTILITIES ──────────────────────────────────────────────────────────────────
  //

  public static Text onOff(boolean on) {
    var tkey = "other.mmic." + (on ? "on" : "off");
    var color = on ? Formatting.GREEN : Formatting.RED;
    var style = Style.EMPTY.withColor(color);
    return Text.translatable(tkey).setStyle(style);
  }

  public static void notify(Text message) {
    sound(SoundEvents.UI_BUTTON_CLICK, .25f, 1f);
    if(mc.currentScreen == null)
      mc.player.sendMessage(message, true);
    else
      SystemToast.show(mc.getToastManager(),
        SystemToast.Type.PERIODIC_NOTIFICATION,
        Text.of(name),
        message);
    log.info(message.getString());
  }

  public static void error(String fmt, Object... args) {
    var txt = String.format(fmt, args);
    log.error(txt);
    // notify(Text.of(txt));
  }

  private static Random soundRandom = Random.create();

  public static void sound(SoundEvent sound, float volume, float pitch) {
    mc.getSoundManager().play(
      new PositionedSoundInstance(
        sound,
        SoundCategory.MASTER,
        volume, pitch,
        soundRandom,
        0, 0, 0));
  }

  //
  // ─── CHUNK NAMES ────────────────────────────────────────────────────────────────
  //

  private static final Long2ObjectMap<String> chunkNameCache = new Long2ObjectArrayMap<>();
  private static String chunkName = "";

  public static void setCurrentChunk(ChunkPos pos) {
    chunkName = chunkName(mc.player.getWorld(), pos);
  }

  public static String getCurrentChunk() {
    return chunkName;
  }

  public static void clearCurrentChunk() {
    chunkName = "";
    chunkNameCache.clear();
  }

  private static final char[] VOWELS = "aeiouáéó".toCharArray();
  private static final char[] CONSONANTS = "bcdfgklmnoprstz".toCharArray();
  private static final String[] VOWL_DIPH = new String[] { "ie", "ei", "ia" };
  private static final String[] CONS_DIPH = new String[] { "sh", "ch", "tch", "th", "tz", "zh" };

  private static String chunkName(World world, ChunkPos pos) {
    var seed = ((BiomeAccessAccess)world.getBiomeAccess()).getSeed();
    var id = seed * pos.toLong();
    if(chunkNameCache.containsKey(id))
      return chunkNameCache.get(id);
    var name = generateChunkName(id);
    chunkNameCache.put(id, name);
    return name;
  }

  private static String generateChunkName(long id) {
    var r = Random.create(id);
    var wordCount = r.nextBetween(2, 3);
    var words = new String[wordCount];
    for(int i = 0; i < wordCount; i++)
      words[i] = generateWord(r);
    return String.join("-", words);
  }

  private static String generateWord(Random r) {
    var len = r.nextBetween(1, 2);
    var out = "";
    if(r.nextBoolean()) {
      out += nextVowl(r);
      out += "'";
    } else if(r.nextBoolean()) {
      out += nextVowl(r);
      out += " ";
    }
    for(int i = 0; i < len; i++) {
      out += nextVowl(r);
      out += nextCons(r);
    }
    if(r.nextBoolean())
      out += nextVowl(r);
    return Character.toUpperCase(out.charAt(0)) + out.substring(1);
  }

  private static String nextCons(Random r) {
    if(r.nextBoolean())
      return CONS_DIPH[r.nextInt(CONS_DIPH.length)];
    return Character.toString(CONSONANTS[r.nextInt(CONSONANTS.length)]);
  }

  private static String nextVowl(Random r) {
    if(r.nextBoolean())
      return VOWL_DIPH[r.nextInt(VOWL_DIPH.length)];
    return Character.toString(VOWELS[r.nextInt(VOWELS.length)]);
  }
}
