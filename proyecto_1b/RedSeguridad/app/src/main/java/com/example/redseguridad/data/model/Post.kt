package com.example.redseguridad.data.model

/**
 * Representa exactamente la estructura JSON que devuelve JSONPlaceholder
 * para el endpoint GET /posts/{id}.
 *
 * Ejemplo de respuesta real:
 * {
 *   "userId": 1,
 *   "id": 1,
 *   "title": "sunt aut facere repellat",
 *   "body": "quia et suscipit..."
 * }
 *
 * Es una data class: equals(), hashCode() y copy() generados automáticamente.
 * No tiene lógica — solo estructura de datos (Single Responsibility).
 */
data class Post(
    val id:     Int,
    val userId: Int,
    val title:  String,
    val body:   String
)