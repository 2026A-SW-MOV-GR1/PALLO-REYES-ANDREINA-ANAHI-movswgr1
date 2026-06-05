package com.example.redseguridad.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.redseguridad.data.model.StorageEngine
import kotlinx.coroutines.flow.first

// Singleton DataStore — DEBE estar a nivel de archivo, fuera de la clase.
// El delegado preferencesDataStore garantiza una sola instancia por proceso.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "rs_datastore"
)

/**
 * Repositorio de almacenamiento seguro (Módulo 3).
 *
 * Abstrae los tres motores nativos de Android detrás de una
 * interfaz uniforme save/retrieve. El ViewModel elige el motor
 * pasando el enum StorageEngine.
 *
 * Todos los métodos son suspend — llamados desde Dispatchers.IO
 * en el ViewModel para no bloquear el UI thread.
 */
class SecretRepository(private val context: Context) {

    // ── GUARDAR ───────────────────────────────────────────────────────────────

    suspend fun save(key: String, value: String, engine: StorageEngine) {
        when (engine) {

            StorageEngine.SHARED_PREFS -> {
                // Síncrono. apply() escribe en background thread interno de Android.
                context
                    .getSharedPreferences("rs_shared_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString(key, value)
                    .apply()
            }

            StorageEngine.DATA_STORE -> {
                // Asíncrono reactivo. edit() es suspend: suspende hasta que
                // los datos se escriben en disco de forma atómica.
                context.dataStore.edit { preferences ->
                    preferences[stringPreferencesKey(key)] = value
                }
            }

            StorageEngine.ENCRYPTED_SHARED_PREFS -> {
                // Cifrado automático: AES-256-SIV para llaves, AES-128-GCM para valores.
                // Los datos cifrados se escriben en disco antes de retornar.
                getEncryptedPrefs()
                    .edit()
                    .putString(key, value)
                    .commit()   // commit() en vez de apply() para garantizar escritura
            }
        }
    }

    // ── RECUPERAR ─────────────────────────────────────────────────────────────

    suspend fun retrieve(key: String, engine: StorageEngine): String? {
        return when (engine) {

            StorageEngine.SHARED_PREFS -> {
                context
                    .getSharedPreferences("rs_shared_prefs", Context.MODE_PRIVATE)
                    .getString(key, null)
            }

            StorageEngine.DATA_STORE -> {
                // data es un Flow<Preferences>. first() recoge la primera emisión
                // (el estado actual del disco) y cancela la suscripción.
                context.dataStore.data.first()[stringPreferencesKey(key)]
            }

            StorageEngine.ENCRYPTED_SHARED_PREFS -> {
                getEncryptedPrefs().getString(key, null)
            }
        }
    }

    // ── PRIVADO ───────────────────────────────────────────────────────────────

    /**
     * Construye (o abre) el EncryptedSharedPreferences.
     * MasterKeys.getOrCreate() crea la clave maestra en el Android Keystore
     * la primera vez; en llamadas posteriores la reutiliza.
     *
     * Puede lanzar GeneralSecurityException o IOException — propagado al
     * ViewModel que lo captura con try/catch.
     */
    private fun getEncryptedPrefs(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "rs_encrypted_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}