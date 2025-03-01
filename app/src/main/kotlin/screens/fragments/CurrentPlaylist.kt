package screens.fragments

import android.annotation.SuppressLint
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import builders.EditPlaylistNameBuilder
import com.example.akap.R
import playlistMenu.adapters.SongAdapter
import broadcast.BroadcastManagerController
import playlistMenu.controllers.PlaylistController
import playlistMenu.controllers.SongSearchController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import global.GlobalManager

class CurrentPlaylist(
    private var onSongClick: ((ISong) -> Unit)? = null
) : Fragment() {
    private lateinit var songsRecyclerView:      RecyclerView
    private lateinit var songAdapter:            SongAdapter
    private lateinit var playlistController:     PlaylistController
    private lateinit var songSearchController:   SongSearchController
    private lateinit var searchText:             EditText
    private lateinit var playlistNameText:       TextView
    private lateinit var deletePlaylistsButton:  ImageButton
    private lateinit var editPlaylistNameButton: ImageButton
    private lateinit var addSongsButton:         ImageButton
    private var          playlist:               IPlaylist? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_current_playlist, container, false)

        songsRecyclerView      = view.findViewById(R.id.songsRecyclerView)
        playlistNameText       = view.findViewById(R.id.playlistNameTextView)
        searchText             = view.findViewById(R.id.searchText)
        deletePlaylistsButton  = view.findViewById(R.id.deletePlaylistButton)
        editPlaylistNameButton = view.findViewById(R.id.editPlaylistNameButton)
        addSongsButton         = view.findViewById(R.id.addSongsButton)

        songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        playlistController   = PlaylistController(requireContext())
        songSearchController = SongSearchController(requireContext(), playlistController)

        val playlistName = GlobalManager.getPlaylistName()
        playlist = playlistController.getPlaylist(playlistName)

        if (playlist == null) {
            playlistNameText.text = context?.getString(R.string.no_playlist) ?: ""
            return view
        }

        playlistNameText.text = playlist!!.name

        if (onSongClick == null) {
            onSongClick = { song -> playSong(song) }
        }

        songAdapter = SongAdapter(playlist!!) { song -> onSongClick?.invoke(song) }

        songsRecyclerView.adapter = songAdapter

        if (GlobalManager.getSongID() == (-1).toLong()) {
            val song = playlist!!.getFirstSong()

            context?.let { context ->
                if (song != null) {
                    GlobalManager.updateSongID(song.id, context)
                }
            }
        }
        playlist!!.getSongAt(playlist!!.getIndex()).let { song ->
            if (song != null) {
                songAdapter.setCurrentSong(song)
            }
        }

        Log.d("CurrentPlaylist", "${GlobalManager.getSongID()} ${GlobalManager.getSongID() != (-1).toLong()} ${(-1).toLong()} $context")

        BroadcastManagerController(requireContext()).sendBroadcast("SHOW_PLAYER")

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (playlist == null) {
                    return
                }

                val query = s?.toString() ?: ""
                playlist = songSearchController.search(query, playlist)

                updatePlaylist(playlistName)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        deletePlaylistsButton.setOnClickListener {
            playlist?.name?.let { name ->
                playlistController.deletePlaylist(name)
            }
            playlist = null
            refresh()
            context?.let { context ->
                BroadcastManagerController(context).sendBroadcast("HIDE_PLAYER")
                BroadcastManagerController(context).sendBroadcast("STOP_SONG")
                BroadcastManagerController(context).sendBroadcast("STOP_NOTIFICATION")
                BroadcastManagerController(context).sendBroadcast("CLOSE_PLAYLIST")
            }
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

                    GlobalManager.updatePlaylistName(newName)
                }
            ).built()
        }

        return view
    }

    private fun updatePlaylist(playlistName: String) {
        songAdapter.updatePlaylist(playlist)

        songsRecyclerView.adapter = songAdapter

        BroadcastManagerController(requireContext()).sendBroadcast("REFRESH_PLAYLIST")

        GlobalManager.updatePlaylistName(playlist?.name ?: playlistName)
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
        playlistNameText.text = playlist?.name ?: context?.getString(R.string.no_playlist) ?: ""

        if (onSongClick == null) {
            onSongClick = { song -> playSong(song) }
        }

        if (playlist == null) {
            songsRecyclerView.adapter = null
            return
        }

        songAdapter = SongAdapter(playlist!!) { song -> onSongClick?.invoke(song) }
        songsRecyclerView.adapter = songAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onSongClick = null
    }
}
