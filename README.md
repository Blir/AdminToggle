|===============================ADMIN TOGGLE===============================|

Version 1.2.1 10/20/2012

By Blir

A Minecraft plugin intended for adminning by allowing you to quickly switch
between saved inventory set-ups (along with armor and more), which I have
dubbed with the name "Snapshots," which is how you will see them addressed
in this plugin.

The project on Bukkit Dev: http://dev.bukkit.org/server-mods/creative-tools/

Blir's Sponsored Minecraft Server: http://minepop.servegame.com

Download link: https://dl.dropbox.com/u/103112496/plugins/AdminToggle/1.2.3/AdminToggle.jar

Commands:

    AdminSwitch:

        The primary feature of the plugin, allows you to switch between whatever
        Snapshots are saved as "admin" and "legit." Admin mode is disabled by
        default the first time you use the plugin. When you enable admin mode,
        your current set-up is saved as "legit" and the plugin attempts to load
        the Snapshot "admin." You can create an admin inventory simply by enter-
        ing "/adminsaveram admin false". When you disable admin mode, the plugin
        will save your current set-up as "temp" to prevent accidental item loss,
        and then load the Snapshot "legit."

        Usage: /AdminSwitch
        Aliases: asdf, adswitch

    AdminSaveRam:

        Saves your current set-up to RAM, with the name that you specify. If a
        Snapshot with the name you specify already exists, and you didn't put
        true for overwrite, then nothing will happen. If you put true for over-
        write and it finds that the Snapshot with that name already exists it
        will replace it with your current set-up. If the Snapshot with that name
        doesn't exist then it doesn't matter whether you put true or false for
        overwrite.

        Usage: /AdminSaveRam [Snapshot name] [Overwrite:True/False]
        Aliases: adram

    AdminSaveFile:

        Saves all Snapshots currently in RAM to the snapshots.properties file,
        the file that the plugin reads from upon being enabled to load Snapshots
        and Users. Use of this command isn't normally necessary as the plugin 
        does this automatically every time the server is restarted or shut down.

        Usage: /AdminSaveFile
        Aliases: adfile

    AdminLoad:

        Loads the specified Snapshot from RAM. If a Snapshot doesn't exist with
        the name you supply, no Snapshot will be loaded.

        Usage: /AdminLoad [Snapshot name]
        Aliases: adload

    AdminListSnapshots:

        Lists all current Snapshots saved to your User.

        Usage: /AdminListSnapshots
        Aliases: adlist

    AdminDelete:

        Deletes the Snapshot with the specified name if it exists.

        Usage: /AdminDelete [Snapshot name]
        Aliases: addel

    AdminDeleteAll:

        Deletes all Snapshots for your User. Use caution, Snapshots are
        permanently lost. You must type confirm in all capital letters to con-
        firm this action. Likewise, there are no shorter aliases to prevent
        accidentall loss of Snapshots.

        Usage: /AdminDeleteAll CONFIRM
        Aliases: None

    AdminUndo:

        Undoes the last Snapshot load. A convenience command, doesn't do any-
        thing that you can't do with /AdminLoad.

        Usage: /AdminUndo
        Aliases: adundo

    AdminCheck:

        Returns your current admin mode setting. For the forgetful ones.

        Usage: /AdminCheck
        Aliases: adcheck

Notes: None of the commands are case-sensitive unless otherwise specified. Each
command has its own permission, denoted by "admintoggle.<permissionname>". To
enable all permissions, use "admintoggle.*".