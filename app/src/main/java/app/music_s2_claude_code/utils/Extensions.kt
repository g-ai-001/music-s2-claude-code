package app.music_s2_claude_code.utils

import android.net.Uri
import android.widget.ImageView
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Long.formatDuration(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

fun ImageView.loadAlbumArt(albumArtUri: String?) {
    if (albumArtUri.isNullOrEmpty()) {
        setImageResource(android.R.drawable.ic_menu_gallery)
        return
    }
    try {
        setImageURI(Uri.parse(albumArtUri))
    } catch (e: Exception) {
        setImageResource(android.R.drawable.ic_menu_gallery)
    }
}
