package com.github.blir.admintoggle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Blir
 * @version 2.0.0 Beta
 * @since 30 July 2013
 */
public class WorldGroup {

    private final String name;
    private final List<String> worlds = new ArrayList<String>();

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
     * @deprecated
     */
    @Deprecated
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
    public WorldGroup(String name, String... worlds) {
        this.name = name;
        addWorlds(worlds);
    }

    /**
     * Returns whether or not the world with the given name is a member of this
     * WorldGroup.
     *
     * @param name The name of the WorldGroup to test for membership
     * @return true if the WorldGroup with the given name is a member
     */
    public final boolean isMember(String name) {
        return worlds.contains(name);
    }

    /**
     * Adds the world with the given name to this WorldGroup.
     *
     * @param world The name of the world to add to this WorldGroup
     * @return true if the world was added
     */
    public final boolean addWorld(String world) {
        return worlds.add(world);
    }

    /**
     * Adds the worlds with the given names to this WorldGroup.
     *
     * @param worlds The names of the worlds to add to this WorldGroup
     * @return true if the worlds were added
     */
    public final boolean addWorlds(String[] worlds) {
        return this.worlds.addAll(Arrays.asList(worlds));
    }

    /**
     * Removes the world with the given name from this WorldGroup.
     *
     * @param world The name of the world to remove from this one
     * @return true if the world was removed
     */
    public final boolean removeWorld(String world) {
        return worlds.remove(world);
    }

    /**
     * Removes the givens worlds from this WorldGroup.
     *
     * @param worlds The worlds to be removed from this WorldGroup
     * @return true if the worlds were removed
     */
    public final boolean removeWorlds(String[] worlds) {
        return this.worlds.removeAll(Arrays.asList(worlds));
    }

    /**
     * Returns the name of this WorldGroup.
     *
     * @return The name of this WorldGroup
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the LinkedWorlds linked to this WorldGroup.
     *
     * @return The LinkedWorlds linked to this WorldGroup
     */
    public final List<String> getWorlds() {
        return worlds;
    }

    /**
     * Tests if this WorldGroup has the same contents as the given Object.
     *
     * @param other The Object to test for equality
     * @return true if the contents are the same
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof WorldGroup)
               ? name.equals(((WorldGroup) other).name)
               : false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns whether the WorldGroup is empty.
     *
     * @return true if the WorldGroup is empty
     */
    public final boolean isEmpty() {
        return worlds.isEmpty();
    }
}
