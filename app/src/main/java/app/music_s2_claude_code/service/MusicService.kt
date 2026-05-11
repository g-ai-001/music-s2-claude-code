package app.music_s2_claude_code.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import app.music_s2_claude_code.R
import app.music_s2_claude_code.data.Song
import app.music_s2_claude_code.ui.MainActivity
import app.music_s2_claude_code.utils.Constants
import app.music_s2_claude_code.utils.LogUtils

class MusicService : MediaSessionService() {

    private val binder = LocalBinder()
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private var currentPlaylist: List<Song> = emptyList()
    private var currentIndex: Int = 0

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        LogUtils.i("MusicService onCreate")

        initializePlayer()
        createNotificationChannel()
    }

    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .build()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "音乐播放"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(Constants.MUSIC_PLAYBACK_CHANNEL_ID, name, importance).apply {
                description = "音乐播放控制"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        currentPlaylist = songs
        currentIndex = startIndex
        LogUtils.i("设置播放列表: ${songs.size} 首歌曲, 从第 $startIndex 首开始")
        playCurrentSong()
    }

    fun playCurrentSong() {
        if (currentPlaylist.isEmpty()) return
        val song = currentPlaylist[currentIndex]
        LogUtils.i("播放歌曲: ${song.title}")

        val mediaItem = androidx.media3.common.MediaItem.fromUri(song.path)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        startForeground(Constants.MUSIC_PLAYBACK_NOTIFICATION_ID, buildNotification(song))
    }

    fun playSong(index: Int) {
        if (index in currentPlaylist.indices) {
            currentIndex = index
            playCurrentSong()
        }
    }

    fun playPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        currentPlaylist.getOrNull(currentIndex)?.let {
            updateNotification(it)
        }
    }

    fun nextSong() {
        if (currentPlaylist.isEmpty()) return
        currentIndex = (currentIndex + 1) % currentPlaylist.size
        playCurrentSong()
    }

    fun previousSong() {
        if (currentPlaylist.isEmpty()) return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else currentPlaylist.size - 1
        playCurrentSong()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun getCurrentSong(): Song? = currentPlaylist.getOrNull(currentIndex)
    fun isPlaying(): Boolean = player.isPlaying
    fun getCurrentPosition(): Long = player.currentPosition
    fun getDuration(): Long = player.duration

    fun addPlayerListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    fun removePlayerListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    private fun buildNotification(song: Song): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, Constants.MUSIC_PLAYBACK_CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(song: Song) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(Constants.MUSIC_PLAYBACK_NOTIFICATION_ID, buildNotification(song))
    }

    override fun onBind(intent: Intent?): IBinder {
        return if (intent?.action == MediaSessionService.SERVICE_INTERFACE) {
            super.onBind(intent) ?: binder
        } else {
            binder
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        LogUtils.i("MusicService onDestroy")
        player.release()
        mediaSession.release()
        super.onDestroy()
    }
}
