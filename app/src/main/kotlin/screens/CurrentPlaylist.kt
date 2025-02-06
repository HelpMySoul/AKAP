package screens

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R
import playlistMenu.adapters.SongAdapter
import playlistMenu.controllers.PlaylistController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.SongPlayerListener
import playlistMenu.managers.GlobalManager

class CurrentPlaylist : Fragment() {

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var playlistController: PlaylistController
    private lateinit var playlistNameText: TextView
    private var playlist: IPlaylist? = null
    private var songPlayerListener: SongPlayerListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_current_playlist, container, false)

        songsRecyclerView = view.findViewById(R.id.songsRecyclerView)
        playlistNameText = view.findViewById(R.id.playlistNameTextView)
        songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        playlistController = PlaylistController(requireContext())

        val playlistName = GlobalManager.playlistName

        playlist = playlistController.getPlaylist(playlistName)

        if (playlist != null) {
            playlistNameText.text = playlist!!.name
            songAdapter = SongAdapter(playlist!!) { song -> playSong(song) }
            songsRecyclerView.adapter = songAdapter
        } else {
            playlistNameText.text = ""
        }

        return view
    }


    private fun playSong(song: ISong) {
        playlist?.findSong(song)?.let {
            songAdapter.refresh()
            songPlayerListener?.updateSong(it, playlist!!)
        }
    }

    fun playNextSong() {
        playlist?.getNext()?.let { playSong(it) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SongPlayerListener) {
            songPlayerListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        songPlayerListener = null
    }
}
