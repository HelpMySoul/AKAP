package screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.akap.R
import playlistMenu.adapters.PlaylistAdapter
import playlistMenu.controllers.PlaylistController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.managers.GlobalManager
import playlistMenu.managers.MenuFragmentManager

class Playlists : Fragment() {

    private lateinit var playlistController:    PlaylistController
    private lateinit var playlistAdapter:       PlaylistAdapter
    private lateinit var createPlaylistButton:  Button

    private val playlists = mutableListOf<IPlaylist>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        playlistController = PlaylistController(requireContext())

        val view = inflater.inflate(R.layout.fragment_playlists, container, false)

        playlistAdapter = PlaylistAdapter(playlists) { playlist ->
            openCurrentPlaylist(playlist)
        }

        createPlaylistButton = view.findViewById(R.id.createPlaylistButton)

        createPlaylistButton.setOnClickListener {
            playlistController.createPlaylist(GlobalManager.getPlaylistName() + " Сохраненный", playlistController.getPlaylist(GlobalManager.getPlaylistName()))
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPlaylists)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = playlistAdapter

        createPlaylistButton = view.findViewById(R.id.createPlaylistButton)

        loadPlaylists()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPlaylists() {
        playlists.clear()
        val allPlaylists = playlistController.getAllPlaylists()
        playlists.addAll(allPlaylists)
        playlistAdapter.notifyDataSetChanged()
    }

    private fun openCurrentPlaylist(playlist: IPlaylist) {
        val playlistName = GlobalManager.getPlaylistName()
        GlobalManager.updatePlaylistName(playlist.name, requireContext())

        val currentPlaylistFragment = CurrentPlaylist().apply {
            arguments = Bundle().apply {
                putString("playlist_name", playlistName)
            }
        }

        parentFragmentManager.let {
            MenuFragmentManager.openFragment(it, R.id.songContainerFragment, playlistName) { currentPlaylistFragment }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        playlists.clear()
    }
}
