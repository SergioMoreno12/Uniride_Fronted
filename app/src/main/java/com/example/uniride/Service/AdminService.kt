package com.example.uniride.Service

import com.example.uniride.Repository.AdminRepository
import com.example.uniride.model.*
import okhttp3.ResponseBody
import retrofit2.Response

class AdminService {
    private val repository = AdminRepository()

    // Usuarios
    suspend fun obtenerUsuarios(): List<Usuario> = repository.obtenerUsuarios()
    suspend fun cambiarRol(id: Long, rol: String): Usuario = repository.actualizarRolUsuario(id, rol)
    suspend fun eliminarUsuario(id: Long) = repository.eliminarUsuario(id)
    suspend fun toggleActivo(id: Long): Usuario = repository.toggleActivo(id)

    // Viajes
    suspend fun obtenerViajes(): List<Viaje> = repository.obtenerViajes()
    suspend fun eliminarViaje(id: Long) = repository.eliminarViaje(id)

    // Vehículos
    suspend fun obtenerVehiculos(): List<Vehiculo> = repository.obtenerVehiculos()
    suspend fun eliminarVehiculo(id: Long) = repository.eliminarVehiculo(id)

    // Sedes
    suspend fun obtenerSedes(): List<Sede> = repository.obtenerSedes()
    suspend fun crearSede(nombre: String, ciudad: String): Sede = repository.crearSede(nombre, ciudad)
    suspend fun eliminarSede(id: Long) = repository.eliminarSede(id)

    // Reservas
    suspend fun obtenerReservas(): List<Reserva> = repository.obtenerReservas()

    // Reportes
    suspend fun obtenerReportes(): List<Reporte> = repository.obtenerReportes()

    // FIX: tipo de retorno actualizado a Response<ResponseBody>
    suspend fun actualizarEstadoReporte(id: Long, estado: String): Response<ResponseBody> =
        repository.actualizarEstadoReporte(id, estado)

    // Notificaciones
    suspend fun obtenerNotificaciones(): List<Notificacion> = repository.obtenerNotificaciones()
    suspend fun crearNotificacion(titulo: String, mensaje: String, destinatarios: String): Notificacion =
        repository.crearNotificacion(titulo, mensaje, destinatarios)
}