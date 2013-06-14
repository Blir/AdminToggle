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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.minepop.servegame.admintoggle.Snapshot.Visibility;
import java.text.DateFormat;

/**
 * To-do: //////////////////////////////////////////////////////////////////////
 * More admin commands to manage others' snapshots /////////////////////////////
 * Save ItemStack metadata /////////////////////////////////////////////////////
 */
/**
 *
 * @author Blir
 * @version 1.2.0 Beta
 * @since 13 June 2013
 */
public class Admin extends JavaPlugin implements Listener {

    private final ArrayList<User> users = new ArrayList<>(0);
    private final ArrayList<WorldGroup> worldGroups = new ArrayList<>(0);
    private final int README_VERSION = 4;
    private File snapFile, snapFileBackup, wgFile;
    private boolean vault, loadBalance, loadHunger, loadGameMode, loadExp, backup,
            loadedProperly = true, mkd;
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
            getDataFolder().mkdirs();
        }
        snapFile = new File(getDataFolder().getPath() + "/snapshots.properties");
        snapFileBackup = new File(getDataFolder().getPath() + "/snapshots_backup - " + DateFormat.getDateInstance().format(Calendar.getInstance().getTime()) + ".properties");
        wgFile = new File(getDataFolder().getPath() + "/linkedworlds.properties");
        mkd = getConfig().getBoolean("worldgroups.makedefault");
        User.setAdmin(this);
        load();
        if (!loadedProperly) {
            getLogger().warning("Since the plugin did not load properly, it will "
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
        if (!isWorldGroup("Default") && mkd && !isInWorldGroup("world") && !isInWorldGroup("world_nether") && !isInWorldGroup("world_the_end")) {
            worldGroups.add(new WorldGroup("Default", new String[]{"world", "world_nether", "world_the_end"}));
        }
        if (!getConfig().getString("readme.version").equals(String.valueOf(README_VERSION))) {
            saveResource("README.txt", true);
            getConfig().set("readme.version", README_VERSION);
        }
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        if (loadedProperly) {
            save(false);
            if (backup) {
                save(true);
            }
        } else {
            getLogger().warning("Not saving since the plugin didn't load properly.");
        }
        getConfig().set("snapshots.savebalance", loadBalance);
        getConfig().set("snapshots.savehunger", loadHunger);
        getConfig().set("snapshots.savegamemode", loadGameMode);
        getConfig().set("snapshots.saveexp", loadExp);
        getConfig().set("snapshots.backup", backup);
        getConfig().set("worldgroups.makedefault", mkd);
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
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (user.isAdminModeEnabled()) {
                    saveSnapshot(user, "temp", player, user.hasSnapshot("temp", player.getWorld().getName()));
                    if (loadSnapshot(user, "legit", player, null)) {
                        player.sendMessage("§aSnapshot \"§9legit§a\" loaded.");
                    } else {
                        player.sendMessage("§cThe snapshot \"§9legit§c\" doesn't exist or isn't accessible from this world!");
                    }
                } else {
                    Snapshot snap = user.getSnapshot("legit", player.getWorld().getName());
                    if (user.getCurrentSnapshot() == snap || snap == null) {
                        saveSnapshot(user, "legit", player, snap != null);
                    }
                    if (loadSnapshot(user, "admin", player, null)) {
                        player.sendMessage("§aSnapshot \"§9admin§a\" loaded.");
                    } else {
                        player.sendMessage("§cThe snapshot \"§9admin§c\" doesn't exist or isn't accessible from this world!");
                    }
                }
                player.sendMessage("§aAdmin mode §9" + (user.invertAdminMode() ? "enabled" : "disabled") + "§a.");
                return true;
            case "newsnapshot":
            case "newsnap":
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 1) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (saveSnapshot(user, args[0], player, false)) {
                    player.sendMessage("§aSnapshot \"§9" + args[0] + "§a\" created.");
                } else {
                    player.sendMessage("§cThe snapshot \"§9" + args[0] + "§c\" already exists!");
                }
                return true;
            case "overwritesnapshot":
            case "overwritesnap":
            case "osnap":
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 1) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (saveSnapshot(user, args[0], player, true)) {
                    player.sendMessage("§aSnapshot \"§9" + args[0] + "§a\" overwritten.");
                } else {
                    player.sendMessage("§cThe snapshot \"§9" + args[0] + "§c\" doesn't exist to be overwritten or isn't accessible from this world!");
                }
                return true;
            case "loadsnapshot":
            case "loadsnap":
            case "lsnap":
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 1) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (loadSnapshot(user, args[0], player, null)) {
                    player.sendMessage("§aSnapshot \"§9" + args[0] + "§a\" loaded!");
                } else {
                    player.sendMessage("§cThe snapshot \"§9" + args[0] + "§c\" doesn't exist or isn't accessible from this world!");
                }
                return true;
            case "loadothersnapshot":
            case "loadothersnap":
            case "losnap":
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 2) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (isUserRegistered(args[0])) {
                    User target = getUser(args[0]);
                    if (loadSnapshot(user, args[1], player, target)) {
                        player.sendMessage("§9" + args[0] + "§a's snapshot \"§9" + args[1] + "§a\" loaded!");
                    } else {
                        player.sendMessage("§cThe user \"§9" + args[0] + "§c\" does not have the snapshot \"§9" + args[1] + "§c\" or it isn't accessible from this world!");
                    }
                } else {
                    if (getServer().getPlayer(args[0]) == null) {
                        player.sendMessage("§cThe user \"§9" + args[0] + "§c\" doesn't exist.");
                    } else {
                        player.sendMessage("§cThe user \"§9" + args[0] + "§c\" isn't registered.");
                    }
                }
                return true;
            case "listsnapshots":
            case "listsnaps":
            case "listsnap":
                if (user != null && player != null) {
                    if (args.length != 0) {
                        player.sendMessage("§cIncorrect amount of arguments!");
                        player.sendMessage("§c/listsnapshots");
                        return true;
                    }
                    if (!user.getSnapshots().isEmpty()) {
                        player.sendMessage("§aCurrent snapshots:");
                        for (Snapshot snap : user.getSnapshots()) {
                            player.sendMessage("§a\"§9" + snap.getName() + "§a\" in the world \"§9" + snap.getWorld() + "§a\" of visibility type §9" + snap.getVisibility() + "§a.");
                        }
                    } else {
                        player.sendMessage("§aYou have no snapshots.");
                    }
                } else {
                    if (args.length != 1) {
                        sender.sendMessage("§cIncorrect amount of arguments!");
                        sender.sendMessage("§c/listsnapshots <user name>");
                        return true;
                    }
                    if (isUserRegistered(args[0])) {
                        user = getUser(args[0]);
                        if (!user.getSnapshots().isEmpty()) {
                            sender.sendMessage("§9" + args[0] + "§a's snapshots:");
                            for (Snapshot snap : user.getSnapshots()) {
                                sender.sendMessage("§a\"§9" + snap.getName() + "§a\" in the world \"§9" + snap.getWorld() + "§a\" of visibility type §9" + snap.getVisibility() + "§a.");
                            }
                        } else {
                            sender.sendMessage("§9" + args[0] + "§a has no snapshots.");
                        }
                    } else {
                        if (getServer().getPlayer(args[0]) == null) {
                            sender.sendMessage("§cThe user \"§9" + args[0] + "§c\" doesn't exist.");
                        } else {
                            sender.sendMessage("§cThe user \"§9" + args[0] + "§c\" user isn't registered.");
                        }
                    }
                }
                return true;
            case "deletesnapshot":
            case "deletesnap":
            case "delsnap":
            case "dsnap":
            case "removesnashot":
            case "removesnap":
            case "rmsnap":
                if (user != null && player != null) {
                    if (args.length != 1) {
                        player.sendMessage("§cIncorrect amount of arguments!");
                        player.sendMessage("§c/deletesnapshot <snapshot name>");
                        return true;
                    }
                    if (user.removeSnapshot(args[0], player.getWorld().getName())) {
                        player.sendMessage("§aSnapshot \"§9" + args[0] + "§a\" deleted!");
                    } else {
                        player.sendMessage("§cThe snapshot \"§9" + args[0] + "§a\" doesn't exist or isn't accissible from this world!");
                    }
                } else {
                    if (args.length != 3) {
                        sender.sendMessage("§cIncorrect amount of arguments!");
                        sender.sendMessage("§c/deletesnapshot <snapshot name> <user name> <world>");
                        return true;
                    }
                    if (isUserRegistered(args[1])) {
                        user = getUser(args[1]);
                        if (getServer().getWorld(args[2]) != null && user.removeSnapshot(args[0], args[2])) {
                            sender.sendMessage("§9" + args[1] + "§a's snapshot \"§9" + args[0] + "§a\" has been deleted from the world \"§9" + args[2] + "§a.\"");
                        } else {
                            sender.sendMessage("§9" + args[1] + "§c's snapshot \"§9" + args[0] + "§c\" doesn't exist or isn't accessible in the world \"§9" + args[2] + "§c.\"");
                        }
                    } else {
                        if (getServer().getPlayer(args[1]) == null) {
                            sender.sendMessage("§cThe user \"§9" + args[1] + "§c\" doesn't exist.");
                        } else {
                            sender.sendMessage("§cThe user \"§9" + args[1] + "§c\" isn't registered.");
                        }
                    }
                }
                return true;
            case "savesnapshots":
            case "savesnaps":
            case "savesnap":
                if (args.length != 0) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                save(false);
                sender.sendMessage("§aSave complete.");
                return true;
            case "admincheck":
            case "adcheck":
            case "adc":
                if (user != null && player != null) {
                    if (args.length != 0) {
                        player.sendMessage("§cIncorrect amount of arguments!");
                        player.sendMessage("§c/admincheck");
                        return true;
                    }
                    player.sendMessage("§aAdmin mode is §9" + (user.isAdminModeEnabled() ? "enabled" : "disabled") + "§a.");
                } else {
                    if (args.length != 1) {
                        sender.sendMessage("§cIncorrect amount of arguments!");
                        sender.sendMessage("§c/admincheck <user name>");
                        return true;
                    }
                    if (isUserRegistered(args[0])) {
                        sender.sendMessage("§aAdmin mode is §9" + (getUser(args[0]).isAdminModeEnabled() ? "enabled" : "disabled") + "§a for \"§9" + args[0] + "§a.\"");
                    } else {
                        if (getServer().getPlayer(args[0]) == null) {
                            sender.sendMessage("§cThe user \"§9" + args[0] + "§c\" doesn't exist.");
                        } else {
                            sender.sendMessage("§cThe user \"§9" + args[0] + "§c\" isn't registered.");
                        }
                    }
                }
                return true;
            case "undosnapshot":
            case "undosnaps":
            case "undosnap":
            case "usnaps":
            case "usnap":
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (revertSnapshot(user, player)) {
                    player.sendMessage("§aSnapshot reverted.");
                } else {
                    player.sendMessage("§cYour last snapshot could not be retrieved.");
                }
                return true;
            case "deletemysnapshots":
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 1 || !args[0].equals("CONFIRM")) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                user.clearSnapshots();
                player.sendMessage("§aSnapshots deleted.");
                return true;
            case "allsnapshots":
            case "allsnaps":
            case "allsnap":
                if (args.length != 0) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                sender.sendMessage("§aAll snapshots: ");
                for (User regUser : users) {
                    if (regUser.getSnapshots().isEmpty()) {
                        sender.sendMessage("§2" + regUser.getName() + " has no snapshots.");
                    } else {
                        sender.sendMessage("§2" + regUser.getName() + "'s snapshots: ");
                        for (Snapshot snap : regUser.getSnapshots()) {
                            sender.sendMessage("§a\"§9" + snap.getName() + "§a\" in the world \"§9" + snap.getWorld() + "§a\" of visibility type §9" + snap.getVisibility() + "§a.");
                        }
                    }
                }
                return true;
            case "createworldgroup":
            case "cwg":
                if (args.length < 1) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (isWorldGroup(args[0])) {
                    sender.sendMessage("§c\"§9" + args[0] + "§c\" is already a world group!");
                    return true;
                }
                String[] worlds = null;
                if (args.length > 1) {
                    worlds = new String[args.length - 1];
                }
                for (int idx = 1; idx < args.length; idx++) {
                    if (getServer().getWorld(args[idx]) == null) {
                        sender.sendMessage("§c\"§9" + args[idx] + "§c\" is not a world!");
                        return true;
                    }
                    for (WorldGroup worldGroup : worldGroups) {
                        if (worldGroup.isMember(args[idx])) {
                            sender.sendMessage("§cThe world \"§9" + args[idx] + "§c\" is already in the world group \"§9" + worldGroup.getName() + "§c.\"");
                            return true;
                        }
                    }
                    if (args.length > 1) {
                        worlds[idx - 1] = args[idx];
                    }
                }
                if (worlds != null) {
                    worldGroups.add(new WorldGroup(args[0], worlds));
                } else {
                    worldGroups.add(new WorldGroup(args[0]));
                }
                sender.sendMessage("§aThe world group \"§9" + args[0] + "§a\" has been created.");
                return true;
            case "addtoworldgroup":
            case "awg":
                if (args.length < 2) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (getWorldGroupByName(args[0]) == null) {
                    sender.sendMessage("§cThe world group \"§9" + args[0] + "§c\" doesn't exist!");
                    return true;
                }
                worlds = new String[args.length - 1];
                for (int idx = 1; idx < args.length; idx++) {
                    if (getServer().getWorld(args[idx]) == null) {
                        sender.sendMessage("§cThe world \"§9" + args[idx] + "§c\" doesn't exist!");
                        return true;
                    }
                    for (WorldGroup worldGroup : worldGroups) {
                        if (worldGroup.isMember(args[idx])) {
                            sender.sendMessage("§cThe world \"§9" + args[idx] + "§c\" is already in the world group \"§9" + worldGroup.getName() + "§c.\"");
                            return true;
                        }
                    }
                    worlds[idx - 1] = args[idx];
                }
                WorldGroup wg = getWorldGroupByName(args[0]);
                if (isConflict(wg, worlds)) {
                    sender.sendMessage("§cThere are conflicting snapshots. There must be no conflicting snapshots to group worlds.");
                    sender.sendMessage("§cUse /allsnapshots to see which snapshots are conflicting.");
                    return true;
                }
                sender.sendMessage("§9" + wg.addWorlds(worlds) + "§a world(s) have been added to the world group \"§9" + args[0] + "§a.\"");
                return true;
            case "removefromworldgroup":
            case "rfwg":
            case "deletefromworldgroup":
            case "dfwg":
                if (args.length < 2) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (getWorldGroupByName(args[0]) == null) {
                    sender.sendMessage("§cThe world group \"§9" + args[0] + "§c\" doesn't exist!");
                    return true;
                }
                worlds = new String[args.length - 1];
                for (int idx = 1; idx < args.length; idx++) {
                    if (getServer().getWorld(args[idx]) == null) {
                        sender.sendMessage("§cThe world \"§9" + args[idx] + "§c\" doesn't exist!");
                        return true;
                    }
                    worlds[idx - 1] = args[idx];
                }
                ungroupWorlds(Arrays.asList(worlds));
                sender.sendMessage("§9" + getWorldGroupByName(args[0]).removeWorlds(worlds)
                        + "§a world(s) have been removed from the world group \"§9" + args[0] + "§a.\"");
                return true;
            case "deleteworldgroup":
            case "dwg":
                if (args.length != 1) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (getWorldGroupByName(args[0]) == null) {
                    sender.sendMessage("§cThe world group \"§9" + args[0] + "§c\" doesn't exist!");
                    return true;
                }
                wg = getWorldGroupByName(args[0]);
                for (User regUser : users) {
                    for (Snapshot snap : regUser.getSnapshots()) {
                        if (snap.getVisibility() == Visibility.GROUPED && wg.isMember(snap.getWorld())) {
                            snap.setVisibility(Visibility.PRIVATE);
                        }
                    }
                }
                ungroupWorlds(wg.getWorlds());
                worldGroups.remove(wg);
                sender.sendMessage("§aThe world group \"§9" + args[0] + "§a\" has been deleted.");
                return true;
            case "listworldgroups":
            case "lwg":
                if (args.length != 0) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                for (WorldGroup worldGroup : worldGroups) {
                    sender.sendMessage("§aThe world group \"§9" + worldGroup.getName() + "§a\" contains the worlds:");
                    for (String world : worldGroup.getWorlds()) {
                        sender.sendMessage("§2    " + world);
                    }
                }
                return true;
            case "movesnapshot":
            case "movesnap":
                if (user != null && player != null) {
                    if (args.length != 2) {
                        player.sendMessage("§cIncorrect amount of arguments!");
                        player.sendMessage("§c/movesnapshot <snapshot name> <destination world>");
                        return true;
                    }
                    if (getServer().getWorld(args[1]) == null) {
                        player.sendMessage("§cThe world \"§9" + args[1] + "§c\" doesn't exist!");
                        return true;
                    }
                    Snapshot snap = user.getSnapshot(args[0], player.getWorld().getName());
                    if (snap == null) {
                        player.sendMessage("§cYou do not have the snapshot \"§9" + args[0] + "§c\" or it isn't accessible from this world.");
                        return true;
                    }
                    if (snap.setWorld(args[1])) {
                        player.sendMessage("§aThe snapshot \"§9" + args[0] + "§a\" has been moved to the world \"§9" + args[1] + "§a.\"");
                    } else {
                        player.sendMessage("§aThe snapshot \"§9" + args[0] + "§a\" is already in the world \"§9" + args[1] + "§a.\"");
                    }
                } else {
                    if (args.length != 4) {
                        sender.sendMessage("§cIncorrect amount of arguments!");
                        sender.sendMessage("§c/movesnapshot <snapshot name> <destination world> <user name> <world of snapshot>");
                        return true;
                    }
                    if (!isUserRegistered(args[2])) {
                        sender.sendMessage("§cThe user \"§9" + args[2] + "§c\" does not exist!");
                        return true;
                    }
                    if (getServer().getWorld(args[1]) == null) {
                        sender.sendMessage("§cThe world \"§9" + args[1] + "§c\" does not exist!");
                        return true;
                    }
                    Snapshot snap = getUser(args[2]).getSnapshot(args[0], args[3]);
                    if (snap == null) {
                        sender.sendMessage("§9" + args[2] + "§c does not have the snapshot \"§9" + args[0] + "§c.\"");
                        return true;
                    }
                    if (snap.setWorld(args[1])) {
                        sender.sendMessage("§9" + args[2] + "§a's snapshot \"§9" + args[0] + "§a\" has been moved to the world \"§9" + args[1] + "§a.\"");
                    } else {
                        sender.sendMessage("§9" + args[2] + "§a's snapshot \"§9" + args[0] + "§a\" is already in the world \"§9" + args[1] + "§a.\"");
                    }
                }
                return true;
            case "snapshottype":
            case "snaptype":
                if (user != null && player != null) {
                    if (args.length != 2) {
                        player.sendMessage("§cIncorrect amount of arguments!");
                        player.sendMessage("§c/snapshottype <snapshot name> <global|private|grouped>");
                        return true;
                    }
                    Snapshot snap = user.getSnapshot(args[0], player.getWorld().getName());
                    if (snap == null) {
                        player.sendMessage("§cThe snapshot \"§9" + args[0] + "§c\" does not exist or isn't accessible from this world!");
                        return true;
                    }
                    if (args[0].equals("legit") || args[0].equals("admin") || args[0].equals("temp")) {
                        player.sendMessage("§cYou may not change the visibility of the legit, admin, or temp snapshots.");
                        return true;
                    }
                    try {
                        Visibility type = Visibility.valueOf(args[1].toUpperCase());
                        if (type == Visibility.GROUPED && !isInWorldGroup(snap.getWorld())) {
                            player.sendMessage("§cThe world \"§9" + snap.getWorld() + "§c\" must be in a world group to use visibility type §9GROUPED§c for the snapshot \"§9" + args[0] + "§c\"");
                            return true;
                        }
                        wg = getWorldGroupByWorld(snap.getWorld());
                        if (type == Visibility.GROUPED && isConflict(snap)) {
                            player.sendMessage("§cThere is a snapshot conflicting with the snapshot \"§9" + args[0] + "§c\" on another world in the world group \"§9" + wg.getName() + "§c.\"");
                            return true;
                        }
                        if (snap.setVisibility(type)) {
                            player.sendMessage("§aThe snapshot \"§9" + args[0] + "§a\" is now of visibility type §9" + type + "§a.");
                        } else {
                            player.sendMessage("§aThe snapshot \"§9" + args[0] + "§a\" is already of visibility type §9" + type + "§a.");
                        }
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§c\"§9" + args[1] + "§c\" is not a valid visibility!");
                        player.sendMessage("§c/snapshottype <snapshot name> <global|private|grouped>");
                    }
                } else {
                    if (args.length != 4) {
                        sender.sendMessage("§cIncorrect amount of arguments!");
                        sender.sendMessage("§c/snapshottype <snapshot name> <global|private|grouped> <user name> <world>");
                        return true;
                    }
                    user = getUser(args[2]);
                    if (user == null) {
                        sender.sendMessage("§cThe user \"§9" + args[2] + "§c\" doesn't exist or isn't registered!");
                        return true;
                    }
                    if (getServer().getWorld(args[3]) == null) {
                        sender.sendMessage("§cThe world \"§9" + args[3] + "§c\" doesn't exist!");
                        return true;
                    }
                    if (args[0].equals("legit") || args[0].equals("admin") || args[0].equals("temp")) {
                        sender.sendMessage("§cYou may not change the visibility of the legit, admin, or temp snapshots.");
                        return true;
                    }
                    Snapshot snap = user.getSnapshot(args[0], args[3]);
                    if (snap == null) {
                        sender.sendMessage("§a" + args[2] + "§c's snapshot \"§9" + args[0] + "§c\" does not exist in the world \"§9" + args[3] + "§c.\"");
                        return true;
                    }
                    try {
                        Visibility type = Visibility.valueOf(args[1]);
                        snap.setVisibility(type);
                        sender.sendMessage("§9" + args[2] + "§a's snapshot \"§9" + args[0] + "§a\" is now of visibility type §9" + args[1] + "§a.");
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("§c\"§9" + args[1] + "§c\" is not a valid visibility!");
                        sender.sendMessage("§c/snapshottype <snapshot name> <global|private|grouped>");
                    }
                }
                return true;
            case "admintoggle":
                if (args.length > 0) {
                    switch (args[0]) {
                        case "test":
                            throw new NullPointerException("Test Exception");
                        case "version":
                            sender.sendMessage(getDescription().getVersion());
                            break;
                        case "help":
                        default:
                            getServer().dispatchCommand(sender, "help admintoggle " + (args.length > 1 ? args[1] : ""));
                            break;
                    }
                } else {
                    getServer().dispatchCommand(sender, "help admintoggle");
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
     * Returns the WorldGroup associated with the given name.
     *
     * @param name The name of the WorldGroup to search for
     * @return The WorldGroup associated with the given name
     */
    public WorldGroup getWorldGroupByName(String name) {
        for (WorldGroup worldGroup : worldGroups) {
            if (name.equals(worldGroup.getName())) {
                return worldGroup;
            }
        }
        return null;
    }

    /**
     * Returns the WorldGroup containing the world with the given name.
     *
     * @param world The name of the world to search for
     * @return The WorldGroup containing the word with the given name
     */
    public WorldGroup getWorldGroupByWorld(String world) {
        for (WorldGroup worldGroup : worldGroups) {
            if (worldGroup.isMember(world)) {
                return worldGroup;
            }
        }
        return null;
    }

    /**
     * Returns true if a WorldGroup with the given name exists.
     *
     * @param name The name of the WorldGroup to check for existence
     * @return true if a WorldGroup with the given name exists
     */
    public boolean isWorldGroup(String name) {
        for (WorldGroup world : worldGroups) {
            if (name.equals(world.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the world with the given name is already in a
     * WorldGroup.
     *
     * @param world The name of the world to check for in WorldGroups
     * @return true if the world with the given name is already in a WorldGroup
     */
    public boolean isInWorldGroup(String world) {
        for (WorldGroup worldGroup : worldGroups) {
            if (worldGroup.isMember(world)) {
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
            snap = user.getSnapshot(name, player.getWorld().getName());
            if (snap == null) {
                return false;
            }
        } else {
            if (user.hasSnapshot(name, player.getWorld().getName())) {
                return false;
            }
            snap = user.addSnapshot(name);
            snap.setWorld(player.getWorld().getName());
            snap.setVisibility(Visibility.PRIVATE);
        }
        if (name.equals("legit") || name.equals("temp")) {
            snap.setVisibility(isInWorldGroup(snap.getWorld()) ? Visibility.GROUPED : Visibility.PRIVATE);
        }
        if (name.equals("admin")) {
            snap.setVisibility(Visibility.GLOBAL);
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
     * @param target The User to load the Snapshot from, null if no target
     * @return true if the Snapshot was loaded
     */
    public boolean loadSnapshot(User user, String name, Player player, User target) {
        Snapshot snap;
        if (target == null) {
            snap = user.getSnapshot(name, player.getWorld().getName());
        } else {
            if (getServer().getPlayer(target.getName()) == null) {
                return false;
            }
            String world = getServer().getPlayer(target.getName()).getWorld().getName();
            snap = target.getSnapshot(name, world);
        }
        if (snap == null) {
            return false;
        }
        user.logSnapshot(snap);
        player.getInventory().setContents(cloneItemStack(snap.getInv()));
        player.getInventory().setArmorContents(cloneItemStack(snap.getArmor()));
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
        player.getInventory().setContents(cloneItemStack(snap.getInv()));
        player.getInventory().setArmorContents(cloneItemStack(snap.getArmor()));
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

    private void setBalance(String name, double amount) {
        if (!vault) {
            return;
        }
        econ.withdrawPlayer(name, econ.getBalance(name));
        econ.depositPlayer(name, amount);
    }

    private static ItemStack[] cloneItemStack(ItemStack[] stack) {
        ItemStack[] inventory = new ItemStack[stack.length];
        for (int idx = 0; idx < stack.length; idx++) {
            if (stack[idx] != null) {
                inventory[idx] = stack[idx].clone();
            }
        }
        return inventory;
    }

    private boolean isConflict(Snapshot prospective) {
        WorldGroup wg = getWorldGroupByWorld(prospective.getWorld());
        User user = getUser(prospective.getUser());
        for (String world : wg.getWorlds()) {
            Snapshot test = user.getSnapshot(prospective.getName(), world);
            if (test != null && test != prospective) {
                return true;
            }
        }
        return false;
    }

    private boolean isConflict(WorldGroup wg, String[] worlds) {
        if (wg.isEmpty()) {
            return false;
        }
        boolean conflict = false;
        for (int idx = 0; idx < worlds.length; idx++) {
            for (String world : worlds) {
                for (User user : users) {
                    for (Snapshot snap : user.getSnapshots()) {
                        if (snap.getVisibility() == Visibility.GROUPED && wg.isMember(snap.getWorld()) && user.getSnapshot(snap.getName(), world) != null) {
                            conflict = true;
                        }
                    }
                }
                wg.addWorld(world);
            }
        }
        wg.removeWorlds(worlds);
        return conflict;
    }

    private void ungroupWorlds(List<String> worlds) {
        for (String world : worlds) {
            for (User user : users) {
                for (Snapshot snap : user.getSnapshots()) {
                    if (snap.getWorld().equals(world) && snap.getVisibility() == Visibility.GROUPED) {
                        snap.setVisibility(Visibility.PRIVATE);
                    }
                }
            }
        }
    }

    private void save(boolean backup) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(backup ? snapFileBackup : snapFile);
        } catch (IOException e) {
            getLogger().warning("Failed to open output stream!");
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
                        if (inv.hasItemMeta()) {
                            ItemMeta meta = inv.getItemMeta();
                            if (meta instanceof BookMeta) {
                                p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3 + "/MetaType", "Book");
                                String data = "";
                                for (int idx5 = 0; idx5 < meta.getLore().toArray().length; idx5++) {
                                    data += meta.getLore().toArray()[idx5];
                                }
                                p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3 + "/MetaData", data);
                            } else if (meta instanceof MapMeta) {
                                p.setProperty(user.getName() + "/" + snap.getName() + "/" + idx3 + "/MetaType", "Map");
                            }
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
                p.setProperty(user.getName() + "/GameMode/" + snap.getName(), String.valueOf(snap.getGameMode()));
                p.setProperty(user.getName() + "/Exp/" + snap.getName(), String.valueOf(snap.getExp()));
                p.setProperty(user.getName() + "/Level/" + snap.getName(), String.valueOf(snap.getLevel()));
                p.setProperty(user.getName() + "/Exhaustion/" + snap.getName(), String.valueOf(snap.getExhaustion()));
                p.setProperty(user.getName() + "/Food Level/" + snap.getName(), String.valueOf(snap.getFoodLevel()));
                p.setProperty(user.getName() + "/Saturation/" + snap.getName(), String.valueOf(snap.getSaturation()));
                p.setProperty(user.getName() + "/Balance/" + snap.getName(), String.valueOf(snap.getBalance()));
                p.setProperty(user.getName() + "/World/" + snap.getName(), snap.getWorld());
                p.setProperty(user.getName() + "/Visibility/" + snap.getName(), String.valueOf(snap.getVisibility()));
            }
        }
        try {
            p.store(fos, null);
        } catch (IOException e) {
            getLogger().warning("Failed to save snapshots.properties!");
        }
        try {
            fos.close();
        } catch (IOException e) {
            getLogger().warning("Error closing the output stream after saving snapshots! Data might not be saved.");
        }
        try {
            fos = new FileOutputStream(wgFile);
        } catch (FileNotFoundException e) {
            getLogger().warning("linkedworlds.properties file missing!");
            return;
        }
        p = new Properties();
        p.setProperty("World Group Count", String.valueOf(worldGroups.size()));
        for (int idx = 0; idx < worldGroups.size(); idx++) {
            p.setProperty(String.valueOf(idx), worldGroups.get(idx).getName());
            p.setProperty(worldGroups.get(idx).getName() + "/WorldCount", String.valueOf(worldGroups.get(idx).getWorlds().size()));
            for (int idx2 = 0; idx2 < worldGroups.get(idx).getWorlds().size(); idx2++) {
                p.setProperty(worldGroups.get(idx).getName() + "/" + idx2, worldGroups.get(idx).getWorlds().get(idx2));
            }
        }
        try {
            p.store(fos, null);
        } catch (IOException e) {
            getLogger().warning("Failed to save linkedworlds.properties!");
            return;
        }
        try {
            fos.close();
        } catch (IOException e) {
            getLogger().warning("Error closing output stream after saving worlds! Data might not be saved.");
        }
    }

    private void load() {
        if (!snapFile.exists() || !wgFile.exists()) {
            save(false);
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(snapFile);
        } catch (FileNotFoundException e) {
            getLogger().warning("snapshots.properties file missing!");
            return;
        }
        Properties p = new Properties();
        try {
            p.load(fis);
        } catch (IOException e) {
            getLogger().warning("Failed to open the input stream!");
            return;
        }
        int userCount;
        try {
            userCount = Integer.parseInt(p.getProperty("User Count"));
        } catch (NumberFormatException | NullPointerException e) {
            getLogger().log(Level.WARNING, "Error loading user count");
            loadedProperly = false;
            return;
        }
        for (int idx = 0; idx < userCount; idx++) {
            String userName = p.getProperty("User/" + idx);
            if (userName == null) {
                getLogger().log(Level.WARNING, "Error loading user {0}",
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
                getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot Count",
                        user.getName());
                loadedProperly = false;
                continue;
            }
            for (int idx2 = 0; idx2 < snapCount; idx2++) {
                ItemStack[] inv = new ItemStack[36];
                String name = p.getProperty(user.getName() + "/Snapshot/" + idx2);
                if (name == null) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot at {1}",
                            new Object[]{user.getName(), idx2});
                    loadedProperly = false;
                    continue;
                }
                for (int idx3 = 0; idx3 < 36; idx3++) {
                    String iType = p.getProperty(user.getName() + "/" + name + "/IType/" + idx3);
                    if (iType == null) {
                        getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Inv at {2}",
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
                        getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Inv at {2}",
                                new Object[]{user.getName(), name, idx3});
                        loadedProperly = false;
                        continue;
                    }
                    int enchantCount;
                    try {
                        enchantCount = Integer.parseInt(p.getProperty(user.getName() + "/" + name + "/IEnchant/" + idx3));
                    } catch (NullPointerException | NumberFormatException e) {
                        getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: CEInv at {2}",
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
                        } catch (NullPointerException | IllegalArgumentException e) {
                            getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: EInv at {2}",
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
                        getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Armor at {2}",
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
                        getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Armor at {2}",
                                new Object[]{user.getName(), name, idx3});
                        loadedProperly = false;
                        continue;
                    }
                    int enchantCount;
                    try {
                        enchantCount = Integer.parseInt(p.getProperty(user.getName() + "/" + name + "/AEnchant/" + idx3));
                    } catch (NullPointerException | NumberFormatException e) {
                        getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: CEArmor at {2}",
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
                        } catch (NullPointerException | IllegalArgumentException e) {
                            getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: EArmor at {2}",
                                    new Object[]{user.getName(), name, idx3});
                            loadedProperly = false;
                            continue;
                        }
                    }
                }
                GameMode gm;
                try {
                    gm = GameMode.valueOf(p.getProperty(user.getName() + "/GameMode/" + name).toUpperCase());
                } catch (NullPointerException | IllegalArgumentException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: GameMode, set to survival",
                            new Object[]{user.getName(), name});
                    gm = GameMode.SURVIVAL;
                    break;
                }
                if (gm == null) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: GameMode, set to survival",
                            new Object[]{user.getName(), name});
                    gm = GameMode.SURVIVAL;
                    break;
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
                String world = p.getProperty(user.getName() + "/World/" + name);
                if (world == null) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: World, set to \"world\"",
                            new Object[]{user.getName(), name});
                    world = "world";
                }
                Visibility type;
                try {
                    type = Visibility.valueOf(p.getProperty(user.getName() + "/Visibility/" + name).toUpperCase());
                } catch (NullPointerException | IllegalArgumentException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Visibility, set to private",
                            new Object[]{user.getName(), name});
                    type = Visibility.PRIVATE;
                }
                if (type == null) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: Visibility, set to private",
                            new Object[]{user.getName(), name});
                    type = Visibility.PRIVATE;
                }
                try {
                    Snapshot snap = new Snapshot(
                            user.getName(), name, inv, armor, exp, level, gm, ex, foodLevel, sat, balance, world, type);
                    user.addSnapshot(snap);
                } catch (NullPointerException e) {
                    getLogger().log(Level.WARNING, "Error loading {0}''s Snapshot {1}: ELEFS",
                            new Object[]{user.getName(), name});
                    loadedProperly = false;
                }
            }
        }
        try {
            fis.close();
        } catch (IOException e) {
            getLogger().warning("Error closing input stream after loading snapshots.");
        }
        if (!wgFile.exists()) {
            save(false);
        }
        try {
            fis = new FileInputStream(wgFile);
        } catch (FileNotFoundException e) {
            getLogger().warning("linkedworlds.properties file missing!");
            return;
        }
        p = new Properties();
        try {
            p.load(fis);
        } catch (IOException e) {
            getLogger().warning("Failed to open the input stream!");
            return;
        }
        int worldGroupCount = 0;
        try {
            worldGroupCount = Integer.parseInt(p.getProperty("World Group Count"));
        } catch (NullPointerException | NumberFormatException e) {
            getLogger().log(Level.WARNING, "Error loading world groups: World Group Count");
            return;
        }
        for (int idx = 0; idx < worldGroupCount; idx++) {
            WorldGroup worldGroup = new WorldGroup(p.getProperty(String.valueOf(idx)));
            worldGroups.add(worldGroup);
            int worldCount;
            try {
                worldCount = Integer.parseInt(p.getProperty(worldGroup.getName() + "/WorldCount"));
            } catch (NullPointerException | NumberFormatException e) {
                getLogger().log(Level.WARNING, "Error loading world group {0}: World Count",
                        worldGroup.getName());
                continue;
            }
            for (int idx2 = 0; idx2 < worldCount; idx2++) {
                try {
                    worldGroup.addWorld(p.getProperty(worldGroup.getName() + "/" + idx2));
                } catch (NullPointerException e) {
                    getLogger().log(Level.WARNING, "Error loading world group {0}: world at {1}",
                            new Object[]{worldGroup.getName(), idx2});
                }
            }
        }
        try {
            fis.close();
        } catch (IOException e) {
            getLogger().warning("Error closing input stream after loading worlds.");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        if (!isUserRegistered(evt.getPlayer().getName()) && (evt.getPlayer().hasPermission("admintoggle.*")
                || evt.getPlayer().hasPermission("admintoggle.basic"))) {
            users.add(new User(evt.getPlayer().getName()));
            evt.getPlayer().sendMessage("§aWelcome to Admin Toggle. For instructions on "
                    + "using this plugin you can view the read me file here: "
                    + "https://github.com/Blir/AdminToggle - I hope you find this "
                    + "plugin useful! :)");
        }
    }
}