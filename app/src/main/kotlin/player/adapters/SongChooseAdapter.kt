package player.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R
import player.holders.SongHolder
import player.interfaces.IPlaylist
import player.interfaces.ISong

class SongChooseAdapter (
    private var playlist:    IPlaylist
                 ) : RecyclerView.Adapter<SongHolder>() {
    private val selectedSongs: MutableList<ISong> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongHolder(view)
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val song = playlist.songs[position]

        holder.titleText.text  = song.title
        holder.artistText.text = song.artist

        val selectedColor = ContextCompat.getColor(holder.itemView.context, R.color.colorSongSelected)
        val defaultColor  = ContextCompat.getColor(holder.itemView.context, R.color.colorSongDefault)

        val isSelected = selectedSongs.contains(song)
        holder.itemView.setBackgroundColor(if (isSelected) selectedColor else defaultColor)

        holder.itemView.setOnClickListener {
            if (selectedSongs.contains(song)) {
                selectedSongs.remove(song)
            } else {
                selectedSongs.add(song)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return playlist.songs.size
    }

    fun updatePlaylist(playlist: IPlaylist?) {
        if (playlist != null) {
            this.playlist = playlist
        }
        refresh()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }

    fun getSelected(): MutableList<ISong> {
        return selectedSongs
    }
}
