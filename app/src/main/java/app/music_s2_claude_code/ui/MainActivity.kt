package app.music_s2_claude_code.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.music_s2_claude_code.R
import app.music_s2_claude_code.databinding.ActivityMainBinding
import app.music_s2_claude_code.utils.LogUtils
import app.music_s2_claude_code.viewmodel.MusicViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MusicViewModel by viewModels()
    private lateinit var adapter: SongAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            viewModel.updateProgress()
            handler.postDelayed(this, 500)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanMusic()
        } else {
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtils.init(this)
        LogUtils.i("MainActivity onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        checkPermissionAndScan()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter { song, index ->
            viewModel.songs.value?.let {
                viewModel.playSong(it, index)
            }
        }
        binding.songsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songsRecyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.songs.observe(this) { songs ->
            adapter.submitList(songs)
            if (songs.isEmpty()) {
                Toast.makeText(this, R.string.no_songs_found, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.currentSong.observe(this) { song ->
            song?.let {
                binding.miniPlayer.visibility = android.view.View.VISIBLE
                binding.miniSongTitle.text = it.title
                binding.miniSongArtist.text = it.artist
            }
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            binding.miniPlayBtn.setImageResource(icon)
        }

        viewModel.isScanning.observe(this) { scanning ->
            binding.refreshBtn.isEnabled = !scanning
        }
    }

    private fun setupListeners() {
        binding.refreshBtn.setOnClickListener {
            checkPermissionAndScan()
        }

        binding.miniPlayBtn.setOnClickListener {
            viewModel.playPause()
        }

        binding.miniNextBtn.setOnClickListener {
            viewModel.nextSong()
        }
    }

    private fun checkPermissionAndScan() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.scanMusic()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.bindService(this)
        handler.post(progressRunnable)
    }

    override fun onStop() {
        super.onStop()
        viewModel.unbindService(this)
        handler.removeCallbacks(progressRunnable)
    }
}
