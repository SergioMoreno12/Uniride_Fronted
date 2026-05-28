package com.example.uniride.Repository

import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.*
import okhttp3.ResponseBody
import retrofit2.Response

class AdminRepository {

    suspend fun obtenerUsuarios(): List<Usuario> =
        RetrofitClient.apiService.obtenerUsuarios()

    suspend fun actualizarRolUsuario(id: Long, rol: String): Usuario =
        RetrofitClient.apiService.actualizarUsuario(id, mapOf("rol" to rol))

    suspend fun eliminarUsuario(id: Long) =
        RetrofitClient.apiService.eliminarUsuario(id)

    suspend fun toggleActivo(id: Long): Usuario =
        RetrofitClient.apiService.toggleActivo(id)

    suspend fun obtenerViajes(): List<Viaje> =
        RetrofitClient.apiService.obtenerViajes()

    suspend fun eliminarViaje(id: Long) =
        RetrofitClient.apiService.eliminarViaje(id)

    suspend fun obtenerVehiculos(): List<Vehiculo> =
        RetrofitClient.apiService.obtenerVehiculos()

    suspend fun eliminarVehiculo(id: Long) =
        RetrofitClient.apiService.eliminarVehiculo(id)

    suspend fun obtenerSedes(): List<Sede> =
        RetrofitClient.apiService.obtenerSedes()

    suspend fun crearSede(nombre: String, ciudad: String): Sede =
        RetrofitClient.apiService.crearSede(mapOf("nombreSede" to nombre, "ciudad" to ciudad))

    suspend fun eliminarSede(id: Long) =
        RetrofitClient.apiService.eliminarSede(id)

    suspend fun obtenerReservas(): List<Reserva> =
        RetrofitClient.apiService.obtenerReservas()

    suspend fun obtenerReportes(): List<Reporte> =
        RetrofitClient.apiService.obtenerReportes()

    // FIX: tipo de retorno actualizado a Response<ResponseBody>
    suspend fun actualizarEstadoReporte(id: Long, estado: String): Response<ResponseBody> =
        RetrofitClient.apiService.actualizarReporte(id, mapOf("estado" to estado))

    suspend fun obtenerNotificaciones(): List<Notificacion> =
        RetrofitClient.apiService.obtenerNotificaciones()

    suspend fun crearNotificacion(titulo: String, mensaje: String, destinatarios: String): Notificacion =
        RetrofitClient.apiService.crearNotificacion(
            mapOf("titulo" to titulo, "mensaje" to mensaje, "destinatarios" to destinatarios)
        )
}