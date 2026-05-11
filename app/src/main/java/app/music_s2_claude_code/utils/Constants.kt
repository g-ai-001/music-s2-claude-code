package app.music_s2_claude_code.utils

object Constants {
    const val LOG_TAG = "MusicS2"
    const val LOG_FILE_NAME = "app_log.txt"
    const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024L // 5MB
    const val MIN_SONG_DURATION_MS = 30000L // 30秒
    const val PROGRESS_UPDATE_INTERVAL_MS = 500L // 0.5秒
    const val MUSIC_PLAYBACK_CHANNEL_ID = "music_playback_channel"
    const val MUSIC_PLAYBACK_NOTIFICATION_ID = 1
}
