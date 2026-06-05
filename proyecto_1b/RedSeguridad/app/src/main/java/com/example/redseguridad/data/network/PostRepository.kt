package com.example.redseguridad.data.network

import com.example.redseguridad.data.model.Post
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Única clase con acceso a la red en el proyecto.
 *
 * Tecnología: HttpURLConnection nativo de Android + org.json.JSONObject
 * (ambos son parte del SDK de Android, sin dependencias externas).
 *
 * IMPORTANTE: sus funciones son BLOQUEANTES. El ViewModel las llama
 * dentro de viewModelScope.launch(Dispatchers.IO) para no bloquear
 * el hilo principal (UI thread).
 *
 * Retorna Result<T> en vez de lanzar excepciones: el llamador (ViewModel)
 * usa result.fold { onSuccess / onFailure } para manejar ambos casos.
 */
class PostRepository {

    companion object {
        private const val BASE_URL   = "https://jsonplaceholder.typicode.com/posts"
        private const val TIMEOUT_MS = 10_000  // 10 segundos
    }

    // ── GET /posts/{id} ───────────────────────────────────────────────────────

    /**
     * Consulta un post por ID. JSONPlaceholder tiene posts del 1 al 100.
     * Para IDs inexistentes (> 100) devuelve HTTP 404.
     *
     * @param id Identificador numérico del post.
     * @return Result.success(Post) si HTTP 200, Result.failure(Exception) en otro caso.
     */
    fun getPost(id: Int): Result<Post> {
        return try {
            val connection = (URL("$BASE_URL/$id").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout    = TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
            }

            val code = connection.responseCode

            if (code == HttpURLConnection.HTTP_OK) {
                val responseBody = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val json = JSONObject(responseBody)
                Result.success(
                    Post(
                        id     = json.getInt("id"),
                        userId = json.getInt("userId"),
                        title  = json.getString("title"),
                        body   = json.getString("body")
                    )
                )
            } else {
                connection.disconnect()
                Result.failure(Exception("HTTP $code — Post no encontrado"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── PUT /posts/{id} ───────────────────────────────────────────────────────

    /**
     * Envía el post modificado de vuelta al servidor.
     * JSONPlaceholder acepta el PUT y responde siempre HTTP 200
     * (es una API fake: no persiste los cambios, pero retorna el código real).
     *
     * @param post Post con los campos title y body modificados por el usuario.
     * @return Result.success(200) si HTTP 200, Result.failure(Exception) en otro caso.
     */
    fun updatePost(post: Post): Result<Int> {
        return try {
            val connection = (URL("$BASE_URL/${post.id}").openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = TIMEOUT_MS
                readTimeout    = TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true  // Habilita el cuerpo de la petición
            }

            // Serializar el Post a JSON y enviarlo como body
            val requestBody = JSONObject().apply {
                put("id",     post.id)
                put("userId", post.userId)
                put("title",  post.title)
                put("body",   post.body)
            }.toString()

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(requestBody)
                writer.flush()
            }

            val code = connection.responseCode
            connection.disconnect()

            if (code == HttpURLConnection.HTTP_OK) {
                Result.success(code)
            } else {
                Result.failure(Exception("HTTP $code — Error al actualizar"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}