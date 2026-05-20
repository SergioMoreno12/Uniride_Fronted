package com.example.uniride.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.dto.LoginRequest
import com.example.uniride.model.dto.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("uniride_session", Context.MODE_PRIVATE)

    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: MutableLiveData<LoginResponse?> = _loginResult

    private val _loginError = MutableLiveData<String?>()
    val loginError: MutableLiveData<String?> = _loginError

    // Carga sesión desde SharedPreferences al arrancar
    var sesionActual: LoginResponse? = cargarSesionGuardada()
        private set

    private fun cargarSesionGuardada(): LoginResponse? {
        val id = prefs.getLong("idUsuario", -1L)
        if (id == -1L) return null
        return LoginResponse(
            mensaje    = "ok",
            rol        = prefs.getString("rol", "pasajero") ?: "pasajero",
            idUsuario  = id,
            nombre     = prefs.getString("nombre", "") ?: "",
            fotoPerfil = prefs.getString("fotoPerfil", null)
        )
    }

    private fun guardarSesion(sesion: LoginResponse) {
        prefs.edit()
            .putLong("idUsuario",  sesion.idUsuario)
            .putString("rol",      sesion.rol)
            .putString("nombre",   sesion.nombre)
            .putString("fotoPerfil", sesion.fotoPerfil)
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
                    // Obtener datos completos del usuario para tener fotoPerfil y rol actualizado
                    try {
                        val usuario = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.obtenerUsuario(body.idUsuario)
                        }
                        val sesionCompleta = LoginResponse(
                            mensaje    = body.mensaje,
                            rol        = usuario.rol,
                            idUsuario  = body.idUsuario,
                            nombre     = usuario.nombre,
                            fotoPerfil = usuario.fotoPerfil
                        )
                        sesionActual = sesionCompleta
                        guardarSesion(sesionCompleta)
                        _loginResult.postValue(sesionCompleta)
                    } catch (e: Exception) {
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

    // Actualiza la sesión en memoria Y en SharedPreferences
    fun actualizarSesion(nueva: LoginResponse) {
        sesionActual = nueva
        guardarSesion(nueva)
        _loginResult.postValue(nueva)
    }

    // Actualiza solo el rol en sesión y SharedPreferences
    fun actualizarRol(nuevoRol: String) {
        val actual = sesionActual ?: return
        val nueva = actual.copy(rol = nuevoRol)
        actualizarSesion(nueva)
    }

    // Actualiza foto de perfil en sesión
    fun actualizarFoto(url: String?) {
        val actual = sesionActual ?: return
        val nueva = actual.copy(fotoPerfil = url)
        actualizarSesion(nueva)
    }

    fun cerrarSesion() {
        sesionActual = null
        limpiarSesion()
        _loginResult.postValue(null)
    }

    fun limpiarError() { _loginError.postValue(null) }
}