package app.music_s2_claude_code.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.music_s2_claude_code.R
import app.music_s2_claude_code.data.Song
import app.music_s2_claude_code.utils.formatDuration
import app.music_s2_claude_code.utils.loadAlbumArt

class SongAdapter(
    private val onItemClick: (Song, Int) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class SongViewHolder(
        itemView: View,
        private val onItemClick: (Song, Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val albumArt: ImageView = itemView.findViewById(R.id.albumArt)
        private val songTitle: TextView = itemView.findViewById(R.id.songTitle)
        private val songArtist: TextView = itemView.findViewById(R.id.songArtist)
        private val songDuration: TextView = itemView.findViewById(R.id.songDuration)

        fun bind(song: Song, position: Int) {
            songTitle.text = song.title
            songArtist.text = song.artist
            songDuration.text = song.duration.formatDuration()
            albumArt.loadAlbumArt(song.albumArtUri)

            itemView.setOnClickListener {
                onItemClick(song, position)
            }
        }
    }

    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}
