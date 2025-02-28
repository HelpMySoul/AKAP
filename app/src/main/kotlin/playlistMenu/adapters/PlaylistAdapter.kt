package playlistMenu.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R
import playlistMenu.holders.PlaylistViewHolder
import playlistMenu.interfaces.IPlaylist
import playlistMenu.managers.GlobalManager

class PlaylistAdapter(
    private val playlists:           List<IPlaylist>,
    private val onPlaylistClick:     (IPlaylist) -> Unit,
    private val onPlaylistLongClick: (IPlaylist) -> Boolean
                     ) : RecyclerView.Adapter<PlaylistViewHolder>() {

    private val selectedPlaylists = mutableListOf<IPlaylist>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]

        holder.playlistName.text = playlist.name

        val songCountText     = holder.itemView.context.getString(R.string.song_S)
        holder.songCount.text = "$songCountText: ${playlist.songs.size}"

        val isSelected = (selectedPlaylists.contains(playlist))

        holder.itemView.isSelected = isSelected

        val selectedColor = ContextCompat.getColor(holder.itemView.context, R.color.playlist_selected)
        val defaultColor  = ContextCompat.getColor(holder.itemView.context, R.color.playlist_default)

        holder.itemView.setBackgroundColor(if (isSelected) selectedColor else defaultColor)

        holder.itemView.setOnClickListener {
            if (selectedPlaylists.isNotEmpty()) {
                toggleSelection(playlist)
            } else {
                onPlaylistClick(playlist)
            }
        }

        holder.itemView.setOnLongClickListener {
            toggleSelection(playlist)
            onPlaylistLongClick(playlist)
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    private fun toggleSelection(playlist: IPlaylist) {
        if (selectedPlaylists.contains(playlist)) {
            selectedPlaylists.remove(playlist)
        } else {
            selectedPlaylists.add(playlist)
        }
        notifyDataSetChanged()
    }

    fun getSelectedPlaylists(): MutableList<IPlaylist> {
        return selectedPlaylists
    }

    fun clearSelection() {
        selectedPlaylists.clear()
        notifyDataSetChanged()
    }
}