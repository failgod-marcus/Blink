package com.monkeyinabucket.world;

import java.io.Serializable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * Wrapper for a Coordinates that implements Serializable. This is used to save the location of runes.
 */
public class SerializableLocation implements Serializable {

  /** The ID of the dimension that this location is within */
  private int dimensionId;

  /** The x coordinate of this location */
  private int x;

  /** The y coordinate of this location */
  private int y;

  /** The z coordinate of this location */
  private int z;

  /**
   * Constructor
   * 
   * @param loc the location to serialize
   */
  public SerializableLocation(Coordinates loc) {
    dimensionId = loc.world.provider.dimensionId;
    x = loc.x;
    y = loc.y;
    z = loc.z;
  }

  /**
   * Provides the Coordinates that this object serialized.
   * 
   * @param server the server that we are running in
   * @return the Coordinates
   */
  public Coordinates getLocation(MinecraftServer server) {
    World world = null;
    for (World nextWorld : server.worldServers) {
      if (nextWorld.provider.dimensionId == dimensionId) {
        world = nextWorld;
        break;
      }
    }

    return new Coordinates(world, x, y, z);
  }
}
