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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import builders.SetNameBuilder
import com.example.akap.R
import broadcast.BroadcastManagerController
import player.controllers.PlaylistController
import player.controllers.SongSearchController
import player.interfaces.IPlaylist
import player.interfaces.ISong
import global.GlobalManager
import player.adapters.SongAdapter

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
    private lateinit var editLayout:             LinearLayout
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
        editLayout             = view.findViewById(R.id.editLayout)

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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (playlist == null) {
                    return
                }

                if (s?.toString() != "") {
                    playlistNameText.visibility = View.GONE
                }

                val query = s?.toString() ?: ""
                playlist = songSearchController.search(query, playlist)

                updatePlaylist(playlistName)
            }

            override fun afterTextChanged(s: Editable?) {
                if (searchText.text.toString() == "") {
                    playlistNameText.visibility = View.VISIBLE
                }
            }
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
            SetNameBuilder(
                context     = requireContext(),
                currentName = playlistNameText.text.toString(),
                onChange      = { newName ->
                    playlistNameText.text = newName

                    playlist?.let { playlist ->
                        playlistController.updatePlaylistName(playlist.name, newName)
                    }

                    songAdapter.notifyDataSetChanged()

                    GlobalManager.updatePlaylistName(newName)
                }
            ).built()
        }

        if (GlobalManager.getPlaylistName() == (context?.getString(R.string.all_songs)
                ?: "All songs")
        ) {
            editLayout.visibility = View.GONE
        }
        else {
            editLayout.visibility = View.VISIBLE
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

            context?.let { context ->
                GlobalManager.updateSongID(song.id, context)
                BroadcastManagerController(context).sendBroadcast("UPDATE_SONG")
                BroadcastManagerController(context).sendBroadcast("PLAY_SONG")
            }
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

    fun shufflePlaylist() {
        BroadcastManagerController(requireContext()).sendBroadcast("STOP_SONG")

        playlist?.shuffle()

        BroadcastManagerController(requireContext()).sendBroadcast("UPDATE_SONG")
    }
}
