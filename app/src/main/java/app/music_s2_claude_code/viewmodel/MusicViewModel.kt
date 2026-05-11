package app.music_s2_claude_code.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import app.music_s2_claude_code.data.Song
import app.music_s2_claude_code.service.MusicService
import app.music_s2_claude_code.utils.LogUtils
import app.music_s2_claude_code.utils.MediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val _songs = MutableLiveData<List<Song>>(emptyList())
    val songs: LiveData<List<Song>> = _songs

    private val _currentSong = MutableLiveData<Song?>(null)
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData(0L)
    val currentPosition: LiveData<Long> = _currentPosition

    private val _duration = MutableLiveData(0L)
    val duration: LiveData<Long> = _duration

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            LogUtils.i("Service connected")
            val binder = service as MusicService.LocalBinder
            musicService = binder.getService()
            isBound = true

            musicService?.addPlayerListener(playerListener)
            updatePlayerState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            LogUtils.i("Service disconnected")
            musicService = null
            isBound = false
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayerState()
        }
    }

    fun bindService(context: Context) {
        LogUtils.i("Binding service...")
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        context.startService(intent)
    }

    fun unbindService(context: Context) {
        if (isBound) {
            musicService?.removePlayerListener(playerListener)
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    fun scanMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            val songList = withContext(Dispatchers.IO) {
                MediaScanner.scanLocalMusic(getApplication())
            }
            _songs.value = songList
            _isScanning.value = false
        }
    }

    fun playSong(songs: List<Song>, index: Int) {
        musicService?.setPlaylist(songs, index)
        _currentSong.value = songs.getOrNull(index)
    }

    fun playPause() {
        musicService?.playPause()
    }

    fun nextSong() {
        musicService?.nextSong()
    }

    fun previousSong() {
        musicService?.previousSong()
    }

    fun seekTo(position: Long) {
        musicService?.seekTo(position)
    }

    private fun updatePlayerState() {
        _currentSong.value = musicService?.getCurrentSong()
        _isPlaying.value = musicService?.isPlaying() ?: false
        _duration.value = musicService?.getDuration() ?: 0L
        _currentPosition.value = musicService?.getCurrentPosition() ?: 0L
    }

    fun updateProgress() {
        _currentPosition.value = musicService?.getCurrentPosition() ?: 0L
    }
}
