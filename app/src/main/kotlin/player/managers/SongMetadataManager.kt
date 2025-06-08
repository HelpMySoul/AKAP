package player.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import broadcast.BroadcastManagerController
import player.interfaces.ISong

object SongMetadataManager {
    private const val PREF_NAME    = "song_metadata"
    private const val KEY_METADATA = "metadata"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveMetadata(context: Context, song: ISong) {
        val prefs         = getPreferences(context)
        val metadataJson  = prefs.getString(KEY_METADATA, "[]") ?: "[]"
        val metadataArray = JSONArray(metadataJson)

        for (i in 0 until metadataArray.length()) {
            val obj = metadataArray.getJSONObject(i)
            if (obj.getLong("id") == song.id) {
                metadataArray.remove(i)
                break
            }
        }

        val songData = JSONObject().apply {
            put("id",            song.id)
            put("title",         song.title)
            put("artist",        song.artist)
            put("localVolume",   song.localVolume)
            put("introDuration", song.introDuration)
            put("outroDuration", song.outroDuration)
        }
        metadataArray.put(songData)
        prefs.edit().putString(KEY_METADATA, metadataArray.toString()).commit()
    }

    fun loadMetadata(context: Context, song: ISong) {
        val prefs = getPreferences(context)
        val metadataJson = prefs.getString(KEY_METADATA, "[]") ?: "[]"
        val metadataArray = JSONArray(metadataJson)

        for (i in 0 until metadataArray.length()) {
            val obj = metadataArray.getJSONObject(i)
            if (obj.getLong("id") == song.id) {
                if (obj.has("title")) {
                    song.title = obj.getString("title")
                }
                if (obj.has("artist")) {
                    song.artist = obj.getString("artist")
                }
                if (obj.has("localVolume")) {
                    song.localVolume = obj.getInt("localVolume")
                }
                if (obj.has("introDuration")) {
                    song.introDuration = obj.getLong("introDuration")
                }
                if (obj.has("outroDuration")) {
                    song.outroDuration = obj.getLong("outroDuration")
                }
                break
            }
        }
    }

    fun removeMetadata(context: Context, songId: Long) {
        val prefs         = getPreferences(context)
        val metadataJson  = prefs.getString(KEY_METADATA, "[]") ?: "[]"
        val metadataArray = JSONArray(metadataJson)

        for (i in 0 until metadataArray.length()) {
            val obj = metadataArray.getJSONObject(i)
            if (obj.getLong("id") == songId) {
                metadataArray.remove(i)
                prefs.edit().putString(KEY_METADATA, metadataArray.toString()).commit()
                return
            }
        }
    }

    fun clearAllMetadata(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_METADATA)
        val success = editor.commit()

        if (success) {
            BroadcastManagerController(context).sendBroadcast("RESTART_APP")
        }
    }
}
