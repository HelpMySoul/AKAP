package playlistMenu.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast

object PlayerSettingsManager {

    private const val PREF_NAME             = "player_settings"
    private const val KEY_SKIP_INTRO        = "skip_intro"
    private const val KEY_SKIP_OUTRO        = "skip_outro"
    private const val KEY_REPEAT_SONG       = "repeat_song"

    private const val KEY_CURRENT_PLAYLIST  = "current_playlist"
    private const val KEY_CURRENT_SONG      = "current_song"

    fun saveSettings(context: Context) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()

        editor.putBoolean(KEY_SKIP_INTRO,       SongManager.skipTheIntro)
        editor.putBoolean(KEY_SKIP_OUTRO,       SongManager.skipTheOutro)
        editor.putBoolean(KEY_REPEAT_SONG,      SongManager.isRepeating)

        editor.putString(KEY_CURRENT_PLAYLIST,  GlobalManager.getPlaylistName())

        editor.putLong(KEY_CURRENT_SONG,        GlobalManager.getSongID())

        editor.apply()

        Log.d("GlobalManager", "Save context:  $context ${SongManager.skipTheIntro} ${SongManager.skipTheOutro} ${SongManager.isRepeating} ${GlobalManager.getPlaylistName()} ${GlobalManager.getSongID()}")
    }

    fun loadSettings(context: Context) {
        val prefs = getPreferences(context)

        SongManager.skipTheIntro    = prefs.getBoolean(KEY_SKIP_INTRO,  false)
        SongManager.skipTheOutro    = prefs.getBoolean(KEY_SKIP_OUTRO,  false)
        SongManager.isRepeating     = prefs.getBoolean(KEY_REPEAT_SONG, false)

        GlobalManager.updatePlaylistName(prefs.getString(KEY_CURRENT_PLAYLIST,   "") ?: "", context)

        GlobalManager.updateSongID(prefs.getLong(KEY_CURRENT_SONG, -1), context)
        Log.d("GlobalManager", "Load context: $context ${SongManager.skipTheIntro} ${SongManager.skipTheOutro} ${SongManager.isRepeating} ${GlobalManager.getPlaylistName()} ${GlobalManager.getSongID()}")
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}