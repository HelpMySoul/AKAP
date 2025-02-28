package playlistMenu.services

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import playlistMenu.classes.Playlist
import playlistMenu.classes.Song
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.managers.SongManager
import playlistMenu.managers.SongMetadataManager

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
            val idColumn        = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn     = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn    = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn  = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn      = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val id          = it.getLong(idColumn)
                val title       = it.getString(titleColumn)
                val artist      = it.getString(artistColumn)
                val duration    = it.getLong(durationColumn)
                val path        = it.getString(pathColumn)

                val song = Song(
                    id          = id,
                    title       = title,
                    artist      = artist,
                    filePath    = path,
                    duration    = duration
                )

                SongMetadataManager.loadMetadata(context, song)
                songs.add(song)
            }
        }

        return songs
    }
}