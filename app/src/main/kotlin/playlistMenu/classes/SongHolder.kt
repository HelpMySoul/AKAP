package playlistMenu.classes

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R

class SongHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleText: TextView = itemView.findViewById(R.id.songTitleTextView)
    val artistText: TextView = itemView.findViewById(R.id.songArtistTextView)
}