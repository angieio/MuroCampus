package com.tecsup.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.UiState

@Composable
fun PantallaDetalle(vm: PostsViewModel, id: Int, onEliminado: () -> Unit, modifier: Modifier = Modifier) {
    val estado by vm.detalle.collectAsStateWithLifecycle()
    val operacion by vm.eliminacion.collectAsStateWithLifecycle()
    val eliminacion = operacion?.takeIf { it.postId == id }?.estado
    val eliminando = operacion?.estado == UiState.Cargando
    var mostrarConfirmacion by rememberSaveable(id) { mutableStateOf(false) }

    LaunchedEffect(eliminacion) {
        if (eliminacion is UiState.Exito) {
            vm.consumirEliminacion(id)
            onEliminado()
        }
    }

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { if (eliminacion !is UiState.Cargando) mostrarConfirmacion = false },
            title = { Text("Eliminar publicación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta publicación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        vm.eliminar(id)
                    },
                    enabled = !eliminando
                ) {
                    if (eliminacion is UiState.Cargando) {
                        Text("Eliminando...")
                    } else {
                        Text("Eliminar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacion = false },
                    enabled = !eliminando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    when (val actual = estado) {
        UiState.Cargando -> Box(
            modifier = modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Cargando()
        }

        is UiState.Error -> Box(
            modifier = modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ErrorConReintento(mensaje = actual.mensaje, onReintentar = { vm.abrir(id) })
        }

        is UiState.Exito -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = colors.surface)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = actual.datos.post.title,
                            style = typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = actual.datos.post.body, style = typography.bodyMedium)

                        Button(
                            onClick = {
                                mostrarConfirmacion = true
                            },
                            enabled = !eliminando,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                        ) {
                            Text(if (eliminacion == UiState.Cargando) "Eliminando..." else "Eliminar publicación")
                        }

                        if (eliminacion is UiState.Error) {
                            Text(
                                text = eliminacion.mensaje,
                                color = colors.error,
                                style = typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Comentarios (${actual.datos.comentarios.size})",
                    style = typography.titleMedium,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            items(actual.datos.comentarios, key = { it.id }) { comentario ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = comentario.email,
                            style = typography.labelLarge,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = comentario.body, style = typography.bodyMedium)
                    }
                }
            }
        }
    }
}
