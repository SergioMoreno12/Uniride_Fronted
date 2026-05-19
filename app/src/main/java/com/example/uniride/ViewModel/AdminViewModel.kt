package com.example.uniride.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniride.Service.AdminService
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminViewModel : ViewModel() {

    private val service = AdminService()

    private val _usuarios      = MutableLiveData<List<Usuario>?>(emptyList())
    val usuarios: MutableLiveData<List<Usuario>?> = _usuarios

    private val _viajes        = MutableLiveData<List<Viaje>?>(emptyList())
    val viajes: MutableLiveData<List<Viaje>?> = _viajes

    private val _vehiculos     = MutableLiveData<List<Vehiculo>?>(emptyList())
    val vehiculos: MutableLiveData<List<Vehiculo>?> = _vehiculos

    private val _sedes         = MutableLiveData<List<Sede>?>(emptyList())
    val sedes: MutableLiveData<List<Sede>?> = _sedes

    private val _reservas      = MutableLiveData<List<Reserva>?>(emptyList())
    val reservas: MutableLiveData<List<Reserva>?> = _reservas

    private val _reportes      = MutableLiveData<List<Reporte>?>(emptyList())
    val reportes: MutableLiveData<List<Reporte>?> = _reportes

    private val _notificaciones = MutableLiveData<List<Notificacion>?>(emptyList())
    val notificaciones: MutableLiveData<List<Notificacion>?> = _notificaciones

    private val _mensaje       = MutableLiveData<String?>(null)
    val mensaje: MutableLiveData<String?> = _mensaje

    private val _cargando      = MutableLiveData(false)
    val cargando: MutableLiveData<Boolean> = _cargando

    // ── Usuarios ──────────────────────────────────────────────────────
    fun cargarUsuarios() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) { service.obtenerUsuarios() }
                _usuarios.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar usuarios: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun cambiarRolUsuario(id: Long, nuevoRol: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { service.cambiarRol(id, nuevoRol) }
                _mensaje.postValue("Rol actualizado a $nuevoRol")
                cargarUsuarios()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    fun eliminarUsuario(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { service.eliminarUsuario(id) }
                _mensaje.postValue("Usuario eliminado")
                cargarUsuarios()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    fun toggleActivoUsuario(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { service.toggleActivo(id) }
                _mensaje.postValue("Estado de cuenta actualizado")
                cargarUsuarios()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    // ── Viajes ────────────────────────────────────────────────────────
    fun cargarViajes() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) { service.obtenerViajes() }
                _viajes.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar viajes: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun eliminarViaje(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { service.eliminarViaje(id) }
                _mensaje.postValue("Viaje eliminado")
                cargarViajes()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    // ── Vehiculos ─────────────────────────────────────────────────────
    fun cargarVehiculos() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) { service.obtenerVehiculos() }
                _vehiculos.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar vehículos: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun eliminarVehiculo(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { service.eliminarVehiculo(id) }
                _mensaje.postValue("Vehículo eliminado")
                cargarVehiculos()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    // ── Sedes ─────────────────────────────────────────────────────────
    fun cargarSedes() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) { service.obtenerSedes() }
                _sedes.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar sedes: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun crearSede(nombre: String, ciudad: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { service.crearSede(nombre, ciudad) }
                _mensaje.postValue("Sede creada con éxito")
                cargarSedes()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    fun eliminarSede(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { service.eliminarSede(id) }
                _mensaje.postValue("Sede eliminada")
                cargarSedes()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    // ── Reservas / Estadisticas ───────────────────────────────────────
    fun cargarReservas() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) { service.obtenerReservas() }
                _reservas.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    // ── Reportes ──────────────────────────────────────────────────────
    fun cargarReportes() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) { service.obtenerReportes() }
                _reportes.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar reportes: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun actualizarEstadoReporte(id: Long, estado: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { service.actualizarEstadoReporte(id, estado) }
                _mensaje.postValue("Reporte marcado como $estado")
                cargarReportes()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    // ── Notificaciones ────────────────────────────────────────────────
    fun cargarNotificaciones() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) { service.obtenerNotificaciones() }
                _notificaciones.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar notificaciones: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun enviarNotificacion(titulo: String, mensaje: String, destinatarios: String) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    service.crearNotificacion(titulo, mensaje, destinatarios)
                }
                _mensaje.postValue("Notificación enviada con éxito")
                cargarNotificaciones()
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun limpiarMensaje() { _mensaje.postValue(null) }
}