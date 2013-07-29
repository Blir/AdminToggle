package com.minepop.servegame.admintoggle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Blir
 * @version 1.2.2
 * @since 10 June 2013
 */
public class WorldGroup {

    private String name;
    private List<String> worlds = new ArrayList<String>(0);

    /**
     * Creates a new WorldGroup with the specified name.
     *
     * @param name the name of the World this WorldGroup is representing
     */
    public WorldGroup(String name) {
        this.name = name;
    }

    /**
     * Creates a new WorldGroup with the specified name and the given initial
     * world.
     *
     * @param name
     * @param world
     */
    public WorldGroup(String name, String world) {
        this.name = name;
        worlds.add(world);
    }

    /**
     * Creates a new WorldGroup with the specified name and the given initial
     * worlds.
     *
     * @param name
     * @param worlds
     */
    public WorldGroup(String name, String[] worlds) {
        this.name = name;
        this.worlds.addAll(Arrays.asList(worlds));
    }

    /**
     * Returns whether or not the world with the given name is a member of this
     * WorldGroup.
     *
     * @param name The name of the WorldGroup to test for membership
     * @return true if the WorldGroup with the given name is a member
     */
    public boolean isMember(String name) {
        for (String world : worlds) {
            if (world.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the world with the given name to this WorldGroup.
     *
     * @param world The name of the world to add to this WorldGroup
     */
    public boolean addWorld(String world) {
        return worlds.add(world);
    }

    /**
     * Adds the worlds with the given names to this WorldGroup.
     *
     * @param worlds The names of the worlds to add to this WorldGroup
     */
    public boolean addWorlds(String[] worlds) {
        return this.worlds.addAll(Arrays.asList(worlds));
    }

    /**
     * Removes the world with the given name from this WorldGroup.
     *
     * @param world The name of the world to remove from this one
     */
    public boolean removeWorld(String world) {
        return worlds.remove(world);
    }

    /**
     * Removes the givens worlds from this WorldGroup.
     * 
     * @param worlds The worlds to be removed from this WorldGroup
     * @return The number of worlds removed
     */
    public int removeWorlds(String[] worlds) {
        int worldsRemoved = 0;
        for (String world : worlds) {
            if (this.worlds.remove(world)) {
                worldsRemoved++;
            }
        }
        return worldsRemoved;
    }

    /**
     * Returns the name of this WorldGroup.
     *
     * @return The name of this WorldGroup
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the LinkedWorlds linked to this WorldGroup.
     *
     * @return The LinkedWorlds linked to this WorldGroup
     */
    public List<String> getWorlds() {
        return worlds;
    }

    /**
     * Tests if this WorldGroup has the same contents as the given Object.
     *
     * @param obj The Object to test for equality
     * @return true if the contents are the same
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof WorldGroup) ? name.equals(((WorldGroup) obj).name) : false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(name);
        return hash;
    }
    
    public boolean isEmpty() {
        return worlds.isEmpty();
    }
}
