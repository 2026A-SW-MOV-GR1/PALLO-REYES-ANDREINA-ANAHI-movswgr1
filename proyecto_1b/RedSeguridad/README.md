# RedSeguridad

Aplicación Android nativa desarrollada en Kotlin que implementa conectividad asíncrona HTTP REST con JSONPlaceholder y persistencia segura de datos utilizando las APIs de almacenamiento nativas del sistema operativo Android.

---

## Tecnologías utilizadas

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| SDK mínimo | API 26 (Android 8.0) |
| SDK objetivo | API 36 |
| Build system | AGP 9.1.1 |
| Arquitectura | MVVM (Model-View-ViewModel) |
| UI | ViewBinding + Material Design 3 |
| Asincronismo | Kotlin Coroutines + StateFlow |
| HTTP | HttpURLConnection (nativo Android) |
| Parseo JSON | org.json.JSONObject (nativo Android) |
| Persistencia 1 | SharedPreferences |
| Persistencia 2 | Jetpack DataStore (Preferences) |
| Persistencia 3 | EncryptedSharedPreferences (AES-256-SIV / AES-128-GCM) |

---

## Estructura del proyecto

```
app/src/main/
├── java/com/example/redseguridad/
│   │
│   ├── data/
│   │   ├── model/
│   │   │   ├── Post.kt                  # Modelo de datos del endpoint /posts/{id}
│   │   │   └── StorageEngine.kt         # Enum de los tres motores de persistencia
│   │   ├── network/
│   │   │   └── PostRepository.kt        # Llamadas HTTP GET y PUT a JSONPlaceholder
│   │   └── storage/
│   │       └── SecretRepository.kt      # Abstracción de los tres motores nativos
│   │
│   ├── ui/
│   │   ├── rest/
│   │   │   ├── RestApiFragment.kt       # Pantalla de conectividad REST
│   │   │   ├── RestApiViewModel.kt      # Lógica y estado de la pantalla REST
│   │   │   └── RestUiState.kt           # Estados posibles: Idle/Loading/GetSuccess/PutSuccess/Error
│   │   └── secrets/
│   │       ├── SecretsFragment.kt       # Pantalla de gestión de secretos
│   │       ├── SecretsViewModel.kt      # Lógica y estado de la pantalla de secretos
│   │       └── SecretsUiState.kt        # Estados posibles: Idle/SaveSuccess/RetrieveSuccess/Error
│   │
│   └── MainActivity.kt                  # Navegación con BottomNavigationView
│
└── res/
    ├── layout/
    │   ├── activity_main.xml            # FrameLayout + BottomNavigationView
    │   ├── fragment_rest_api.xml        # Cards GET y PUT con campos y ProgressBar
    │   └── fragment_secrets.xml         # Campos Llave/Valor, Spinner de motor, card resultado
    ├── menu/
    │   └── bottom_nav_menu.xml          # Dos tabs: REST API y Secretos
    └── values/
        └── strings.xml                  # Array storage_engines para el Spinner
```

---

## Módulo 1 — Conectividad REST

La pantalla **REST API** permite interactuar con el endpoint público `https://jsonplaceholder.typicode.com/posts`.

### Flujo GET

1. El usuario ingresa un ID numérico (1–100) y toca **Consultar**.
2. Todos los campos y botones se deshabilitan y el `ProgressBar` se hace visible mientras la petición está en tránsito.
3. `RestApiViewModel` lanza una coroutine en `Dispatchers.IO` y delega a `PostRepository.getPost(id)`.
4. `PostRepository` abre una conexión `HttpURLConnection` con método `GET`, lee el `inputStream` y parsea la respuesta con `org.json.JSONObject`.
5. El resultado se emite como `RestUiState.GetSuccess(post)` via `StateFlow`.
6. El Fragment colecta el estado con `repeatOnLifecycle(STARTED)` y pinta `title` y `body` en los campos editables, mostrando `✅ 200 OK`.

### Flujo PUT

1. Con un post ya cargado, el usuario edita título o cuerpo y toca **Actualizar**.
2. El ViewModel construye el `Post` modificado usando `post.copy(title, body)` y llama a `PostRepository.updatePost(post)`.
3. `PostRepository` serializa el objeto a JSON, lo envía como body del `PUT` y lee el `responseCode`.
4. JSONPlaceholder responde `200 OK` — la app captura ese código y emite `RestUiState.PutSuccess(200)`.
5. El Fragment actualiza el estado visual con `✅ 200 OK — Post actualizado`.

> **Nota:** JSONPlaceholder es una API de pruebas que no persiste cambios en su servidor. El PUT envía el JSON correctamente y recibe una respuesta HTTP real; el servidor simplemente no almacena la modificación por diseño de la API pública.

---

## Módulo 3 — Almacenamiento Seguro

La pantalla **Secretos** permite guardar y recuperar pares clave-valor en cualquiera de los tres motores de persistencia nativos. La UI opera en modo transaccional: el usuario elige el motor, ingresa la llave y el valor, y ejecuta la acción.

### Motor 1 — SharedPreferences

Almacenamiento clave-valor síncrono. Adecuado para preferencias de interfaz, configuraciones de sesión y banderas de estado sin información crítica. Los datos se almacenan en texto plano en un archivo XML del directorio privado de la aplicación.

```
Archivo: rs_shared_prefs.xml
Cifrado: ninguno
Acceso:  síncrono / XML directo
```

### Motor 2 — Jetpack DataStore

Alternativa moderna y reactiva a SharedPreferences. Utiliza Kotlin Coroutines y `Flow` internamente, lo que garantiza que las operaciones de I/O nunca bloqueen el hilo principal. Las escrituras son atómicas — no hay riesgo de archivos corruptos ante cierres abruptos.

```
Archivo: rs_datastore.preferences_pb (Protocol Buffers)
Cifrado: ninguno
Acceso:  asíncrono / Kotlin Flow (suspend)
```

### Motor 3 — EncryptedSharedPreferences

Extiende la API de SharedPreferences con cifrado automático antes de escribir al disco. Utiliza el Android Keystore para gestionar la clave maestra y aplica dos esquemas de cifrado:

- **Llaves:** AES-256-SIV (cifrado determinístico que permite búsqueda)
- **Valores:** AES-128-GCM (cifrado autenticado con nonce aleatorio)

```
Archivo: rs_encrypted_prefs.xml (contenido cifrado)
Cifrado: AES-256-SIV (llaves) + AES-128-GCM (valores)
Acceso:  cifrado automático sobre API de SharedPreferences
```

### Persistencia entre sesiones

Los tres motores persisten los datos en el almacenamiento interno del dispositivo. Al cerrar y reabrir la aplicación, los secretos guardados pueden recuperarse con la misma llave y el mismo motor con el que fueron almacenados.

---

## Arquitectura MVVM

```
Fragment  →  ViewModel  →  Repository  →  API / Sistema
   ↑              |
   └── StateFlow ─┘
```

- **Fragment:** observa el `StateFlow` del ViewModel con `repeatOnLifecycle(STARTED)`. No contiene lógica de negocio.
- **ViewModel:** lanza coroutines en `Dispatchers.IO`, mantiene el estado con `MutableStateFlow` y sobrevive a rotaciones de pantalla.
- **Repository:** única clase con acceso a red o almacenamiento. Retorna `Result<T>` en lugar de lanzar excepciones.
- **Model:** `Post` (data class) y `StorageEngine` (enum con nombre de display) sin lógica propia.

---

## Dependencias

```toml
# Arquitectura
kotlinx-coroutines-android  = "1.8.1"
lifecycle-viewmodel-ktx     = "2.8.7"
lifecycle-runtime-ktx       = "2.8.7"
androidx-fragment-ktx       = "1.8.2"

# Persistencia
datastore-preferences       = "1.1.1"
security-crypto             = "1.0.0"

# UI
material                    = "1.14.0"
androidx-constraintlayout   = "2.2.1"
```

Las llamadas HTTP y el parseo JSON utilizan exclusivamente APIs nativas del SDK de Android (`HttpURLConnection`, `org.json`) sin dependencias externas adicionales.
