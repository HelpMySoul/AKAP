package screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var playlistController: PlaylistController
    private lateinit var playlistAdapter:    PlaylistAdapter

    private val playlists = mutableListOf<IPlaylist>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistController = PlaylistController(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPlaylists)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        playlistAdapter = PlaylistAdapter(playlists) { playlist ->
            openCurrentPlaylist(playlist)
        }

        recyclerView.adapter = playlistAdapter

        loadPlaylists()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPlaylists() {
        playlists.clear()
        playlists.addAll(playlistController.getAllPlaylists())
        playlistAdapter.notifyDataSetChanged()
    }

    private fun openCurrentPlaylist(playlist: IPlaylist) {
        GlobalManager.updatePlaylistName(playlist.name, requireContext())

        val currentPlaylistFragment = CurrentPlaylist().apply {
            arguments = Bundle().apply {
                putString("playlist_name", GlobalManager.getPlaylistName())
            }
        }

        MenuFragmentManager.openFragment(parentFragmentManager, R.id.songContainerFragment, GlobalManager.getPlaylistName(), { currentPlaylistFragment })
    }
}
