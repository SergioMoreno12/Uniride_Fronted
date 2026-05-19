package com.example.uniride.Service

import com.example.uniride.Repository.UsuarioRepository
import com.example.uniride.model.Usuario
import com.example.uniride.model.dto.UsuarioDTO
import java.time.LocalDate

class UsuarioService {
    private val repository = UsuarioRepository()

    suspend fun crearUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        telefono: String,
        rol: String
    ): Usuario {
        return repository.crearUsuario(
            UsuarioDTO(
                nombre = nombre,
                correo = correo,
                contrasena = contrasena,
                telefono = telefono.ifBlank { null },
                fechaRegistro = LocalDate.now().toString(),
                rol = rol
            )
        )
    }

    suspend fun obtenerUsuario(id: Long): Usuario {
        return repository.obtenerUsuario(id)
    }
}