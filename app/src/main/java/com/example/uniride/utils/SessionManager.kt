package com.example.uniride.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Singleton en memoria que expone el token JWT y datos de sesión.
 * El RetrofitClient lo lee en cada request para adjuntar el header Authorization.
 */
object SessionManager {

    /** Token JWT activo. Se actualiza tras cada login y se borra en logout. */
    var token: String? by mutableStateOf(null)

    /** ID del usuario en sesión. */
    var idUsuario: Long? by mutableStateOf(null)

    /** Rol del usuario en sesión: "pasajero", "conductor" o "administrador". */
    var rol: String? by mutableStateOf(null)

    /** Nombre del usuario en sesión. */
    var nombre: String? by mutableStateOf(null)

    /** Inicializa el estado desde los datos persistidos en SharedPreferences. */
    fun inicializar(
        token: String?,
        idUsuario: Long?,
        rol: String?,
        nombre: String?
    ) {
        this.token     = token
        this.idUsuario = idUsuario
        this.rol       = rol
        this.nombre    = nombre
    }

    /** Limpia toda la sesión en memoria. */
    fun limpiar() {
        token     = null
        idUsuario = null
        rol       = null
        nombre    = null
    }

    /** Retorna true si hay una sesión activa con token. */
    fun haySession(): Boolean = token != null && idUsuario != null
}