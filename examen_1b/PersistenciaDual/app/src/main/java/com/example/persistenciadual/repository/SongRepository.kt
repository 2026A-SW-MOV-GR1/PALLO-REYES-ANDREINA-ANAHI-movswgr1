package com.example.persistenciadual.repository

import com.example.persistenciadual.model.Song

/**
 * Contrato de persistencia para canciones.
 * El ViewModel solo conoce esta interfaz — nunca las implementaciones concretas.
 */
interface SongRepository {
    fun getAll(): List<Song>
    fun insert(song: Song): Long
    fun update(song: Song): Int
    fun delete(id: Long): Int
}