package com.example.uniride.Repository

import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Viaje
import com.example.uniride.model.dto.ViajeDTO

class ViajeRepository {

    suspend fun obtenerDisponibles(): List<Viaje> =
        RetrofitClient.apiService.viajesPorEstado("disponible")

    suspend fun viajesPorVehiculo(idVehiculo: Long): List<Viaje> =
        RetrofitClient.apiService.viajesPorVehiculo(idVehiculo)

    suspend fun crearViaje(dto: ViajeDTO): Viaje =
        RetrofitClient.apiService.crearViaje(dto)
}