package com.minepop.servegame.admintoggle;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Blir
 * @version 1.2.5
 * @since 10/30/12
 */
public class Snapshot {

    private String user;
    private String name;
    private ItemStack[] inv;
    private ItemStack[] armor;
    private float exp;
    private int level;
    private GameMode gm;
    private float ex;
    private int food;
    private float sat;
    private double balance = 0.0;

    /**
     * Creates a new Snapshot belonging to the given user and with the given
     * name.
     *
     * @param user The user the Snapshot belongs to
     * @param name The name of the Snapshot
     */
    public Snapshot(String user, String name) {
        this.name = name;
        this.user = user;
    }

    /**
     * Creates a new Snapshot and sets the value of every instance variable.
     *
     * @param user The user the Snapshot belongs to
     * @param name The name of the Snapshot
     * @param inv The inventory of the Snapshot
     * @param armor The armor of the Snapshot
     * @param exp The experience of the Snapshot
     * @param level The level of the Snapshot
     * @param gm The GameMode of the Snapshot
     * @param ex The exhaustion of the Snapshot
     * @param food The food level of the Snapshot
     * @param sat The saturation of the Snapshot
     */
    public Snapshot(String user, String name, ItemStack[] inv, ItemStack[] armor, float exp, int level, GameMode gm, float ex, int food, float sat, double balance) {
        this.user = user;
        this.name = name;
        this.inv = inv;
        this.armor = armor;
        this.exp = exp;
        this.level = level;
        this.gm = gm;
        this.ex = ex;
        this.food = food;
        this.sat = sat;
        this.balance = balance;
    }

    /**
     * Returns the name of the user the Snapshot belongs to.
     *
     * @return The name of the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the name of the Snapshot.
     *
     * @return The name of the Snapshot
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the inventory of the Snapshot.
     *
     * @return The inventory of the Snapshot
     */
    public ItemStack[] getInv() {
        return inv;
    }

    /**
     * Returns the armor of the Snapshot.
     *
     * @return The armor of the Snapshot
     */
    public ItemStack[] getArmor() {
        return armor;
    }

    /**
     * Returns the GameMode of the Snapshot.
     *
     * @return The GameMode of the Snapshot
     */
    public GameMode getGameMode() {
        return gm;
    }

    /**
     * Returns the experience of the Snapshot.
     *
     * @return The experience of the Snapshot
     */
    public float getExp() {
        return exp;
    }

    /**
     * Returns the level of the Snapshot.
     *
     * @return The level of the Snapshot
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the exhaustion of the Snapshot.
     *
     * @return The exhaustion of the Snapshot
     */
    public float getExhaustion() {
        return ex;
    }

    /**
     * Returns the food level of the Snapshot.
     *
     * @return The food level of the Snapshot
     */
    public int getFoodLevel() {
        return food;
    }

    /**
     * Returns the saturation of the snapshot.
     *
     * @return The saturation of the Snapshot
     */
    public float getSaturation() {
        return sat;
    }
    
    /**
     * Returns the balance of the Snapshot.
     * 
     * @return The balance of the Snapshot
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets the inventory and armor of the Snapshot.
     *
     * @param inv The inventory to be set to
     * @param armor The armor to be set to
     */
    public void setInv(ItemStack[] inv, ItemStack[] armor) {
        this.inv = new ItemStack[inv.length];
        for (int idx = 0; idx < inv.length; idx++) {
            if (inv[idx] != null) {
                this.inv[idx] = inv[idx].clone();
            }
        }
        this.armor = new ItemStack[armor.length];
        for (int idx = 0; idx < armor.length; idx++) {
            if (armor[idx] != null) {
                this.armor[idx] = armor[idx].clone();
            }
        }
    }

    /**
     * Sets the GameMode of the Snapshot.
     *
     * @param gm The GameMode the Snapshot will be set to
     */
    public void setGameMode(GameMode gm) {
        this.gm = gm;
    }

    /**
     * Sets the experience of the Snapshot.
     *
     * @param exp The experience the Snapshot will be set to
     */
    public void setExp(float exp) {
        this.exp = exp;
    }

    /**
     * Sets the level of the Snapshot.
     *
     * @param level The level the Snapshot will be set to
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Sets the exhaustion of the Snapshot.
     *
     * @param ex The exhaustion the Snapshot will be set to
     */
    public void setExhaustion(float ex) {
        this.ex = ex;
    }

    /**
     * Sets the food level of the Snapshot.
     *
     * @param food The food level the Snapshot will be set to
     */
    public void setFoodLevel(int food) {
        this.food = food;
    }

    /**
     * Sets the saturation of the Snapshot.
     *
     * @param sat The saturation the Snapshot will be set to
     */
    public void setSaturation(float sat) {
        this.sat = sat;
    }
    
    /**
     * Sets the balance of the Snapshot.
     * 
     * @param balance The balance the Snapshot will be set to
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Clones the Snapshot.
     *
     * @return The clone of the Snapshot
     */
    @Override
    public Snapshot clone() {
        return new Snapshot(user, name, inv, armor, exp, level, gm, ex, food, sat, balance);
    }
}