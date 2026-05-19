package com.example.uniride.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Usuario
import com.example.uniride.model.Vehiculo
import com.example.uniride.model.dto.VehiculoDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilViewModel : ViewModel() {

    private val _mensaje       = MutableLiveData<String?>(null)
    val mensaje: MutableLiveData<String?> = _mensaje

    private val _cargando      = MutableLiveData(false)
    val cargando: MutableLiveData<Boolean> = _cargando

    private val _misVehiculos  = MutableLiveData<List<Vehiculo>?>(emptyList())
    val misVehiculos: MutableLiveData<List<Vehiculo>?> = _misVehiculos

    private val _perfilUsuario = MutableLiveData<Usuario?>(null)
    val perfilUsuario: MutableLiveData<Usuario?> = _perfilUsuario

    // ── Cargar perfil completo desde el backend ────────────────────
    fun cargarPerfil(idUsuario: Long) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val usuario = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerUsuario(idUsuario)
                }
                _perfilUsuario.postValue(usuario)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar perfil: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    // ── Actualizar nombre y teléfono ───────────────────────────────
    fun actualizarPerfil(idUsuario: Long, nombre: String, telefono: String) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val usuario = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.actualizarPerfil(
                        idUsuario,
                        mapOf("nombre" to nombre, "telefono" to telefono)
                    )
                }
                _perfilUsuario.postValue(usuario)
                _mensaje.postValue("Perfil actualizado con éxito")
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    // ── Cambiar contraseña ─────────────────────────────────────────
    fun cambiarContrasena(idUsuario: Long, actual: String, nueva: String) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.cambiarContrasena(
                        idUsuario,
                        mapOf("contrasenaActual" to actual, "contrasenaNueva" to nueva)
                    )
                }
                _mensaje.postValue("Contraseña cambiada con éxito")
            } catch (e: Exception) {
                _mensaje.postValue("Contraseña actual incorrecta")
            }
            _cargando.postValue(false)
        }
    }

    // ── Cargar vehículos del conductor ─────────────────────────────
    fun cargarMisVehiculos(idUsuario: Long) {
        viewModelScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.vehiculosPorUsuario(idUsuario)
                }
                _misVehiculos.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar vehículos: ${e.message}")
            }
        }
    }

    // ── Registrar vehículo usando DTO tipado ───────────────────────
    // Se usa VehiculoDTO en lugar de Map<String, Any> para evitar
    // que Gson serialice Int como Double (bug java.lang.Object cast)
    fun registrarVehiculo(
        placa: String,
        marca: String,
        modelo: String,
        capacidad: Int,
        idUsuario: Long
    ) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.crearVehiculo(
                        VehiculoDTO(
                            placa     = placa,
                            marca     = marca,
                            modelo    = modelo,
                            capacidad = capacidad,
                            idUsuario = idUsuario
                        )
                    )
                }
                _mensaje.postValue("Vehículo registrado con éxito")
                cargarMisVehiculos(idUsuario)
            } catch (e: Exception) {
                _mensaje.postValue("Error al registrar vehículo: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun limpiarMensaje() { _mensaje.postValue(null) }
}