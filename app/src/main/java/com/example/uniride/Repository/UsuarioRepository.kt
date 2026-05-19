package com.example.uniride.Repository

import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Usuario
import com.example.uniride.model.dto.UsuarioDTO

class UsuarioRepository {
    suspend fun crearUsuario(dto: UsuarioDTO): Usuario {
        return RetrofitClient.apiService.crearUsuario(dto)
    }
    suspend fun obtenerUsuario(id: Long): Usuario {
        return RetrofitClient.apiService.obtenerUsuario(id)
    }
}