package screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import builders.EditPlaylistNameBuilder
import com.example.akap.R
import playlistMenu.adapters.SongAdapter
import playlistMenu.controllers.BroadcastManagerController
import playlistMenu.controllers.PlaylistController
import playlistMenu.controllers.SongSearchController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.managers.GlobalManager

class CurrentPlaylist(
    private var onSongClick: ((ISong) -> Unit)? = null
) : Fragment() {
    private lateinit var songsRecyclerView:         RecyclerView
    private lateinit var songAdapter:               SongAdapter
    private lateinit var playlistController:        PlaylistController
    private lateinit var songSearchController:      SongSearchController
    private lateinit var searchText:                EditText
    private lateinit var playlistNameText:          TextView
    private lateinit var deletePlaylistsButton:     ImageButton
    private lateinit var editPlaylistNameButton:    ImageButton
    private lateinit var addSongsButton:            ImageButton
    private var          playlist:                  IPlaylist? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_current_playlist, container, false)

        songsRecyclerView       = view.findViewById(R.id.songsRecyclerView)
        playlistNameText        = view.findViewById(R.id.playlistNameTextView)
        searchText              = view.findViewById(R.id.searchText)
        deletePlaylistsButton   = view.findViewById(R.id.deletePlaylistButton)
        editPlaylistNameButton  = view.findViewById(R.id.editPlaylistNameButton)
        addSongsButton          = view.findViewById(R.id.addSongsButton)

        songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        playlistController      = PlaylistController(requireContext())
        songSearchController    = SongSearchController(requireContext(), playlistController)

        val playlistName = GlobalManager.getPlaylistName()
        playlist = playlistController.getPlaylist(playlistName)

        if (playlist != null) {
            playlistNameText.text = playlist!!.name

            if (onSongClick == null) {
                onSongClick = { song -> playSong(song) }
            }
            songAdapter = SongAdapter(playlist!!) { song -> onSongClick?.invoke(song) }

            songsRecyclerView.adapter   = songAdapter

            Log.e("CurrentPlaylist", "${GlobalManager.getSongID()} ${GlobalManager.getSongID() != (-1).toLong()} ${(-1).toLong()} $context")

            if (GlobalManager.getSongID() != (-1).toLong()) {
                playlist!!.findSongByID(GlobalManager.getSongID())?.let { songAdapter.setCurrentSong(it) }
                BroadcastManagerController(requireContext()).sendBroadcast("SHOW_PLAYER")
            }

        } else {
            playlistNameText.text       = context?.getString(R.string.No_Playlist) ?: ""
            Log.e("CurrentPlaylist", "null")
        }

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query   = s?.toString() ?: ""
                playlist    = songSearchController.search(query, playlist)

                songAdapter.updatePlaylist(playlist)

                songsRecyclerView.adapter = songAdapter

                BroadcastManagerController(requireContext()).sendBroadcast("REFRESH_PLAYLIST")

                GlobalManager.updatePlaylistName(playlist?.name ?: playlistName, requireContext())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        deletePlaylistsButton.setOnClickListener {
            playlist?.name?.let { name -> playlistController.deletePlaylist(name) }
            refresh()
        }

        editPlaylistNameButton.setOnClickListener {
            EditPlaylistNameBuilder(
                context     = requireContext(),
                currentName = playlistNameText.text.toString(),
                onSave      = { newName ->
                    playlistNameText.text = newName

                    playlist?.let { playlist ->
                        playlistController.updatePlaylistName(playlist.name, newName)
                    }

                    songAdapter.notifyDataSetChanged()

                    GlobalManager.updatePlaylistName(newName, requireContext())
                }
            ).built()
        }

        return view
    }

    private fun playSong(song: ISong) {
        playlist?.findSong(song)?.let {
            songAdapter.refresh()

            context?.let { context -> GlobalManager.updateSongID(song.id, context) }

            context?.let { context -> BroadcastManagerController(context).sendBroadcast("UPDATE_SONG") }
        }
    }

    fun playNextSong() {
        songAdapter.refresh()
    }

    fun prevSong() {
        songAdapter.refresh()
    }

    fun repeatSong() {
        songAdapter.refresh()
    }

    fun refresh() {
        playlistNameText.text = playlist!!.name
        if (onSongClick == null) {
            onSongClick = { song -> playSong(song) }
        }
        songAdapter = SongAdapter(playlist!!) { song -> onSongClick?.invoke(song) }
        songsRecyclerView.adapter = songAdapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        onSongClick = null
    }
}
