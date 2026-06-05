package com.example.persistenciadual.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistenciadual.model.Song
import com.example.persistenciadual.repository.JsonSongRepository
import com.example.persistenciadual.repository.SongRepository
import com.example.persistenciadual.repository.SqliteSongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SongViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SongViewModel"
    }

    private val sqliteRepo: SongRepository = SqliteSongRepository(application)
    private val jsonRepo: SongRepository = JsonSongRepository(application.filesDir)

    private var activeRepo: SongRepository = sqliteRepo

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _activeEngine = MutableStateFlow("SQL")
    val activeEngine: StateFlow<String> = _activeEngine.asStateFlow()

    val isUsingSql: Boolean
        get() = _activeEngine.value == "SQL"

    init {
        loadSongs()
    }

    fun switchEngine(useSql: Boolean) {
        activeRepo = if (useSql) sqliteRepo else jsonRepo
        _activeEngine.value = if (useSql) "SQL" else "NoSQL"
        Log.i(TAG, "INFO — Motor cambiado a: ${_activeEngine.value}")
        loadSongs()
    }

    fun insert(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = activeRepo.insert(song)
            Log.i(TAG, "INFO — Insert completado id=$id motor=${_activeEngine.value}")
            reloadSongs()
        }
    }

    fun update(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            activeRepo.update(song)
            Log.i(TAG, "INFO — Update completado id=${song.id} motor=${_activeEngine.value}")
            reloadSongs()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            activeRepo.delete(id)
            Log.i(TAG, "INFO — Delete completado id=$id motor=${_activeEngine.value}")
            reloadSongs()
        }
    }

    fun getSongById(id: Long): Song? {
        return _songs.value.find { it.id == id }
    }

    private fun loadSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            reloadSongs()
        }
    }

    private suspend fun reloadSongs() {
        val list = activeRepo.getAll()
        _songs.value = list
        Log.d(TAG, "DEBUG — Cargadas ${list.size} canciones desde ${_activeEngine.value}")
    }
}