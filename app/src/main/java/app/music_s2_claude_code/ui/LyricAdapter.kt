package app.music_s2_claude_code.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.music_s2_claude_code.R
import app.music_s2_claude_code.data.LyricLine

class LyricAdapter : ListAdapter<LyricLine, LyricAdapter.LyricViewHolder>(LyricDiffCallback()) {

    private var currentLineIndex = 0

    fun setCurrentLine(index: Int) {
        val previousIndex = currentLineIndex
        currentLineIndex = index
        notifyItemChanged(previousIndex)
        notifyItemChanged(currentLineIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LyricViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lyric, parent, false)
        return LyricViewHolder(view)
    }

    override fun onBindViewHolder(holder: LyricViewHolder, position: Int) {
        holder.bind(getItem(position), position == currentLineIndex)
    }

    class LyricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lyricText: TextView = itemView.findViewById(R.id.lyricText)

        fun bind(lyricLine: LyricLine, isCurrent: Boolean) {
            lyricText.text = lyricLine.text
            lyricText.setTextColor(
                if (isCurrent) {
                    itemView.context.getColor(R.color.primary_text)
                } else {
                    itemView.context.getColor(R.color.secondary_text)
                }
            )
            lyricText.textSize = if (isCurrent) 20f else 16f
        }
    }

    class LyricDiffCallback : DiffUtil.ItemCallback<LyricLine>() {
        override fun areItemsTheSame(oldItem: LyricLine, newItem: LyricLine): Boolean {
            return oldItem.timeMs == newItem.timeMs
        }

        override fun areContentsTheSame(oldItem: LyricLine, newItem: LyricLine): Boolean {
            return oldItem == newItem
        }
    }
}
