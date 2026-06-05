package com.example.redseguridad.ui.secrets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redseguridad.data.model.StorageEngine
import com.example.redseguridad.data.storage.SecretRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AndroidViewModel (en lugar de ViewModel) porque SecretRepository
 * necesita Context para inicializar SharedPreferences, DataStore
 * y EncryptedSharedPreferences. getApplication() provee el
 * ApplicationContext de forma segura sin riesgo de memory leak.
 */
class SecretsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SecretRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<SecretsUiState>(SecretsUiState.Idle)
    val uiState: StateFlow<SecretsUiState> = _uiState.asStateFlow()

    // ── GUARDAR ───────────────────────────────────────────────────────────────

    fun saveSecret(key: String, value: String, engine: StorageEngine) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.save(key, value, engine)
                _uiState.value = SecretsUiState.SaveSuccess(engine.label)
            } catch (e: Exception) {
                _uiState.value = SecretsUiState.Error(
                    "Error al guardar en ${engine.label}: ${e.message}"
                )
            }
        }
    }

    // ── RECUPERAR ─────────────────────────────────────────────────────────────

    fun retrieveSecret(key: String, engine: StorageEngine) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val value = repository.retrieve(key, engine)
                _uiState.value = if (value != null) {
                    SecretsUiState.RetrieveSuccess(key, value, engine.label)
                } else {
                    SecretsUiState.Error("⚠️ La llave \"$key\" no existe en ${engine.label}")
                }
            } catch (e: Exception) {
                _uiState.value = SecretsUiState.Error(
                    "Error al recuperar de ${engine.label}: ${e.message}"
                )
            }
        }
    }

    // ── RESET ─────────────────────────────────────────────────────────────────

    /**
     * Vuelve al estado Idle después de consumir un SaveSuccess o Error.
     * Evita que los Toasts se repitan al rotar la pantalla.
     */
    fun resetState() {
        _uiState.value = SecretsUiState.Idle
    }
}