package com.example.persistenciadual.model

data class Song(
    val id: Long = 0L,
    val title: String,
    val duration: String,
    val category: Category,
    val links: List<SongLink> = emptyList()
)