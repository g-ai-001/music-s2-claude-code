package app.music_s2_claude_code.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.music_s2_claude_code.R
import app.music_s2_claude_code.databinding.ActivityPlayerBinding
import app.music_s2_claude_code.utils.Constants
import app.music_s2_claude_code.utils.LogUtils
import app.music_s2_claude_code.utils.formatTime
import app.music_s2_claude_code.utils.loadAlbumArt
import app.music_s2_claude_code.viewmodel.MusicViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: MusicViewModel by viewModels()
    private lateinit var lyricAdapter: LyricAdapter
    private var progressJob: Job? = null
    private var isTrackingTouch = false
    private var lastScrollPosition = -1
    private val scrollHandler = Handler(Looper.getMainLooper())
    private var pendingScrollRunnable: Runnable? = null
    private val SCROLL_DEBOUNCE_MS = 150L

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PlayerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLyricRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.bindService(this)
    }

    private fun setupLyricRecyclerView() {
        lyricAdapter = LyricAdapter()
        binding.lyricRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlayerActivity)
            adapter = lyricAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.currentSong.observe(this) { song ->
            song?.let {
                binding.songTitle.text = it.title
                binding.songArtist.text = it.artist
                binding.coverSongTitle.text = it.title
                binding.coverSongArtist.text = it.artist
                binding.albumArt.loadAlbumArt(it.albumArtUri)
            }
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            binding.playPauseBtn.setImageResource(icon)
        }

        viewModel.currentPosition.observe(this) { position ->
            if (!isTrackingTouch) {
                binding.seekBar.progress = position.toInt()
                binding.currentTime.text = position.formatTime()
            }
        }

        viewModel.duration.observe(this) { duration ->
            binding.seekBar.max = duration.toInt()
            binding.totalTime.text = duration.formatTime()
        }

        viewModel.lyrics.observe(this) { lyrics ->
            lyricAdapter.submitList(lyrics)
            if (lyrics.isEmpty()) {
                binding.lyricPreview.text = getString(R.string.no_lyrics)
            }
        }

        viewModel.currentLyricIndex.observe(this) { index ->
            lyricAdapter.setCurrentLine(index)
            if (index >= 0 && index < lyricAdapter.itemCount) {
                viewModel.lyrics.value?.getOrNull(index)?.let {
                    binding.lyricPreview.text = it.text
                }
                smoothScrollToLyric(index)
            }
        }

        viewModel.isLyricMode.observe(this) { isLyric ->
            if (isLyric) {
                binding.viewFlipper.displayedChild = 1
                binding.modeSwitchBtn.setImageResource(R.drawable.ic_cover_mode)
            } else {
                binding.viewFlipper.displayedChild = 0
                binding.modeSwitchBtn.setImageResource(R.drawable.ic_lyric_mode)
            }
        }
    }

    private fun smoothScrollToLyric(position: Int) {
        if (position == lastScrollPosition) return

        pendingScrollRunnable?.let { scrollHandler.removeCallbacks(it) }

        val runnable = Runnable {
            if (position != lastScrollPosition) {
                lastScrollPosition = position
                val layoutManager = binding.lyricRecyclerView.layoutManager as LinearLayoutManager
                layoutManager.scrollToPositionWithOffset(position, binding.lyricRecyclerView.height / 3)
            }
        }
        pendingScrollRunnable = runnable
        scrollHandler.postDelayed(runnable, SCROLL_DEBOUNCE_MS)
    }

    private fun setupListeners() {
        binding.closeBtn.setOnClickListener {
            finish()
        }

        binding.playPauseBtn.setOnClickListener {
            viewModel.playPause()
        }

        binding.previousBtn.setOnClickListener {
            viewModel.previousSong()
        }

        binding.nextBtn.setOnClickListener {
            viewModel.nextSong()
        }

        binding.modeSwitchBtn.setOnClickListener {
            viewModel.toggleMode()
        }

        binding.coverModeLayout.setOnClickListener {
            viewModel.toggleMode()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.currentTime.text = progress.toLong().formatTime()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTrackingTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTrackingTouch = false
                seekBar?.progress?.let { viewModel.seekTo(it.toLong()) }
            }
        })
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            while (true) {
                viewModel.updateProgress()
                viewModel.updateLyricProgress()
                delay(Constants.PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onStart() {
        super.onStart()
        startProgressUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopProgressUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        pendingScrollRunnable?.let { scrollHandler.removeCallbacks(it) }
        viewModel.unbindService(this)
    }
}
