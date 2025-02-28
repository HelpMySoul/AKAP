package playlistMenu.classes

import org.json.JSONObject
import playlistMenu.interfaces.ISong

open class Song (
    override val id:            Long,
    override var duration:      Long,
    override var introDuration: Long = 0,
    override var outroDuration: Long = 0,

    override val title:         String,
    override val artist:        String,
    override val filePath:      String,

    override var localVolume:   Int = 75
) : ISong {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id",            id)
        json.put("duration",      duration)
        json.put("introDuration", introDuration)
        json.put("outroDuration", outroDuration)
        json.put("title",         title)
        json.put("artist",        artist)
        json.put("filePath",      filePath)
        json.put("localVolume",   localVolume)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): Song {
            return Song(
                id            = json.getLong("id"),
                duration      = json.getLong("duration"),
                introDuration = json.getLong("introDuration"),
                outroDuration = json.getLong("outroDuration"),

                title         = json.getString("title"),
                artist        = json.getString("artist"),
                filePath      = json.getString("filePath"),

                localVolume   = json.getInt("localVolume")
            )
        }
    }
}