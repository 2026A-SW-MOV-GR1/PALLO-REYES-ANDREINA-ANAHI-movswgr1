package com.example.persistenciadual.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SongDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "SongDbHelper"
        const val DATABASE_NAME = "songs.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE songs (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                title      TEXT    NOT NULL,
                duration   TEXT    NOT NULL,
                category   TEXT    NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE song_links (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                song_id  INTEGER NOT NULL,
                platform TEXT    NOT NULL,
                url      TEXT    NOT NULL,
                FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        Log.i(TAG, "INFO — Base de datos creada: $DATABASE_NAME v$DATABASE_VERSION")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "INFO — Upgrade de v$oldVersion a v$newVersion")
        db.execSQL("DROP TABLE IF EXISTS song_links")
        db.execSQL("DROP TABLE IF EXISTS songs")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA foreign_keys = ON")
        Log.d(TAG, "DEBUG — PRAGMA foreign_keys activado")
    }
}