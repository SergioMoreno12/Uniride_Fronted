package com.example.uniride.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.dto.LoginRequest
import com.example.uniride.model.dto.LoginResponse
import com.example.uniride.model.dto.UsuarioDTO
import com.example.uniride.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("uniride_session", Context.MODE_PRIVATE)

    private val _loginResult = MutableLiveData<LoginResponse?>(null)
    val loginResult: MutableLiveData<LoginResponse?> = _loginResult

    private val _loginError = MutableLiveData<String?>(null)
    val loginError: MutableLiveData<String?> = _loginError

    // (lógica de negocio movida al ViewModel, fuera de la pantalla)
    private val _registroExito = MutableLiveData<Boolean>(false)
    val registroExito: MutableLiveData<Boolean> = _registroExito

    private val _registroError = MutableLiveData<String?>(null)
    val registroError: MutableLiveData<String?> = _registroError

    private val _registroCargando = MutableLiveData(false)
    val registroCargando: MutableLiveData<Boolean> = _registroCargando

    var sesionActual: LoginResponse? = cargarSesionGuardada()
        private set

    init {
        sesionActual?.let { sesion ->
            SessionManager.inicializar(
                token     = sesion.token,
                idUsuario = sesion.idUsuario,
                rol       = sesion.rol,
                nombre    = sesion.nombre
            )
        }
    }

    private fun cargarSesionGuardada(): LoginResponse? {
        val id = prefs.getLong("idUsuario", -1L)
        if (id == -1L) return null
        return LoginResponse(
            mensaje    = "ok",
            rol        = prefs.getString("rol", "pasajero") ?: "pasajero",
            idUsuario  = id,
            nombre     = prefs.getString("nombre", "") ?: "",
            fotoPerfil = prefs.getString("fotoPerfil", null),
            token      = prefs.getString("token", null)
        )
    }

    private fun guardarSesion(sesion: LoginResponse) {
        prefs.edit()
            .putLong("idUsuario",    sesion.idUsuario)
            .putString("rol",        sesion.rol)
            .putString("nombre",     sesion.nombre)
            .putString("fotoPerfil", sesion.fotoPerfil)
            .putString("token",      sesion.token)
            .apply()
    }

    private fun limpiarSesion() {
        prefs.edit().clear().apply()
    }

    fun login(correo: String, contrasena: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.login(LoginRequest(correo, contrasena))
                }

                if (response.isSuccessful) {
                    val body = response.body()!!

                    if (body.rol == "administrador") {
                        SessionManager.inicializar(
                            token     = body.token,
                            idUsuario = body.idUsuario,
                            rol       = body.rol,
                            nombre    = body.nombre
                        )
                        sesionActual = body
                        guardarSesion(body)
                        _loginResult.postValue(body)
                        return@launch
                    }

                    // Para pasajero/conductor, obtener datos completos del usuario
                    try {
                        val usuario = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.obtenerUsuario(body.idUsuario)
                        }
                        val sesionCompleta = LoginResponse(
                            mensaje    = body.mensaje,
                            rol        = usuario.rol,
                            idUsuario  = body.idUsuario,
                            nombre     = usuario.nombre,
                            fotoPerfil = usuario.fotoPerfil,
                            token      = body.token
                        )
                        SessionManager.inicializar(
                            token     = sesionCompleta.token,
                            idUsuario = sesionCompleta.idUsuario,
                            rol       = sesionCompleta.rol,
                            nombre    = sesionCompleta.nombre
                        )
                        sesionActual = sesionCompleta
                        guardarSesion(sesionCompleta)
                        _loginResult.postValue(sesionCompleta)
                    } catch (e: Exception) {
                        // Si falla la segunda consulta, usar lo que vino del login
                        SessionManager.inicializar(
                            token     = body.token,
                            idUsuario = body.idUsuario,
                            rol       = body.rol,
                            nombre    = body.nombre
                        )
                        sesionActual = body
                        guardarSesion(body)
                        _loginResult.postValue(body)
                    }

                } else {
                    _loginError.postValue("Correo o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _loginError.postValue("Error de conexión: ${e.message}")
            }
        }
    }

    fun register(
        nombre: String,
        correo: String,
        contrasena: String,
        telefono: String?,
        rol: String
    ) {
        // Validaciones frontend — Recomendación del profesor
        if (nombre.isBlank()) {
            _registroError.postValue("Ingresa tu nombre")
            return
        }
        if (correo.isBlank() || !correo.contains("@")) {
            _registroError.postValue("Ingresa un correo válido")
            return
        }
        if (contrasena.length < 6) {
            _registroError.postValue("La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _registroCargando.postValue(true)
            _registroError.postValue(null)
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.crearUsuario(
                        UsuarioDTO(
                            nombre     = nombre.trim(),
                            correo     = correo.trim(),
                            contrasena = contrasena,
                            telefono   = telefono?.ifBlank { null },
                            rol        = rol
                        )
                    )
                }
                _registroExito.postValue(true)
            } catch (e: Exception) {
                val mensajeError = when {
                    e.message?.contains("correo", ignoreCase = true) == true ->
                        "Este correo ya está registrado"
                    e.message?.contains("400") == true ->
                        "Datos inválidos. Verifica la información ingresada."
                    else -> "Error al registrar: ${e.message}"
                }
                _registroError.postValue(mensajeError)
            }
            _registroCargando.postValue(false)
        }
    }

    fun actualizarSesion(nueva: LoginResponse) {
        sesionActual = nueva
        guardarSesion(nueva)
        SessionManager.inicializar(
            token     = nueva.token,
            idUsuario = nueva.idUsuario,
            rol       = nueva.rol,
            nombre    = nueva.nombre
        )
        _loginResult.postValue(nueva)
    }

    fun actualizarRol(nuevoRol: String) {
        val actual = sesionActual ?: return
        val nueva  = actual.copy(rol = nuevoRol)
        actualizarSesion(nueva)
    }

    fun actualizarFoto(url: String?) {
        val actual = sesionActual ?: return
        val nueva  = actual.copy(fotoPerfil = url)
        actualizarSesion(nueva)
    }

    fun cerrarSesion() {
        sesionActual = null
        limpiarSesion()
        SessionManager.limpiar()
        _loginResult.postValue(null)
    }

    fun limpiarError() { _loginError.postValue(null) }

    fun limpiarRegistro() {
        _registroExito.postValue(false)
        _registroError.postValue(null)
    }
}