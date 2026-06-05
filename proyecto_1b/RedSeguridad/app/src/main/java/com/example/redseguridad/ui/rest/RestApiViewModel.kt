package com.example.redseguridad.ui.rest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redseguridad.data.network.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel del módulo REST API.
 *
 * Responsabilidades:
 *  - Lanzar coroutines en Dispatchers.IO para no bloquear el UI thread.
 *  - Mantener el post activo (currentPost) entre GET y PUT.
 *  - Emitir estados via StateFlow para que el Fragment los observe.
 *
 * Sobrevive a rotaciones de pantalla (ciclo de vida del ViewModel).
 * El Fragment se suscribe en onViewCreated y cancela en onDestroyView
 * gracias a repeatOnLifecycle(Lifecycle.State.STARTED).
 */
class RestApiViewModel : ViewModel() {

    private val repository = PostRepository()

    // StateFlow privado mutable → expuesto como inmutable al Fragment
    private val _uiState = MutableStateFlow<RestUiState>(RestUiState.Idle)
    val uiState: StateFlow<RestUiState> = _uiState.asStateFlow()

    // Guarda el último post cargado para reutilizar id y userId en el PUT
    private var currentPost: com.example.redseguridad.data.model.Post? = null

    // ── GET ───────────────────────────────────────────────────────────────────

    /**
     * Consulta el post con el [id] dado en JSONPlaceholder.
     * Emite Loading → GetSuccess o Error.
     */
    fun fetchPost(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = RestUiState.Loading

            repository.getPost(id).fold(
                onSuccess = { post ->
                    currentPost = post
                    _uiState.value = RestUiState.GetSuccess(post)
                },
                onFailure = { error ->
                    currentPost = null
                    _uiState.value = RestUiState.Error(
                        error.message ?: "Error desconocido al consultar"
                    )
                }
            )
        }
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    /**
     * Envía el post modificado al servidor.
     * Solo se puede llamar si existe un [currentPost] (botón PUT deshabilitado
     * hasta que GET haya sido exitoso).
     *
     * @param title Título editado por el usuario.
     * @param body  Cuerpo editado por el usuario.
     *
     * Emite Loading → PutSuccess o Error.
     */
    fun updatePost(title: String, body: String) {
        val post = currentPost ?: return  // Guarda silenciosa: no debería ocurrir

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = RestUiState.Loading

            val updatedPost = post.copy(title = title, body = body)

            repository.updatePost(updatedPost).fold(
                onSuccess = { statusCode ->
                    _uiState.value = RestUiState.PutSuccess(statusCode)
                },
                onFailure = { error ->
                    _uiState.value = RestUiState.Error(
                        error.message ?: "Error desconocido al actualizar"
                    )
                }
            )
        }
    }
}