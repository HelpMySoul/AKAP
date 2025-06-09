package global

import android.content.Context
import android.util.Log

object GlobalManager {
    private var displayedPlaylistName: String = ""
    private var playedPlaylistName:    String = ""
    private var songID:                Long   = -1
    var outroSkipPercent: Int = 100
    fun updateDisplayedPlaylistName(newName: String) {
        displayedPlaylistName = newName
    }
    fun updatePlayedPlaylistName(newName: String) {
        playedPlaylistName = newName
    }
    fun getDisplayedPlaylistName(): String {
        return displayedPlaylistName
    }
    fun getPlayedPlaylistName(): String {
        return playedPlaylistName
    }
    fun updateSongID(iD: Long, context: Context) {
        songID = iD
        Log.e("SongName", "${getSongID()} $context")
    }
    fun getSongID(): Long {
        return songID
    }
}
