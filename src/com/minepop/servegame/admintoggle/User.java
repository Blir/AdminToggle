package com.minepop.servegame.admintoggle;

import java.util.ArrayList;

/**
 *
 * @author Blir
 */
public class User {

    private String name;
    private ArrayList<Snapshot> snapshots = new ArrayList<>(0);
    private boolean admin = false;

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
        snapshots.add(snap);
        return snap;
    }

    /**
     * Adds a the given Snapshot to the User.
     *
     * @param snap The Snapshot to be added
     */
    public void addSnapshot(Snapshot snap) {
        snapshots.add(snap);
    }

    /**
     * Returns all Snapshots for the User
     *
     * @return The Snapshots
     */
    public ArrayList<Snapshot> getSnapshots() {
        return snapshots;
    }

    /**
     * Returns the Snapshot with the given name, or null if there isn't one.
     *
     * @param name The name of the Snapshot to be returned
     * @return The Snapshot
     */
    public Snapshot getSnapshot(String name) {
        for (Snapshot snap : snapshots) {
            if (snap.getName().equals(name)) {
                return snap;
            }
        }
        return null;
    }

    /**
     * Removes the Snapshot with the given name from the User.
     *
     * @param name The name of the Snapshot to be removed
     * @return true if a Snapshot was removed
     */
    public boolean removeSnapshot(String name) {
        for (Snapshot snap : snapshots) {
            if (snap.getName().equals(name)) {
                snapshots.remove(snap);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the Snapshot with the given name exists.
     *
     * @param name The name of the Snapshot
     * @return true if it exists
     */
    public boolean snapshotExists(String name) {
        for (Snapshot snap : snapshots) {
            if (snap.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the User is in admin mode.
     *
     * @return The value of admin, true if admin mode is enabled
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Inverts the value of admin and returns the value.
     *
     * @return The value of admin, true if admin mode is enabled
     */
    public boolean invertAdmin() {
        admin = !admin;
        return admin;
    }

    /**
     * Sets the value of admin for the User.
     *
     * @param admin The value admin will be set to
     * @return The value of admin, true if admin mode is enabled
     */
    public boolean setAdmin(boolean admin) {
        this.admin = admin;
        return this.admin;
    }
}
