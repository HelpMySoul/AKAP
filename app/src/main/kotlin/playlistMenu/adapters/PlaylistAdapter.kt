package playlistMenu.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R
import kotlinx.coroutines.currentCoroutineContext
import playlistMenu.interfaces.IPlaylist

class PlaylistAdapter(
    private val playlists: List<IPlaylist>,
    private val onPlaylistClick: (IPlaylist) -> Unit) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistName: TextView = view.findViewById(R.id.playlist_name)
        val songCount: TextView = view.findViewById(R.id.song_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.playlistName.text = playlist.name
        val songCountText = holder.itemView.context.getString(R.string.song_S)
        holder.songCount.text = "$songCountText: ${playlist.songs.size}"

        holder.itemView.setOnClickListener { onPlaylistClick(playlist) }
    }

    override fun getItemCount(): Int{
        return playlists.size
    }
}
