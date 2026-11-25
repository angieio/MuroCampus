package com.tecsup.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.murocampus.data.PostRepository
import com.tecsup.murocampus.model.Comentario
import com.tecsup.murocampus.model.NuevoPost
import com.tecsup.murocampus.model.Post
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleUi(val post: Post, val comentarios: List<Comentario>)

data class EliminacionUi(val postId: Int, val estado: UiState<Unit>)

class PostsViewModel(private val repo: PostRepository) : ViewModel() {
    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista = _lista.asStateFlow()

    private val _usuarioFiltro = MutableStateFlow<Int?>(null)
    val usuarioFiltro = _usuarioFiltro.asStateFlow()
    private var cargaLista: Job? = null

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle = _detalle.asStateFlow()
    private var cargaDetalle: Job? = null

    private val _publicacion = MutableStateFlow<UiState<Post>?>(null)
    val publicacion = _publicacion.asStateFlow()

    private val idsEliminados = mutableSetOf<Int>()
    private val _eliminacion = MutableStateFlow<EliminacionUi?>(null)
    val eliminacion = _eliminacion.asStateFlow()

    init {
        cargar()
    }

    fun filtrar(userId: Int?) {
        if (_usuarioFiltro.value == userId) return
        _usuarioFiltro.value = userId
        cargar()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun cargar() {
        cargaLista?.cancel()
        val userId = _usuarioFiltro.value
        _lista.value = UiState.Cargando
        cargaLista = viewModelScope.launch {
            val resultado = if (userId == null) repo.listar() else repo.porUsuario(userId)
            ensureActive()
            resultado
                .onSuccess { posts -> _lista.value = UiState.Exito(posts.filterNot { it.id in idsEliminados }) }
                .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun abrir(id: Int) {
        cargaDetalle?.cancel()
        _detalle.value = UiState.Cargando
        cargaDetalle = viewModelScope.launch {
            try {
                val datos = coroutineScope {
                    val post = async { repo.detalle(id).getOrThrow() }
                    val comentarios = async { repo.comentarios(id).getOrThrow() }
                    DetalleUi(post.await(), comentarios.await())
                }
                _detalle.value = UiState.Exito(datos)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detalle.value = UiState.Error(mensajeDe(e))
            }
        }
    }

    fun prepararPublicacion() {
        if (_publicacion.value != UiState.Cargando) {
            _publicacion.value = null
        }
    }

    fun publicar(titulo: String, texto: String) {
        if (titulo.isBlank() || texto.isBlank() || _publicacion.value == UiState.Cargando) return

        _publicacion.value = UiState.Cargando
        viewModelScope.launch {
            repo.crear(NuevoPost(userId = 1, title = titulo, body = texto))
                .onSuccess { _publicacion.value = UiState.Exito(it) }
                .onFailure { _publicacion.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun consumirEliminacion(id: Int) {
        val actual = _eliminacion.value
        if (actual?.postId == id && actual.estado is UiState.Exito) {
            _eliminacion.value = null
        }
    }

    fun eliminar(id: Int) {
        if (_eliminacion.value?.estado == UiState.Cargando) return
        _eliminacion.value = EliminacionUi(id, UiState.Cargando)
        viewModelScope.launch {
            repo.eliminar(id)
                .onSuccess {
                    idsEliminados.add(id)
                    val actual = _lista.value
                    if (actual is UiState.Exito) {
                        _lista.value = UiState.Exito(actual.datos.filterNot { it.id == id })
                    }
                    _eliminacion.value = EliminacionUi(id, UiState.Exito(Unit))
                }
                .onFailure { _eliminacion.value = EliminacionUi(id, UiState.Error(mensajeDe(it))) }
        }
    }
}
