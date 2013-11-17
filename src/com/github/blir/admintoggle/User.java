package com.github.blir.admintoggle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Blir
 * @version 2.0.0 Beta
 * @since 30 July 2012
 */
public class User {

    private final String name;
    private final List<Snapshot> snaps = new ArrayList<Snapshot>();
    private final List<Snapshot> snapLog = new ArrayList<Snapshot>();
    private Snapshot currentSnap = null;
    private int snapLogIdx = 0;
    private boolean adminMode = false;
    private static AdminToggle plugin;

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
    public final Snapshot addSnapshot(String name) {
        Snapshot snap = new Snapshot(this.name, name);
        snaps.add(snap);
        return snap;
    }

    /**
     * Adds a the given Snapshot to the User.
     *
     * @param snap The Snapshot to be added
     * @return true if the snapshot was added
     */
    public final boolean addSnapshot(Snapshot snap) {
        return snaps.add(snap);
    }

    /**
     * Returns all Snapshots for the User
     *
     * @return The Snapshots
     */
    public final List<Snapshot> getSnapshots() {
        return snaps;
    }

    /**
     * Returns the Snapshot with the given name, or null if there isn't one.
     *
     * @param name The name of the Snapshot to be returned
     * @param world The world the Snapshot was created in
     * @return The Snapshot
     */
    public final Snapshot getSnapshot(String name, String world) {
        for (Snapshot snap : snaps) {
            if (snap.getName().equals(name)) {
                switch (snap.getVisibility()) {
                    case GLOBAL:
                        return snap;
                    case GROUPED:
                        if (plugin.getWorldGroupByWorld(snap.getWorld()).isMember(world)) {
                            return snap;
                        }
                        break;
                    case PRIVATE:
                        if (snap.getWorld().equals(world)) {
                            return snap;
                        }
                        break;
                }
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
    public final List<Snapshot> getSnapshots(String name) {
        List<Snapshot> matches = new LinkedList<Snapshot>();
        for (Snapshot snap : snaps) {
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
     * @param world The world the Snapshot was created in
     * @return true if a Snapshot was removed
     */
    public final boolean removeSnapshot(String name, String world) {
        Snapshot snap = getSnapshot(name, world);
        return snap != null && snaps.remove(snap);
    }

    /**
     * Returns whether or not the Snapshot with the given name exists.
     *
     * @param name  The name of the Snapshot
     * @param world The WorldGroup to be associated with the Snapshot
     * @return true if it exists
     */
    public final boolean hasSnapshot(String name, String world) {
        return getSnapshot(name, world) != null;
    }

    /**
     * Logs a Snapshot to the Snapshot log, sets the current Snapshot, and
     * resets the Snapshot log index.
     *
     * @param snap The Snapshot to be logged
     */
    public void logSnapshot(Snapshot snap) {
        if (currentSnap != null) {
            snapLog.add(new Snapshot(currentSnap));
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
    public final void clearSnapshots() {
        snaps.clear();
    }

    /**
     * Returns whether or not the User is in admin mode.
     *
     * @return The value of admin, true if admin mode is enabled
     */
    public final boolean isAdmin() {
        return adminMode;
    }

    /**
     * Inverts the value of admin and returns the value.
     *
     * @return The value of admin, true if admin mode is enabled
     */
    public final boolean invertAdminMode() {
        adminMode = !adminMode;
        return adminMode;
    }

    /**
     * Sets the value of admin for the User.
     *
     * @param adminMode The new value of adminMode
     */
    public final void setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
    }

    /**
     * Returns the current Snapshot the User is set to.
     *
     * @return The current Snapshot
     */
    public Snapshot getCurrentSnapshot() {
        return currentSnap;
    }

    protected static void setPlugin(AdminToggle plugin) {
        User.plugin = plugin;
    }
}
