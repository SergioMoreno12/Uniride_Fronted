package com.example.uniride.Service

import com.example.uniride.Repository.ViajeRepository
import com.example.uniride.model.Viaje
import com.example.uniride.model.dto.ViajeDTO

class ViajeService {
    private val repository = ViajeRepository()

    suspend fun obtenerDisponibles(): List<Viaje> {
        return repository.obtenerDisponibles()
    }

    suspend fun viajesPorVehiculo(idVehiculo: Long): List<Viaje> {
        return repository.viajesPorVehiculo(idVehiculo)
    }

    suspend fun crearViaje(dto: ViajeDTO): Viaje {
        return repository.crearViaje(dto)
    }
}