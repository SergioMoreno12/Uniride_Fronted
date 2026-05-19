package com.example.uniride.Repository

import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.dto.LoginRequest
import com.example.uniride.model.dto.LoginResponse
import retrofit2.Response

class AuthRepository {
    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return RetrofitClient.apiService.login(request)
    }
}