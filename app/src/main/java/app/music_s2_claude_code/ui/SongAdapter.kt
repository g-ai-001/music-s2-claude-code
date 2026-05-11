package app.music_s2_claude_code.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.music_s2_claude_code.R
import app.music_s2_claude_code.data.Song
import java.util.Locale
import java.util.concurrent.TimeUnit

class SongAdapter(
    private val onItemClick: (Song, Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var songs: List<Song> = emptyList()

    fun submitList(list: List<Song>) {
        songs = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], position)
    }

    override fun getItemCount(): Int = songs.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumArt: ImageView = itemView.findViewById(R.id.albumArt)
        private val songTitle: TextView = itemView.findViewById(R.id.songTitle)
        private val songArtist: TextView = itemView.findViewById(R.id.songArtist)
        private val songDuration: TextView = itemView.findViewById(R.id.songDuration)

        fun bind(song: Song, position: Int) {
            songTitle.text = song.title
            songArtist.text = song.artist
            songDuration.text = formatDuration(song.duration)

            try {
                song.albumArtUri?.let {
                    albumArt.setImageURI(Uri.parse(it))
                }
            } catch (e: Exception) {
                albumArt.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            itemView.setOnClickListener {
                onItemClick(song, position)
            }
        }

        private fun formatDuration(durationMs: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
