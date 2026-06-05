package com.example.persistenciadual

import com.example.persistenciadual.model.Category
import com.example.persistenciadual.model.Song
import com.example.persistenciadual.model.SongLink
import com.example.persistenciadual.repository.JsonSongRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class JsonSongRepositoryTest {

    private lateinit var repo: JsonSongRepository
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("json_test").toFile()
        repo = JsonSongRepository(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun insert_y_getAll_devuelve_cancion_con_links() {
        val song = Song(
            title = "Bohemian Rhapsody",
            duration = "5:55",
            category = Category.ROCK,
            links = listOf(
                SongLink("Spotify", "https://open.spotify.com/track/test"),
                SongLink("YouTube", "https://youtube.com/watch?v=test")
            )
        )

        val id = repo.insert(song)
        val result = repo.getAll()

        assertTrue("El id debe ser mayor a 0", id > 0)
        assertEquals("Debe haber 1 canción", 1, result.size)
        assertEquals("Bohemian Rhapsody", result[0].title)
        assertEquals("5:55", result[0].duration)
        assertEquals(Category.ROCK, result[0].category)
        assertEquals("Debe tener 2 links", 2, result[0].links.size)
        assertEquals("Spotify", result[0].links[0].platform)
    }

    @Test
    fun delete_elimina_cancion() {
        val id = repo.insert(
            Song(title = "Blinding Lights", duration = "3:20", category = Category.POP)
        )
        assertEquals("Debe haber 1 canción antes de borrar", 1, repo.getAll().size)

        repo.delete(id)
        assertEquals("Debe haber 0 canciones después de borrar", 0, repo.getAll().size)
    }

    @Test
    fun update_modifica_cancion_existente() {
        val id = repo.insert(
            Song(title = "Original", duration = "1:00", category = Category.JAZZ)
        )

        val updated = Song(
            id = id,
            title = "Modificada",
            duration = "2:30",
            category = Category.CLASICA,
            links = listOf(SongLink("SoundCloud", "https://soundcloud.com/test"))
        )
        val rows = repo.update(updated)
        val result = repo.getAll()

        assertEquals("update debe retornar 1", 1, rows)
        assertEquals("Modificada", result[0].title)
        assertEquals("2:30", result[0].duration)
        assertEquals(Category.CLASICA, result[0].category)
        assertEquals(1, result[0].links.size)
    }
}