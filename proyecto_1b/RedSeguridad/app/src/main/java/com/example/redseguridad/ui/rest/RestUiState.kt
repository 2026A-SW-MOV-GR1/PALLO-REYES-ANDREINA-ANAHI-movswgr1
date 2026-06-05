package com.example.redseguridad.ui.rest

import com.example.redseguridad.data.model.Post

/**
 * Representa todos los estados posibles de la pantalla REST API.
 *
 * El Fragment colecta este StateFlow y reacciona a cada estado:
 *
 *   Idle        → pantalla vacía, botón PUT deshabilitado
 *   Loading     → ProgressBar visible, todos los campos deshabilitados
 *   GetSuccess  → pinta title y body en los EditText, habilita PUT
 *   PutSuccess  → muestra "✅ 200 OK — Actualizado", mantiene los campos
 *   Error       → muestra mensaje de error, oculta ProgressBar
 *
 * sealed class garantiza que el when() en el Fragment sea exhaustivo
 * (el compilador exige cubrir todos los casos).
 */
sealed class RestUiState {

    /** Estado inicial: ninguna petición lanzada. */
    data object Idle : RestUiState()

    /** Petición GET o PUT en tránsito: deshabilitar UI. */
    data object Loading : RestUiState()

    /**
     * GET exitoso (HTTP 200).
     * @param post El post recibido del servidor, listo para pintar en los campos.
     */
    data class GetSuccess(val post: Post) : RestUiState()

    /**
     * PUT exitoso (HTTP 200).
     * @param statusCode Código HTTP recibido (siempre 200 en JSONPlaceholder).
     */
    data class PutSuccess(val statusCode: Int) : RestUiState()

    /**
     * Error de red, timeout o código HTTP inesperado.
     * @param message Descripción legible del error para mostrar al usuario.
     */
    data class Error(val message: String) : RestUiState()
}