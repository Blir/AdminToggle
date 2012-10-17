package com.minepop.servegame.admintoggle;

import java.io.*;
import java.util.*;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Blir
 */
public class Admin extends JavaPlugin {

    private static ArrayList<User> users = new ArrayList<>(1);
    private static String folder;

    @Override
    public void onEnable() {
        File file = getDataFolder();
        if (!file.isDirectory()) {
            file.mkdir();
        }
        folder = file.getPath();
        loadSnapshots();
    }

    @Override
    public void onDisable() {
        saveSnapshots();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this plugin!");
            return true;
        }

        Player player = (Player) sender;
        User user = getUser(player.getName());
        String action = cmd.getName().toLowerCase();
        switch (action) {
            case "adminswitch":
            case "asdf":
            case "adswitch":
                if (user.isAdmin()) {
                    saveSnapshot(user, "temp", player, true);
                    loadSnapshot(user, "legit", player);
                } else {
                    saveSnapshot(user, "legit", player, true);
                    loadSnapshot(user, "admin", player);
                }
                player.sendMessage("Admin mode " + (user.invertAdmin() ? "enabled." : "disabled."));
                return true;
            case "adminsaveram":
            case "adram":
                if (args.length != 2 || !(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
                    return false;
                }
                if (saveSnapshot(user, args[0], player, Boolean.parseBoolean(args[1].toLowerCase()))) {
                    player.sendMessage("Save successful!");
                }
                return true;
            case "adminload":
            case "adload":
                if (loadSnapshot(user, args[0], player)) {
                    player.sendMessage("Load successful!");
                }
                return true;
            case "adminlistsnapshots":
            case "adlist":
                player.sendMessage("Current snapshots:");
                for (Snapshot snap : user.getSnapshots()) {
                    player.sendMessage(snap.getName());
                }
                return true;
            case "admindelete":
            case "addel":
                if (user.removeSnapshot(args[0])) {
                    player.sendMessage("Snapshot " + args[0] + " removed.");
                } else {
                    player.sendMessage("The snapshot " + args[0] + " doesn't exist!");
                }
                return true;
            case "adminsavefile":
            case "adfile":
                saveSnapshots();
                return true;
            case "admincheck":
            case "adcheck":
                player.sendMessage("Admin mode is " + (user.isAdmin() ? "enabled." : "disabled."));
                return true;
        }
        return false;
    }

    public User getUser(String name) {
        for (User user : users) {
            if (name.equals(user.getName())) {
                return user;
            }
        }
        User user = new User(name);
        users.add(user);
        return user;
    }

    public boolean saveSnapshot(User user, String name, Player player, boolean overwrite) {
        if (user.snapshotExists(name) && !overwrite) {
            player.sendMessage("The snapshot " + name + " already exists!");
            return false;
        }
        Snapshot snap;
        if (user.snapshotExists(name) && overwrite) {
            snap = user.getSnapshot(name);
        } else {
            snap = user.addSnapshot(name);
        }
        snap.setInv(player.getInventory().getContents(), player.getInventory().getArmorContents());
        snap.setGameMode(player.getGameMode());
        snap.setExp(player.getExp());
        snap.setLevel(player.getLevel());
        snap.setExhaustion(player.getExhaustion());
        snap.setFoodLevel(player.getFoodLevel());
        snap.setSaturation(player.getSaturation());
        return true;
    }

    public boolean loadSnapshot(User user, String name, Player player) {
        Snapshot snap = user.getSnapshot(name);
        if (snap == null) {
            player.sendMessage("The snapshot " + name + " doesn't exist!");
            return false;
        }
        player.getInventory().setContents(snap.getInv());
        player.getInventory().setArmorContents(snap.getArmor());
        player.setGameMode(snap.getGameMode());
        player.setExp(snap.getExp());
        player.setLevel(snap.getLevel());
        player.setExhaustion(snap.getExhaustion());
        player.setFoodLevel(snap.getFoodLevel());
        player.setSaturation(snap.getSaturation());
        return true;
    }

    public void saveSnapshots() {
        File file = new File(folder + "/snapshots.properties");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            Properties p = new Properties();
            p.setProperty("User Count", String.valueOf(users.size()));
            for (int idx = 0; idx < users.size(); idx++) {
                User user = users.get(idx);
                p.setProperty("User" + "\\" + idx, user.getName());
                p.setProperty("Admin" + "\\" + idx, String.valueOf(user.isAdmin()));
                p.setProperty("Snapshot Count" + "\\" + user.getName(), String.valueOf(user.getSnapshots().size()));
                for (int idx2 = 0; idx2 < user.getSnapshots().size(); idx2++) {
                    Snapshot snap = user.getSnapshots().get(idx2);
                    p.setProperty("Inventory Size" + "\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getInv().length));
                    p.setProperty("Snapshot" + "\\" + user.getName() + "\\" + idx2, snap.getName());
                    for (int idx3 = 0; idx3 < snap.getInv().length; idx3++) {
                        ItemStack inv = snap.getInv()[idx3];
                        if (inv != null) {
                            p.setProperty("IType" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, String.valueOf(inv.getTypeId()));
                            p.setProperty("IAmount" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, String.valueOf(inv.getAmount()));
                            p.setProperty("IDamage" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, String.valueOf(inv.getDurability()));
                            p.setProperty("IEnchant" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, String.valueOf(inv.getEnchantments().size()));
                            int idx4 = 0;
                            for (Iterator<Enchantment> it = inv.getEnchantments().keySet().iterator(); it.hasNext();) {
                                Enchantment ench = it.next();
                                p.setProperty("IEnchantment" + "\\" + user.getName() + "\\" + idx3 + "\\" + snap.getName() + "\\" + idx4, String.valueOf(ench.getName()));
                                p.setProperty("IEnchantLevel" + "\\" + user.getName() + "\\" + idx3 + "\\" + snap.getName() + "\\" + idx4, String.valueOf(inv.getEnchantmentLevel(ench)));
                                idx4++;
                            }
                        } else {
                            p.setProperty("IType" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, "null");
                        }
                    }
                    p.setProperty("Armor Size" + "\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getArmor().length));
                    for (int idx3 = 0; idx3 < snap.getArmor().length; idx3++) {
                        ItemStack armor = snap.getArmor()[idx3];
                        if (armor != null) {
                            p.setProperty("AType" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, String.valueOf(armor.getTypeId()));
                            p.setProperty("AAmount" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, String.valueOf(armor.getAmount()));
                            p.setProperty("ADamage" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, String.valueOf(armor.getDurability()));
                            p.setProperty("AEnchant" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, String.valueOf(armor.getEnchantments().size()));
                            int idx4 = 0;
                            for (Iterator<Enchantment> it = armor.getEnchantments().keySet().iterator(); it.hasNext();) {
                                Enchantment ench = it.next();
                                p.setProperty("AEnchantment" + "\\" + user.getName() + "\\" + idx3 + "\\" + snap.getName() + "\\" + idx4, String.valueOf(ench.getName()));
                                p.setProperty("AEnchantLevel" + "\\" + user.getName() + "\\" + idx3 + "\\" + snap.getName() + "\\" + idx4, String.valueOf(armor.getEnchantmentLevel(ench)));
                                idx4++;
                            }
                        } else {
                            p.setProperty("AType" + "\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, "null");
                        }
                    }
                    switch (snap.getGameMode()) {
                        case CREATIVE:
                            p.setProperty("GameMode" + "\\" + user.getName() + "\\" + idx2, "creative");
                            break;
                        case SURVIVAL:
                            p.setProperty("GameMode" + "\\" + user.getName() + "\\" + idx2, "survival");
                            break;
                        case ADVENTURE:
                            p.setProperty("GameMode" + "\\" + user.getName() + "\\" + idx2, "adventure");
                            break;
                    }
                    p.setProperty("Exp" + "\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getExp()));
                    p.setProperty("Level" + "\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getLevel()));
                    p.setProperty("Exhaustion" + "\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getExhaustion()));
                    p.setProperty("Food Level" + "\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getFoodLevel()));
                    p.setProperty("Saturation" + "\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getSaturation()));
                }
            }
            p.store(fos, null);
        } catch (Exception e) {
            getLogger().severe("Error saving snapshots!");
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    public void loadSnapshots() {
        File file = new File(folder + "/snapshots.properties");
        if (!file.exists()) {
            saveSnapshots();
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Properties p = new Properties();
            p.load(fis);
            int userCount = Integer.parseInt(p.getProperty("User Count"));
            for (int idx = 0; idx < userCount; idx++) {
                User user = new User(p.getProperty("User" + "\\" + idx));
                users.add(user);
                user.setAdmin(Boolean.parseBoolean(p.getProperty("Admin" + "\\" +idx)));
                int snapCount = Integer.parseInt(p.getProperty("Snapshot Count" + "\\" + user.getName()));
                for (int idx2 = 0; idx2 < snapCount; idx2++) {
                    int invSize = Integer.parseInt(p.getProperty("Inventory Size" + "\\" + user.getName() + "\\" + idx2));
                    ItemStack[] inv = new ItemStack[invSize];
                    String name = p.getProperty("Snapshot" + "\\" + user.getName() + "\\" + idx2);
                    for (int idx3 = 0; idx3 < invSize; idx3++) {
                        if (!p.getProperty("IType" + "\\" + user.getName() + "\\" + name + "\\" + idx3).equals("null")) {
                            inv[idx3] = new ItemStack(Integer.parseInt(p.getProperty("IType" + "\\" + user.getName() + "\\" + name + "\\" + idx3)), Integer.parseInt(p.getProperty("IAmount" + "\\" + user.getName() + "\\" + name + "\\" + idx3)), Short.parseShort(p.getProperty("IDamage" + "\\" + user.getName() + "\\" + name + "\\" + idx3)));
                            for (int idx4 = 0; idx4 < Integer.parseInt(p.getProperty("IEnchant" + "\\" + user.getName() + "\\" + name + "\\" + idx3)); idx4++) {
                                inv[idx3].addEnchantment(Enchantment.getByName(p.getProperty("IEnchantment" + "\\" + user.getName() + "\\" + idx3 + "\\" + name + "\\" + idx4)), Integer.parseInt(p.getProperty("IEnchantLevel" + "\\" + user.getName() + "\\" + idx3 + "\\" + name + "\\" + idx4)));
                            }
                        }
                    }
                    int armorSize = Integer.parseInt(p.getProperty("Armor Size" + "\\" + user.getName() + "\\" + idx2));
                    ItemStack[] armor = new ItemStack[armorSize];
                    for (int idx3 = 0; idx3 < armorSize; idx3++) {
                        if (!p.getProperty("AType" + "\\" + user.getName() + "\\" + name + "\\" + idx3).equals("null")) {
                            armor[idx3] = new ItemStack(Integer.parseInt(p.getProperty("AType" + "\\" + user.getName() + "\\" + name + "\\" + idx3)), Integer.parseInt(p.getProperty("AAmount" + "\\" + user.getName() + "\\" + name + "\\" + idx3)), Short.parseShort(p.getProperty("ADamage" + "\\" + user.getName() + "\\" + name + "\\" + idx3)));
                            for (int idx4 = 0; idx4 < Integer.parseInt(p.getProperty("AEnchant" + "\\" + user.getName() + "\\" + name + "\\" + idx3)); idx4++) {
                                armor[idx3].addEnchantment(Enchantment.getByName(p.getProperty("AEnchantment" + "\\" + user.getName() + "\\" + idx3 + "\\" + name + "\\" + idx4)), Integer.parseInt(p.getProperty("AEnchantLevel" + "\\" + user.getName() + "\\" + idx3 + "\\" + name + "\\" + idx4)));
                            }
                        }
                    }
                    GameMode gm = null;
                    switch (p.getProperty("GameMode" + "\\" + user.getName() + "\\" + idx2)) {
                        case "creative":
                            gm = GameMode.CREATIVE;
                            break;
                        case "survival":
                            gm = GameMode.SURVIVAL;
                            break;
                        case "adventure":
                            gm = GameMode.ADVENTURE;
                            break;
                    }
                    Snapshot snap = new Snapshot(user.getName(), name, inv, armor, Float.parseFloat(p.getProperty("Exp" + "\\" + user.getName() + "\\" + idx2)), Integer.parseInt(p.getProperty("Level" + "\\" + user.getName() + "\\" + idx2)), gm, Float.parseFloat(p.getProperty("Exhaustion" + "\\" + user.getName() + "\\" + idx2)), Integer.parseInt(p.getProperty("Food Level" + "\\" + user.getName() + "\\" + idx2)), Float.parseFloat(p.getProperty("Saturation" + "\\" + user.getName() + "\\" + idx2)));
                    user.loadSnapshot(snap);
                }
            }
        } catch (IOException | NumberFormatException e) {
            getLogger().severe("Error loading snapshots!");
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }
}
