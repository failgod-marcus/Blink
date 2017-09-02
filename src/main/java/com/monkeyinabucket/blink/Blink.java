package com.monkeyinabucket.blink;

import java.io.*;
import java.util.ArrayList;

import com.monkeyinabucket.blink.block.RuneCore;
import com.monkeyinabucket.blink.minecraft.Environment;
import com.monkeyinabucket.blink.rune.BlinkRune;
import com.monkeyinabucket.blink.rune.RuneManager;
import com.monkeyinabucket.world.Coordinates;
import com.monkeyinabucket.world.SerializableLocation;

/**
 * Main class for the Blink plugin. Handles the enabling and disabling process,
 * provides centralized logging features, and acts as the primary container for
 * all objects in the plugin.
 */
public class Blink {

  /** The name of the rune save file. This will be stored in the world folder */
  private static String SAVE_FILE_NAME = "blink.sav";

  /** Configuration options for the mod */
  public static Configuration config;

  /** Information about, and access to the current execution environment */
  public Environment environment;

  /** The data file that will be used to save and load rune locations */
  protected String saveFile;

  /** The primary object used to manage runes in the plugin */
  public static RuneManager runeManager = RuneManager.getInstance();

  public static RuneCore runecore;

  private static Blink instance;

  public static Blink getInstance() {
    if (instance == null) {
      instance = new Blink();
    }

    return instance;
  }

  /**
   * Called when this Plugin is enabled. Initializes the plugin and loads all saved Runes.
   */
  @EventHandler
  public void onInit(FMLInitializationEvent event) {
    GameRegistry.registerBlock(runecore, "runecore");

    ItemStack ironStack = new ItemStack(Items.iron_ingot);
    ItemStack emeraldStack = new ItemStack(Items.emerald);
    ItemStack obsidianStack = new ItemStack(Blocks.obsidian);
    GameRegistry.addRecipe(
      new ItemStack(runecore),
      "xxx",
      "yzy",
      "xxx",
      'x',
      ironStack,
      'y',
      emeraldStack,
      'z',
      obsidianStack
    );
  }

  /**
   * Event handler for the Server Started Event. Sets up the various event
   * listeners for the mod.
   *
   * @param event The server started event.
   */
  @EventHandler
  public void onServerStarted(FMLServerStartedEvent event) {
    MinecraftForge.EVENT_BUS.register(this);
    FMLCommonHandler.instance().bus().register(this);

    saveFile = "saves" + "/" + MinecraftServer.getServer().getFolderName() + '/' + SAVE_FILE_NAME;
  }

  /**
   * Called when this mod is disabled. Shuts down the mod, and saves all Runes.
   */
  @EventHandler
  public void unload(FMLServerStoppingEvent event) {
    saveRunes();
  }

  /**
   * Loads the runes that have been saved to disk. This should be called once
   * when the World is first loaded (Not the Dimension). Calling this a second
   * time would result in duplicate runes being registered.
   */
  public void loadRunes() {
    runeManager.clearRunes();

    ArrayList<SerializableLocation> locs;
    ObjectInputStream stream = null;
    try {
      stream = new ObjectInputStream(new FileInputStream(saveFile));
      locs = (ArrayList<SerializableLocation>) stream.readObject();
    } catch (FileNotFoundException ex) {
      locs = new ArrayList<SerializableLocation>();
    } catch (ClassNotFoundException ex) {
      Logger.severe(ex.getMessage());
      return;
    } catch (IOException ex) {
      Logger.severe(ex.getMessage());
      return;
    } finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (IOException ex) {
        Logger.severe(ex.getMessage());
      }
    }

    MinecraftServer server = MinecraftServer.getServer();
    for (SerializableLocation savedLoc : locs) {
      Coordinates loc = savedLoc.getLocation(server);
      BlinkRune rune = new BlinkRune(loc);
      runeManager.addRune(rune);
    }
  }

  /**
   * Saves the currently registered runes to a save file in the world folder.
   */
  public void saveRunes() {
    ArrayList<SerializableLocation> locs = runeManager.getLocationsForSave();
    ObjectOutputStream os = null;
    try {
      os = new ObjectOutputStream(new FileOutputStream(saveFile));
      os.writeObject(locs);
      os.flush();
      os.close();
    } catch (IOException ex) {
      Logger.severe(ex.getMessage());
    } finally {
      try {
        if (os != null) {
          os.close();
        }
      } catch (IOException ex) {
        Logger.severe(ex.getMessage());
      }
    }
  }
}
