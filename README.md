# MuroCampus

**Nombre:** Angieluz Vasquez Macalupu

## Descripción

MuroCampus es una aplicación Android que consulta publicaciones y comentarios de JSONPlaceholder. Permite ver el detalle de una publicación, filtrar la lista por usuario y enviar una publicación nueva de forma simulada. La app está desarrollada en Kotlin con Jetpack Compose y usa la arquitectura MVVM con Repository. Retrofit y OkHttp realizan las solicitudes HTTP, Kotlin Serialization convierte el JSON y el ViewModel expone mediante `StateFlow` los estados de carga, éxito y error que muestra la interfaz.

## Árbol de paquetes

```text
com.tecsup.murocampus/
├── MainActivity.kt
├── data/
│   ├── PostRepository.kt
│   └── remote/
│       ├── PostApi.kt
│       └── RetrofitClient.kt
├── model/
│   └── Modelos.kt
├── ui/
│   ├── Componentes.kt
│   ├── PantallaLista.kt
│   ├── PantallaDetalle.kt
│   ├── PantallaCrear.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── viewmodel/
    ├── PostsViewModel.kt
    ├── PostsViewModelFactory.kt
    └── UiState.kt
```

## Diagrama MVVM

```text
Acción del usuario
        │
        ▼
Pantallas Compose
Lista · Detalle · Crear publicación
        │ cargar(), filtrar(), abrir() o publicar()
        ▼
PostsViewModel
Gestiona las acciones y el estado de la UI
        │ usa la interfaz PostRepository
        ▼
PostRepositoryRemote
Ejecuta la operación y devuelve Result
        │ llama a PostApi
        ▼
RetrofitClient · Retrofit y OkHttp
Envía GET o POST y convierte la respuesta JSON
        │
        ▼
JSONPlaceholder
Servicio REST de publicaciones y comentarios
```

```text
Regreso de los datos

JSONPlaceholder ── respuesta HTTP ──► Retrofit
Retrofit ── objeto Kotlin o excepción ──► Repositorio
Repositorio ── Result de éxito o fallo ──► PostsViewModel
PostsViewModel ── StateFlow<UiState> ──► Pantalla Compose
```

## Capturas

<table>
  <tr>
    <td align="center"><strong>Lista de publicaciones</strong><br><img src="https://github.com/user-attachments/assets/43f35fdf-252c-4225-b6ff-d718ab8968a3" alt="Lista de publicaciones" width="240"></td>
    <td align="center"><strong>Detalle y comentarios</strong><br><img src="https://github.com/user-attachments/assets/7abfd6c9-c8e5-461f-ac48-1bd0f7f5a756" alt="Detalle y comentarios" width="240"></td>
  </tr>
  <tr>
    <td align="center"><strong>Filtro por usuario</strong><br><img src="https://github.com/user-attachments/assets/a91b6f9f-ac8a-473b-b02a-2f0aaff4ee57" alt="Filtro por usuario" width="240"></td>
    <td align="center"><strong>Crear publicación</strong><br><img src="https://github.com/user-attachments/assets/66f9b5a9-e44d-49d7-879d-eb3446e8e28b" alt="Formulario con confirmación de publicación" width="240"></td>
  </tr>
  <tr>
    <td align="center"><strong>Error de conexión</strong><br><img src="https://github.com/user-attachments/assets/9d4487ce-fee3-4980-8f32-a430565ee9c8" alt="Error de conexión con botón Reintentar" width="240"></td>
    <td></td>
  </tr>
</table>

## Verificación del paso 10

| Prueba | Cómo provocarla | Resultado esperado | OK |
| --- | --- | --- | :---: |
| Sin internet | Activa el modo avión y abre la app | **Sin conexión a internet** y botón **Reintentar** | ✅ |
| Reintentar | Desactiva el modo avión y pulsa **Reintentar** | Carga la lista | ✅ |
| Recurso inexistente | Cambia temporalmente `posts/{id}` por `posts/9999` en detalle | **Error del servidor (404)** | ✅ |
| JSON inesperado | Quita temporalmente un campo de `Post` que sí llega o agrega uno obligatorio que no llega | Con `ignoreUnknownKeys` se ignora el campo extra. Si falta un campo obligatorio, aparece **Respuesta inesperada**. | ✅ |
| Cancelación | Abre el detalle y vuelve rápido | No hay errores en Logcat | ✅ |
| Lista y detalle | Abre varias publicaciones | Siempre 5 comentarios | ✅ |
| Filtro | Elige cada usuario | Siempre 10 publicaciones | ✅ |
| Crear | Envía una publicación | **Publicado con id 101** | ✅ |
| Código | Revisa `viewmodel/` | No hay referencias a Retrofit, `PostApi` ni `HttpException` | ✅ |

## Para pensar

1. ¿Qué diferencia hay entre GET y POST? ¿Por qué `crear` lleva `@Body` y `porUsuario` lleva `@Query`?

   GET sirve para consultar datos y POST se usa para enviar datos y crear un recurso. En el método `crear`, se usa `@Body` para enviar el objeto `NuevoPost` como JSON en el cuerpo de la solicitud. En `porUsuario`, se usa `@Query` para agregar el `userId` a la URL, como en `posts?userId=2`.

2. ¿Por qué la ruta es `"posts"` y no `"/posts"`?

   `"posts"` es una ruta relativa porque se agrega al camino de la URL base. Si se escribe `"/posts"`, la ruta empieza desde la raíz del dominio. Con la URL base actual, las dos llegan a `/posts`. La diferencia se ve si la base es `https://ejemplo.com/api/`, porque `"posts"` lleva a `/api/posts` y `"/posts"` lleva a `/posts`.

3. ¿Por qué el repositorio devuelve `Result` en vez de dejar que la excepción llegue al ViewModel?

   Se usa `Result` para que el ViewModel pueda saber si la consulta salió bien o si ocurrió un error. El repositorio captura los errores de red y el ViewModel usa el resultado para actualizar el `UiState`. Si se cancela la corrutina, se vuelve a lanzar la excepción de cancelación para que la corrutina pueda detenerse correctamente.

4. ¿Qué pasa si olvidas el permiso `INTERNET`? ¿Y si olvidas `ignoreUnknownKeys` y el servidor agrega un campo nuevo?

   Si falta el permiso `INTERNET`, la app no puede hacer solicitudes a la red. También puede ocurrir un error al leer el JSON si el servidor agrega un campo que el modelo no tiene. Con `ignoreUnknownKeys = true`, el conversor ignora ese campo y sigue leyendo los demás datos.

5. ¿Por qué la pantalla de lista usa un `when` sobre `UiState` en vez de un `if` con tres variables?

   `UiState` permite que la pantalla tenga un solo estado a la vez. Con el `when`, se define qué mostrar en cada caso, ya sea `Cargando`, `Exito` o `Error`. Si se usaran tres variables independientes, podría pasar que la pantalla muestre la carga y el error al mismo tiempo.

6. JSONPlaceholder no guarda los POST. ¿Cómo cambiaría tu app si usaras un servicio real?

   Con un servicio real, el servidor guardaría la publicación y devolvería su ID. Después de recibir una respuesta exitosa, la app podría volver a consultar la lista o agregar la publicación devuelta a la lista actual si coincide con el filtro. Como la publicación quedó guardada en el servidor, también se podría consultar más adelante.
