package screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R
import playlistMenu.adapters.SongAdapter
import playlistMenu.controllers.BroadcastManagerController
import playlistMenu.controllers.PlaylistController
import playlistMenu.controllers.SongSearchController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.ISongPlayerListener
import playlistMenu.managers.GlobalManager

class CurrentPlaylist : Fragment() {
    private lateinit var songsRecyclerView:     RecyclerView
    private lateinit var songAdapter:           SongAdapter
    private lateinit var playlistController:    PlaylistController
    private lateinit var songSearchController:  SongSearchController
    private lateinit var searchText:            EditText
    private lateinit var playlistNameText:      TextView
    private var          playlist:              IPlaylist?              = null
    private var          songPlayerListener:    ISongPlayerListener?    = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_current_playlist, container, false)

        songsRecyclerView   = view.findViewById(R.id.songsRecyclerView)
        playlistNameText    = view.findViewById(R.id.playlistNameTextView)
        searchText          = view.findViewById(R.id.searchText)

        songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        playlistController      = PlaylistController(requireContext())
        songSearchController    = SongSearchController(requireContext(), playlistController)

        val playlistName = GlobalManager.getPlaylistName()
        playlist = playlistController.getPlaylist(playlistName)

        if (playlist != null) {
            playlistNameText.text       = playlist!!.name
            songAdapter                 = SongAdapter(playlist!!) { song -> playSong(song) }
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

        return view
    }

    private fun playSong(song: ISong) {
        playlist?.findSong(song)?.let {
            songAdapter.refresh()
            songPlayerListener?.updateUI(it, playlist!!)
        }
    }

    fun playNextSong() {
        songAdapter.refresh()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is ISongPlayerListener) {
            songPlayerListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()

        songPlayerListener = null
    }

    fun playFirstInPlaylist() {
        playlist?.getFirstSong()?.let {
            songAdapter.refresh()
            songPlayerListener?.updateUI(it, playlist!!)
        }
    }

    fun repeatSong() {
        songAdapter.refresh()
    }

    fun refresh() {
        playlistNameText.text       = playlist!!.name
        songAdapter                 = SongAdapter(playlist!!) { song -> playSong(song) }
        songsRecyclerView.adapter   = songAdapter
    }


}
