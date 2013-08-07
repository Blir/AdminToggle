package com.minepop.servegame.admintoggle;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.minepop.servegame.admintoggle.Snapshot.Visibility;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Blir
 * @version 2.0.0 Beta
 * @since 30 July 2013
 */
public class Admin extends JavaPlugin {

    protected enum Action {

        ADMINSWITCH, NEWSNAPSHOT, OVERWRITESNAPSHOT, LOADSNAPSHOT,
        LOADOTHERSNAPSHOT, LISTSNAPSHOTS, DELETESNAPSHOT, SAVESNAPSHOTS,
        ADMINCHECK, UNDOSNAPSHOT, DELETEMYSNAPSHOTS, ALLSNAPSHOTS,
        CREATEWORLDGROUP, ADDTOWORLDGROUP, REMOVEFROMWORLDGROUP,
        DELETEWORLDGROUP, LISTWORLDGROUPS, MOVESNAPSHOT, SNAPSHOTTYPE,
        ADMINTOGGLE, TEST, VERSION, HELP
    }
    protected final ArrayList<User> users = new ArrayList<User>(0);
    protected final ArrayList<WorldGroup> worldGroups = new ArrayList<WorldGroup>(0);
    private final int README_VERSION = 5;
    private boolean vault;
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
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent evt) {
                if (!isUser(evt.getPlayer().getName()) && (evt.getPlayer().hasPermission("admintoggle.*")
                        || evt.getPlayer().hasPermission("admintoggle.basic"))) {
                    users.add(new User(evt.getPlayer().getName()));
                    evt.getPlayer().sendMessage("§aWelcome to Admin Toggle. For instructions on "
                            + "using this plugin you can view the read me file here: "
                            + "https://github.com/Blir/AdminToggle - I hope you find this "
                            + "plugin useful! :)");
                }
            }
        }, this);
        if (!getDataFolder().isDirectory()) {
            getDataFolder().mkdirs();
        }
        User.setPlugin(this);
        getDataFolder().mkdirs();
        load();
        if (!isWorldGroup("Default") && getConfig().getBoolean("worldgroups.makedefault") && !isInWorldGroup("world") && !isInWorldGroup("world_nether") && !isInWorldGroup("world_the_end")) {
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
        save(false);
        if (getConfig().getBoolean("snapshots.backup")) {
            save(true);
        }
        getConfig().set("snapshots.savebalance", getConfig().getBoolean("snapshots.savebalance"));
        getConfig().set("snapshots.savehunger", getConfig().getBoolean("snapshots.savehunger"));
        getConfig().set("snapshots.savegamemode", getConfig().getBoolean("snapshots.savegamemode"));
        getConfig().set("snapshots.saveexp", getConfig().getBoolean("snapshots.saveexp"));
        getConfig().set("snapshots.backup", getConfig().getBoolean("snapshots.backup"));
        getConfig().set("worldgroups.makedefault", getConfig().getBoolean("worldgroups.makedefault"));
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

        switch (Action.valueOf(cmd.getName().toUpperCase())) {
            case ADMINSWITCH:
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 0) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (user.isAdmin()) {
                    saveSnapshot(user, "temp", player, user.hasSnapshot("temp", player.getWorld().getName()));
                    if (loadSnapshot(user, "legit", player, null)) {
                        player.sendMessage("§aSnapshot \"§9legit§a\" loaded.");
                    } else {
                        player.sendMessage("§cThe snapshot \"§9legit§c\" doesn't exist or isn't accessible from this world!");
                    }
                } else {
                    saveSnapshot(user, "legit", player, user.hasSnapshot("legit", player.getWorld().getName()));
                    if (loadSnapshot(user, "admin", player, null)) {
                        player.sendMessage("§aSnapshot \"§9admin§a\" loaded.");
                    } else {
                        player.sendMessage("§cThe snapshot \"§9admin§c\" doesn't exist or isn't accessible from this world!");
                    }
                }
                player.sendMessage("§aAdmin mode §9" + (user.invertAdminMode() ? "enabled" : "disabled") + "§a.");
                return true;
            case NEWSNAPSHOT:
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
            case OVERWRITESNAPSHOT:
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
            case LOADSNAPSHOT:
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
            case LOADOTHERSNAPSHOT:
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                } else if (args.length != 2) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (isUser(args[0])) {
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
            case LISTSNAPSHOTS:
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
                    if (isUser(args[0])) {
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
            case DELETESNAPSHOT:
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
                    if (isUser(args[1])) {
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
            case SAVESNAPSHOTS:
                if (args.length != 0) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                save(false);
                sender.sendMessage("§aSave complete.");
                return true;
            case ADMINCHECK:
                if (user != null && player != null) {
                    if (args.length != 0) {
                        player.sendMessage("§cIncorrect amount of arguments!");
                        player.sendMessage("§c/admincheck");
                        return true;
                    }
                    player.sendMessage("§aAdmin mode is §9" + (user.isAdmin() ? "enabled" : "disabled") + "§a.");
                } else {
                    if (args.length != 1) {
                        sender.sendMessage("§cIncorrect amount of arguments!");
                        sender.sendMessage("§c/admincheck <user name>");
                        return true;
                    }
                    if (isUser(args[0])) {
                        sender.sendMessage("§aAdmin mode is §9" + (getUser(args[0]).isAdmin() ? "enabled" : "disabled") + "§a for \"§9" + args[0] + "§a.\"");
                    } else {
                        if (getServer().getPlayer(args[0]) == null) {
                            sender.sendMessage("§cThe user \"§9" + args[0] + "§c\" doesn't exist.");
                        } else {
                            sender.sendMessage("§cThe user \"§9" + args[0] + "§c\" isn't registered.");
                        }
                    }
                }
                return true;
            case UNDOSNAPSHOT:
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
            case DELETEMYSNAPSHOTS:
                if (user == null || player == null) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return true;
                }
                if (args.length != 1) {
                    player.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (!args[0].equals("CONFIRM")) {
                    player.sendMessage("§cYou must enter \"§9/deletemysnapshots CONFIRM§f\" to delete your snapshots.");
                    return true;
                }
                user.clearSnapshots();
                player.sendMessage("§aSnapshots deleted.");
                return true;
            case ALLSNAPSHOTS:
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
            case CREATEWORLDGROUP:
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
            case ADDTOWORLDGROUP:
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
            case REMOVEFROMWORLDGROUP:
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
            case DELETEWORLDGROUP:
                if (args.length != 1) {
                    sender.sendMessage("§cIncorrect amount of arguments!");
                    return false;
                }
                if (getWorldGroupByName(args[0]) == null) {
                    sender.sendMessage("§cThe world group \"§9" + args[0] + "§c\" doesn't exist!");
                    return true;
                }
                wg = getWorldGroupByName(args[0]);
                ungroupWorlds(wg.getWorlds());
                worldGroups.remove(wg);
                sender.sendMessage("§aThe world group \"§9" + args[0] + "§a\" has been deleted.");
                return true;
            case LISTWORLDGROUPS:
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
            case MOVESNAPSHOT:
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
                    if (!isUser(args[2])) {
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
            case SNAPSHOTTYPE:
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
            case ADMINTOGGLE:
                if (args.length > 0) {
                    switch (Action.valueOf(args[0].toUpperCase())) {
                        case TEST:
                            throw new NullPointerException("Test Exception");
                        case VERSION:
                            sender.sendMessage("§aVersion: §9" + getDescription().getVersion());
                            break;
                        case HELP:
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
    public boolean isUser(String name) {
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
        loadSnapshot(player, snap);
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
        loadSnapshot(player, snap);
        return true;
    }

    public void loadSnapshot(Player player, Snapshot snap) {
        player.getInventory().setContents(cloneItemStack(snap.getInv()));
        player.getInventory().setArmorContents(cloneItemStack(snap.getArmor()));
        if (getConfig().getBoolean("snapshots.savegamemode")) {
            player.setGameMode(snap.getGameMode());
        }
        if (getConfig().getBoolean("snapshots.saveexp")) {
            player.setExp(snap.getExp());
            player.setLevel(snap.getLevel());
        }
        if (getConfig().getBoolean("snapshots.savehunger")) {
            player.setExhaustion(snap.getExhaustion());
            player.setFoodLevel(snap.getFoodLevel());
            player.setSaturation(snap.getSaturation());
        }
        if (getConfig().getBoolean("snapshots.savebalance")) {
            setBalance(player.getName(), snap.getBalance());
        }
    }

    private void setBalance(String name, double amount) {
        if (!vault) {
            return;
        }
        econ.withdrawPlayer(name, econ.getBalance(name));
        econ.depositPlayer(name, amount);
    }

    protected static ItemStack[] cloneItemStack(ItemStack[] stack) {
        ItemStack[] inventory = new ItemStack[stack.length];
        for (int idx = 0; idx < stack.length; idx++) {
            if (stack[idx] != null) {
                inventory[idx] = stack[idx].clone();
            }
        }
        return inventory;
    }

    protected boolean isConflict(Snapshot prospective) {
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

    protected boolean isConflict(WorldGroup wg, String[] worlds) {
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

    protected void ungroupWorlds(List<String> worlds) {
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
        YamlConfiguration out = new YamlConfiguration();
        try {
            out.set("user.count", users.size());
            for (int idx = 0; idx < users.size(); idx++) {
                User user = users.get(idx);
                out.set("user." + idx + ".name", user.getName());
                out.set("user." + idx + ".adminmode", user.isAdmin());
                out.set("user." + idx + ".snapshot.count", user.getSnapshots().size());
                for (int idx2 = 0; idx2 < user.getSnapshots().size(); idx2++) {
                    Snapshot snap = user.getSnapshots().get(idx2);
                    out.set("user." + idx + ".snapshot." + idx2 + ".name", snap.getName());
                    out.set("user." + idx + ".snapshot." + idx2 + ".inv", snap.getInv());
                    out.set("user." + idx + ".snapshot." + idx2 + ".armor", snap.getArmor());
                    out.set("user." + idx + ".snapshot." + idx2 + ".exp", snap.getExp());
                    out.set("user." + idx + ".snapshot." + idx2 + ".level", snap.getLevel());
                    out.set("user." + idx + ".snapshot." + idx2 + ".gamemode", String.valueOf(snap.getGameMode()));
                    out.set("user." + idx + ".snapshot." + idx2 + ".exhaustion", snap.getExhaustion());
                    out.set("user." + idx + ".snapshot." + idx2 + ".foodlevel", snap.getFoodLevel());
                    out.set("user." + idx + ".snapshot." + idx2 + ".saturation", snap.getSaturation());
                    out.set("user." + idx + ".snapshot." + idx2 + ".balance", snap.getBalance());
                    out.set("user." + idx + ".snapshot." + idx2 + ".world", snap.getWorld());
                    out.set("user." + idx + ".snapshot." + idx2 + ".visibility", String.valueOf(snap.getVisibility()));
                }
            }
            out.set("worldgroup.count", worldGroups.size());
            for (int idx = 0; idx < worldGroups.size(); idx++) {
                WorldGroup wg = worldGroups.get(idx);
                out.set("worldgroup." + idx + ".name", wg.getName());
                out.set("worldgroup." + idx + ".worldcount", wg.getWorlds().size());
                for (int idx2 = 0; idx2 < wg.getWorlds().size(); idx2++) {
                    out.set("worldgroup." + idx + ".world." + idx2, wg.getWorlds().get(idx2));
                }
            }
            out.save(new File(getDataFolder(), "data" + (backup ? " - " + java.text.DateFormat.getDateInstance().format(Calendar.getInstance().getTime()) : "") + ".yml"));
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Error loading data.", ex);
        }
    }

    private void load() {
        YamlConfiguration in = new YamlConfiguration();
        try {
            in.load(new File(getDataFolder(), "data.yml"));
            int userCount = in.getInt("user.count");
            for (int idx = 0; idx < userCount; idx++) {
                User user = new User(in.getString("user." + idx + ".name"));
                user.setAdminMode(in.getBoolean("user." + idx + ".adminmode"));
                int snapCount = in.getInt("user." + idx + ".snapshot.count");
                for (int idx2 = 0; idx2 < snapCount; idx2++) {
                    user.addSnapshot(new Snapshot(user.getName(),
                            in.getString("user." + idx + ".snapshot." + idx2 + ".name"),
                            ((ArrayList<ItemStack>) in.get("user." + idx + ".snapshot." + idx2 + ".inv")).toArray(new ItemStack[36]),
                            ((ArrayList<ItemStack>) in.get("user." + idx + ".snapshot." + idx2 + ".armor")).toArray(new ItemStack[4]),
                            (float) in.getDouble("user." + idx + ".snapshot." + idx2 + ".exp"),
                            in.getInt("user." + idx + ".snapshot." + idx2 + ".level"),
                            GameMode.valueOf(in.getString("user." + idx + ".snapshot." + idx2 + ".gamemode")),
                            (float) in.getDouble("user." + idx + ".snapshot." + idx2 + ".exhaustion"),
                            in.getInt("user." + idx + ".snapshot." + idx2 + ".foodlevel"),
                            (float) in.getDouble("user." + idx + ".snapshot." + idx2 + ".saturation"),
                            (float) in.getDouble("user." + idx + ".snapshot." + idx2 + ".balance"),
                            in.getString("user." + idx + ".snapshot." + idx2 + ".world"),
                            Visibility.valueOf(in.getString("user." + idx + ".snapshot." + idx2 + ".visibility"))));
                }
                users.add(user);
            }
            int worldGroupCount = in.getInt("worldgroup.count");
            for (int idx = 0; idx < worldGroupCount; idx++) {
                String[] worlds = new String[in.getInt("worldgroup." + idx + ".worldcount")];
                for (int idx2 = 0; idx2 < worlds.length; idx2++) {
                    worlds[idx2] = in.getString("worldgroup." + idx + ".world." + idx2);
                }
                worldGroups.add(new WorldGroup(in.getString("worldgroup." + idx + ".name"), worlds));
            }
        } catch (FileNotFoundException ex) {
            getLogger().warning("data.yml file missing.");
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Error loading data.", ex);
        } catch (InvalidConfigurationException ex) {
            getLogger().log(Level.WARNING, "Error loading data.", ex);
        }
    }
}