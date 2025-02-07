package playlistMenu.managers

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import playlistMenu.interfaces.ISong

object SongMetadataManager {
    private const val PREF_NAME = "song_metadata"
    private const val KEY_METADATA = "metadata"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveMetadata(context: Context, song: ISong) {
        val prefs = getPreferences(context)
        val metadataJson = prefs.getString(KEY_METADATA, "[]") ?: "[]"
        val metadataArray = JSONArray(metadataJson)

        for (i in 0 until metadataArray.length()) {
            val obj = metadataArray.getJSONObject(i)
            if (obj.getLong("id") == song.id) {
                metadataArray.remove(i)
                break
            }
        }

        val songData = JSONObject().apply {
            put("id", song.id)
            put("localVolume", song.localVolume)
            put("introDuration", song.introDuration)
            put("outroDuration", song.outroDuration)
        }
        metadataArray.put(songData)

        prefs.edit().putString(KEY_METADATA, metadataArray.toString()).apply()
    }

    fun loadMetadata(context: Context, song: ISong) {
        val prefs = getPreferences(context)
        val metadataJson = prefs.getString(KEY_METADATA, "[]") ?: "[]"
        val metadataArray = JSONArray(metadataJson)

        for (i in 0 until metadataArray.length()) {
            val obj = metadataArray.getJSONObject(i)
            if (obj.getLong("id") == song.id) {
                song.localVolume = obj.getInt("localVolume")
                song.introDuration = obj.getLong("introDuration")
                song.outroDuration = obj.getLong("outroDuration")
                break
            }
        }
    }
}
