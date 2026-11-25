package com.tecsup.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.UiState

@Composable
fun PantallaLista(vm: PostsViewModel, onAbrirPost: (Int) -> Unit, modifier: Modifier = Modifier) {
    val estado by vm.lista.collectAsStateWithLifecycle()
    val usuarioFiltro by vm.usuarioFiltro.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Column(modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf(null) + (1..10)) { userId ->
                FilterChip(
                    selected = usuarioFiltro == userId,
                    onClick = { vm.filtrar(userId) },
                    label = { Text(if (userId == null) "Todos" else "Usuario $userId") },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = colors.primary,
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = usuarioFiltro == userId,
                        borderColor = colors.primary.copy(alpha = 0.25f)
                    )
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (val actual = estado) {
                UiState.Cargando -> Cargando(modifier = Modifier.padding(16.dp))

                is UiState.Error -> ErrorConReintento(
                    mensaje = actual.mensaje,
                    onReintentar = vm::cargar,
                    modifier = Modifier.padding(16.dp)
                )

                is UiState.Exito -> {
                    if (actual.datos.isEmpty()) {
                        Text("Sin publicaciones")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(actual.datos, key = { it.id }) { post ->
                                TarjetaPost(
                                    userId = post.userId,
                                    title = post.title,
                                    body = post.body,
                                    onClick = { onAbrirPost(post.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
