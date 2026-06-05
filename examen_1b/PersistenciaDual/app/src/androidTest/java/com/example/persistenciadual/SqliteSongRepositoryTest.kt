package com.example.persistenciadual

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.persistenciadual.db.SongDbHelper
import com.example.persistenciadual.model.Category
import com.example.persistenciadual.model.Song
import com.example.persistenciadual.model.SongLink
import com.example.persistenciadual.repository.SqliteSongRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SqliteSongRepositoryTest {

    private lateinit var repo: SqliteSongRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(SongDbHelper.DATABASE_NAME)
        repo = SqliteSongRepository(context)
    }

    @After
    fun tearDown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(SongDbHelper.DATABASE_NAME)
    }

    @Test
    fun insert_y_getAll_devuelve_cancion_con_links() {
        val song = Song(
            title = "Hotel California",
            duration = "6:30",
            category = Category.ROCK,
            links = listOf(
                SongLink("Spotify", "https://open.spotify.com/track/hotel"),
                SongLink("YouTube", "https://youtube.com/watch?v=hotel")
            )
        )

        val id = repo.insert(song)
        val result = repo.getAll()

        assertTrue("El id debe ser mayor a 0", id > 0)
        assertEquals("Debe haber 1 canción", 1, result.size)
        assertEquals("Hotel California", result[0].title)
        assertEquals("6:30", result[0].duration)
        assertEquals(Category.ROCK, result[0].category)
        assertEquals("Debe tener 2 links", 2, result[0].links.size)
    }

    @Test
    fun delete_elimina_cancion_y_cascade_links() {
        val id = repo.insert(
            Song(
                title = "Stairway to Heaven",
                duration = "8:02",
                category = Category.ROCK,
                links = listOf(SongLink("YouTube", "https://youtube.com/stairway"))
            )
        )

        repo.delete(id)
        val result = repo.getAll()
        assertEquals("Debe haber 0 canciones después de CASCADE delete", 0, result.size)
    }
}