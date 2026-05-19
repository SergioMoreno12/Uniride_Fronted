package com.example.uniride.Service

import com.example.uniride.Repository.AuthRepository
import com.example.uniride.model.dto.LoginRequest
import com.example.uniride.model.dto.LoginResponse
import retrofit2.Response

class AuthService {
    private val repository = AuthRepository()

    suspend fun login(correo: String, contrasena: String): Response<LoginResponse> {
        return repository.login(LoginRequest(correo, contrasena))
    }
}