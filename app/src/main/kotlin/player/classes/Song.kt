package player.classes

import org.json.JSONObject
import player.interfaces.ISong

class Song (
    override var id:            Long,
    override var duration:      Long,
    introDuration:              Long = 0,
    outroDuration:              Long = 0,

    override var title:         String,
    override var artist:        String,
    override val filePath:      String,

    localVolume:                Int = 75
) : ISong {

    override var introDuration: Long = introDuration
        set(value) {
            field = if (value in 0..< duration - outroDuration )
            {
                value
            }
            else if (value > duration)
            {
                duration
            }
            else
            {
                0
            }
        }

    override var outroDuration: Long = outroDuration
        set(value) {
            field = if (value in 0.. duration - introDuration )
            {
                value
            }
            else if (value > duration)
            {
                duration
            }
            else
            {
                0
            }
        }

    override var localVolume: Int = localVolume
        set(value) {
            field = if (value in 0.. 100 )
            {
                value
            }
            else if (value >= 100)
            {
                100
            }
            else
            {
                0
            }
        }

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