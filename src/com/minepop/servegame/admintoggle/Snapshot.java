package com.minepop.servegame.admintoggle;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Blir
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

    public Snapshot(String user, String name) {
        this.name = name;
        this.user = user;
    }
    
    public Snapshot(String user, String name, ItemStack[] inv, ItemStack[] armor, float exp, int level, GameMode gm, float ex, int food, float sat) {
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
    }

    public String getUser() {
        return user;
    }
    
    public String getName() {
        return name;
    }

    public ItemStack[] getInv() {
        return inv;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public GameMode getGameMode() {
        return gm;
    }

    public float getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public float getExhaustion() {
        return ex;
    }

    public int getFoodLevel() {
        return food;
    }

    public float getSaturation() {
        return sat;
    }

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

    public void setGameMode(GameMode gm) {
        this.gm = gm;
    }

    public void setExp(float exp) {
        this.exp = exp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setExhaustion(float ex) {
        this.ex = ex;
    }

    public void setFoodLevel(int food) {
        this.food = food;
    }

    public void setSaturation(float sat) {
        this.sat = sat;
    }
}
