package com.example.uniride.interfaces

import com.example.uniride.model.*
import com.example.uniride.model.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/api/usuarios")
    suspend fun obtenerUsuarios(): List<Usuario>

    @GET("/api/usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Long): Usuario

    @POST("/api/usuarios")
    suspend fun crearUsuario(@Body dto: UsuarioDTO): Usuario

    @GET("/api/usuarios/correo/{correo}")
    suspend fun buscarPorCorreo(@Path("correo") correo: String): Usuario

    // Actualiza perfil incluyendo rol y fotoPerfil
    @PATCH("/api/usuarios/{id}/perfil")
    suspend fun actualizarPerfil(
        @Path("id") id: Long,
        @Body dto: ActualizarPerfilDTO
    ): Usuario

    @PATCH("/api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): String

    @PATCH("/api/usuarios/{id}/activo")
    suspend fun toggleActivo(@Path("id") id: Long): Usuario

    @PUT("/api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Usuario

    @DELETE("/api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Long)

    @GET("/api/sedes")
    suspend fun obtenerSedes(): List<Sede>

    @POST("/api/sedes")
    suspend fun crearSede(@Body body: Map<String, String>): Sede

    @DELETE("/api/sedes/{id}")
    suspend fun eliminarSede(@Path("id") id: Long)

    @GET("/api/vehiculos")
    suspend fun obtenerVehiculos(): List<Vehiculo>

    @GET("/api/vehiculos/usuario/{idUsuario}")
    suspend fun vehiculosPorUsuario(@Path("idUsuario") id: Long): List<Vehiculo>

    @POST("/api/vehiculos")
    suspend fun crearVehiculo(@Body dto: VehiculoDTO): Vehiculo

    @PUT("/api/vehiculos/{id}")
    suspend fun editarVehiculo(@Path("id") id: Long, @Body dto: VehiculoDTO): Vehiculo

    @DELETE("/api/vehiculos/{id}")
    suspend fun eliminarVehiculo(@Path("id") id: Long)

    @GET("/api/viajes")
    suspend fun obtenerViajes(): List<Viaje>

    @GET("/api/viajes/{id}")
    suspend fun obtenerViaje(@Path("id") id: Long): Viaje

    @GET("/api/viajes/estado/{estado}")
    suspend fun viajesPorEstado(@Path("estado") estado: String): List<Viaje>

    @GET("/api/viajes/vehiculo/{idVehiculo}")
    suspend fun viajesPorVehiculo(@Path("idVehiculo") id: Long): List<Viaje>

    @GET("/api/viajes/sede/{idSede}")
    suspend fun viajesPorSede(@Path("idSede") id: Long): List<Viaje>

    @GET("/api/viajes/ciudad/{ciudad}")
    suspend fun viajesPorCiudad(@Path("ciudad") ciudad: String): List<Viaje>

    @POST("/api/viajes")
    suspend fun crearViaje(@Body dto: ViajeDTO): Viaje

    @PUT("/api/viajes/{id}")
    suspend fun editarViaje(@Path("id") id: Long, @Body dto: ViajeDTO): Viaje

    @DELETE("/api/viajes/{id}")
    suspend fun eliminarViaje(@Path("id") id: Long)

    @PATCH("/api/viajes/{id}/cancelar")
    suspend fun cancelarViaje(@Path("id") id: Long): Viaje

    @GET("/api/reservas")
    suspend fun obtenerReservas(): List<Reserva>

    @GET("/api/reservas/usuario/{idUsuario}")
    suspend fun reservasPorUsuario(@Path("idUsuario") id: Long): List<Reserva>

    @GET("/api/reservas/viaje/{idViaje}")
    suspend fun reservasPorViaje(@Path("idViaje") id: Long): List<Reserva>

    @POST("/api/reservas")
    suspend fun crearReserva(@Body dto: ReservaDTO): Response<Any>

    @PATCH("/api/reservas/{id}/confirmar")
    suspend fun confirmarReserva(@Path("id") id: Long): Reserva

    @PATCH("/api/reservas/{id}/cancelar")
    suspend fun cancelarReserva(@Path("id") id: Long): String

    @GET("/api/reportes")
    suspend fun obtenerReportes(): List<Reporte>

    @PUT("/api/reportes/{id}")
    suspend fun actualizarReporte(@Path("id") id: Long, @Body body: Map<String, String>): Reporte

    @GET("/api/notificaciones")
    suspend fun obtenerNotificaciones(): List<Notificacion>

    @GET("/api/notificaciones/usuario/{idUsuario}")
    suspend fun notificacionesPorUsuario(@Path("idUsuario") id: Long): List<Notificacion>

    @POST("/api/notificaciones")
    suspend fun crearNotificacion(@Body body: Map<String, String>): Notificacion

    @PATCH("/api/notificaciones/{id}/leer")
    suspend fun marcarLeida(@Path("id") id: Long)

    @POST("/api/calificaciones")
    suspend fun calificarConductor(@Body body: Map<String, Any>): Any

    @GET("/api/calificaciones/conductor/{idConductor}")
    suspend fun calificacionesConductor(@Path("idConductor") id: Long): List<Calificacion>

    @GET("/api/calificaciones/conductor/{idConductor}/promedio")
    suspend fun promedioConductor(@Path("idConductor") id: Long): Double

    @GET("/api/calificaciones/reserva/{idReserva}/calificada")
    suspend fun yaCalificada(@Path("idReserva") id: Long): Boolean

    // Agrega estos endpoints a los existentes:

    @DELETE("/api/notificaciones/{id}")
    suspend fun eliminarNotificacion(@Path("id") id: Long)

    @PATCH("/api/reservas/{id}/calificada")
    suspend fun marcarReservaCalificada(@Path("id") id: Long)

    @GET("/api/reservas/{id}")
    suspend fun obtenerReserva(@Path("id") id: Long): Reserva

    @PATCH("/api/viajes/{id}/completar")
    suspend fun completarViaje(@Path("id") id: Long): Viaje
}

object RetrofitClient {
    private const val BASE_URL = "https://uniride-ja9a.onrender.com"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}