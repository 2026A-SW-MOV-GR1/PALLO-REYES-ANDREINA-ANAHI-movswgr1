package com.example.redseguridad.data.model

enum class StorageEngine(val label: String) {
    SHARED_PREFS("SharedPreferences"),
    DATA_STORE("Jetpack DataStore"),
    ENCRYPTED_SHARED_PREFS("EncryptedSharedPreferences")
}