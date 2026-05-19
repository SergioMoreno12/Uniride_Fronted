package com.example.uniride.Repository

import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Reserva
import com.example.uniride.model.dto.ReservaDTO
import retrofit2.Response

class ReservaRepository {

    suspend fun reservasPorUsuario(idUsuario: Long): List<Reserva> =
        RetrofitClient.apiService.reservasPorUsuario(idUsuario)

    suspend fun reservasPorViaje(idViaje: Long): List<Reserva> =
        RetrofitClient.apiService.reservasPorViaje(idViaje)

    suspend fun crearReserva(dto: ReservaDTO): Response<Any> =
        RetrofitClient.apiService.crearReserva(dto)

    suspend fun confirmarReserva(idReserva: Long): Reserva =
        RetrofitClient.apiService.confirmarReserva(idReserva)

    suspend fun cancelarReserva(idReserva: Long): String =
        RetrofitClient.apiService.cancelarReserva(idReserva)
}