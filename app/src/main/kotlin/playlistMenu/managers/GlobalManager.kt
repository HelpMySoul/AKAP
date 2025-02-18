package playlistMenu.managers

import android.content.Context
import android.util.Log
import playlistMenu.controllers.BroadcastManagerController

object GlobalManager {
    private var playlistName: String    = ""
    private var songID      : Long      = -1

    fun updatePlaylistName(newName: String, context: Context) {
        playlistName = newName
    }

    fun getPlaylistName(): String {
        return playlistName
    }

    fun updateSongID(iD: Long, context: Context) {
        songID = iD

        Log.e("SongName", "${GlobalManager.getSongID()} $context")
    }

    fun getSongID(): Long {
        return songID
    }
}