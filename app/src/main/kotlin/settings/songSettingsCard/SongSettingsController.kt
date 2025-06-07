package settings.songSettingsCard

import android.content.Context
import builders.SetNameBuilder
import global.GlobalManager
import player.adapters.SongPlayerAdapter
import player.interfaces.ISong
import player.managers.PlaylistManager
import player.managers.SongMetadataManager

class SongSettingsController {
    companion object {
        fun changeSongName(context: Context, song: ISong, refresh: () -> Unit) {
            SetNameBuilder(
                context     = context,
                currentName = song.title,
                onChange    = { newName ->
                    song.title = newName
                    refresh.invoke()
                    SongMetadataManager.saveMetadata(context, song)
                }
            ).built()
        }

        fun changeAuthor(context: Context, song: ISong, refresh: () -> Unit) {
            SetNameBuilder(
                context     = context,
                currentName = song.artist,
                onChange    = { newName ->
                    song.artist = newName
                    refresh.invoke()
                    SongMetadataManager.saveMetadata(context, song)
                }
            ).built()
        }

        fun discardMetaData(context: Context, song: ISong) {
            SongMetadataManager.removeMetadata(context, song.id)
        }
    }
}