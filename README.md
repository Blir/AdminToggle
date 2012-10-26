|=================================ADMIN TOGGLE=================================|

Version 1.2.3 10/25/2012

By Blir

AdminToggle is a Minecraft plugin intended for server admins to quickly toggle
between two or more player profiles. These profiles (or "snapshots") contain the
players inventory, equipped items, health, hunger, game mode, and exp.

Downloads:
		v1.2.0:

https://dl.dropbox.com/u/103112496/plugins/AdminToggle/1.2.0/AdminToggle.jar

	v1.2.2:

https://dl.dropbox.com/u/103112496/plugins/AdminToggle/1.2.2/AdminToggle.jar

	v1.2.3:

https://dl.dropbox.com/u/103112496/plugins/AdminToggle/1.2.3/AdminToggle.jar

The project on Dev Bukkit: Not yet.

Blir's Sponsored Minecraft Server: http://minepop.servegame.com

Commands:

    AdminSwitch:

        The primary feature of the plugin, allows you to switch between whatever
        snapshots are saved as "admin" and "legit." Admin mode is disabled by
        default the first time you use the plugin. When you enable admin mode,
        your current set-up is saved as "legit" and the plugin attempts to load
        the Snapshot "admin." You can create an admin inventory simply by
        entering "/snap admin". When you disable admin mode, the plugin
        will save your current set-up as "temp" to help prevent accidental item 
        loss, and then load the snapshot "legit."

        Usage: /AdminSwitch
        Aliases: asdf, adswitch

    NewSnapshot:

        Saves your current profile to RAM, with the name that you specify.

        Usage: /NewSnapshot [Snapshot name]
        Aliases: snap

    OverwriteSnapshot:

        Overwrites the snapshot you specify with your current profile.

        Usage: /OverwriteSnapshot [Snapshot name]
        Aliases: osnap

    SaveSnapshots:

        Saves all snapshots currently in RAM to the snapshots.properties file,
        the file that the plugin reads from upon being enabled to load snapshots
        and users. Use of this command isn't normally necessary as the plugin
        does this automatically every time the server is restarted or shut down,
        unless there was a problem loading.

        Usage: /SaveSnapshots
        Aliases: savesnaps

    LoadSnapshot:

        Loads the specified snapshot from RAM. If a snapshot doesn't exist with
        the name you supply, no snapshot will be loaded.

        Usage: /LoadSnapshot [Snapshot name]
        Aliases: loadsnap, lsnap

    ListSnapshots:

        Lists all current snapshots saved to your User.

        Usage: /ListSnapshots
        Aliases: list, listsnaps, lsnaps

    ListAllSnapshots:

        Lists all current snapshots saved to all users.

        Usage: /ListAllSnapshots
        Aliases: listall, listallsnaps, lasnaps

    DeleteSnapshot:

        Deletes the snapshot with the specified name if it exists.

        Usage: /DeleteSnapshot [Snapshot name]
        Aliases: dsnap, delsnap

    DeleteAllSnapshots:

        Deletes all snapshots for your User. Use caution, snapshots are
        permanently lost. You must type confirm in all capital letters to
        confirm this action. Likewise, there are no shorter aliases to prevent
        accidental loss of snapshots.

        Usage: /DeleteAllSnapshots CONFIRM
        Aliases: None

    UndoSnapshot:

        Reverses the last snapshot load. A convenience command, doesn't do
        much that you can't do with /LoadSnapshot.

        Usage: /UndoSnapshot
        Aliases: usnap, undo

    AdminCheck:

        Returns your current admin mode setting. For the forgetful ones.

        Usage: /AdminCheck
        Aliases: adcheck, ad

Notes: None of the commands are case-sensitive unless otherwise specified.

Special thanks to LegendOfBrian for helping with the read me file. <3