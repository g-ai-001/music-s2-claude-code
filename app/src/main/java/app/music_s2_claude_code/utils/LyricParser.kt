package app.music_s2_claude_code.utils

import app.music_s2_claude_code.data.LyricLine
import java.io.File
import java.util.regex.Pattern

object LyricParser {

    private val LRC_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

    fun parseLyricFile(lrcFile: File): List<LyricLine> {
        val lyrics = mutableListOf<LyricLine>()
        if (!lrcFile.exists()) {
            LogUtils.i("歌词文件不存在: ${lrcFile.absolutePath}")
            return emptyList()
        }

        try {
            lrcFile.forEachLine { line ->
                parseLyricLine(line)?.let { lyrics.add(it) }
            }
            lyrics.sortBy { it.timeMs }
            LogUtils.i("解析歌词完成，共 ${lyrics.size} 行")
        } catch (e: Exception) {
            LogUtils.e("解析歌词文件失败: ${e.message}")
        }

        return lyrics
    }

    fun parseLyricLine(line: String): LyricLine? {
        val matcher = LRC_PATTERN.matcher(line.trim())
        if (matcher.find()) {
            val minutes = matcher.group(1)?.toIntOrNull() ?: 0
            val seconds = matcher.group(2)?.toIntOrNull() ?: 0
            val milliseconds = matcher.group(3)?.toIntOrNull() ?: 0
            val text = matcher.group(4)?.trim() ?: ""

            if (text.isNotEmpty()) {
                val minutesMs = minutes * 60 * 1000L
                val secondsMs = seconds * 1000L
                val millisPart = if (matcher.group(3)?.length == 2) milliseconds * 10L else milliseconds
                val timeMs = minutesMs + secondsMs + millisPart
                return LyricLine(timeMs, text)
            }
        }
        return null
    }

    fun findLrcFile(audioPath: String): File? {
        val audioFile = File(audioPath)
        if (!audioFile.exists()) {
            return null
        }

        val lrcFileName = audioFile.nameWithoutExtension + ".lrc"
        val lrcFile = File(audioFile.parent, lrcFileName)

        if (lrcFile.exists()) {
            LogUtils.i("找到歌词文件: ${lrcFile.absolutePath}")
            return lrcFile
        }

        LogUtils.i("未找到歌词文件: $lrcFileName")
        return null
    }

    fun findCurrentLyricIndex(lyrics: List<LyricLine>, positionMs: Long): Int {
        for (i in lyrics.indices.reversed()) {
            if (positionMs >= lyrics[i].timeMs) {
                return i
            }
        }
        return 0
    }
}
