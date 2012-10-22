package com.minepop.servegame.admintoggle;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Blir
 */
public class Admin extends JavaPlugin implements Listener {

    private ArrayList<User> users = new ArrayList<>(1);
    private String folder;

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        File file = getDataFolder();
        if (!file.isDirectory()) {
            file.mkdir();
        }
        folder = file.getPath();
        loadSnapshots();
        File destFile = new File(folder + "/README.txt");
        InputStream sourceFile = getResource("README.txt");
        Scanner sourceFileReader = null;
        PrintWriter destFileWriter = null;
        try {
            sourceFileReader = new Scanner(sourceFile);
            destFileWriter = new PrintWriter(destFile);
            while (sourceFileReader.hasNext()) {
                destFileWriter.println(sourceFileReader.nextLine());
            }
        } catch (Exception e) {
            getLogger().warning("Error copying readme.txt");
        } finally {
            try {
                sourceFileReader.close();
                destFileWriter.close();
                sourceFile.close();
            } catch (Exception e) {
                getLogger().warning("Error closing streams after copying readme.txt");
            }
        }
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        saveSnapshots();
    }

    /**
     * Called when a player executes a command.
     *
     * @param sender The sender of the command
     * @param cmd The command sent
     * @param label
     * @param args The command arguments
     * @return success
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        User user = null;
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            user = getUser(player.getName());
        }


        String action = cmd.getName().toLowerCase();
        switch (action) {
            case "adminswitch":
            case "asdf":
            case "adswitch":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    return false;
                }
                if (user.isAdminModeEnabled()) {
                    saveSnapshot(user, "temp", player, true);
                    if (loadSnapshot(user, "legit", player)) {
                        player.sendMessage("Snapshot \"legit\" loaded!");
                    } else {
                        player.sendMessage("The snapshot \"legit\" doesn't exist!");
                    }
                } else {
                    saveSnapshot(user, "legit", player, true);
                    if (loadSnapshot(user, "admin", player)) {
                        player.sendMessage("Snapshot \"admin\" loaded!");
                    } else {
                        player.sendMessage("The snapshot \"admin\" doesn't exist!");
                    }
                }
                player.sendMessage("Admin mode " + (user.invertAdminMode() ? "enabled." : "disabled."));
                return true;
            case "adminsaveram":
            case "adram":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                } else if (args.length != 1) {
                    return false;
                }
                if (saveSnapshot(user, args[0], player, false)) {
                    player.sendMessage("Snapshot \"" + args[0] + "\" saved!");
                } else {
                    player.sendMessage("The snapshot \"" + args[0] + "\" already exists!");
                }
                return true;
            case "adminsaveramoverwrite":
            case "adramo":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 2) {
                    return false;
                }
                if (saveSnapshot(user, args[0], player, true)) {
                    player.sendMessage("Snapshot \"" + args[0] + "\" overwritten!");
                } else {
                    player.sendMessage("The snapshot \"" + args[0] + "\" doesn't exist to be overwritten! Snapshot created.");
                    saveSnapshot(user, args[0], player, false);
                }
                return true;
            case "adminload":
            case "adload":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 1) {
                    return false;
                }
                if (loadSnapshot(user, args[0], player)) {
                    player.sendMessage("Snapshot \"" + args[0] + "\" loaded!");
                } else {
                    player.sendMessage("The snapshot \"" + args[0] + "\" doesn't exist!");
                }
                return true;
            case "adminlistsnapshots":
            case "adlist":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    return false;
                }
                if (!user.getSnapshots().isEmpty()) {
                    player.sendMessage("Current snapshots:");
                    for (Snapshot snap : user.getSnapshots()) {
                        player.sendMessage(snap.getName());
                    }
                } else {
                    player.sendMessage("You have no snapshots.");
                }
                return true;
            case "admindelete":
            case "addel":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 1) {
                    return false;
                }
                if (user.removeSnapshot(args[0])) {
                    player.sendMessage("Snapshot \"" + args[0] + "\" deleted!");
                } else {
                    player.sendMessage("The snapshot \"" + args[0] + "\" doesn't exist!");
                }
                return true;
            case "adminsavefile":
            case "adfile":
                if (args.length != 0) {
                    return false;
                }
                saveSnapshots();
                sender.sendMessage("Snapshots saved.");
                return true;
            case "admincheck":
            case "adcheck":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    return false;
                }
                player.sendMessage("Admin mode is " + (user.isAdminModeEnabled() ? "enabled." : "disabled."));
                return true;
            case "adminundo":
            case "adundo":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    return false;
                }
                if (revertSnapshot(user, player)) {
                    player.sendMessage("Snapshot reverted.");
                } else {
                    player.sendMessage("Your last snapshot could not be retrieved.");
                }
                return true;
            case "admindeleteall":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 1 || !args[0].equals("CONFIRM")) {
                    return false;
                }
                user.clearSnapshots();
                player.sendMessage("Snapshots deleted.");
                return true;
        }
        return false;
    }

    /**
     * Returns the User associated with the given name, and creates one if one
     * isn't found.
     *
     * @param name The name of the User
     * @return The User
     */
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

    /**
     * Returns wether or not the Player with the given name is registered in the plugin.
     * 
     * @param name The name of the Player to check if they're registered
     * @return true if the Player is registered
     */
    public boolean isUserRegistered(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves a Snapshot to RAM.
     *
     * @param user The User to save the Snapshot to
     * @param name The name the Snapshot will be saved as
     * @param player The Player saving the Snapshot
     * @param overwrite If true then overwrite the existing Snapshot with the
     * given name, if there is one
     * @return true if the Snapshot was saved.
     */
    public boolean saveSnapshot(User user, String name, Player player, boolean overwrite) {
        Snapshot snap;
        if (overwrite) {
            if (user.snapshotExists(name)) {
                snap = user.getSnapshot(name);
            } else {
                return false;
            }
        } else {
            if (user.snapshotExists(name)) {
                return false;
            } else {
                snap = user.addSnapshot(name);
            }
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

    /**
     * Loads a Snapshot from RAM.
     *
     * @param user The User to load the Snapshot to
     * @param name The name of the Snapshot to load
     * @param player The Player to load the Snapshot to
     * @return true if the Snapshot was loaded
     */
    public boolean loadSnapshot(User user, String name, Player player) {
        Snapshot snap = user.getSnapshot(name);
        if (snap == null) {
            return false;
        }
        user.logSnapshot(snap);
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

    public boolean revertSnapshot(User user, Player player) {
        Snapshot snap = user.revertSnapshot();
        if (snap == null) {
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

    /**
     * Saves all Snapshots to file.
     */
    public void saveSnapshots() {
        File file = new File(folder + "/snapshots.properties");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            Properties p = new Properties();
            p.setProperty("User Count", String.valueOf(users.size()));
            for (int idx = 0; idx < users.size(); idx++) {
                User user = users.get(idx);
                p.setProperty("User\\" + idx, user.getName());
                p.setProperty("Admin\\" + idx, String.valueOf(user.isAdminModeEnabled()));
                p.setProperty("Snapshot Count\\" + user.getName(), String.valueOf(user.getSnapshots().size()));
                for (int idx2 = 0; idx2 < user.getSnapshots().size(); idx2++) {
                    Snapshot snap = user.getSnapshots().get(idx2);
                    p.setProperty("Snapshot\\" + user.getName() + "\\" + idx2, snap.getName());
                    for (int idx3 = 0; idx3 < snap.getInv().length; idx3++) {
                        ItemStack inv = snap.getInv()[idx3];
                        if (inv != null) {
                            p.setProperty("IType\\" + user.getName() + "\\" + snap.getName() + "\\"
                                    + idx3, String.valueOf(inv.getTypeId()));
                            p.setProperty("IAmount\\" + user.getName() + "\\" + snap.getName() + "\\"
                                    + idx3, String.valueOf(inv.getAmount()));
                            p.setProperty("IDamage\\" + user.getName() + "\\" + snap.getName() + "\\"
                                    + idx3, String.valueOf(inv.getDurability()));
                            p.setProperty("IEnchant\\" + user.getName() + "\\" + snap.getName() + "\\"
                                    + idx3, String.valueOf(inv.getEnchantments().size()));
                            int idx4 = 0;
                            for (Iterator<Enchantment> it = inv.getEnchantments().keySet().iterator(); it.hasNext();) {
                                Enchantment ench = it.next();
                                p.setProperty("IEnchantment\\" + user.getName() + "\\" + idx3 + "\\" + snap.getName()
                                        + "\\" + idx4, String.valueOf(ench.getName()));
                                p.setProperty("IEnchantLevel\\" + user.getName() + "\\" + idx3 + "\\" + snap.getName()
                                        + "\\" + idx4, String.valueOf(inv.getEnchantmentLevel(ench)));
                                idx4++;
                            }
                        } else {
                            p.setProperty("IType\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, "null");
                        }
                    }
                    for (int idx3 = 0; idx3 < snap.getArmor().length; idx3++) {
                        ItemStack armor = snap.getArmor()[idx3];
                        if (armor != null) {
                            p.setProperty("AType\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3,
                                    String.valueOf(armor.getTypeId()));
                            p.setProperty("AAmount\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3,
                                    String.valueOf(armor.getAmount()));
                            p.setProperty("ADamage\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3,
                                    String.valueOf(armor.getDurability()));
                            p.setProperty("AEnchant\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3,
                                    String.valueOf(armor.getEnchantments().size()));
                            int idx4 = 0;
                            for (Iterator<Enchantment> it = armor.getEnchantments().keySet().iterator(); it.hasNext();) {
                                Enchantment ench = it.next();
                                p.setProperty("AEnchantment\\" + user.getName() + "\\" + idx3 + "\\" + snap.getName()
                                        + "\\" + idx4, String.valueOf(ench.getName()));
                                p.setProperty("AEnchantLevel\\" + user.getName() + "\\" + idx3 + "\\" + snap.getName()
                                        + "\\" + idx4, String.valueOf(armor.getEnchantmentLevel(ench)));
                                idx4++;
                            }
                        } else {
                            p.setProperty("AType\\" + user.getName() + "\\" + snap.getName() + "\\" + idx3, "null");
                        }
                    }
                    switch (snap.getGameMode()) {
                        case CREATIVE:
                            p.setProperty("GameMode\\" + user.getName() + "\\" + idx2, "creative");
                            break;
                        case SURVIVAL:
                            p.setProperty("GameMode\\" + user.getName() + "\\" + idx2, "survival");
                            break;
                        case ADVENTURE:
                            p.setProperty("GameMode\\" + user.getName() + "\\" + idx2, "adventure");
                            break;
                    }
                    p.setProperty("Exp\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getExp()));
                    p.setProperty("Level\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getLevel()));
                    p.setProperty("Exhaustion\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getExhaustion()));
                    p.setProperty("Food Level\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getFoodLevel()));
                    p.setProperty("Saturation\\" + user.getName() + "\\" + idx2, String.valueOf(snap.getSaturation()));
                }
            }
            p.store(fos, null);
        } catch (Exception e) {
            getLogger().severe("Unknown error saving snapshots!");
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                getLogger().severe("Error closing the output stream! Data might not be saved.");
            }
        }
    }

    /**
     * Loads all Snapshots from file to RAM.
     */
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
            int userCount;
            try {
                userCount = Integer.parseInt(p.getProperty("User Count"));
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error loading user count");
                userCount = 0;
            }
            for (int idx = 0; idx < userCount; idx++) {
                User user;
                try {
                    user = new User(p.getProperty("User\\" + idx));
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, "Error loading user {0}",
                            idx);
                    continue;
                }
                users.add(user);
                try {
                    user.setAdminMode(Boolean.parseBoolean(p.getProperty("Admin\\" + idx)));
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, "Error loading {0}''s admin mode setting, set to false",
                            user.getName());
                }
                int snapCount;
                try {
                    snapCount = Integer.parseInt(p.getProperty("Snapshot Count\\" + user.getName()));
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot Count",
                            user.getName());
                    continue;
                }
                for (int idx2 = 0; idx2 < snapCount; idx2++) {
                    ItemStack[] inv = new ItemStack[36];
                    String name;
                    try {
                        name = p.getProperty("Snapshot\\" + user.getName() + "\\" + idx2);
                    } catch (Exception e) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot at {1}",
                                new Object[]{user.getName(), idx2});
                        continue;
                    }
                    for (int idx3 = 0; idx3 < inv.length; idx3++) {
                        String iType;
                        try {
                            iType = p.getProperty("IType\\" + user.getName() + "\\" + name + "\\" + idx3);
                        } catch (Exception e) {
                            getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: Inv at {2}",
                                    new Object[]{user.getName(), name, idx3});
                            continue;
                        }
                        if (!iType.equals("null")) {
                            try {
                                inv[idx3] = new ItemStack(
                                        Integer.parseInt(iType),
                                        Integer.parseInt(p.getProperty("IAmount\\" + user.getName() + "\\" + name + "\\" + idx3)),
                                        Short.parseShort(p.getProperty("IDamage\\" + user.getName() + "\\" + name + "\\" + idx3)));
                            } catch (Exception e) {
                                getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: Inv at {2}",
                                        new Object[]{user.getName(), name, idx3});
                                continue;
                            }
                            int enchantCount;
                            try {
                                enchantCount = Integer.parseInt(p.getProperty("IEnchant\\" + user.getName() + "\\" + name + "\\" + idx3));
                            } catch (Exception e) {
                                getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: CEInv at {2}",
                                        new Object[]{user.getName(), name, idx3});
                                continue;
                            }
                            for (int idx4 = 0; idx4 < enchantCount; idx4++) {
                                try {
                                    inv[idx3].addEnchantment(Enchantment.getByName(
                                            p.getProperty("IEnchantment\\" + user.getName() + "\\" + idx3 + "\\" + name + "\\" + idx4)),
                                            Integer.parseInt(p.getProperty("IEnchantLevel" + "\\"
                                            + user.getName() + "\\" + idx3 + "\\" + name + "\\" + idx4)));
                                } catch (Exception e) {
                                    getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: EInv at {2}",
                                            new Object[]{user.getName(), name, idx4});
                                    continue;
                                }
                            }
                        }
                    }
                    ItemStack[] armor = new ItemStack[4];
                    for (int idx3 = 0; idx3 < armor.length; idx3++) {
                        String aType;
                        try {
                            aType = p.getProperty("AType\\" + user.getName() + "\\" + name + "\\" + idx3);
                        } catch (Exception e) {
                            getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: Armor at {2}",
                                    new Object[]{user.getName(), name, idx3});
                            continue;
                        }
                        if (!aType.equals("null")) {
                            try {
                                armor[idx3] = new ItemStack(
                                        Integer.parseInt(aType),
                                        Integer.parseInt(p.getProperty("AAmount\\" + user.getName() + "\\" + name + "\\" + idx3)),
                                        Short.parseShort(p.getProperty("ADamage\\" + user.getName() + "\\" + name + "\\" + idx3)));
                            } catch (Exception e) {
                                getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: Armor at {2}",
                                        new Object[]{user.getName(), name, idx3});
                                continue;
                            }
                            int enchantCount;
                            try {
                                enchantCount = Integer.parseInt(p.getProperty("AEnchant\\" + user.getName() + "\\" + name + "\\" + idx3));
                            } catch (Exception e) {
                                getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: CEArmor at {2}",
                                        new Object[]{user.getName(), name, idx3});
                                continue;
                            }
                            for (int idx4 = 0; idx4 < enchantCount; idx4++) {
                                try {
                                    armor[idx3].addEnchantment(Enchantment.getByName(
                                            p.getProperty("AEnchantment\\" + user.getName() + "\\" + idx3 + "\\" + name + "\\" + idx4)),
                                            Integer.parseInt(p.getProperty("AEnchantLevel" + "\\"
                                            + user.getName() + "\\" + idx3 + "\\" + name + "\\" + idx4)));
                                } catch (Exception e) {
                                    getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: EArmor at {2}",
                                            new Object[]{user.getName(), name, idx3});
                                    continue;
                                }
                            }
                        }
                    }
                    GameMode gm = null;
                    try {
                        switch (p.getProperty("GameMode\\" + user.getName() + "\\" + idx2)) {
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
                    } catch (Exception e) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: GameMode",
                                new Object[]{user.getName(), name});
                        continue;
                    }
                    try {
                        Snapshot snap = new Snapshot(user.getName(), name, inv, armor,
                                Float.parseFloat(p.getProperty("Exp\\" + user.getName() + "\\" + idx2)),
                                Integer.parseInt(p.getProperty("Level\\" + user.getName() + "\\" + idx2)), gm,
                                Float.parseFloat(p.getProperty("Exhaustion\\" + user.getName() + "\\" + idx2)),
                                Integer.parseInt(p.getProperty("Food Level\\" + user.getName() + "\\" + idx2)),
                                Float.parseFloat(p.getProperty("Saturation\\" + user.getName() + "\\" + idx2)));
                        user.addSnapshot(snap);
                    } catch (Exception e) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: ELEFS",
                                new Object[]{user.getName(), name});
                    }
                }
            }
        } catch (Exception e) {
            getLogger().severe("Unknown error loading snapshots!");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                getLogger().severe("Error closing input stream.");
            }
        }
    }

    /**
     * Called when a player joins the server.
     * 
     * @param evt The event that occurred
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        if (!isUserRegistered(evt.getPlayer().getName()) && evt.getPlayer().hasPermission("admintoggle.*")) {
            users.add(new User(evt.getPlayer().getName()));
            evt.getPlayer().sendMessage("This is your first time using Admin Toggle. For instructions on");
            evt.getPlayer().sendMessage("using this plugin you can view the read me file here:");
            evt.getPlayer().sendMessage("https://github.com/Blir/AdminToggle - I hope you find this");
            evt.getPlayer().sendMessage("plugin useful! :)");
        }
    }
}
