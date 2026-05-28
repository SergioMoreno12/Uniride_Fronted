package com.example.uniride.Service

import com.example.uniride.Repository.ReservaRepository
import com.example.uniride.model.Reserva
import com.example.uniride.model.dto.ReservaDTO
import retrofit2.Response
import java.time.LocalDate

class ReservaService {
    private val repository = ReservaRepository()

    suspend fun reservasPorUsuario(idUsuario: Long): List<Reserva> =
        repository.reservasPorUsuario(idUsuario)

    suspend fun reservasPorViaje(idViaje: Long): List<Reserva> =
        repository.reservasPorViaje(idViaje)

    suspend fun crearReserva(idUsuario: Long, idViaje: Long): Response<Any> =
        repository.crearReserva(
            ReservaDTO(
                idUsuario = idUsuario,
                idViaje = idViaje,
                fechaReserva = LocalDate.now().toString(),
                confirmada = false
            )
        )

    suspend fun confirmarReserva(idReserva: Long): Reserva =
        repository.confirmarReserva(idReserva)

    suspend fun cancelarReserva(idReserva: Long): retrofit2.Response<Void> =
        repository.cancelarReserva(idReserva)

}