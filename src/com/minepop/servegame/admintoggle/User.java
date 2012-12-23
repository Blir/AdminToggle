package com.minepop.servegame.admintoggle;

import java.util.ArrayList;

/**
 *
 * @author Blir
 * @version 1.1.0 Beta
 * @since 22 Dec. 2012
 */
public class User {

    private String name;
    private ArrayList<Snapshot> snaps = new ArrayList<>(0), snapLog = new ArrayList<>(0);
    private Snapshot currentSnap = null;
    private int snapLogIdx = 0;
    private boolean adminMode = false;

    /**
     * Creates a new User with the given name.
     *
     * @param name The name of the User
     */
    public User(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the User.
     *
     * @return the name of the User
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a new Snapshot with the given name to the User.
     *
     * @param name The name of the Snapshot
     * @return The Snapshot added
     */
    public Snapshot addSnapshot(String name) {
        Snapshot snap = new Snapshot(this.name, name);
        snaps.add(snap);
        return snap;
    }

    /**
     * Adds a the given Snapshot to the User.
     *
     * @param snap The Snapshot to be added
     */
    public void addSnapshot(Snapshot snap) {
        snaps.add(snap);
    }

    /**
     * Returns all Snapshots for the User
     *
     * @return The Snapshots
     */
    public ArrayList<Snapshot> getSnapshots() {
        return snaps;
    }

    /**
     * Returns the Snapshot with the given name, or null if there isn't one.
     *
     * @param name The name of the Snapshot to be returned
     * @return The Snapshot
     */
    public Snapshot getSnapshot(String name, WorldGroup world) {
        for (Snapshot snap : snaps) {
            if (snap.getName().equals(name) && world.isMember(snap.getWorld())) {
                return snap;
            }
        }
        return null;
    }
    
    /**
     * Returns all Snapshots with the given name.
     * 
     * @param name The name of the Snapshot to search for
     * @return The Snapshots with matching names
     */
    public ArrayList<Snapshot> getSnapshots(String name) {
        ArrayList<Snapshot> matches = new ArrayList<>(0);
        for (Snapshot snap: snaps) {
            if (snap.getName().equals(name)) {
                matches.add(snap);
            }
        }
        return matches;
    }

    /**
     * Removes the Snapshot with the given name from the User.
     *
     * @param name The name of the Snapshot to be removed
     * @return true if a Snapshot was removed
     */
    public boolean removeSnapshot(String name, WorldGroup world) {
        for (Snapshot snap : snaps) {
            if (snap.getName().equals(name) && world.isMember(snap.getWorld())) {
                snaps.remove(snap);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the Snapshot with the given name exists.
     *
     * @param name The name of the Snapshot
     * @param world The WorldGroup to be associated with the Snapshot
     * @return true if it exists
     */
    public boolean hasSnapshot(String name, WorldGroup world) {
        for (Snapshot snap : snaps) {
            if (snap.getName().equals(name) && world.isMember(snap.getWorld())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs a Snapshot to the Snapshot log, sets the current Snapshot, and
     * resets the Snapshot log index.
     *
     * @param snap The Snapshot to be logged
     */
    public void logSnapshot(Snapshot snap) {
        if (currentSnap != null) {
            snapLog.add(currentSnap.clone());
        }
        snapLogIdx = snapLog.size() - 1;
        currentSnap = snap;
    }

    /**
     * Returns the last Snapshot for the User.
     *
     * @return The last Snapshot for the User
     */
    public Snapshot revertSnapshot() {
        if (snapLogIdx < 0 || snapLogIdx >= snapLog.size()) {
            return null;
        }
        snapLogIdx--;
        return snapLog.get(snapLogIdx + 1);
    }
    
    /**
     * Clears the Snapshots for the User.
     */
    public void clearSnapshots() {
        snaps.clear();
    }

    /**
     * Returns whether or not the User is in admin mode.
     *
     * @return The value of admin, true if admin mode is enabled
     */
    public boolean isAdminModeEnabled() {
        return adminMode;
    }

    /**
     * Inverts the value of admin and returns the value.
     *
     * @return The value of admin, true if admin mode is enabled
     */
    public boolean invertAdminMode() {
        adminMode = !adminMode;
        return adminMode;
    }

    /**
     * Sets the value of admin for the User.
     *
     * @param admin The value admin will be set to
     * @return The value of admin, true if admin mode is enabled
     */
    public boolean setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
        return this.adminMode;
    }
    
    /**
     * Returns the current Snapshot the User is set to.
     * 
     * @return The current Snapshot
     */
    public Snapshot getCurrentSnapshot() {
        return currentSnap;
    }
}