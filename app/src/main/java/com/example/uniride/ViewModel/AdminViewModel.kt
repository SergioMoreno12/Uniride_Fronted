package com.example.uniride.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.*
import com.example.uniride.model.dto.ActualizarPerfilDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminViewModel : ViewModel() {

    // ── Listas globales ───────────────────────────────────────────────
    private val _usuarios       = MutableLiveData<List<Usuario>>(emptyList())
    val usuarios: MutableLiveData<List<Usuario>> = _usuarios

    private val _viajes         = MutableLiveData<List<Viaje>>(emptyList())
    val viajes: MutableLiveData<List<Viaje>> = _viajes

    private val _vehiculos      = MutableLiveData<List<Vehiculo>>(emptyList())
    val vehiculos: MutableLiveData<List<Vehiculo>> = _vehiculos

    private val _sedes          = MutableLiveData<List<Sede>>(emptyList())
    val sedes: MutableLiveData<List<Sede>> = _sedes

    private val _reservas       = MutableLiveData<List<Reserva>>(emptyList())
    val reservas: MutableLiveData<List<Reserva>> = _reservas

    private val _reportes       = MutableLiveData<List<Reporte>>(emptyList())
    val reportes: MutableLiveData<List<Reporte>> = _reportes

    private val _notificaciones = MutableLiveData<List<Notificacion>>(emptyList())
    val notificaciones: MutableLiveData<List<Notificacion>> = _notificaciones

    // ── Datos de detalle de un usuario ────────────────────────────────
    private val _vehiculosUsuario   = MutableLiveData<List<Vehiculo>>(emptyList())
    val vehiculosUsuario: MutableLiveData<List<Vehiculo>> = _vehiculosUsuario

    private val _viajesUsuario      = MutableLiveData<List<Viaje>>(emptyList())
    val viajesUsuario: MutableLiveData<List<Viaje>> = _viajesUsuario

    private val _reservasUsuario    = MutableLiveData<List<Reserva>>(emptyList())
    val reservasUsuario: MutableLiveData<List<Reserva>> = _reservasUsuario

    private val _calificacionesUsuario = MutableLiveData<List<Calificacion>>(emptyList())
    val calificacionesUsuario: MutableLiveData<List<Calificacion>> = _calificacionesUsuario

    private val _reportesUsuario    = MutableLiveData<List<Reporte>>(emptyList())
    val reportesUsuario: MutableLiveData<List<Reporte>> = _reportesUsuario

    private val _promedioUsuario    = MutableLiveData<Double>(0.0)
    val promedioUsuario: MutableLiveData<Double> = _promedioUsuario

    private val _mensaje            = MutableLiveData<String?>(null)
    val mensaje: MutableLiveData<String?> = _mensaje

    private val _cargando           = MutableLiveData(false)
    val cargando: MutableLiveData<Boolean> = _cargando

    private val _cargandoDetalle    = MutableLiveData(false)
    val cargandoDetalle: MutableLiveData<Boolean> = _cargandoDetalle

    // ── Carga de detalle completo de un usuario ───────────────────────
    fun cargarDetalleUsuario(idUsuario: Long, esConductor: Boolean) {
        viewModelScope.launch {
            _cargandoDetalle.postValue(true)
            try {
                // Reservas como pasajero
                val reservas = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.reservasPorUsuario(idUsuario)
                }
                _reservasUsuario.postValue(reservas)

                // Reportes del usuario
                val reportes = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.reportesPorUsuario(idUsuario)
                }
                _reportesUsuario.postValue(reportes)

                if (esConductor) {
                    // Vehículos
                    val vehiculos = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.vehiculosPorUsuario(idUsuario)
                    }
                    _vehiculosUsuario.postValue(vehiculos)

                    // Viajes de cada vehículo
                    val todosViajes = mutableListOf<Viaje>()
                    for (v in vehiculos) {
                        val viajesVehiculo = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.viajesPorVehiculo(v.idVehiculo)
                        }
                        todosViajes.addAll(viajesVehiculo)
                    }
                    _viajesUsuario.postValue(todosViajes)

                    // Calificaciones y promedio
                    val califs = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.calificacionesConductor(idUsuario)
                    }
                    _calificacionesUsuario.postValue(califs)

                    val promedio = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.promedioConductor(idUsuario)
                    }
                    _promedioUsuario.postValue(promedio)
                } else {
                    _vehiculosUsuario.postValue(emptyList())
                    _viajesUsuario.postValue(emptyList())
                    _calificacionesUsuario.postValue(emptyList())
                    _promedioUsuario.postValue(0.0)
                }
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar datos del usuario")
            }
            _cargandoDetalle.postValue(false)
        }
    }

    fun limpiarDetalleUsuario() {
        _vehiculosUsuario.postValue(emptyList())
        _viajesUsuario.postValue(emptyList())
        _reservasUsuario.postValue(emptyList())
        _calificacionesUsuario.postValue(emptyList())
        _reportesUsuario.postValue(emptyList())
        _promedioUsuario.postValue(0.0)
    }

    // ── Usuarios ──────────────────────────────────────────────────────
    fun cargarUsuarios() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerUsuarios()
                }
                _usuarios.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar usuarios")
            }
            _cargando.postValue(false)
        }
    }

    fun cambiarRol(idUsuario: Long, nuevoRol: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.actualizarPerfil(
                        idUsuario,
                        ActualizarPerfilDTO(rol = nuevoRol)
                    )
                }
                _mensaje.postValue("Rol actualizado a $nuevoRol")
                cargarUsuarios()
            } catch (e: Exception) {
                _mensaje.postValue("Error al cambiar rol")
            }
        }
    }

    fun toggleActivoUsuario(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.toggleActivo(id)
                }
                _mensaje.postValue("Estado de cuenta actualizado")
                cargarUsuarios()
            } catch (e: Exception) {
                _mensaje.postValue("Error al actualizar estado")
            }
        }
    }

    fun eliminarUsuario(id: Long, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.eliminarUsuario(id)
                }
                _mensaje.postValue("Usuario eliminado correctamente")
                cargarUsuarios()
                onExito()
            } catch (e: Exception) {
                _mensaje.postValue("Error al eliminar usuario: ${e.message}")
            }
        }
    }

    // ── Viajes ────────────────────────────────────────────────────────
    fun cargarViajes() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerViajes()
                }
                _viajes.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar viajes")
            }
            _cargando.postValue(false)
        }
    }

    fun eliminarViaje(id: Long, idUsuario: Long? = null, esConductor: Boolean = false) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.eliminarViaje(id)
                }
                _mensaje.postValue("Viaje eliminado")
                // Recargar datos relevantes
                if (idUsuario != null) cargarDetalleUsuario(idUsuario, esConductor)
                else cargarViajes()
            } catch (e: Exception) {
                _mensaje.postValue("Error al eliminar viaje")
            }
        }
    }

    // ── Vehículos ─────────────────────────────────────────────────────
    fun cargarVehiculos() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerVehiculos()
                }
                _vehiculos.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar vehículos")
            }
            _cargando.postValue(false)
        }
    }

    fun eliminarVehiculo(id: Long, idUsuario: Long? = null, esConductor: Boolean = false) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.eliminarVehiculo(id)
                }
                _mensaje.postValue("Vehículo eliminado")
                if (idUsuario != null) cargarDetalleUsuario(idUsuario, esConductor)
                else cargarVehiculos()
            } catch (e: Exception) {
                _mensaje.postValue("Error al eliminar vehículo")
            }
        }
    }

    // ── Sedes ─────────────────────────────────────────────────────────
    fun cargarSedes() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerSedes()
                }
                _sedes.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar sedes")
            }
            _cargando.postValue(false)
        }
    }

    fun crearSede(nombre: String, ciudad: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.crearSede(
                        mapOf("nombreSede" to nombre, "ciudad" to ciudad)
                    )
                }
                _mensaje.postValue("Sede creada con éxito")
                cargarSedes()
            } catch (e: Exception) {
                _mensaje.postValue("Error al crear sede")
            }
        }
    }

    fun eliminarSede(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.eliminarSede(id)
                }
                _mensaje.postValue("Sede eliminada")
                cargarSedes()
            } catch (e: Exception) {
                _mensaje.postValue("Error al eliminar sede")
            }
        }
    }

    // ── Estadísticas ──────────────────────────────────────────────────
    fun cargarReservas() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerReservas()
                }
                _reservas.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar reservas")
            }
            _cargando.postValue(false)
        }
    }

    fun cargarTodo() {
        cargarUsuarios()
        cargarViajes()
        cargarReservas()
        cargarVehiculos()
    }

    // ── Reportes ──────────────────────────────────────────────────────
    fun cargarReportes() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerReportes()
                }
                _reportes.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar reportes")
            }
            _cargando.postValue(false)
        }
    }

    fun actualizarEstadoReporte(id: Long, estado: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.actualizarReporte(
                        id, mapOf("estado" to estado)
                    )
                }
                _mensaje.postValue("Reporte marcado como $estado")
                cargarReportes()
            } catch (e: Exception) {
                _mensaje.postValue("Error al actualizar reporte")
            }
        }
    }

    // ── Notificaciones ────────────────────────────────────────────────
    fun cargarNotificaciones() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerNotificaciones()
                }
                _notificaciones.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar notificaciones")
            }
            _cargando.postValue(false)
        }
    }

    fun enviarNotificacion(titulo: String, mensaje: String, destinatarios: String) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.crearNotificacion(
                        mapOf(
                            "titulo"        to titulo,
                            "mensaje"       to mensaje,
                            "destinatarios" to destinatarios
                        )
                    )
                }
                _mensaje.postValue("Notificación enviada con éxito")
                cargarNotificaciones()
            } catch (e: Exception) {
                _mensaje.postValue("Error al enviar notificación")
            }
            _cargando.postValue(false)
        }
    }

    fun limpiarMensaje() { _mensaje.postValue(null) }
}