package com.example.persistenciadual.model

enum class Category(val displayName: String) {
    ROCK("Rock"),
    POP("Pop"),
    ELECTRONICA("Electrónica"),
    RAP("Rap"),
    JAZZ("Jazz"),
    CLASICA("Clásica"),
    OTRO("Otro");

    companion object {
        /** Busca por nombre del enum (ej. "ROCK") — usado al leer de SQLite y JSON. */
        fun fromName(name: String): Category {
            return entries.firstOrNull { it.name == name } ?: OTRO
        }
    }
}