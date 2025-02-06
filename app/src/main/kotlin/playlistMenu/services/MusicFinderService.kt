package playlistMenu.services

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import playlistMenu.classes.Playlist
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong

class MusicFinderService(private val context: Context) {

    fun findAllMusic(): MutableList<ISong> {

        val songs: MutableList<ISong> = mutableListOf()

        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor: Cursor? = context.contentResolver.query(
            musicUri,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val duration = it.getLong(durationColumn)
                val path = it.getString(pathColumn)

                val song = object : ISong {
                    override val id: Long = id
                    override val title: String = title
                    override val artist: String = artist
                    override var duration: Long = duration
                    override val filePath: String = path
                    override var localVolume: Int = 50
                    override val introDuration: Long = 0
                    override val outroDuration: Long = 0
                }
                songs.add(song)
            }
        }

        return songs
    }
}