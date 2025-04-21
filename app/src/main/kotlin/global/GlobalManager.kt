package global

import android.content.Context
import android.util.Log

object GlobalManager {
    private var playlistName: String = ""
    private var songID      : Long   = -1

    fun updatePlaylistName(newName: String) {
        playlistName = newName
    }

    fun getPlaylistName(): String {
        return playlistName
    }

    fun updateSongID(iD: Long, context: Context) {
        songID = iD

        Log.e("SongName", "${getSongID()} $context")
    }

    fun getSongID(): Long {
        return songID
    }
}
