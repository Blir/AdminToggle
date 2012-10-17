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
    public User(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public Snapshot addSnapshot(String name) {
        Snapshot snap = new Snapshot(this.name, name);
        snapshots.add(snap);
        return snap;
    }
    public void addSnapshot(Snapshot snap) {
        snapshots.add(snap);
    }
    public ArrayList<Snapshot> getSnapshots() {
        return snapshots;
    }
    public Snapshot getSnapshot(String name) {
        for (Snapshot snap: snapshots) {
            if (snap.getName().equals(name)) {
                return snap;
            }
        }
        return null;
    }
    public boolean removeSnapshot(String name) {
        for (Snapshot snap: snapshots) {
            if (snap.getName().equals(name)) {
                snapshots.remove(snap);
                return true;
            }
        }
        return false;
    }
    public boolean snapshotExists(String name) {
        for (Snapshot snap: snapshots) {
            if (snap.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    public boolean isAdmin() {
        return admin;
    }
    public boolean invertAdmin() {
        admin = !admin;
        return admin;
    }
    public boolean setAdmin(boolean admin) {
        this.admin = admin;
        return this.admin;
    }
}
