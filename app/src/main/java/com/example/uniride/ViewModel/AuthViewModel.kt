package com.example.uniride.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniride.Service.AuthService
import com.example.uniride.Service.UsuarioService
import com.example.uniride.model.dto.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    private val authService    = AuthService()
    private val usuarioService = UsuarioService()

    private val _loginResult = MutableLiveData<LoginResponse?>(null)
    val loginResult: MutableLiveData<LoginResponse?> = _loginResult

    private val _mensaje = MutableLiveData<String?>(null)
    val mensaje: MutableLiveData<String?> = _mensaje

    private val _cargando = MutableLiveData(false)
    val cargando: MutableLiveData<Boolean> = _cargando

    var sesionActual: LoginResponse? = null
        private set

    fun login(correo: String, contrasena: String) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val response = withContext(Dispatchers.IO) {
                    authService.login(correo, contrasena)
                }
                if (response.isSuccessful) {
                    sesionActual = response.body()
                    _loginResult.postValue(response.body())
                } else {
                    _mensaje.postValue("Correo o contraseña incorrectos.")
                }
            } catch (e: Exception) {
                _mensaje.postValue("Sin conexión. Verifica tu internet.")
            }
            _cargando.postValue(false)
        }
    }

    fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        telefono: String,
        rol: String,
        onExito: () -> Unit
    ) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    usuarioService.crearUsuario(nombre, correo, contrasena, telefono, rol)
                }
                _mensaje.postValue("Registro exitoso. Ahora inicia sesión.")
                onExito()
            } catch (e: Exception) {
                _mensaje.postValue("Error al registrar: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun cerrarSesion() {
        sesionActual = null
        _loginResult.postValue(null)
    }

    fun limpiarMensaje() { _mensaje.postValue(null) }

    fun actualizarSesion(nuevaSesion: LoginResponse) {
        sesionActual = nuevaSesion
        _loginResult.postValue(nuevaSesion)
    }
}