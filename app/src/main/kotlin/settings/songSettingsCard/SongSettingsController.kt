package settings.songSettingsCard

import android.content.Context
import builders.SetNameBuilder
import com.example.akap.R
import global.GlobalManager
import player.adapters.SongPlayerAdapter
import player.controllers.PlaylistController
import player.controllers.SongSearchController
import player.interfaces.IPlaylist
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

        fun searchAuthor(context: Context, song: ISong, action: () -> Unit) {
            val playlistController = PlaylistController(context)

            val playlist = playlistController.getPlaylist(GlobalManager.getDisplayedPlaylistName())

            val songs: List<ISong>? = playlist?.songs?.filter { playlistSong -> playlistSong.artist == song.artist}

            val name = "${context.getString(R.string.found_by)} ${song.artist}"

            if (songs != null) {
                playlistController.createPlaylist(name, songs, true)
                GlobalManager.updateDisplayedPlaylistName(name)
            }

            action.invoke()
        }
    }
}