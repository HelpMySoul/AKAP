package settings.player

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import global.GlobalManager
import player.managers.SongManager

object PlayerSettingsManager {

    private const val PREF_NAME = "player_settings"

    private const val KEY_SKIP_INTRO  = "skip_intro"
    private const val KEY_SKIP_OUTRO  = "skip_outro"
    private const val KEY_REPEAT_SONG = "repeat_song"

    private const val KEY_AUTO_OUTRO_SKIP    = "auto_outro_skip"
    private const val KEY_OUTRO_SKIP_PERCENT = "outro_skip_percent"

    private const val KEY_JUMP_TO_START         = "jump_to_start"
    private const val KEY_JUMP_TO_START_PERCENT = "jump_to_start_percent"

    private const val KEY_CURRENT_PLAYLIST = "current_playlist"
    private const val KEY_CURRENT_SONG     = "current_song"

    fun saveSettings(context: Context) {
        val prefs  = getPreferences(context)
        val editor = prefs.edit()

        editor.putBoolean(KEY_SKIP_INTRO,      SongManager.skipTheIntro)
        editor.putBoolean(KEY_SKIP_OUTRO,      SongManager.skipTheOutro)
        editor.putBoolean(KEY_REPEAT_SONG,     SongManager.isRepeating)
        editor.putBoolean(KEY_AUTO_OUTRO_SKIP, SongManager.autoOutroSkip)
        editor.putBoolean(KEY_JUMP_TO_START,   SongManager.jumpToStart)

        editor.putString(KEY_CURRENT_PLAYLIST, GlobalManager.getPlayedPlaylistName())

        editor.putLong(KEY_CURRENT_SONG, GlobalManager.getSongID())

        editor.putInt(KEY_OUTRO_SKIP_PERCENT,    GlobalManager.outroSkipPercent)
        editor.putInt(KEY_JUMP_TO_START_PERCENT, GlobalManager.jumpToStartPercent)

        editor.apply()

        Log.d("SettingsManager", "Save context:  $context ${SongManager.skipTheIntro} " +
                                                        "${SongManager.skipTheOutro} "          +
                                                        "${SongManager.isRepeating} "           +
                                                        "${GlobalManager.getDisplayedPlaylistName()} "   +
                                                        "${GlobalManager.getSongID()}")
    }

    fun loadSettings(context: Context) {
        val prefs = getPreferences(context)

        SongManager.skipTheIntro  = prefs.getBoolean(KEY_SKIP_INTRO,      false)
        SongManager.skipTheOutro  = prefs.getBoolean(KEY_SKIP_OUTRO,      false)
        SongManager.isRepeating   = prefs.getBoolean(KEY_REPEAT_SONG,     false)
        SongManager.autoOutroSkip = prefs.getBoolean(KEY_AUTO_OUTRO_SKIP, false)
        SongManager.jumpToStart   = prefs.getBoolean(KEY_JUMP_TO_START,   false)

        GlobalManager.outroSkipPercent   = prefs.getInt(KEY_OUTRO_SKIP_PERCENT,    100)
        GlobalManager.jumpToStartPercent = prefs.getInt(KEY_JUMP_TO_START_PERCENT, 10)

        GlobalManager.updatePlayedPlaylistName(prefs.getString(KEY_CURRENT_PLAYLIST,   "") ?: "")

        GlobalManager.updateSongID(prefs.getLong(KEY_CURRENT_SONG, -1), context)
        Log.d("SettingsManager", "Load context: $context ${SongManager.skipTheIntro} " +
                                                        "${SongManager.skipTheOutro} "          +
                                                        "${SongManager.isRepeating} "           +
                                                        "${GlobalManager.getDisplayedPlaylistName()} "   +
                                                        "${GlobalManager.getSongID()}")
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}