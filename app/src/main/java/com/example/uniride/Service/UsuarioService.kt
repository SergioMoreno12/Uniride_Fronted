package com.example.uniride.Service

import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Usuario
import com.example.uniride.model.dto.UsuarioDTO

class UsuarioService {

    private val repository = RetrofitClient.apiService

    suspend fun crearUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        telefono: String,
        rol: String
    ): Usuario {
        return repository.crearUsuario(
            dto = UsuarioDTO(
                nombre     = nombre,
                correo     = correo,
                contrasena = contrasena,
                telefono   = telefono.ifBlank { null },
                rol        = rol
            )
        )
    }

    suspend fun obtenerUsuario(id: Long): Usuario {
        return repository.obtenerUsuario(id)
    }

    suspend fun buscarPorCorreo(correo: String): Usuario {
        return repository.buscarPorCorreo(correo)
    }
}