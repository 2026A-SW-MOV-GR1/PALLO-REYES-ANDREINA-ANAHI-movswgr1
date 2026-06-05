package com.example.redseguridad.ui.secrets

/**
 * Estados posibles de la pantalla Gestión de Secretos.
 *
 * Se separan SaveSuccess y RetrieveSuccess deliberadamente:
 * - SaveSuccess → solo necesita mostrar un Toast de confirmación
 * - RetrieveSuccess → necesita mostrar el valor en cardResult
 *
 * El Fragment usa when() exhaustivo sobre este sealed class.
 */
sealed class SecretsUiState {

    /** Estado inicial o tras resetear. cardResult oculto. */
    data object Idle : SecretsUiState()

    /**
     * Guardado exitoso.
     * @param engine Nombre del motor para mostrar en el Toast.
     */
    data class SaveSuccess(val engine: String) : SecretsUiState()

    /**
     * Recuperación exitosa.
     * @param key    Llave consultada.
     * @param value  Valor recuperado del motor.
     * @param engine Nombre del motor donde se encontró.
     */
    data class RetrieveSuccess(
        val key:    String,
        val value:  String,
        val engine: String
    ) : SecretsUiState()

    /**
     * Error en cualquier operación (motor no encontrado, excepción, etc.).
     * @param message Descripción del error para mostrar al usuario.
     */
    data class Error(val message: String) : SecretsUiState()
}