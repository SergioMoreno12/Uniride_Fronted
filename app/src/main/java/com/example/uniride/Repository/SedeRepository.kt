package com.example.uniride.Repository

import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Sede

class SedeRepository {
    suspend fun obtenerSedes(): List<Sede> {
        return RetrofitClient.apiService.obtenerSedes()
    }
}