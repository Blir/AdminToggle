package com.minepop.servegame.admintoggle;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Blir
 * @version 1.0.0
 * @since 11/3/2012
 */
public class Admin extends JavaPlugin implements Listener {

    private final ArrayList<User> users = new ArrayList<>(1);
    private String folder;
    private boolean loadedProperly = true;
    private final int README_VERSION = 1;
    private boolean vault;
    private boolean loadBalance;
    private boolean loadHunger;
    private boolean loadGameMode;
    private boolean loadExp;
    private boolean backup;
    private Economy econ = null;

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        vault = getServer().getPluginManager().isPluginEnabled("Vault");
        if (vault) {
            RegisteredServiceProvider<Economy> rsp;
            rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
            } else {
                getLogger().warning("Failed to retrieve economy from Vault.");
                vault = false;
            }
        }
        loadBalance = getConfig().getBoolean("snapshots.savebalance");
        loadHunger = getConfig().getBoolean("snapshots.savehunger");
        loadGameMode = getConfig().getBoolean("snapshots.savegamemode");
        loadExp = getConfig().getBoolean("snapshots.saveexp");
        backup = getConfig().getBoolean("snapshots.backup");
        getServer().getPluginManager().registerEvents(this, this);
        if (!getDataFolder().isDirectory()) {
            getDataFolder().mkdir();
        }
        folder = getDataFolder().getPath();
        loadSnapshots();
        if (!loadedProperly) {
            getLogger().warning("Since the plugin did not load properly, it will"
                    + "not save upon disabling. You will need to manually save or correct the error in "
                    + "the file, if there is one. If this issue persists, contact Blir at "
                    + "             http://dev.bukkit.org/profiles/Bliry/");
            for (Player player : getServer().getOnlinePlayers()) {
                if (player.hasPermission("admintoggle.*")) {
                    player.sendMessage("§4Since AdminToggle did not load properly, it will not save upon "
                            + "disabling. You will need to manually save or correct the error "
                            + "in the file, if there is one. If this issue persists, contact Blir at "
                            + "http://dev.bukkit.org/profiles/Bliry/");
                }
            }
        }
        if (getConfig().getString("readme.version").equals(String.valueOf(README_VERSION))) {
            return;
        }
        InputStream sourceFile = getResource("README.txt");
        Scanner sourceFileReader = null;
        PrintWriter destFileWriter = null;
        try {
            sourceFileReader = new Scanner(sourceFile);
            destFileWriter = new PrintWriter(new File(folder + "/README.txt"));
            while (sourceFileReader.hasNext()) {
                destFileWriter.println(sourceFileReader.nextLine());
            }
        } catch (IOException | NullPointerException e) {
            getLogger().warning("Error copying readme.txt");
        } finally {
            try {
                sourceFileReader.close();
                destFileWriter.close();
                sourceFile.close();
            } catch (IOException e) {
                getLogger().warning("Error closing streams after copying readme.txt");
            }
        }
        getConfig().set("readme.version", README_VERSION);
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        if (loadedProperly) {
            saveSnapshots();
            if (backup) {
                backupSnapshots();
            }
        } else {
            getLogger().warning("Not saving since the plugin didn't load properly.");
        }

        loadBalance = getConfig().getBoolean("snapshots.savebalance");
        loadHunger = getConfig().getBoolean("snapshots.savehunger");
        loadGameMode = getConfig().getBoolean("snapshots.savegamemode");
        loadExp = getConfig().getBoolean("snapshots.saveexp");
        getConfig().set("snapshots.savebalance", loadBalance);
        getConfig().set("snapshots.savehunger", loadHunger);
        getConfig().set("snapshots.savegamemode", loadGameMode);
        getConfig().set("snapshots.saveexp", loadExp);
        getConfig().set("snapshots.backup", backup);
        saveConfig();
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

        switch (cmd.getName().toLowerCase()) {
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
                    saveSnapshot(user, "temp", player, user.snapshotExists("temp"));
                    if (loadSnapshot(user, "legit", player, null)) {
                        player.sendMessage("§aSnapshot \"legit\" loaded!");
                    } else {
                        player.sendMessage("§cThe snapshot \"legit\" doesn't exist!");
                    }
                } else {
                    saveSnapshot(user, "legit", player, user.snapshotExists("legit"));
                    if (loadSnapshot(user, "admin", player, null)) {
                        player.sendMessage("§aSnapshot \"admin\" loaded!");
                    } else {
                        player.sendMessage("§cThe snapshot \"admin\" doesn't exist!");
                    }
                }
                player.sendMessage("§aAdmin mode " + (user.invertAdminMode() ? "enabled." : "disabled."));
                return true;
            case "newsnapshot":
            case "newsnap":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                } else if (args.length != 1) {
                    return false;
                }
                if (saveSnapshot(user, args[0], player, false)) {
                    player.sendMessage("§aSnapshot \"" + args[0] + "\" saved!");
                } else {
                    player.sendMessage("§cThe snapshot \"" + args[0] + "\" already exists!");
                }
                return true;
            case "overwritesnapshot":
            case "overwritesnap":
            case "osnap":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 1) {
                    return false;
                }
                if (saveSnapshot(user, args[0], player, true)) {
                    player.sendMessage("§aSnapshot \"" + args[0] + "\" overwritten!");
                } else {
                    player.sendMessage("§cThe snapshot \"" + args[0] + "\" doesn't exist to be overwritten! Snapshot created.");
                    saveSnapshot(user, args[0], player, false);
                }
                return true;
            case "loadsnapshot":
            case "loadsnap":
            case "lsnap":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 1) {
                    return false;
                }
                if (loadSnapshot(user, args[0], player, null)) {
                    player.sendMessage("§aSnapshot \"" + args[0] + "\" loaded!");
                } else {
                    player.sendMessage("§cThe snapshot \"" + args[0] + "\" doesn't exist!");
                }
                return true;
            case "loadothersnapshot":
            case "loadothersnap":
            case "losnap":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 2) {
                    return false;
                }
                User target = getUser(args[0]);
                if (target != null) {
                    if (loadSnapshot(user, args[1], player, target)) {
                        player.sendMessage("§a" + args[0] + "'s snapshot \"" + args[1] + "\" loaded!");
                    } else {
                        player.sendMessage("§cThe user \"" + args[0] + "\" does not have the snapshot \"" + args[1] + ".\"");
                    }
                } else {
                    player.sendMessage("§cThe user \"" + args[0] + "\" doesn't exist or isn't registered.");
                }
                return true;
            case "mysnapshots":
            case "mysnaps":
            case "mysnap":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    return false;
                }
                if (!user.getSnapshots().isEmpty()) {
                    player.sendMessage("§aCurrent snapshots:");
                    for (Snapshot snap : user.getSnapshots()) {
                        player.sendMessage("§a" + snap.getName());
                    }
                } else {
                    player.sendMessage("§aYou have no snapshots.");
                }
                return true;
            case "deletesnapshot":
            case "deletesnap":
            case "delsnap":
            case "dsnap":
            case "removesnashot":
            case "removesnap":
            case "rmsnap":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 1) {
                    return false;
                }
                if (user.removeSnapshot(args[0])) {
                    player.sendMessage("§aSnapshot \"" + args[0] + "\" deleted!");
                } else {
                    player.sendMessage("§cThe snapshot \"" + args[0] + "\" doesn't exist!");
                }
                return true;
            case "savesnapshots":
            case "savesnaps":
            case "savesnap":
                if (args.length != 0) {
                    return false;
                }
                saveSnapshots();
                sender.sendMessage("§aSave complete.");
                return true;
            case "admincheck":
            case "adcheck":
            case "adc":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    return false;
                }
                player.sendMessage("§aAdmin mode is " + (user.isAdminModeEnabled() ? "enabled." : "disabled."));
                return true;
            case "undosnapshot":
            case "undosnaps":
            case "undosnap":
            case "usnaps":
            case "usnap":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    return false;
                }
                if (revertSnapshot(user, player)) {
                    player.sendMessage("§aSnapshot reverted.");
                } else {
                    player.sendMessage("§cYour last snapshot could not be retrieved.");
                }
                return true;
            case "deleteallsnapshots":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("You must be a player to use this command!");
                    return true;
                } else if (args.length != 1 || !args[0].equals("CONFIRM")) {
                    return false;
                }
                user.clearSnapshots();
                player.sendMessage("§aSnapshots deleted.");
                return true;
            case "listallsnapshots":
            case "listallsnaps":
            case "listall":
            case "lasnaps":
                if (args.length != 0) {
                    return false;
                }
                sender.sendMessage("§aAll snapshots: ");
                for (User regUser : users) {
                    if (regUser.getSnapshots().isEmpty()) {
                        sender.sendMessage("§a" + regUser.getName() + " has no snapshots.");
                    } else {
                        sender.sendMessage("§a" + regUser.getName() + "'s snapshots: ");
                        for (Snapshot snap : regUser.getSnapshots()) {
                            sender.sendMessage("§a" + snap.getName());
                        }
                    }
                }
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
     * Returns whether or not the Player with the given name is registered in
     * the plugin.
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
        if (vault) {
            snap.setBalance(econ.getBalance(player.getName()));
        }
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
    public boolean loadSnapshot(User user, String name, Player player, User target) {
        Snapshot snap;
        if (target == null) {
            snap = user.getSnapshot(name);
        } else {
            snap = target.getSnapshot(name);
        }
        if (snap == null) {
            return false;
        }
        user.logSnapshot(snap);
        player.getInventory().setContents(cloneInventory(snap.getInv()));
        player.getInventory().setArmorContents(cloneInventory(snap.getArmor()));
        if (loadGameMode) {
            player.setGameMode(snap.getGameMode());
        }
        if (loadExp) {
            player.setExp(snap.getExp());
            player.setLevel(snap.getLevel());
        }
        if (loadHunger) {
            player.setExhaustion(snap.getExhaustion());
            player.setFoodLevel(snap.getFoodLevel());
            player.setSaturation(snap.getSaturation());
        }
        if (loadBalance) {
            setBalance(player.getName(), snap.getBalance());
        }
        return true;
    }

    /**
     * Reverts the Snapshot for the given User and Player.
     *
     * @param user The User associated with the Player
     * @param player The Player to have their Snapshot reverted
     * @return true if the Snapshot was reverted
     */
    public boolean revertSnapshot(User user, Player player) {
        Snapshot snap = user.revertSnapshot();
        if (snap == null) {
            return false;
        }
        player.getInventory().setContents(cloneInventory(snap.getInv()));
        player.getInventory().setArmorContents(cloneInventory(snap.getArmor()));
        if (loadGameMode) {
            player.setGameMode(snap.getGameMode());
        }
        if (loadExp) {
            player.setExp(snap.getExp());
            player.setLevel(snap.getLevel());
        }
        if (loadHunger) {
            player.setExhaustion(snap.getExhaustion());
            player.setFoodLevel(snap.getFoodLevel());
            player.setSaturation(snap.getSaturation());
        }
        if (loadBalance) {
            setBalance(player.getName(), snap.getBalance());
        }
        return true;
    }

    /**
     * Sets the balance for the Player with the given name.
     *
     * @param name The name of the Player to have their balance set
     * @param amount The amount the Player's balance will be set to
     */
    public void setBalance(String name, double amount) {
        if (!vault) {
            return;
        }
        econ.withdrawPlayer(name, econ.getBalance(name));
        econ.depositPlayer(name, amount);
    }

    /**
     * Clones the sent ItemStacks.
     *
     * @param inv The ItemStacks to be cloned
     * @return The cloned ItemStacks
     */
    public ItemStack[] cloneInventory(ItemStack[] stack) {
        ItemStack[] inventory = new ItemStack[stack.length];
        for (int idx = 0; idx < stack.length; idx++) {
            if (stack[idx] != null) {
                inventory[idx] = stack[idx].clone();
            }
        }
        return inventory;
    }

    /**
     * Saves all Snapshots to file.
     */
    public void saveSnapshots() {
        File file = new File(folder + "/snapshots.properties");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            getLogger().severe("Failed to open output stream!");
            return;
        }
        Properties p = new Properties();
        p.setProperty("User Count", String.valueOf(users.size()));
        for (int idx = 0; idx < users.size(); idx++) {
            User user = users.get(idx);
            p.setProperty("User/" + idx, user.getName());
            p.setProperty("Admin/" + idx, String.valueOf(user.isAdminModeEnabled()));
            p.setProperty(user.getName() + "/Snapshot Count", String.valueOf(user.getSnapshots().size()));
            for (int idx2 = 0; idx2 < user.getSnapshots().size(); idx2++) {
                Snapshot snap = user.getSnapshots().get(idx2);
                p.setProperty(user.getName() + "/Snapshot/" + idx2, snap.getName());
                for (int idx3 = 0; idx3 < 36; idx3++) {
                    ItemStack inv = snap.getInv()[idx3];
                    if (inv != null) {
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IType/"
                                + idx3, String.valueOf(inv.getTypeId()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IAmount/"
                                + idx3, String.valueOf(inv.getAmount()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IDamage/"
                                + idx3, String.valueOf(inv.getDurability()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IEnchant/"
                                + idx3, String.valueOf(inv.getEnchantments().size()));
                        int idx4 = 0;
                        for (Iterator<Enchantment> it = inv.getEnchantments().keySet().iterator(); it.hasNext();) {
                            Enchantment ench = it.next();
                            p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3
                                    + "/IEnchantment/" + idx4, String.valueOf(ench.getName()));
                            p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3
                                    + "/IEnchantLevel/" + idx4, String.valueOf(inv.getEnchantmentLevel(ench)));
                            idx4++;
                        }
                    } else {
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IType/" + idx3, "null");
                    }
                }
                for (int idx3 = 0; idx3 < 4; idx3++) {
                    ItemStack armor = snap.getArmor()[idx3];
                    if (armor != null) {
                        p.setProperty(user.getName() + "/" + snap.getName() + "/AType/" + idx3,
                                String.valueOf(armor.getTypeId()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/AAmount/" + idx3,
                                String.valueOf(armor.getAmount()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/ADamage/" + idx3,
                                String.valueOf(armor.getDurability()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/AEnchant/" + idx3,
                                String.valueOf(armor.getEnchantments().size()));
                        int idx4 = 0;
                        for (Iterator<Enchantment> it = armor.getEnchantments().keySet().iterator(); it.hasNext();) {
                            Enchantment ench = it.next();
                            p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3
                                    + "/AEnchantment/" + idx4, String.valueOf(ench.getName()));
                            p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3
                                    + "/AEnchantLevel/" + idx4, String.valueOf(armor.getEnchantmentLevel(ench)));
                            idx4++;
                        }
                    } else {
                        p.setProperty(user.getName() + "/" + snap.getName() + "/AType/" + idx3, "null");
                    }
                }
                switch (snap.getGameMode()) {
                    case CREATIVE:
                        p.setProperty(user.getName() + "/GameMode/" + snap.getName(), "creative");
                        break;
                    case SURVIVAL:
                        p.setProperty(user.getName() + "/GameMode/" + snap.getName(), "survival");
                        break;
                    case ADVENTURE:
                        p.setProperty(user.getName() + "/GameMode/" + snap.getName(), "adventure");
                        break;
                }
                p.setProperty(user.getName() + "/Exp/" + snap.getName(), String.valueOf(snap.getExp()));
                p.setProperty(user.getName() + "/Level/" + snap.getName(), String.valueOf(snap.getLevel()));
                p.setProperty(user.getName() + "/Exhaustion/" + snap.getName(), String.valueOf(snap.getExhaustion()));
                p.setProperty(user.getName() + "/Food Level/" + snap.getName(), String.valueOf(snap.getFoodLevel()));
                p.setProperty(user.getName() + "/Saturation/" + snap.getName(), String.valueOf(snap.getSaturation()));
                p.setProperty(user.getName() + "/Balance/" + snap.getName(), String.valueOf(snap.getBalance()));
            }
        }
        try {
            p.store(fos, null);
        } catch (IOException e) {
            getLogger().severe("Failed to save snapshots.properties!");
        }
        try {
            fos.close();
        } catch (IOException e) {
            getLogger().severe("Error closing the output stream! Data might not be saved.");
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
        } catch (FileNotFoundException e) {
            getLogger().severe("snapshots.properties file missing!");
            return;
        }
        Properties p = new Properties();
        try {
            p.load(fis);
        } catch (IOException e) {
            getLogger().severe("Failed to open the input stream!");
            return;
        }
        int userCount;
        try {
            userCount = Integer.parseInt(p.getProperty("User Count"));
        } catch (NumberFormatException | NullPointerException e) {
            getLogger().log(Level.SEVERE, "Error loading user count");
            loadedProperly = false;
            return;
        }
        for (int idx = 0; idx < userCount; idx++) {
            String userName = p.getProperty("User/" + idx);
            if (userName == null) {
                getLogger().log(Level.SEVERE, "Error loading user {0}",
                        idx);
                loadedProperly = false;
                continue;
            }
            User user = new User(userName);
            users.add(user);
            try {
                user.setAdminMode(Boolean.parseBoolean(p.getProperty("Admin/" + idx)));
            } catch (NumberFormatException | NullPointerException e) {
                getLogger().log(Level.WARNING, "Error loading {0}''s admin mode setting, set to false",
                        user.getName());
            }
            int snapCount;
            try {
                snapCount = Integer.parseInt(p.getProperty(user.getName() + "/Snapshot Count"));
            } catch (NumberFormatException | NullPointerException e) {
                getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot Count",
                        user.getName());
                loadedProperly = false;
                continue;
            }
            for (int idx2 = 0; idx2 < snapCount; idx2++) {
                ItemStack[] inv = new ItemStack[36];
                String name = p.getProperty(user.getName() + "/Snapshot/" + idx2);
                if (name == null) {
                    getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot at {1}",
                            new Object[]{user.getName(), idx2});
                    loadedProperly = false;
                    continue;
                }
                for (int idx3 = 0; idx3 < 36; idx3++) {
                    String iType = p.getProperty(user.getName() + "/" + name + "/IType/" + idx3);
                    if (iType == null) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: Inv at {2}",
                                new Object[]{user.getName(), name, idx3});
                        loadedProperly = false;
                        continue;
                    }
                    if (iType.equals("null")) {
                        continue;
                    }
                    try {
                        inv[idx3] = new ItemStack(
                                Integer.parseInt(iType),
                                Integer.parseInt(p.getProperty(user.getName() + "/" + name + "/IAmount/" + idx3)),
                                Short.parseShort(p.getProperty(user.getName() + "/" + name + "/IDamage/" + idx3)));
                    } catch (NumberFormatException | NullPointerException e) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: Inv at {2}",
                                new Object[]{user.getName(), name, idx3});
                        loadedProperly = false;
                        continue;
                    }
                    int enchantCount;
                    try {
                        enchantCount = Integer.parseInt(p.getProperty(user.getName() + "/" + name + "/IEnchant/" + idx3));
                    } catch (NullPointerException | NumberFormatException e) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: CEInv at {2}",
                                new Object[]{user.getName(), name, idx3});
                        loadedProperly = false;
                        continue;
                    }
                    for (int idx4 = 0; idx4 < enchantCount; idx4++) {
                        try {
                            inv[idx3].addEnchantment(Enchantment.getByName(
                                    p.getProperty(user.getName() + "/" + name + "/" + idx3 + "/IEnchantment/" + idx4)),
                                    Integer.parseInt(p.getProperty(user.getName() + "/"
                                    + name + "/" + idx3 + "/IEnchantLevel/" + idx4)));
                        } catch (NullPointerException | NumberFormatException e) {
                            getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: EInv at {2}",
                                    new Object[]{user.getName(), name, idx4});
                            loadedProperly = false;
                            continue;
                        }
                    }
                }
                ItemStack[] armor = new ItemStack[4];
                for (int idx3 = 0; idx3 < 4; idx3++) {
                    String aType = p.getProperty(user.getName() + "/" + name + "/AType/" + idx3);
                    if (aType == null) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: Armor at {2}",
                                new Object[]{user.getName(), name, idx3});
                        loadedProperly = false;
                        continue;
                    }
                    if (aType.equals("null")) {
                        continue;
                    }
                    try {
                        armor[idx3] = new ItemStack(
                                Integer.parseInt(aType),
                                Integer.parseInt(p.getProperty(user.getName() + "/" + name + "/AAmount/" + idx3)),
                                Short.parseShort(p.getProperty(user.getName() + "/" + name + "/ADamage/" + idx3)));
                    } catch (NumberFormatException | NullPointerException e) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: Armor at {2}",
                                new Object[]{user.getName(), name, idx3});
                        loadedProperly = false;
                        continue;
                    }
                    int enchantCount;
                    try {
                        enchantCount = Integer.parseInt(p.getProperty(user.getName() + "/" + name + "/AEnchant/" + idx3));
                    } catch (NullPointerException | NumberFormatException e) {
                        getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: CEArmor at {2}",
                                new Object[]{user.getName(), name, idx3});
                        loadedProperly = false;
                        continue;
                    }
                    for (int idx4 = 0; idx4 < enchantCount; idx4++) {
                        try {
                            armor[idx3].addEnchantment(Enchantment.getByName(
                                    p.getProperty(user.getName() + "/" + name + "/" + idx3 + "/AEnchantment/" + idx4)),
                                    Integer.parseInt(p.getProperty(user.getName() + "/"
                                    + name + "/" + idx3 + "/AEnchantLevel/" + idx4)));
                        } catch (NullPointerException | NumberFormatException e) {
                            getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: EArmor at {2}",
                                    new Object[]{user.getName(), name, idx3});
                            loadedProperly = false;
                            continue;
                        }
                    }
                }
                GameMode gm;
                try {
                    switch (p.getProperty(user.getName() + "/GameMode/" + name)) {
                        case "creative":
                            gm = GameMode.CREATIVE;
                            break;
                        case "survival":
                            gm = GameMode.SURVIVAL;
                            break;
                        case "adventure":
                            gm = GameMode.ADVENTURE;
                            break;
                        default:
                            getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: GameMode, set to survival",
                                    new Object[]{user.getName(), name});
                            gm = GameMode.SURVIVAL;
                            break;
                    }
                } catch (NullPointerException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: GameMode, set to survival",
                            new Object[]{user.getName(), name});
                    gm = GameMode.SURVIVAL;
                }
                double balance;
                try {
                    balance = Double.parseDouble(p.getProperty(user.getName() + "/Balance/" + name));
                } catch (NumberFormatException | NullPointerException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Balance, set to 0.0",
                            new Object[]{user.getName(), name});
                    balance = 0.0;
                }
                float exp;
                try {
                    exp = Float.parseFloat(p.getProperty(user.getName() + "/Exp/" + name));
                } catch (NullPointerException | NumberFormatException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Exp, set to 0",
                            new Object[]{user.getName(), name});
                    exp = 0;
                }
                int level;
                try {
                    level = Integer.parseInt(p.getProperty(user.getName() + "/Level/" + name));
                } catch (NullPointerException | NumberFormatException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Level, set to 0",
                            new Object[]{user.getName(), name});
                    level = 0;
                }
                float ex;
                try {
                    ex = Float.parseFloat(p.getProperty(user.getName() + "/Exhaustion/" + name));
                } catch (NullPointerException | NumberFormatException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Exhaustion, set to 2.5",
                            new Object[]{user.getName(), name});
                    ex = (float) 2.5;
                }
                int foodLevel;
                try {
                    foodLevel = Integer.parseInt(p.getProperty(user.getName() + "/Food Level/" + name));
                } catch (NullPointerException | NumberFormatException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Food Level, set to 20",
                            new Object[]{user.getName(), name});
                    foodLevel = 20;
                }
                float sat;
                try {
                    sat = Float.parseFloat(p.getProperty(user.getName() + "/Saturation/" + name));
                } catch (NullPointerException | NumberFormatException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Saturation, set to 0",
                            new Object[]{user.getName(), name});
                    sat = 0;
                }
                try {
                    Snapshot snap = new Snapshot(
                            user.getName(), name, inv, armor, exp, level, gm, ex, foodLevel, sat, balance);
                    user.addSnapshot(snap);
                } catch (NullPointerException e) {
                    getLogger().log(Level.SEVERE, "Error loading {0}''s Snapshot {1}: ELEFS",
                            new Object[]{user.getName(), name});
                    loadedProperly = false;
                }
            }
        }
        try {
            fis.close();
        } catch (IOException e) {
            getLogger().warning("Error closing input stream.");
        }
    }

    /**
     * Called when a player joins the server.
     *
     * @param evt The event that occurred
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        if (!isUserRegistered(evt.getPlayer().getName()) && (evt.getPlayer().hasPermission("admintoggle.*")
                || evt.getPlayer().hasPermission("admintoggle.basic"))) {
            users.add(new User(evt.getPlayer().getName()));
            evt.getPlayer().sendMessage("§aThis is your first time using Admin Toggle. For instructions on"
                    + "using this plugin you can view the read me file here:"
                    + "https://github.com/Blir/AdminToggle - I hope you find this"
                    + "plugin useful! :)");
        }
    }

    /**
     * Saves all Snapshots to file.
     */
    public void backupSnapshots() {
        File file = new File(folder + "/snapshots_backup.properties");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            getLogger().severe("Failed to open output stream!");
            return;
        }
        Properties p = new Properties();
        p.setProperty("User Count", String.valueOf(users.size()));
        for (int idx = 0; idx < users.size(); idx++) {
            User user = users.get(idx);
            p.setProperty("User/" + idx, user.getName());
            p.setProperty("Admin/" + idx, String.valueOf(user.isAdminModeEnabled()));
            p.setProperty(user.getName() + "/Snapshot Count", String.valueOf(user.getSnapshots().size()));
            for (int idx2 = 0; idx2 < user.getSnapshots().size(); idx2++) {
                Snapshot snap = user.getSnapshots().get(idx2);
                p.setProperty(user.getName() + "/Snapshot/" + idx2, snap.getName());
                for (int idx3 = 0; idx3 < 36; idx3++) {
                    ItemStack inv = snap.getInv()[idx3];
                    if (inv != null) {
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IType/"
                                + idx3, String.valueOf(inv.getTypeId()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IAmount/"
                                + idx3, String.valueOf(inv.getAmount()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IDamage/"
                                + idx3, String.valueOf(inv.getDurability()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IEnchant/"
                                + idx3, String.valueOf(inv.getEnchantments().size()));
                        int idx4 = 0;
                        for (Iterator<Enchantment> it = inv.getEnchantments().keySet().iterator(); it.hasNext();) {
                            Enchantment ench = it.next();
                            p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3
                                    + "/IEnchantment/" + idx4, String.valueOf(ench.getName()));
                            p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3
                                    + "/IEnchantLevel/" + idx4, String.valueOf(inv.getEnchantmentLevel(ench)));
                            idx4++;
                        }
                    } else {
                        p.setProperty(user.getName() + "/" + snap.getName() + "/IType/" + idx3, "null");
                    }
                }
                for (int idx3 = 0; idx3 < 4; idx3++) {
                    ItemStack armor = snap.getArmor()[idx3];
                    if (armor != null) {
                        p.setProperty(user.getName() + "/" + snap.getName() + "/AType/" + idx3,
                                String.valueOf(armor.getTypeId()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/AAmount/" + idx3,
                                String.valueOf(armor.getAmount()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/ADamage/" + idx3,
                                String.valueOf(armor.getDurability()));
                        p.setProperty(user.getName() + "/" + snap.getName() + "/AEnchant/" + idx3,
                                String.valueOf(armor.getEnchantments().size()));
                        int idx4 = 0;
                        for (Iterator<Enchantment> it = armor.getEnchantments().keySet().iterator(); it.hasNext();) {
                            Enchantment ench = it.next();
                            p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3
                                    + "/AEnchantment/" + idx4, String.valueOf(ench.getName()));
                            p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3
                                    + "/AEnchantLevel/" + idx4, String.valueOf(armor.getEnchantmentLevel(ench)));
                            idx4++;
                        }
                    } else {
                        p.setProperty(user.getName() + "/" + snap.getName() + "/AType/" + idx3, "null");
                    }
                }
                switch (snap.getGameMode()) {
                    case CREATIVE:
                        p.setProperty(user.getName() + "/GameMode/" + snap.getName(), "creative");
                        break;
                    case SURVIVAL:
                        p.setProperty(user.getName() + "/GameMode/" + snap.getName(), "survival");
                        break;
                    case ADVENTURE:
                        p.setProperty(user.getName() + "/GameMode/" + snap.getName(), "adventure");
                        break;
                }
                p.setProperty(user.getName() + "/Exp/" + snap.getName(), String.valueOf(snap.getExp()));
                p.setProperty(user.getName() + "/Level/" + snap.getName(), String.valueOf(snap.getLevel()));
                p.setProperty(user.getName() + "/Exhaustion/" + snap.getName(), String.valueOf(snap.getExhaustion()));
                p.setProperty(user.getName() + "/Food Level/" + snap.getName(), String.valueOf(snap.getFoodLevel()));
                p.setProperty(user.getName() + "/Saturation/" + snap.getName(), String.valueOf(snap.getSaturation()));
                p.setProperty(user.getName() + "/Balance/" + snap.getName(), String.valueOf(snap.getBalance()));
            }
        }
        try {
            p.store(fos, null);
        } catch (IOException e) {
            getLogger().severe("Failed to save snapshots.properties!");
        }
        try {
            fos.close();
        } catch (IOException e) {
            getLogger().severe("Error closing the output stream! Data might not be saved.");
        }
    }
}