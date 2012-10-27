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

The project on Dev Bukkit: TBA.

Blir's Sponsored Minecraft Server: http://minepop.servegame.com

Commands:

    AdminSwitch:

         This is the primary feature of the plugin. It allows the player to
         switch between the "admin" and "legit" snapshots. The first time you
         use the plugin, you are in "legit" mode. When you enable "admin" mode,
         your current set-up is saved as "legit" and the plugin attempts to
         load the "admin" snapshot. If the "admin" snapshot does not exist,
         your inventory is not changed when switching to "admin" mode nor is 
         the "admin" snapshot saved as "admin" upon switching back to "legit" 
         mode. You can create an admin inventory simply by entering "/snap 
         admin" (or "/osnap admin" to overwrite an existing snapshot). When you
         disable admin mode, the plugin will save your current set-up as "temp"
         to help prevent accidental item loss, and then load the "legit"
         snapshot.

        Usage: /AdminSwitch
        Aliases: asdf, adswitch

    NewSnapshot:

        Saves your current profile to RAM, with the name that you specify.
        All snapshots in RAM are automatically saved to a file.
        See SaveSnapshots for more details on saving.

        Usage: /NewSnapshot [Snapshot name]
        Aliases: newsnap

    OverwriteSnapshot:

        Overwrites the snapshot you specify with your current profile.

        Usage: /OverwriteSnapshot [Snapshot name]
        Aliases: osnap, overwritesnap

    SaveSnapshots:

        Saves all snapshots currently in RAM to the snapshots.properties file.
        Snapshots are loaded from this file when the plugin is loaded. Use of
        this command isn't normally necessary as the plugin does this
        automatically every time the server is restarted or shut down, unless
        there was a problem loading.

        Usage: /SaveSnapshots
        Aliases: savesnap, savesnaps

    LoadSnapshot:

        Loads the specified snapshot from RAM. If a snapshot doesn't exist with
        the name you supply, no snapshot will be loaded.

        Usage: /LoadSnapshot [Snapshot name]
        Aliases: loadsnap, lsnap

    MySnapshots:

        Lists snapshots saved for your user.

        Usage: /MySnapshots
        Aliases: mysnaps, mysnap

    AllSnapshots:

        Lists snapshots saved for all users.

        Usage: /AllSnapshots
        Aliases: allsnaps, allsnap

    DeleteSnapshot:

        Deletes the snapshot with the specified name if it exists.

        Usage: /DeleteSnapshot [Snapshot name]
        Aliases: dsnap, delsnap, deletesnap, removesnapshot, removesnap, rmsnap

    DeleteMySnapshots:

        Deletes all snapshots for your user. Use carefully! Snapshots are
        permanently lost. You must type "CONFIRM" in all capital letters to
        confirm this action. Likewise, there are no shorter aliases to prevent
        accidental loss of snapshots.

        Usage: /DeleteMySnapshots CONFIRM
        Aliases: None

    UndoSnapshot:

        Reverses the last snapshot load. An intuitive way to undo snapshots
        without having to use /LoadSnapshot.

        Usage: /UndoSnapshot
        Aliases: undosnap, undosnaps, usnap, usnaps

    AdminCheck:

        Returns your current admin mode setting. For the forgetful ones.

        Usage: /AdminCheck
        Aliases: adcheck, adc

Notes: None of the commands are case-sensitive unless otherwise specified.