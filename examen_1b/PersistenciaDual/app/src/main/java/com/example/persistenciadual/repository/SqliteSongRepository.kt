package com.example.persistenciadual.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.persistenciadual.db.SongDbHelper
import com.example.persistenciadual.model.Category
import com.example.persistenciadual.model.Song
import com.example.persistenciadual.model.SongLink

class SqliteSongRepository(context: Context) : SongRepository {

    companion object {
        private const val TAG = "SqliteRepo"
    }

    private val dbHelper = SongDbHelper(context)

    override fun getAll(): List<Song> {
        val db = dbHelper.readableDatabase
        val songs = mutableListOf<Song>()

        val cursor = db.query("songs", null, null, null, null, null, "created_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                val songId = it.getLong(it.getColumnIndexOrThrow("id"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val duration = it.getString(it.getColumnIndexOrThrow("duration"))
                val category = Category.fromName(
                    it.getString(it.getColumnIndexOrThrow("category"))
                )
                val links = getLinksForSong(db, songId)

                songs.add(Song(songId, title, duration, category, links))
            }
        }

        Log.d(TAG, "DEBUG — SELECT * FROM songs → ${songs.size} registros")
        return songs
    }

    override fun insert(song: Song): Long {
        val db = dbHelper.writableDatabase
        var songId = -1L

        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("title", song.title)
                put("duration", song.duration)
                put("category", song.category.name)
                put("created_at", System.currentTimeMillis())
            }
            songId = db.insert("songs", null, values)

            for (link in song.links) {
                val linkValues = ContentValues().apply {
                    put("song_id", songId)
                    put("platform", link.platform)
                    put("url", link.url)
                }
                db.insert("song_links", null, linkValues)
            }

            db.setTransactionSuccessful()
            Log.i(TAG, "INFO — INSERT song id=$songId title=\"${song.title}\" links=${song.links.size}")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR — INSERT falló: ${e.message}")
        } finally {
            db.endTransaction()
        }

        return songId
    }

    override fun update(song: Song): Int {
        val db = dbHelper.writableDatabase
        var rowsUpdated = 0

        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("title", song.title)
                put("duration", song.duration)
                put("category", song.category.name)
            }
            rowsUpdated = db.update("songs", values, "id = ?", arrayOf(song.id.toString()))

            // Reemplazar links: borrar viejos e insertar nuevos
            db.delete("song_links", "song_id = ?", arrayOf(song.id.toString()))
            for (link in song.links) {
                val linkValues = ContentValues().apply {
                    put("song_id", song.id)
                    put("platform", link.platform)
                    put("url", link.url)
                }
                db.insert("song_links", null, linkValues)
            }

            db.setTransactionSuccessful()
            Log.i(TAG, "INFO — UPDATE song id=${song.id} rows=$rowsUpdated")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR — UPDATE falló: ${e.message}")
        } finally {
            db.endTransaction()
        }

        return rowsUpdated
    }

    override fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        val rows = db.delete("songs", "id = ?", arrayOf(id.toString()))
        Log.i(TAG, "INFO — DELETE song id=$id rows=$rows (links eliminados por CASCADE)")
        return rows
    }

    private fun getLinksForSong(db: SQLiteDatabase, songId: Long): List<SongLink> {
        val links = mutableListOf<SongLink>()

        val cursor = db.query(
            "song_links", null,
            "song_id = ?", arrayOf(songId.toString()),
            null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                links.add(
                    SongLink(
                        platform = it.getString(it.getColumnIndexOrThrow("platform")),
                        url = it.getString(it.getColumnIndexOrThrow("url"))
                    )
                )
            }
        }

        return links
    }
}