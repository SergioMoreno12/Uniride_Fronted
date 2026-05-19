package com.example.uniride.Service

import com.example.uniride.Repository.SedeRepository
import com.example.uniride.model.Sede

class SedeService {
    private val repository = SedeRepository()

    suspend fun obtenerSedes(): List<Sede> {
        return repository.obtenerSedes()
    }
}