package com.example.persistenciadual.repository

import android.util.Log
import com.example.persistenciadual.model.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class JsonSongRepository(private val directory: File) : SongRepository {

    companion object {
        private const val TAG = "JsonRepo"
        private const val FILE_NAME = "songs_nosql.json"
    }

    private val gson = Gson()
    private val file = File(directory, FILE_NAME)
    private val listType = object : TypeToken<MutableList<Song>>() {}.type

    private fun readAll(): MutableList<Song> {
        if (!file.exists()) return mutableListOf()
        val json = file.readText()
        if (json.isBlank()) return mutableListOf()
        return try {
            gson.fromJson(json, listType) ?: mutableListOf()
        } catch (e: Exception) {
            Log.e(TAG, "ERROR — Lectura JSON falló: ${e.message}")
            mutableListOf()
        }
    }

    private fun writeAll(songs: List<Song>) {
        file.writeText(gson.toJson(songs))
    }

    override fun getAll(): List<Song> {
        val songs = readAll()
        Log.d(TAG, "DEBUG — Lectura $FILE_NAME → ${songs.size} documentos")
        return songs
    }

    override fun insert(song: Song): Long {
        val songs = readAll()
        val newId = if (songs.isEmpty()) 1L else songs.maxOf { it.id } + 1L
        val newSong = song.copy(id = newId)
        songs.add(newSong)
        writeAll(songs)
        Log.i(TAG, "INFO — INSERT doc id=$newId title=\"${song.title}\" links=${song.links.size}")
        return newId
    }

    override fun update(song: Song): Int {
        val songs = readAll()
        val index = songs.indexOfFirst { it.id == song.id }
        if (index == -1) {
            Log.e(TAG, "ERROR — UPDATE doc id=${song.id} no encontrado")
            return 0
        }
        songs[index] = song
        writeAll(songs)
        Log.i(TAG, "INFO — UPDATE doc id=${song.id}")
        return 1
    }

    override fun delete(id: Long): Int {
        val songs = readAll()
        val sizeBefore = songs.size
        songs.removeAll { it.id == id }
        writeAll(songs)
        val removed = sizeBefore - songs.size
        Log.i(TAG, "INFO — DELETE doc id=$id removed=$removed")
        return removed
    }
}