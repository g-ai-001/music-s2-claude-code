package app.music_s2_claude_code.utils

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import app.music_s2_claude_code.data.Song

object MediaScanner {

    suspend fun scanLocalMusic(context: Context): List<Song> {
        val songs = mutableListOf<Song>()
        LogUtils.i("开始扫描本地音乐...")

        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf("30000")

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "未知标题"
                val artist = cursor.getString(artistColumn) ?: "未知艺术家"
                val album = cursor.getString(albumColumn) ?: "未知专辑"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(pathColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val albumArtUri = "content://media/external/audio/albumart/$albumId"

                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                        albumArtUri = albumArtUri
                    )
                )
            }
        }

        LogUtils.i("扫描完成，共找到 ${songs.size} 首歌曲")
        return songs
    }
}
