package player.services

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import player.classes.Song
import player.interfaces.ISong
import player.managers.PlaylistManager
import player.managers.SongMetadataManager
import kotlin.random.Random

class MusicFinderService(private val context: Context) {


    fun findSongById(id: Long): ISong {
        val song: ISong? = PlaylistManager.getAllSongsPlaylist().findSongByID(id)

        SongMetadataManager.loadMetadata(context, song as Song)

        return song
    }

    /// demonstration
    /*
    fun findAllMusic(): MutableList<ISong> {
        val songs: MutableList<ISong> = mutableListOf()

        for (i in 0..15) {
            val song = Song(
                id       = i.toLong(),
                title    = "Песня $i",
                artist   = "Автор $i",
                filePath = "",
                duration = (1000* Random.nextInt(150, 250)).toLong()
            )
            SongMetadataManager.loadMetadata(context, song)
            songs.add(song)
        }

        return songs
    }
    */


    /// functional
    fun findAllMusic(): MutableList<ISong> {

        val songs: MutableList<ISong> = mutableListOf()

        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED

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
            val idColumn        = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn     = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn    = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn  = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn      = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id        = it.getLong(idColumn)
                val title     = it.getString(titleColumn)
                val artist    = it.getString(artistColumn)
                val duration  = it.getLong(durationColumn)
                val path      = it.getString(pathColumn)
                val dateAdded = it.getLong(dateAddedColumn)

                Log.e("MFS", "$dateAdded")

                val song = Song(
                    id        = id,
                    title     = title,
                    artist    = artist,
                    filePath  = path,
                    duration  = duration,
                    dateAdded = dateAdded
                )

                SongMetadataManager.loadMetadata(context, song)
                songs.add(song)
            }
        }
        return songs
    }
}