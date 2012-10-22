|===============================ADMIN TOGGLE===============================|

Version 1.2.2 10/21/2012

By Blir

AdminToggle is a Minecraft plugin intended for server admins to quickly toggle between two or more player profiles. These profiles (or "snapshots") contain the players inventory, equipped items, health, hunger, game mode, and exp.

The source code on GitHub: https://github.com/Blir/AdminToggle

The project on Bukkit Dev: http://dev.bukkit.org/server-mods/creative-tools/

Blir's Sponsored Minecraft Server: http://minepop.servegame.com

Commands:

    AdminSwitch:

        The primary feature of the plugin, allows you to switch between whatever snapshots are saved as "admin" and "legit." Admin mode is disabled by default the first time you use the plugin. When you enable admin mode, your current set-up is saved as "legit" and the plugin attempts to load the Snapshot "admin." You can create an admin inventory simply by entering "/adminsaveram admin". When you disable admin mode, the plugin will save your current set-up as "temp" to prevent accidental item loss, and then load the snapshot "legit."

        Usage: /AdminSwitch
        Aliases: asdf, adswitch

    AdminSaveRam:

        Saves your current profile to RAM, with the name that you specify. If a snapshot with the name you specify already exists, and you didn't put overwrite, then nothing will happen. If you put overwrite and it finds that the snapshot with that name already exists it will replace it with your current profile.

        Usage: /AdminSaveRam [Snapshot name] [Overwrite]
        Aliases: adram

    AdminSaveFile:

        Saves all snapshots currently in RAM to the snapshots.properties file, the file that the plugin reads from upon being enabled to load snapshots and users. Use of this command isn't normally necessary as the plugin  does this automatically every time the server is restarted or shut down.

        Usage: /AdminSaveFile
        Aliases: adfile

    AdminLoad:

        Loads the specified snapshot from RAM. If a snapshot doesn't exist with the name you supply, no snapshot will be loaded.

        Usage: /AdminLoad [Snapshot name]
        Aliases: adload

    AdminListSnapshots:

        Lists all current snapshots saved to your User.

        Usage: /AdminListSnapshots
        Aliases: adlist

    AdminDelete:

        Deletes the snapshot with the specified name if it exists.

        Usage: /AdminDelete [Snapshot name]
        Aliases: addel

    AdminDeleteAll:

        Deletes all snapshots for your User. Use caution, snapshots are permanently lost. You must type confirm in all capital letters to con- firm this action. Likewise, there are no shorter aliases to prevent accidental loss of snapshots.

        Usage: /AdminDeleteAll CONFIRM
        Aliases: None

    AdminUndo:

        Reverses the last snapshot load. A convenience command, doesn't do anything that you can't do with /AdminLoad.

        Usage: /AdminUndo
        Aliases: adundo

    AdminCheck:
        Returns your current admin mode setting. For the forgetful ones.

        Usage: /AdminCheck
        Aliases: adcheck

Notes: None of the commands are case-sensitive unless otherwise specified.