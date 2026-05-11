package app.music_s2_claude_code.utils

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogUtils {
    private var logFile: File? = null
    private var bufferedWriter: BufferedWriter? = null
    private const val BUFFER_SIZE = 8192

    fun init(context: Context) {
        val logDir = context.getExternalFilesDir(null)
        logDir?.let {
            if (!it.exists()) {
                it.mkdirs()
            }
            logFile = File(it, Constants.LOG_FILE_NAME)
        }
        i("LogUtils initialized")
    }

    private fun checkAndRotateLogFile() {
        logFile?.let { file ->
            if (file.exists() && file.length() > Constants.MAX_LOG_FILE_SIZE) {
                closeWriter()
                val backupFile = File(file.parent, "${Constants.LOG_FILE_NAME}.old")
                if (backupFile.exists()) {
                    backupFile.delete()
                }
                file.renameTo(backupFile)
                i("Log file rotated due to size limit")
            }
        }
    }

    private fun getWriter(): BufferedWriter? {
        if (bufferedWriter == null) {
            logFile?.let { file ->
                try {
                    checkAndRotateLogFile()
                    bufferedWriter = BufferedWriter(FileWriter(file, true), BUFFER_SIZE)
                } catch (e: Exception) {
                    android.util.Log.e(Constants.LOG_TAG, "Failed to create log writer: ${e.message}")
                }
            }
        }
        return bufferedWriter
    }

    private fun closeWriter() {
        try {
            bufferedWriter?.flush()
            bufferedWriter?.close()
        } catch (e: Exception) {
            // Ignore
        } finally {
            bufferedWriter = null
        }
    }

    fun log(message: String, level: String = "I") {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] [$level] $message\n"

        when (level) {
            "D" -> android.util.Log.d(Constants.LOG_TAG, message)
            "E" -> android.util.Log.e(Constants.LOG_TAG, message)
            "W" -> android.util.Log.w(Constants.LOG_TAG, message)
            else -> android.util.Log.i(Constants.LOG_TAG, message)
        }

        try {
            getWriter()?.let { writer ->
                checkAndRotateLogFile()
                writer.write(logMessage)
                writer.flush()
            }
        } catch (e: Exception) {
            android.util.Log.e(Constants.LOG_TAG, "Failed to write log: ${e.message}")
            closeWriter()
        }
    }

    fun d(message: String) = log(message, "D")
    fun e(message: String) = log(message, "E")
    fun w(message: String) = log(message, "W")
    fun i(message: String) = log(message, "I")

    fun release() {
        i("LogUtils releasing resources")
        closeWriter()
    }
}
