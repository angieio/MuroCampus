package com.tecsup.murocampus.model

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class Usuario(
    val id: Int,
    val name: String,
    val email: String
)
interface UsuarioApi {
    @GET("users")
    suspend fun listar(): List<Usuario>
}
interface UsuarioRepository {
    suspend fun listar(): Result<List<Usuario>>
}

class UsuarioRepositoryRemote(private val api: UsuarioApi) : UsuarioRepository {
    override suspend fun listar(): Result<List<Usuario>> =
        try {
            Result.success(api.listar())
        } catch (e: CancellationException) {
            throw e // nunca se traga la cancelación
        } catch (e: Exception) {
            Result.failure(e)
        }
}