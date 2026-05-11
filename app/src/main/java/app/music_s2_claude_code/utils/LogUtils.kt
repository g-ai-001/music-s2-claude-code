package app.music_s2_claude_code.utils

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogUtils {
    private const val TAG = "MusicS2"
    private const val LOG_FILE_NAME = "app_log.txt"
    private var logFile: File? = null

    fun init(context: Context) {
        val logDir = context.getExternalFilesDir(null)
        logDir?.let {
            if (!it.exists()) {
                it.mkdirs()
            }
            logFile = File(it, LOG_FILE_NAME)
        }
        log("LogUtils initialized")
    }

    fun log(message: String, level: String = "I") {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] [$level] $message\n"

        when (level) {
            "D" -> android.util.Log.d(TAG, message)
            "E" -> android.util.Log.e(TAG, message)
            "W" -> android.util.Log.w(TAG, message)
            else -> android.util.Log.i(TAG, message)
        }

        logFile?.let { file ->
            try {
                FileWriter(file, true).use { writer ->
                    writer.append(logMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to write log: ${e.message}")
            }
        }
    }

    fun d(message: String) = log(message, "D")
    fun e(message: String) = log(message, "E")
    fun w(message: String) = log(message, "W")
    fun i(message: String) = log(message, "I")
}
